package com.redeye.logexporter.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.workflow.runner.AbstractRunner;

/**
 * Workflow 생성 클래스
 * 
 * @author jmsohn
 */
@Configuration
@ConfigurationProperties(prefix="workflow.comp")
public class WorkflowConfiguration {
	
	/** 컴포넌트 명 패턴 문자열 */
	private static final String COMPONENT_NAME_PATTERN = "[a-zA-Z0-9][a-zA-Z0-9_\\-]*";
	
	/** 워크플로우 설정 맵 */
	private final Map<String, String> config;
	
	/** */
	private Pattern componentNameP = Pattern.compile(COMPONENT_NAME_PATTERN);

	
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
	@Bean("workflow")
	Workflow workflow() throws Exception {
		
		// 런너 맵
		Map<String, AbstractRunner> map = new HashMap<>();
		
		// 런너 생성 -------------
		// 컴포넌트 생성
		// 컴포넌트의 런너 생성 및 설정
		
		// 런너 링킹 -------------
		
		// 워크플로우 생성 및 반환
		Workflow wf = new Workflow();
		wf.setRunnerMap(map);
		
		return wf;
	}
}
