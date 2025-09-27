package com.redeye.logexporter.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.jutools.script.olexp.OLExp;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * 스크립트를 이용해서 메시지 검사
 * 검사 결과에 따라 export 여부 설정
 * 
 * @author jmsohn
 */
@Slf4j
@Component("filter")
public class LogFilter {
	
	/** 스크립트 디버깅 모드 true 이면 디버깅 모드로 동작 */
	@Value("${app.handler.filter.debug}")
	private boolean debug;
	
	/** 스크립트 문자열 */
	@Value("${app.handler.filter.script}")
	private String scriptStr;
	
	/** 스크립트 실행 객체 */
	private OLExp script;
	
	/**
	 * 초기화 수행
	 */
	@PostConstruct
	public void init() throws Exception {
		
		this.script = (StringUtil.isBlank(scriptStr) == false)
				? OLExp.compile(this.scriptStr) : null;
	}

	/**
	 * export 대상 여부 반환<br>
	 * true 일 경우 export 대상
	 * 
	 * @param values 로그 메시지의 values 객체
	 * @return export 대상 여부
	 */
	public boolean shouldBeExported(Map<String, Object> values) throws Exception {
		
		// 스크립트 설정이 없는 경우 무조건 export 시킴
		if(this.script == null) {
			return true;
		}
		
		// 스크립트 실행
		log.info("script : " + this.scriptStr);
		
		try {
			
			if(this.debug == true) {
				return this.script.executeForDebug(values).pop(Boolean.class);
			} else {
				return this.script.execute(values).pop(Boolean.class);
			}
			
		} catch(Exception ex) {
			log.error("filter script error.", ex);
			return false; 
		}
	}
}
