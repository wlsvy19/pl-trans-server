package com.eBrother.dutil;

import com.eBrother.wutil.ZValue;
import com.ibatis.sqlmap.client.SqlMapClient;

import java.util.List;

public class DAOAccess {

	public List<ZValue> DBList(String statement, Object parameter) {

		SqlMapClient sqlMap;
		
		sqlMap = DAOConfig.getSqlMapInstance();
		
		if ( sqlMap == null ) return null;
		
		try {
			
			List<ZValue> localList = (List<ZValue>)sqlMap.queryForList (statement, parameter);
			return localList;
		}
		catch (Exception e) {
			// throw new RuntimeException("DAO Error: " + e);
			e.printStackTrace();
		}
		finally {
			
		}
		return null;
	}
	
	
	public void DBUpdate(String statement, Object parameter) {

		SqlMapClient sqlMap;
		
		sqlMap = DAOConfig.getSqlMapInstance();
		
		if ( sqlMap == null ) return;

		try {
			sqlMap.startTransaction();
			sqlMap.update( statement, parameter);
			sqlMap.commitTransaction();
			sqlMap.endTransaction();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			
		}
	}
	public void DBInsert(String statement, Object parameter) {

		SqlMapClient sqlMap;
		
		sqlMap = DAOConfig.getSqlMapInstance();
		
		if ( sqlMap == null ) return;

		try {
			sqlMap.startTransaction();
			sqlMap.insert( statement, parameter);
			sqlMap.commitTransaction();
			sqlMap.endTransaction();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			
		}
	}
	
	
}
