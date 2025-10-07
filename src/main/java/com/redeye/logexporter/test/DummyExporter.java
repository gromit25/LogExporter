package com.redeye.logexporter.test;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Process;

@Activity(from="dummyHandler")
public class DummyExporter {

	@Process
	public void export(Message<?> message) throws Exception {
		System.out.println("Exporter : " + message.getBody());
	}
}
