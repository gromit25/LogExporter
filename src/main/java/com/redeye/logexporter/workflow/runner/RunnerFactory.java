package com.redeye.logexporter.workflow.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redeye.logexporter.workflow.WorkflowContext;
import com.redeye.logexporter.workflow.comp.Collector;
import com.redeye.logexporter.workflow.comp.CronHandler;
import com.redeye.logexporter.workflow.comp.Exporter;
import com.redeye.logexporter.workflow.comp.Handler;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
public class RunnerFactory {
	
	/** */
	@Autowired
	private WorkflowContext context;

	
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
		AbstractRunner<?> runner = this.newRunner(name, component); 
    
		// 런너 객체 공통 설정
		this.context.setupRunner(runner);

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
}
