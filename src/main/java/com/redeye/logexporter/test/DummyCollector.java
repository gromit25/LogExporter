package com.redeye.logexporter.test;

import java.util.ArrayList;
import java.util.List;

import com.jutools.DateUtil;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Cron;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

@Activity(value="dummyCollector")
public class DummyCollector {
	
	@Init
	public void init() throws Exception {
		System.out.println("### Initialize DummyCollector. ###");
	}

	@Process
	public List<Message<?>> collect() throws Exception {
		
		Thread.sleep(1 * 1000);
		
		List<Message<?>> data = new ArrayList<>();
		
		Message<String> dummy = new Message<String>();
		dummy.setSubject("dummy subject");
		dummy.setBody("dummy body : " + DateUtil.getDateTimeStr(System.currentTimeMillis()));
		
		data.add(dummy);
		
		System.out.println("COLLECT: " + dummy);
		
		return data;
	}
	
	@Cron(period = "*/5 * * * * *")
	public void cron() throws Exception {
		System.out.println(" #### CRON JOB #### ");
	}
}
