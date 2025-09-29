package com.redeye.logexporter.workflow.runner;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
public class RunnerFactory {

  @Value("${workflow.timeout.sec}")
  private final long timeout;

  @Value("${workflow.lag.max}")
  private final int maxLag;

  /**
   * 컴포넌트의 런너 객체 생성 후 반환
   *
   * @param component 컴포넌트 
   * @param threadCount 스레드 개수
   * @return 컴포넌트의 런너 객체
   */
  public AbstractRunner create(Component component, int threadCount) throws Exception {

    // 입력 값 검증
    if(component == null) {
      throw new IllegalArgumentException("component is null.");
    }

    // 컴포넌트 종류에 따라 런너 객체 생성
    AbstractRunner runner = null;
    
    switch (component) {
      case Collector c -> {
        runner = new CollectorRunner(component);
      }
      case Handler h -> {
        runner = new HandlerRunner(component);
        runner.setFromQueue();
      }
      case Exporter e -> {
        runner = new ExporterRunner(component);
        runner.setFromQueue();
      }
      case CronHandler ch -> {
        runner = new CronHandlerRunner(component);
        runner.setFromQueue();
      }
      default -> {
        throw new IllegalArgumentException("component type is not available: " + component.class);
      }
    }
    
    // 런너 객체 공통 설정
    runner.setTimeout(this.timeout);
    runner.setMaxLag(this.maxLag);
    runner.setThreadCount(threadCount);
  }
}
