package com.talkweb.onecard.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName OneCardService.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2016年3月18日 上午11:05:14
 */
public interface OneCardService {
	List<JSONObject> queryOneCardByUserId(JSONObject jsonObject);
	List<JSONObject> queryOneCardByXnxqOfUserId(JSONObject jsonObject);
	List<JSONObject> queryOneCardByTeacher(JSONObject jsonObject);
	List<JSONObject> queryOneCardByWeekOfTeacher(JSONObject jsonObject);
}
