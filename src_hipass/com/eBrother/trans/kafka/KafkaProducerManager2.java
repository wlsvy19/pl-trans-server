package com.eBrother.trans.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.concurrent.Future;


public class KafkaProducerManager2 implements KafkaConst {

	static Properties _config;
	
	protected static Logger _log = Logger.getLogger(KafkaProducerManager2.class.getName());	
	private KafkaProducer<byte[], byte[]> producer = null;

	public KafkaProducerManager2( Properties config ) {
		
		_config = config;
		_init( );
	}

	private void _init( ) {

		String brokers = _config.getProperty(_key_brokers);
		//int retries = Integer.parseInt ( _config.getProperty("kafka.retries"));
		//int batchSize = Integer.parseInt (_config.getProperty("kafka.batch.size"));
		// int bufferMemory = Integer.parseInt (_config.getProperty("kafka.buffer.memory"));
		
		Properties props = new Properties();
		props.put("bootstrap.servers", brokers);
		props.put("acks", "all");
		props.put("retries", 3);
		props.put("batch.size", 1);
		props.put("linger.ms", 100);
		// props.put("buffer.memory", bufferMemory);
		props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

		_config = props;
		
		this.producer = new KafkaProducer<>(props);

	}

	public boolean send(String topic, String str) {

		byte[] bytes;
		try {
			bytes = str.getBytes("utf8");
			ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(topic, bytes);
			
			if ( this.producer == null ) this.producer = new KafkaProducer<>(_config);
			Future<RecordMetadata> qqq = this.producer.send(record); 
			return true;
		} catch (Exception ex) {
			
			this.producer = null;
			_log.error( ex.fillInStackTrace() );
			try {
				this.producer.close();
			}
			catch ( Exception e2 ) {
				_log.error( ex.fillInStackTrace() );
			}
			return false;
		}
		
	}

	public void flush() {
		try {
			this.producer.flush();
		}
		catch ( Exception e ) {
			_log.error( e.fillInStackTrace() );
			try {
				this.producer.close();
			}
			catch ( Exception e2 ) {
				_log.error( e2.fillInStackTrace() );
			}
		}
	}

	public void close() {
		try {
			this.producer.close();
			this.producer = null;
		}
		catch ( Exception e2 ) {
			_log.error( e2.fillInStackTrace() );
		}		
	}	
}
