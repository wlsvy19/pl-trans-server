package com.eBrother.trans.kafka;

import com.eBrother.trans.si.HiHeaderConst;
import com.eBrother.trans.si.HiTransHeader;
import com.eBrother.trans.si.HiTransWorker632;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.StringUtil;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConsumerExecutor implements IConsumerIssueProcessing {
	
	private static final Logger _log = Logger.getLogger(ConsumerExecutor.class);

	private String _topicIds = null;
	Properties _config = null;
	JSONParser _jsonparser = new JSONParser();

	SocketUtil socUtil = new SocketUtil ();
	
	@Override
	public void initialization(Properties config, String key_topic, String key_numthread, String consumerGroupId, KafkaConsumerManager commonConsumer) throws Exception {

		this._topicIds = config.getProperty( key_topic );
		int numOfThreads = 1; // Integer.parseInt(config.getProperty( key_numthread ));
		this._config = config;
	}

	@Override
	public List<String> getTopicList(String consumerGroupId) {
		if (null == this._topicIds) return null;

		return Arrays.asList(StringUtil.explode(this._topicIds, ",", true));
	}

	@Override
	public void issueProcessing(long offset, String topic, String msg) {

		HiTransHeader hiheader = new HiTransHeader ();
		String clientip = null;
		String issue = null;
		try {

			issue = msg.substring(5);
			
			JSONObject jsonObject = (JSONObject) _jsonparser.parse(issue);

			hiheader.setMSG_TYPE( (String ) jsonObject.get(HiHeaderConst.MSG_TYPE));
			hiheader.setDATA_TYPE( (String ) jsonObject.get(HiHeaderConst.DATA_TYPE));
			hiheader.setSYS_TYPE( (String ) jsonObject.get(HiHeaderConst.SYS_TYPE));
			hiheader.setSEND_TIME( (String ) jsonObject.get(HiHeaderConst.SEND_TIME));
			hiheader.setIC_CODE( (String ) jsonObject.get(HiHeaderConst.IC_CODE));
			hiheader.setLANE_NO( (String ) jsonObject.get(HiHeaderConst.LANE_NO));
			hiheader.setBD_NAME( (String ) jsonObject.get(HiHeaderConst.BD_NAME));
			hiheader.setMAKER_NAME( (String ) jsonObject.get(HiHeaderConst.MAKER_NAME));
			hiheader.setINTERFACE_VERSION( (String ) jsonObject.get(HiHeaderConst.INTERFACE_VERSION));

			StringBuffer buf = new StringBuffer();

			buf.append( hiheader.getSYS_TYPE());
			buf.append( ".");
			buf.append( hiheader.getIC_CODE());
			buf.append( ".");
			buf.append( hiheader.getLANE_NO());
			buf.append( ".");
			buf.append( hiheader.getBD_NAME());
			buf.append( ".");
			buf.append( hiheader.getMAKER_NAME());
			
			String hash_data = buf.toString();
			
			// i.e. control 명령을 실행하기 위해 admin 에서 접속한 socket 입니다.
			Socket clientSocket = HiTransWorker632.getClientSocket(hash_data );
			
			//20191202//
			//byte [] payload = null;

			if ( clientSocket != null ) {
			
				clientip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");


				// 신규 메시지 생성. 길이 5byte + 원래 data size 임
				// 여기서 ... 현재 utf8 이기 때문에, euckr 로 바꾸어야  하나 ??
				// data 만들어서 전송함. 그것으로 끝 .
				byte [] payload = issue.getBytes(Charset.forName("euc-kr"));
				
				//byte [] payload = issue.getBytes("euc-kr")
				
				byte [] msgout = new byte [ 5 + payload.length];
				
				String szlen = eBrotherUtil.setString("" + payload.length, 5, '0');
				
				System.arraycopy( szlen.getBytes(), 0, msgout, 0, 5);
				System.arraycopy(payload, 0, msgout, 5, payload.length);
				
				try {
					socUtil.setClientMsg2( clientSocket, msgout, msgout.length );
					_log.info( clientip + " - "+ clientip + " : CONTROL MSG SUCC - " +  hash_data + "\n" + new String (msgout) );
				}
				catch ( Exception e_send ) {
					// error msg ...
					_log.info( clientip + " - "+ clientip + " : CONTROL MSG FAIL - " +  hash_data + " ->" + new String (payload));
				}
				// msg 전송하는 것으로 종료됨.
			}
			else {
			
				_log.info( clientip + " - "+ clientip + " : CONTROL MSG FAIL - " +  hash_data + " ->" + issue);
			}
			
		}
		catch ( Exception e2 ) {
		}
		
		
	}

	@Override
	public void close() {
		//this.executor.shutdown();
	}


}

