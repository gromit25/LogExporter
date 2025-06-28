package com.redeye.logexpoter.filter;

/**
 * 로그 필터 인터페이스
 * 
 * @author jmsohn
 */
public interface Filter {
	
	/**
	 * 로그 메시지를 필터링할 것인지 여부 반환
	 * 
	 * @param log 로그 메시지
	 * @return 필터링 여부 (true: 필터되어 export 퇴지 않음, false: export 됨)
	 */
	boolean filter(String log);
}
