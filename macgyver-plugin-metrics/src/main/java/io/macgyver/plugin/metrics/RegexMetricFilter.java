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
package io.macgyver.plugin.metrics;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.base.Strings;

public class RegexMetricFilter implements MetricFilter {

	List<Pattern> excludePatterns = Lists.newCopyOnWriteArrayList();
	List<Pattern> includePatterns = Lists.newCopyOnWriteArrayList();

	public RegexMetricFilter() {

	}

	public RegexMetricFilter excludes(String s) {
	
		Splitter.on(CharMatcher.anyOf(" \n\t\r,;|")).omitEmptyStrings().splitToList(Strings.nullToEmpty(s)).forEach(it -> {
			
			exclude(Pattern.compile(it));
			
		});
		return this;
	}
	
	
	
	public RegexMetricFilter includes(String s) {
		
		Splitter.on(CharMatcher.anyOf(" \n\t\r,;|")).omitEmptyStrings().splitToList(Strings.nullToEmpty(s)).forEach(it -> {
			
			include(Pattern.compile(it));
			
		});
		return this;
	}
	
	
	public RegexMetricFilter exclude(String s) {
		exclude(Pattern.compile(s));
		return this;
	}
	public RegexMetricFilter exclude(Pattern p) {
		
		Preconditions.checkNotNull(p);
		excludePatterns.add(p);
		return this;
	}
	
	public RegexMetricFilter include(String s) {
		include(Pattern.compile(s));
		return this;
	}
	public RegexMetricFilter include(Pattern p) {
		Preconditions.checkNotNull(p);
		includePatterns.add(p);
		return this;
	}

	@Override
	public boolean matches(String n, Metric metric) {

		boolean include = true;

		Iterator<Pattern> t = excludePatterns.iterator();

		while (include && t.hasNext()) {
			Pattern p = t.next();
			Matcher m = p.matcher(n);
			if (m.matches()) {
				include=false;
			}
		}
		if (include) {
			return true;
		}
		
		t = includePatterns.iterator();

		while (t.hasNext()) {
			Pattern p = t.next();
			Matcher m = p.matcher(n);
			if (m.matches()) {
				return true;
			}
		}
		
		return include;
	}

	
	
}
