package com.redeye.logexporter.exporter.print;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.exporter.Exporter;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
@Component("exporter")
@ConditionalOnProperty
(
	value = "app.exporter.type",
	havingValue = "PRINT"
)
public class PrintExporter implements Exporter {

	/**
	 * 화면 출력
	 *
	 * @param message 출력할 메시지
	 */
	@Override
	public void send(String message) throws Exception {
		
		if(message == null) {
			return;
		}
		
		System.out.println("RECEIVED: " + message);
	}
}
