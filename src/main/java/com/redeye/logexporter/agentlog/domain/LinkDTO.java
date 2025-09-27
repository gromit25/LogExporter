package com.redeye.logexporter.agentlog.domain;

import lombok.Data;

/**
 * 조인 포인트 관계 객체
 * 
 * @author jmsohn
 */
@Data
public class LinkDTO {
	
	/** 기준 조인 포인트 */
	private JoinPointDTO base;
	
	/** 다음 조인 포인트 */
	private JoinPointDTO next;
}
