package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.csbasedata.dao.OrgManageDao;
import com.talkweb.csbasedata.service.OrgManageService;
import com.talkweb.csbasedata.util.GradeUtil;
import com.talkweb.csbasedata.util.SortUtil;


/**
 * 机构管理实现-serviceImpl
 * @author zhh
 *
 */
@Service("orgManageService")
public class OrgManageServiceImpl implements OrgManageService{
	@Autowired
	private OrgManageDao orgManageDao;
	final String LEADERTYPE="1"; //领导
	final String MEMBERTYPE="2"; //成员
/**
 * 科室
 * @author zhh
 */
	@Override
	public List<JSONObject> getDepartmentList(JSONObject obj)throws Exception {
		final  String fixedDepartmentName = "校办";
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		List<JSONObject> dList = orgManageDao.getOrgList(obj);
		Map<String,List<JSONObject>> orgLeaderNameMap = new HashMap<String,List<JSONObject>>();
		Map<String,Integer> orgMemberNumMap = new HashMap<String,Integer>();
		for(JSONObject dInfo:dInfos){
			String departmentId = dInfo.getString("uuid");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			JSONObject dJson = new JSONObject();
			if(StringUtils.isBlank(name)){
				dJson.put("name", "");
			}else{
				dJson.put("name", name);
			}
			dJson.put("accountId", accountId);
			if(LEADERTYPE.equals(departmentType)){
				if(orgLeaderNameMap.get(departmentId)==null){
					List<JSONObject> dJsonList= new ArrayList<JSONObject>();
					dJsonList.add(dJson);
					orgLeaderNameMap.put(departmentId, dJsonList);
				}else{
					List<JSONObject> dJsonList = orgLeaderNameMap.get(departmentId);
					dJsonList.add(dJson);
					orgLeaderNameMap.put(departmentId, dJsonList);
				}
			}
			
			if(orgMemberNumMap.get(departmentId)==null){
				orgMemberNumMap.put(departmentId, 1);
			}else{
				int num  = orgMemberNumMap.get(departmentId);
				num++;
				orgMemberNumMap.put(departmentId, num);
			}
			
		}//end of for dInfos
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("uuid");
			String departmentName = d.getString("orgName");
			if(fixedDepartmentName.equals(departmentName)){
				d.put("isOperate", 1);
			}else{
				d.put("isOperate", 0);
			}
			d.put("departmentLeader", new ArrayList<JSONObject>());
			d.put("departmentId", departmentId);
			d.put("departmentName", departmentName);
			d.put("departmentNum", 0);
			if(orgLeaderNameMap.get(departmentId)!=null ){
				List<JSONObject> departmentLeader = orgLeaderNameMap.get(departmentId);
				if(departmentLeader!=null){
					SortUtil.sortNameJSONList(departmentLeader,"name");
					d.put("departmentLeader", departmentLeader);
				}
			}
			if(orgMemberNumMap.get(departmentId)!=null){
				int num = orgMemberNumMap.get(departmentId);
				if(num>0){
					d.put("departmentNum", num);
				}
			}
			d.put("orgId",departmentId);
		}//end of for dList
		return dList;
	}

	@Override
	public int updateDepartment(JSONObject obj)throws Exception{
		String schoolId = obj.getString("schoolId");
		String uuid = obj.getString("uuid");
		Boolean isInsert = false;
	    //判断传入的机构是否有重复的名称(同学校同机构类型之下)
		if(StringUtils.isNotBlank(uuid)){
			obj.put("noUuid", uuid);
		}
		List<JSONObject> oNameList = orgManageDao.getOrgObj(obj);
		if(oNameList!=null  && oNameList.size()>0){
			return -1;
		}
		if(StringUtils.isBlank(uuid)){ //新增
			obj.put("uuid", UUIDUtil.getUUID());
			isInsert=true;
			//新增orgInfo
			orgManageDao.insertOrg(obj);
		}else{
			//更新orgInfo
			orgManageDao.updateOrg(obj);
		}
		if(isInsert){
			obj.put("isInsert", "1");
		}
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		if(oList==null || oList.size()<1 || oList.get(0)==null || !oList.get(0).containsKey("orgId")){
			throw new Exception();
		}
		JSONObject o = oList.get(0);
		String orgId = o.getString("orgId");
		if(StringUtils.isBlank(orgId)){
			throw new Exception();
		}
		//删除所有领导
		if(!isInsert){
			List<String> ids = new ArrayList<String>();
			ids.add(orgId);
			obj.put("ids", ids);
			if(ids.size()>0){
				orgManageDao.deleteOrgLeader(obj);
			}
		}
		//添加所有领导
		List<String> oldOrgLeaderList = (List<String>) obj.get("oldOrgLeader");
		List<String> lList = (List<String>) obj.get("orgLeader");
		List<String> allAccountIds = new ArrayList<String>();
		if(lList!=null && lList.size()>0){
			allAccountIds.addAll(lList);
		}else{
			lList= new ArrayList<String>();
		}
		if(oldOrgLeaderList!=null && oldOrgLeaderList.size()>0){
			allAccountIds.addAll(oldOrgLeaderList);
		}else{
			oldOrgLeaderList= new ArrayList<String>();
		}
		
		JSONObject json = new JSONObject();
		json.put("schoolId", obj.getString("schoolId"));
		json.put("ids", allAccountIds);
		json.put("role", T_Role.Teacher.getValue());
		List<JSONObject> userList = new ArrayList<JSONObject>();
		if(allAccountIds.size()>0){
			userList = orgManageDao.getUserIdByAccountIdList(json);
		}
		Map<String,String> accountIdUserIdMap = new HashMap<String,String>();
		for(JSONObject auObj:userList){
			String accountId = auObj.getString("accountId");
			String userId = auObj.getString("userId");
			if(StringUtils.isNotBlank(accountId)&& StringUtils.isNotBlank(userId)){
				accountIdUserIdMap.put(accountId, userId );
			}
		}
		List<String> oldUserIdList = new ArrayList<String>();
		for(String oldAccountId:oldOrgLeaderList){
			if(accountIdUserIdMap.get(oldAccountId)==null){continue;}
			if(!oldUserIdList.contains(accountIdUserIdMap.get(oldAccountId))){
				oldUserIdList.add(accountIdUserIdMap.get(oldAccountId));
			}
		}
		List<String> memberIds = new ArrayList<String>();
		if(!isInsert){
			 //获取该机构的所有成员
			memberIds = orgManageDao.getMembers(obj);
		}
		//获取该学校的所有staff
		List<String> userIdList  = new ArrayList<String>();
		List<JSONObject> ls = new ArrayList<JSONObject>();
		for(String lId:lList){
			JSONObject  lObj = new JSONObject();
			lObj.put("orgId", orgId);
			if(accountIdUserIdMap.get(lId)==null){continue;}
			lObj.put("userId", accountIdUserIdMap.get(lId));
			lObj.put("schoolId", schoolId);
			lObj.put("jobType", 0); //职务类型都默认不填 用于staff表的插入
			if(memberIds!=null && memberIds.contains(accountIdUserIdMap.get(lId))){
				if(!userIdList.contains(accountIdUserIdMap.get(lId))){
					userIdList.add(accountIdUserIdMap.get(lId));
				}
			}
			ls.add(lObj);
		}
		if(ls.size()>0){
			orgManageDao.insertOrgLeaderBatch(ls);
		}
		//判断插入的领导中是否有成员身份，若有则删除成员身份
		if(userIdList.size()>0 && !isInsert){
			JSONObject param = new JSONObject();
			param.put("ids", userIdList);
			param.put("uuid", uuid);
			orgManageDao.deleteOrgMemberByUserId(param);
		}
		obj.put("orgId", orgId);
		JSONObject schoolOrg = orgManageDao.getSchoolOrg(obj);
		if(schoolOrg==null){
			orgManageDao.insertSchoolOrg(obj);
		}
		//删除staff
		if(!isInsert &&  oldUserIdList!=null && oldUserIdList.size()>0){
			JSONObject staffObj = new JSONObject();
			staffObj.put("schoolId", schoolId);
			staffObj.put("orgId", orgId);
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(staffObj);
			if(staffIdsNotInOrg!=null){
				oldUserIdList.removeAll(staffIdsNotInOrg);
			}
			obj.put("ids", oldUserIdList);
			if(oldUserIdList.size()>0){
				orgManageDao.deleteStaff(obj);
			}
		}
		//添加staff
		if(ls.size()>0){
			orgManageDao.insertStaffBatch(ls);
		}
		return 1;
	}
	@Override
	public int updateDepartmentMember(JSONObject obj) throws Exception {
		//删除所有成员 //获取orgId
		List<JSONObject> list = orgManageDao.getOrgList(obj);
		if(list!=null &&list.size()>0 && list.get(0).containsKey("orgId") && StringUtils.isNotBlank(list.get(0).getString("orgId"))){
			JSONObject org = list.get(0);
			String orgId=org.getString("orgId");
			List<String> ids = new ArrayList<String>();
			ids.add(orgId);
			obj.put("ids", ids);
			if(ids.size()>0){
				orgManageDao.deleteOrgMember(obj);
			}
			//添加成员
			List<String> mList = (List<String>) obj.get("orgMember");
			JSONObject json = new JSONObject();
			json.put("schoolId", obj.getString("schoolId"));
			if(mList!=null && mList.size()>0){
				json.put("ids", mList);
			}else{
				mList = new ArrayList<String>();
			}
			json.put("role", T_Role.Teacher.getValue());
			List<JSONObject> userList = new ArrayList<JSONObject>();
			if(mList.size()>0){
				userList = orgManageDao.getUserIdByAccountIdList(json);
			}
			Map<String,String> accountIdUserIdMap = new HashMap<String,String>();
			List<String> userIds = new ArrayList<String>();
			if(userList!=null){
				for(JSONObject auObj:userList){
					String accountId = auObj.getString("accountId");
					String userId = auObj.getString("userId");
					if(StringUtils.isBlank(accountId)||StringUtils.isBlank(userId)){continue;}
					if(!userIds.contains(userId)){
						userIds.add(userId);
					}
					accountIdUserIdMap.put(accountId, userId);
				}
			}
			//判断该成员是否为领导，若是则剔除这些成员的插入普通成员操作
			List<String>  leaderUserIds = new ArrayList<String>();
			if(userIds.size()>0){
				JSONObject leaderJson = new JSONObject();
				leaderJson.put("uuid", obj.getString("uuid"));
				leaderJson.put("ids", userIds);
				leaderUserIds = orgManageDao.getLeaders(leaderJson);
			}
			List<JSONObject> ms = new ArrayList<JSONObject>();
			for(String mId:mList){
				JSONObject  mObj = new JSONObject();
				mObj.put("orgId", orgId);
				if(accountIdUserIdMap.get(mId)==null){continue;}
				String userId = accountIdUserIdMap.get(mId);
				if(leaderUserIds!=null  && leaderUserIds.contains(userId)){continue;}
				mObj.put("userId", userId);
				ms.add(mObj);
			}
			if(ms.size()>0){
				orgManageDao.insertOrgMemberBatch(ms);
			}
		}
		return 1;
	}
	@Override
	public JSONObject getDepartmentInfo(JSONObject obj)throws Exception {
		JSONObject returnObj = new JSONObject();
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		int memberNum = 0;
		returnObj.put("departmentNum", memberNum);
		List<JSONObject> departmentMembers = new ArrayList<JSONObject>();
		List<JSONObject> departmentLeaders = new ArrayList<JSONObject>();
		List<JSONObject> departmentLeaderList = new ArrayList<JSONObject>();
		for(JSONObject dInfo:dInfos){
			JSONObject departmentMember = new JSONObject();
			JSONObject departmentLeader = new JSONObject();
			String departmentId = dInfo.getString("uuid");
			String departmentName = dInfo.getString("orgName");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			String createTime = dInfo.getString("createTime");
			returnObj.put("departmentId", departmentId);
			if(StringUtils.isBlank(departmentName)){
				returnObj.put("departmentName", "");
			}else{
				returnObj.put("departmentName", departmentName);
			}
			returnObj.put("createTime", createTime);
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			if(StringUtils.isBlank(name)){
				departmentMember.put("name", "");
			}else{
				departmentMember.put("name", name);
			}
			departmentMember.put("accountId", accountId);
			memberNum++;
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					departmentLeader.put("name", name);
				}else{
					departmentLeader.put("name", "");
				}
				departmentLeader.put("accountId", accountId);
				departmentLeaderList.add(departmentLeader);
				departmentMember.put("position", 1); //领导
				departmentLeaders.add(departmentMember);
			}
			if(MEMBERTYPE.equals(departmentType)){
				departmentMember.put("position", 2); //成员
				departmentMembers.add(departmentMember);
			}
			
		}// end of for dInfos
		SortUtil.sortNameJSONList(departmentLeaderList,"name");
		returnObj.put("departmentLeader", departmentLeaderList);
		if(memberNum>0){
			returnObj.put("departmentNum", memberNum);
		}
		SortUtil.sortNameJSONList(departmentLeaders,"name");
		SortUtil.sortNameJSONList(departmentMembers,"name");
		departmentLeaders.addAll(departmentMembers);
		returnObj.put("departments", departmentLeaders);
		return returnObj;
	}

	@Override
	public int deleteDepartment(JSONObject obj) throws Exception{
		List<String> ids = (List<String>) obj.get("ids");
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		List<String> orgIds = new ArrayList<String>();
		if(oList!=null){
			for(JSONObject o:oList){
				String orgId = o.getString("orgId");
				if(StringUtils.isNotBlank(orgId)){
					if(!orgIds.contains(orgId)){
						orgIds.add(orgId);
					}
				}
			}
		}
		if(ids!=null && ids.size()>0 && orgIds.size()>0){
			orgManageDao.deleteOrg(obj);
			obj.put("ids", orgIds);
			//删除staff
			//获取本学校所有leader的userId
			List<String> staffIds = orgManageDao.getStaffIdsList(obj);
			//查找staffIds在所有的包括将删除的orgId的leader
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(obj);
			//staffIds-staffIdss删除这部分人
			if(staffIds!=null && staffIdsNotInOrg!=null){
				staffIds.removeAll(staffIdsNotInOrg);
			}
			orgManageDao.deleteOrgMember(obj);
			orgManageDao.deleteOrgLeader(obj);
			orgManageDao.deleteSchoolOrg(obj);
			if(staffIds!=null && staffIds.size()>0){
				obj.put("ids", staffIds);
				orgManageDao.deleteStaff(obj);
			}
		}
		return 1;
	}
	@Override
	public int deleteDepartmentMember(JSONObject param) throws Exception {
		String position = param.getString("position");
		param.put("role", T_Role.Teacher.getValue());
		List<JSONObject> users = orgManageDao.getUserIdByAccountIdList(param);
		if("1".equals(position)){ //领导
			if(users!=null && users.size()>0 && users.get(0)!=null && StringUtils.isNotBlank( users.get(0).getString("userId"))){
				JSONObject u = users.get(0);
				String userId = u.getString("userId");
				//查找staffIds在所有的包括将删除的orgId的leader
				List<JSONObject> orgList = orgManageDao.getOrgList(param);
				if(orgList!=null && orgList.size()>0 && orgList.get(0)!=null &&StringUtils.isNotBlank(orgList.get(0).getString("orgId"))){
					JSONObject o = orgList.get(0);
					String orgId = o.getString("orgId");
					param.put("orgId", orgId);
					List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(param);
					if(staffIdsNotInOrg!=null && !staffIdsNotInOrg.contains(userId)){
						//删除staff表
						JSONObject staffObj = new JSONObject();
						staffObj.put("staffId", userId);
						staffObj.put("schoolId",param.getString("schoolId"));
						orgManageDao.deleteStaff(staffObj);
					}
					List<String> ids = new ArrayList<String>();
					ids.add(userId);
					param.put("ids", ids);
					if(StringUtils.isNotBlank(userId)){
						orgManageDao.deleteOrgLeaderByUserId(param);
					}
				}
			}
		}else{ //成员
			if(users!=null && users.size()>0 && users.get(0)!=null && StringUtils.isNotBlank(users.get(0).getString("userId"))){
				JSONObject u = users.get(0);
				String userId = u.getString("userId");
				List<String> ids = new ArrayList<String>();
				ids.add(userId);
				param.put("ids", ids);
				if(StringUtils.isNotBlank(userId)){
					orgManageDao.deleteOrgMemberByUserId(param);
				}
			}
		}
		
		return 1;
	}
/**
 * 年级组
 * @author zhh
 */
	@Override
	public List<JSONObject> getGradegroupList(JSONObject obj) throws Exception {
		String schoolId = obj.getString("schoolId");
		String xn = obj.getString("xn");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		List<JSONObject> dList = orgManageDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderNameMap = new HashMap<String, List<String>>();
		//uuid-JSONObject
		Map<String,List<JSONObject>> orgLeaderMap = new HashMap<String,List<JSONObject>>();
		for(JSONObject dInfo:dInfos){
			String departmentId = dInfo.getString("orgId");
			String uuid = dInfo.getString("uuid");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			JSONObject dJson = new JSONObject();
			if(StringUtils.isBlank(name)){
				dJson.put("name", "");
			}else{
				dJson.put("name", name);
			}
			dJson.put("accountId", accountId);
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					if(leaderNameMap.get(departmentId)==null){
						List<String> nameStringList = new ArrayList<String>();
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}else{
						List<String> nameStringList = leaderNameMap.get(departmentId);
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}
				}
				if(orgLeaderMap.get(uuid)==null){
					List<JSONObject> dJsonList = new ArrayList<JSONObject>();
					dJsonList.add(dJson);
					orgLeaderMap.put(uuid, dJsonList);
				}else{
					List<JSONObject> dJsonList = orgLeaderMap.get(uuid);
					dJsonList.add(dJson);
					orgLeaderMap.put(uuid, dJsonList);
				}
			}
			
		} //end of for dInfos
		//获取机构-年级数据
		Map<String, LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		//orgId（不是uuid）-年级名称
		Map<String,LinkedHashSet<String>> orgIdScopeNamesMap = new HashMap<String, LinkedHashSet<String>>();
		List<JSONObject> osList = orgManageDao.getOrgScopeList(obj);
		//gradeId
		List<String> removeGIdList = new ArrayList<String>();
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		for(JSONObject os:osList){
			int currentLevel = os.getIntValue("currentLevel");
			int createLevel = os.getInteger("createLevel");
			String gradeId = os.getString("gradeId");
			Boolean isGraduate = GradeUtil.isGraduate(createLevel, currentLevel);
			if(isGraduate){
				if(StringUtils.isNotBlank(gradeId)){
					if(!removeGIdList.contains(gradeId)){
						removeGIdList.add(gradeId);
					}
				}
				continue;
			}
			String orgId = os.getString("orgId");
			LinkedHashSet<String> idSet = new LinkedHashSet<String>();
			LinkedHashSet<String> nameSet = new LinkedHashSet<String>();
			if(orgIdScopeIdsmap.containsKey(orgId)){
				idSet = orgIdScopeIdsmap.get(orgId);	
			}
			if(currentLevel!=0){
				idSet.add(currentLevel+"");
				orgIdScopeIdsmap.put(orgId, idSet);
			}
			if(orgIdScopeNamesMap.containsKey(orgId)){
				nameSet = orgIdScopeNamesMap.get(orgId);	
			}
			if(StringUtils.isNotBlank(njName.get(T_GradeLevel.findByValue(currentLevel)))){
				nameSet.add(njName.get(T_GradeLevel.findByValue(currentLevel)));
				orgIdScopeNamesMap.put(orgId, nameSet);
			}
		}
		//获取机构成员数据
		Map<String,List<String>> mMap = getTeacherByLessonOrGradeBatch(leaderNameMap,removeGIdList,orgIdScopeIdsmap,null,schoolId,xn);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String uuid = d.getString("uuid");
			String departmentName = d.getString("orgName");
			d.put("gradeId", "");
			d.put("gradeName", "");
			d.put("gradegroupLeader", new ArrayList<JSONObject>());
			d.put("gradegroupId", departmentId);
			d.put("gradegroupName", departmentName);
			d.put("gradegroupNum", 0);
			if(orgLeaderMap.get(uuid)!=null ){
				List<JSONObject> departmentLeader = orgLeaderMap.get(uuid);
				if(departmentLeader!=null){
					SortUtil.sortNameJSONList(departmentLeader,"name");
					d.put("gradegroupLeader", departmentLeader);
					d.put("gradegroupNum", departmentLeader.size());
				}
			}
			if(mMap!=null && mMap.get(departmentId)!=null &&  mMap.get(departmentId).size()>0){
				List<String> names = mMap.get(departmentId);
				int gradegroupNum = d.getIntValue("gradegroupNum");
				if(names!=null){
					d.put("gradegroupNum", names.size()+gradegroupNum);
				}
			}
			if(orgIdScopeIdsmap.get(departmentId)!=null){
				Set<String> idSetList = orgIdScopeIdsmap.get(departmentId);
				String idList = "";
				for(String id:idSetList){
					idList+=id+",";
				}
				if(!StringUtils.isBlank(idList)){
					idList=idList.substring(0, idList.length()-1);
					d.put("gradeId", idList);
				}
			}
			if(orgIdScopeNamesMap.get(departmentId)!=null){
				Set<String> nameSetList = orgIdScopeNamesMap.get(departmentId);
				String nameList="";
				for(String name:nameSetList){
					nameList+=name+",";
				}
				if(!StringUtils.isBlank(nameList)){
					nameList=nameList.substring(0, nameList.length()-1);
					d.put("gradeName", nameList);
				}
			}
			d.put("orgId",uuid);
			d.put("gradegroupId",uuid);
		}
		return dList;
	}
   
	@Override
	public JSONObject updateGradegroup(JSONObject obj) throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		String uuid = obj.getString("uuid");
		Boolean isInsert = false;
		 //判断传入的机构是否有重复的名称
		if(StringUtils.isNotBlank(uuid)){
			obj.put("noUuid", uuid);
		}
		List<JSONObject> oNameList = orgManageDao.getOrgObj(obj);
		if(oNameList!=null  && oNameList.size()>0){
			returnObj.put("returnNum",-1);
			return returnObj;
		}
		String createUuid = UUIDUtil.getUUID();
		//判断传入的年级数据是否重复设置
		List<String> gIdList = (List<String>) obj.get("gradeId");//传入的是currentLevel
		JSONObject scopeObj = new JSONObject(); //获取当前学校的所有已设置的年级列表
		scopeObj.put("schoolId", schoolId);
		scopeObj.put("xn", obj.getString("xn"));
		scopeObj.put("orgType", obj.getString("orgType"));
		if(StringUtils.isBlank(uuid)){
			scopeObj.put("noUuid", createUuid);
		}else{
			scopeObj.put("noUuid", uuid);
		}
		List<JSONObject> scopeList = orgManageDao.getOrgScopeList(scopeObj);
		List<String> setedCurrentLevel = new ArrayList<String>();
		for(JSONObject scope:scopeList){
			setedCurrentLevel.add(scope.getString("currentLevel"));
		}
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		List<String> clList= new ArrayList<String>() ;
		for(String cl:gIdList){
			if(setedCurrentLevel.contains(cl)){
				clList.add(cl);
			}
		}
		if(clList.size()>0){
			String returnGradeName = "";
			for(String cl:clList){
				returnGradeName+=njName.get(T_GradeLevel.findByValue(Integer.parseInt(cl)))+",";
			}
			returnGradeName = returnGradeName.substring(0,returnGradeName.length()-1);
			returnObj.put("returnNum", -2);
			returnObj.put("returnGradeName", returnGradeName);
			return returnObj;
		}
		if(StringUtils.isBlank(uuid)){ //新增
			obj.put("uuid",createUuid);
			isInsert=true;
			//新增orgInfo
			orgManageDao.insertOrg(obj);
		}else{
			//更新orgInfo
			orgManageDao.updateOrg(obj);
		}
		if(isInsert){
			obj.put("isInsert", "1");
		}
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		if(oList==null || oList.size()<1 || oList.get(0)==null ||!oList.get(0).containsKey("orgId")){
			throw new Exception();
		}
		JSONObject o = oList.get(0);
		String orgId = o.getString("orgId");
		if(StringUtils.isBlank(orgId)){
			throw new Exception();
		}
		
		if(!isInsert){
			List<String> ids = new ArrayList<String>();
			ids.add(orgId);
			obj.put("ids", ids);
			if(ids.size()>0){
				//删除所有领导
				orgManageDao.deleteOrgLeader(obj);
				//删除所有年级
				orgManageDao.deleteOrgScope(obj);
			}
			
		}
		//添加所有领导
		List<String> oldOrgLeaderList = (List<String>) obj.get("oldOrgLeader");
		List<String> lList = (List<String>) obj.get("orgLeader");
		List<String> allAccountIds = new ArrayList<String>();
		if(oldOrgLeaderList!=null && oldOrgLeaderList.size()>0){
			allAccountIds.addAll(oldOrgLeaderList);
		}else{
			oldOrgLeaderList=new ArrayList<String>();
		}
		if(lList!=null && lList.size()>0){
			allAccountIds.addAll(lList);
		}else{
			lList=new ArrayList<String>();
		}
		//List<String> userIdList = new ArrayList<String>();
		JSONObject json = new JSONObject();
		json.put("schoolId", obj.getString("schoolId"));
		json.put("ids", allAccountIds);
		json.put("role", T_Role.Teacher.getValue());
		List<JSONObject> userList = new ArrayList<JSONObject>();
		if(allAccountIds.size()>0){
			userList = orgManageDao.getUserIdByAccountIdList(json);
		}
		Map<String,String> accountIdUserIdMap = new HashMap<String,String>();
		for(JSONObject auObj:userList){
			String accountId = auObj.getString("accountId");
			String userId = auObj.getString("userId");
			if(StringUtils.isNotBlank(accountId) && StringUtils.isNotBlank(userId)){
				accountIdUserIdMap.put(accountId, userId);
			}
		}
		//获取该学校的所有staff
		List<JSONObject> ls = new ArrayList<JSONObject>();
		for(String lId:lList){ //需要添加的leader
			JSONObject  lObj = new JSONObject();
			lObj.put("orgId", orgId);
			if(accountIdUserIdMap.get(lId)==null){continue;}
			lObj.put("userId", accountIdUserIdMap.get(lId));
			lObj.put("schoolId", schoolId);
			lObj.put("jobType", 0); //职务类型都默认不填 用于staff表的插入
			ls.add(lObj);
		}
		List<String> oldUserIdList = new ArrayList<String>();
		for(String oldAccountId:oldOrgLeaderList){
			if(accountIdUserIdMap.get(oldAccountId)==null){continue;}
			if(!oldUserIdList.contains(accountIdUserIdMap.get(oldAccountId))){
				oldUserIdList.add(accountIdUserIdMap.get(oldAccountId));
			}
		}
		if(ls.size()>0){
			orgManageDao.insertOrgLeaderBatch(ls);
		}
		obj.put("orgId", orgId);
		JSONObject schoolOrg = orgManageDao.getSchoolOrg(obj);
		if(schoolOrg==null){
			orgManageDao.insertSchoolOrg(obj);
		}
		//新增所有年级
		/*JSONObject param = new JSONObject();
		if(gIdList!=null && gIdList.size()>0){
			param.put("ids", gIdList);
		}
		param.put("xn", obj.getString("xn"));
		List<JSONObject> gList = orgManageDao.getCurrentLevelGradeList(param);*/
		List<JSONObject> gList = new ArrayList<JSONObject>();
		if(gIdList!=null){
			for(String gId:gIdList){
				JSONObject gObj = new JSONObject();
				if(StringUtils.isBlank(gId)){continue;}
				gObj.put("orgId", orgId);
				gObj.put("scopeId", gId);
				gList.add(gObj);
			}
		}
		if(gList.size()>0){
			orgManageDao.insertOrgScopeBatch(gList);
		}
		//删除staff
		if(!isInsert && oldUserIdList!=null && oldUserIdList.size()>0){
			JSONObject staffObj = new JSONObject();
			staffObj.put("schoolId", schoolId);
			staffObj.put("orgId", orgId);
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(staffObj);
			if(staffIdsNotInOrg!=null){
				oldUserIdList.removeAll(staffIdsNotInOrg);
			}
			obj.put("ids", oldUserIdList);
			if(oldUserIdList.size()>0){
				orgManageDao.deleteStaff(obj);
			}
		}
		//添加staff
		if(ls.size()>0){
			orgManageDao.insertStaffBatch(ls);
		}
		returnObj.put("returnNum", 1);
		return returnObj;
	}

	@Override
	public JSONObject getGradegroupInfo(JSONObject obj) throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		//String leaderNames = "";
		int memberNum = 0;
		returnObj.put("gradegroupLeader", new ArrayList<JSONObject>());
		returnObj.put("gradegroupNum", memberNum);
		returnObj.put("gradegroups", new ArrayList<JSONObject>());
		List<JSONObject> departments = new ArrayList<JSONObject>();
		List<JSONObject> departmentLeaderList = new ArrayList<JSONObject>();
		String orgId = "";
		List<String> leaderName = new ArrayList<String>();
		for(JSONObject dInfo:dInfos){
			orgId = dInfo.getString("orgId");
			//JSONObject departmentMember = new JSONObject();
			JSONObject departmentLeader = new JSONObject();
			String departmentId = dInfo.getString("uuid");
			String departmentName = dInfo.getString("orgName");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			String createTime = dInfo.getString("createTime");
			returnObj.put("gradegroupId", departmentId);
			if(StringUtils.isBlank(departmentName)){
				returnObj.put("gradegroupName", "");	
			}else{
				returnObj.put("gradegroupName", departmentName);
			}
			returnObj.put("createTime", createTime);
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					if(!leaderName.contains(name)){
						leaderName.add(name);
					}
				}
				if(StringUtils.isNotBlank(name)){
					departmentLeader.put("name", name);
				}else{
					departmentLeader.put("name", "");
				}
				departmentLeader.put("accountId", accountId);
				departmentLeaderList.add(departmentLeader);
				JSONObject departmentMember = (JSONObject) departmentLeader.clone();
				departmentMember.put("position", 1);
				departments.add(departmentMember);
			}
		}//end of for dInfos
		SortUtil.sortNameJSONList(departmentLeaderList,"name");
		returnObj.put("gradegroupLeader", departmentLeaderList);
		//获取机构范围
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("orgId", orgId);
		json.put("xn", obj.getString("xn"));
		json.put("orgType", obj.getString("orgType"));
		//获取机构相关的年级
		List<JSONObject> scopeList = orgManageDao.getOrgScopeList(json);
		List<String> gList = new ArrayList<String>();
		List<String> removeGIdList = new ArrayList<String>();
		List<JSONObject> gradeList = new ArrayList<JSONObject>();
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		if(scopeList!=null){
			for(JSONObject scope:scopeList){
				String gradeId = scope.getString("gradeId");
				int createLevel = scope.getInteger("createLevel");
				int currentLevel = scope.getInteger("currentLevel");
				Boolean isGraduate = GradeUtil.isGraduate(createLevel, currentLevel);
				if(isGraduate){//去除已毕业的年级
					if(StringUtils.isNotBlank(gradeId)){
						if(!removeGIdList.contains(gradeId)){
							removeGIdList.add(gradeId);
						}
					}
					continue;
				}
				if(!gList.contains(currentLevel+"")){
					gList.add(currentLevel+"");
					JSONObject grade = new JSONObject();
					grade.put("gradeId", currentLevel);
					if(StringUtils.isBlank(njName.get(T_GradeLevel.findByValue(currentLevel)))){
						grade.put("gradeName", "");
					}else{
						grade.put("gradeName", njName.get(T_GradeLevel.findByValue(currentLevel)));
					}
					gradeList.add(grade);
				}
				
			}
		}
		returnObj.put("grade", gradeList);
		//去除领导身份的成员
		List<JSONObject> mList = new ArrayList<JSONObject>();
		if(gradeList.size()>0){
			mList = getTeacherByLessonAndGrade(leaderName,removeGIdList,gList,null,schoolId,obj.getString("xn"));
		}
		if(mList!=null){
			mList.addAll(departments);
			returnObj.put("gradegroupNum", mList.size());
			//中文排序
			SortUtil.sortNameJSONList(mList,"name");
			returnObj.put("gradegroups", mList);
		}
		return returnObj;
	}
  
	@Override
	public int deleteGradegroup(JSONObject obj) throws Exception {
		List<String> ids = (List<String>) obj.get("ids");
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		List<String> orgIds = new ArrayList<String>();
		if(oList!=null){
			for(JSONObject o:oList){
				String orgId = o.getString("orgId");
				if(StringUtils.isNotBlank(orgId)){
					if(!orgIds.contains(orgId)){
						orgIds.add(orgId);
					}
				}
			}
		}
		if(ids!=null && ids.size()>0 && orgIds.size()>0){
			orgManageDao.deleteOrg(obj);
			obj.put("ids", orgIds);
			//删除staff
			//获取本学校所有leader的userId
			List<String> staffIds = orgManageDao.getStaffIdsList(obj);
			//查找staffIds在所有的包括将删除的orgId的leader
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(obj);
			//staffIds-staffIdss删除这部分人
			if(staffIds!=null && staffIdsNotInOrg!=null){
				staffIds.removeAll(staffIdsNotInOrg);
			}
			orgManageDao.deleteOrgMember(obj);
			orgManageDao.deleteOrgLeader(obj);
			orgManageDao.deleteSchoolOrg(obj);
			orgManageDao.deleteOrgScope(obj);
			if(staffIds!=null && staffIds.size()>0){
				obj.put("ids", staffIds);
				orgManageDao.deleteStaff(obj);
			}
		}
		return 1;
	}
	
	/**
	 * 根据单个机构的任教科目或者任教年级获取老师姓名(机构成员) 
	 * 去除nameList后的列表
	 * @param nameList 领导name列表（返回的列表应该去除领导的name列表）
	 * @param removeGIdList g.id
	 * @param gradeId 年级 scopeId即currentLevel 
	 * @param lessonId 科目
	 * @param schoolId 学校
	 * @param xn 学年
	 * @return List<JSONObject>  [{
	 * accountId
	 * name  //某个orgId name不能重复
	 * position
	 * }]
	 * @author zhh
	 */
	private List<JSONObject> getTeacherByLessonAndGrade(List<String> nameList,List<String> removeGIdList,List<String> gradeId,List<String> lessonId,String schoolId,String xn)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(gradeId==null && lessonId==null)
		{
			return null;
		}
		if(StringUtils.isBlank(xn)){
			return null;
		}
		JSONObject obj = new JSONObject();
		obj.put("schoolId", schoolId);
		if(gradeId!=null && gradeId.size()>0){
			obj.put("ids", gradeId);
		}
		if(lessonId!=null && lessonId.size()>0){
			obj.put("lessonIds", lessonId);
		}
		obj.put("noIds", removeGIdList);
		obj.put("xn", xn);
		obj.put("nameList", nameList);
		List<JSONObject> list = orgManageDao.getTeacherByLessonAndGrade(obj);
		return list;
	}
	/**
	 * 根据多个机构的任教科目或者任教年级获取老师姓名（机构成员） 过滤领导
	 * @param leaderNameMap<String,List<String>> orgId-name 要过滤的名称(orgId非uuid)
	 * @param orgIdScopeIdmap<String,String> orgId-scopeId  (currentLevel)
	 * @param orgIdLessonIdmap<String,String> orgId-lessonId
	 * @param schoolId 学校
	 * @return Map<String,List<String>> orgId-nameList   nameList已去重和过滤
	 * @author zhh
	 */
	private Map<String,List<String>> getTeacherByLessonOrGradeBatch(Map<String,List<String>> leaderNameMap,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String xn)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(orgIdScopeIdsmap==null && orgIdLessonIdmap==null)
		{
			return null;
		}
		if(StringUtils.isBlank(xn))
		{
			return null;
		}
		Set<String> scopeList = new HashSet<String>();//存放所有的scopeId
		if(orgIdScopeIdsmap!=null)
		{
			
			for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
			{
				 Set<String> s = entry.getValue();
					 if(s!=null && s.size()>0){
						 scopeList.addAll(s);
					 }
			}
			
		}
		Set<String> lessonList = new HashSet<String>(); //存放所有的lessonId
		if(orgIdLessonIdmap!=null){
			for (Map.Entry<String, String> entry : orgIdLessonIdmap.entrySet()) 
			{
				 String s = entry.getValue();
				 if(StringUtils.isNotBlank(s)){
					 List<String> eList = Arrays.asList(s.split(","));
					 if(eList!=null && eList.size()>0){
						 lessonList.addAll(eList);
					 }
				 }
			}
		}
		JSONObject obj = new JSONObject();
		obj.put("ids", new ArrayList(scopeList)); //currentLevels
		obj.put("lessonIds", new ArrayList(lessonList));
		obj.put("schoolId", schoolId);
		obj.put("xn", xn);
		obj.put("noIds", removeGIdList);
		List<JSONObject> list = orgManageDao.getTeacherByLessonAndGradeBatch(obj);
		//currentLevel-name List
		Map<String,List<String>> scopeNameMap = new HashMap<String, List<String>>();
		//lessonId-name List
		Map<String,List<String>> lessonNameMap = new HashMap<String, List<String>>();
		//得到老师对应的lessonId和currentLevel
		for(JSONObject json:list){
			int currentLevel = json.getIntValue("currentLevel");
			String lessonId = json.getString("lessonId");
			String name = json.getString("name");
			if(lessonNameMap.containsKey(lessonId)){
				List<String> names = lessonNameMap.get(lessonId);
				if(names!=null && !names.contains(name)){
					names.add(name);
					lessonNameMap.put(lessonId, names);
				}
			}else{
				List<String> names = new ArrayList<String>();
				names.add(name);
				lessonNameMap.put(lessonId, names);
			}
			if(scopeNameMap.containsKey(currentLevel+"")){
				List<String> names = scopeNameMap.get(currentLevel+"");
				if(names!=null && !names.contains(name)){
					names.add(name);
					scopeNameMap.put(currentLevel+"", names);
				}
			}else{
				List<String> names = new ArrayList<String>();
				names.add(name);
				scopeNameMap.put(currentLevel+"", names);
			}
		}
		Map<String,List<String>> orgIdNameMap = new HashMap<String, List<String>>();
		if(orgIdScopeIdsmap!=null){
			for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
			{
				String orgId = entry.getKey();
				Set<String> currentLevelSetList = entry.getValue();
				List<String> nameList = leaderNameMap.get(orgId);
				List<String> currentLevelList = new ArrayList<String>(currentLevelSetList);
				//一个机构下的currentLevel循环
				if(currentLevelList!=null){
					for(String c:currentLevelList){
						List<String> names = scopeNameMap.get(c);
						if(names!=null && names.size()>0){
							for(String name:names){
								if(orgIdNameMap.containsKey(orgId)){
									List<String> sList = orgIdNameMap.get(orgId);
									if(!sList.contains(name) ){ //去重
										if(nameList==null || !nameList.contains(name)){
											sList.add(name);
											orgIdNameMap.put(orgId, sList);
										}
									}
								}else{
									if(nameList==null || !nameList.contains(name)){
										List<String> sList = new ArrayList<String>();
										sList.add(name);
										orgIdNameMap.put(orgId, sList);
									}
								}
							}
						
						}
					}
			    }
			}
		}
		if(orgIdLessonIdmap!=null){
			for (Map.Entry<String, String> entry : orgIdLessonIdmap.entrySet()) 
			{
				String orgId = entry.getKey();
				String ll  = entry.getValue();
				List<String> nameList = leaderNameMap.get(orgId);
				List<String> lessonIdList = new ArrayList<String>();
				if(StringUtils.isNotBlank(ll)){
					lessonIdList = Arrays.asList(ll.split(","));
				}
				if(lessonIdList!=null){
					for(String l:lessonIdList){
						List<String> names = lessonNameMap.get(l);
						if(names!=null && names.size()>0){
							for(String name:names){
								if(orgIdNameMap.containsKey(orgId)){
									List<String> sList = orgIdNameMap.get(orgId);
									if(sList!=null && !sList.contains(name)){ //去重
										if(nameList==null || !nameList.contains(name)){
											sList.add(name);
											orgIdNameMap.put(orgId, sList);
										}
									}
								}else{
									if(nameList==null || !nameList.contains(name)){
										List<String> sList = new ArrayList<String>();
										sList.add(name);
										orgIdNameMap.put(orgId, sList);
									}
								}
							}
						
						}
					}
				}
			}
		}
		return orgIdNameMap;
	}
	/**
	 * 根据多个机构的任教科目和任教年级获取老师姓名（机构成员） 过滤领导
	 * @param leaderNameMap<String,List<String>> orgId-name 要过滤的名称(orgId非uuid)
	 * @param orgIdScopeIdmap<String,String> orgId-scopeId  (currentLevel)
	 * @param orgIdLessonIdmap<String,String> orgId-lessonId
	 * @param schoolId 学校
	 * @return Map<String,List<String>> orgId-nameList   nameList已去重和过滤
	 * @author zhh
	 */
	private Map<String,List<String>> getTeacherByLessonAndGradeBatch(Map<String,List<String>> leaderNameMap,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String xn)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(orgIdScopeIdsmap==null || orgIdLessonIdmap==null)
		{
			return null;
		}
		if(StringUtils.isBlank(xn))
		{
			return null;
		}
		Set<String> scopeList = new HashSet<String>();//存放所有的scopeId
		if(orgIdScopeIdsmap!=null)
		{
			
			for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
			{
				 Set<String> s = entry.getValue();
					 if(s!=null && s.size()>0){
						 scopeList.addAll(s);
					 }
			}
			
		}
		Set<String> lessonList = new HashSet<String>(); //存放所有的lessonId
		if(orgIdLessonIdmap!=null){
			for (Map.Entry<String, String> entry : orgIdLessonIdmap.entrySet()) 
			{
				 String s = entry.getValue();
				 if(StringUtils.isNotBlank(s)){
					 List<String> eList = Arrays.asList(s.split(","));
					 if(eList!=null && eList.size()>0){
						 lessonList.addAll(eList);
					 }
				 }
			}
		}
		JSONObject obj = new JSONObject();
		obj.put("ids", new ArrayList(scopeList)); //currentLevels
		obj.put("lessonIds", new ArrayList(lessonList));
		obj.put("schoolId", schoolId);
		obj.put("xn", xn);
		obj.put("noIds", removeGIdList);
		List<JSONObject> list = orgManageDao.getTeacherByLessonAndGradeBatch(obj);
		//currentLevel_lessonId-name List
		Map<String,List<String>> scopeAndLessonNameMap = new HashMap<String, List<String>>();
		//得到老师对应的lessonId和currentLevel
		for(JSONObject json:list){
			int currentLevel = json.getIntValue("currentLevel");
			String lessonId = json.getString("lessonId");
			String name = json.getString("name");
			if(scopeAndLessonNameMap.containsKey(currentLevel+"_"+lessonId)){
				List<String> names = scopeAndLessonNameMap.get(currentLevel+"_"+lessonId);
				if(names!=null && !names.contains(name)){
					names.add(name);
					scopeAndLessonNameMap.put(currentLevel+"_"+lessonId, names);
				}
			}else{
				List<String> names = new ArrayList<String>();
				names.add(name);
				scopeAndLessonNameMap.put(currentLevel+"_"+lessonId, names);
			}
		}
		Map<String,List<String>> orgIdNameMap = new HashMap<String, List<String>>();
		for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
		{
			String orgId = entry.getKey();
			Set<String> currentLevelSetList = entry.getValue();
			String ll = orgIdLessonIdmap.get(orgId);
			List<String> lessonIdList = new ArrayList<String>();
			if(StringUtils.isNotBlank(ll)){
				lessonIdList = Arrays.asList(ll.split(","));
			}
			List<String> nameList = leaderNameMap.get(orgId);
			List<String> currentLevelList = new ArrayList<String>(currentLevelSetList);
			//一个机构下的currentLevel循环
			if(currentLevelList!=null && lessonIdList!=null){
				for(String c:currentLevelList){
					for(String l :lessonIdList){
						List<String> names = scopeAndLessonNameMap.get(c+"_"+l);
						if(names!=null && names.size()>0){
							for(String name:names){
								if(orgIdNameMap.containsKey(orgId)){
									List<String> sList = orgIdNameMap.get(orgId);
									if(!sList.contains(name) ){ //去重
										if(nameList==null || !nameList.contains(name)){
											sList.add(name);
											orgIdNameMap.put(orgId, sList);
										}
									}
								}else{
									if(nameList==null || !nameList.contains(name)){
										List<String> sList = new ArrayList<String>();
										sList.add(name);
										orgIdNameMap.put(orgId, sList);
									}
								}
							}
						
						}
				}
				}
		    }
		}
		return orgIdNameMap;
	}
/**
 * 教研组
 * @author zhh
 */
	@Override
	public JSONObject getResearchgroupInfo(JSONObject obj)throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		//String leaderNames = "";
		int memberNum = 0;
		returnObj.put("researchgroupLeader", new ArrayList<JSONObject>());
		returnObj.put("researchgroupNum", memberNum);
		returnObj.put("researchgroups", new ArrayList<JSONObject>());
		List<JSONObject> departments = new ArrayList<JSONObject>();
		List<JSONObject> departmentLeaderList = new ArrayList<JSONObject>();
		List<String> leaderName = new ArrayList<String>();
		String orgId = "";
		for(JSONObject dInfo:dInfos){
			orgId = dInfo.getString("orgId");
			JSONObject departmentLeader = new JSONObject();
			String departmentId = dInfo.getString("uuid");
			String departmentName = dInfo.getString("orgName");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			String createTime = dInfo.getString("createTime");
			returnObj.put("researchgroupId", departmentId);
			if(StringUtils.isBlank(departmentName)){
				returnObj.put("researchgroupName", "");
			}else{
				returnObj.put("researchgroupName", departmentName);
			}
			returnObj.put("createTime", createTime);
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					if(!leaderName.contains(name)){
						leaderName.add(name);
					}
				}
				if(StringUtils.isBlank(name)){
					departmentLeader.put("name", "");
				}
				else{
					departmentLeader.put("name", name);
				}
				departmentLeader.put("accountId", accountId);
				departmentLeaderList.add(departmentLeader);
				JSONObject departmentMember = (JSONObject) departmentLeader.clone();
				departmentMember.put("position", 1);
				departments.add(departmentMember);
			}
			
		}
		SortUtil.sortNameJSONList(departmentLeaderList,"name");
		returnObj.put("researchgroupLeader", departmentLeaderList);
		//获取机构科目
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("orgId", orgId);
		json.put("orgType", obj.getString("orgType"));
		//获取机构相关的科目
		List<JSONObject> lessonObjList = new ArrayList<JSONObject>();
		List<JSONObject> lessonList = orgManageDao.getOrgLessonList(json);
		List<String> lList = new ArrayList<String>();
		for(JSONObject lesson:lessonList){
			if(!lList.contains(lesson.getString("lessonId"))){
				lList.add(lesson.getString("lessonId"));
				JSONObject lessonObj = new JSONObject();
				lessonObj.put("lessonId", lesson.getString("lessonId"));
				if(StringUtils.isBlank(lesson.getString("name"))){
					lessonObj.put("lessonName", "");
				}else{
					lessonObj.put("lessonName", lesson.getString("name"));
				}
				lessonObjList.add(lessonObj);
			}
			
		}
		returnObj.put("lesson", lessonObjList);
		List<JSONObject> mList = new ArrayList<JSONObject>();
		if(lessonObjList.size()>0){
			mList = getTeacherByLessonAndGrade(leaderName,new ArrayList<String>(),null,lList,schoolId,obj.getString("xn"));
		}
		if(mList!=null){
			mList.addAll(departments);
			returnObj.put("researchgroupNum", mList.size());
			//中文排序
			SortUtil.sortNameJSONList(mList,"name");
			returnObj.put("researchgroups", mList);
		}
		return returnObj;
	}


	@Override
	public List<JSONObject> getResearchgroupList(JSONObject obj)throws Exception {
		String xn = obj.getString("xn");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		List<JSONObject> dList = orgManageDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderNameMap = new HashMap<String, List<String>>();
		//uuid-JSONObject
		Map<String,List<JSONObject>> orgLeaderMap = new HashMap<String,List<JSONObject>>();
		for(JSONObject dInfo:dInfos){
			String departmentId = dInfo.getString("orgId");
			String uuid = dInfo.getString("uuid");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			JSONObject dJson = new JSONObject();
			if(StringUtils.isBlank(name)){
				dJson.put("name", "");
			}else{
				dJson.put("name", name);
			}
			dJson.put("accountId", accountId);
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					if(leaderNameMap.get(departmentId)==null){
						List<String> nameStringList = new ArrayList<String>();
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}else{
						List<String> nameStringList = leaderNameMap.get(departmentId);
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}
				}
				if(orgLeaderMap.get(uuid)==null){
					List<JSONObject> dJsonList = new ArrayList<JSONObject>();
					dJsonList.add(dJson);
					orgLeaderMap.put(uuid, dJsonList);
				}else{
					List<JSONObject> dJsonList = orgLeaderMap.get(uuid);
					dJsonList.add(dJson);
					orgLeaderMap.put(uuid, dJsonList);
				}
			}
			
		}
		//获取机构-科目数据
		Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();
		Map<String,String> orgIdLessonNamesMap = new HashMap<String, String>();
		List<JSONObject> olList = orgManageDao.getOrgLessonList(obj);
		List<String> removeGIdList = new ArrayList<String>();
		if(olList!=null){
			for(JSONObject ol:olList){
				String orgId = ol.getString("orgId");
				String lessonId = ol.getString("lessonId");
				String name = ol.getString("name");
				String ids = "";
				String names = "";
				if(orgIdLessonIdsmap.containsKey(orgId)){
					ids = orgIdLessonIdsmap.get(orgId);	
				}
				if(StringUtils.isNotBlank(lessonId)){
					ids+= lessonId +",";
					orgIdLessonIdsmap.put(orgId, ids);
				}
				if(orgIdLessonNamesMap.containsKey(orgId)){
					names = orgIdLessonNamesMap.get(orgId);	
				}
				if(StringUtils.isNotBlank(name)){
					names+=name+",";
					orgIdLessonNamesMap.put(orgId, names);
				}
			}
		}
		//获取机构成员数据
		Map<String,List<String>> mMap = getTeacherByLessonOrGradeBatch(leaderNameMap,removeGIdList,null,orgIdLessonIdsmap,schoolId,xn);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String uuid = d.getString("uuid");
			String departmentName = d.getString("orgName");
			d.put("lessonId", "");
			d.put("lessonName", "");
			d.put("researchgroupLeader", new ArrayList<JSONObject>());
			d.put("researchgroupId", departmentId);
			d.put("researchgroupName", departmentName);
			d.put("researchgroupNum", 0);
			if(orgLeaderMap.get(uuid)!=null ){
				List<JSONObject> departmentLeader = orgLeaderMap.get(uuid);
				if(departmentLeader!=null){
					SortUtil.sortNameJSONList(departmentLeader,"name");
					d.put("researchgroupLeader", departmentLeader);
					d.put("researchgroupNum", departmentLeader.size());
				}
			}
			if(mMap!=null && mMap.get(departmentId)!=null &&  mMap.get(departmentId).size()>0){
				List<String> names = mMap.get(departmentId);
				int gradegroupNum = d.getIntValue("researchgroupNum");
				d.put("researchgroupNum", names.size()+gradegroupNum);
			}
			if(orgIdLessonIdsmap.get(departmentId)!=null){
				String idList = orgIdLessonIdsmap.get(departmentId);
				if(!StringUtils.isBlank(idList)){
					idList=idList.substring(0, idList.length()-1);
					d.put("lessonId", idList);
				}
			}
			if(orgIdLessonNamesMap.get(departmentId)!=null){
				String nameList = orgIdLessonNamesMap.get(departmentId);
				if(!StringUtils.isBlank(nameList)){
					nameList=nameList.substring(0, nameList.length()-1);
					d.put("lessonName", nameList);
				}
			}
			d.put("orgId",uuid);
			d.put("researchgroupId", uuid);
		}
		return dList;
	}
	@Override
	public int deleteResearchgroup(JSONObject obj)throws Exception {
		List<String> ids = (List<String>) obj.get("ids");
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		List<String> orgIds = new ArrayList<String>();
		if(oList!=null){
			for(JSONObject o:oList){
				String orgId = o.getString("orgId");
				if(StringUtils.isNotBlank(orgId)){
					if(!orgIds.contains(orgId)){
						orgIds.add(orgId);
					}
				}
			}
		}
		if(ids!=null && ids.size()>0 && orgIds.size()>0){
			orgManageDao.deleteOrg(obj);
			obj.put("ids", orgIds);
			//删除staff
			//获取本学校所有leader的userId
			List<String> staffIds = orgManageDao.getStaffIdsList(obj);
			//查找staffIds在所有的包括将删除的orgId的leader
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(obj);
			//staffIds-staffIdss删除这部分人
			if(staffIds!=null && staffIdsNotInOrg!=null){
				staffIds.removeAll(staffIdsNotInOrg);
			}
			orgManageDao.deleteOrgMember(obj);
			orgManageDao.deleteOrgLeader(obj);
			orgManageDao.deleteSchoolOrg(obj);
			orgManageDao.deleteOrgLesson(obj);
			if(staffIds!=null && staffIds.size()>0){
				obj.put("ids", staffIds);
				orgManageDao.deleteStaff(obj);
			}
		}
		return 1;
	}
	@Override
	public JSONObject updateResearchgroup(JSONObject obj)throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		String uuid = obj.getString("uuid");
		 //判断传入的机构是否有重复的名称
		if(StringUtils.isNotBlank(uuid)){
			obj.put("noUuid", uuid);
		}
		List<JSONObject> oNameList = orgManageDao.getOrgObj(obj);
		if(oNameList!=null  && oNameList.size()>0){
			returnObj.put("returnNum", -1);
			return returnObj;
		}
		String  createUuid = UUIDUtil.getUUID();
		//判断插入的科目是否有重复
		List<String> lIdList = (List<String>) obj.get("lessonId");
		JSONObject scopeObj = new JSONObject(); //获取当前学校的所有已设置的年级列表
		scopeObj.put("schoolId", schoolId);
		scopeObj.put("xn", obj.getString("xn"));
		scopeObj.put("orgType", obj.getString("orgType"));
		if(StringUtils.isBlank(uuid)){
			scopeObj.put("noUuid", createUuid);
		}else{
			scopeObj.put("noUuid", uuid);
		}
		List<JSONObject> lessonsList = orgManageDao.getOrgLessonList(scopeObj);
		Map<String,String> lessonMap = new LinkedHashMap<String, String>();
		List<String> setedlIds = new ArrayList<String>();
		for(JSONObject lesson:lessonsList){
			setedlIds.add(lesson.getString("lessonId"));
			lessonMap.put(lesson.getString("lessonId"), lesson.getString("name"));
		}
		List<String> llList= new ArrayList<String>() ;
		for(String lId:lIdList){
			if(setedlIds.contains(lId)){
				llList.add(lId);
			}
		}
		if(llList.size()>0){
			String returnLessonName = "";
			for(String lId:llList){
				returnLessonName+=lessonMap.get(lId)+",";
			}
			returnLessonName = returnLessonName.substring(0,returnLessonName.length()-1);
			returnObj.put("returnNum", -2);
			returnObj.put("returnLessonName", returnLessonName);
			return returnObj;
		}
		
		Boolean isInsert = false;
		if(StringUtils.isBlank(uuid)){ //新增
			obj.put("uuid",createUuid);
			isInsert=true;
			//新增orgInfo
			orgManageDao.insertOrg(obj);
		}else{
			//更新orgInfo
			orgManageDao.updateOrg(obj);
		}
		if(isInsert){
			obj.put("isInsert", "1");
		}
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		if(oList==null || oList.size()<1|| oList.get(0)==null || StringUtils.isBlank(oList.get(0).getString("orgId"))){
			throw new Exception();
		}
		JSONObject o = oList.get(0);
		String orgId = o.getString("orgId");
		
		if(!isInsert){
			List<String> ids = new ArrayList<String>();
			ids.add(orgId);
			obj.put("ids", ids);
			if(ids.size()>0){
				//删除所有领导
				orgManageDao.deleteOrgLeader(obj);
				//删除所有科目
				orgManageDao.deleteOrgLesson(obj);
			}
			
		}
		//添加所有领导
		List<String> oldList = (List<String>) obj.get("oldOrgLeader");
		List<String> lList = (List<String>) obj.get("orgLeader");
		List<String> allAccountList = new ArrayList<String>();
		if(oldList!=null && oldList.size()>0){
			allAccountList.addAll(oldList);
		}else{
			oldList = new ArrayList<String>();
		}
		if(lList!=null && lList.size()>0){
			allAccountList.addAll(lList);
		}else{
			lList = new ArrayList<String>();
		}
		//List<String> userIdList = new ArrayList<String>();
		JSONObject json = new JSONObject();
		json.put("schoolId", obj.getString("schoolId"));
		json.put("ids", allAccountList);
		json.put("role", T_Role.Teacher.getValue());
		List<JSONObject> userList = new ArrayList<JSONObject>();
		if(allAccountList.size()>0){
			userList = orgManageDao.getUserIdByAccountIdList(json);
		}
		Map<String,String> accountIdUserIdMap = new HashMap<String,String>();
		for(JSONObject auObj:userList){
			String accountId = auObj.getString("accountId");
			String userId = auObj.getString("userId");
			if(StringUtils.isNotBlank(accountId) && StringUtils.isNotBlank(userId)){
				accountIdUserIdMap.put(accountId, userId);
			}
		}
		List<String> oldUserIdList = new ArrayList<String>();
		for(String oldAccountId:oldList){
			if(accountIdUserIdMap.get(oldAccountId)==null){continue;}
			if(!oldUserIdList.contains(accountIdUserIdMap.get(oldAccountId))){
				oldUserIdList.add(accountIdUserIdMap.get(oldAccountId));
			}
		}
		
		//获取该学校的所有staff
		List<JSONObject> ls = new ArrayList<JSONObject>();
		for(String lId:lList){
			JSONObject  lObj = new JSONObject();
			lObj.put("orgId", orgId);
			if(accountIdUserIdMap.get(lId)==null){continue;}
			lObj.put("userId", accountIdUserIdMap.get(lId));
			lObj.put("schoolId", schoolId);
			lObj.put("jobType", 0); //职务类型都默认不填 用于staff表的插入
			/*if(!userIdList.contains(accountIdUserIdMap.get(lId))){
				userIdList.add(accountIdUserIdMap.get(lId));
			}*/
			ls.add(lObj);
		}
		if(ls.size()>0){
			orgManageDao.insertOrgLeaderBatch(ls);
		}
		obj.put("orgId", orgId);
		JSONObject schoolOrg = orgManageDao.getSchoolOrg(obj);
		if(schoolOrg==null){
			orgManageDao.insertSchoolOrg(obj);
		}
		//新增所有科目
		
		if(lIdList!=null && lIdList.size()>0){
			List<JSONObject> lessons = new ArrayList<JSONObject>();
			JSONObject param = new JSONObject();
			param.put("ids", lIdList);
			List<JSONObject> lessonList = orgManageDao.getLessonList(param);
			for(JSONObject l:lessonList){
				JSONObject  lObj = new JSONObject();
				lObj.put("orgId", orgId);
				if(StringUtils.isBlank(l.getString("id"))){continue;}
				lObj.put("lessonId", l.getString("id"));
				lessons.add(lObj);
			}
			orgManageDao.insertOrgLessonBatch(lessons);
		}
		//删除staff
		if(!isInsert && oldUserIdList!=null && oldUserIdList.size()>0){
			JSONObject staffObj = new JSONObject();
			staffObj.put("schoolId", schoolId);
			staffObj.put("orgId", orgId);
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(staffObj);
			if(staffIdsNotInOrg!=null){
				oldUserIdList.removeAll(staffIdsNotInOrg);
			}
			obj.put("ids", oldUserIdList);
			if(oldUserIdList.size()>0){
				orgManageDao.deleteStaff(obj);
			}
		}
		//添加staff
		if(ls.size()>0){
			orgManageDao.insertStaffBatch(ls);
		}
		returnObj.put("returnNum", 1);
		return returnObj;
	}
/**
 * 备课组
 * @author zhh
 */
	@Override
	public List<JSONObject> getPreparationList(JSONObject obj)throws Exception {
		String xn = obj.getString("xn");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		List<JSONObject> dList = orgManageDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderNameMap = new HashMap<String, List<String>>();
		//uuid-JSONObject
		Map<String,List<JSONObject>> orgLeaderNameMap = new HashMap<String,List<JSONObject>>();
		for(JSONObject dInfo:dInfos){
			String departmentId = dInfo.getString("orgId");
			String uuid = dInfo.getString("uuid");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			JSONObject dJson = new JSONObject();
			if(StringUtils.isNotBlank(name)){
				dJson.put("name", name);
			}else{
				dJson.put("name", "");
			}
			dJson.put("accountId", accountId);
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isNotBlank(name)){
					if(leaderNameMap.get(departmentId)==null){
						List<String> nameStringList = new ArrayList<String>();
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}else{
						List<String> nameStringList = leaderNameMap.get(departmentId);
						nameStringList.add(name);
						leaderNameMap.put(departmentId, nameStringList);
					}
				}
				if(orgLeaderNameMap.get(uuid)==null){
					List<JSONObject> dJsonList = new ArrayList<JSONObject>();
					dJsonList.add(dJson);
					orgLeaderNameMap.put(uuid, dJsonList);
				}else{
					List<JSONObject> dJsonList = orgLeaderNameMap.get(uuid);
					dJsonList.add(dJson);
					orgLeaderNameMap.put(uuid, dJsonList);
				}
			}
			
		}
		//获取机构-年级数据
		Map<String,LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		Map<String,LinkedHashSet<String>> orgIdScopeNamesMap = new HashMap<String, LinkedHashSet<String>>();
		List<JSONObject> osList = orgManageDao.getOrgScopeList(obj);
		List<String> removeGIdList = new ArrayList<String>();
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		for(JSONObject os:osList){
			int currentLevel = os.getIntValue("currentLevel");
			Boolean isGraduate = GradeUtil.isGraduate(os.getInteger("createLevel"), os.getInteger("currentLevel"));
			if(isGraduate){
				if(StringUtils.isNotBlank(os.getString("gradeId"))){
					if(!removeGIdList.contains(os.getString("gradeId"))){
						removeGIdList.add(os.getString("gradeId"));
					}
				}
				continue;
			}
			String orgId = os.getString("orgId");
			LinkedHashSet<String> idSet =  new LinkedHashSet();
			LinkedHashSet<String> nameSet =  new LinkedHashSet();
			if(orgIdScopeIdsmap.containsKey(orgId)){
				idSet = orgIdScopeIdsmap.get(orgId);	
			}
			if(currentLevel!=0){
				idSet.add(currentLevel+"");
				orgIdScopeIdsmap.put(orgId, idSet);
			}
			if(orgIdScopeNamesMap.containsKey(orgId)){
				nameSet = orgIdScopeNamesMap.get(orgId);	
			}
			if(StringUtils.isNotBlank(njName.get(T_GradeLevel.findByValue(currentLevel)))){
				nameSet.add(njName.get(T_GradeLevel.findByValue(currentLevel)));
				orgIdScopeNamesMap.put(orgId, nameSet);
			}
		}
		//获取机构-科目数据
		Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();
		Map<String,String> orgIdLessonNamesMap = new HashMap<String, String>();
		List<JSONObject> olList = orgManageDao.getOrgLessonList(obj);
		for(JSONObject ol:olList){
			String orgId = ol.getString("orgId");
			String lessonId = ol.getString("lessonId");
			String name = ol.getString("name");
			String ids = "";
			String names = "";
			if(orgIdLessonIdsmap.containsKey(orgId)){
				ids = orgIdLessonIdsmap.get(orgId);	
			}
			if(StringUtils.isNotBlank(lessonId)){
				ids+= lessonId +",";
				orgIdLessonIdsmap.put(orgId, ids);
			}
			if(orgIdLessonNamesMap.containsKey(orgId)){
				names = orgIdLessonNamesMap.get(orgId);	
			}
			if(StringUtils.isNotBlank(name)){
				names+=name+",";
				orgIdLessonNamesMap.put(orgId, names);
			}
		}
		//获取机构成员数据
		Map<String,List<String>> mMap = getTeacherByLessonAndGradeBatch(leaderNameMap,removeGIdList,orgIdScopeIdsmap,orgIdLessonIdsmap,schoolId,xn);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String uuid = d.getString("uuid");
			String departmentName = d.getString("orgName");
			d.put("gradeId", "");
			d.put("gradeName", "");
			d.put("lessonId", "");
			d.put("lessonName", "");
			d.put("preparationLeader", new ArrayList<JSONObject>());
			d.put("preparationId", departmentId);
			d.put("preparationName", departmentName);
			d.put("preparationNum", 0);
			if(orgLeaderNameMap.get(uuid)!=null ){
				List<JSONObject> departmentLeader = orgLeaderNameMap.get(uuid);
				if(departmentLeader!=null){
					SortUtil.sortNameJSONList(departmentLeader,"name");
					d.put("preparationLeader", departmentLeader);
					d.put("preparationNum", departmentLeader.size());
				}
			}
			if(mMap!=null && mMap.get(departmentId)!=null &&  mMap.get(departmentId).size()>0){
				List<String> names = mMap.get(departmentId);
				int gradegroupNum = d.getIntValue("preparationNum");
				d.put("preparationNum", names.size()+gradegroupNum);
			}
			if(orgIdLessonIdsmap.get(departmentId)!=null){
				String idList = orgIdLessonIdsmap.get(departmentId);
				if(!StringUtils.isBlank(idList)){
					idList=idList.substring(0, idList.length()-1);
					d.put("lessonId", idList);
				}
			}
			if(orgIdLessonNamesMap.get(departmentId)!=null){
				String nameList = orgIdLessonNamesMap.get(departmentId);
				if(!StringUtils.isBlank(nameList)){
					nameList=nameList.substring(0, nameList.length()-1);
					d.put("lessonName", nameList);
				}
			}
			if(orgIdScopeIdsmap.get(departmentId)!=null){
				Set<String> idSetList = orgIdScopeIdsmap.get(departmentId);
				String idList = "";
				for(String id:idSetList){
					idList+=id+",";
				}
				if(!StringUtils.isBlank(idList)){
					idList=idList.substring(0, idList.length()-1);
					d.put("gradeId", idList);
				}
			}
			if(orgIdScopeNamesMap.get(departmentId)!=null){
				Set<String> nameSetList = orgIdScopeNamesMap.get(departmentId);
				String nameList="";
				for(String name:nameSetList){
					nameList+=name+",";
				}
				if(!StringUtils.isBlank(nameList)){
					nameList=nameList.substring(0, nameList.length()-1);
					d.put("gradeName", nameList);
				}
			}
			d.put("orgId",uuid);
			d.put("preparationId", uuid);
		}//end of for
		return dList;
	}

	@Override
	public JSONObject getPreparationInfo(JSONObject obj)throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgManageDao.getOrgInfos(obj);
		//String leaderNames = "";
		int memberNum = 0;
		returnObj.put("preparationLeader", new ArrayList<JSONObject>());
		returnObj.put("preparationNum", memberNum);
		returnObj.put("preparations", new ArrayList<JSONObject>());
		List<JSONObject> departments = new ArrayList<JSONObject>();
		List<JSONObject> departmentLeaderList = new ArrayList<JSONObject>();
		List<String> leaderName = new ArrayList<String>();
		String orgId = "";
		for(JSONObject dInfo:dInfos){
			orgId = dInfo.getString("orgId");
			//JSONObject departmentMember = new JSONObject();
			JSONObject departmentLeader = new JSONObject();
			String departmentId = dInfo.getString("uuid");
			String departmentName = dInfo.getString("orgName");
			String departmentType = dInfo.getString("orgType");
			String name = dInfo.getString("name");
			String accountId = dInfo.getString("accountId");
			String createTime = dInfo.getString("createTime");
			returnObj.put("preparationId", departmentId);
			if(StringUtils.isBlank(departmentName)){
				returnObj.put("preparationName", "");
			}else{
				returnObj.put("preparationName", departmentName);
			}
			returnObj.put("createTime", createTime);
			if(StringUtils.isBlank(accountId)){
				continue;
			}
			if(LEADERTYPE.equals(departmentType)){
				if(StringUtils.isBlank(name)){
					departmentLeader.put("name", "");
				}else{
					departmentLeader.put("name", name);
					if(!leaderName.contains(name)){
						leaderName.add(name);
					}
				}
				departmentLeader.put("accountId", accountId);
				departmentLeaderList.add(departmentLeader);
				JSONObject departmentMember = (JSONObject) departmentLeader.clone();
				departmentMember.put("position", 1);
				departments.add(departmentMember);
			}
		}
		SortUtil.sortNameJSONList(departmentLeaderList,"name");
		returnObj.put("preparationLeader", departmentLeaderList);
		//获取机构范围
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("orgId", orgId);
		json.put("xn", obj.getString("xn"));
		json.put("orgType", obj.getString("orgType"));
		//获取机构相关的年级
		List<JSONObject> scopeObjList = new ArrayList<JSONObject>();
		List<JSONObject> scopeList = orgManageDao.getOrgScopeList(json);
		List<String> gList = new ArrayList<String>();
		List<String> removeGIdList = new ArrayList<String>();
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		for(JSONObject scope:scopeList){
			Boolean isGraduate = GradeUtil.isGraduate(scope.getInteger("createLevel"), scope.getInteger("currentLevel"));
			if(isGraduate){
				if(StringUtils.isNotBlank(scope.getString("gradeId"))){
					if(!removeGIdList.contains(scope.getString("gradeId"))){
						removeGIdList.add(scope.getString("gradeId"));
					}
				}
				continue;
			}
			if(!gList.contains(scope.getString("currentLevel"))){
				gList.add(scope.getString("currentLevel"));
				JSONObject scopeObj=new JSONObject();
				scopeObj.put("gradeId", scope.getString("currentLevel"));
				if(StringUtils.isNotBlank(njName.get(T_GradeLevel.findByValue(scope.getIntValue("currentLevel"))))){
					scopeObj.put("gradeName", njName.get(T_GradeLevel.findByValue(scope.getIntValue("currentLevel"))));
				}else{
					scopeObj.put("gradeName", "");
				}
				scopeObjList.add(scopeObj);
			}
			
			
		}
		returnObj.put("grade", scopeObjList);
		//获取机构科目
		JSONObject json1 = new JSONObject();
		json1.put("schoolId", schoolId);
		json1.put("orgId", orgId);
		json1.put("orgType", obj.getString("orgType"));
		//获取机构相关的科目
		List<JSONObject> lessonObjList = new ArrayList<JSONObject>();
		List<JSONObject> lessonList = orgManageDao.getOrgLessonList(json1);
		List<String> lList = new ArrayList<String>();
		for(JSONObject lesson:lessonList){
			if(!lList.contains(lesson.getString("lessonId")))
			{
				lList.add(lesson.getString("lessonId"));
				JSONObject lessonObj = new JSONObject();
				lessonObj.put("lessonId", lesson.getString("lessonId"));
				if(StringUtils.isNotBlank(lesson.getString("name"))){
					lessonObj.put("lessonName", lesson.getString("name"));
				}else{
					lessonObj.put("lessonName", "");
				}
				lessonObjList.add(lessonObj);
			}
		}
		returnObj.put("lesson", lessonObjList);
		List<JSONObject> mList = new ArrayList<JSONObject>();
		if(scopeObjList.size()>0 && lessonObjList.size()>0){
			mList = getTeacherByLessonAndGrade(leaderName,removeGIdList,gList,lList,schoolId,obj.getString("xn"));
		}
		if(mList!=null){
			mList.addAll(departments);
			returnObj.put("preparationNum", mList.size());
			//中文排序
			SortUtil.sortNameJSONList(mList,"name");
			returnObj.put("preparations", mList);
		}
		return returnObj;
	}
	

	@Override
	public int deletePreparation(JSONObject obj)throws Exception {
		List<String> ids = (List<String>) obj.get("ids");
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		List<String> orgIds = new ArrayList<String>();
		for(JSONObject o:oList){
			String orgId = o.getString("orgId");
			if(StringUtils.isNotBlank(orgId)){
				if(!orgIds.contains(orgId)){
					orgIds.add(orgId);
				}
			}
		}
		if(ids!=null && ids.size()>0 && orgIds.size()>0){
			orgManageDao.deleteOrg(obj);
			obj.put("ids", orgIds);
			//删除staff
			//获取本学校所有leader的userId
			List<String> staffIds = orgManageDao.getStaffIdsList(obj);
			//查找staffIds在所有的包括将删除的orgId的leader
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(obj);
			//staffIds-staffIdss删除这部分人
			if(staffIds!=null && staffIdsNotInOrg!=null){
				staffIds.removeAll(staffIdsNotInOrg);
			}
			orgManageDao.deleteOrgMember(obj);
			orgManageDao.deleteOrgLeader(obj);
			orgManageDao.deleteSchoolOrg(obj);
			orgManageDao.deleteOrgLesson(obj);
			orgManageDao.deleteOrgScope(obj);
			if(staffIds.size()>0){
				obj.put("ids", staffIds);
				orgManageDao.deleteStaff(obj);
			}
			
		}
		return 1;
	}

	

	@Override
	public JSONObject updatePreparation(JSONObject obj)throws Exception {
		JSONObject returnObj = new JSONObject();
		String schoolId = obj.getString("schoolId");
		String uuid = obj.getString("uuid");
		 //判断传入的机构是否有重复的名称
		if(StringUtils.isNotBlank(uuid)){
			obj.put("noUuid", uuid);
		}
		List<JSONObject> oNameList = orgManageDao.getOrgObj(obj);
		if(oNameList!=null  && oNameList.size()>0){
			returnObj.put("returnNum", -1);
			return returnObj;
		}
		String createUuid = UUIDUtil.getUUID();
		//判断传入的年级、科目数据是否重复设置
		List<String> lIdList = (List<String>) obj.get("lessonId");
		List<String> gIdList = (List<String>) obj.get("gradeId");//传入的是currentLevel
		JSONObject scopeObj = new JSONObject(); //获取当前学校的所有已设置的年级列表
		scopeObj.put("schoolId", schoolId);
		scopeObj.put("xn", obj.getString("xn"));
		scopeObj.put("orgType", obj.getString("orgType"));
		if(StringUtils.isBlank(uuid)){ //新增
			scopeObj.put("noUuid", createUuid);
		}else{
			scopeObj.put("noUuid", uuid);
		}
		List<JSONObject> scopeLessonList = orgManageDao.getOrgScopeGradeList(scopeObj);
		List<String> setedCurrentLevelLesson = new ArrayList<String>();
		Map<String,String> lessonMap = new HashMap<String,String>();
		for(JSONObject scope:scopeLessonList){
			String lessonId = scope.getString("lessonId");
			String name = scope.getString("name");
			lessonMap.put(lessonId, name);
			setedCurrentLevelLesson.add(scope.getString("currentLevel")+"_"+lessonId);
		}
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		List<String> rList= new ArrayList<String>() ;
		for(String cl:gIdList){
			for(String lessonId:lIdList){
				String index = cl+"_"+lessonId;
				if(setedCurrentLevelLesson.contains(index)){
					rList.add(index);
				}
			}
		}
		
	
		if(rList.size()>0){
			String returnLessonName = "";
			String returnGradeName = "";
			String returnString = "";
			for(String rString:rList){
				int temp = rString.indexOf('_');
				String cl = rString.substring(0,temp);
				String lId = rString.substring(temp+1,rString.length());
				returnGradeName=njName.get(T_GradeLevel.findByValue(Integer.parseInt(cl)));
				returnLessonName=lessonMap.get(lId);
				returnString +=returnGradeName+returnLessonName+",";
			}
			returnString = returnString.substring(0,returnString.length()-1);
			returnObj.put("returnNum", -2);
			returnObj.put("returnString", returnString);
			return returnObj;
		}
		
		
		Boolean isInsert = false;
		if(StringUtils.isBlank(uuid)){ //新增
			obj.put("uuid",createUuid );
			isInsert=true;
			//新增orgInfo
			orgManageDao.insertOrg(obj);
		}else{
			//更新orgInfo
			orgManageDao.updateOrg(obj);
		}
		if(isInsert){
			obj.put("isInsert", "1");
		}
		List<JSONObject> oList = orgManageDao.getOrgList(obj);
		if(oList==null || oList.size()<1||oList.get(0)==null||StringUtils.isBlank(oList.get(0).getString("orgId"))){
			throw new Exception();
		}
		JSONObject o = oList.get(0);
		String orgId = o.getString("orgId");
		
		if(!isInsert){
			List<String> ids = new ArrayList<String>();
			ids.add(orgId);
			obj.put("ids", ids);
			if(ids.size()>0){
				//删除所有领导
				orgManageDao.deleteOrgLeader(obj);
				//删除所有年级
				orgManageDao.deleteOrgScope(obj);
				//删除所有科目
				orgManageDao.deleteOrgLesson(obj);
			}
			
		}
		//添加所有领导
		List<String> oldList = (List<String>) obj.get("oldOrgLeader");
		List<String> lList = (List<String>) obj.get("orgLeader");
		List<String> allAccountIds = new ArrayList<String>();
		if(oldList!=null && oldList.size()>0){
			allAccountIds.addAll(oldList);
		}else{
			oldList= new ArrayList<String>();
		}
		if(lList!=null && lList.size()>0){
			allAccountIds.addAll(lList);
		}else{
			lList= new ArrayList<String>();
		}
		JSONObject json = new JSONObject();
		json.put("schoolId", obj.getString("schoolId"));
		json.put("ids", allAccountIds);
		json.put("role", T_Role.Teacher.getValue());
		List<JSONObject> userList = new ArrayList<JSONObject>();
		if(allAccountIds.size()>0){
			userList = orgManageDao.getUserIdByAccountIdList(json);
		}
		Map<String,String> accountIdUserIdMap = new HashMap<String,String>();
		for(JSONObject auObj:userList){
			String accountId = auObj.getString("accountId");
			String userId = auObj.getString("userId");
			if(StringUtils.isNotBlank(accountId) && StringUtils.isNotBlank(userId)){
				accountIdUserIdMap.put(accountId, userId);
			}
		}
		List<String> oldUserIdList = new ArrayList<String>();
		for(String oldAccountId:oldList){
			if(accountIdUserIdMap.get(oldAccountId)==null){continue;}
			if(!oldUserIdList.contains(accountIdUserIdMap.get(oldAccountId))){
				oldUserIdList.add(accountIdUserIdMap.get(oldAccountId));
			}
		}
		//获取该学校的所有staff
		List<JSONObject> ls = new ArrayList<JSONObject>();
		//List<String> userIdList = new ArrayList<String>();
		for(String lId:lList){
			JSONObject  lObj = new JSONObject();
			lObj.put("orgId", orgId);
			if(accountIdUserIdMap.get(lId)==null){continue;}
			lObj.put("userId", accountIdUserIdMap.get(lId));
			lObj.put("schoolId", schoolId);
			lObj.put("jobType", 0); //职务类型都默认不填 用于staff表的插入
			ls.add(lObj);
		}
		if(ls.size()>0){
			orgManageDao.insertOrgLeaderBatch(ls);
		}
		obj.put("orgId", orgId);
		JSONObject schoolOrg = orgManageDao.getSchoolOrg(obj);
		if(schoolOrg==null){
			orgManageDao.insertSchoolOrg(obj);
		}
		//新增所有年级
		/*JSONObject param = new JSONObject();
		if(gIdList!=null && gIdList.size()>0){
			param.put("ids", gIdList);
		}
		param.put("xn", obj.getString("xn"));
		List<JSONObject> gList = orgManageDao.getCurrentLevelGradeList(param);*/
		List<JSONObject> gList = new ArrayList<JSONObject>();
		if(gIdList!=null){
			for(String  gId:gIdList){
				JSONObject gObj = new JSONObject();
				if(StringUtils.isBlank(gId)){continue;}
				gObj.put("orgId", orgId);
				gObj.put("scopeId", gId);
				gList.add(gObj);
			}
		}
		if(gList.size()>0){
			orgManageDao.insertOrgScopeBatch(gList);
		}
		//新增所有科目
		JSONObject param = new JSONObject();
		List<JSONObject> lessons = new ArrayList<JSONObject>();
		param.put("xn", obj.getString("xn"));
		if(lIdList!=null && lIdList.size()>0){
			param.put("ids", lIdList);
		}
		List<JSONObject> lessonList = orgManageDao.getLessonList(param);
		for(JSONObject l:lessonList){
			JSONObject  lObj = new JSONObject();
			lObj.put("orgId", orgId);
			lObj.put("lessonId", l.getString("id"));
			lessons.add(lObj);
		}
		if(lessons.size()>0){
			orgManageDao.insertOrgLessonBatch(lessons);
		}
		//删除staff
		if(!isInsert && oldUserIdList!=null && oldUserIdList.size()>0){
			JSONObject staffObj = new JSONObject();
			staffObj.put("schoolId", schoolId);
			staffObj.put("orgId", orgId);
			List<String> staffIdsNotInOrg = orgManageDao.getStaffIdsListNotInOrg(staffObj);
			if(staffIdsNotInOrg!=null){
				oldUserIdList.removeAll(staffIdsNotInOrg);
			}
			obj.put("ids", oldUserIdList);
			if(oldUserIdList.size()>0){
				orgManageDao.deleteStaff(obj);
			}
		}
		//添加staff
		if(ls.size()>0){
			orgManageDao.insertStaffBatch(ls);
		}
		returnObj.put("returnNum", 1);
		return returnObj;
	}

	@Override
	public int addImportDepartmentBatch(Map<String, Object> needInsert) throws Exception {
		return orgManageDao.addImportDepartmentBatch(needInsert);
	}

	
	
}
