package com.redeye.logexporter.common.exporter.restapi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.reactive.function.client.WebClient;

import com.jutools.FileUtil;
import com.jutools.StringUtil;
import com.jutools.TextGen;
import com.jutools.publish.Publisher;
import com.jutools.publish.PublisherFactory;
import com.jutools.publish.PublisherType;
import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * 
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
	private String subpathFormatStr;
	
	/** 서브패스 생성 객체 */
	private TextGen subpathGen;
	
	/** 페이로드 포맷 파일 위치 문자열 */
	@Value("${app.restapi.payload.formatfile}")
	private String payloadFormatFileStr;
	
	/** 페이로드 퍼블리셔 */
	private Publisher payloadPublisher;
	
	/** 페이로드 케릭터셋 문자열 */
	@Value("${app.restapi.payload.charset:}")
	private String charsetStr;
	
	/** 페이로드 캐릭터셋 문자열 */
	private Charset charset;
	
	
	/**
	 * 초기화
	 */
	public void init() throws Exception {
		
		// 서브패스 생성 객체 설정
		this.subpathGen = TextGen.compile(this.subpathFormatStr, '%');
		
		// 페이로드 퍼블리셔 설정
		InputStream formatIn = FileUtil.getInputStream(this.payloadFormatFileStr);
		this.payloadPublisher = PublisherFactory.create(PublisherType.TEXT_FILE, formatIn);
		
		// 페이로드 캐릭터셋 설정
		if(StringUtil.isBlank(this.charsetStr) == true) {
			this.charset = Charset.defaultCharset();
		} else {
			this.charset = Charset.forName(this.charsetStr);
		}
	}
	
	/**
	 * REST API 호출하여 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	@Proc
	public void requestAPI(Message<?> message) throws Exception {
		
		try {
			
			@SuppressWarnings("unchecked")
			Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
			
			// 전송할 subpath 획득
			String subpath = this.subpathGen.gen(messageMap);
			
			// 전송할 데이터 획득
			String payload = this.payloadPublisher.publish(this.charset, messageMap);
			
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
