package com.redeye.logexporter.agentlog.handler;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.spring.workflow.Message;
import com.jutools.spring.workflow.annotation.Activity;
import com.jutools.spring.workflow.annotation.Cron;
import com.jutools.spring.workflow.annotation.Proc;
import com.redeye.logexporter.ExporterContext;
import com.redeye.logexporter.agentlog.model.TraceStatDTO;

import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class TraceStatHandler {
	
	
	/** 로그 익스포터의 컨텍스트 */
	private final ExporterContext context;
	
	/** 앱 트레이스 통계 객체 */
	private TraceStatDTO appTraceStatDTO;

	
	/**
	 * 수집된 메시지로 부터 통계 데이터 업데이트
	 * 
	 * @param message 수집 메시지
	 */
	@Proc
	public void record(Message<?> message) throws Exception {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> messageMap = (Map<String, Object>)message.getBody();
		
		this.appTraceStatDTO.add(messageMap);
	}

	/**
	 * 크론 초기화
	 *
	 * @param nextTime 다음 수행 시간
	 */
	public void initCron(long nextTime) throws Exception {
		
		// 앱 트래이스 통계 객체 초기화
		this.appTraceStatDTO = createTraceStatDTO(
			System.currentTimeMillis(),
			nextTime
		);
	}
	
	/**
	 * 크론 수행
	 * 
	 * @param startTime 실행 기준 시간
	 * @param endTime 다음 수행 시간
	 * @return 전송할 메시지
	 */
	@Cron(schedule="${app.appstat.schedule}", init="initCron")
	public Message<?> send(long baseTime, long nextTime) throws Exception {

		// 발송할 앱 트래이스 통계 정보 변수
		TraceStatDTO appTraceStatToSendDTO = null;

		// 앱 트레이스 통계 정보 객체 신규 생성 및 교체
		synchronized(this) {
			appTraceStatToSendDTO = this.appTraceStatDTO;
			this.appTraceStatDTO = createTraceStatDTO(baseTime, nextTime);
		}
		
		// 앱 트레이스 정보 메시지 생성 및 전송
		Message<TraceStatDTO> message = new Message<>();
		message.setBody(appTraceStatToSendDTO);
		
		// 전송 메시지 반환
		return message;
	}
	
	/**
	 * 앱 트레이스 통계 객체 생성 및 반환 
	 * 
	 * @param startTime 시작 시간
	 * @param endTime 종료 시간
	 * @return 생성된 앱 트레이스 통계 객체
	 */
	private TraceStatDTO createTraceStatDTO(long startTime, long endTime) throws Exception {
		
		return new TraceStatDTO(
				this.context.getOrganCode(),
				this.context.getDomainCode(),
				this.context.getHostname(),
				this.context.getAppCode(),
				startTime,
				endTime
			);
	}
}
