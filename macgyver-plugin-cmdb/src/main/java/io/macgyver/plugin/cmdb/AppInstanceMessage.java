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
package io.macgyver.plugin.cmdb;



public class AppInstanceMessage extends io.macgyver.core.event.MacGyverMessage {

	public static class Discovery extends AppInstanceMessage {

	}


	public static class DeploymentInitiated extends AppInstanceMessage {
		
	}
	public static class DeploymentFailure extends AppInstanceMessage {
		
	}
	public static class DeploymentComplete extends AppInstanceMessage {

	}
	
	public static class ShutdownInitiated extends AppInstanceMessage {
		
	}
	
	public static class ShutdownComplete extends AppInstanceMessage {
		
	}
	
	public static class StartupInitiated extends AppInstanceMessage {
		
	}
	
	public static class StartupFailure extends AppInstanceMessage {

	}
	
	public static class StartupComplete extends AppInstanceMessage {

	}
	
	public static class RevisionChange extends AppInstanceMessage {

	}
	
	public static class VersionChange extends AppInstanceMessage {

	}
	
	public static class Monitor extends AppInstanceMessage {

	}
}
