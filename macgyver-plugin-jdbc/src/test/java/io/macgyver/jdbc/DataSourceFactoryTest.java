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
package io.macgyver.jdbc;

import io.macgyver.core.Kernel;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.test.MacGyverIntegrationTest;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.davidmoten.rx.jdbc.Database;

public class DataSourceFactoryTest extends MacGyverIntegrationTest {

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	Kernel kernel;

	@Autowired
	ServiceRegistry registry;

	@Test
	public void testApplicationContextIntegrity() {
		Assert.assertNotNull(applicationContext);
		Assert.assertSame(Kernel.getInstance(), kernel);
		Assert.assertSame(Kernel.getInstance(),
				applicationContext.getBean("macKernel"));

	}

	@Test
	public void testAutoRegistration() throws SQLException {

		JdbcTemplate t = (JdbcTemplate) registry.get("testdsTemplate");
		Assert.assertNotNull(t);
		Assert.assertSame(t, registry.get("testdsTemplate"));

		DataSource ds = (DataSource) registry.get("testds");
		Assert.assertNotNull(ds);

		DataSource ds1 = (DataSource) registry.get("testds");
		DataSource ds2 = (DataSource) registry.get("testds");
		Assert.assertNotNull(ds1);
		Assert.assertSame(ds1, ds2);
		Assert.assertSame(ds, t.getDataSource());
	}



}
