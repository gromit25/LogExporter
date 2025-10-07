package com.redeye.logexporter.workflow.runner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.jutools.CronJob;
import com.jutools.StringUtil;
import com.jutools.thread.AbstractDaemon;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Cron;
import com.redeye.logexporter.workflow.annotation.Exit;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

import lombok.Getter;
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
	private final String name;
	
	/** 액티비티 객체 */
	private final Object activity;
	
	/** 액티비티 객체의 초기화 메소드 */
	private Method initMethod;
	
	/** 액티비티 객체의 데이터 처리 메소드 */
	private Method processMethod;
	
	/** 액티비티 객체의 동료시 호출 메소드 */
	private Method exitMethod;
	
	/** 스레드 중단 여부 */
	@Getter
	private volatile boolean stop = true;
	
	/** 스레드 목록 */
	private AbstractDaemon[] threadAry;
	
	/** 스레드 개수 */
	private int threadCount = 1;
	
	/** 구독 제목 패턴 */
	private StringUtil.WildcardPattern subscriptionSubject;
	
	/** 입력 큐 */
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
	private Map<String, CronJob> cronJobMap = new ConcurrentHashMap<>();

	
	/**
	 * 생성자
	 *
	 * @param name 스프링부트 컴포넌트 명
	 * @param activity 워크플로우 액티비티 객체
	 */
	protected ActivityRunner(String name, Object activity) throws Exception {
		
		// 입력값 검증
		if(name == null) {
			throw new IllegalArgumentException("'name' is null.");
		}
		
		if(activity == null) {
			throw new IllegalArgumentException("'activity' is null.");
		}
		
		// 액티비티 명과 객체 설정
		this.name = name;
		this.activity = activity;
		
		// 실행할 메소드 추출 및 설정
		for(Method method: this.activity.getClass().getMethods()) {
			
			// init 메소드 설정
			this.setInitMethod(method);
			
			// process 메소드 설정
			this.setProcessMethod(method);

			// exit 메소드 설정
			this.setExitMethod(method);
			
			// cron 메소드 추가
			this.addCronMethod(method);
			
		}
		
		// process 메소드 설정되어 있는지 확인
		if(this.processMethod == null) {
			throw new IllegalArgumentException("process method is not found at " + this.activity.getClass());
		}
	}
	
	/**
	 * 초기화 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setInitMethod(Method method) throws Exception {
		
		Init initAnnotation = method.getAnnotation(Init.class);
		if(initAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(this.initMethod != null) {
			throw new IllegalArgumentException("duplicated init method at " + this.activity.getClass());
		}
		
		// 메소드 public 여부 검사
		if(isPublic(method) == false) {
			throw new IllegalArgumentException("ini method must be public: " + method);
		}
		
		// 리턴 타입 검사
		if(method.getReturnType() != void.class) {
			throw new IllegalArgumentException("init method return type must be void: " + method);
		}
		
		// 파라미터 검사
		if(method.getParameterCount() != 0) {
			throw new IllegalArgumentException("init method must have 0 parameter: " + method);
		}
		
		// init method 설정
		this.initMethod = method;
	}
	
	/**
	 * 데이터 처리 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setProcessMethod(Method method) throws Exception {
		
		Process processAnnotation = method.getAnnotation(Process.class);
		if(processAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(this.processMethod != null) {
			throw new IllegalArgumentException("duplicated process method at " + this.activity.getClass());
		}

		// 메소드 public 여부 검사
		if(isPublic(method) == false) {
			throw new IllegalArgumentException("ini method must be public: " + method);
		}
		
		// 리턴 타입 검사
		Type returnType = method.getGenericReturnType();
		if(
			returnType != void.class
			&& isMessageListType(returnType) == false
			&& isMessageType(returnType) == false
		) {
			throw new IllegalArgumentException("process method return type must be 'void' or 'List<Message<?>>': " + method);
		}
		
		// 파라미터 검사
		if(method.getParameterCount() != 0) {
			if(
				method.getParameterCount() == 1
				&& isMessageType(method.getGenericParameterTypes()[0]) == true
			) {
				// process 메소드에 입력 파라미터(Message<?>)가 있는 경우 fromQueue 를 생성함
				this.fromQueue = new LinkedBlockingQueue<>();
			} else {
				throw new IllegalArgumentException("init method must have 0 parameter: " + method);
			}
		}
		
		// process method 설정
		this.processMethod = method;
	}
	
	/**
	 * 종료시 호출 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setExitMethod(Method method) throws Exception {
		
		Exit exitAnnotation = method.getAnnotation(Exit.class);
		if(exitAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(this.exitMethod != null) {
			throw new IllegalArgumentException("duplicated exit method at " + this.activity.getClass());
		}
		
		// 메소드 public 여부 검사
		if(isPublic(method) == false) {
			throw new IllegalArgumentException("ini method must be public: " + method);
		}
		
		// 리턴 타입 검사
		if(method.getReturnType() != void.class) {
			throw new IllegalArgumentException("exit method return type must be void: " + method);
		}
		
		// 파라미터 검사
		if(method.getParameterCount() != 0) {
			throw new IllegalArgumentException("exit method must have 0 parameter: " + method);
		}
		
		// exit method 설정
		this.exitMethod = method;
	}
	
	/**
	 * 크론 메소드 추가
	 * 
	 * @param method 설정할 메소드
	 */
	private void addCronMethod(Method method) throws Exception {
		
		Cron cronAnnotation = method.getAnnotation(Cron.class);
		if(cronAnnotation == null) {
			return;
		}
		
		// 메소드 public 여부 검사
		if(isPublic(method) == false) {
			throw new IllegalArgumentException("ini method must be public: " + method);
		}
		
		// 리턴 타입 검사
		Type returnType = method.getGenericReturnType();
		if(returnType != void.class && isMessageListType(returnType) == false) {
			throw new IllegalArgumentException("cron method return type must be 'void' or 'List<Message<?>>': " + method);
		}
		
		// 파라미터 검사
		if(method.getParameterCount() != 0) {
			throw new IllegalArgumentException("cron method must have 0 parameter: " + method);
		}
		
		// cron method 추가
		this.cronJobMap.put(
			method.getName(),
			new CronJob(cronAnnotation.period(), () -> {
				
				final Method cronMethod = method;
				
				try {
					Object result = cronMethod.invoke(this.activity);
					put(result);
				}catch(Exception ex) {
					log.error(cronMethod.toString(), ex);
				}
			})
		);
	}
	
	/**
	 * 주어진 타입이 List<Message<?>> 여부 반환
	 * 
	 * @param type 검사할 타입
	 * @return List<Message<?>> 여부
	 */
	private boolean isMessageListType(Type type) {
		
		if(type instanceof ParameterizedType == true) {
			
			ParameterizedType paramType = (ParameterizedType)type;
			
			// List 여부 확인
			if(paramType.getRawType() == List.class) {

				// 제네릭 타입 파라미터 확인
				Type[] typeArgs = paramType.getActualTypeArguments();

				if(typeArgs.length == 1) {
					return isMessageType(typeArgs[0]);
				}
			}
		}
		
		return false;
	}

	/**
	 * 주어진 타입이 Message<?> 여부 반환
	 * 
	 * @param type 검사할 타입
	 * @return Message<?> 여부
	 */
	private boolean isMessageType(Type type) {
		
		if(type instanceof ParameterizedType == true) {
			return ((ParameterizedType)type).getRawType() == Message.class;
        }
        
		return false;
	}
	
	/**
	 * 메소드의 public 여부 반환
	 * 
	 * @param method 검사 대상 메소드
	 * @return public 여부
	 */
	private static boolean isPublic(Method method) {
		return Modifier.isPublic(method.getModifiers());
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
				subscriber.subscriptionSubject == null 
				|| subscriber.subscriptionSubject.match(message.getSubject()).isMatch()
			);
	}
	
	/**
	 * 구독 제목 설정
	 * 
	 * @param subscriptionSubject 구독 제목
	 */
	public void setSubscriptionSubject(String subscriptionSubject) throws Exception {
		
		// 구독 제목(subscriptionSubject) 이 없는 경우,
		// 모든 제목에 대해 구독하도록 this.subscriptionSubject을 null 로 설정
		if(StringUtil.isBlank(subscriptionSubject) == false) {
			this.subscriptionSubject = StringUtil.WildcardPattern.create(subscriptionSubject);
		} else {
			this.subscriptionSubject = null;
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
	
	/**
	 * 액티비티에 설정된 액티비티 어노테이션 반환
	 * 
	 * @return 액티비티 어노테이션
	 */
	public Activity getActivityAnnotation() {
		return this.activity.getClass().getAnnotation(Activity.class);
	}
}
