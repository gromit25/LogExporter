package com.redeye.logexporter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.jutools.SysUtil;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

/**
 * 로그 익스포터의 컨텍스트 클래스
 * 
 * @author jmsohn
 */
@Component
public class ExporterContext {

	/** 기관 코드 */
	@Value("${app.context.organ}")
	@Getter
	private String organCode;
	
	/** 도메인 코드 */
	@Value("${app.context.domain}")
	@Getter
	private String domainCode;
	
	/** 어플리케이션 코드 */
	@Value("${app.context.hostname:}")
	@Getter
	private String hostname;
	
	/** 어플리케이션 코드 */
	@Value("${app.context.app}")
	@Getter
	private String appCode;
	
	
	/**
	 * 초기화
	 */
	@PostConstruct
	public void init() throws Exception {
		
		// 호스트 명이 설정되어 있지 않으면 현재 호스트명을 설정
		if(StringUtil.isBlank(this.hostname) == true) {
			this.hostname = SysUtil.getHostname();
		}
	}
}
