package com.redeye.logexporter.common.exporter.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;

import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 출력 Exporter
 * 
 * @author jmsohn
 */
@Slf4j
@Activity(
	value="kafka",
	from="${app.common.kafka.from}"
)
@ConditionalOnProperty(
	name="app.common.kafka.use",
	havingValue="y"
)
public class KafkaExporter {

	/** Kafka 연결 객체 */
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	
	/**
	 * Kafka 로 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	@Proc
	public void export(Message<?> message) throws Exception {
		
		log.info("SEND:" + message);
		this.kafkaTemplate.send(message.getTopic(), message.getBody().toString());
	}
}
