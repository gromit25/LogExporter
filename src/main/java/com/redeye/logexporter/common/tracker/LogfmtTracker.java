package com.redeye.logexporter.common.tracker;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.filetracker.FileTracker;
import com.jutools.filetracker.trimmer.LogfmtTrimmer;
import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Proc;
import com.redeye.logexporter.ExporterContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 앱 메이전트 로그 트랙커 클래스<br>
 * 설정 값<br>
 * <li>app.apptracker.use: 'y' 일 경우 활성화</li>
 * <li>app.apptracker.file: 로그 파일 명</li>
 * 
 * @author jmsohn
 */
@Activity(value="apptracker")
@ConditionalOnProperty(
	name="app.apptracker.use",
	havingValue="y"
)
@RequiredArgsConstructor
@Slf4j
public class LogfmtTracker {
	
	
	/**로그 익스포터의 컨텍스트 */
	private final ExporterContext context;
	
	/** 트래킹 앱 에이전트 로그 파일 */
	@Value("${app.apptracker.file}")
	private File logFile;
	
	/** 메시지 제목 */
	private String topic;
	
	/** 로그 트랙커 객체 */
	private FileTracker<Map<String, Object>> tracker;
	
	/** 로그 데이터 저장 큐 */
	private BlockingQueue<Map<String, Object>> logQueue = new LinkedBlockingQueue<>();
	
	
	/**
	 * 초기화 - traking 메소드의 Proc 어노테이션에 설정되어 있음
	 */
	public void init() throws Exception {
		
		// 메시지 제목 설정
		this.topic = String.format(
			"%s:%s:%s:%s",
			this.context.getOrganCode(),
			this.context.getDomainCode(),
			this.context.getHostname(),
			this.logFile.getAbsolutePath()
		);
		
		// 트랙커 객체 생성
		this.tracker = FileTracker.create(logFile, new LogfmtTrimmer());
		
		// 트랙킹 스레드 생성 및 실행
		// 트랙커에서 수집된 로그를 로그 큐에 저장
		Thread trackerThread = new Thread(() -> {
			
			try {
				
				this.tracker.tracking(logMap -> {
					try {
						logQueue.put(logMap);
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
	 * @return 수집된 로그 메시지
	 */
	@Proc(init="init")
	public Message<?> traking() throws Exception {
		
		// 수집된 로그 데이터 획득
		Map<String, Object> logMap = this.logQueue.poll(10, TimeUnit.SECONDS);
		
		// 메시지 생성 및 반환
		Message<Map<String, Object>> logMessage = new Message<>();
		logMessage.setTopic(this.topic);
		logMessage.setBody(logMap);
		
		return logMessage;
	}
}
