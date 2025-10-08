package com.redeye.logexporter.common.tracker;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.jutools.filetracker.LineSplitReader;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그 파일 트랙커 클래스
 * 
 * @author jmsohn
 */
@Activity("logTracker")
@ConditionalOnProperty(
	name="log.type",
	havingValue="common"
)
@Slf4j
public class LogTracker {
	
	/** 트래킹 로그 파일 */
	@Value("${log.file}")
	private File logFile;
	
	/** 로그 트랙커 객체 */
	private FileTracker tracker;
	
	/** 로그 데이터 저장 큐 */
	private BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

	
	/**
	 * 초기화
	 */
	@Init
	public void init() throws Exception {
		
		// 트랙커 객체 생성
		this.tracker = FileTracker.create(logFile, new LineSplitReader());
		
		// 트랙킹 스레드 생성 및 실행
		// 트랙커에서 수집된 로그를 로그 큐에 저장
		Thread trackerThread = new Thread(() -> {
			
			try {
				
				this.tracker.tracking(log -> {
					try {
						logQueue.put(log);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
				
			} catch(Exception ex) {
				log.error("tracker: " + logFile.getAbsolutePath(), ex);
			}
		});
		
		trackerThread.start();
	}
	
	/**
	 * 로그 파일 트래킹 수행
	 * 
	 * @return
	 */
	@Process
	public Message<?> traking() throws Exception {
		
		// 수집된 로그 데이터 획득
		String log = this.logQueue.poll(1000, TimeUnit.SECONDS);
		
		// 메시지 생성 및 반환
		Message<String> logMessage = new Message<>();
		logMessage.setTopic("common log");
		logMessage.setBody(log);
		
		return logMessage;
	}
}
