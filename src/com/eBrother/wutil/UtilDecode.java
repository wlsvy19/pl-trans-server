package com.eBrother.wutil;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * public class eBrotherDecode
 */
 
public class UtilDecode {
	
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
	public static String byteArrayToHex(byte[] ba, int nsize ) {
		
	    if (ba == null || ba.length == 0) {
	        return null;
	    }

	    StringBuffer sb = new StringBuffer( nsize * 2);
	    String hexNumber;
	    for (int x = 0; x < nsize; x++) {
	        hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
	 
	        sb.append(hexNumber.substring(hexNumber.length() - 2));
	    }
	    return sb.toString();
	}
	
	/**
	 * ebDecode()
	 * 
	 * @param szKeyword
	 * @param szLanguage
	 * @return
	 */
	public static String decode(String szKeyword, String szLanguage) {
		String szTemp;

		if (szKeyword == null)
			return "";

		try {
			int nPos = 0;
			if (szKeyword.indexOf("%u") >= 0)
				szKeyword = Unidecode(szKeyword);
			else
				szKeyword = getHexFix(szKeyword);

			nPos = szKeyword.indexOf("%");
			if (nPos >= 0 && szKeyword.length() >= 4) {
				if (szKeyword.length() >= (nPos + 4)) {
					szTemp = szKeyword.toUpperCase();
					String sFlag = "";
					while (1 == 1) {
						nPos = szTemp.indexOf("%");
						if (nPos >= 0 && szTemp.length() >= (nPos + 3)
								&& (szTemp.substring(nPos + 1, nPos + 2).compareTo("2") >= 0 && szTemp.substring(nPos + 1, nPos + 2).compareTo("9") <= 0)) {
							szTemp = szTemp.substring(nPos + 3);
							sFlag = "*";
						} else {
							break;
						}
					} // while

					if (sFlag.equals("*")) {
						nPos = szTemp.indexOf("%");
						if (nPos >= 0 && szTemp.length() >= 4)
							szTemp = szTemp.substring(nPos, nPos + 4);
					} else {
						szTemp = szKeyword.toUpperCase().substring(nPos, nPos + 4);
					}
				} else {
					szTemp = szKeyword.toUpperCase();
				}
			} else {
				szTemp = szKeyword.toUpperCase();
			}

			String szTempReverse;
			if (szTemp.length() == 4 && szTemp.charAt(0) == '%' && szTemp.charAt(1) == 'E' && szTemp.charAt(2) != '0' && szTemp.charAt(2) != '1'
					&& szTemp.charAt(3) == '%') {
				szTemp = URLDecoder.decode(szKeyword, "UTF-8");
				szTempReverse = getHexFix(URLEncoder.encode(szTemp, "UTF-8"));
				if (szTempReverse.equals(szKeyword)) {
					szKeyword = szTemp;
				} else if (szLanguage.equalsIgnoreCase("CHT")) {
					szTemp = URLDecoder.decode(szKeyword, "big5");
					szTempReverse = getHexFix(URLEncoder.encode(szTemp, "big5"));
					if (szTempReverse.equals(szKeyword))
						szKeyword = szTemp;
					else
						szKeyword = URLDecoder.decode(szKeyword, "gbk");
				} else if (szLanguage.equalsIgnoreCase("CHS")) {
					szTemp = URLDecoder.decode(szKeyword, "gbk");
					szTempReverse = getHexFix(URLEncoder.encode(szTemp, "gbk"));
					if (szTempReverse.equals(szKeyword))
						szKeyword = szTemp;
					else
						szKeyword = URLDecoder.decode(szKeyword, "big5");
				} else if (szLanguage.equalsIgnoreCase("KOR")) {
					szKeyword = URLDecoder.decode(szKeyword, "euc-kr");
				} else {
					szKeyword = URLDecoder.decode(szKeyword);
				}
			} else {
				if (szLanguage.equalsIgnoreCase("CHT")) {
					szKeyword = URLDecoder.decode(szKeyword, "big5");
					szTempReverse = getHexFix(URLEncoder.encode(szTemp, "big5"));
					if (szTempReverse.equals(szKeyword))
						szKeyword = szTemp;
					else
						szKeyword = URLDecoder.decode(szKeyword, "gbk");
				} else if (szLanguage.equalsIgnoreCase("CHS")) {
					szKeyword = URLDecoder.decode(szKeyword, "gbk");
					szTempReverse = getHexFix(URLEncoder.encode(szTemp, "gbk"));
					if (szTempReverse.equals(szKeyword))
						szKeyword = szTemp;
					else
						szKeyword = URLDecoder.decode(szKeyword, "big5");
				} else if (szLanguage.equalsIgnoreCase("KOR")) {
					szKeyword = URLDecoder.decode(szKeyword, "euc-kr");
				} else {
					szKeyword = URLDecoder.decode(szKeyword);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return szKeyword;
	} // ebDecode()

	/**
	 * isUTF8()
	 * 
	 * @param szData
	 * @return
	 */
	public static boolean isUTF8(String szData) {
		try {
			int i = 0;
			int nPos = 0;
			int nLen = 0;
			boolean bRC = true;
			byte bData;
			byte baData[] = null;
			String szDataX = "";

			try {
				szDataX = URLDecoder.decode(szData, "UTF8");
			} catch (Exception eee) {
				szDataX = "";
			}

			baData = szDataX.getBytes();
			nLen = baData.length;
			i = 0;
			while (i < nLen && bRC) {
				bData = baData[i];

				if ((bData | 0x7f) == 0x7f)
					bRC = true;
				else
					bRC = false;

				nPos = 1;

				if (!bRC) {
					if ((i + 1) >= nLen)
						break;

					if ((baData[i] | 0xdf) == 0xdf && (baData[i + 1] | 0xbf) == 0xbf)
						bRC = true;
					else
						bRC = false;

					nPos = 2;
				}

				if (!bRC) {
					if ((i + 2) >= nLen)
						break;

					if ((baData[i] | 0xef) == 0xef && (baData[i + 1] | 0xbf) == 0xbf && (baData[i + 2] | 0xbf) == 0xbf)
						bRC = true;
					else
						bRC = false;

					nPos = 3;
				}

				i += nPos;
			}

			if (szDataX == null || szDataX.length() < 1) {
				try {
					szDataX = URLDecoder.decode(szData);
				} catch (Exception eee) {
					szDataX = "";
				}
			}

			return !bRC;
		} catch (Throwable t) {
			t.printStackTrace();

			return false;
		}
	} // isUTF8()

	/**
	 * getHexFix()
	 * 
	 * @param szData
	 * @return
	 */
	public static String getHexFix(String szData) {
		try {
			StringBuffer sbTemp1 = new StringBuffer();
			StringBuffer sbTemp2 = new StringBuffer();
			int nLen = szData.length();
			int l = 0;
			Integer oInt = null;

			for (int i = 0; i < nLen; i++) {
				if (szData.charAt(i) == '%') {
					if ((i + 2) < nLen) {
						sbTemp1.append(szData.charAt(i));
						sbTemp1.append(szData.charAt(i + 1));
						sbTemp1.append(szData.charAt(i + 2));

						sbTemp2.setLength(0);
						sbTemp2.append("0x");
						sbTemp2.append(szData.charAt(i + 1));
						sbTemp2.append(szData.charAt(i + 2));

						
						oInt = Integer.decode(sbTemp2.toString());
						if (oInt.longValue() >= 128 && l == 0)
							l++;

						if (l == 2)
							l = 0;

						i += 2;
						continue;
					} // if
				} // if

				if (szData.charAt(i) != '%' || i != (nLen - 1)) {
					if (l == 1) {
						sbTemp1.append('%');
						sbTemp1.append(Integer.toHexString((int) szData.charAt(i)));
						l = 0;
					} else {
						sbTemp1.append(szData.charAt(i));
					}
				} // if
			} // for

			return sbTemp1.toString();
		} catch (Throwable t) {
			t.printStackTrace();

			return szData;
		}
	} // getHexFix()

	/**
	 * Unidecode()
	 * 
	 * @param uni
	 * @return
	 */
	public static String Unidecode(String szSrc) {
		try {
			StringBuffer sbDecoded = new StringBuffer();
			for (int i = szSrc.indexOf("%u"); i > -1; i = szSrc.indexOf("%u")) {
				sbDecoded.append(szSrc.substring(0, i));
				String strTemp = szSrc.substring(i + 2);
				if (strTemp.length() >= 4) {
					sbDecoded.append((char) Integer.parseInt(szSrc.substring(i + 2, i + 6), 16));
					szSrc = szSrc.substring(i + 6);
				} else {
					break;
				}
			}

			sbDecoded.append(szSrc);
			return sbDecoded.toString();
		} catch (Throwable t) {
			t.printStackTrace();

			return szSrc;
		}
	} // Unidecode()
	
    public static void main(String[] args)  {

    	System.out.println ( UtilDecode.decode ( "%uB12C%uB9AC%20V%uB125%20%uB2C8%uD2B8%20%uC870%uB07C%20C22V041","KOR" ));
    				
    }
} // public class AdDecode
