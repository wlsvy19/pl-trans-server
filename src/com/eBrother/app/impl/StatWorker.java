package com.eBrother.app.impl;

import com.eBrother.wutil.ZValue;
import com.eBrother.dutil.DAOAccess;

import java.util.List;


public class StatWorker {

	DAOAccess m_dao = new DAOAccess ();;
	static StatWorker myself = null;

	static public StatWorker getInstance () {
		
		if ( myself == null ) myself = new StatWorker ();
		return myself;
		
	}

	public String getLoadID ( String szruncd, long ltime, String szip, String szfile) {
		
		ZValue inparam = new ZValue(), outparam = new ZValue ();
		inparam.put( "file", szfile );
		inparam.put( "type", szruncd );
		inparam.put( "ip", szip );
		inparam.put( "updatetime", ltime );
		
		List<ZValue> result = null;
		boolean isok = false;
		ZValue row;
		int ncnt = 0;
		try {
			try {
				result = m_dao.DBList( "LOG_SELECT", inparam );
			}
			catch ( Exception e ) {
			}
			if (result.size() != 0 ) {
				row = result.get(0);
				ncnt = row.getInt("cnt");
			}
			if ( result == null || result.size() == 0 || ncnt == 0 ) {
				try {
					m_dao.DBInsert("LOG_INSERT", inparam );
					isok = true;
				}
				catch ( Exception e ) {
					isok = false;
				}
			}
			if ( isok == false ) {
				try {
					m_dao.DBUpdate("LOG_UPDATE", inparam );
				}
				catch ( Exception e ) {
				}
			}			
		}
		catch ( Exception e ) {
			
		}
		return "";
	}
	
	public String setFile ( String szruncd, long ltime, String szip, String szfile) {
		
		ZValue inparam = new ZValue(), outparam = new ZValue ();
		inparam.put( "file", szfile );
		inparam.put( "type", szruncd );
		inparam.put( "ip", szip );
		inparam.put( "updatetime", ltime );
		
		List<ZValue> result = null;
		boolean isok = false;
		ZValue row;
		int ncnt = 0;
		try {
			try {
				result = m_dao.DBList( "LOG_SELECT", inparam );
			}
			catch ( Exception e ) {
			}
			if (result.size() != 0 ) {
				row = result.get(0);
				ncnt = row.getInt("cnt");
			}
			if ( result == null || result.size() == 0 || ncnt == 0 ) {
				try {
					m_dao.DBInsert("LOG_INSERT", inparam );
					isok = true;
				}
				catch ( Exception e ) {
					isok = false;
				}
			}
			if ( isok == false ) {
				try {
					m_dao.DBUpdate("LOG_UPDATE", inparam );
				}
				catch ( Exception e ) {
				}
			}			
		}
		catch ( Exception e ) {
			
		}
		return "";
	}
	
	public void write ( String [] in_key, int [] in_fact ) {
	
		
		// #occ_dtm#, #svr_grp_cd#, #log_svr_id#, #inst_id#, #wk_st#, #ymd#, #hr#, #mi#, #log_cnt#, #log_sz#, #ip_cnt#, #lst_chg_dtm# )
		
		ZValue inparam = new ZValue(), outparam = new ZValue ();
		inparam.put( "svr_grp_cd", in_key[0] );
		inparam.put( "log_svr_id", in_key[1] );
		inparam.put( "inst_id", in_key[2] );
		inparam.put( "ymd", in_key[3] );
		inparam.put( "hr", in_key[4] );
		inparam.put( "mi", in_key[5] );
		inparam.put( "wk_st", in_key[6] );
		
		inparam.put( "log_cnt", in_fact[0] );
		inparam.put( "log_sz", in_fact[1] );
		inparam.put( "ip_cnt", in_fact[2] );
		
		List<ZValue> result = null;
		boolean isok = false;
		ZValue row;
		int ncnt = 0;
		try {
			try {
				result = m_dao.DBList( "statGetCnt", inparam );
				// System.out.println ( result.toString());
			}
			catch ( Exception e ) {
				
			}

			if (result.size() != 0 ) {
				row = result.get(0);
				ncnt = row.getInt("cnt");
				
			}
			
			if ( result == null || result.size() == 0 || ncnt == 0 ) {
				
				try {
					m_dao.DBInsert("statInsert", inparam );
					isok = true;
				}
				catch ( Exception e ) {
					isok = false;
				}
			}
			if ( isok == false ) {
				try {
					m_dao.DBUpdate("statUpdate", inparam );
				}
				catch ( Exception e ) {
				}
			}			
			
		}
		catch ( Exception e ) {
			
		}
	}
	
	public static void main(String[] args)  {

		String [] key = new String [] { "9995", "1", "1", "20130104", "00", "15", "IE" };
		int [] dat = new int [] {100,100,100,0};
		
		StatWorker sw = StatWorker.getInstance();
		
		sw.write(key, dat);
		
	}
}

