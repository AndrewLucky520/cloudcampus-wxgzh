package com.talkweb.committee.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.committee.dao.SupportPoorDao;
 
/** 
* @author  Administrator
* @version 创建时间：2017年12月11日 下午2:45:43 
* 程序的简单说明
*/

@Repository
public class SupportPoorDaoImpl  extends MyBatisBaseDaoImpl implements SupportPoorDao{

	@Override
	public List<JSONObject> getPoorStuList(JSONObject param) {
	 
		return selectList("getPoorStuList", param);
	}

	@Override
	public List<JSONObject> getLeftChildList(JSONObject param) {
	 
		return selectList("getLeftChildList", param);
	}

	@Override
	public List<JSONObject> getCityWorkList(JSONObject param) {
		 
		return selectList("getCityWorkList", param);
	}

	@Override
	public List<JSONObject> getDisableChildList(JSONObject param) {
		 
		return selectList("getDisableChildList", param);
	}

	@Override
	public List<JSONObject> getSinglChildList(JSONObject param) {
		 
		return selectList("getSinglChildList", param);
	}

	@Override
	public int updatePoorStudent(JSONObject param) {
 
		return  update("updatePoorStudent" , param);
	}

	@Override
	public int updateLeftChild(JSONObject param) {
	 
		return  update("updateLeftChild" , param);
	}

	@Override
	public int updateCityWork(JSONObject param) {
		 
		return update("updateCityWork" , param);
	}

	@Override
	public int updateDisableChild(JSONObject param) {
	 
		return update("updateDisableChild" , param);
	}

	@Override
	public int updateSinglChild(JSONObject param) {
         
		return update("updateSinglChild" , param);
	}

	@Override
	public int deletePoorStudent(JSONObject param) {
	 
		return delete("deletePoorStudent", param);
	}

	@Override
	public int deleteLeftChild(JSONObject param) {
		 
		return delete("deleteLeftChild", param);
	}

	@Override
	public int deleteCityWork(JSONObject param) {
		 
		return delete("deleteCityWork", param);
	}

	@Override
	public int deleteDisableChild(JSONObject param) {
		 
		return delete("deleteDisableChild", param);
	}

	@Override
	public int deleteSinglChild(JSONObject param) {
	 
		return delete("deleteSinglChild", param);
	}

	@Override
	public JSONObject getPoorStu(JSONObject param) {
		 
		return selectOne("getPoorStu", param);
	}

	@Override
	public JSONObject getLeftChild(JSONObject param) {
		 
		return selectOne("getLeftChild", param);
	}

	@Override
	public JSONObject getCityWork(JSONObject param) {
		 
		return selectOne("getCityWork", param);
	}

	@Override
	public JSONObject getDisableChild(JSONObject param) {
		 
		return selectOne("getDisableChild", param);
	}

	@Override
	public JSONObject getSinglChild(JSONObject param) {
		 
		return selectOne("getSinglChild", param);
	}

}
