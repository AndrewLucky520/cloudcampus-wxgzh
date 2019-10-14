package com.talkweb.questionnaire.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
/**
 * @ClassName QuestionnaireService.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:46:16
 */
public interface QuestionnaireService {
	public String queryTermInfo(JSONObject request);
	
	Integer queryObjTypeFromQnObj(Map<String, Object> map);
	/**
	 *@see 查询问卷
	 *@date 2015年10月20日 下午4:01:05
	 *@version 版本
	 *@author liboqi
	 */
	List<JSONObject> queryQuestionList(Map<String, Object> map);
	/**
	 *@see 新增问卷调查
	 *@date 2015年10月20日 下午4:01:05
	 *@version 版本
	 *@author liboqi
	 */
	Integer createQuestionnaire(Map<String, Object> map);
	/**
	 *@see 编辑问卷调查名字
	 *@date 2015年10月20日 下午4:01:05
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionByName(Map<String, Object> paramMap);
	/**
	 *@see 编辑问卷调查详细信息
	 *@date 2015年10月20日 下午4:01:05
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionnaireByDetail(Map<String, Object> paramMap);
	
	/**
	  *@see 查询问卷的详细信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	List<JSONObject> queryRuleForTableById(Map<String, Object> map);
	
	/**
	  *@see 保存表格信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	Integer saveQuestionByTab(Map<String, Object> map,String questionId);
	
	/**
	 *@see 保存表格信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	public List<JSONObject> queryOptionForTableById(Map<String, Object> map);
	
	/**
	  *@see 导入问卷表格信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	JSONObject importExcel(MultipartFile file,String questionId);
	
	/**
	  *@see 新增个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	public Integer saveQuestionByQx(Map<String, Object> map);
	
	/**
	  *@see 获取最大的个性题目序号
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	public Integer queryTargetMaxSeqByQueId(Map<String, Object> map);
	
	/**
	  *@see 查询问卷的指标信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryTargetByQueId(Map<String, Object> map);
	 
	 /**
	  *@see 查询问卷的指标等级信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryTargetLevelById(Map<String, Object> map);
	 
	 /**
	  *@see 修改个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 public Integer updateQuestionByQx(Map<String, Object> map);
	 
	 /**
	  *@see 个性信息上移下移
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 public Integer updateUpOrDownTargetSeq(JSONObject request);
	 
	 /**
	  *@see 删除个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteQuestionByGx(Map<String, Object> map);
	 
	 /**
	 *@see 编辑问卷调查状态信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionnaireStatus(Map<String, Object> map);
	
	 /**
	 *@see 保存用户填写的问卷（表格）
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer saveQuestionByUserDetails(Map<String, Object> paramMap);
	
	/**
	  *@see 查询记录信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordByUser(Map<String, Object> map);
	 
	 /**
	  *@see 查询记录详细信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordDetailForTableByUser(Map<String, Object> map);
	 /**
	 *@see 保存用户填写的问卷（个性）
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	 Integer saveQuestionByUserDetailsToGx(Map<String, Object> paramMap);
	 
	 /**
	  *@see 查询记录详细信息（客观题）
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordForSpecific(Map<String, Object> map);
	 
	 /**
	  *@see 查询记录详细信息(主观题)
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> querySubjectResultForSpecific(Map<String, Object> map);
	 
	 /**
	  *@see 统计客观题
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordForSpecificResult(Map<String, Object> map);
	 
	 /**
	  *@see 统计主观题
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> querySubjectForSpecificResult(Map<String, Object> map);
	 
	 /**
	  *@see 统计主观题行数
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> querySubjectResultMax(Map<String, Object> map);
	 
	 /**
	  *@see 统计表格题目
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordTableResult(Map<String, Object> map);
	 
	 /**
	  *@see 统计表格回答人数
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordTableResultAccountId(Map<String, Object> map);
	 
	 /**
	  *@see 获取设置的对象
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryObInfo(Map<String, Object> map);
	 
	 /**
	  *@see 获取设置的对象
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryObInfoByList(Map<String, Object> map);
	 
	 /**
	  *@see 是否已经有人提交了问卷选项
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordByList(Map<String, Object> map);
	 
	 /**
	  *@see 删除调查对象
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteObjInfo(Map<String, Object> map);
	 
	 /**
	  *@see 新增调查对象
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveObjInfo(Map<String, Object> map);
	 
	 /**
	  *@see 删除问卷调查
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteQuestionById(Map<String, Object> map);
	 
	 /**
	  *@see 删除记录信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteRecordByQuestionId(Map<String, Object> map);
	 
	 /**
	  *@see 按投票人查看统计信息
	  *@date 2016年8月10日 上午9:51:17
	  *@version 版本
	  *@author zouyang
	  */
	 JSONObject queryStatisticsResultAccToVoter(JSONObject request, long schoolId) throws Exception;
	 
	 JSONObject showResultUserNameByWcqk(JSONObject params);
	 // 复制问卷调查
	 Integer copyQuestionnaire(JSONObject params);
	 List<JSONObject> getRecentQuestionnaire(JSONObject params);
 
	 Map<String, List<Long>> getWXNoticePerson(JSONObject params);
	 
	 Integer getNoticeTime(Map<String, Object> map);
	 Integer updateNoticeTimes(Map<String, Object> map);
	 
	 /**
	  * 排除有记录的accountId
	  * @param schoolId
	  * @param questionId
	  * @param accountIds
	  * @return
	  */
	 List<Long> checkHasRecord(String schoolId,String questionId,Map<String,String> accountIds);
	 
	 List<JSONObject>  getViewVoteDetail(JSONObject params);
	 List<JSONObject> getSubjectResultByTargetId(Map<String, Object> map);
}
