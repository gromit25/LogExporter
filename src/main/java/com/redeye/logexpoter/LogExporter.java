package com.redeye.logexpoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.redeye.logexpoter.exporter.Exporter;
import com.redeye.logexpoter.exporter.ExporterType;
import com.redeye.logexpoter.filter.LogFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author jmsohn
 */
@Slf4j
@Component
public class LogExporter implements Runnable {

	/** 큐에서 데이터 취득시 대기 시간(1초) */
	private static long POLLING_PERIOD = 1000L;
	
	/** 모니터링할 파일 tracker 목록 */
	private List<FileTracker> trackerList;

	/** 로그 필터 객체 */
	@Autowired
	@Qualifier("logFilter")
	private LogFilter filter;

	/** 필터 스레드 객체 */
	private Thread filterThread;

	/** 로그 반출 객체 */
	@Autowired
	@Qualifier("exporter")
	private Exporter exporter;

	/** 반출 스레드 객체 */
	private Thread exporterThread;
	
	/** tracker -> filter 큐 */
	private BlockingQueue<String> toFilterQueue;

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
		@Value("${app.monitor.files}") String targetFileNames
	) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(targetFileNames) == true) {
			throw new IllegalArgumentException("monitor file list(LE_MONITOR_FILES) is null or blank.");
		}
		
		// 로그 파일별 tracker 생성 및 저장
		this.trackerList = new ArrayList<>();
		for(String targetFileName: targetFileNames.split("[ \\t]*,[ \\t]*")) {
			
			FileTracker tracker = FileTracker.create(new File(targetFileName));
			this.trackerList.add(tracker);
		}

		// tracker -> filter 큐 생성
		this.toFilterQueue = new LinkedBlockingQueue<>();
		
		// filter -> exporter 큐 생성
		this.toExporterQueue = new LinkedBlockingQueue<>();
	}
	
	@Override
	public void run() {

		// 중단 여부 설정
		this.setStop(false);
		
		// 설정된 tracker 가 없으면 반환
		if(this.trackerList == null || this.trackerList.size() == 0) {
			return;
		}
		
		// 파일 트랙킹 스레드 시작
		this.startTracking();
		
		// 필터링 스레드 시작
		this.startFiltering();
		
		// 로그 반출 스레드 시작
		this.startExporting();
	}
	
	/**
	 * 파일 트랙킹 스레드 생성 및 시작
	 */
	private void startTracking() {
		
		//
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
								toFilterQueue.put(message);
							} catch(Exception ex) {
								log.error("when put to filter.", ex);
							}
						});
						
					} catch(Exception ex) {
						log.error("file tracking error:" + currentTracker.getPath(), ex);
					}
				}
			});

			// tracker 스레드 설정 및 시작
			trackingThread.start();
		}
	}
	
	/**
	 * 필터링 스레드 생성 및 시작
	 */
	private void startFiltering() {
		
		this.filterThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(stop == false) {
					try {
						
						String message = toFilterQueue.poll(POLLING_PERIOD);
						if(message != null && filter.shouldBeExported(message) == true) {
							toExporterQueue.put(message);
						}
						
					} catch(Exception ex) {
						log.error("filter error.", ex);
					}
				}
			}
		});
		
		filterThread.start();
	}
	
	/**
	 * 로그 반출 스레드 생성 및 시작
	 */
	private void startExporting() {
		
		this.exporterThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(stop == false) {
					try {
						
						String message = toExporterQueue.poll(POLLING_PERIOD);
						if(message != null) {
							exporter.send(message);
						}
						
					} catch(Exception ex) {
						log.error("filter error.", ex);
					}
				}
			}
		});

		exporterThread.start();
	}
	
	/**
	 * 현재 수행 중인 작업들을 모두 종료 시킴
	 */
	public void stop() {

		// 파일 tracker 중단
		for(FileTracker tracker : this.trackerList) {
			tracker.setStop(true);
		}
		
		// 중단 여부 설정
		this.setStop(true);

		// 종료 대기
		this.filterThread.join();
		this.exporterThread.join();
	}
}
