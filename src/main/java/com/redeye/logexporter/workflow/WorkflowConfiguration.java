package com.redeye.logexporter.workflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.workflow.annotation.LinkType;
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
	
	/** */
	@Autowired
	private WorkflowContext context;

	/** 런너 객체 생성 팩토리 객체 */
	@Autowired
	private RunnerFactory factory;

	/** 컴포넌트 맵 - 스프링부트에서 설정됨 */
	@Autowired
	private Map<String, Component> componentMap;

	
	/**
	 * 워크플로우 생성 메소드
	 * 
	 * @return 생성된 워크플로우
	 */
	@Bean("workflow")
	Workflow workflow() throws Exception {
		
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
			
			AbstractRunner<?> runner = runnerMap.get(name);
			
			//
			String from = this.context.getFrom(runner);
			AbstractRunner<?> fromRunner = runnerMap.get(from);
			if(fromRunner == null) {
				continue;
			}
			
			//
			LinkType type = this.context.getType(runner);
			
			if(type == LinkType.NOTICE_HANDLER) {
				fromRunner.addNoticeSubscriber(runner);
			} else {
				fromRunner.addSubscriber(runner);
			}
		}
	}
}
