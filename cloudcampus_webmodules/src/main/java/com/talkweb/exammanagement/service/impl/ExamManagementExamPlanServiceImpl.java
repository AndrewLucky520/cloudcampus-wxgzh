package com.talkweb.exammanagement.service.impl;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.dao.ExamManagementSetDao;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.domain.ExamSubject;
import com.talkweb.exammanagement.enums.EnumSubjectLevel;
import com.talkweb.exammanagement.service.ExamManagementExamPlanService;
import com.talkweb.schedule.entity.TSchTClassInfoExternal;
import com.talkweb.schedule.entity.TSchTClassInfoExternal.TSchSubjectInfo;
import com.talkweb.schedule.service.ScheduleExternalService;

@Service
public class ExamManagementExamPlanServiceImpl implements ExamManagementExamPlanService {
	Logger logger = LoggerFactory.getLogger(ExamManagementExamPlanServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ExamManagementDao examManagementDao;
	@Autowired
	private ExamManagementSetDao examManagementSetDao;

	@Autowired
	private ScheduleExternalService scheduleExternalService;

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	public List<JSONObject> getExamPlanList(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}

		Integer autoIncr = em.getAutoIncr();

		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(examPlanList)) {
			return new ArrayList<JSONObject>();
		}

		request.put("examPlanList", examPlanList);
		List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(request, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(examSubjectList)) {
			return new ArrayList<JSONObject>();
		}

		Map<String, ExamPlan> examPlanId2Obj = new HashMap<String, ExamPlan>();
		for (ExamPlan ep : examPlanList) {
			examPlanId2Obj.put(ep.getExamPlanId(), ep);
		}

		// 使用年级对应年级名字
		Map<String, String> usedGrade2Name = new HashMap<String, String>();

		String xn = termInfo.substring(0, termInfo.length() - 1);

		List<JSONObject> result = new ArrayList<JSONObject>(examSubjectList.size());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		for (ExamSubject es : examSubjectList) {
			String examPlanId = es.getExamPlanId();
			ExamPlan ep = examPlanId2Obj.get(examPlanId);
			String usedGrade = ep.getUsedGrade();

			if (!usedGrade2Name.containsKey(usedGrade)) {
				T_GradeLevel gl = T_GradeLevel
						.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
				usedGrade2Name.put(usedGrade, njName.get(gl));
			}

			JSONObject json = new JSONObject();
			json.put("examPlanId", examPlanId);
			json.put("gradeName", usedGrade2Name.get(usedGrade));
			json.put("usedGrade", usedGrade);
			json.put("date", dateFormat.format(es.getStartTime()));
			json.put("sort", es.getStartTime().getTime());
			json.put("time", timeFormat.format(es.getStartTime()) + " - " + timeFormat.format(es.getEndTime()));
			json.put("subjectName", es.getExamSubjName());
			json.put("subjectId", es.getSubjectId());
			json.put("subjectLevel", es.getSubjectLevel());
			result.add(json);
		}

		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String usedGrade1 = o1.getString("usedGrade");
				String usedGrade2 = o2.getString("usedGrade");
				int result = -usedGrade1.compareTo(usedGrade2);
				if (result != 0) {
					return result;
				}
				long sort1 = o1.getLongValue("sort");
				long sort2 = o2.getLongValue("sort");
				result = Long.compare(sort1, sort2);
				if(result != 0) {
					return result;
				}
				long subjectId1 = o1.getLongValue("subjectId");
				long subjectId2 = o2.getLongValue("subjectId");
				result = Long.compare(subjectId1, subjectId2);
				if(result != 0) {
					return result;
				}
				int subjectLevel1 = o1.getIntValue("subjectLevel");
				int subjectLevel2 = o2.getIntValue("subjectLevel");
				return Integer.compare(subjectLevel1, subjectLevel2);
			}
		});

		return result;
	}

	public void deleteExamPlan(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if (em == null) {
			
		}
		Integer autoIncr = em.getAutoIncr();
		Map<String,Object> param=new HashMap<String, Object>();
		
		try{
			ObjectMapper mapper = new ObjectMapper();  
			param = mapper.readValue(request.toJSONString(),Map.class);
			param.put("autoIncr", autoIncr);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("json转map异常");
		}
		if(request.get("isQueryOrDelete").toString().equals("0")){
			List<JSONObject> d=examManagementSetDao.getStudsInExamPlace(param);
			if(!CollectionUtils.isEmpty(d)){
				throw new CommonRunException(-2, "年级下已经有学生排考，不允许修改考试计划！");
			}
		}else{
			examManagementDao.deleteExamSubject(request, termInfo, autoIncr);
			examManagementDao.deleteExamPlan(request, termInfo, autoIncr);
		}
	
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		//if (CollectionUtils.isEmpty(examPlanList)) {
		//	em.setStatus(0);
		//	examManagementDao.updateExamManagementStatus(em, termInfo);
		//}
	}

	public JSONObject getExamPlan(JSONObject request) {
		String termInfo = request.getString("termInfo");
		ExamManagement em = examManagementDao.getExamManagementListById(request, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		JSONObject result = new JSONObject();
		Map<String,Object> param=new HashMap<String, Object>();
		
		try{
			ObjectMapper mapper = new ObjectMapper();  
			param = mapper.readValue(request.toJSONString(),Map.class);
			param.put("autoIncr", autoIncr);
		}catch(Exception e){
			e.printStackTrace();
			logger.error("json转map异常");
		}
		if(request.get("isQueryOrEdit").toString().equals("0")){
			List<JSONObject> d=examManagementSetDao.getStudsInExamPlace(param);
			if(!CollectionUtils.isEmpty(d)){
				throw new CommonRunException(-2, "年级下已经有学生排考，不允许修改考试计划！");
			}
		}
		
		List<ExamPlan> examPlanList = examManagementDao.getExamPlanList(request, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(examPlanList)) {
			throw new CommonRunException(-1, "没有查询到相应的考试计划信息，请刷新页面！");
		}
		
		ExamPlan ep = examPlanList.get(0);
		result.put("examPlanId", ep.getExamPlanId());
		result.put("usedGrade", ep.getUsedGrade());
		result.put("scheduleId", ep.getScheduleId());
		List<JSONObject> list = new ArrayList<JSONObject>();
		
		List<ExamSubject> examSubjectList = examManagementDao.getExamSubjectList(request, termInfo, autoIncr);
		if (CollectionUtils.isNotEmpty(examSubjectList)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			for (ExamSubject es : examSubjectList) {
				JSONObject json = new JSONObject();
				json.put("examSubjectId", es.getExamSubjectId());
				json.put("date", dateFormat.format(es.getStartTime()));
				json.put("startTime", timeFormat.format(es.getStartTime()));
				json.put("endTime", timeFormat.format(es.getEndTime()));
				json.put("subjectId", es.getSubjectId());
				json.put("subjectLevel", es.getSubjectLevel());
				json.put("sort", es.getStartTime().getTime());
				list.add(json);
			}
			Collections.sort(list, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					long sort1 = o1.getLongValue("sort");
					long sort2 = o2.getLongValue("sort");
					int result = Long.compare(sort1, sort2);
					if(result != 0) {
						return result;
					}
					long subjectId1 = o1.getLongValue("subjectId");
					long subjectId2 = o2.getLongValue("subjectId");
					result = Long.compare(subjectId1, subjectId2);
					if(result != 0) {
						return result;
					}
					int subjectLevel1 = o1.getIntValue("subjectLevel");
					int subjectLevel2 = o2.getIntValue("subjectLevel");
					return Integer.compare(subjectLevel1, subjectLevel2);
				}
			});
		}
		
		result.put("examSubjectList", list);
		return result;
	}

	public void saveExamPlan(JSONObject request) throws ParseException {
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		String examManagementId = request.getString("examManagementId");
		String scheduleId = request.getString("scheduleId");
		Long schoolId = request.getLong("schoolId");
		String usedGrade = request.getString("usedGrade");
		String isQueryOrEdit=request.getString("isQueryOrEdit");
		// 新增添加此字段为空，修改此字段有值
		String examPlanId = request.getString("examPlanId");

		JSONObject params = new JSONObject();
		params.put("termInfo", termInfo);
		params.put("examManagementId", examManagementId);
		params.put("schoolId", schoolId);
		logger.info("params:"+params+"\n termInfo:"+termInfo);
		ExamManagement em = examManagementDao.getExamManagementListById(params, termInfo);
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		logger.info("autoIncr:"+autoIncr);
		JSONArray list = request.getJSONArray("examSubjectList");
		if (CollectionUtils.isEmpty(list)) {
			throw new CommonRunException(-1, "考试科目不能为空，请设置考试科目！");
		}
		request.remove("examSubjectList");

		params.put("usedGrade", usedGrade);
		ExamPlan ep = null;
		List<ExamPlan> epList = examManagementDao.getExamPlanList(params, termInfo, autoIncr);
		if (CollectionUtils.isEmpty(epList)) {
			ep = new ExamPlan();
			ep.setExamPlanId(examPlanId);
			if (StringUtils.isBlank(ep.getExamPlanId())) {
				ep.setExamPlanId(UUIDUtil.getUUID());
			}
			ep.setExamManagementId(examManagementId);
			ep.setSchoolId(schoolId);
			ep.setUsedGrade(usedGrade);
			ep.setScheduleId(request.getString("scheduleId"));
			ep.setTermInfo(termInfo);
		} else {
			ep = epList.get(0);
			if (!ep.getScheduleId().equals(request.getString("scheduleId"))) {
				throw new CommonRunException(-2, "年级下已经有学生排考，不允许新增考试计划！");
			}
		}
		params.remove("usedGrade");

		params.put("examPlanId", ep.getExamPlanId());
		List<ExamSubject> examSubjectList = null;
		if (StringUtils.isBlank(examPlanId)) { // 如果examPlanId为空，表示新增数据
			logger.info("params:"+params);
			logger.info("termInfo:"+termInfo);
			logger.info("autoIncr:"+autoIncr);
			examSubjectList = examManagementDao.getExamSubjectList(params, termInfo, autoIncr);
		} else { // 如果examPlanId不为空，则是修改数据，修改数据会将前台所有数据传至后台，因此不用在查询数据库了，否则会在下面判冲突时抛异常
			
			examSubjectList = new ArrayList<ExamSubject>();
		}

		School school = commonDataService.getSchoolById(schoolId, termInfo);
		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoList(school, termInfo);
		Map<Long, LessonInfo> subjectId2Obj = new HashMap<Long, LessonInfo>();
		if (CollectionUtils.isNotEmpty(lessonInfoList)) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				subjectId2Obj.put(lessonInfo.getId(), lessonInfo);
			}
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		for (Object obj : list) { // 组装来自前台的数据
			JSONObject json = (JSONObject) obj;
			ExamSubject es = new ExamSubject();
			es.setSchoolId(schoolId);
			es.setExamManagementId(examManagementId);
			es.setTermInfo(termInfo);
			es.setExamSubjectId(json.getString("examSubjectId"));
			es.setExamPlanId(ep.getExamPlanId());
			es.setStartTime(format.parse(json.getString("startTime")));
			es.setEndTime(format.parse(json.getString("endTime")));
			es.setSubjectId(json.getLong("subjectId"));
			es.setSubjectLevel(json.getInteger("subjectLevel"));

			LessonInfo lessonInfo = subjectId2Obj.get(json.getLong("subjectId"));
			String examSubjectName = lessonInfo.getName();
			String examSubjSimpleName = lessonInfo.getSimpleName();
			if (json.getInteger("subjectLevel") != null && json.getInteger("subjectLevel") != 0) {
				examSubjectName += EnumSubjectLevel.findNameByValueWithBrackets(json.getInteger("subjectLevel"));
				examSubjSimpleName += EnumSubjectLevel
						.findSimpleNameByValueWithBrackets(json.getInteger("subjectLevel"));
			}
			es.setExamSubjName(examSubjectName);
			es.setExamSubjSimpleName(examSubjSimpleName);

			if (StringUtils.isBlank(es.getExamSubjectId())) {
				es.setExamSubjectId(UUIDUtil.getUUID());
			}
			examSubjectList.add(es);
		}

		// 给所有数据按开始时间排序，以便后面生成场次
		Collections.sort(examSubjectList, new Comparator<ExamSubject>() {
			@Override
			public int compare(ExamSubject o1, ExamSubject o2) {
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		});

		// ******************************生成场次********************************//
		// 检测冲突依据，subjectId_subjectLevel映射场次
		Map<String, Integer> checkConflictBasis = new HashMap<String, Integer>();

		int scene = 1; // 第1场次
		Date endTime = examSubjectList.get(0).getEndTime();

		for (ExamSubject es : examSubjectList) {
			// 因为按日期和开始时间排了序，因此只需要判断日期是否相等，如果日期相等才需要判断开始时间
			if (endTime.before(es.getStartTime())) {
				endTime = es.getEndTime();
				++scene;
			}
			es.setScene(scene);

			String key = es.getSubjectId() + "_" + es.getSubjectLevel();
			// 冲突需要判断_0的所有科目；
			if (es.getSubjectLevel() != 0 && (checkConflictBasis.containsKey(key) 
					|| checkConflictBasis.containsKey(es.getSubjectId() + "_0"))) {
				throw new CommonRunException(-2, "存在已设置过的考试科目和类型，请检查相关选项！");
			} else if(es.getSubjectLevel() == 0) {
				for(String subjIdAndLevel : checkConflictBasis.keySet()) {
					if(subjIdAndLevel.startsWith(String.valueOf(es.getSubjectId()))) {
						throw new CommonRunException(-2, "存在已设置过的考试科目和类型，请检查相关选项！");
					}
				}
			}
			checkConflictBasis.put(key, scene);
		}

		// ******************************校验考试科目所在场次是否合法********************************//
		// 校验学生，是否同一场场次有2个相同科目需要考试
		if (StringUtils.isNotBlank(scheduleId)) {
			// 获取年级下的所有学生信息（所选科目），并且判断是否所选科目有冲突（同场次同一学生有不同的考试科目）
			// 因为有可能走班，因此需要统计学生志愿，统计一名学生需要上哪些课程
			Map<Long, List<String>> accId2Wish = new HashMap<Long, List<String>>();
			List<TSchTClassInfoExternal> classInfoList = scheduleExternalService.getTClassInfoExternal(scheduleId,
					schoolId, termInfo, usedGrade, null);
			for (TSchTClassInfoExternal classInfo : classInfoList) {
				List<String> wishList = new ArrayList<String>();
				for (TSchSubjectInfo subjInfo : classInfo.getSubjectList()) {
					String wishStr = subjInfo.getSubjectId() + "_" + subjInfo.getSubjectLevel();
					wishList.add(wishStr);
				}
				for (Long accId : classInfo.getStudentIdList()) {
					if (!accId2Wish.containsKey(accId)) {
						accId2Wish.put(accId, new ArrayList<String>());
					}
					accId2Wish.get(accId).addAll(wishList);
				}
			}

			// 循环学生自愿，判断是否存在同一个学生在同一场次拥有2场考试
			for (Map.Entry<Long, List<String>> entry : accId2Wish.entrySet()) {
				List<String> wishList = entry.getValue();
				Map<Integer, String> check = new HashMap<Integer, String>();
				for (String key : wishList) {
					String[] tmp = key.split("_");
					Long subjectId = Long.valueOf(tmp[0]);
					int subjectLevel = Integer.valueOf(tmp[1]);

					Integer sceneNum = checkConflictBasis.get(key);
					if (sceneNum == null) {
						subjectLevel = 0;
						sceneNum = checkConflictBasis.get(subjectId + "_0");
					}
					if(sceneNum == null) {
						continue;
					}
					
					if (check.containsKey(sceneNum)) {
						// 抛出考试冲突异常，同一个场次同一个班级拥有多个科目考试
						StringBuffer msg = new StringBuffer()
								.append(subjectId2Obj.get(subjectId).getName()
										+ EnumSubjectLevel.findSimpleNameByValueWithBrackets(subjectLevel))
								.append("和").append(check.get(sceneNum)).append("产生场次冲突，请检查相关选项！");
						throw new CommonRunException(-2, msg.toString());
					}
					check.put(sceneNum, subjectId2Obj.get(subjectId).getName()
							+ EnumSubjectLevel.findSimpleNameByValueWithBrackets(subjectLevel));
				}
			}
		} 
		// 行政班排考，可变因素太多，有些可能都没任教关系，因此按行政班排考不考虑场次冲突
		/*else { // 如果scheduleId是空白的，那么不是新高考分班
			T_GradeLevel gradeLevel = T_GradeLevel
					.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
			Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gradeLevel, termInfo);
			if (CollectionUtils.isEmpty(grade.getClassIds())) {
				throw new CommonRunException(-1, njName.get(gradeLevel) + "没有班级信息，请联系管理员！");
			}
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), termInfo);
			if (CollectionUtils.isEmpty(classrooms)) {
				throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
			}
			for (Classroom classroom : classrooms) { // 一个班级对应多个学生，而一个学生只能属于一个班级，因此只需要判断班级任教是否冲突
				List<AccountLesson> accountLessonList = classroom.getAccountLessons(); // 任教关系
				if (CollectionUtils.isEmpty(accountLessonList)) {
					continue;
				}

				Set<Long> subjectIdSet = new HashSet<Long>();
				for (AccountLesson accountLesson : accountLessonList) {
					subjectIdSet.add(accountLesson.getLessonId());
				}

				Map<Integer, Long> check = new HashMap<Integer, Long>();
				for (Long subjectId : subjectIdSet) {
					String key = subjectId + "_" + EnumSubjectLevel.OTHER.getValue();
					Integer sceneNum = checkConflictBasis.get(key);
					if (sceneNum == null) {
						continue;
					}
					if (check.containsKey(sceneNum)) {
						// 抛出考试冲突异常，同一个场次同一个班级拥有多个科目考试
						StringBuffer msg = new StringBuffer().append(subjectId2Obj.get(subjectId).getName()).append("和")
								.append(subjectId2Obj.get(check.get(sceneNum)).getName()).append("产生场次冲突，请检查相关选项！");
						throw new CommonRunException(-2, msg.toString());
					}
					check.put(sceneNum, subjectId);
				}
			}
		}  */

		// 先删除原来的数据
		examManagementDao.deleteExamSubject(params, termInfo, autoIncr);
		examManagementDao.deleteExamPlan(params, termInfo, autoIncr);

		examManagementDao.insertExamPlan(ep, termInfo, autoIncr);
		examManagementDao.insertExamSubjectBatch(examSubjectList, termInfo, autoIncr);
		
		em.setStatus(1);
		examManagementDao.updateExamManagementStatus(em, termInfo);
	}
	
	public static void main(String a[]){
		JSONObject b=new JSONObject();
		b.put("a", "a");
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String,Object> productMap = mapper.readValue(b.toJSONString(),Map.class);
			System.out.println(123123);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
