package com.talkweb.commondata.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;

/**
 * @ClassName CommonDataDao
 * @author Homer
 * @version 1.0
 * @Description 公共数据Dao
 * @date 2015年3月3日
 */
@Repository
public class TETestDao extends MyBatisBaseDaoImpl {
	public List<JSONObject> test1() {
		return selectList("test1");
	}
	public  List<JSONObject> test2(){
		return selectList("test2");
	}
	public  List<JSONObject> test3(){
		return selectList("test3");
	}
	public List<JSONObject> getTarget() {
		return selectList("getTarget");
	}
	public void insertSchool() {
		update("insertSchoolTEST");
	}
	public void deleteSchool(){
		update("deleteSchoolTEST");
	}
	public JSONObject selectSchool() {
		return selectOne("selectSchoolTEST");
	}
}
