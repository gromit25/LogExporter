package com.redeye.logexpoter.filter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.jutools.script.olexp.OLExp;

import jakarta.annotation.PostConstruct;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
public class DefaultScriptFilter implements LogFilter {
	
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
		
		System.out.println("### DEBUG ###");
		System.out.println(message);
		
		//
		Map<String, Object> values = new HashMap<>();
		if(StringUtil.isBlank(message) == false) {
			
			String[] fieldArray = message.split("[ \\t]+");
			
			for(int index = 0; index < fieldArray.length; index++) {
				
				//
				String fieldName = "F" + (index + 1);
				values.put(fieldName, fieldArray[index]);
			}
			
			values.put("NF", fieldArray.length);
		}
		
		return this.script.execute(values).pop(Boolean.class);
	}
}
