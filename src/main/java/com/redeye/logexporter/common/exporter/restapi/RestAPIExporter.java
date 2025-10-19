package com.redeye.logexporter.common.exporter.restapi;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.reactive.function.client.WebClient;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @autor jmsohn
 */
@Activity("exporter")
@ConditionalOnProperty(
	name="app.kafka.use",
	havingValue="y"
)
@Slf4j
public class RestAPIExporter {

	/** API 클라이언트 */
	@Autowired
	@Qualifier("apiClient")
	private WebClient apiClient;

	
	/**
	 * 
	 * 
	 * @param message
	 */
	@Proc
	public void export(Message<?> message) throws Exception {
		
		try {
			
			@SuppressWarnings("unchecked")
			Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
			
			// 전송할 subpath 획득
			String subpath = messageMap.get("subpath").toString();
			
			// 전송할 데이터 획득
			String payload = messageMap.get("payload").toString();
			
			// API 호출
			String result = this.apiClient
				.post()
				.uri(subpath)
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
