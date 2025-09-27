package com.redeye.logexporter.workflow;

import lombok.Data;

/**
 * 
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
