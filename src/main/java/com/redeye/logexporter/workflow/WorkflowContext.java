package com.redeye.logexporter.workflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.runner.ActivityRunner;

import lombok.Getter;

/**
 * 워크플로우 공통 설정 값 관리 객체
 * 
 * @author jmsohn
 */
@Component
public class WorkflowContext {
	
	/** from 큐 대기 시간 - from 액티비티가 없을 경우 무시됨 */
	@Getter
	@Value("${workflow.timeout.sec}")
	private long timeout;

	/** 큐의 최대치 - 최대치의 데이터가 대기 중이면 현재 데이터는 버려짐 */
	@Getter
	@Value("${workflow.maxlag}")
	private int maxLag;

	
	/**
	 * 런너 객체에 설정 정보 반영
	 * 
	 * @param runner 런너 객체
	 */
	public void setupRunner(ActivityRunner runner) throws Exception {
		
		// 런너 공통 정보 설정
		runner.setTimeout(this.timeout);
		runner.setMaxLag(this.maxLag);
	}
}
