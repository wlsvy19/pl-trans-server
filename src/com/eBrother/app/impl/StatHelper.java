package com.eBrother.app.impl;


import javax.servlet.ServletContext;
import java.util.Hashtable;

public class StatHelper implements Runnable {
	
	static Hashtable<String, StatHelper> m_hstore = new Hashtable<String, StatHelper> (); 

	boolean m_stop = false;
	boolean m_started = false;
	ServletContext m_servletContext = null;
	
	synchronized public static StatHelper getInstance ( ServletContext sc ) {

		StatHelper ph;
	
		String szpath;
		if ( sc == null ) szpath = "DEFAULT";
		else szpath = sc.getRealPath("");
		
		if ( m_hstore.containsKey(szpath)) {
			return m_hstore.get(szpath);
		}
		else {
			ph = new StatHelper ( sc );
			m_hstore.put( szpath, ph );
			return ph;
		}
	}

	private StatHelper ( ServletContext sc ) {
		
		m_servletContext = sc;
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
		
	}
	
	public boolean write_stat ( String szkey, String [] arstat) {
		
		boolean bret = false;
		
		
		return bret;
	}
	
}

