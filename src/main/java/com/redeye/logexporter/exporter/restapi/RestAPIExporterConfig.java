package com.redeye.logexporter.exporter.restapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 
 * 
 * @author jmsohn
 */
@Configuration
@ConditionalOnProperty
(
	value = "app.exporter.type",
	havingValue = "RESTAPI"
)
public class RestAPIExporterConfig {
	
	/**
	 * WebClient 객체 생성 및 반환
	 *
	 * @param url 기본 url
  	 * @return 생성된 WebClient 객체
	 */
	@Bean("apiClient")
	WebClient apiClient(
		@Value("app.exporter.restapi.url") String url
	) {
		return WebClient.builder()
			.baseUrl(url)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}
}
