package com.redeye.logexporter.exporter.restapi.domain;

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

	/** 메시지 */
	private String message;

	/** 조인 포인트의 수행 시간 모수(Parameter) 통계 객체 */
	private Parameter elasedParameter;

	/** 오류 발생 수 */
	private int errorCount;
}
