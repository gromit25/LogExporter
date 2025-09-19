package com.redeye.logexporter.exporter.restapi;

import static com.redeye.logexporter.Constants.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

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
	
	/** */
	@Value("${app.exporter.restapi.period}")
	private String sendPeriod;
	
	/** */
	private CronJob sendJob;

	/** */
	@Autowired
	private WebClient webClient;
	
	/**
	 * 
	 */
	@PostConstruct
	public void init() throws Exception {
		
	}

	@Override
	public void send(String message) throws Exception {

		// 입력 데이터 확인
		
	    // 출력 format input stream
		InputStream formatInputStream = FileUtil.getInputStream(FORMAT_FILE);
		
		// JSON 출력 실행
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Publisher publisher = PublisherFactory.create(PublisherType.TEXT_FILE, formatInputStream);
		publisher.publish(out, Charset.forName("UTF-8"), values);
		
		String schemaJSON = new String(out.toByteArray(), "UTF-8");
		log.info("API JSON: \n" + schemaJSON);
		
		// API 호출
		String result = this.webClient.post()
			.uri(String.format(API_SUBPATH, organCode, domainCode, appCode))
			.header("Content-Type", "application/json")
			.bodyValue(schemaJSON)
			.retrieve()
			.bodyToMono(String.class)
			.block();
		
		log.info("RESULT: \n" + result);
	}
}

