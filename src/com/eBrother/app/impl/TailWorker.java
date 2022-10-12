package com.eBrother.app.impl;

import com.eBrother.app.core.IWorkResult;
import com.eBrother.app.core.IWorker;
import com.eBrother.app.main.FilePattern;
import com.eBrother.util.*;
import com.eBrother.wutil.UtilDecode;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Hashtable;


/*
 * Read Data file as tail and send it to server.
 */
public class TailWorker implements IWorker, eBrotherConstant {

	private static Logger mlog = Logger.getLogger(TailWorker.class.getName());
	String m_szRunType = "";
	static boolean m_isDB = false;
	
	String m_szServerName = "";
	int m_nServerPort = 0;
	
	String m_szWorkFile = "";
	
	String m_szinclude = "";
	String m_szexclude = "";
	
	String m_lastline = "";
	boolean m_blastok = true;
	int m_nMAXDEPTH = 3;

	boolean m_bexclude = false, m_binclude = false;

	int m_nskipline = 0;
	int m_nfilesize = 0;

	SocketUtil m_socUtil;
	String m_szrundate;
	IWorkResult m_parent = null;

	public boolean m_isreal = false;  
	FilePattern m_fpattern = null;
	
	static Hashtable<String, Integer> m_sendHistory = new Hashtable<String, Integer>(); 
	Hashtable<String, String> m_henv = new Hashtable<String, String> ();
	
	public void set_env ( String szkey, String szvalue ) {
		
		m_henv.put( szkey, szvalue );
	}

	public String get_env ( String szkey ) {
		
		return m_henv.get( szkey );
	}
	
	/*
	 * (non-Javadoc) set whether realtime send or not.
	 * @see com.eBrother.app.core.IWorker#set_real(boolean)
	 */
	public void set_real ( boolean isreal ) {
		m_isreal = isreal;
	}
	
	/*
	 * (non-Javadoc) watch the file and parse filename.
	 * @see com.eBrother.app.core.IWorker#init(com.eBrother.app.core.IWorkResult, java.lang.String[], java.lang.String, java.lang.String, com.eBrother.app.main.FilePattern)
	 */
	public void init ( IWorkResult cmain, String [] args, String szrundate, String sztarget, FilePattern fpattern ) {
		
		if (args.length > 0 ) {    	
			m_szServerName = args[0];
		}
		
		if (args.length > 1 ) {    	
			m_nServerPort = (int)eBrotherUtil.getNumber(args[1]);
		}
		
		m_parent = cmain;
		
		m_szWorkFile = sztarget;
		
		if ( m_nServerPort == 0 ) m_nServerPort = 10021;
		if ( m_szServerName == null || m_szServerName.length() == 0 ) m_szServerName = "127.0.0.1";
		if ( m_szWorkFile == null || m_szWorkFile.length() == 0 ) m_szWorkFile = "d:/asp/admidas/data";
		
		m_socUtil = new SocketUtil ();
		m_szrundate = szrundate;
	
		m_fpattern = fpattern;
		
	}
	
	/*
	 * (non-Javadoc) main thread.
	 * @see com.eBrother.app.core.IWorker#run()
	 */
	public void run () {
		
		boolean bret = false;
		String szHeader;
		long lfilesize;
		String szSvrInfo = "";
		lfilesize = 99999999;

		try {
			
			mlog.info ( "TailWorker Start :  " + m_szWorkFile );
		
			bret = m_socUtil.connectServer ( m_szServerName, m_nServerPort );
			if ( ! bret ) {
				
				mlog.error("TCP Server Connect Error : IP:PORT[" + m_szServerName + ":" + m_nServerPort + "] " + m_szWorkFile );
				if ( m_parent != null ) m_parent.set_result( m_szWorkFile, false, m_fpattern ); 
				return;
			}
			
			// read line
		    String qqq, rrr;
		    
		    // windows file system does not allow to use :. more over, IP6 provide 1:2:3:4 instead of 1.2.3.4
		    rrr = UtilExt.getbasename(m_szWorkFile);
		    if ( rrr.indexOf(m_szrundate) != 0 ) {
		    	qqq = m_szrundate + "_" + m_szWorkFile.replace ( System.getProperty("file.separator"), "_").replace( ":", "_" );
		    }
		    else qqq = rrr;
		    
		    // bacth 일 경우는, file size 변경. --> file size 는 넣지 말자... 어렵다. 정말...
		    if ( ! m_isreal ) lfilesize = FileUtil.getFileSize(m_szWorkFile);
		    
		    // 02.24
		    // 04.12. BIN 을 5번째 column 에 추가.
		    // 기존 버젼은 BINARY 전송 불가.
		    // BINARY 전송을 위해  BIN 추가함.
		    szHeader = qqq + "|" + lfilesize;
		    szHeader += "|" + szSvrInfo + "|" + ( m_isreal ? 'Y' : 'N' );
		    
		    // Realtime 은 text 로 넘기고, Batch 는 BINARY 로 넘긴다.
		    if ( ! m_isreal ) szHeader += "|" + "BIN|||\n";
		    else szHeader += "|" + "|||\n";
		    ///////////////////////////////////////////////////////////////////////////
		    
		    bret = true;
		    
		    mlog.debug("Data sent (szHeader) : " + szHeader);
		    
		    m_socUtil.setClientMsg(null, szHeader);
		    
		    int lll = m_socUtil.getClientMsgLine();
		    
		    mlog.debug("Data Read : " + m_socUtil.m_strMsgIn );
		    
		    // if ( lll == 0 || m_socUtil.m_strMsgIn.indexOf("OK") < 0 )
		    if ( m_socUtil.m_strMsgIn.indexOf("OK") < 0) {
		    
				if ( m_socUtil.m_strMsgIn.indexOf("EX") >= 0) {
					m_nskipline = eBrotherUtil.getIntNumber(   eBrotherUtil.getDelimitData( m_socUtil.m_strMsgIn, "|", 2 ));
					m_nfilesize = eBrotherUtil.getIntNumber(   eBrotherUtil.getDelimitData( m_socUtil.m_strMsgIn, "|", 3 ));
				}
				else if ( m_socUtil.m_strMsgIn.indexOf("SENDED") >= 0) {
					
				    // f there is a problem, return ...
					mlog.debug("File Already Sended (m_strMsgIn): " + m_socUtil.m_strMsgIn);
					m_socUtil.CloseSocInOutStream();
					bret = true;
					if ( m_parent != null ) m_parent.set_result( m_szWorkFile, bret, m_fpattern );
					return;
				}
				else {
				    // f there is a problem, return ...
					mlog.debug("Data received error (m_strMsgIn): " + m_socUtil.m_strMsgIn);
					m_socUtil.CloseSocInOutStream();
					bret = false;
					if ( m_parent != null ) m_parent.set_result( m_szWorkFile, false, m_fpattern );
					return;
				}
		    }
		    
		    if ( m_nfilesize >= FileUtil.getFileSize(m_szWorkFile)) {
		    	mlog.debug("File Already Sended ( " + m_nfilesize + ") : " + m_szWorkFile );
		    }
		    else {
		    	mlog.debug("Data skip line : " + m_nskipline  + " - " + m_szWorkFile );
			    // handle this file
			    bret =	run_core ();
		    }
		    m_socUtil.CloseSocInOutStream();
		    mlog.trace ( "TailWorker End 1:  " + m_szWorkFile );
		    if ( m_parent != null ) m_parent.set_result( m_szWorkFile, bret, m_fpattern );

		}
		catch ( Exception e_g ) {
		    if ( m_parent != null ) m_parent.set_result( m_szWorkFile, false, m_fpattern );
		    mlog.trace ( "TailWorker End 2:  " + m_szWorkFile );
		}
		
		return; 
	
	}
	
	/*
	 * real part ...
	 */
	public boolean run_core () {
		
		File file = null;
		
		boolean bret = false;
		long updateTime, curtime;
		String szcur;
		int i;
		BufferedReader reader = null;
		String szencode = "KSC5601";
		BufferedInputStream input = null;

		final int EB_READ_SIZE = 1024;
	    
		
		try {

			int nreadline = 0;
			file = new File(m_szWorkFile);
		
			int totalBytesRead = 0; 
			
			long filelen = file.length();
			int bytesRemaining = 0;
			int bytesRead = 0;
			int ntryread;
			
			updateTime = file.lastModified();

			if ( m_henv.containsKey(EB_ENCODE)) szencode = m_henv.get(EB_ENCODE);
			
			if ( m_isreal == false ) {
				input = new BufferedInputStream(new FileInputStream(file));
			}
			else {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(m_szWorkFile), szencode));
			}
			
			String line = "";

			int nmsg = 0;
			boolean btimepass = false;
			
			bret = true;

			while(true && ( totalBytesRead < filelen )) {
				
				if ( ! m_blastok ) {
					if ( m_socUtil.setClientMsg( m_lastline + "\n" )) {
						m_lastline = "";
						m_blastok = true;
					}
					else {
						break;
					}
				}
				
				if ( m_isreal ) {
					line = reader.readLine();
				}
				else {

					byte[] result = new byte[(int)EB_READ_SIZE];
					bytesRemaining = (int) ( filelen - totalBytesRead);
					if ( bytesRemaining> EB_READ_SIZE ) ntryread = EB_READ_SIZE;
					else ntryread = bytesRemaining;
					bytesRead = input.read(result, 0, ntryread);
					totalBytesRead += bytesRead;
					line = UtilDecode.byteArrayToHex ( result, bytesRead);
				}

				if(line == null) {

					// batch 이고, 마지막 line 이 없으면 그냥 끝냅니다.
					if ( m_isreal == false ) break;
					
					// time changed...
					szcur = eBrotherUtil.getY2K_CurFullDate();
					i = m_szrundate.length();
					
					if ( nmsg++ > 30 ) {
						// mlog.debug("CHECK TIME : [" + m_szrundate + "] -->[" + m_szWorkFile + "]" );
						nmsg = 0;
					}

					if ( ! m_szrundate.equals ( szcur.substring(0, i ))) {
						btimepass = true;
						break;
					}

					file = new File(m_szWorkFile);

					if( ! file.exists()) {
						mlog.debug("File moved " );
						bret = false;
						break;
					}
					
					if ( ! m_socUtil.isconnect ()) {
						mlog.debug("Check Network " );
						bret = false;
						break;
					}

					Thread.sleep(10000);
					continue;

				}

				// if ( m_szlastsend.length() > 0 && m_szlastsend.compareTo( line ) == 0 ) continue;
				// m_szlastsend = line;

				if ( m_isreal ) {
					nreadline++;					
					if ( nreadline < m_nskipline ) continue;
				}
				
				if ( ! m_socUtil.setClientMsg( line + "\n" )) {
					nreadline--;
					bret = false;
					m_lastline = line;
					m_blastok = false;
					break;
				}
				line = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			bret = false;
		} finally {
			try {
				if(reader != null) {reader.close();}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
			try {
				if ( input != null ) {
					input.close();
				}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}

		return bret;

	}

    public static void main(String[] args) {
		
    	String [] largs = new String [2];
    	String szrundate;
    	String szfile;
    	
    	if ( args.length < 4 ) {
	    	largs[0] = "127.0.0.1";
	    	largs[1] = "10021";
	    	
	    	szrundate = "20121220012";
	    	szfile = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\1.txt";
    	}
    	else {
    		largs = args;
    		szrundate = args[2];
    		szfile= args[3];
    	}
    	
    	TailWorker twr = new TailWorker ();
    	
    	twr.init ( null, largs, szrundate, szfile, null );
    	
    	twr.run();
    	
    }
    
}

