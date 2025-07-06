package com.redeye.logexporter.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.StringUtil;
import com.redeye.logexporter.handler.filter.LogFilter;

/**
 * 메시지 필터링 처리 및 변환 작업(포맷팅) 수행
 * 
 * @author jmsohn
 */
@Component
public class LogHandler {
	
	/** 메시지 구분자 */
	@Value("${app.handler.delimiter}")
	private String delimiter;
	
	/** */
	@Autowired
	@Qualifier("filter")
	private LogFilter filter;
	

	/**
	 * 로그 메시지 처리
	 * 
	 * @param message 로그 메시지
	 * @param toExporterQueue exporter 큐
	 */
	public void handle(String message, BlockingQueue<String> toExporterQueue) throws Exception {
		
		// 로그 메시지가 null 이거나 exporter 큐가 null 일 경우 반환
		if(message == null || toExporterQueue == null) {
			return;
		}
		
		// log 메시지를 분해하여 values 컨테이너 생성
		Map<String, Object> values = this.makeValues(message);
		
		// 필터링 대상 여부 확인
		if(this.filter.shouldBeExported(message, values) == true) {
			
			// 메시지 포맷 변경
			
			// 메시지 전송
			toExporterQueue.put(message);
		}
	}
	
	/**
	 * 
	 * 
	 * @param message
	 * @return
	 */
	private Map<String, Object> makeValues(String message) {
		
		Map<String, Object> values = new HashMap<>();
		if(StringUtil.isBlank(message) == false) {
			
			String[] fieldArray = null;
			if(StringUtil.isEmpty(this.delimiter) == true) {
				fieldArray = new String[] {message};
			} else {
				fieldArray = message.split(this.delimiter);
			}
			
			List<String> fieldList = Arrays.asList(fieldArray);
			values.put("fields", fieldList);
		}
		
		return values;
	}
}
