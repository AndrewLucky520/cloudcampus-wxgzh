package com.talkweb.committee.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.committee.dao.SupportPoorDao;
import com.talkweb.committee.service.SupportPoorService;
 

/** 
* @author  Administrator
* @version 创建时间：2017年12月11日 下午2:46:01 
* 程序的简单说明
*/

@Service
public class SupportPoorServiceImpl implements SupportPoorService{

	@Autowired
	private SupportPoorDao supportPoorDao;
	
	@Override
	public List<JSONObject> getPoorStuList(JSONObject param) {
		 
		return supportPoorDao.getPoorStuList(param);
	}

	@Override
	public List<JSONObject> getLeftChildList(JSONObject param) {
	 
		return  supportPoorDao.getLeftChildList(param);
	}

	@Override
	public List<JSONObject> getCityWorkList(JSONObject param) {
		 
		return  supportPoorDao.getCityWorkList(param);
	}

	@Override
	public List<JSONObject> getDisableChildList(JSONObject param) {
	 
		return supportPoorDao.getDisableChildList(param);
	}

	@Override
	public List<JSONObject> getSinglChildList(JSONObject param) {
		 
		return supportPoorDao.getSinglChildList(param);
	}
	
	

	@Override
	public JSONObject getPoorStu(JSONObject param) {
		 
		return supportPoorDao.getPoorStu(param);
	}

	@Override
	public JSONObject getLeftChild(JSONObject param) {
		 
		return supportPoorDao.getLeftChild(param);
	}

	@Override
	public JSONObject getCityWork(JSONObject param) {
		 
		return supportPoorDao.getCityWork(param);
	}

	@Override
	public JSONObject getDisableChild(JSONObject param) {
		 
		return supportPoorDao.getDisableChild(param);
	}

	@Override
	public JSONObject getSinglChild(JSONObject param) {
		 
		return supportPoorDao.getSinglChild(param);
	}
	

	@Override
	public int updatePoorStudent(JSONObject param) {
	 
		return supportPoorDao.updatePoorStudent(param);
	}

	@Override
	public int updateLeftChild(JSONObject param) {
	 
		return supportPoorDao.updateLeftChild(param);
	}

	@Override
	public int updateCityWork(JSONObject param) {
	 
		return supportPoorDao.updateCityWork(param);
	}

	@Override
	public int updateDisableChild(JSONObject param) {
	 
		return  supportPoorDao.updateDisableChild(param);
	}

	@Override
	public int updateSinglChild(JSONObject param) {
		 
		return supportPoorDao.updateSinglChild(param);
	}

	@Override
	public int deletePoorStudent(JSONObject param) {
		 
		return supportPoorDao.deletePoorStudent(param);
	}

	@Override
	public int deleteLeftChild(JSONObject param) {
		 
		return supportPoorDao.deleteLeftChild(param);
	}

	@Override
	public int deleteCityWork(JSONObject param) {
		 
		return supportPoorDao.deleteCityWork(param);
	}

	@Override
	public int deleteDisableChild(JSONObject param) {
		 
		return supportPoorDao.deleteDisableChild(param);
	}

	@Override
	public int deleteSinglChild(JSONObject param) {
		 
		return supportPoorDao.deleteSinglChild(param);
	}

 
	
}
