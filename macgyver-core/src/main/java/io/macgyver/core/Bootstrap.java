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
package io.macgyver.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

/**
 * The purpose of bootstrap is to collect configuration settings that need to be
 * configured before they are passed to Spring for initialization.
 *
 * @author rschoening
 *
 */
public class Bootstrap {

	static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public static Bootstrap instance = new Bootstrap();

	protected Properties bootstrapProps = new Properties();

	private File macgyverHome;

	public static Bootstrap getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}
		return instance;
	}

	protected Bootstrap() {

	}


	public File getMacGyverHome() {
	
		if (macgyverHome==null) {
			return new File(".");
		}
		return macgyverHome;
	}
	public File getWebDir() {
		return new File(getMacGyverHome(),"web");
	}
	public File getConfigDir() {
		return new File(getMacGyverHome(),"config");
	}
	public File getLogDir() {
		return new File(getMacGyverHome(),"logs");
	}
	public File getDataDir() {
		return new File(getMacGyverHome(),"data");
	}
	public File getScriptsDir() {
		return new File(getMacGyverHome(),"scripts");
	}


	AtomicBoolean initialized = new AtomicBoolean(false);

	public File resolveConfig(String name) {
		return new File(getConfigDir(),name);
	}
	protected File findLocation(String name) throws  MalformedURLException {



		String syspropKey = "macgyver."+name.toLowerCase()+".dir";
		String val = System.getProperty(syspropKey);
		if (!Strings.isNullOrEmpty(val)) {
			logger.info("resolved location ("+name+") via sysprop: "+syspropKey+"="+val);
			return new File(val);
		}




		String envKey = "MACGYVER_EXT_"+name.toUpperCase().trim()+"_DIR";
		val = System.getenv(envKey);
		if (!Strings.isNullOrEmpty(val)) {
			logger.info("resolved location ("+name+") via env var: "+envKey+"="+val);
			return new File(val);
		}


		val = new File(getMacGyverHome(),name).getAbsolutePath();
		logger.info("resolved location ("+name+") via macgyver.home: "+val);
		return new File(val);
	}
	public synchronized void init(Properties p) {
		if (initialized.get()) {
		//	throw new IllegalStateException("Already initialized");
			//return;
		}

		bootstrapProps.putAll(p);

		String val = bootstrapProps.getProperty("macgyver.home");

		Preconditions.checkState(!Strings.isNullOrEmpty(val),"macgyver.home must be set");
		try {
			macgyverHome = new File(val).getCanonicalFile();
		}
		catch (IOException e) {
			macgyverHome = new File(val).getAbsoluteFile();
		}




			logger.info("macgyver    home : {}",getMacGyverHome());
			logger.info("macgyver  config : {}",getConfigDir());
			logger.info("macgyver scripts : {}",getScriptsDir());
			logger.info("macgyver    data : {}",getDataDir());
			logger.info("macgyver     web : {}",getWebDir());



			// need to move this block upstream to Bootstrap

			String templateRoots = computeTemplateRoots();




		initialized.set(true);


	}
	public static String computeTemplateRoots() {
		try {
			File webDir = Bootstrap.getInstance()
					.getWebDir();
			String templateRoots = "classpath:/web/templates";

			templateRoots = webDir.toURI().toURL().toString() + ","
					+ templateRoots;

			return templateRoots;
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e);
		}

	}
	
	public static String getBannerText() {
		// Spring boot doesn't support alternate banner until 1.1.x
				String bannerText = "\n";
				try {
					URL url = ServerMain.class.getResource("/banner_alt.txt");
					if (url != null) {
						try (InputStreamReader reader = new InputStreamReader(
								url.openStream(), Charsets.UTF_8)) {
							String text = CharStreams.toString(reader);

							bannerText += text;
						}
					}

					url = null;
					url = ServerMain.class
							.getResource("/macgyver-core-revision.properties");
					if (url != null) {
						Properties p = new Properties();
						try (InputStream x = url.openStream()) {
							p.load(x);
						}

						bannerText += String.format(
								"\n\n                      (v%s rev:%s)\n",
								p.getProperty("version"),
								p.getProperty("gitShortCommitId"));
					}

				} catch (Exception e) {
					logger.warn("could not load banner");
				}
				return bannerText;
	}
	public static void printBanner() {

		
		logger.info(getBannerText());
	}


}
