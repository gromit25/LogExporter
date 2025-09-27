package com.redeye.logexporter.workflow.comp;

import com.redeye.logexporter.workflow.Message;

/**
 * exporter 인터페이스 클래스
 * 
 * @author jmsohn
 */
public interface Exporter extends Component {
  
	/**
	 * 리파지토리로 메시지 전송
	 *
	 * @param message 전송할 메시지 
	 */
	void export(Message<?> message) throws Exception;
}
