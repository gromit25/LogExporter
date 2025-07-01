package com.redeye.logexporter.config;

import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author jmsohn
 */
@Configuration
public class LogFilterConfig {

  @Bean("logFilter")
  public LogFilter logFilter() {
    return new DefaultScriptFilter();
  }
}
