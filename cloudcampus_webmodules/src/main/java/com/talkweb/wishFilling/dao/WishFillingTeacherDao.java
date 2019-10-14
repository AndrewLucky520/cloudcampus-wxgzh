package com.talkweb.wishFilling.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-老师DAO
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return 
 * @version 2.0 2016年11月3日  author：zhh 
 */
public interface WishFillingTeacherDao {
	List<JSONObject> getTbListTeacher (JSONObject param);
}
