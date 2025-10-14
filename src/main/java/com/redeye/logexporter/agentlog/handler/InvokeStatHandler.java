package com.redeye.logexporter.agentlog.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.jutools.workflow.Message;
import com.jutools.workflow.annotation.Activity;
import com.jutools.workflow.annotation.Cron;
import com.jutools.workflow.annotation.Proc;
import com.redeye.logexporter.agentlog.domain.TraceDTO;

/**
 * 호출 통계 핸들러 클래스
 * 
 * @author jmsohn
 */
@Activity(
	value="appstat",
	from="${app.appagent.stat.from}"
)
@ConditionalOnProperty(
	name="app.appagent.stat.use",
	havingValue="y"
)
public class InvokeStatHandler {
	
	/** 앱 호출 트레이스 객체 - 통계 정보도 포함됨 */
	private TraceDTO appTrace;

	
	/**
	 * 크론 초기화
	 *
	 * @param nextTime 다음 수행 시간
	 */
	@CronInit
	public void init(long nextTime) throws Exception {
		this.appTrace = new TraceDTO(System.currentTimeMillis(), nextTime);
	}
	
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
	 * 크론 수행
	 * 
	 * @param startTime 실행 기준 시간
	 * @param endTime 다음 수행 시간
	 * @return 전송할 메시지
	 */
	@Cron(period="${app.appagent.stat.period}")
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
