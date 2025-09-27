package com.redeye.logexporter.workflow;

import org.springframework.beans.factory.annotation.Value;

import com.jutools.CronJob;

/**
 * 크론 핸들러 실행 클래스
 * 
 * @authro jmsohn
 */
public class CronHandlerRunner extends AbstractRunner {
	
	/** */
	private CronJob job;
	
	
	/**
	 * 
	 * 
	 * @param period
	 */
	public CronHandlerRunner(@Value("${}") String period) {
		
	}

	@Override
	protected void processData() throws Exception {
	}
}
