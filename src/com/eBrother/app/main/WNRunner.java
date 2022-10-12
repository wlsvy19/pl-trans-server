package com.eBrother.app.main;

import com.eBrother.util.eBrotherIni;
import com.eBrother.util.eBrotherUtil;

import java.util.Map;
import java.util.Vector;


public class WNRunner {
	
	public static final String V_FILE_SEPARATOR = System.getProperty("file.separator");
	final static String  WN_RUN_NAME = "[WNRunner]";
	String m_szRunOpt = "";
	String m_szRunDate = "";
	String m_szIni = "";
	String m_szData = "";
	String m_szRptOpt = "";
	String m_szEndDate = "";
	
	String m_szBaseDir = "";
	String m_szResultDir = "";

	String m_szCmdRpt = "";
	String m_szCmdAdHoc = "";
	String m_szCmdLoad = "";
	String m_szCmdExport = "";
	
	String m_szRptGroup = "";
	
	
	eBrotherIni m_ini = null;
	
	String m_szModuleInfo = null;	
	ModuleInfo m_moduleinfo = null;
	
	String m_szDBID = null;
	String m_szDBPWD = null;
	String m_szDBName = null;		
	String m_szDBServer = null;		// tnsname.
	String m_szDBJdbcUrl = null;	// jdbc URL
	
	String m_szQueryBase = null;
	String m_szLogBase = null;
	String m_szCtlBase = null;
	
	String [] m_arrEnv = new String [30]; // env for runtime
	
	public class ModuleInfo {

		/*
		#############################################################################################
		## run opt=module name, run type ( nblog = 0 inbound = 1 )
		##              , cnt loop ( normal =0 process or loop #n = #n -1)
		##              , runtype 2 ( dbm=0 btree = 1)
		##              , userlist ( 0 : not gen, 1 : generate(idanon only), 2 : generate(idanon + userid) )
		##              , result file (  | delimite)
		##              , process exec ( null = not exec , 1 = exec )
		#############################################################################################
		121=rpt_kth_visit_check,0,0,9,0,rprowuserdate_base,1
		*/
		String m_szmodulename = null;
		String m_szRunType = null;
		int m_nCntLoop = 0;
		String m_szRunType2 = null;
		String m_szUserList = null;
		String m_szProcessExec = null;
		
		Vector<String> m_vResult = new Vector<String> ();
		Vector<String> m_vSQLPre = new Vector<String> ();
		Vector<String> m_vSQLPost = new Vector<String> ();
		Vector<String> m_vLoadCTL = new Vector<String> ();

		final int EB_MODULE_NAME = 1;
		final int EB_MODULE_RUNTYPE = 2;
		final int EB_MODULE_CNTLOOP = 3;
		final int EB_MODULE_RUNTYPE2 = 4;
		final int EB_MODULE_USERLIST = 5;
		final int EB_MODULE_RESULT = 6;
		final int EB_MODULE_PROCESS = 7;
		final int EB_MODULE_PRESQL = 8;
		final int EB_MODULE_POSTSQL = 9;
		final int EB_MODULE_LOADCTL = 10;
			
		
		public void set_ModuleInfo ( String szmoduleinfo ) {

			String sztemp, sztemp2;
			int i;

			m_szmodulename = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_NAME );
			m_szRunType = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_RUNTYPE );
			m_szRunType2 = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_RUNTYPE2 );
			m_szUserList = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_USERLIST );
			m_szProcessExec = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_PROCESS );

			sztemp = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_CNTLOOP );
			m_nCntLoop = eBrotherUtil.getIntNumber( sztemp );

			sztemp = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_RESULT );
			for ( i = 1;;i++ ) {
				
				sztemp2 = eBrotherUtil.getDelimitData( sztemp, "|", i ).trim();
				if ( sztemp2.length() == 0 ) break;
				m_vResult.add(  sztemp2);
			}
			
			sztemp = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_PRESQL );
			for ( i = 1;;i++ ) {
				sztemp2 = eBrotherUtil.getDelimitData( sztemp, "|", i ).trim();
				if ( sztemp2.length() == 0 ) break;
				m_vSQLPre.add(  sztemp2);
			}			

			sztemp = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_POSTSQL );
			for ( i = 1;;i++ ) {
				sztemp2 = eBrotherUtil.getDelimitData( sztemp, "|", i ).trim();
				if ( sztemp2.length() == 0 ) break;
				m_vSQLPost.add(  sztemp2);
			}
			
			sztemp = eBrotherUtil.getDelimitData( szmoduleinfo, ",", EB_MODULE_LOADCTL );
			for ( i = 1;;i++ ) {
				sztemp2 = eBrotherUtil.getDelimitData( sztemp, "|", i ).trim();
				if ( sztemp2.length() == 0 ) break;
				m_vLoadCTL.add(  sztemp2);
			}
			
		}
		
	}
	
	public WNRunner () {

	}

	public void set_param ( String [] args ) {

		int i;
		m_szRunOpt = args[0];
		m_szRunDate = args[1];	// domain
		m_szIni = args[2];	// user macther dir
		m_szData = args[3];	// src dir
		m_szRptOpt = args[4];	// dest dir
		
		if ( args.length > 6 ) m_szEndDate = eBrotherUtil.getZeroStr(args[6]);
		if ( args.length > 5 ) m_szRptGroup = args[5];
		else m_szRptGroup = "";

		m_ini = new eBrotherIni ( );
		m_ini.open( m_szIni );

		m_szModuleInfo = m_ini.getString ( "MODULE", m_szRptOpt, "" );

		m_moduleinfo = new ModuleInfo ();
		m_moduleinfo.set_ModuleInfo(m_szModuleInfo );
		

		System.out.println ( WN_RUN_NAME + " ini file : " + m_szIni );
		System.out.println ( WN_RUN_NAME + " module info : " + m_szRptOpt + " = " + m_szModuleInfo );
		
		System.out.println ( WN_RUN_NAME + " m_vLoadCTL : " + m_moduleinfo.m_vLoadCTL.toString()); 
		System.out.println ( WN_RUN_NAME + " m_vSQLPost : " + m_moduleinfo.m_vSQLPost.toString()); 
		System.out.println ( WN_RUN_NAME + " m_vSQLPre : " + m_moduleinfo.m_vSQLPre.toString()); 
		System.out.println ( WN_RUN_NAME + " m_vResult : " + m_moduleinfo.m_vResult.toString());

		if ( m_szModuleInfo.length() == 0 ) {
			
			System.out.println ( WN_RUN_NAME + " please check module Option " );
			return;
		}
		
		m_szDBID = m_ini.getString ( "DATABASE", "id", "" );
		m_szDBPWD = m_ini.getString ( "DATABASE", "pwd", "" );
		m_szDBName = m_ini.getString ( "DATABASE", "name", "" );
		m_szDBServer = m_ini.getString ( "DATABASE", "dbserver", "" );
		m_szDBJdbcUrl = m_ini.getString ( "DATABASE", "server", "" );
		
		Map<String, String> env = System.getenv();
		i = 0;
        for (String envName : env.keySet()) {
        	if ( envName.indexOf( "EB") >= 0 ) m_arrEnv[i++] = envName + "=" + env.get(envName);  
            // System.out.println ( envName + "=" + env.get(envName));
        }

		m_szResultDir = m_ini.getString ( "REPORT", "RESULTPATH", "" );

		if ( env.containsKey("EBROTHER_RESULTDIR")) {
        	m_szResultDir = env.get("EBROTHER_RESULTDIR");
        }		
		
        m_szBaseDir = m_ini.getString ( "GLOBAL", "home", "" );
        
        

        String sztemp = System.getProperty("wnostype");
        boolean bisoswin = true;
        if ( sztemp != null && sztemp.equals("unix")) bisoswin = false;
        
        if ( bisoswin && eBrotherUtil.is_windows()) {
        	m_szCmdRpt = m_szBaseDir + V_FILE_SEPARATOR + "bin" + V_FILE_SEPARATOR +  "runner_rpt.cmd";
        }
        else m_szCmdRpt = m_szBaseDir + '/' + "bin" + '/' +  "runner_rpt.sh";

        if ( bisoswin && eBrotherUtil.is_windows()) {
        	m_szCmdAdHoc = m_szBaseDir + V_FILE_SEPARATOR + "bin" + V_FILE_SEPARATOR + "runner_adhoc.cmd";
        }
        else m_szCmdAdHoc = m_szBaseDir + '/' + "bin" + '/' + "runner_adhoc.sh";
        
        if ( bisoswin && eBrotherUtil.is_windows()) {
        	m_szCmdLoad = m_szBaseDir + V_FILE_SEPARATOR + "bin" + V_FILE_SEPARATOR +  "runner_load.cmd";
        }
        else m_szCmdLoad = m_szBaseDir + '/' + "bin" + '/' +  "runner_load.sh";

        
        if ( bisoswin && eBrotherUtil.is_windows()) {
        	m_szCmdExport = m_szBaseDir + V_FILE_SEPARATOR + "bin" + V_FILE_SEPARATOR +  "runner_export.cmd";
        }
        else m_szCmdExport = m_szBaseDir + '/' + "bin" + '/' +  "runner_export.sh";

        m_szQueryBase = m_szBaseDir + V_FILE_SEPARATOR + "db" + V_FILE_SEPARATOR + "base" + V_FILE_SEPARATOR;
        m_szCtlBase = m_szBaseDir + V_FILE_SEPARATOR + "db" + V_FILE_SEPARATOR + "ctl" + V_FILE_SEPARATOR;
        
        m_szLogBase = m_szBaseDir + V_FILE_SEPARATOR + "log" + V_FILE_SEPARATOR + "report" + V_FILE_SEPARATOR;

	}

	public void init () {


	}

	
	public void run_core () {
	
		int i;
		String szcmd;

		// step 1. run
		if ( m_szRunOpt != null && ( m_szRunOpt.equals("-") || m_szRunOpt.equals("ALL")  || m_szRunOpt.equals("RPT") )) {
			
			if ( m_moduleinfo.m_szmodulename.trim().length() > 0 ) { 
				szcmd =  m_szCmdRpt + " " +  m_szRunDate + " " + m_szIni
								+ " " + m_szData + " " + m_szRptOpt
								+ " " + m_szEndDate;
				System.out.println ( WN_RUN_NAME + " Step 1. " + szcmd );
				eBrotherUtil.run_cmd( true, true, szcmd );
			}
			else {
				
				System.out.println ( WN_RUN_NAME + " Step 1. Run WN report. Skip " );
			}
		}

		if ( m_szRunOpt != null 
				&& ( m_szRunOpt.equals("-") || m_szRunOpt.equals("ALL") || m_szRunOpt.equals("IMP"))) {
			
/*			String m_szDBID = null;
			String m_szDBPWD = null;
			String m_szDBName = null;		
			String m_szDBServer = null;		// tnsname.
			String m_szDBJdbcUrl = null;	// jdbc URL
*/
			System.out.println ( WN_RUN_NAME + " Step 2. Pre AdHoc " );
			for ( i = 0; i < m_moduleinfo.m_vSQLPre.size(); i++ ) {
				szcmd = m_szCmdAdHoc + " " + m_szRunDate + " " + m_szDBID + " " + m_szDBPWD + " "
							+ m_szDBServer + " " + m_moduleinfo.m_vSQLPre.get(i)
									+ " - " + m_moduleinfo.m_vSQLPre.get(i)
									+ " " + m_szRptGroup;
				System.out.println ( WN_RUN_NAME + " Step 2_" + i + " " + szcmd );
				eBrotherUtil.run_cmd( true, true, szcmd, m_arrEnv );
			}

/*
 * $ORACLE_LOADER $JOB_USERID/$JOB_PWD@$JOB_SID ${JOB_CTLFILE} data=${JOB_DATFILE} log=${JOB_LOGFILE} bad=${JOB
_BADFILE} errors=1000000 > /dev/null 2>&1			
 */
			System.out.println ( WN_RUN_NAME + " Step 3. Load " );
			for ( i = 0; i < m_moduleinfo.m_vLoadCTL.size(); i++ ) {
				szcmd = m_szCmdLoad + " " + m_szRunDate + " " + m_szDBID + " " + m_szDBPWD + " "
							+ m_szDBServer 
									+ " " + m_szResultDir + V_FILE_SEPARATOR + m_moduleinfo.m_vResult.get(i) + "." + m_szRunDate
									+ " " + m_moduleinfo.m_vLoadCTL.get(i)
									+ " " + m_moduleinfo.m_vResult.get(i)+ "." + m_szRunDate + ".log"
									+ " " + m_moduleinfo.m_vResult.get(i) + "." + m_szRunDate + ".bad"
									+ " " + m_szRptGroup;
				System.out.println ( WN_RUN_NAME + " Step 3_" + i + " " + szcmd );
				eBrotherUtil.run_cmd( true, true, szcmd, m_arrEnv );
			}

			System.out.println ( WN_RUN_NAME + " Step 4. Post AdHoc " );
			for ( i = 0; i < m_moduleinfo.m_vSQLPost.size(); i++ ) {
				szcmd = m_szCmdAdHoc + " " + m_szRunDate + " " + m_szDBID + " " + m_szDBPWD + " "
							+ m_szDBServer + " " + m_moduleinfo.m_vSQLPost.get(i)
									+ " - " + m_moduleinfo.m_vSQLPost.get(i)
									+ " " + m_szRptGroup;
				System.out.println ( WN_RUN_NAME + " Step 4_" + i + " " + szcmd );
				eBrotherUtil.run_cmd( true, true, szcmd, m_arrEnv );
			}

		}

		/*
		
		if ( m_szRunOpt != null 
				&& ( m_szRunOpt.equals("-") || m_szRunOpt.equals("ALL") || m_szRunOpt.equals("IMP"))) {
			
			for ( i = 0; i < m_moduleinfo.m_vResult.size(); i++ ) {
				szcmd = " ett_dataload.cmd " +  m_szRunDate + " " + m_szIni
						+ " " + m_szData + " " + m_szRptOpt
						+ " " + m_szEndDate;
				eBrotherUtil.run_cmd( true, true, szcmd, m_arrEnv );
			}
		}

		if ( m_szRunOpt != null 
				&& ( m_szRunOpt.equals("-") || m_szRunOpt.equals("ALL") || m_szRunOpt.equals("IMP"))) {
			
			for ( i = 0; i < m_moduleinfo.m_vSQLPost.size(); i++ ) {
				szcmd = " ett_dataload.cmd " +  m_szRunDate + " " + m_szIni
						+ " " + m_szData + " " + m_szRptOpt
						+ " " + m_szEndDate;
				eBrotherUtil.run_cmd( true, true, szcmd, m_arrEnv );
			}
		}
		*/
	}

	public static void main(String[] args)  {
		
		WNRunner c_me;
		
		if ( args.length < 4 ) {
			System.out.println ( WN_RUN_NAME + " [domain name] [user matcher dir] [source file] [target file] " );
			return;
		}

		c_me = new WNRunner ();
		
		c_me.set_param( args );
	
		c_me.run_core ();
		System.out.println ( WN_RUN_NAME + " End " );

	}		
}


