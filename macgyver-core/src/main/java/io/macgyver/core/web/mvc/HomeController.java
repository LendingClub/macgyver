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

import org.lendingclub.neorx.NeoRxClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.macgyver.core.auth.AuthUtil;
import io.macgyver.core.auth.MacGyverRole;
import io.macgyver.core.web.UIContext;
import io.macgyver.core.web.UIContextDecorator;

@Component("macHomeController")
@Controller
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_ADMIN')")
public class HomeController implements UIContextDecorator {


	@Autowired
	NeoRxClient neo4j;

	Logger logger = LoggerFactory.getLogger(HomeController.class);

	public HomeController() {
		
	}

	@RequestMapping("/")
	@ResponseBody
	public ModelAndView home() {

		return new ModelAndView("redirect:/home");

	}

	@RequestMapping("/home")
	@ResponseBody
	public ModelAndView lteHome() {


		return new ModelAndView("/home");

	}

	@RequestMapping("/test/throwException")
	@ResponseBody
	public ModelAndView throwException() {
		throw new RuntimeException("test exception");
	}

	@Override
	public void call(UIContext ctx) {
		ctx.getOrCreateMenuItem("dashboard").label("Dashboard")
				.style("fa fa-dashboard").order(5);
		ctx.getOrCreateMenuItem("dashboard", "home").label("Home").url("/home");

		ctx.getOrCreateMenuItem("misc").label("Misc").style("fa fa-cubes");
		ctx.getOrCreateMenuItem("misc", "snapshare").label("Snap Share")
				.url("/core/snap-share");
		ctx.getOrCreateMenuItem("misc", "cli").label("CLI")
				.url("/cli");
		if (AuthUtil.currentUserHasRole(MacGyverRole.ROLE_MACGYVER_ADMIN)) {
			ctx.getOrCreateMenuItem("admin").label("Manage MacGyver")
					.style("fa fa-gear").order(100);
			ctx.getOrCreateMenuItem("admin", "scripts").label("Scripts")
					.url("/core/admin/scripts");
			ctx.getOrCreateMenuItem("admin", "cluster-info").label("Cluster")
					.url("/core/admin/cluster-info");
			ctx.getOrCreateMenuItem("admin", "encrypt-string")
					.label("Encrypt String").url("/core/admin/encrypt-string");
			ctx.getOrCreateMenuItem("admin", "services").label("Services")
					.url("/core/admin/services");
			ctx.getOrCreateMenuItem("admin", "spring").label("Spring")
					.url("/core/admin/spring-beans");
			ctx.getOrCreateMenuItem("admin", "neo4j-browser").label("Neo4j")
					.url("/browser");
		}
	}


}
