package com.redeye.logexporter.workflow;

import lombok.Builder;
import lombok.Data;

/**
 * 워크플로우 메시지 클래스
 * 
 * @author jmsohn
 */
@Data
@Builder
public class Message<T> {
	
	/** 제목 */
	private String topic;
	
	/** 메시지 바디 */
	private T body;
	
	
	/**
	 * 디폴트 생성자
	 */
	public Message() {
	}
	
	/**
	 * 생성자
	 * 
	 * @param topic 제목
	 */
	public Message(String topic) {
		this.setTopic(topic);
	}
	
	/**
	 * 생성자
	 * 
	 * @param topic 제목
	 * @param body 내용
	 */
	public Message(String topic, T body) {
		this.setTopic(topic);
		this.setBody(body);
	}
}
