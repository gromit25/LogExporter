package com.redeye.logexporter.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Process;

@Activity(value="dummyHandler", from="dummyCollector")
@ConditionalOnProperty(
	name="log.type",
	havingValue="test"
)
public class DummyHandler {

	@Process
	public List<Message<?>> handle(Message<?> message) throws Exception {
		
		System.out.println("Handler : " + message.getBody());
		
		List<Message<?>> data = new ArrayList<>();
		data.add(message);
		
		return data;
	}
}
