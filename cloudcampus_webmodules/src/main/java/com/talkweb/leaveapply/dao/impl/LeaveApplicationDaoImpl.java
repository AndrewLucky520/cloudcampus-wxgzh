package com.talkweb.leaveapply.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.leaveapply.dao.LeaveApplicationDao;

/** 
* @author  Administrator
* @version 创建时间：2017年2月7日 下午5:04:21 
* 
*/

@Repository
public class LeaveApplicationDaoImpl extends MyBatisBaseDaoImpl  implements LeaveApplicationDao    {

	@Override
	public JSONObject getLeaveApply(JSONObject param) {
		return selectOne("getleaveApply", param);
	}

	@Override
	public List<JSONObject> getAdminLeaveApplyList(JSONObject param) {
		return selectList("getAdminLeaveApplyList", param);
	}
	
	@Override
	public List<JSONObject> getAuditLeaveApplyList(JSONObject param) {
		 
		return selectList("getAuditLeaveApplyList", param);
	}

	@Override
	public List<JSONObject> getLeaveApplyList(JSONObject param) {
		 
		return selectList("getLeaveApplyList", param);
	}
	
	@Override
	public int insertLeaveApplyRecord(JSONObject oneRecord) {
		 
		return insert("insertLeaveApplyRecord", oneRecord);
	}

	@Override
	public int deleteLeaveApply(JSONObject param) {
		
		  return delete("delLeaveApply", param);
 
	}

	@Override
	public List<JSONObject> getLeaveApplyFileList(JSONObject param) {
	 
		return selectList("getLeaveApplyFileList", param);
	}

	@Override
	public int insertLeaveApplyFile(JSONObject oneRecord) {
		
		return insert("insertleaveApplyfile", oneRecord);
	}

	@Override
	public int deleteLeaveApplyFile(JSONObject param) {
	 
		  return delete("delleaveApplyfile", param);
	}

	@Override
	public List<JSONObject> getGroupMemberList(JSONObject param) {
		
		return selectList("getGroupMemberList", param); 
	}
	
	@Override
	public List<JSONObject> getGroupMemberAndAuditdaysList(JSONObject param) {
		 
		return selectList("getGroupMemberAndAuditdaysList", param); 
	}

	@Override
	public int insertLeaveApplyGroup(JSONObject oneRecord) {

		return insert("insertGroupRecord", oneRecord);
		
	}

	@Override
	public int deleteLeaveApplyGroup(JSONObject param) {
		
	  return delete("delGroup", param) ;
		
	}

	@Override
	public List<JSONObject> getAuditdaysList(JSONObject param) {
		 
		return selectList("getAuditdaysList", param);
	}

	@Override
	public int insertAuditdays(JSONObject oneRecord) {
		
	  return insert("insertAuditdays", oneRecord); 
		
	}

	@Override
	public int deleteAuditdays(JSONObject param) {
		
		return  delete("deleteAuditdays", param);
	}

	@Override
	public List<JSONObject> getLeaveApplyprocedureList(JSONObject param) {
		 
		return selectList("getLeaveApplyprocedureList", param);
	}

	@Override
	public int insertLeaveApplyprocedure(JSONObject oneRecord) {
		
		return insert("insertLeaveApplyprocedure", oneRecord); 
	}
	
	@Override
	public int updateLeaveApplyprocedure(JSONObject oneRecord) {
		
		return update("updateLeaveApplyprocedure", oneRecord); 
	}
	
	@Override
	public int updateLeaveApplyprocedureStatus(JSONObject oneRecord) {
		
		return update("updateLeaveApplyprocedureStatus", oneRecord); 
	}

	@Override
	public int deleteLeaveApplyprocedure(JSONObject param) {
	 
		return delete("deleteLeaveApplyprocedure", param) ;
	}

	@Override
	public List<JSONObject> getLeaveTypeList(JSONObject param) {
		return selectList("getLeaveTypeList", param);
		 
	}

	@Override
	public List<JSONObject> getFestivalList(JSONObject param) {
		return selectList("getFestivalList", param);
	}

	@Override
	public JSONObject getTeacherGroup(JSONObject param) {
		 
		return selectOne("getTeacherGroup", param);
	}

	@Override
	public List<JSONObject> getAuditorList(JSONObject param) {
		 
		return selectList("getAuditorList", param);
	}

	@Override
	public int insertGroupMember(JSONObject param) {
		return insert("insertGroupMember", param); 
	}

	@Override
	public int delGroupMember(JSONObject param) {
		return delete("delGroupMember", param) ;
	}

	@Override
	public int insertAuditMember(JSONObject param) {
		 
		return insert("insertAuditMember", param); 
	}

	@Override
	public int deleteAuditMember(JSONObject param) {

		return delete("deleteAuditMember", param) ;
	}

	@Override
	public String getSelectedTeacherIds(JSONObject param) {
		 
		return selectOne("getLeaveSelectedTeacherIds" , param);
	}

	@Override
	public int updateLeaveApplyStatus(JSONObject param) {
		
		return update("updateLeaveApplyStatus", param);
	}

	@Override
	public Integer getAdminLeaveApplyListCnt(JSONObject param) {

		return selectOne("getAdminLeaveApplyListCnt" , param);
	}

	@Override
	public Integer getAuditLeaveApplyListCnt(JSONObject param) {
		 
		return selectOne("getAuditLeaveApplyListCnt" , param);
	}

	@Override
	public Integer getLeaveApplyListCnt(JSONObject param) {
		 
		return selectOne("getLeaveApplyListCnt" , param);
	}

	@Override
	public JSONObject getLeaveApplyprocedureLevelByTeacherId(JSONObject param) {
	 
		return  selectOne("getLeaveApplyprocedureLevelByTeacherId" , param);
	}

	@Override
	public List<JSONObject> getLeaveStatistics(JSONObject param) {
		 
		return selectList("getLeaveStatistics", param);
	}

	@Override
	public Integer getLeaveStatisticsCnt(JSONObject param) {
	 
		return selectOne("getLeaveStatisticsCnt", param);
	}

	@Override
	public int insertLeaveApplyTemp(JSONObject param) {
		 
		return insert("insertLeaveApplyTemp", param); 
	}

	@Override
	public int deleteLeaveApplyTemp(JSONObject param) {
		 
		return delete("deleteLeaveApplyTemp", param) ;
	}

	@Override
	public List<JSONObject> getLeaveApplyTempStatistics(JSONObject param) {
		 
		return selectList("getLeaveApplyTempStatistics", param);
	}

	@Override
	public int insertLeaveApplyProcedureMember(JSONObject param) {
		 
		return insert("insertLeaveApplyProcedureMember", param);
	}

	@Override
	public int deleteLeaveApplyProcedureMember(JSONObject param) {
		 
		return delete("deleteLeaveApplyProcedureMember", param) ;
	}

	@Override
	public List<JSONObject> getLeaveApplyProcedureMember(JSONObject param) {
	 
		return selectList("getLeaveApplyProcedureMember", param);
	}

	@Override
	public int deleteFestival(JSONObject param) {
		return delete("deleteFestival", param);
	}

	@Override
	public int insertFestivalList(List<JSONObject> param) {
		return insert("insertFestivalList", param);
	}

	@Override
	public List<JSONObject> getLeaveApplyPendingForTeacherZone(JSONObject param) {
		 
		return selectList("getLeaveApplyPendingForTeacherZone" , param);
	}

	@Override
	public JSONObject getLeaveApplyFinshedForTeacherZone(JSONObject param) {
		return  selectOne("getLeaveApplyFinshedForTeacherZone", param);
	}

	@Override
	public List<JSONObject> getLeaveApplyAuditorForTeacherZone(JSONObject param) {
	 
		return selectList("getLeaveApplyAuditorForTeacherZone", param);
	}

	@Override
	public List<JSONObject> getTeacherListByMoudleId(JSONObject param) {
		return selectList("getTeacherListByMoudleId", param);
	}
}
