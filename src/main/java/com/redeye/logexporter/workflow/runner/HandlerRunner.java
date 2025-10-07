package com.redeye.logexporter.workflow.runner;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Handler;

/**
 * 핸들러 실행 클래스
 * 
 * @author jmsohn
 */
public class HandlerRunner extends AbstractRunner<Handler> {

	/**
	 * 생성자
	 * 
	 * @param name 컴포넌트 명
	 * @param handler 핸들러 컴포넌트
	 */
	HandlerRunner(String name, Handler handler) {
		super(name, handler);
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
		
		// 메시지 처리 후 결과 반환
		List<Message<?>> messageList = this.getComponent().handle(message);
		
		// 결과를 구독 컴포넌트로 전달
		this.put(messageList);
	}
}
