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
package io.macgyver.mongodb;

import io.macgyver.test.MacGyverIntegrationTest;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDBFactoryBeanIntegrationTest extends MacGyverIntegrationTest {

	
	@Autowired
	ApplicationContext applicationContext;
	
	
	@Test
	@Ignore
	public void testIt() {
		Mongo m =  applicationContext.getBean("mongotest",Mongo.class);
		
		DB db = applicationContext.getBean("mongotestDB",DB.class);
		
		Assert.assertNotNull(m);
		Assert.assertNotNull(db);
		
	}
	
	@Before
	public void checkMongo() throws UnknownHostException {

		try {
			MongoClientURI curi = new MongoClientURI(
					"mongodb://localhost/macgyver_junit");
			MongoClient c = new MongoClient(curi);

			DB db = c.getDB("macgyver_junit");

			db.getCollectionNames();
		} catch (Exception e) {
			Assume.assumeTrue(false);
		}
	}
}
