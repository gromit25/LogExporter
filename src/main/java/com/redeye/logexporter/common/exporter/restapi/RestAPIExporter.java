package com.redeye.logexporter.common.exporter.restapi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.jutools.CronJob;
import com.jutools.FileUtil;
import com.jutools.publish.Publisher;
import com.jutools.publish.PublisherFactory;
import com.jutools.publish.PublisherType;
import com.jutools.workflow.Message;
import com.redeye.logexporter.agentlog.domain.TraceDTO;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @autor jmsohn
 */
@Slf4j
@Component("exporter")
@ConditionalOnProperty
(
	value = "app.exporter.type",
	havingValue = "RESTAPI"
)
public class RestAPIExporter {

	/** 포맷 파일 명 */
	private static final String FORMAT_FILE = "format/restapi/json_format.xml";
	
	/** API 호출 Subpath */
	private static final String API_SUBPATH = "/api/agentlog/%s/%s/%s";
	
	/** 기관 코드 */
	@Value("${app.exporter.restapi.organ.code")
	private String organCode;
	
	/** 도메인 코드 */
	@Value("${app.exporter.restapi.domain.code")
	private String domainCode;
	
	/** 어플리케이션 코드 */
	@Value("${app.exporter.restapi.app.code")
	private String appCode;
	
	/** 발송 주기 설정 값 */
	@Value("${app.exporter.restapi.period}")
	private String sendPeriod;
	
	/** API 전송용 크론잡 */
	private CronJob sendJob;

	/** API 클라이언트 */
	@Autowired
	@Qualifier("apiClient")
	private WebClient apiClient;
	
	/** API 메시지 생성용 publisher */
	private Publisher publisher;
	
	/** 조인 포인트 트래이스 객체 */
	private TraceDTO joinPointTrace;

	
	/**
	 * 초기화 수행
	 */
	@PostConstruct
	public void init() throws Exception {
		
	    // API 메시지 생성용 publisher 객체 생성
		InputStream formatInputStream = FileUtil.getInputStream(FORMAT_FILE);
		this.publisher = PublisherFactory.create(PublisherType.TEXT_FILE, formatInputStream);

		// API 전송용 크론잡 생성
		this.sendJob = new CronJob(
			this.sendPeriod,
			() -> {
				sendToAPI();
			}
		);
		
		this.sendJob.run();
	}
	
	/**
	 * API 서버로 조인포인트 데이터 전송
	 */
	private void sendToAPI() {
		
		try {
			
			// 통계 데이터 임시 저장 후 신규 생성
			Map<String, Object> values = new HashMap<>();
			values.put("joinPointTrace", this.joinPointTrace);
			
			this.joinPointTrace = new TraceDTO();
			
			// JSON 출력 실행
			String joinPointJSON = this.publisher.publish(Charset.forName("UTF-8"), values); 
			log.info("API JSON: \n" + joinPointJSON);
			
			// API 호출
			String result = this.apiClient
				.post()
				.uri(String.format(API_SUBPATH, organCode, domainCode, appCode))
				.header("Content-Type", "application/json")
				.bodyValue(joinPointJSON)
				.retrieve()
				.bodyToMono(String.class)
				.block();
			
			log.info("RESULT: \n" + result);
			
		} catch(Exception ex) {
			log.error("failed to send request to API server.", ex);
		}
	}

	public void export(Message<?> message) throws Exception {
		//
	}
}

