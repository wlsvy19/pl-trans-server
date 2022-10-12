package com.eBrother.util;

import com.eBrother.app.main.FilePattern;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class eBrotherUtil  extends Object   implements Serializable, eBrotherConstant {


	private static Logger mlog = Logger.getLogger(eBrotherUtil.class.getName());
	
	static eBrotherUtil myself = null;
	
	public static eBrotherIni  ebIni = new eBrotherIni ();
	static String strCharSet = "";
	static String strUniCode = "";
	static String strIsMethod = "";
	static String strDateType = "";
	static String strusefilesep = "";
	static boolean bIniOpen = false;

	static boolean m_boswin = false;
	static {
		if ( System.getProperty("os.name").toLowerCase().indexOf("window") >= 0 ) m_boswin= true;
		// System.out.println ( System.getProperty("os.name"));
	}
	
	
	public static boolean is_windows () {
		return m_boswin;
	}	

	static public String getYMD4Access (String strDate){

		String strYear="", strMon="", strDay="", strHour="", strMin="", strSec="", strTmp="";
		//setString("1",2,'0') => "01"
		if( strDate == null ) return "";
		strDay = setString( getDelimitData(strDate,"/",1).trim(), 2, '0');
		strMon = getDelimitData(strDate,"/",2).trim();
		strTmp = getDelimitData(strDate,"/",3).trim();
		strYear = setString( getDelimitData(strTmp,":",1).trim(), 4, '0');

		if( strMon.equalsIgnoreCase("Jan") ) strMon = "01";
		else if( strMon.equalsIgnoreCase("Feb") ) strMon = "02";
		else if( strMon.equalsIgnoreCase("Mar") ) strMon = "03";
		else if( strMon.equalsIgnoreCase("Apr") ) strMon = "04";
		else if( strMon.equalsIgnoreCase("May") ) strMon = "05";
		else if( strMon.equalsIgnoreCase("Jun") ) strMon = "06";
		else if( strMon.equalsIgnoreCase("Jul") ) strMon = "07";
		else if( strMon.equalsIgnoreCase("Aug") ) strMon = "08";
		else if( strMon.equalsIgnoreCase("Sep") ) strMon = "09";
		else if( strMon.equalsIgnoreCase("Oct") ) strMon = "10";
		else if( strMon.equalsIgnoreCase("Nov") ) strMon = "11";
		else if( strMon.equalsIgnoreCase("Dec") ) strMon = "12";

		return strYear + strMon + strDay;

	}    

	public String getDateFromAccessDate(String strDate){

		String strYear="", strMon="", strDay="", strHour="", strMin="", strSec="", strTmp="";
		//setString("1",2,'0') => "01"
		if( strDate == null ) return "";
		strDay = setString( getDelimitData(strDate,"/",1).trim(), 2, '0');
		strMon = getDelimitData(strDate,"/",2).trim();
		strTmp = getDelimitData(strDate,"/",3).trim();
		strYear = setString( getDelimitData(strTmp,":",1).trim(), 4, '0');
		strHour = setString( getDelimitData(strTmp,":",2).trim(), 2, '0');
		strMin = setString( getDelimitData(strTmp,":",3).trim(), 2, '0');
		strSec = setString( getDelimitData(strTmp,":",4).trim(), 2, '0');

		if( strMon.equalsIgnoreCase("Jan") ) strMon = "01";
		else if( strMon.equalsIgnoreCase("Feb") ) strMon = "02";
		else if( strMon.equalsIgnoreCase("Mar") ) strMon = "03";
		else if( strMon.equalsIgnoreCase("Apr") ) strMon = "04";
		else if( strMon.equalsIgnoreCase("May") ) strMon = "05";
		else if( strMon.equalsIgnoreCase("Jun") ) strMon = "06";
		else if( strMon.equalsIgnoreCase("Jul") ) strMon = "07";
		else if( strMon.equalsIgnoreCase("Aug") ) strMon = "08";
		else if( strMon.equalsIgnoreCase("Sep") ) strMon = "09";
		else if( strMon.equalsIgnoreCase("Oct") ) strMon = "10";
		else if( strMon.equalsIgnoreCase("Nov") ) strMon = "11";
		else if( strMon.equalsIgnoreCase("Dec") ) strMon = "12";

		return strYear + strMon + strDay + strHour + strMin + strSec;

	}    
	
	public static eBrotherUtil getInstance () {
		
		if ( myself == null ) {
			
			myself = new eBrotherUtil ();
		}	
		return myself;
	}

	public static boolean run_cmd ( boolean bdebug_cmd, boolean bdebug_out, String sz_cmd, String [] sz_arrenv ) {
		
		BufferedReader buf = null;

		mlog.debug ( sz_cmd );

		try {
			
			Runtime run = Runtime.getRuntime();
			Process pr = null;
			
			if ( sz_arrenv == null ) { 
				pr = run.exec(sz_cmd);
			}
			else {
				// pr = run.exec(sz_cmd, sz_arrenv);
				pr = run.exec(sz_cmd);
			}

			// pr.waitFor();
			buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				mlog.debug (line);
			}
			mlog.debug ("FINISH");
		}
		catch ( Exception e ) {
			
			mlog.error ( e );
			try {
				if ( buf != null ) buf.close();
			}
			catch ( Exception e2) {
				mlog.error ( e2 );
			}
			return false;
		}
		finally {
			try {
				if ( buf != null ) buf.close();
			}
			catch ( Exception e2) {
				mlog.error ( e2 );
			}
		}
		return true;
	}
	
	public static boolean run_cmd ( boolean bdebug_cmd, boolean bdebug_out, String sz_cmd ) {
		
		boolean bret = false;
		
		bret = run_cmd ( bdebug_cmd, bdebug_out, sz_cmd, null );
		return true;
	}

	
	public static String runFileCallBack(String szfile, ICallBack icall) {
		StringBuffer sb_return = new StringBuffer();
		int i = 0;

		if ( icall != null ) icall.run_Pre();
		try {
			
			BufferedReader in = new BufferedReader(new FileReader(szfile));
			String sLine;
			try {
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					i++;
					if (sLine.length() == 0)
						continue;

					sb_return.append(sLine);
					sb_return.append('\n');

					icall.run_CallBack( sLine );
				}
			} catch (Exception e) {} finally {
				try {
					if (in != null)
						in.close();
				} catch (Exception e) {}
			}
		} catch (Exception e) {
			System.out.print("[AdUtil.runFileCallBackExt] not found : ");
			System.out.println(szfile);
		}

		if ( icall != null ) icall.run_Post ();
		return sb_return.toString();
	}

	public static String getbase_name ( String sztarget ) {
		
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
		
		return szfile;
	}
	
	public static boolean getDirListCore ( Vector<String> v_filelist, int nmax_depth, int ncur_depth
			, boolean binc_child, String directory, IFilter c_filter ) {
	
		File dir = new File(directory);
		File[] fileList = dir.listFiles();
		String sztemp;
		
		if ( fileList == null ) return false;
		int len = fileList.length;
		
		if ( v_filelist == null ) return false;
	
		if ( nmax_depth < ncur_depth ) return true;
		
		for ( File f_cur : fileList ) {
			sztemp = f_cur.toString();
			if ( f_cur.isDirectory() ) {
				v_filelist.add( sztemp );
			}
		}
		return true;
	}
	
	public static boolean getFileList( Vector<String> v_filelist, int nmax_depth, int ncur_depth
					, boolean binc_child, boolean binc_pname, String directory
					, IFilter c_filter ) {
		
		Vector<String> v_dup;
		if ( v_filelist == null ) return false;
		
		if ( nmax_depth < ncur_depth ) return true;
		v_dup = new Vector<String> ();
		getFileListCore ( v_dup, nmax_depth, ncur_depth, binc_child, directory, c_filter );
		
		for ( String szfile : v_dup ) {
			if (  binc_pname ) v_filelist.add( szfile );
			else v_filelist.add ( new String (szfile.replace( directory, "" )));
		}
		v_dup.clear();
		v_dup = null;
		return true;
	}
	
	public static boolean getFileListCore ( Vector<String> v_filelist, int nmax_depth, int ncur_depth
				, boolean binc_child, String directory, IFilter c_filter ) {
		
		File dir = new File(directory);
		File[] fileList = dir.listFiles();
		String sztemp;
		
		if ( fileList == null ) return false;
		int len = fileList.length;
		
		if ( v_filelist == null ) return false;

		if ( nmax_depth < ncur_depth ) return true;
		
		for ( File f_cur : fileList ) {
			sztemp = f_cur.toString();
			if ( f_cur.isDirectory() ) {
				if ( binc_child && nmax_depth > ncur_depth) getFileListCore( v_filelist, nmax_depth, ncur_depth + 1, binc_child, sztemp, c_filter );
			} else {
				if ( c_filter == null || c_filter.is_logfile(sztemp)) v_filelist.add( sztemp );
			}
		}
		return true;
	}
	
	private void openIniFile(){
		if( !bIniOpen ){
			ebIni.open ( ebIni.getPath() );
			strCharSet = ebIni.getString ( "encoding", "charset", "" );
			strUniCode = ebIni.getString ( "encoding", "unicode", "" );
			strIsMethod = ebIni.getString( "encoding", "ismethod", "1" );
			strDateType = ebIni.getString( "source_htdocs", "datetype", "-" );
			strusefilesep = ebIni.getString ( "source_htdocs", "usefilesep", "1" );
		}			
	}
	/* CGI �� ��� string �� �ΰ��� ������ function */
	/* ������ ? �� �������� ������� ������, �׷��� ���� ���ϴ� ��쵵 ���� */
	/* ��, seperator �� ; �Ǵ� .cgi ������ ������ �� */
	/* ����� hard coring �� �ϴ� ������ ������, ���߿��� ���� ó���� �ؾ� �� ������ �Ǵܵ� */
	public static String[] getCgiStr(String str, String sep) {
		int i = 0 , j = 0;
		String str1[] = new String [2];
		try {
			i = str.indexOf(sep);
			j = sep.length();
			if (i  > 0 ) {
				str1[0] = str.substring(0,i+j);
				if ( str.length() > i+j )
					str1[1] = str.substring(i+j+1,str.length());
				else
					str1[1] = "";
			}
		} //try
		catch(Exception e) {
			System.out.println("[getCgiStr] ���� �߻� " + e);
			str1[0] = "";
			str1[1] = "";
		}
		return	str1;
	}	
	
	/* ���� : �ڷᰡ ������ Ȯ���ؼ�, ""�� Return �� */
	/* �Է� : strIn */
	/* ��� : "" �Ǵ� �� ���ڿ� */
	static public  String getZeroStr(String strln) {
		
		if ((strln == null) || (strln.equals(" ")))
		{
		 strln = "";
		}
		return strln.trim ();
	}
	
/// shin dong hoon start ///
/*
	���� : ���ڿ� �ΰ��� �޾Ƽ� ���� ������ " selected" ��ȯ
	�ٸ��� "" ��ȯ
	�Է� : op1, op2 -> ��ȯ��
	��� : ���ڿ�
*/
	public  String getNumSelected(int op1, int op2) {
		String str = new String("");

		if (op1==op2 )		str = str+ " selected ";
		return str;
	}
		
	/*
	���� : ���ڿ� �ΰ��� �޾Ƽ� ���� ������ " checked " ��ȯ
	�ٸ��� "" ��ȯ
	�Է� : op1, op2 -> ��ȯ��
	��� : ���ڿ�
*/
	public  String getNumChecked(int op1, int op2) {
		String str = new String("");
	
		/* Null : -1 */
	
		if ( op1==op2 )
			str = str+ " checked ";
		return str;
	}	
	
/* ����� ��ȯ */
	public  String getStrDate3( String strDate) {
		String str = new String(strDate);
		if (str==null|| str.equals("")|| str.equals(" ")){
			str = "������� ����ϴ�";
		return str;
		}
		else{
		str = strDate.substring(0,4) + "��" + strDate.substring(4,6) + "��" + strDate.substring(6,8)+ "��";
		return str;
		}
	}

	public  String getStrDateEng( String strDate) {
		String str = new String(strDate);
		if (str==null|| str.equals("")|| str.equals(" ")){
			str = "no registration date";
			return str;
		}
		else{
			str = strDate.substring(4,6) + "/" + strDate.substring(6,8) + "/" + strDate.substring(0,4);  //�̱���
			return str;
		}
	}
/*
	���� : strln�� �޾Ƽ� byte ó�� �Ͽ� �ѱ� ��ȯ�Ѵ�.
	��) getHan(Ư������) => �ѱ� ��ȯ
	�Է� : strln->���ڿ� �ѱ� ��ȯ
	��� : ���ڿ�
*/
	
	public  String getZeroHan(String strln)  throws UnsupportedEncodingException  {
		
		String str = new String(" ");
		
		openIniFile();
		
		if ((strln == null) || (strln.equals(" "))||(strln.equals(""))||(strln.equals("  ")))
		{
			return	"";
		}	
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		str = strln;
		try {
			if( !strCharSet.equals("") && !strUniCode.equals("") ){
				return str = new String(strln.getBytes(strUniCode),strCharSet);
			}else{
				return ( str );
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}
	
	
	public  String getZeroHan(String strln, String strMethod)  throws UnsupportedEncodingException  {
		
		String str = new String(" ");
		
		openIniFile();
		
		if ((strln == null) || (strln.equals(" "))||(strln.equals(""))||(strln.equals("  ")))
		{
			return	"";
		}	
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		if( strMethod == null ) strMethod = "";
		
		str = strln;
		
		try {
			if( (!strCharSet.equals("") && !strUniCode.equals("") 
				&& strMethod.equalsIgnoreCase("post")) || !strIsMethod.equals("1") ){
				return str = new String(strln.getBytes(strUniCode),strCharSet);
			}else{
				return ( str );
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}
/////*  shin dong hoon end */////////

	/*
	���� : ���ڿ����� space�� ����
	�Է� : strln -> ó����
	��� : ���ڿ�
	*/
	public  String getRemoveSpace(String strln) throws java.lang.ArrayIndexOutOfBoundsException  {

		int i;
		String str = new String("");
		String str1 = new String("");
		
		if ((strln == null) || (strln.equals("")))
		{
			 str1 = "";
			 return str1;
		}
		i = 0;
		try {
			while( i < strln.length())
			{
				str = strln.substring(i,i+1);
				if (!str.equals(" ") )
				{
					str1 = str1 + str;
				}	
				i++ ;
			}; 		
			return str1;
		}
		catch(Exception e)
		{
		 	// System.out.println("���� :" +e.toString());
			str1 = "";
			return str1;
		}
	}

/*
	����: ���ڿ����� Ư������(strsep)�� �������� intseq��° �ִ� ���ڿ� ��ȯ
	��) getDelimitData("abc&&def&&ghi&&jkl","&&",1) =>"abc" ��ȯ
	��� : ���ڿ�
*/
    // for rule server
	public String getDelimitData3(String strln, String strSep, int intSeq) throws java.util.NoSuchElementException {
		int i = 0;
		String str1 = new String("");
		String strtemp = "", strchar1 = "", strchar2 = "";
		
		if ((strln == null) || (strln.equals("")))
		{
			 str1 = "";
			 return str1;
		}
		
		// "##"�� "# #"�� replace
		for( int j=0; j<strln.length(); j++){
			 strchar2 = strln.substring(j,j+1);
			 if( strchar2.equals(strchar1) && strchar2.equals(strSep) ) 
			 	strtemp = strtemp + " " + strchar2;
			 else
			 	strtemp = strtemp + strchar2;
			 strchar1 = strchar2;
		}
		
		// token �и�
		//StringTokenizer token = new StringTokenizer(strtemp,strSep);

		//while(token.hasMoreTokens())
		// count �����̱� ������ token ������ ã�����ϴ� ��ȣ�� ���
        
		//i = token.countTokens() - (intSeq-1);			
        strtemp = strln;
        int iPos;
        try {
            while (i < intSeq) {
                iPos = strtemp.indexOf(strSep);
                if (iPos >= 0) { str1 = strtemp.substring(0,iPos); strtemp = strtemp.substring(iPos+2); }
                else str1 = strtemp;
                i++;
            }
            return str1;
        }
		catch(Exception e)		
		{
		 	// System.out.println("���� :" +e.toString());
			str1 = "";
			return str1;
		}
	}
	
    // for wepa
	public static String getDelimitData(String strln, String strSep, int intSeq) {
		
		int intSep=0, intIdx = 0;
		String strtemp = "", strRet = "";
		
		if (strln == null || strln.equals("")) return "";
		if (strSep == null || strSep.equals("")) return "";
		
		try{
			strtemp = strln;
			while( (intIdx=strtemp.indexOf(strSep,0)) >= 0 ){
				intSep++;
				if( intSep == intSeq ){
					strRet = strtemp.substring(0,intIdx);
					return strRet;
				}
				strtemp = strtemp.substring(intIdx+strSep.length(),strtemp.length());
			}
			intSep++;
		
			if( intSep == intSeq ) return strtemp;
			if( intSep < intSeq ) return "";
			
		} catch (Exception e) { 
			System.out.println("getDelimitData Error : " + e);  
		}
		return "";
	}

	public String getDelimitData(String strln, char cSep, int intSeq) throws java.util.NoSuchElementException {
		
		int i = 0;
		String str1 = new String("");
		String strtemp = "", strchar1 = "", strchar2 = "";
		// note. 
		// String	strSep = Integer.toString ( cSep );
		String	strSep = 	EB_REC_HASH_DELIMITER2;
		if (strln == null || strln.equals("")) return "";
		if (strSep == null || strSep.equals("")) return "";
		
		// "##"�� "# #"�� replace
		for( int j=0; j<strln.length(); j++){
			 
			if( (j+strSep.length()) <= strln.length() ) strchar2 = strln.substring(j,j+strSep.length());
			else strchar2 = strln.substring(j,j+1);
			
			if( strchar2.equals(strSep) ){ 
				j = j + strSep.length() - 1;
			}else{ 
				strchar2 = strln.substring(j,j+1);
			}
			
			if( strchar2.equals(strchar1) && strchar2.equals(strSep) ) 
			 	strtemp = strtemp + " " + strchar2;
			else
			 	strtemp = strtemp + strchar2;
			
			 strchar1 = strchar2;
	
		}
		
		// token �и�
		StringTokenizer token = new StringTokenizer(strtemp,strSep);
		
		//while(token.hasMoreTokens())
		// count �����̱� ������ token ������ ã�����ϴ� ��ȣ�� ���
        
		i = token.countTokens() - (intSeq-1);			
		try {
			while(i <= token.countTokens())	{
				str1 = token.nextToken();
			}
			return str1;
		}
		catch(Exception e)		
		{
		 	// System.out.println("���� :" +e.toString());
			str1 = "";
			return str1;
		}
	}
    
/*
	by skkim 20030408
    ���� : ���ڿ����� Ư�����ڿ�(strsep)�� �������� ���ڿ��� �� ��ȯ
	��) getDelimitData2("abc</a>def</a>ghi</a>jkl","</a>", 2) => def ��ȯ
        getDelimitData2("</a>def</a>ghi</a>jkl","</a>", 1)    => ""  ��ȯ
        getDelimitData2("abc</a>def</a>ghi</a>jkl","###", 1)  => ""  ��ȯ
	��� : String
*/
    public String getDelimitData2(String szSrc, String szSep, int nSeq) throws java.util.NoSuchElementException {
		
        int nSeqLength = 0;
        String szTmp = "";
        
        nSeqLength = szSep.length();
        
        //if ( nSeq <= 1 ) return szSrc;
        if ( szSrc.indexOf(szSep) < 0 ) return "";
        
        szTmp = szSrc;
        for ( int k = 1 ; k < nSeq ; k++ ) {
            szTmp = szTmp.substring(szTmp.indexOf(szSep) + nSeqLength);
        }

        // szSeq �� ó�� ���� ���� ��.. ex getDelimitData2( "</a>www", "</a>", 1 );
        if ( szTmp.indexOf(szSep) == 0 && nSeq == 1)
            szTmp = "";
        else if ( szTmp.indexOf(szSep) > 0 )
            szTmp = szTmp.substring(0, szTmp.indexOf(szSep));
        
        return szTmp;
	}
    
    
/*
	���� : ���ڿ����� Ư������(strsep)�� �������� ���ڿ��� �� ��ȯ
	��) getDelimitNum("abc&&def&&ghi&&jkl","&&") => 4 ��ȯ
	��� : ����
*/
	public  int getDelimitNum(String strln, String strSep) {
		int intSep=0, intIdx=0;
		String strtemp = "";
		
		if (strln == null || strln.equals("")) return 0;
		if (strSep == null || strSep.equals("")) return 0;
		
		try{
			strtemp = strln;
			while( (intIdx=strtemp.indexOf(strSep,0)) >= 0 ){
				intSep++;
				strtemp = strtemp.substring(intIdx+strSep.length(),strtemp.length());
			}
			intSep++;
			return intSep;
		} catch (Exception e) {
			System.out.println("getDelimitNum Error : " + e);
			return 0;
		}
	}
/*
	���� : ���ڿ� �ΰ��� �޾Ƽ� ���� ������ <option value= 'test' seleted> ��ȯ
	�ٸ��� <option value= 'test' > ��ȯ
	�Է� : op1, op2 -> �񱳰�
	��� : ���ڿ�
*/
			
	public  String getPageOption(String op1, String op2) {
		String str = new String("<option value='"+op1+"'" );
		String st1 = new String("");
		String st2 = new String("");
		if ((op1.trim().length() <= 0) || (op2.trim().length() <= 0))  
		{
			str = "";
			return str;
		}
		st1 = op1.trim();
		st2 = op2.trim();
		
		if (st1.equals(st2) )
			str = str+ " selected >";
		else
			str = str + " '>";
		return str;
	}
/*
	���� : ���ڿ� �ΰ��� �޾Ƽ� ���� ������ " selected" ��ȯ
	�ٸ��� "" ��ȯ
	�Է� : op1, op2 -> ��ȯ��
	��� : ���ڿ�
*/
	public  String getSelected(String op1, String op2) {
		String str = new String("");
		String st1 = new String("");
		String st2 = new String("");
		
		if ((op1.trim().length() <= 0) || (op2.trim().length() <= 0))  {
			str = "";
			return str;
		}
		
		st1 = op1.trim();
		st2 = op2.trim();
		
		if (st1.equals(st2) )		str = str+ " selected ";
		return str;
	}
	
/**
	���� : ���ڿ� �ΰ��� �޾Ƽ� ���� ������ " checked " ��ȯ
	�ٸ��� "" ��ȯ
	�Է� : op1, op2 -> ��ȯ��
	��� : ���ڿ�
*/
	public  String getChecked(String op1, String op2) {
		String str = new String("");
		String st1 = new String("");
		String st2 = new String("");
		
		/* Null : -1 */
		if ((op1.trim().length() <= 0) || (op2.trim().length() <= 0))  
		{
			str = "";
			return str;
		}
		st1 = op1.trim();
		st2 = op2.trim();
		
		if (st1.equals(st2) )
			str = str+ " checked ";
		return str;
	}
/*
	���� : ���ڿ� �ΰ� strop1, strop2�� �޾Ƽ� strop1�� strop2�� ���ԵǾ����� "checked" ��ȯ
	(checkbox ���� �̿�)
	�Է� : strop1, strop2 -> �񱳰�
	��� : ���ڿ�
*/
	public  String getChecked_checkbox(String strop1, String strop2) {
		int intval;
		String str = new String("");
		String st1 = new String("");
		String st2 = new String("");
		if ((strop1.trim().length() <= 0) || (strop2.trim().length() <= 0))  
		{
			str = "";
			return str;
		}
		st1 = strop1.trim();
		st2 = strop2.trim();
		intval = st1.indexOf(st2); 
		if (intval >= 0) 
			str = " checked ";
		else
			str = "";
		return str;
	}
/*
	���� : ���ڿ� �ΰ� strop1, strop2�� �޾Ƽ� strop1�� strop2�� ���ԵǾ����� "checked" ��ȯ
	(checkbox ���� �̿�)
	�Է� : strop1, strop2 -> �񱳰�
	��� : ���ڿ�
*/
	public  String getChecked_select(String strop1, String strop2) {
		int intval;
		String str = new String("");
		String st1 = new String("");
		String st2 = new String("");
		if ((strop1.trim().length() <= 0) || (strop2.trim().length() <= 0))  
		{
			str = "";
			return str;
		}
		st1 = strop1.trim();
		st2 = strop2.trim();
		intval = st1.indexOf(st2); 
		if (intval >= 0) 
			str = " selected ";
		else
			str = "";
		return str;
	}
/*
	���� : strln�� �޾Ƽ� ���̰� intlength�� ���ġ��� ������ chrln���ڷ� ���̸� ���߾� ��ȯ
	��) setString("1",2,'0') => "01" ��ȯ
	�Է� : strln->���ڿ� intLength->���� chrln -> ����
	��� : ���ڿ�
*/
	public static String setString(String strln, int intlength, char chrln) {
		
		if( strln == null ) return "";
		if( strln.equals("") ) return "";
		
		String str = new String(strln);
		int i;
		if (intlength > strln.trim().length())
		{
			str = "";
			i = intlength - strln.length();
			for(int j=1;j<=i; j++) str = str + chrln;
			str = str + strln;
		}
		return str;
	}
/*
	���� : strln�� �޾Ƽ� ���̰� intlength�� ���ġ��� ������ chrln���ڷ� ���̸� ���߾� ��ȯ
	��) setString("1",2,'0') => "01" ��ȯ
	�Է� : strln->���ڿ� intLength->���� chrln -> ����
	��� : ���ڿ�
*/
	
	public  String setStrafter(String strln, int intlength, char chrln) {
		
		if( strln == null ) return "";
		if( strln.equals("") ) return "";
		
		String str = new String(strln);
		int i;
		if (intlength > strln.trim().length())
		{
			str = "";
			i = intlength - strln.length();
			str = str + strln;
			for(int j=1;j<=i; j++) str = str + chrln;
		}else if (intlength < strln.trim().length())
		{
			str = strln.substring(0,intlength);
		}
		return str;
	}

/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrMDHM("200004190720") => 2000/04/19 07:20 AM 
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrMDHM( String strDate) {
		int ihour ;
		String str = new String("");
		String imunite = new String("");
		String status = new String("AM");
		
		ihour = Integer.parseInt(strDate.substring(8,10));
		imunite = strDate.substring(10,12);
		if (imunite.trim().length() <= 0) imunite = "00";
		
		if (ihour > 12)
		{
			status = "PM";
			ihour = ihour - 12;
		}
		
		str = strDate.substring(0,4) + "/" + strDate.substring(4,6) + "/" + strDate.substring(6,8) + " " + 
			  setString(String.valueOf(ihour),2,'0') + ":" + setString(imunite,2,'0') + " " + status ;     
		return str;
	}	
/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrMDHM2("200004191520") => 04��19��03:20 PM
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrMDHM2( String strDate) {
		int ihour ;
		String str = new String("");
		String imunite = new String("");
		String status = new String("AM");
		
		ihour = Integer.parseInt(strDate.substring(8,10));
		imunite = strDate.substring(10,12);
		if (imunite.trim().length() <= 0) imunite = "00";
		
		if (ihour > 12)
		{
			status = "PM";
			ihour = ihour - 12;
		}
		
		str = strDate.substring(4,6) + "��" + strDate.substring(6,8) + "��" + 
			  setString(String.valueOf(ihour),2,'0') + ":" + setString(imunite,2,'0') + " " + status ;     
		return str;
	}	
/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrMDHM3("200004191520") => 04��19��03:20
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrMDHM3( String strDate) {
		int ihour ;
		String str = new String("");
		String imunite = new String("");
		
		ihour = Integer.parseInt(strDate.substring(8,10));
		imunite = strDate.substring(10,12);
		if (imunite.trim().length() <= 0) imunite = "00";
		
		str = strDate.substring(4,6) + "��" + strDate.substring(6,8) + "��" + 
			  setString(String.valueOf(ihour),2,'0') + ":" + setString(imunite,2,'0') ;     
		return str;
	}	
/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrDate("200004191520") => 2000/04/19
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrDate( String strDate ) {
		String str = strDate;
		
		openIniFile();
		
		if( strDate == null ) return str;
		
		if( strDate.length() < 6 ) return str;
		if( strDateType.equalsIgnoreCase("E") ){
			String strYear = "";
			String strMonth = "";
			String strDay = "";
			
			if( strDate.length() >= 8 ){
				strYear = strDate.substring(0,4);
				strMonth = strDate.substring(4,6);
				strDay = strDate.substring(6,8);
			
				if( strMonth.equals("01") ) strMonth = "Jan";
				else if( strMonth.equals("02") ) strMonth = "Feb";
				else if( strMonth.equals("03") ) strMonth = "Mar";
				else if( strMonth.equals("04") ) strMonth = "Apr";
				else if( strMonth.equals("05") ) strMonth = "May";
				else if( strMonth.equals("06") ) strMonth = "Jun";
				else if( strMonth.equals("07") ) strMonth = "Jul";
				else if( strMonth.equals("08") ) strMonth = "Aug";
				else if( strMonth.equals("09") ) strMonth = "Sep";
				else if( strMonth.equals("10") ) strMonth = "Oct";
				else if( strMonth.equals("11") ) strMonth = "Nov";
				else if( strMonth.equals("12") ) strMonth = "Dec";
				
				str = strMonth + " " + strDay + ", " + strYear;
			}else{
				str = strDate.substring(0,4) + "." + strDate.substring(4,6);
			}
			
		}else{
		
			if( strDate.length() == 6 )
				str = strDate.substring(0,4) + strDateType + strDate.substring(4,6);
			if( strDate.length() >= 8 )
				str = strDate.substring(0,4) + strDateType + strDate.substring(4,6) + strDateType + strDate.substring(6,8);
		}
		return str;
	}	
	
	
	public  String getStrDate( String strDate, String strPType ) {
		String str = strDate;
		String strType = strPType;
		
		
		if( strDate == null ) return str;
		
		if( strDate.length() < 6 ) return str;
		if( strType.equalsIgnoreCase("E") ){
			String strYear = "";
			String strMonth = "";
			String strDay = "";
			
			if( strDate.length() >= 8 ){
				strYear = strDate.substring(0,4);
				strMonth = strDate.substring(4,6);
				strDay = strDate.substring(6,8);
			
				if( strMonth.equals("01") ) strMonth = "Jan";
				else if( strMonth.equals("02") ) strMonth = "Feb";
				else if( strMonth.equals("03") ) strMonth = "Mar";
				else if( strMonth.equals("04") ) strMonth = "Apr";
				else if( strMonth.equals("05") ) strMonth = "May";
				else if( strMonth.equals("06") ) strMonth = "Jun";
				else if( strMonth.equals("07") ) strMonth = "Jul";
				else if( strMonth.equals("08") ) strMonth = "Aug";
				else if( strMonth.equals("09") ) strMonth = "Sep";
				else if( strMonth.equals("10") ) strMonth = "Oct";
				else if( strMonth.equals("11") ) strMonth = "Nov";
				else if( strMonth.equals("12") ) strMonth = "Dec";
				
				str = strMonth + " " + strDay + ", " + strYear;
			}else{
				str = strDate.substring(0,4) + "." + strDate.substring(4,6);
			}
			
		}else{
		
			if( strDate.length() == 6 )
				str = strDate.substring(0,4) + strType + strDate.substring(4,6);
			if( strDate.length() >= 8 )
				str = strDate.substring(0,4) + strType + strDate.substring(4,6) + strType + strDate.substring(6,8);
		}
		return str;
	}	
	
	public String getParseStr(String str, String sep) {
		String strs = "";
		int nCnt = 0;
		if( str == null || sep == null ) return "";
		
		nCnt = getDelimitNum(str, sep);
		if( nCnt <= 0 ) return "";
		
		for( int i=1; i<=nCnt; i++)
			strs += getDelimitData(str, sep, i);
		return strs;
	}
	
/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrDate2("200004191520") => 2000��04��19
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrDate2( String strDate) {
		String str = new String(strDate);
		str = strDate.substring(0,4) + "��" + strDate.substring(4,6) + "��" + strDate.substring(6,8);
		return str;
	}	
/*
	���� : ���ڿ��� �޾� ��¥������� ��ȯ
	��) getStrDateMD("200004191520") => 04/19
	�Է� : strDate -> ���ڿ�
	��� : ���ڿ�
*/
	public  String getStrDateMD( String strDate) {
		String str = new String(strDate);
		str = strDate.substring(4,6) + "/" + strDate.substring(6,8);
		return str;
	}	
/*
	���� : ���� ��¥ ��ȯ ��) "000419"
	�Է� : N/A
	��� : ���ڿ�
*/
	public  String getCurDate() {
		String str = new String("");
		int iyear, imonth, iday;
		iyear = 0;
		imonth = 0;
		iday = 0;
		Calendar today =  Calendar.getInstance(); 
		iyear =  today.get(Calendar.YEAR); 
		imonth = today.get(Calendar.MONTH) + 1;
		iday = today.get(Calendar.DAY_OF_MONTH); 
		str = String.valueOf(iyear).substring(2,4) + setString(String.valueOf(imonth),2,'0') + 
			  setString(String.valueOf(iday),2,'0') ;
		return str;
	}
/*
	���� : ���� ��¥ ��ȯ ��) "20000419"
	�Է� : N/A
	��� : ���ڿ�
*/
	public static String getY2KCurDate() {
		String str = new String("");
		int iyear, imonth, iday;
		iyear = 0;
		imonth = 0;
		iday = 0;
		Calendar today =  Calendar.getInstance(); 
		iyear =  today.get(Calendar.YEAR); 
		imonth = today.get(Calendar.MONTH) + 1;
		iday = today.get(Calendar.DAY_OF_MONTH); 
		str = String.valueOf(iyear) + setString(String.valueOf(imonth),2,'0') + 
			  setString(String.valueOf(iday),2,'0') ;
		return str;
	}
/*
	���� : ���� ��¥ ��ȯ ��) "1858"
	�Է� : N/A
	��� : ���ڿ�
*/
	public static  String getStrCurTime() {
		String str = new String("");
		int ihour,iminute ;
		Calendar today =  Calendar.getInstance(); 

		ihour =  today.get(Calendar.HOUR_OF_DAY); 
		iminute = today.get(Calendar.MINUTE);
		str = setString(String.valueOf(ihour),2,'0') + setString(String.valueOf(iminute),2,'0') ;
			  
		return str;
	}
/*
	���� : ���� ��¥ ��ȯ ��) "07:58pm"
	�Է� : N/A
	��� : ���ڿ�
*/
	public static String getCurTime() {
		String str = new String("");
		int ihour,iminute,iampm;
		String sampm;
		Calendar today =  Calendar.getInstance(); 

		ihour =  today.get(Calendar.HOUR); 
		iminute = today.get(Calendar.MINUTE);
		iampm = today.get(Calendar.AM_PM);
		if (iampm == 0)
			sampm = "am";
		else
			sampm = "pm";
		str = setString(String.valueOf(ihour),2,'0') + ":"+setString(String.valueOf(iminute),2,'0') +sampm ;
		return str;
	}
/*
	���� : ���ڸ� �޾Ƽ� "1"�̸� "O"�� ��ȯ �ƴϸ� "X"��ȯ
	�Է� : chrln -> ����
	��� :   
*/
	public  char getOXchr(char chrln) {
		char chr;
		if (chrln == '1')
			chr = 'O';
		else
			chr = 'X';
		return chr;
	}

/*
	���� : ���ڸ� �޾Ƽ� "1"�̸� "O"�� ��ȯ "0"��  "X"��ȯ �ƴϸ� ��� ��ȯ
	�Է� : chrln -> ����
	��� :   
*/
	public  char getOXint(int intln) {
		char chr;
		switch (intln)
		{
			case 1 :
				chr = 'O';
				break;
			case 0 :
				chr = 'X';
				break;
			default :
				chr = ' ';
		}
		return chr;
	}
/*
	���� : �� ��¥ ���ڿ��� �޾Ƽ� �γ� ������ �Ⱓ�� ��ȯ ���� �ð��� �� ��� "_" ��ȯ
	��) getDateDiff("200003201104", "200003211104" ) => 1�� 24:0"
	�Է� : istart -> ��¥���ڿ� , iend -> ��¥���ڿ�
	��� : ���ڿ�
*/
	public  String getDateDiff (String istart, String iend) {
		int isyear, ismonth, isday,ishour, isminute;
		int ieyear, iemonth, ieday,iehour, ieminute;
		int iyear, imonth , iday, ihour, iminute, itemp, itemp1, iyear1 ;
		long lstart, lend ;
		String str = new String("");
		
		itemp = 0;
		itemp1 = 0;
		lstart = (new Long(istart)).longValue();
		lend = (new Long(iend)).longValue();
		
		isyear = Integer.parseInt(istart.substring(0,4));
		ismonth = Integer.parseInt(istart.substring(4,6)) - 1;
		isday = Integer.parseInt(istart.substring(6,8));
		ishour = Integer.parseInt(istart.substring(8,10)) ;
		isminute = Integer.parseInt(istart.substring(10,12)) ;

		ieyear = Integer.parseInt(iend.substring(0,4));
		iemonth = Integer.parseInt(iend.substring(4,6)) - 1;
		ieday = Integer.parseInt(iend.substring(6,8));
		iehour = Integer.parseInt(iend.substring(8,10)) ;
		ieminute = Integer.parseInt(iend.substring(10,12)) ;
		
		Calendar start1 =  Calendar.getInstance(); 
		Calendar end1 = Calendar.getInstance();
		
		start1.set(isyear,ismonth, isday, ishour,isminute);
		end1.set(ieyear, iemonth, ieday, iehour,ieminute);
		if (lstart > lend) 
		{
			str = "_";
			return str;
		}	
		
		
		iyear = end1.get(end1.YEAR) - start1.get(start1.YEAR); 
		if (iyear > 0) 
		{
			iyear1 = isyear;
			for( int j = 1; j <= iyear; j++)
			{
				Calendar year1 = Calendar.getInstance();
				year1.set(iyear1,11,31,23,59);
				itemp = itemp + year1.get(year1.DAY_OF_YEAR); 
				iyear1++ ;
			}
		}	
		
		imonth = end1.get(end1.MONTH) - start1.get(start1.MONTH);  
		
		if (imonth > 0 )
		{	
			itemp1 = end1.get(end1.DAY_OF_YEAR) - start1.get(start1.DAY_OF_YEAR);  
			iday = itemp + itemp1;
		}
		else
		{
			itemp1 = end1.get(end1.DAY_OF_YEAR) - start1.get(start1.DAY_OF_YEAR);  
			iday = itemp + itemp1;
		}
		
		if (end1.get(end1.HOUR_OF_DAY) < start1.get(start1.HOUR_OF_DAY))
		{
			ihour = (24 +  end1.get(end1.HOUR_OF_DAY)) - start1.get(start1.HOUR_OF_DAY);
		}
		else
		{	
			ihour = end1.get(end1.HOUR_OF_DAY) - start1.get(start1.HOUR_OF_DAY);
		}
		
		int itminute = end1.get(end1.MINUTE) ;
		if (end1.get(end1.MINUTE) < start1.get(start1.MINUTE)) 
		{
			itminute = itminute + 60;
			
			if (ihour == 0 )
			{
				ihour = 23;
				iday = iday - 1;
			}
			else
			{
				ihour = ihour - 1;
			}
		}
		iminute = itminute  - start1.get(start1.MINUTE);
		str = iday + "�� " + ihour + ":" + iminute;
		return str;
	}

/*
	���� : �� ��¥ ���ڿ��� �޾Ƽ� �γ� ������ �Ⱓ�� ��ȯ ���� �ð��� �� ��� "_" ��ȯ
	��) getDayDiff("200003201104", "200003211104" ) => 13
	�Է� : istart -> ��¥���ڿ� , iend -> ��¥���ڿ�
	��� : ���ڿ�
*/
	public  int getDayDiff (String istart, String iend) {
		int isyear, ismonth, isday,ishour, isminute;
		int ieyear, iemonth, ieday,iehour, ieminute;
		int iyear, imonth , iday, ihour, iminute, itemp, itemp1, iyear1 ;
		long lstart, lend ;
		String str = new String("");
		
		itemp = 0;
		itemp1 = 0;
		lstart = (new Long(istart)).longValue();
		lend = (new Long(iend)).longValue();
		
		isyear = Integer.parseInt(istart.substring(0,4));
		ismonth = Integer.parseInt(istart.substring(4,6)) - 1;
		isday = Integer.parseInt(istart.substring(6,8));
		ishour = Integer.parseInt(istart.substring(8,10)) ;
		isminute = Integer.parseInt(istart.substring(10,12)) ;

		ieyear = Integer.parseInt(iend.substring(0,4));
		iemonth = Integer.parseInt(iend.substring(4,6)) - 1;
		ieday = Integer.parseInt(iend.substring(6,8));
		iehour = Integer.parseInt(iend.substring(8,10)) ;
		ieminute = Integer.parseInt(iend.substring(10,12)) ;
		
		Calendar start1 =  Calendar.getInstance(); 
		Calendar end1 = Calendar.getInstance();
		
		start1.set(isyear,ismonth, isday, ishour,isminute);
		end1.set(ieyear, iemonth, ieday, iehour,ieminute);
		if (lstart > lend) 
		{
			str = "_";
			return -1;
		}	
		
		
		iyear = end1.get(end1.YEAR) - start1.get(start1.YEAR); 
		if (iyear > 0) 
		{
			iyear1 = isyear;
			for( int j = 1; j <= iyear; j++)
			{
				Calendar year1 = Calendar.getInstance();
				year1.set(iyear1,11,31,23,59);
				itemp = itemp + year1.get(year1.DAY_OF_YEAR); 
				iyear1++ ;
			}
		}	
		
		imonth = end1.get(end1.MONTH) - start1.get(start1.MONTH);  
		
		if (imonth > 0 )
		{	
			itemp1 = end1.get(end1.DAY_OF_YEAR) - start1.get(start1.DAY_OF_YEAR);  
			iday = itemp + itemp1;
		}
		else
		{
			itemp1 = end1.get(end1.DAY_OF_YEAR) - start1.get(start1.DAY_OF_YEAR);  
			iday = itemp + itemp1;
		}
		
		if (end1.get(end1.HOUR_OF_DAY) < start1.get(start1.HOUR_OF_DAY))
		{
			ihour = (24 +  end1.get(end1.HOUR_OF_DAY)) - start1.get(start1.HOUR_OF_DAY);
		}
		else
		{	
			ihour = end1.get(end1.HOUR_OF_DAY) - start1.get(start1.HOUR_OF_DAY);
		}
		
		int itminute = end1.get(end1.MINUTE) ;
		if (end1.get(end1.MINUTE) < start1.get(start1.MINUTE)) 
		{
			itminute = itminute + 60;
			
			if (ihour == 0 )
			{
				ihour = 23;
				iday = iday - 1;
			}
			else
			{
				ihour = ihour - 1;
			}
		}
		iminute = itminute  - start1.get(start1.MINUTE);
		//str = iday + "�� " + ihour + ":" + iminute;
		
		return iday;
	}
	
/*
	���� : �ΰ��� ��¥�� �޾Ƶ鿩 ���Ͽ� ó���� ��¥�� ũ�� -���� �ٿ��� ���� �ƴϸ� ����ǰ��� ���
	��) getDateDiffDay("200003211104", "200003201104" ) => -1�� 24:0" 
	�Է� : istart -> ��¥���ڿ� , iend -> ��¥���ڿ�
	��� : ���ڿ�
*/
	public  String getDateDiffDay (String istart, String iend) {
		String str = new String("");
		String str1 = new String("");
		long lstart, lend ;
		
		lstart = (new Long(istart)).longValue();
		lend = (new Long(iend)).longValue();
		
		if (lstart > lend) {
			str1 =	String.valueOf(getDayDiff(iend,istart));
			str = str1;
		}
		else {
			str1 =	String.valueOf(getDayDiff(istart,iend));
			str = "-"+str1;
		}
		
		return str;
	}
	
	
/*
	���� : ��¥���ڿ��� ����(iintdate)�� �޾Ƽ� ���� ��¥�� iintdate��ŭ ���� ��¥�� ��ȯ
	��) getDateAdd("200002201110",3) => "200002230000"
	�Է� : istart-> ��¥���ڿ�, iintdate->����
	��� : ���ڿ�
*/
	public  String getDateAdd (String istrdate, int iintdate ) {
		int isyear, ismonth, isday,ishour, isminute;
		String str = new String("");
		isyear = Integer.parseInt(istrdate.substring(0,4));
		ismonth = Integer.parseInt(istrdate.substring(4,6)) - 1;
		isday = Integer.parseInt(istrdate.substring(6,8));
		ishour = Integer.parseInt(istrdate.substring(8,10)) ;
		isminute = Integer.parseInt(istrdate.substring(10,12)) ;

		if (istrdate.equals(" ") || (istrdate == null))
		{
			str = "";
			return str;
		}
		Calendar start1 =  Calendar.getInstance(); 
		start1.set(isyear,ismonth, isday, ishour,isminute);
		start1.add(start1.DATE , iintdate);
		str = String.valueOf(start1.get(start1.YEAR)) +							//�⵵
			  setString(String.valueOf(start1.get(start1.MONTH) + 1),2,'0') +	//��
			  setString(String.valueOf(start1.get(start1.DATE)),2,'0') + "0000";	//�Ͻð�
		return str;
	}
/*
	���� : ���� ��¥ ��ȯ ��) "200004191203"
	�Է� : N/A
	��� : ���ڿ�
*/

	public static String getY2K_CurDate() {
		String str = new String("");
		Calendar currdate = Calendar.getInstance();
		str = String.valueOf(currdate.get(currdate.YEAR)) +
			  setString(String.valueOf(currdate.get(currdate.MONTH) + 1),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.DATE)),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.HOUR_OF_DAY)),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.MINUTE )),2,'0');
		return str;
	}

	public static String getY2K_CurFullDate( int DIFFTYPE, int ndiff ) {

		Calendar currdate = Calendar.getInstance(Locale.KOREA);

		currdate.add ( DIFFTYPE, ndiff);
		
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        Date currentTime1 = new Date();
        String dTime = formatter.format(currentTime1);

		return dTime;
	}
	
	public static String getY2K_CurFullDate() {
		String str = new String("");
		
		TimeZone qqq;
		
		String strDate;
		Calendar currdate = Calendar.getInstance(Locale.KOREA);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
        Date currentTime1 = new Date();
        String dTime = formatter.format(currentTime1);

        /*
		//SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		//String strDate = sdfDate.format(currdate);
		
		System.out.println( dTime );
		str = String.valueOf(currdate.get(currdate.YEAR)) +
			  setString(String.valueOf(currdate.get(Calendar.MONTH) + 1),2,'0') +
			  setString(String.valueOf(currdate.get(Calendar.DATE)),2,'0') +
			  setString(String.valueOf(currdate.get(Calendar.HOUR_OF_DAY)),2,'0') +
			  setString(String.valueOf(currdate.get(Calendar.MINUTE )),2,'0') +
              setString(String.valueOf(currdate.get(Calendar.SECOND )),2,'0');
		*/
		return dTime;
	}
	
	public  String getY2K_CurFullDateMilli() {
		String str = new String("");
		Calendar currdate = Calendar.getInstance();
		str = String.valueOf(currdate.get(currdate.YEAR)) +
			  setString(String.valueOf(currdate.get(currdate.MONTH) + 1),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.DATE)),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.HOUR_OF_DAY)),2,'0') +
			  setString(String.valueOf(currdate.get(currdate.MINUTE )),2,'0') +
              setString(String.valueOf(currdate.get(currdate.SECOND )),2,'0') + 
			  setString(String.valueOf(currdate.get(currdate.MILLISECOND )),3,'0');
		return str;
	}
	/*
	���� : ��¥�� �޾Ƽ� �׳��� ������� ����(���糯¥ - �񱳳�¥)�� 2�� �̳��� 1�� ��ȯ
	�Է� : istrdate -> ��¥���ڿ�
	��� : ����
*/
	
	public  String getIsNewDate(String istrdate) throws NumberFormatException {
		int iresult;
		String str = new String("");
		String today = new String("");
		if (istrdate.equals(" ") || (istrdate == null))
		{
			str = "";
			return str;
		}
		today = getY2K_CurDate();
		try 
		{
			iresult = Integer.parseInt(getDelimitData(getDateDiff(istrdate,today),"��",1));
			if ((iresult >= 0) && (iresult <=2))
				str = "1";
			else
				str = "0";
			return str;
		}
		catch(Exception e)		
		{
		 	System.out.println("���� :" +e.toString());
			str = "0";
			return str;
		}
		
	}
	
/*
	���� : strln�� �޾Ƽ� byte ó�� �Ͽ� �ѱ� ��ȯ�Ѵ�.
	��) getHan(Ư������) => �ѱ� ��ȯ
	�Է� : strln->���ڿ� �ѱ� ��ȯ
	��� : ���ڿ�
*/
	
	public  String getHan(String strln)  throws UnsupportedEncodingException  {
		
		String str = new String("");
		
		openIniFile();
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		str = strln.trim();
		try {
			if( !strCharSet.equals("") && !strUniCode.equals("") ){
				return str = new String(strln.getBytes(strUniCode),strCharSet);
			}else{
				return ( str );
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}
	
	public  String getHan(String strln, String strMethod)  throws UnsupportedEncodingException  {
		
		String str = new String("");
		
		openIniFile();
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		if( strMethod == null ) strMethod = "";
		str = strln.trim();
		try {
			if( (!strCharSet.equals("") && !strUniCode.equals("") 
				&& strMethod.equalsIgnoreCase("post")) || !strIsMethod.equals("1") ){
				return str = new String(strln.getBytes(strUniCode),strCharSet);
			}else{
				return ( str );
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}

/*
	���� : strln�� �޾Ƽ� byte ó�� �Ͽ� ���� ��ȯ�Ѵ�.
	��) �ѱ� --> ����.
	�Է� : strln->���ڿ� �ѱ� ��ȯ
	��� : ���ڿ�
*/
	
	public  String getEng(String strln)  throws UnsupportedEncodingException  {
		
		String str = new String("");
		
		openIniFile();
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		str = strln.trim();
		try {
			if( !strCharSet.equals("") && !strUniCode.equals("") ){
				return str = new String(strln.getBytes(strCharSet),strUniCode);
			}else{
				return str;
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}
	
	public  String getEng(String strln, String strMethod)  throws UnsupportedEncodingException  {
		
		String str = new String("");
		
		openIniFile();
		
		if ((strln == null) || (strln.trim().length() <= 0)  ) {
			return "";
		}
		if( strMethod == null ) strMethod = "";
		str = strln.trim();
		try {
			if( !strCharSet.equals("") && !strUniCode.equals("") && strMethod.equalsIgnoreCase("get") ){
				return str = new String(strln.getBytes(strCharSet),strUniCode);
			}else{
				return str;
			}
		}
		catch(Exception e)
		{
			System.out.println("���� :" +e.toString());
			return str;
		}
	}
	
/*
	���� : ���� ��ȯ
	�Է� : str -> ó���� ("123,456") -> 123456
	��� : ���� (LONG)
*/
	public static long getNumber(String str) throws StringIndexOutOfBoundsException  {
	
		int i, iddd;
		long longln = 0L;
		char ch;
		String qq = new String("");

		if ((str == null) || (str == "") || str.length () == 0  ) return ( 0) ;
		if (str.substring(0,1) == "-") 
		{	
			i = 1;
			iddd = -1;
		}
		else
		{
			i = 0;
			iddd = 1;
		}
		try 
		{
			while(i < str.length())
			{
				ch = str.charAt(i);
				if ((!Character.isDigit(ch)) && (ch != ',')) 
				{
					longln = 0L;
					return longln;
				}
				if (ch != ',') qq = qq + ch;
				i++ ;
			}
		
			if (!qq.equals("") || qq != null) 
				longln = Long.parseLong(qq) * iddd;		
			else
				longln = 0L; 
			return longln;
		}
		catch(Exception e)		
		{
		 	System.out.println("���� :" +e.toString());
			longln = 0;
			return longln;
		}
	}

/*
	���� : ���� ��ȯ
	�Է� : str -> ó���� ("123,456") -> 123456
	��� : ���� (LONG)
*/
	static public  int getIntNumber(String str) throws StringIndexOutOfBoundsException  {
	
		int i, iddd;
		int  intln = 0;
		char ch;
		String qq = new String("");

		if ((str == null) || (str == "") || str.length () == 0  ) return ( 0) ;
		if (str.substring(0,1) == "-") 
		{	
			i = 1;
			iddd = -1;
		}
		else
		{
			i = 0;
			iddd = 1;
		}
		try 
		{
			while(i < str.length())
			{
				ch = str.charAt(i);
				if ((!Character.isDigit(ch)) && (ch != ',')) 
				{
					intln = 0;
					return intln;
				}
				if (ch != ',') qq = qq + ch;
				i++ ;
			}
		
			if (!qq.equals("") || qq != null) 
				intln = Integer.parseInt(qq) * iddd;
			else
				intln = 0; 
			return intln;
		}
		catch(Exception e)		
		{
		 	System.out.println("Error:" +e.toString());
			intln = 0;
			return intln;
		}
	}

	public String GetWebHostHeader	( String strTemp, int nGubun  ) {
	
		if ( nGubun == EB_HOSTHEADERTYPE_HOST ) return (  getDelimitData ( strTemp, EB_HOSTHEADERTYPE_DELIMETER, 1 ));
		else if ( nGubun == EB_HOSTHEADERTYPE_PORT ) return (  getDelimitData ( strTemp, EB_HOSTHEADERTYPE_DELIMETER, 2));
		
		return "";
	
	}
	
    public int getDiffSec( String sEndDate )
    {
        int expiry = 0;
        java.util.Date nowDate, exDate;

                exDate = new java.util.Date( Integer.parseInt(sEndDate.substring(0,4)) - 1900,
                            Integer.parseInt(sEndDate.substring(4,6)) - 1,
                            Integer.parseInt(sEndDate.substring(6,8)),
                            Integer.parseInt(sEndDate.substring(8,10)),
                            Integer.parseInt(sEndDate.substring(10,12)),
                            Integer.parseInt(sEndDate.substring(12)) );

        nowDate = new java.util.Date();
        expiry = (int)( (exDate.getTime() - nowDate.getTime())/1000 ) + 1;


        if ( expiry < 0 )
            expiry = 0;
        return expiry;
    }	
	

	public byte getbase64Byte ( byte bsrc ) {
		
		if (bsrc < 26)       bsrc = (byte)(bsrc +'A');
		else if (bsrc < 52)  bsrc = (byte)(bsrc + 'a'- 26);
		else if (bsrc < 62)  bsrc = (byte)(bsrc + '0'- 52);
		else if (bsrc < 63)  bsrc = ( byte ) '+';
		else                      bsrc = ( byte ) '/';
		
		return bsrc;
		
	}
    /**
     * This method encodes the given string using the base64-encoding
     * specified in RFC-1521. It's used for example in the "basic"
     * authorization scheme.
     *
     * @param  str the string
     * @return the base64-encoded string
     */
    public String GetBase64Encode (String str)
    {
		byte btemp;
		int sidx, didx, nsrclen;
		if (str == null)  return  null;

        byte data[] = new byte[str.length()+2];
        // str.getBytes(0, str.length(), data, 0);

		// for ( int i = 0; i < str.length (); i++ ) data [i] = ( byte )str.charAt ( i );
		data = str.getBytes ();
		
        byte dest[] = new byte[( data.length + 3 - data.length/3)*4];
        // 3-byte to 4-byte conversion
		
		nsrclen =data.length;
		// System.out.println ( " Len : " + nsrclen );
		didx = 0; sidx = 0;
		while ( nsrclen > 2 ) {
			
            dest[didx++]   = getbase64Byte ( (byte) ((data[sidx] >>> 2) & 0x003f));
            dest[didx++] = getbase64Byte ((byte) ((data[sidx+1] >>> 4) & 0x000f | ( ( data[sidx] & 0x03 ) << 4)));
            dest[didx++] = getbase64Byte((byte) ((data[sidx+2] >>> 6) & 0x0003 | ((data[sidx+1] & 0x0f)<< 2) ));
            dest[didx++] = getbase64Byte((byte) (data[sidx+2] & 0x3f ));
			nsrclen -= 3;
			sidx += 3;
        }
		
		// System.out.println ( " Len : " + nsrclen + "," + didx + "," + sidx  );

		if ( nsrclen != 0 ) {
			btemp = getbase64Byte ( (byte) ((data[sidx] >>> 2) & 0x003f));
			dest[didx++] = btemp;
			if ( nsrclen > 1) {
				dest[didx++] = getbase64Byte ((byte) ((data[sidx+1] >>> 4) & 0x000f | ( ( data[sidx] & 0x03 ) << 4)));
				dest[didx++] = getbase64Byte((byte) ( ((data[sidx+1] & 0x0f)<< 2) ));
											//	base64_table[(current[1] & 0x0f) << 2];
				dest[didx++] =( byte) '=';
			}
			else {
				dest[didx++] = getbase64Byte ((byte) ( ( ( data[sidx] & 0x03 ) << 4)));
				// dest[didx++] = base64_table[(current[0] & 0x03) << 4];
				dest[didx++] =( byte) '=';
				dest[didx++] = ( byte)'=';
			}
		}
			
		
        // add padding
		/*
        for (int idx = dest.length-1; idx > (str.length()*4)/3; idx--)
            dest[idx] = ( byte ) '=';
		*/
	   int reallength = 0;
		for(int i=0;i<dest.length;i++)	
		{
			reallength = i;
			int nzero = 0;
			if( (char)dest[i] == (char)nzero ) break;
		}
		return new String(dest, 0, reallength );

       //return new String(dest, 0, dest.length );
		
    }


    /**
     * This method decodes the given string using the base64-encoding
     * specified in RFC-1521.
     *
     * @param  str the string
     * @return the base64-decoded string
     */
    public final static String GetBase64Decode  (String str)
    {
        if (str == null)  return  null;

        byte data[] = new byte[str.length()];
        // str.getBytes(0, str.length(), data, 0);
		
		data = str.getBytes ();
		
        int tail = str.length();
        if( tail == 0 ) return "";
        while (data[tail-1] == '=')  tail--;

        byte dest[] = new byte[tail - data.length/4];


        // ascii printable to 0-63 conversion
        for (int idx = 0; idx <data.length; idx++)
        {
            if (data[idx] == '=')      data[idx] = 0;
            else if (data[idx] == '/') data[idx] = 63;
            else if (data[idx] == '+') data[idx] = 62;
            else if (data[idx] >= '0'  &&  data[idx] <= '9')
                data[idx] = (byte)(data[idx] - ('0' - 52));
            else if (data[idx] >= 'a'  &&  data[idx] <= 'z')
                data[idx] = (byte)(data[idx] - ('a' - 26));
            else if (data[idx] >= 'A'  &&  data[idx] <= 'Z')
                data[idx] = (byte)(data[idx] - 'A');
        }

        // 4-byte to 3-byte conversion
        int sidx, didx;
        for (sidx = 0, didx=0; didx < dest.length-2; sidx += 4, didx +=3)
        {
            dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |  ((data[sidx+1] >>> 4) & 0x000f) );
            dest[didx+1] = (byte) ( ((data[sidx+1] << 4) & 255) |   ((data[sidx+2] >>> 2) & 0x003f ) );
            dest[didx+2] = (byte) ( ((data[sidx+2] << 6) & 255) |   (data[sidx+3] & 077) );
        }
        if (didx < dest.length && sidx+1 < data.length )
            dest[didx]   = (byte) ( ((data[sidx] << 2) & 255) |
                            ((data[sidx+1] >>> 4) & 0x000f) );
        if (++didx < dest.length && sidx+2 < data.length )
            dest[didx]   = (byte) ( ((data[sidx+1] << 4) & 255) |
                            ((data[sidx+2] >>> 2) & 0x003f) );

        return new String(dest, 0, dest.length );
    }

	public static String getDelimitDataRight(String strIn, String strSep, int intSeq) {
		if (strIn == null || strIn.length() == 0)
			return "";

		if (strSep == null || strSep.length() == 0)
			return "";

		int intSep = 1, intIdx = 0;
		int intSepLen = strSep.length();
		String strtemp = "";
		int newidx = 0;
		try {
			while ((newidx = strIn.indexOf(strSep, intIdx )) >= 0) {
				intIdx = ++newidx;
				intSep++;
				if (intSep >= intSeq) break;
			}

			if (intSep >= intSeq) {
				strtemp = strIn.substring(newidx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strtemp;
	}
    
	public void	 prtHashData ( Hashtable objHashTmp, String strTitle ) {
		
		String	strHashKey = "";
		String	strHashDat = "";
		
		System.out.println (  " DEBUG HASH -> " + strTitle );
		for (Enumeration ee = objHashTmp.keys() ; ee.hasMoreElements() ; ) {
         
			strHashKey =  (String) ee.nextElement();
			strHashDat		=  objHashTmp.get ( strHashKey).toString ();
			
			System.out.println  ( "[" +  strHashKey + "]= [" + strHashDat + "] " );
			
		}
		System.out.println (   " DEBUG HASH END -> " + strTitle );
		
	}
   
	public  String getWriteLogName ( String strPath, String strHost, int nPort ) {

		// create a GregorianCalendar with the Pacific Daylight time zone
		// and the current date and time
		
		int i_writtenbytes;
		int i_year, i_month, i_day, i_hour;
		String s_filename, s_date;
		File myfile;
		// GregorianCalendar calendar;

		s_date = getY2KCurDate() + getStrCurTime().substring(0,2);
				
		// Now compose the filename
		s_filename = strPath +System.getProperty("file.separator" ) + s_date + "." + strHost + "." + nPort + ".log";

		return s_filename;
	}
	
	public  String getWriteLogName ( String strPath, String strHost, int nPort, int nGubun  ) {

		// create a GregorianCalendar with the Pacific Daylight time zone
		// and the current date and time
		
		int i_writtenbytes;
		int i_year, i_month, i_day, i_hour;
		String s_filename, s_date;
		File myfile;
		// GregorianCalendar calendar;

		s_date = getY2KCurDate();
		switch ( nGubun ) {
		case EB_TIME_FORM_HOUR :
				s_date +=  getStrCurTime().substring(0,2);
				break;
		}
				
		// Now compose the filename
		s_filename = strPath +System.getProperty("file.separator" ) + s_date + "." + strHost + "." + nPort + ".log";

		return s_filename;
	}
	
	//------------- For File manangement ------------------------------------------------------//

	/**
	 * Check  File Exist ? <br>
	 * eBrotherIni has also sample function
	 * So, If there is need to update IsFileExist, please update eBrotherIni too.
	 * @param String	FileName
	 * @return boolean
	 */

	static public boolean IsFileExist ( String strFileName ) {

		File		objFileTemp;
		boolean bRet = false;
		
		try {
			objFileTemp = new File(  strFileName  );
		
			if ( objFileTemp.exists() ) bRet = true;
		}
		catch ( Exception e ) {}

		objFileTemp = null;

		return bRet;

	}
	
	//2002.12.18 �߰�(���)
	public  String getReplaceStr( String strData ) {
		
		String strRetData = "";
		
		if( strData == null ) return "";
		if( strData.equals("") ) return "";
		
		strRetData = ebIni.getString ( "source_htdocs", strData, "" );
		
		if( strRetData.equals("") ) return strData;
		else return strRetData;
		
	}
	
	//2002.12.18 �߰�(���)
	public  String setStrLength( String strData, int intLength ) {
		
		if( strData == null ) return "";
		if( strData.equals("") ) return "";
		
		if( strData.length() > intLength ) return strData.substring(0,intLength);
		else if( strData.length() <= intLength ) return strData;
		return strData;
	}
	
	public String getDelFileSep(String str, String sep) {
		
		String strRetData = "";
		
		openIniFile();
		
		if( str == null ) strRetData = "";
		else strRetData = str.trim();
		
		if( !strusefilesep.equals("1") ){
			if( strRetData.length() < 1 ){ strRetData = ""; }
			else{
				if( strRetData.substring(0,1).equals(sep) ){
					strRetData = strRetData.substring(1,strRetData.length());
				}
			}
		}
		return strRetData;
	}
	
	//totlasec : �� 
	//pattern : �ú��� ������ ("1" �̸� 01�ð�04��06�� ":" �̸� 01:04:06 "/" �̸� 01/04/06) 
	public String getTimeStrFromSec(int totalseconds, String pattern) 
	{ 

		String timestamp = new String(""); 
		String shours = new String(""); 
		String smin = new String(""); 
		String ssec = new String(""); 

		int hours = totalseconds / 3600; 
		totalseconds = totalseconds % 3600; 
		int min = totalseconds/60; 
		int sec = totalseconds % 60; 

		shours = Integer.toString(hours); 
		smin = Integer.toString(min); 
		ssec = Integer.toString(sec); 

		if (hours < 10) { 
			shours = "0" + shours; 
		} 
		if (min < 10) { 
			smin = "0" + smin; 
		} 
		if (sec < 10) { 
			ssec = "0" + ssec; 
		} 

		if (pattern.equals("1")) { 
			timestamp = shours +"�ð�" + smin +"��" + ssec +"��"; 
		} else { 
			timestamp = shours + pattern + smin + pattern + ssec; 
		} 

		return timestamp; 

	} 
	
	public  String setHangul(String strln, String strtype)  throws UnsupportedEncodingException  {
		
		String str = new String(" ");
		String strEncType = "";
		
		openIniFile();
		strEncType = ebIni.getString ( "encoding", strtype, "" );
		
		strln = getZeroStr(strln).trim();
		if( strln.equals("") ) return "";

		str = strln;
		try {
			
			if( !strCharSet.equals("") && !strUniCode.equals("") ){
				if( strEncType.equals("1") ){
					return str = new String(strln.getBytes(strUniCode),strCharSet);
				}else if( strEncType.equals("-1") ){
					return str = new String(strln.getBytes(strCharSet),strUniCode);
				}else{
					return str;
				}
			}else{
				return str;
			}
		}
		catch(Exception e)
		{
			System.out.println("[ERROR] setHangul :" + e );
			return str;
		}
	}

/*
    ���� : ���ڿ����� Ư�� char ������ ���ڿ� �̳�����
    ��) getMidSring("2000[02]201110",'[', ']') => "02"
    �Է� : strData-> ���ڿ�, strLeft->������ �� ����, strRight->������ �� ����
    ��� : ���ڿ�
*/
	public String getMidString(String strData, String strLeft, String strRight){

		int nIdxLeft=0, nIdxRight=0;

		if( strData == null ) return "";

		nIdxLeft = strData.indexOf(strLeft);
		nIdxRight = strData.indexOf(strRight);

		if( nIdxLeft < 0 || nIdxRight < 0 ) return "";

		return strData.substring(nIdxLeft+1,nIdxRight);

	}

	public String getPassage(String strData, String strLeft, String strRight){

		int nIdxLeft=0, nIdxRight=0;

		if( strData == null ) return "";

		nIdxLeft = strData.indexOf(strLeft);
		nIdxRight = strData.indexOf(strRight);

		if( nIdxLeft < 0 || nIdxRight < 0 ) return "";

		return strData.substring(nIdxLeft+1,nIdxRight);

	}

	// dd/mon/yyyy:hh:mm:ss => yyyymmddhhmmss
	// ��)24/Feb/2003:10:55:12 ---> 20030824105512 ��
	public String getDateString(String strDate){

		String strYear="", strMon="", strDay="", strHour="", strMin="", strSec="", strTmp="";
		//setString("1",2,'0') => "01"
		if( strDate == null ) return "";
		strDay = setString( getDelimitData(strDate,"/",1).trim(), 2, '0');
		strMon = getDelimitData(strDate,"/",2).trim();
		strTmp = getDelimitData(strDate,"/",3).trim();
		strYear = setString( getDelimitData(strTmp,":",1).trim(), 4, '0');
		strHour = setString( getDelimitData(strTmp,":",2).trim(), 2, '0');
		strMin = setString( getDelimitData(strTmp,":",3).trim(), 2, '0');
		strSec = setString( getDelimitData(strTmp,":",4).trim(), 2, '0');

		if( strMon.equalsIgnoreCase("Jan") ) strMon = "01";
		else if( strMon.equalsIgnoreCase("Feb") ) strMon = "02";
		else if( strMon.equalsIgnoreCase("Mar") ) strMon = "03";
		else if( strMon.equalsIgnoreCase("Apr") ) strMon = "04";
		else if( strMon.equalsIgnoreCase("May") ) strMon = "05";
		else if( strMon.equalsIgnoreCase("Jun") ) strMon = "06";
		else if( strMon.equalsIgnoreCase("Jul") ) strMon = "07";
		else if( strMon.equalsIgnoreCase("Aug") ) strMon = "08";
		else if( strMon.equalsIgnoreCase("Sep") ) strMon = "09";
		else if( strMon.equalsIgnoreCase("Oct") ) strMon = "10";
		else if( strMon.equalsIgnoreCase("Nov") ) strMon = "11";
		else if( strMon.equalsIgnoreCase("Dec") ) strMon = "12";

		return strYear + strMon + strDay + strHour + strMin + strSec;

	}
	
	public static void main(String[] args) {
		
		Vector<String> v_filelist = new Vector<String> (); 
		// String szdir = args[0];
		// int ndepth = (int)eBrotherUtil.getNumber( args[1] );
		String szdir = "c://";
		int ndepth = 2;
		
		boolean bret = getFileList( v_filelist, ndepth, 1, true, true, szdir, null );
		
		for ( int j= 0; j < v_filelist.size(); j++ ) {
			System.out.println ( v_filelist.get(j));
		}
	}
	
}