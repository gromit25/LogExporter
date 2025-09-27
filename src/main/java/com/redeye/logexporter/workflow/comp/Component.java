package com.redeye.logexporter.workflow.comp;

import java.util.Map;

/**
 * 컴포넌트 인터페이스
 * 
 * @author jmsohn
 */
public interface Component {
	
	/**
	 * 컴포넌트 명 반환
	 * 
	 * @return 컴포넌트 명
	 */
	String name();
	
	/**
	 * 컴포넌트 실행 전 초기화
	 * 
	 * @param config
	 */
	default void init(Map<String, String> config) throws Exception {
		// Do nothing
	}
	
	/**
	 * 컴포넌트 실행 후 호출
	 */
	default void exit() throws Exception {
		// Do nothing
	}
}
