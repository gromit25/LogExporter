package com.redeye.logexporter.test;

import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.ComponentConfig;
import com.redeye.logexporter.workflow.comp.Exporter;

@Component
@ComponentConfig(from="dummyHandler")
public class DummyExporter implements Exporter {

	@Override
	public void export(Message<?> message) throws Exception {
		System.out.println("Exporter : " + message.getBody());
	}
}
