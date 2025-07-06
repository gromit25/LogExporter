package com.redeye.logexporter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.handler.filter.LogFilter;
import com.redeye.logexporter.handler.filter.ScriptFilter;

/**
 *
 *
 * @author jmsohn
 */
@Configuration
public class LogFilterConfig {

	@Bean("filter")
	LogFilter logFilter() {
		return new ScriptFilter();
	}
}
