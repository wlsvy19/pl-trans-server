package com.eBrother.util ;

import java.sql.Connection;
import java.util.Stack;

/**
 *  Thread Pool implemantation
  * @author		CCMEDIA KOREA
 * @since		2000.04
 * @version	3.0
**/
public class eBrotherPool implements  eBrotherConstant 
{
	/**
	 * Handler class for perform work
	 * requested by the Pool.
	 */
	class WorkerThread extends Thread
	{
		
		private eBrotherWorker		_worker ;
		private Object		        _data ;
		private Connection          dbConn ;
		private int				    m_nDBType ;
		private	String		        m_strDriverType;
		private	int			        m_nDriverType;
		private String		        _m_strWorkerName;
		private int				    _m_runGubun = EB_WORKER_TYPE_TCP;
		
		/**
		 * Creates a new WorkerThread
		 * @param id Thread ID
		 * @param worker Worker instance associated with the WorkerThread
		 */
		WorkerThread(String id, eBrotherWorker worker)
		{
			super(id) ;
			_worker = worker ;
			_data = null ;
			dbConn = null ;
			_m_strWorkerName = id;
		}
		
		/**
		 * Wakes the thread and does some work
		 * Other Call may be merging to this.
		 * @param data Data to send to the Worker
		 * @param Connection 
		 * @param int	db kind
		 * @param int	driver type. jdbc or odbc
		 * @param int	RunType : TCP or DB Write or realtime DB Write
		 * @return void
		 */
		synchronized void wake ( Object data, Connection objDBConn, int nDBKind, int nDriverType,  int nRunType  )
		{
			this._data = data ;
			this.dbConn = objDBConn;
			this.m_nDBType = nDBKind;
			this.m_nDriverType = nDriverType;
			this._m_runGubun = nRunType;
			 // System.out.println ( " Worker Notify Ready :  " + _m_strWorkerName );
			notify();
		}
		
		/**
		 * 
		 * for TCP Connection job. <br>
		 * specially, the recorder use it.
		 * @param Object actually hashtable. <br> socket : connection of the client. <br> Class : parent class
		 */
		synchronized void wake (Object data )
		{
			this._data = data ;
			this._m_runGubun = EB_WORKER_TYPE_TCP;
			 // System.out.println ( " Worker Notify Ready :  " + _m_strWorkerName );
			notify();
		}

		/**
		 * for Thread wake
		 * do not anything
		 */
		synchronized void wake (  )
		{
			notify();
		}

		
		/**
		 * WorkerThread's thread routine
		 */
		synchronized public void run()
		{
			boolean stop = false ;
			
			// For DB Write
			// Note. In the db write, there is a infinite loop
			// So, we just return. Maybe it didn't exit.
			try {
				while (!stop && ! _bIsAllStop )	{
					
					if ( _data == null )	{
						try		{
						wait() ;
						}
						catch (InterruptedException e)	{
							e.printStackTrace() ;
							_data = null;
							continue ;
						}
					}

					if ( _bIsAllStop == true ) break;
					// System.out.println ( "[Thread Pool] Run : " + _m_strWorkerName );
					if ( _data != null ) 	{
					
						/**
						 * 
						 * for eBrotherRecorder.
							 * eBrotherRecorder implements connection thread & db write thread.
					 * the db connection thread use  run ( data ) function.
						 * the db writer & other thread use run ( data, connection, db type ) 
						 */
						//switch ( this._m_runGubun  ) {
						//case 1 ://case EB_WORKER_TYPE_TCP :
								_worker.run( this._data  );
								// break;
						//default :
						//	_worker.run( this._data,  this.dbConn,  this.m_nDBType );
						//	break;
						//}
					}
					_data = null;

					// When I returning the dbConnection ?
					// case 1. db pooling
					// case 2. direct connect for jdbc/odbc bridge
				
					dbConn = null;
					
					if ( _bIsAllStop == true ) break;
					
					stop = !(_push (this));
				} // end of while
			} // end of try 
			catch ( Exception ee ) {
		
				System.out.println ( "[eBrotherWorker] Worker Error " + ee );
			}
			
			System.out.println ( "[eBrotherWorker] eBrother Worker Stop: "  + _m_strWorkerName );
		}
	};

	private Stack _waiting;
	private int _max;
	private Class _workerClass;
	private String _m_strPoolName;
	private Connection _m_objDBConn; 
	private int				_m_runGubun; 
	WorkerThread		_m_objWorker ;
	boolean		_bIsAllStop = false;
	
	static private boolean m_bisfull = false;
	
	public boolean isfullcheck () {
		
		return m_bisfull;
	}
	/**
	 * Creates a new Pool instance
	 * @param max Max number of handler threads
	 * @param workerClass Name of Worker implementation
	 * @throws Exception
	 */
	public eBrotherPool ( String strPoolName, int max, Class workerClass) throws Exception
	{

		this._max = max ;
		this._waiting = new Stack() ;
		this._workerClass = workerClass ;
		this._m_strPoolName = strPoolName;
		
		eBrotherWorker worker ;
		WorkerThread w ;
		for ( int i = 0; i < _max; i++ )	{

			worker = (eBrotherWorker)_workerClass.newInstance() ;
			w = new WorkerThread ( strPoolName + ":"+i, worker) ;
			w.start() ;
			_waiting.push (w) ;
			
			System.out.println ( "[eBrotherPool] Thread Pool : " + strPoolName + ":"+i );
		}
	}

	
	/**
	 * called by eBrother Daemon process. <br>
	 * for only  Receving Client Data using TCP <br>
	 * we set run_gubun by EB_WORKER_TYPE_TCP
	 * @param Object  passeing Hashtable <br>
	 *								socket : connection of the client 
	 *								class    : daemon class
	 * @return if success, true otherwise return false.
	 * @exception InstantiationException
	 * @see eBrother.wepa.eBrotherWepa
	 * @see eBrother.recorder.eBrotherRecorder
	 * @see eBrother.wepa.eBrotherUserMatcher
	 */
	public boolean  performWork ( Object data  ) throws InstantiationException
	{
		WorkerThread w = null;
		
		try  {
			synchronized (_waiting)	{
				
				if ( _waiting.empty() )	{
					System.out.println ( "[eBrotherPool] Pool Full. " );
					return false;
				}
				else 	{
					w = (WorkerThread)_waiting.pop() ;
				}
			}
		}
		catch ( Exception e ) {
			System.out.println ( "[eBrotherPool] Perform Work Error " );
			return false;
		}

		w._m_runGubun = EB_WORKER_TYPE_TCP;
		w._data = data;
		w.wake (  ) ;

		return true;
	}

	
	/**
	 * Convience method used by WorkerThread
	 * to put Thread back on the stack
	 * @param w WorkerThread to push
	 * @return boolean True if pushed, false otherwise
	 */
	private boolean _push (WorkerThread w)
	{
		
		boolean stayAround = false ;
		synchronized (_waiting)
		{
			if ( _waiting.size() < _max )
			{
				stayAround = true ;
				_waiting.push (w) ;
			}
		}
		return stayAround ;
	}
	
	public  boolean isFull  ( )
	{
		boolean bRet = false;
		synchronized (_waiting)
		{
			if ( _waiting.empty ())  bRet = true;
		}
		return bRet ;
	}	

	public void		KillAllThread () {
		
		WorkerThread w = null;
		
		try {
			
			_bIsAllStop = true;
			while ( ! _waiting.empty ())  {
				
				try {
					 w = (WorkerThread)_waiting.pop() ;
					 if ( w != null ) {
						 w.notifyAll ();
					 }
				}
				catch ( Exception e ) {
				
					System.out.println ( "[eBrotherWorker] PopUp Errpr : " + e );
				}
				
			} // end of while
			
		}
		catch ( Exception ee ) {
			System.out.println ( "[eBrotherWorker] Kill Error :" + ee);
		}
	}	
}
