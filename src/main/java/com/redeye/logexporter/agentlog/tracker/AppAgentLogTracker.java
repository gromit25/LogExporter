package com.redeye.logexporter.agentlog.tracker;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Init;
import com.jutools.workflow.annotation.Proc;

import lombok.extern.slf4j.Slf4j;

/**
 * 앱 메이전트 로그 트랙커 클래스<br>
 * 설정 값<br>
 * <li>app.appagent.tracker.use: 'y' 일 경우 활성화</li>
 * <li>app.appagent.tracker.file: 로그 파일 명</li>
 * 
 * @author jmsohn
 */
@Activity(value="logTracker")
@ConditionalOnProperty(
	name="app.appagent.tracker.use",
	havingValue="y"
)
@Slf4j
public class AppAgentLogTracker {
	
	/** 트래킹 앱 에이전트 로그 파일 */
	@Value("${app.appagent.tracker.file}")
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
		this.tracker = FileTracker.create(logFile, new AppAgentSplitReader());
		
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
	 * @return 수집된 로그 메시지
	 */
	@Proc
	public Message<?> traking() throws Exception {
		
		// 수집된 로그 데이터 획득
		String log = this.logQueue.poll(1000, TimeUnit.SECONDS);
		
		// 메시지 생성 및 반환
		Message<String> logMessage = new Message<>();
		logMessage.setTopic("app agent log:");
		logMessage.setBody(log);
		
		return logMessage;
	}
}
