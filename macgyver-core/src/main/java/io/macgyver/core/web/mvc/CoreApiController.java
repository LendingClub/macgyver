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
package io.macgyver.core.web.mvc;

import io.macgyver.core.CoreSystemInfo;
import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.core.util.JsonNodes;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RequestMapping("/api/core")
@Controller("macCoreApiController")
public class CoreApiController {

	Logger logger = LoggerFactory.getLogger(CoreApiController.class);
	
	@Autowired
	CoreSystemInfo coreRevisionInfo;

	
	@Autowired
	ExtensionResourceProvider extensionProvider;
	
	ObjectMapper mapper = new ObjectMapper();
	
	@RequestMapping(value = "/systemInfo", method=RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@PreAuthorize("permitAll")
	public String coreRevisionInfo() {

		return JsonNodes.pretty(mapper.valueToTree(coreRevisionInfo.getData()));

	}
	
	
	@RequestMapping(value="refreshResourceProviderHook",produces="application/json")
	@ResponseBody
	@PreAuthorize("permitAll")
	public String refreshResourceProviderHook(HttpServletRequest rquest) throws IOException {
		extensionProvider.refresh();
		return "{}";
	}
	
	
	@RequestMapping(value = "/error")
	@ResponseBody
	public String errorHandler() {

		return "problem";

	}
}
