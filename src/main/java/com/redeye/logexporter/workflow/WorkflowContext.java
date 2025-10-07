package com.redeye.logexporter.workflow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.annotation.ComponentConfig;
import com.redeye.logexporter.workflow.annotation.LinkType;
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
	private Map<String, String> contextMap = new ConcurrentHashMap<>();
	
	
	/**
	 * 
	 * 
	 * @param runner
	 */
	public void setupRunner(AbstractRunner<?> runner) throws Exception {
		
		// 런너 공통 정보 설정
		runner.setTimeout(this.timeout);
		runner.setMaxLag(this.maxLag);
		
		//
		runner.setSubscriptionSubject(this.getSubscriptionSubject(runner));
		runner.setThreadCount(this.getThreadCount(runner));
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public LinkType getType(AbstractRunner<?> runner) throws Exception {
		
		String value = this.getContext(runner, "type");
		if(value != null) {
			return LinkType.valueOf(value);
		} else {
			return getComponentConfig(runner).linkType();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public String getFrom(AbstractRunner<?> runner) throws Exception {
		
		String value = this.getContext(runner, "from");
		if(value != null) {
			return value;
		} else {
			return getComponentConfig(runner).from();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public String getSubscriptionSubject(AbstractRunner<?> runner) throws Exception {
		
		String value = this.getContext(runner, "subscribe");
		if(value != null) {
			return value;
		} else {
			return getComponentConfig(runner).subscribe();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public int getThreadCount(AbstractRunner<?> runner) throws Exception {
		
		String value = this.getContext(runner, "threadcount");
		if(value != null) {
			return Integer.parseInt(value);
		} else {
			return getComponentConfig(runner).threadCount();
		}
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
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	private static ComponentConfig getComponentConfig(AbstractRunner<?> runner) {
		return runner.getComponent().getClass().getAnnotation(ComponentConfig.class);
	}
}
