package com.redeye.logexpoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jutools.FileTracker;
import com.jutools.StringUtil;
import com.redeye.logexpoter.exporter.ExporterType;

/**
 * 
 * 
 * @author jmsohn
 */
@Component
public class LogExporter {
	
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
		
		// 로그 파일별 traker 생성 및 실행
		this.trackerList = new ArrayList<>();
		for(String targetFileName: targetFileNames.split("[ \\t]*,[ \\t]*")) {
			
			FileTracker tracker = FileTracker.create(new File(targetFileName));
			tracker.tracking(log -> {
				System.out.println("### DEBUG ###");
				System.out.println(log);
			});
			
			this.trackerList.add(tracker);
		}
	}
}
