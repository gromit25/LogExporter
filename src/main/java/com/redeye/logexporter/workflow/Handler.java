package com.redeye.logexporter.workflow;

/**
 * 핸들러 인터페이스 클래스
 * 
 * @author jmsohn
 */
public interface Handler {
	
	/**
	 * 
	 * 
	 * @param message
	 * @return 
	 */
	String[] handle(String message) throws Exception;
}
