package com.redeye.logexporter.handler.filter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

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
public class ScriptFilter implements LogFilter {
	
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

	@Override
	public boolean shouldBeExported(String message, Map<String, Object> values) throws Exception {
		
		// 입력값 검증
		if(message == null) {
			log.info("message is null.");
			return false;
		}
		
		// 스크립트 설정이 없는 경우 무조건 export 시킴
		if(this.script == null) {
			return true;
		}
		
		// 스크립트 실행
		log.info("script : " + this.scriptStr);
		if(this.debug == true) {
			return this.script.executeForDebug(values).pop(Boolean.class);
		} else {
			return this.script.execute(values).pop(Boolean.class);
		}
	}
}
