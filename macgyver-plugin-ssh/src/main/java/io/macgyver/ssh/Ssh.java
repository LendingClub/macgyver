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
import java.io.IOException;
import java.security.PublicKey;

import org.apache.sshd.SshClient;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class Ssh {

	private String host;
	private String command;

	Credentials credentials;

	private HostKeyVerifier verifier = null;
	private int connectTimeout = -1;

	public static Ssh to(String host) {
		return hostname(host);
	}

	public static Ssh hostname(String host) {
		Ssh ssh = new Ssh();
		ssh.host = host;
		return ssh;
	}

	public Ssh command(String command) {
		this.command = command;
		return this;
	}

	public Iterable<String> exec() throws IOException {
		return exec(new LineResponseCallback());
	}

	public String execString() throws IOException {
		return exec(new StringResponseCallback());
	}

	public Ssh connectTimeout(int timeout) {
		this.connectTimeout = timeout;
		return this;
	}

	public Ssh withPubKeyAuth(String username) {
		return withAuth(new PubKeyCredentials(username));
	}
	
	public Ssh withPubKeyAuth(String username, String privateKeyFile) {
		return withPubKeyAuth(username,new File(privateKeyFile));
	}
	
	public Ssh withPubKeyAuth(String username, File privateKeyFile) {
		return withAuth(new PubKeyCredentials(username, privateKeyFile));
	}
	public Ssh withAuth(Credentials c) {
		this.credentials = c;
		return this;
	}

	public Ssh withPasswordAuth(String username, char[] password) {
		return withAuth(new UsernamePasswordCredentials(username, password));
	}

	public Ssh hostKeyVerifier(HostKeyVerifier v) {
		this.verifier = v;
		return this;
	}

	public Ssh disableHostKeyVerification() {
		verifier = new HostKeyVerifier() {

			@Override
			public boolean verify(String hostname, int port, PublicKey key) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		return this;
	}

	public <T> T exec(ResponseCallback<T> cb) throws IOException {

		final SSHClient ssh = new SSHClient();
		ssh.loadKnownHosts();

		if (verifier != null) {
			ssh.addHostKeyVerifier(verifier);
		}
		ssh.connect(host);
		if (connectTimeout > 0) {
			ssh.setConnectTimeout(connectTimeout);
		}
		try {
			applyCredentials(ssh);

			final Session session = ssh.startSession();
			try {
				final Command cmd = session.exec(command);
				return cb.handle(cmd);

			} finally {
				session.close();
			}
		} finally {
			ssh.disconnect();
		}

	}

	protected void applyCredentials(SSHClient ssh) throws IOException {
		Preconditions.checkNotNull(ssh);
		if (credentials instanceof UsernamePasswordCredentials) {
			UsernamePasswordCredentials upc = (UsernamePasswordCredentials) credentials;
			ssh.authPassword(upc.getUsername(), upc.getPassword());
		} else if (credentials instanceof PubKeyCredentials) {
			PubKeyCredentials pk = (PubKeyCredentials) credentials;
			if (pk.getPrivateKeyFile().isPresent()) {
				ssh.authPublickey(pk.getUsername(), pk.getPrivateKeyFile()
						.get().getAbsolutePath());
			} else {
				ssh.authPublickey(pk.getUsername());
			}
		}
	}
}
