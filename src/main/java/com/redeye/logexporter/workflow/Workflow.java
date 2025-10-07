package com.redeye.logexporter.workflow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.redeye.logexporter.workflow.runner.ActivityRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * 워크플로우 클래스
 * 
 * @author jmsohn
 */
@Slf4j
public class Workflow {
	
	/** 런너 맵 (key: 런너의 컴포넌트 명, value: 런너 */
	private Map<String, ActivityRunner> runnerMap = new ConcurrentHashMap<>();
	
	
	/**
	 * 워크플로우 실행
	 */
	public void run() {
		
		for(String key: this.runnerMap.keySet()) {
			
			ActivityRunner runner = this.runnerMap.get(key);
			
			try {
				runner.run();
			} catch(Exception ex) {
				log.error("start fail: " + runner.getName(), ex);
			}
		}
	}
	
	/**
	 * 워크플로우 중단
	 */
	public void stop() throws Exception {
		
		for(String key: this.runnerMap.keySet()) {
			
			ActivityRunner runner = this.runnerMap.get(key);
			
			try {
				runner.stop();
			} catch(Exception ex) {
				log.error("stop fail: " + runner.getName(), ex);
			}
		}
	}
	
	/**
	 * 런너 설정
	 * 
	 * @param runnerMap 설정할 런너
	 */
	public void setRunnerMap(Map<String, ActivityRunner> runnerMap) {
		this.runnerMap.clear();
		this.runnerMap.putAll(runnerMap);
	}
}
