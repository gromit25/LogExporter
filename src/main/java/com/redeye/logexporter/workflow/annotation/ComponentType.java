package com.redeye.logexporter.workflow.annotation;

/**
 * 워크플로우 컴포넌트 종류
 * 
 * @author jmsohn
 */
public enum ComponentType {
	
	/** 일반 컴포넌트 */
	NORMAL,
	
	/** 에러 핸들러 컴포넌트 */
	ERROR_HANDLER;
}
