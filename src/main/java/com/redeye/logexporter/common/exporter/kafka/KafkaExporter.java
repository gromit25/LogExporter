package com.redeye.logexporter.common.exporter.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.redeye.logexporter.workflow.Message;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 출력 Exporter
 * 
 * @author jmsohn
 */
@Slf4j
@Component("exporter")
@ConditionalOnProperty
(
	value = "app.exporter.type",
	havingValue = "KAFKA"
)
public class KafkaExporter {

	/** Kafka topic name */
	@Value("${app.exporter.kafka.topicname}")
	private String topicName;
	
	/** Kafka 연결 객체 */
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	/**
	 * 초기화
	 */
	@PostConstruct
	public void init() throws Exception {
		
		if(StringUtil.isBlank(this.topicName) == true) {
			throw new IllegalArgumentException("topicName(LE_EXPORTER_KAFKA_TOPIC_NAME) is null or blank.");
		}
	}

	/**
	 * Kafka 로 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	public void export(Message<?> message) throws Exception {
		
		// 메시지가 null 인 경우 반환
		if(message == null) {
			return;
		}
		
		log.info("SEND:" + message);
		this.kafkaTemplate.send(this.topicName, message.getBody().toString());
	}
}
