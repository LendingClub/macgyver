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
package io.macgyver.plugin.splunk;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.splunk.Event;
import com.splunk.Job;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;

import io.macgyver.core.MacGyverException;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RxSplunk {

	static Logger logger = LoggerFactory.getLogger(RxSplunk.class);

	Service service;

	static Scheduler scheduler = Schedulers.io();

	RxSplunk() {

	}

	public static RxSplunk with(Service splunk) {
		RxSplunk rx = new RxSplunk();
		rx.service = splunk;
		return rx;
	}

	public static class JobFuture implements Future<Job> {

		volatile boolean cancelled = false;
		Job job;

		long expiration = 0;// System.currentTimeMillis()+60000;

		public JobFuture(Job job, long duration, TimeUnit unit) {
			this.job = job;
			expiration = System.currentTimeMillis() + unit.toMillis(duration);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			cancelled = true;
			job.cancel();
			return true;
		}

		@Override
		public boolean isCancelled() {

			if (cancelled) {
				return true;
			}
			logger.info("cancel refreshing job state: {}", job);
			job.refresh();
			boolean b = job.isFailed() || job.isZombie();
			if (b) {
				cancelled = b;
			}
			return true;
		}

		@Override
		public boolean isDone() {

			// https://answers.splunk.com/answers/294181/splunk-java-sdk-why-does-jobisdone-hang-forever-bu.html
			if (System.currentTimeMillis() > expiration) {
				logger.info("job timed out: {}", job);
				cancelled = true;
				Job tmp = job;
				job = null;
				try {
					tmp.cancel();
				} catch (Exception e) {
					logger.warn("failed to cancel job: {}", tmp);
				}
				return true;
			}

			return cancelled || job.isDone();

		}

		@Override
		public Job get() throws InterruptedException, ExecutionException {
			try {
				return get(1, TimeUnit.MINUTES);
			} catch (TimeoutException e) {
				throw new ExecutionException(e);
			}
		}

		@Override
		public Job get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			long expiration = System.currentTimeMillis() + unit.toMillis(timeout);
			while (System.currentTimeMillis() < expiration) {
				if (isDone()) {
					return job;
				}
				try {
					Thread.sleep(1000L);
				} catch (Exception e) {
				}
			}
			throw new TimeoutException();
		}

	}

	public static Func1<Event, Observable<Long>> extractScalar(String name) {
		Func1<Event, Observable<Long>> f = new Func1<Event, Observable<Long>>() {

			@Override
			public Observable<Long> call(Event t) {

				return Observable.just(new Long(Long.parseLong(t.get(name))));
			}
		};
		return f;
	}

	public static Observable<Event> convertJobToObservableEvent(Job job) {

		// This is complicated in terms of exception handling.  TWe need to
		// leave the stream open after this method returns because the results
		// are streamed via the Observable Event sequence.  The ResultsReaderXml 
		// will close the underlying stream after it has been
		// consumed.  If the monad blows up due to a problem in consuming code
		// 
		InputStream is = null;
		try {
			is = job.getResults();
			
			ResultsReaderXml rr = new ResultsReaderXml(is);
			Observable<Event> eventObservable = Observable.from(rr);
			eventObservable.doOnError(t -> {
				try {
					rr.close();
				}
				catch (IOException e) {
					logger.warn("problem closing ResultsReader");
				}
			});
			return eventObservable;
		} catch (IOException e) {
			if (is!=null) {
				try {
					is.close();
				}
				catch (IOException swallow) {
					logger.warn("problem closing stream",e);
				}
			}
			throw new MacGyverException(e);
		}

	}

	public Observable<Event> searchEvents(String search, int timeoutSecs) {
		return search(search, timeoutSecs).flatMap(RxSplunk::convertJobToObservableEvent);
	}

	public Observable<Job> search(String search, int timeoutSecs) {

		Job job = service.search(search);

		JobFuture jf = new JobFuture(job, timeoutSecs, TimeUnit.SECONDS);
		return Observable.from(jf, scheduler);

	}

}
