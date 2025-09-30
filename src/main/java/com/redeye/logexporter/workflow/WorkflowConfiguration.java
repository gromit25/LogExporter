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
public class WorkflowConfiguration {

	/** */
	@Value("workflow.timeout")
	private long timeout;

	/** */
	@Value("workflow.maxlag")
	private int maxLag; 
	
	/** 런너 객체 생성 팩토리 객체 */
	@Autowired
	private RunnerFactory factory;

	/** 컴포넌트 맵 */
	@Autowired
	private Map<String, Component> componentMap;

	
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
		// 
		Component component;
		
		// 컴포넌트의 런너 생성 및 설정
		AbstractRunner runner = this.factory.create(component, threadCount);
		
		// 런너 링킹 -------------
		
		// 워크플로우 생성 및 반환
		Workflow wf = new Workflow();
		wf.setRunnerMap(map);
		
		return wf;
	}
}
