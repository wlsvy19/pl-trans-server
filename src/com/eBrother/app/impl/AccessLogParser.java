package com.eBrother.app.impl;

import com.eBrother.app.impl.ParserWorker.Meta;
import com.eBrother.util.FileUtil;
import com.eBrother.util.Util;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class AccessLogParser implements ILogConst {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	protected static Logger m_logger = Logger.getLogger( AccessLogParser.class.getName());
	
	int m_nlogtype = -1;
	String m_logrex = null;
	Vector<String> mv_szrex = new Vector<String> ();

	boolean m_bDebug = false;
	Pattern m_pattern = null;
	Matcher m_matcher = null;
	
	//public EBLog m_eblog = new EBLog (); 
	static Hashtable<String,String> mh_rex = new Hashtable<String,String> ();
	static String WN_ACCESS_TOKEN [] = { "date", "time", "s-sitename", "s-ip", "cs-method", "cs-uri-stem"
		, "cs-uri-query", "s-port", "cs-username", "c-ip",  "cs(User-Agent)"
		, "sc-status",  "sc-substatus", "sc-win32-status", "cs(Referrer)", "sc-bytes" }; 

	
	public int m_nCOL_CIP = -1;
	public int m_nCOL_DATE = -1;
	public int m_nCOL_TIME = -1;
	public int m_nCOL_METHOD = -1;
	public int m_nCOL_URL = -1;
	public int m_nCOL_QUERY = -1;
	public int m_nCOL_STATUS = -1;
	public int m_nCOL_SIZE = -1;
	public int m_nCOL_REF = -1;
	public int m_nCOL_AGENT = -1;
	
	Pattern m_patternFinal;
	StringBuffer m_sb;
	Meta m_cmeta;
	String m_szoutdir;
	String m_curdate;
	String m_szinfile;
	String m_szoutfile = null;
	String m_szrundate = null;
	
	BufferedWriter m_bw = null;
		
	static {

		mh_rex.put ( "common", "^(\\S+) (\\S+) (\\S+) \\[([^:]+):(\\d+:\\d+:\\d+) ([^\\]]+)\\] \"(\\S+) (.+?) (\\S+)\" (\\S+) (\\S+) (\\S+)" );
		
		mh_rex.put ( "common", "^(\\S+) (\\S+) (\\S+) \\[([^:]+):(\\d+:\\d+:\\d+) ([^\\]]+)\\] \"(\\S+) (.+?) (\\S+)\" (\\S+) (\\S+)" );
		// "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"";
		// mh_rex.put ( "common", "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+)");
		// ($host, $ident_user, $auth_user, $date, $time, $time_zone, $method, $url, $protocol, $status, $bytes)
		// "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450

		mh_rex.put ( "extgs", "^(\\S+) \\[([^:]+):(\\d+:\\d+:\\d+) ([^\\]]+)\\] \"(\\S+) (.+?) (\\S+)TP/1.[0-9]\" (\\S+) (\\S+) \"([^\"]+)\" \"([^\"]+)\" \"(.+).$");
		
		mh_rex.put ( "ext", "^(\\S+) (\\S+) (\\S+) \\[([^:]+):(\\d+:\\d+:\\d+) ([^\\]]+)\\] \"(\\S+) (.+?) (\\S+)\" (\\S+) (\\S+) \"([^\"]+)\" \"([^\"]+)\"");
		// ($host, $ident_user, $auth_user, $date, $time, $time_zone, $method, $url, $protocol, $status, $bytes, $referer, $agent)
		// "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"";

		// iis ...
		mh_rex.put ( "msext", "^(\\d+-\\d+-\\d+) (\\d+:\\d+:\\d+) (\\S+) (\\S+) (\\S+) (\\S+) (\\d+) (\\S+) (\\S+) (\\S+) (\\d+) (\\d+)");
		/*
		#Software: Microsoft Internet Information Services 6.0
		#Version: 1.0
		#Date: 2011-06-29 15:00:07
		#Fields: date time s-ip cs-method cs-uri-stem cs-uri-query s-port cs-username c-ip cs(User-Agent) sc-status sc-substatus 
		2011-06-29 15:00:07 210.118.63.49 GET /personal/anywebprint/en/cap_2_3.txt - 80 - 77.23.51.57 Mozilla/5.0+(Windows+NT+6.1)+AppleWebKit/534.30+(KHTML,+like+Gecko)+Chrome/12.0.742.100+Safari/534.30 200 0
		*/

		mh_rex.put ( "spider", "(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)|(\\S+)");
		/*
		 * #Software: Microsoft Internet Information Services 6.0
		#Version: 1.0
		#Date: 2002-05-02 17:42:15
		#Fields: date time c-ip cs-username s-ip s-port cs-method cs-uri-stem cs-uri-query sc-status cs(User-Agent)
		2002-05-02 17:42:15 172.22.255.255 - 172.30.255.255 80 GET /images/picture.jpg - 200 Mozilla/4.0+(compatible;MSIE+5.5;+Windows+2000+Server)
		 */


	}
	
	String m_szfilter;
	
	public AccessLogParser ( TransWorker632 ww, String [] args ) {

		String szrundate = args[0];
		String szindir = args[1];		
		String szoutdir = args[2];
		
		m_szrundate = szrundate;

		m_szinfile = szindir;
		m_szoutdir = szoutdir;

	}	
	
	public void set_debug ( boolean bset ) {
		
		m_bDebug = bset;
	}
	
	/*
	 * real parser for each data format.
	 */
	public boolean get_log ( String [] saz_dest, String szline ) {
		
		String sztemp;
		String szrex;
		String sztemp2;
		// if ( saz_dest == null ) return false;
		
		
		if ( szline.charAt(0) == '#' ) return false;

		for(int i=0;i<saz_dest.length;i++)
		{
		    saz_dest[i] = null;
		}

		if ( m_nlogtype == 1 ) {
			// MS TYPE
			saz_dest[0] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_DATE ).replace( "-", "" );
			saz_dest[1] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_TIME ).replace( ":", "" );
			saz_dest[2] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_CIP );
			saz_dest[3] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_URL );
			saz_dest[4] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_QUERY );
			saz_dest[5] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_REF );
			saz_dest[6] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_AGENT );
			saz_dest[7] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_STATUS );
			saz_dest[8] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_SIZE );
		}
		else if (  m_nlogtype == 21 ) {
			// i.e. ext
			try {
				
		    	szrex = mh_rex.get("extgs");
		    	m_pattern = Pattern.compile( szrex );
		    	m_matcher = m_pattern.matcher( szline );
		    	
				if ( ! m_matcher.matches()) {
					
					// m_bwerr.write( szline );
					// m_bwerr.write("\n");
		    		return false;
		    	}
		    	
				saz_dest[0] = eBrotherUtil.getYMD4Access( m_matcher.group ( m_nCOL_DATE ));
				saz_dest[1] = m_matcher.group ( m_nCOL_TIME).replace( ":", "" );
				saz_dest[2] = m_matcher.group ( m_nCOL_CIP );
				saz_dest[3] = m_matcher.group ( m_nCOL_URL );
				saz_dest[4] = "";
				saz_dest[5] = m_matcher.group ( m_nCOL_REF );
				
				saz_dest[6] = m_matcher.group ( m_nCOL_AGENT );
				
				saz_dest[7] = m_matcher.group ( m_nCOL_STATUS );
				saz_dest[8] = m_matcher.group ( m_nCOL_SIZE );
				//saz_dest[9] = m_matcher.group ( m_nCOL_COOKIE );
				
				// GS ...
				// saz_dest[6] = "";
				sztemp = saz_dest[6];
				
				if ( sztemp != null ) {
					saz_dest[6] = sztemp.replace(',', ' ' );
				}
				
				/*
				 * m_eblog.LOG_COOKIE = saz_dest[9]; m_eblog.LOG_HMS = saz_dest[1];
				 * m_eblog.LOG_IP = saz_dest[2]; m_eblog.LOG_QUERY = ""; m_eblog.LOG_REF =
				 * saz_dest[5]; m_eblog.LOG_YMD = saz_dest[0]; m_eblog.LOG_AGENT = saz_dest[6];
				 * m_eblog.LOG_COOKIE = saz_dest[9]; m_eblog.LOG_SIZE = saz_dest[8];
				 * m_eblog.LOG_STAT = saz_dest[7]; m_eblog.LOG_URL = saz_dest[3];
				 */
				
				// 
				/*
				 * m_eblog.LOG_YMDHMS = saz_dest[0] + saz_dest[1];
				 * 
				 * // 
				 * 
				 * try { URL url = new URL ( m_eblog.LOG_URL ); } catch ( Exception e ) { return
				 * false; }
				 */
				
			}
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}
		}		
		else if (  m_nlogtype == 2 ) {
			// i.e. ext
			try {
		    	szrex = mh_rex.get("ext");
		    	m_pattern = Pattern.compile( szrex );
		    	m_matcher = m_pattern.matcher( szline );
				saz_dest[0] = eBrotherUtil.getYMD4Access( m_matcher.group ( m_nCOL_DATE ));
				saz_dest[1] = eBrotherUtil.getDelimitData( szline, " ", m_nCOL_TIME ).replace( ":", "" );
				saz_dest[2] = m_matcher.group ( m_nCOL_CIP );
				saz_dest[3] = m_matcher.group ( m_nCOL_URL );
				saz_dest[4] = "";
				saz_dest[5] = m_matcher.group ( m_nCOL_REF );
				saz_dest[6] = m_matcher.group ( m_nCOL_AGENT );
				saz_dest[7] = m_matcher.group ( m_nCOL_STATUS );
				saz_dest[8] = m_matcher.group ( m_nCOL_SIZE );
			}
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}
		} 
		else if (  m_nlogtype == 3 ) {
			// i.e. common
			try {
		    	// szrex = mh_rex.get("common");
		    	// System.out.println ( szrex );
		    	// m_pattern = Pattern.compile( szrex );
				m_matcher = m_pattern.matcher( szline );
				if ( ! m_matcher.matches()) {
					System.out.println ( " err " );
					return false;
				}
				else {
					saz_dest[0] = eBrotherUtil.getYMD4Access( m_matcher.group ( m_nCOL_DATE ));
					saz_dest[1] = m_matcher.group ( m_nCOL_TIME ).replace( ":", "" );
					saz_dest[2] = m_matcher.group ( m_nCOL_CIP );
					saz_dest[3] = m_matcher.group ( m_nCOL_URL );
					saz_dest[4] = "";
					saz_dest[5] = "";
					saz_dest[6] = "";
					saz_dest[7] = m_matcher.group ( m_nCOL_STATUS );
					saz_dest[8] = m_matcher.group ( m_nCOL_SIZE );
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}
		}
		else if ( m_nlogtype == 4 ) { 

			// KB LOG parser.
			try {

				saz_dest[0] = eBrotherUtil.getDelimitData( szline, "|", 1 ).substring(0,14).trim(); // full date
				saz_dest[1] = "";	// time
				saz_dest[10] = eBrotherUtil.getDelimitData( szline, "|", 2 ).trim();// server domain id

				// userid
				// format : userid_/gender_/age_/usercat1_/usercat2
				saz_dest[9] = eBrotherUtil.getDelimitData( szline, "|", 6 ).trim();	// user customer #.
				
				sztemp = eBrotherUtil.getDelimitData( szline, "|", 4 ).trim();
				
				if ( sztemp.equals("GUEST") || sztemp.length() == 0 || saz_dest[9].length() == 0  ) saz_dest[9] = "";
				else {
					
					sztemp = saz_dest[9] + "_/" 	// user info #
							+  eBrotherUtil.getDelimitData( szline, "|", 16 ).trim() // gender
							+ "_/" +  eBrotherUtil.getDelimitData( szline, "|", 17 ).trim() // age
							+ "_/" +  eBrotherUtil.getDelimitData( szline, "|", 18 ).trim() // ucatcode1
							+ "_/" +  eBrotherUtil.getDelimitData( szline, "|", 19 ).trim();; // ucatcode2
					saz_dest[9] = sztemp;
				}
				/////////////////////////////////////////////////////////////////////////////////
				// URL : http://life.kbcard.com:80/CXLSOCMC0001.cms?responseContentType=json&opt=campusList2&key
				saz_dest[3] = eBrotherUtil.getDelimitData( szline, "|", 7 ).trim().replace ( ":80/", "/");	// url

				int k_pos = 0;
				
				k_pos = saz_dest[3].indexOf('?');
				saz_dest[4] = "";
				if ( k_pos >= 0 ) {
					sztemp = saz_dest[3].substring( 0, k_pos );
					sztemp2 = saz_dest[3].substring( k_pos +1 );
					saz_dest[4] = sztemp2;
					saz_dest[3] = sztemp;
				}

				sztemp = eBrotherUtil.getDelimitData( szline, "|", 13 ).trim();	// ?붿껌 . ?묐떟				
				if ( sztemp.length() > 0 ) {
					if ( saz_dest[4].length() > 0 ) saz_dest[4] += "&";
					saz_dest[4] += "log_rtype=" + sztemp;
				}

				sztemp = eBrotherUtil.getDelimitData( szline, "|", 14 ).trim();	// ?먮윭肄붾뱶				
				if ( sztemp.length() > 0 ) {
					if ( saz_dest[4].length() > 0 ) saz_dest[4] += "&";
					saz_dest[4] += "log_err=" + sztemp;
				}

				sztemp = eBrotherUtil.getDelimitData( szline, "|", 3 ).trim();	// 嫄곕옒 異붿쟻 肄붾뱶			
				if ( sztemp.length() > 0 ) {
					if ( saz_dest[4].length() > 0 ) saz_dest[4] += "&";
					saz_dest[4] += "log_tx=" + sztemp;
				}
				
				
				saz_dest[5] = eBrotherUtil.getDelimitData( szline, "|", 11 ).trim();	// referrer

				saz_dest[2] = eBrotherUtil.getDelimitData( szline, "|", 12 ).trim();	// IP
				
				saz_dest[7] = eBrotherUtil.getDelimitData( szline, "|", 15 ).trim();	// idanon

				////////////////////////////////////////////////////////////////////////////
				/*sztemp = eBrotherUtil.getDelimitData( szline, "|", 21 ).trim();
				saz_dest[4] = sztemp.replace("<UL>", "&" )
						.replace("</ul>", "&" ).replace("[", "").replace("]", "" )
						.replace("<li>", "&" );
				*/
				// saz_dest[3] : URL have full URL style. need to parse.
				 	// query string --> not fixed
				////////////////////////////////////////////////////////////////////////////
				
				saz_dest[6] = eBrotherUtil.getDelimitData( szline, "|", 20 ).trim();	// user_agent
				
				
				//saz_dest[8] = saz_dest[0].substring(0,11) + "00_" + saz_dest[7].substring(0,14)
				//					+ saz_dest[7].substring(16);	// idvisit
				if ( saz_dest[7].length() > 16 ) { 
					saz_dest[8] = saz_dest[0].substring(0,11) + "000_" + saz_dest[7].substring(14);
				}
				else {
					saz_dest[8] = saz_dest[0].substring(0,11) + "000_" + saz_dest[7];
				}

				sztemp = eBrotherUtil.getDelimitData( szline, "|", 13 );

				for ( int kkk = 0; kkk < saz_dest.length; kkk++ ) {
					// LOG BUG FIX
					// sometimes, there is abnormal log, NULNULLNULLNULL style.
					sztemp2 = saz_dest [kkk];
					if ( sztemp2 != null ) {
						sztemp = sztemp2.replace((char)0x00,'`').replace( "`", "");
						saz_dest [kkk] = sztemp.trim();
					}
					////////////////////////////////////////////////////////////////////////
					sztemp2 = saz_dest [kkk];
					if ( sztemp2 != null ) {
						saz_dest [kkk] = sztemp2.replace( '\"', ' ' ).replace( '\'', ' ' ).replace( ',', ' ' );
					}
				}
				
				if ( m_bDebug ) {
					
					System.out.println ( "\n-----------------------------------" );
					System.out.println ( szline );
					for ( int kkk = 0; kkk < 22; kkk++ ) {
						
						// System.out.println ( eBrotherUtil.getDelimitData( szline, "|", kkk ));
						try {
							
							System.out.println ( "" + kkk + "=[" + saz_dest[kkk] + "]");
						}
						catch ( Exception e ) {
							
						}
					}
					System.out.println ( "" );
				}
					/*
					saz_dest[0] = eBrotherUtil.getYMD4Access( m_matcher.group ( m_nCOL_DATE ));
					saz_dest[1] = m_matcher.group ( m_nCOL_TIME ).replace( ":", "" );
					saz_dest[2] = m_matcher.group ( m_nCOL_CIP );
					saz_dest[3] = m_matcher.group ( m_nCOL_URL );
					saz_dest[4] = "";
					saz_dest[5] = "";
					saz_dest[6] = "";
					saz_dest[7] = m_matcher.group ( m_nCOL_STATUS );
					saz_dest[8] = m_matcher.group ( m_nCOL_SIZE );
					*/
			}
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}

		}
		
		if ( m_nlogtype != 4 ) {
			saz_dest[6] = saz_dest[6].replace ( '+', ' ');
			saz_dest[6] = saz_dest[6].replace ( ',', ' ');
		}
		return true;
	}
	
	public void set_msmeta ( String szfields ) {
		
		int i, j, k;
		String sztemp;

		k = WN_ACCESS_TOKEN.length;
		for ( i = 1;; i++ ) {
			
			sztemp = eBrotherUtil.getDelimitData( szfields, " ", i );
			if ( sztemp.length() < 1 ) break;
			
			/*			
				int m_nCOL_CIP = -1;
				int m_nCOL_DATE = -1;
				int m_nCOL_TIME = -1;
				int m_nCOL_METHOD = -1;
				int m_nCOL_URL = -1;
				int m_nCOL_QUERY = -1;
				int m_nCOL_STATUS = -1;
				int m_nCOL_SIZE = -1;
				int m_nCOL_REF = -1;
				int m_nCOL_AGENT = -1;
				static String WN_ACCESS_TOKEN [] = { "date", "time", "s-sitename", "s-ip", "cs-method", "cs-uri-stem"
					, "cs-uri-query", "s-port", "cs-username", "c-ip",  "cs(User-Agent)"
					, "sc-status",  "sc-substatus", "sc-win32-status", "cs(Referrer)", "sc-bytes" }; 	
			*/
			for ( j = 0; j <k && ! WN_ACCESS_TOKEN[j].equalsIgnoreCase(sztemp); j++ );
			switch ( j ) {
			case 0 : 
				m_nCOL_DATE = i-1;
				System.out.println ( " m_nCOL_DATE = " + m_nCOL_DATE );
				break;
			case 1 :
				m_nCOL_TIME = i-1;
				System.out.println ( " m_nCOL_TIME = " + m_nCOL_TIME );
				break;
			case 5 :
				m_nCOL_URL = i-1;
				System.out.println ( " m_nCOL_URL = " + m_nCOL_URL );
				break;
			case 6 :
				m_nCOL_QUERY = i-1;
				System.out.println ( " m_nCOL_QUERY = " + m_nCOL_QUERY );
				break;
			case 9 :
				m_nCOL_CIP = i-1;
				System.out.println ( " m_nCOL_CIP = " + m_nCOL_CIP );
				break;
			case 10 :
				m_nCOL_AGENT = i-1;
				System.out.println ( " m_nCOL_AGENT = " + m_nCOL_AGENT );
				break;
			case 11 :
				m_nCOL_STATUS = i-1;
				System.out.println ( " m_nCOL_STATUS = " + m_nCOL_STATUS );
				break;
			case 14 :
				m_nCOL_REF = i-1;
				System.out.println ( " m_nCOL_REF = " + m_nCOL_REF );
				break;
			case 15 :
				m_nCOL_SIZE = i-1;
				System.out.println ( " m_nCOL_SIZE = " + m_nCOL_SIZE );
				break;
			}
		}
	}
	
	public boolean set_logmeta ( int metatype  ) {
		
		boolean bret = false;
		m_nlogtype = metatype;
		
		if ( metatype == 4 ) {
			
			m_logrex = "spider";
	    	m_nCOL_CIP = 12;
	    	m_nCOL_DATE = 1;
	    	m_nCOL_TIME = -1;
	    	m_nCOL_URL = 9;
	    	m_nCOL_STATUS = 14;
	    	m_nCOL_SIZE = -1;
	    	m_nCOL_REF = 11;
	    	m_nCOL_AGENT = 20;
	    				
		}
		else if ( metatype == 3 ) {
			
	    	String szrex = mh_rex.get("common");
	    	m_pattern = Pattern.compile( szrex );
		    	m_nCOL_CIP = 1;
		    	m_nCOL_DATE = 4;
		    	m_nCOL_TIME = 5;
		    	m_nCOL_URL = 8;
		    	m_nCOL_STATUS = 10;
		    	m_nCOL_SIZE = 11;					    	
		    	m_nlogtype = 3;

		}
		
		return bret;	
	}
	
	public int set_logmeta ( String szfile ) {
		
		String sztemp;
		int i = 1;
		String szrex;
		String szrex_type = null;
		m_nlogtype = -1;
		BufferedReader in = null;
		String sLine;
		try {
			
			if ( szfile.indexOf(".gz") > 0 ) {
				in = new BufferedReader(new InputStreamReader( new GZIPInputStream (new FileInputStream(szfile))));
			}
			else {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(szfile), "utf-8"));
			}
			
			try {

				while ((sLine = in.readLine()) != null) {

					sLine = sLine.trim();
					if (sLine.length() == 0) continue;
					if (sLine.charAt(0) == '#' ) {
						if ( sLine.indexOf("#Fields: ") >= 0 ) {
							System.out.println ( "sLine -> " + sLine );
							set_msmeta ( sLine );
							m_nlogtype = 1;
							break;
						}
						continue;
					}

					System.out.println ( sLine );
					// mv_szrex.add(sLine );
					szrex = mh_rex.get("ext");
					m_pattern = Pattern.compile( szrex );
					m_matcher = m_pattern.matcher( sLine );
				    
				    if ( m_matcher.matches()) {
				    	szrex_type = "ext";
				    	m_nCOL_CIP = 1;
				    	m_nCOL_DATE = 4;
				    	m_nCOL_TIME = 5;
				    	m_nCOL_URL = 8;
				    	m_nCOL_STATUS = 10;
				    	m_nCOL_SIZE = 11;
				    	m_nCOL_REF = 12;
				    	m_nCOL_AGENT = 13;
				    	
				    	m_nlogtype = 2;
				    	break;
				    }
				    else {
				    	szrex = mh_rex.get("common");
				    	m_pattern = Pattern.compile( szrex );
				    	m_matcher = m_pattern.matcher( sLine );
				    	if ( m_matcher.matches()) {
				    		szrex_type = "common";
					    	m_nCOL_CIP = 1;
					    	m_nCOL_DATE = 4;
					    	m_nCOL_TIME = 5;
					    	m_nCOL_URL = 8;
					    	m_nCOL_STATUS = 10;
					    	m_nCOL_SIZE = 11;					    	
					    	m_nlogtype = 3;
					    	break;
				    	}
				    }

				}
			} catch (Exception e) {
				System.out.print("[ParserAccessImpl] Error getFileContent :");
				System.out.println(szfile);
			}
			
	    	System.out.println( "Log Type : " + szrex_type );
	    	for ( int ii = 1; ii <= m_matcher.groupCount(); ii++ ) {
	    		System.out.println(m_matcher.group(ii) );
	    	}

		} catch (Exception e) {
			System.out.print("[ParserAccessImpl] Error getFileContent :");
			System.out.println(szfile);
		}
		finally {
			try {
				if (in != null) in.close();
			} catch (Exception e) {}
		}
		
		return m_nlogtype;
	}

	public void run () {
		
		String szfile;
		String szoutfile;
		int ntargetfile = 0;

		// ?닿굅??listner ?먯꽌 ?꾩껜 directory scan ?⑹쑝濡??ㅽ뻾?쒕떎.
		Vector<String>vFileL;

		vFileL = new Vector<String> ();
	
		try {

			m_logger.info( "Parser Target Scan : " + m_szinfile );
			Util.getFileListCore ( vFileL, true, m_szinfile, ".gz" );

			ntargetfile = vFileL.size();
			for ( int i = 0; i < ntargetfile; i++ ) {
				szfile = vFileL.get(i);
				m_logger.info( "Parser Target File : " + szfile );
				
				if ( szfile.indexOf( m_szrundate ) < 0 ) continue;
				szoutfile = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile) + ".txt";

				m_szoutfile = szoutfile;
				m_logger.info( "Parser Out File : " + szoutfile );
				run_core ( szfile, szoutfile);
			}

			// m_logger.info( "Parser Target Scan : " + ntargetfile + " - " + m_szinfile + ", " + m_szfilter);
		}
		catch ( Exception e ) {
			
		}

	}

	public void write_core ( String [] arrlog) {
		
		String szwritefile, sztmp;
		String szwdate;
		String szwhh;
		String szwmi;
		String szwc;	
		int j;
		BufferedWriter bw;;
		
		szwritefile = m_szoutfile;
		
		sztmp = szwritefile;

	/*	try {
			m_bw.write ( FileUtil.getbasename( szwritefile));
			m_bw.write( "$^");
		}
		catch ( Exception e ) {
		}
*/
		for ( j = 0; j < arrlog.length; j++) {
			try {
				
				if ( j > 0 ) m_bw.write( "\t");
				if ( arrlog[j] != null ) {
					m_bw.write( arrlog[j].replace(',', ' ') );
				}
				
			}
			catch ( Exception e ) {
			}		
		}
		try {
			m_bw.write( '\n');
		}
		catch ( Exception e) {
			
		}
	}
	
	public boolean run_CallBack (String szline, Hashtable<String, Object> hparam ) {
	
		boolean bisonlyerror = false;
		boolean bisonlycontent = false;
		
		boolean bret = false;
		String targetline;
		Matcher m = null;
		int j= 1;
		Pattern c_pattern, c_patternFinal;
		String [] saz_dest = new String [20];
		
		
		bret = get_log( saz_dest, szline );
		
		if ( ! bret ) return false;
		
		int nhttp_stat = eBrotherUtil.getIntNumber(saz_dest[7]);
		m_logger.trace( "line : " + nhttp_stat + " --> " + szline );
		
		if ( bisonlyerror && (nhttp_stat < 400 || nhttp_stat >= 600 )) {	
			return true;
		}

		write_core ( saz_dest );
		
		return bret;
	}


	public void run_core  ( String sztargetfile, String szoutfile ) {
		
		String szkey;
		BufferedWriter bw;
		String sztype;
		File file = null;
		BufferedReader reader = null;
		boolean biswn = false;
		String [] qqq = new String [20];
		m_curdate = UtilExt.getY2K_Date();
		
		int ncnt = 0;
	    try {
										
			int nreadline = 0;
			file = new File(sztargetfile);
			
			m_bw = UtilExt.getFileWriter(szoutfile);

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(sztargetfile), "KSC5601"));
			
			String line = "";
			
			while(true) {

				try {
					line = reader.readLine();				
					if(line == null) break;
					nreadline++;
					run_CallBack ( line, null );

				}
				catch (Exception e_file) {
					
				}
			}

			run_CallBack ( "LAST LINE", null );
			
			m_bw.close();
			m_bw = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				if(reader != null) {reader.close();}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
			
			try {
				if( m_bw != null) { m_bw.close();}
			} catch(Exception e1) {
				e1.printStackTrace();
			}
		}
	    
	    m_logger.debug("Worker thread end : " + sztargetfile );
	}
	
	
	public static void main(String[] args)  {
		
		String szfile;
		
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

		// szindir = "D:\\eBrotherProject\\rnd\\project\\axa\\inbound_4";
		// szoutdir = "D:\\eBrotherProject\\rnd\\project\\axa\\outbound";

		szindir = "D:/eBrotherProject/rnd/project/hanabank/logs";
		szoutdir = "D:\\eBrotherProject\\rnd\\project\\hanabank";
		
		
		szindir = "D:/eBrotherProject/rnd/project/kcisa/inbound";
		szoutdir = "D:/eBrotherProject/rnd/project/kcisa/outbound";
		
		String szrundate = "20140701";

		if ( args.length >= 4 ) {
			
			szrundate = args[0];
			szindir = args[1];
			szoutdir = args[2];
		}
		else {
			// args = new String [] { szpattern_server, szpatterhn, szindir, szoutdir, szfilter };
			args = new String [] { szrundate, szindir, szoutdir, "" };
		}

		AccessLogParser oParser = new AccessLogParser  ( null, args);
		
		String szbuf = "";

		oParser.set_debug(true);
		oParser.set_logmeta(3);
		oParser.run ();
	}
	
}
