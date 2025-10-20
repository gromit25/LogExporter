package com.redeye.logexporter.common.exporter.print;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

/**
 * 화면 출력 Exporter (테스트용)<br>
 * 설정값<br>
 * <li>app.print.use: 'y' 일 경우 활성화</li>
 * <li>app.print.from: 이전 액티비티 명</li>
 *
 * @author jmsohn
 */
@Activity(
	value="print",
	from="${app.print.from}"
)
@ConditionalOnProperty(
	name="app.print.use",
	havingValue="y"
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
