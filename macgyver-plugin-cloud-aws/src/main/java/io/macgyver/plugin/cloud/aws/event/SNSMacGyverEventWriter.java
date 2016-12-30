package io.macgyver.plugin.cloud.aws.event;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.lendingclub.reflex.consumer.Consumers;
import org.lendingclub.reflex.predicate.Predicates;
import org.lendingclub.reflex.queue.WorkQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import io.macgyver.core.event.EventSystem;
import io.macgyver.core.event.MacGyverMessage;

public class SNSMacGyverEventWriter implements ApplicationListener<ApplicationReadyEvent>{

	AtomicReference<AmazonSNSAsyncClient> snsClientRef = new AtomicReference<>();
	AtomicReference<String> topicArnRef = new AtomicReference<>();
	Logger logger = LoggerFactory.getLogger(SNSMacGyverEventWriter.class);

	AtomicBoolean enabled = new AtomicBoolean(true);

	WorkQueue<MacGyverMessage> workQueue;
	
	public void setSNSClient(AmazonSNSAsyncClient client) {
		withSNSClient(client);
	}
	public SNSMacGyverEventWriter withSNSClient(AmazonSNSAsyncClient client) {
		this.snsClientRef.set(client);
		return this;
	}

	public void setTopicArn(String arn) {
		withTopicArn(arn);
	}
	public SNSMacGyverEventWriter withTopicArn(String arn) {
		this.topicArnRef.set(arn);
		return this;
	}

	class ResponseHandler implements AsyncHandler<PublishRequest, PublishResult> {

		@Override
		public void onError(Exception exception) {
			logger.error("problem sending message to SNS", exception);

		}

		@Override
		public void onSuccess(PublishRequest request, PublishResult result) {
			// TODO Auto-generated method stub

		}

	}

	public boolean isEnabled() {
		return enabled.get() && getTopicArn().isPresent() && getSNSClient().isPresent();
	}
	
	public void setEnabled(boolean b) {
		enabled.set(b);
	}
	
	public Optional<AmazonSNSAsyncClient> getSNSClient() {
		return Optional.ofNullable(snsClientRef.get());
	}
	public Optional<String> getTopicArn() {
		return Optional.ofNullable(topicArnRef.get());
	}
	public void subscribe(EventSystem eventSystem) {
		workQueue = new WorkQueue<MacGyverMessage>().withCoreThreadPoolSize(2).withThreadName("SNSMacGyverEventWriter-%d");
		
		workQueue.getObservable().subscribe(Consumers.safeConsumer(event -> {
			try {
				if (isEnabled()) {
					PublishRequest request = new PublishRequest();
					request.setTopicArn(getTopicArn().get());
					request.setMessage(MacGyverMessage.class.cast(event).getEnvelope().toString());
					getSNSClient().get().publishAsync(request, new ResponseHandler());
				}
			} catch (Exception e) {
				logger.error("problem sending message to SNS: {}",e.toString());
			}
		}));
		
		eventSystem.getObservable().filter(Predicates.type(MacGyverMessage.class)).subscribe(Consumers.safeObserver(workQueue));
	}
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		
		subscribe(event.getApplicationContext().getBean(EventSystem.class));
		
	}
}
