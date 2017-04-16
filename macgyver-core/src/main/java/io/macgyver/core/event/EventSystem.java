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
package io.macgyver.core.event;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.lendingclub.reflex.concurrent.ConcurrentSubscribers;
import org.lendingclub.reflex.concurrent.ConcurrentSubscribers.ConcurrentSubscriber;
import org.lendingclub.reflex.eventbus.ReflexBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

import io.reactivex.Observable;

public class EventSystem {

	Logger logger = LoggerFactory.getLogger(EventSystem.class);

	ReflexBus reflexBus;

	@Value("${MACGYVER_EVENT_SYSTEM_THREAD_COUNT:50}")
	int threadCount = 50;

	@Value("${MACGYVER_EVENT_SYSTEM_BACKLOG:2048}")
	int backlog = 2048;

	@SuppressWarnings("unchecked")
	public <T> Observable<T> createObservable(Class<? extends T> clazz) {
		return (Observable<T>) getReflexBus().createObservable(clazz);	
	}

	public Observable<Object> createObservable() {
		return createObservable(Object.class);
	}

	public <T> ConcurrentSubscriber<T> createConcurrentSubscriber(Class<T> clazz) {
		ConcurrentSubscriber<T> concurrentSubscriber = ConcurrentSubscribers
				.createConcurrentSubscriber(createObservable(clazz));
		return concurrentSubscriber;
	}

	public ReflexBus getReflexBus() {
		return reflexBus;
	}
	
	public EventBus getEventBus() {
		return getReflexBus().getGuavaEventBus();
	}

	public Executor getExecutor() {
		return getReflexBus().getExecutor();	
	}

	public void post(Object event) {
		Preconditions.checkNotNull(event, "event cannot be null");
		getEventBus().post(event);
	}

	class MyRejectedExecutionHandler extends ThreadPoolExecutor.DiscardPolicy {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			super.rejectedExecution(r, e);
			logger.error("rejected execution of {} in {}", r, e);
		}

	}

	@PostConstruct
	public synchronized void init() {

		if (reflexBus!=null) {
			throw new IllegalStateException("init() can only be called once");
		}
		reflexBus = ReflexBus.newBuilder().withThreadPoolExecutorConfig(cfg -> {
			cfg.withThreadNameFormat("EventSystem-%d")
					.withThreadPoolSize(threadCount)
					.withThreadTimeout(true)
					.withRejectedExecutionHandler(new MyRejectedExecutionHandler());
		}).build();
		
	}

	public void shutdown() {
		if (reflexBus!=null) {
			Executor executor = reflexBus.getExecutor();
			if (executor!=null && executor instanceof ThreadPoolExecutor) {
				ThreadPoolExecutor.class.cast(executor).shutdown();
			}
		}
	}
}
