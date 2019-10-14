package com.talkweb.leaveapply.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.leaveapply.dao.LeaveApplicationDao;
import com.talkweb.leaveapply.service.LeaveApplicationService;

/** 
* @author  Administrator
* @version 创建时间：2017年2月7日 下午5:09:48 
* 程序的简单说明
*/
@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService{

	@Autowired
	private LeaveApplicationDao leaveApplicationDao;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Override
	public JSONObject getLeaveApply(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getLeaveApply(jsonObject);
	}
	
	
	@Override
	public List<JSONObject> getAdminLeaveApplyList(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getAdminLeaveApplyList(jsonObject);
	}


	@Override
	public List<JSONObject> getAuditLeaveApplyList(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getAuditLeaveApplyList(jsonObject);
	}
	
	@Override
	public List<JSONObject> getLeaveApplyList(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getLeaveApplyList(jsonObject);
	}
	
	@Override
	public int insertLeaveApply(JSONObject param) {
		 
		return leaveApplicationDao.insertLeaveApplyRecord(param);
	}
 
	@Override
	public int updateLeaveApplyStatus(JSONObject jsonObject) {
		
		return leaveApplicationDao.updateLeaveApplyStatus(jsonObject);
	}

	@Override
	public int delLeaveApplyRecord(JSONObject jsonObject) {
		
		return leaveApplicationDao.deleteLeaveApply(jsonObject);
	}

	
	@Override
	public List<JSONObject> getGroupMemberList(JSONObject jsonObject) {
		
		return leaveApplicationDao.getGroupMemberList(jsonObject);
	}


	@Override
	public List<JSONObject> getGroupMemberAndAuditdaysList(JSONObject jsonObject) {
	   
		return leaveApplicationDao.getGroupMemberAndAuditdaysList(jsonObject);
	}
	
	@Override
	public int updateGroupMember(JSONObject jsonObject) {
	    
		leaveApplicationDao.insertLeaveApplyGroup(jsonObject);
		leaveApplicationDao.delGroupMember(jsonObject);
		JSONArray array = jsonObject.getJSONArray("members");
		 if (array!=null && array.size() > 0 ) {
			 for (int i = 0; i < array.size(); i++) {
				 jsonObject.put("teacherId", array.get(i));
				 leaveApplicationDao.insertGroupMember(jsonObject);
			 }
		 } 
		 
		
		return 0;
	}


	@Override
	public int delGroupMember(JSONObject jsonObject) {
		leaveApplicationDao.deleteLeaveApplyGroup(jsonObject); 
		leaveApplicationDao.delGroupMember(jsonObject);
		return 0;
	}


	@Override
	public int updateAuditdays(JSONObject jsonObject) {
		 String schoolId = jsonObject.getString("schoolId");
		 String groupId = jsonObject.getString("groupId");
		 leaveApplicationDao.deleteAuditdays(jsonObject);
		 leaveApplicationDao.deleteAuditMember(jsonObject);
		 JSONArray jsonArray = jsonObject.getJSONArray("auditflows");
		 if (jsonArray!=null) {
			 for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject object =  jsonArray.getJSONObject(i) ;
					JSONObject parm = new JSONObject();
					parm.put("daysBegin", object.getFloat("daysBegin"));
					if (StringUtils.isNotBlank(object.getString("daysEnd"))) {
						parm.put("daysEnd", object.getFloat("daysEnd"));
					}
					parm.put("schoolId", schoolId);
					parm.put("groupId", groupId);
					leaveApplicationDao.insertAuditdays(parm);
					JSONArray auditorLevel = object.getJSONArray("auditorLevel");
					if (auditorLevel!=null) {
						for (int j = 0; j < auditorLevel.size(); j++) {
							JSONObject obj =  auditorLevel.getJSONObject(j) ;
							//String levelNum = obj.getString("levelNum");
							parm.put("levelNum", (j +1));
							JSONArray auditors = obj.getJSONArray("auditors");
							if (auditors!=null) {
								for (int k = 0; k < auditors.size(); k++) {
									JSONObject obj2 =  auditors.getJSONObject(k) ;
									String teacherId = obj2.getString("teacherId");
									parm.put("teacherId", teacherId);
									leaveApplicationDao.insertAuditMember(parm);
								}
							}
							
						}
					}
					
		      }
		 }
 
		 return 0;
	}


	@Override
	public int delAuditdays(JSONObject jsonObject) {
		
		return leaveApplicationDao.deleteAuditdays(jsonObject); 
	}


	@Override
	public int insertProcedure(JSONObject jsonObject) {
		
		return leaveApplicationDao.insertLeaveApplyprocedure(jsonObject);
	}

	
	@Override
	public int updateProcedure(JSONObject jsonObject) {
		
		return leaveApplicationDao.updateLeaveApplyprocedure(jsonObject);
	}
	
	@Override
	public int updateProcedureStatus(JSONObject jsonObject) {
		
		return leaveApplicationDao.updateLeaveApplyprocedureStatus(jsonObject);
	}

	@Override
	public int delProcedure(JSONObject jsonObject) {
		
		return leaveApplicationDao.deleteLeaveApplyprocedure(jsonObject);
	}


	@Override
	public int updateLeaveApplyFile(JSONObject jsonObject) {
		leaveApplicationDao.insertLeaveApplyFile(jsonObject);
		return 0;
	}


	@Override
	public int delLeaveApplyFile(JSONObject jsonObject) {
		leaveApplicationDao.deleteLeaveApplyFile(jsonObject);
		return 0;
	}


	@Override
	public List<JSONObject> getAuditdaysList(JSONObject jsonObject) {
		
		return leaveApplicationDao.getAuditdaysList(jsonObject);
	}





	@Override
	public List<JSONObject> getProcedureList(JSONObject jsonObject) {
		
		return leaveApplicationDao.getLeaveApplyprocedureList(jsonObject);
	}


	@Override
	public List<JSONObject> getLeaveApplyFileList(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getLeaveApplyFileList(jsonObject);
	}


	@Override
	public List<JSONObject> getLeaveTypeList(JSONObject jsonObject) {
		return leaveApplicationDao.getLeaveTypeList(jsonObject);
	}


	@Override
	public List<JSONObject> getFestivalList(JSONObject jsonObject) {
		return leaveApplicationDao.getFestivalList(jsonObject);
	}


	@Override
	public JSONObject getTeacherGroup(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getTeacherGroup(jsonObject);
	}


	@Override
	public List<JSONObject> getAuditorList(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getAuditorList(jsonObject);
	}


	@Override
	public String getSelectedTeacherIds(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getSelectedTeacherIds(jsonObject);
	}


	@Override
	public List<JSONObject> getAllTeacherList(JSONObject param) {
		List<JSONObject> rList = new ArrayList<JSONObject>();
		School school = (School) param.get("school");
		String selectedSemester = param.getString("selectedSemester");
		String teacherName = StringUtils.isEmpty(param.getString("teacherName"))?"": param.getString("teacherName").trim() ; 
		List<Account> tList = commonDataService.getAllSchoolEmployees(school, selectedSemester, teacherName); 
		for (Account account : tList) {
			if(!account.getName().isEmpty()){//过滤特殊数据
			JSONObject line = new JSONObject();
			line.put("teacherId", account.getId());
			line.put("teacherName", account.getName());
			rList.add(line);
			}
		}
		return rList;
	}


	@Override
	public int deleteAuditMember(JSONObject jsonObject) {
		return leaveApplicationDao.deleteAuditMember(jsonObject);
	}


	@Override
	public Integer getAdminLeaveApplyListCnt(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getAdminLeaveApplyListCnt(jsonObject);
	}


	@Override
	public Integer getAuditLeaveApplyListCnt(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getAuditLeaveApplyListCnt(jsonObject);
	}


	@Override
	public Integer getLeaveApplyListCnt(JSONObject jsonObject) {
		 
		return leaveApplicationDao.getLeaveApplyListCnt(jsonObject);
	}


	@Override
	public JSONObject getLeaveApplyprocedureLevelByTeacherId(JSONObject param) {

		return leaveApplicationDao.getLeaveApplyprocedureLevelByTeacherId(param);
	}


	@Override
	public List<JSONObject> getLeaveStatistics(JSONObject param) {
		 
		return leaveApplicationDao.getLeaveStatistics(param);
	}


	@Override
	public Integer getLeaveStatisticsCnt(JSONObject param) {
		
		return leaveApplicationDao.getLeaveStatisticsCnt(param);
	}


	@Override
	public int insertLeaveApplyTemp(JSONObject param) {
		 
		return leaveApplicationDao.insertLeaveApplyTemp(param);
	}


	@Override
	public int deleteLeaveApplyTemp(JSONObject param) {
		 
		return leaveApplicationDao.deleteLeaveApplyTemp(param);
	}


	@Override
	public List<JSONObject> getLeaveApplyTempStatistics(JSONObject param) {
		 
		return leaveApplicationDao.getLeaveApplyTempStatistics(param);
	}


	@Override
	public int insertLeaveApplyProcedureMember(JSONObject param) {
		 
		return leaveApplicationDao.insertLeaveApplyProcedureMember(param);
	}


	@Override
	public int deleteLeaveApplyProcedureMember(JSONObject param) {
		 
		return leaveApplicationDao.deleteLeaveApplyProcedureMember(param);
	}


	@Override
	public List<JSONObject> getLeaveApplyProcedureMember(JSONObject param) {
		 
		return leaveApplicationDao.getLeaveApplyProcedureMember(param);
	}


	@Override
	public int deleteFestival(JSONObject param) {
		return leaveApplicationDao.deleteFestival(param);
	}


	@Override
	public int insertFestivalList(List<JSONObject> param) {
		return leaveApplicationDao.insertFestivalList(param);
	}


	@Override
	public List<JSONObject> getLeaveApplyPendingForTeacherZone(JSONObject param) {
		 
		return leaveApplicationDao.getLeaveApplyPendingForTeacherZone(param);
	}


	@Override
	public JSONObject getLeaveApplyFinshedForTeacherZone(JSONObject param) {
		 
		return  leaveApplicationDao.getLeaveApplyFinshedForTeacherZone(param);
	}


	@Override
	public List<JSONObject> getLeaveApplyAuditorForTeacherZone(JSONObject param) {
		 
		return leaveApplicationDao.getLeaveApplyAuditorForTeacherZone(param);
	}

	@Override
	public boolean isMoudleManager(String moulderId,String teacherUserId) {
		JSONObject param = new JSONObject();
		param.put("moudleId", moulderId);
		List<JSONObject> teacherList = leaveApplicationDao.getTeacherListByMoudleId(param);
		for(JSONObject each : teacherList) {
			if(teacherUserId.equals(each.getString("teacherUserId"))) {
				return true;
			}
		}
		return false;
	}
}
