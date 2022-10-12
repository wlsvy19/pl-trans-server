package com.eBrother.app.impl;

import com.eBrother.util.UtilExt;
import com.eBrother.wutil.UtilDecode;

import java.net.URL;
import java.util.StringTokenizer;

public class EBLogParser implements ILogConst {


	static public boolean ccnt_util_parse_header(String[] aszLog, String m_strMsgIn, String sz_tagtoken ) {
		
		boolean nRet = true;
		int nPos1 = 0, nStartIdx = 0, nStartPos = 0, nEndIdx = 0, nPos2 = 0, nEndPos = 0;
		int nLoop = 0;
		String[] aszref = new String[3];
		String strTemp;
		String sztemp;

		try {
			nPos1 = m_strMsgIn.indexOf(EB_REC_HEADER_KEY[0]);
			if (nPos1 < 0)
				return false;

			nStartIdx = 0;
			nStartPos = nPos1 + EB_REC_HEADER_LEN[nStartIdx] + 1;
			for (nLoop = 0; nLoop < (EB_REC_HEADER_CNT - 1); nLoop++) {
				nEndIdx = nLoop + 1;
				nPos2 = m_strMsgIn.indexOf(EB_REC_HEADER_KEY[nEndIdx]);
				if (nPos2 > 0)
					nEndPos = nPos2 - 2; // because key1="  " key2="  "
				else
					nEndPos = 0; // so, always there is space + " ( 2 byte )

				if (nStartPos == nEndPos) {
					
					aszLog[nLoop] = "";
				
				} else if (nStartPos < nEndPos) {
					aszLog[nLoop] = new String(m_strMsgIn.substring(nStartPos, nEndPos));

				} else if (nEndPos == 0 && (nLoop == (EB_REC_HEADER_CNT - 1) || nLoop == (EB_REC_HEADER_CNT - 2))) {

					// this is for last.
					nEndPos = m_strMsgIn.lastIndexOf('"');
					if (nEndPos > 0) {
						if (nStartPos == nEndPos)
							aszLog[nLoop] = "";
						else if (nStartPos < nEndPos)
							aszLog[nLoop] = new String(m_strMsgIn.substring(nStartPos, nEndPos));
					}

					break;
				} else {
					nRet = false;
					break;
				}

				nStartIdx = nEndIdx;
				nStartPos = nPos2 + EB_REC_HEADER_LEN[nStartIdx] + 1;
			}
		} catch (Exception e) {
			nRet = false;
		}

		
		/*System.out.print("-----------------------------------\n");
		for (nLoop = 0; nLoop < EB_REC_HEADER_CNT; nLoop++) {
			
			System.out.print(EB_REC_HEADER_KEY[nLoop]);
			System.out.print(" = ");
			System.out.println(aszLog[nLoop]);
		}*/
		
		strTemp = null;
		if ( aszLog[EB_REC_HEAD_URL].indexOf("tag.") >= 0 ) {
			strTemp = aszLog[EB_REC_HEAD_REF];
			// if ( strTemp != null && strTemp.length() > 0 ) {
			strTemp = UtilExt.getQueryStringVal(aszLog[EB_REC_HEAD_QUERY], "url");
			aszLog [EB_REC_HEAD_REF] = "";
			//}
		}
		// i.e. tag 쪽에 뭔가 data 가 있따...
		if (strTemp != null && strTemp.length() > 1) {
			
			sztemp = UtilDecode.decode ( strTemp, "KOR");
			/*try {
				strTemp = URLDecoder.decode(strTemp);
			} catch (Exception e) {
				// urldecode exception.
				// nothing ...
			}*/

			aszref = ccnt_http_referer(sztemp);
			aszLog[EB_REC_HEAD_SITE] = aszref[0];
			aszLog[EB_REC_HEAD_URL] = aszref[1];

			StringBuffer sb = new StringBuffer(aszref[2]);
			sb.append('&');
			sb.append(aszLog[EB_REC_HEAD_QUERY]);
			aszLog[EB_REC_HEAD_QUERY] = sb.toString();
		}

		if ( m_strMsgIn.indexOf( sz_tagtoken ) >= 0 ) {
			strTemp = UtilExt.getQueryStringVal(aszLog[EB_REC_HEAD_QUERY], "ref");
			sztemp = UtilDecode.decode ( strTemp, "KOR");
			/*try {
				sztemp = URLDecoder.decode( strTemp );
				
			}
			catch ( Exception e ) {
				sztemp = strTemp;
			}*/
			aszLog[EB_REC_HEAD_REF] = sztemp;
		}

		if ( m_strMsgIn.indexOf( sz_tagtoken ) >= 0 ) {
			strTemp = UtilExt.getQueryStringVal(aszLog[EB_REC_HEAD_QUERY], "seg");
			sztemp = UtilDecode.decode ( strTemp, "KOR");
			
			if ( sztemp != null && sztemp.length() > 0 ) aszLog[EB_REC_HEAD_USER] = sztemp;
		}
		
		/*System.out.print(">>>>>>>>>>>>>>>>>>>>>>-------\n");
		for (nLoop = 0; nLoop < EB_REC_HEADER_CNT; nLoop++) {
			System.out.print(EB_REC_HEADER_KEY[nLoop]);
			System.out.print(" = ");
			System.out.println(aszLog[nLoop]);
		}*/

		return nRet;
	}
	
	static public boolean ccnt_util_parse_header(String[] aszLog, String m_strMsgIn) {
		boolean nRet = true;
		int nPos1 = 0, nStartIdx = 0, nStartPos = 0, nEndIdx = 0, nPos2 = 0, nEndPos = 0;
		int nLoop = 0;
		String[] aszref = new String[3];

		try {
			nPos1 = m_strMsgIn.indexOf(EB_REC_HEADER_KEY[0]);
			if (nPos1 < 0)
				return false;

			nStartIdx = 0;
			nStartPos = nPos1 + EB_REC_HEADER_LEN[nStartIdx] + 1;
			for (nLoop = 0; nLoop < (EB_REC_HEADER_CNT - 1); nLoop++) {
				nEndIdx = nLoop + 1;
				nPos2 = m_strMsgIn.indexOf(EB_REC_HEADER_KEY[nEndIdx]);
				if (nPos2 > 0)
					nEndPos = nPos2 - 2; // because key1="  " key2="  "
				else
					nEndPos = 0; // so, always there is space + " ( 2 byte )

				if (nStartPos == nEndPos) {
					aszLog[nLoop] = "";
				} else if (nStartPos < nEndPos) {
					aszLog[nLoop] = new String(m_strMsgIn.substring(nStartPos, nEndPos));
				} else if (nEndPos == 0 && (nLoop == (EB_REC_HEADER_CNT - 1) || nLoop == (EB_REC_HEADER_CNT - 2))) {
					// this is for last.
					nEndPos = m_strMsgIn.lastIndexOf('"');
					if (nEndPos > 0) {
						if (nStartPos == nEndPos)
							aszLog[nLoop] = "";
						else if (nStartPos < nEndPos)
							aszLog[nLoop] = new String(m_strMsgIn.substring(nStartPos, nEndPos));
					}

					break;
				} else {
					nRet = false;
					break;
				}

				nStartIdx = nEndIdx;
				nStartPos = nPos2 + EB_REC_HEADER_LEN[nStartIdx] + 1;
			}
		} catch (Exception e) {
			nRet = false;
		}

		for (nLoop = 0; nLoop < EB_REC_HEADER_CNT; nLoop++) {
			System.out.print(EB_REC_HEADER_KEY[nLoop]);
			System.out.print(" = ");
			System.out.println(aszLog[nLoop]);
		}


		String strTemp = UtilExt.getQueryStringVal(aszLog[EB_REC_HEAD_QUERY], "url");
		if (strTemp != null && strTemp.length() > 1) {
			
			strTemp = UtilDecode.decode ( strTemp, "KOR");

			aszref = ccnt_http_referer(strTemp);
			aszLog[EB_REC_HEAD_SITE] = aszref[0];
			aszLog[EB_REC_HEAD_URL] = aszref[1];

			StringBuffer sb = new StringBuffer(aszref[2]);
			sb.append('&');
			sb.append(aszLog[EB_REC_HEAD_QUERY]);

			aszLog[EB_REC_HEAD_QUERY] = sb.toString();
		}

		for (nLoop = 0; nLoop < EB_REC_HEADER_CNT; nLoop++) {
			System.out.print(EB_REC_HEADER_KEY[nLoop]);
			System.out.print(" = ");
			System.out.println(aszLog[nLoop]);
		}

		return nRet;
	}

	/**
	 * Referer parsing <br>
	 * 
	 * @param String
	 *          URI
	 * @return String Array index 0 : referer site name <br>
	 *         index 1 : uri <br>
	 *         index 2 : query. not use <br>
	 */
	static public String[] ccnt_http_referer(String strHttpValue) {
		String strOValue[] = new String[3];
		strOValue[0] = "";
		strOValue[1] = "";
		strOValue[2] = "";

		if (strHttpValue == null)
			return strOValue;

		try {
			URL u1 = new URL(strHttpValue);
			strOValue[0] = u1.getHost();
			String token1 = u1.getFile();
			StringTokenizer token = new StringTokenizer(token1, "?");
			if (token.countTokens() > 0) {
				strOValue[1] = token.nextToken();
				while (token.hasMoreTokens())
					strOValue[2] = token.nextToken();
			}
		} catch (Exception e) {
			strOValue[0] = "";
			strOValue[1] = "";
			strOValue[2] = "";
		}

		if (strOValue[0] == null)
			strOValue[0] = "";

		if (strOValue[1] == null)
			strOValue[1] = "";

		if (strOValue[2] == null)
			strOValue[2] = "";

		return strOValue;
	}

	/**
	 * URI parsing <br>
	 * 
	 * @param String
	 *          URI
	 * @param String
	 *          query
	 * @return String Array index 0 : path <br>
	 *         index 1 : file <br>
	 *         index 2 : query <br>
	 */
	static public String[] ccnt_http_uri(String strHttpValue, String strQuery) {
		String token1;
		String strOValue[] = new String[3];
		String strBuff = "";

		strOValue[0] = "/";
		strOValue[1] = "";
		strOValue[2] = strQuery;

		try {
			// If the input data is null or etc, then exit
			if (strHttpValue == null || strHttpValue.length() == 0)
				return (strOValue);

			try {
				URL u1 = new URL(strHttpValue);
				token1 = u1.getFile();
			} catch (Exception ee) {
				token1 = strHttpValue;
			}

			StringTokenizer token = new StringTokenizer(token1, "?");
			if (token.countTokens() > 0)
				strBuff = token.nextToken();

			int nlength = strBuff.length();
			if (nlength > 1) {
				int nfile = strBuff.lastIndexOf("/");
				if (nfile > 0) {
					strOValue[0] = new String(strBuff.substring(0, nfile + 1)); // path name
					strOValue[1] = new String(strBuff.substring(nfile + 1, nlength)); // file name
				} else {
					strOValue[0] = "/";
					strOValue[1] = "";
				}
			}
		} catch (Exception e) {}

		return strOValue;
	}
}


