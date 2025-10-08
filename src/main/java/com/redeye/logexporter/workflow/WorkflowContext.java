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
	
	/** 액티비티 설정 접두사 */
	private static final String ACTIVITY_PREFIX = "workflow.activity.";
	
	/** from 큐 대기 시간 - from 액티비티가 없을 경우 무시됨 */
	@Getter
	@Value("${workflow.timeout.sec}")
	private long timeout;

	/** 큐의 최대치 - 최대치의 데이터가 대기 중이면 현재 데이터는 버려짐 */
	@Getter
	@Value("${workflow.maxlag}")
	private int maxLag;

	/** workflow.activity 이하의 컴포넌트 설정 값 */
	@Getter
	@Setter
	private Map<String, String> contextMap = new ConcurrentHashMap<>();
	
	
	/**
	 * 런너 객체에 설정 정보 반영
	 * 
	 * @param runner 런너 객체
	 */
	public void setupRunner(ActivityRunner runner) throws Exception {
		
		// 런너 공통 정보 설정
		runner.setTimeout(this.timeout);
		runner.setMaxLag(this.maxLag);
		
		// 구독 
		runner.setSubscriptionSubject(this.getSubscriptionTopic(runner));
		runner.setThreadCount(this.getThreadCount(runner));
	}
	
	/**
	 * 런너 객체의 링크 타입 반환
	 * 
	 * @param runner 런너 객체
	 * @return 링크 타입
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
	 * 런너 객체의 from 값 반환 
	 * 
	 * @param runner 런너 객체
	 * @return from 값
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
	 * 런너 객체의 구독 토픽 반환
	 * 
	 * @param runner 런너 객체
	 * @return
	 */
	public String getSubscriptionTopic(ActivityRunner runner) throws Exception {
		
		String value = this.getContext(runner, "subscribe");
		if(value != null) {
			return value;
		} else {
			return runner.getActivityAnnotation().subscribe();
		}
	}
	
	/**
	 * 런너 객체의 스레드 수 반환
	 * 
	 * @param runner 런너 객체
	 * @return 스레드 수
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
	 * 프로퍼티 파일에 속성 설정 값 반환
	 * 
	 * @param runner 런너 객체
	 * @param propertyName 속성 명
	 * @return 속성에 설정된 값
	 */
	private String getContext(ActivityRunner runner, String propertyName) {
		return this.contextMap.get(ACTIVITY_PREFIX + runner.getName() + "." + propertyName);
	}
}
