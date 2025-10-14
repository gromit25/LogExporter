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
	 * @param messageMap 로그 메시지 맵
	 * @return 현재 객체
	 */
	public void add(Map<String, Object> messageMap) throws Exception {

		long timestamp = messageMap.get("timestamp");
		
		// 키를 통해 조인 포인트 정보 획득
		String key = messageMap.get("key").toString();
		JoinPointDTO joinPoint = this.joinPointMap.getOrDefault(
			key,
			new JoinPointDTO(key, messageMap.get("message").toString())
		);
		
		// 조인 포인트의 
		joinPoint.add((long)messageMap.get("elapsed"));
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		
		builder
			.append("{")
			.append("\"start\": " + this.startTime + ",")
			.append("\"end\": " + this.endTime + ",");
		
		//
		builder.append("\"joinPointMap\": {");
		for(String key: this.joinPointMap.keySet()) {
			builder.append(this.joinPointMap.get(key).toString());
		}
		builder.append("}");
		
		//
		builder.append("}");
		
		return builder.toString();
	}
}
