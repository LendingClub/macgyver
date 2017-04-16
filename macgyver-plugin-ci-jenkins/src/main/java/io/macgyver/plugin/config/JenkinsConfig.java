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
package io.macgyver.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.macgyver.plugin.ci.jenkins.JenkinsApiProxy;
import io.macgyver.plugin.ci.jenkins.JenkinsServiceFactory;
import io.macgyver.plugin.ci.jenkins.PostCompletedBuildResultWebhook;
import io.macgyver.plugin.ci.jenkins.StatisticsNotificationWebhook;

@Configuration
public class JenkinsConfig {

	
	@Bean
	JenkinsServiceFactory jenkinsServiceFactory() {
		return new JenkinsServiceFactory();
	}
	
	@Bean
	public PostCompletedBuildResultWebhook macJenkinsPostCompletedWebhook() {
		return new PostCompletedBuildResultWebhook();
	}
	
	@Bean
	public StatisticsNotificationWebhook macStatisticsNotificationWebhook() {
		return new StatisticsNotificationWebhook();
	}
	
	@Bean
	public JenkinsApiProxy macJenkinsApiProxy() {
		return new JenkinsApiProxy();
	}
}
