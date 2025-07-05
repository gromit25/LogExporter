package com.redeye.logexporter.exporter.print;

import com.redeye.logexporter.exporter.Exporter;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
public class PrintExporter implements Exporter {

	/**
	 * 화면 출력
	 *
	 * @param message 출력할 메시지
	 */
	@Override
	public void send(String message) throws Exception {
		System.out.println("RECEIVED: " + message);
	}
}
