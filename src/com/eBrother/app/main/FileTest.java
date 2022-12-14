package com.eBrother.app.main;

import com.eBrother.util.ICallBack;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTest implements ICallBack {
	
	private static Logger mlog = Logger.getLogger(FileTest.class.getName());
	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	static eBrotherUtil m_ebutil = eBrotherUtil.getInstance();
	static FileTest m_myself = null; 
	
	Hashtable<String, FilePattern> m_hpattern = new Hashtable<String, FilePattern> (); 

	boolean m_isinit = false;
	
	int m_npatterntype = 0;
	
	boolean m_hasreal = false;
	
	public boolean hasreal () {
		return m_hasreal;
	}
	
	public void set_partterntype ( int npatterntype ) {
		m_npatterntype = npatterntype;
	}
	
	public boolean setPattern ( String szfile ) {
		
		eBrotherUtil.runFileCallBack ( szfile, m_myself );
		return m_isinit;
	}
	
	public boolean setPattern4Text ( String szcontents ) {
		
		
		return m_isinit;
	}
	
	
	public boolean isinit () {
		
		return m_isinit;
	}
	
	  public void run_Pre () {
		  m_isinit = false;
	  }

	  public void run_Post () {
		  m_isinit = true;
	  }
	  
	  /*
	   * (non-Javadoc)
	   * @see com.eBrother.util.ICallBack#run_CallBackFile(java.lang.String, java.lang.String, java.util.Hashtable)
	   */
	  public boolean run_CallBackFile ( String p1, String p2, Hashtable<String, Object> hparam ) {
		  
		  return true;
	  }

	  public boolean run_CallBack (String szlineorg, Hashtable<String, Object> hparam ) {
		  
		  return true;
	  }
	/*
	 * (non-Javadoc)
	 * @see com.eBrother.util.ICallBack#run_CallBack(java.lang.String)
	 * set file format & regex pattern
	 */
	public void run_CallBack ( String szline ) {

		String szdata;
		FilePattern fp;
				
		if ( szline.length() == 0 )return;
		if ( szline.charAt(0) == '#' ) return;
		
		fp = new FilePattern ();
		
		// ????????? ????????? column ????????? ?????? ????????? ?????????.
		// ?????????, client ????????? ?????? ?????? TYPE ??? token ??? ?????? 
		// ??????????????? ????????? ???????????? ??????.
		// ????????? pattern ??? ?????? ????????????.
		// ignore error
		try {
			szdata = eBrotherUtil.getDelimitData(szline, ",", 7 );
			fp.sztimepattern = szdata;
			try {
				Pattern pnew = Pattern.compile( szdata );
				fp.ctimepattern = pnew;
			}
			catch ( Exception e ) {
				fp.ctimepattern = null;
			}		
		}
		catch ( Exception e ) {
			
		}

		// ????????? ?????? ?????? ??????.
		// ????????? ?????????, tail ????????????. batch ??? ?????? : ????????? ?????? ????????? ?????? ?????? ????????? ????????? ????????? ????????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 7 );
		if ( szdata.equals("Y")) {
			fp.issendreal = true;
			m_hasreal = true;
		}
		else fp.issendreal = false;

		// ?????? ?????? ???????????? instance ????????? ???????????? ??????.
		// ????????? ?????? ?????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 8 ).trim();
		fp.sz2ndtoken = szdata;		
		
		// ????????? ??????, time ????????? ????????????.
		//ignore error
		try {
			szdata = eBrotherUtil.getDelimitData(szline, ",", 8 );
			fp.sztimestamp = szdata;
			
			if ( fp.sztimestamp.length() > 0 ) {
				fp.dformattimetstamp = new SimpleDateFormat ( fp.sztimestamp, Locale.ENGLISH);
			}
			else {
				fp.dformattimetstamp = null;
			}
		}
		catch ( Exception e) {
			
		}		
		/////////////////////////////////////////////////////////////////////////////////////
		
		// ????????? file ?????? token ??? 2?????? ?????? ?????? token ( ?????? ?????? ??????) ??? ?????? ?????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 1 );
		fp.key = szdata + "|" + fp.sz2ndtoken;
		fp.szpattern = szdata;

		try {
			Pattern pnew = Pattern.compile( szdata );
			fp.cpattern = pnew;
		}
		catch ( Exception e ) {
			fp.cpattern = null;
		}

		// ?????? ????????? ???????????? ????????? ????????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 2 );
		fp.sep = szdata;
		
		// ?????? ????????? ????????? ???????????? column ??? ????????????.
		// ?????? ??????  access.log ?????? ?????? ??????, access.log_2013123109 ?????? ????????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 3 );
		fp.idx = eBrotherUtil.getIntNumber( szdata );
		
		// ?????? column ??? ???????????? ?????????  token ??????.
		// access_2013123109.log ??? ????????? ????????? ??? ????????????. 
		szdata = eBrotherUtil.getDelimitData(szline, ",", 4 );
		fp.tail = szdata;
		
		// ????????? token ??? ?????????, ?????? ???????????? ?????? ????????????.
		if ( fp.tail.length() > 0 ) fp.istail = true;
		
		// ?????? column ??? ????????? ?????? ????????????.
		// ?????? ?????? ???????????? ????????? ????????? ... ??? ????????????.
		// format ??? ????????? ??????, ?????? ????????? ????????? ???????????? ?????? ?????? ?????? ????????????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 5 );
		fp.iformat = szdata;
		if ( fp.iformat.length() > 0 ) {
			fp.idateformat = new SimpleDateFormat ( fp.iformat, Locale.ENGLISH);
			fp.hasdate = true;
		}
		else {
			fp.idateformat = null;
			fp.hasdate = false;
		}

		// eBrother Log ?????? format ??? ?????????, Full Date ??? ?????? ??????.
		// ????????? ?????????, ???????????? ?????? size ??? ????????? ??????.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 6 );
		fp.cutsize = UtilExt.getNumber(szdata);
		
		mlog.debug( "" + fp.cutsize + " ->" + szline);

		///////////////////////////////////////////////////////////////////
		
		// ??????  ????????????. ??? FIX ??????.
		// ?????? .. yyyyMMdd ??? ?????????. ????????? ???????????? ????????????.
		fp.oformat = "yyyyMMdd";
		if ( fp.oformat.length() > 0 ) {
			fp.odateformat = new SimpleDateFormat ( fp.oformat, Locale.ENGLISH);
		}
		else {
			fp.odateformat = null;
		}

		// ?????? ??? ?????? ??????
		// 
		szdata = eBrotherUtil.getDelimitData(szline, ",", 9 );
		if ( szdata.equals("Y")) fp.isdelete = true;
		else fp.isdelete = false;		
		
		m_hpattern.put( fp.key, fp );
		
	}

	/*
	 * ?????? ????????? ?????? Date ????????? ???????????????.
	 * ??????, ?????? ????????? ????????? Pattern ????????? ?????????.
	 */
	public FilePattern getPattern ( String szfile ) {
		
		FilePattern fp;
		if ( (fp=isTarget ( szfile)) == null) return null;
		return fp;		
	}	
	
	/*
	 * ?????? ????????? ?????? Date ????????? ???????????????.
	 * ??????, ?????? ????????? ????????? Pattern ????????? ?????????.
	 */
	public String getToken ( String szfilenm ) {
		
		String szret = null;
		String szfile;
		FilePattern fp;
		int k, n;
		// SimpleDateFormat fmt1;
		
		// SimpleDateFormat fmt2 = new SimpleDateFormat("HHmmss");
		// SimpleDateFormat fmt3 = new SimpleDateFormat("yyyy/MM/dd");

		String sztemp, sztemp2;
		
		if ( (fp=isTarget ( szfilenm)) == null) return null;
		
		n = szfilenm.lastIndexOf('/');
		if ( n >= 0 ) {
			szfile = new String (szfilenm.substring( n + 1));
		}
		else {
			n = szfilenm.lastIndexOf('\\');
			if ( n >= 0 ) {
				szfile = new String (szfilenm.substring( n + 1));
			}
			else {
				// System.out.println ( "2" );
				szfile = szfilenm;
			}
		}
		
		k = fp.cutsize;
		if ( k > 0 ) {
			// ebrother file ??????. ??????????????????_ ... .
			// ?????? ?????????  ?????? ?????? ????????? ?????? ?????????, check ??? ?????? ????????? ???????????? ??????.
			szret = new String ( szfile.substring( 0, k ));
		}
		else if ( fp.hasdate ) {

			sztemp = eBrotherUtil.getDelimitDataRight ( szfile, fp.sep, fp.idx );
			sztemp2 = sztemp;
			if ( fp.istail) {
				k = sztemp.indexOf(fp.tail);
				if ( k >= 0 ) {
					sztemp2 = new String ( sztemp.substring(0, k));
				}
			}
			try {
				szret = fp.odateformat.format (fp.idateformat.parse( sztemp2 ));
			}
			catch ( Exception e) {
				
			}
		}
		else {
			szret = "";
		}
		return szret;
	}

	  public FilePattern isTarget ( String sztarget ) {
		  
			boolean bret = false;
			Pattern pat;
			String szkey, szdata;
			Matcher m;
			FilePattern fp = null;
			String szfile = "";

			int n;
			n = sztarget.lastIndexOf('/');
			if ( n >= 0 ) {
				szfile = sztarget.substring( n + 1);
			}
			else {
				n = sztarget.lastIndexOf('\\');
				if ( n >= 0 ) {
					szfile = sztarget.substring( n + 1);
				}
				else {
					// System.out.println ( "2" );
					szfile = sztarget;
				}
			}

			// System.out.println ( "token check " + szfile );
			for (Enumeration ee = m_hpattern.keys() ; ee.hasMoreElements() ; ) {
			     
				szkey =  (String) ee.nextElement();
				
				fp	=  m_hpattern.get ( szkey);
				if ( fp.cpattern == null ) continue;
				
				m = fp.cpattern.matcher( szfile);
				bret = m.matches ();
				if ( bret) {

					if ( fp.sz2ndtoken != null && fp.sz2ndtoken.length() > 0 && sztarget.indexOf( fp.sz2ndtoken ) >= 0 ) {
						// 2nd token ??? ?????? ????????? ?????? ??? check
						return fp;
					} else if ( fp.sz2ndtoken == null || fp.sz2ndtoken.length() == 0 ) {
						// 2nd token ??? ???????????? ??????
						return fp;
					}
				}
			}
		
			return null;

	 }
	  
	public static FileTest getInstace () {
		
		if ( m_myself == null ) {
			m_myself = new FileTest ();
		}
		
		return m_myself;
	}

	static public void prtData ( Matcher m ) { 
		int i = 0;
		if ( m == null ) return;
		while(m.find()) {
			System.out.println ( " matcher data [" + i++ + "]=" + m.group(0));
		}
	}
	
	public static void main(String[] args) {
		
		String source;
		String szret;
		// init pattern env
		FileTest c_me = FileTest.getInstace();
		
		String szrun_type = "D:\\workspace_nibbler\\NibblerTrans4\\src\\properties\\transfilepattern.txt";

		szrun_type = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\client\\transfilepattern.txt";
		
		eBrotherUtil.runFileCallBack ( szrun_type, c_me );

		String szregex = "";
		String szdata = "access.2012";
		String pattern;
		Pattern p;
		Matcher m;
		boolean bret;
		// access.log_mmddyyyy,
		//error.log_mmddyyyy.
		
		source = "20130831193000_0_1377944845479.log"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^201[2-9][0-2][0-9][0-5][0-9](.*?)_(.*?).log$"; // ^??????,$???
		// pattern = "^201[2-9][0-1][0-9][0-2][0-9](.*?)000_(.*?).log$";
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		prtData(m);
		bret = m.matches ();
		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		
		// lets try it ...
		source = "D:\\workspace_nibbler\\NibblerTrans4\\dist\\client\\inbound\\20130831193000_0_1377944845479.log";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		// lets try it ...
		source = "/app/oracle/admin/VMIS/bdump/old/alert_VMIS2.log.20120404";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );
		
		// lets try it ...
		source = "/app/oracle/admin/VMIS/bdump/old/access.log_01312012";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		// lets try it ...
		source = "error.log_01312012";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		// lets try it ...
		source = "/app/oracle/admin/bmiswas1/JeusServer_20121013.log";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		// lets try it ...
		source = "JeusServer_20120931_1.log";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		// lets try it ...
		source = "JeusServer_20120931_01.log.gz";
		szret = c_me.getToken( source );		
		System.out.println ( " source : " + source + " ==> token :  " + szret );

		/*
		String szregex = "";
		String szdata = "access.2012";
		String pattern, source;
		Pattern p;
		Matcher m;
		boolean bret;
		// access.log_mmddyyyy,
		//error.log_mmddyyyy.
		
		source = "access.log_01312012"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^access.log_[0-1][0-9][0-3][0-9]201[2-9]$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		prtData(m);
		bret = m.matches ();
		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		
		source = "error.log_01312012"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^error.log_[0-1][0-9][0-3][0-9]201[2-9]$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		prtData(m);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		
		source = "JeusServer_20121013.log"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);

		source = "JeusServer_20121013.log.gz"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);

		source = "JeusServer_20121313.log.gz"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);
		
		source = "JeusServer_20120931_01.log"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^??????,$??? 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9]_[0-2][0-9].log$"; // ^??????,$???
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);
		
		*/
	}
	
}
