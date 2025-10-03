package com.redeye.logexporter.workflow;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.annotation.ComponentConfig;
import com.redeye.logexporter.workflow.runner.AbstractRunner;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
@ConfigurationProperties(prefix = "workflow.comp") 
public class WorkflowContext {
	
	/** */
	private static final String COMPONENT_PREFIX = "workflow.comp.";
	
	/** */
	@Getter
	@Value("${workflow.timeout.sec}")
	private long timeout;

	/** */
	@Getter
	@Value("${workflow.maxlag}")
	private int maxLag;

	/** workflow.comp 이하의 컴포넌트 설정 값 */
	@Getter
	@Setter
	private Map<String, String> contextMap;
	
	
	/**
	 * 
	 * 
	 * @param runner
	 */
	public void setupRunner(AbstractRunner<?> runner) throws Exception {

		String subscribeSubject = "";
		int threadCount = 1;
		
		//
		ComponentConfig config = runner.getComponent().getClass()
				.getAnnotation(ComponentConfig.class);

		
		String configThreadCount = this.getContext(runner, "threadcount");
		if(configThreadCount != null) {
			threadCount = Integer.parseInt(configThreadCount);
		} else if(config != null) {
			threadCount = config.threadCount();
		}
		
		String configSubscribe = this.getContext(runner, "subscribe");
		if(configSubscribe != null) {
			subscribeSubject = configSubscribe;
		} else if(config != null) {
			subscribeSubject = config.subscribe(); 
		}
		
		// 런너 공통 정보 설정
		runner.setTimeout(this.timeout);
		runner.setMaxLag(this.maxLag);
		
		//
		runner.setSubscribeSubject(subscribeSubject);
		runner.setThreadCount(threadCount);
	}
	
	/**
	 * 
	 */
	public void linkRunner(Map<String, AbstractRunner<?>> runnerMap) throws Exception {
		
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @param propertyName
	 * @return
	 */
	private String getContext(AbstractRunner<?> runner, String propertyName) {
		return this.contextMap.get(COMPONENT_PREFIX + runner.getName() + "." + propertyName);
	}
}
