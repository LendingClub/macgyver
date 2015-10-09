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
package io.macgyver.core.web.vaadin.views.admin;

import io.macgyver.core.Kernel;
import io.macgyver.core.MacGyverException;
import io.macgyver.core.crypto.Crypto;
import io.macgyver.core.web.vaadin.MacGyverView;
import io.macgyver.core.web.vaadin.ViewConfig;

import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;

@ViewConfig(viewName=PropertyEncryptionView.VIEW_NAME,menuPath={"Admin","Encrypt String"})
public class PropertyEncryptionView extends MacGyverView {

	Logger logger = LoggerFactory.getLogger(PropertyEncryptionView.class);

	public static final String VIEW_NAME = "admin/propertyEncryption";

	FormLayout form = new FormLayout();
	ComboBox keyComboBox;
	Label cipherTextOutput;
	TextField plainTextInput;

	public PropertyEncryptionView() {
		super();
		setSpacing(true);
		setMargin(true);

		form = new FormLayout();
		form.setMargin(false);

		form.setWidth("50%");

		Label section = new Label("Encrypt String");
		section.addStyleName("h2");
		section.addStyleName("colored");
		addComponent(section);

		plainTextInput = new TextField("Plain Text Input");
		plainTextInput.setWidth("100%");
		form.addComponent(plainTextInput);

		keyComboBox = new ComboBox("Key Name");
		keyComboBox.addItem("mac0");
		keyComboBox.setNewItemsAllowed(true);
		form.addComponent(keyComboBox);

		cipherTextOutput = new Label();
		cipherTextOutput.setCaption("Cipher Text Output");

		// cipherTextOutput.setReadOnly(true);
		cipherTextOutput.setWidth("100%");
		form.addComponent(cipherTextOutput);

		addComponent(form);

		Button encryptButton = new Button("Encrypt");

		encryptButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				encryptString();

			}
		});

		addComponent(encryptButton);
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

	public void encryptString() {

		try {

			Object keyVal = keyComboBox.getValue();
			if (keyVal == null) {
				throw new MacGyverException("no key specified");
			}
			Crypto crypto = Kernel.getApplicationContext().getBean(Crypto.class);

			String val = crypto.encryptString(plainTextInput.getValue(),
					keyComboBox.getValue().toString());

			cipherTextOutput.setValue(val);

		} catch (RuntimeException | GeneralSecurityException e) {
			String message = e.getMessage();
			if (Strings.isNullOrEmpty(message)) {
				message = e.getClass().getName();
			}
			Notification n = new Notification("");
			n.setCaption("Encrption Failure");
			
			n.setDescription(message);
			n.setStyleName("tray failure closable");
			n.setPosition(Position.MIDDLE_CENTER);
			n.setDelayMsec(-1);
			n.show(Page.getCurrent());
			logger.error("error encrypting", e);
		}

	}

}
