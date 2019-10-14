package com.talkweb.placementtask.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementRule;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezySubjectgroup;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TPlMediumClasslevel;
import com.talkweb.placementtask.domain.TPlMediumSettings;
import com.talkweb.placementtask.domain.TPlMediumSubjectSet;
import com.talkweb.placementtask.domain.TPlMediumZhSet;
import com.talkweb.placementtask.domain.TeachingClassInfo;

public interface PlacementTaskDao {
	List<PlacementTask> queryPlacementTaskList(Map<String, Object> map);
	
	PlacementTask queryPlacementTaskById(Map<String, Object> map);
	
	boolean ifExistsSamePlacementName(Map<String, Object> map);
	
	int insertOrUpdatePlacementTask(PlacementTask placementTask);
	
	int updatePlacementTaskById(PlacementTask placementTask);
	
	int deletePlacementTask(Map<String, Object> map);
	
	List<OpenClassTask> queryOpenClassTasksWithInfo(Map<String, Object> map);
	
	List<OpenClassInfo> queryOpenClassInfosWithTask(Map<String, Object> map);
	
	List<OpenClassInfo> queryOpenClassInfo(Map<String, Object> map);
	
	List<OpenClassTask> queryOpenClassTask(Map<String, Object> map);
	
	int deleteOpenClassInfo(Map<String, Object> map);
	
	int deleteOpenClassTask(Map<String, Object> map);
	
	int insertOpenClassInfo(OpenClassInfo openClassInfos);
	
	int insertOpenClassInfoBatch(List<OpenClassInfo> openClassInfos, String termInfo);
	
	int insertOpenClassTask(OpenClassTask openClassTask);
	
	int insertOpenClassTaskBatch(List<OpenClassTask> openClassTasks, String termInfo);
	
	int updateOpenClassInfo(OpenClassInfo openClassInfo);
	
	int updateOpenClassTask(OpenClassTask openClassTask);
	
	List<PlacementRule> queryRuleInfo(Map<String, Object> map);
	
	int insertPlacementRule(PlacementRule placementRule);
	
	int deletePlacementRule(Map<String, Object> map);
	
	List<TeachingClassInfo> queryTeachingClassInfo(Map<String, Object> map);
	
	List<TeachingClassInfo> queryTeachingClassInfoWithAll(Map<String, Object> map);
	
	Boolean ifExistsSameTClassName(Map<String, Object> map);
	
	int updateTeachingClassInfo(Map<String, Object> map);
	
	int insertTeachingClassInfoBatch(List<TeachingClassInfo> teachingClassInfos, String termInfo);
	
	int deleteTeachingClassInfo(Map<String, Object> map);
	
	List<StudentInfo> queryStudentInfo(Map<String, Object> map);
	
	List<StudentInfo> queryStudentInfoWithAll(Map<String, Object> map);
	
	int insertStudentInfoBatch(List<StudentInfo> studentInfos, String termInfo);
	
	int deleteStudentInfo(Map<String, Object> map);
	
	List<Float> validateScoreLayerLarge(Map<String, Object> map);
	
	List<StudentInfo> queryStudentInfoWaitForPlacement(Map<String, Object> map);
	
	List<StudentInfo> queryStudentInfoWaitForPlacementWithAll(Map<String, Object> map);
	
	int insertStudentInfoWaitForPlacementBatch(List<StudentInfo> studentInfos, String termInfo);
	
	int deleteStudentInfoWaitForPlacement(Map<String, Object> map);
	
	
	
	Set<String> getPlacementTaskSchemas(String prefixSchema);
	
	List<JSONObject> getPlacementTaskDropDownList(Map<String, Object> map);
	/**
	 * 定二走一设置参数
	 * @param map
	 * @return
	 */
	TPlDezySettings getTPlDezySet(Map<String, Object> map);
	/**
	 * //定二走一 科目组
	 * @param map
	 * @return
	 */
	List<TPlDezySubjectgroup> getSubjectGroupList(Map<String, Object> map);
	/**
	 * 定二走一 科目设置
	 * @param map
	 * @return
	 */
	List<TPlDezySubjectSet> getSubjectSetList(Map<String, Object> map);
	
	/**
	 * 定二走一 科目设置(取科目组id)
	 * @param map
	 * @return
	 */
	List<String> getSubjectGroupIDList(Map<String, Object> map);
	/**
	 * 一 行政班组列表
	 * @param map
	 * @return
	 */
	List<TPlDezyClassgroup> getDezyClassGroupList(Map<String, Object> map);
	/**
	 * 班级列表
	 * @param map
	 * @return
	 */
	List<TPlDezyClass> getDezyClassList(Map<String, Object> map);
	/**
	 * 科目组合(志愿组合)
	 * @param map
	 * @return
	 */
	List<TPlDezySubjectcomp> getSubjectcompList(Map<String, Object> map);
	/**
	 * 科目组合(志愿组合)下的学生
	 * @param map
	 * @return
	 */
	List<TPlDezySubjectcompStudent> getSubjectcompStuList(Map<String, Object> map);
	/**
	 * 班级构成(志愿组合)
	 * @param map
	 * @return
	 */
	List<TPlDezyTclassSubcomp> getTclassSubcompList(Map<String, Object> map);
	/**
	 * 教学班与行政班关联关系
	 * @param map
	 * @return
	 */
	List<TPlDezyTclassfrom> getTclassFromList(Map<String, Object> map);
	void updateDezyResult(HashMap<String,Object> cxmap) throws Exception;
	
	/**
	 * 更新分班状态
	 * @param map
	 * @return
	 */
	int updateDivProc(Map<String,Object> map);

	List<TPlConfIndexSubs> getConfIndexSubsList(Map<String, Object> map);

	List<String> selectUsedWfId(Map<String,Object> map);
	
	/**
	 * 通过学生id获取学生对应志愿信息(补选)
	 * */
	List<JSONObject> getByZhStudentToThirdByAccountId(Map<String,String> map);
	/**
	 * 通过学生id获取学生对应志愿信息
	 * */
	List<JSONObject> getZhStudentToThirdByAccountId(Map<String,String> map);
	
	/**
	 * 根据schoolId和wfId查询选科类型(是否补选)
	 * */
	String getWfInfo(Map<String,Object> map);
	
	/**
	 * 获取accoutId对应的class信息
	 * */
	List<JSONObject> getStudentClassInfo(Map<String,Object> map);
	/**
	 * 获取班级信息id、className
	 * */
	List<JSONObject> getOldClassInfo(Map<String,Object> map);
	/**
	 * 根据schoolId、wfId从t_pl_dezy_settings中获取本学期使用了这次选科数据的placementId(定二走一和大走班用)
	 * */
	List<String> getplacementInfoByWfid(Map<String,Object> map);
	
	/**
	 * 获取指定学生的subjectCompId(科目分组代码)、compFrom(科目组合)以及行政班、教学班等相关信息
	 * */
	List<JSONObject> getSubjectcompInfoByAccountId(Map<String,Object> map);
	
	/**
	 * 获取指定班级id的班级详情
	 * */
	List<TPlDezyClass> getDezyClassListByClassIds(Map<String,Object> map);
	
	/**
	 * 更新班级表t_pl_dezy_class人数信息
	 * */
	int updateTPlDezyClassNum(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_class表中指定数据
	 * */
	int deleteTPlDezyClass(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_tclassfrom表中指定教学班数据
	 * */
	int deleteTPlDezyTclassfrom(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_tclassfrom表中指定行政班数据
	 * */
	int deleteTPlDezyclassfrom(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_subjectcomp表中指定数据
	 * */
	int deleteTPlDezySubjectcomp(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_tclass_subcomp表中指定数据
	 * */
	int deleteTPlDezyTclassSubcomp(Map<String,Object> map);
	
	/**
	 * 更新t_pl_dezy_subjectcomp表中的人数信息
	 * */
	int updateTPlDezySubjectcomp(Map<String,Object> map);
	
	/**
	 * 删除t_pl_dezy_subjectcomp_student表中对应数据
	 * */
	int deleteTPlDezySubjectcompStudentById(Map<String,Object> map);
	
	/**
	 * 获取当前学年学期
	 * */
	String getCurrenTermInfo();
	
	/**
	 * 从选科数据中统计每个科目选考人数
	 * */
	List<JSONObject> countIdByWfid(Map<String,Object> map);
	
	/**
	 * 从补选选科数据中统计每个科目选考人数
	 * */
	List<JSONObject> countByIdByWfid(Map<String,Object> map);
	
	/**
	 * 更新t_pl_dezy_subject_set表中学选考人数信息
	 * */
	void updateTPlDezySubjectSet(Map<String,Object> map);
	
	/**
	 * 获取指定学生的openClassInfo、openClassTask、subjectIdsStr、numOfStuds、classId信息
	 * */
	JSONObject getOpenClassByAccountId(Map<String,Object> map);
	
	/**
	 * 删除t_pl_openclasstask中指定数据
	 * */
	int deleteTPlOpenclasstask(Map<String,Object> map);
	
	/**
	 * 删除t_pl_openclassinfo中指定数据
	 * */
	int deleteTPlOpenclassinfo(Map<String,Object> map);
	
	/**
	 * 更新t_pl_openclasstask表中指定数据的numOfStuds
	 * */
	int updateTPlOpenclasstask(Map<String,Object> map);
	
	/**
	 * 删除t_pl_teachingclassinfo中指定数据
	 * */
	int deleteTPlTeachingclassinfo(Map<String,Object> map);

	/**
	 * 删除t_pl_studentinfo中指定数据
	 * */
	int deleteTPlStudentinfo(Map<String,Object> map);
	
	/**
	 * 更新t_pl_teachingclassinfo中人数信息
	 * */
	int updateTPlTeachingclassinfo(Map<String,Object> map);
	
	/**
	 * 查询学生性别gender
	 * */
	String queryGender(Map<String,Object> map);
	
	/**
	 * 根据wfid从t_pl_placementtask查询数据(微走班、中走班用)
	 * */
	List<String> getplacementIdByWfid(Map<String,Object> map);
	
	/**
	 * 根据志愿id关联t_pl_openclassinfo和t_pl_openclasstask表，查询openClassTaskId信息
	 * */
	String getopenClassTaskId(Map<String,Object> map);
	
	/**
	 * 删除t_pl_studentinfowaitforplacement表中对应数据
	 * */
	void deleteTPlStudentinfowaitforplacement(Map<String,Object> map);
	
	/**
	 * t_pl_studentinfowaitforplacement中插入数据
	 * */
	int insertTPlStudentinfowaitforplacement(Map<String,Object> map);
	
	/**
	 * 查询定二走一方式分班里面行政班和教学班的定二关系
	 * */
	List<JSONObject> getClassToTClassRelation(Map<String,Object> map);
	
	/**
	 * 根据subjectCompId查询对应的行政、教学班信息
	 * */
	List<TPlDezyClass> getTPlDezyClassBySubjectCompId(Map<String,Object> map);
	
	/**
	 * 中走班中，根据学年学期、学校id、分班id获取t_pl_medium_settings(中走班基本设置表)中的数据
	 * */
	List<TPlMediumSettings> getTPlMediumSettings(Map<String,Object> map);
	
	/**
	 * 中走班中，根据学年学期、学校id、分班id获取t_pl_medium_zh_set(中走班组合的固定班级以及人数设置)中的数据
	 * */
	List<TPlMediumZhSet> getTPlMediumZhSet(Map<String,Object> map);
	
	/**
	 * 中走班中，根据学年学期、学校id、分班id获取t_pl_medium_classlevel(走班分层设置表)中的数据
	 * */
	List<TPlMediumClasslevel> getTPlMediumClasslevel(Map<String,Object> map);
	
	/**
	 * 中走班中，根据学年学期、学校id、分班id获取t_pl_medium_subject_set(走班科目学时设置)中的数据
	 * */
	List<TPlMediumSubjectSet> getTPlMediumSubjectSet(Map<String,Object> map);
	
	/**
	 * 删除t_pl_medium_zh_set表中的数据
	 * */
	int deleteTPlMediumZhSet(Map<String,Object> map);
	
	/**
	 * 新增t_pl_medium_zh_set表中的数据
	 * */
	int insertTPlMediumZhSet(String termInfo, List<TPlMediumZhSet> list);
	
	/**
	 * 更新t_pl_medium_settings表中的数据
	 * */
	int updateTPlMediumSettings(TPlMediumSettings TtPlMediumSettings);
	
	/**
	 * 删除t_pl_medium_classlevel表中的数据
	 * */
	int deleteTPlMediumClasslevel(Map<String,Object> map);

	/**
	 * 新增t_pl_medium_classlevel表中的数据
	 * */
	int insertTPlMediumClasslevel(String termInfo, List<TPlMediumClasslevel> list);
	
	/**
	 * 删除t_pl_medium_subject_set表中的数据
	 * */
	int deleteTPlMediumSubjectSet(Map<String,Object> map);
	
	/**
	 * 新增t_pl_medium_subject_set表中的数据
	 * */
	int insertTPlMediumSubjectSet(String termInfo, List<TPlMediumSubjectSet> list);
	/**
	 * 新增t_pl_medium_settings表中的数据
	 * */
	int insertTPlMediumSettings(TPlMediumSettings TtPlMediumSettings);
	
}
