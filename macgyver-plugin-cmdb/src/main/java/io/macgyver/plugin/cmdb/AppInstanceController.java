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

import java.util.List;

import org.lendingclub.neorx.NeoRxClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_ADMIN')")
public class AppInstanceController {

	@Autowired
	NeoRxClient neo4j;
	
	@RequestMapping(value = {"/cmdb/app-instances","/plugin/cmdb/app-instances"})
	@ResponseBody
	public ModelAndView lteHome() {

		List<JsonNode> results = neo4j.execCypherAsList("match (m:AppInstance) return m order by m.environment, m.host ");
		

		
		return new ModelAndView("/cmdb/app-instances","results",results);

	}
    


}
