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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.cli.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import io.macgyver.cli.CLI;
import io.macgyver.cli.CLIException;
import io.macgyver.cli.Command;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Parameters()
public class UpdateCommand extends Command {

	Logger logger = LoggerFactory.getLogger(UpdateCommand.class);

	@Parameter(names = { "--url" }, arity = 1, description = "url for macgyver server")
	String url;

	@Override
	public String getServerUrl() {
		if (url != null) {
			return url;
		}
		return super.getServerUrl();
	}

	@Override
	public void execute() throws IOException {

		String exe = System.getProperty("cli.exe");
		if (Strings.isNullOrEmpty(exe)) {
			throw new CLIException("update can only be called from packaged executable");

		}
		File exeFile = new File(exe);
		File exeBackup = new File(exe + ".bak");

		Files.copy(exeFile, exeBackup);

		File tempDir = Files.createTempDir();
		File tempExe = new File(tempDir, "macgyver");
		Response response = getOkRestTarget().path("/cli/download").get().execute().response();
		if (!response.isSuccessful()) {
			throw new CLIException("could not download CLI");
		}
		try (InputStream is = response.body().byteStream()) {
			try (FileOutputStream fos = new FileOutputStream(tempExe)) {
				ByteStreams.copy(is, fos);
				Files.copy(tempExe, exeFile);
				tempExe.setExecutable(true);
				statusOutput("successfully updated " + exeFile.getAbsolutePath());
			}
		}

	}

	@Override
	public HttpLoggingInterceptor createLogger() {
		// This will be a binary response, but with a plain-text header.  OkHttp's binary content detection
		// is confused by this.  So we just force it to HEADERS-only mode.
		return super.createLogger().setLevel(Level.HEADERS);
	}

}
