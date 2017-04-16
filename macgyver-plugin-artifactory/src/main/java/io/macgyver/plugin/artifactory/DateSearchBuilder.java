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
package io.macgyver.plugin.artifactory;

import io.macgyver.okrest3.OkRestTarget;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;

public class DateSearchBuilder extends AbstractSearchBuilder<DateSearchBuilder> {

	Set<DateField> dateFields = Sets.newHashSet();

	public static enum DateField {

		LAST_MODIFIED("lastModified"), LAST_DOWNLOADED("lastDownloaded"), CREATED(
				"created");

		private final String id;

		DateField(String f) {
			id = f;
		}

		public String value() {
			return id;
		}
	}

	public DateSearchBuilder(OkRestTarget r) {
		super(r, "/api/search/dates");

	}

	public <C extends DateSearchBuilder> C from(long f) {
		target = target.queryParam("from", "" + f);
		return (C) this;
	}

	public <C extends DateSearchBuilder> C fromDaysAgo(long d) {

		long delta = TimeUnit.DAYS.toMillis(d);

		long t0 = System.currentTimeMillis() - delta;
		return from(t0);
	}

	public <C extends DateSearchBuilder> C to(long f) {
		target = target.queryParam("to", "" + f);
		return (C) this;
	}

	public <C extends DateSearchBuilder> C toDaysAgo(long d) {

		long delta = TimeUnit.DAYS.toMillis(d);

		long t0 = System.currentTimeMillis() - delta;
		return to(t0);
	}

	@Override
	public void formatRequest() throws IOException {

		super.formatRequest();
		
		if (dateFields.isEmpty()) {
			dateFields.add(DateField.CREATED);
		}

		String s = "";
		for (DateField d : dateFields) {
			if (s.length() > 0) {
				s = s + ",";
			}
			s = s + d.value();

		}

		target = target.queryParam("dateFields", s);
	}

	public <C extends DateSearchBuilder> C forDateField(DateField... df) {

		for (DateField d : df) {

			dateFields.add(d);

		}

		return (C) this;
	}

}
