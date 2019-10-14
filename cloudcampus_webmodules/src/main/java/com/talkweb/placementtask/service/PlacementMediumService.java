package com.talkweb.placementtask.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumFixedClassInfo;

public interface PlacementMediumService {
	/**
	 * 中走班获取成绩下拉
	 * */
	JSONObject newqueryScoreList(String schoolId,String termInfo,String placementId);
	
	/**
	 * 中走班根据下拉的选科获取组合数据
	 * */
	List<JSONObject> QueryZhInfoByWfId(String schoolId,String termInfo,String placementId,String wfId);
	
	/**
	 * 中走班获取其他分班设置信息
	 * */
	
	JSONObject GetMediumPreSetting(String schoolId,String termInfo,String placementId,String wfId);
	
	/**
	 * 中走班分班设置
	 * */
	void SetMediumPreSetting(String schoolId,String termInfo,String placementId,String examTermInfo,String wfId,
			String examId,Integer ruleCode,Integer maxClassNum,Integer gradeSumLesson,Integer fixedSumLesson,
			List<JSONObject> zhSetList,List<JSONObject> classlevelList,List<JSONObject> subjectSetList);
	
	/**
	 * 中走班预览中科目下拉
	 * */
	JSONObject GetQuerySubList(String schoolId,String termInfo,String placementId,String usedGrade);
	
	/**
	 * 分班完之后调用接口安排场地以及保存结果
	 * */
	void UpdateMediumResult(String schoolId,String termInfo,String placementId,
			List<MediumFixedClassInfo> mediumFixedClassInfoList,List<ClassInfo> walkClassInfoList);
}
