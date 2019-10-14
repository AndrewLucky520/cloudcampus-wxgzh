package com.talkweb.wishFilling.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 志愿填报-设置DAO
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
public interface WishFillingSetDao {
	 //公共
	 List<JSONObject> getSubjectListByTb(JSONObject param);
	 int hasTbByUseGrades(JSONObject param);
	 List<JSONObject> getZhListByTb(JSONObject param);
	 List<JSONObject> getTbSelectList(JSONObject param);
	 JSONObject getTWfWfinfo(JSONObject param);
	 //管理员设置
	 List<JSONObject> getDicSubjectList(String sId,String areaCode,String pycc,String isDivided);
	 void createTb(JSONObject param);
	 List<JSONObject> getTbList(JSONObject param);
	 List<JSONObject> getByZhStudentNum(JSONObject param);
	 void updateTbName(JSONObject param);
	 JSONObject getTb(JSONObject param);
	 void deleteTb(JSONObject param);
	 void updateTb(JSONObject param);
	 void deleteSubjectTb(JSONObject param);
	 void createSubjectTbBatch(List<JSONObject> param);
	 void deleteStudentTb(JSONObject param);
	 void deleteByStudentTbByAccountId(JSONObject param);
	 void deleteZhSubject(JSONObject param);
	 void deleteZhStudent(JSONObject param);
	 void createZhSubjectBatch(List<JSONObject> param);
	 int getTotalStudentCount(JSONObject param);
	 int getByTotalStudentCount(JSONObject param);
	 List<JSONObject> getByZhStudentCount(JSONObject param);
	 int getStudentSubjectCount(JSONObject param);
	 int insertStudentTbBatch(List<JSONObject> param);
	 int insertByStudentTbBatch(List<JSONObject> param);
	 int insertStudentZhBatch(List<JSONObject> param);
	 List<JSONObject> getZhSubject(JSONObject param);
	 List<JSONObject> getStudentTb(JSONObject param);
	 List<JSONObject> getStudentTbBy(JSONObject param);
	 JSONObject getZhStudent(JSONObject param);
	 JSONObject getByZhStudent(JSONObject param);
	 void insertZhStudent(JSONObject param);
	 void insertByZhStudent(JSONObject param);
	 void updateByTb(JSONObject param);
	 int updateZhSubject(JSONObject param);
	 void insertByZhStudentBatch(JSONObject param);
	 List<JSONObject> getByStudentByFixedZhIds(JSONObject param);
	 void insertByStudentTbBatchForSet(JSONObject param);
	 void insertByStudentTbBatchForSetNoPerson(JSONObject param);
	 void deleteByStudentTb(JSONObject param);
	 void deleteByZhStudent(JSONObject param);
	 List<JSONObject> getTbNameList(JSONObject param);
	 List<JSONObject> getByAllZhStudent(JSONObject param);
	 int insertByStudentZhBatch(List<JSONObject> param);
	 //统计
	 List<Long> getStudentIds(JSONObject param);
	 List<Long> getByStudentIds(JSONObject param);
	 List<JSONObject> getStudentZhAndSubject(JSONObject param);
	 List<JSONObject> getByStudentZhAndSubject(JSONObject param);
	 void updateZhSubjectFixedHasNum(JSONObject param);
	 List<JSONObject> getZhStudentCount(JSONObject param);
	 List<Long> getHasSelectedStudentIds(JSONObject param);
	 List<Long> getByHasSelectedStudentIds(JSONObject param);
	 List<JSONObject> getStaticListBySubject(JSONObject param);
	 List<JSONObject> getByStaticListBySubject(JSONObject param);
	 List<JSONObject> getStaticListByZh(JSONObject param);
	 List<JSONObject> getByStaticListByZh(JSONObject param);
	 int getZhTotalStudentNum(JSONObject param);
	 int getByZhTotalStudentNum(JSONObject param);
	 List<JSONObject> getStudentZh(JSONObject param);
	 List<JSONObject> getByStudentZh(JSONObject param);
	 List<JSONObject> getAllStudentZh(JSONObject param);
	 List<JSONObject> getByAllStudentZh(JSONObject param);
	 //学生
	 List<String> getWfListByAccountId(JSONObject param);
	 List<String> getWfListByOpenTime(JSONObject param);
	 void updateByZhStudentBatch(JSONObject param);
	 void updateByStudentTbBatchForSet(JSONObject param);
	 void updateSubjectTbByBatch(JSONObject param);
	void updateSubjectTbNoByBatch(JSONObject param);
	List<JSONObject> getAllStudentZhNoGroupBy(JSONObject param);
	List<JSONObject> getByAllStudentZhNoGroupBy(JSONObject param);
	//APPH5
	List<JSONObject> getLastOpenTb(JSONObject param);
	//文件操作
	void insertFile(JSONObject param);
	void deleteFile(JSONObject param);
	JSONObject getFile(JSONObject param);
	JSONObject getFileById(JSONObject param);
	List<JSONObject> getDividedSubjectList(JSONObject param);
	
}
