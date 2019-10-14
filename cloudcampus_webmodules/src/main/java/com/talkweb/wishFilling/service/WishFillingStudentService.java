package com.talkweb.wishFilling.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-学生填报service
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
public interface WishFillingStudentService {
	JSONObject getStudentTb(JSONObject param)throws Exception;
	int addStudentTb(JSONObject param)throws Exception;
	List<JSONObject> getTbSelectList(JSONObject param)throws Exception;
	List<JSONObject> getLastOpenTb(JSONObject param)throws Exception;
	//图片文件
	void insertFile(JSONObject param);
	void deleteFile(JSONObject param);
	JSONObject getFile(JSONObject param);
	JSONObject getFileById(JSONObject param);
}
