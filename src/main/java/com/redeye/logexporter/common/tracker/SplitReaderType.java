package com.redeye.logexporter.common.tracker;

import com.jutools.filetracker.LineSplitReader;
import com.jutools.filetracker.SplitReader;
import com.redeye.logexporter.agentlog.tracker.AppAgentSplitReader;

/**
 * 파일 트랙커의 Reader 타입
 * 
 * @author jmsohn
 */
public enum SplitReaderType {
	
	LINE_READER {
		
		@Override
		public SplitReader create() throws Exception {
			return new LineSplitReader();
		}

		@Override
		public String defaultFormat() {
			return "%{log}";
		}
	},
	
	APPAGENT_READER {
		
		@Override
		public SplitReader create() throws Exception {
			return new AppAgentSplitReader();
		}

		@Override
		public String defaultFormat() {
			return "%{log}";
		}
	};
	
	/**
	 * 파일 트랙커에서 사용할 Reader를 생성하여 반환
	 * 
	 * @return 생성된 Reader 객체
	 */
	public abstract SplitReader create() throws Exception;
	
	/**
	 * Reader 별 export용 디폴트 메시지 포맷 반환<br>
	 * 사용자 지정 포맷이 없는 경우 사용
	 * 
	 * @return 디폴트 메시지 포맷
	 */
	public abstract String defaultFormat();
}
