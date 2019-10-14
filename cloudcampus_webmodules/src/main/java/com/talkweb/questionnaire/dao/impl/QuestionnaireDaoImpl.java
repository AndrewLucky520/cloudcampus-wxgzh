package com.talkweb.questionnaire.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.questionnaire.dao.QuestionnaireDao;

/**
 * @ClassName QuestionnaireDaoImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:47:16
 */
@Repository
public class QuestionnaireDaoImpl extends MyBatisBaseDaoImpl  implements QuestionnaireDao {
	@Autowired
	private AllCommonDataService commonDataService;
	
	public boolean ifExistsDataInTermInfo(Map<String, Object> map){
		Integer flag = selectOne("ifExistsDataInTermInfo", map);
		if(flag != null && flag == 1){
			return true;
		}else{
			return false;
		}
	}
	
	public Integer queryObjTypeFromQnObj(Map<String, Object> map){
		return selectOne("queryObjTypeFromQnObj", map);
	}
	/**
	 *@see 查询问卷
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryQuestionList(Map<String, Object> map) {
		return selectList("queryQuestionList",map);
	}
	
	/**
	 *@see 新增问卷调查
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer createQuestionnaire(Map<String, Object> map) {
		return update("createQuestionnaire",map);
	}
	
	/**
	 *@see 编辑问卷调查名字
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer updateQuestionByName(Map<String, Object> map) {
		return update("updateQuestionByName",map);
	}
	
	/**
	 *@see 编辑问卷调查详细信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer updateQuestionnaireByDetail(Map<String, Object> map) {
		return update("updateQuestionnaireByDetail",map);
	}
	
	/**
	 *@see  编辑问卷调查状态信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer updateQuestionnaireStatus(Map<String, Object> map) {
		return update("updateQuestionnaireStatus",map);
	}
	
	/**
	 *@see 新增表格规则信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer createRuleForTable(Map<String, Object> map) {
		return update("createRuleForTable",map);
	}
	/**
	 *@see 删除表格规则信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteRuleForTable(Map<String, Object> map) {
		return update("deleteRuleForTable",map);
	}
	
	/**
	 *@see 查询问卷的详细信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRuleForTableById(Map<String, Object> map) {
		return selectList("queryRuleForTableById",map);
	}

	/**
	 *@see 保存表格信息
	 *@date 2015年10月23日 上午11:05:45
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveQuestionByTab(Map<String, Object> map) {
		return update("saveQuestionByTab",map);
	}
	
	/**
	 *@see 删除表格信息
	 *@date 2015年10月23日 上午11:05:45
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteQuestionByTab(Map<String, Object> map) {
		return update("deleteQuestionByTab",map);
	}
	
	/**
	 *@see 查询问卷的表格信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOptionForTableById(Map<String, Object> map) {
		return selectList("queryOptionForTableById",map);
	}

	/**
	 *@see 删除个性等级信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteQuestionByGxLevel(Map<String, Object> map) {
		return update("deleteQuestionByGxLevel",map);
	}

	/**
	 *@see 删除个性信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteQuestionByGx(Map<String, Object> map) {
		return update("deleteQuestionByGx",map);
	}
	
	/**
	 *@see 查询问卷的指标信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryTargetByQueId(Map<String, Object> map) {
		return selectList("queryTargetByQueId",map);
	}
	
	/**
	 *@see 新增个性等级信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveQuestionByQxLevel(Map<String, Object> map) {
		return update("saveQuestionByQxLevel",map);
	}

	/**
	 *@see 新增个性信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveQuestionByQx(Map<String, Object> map) {
		return update("saveQuestionByQx",map);
	}

	/**
	 *@see 获取个性题目编号
	 *@date 2015年11月4日 上午10:47:46
	 *@version 版本
	 *@author liboqi
	 */
	public Integer queryTargetMaxSeqByQueId(Map<String, Object> map) {
		return selectOne("queryTargetMaxSeqByQueId",map);
	}
	
	/**
	 *@see 查询问卷的指标等级信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryTargetLevelById(Map<String, Object> map) {
		return selectList("queryTargetLevelById",map);
	}
	
	/**
	 *@see 修改个性信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer updateQuestionByQx(Map<String, Object> map) {
		return update("updateQuestionByQx",map);
	}
	
	/**
	 *@see 修改个性信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer updateQuestionBySeq(Map<String, Object> map) {
		return update("updateQuestionBySeq",map);
	}
	
	/**
	 *@see 新增记录信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveRecord(Map<String, Object> map) {
		return update("saveRecord",map);
	}
	
	/**
	 *@see 查询记录信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordByUser(Map<String, Object> map) {
		return selectList("queryRecordByUser",map);
	}
	
	/**
	 *@see 新增记录详细信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveRecordDetailForTable(Map<String, Object> map) {
		return update("saveRecordDetailForTable",map);
	}
	
	/**
	 *@see 查询记录详细信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordDetailForTableByUser(Map<String, Object> map) {
		return selectList("queryRecordDetailForTableByUser",map);
	}
	
	/**
	 *@see 删除记录详细信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteRecordDetailForTable(Map<String, Object> map) {
		return update("deleteRecordDetailForTable",map);
	}
	
	/**
	 *@see 新增记录详细信息（客观题）
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveRecordForSpecific(Map<String, Object> map) {
		return update("saveRecordForSpecific",map);
	}
	
	/**
	 *@see 查询记录详细信息（客观题）
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordForSpecific(Map<String, Object> map) {
		return selectList("queryRecordForSpecific",map);
	}
	
	/**
	 *@see 删除记录详细信息（客观题）
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteRecordForSpecific(Map<String, Object> map) {
		return update("deleteRecordForSpecific",map);
	}
	
	/**
	 *@see 新增记录详细信息（主观题）
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveSubjectResultForSpecific(Map<String, Object> map) {
		return update("saveSubjectResultForSpecific",map);
	}
	
	/**
	 *@see 查询记录详细信息（主观题）
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> querySubjectResultForSpecific(Map<String, Object> map) {
		return selectList("querySubjectResultForSpecific",map);
	}
	
	/**
	 *@see 删除记录详细信息（主观题）
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteSubjectResultForSpecific(Map<String, Object> map) {
		return update("deleteSubjectResultForSpecific",map);
	}
	
	/**
	 *@see 统计客观题
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordForSpecificResult(Map<String, Object> map) {
		return selectList("queryRecordForSpecificResult",map);
	}
	
	/**
	 *@see 统计主观题
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> querySubjectForSpecificResult(Map<String, Object> map) {
		return selectList("querySubjectForSpecificResult",map);
	}
	
	/**
	 *@see 统计主观题行数
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> querySubjectResultMax(Map<String, Object> map) {
		return selectList("querySubjectResultMax",map);
	}
	
	/**
	 *@see 统计表格题目
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordTableResult(Map<String, Object> map) {
		return selectList("queryRecordTableResult",map);
	}
	
	/**
	 *@see 统计表格回答人数
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordTableResultAccountId(Map<String, Object> map) {
		return selectList("queryRecordTableResultAccountId",map);
	}
	
	/**
	 *@see 获取设置的对象
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryObInfo(Map<String, Object> map) {
		return selectList("queryObInfo",map);
	}
	
	/**
	 *@see 获取设置的对象
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryObInfoByList(Map<String, Object> map) {
		return selectList("queryObInfoByList",map);
	}
	
	/**
	 *@see 是否已经有人提交了问卷选项
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryRecordByList(Map<String, Object> map) {
		return selectList("queryRecordByList",map);
	}
	
	/**
	 *@see 删除调查对象
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteObjInfo(Map<String, Object> map) {
		return update("deleteObjInfo",map);
	}
	
	/**
	 *@see 新增调查对象
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer saveObjInfo(Map<String, Object> map) {
		return update("saveObjInfo",map);
	}
	
	/**
	 *@see 删除问卷调查
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteQuestionById(Map<String, Object> map) {
		return update("deleteQuestionById",map);
	}
	
	/**
	 *@see 删除记录信息
	 *@date 2015年11月3日 下午8:11:42
	 *@version 版本
	 *@author liboqi
	 */
	public Integer deleteRecordByQuestionId(Map<String, Object> map) {
		return update("deleteRecordByQuestionId",map);
	}
	
	public Collection<JSONObject> queryStatisticsResultAccToVoter(Map<String, Object> map){
		Integer objType = selectOne("queryObjTypeFromQnObj", map);
		if(objType == null){
			return new ArrayList<JSONObject>();
		}
		if(objType == 1){
			map.remove("classIds");
		}
		
		List<JSONObject> list = selectList("queryTargetlevel", map);
		if(list == null){
			list = new ArrayList<JSONObject>();
		}
		
		Map<String, Map<String, String>> targetMap = new HashMap<String, Map<String, String>>();
		for(JSONObject obj : list){
			String targetId = obj.getString("targetId");
			String levelId = obj.getString("levelId");
			String opt = obj.getString("targetLevelOpt");
			if(!targetMap.containsKey(targetId)){
				targetMap.put(targetId, new HashMap<String, String>());
			}
			Map<String, String> subMap = targetMap.get(targetId);
			subMap.put(levelId, opt);
		}
		
		Map<String, JSONObject> result = new HashMap<String, JSONObject>();
		Map<Long, Set<Long>> accId2UserIds = new HashMap<Long, Set<Long>>();
		Set<Long> classIds = new HashSet<Long>();
		
		list = selectList("queryObjResultAccToVoter", map);
		if(list == null){
			list = new ArrayList<JSONObject>();
		}
		for(JSONObject obj : list){
			Long accountId = obj.getLong("accountId");
			if(accountId == null){
				continue;
			}
			if(!accId2UserIds.containsKey(accountId)){
				accId2UserIds.put(accountId, new HashSet<Long>());
			}
			Long userId = obj.getLong("userId");
			if(objType == 3 && userId != null){
				accId2UserIds.get(accountId).add(userId);
			}
			
			String key = null;
			Long classId = obj.getLong("classId");
			if(objType == 1){
				key = String.valueOf(accountId);
			}else if(objType == 2) {
				key = new StringBuffer().append(accountId).append("_")
						.append(classId).toString();
				classIds.add(classId);
			}else if(objType == 3){
				key = new StringBuffer().append(accountId).append("_").append(userId).append("_")
						.append(classId).toString();
				classIds.add(classId);
			}else{
				continue;
			}
			
			if(!result.containsKey(key)){
				result.put(key, new JSONObject());
			}
			String targetId = obj.getString("targetId");
			String levelId = obj.getString("levelId");
			String content = "";
			if(targetMap.containsKey(targetId) && targetMap.get(targetId).containsKey(levelId)){
				content = targetMap.get(targetId).get(levelId);
			}
			JSONObject json = result.get(key);
			json.put("name", "");
			if(json.containsKey(targetId)) {	// 可能有多选
				content = json.getString(targetId) + "/" + content;
				json.put(targetId, content);
			} else {
				json.put(targetId, content);
			}
		}
		
		list = selectList("querySubjResultAccToVoter", map);
		if(list == null){
			list = new ArrayList<JSONObject>();
		}
		for(JSONObject obj : list){
			Long accountId = obj.getLong("accountId");
			if(accountId == null){
				continue;
			}
			if(!accId2UserIds.containsKey(accountId)){
				accId2UserIds.put(accountId, new HashSet<Long>());
			}
			Long userId = obj.getLong("userId");
			if(objType == 3 && userId != null){
				accId2UserIds.get(accountId).add(userId);
			}
			
			String key = null;
			Long classId = obj.getLong("classId");
			if(objType == 1){
				key = String.valueOf(accountId);
			}else if(objType == 2) {
				key = new StringBuffer().append(accountId).append("_")
						.append(classId).toString();
				classIds.add(classId);
			}else if(objType == 3){
				key = new StringBuffer().append(accountId).append("_").append(userId).append("_")
						.append(classId).toString();
				classIds.add(classId);
			}else{
				continue;
			}
			
			if(!result.containsKey(key)){
				result.put(key, new JSONObject());
			}
			String targetId = obj.getString("targetId");
			String content = obj.getString("content");
			JSONObject json = result.get(key);
			json.put("name", "");
			json.put(targetId, content);
		}
		
		Long schoolId = (Long) map.get("schoolId");
		String termInfoId = (String) map.get("termInfoId");
		// 批量获取账户信息
		List<Account> accounts = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(accId2UserIds.keySet()), termInfoId);
		Map<String, String> accId2Name = new HashMap<String, String>();
		if(CollectionUtils.isNotEmpty(accounts)){
			for(Account acc : accounts){
				long id = acc.getId();
				if(3 == objType){	// 家长
					List<User> users = acc.getUsers();
					if(CollectionUtils.isEmpty(users)){
						continue;
					}
					Set<Long> userIds = accId2UserIds.get(id);
					for(User u : users){
						if(T_Role.Parent.equals(u.getUserPart().getRole()) 
								&& userIds.contains(u.getParentPart().getId())){
							accId2Name.put(String.valueOf(u.getParentPart().getId()), u.getParentPart().getStudentName() + "家长");
						}
					}
				} else {	// 老师和学生
					accId2Name.put(String.valueOf(id), acc.getName());
				}
			}
		}
		
		Map<String, String> classId2Name = new HashMap<String, String>();
		if(objType != 1 && classIds.size() > 0){
			List<Classroom> crs = commonDataService.getClassroomBatch(schoolId, new ArrayList<Long>(classIds), termInfoId);
			if(CollectionUtils.isNotEmpty(crs)){
				for(Classroom cr : crs){
					long id = cr.getId();
					String className = cr.getClassName();
					classId2Name.put(String.valueOf(id), className);
				}
			}
		}
		
		for(Map.Entry<String, JSONObject> entry : result.entrySet()){
			String key = entry.getKey();
			JSONObject json = entry.getValue();
			if(objType == 1){
				json.put("name", accId2Name.get(key));
			}else if(objType == 2){
				String[] tmp = key.split("_");
				String name = new StringBuffer().append(accId2Name.get(tmp[0])).append("(").append(classId2Name.get(tmp[1]))
						.append(")").toString();
				json.put("name", name);
			}else if(objType == 3){
				String[] tmp = key.split("_");
				String name = new StringBuffer().append(accId2Name.get(tmp[1])).append("(").append(classId2Name.get(tmp[2]))
						.append(")").toString();
				json.put("name", name);
			}
		}
		
		return result.values();
	}

	@Override
	public Integer getRecordCnt(Map<String, Object> map) {
		return selectOne("getRecordCnt", map) ;
	}

	@Override
	public Integer getNoticeTime(Map<String, Object> map) {
		 
		return selectOne("getNoticeTime", map) ;
	}

	@Override
	public Integer updateNoticeTimes(Map<String, Object> map) {
	 
		return insert("updateNoticeTimes", map);
	}

	@Override
	public List<JSONObject> getRecorddetailfortableSubmit(List<String> list) {

		return selectList("getRecorddetailfortableSubmit" , list);
	}

	@Override
	public List<JSONObject> getRecordSubmited(Map<String, Object> map) {
		return selectList("getRecordSubmited" , map);
	}

	@Override
	public Integer updateSubmitStatus(Map<String, Object> map) {
	 
		return update("updateSubmitStatus", map);
	}

	@Override
	public List<JSONObject> getViewVoteDetail(Map<String, Object> map) {
		 
		return selectList("getViewVoteDetail" , map);
	}

	@Override
	public List<JSONObject> getSubjectResultByTargetId(Map<String, Object> map) {
		 
		return selectList("getSubjectResultByTargetId" , map);
	}
}
