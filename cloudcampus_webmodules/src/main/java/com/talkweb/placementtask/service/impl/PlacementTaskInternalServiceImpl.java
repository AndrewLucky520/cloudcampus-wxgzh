package com.talkweb.placementtask.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezySubjectgroup;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TeachingClassInfo;
import com.talkweb.placementtask.service.PlacementTaskInternalService;

@Service
public class PlacementTaskInternalServiceImpl implements PlacementTaskInternalService {
	Logger logger = LoggerFactory.getLogger(PlacementTaskInternalServiceImpl.class);

	@Value("#{configProperties['db_name_placementtask']}")
	private String prefixSchema;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private PlacementTaskDao placementTaskDao;
	
	@Override
	public List<JSONObject> getGradePlacementList(String usedGrade, Long schoolId, String termInfo){
		String xn = termInfo.substring(0, termInfo.length() - 1);
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		if(gl == null || !(gl.equals(T_GradeLevel.T_HighOne) || gl.equals(T_GradeLevel.T_HighTwo) || gl.equals(T_GradeLevel.T_HighThree))) {
			throw new CommonRunException(-1, "入参错误，计算后的年级代码不为高中年级！");
		}
		
		int xnInterval = gl.getValue() + 1 - T_GradeLevel.T_HighOne.getValue();
		String xq = termInfo.substring(termInfo.length() - 1);
		
		Set<String> dbSchemas = placementTaskDao.getPlacementTaskSchemas(prefixSchema);
		
		List<String> schemas = new ArrayList<String>();
		for(int i = 0, schoolYearId = Integer.parseInt(xn); i < xnInterval; i ++, schoolYearId --) {
			int termId;
			if(i == 0 && "1".equals(xq)) {
				termId = 1;
			} else {
				termId = 2;
			}
			for(;termId > 0; termId --) {
				String schema = prefixSchema + "_" + schoolYearId + termId;
				if(dbSchemas.contains(schema)){
					schemas.add(schema);
				}
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", String.valueOf(schoolId));
		map.put("usedGrade", usedGrade);
		map.put("schemas", schemas);
		return placementTaskDao.getPlacementTaskDropDownList(map);
	}
	
	@Override
	public List<JSONObject> getSubjectOrGroupList(String placementId, Long schoolId, String termInfo){
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return new ArrayList<JSONObject>();
		}
		params.put("placementType", pl.getPlacementType());
		
		List<JSONObject> data = new ArrayList<JSONObject>();
		
		params.put("types", new Object[]{1, 3, 4});	// 三三固定组合/按志愿单科/分层单科
		
		List<OpenClassInfo> opClassInfoList = placementTaskDao.queryOpenClassInfo(params);
		for(OpenClassInfo opClassInfo : opClassInfoList) {
			JSONObject json = new JSONObject();
			json.put("id", opClassInfo.getSubjectIdsStr());
			json.put("name", opClassInfo.getZhName());
			json.put("type", opClassInfo.getType());
			data.add(json);
		}
		return data;
	}
	
	@Override
	public String getTeachingClassNameById(String placementId, Long schoolId, String termInfo, String teachingClassId){
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return null;
		}
		params.put("placementType", pl.getPlacementType());
		
		List<TeachingClassInfo> tClassInfoList = placementTaskDao.queryTeachingClassInfo(params);
		if(CollectionUtils.isEmpty(tClassInfoList)){
			return null;
		}
		return tClassInfoList.get(0).getTeachingClassName();
	}
	
	@Override
	public List<TeachingClassInfo> getTeachingClassInfoList(String placementId, Long schoolId, String termInfo) {
//		JSONObject params = new JSONObject();
//		params.put("placementId", placementId);
//		params.put("schoolId", String.valueOf(schoolId));
//		params.put("termInfo", termInfo);
//		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
//		if(pl == null){
//			return new ArrayList<TeachingClassInfo>();
//		}
//		params.put("placementType", pl.getPlacementType());
//		
//		List<TeachingClassInfo> tClassInfoList = placementTaskDao.queryTeachingClassInfo(params);
		
		return getTeachingClassInfoList(placementId, schoolId, termInfo, null);
	}
	
	@Override
	public List<TeachingClassInfo> getTeachingClassInfoList(String placementId, Long schoolId, String termInfo,
			Collection<String> tClassIds) {
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return new ArrayList<TeachingClassInfo>();
		}
		params.put("placementType", pl.getPlacementType());
		
		if(CollectionUtils.isNotEmpty(tClassIds)) {
			params.put("teachingClassIds", tClassIds);
		}
		
		return placementTaskDao.queryTeachingClassInfo(params);
	}
	
	@Override
	public List<OpenClassInfo> queryAllPlacementData(String placementId, Long schoolId, String termInfo){
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return new ArrayList<OpenClassInfo>();
		}
		params.put("placementType", pl.getPlacementType());
		
		Map<String, OpenClassInfo> opClassInfoId2Obj = new HashMap<String, OpenClassInfo>();
		
		List<OpenClassInfo> opClassInfoList = placementTaskDao.queryOpenClassInfo(params);
		for(OpenClassInfo opClassInfo : opClassInfoList) {
			String opClassInfoId = opClassInfo.getOpenClassInfoId();
			opClassInfoId2Obj.put(opClassInfoId, opClassInfo);
		}
		
		
		Map<String, OpenClassTask> opClassTaskId2Obj = new HashMap<String, OpenClassTask>();
		
		params.put("openClassInfoIds", opClassInfoId2Obj.keySet());
		List<OpenClassTask> opClassTaskList = placementTaskDao.queryOpenClassTask(params);
		params.remove("openClassInfoIds");
		for(OpenClassTask opClassTask : opClassTaskList) {
			OpenClassInfo opClassInfo = opClassInfoId2Obj.get(opClassTask.getOpenClassInfoId());
			if(opClassInfo == null) {
				continue;
			}
			opClassInfo.getOpenClassTasks().add(opClassTask);
			opClassTask.setOpenClassInfo(opClassInfo);
			
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			opClassTaskId2Obj.put(openClassTaskId, opClassTask);
		}
		opClassInfoId2Obj = null;
		
		
		Map<String, TeachingClassInfo> tClassId2Obj = new HashMap<String, TeachingClassInfo>();
		
		params.put("openClassTaskIds", opClassTaskId2Obj.keySet());
		List<TeachingClassInfo> tClassInfoList = placementTaskDao.queryTeachingClassInfo(params);
		params.remove("openClassTaskIds");
		for(TeachingClassInfo tClassInfo : tClassInfoList) {
			OpenClassTask opClassTask = opClassTaskId2Obj.get(tClassInfo.getOpenClassTaskId());
			if(opClassTask == null) {
				continue;
			}
			
			opClassTask.addTeachingClassInfo(tClassInfo);
			tClassInfo.setOpenClassTask(opClassTask);
			
			tClassId2Obj.put(tClassInfo.getTeachingClassId(), tClassInfo);
		}
		opClassTaskId2Obj = null;
		
		
		params.put("teachingClassIds", tClassId2Obj.keySet());
		List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfo(params);
		for(StudentInfo studInfo : studInfoList) {
			TeachingClassInfo tClassInfo = tClassId2Obj.get(studInfo.getTeachingClassId());
			if(tClassInfo == null) {
				continue;
			}
			tClassInfo.addStudentInfo(studInfo);
			studInfo.setTeachingClassInfo(tClassInfo);
		}
		return opClassInfoList;
	}
	
	@Override
	public List<StudentInfo> queryStudInfoListWaitForPlacement(String placementId, Long schoolId, String termInfo){
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return new ArrayList<StudentInfo>();
		}
		params.put("placementType", pl.getPlacementType());
		
		List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfoWaitForPlacement(params);
		return studInfoList;
	}
	
	@Override
	public List<JSONObject> getSubjectLevelList(String placementId, Long schoolId, String termInfo){
		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("termInfo", termInfo);
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(params);
		if(pl == null){
			return new ArrayList<JSONObject>();
		}
		Integer placementType = pl.getPlacementType();
		params.put("placementType", placementType);
		String usedGrade = pl.getUsedGrade();
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		List<OpenClassTask> opClassTaskList = placementTaskDao.queryOpenClassTasksWithInfo(params);
		for(OpenClassTask opClassTask : opClassTaskList) {
			OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
			JSONObject json = new JSONObject();
			json.put("usedGrade", usedGrade);
			json.put("subjectIds", opClassInfo.getSubjectIdsStr());
			
			String subjectLevel = String.valueOf(opClassInfo.getType()) + opClassTask.getSubjectLevel();
			json.put("subjectLevel", subjectLevel);
			if(subjectLevel.equals("31")){
				json.put("levelName", "学考");
			}else if(subjectLevel.equals("32")){
				json.put("levelName", "选考");
			}else if(subjectLevel.equals("41") || subjectLevel.equals("42") || subjectLevel.equals("43")){
				json.put("levelName", opClassTask.getLayName());
			}
			
			json.put("name", opClassInfo.getZhName());
			
			result.add(json);
		}
		
		return result;
	}

	@Override
	public JSONObject queryDezyResultForSchedule(String placementId,
			long schId, String xnxq) {
		// TODO Auto-generated method stub
		 
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId",  schId );
		map.put("placementId", placementId);
		map.put("termInfo", xnxq);
		
		//定二走一设置参数
		TPlDezySettings dezySet = placementTaskDao.getTPlDezySet(map);
		//定二走一 科目组
		List<TPlDezySubjectgroup> subjectGroupList = placementTaskDao.getSubjectGroupList(map);
		//定二走一 科目设置
		List<TPlDezySubjectSet> subjectSetList = placementTaskDao.getSubjectSetList(map);
		//定二走一 行政班组列表
		List<TPlDezyClassgroup> classGroupList = placementTaskDao.getDezyClassGroupList(map);
		//定二走一 班级列表
		List<TPlDezyClass> classList = placementTaskDao.getDezyClassList(map);
		//定二走一 科目组合
		List<TPlDezySubjectcomp> subjectcompList = placementTaskDao.getSubjectcompList(map);
		//定二走一 科目组合下的学生
		List<TPlDezySubjectcompStudent> subjectcompStuList = placementTaskDao.getSubjectcompStuList(map);
		//定二走一 班级构成
		List<TPlDezyTclassSubcomp> tclassSubcompList = placementTaskDao.getTclassSubcompList(map);
		//定二走一--教学班与行政班关联关系
		List<TPlDezyTclassfrom> tclassFromList = placementTaskDao.getTclassFromList(map);
		//大走班 上课科目冲突次序
		List<TPlConfIndexSubs> confIndexSubsList = placementTaskDao.getConfIndexSubsList(map);
		JSONObject rs = new JSONObject();
		rs.put("dezySet", dezySet);
		rs.put("subjectGroupList", subjectGroupList);
		rs.put("subjectSetList", subjectSetList);
		rs.put("classGroupList", classGroupList);
		rs.put("classList", classList);
		rs.put("subjectcompList", subjectcompList);
		rs.put("subjectcompStuList", subjectcompStuList);
		rs.put("tclassSubcompList", tclassSubcompList);
		rs.put("tclassFromList", tclassFromList);
		rs.put("confIndexSubsList", confIndexSubsList);
		return rs;
	}

	@Override
	public TPlDezySettings getDezySettings(String schoolId, String placementId,
			String gradeId, String xnxq) {
		// TODO Auto-generated method stub
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId",  schoolId );
		map.put("placementId", placementId);
		map.put("termInfo", xnxq);
		
		//定二走一设置参数
		TPlDezySettings dezySet = placementTaskDao.getTPlDezySet(map);
		return dezySet;
	}
}
