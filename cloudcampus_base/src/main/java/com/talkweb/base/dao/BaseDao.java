package com.talkweb.base.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

public interface BaseDao{

	<T> T selectOne(String statement);
	
	<T> T selectOne(String statement, Object parameter);
	
	<K, V> Map<K, V> selectMap(String statement, String mapKey);
	
	<K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);
	
	<K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);
	
	<E> List<E> selectList(String statement);
	
	<E> List<E> selectList(String statement, Object parameter);
	
	<E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);
	
	void select(String statement, ResultHandler handler); 
	
	void select(String statement, Object parameter, ResultHandler handler);
	
	void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);
	
	int insert(String statement);
	
	int insert(String statement, Object parameter);
	
	int update(String statement);
	
	int update(String statement, Object parameter);
	
	int delete(String statement);
	
	int delete(String statement, Object parameter);
		
}
