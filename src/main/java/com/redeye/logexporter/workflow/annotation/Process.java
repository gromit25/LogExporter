package com.redeye.logexporter.workflow.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 액티비티 데이터 처리 메소드 어노테이션<br>
 * 아래의 형식 중 하나여야 함<br>
 * List<Message<?>> method()<br>
 * List<Message<?>> method(Message<?> message)<br>
 * void method(Message<?> message)<br>
 * void method()
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Process {

}
