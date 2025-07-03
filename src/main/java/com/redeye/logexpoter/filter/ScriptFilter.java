package com.redeye.logexpoter.filter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.jutools.StringUtil;
import com.jutools.script.olexp.OLExp;

import jakarta.annotation.PostConstruct;

/**
 * 
 * 
 * @author jmsohn
 */
public class ScriptFilter implements LogFilter {
	
	/** */
	@Value("${app.filter.script}")
	private String scriptStr;
	
	/** */
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
	public boolean shouldBeExported(String message) throws Exception {
		
		// 스크립트 설정이 없는 경우 무조건 export 시킴
		if(this.script == null) {
			return true;
		}
		
		// log 메시지를 분해하여 values 에 추가
		Map<String, Object> values = new HashMap<>();
		if(StringUtil.isBlank(message) == false) {
			
			String[] fieldArray = message.split("[ \\t]+");
			List<String> fieldList = Arrays.asList(fieldArray);
			
			values.put("fields", fieldList);
		}
		
		// 스크립트 실행 후 결과 반환
		return this.script.execute(values).pop(Boolean.class);
	}
}
