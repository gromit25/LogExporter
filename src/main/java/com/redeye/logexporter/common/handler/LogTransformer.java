package com.redeye.logexporter.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.TextGen;

import jakarta.annotation.PostConstruct;

/**
 * 로그 메시지 변환
 * 
 * @author jmsohn
 */
@Component("transformer")
public class LogTransformer {
	
	/** 변환 포맷 */
	@Value("${app.handler.transformer.format}")
	private String format;
	
	/** 변환 생성용 객체 */
	private TextGen generator;

	/**
	 * 초기화
	 */
	@PostConstruct
	public void init() throws Exception {
		this.generator = TextGen.compile(
			this.format,
			true,	// Escape 문자 치환 기능
			'%'		// 표현식 시작 문자 지정
		);
	}
	
	/**
	 * 메시지 변환 수행
	 * 
	 * @param values 로그 메시지의 values 객체
	 * @return 변환된 메시지
	 */
	public String transform(Map<String, Object> values) throws Exception {
		return this.generator.gen(values);
	}
}
