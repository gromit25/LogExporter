package com.redeye.logexpoter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 
 * 
 * @author jmsohn
 */
@SpringBootApplication
public class LogExporterApplication implements CommandLineRunner {
	
	/** */
	@Autowired
	private LogExporter logExporter;
	
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
		
		System.out.println("DEBUG 100");

		// log tracking 시작
		Thread thread = new Thread(this.logExporter);
		thread.start();

		// 인터럽트가 발생할 떄까지 대기
		LockSupport.park();
		
		System.out.println("DEBUG 200");
	}
}
