package com.eBrother.app.impl;

public interface ILogConst {

	final int EB_REC_HEADER_CNT = 14;
	final int EB_REC_HEADER_CNT2 = 30;
	final String EB_REC_HEADER_KEY[] = { "HTTP_CC_GUID=", "HTTP_CC_SESSION=", "SERVER_HOST=", "SERVER_URL=", "REMOTE_ADDR=", "REMOTE_USER=",
			"USER_AGENT=", "HTTP_URI=", "HTTP_REFERER=", "HTTP_COOKIE=", "HTTP_METHOD=", "HTTP_TIME=", "HTTP_QUERY=", "HTTP_ISLOGON=", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };

	final int EB_REC_HEADER_LEN[] = { 13, 16, 12, 11, 12, 12, 11, 9, 13, 12, 12, 10, 11, 13, 0,0,0,0,0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0,0,0,0,0 };

	final int EB_REC_HEAD_GUID = 0;
	final int EB_REC_HEAD_SESSION = 1;
	final int EB_REC_HEAD_SITE = 2;
	final int EB_REC_HEAD_URL = 3;
	final int EB_REC_HEAD_IP = 4;
	final int EB_REC_HEAD_USER = 5;
	final int EB_REC_HEAD_AGENT = 6;
	final int EB_REC_HEAD_URI = 7;
	final int EB_REC_HEAD_REF = 8;
	final int EB_REC_HEAD_COOKIE = 9;
	final int EB_REC_HEAD_METHOD = 10;
	final int EB_REC_HEAD_TIME = 11;
	final int EB_REC_HEAD_QUERY = 12;
	final int EB_REC_HEAD_LOGON = 13;

	final int EB_REC_HEAD_REFDOMAIN = 14;
	final int EB_REC_HEAD_REFKEYWORD = 15;
	
	final int EB_REC_HEAD_REFCAM1 = 16;
	final int EB_REC_HEAD_REFCAM2 = 17;
	final int EB_REC_HEAD_REFCAM3 = 18;

	final int EB_REC_HEAD_REF_KEY1 = 19;
	final int EB_REC_HEAD_REF_KEY2 = 20;
	final int EB_REC_HEAD_REF_KEY3 = 21;
	
	final int EB_REC_HEAD_URL_KEY1 = 22;
	final int EB_REC_HEAD_URL_KEY2 = 23;	
	final int EB_REC_HEAD_URL_KEY3 = 24;
	final int EB_REC_HEAD_URL_KEY4 = 25;
	final int EB_REC_HEAD_URL_KEY5 = 26;
	
	final String EC_QUERY_VPRODUCT = "_wn.btp";
	final String EC_QUERY_BPRODUCT = "_wn.bac";
	final String EC_QUERY_OPRODUCT = "_wn.opc";
	final String EC_QUERY_PARTNER = "_wn.btref";
	final String EC_QUERY_ONUM = "_wn.ordnum"; 
	final String EC_QUERY_OID = "_wn.ordno";
	final String EC_QUERY_OPRICE = "_wn.amt";

	final int EC_HEAD_VPRODUCT = 0;
	final int EC_HEAD_BPRODUCT = 1;
	final int EC_HEAD_OPRODUCT = 2;
	final int EC_HEAD_PARTNER = 3;
	final int EC_HEAD_OID = 4;
	final int EC_HEAD_ONUM = 5;
	final int EC_HEAD_OPRICE = 6;
	final int EC_HEAD_KEYWORD = 7;
	final int EC_HEAD_USER_DOMAIN = 8;
	final int EC_HEAD_USERPARTNER = 9;
	
	final String EB_TRANS_START = "start";
	final String EB_TRANS_END = "end";
	
	final int ST_REC_WCNT = 1;
	final int ST_REC_WSIZE = 2;
	final int ST_REC_IP = 3;
	final int ST_REC_END = 0;
	final int ST_REC_ETIME = 4;
}

