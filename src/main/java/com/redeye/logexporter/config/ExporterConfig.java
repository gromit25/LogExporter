package com.redeye.logexporter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redeye.logexporter.exporter.Exporter;
import com.redeye.logexporter.exporter.ExporterType;

/**
 * Exporter 생성 컴포넌트
 * 
 * @author jmsohn
 */
@Configuration
public class ExporterConfig {

	/**
	 * Expoert 컴포넌트를 생성하여 반환
	 *
	 * @param typeStr 환경 변수에 설정된 Exporter 의 타입
	 * @return  생성된 Exporter 컴포넌트
	 */
	@Bean("exporter")
	Exporter exporter(@Value("${app.exporter.type}") String typeStr) {
		ExporterType type = ExporterType.valueOf(typeStr);
		return type.create();
	}
}
