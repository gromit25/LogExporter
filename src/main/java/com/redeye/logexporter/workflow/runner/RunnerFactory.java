package com.redeye.logexporter.workflow.runner;

/**
 * 런너 생성 팩토리 클래스
 *
 * @author jmsohn
 */
@Component
@ConfigurationProperties(prefix = "workflow.comp") 
public class RunnerFactory {

  @Value("${workflow.timeout}")
  private final long timeout;

  @Value("${workflow.maxlag}")
  private final int maxLag;

  /** */
  @Getter
  @Setter
  private Map<String, String> configMap;

  /**
   * 컴포넌트의 런너 객체 생성 후 반환
   *
   * @param component 컴포넌트 
   * @return 컴포넌트의 런너 객체
   */
  public AbstractRunner create(Component component) throws Exception {

    // 입력 값 검증
    if(component == null) {
      throw new IllegalArgumentException("component is null.");
    }

    // 컴포넌트 종류에 따라 런너 객체 생성
    AbstractRunner runner = 
      switch (component) {
        case Collector c -> new CollectorRunner(c);
        case Handler h -> new HandlerRunner(h);
        case Exporter e -> new ExporterRunner(e);
        case CronHandler ch -> new CronHandlerRunner(ch);
        default -> throw new IllegalArgumentException("component type is not available: " + component.class);
      };
    
    // 런너 객체 공통 설정
    this.setupRunner(runner);

    // 생성된 런너 객체 반환
    return runner;
  }

  /**
   *
   *
   * @param runner
   */
  private void setupRunner(AbstractRunner runner) {

    //
    int threadCount = 1;
    String subscribeSubject = "";
    
    //
    runner.setTimeout(this.timeout);
    runner.setMaxLag(this.maxLag);
    runner.setThreadCount(threadCount);
    runner.setSubscribeSubject(subscribeSubject);
  }
}
