package io.macgyver.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;

import org.lendingclub.reflex.guava.EventBusAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.reactivex.Observable;

public class EventSystem {


	Logger logger = LoggerFactory.getLogger(EventSystem.class);
	
	Observable<Object> observable;
	ExecutorService executor;
	
	@Value("${MACGYVER_EVENT_SYSTEM_THREAD_COUNT:30}")
	int threadCount = 30;
	
	EventBusAdapter<Object> eventBusAdapter;
	EventBus eventBus;
	
	public Observable<Object> getObservable() {
		return observable;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public ExecutorService getExecutorService() {
		return executor;
	}
	
	public void publish(Object event) {
		Preconditions.checkArgument(event!=null, "event cannot be null");
		getEventBus().post(event);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public synchronized void init() {
		if (eventBusAdapter == null) {
			logger.info("initializing {} with {} threads",getClass().getName(),threadCount);
			ThreadFactory threadFactory = new ThreadFactoryBuilder()
					.setDaemon(true).setNameFormat("EventSystem-%d").build();
			executor = Executors.newFixedThreadPool(threadCount, threadFactory);
			eventBus = new AsyncEventBus("MacGyverEventBus",executor);
	
			eventBusAdapter = (EventBusAdapter<Object>) EventBusAdapter.createAdapter(eventBus);
			observable = eventBusAdapter.getObservable();
		}
		else {
			throw new IllegalStateException("init() can only be called once");
		}
	}

	public void shutdown() {
		executor.shutdown();
	}
}
