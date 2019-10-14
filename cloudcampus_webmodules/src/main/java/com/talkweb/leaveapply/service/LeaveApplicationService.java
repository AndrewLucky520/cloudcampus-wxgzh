package com.talkweb.leaveapply.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
* @author  Administrator
* @qq: 295218658 
* @version 创建时间：2017年2月7日 下午5:00:02 
* 程序的简单说明
*/
public interface LeaveApplicationService {
 
	
	public JSONObject getLeaveApply(JSONObject jsonObject);  
	public List<JSONObject> getAdminLeaveApplyList(JSONObject jsonObject); 
	public Integer getAdminLeaveApplyListCnt(JSONObject jsonObject);
	public List<JSONObject> getAuditLeaveApplyList(JSONObject jsonObject); 
	public Integer getAuditLeaveApplyListCnt(JSONObject jsonObject);
	public List<JSONObject> getLeaveApplyList(JSONObject jsonObject); 
	public Integer getLeaveApplyListCnt(JSONObject jsonObject);
	
	public int insertLeaveApply(JSONObject jsonObject);
	public int updateLeaveApplyStatus(JSONObject jsonObject);
	public int delLeaveApplyRecord(JSONObject jsonObject);
	
	public List<JSONObject> getLeaveApplyFileList(JSONObject jsonObject);  
	public int updateLeaveApplyFile(JSONObject jsonObject);
	public int delLeaveApplyFile(JSONObject jsonObject);
   
	public List<JSONObject> getGroupMemberList(JSONObject jsonObject);  
	public List<JSONObject> getGroupMemberAndAuditdaysList(JSONObject jsonObject);  
	public JSONObject getTeacherGroup(JSONObject jsonObject);
	public int updateGroupMember(JSONObject jsonObject);
	public int delGroupMember(JSONObject jsonObject);
	
	public List<JSONObject> getAuditdaysList(JSONObject jsonObject); 
	public int updateAuditdays(JSONObject jsonObject);
	public int delAuditdays(JSONObject jsonObject);
	public int deleteAuditMember(JSONObject jsonObject);
	public List<JSONObject> getAuditorList(JSONObject jsonObject);  
	
	public List<JSONObject> getProcedureList(JSONObject jsonObject); 
	public int insertProcedure(JSONObject jsonObject);
	public int updateProcedure(JSONObject jsonObject);
	public int updateProcedureStatus(JSONObject jsonObject);
	public int delProcedure(JSONObject jsonObject);
	
	public List<JSONObject> getLeaveTypeList(JSONObject jsonObject); 
	public List<JSONObject> getFestivalList(JSONObject jsonObject);
	public int deleteFestival(JSONObject param);
	public int insertFestivalList(List<JSONObject> param);

	public String getSelectedTeacherIds(JSONObject jsonObject);
	public List<JSONObject> getAllTeacherList(JSONObject param);
	
	public JSONObject getLeaveApplyprocedureLevelByTeacherId(JSONObject param);
	public List<JSONObject> getLeaveStatistics(JSONObject param);
	public Integer getLeaveStatisticsCnt(JSONObject param);
	
	
	public int insertLeaveApplyTemp(JSONObject  param);
	public int deleteLeaveApplyTemp(JSONObject param);
	public List<JSONObject> getLeaveApplyTempStatistics(JSONObject param);
 
	public int insertLeaveApplyProcedureMember(JSONObject  param);
	public int deleteLeaveApplyProcedureMember(JSONObject param);
	public List<JSONObject> getLeaveApplyProcedureMember(JSONObject param);
	
	public List<JSONObject> getLeaveApplyPendingForTeacherZone(JSONObject param) ;
	public JSONObject getLeaveApplyFinshedForTeacherZone(JSONObject param) ;
	public List<JSONObject> getLeaveApplyAuditorForTeacherZone(JSONObject param) ;
	
	public boolean isMoudleManager(String moulderId,String teacherUserId);
}
