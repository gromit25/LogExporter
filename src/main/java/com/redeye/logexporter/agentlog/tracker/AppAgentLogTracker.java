package com.redeye.logexporter.agentlog.tracker;

import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.jutools.SysUtil;
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
@Activity(value="tracker")
@ConditionalOnProperty(
	name="app.appagent.tracker.use",
	havingValue="y"
)
@Slf4j
public class AppAgentLogTracker {
	
	/** 스택 정보 파싱용 정규표현식 객체 */
	private static Pattern locationP = Pattern.compile("(?<class>[^.]+(\\.[^.]+)*)\\.(?<method>[^.]+)\\:(?<loc>\\-?[0-9]+)");

	/** 기관 명 */
	@Value("${app.config.organ}")
	private String organCode;
	
	/** 도메인 명 */
	@Value("${app.config.domain}")
	private String domainCode;
	
	/** 호스트 명 */
	@Value("${app.config.hostname}")
	private String hostname;
	
	/** 트래킹 앱 에이전트 로그 파일 */
	@Value("${app.appagent.tracker.file}")
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
		
		// 호스트 명 설정
		// 설정된 호스트명이 없는 경우 시스템의 호스트명을 설정함
		if(StringUtil.isBlank(this.hostname) == true) {
			this.hostname = SysUtil.getHostname();
		}
		
		// 메시지 제목 설정
		this.topic = String.format(
			"%s:%s:%s:%s",
			this.organCode,
			this.domainCode,
			this.hostname,
			this.logFile.getAbsolutePath()
		);
		
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
		String logText = this.logQueue.poll(10, TimeUnit.SECONDS);
		
		// 메시지 생성 및 반환
		Message<Map<String, Object>> logMessage = new Message<>();
		logMessage.setTopic(this.topic);
		logMessage.setBody(parse(logText));
		
		return logMessage;
	}
	
	/**
	 * 로그를 분석하여 맵 형태로 만들어 추가
	 * 
	 * @param logText 로그 메시지
	 * @return 로그 맵
	 */
	private static Map<String, Object> parse(String logText) throws Exception {
		
		// 로그 맵 변수 생성 및 설정
		Map<String, Object> logMap = new ConcurrentHashMap<>();
		logMap.put("log", logText);
		
		String[] timestampSplit = split(logText);
		logMap.put("timestamp", Long.parseLong(timestampSplit[0]));
		
		String[] elapsedSplit = split(timestampSplit[1]);
		logMap.put("elapsed", Long.parseLong(elapsedSplit[0]));
		
		String[] pidSplit = split(elapsedSplit[1]);
		logMap.put("pid", Integer.parseInt(pidSplit[0]));
		
		String[] txIdSplit = split(pidSplit[1]);
		logMap.put("txId", txIdSplit[0]);
		
		String[] apiTypeSplit = split(txIdSplit[1]);
		logMap.put("apiType", apiTypeSplit[0]);

		String[] objIdSplit = split(apiTypeSplit[1]);
		logMap.put("objId", objIdSplit[0]);
		
		String[] stackTraceSplit = split(objIdSplit[1]);
		logMap.put("stackTrace", stackTraceSplit[0]);
		
		logMap.put("message", objIdSplit[1]);
		
		// key를 만들어 넣음
		logMap.put(
			"key",
			makeKey(
				logMap.get("apiType").toString(),
				logMap.get("stackTrace").toString()
			)
		);
		
		return logMap;
	}
	
	/**
	 * 로그 메시지 분해<br>
	 * 처음 나타나는 구분자를 기준으로 2개의 문자열로 분해함
	 * 
	 * @param logText 로그 매시지
	 * @return 분해된 로그 매시지
	 */
	private static String[] split(String logText) throws Exception {
		
		String[] splitText = StringUtil.splitFirst(logText, "\t");
		if(splitText.length != 2) {
			throw new Exception("Insufficient number of fields.");
		}
		
		return splitText;
	}
	
	/**
	 * 로그 메시지의 키를 만듦
	 * 
	 * @param apiType API 타입
	 * @param stackTrace 스택 트레이스 문자열
	 * @return 로그 메시지 키
	 */
	private static String makeKey(String apiType, String stackTrace) throws Exception {
		
		// 로그 발생 위치 변수
		String location = stackTrace;
		
		// 가장 뒤에 있는 스택을 가져옴 -> 메소드 호출 위치   
		String[] stackSplit = StringUtil.splitFirst(stackTrace, ">");
		if(stackSplit.length == 2) {
			location = stackSplit[1];
		}
		
		// 클래스, 메소드, LOC(Line Of Code) 추출
		Matcher locationM = locationP.matcher(location);
    	if(locationM.matches() == false) {
    		throw new Exception("location format is not matched: " + location);
    	}
    	
		String className = locationM.group("class");
		String methodName = locationM.group("method");
		String loc = locationM.group("loc");
		
		// 타입:클래스:메소드:LOC
		return String.format(
			"%s:%s:%s:%s",
			apiType,
			className,
			methodName,
			loc
		);
	}
}
