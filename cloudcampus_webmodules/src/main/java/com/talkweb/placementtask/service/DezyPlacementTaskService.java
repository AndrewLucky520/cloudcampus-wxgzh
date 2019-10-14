package com.talkweb.placementtask.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface DezyPlacementTaskService {
	/**
	 * 1.1	获取选课志愿列表
	 * 	gradeId：使用年级
      	type:0单科  1组合  2所有
        schoolId:学校代码
	 */
	List<JSONObject> getWfListToThird(JSONObject obj);
	/**
	 * 1.3	分班前期设置
	 * @param obj
	 * @return
	 */
	int insertDezyPreSettings(JSONObject obj);
	
	/**
	 * 1.4	获取设置预览
	 * @param obj
	 * @return
	 */
	JSONObject getDezyPreSettings(JSONObject obj);	
	
	/**
	 * 获取行政班列表(用于入库的行政班Id由调用者自行生成)
	 * @param placementId
	 * @param schoolId
	 * @param gradeId
	 * @return	Map:{dezyClassGroup:TPlDezyClassgroup(实体),dezyClass:DezyClass(实体,只包含行政班部分),dezySubjectComp:TPlDezySubjectcomp(实体)},
	 */
	List<Map<String,Object>> getClassInfo(String placementId, String schoolId, String gradeId);	
	
	
	/**
	 * 1.4	获取分班结果预览
	 * @param obj
	 * @return
	 */
	List<JSONObject> getTclassPreview(JSONObject obj);
	
	/**
	 * 1.10	获取班级名称（行政班、教学班）
	 * @param obj
	 * @return
	 */
	List<JSONObject> getClassAll(JSONObject obj);
	
	/**
	 *1.8	修改班级名称(教学班、行政班)
	 * 		termInfoId：学年学期<br>
			placementId：分班代码<br>
			useGrade: 使用年级<br>
			subjectGroupId: 科目组代码（optional）<br>
			classGroupId：行政班组代码（optional）<br>
			data:<br>
			[{<br>
				tclassId:班级代码<br>
				tclassType：班级类型（6：行政班；7：走班教学班）<br>
				tclassName：班级名称<br>
			}]
	 * @param obj
	 * @return
	 */
	int modifyClassName(JSONObject obj);
	/**
	 * 1.9	还原班级名称
	 * @param obj
	 * @return
	 */
	int recoveryClassName(JSONObject obj);
	
	/**
	 * 1.6	获取分班结果查询科目
	 * @param obj
	 * @return
	 */
	JSONArray getQuerySubList(JSONObject obj);
	
	/**
	 * 1.2	获取志愿详情
	 */
	JSONObject getWishingDetail(JSONObject obj);
	
	/**
	 * 1.11	查看学生分班明细
	 */
	List<JSONObject> getStuInClassDetail(JSONObject obj);
	
	//>>>>>>>>>>>>>>>>>>>>>   大走班        <<<<<<<<<<<<<<<<<<<<<<<<<<
	int insertDzbPreSettings(JSONObject obj);
	JSONObject getDzbPreSettings(JSONObject obj);
	
	List<JSONObject> getDzbDivQueryParams(Map<String,Object> obj);
	List<JSONObject> getDzbDivResult(JSONObject obj);
	
	//获取大走班学生明细-查询参数
	List<JSONObject> getDzbStuQueryParams(JSONObject obj);
	
	//获取大走班学生分班明细
	List<JSONObject> getDzbStuClassInfoDetail(JSONObject obj);
	//获取大走班主表
	List<JSONObject> getDzbMainTable(JSONObject obj);
	
	//获取师资配比
	List<JSONObject> getDzbTeachingResource(JSONObject obj);

	
	void setDivProc(String placementId, String percentage, String msg,
			int code ,String... rsMsg);
	void setDivProc(String placementId, String percentage,
			String nextPercentage, String msg, int code, String... rsMsg);
	JSONObject getGenDezyClassProc(String placementId, Float currentProgress);
	
}
