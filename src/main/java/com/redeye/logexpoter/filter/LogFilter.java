package com.redeye.logexpoter.filter;

/**
 * 로그 필터 인터페이스
 * 
 * @author jmsohn
 */
public interface LogFilter {
	
	/**
	 * 로그 메시지를 필터링할 것인지 여부 반환
	 * 
	 * @param message 로그 메시지
	 * @return 필터링 여부 (true: export 됨, false: export 안됨)
	 */
	boolean shouldBeExported(String message) throws Exception;
}
