package com.redeye.logexpoter;

import java.util.concurrent.locks.LockSupport;

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

	/** */
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
		
		System.out.println("DEBUG 100");

		// log tracking 시작
		Thread thread = new Thread(this.logExporter);
		thread.start();

		// stop 파일이 생성될 때까지 대기
		waitForFileCreated(this.stopFile, 1000L);

		System.out.println("DEBUG 200");
	}

	/**
	 * stop 파일이 생성될 때까지 대기
	 *
	 * @param stopFile stop 파일
	 * @param pollingPeriod 폴링 시간
	 */
	private static void waitForFileCreated(File stopFile, long pollingPeriod) throws Exception {

		// 입력값 검증
		if(stopFile == null) {
			throw new IllegalArgumentException("stop file is null.");
		}

		if(pollingPeriod < 100L) {
			throw new IllegalArgumentException("polling period must be greater then 100: " + pollingPeriod);
		}
		
		// 파일 생성 이벤트 수신을 위한 Watch 서비스 생성 및 등록
		Path parentPath = stopFile.getPath().getParent();
		WatchService parentWatchService = parentPath.getFileSystem().newWatchService();
		parentPath.register(parentWatchService, StandardWatchEventKinds.ENTRY_CREATE);

		while(true) {
			
			// WatchKey에 이벤트 들어올 때 까지 대기
			WatchKey watchKey = watchService.poll(pollingPeriod, TimeUnit.MILLISECONDS);

			try {
				// 생성 이벤트 대기
				List<WatchEvent<?>> eventList = watchKey.pollEvents();
				for (WatchEvent<?> event : eventList) {
			
					WatchEvent.Kind<?> kind = event.kind();   // 이벤트 종류
					Path eventFile = (Path) event.context();  // 이벤트가 발생한 파일

					// 파일이 생성되면 대기 중지
					if(kind == StandardWatchEventKinds.ENTRY_CREATE
					  && eventFile.toFile().getName().equals(stopFile.getName()) == true) {
						return;
					}
				}
				
			} finally {
				// WatchKey 초기화 꼭 해줘야 함
				watchKey.reset();
			}
		} // End of while
	}
}
