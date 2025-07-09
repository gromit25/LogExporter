# Log Exporter

### 특정 로그 파일을 읽어서 필터링 후 외부저장소(kafka 등)로 전송     
    
### 환경변수 목록    
- **프로그램 중지 파일 환경 변수(Optional) - 해당 파일이 새로 생성 되거나 touch 되면 Log Exporter는 중지됨**     
  디폴트 값 : ./log_exporter.stop    
  주의) 파일명은 아래의 예와 같이 항상 디렉토리가 같이 설정되어야 함    
        가능하면 전체 경로를 모두 설정하는 것이 좋음    
  LE_STOP_FILE=./log_exporter.stop
   
   
- **모니터링 대상 파일 지정 환경 변수(Mandatory)**    
  LE_MONITOR_FILES=was1_agent.log, was2_agent.log    

- **모니터링 대상 파일의 Reader 지정 변수(Optional)**    
  디폴트 값 : LINE_READER   
  LE_TRAKCER_READER_TYPE=LINE_READER  # LINE_READER 지원 (소문자 인식 안됨)   
   
  
- **필터의 필드 구분자 설정(Optional)**   
  필터 및 메시지 변환에서 사용할 변수 목록을 생성용으로 사용
  디폴트 값 : "[ \t]+"   
  LE_FILTER_DELIMITER=[ \t]+  # 정규표현식 사용, String.split 메소드의 파라미터로 사용   
  필터 및 메시지 변환에서 사용 가능한 변수   
  - log : log 메시지 전체    
  - fields : log를 LE_FILTER_DELIMITER 로 나누어진 변수 목록    
  
- **필터 표현식(Optional)**    
  LE_FILTER_SCRIPT=match(fields[0], 'abc')  # 0 번째 필드가 abc 일 경우 export 실행    
  미설정 시 모든 로그 export 됨


- **메시지 변환(Optional)**   
  디폴트 값 : %{log}    
  LE_TRANSFORMER_FORMAT=%{log}   
  사용 예)   
  - %{fields[0]}    
  - {"time":%{fields[0]}, "type":"%{fields[2]}"}   
  
  
- **로드할 Exporter 설정 환경 변수(Optional)**    
  디폴트 값 : PRINT    
  LE_EXPORTER_TYPE=KAFKA # PRINT, KAFKA 지원(소문자 인식 안됨)     
       
- **kafka 연결정보 환경변수(LE_EXPORTER_TYPE=KAFKA 인 경우, Mandatory)**     
  LE_EXPORTER_KAFKA_CLIENT_ID=1    
  LE_EXPORTER_KAFKA_TOPIC_NAME=test_topic     
  LE_EXPORTER_KAFKA_URL=localhost:9092    
  
