package com.redeye.logexporter.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

@Activity(value="dummyHandler", from="dummyCollector")
@ConditionalOnProperty(
	name="dummy.test",
	havingValue="y"
)
public class DummyHandler {

	@Proc
	public List<Message<?>> handle(Message<?> message) throws Exception {
		
		System.out.println("Handler : " + message.getBody());
		
		List<Message<?>> data = new ArrayList<>();
		data.add(message);
		
		return data;
	}
}
