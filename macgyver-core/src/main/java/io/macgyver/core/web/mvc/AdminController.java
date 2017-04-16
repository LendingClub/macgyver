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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.auth.AuthUtil;
import io.macgyver.core.auth.MacGyverRole;
import io.macgyver.core.cluster.ClusterManager;
import io.macgyver.core.cluster.ClusterManager.NodeInfo;
import io.macgyver.core.crypto.Crypto;
import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceProvider;
import io.macgyver.core.resource.provider.filesystem.FileSystemResourceProvider;
import io.macgyver.core.scheduler.DirectScriptExecutor;
import io.macgyver.core.scheduler.LocalScheduler;
import io.macgyver.core.script.ExtensionResourceProvider;
import io.macgyver.core.service.ServiceDefinition;
import io.macgyver.core.service.ServiceFactory;
import io.macgyver.core.service.ServiceRegistry;
import io.macgyver.core.web.BrowserControl;

@Component("macAdminController")
@Controller
@RequestMapping("/core/admin")
@PreAuthorize("hasAnyRole('ROLE_MACGYVER_ADMIN')")
public class AdminController {

	@Autowired
	Crypto crypto;

	@Autowired
	ServiceRegistry serviceRegistry;

	@Autowired
	LocalScheduler localScheduler;
	
	Logger logger = LoggerFactory.getLogger(AdminController.class);
	@Autowired
	ApplicationContext applicationContext;

	ObjectMapper mapper = new ObjectMapper();

	@RequestMapping("/spring-beans")
	@ResponseBody
	public ModelAndView springBeans() {

		return new ModelAndView("/admin/spring-beans");

	}

	@RequestMapping("/cluster-info")
	@ResponseBody
	public ModelAndView clusterInfo() {

		ClusterManager clusterManager = Kernel.getApplicationContext().getBean(
				ClusterManager.class);

		List<NodeInfo> list = new ArrayList<>();
		
		list.addAll(clusterManager.getClusterNodes().values());
		
	
		
		return new ModelAndView("/admin/cluster-info", "list", list);

	}

	@RequestMapping("/encrypt-string")
	@ResponseBody
	public ModelAndView encryptString(HttpServletRequest request) {

		String alias = request.getParameter("alias");
		String plaintext = request.getParameter("plaintext");
	
		Map<String, Object> data = com.google.common.collect.Maps.newHashMap();

		if (request.getMethod().equals("POST")) {

			if (Strings.isNullOrEmpty(plaintext)) {
				BrowserControl.addClass("plaintext-div", "has-error");	
			}
			else if (
					 !Strings.isNullOrEmpty(alias)
					&& !Strings.isNullOrEmpty(plaintext)) {

				String ciphertext = encrypt(plaintext.trim(), alias);
				data.put("ciphertext", ciphertext);
			}
		}
		try {

			List tmp = Collections.list(crypto.getKeyStoreManager()
					.getKeyStore().aliases());
			data.put("aliases", tmp);
			return new ModelAndView("/admin/encrypt-string", data);
		} catch (GeneralSecurityException e) {
			throw new MacGyverException();
		}

	}

	@RequestMapping("/services")
	@ResponseBody
	public ModelAndView services() {
		Map<String, ServiceDefinition> defMap = serviceRegistry
				.getServiceDefinitions();

		List<JsonNode> serviceList = Lists.newArrayList();
		for (ServiceDefinition def : defMap.values()) {
			ObjectNode n = mapper.createObjectNode();
			n.put("serviceName", def.getName());
			n.put("serviceType", def.getServiceFactory().getServiceType());
			serviceList.add(n);
		}

		Map<String, Object> model = ImmutableMap.of("services", serviceList);
		return new ModelAndView("/admin/services", model);

	}

	boolean currentUserHasExecutePermissions() {
		return AuthUtil.currentUserHasRole(MacGyverRole.ROLE_MACGYVER_ADMIN);
	}

	@RequestMapping("/scripts")
	@ResponseBody
	public ModelAndView scripts(HttpServletRequest request) {
		List<JsonNode> list = Lists.newArrayList();
		try {

			String path = request.getParameter("path");
			if (!Strings.isNullOrEmpty(path)) {
				try {
					if (!currentUserHasExecutePermissions()) {
						throw new MacGyverException("unauthorized");
					}
					
					Optional<Resource> r = findResourceByPath(path);
					if (r.isPresent()) {
						scheduleImmediate(r.get());
					}

				} catch (IOException e) {
					throw new MacGyverException(e);
				}
			}

			ExtensionResourceProvider extensionProvider = Kernel.getInstance()
					.getApplicationContext()
					.getBean(ExtensionResourceProvider.class);
			extensionProvider.refresh();
			ObjectMapper mapper = new ObjectMapper();

			for (Resource r : extensionProvider.findResources()) {
				ResourceProvider rp = r.getResourceProvider();

				if (r.getPath().startsWith("scripts/")) {
					ObjectNode n = mapper.createObjectNode();
					n.put("resource", r.getPath());
					if (rp.getClass().equals(FileSystemResourceProvider.class)) {
						n.put("providerType", "filesystem");
					} else if (rp.getClass().getName().contains("Git")) {
						n.put("providerType", "git");
					}
		
					n.put("executeAllowed", currentUserHasExecutePermissions());
					list.add(n);

				}

			}

		} catch (IOException e) {
			throw new MacGyverException(e);
		}
		return new ModelAndView("/admin/scripts", "list", list);

	}

	protected Optional<Resource> findResourceByPath(String path) throws IOException {
		ExtensionResourceProvider extensionProvider = Kernel.getInstance()
				.getApplicationContext()
				.getBean(ExtensionResourceProvider.class);
		return extensionProvider.findResourceByPath(path);
	}
	public void scheduleImmediate(Resource r) {
		try {
			DirectScriptExecutor service = Kernel
					.getApplicationContext()
					.getBean(LocalScheduler.class);

			service.executeScriptImmediately(r.getPath());

		} catch (IOException e) {
			throw new MacGyverException(e);
		}
	}

	protected ExtensionResourceProvider getExtensionResourceProvider() {
		return Kernel.getInstance().getApplicationContext()
				.getBean(ExtensionResourceProvider.class);
	}



	public String encrypt(String plaintext, String alias) {

		try {

			Crypto crypto = Kernel.getApplicationContext()
					.getBean(Crypto.class);

			String val = crypto.encryptString(plaintext, alias);

			return val;

		} catch (RuntimeException | GeneralSecurityException e) {
			throw new RuntimeException();
		}
	}

	List<ServiceFactory> getServiceFactories() {
		Map<String, ServiceFactory> map = Kernel.getApplicationContext()
				.getBeansOfType(ServiceFactory.class);
		List<ServiceFactory> list = Lists.newArrayList(map.values());

		return list;
	}

}
