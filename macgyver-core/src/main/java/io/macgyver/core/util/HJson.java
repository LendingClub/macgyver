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
package io.macgyver.core.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.crsh.console.jline.internal.InputStreamReader;
import org.hjson.JsonValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HJson {

	private static ObjectMapper mapper = new ObjectMapper();

	public static JsonNode parse(String input) throws IOException {
		return mapper.readTree(JsonValue.readHjson(input).toString());
	}

	public static JsonNode parse(InputStream input) throws Exception {
		return mapper.readTree(JsonValue.readJSON(new InputStreamReader(input))
				.toString());
	}

	public static JsonNode parse(Reader input) throws Exception {
		return mapper.readTree(JsonValue.readJSON(input).toString());
	}

	public static JsonNode parse(File input) throws IOException {

		try (FileReader fr = new FileReader(input)) {
			return mapper.readTree(JsonValue.readHjson(fr).toString());
		}
	}
}
