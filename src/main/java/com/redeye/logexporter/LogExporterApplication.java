package com.redeye.logexporter;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.jutools.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 로그 출력기 어플리케이션
 * 
 * @author jmsohn
 */
@Slf4j
@SpringBootApplication
public class LogExporterApplication implements CommandLineRunner {
	
	/** 로그 출력기 */
	@Autowired
	private LogExporter logExporter;

	/** 중단 파일 */
	@Value("${app.stop.file}")
	private File stopFile;
	
	/**
	 * 메인 메소드
	 * 
	 * @param args 명령행 인자
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(LogExporterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		try {
			
			// log tracking 시작
			log.info("start log exporter.");
			Thread thread = new Thread(this.logExporter);
			thread.start();
	
			// stop 파일이 생성되거 업데이트 될때까지 대기
			FileUtil.waitForFileTouched(this.stopFile);
			
		} finally {

			// stop 파일 생성시 logExporter 중지
			log.info("stop log exporter.");
			this.logExporter.stop();
		}
	}
}
