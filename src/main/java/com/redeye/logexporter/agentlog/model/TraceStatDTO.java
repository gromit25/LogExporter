package com.redeye.logexporter.agentlog.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jutools.StringUtil;
import com.jutools.TypeUtil;

import lombok.Data;

/**
 * 조인 포인트 트레이스 DTO 객체
 * 
 * @author jmsohn
 */
@Data
public class TraceStatDTO {
	
	
	/** 기관 코드 */
	private String organCode;
	
	/** 도메인 코드 */
	private String domainCode;
	
	/** 호스트 명 */
	private String hostname;
	
	/** 어플리케이션 코드 */
	private String appCode;

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
	 * @param organCode 기관 코드
	 * @param domainCode 도메인 코드
	 * @param hostname 호스트 명
	 * @param appCode 어플리케이션 코드
	 * @param startTime 통계 수집 시간
	 * @param endTime 통계 종료 시간
	 */
	public TraceStatDTO(
		String organCode,
		String domainCode,
		String hostname,
		String appCode,
		long startTime,
		long endTime
	) throws Exception {

		// 입력 값 검증
		if(StringUtil.isBlank(organCode) == true) {
			throw new IllegalArgumentException("'organCode' is null or blank.");
		}
		
		if(StringUtil.isBlank(domainCode) == true) {
			throw new IllegalArgumentException("'domainCode' is null or blank.");
		}
		
		if(StringUtil.isBlank(hostname) == true) {
			throw new IllegalArgumentException("'hostname' is null or blank.");
		}
		
		if(StringUtil.isBlank(appCode) == true) {
			throw new IllegalArgumentException("'appCode' is null or blank.");
		}
		
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
		this.organCode = organCode;
		this.domainCode = domainCode;
		this.hostname = hostname;
		this.appCode = appCode;
		
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
		long timestamp = TypeUtil.toLong(messageMap.get("ts"));
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
