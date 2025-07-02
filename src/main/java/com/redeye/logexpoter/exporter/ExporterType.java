package com.redeye.logexpoter.exporter;

/**
 * exporter 타입
 * 
 * @author jmsohn
 */
public enum ExporterType {
	
	PRINT {
		@Override
		public Exporter create() {
			return new PrintExporter();
		}
	},
	
	KAFKA {
		@Override
		public Exporter create() {
			return new KafkaExpoter();
		}
	};

	/**
	 * 각 타입별 Exporter 생성 후 반환
	 *
	 * @return 생성된 Expoter
	 */
	public abstract Expoter create();
}
