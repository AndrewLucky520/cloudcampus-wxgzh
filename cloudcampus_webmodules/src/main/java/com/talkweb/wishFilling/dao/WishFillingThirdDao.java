package com.talkweb.wishFilling.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-对外接口DAO
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @time 2016年11月23日  author：zhh
 */
public interface WishFillingThirdDao {
	List<JSONObject> getWfListToThird(JSONObject param);
	List<JSONObject> getByZhStudentToThird(JSONObject param);
	List<JSONObject> getZhStudentToThird(JSONObject param);
	List<JSONObject> getBySubjectNumToThird(JSONObject json);
	List<JSONObject> getSubjectNumToThird(JSONObject json);
	List<JSONObject> getBySubjectStudentToThird(JSONObject json);
	List<JSONObject> getSubjectStudentToThird(JSONObject json);
	List<JSONObject> getDicSubjectListToThird(JSONObject param);
}
