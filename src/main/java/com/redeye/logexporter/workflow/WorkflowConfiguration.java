package com.redeye.logexporter.workflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jutools.StringUtil;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.runner.ActivityRunner;
import com.redeye.logexporter.workflow.runner.LinkType;
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

	/** 액티비티 맵 */
	private Map<String, Object> activityMap;
	
	
	/**
	 * 생성자
	 * 
	 * @param context 스프링부트 어플리케이션 컨택스트 객체
	 */
	public WorkflowConfiguration(ApplicationContext context) throws Exception {
		this.activityMap = context.getBeansWithAnnotation(Activity.class);
	}

	
	/**
	 * 워크플로우 생성 메소드
	 * 
	 * @return 생성된 워크플로우
	 */
	@Bean("workflow")
	Workflow workflow() throws Exception {
		
		// 런너 생성 -------------
		Map<String, ActivityRunner> runnerMap = this.createRunner();
		
		// 런너 링킹 -------------
		this.linkRunner(runnerMap);
		
		// 워크플로우 생성 및 반환
		Workflow wf = new Workflow();
		wf.setRunnerMap(runnerMap);
		
		return wf;
	}

	/**
	 * 액티비티 런너 생성 후 반환<br>
	 * Activity 어노테이션이 설정된 모든 스프링부트 컴포넌트 대상
	 * 
	 * @return 생성된 액티비티 런너 - K: 스프링부트 컴포넌트 명, V: 생성된 액티비티 런너
	 */
	private Map<String, ActivityRunner> createRunner() throws Exception {
		
		Map<String, ActivityRunner> runnerMap = new HashMap<>(); 
		
		for(String name: this.activityMap.keySet()) {
			
			// 액티비티 객체 획득
			Object activity = this.activityMap.get(name);
		
			// 액티비티 런너 생성 및 설정
			ActivityRunner runner = this.factory.create(name, activity);
			runnerMap.put(name, runner);
		}
		
		return runnerMap;
	}
	
	/**
	 * 생성된 각 액티비티 런너를 링크
	 * 
	 * @param runnerMap 액티비티 맵
	 */
	private void linkRunner(Map<String, ActivityRunner> runnerMap) throws Exception {
		
		// 각 액티비티별 연결 수행
		for(String name: runnerMap.keySet()) {
			
			ActivityRunner runner = runnerMap.get(name);
			
			// 이전 액티비티 런너 명 획득
			String from = runner.getFrom();
			if(StringUtil.isEmpty(from) == true) {
				continue;
			}
			
			ActivityRunner fromRunner = runnerMap.get(from);
			if(fromRunner == null) {
				throw new Exception("from(" + from + ") activity is not found at " + runner.getName());
			}
			
			// 링크 타입 획득
			LinkType type = runner.getLinkType();
			
			// 링크 타입에 따라 이전 액티비티와 연결 설정
			if(type == LinkType.NOTICE_HANDLER) {
				fromRunner.addNoticeSubscriber(runner);
			} else {
				fromRunner.addSubscriber(runner);
			}
		}
	}
}
