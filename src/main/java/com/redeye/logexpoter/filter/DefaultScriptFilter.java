package com.redeye.logexpoter.filter;

import org.springframework.stereotype.Component;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
public class DefaultScriptFilter implements LogFilter {

	@Override
	public boolean shouldBeExported(String log) {
		
		System.out.println("### DEBUG ###");
		System.out.println(log);
		
		return true;
	}
}
