package com.talkweb.placementtask.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.ExecProgressRedisMsg;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentClassInfo;
import com.talkweb.placementtask.domain.TPlDezyClass;

public interface PlacementTaskService {
	List<JSONObject> queryGradeList(JSONObject request);
	
	JSONObject queryWishFillingList(JSONObject request);
	
	List<JSONObject> queryScoreList(JSONObject request);
	
	List<JSONObject> queryPlacementSubjectList(JSONObject request);
	
	List<JSONObject> queryTeachingClassesList(JSONObject request);
	
	List<JSONObject> querySubjects(JSONObject request);
	
	List<PlacementTask> queryPlacementTaskList(JSONObject request);
	
	JSONObject queryPlacementTaskById(JSONObject request);
	
	void insertOrUpdatePlacementTask(JSONObject request);
	
	void deletePlacementTask(JSONObject request);
	
	void startExecProcess(JSONObject request, String redisKey) throws Exception;
	
	ExecProgressRedisMsg queryExecProgress(JSONObject request, String redisKey) throws Exception;
	
	/***************************** 微走班 ***********************************/
	
	JSONObject queryOpenClassInfoByWfIdMicro(JSONObject request);
	
	void saveOpenClassInfoMicro(JSONObject request);
	
	/***************************** 中走班 ***********************************/
	
	JSONObject queryZhDataMedium(JSONObject request);
	
	void saveZhDataMedium(JSONObject request);
	
	JSONObject queryZhOpenClassInfoMedium(JSONObject request);
	
	void saveZhOpenClassInfoMedium(JSONObject request);
	
	JSONObject queryRemainOpenClassInfoMedium(JSONObject request);
	
	void saveRemainOpenClassInfoMedium(JSONObject request);
	
	/******************************** 大走班 ************************************/
	
	List<JSONObject> queryLayerSetterInfoLarge(JSONObject request);
	
	void saveLayerSetterInfoLarge(JSONObject request);
	
	JSONObject queryLayerOpenClassInfoLarge(JSONObject request);
	
	void saveLayerOpenClassInfoLarge(JSONObject request);
	
	void deleteLayerOpenClassInfoLarge(JSONObject request);
	
	JSONObject queryWishSetterLarge(JSONObject request);
	
	void saveWishSetterLarge(JSONObject request);
	
	JSONObject queryWishOpenClassInfoLarge(JSONObject request);
	
	void saveWishOpenClassInfoLarge(JSONObject request);
	
	void deleteWishOpenClassInfo(JSONObject request);
	
	/**
	 * /大走班分班V4.0(长郡滨江版)
	 */
	JSONObject startLargeDivClass(JSONObject request);
	/********************************* 分班结果 ***************************************/
	JSONObject queryResultPreview(JSONObject request);
	
	void updateTeachingClassName(JSONObject request);
	
	JSONObject queryResultDetail(JSONObject request);
	
	JSONObject queryStudenInfoWaitForPlacement(JSONObject request);
	
	JSONObject queryStudenInfo(JSONObject request);
	
	void modifyStudenInfoToWaitForPlacement(JSONObject request);
	
	void modifyStudenInfoToPlacement(JSONObject request);
	void updateAllDezyResult(String schoolId ,String placementId,
			String  termInfo) throws Exception;
	
	List<String> getAllUsedWfId(String termInfo);
	
	/**
	 * 大走班和定二走一新版分班结果保存(2019.04)
	 * */
	void updateResult(String schoolId,String termInfo,String placementId,List<StudentClassInfo> studentClassInfos,List<TPlDezyClass> tPlDezyClasss,Map<String,List<String>> adminClassDeDistribute);
	
	/**
	 * 大走班和定二走一新版分班结果数据转化(2019.04)
	 * */
	
	void dataConversion(String schoolId,String termInfo,String usedGrade,String placementId,List<ClassInfo> classInfos);
	
	/**
	 * 大走班和定二走一分班算法中调用，对行政班、教学班安排场地信息(2019.04)
	 * 包括List<ClassInfo>中所有班级对象的场地id、场地名、班级名(班级名需要传进来)
	 * */
	List<ClassInfo>  arrangeClassGroud(String schoolId,String termInfo,String usedGrade,String placementId,List<ClassInfo> classInfos);
	
	/**
	 * 调班外层方法，需要先执行完updateWishPlacement再调用排课接口来更新排课的数据
	 * */
	void updateWish(String schoolId,String usedGrade,String wfId,List<JSONObject> studentWishs);
	
	
	
	/**
	 *  选科中修改志愿的时候调用接口
	 *  选科中先保存数据，保存完之后再调用这个接口修改分班中的数据(因为会用到选科表t_wf_studenttb计算人数，如果先调用的话人数会不准确)
	 *  大走班、定二走一、微走班
	 *  List<JSONObject> 中保存每个学生的信息，key为accountId对应学生的accountId，Key为wishId对应新志愿(格式为"4,5,6")
	 * */
	List<JSONObject> WishPlacement(String schoolId,String usedGrade,String wfId,List<JSONObject> studentWishs);
	
	/**
	 * 更新未分班学生班级信息(有志愿未分班的学生)
	 * 适用于大走班和定二走一
	 * */
	JSONObject insertNoDividedStuClassInfo(String schoolId,String termInfo,String placementId);
}
