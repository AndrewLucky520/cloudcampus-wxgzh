package com.talkweb.exammanagement.service.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.dao.ExamManagementSetDao;
import com.talkweb.exammanagement.domain.ArrangeExamClassInfo;
import com.talkweb.exammanagement.domain.ArrangeExamLog;
import com.talkweb.exammanagement.domain.ArrangeExamPlaceInfo;
import com.talkweb.exammanagement.domain.ArrangeExamResult;
import com.talkweb.exammanagement.domain.ArrangeExamRule;
import com.talkweb.exammanagement.domain.ArrangeExamSubjectInfo;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.domain.ExamSubject;
import com.talkweb.exammanagement.domain.ExecProgressRedisMsg;
import com.talkweb.exammanagement.domain.StudNotTakingExam;
import com.talkweb.exammanagement.domain.TestNumberInfo;
import com.talkweb.exammanagement.service.ExamManagementArrangeExamService;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.entity.TSchTClassInfoExternal.TSchSubjectInfo;
import com.talkweb.schedule.service.ScheduleExternalService;

@Service
public class ExamManagementArrangeExamServiceImpl implements ExamManagementArrangeExamService {
	Logger logger = LoggerFactory.getLogger(ExamManagementArrangeExamServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ExamManagementDao examManagementDao;
	@Autowired
	private ExamManagementSetDao examManagementSetDao;

	@Autowired
	private ScheduleExternalService scheduleExternalService;

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@Autowired
	private DataSourceTransactionManager transactionManager;
	
	
	
	
	private final ExecutorService threadPool = new ThreadPoolExecutor(5, 500, 1L, TimeUnit.HOURS,
			new LinkedBlockingQueue<Runnable>());

	@SuppressWarnings("unchecked")
	public JSONObject getArrangeExamInfo(JSONObject request) {
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		Long schoolId = request.getLong("schoolId");
		Set<String> examSubjectIdSet = (Set<String>) request.remove("examSubjectIds"); // 选择的科目
		
		List<String> targetSubj = new ArrayList<String>(examSubjectIdSet); 
		Collections.sort(targetSubj);
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		request.put("autoIncr", autoIncr);
		request.put("examSubjectIds", examSubjectIdSet);
		List<ExamSubject> sublist=examManagementDao.getExamSubjectList(request, termInfo, autoIncr);//查出所选科目的开始时间等信息
		
		// 获取考试计划，主要获取使用年级和课表代码（通过课表代码判空，判断是否是新高考选课）
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(examPlanList)) {
			throw new CommonRunException(-1, "没有找到对应年级的考试计划，请前往排考中设置");
		}
		ExamPlan examPlan = examPlanList.get(0);
		String scheduleId = examPlan.getScheduleId(); // 课表代码
		String usedGrade = examPlan.getUsedGrade(); // 使用年级
		
		List<JSONObject> waitsubjectlist=examManagementSetDao.getstudsWaitingSubject(request);//不参考学生及科目
		Map<String,Map<String,List<String>>> waimap=new HashMap<String, Map<String,List<String>>>();
		for(JSONObject json:waitsubjectlist){
			if(waimap.containsKey(json.getString("tClassId"))){
				Map<String,List<String>> stumap=waimap.get(json.getString("tClassId"));
				if(stumap.containsKey(json.getString("accountId"))){
					List<String> sub=stumap.get(json.getString("accountId"));
					sub.add(json.getString("examSubjectId"));
				}else{
					List<String> sub=new ArrayList<String>();
					sub.add(json.getString("examSubjectId"));
					stumap.put(json.getString("accountId"), sub);
				}
			}else{
				Map<String,List<String>> stumap=new HashMap<String, List<String>>();
				List<String> sub=new ArrayList<String>();
				sub.add(json.getString("examSubjectId"));
				stumap.put(json.getString("accountId"), sub);
				waimap.put(json.getString("tClassId"), stumap);
			}
		}
		request.put("examSubjectIds", null);
		List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(request, termInfo, autoIncr);
		Map<String, ExamSubject> examSubjectId2Obj = new HashMap<String, ExamSubject>();
		if (CollectionUtils.isNotEmpty(examSubjectList)) {
			for (ExamSubject es : examSubjectList) {
				String examSubjectId = es.getExamSubjectId();
				examSubjectId2Obj.put(examSubjectId, es);
			}
		}
		int totalwait=0;
		int totalNumOfStuds = 0; // 排考总人数
		Map<String, JSONObject> tClassId2ObjMap = new HashMap<String, JSONObject>(); // 用于通过classId获取对应数据，年级下所有教学班级的数据
		Map<String, JSONObject> tClassValue = new HashMap<String, JSONObject>(); // 用于向前台传值，前面的classId2Obj包含tClassMap
		if (StringUtils.isBlank(scheduleId)) { // 判断是否为空，为空则从SDK获取班级数据

			List<Long> subjectFilter = new ArrayList<Long>();
			for (String id : examSubjectIdSet) { // 考试科目代码集合转换成科目代码
				ExamSubject es = examSubjectId2Obj.get(id);
				if(es!=null){
					subjectFilter.add(es.getSubjectId());
				}
			}

			T_GradeLevel gl = T_GradeLevel
					.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, termInfo);
			if (grade == null) {
				throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
			}
			if (CollectionUtils.isEmpty(grade.getClassIds())) {
				throw new CommonRunException(-1, "年级下没有班级，请联系管理员！");
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), termInfo);
			if (CollectionUtils.isEmpty(classrooms)) {
				throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
			}
			for (Classroom classroom : classrooms) {
				int wait=0;
				if(waimap.containsKey(classroom.getId()+"")){
					Map<String,List<String>> stum=waimap.get(classroom.getId()+"");
					 for (Entry<String, List<String>> entry : stum.entrySet()) {
						 List<String> sub=entry.getValue();
							Collections.sort(sub);
							Collections.sort(targetSubj);
							sub.retainAll(targetSubj);
							String userj=StringUtils.join(sub, ",");
							String targetj=StringUtils.join(targetSubj, ",");
							if(userj.contains(targetj)){
								wait++;
								totalwait++;
							}
					 }
				}
				JSONObject json = new JSONObject();
				json.put("tClassId", classroom.getId());
				json.put("tClassName", classroom.getClassName());
				json.put("studSize", classroom.getStudentAccountIdsSize()-wait);
				json.put("truestudSize", classroom.getStudentAccountIdsSize());
				// 安排后的科目也应该被显示出来，比如选择的拉开框是语文，但是数学对应的班级名称也应该显示在安排表里
				tClassId2ObjMap.put(String.valueOf(classroom.getId()), json);
				tClassValue.put(String.valueOf(classroom.getId()), json);
				totalNumOfStuds += classroom.getStudentAccountIdsSize();
				
//				List<AccountLesson> accountLessonList = classroom.getAccountLessons(); // 获取班级的任教关系
//				Set<Long> subjectSetInClass = new HashSet<Long>(); // 获取班级科目集合
//				if (CollectionUtils.isNotEmpty(accountLessonList)) {
//					for (AccountLesson accountLesson : accountLessonList) {
//						subjectSetInClass.add(accountLesson.getLessonId());
//					}
//				}
//
//				if (subjectSetInClass.containsAll(subjectFilter)) { // 如果班级中的科目包含全部的过滤条件，那么显示班级，并且计算总人数
//					tClassValue.put(String.valueOf(classroom.getId()), json);
//					totalNumOfStuds += classroom.getStudentAccountIdsSize();
//				}
			}
		} else {
			// 课表代码不为空，则从新高考获取班级数据
			// tClassId2ObjMap 年级下所有班级数据
			// tClassValue 通过 examSubjectIdSet 后所得到的班级数据
			Map<Long, List<Integer>> subjectFilter = new HashMap<Long, List<Integer>>();
			for (String id : examSubjectIdSet) { // 考试科目代码集合转换成科目代码与科目分层
				ExamSubject es = examSubjectId2Obj.get(id);
				if(es!=null){
					if(!subjectFilter.containsKey(es.getSubjectId())) {
						subjectFilter.put(es.getSubjectId(), new ArrayList<Integer>());
					}
					subjectFilter.get(es.getSubjectId()).add(es.getSubjectLevel());
				}
			
			}

			List<TSchTClassInfoExternal> classInfoList = scheduleExternalService.getTClassInfoExternal(scheduleId,
					schoolId, termInfo, usedGrade, null);
			for (TSchTClassInfoExternal classInfo : classInfoList) {
				List<TSchSubjectInfo> subjectInfoList = classInfo.getSubjectList();
				
				int wait=0;
				if(waimap.containsKey(classInfo.getTclassId())){
					Map<String,List<String>> stum=waimap.get(classInfo.getTclassId());
					 for (Entry<String, List<String>> entry : stum.entrySet()) {
						 List<String> sub=entry.getValue();
							Collections.sort(sub);
							Collections.sort(targetSubj);
							sub.retainAll(targetSubj);
							String userj=StringUtils.join(sub, ",");
							String targetj=StringUtils.join(targetSubj, ",");
							if(userj.contains(targetj)){
								wait++;
								totalwait++;
							}
					 }
				}
				JSONObject json = new JSONObject();
				json.put("tClassId", classInfo.getTclassId());
				json.put("tClassName", classInfo.getTclassName());
				json.put("studSize", classInfo.getStudentSize()-wait);
				json.put("truestudSize", classInfo.getStudentSize());
				// 安排后的科目也应该被显示出来，比如选择的拉开框是语文，但是数学对应的班级名称也应该显示在安排表里
				tClassId2ObjMap.put(classInfo.getTclassId(), json);
				
				Map<Long, Set<Integer>> subjId2Level = new HashMap<Long, Set<Integer>>();
				for (TSchSubjectInfo subjectInfo : subjectInfoList) {
					if (!subjId2Level.containsKey(subjectInfo.getSubjectId())) {
						subjId2Level.put(subjectInfo.getSubjectId(), new HashSet<Integer>());
					}
					subjId2Level.get(subjectInfo.getSubjectId()).add(subjectInfo.getSubjectLevel());
				}

				boolean isContained = true;
				for (Map.Entry<Long, List<Integer>> entry : subjectFilter.entrySet()) { 
					// subjectFilter中的科目代码必须都在班级所上科目里，科目层级或
					Long subjectId = entry.getKey();
					List<Integer> filterSubjLevelList = entry.getValue();
					
					if(!subjId2Level.containsKey(subjectId)) {	
						// 所有科目必须存在班级中，如果一个不满足，则这个班级不展示
						isContained = false;
						break;
					}
					
					if(filterSubjLevelList.contains(0)) {	
						// 如果层级是全部，则只需要判断是否满足科目
						continue;
					}
					
					// 班级中的科目层级包含了过滤层级（只需要包含一部分）
					Set<Integer> subjLevelList = subjId2Level.get(subjectId);
					// 移除filterSubjLevelList中的数据，求交集，只需要一个满足就可以展示班级
					subjLevelList.retainAll(filterSubjLevelList);
					if(subjLevelList.size() == 0) {	
						// 没有层次满足
						isContained = false;
						break;
					}
				}

				if (isContained) {
					tClassValue.put(classInfo.getTclassId(), json);
					totalNumOfStuds += classInfo.getStudentSize();
				}
			}
		}

		// 考试地点数据
		List<JSONObject> examPlaceList = examManagementDao.getExamPlaceInfoInArrange(request, termInfo, autoIncr);

		Map<String, String> placeId2NameMap = new HashMap<String, String>(); // 考场代码映射名字
		Map<String, JSONObject> placeValue = new HashMap<String, JSONObject>(); // 考场传值，使用Map方便删除
		Map<String, JSONObject> placeMap = new HashMap<String, JSONObject>();
		for (JSONObject json : examPlaceList) {
			placeId2NameMap.put(json.getString("examPlaceId"), json.getString("examPlaceName"));
			placeValue.put(json.getString("examPlaceId"), json);
			placeMap.put(json.getString("examPlaceId"), json);
		}

		// 获取排考规则
		List<ArrangeExamRule> arrangeExamRuleList = examManagementDao.getArrangeExamRule(request, termInfo, autoIncr);

		int numOfStuds = 0; // 已分配人数
		List<JSONObject> arrangeInfo = new ArrayList<JSONObject>();
		if (CollectionUtils.isNotEmpty(arrangeExamRuleList)) { // 规则不为空，表示已经设置过数据了
			Map<String, JSONObject> subjGroupId2Obj = new HashMap<String, JSONObject>();
			for (ArrangeExamRule rule : arrangeExamRuleList) {
				String subjGroupId = rule.getExamSubjectGroupId();
				JSONObject json = new JSONObject();
				json.put("numOfSubject", rule.getNumOfSubject());
				json.put("examSubjectGroupId", subjGroupId);
				json.put("createDateTime", rule.getCreateDateTime());
				json.put("numOfTClasses", 0); // 班级数
				json.put("numOfStuds", 0); // 班级总人数
				json.put("numOfExamPlaces", 0); // 考场数
				json.put("numOfExaminees", 0); // 可安排人数
				subjGroupId2Obj.put(subjGroupId, json);
				arrangeInfo.add(json);
			}

			request.put("examSubjectGroupObjs", arrangeExamRuleList);

			Set<String> deleteSubjGroupId = new HashSet<String>();
			List<JSONObject> examSubjectInfoList = examManagementDao.getArrangeExamSubjectAndsubject(request,
					termInfo, autoIncr);
			LinkedHashMap<String, List<JSONObject>> submap=new LinkedHashMap<String, List<JSONObject>>();
			for(JSONObject subjectInfo : examSubjectInfoList){
				if(submap.containsKey(subjectInfo.getString("examSubjectGroupId"))){
					List<JSONObject> slist=submap.get(subjectInfo.getString("examSubjectGroupId"));
					slist.add(subjectInfo);
				}else{
					List<JSONObject> slist=new ArrayList<JSONObject>();
					slist.add(subjectInfo);
					submap.put(subjectInfo.getString("examSubjectGroupId"), slist);
				}
			}
			 for (Entry<String, List<JSONObject>> entry : submap.entrySet()) {
				 List<JSONObject> sub=entry.getValue();
					String subjGroupId = entry.getKey(); // 考试分组id
					JSONObject json = subjGroupId2Obj.get(subjGroupId);
					for(JSONObject sdata:sub){
						Long start=sdata.getDate("startTime").getTime();
						Long end=sdata.getDate("endTime").getTime();
						String examSubjectId = sdata.getString("examSubjectId"); // 考试科目id
						ExamSubject es = examSubjectId2Obj.get(examSubjectId);
						
						if(es==null)break;
						
						if (examSubjectIdSet.contains(examSubjectId)) { // 判断科目是否存在已经选择
							deleteSubjGroupId.add(subjGroupId); // 存在的话，需要把教学班级和考场一些数据过滤掉，数据做“与”操作
						}
						for(ExamSubject sjon:sublist){//删除时间存在重合的
							if(sjon.getStartTime().getTime()<=start&&start<=sjon.getEndTime().getTime()){
								deleteSubjGroupId.add(subjGroupId); // 存在的话，需要把教学班级和考场一些数据过滤掉，数据做“与”操作
							}
							if(sjon.getStartTime().getTime()<=end&&end<=sjon.getEndTime().getTime()){
								deleteSubjGroupId.add(subjGroupId); // 存在的话，需要把教学班级和考场一些数据过滤掉，数据做“与”操作
							}
						}
						
						String subjName = null;
						if (json.getInteger("numOfSubject") == 1) { // 如果只有一个科目组合中只有一门科目
							subjName = es.getExamSubjName(); // 科目使用全称
						} else {
							subjName = es.getExamSubjSimpleName(); // 否则使用简称
						}
						String level=es.getSubjectId()+"_"+es.getSubjectLevel();
						String examSubjectGroupName = json.getString("examSubjectGroupName");
						String examSubjectGrouplevel = json.getString("examSubjectGrouplevel");
						if (examSubjectGroupName == null) {
							json.put("examSubjectGroupName", subjName);
							json.put("examSubjectGrouplevel", level);
						} else {
							examSubjectGroupName += "+" + subjName;
							examSubjectGrouplevel+=","+level;
							json.put("examSubjectGroupName", examSubjectGroupName);
							json.put("examSubjectGrouplevel", examSubjectGrouplevel);
						}
					}
			 }
//			for (JSONObject subjectInfo : examSubjectInfoList) {
//				String examSubjectId = subjectInfo.getExamSubjectId(); // 考试科目id
//				ExamSubject es = examSubjectId2Obj.get(examSubjectId);
//
//				String subjGroupId = subjectInfo.getExamSubjectGroupId(); // 考试分组id
//				if (examSubjectIdSet.contains(examSubjectId)) { // 判断科目是否存在已经选择
//					deleteSubjGroupId.add(subjGroupId); // 存在的话，需要把教学班级和考场一些数据过滤掉，数据做“与”操作
//				}
//
//				JSONObject json = subjGroupId2Obj.get(subjGroupId);
//
//				String subjName = null;
//				if (json.getInteger("numOfSubject") == 1) { // 如果只有一个科目组合中只有一门科目
//					subjName = es.getExamSubjName(); // 科目使用全称
//				} else {
//					subjName = es.getExamSubjSimpleName(); // 否则使用简称
//				}
//
//				String examSubjectGroupName = json.getString("examSubjectGroupName");
//				if (examSubjectGroupName == null) {
//					json.put("examSubjectGroupName", subjName);
//				} else {
//					examSubjectGroupName += "+" + subjName;
//					json.put("examSubjectGroupName", examSubjectGroupName);
//				}
//			}

			List<ArrangeExamClassInfo> examClassInfoList = examManagementDao.getArrangeExamClassInfo(request, termInfo,
					autoIncr);
		
			for (ArrangeExamClassInfo classInfo : examClassInfoList) {
				String tClassId = classInfo.gettClassId();
				String examSubjectGroupId = classInfo.getExamSubjectGroupId();
				JSONObject tClassInfo = tClassId2ObjMap.get(tClassId);
				int wait=0;
				List<String> subj=new ArrayList<String>();
				if(submap.containsKey(examSubjectGroupId)){
					List<JSONObject> sulist=submap.get(examSubjectGroupId);
					for(JSONObject sjon:sulist){
						subj.add(sjon.getString("examSubjectId"));
					}
				}
				
				if(waimap.containsKey(tClassId)){
					Map<String,List<String>> stum=waimap.get(tClassId);
					 for (Entry<String, List<String>> entry : stum.entrySet()) {
						 List<String> sub=entry.getValue();
							Collections.sort(sub);
							Collections.sort(subj);
							String userj=StringUtils.join(sub, ",");
							String targetj=StringUtils.join(subj, ",");
							if(userj.contains(targetj)){
								wait++;
							}
					 }
				}
				if(tClassInfo == null) {
					continue;
				}
				String subjGroupId = classInfo.getExamSubjectGroupId();
				JSONObject json = subjGroupId2Obj.get(subjGroupId);

				String tClassNames = json.getString("tClassNames");
				if (tClassNames == null) {
					json.put("tClassNames", tClassInfo.get("tClassName"));
				} else {
					tClassNames += ", " + tClassInfo.get("tClassName");
					json.put("tClassNames",tClassNames);
				
				}
				
				json.put("numOfTClasses", json.getInteger("numOfTClasses") + 1);
				json.put("numOfStuds", json.getInteger("numOfStuds") + tClassInfo.getIntValue("truestudSize")-wait);

				if (deleteSubjGroupId.contains(subjGroupId)) { // 过滤掉已经选择的班级数据
					JSONObject classJson = tClassValue.remove(tClassId);
					if (classJson != null) {
						numOfStuds += classJson.getIntValue("studSize");
					}
				}
			}

			List<ArrangeExamPlaceInfo> examPlaceInfoList = examManagementDao.getArrangeExamPlaceInfo(request, termInfo,
					autoIncr);
			for (ArrangeExamPlaceInfo placeInfo : examPlaceInfoList) {
				String examPlaceId = placeInfo.getExamPlaceId();
				String examPlaceName = placeId2NameMap.get(examPlaceId);
				JSONObject pob=placeMap.get(examPlaceId);
				String subjGroupId = placeInfo.getExamSubjectGroupId();
				JSONObject json = subjGroupId2Obj.get(subjGroupId);

				String examPlaceNames = json.getString("examPlaceNames");
				if (examPlaceNames == null) {
					json.put("examPlaceNames", examPlaceName);
					List<JSONObject> plist=new ArrayList<JSONObject>();
					plist.add(pob);
					json.put("enme", plist);
				} else {
					examPlaceNames += ", " + examPlaceName;
					List<JSONObject> plist=(List<JSONObject>) json.get("enme");
					plist.add(pob);
					json.put("examPlaceNames", examPlaceNames);
					json.put("enme", plist);
				}

				json.put("numOfExamPlaces", json.getInteger("numOfExamPlaces") + 1);
				json.put("numOfExaminees", json.getInteger("numOfExaminees") + placeInfo.getNumOfStuds());

				if (deleteSubjGroupId.contains(subjGroupId)) { // 过滤掉已经选择的考场信息
					placeValue.remove(examPlaceId);
				}
			}
			//排序
			 for (Entry<String, JSONObject> entry : subjGroupId2Obj.entrySet()) {
				 
				JSONObject data1= entry.getValue();
				List<String> namelist=Arrays.asList(data1.getString("tClassNames").split(","));
				List<JSONObject> plalist=(List<JSONObject>) data1.get("enme");
				List<String> pl=new ArrayList<String>();
				Collections.sort(plalist, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						// TODO Auto-generated method stub
						int code1=o1.getIntValue("examPlaceCode");
						int code2=o2.getIntValue("examPlaceCode");
						return code1==code2?0:(code1<code2?-1:1);
					}
				});
				Collections.sort(namelist,Collator.getInstance(java.util.Locale.CHINA));
				
				for(JSONObject p:plalist){
					pl.add(p.getString("examPlaceName"));
				}
				
				data1.put("tClassNames", StringUtils.join(namelist, ","));
				
				data1.put("examPlaceNames", StringUtils.join(pl, ","));
				
				data1.put("enme", "");
			 }
		}
		
		
		
		
		JSONObject data = new JSONObject();
		List<JSONObject> tClassList = new ArrayList<JSONObject>(tClassValue.values());
		List<String> tnamelist=new ArrayList<String>();
		for(JSONObject cjson:tClassList){
			tnamelist.add(cjson.getString("tClassName"));
		}
		tClassList=SortUtil.sortJsonListByTclassName(tClassList, 0, tnamelist, "tClassName");
		
		List<JSONObject> placeList = new ArrayList<JSONObject>(placeValue.values());
		Collections.sort(placeList, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				int examPlaceCode0 = arg0.getIntValue("examPlaceCode");
				int examPlaceCode1 = arg1.getIntValue("examPlaceCode");
				return examPlaceCode0==examPlaceCode1?0:examPlaceCode0<examPlaceCode1?-1:1;
			}
		});
		//排序处理
		Collections.sort(arrangeInfo, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int result = 0;
				
				List<String> subjKeys1 = Arrays.asList(o1.getString("examSubjectGrouplevel").split(","));
				List<String> subjKeys2 =  Arrays.asList(o2.getString("examSubjectGrouplevel").split(","));
				int len = Math.min(subjKeys1.size(), subjKeys2.size());
				for(int i = 0; i < len; i ++) {
					String[] item1 = ((String) subjKeys1.get(i)).split("_");
					String[] item2 = ((String) subjKeys2.get(i)).split("_");
					result = Long.compare(Long.parseLong(item1[0]), Long.parseLong(item2[0]));
					if(result != 0) {
						return result;
					}
					result = Integer.compare(Integer.parseInt(item1[1]), Integer.parseInt(item2[1]));
					if(result != 0) {
						return result;
					}
					result = Long.compare(o1.getDate("createDateTime").getTime(), o2.getDate("createDateTime").getTime());
					if(result != 0) {
						return result;
					}
				}
				return Integer.compare(subjKeys1.size(), subjKeys2.size());
			}
		});
		//重名处理
		Map<String,String> armap=new LinkedHashMap<String, String>();
		for(int t=0;t<arrangeInfo.size();t++){
			if(armap.containsKey(arrangeInfo.get(t).getString("examSubjectGroupName"))){
				String index=armap.get(arrangeInfo.get(t).getString("examSubjectGroupName"));
				String indexn[]=index.split("_");
				String gname= arrangeInfo.get(t).getString("examSubjectGroupName");
				if(indexn[1].equals("0")){
					int in=Integer.valueOf(indexn[0]);
					arrangeInfo.get(in).put("examSubjectGroupName", arrangeInfo.get(in).get("examSubjectGroupName")+"1");
					arrangeInfo.get(t).put("examSubjectGroupName", arrangeInfo.get(t).get("examSubjectGroupName")+"2");
					armap.put(gname, t+"_2");
				}else{
					int num=Integer.valueOf(indexn[1])+1;
					arrangeInfo.get(t).put("examSubjectGroupName", arrangeInfo.get(t).getString("examSubjectGroupName")+num);
					armap.put(gname, t+"_"+num);
				}
			}else{
				armap.put(arrangeInfo.get(t).getString("examSubjectGroupName"), t+"_0");
			}
		}
		data.put("tClassList", tClassList);
		data.put("examPlaceList", placeList);
		data.put("arrangeInfo", arrangeInfo);
		data.put("numOfStuds", numOfStuds);
		data.put("numOfNotStuds", totalNumOfStuds - numOfStuds-totalwait);
		return data;
	}

	public void arrangeExam(JSONObject request) {
		JSONObject params = new JSONObject();
		Long schoolId = request.getLong("schoolId");
		String examManagementId = request.getString("examManagementId");
		String termInfo = request.getString("termInfo");
		params.put("schoolId", schoolId);
		params.put("examManagementId", examManagementId);
		params.put("termInfo", termInfo);

		ExamManagement em = examManagementDao.getExamManagementListById(params, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();

		// 获取考试计划，主要获取使用年级和课表代码（通过课表代码判空，判断是否是新高考选课）
		JSONArray examSubjectIds = request.getJSONArray("examSubjectIds"); // 传递数据的时候需要把顺序排好
		JSONArray tClassIds = request.getJSONArray("tClassIds");
		JSONArray examPlace = request.getJSONArray("examPlace");

		String examPlanId = request.getString("examPlanId");

		String examSubjectGroupId = UUIDUtil.getUUID();

		ArrangeExamRule rule = new ArrangeExamRule();
		rule.setExamManagementId(examManagementId);
		rule.setTermInfo(termInfo);
		rule.setSchoolId(schoolId);
		rule.setExamSubjectGroupId(examSubjectGroupId);
		rule.setExamPlanId(examPlanId);
		rule.setCreateDateTime(new Date());
		rule.setNumOfSubject(examSubjectIds.size());
		examManagementDao.insertArrExamRule(rule, termInfo, autoIncr);

		List<ArrangeExamSubjectInfo> arrExamSubjInfoList = new ArrayList<ArrangeExamSubjectInfo>();
		for (int i = 0, len = examSubjectIds.size(); i < len; i++) {
			ArrangeExamSubjectInfo arrExamSubjInfo = new ArrangeExamSubjectInfo();
			arrExamSubjInfo.setExamManagementId(examManagementId);
			arrExamSubjInfo.setSchoolId(schoolId);
			arrExamSubjInfo.setTermInfo(termInfo);

			arrExamSubjInfo.setExamSubjectGroupId(examSubjectGroupId);
			arrExamSubjInfo.setExamSubjectId(examSubjectIds.getString(i));
			//arrExamSubjInfo.setSort(i);废除

			arrExamSubjInfoList.add(arrExamSubjInfo);
		}
		examManagementDao.insertArrExamSubjInfoBatch(arrExamSubjInfoList, termInfo, autoIncr);

		params.put("examPlanId", examPlanId);
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(examPlanList)) {
			throw new CommonRunException(-1, "没有找到对应年级的考试计划，请前往排考中设置");
		}
		ExamPlan examPlan = examPlanList.get(0);
		String usedGrade = examPlan.getUsedGrade(); // 使用年级

		List<ArrangeExamClassInfo> arrExamClassInfoList = new ArrayList<ArrangeExamClassInfo>();
		for (Object obj : tClassIds) {
			String tClassId = (String) obj;
			ArrangeExamClassInfo arrExamClassInfo = new ArrangeExamClassInfo();
			arrExamClassInfo.setExamManagementId(examManagementId);
			arrExamClassInfo.setExamSubjectGroupId(examSubjectGroupId);
			arrExamClassInfo.setSchoolId(schoolId);
			arrExamClassInfo.settClassId(tClassId);
			arrExamClassInfo.setTermInfo(termInfo);
			arrExamClassInfo.setUsedGrade(usedGrade);
			arrExamClassInfoList.add(arrExamClassInfo);
		}
		examManagementDao.insertArrExamClassInfoBatch(arrExamClassInfoList, termInfo, autoIncr);

		List<ArrangeExamPlaceInfo> arrExamPlaceInfoList = new ArrayList<ArrangeExamPlaceInfo>();
		for (int i = 0, len = examPlace.size(); i < len; i++) {
			JSONObject json = examPlace.getJSONObject(i);

			ArrangeExamPlaceInfo arrExamPlaceInfo = new ArrangeExamPlaceInfo();
			arrExamPlaceInfo.setExamManagementId(examManagementId);
			arrExamPlaceInfo.setSchoolId(schoolId);
			arrExamPlaceInfo.setTermInfo(termInfo);

			arrExamPlaceInfo.setExamSubjectGroupId(examSubjectGroupId);
			arrExamPlaceInfo.setExamPlaceId(json.getString("examPlaceId"));
			arrExamPlaceInfo.setNumOfStuds(json.getInteger("numOfStuds"));
			arrExamPlaceInfo.setRemainNumOfStuds(json.getInteger("remainNumOfStuds"));
			arrExamPlaceInfoList.add(arrExamPlaceInfo);
		}
		examManagementDao.insertArrExamPlaceInfoBatch(arrExamPlaceInfoList, termInfo, autoIncr);
		
		em.setStatus(4);
		examManagementDao.updateExamManagementStatus(em, termInfo);
	}

	public void deleteArrangeExamInfo(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();

		examManagementDao.deleteArrangeExamClassInfo(request, termInfo, autoIncr);
		examManagementDao.deleteArrangeExamPlaceInfo(request, termInfo, autoIncr);
		examManagementDao.deleteArrangeExamSubjectInfo(request, termInfo, autoIncr);
		examManagementDao.deleteArrangeExamRule(request, termInfo, autoIncr);
		examManagementDao.deleteArrExamResult(request, termInfo, autoIncr);

		// 其他一些删除操作
	}

	public void autoArrangeExam(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		
		threadPool.execute(new AutoArrangeExam(transactionManager, request, em));
//		 new AutoArrangeExam(transactionManager, request, em).run();

		em.setStatus(5);
		examManagementDao.updateExamManagementStatus(em, termInfo);
	}

	public ExecProgressRedisMsg queryProgress(JSONObject request) throws Exception {
		String redisKey = (String) request.remove("redisKey");
		ExecProgressRedisMsg msg = (ExecProgressRedisMsg) redisOperationDAO.get(redisKey);
		if (msg == null) {
			msg = new ExecProgressRedisMsg();
			msg.setCode(-1);
			msg.setErrorMsg("无法从缓存获取对应的分班进度，请联系管理员！");
			msg.setMessage("");
		}
		if (msg.getCode() == 1 && msg.getProgress() < 100) {
			// 重置缓存时间
			redisOperationDAO.expire(redisKey, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} else {
			redisOperationDAO.del(redisKey);
		}
		return msg;
	}

	class AutoArrangeExam implements Runnable {
		private DataSourceTransactionManager transactionManager;
		private DefaultTransactionDefinition def = new DefaultTransactionDefinition();

		private JSONObject request;
		private Integer autoIncr;
		private String examManagementId;
		private Long schoolId;
		private String termInfo;
		private String xn;
		private String redisKey;

		private ExecProgressRedisMsg progress = new ExecProgressRedisMsg();

		public AutoArrangeExam(DataSourceTransactionManager transactionManager, JSONObject request, ExamManagement em) {
			this.transactionManager = transactionManager;
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

			this.request = request;
			this.autoIncr = em.getAutoIncr();
			this.termInfo = request.getString("termInfo");
			this.schoolId = request.getLong("schoolId");
			this.examManagementId = request.getString("examManagementId");
			this.xn = termInfo.substring(0, termInfo.length() - 1);

			this.redisKey = (String) request.remove("redisKey");
		}

		private void setProgress(double progress, String msg) {
			setProgress(progress, msg, null);
		}
		
		private void setProgress(double progress, String msg, List<ArrangeExamLog> logList) {
			this.progress.setMessage(msg);
			if (progress != -1) {
				this.progress.setProgress(progress);
			}
			this.progress.setLogList(logList);
			this.progress.setCode(1);
			try {
				redisOperationDAO.set(redisKey, this.progress,
						CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

		private void setError(int code, String errorMsg) {
			setError(code, errorMsg, null);
		}

		private void setError(int code, String errorMsg, String message) {
			this.progress.setCode(code);
			this.progress.setErrorMsg(errorMsg);
			this.progress.setProgress(0);
			if (message != null) {
				this.progress.setMessage(message);
			}
			try {
				redisOperationDAO.set(redisKey, this.progress,
						CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// 获得事务状态
			TransactionStatus transactionStatus = transactionManager.getTransaction(def);
			try {
				setProgress(0, "开始排考。");

				JSONObject params = new JSONObject();
				params.put("examManagementId", request.get("examManagementId"));
				params.put("schoolId", request.get("schoolId"));
				params.put("termInfo", request.get("termInfo"));
				
				setProgress(1, "删除已有的排考数据。");
				// 删除安排的结果数据
				examManagementDao.deleteArrExamResult(params, termInfo, autoIncr);
				// 删除考号信息数据
				examManagementDao.deleteTestNumberInfo(params, termInfo, autoIncr);
				// 删除等待学生数据
				examManagementDao.deleteStudsWaiting(params, termInfo, autoIncr);

				setProgress(3, "获取考试计划。");
				params.put("examPlanIds", request.get("examPlanIds"));
				List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(params, termInfo, autoIncr);
				params.remove("examPlanIds");

				if(CollectionUtils.isEmpty(examPlanList)) {
					throw new CommonRunException(-1, "没有设置考试计划，请前往排考中设置");
				}
				
				// 遍历考试计划
				setProgress(6, "开始安排考试。");
				List<ArrangeExamLog> logList = new ArrayList<ArrangeExamLog>(examPlanList.size());
				for (ExamPlan ep : examPlanList) {
					params.put("examPlanId", ep.getExamPlanId());
					T_GradeLevel gl = T_GradeLevel
							.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(ep.getUsedGrade(), xn)));
					// 自动排考
					logList.add(autoArrengeExam(params, ep, gl));
					setProgress(6 + 90 / examPlanList.size(), "完成" + njName.get(gl) + "年级的考试安排。");
				}

				setProgress(98, "更新排考状态。");
				// 更新考务状态
				params.put("status", 100);
				examManagementDao.updateExamManagementStatus(params, termInfo);
				
				// 提交事务
				transactionManager.commit(transactionStatus);
				setProgress(100, "排考已完成，请通过查看页面查看排考数据！", logList);
			} catch (CommonRunException e) {
				// 回滚事务
				transactionManager.rollback(transactionStatus);
				setError(e.getCode(), e.getMessage());
				logger.error(e.getMessage());
			} catch (Exception e) {
				// 回滚事务
				transactionManager.rollback(transactionStatus);
				setError(-1, "后台异常，请联系管理员！", e.getMessage());
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

		private ArrangeExamLog autoArrengeExam(JSONObject params, ExamPlan ep, T_GradeLevel gl) throws CloneNotSupportedException {
			String gradeName = njName.get(gl);
			
			String scheduleId = ep.getScheduleId();
			// 测试规则
			JSONObject testNumRule = request.getJSONObject("testNumRule");
			ep.setTestNumPrefix(testNumRule.getString("testNumPrefix"));
			ep.setGradeDigit(testNumRule.getIntValue("gradeDigit"));
			ep.setSerialNumDigit(testNumRule.getIntValue("serialNumDigit"));

			// 是否随机排
			Boolean isRandom = request.getBoolean("isRandom");
			final JSONObject scoreRule = request.getJSONObject("scoreRule");
			if (!isRandom && scoreRule == null) { // 不随机排，那么需要判断规则是否有数据
				throw new CommonRunException(-1, "传参错误，请联系管理员！");
			}

			// 获取排考规则
			List<ArrangeExamRule> examRuleList = examManagementDao.getArrangeExamRule(params, termInfo, autoIncr);
			if(CollectionUtils.isEmpty(examRuleList)) {
				throw new CommonRunException(-1, "排考数据为空，请在安排考场设置界面设置排考数据！");
			}
			params.put("examSubjectGroupObjs", examRuleList);
			// 获取排考科目列表
			List<ArrangeExamClassInfo> arrExamClassList = examManagementDao.getArrangeExamClassInfo(params, termInfo,
					autoIncr);
			// 获取排考地点列表
			List<ArrangeExamPlaceInfo> arrExamPlaceList = examManagementDao.getArrangeExamPlaceInfo(params, termInfo,
					autoIncr);
			// 获取排考科目列表
			List<ArrangeExamSubjectInfo> arrExamSubjectList = examManagementDao.getArrangeExamSubjectInfo(params,
					termInfo, autoIncr);
			params.remove("examSubjectGroupObjs");

			// 考试科目代码 映射 科目代码
			List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(params, termInfo, autoIncr);
			Map<String, ExamSubject> examSubjId2Obj = new HashMap<String, ExamSubject>();
			for (ExamSubject es : examSubjectList) {
				examSubjId2Obj.put(es.getExamSubjectId(), es);
			}

			// 科目分组代码映射考试科目代码列表
			Map<String, Set<String>> subjGrpId2ExamSubjIdSet = new HashMap<String, Set<String>>();
			for (ArrangeExamSubjectInfo examSubjectInfo : arrExamSubjectList) {
				String subjGrpId = examSubjectInfo.getExamSubjectGroupId();
				if (!subjGrpId2ExamSubjIdSet.containsKey(subjGrpId)) {
					subjGrpId2ExamSubjIdSet.put(subjGrpId, new HashSet<String>());
				}
				subjGrpId2ExamSubjIdSet.get(subjGrpId).add(examSubjectInfo.getExamSubjectId());
			}

			// 排除不参考的学生
			Map<Long, Set<String>> accId2ExamSubjIdSet = new HashMap<Long, Set<String>>();
			List<StudNotTakingExam> studNotTakingExamList = examManagementDao.getStudsNotTakingExam(params, termInfo,
					autoIncr);
			for (StudNotTakingExam studNotTakingExam : studNotTakingExamList) {
				Long accId = studNotTakingExam.getAccountId();
				if (!accId2ExamSubjIdSet.containsKey(accId)) {
					accId2ExamSubjIdSet.put(accId, new HashSet<String>());
				}
				accId2ExamSubjIdSet.get(accId).add(studNotTakingExam.getExamSubjectId());
			}

			// 分组科目代码对应不参考的学生
			// 如果只有一门不参考，按所有来排，只是展示的时候座位空出来，如果分组科目所有科目都不参考，那么这个学生不排考试
			Map<String, Set<Long>> subjGrpId2RmAccIdSet = new HashMap<String, Set<Long>>();
			for (Map.Entry<String, Set<String>> entry1 : subjGrpId2ExamSubjIdSet.entrySet()) {
				String subjGrpId = entry1.getKey();
				Set<String> examSubjIdSet = entry1.getValue();

				if (!subjGrpId2RmAccIdSet.containsKey(subjGrpId)) {
					subjGrpId2RmAccIdSet.put(subjGrpId, new HashSet<Long>());
				}

				for (Map.Entry<Long, Set<String>> entry2 : accId2ExamSubjIdSet.entrySet()) {
					// 不参考学生
					Long accId = entry2.getKey();
					if (!entry2.getValue().containsAll(examSubjIdSet)) {
						continue;
					}
					subjGrpId2RmAccIdSet.get(subjGrpId).add(accId);
				}
			}

			// 获取学生分数
			Map<Long, Map<Long, Double>> accId2Score = new HashMap<Long, Map<Long, Double>>();
			// 科目组对应科目
			if (!isRandom) { // 不是随机排考，则需要获取成绩信息
				
				
				
				params.put("examId", scoreRule.get("scoreId"));
				params.put("usedGrade", ep.getUsedGrade());
				JSONObject degree = examManagementDao.getDegreeinfo(params);
				params.put("tableKey", degree.getString("tableKey"));
				List<JSONObject> scoreInfoList = examManagementDao.getScoreInfo(params);
				params.remove("examId");
				params.remove("usedGrade");
				for (JSONObject scoreInfo : scoreInfoList) {
					Long accountId = scoreInfo.getLong("accountId");
					Long subjectId = scoreInfo.getLong("subjectId");
					Double score = scoreInfo.getDouble("score");
					if (accountId == null || subjectId == null) {
						continue;
					}
					if (!accId2Score.containsKey(accountId)) {
						accId2Score.put(accountId, new HashMap<Long, Double>());
					}
					accId2Score.get(accountId).put(subjectId, score);
				}
			}

			Map<String, List<ArrangeExamResult>> classId2List = new HashMap<String, List<ArrangeExamResult>>();
			if (StringUtils.isBlank(scheduleId)) {	// 如果scheduleId为空，则表示不是从新高考获取数据
				Set<Long> classIdSet = new HashSet<Long>();
				for (ArrangeExamClassInfo examClassInfo : arrExamClassList) { // 获取去重后的班级id
					classIdSet.add(Long.valueOf(examClassInfo.gettClassId()));
				}

				List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId,
						new ArrayList<Long>(classIdSet), termInfo);

				if (CollectionUtils.isNotEmpty(classIdSet) && CollectionUtils.isEmpty(classrooms)) {
					throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
				}

				if (CollectionUtils.isNotEmpty(classrooms)) {
					for (Classroom classroom : classrooms) {
						String classId = String.valueOf(classroom.getId());
						if (!classId2List.containsKey(classId)) {
							classId2List.put(classId, new ArrayList<ArrangeExamResult>());
						}
						if (CollectionUtils.isEmpty(classroom.getStudentAccountIds())) {
							continue;
						}
						
						for (Long accountId : classroom.getStudentAccountIds()) {
							ArrangeExamResult arrExamResult = new ArrangeExamResult();
							arrExamResult.setExamManagementId(examManagementId);
							arrExamResult.setTermInfo(termInfo);
							arrExamResult.setSchoolId(schoolId);

							arrExamResult.setAccountId(accountId);
							arrExamResult.setExamPlanId(ep.getExamPlanId());
							arrExamResult.settClassId(String.valueOf(classroom.getId()));
							arrExamResult.settClassNo(classroom.getClassNo());
							arrExamResult.settClassName(classroom.getClassName());
							classId2List.get(classId).add(arrExamResult);
						}
					}
				}
			} else {
				// 使用新高考进行排考
				Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, termInfo);
				if (grade == null) {
					throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
				}

				if (CollectionUtils.isEmpty(grade.getClassIds())) {
					throw new CommonRunException(-1, "年级下没有班级，请联系管理员！");
				}

				List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(),
						termInfo);
				if (CollectionUtils.isEmpty(classrooms)) {
					throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
				}

				Map<Long, Classroom> accId2Classroom = new HashMap<Long, Classroom>();
				for (Classroom classroom : classrooms) {
					if (CollectionUtils.isEmpty(classroom.getStudentAccountIds())) {
						continue;
					}
					for (Long accId : classroom.getStudentAccountIds()) {
						accId2Classroom.put(accId, classroom);
					}
				}

				Set<String> classIdSet = new HashSet<String>();
				for (ArrangeExamClassInfo examClassInfo : arrExamClassList) { // 获取去重后的班级id
					classIdSet.add(examClassInfo.gettClassId());
				}

				List<TSchTClassInfoExternal> classInfoList = scheduleExternalService.getTClassInfoExternal(scheduleId,
						schoolId, termInfo, ep.getUsedGrade(), classIdSet);

				for (TSchTClassInfoExternal classInfo : classInfoList) {
					String classId = classInfo.getTclassId();
					if (!classId2List.containsKey(classId)) {
						classId2List.put(classId, new ArrayList<ArrangeExamResult>());
					}

					if (CollectionUtils.isEmpty(classInfo.getStudentIdList())) {
						continue;
					}
					
					for (Long accountId : classInfo.getStudentIdList()) {
						Classroom classroom = accId2Classroom.get(accountId);
						if (classroom == null) {
							continue;
						}

						ArrangeExamResult arrExamResult = new ArrangeExamResult();
						arrExamResult.setExamManagementId(examManagementId);
						arrExamResult.setTermInfo(termInfo);
						arrExamResult.setSchoolId(schoolId);

						arrExamResult.setAccountId(accountId);
						arrExamResult.setExamPlanId(ep.getExamPlanId());
						arrExamResult.settClassId(classInfo.getTclassId());
						arrExamResult.settClassNo(classroom.getClassNo());
						arrExamResult.settClassName(classInfo.getTclassName());
						classId2List.get(classId).add(arrExamResult);
					}
				}
			}

			Map<String, LinkedList<ArrangeExamResult>> subjGrpId2StudList = new HashMap<String, LinkedList<ArrangeExamResult>>();
			for (ArrangeExamClassInfo examClassInfo : arrExamClassList) {
				String subjGrpId = examClassInfo.getExamSubjectGroupId();
				if (!subjGrpId2StudList.containsKey(subjGrpId)) {
					subjGrpId2StudList.put(subjGrpId, new LinkedList<ArrangeExamResult>());
				}

				// 不参考的学生
				Set<Long> rmAccIdSet = subjGrpId2RmAccIdSet.get(subjGrpId);

				String classId = examClassInfo.gettClassId();
				List<ArrangeExamResult> list = classId2List.get(classId); // 班级代码对应参考学生
				if (CollectionUtils.isEmpty(list)) {
					continue;
				}

				Set<String> examSubjIds = subjGrpId2ExamSubjIdSet.get(subjGrpId);
				for (ArrangeExamResult studInfo : list) {
					Long accountId = studInfo.getAccountId();

					if (rmAccIdSet.contains(accountId)) {
						// 如果此学生不参考此科目组合，则排考的时候排除学生
						continue;
					}

					if (!isRandom && CollectionUtils.isNotEmpty(examSubjIds)) {
						// 不是随机排，并且考试科目代码集合也不为空，那么按分数排名
						Double score = 0d;
						// accountId的所有分数
						Map<Long, Double> allScore = accId2Score.get(accountId);
						for (String examSubjId : examSubjIds) { // 多门科目排考，分数合并
							Long subjId = examSubjId2Obj.get(examSubjId).getSubjectId();
							if(allScore != null) {
								score += allScore.get(subjId) == null ? 0 : allScore.get(subjId);
							}
						}
						studInfo.setScore(score);
					}
					ArrangeExamResult cloneResult = (ArrangeExamResult) studInfo.clone();
					cloneResult.setExamSubjectGroupId(subjGrpId);
					subjGrpId2StudList.get(subjGrpId).add(cloneResult);
				}
			}

			// 考试地点数据（注意，这里不是安排考试地点数据）
			List<JSONObject> examPlaceList = examManagementDao.getExamPlaceInfoInArrange(params, termInfo, autoIncr);
			Map<String, String> placeId2CodeMap = new HashMap<String, String>(); // 考场代码映射代码，用于生成考号
			for (JSONObject json : examPlaceList) {
				placeId2CodeMap.put(json.getString("examPlaceId"), json.getString("examPlaceCode"));
			}

			// 科目分组代码 映射 考场列表
			Map<String, List<ArrangeExamPlaceInfo>> subjGrpId2PlaceList = new HashMap<String, List<ArrangeExamPlaceInfo>>();
			for (ArrangeExamPlaceInfo examPlaceInfo : arrExamPlaceList) {
				String subjGrpId = examPlaceInfo.getExamSubjectGroupId();
				if (!subjGrpId2PlaceList.containsKey(subjGrpId)) {
					subjGrpId2PlaceList.put(subjGrpId, new ArrayList<ArrangeExamPlaceInfo>());
				}
				if(placeId2CodeMap.containsKey(examPlaceInfo.getExamPlaceId())) {
					examPlaceInfo.setExamPlaceCode(placeId2CodeMap.get(examPlaceInfo.getExamPlaceId()));
					subjGrpId2PlaceList.get(subjGrpId).add(examPlaceInfo);
				}
			}

			// *************************************** 智能排考 ******************************************* //
			List<ArrangeExamResult> remainArrExamResult = new ArrayList<ArrangeExamResult>();
			// 生成考号映射，因为一个学生只能拥有一个考号，此考号跟随第一次考试信息
			Map<Long, TestNumberInfo> accId2TestNumObj = new HashMap<Long, TestNumberInfo>();
			
			// 考号集合，判断是否拥有重复的考号，将已有的考号取出
			Set<String> testNumSet = new HashSet<String>();
			long serialNumber = 0;
			
			int completedNumOfStuds = 0;
			int totalNumOfStuds = 0;
			Set<String> ids=new HashSet<String>();
			Set<String> allids=new HashSet<String>();
			List<ArrangeExamResult> arrExamResult = new ArrayList<ArrangeExamResult>();
			for (Map.Entry<String, List<ArrangeExamPlaceInfo>> entry : subjGrpId2PlaceList.entrySet()) {
				String subjGrpId = entry.getKey();

				List<ArrangeExamPlaceInfo> placeList = entry.getValue();
				if (CollectionUtils.isEmpty(placeList)) {
					continue;
				}

				// 获取科目组合的学生
				LinkedList<ArrangeExamResult> studList = subjGrpId2StudList.get(subjGrpId);
				if (CollectionUtils.isEmpty(studList)) {
					continue;
				}
				for(ArrangeExamResult o:studList){
					allids.add(o.getAccountId()+"");
				}
				//totalNumOfStuds += studList.size();
				
				// 考场排序，从第一个考场排起
				Collections.sort(placeList, new Comparator<ArrangeExamPlaceInfo>() {
					@Override
					public int compare(ArrangeExamPlaceInfo o1, ArrangeExamPlaceInfo o2) {
						return o1.getExamPlaceCode().compareTo(o2.getExamPlaceCode());
					}
				});

				if (!isRandom) { // 按成绩排序
					Collections.sort(studList, new Comparator<ArrangeExamResult>() {
						@Override
						public int compare(ArrangeExamResult o1, ArrangeExamResult o2) {
							int pam=Double.compare(o1.getScore(), o2.getScore())==0?0:(Double.compare(o1.getScore(), o2.getScore())==1?-1:1);//反了
							return scoreRule.getIntValue("order") * pam;
						}
					});
				} else { // 随机排
					Collections.shuffle(studList);
				}
				
				for (ArrangeExamPlaceInfo examPlaceInfo : placeList) {
					Integer numOfStuds = examPlaceInfo.getNumOfStuds();
					String examPlaceId = examPlaceInfo.getExamPlaceId();

					List<ArrangeExamResult> subStudList = new ArrayList<ArrangeExamResult>();
					// 如果studList不为空，并且可排学生数大于0
					while (CollectionUtils.isNotEmpty(studList) && numOfStuds > 0) {
						subStudList.add(studList.removeFirst());
						-- numOfStuds;
					}
					examPlaceInfo.setRemainNumOfStuds(numOfStuds);
					//completedNumOfStuds += subStudList.size();

					if (!isRandom && scoreRule.getBooleanValue("isRandomInExamPlace")) { // 是否考场内随机排
						Collections.shuffle(subStudList);
					}

					Integer seatNumber = 0;
					for (ArrangeExamResult studInfo : subStudList) {
						++seatNumber;
						ids.add(studInfo.getAccountId()+"");
						// 设置studInfo可变信息，注意studInfo是共享数据（可能会造成主键冲突）
						studInfo.setExamPlaceId(examPlaceId);
						studInfo.setSeatNumber(seatNumber);
						Long accId = studInfo.getAccountId();

						TestNumberInfo testNum = accId2TestNumObj.get(accId);
						
						if (testNum == null) {
							serialNumber ++;
							
							if(serialNumber == Long.MAX_VALUE) {
								serialNumber = 1L;
							}
							
							String testNumber = examManagementDao.produceTestNumber(ep, serialNumber, xn);
							if(testNumSet.contains(testNumber)) {
								throw new CommonRunException(-1, "考号重复，请检查考号生成规则！");
							}
							
							testNum = new TestNumberInfo();
							testNum.setExamManagementId(examManagementId);
							testNum.setSchoolId(schoolId);
							testNum.setAccountId(accId);
							testNum.setUsedGrade(ep.getUsedGrade());
							testNum.setTermInfo(termInfo);
							testNum.setTestNumber(testNumber);
							
							accId2TestNumObj.put(accId, testNum);
							testNumSet.add(testNumber);
						}

						studInfo.setTestNumber(testNum.getTestNumber());

						arrExamResult.add(studInfo);
					}
					examManagementDao.updateRemainNumOfStuds(examPlaceInfo, termInfo, autoIncr);
				}
				
				if(studList.size() > 0) {	// 如果还有剩余，则表示还有学生没有排入，真实学生数大于可排学生数
					remainArrExamResult.addAll(studList);
				}
			}

			// 批量插入结果
			examManagementDao.insertArrExamResultBatch(arrExamResult, termInfo, autoIncr);
			
			examManagementDao.insertTestNumberInfoBatch(new ArrayList<TestNumberInfo>(accId2TestNumObj.values()),
					termInfo, autoIncr);

			ArrangeExamLog log = new ArrangeExamLog();
			log.setCompletedNumOfStuds(ids.size());
			log.setTotalNumOfStuds(allids.size());
			if(CollectionUtils.isNotEmpty(remainArrExamResult)) {
				// 把所有学生放入待排区
				examManagementDao.insertStudsWaitingBatch(remainArrExamResult, termInfo, autoIncr);
				
				List<Long> accIdList = new ArrayList<Long>();
				Map<String, List<Long>> subjClass2AccIds = new HashMap<String, List<Long>>();
				for(ArrangeExamResult result : remainArrExamResult) {
					accIdList.add(result.getAccountId());
					
					String subjGrpId = result.getExamSubjectGroupId();
					for(String examSubjId : subjGrpId2ExamSubjIdSet.get(subjGrpId)) {
						String subjName = examSubjId2Obj.get(examSubjId).getExamSubjName();
						
						String key = subjName + "_###_" + result.gettClassName(); 
						if(!subjClass2AccIds.containsKey(key)) {
							subjClass2AccIds.put(key, new ArrayList<Long>());
						}
						subjClass2AccIds.get(key).add(result.getAccountId());
					}
				}
				
				Map<Long, String> accId2Name = new HashMap<Long, String>();
				List<Account> accList = commonDataService.getAccountBatch(schoolId, accIdList, termInfo);
				for(Account acc : accList) {
					accId2Name.put(acc.getId(), acc.getName());
				}
				
				log.setType(-1);   // 失败日志
				log.setGradeName(gradeName + "年级");
				for(Map.Entry<String, List<Long>> entry : subjClass2AccIds.entrySet()) {
					String[] tmp = entry.getKey().split("_###_");
					String subjName = tmp[0];
					String className = tmp[1];
					StringBuffer studNames = new StringBuffer();
					for(Long accId : entry.getValue()) {
						studNames.append(accId2Name.get(accId)).append(",");
					}
					studNames.deleteCharAt(studNames.length() - 1);
					
					log.addMessage(subjName + ", " + className + ", " + studNames.toString());
				}
			} else {
				log.setType(1);   // 成功日志
				log.setGradeName(gradeName + "年级");
				log.addMessage(ids.size() + "名考生已完成考场安排！");
			}
			
			ep.setStatus(1);	// 完成
			examManagementDao.updateExamPlan(ep, termInfo, autoIncr);
			return log;
		}
	}
	
}
