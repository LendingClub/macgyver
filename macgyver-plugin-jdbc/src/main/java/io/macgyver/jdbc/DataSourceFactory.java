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
package io.macgyver.jdbc;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;

import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.base.Throwables;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceFactory extends ServiceFactory<DataSource> {

	public DataSourceFactory() {
		super("dataSource");
	}

	@Override
	protected Object doCreateInstance(ServiceDefinition def) {
		try {
			Properties p = new Properties();
			// set some defaults
			p.put("autoCommit", Boolean.TRUE.toString());

			p.putAll(def.getProperties());

			if (p.contains("driverClass")) {
				p.put("driverClassName", p.getProperty("driverClass"));
				p.remove("driverClass");
			}
			if (p.contains("defaultAutoCommit")) {
				p.put("autoCommit", p.getProperty("defaultAutoCommit"));
				p.remove("defaultAutoCommit");
			}


			HikariConfig cp = new HikariConfig(p);


			HikariDataSource ds = new HikariDataSource(cp);


			return ds;
		} catch (Exception e) {
			Throwables.propagateIfPossible(e, MacGyverException.class);
			throw new MacGyverException(e);
		}
	}







	@Override
	protected void doCreateCollaboratorInstances(
			ServiceRegistry registry,
			ServiceDefinition primaryDefinition, Object primaryBean) {
		DataSource ds = (DataSource) primaryBean;
		JdbcTemplate t = new JdbcTemplate(ds, true);
		registry.registerCollaborator(primaryDefinition.getPrimaryName()+"Template", t);
		
		Database rx = Database.fromDataSource(ds);
		registry.registerCollaborator(primaryDefinition.getPrimaryName()+"RxJdbc", rx);
	}

	@Override
	public void doCreateCollaboratorDefinitions(Set<ServiceDefinition> defSet,
			ServiceDefinition primary) {
		ServiceDefinition def = new ServiceDefinition(primary.getName()+"Template", primary.getName(), primary.getProperties(), this);

		defSet.add(def);
		
		ServiceDefinition rxJdbc = new ServiceDefinition(primary.getName()+"RxJdbc", primary.getName(), primary.getProperties(), this);

		defSet.add(rxJdbc);

	}


}
