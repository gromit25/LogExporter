package com.redeye.logexpoter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexpoter.exporter.Exporter;
import com.redeye.logexpoter.exporter.kafka.KafkaExporter;

/**
 * 
 * 
 * @author jmsohn
 */
@Configuration
public class ExporterConfig {

  @Bean("exporter")
  Exporter exporter() {
    return new KafkaExporter();
  }
}
