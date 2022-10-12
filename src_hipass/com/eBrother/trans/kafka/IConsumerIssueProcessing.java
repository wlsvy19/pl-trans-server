package com.eBrother.trans.kafka;

import java.util.List;
import java.util.Properties;

public interface IConsumerIssueProcessing {

	public void initialization(Properties config, String key_topic, String key_numthread, String consumerGroupId, KafkaConsumerManager commonConsumer) throws Exception;
	public List<String> getTopicList(String consumerGroupId);
	public void issueProcessing(long offset, String topic, String issue);
	public void close();
}
