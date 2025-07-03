package com.redeye.logexpoter.exporter;

/**
 * exporter
 * 
 * @author jmsohn
 */
public interface Exporter {
  
  /**
   * repository로 메시지 전송
   *
   * @param message 전송할 메시지 
   */
  void send(String message) throws Exception;
}
