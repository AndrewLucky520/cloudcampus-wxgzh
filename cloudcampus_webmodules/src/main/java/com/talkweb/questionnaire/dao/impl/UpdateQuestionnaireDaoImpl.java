package com.talkweb.questionnaire.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.questionnaire.dao.UpdateQuestionnaireDao;

/**
 * @ClassName QuestionnaireDaoImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:47:16
 */
@Repository
public class UpdateQuestionnaireDaoImpl extends MyBatisBaseDaoImpl  implements UpdateQuestionnaireDao {
	public List<JSONObject> queryRecordAll(){
		return selectList("update1002269_queryRecord");
	}
	
	public Integer queryObjTypeFromObj(Map<String, Object> map){
		return selectOne("update1002269_queryObjTypeFromObj", map);
	}
	
	public String queryTermInfoIdFromQues(Map<String, Object> map){
		return selectOne("update1002269_queryTermInfoIdFromQues", map);
	}
	
	public int updateData(Map<String, Object> map){
		return insert("update1002269_updateInfo", map);
	}
}
