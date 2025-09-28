package com.redeye.logexporter.workflow;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Workflow 생성 클래스
 * 
 * @author jmsohn
 */
@Configuration
@ConfigurationProperties(prefix="comp")
public class WorkflowConfiguration {
	
	/** 워크플로우 설정 맵 */
	private final Map<String, String> config;

	
	/**
	 * 생성자 
	 * 
	 * @param config
	 */
	public WorkflowConfiguration(Map<String, String> config) {
		this.config = config;
	}
	
	/**
	 * 워크플로우 생성 메소드
	 * 
	 * @return 생성된 워크플로우
	 */
	@Bean
	public Workflow workflow() throws Exception {
		//TODO workflow 생성 코드 작성 예정
		return null;
	}
}
