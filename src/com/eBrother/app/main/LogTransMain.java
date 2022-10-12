package com.eBrother.app.main;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;

import java.io.File;

public class LogTransMain {

	final String EB_CLASS = "";
	
	private static final String DEFAULT_ENGINE = "default";
	private static final String DEFAULT_HOST = "localhost";
	private static final String WEB_APPS_NAME = "myapps";
	private static final String DOC_BASE = "docbase";
	
	private Embedded embedded;
	private String catalinaHome;
	String [] m_args = null;
	
	public LogTransMain( String [] args ) {
		
		m_args = args;
		// Register a shutdown hook to do a clean shutdown
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					stopServer();
				}
			}
		);
	}
	
	
	/*
	 * embedded = new Embedded();
  executor = createExecutor();
  StandardEngine engine = createEngine(paths);
  Host host = createHost(paths);
  StandardContext context = createContext(paths);
  Connector connector = createConnector(paths);
  // tomcat binding
  embedded.addEngine(engine);
  engine.setDefaultHost(host.getName());
  engine.addChild(host);
  host.addChild(context);
  Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
  proto.setExecutor(executor);
  embedded.addConnector(connector);
  // spring binding
  EmbeddedSpringContext embeddedSpringContext = (EmbeddedSpringContext) springContext;
  embeddedSpringContext.bind(context.getServletContext());
  // starting
  executor.start();
  embedded.start();
	 */

	private void init() throws Exception {

		String szclass = "com.eBrother.app.main.LogTrans632ProtocolHandler";
		
		File home = (new File(".")).getCanonicalFile();
		catalinaHome = home.getAbsolutePath();

		embedded = new Embedded();
		embedded.setCatalinaHome(catalinaHome);

		// Create an Engine
		Engine engine = embedded.createEngine();
		engine.setName(DEFAULT_ENGINE);

		engine.setDefaultHost(DEFAULT_HOST);
		embedded.addEngine(engine);

		
		// Create a Host
/*		File webAppsLocation = new File(home, WEB_APPS_NAME);
		Host host = embedded.createHost(
				DEFAULT_HOST, webAppsLocation.getAbsolutePath());
		engine.addChild(host);
		
		
		// Add the context
		File docBase = new File(webAppsLocation, DOC_BASE);
		Context context = createContext("", docBase.getAbsolutePath());
		host.addChild(context);
*/

		// Create a connector that listens on all addresses
		// on port 5050
		
		// Connector connector = embedded.createConnector((String)null, 10010, false);
	
		Connector retobj = new Connector( szclass );
		
		retobj.setProtocolHandlerClassName(szclass);
		
		String szBindIP = "";
		String szBindPort = "0";		
		String szInbound = "";
		String szOutbound = "";
		String pattern_server = "";
		String pattern_meta = "";
		
		String szwrite_dirip = "";
		
		if ( m_args.length > 1 ) {
			szBindIP = m_args[0];
		}

		if ( m_args.length > 2 ) {
			szBindPort = m_args[1];
		}
		
		
		try {

			szBindPort = m_args[0];
			szInbound = m_args[1];
			szOutbound = m_args[2];
			
			pattern_meta = m_args[3];
			pattern_server = m_args[4];

			if( m_args.length > 5 ) szwrite_dirip = m_args[5];
			else szwrite_dirip = "";
			
			retobj.setPort(Integer.parseInt(szBindPort));
		
			retobj.setProperty ( "inbound", szInbound );
			retobj.setProperty ( "outbound", szOutbound );
			retobj.setProperty ( "pattern_server", pattern_server );
			retobj.setProperty ( "pattern_meta", pattern_meta );
			retobj.setProperty ( "diripwrite", szwrite_dirip );
			
		}
		catch ( Exception e ) {
			
		}
		// Wire up the connector
		embedded.addConnector(retobj);

	}

	private Context createContext(String path, String docBase){
		// Create a Context
		Context context = embedded.createContext(path, docBase);
		context.setParentClassLoader(this.getClass().getClassLoader());

		// Create a default servlet
		Wrapper servlet = context.createWrapper();
		servlet.setName("default");
		servlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		servlet.setLoadOnStartup(1);
		servlet.addInitParameter("debug", "0");
		servlet.addInitParameter("listings", "false");
		context.addChild(servlet);
		context.addServletMapping("/", "default");

		// Create a handler for jsps
		Wrapper jspServlet = context.createWrapper();
		jspServlet.setName("jsp");
		jspServlet.setServletClass(
		"org.apache.jasper.servlet.JspServlet");
		jspServlet.addInitParameter("fork", "false");
		jspServlet.addInitParameter("xpoweredBy", "false");
		jspServlet.setLoadOnStartup(2);
		context.addChild(jspServlet);
		context.addServletMapping("*.jsp", "jsp");
		context.addServletMapping("*.jspx", "jsp");
		
		// Set seme default welcome files
		context.addWelcomeFile("index.html");
		context.addWelcomeFile("index.htm");
		context.addWelcomeFile("index.jsp");
		context.setSessionTimeout(30);
		
		// Add some mime mappings
		context.addMimeMapping("html", "text/html");
		context.addMimeMapping("htm", "text/html");
		context.addMimeMapping("gif", "image/gif");
		context.addMimeMapping("jpg", "image/jpeg");
		context.addMimeMapping("png", "image/png");
		context.addMimeMapping("js", "text/javascript");
		context.addMimeMapping("css", "text/css");
		context.addMimeMapping("pdf", "application/pdf");
		
		return context;
	}
		
	public void startServer() throws Exception {
		init();
		embedded.start();
	}
		
	public void stopServer() {
		if (embedded != null) {
			try {
				System.out.println("MyServer Shutting try down");
				embedded.stop();
				System.out.println("LogTransMain shutdown");
			} catch (Exception e) {
				//No need to do anything
			}
		}
	}

	public static void main(String args[]) throws Exception {
		
		LogTransMain server = new LogTransMain( args );
		server.startServer();
		
		// This code is just to prevent the sample
		// application from terminating
		synchronized (server) {
			server.wait();
		}
	}
}

