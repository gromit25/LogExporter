package com.redeye.logexporter.common.tracker;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.jutools.filetracker.LineSplitReader;
import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Init;
import com.jutools.spring.workflow.annotation.Proc;
import com.redeye.logexporter.ExporterContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그 파일 트랙커 클래스<br>
 * 설정 값<br>
 * <li>app.tracker.use: 'y' 일 경우 활성화</li>
 * <li>app.tracker.file: 로그 파일 명</li>
 * 
 * @author jmsohn
 */
@Activity("tracker")
@ConditionalOnProperty(
	name="app.tracker.use",
	havingValue="y"
)
@Slf4j
public class LogTracker {
	
	/** 필드 분리 문자 */
	private static final String DELIMITER = "[ \t]+";
	
	/**로그 익스포터의 컨텍스트 */
	@Autowired
	private ExporterContext context;
	
	/** 트래킹 로그 파일 */
	@Value("${app.tracker.file}")
	private File logFile;
	
	/** 메시지 제목 */
	private String topic;
	
	/** 로그 트랙커 객체 */
	private FileTracker tracker;
	
	/** 로그 데이터 저장 큐 */
	private BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

	
	/**
	 * 초기화
	 */
	@Init
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
	 * @return 수집된 로그 메시지
	 */
	@Proc
	public Message<?> traking() throws Exception {
		
		// 수집된 로그 데이터 획득
		String logText = this.logQueue.poll(10, TimeUnit.SECONDS);
		
		// 메시지 생성 및 반환
		Message<Map<String, Object>> logMessage = new Message<>();
		logMessage.setTopic(this.topic);
		logMessage.setBody(makeLogMap(logText));
		
		return logMessage;
	}
	
	/**
	 * 로그 메시지(logText)에서 로그 맵 객체를 생성 및 반환<br>
	 * <li>"log" -> 로그 메시지 전체</li>
	 * <li>"fields" -> 분할된 필드 목록 객체</li>
	 * 
	 * @param logText 로그 메시지
	 * @return 로그 맵
	 */
	private static Map<String, Object> makeLogMap(String logText) {
		
		Map<String, Object> logMap = new HashMap<>();
		
		// 로그 메시지 원본 추가
		logMap.put("log", logText);
		
		// 구분자(delimiter)에 따라 분리 후 logMap에 추가
		if(StringUtil.isBlank(logText) == false) {
			
			String[] fieldArray = null;
			if(StringUtil.isEmpty(logText) == true) {
				fieldArray = new String[] {logText};
			} else {
				fieldArray = logText.split(DELIMITER);
			}
			
			List<String> fieldList = Arrays.asList(fieldArray);
			logMap.put("fields", fieldList);
		}
		
		return logMap;
	}
}
