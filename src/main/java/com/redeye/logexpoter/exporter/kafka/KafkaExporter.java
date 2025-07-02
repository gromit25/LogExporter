package com.redeye.logexpoter.exporter.kafka;

import org.springframework.stereotype.Component;

import com.redeye.logexpoter.exporter.Exporter;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
public class KafkaExporter extends Exporter {
  
  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;
}
