package com.talkweb.wishFilling.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-管理员设置service
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return 
 * @version 2.0 2016年11月3日  author：zhh 
 */
public interface WishFillingService {
	List<JSONObject> getTbList(JSONObject param)throws Exception;
	void createTb(JSONObject param)throws Exception;
	void updateTbName(JSONObject param) throws Exception;
	JSONObject getTb(JSONObject param) throws Exception;
	void deleteTb(JSONObject param) throws Exception;
	int updateTb(JSONObject param) throws Exception;
	JSONObject getProgressTb(JSONObject param) throws Exception;
	JSONObject getProgressTbByZh(JSONObject param) throws Exception;
	int updateByElection(JSONObject param) throws Exception;
	JSONObject getByElection(JSONObject param) throws Exception;
	 List<JSONObject> getZhSubject(JSONObject param) throws Exception;
	 JSONObject getStaticListByStudent(JSONObject param) throws Exception;
	 JSONObject getNoselectedStudentList(JSONObject param) throws Exception;
	 JSONObject getStudentTb(JSONObject param) throws Exception;
	 List<JSONObject> getStaticListBySubject(JSONObject param) throws Exception;
	 List<JSONObject> getStaticListByZh(JSONObject param) throws Exception;
	 List<JSONObject> getStudentZh(JSONObject param) throws Exception;
	 JSONArray exportStudentZh(JSONObject param)throws Exception;
	 int getTotalStudentCount(JSONObject param)throws Exception;
	 List<JSONObject> getDicSubjectList(String sId,String areaCode,String pycc,String isDivided)throws Exception;
	 JSONObject getStaticTotalByStudent(JSONObject param)throws Exception;
	 List<JSONObject> getTbNameList(JSONObject param) throws Exception;
	 List<JSONObject> getZhListByTb(JSONObject param) throws Exception;
	 List<JSONObject> getByAllZhStudent(JSONObject param) throws Exception;
	int deleteStudentTb(JSONObject param)throws Exception;
	int getByTotalStudentCount(JSONObject param)throws Exception;
	int sendWx(JSONObject param);
}
