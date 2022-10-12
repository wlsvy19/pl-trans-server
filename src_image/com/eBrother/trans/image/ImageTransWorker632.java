package com.eBrother.trans.image;

import com.eBrother.trans.image.model.PlReq;
import com.eBrother.trans.image.util.CarBcdConv;
import com.eBrother.trans.image.util.MultipartFormPost;
import com.eBrother.trans.kafka.KafkaProducerManager2;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.UtilExt;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.*;
import org.apache.coyote.http11.InternalInputBuffer;
import org.apache.coyote.http11.InternalOutputBuffer;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.SSLSupport;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


public class ImageTransWorker632 implements ActionHook, ImageTransConst {
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
	protected static Logger _log = Logger.getLogger(ImageTransWorker632.class.getName());
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
    protected String[] compressableMimeTypes = { "text/html", "text/xml", "text/plain" };
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
    SocketUtil _socUtil = null;
    String _szinbound = "";
	String _szpattern_server = "";
	String _szpattern_meta = "";
	String _szoutbound = "";
	String _diripwrite = "";
	String ENCODE = "KSC5601";

	// JSONParser _jsonparser = new JSONParser();

	SimpleDateFormat sys_time_format;

    ImageTransHelper _transhelper = null;

    ImageCacheJnaHelper _cacheHelper_ = null;

    CarBcdConv bcdConv = CarBcdConv.getInstance ();

    private short plHeadLen;
    private long sys_time_exec_recv;

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
    	
    	if ( szkey.equals("inbound"))  	_szinbound = szdata;
    	else if ( szkey.equals("pattern_server"))  	_szpattern_server = szdata;
    	else if ( szkey.equals("pattern_meta"))  	_szpattern_meta = szdata;
    	else if ( szkey.equals("outbound"))  	_szoutbound = szdata;
    	else if ( szkey.equals("encode")) ENCODE = szdata;
    	else if ( szkey.equals("diripwrite")) _diripwrite = szdata;

    }

    HashMap<String, Object> _attributes = null;

    String _img_svr_ip = "127.0.0.1";
    int	   _img_svr_port = 0;
    static private CarBcdConv _carBcdHelper = CarBcdConv.getInstance();


    public String getProperty(String name) {
        return (String)this._attributes.get(name);
    }

	public ImageTransWorker632(int headerBufferSize, JIoEndpoint endpoint, Properties config, String svr_ip, String svr_port ) {
		
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
        _socUtil = new SocketUtil ();
        _config = config;

        _transhelper = ImageTransHelper.getInstance();

         _img_svr_ip = svr_ip;
	         try {
       		     _img_svr_port = Integer.parseInt( svr_port ); 
         	}
		catch ( Exception e ) {
		     _img_svr_port = 5001;
		}

	}


	/*
	 * socket 실제 처리 부 ...
	 */
	public void process(Socket theSocket) throws IOException {

	    String keyControlSocket;
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
        keyControlSocket = szip + "-" + theSocket.getPort();

		// Error flag
		error = false;
		keepAlive = true;

		/////////////////////////////////////////////////////////////////////////
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
		int nLen = _PL_HEAD_LEN_V2;

		//server, client ip
		String clientip = null;
		String cserverip = null;
		String sys_time = null;
		sys_time_format = null;
		Gson gson = null;
		LinkedHashMap<String, Object> result_confir_resp_map = null;
		LinkedHashMap<String, Object> socket_close = null;
		LinkedHashMap<String, Object> msg_type_map = null;

		int  writetype = 0;

		String temp = System.getenv("IMAGE_WRITETYPE");
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

		String run_step = "step . 0 ";
		
		try {
			
			_socUtil.setSocket(theSocket );
			
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
			
            _log.info ("[BOSU_DATA_TEST] START == 01(cserverip)==" + cserverip);
            
            
			String realdata;

//			향후 kafka 를 위해서 살려 둠
//			kafka = new KafkaProducerManager2 ( _config );
			
			byte msg_stx;
			short msg_seq;
			int msg_len = 0;
			short msg_icCode;
			short msg_workNo;
			short msg_cmdtype = 0;

            long sys_time_exec_start = 0L;
            long sys_time_exec_recv = 0L;
            long sys_time_exec_imgcheck = 0L;
            long sys_time_exec_blcheck = 0L;
            long sys_time_exec_end = 0L;
            
			while ( true ) {

				run_step = "step 01. header read ";

                // plHeadLen = _PL_HEAD_LEN_V2;

				// 1. header 7 자리 read.
				nLen = _socUtil.getClientMsg2( _PL_HEAD_LEN_V2 );

                sys_time_exec_imgcheck = 0L;
                sys_time_exec_blcheck = 0L;
                sys_time_exec_end = 0L;
                sys_time_exec_recv = 0L;
                sys_time_exec_start = System.nanoTime();
                
				nReadTotal += nLen;
				
//				_log.info ("[BOSU_DATA_TEST] START == 01(nReadTotal)==" + nReadTotal);
				
				
				// 1. header 가 5자리가 아니면, 다시 읽음. 이거는 ... 무한 loop 돌 수 있는데 ...
				if ( nLen < _PL_HEAD_LEN_V2 ) {

				    if ( nLen == 0 ) {
				        // i.e client 에서 socker 을 close 함
//                        _log.info ( clientip + " --> " + cserverip + " : socket closed " );
                        break;
                    }
					_log.info ( clientip + " --> " + cserverip + " 2.9 header read error : " +  nLen + " != " +  _PL_HEAD_LEN_V2 );
					Thread.sleep(500);
					break;
				}
				
				run_step = "step 02. header data set";
				final ByteBuffer bufbyte = ByteBuffer.wrap( _socUtil.getCurMsgBytes());
				
				run_step = "step 03. header byte to data";
				
				msg_stx = bufbyte.get();
				msg_seq = (bufbyte.getShort());

				msg_len = (bufbyte.getInt());

                msg_icCode = (bufbyte.getShort());
                msg_workNo = (bufbyte.getShort());
                msg_cmdtype = (bufbyte.getShort());

                run_step = "step 04. payload read : " + msg_len;
				
				nLen = _socUtil.getClientMsg2( _PL_TAIL_LEN_ + msg_len);

//                _log.info( szip + " msg_cmdtype = " + msg_cmdtype
//                        + " " + String.format( "0x%04X", msg_seq ) + " = " + msg_seq
//                        + " " + String.format( "0x%04X", msg_len ) + " , msg_len = " + msg_len
//                );

                sys_time_exec_recv = System.nanoTime();
                
                if ( nLen < ( _PL_TAIL_LEN_ + msg_len )) {
					
					_log.info ( clientip + " --> " + cserverip + " 2.10 payload read error : "  +  nLen + " != " +  ( _PL_TAIL_LEN_ + msg_len ) );
					Thread.sleep(2000);
					break;
				}
				
				// 최적화가 되어야 하는 부분이다.

                PlReq plreq = new PlReq();
                plreq.setStx(msg_stx);
                plreq.setSeq(msg_seq);
                plreq.setLenPayload(msg_len);
                plreq.setIcCore(msg_icCode);
                plreq.setWorkNo(msg_workNo);
                plreq.setCmdType(msg_cmdtype);

                byte [] msgout = null;

                if ( msg_cmdtype !=  _PL_MSG_IMG_RECCHECK_REQ_ ) {

                    if ( msg_cmdtype !=  _PL_MSG_IMG_INIT_REQ_) {
                    	
                    	if( msg_cmdtype == _PL_MSG_BOSU_DATA_)
                        {
                    		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setStx)==" + plreq.getStx());
                    		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setSeq)==" + plreq.getSeq());
                    		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setLenPayload)==" + plreq.getLenPayload());
                    		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setIcCore)==" + plreq.getIcCore());
                    		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setWorkNo)==" + plreq.getWorkNo());
                    		 
                    		 
                             final ByteBuffer bufbyte2 = ByteBuffer.wrap(_socUtil.getCurMsgBytes());
                             byte msg_payload[] = new byte[msg_len];
                             bufbyte2.get(msg_payload, 0, msg_len);

                             _log.info(szip + " pos = " + bufbyte2.remaining() + " , " + String.format("0x%04X", msg_icCode) + " = " + msg_icCode
                                     + " " + String.format("0x%04X", msg_workNo) + " = " + msg_workNo
                                     + " " + String.format("0x%04X", msg_cmdtype) + " = " + msg_cmdtype
                             );
                    		 
                    		 
                    		 msgout = _transhelper.run_core ( szip, plreq );
                        }
                    	else
                    	{
                    		_controlHsocket_.put(keyControlSocket, theSocket);
//                        	_log.info("socket checker put : " + keyControlSocket);
                    	}

                    }


                    byte [] carBcdNo = new byte [] { 0x00, 0x00, 0x00, 0x00, 0x00 };
                    plreq.setCarNo(  carBcdNo );

                    // 이미지는 너무 커서 별도 처리합니다.
                    run_step = "step 05. payload data set ";
                    final ByteBuffer bufbyte2 = ByteBuffer.wrap(_socUtil.getCurMsgBytes());
                    run_step = "step 06. payload byte to data";
                    byte msg_payload[] = new byte[msg_len];
                    bufbyte2.get(msg_payload, 0, msg_len);

//                    _log.info(szip + " pos = " + bufbyte2.remaining() + " , " + String.format("0x%04X", msg_icCode) + " = " + msg_icCode
//                            + " " + String.format("0x%04X", msg_workNo) + " = " + msg_workNo
//                            + " " + String.format("0x%04X", msg_cmdtype) + " = " + msg_cmdtype
//                    );

                    // 이미지 재인식 - 이미지 file 전송됨에 따라 ... log 출력 제외.
//                    if (msg_len < 500)
//                        _log.info(szip + ", msg_type = " + msg_cmdtype + " - 11payload = " + UtilExt.print(msg_payload));
//                    else _log.info(szip + ", msg_type = " + msg_cmdtype + " ");
                    ////////////////////////////////////////////////////////////////////////
                    plreq.setPayload(msg_payload);
                    msgout = _transhelper.run_core ( szip, plreq );

                    sys_time_exec_imgcheck = sys_time_exec_recv;
                    sys_time_exec_blcheck = System.nanoTime();
                }
                else {

                    byte [] carBcdNo = new byte [] { (byte)255, (byte)255, (byte)255, (byte)255, (byte)255 };
                    plreq.setCarNo(  carBcdNo );

                    // 이미지는 너무 커서 별도 처리합니다.
                    run_step = "step 05. payload data set ";
                    final ByteBuffer bufbyte2 = ByteBuffer.wrap(_socUtil.getCurMsgBytes());
                    run_step = "step 06. payload byte to data";

                    int imgSerial = bufbyte2.getInt();
                    int imgSize = bufbyte2.getInt ();

                    plreq.setImgSerial( imgSerial );

                    byte msg_payload[] = new byte[bufbyte2.remaining() - 1];
                    bufbyte2.get(msg_payload, 0, msg_payload.length);

                    String carNo =  null;

                    try {

                        sys_time = sys_time_format.format(System.currentTimeMillis());


//                        FileUtils.writeByteArrayToFile(new File("/Users/demo860/app/dev/hipass/data/image-" + sys_time + "-" + szip
//                                + "-" + msg_payload.length + ".jpg"), msg_payload);

                        _log.info( szip + " - msg_type = " + msg_cmdtype + " - msg len = " + msg_len + ", img size = " + imgSize + ", real size = "  + msg_payload.length );

                        // {'lap': '0.090', 'size': '2048:1300', 'port': '5001', 'platenum': '충남83바2212'}
                        String imgCheckData = MultipartFormPost.call( _img_svr_ip, _img_svr_port, msg_payload, "null", null, clientip);
                        ObjectMapper mapper = new ObjectMapper();

                        // json parsing ??
                        JsonNode root = mapper.readTree(imgCheckData);
                        carNo = root.get("platenum").getTextValue();

//                        _log.info ( carNo + " => " + imgCheckData );

                        // BCD format 처
                        try {
                            if ( carNo != null && carNo.length() > 4 ) plreq.setCarNo( bcdConv.convCarNoBcdFormat(carNo) );
                        }
                        catch ( Exception e_2) {

                        }

                    }
                    catch ( Exception e ) {

                    }

                    sys_time_exec_imgcheck = System.nanoTime();

                    msgout = _transhelper.run_core ( szip, plreq );
                    sys_time_exec_blcheck = System.nanoTime();
                }

                if ( msgout != null ) {
//                    _log.info( szip +  " out = " + UtilExt.print( msgout ));
                    run_step = "step 07. ready resp";
                    _log.info("cmdtype = " + plreq.getCmdType());
                    _socUtil.setClientMsg2(theSocket, msgout, msgout.length);
                }
                else {

                    run_step = "step 07. just stop";
                    _log.info("cmdtype1 = " + plreq.getCmdType());

                }

                sys_time_exec_end = System.nanoTime();
                String carNoPlain = null;
                try {
                    carNoPlain = _carBcdHelper.convBcdFormatToCarNo(plreq.getCarNo());
                }
                catch ( Exception e ) {

                }

//                _log.info ( "exec run time check , client ip = " + clientip + ", cmd type = " + msg_cmdtype + ", payload len = " + msg_len + " "
//                        + UtilExt.print(plreq.getCarNo()) + " = "
//                        + carNoPlain
//                        + ", data recv," + (( sys_time_exec_recv - sys_time_exec_start ) / 1000000 )
//                        + ", img check," + (( sys_time_exec_imgcheck - sys_time_exec_recv ) / 1000000 )
//                        + ", bl check," + (( sys_time_exec_blcheck - sys_time_exec_imgcheck ) / 1000000 )
//                        + ", data send," + (( sys_time_exec_end - sys_time_exec_blcheck ) / 1000000 )
//                );
			}

		}
		catch (Exception socex) {
			_log.debug(clientip + " step = " + run_step + ", err " + socex.getMessage());
			socex.printStackTrace();
		}
	    finally {
	    	try { if ( _socUtil != null ) _socUtil.close (); } catch ( Exception e_f2) {}
            _controlHsocket_.remove( keyControlSocket );
//            _log.info( "socket checker remove : " + keyControlSocket );
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

