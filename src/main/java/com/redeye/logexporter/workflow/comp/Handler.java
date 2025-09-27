package com.redeye.logexporter.workflow.comp;

/**
 * 핸들러 인터페이스 클래스
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
	String[] handle(String message) throws Exception;
}
