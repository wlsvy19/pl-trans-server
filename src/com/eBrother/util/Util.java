package com.eBrother.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

public class Util  implements eBrotherConstant {

	
	// hex to byte[]
	public static byte[] hexToByteArray(String hex) {
	    if (hex == null || hex.length() == 0) {
	        return null;
	    }
	 
	    byte[] ba = new byte[hex.length() / 2];
	    for (int i = 0; i < ba.length; i++) {
	        ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
	    }
	    return ba;
	}
	 
	// byte[] to hex
	public static String byteArrayToHex(byte[] ba) {
	    if (ba == null || ba.length == 0) {
	        return null;
	    }
	 
	    StringBuffer sb = new StringBuffer(ba.length * 2);
	    String hexNumber;
	    for (int x = 0; x < ba.length; x++) {
	        hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
	 
	        sb.append(hexNumber.substring(hexNumber.length() - 2));
	    }
	    return sb.toString();
	} 
	
    public static String getFileContent ( String szfile ) {
        
        String szSQL = "";
	String		sLine;

	Hashtable	mapEntrys = null;
	String		sSection;
	String		sEntry;
	String		sValue;
	int			nIndex;

        try {
            
	   BufferedReader	in = new BufferedReader(new FileReader(szfile));

            try {
                while ((sLine=in.readLine()) != null) {
                    sLine = sLine.trim();
                    if (sLine.length() == 0)	continue;

                    szSQL += sLine + "\n";

                }
            }
            catch ( Exception e ) {

            }
            in.close();
        }
        catch ( Exception e ) {
            szSQL = "";
        }
        return szSQL;
    }

	public static  void	prtHashData ( Hashtable objHashTmp, String strTitle )  {
        
		String	strHashKey = "";
		String	strHashDat = "";
		int i;
		System.out.println (  " DEBUG HASH Start -> " + strTitle );
        i = 0;
		for (Enumeration ee = objHashTmp.keys(); ee.hasMoreElements() ; ) 
        {
			strHashKey =  (String) ee.nextElement();
			strHashDat =  objHashTmp.get ( strHashKey).toString ();
			
			System.out.println  ( i++ + " -> ( " + strHashKey + " ) = [ " + strHashDat + " ] " );
		}
		System.out.println  (  " DEBUG HASH END -> " + strTitle );
	}    
    
    public static BufferedReader  getFileReader ( String szfile ) {
        
		BufferedReader	in = null;
	
        try {
            
	   in = new BufferedReader(new FileReader(szfile));

        }
        catch ( Exception e ) {
           in = null;
        }
        return in;
    }

    public static BufferedWriter  getFileWriter ( String szfile ) {
        
    	BufferedWriter	in = null;
	
        try {
            
	   in = new BufferedWriter(new FileWriter (szfile));

        }
        catch ( Exception e ) {
           in = null;
        }
        return in;
    }

    public static String replace_date (String orig, String to) {
    	
    	String sz_ret;
    	sz_ret  = replace( orig, "[RUN_DATE]", to );
    	try {
    		sz_ret  = replace( sz_ret, "[RUN_YY]", to.substring(0,4));
    	}
    	catch ( Exception e ) {}
    	
    	try {
    		sz_ret  = replace( sz_ret, "[RUN_MM]", to.substring(4,6));
    	}
    	catch ( Exception e ) {}
    	
    	try {
    		sz_ret  = replace( sz_ret, "[RUN_DD]", to.substring(6,8));
    	}
    	catch ( Exception e ) {}

    	try {
    		sz_ret  = replace( sz_ret, "[RUN_YYMM]", to.substring(0,6));
    	}
    	catch ( Exception e ) {}

    	try {
    		sz_ret  = replace( sz_ret, "[RUN_MMDD]", to.substring(4,8));
    	}
    	catch ( Exception e ) {}

    	return sz_ret;
    	
    }

    /*
     * replace data ..
     */
    public static String replace_Args (String orig, String [] sz_args, int n_base ) {
    	
    	String sz_ret;
    	int i, j, k;
    	
    	if ( sz_args == null ) return orig;
    	
    	i = sz_args.length;
    	if ( i < n_base ) return orig;
    	
    	sz_ret = orig;
    	k = 1;
    	for ( j = n_base; j < i; j++ ) {

    		try {
        		sz_ret  = replace( sz_ret, "[CUST_ARG" + k + "]", sz_args[j] );
        	}
        	catch ( Exception e ) {

        	}
    	}

    	return sz_ret;
    	
    }
    
    public static String replace (String orig, String from, String to)
    {
        int fromLength = from.length();
        
        if (fromLength==0)
            throw new IllegalArgumentException 
            ("String to be replaced must not be empty");
        
        int start = orig.indexOf (from);
        if (start==-1)
            return orig;
        
        boolean greaterLength = (to.length() >= fromLength);
        
        StringBuffer buffer;
        // If the "to" parameter is longer than (or
        // as long as) "from", the final length will
        // be at least as large
        if (greaterLength)
        {
            if (from.equals (to))
                return orig;
            buffer = new StringBuffer(orig.length());
        }
        else
        {
            buffer = new StringBuffer();
        }
        
        char [] origChars = orig.toCharArray();
        
        int copyFrom=0;
        while (start != -1)
        {
            buffer.append (origChars, copyFrom, start-copyFrom);
            buffer.append (to);
            copyFrom=start+fromLength;
            start = orig.indexOf (from, copyFrom);
        }
        buffer.append (origChars, copyFrom, origChars.length-copyFrom);
        
        return buffer.toString();
    }

	/**
	 * ������ ���丮�ȿ� �ִ� ���ϵ��� ����Ʈ�� �ۼ��Ͽ� ����
	 * @param directory - Ȩ���丮��
	 * @return ���ϸ���Ʈ ����
	 */
	public static Vector<String> getFileList(String directory) {
		
		File dir = new File(directory);
		File[] fileList = dir.listFiles();

		int len = fileList.length;
		Vector list = new Vector();
		for ( int i = 0; i < len; i++ ) {
			if ( fileList[i].isDirectory() ) {
				list.addAll( getFileList(fileList[i].toString()) );
			} else {
				list.add( fileList[i].toString() );
			}
		}
		return list;
	}
    
	public static boolean getFileList( Vector<String> v_filelist, boolean binc_child, boolean binc_pname, String directory ) {
		
		Vector<String> v_dup;
		if ( v_filelist == null ) return false;
		
		v_dup = new Vector<String> ();
		getFileListCore ( v_dup, binc_child, directory );
		
		for ( String szfile : v_dup ) {
			if (  binc_pname ) v_filelist.add( szfile );
			else v_filelist.add ( new String (szfile.replace( directory, "" )));
		}
		v_dup.clear();
		v_dup = null;
		return true;
	}
	
	public static boolean getFileListCore ( Vector<String> v_filelist, boolean binc_child, String directory ) {
		
		File dir = new File(directory);
		File[] fileList = dir.listFiles();
		String sztemp;
		
		int len = fileList.length;
		
		if ( v_filelist == null ) return false;
		
		for ( File f_cur : fileList ) {
			sztemp = f_cur.toString();
			if ( f_cur.isDirectory() ) {
				if ( binc_child ) getFileListCore( v_filelist, binc_child, sztemp );
			} else {
				v_filelist.add( sztemp );
			}
		}
		return true;
	}

	public static boolean getFileListCore ( Vector<String> v_filelist, boolean binc_child, String directory, String szfilter ) {
		
		String sztemp;
		File dir = new File(directory);
		
		try {
			if ( dir == null ) return false;
			File[] fileList = dir.listFiles();
			
			if ( v_filelist == null ) return false;
			if ( fileList == null ) return false;
			
			
			final String pattern = szfilter;
			final boolean bfilter = ( pattern != null && pattern.length() > 0  ) ? true : false;
			
			int len = fileList.length;
			
			if ( len <= 0 ) return false;
			for ( File f_cur : fileList ) {
				sztemp = f_cur.toString();
				if ( bfilter && sztemp.indexOf(pattern) >= 0 ) continue;
				if ( f_cur.isDirectory() ) {
					if ( binc_child ) getFileListCore( v_filelist, binc_child, sztemp, szfilter );
				} else {
					v_filelist.add( sztemp );
				}
			}
		}
		catch ( Exception e ) {
			
			return false;
		}
		return true;
	}
	
	public static boolean getFileListCoreExt ( Vector<String> v_filelist, boolean binc_child, String directory, String szfilter ) {

		File dir = new File(directory);
		final String pattern = szfilter;
		final boolean bfilter = ( pattern != null && pattern.length() > 0  ) ? true : false;

		String[] filenames = dir.list(
				new FilenameFilter() {
					// @Override
					public boolean accept( File dir, String szfilename) {
						if ( bfilter ) return szfilename.indexOf(pattern) != -1;
						else return true;
					}
				}
		);

		if ( filenames == null ) return false;
		for ( int i = 0; i < filenames.length; i++ ) {
			v_filelist.add( filenames[i]);
		}

		return true;
	}	

	public static boolean getFileContent ( StringBuffer sbout, String filename ) {

		FileInputStream fis;
		InputStreamReader isr;
		BufferedReader br;
		boolean bisgzip = false;

		try {
			
			if ( filename == null ) return false;
			
			fis = new FileInputStream(filename);
			if ( filename.indexOf(".gz") >= 0 ) bisgzip = true;
			
			if ( bisgzip ) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)));
			}
			else {
				br = new BufferedReader( new InputStreamReader(fis ));
			}

			String line;

			while ( (line = br.readLine()) != null ) {
				sbout.append( line); sbout.append("\n");
			}
			br.close();
			fis.close();
		}
		catch ( IOException ex ) {
			return false;
		}
		return true;
	}
	
	public static String getQueryStringVal(String szQuery, String szKeyName) {
		try {
			if (!szKeyName.endsWith("=")) {
				StringBuffer sbKeyName = new StringBuffer(szKeyName.toLowerCase());
				sbKeyName.append('=');
				szKeyName = sbKeyName.toString();
			}

			StringTokenizer stCookieValues = new StringTokenizer(szQuery, "&?");
			while (stCookieValues.hasMoreTokens()) {
				String szCookieKVPair = stCookieValues.nextToken();
				if (szCookieKVPair.toLowerCase().startsWith(szKeyName))
					return new String(szCookieKVPair.substring(szKeyName.length()));
			} // while

			return "";
		} catch (Throwable t) {
			return "";
		}
	} // ebrother_getCookieVal()

	// for wepa
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
	
	
	
	// for wepa
	public static String getDelimitData(String strln, String strSep, int intSeq) {
		if (strln == null || strln.length() == 0)
			return "";

		if (strSep == null || strSep.length() == 0)
			return "";

		int intSep = 0, intIdx = 0;
		int intSepLen = strSep.length();
		String strtemp = strln;

		try {
			while ((intIdx = strtemp.indexOf(strSep, 0)) >= 0) {
				intSep++;
				if (intSep == intSeq)
					return new String(strtemp.substring(0, intIdx));

				strtemp = strtemp.substring(intIdx + intSepLen, strtemp.length());
			}

			intSep++;
			if (intSep == intSeq)
				return new String(strtemp);

			if (intSep < intSeq)
				return "";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
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
	
	// H:\eBrotherProject\rnd\project\sds\nas\WEBLOG
	public static void main(String[] args)	{
		
		Vector<String> vFileL = new Vector<String> ();

		getFileListCore( vFileL, true, "d:/asp/trans/inbound", "" );
		
		for ( String file : vFileL) {
			System.out.println ( file );
		}
	}
	
}

