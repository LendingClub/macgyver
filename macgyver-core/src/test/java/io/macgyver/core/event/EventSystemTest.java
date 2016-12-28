package io.macgyver.core.event;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class EventSystemTest {

	Logger logger = LoggerFactory.getLogger(EventSystemTest.class);
	
	EventSystem eventSystem;
	AtomicInteger counter = new AtomicInteger();
	@Test
	public void testIt() throws InterruptedException {
		 
		eventSystem.getEventBus().register(this);

		CountDownLatch runnableLatch = new CountDownLatch(10);
		CountDownLatch cdl = new CountDownLatch(100);
		
		eventSystem.getObservable().subscribe( c->{
			logger.info("Thread: "+Thread.currentThread()+" "+c);
			cdl.countDown();
		});
		Runnable r = new Runnable() {

			@Override
			public void run() {
				
				runnableLatch.countDown();

			}
			
		};
		for (int i=0; i<100; i++) {
			eventSystem.getExecutorService().submit(r);
		}
		for (int i=0; i<100; i++) {
			eventSystem.getEventBus().post("test");
		}
		
		Assertions.assertThat(cdl.await(5, TimeUnit.SECONDS)).isTrue();
		Assertions.assertThat(runnableLatch.await(5, TimeUnit.SECONDS)).isTrue();
		
	
		Assertions.assertThat(eventSystem.getEventBus()).isNotNull();
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receive(String message) {
		logger.info("receive message: "+message+" on "+Thread.currentThread());
		try {
			Thread.sleep(250);	
		}
		catch (Exception e) {}
		}
	
	
	@Test
	public void testConcurrency() throws Exception {
		int count = 10;
		CountDownLatch latch = new CountDownLatch(count*2);
		eventSystem.getObservable().subscribe(c->{
			logger.info("subscriber1 - {}",c);
			latch.countDown();
		});
		eventSystem.getObservable().subscribe(c->{
			logger.info("subscriber2 - {}", c);
			Thread.sleep(1000);
			latch.countDown();
		});
		
		eventSystem.getEventBus().register(this);
		
		long t0 = System.currentTimeMillis();
		for (int i=0; i<10; i++) {
			eventSystem.getEventBus().post(""+i);
		}
		long t1 = System.currentTimeMillis();
		
		Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
		long t2 = System.currentTimeMillis();
		
		Assertions.assertThat(t2-t0).isLessThan(3000);

	}
	
	@Test(expected=NullPointerException.class)
	public void testNull() {
		eventSystem.post(null);
	}
	
	@Before
	public void setup() {
		eventSystem = new EventSystem();
		eventSystem.init();
		counter = new AtomicInteger();
	}
	@After
	public void shutdown() {
		if (eventSystem!=null) {
			eventSystem.shutdown();
		}
	}
	


	public void testExecutorBehavior() throws InterruptedException{
	
		
		AtomicInteger receiveCount = new AtomicInteger(0);
		Object obj = new Object() {
			
			
			@Subscribe
			@AllowConcurrentEvents
			public void receive(String message) {
				System.out.println("receive "+message+" on "+Thread.currentThread());
			receiveCount.incrementAndGet();
				try {
					Thread.sleep(50);
				}
				catch (Exception e) {
					
				}
			}
		};
		int count = 200;
		
		AtomicInteger x = new AtomicInteger(0);
		RejectedExecutionHandler h = new ThreadPoolExecutor.DiscardPolicy() {
			
			int id = x.incrementAndGet();
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				System.out.println("rejected "+r+" for "+executor);
				
			}
		
		};
		LinkedBlockingDeque<Runnable> queue = new LinkedBlockingDeque<>(1024);
		ThreadPoolExecutor s = new ThreadPoolExecutor(3, 3, 10, TimeUnit.SECONDS, queue);
		
		
		s.setRejectedExecutionHandler(h);
		EventBus bus = new AsyncEventBus(s);
		

		bus.register(obj);
		
		
		
		for (int i=0; i<count; i++) {
			bus.post("message-"+i);
			System.out.println(s);
		}
		
		Thread.sleep(25000);
		
		System.out.println("messages received: "+receiveCount.get());
		
	}
}
