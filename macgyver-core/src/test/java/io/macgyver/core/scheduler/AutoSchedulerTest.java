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
package io.macgyver.core.scheduler;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AutoSchedulerTest {

	@Test
	public void testCrontabScanner() throws IOException {

		File f = File.createTempFile("junit", "junit");
		String cronExpression = "* * * * * ?";
		String expected = "{\"cron\":\""+cronExpression+"\"}";
		String data = "blah\nblah\n//   #@Schedule    " + expected + "   \n\nblah";
		Files.write(data, f, Charsets.UTF_8);

	//	LineProcessor<Optional<ObjectNode>> lp = new ScheduleScanner.CrontabLineProcessor();

	//	Optional<ObjectNode> x = Files.readLines(f, Charsets.UTF_8, lp);

	//	Assert.assertTrue(x.isPresent());
	//	org.junit.Assert.assertEquals(cronExpression, x.get().get("cron").asText());
	}
}
