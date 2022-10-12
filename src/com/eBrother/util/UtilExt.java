package com.eBrother.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class UtilExt {

	public static final String V_FILE_SEPARATOR = System.getProperty("file.separator");

	public static void set_vector4data ( Vector<String> c_vout, String sz_in, String sz_deli ) {
		
		String sz_temp1;
	
		if ( sz_in == null || sz_in.length() < 1) return;
		if ( c_vout == null ) return;
		for ( int i = 1;; i++) {
			sz_temp1 = UtilExt.getDelimitData( sz_in, sz_deli, i );
			if ( sz_temp1.length() == 0 ) break;
			c_vout.add(sz_temp1);
		}
		return;
	}

	public static String print(byte[] bytes) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("[ ");
	    for (byte b : bytes) {
	        sb.append(String.format("0x%02X ", b));
	    }
	    sb.append("]");
	    return sb.toString();
	}
	
	public static String ebrother_getCookieVal(String szHTTP_COOKIE,
			String szKeyName) {
		try {
			StringTokenizer stCookieValues = new StringTokenizer(szHTTP_COOKIE,
					"; ");
			while (stCookieValues.hasMoreTokens()) {
				String szCookieKVPair = stCookieValues.nextToken();
				if (szCookieKVPair.toLowerCase().startsWith(
						szKeyName.toLowerCase())) {
					int nPos = szCookieKVPair.indexOf("=");
					String szCookieValue = szCookieKVPair.substring(nPos + 1);
					return szCookieValue;
				} // if
			} // while

			return "";
		} catch (Throwable t) {
			return "";
		} finally {
		}
	} // ebrother_getCookieVal()
	
	public static String ebrother_util_makedir(String dir, String url, boolean make, int ntime_out, String sz_downfile ) {
		if (url == null) return "";

		StringBuffer sbfullpath = new StringBuffer();
		char c_q;
		
		try {
			Vector<String> vPath = new Vector<String>();
			String data;
			int i = 1;
			while (true) {
				data = UtilExt.getDelimitData(url, "/", i);
				if (data.length() == 0)
					break;

				vPath.add(data);
				i++;
			}

			StringBuffer sbdir = new StringBuffer(dir);
			
			c_q = dir.charAt( dir.length() - 1);
			if ( c_q != '/' && c_q != '\\' ) sbdir.append(V_FILE_SEPARATOR);

			i = vPath.size();
			for (int j = 0; j < i; j++) {
				
				if ( vPath.get(j).length() > 0 ) {
					sbfullpath.append(vPath.get(j));
					sbfullpath.append(V_FILE_SEPARATOR);
					sbdir.append(vPath.get(j));
					sbdir.append(V_FILE_SEPARATOR);
				}
				if (make) {
					File objFile = new File(sbdir.toString());
					long t = System.currentTimeMillis();
					
					if (!objFile.isDirectory()) {

						if ((System.currentTimeMillis() - t) > ntime_out) {
							UtilExt.createFile(sz_downfile);
							return "";
						}
	
						if (!objFile.mkdir()) {
							if ((System.currentTimeMillis() - t) > ntime_out) UtilExt.createFile(sz_downfile);
							return "";
						}
					}
				}
			}
		} catch (Exception e_makedir) {
			System.out.print("[ebrother_util_makedir]");
			System.out.print(sbfullpath.toString());
			System.out.print(" --> ");
			System.out.print(dir);
			System.out.print(" : ");
			System.out.println(url);

			return "";
		}

		return sbfullpath.toString();
	}
	
	public static String get_file_name(String sz_base, String sz_idanon, String sz_token, boolean fByMonth, boolean fByHour, int ntimeout, String sz_downfile ) {
		
		// if ( sz_base == null ) return null;
		
		StringBuffer sbfile_name = new StringBuffer(sz_base);

		String szdir = ebrother_util_makedir(sz_base, UtilExt.get_idanon2subdir(sz_idanon, false,false),true, ntimeout, sz_downfile );
		if (szdir == null || szdir.length() == 0) return "";

		sbfile_name.append(szdir);
		sbfile_name.append(sz_idanon);
		sbfile_name.append('.');
		sbfile_name.append(sz_token);
		return sbfile_name.toString();
	}
	
	public static String get_idanon2subdir(String sz_idanon, boolean fByMonth, boolean fByHour ) {
		StringBuffer sbret = new StringBuffer();
		final int V_IDANON_YEAR_START = 0;
		final int V_IDANON_YEAR_END = 4;
		final int V_IDANON_MONTH_START = 4;
		final int V_IDANON_MONTH_END = 6;
		final int V_IDANON_MONTH_DATE_START = 6;
		final int V_IDANON_MONTH_DATE_END = 8;
		final int V_IDANON_DATE_START = 4;
		final int V_IDANON_DATE_END = 8;
		final int V_IDANON_HOUR_START = 8;
		final int V_IDANON_HOUR_END = 10;
		final int V_IDANON_MIN_START = 10;
		final int V_IDANON_MIN_END = 12;

		try {
			if (sz_idanon.indexOf(".") > 0) {
				sbret.append(UtilExt.getDelimitData(sz_idanon, ".", 1));
				sbret.append('/');
				sbret.append(UtilExt.getDelimitData(sz_idanon, ".", 2));
				sbret.append('/');
				sbret.append(UtilExt.getDelimitData(sz_idanon, ".", 3));
				sbret.append('/');
				sbret.append(UtilExt.getDelimitData(sz_idanon, ".", 4));
				sbret.append('/');
			} else {

				sbret.append( new String (sz_idanon.substring(V_IDANON_YEAR_START, V_IDANON_YEAR_END)));
				sbret.append('/');
				if (fByMonth) {
					sbret.append(sz_idanon.substring(V_IDANON_MONTH_START, V_IDANON_MONTH_END));
					sbret.append('/');
					sbret.append(sz_idanon.substring(V_IDANON_MONTH_DATE_START, V_IDANON_MONTH_DATE_END));
				} else {
					sbret.append(sz_idanon.substring(V_IDANON_DATE_START, V_IDANON_DATE_END));
				}
				sbret.append('/');

				if ( sz_idanon.length() > 12 ) {
					if ( fByHour ) {
						sbret.append(sz_idanon.substring(V_IDANON_HOUR_START, V_IDANON_HOUR_END));
						sbret.append('/');
						sbret.append(sz_idanon.substring(V_IDANON_MIN_START, V_IDANON_MIN_END));
					}
					else {
						sbret.append(sz_idanon.substring(V_IDANON_HOUR_START, V_IDANON_MIN_END));
					}
				}
				else {
					sbret.append(sz_idanon.substring(V_IDANON_HOUR_START, V_IDANON_HOUR_END));
				}
				sbret.append('/');
			}
		} catch (Exception e_getid) {
			sbret.setLength(0);
			sbret.append(sz_idanon);
		}

		return sbret.toString();
	}

	public static boolean write_hash ( String sz_file, Hashtable<String,String> c_hcache, Hashtable<String,String> c_hcache2, String sz_final_date ) {
		
		String sz_cache_data;
		BufferedWriter c_bw = null;
		Hashtable<String,String> c_htemp;
		c_bw = UtilExt.getFileWriter ( sz_file, false );

		if ( c_bw == null ) return false;
		try {
			String strHashKey;
			c_htemp = c_hcache;
			if ( c_htemp != null ) {
				for (Enumeration<String> ee = c_htemp.keys(); ee.hasMoreElements();) {
					strHashKey = ee.nextElement();
					if (strHashKey.length() < 2) continue;
					sz_cache_data = c_htemp.get(strHashKey); 
	
					c_bw.write(strHashKey);
					
					if ( sz_cache_data.length() > 0 ) {
						c_bw.write(','); c_bw.write( sz_cache_data );
					}
					c_bw.write(','); c_bw.write( sz_final_date );
					
					c_bw.write("\n");
				}
			}

			c_htemp = c_hcache2;
			if ( c_htemp != null ) {
				for (Enumeration<String> ee = c_htemp.keys(); ee.hasMoreElements();) {
					strHashKey = ee.nextElement();
					if (strHashKey.length() < 2) continue;
					sz_cache_data = c_htemp.get(strHashKey); 
	
					c_bw.write(strHashKey);
					
					if ( sz_cache_data.length() > 0 ) {
						c_bw.write(','); c_bw.write( sz_cache_data );
					}
					c_bw.write(','); c_bw.write( sz_final_date );
					c_bw.write("\n");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				c_bw.close();
			} catch (Exception e) {}
		}
		return true;
	}
	
	public static boolean write_cache ( String sz_file, Vector<String> c_vcache ) {
		
		String sz_cache_data;
		BufferedWriter c_bw = null;
		Vector<String> c_vtemp;
		
		try {
			String strHashKey;
			
			if ( c_vcache != null && c_vcache.size() > 0 ) {
				c_bw = UtilExt.getFileWriter ( sz_file, false );

				if ( c_bw == null ) return false;

				for ( int i = 0; i < c_vcache.size(); i++ ) {
					
					c_bw.write(c_vcache.get(i));
					c_bw.write("\n");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				c_bw.close();
			} catch (Exception e) {}
		}
		return true;
	}
	
	public static boolean write_hash ( String sz_file, Hashtable<String,String> c_hcache, Hashtable<String,String> c_hcache2 ) {
		
		String sz_cache_data;
		BufferedWriter c_bw = null;
		Hashtable<String,String> c_htemp;
		c_bw = UtilExt.getFileWriter ( sz_file, false );

		if ( c_bw == null ) return false;
		try {
			String strHashKey;
			c_htemp = c_hcache;
			if ( c_htemp != null ) {
				for (Enumeration<String> ee = c_htemp.keys(); ee.hasMoreElements();) {
					strHashKey = ee.nextElement();
					if (strHashKey.length() < 2) continue;
					sz_cache_data = c_htemp.get(strHashKey); 
	
					c_bw.write(strHashKey);
					
					if ( sz_cache_data.length() > 0 ) {
						c_bw.write(','); c_bw.write( sz_cache_data );
					}
					c_bw.write("\n");
				}
			}

			c_htemp = c_hcache2;
			if ( c_htemp != null ) {
				for (Enumeration<String> ee = c_htemp.keys(); ee.hasMoreElements();) {
					strHashKey = ee.nextElement();
					if (strHashKey.length() < 2) continue;
					sz_cache_data = c_htemp.get(strHashKey); 
	
					c_bw.write(strHashKey);
					
					if ( sz_cache_data.length() > 0 ) {
						c_bw.write(','); c_bw.write( sz_cache_data );
					}
					c_bw.write("\n");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				c_bw.close();
			} catch (Exception e) {}
		}
		return true;
	}
	
	public static void clearCache(Hashtable hSrc, long lcheck, long lcur) {
		if (hSrc == null || lcur <= 0)
			return;

		for (Enumeration ee = hSrc.keys(); ee.hasMoreElements();) {
			try {
				String szkey = (String) ee.nextElement();
				long lfile = Long.parseLong((String) hSrc.get(szkey));
				if ((lcur - lfile) > lcheck)
					hSrc.remove(szkey);
			} catch (Exception e) {}
		}
	}

	public static void clearCache(HashMap hSrc, long lcheck, long lcur) {
		if (hSrc == null || lcur <= 0)
			return;

		Set set = hSrc.keySet();
		Object[] hmKeys = set.toArray();
		for (int i = 0; i < hmKeys.length; i++) {
			try {
				String szkey = (String) hmKeys[i];
				long lfile = Long.parseLong((String) hSrc.get(szkey));
				if ((lcur - lfile) > lcheck)
					hSrc.remove(szkey);
			} catch (Exception e) {}
		}
	}

	public static void clearCache(Hashtable hSrc, Hashtable hkey, String sztimestamp) {
		if (hSrc == null || hkey == null)
			return;

		for (Enumeration ee = hSrc.keys(); ee.hasMoreElements();) {
			String szkey = (String) ee.nextElement();
			try {
				Hashtable htemp = (Hashtable) hSrc.get(szkey);
				if (hkey.containsKey(szkey)) {
					for (Enumeration e_in = htemp.keys(); e_in.hasMoreElements();) {
						String szsubkey = (String) e_in.nextElement();
						String szsubdat = (String) htemp.get(szsubkey);
						if (!szsubdat.equals(sztimestamp)) {
							szsubdat = null;
							htemp.remove(szsubkey);
						}
					}
				} else {
					htemp.clear();
					htemp = null;
					hSrc.remove(szkey);
				}
			} catch (Exception e) {}
			// ////////////////////////////////////////////////////////////////
		}
	}


	public static String dumpCache(Hashtable hSrc) {
		StringBuffer sbret = new StringBuffer();

		for (Enumeration ee = hSrc.keys(); ee.hasMoreElements();) {
			String szkey = (String) ee.nextElement();
			Hashtable htemp = (Hashtable) hSrc.get(szkey);
			for (Enumeration e_in = htemp.keys(); e_in.hasMoreElements();) {
				String szsubkey = (String) e_in.nextElement();

				sbret.append(szkey);
				sbret.append("->");
				sbret.append(szsubkey);
				sbret.append('\n');
			}
		}

		return sbret.toString();
	}

	public static String dumpCacheObj(Hashtable hSrc) {
		StringBuffer sbret = new StringBuffer();

		for (Enumeration ee = hSrc.keys(); ee.hasMoreElements();) {
			String szkey = (String) ee.nextElement();
			Object obj = hSrc.get(szkey);
			if (obj instanceof Hashtable) {
				Hashtable htemp = (Hashtable) obj;
				for (Enumeration e_in = htemp.keys(); e_in.hasMoreElements();) {
					String szsubkey = (String) e_in.nextElement();

					sbret.append(szkey);
					sbret.append("->");
					sbret.append(szsubkey);
					sbret.append('\n');
				}
			} else {
				sbret.append(szkey);
				sbret.append("->\n");
			}
		}

		return sbret.toString();
	}

	/*
	 * merge 2 year data into just one. logic : check 2 data ...
	 */
	public static String merge_year(String sz_org, String sz_new) {
		String sz_dest = "";
		return sz_dest;
	}

	public static String get_daypv(String sz_org, int npos) {
		String sz_dest = "";
		return sz_dest;
	}

	public static String replace_year4day(String sz_org, String sz_new, int npos) {
		String sz_dest = "";
		return sz_dest;
	}

	/*
	 * UNIX MONTH : start 0, not 1.
	 */
	public static int get_day4year_ext(boolean bis_fix, String sz_date_base) {
		int year, month, date;
		int nday_end = -1;
		Calendar today;

		try {
			today = Calendar.getInstance();
			if (sz_date_base != null && sz_date_base.length() >= 8) {
				year = Integer.parseInt(sz_date_base.substring(0, 4));
				month = Integer.parseInt(sz_date_base.substring(4, 6));
				if (bis_fix) {
					month--;
					if (month <= 0)
						month = 0;
				}

				date = Integer.parseInt(sz_date_base.substring(6, 8));
				today.set(year, month, date);

				// because substring start at ZERO, 0.
				nday_end = today.get(Calendar.DAY_OF_YEAR);
			} else {
				nday_end = today.get(Calendar.DAY_OF_YEAR);
			}
		} catch (Exception e_day) {
			nday_end = -1;
		}

		return nday_end;
	}

	/*
	 * Fix the month ..
	 */
	public static int get_day4year(String sz_date_base) {
		int nday_end;

		try {
			Calendar today = Calendar.getInstance();
			if (sz_date_base != null && sz_date_base.length() >= 8) {
				int year = Integer.parseInt(sz_date_base.substring(0, 4));
				int month = Integer.parseInt(sz_date_base.substring(4, 6)) - 1;
				int date = Integer.parseInt(sz_date_base.substring(6, 8));
				today.set(year, month, date);

				// because substring start at ZERO, 0.
				nday_end = today.get(Calendar.DAY_OF_YEAR);
			} else {
				nday_end = today.get(Calendar.DAY_OF_YEAR);
			}
		} catch (Exception e) {
			nday_end = -1;
		}

		return nday_end;
	}

	public static int compare_number4bytes(int nmax, String sz_in) {
		int nsum = 0;
		int j;
		byte arr[] = sz_in.getBytes();

		for (int i = 0; i < arr.length; i++) {
			j = arr[i] - 0x30;
			if (j < 0)
				j = 0;

			nsum += j;
			if (nsum > nmax)
				return 1;
		}

		if (nsum > nmax)
			return 1;

		if (nsum == nmax)
			return 0;

		return -1;
	}

	public static int get_number4bytes(String sz_in) {
		int nsum = 0;
		int j;
		byte arr[] = sz_in.getBytes();

		for (int i = 0; i < arr.length; i++) {
			j = arr[i] - 0x30;
			if (j < 0)
				j = 0;

			nsum += j;
		}

		return nsum;
	}

	public static String getFileContent(String szfile) {
		return getFileContent(szfile, 0);
	}

	public static String getZeroStr(String strln) {
		if (strln == null)
			return "";

		return strln.trim();
	}

	/*
	 * setString ( "1", 3, '0' ) ==> "001" strpad ..
	 */
	public static String setString(String sz_src, int intlength, char chrln) {
		if (sz_src == null)
			sz_src = "";
		else
			sz_src = sz_src.trim();

		int nlen = sz_src.length();
		if (intlength > nlen) {
			StringBuffer sb = new StringBuffer();
			int i = intlength - nlen;
			for (int j = 1; j <= i; j++)
				sb.append(chrln);

			sb.append(sz_src);

			return sb.toString();
		} else {
			return sz_src;
		}
	}

	public static String getY2K_CurFullDate() {
		Calendar currdate = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(currdate.getTime());

		/*
		StringBuffer sb = new StringBuffer();
		sb.append(currdate.get(Calendar.YEAR));
		sb.append(setString(String.valueOf(currdate.get(Calendar.MONTH) + 1), 2, '0'));
		sb.append(setString(String.valueOf(currdate.get(Calendar.DATE)), 2, '0'));
		sb.append(setString(String.valueOf(currdate.get(Calendar.HOUR_OF_DAY)), 2, '0'));
		sb.append(setString(String.valueOf(currdate.get(Calendar.MINUTE)), 2, '0'));
		sb.append(setString(String.valueOf(currdate.get(Calendar.SECOND)), 2, '0'));

		return sb.toString();
		*/
	}

	public static String getY2K_Date() {
		Calendar currdate = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(currdate.getTime());

		/*
		StringBuffer sb = new StringBuffer();
		sb.append(currdate.get(Calendar.YEAR));
		sb.append(setString(String.valueOf(currdate.get(Calendar.MONTH) + 1), 2, '0'));
		sb.append(setString(String.valueOf(currdate.get(Calendar.DATE)), 2, '0'));

		return sb.toString();
		*/
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

	public static String getFileContent(String szfile, int nline) {
		return getFileContent(szfile, nline, 0);
	}

	public static String getFileContent(String szfile, int nline, int isremoveCR) {
		StringBuffer sbSQL = new StringBuffer();
		int i = 1;

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(szfile), "utf-8"));
			String sLine;
			try {
				while ((sLine = in.readLine()) != null) {
					sLine = sLine.trim();
					if (sLine.length() == 0)
						continue;

					sbSQL.append(sLine);
					i++;
					if (nline > 0 && i > nline)
						break;

					if (isremoveCR == 0)
						sbSQL.append('\n');
				}
			} catch (Exception e) {
				System.out.print("[AdUtil.getFileContent] Error getFileContent :");
				System.out.println(szfile);
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (Exception e) {}
			}
		} catch (Exception e) {
			sbSQL.setLength(0);
			System.out.print("[AdUtil.getFileContent] Error getFileContent :");
			System.out.println(szfile);
		}

		return sbSQL.toString();
	}


	public static int runFileCallBack(String szfile, ICallBack icall, Hashtable<String, Object> hparam ) {
		BufferedReader in = null;
		int i = -1;

		try {
			in = new BufferedReader(new FileReader(szfile));
			i = 0;

			String sLine;
			while ((sLine = in.readLine()) != null) {
				sLine = sLine.trim();
				i++;
				if (sLine.length() == 0)
					continue;

				icall.run_CallBack(sLine, hparam );
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {}
		}

		return i;
	}

	static public void set_cache ( Vector<String> c_vtemp, String sz_keytoken, String sz_file ) {
		
		String sz_type;
		String sz_key;
		String sz_data;
		String sz_line;
		BufferedReader c_br = UtilExt.getFileReader ( sz_file );
		StringBuffer sb_temp = new StringBuffer ();
		boolean bkey_part = false;
		String sz_keytoken_part = sz_keytoken.substring(0,1);
		if ( sz_keytoken.indexOf('?') >= 0 ) bkey_part = true; 
		if ( c_br == null ) return;
		try {
			while ((sz_line = c_br.readLine()) != null) {
				sz_type = UtilExt.getDelimitData(sz_line, ",", 1 );
				if ( ! sz_keytoken.equals("-") && sz_keytoken != null && sz_keytoken.length() > 0 ) {
					if ( ! bkey_part ) {
						if ( sz_type.equals( sz_keytoken)) continue;
					}
					else {
						if ( sz_type.startsWith(sz_keytoken_part )) continue;
					}
				}
				sz_key = UtilExt.getDelimitData(sz_line, ",", 2 );
				sz_data = UtilExt.getDelimitDataRight(sz_line, ",", 3 );

				sb_temp.setLength(0);
				sb_temp.append( sz_type);
				sb_temp.append( ',');
				sb_temp.append( sz_key);
				sb_temp.append( ',');
				sb_temp.append( sz_data);
				c_vtemp.add( sb_temp.toString());
			}
		} catch (Exception e) {
			System.out.print("[AdUtil.getFileContent] Error getFileContent :");
		} finally {
			try {
				if (c_br != null)
					c_br.close();
			} catch (Exception e) {}
		}		
	}
	
	/*
	 * 
	 */
	static public void set_cache ( Hashtable<String,String> c_htemp, String sz_keytoken, String sz_file, int nmax_line, int nextra_check_field, int n_cond_max, int n_cond_min ) {
		
		String sz_type;
		String sz_key;
		String sz_data;
		String sz_line;
		BufferedReader c_br = UtilExt.getFileReader ( sz_file );
		String sz_temp1, sz_temp2;
		int n_temp1, n_temp2;
		
		if ( c_br == null ) return;
		if ( nmax_line == 0 ) nmax_line = 9999999;
		try {
			int i = 0;
			while ((sz_line = c_br.readLine()) != null) {

				sz_type = UtilExt.getDelimitData(sz_line, ",", 1 );
				
				if ( sz_keytoken != null && sz_keytoken.length() > 0 && sz_type.equals( sz_keytoken)) continue;

				if ( nextra_check_field > 0 ) {
					n_temp1 = UtilExt.getNumber(UtilExt.getDelimitData( sz_line, ",", nextra_check_field ));
					if ( (n_cond_max != 0 && n_temp1 > n_cond_max ) || (n_cond_min != 0 && n_temp1 < n_cond_min )) continue;
				}
				sz_key = UtilExt.getDelimitData(sz_line, ",", 2 );
				sz_data = UtilExt.getDelimitDataRight(sz_line, ",", 3 );

				String sz_newkey = sz_type + "," + sz_key;

				if ( ! c_htemp.containsKey(sz_newkey)){
					if ( i++ > nmax_line) break;
					c_htemp.put ( sz_newkey, sz_data);
					// System.out.println("[AdUtil.set_cache] cache -> " + sz_newkey + "=" + sz_data );
				}
			}
		} catch (Exception e) {
			System.out.print("[AdUtil.set_cache] Error getFileContent :");
		} finally {
			try {
				if (c_br != null)
					c_br.close();
			} catch (Exception e) {}
		}		
	}
	
	static public void set_cache ( Hashtable c_htemp, String sz_keytoken, String sz_file ) {
		set_cache ( c_htemp, sz_keytoken, sz_file, 0, 0,0, 0 );
	}	

	public static void prtHashData(Hashtable objHashTmp, String strTitle) {
		System.out.print(" DEBUG HASH Start -> ");
		System.out.println(strTitle);

		int i = 0;
		if (objHashTmp != null) {
			for (Enumeration ee = objHashTmp.keys(); ee.hasMoreElements();) {
				String strHashKey = (String) ee.nextElement();
				String strHashDat = objHashTmp.get(strHashKey).toString();

				System.out.print(i++);
				System.out.print(" -> ( ");
				System.out.print(strHashKey);
				System.out.print(" ) = [ ");
				System.out.print(strHashDat);
				System.out.println(" ] ");
			}
		} else {
			System.out.println(" --- HASH TABLE IS NULL ----");
		}

		System.out.print(" DEBUG HASH END -> ");
		System.out.println(strTitle);
	}

	public static long get_timestamp() {
		Calendar kk = Calendar.getInstance();
		return kk.getTimeInMillis();
	}

	public static void prtHashData(PrintWriter output, Hashtable objHashTmp, String strTitle) {
		String strHashKey = "";
		String strHashDat = "";

		output.print(" DEBUG HASH Start -> ");
		output.println(strTitle);

		int i = 0;
		if (objHashTmp != null) {
			for (Enumeration ee = objHashTmp.keys(); ee.hasMoreElements();) {
				strHashKey = (String) ee.nextElement();
				strHashDat = objHashTmp.get(strHashKey).toString();

				output.print(i++);
				output.print(" -> ( ");
				output.print(strHashKey);
				output.print(" ) = [ ");
				output.print(strHashDat);
				output.println(" ] ");
			}
		} else {
			output.println(" --- HASH TABLE IS NULL ----");
		}

		output.print(" DEBUG HASH END -> ");
		output.println(strTitle);
	}

	public static String dumpHashData(Hashtable objHashTmp, String strTitle) {
		StringBuffer sbret = new StringBuffer(" DEBUG HASH Start -> ");
		sbret.append(strTitle);
		sbret.append('\n');

		int i = 0;
		if (objHashTmp != null) {
			for (Enumeration ee = objHashTmp.keys(); ee.hasMoreElements();) {
				String strHashKey = (String) ee.nextElement();
				String strHashDat = objHashTmp.get(strHashKey).toString();

				sbret.append(i++);
				sbret.append(" -> ( ");
				sbret.append(strHashKey);
				sbret.append(" ) = [ ");
				sbret.append(strHashDat);
				sbret.append(" ]\n");
			}
		} else {
			sbret.append(" --- HASH TABLE IS NULL ----\n");
		}

		sbret.append(" DEBUG HASH END -> ");
		sbret.append(strTitle);
		sbret.append('\n');

		return sbret.toString();
	}

	public static BufferedReader getFileReader(String szfile) {
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(szfile));
		} catch (Exception e) {
			in = null;
		}

		return in;
	}

	public static boolean getFileIsNew(String szfile, long lcheckstamp) {
		boolean bret = true;
		File oftemp;

		try {
			oftemp = new File(szfile);
			if (!oftemp.exists()) {
				bret = false;
			} else {
				long ltimestamp = oftemp.lastModified();
				if (lcheckstamp > ltimestamp) {
					bret = false;
					System.out.println("[DEBUG] getFileIsOld checked");
				}
			}
		} catch (Exception e) {}

		oftemp = null;
		return bret;
	}

	public static int getFileIsOld(String szfile, long lcheckstamp) {
		int bret = -1;
		File oftemp;

		try {
			oftemp = new File(szfile);
			if (!oftemp.exists()) {
				bret = -1;
			} else {
				long ltimestamp = oftemp.lastModified();
				if (lcheckstamp > ltimestamp)
					bret = 0;
			}
		} catch (Exception e) {
			bret = -1;
		}

		oftemp = null;

		return bret;
	}

	public static BufferedWriter getFileWriter(String szfile, boolean bappend) {
		BufferedWriter in = null;

		try {
			in = new BufferedWriter(new FileWriter(szfile, bappend));
		} catch (Exception e) {
			in = null;
		}

		return in;
	}

	
	public static BufferedWriter getFileWriter(String szfile) {
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(szfile), "EUC-KR")); 
			// in = new BufferedWriter(new FileWriter(szfile));
		} catch (Exception e) {
			out = null;
		}

		return out;
	}

	public static BufferedWriter getFileWriter(String szfile, String enc) {
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(szfile), enc)); 
			// in = new BufferedWriter(new FileWriter(szfile));
		} catch (Exception e) {
			out = null;
		}

		return out;
	}
	
	public static String getbasename ( String fullfile ) {
		
		int n;
		String szfile;
		n = fullfile.lastIndexOf('/');
		if ( n >= 0 ) {
			szfile = new String (fullfile.substring( n + 1));
		}
		else {
			n = fullfile.lastIndexOf('\\');
			if ( n >= 0 ) {
				szfile = new String (fullfile.substring( n + 1));
			}
			else {
				// System.out.println ( "2" );
				szfile = fullfile;
			}
		}
		return szfile;
	}
	
	public static String replace(String orig, String from, String to) {
		int fromLength = from.length();
		if (fromLength == 0)
			throw new IllegalArgumentException("String to be replaced must not be empty");

		int start = orig.indexOf(from);
		if (start == -1)
			return orig;

		boolean greaterLength = (to.length() >= fromLength);
		StringBuffer buffer;
		// If the "to" parameter is longer than (or
		// as long as) "from", the final length will
		// be at least as large
		if (greaterLength) {
			if (from.equals(to))
				return orig;

			buffer = new StringBuffer(orig.length());
		} else {
			buffer = new StringBuffer();
		}

		char[] origChars = orig.toCharArray();
		int copyFrom = 0;
		while (start != -1) {
			buffer.append(origChars, copyFrom, start - copyFrom);
			buffer.append(to);
			copyFrom = start + fromLength;
			start = orig.indexOf(from, copyFrom);
		}

		buffer.append(origChars, copyFrom, origChars.length - copyFrom);

		return buffer.toString();
	}

	public static String makedir(String dir, String url) {
		int i = 2;
		StringBuffer fullpath = new StringBuffer();
		StringBuffer sb = new StringBuffer(dir);
		sb.append(V_FILE_SEPARATOR);

		try {
			while (true) {
				String data = getDelimitData(url, "/", i);
				if (data.length() == 0)
					break;

				i++;
			}

			for (int j = 2; j < i; j++) {
				String s = getDelimitData(url, "/", j);

				fullpath.append(s);
				fullpath.append(V_FILE_SEPARATOR);

				sb.append(s);
				sb.append(V_FILE_SEPARATOR);

				File objFile = new File(sb.toString());
				try {
					objFile.mkdir();
				} catch (Exception e) {}
			}
		} catch (Exception e_makedir) {
			e_makedir.printStackTrace();
		}

		return fullpath.toString();
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	public static int getNumber(String str) {
		int i, longln;

		if (str == null || str.length() == 0)
			return 0;

		if (str.substring(0, 1).equals("-"))
			i = 1;
		else
			i = 0;

		try {
			StringBuffer qq = new StringBuffer();
			while (i < str.length()) {
				char ch = str.charAt(i);
				if ((!Character.isDigit(ch)) && (ch != ','))
					return 0;

				if (ch != ',')
					qq.append(ch);

				i++;
			}

			if (qq.length() > 0)
				longln = Integer.parseInt(qq.toString());
			else
				longln = 0;

			return longln;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static void createFile(String filename) {
		try {
			File f = new File(filename);
			if (!f.isFile())
				f.createNewFile();
		} catch (Exception e) {}
	}

    //========================================================== listRecursively
    public static void getDirList (File fdir, int depth, Hashtable<String, String> c_hfilelist, String sz_filter, int MAX_DEPTH ) {
        // System.out.println(INDENTS[depth] + fdir.getPath());  // Print name.
        if (fdir.isDirectory() && depth < MAX_DEPTH ) {
            for (File f : fdir.listFiles()) {  // Go over each file/subdirectory.
            	getDirList (f, depth+1, c_hfilelist, sz_filter, MAX_DEPTH );
            }
        }
        else {
        	if ( fdir.getPath().indexOf(sz_filter) > 0 ) {
        		// c_hfilelist.add( fdir.getPath());
        		c_hfilelist.put ( fdir.getPath(), "");
        	}
        }
    }

    
	public static boolean run_cmdnew ( boolean bdebug_cmd, boolean bdebug_out, String [] sz_arrcmd, Logger log ) {
		
		boolean bret = false;
		
		try {
			if ( log == null ) log = Logger.getLogger( eBrotherUtil.class.getName());
			log.debug( sz_arrcmd.toString());

			Process oProcess = new ProcessBuilder(sz_arrcmd).start();
			// oProcess.waitFor();
			// new copy(oProcess.getInputStream());  
			//new copy(oProcess.getErrorStream());
			
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(oProcess.getInputStream())); 
			BufferedReader stdError = new BufferedReader(new InputStreamReader(oProcess.getErrorStream())); 
			String s;
			while ((s = stdOut.readLine()) != null) 
				log.debug (s); 
			while ((s = stdError.readLine()) != null) 
				log.debug (s); 
			

		}
		catch ( Exception e ) {
			log.debug(e.getStackTrace());
		}
		
		return bret;
	}
	
	
	public static boolean run_cmd ( boolean bdebug_cmd, boolean bdebug_out, String sz_cmd, String [] sz_arrenv, Logger log ) {
		
		BufferedReader buf = null;

		if ( log == null ) log = Logger.getLogger( eBrotherUtil.class.getName());
		log.debug( sz_cmd );

		Process pr = null;
		try {
			
			Runtime run = Runtime.getRuntime();
			
			if ( sz_arrenv == null ) { 
				pr = run.exec(sz_cmd);
			}
			else {
				pr = run.exec(sz_cmd, sz_arrenv);
				// pr = run.exec(sz_cmd);
			}

			// pr.waitFor();
			buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) {
				log.debug(line);
			}

			log.debug ("FINISH");
		}
		catch ( Exception e ) {
			
			log.debug( e.getStackTrace());
			try {
				if ( buf != null ) buf.close();
			}
			catch ( Exception e2) {
				log.debug(e2.getMessage());
			}
			
			return false;
		}
		finally {
			
			try {
				if ( buf != null ) buf.close();
				if ( pr != null ) pr.destroy();
			}
			catch ( Exception e2) {
				log.debug(e2.getMessage());
			}
		}
		return true;
	}
	
	public static boolean run_cmd ( boolean bdebug_cmd, boolean bdebug_out, String sz_cmd, Logger log ) {
	
		boolean bret = false;

		bret = run_cmd ( bdebug_cmd, bdebug_out, sz_cmd, null, log );
		return bret;
	}
    
	public static void main(String[] args) {
		
		String sz_in = "0002/,00020243,,2,3,,5,6";
		String sz_out = "";
		String sz_sep = ",";
		
	}
}

