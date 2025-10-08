package com.redeye.logexporter.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Process;

@Activity(from="dummyHandler")
@ConditionalOnProperty(
	name="log.type",
	havingValue="test"
)
public class DummyExporter {

	@Process
	public void export(Message<?> message) throws Exception {
		System.out.println("Exporter : " + message.getBody());
	}
}
