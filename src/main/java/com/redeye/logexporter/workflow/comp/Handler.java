package com.redeye.logexporter.workflow.comp;

import java.util.List;

import com.redeye.logexporter.workflow.Message;

/**
 * 핸들러 인터페이스
 * 
 * @author jmsohn
 */
public interface Handler extends Component {
	
	/**
	 * 메시지 변환 및 처리 후 반환
	 * 
	 * @param message 처리할 메시지
	 * @return 처리된 메시지
	 */
	List<Message<?>> handle(Message<?> message) throws Exception;
}
