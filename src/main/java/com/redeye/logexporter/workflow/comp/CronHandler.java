package com.redeye.logexporter.workflow.comp;

import java.util.List;

import com.redeye.logexporter.workflow.Message;

/**
 * 데이터 수신 후 특정 주기로 데이터를 전송하는 인터페이스
 * 
 * @author jmsohn
 */
public interface CronHandler extends Component {

	/**
	 * 수집 주기 반환
	 *
	 * @return 수집 주기
	 */
	String getPeriod();
	
	/**
	 * 데이터 수신 부
	 *
	 * @param message 수집된 메시지 
	 */
	void accept(Message<?> message) throws Exception;
	
	/**
	 * 특정 주기에 데이터 전송
	 * 
	 * @return 전송할 데이터 목록
	 */
	List<Message<?>> flush() throws Exception;
}
