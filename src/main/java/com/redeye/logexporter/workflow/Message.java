package com.redeye.logexporter.workflow;

import lombok.Data;

/**
 * 워크플로우 메시지 클래스
 * 
 * @author jmsohn
 */
@Data
public class Message<T> {
	
	/** 제목 */
	private String subject;
	
	/** 메시지 바디 */
	private T body;
}
