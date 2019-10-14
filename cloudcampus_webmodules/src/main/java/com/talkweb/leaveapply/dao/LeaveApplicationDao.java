package com.talkweb.leaveapply.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
* @author  Administrator
* @version 创建时间：2017年2月7日 下午5:00:12 
* 程序的简单说明
*/
public interface LeaveApplicationDao {
 
	public JSONObject getLeaveApply(JSONObject param);
 

	public List<JSONObject> getAdminLeaveApplyList(JSONObject param);
	public List<JSONObject> getAuditLeaveApplyList(JSONObject param);
	public List<JSONObject> getLeaveApplyList(JSONObject param);
	
	public Integer  getAdminLeaveApplyListCnt(JSONObject param);
	public Integer  getAuditLeaveApplyListCnt(JSONObject param);
	public Integer  getLeaveApplyListCnt(JSONObject param);
	
	public int insertLeaveApplyRecord(JSONObject  param);
	public int updateLeaveApplyStatus(JSONObject  param);
	public int deleteLeaveApply(JSONObject param);

	public List<JSONObject> getLeaveApplyFileList(JSONObject param);
	public int insertLeaveApplyFile(JSONObject  param);
	public int deleteLeaveApplyFile(JSONObject param);
	public JSONObject getTeacherGroup(JSONObject param);
	public List<JSONObject> getGroupMemberList(JSONObject param);
	public List<JSONObject> getGroupMemberAndAuditdaysList(JSONObject param);
	public int insertGroupMember(JSONObject  param);
	public int delGroupMember(JSONObject  param);
	
	
	public int insertLeaveApplyGroup(JSONObject  param);
	public int deleteLeaveApplyGroup(JSONObject param);
	
	
	public List<JSONObject> getAuditdaysList(JSONObject param);
	public int insertAuditdays(JSONObject  param);
	public int deleteAuditdays(JSONObject param);
	public List<JSONObject> getAuditorList(JSONObject jsonObject); 
	
	public int insertAuditMember(JSONObject  param);
	public int deleteAuditMember(JSONObject param);
	
	
	public List<JSONObject> getLeaveApplyprocedureList(JSONObject param);
	public int insertLeaveApplyprocedure(JSONObject  param);
	public int updateLeaveApplyprocedure(JSONObject  param);
	public int updateLeaveApplyprocedureStatus(JSONObject  param);
	public int deleteLeaveApplyprocedure(JSONObject param);
	
	
	public List<JSONObject> getLeaveTypeList(JSONObject param);
	public List<JSONObject> getFestivalList(JSONObject param);
	public int deleteFestival(JSONObject param);
	public int insertFestivalList(List<JSONObject> param);
	
	
	
	public String getSelectedTeacherIds(JSONObject param);
	
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
	
	public List<JSONObject> getTeacherListByMoudleId(JSONObject param);
}
