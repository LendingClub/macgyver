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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.davidmoten.rx.jdbc.Database;

import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.test.MacGyverIntegrationTest;

public class RxJdbcTest extends MacGyverIntegrationTest {
	@Autowired
	ServiceRegistry registry;

	@Test
	public void testRxJdbc() {
		Database db = registry.get("testdsRxJdbc");
		Assertions.assertThat(db).isNotNull();

		db.update("create table test_rx(abc int, x_date date, x_time time, x_timestamp timestamp);").execute();

		db.update("insert into test_rx(abc, x_date,x_time,x_timestamp) values (1,now(),now(),now())").execute();
		db.update("insert into test_rx(abc, x_date,x_time,x_timestamp) values (10,now(),now(),now())").execute();

		Assertions.assertThat(db.select("select abc from test_rx").getAs(Integer.class).toList().toBlocking().first())
				.contains(new Integer(1), new Integer(10));

		ObjectNode n = db.select("select avg(abc) as X  from test_rx")
				.get(new JacksonRxJdbcResultSetMapper().withLowerCaseColumnNames()).toBlocking().first();

		Assertions.assertThat(n.path("x").asInt()).isEqualTo(5);
		
		n = db.select("select avg(abc) as X  from test_rx")
				.get(new JacksonRxJdbcResultSetMapper().withUpperCaseColumnNames()).toBlocking().first();

		Assertions.assertThat(n.get("X").asInt()).isEqualTo(5);
		
		
		n = db.select("select avg(abc) as X  from test_rx")
				.get(new JacksonRxJdbcResultSetMapper()).toBlocking().first();

		Assertions.assertThat(n.get("X").asInt()).isEqualTo(5);

	}
}
