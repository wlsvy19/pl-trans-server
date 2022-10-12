package com.eBrother.app.core;

import com.eBrother.util.IFilter;
import com.eBrother.util.eBrotherIni;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;

import java.util.Hashtable;


public class BaseMain implements IFilter {

		protected Hashtable<String, Thread> m_thead = new Hashtable<String, Thread> ();
		
		public static final String FILE_SEPARATOR = System.getProperty("file.separator");
		
		protected static Logger m_logger = Logger.getLogger(BaseMain.class.getName());
		
		protected int m_nBatchInterval = 0;
			
		protected eBrotherIni m_ebIni = new eBrotherIni();
		protected eBrotherUtil m_ebUtil = new eBrotherUtil ();	
		
		protected int m_nCheckInterval = 0;
		
		static ClassLoader m_loader = null;

		public String [] m_args = null;
		
		protected boolean m_bexclude = false, m_binclude = false;

		protected String m_szinclude = "";
		protected String m_szexclude = "";
		
		static {
			
			m_loader = BaseMain.class.getClassLoader ();
			
			// m_loader = ClassLoader.getSystemClassLoader();
		}
		
		public void set_result ( String szfile, boolean bresult ) {
			
			m_logger.info("set_result 2");
			
		}
		
		public boolean is_logfile ( String szfile ) {
			  
			int k, l;
			String sztemp;
			
			if ( m_bexclude ) {
				if ( szfile.indexOf( m_szexclude) >= 0 ) return false;
			}

			if ( m_binclude ) {
				
				for ( int i =  1;; i++ ) {
					sztemp = eBrotherUtil.getDelimitData( m_szinclude, "|", i );
					if ( sztemp.length() == 0 ) break;
					if ( szfile.indexOf( sztemp) >= 0 ) return true;
				}
			}

			return true;
		}
		
		public IWorker getWorker ( String szclass ) {

			Object run_object = null;
			Class cl;
			try {
				
				// ClassLoader qqq = ModuleEngine.class.getClassLoader ();
				
				cl = m_loader.loadClass( szclass);
				run_object = cl.newInstance();
			}
			catch ( Exception e ) {
				//AdUtil.log( e.getMessage());
				e.printStackTrace();
				return null;
			}
			return ( IWorker )run_object;
		}

		public static Class getClass ( String szclass ) {

			Class cl;
			try {
				cl = m_loader.loadClass( szclass);
			}
			catch ( Exception e ) {
				//AdUtil.log( e.getMessage());
				//e.printStackTrace();
				return null;
			}
			return cl;
		}
}

