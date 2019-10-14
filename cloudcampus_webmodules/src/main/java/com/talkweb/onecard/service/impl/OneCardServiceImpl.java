package com.talkweb.onecard.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.onecard.dao.OneCardDao;
import com.talkweb.onecard.service.OneCardService;

/**
 * @ClassName OneCardServiceImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2016年3月18日 上午11:05:57
 */
@Service
public class OneCardServiceImpl implements OneCardService {
	
	@Autowired
	private OneCardDao oneCardDao;
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:23:53
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByUserId(JSONObject jsonObject) {
		return oneCardDao.queryOneCardByUserId(jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:23:53
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByXnxqOfUserId(JSONObject jsonObject) {
		return oneCardDao.queryOneCardByXnxqOfUserId(jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:23:53
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByTeacher(JSONObject jsonObject) {
		return oneCardDao.queryOneCardByTeacher(jsonObject);
	}
	
	/**
	 *@see 创建的原因
	 *@date 2016年3月18日 上午11:23:53
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOneCardByWeekOfTeacher(JSONObject jsonObject) {
		return oneCardDao.queryOneCardByWeekOfTeacher(jsonObject);
	}

}
