package com.redeye.logexporter.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.ComponentConfig;
import com.redeye.logexporter.workflow.comp.Handler;

@Component("dummyHandler")
@ComponentConfig(from="dummyCollector")
public class DummyHandler implements Handler {

	@Override
	public List<Message<?>> handle(Message<?> message) throws Exception {
		
		System.out.println("Handler : " + message.getBody());
		
		List<Message<?>> data = new ArrayList<>();
		data.add(message);
		
		return data;
	}
}
