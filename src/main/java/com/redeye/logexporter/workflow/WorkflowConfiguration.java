package com.redeye.logexporter.workflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.workflow.comp.Component;
import com.redeye.logexporter.workflow.runner.AbstractRunner;
import com.redeye.logexporter.workflow.runner.RunnerFactory;

/**
 * Workflow 생성 클래스
 * 
 * @author jmsohn
 */
@Configuration
public class WorkflowConfiguration {

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
		
		
		// 런너 생성 -------------
		Map<String, AbstractRunner<?>> runnerMap = this.createRunner();
		
		// 런너 링킹 -------------
		this.linkRunner(runnerMap);
		
		// 워크플로우 생성 및 반환
		Workflow wf = new Workflow();
		wf.setRunnerMap(runnerMap);
		
		return wf;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private Map<String, AbstractRunner<?>> createRunner() throws Exception {
		
		Map<String, AbstractRunner<?>> runnerMap = new HashMap<>(); 
		
		for(String name: this.componentMap.keySet()) {
			
			// 컴포넌트 획득
			Component component = this.componentMap.get(name);
		
			// 컴포넌트 런너 생성 및 설정
			AbstractRunner<?> runner = this.factory.create(name, component);
			runnerMap.put(name, runner);
		}
		
		return runnerMap;
	}
	
	/**
	 * 
	 * 
	 * @param runnerMap
	 */
	private void linkRunner(Map<String, AbstractRunner<?>> runnerMap) throws Exception {
		
		for(String name: runnerMap.keySet()) {
			
			// 링킹 작업할 런너 획득
			AbstractRunner<?> subscribeRunner = runnerMap.get(name);
		
		}
	}
}
