package com.redeye.logexporter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.filter.LogFilter;
import com.redeye.logexporter.filter.ScriptFilter;

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
