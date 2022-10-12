package com.eBrother.app.main;

import com.eBrother.app.impl.ParserWorker;
import com.eBrother.app.impl.StatHelper;
import com.eBrother.app.impl.TransHelper;
import com.eBrother.app.impl.WNAnalHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Hashtable;

/**
 * Application Lifecycle Listener implementation class AdWepaServletContextListener
 *
 */
public class TransListner implements ServletContextListener {
		
	TransHelper m_ph = null;
	StatHelper m_sh = null;
	ParserWorker m_pw = null;
	public static WNAnalHelper m_wah = null;
	
	static Hashtable<String, ServletContext> m_context = new Hashtable<String, ServletContext> (); 

	static public ServletContext get_transSContext () {
		

		return m_context.get("TRANS");
		
	}
	
    public void contextInitialized(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    	
		ServletContext sc = null;

		sc = arg0.getServletContext();
		
		m_context.put("TRANS", sc );
		// parser helper start
		try {
			
			m_ph = TransHelper.getInstance( null );
			m_ph.start();
		}
		catch ( Exception e ) {

		}

		// stat helper start
		try {
			m_sh = StatHelper.getInstance( null );
			m_sh.start();

		}
		catch ( Exception e ) {

		}

		m_context.put( "TRANS", sc );

		try {
			m_wah = WNAnalHelper.getInstance( sc);
			m_wah.start();
			
			ParserWorker.m_wah = m_wah;

		}
		catch ( Exception e ) {
			
			
		}
	
		
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {

    	// TODO Auto-generated method stub
    	if ( m_ph != null ) m_ph.stop();
    	if ( m_sh != null ) m_sh.stop();
    	if ( m_pw != null ) {
    		m_pw.stopGlobal();
    	}
    	
    }
    

}


