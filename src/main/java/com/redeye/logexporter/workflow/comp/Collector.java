package com.redeye.logexporter.workflow.comp;

/**
 * 처리할 데이터 수집 클래스
 * 
 * @author jmsohn
 */
public interface Collector extends Component {
	
	/**
	 * 데이터 수집 후 반환
	 * 
	 * @return 수집된 데이터
	 */
	String[] collect() throws Exception;
}
