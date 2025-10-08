package com.redeye.logexporter.common.exporter.print;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Proc;

/**
 * 화면 출력 Exporter (테스트용)
 *
 * @author jmsohn
 */
@Activity(
	value="exporter",
	from="${exporter.from}"
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
	@Proc
	public void export(Message<?> message) throws Exception {
		
		if(message == null) {
			return;
		}
		
		System.out.println("RECEIVED: " + message);
	}
}
