package com.redeye.logexporter.workflow.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.jutools.CronJob;
import com.jutools.StringUtil;
import com.jutools.thread.AbstractDaemon;
import com.redeye.logexporter.workflow.Message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 액티비티 실행 클래스
 * 
 * @author jmsohn
 */
@Slf4j
public class ActivityRunner {
	
	/** 액티비티 객체 명 - 스프링부트에 등록된 컴포넌트 이름 */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private String name;
	
	/** 이전 액티비티 연결 타입 */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private LinkType linkType;
	
	/** 연결할 이전 액티비티 명 */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private String from;
	
	/** 액티비티 객체 */
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Object activity;
	
	/** 액티비티 객체의 초기화 메소드 */
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Method initMethod;
	
	/** 액티비티 객체의 데이터 처리 메소드 */
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private Method processMethod;
	
	/** 액티비티 객체의 동료시 호출 메소드 */
	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	private Method exitMethod;
	
	/** 스레드 중단 여부 */
	@Getter
	private volatile boolean stop = true;
	
	/** 스레드 목록 */
	private AbstractDaemon[] threadAry;
	
	/** 스레드 개수 */
	private int threadCount = 1;
	
	/** 구독 제목 패턴 */
	private StringUtil.WildcardPattern subscriptionTopic;
	
	/** 입력 큐 */
	@Setter(AccessLevel.PACKAGE)
	private BlockingQueue<Message<?>> fromQueue;
	
	/** 입력 큐 데이터 수신 타입 아웃 */
	private long timeout;
	
	/** 입력 큐 대기 최대치 */
	private int maxLag;
	
	/** 현재 액티비티의 구독 액티비티 */
	private List<ActivityRunner> subscriberList;
	
	/** 현재 액티비티의 알림 메시지(예외, 상태 변경 등) 구독 컴포넌트 */
	private List<ActivityRunner> noticeSubscriberList;
	
	/** 크론 잡 맵 */
	@Getter(AccessLevel.PACKAGE)
	private Map<String, CronJob> cronJobMap = new ConcurrentHashMap<>();

	
	/**
	 * 생성자 - RunnerFactory 에서만 생성
	 */
	ActivityRunner() {
	}
		
	/**
	 * 액티비티 실행
	 */
	public synchronized void run() throws Exception {

		// 이미 수행 중 이면 반환
		if(this.stop == false) {
			return;
		}
		
		// ------ 스레드 생성 및 목록에 추가
		this.threadAry = new AbstractDaemon[this.threadCount];
		for(int index = 0; index < this.threadCount; index++) {
			
			this.threadAry[index] = new AbstractDaemon() {
				
				@Override
				protected void process() throws InterruptedException {
					
					try {
						
						Object result = null;
						
						if(fromQueue != null) {
							
							Message<?> message = fromQueue.poll(timeout, TimeUnit.SECONDS);
							if(message == null) {
								return;
							}
							
							result = processMethod.invoke(activity, message);
							
						} else {
							
							result = processMethod.invoke(activity);
						}
						
						put(result);
						
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
						
						if(exitMethod != null) {
							exitMethod.invoke(activity);
						}
						
					} catch (Exception ex) {
						log.error(getName(), ex);
						putNotice(ex);
					}
				}
			};
		}

		// 스레드 시작 전 초기화 메소드 호출
		if(this.initMethod != null) {
			this.initMethod.invoke(activity);
		}
		
		// 각 스레드 시작
		for(AbstractDaemon t: this.threadAry) {
			t.run();
		}
		
		// -------- 크론 작업 시작 
		for(String key: this.cronJobMap.keySet()) {
			CronJob job = this.cronJobMap.get(key);
			job.run();
		}
		
		// 상태 수정
		this.stop = false;
	}
	
	/**
	 * 스레드 모두 중지
	 */
	public synchronized void stop() {
		
		// 큐 처리 스레드 중지
		for(AbstractDaemon t: this.threadAry) {
			t.stop();
		}
		
		// 크론잡 중지
		for(String key: this.cronJobMap.keySet()) {
			CronJob job = this.cronJobMap.get(key);
			job.stop();
		}
		
		// 상태 변경
		this.stop = true;
	}
	
	/**
	 * 메시지 목록의 메시지를 전송
	 * 
	 * @param messageList 메시지 목록
	 */
	@SuppressWarnings("unchecked")
	protected void put(Object result) throws Exception {
		
		// 입력값 검증
		if(result == null) {
			return;
		}
		
		// 리스트 형태가 아니면 리스트에 만들어 넣음
		List<Message<?>> messageList = null;
		if(result instanceof List == false) {
			messageList = new ArrayList<Message<?>>();
			messageList.add((Message<?>)result);
		} else {
			messageList = (List<Message<?>>)result;
		}
		
		// 메시지가 없을 경우 반환
		if(messageList.size() == 0) {
			return;
		}
		
		// 각 메시지를 순서대로 전송
		for(Message<?> message: messageList) {
			this.put(this.subscriberList, message);
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
		notice.setTopic("an exception is raised at " + this.getName());
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
	private Map<String, Integer> put(List<ActivityRunner> subscriberList, Message<?> message) throws Exception {
		
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
		for(ActivityRunner subscriber: subscriberList) {
			
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
	private static boolean isSubscribe(ActivityRunner subscriber, Message<?> message) throws Exception {
		
		return
			subscriber.fromQueue != null
			&& (
				subscriber.subscriptionTopic == null 
				|| subscriber.subscriptionTopic.match(message.getTopic()).isMatch()
			);
	}
	
	/**
	 * 구독 제목 설정
	 * 
	 * @param subscriptionTopic 구독 제목
	 */
	public void setSubscriptionTopic(String subscriptionTopic) throws Exception {
		
		// 구독 제목(subscriptionTopic) 이 없는 경우,
		// 모든 제목에 대해 구독하도록 this.subscriptionTopic을 null 로 설정
		if(StringUtil.isBlank(subscriptionTopic) == false) {
			this.subscriptionTopic = StringUtil.WildcardPattern.create(subscriptionTopic);
		} else {
			this.subscriptionTopic = null;
		}
	}
	
	/**
	 * 구독 컴포넌트 추가
	 * 
	 * @param subscriber 구독 컴포넌트
	 */
	public synchronized void addSubscriber(ActivityRunner subscriber) throws Exception {
		
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
	public synchronized void addNoticeSubscriber(ActivityRunner subscriber) throws Exception {
		
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
			throw new IllegalArgumentException("'threadCount' must be greater than 0: " + threadCount);
		}
		
		this.threadCount = threadCount;
	}
	
	/**
	 * 입력 큐 데이터 수신 타입 아웃 설정
	 * 
	 * @param timeout 입력 큐 데이터 수신 타입 아웃
	 */
	public synchronized void setTimeout(long timeout) throws Exception {
		
		if(timeout < 1) {
			throw new IllegalArgumentException("'timeout' must be greater than 0: " + timeout);
		}
		
		this.timeout = timeout;
	}
	
	/**
	 * from 큐의 최대 lag 개수 설정
	 * 
	 * @param maxLag 설정할 최대 lag 수
	 */
	public synchronized void setMaxLag(int maxLag) throws Exception {
		
		if(maxLag < 1) {
			throw new IllegalArgumentException("'maxLag' must be greater than 0: " + maxLag);
		}
		
		this.maxLag = maxLag;
	}
}
