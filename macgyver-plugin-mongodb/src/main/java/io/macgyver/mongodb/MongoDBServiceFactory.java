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

import io.macgyver.core.ConfigurationException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Component
public class MongoDBServiceFactory extends ServiceFactory<MongoClient> {

	

	public MongoDBServiceFactory() {
		super("mongodb");
	}

	public Object doCreateInstance(ServiceDefinition def) {
		Properties props = def.getProperties();
		try {
			return new ExtendedMongoClient(new MongoClientURI(
					injectCredentials(props.getProperty("uri"),
							props.getProperty("username"),
							props.getProperty("password"))));
		} catch (UnknownHostException e) {
			throw new ConfigurationException(e);
		}
	}



	public class ExtendedMongoClient extends MongoClient {

		MongoClientURI mongoClientUri;

		public ExtendedMongoClient(MongoClientURI uri)
				throws UnknownHostException {
			super(uri);
			this.mongoClientUri = uri;

		}

		public String getDatabaseName() {
			return mongoClientUri.getDatabase();
		}
	}

	



	public String injectCredentials(String uri, String username, String password) {
		Preconditions.checkNotNull(uri);
		Preconditions.checkArgument(uri.startsWith("mongodb://"),
				"mongo uri must start with mongodb://");
		if (!Strings.isNullOrEmpty(username)
				&& !Strings.isNullOrEmpty(password)) {
			if (uri.contains("@")) {
				throw new IllegalArgumentException(
						"mongo uri must not contain credentials if you are injecting them");
			}
			uri = uri.replace("mongodb://", "mongodb://" + username + ":"
					+ password + "@");
			return uri;

		} else {
			return uri;
		}
	}

	public static class DBFactoryShim implements FactoryBean<DB> {

		MongoClient client;

		DBFactoryShim(MongoClient client) {
			this.client = client;
		}

		@Override
		public DB getObject() throws Exception {

			ExtendedMongoClient extendedMongoClient = (ExtendedMongoClient) client;
			return extendedMongoClient.getDB(extendedMongoClient
					.getDatabaseName());
		}

		@Override
		public Class<?> getObjectType() {
			return DB.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

	}





	@Override
	protected void doCreateCollaboratorInstances(
			ServiceRegistry registry,
			ServiceDefinition primaryDefinition, Object primaryBean) {
		
		ExtendedMongoClient c = (ExtendedMongoClient) primaryBean;
		DB db = c.getDB(c.getDatabaseName());
		MongoTemplate template = new MongoTemplate(db.getMongo(),c.getDatabaseName());
		
		registry.registerCollaborator(primaryDefinition.getName()+"DB", db);
		registry.registerCollaborator(primaryDefinition.getName()+"Template", template);
		
	}

	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition def) {
		ServiceDefinition dbDef = new ServiceDefinition(def.getName()+"DB", def.getName(), def.getProperties(), this);
		defSet.add(dbDef);
	}
}
