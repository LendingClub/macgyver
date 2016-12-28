package io.macgyver.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	ThreadPoolExecutor executor;
	
	@Value("${MACGYVER_EVENT_SYSTEM_CORE_THREAD_COUNT:30}")
	int coreThreadCount = 30;
	
	@Value("${MACGYVER_EVENT_SYSTEM_MAX_THREAD_COUNT:50}")
	int maxThreadCount = 50;

	
	@Value("${MACGYVER_EVENT_SYSTEM_BACKLOG:2048}")
	int backlog = 2048;
	
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
	
	public void post(Object event) {
		Preconditions.checkNotNull(event, "event cannot be null");
		getEventBus().post(event);
	}
	
	class MyRejectedExecutionHandler extends ThreadPoolExecutor.DiscardPolicy {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			super.rejectedExecution(r, e);
			logger.error("rejected execution of {} in {}",r,e);
		}
		
	}
	@SuppressWarnings("unchecked")
	@PostConstruct
	public synchronized void init() {
		if (eventBusAdapter == null) {
			logger.info("initializing {} with {} threads",getClass().getName(),coreThreadCount);
			ThreadFactory threadFactory = new ThreadFactoryBuilder()
					.setDaemon(true).setNameFormat("EventSystem-%d").build();
			LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(backlog);
			
			maxThreadCount = Math.max(coreThreadCount,maxThreadCount);
			
			executor = new ThreadPoolExecutor(coreThreadCount,maxThreadCount,30, TimeUnit.SECONDS,queue,threadFactory,new MyRejectedExecutionHandler());
	
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
