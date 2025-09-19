package com.redeye.logexporter;

/**
 * 공통 상수 정의
 * 
 * @author jmsohn
 */
public class Constants {

	/** 시간 검사용 패턴 문자열 */
	public static final String TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	/** 기관 코드 */
	public static final String ORGAN_CODE = "oraganCode";

	/** 기관 코드 패턴 문자열 */
	public static final String ORGAN_CODE_PATTERN = "[a-zA-Z0-9_\\-]+";
	
	/** 도메인 코드 */
	public static final String DOMAIN_CODE = "domainCode";

	/** 도메인 코드 패턴 문자열 */
	public static final String DOMAIN_CODE_PATTERN = "[a-zA-Z0-9_\\-]+";
	
	/** 호스트 명 */
	public static final String HOST_NAME = "hostName";

	/** 호스트 명 패턴 문자열 */
	public static final String HOST_NAME_PATTERN = "[a-zA-Z0-9_\\-]+";

	/** 어플리케이션 코드 패턴 문자열 */
	public static final String APP_CODE_PATTERN = "[a-zA-Z0-9_\\-]+";
}
