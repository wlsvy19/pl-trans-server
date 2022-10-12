package com.eBrother.app.impl;


import com.eBrother.wutil.ZValue;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


public class WNAnalHelper implements Runnable, ILogConst {
	
	static Hashtable<String, WNAnalHelper> m_hstore = new Hashtable<String, WNAnalHelper> (); 

	boolean m_stop = false;
	boolean m_started = false;
	static public ServletContext m_servletContext = null;
	long m_ltimestamp = 0;
	StatHelper m_sh = null;

	Hashtable<String, ZValue> m_metaFunnelInfo = new Hashtable<String, ZValue> ();
	Hashtable<String, ZValue> m_metaIDFILEEX  = new Hashtable<String, ZValue> ();;
	Hashtable<String, ZValue> m_metaItem = new Hashtable<String, ZValue> ();;
	
	public static WebApplicationContext m_wctx = null;
	public static String m_transme = null;
	public static WNAnalHelper m_me = null;

	public WNAnalHelper ( ServletContext sc ) {
		
		if ( sc == null ) return;
		m_servletContext = sc;
		
		if ( sc != null ) {
		
			m_wctx = WebApplicationContextUtils.getWebApplicationContext(sc);
		}
		
		set_meta ();
		
		m_me = this;
		
		if ( m_me == null ) {
			System.out.println ( "WNAnalHelper  --> this is NULL ???");
		}
		else {
			System.out.println ( "WNAnalHelper  --> this is NOT NULL .................");
		}
		
		m_sh = StatHelper.getInstance(sc);
	}
	
	public String toString () {
		
		return "aaa"; 
	}
	
	/*
	public static String get_funnelid ( String [] arlog ) {
	
		
		
		
	}
	*/
	
	
	synchronized public static WNAnalHelper getInstance ( ServletContext sc ) {

		String szpath;
		WNAnalHelper ph;
		
		// web application 이 아직 준비가 되지 않았다.
		if ( sc == null) {
			return null;
		}
		
		m_servletContext = sc;
		// 설정한다...
		if ( sc != null ) {
			szpath = sc.getRealPath("");
		
			m_transme = szpath;
		}
		else {
			szpath = m_transme;
		}
		if ( m_hstore.containsKey(szpath)) {
			
			m_me = m_hstore.get(szpath);
			return m_me;
		}
		else {
			
			ph = new WNAnalHelper ( sc );
			
			m_me = ph;
			m_hstore.put( szpath, m_me );
			return m_me;
		}
	}

	void set_meta () {
		
		
		try {
			
//			RestDAO rt = ( RestDAO) m_wctx.getBean("restDAO");
//
//			String sqlid = "meta_funnel";
//			ZValue zvl = new ZValue ();
//			List<ZValue> result = null;
//
//			try {
//				result = rt.run( sqlid, zvl);
//				// System.out.println ( result.toString());
//
//				set_meta4funnel ( result);
//			}
//			catch ( Exception e ) {
//				e.printStackTrace();
//			}
		}
		catch ( Exception e ) {
			
		}
	}
	
	void set_meta4funnel ( List<ZValue> result ) {
		
		String szurl;
		String szsite;
		ZValue param;
		String szkey;
		String szdata;
		Hashtable<String, String> htemp = new Hashtable<String, String> ();
		
		// set result
		for ( int i = 0; i < result.size(); i++ ) {
			
			param = result.get(i);
			szsite = (String)param.get("REALDATA");
			szurl = (String)param.get("FULL_FILEPATH");
			
			szkey = szsite + "|" + szurl;
			
			m_metaFunnelInfo.put( szkey, param );
			htemp.put( szkey, "" );
		}
		
		// remove data ..
		for (Enumeration ee = m_metaFunnelInfo.keys(); ee.hasMoreElements() ; ) {
			szkey =  (String) ee.nextElement();
			if ( ! htemp.containsKey(szkey)) m_metaFunnelInfo.remove(szkey);
			
		}

		// clear again ...
		htemp.clear();
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

				// System.out.println ( "WNANALY >>> SET ");
				ParserWorker.m_wah = this;
				run_core ();
				
				Thread.sleep( 60000); // sleep 1 min.
				if ( m_stop ) break;

			}
			catch ( Exception e ) {
				
			}
		}

		m_started = false;

	}
	
	public void run_core () {
		
		// List<ZValue> result = 
		// m_ltimestamp =System.currentTimeMillis();


	}

}
