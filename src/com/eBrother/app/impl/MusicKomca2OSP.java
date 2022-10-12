package com.eBrother.app.impl;

import com.eBrother.util.FileUtil;
import com.eBrother.util.Util;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

public class MusicKomca2OSP implements Runnable {

	private String m_PROJECTCD = "";
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	protected static Logger m_logger = Logger.getLogger( MusicKomca2OSP.class.getName());

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

	BufferedWriter m_bw = null;
	
	final static int LOG_COLUMN = 11;

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

	public MusicKomca2OSP ( TransWorker632 ww, String [] args ) {

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
		
		m_logger.info("ParserWorker (master : " + m_bmasterthread + ") : " + szindir );

	}


	// 정말 어려운 case 는 ...
	// transworker 가 실행하다 중지. parserworker 가 따라 죽는데, .run 을 삭제 못하고 죽음.
	// 이 경우, .run 은 처리를 못한다.
	// 다른 어려운 경우 ...
	// 모두다 정상 처리. 그런데, batch 에서 또 읽어서 처리하려고 함. 계속 읽겠지. move 를 해야 하는데 ... 그것을 여기다 넣을까 ? 어렵네...
	// 어디선가는 파일을 옮겨 줘야 한다. !!!
	
	final String EB_KOMCA_CHECK = "splunk_server";
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

		vFileL = new Vector<String> ();
		
		try {	

			m_logger.info( "Parser Target Scan : " + m_szinfile );
			bret = Util.getFileListCore ( vFileL, true, m_szinfile, "" );

			ntargetfile = vFileL.size();
			m_logger.info( "Parser Target Scan : " + ntargetfile + " - " + m_szinfile + ", " + m_szfilter);


			ntargetfile = vFileL.size();
			////////////////////////////////////////////////////////////////////////////////////////////
			
			File file = null;
			BufferedReader reader = null;
			BufferedWriter bw = null;

			for ( int i = 0; i < ntargetfile; i++ ) {
				
				szfile = vFileL.get(i);

				if ( m_szfilter.length() > 0 &&  szfile.indexOf( m_szfilter) < 0 ) { //|| szfile.indexOf(".gz") >= 0 ) {
					m_logger.trace( "Parser Target Skip : " + szfile + " - " + m_szinfile + ", " + m_szfilter);
					continue;
				}
				
				if ( szfile.indexOf(".run") >= 0 ) continue;
				
				szoutfile = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile);
				szouterr = m_szoutdir + FILE_SEPARATOR + FileUtil.getbasename( szfile) + ".err";

				String line;
				int nreadline = 0;
				file = new File(szfile);
				String sztemp;
				int nrawdidx = 0;
				
				m_logger.debug( "Trans KOMCA src file : " + szfile );
				m_logger.debug( "Trans KOMCA dest file : " + szoutfile );
				try {
					
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(szfile), "UTF-8"));
					
					// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
					bw = UtilExt.getFileWriter(szoutfile);
					UtilExt.createFile( szoutfile + ".run");
					
					boolean bquery = false;
					
					while(true) {

						try {

							line = reader.readLine();

							if(line == null) break;
							
							if ( line.indexOf(EB_KOMCA_CHECK) >= 0 ) {
								bquery = true;
								nrawdidx = 4;
								continue;
							}
							
							if ( line.indexOf("\"_raw\"") >= 0 ) {
								
								nrawdidx = 2;
								continue;
							}
							
							sztemp = eBrotherUtil.getDelimitData( line, "\"", 1 );
							
							if ( sztemp.length() > 0 ) continue;
							
							sztemp = eBrotherUtil.getDelimitData( line, "\"", nrawdidx );
							bw.write(sztemp);
							bw.write("\n");
							
						}
						catch (Exception e_file) {
							
						}
					}
				}
				catch ( Exception e_) {
					
					
				}
				
				FileUtil.deleteFile( szoutfile + ".run");
				if ( reader != null ) {
					try { reader.close(); }	catch ( Exception e_cl) { }
				}
				if ( bw != null ) {
					try { bw.close(); }	catch ( Exception e_cl) { }
				}

			}
			
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
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

		szindir = "D:\\eBrotherProject\\rnd\\project\\komc\\inbound";
		
		szoutdir = "D:\\eBrotherProject\\rnd\\project\\osp\\outbound";
		String szfilter = "20150207";

		m_bstandalone = true;

		System.setProperty("SITE", "m1");
		m_bstandalone = true;

		String szbase = "D:\\ebrotherpilms\\parser\\";
		szindir = szbase + "inbound";
		szoutdir = szbase + "outbound";
		
		szfilter = "";
		
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
		MusicKomca2OSP c_p = new MusicKomca2OSP ( null, args);

		c_p.run ();

		m_logger.info("MUSIC LOG : OSP Parser End" );
	}

}

