package com.redeye.logexporter.agentlog.tracker;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

/**
 * 
 * 
 * @author jmsohn
 */
@Activity(value="logTracker")
@ConditionalOnProperty(
	name="log.type",
	havingValue="appagent"
)
public class AppAgentLogTracker {
	
	/** */
	@Value("${log.file}")
	private File logFile;
	
	/** */
	private FileTracker tracker;

	/**
	 * 
	 */
	@Init
	public void init() throws Exception {
		this.tracker = FileTracker.create(logFile, new AppAgentSplitReader());
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	@Process
	public Message<?> traking() throws Exception {
		
		final Message<String> logMessage = new Message<>();
		logMessage.setSubject("app agent log");
		
		this.tracker.tracking(log -> {
			logMessage.setBody(log);
		});
		
		return logMessage;
	}
}
