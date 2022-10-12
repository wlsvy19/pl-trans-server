package com.eBrother.app.main;

import com.eBrother.app.core.BaseMain;
import com.eBrother.app.core.IWorkResult;
import com.eBrother.app.core.IWorker;
import com.eBrother.util.FileUtil;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.eBrotherIni;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
* eBrother BatchRec direved eBrotherRecorder Version.
* 
* @author CCMEDIA KOREA
* @see eBrother.util.eBrotherConstant
* @since 2000.04
* @version 3.0
*/
public class LogTransClient extends BaseMain implements Runnable, IWorkResult {
	
	// --------------------------------------------------------------------------------
	private static String m_eBroterLogTransClientVersion = "ASP LogTrans Client by CCMedia Technology Company, Version: 1.00.20070808.01.REAL";
	
	// private static String m_eBrotherLogTransClientMade = "2007/01/16 20:00";
	// --------------------------------------------------------------------------------
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static Logger mlog = Logger.getLogger(LogTransClient.class.getName());
		
	private Thread m_bossTID;
	static private String m_szRUNPre = null;
	
	static Hashtable<String, ServletContext> m_context = new Hashtable<String, ServletContext> ();
	
	static LogTransClient m_myself = null;
	public volatile boolean bRunning = true;
	private String m_szRunType = "";
	private boolean m_isDB = false;
	
	private String m_szServerName = "";
	private int m_nServerPort = 0;
	static String m_szWorkDir = "";
	static String m_szInboundDir = "";
	static String m_szFileGenType = "11";
	
	int m_nMAXDEPTH = 3;
	int	m_maxDepth = 10;
	
	boolean m_istest = false;
	public FileTest m_ft = null;

	public Hashtable<String, Long> m_hfilesend = new Hashtable<String, Long> ();
	
	String m_szWorkerClass = "com.eBrother.app.impl.TailWorker";
	
	String m_szstartdate = "";
	boolean m_iswebapp = false;
	
	static public String m_szSYSBLOCKTIME = "";
	
	public LogTransClient( ) {
		
		m_myself = this;
		
		m_szSYSBLOCKTIME = System.getenv("BLOCK_TIME");
		if ( m_szSYSBLOCKTIME == null ) {
			m_szSYSBLOCKTIME = System.getProperty("BLOCK_TIME");
		}
		
		mlog.info("set BLOCK TIME : " + m_szSYSBLOCKTIME );
	}
	
	public synchronized static LogTransClient getInstance () {
	
		LogTransClient ltc = null;
		
		{
			if ( m_myself == null ) {
			
				m_myself = new LogTransClient ();
				ltc = m_myself;
			}
		}

		return ltc;
	}

	public boolean is_block ( String szcurtime ) {
		
		boolean bret = false;
		String szdata = "";
		String sz1, sz2;
		
		String szcur;
		
		try {

			if ( m_szSYSBLOCKTIME == null || m_szSYSBLOCKTIME.length() == 0 ) return false;
			szcur = szcurtime.substring(8,10);

			mlog.trace (" block check cur : " + szcur );

			for ( int i = 1;; i++) {
				szdata = eBrotherUtil.getDelimitData( m_szSYSBLOCKTIME, ",", i );
				if ( szdata.length() == 0 ) break;
				sz1 = eBrotherUtil.getDelimitData( szdata, "-", 1 );
				sz2 = eBrotherUtil.getDelimitData( szdata, "-", 2 );

				mlog.trace (" block check part : " + sz1 + " -" + sz2 );
				
				if ( sz2.length() > 0 ) {
					if ( szcur.compareTo(sz1) >= 0 && szcur.compareTo(sz2) <= 0 ) {
						return true;
					}
				}
				else if ( szcur.compareTo(sz1) == 0 ) return true;
			}
		}
		catch ( Exception e ) {
			
		}
		
		return bret;
	}
	
	public void set_pattern ( String szpatternfile ) {
	
		
		mlog.info("Version:" + m_eBroterLogTransClientVersion);

		m_ft = FileTest.getInstace();
		
		m_ft.set_partterntype( 1 );
		
		m_ft.setPattern( szpatternfile );
		
	}
	
	public void run_tar () {

		Vector<String> vfilelist;
		String strCurHour;
		int i, ncntFile, j;
		String sztemp;
		boolean bdebug_cmd = false;
		boolean bdebug_out = true;
		int kkk;
		String szpredate = null;
		String szfiletime;
		String strFileName;
		FilePattern fpattern;
		String szcmd;
		int nFileGenType;
		
		
		nFileGenType = (int)m_ebUtil.getNumber( m_szFileGenType);
		
		vfilelist = new Vector<String> ();
		strCurHour = m_ebUtil.getY2K_CurFullDate();

		/// 전송하기 전에 압축을 해야 할 필요가 있을때, 로그를 압축하는 명령을 실행함.
		mlog.info("RUN TAR check D: " + m_szInboundDir + ", cur time : " + strCurHour + ", block info : " + m_szSYSBLOCKTIME) ;
	
		String szinbound = "";
		for ( i = 1;; i++ ) {
			
			vfilelist.capacity();
			szinbound = eBrotherUtil.getDelimitData(m_szInboundDir, "$", i );
			if ( szinbound == null || szinbound.length() == 0 ) break;

			eBrotherUtil.getDirListCore(vfilelist, 1, 1, true, szinbound, m_myself );			

			ncntFile = vfilelist.size();
			
			for ( j = 0; j < ncntFile; j++) {
				
				strFileName = vfilelist.get(j);
	
		    	try {
	
		    		String sztemptemp = eBrotherUtil.getbase_name ( strFileName );
		  	  		
		    		if ( nFileGenType <= sztemptemp.length()) kkk =nFileGenType;
		    		else kkk = sztemptemp.length();

		    		// kkk = sztemptemp.length() -1;

		    		szfiletime =  sztemptemp.substring ( 0,kkk );
		    		
		  	  		if ( szfiletime == null ) {
		  	  			mlog.info("RUN TAR. SKIP file. skip time 1: " + strCurHour.substring( 0, kkk ) + " ==> "  + strFileName + ", " + szfiletime );
		  	  			continue;
		  	  		}
	
		  			// realtime 으로 전송해야 하지만, 이미 오래전 file 인 경우 : real time 에서는 무시한다.
		  			if ( kkk > 0 && szfiletime.compareTo( strCurHour.substring( 0, kkk )) >= 0 ) {
		  				mlog.info("RUN TAR. SKIP file. skip time 2: " + strCurHour.substring( 0, kkk ) + " ==> "  + strFileName );
			  			continue;
		  			}					  	  		
		  			
		  			szcmd = m_szRUNPre + " " + sztemptemp + " " + szinbound + " " + m_szWorkDir;
		  			
		  			mlog.info("RUN TAR : " + szcmd );
		  			// String args [] = { szinbound, m_szWorkDir, szfiletime }; 
		  			eBrotherUtil.run_cmd ( bdebug_cmd, bdebug_out, szcmd, null );
		  			szpredate =  szfiletime;
	
		    	}
		    	catch ( Exception e ) {
					mlog.error( "run_cmd :" + e );
		    	}
			}
		}

		mlog.info("RUN TAR check F: " + m_szInboundDir + ", cur time : " + strCurHour + ", block info : " + m_szSYSBLOCKTIME) ;
		for ( i = 1;; i++ ) {
			
			vfilelist.capacity();
			szinbound = eBrotherUtil.getDelimitData(m_szInboundDir, "$", i );
			
			if ( szinbound == null || szinbound.length() == 0 ) break;

			eBrotherUtil.getFileListCore(vfilelist, 1, 1, true, szinbound, m_myself );			

			ncntFile = vfilelist.size();
			
			for ( j = 0; j < ncntFile; j++) {

				strFileName = vfilelist.get(j);
	
		    	try {
	
		    		String sztemptemp = eBrotherUtil.getbase_name ( strFileName );

		    		if ( nFileGenType <= sztemptemp.length()) kkk =nFileGenType;
		    		else kkk = sztemptemp.length();

		    		szfiletime =  sztemptemp.substring ( 0,kkk );
		    		
		  	  		if ( szfiletime == null ) {
		  	  			mlog.info("RUN TAR. SKIP file. skip time 1: " + strCurHour.substring( 0, kkk ) + " ==> "  + strFileName + ", " + szfiletime );
		  	  			continue;
		  	  		}
	
		  			// realtime 으로 전송해야 하지만, 이미 오래전 file 인 경우 : real time 에서는 무시한다.
		  			if ( kkk > 0 && szfiletime.compareTo( strCurHour.substring( 0, kkk )) >= 0 ) {
		  				mlog.info("RUN TAR. SKIP file. skip time 2: " + strCurHour.substring( 0, kkk ) + " ==> "  + strFileName );
			  			continue;
		  			}					  	  		

		  			if ( szpredate != null && szpredate.equals( szfiletime )) continue;


		  			szpredate = szfiletime;
		  			szcmd = m_szRUNPre + " " + szfiletime + " " + szinbound + " " + m_szWorkDir;

		  			mlog.info("RUN TAR : " + szcmd );
		  			// String args [] = { szinbound, m_szWorkDir, szfiletime }; 
		  			eBrotherUtil.run_cmd ( bdebug_cmd, bdebug_out, szcmd, null );
		  			szpredate =  szfiletime;
	
		    	}
		    	catch ( Exception e ) {
					mlog.error( "run_cmd :" + e );
		    	}
			}
		}
		/////////////////////////////////////////////////////////////////////////
	}	
	
	public void start () {
		
		String sztest;
		
		m_nCheckInterval = m_ebIni.getInteger("RECORDER", "BatchInterval", 1);
		mlog.info("Execution period m_nCheckInterval(minute)=" + m_nCheckInterval);
		
		sztest = System.getProperty("ISTEST");
		
		if ( sztest != null && sztest.length() > 1 ) m_istest = true;
		if (  m_istest ) return;
		
		try {
			
			if (m_bossTID != null) {
			
				if (m_bossTID.isAlive()) {
					mlog.fatal("LogTransClient is already running!");
					System.exit(1);
				}
				mlog.fatal("LogTransClient is not NULL!");
				System.exit(1);
			}
			
			m_bossTID = new Thread(this);
			m_bossTID.start();
			
		} catch (Throwable t) {
			mlog.fatal("Thread creation error", t);
			System.exit(1);
		}
	
	}

	public void set_filter  ( String szinclude, String szexclude ) {
	  
		int k;
		m_szinclude = szinclude;
		m_szexclude = szexclude;
	  
		if ( m_szinclude != null && m_szinclude.length() > 0 && ! "-".equals(m_szinclude)) m_binclude = true; 
		if ( m_szexclude != null && m_szexclude.length() > 0 && ! "-".equals(m_szexclude)) m_bexclude = true;

	}
  
	public void set_maxdepth ( int ndepth ) {
		m_nMAXDEPTH = ndepth;
	}
	
	public void set_result ( String szfile, boolean bresult, FilePattern fpattern ) {
		
		boolean bfilesend = m_hfilesend.containsKey( szfile);
		mlog.info("set_result 1");
   		if ( bfilesend && ! bresult ) {
   			m_hfilesend.remove ( szfile);
   			return;
   		}
   		if ( fpattern != null && fpattern.isdelete && bresult ) {
   			FileUtil.deleteFile(szfile);
   		}
	}
	
	public void run() {
			
		try {

			mlog.info("Main Start");
			// local set.
			// move from worker because of several thread.
			set_filter  ( m_szinclude, m_szexclude );
			set_maxdepth  ( m_maxDepth );
			
			Calendar rightNow = Calendar.getInstance();
			rightNow.set(Calendar.MILLISECOND, 0);
			rightNow.set(Calendar.SECOND, 0);

			if ((m_nCheckInterval % 60) == 0) {
				rightNow.set(Calendar.MINUTE, 0);
				rightNow.add(Calendar.HOUR_OF_DAY, 1);
			} else {
				rightNow.add(Calendar.MINUTE, 1);
			}

			// check running thread whether it's running or not.
			Date dt = rightNow.getTime();
			Timer timer = new Timer();
			Timer timer2 = new Timer();
			Timer timerRunTar = new Timer();
			
			m_szstartdate = System.getProperty("TDATE");			

			//////////////////////////////////////////////////////////////////////////////////////////////////////

			if ( m_ft.hasreal()) {
				
				timer.scheduleAtFixedRate(new TimerTask() {
					
					public void run() {
						
						File fcheck;
						int ncntFile;
						long lfilesize;
						long lsendsize;
						int i,j, kkk;
						IWorker ltw;
						String sztemp;
						Hashtable<String, Object> hparam = new Hashtable<String, Object>(); 
						
						try {
													
							String strCurHour = m_ebUtil.getY2K_CurFullDate ();
	
							mlog.info("Real Check Dir : " + m_szWorkDir + ", cur time : " + strCurHour + ", block info : " + m_szSYSBLOCKTIME) ;
							
							if ( is_block(strCurHour ) ) {
								mlog.info("BLOCKED. Real Block Time : " + strCurHour + " - " + m_szSYSBLOCKTIME);
								return;
							}
	
							////////////////////////////////////////////////////////////////////////////
							Vector<String> vfilelist = new Vector<String> ();
							
							for ( i = 1;; i++ ) {
								
								sztemp = eBrotherUtil.getDelimitData(m_szWorkDir, "$", i );
								if ( sztemp == null || sztemp.length() == 0 ) break;
								eBrotherUtil.getFileList(vfilelist, m_maxDepth, 1, true, true, sztemp, m_myself );
								
							}

							ncntFile = vfilelist.size();
							Thread tthread;
							
							boolean bneedcreate = false;

							for ( i = 0; i < ncntFile; i++) {
								
								String strFileName = vfilelist.get(i);
								String szfiletime;
	
						    	try {
	
						  	  		FilePattern fpattern = m_ft.getPattern ( strFileName );
	
						  	  		if ( fpattern == null ) continue;
	
						  	  		// 파일의 일시가 현재 시각과 동일하거나, 나중입니다.
						  			if (  fpattern.issendreal == false ) {
						  				mlog.trace("Real TIme SKIP file. pattern Not REAL => " + strFileName );
							  			continue;
						  			}
	
						  	  		szfiletime = m_ft.getToken( strFileName );
						  	  		
						  	  		if ( szfiletime == null ) continue;
	
						  	  		kkk = szfiletime.length();
						  	  		if ( m_szstartdate != null && kkk > 0 && m_szstartdate.compareTo(szfiletime) > 0 ) {
						  	  			
						  	  			mlog.trace("Real TIme SKIP file. skip time : " + szfiletime + " ==> "  + strFileName );
						  	  			continue;
						  	  		}
	
						  			// realtime 으로 전송해야 하지만, 이미 오래전 file 인 경우 : real time 에서는 무시한다.
						  			if ( kkk > 0 && szfiletime.compareTo( strCurHour.substring( 0, kkk )) >= 0 ) {
						  				
						  				mlog.trace("Real TIme SKIP file. skip time : " + strCurHour.substring( 0, kkk ) + " ==> "  + strFileName );
							  			continue;
						  			}					  	  		
						  			
						    		lfilesize = FileUtil.getFileSize ( strFileName );
	
						  			mlog.info("Real Time TARGET FILE : " + strFileName + ", " + szfiletime
					  						+ ", " + strCurHour + ", " + szfiletime.compareTo( strCurHour.substring( 0, kkk )));
	
						  			bneedcreate = false;
	
						  			//mlog.info("File. : " +  strFileName);
						  			if ( m_thead.containsKey(strFileName)) {
						  				
						  				tthread = m_thead.get ( strFileName );
										try {
											if ((tthread == null) || !tthread.isAlive()) {
												bneedcreate = true;
											}
										} catch (Exception t) {
											// mlog.error(t);
											mlog.error( "Real Step3 : " + t);
										}					  				
						  			} else bneedcreate = true;
						  								  			
						  			if ( bneedcreate ) {
						  				
						  				m_hfilesend.put( strFileName, lfilesize );
						  				ltw = getWorker ( m_szWorkerClass );
						  				ltw.init( m_myself, m_args, szfiletime, strFileName, fpattern );
						  				ltw.set_real( true );
						  				tthread = new Thread(ltw);
						  				// tthread.setName("Worker Start :" + m_szWorkerClass );
						  				tthread.setName("CCMediaService.LogTailWorker");
						  				tthread.start();
										m_thead.put( strFileName, tthread );
						  			}
						    	}
						    	catch ( Exception e_check2) {
						    		e_check2.printStackTrace();
						    		mlog.error( "Real Step2 : " +  e_check2 );
						    	}

							}
						} 
						catch (Exception t2) {
							mlog.error( "Real Step1 " + t2 );
						}
					}
				}, dt, m_nCheckInterval * 60000);		
				///////////////////////////////////////////////////////////////////////////
			}

			timer2.scheduleAtFixedRate(new TimerTask( ) {
				
				boolean isrun = false;
				public void run() {
					
					File fcheck;
					int ncntFile;
					long lfilesize;
					long lsendsize;
					int i,j, kkk;
					IWorker ltw;
					String sztemp;
					Hashtable<String, Object> hparam = new Hashtable<String, Object>(); 
					
					try {

						if ( isrun == true ) return;
						isrun = true;

						////////////////////////////////////////////////////////////////////////////
						String strCurHour = m_ebUtil.getY2K_CurFullDate(); //m_ebUtil.getY2KCurDate() + m_ebUtil.getStrCurTime();
						mlog.info("Batch Check Dir : " + m_szWorkDir + ", cur time : " + strCurHour + ", block info : " + m_szSYSBLOCKTIME) ;

						if ( is_block(strCurHour ) ) {
							mlog.info ("BLOCKED. Batch Block Time : " + strCurHour + " - " + m_szSYSBLOCKTIME);
							isrun = false;
							return;
						}

						Vector<String> vfilelist = new Vector<String> ();
						
						for ( i = 1;; i++ ) {
							sztemp = eBrotherUtil.getDelimitData(m_szWorkDir, "$", i );
							if ( sztemp == null || sztemp.length() == 0 ) break;
							eBrotherUtil.getFileList(vfilelist, m_maxDepth, 1, true, true, sztemp, m_myself );
						}
						
						ncntFile = vfilelist.size();

						Thread tthread;
						
						boolean bneedcreate = false;

						for ( i = 0; i < ncntFile; i++) {
							
							String strFileName = vfilelist.get(i);
							String szfiletime;

					    	try {

					  	  		szfiletime = m_ft.getToken( strFileName );

					  	  		FilePattern fpattern = m_ft.getPattern ( strFileName );
					  	  		
					  	  		if ( fpattern == null ) {
					  	  			mlog.trace("Batch SKIP file. no pattern :" + strFileName);
					  	  			continue;
					  	  		}

					  	  		kkk = szfiletime.length();
					  			// 파일의 일시가 현재 시각과 동일하거나, 나중입니다.
					  	  		// szfiletime : 이것은 0 일 수 있다. access.log 처럼. 이런 파일을 처리할 수 있다.
					  			// if ( szfiletime == null && szfiletime.length() == 0 ) continue;
					  	  		
					  			if ( kkk > 0 && szfiletime.compareTo( strCurHour.substring( 0, kkk)) >= 0 ) {
					  				mlog.trace("Batch SKIP file. file check  : szfiletime = " + kkk + ", " + strCurHour.substring( 0, kkk));
					  	  			continue;
					  			}
					  			
					  	  		if ( m_szstartdate != null && kkk > 0 && m_szstartdate.compareTo(szfiletime) > 0 ) {
					  	  			// 시간 check ??
					  	  			/*if ( m_hfilesend.containsKey( strFileName)) {
					  	  				mlog.trace("Batch SKIP file. filtered :" + szfiletime + ", max : " + m_szstartdate + ", old file " + strFileName);
					  	  			}*/
					  	  			mlog.trace("Batch SKIP file. startfile : " + m_szstartdate + ", " + szfiletime ); 
					  	  			continue;
					  	  		}

					    		lfilesize = FileUtil.getFileSize ( strFileName );

					    		if ( m_hfilesend.containsKey( strFileName)) {
					    			lsendsize = m_hfilesend.get(strFileName);
					    			if ( lfilesize == lsendsize ) {						  	  			
					  	  				mlog.trace("Batch SKIP file. Already sended : " + lsendsize + " => " + strFileName );
					    				continue;
					    			}
					    			m_hfilesend.remove( strFileName );
					    		}

					  	  		mlog.info("Batch TARGET FILE : " + strFileName);

					    		try {
						    		m_hfilesend.put( strFileName, lfilesize );
						  			bneedcreate = false;
						  	  		
					  				ltw = getWorker ( m_szWorkerClass );
					  				ltw.init( m_myself, m_args, szfiletime, strFileName, fpattern );
					  				
					  				ltw.set_real( false );
					  				ltw.run();
					  				lfilesize = FileUtil.getFileSize ( strFileName );
					  				if ( lfilesize < 0 ) {
					  					m_hfilesend.remove( strFileName );
					  				}
					    		}
					    		catch ( Exception e ) {
					    			m_hfilesend.remove( strFileName );
					    			mlog.error( "Batch Step 1 : " + strFileName + " ==> " + e );
					    		}
					    	}
					    	catch ( Exception e_check2) {
					    		mlog.error( "Batch Step 2 : " + strFileName + " ==> " + e_check2 );
					    	}
						}
					} 
					catch (Throwable t) {
						mlog.error( "Batch Step 3" + t);
					}
					
					isrun = false;
				}
			}, dt, m_nCheckInterval * 60000);		
			///////////////////////////////////////////////////////////////////////////
			
			while (bRunning) {
				
				try {
					
					if ( m_szRUNPre != null && m_szRUNPre.length() > 0 ) {
						run_tar ();
					}
					Thread.sleep(60000);
				} catch (InterruptedException ie) {
					//
				}
			}
			
			timer2.cancel();
			if ( m_ft.hasreal()) {
				timer.cancel();
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				//
			}

			String szkey;
			for (Enumeration<String> ee = m_thead.keys(); ee.hasMoreElements();) {
				szkey = ee.nextElement();
				Thread qqq = m_thead.get(szkey);

				if ((qqq != null) && qqq.isAlive()) {
					try {
						qqq.join(15000);
					} catch (InterruptedException ie) {
						// 
					}
				}				
			}
			mlog.info("LogTrans Client Boss Thread ended.");
			
			System.exit(0);
		
		} catch (Throwable t) {
			mlog.fatal("Exception of bRunning : " + t);
			System.exit(1);
		}
	}
	
	public static void main (String[] args) {
		
		LogTransClient ltc = new LogTransClient ();
		
		String strCurHour = eBrotherUtil.getY2K_CurFullDate ();
		boolean isblock = ltc.is_block ( strCurHour );
		
		System.out.println( strCurHour + " --> " + isblock );

		Vector<String> vfilelist = new Vector<String> ();
		
		String szinbound = "D:\\ebrotherpilms\\client\\inbound\\min";
		szinbound = "D:\\pilms\\client\\inbound";
		eBrotherUtil.getDirListCore(vfilelist, 1, 1, true, szinbound, m_myself );			

		String sztrans_patternfile = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\client\\transfilepattern.txt";
		System.out.println ( vfilelist.toString());

		ltc.set_pattern ( sztrans_patternfile );
		m_szInboundDir = szinbound;
		m_szRUNPre = "D:\\pilms\\client\\gnuutil\\bin\\bash d:\\pilms\\client\\bin\\run_tar.sh ";
		m_szWorkDir= "D:\\ebrotherpilms\\client\\send";
		ltc.run_tar ();
		// ltc.exec ( args );

		/*
		String strCurHour = eBrotherUtil.getY2KCurDate() + eBrotherUtil.getStrCurTime(); 
		
		strCurHour = eBrotherUtil.getY2K_CurFullDate ();
		
		String sz = strCurHour.substring( 8,10 );
		
		System.out.println( strCurHour + " --> "  + sz );
		*/
	}
	
	public void setWebApp ( boolean bwebapp ) {
		
		m_iswebapp = bwebapp;
	}

	public boolean runWebApp ( String szini ) {
		
		eBrotherIni  ebIni = new eBrotherIni ();
		
		boolean bini = ebIni.open(szini);
		if ( ! bini) return false;
		
		String szserver = ebIni.getString("TRANS", "SERVER", "" );
		String szport = ebIni.getString("TRANS", "PORT", "" );
		String szwork = ebIni.getString("TRANS", "WORK", "" );
		String szinbound = ebIni.getString("TRANS", "INBOUND", "" );
		String szpattern = ebIni.getString("TRANS", "PATTERN", "" );
		String szinclude = ebIni.getString("TRANS", "INCLUDE", "" );
		String szexclude = ebIni.getString("TRANS", "EXCLUDE", "" );
		String strLog4JProp = ebIni.getString("TRANS", "LOG4J", "" );
		String szruncmd = ebIni.getString("TRANS", "RUNPRE", "" );
		
		String szrunfilegen = ebIni.getString("TRANS", "GENTYPE", "11" );
		
		String args [] = new String [] { szserver, szport, szwork, szpattern
				, szinclude, szexclude, strLog4JProp, "" + 10, szinbound, szruncmd, szrunfilegen };
		exec ( args);
		return true;
	}

	public void exec (String[] args) {

		String strCurrentPath = System.getProperty("user.dir");
		String strINI = "";
		String strLog4JProp = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\client\\log4j.properties";
		String sztrans_patternfile = "";
		String szinclude;
		String sz_servername = null;
		String sztemp;
		int    n_serverport = 0;
				
		// if ( szstartdate != null ) m_startdate = szstartdate;
				
		//		nohup $JAVA_HOME/bin/java -D$PNAME com.eBrother.asp.trans.LogTransClient \
		//       $WEBNIBBLER_SERVER_IP $RECOVERY_RECEIVER_PORT $RUN_INBOUND \
		//        $SERVERPATTERN - -  $WEBNIBBLER_HOME/etc/log4j.properties  \
		///>> $WEBNIBBLER_HOME/log/LogTransClient.$HOSTNAME.$RUN_DATE.log &
		//	
	
		if (args.length > 0 ) {    	
			sz_servername = args[0];
		}

		if (args.length > 1 ) {    	
			n_serverport = (int)eBrotherUtil.getNumber(args[1]);
		}
		
		if (args.length > 2 ) {    	
			m_szWorkDir = args[2];
		}
		
		if (args.length > 3) {    	
			sztrans_patternfile = args[3];
			// if ( "DB".equals(sztrans_patternfile)) m_isDB = true;
		}
		
		if (args.length > 4) {
		
			m_szinclude = args[4];
			if ( m_szinclude.equals("-")) m_szinclude = "";  
		}
		else {
			m_szinclude = "MESSAGE";
		}

		if (args.length > 5) {    	
			m_szexclude = args[5];
			if ( m_szexclude.equals("-")) m_szexclude = "";
		}
		else {
			m_szexclude = "MIN";
		}
		
		// log4j setup
		if (args.length > 6 ) {
			strLog4JProp = args[6];
		}
		
		m_maxDepth = 0;
		// log4j setup
		if (args.length > 7 ) {
			m_maxDepth = (int)eBrotherUtil.getNumber(args[7]);
		}
		if ( m_maxDepth == 0 ) m_maxDepth = 10;

		// log4j setup
		if (args.length > 8 ) {
			m_szInboundDir = args[8];
		}
		else {
			m_szInboundDir = m_szWorkDir;
		}
		
		// log4j setup
		if (args.length > 9 ) {
			m_szRUNPre = args[9];
		}
		else {
			m_szRUNPre = "";
		}
		
		
		// log4j setup
		if (args.length > 10 ) {
			m_szFileGenType = args[10];
		}
		else {
			m_szFileGenType = "11";
		}
		
		////////////////////////////////////////////////////////////////////////////////

		if ( false ) {
			
			if ( n_serverport == 0 ) n_serverport = 10010;
			if ( sz_servername == null || sz_servername.length() == 0 ) sz_servername = "127.0.0.1";
			
			sz_servername = "127.0.0.1";
			m_szWorkDir = "d:\\pilms\\client\\send";
			sztrans_patternfile = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\client\\transfilepattern.txt";
			m_szInboundDir = "d:\\pilms\\client\\inbound";
			m_szRUNPre = "D:\\pilms\\client\\gnuutil\\bin\\bash d:\\pilms\\client\\bin\\run_tar.sh ";
		
		}
		
		m_szServerName = sz_servername;
		m_nServerPort = n_serverport;
		
		mlog.info ( sztrans_patternfile );
		mlog.info(strLog4JProp);

		PropertyConfigurator.configure(strLog4JProp);
		
		// set pattern file
		mlog.info("Set parrtern : " + sztrans_patternfile );
		
		m_myself.set_pattern ( sztrans_patternfile );

		mlog.info("RUN Pre : " + m_szRUNPre );
		
		if ( args.length != 0 ) {
			m_myself.m_args = args;
		}
		else {
			
			m_args = new String [] { sz_servername, "" + n_serverport, m_szWorkDir, sztrans_patternfile
				, m_szinclude, m_szexclude, strLog4JProp, "" + m_maxDepth, m_szInboundDir, m_szRUNPre };

		}

		// run main thread
		m_myself.start();
		//////////////////////////////////////////////////////////////////////////////////////////
		
		if ( ! m_iswebapp ) {

			// binding server socket to prevent 2nd instance.
			Thread th2 = new Thread() {
				public void run() {
					
					ServerSocket ssock;
					mlog.info("use TCP port :" + m_nServerPort + " to prevent dual run");
					try {
						
						ssock = SocketUtil.getServerSocket(m_nServerPort + 5);
						
						while ( true ) {
							Socket sock = ssock.accept();
							Thread.sleep(1000);
						}
					} 
					catch ( Exception  ie) {
						
						mlog.debug("eBrother LogTransClient already run. Please check the system 1st." );
						mlog.debug("run following cmd : ps -ef | grep LogTrans " );
						System.exit( -1);
					}
				}
			};
			
			th2.start();
			//////////////////////////////////////////////////////////////////////////////////////////
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					bRunning = false;
					mlog.debug("ShutdownHook invoked. bRunning=" + bRunning);
					try {
						Thread.sleep(2000);
					} 
					catch (InterruptedException ie) {
					}
				}
			});
		}
	}
	
}

