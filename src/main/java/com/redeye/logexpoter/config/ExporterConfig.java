package com.redeye.logexpoter.config;

import org.springframework.context.annotation.Configuration;

/**
 * 
 * 
 * @author jmsohn
 */
@Configuration
public class ExporterConfig {

  @Bean("exporter")
  public Exporter exporter() {
    return new KafkaExporter();
  }
}
