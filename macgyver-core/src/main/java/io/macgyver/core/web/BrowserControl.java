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
package io.macgyver.core.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Utility class to control Browser CSS from java.
 * 
 * @author rschoening
 *
 */
public class BrowserControl {

	public static final String OPLIST_KEY = "JavaScriptControl.oplist";

	public static class Instruction {
		public String operation;
		public String element;
		public String className;
		public String javascript;
	}

	public static void addJavaScript(String js) {
		Instruction c = new Instruction();
		c.operation = "javascript";
		c.javascript = js;

		getInstructionList().add(c);

	}

	public static void addClass(String name, String className) {

		Instruction c = new Instruction();
		c.operation = "addClass";
		c.element = name;
		c.className = className;

		getInstructionList().add(c);

	}

	public static void toggleClass(String element, String className) {
		Instruction c = new Instruction();
		c.operation = "toggleClass";
		c.element = element;
		c.className = className;

		getInstructionList().add(c);
	}

	public static void removeClass(String element, String className) {
		Instruction c = new Instruction();
		c.operation = "removeClass";
		c.element = element;
		c.className = className;

		getInstructionList().add(c);
	}

	public static List<Instruction> getInstructionList() {

		MacGyverWebContext ctx = MacGyverWebContext.get();
		Preconditions.checkState(ctx != null,
				"MacGyverWebContext not bound to current thread");
		HttpServletRequest request = ctx.getServletRequest();
		Preconditions
				.checkState(request != null,
						"HttpServletRequest not bound to current thread via MacGyverWebContext");

		List<Instruction> list = (List<Instruction>) request
				.getAttribute(OPLIST_KEY);
		if (list == null) {
			list = Lists.newArrayList();
			request.setAttribute(OPLIST_KEY, list);
		}

		return list;
	}

}
