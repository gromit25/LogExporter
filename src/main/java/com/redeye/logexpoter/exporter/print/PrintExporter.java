package com.redeye.logexpoter.exporter.print;

import com.redeye.logexpoter.exporter.Exporter;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
public class PrintExporter implements Exporter {
  
	@Override
	public void send(String message) throws Exception {
		System.out.println("RECEIVED: " + message);
	}
}
