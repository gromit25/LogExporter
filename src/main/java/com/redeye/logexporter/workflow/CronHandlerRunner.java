package com.redeye.logexporter.workflow;

import java.util.List;

import com.jutools.CronJob;
import com.redeye.logexporter.workflow.comp.CronHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 크론 핸들러 실행 클래스
 * 
 * @authro jmsohn
 */
@Slf4j
public class CronHandlerRunner extends AbstractRunner {
	
	/** 크론 잡 */
	private CronJob job;
	
	
	/**
	 * 생성자
	 * 
	 * @param period 크론잡 주기
	 */
	public CronHandlerRunner(String period) throws Exception {
		
		// 크론 잡 생성
		this.job = new CronJob(period, () -> {
			
			try {
				
				List<Message<?>> messageList = getComponent(CronHandler.class).flush();
				put(messageList);
				
			} catch(Exception ex) {
				
				log.error(getComponent(CronHandler.class).name(), ex);
				putNotice(ex);
			}
		});
		
		// 크론 잡 수행
		this.job.run();
	}

	@Override
	protected void processData() throws Exception {
		
		// 입력 큐에서 메시지를 가져옴
		// 없을 경우 즉시 반환
		Message<?> message = this.poll();
		if(message == null) {
			return;
		}
		
		// 메시지 처리
		this.getComponent(CronHandler.class).accept(message);
	}
}
