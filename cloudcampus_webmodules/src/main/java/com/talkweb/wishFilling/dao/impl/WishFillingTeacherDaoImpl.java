package com.talkweb.wishFilling.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.wishFilling.dao.WishFillingTeacherDao;

/** 
 * 志愿填报-老师DIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Repository
public class WishFillingTeacherDaoImpl  extends MyBatisBaseDaoImpl implements WishFillingTeacherDao {

	@Override
	public List<JSONObject> getTbListTeacher(JSONObject param) {
		return selectList("getTbListTeacher",param);
	}

	

}
