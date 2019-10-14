package com.talkweb.base.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.talkweb.base.dao.BaseDao;

public class MyBatisBaseDaoImpl extends SqlSessionDaoSupport implements BaseDao{
	
	@Autowired
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate){
	    super.setSqlSessionTemplate(sqlSessionTemplate);
	}
	
	@Override
	public <T> T selectOne(String statement){

		return getSqlSession().selectOne(statement);	
	}

	@Override
	public <T> T selectOne(String statement, Object parameter){
		return getSqlSession().selectOne(statement, parameter);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, String mapKey){
		return getSqlSession().selectMap(statement, mapKey);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter,String mapKey){
		return getSqlSession().selectMap(statement, parameter, mapKey);
	}

	@Override
	public <K, V> Map<K, V> selectMap(String statement, Object parameter,String mapKey, RowBounds rowBounds){
		return getSqlSession().selectMap(statement, parameter, mapKey, rowBounds);
	}

	@Override
	public <E> List<E> selectList(String statement){
		return getSqlSession().selectList(statement);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter){
		return getSqlSession().selectList(statement, parameter);
	}

	@Override
	public <E> List<E> selectList(String statement, Object parameter,RowBounds rowBounds){
		return getSqlSession().selectList(statement, parameter, rowBounds);
	}

	@Override
	public void select(String statement, ResultHandler handler){
		getSqlSession().select(statement, handler);
	}

	@Override
	public void select(String statement, Object parameter, ResultHandler handler){
		getSqlSession().select(statement, parameter, handler);
	}

	@Override
	public void select(String statement, Object parameter, RowBounds rowBounds,ResultHandler handler){
		getSqlSession().select(statement, parameter, rowBounds, handler);
	}

	@Override
	public int insert(String statement){
		return getSqlSession().insert(statement);
	}

	@Override
	public int insert(String statement, Object parameter) {
		return getSqlSession().insert(statement, parameter);
	}

	@Override
	public int update(String statement){
		return getSqlSession().update(statement);
	}

	@Override
	public int update(String statement, Object parameter){
		return getSqlSession().update(statement, parameter);
	}

	@Override
	public int delete(String statement){
		return getSqlSession().delete(statement);
	}

	@Override
	public int delete(String statement, Object parameter){
		return getSqlSession().delete(statement, parameter);
	}
	
}
