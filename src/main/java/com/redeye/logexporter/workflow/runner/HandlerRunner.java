package com.redeye.logexporter.workflow.runner;

import java.util.List;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Handler;

/**
 * 핸들러 실행 클래스
 * 
 * @author jmsohn
 */
public class HandlerRunner extends AbstractRunner {

	public HandlerRunner(Handler handler) {
		
		//
		super(handler);

		// 입력 큐 설정
		this.setFromQueue();
	}

	@Override
	protected void processData() throws Exception {
		
		// 입력 큐에서 메시지를 가져옴
		// 없을 경우 즉시 반환
		Message<?> message = this.poll();
		if(message == null) {
			return;
		}
		
		// 메시지 처리 후 결과 반환
		List<Message<?>> messageList = this.getComponent(Handler.class).handle(message);
		
		// 결과를 구독 컴포넌트로 전달
		this.put(messageList);
	}
}
