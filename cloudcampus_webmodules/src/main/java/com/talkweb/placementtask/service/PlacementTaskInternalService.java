package com.talkweb.placementtask.service;

import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TeachingClassInfo;

public interface PlacementTaskInternalService {
	List<JSONObject> getGradePlacementList(String usedGrade, Long schoolId, String termInfo);
	
	List<JSONObject> getSubjectOrGroupList(String placementId, Long schoolId, String termInfo);
	
	String getTeachingClassNameById(String placementId, Long schoolId, String termInfo, String teachingClassId);
	
	List<TeachingClassInfo> getTeachingClassInfoList(String placementId, Long schoolId, String termInfo);
	
	List<TeachingClassInfo> getTeachingClassInfoList(String placementId, Long schoolId, String termInfo, Collection<String> tClassIds);
	
	List<OpenClassInfo> queryAllPlacementData(String placementId, Long schoolId, String termInfo);
	
	List<StudentInfo> queryStudInfoListWaitForPlacement(String placementId, Long schoolId, String termInfo);
	
	List<JSONObject> getSubjectLevelList(String placementId, Long schoolId, String termInfo);

	/**
	 * 查询定二走一分班结果 
	 * @param placementId
	 * @param schId
	 * @param xnxq
	 * @return
	 */
	JSONObject queryDezyResultForSchedule(String placementId, long schId,
			String xnxq);

	/**
	 * 获取定二走一设置信息
	 * @param schoolId
	 * @param placementId
	 * @param gradeId
	 * @param xnxq
	 */
	TPlDezySettings getDezySettings(String schoolId, String placementId, String gradeId,
			String xnxq);
}
