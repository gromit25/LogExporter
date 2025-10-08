package com.redeye.logexporter.workflow.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 액티비티 데이터 처리 메소드 어노테이션<br>
 * 아래의 형식 중 하나여야 함<br>
 * <li>List<Message<?>> method()</li>
 * <li>Message<?> method()</li>
 * <li>List<Message<?>> method(Message<?> message)</li>
 * <li>Message<?> method(Message<?> message)</li>
 * <li>void method(Message<?> message)</li>
 * <li>void method()</li>
 * 
 * @author jmsohn
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Process {

}
