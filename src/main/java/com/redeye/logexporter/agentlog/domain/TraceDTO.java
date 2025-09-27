package com.redeye.logexporter.agentlog.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

/**
 * 조인 포인트 트레이스 DTO 객체
 * 
 * @author jmsohn
 */
@Data
public class TraceDTO {

	/** 기간 시작 시간 */
	private long startTime;

	/** 기간 종료 시간 */
	private long endTime;

	/** 조인 포인트 맵 */
	private Map<String, JoinPointDTO> joinPointMap = new ConcurrentHashMap<>();

	/** 조인 포인트 링크 목록 */
	private List<LinkDTO> linkList;


	/**
	 * 메시지 파싱 및 조인 포인트 정보 업데이트
	 * 
	 * @param log 로그 메시지
	 * @return 현재 객체
	 */
	public TraceDTO add(String log) throws Exception {
		return this;
	}
}
