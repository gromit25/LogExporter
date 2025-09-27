package com.redeye.logexporter.workflow.comp;

import java.util.List;

import com.redeye.logexporter.workflow.Message;

/**
 * 데이터 수집 인터페이스
 * 
 * @author jmsohn
 */
public interface Collector extends Component {
	
	/**
	 * 데이터 수집 후 반환
	 * 
	 * @return 수집된 데이터
	 */
	List<Message<?>> collect() throws Exception;
}
