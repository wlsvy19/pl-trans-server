package com.eBrother.util;

/**
 * eBrotherWorker Interface
  * @author		CCMEDIA KOREA
 * @since		2000.04
 * @version	3.0
**/
public interface eBrotherWorker
{
	
	/**
	 * Interface method <br>
	 * This is for real time running or DB Writer 
	 * @param Object we pass hashtable
	 * @param Connection db connection
	 * @param int	kind of DB ( Oracle, MSSQL, Informix .. )
	 * 
	 */
	// public void run(Object data, Connection dbConn,  int m_bISMSSqlServer);
	/**
	 * Interface method <br>
	 * This is for TCP Connection of the Recorder
	 * @param Object we pass hashtable
	 */
	public void run(Object data );
	
	
}

