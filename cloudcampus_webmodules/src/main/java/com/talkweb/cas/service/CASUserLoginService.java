package com.talkweb.cas.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;

public interface CASUserLoginService {

	public User getUserById(long schoolId,long userId);
	
	public List<JSONObject> getUserIdByExtId(String extId, String termInfoId);
	
	public String getCurrentXnxq(School school);
	
	public School getSchoolByUserId(long schoolId,long userId);

	public List<JSONObject> getUserIdByConditon(JSONObject condition);

	public void setRedisSchoolPlateKey();	

}