package com.redeye.logexporter.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.StringUtil;
import com.jutools.script.olexp.OLExp;
import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * 스크립트를 이용해서 메시지 검사<br>
 * 검사 결과에 따라 export 여부 설정<br>
 * 설정값<br>
 * <li>app.filter.use: 'y' 일 경우 활성화</li>
 * <li>app.filter.from: 이전 액티비티 명</li>
 * <li>app.filter.thread.count: 스레드 수</li>
 * <li>app.filter.debug: 필터링 스크립트 디버깅 여부</li>
 * <li>app.filter.script: 필터링 스크립트</li>
 * 
 * @author jmsohn
 */
@Slf4j
@Activity(
	value="filter",
	from="${app.filter.from}",
	threadCount="${app.filter.thread.count:1}"
)
@ConditionalOnProperty(
	name="app.common.filter.use",
	havingValue="y"
)
public class LogFilter {
	
	
	/** 스크립트 디버깅 모드 true 이면 디버깅 모드로 동작 */
	@Value("${app.filter.debug:false}")
	private boolean debug = false;
	
	/** 스크립트 문자열 */
	@Value("${app.filter.script}")
	private String scriptStr;
	
	/** 스크립트 실행 객체 */
	private OLExp script;
	
	
	/**
	 * 초기화 수행 - filter 메소드의 Proc 어노테이션에 설정되어 있음
	 */
	public void init() throws Exception {
		
		// 스크립트 컴파일
		log.info("script: " + this.scriptStr);
		
		this.script =
				(StringUtil.isBlank(this.scriptStr) == false)
				? OLExp.compile(this.scriptStr):null;
	}
	
	/**
	 * 필터링 수행
	 * 
	 * @param message 수신 메시지
	 * @return 필터링된 메시지
	 */
	@Proc(init="init")
	public Message<?> filter(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>)message.getBody();

		// 필터링 여부 검사
		if(this.shouldBeExported(body) == true) {
			return message;
		} else {
			return null;
		}
	}

	/**
	 * export 대상 여부 반환<br>
	 * true 일 경우 export 대상
	 * 
	 * @param values 로그 메시지의 values 객체
	 * @return export 대상 여부
	 */
	private boolean shouldBeExported(Map<String, Object> values) throws Exception {
		
		// 스크립트 설정이 없는 경우 무조건 export 시킴
		if(this.script == null) {
			return true;
		}
		
		// 스크립트 실행
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
