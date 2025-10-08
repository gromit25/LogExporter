package com.redeye.logexporter.common.handler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.StringUtil;
import com.jutools.script.olexp.OLExp;
import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Init;
import com.jutools.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * 스크립트를 이용해서 메시지 검사<br>
 * 검사 결과에 따라 export 여부 설정
 * 
 * @author jmsohn
 */
@Slf4j
@Activity(
	value="filter",
	from="${app.filter.from}",
	threadCount="${app.filter.thread.count}"
)
@ConditionalOnProperty(
	name="log.type",
	havingValue="common"
)
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
	@Init
	public void init() throws Exception {
		
		// 스크립트 컴파일
		log.info("script : " + this.scriptStr);
		
		this.script = (StringUtil.isBlank(scriptStr) == false)
				? OLExp.compile(this.scriptStr) : null;
	}
	
	/**
	 * 필터링 수행
	 * 
	 * @param message
	 * @return
	 */
	@Proc
	public Message<?> filter(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> body = (Map<String, Object>)message.getBody();
		
		return null;
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
