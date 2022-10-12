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

public class MusicOSPParser implements Runnable {

	private String m_PROJECTCD = "";
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	protected static Logger m_logger = Logger.getLogger( MusicOSPParser.class.getName());

	static Hashtable<String,String> mh_rex = new Hashtable<String,String> ();
	
	static {

		mh_rex.put ( "uci", "^(\\S+),(\\S+),(\\S+),(\\S+),(\\S+)" );

	}

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

	String _infile_encode = "UTF-8";
	
	
	BufferedWriter m_bw = null;
	
	final static int LOG_COLUMN = 11;
	final int LOG_COL_INLINE = 7;

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
	
	
	int m_nrownum = 0;
	
	class Server {
		public String m_ip = "";
		public String m_logtoken = "";
		public String m_group = "";
		public String m_svr = "";
		public String m_instance = "";
		public boolean iswn = false;
		public String m_note = "";
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

	public MusicOSPParser ( TransWorker632 ww, String [] args ) {

		String szpattern_server = args[0];
		String szpatterhn = args[1];
		String szindir = args[2];		
		String szoutdir = args[3];

		if ( args.length > 4 ) {
			m_szfilter =args[4]; 
		}
		
		m_ww = ww;
		
		if ( ww == null ) m_bmasterthread = true;
		
		m_bmasterthread = true;
		
		m_patternfile = szpatterhn;
		m_serverfile = szpattern_server;

		m_szinfile = szindir;
		m_szoutdir = szoutdir;
		
		m_PROJECTCD = System.getenv("SITE");
		
		if ( m_PROJECTCD == null ) {
			m_PROJECTCD = "";
			m_PROJECTCD = System.getProperty("SITE");
		}
		
		if ( m_PROJECTCD == null ) {
			
			m_PROJECTCD = "";

		}
		
		_infile_encode = System.getenv("SITE_INCODE");
		
		if ( _infile_encode == null ) {
			_infile_encode = "";
			_infile_encode = System.getProperty("SITE_INCODE");
		}
		
		if ( _infile_encode == null ) {
			
			_infile_encode = "UTF-8";

		}
		
		
		
		
		m_logger.info("ParserWorker (master : " + m_bmasterthread + ") : " + szindir );

		// set WN default
		m_wnserver = new Server ();
		m_wnserver.iswn = true;
		m_wnserver.m_instance = "wn";

		synchronized ( this ){
			
			if ( ! m_bmetaset ) {

				// m_logger.debug("Meta Setup : " + m_patternfile + ", " + m_serverfile);
				
				if ( m_patternfile != null && UtilExt.isFileExist(m_patternfile) ) set_patternmeta ( m_patternfile );
				if ( m_serverfile != null && UtilExt.isFileExist(m_serverfile) ) set_server ( m_serverfile);
				m_bmetaset = true;
			}
		}
	}

	int m_idxresult = 0;
	public String get_uuid ( String szdate, String svccode ) {
		
		
		if ( m_idxresult++ > 999999 ) m_idxresult = 100000;

		return szdate + System.currentTimeMillis() +  m_idxresult + svccode ;
		
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

	String m_prtfilenm = "";
	String m_callTYPE = "";

	/*
	 * file 이름에 service type 이 들어 오는 경우가 있습니다.
	 * 이것을 지원하기 위해 ...
	 * m1 :  
	 * 
		m1,soribada - LOG_SORIBADA_20140731_669.log.txt.gz
		m2,cjenm	 - CJENM_20140730.csv.txt.gz, CJENM_ASP_20140731.csv.txt.gz
		m3,bugs - 20140730_ASP.txt.gz, 20140731_DNL.txt.gz, 20140731_MR.txt.gz, 20140731_MV.txt.gz, 20140731_STR.txt.gz
		m4,ktmusic - 20140731_OLLEHMUSIC_20140801120406.txt.txt.gz
		m5,loen  - 20140730.log_LOEN_201407.txt.gz

	 * 
	 * 
	 * 
	 */
	public void set_ospinfo ( String szfile ) {
		
		m_prtfilenm = eBrotherUtil.getbase_name(szfile);

		if ( m_PROJECTCD.equals("m2")) {
			
			if ( szfile != null && szfile.indexOf("ASP") >= 0 ) {
				m_callTYPE = "ASP";
			}
			else m_callTYPE = "";
		}
		else if ( m_PROJECTCD.equals("m3")) {
			m_callTYPE = eBrotherUtil.getDelimitData(eBrotherUtil.getDelimitData(szfile, "_", 2 ), ".", 1 );
		}
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
	public void write_core ( boolean biserr, String [] arrlog) {
		
		String szwritefile, sztmp;
		String szwdate;
		String szwhh;
		String szwmi;
		String szwc;	
		int j;
		BufferedWriter bw;;
		
		szwritefile = m_szoutdir + FILE_SEPARATOR + UtilExt.getbasename(m_szcurreadfile);
		
		sztmp = szwritefile;

		if ( m_hfileBW.containsKey(sztmp )) {
			bw = m_hfileBW.get( sztmp );
		}
		else {
			// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
			bw = UtilExt.getFileWriter(sztmp, "UTF-8");
			UtilExt.createFile( sztmp + ".run");
			m_hfileBW.put( sztmp, bw );
		}

		for ( j = 0; j < arrlog.length; j++) {
			try {
				if ( j > 0 ) bw.write( "\t");
				if ( arrlog[j] == null ) bw.write( "" ); 
				else bw.write( arrlog[j] ); // .replace(',', ' ') );
			}
			catch ( Exception e ) {
			}		
		}
		try {
			bw.write( '\n');
		}
		catch ( Exception e) {
			
		}
		
		if ( biserr ) {

			szwritefile = m_szoutdir + FILE_SEPARATOR + UtilExt.getbasename(m_szcurreadfile) +".err";
			
			sztmp = szwritefile;
	
			// 에러 파일은 원본을 유지해야 함.
			// 따라서, 원본 파일인 EUC-KR 로 유지함.
			if ( m_hfileBW.containsKey(sztmp )) {
				bw = m_hfileBW.get( sztmp );
			}
			else {
				// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
				bw = UtilExt.getFileWriter(sztmp, "UTF-8");
				UtilExt.createFile( sztmp + ".run");
				m_hfileBW.put( sztmp, bw );
			}
	
			for ( j = 0; j < arrlog.length; j++) {
				try {
					if ( j > 0 ) bw.write( "\t");
					if ( arrlog[j] == null ) bw.write( "" ); 
					else bw.write( arrlog[j] ); // .replace(',', ' ') );
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
			bw = UtilExt.getFileWriter(sztmp, "UTF-8");
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

	final String EB_UCI_500 = "uci:i500";
	
	
	public String [] check_uci_fix ( String szuci_in ) {

		String [] szret = new String [2];
		Pattern pattern = null;
		Matcher matcher = null;	
		
		boolean bret = false;
		int nerr_type = 0;
		String szfix = "";
		int nuci500 = 0;
		int nuciformat = 0;
		String szuci;
		boolean bfix2 = false;
		
		String szright;
		
		// CHECK UCI ...
		// valid uci ...  uci:i500-USA0449819.1111117353-1
		String szpattern1 = "", szpattern2 = "", szpattern3 = "", szpattern4 = "", szpattern5 = "", sztmp = "";
		String sztoken1 = "", sztoken2 = "", sztoken3 = "", sztoken4 = "", sztoken5 = "";

		String szrex = "^(\\S+)-(\\S+)\\.(\\S+)-(\\S+)";
		
		boolean bpattern = false;
		
		if ( szuci_in.indexOf("\\n") >= 0 ) {
			bfix2 = true;
		}

		// very special case ...
		// 이것이 소리바다에서 이상하게 들어오는 case 가 있음.
		szuci = eBrotherUtil.getDelimitData( szuci_in, "\\n", 1 );
		///////////////////////////////////////////////////////////////////////////////
		
		// uci 가 없으면 모두 무시하고, 9로 설정하고 빠짐.
		if ( szuci.length() == 0 ) {
			szret [0] = "8";
			szret [1] = "";
			return szret;
		}
		
		try {

			pattern = Pattern.compile( szrex );
	    	matcher = pattern.matcher( szuci );
    	
			if ( matcher != null && ! matcher.matches()) {
				
				szpattern1 = matcher.group ( 0);
				szpattern2 = matcher.group ( 1);
				szpattern3 = matcher.group ( 2);
				szpattern4 = matcher.group ( 3);
				
				bpattern = true;
	    	}
			else {
				bpattern = false;
			}
			
		}
		catch ( Exception e) {
			bpattern = false;
		}

		// valid uci ...  uci:i500-USA0449819.1111117353-1
		szright = eBrotherUtil.getDelimitDataRight(szuci, "-", 2 );
		
		// -1 을 가지는데...
		// 이것이 아주가끔 없는 경우 발생.
		sztoken4 = eBrotherUtil.getDelimitData( szuci, "-", 3 );

		// uci:i500-
		sztoken1 = eBrotherUtil.getDelimitData( szuci, "-", 1 );
		
		// USA0449819.1111117353
		sztmp = eBrotherUtil.getDelimitData( szuci, "-", 2 );
		
		// USA0449819.
		sztoken2 = eBrotherUtil.getDelimitData( sztmp, ".", 1 );
		
		// .1111117353
		sztoken3 = eBrotherUtil.getDelimitData( sztmp, ".", 2 );
		
		szfix = EB_UCI_500 + "-" + sztoken2 + "." + sztoken3 + "-" + sztoken4;

		// 페턴 및 정확히 데이터가 분리되는 경우에 한하여 ...
		if ( szfix.equals(szuci_in) && szuci_in.length() > 0 ) {
			szret [0] = "0";
			szret [1] = szfix;
			return szret;
		}
		
		// 패턴은 정상인데, UCI:i500 등등에서 오류가 난 경우 ..
		if ( (szpattern2.equals(sztoken2) && szpattern3.equals(sztoken3) && szpattern4.equals(sztoken4)) || bfix2 ) {
			// 말그대로 .. UCI에 대.소문자 들어가 거나 ... 좀 특수한 case 임
			szret [0] = "1";
			szret [1] = szfix;
			return szret;
		}

		// 문제는 마지막, -1 이 없는 경우가 좀 있는데 ... 이것을 복구할 것인가 ?
		// 결론은, 복구하지 말자 ?
		// 이유는 length 등 뭔가 가지고 check 해야 하는데, 많이 어렵다.

		if ( szfix.equals( EB_UCI_500 + "-" + szright ) && szright.length() > 0 ) {
			szret [0] = "1";
			szret [1] = szfix;
			return szret;
		}

		// 13096707,3556581,25948,20140701-04:15:46, <-- 이런 형식이 있음.
		// UCI 에 그냥 ### 가 들어감.
		szret [0] = "8";
		szret [1] = szfix;

		return szret;

	}
	
	// uci:i500-KRA0130574.1111581540-1,01078289,F11,20140728-20:42:06,
	public boolean check_log ( String arlog [] ) {
		
		boolean bret = false;
		String szdategood = "9";
		String szucierr = "9";
		
		String szuci, szdate;
		
		// CHECK UCI ...
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		szuci = arlog [1];
		szdate = arlog[4];
		
		// if ( szuci.length() == 0 ) 
		try {
			df.parse(szdate);
			szdategood = "0";

		}
		catch ( Exception e ) {
			szdategood = "1";
		}
		
		String szret [] = check_uci_fix ( szuci );

		arlog [6] = m_PROJECTCD;
		arlog [7] = m_callTYPE;
		arlog [8] = szret[1];
		arlog [9] = szret[0];
		arlog [10] = szdategood;

		arlog [0] = get_uuid ( arlog[4], m_PROJECTCD);

		// arlog [13] = szdategood;
		
		return true;

	}

	/*
	 * 
	 */
	public boolean run_CallBack (String szlineorg, Hashtable<String, Object> hparam ) {
		
		String sztemp;
		String targetline;
		Matcher m = null;
		int j= 1;
		Pattern c_pattern, c_patternFinal;
		String szline;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		boolean biserr = false;
		
		if ( true || m_server.iswn ) {
			
			// WN format check.
			szline = szlineorg;
			if ( szline.equals("LAST LINE" )) {
				/*if ( szline.indexOf("HTTP_CC_GUID") < 0 ) {
					m_sbcurhead.append(szline);
					return false;
				}
				if ( m_sbcurhead.length() == 0 ) {
					m_sbcurhead.append(szline);
					return false;
				}
				*/
				return false;
			}

			// 누적된 LINE 에 대해 매칭 처리를 한다.
			try {

				// targetline = m_sbcurhead.toString();

				m_nrownum++;
				targetline = szline;
				
				String arlog [] = new String [LOG_COLUMN];
				
				m_logger.trace ( "LINE 11 : " + targetline);
				
				for ( int kk = 1; kk < LOG_COLUMN; kk++ ) {
					sztemp = eBrotherUtil.getDelimitData( targetline, ",", kk );
					m_logger.trace ( "WRITE : " + kk + " ( " + LOG_COLUMN + ") " + sztemp  );
					arlog[ kk ] = eBrotherUtil.getDelimitData( targetline, ",", kk );
				}

				if ( m_callTYPE != null && m_callTYPE.length() > 0 ) {
					// arlog [2] = m_callTYPE;
				}

				// date check ...
				sztemp = arlog [4];
				try {
					arlog [4] = sztemp.replaceAll(":", "").replaceAll("-", "");
				}
				catch ( Exception e ) {
					arlog [4] = sztemp;
				}

				///////////////////////////////////////////////////////////////////////////////////
				for ( int k = 1; k <= 4; k++ ) {
					if ( arlog[k] == null || arlog[k].length() == 0 ) {
						biserr = true;
						break;
					}
				}
				
				check_log ( arlog );

				if ( ! arlog [9].equals("0")) biserr = true;
			
				// 일단 tab 으로 한번 바꾸어 본다.
				sztemp = szline.replace( '\t', '`' );
				arlog [LOG_COL_INLINE ] =  sztemp;

				arlog[0] = "" + m_nrownum;
				write_core ( biserr, arlog );
				
				// m_logger.debug("ORG :" + m_sbcurhead.toString() );
			}
			catch ( Exception e ) {
				// m_logger.debug("ORG :" + m_sbcurhead.toString() );
			}

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
		String szoutfile, szouterr;
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
						
						szoutfile = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile);
						szouterr = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile) + ".err";
						
						// master thread 일때만 .run, 즉 현재 실행중인 것은 처리하지 않는다.
						// 개별 file 이 들어 올때는, TransWorker632 에서 ParserWorker 를 invoke 한다.
						// 이 경우, .run 이 만들어 진체로 넘어오는데, 이 경우에 대해서는 Parser 를 처리해야 한다.
						if ( m_bmasterthread && FileUtil.isFileExist( szoutfile + ".run")) continue;
						if ( m_bmasterthread && FileUtil.isFileExist(szfile + ".run")) continue;
						//////////////////////////////////////////////////////////////////////////////////////////////////

						/*
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
						*/

						setSkipLine (0);
						// setSkipLine(ndestline);

						run_core ( szfile, szoutfile, szouterr );

						curfilesize = FileUtil.getFileSize(szfile);
						curfiledestsize = FileUtil.getFileSize(szoutfile);
						m_logger.trace ( "put stat :  " + curfilesize + ", " + curfiledestsize );
						m_hstatPW.put( szoutfile, new long [] { curfilesize,curfiledestsize });

					}
				}

				vFileL.clear();
				vFileL = null;
				if ( m_isstopGlobal ) break;
				if ( m_bstandalone ) break;
				Thread.sleep(30000);
				// break;

			}
			catch ( Exception e ) {
				e.printStackTrace();
			}

		}
		
	}

	public void run_core  ( String sztargetfile, String szoutfile, String szouterr ) {
		
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

			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(sztargetfile), _infile_encode));
			
			String line = "";
			
			set_ospinfo ( sztargetfile );

			m_logger.debug ( "File OPEN : " + sztargetfile );
			
			m_nrownum = 0;
			
			String szline;
			while(true) {

				try {

					line = reader.readLine();

					if(line == null) {

						if ( m_bmasterthread ) break;

						if ( m_isstop || m_isstopGlobal) break;

						Thread.sleep(5000);
						continue;
					}

					m_logger.trace ( "line : " + line );
					
					// 처음부터 그냥 바꾼다...
					szline = line.replaceAll("\t", "" );
					nreadline++;
					if ( nreadline < m_nskipline ) continue;
					run_CallBack ( szline, null );

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
		
		String szpattern_server = "";
		String szpatterhn = "D:\\workspace_nibbler\\NibblerExtractor\\dist\\parser_pattern.txt";

		String szindir = "";
		String szoutdir = "";	
		
		szpattern_server = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\server\\parser_server.txt";
		szpatterhn = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\server\\parser_pattern.txt";
		// szindir = "D:\\asp\\trans\\inbound\\10.3.110.118\\20130904114100_1378259982622_10.3.110.118_99999999.log";
		
		// szindir = "H:/eBrotherProject/rnd/project/onsure/inbound";
		// szoutdir = "H:/eBrotherProject/rnd/project/onsure/out_parser";

		szindir = "D:\\eBrotherProject\\rnd\\project\\axa\\inbound_4";
		szoutdir = "D:\\eBrotherProject\\rnd\\project\\axa\\outbound";

		szindir = "D:\\eBrotherProject\\rnd\\project\\osp\\inbound";
		
		szoutdir = "D:\\eBrotherProject\\rnd\\project\\osp\\outbound";
		String szfilter = "20150207";

		// szindir = szindir + "\\" + "192.168.1.34\\33333.txt";
		
		m_bstandalone = true;
		
		System.setProperty("SITE", "m1");
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
		
		m_logger.info("MUSIC LOG : OSP Parser Start" );
		m_logger.info("ARGS : " + args.toString());
		MusicOSPParser c_p = new MusicOSPParser ( null, args);

		Thread th = new Thread ( c_p );
		th.start();
		c_p.stop();

		m_logger.info("MUSIC LOG : OSP Parser End" );
	}

}

