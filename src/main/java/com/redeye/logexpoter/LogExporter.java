package com.redeye.logexpoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.redeye.logexpoter.exporter.ExporterType;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @author jmsohn
 */
@Slf4j
@Component
public class LogExporter implements Runnable {
	
	/** 모니터링할 파일 tracker 목록 */
	private List<FileTracker> trackerList;
	
	/**
	 * 생성자
	 * 
	 * @param targetFileNames 로그 추적 대상 파일이름 목록
	 * @param exporterType exporter 타입
	 */
	public LogExporter(
		@Value("${app.monitor.files}") String targetFileNames,
		@Value("${app.exporter.type}") ExporterType exporterType
	) throws Exception {
		
		// 입력값 검증
		if(StringUtil.isBlank(targetFileNames) == true) {
			throw new Exception("LE_MONITOR_FILES is null or blank.");
		}
		
		// 로그 파일별 tracker 생성 및 저장
		this.trackerList = new ArrayList<>();
		for(String targetFileName: targetFileNames.split("[ \\t]*,[ \\t]*")) {
			
			FileTracker tracker = FileTracker.create(new File(targetFileName));
			this.trackerList.add(tracker);
		}
	}
	
	@Override
	public void run() {
		
		// 설정된 tracker 가 없으면 반환
		if(this.trackerList == null || this.trackerList.size() == 0) {
			return;
		}
		
		//
		List<Thread> threadList = new ArrayList<>();
		
		for(FileTracker tracker : this.trackerList) {
			
			// tracker 변수가 Thread 객체 생성시 문제를 일으킬 수 있기 때문에 final 변수로 참조하도록 변경
			final FileTracker currentTracker = tracker;
			
			// tracker 스레드 생성
			Thread trackingThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						
						// 파일 트랙킹 수행
						currentTracker.tracking(log -> {
							System.out.println("### DEBUG:" + currentTracker.getPath() +" ###");
							System.out.println(log);
						});
						
					} catch(Exception ex) {
						log.error("file tracking error:" + currentTracker.getPath(), ex);
					}
				}
			});
			
			// tracker 스레드 설정 및 시작
			trackingThread.start();
		}

		// 스레드 종료까지 대기
		for(Thread thread : threadList) {
			thread.join();
		}
	}
	
	/**
	 * 현재 수행 중인 작업들을 모두 종료 시킴
	 */
	public void stop() {
		
		// 설정된 tracker 가 없으면 반환
		if(this.trackerList == null || this.trackerList.size() == 0) {
			return;
		}
		
		//
		for(FileTracker tracker : this.trackerList) {
			tracker.setStop(true);
		}
	}
}
