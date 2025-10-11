package com.redeye.logexporter.agentlog.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Cron;
import com.jutools.workflow.annotation.Proc;
import com.redeye.logexporter.agentlog.domain.TraceDTO;

/**
 * 
 * 
 * 
 * @author jmsohn
 */
@Activity(
	value="stat",
	from="${app.appagent.stat.from}"
)
@ConditionalOnProperty(
	name="app.appagent.stat.use",
	havingValue="y"
)
public class InvokeStatHandler {
	
	/** */
	private TraceDTO traceInfo = new TraceDTO();
	
	
	/**
	 * 
	 * 
	 * @param message
	 */
	@Proc
	public void record(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
		
		this.traceInfo.add(messageMap);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	@Cron(period="${app.appagent.stat.period}")
	public Message<?> send() throws Exception {
		return null;
	}
}
