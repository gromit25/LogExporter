### 특정 로그 파일을 읽어서 필터링 후 외부저장소(kafka 등)로 전송    

### 환경변수 목록   
- 모니터링 대상 파일 지정 환경 변수(Mandatory)   
  LE_MONITOR_FILES=was1_agent.log, was2_agent.log    
  
- 로드할 Exporter 설정 환경 변수(Mandatory)   
  LE_EXPORTER_TYPE=KAFKA # 현재는 KAFKA만 지원(소문자 인식 안됨)   
  
- 필터 표현식(Optional)    
  LE_FILTER_SCRIPT=match(F[0], 'abc')   
  미설정 시 모든 로그 export 됨   
    
- kafka 연결정보 환경변수    
  LE_EXPORTER_TYPE=KAFKA 일 경우에만
  
  SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
