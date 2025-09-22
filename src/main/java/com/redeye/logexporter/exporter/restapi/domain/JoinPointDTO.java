package com.readeye.logexporter.exporter.restapi.domain;

/**
 *
 *
 * @author jmsohn
 */
public class JoinPointDTO {
  
  private String key;
  
  private String message;
  
  /** 조인 포인트의 수행 시간 모수(Parameter) 통계 객체 */
  private Parameter elasedParameter;
  
  private int errorCount;
}
