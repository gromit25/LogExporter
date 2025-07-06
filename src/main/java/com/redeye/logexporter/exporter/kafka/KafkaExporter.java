package com.redeye.logexporter.exporter.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import com.jutools.StringUtil;
import com.redeye.logexporter.exporter.Exporter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 출력 Exporter
 * 
 * @author jmsohn
 */
@Slf4j
public class KafkaExporter implements Exporter {
  
	/** Kafka client id */
	@Value("${app.exporter.kafka.clientid}")
	private String clientId;

	/** Kafka topic name */
	@Value("${app.exporter.kafka.topicname}")
	private String topicName;

	/** Kafka url */
	@Value("${app.exporter.kafka.url}")
	private String url;
	
	/** Kafka 연결 객체 */
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	/**
	 * 초기화
	 */
	@PostConstruct
	public void init() throws Exception {
		
		if(StringUtil.isBlank(this.clientId) == true) {
			throw new IllegalArgumentException("clientId(LE_EXPORTER_KAFKA_CLIENT_ID) is null or blank.");
		}
		
		if(StringUtil.isBlank(this.topicName) == true) {
			throw new IllegalArgumentException("topicName(LE_EXPORTER_KAFKA_TOPIC_NAME) is null or blank.");
		}
		
		if(StringUtil.isBlank(this.url) == true) {
			throw new IllegalArgumentException("url(LE_EXPORTER_KAFKA_URL) is null or blank.");
		}
	}

	/**
	 * Kafka 로 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	@Override
	public void send(String message) throws Exception {
		
		// 메시지가 null 인 경우 반환
		if(message == null) {
			return;
		}
		
		log.info("send to kafka:" + message);
		this.kafkaTemplate.send(this.topicName, message);
	}
}
