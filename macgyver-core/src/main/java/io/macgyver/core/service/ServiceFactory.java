package io.macgyver.core.service;

import io.macgyver.core.MacGyverException;
import io.macgyver.core.ServiceNotFoundException;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public abstract class ServiceFactory<T> implements ApplicationContextAware {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	String serviceType;

	Map<String, String> colloboratorNameMap = Maps.newConcurrentMap();

	@Autowired
	protected ServiceRegistry registry;

	@Autowired
	protected ApplicationContext applicationContext;


	protected ServiceFactory(String type) {
		Preconditions.checkNotNull(type);
		this.serviceType = type.toLowerCase();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		Preconditions.checkNotNull(applicationContext);
		this.applicationContext = applicationContext;

	}

	protected abstract T doCreateInstance(ServiceDefinition def);

	public void doConfigureDefinition(ServiceDefinition def) {}
	public synchronized Object get(String name) {
		Preconditions.checkNotNull(registry);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(registry.instances);
		Object instance = registry.instances.get(name);
		if (instance != null) {
			return instance;
		}

		String primaryName = name;
		ServiceDefinition def = registry.definitions.get(name);
		if (def == null) {
			throw new ServiceNotFoundException(name);
		}

		primaryName = def.getName();

		if (!primaryName.equals(name)) {
			Object primaryInstance = get(primaryName);
			instance = registry.instances.get(name);
			if (instance == null) {
				throw new MacGyverException("collaborator '" + name
						+ "'was not created properly");
			}
			return instance;
		} else {
			T newInstance = doCreateInstance(def);
			registry.instances.put(name, newInstance);

			createCollaboratorInstances(registry, def, newInstance);
			ServiceCreatedEvent event = new ServiceCreatedEvent(def,newInstance);
			registry.publish(event);
			return newInstance;

		}

	}

	protected final void createCollaboratorInstances(
			ServiceRegistry registry,
			ServiceDefinition primaryDefinition, T primaryBean) {
		Preconditions.checkNotNull(registry);
		Preconditions.checkNotNull(primaryDefinition);
		Preconditions.checkNotNull(primaryBean);
		
		doCreateCollaboratorInstances(registry, primaryDefinition, primaryBean);
	}

	protected abstract void doCreateCollaboratorInstances(
			ServiceRegistry registry,
			ServiceDefinition primaryDefinition, T primaryBean);

	protected final void createServiceDefintions(Set<ServiceDefinition> defSet,
			String name, Properties props) {
		Preconditions.checkNotNull(defSet);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(props);
		ServiceDefinition def = new ServiceDefinition(name, name, props, this);
		defSet.add(def);
		doCreateCollaboratorDefinitions(defSet, def);
	}

	public abstract void doCreateCollaboratorDefinitions(
			Set<ServiceDefinition> defSet, ServiceDefinition def);

	public String getServiceType() {
		return serviceType;
	}

	protected void assignProperties(Object target, Properties props,
			boolean failOnError) {

		BeanWrapperImpl bw = new BeanWrapperImpl(target);
		String key = null;
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			try {
				key = entry.getKey().toString();
				logger.debug("setting {}.{}", target, key);
				Object val = entry.getValue();
				bw.setPropertyValue(key, val);
			} catch (RuntimeException e) {
				if (failOnError) {
					throw e;
				} else {
					logger.warn("error setting {}.{}", target, key);
				}
			}
		}
	}

	public String toString() {
		return Objects.toStringHelper(this).add("serviceType", getServiceType()).toString();
	}
}
