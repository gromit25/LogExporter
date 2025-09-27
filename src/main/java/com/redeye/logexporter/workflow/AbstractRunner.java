package com.redeye.logexporter.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Value;

import com.jutools.StringUtil;
import com.jutools.thread.AbstractDaemon;
import com.redeye.logexporter.workflow.comp.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Runner 추상 클래스
 * 
 * @author jmsohn
 */
@Slf4j
public abstract class AbstractRunner {
	
	/** 컴포넌트 객체 */
	private Component component;
	
	/** 설정 정보 - application.properties */
	private Map<String, String> config;
	
	/** 스레드 중단 여부 */
	@Getter
	private volatile boolean stop = true;
	
	/** 스레드 개수 */
	@Getter
	private int threadCount = 1;
	
	/** 스레드 목록 */
	private AbstractDaemon[] threadAry;
	
	/** 구독 패턴 */
	private StringUtil.WildcardPattern subscribePattern;
	
	/** 입력 큐의 타입 아웃 */
	@Value("${workflow.timeout.sec}")
	private long timeout;
	
	/** 입력 큐(Collector의 경우 null) */
	private BlockingQueue<Message<?>> fromQueue;
	
	/** 구독 컴포넌트(Handler의 경우 null) */
	private List<AbstractRunner> subscriberList;
	
	
	/**
	 * 각 Runner 프로세스
	 */
	protected abstract void processData() throws InterruptedException;
	
	/**
	 * 컴포넌트 실행
	 */
	public synchronized void run() throws Exception {
		
		// 스레드 생성 및 목록에 추가
		this.threadAry = new AbstractDaemon[this.threadCount];
		for(int index = 0; index < this.threadCount; index++) {
			
			this.threadAry[index] = new AbstractDaemon() {
				
				@Override
				protected void process() throws InterruptedException {
					processData();
				}
				
				@Override
				protected void exit() {
					try {
						component.exit();
					} catch (Exception ex) {
						log.error(component.name(), ex);
					}
				}
			};
		}
		
		// 스레드 시작 전 컴포넌트 초기화
		this.component.init(this.config);
		
		// 각 스레드 시작
		for(AbstractDaemon t: this.threadAry) {
			t.run();
		}
		
		// 상태 수정
		this.stop = false;
	}
	
	/**
	 * 스레드 모두 중지
	 */
	public void stop() {
		
		// 스레드 중지
		for(AbstractDaemon t: this.threadAry) {
			t.stop();
		}
		
		// 상태 변경
		this.stop = true;
	}
	
	/**
	 * 입력 큐로 부터 메시지를 하나 가져옴
	 * 
	 * @return 입력 큐의 최신 메시지
	 */
	protected Message<?> poll() throws Exception {
		
		if(this.fromQueue == null) {
			throw new IllegalStateException("fromQueue is null.");
		}
		
		return this.fromQueue.poll(this.timeout, TimeUnit.SECONDS);
	}
	
	/**
	 * 수신 컴포넌트에 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	protected void put(Message<?> message) throws Exception {
		
		// 보낼 메시지가 없으면 반환
		if(message == null) {
			return;
		}
		
		// 수신자가 설정되어 있는지 확인
		if(this.subscriberList == null || this.subscriberList.size() == 0) {
			throw new IllegalStateException("subscribeList is null or zero.");
		}
		
		// 각 수신자에게 메시지 전송
		for(AbstractRunner subscriber: this.subscriberList) {
			if(isSubscribe(subscriber, message) == true) {
				subscriber.fromQueue.put(message);
			}
		}
	}
	
	/**
	 * 수신 컴포넌트의 메시지 구독 여부 반환
	 * 
	 * @param to 수신 컴포넌트 
	 * @param message 전송할 메시지
	 * @return 수신 컴포넌트의 구독 여부
	 */
	private static boolean isSubscribe(AbstractRunner subscriber, Message<?> message) throws Exception {
		return
			subscriber.fromQueue != null
			&& (
				subscriber.subscribePattern == null
				|| subscriber.subscribePattern.match(message.getSubject()).isMatch()
				)
			;
	}
	
	/**
	 * 구독 패턴 설정
	 * 
	 * @param subscribePatternStr 구독 패턴 문자열
	 */
	public void setSubscribePattern(String subscribePatternStr) throws Exception {
		this.subscribePattern = StringUtil.WildcardPattern.create(subscribePatternStr);
	}
	
	/**
	 * 구독 컴포넌트 추가
	 * 
	 * @param subscriber 구독 컴포넌트
	 */
	public synchronized void addSubscriber(AbstractRunner subscriber) throws Exception {
		
		if(this.subscriberList == null) {
			// ArrayList는 동시성 문제가 있으나,
			// 구독 컴포넌트 목록은 처음 한번 만든 이후 변경이 없음
			this.subscriberList = new ArrayList<>();
		}
		
		this.subscriberList.add(subscriber);
	}
	
	/**
	 * 실행할 스레드 개수 설정
	 * 
	 * @param threadCount 실행할 스레드 개수
	 */
	public synchronized void setThreadCount(int threadCount) throws Exception {
		
		if(threadCount < 1) {
			throw new IllegalArgumentException("threadCount must be greater than 0: " + threadCount);
		}
		
		this.threadCount = threadCount;
	}
	
	/**
	 * 컴포넌트 반환
	 * 
	 * @param clazz 컴포넌트 타입
	 * @return 컴포넌트
	 */
	public T getComponent(Class<T> clazz) {
		return clazz.cast(this.component);
	}
}
