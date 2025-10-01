package com.redeye.logexporter.workflow.comp;

import java.util.Map;

/**
 * 컴포넌트 인터페이스
 * 
 * @author jmsohn
 */
public interface Component {
	
	/**
	 * 컴포넌트 실행 전 초기화<br>
	 * 재 시작시 초기화가 필요한 경우
	 */
	default void init() throws Exception {
		// Do nothing
	}
	
	/**
	 * 컴포넌트 실행 후 호출
	 */
	default void exit() throws Exception {
		// Do nothing
	}
}
