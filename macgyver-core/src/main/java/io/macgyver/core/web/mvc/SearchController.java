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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.macgyver.core.web.UIContext;
import io.macgyver.core.web.UIContext.MenuItem;
import io.macgyver.core.web.UIContextDecorator;

@Component("macSearchController")
@Controller
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_USER','ROLE_MACGYVER_ADMIN')")
public class SearchController {

	@RequestMapping("/core/search")
	@ResponseBody
	public ModelAndView search(HttpServletRequest request) {

		String query = Strings.nullToEmpty(request.getParameter("q")).toLowerCase();

		UIContext uic = UIContext.forCurrentUser();

		List<MenuItem> results = Lists.newArrayList();
		
		search(query,uic.getRootMenu(),results);
		

		return new ModelAndView("/core/search", "results", results);

	}

	public void search(String q, MenuItem current, List<MenuItem> results) {
		current.getItems().iterator().forEachRemaining(it -> {

			search(q,it,results);
			

		});
		
	
		String label = Strings.nullToEmpty(current.getLabel()).toLowerCase();


		if (label.contains(q)) {
			results.add(current);
		
		}
	}

}
