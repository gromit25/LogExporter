package com.redeye.logexpoter.exporter;

/**
 * exporter
 * 
 * @author jmsohn
 */
public abstract class Exporter {
  
  /**
   * repository로 메시지 전송
   *
   * @param message 전송할 메시지 
   */
  public abstract void send(String message) throws Exception;
}
