package com.redeye.logexpoter.exporter.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.redeye.logexpoter.exporter.Exporter;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
public class KafkaExporter implements Exporter {
  
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Value("${}")
	private String topicName;

	@Override
	public void send(String message) throws Exception {
		
		if(message == null) {
			throw new IllegalArgumentException("message is null.");
		}
		
		this.kafkaTemplate.send(this.topicName, message);
  }
}
