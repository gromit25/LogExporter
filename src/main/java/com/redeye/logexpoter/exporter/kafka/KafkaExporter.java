package com.redeye.logexpoter.exporter.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.redeye.logexpoter.exporter.Exporter;

import jakarta.annotation.PostConstruct;

/**
 * Kafka 출력 Exporter
 * 
 * @author jmsohn
 */
@Component
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

	@Override
	public void send(String message) throws Exception {
		
		if(message == null) {
			throw new IllegalArgumentException("message is null.");
		}
		
		this.kafkaTemplate.send(this.topicName, message);
	}
}
