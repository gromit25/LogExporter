package com.redeye.logexporter.workflow;

/**
 * exporter 인터페이스 클래스
 * 
 * @author jmsohn
 */
public interface Exporter {
  
	/**
	 * 리파지토리로 메시지 전송
	 *
	 * @param message 전송할 메시지 
	 */
	void send(String message) throws Exception;
}
