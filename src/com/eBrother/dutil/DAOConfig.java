package com.eBrother.dutil;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import java.io.IOException;
import java.io.Reader;

public class DAOConfig {

	private static final SqlMapClient sqlMap;  

	static  {
	    try
	    {
	    	String resource = "properties/sql-map-config.xml";
	
	      	resource = "file:///D:/workspace_nibbler/NibblerTrans4/webapp/docs_engine/WEB-INF/config/sqlmap_trans.xml";
	      	//Reader reader = Resources.getResourceAsReader(resource);
	    	
	      	// resource = "file://D:/workspace_nibbler/NibblerTrans4/webapp/docs_engine/WEB-INF/config/sqlmap_trans.xml";
	    	Reader reader = Resources.getUrlAsReader( resource );
	      	// Reader reader = new java.io.FileReader ( resource);
	      	sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader);
	      	
	      	System.out.println (resource );
	      	
	    }
	    catch (IOException e) {
	    	throw new RuntimeException("Error initializing iBATIS ... . Cause: " + e);
	    }
	    	    
	}
	
	public static SqlMapClient getSqlMapInstance() {
		
		return sqlMap;
		
	}
	
	public static void main(String[] args) {

		DAOConfig.getSqlMapInstance();
	}

}

