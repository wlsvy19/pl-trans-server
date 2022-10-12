package com.eBrother.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Hashtable;


public class FileUtil {
	
	static Logger m_logger = Logger.getLogger( FileUtil.class.getName());
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	public static void scan_dir ( String szdir, String szintoken, String szexcludtoken
				, String szoutdir, ICallBack cb, Hashtable<String, Object> hparam ) {
		
		String szfile;

		// System.out.println("scan dir : step 1 -" + szdir );
		File dir = new File(szdir);

		// System.out.println("scan dir : step 2 -" + szdir );
		File[] fileList = dir.listFiles();
		String sztemp;
		
		szfile = "";
		if ( fileList == null ) {
			//System.out.println("scan dir : isnull. check please - " + szdir );
			return;
		}
		int len = fileList.length;
		
		//System.out.println("scan dir : " + len + ", " + szdir );
		
		for ( File f_cur : fileList ) {
			sztemp = f_cur.toString();
			if ( f_cur.isDirectory() ) {
				scan_dir( sztemp, szintoken, szexcludtoken, szoutdir, cb, hparam );
			} else {

				if ( szintoken.length() > 0 && sztemp.indexOf( szintoken ) < 0 ) {
					continue;
				}

				if ( ( sztemp.indexOf(".gz") >= 0 
						|| sztemp.indexOf("run")>= 0
						|| eBrotherUtil.IsFileExist( sztemp + "." + szexcludtoken )
						|| sztemp.indexOf(szexcludtoken) >= 0
						)) {
					
					//System.out.println("Exclude : " + sztemp );
					//System.out.println("Exclude : File exist - " + eBrotherUtil.IsFileExist( sztemp + "." + szexcludtoken ) );
					continue;
				}
				cb.run_CallBackFile( sztemp, szoutdir, hparam );
			}
		}
	}

	
	public static boolean createFile ( String szfile ) {
		
		boolean bret = false;
		
		File fp = new File ( szfile );
		
		try {
			fp.createNewFile();
			return true;
		}
		catch ( Exception e ) {
			
		}
		return false;
		
	}

	
	public static boolean deleteFile ( String szfile ) {
		
		boolean bret = false;
		
		File fp = new File ( szfile );
		
		try {
			fp.delete();
			return true;
		}
		catch ( Exception e ) {
			
		}
		return false;
		
	}
	
	public static long getFileSize ( String szfile ) {
		
		boolean bret = false;
			
		try {
			File fp = new File ( szfile );
			return fp.length();
		}
		catch ( Exception e ) {
			return -1;
		}
		
	}	
	
	public static boolean createDir ( String szdir ) {
		
	
		boolean bret = false;
		
		File fp = new File ( szdir );
		
		try {
			fp.mkdir();
			return true;
		}
		catch ( Exception e ) {
			
		}
		return false;
		
	}
	
	public static BufferedWriter getFileWriter(String szfile, String szencode) {
		BufferedWriter out = null;

		try {
			
			if ( szencode != null && szencode.length() > 1 ) {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(szfile), "KSC5601"));
			}
			else {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(szfile)));
			}
			// in = new BufferedWriter(new FileWriter(szfile));
		} catch (Exception e) {
			out = null;
		}

		return out;
	}
	
	public static String getFName ( String szurl ) {
		
		String szfile;
		int i;
		
		if ( szurl == null ) return "";
		
		i = szurl.lastIndexOf(FILE_SEPARATOR);
		
		szfile = new String ( szurl.substring(i+1));
		
		return szfile;		
	}
		
	public static int getflines ( String szfile ) {
		
		int nline = 0;
		BufferedReader in = null;
		String content;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(
			       // new GZIPInputStream(
					new FileInputStream( szfile )));
		
			while ((content = in.readLine()) != null) nline++;
	
			in.close ();
			
		}
		catch ( Exception e ) {
			return -1;
		}
		finally {
			try {
				if ( in != null ) in.close();
			}
			catch ( Exception e ) {}
		}

		return nline;
	}

	public static boolean writeOSW ( OutputStreamWriter ows, String szline ) {
		
		try {
			if ( ows == null ) return false;
			ows.write( szline );
			
			return true;
		}
		catch ( Exception  e ) {
			return false;
		}
	}
	
	public static String getbasename ( String fullfile ) {
		
		int n;
		String sztarget = null;
		String szfile;
		n = fullfile.lastIndexOf('/');
		if ( n >= 0 ) {
			sztarget = new String (fullfile.substring( n + 1));
		}
		else sztarget = fullfile;
		
		n = sztarget.lastIndexOf('\\');
		if ( n >= 0 ) {
			szfile = new String (sztarget.substring( n + 1));
		}
		else {
			// System.out.println ( "2" );
			szfile = sztarget;
		}
		return szfile;
	}
		
	
	public static boolean isFileExist(String strFileName) {
		File objFileTemp;
		boolean bRet = false;

		try {
			objFileTemp = new File(strFileName);
			if (objFileTemp.exists())
				bRet = true;
		} catch (Exception e) {}

		objFileTemp = null;

		return bRet;
	}
	
	public static OutputStreamWriter getOSW( String szfile, boolean bappend, String ENCODE ) {
		
		OutputStreamWriter  out  = null;
		try {
			if ( ENCODE == null || ENCODE.length() == 0 ) ENCODE= "KSC5601";
			out  = new OutputStreamWriter (
						// new GZIPOutputStream(
								new FileOutputStream( szfile, bappend ), ENCODE );
		}
		catch ( Exception e) {
			return null;
		}
		return out;
	
	}

	public static BufferedOutputStream getOBW( String szfile, boolean bappend, String ENCODE ) {
		
		BufferedOutputStream  out  = null;
		try {
			if ( ENCODE == null || ENCODE.length() == 0 ) ENCODE= "KSC5601";
			out  = new BufferedOutputStream ( new FileOutputStream( szfile, bappend ) );
		}
		catch ( Exception e) {
			return null;
		}
		return out;
	
	}	
}		
 