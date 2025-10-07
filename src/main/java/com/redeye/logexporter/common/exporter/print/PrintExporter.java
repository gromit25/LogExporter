package com.redeye.logexporter.common.exporter.print;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Process;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
@Activity(
	value="exporter",
	from="logTracker"
)
@ConditionalOnProperty(
	name="log.type",
	havingValue="common"
)
public class PrintExporter {

	/**
	 * 화면 출력
	 *
	 * @param message 출력할 메시지
	 */
	@Process
	public void export(Message<?> message) throws Exception {
		
		if(message == null) {
			return;
		}
		
		System.out.println("RECEIVED: " + message);
	}
}
