package com.talkweb.schedule.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.enums.EnumSubjectLevel;
import com.talkweb.placementtask.domain.TeachingClassInfo;
import com.talkweb.placementtask.service.PlacementTaskInternalService;
import com.talkweb.schedule.action.ScheduleAction;
import com.talkweb.schedule.dao.ScheduleExternalDao;
import com.talkweb.schedule.entity.TSchScheduleGradeset;
import com.talkweb.schedule.entity.TSchStudentTclassRelationship;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.entity.TSchTask;
import com.talkweb.schedule.entity.TSchTclass;
import com.talkweb.schedule.service.ScheduleExternalService;
import com.talkweb.utils.HttpClientUtil;

@Service
public class ScheduleExternalServiceImpl implements ScheduleExternalService {
	Logger logger = LoggerFactory.getLogger(ScheduleExternalServiceImpl.class);
	String rootPath = ScheduleAction.rootPath;
	@Autowired
	private ScheduleExternalDao externalDao;

	@Autowired
	private PlacementTaskInternalService placementTaskInternalService;

	@Autowired
	private AllCommonDataService commonDataService;

	private Set<Long> fixedSubjIdSet = new HashSet<Long>(); // 固定科目
	{
		fixedSubjIdSet.add(4L);
		fixedSubjIdSet.add(5L);
		fixedSubjIdSet.add(6L);
		fixedSubjIdSet.add(7L);
		fixedSubjIdSet.add(8L);
		fixedSubjIdSet.add(9L);
	}

	@Override
	public List<String> getChangedTclassList(String schoolId, String placementId) {
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("placementId", placementId);
		JSONObject ret = HttpClientUtil.postAction(rootPath + "external/getChangedTclassList", param);
		if (ret != null && ret.getIntValue("code") >= 0) {
			JSONArray arr = ret.getJSONArray("data");
			List<String> list = new ArrayList<>();
			for (int i = 0; i < arr.size(); i++) {
				list.add(arr.getString(i));
			}
			return list;
		}
		return null;
	}

	@Override
	public int updateTclass(String schoolId, String placementId, int placementType, 
			String termInfo, List<String> tClassIds) {
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("placementId", placementId);
		param.put("placementType", placementType);
		param.put("placementTermInfo", termInfo);
		param.put("tClassIds", tClassIds);
		JSONObject ret = HttpClientUtil.postAction(rootPath + "external/updateTclass", param);
		if (ret != null && ret.getIntValue("code") >= 0) {
			return 1;
		}
		return -1;
	}

	@Override
	public List<JSONObject> getScheduleForExam(Long schoolId,
			String termInfoId, String gradeId) {
		List<JSONObject> result = new ArrayList<JSONObject>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("schoolYear", termInfoId.substring(0, 4));
		map.put("termInfoId", termInfoId.substring(4));
		map.put("gradeId", gradeId);
		
		// 管理员身份
		// map.put("published", "1");
		List<JSONObject> scheduleList = externalDao.getScheduleForExam(map);
		for (JSONObject schedule : scheduleList) {
			JSONArray tclassIdList = schedule.getJSONArray("tclassIdList");
			if (tclassIdList != null && tclassIdList.size() < 1) {
				continue;
			}
			JSONObject singleResult = new JSONObject();
			singleResult.put("scheduleId",
					new String(schedule.getString("scheduleId")));
			singleResult.put("scheduleName",
					new String(schedule.getString("scheduleName")));
			result.add(singleResult);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getScheduleSubjectForExam(String scheduleId,
			Long schoolId, String termInfoId, String usedGrade) {
		JSONObject params = new JSONObject();
		params.put("scheduleId", scheduleId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("usedGrade", usedGrade);
		params.put("termInfoId", termInfoId.substring(4));
		params.put("schoolYear", termInfoId.substring(0, 4));

		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoList(
				school, termInfoId);

		Map<Long, String> lessonId2Name = new HashMap<Long, String>();
		for (LessonInfo lessonInfo : lessonInfoList) {
			if (lessonInfo.getType() == 2) {
				continue;
			}
			lessonId2Name.put(lessonInfo.getId(), lessonInfo.getName());
		}

		Map<String, Object[]> subjStrMap = new HashMap<String, Object[]>(); // 防止重复

		List<JSONObject> subjIdAndLevelList = externalDao
				.querySubjLevelAndIdInClass(params);
		for (JSONObject json : subjIdAndLevelList) {
			String groupFromSubs = json.getString("groupFromSubs");
			int subjectLevel = json.getIntValue("subjectLevel");
			if (subjectLevel == 10) { // 三三固定组合
				Set<Long> subjIds = StringUtil.convertToSetFromStr(
						groupFromSubs, ",", Long.class);
				for (Long subjectId : subjIds) {
					String key = subjectId + "_31";
					if (!subjStrMap.containsKey(key)) {
						subjStrMap.put(key, new Object[] { subjectId, 31 });
					}
				}
				for (Long subjectId : fixedSubjIdSet) {
					if (subjIds.contains(subjectId)) {
						continue;
					}
					String key = subjectId + "_32";
					if (!subjStrMap.containsKey(key)) {
						subjStrMap.put(key, new Object[] { subjectId, 32 });
					}
				}
			} else {
				String key = groupFromSubs + "_" + subjectLevel;
				if (!subjStrMap.containsKey(key)) {
					subjStrMap.put(key,
							new Object[] { Long.parseLong(groupFromSubs),
									subjectLevel });
				}
			}
		}

		List<Long> subjIdList = externalDao.querySubjIdInTSchTask(params);
		for (Long subjectId : subjIdList) {
			String key = subjectId + "_0";
			if (!subjStrMap.containsKey(key)) {
				subjStrMap.put(key, new Object[] { subjectId, 0 });
			}
		}

		List<Object[]> subjInfoList = new ArrayList<Object[]>();
		subjInfoList.addAll(subjStrMap.values());

		Collections.sort(subjInfoList, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				Long subjectId1 = (Long) o1[0];
				Integer subjectLevel1 = (Integer) o1[1];

				Long subjectId2 = (Long) o2[0];
				Integer subjectLevel2 = (Integer) o2[1];

				int result = Long.compare(subjectId1, subjectId2);
				if (result != 0) {
					return result;
				}

				return Integer.compare(subjectLevel1, subjectLevel2);
			}
		});

		List<JSONObject> result = new ArrayList<JSONObject>();
		Map<Long, JSONObject> subjId2Data = new HashMap<Long, JSONObject>();
		for (Object[] obj : subjInfoList) {
			long subjectId = (Long) obj[0];
			int subjectLevel = (Integer) obj[1];

			if(!lessonId2Name.containsKey(subjectId)) {
				continue;
			}
			
			if (!subjId2Data.containsKey(subjectId)) {
				JSONObject subjItem = new JSONObject();
				subjItem.put("subjectId", subjectId);
				subjItem.put("subjectName", lessonId2Name.get(subjectId));
				subjItem.put("subjectType", new ArrayList<JSONObject>());
				subjId2Data.put(subjectId, subjItem);
				result.add(subjItem);
			}
			JSONObject subjectType = new JSONObject();
			subjectType.put("typeText",
					EnumSubjectLevel.findNameByValue(subjectLevel));
			subjectType.put("subjectLevel", subjectLevel);
			((List<JSONObject>) subjId2Data.get(subjectId).get("subjectType"))
					.add(subjectType);
		}
		
		return result;
	}

	@Override
	public List<TSchTClassInfoExternal> getTClassInfoExternal(
			String scheduleId, Long schoolId, String termInfoId,
			String usedGrade, Collection<String> tclassIds) {
		return getTClassInfoExternal(scheduleId, schoolId, termInfoId,
				usedGrade, tclassIds, true);
	}

	@Override
	public List<TSchTClassInfoExternal> getTClassInfoExternalNoAccount(
			String scheduleId, Long schoolId, String termInfoId,
			String usedGrade, Collection<String> tclassIds) {
		return getTClassInfoExternal(scheduleId, schoolId, termInfoId,
				usedGrade, tclassIds, false);
	}

	private List<TSchTClassInfoExternal> getTClassInfoExternal(
			String scheduleId, Long schoolId, String termInfoId,
			String usedGrade, Collection<String> tclassIds, boolean hasStudIds) {
		List<TSchTClassInfoExternal> result = new ArrayList<TSchTClassInfoExternal>();
		JSONObject params = new JSONObject();
		params.put("scheduleId", scheduleId);
		params.put("schoolId", String.valueOf(schoolId));
		params.put("usedGrade", usedGrade);
		params.put("gradeId", usedGrade);
		params.put("termInfoId", termInfoId.substring(4));
		params.put("schoolYear", termInfoId.substring(0, 4));

		TSchScheduleGradeset gradeSet = externalDao
				.queryPlacementInfoByGrade(params);
		if (gradeSet == null) {
			return result;
		}

		if (CollectionUtils.isNotEmpty(tclassIds)) {
			params.put("tclassIds", tclassIds);
		}

		List<TSchTask> taskList = externalDao.queryTSchTask(params);

		Map<String, Set<Long>> tclassId2SubjIdSet = new HashMap<String, Set<Long>>();
		for (TSchTask task : taskList) {
			Long subjectId = Long.valueOf(task.getSubjectId());
			String tclassId = task.getTclassId();
			if (!tclassId2SubjIdSet.containsKey(tclassId)) {
				tclassId2SubjIdSet.put(tclassId, new HashSet<Long>());
			}
			tclassId2SubjIdSet.get(tclassId).add(subjectId);
		}
		
		// 获取教学班对应的名字
		List<TeachingClassInfo> tClassList = placementTaskInternalService
				.getTeachingClassInfoList(gradeSet.getPlacementId(), schoolId,
						gradeSet.getPlacementTermInfo(),
						tclassId2SubjIdSet.keySet());
		List<TSchTclass>tclassList = externalDao.getTclassList(params);
		List<String>tclassIdList = new ArrayList<String>();
		T_GradeLevel gl = T_GradeLevel
				.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, termInfoId.substring(0,4))));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, termInfoId);
		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), termInfoId);
		Map<String,String>classIdNameMap = new HashMap<String,String>();
		for(Classroom cr : classrooms) {
			classIdNameMap.put(String.valueOf(cr.getId()), cr.getClassName());
		}
		for(TSchTclass tclass : tclassList) {
			tclassIdList.add(tclass.getTclassId());
		}
		
		Map<String, TeachingClassInfo> clId2TClassInfo = new HashMap<String, TeachingClassInfo>();
		logger.info("exam getTeachingClassInfoList params:"+gradeSet.getPlacementId()+" schoolId "+schoolId +" gradeSet.getPlacementTermInfo() "+gradeSet.getPlacementTermInfo() +" tclassId2SubjIdSet.keySet() "+tclassId2SubjIdSet.keySet());
		logger.info("exam getTeachingClassInfoList results:"+tClassList.toString());
		logger.info("exam pId:"+gradeSet.getPlacementId()+" schoolId:"+schoolId);
		for (TeachingClassInfo tClass : tClassList) {
			clId2TClassInfo.put(tClass.getTeachingClassId(), tClass);
		}

		// 教学班级id对应学生id信息
		Map<String, List<Long>> clId2StudIds = new HashMap<String, List<Long>>();
		if (hasStudIds) {
			List<TSchStudentTclassRelationship> studRelationList = externalDao
					.queryStudentInfoList(params);
			for (TSchStudentTclassRelationship studInfo : studRelationList) {
				String tclassId = studInfo.getTclassId();
				if (!clId2StudIds.containsKey(tclassId)) {
					clId2StudIds.put(tclassId, new ArrayList<Long>());
				}
				clId2StudIds.get(tclassId).add(
						Long.valueOf(studInfo.getStudentId()));
			}
		}

		List<JSONObject> tClassInfoList = externalDao
				.queryTClassInfoList(params);
		if (CollectionUtils.isEmpty(tClassInfoList)) {
			return result;
		}
		logger.info("exam queryTClassInfoList params:==>"+params);
		logger.info("exam queryTClassInfoList results:==>"+tClassInfoList);
		for (JSONObject json : tClassInfoList) {
			String tclassId = json.getString("tclassId");
			if (!tclassId2SubjIdSet.containsKey(tclassId)) {
				logger.info("exam   continue tclassId for:"+tclassId +" not include tclassId2SubjIdSet" +tclassId2SubjIdSet);
				continue;
			}
			// 班级对应的所有科目
			Set<Long> allSubjIdSet = tclassId2SubjIdSet.get(tclassId);

			TSchTClassInfoExternal tclassInfo = new TSchTClassInfoExternal();
			tclassInfo.setTclassId(tclassId);

			TeachingClassInfo teachingClassInfo = clId2TClassInfo.get(tclassId);
			tclassInfo.setTclassName(teachingClassInfo == null ? "已删除"
					: teachingClassInfo.getTeachingClassName());
			if(teachingClassInfo==null && tclassIdList.contains(tclassId)) {
				tclassInfo.setTclassName(classIdNameMap.get(tclassId));	
			}
			
			logger.info("exam tclassId:"+tclassId +"tclassName" +" teachingClassInfo:"+teachingClassInfo);
			// 所有的学生代码
			List<Long> studIds = clId2StudIds.get(tclassId);
			if (studIds != null) {
				tclassInfo.setStudentIdList(studIds);
			}

			int type = json.getIntValue("type");
			if (type == 1) { // 三三固定组合
				Set<Long> zhSubjectIdSet = StringUtil.convertToSetFromStr(
						json.getString("groupFromSubs"), ",", Long.class);
				for (Long subjectId : allSubjIdSet) {
					if (fixedSubjIdSet.contains(subjectId)) { // 属于政史地物化生科目，这6门科目三三组合的
						if (zhSubjectIdSet.contains(subjectId)) { // 属于三三组合科目，则作为选考（高考）
							tclassInfo.addSubjectInfo(subjectId, 31);
						} else {
							tclassInfo.addSubjectInfo(subjectId, 32); // 不属于三三组合科目，则作为学考
						}
					} else {
						tclassInfo.addSubjectInfo(subjectId, 0); // 其他科目则作为普通科目
					}
				}
			} else if (type == 2) {
				for (Long subjectId : allSubjIdSet) {
					int subjectLevel = 0;
					if (fixedSubjIdSet.contains(subjectId)) { // 中走班中，按教学班开班，如果里面有政史地物化生，则都为学考
						subjectLevel = 32;
					}
					tclassInfo.addSubjectInfo(subjectId, subjectLevel);
				}
			} else if (type == 5||type == 6) { // 固定组合
				for (Long subjectId : allSubjIdSet) {
					tclassInfo.addSubjectInfo(subjectId, 0);
				}
			} else if (type == 3 || type == 4  || type == 7 ) { // 走班分层
				long subjectId = Long.valueOf(json.getString("groupFromSubs"));
				if (allSubjIdSet.contains(subjectId)) {
					int layCode = json.getIntValue("layCode");
					tclassInfo.addSubjectInfo(subjectId, type * 10 + layCode);
				}
			} else {
				
				System.out.println("------------type-----"+type);
				throw new CommonRunException(-1, "教学班参数类型错误，请联系管理员！");
			}
			result.add(tclassInfo);
		}
		logger.info("exam getTClassInfoExternal result is :"+result); 
		return result;
	}
}