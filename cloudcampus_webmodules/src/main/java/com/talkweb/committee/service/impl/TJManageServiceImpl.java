package com.talkweb.committee.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.committee.dao.TJManageDao;
import com.talkweb.committee.service.TJManageService;
import com.talkweb.commondata.service.AllCommonDataService;

@Service
public class TJManageServiceImpl implements TJManageService {
	
	@Autowired
	private TJManageDao tJManageDao;
	
	@Autowired
	private AllCommonDataService allCommonDataService;

	@Override
	public int updateStudentInfo(JSONObject param) {
		return tJManageDao.updateStudentInfo(param);
	}

	@Override
	public int deleteStudent(JSONObject param) {
		return tJManageDao.deleteStudent(param);
	}

	@Override
	public JSONObject getStudentList(JSONObject param,int branch) {
		JSONObject result = new JSONObject();
		final int IS_DB_MEMBER = 0;
		final int IS_MEMBER = 1;
		final int IS_NOT_MEMBER = 2;
		// 查询出团籍库中的学生列表
		List<JSONObject> nameList = tJManageDao.getStudentList(param);
		if (CollectionUtils.isNotEmpty(nameList)){
			Map<Long,JSONObject> stuMap = new HashMap<Long,JSONObject>();
			List<Long> accountIds = new ArrayList<Long>();
			for(JSONObject stuJSON : nameList){
				long studentId = stuJSON.getLongValue("studentId");
				accountIds.add(studentId);
				stuMap.put(studentId, stuJSON);
			}
		    // 查询出学生对应的account信息
			String termInfoId = param.getString("termInfoId");
			long schoolId = Long.parseLong(param.getString("schoolId"));			
			List<Account> list = allCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
			List<Long> classIds = new ArrayList<Long>();
			for(Account account : list)
			{
				long accountId = account.getId();
				JSONObject stu = stuMap.get(accountId);
				stu.put("stdNumber", account.getIdNumber());
				List<User> userList = account.getUsers();
				for(User user : userList)
				{
					if (null == user || null == user.getUserPart() || null == user.getUserPart().getRole()) {
						continue;
					}
					if (user.getUserPart().getRole().equals(T_Role.Student)) 
					{
					    long classId = user.getStudentPart().getClassId();
					    classIds.add(classId);
					    stu.put("classId", classId);
					    if (IS_DB_MEMBER == user.getStudentPart().getIsYouth())
					    {
						    stu.put("isMembers", IS_MEMBER);
					    }else{
						    stu.put("isMembers", IS_NOT_MEMBER);
					    }
					}	
				}
			}
			// 查询出学生的班级信息列表
			List<Long> gradeIds = new ArrayList<Long>();
			Map<Long,Long> classGradeMap = new HashMap<Long,Long>();
			Map<Long,String> classMap = new HashMap<Long,String>();
			if (CollectionUtils.isNotEmpty(classIds)){
				List<Classroom> roomList = allCommonDataService.getClassroomBatch(schoolId, classIds, termInfoId);
				for(Classroom room : roomList)
				{
					classMap.put(room.getId(), room.getClassName());
					long gradeId = room.getGradeId();
					gradeIds.add(gradeId);
					classGradeMap.put(room.getId(), gradeId);
				}
			}
			// 查询出班级对应的年级信息列表
			Map<Long,String> gradeMap = new HashMap<Long,String>();
			Map<String,String> gradeNameMap = new HashMap<String,String>();
			if (CollectionUtils.isNotEmpty(gradeIds)){
				List<Grade> gradeList = allCommonDataService.getGradeBatch(schoolId, gradeIds, termInfoId);
				for(Grade grade : gradeList)
				{
					String xn = termInfoId.substring(0, 4);
					int njdm = grade.getCurrentLevel().getValue();
					String synj = allCommonDataService.ConvertNJDM2SYNJ(njdm + "", xn);
					gradeMap.put(grade.getId(), synj);
					T_GradeLevel tgl = T_GradeLevel.findByValue(njdm);
					String gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
					gradeNameMap.put(synj, gradeName);
				}
			}
			// 组合数据	
			int memberNum = 0;
			if (branch == 1){
				for(JSONObject stuJSON : nameList){
					long classId = stuJSON.getLongValue("classId");
					stuJSON.put("className", classMap.get(classId));
					long gradeId = classGradeMap.get(classId);
					stuJSON.put("gradeName", gradeNameMap.get(gradeMap.get(gradeId)));
					if(stuJSON.getIntValue("isMembers") == IS_MEMBER)memberNum++;
				}
				result.put("membersNum", memberNum);
				result.put("totalNum", nameList.size());
				result.put("studentList", nameList);
			}else{
				String gradeIdPage = param.getString("gradeId");
				String classIdPage = param.getString("classId");
				String isMembers = param.getString("isMembers");
				List<JSONObject> tempList = new ArrayList<JSONObject>(nameList);
				for(JSONObject stuJSON : nameList){
					long classId = stuJSON.getLongValue("classId");
					long gradeId = classGradeMap.get(classId);
					String synj = gradeMap.get(gradeId);
					int isMember = stuJSON.getIntValue("isMembers");
					if (equalsPage(isMembers, String.valueOf(isMember), gradeIdPage, 
							synj, classIdPage, String.valueOf(classId))) {
						tempList.remove(stuJSON);continue;
					}			
					stuJSON.put("className", classMap.get(classId));
					stuJSON.put("gradeName", gradeNameMap.get(gradeMap.get(gradeId)));
					if(isMember == IS_MEMBER)memberNum++;
				}
				if (CollectionUtils.isNotEmpty(tempList))
				{
					result.put("membersNum", memberNum);
					result.put("totalNum", tempList.size());
					result.put("studentList", tempList);
				}	
			}
		}
		return result;
	}

	private boolean equalsPage(String isMembers, String isMember, 
			String gradeIdPage, String synj, String classIdPage, String classId) {
		final String IS_ALL = "0";
		if (!isMembers.equals(IS_ALL) 
				&& !isMembers.equals(isMember)) {
			return true;
		} else if (!gradeIdPage.equals(IS_ALL) 
				&& !gradeIdPage.equals(synj)) {
			return true;
		} else if (!classIdPage.equals(IS_ALL) 
				&& !classIdPage.equals(classId)) {
			return true;
		} else {
			return false;
		}	
	}

	@Override
	public List<JSONObject> getStudentIdentityList(JSONObject param) {
		List<JSONObject> array = new ArrayList<JSONObject>();
		String termInfoId = param.getString("termInfoId");
		String name = param.getString("studentName");
		long schoolId = Long.parseLong(param.getString("schoolId"));
		School sch = allCommonDataService.getSchoolById(schoolId,termInfoId);
		List<Account> aList = allCommonDataService.getAllStudent(sch, termInfoId);
		for(Account account : aList)
		{
			if (StringUtils.isBlank(account.getName()))
			{
				continue;
			}else if(StringUtils.isEmpty(name)
					|| (account.getName().toLowerCase()).indexOf(name.toLowerCase()) >= 0){				
				JSONObject returnObj = new JSONObject();
				returnObj.put("studentName", account.getName().toLowerCase());
				returnObj.put("studentId", account.getId() + "");
				returnObj.put("stdNumber", account.getIdNumber());
				array.add(returnObj);	
			}
		}
		return array;
	}

	@Override
	public List<JSONObject> getTJGradeList(JSONObject param) {	
		List<JSONObject> array = new ArrayList<JSONObject>();
		long schoolId = Long.parseLong(param.getString("schoolId"));
		String termInfoId = param.getString("termInfoId");
		School sch = allCommonDataService.getSchoolById(schoolId,termInfoId);
		List<Grade> gradeList = allCommonDataService.getGradeList(sch, termInfoId);
		for(Grade grade : gradeList){
			JSONObject gradeJSON = new JSONObject();
			String xn = termInfoId.substring(0, 4);
			int njdm = grade.getCurrentLevel().getValue();
			String synj = allCommonDataService.ConvertNJDM2SYNJ(njdm + "", xn);
			T_GradeLevel tgl = T_GradeLevel.findByValue(njdm);
			String gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
			gradeJSON.put("gradeId", synj);
			gradeJSON.put("gradeName", gradeName);
			array.add(gradeJSON);
		}
		return array;
	}

	@Override
	public List<JSONObject> getTJClassList(JSONObject param) {
		List<JSONObject> array = new ArrayList<JSONObject>();
		HashMap<String,Object> map = new HashMap<String,Object>();
		String gradeId = param.getString("gradeId");
		final String IS_ALL = "0";
		if (!gradeId.equals(IS_ALL)){
			map.put("usedGradeId", gradeId);
		}
		map.put("schoolId", param.getString("schoolId"));
		map.put("termInfoId", param.getString("termInfoId"));
		List<Classroom> roomList = allCommonDataService.getClassList(map);
		for(Classroom room : roomList){
			JSONObject classJSON = new JSONObject();
			classJSON.put("classId", room.getId());
			classJSON.put("className", room.getClassName());
			array.add(classJSON);
		}		
		return array;
	}
	
}