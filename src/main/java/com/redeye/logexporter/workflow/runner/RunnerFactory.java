package com.redeye.logexporter.workflow.runner;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.annotation.ComponentConfig;
import com.redeye.logexporter.workflow.annotation.ComponentType;
import com.redeye.logexporter.workflow.comp.Collector;
import com.redeye.logexporter.workflow.comp.CronHandler;
import com.redeye.logexporter.workflow.comp.Exporter;
import com.redeye.logexporter.workflow.comp.Handler;

import lombok.Getter;
import lombok.Setter;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
@ConfigurationProperties(prefix = "workflow.comp") 
public class RunnerFactory {

	/** */
	@Value("${workflow.timeout.sec}")
	private long timeout;

	/** */
	@Value("${workflow.maxlag}")
	private int maxLag;

	/** workflow.comp 이하의 컴포넌트 설정 값 */
	@Getter
	@Setter
	private Map<String, String> configMap;

	/**
	 * 컴포넌트의 런너 객체 생성 후 반환
	 *
	 * @param name 컴포넌트 명
	 * @param component 컴포넌트 
	 * @return 컴포넌트의 런너 객체
	 */
	public AbstractRunner<?> create(String name, com.redeye.logexporter.workflow.comp.Component component) throws Exception {

		// 입력 값 검증
		if(component == null) {
			throw new IllegalArgumentException("component is null.");
		}

		// 컴포넌트 종류에 따라 런너 객체 생성
		AbstractRunner<?> runner = this.newRunner(name,component); 
    
		// 런너 객체 공통 설정
		this.setupRunner(runner);

		// 생성된 런너 객체 반환
		return runner;
	}
	
	/**
	 * 컴포넌트의 런너 객체 생성 후 반환<br>
	 * 런너 생성만 담당
	 * 
	 * @param name 컴포넌트 명
	 * @param component 컴포넌트 
	 * @return 컴포넌트의 런너 객체
	 */
	private AbstractRunner<?> newRunner(String name, com.redeye.logexporter.workflow.comp.Component component) throws Exception {
		
		if(component instanceof Collector) {
			return new CollectorRunner(name, (Collector)component);
		} else if(component instanceof Handler) {
			return new HandlerRunner(name, (Handler)component);
		} else if(component instanceof Exporter) {
			return new ExporterRunner(name, (Exporter)component);
		} else if(component instanceof CronHandler) {
			return new CronHandlerRunner(name, (CronHandler)component);
		} else {
			throw new IllegalArgumentException("component type is not available: " + component.getClass());
		}
	}

	/**
	 * 컴포넌트 런너 설정 수행
	 *
	 * @param runner 설정할 런너 객체
	 */
	private void setupRunner(AbstractRunner<?> runner) throws Exception {

		ComponentType type = ComponentType.NORMAL;
		String from = "";
		String subscribeSubject = "";
		int threadCount = 1;
		
		//
		ComponentConfig config = runner.getComponent().getClass()
				.getAnnotation(ComponentConfig.class);

		//
		String configPrefix = "workflow.comp." + runner.getName() + ".";
		
		String configThreadCount = this.configMap.get(configPrefix + "threadcount");
		if(configThreadCount != null) {
			threadCount = Integer.parseInt(configThreadCount);
		} else if(config != null) {
			threadCount = config.threadCount();
		}
		
		String configSubscribe = this.configMap.get(configPrefix + "subscribe");
		if(configSubscribe != null) {
			subscribeSubject = configSubscribe;
		} else if(config != null) {
			subscribeSubject = config.subscribe(); 
		}
		
		String configType = this.configMap.get(configPrefix + "type");
		if(configType != null) {
			type = ComponentType.valueOf(configType);
		} else if(config != null) {
			type = config.type();
		}
		
		String configFrom = this.configMap.get(configPrefix + "from");
		if(configFrom != null) {
			
		} else if(config != null) {
			config.from();
		}
		
		// 런너 공통 정보 설정
		runner.setTimeout(this.timeout);
		runner.setMaxLag(this.maxLag);
		
		//
		runner.setSubscribeSubject(subscribeSubject);
		runner.setThreadCount(threadCount);
	}
}
