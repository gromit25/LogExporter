package com.redeye.logexporter.workflow.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.WorkflowContext;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
public class RunnerFactory {
	
	/** 워크플로우 컨텍스트(설정) */
	@Autowired
	private WorkflowContext context;

	
	/**
	 * 컴포넌트의 런너 객체 생성 후 반환
	 *
	 * @param name 액티비티 명
	 * @param activity 액티비티 객체 
	 * @return 컴포넌트의 런너 객체
	 */
	public ActivityRunner create(String name, Object activity) throws Exception {

		// 입력 값 검증
		if(activity == null) {
			throw new IllegalArgumentException("'activity' is null.");
		}

		// 컴포넌트 종류에 따라 런너 객체 생성
		ActivityRunner runner = new ActivityRunner(name, activity); 
    
		// 런너 객체 설정 - 컨텍스트 반영
		this.context.setupRunner(runner);

		// 생성된 런너 객체 반환
		return runner;
	}
}
