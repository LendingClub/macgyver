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
package io.macgyver.core.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.lendingclub.reflex.eventbus.EventBusAdapter;
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
	

	ThreadPoolExecutor executor;
	
	@Value("${MACGYVER_EVENT_SYSTEM_CORE_THREAD_COUNT:30}")
	int coreThreadCount = 30;
	
	@Value("${MACGYVER_EVENT_SYSTEM_MAX_THREAD_COUNT:50}")
	int maxThreadCount = 50;

	
	@Value("${MACGYVER_EVENT_SYSTEM_BACKLOG:2048}")
	int backlog = 2048;
	
	EventBus eventBus;
	
	public <T> Observable<T> newObservable(Class<? extends T> clazz) {
		
		return EventBusAdapter.toObservable(eventBus, clazz);
	}
	public Observable<Object> newObservable() {
		return EventBusAdapter.toObservable(eventBus);
	}
	
	@Deprecated
	public Observable<Object> getObservable() {	
		return newObservable();
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
		if (executor == null) {
			logger.info("initializing {} with {} threads",getClass().getName(),coreThreadCount);
			ThreadFactory threadFactory = new ThreadFactoryBuilder()
					.setDaemon(true).setNameFormat("EventSystem-%d").build();
			LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(backlog);
			
			maxThreadCount = Math.max(coreThreadCount,maxThreadCount);
			
			executor = new ThreadPoolExecutor(coreThreadCount,maxThreadCount,30, TimeUnit.SECONDS,queue,threadFactory,new MyRejectedExecutionHandler());
	
			eventBus = new AsyncEventBus("MacGyverEventBus",executor);
	
		
		}
		else {
			throw new IllegalStateException("init() can only be called once");
		}
	}

	public void shutdown() {
		executor.shutdown();
	}
}
