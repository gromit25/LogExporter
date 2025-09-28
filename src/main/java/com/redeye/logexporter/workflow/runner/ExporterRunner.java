package com.redeye.logexporter.workflow.runner;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Exporter;

/**
 * 익스포터 실행 클래스
 * 
 * @author jmsohn
 */
public class ExporterRunner extends AbstractRunner {

	@Override
	protected void processData() throws Exception {
		
		// 입력 큐에서 메시지를 가져옴
		// 없을 경우 즉시 반환
		Message<?> message = this.poll();
		if(message == null) {
			return;
		}
		
		// 메시지 처리
		this.getComponent(Exporter.class).export(message);
	}
}
