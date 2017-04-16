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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class BrowserControlTest {

	@Test()
	public void testIllegalStateException() {

		try {
			BrowserControl.addClass("test-element", "test-class");
		} catch (IllegalStateException e) {
			Assertions.assertThat(e).isInstanceOf(IllegalStateException.class);
		}
	}

	@Test()
	public void testInstructions() {

		try {

			ApplicationContext ctx = Mockito
					.mock(org.springframework.context.ApplicationContext.class);
			ServletContext sc = Mockito.mock(ServletContext.class);
			HttpServletRequest request = new MockHttpServletRequest();
			HttpServletResponse response = new MockHttpServletResponse();

			MacGyverWebContext.contextLocal.set(new MacGyverWebContext(ctx, sc,
					request, response));

			Assertions.assertThat(BrowserControl.getInstructionList())
					.isNotNull().isSameAs(BrowserControl.getInstructionList()).hasSize(0);

			
			BrowserControl.addClass("test-element", "test-class");			
			Assertions.assertThat(BrowserControl.getInstructionList().get(0).operation).isEqualTo("addClass");
			Assertions.assertThat(BrowserControl.getInstructionList().get(0).element).isEqualTo("test-element");
			Assertions.assertThat(BrowserControl.getInstructionList().get(0).className).isEqualTo("test-class");
			
			BrowserControl.removeClass("test-element", "test-class");			
			Assertions.assertThat(BrowserControl.getInstructionList().get(1).operation).isEqualTo("removeClass");
			Assertions.assertThat(BrowserControl.getInstructionList().get(1).element).isEqualTo("test-element");
			Assertions.assertThat(BrowserControl.getInstructionList().get(1).className).isEqualTo("test-class");
			
			BrowserControl.toggleClass("test-element", "test-class");			
			Assertions.assertThat(BrowserControl.getInstructionList().get(2).operation).isEqualTo("toggleClass");
			Assertions.assertThat(BrowserControl.getInstructionList().get(2).element).isEqualTo("test-element");
			Assertions.assertThat(BrowserControl.getInstructionList().get(2).className).isEqualTo("test-class");
			
			
			BrowserControl.addJavaScript("document.foo();");			
			Assertions.assertThat(BrowserControl.getInstructionList().get(3).operation).isEqualTo("javascript");
			Assertions.assertThat(BrowserControl.getInstructionList().get(3).javascript).isEqualTo("document.foo();");
		
		} finally {
			MacGyverWebContext.contextLocal.set(null);
		}
	}
}
