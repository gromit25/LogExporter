package com.redeye.logexporter.workflow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.runner.ActivityRunner;
import com.redeye.logexporter.workflow.runner.LinkType;

import lombok.Getter;
import lombok.Setter;

/**
 * 워크플로우 설정값 관리 클래스<br>
 * 액티비티 어노테이션 설정 값과 스프링부트 설정값을 관리<br>
 * 스프링부트 설정값을 더 우선시 함<br>
 * 예를 들어)<br>
 * Activity 어노테이션으로 from 을 지정해 두더라도<br>
 * 스프링부트에 workflow.activity.액티비티명.from 이 설정되어 있으면<br>
 * 어노테이션의 from 은 무시됨
 * 
 * @author jmsohn
 */
@Component
@ConfigurationProperties(prefix = "workflow.activity") 
public class WorkflowContext {
	
	/** */
	private static final String ACTIVITY_PREFIX = "workflow.activity.";
	
	/** */
	@Getter
	@Value("${workflow.timeout.sec}")
	private long timeout;

	/** */
	@Getter
	@Value("${workflow.maxlag}")
	private int maxLag;

	/** workflow.activity 이하의 컴포넌트 설정 값 */
	@Getter
	@Setter
	private Map<String, String> contextMap = new ConcurrentHashMap<>();
	
	
	/**
	 * 
	 * 
	 * @param runner
	 */
	public void setupRunner(ActivityRunner runner) throws Exception {
		
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
	public LinkType getType(ActivityRunner runner) throws Exception {
		
		String value = this.getContext(runner, "linktype");
		if(value != null) {
			return LinkType.valueOf(value);
		} else {
			return runner.getActivityAnnotation().linkType();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public String getFrom(ActivityRunner runner) throws Exception {
		
		String value = this.getContext(runner, "from");
		if(value != null) {
			return value;
		} else {
			return runner.getActivityAnnotation().from();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public String getSubscriptionSubject(ActivityRunner runner) throws Exception {
		
		String value = this.getContext(runner, "subscribe");
		if(value != null) {
			return value;
		} else {
			return runner.getActivityAnnotation().subscribe();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @return
	 */
	public int getThreadCount(ActivityRunner runner) throws Exception {
		
		String value = this.getContext(runner, "threadcount");
		if(value != null) {
			return Integer.parseInt(value);
		} else {
			return runner.getActivityAnnotation().threadCount();
		}
	}
	
	/**
	 * 
	 * 
	 * @param runner
	 * @param propertyName
	 * @return
	 */
	private String getContext(ActivityRunner runner, String propertyName) {
		return this.contextMap.get(ACTIVITY_PREFIX + runner.getName() + "." + propertyName);
	}
}
