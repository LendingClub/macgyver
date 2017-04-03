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
package io.macgyver.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Strings;

@SpringApplicationConfiguration(classes = TestConfig.class,initializers={SoftDependencyInitializer.class})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class MacGyverIntegrationTest extends
		AbstractJUnit4SpringContextTests {

	private static Properties privateProperties = new Properties();
	@Autowired
	protected ApplicationContext applicationContext;

	static volatile Boolean neo4jAvailable = null;

	protected static Logger logger = LoggerFactory
			.getLogger(MacGyverIntegrationTest.class);

	public static synchronized boolean isNeo4jAvailable() {


		if (neo4jAvailable == null) {

			try {
				neo4jAvailable = new NeoRxClient.Builder().build().checkConnection(); // TODO configurable endpoint

			} catch (Exception e) {
				logger.info("problem communicating with neo4j", e);
				neo4jAvailable = false;
			}

			if (!neo4jAvailable) {
				logger.warn("neo4j not available -- integration tests will be skipped");
			}
			else {
				logger.info("neo4j is available for integration tests");
			}

		}
		return neo4jAvailable;
	}

	@BeforeClass
	public static void setup() throws IOException {



		String macGyverHome = System.getProperty("macgyver.home");

		if (Strings.isNullOrEmpty(macGyverHome)) {
			File dir = new File("./src/test/resources/ext");
			macGyverHome = dir.getAbsolutePath();
			System.setProperty("macgyver.home", ".");

		}


		logger.info("macgyver.home: " + macGyverHome);


		File f = new File(System.getProperty("user.home"),
				".macgyver/private-test.properties");
		if (f.exists()) {

			try (InputStream is = new FileInputStream(f)) {
				privateProperties.load(is);
			} finally {
				//
			}
		}

		//Assume.assumeTrue(isNeo4jAvailable());
	}

	@Before
	public void checkNeo() {
		Assume.assumeTrue(isNeo4jAvailable());
		
		logger.info("running tests in environment: {}",applicationContext.getEnvironment());

	}
	/**
	 * Allows integration tests to be written using properties that are held
	 * outside the project.
	 *
	 * @param key
	 */
	public String getPrivateProperty(String key) {
		return privateProperties.getProperty(key);
	}

	@After
	public void cleanupTestData() {
		if (neo4jAvailable!=null && neo4jAvailable.booleanValue()) {
			NeoRxClient neo4j = applicationContext.getBean(NeoRxClient.class);
			try {
				neo4j.execCypher("match (x)-[y]-() where x.name=~'(i?)junit.*' delete y");
				neo4j.execCypher("match (x) where x.name=~'(i?)junit.*' delete x");

			} catch (Exception e) {
				// ignore
			}
		}
	}
	
	
	public boolean isRunningInCircleCI() {
		return !Strings.isNullOrEmpty(System.getenv("CIRCLECI"));
	}
}
