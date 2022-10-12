package com.eBrother.trans.si;

import com.eBrother.app.impl.ILogConst;
import com.eBrother.trans.kafka.KafkaProducerManager2;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.eBrotherUtil;
import com.google.gson.Gson;
import org.apache.coyote.*;
import org.apache.coyote.http11.*;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.SSLSupport;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.regex.Pattern;


public class HiTransWorker632 implements ActionHook, ILogConst {
	static{
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    System.setProperty("current.date", dateFormat.format(new Date()));
	}
	// KafkaProducerManager2 _kafka;
	final static int TR_PRO_V1 = 2;
	final static int TR_PRO_V2 = 3;
	final static int TR_PRO_V3 = 5;
	// socket 이 연결되면, 해당 socket 에 대한 IP, client header 정보를 저장함.
	// socket 이 끈어지면, 값을 삭제해야 함. 따라서, 2개를 가지고 있는 것임.
	// header key 를 가지고, socket 을 찾는 것.
	static public Hashtable<String, Socket > _controlHsocket_ = new Hashtable<String, Socket> ();
	static public Hashtable _connectchekerst = new Hashtable<String, String> (); 
	// - 파일로 로그 저장하기 위해 사용 ...
	static Hashtable<String, BufferedWriter > _writer_ = new Hashtable<String, BufferedWriter> ();
	protected static Logger _log = Logger.getLogger(HiTransWorker632.class.getName());
	protected static Logger _payload_succ = Logger.getLogger( "payload_succ");
	protected static Logger _payload_fail = Logger.getLogger( "payload_fail");
    /**
     * Associated adapter.
     */
    protected Adapter adapter = null;
    /**
     * Request object.
     */
    protected Request request = null;
    /**
     * Response object.
     */
    protected Response response = null;
    /**
     * Input.
     */
    protected InternalInputBuffer inputBuffer = null;
    static final String VFS = System.getProperty("file.separator");   
    /**
     * Output.
     */
    protected InternalOutputBuffer outputBuffer = null;
    /**
     * State flag.
     */
    protected boolean started = false;
    /**
     * Error flag.
     */
    protected boolean error = false;
    /**
     * Keep-alive.
     */
    protected boolean keepAlive = true;
    /**
     * HTTP/1.1 flag.
     */
    protected boolean http11 = true;
    /**
     * HTTP/0.9 flag.
     */
    protected boolean http09 = false;
    /**
     * Content delimitator for the request (if false, the connection will
     * be closed at the end of the request).
     */
    protected boolean contentDelimitation = true;
    /**
     * Is there an expectation ?
     */
    protected boolean expectation = false;
    /**
     * List of restricted user agents.
     */
    protected Pattern[] restrictedUserAgents = null;
    /**
     * Maximum number of Keep-Alive requests to honor.
     */
    protected int maxKeepAliveRequests = -1;
    /**
     * The number of seconds Tomcat will wait for a subsequent request
     * before closing the connection.
     */
    protected int keepAliveTimeout = -1;
    /**
     * SSL information.
     */
    protected SSLSupport sslSupport;
    /**
     * Socket associated with the current connection.
     */
    protected Socket socket;
    /**
     * Remote Address associated with the current connection.
     */
    protected String remoteAddr = null;
    /**
     * Remote Host associated with the current connection.
     */
    protected String remoteHost = null;
    /**
     * Local Host associated with the current connection.
     */
    protected String localName = null;
    /**
     * Local port to which the socket is connected
     */
    protected int localPort = -1;
    /**
     * Remote port to which the socket is connected
     */
    protected int remotePort = -1;
    /**
     * The local Host address.
     */
    protected String localAddr = null;
    /**
     * Maximum timeout on uploads. 5 minutes as in Apache HTTPD server.
     */
    protected int timeout = 300000;
    /**
     * Flag to disable setting a different time-out on uploads.
     */
    protected boolean disableUploadTimeout = false;
    /**
     * Allowed compression level.
     */
    protected int compressionLevel = 0;
    /**
     * Minimum contentsize to make compression.
     */
    protected int compressionMinSize = 2048;
    /**
     * Socket buffering.
     */
    protected int socketBuffer = -1;
    /**
     * Max saved post size.
     */
    protected int maxSavePostSize = 4 * 1024;
    /**
     * List of user agents to not use gzip with
     */
    protected Pattern noCompressionUserAgents[] = null;
    /**
     * List of MIMES which could be gzipped
     */
    protected String[] compressableMimeTypes =
    { "text/html", "text/xml", "text/plain" };
    /**
     * Host name (used to avoid useless B2C conversion on the host name).
     */
    protected char[] hostNameC = new char[0];
    /**
     * Associated endpoint.
     */
    protected JIoEndpoint endpoint;
    Properties _config;
    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    protected String server = null;
    // ------------------------------------------------------------- Properties
    ////////////////// 가장 중요한 변수
    // client 가 접속한 header 정보 중 key 에 해당한 값임.
    public String _header_data = null;
    SocketUtil m_socUtil = null;
    String m_szinbound = "";
	String m_szpattern_server = "";
	String m_szpattern_meta = "";
	String m_szoutbound = "";
	String m_diripwrite = "";
	String ENCODE = "KSC5601";
	JSONParser _jsonparser = new JSONParser();
	SimpleDateFormat sys_time_format;
	
    static public Socket getClientSocket ( String key ) {
    	Socket controlSocket = null;
    	if ( _controlHsocket_.containsKey(key)) {
    		controlSocket = _controlHsocket_.get(key);
    	}
    	return controlSocket;
    }
    /** Get the request associated with this processor.
     *
     * @return The request
     */
    public Request getRequest() {
        return request;
    }
    public void init ( String szkey, String szdata ) {
    	if ( szkey.equals("inbound"))  	m_szinbound = szdata;
    	else if ( szkey.equals("pattern_server"))  	m_szpattern_server = szdata;
    	else if ( szkey.equals("pattern_meta"))  	m_szpattern_meta = szdata;
    	else if ( szkey.equals("outbound"))  	m_szoutbound = szdata;
    	else if ( szkey.equals("encode")) ENCODE = szdata;
    	else if ( szkey.equals("diripwrite")) m_diripwrite = szdata;
    }
	public HiTransWorker632(int headerBufferSize, JIoEndpoint endpoint, Properties config) {
		_log.trace("new Instance " );
        this.endpoint = endpoint;
        request = new Request();
        inputBuffer = new InternalInputBuffer(request, headerBufferSize);
        request.setInputBuffer(inputBuffer);
        response = new Response();
        response.setHook(this);
        outputBuffer = new InternalOutputBuffer(response, headerBufferSize);
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);
        m_socUtil = new SocketUtil ();
        _config = config;
	}

	/*
	 * socket 실제 처리 부 ...
	 */
	public void process(Socket theSocket) throws IOException {
		
		// Set the remote address
		remoteAddr = null;
		remoteHost = null;
		localAddr = null;
		localName = null;
		remotePort = -1;
		localPort = -1;
		
		// Setting up the I/O
		this.socket = theSocket;
		String szip = theSocket.getInetAddress().getHostAddress();
		
		// Error flag
		error = false;
		keepAlive = true;
		
		// int keepAliveLeft = maxKeepAliveRequests;
		// int soTimeout = endpoint.getSoTimeout();
		// When using an executor, these values may return non-positive values
		int curThreads = endpoint.getCurrentThreadsBusy();
		int maxThreads = endpoint.getMaxThreads();
		if (curThreads > 0 && maxThreads > 0) {
		    // Only auto-disable keep-alive if the current thread usage % can be
		    // calculated correctly
		    if ((curThreads*100)/maxThreads > 75) {
		    //    keepAliveLeft = 1;
		    }
		}
		boolean keptAlive = false;
		int nReadTotal = 0;
		String szmsgin;
		String szmsgtr;
		
		// msg size
		int nLen = 5;
		int QQQ = 5;
		//server, client ip
		String clientip = null;
		String cserverip = null;
		String sys_time = null;
		sys_time_format = null;
		Gson gson = null;
		LinkedHashMap<String, Object> result_confirm_resp_map = null;
		LinkedHashMap<String, Object> socket_close = null;
		LinkedHashMap<String, Object> msg_type_map = null;
		//JSONObject result_confirm_resp = null;
		int  writetype = 0;
	
		// write type 을 지정합니다.
		//String temp = "file";
		String temp = System.getenv("HIPASS_WRITETYPE");
		if (temp != null && ( temp.equals("both") || temp.equals("BOTH") )) {
			writetype = 2;
		}
		else if ( temp != null && ( temp.equals("file") || temp.equals("FILE") )) {
			writetype = 0;
		}
		else if ( temp != null && ( temp.equals("kafka") || temp.equals("KAFKA") )) {
			writetype = 1;
		}
		else {
			writetype = 0;
		}
		
		KafkaProducerManager2 kafka = null;
		try {
			
			m_socUtil.setSocket(theSocket );
			
            if ((remoteAddr == null) && (socket != null)) {
                InetAddress inetAddr = socket.getInetAddress();
                if (inetAddr != null) {
                    remoteAddr = inetAddr.getHostAddress();
                }
            }
            
            //cserverip add date - 20190704 
            clientip = (((InetSocketAddress) theSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
            //cserverip = "121.137.90.97";
            cserverip = System.getenv("HIPASS_LOCAL_IP");
            sys_time_format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			_log.debug ( clientip + " --> " + cserverip +  " 1. socket open " + clientip + " --> " + cserverip);
			
			String realdata;

			kafka = new KafkaProducerManager2 ( _config );
			
			while ( true ) {

				// 1. header 5 자리 read.
				//    size 를 환경으로 빼야 하나 ?
				nReadTotal = m_socUtil.getClientMsg2(QQQ);

				// 1. header 가 5자리가 아니면, 다시 읽음. 이거는 ... 무한 loop 돌 수 있는데 ...
				if ( nReadTotal < QQQ ) {
					_log.info ( clientip + " --> " + cserverip + " 2.9 header read error : " +  nReadTotal );
					Thread.sleep(2000);
					break;
				}
				
				// 2. header string 을 받고, int 로 치환.
				szmsgin = m_socUtil.getCurMsg( );
				nLen = eBrotherUtil.getIntNumber( szmsgin );
				_log.debug ( clientip + " --> " + cserverip +  " 2.1 header read : socket read size - " +  nReadTotal + ", socket data - " + szmsgin  );

				// 문제가 있어 보임... 강제로 socket 중단
				if ( nLen == 0  ) {
					_log.info ( clientip + " --> " + cserverip +  " 2.1 header data error : socket read size - " +  nReadTotal + ", socket data - " + szmsgin  );
					break;
				}
				
				request.updateCounters();
				response.setContentLength(nLen);
				
				response.setBytesWritten(nLen);
				
				request.setBytesRead(nLen);
				
				
				// 3. payload 부문 데이터 획득. nLen 즉 header 에서 지정한 크기 만큼 데이터 획득함
				nReadTotal = m_socUtil.getClientMsg2(nLen);
				//    이거는 ... 문자변환인데 ... 환경변수로 빼야 하지 않을까 ??
				realdata = m_socUtil.getCurMsg( "EUC-KR").replaceAll("\n", "" );
				sys_time = sys_time_format.format(System.currentTimeMillis());
				// ADD USER ID + ADD ServerIP
				int k_start = realdata.indexOf("{");
				if ( k_start >= 0 ) {
					szmsgin = "{" + "\"clientip\":\"" + clientip + "\"," + "\"cserverip\":\"" + cserverip + "\"," + "\"sys_time\":\"" + sys_time + "\"," + realdata.substring(k_start + 1);
				}
				else {
					szmsgin = realdata; 
				}
				//////////////////////////////////////////////////////////////////////////////////////////

				if ( nLen != nReadTotal ) {
					_log.info ( clientip + " --> " + cserverip + " 2.X check network - " + nLen + " - " +  nReadTotal );
				}
				
				
				// 4. json check 합니다. 전문 정상 유무 확인
				// 
				String topic_succ = "hipass_succreal";
				String topic_fail = "hipass_failreal";
				String topic_cmdresp = "hipass_cmdrespreal";
				String topic;
				String msg_type;
				
				HiTransHeader hiheader = new HiTransHeader ();
				
				boolean bjsongood = false;
				boolean biscontrol = false;
				
				try {

					try {

						JSONObject jsonObject = (JSONObject) _jsonparser.parse(szmsgin);

						hiheader.setMSG_TYPE( (String ) jsonObject.get(HiHeaderConst.MSG_TYPE));
						hiheader.setDATA_TYPE( (String ) jsonObject.get(HiHeaderConst.DATA_TYPE));
						
						if ( "CONTROL".equals(hiheader.getDATA_TYPE()) 
								&& ( hiheader.getMSG_TYPE().indexOf("RESP") >= 0 || hiheader.getMSG_TYPE().indexOf("RESULT") >= 0 || hiheader.getMSG_TYPE().indexOf("LANE_LINE_TEST_DATA") >= 0)) {
							biscontrol = true;
							
						}
						
						hiheader.setSYS_TYPE( (String ) jsonObject.get(HiHeaderConst.SYS_TYPE));
						hiheader.setSEND_TIME( (String ) jsonObject.get(HiHeaderConst.SEND_TIME));
						hiheader.setIC_CODE( (String ) jsonObject.get(HiHeaderConst.IC_CODE));
						hiheader.setLANE_NO( (String ) jsonObject.get(HiHeaderConst.LANE_NO));
						hiheader.setBD_NAME( (String ) jsonObject.get(HiHeaderConst.BD_NAME));
						hiheader.setMAKER_NAME( (String ) jsonObject.get(HiHeaderConst.MAKER_NAME));
						hiheader.setINTERFACE_VERSION( (String ) jsonObject.get(HiHeaderConst.INTERFACE_VERSION));

						if ( hiheader.getMSG_TYPE() != null ) {
							
							// topic = topic_succ + hiheader.getMSG_TYPE().toLowerCase();
							topic = topic_succ;
							bjsongood = true;
						}
						else topic = topic_fail;
					}
					catch ( Exception e2 ) {
						topic = topic_fail;
					}

					StringBuffer buf = new StringBuffer();
					String hash_key = remoteAddr;
					
					buf.append( hiheader.getSYS_TYPE());
					buf.append( ".");
					buf.append( hiheader.getIC_CODE());
					buf.append( ".");
					buf.append( hiheader.getLANE_NO());
					buf.append( ".");
					buf.append( hiheader.getBD_NAME());
					buf.append( ".");
					buf.append( hiheader.getMAKER_NAME());
					//buf.append( ".");
					
					String hash_data = buf.toString();
					
					_header_data = hash_data;
					_log.info( "SET_Header_Socket : " + hash_data);
				
					if ( hiheader.getDATA_TYPE().equals("CONTROL") ) {
						
						if ( _controlHsocket_.containsKey(hash_data)) {
							
							Socket controlSocket = _controlHsocket_.get(hash_data);
							String controlIP = (((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");

							// i.e. control 명령을 실행하기 위해 admin 에서 접속한 socket 입니다.
							if ( controlSocket != theSocket ) {

								szmsgin = realdata;
							
								// 신규 메시지 생성. 길이 5byte + 원래 data size 임
								// 여기서 ... 현재 utf8 이기 때문에, euckr 로 바꾸어야  하나 ??
								// data 만들어서 전송함. 그것으로 끝 .
								byte [] payload = szmsgin.getBytes("EUC-KR");
								byte [] msgout = new byte [ 5 + payload.length];
								
								String szlen = eBrotherUtil.setString("" + payload.length, 5, '0');
								
								System.arraycopy( szlen.getBytes(), 0, msgout, 0, 5);
								System.arraycopy(payload, 0, msgout, 5, payload.length);
								
								//--test--//
								String decodedFromEuckr = new String (payload, "euc-kr");
								_log.info( clientip + " - "+ clientip + " : CONTROL MSG euc-kr - length ->" + payload.length);
								_log.info( clientip + " - "+ clientip + " : CONTROL MSG euc-kr - " +  hash_data + " ->" + decodedFromEuckr);
								byte [] utf8stringbuffer = decodedFromEuckr.getBytes("utf-8");
								String decodedFromutf8 = new String (utf8stringbuffer, "utf-8");
								_log.info( clientip + " - "+ clientip + " : CONTROL MSG utf-8 - length ->" + utf8stringbuffer.length);
								_log.info( clientip + " - "+ clientip + " : CONTROL MSG euc-kr - " +  hash_data + " ->" + decodedFromutf8);
								//--test--//

								try {
									m_socUtil.setClientMsg2( controlSocket, msgout, msgout.length );
									_log.info( clientip + " - "+ controlIP + " : CONTROL MSG SUCC - " +  hash_data + "\n" + new String (msgout) );
								}
								catch ( Exception e_send ) {
									// error msg ...
									_log.info( clientip + " - "+ controlIP + " : CONTROL MSG FAIL - " +  hash_data );
								}
								// msg 전송하는 것으로 종료됨.
								continue;
							}
							else {
								// msg 를 file 로 적재함.
								// 즉 아무것도 하지 않음 ...
								//제어응답의 응답 - 차로 적용 결과 수신 응답 20190704 MSG CREATE
								try {
									if(biscontrol)
									{
									result_confirm_resp_map = new LinkedHashMap<>();
									msg_type_map = new LinkedHashMap<>();
									gson = new Gson();
									
									result_confirm_resp_map.put("DATA_TYPE", hiheader.getDATA_TYPE());
									result_confirm_resp_map.put("SYS_TYPE", hiheader.getSYS_TYPE());
									result_confirm_resp_map.put("SEND_TIME", sys_time);
									result_confirm_resp_map.put("IC_CODE", hiheader.getIC_CODE());
									result_confirm_resp_map.put("LANE_NO", hiheader.getLANE_NO());
									result_confirm_resp_map.put("BD_NAME", hiheader.getBD_NAME());
									result_confirm_resp_map.put("MSG_TYPE", "RESULT_CONFIRM_RESP");
									result_confirm_resp_map.put("MAKER_NAME", hiheader.getMAKER_NAME());
									result_confirm_resp_map.put("INTERFACE_VERSION", hiheader.getINTERFACE_VERSION());
									result_confirm_resp_map.put("RESULT_CONFIRM_RESP", "");
									msg_type_map.put("RESP_DATETIME", hiheader.getSEND_TIME());
									msg_type_map.put("RESP_MSG_TYPE", hiheader.getMSG_TYPE());
									result_confirm_resp_map.put("RESULT_CONFIRM_RESP", msg_type_map);
									
									
									//result_confirm_resp = new JSONObject(result_confirm_resp_map);
									//szmsgtr = result_confirm_resp.toJSONString();
									
									szmsgtr = gson.toJson(result_confirm_resp_map);
									

										// 신규 메시지 생성. 길이 5byte + 원래 data size 임
										// 여기서 ... 현재 utf8 이기 때문에, euckr 로 바꾸어야  하나 ??
										// data 만들어서 전송함. 그것으로 끝 .
										byte [] payload = szmsgtr.getBytes(Charset.forName("euc-kr"));
										byte [] msgout = new byte [ 5 + payload.length];
										
										String szlen = eBrotherUtil.setString("" + payload.length, 5, '0');
										
										System.arraycopy( szlen.getBytes(), 0, msgout, 0, 5);
										System.arraycopy(payload, 0, msgout, 5, payload.length);

										try {
											m_socUtil.setClientMsg2( controlSocket, msgout, msgout.length );
											_log.info( clientip + " - "+ controlIP + " : CONTROL MSG_CONFIRM SUCC - " +  hash_data + "\n" + new String (msgout) );
										}
										catch ( Exception e_send ) {
											// error msg ...
											_log.info( clientip + " - "+ controlIP + " : CONTROL MSG_CONFIRM FAIL - " +  hash_data );
										}
										// msg 전송하는 것으로 종료됨.
										//continue;
									}
								}
								catch (Exception t)
								{
									_log.info(clientip + " - "+ controlIP + " : JSON_RESP CREATE FAIL " +  hash_data);
								}
								_log.info( clientip + " - " + controlIP + " : CONTROL MSG GET - "+ hash_data );
							}
						}
						else {
						
							_log.info( clientip + " - *** : CONTROL CHECK PLS - "+ hash_data );
						
						}
						//////////////////////////////////////////////////////////////////
					}
					// control 이 아닌 경우에 해당함 ... 그냥 write 함.

					// 통상 끊어지면, 바로 다시 연결이됨.
					// 따라서, ... 삭제하는 것 보다는 그냥 두는 것이 좋을 것으로 판단됨.
					// 메모리 leak 이 걱정되기는 하지만, 한정되 수의 heah_data 로 접속이 됨에 따라 유지하는 것도 나쁘지 않을 것으로 보임. 
					 
					_controlHsocket_.put( hash_data, theSocket );
					_connectchekerst.remove(hash_data);
					////////////////////////////////////////////////////////////////

				}
				catch ( Exception e ) {
					topic = topic_fail;
				}

				if ( writetype == 0 || writetype == 2 ) {

					if ( bjsongood ) {
						
						_payload_succ.info(szmsgin);
					}
					else {
						
						_payload_fail.info(szmsgin);
					}
					
				}
				
				
				if ( writetype == 2 || writetype == 1 ) {
					
					_log.info( clientip + " HIPASSDATA [" + topic + "] " + szmsgin );
					
					try {
						if ( kafka == null ) kafka = new KafkaProducerManager2 ( _config );
						kafka.send(topic, szmsgin);
						kafka.flush();
						
						//제어 명령 응답일경우 토픽 변경하여 처리 
						if ( biscontrol == true ) {
							topic = topic_cmdresp;
							kafka.send(topic, szmsgin);
							kafka.flush();
						}

						_log.debug( clientip + " kafka succ [" + topic + "] : " + szmsgin );
					}
					catch ( Exception e ) {
						
						if ( bjsongood ) {
							_payload_succ.info(szmsgin);
						}
						else {
							
							_payload_fail.info(szmsgin);
						}
						
						_log.debug( clientip + " kafka fail [" + topic + "] : " + szmsgin );
						try { kafka.close(); } catch ( Exception e_kafka ) {}
						kafka = null;
					}
				}
				
			}
				
		}
		catch (Exception socex) {
			_log.debug(clientip + " err " + socex.getMessage());
		}
	    finally {
	    	try {
	    		//_log.info( "무조건 타야 하는데 ㅡ.,ㅡ" + _header_data);
	    		if ( _header_data != null ) 
	    		{
					sys_time = sys_time_format.format(System.currentTimeMillis());
					_connectchekerst.put(_header_data,_header_data+"."+sys_time+".0");
	    			_controlHsocket_.remove( _header_data );	
	    		}

	    	}
	    	catch ( Exception e ) {
	    		
	    	}
	    	try { if ( m_socUtil != null ) m_socUtil.close (); } catch ( Exception e_f2) {}
			try { kafka.close(); } catch ( Exception e_kafka ) {}
			kafka = null;
	    }
		
	}

    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse() {
    	
    	_log.debug( "PROCESS");
    }
    
    /**
     * Set the associated adapter.
     *
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }


    /**
     * Get the associated adapter.
     *
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }

   
    // ----------------------------------------------------- ActionHook Methods


    /**
     * Send an action to the connector.
     *
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    public void action(ActionCode actionCode, Object param) {

        if (actionCode == ActionCode.ACTION_COMMIT) {
            // Commit current response

            if (response.isCommitted())
                return;

            // Validate and write response headers
            prepareResponse();
            try {
                // outputBuffer.commit();
            	outputBuffer.flush();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_ACK) {

            // Acknowlege request

            // Send a 100 status back if it makes sense (response not committed
            // yet, and client specified an expectation for 100-continue)

            if ((response.isCommitted()) || !expectation)
                return;

            inputBuffer.setSwallowInput(true);
            try {
                outputBuffer.sendAck();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_CLIENT_FLUSH) {

            try {
                outputBuffer.flush();
            } catch (IOException e) {
                // Set error flag
                error = true;
                response.setErrorException(e);
            }

        } else if (actionCode == ActionCode.ACTION_CLOSE) {
            // Close

            // End the processing of the current request, and stop any further
            // transactions with the client

            try {
                outputBuffer.endRequest();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_RESET) {

            // Reset response

            // Note: This must be called before the response is committed

            outputBuffer.reset();

        } else if (actionCode == ActionCode.ACTION_CUSTOM) {

            // Do nothing

        } else if (actionCode == ActionCode.ACTION_START) {

            started = true;

        } else if (actionCode == ActionCode.ACTION_STOP) {

            started = false;
            
        } else if (actionCode == ActionCode.ACTION_REQ_HOST_ADDR_ATTRIBUTE) {

            if ((remoteAddr == null) && (socket != null)) {
                InetAddress inetAddr = socket.getInetAddress();
                if (inetAddr != null) {
                    remoteAddr = inetAddr.getHostAddress();
                }
            }
            request.remoteAddr().setString(remoteAddr);

        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_NAME_ATTRIBUTE) {

            if ((localName == null) && (socket != null)) {
                InetAddress inetAddr = socket.getLocalAddress();
                if (inetAddr != null) {
                    localName = inetAddr.getHostName();
                }
            }
            request.localName().setString(localName);

        } else if (actionCode == ActionCode.ACTION_REQ_HOST_ATTRIBUTE) {

            if ((remoteHost == null) && (socket != null)) {
                InetAddress inetAddr = socket.getInetAddress();
                if (inetAddr != null) {
                    remoteHost = inetAddr.getHostName();
                }
                if(remoteHost == null) {
                    if(remoteAddr != null) {
                        remoteHost = remoteAddr;
                    } else { // all we can do is punt
               //         szip.recycle();
                    }
                }
            }
            // szip.setString(remoteHost);

        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_ADDR_ATTRIBUTE) {

            if (localAddr == null)
               localAddr = socket.getLocalAddress().getHostAddress();

            request.localAddr().setString(localAddr);

        } else if (actionCode == ActionCode.ACTION_REQ_REMOTEPORT_ATTRIBUTE) {

            if ((remotePort == -1 ) && (socket !=null)) {
                remotePort = socket.getPort();
            }
            request.setRemotePort(remotePort);

        } else if (actionCode == ActionCode.ACTION_REQ_LOCALPORT_ATTRIBUTE) {

            if ((localPort == -1 ) && (socket !=null)) {
                localPort = socket.getLocalPort();
            }
            request.setLocalPort(localPort);

        } else if (actionCode == ActionCode.ACTION_REQ_SET_BODY_REPLAY) {
         
        }
    }
}

