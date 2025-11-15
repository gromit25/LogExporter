package com.redeye.logexporter.agentlog.handler;

import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.FileUtil;
import com.jutools.publish.Publisher;
import com.jutools.publish.PublisherFactory;
import com.jutools.publish.PublisherType;
import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Cron;
import com.jutools.spring.workflow.annotation.CronInit;
import com.jutools.spring.workflow.annotation.Proc;
import com.redeye.logexporter.ExporterContext;
import com.redeye.logexporter.agentlog.domain.TraceDTO;

/**
 * 호출 통계 핸들러 클래스
 * 
 * @author jmsohn
 */
@Activity(
	value="appstat",
	from="${app.appstat.from}"
)
@ConditionalOnProperty(
	name="app.appstat.use",
	havingValue="y"
)
public class InvokeStatHandler {
	
	
	/** 포맷 파일 명 */
	private static final String FORMAT_FILE = "format/restapi/json_format.xml";
	
	/** API 호출 Subpath */
	private static final String API_SUBPATH = "/api/agentlog/%s/%s/%s";
	
	@Autowired
	private ExporterContext context;
	
	/** API 메시지 생성용 publisher */
	private Publisher publisher;
	
	/** 앱 호출 트레이스 객체 - 통계 정보도 포함됨 */
	private TraceDTO appTrace;

	
	/**
	 * 수신된 데이터로 통계 데이터 업데이트
	 * 
	 * @param message
	 */
	@Proc
	public void record(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
		
		this.appTrace.add(messageMap);
	}

	/**
	 * 크론 초기화
	 *
	 * @param nextTime 다음 수행 시간
	 */
	@CronInit(method="send")
	public void init(long nextTime) throws Exception {
		
	    // API 메시지 생성용 publisher 객체 생성
		InputStream formatInputStream = FileUtil.getInputStream(FORMAT_FILE);
		this.publisher = PublisherFactory.create(PublisherType.TEXT_FILE, formatInputStream);

		//
		this.appTrace = new TraceDTO(System.currentTimeMillis(), nextTime);
	}
	
	/**
	 * 크론 수행
	 * 
	 * @param startTime 실행 기준 시간
	 * @param endTime 다음 수행 시간
	 * @return 전송할 메시지
	 */
	@Cron(period="${app.appstat.period}")
	public Message<?> send(long baseTime, long nextTime) throws Exception {

		TraceDTO appTraceToSend = null;

		// 앱 트레이스 정보 객체 신규 생성 및 교체
		synchronized(this) {
			appTraceToSend = this.appTrace;
			this.appTrace = new TraceDTO(baseTime, nextTime);
		}
		
		// 앱 트레이스 정보 메시지 생성 및 전송
		Message<TraceDTO> message = new Message<>();
		message.setBody(appTraceToSend);
		
		return message;
	}
}
