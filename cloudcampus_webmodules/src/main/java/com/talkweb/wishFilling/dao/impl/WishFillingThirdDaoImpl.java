package com.talkweb.wishFilling.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.wishFilling.dao.WishFillingThirdDao;

/** 
 * 志愿填报-设置DIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Repository
public class WishFillingThirdDaoImpl  extends MyBatisBaseDaoImpl implements WishFillingThirdDao {


	@Override
	public List<JSONObject> getWfListToThird(JSONObject param) {
		return selectList("getWfListToThird",param);
	}

	@Override
	public List<JSONObject> getByZhStudentToThird(JSONObject param) {
		return selectList("getByZhStudentToThird",param);
	}

	@Override
	public List<JSONObject> getZhStudentToThird(JSONObject param) {
		return selectList("getZhStudentToThird",param);
	}

	@Override
	public List<JSONObject> getBySubjectNumToThird(JSONObject param) {
		return selectList("getBySubjectNumToThird",param);
	}

	@Override
	public List<JSONObject> getSubjectNumToThird(JSONObject param) {
		return selectList("getSubjectNumToThird",param);
	}

	@Override
	public List<JSONObject> getBySubjectStudentToThird(JSONObject param) {
		return selectList("getBySubjectStudentToThird",param);
	}

	@Override
	public List<JSONObject> getSubjectStudentToThird(JSONObject param) {
		return selectList("getSubjectStudentToThird",param);
	}
	@Override
	public List<JSONObject> getDicSubjectListToThird(JSONObject param) {
		return selectList("getDicSubjectListToThird",param);
	}
}
