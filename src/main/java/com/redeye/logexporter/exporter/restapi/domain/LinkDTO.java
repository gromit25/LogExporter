package com.redeye.logexporter.exporter.restapi.domain;

import lombok.Data;

/**
 * 
 * 
 * 
 */
@Data
public class LinkDTO {
	
	/** */
	private JoinPointDTO origin;
	
	/** */
	private JoinPointDTO next;
}
