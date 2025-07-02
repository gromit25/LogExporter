package com.redeye.logexpoter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexpoter.filter.ScriptFilter;
import com.redeye.logexpoter.filter.LogFilter;

/**
 *
 *
 * @author jmsohn
 */
@Configuration
public class LogFilterConfig {

	@Bean("logFilter")
	LogFilter logFilter() {
		return new ScriptFilter();
	}
}
