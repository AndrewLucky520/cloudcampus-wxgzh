package com.talkweb.onecard.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.onecard.dao.OneCardDao;

/**
 * @ClassName OneCardDaoImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2016年3月18日 上午11:04:45
 */
@Repository
public class OneCardDaoImpl extends MyBatisBaseDaoImpl implements OneCardDao {
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:21:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByUserId(JSONObject jsonObject) {
		return selectList("queryOneCardByUserId",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:21:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByXnxqOfUserId(JSONObject jsonObject) {
		return selectList("queryOneCardByXnxqOfUserId",jsonObject);
	}

	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:21:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByTeacher(JSONObject jsonObject) {
		return selectList("queryOneCardByTeacher",jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:21:29
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByWeekOfTeacher(JSONObject jsonObject) {
		return selectList("queryOneCardByWeekOfTeacher",jsonObject);
	}
}
