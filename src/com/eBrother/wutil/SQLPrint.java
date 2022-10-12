package com.eBrother.wutil;


import java.util.Enumeration;
import java.util.Hashtable;

public class SQLPrint {
	
    public static String getQueryString(String lm_sQueryString, Hashtable pm_oParam) {
        int lm_iIndex = -1;
        int lm_iKeyLen = 0;
        String lm_sKey = null;
        String lm_sValue = null;
        StringBuffer lm_oBuffer = new StringBuffer(lm_sQueryString);
        Hashtable lm_oParam = pm_oParam;
        Enumeration lm_oKeys = pm_oParam.keys();

        while (lm_oKeys.hasMoreElements()) {
            lm_sKey = (String) lm_oKeys.nextElement();
            lm_sValue = (String) lm_oParam.get(lm_sKey);
            lm_iKeyLen = lm_sKey.length();
            while ((lm_iIndex = lm_sQueryString.indexOf(lm_sKey)) != -1) {
                lm_oBuffer =
                    lm_oBuffer.replace(lm_iIndex, lm_iIndex + lm_iKeyLen, lm_sValue);
                lm_sQueryString = lm_oBuffer.toString();
            }
        }
        System.out.println("QueryString::" + lm_sQueryString);
        return lm_sQueryString;
    }

}
