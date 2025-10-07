package com.redeye.logexporter.common.tracker;

import java.io.File;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.jutools.filetracker.LineSplitReader;
import com.jutools.filetracker.SplitReader;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

@Activity("logTracker")
@ConditionalOnProperty(
	name="log.type",
	havingValue="common"
)
public class LogTracker {
	
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
		this.tracker = FileTracker.create(logFile, new LineSplitReader());
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	@Process
	public Message<?> traking() throws Exception {
		
		final Message<String> logMessage = new Message<>();
		logMessage.setSubject("common log");
		
		this.tracker.tracking(log -> {
			System.out.println("### DEBUG 100");
			logMessage.setBody(log);
		});
		
		System.out.println("### DEBUG 200");
		
		return logMessage;
	}
}
