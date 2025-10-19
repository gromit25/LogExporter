package com.redeye.logexporter.common.exporter.restapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * REST API 접속 클라이언트 객체 생성 클래스
 * 
 * @author jmsohn
 */
@Configuration
@ConditionalOnProperty(
	name="app.restapi.use",
	havingValue="y"
)
public class RestAPIConfig {
	
	/**
	 * WebClient 객체 생성 및 반환
	 *
	 * @param url 기본 url
  	 * @return 생성된 WebClient 객체
	 */
	@Bean("apiClient")
	WebClient apiClient(
		@Value("app.restapi.url") String url
	) {
		return WebClient.builder()
			.baseUrl(url)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}
}
