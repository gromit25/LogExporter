package com.redeye.logexporter.workflow;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="comp")
public class WorkflowBuilder {

	public WorkflowBuilder(Map<String, String> config) throws Exception {
		// 컴포넌트 객체 생성
		// 컴포넌트 설정 
	}
}
