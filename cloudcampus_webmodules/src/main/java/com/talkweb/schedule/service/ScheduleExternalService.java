package com.talkweb.schedule.service;

import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;

public interface ScheduleExternalService {

	/**
	 * 获取已改变排课位置的走班教学班Id列表
	 * @param schoolId 学校id
	 * @param placementId 分班id
	 * @return
	 */
	public List<String> getChangedTclassList(String schoolId, String placementId);
	
	/**
	 * 更新教学班人数和关联的学生数据（学生志愿调整）
	 * @param schoolId 学校id
	 * @param placementId 分班id
	 * @param placementType 分班类型
	 * @param termInfo 分班学年学期
	 * @param tClassIds 需要更新的教学班id列表
	 * @return
	 */
	public int updateTclass(String schoolId, String placementId, int placementType, 
			String termInfo, List<String> tClassIds);
	
	/**
	 * 通过课表id，教学班级id，获取学生信息
	 * 
	 * @param schoolId
	 * @param termInfoId
	 * @param gradeId
	 * @return [{<br>
	 *         scheduleId： 课表id<br>
	 *         scheduleName: 课表名称<br>
	 *         }]
	 */
	List<JSONObject> getScheduleForExam(Long schoolId, String termInfoId,
			String gradeId);
	
	/**
	 * 通过参数学校代码、使用年级、课表代码、学年学期获取课表所拥有的科目数据
	 * [
	 *	    {
	 *	        "subjectId":"科目代码，long类型",
	 *	        "subjectName":"科目名称",
	 *	        "subjectType":[
	 *	            {
	 *	                "typeText":"科目类型名称（选考/学考/A层/B层/C层）",
	 *	                "subjectLevel":"科目类型等级（31/32/41/42/43）"
	 *	            }
	 *	        ]
	 *	    }
	 *	]
	 * @param scheduleId
	 * @param schoolId
	 * @param termInfoId
	 * @param usedGrade
	 * @return
	 */
	List<JSONObject> getScheduleSubjectForExam(String scheduleId,
			Long schoolId, String termInfoId, String usedGrade);

	/**
	 * 通过参数获取教学班级信息，教学班级所上科目以及班级信息里面带有学生id
	 * @param scheduleId
	 * @param schoolId
	 * @param termInfoId
	 * @param usedGrade
	 * @param tclassIds
	 * @return
	 */
	List<TSchTClassInfoExternal> getTClassInfoExternal(String scheduleId,
			Long schoolId, String termInfoId, String usedGrade,
			Collection<String> tclassIds);

	/**
	 * 通过参数获取教学班级信息，教学班级所上科目，但此接口班级信息里面不带有学生Id
	 * @param scheduleId
	 * @param schoolId
	 * @param termInfoId
	 * @param usedGrade
	 * @param tclassIds
	 * @return
	 */
	List<TSchTClassInfoExternal> getTClassInfoExternalNoAccount(
			String scheduleId, Long schoolId, String termInfoId,
			String usedGrade, Collection<String> tclassIds);
}
