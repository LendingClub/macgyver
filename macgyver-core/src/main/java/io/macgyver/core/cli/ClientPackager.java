/**
 * Copyright 2017 Lending Club, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.cli;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.ZipException;

import org.apache.mina.filter.buffer.BufferedWriteFilter;
import org.crsh.console.jline.internal.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import io.macgyver.cli.CLI;
import io.macgyver.cli.Command;
import io.macgyver.core.Bootstrap;
import io.macgyver.core.Kernel;
import io.macgyver.core.resource.Resource;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

public class ClientPackager {

	public static final String CLIENT_DIR_NAME = ".cli-generated";
	Logger logger = LoggerFactory.getLogger(ClientPackager.class);
	File targetDir = new File(".", CLIENT_DIR_NAME);

	public ClientPackager withOutputDir(File output) {
		targetDir = output;
		return this;
	}

	public File getMacGyvverCLIExecutable() {
		return new File(targetDir, "macgyver");
	}

	public synchronized File rebuildIfNecessary() throws IOException {
		File f = getMacGyvverCLIExecutable();
		if (f.exists()) {
			return f;
		}
		return rebuild();
	}

	public synchronized File rebuild() throws IOException {
		try {
			File f = getMacGyvverCLIExecutable();
			f.delete();
			buildCli();
			return f;
		} catch (net.lingala.zip4j.exception.ZipException e) {
			throw new IOException(e);
		}
	}

	private synchronized File buildCli() throws IOException, net.lingala.zip4j.exception.ZipException {

		File tempDir = Files.createTempDir();

		File f = new File(tempDir, "collected-cli-classes.jar");

		ZipFile z = new ZipFile(f);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		for (String packageName : CLI.findPackageNamesToSearch()) {

			logger.info("searching pacakge for CLI commands: {}", packageName);
			ClassPath.from(cl).getTopLevelClasses(packageName)
					.forEach(it -> {
						
						if (isToBePackaged(it)) {
							logger.info("adding command class: {}", it);
							try (InputStream input = it.url().openStream()) {
								byte[] b = ByteStreams.toByteArray(input);

								ZipParameters zp = new ZipParameters();
								zp.setSourceExternalStream(true);
								zp.setFileNameInZip(it.getName().replace(".",
										"/") + ".class");
								z.addStream(new ByteArrayInputStream(b), zp);
							} catch (IOException | net.lingala.zip4j.exception.ZipException e) {
								throw new RuntimeException(e);
							}
						}

					});
		}

		File generatedJar = new File(tempDir, "macgyver-capsule-with-extensions.jar");

		try (InputStream in = cl.getResourceAsStream("cli/macgyver-cli-capsule.jar")) {
			Preconditions.checkNotNull(in,
					"resource cli/macgyver-cli-capsule.jar could not be loaded.  Did you run gradle install in macgyver-cli?");
			try (FileOutputStream out = new FileOutputStream(generatedJar)) {

				ByteStreams.copy(in, out);
			}
		}

		ZipFile mj = new ZipFile(generatedJar);
		ZipParameters zp = new ZipParameters();
		zp.setFileNameInZip("collected-cli-classes.jar");

		try {
			mj.removeFile("collected-cli-classes.jar");
		} catch (Exception e) {
		}
		mj.addFile(f, zp);

		targetDir.mkdirs();
		File executable = new File(targetDir, "macgyver");
		executable.delete();

		PrintWriter bw = new PrintWriter(new FileWriter(executable));

		org.springframework.core.io.Resource resource = Kernel.getApplicationContext()
				.getResource("classpath:cli/cli-jar-header.sh");
		try (InputStreamReader isr = new InputStreamReader(resource.getInputStream())) {
			String script = CharStreams.toString(isr);
			bw.println(script);
		}
		// bw.println("#!/bin/bash");
		// bw.println("exec java -Dcli.exe=\"$0\" -Dcli.launch=true -jar $0
		// \"$@\"");

		bw.close();

		try (FileInputStream fis = new FileInputStream(generatedJar)) {
			try (FileOutputStream fos = new FileOutputStream(executable, true)) {
				ByteStreams.copy(fis, fos);
			}
		}
		executable.setExecutable(true);

		return executable;
	}
	
	public boolean isToBePackaged(ClassInfo ci) {
		try {
			Class z = Thread.currentThread().getContextClassLoader().loadClass(ci.getName());
			if (Command.class.isAssignableFrom(z)) {
				return true;
			}
		}
		catch (Exception e) {
			logger.debug("problem scanning class: "+ci);
		}
		return false;
	}
}
