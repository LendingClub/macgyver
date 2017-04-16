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
package io.macgyver.core.cli;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameters;

import io.macgyver.cli.CLIRemoteException;
import io.macgyver.cli.Command;
import io.macgyver.okrest3.OkRestResponse;
import okhttp3.Response;


@Parameters()
public class LogoutCommand  extends Command {

	Logger logger = LoggerFactory.getLogger(LogoutCommand.class);

	@Override
	public void execute() throws IOException {
		executeDeleteAll();
	}

	public void executeDeleteAll() throws IOException {
		OkRestResponse rr = getOkRestTarget().path("/api/core/token/delete-all").post(mapper.createObjectNode()).execute();

		Response response = rr.response();

		if (response.isSuccessful()) {
			
			statusOutput("successfully deleted all API tokens");
			getConfig().remove("token");
			getConfigManager().saveConfig();
		}
		else {
			throw new CLIRemoteException("could not delete token", response.code());
		}
	}
}
