package com.redeye.logexporter.agentlog.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jutools.TypeUtil;

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
	 * 생성자
	 *
	 * @param startTime
	 * @param endTime
	 */
	public TraceDTO(long startTime, long endTime) throws Exception {

		// 입력 값 검증
		if(startTime < 0) {
			throw new IllegalArgumentException("'startTime' must be greater than 0: " + startTime);
		}

		if(endTime < 0) {
			throw new IllegalArgumentException("'endTime' must be greater than 0: " + startTime);
		}

		if(endTime <= startTime) {
			throw new IllegalArgumentException("'endTime: " + endTime + "' must be greater than 'startTime: " + startTime + "'.");
		}

		// 설정
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * 메세지 타임스탬프 유효성 검증
	 *
	 * @param messageMap 로그 메시지 맵
	 * @return 유효성 여부(유효할 경우 true)
	 */
	public boolean isValid(Map<String, Object> messageMap) throws Exception {
		long timestamp = TypeUtil.toLong(messageMap.get("timestamp"));
		return timestamp >= this.startTime && timestamp <= this.endTime;
	}
	
	/**
	 * 메시지 파싱 및 조인 포인트 정보 업데이트
	 * 
	 * @param messageMap 로그 메시지 맵
	 */
	public void add(Map<String, Object> messageMap) throws Exception {

		// 메시지 타임스템프 검사
		if(this.isValid(messageMap) == false) {
			throw new IllegalArgumentException("invalid timestamp value(" + this.startTime + ", " + this.endTime + "): " + messageMap.get("timestamp"));
		}
		
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
		
		// 조인포인트 정보 출력
		builder.append("\"joinPointMap\": {");
		for(String key: this.joinPointMap.keySet()) {
			builder.append(this.joinPointMap.get(key).toString());
		}
		builder.append("}");
		
		builder.append("}");
		
		return builder.toString();
	}
}
