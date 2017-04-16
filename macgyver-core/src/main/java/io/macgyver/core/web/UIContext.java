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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.macgyver.core.Kernel;

public class UIContext {
	static ObjectMapper mapper = new ObjectMapper();

	ObjectNode descriptor;

	Comparator<MenuItem> comparator = new MenuItemComparator();

	
	public UIContext() {
		descriptor = mapper.createObjectNode();
		ObjectNode menu = mapper.createObjectNode();
		menu.put("id", "root");
		menu.set("items", mapper.createArrayNode());
		descriptor.set("menu", menu);
	}

	public static UIContext forCurrentUser() {
		return Kernel.getApplicationContext().getBean(UIContextManager.class).forCurrentUser();
	}
	public class MenuItem {

		public static final int DEFAULT_ORDER=10;
		ObjectNode itemNode;

		public MenuItem(ObjectNode n) {
			this.itemNode = n;
		}

		public ObjectNode getModel() {
			return itemNode;
		}

		public MenuItem label(String label) {
			itemNode.put("label", label);
			return this;
		}

		public MenuItem style(String className) {
			itemNode.put("style", className);
			return this;
		}
		
		public String getStyle() {
			return itemNode.path("style").asText();
		}
		
		public MenuItem order(int order) {
			itemNode.put("order", order);
			return this;
		}
		public int getOrder() {
			return itemNode.path("order").asInt(DEFAULT_ORDER);
		}
		public MenuItem url(String url) {
			itemNode.put("url", url);
			return this;
		}

		public String getUrl() {
			return itemNode.path("url").asText();
		}

		public String getId() {
			return itemNode.path("id").asText();
		}

		public String getLabel() {
			return itemNode.path("label").asText();
		}

		public boolean isSelected() {
			for (MenuItem item: getItems()) {
				if (item.getUrl().length()>0 && MacGyverWebContext.get().getServletRequest().getRequestURI().startsWith(item.getUrl()))  {
					return true;
				}
			}
			return false;
		}
		public List<MenuItem> getItems() {

			List<MenuItem> tmp = Lists.newArrayList();
			for (JsonNode n : Lists.newArrayList(itemNode.path("items").iterator())) {
				MenuItem m = new MenuItem((ObjectNode) n);
				tmp.add(m);
			}
			return tmp;
		}

		public void sort() {
			sort(this);
		}

		public void sort(MenuItem m) {
			List<MenuItem> tmp = getItems();
			Collections.sort(tmp, comparator);
			ArrayNode n = (ArrayNode) m.getModel().get("items");
			n.removeAll();
			for (MenuItem t : tmp) {
				n.add(t.getModel());
			}
			tmp.stream().forEach(it -> {
				it.sort();
			});

		}
	}

	public ObjectNode getModel() {
		return descriptor;
	}

	public MenuItem getRootMenu() {
		return new MenuItem((ObjectNode) descriptor.path("menu"));
	}

	public void sort() {
		getRootMenu().sort();
	}

	public MenuItem getOrCreateMenuItem(String... path) {
		JsonNode ptr = descriptor.path("menu");
		for (int i = 0; i < path.length; i++) {
			JsonNode match = null;
			for (JsonNode n : Lists.newArrayList(ptr.path("items").iterator())) {
				if (n.path("id").asText().equals(path[i])) {
					match = n;
				}
			}
			if (match != null) {
				ptr = match;
			} else {
				ObjectNode nn = mapper.createObjectNode();
				nn.put("id", path[i]);
				nn.set("items", mapper.createArrayNode());

				((ArrayNode) ptr.path("items")).add(nn);
				ptr = nn;
			}
		}
		return new MenuItem((ObjectNode) ptr);
	}

}
