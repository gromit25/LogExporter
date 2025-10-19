package com.redeye.logexporter.common.exporter.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.jutools.StringUtil;

/**
 * kafka exporter 및 kafkaTemplate 객체 생성 클래스<br>
 * 설정값<br>
 * <li>app.kafka.use: 'y' 일 경우 활성화</li>
 * <li>app.kafka.servers: kafka server 목록</li>
 * <li>app.kafka.acks: kafka acks 설정</li>
 * <li>app.kafka.clientid: producer client id</li>
 *
 * @author jmsohn
 */
@Configuration
@ConditionalOnProperty(
	name="app.kafka.use",
	havingValue="y"
)
public class KafkaConfig {

	/**
	 * kafka template 생성 후 반환
	 * 
	 * @param producerFactory kafka producer factory
	 * @return kafka template 객체
	 */
	@Bean
	KafkaTemplate<String, String> kafkaTemplate(
		@Qualifier("producerFactory") ProducerFactory<String, String> producerFactory 
	) {
		return new KafkaTemplate<>(producerFactory);
	}

	/**
	 * kafka producer factory 생성
	 * 
	 * @param servers kafka server 목록
	 * @param acks kafka acks 설정
	 * @param clientId producer client id
	 * @return kafka producer factory 객체
	 */
	@Bean
	ProducerFactory<String, String> producerFactory(
		@Value("${app.kafka.servers}") String servers,
		@Value("${app.kafka.acks}") String acks,
		@Value("${app.kafka.clientid}") String clientId
	) {
		
		// 입력값 검증
		if(StringUtil.isBlank(servers) == true) {
			throw new IllegalArgumentException("app.kafka.servers is null or blank.");
		}
		
		if(StringUtil.isBlank(acks) == true) {
			throw new IllegalArgumentException("app.kafka.acks is null or blank.");
		}

		Map<String, Object> configProps = new HashMap<>();

		// 연결 설정
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		configProps.put(ProducerConfig.ACKS_CONFIG, acks);
		
		// 클라이언트 아이디 설정
		if(StringUtil.isBlank(clientId) == false) {
			configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		}

		return new DefaultKafkaProducerFactory<>(configProps);
	}
}
