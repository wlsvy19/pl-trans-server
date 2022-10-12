package com.eBrother.trans.kafka;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

public class KafkaConsumerManager implements Runnable, KafkaConst {
	
	private Properties _config;
	 
	private Properties _props; //  = new Properties();

	protected static Logger _log = Logger.getLogger(KafkaConsumerManager.class);	
	
	private KafkaConsumer<byte[], byte[]> consumer = null;
	private IConsumerIssueProcessing issueProcessor = null;

	private boolean bRunning = false;
	private boolean _bShutdown = false;
	private String _agent_groupid = null;
    
    static KafkaConsumerManager _myself = null;

    
    public void shutdown () {
    	_bShutdown = true;
    }

    synchronized public static KafkaConsumerManager getInstance ( Properties props ) {
    	
    	if ( _myself== null ) {
    		_myself = new KafkaConsumerManager ( props );
    	}
    	
    	_myself.reset();
    	
    	return _myself;
    }

    private void reset () {
    	
    	_bShutdown = false;
    	bRunning = false;
    	
    }
    public KafkaConsumerManager ( Properties props ) {
    	
    	_props = props;
    	_config = props;
    	init ();
    }
    
	private void init() {

		//String brokers = _config.getProperty("kafka.status.brokers");

		String interval = "10000";
		String timeout = "30000";

		//_props.put("bootstrap.servers", brokers);
		_props.put("enable.auto.commit", "true");
		_props.put("auto.offset.reset", "latest");

		_props.put("heartbeat.interval.ms", interval);
		_props.put("session.timeout.ms", timeout);

		_props.put("receive.buffer.bytes", 1024 * 1024 * 10);
		_props.put("fetch.max.bytes", 1024 * 1024 * 1024);
		
		_props.put("max.poll.records", 1);

		_props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		_props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	}

	public void start ( ) {
		
		Thread myself = new Thread ( this, KafkaConsumerManager.class.getName());
		myself.start();
		
	}
	
	@Override
	public void run() {

		
		String brokers = (String) _config.getProperty( _key_brokers );
		String consumerclass = (String) _config.getProperty( _key_handler );
		String consumerGroupId = (String)_config.getProperty( _key_groupId );
		while ( true ) {

			_log.info( "1000. consumer start / restart : " + consumerGroupId + "," + brokers + ", " +  consumerclass);

			// 1. consumer 준비.
			// 밑에서 에러가 나서 문제가 생기면, consumer 재 기동하기 위해서 사용함.
			try {
				_props.put("bootstrap.servers", brokers);
				_props.put("group.id", consumerGroupId );
				this.consumer = new KafkaConsumer<>(_props);
				this.issueProcessor = (IConsumerIssueProcessing)Class.forName( consumerclass ).newInstance();
			} catch (InstantiationException ex) {
				// class 가 없는 것임. 중지해야함.
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				// class 가 없는 것임. 중지해야함.
				throw new RuntimeException(ex);
			} catch (ClassNotFoundException ex) {
				// class 가 없는 것임. 중지해야함.
				throw new RuntimeException(ex);
			}

			try {
				this.issueProcessor.initialization( _config, _key_topic, _key_numthread, consumerGroupId, this);
			} catch (Exception ex) {
				_log.error(ex.getMessage(), ex);
				try { Thread.sleep( 10 * 1000L); } catch (InterruptedException ex2) { ; }
				continue;
			}

			int nrun = 0;
			while ( true ) {

				try { Thread.sleep( 1 * 1000L); } catch (InterruptedException ex) { ; }
				
				List<String> topicList = this.issueProcessor.getTopicList(consumerGroupId);
				if (null == topicList) {
					_log.error(String.format("1500. [%s]TOPIC_LIST IS NULL", consumerGroupId));
					this.consumer.close();
					break;
				}

				_log.debug(String.format("1010. [%s] TOPIC_LIST : [%s]", consumerGroupId, topicList.toString()));
					
				this.consumer.subscribe(topicList);
				this._bShutdown = false;
				this.bRunning = true;
		
				// _log.debug("1020. check for loop" );
				
				// Duration du = new Duration (1000); 
				
				if ( nrun++ > 60 ) {
					_log.info("1030. consumer stat : 실행중" );
					nrun = 0;
				}
				try {
					
					ConsumerRecords<byte[], byte[]> records = this.consumer.poll(100);
						
					// if ( records != null && records.count() > 0) _log.info("1030. check for loop : " + records.count()); 
					for (ConsumerRecord<byte[], byte[]> record : records) {
		
						String topic1 = record.topic();
						String issue = null;
						try {
							issue = new String(record.value(), "utf8");
							this.issueProcessor.issueProcessing(record.offset(), topic1, issue);
						} catch (Throwable ex) {
							if (null != issue) _log.error(String.format("1510. [ERR]%s", issue));
							_log.error("1520", ex);
						}
						//_log.info("3. check for loop" );
					}
					
					// _log.debug("1600. check for loop" );
					if (!records.isEmpty()) {
						this.consumer.commitSync(); // offset commit
					}				
	
				}
				catch ( Exception e ) {
					break;
				}
				this.bRunning = false;
			}
			
			// main consumer 가 죽음.
			// 재 기동 전에 잠시 대기
			try { Thread.sleep( 10 * 1000L); } catch (InterruptedException ex2) { ; }
		}
		
	}

	@Deprecated
	public synchronized boolean process( String key_brokers, String key_groupId,  String key_handler ) {
		
		String brokers = (String) _config.getProperty( key_brokers );
		String consumerclass = (String) _config.getProperty( key_groupId );
		String consumerGroupId = (String)_config.getProperty(key_handler);
		
		try {
			_props.put("bootstrap.servers", brokers);
			_props.put("group.id", consumerGroupId );
			
			this.consumer = new KafkaConsumer<>(_props);
			this.issueProcessor = (IConsumerIssueProcessing)Class.forName( consumerclass ).newInstance();
			
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		try {
			this.issueProcessor.initialization( _config, null, null, consumerGroupId, this);
		} catch (Exception ex) {
			_log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}

		List<String> topicList = this.issueProcessor.getTopicList(consumerGroupId);
		if (null == topicList) {
			_log.error(String.format("[%s]TOPIC_LIST IS NULL", consumerGroupId));
			this.consumer.close();
			System.exit(-1);
		}

		this.consumer.subscribe(topicList);
		
		if (this._bShutdown) return false;
		
		this.bRunning = true;
		
		ConsumerRecords<byte[], byte[]> records = this.consumer.poll(100);

		for (ConsumerRecord<byte[], byte[]> record : records) {
			String topic = record.topic();
			String issue = null;
			try {
				issue = new String(record.value(), "utf8");
				this.issueProcessor.issueProcessing(record.offset(), topic, issue);
			} catch (Throwable ex) {
				if (null != issue) _log.error(String.format("[ERR]%s", issue));
				_log.error(ex.getMessage(), ex);
			}
		}

		if (!records.isEmpty()) {
			this.consumer.commitSync(); // offset commit
		}

		this.bRunning = false;
				
		return true;
	}

	public synchronized void close() {
		// 실행 중인 프로세스가 끝날 때까지 대기
		while (this.bRunning) try { Thread.sleep(100L); } catch (InterruptedException ex) { ; }

		this._bShutdown = true;

		this.issueProcessor.close();
		this.consumer.close();
	}

}
