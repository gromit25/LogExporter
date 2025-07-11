package com.redeye.logexporter.exporter;

import com.redeye.logexporter.exporter.kafka.KafkaExporter;
import com.redeye.logexporter.exporter.print.PrintExporter;

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
			return new KafkaExporter();
		}
	};

	/**
	 * 각 타입별 Exporter 생성 후 반환
	 *
	 * @return 생성된 Exporter
	 */
	public abstract Exporter create();
}
