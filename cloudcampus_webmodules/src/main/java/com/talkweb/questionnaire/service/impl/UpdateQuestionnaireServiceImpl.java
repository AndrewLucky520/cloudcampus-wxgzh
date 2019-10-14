package com.talkweb.questionnaire.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.questionnaire.dao.UpdateQuestionnaireDao;
import com.talkweb.questionnaire.service.UpdateQuestionnaireService;

/**
 * @ClassName QuestionnaireServiceImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:47:33
 */
@Service
public class UpdateQuestionnaireServiceImpl implements UpdateQuestionnaireService {
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private UpdateQuestionnaireDao updateQuestionnaireDao;

	public void update1002269(){
		List<JSONObject> records = updateQuestionnaireDao.queryRecordAll();
		Map<String, Set<Long>> key2AccIds = new HashMap<String, Set<Long>>();
		for(JSONObject json : records){
			Long accId = json.getLong("accountId");
			Long schoolId = json.getLong("schoolId");
			String questionId = json.getString("questionId");
			if(schoolId == null || accId == null || questionId == null){
				throw new RuntimeException("学校Id：" + schoolId + "，账户Id：" + accId + "，问卷Id" + questionId + " 其中至少一个为空！");
			}
			String key = questionId + "#" + schoolId;
			if(!key2AccIds.containsKey(key)){
				key2AccIds.put(key, new HashSet<Long>());
			}
			key2AccIds.get(key).add(accId);
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, JSONObject> key2Json = new HashMap<String, JSONObject>();
		for(Map.Entry<String, Set<Long>> entry : key2AccIds.entrySet()){
			String tmp[] = entry.getKey().split("#");
			Set<Long> accIds = entry.getValue();
			params.put("questionId", tmp[0]);
			params.put("schoolId", tmp[1]);
			Integer objType = updateQuestionnaireDao.queryObjTypeFromObj(params);
			if(objType == null){
				System.out.println("questionId:" + tmp[0] + ", schoolId" + tmp[1] + " 没有对应的objType");
				continue;
			}
			String termInfoId = updateQuestionnaireDao.queryTermInfoIdFromQues(params);
			if(termInfoId == null){
				System.out.println("学年学期字段为空！questionId:" + tmp[0] + "， schoolId:" + tmp[1]);
				continue;
			}
			List<Account> accounts = commonDataService.getAccountBatch(Long.valueOf(tmp[1]), new ArrayList<Long>(accIds), termInfoId);
			if(CollectionUtils.isEmpty(accounts)){
				System.out.println("账户：" + accIds);
				continue;
			}
			for(Account acc : accounts){
				Long accId = acc.getId();
				List<User> users = acc.getUsers();
				Long userId = null;
				Long classId = null;
				for(User user : users){
					if(user.getUserPart() == null || user.getUserPart().getRole() == null){
						System.out.println("账户部分为空，accId = " + accId);
						continue;
					}
					if(objType == 1){
						break;
					}else if (objType == 2 && T_Role.Student.equals(user.getUserPart().getRole())) {
						classId = user.getStudentPart().getClassId();
						break;
					} else if (objType == 3 && T_Role.Parent.equals(user.getUserPart().getRole())) {
						userId = user.getParentPart().getId();
						classId = user.getParentPart().getClassId();
						break;
					}
				}
				if(objType == 3 && userId == null){
					System.out.println("没有此账户对应的userId！数据类型(objType)：" + objType + "，accId:" + accId + ", schoolId:" + tmp[1] + ", 学年学期:" + termInfoId);
				}
				if(objType != 1 && classId == null){
					System.out.println("没有此账户对应的classId！数据类型(objType)：" + objType + "，accId:" + accId + ", schoolId:" + tmp[1] + ", 学年学期:" + termInfoId);
				}
				JSONObject json = new JSONObject();
				json.put("userId", userId == null ? "" : userId);
				json.put("classId", classId == null ? "" : classId);
				String key = new StringBuffer().append(tmp[0]).append("#").append(tmp[1]).append("#").append(accId).toString();
				key2Json.put(key, json);
			}
		}
		
		for(JSONObject json : records){
			Long accId = json.getLong("accountId");
			Long schoolId = json.getLong("schoolId");
			String questionId = json.getString("questionId");
			String key = new StringBuffer().append(questionId).append("#").append(schoolId).append("#").append(accId).toString();
			JSONObject obj = key2Json.get(key);
			if(obj == null){
				continue;
			}
			obj.put("recordId", json.get("recordId"));
			obj.put("schoolId", schoolId);
			
			updateQuestionnaireDao.updateData(obj);
		}
	}
}
