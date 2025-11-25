package com.redeye.logexporter.common.exporter.restapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.reactive.function.client.WebClient;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest API 전송 Exporter<br>
 * 설정값<br>
 * <li>app.restapi.use: 'y' 일 경우 활성화</li>
 * <li>app.restapi.subpath: Rest API subpath</li>
 * <li>app.restapi.payload.formatfile: 바디 포맷 파일 위치</li>
 * <li>app.restapi.payload.charset: 캐릭터셋</li>
 * 
 * @autor jmsohn
 */
@Activity("restapi")
@ConditionalOnProperty(
	name="app.restapi.use",
	havingValue="y"
)
@Slf4j
public class RestAPIExporter {
	

	/** API 클라이언트 */
	@Autowired
	@Qualifier("apiClient")
	private WebClient apiClient;
	
	/** 서브패스 포맷 문자열 */
	@Value("${app.restapi.subpath}")
	private String subpath;
	
	
	/**
	 * REST API 호출하여 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	@Proc
	public void requestAPI(Message<?> message) throws Exception {
		
		try {
						
			// 전송할 데이터 획득
			String payload = message.getBody().toString();
			
			// API 호출
			String result =
				this.apiClient
				.post()
				.uri(this.subpath)
				.header("Content-Type", "application/json")
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(String.class)
				.block();
			
			log.info("RESULT: \n" + result);
			
		} catch(Exception ex) {
			log.error("failed to send request to API server.", ex);
		}
	}
}
