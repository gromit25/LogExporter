### 특정 로그 파일을 읽어서 kafka로 전송    
    
- 모니터링 대상 파일 지정 환경 변수(Mandatory)   
  LE_MONITOR_FILES=was1_agent.log, was2_agent.log    
  
- 로드할 Exporter 설정 환경 변수(Mandatory)   
  LE_EXPORTER_TYPE=KAFKA # 현재는 KAFKA만 지원   
  
- 필터 표현식(Optional)   
  LE_FILTER_SCRIPT=$1 == Name   
  
- 필터 클래스 설정(Optional)   
  설정되지 않을 경우 모든 로그 메시지를 전송    
  LE_FILTER_CLASS=com.redeye.builtin.Filter   
    
- kafka 연결정보 환경변수    
  LE_EXPORTER_TYPE=KAFKA 일 경우에만    
      
  KAFKA_BROKER_ID: 1    
  KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181    
  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT    
  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092    
  KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1   
