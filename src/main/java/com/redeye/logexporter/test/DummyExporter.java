package com.redeye.logexporter.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

@Activity(from="dummyHandler")
@ConditionalOnProperty(
	name="dummy.test",
	havingValue="y"
)
public class DummyExporter {

	@Proc
	public void export(Message<?> message) throws Exception {
		System.out.println("Exporter : " + message.getBody());
	}
}
