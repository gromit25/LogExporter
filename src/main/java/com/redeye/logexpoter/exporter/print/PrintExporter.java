package com.readeye.logexporter.exporter.print;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
@Component
public class PrintExporter implements Exporter {
  
  @Override
  public void send(String message) throws Exception {
    System.out.println("RECEIVED: " + message);
  }
}
