package com.eBrother.app.main;

import com.eBrother.app.impl.WNAnalHelper;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class AdWepaServletContextListener
 *
 */
public class ListenerTransClient implements ServletContextListener {
		
	ServletContext m_context = null;

	public static WNAnalHelper m_wah = null;
	
	LogTransClient m_ltc = null;

    public void contextInitialized(ServletContextEvent arg0) {
        // TODO Auto-generated method stub
    	
    	String szini;
		ServletContext sc = null;
		sc = arg0.getServletContext();

		m_ltc = new LogTransClient ();
		m_ltc.setWebApp(true);
		 
		sc.getServletContextName();
		
		szini = sc.getInitParameter("transclientconfig");
		
		if ( szini == null || szini.length() == 0 ) return;
		m_ltc.runWebApp ( szini );

    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {

    	
    }

}


