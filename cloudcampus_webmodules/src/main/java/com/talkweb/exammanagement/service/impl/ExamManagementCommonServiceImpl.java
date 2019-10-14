package com.talkweb.exammanagement.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.domain.ArrangeExamClassInfo;
import com.talkweb.exammanagement.domain.ArrangeExamRule;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.domain.ExamSubject;
import com.talkweb.exammanagement.service.ExamManagementCommonService;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.entity.TSchTClassInfoExternal.TSchSubjectInfo;
import com.talkweb.schedule.service.ScheduleExternalService;

@Service
public class ExamManagementCommonServiceImpl implements ExamManagementCommonService {
	Logger logger = LoggerFactory.getLogger(ExamManagementCommonServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private ExamManagementDao examManagementDao;
	
	@Autowired
	private ScheduleExternalService scheduleExternalService;
	
	private T_GradeLevel[] gradeLevelList = new T_GradeLevel[] { T_GradeLevel.T_HighOne, T_GradeLevel.T_HighTwo,
			T_GradeLevel.T_HighThree };
	
	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	public JSONObject getStatus(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		JSONObject json = new JSONObject();
		json.put("status", em.getStatus());
		return json;
	}
	
	public List<JSONObject> getGradeList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		
		List<JSONObject> result = new ArrayList<JSONObject>(gradeLevelList.length);
		for(T_GradeLevel gradeLevel: gradeLevelList) {
			JSONObject data = new JSONObject();
			data.put("usedGrade", commonDataService.ConvertNJDM2SYNJ(String.valueOf(gradeLevel.getValue()), xn));
			data.put("gradeName", njName.get(gradeLevel));
			result.add(data);
		}
		
		return result;
	}
	
	public List<JSONObject> getScheduleList(JSONObject request) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		JSONObject blankItem = new JSONObject();
		blankItem.put("scheduleId", "");
		blankItem.put("scheduleName", " ");
		result.add(blankItem);
		
		List<JSONObject> scheduleList = scheduleExternalService.getScheduleForExam(request.getLong("schoolId"), 
				request.getString("termInfo"), request.getString("usedGrade"));
		if(CollectionUtils.isNotEmpty(scheduleList)) {
			for(JSONObject json : scheduleList) {
				JSONObject item = new JSONObject();
				item.put("scheduleId", json.get("scheduleId"));
				item.put("scheduleName", json.get("scheduleName"));
				result.add(item);
			}
		}
		
		return result;
	}
	
	public List<JSONObject> getSubjectList(JSONObject request) {
		String scheduleId = request.getString("scheduleId");
		String usedGrade = request.getString("usedGrade");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		
		if(StringUtils.isBlank(scheduleId)) {
			School school = commonDataService.getSchoolById(schoolId, termInfo);
			List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoList(school, termInfo);
			List<JSONObject> result = new ArrayList<JSONObject>();
			if(CollectionUtils.isNotEmpty(lessonInfoList)) {
				for(LessonInfo lessonInfo : lessonInfoList) {
					if (lessonInfo.getType() == 2) {
						continue;
					}
					JSONObject subjItem = new JSONObject();
					subjItem.put("subjectId", lessonInfo.getId());
					subjItem.put("subjectName", lessonInfo.getName());
					
					List<JSONObject> list = new ArrayList<JSONObject>(1);
					subjItem.put("subjectType", list);
					
					JSONObject subjectType = new JSONObject();
					subjectType.put("typeText", "");
					subjectType.put("subjectLevel", 0);
					list.add(subjectType);
					
					result.add(subjItem);
				}
			}
			return result;
		} else {
			// 需要从课表获取数据
			return scheduleExternalService.getScheduleSubjectForExam(scheduleId, schoolId, termInfo, usedGrade);
		}
	}
	
	public List<JSONObject> getExamPlanList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		String xn = termInfo.substring(0, termInfo.length() - 1);
		for(ExamPlan examPlan : examPlanList) {
			JSONObject json = new JSONObject();
			String usedGrade = examPlan.getUsedGrade();
			T_GradeLevel gl = T_GradeLevel.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			json.put("gradeName", njName.get(gl));
			json.put("examPlanId", examPlan.getExamPlanId());
			json.put("usedGrade", examPlan.getUsedGrade());
			json.put("status", examPlan.getStatus());
			result.add(json);
		}
		
		return result;
	}
	
	public List<JSONObject> getExamSubjectList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		request.put("isOrder", true);
		List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(request, termInfo, autoIncr);
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		for(ExamSubject es : examSubjectList) {
			JSONObject item = new JSONObject();
			item.put("examSubjectId", es.getExamSubjectId());
			item.put("examSubjectName", es.getExamSubjName());
			result.add(item);
		}
		
		return result;
	}
	
	public List<JSONObject> getGroupSubjectList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		List<ArrangeExamRule> arrExamRuleList = examManagementDao.getArrangeExamRule(request, termInfo, autoIncr);
		if(arrExamRuleList.size() == 0) {
			return new ArrayList<JSONObject>();
		}
		request.put("examSubjectGroupObjs", arrExamRuleList);
		
		List<JSONObject> list = examManagementDao.getExamSubjectGroupList(request, termInfo, autoIncr);
		
		for(JSONObject json : list) {
			String subjKeyStr = json.getString("subjKeys");
			List<String> subjKeys = StringUtil.convertToListFromStr(subjKeyStr, ",", String.class);
			json.put("subjKeys", subjKeys);
		}
		
		Collections.sort(list, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int result = 0;
				
				List<Object> subjKeys1 = o1.getJSONArray("subjKeys");
				List<Object> subjKeys2 = o2.getJSONArray("subjKeys");
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
		for(int t=0;t<list.size();t++){
			if(armap.containsKey(list.get(t).getString("examSubjectGroupName"))){
				String index=armap.get(list.get(t).getString("examSubjectGroupName"));
				String indexn[]=index.split("_");
				String gname= list.get(t).getString("examSubjectGroupName");
				if(indexn[1].equals("0")){
					int in=Integer.valueOf(indexn[0]);
					list.get(in).put("examSubjectGroupName", list.get(in).get("examSubjectGroupName")+"1");
					list.get(t).put("examSubjectGroupName", list.get(t).get("examSubjectGroupName")+"2");
					armap.put(gname, t+"_2");
				}else{
					int num=Integer.valueOf(indexn[1])+1;
					list.get(t).put("examSubjectGroupName", list.get(t).getString("examSubjectGroupName")+num);
					armap.put(gname, t+"_"+num);
				}
			}else{
				armap.put(list.get(t).getString("examSubjectGroupName"), t+"_0");
			}
		}
		if(request.containsKey("isAll")&&request.getString("isAll").equals("1")){
			List<String> ids=new ArrayList<String>();
			for(JSONObject j:list){
				ids.add(j.getString("examSubjectGroupId"));
			}
			JSONObject isall=new JSONObject();
			isall.put("examSubjectGroupId", StringUtils.join(ids, ","));
			isall.put("examSubjectGroupName", "全部");
			if(!list.isEmpty()){
			list.add(0, isall);
			}
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getPlaceInfoList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		List<JSONObject> list = examManagementDao.getCommPlaceList(request, termInfo, autoIncr);
		
			for(JSONObject j:list){
				if(request.containsKey("isId")&&request.getString("isId").equals("1")){
					j.put("examPlaceIdOrCode", j.getString("examPlaceId"));
				}else{
					j.put("examPlaceIdOrCode", j.getString("examPlaceCode"));
				}
			}
		
		if(request.containsKey("isAll")&&request.getString("isAll").equals("1")){
			List<String> ids=new ArrayList<String>();
			for(JSONObject j:list){
				ids.add(j.getString("examPlaceIdOrCode"));
			}
			JSONObject isall=new JSONObject();
			isall.put("examPlaceIdOrCode", StringUtils.join(ids, ","));
			isall.put("examPlaceName", "全部");
			if(!list.isEmpty()){
			list.add(0, isall);
			}
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getTClassListByGroupId(JSONObject request) {
		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if(examPlanList.size() == 0) {
			throw new CommonRunException(-1, "没有找到对应年级的考试计划，请前往排考中设置");
		}
		ExamPlan ep = examPlanList.get(0);
		String scheduleId = ep.getScheduleId();
		String usedGrade = ep.getUsedGrade();
		
		List<ArrangeExamClassInfo> examSubjectList = examManagementDao.getArrangeExamClassInfo(request, termInfo, autoIncr);
		if(examSubjectList.size() == 0) {
			throw new CommonRunException(-1, "获取不到对应的考试科目，请联系管理员！");
		}
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		List<String> cids=new ArrayList<String>();
		List<String> classnlist=new ArrayList<String>();
		if(StringUtils.isBlank(scheduleId)) {	// 从基础数据获取班级信息
			List<Long> ids=new ArrayList<Long>();
			for(ArrangeExamClassInfo es:examSubjectList){
				ids.add(Long.valueOf(es.gettClassId()));
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId,ids, termInfo);
			if(CollectionUtils.isEmpty(classrooms)) {
				throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
			}
			
			for(Classroom classroom : classrooms) {
				cids.add(classroom.getId()+"");
				JSONObject item=new JSONObject();
				item.put("tClassId", classroom.getId());
				item.put("tClassName", classroom.getClassName());
				classnlist.add(classroom.getClassName());
				result.add(item);		
			}
			result = SortUtil.sortJsonListByTclassName(result, 0, classnlist, "tClassName");
		} else {
			// 从新高考课表获取班级信息
			List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService.getTClassInfoExternalNoAccount(scheduleId, schoolId, termInfo, usedGrade, null);
			Map<String,TSchTClassInfoExternal> classmap=new HashMap<String, TSchTClassInfoExternal>();
			if(CollectionUtils.isNotEmpty(tClassInfoList)) {
				for(TSchTClassInfoExternal classInfo : tClassInfoList) {
					classmap.put(classInfo.getTclassId(), classInfo);
				}
			}
			for(ArrangeExamClassInfo es:examSubjectList){
				if(classmap.containsKey(es.gettClassId())){
					cids.add(es.gettClassId());
					JSONObject item=new JSONObject();
					item.put("tClassId", es.gettClassId());
					item.put("tClassName", classmap.get(es.gettClassId()).getTclassName());
					classnlist.add(classmap.get(es.gettClassId()).getTclassName());
					result.add(item);	
				}
			}
			result = SortUtil.sortJsonListByTclassName(result, 0, classnlist, "tClassName");
		}
		
		
		if(request.containsKey("isAll")&&request.getString("isAll").equals("1")){
			JSONObject all=new JSONObject();
			all.put("tClassId", StringUtils.join(cids, ","));
			all.put("tClassName", "全部");
			result.add(0, all);
		}
		
		return result;
	}
	
	public List<JSONObject> getTClassList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if(em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if(examPlanList.size() == 0) {
			throw new CommonRunException(-1, "没有找到对应年级的考试计划，请前往排考中设置");
		}
		ExamPlan ep = examPlanList.get(0);
		String scheduleId = ep.getScheduleId();
		String usedGrade = ep.getUsedGrade();
		
		List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(request, termInfo, autoIncr);
		if(examSubjectList.size() == 0) {
			throw new CommonRunException(-1, "获取不到对应的考试科目，请联系管理员！");
		}
		ExamSubject es = examSubjectList.get(0);
		List<String> cids=new ArrayList<String>();
		List<JSONObject> result = new ArrayList<JSONObject>();
		if(StringUtils.isBlank(scheduleId)) {	// 从基础数据获取班级信息
//			long subjectId = es.getSubjectId();
			T_GradeLevel gl = T_GradeLevel.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, termInfo);
			if(grade == null) {
				throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
			}
			if(CollectionUtils.isEmpty(grade.getClassIds())) {
				throw new CommonRunException(-1, "年级下面的班级为空，请联系管理员！");
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), termInfo);
			if(CollectionUtils.isEmpty(classrooms)) {
				throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
			}
			for(Classroom classroom : classrooms) {
				JSONObject item = new JSONObject();
				item.put("tClassId", classroom.getId());
				item.put("tClassName", classroom.getClassName());
				cids.add(classroom.getId()+"");
				result.add(item);
//				List<AccountLesson> accountLessonList = classroom.getAccountLessons();
//				if(CollectionUtils.isEmpty(accountLessonList)) {
//					continue;
//				}
//				for(AccountLesson accLesson : accountLessonList) {
//					if(subjectId == accLesson.getLessonId()) {
//						JSONObject item = new JSONObject();
//						item.put("tClassId", classroom.getId());
//						item.put("tClassName", classroom.getClassName());
//						result.add(item);
//						break;
//					}
//				}
			}
		} else {
			long subjectId = es.getSubjectId();
			int subjectLevel = es.getSubjectLevel();
			// 从新高考课表获取班级信息
			List<TSchTClassInfoExternal> tClassInfoList = scheduleExternalService.getTClassInfoExternalNoAccount(scheduleId, schoolId, termInfo, usedGrade, null);
			if(CollectionUtils.isNotEmpty(tClassInfoList)) {
				List<String> classnlist=new ArrayList<String>();
				for(TSchTClassInfoExternal classInfo : tClassInfoList) {
					for(TSchSubjectInfo subjectInfo : classInfo.getSubjectList()) {
						if(subjectId == subjectInfo.getSubjectId() 
								/*&& subjectLevel == subjectInfo.getSubjectLevel()*/) {
							JSONObject item = new JSONObject();
							item.put("tClassId", classInfo.getTclassId());
							item.put("tClassName", classInfo.getTclassName());
							classnlist.add(classInfo.getTclassName());
							cids.add(classInfo.getTclassId());
							result.add(item);
							break;
						}
					}
				}
				
				result = SortUtil.sortJsonListByTclassName(result, 0, classnlist, "tClassName");
			}
		}
		
		
		
		if(request.containsKey("isAll")&&request.getString("isAll").equals("1")){
			JSONObject all=new JSONObject();
			all.put("tClassId", StringUtils.join(cids, ","));
			all.put("tClassName", "全部");
			result.add(0, all);
		}
		return result;
	}
	
	public List<JSONObject> getExamManagementList(JSONObject request) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		String termInfo = request.getString("termInfo");
		String examManagementId = (String) request.remove("examManagementId");
		request.put("rmExamManagementId", examManagementId);
		
		while(TermInfoIdUtils.compare(termInfo, TermInfoIdUtils.FIRST_TERMINFOID) != -1) {	// termInfo >= "20151"
			request.put("termInfo", termInfo);
			
			List<ExamManagement> emList = examManagementDao.getExamManagementList(request, termInfo);
			for(ExamManagement em : emList) {
				JSONObject item = new JSONObject();
				item.put("name", em.getName());
				item.put("copyTermInfo", em.getTermInfo());
				item.put("copyExamManagementId", em.getExamManagementId());
				result.add(item);
			}
			
			termInfo = TermInfoIdUtils.decreaseTermInfo(termInfo);	// 学年学期自减
		}
		
		return result;
	}
	
	public List<JSONObject> getScoreList(JSONObject request) {
		return examManagementDao.getScoreList(request);
	}
}
