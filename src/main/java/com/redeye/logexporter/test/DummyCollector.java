package com.redeye.logexporter.test;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.DateUtil;
import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Cron;
import com.jutools.workflow.annotation.Init;
import com.jutools.workflow.annotation.Proc;

@Activity(value="dummyCollector")
@ConditionalOnProperty(
	name="dummy.test",
	havingValue="y"
)
public class DummyCollector {
	
	@Init
	public void init() throws Exception {
		System.out.println("### Initialize DummyCollector. ###");
	}

	@Proc
	public Message<?> collect() throws Exception {
		
		Thread.sleep(1 * 1000);
		
		Message<String> dummy = new Message<String>();
		dummy.setTopic("dummy subject");
		dummy.setBody("dummy body : " + DateUtil.getDateTimeStr(System.currentTimeMillis()));
		
		System.out.println("COLLECT: " + dummy);
		
		return dummy;
	}
	
	@Cron(period = "${test.cron}")
	public void cron() throws Exception {
		System.out.println(" #### CRON JOB #### ");
	}
}
