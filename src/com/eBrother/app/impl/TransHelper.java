package com.eBrother.app.impl;


import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Hashtable;

public class TransHelper implements Runnable, ILogConst {
	
	static Hashtable<String, TransHelper> m_hstore = new Hashtable<String, TransHelper> (); 

	boolean m_stop = false;
	boolean m_started = false;
	ServletContext m_servletContext = null;
	long m_ltimestamp = 0;
	StatHelper m_sh = null;
	
	synchronized public static TransHelper getInstance ( ServletContext sc ) {

		String szpath;
		TransHelper ph;
		
		if ( sc == null ) szpath = "DEFAULT";
		else szpath = sc.getRealPath("");

		if ( m_hstore.containsKey(szpath)) {
			return m_hstore.get(szpath);
		}
		else {
			ph = new TransHelper ( sc );
			m_hstore.put( szpath, ph );
			return ph;
		}
	}

	private TransHelper ( ServletContext sc ) {
		
		m_servletContext = sc;
		m_sh = StatHelper.getInstance(sc);
	}
	
	public void init () {
		
	}	
	
	public void start () {
		
		// prevent dual run
		if ( m_started ) return;
		
		try {
			Thread thread = new Thread( this );
			thread.start();
		}
		catch ( Exception e ) {
			
		}		
	}
	
	/*
	 * stop call
	 */
	public void stop () {

		m_stop = true;
	}	
	
	public void run() {

		m_started = true;

		while ( true ) {
			
			try {

				if ( WNAnalHelper.m_me == null ) {
					// System.out.println ( "TRANS -- WNAnalHelper  --> this is NULL ???");
				}
				else {
					// System.out.println ( "TRANS -- WNAnalHelper  --> this is NOT NULL .................");
				}
			
				run_core ();
				
				for ( int i = 0; i < 6; i++) {
					Thread.sleep(10000); // sleep 1 min.
					if ( m_stop ) break;
				}
				if ( m_stop ) break;
			}
			catch ( Exception e ) {
				
			}
		}

		m_started = false;

	}
	
	public void run_core () {
		
		String [] arstat;
		String szkey;
		
		// handle worker hash 1st.
		Hashtable<String, String []> hstat = TransWorker632.get_status ();
		Enumeration ekeys = hstat.keys();
		long cur = 0L;
		while (ekeys.hasMoreElements()) {
	        szkey = (String) ekeys.nextElement();
	        arstat = hstat.get(szkey);
	        cur = Long.parseLong(arstat[ST_REC_ETIME]);
	        
	        if ( cur > m_ltimestamp) {
	        	m_sh.write_stat ( szkey, arstat);
	        }
	        if ( m_ltimestamp < cur && "Y".equals(arstat[ST_REC_END])) {
	        	hstat.remove(szkey);
	        }
	    }
		
		m_ltimestamp =System.currentTimeMillis();


	}

}
