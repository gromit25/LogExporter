package com.redeye.logexporter.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jutools.DateUtil;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Collector;

@Component("dummyCollector")
public class DummyCollector implements Collector {

	@Override
	public List<Message<?>> collect() throws Exception {
		
		Thread.sleep(1 * 1000);
		
		List<Message<?>> data = new ArrayList<>();
		
		Message<String> dummy = new Message<String>();
		dummy.setSubject("dummy subject");
		dummy.setBody("dummy body : " + DateUtil.getDateTimeStr(System.currentTimeMillis()));
		
		data.add(dummy);
		
		return data;
	}
}
