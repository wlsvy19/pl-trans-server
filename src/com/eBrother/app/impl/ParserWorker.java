package com.eBrother.app.impl;

import com.eBrother.util.FileUtil;
import com.eBrother.util.Util;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParserWorker implements Runnable {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	protected static Logger m_logger = Logger.getLogger( ParserWorker.class.getName());

	// String logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
	// 123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"
	String m_szcur2ndafter;
	StringBuffer m_sbcurhead, m_sbcur2ndafter;
	StatWorker m_sw = StatWorker.getInstance();

	Pattern m_pattern, m_patternFinal;
	StringBuffer m_sb;
	Meta m_cmeta;
	String m_szoutdir;
	String m_curdate;
	String m_szinfile;

	String m_szfilter = "";
	String m_szcurreadfile;
	long   m_lcurreadfile;

	BufferedWriter m_bw = null;

	static boolean m_bmetaset = false;
	static String m_patternfile;
	static String m_serverfile;
	static Hashtable<String, Meta> m_hpattern = new Hashtable<String, Meta> (); 
	static Hashtable<String, Server> m_hserver = new Hashtable<String, Server> ();
	
	Hashtable<String, BufferedWriter> m_hfileBW = new Hashtable<String, BufferedWriter> ();
	Hashtable<String, String> m_hfileNM = new Hashtable<String, String> ();
	
	static Hashtable<String, long []> m_hstatPW = new Hashtable<String, long []> (); 

	public Server m_wnserver = null;
	
	public Server m_server = null;
	
	TransWorker632 m_ww = null;

	boolean m_isstop = false;
	
	static boolean m_isstopGlobal = false;
	
	int m_nskipline = 0;
	
	String [] m_statkey = new String [] {"","","","", "", "", "" };
	int m_statcheck = 0;
	int m_curlinesize = 0;
	int [] m_statdata = new int [] {0,0,0,0 };
	
	boolean m_bmasterthread = false;
	static boolean m_bstandalone = false;
	
	public static WNAnalHelper m_wah = null;
	
	class Server {
		public String m_ip = "";
		public String m_logtoken = "";
		public String m_group = "";
		public String m_svr = "";
		public String m_instance = "";
		public boolean iswn = false;
		public boolean isw3c = false;
	}

	public class Meta {
		public String m_szserver;
		public String m_szformat;
		public String m_szpattern;
		public Pattern m_pattern, m_patternFinal;
		public Vector<String>m_format = new Vector<String>();
		public Vector<Integer>m_format_col = new Vector<Integer>();
		public Vector<String>m_format_name = new Vector<String>();
		public Vector<String>m_format_ymd = new Vector<String>();
		
		public SimpleDateFormat m_timestamp = null;
		public SimpleDateFormat m_dateformatin = null;
		public Locale m_datelocale = Locale.ENGLISH;
	}

	public ParserWorker ( TransWorker632 ww, String [] args ) {

		String szpattern_server = args[0];
		String szpatterhn = args[1];
		String szindir = args[2];		
		String szoutdir = args[3];

		if ( args.length > 4 ) {
			m_szfilter =args[4]; 
		}
		
		m_ww = ww;
		
		if ( ww == null ) m_bmasterthread = true;
		
		m_patternfile = szpatterhn;
		m_serverfile = szpattern_server;

		m_szinfile = szindir;
		m_szoutdir = szoutdir;

		m_logger.debug("ParserWorker (master : " + m_bmasterthread + ") : " + szindir );

		// set WN default
		m_wnserver = new Server ();
		m_wnserver.iswn = true;
		m_wnserver.isw3c = true;
		m_wnserver.m_instance = "wn";

		synchronized ( this ){
			
			if ( ! m_bmetaset ) {

				m_logger.debug("Meta Setup : " + m_patternfile + ", " + m_serverfile);
				
				if ( m_patternfile != null && UtilExt.isFileExist(m_patternfile) ) set_patternmeta ( m_patternfile );
				if ( m_serverfile != null && UtilExt.isFileExist(m_serverfile) ) set_server ( m_serverfile);
				m_bmetaset = true;
			}
		}
	}

	// 전체 stop. main thread 에 대해서 stop
	public void stopGlobal () {
		m_isstopGlobal = true;
	}
	
	// 실시간 개별 파일 parser 에 대해서 stop.
	public void stop () {
	
		m_isstop = true;
	}
	
	public void setSkipLine ( int nline ) {
		m_nskipline = nline;
	}

	static public void set_format ( Meta c_meta, String szformat ) {

		JSONParser parser = new JSONParser();
		String sztmp;
		int nmax_col;
		String sztmp2;
		try {
	 	
			Object obj = parser.parse(szformat);
	 
			JSONObject jsonObject = (JSONObject) obj;
			
			sztmp = (String) jsonObject.get("cols");

			nmax_col = UtilExt.getNumber(sztmp);

			// if ( m_ww == null ) m_logger.info( "[Parser] write format : columns - " + nmax_col );
			for ( int i = 1; i <= nmax_col; i++ ) {
				sztmp = (String) jsonObject.get("c" + i);
				if ( sztmp == null ) sztmp = ""; 

				c_meta.m_format.add( sztmp );
				sztmp2 = UtilExt.getDelimitData(sztmp, ",", 1 );
				c_meta.m_format_col.add( UtilExt.getNumber(sztmp2));
				sztmp2 = UtilExt.getDelimitData(sztmp, ",", 2 );
				c_meta.m_format_name.add( sztmp2);
				sztmp2 = UtilExt.getDelimitData(sztmp, ",", 3 );
				c_meta.m_format_ymd.add( sztmp2);
				if ( sztmp2.trim().length() > 0 ) {
					c_meta.m_timestamp = new SimpleDateFormat("yyyyMMddHHmmss");
					c_meta.m_dateformatin = new SimpleDateFormat(sztmp2, Locale.ENGLISH);
				}

				// if ( m_ww == null ) m_logger.info( "[Parser] columns " + i + " datecheck ? " + sztmp2 );
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public void set_patternmeta ( String szfile ) {
				
		String [] arr;
		String szcontent = UtilExt.getFileContent(szfile);
		String szkey, szpattern, szwformat;
		arr = szcontent.split("\n");

		for ( int i = 0; i < arr.length; i++ ) {

			szkey = UtilExt.getDelimitData(arr[i], ",", 1);
			szpattern = UtilExt.getDelimitData(arr[i], ",", 2);
			szwformat = UtilExt.getDelimitDataRight(arr[i], ",", 3);
			if ( ! m_hpattern.containsKey(szkey)) {
				Pattern p = null, pfinal = null;
				Meta c_meta = new Meta();
				c_meta.m_szserver = szkey;
				c_meta.m_szformat = szwformat;
				c_meta.m_szpattern = szpattern;
				
				try {
					p = Pattern.compile( szpattern );
					c_meta.m_pattern = p;
				}
				catch ( Exception e ) {
					
				}

				try {
					pfinal = Pattern.compile( szpattern + ":::::::::$", Pattern.MULTILINE | Pattern.DOTALL );
					c_meta.m_patternFinal = pfinal;
				}
				catch ( Exception e ) {
					
				}
				set_format ( c_meta, szwformat );
				m_hpattern.put( szkey, c_meta );
			}
		}
	}

	public void set_server ( String szfile ) {
		
		String [] arr;
		String szcontent = UtilExt.getFileContent(szfile);
		String szkey;

		arr = szcontent.split("\n");

		for ( int i = 0; i < arr.length; i++ ) {

			Server c_server = new Server ();
			szkey = UtilExt.getDelimitData(arr[i], ";", 1);
			c_server.m_ip = szkey;
			szkey = UtilExt.getDelimitData(arr[i], ";", 2);
			c_server.m_logtoken = szkey;
			szkey = UtilExt.getDelimitData(arr[i], ";", 3);
			c_server.m_group = szkey;
			szkey = UtilExt.getDelimitData(arr[i], ";", 4);
			c_server.m_svr = szkey;
			szkey = UtilExt.getDelimitData(arr[i], ";", 5);
			c_server.m_instance = szkey;
			m_hserver.put ( c_server.m_ip + "|" + c_server.m_logtoken, c_server );
		}
	}

	public Server get_server ( String szfile ) {
		
		Server c_server = null;
		
		String	szkey = "";
		
		for (Enumeration<String> ee = m_hserver.keys() ; ee.hasMoreElements() ; ) {
         
			szkey =  ee.nextElement();
			c_server = m_hserver.get(szkey);
			
			if ( szfile.indexOf(c_server.m_ip) >= 0 && szfile.indexOf( c_server.m_logtoken) >= 0 ) {
				return c_server;
			}
		}
		return null;

	}

	public String get_writefile ( String sztimplestamp, StringBuffer sbhead, StringBuffer sbdata ) {
		
		String szoutfile;
		String sztmp;
		String szcheck;
		
		String szorgfile = m_szcurreadfile.substring(m_szcurreadfile.lastIndexOf( FILE_SEPARATOR )+1 );
		
		if ( sztimplestamp.length() < 8 ) {
			szoutfile = sztimplestamp; 
			szcheck = sztimplestamp;
		}
		else {
			szoutfile = sztimplestamp.substring(0, 8) + "235959";
			szcheck = sztimplestamp.substring(0, 8);
		}

		if ( m_hfileNM.containsKey(szcheck)) {
			return m_hfileNM.get(szcheck);
		}

		Date dd = new Date();

		// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
		FileUtil.createDir( m_szoutdir);
		FileUtil.createDir( m_szoutdir + FILE_SEPARATOR + szoutfile );
		sztmp = m_szoutdir + FILE_SEPARATOR + szoutfile + FILE_SEPARATOR 
				+ "FIL_" + szoutfile + "_" +  m_server.m_group + "_" + m_server.m_svr
				+ "_" + m_server.m_instance + "_" +  szorgfile + "_" + dd.getTime() + ".log";
		m_hfileNM.put( szcheck, sztmp );
		return sztmp;

	}

	/*
	 * WN log write
	 */
	public void write_core ( String [] arrlog) {
		
		StringBuffer sbf = new StringBuffer ();
		String szwritefile, sztmp;
		String szwdate;
		String szwhh;
		String szwmi;
		String szwc;	
		int j;
		BufferedWriter bw;;
		
		szwritefile = m_szoutdir + FILE_SEPARATOR + UtilExt.getbasename(m_szcurreadfile) + ".txt";
		
		sztmp = szwritefile;

		if ( m_hfileBW.containsKey(sztmp )) {
			bw = m_hfileBW.get( sztmp );
		}
		else {
			// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
			bw = UtilExt.getFileWriter(sztmp);
			UtilExt.createFile( sztmp + ".run");
			m_hfileBW.put( sztmp, bw );
		}

		if ( ! m_server.isw3c ) {
			for ( j = 0; j < arrlog.length; j++) {
	
				try {
					if ( arrlog[j] != null ) {
						bw.write( arrlog[j].replace(',', ' ') );
					}
					
					bw.write( "\t");
					
				}
				catch ( Exception e ) {
				}		
			}
			try {
				bw.write( '\n');
			}
			catch ( Exception e) {
				
			}
		}
		else {
			
			sbf.setLength(0);
			
			sbf.append( arrlog[ILogConst.EB_REC_HEAD_IP] );
			sbf.append( " " );
			sbf.append( "-" );
			sbf.append( " " );
			sbf.append( "-" );
			sbf.append( " " );
			sbf.append( "[" );
			
			sztmp = "";
			try {
				DateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				Date tempDate = sdFormat.parse( arrlog[ILogConst.EB_REC_HEAD_TIME] );
				SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
				sztmp = df.format( tempDate );
			}
			catch ( Exception e_date ) {
			}
			
			sbf.append( sztmp );
			sbf.append( " +0900]" );
			sbf.append( " \"GET " );
			
			sbf.append( arrlog[ILogConst.EB_REC_HEAD_URL]  );
			
			if ( arrlog[ILogConst.EB_REC_HEAD_QUERY] != null && arrlog[ILogConst.EB_REC_HEAD_QUERY].length() > 1 ) {
				sbf.append( "?" + arrlog[ILogConst.EB_REC_HEAD_QUERY]  );
			}
			sbf.append( " HTTP/1.0\" 304 - " );
			
			if ( arrlog[ILogConst.EB_REC_HEAD_REF] != null && arrlog[ILogConst.EB_REC_HEAD_REF].length() > 1 ) {
				
				sbf.append( "\"" +arrlog[ILogConst.EB_REC_HEAD_REF]  + "\"");
			}
			else {
				sbf.append( "\"" + "-"  + "\"");
			}
			
			sbf.append( " " );
			if ( arrlog[ILogConst.EB_REC_HEAD_AGENT] != null && arrlog[ILogConst.EB_REC_HEAD_AGENT].length() > 1 ) {
				
				sbf.append( "\"" +arrlog[ILogConst.EB_REC_HEAD_AGENT]  + "\"");
			}
			else {
				sbf.append( "\"" + "-"  + "\"");
			}
			
			try {
				bw.write( sbf.toString());
				bw.write( '\n');
			}
			catch ( Exception e) {
				
			}
			/*
			
			for ( j = 0; j < arrlog.length; j++) {
				
				try {
					if ( arrlog[j] != null ) {
						bw.write( arrlog[j].replace(',', ' ') );
					}
					
					bw.write( "\t");
					
				}
				catch ( Exception e ) {
				}		
			}
			try {
				bw.write( '\n');
			}
			catch ( Exception e) {
				
			}
			*/
		}
	}

	public void write_core ( String szwritefile, String sztimplestamp, StringBuffer sbhead, StringBuffer sbdata, int norgsize ) {
		
		String szwdate;
		String szwhh;
		String szwmi;
		String szwc;
		String sztmp;		
		int j;
		BufferedWriter bw;;

		sztmp = szwritefile;
		if ( m_hfileBW.containsKey(sztmp )) {
			bw = m_hfileBW.get( sztmp );
		}
		else {
			// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
			bw = UtilExt.getFileWriter(sztmp);
			UtilExt.createFile( sztmp + ".run");
			m_hfileBW.put( sztmp, bw );
		}
		
		try {
			bw.write(sbhead.toString());
			bw.write(sbdata.toString());
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
		try {
			szwdate = sztimplestamp.substring(0,8);
			szwhh = sztimplestamp.substring(8,10);
			szwc = sztimplestamp.substring(11,12);
			j = eBrotherUtil.getIntNumber(szwc) / 5;
			szwmi = sztimplestamp.substring(10,11) + ( j * 5 );
			if ( m_statkey[3].length() > 0
				&& ( ! m_statkey[3].equals ( szwdate )
				|| ! m_statkey[4].equals ( szwhh )
				|| ! m_statkey[5].equals ( szwmi ))
				) {
				m_sw.write( m_statkey, m_statdata );
				m_statdata[0] = 0; m_statdata[1] = 0; m_statdata[2] = 0;
			}

			m_statkey[3] = szwdate;
			m_statkey[4] = szwhh;
			m_statkey[5] = szwmi;
			m_statkey[6] = "IE";
			m_statcheck = j;
			m_statdata[0] +=1;
			m_statdata[1] += norgsize;
			
		}
		catch ( Exception e) {
			
		}
	}
	
	public void write_data ( Vector<String> vin, StringBuffer sbin, String szdelimiter, String szend ) {

		String szin = sbin.toString();
		
		StringBuffer sbheader = new StringBuffer ();
		StringBuffer sbdata = new StringBuffer ();
		String szvalue;
		String sztimestamp = "";
		String sztmp;
		int nidx;
		Vector<Integer> vcol;
		Vector<String> vname;
		Vector<String> vymd;

		//	20121023153236^0202^12^34^[2012.10.23 15:32:36][2][0_563] [container1-71] [TM-6216] xa session is started^logtime="2012.10.23 15:32:36" level="2" version="0_563"  thread="container1-71" logid="[TM-6216]" msg="xa session is started"$$		

		sbdata.append( szin.replaceAll ("[\"']", ""));
		sbdata.append( szdelimiter);

		vcol = m_cmeta.m_format_col;
		vname = m_cmeta.m_format_name;
		vymd = m_cmeta.m_format_ymd;
		// m_logger.debug ( "indata -> " + vin.toString());
		
		try {
			for ( int i = 0; i < vcol.size(); i++) {
				nidx = vcol.get(i);
				if ( nidx == 0 ) continue;
				sztmp = vname.get(i);
				sbdata.append( sztmp.replaceAll ( "[\"']", " "));
				sbdata.append( "=\"");
				szvalue = vin.get(nidx -1);
				sbdata.append( szvalue.replaceAll ( "[\"']", " ") );
				sbdata.append( "\" ");
				sztmp = vymd.get(i);
				// m_logger.debug(" nidx= " + nidx + ", value " + ( nidx-1) + " = " + szvalue + ", ymd meta = " + sztmp );
				if ( sztmp != null && sztmp.length() > 0 ) {
					try {
						sztimestamp = m_cmeta.m_timestamp.format (m_cmeta.m_dateformatin.parse( szvalue));
					}
					catch ( Exception e ) {
						
					}
				}
			}
		}
		catch ( Exception e ) {
			return;
		}

		sbdata.append( szend + "\n");

		String szwritefile = get_writefile ( sztimestamp, sbheader,sbdata );

		int kkkk =szwritefile.lastIndexOf("\\");
		
		if ( kkkk < 0 ) {
			kkkk = szwritefile.lastIndexOf("/");
		}

		String wwww = new String (szwritefile.substring( kkkk + 1 ));

		sbheader.append(sztimestamp);
		sbheader.append( szdelimiter);

		sbheader.append( wwww.replace('\n', '_'));
		sbheader.append( szdelimiter);
		sbheader.append( m_server.m_group);
		sbheader.append( szdelimiter);
		sbheader.append( m_server.m_svr);
		sbheader.append( szdelimiter);
		sbheader.append( m_server.m_instance);
		sbheader.append( szdelimiter);
		write_core ( szwritefile, sztimestamp, sbheader,sbdata, sbin.length() );
		
	}

	public boolean run_CallBack (String szlineorg, Hashtable<String, Object> hparam ) {
		
		String targetline;
		Matcher m = null;
		int j= 1;
		Pattern c_pattern, c_patternFinal;
		String szline;
		
		if ( true || m_server.iswn ) {
			// WN format check.
			szline = szlineorg;
			if ( ! szline.equals("LAST LINE" )) {
				if ( szline.indexOf("HTTP_CC_GUID") < 0 ) {
					m_sbcurhead.append(szline);
					return false;
				}
				if ( m_sbcurhead.length() == 0 ) {
					m_sbcurhead.append(szline);
					return false;
				}
			}

			// 누적된 LINE 에 대해 매칭 처리를 한다.
			try {
				targetline = m_sbcurhead.toString();
				String arlog [] = new String [ILogConst.EB_REC_HEADER_CNT];
				if ( EBLogParser.ccnt_util_parse_header( arlog, targetline, "tag.jsp" )) {
					// m_logger.debug ( targetline + "\n" + arlog.toString() );
					write_core ( arlog );
				}
				// m_logger.debug("ORG :" + m_sbcurhead.toString() );
			}
			catch ( Exception e ) {
				// m_logger.debug("ORG :" + m_sbcurhead.toString() );
			}
			// write_data ( vdata, m_sbcurhead, "^", "$$" );
			m_sbcurhead.setLength(0);
			// m_szcurhead = szline;
			m_sbcurhead.append(szline);
			/////////////////////////////////////////////////////////////////////////
			// 뭔가 COLUMN 에 맞추어서 WRITE 를 해야하겠지...
			return true;
		}

		Vector<String> vdata = new Vector<String> (); 

		// WN 이 아닌 원래 파일이다... parsing 대상 파일... 처리한다.
		c_pattern = m_cmeta.m_pattern;
		c_patternFinal = m_cmeta.m_patternFinal;

		// 이것이 신규 LINE 인지를 CHECK 하기 위함.
		String szline1 = szlineorg.replace('$', '_' );
		
		szline = szline1.replaceAll(":::::::::", ":::::" ).replaceAll(":::::::::", ":::::" ).replaceAll(":::::::::", ":::::" );
		
		if ( ! szline.equals("LAST LINE" )) {

			try {
				m = c_pattern.matcher( szline );
			}
			catch ( Exception e ) {
				
				// if ( m_szcurhead.equals("")) m_szcurhead = szline;
				// else m_szcurhead += "\n" + szline;
				
				if ( m_sbcurhead.length() != 0 ) m_sbcurhead.append('\n');
				m_sbcurhead.append(szline);

				// m_logger.debug("ORG :" + szline );
				return false;
			}

			// match 가 없다. 
			// 즉, 앞에 것이  Log 의 시작이고, 이것은 연속된 LINE 이다.
			// 따라서, 앞에 LINE 에 추가해 주고, return 한다.
			if ( ! m.find()) {
				if ( m_sbcurhead.length() != 0 ) m_sbcurhead.append('\n');
				m_sbcurhead.append(szline);
				// if ( m_szcurhead.equals("")) m_szcurhead = szline;
				// else m_szcurhead += "\n" + szline;
				return false;
			}
			else {
				// 처음 LINE 이고, 매칭된 자료가 있다.
				// 다음줄을 보기 위해 일단 return 한다.
				// if ( m_szcurhead.equals("")) {
				//	m_szcurhead = szline;
				if ( m_sbcurhead.length() == 0 ) {
					m_sbcurhead.append(szline);
					return false;
				}
			}
		}

		// 누적된 LINE 에 대해 매칭 처리를 한다.
		try {
			targetline = m_sbcurhead.toString() + ":::::::::";
			m = c_patternFinal.matcher( targetline );	
			while(m.find()) {
				for ( j = 1; j <= m.groupCount(); j++ ) {
					// m_logger.debug ( "LINE [" + j + "] = " + m.group( j).toString() );
					// ignore last column. 이것이 문제가 ... 마지막  column 은 데이터인데, 넘 길어서 
					// 데이터가 두배로 들어간다. 그래서 제외함.
					if ( j == m.groupCount()) vdata.add( "" );
					else vdata.add( m.group( j).toString());
				}
			}
		}
		catch ( Exception e ) {
			// m_logger.debug("ORG :" + m_sbcurhead.toString() );
		}

		write_data ( vdata, m_sbcurhead, "^", "$$" );

		m_sbcurhead.setLength(0);
		// m_szcurhead = szline;
		m_sbcurhead.append(szline);
		/////////////////////////////////////////////////////////////////////////
		// 뭔가 COLUMN 에 맞추어서 WRITE 를 해야하겠지...
		return true;
	}

	// 정말 어려운 case 는 ...
	// transworker 가 실행하다 중지. parserworker 가 따라 죽는데, .run 을 삭제 못하고 죽음.
	// 이 경우, .run 은 처리를 못한다.
	// 다른 어려운 경우 ...
	// 모두다 정상 처리. 그런데, batch 에서 또 읽어서 처리하려고 함. 계속 읽겠지. move 를 해야 하는데 ... 그것을 여기다 넣을까 ? 어렵네...
	// 어디선가는 파일을 옮겨 줘야 한다. !!!
	
	public void run () {
		
		String szfile;
		String szoutfile;
		int ncurline = 0, ndestline = 0;
		
		long curfilesize = 0L;
		long curfiledestsize = 0L;
		long lastfilesize = 0L;
		long lastfiledestsize = 0L;
		int ntargetfile = 0;
		long [] arsize;
		boolean bret = false;
		
		// 이거는 listner 에서 전체 directory scan 용으로 실행한다.
		Vector<String>vFileL;

		while ( true ) {
			
			vFileL = new Vector<String> ();
			
			try {	

				m_logger.info( "Parser Target Scan : " + m_szinfile );
				bret = Util.getFileListCore ( vFileL, true, m_szinfile, "" );

				ntargetfile = vFileL.size();
				m_logger.info( "Parser Target Scan : " + ntargetfile + " - " + m_szinfile + ", " + m_szfilter);
				
				// getFileListCore 는 directory 에 대해서만 file 을 count 한다.
				// 따라서, 현재 전송되는 파일에 대해서는 List 가 0 이다.
				if ( ! m_bmasterthread && ntargetfile == 0 ) {
					vFileL.add ( m_szinfile );
					bret = true;
				}
				ntargetfile = vFileL.size();
				////////////////////////////////////////////////////////////////////////////////////////////
				
				if ( bret ) {
					
					for ( int i = 0; i < ntargetfile; i++ ) {
						
						szfile = vFileL.get(i);

						if ( szfile.indexOf( m_szfilter) < 0 ) { //|| szfile.indexOf(".gz") >= 0 ) {
							m_logger.trace( "Parser Target Skip : " + szfile + " - " + m_szinfile + ", " + m_szfilter);
							continue;
						}
						
						if ( szfile.indexOf(".run") >= 0 ) continue;
						
						szoutfile = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile) + ".txt";
						m_logger.info( "Parser Target Out : " + szoutfile ); 
						// master thread 일때만 .run, 즉 현재 실행중인 것은 처리하지 않는다.
						// 개별 file 이 들어 올때는, TransWorker632 에서 ParserWorker 를 invoke 한다.
						// 이 경우, .run 이 만들어 진체로 넘어오는데, 이 경우에 대해서는 Parser 를 처리해야 한다.
						if ( m_bmasterthread && FileUtil.isFileExist( szoutfile + ".run")) continue;
						if ( m_bmasterthread && FileUtil.isFileExist(szfile + ".run")) continue;
						//////////////////////////////////////////////////////////////////////////////////////////////////

						curfilesize = FileUtil.getFileSize(szfile);
						curfiledestsize = FileUtil.getFileSize(szoutfile);
						if ( m_hstatPW.containsKey(szoutfile )) {
							arsize = m_hstatPW.get(szoutfile);
							if ( arsize[0] >= curfilesize && arsize[1] >= curfiledestsize ) {
								m_logger.trace( "Parser Target Skip ( file no change ) : " + szoutfile + ", " + szfile );
								continue;
							}
						}

						ndestline = FileUtil.getflines ( szoutfile);
						ncurline = FileUtil.getflines ( szfile);

						if ( ncurline == ndestline && ncurline > 0 ) {
							m_logger.trace( "Parser Target Skip ( file no change ) : " + szoutfile + ", " + szfile );
							continue;
						}

						setSkipLine (0);
						setSkipLine(ndestline);
						
						run_core ( szfile, szoutfile);

						curfilesize = FileUtil.getFileSize(szfile);
						curfiledestsize = FileUtil.getFileSize(szoutfile);
						m_logger.info( "put stat :  " + curfilesize + ", " + curfiledestsize );
						m_hstatPW.put( szoutfile, new long [] { curfilesize,curfiledestsize });
						
					}
				}
				
				vFileL.clear();
				vFileL = null;
				if ( m_isstopGlobal ) break;
				if ( m_bstandalone ) break;
				Thread.sleep(3000);
				// break;

			}
			catch ( Exception e ) {
				e.printStackTrace();
			}

		}
		
	}

	public void run_core  ( String sztargetfile, String szoutfile ) {
		
		String szkey;
		BufferedWriter bw;
		String sztype;
		File file = null;
		BufferedReader reader = null;
		boolean biswn = false;

		m_curdate = UtilExt.getY2K_Date();
	    try {
			
			m_logger.trace ( "run_core. target file : " + sztargetfile );
			m_server = get_server ( sztargetfile);

			if ( m_server == null ) {
				
				m_logger.trace ("No Server info ( run WN ) : " + sztargetfile );
				m_server = m_wnserver;
				biswn = true;

			}

			
			// set log info
			m_statkey [0] = m_server.m_group;
			m_statkey [1] = m_server.m_svr;
			m_statkey [2] = m_server.m_instance;
			
			sztype = m_server.m_instance;
			if ( ! m_hpattern.containsKey(sztype) && ! biswn ) {
				m_logger.debug ( "No pattern meta for : " + sztype );
				m_logger.debug ( "Check Pattern File : " );
			}

			m_cmeta = m_hpattern.get( sztype );
			m_sbcurhead = new StringBuffer ();
			
			m_szcurreadfile = sztargetfile;
							
			int nreadline = 0;
			file = new File(sztargetfile);

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(sztargetfile), "KSC5601"));
			
			String line = "";
			
			while(true) {

				try {

					line = reader.readLine();

					if(line == null) {
						
						if ( m_bmasterthread ) break;

						if ( m_isstop || m_isstopGlobal) break;

						Thread.sleep(5000);
						continue;
					}

					nreadline++;
					if ( nreadline < m_nskipline ) continue;
					run_CallBack ( line, null );

				}
				catch (Exception e_file) {
					
				}
			}

			run_CallBack ( "LAST LINE", null );

			for (Enumeration<String> ee = m_hfileBW.keys() ; ee.hasMoreElements() ; ) {
				szkey =  (String) ee.nextElement();
				bw = m_hfileBW.get(szkey);
				FileUtil.deleteFile( szkey + ".run" );
				try {
					bw.close();
					
				}
				catch ( Exception e ) {
					
				}
				m_hfileBW.remove(szkey);
			}

			///////////////////////////////////////////////////////////////////////////////////////////
			/*long curfilesize = FileUtil.getFileSize(sztargetfile);
			long curfiledestsize = FileUtil.getFileSize(szoutfile);
			long [] arsize;
			if ( m_hstatPW.containsKey(szoutfile )) {
				arsize = m_hstatPW.get(szoutfile);
				arsize [0] = curfilesize; arsize [1] =  curfiledestsize;
			}
			else {
				arsize = new long []{curfilesize, curfiledestsize };
				m_hstatPW.put( szoutfile, arsize );
			}
			*/
			///////////////////////////////////////////////////////////////////////////////////////////
			
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if(reader != null) {reader.close();}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
			
			for (Enumeration<String> ee = m_hfileBW.keys() ; ee.hasMoreElements() ; ) {
				try {
					szkey =  (String) ee.nextElement();
					bw = m_hfileBW.get(szkey);
					FileUtil.deleteFile( szkey + ".run" );
					try {
						bw.close();
					}
					catch ( Exception e ) {
						
					}
				}
				catch ( Exception e_qq) {
					
				}
			}
		}
	    
	    m_logger.debug("Worker thread end : " + sztargetfile );
	}
	
	public static void main(String[] args)  {

		// boolean btest = true;
		
		String szpattern_server = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\parser_server.txt";
		String szpatterhn = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\parser_pattern.txt";

		String szindir = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\in\\__app_eai_BizMaster_logs_vmiseai1_JeusServer_20121203_10_172.30.1.135_168568048.log";
		String szoutdir = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\out";	
		
		szpattern_server = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\server\\parser_server.txt";
		szpatterhn = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\server\\parser_pattern.txt";
		// szindir = "D:\\asp\\trans\\inbound\\10.3.110.118\\20130904114100_1378259982622_10.3.110.118_99999999.log";
		
		// szindir = "H:/eBrotherProject/rnd/project/onsure/inbound";
		// szoutdir = "H:/eBrotherProject/rnd/project/onsure/out_parser";

		szindir = "D:\\eBrotherProject\\rnd\\project\\axa\\inbound_4";
		szoutdir = "D:\\eBrotherProject\\rnd\\project\\axa\\outbound";

		String szfilter = "20140404";
		
		// szindir = szindir + "\\" + "192.168.1.34\\33333.txt";
		
		m_bstandalone = true;
		if ( args.length >= 4 ) {
			
			szpattern_server = args[0];
			szpatterhn = args[1];
			szindir = args[2];
			szoutdir = args[3];
		}
		else {
			// args = new String [] { szpattern_server, szpatterhn, szindir, szoutdir, szfilter };
			args = new String [] { null, null, szindir, szoutdir, szfilter };
		}

		ParserWorker c_p = new ParserWorker ( null, args);
		
		Thread th = new Thread ( c_p );
		th.start();
		c_p.stop();

		/*
		String dir = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\sample\\";
		String sample = "[2012.10.23 15:32:36][2][0_563] [container1-71] [TM-6216] xa session is started";

		String reg = "(\\d{4}).(\\d{2}).(\\d{2})\\s(\\d{2}).(\\d{2}).(\\d{2})";

		reg = "^\\[([\\d.]+)\\s([\\d:]+)\\]\\[(\\d*?)\\]\\[(.*?)\\]\\s\\[(.*?)\\]\\s\\[(.*?)\\]\\s(.*?)";
		// sample = "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"";
		// reg = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";

		try {
			SimpleDateFormat dout = new SimpleDateFormat ("yyyyMMddHHmmss");
			SimpleDateFormat din = new SimpleDateFormat ("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

			Date qda = din.parse("01/Oct/2000:09:27:09 -0400");
			String szout = dout.format( qda);
			m_logger.debug ( szout );
		}
		catch ( Exception e ) {
			
		}
		reg = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?) (.+?) (.+?)\" (\\d{3}) ([\\d-].*?)";
		sample = "223.33.186.91 - - [21/Oct/2012:00:27:33 +0900] \"GET /ext/images/common/quick_07.gif HTTP/1.0\" 304 100";

		reg = "^\\[([\\w:/]+\\s[+\\-]\\d{4})\\] \\[(.*?)\\] \\[(.*?)\\s([\\d.]+)\\] (.*?): (.*?)#####$";
		
		// [19/Nov/2012:00:02:36 +0900] [error] [client 1.241.16.196] File does not exist: /data/vpot/EarContent/webapp/vpot/jsp/ext/images/common/title_quick.gif 
		sample = "[19/Nov/2012:00:02:36 +0900] [error] [client 1.241.16.196] File does not exist: /data/vpot/EarContent/webapp/vpot/jsp/ext/images/common/title_quick.gif#####";
		
		m_logger.debug ( reg );
		m_logger.debug ( sample );
		Pattern p = null, pfinal = null;
		Matcher m = null;
		p = Pattern.compile( reg );
		m = p.matcher( sample );

		m_logger.debug ( "line --> " + sample );
		while(m.find()) {
			for ( int j = 1; j <= m.groupCount(); j++ ) {
				m_logger.debug ( "LINE [" + j + "] = " + m.group( j).toString() );

			}
		}
		*/
	}

}

