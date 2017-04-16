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
package io.macgyver.ssh;

import java.io.File;

import com.google.common.base.Optional;

public class PubKeyCredentials implements Credentials {

	String username;
	Optional<File> privateKey = Optional.absent();

	public PubKeyCredentials(String username) {
		this.username = username;
		this.privateKey = locateKeyFile(username);
	}

	public PubKeyCredentials(String username, File pkf) {
		this.username = username;
		this.privateKey = Optional.fromNullable(pkf);
	}

	@Override
	public String getUsername() {
		return username;
	}

	public Optional<File> getPrivateKeyFile() {
		return privateKey;
	}

	private Optional<File> locateKeyFile(String username) {
		String home = System.getProperty("user.home",".");
		File f = new File(home, ".ssh/id_rsa");
		if (f.exists()) {
			return Optional.of(f);
		}

		f = new File(home, ".ssh/id_dsa");
		if (f.exists()) {
			return Optional.of(f);
		}

		return Optional.absent();
	}
}
