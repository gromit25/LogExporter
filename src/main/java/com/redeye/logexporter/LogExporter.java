package com.redeye.logexporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.redeye.logexporter.common.handler.LogHandler;
import com.redeye.logexporter.common.tracker.SplitReaderType;
import com.redeye.logexporter.workflow.comp.Exporter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그 파일을 트랙킹하여 특정 로그만 exporter를 통해 외부 반출 및 저장 수행<br>
 * 워크플로우 : 파일 트렉커 -> 필터 -> 반출(exporter)<br>
 * 각 컴포넌트의 연결은 큐를 사용
 * 
 * @author jmsohn
 */
@Slf4j
public class LogExporter implements Runnable {

	/** 큐에서 데이터 취득시 대기 시간(1초) */
	private static long POLLING_PERIOD = 1000L;
	
	/** 모니터링할 파일 tracker 목록 */
	private List<FileTracker> trackerList;

	/** 로그 핸들러 객체 */
	@Autowired
	@Qualifier("handler")
	private LogHandler logHandler;

	/** 핸들러 스레드 객체 */
	private Thread handlerThread;

	/** 로그 반출 객체 */
	@Autowired
	@Qualifier("exporter")
	private Exporter exporter;

	/** 반출 스레드 객체 */
	private Thread exporterThread;
	
	/** tracker -> handler 큐 */
	private BlockingQueue<String> toHandlerQueue;

	/** filter -> exporter 큐 */
	private BlockingQueue<String> toExporterQueue;

	/** 중단 여부 */
	@Getter
	@Setter
	private volatile boolean stop;
	
	
	/**
	 * 생성자
	 * 
	 * @param targetFileNames 로그 추적 대상 파일이름 목록
	 */
	public LogExporter(
		@Value("${app.tracker.files}") String targetFileNames,
		@Value("${app.tracker.reader.type}") SplitReaderType readerType
	) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(targetFileNames) == true) {
			throw new IllegalArgumentException("monitor file list(LE_MONITOR_FILES) is null or blank.");
		}
		
		// 로그 파일별 tracker 생성 및 저장
		this.trackerList = new ArrayList<>();
		for(String targetFileName: targetFileNames.split("[ \\t]*,[ \\t]*")) {
			
			FileTracker tracker = FileTracker.create(
				new File(targetFileName),
				readerType.create()
			);
			
			this.trackerList.add(tracker);
		}

		// tracker -> handler 큐 생성
		this.toHandlerQueue = new LinkedBlockingQueue<>();
		
		// filter -> exporter 큐 생성
		this.toExporterQueue = new LinkedBlockingQueue<>();

		// 중단 여부 설정
		this.setStop(true);
	}
	
	@Override
	public void run() {

		// 중단 여부 설정
		this.setStop(false);
		
		// 설정된 tracker 가 없으면 반환
		if(this.trackerList == null || this.trackerList.size() == 0) {
			return;
		}

		// 후행 스레드가 미리 준비되도록
		// 실행 순서를 반출 -> 핸들러 -> 트렉커로 함
		
		// 로그 반출 스레드 시작
		this.startExporterComponent();

		// 필터링 스레드 시작
		this.startHandlerComponent();
		
		// 파일 트랙커 스레드 시작
		this.startTrackerComponent();
	}
	
	/**
	 * 파일 트랙커 스레드 생성 및 시작
	 */
	private void startTrackerComponent() {
		
		// 각 파일들의 트랙커 별로 스레드 생성 및 실행
		for(FileTracker tracker : this.trackerList) {
			
			// tracker 변수가 Thread 객체 생성시 문제를 일으킬 수 있기 때문에 final 변수로 참조하도록 변경
			final FileTracker currentTracker = tracker;
			
			// tracker 스레드 생성
			Thread trackingThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						
						// 파일 트랙킹 수행
						currentTracker.tracking(message -> {
							
							try {
								toHandlerQueue.put(message);
							} catch(Exception ex) {
								log.error("when put to filter.", ex);
							}
						});
						
					} catch(Exception ex) {
						log.error("file tracking error:" + currentTracker.getPath(), ex);
					}
				}
			}, "Tracker-Thread:" + currentTracker.getPath());

			// tracker 스레드 설정 및 시작
			trackingThread.start();
			
		} // End of for
	}
	
	/**
	 * 필터링 스레드 생성 및 시작
	 */
	private void startHandlerComponent() {
		
		this.handlerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(stop == false) {
					try {
						
						// 필터링 수행
						String message = toHandlerQueue.poll(POLLING_PERIOD, TimeUnit.MILLISECONDS);
						logHandler.handle(message, toExporterQueue);
						
					} catch(Exception ex) {
						log.error("filter error.", ex);
					}
				}
			}
		}, "Handler-Thread");
		
		handlerThread.start();
	}
	
	/**
	 * 로그 반출 스레드 생성 및 시작
	 */
	private void startExporterComponent() {
		
		this.exporterThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(stop == false) {
					try {

						// exporter를 통해 출력
						String message = toExporterQueue.poll(POLLING_PERIOD, TimeUnit.MILLISECONDS);
						//exporter.export(message);
						
					} catch(Exception ex) {
						log.error("exporter error.", ex);
					}
				}
			}
		}, "Exporter-Thread");

		exporterThread.start();
	}
	
	/**
	 * 현재 수행 중인 작업들을 모두 종료 시킴
	 */
	public void stop() throws Exception {

		// 파일 tracker 중단
		for(FileTracker tracker : this.trackerList) {
			tracker.setStop(true);
		}
		
		// 중단 여부 설정
		this.setStop(true);

		// 종료 대기
		if(this.handlerThread.isAlive() == true) {
			this.handlerThread.join();
		}
		
		if(this.exporterThread.isAlive() == true) {
			this.exporterThread.join();
		}
	}
}
