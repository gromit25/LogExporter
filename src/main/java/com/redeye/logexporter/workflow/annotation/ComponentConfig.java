package com.redeye.logexporter.workflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 워크플로우 컴포넌트 설정
 * 
 * @author jmsohn
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE)
public @interface ComponentConfig {
	
	/** 컴포넌트 타입 */
	LinkType type() default LinkType.NORMAL;
	
	/** from 컴포넌트 설정 */
	String from() default "";
	
	/** 구독할 제목 */
	String subscribe() default "";
	
	/** 스레드 개수 */
	int threadCount() default 1;
}
