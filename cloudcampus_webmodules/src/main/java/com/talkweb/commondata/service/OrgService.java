package com.talkweb.commondata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import com.talkweb.commondata.dao.OrgCommonDao;
import com.talkweb.csbasedata.util.GradeUtil;

@Service("orgService")
public class OrgService {
	@Autowired
	private OrgCommonDao orgCommonDao;
	final String LEADERTYPE="1"; //领导
	final String MEMBERTYPE="2"; //成员
	
	public List<JSONObject> getGradegroupList(JSONObject obj) throws Exception {
		String termInfoId = obj.getString("termInfoId");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgCommonDao.getOrgInfos(obj);
		List<JSONObject> dList = orgCommonDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderIdMap = new HashMap<String, List<String>>();
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
				if(StringUtils.isNotBlank(accountId)){
					if(leaderIdMap.get(departmentId)==null){
						List<String> idStringList = new ArrayList<String>();
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}else{
						List<String> idStringList = leaderIdMap.get(departmentId);
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}
				}
			}
			
		} //end of for dInfos
		//获取机构-年级数据
		Map<String, LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		//orgId（不是uuid）-年级名称
		Map<String,LinkedHashSet<String>> orgIdScopeNamesMap = new HashMap<String, LinkedHashSet<String>>();
		List<JSONObject> osList = orgCommonDao.getOrgScopeList(obj);
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
		Map<String,List<String>> mMap = getTeacherByLessonOrGradeBatch(leaderIdMap,removeGIdList,orgIdScopeIdsmap,null,schoolId,termInfoId);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String departmentName = d.getString("orgName");
			d.put("orgId", departmentId);
			d.put("gradegroupName", departmentName);
			d.put("memberIds", new ArrayList<Long>());
			if(mMap!=null){
				List<String> ids = mMap.get(departmentId);
				List<Long> trueIds = new ArrayList<Long>();
				if(ids!=null){
					for(String id:ids){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				List<String> leaderIds = leaderIdMap.get(departmentId);
				if(leaderIds!=null){
					for(String id:leaderIds){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				d.put("memberIds", trueIds);
			}
		}
		return dList;
	}
	public List<JSONObject> getResearchgroupList(JSONObject obj)throws Exception {
		String termInfoId = obj.getString("termInfoId");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgCommonDao.getOrgInfos(obj);
		List<JSONObject> dList = orgCommonDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderIdMap = new HashMap<String, List<String>>();
		for(JSONObject dInfo:dInfos){
			String departmentId = dInfo.getString("orgId");
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
				if(StringUtils.isNotBlank(accountId)){
					if(leaderIdMap.get(departmentId)==null){
						List<String> idStringList = new ArrayList<String>();
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}else{
						List<String> idStringList = leaderIdMap.get(departmentId);
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}
				}
			}
			
		}
		//获取机构-科目数据
		Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();
		Map<String,String> orgIdLessonNamesMap = new HashMap<String, String>();
		List<JSONObject> olList = orgCommonDao.getOrgLessonList(obj);
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
		Map<String,List<String>> mMap = getTeacherByLessonOrGradeBatch(leaderIdMap,removeGIdList,null,orgIdLessonIdsmap,schoolId,termInfoId);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String departmentName = d.getString("orgName");
			d.put("orgId", departmentId);
			d.put("researchgroupName", departmentName);
			d.put("memberIds", new ArrayList<Long>());
			if(mMap!=null){
				List<String> ids = mMap.get(departmentId);
				List<Long> trueIds = new ArrayList<Long>();
				if(ids!=null){
					for(String id:ids){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				List<String> leaderIds = leaderIdMap.get(departmentId);
				if(leaderIds!=null){
					for(String id:leaderIds){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				d.put("memberIds", trueIds);
			}
		}
		return dList;
	}
	public List<JSONObject> getPreparationList(JSONObject obj)throws Exception {
		String termInfoId = obj.getString("termInfoId");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = orgCommonDao.getOrgInfos(obj);
		List<JSONObject> dList = orgCommonDao.getOrgList(obj);
		//departmentId-String
		Map<String,List<String>> leaderIdMap = new HashMap<String, List<String>>();
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
				if(StringUtils.isNotBlank(accountId)){
					if(leaderIdMap.get(departmentId)==null){
						List<String> idStringList = new ArrayList<String>();
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}else{
						List<String> idStringList = leaderIdMap.get(departmentId);
						idStringList.add(accountId);
						leaderIdMap.put(departmentId, idStringList);
					}
				}
			}
			
		}
		//获取机构-年级数据
		Map<String,LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		Map<String,LinkedHashSet<String>> orgIdScopeNamesMap = new HashMap<String, LinkedHashSet<String>>();
		List<JSONObject> osList = orgCommonDao.getOrgScopeList(obj);
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
		List<JSONObject> olList = orgCommonDao.getOrgLessonList(obj);
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
		Map<String,List<String>> mMap = getTeacherByLessonAndGradeBatch(leaderIdMap,removeGIdList,orgIdScopeIdsmap,orgIdLessonIdsmap,schoolId,termInfoId);
		//返回数据
		for(JSONObject d:dList){
			String departmentId = d.getString("orgId");
			String departmentName = d.getString("orgName");
			d.put("orgId", departmentId);
			d.put("preparationName", departmentName);
			d.put("memberIds", new ArrayList<Long>());
			if(mMap!=null && mMap.get(departmentId)!=null &&  mMap.get(departmentId).size()>0){
				List<String> ids = mMap.get(departmentId);
				List<Long> trueIds = new ArrayList<Long>();
				if(ids!=null){
					for(String id:ids){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				List<String> leaderIds = leaderIdMap.get(departmentId);
				if(leaderIds!=null){
					for(String id:leaderIds){
						if(!trueIds.contains(Long.parseLong(id))){
							trueIds.add(Long.parseLong(id));
						}
					}
				}
				d.put("memberIds", trueIds);
			}
		}//end of for
		return dList;
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
	private Map<String,List<String>> getTeacherByLessonOrGradeBatch(Map<String,List<String>> leaderIdMap,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String termInfoId)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(orgIdScopeIdsmap==null && orgIdLessonIdmap==null)
		{
			return null;
		}
		if(StringUtils.isBlank(termInfoId))
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
		obj.put("xn", termInfoId.substring(0,4));
		obj.put("noIds", removeGIdList);
		obj.put("termInfoId", termInfoId);
		List<JSONObject> list = orgCommonDao.getTeacherByLessonAndGradeBatch(obj);
		//currentLevel-name List
		Map<String,List<String>> scopeNameMap = new HashMap<String, List<String>>();
		//lessonId-name List
		Map<String,List<String>> lessonNameMap = new HashMap<String, List<String>>();
		//得到老师对应的lessonId和currentLevel
		for(JSONObject json:list){
			int currentLevel = json.getIntValue("currentLevel");
			String lessonId = json.getString("lessonId");
			String name = json.getString("accountId");
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
				List<String> nameList = leaderIdMap.get(orgId);
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
				List<String> nameList = leaderIdMap.get(orgId);
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
	private Map<String,List<String>> getTeacherByLessonAndGradeBatch(Map<String,List<String>> leaderIdMap,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String termInfoId)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(orgIdScopeIdsmap==null || orgIdLessonIdmap==null)
		{
			return null;
		}
		if(StringUtils.isBlank(termInfoId))
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
		obj.put("noIds", removeGIdList);
		obj.put("xn", termInfoId.substring(0,4));
		obj.put("termInfoId", termInfoId);
		List<JSONObject> list = orgCommonDao.getTeacherByLessonAndGradeBatch(obj);
		//currentLevel_lessonId-name List
		Map<String,List<String>> scopeAndLessonNameMap = new HashMap<String, List<String>>();
		//得到老师对应的lessonId和currentLevel
		for(JSONObject json:list){
			int currentLevel = json.getIntValue("currentLevel");
			String lessonId = json.getString("lessonId");
			String name = json.getString("accountId");
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
			List<String> nameList = leaderIdMap.get(orgId);
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
}
