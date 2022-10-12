package com.eBrother.util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketUtil implements eBrotherConstant {

	Socket _objSoc = null;
	InputStream _objSocIn = null;
	DataInputStream _objSocDataIn = null;
    BufferedReader _objSocBuffRead = null;
    DataOutputStream _objSocDataOut = null;	
	public String m_strMsgIn = "";
	public byte []m_byteMsgIn = null;
	
	static public ServerSocket getServerSocket ( int port ) {
		
		ServerSocket ssock = null;
		try {
			ssock = new ServerSocket( port );
			
			return ssock;
		}
		catch ( Exception e) {
			
		}
		return null; 
	}
     
	public Socket getSocket () {
		
		return _objSoc;
	}
	public void setSocket ( Socket soc ) {
		
		_objSoc = soc;
		_objSocDataIn = null;
		_objSocBuffRead = null;
		_objSocDataOut = null;
		m_strMsgIn = "";
		getSocInOutStream ();
		
	}
	
	public void close () {
		
		CloseSocInOutStream ();
		
		
	}
	public boolean connectServer(String szServer, int nPort) {

	    boolean bRet = false;
	    try {

	    	_objSoc = TimedSocket.getSocket(szServer, nPort, 10000);

	    	if (_objSoc != null) bRet = this.getSocInOutStream();
	    	
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return bRet;
	}

	public void CloseSocInOutStream() {
	    try {

	    	try {
		        this._objSocDataIn.close();
		    } catch (Exception e1) {
		    
		    }
		    
	    	try {
		        this._objSocDataOut.close();
		      } catch (Exception e1) {
		      }
		      ;
		      try {
		        this._objSoc.close();
		      } catch (Exception e1) {
		      }
		      ;
		    } catch (Exception ee) {
		    }
		    ;

		  }

	public String getClientMsgLine(boolean bret) {

		    String szin;
		    try {
		      szin = "";
		      szin = _objSocBuffRead.readLine();
		      return szin;
		    } catch (Throwable t) {
	
		      return "";
		    }
	}

	public byte[] getCurMsgBytes ( ) {
		
		try {
			
			return this.m_byteMsgIn;
		}
		catch ( Exception e ) {
			return null;
		}
		
	}
	
	
	public int getClientMsgLine () {
		    try {
		      this.m_strMsgIn = "";
		      this.m_strMsgIn = _objSocBuffRead.readLine();
		      return 1;
		    } catch ( Exception e) {
		    	e.printStackTrace();
		    	this.m_strMsgIn = null;
		      return 0;
		    }
		  }

	public String getCurMsg ( String encode ) {
		
		try {
			
			return new String ( this.m_byteMsgIn, encode );
		}
		catch ( Exception e ) {
			return "";
		}
		
	}

	
	
	
	public String getCurMsg ( ) {
		
		try {
			
			return new String ( this.m_byteMsgIn );
		}
		catch ( Exception e ) {
			return "";
		}
		
	}	
	  public int getClientMsg2 (int nLen) {
			
		    int i = 0, nReadTotal = 0, nReadSize = 0;
		
		    this.m_byteMsgIn = new byte[nLen ];

		    try {

		      this.m_strMsgIn = "";
		
		      while (true) {
		
		        if (nLen > (EB_SOCKET_BUF_SIZE + nReadTotal))
		          nReadSize = EB_SOCKET_BUF_SIZE;
		        else
		          nReadSize = nLen - nReadTotal;
		
		        i = this._objSocDataIn.read(this.m_byteMsgIn, nReadTotal, nReadSize);
		
		        if (i >= 0)
		          nReadTotal += i;
		        if (i == -1)
		          break;
		        if (nReadTotal >= nLen)
		          break;
		      }
		      
		      // System.out.println ( " read data(test) : " + nReadTotal + " -> " + new String ( this.m_byteMsgIn));
		      
		    } catch (Throwable t) {
		     
		    	this.m_byteMsgIn = null;
		    	nReadTotal = -1;
		    }
		    return nReadTotal;
		  }	
	
	
	  public int getClientMsg(int nLen) {
	
	    int i = 0, nReadTotal = 0, nReadSize = 0;
	
	    this.m_byteMsgIn = new byte[nLen + 1024];
	
	    try {
	      this.m_strMsgIn = "";
	
	      while (true) {
	
	        if (nLen > (EB_SOCKET_BUF_SIZE + nReadTotal))
	          nReadSize = EB_SOCKET_BUF_SIZE;
	        else
	          nReadSize = nLen - nReadTotal;
	
	        i = this._objSocDataIn.read(this.m_byteMsgIn, nReadTotal, nReadSize);
	
	        if (i >= 0)
	          nReadTotal += i;
	        if (i == -1)
	          break;
	        if (nReadTotal >= nLen)
	          break;
	      }
	    } catch (Throwable t) {
	     
	    	this.m_byteMsgIn = null;
	    	nReadTotal = -1;
	    }
	    return nReadTotal;
	  }
	  
		  public boolean getSocInOutStream() {
		    try {
		      this._objSocIn = this._objSoc.getInputStream();
		      this._objSocDataIn = new DataInputStream(new BufferedInputStream(this._objSocIn));
		      this._objSocBuffRead = new BufferedReader(new InputStreamReader(this._objSoc.getInputStream()));
		      this._objSocDataOut = new DataOutputStream(new BufferedOutputStream(this._objSoc.getOutputStream()));
		      return true;
		    } catch (Exception e) {
		    	e.printStackTrace();
		      return false;
		    }
		  }

	public boolean setClientMsg2 (Socket socket, byte bbb[], int nsize) {
	
	    DataOutputStream objSocDataOut = null;
		int i = 1, j = 0, k = 0;
		try {
			
			objSocDataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			k = nsize;
			while (k > 0) {
				i = Math.min(k, EB_SOCKET_BUF_SIZE);
				objSocDataOut.write(bbb, j, i);
			    j += i;
			    k = k - i;
		  }
		  objSocDataOut.flush();
		  return true;
		} catch (Exception e) {
		  return false;
		}
		
	}
		  
	/*
	  public boolean setClientMsg2(Socket server, byte bbb[], int nsize) {

		    int i = 1, j = 0, k = 0;
		    try {
		      k = nsize;
		      while (k > 0) {
		        i = Math.min(k, EB_SOCKET_BUF_SIZE);
		        this._objSocDataOut.write(bbb, j, i);
		        j += i;
		        k = k - i;
		      }
		      this._objSocDataOut.flush();
		      return true;
		    } catch (Exception e) {
		      return false;
		    }
		  }
	*/
	
		  public boolean setClientMsg(Socket server, byte bbb[], int nsize) {

		    int i = 1, j = 0, k = 0;
		    try {
		      k = nsize;
		      while (k > 0) {
		        i = Math.min(k, EB_SOCKET_BUF_SIZE);
		        this._objSocDataOut.write(bbb, j, i);
		        j += i;
		        k = k - i;
		      }
		      this._objSocDataOut.flush();
		      return true;
		    } catch (Exception e) {
		      return false;
		    }
		  }

		  public boolean setClientMsg(Socket server, String szsrc) {

		    int i = 1, j = 0, k = 0;
		    try {
		      byte[] bbb = szsrc.getBytes();
		      k = bbb.length;
		      
		      while (k > 0) {
		        i = Math.min(k, EB_SOCKET_BUF_SIZE);
		        this._objSocDataOut.write(bbb, j, i);
		        j += i;
		        k = k - i;
		      }
		      this._objSocDataOut.flush();
		      return true;
		    } catch (Exception e) {
		    	
		    	e.printStackTrace();
		      return false;
		    }
		  }

		  public boolean isconnect ( ) {
			  
			  if ( _objSoc.isInputShutdown() || ! _objSoc.isBound() 
					  || _objSoc.isClosed() || _objSoc.isOutputShutdown()
					  || ! _objSoc.isConnected() ) return false;
			  else return true;
			  
		  }
		  public boolean setClientMsg( String strMsgOut ) {

		    int i = 1, j = 0, k = 0;
		    try {
		      byte[] bbb = strMsgOut.getBytes("KSC5601");
		      k = bbb.length;
		      while (k > 0) {
		        i = Math.min(k, EB_SOCKET_BUF_SIZE);
		        this._objSocDataOut.write(bbb, j, i);
		        j += i;
		        k = k - i;
		      }
		      this._objSocDataOut.flush();
		      return true;
		    } catch (Exception e) {
		      return false;
		    }
		  }	
		  
}
