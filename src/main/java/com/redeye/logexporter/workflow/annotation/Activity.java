package com.redeye.logexporter.workflow.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.runner.LinkType;

/**
 * 워크플로우 액티비티 어노테이션<br>
 * 액티비티는 하나의 Process 어노테이션이 있어야 함
 * 
 * @author jmsohn
 */
@Retention(RUNTIME)
@Target(TYPE)
@Component
public @interface Activity {
	
	/** 스프링 부트 컴포넌트 명 */
	String value() default "";
	
	/** from 액티비티와의 연결 타입 */
	LinkType linkType() default LinkType.NORMAL;
	
	/** from 액티비티 설정 */
	String from() default "";
	
	/** 구독할 제목 */
	String subscribe() default "";
	
	/** 스레드 개수 */
	String threadCount() default "1";
}
