package com.redeye.logexporter.workflow.runner;

import java.util.List;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Collector;

/**
 * 컬렉터 실행 클래스
 * 
 * @author jmsohn
 */
public class CollectorRunner extends AbstractRunner<Collector> {

	/**
	 * 생성자
	 * 
	 * @param name 컴포넌트 명
	 * @param collector 컬렉터 컴포넌트
	 */
	CollectorRunner(String name, Collector collector) {
		super(name, collector);
	}

	@Override
	protected void processData() throws Exception {
		
		// 메시지 수집 후 결과 반환
		List<Message<?>> messageList = this.getComponent().collect();
		
		// 결과를 구독 컴포넌트로 전달
		this.put(messageList);
	}
}
