package com.eBrother.app.impl;

import com.eBrother.util.FileUtil;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.eBrotherUtil;
import com.eBrother.wutil.UtilDecode;
import org.apache.coyote.*;
import org.apache.coyote.http11.*;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.apache.tomcat.util.net.SSLSupport;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.regex.Pattern;


public class TransWorker632 implements ActionHook, ILogConst {

	
	final static int TR_PRO_V1 = 2;
	final static int TR_PRO_V2 = 3;
	final static int TR_PRO_V3 = 5;
	
	 // --Instance Variables
	static Hashtable<String, String []> m_hstat = new Hashtable<String, String []> ();
	
	protected static Logger log = Logger.getLogger(TransWorker632.class.getName());
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


    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    protected String server = null;

    // ------------------------------------------------------------- Properties

    /** Get the request associated with this processor.
     *
     * @return The request
     */
    public Request getRequest() {
        return request;
    }
    
    SocketUtil m_socUtil = null;
    String m_szinbound = "";
	String m_szpattern_server = "";
	String m_szpattern_meta = "";
	String m_szoutbound = "";
	
	String m_diripwrite = "";
	
	StatWorker m_statwork = StatWorker.getInstance();
	
	String ENCODE = "KSC5601";
	
    public void init ( String szkey, String szdata ) {
    	
    	if ( szkey.equals("inbound"))  	m_szinbound = szdata;
    	else if ( szkey.equals("pattern_server"))  	m_szpattern_server = szdata;
    	else if ( szkey.equals("pattern_meta"))  	m_szpattern_meta = szdata;
    	else if ( szkey.equals("outbound"))  	m_szoutbound = szdata;
    	else if ( szkey.equals("encode")) ENCODE = szdata;
    	else if ( szkey.equals("diripwrite")) m_diripwrite = szdata;
    	
    }

    static public Hashtable<String, String []> get_status () {
    	
    	return m_hstat;
    }
    
	public TransWorker632(int headerBufferSize, JIoEndpoint endpoint) {
	
		log.trace("new Instance " );
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
        
        // Cause loading of HexUtils
        //int foo = HexUtils.DEC[0];
	
	}
	   
	public void process(Socket theSocket)
            throws IOException {
		
		// log.debug( "PROCESS");
		
		ParserWorker pw = null;
		
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
		eBrotherUtil ebUtil = eBrotherUtil.getInstance();
		
		String filename;
		long nFileSize;
		String sfolder;
		int ncurline;
		String szoutfile = "";
		String szmsgout;
		OutputStreamWriter respTP = null;
		OutputStream respBW = null;
		long ltimestamp =System.currentTimeMillis();
		
		String strtmpFileName = null;
		String strtmpFileExt = null;
		
		String szfilenamereal = null;
		boolean bisreal = false;
		boolean bis_transBIN = false;
		int nkkkk = 99999999;
		String [] arstat;

		arstat = new String []{ "N", "", "", "", "", "" };
		
		int nprotocoltype = 1;
		int i;
		boolean bsended = false;

		// Parsing the request header
		try {
			
			log.trace("Process Start " );
		
			m_socUtil.setSocket(theSocket );
			
		   request.setStartTime( ltimestamp );
		
			nReadTotal = m_socUtil.getClientMsgLine();
			szmsgin = m_socUtil.m_strMsgIn;
			
			log.trace(" msg in : " + szmsgin );
			
			i = ebUtil.getDelimitNum(szmsgin, "|");
			
			String szprojectname;
			String szREAL;
			String szTransType;
			
			if ( i == TR_PRO_V1 ) {
				// 첫번째 ... filename + size 전송.
				szprojectname = "";	
				filename = eBrotherUtil.getDelimitData(szmsgin, "|", 1);
				nFileSize = eBrotherUtil.getNumber(eBrotherUtil.getDelimitData(szmsgin, "|", 2));
				szREAL = "N";
				szTransType = "B";
				nprotocoltype = TR_PRO_V1;
			}
			else if ( i == TR_PRO_V2 ) {
				
				szprojectname = eBrotherUtil.getDelimitData(szmsgin, "|", 1);	
				filename = eBrotherUtil.getDelimitData(szmsgin, "|", 2);
				nFileSize = eBrotherUtil.getNumber(eBrotherUtil.getDelimitData(szmsgin, "|", 3));
				szREAL = "N";
				szTransType = "B";
				nprotocoltype = TR_PRO_V2;
			}
			else {
				filename = eBrotherUtil.getDelimitData(szmsgin, "|", 1);
				nFileSize = eBrotherUtil.getNumber(eBrotherUtil.getDelimitData(szmsgin, "|", 2));
				
				szprojectname = eBrotherUtil.getDelimitData(szmsgin, "|", 3);
				szREAL = eBrotherUtil.getDelimitData(szmsgin, "|", 4);
				szTransType = eBrotherUtil.getDelimitData(szmsgin, "|", 5);
				nprotocoltype = TR_PRO_V3;
			}

			if ( szTransType.length() > 0 ) {
				bis_transBIN = true;
			}
			
			if ( szREAL.equals("Y")) bisreal = true;
			else bisreal = false;
			
			sfolder = m_szinbound;

			log.trace("Write target : " + sfolder + " : " + szmsgin );
			
			if ((szprojectname != null) && (szprojectname.length() > 0)) {
				sfolder = m_szinbound + VFS + szprojectname;
				File sfile = new File(sfolder);
				if (!sfile.exists()) sfile.mkdirs();
				
				if ( m_diripwrite != null && m_diripwrite.equals("ip")) sfolder = sfolder + VFS + szip;
				sfile = new File(sfolder);
				if (!sfile.exists()) sfile.mkdirs(); 
			}
			else  {
				
				if ( m_diripwrite != null && m_diripwrite.equals("ip")) sfolder = m_szinbound + VFS + szip;
		
				File sfile = new File(sfolder);
				if (!sfile.exists()) sfile.mkdirs();
			}

			if (! filename.trim().equals(""))   {
				
				strtmpFileName = filename;
				strtmpFileExt = "";
				if (filename.indexOf(".") >= 0) {
					strtmpFileName = filename.substring(0, filename.lastIndexOf("."));
					strtmpFileExt = filename.substring(filename.lastIndexOf("."), filename.length());
				}
				
				filename = strtmpFileName + "_" + szip + "_" + nFileSize + strtmpFileExt; // + ".gz";
				szfilenamereal = strtmpFileName + "_" + szip + "_" + nkkkk + strtmpFileExt; // + ".gz";
				
			}			
			
			log.debug(  " path : " + sfolder + " file : " + filename + " File Size : " + nFileSize);
			
			szmsgout = "";
			
			// 실시간으로 보내다, 시스템이 꺼져서, 시간대가 지나가 버린 파일이 존재한다.
			// 그런 경우, batch 형식으로 파일이 전송된다.
			// batch 로 보낼 때는 filesize 가 원래 size 이고, realtime 은 filesize 를 결정할 수 없기 때문에 999999 로 한다.
			// 따라서 ...
			// file size 가 999999 가 있는지를 선 확인하고, 있으면 수집한 line 수 만큼 skip 한다.
			// 지금은, 파일을 읽어서 skip 하지만, file 이 없는 경우, db 에서 수집 여부를 다시 확인해야 한다.
			
			ncurline = 0;
			
			if ( nprotocoltype != TR_PRO_V1 ) {
				
				if ( eBrotherUtil.IsFileExist(sfolder + VFS + szfilenamereal)) {
	
					
					ncurline = 0;
					szoutfile = sfolder + VFS + szfilenamereal;
					ncurline = FileUtil.getflines ( szoutfile);
					if ( ncurline > 0 ) {
						// szmsgout = "EX|" + ncurline + "\n";
						szmsgout = "EX|" + ncurline + "|" + FileUtil.getFileSize(szoutfile ) + "|\n";
					}
					else ncurline = 0;
				}
				else {
					// 확인한다...
					szoutfile = sfolder + VFS + filename;
					
					ncurline = 0;
	
					if (eBrotherUtil.IsFileExist(szoutfile)) {
						
						ncurline = FileUtil.getflines ( szoutfile);
						if ( ncurline > 0 ) {
							szmsgout = "EX|" + ncurline + "|" + FileUtil.getFileSize(szoutfile ) + "|\n";
						}
						else ncurline = 0;
					}
				}
			}
			else {
				
				szoutfile = sfolder + VFS + filename;
				ncurline = 0;
			}
			
			log.debug(  "Target File Check : (" + nFileSize + ") (" + FileUtil.getFileSize(szoutfile ) + ") " + szoutfile );

			if ( nprotocoltype != TR_PRO_V1 && nFileSize == FileUtil.getFileSize(szoutfile )) {
				
				log.debug(  "Target File Received : (" + nFileSize + ") " + szoutfile );
				szmsgout = "SENDED\n";
				bsended = true;
			}
			else {

				if ( ncurline == 0 ) {
					szmsgout = "OK\n";
				}
	
				// binary 로 전송할 때와 batch 로 전송하는 방법이 다르다...
				// binary : batch 전송일때 사용, text : real time 일때 ...
				if ( nprotocoltype == TR_PRO_V1 ) {
					bis_transBIN = true;
				}
				
				if ( ! bis_transBIN ) {
						respTP = FileUtil.getOSW( szoutfile, true, ENCODE );
				}
				else {
					
					// batch 로 넘어올때는, 처음부터 다시 저장하자...
					// 가장 확실한 방법이다.
					respBW = FileUtil.getOBW( szoutfile, false, ENCODE );
				}
	
				// StatHelper 에서 정의해야 되나 ??
				// 어렵다...
				// 않좋은 코드이기는 하지만, 일단 여기다 넣는다. StatHelper 에도 있다.
 
				if ( m_hstat.containsKey( szoutfile )) {
					arstat = m_hstat.get(szoutfile);
				}
				else {
					
					arstat[ST_REC_ETIME] = "0";
			    	arstat[ST_REC_WCNT] = "0";
			    	arstat[ST_REC_WSIZE] = "0";
					m_hstat.put( szoutfile, arstat );

				}
				
				FileUtil.createFile ( szoutfile + ".run");
				// int nexecnum = m_statwork.setFile ( EB_TRANS_START, ltimestamp, szip, szoutfile );
				//////// PARSER ...
				pw = null;
	
				if ( bisreal ) {
					
					try {
						
						log.trace( "[TransWorker632] ParserWorker Gen Real - " + szoutfile + ", " + m_szoutbound );
						
						String [] args = new String [] { m_szpattern_server,  m_szpattern_meta, szoutfile, m_szoutbound };
						pw = new ParserWorker ( this, args);
						pw.setSkipLine(ncurline);
						Thread th = new Thread ( pw );
						th.start();
		
					}
					catch ( Exception e ) {
						log.trace ( "Err ParserWorker Gen ");
						e.printStackTrace();
					}
					log.debug("Parser Thread launched ");
					//////////////////////////////////////////////////////////////////////////////////////
				} 
				else {
					log.trace( "[TransWorker632] ParserWorker Gen Batch - " + szoutfile + ", " + m_szoutbound );
				}
			}

			boolean ttt = false; 
			
			if ( nprotocoltype == TR_PRO_V1 || nprotocoltype == TR_PRO_V2 ) {
				szmsgout = "OK\n";
				ttt =  m_socUtil.setClientMsg( null, szmsgout);
			}

		    int nline = 0;
		    int nsize = 0;


            int nReadCnt = 0;
            int nReadUnit = 1024;
            int nreadtotal2 = 0;
            int nfilesize2 = ( int ) nFileSize;
            boolean berror = false;

            log.debug("nprotocoltype : " + nprotocoltype + ", Send Msg : " + ttt + " - " + szmsgout + ", target file : " + szoutfile);
            
            try {

		    	while ( ! bsended  ) {

		    		if ( nprotocoltype == TR_PRO_V1 || nprotocoltype == TR_PRO_V2 ) {
		    			
		    			// 기존 protocol 이다.
		    			// 들어 온데로, 그냥 적재 ...
		    			if ( (nreadtotal2 + nReadUnit) >= nfilesize2) {
		    				nReadUnit = nfilesize2 - nreadtotal2;
		    			}

		    			// log.debug("read try : " + nReadUnit );
		    			nReadCnt = m_socUtil.getClientMsg(nReadUnit);
		    			
		    			// log.debug("read real : " + nReadCnt + ", total : " + nreadtotal2);
		    			if (nReadCnt <= 0 ) break;

	      				try {
	      					respBW.write( m_socUtil.m_byteMsgIn, 0, nReadCnt);
	      				}
				    	catch ( Exception e ) {
				    		e.printStackTrace();
				    		berror = true;
				    		try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
				    		break;
				    	}

	      				nreadtotal2 += nReadCnt;
						if (nreadtotal2 >= nfilesize2) {
							break;
						}	      				
		    		}
		    		else {
		    			
			    		//log.debug("READY Read ...\n");
				    	nline = m_socUtil.getClientMsgLine();
		      			szmsgin = m_socUtil.m_strMsgIn;
	
		      			if ( m_socUtil.m_strMsgIn == null ) {
		      				log.trace("Client disconnected");
		      				pw.stop();
		      				pw = null;
		      				break;
		      			}
	
		      			// 신버전 : binary trans 를 지원함.
		      			// 신버젼에서는 binary 를 byte 만큼 읽고, 해당 bytes 를 hex 로 바꾸어서 전송.
		      			// 구 버젼은 하나의 Line 을 전송. 구버전 호환성 때문에 ...
		      			if ( ! bis_transBIN ) {
					    	try { 
					    		respTP.write(szmsgin);
					    		respTP.write ("\n");
					    		respTP.flush();
					    		nline++;
					    		nsize += szmsgin.length();
					    	}
					    	catch ( Exception e ) {
					    		// e.printStackTrace();
					    		berror = true;
					    		try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
					    		break;
					    	}
		      			}
		      			else {
		      				
		      				byte [] bmsgin = UtilDecode.hexToByteArray(szmsgin);
	
		      				try {
		      					respBW.write( bmsgin);
		      				}
					    	catch ( Exception e ) {
					    		// e.printStackTrace();
					    		berror = true;
					    		try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
					    		break;
					    	}
		      			}
		    		}

	      			arstat[ST_REC_WCNT] = "" + nline;
			    	arstat[ST_REC_WSIZE] = "" + nsize;
		    	}
		    	
			    log.debug("read real : " + nreadtotal2 + ", file size : " + nfilesize2);
			    
			    if ( nprotocoltype == TR_PRO_V1 || nprotocoltype == TR_PRO_V2 ) {		    
				    if ( nreadtotal2 == nfilesize2 ) {
				    	szmsgout = "END";
				    }
				    else {
				    	szmsgout = "FAIL";
				    }
				    
				    log.debug(" final msg : " + szmsgout);
				    ttt =  m_socUtil.setClientMsg( null, szmsgout);		    
			    }

            }
		    catch ( Exception e_n) {
		    	berror = true;
		    	try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
		    }
	
		    if ( bisreal ) {
			    respTP.close();
			    respTP = null;
		    }
		    else {
		    	respBW.close();
		    	respBW = null;
		    }

		    ltimestamp =System.currentTimeMillis();
		    arstat[ST_REC_END] = "Y";
		    arstat[ST_REC_ETIME] = "" + ltimestamp;
		    ////////////////////////////////////////////////////////////
		    // log.debug("Finish WriterWorker : " + szoutfile );
		}
		catch (Exception socex) {
			try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
			log.debug("err " + socex.getMessage());
			log.debug("Finish WriterWorker : " + szoutfile );
		}
	    finally {

	    	try { if ( pw != null ) pw.stop(); pw = null;} catch ( Exception e_c ) {}
	    	try { if ( respTP != null ) respTP.close (); } catch ( Exception e_f1) {}
	    	try { if ( respBW != null ) respBW.close (); } catch ( Exception e_f1) {}
	    	if ( FileUtil.isFileExist( szoutfile + ".run" )) FileUtil.deleteFile( szoutfile + ".run");
	    	try { if ( m_socUtil != null ) m_socUtil.close (); } catch ( Exception e_f2) {}
	    	log.info ("Finish WriterWorker : " + szoutfile );
	    }
	}

    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse() {
    	
    	log.debug( "PROCESS");
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
            ByteChunk body = (ByteChunk) param;
            
            InputFilter savedBody = new SavedRequestInputFilter(body);
            savedBody.setRequest(request);

            InternalInputBuffer internalBuffer = (InternalInputBuffer)
                request.getInputBuffer();
            internalBuffer.addActiveFilter(savedBody);
        }
    }
}

