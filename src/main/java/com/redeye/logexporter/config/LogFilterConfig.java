package com.redeye.logexporter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.handler.filter.LogFilter;
import com.redeye.logexporter.handler.filter.ScriptFilter;

/**
 * 필터 컴포넌트 생성 (LogHandler 에서 사용)
 *
 * @author jmsohn
 */
@Configuration
public class LogFilterConfig {

	/**
	 * 필터 컴포넌트 생성 및 반환
	 * 
	 * @return 필터 컴포넌트
	 */
	@Bean("filter")
	LogFilter logFilter() {
		// 추후, 설정에 따라 선택 가능할 것 같아서 넣어는 놨는데
		// 어떻게 될지 모르겠음
		return new ScriptFilter();
	}
}
