package com.redeye.logexporter.agentlog.handler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.workflow.annotation.Activity;

/**
 * 
 * 
 * @author jmsohn
 */
@Activity(
	value="",
	from=""
)
@ConditionalOnProperty(
	name="app.appagent.tracker.use",
	havingValue="y"
)
public class InvokeStatHandler {
  
}
