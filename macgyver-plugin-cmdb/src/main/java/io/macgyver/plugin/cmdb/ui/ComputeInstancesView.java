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
package io.macgyver.plugin.cmdb.ui;

import io.macgyver.core.Kernel;
import io.macgyver.core.web.vaadin.IndexedJsonContainer;
import io.macgyver.core.web.vaadin.VaadinUtil;
import io.macgyver.core.web.vaadin.ViewConfig;
import io.macgyver.core.web.vaadin.ViewDecorators;
import io.macgyver.neorx.rest.NeoRxClient;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@ViewConfig(viewName = ComputeInstancesView.VIEW_NAME, menuPath = {
		"Inventory", "Compute Instances" })
public class ComputeInstancesView extends VerticalLayout implements View {

	public static final String VIEW_NAME = "cmdb/computeInstances";
	Table table;

	TextField searchTextField;

	int refreshCount = 0;

	public ComputeInstancesView() {
		setMargin(true);
		// setSizeFull();
		// setWidth(800, Unit.PIXELS);
		table = new Table("Compute Instances", new IndexedJsonContainer());

		table.addStyleName("compact");
		table.addStyleName("small");
		// Define two columns for the built-in container
		table.addContainerProperty("name", String.class, null);
		table.addContainerProperty("lastUpdatePrettyTs",String.class,null);

		table.setWidth("100%");
		table.setHeight("100%");

		table.setColumnCollapsingAllowed(true);

		table.setPageLength(table.size());


		searchTextField = new TextField();
		searchTextField.setInputPrompt("Search...");

		CssLayout group = new CssLayout();
		group.addStyleName("v-component-group");
		addComponent(group);

		Button search = new Button("Search");
		search.setClickShortcut(KeyCode.ENTER);
		group.addComponent(searchTextField);
		group.addComponent(search);
		addComponent(table);

		table.setColumnHeader("name", "Name");
		table.setColumnHeader("lastUpdatePrettyTs", "Last Update");

		BlurListener bl = new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				refresh();

			}
		};
		searchTextField.addBlurListener(bl);
		ClickListener cl = new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				refresh();

			}
		};
		search.addClickListener(cl);

		ViewDecorators.decorate(this);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		refresh();
	}

	/**
	 * Check if any field values contain the given search string.
	 * 
	 * @param search
	 * @param row
	 * @return
	 */
	boolean filter(String search, JsonNode row) {
		if (Strings.isNullOrEmpty(search) || row == null) {
			return false;
		}
		Iterator<String> t = row.fieldNames();
	
		
		while (t.hasNext()) {
			if (row.path(t.next()).asText().toLowerCase().contains(search.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void refresh() {

		NeoRxClient neo4j = Kernel.getInstance().getApplicationContext()
				.getBean(NeoRxClient.class);

		table.removeAllItems();
		ObjectMapper mapper = new ObjectMapper();
		String cypher = "match (c:ComputeInstance)   return c";
		PrettyTime pt = new PrettyTime();
		List<JsonNode> results = neo4j.execCypher(cypher).toList().toBlocking()
				.first();
	
		ApplicationContext ctx = Kernel.getInstance().getApplicationContext();

		IndexedJsonContainer container = (IndexedJsonContainer) table
				.getContainerDataSource();
		Collection<?> containerProps = container.getContainerPropertyIds();

		String searchValue = searchTextField.getValue();

		for (JsonNode node : results) {
			ObjectNode objectNode = (ObjectNode) node;
			if (filter(searchValue, node)) {

				long lastContact = node.path("lastUpdateTs").asLong(0);
				if (lastContact > 0) {
				
					String pretty = pt.format(new Date(lastContact));

					objectNode.put("lastUpdatePrettyTs", pretty);
				}

				container.addJsonObject(objectNode);

			}

		}

		if (VaadinUtil.isEmpty(container) && refreshCount > 0) {

			Notification notification = new Notification("No Results Found");
			notification.setPosition(Position.MIDDLE_CENTER);
			notification.setDelayMsec(1000);

			notification.show(Page.getCurrent());
		}
		refreshCount++;

		table.sort(new Object[] { "name" }, new boolean[] { true });
	}
}