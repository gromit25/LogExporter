package com.redeye.logexporter.workflow.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.jutools.StringUtil;
import com.jutools.thread.AbstractDaemon;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.comp.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Runner 추상 클래스
 * 
 * @author jmsohn
 */
@Slf4j
public abstract class AbstractRunner {
	
	/** 컴포넌트 객체 명 - 스프링부트에 등록된 이름 */
	@Getter
	private final String name;
	
	/** 컴포넌트 객체 */
	@Getter
	private final Component component;
	
	/** 스레드 중단 여부 */
	@Getter
	private volatile boolean stop = true;
	
	/** 스레드 개수 */
	@Getter
	private int threadCount = 1;
	
	/** 스레드 목록 */
	private AbstractDaemon[] threadAry;
	
	/** 구독 제목 패턴 */
	private StringUtil.WildcardPattern subscribeSubjectPattern;
	
	/** 입력 큐의 타입 아웃 */
	private long timeout;
	
	/** 입력 큐 대기 최대치 */
	private int maxLag;
	
	/** 입력 큐(Collector의 경우 null) */
	private BlockingQueue<Message<?>> fromQueue;
	
	/** 구독 컴포넌트(Handler의 경우 null) */
	private List<AbstractRunner> subscriberList;
	
	/** 알림 메시지(예외, 상태 변경 등) 구독 컴포넌트 */
	private List<AbstractRunner> noticeSubscriberList;
	

	/**
	 * 각 Runner 프로세스
	 */
	protected abstract void processData() throws Exception;
	
	/**
	 * 생성자
	 *
	 * @param name 컴포넌트 명
	 * @param component 워크플로우 컴포넌트
	 */
	public AbstractRunner(String name, Component component) {
		this.name = name;
		this.component = component;
	}
	
	/**
	 * 컴포넌트 실행
	 */
	public synchronized void run() throws Exception {

		// 이미 수행 중 이면 반환
		if(this.stop == false) {
			return;
		}
		
		// 스레드 생성 및 목록에 추가
		this.threadAry = new AbstractDaemon[this.threadCount];
		for(int index = 0; index < this.threadCount; index++) {
			
			this.threadAry[index] = new AbstractDaemon() {
				
				@Override
				protected void process() throws InterruptedException {
					
					try {
						processData();
					} catch(InterruptedException iex) {
						throw iex;
					} catch(Exception ex) {
						log.error(getName(), ex);
						putNotice(ex);
					}
				}
				
				@Override
				protected void exit() {
					
					try {
						component.exit();
					} catch (Exception ex) {
						log.error(getName(), ex);
						putNotice(ex);
					}
				}
			};
		}

		// 스레드 시작 전 초기화 메소드 호출
		this.component.init();
		
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
	public synchronized void stop() {
		
		// 입력 큐 클리어
		this.fromQueue.clear();
		
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
	 * 메시지 목록의 메시지를 전송
	 * 
	 * @param messageList 메시지 목록
	 */
	protected void put(List<Message<?>> messageList) throws Exception {
		
		// 메시지가 null 이거나 없을 경우 반환
		if(messageList == null || messageList.size() == 0) {
			return;
		}
		
		// 각 메시지를 순서대로 전송
		for(Message<?> message: messageList) {
			this.put(message);
		}
	}
	
	/**
	 * 수신 컴포넌트에 메시지 전송
	 * 
	 * @param message 전송할 메시지
	 */
	protected void put(Message<?> message) throws Exception {
		
		// 메시지 전송
		Map<String, Integer> failMap = this.put(this.subscriberList, message);
		
		// lag 초과로 인한 메시지 전송 실패가 있는 경우 알림 메시지 발송
		if(failMap.size() != 0) {
			
			Message<String> notice = new Message<>();
			notice.setSubject("lag is exceeded.");
			notice.setBody(failMap.toString());
			
			this.putNotice(notice);
		}
	}
	
	/**
	 * 알림 메시지 수신 컴포넌트에 메시지 전송
	 * 
	 * @param notice 전송할 알림 메시지
	 */
	protected void putNotice(Message<?> notice) {
		
		try {
			this.put(this.noticeSubscriberList, notice);
		} catch(Exception ex) {
			log.error("notice failed in " + this.getName(), ex);
		}
	}
	
	/**
	 * 예외 알림 발송
	 * 
	 * @param ex 발생한 예외 객체
	 */
	protected void putNotice(Exception ex) {
		
		Message<Exception> notice = new Message<>();
		notice.setSubject("an exception is raised at " + this.getName());
		notice.setBody(ex);
		
		this.putNotice(notice);
	}
	
	/**
	 * 수신 컴포넌트에 메시지 전송
	 * 
	 * @param subscriberList 구독 컴포넌트 목록
	 * @param message 전송할 메시지
	 * @return lag 초과로 인한 발송 실패 맵 (key: 컴포넌트 명, value: 현재 큐 사이즈)
	 */
	private Map<String, Integer> put(List<AbstractRunner> subscriberList, Message<?> message) throws Exception {
		
		// 메시지 발송 실패한 구독 컴포넌트 목록
		Map<String, Integer> failMap = new HashMap<>();
		
		// 보낼 메시지가 없으면 반환
		if(message == null) {
			return failMap;
		}
		
		// 수신자가 설정되어 있지 않은 경우 반환
		if(subscriberList == null || subscriberList.size() == 0) {
			return failMap;
		}
		
		// 각 수신자에게 메시지 전송
		for(AbstractRunner subscriber: subscriberList) {
			if(isSubscribe(subscriber, message) == true) {
				
				// 큐의 크기가 maxLag 보다 적으면 put 하고
				// 아닐 경우 발송 실패 목록에 추가함
				if(subscriber.fromQueue.size() < this.maxLag) {
					subscriber.fromQueue.put(message);
				} else {
					failMap.put(subscriber.getName(), subscriber.fromQueue.size());
				}
			}
		}
		
		return failMap;
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
				subscriber.subscribeSubjectPattern == null 
				|| subscriber.subscribeSubjectPattern.match(message.getSubject()).isMatch()
				)
			;
	}
	
	/**
	 * 구독 제목 설정
	 * 
	 * @param subscribeSubject 구독 제목
	 */
	public void setSubscribeSubject(String subscribeSubject) throws Exception {
		this.subscribeSubjectPattern = StringUtil.WildcardPattern.create(subscribeSubject);
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
	 * 알림 구독 컴포넌트 추가
	 * 
	 * @param subscriber 알림 구독 컴포넌트
	 */
	public synchronized void addNoticeSubscriber(AbstractRunner subscriber) throws Exception {
		
		if(this.noticeSubscriberList == null) {
			// ArrayList는 동시성 문제가 있으나,
			// 구독 컴포넌트 목록은 처음 한번 만든 이후 변경이 없음
			this.noticeSubscriberList = new ArrayList<>();
		}
		
		this.noticeSubscriberList.add(subscriber);
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
	public <T> T getComponent(Class<T> clazz) {
		return clazz.cast(this.component);
	}
}
