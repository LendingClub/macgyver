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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.concurrent.TimeUnit;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.davidmoten.rx.jdbc.ResultSetMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import io.macgyver.core.util.JsonNodes;
import rx.functions.Func1;

public class JacksonRxJdbcResultSetMapper implements ResultSetMapper<ObjectNode> {

	Func1<String, String> columnNameTranslator = new IdentityColumnNameTranslator();

	static Cache<ResultSet, List<ColumnInfo>> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(10, TimeUnit.SECONDS)
			.maximumSize(200)
			.build();

	public static class IdentityColumnNameTranslator implements Func1<String, String> {

		@Override
		public String call(String t) {
			return t;
		}

	};

	public static class UpperCaseColumnNameTranslator implements Func1<String, String> {

		@Override
		public String call(String t) {
			return t.toUpperCase();
		}

	};

	public static class LowerCaseColumnNameTranslator implements Func1<String, String> {

		@Override
		public String call(String t) {
			return t.toLowerCase();
		}

	};

	public JacksonRxJdbcResultSetMapper withLowerCaseColumnNames() {
		return withColumnNameTranslator(new LowerCaseColumnNameTranslator());
	}

	public JacksonRxJdbcResultSetMapper withUpperCaseColumnNames() {
		return withColumnNameTranslator(new UpperCaseColumnNameTranslator());
	}

	public JacksonRxJdbcResultSetMapper withColumnNameTranslator(Func1<String, String> x) {
		this.columnNameTranslator = x;
		return this;
	}

	class ColumnInfo {
		String columnName;
		int columnNumber;
		int type;
	}

	protected List<ColumnInfo> getColumnInfo(ResultSet rs) throws SQLException {
		List<ColumnInfo> list = cache.getIfPresent(rs);
		if (list != null) {
			return list;
		}

		list = Lists.newArrayList();
		ResultSetMetaData md = rs.getMetaData();
		int cc = md.getColumnCount();
		for (int i = 1; i <= cc; i++) {
			String columnName = columnNameTranslator.call(md.getColumnName(i));
			ColumnInfo ci = new ColumnInfo();
			ci.columnNumber = i;
			ci.columnName = columnName;
			ci.type = md.getColumnType(i);
			list.add(ci);
		}
		cache.put(rs, list);
		return list;
	}

	@Override
	public ObjectNode call(ResultSet rs) throws SQLException {
		ObjectNode n = JsonNodes.mapper.createObjectNode();

		for (ColumnInfo ci : getColumnInfo(rs)) {

			switch (ci.type) {
			case (Types.NUMERIC):
			case (Types.DECIMAL):
			case (Types.REAL):
			case (Types.FLOAT):
			case (Types.DOUBLE):
				n.put(ci.columnName, rs.getBigDecimal(ci.columnNumber));
				break;
			case (Types.INTEGER):
			case (Types.SMALLINT):
			case (Types.TINYINT):
				n.put(ci.columnName, rs.getInt(ci.columnNumber));
				break;
			case (Types.BIGINT):
				n.put(ci.columnName, rs.getLong(ci.columnNumber));
				break;
			case (Types.BOOLEAN):
			case (Types.BIT):
				n.put(ci.columnName, rs.getBoolean(ci.columnNumber));
				break;
			case (Types.DATE):

				n.put(ci.columnName, rs.getString(ci.columnNumber));
				break;
			case (Types.TIME):
				n.put(ci.columnName, rs.getString(ci.columnNumber));
				break;
			case (Types.TIMESTAMP):
				n.put(ci.columnName, rs.getString(ci.columnNumber));
				break;
			default:
				n.put(ci.columnName, rs.getString(ci.columnNumber));
				break;
			}

		}
		return n;
	}

}
