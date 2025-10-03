package com.redeye.logexporter.workflow.runner;

import java.util.List;

import com.jutools.CronJob;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.CronHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 크론 핸들러 실행 클래스
 * 
 * @authro jmsohn
 */
@Slf4j
public class CronHandlerRunner extends AbstractRunner<CronHandler> {
	
	/** 크론 잡 */
	private CronJob job;


	/**
	 * 생성자
	 * 
	 * @param name 컴포넌트 명
	 * @param cronHandler 크론  컴포넌트
	 */
	CronHandlerRunner(String name, CronHandler cronHandler) throws Exception {

		super(name, cronHandler);

		// 크론 잡 생성
		this.job = new CronJob(cronHandler.getPeriod(), () -> {
			
			try {
				
				List<Message<?>> messageList = getComponent().flush();
				put(messageList);
				
			} catch(Exception ex) {
				
				log.error(getName(), ex);
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
		this.getComponent().accept(message);
	}
}
