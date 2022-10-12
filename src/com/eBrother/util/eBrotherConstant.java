package com.eBrother.util;

/**
 *  define Constant Variable for eBrother Class
  * @author		CCMEDIA KOREA
 * @since		2000.04
 * @version	3.0
**/

/* UPDATE history 
* 2005/02/27 
* socket size : 2048 --> 1024
	UPDATE CODE : 2005/02/27.3
* append constant for rule system
      	UPDATE CODE : 2005/02/27.1
* append wepa exec type
	UPDATE CODE : 2005/02/27.2

*/

public interface eBrotherConstant
{

	/**
	 * For DB Driver type <br>
	 * If jdbc, we do not use DB Pooling
	 */
	int	EB_DBDRIVER_DEFAULT = 0,	EB_DBDRIVER_ODBC = 0, EB_DBDRIVER_JDBC =  1;
	
	/**
	 * For DB Kind <br>
	 */
	int	EB_DB_DEFAULT = 0, 	EB_DB_MSSQL = 1, EB_DB_SYBASE = 2,	EB_DB_INFORMIX = 3,	EB_DB_ORACLE = 4, EB_DB_DB2 = 5;

	/**
	 * For Socket. <br>
	 */
	 // UPDATE CODE : 2005/02/27.3
	 // set socket size : 1024. because if you set 2048, AIX and other system do set send and recieve full data.
	 // escpeailly, kyobo life 
	// int	EB_SOCKET_BUF_SIZE = 2048;
	int	EB_SOCKET_BUF_SIZE = 1024;
	//////////////////////////////////////////
	
		
    	//int	EB_SOCKET_BUF_SIZE = 512;
    	int	EB_HOSTHEADERTYPE_HOST	=	0;
	int	EB_HOSTHEADERTYPE_PORT	=	1;
	String	EB_HOSTHEADERTYPE_DELIMETER = ":";

	int		EB_TIME_FORM_DAY = 1;
	int		EB_TIME_FORM_HOUR = 2;
	int		EB_TIME_FORM_WEEK = 3;
	
	int		EB_WORKER_TYPE_TCP = 1;
	int		EB_WORKER_TYPE_BOTH = 2;
	int		EB_WORKER_TYPE_DBW = 3;
		
	String EB_COOKIE_DELIMETER = "|";
	String	EB_USERMATCHER_DELIMETER = "|";
	int		EB_USERMATCHER_PROTOCOL_LEN = 14;
	
	int	EB_USERMATCHER_PRO_V1 = 5;
	int	EB_USERMATCHER_PRO_V2 = 6;
	int	EB_USERMATCHER_PRO_V0 = 1;
	
	String EB_WEPA_MSG_DELIMETER = "#";

	int	EB_WEPA_PRO_V1 = 1;
	int	EB_WEPA_PRO_V2 = 2;
	int	EB_WEPA_PRO_V3 = 3;

	// msg starting point
	int	EB_WEPA_MSG_OFFSET	=	5;
	int	EB_WEPA_MSG_INCSIZE	=	4;

	// delimited msg position
	int	EB_WEPA_MSG_CNT		=	1;
	int	EB_WEPA_MSG_ISMSG	=	2;
	int	EB_WEPA_MSG_ISBANNER	=3;
	int	EB_WEPA_MSG_ISIFRAME	=4;
	int	EB_WEPA_MSG_ISPOPUP	=	5;

	int	EB_WEPA_MSG_GU	=		1;
	int	EB_WEPA_MSG_POSCD	=	2;
	int	EB_WEPA_MSG_ICON	=	3;
	int	EB_WEPA_MSG_DATA	=	4;	
	
    int	EB_WEPA_TYPE_MSG = 1;
    int	EB_WEPA_TYPE_POPUP = 2;
    int	EB_WEPA_TYPE_IFRAME = 3;
	int	EB_WEPA_TYPE_BANNER = 4;
	//[HANA]
	int EB_WEPA_INC_MULTDB  = 2;
	int	EB_WEPA_INC_MIDWEAR = 1;
	int EB_WEPA_INC_DEFAULT = 0;
	
	int EB_WEPA_DBPROC_TIME = 20000;
	
	/**
	 * For Recorder
	 */
	int		EB_REC_HEADER_CNT = 14;
	String EB_REC_HEADER_KEY [] = {"HTTP_CC_GUID=","HTTP_CC_SESSION=", "SERVER_HOST=",
							"SERVER_URL=", "REMOTE_ADDR=", "REMOTE_USER=",
							"USER_AGENT=","HTTP_URI=","HTTP_REFERER=",
							"HTTP_COOKIE=","HTTP_METHOD=","HTTP_TIME=","HTTP_QUERY=", 
							"HTTP_ISLOGON=" };
	int		EB_REC_HEADER_LEN [] = {13,16,12,11,12,12,11,9,13,12,12,10,11,13 };	
	
	int		EB_REC_HEAD_GUID = 0;
	int		EB_REC_HEAD_SESSION = 1;
	int		EB_REC_HEAD_SITE = 2;
	int		EB_REC_HEAD_URL = 3;
	int		EB_REC_HEAD_IP = 4;
	int		EB_REC_HEAD_USER = 5;
	int		EB_REC_HEAD_AGENT = 6;
	int		EB_REC_HEAD_URI = 7;
	int		EB_REC_HEAD_REF = 8;
	int		EB_REC_HEAD_COOKIE = 9;
	int		EB_REC_HEAD_METHOD = 10;
	int		EB_REC_HEAD_TIME = 11;
	int		EB_REC_HEAD_QUERY = 12;
	int		EB_REC_HEAD_LOGON = 13;
	
	char	EB_REC_HASH_DELIMITER = ':';
	String	EB_REC_HASH_DELIMITER2 = ":";
	
	int		EB_REC_HASH_IDX_IDQUERY = 1;
	int		EB_REC_HASH_IDX_IDFILEEX = 2;
	int		EB_REC_HASH_IDX_CAT1 = 3;
	int		EB_REC_HASH_IDX_CAT2 = 4;
	int		EB_REC_HASH_IDX_CAT3 = 5;
	
	int		EB_REC_WRI_TEMP	= 1;
	int		EB_REC_WRI_PERM   = 2;
	

	int		EB_REC_TYPE_REAL = 1;
	int		EB_REC_TYPE_BAT = 2;
	int		EB_REC_TYPE_BOTH = 3;
	
	//////////////////////////////
        // UPDATE CODE : 2005/02/27.1
        // Append for Rule 
      	final String       EB_RULE_IN_DELIMETER = "_/";
        final String       EB_RULE_ROW_DELEMITER = "#_";
        final String       EB_RULE_COLUMN_DELEMITER = "_/";
        /////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////
        // UPDATE CODE : 2005/02/27.2
        // append for wepa exec code type
        final int	EB_WEPA_EXEC_TYPE_WEB = 1;
        final int	EB_WEPA_EXEC_TYPE_TEMPLATE = 2;
        final int	EB_WEPA_EXEC_TYPE_DEFAULT = 1;
	///////////////////////////////////////////////////////////////////////////
	
        final String EB_ENCODE = "env_encode";
}
