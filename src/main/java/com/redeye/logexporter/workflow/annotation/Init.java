package com.redeye.logexporter.workflow.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 초기화 메소드 어노테이션
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Init {

}
