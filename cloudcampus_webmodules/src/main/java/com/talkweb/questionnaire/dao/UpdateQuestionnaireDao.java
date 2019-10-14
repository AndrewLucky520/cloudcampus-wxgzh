package com.talkweb.questionnaire.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName QuestionnaireDao.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:46:01
 */
public interface UpdateQuestionnaireDao {
	public List<JSONObject> queryRecordAll();
	
	public Integer queryObjTypeFromObj(Map<String, Object> map);
	
	public String queryTermInfoIdFromQues(Map<String, Object> map);
	
	public int updateData(Map<String, Object> map);
}
