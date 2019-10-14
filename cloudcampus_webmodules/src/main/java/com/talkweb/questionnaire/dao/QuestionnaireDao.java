package com.talkweb.questionnaire.dao;

import java.util.Collection;
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
public interface QuestionnaireDao {
	/**
	 * 在当前学年学期里是否有问卷调查数据
	 * @param termInfo
	 * @return
	 */
	boolean ifExistsDataInTermInfo(Map<String, Object> map);
	
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
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer createQuestionnaire(Map<String, Object> map);
	/**
	 *@see 编辑问卷调查名字
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionByName(Map<String, Object> map);
	
	/**
	 *@see 编辑问卷调查详细信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionnaireByDetail(Map<String, Object> map);
	
	/**
	 *@see 编辑问卷调查状态信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	Integer updateQuestionnaireStatus(Map<String, Object> map);
	
	/**
	 *@see 新增表格规则信息
	 *@date 2015年10月20日 下午4:03:17
	 *@version 版本
	 *@author liboqi
	 */
	 Integer createRuleForTable(Map<String, Object> map);
	 
	 /**
	  *@see 删除表格规则信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteRuleForTable(Map<String, Object> map);
	 
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
	 Integer saveQuestionByTab(Map<String, Object> map);
	 
	 /**
	  *@see 删除表格信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteQuestionByTab(Map<String, Object> map);
	 
	 /**
	  *@see 查询问卷的表格信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryOptionForTableById(Map<String, Object> map);
	 
	 /**
	  *@see 删除个性等级信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteQuestionByGxLevel(Map<String, Object> map);
	 
	 /**
	  *@see 删除个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteQuestionByGx(Map<String, Object> map);
	 
	 /**
	  *@see 查询问卷的指标信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryTargetByQueId(Map<String, Object> map);
	 
	 
	 /**
	  *@see 新增个性等级信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveQuestionByQxLevel(Map<String, Object> map);
	 
	 /**
	  *@see 新增个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveQuestionByQx(Map<String, Object> map);
	 
	 /**
	  *@see 新增个性信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer queryTargetMaxSeqByQueId(Map<String, Object> map);
	 
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
	 Integer updateQuestionByQx(Map<String, Object> map);
	 
	 /**
	  *@see 个性信息上移下移
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer updateQuestionBySeq(Map<String, Object> map);
	 
	 /**
	  *@see 新增记录信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveRecord(Map<String, Object> map);
	 
	 
	 /**
	  *@see 查询记录信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordByUser(Map<String, Object> map);
	 
	 /**
	  *@see 新增记录详细信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveRecordDetailForTable(Map<String, Object> map);
	 
	 /**
	  *@see 查询记录详细信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 List<JSONObject> queryRecordDetailForTableByUser(Map<String, Object> map);
	 
	 /**
	  *@see 删除记录详细信息
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteRecordDetailForTable(Map<String, Object> map);
	 
	 /**
	  *@see 新增记录详细信息（客观题）
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveRecordForSpecific(Map<String, Object> map);
	 
	 /**
	  *@see 新增记录详细信息(主观题)
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer saveSubjectResultForSpecific(Map<String, Object> map);
	 
	 /**
	  *@see 删除记录详细信息（客观题）
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteRecordForSpecific(Map<String, Object> map);
	 
	 /**
	  *@see 删除记录详细信息(主观题)
	  *@date 2015年10月20日 下午4:03:17
	  *@version 版本
	  *@author liboqi
	  */
	 Integer deleteSubjectResultForSpecific(Map<String, Object> map);
	 
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
	  * 按照投票人查询统计数据
	  * @param map
	  * @return
	  */
	 Collection<JSONObject> queryStatisticsResultAccToVoter(Map<String, Object> map);
	 
	 // 查看是否有人已经投票
	 Integer getRecordCnt(Map<String, Object> map);
	 
	 Integer getNoticeTime(Map<String, Object> map);
	 Integer updateNoticeTimes(Map<String, Object> map);
	 
	 List<JSONObject> getRecorddetailfortableSubmit(List<String> list);
	 List<JSONObject> getRecordSubmited(Map<String, Object> map);
	 
	 Integer updateSubmitStatus(Map<String, Object> map);
	 List<JSONObject> getViewVoteDetail(Map<String, Object> map);
	 List<JSONObject> getSubjectResultByTargetId(Map<String, Object> map);
}
