package com.redeye.logexporter.agentlog.domain;

import com.jutools.stat.Parameter;

import lombok.Data;

/**
 * 조인 포인트 DTO 클래스
 *
 * @author jmsohn
 */
@Data
public class JoinPointDTO {

	/** 키 - 타입:클래스:메소드:LOC */
	private String key;

	/** 메시지 - SQL 문 등 추가 메시지 */
	private String message;

	/** 조인 포인트의 수행 시간 모수(Parameter) 통계 객체 */
	private Parameter elasedParameter = new Parameter();

	/** 오류 발생 수 */
	private int errorCount = 0;
	
	
	/**
	 * 생성자
	 * 
	 * @param key 키 값
	 * @param message 메시지
	 */
	JoinPointDTO(String key, String message) {
		this.key = key;
		this.message = message;
	}
	
	/**
	 * 수행 시간 
	 * 
	 * @param elapsed
	 */
	public void add(long elapsed) throws Exception {
		this.elasedParameter.add((double)elapsed);
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		
		builder
			.append("\"").append(this.key).append("\": {")
			.append("\"message\": \"").append(this.message).append("\",")
			.append("\"count\": ").append(this.elasedParameter.getCount()).append(",")
			.append("\"sum\": ").append(this.elasedParameter.getSum()).append(",")
			.append("\"squaredSum\": ").append(this.elasedParameter.getSquaredSum()).append(",")
			.append("\"cubedSum\": ").append(this.elasedParameter.getCubedSum()).append(",")
			.append("\"fourthPoweredSum\": ").append(this.elasedParameter.getFourthPoweredSum()).append(",")
			.append("\"min\": ").append(this.elasedParameter.getMin()).append(",")
			.append("\"max\": ").append(this.elasedParameter.getMax())
			.append("}");
		
		return builder.toString();
	}
}
