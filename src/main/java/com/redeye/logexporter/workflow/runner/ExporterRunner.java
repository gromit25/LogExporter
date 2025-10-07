package com.redeye.logexporter.workflow.runner;

import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Exporter;

/**
 * 익스포터 실행 클래스
 * 
 * @author jmsohn
 */
public class ExporterRunner extends AbstractRunner<Exporter> {

	/**
	 * 생성자
	 * 
	 * @param name 컴포넌트 명
	 * @param exporter 익스포터 컴포넌트
	 */
	ExporterRunner(String name, Exporter exporter) {
		super(name, exporter);
		this.setFromQueue(new LinkedBlockingQueue<>());
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
		this.getComponent().export(message);
	}
}
