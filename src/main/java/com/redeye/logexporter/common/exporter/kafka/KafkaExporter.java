package com.redeye.logexporter.common.exporter.kafka;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka 출력 Exporter<br>
 * 설정값<br>
 * <li>app.common.kafka.use: 'y' 일 경우 활성화</li>
 * <li>app.common.kafka.from: 이전 액티비티 명</li>
 * <li>app.common.kafka.topic: kafka topic</li>
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
	
	/** Kafka 전송 토픽명 */
	@Value("${app.common.kafka.topic}")
	private String topic;
	
	
	/**
	 * Kafka 로 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	@Proc
	public void export(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
		
		log.info("SEND:" + message);
		
		this.kafkaTemplate.send(
			this.topic,
			messageMap.get("key").toString(),
			messageMap.get("log").toString()
		);
	}
}
