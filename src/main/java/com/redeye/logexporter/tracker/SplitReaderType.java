package com.redeye.logexporter.tracker;

import com.jutools.filetracker.LineSplitReader;
import com.jutools.filetracker.SplitReader;

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
	},
	
	AGENT_READER {
		
		@Override
		public SplitReader create() throws Exception {
			return null;
		}
	};
	
	/**
	 * 파일 트랙커에서 사용할 Reader를 생성하여 반환
	 * 
	 * @return 생성된 Reader 객체
	 */
	public abstract SplitReader create() throws Exception;
}
