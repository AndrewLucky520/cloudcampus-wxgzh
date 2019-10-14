package com.talkweb.committee.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
* @author  Administrator
* @version 创建时间：2017年12月11日 下午2:33:31 
* 程序的简单说明
*/
public interface SupportPoorService {

	public List<JSONObject> getPoorStuList(JSONObject param);
	public List<JSONObject> getLeftChildList(JSONObject param);
	public List<JSONObject> getCityWorkList(JSONObject param);
	public List<JSONObject> getDisableChildList(JSONObject param);
	public List<JSONObject> getSinglChildList(JSONObject param);
	
	
	public JSONObject getPoorStu(JSONObject param);
	public JSONObject getLeftChild(JSONObject param);
	public JSONObject getCityWork(JSONObject param);
	public JSONObject getDisableChild(JSONObject param);
	public JSONObject getSinglChild(JSONObject param);
	
	 
	public int updatePoorStudent(JSONObject param);
	public int updateLeftChild(JSONObject param);
	public int updateCityWork(JSONObject param);
	public int updateDisableChild(JSONObject param);
	public int updateSinglChild(JSONObject param);
	
	public int deletePoorStudent(JSONObject param);
	public int deleteLeftChild(JSONObject param);
	public int deleteCityWork(JSONObject param);
	public int deleteDisableChild(JSONObject param);
	public int deleteSinglChild(JSONObject param);
	
 
}
