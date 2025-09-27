package com.redeye.logexporter.workflow;

import java.util.List;

import com.redeye.logexporter.workflow.comp.Collector;

/**
 * Collector 런너 클래스
 * 
 * @author jmsohn
 */
public class CollectorRunner extends AbstractRunner {

	@Override
	protected void processData() throws Exception {
		
		// 메시지 수집 후 결과 반환
		List<Message<?>> messageList = this.getComponent(Collector.class).collect();
		
		// 결과를 구독 컴포넌트로 전달
		this.put(messageList);
	}
}
