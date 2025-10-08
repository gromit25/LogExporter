package com.redeye.logexporter.workflow.runner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.jutools.CronJob;
import com.jutools.StringUtil;
import com.redeye.logexporter.workflow.Message;
import com.redeye.logexporter.workflow.WorkflowContext;
import com.redeye.logexporter.workflow.annotation.Activity;
import com.redeye.logexporter.workflow.annotation.Cron;
import com.redeye.logexporter.workflow.annotation.Exit;
import com.redeye.logexporter.workflow.annotation.Init;
import com.redeye.logexporter.workflow.annotation.Process;

import lombok.extern.slf4j.Slf4j;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
@Slf4j
public class RunnerFactory {
	
	/** 워크플로우 컨텍스트(설정) */
	@Autowired
	private WorkflowContext context;
	
	/** 스프링부트 환경 객체 */
	private final Environment env;

	/**
	 * 생성자
	 * 
	 * @param env 스프링부트 환경 객체
	 */
	public RunnerFactory(Environment env) {
		this.env = env;
	}
	
	/**
	 * 컴포넌트의 런너 객체 생성 후 반환
	 *
	 * @param name 액티비티 명
	 * @param activity 액티비티 객체 
	 * @return 컴포넌트의 런너 객체
	 */
	public ActivityRunner create(String name, Object activity) throws Exception {
		
		// 스프링부트 환경 객체 존재 여부 확인
		if(this.env == null) {
			throw new NullPointerException("'env' is null.");
		}
		
		// 입력 값 검증
		if(StringUtil.isBlank(name) == true) {
			throw new IllegalArgumentException("'name' is null or blank.");
		}
		
		if(activity == null) {
			throw new IllegalArgumentException("'activity' is null.");
		}
		
		// 액티비티 객체 생성
		ActivityRunner runner = new ActivityRunner();

		// 액티비티 명 설정
		runner.setName(name);
		
		// 액티비티 객체 설정
		this.setActivity(runner, activity);
		
		// 실행할 메소드 추출 및 설정
		for(Method method: activity.getClass().getMethods()) {
			
			// init 메소드 설정
			this.setInitMethod(runner, method);
			
			// process 메소드 설정
			this.setProcessMethod(runner, method);

			// exit 메소드 설정
			this.setExitMethod(runner, method);
			
			// cron 메소드 추가
			this.addCronMethod(runner, method);
			
		}
		
		// process 메소드 설정되어 있는지 확인
		if(runner.getProcessMethod() == null) {
			throw new IllegalArgumentException("process method is not found at " + activity.getClass());
		}
		
		// 런너 객체 설정 - 컨텍스트 반영
		this.context.setupRunner(runner);
		
		return runner;
	}
	
	/**
	 * 액티비티 어노테이션 관련 설정<br>
	 * 이전 액티비티 연결 타입, 이전 액티비티명, 구독 제목, 실행 스레드 수
	 * 
	 * @param activity 설정할 액티비티
	 */
	private void setActivity(ActivityRunner runner, Object activity) throws Exception {
		
		// 액티비티 객체 설정
		runner.setActivity(activity);
		
		// 액티비티 어노테이션 획득
		Activity activityAnnotation = this.getActivityAnnotation(runner);
		
		// 이전 액티비티와의 링크 타입 설정
		runner.setLinkType(activityAnnotation.linkType());
		
		// 이전 액티비티 명 설정
		runner.setFrom(this.resolveValue(activityAnnotation.from()));
		
		// 구독 제목 설정
		runner.setSubscriptionTopic(this.resolveValue(activityAnnotation.subscribe()));
		
		// 실행 스레드 수 설정
		String threadCount = this.resolveValue(activityAnnotation.threadCount());
		runner.setThreadCount(Integer.parseInt(threadCount));
	}
	
	/**
	 * 초기화 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setInitMethod(ActivityRunner runner, Method method) throws Exception {
		
		Init initAnnotation = method.getAnnotation(Init.class);
		if(initAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(runner.getInitMethod() != null) {
			throw new IllegalArgumentException("duplicated init method at " + runner.getActivity().getClass());
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
		runner.setInitMethod(method);
	}
	
	/**
	 * 종료시 호출 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setExitMethod(ActivityRunner runner, Method method) throws Exception {
		
		Exit exitAnnotation = method.getAnnotation(Exit.class);
		if(exitAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(runner.getExitMethod() != null) {
			throw new IllegalArgumentException("duplicated exit method at " + runner.getActivity().getClass());
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
		runner.setExitMethod(method);
	}
	
	/**
	 * 크론 메소드 추가
	 * 
	 * @param method 설정할 메소드
	 */
	private void addCronMethod(ActivityRunner runner, Method method) throws Exception {
		
		Cron cronAnnotation = method.getAnnotation(Cron.class);
		if(cronAnnotation == null) {
			return;
		}
		
		// 메소드 public 여부 검사
		if(isPublic(method) == false) {
			throw new IllegalArgumentException("cron method must be public: " + method);
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
		
		// cron 주기 설정
		String period = this.resolveValue(cronAnnotation.period());
		
		// cron method 추가
		runner.getCronJobMap().put(
			method.getName(),
			new CronJob(period, () -> {
				
				final ActivityRunner curRunner = runner;
				final Method cronMethod = method;
				
				try {
					Object result = cronMethod.invoke(curRunner.getActivity());
					curRunner.put(result);
				}catch(Exception ex) {
					log.error(cronMethod.toString(), ex);
				}
			})
		);
	}
	
	/**
	 * 데이터 처리 메소드 설정
	 * 
	 * @param method 설정할 메소드
	 */
	private void setProcessMethod(ActivityRunner runner, Method method) throws Exception {
		
		Process processAnnotation = method.getAnnotation(Process.class);
		if(processAnnotation == null) {
			return;
		}
		
		// 이미 설정되어 있는 경우 예외 발생
		if(runner.getProcessMethod() != null) {
			throw new IllegalArgumentException("duplicated process method at " + runner.getActivity().getClass());
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
				runner.setFromQueue(new LinkedBlockingQueue<>());
			} else {
				throw new IllegalArgumentException("init method must have 0 parameter: " + method);
			}
		}
		
		// process method 설정
		runner.setProcessMethod(method);
	}
	
	/**
	 * 스프링부트의 SpEL 수행 결과 반환
	 * 
	 * @param value SpEL
	 * @return 수행 결과
	 */
	private String resolveValue(String value) throws Exception {
		return this.env.resolveRequiredPlaceholders(value);
	}
	
	/**
	 * 액티비티에 설정된 액티비티 어노테이션 반환
	 * 
	 * @return 액티비티 어노테이션
	 */
	private Activity getActivityAnnotation(ActivityRunner runner) throws Exception {
		
		Activity activityAnnotation = runner.getActivity().getClass().getAnnotation(Activity.class);
		
		if(activityAnnotation == null) {
			throw new NullPointerException("'activity' class must have Activity Annotation: " + runner.getActivity().getClass());
		}
		
		return activityAnnotation;
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
	 * 주어진 타입이 List<Message<?>> 여부 반환
	 * 
	 * @param type 검사할 타입
	 * @return List<Message<?>> 여부
	 */
	private static boolean isMessageListType(Type type) {
		
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
	private static boolean isMessageType(Type type) {
		
		if(type instanceof ParameterizedType == true) {
			return ((ParameterizedType)type).getRawType() == Message.class;
        }
        
		return false;
	}
}
