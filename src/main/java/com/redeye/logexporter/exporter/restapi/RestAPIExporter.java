package com.redeye.logexporter.exporter.restapi;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.jutools.CronJob;
import com.jutools.FileUtil;
import com.jutools.publish.Publisher;
import com.jutools.publish.PublisherFactory;
import com.jutools.publish.PublisherType;
import com.redeye.logexporter.exporter.Exporter;

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
public class RestAPIExporter implements Exporter {

	/** 포맷 파일 명 */
	private static final String FORMAT_FILE = "format/api/json_format.xml";
	
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
	
	/** 발송 잡 */
	private CronJob sendJob;

	/** API 클라이언트 */
	@Autowired
	private WebClient webClient;
	
	/** 조인 포인트 맵 */
	private Map<String, Object> joinPointMap = new ConcurrentHashMap<>();
	
	/**
	 * 초기화 수행
	 */
	@PostConstruct
	public void init() throws Exception {
		
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
			
		    // 출력 format input stream
			InputStream formatInputStream = FileUtil.getInputStream(FORMAT_FILE);
			
			// JSON 출력 실행
			Publisher publisher = PublisherFactory.create(PublisherType.TEXT_FILE, formatInputStream);
			String joinPointJSON = publisher.publish(Charset.forName("UTF-8"), this.joinPointMap); 
			log.info("API JSON: \n" + joinPointJSON);
			
			// API 호출
			String result = this.webClient.post()
				.uri(String.format(API_SUBPATH, organCode, domainCode, appCode))
				.header("Content-Type", "application/json")
				.bodyValue(joinPointJSON)
				.retrieve()
				.bodyToMono(String.class)
				.block();
			
			log.info("RESULT: \n" + result);
			
		} catch(Exception ex) {
			
			log.error("failed to send request to API server.", ex);
			
		} finally {
			
			// 조인포인트 정보 삭제
			this.joinPointMap.clear();
		}
	}

	@Override
	public void send(String message) throws Exception {
		// 입력 데이터 확인
	}
}

