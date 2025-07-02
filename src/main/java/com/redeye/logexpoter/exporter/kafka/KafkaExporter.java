package com.redeye.logexpoter.exporter.kafka;

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
    
    this.kafkaTemplate.send(message);
  }
}
