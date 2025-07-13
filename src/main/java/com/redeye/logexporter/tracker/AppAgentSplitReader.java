package com.redeye.logexporter.tracker;

import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

import com.jutools.BytesUtil;
import com.jutools.filetracker.SplitReader;

/**
 * AppAgent에서 출력되는 로그 파일의 메시지를 분할해서 읽는 Reader
 * 
 * @author jmsohn
 */
public class AppAgentSplitReader implements SplitReader {
	
	/** Record Separator 의 바이트 배열 */
	private static final byte[] RECORD_SEPARATOR = "\u001E\r\n".getBytes();

	/** 파일을 읽을 때, 문자 인코딩 방식 */
	private Charset charset;
	
	/** 끝나지 않은 데이터 임시 저장 변수 */
	byte[] temp = null;
	
	/**
	 * 생성자
	 * 
	 * @param charset character set
	 */
	public AppAgentSplitReader(Charset charset) throws Exception {
		
		if(charset != null) {
			this.charset = charset;
		} else {
			this.charset = Charset.defaultCharset();
		}
	}
	
	/**
	 * 생성자
	 */
	public AppAgentSplitReader() throws Exception {
		this(null);
	}

	@Override
	public synchronized void read(byte[] buffer, Consumer<String> action) throws Exception {
		
		// 데이터 끝에 Record Separator 가 있는지 확인
		boolean isEndsWithRecordSeparator = BytesUtil.endsWith(buffer, RECORD_SEPARATOR);

		// lineSeparator로 데이터를 한 문장씩 split함
		List<byte[]> messages = BytesUtil.split(buffer, RECORD_SEPARATOR);
		for(int index = 0; index < messages.size(); index++) {

			byte[] message = messages.get(index);

			// 임시 저장된 데이터가 있을 경우
			// 데이터 합친 뒤 임시 저장 초기화
			if(index == 0 && this.temp != null) {
				
				message = BytesUtil.concat(this.temp, message);
				this.temp = null;
			}

			// Record Separator 를 통해 잘린 데이터는
			// 날짜 포맷 후 logMessageArr 에 추가
			if(index != messages.size() - 1 || isEndsWithRecordSeparator == true) {
				
				// 사용자 처리 메소드에서 데이터를 처리함
				action.accept(new String(message, this.charset));
				
			} else {
				
				// 데이터가 끝나지 않았을 경우 임시 저장
				this.temp = message;
			}
		}
	}
}
