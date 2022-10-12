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
		
		// 실수로 동일한 column 위치에 다른 의미를 넣었다.
		// 따라서, client 일때는 로그 전송 TYPE 과 token 을 넣고 
		// 서버일때는 기존의 방식데로 한다.
		// 로그의 pattern 을 넣기 위함이다.
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

		// 실시간 전송 여부 설정.
		// 실시간 일경우, tail 개념이다. batch 일 경우 : 파일내 시간 형식을 보고 시간 형식이 넘으면 한번에 전송한다.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 7 );
		if ( szdata.equals("Y")) {
			fp.issendreal = true;
			m_hasreal = true;
		}
		else fp.issendreal = false;

		// 파일 이름 마지막에 instance 이름이 들어가곤 한다.
		// 경우에 따라 다르다.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 8 ).trim();
		fp.sz2ndtoken = szdata;		
		
		// 뒤부분 역시, time 형식을 설정한다.
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
		
		// 첫번째 file 이름 token 과 2번째 서버 이름 token ( 시간 형식 가능) 을 같이 넣는다.
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

		// 파일 이름에 들어가는 중간에 구분자다.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 2 );
		fp.sep = szdata;
		
		// 파일 이름에 시간이 들어가는 column 을 정의한다.
		// 파일 이름  access.log 이런 형식 또는, access.log_2013123109 이런 형식이다.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 3 );
		fp.idx = eBrotherUtil.getIntNumber( szdata );
		
		// 시간 column 이 종료되는 부분의  token 이다.
		// access_2013123109.log 의 형식을 처리할 때 필요하다. 
		szdata = eBrotherUtil.getDelimitData(szline, ",", 4 );
		fp.tail = szdata;
		
		// 마지막 token 이 없으면, 그냥 무시하기 위해 설정한다.
		if ( fp.tail.length() > 0 ) fp.istail = true;
		
		// 시간 column 이 가지는 일자 형식이다.
		// 이런 거는 자동화해 주어야 하는데 ... 좀 고민이다.
		// format 이 없다는 거는, 파일 이름에 일자가 들어가지 않는 다는 것을 의미한다.
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

		// eBrother Log 기본 format 에 의하면, Full Date 가 들어 간다.
		// 그렇게 하려면, 잘라내야 하는 size 를 알아야 한다.
		szdata = eBrotherUtil.getDelimitData(szline, ",", 6 );
		fp.cutsize = UtilExt.getNumber(szdata);
		
		mlog.debug( "" + fp.cutsize + " ->" + szline);

		///////////////////////////////////////////////////////////////////
		
		// 항상  동일하다. 걍 FIX 한다.
		// 통상 .. yyyyMMdd 를 가진다. 일자를 지정하기 위함이다.
		fp.oformat = "yyyyMMdd";
		if ( fp.oformat.length() > 0 ) {
			fp.odateformat = new SimpleDateFormat ( fp.oformat, Locale.ENGLISH);
		}
		else {
			fp.odateformat = null;
		}

		// 전송 후 삭제 여부
		// 
		szdata = eBrotherUtil.getDelimitData(szline, ",", 9 );
		if ( szdata.equals("Y")) fp.isdelete = true;
		else fp.isdelete = false;		
		
		m_hpattern.put( fp.key, fp );
		
	}

	/*
	 * 파일 내에서 최종 Date 부분을 추출합니다.
	 * 물론, 파일 이름은 동일한 Pattern 이어야 합니다.
	 */
	public FilePattern getPattern ( String szfile ) {
		
		FilePattern fp;
		if ( (fp=isTarget ( szfile)) == null) return null;
		return fp;		
	}	
	
	/*
	 * 파일 내에서 최종 Date 부분을 추출합니다.
	 * 물론, 파일 이름은 동일한 Pattern 이어야 합니다.
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
			// ebrother file 형식. 년월일시분초_ ... .
			// 이런 경우는  앞에 이미 일자가 있기 때문에, check 할 대상 일자를 결정하면 된다.
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
						// 2nd token 이 있을 경우는 한번 더 check
						return fp;
					} else if ( fp.sz2ndtoken == null || fp.sz2ndtoken.length() == 0 ) {
						// 2nd token 이 없을경우 무시
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
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^201[2-9][0-2][0-9][0-5][0-9](.*?)_(.*?).log$"; // ^시작,$끝
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
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^access.log_[0-1][0-9][0-3][0-9]201[2-9]$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		prtData(m);
		bret = m.matches ();
		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		
		source = "error.log_01312012"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^error.log_[0-1][0-9][0-3][0-9]201[2-9]$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		prtData(m);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		
		source = "JeusServer_20121013.log"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);

		source = "JeusServer_20121013.log.gz"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);

		source = "JeusServer_20121313.log.gz"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9].log$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);
		
		source = "JeusServer_20120931_01.log"; 
		//String pattern = "^[0-9][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]$"; // ^시작,$끝 
		pattern = "^JeusServer_201[2-9][0-1][0-9][0-3][0-9]_[0-2][0-9].log$"; // ^시작,$끝
		p = Pattern.compile( pattern );
		m = p.matcher( source);
		bret = m.matches ();		
		System.out.println ( "result : " + bret + " ==> \n pattern -> " + pattern + "\n" + "data -> " + source + "\n"  );
		prtData(m);
		
		*/
	}
	
}
