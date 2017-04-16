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
package io.macgyver.core.metrics;

import java.util.concurrent.ThreadPoolExecutor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

public class MetricsUtil {

	
	
	public static void monitorExecutor(MetricRegistry metricRegistry, ThreadPoolExecutor executor, String name) {
		metricRegistry.register(MetricRegistry.name(name, "taskCount"),new Gauge<Long>() {

			@Override
			public Long getValue() {
				return executor.getTaskCount();
			}
		});
		
		metricRegistry.register(MetricRegistry.name(name, "largestPoolSize"),new Gauge<Integer>() {

			@Override
			public Integer getValue() {
				return executor.getLargestPoolSize();
			}
		});
		metricRegistry.register(MetricRegistry.name(name, "poolSize"),new Gauge<Integer>() {

			@Override
			public Integer getValue() {
				return executor.getPoolSize();
			}
		});
		metricRegistry.register(MetricRegistry.name(name, "completedTaskCount"),new Gauge<Long>() {

			@Override
			public Long getValue() {
				return executor.getCompletedTaskCount();
			}
		});
	}
}
