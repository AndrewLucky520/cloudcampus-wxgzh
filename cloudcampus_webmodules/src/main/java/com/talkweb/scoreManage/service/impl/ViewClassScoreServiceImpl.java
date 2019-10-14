package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.action.ScoreCommonAction;
import com.talkweb.scoreManage.dao.ClassScoreCrudDao;
import com.talkweb.scoreManage.dao.ViewClassScoreDao;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;
import com.talkweb.scoreManage.service.ViewClassScoreService;

@Service
public class ViewClassScoreServiceImpl implements ViewClassScoreService {

	@Autowired
	private ViewClassScoreDao viewClassScoreDao;

	@Autowired
	private ClassScoreCrudDao classScoreCrudDao;

	@Autowired
	private AllCommonDataService allCommonDataService;
	
	private static final Logger logger = LoggerFactory.getLogger(ScoreCommonAction.class);


	@Override
	public List<JSONObject> getClassInfoDropDownList(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		Long schoolId = params.getLong("schoolId");
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = exam.getAutoIncr();

		List<JSONObject> result = new ArrayList<JSONObject>();

		List<Long> classIds = viewClassScoreDao.getClassIdsInClassExam(termInfoId, autoIncr, params);
		if (classIds.size() == 0) {
			return result;
		}

		List<Classroom> classrooms = allCommonDataService.getClassroomBatch(schoolId, classIds, termInfoId);
		if (CollectionUtils.isEmpty(classrooms)) {
			return result;
		}

		for (Classroom classroom : classrooms) {// 循环班级信息列表，把数据按照接口格式组织起来，并且放置到List中
			JSONObject item = new JSONObject();
			item.put("value", classroom.getId());
			item.put("text", classroom.getClassName());
			result.add(item);
		}

		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				return arg1.getString("text").compareTo(arg2.getString("text"));
			}
		});

		return result;
	}

	@Override
	public JSONObject getClassExamScore(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer type = exam.getExamType(); // 1：系统格式，2：自定义考试

		String classIdStr = params.getString("classIdStr");
		params.remove("classIdStr");
		params.put("classIdList", StringUtil.convertToListFromStr(classIdStr, ",", String.class));

		JSONObject data = new JSONObject();
		
		Long schoolId = params.getLong("schoolId");
		List<Long> classIdList = StringUtil.convertToListFromStr(classIdStr, ",", Long.class);
		// 班级信息列表
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfoId);
		Map<Long, Classroom> classId2NameMap = new HashMap<Long, Classroom>();
		if (classroomList != null) {
			for (Classroom classroom : classroomList) {
				classId2NameMap.put(classroom.getId(), classroom);
			}
		}

		// 1.1设置获取学生信息参数
		HashMap<String, Object> studentListParam = new HashMap<String, Object>();
		studentListParam.put("schoolId", params.getString("schoolId"));
		studentListParam.put("classId", classIdStr);
		studentListParam.put("termInfoId", termInfoId);
		studentListParam.put("keyword", params.getString("keyword"));
		// 学生信息列表
		List<Account> studentList = allCommonDataService.getStudentList(studentListParam);
		if (CollectionUtils.isEmpty(studentList)) {
			return data;
		}

		Map<String, JSONObject> stuId2ObjMap = new HashMap<String, JSONObject>();// 学生信息Map
		if (!CollectionUtils.isEmpty(studentList)) {
			for (Account acc : studentList) {// 把学生编号和学生信息放置到Map结构中
				if(acc == null) {
					continue;
				}
				
				String id = String.valueOf(acc.getId());
				if (stuId2ObjMap.containsKey(id)) {
					continue;
				}
				
				List<User> users = acc.getUsers();
				if(users == null) {
					continue;
				}
				for(User user : users) {
					if(user == null || user.getStudentPart() == null) {
						continue;
					}
					StudentPart stuPart = user.getStudentPart();
					
					JSONObject json = new JSONObject();
					json.put("classId", stuPart.getClassId());
					json.put("className", classId2NameMap.get(stuPart.getClassId()).getClassName());
					json.put("name", acc.getName());
					json.put("schoolNumber", stuPart.getSchoolNumber());
					
					stuId2ObjMap.put(id, json);
					break;
				}
			}
		}

		params.put("studentIds", stuId2ObjMap.keySet()); // 设置学生列表参数

		if (type == 1) {// 系统格式
			data = this.getSystemFormatScore(exam, stuId2ObjMap, params);
		} else {	// 自定义格式
			data = this.getCustomFormatScore(exam, stuId2ObjMap, params);
		}
		return data;
	}

	private JSONObject getSystemFormatScore(ClassExamInfo exam, Map<String, JSONObject> stuId2ObjMap, JSONObject params) {
		JSONObject data = new JSONObject();
		
		Long schoolId = params.getLong("schoolId");
		String termInfoId = exam.getTermInfoId();
		Integer autoIncr = exam.getAutoIncr();
		Integer scoreDataType = exam.getScoreDataType(); // examType=1时，即系统格式：0：非数值类型，1：数值类型
		// 2. 组织返回的data 的表头已经数据
		// 2.1 从数据库中读取科目列表，单科成绩，总分成绩
		// 科目列表
		List<Long> subjIds = viewClassScoreDao.getClassExamSubjectIdList(termInfoId, autoIncr, params);
		// 学生单科成绩列表
		List<ClassExamSubjectScore> subjectScoreList = viewClassScoreDao.getClassExamSubjectScoreList(termInfoId,
				autoIncr, params);
		// 学生总分列表
		List<StudentTotalScoreStatistics> totalScoreList = viewClassScoreDao.getClassExamTotalScore(termInfoId,
				autoIncr, params);

		// 2.2 学生单科成绩已经总分成绩放置到Map中，以便在循环数据的时候提交处理性能
		// 学生科目成绩信息Map,以classId+studentId+subjectId作为键
		Map<String, ClassExamSubjectScore> subjectScoreMap = new HashMap<String, ClassExamSubjectScore>();
		for (ClassExamSubjectScore subjectScore : subjectScoreList) {
			String key = subjectScore.getClassId() + subjectScore.getStudentId() + subjectScore.getSubjectId();
			if (!subjectScoreMap.containsKey(key)) {
				subjectScoreMap.put(key, subjectScore);
			}
		}

		// 学生总分成绩信息Map,以classId+studentId作为键
		Map<String, StudentTotalScoreStatistics> totalScoreMap = new HashMap<String, StudentTotalScoreStatistics>();
		for (StudentTotalScoreStatistics statistics : totalScoreList) {
			String key = statistics.getClassId() + statistics.getStudentId();
			if (!totalScoreMap.containsKey(key)) {
				totalScoreMap.put(key, statistics);
			}
		}

		// 2.4 组织数据列表
		List<Map<String, Object>> scoreDataList = new ArrayList<Map<String, Object>>(); // 学生成绩列表

		for (Map.Entry<String, JSONObject> entry : stuId2ObjMap.entrySet()) { // 循环学生编号列表，逐一的组织每个学生的成绩
			String studentId = entry.getKey();
			JSONObject obj = entry.getValue();
			
			Map<String, Object> scoreDataItem = new HashMap<String, Object>();// 学生成绩列表每项数据存放集合
			scoreDataItem.put("studentName", obj.getString("name"));// 设置学生姓名
			scoreDataItem.put("className", obj.getString("className"));// 设置班级名称

			String classId = obj.getString("classId");
			String firstSubjectId = subjIds.size() == 0 ? "" : String.valueOf(subjIds.get(0));
			if (!subjectScoreMap.containsKey(classId + studentId + firstSubjectId)) {// 如果不存在成绩,跳过该学生的成绩统计
				continue;
			}
			for (int i = 0; subjIds != null && i < subjIds.size(); i++) {// 循环考试科目列表，计算出学生每个科目的成绩
				String subjectId = StringUtil.transformString(subjIds.get(i));// 科目编号
				ClassExamSubjectScore subjectScore = subjectScoreMap.get(classId + studentId + subjectId);

				if (subjectScore != null) {
					if (scoreDataType == 1) { // 数字类型
						scoreDataItem.put(subjectId + "_score", subjectScore.getScore());
					} else {
						scoreDataItem.put(subjectId + "_score", subjectScore.getScore2());
					}
				}
			}
			// 设置学生总分班级排名
			StudentTotalScoreStatistics totalScore = totalScoreMap.get(classId + studentId);
			if (totalScore != null) {
				if (scoreDataType == 1) { // 数字类型
					scoreDataItem.put("classRank", totalScore.getTotalScoreRank());
				}
			}

			// 把每个学生的成绩添加到列表中
			scoreDataList.add(scoreDataItem);
		}

		data.put("rows", scoreDataList);// 设置数据

		// 2.3 组织数据列表表头
		EasyUIDatagridHead columns[][] = null;

		if (subjectScoreList != null && subjectScoreList.size() > 0
				|| totalScoreList != null && totalScoreList.size() > 0) {
			List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(StringUtil.transformLong(schoolId),
					subjIds, termInfoId);
			Map<Long, String> lessonInfoMap = new HashMap<Long, String>();
			for (int i = 0; lessonInfos != null && i < lessonInfos.size(); i++) {
				Long lessonId = lessonInfos.get(i).getId();
				if (!lessonInfoMap.containsKey(lessonId)) {
					lessonInfoMap.put(lessonId, lessonInfos.get(i).getName());
				}
			}

			if (subjIds != null) {
				if (scoreDataType == 1) { // 数字类型
					columns = new EasyUIDatagridHead[1][subjIds.size() + 1];
				} else {
					columns = new EasyUIDatagridHead[1][subjIds.size()];
				}
				for (int i = 0; i < subjIds.size(); i++) {
					columns[0][i] = new EasyUIDatagridHead(subjIds.get(i) + "_score", lessonInfoMap.get(subjIds.get(i)),
							"center", 60, 1, 1, true);
				}

				if (scoreDataType == 1) { // 数字类型
					columns[0][subjIds.size()] = new EasyUIDatagridHead("classRank", "班级排名", "center", 80, 1, 1, true);
				}
			} else {
				if (scoreDataType == 1) { // 数字类型
					columns = new EasyUIDatagridHead[1][1];
					columns[0][0] = new EasyUIDatagridHead("classRank", "班级排名", "center", 80, 1, 1, true);
				}
			}
		}

		data.put("columns", columns);// 设置表头
		return data;
	}

	// 自定义格式分数
	private JSONObject getCustomFormatScore(ClassExamInfo exam, Map<String, JSONObject> stuId2ObjMap, JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		Integer autoIncr = exam.getAutoIncr();

		JSONObject data = new JSONObject();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<List<JSONObject>> columns = new ArrayList<List<JSONObject>>();
		List<JSONObject> frozenCol = new ArrayList<JSONObject>();
		List<CustomScore> l_se = viewClassScoreDao.getAllCustomExcel(termInfoId, autoIncr, params);
		List<CustomScoreInfo> l_sd = viewClassScoreDao.getAllCustomDetail(termInfoId, autoIncr, params);

		Map<String, String> frozenColumns = new HashMap<String, String>();// 存放冻结列

		Integer headRowNum = 0;
		if (l_se.size() > 0) {
			headRowNum = l_se.get(0).getTitleCount();
		}
		for (int i = 0; i < headRowNum; i++) {
			List<JSONObject> l_json = new ArrayList<JSONObject>();
			for (CustomScore se : l_se) {
				int rowNum = se.getRow();
				int rowspan = se.getRowSpan();
				int colSpan = se.getColSpan();
				String projectId = se.getProjectId();
				String projectName = se.getProjectName();

				if (rowNum == i) {
					JSONObject headInfo = new JSONObject();
					if (headRowNum == rowNum + rowspan) {
						headInfo.put("field", projectId);
					} else {
						headInfo.put("field", null);
					}
					headInfo.put("title", projectName);
					headInfo.put("align", "center");
					headInfo.put("width", 100);
					headInfo.put("rowspan", rowspan);
					headInfo.put("colspan", colSpan);
					headInfo.put("sortable", false);
					if (frozenColumns.containsKey(projectId)) {
						frozenCol.add(headInfo);
					} else {
						l_json.add(headInfo);
					}
				}
			}

			columns.add(l_json);
		}

		Map<String, JSONObject> stuId2RowMap = new HashMap<String, JSONObject>();
		for (CustomScoreInfo sd : l_sd) {
			String stuId = sd.getStudentId();
			
			JSONObject stuObj = stuId2ObjMap.get(stuId);
			
			String projectId = sd.getProjectId();
			String score = sd.getScore();

			JSONObject rowInfo = stuId2RowMap.get(stuId);
			if (rowInfo == null) {
				rowInfo = new JSONObject();
				stuId2RowMap.put(stuId, rowInfo);
			}

			if ("xm".equals(score)) {
				String name = "";
				if (stuObj != null) {
					name = stuObj.getString("name");
				}
				rowInfo.put(projectId, name);
				frozenColumns.put(projectId, projectId);
			} else if ("userXh".equals(score)) {
				String xh = "";
				if (stuObj != null) {
					xh = stuObj.getString("schoolNumber");
				}
				rowInfo.put(projectId, xh);
				frozenColumns.put(projectId, projectId);
			} else if ("bjmc".equals(score)) {
				String className = "";
				if(stuObj != null) {
					className = stuObj.getString("className");
				}
				rowInfo.put(sd.getProjectId(), className);
				frozenColumns.put(projectId, projectId);
			} else {
				String name = "";
				if(stuObj != null) {
					name = stuObj.getString("name");
				}
				String className = "";
				if(stuObj != null) {
					className = stuObj.getString("className");
				}
				rowInfo.put("r0c0", name);
				rowInfo.put("r0c1", className);
				rowInfo.put(projectId, score);
			}
		}
		rows.addAll(stuId2RowMap.values());

		data.put("total", stuId2RowMap.size());
		data.put("rows", rows);
		if (rows.size() > 0) {
			data.put("columns", columns);
			data.put("frozenColumns", frozenCol);
		}
		return data;
	}
	
	@Override
	public List<JSONObject> getClassExamListByTeacher(JSONObject params) {
		Long schoolId = params.getLong("schoolId");
		Long accountId = params.getLong("accountId");
		String termInfoId = params.getString("termInfoId");
		System.out.println();
		Account acc = allCommonDataService.getAccountAllById(schoolId, accountId, termInfoId);
		if(acc == null || acc.getUsers() == null) {
			logger.info("getClassExamListByTeacher:无法从SDK获取教师信息！");
			return new ArrayList<JSONObject>();
			//throw new CommonRunException(-1, "无法从SDK获取教师信息！");
		}
		List<String> classIds = new ArrayList<String>();
		 
		for(User user : acc.getUsers()) {
			if(user.getTeacherPart() == null) {
				continue;
			}
			List<Long> deanClassIds= user.getTeacherPart().getDeanOfClassIds();
			if (deanClassIds!=null) {
				for (int i = 0; i < deanClassIds.size(); i++) {
					classIds.add(String.valueOf(deanClassIds.get(i)));
				}
			}
			List<Course> courses = user.getTeacherPart().getCourseIds();
			logger.info("classIds===2>" + courses);
			if(courses == null) {
				continue;
			}
			for(Course course : courses) {
				Long classId = course.getClassId();
				if(!classIds.contains(classId)) {
					classIds.add(String.valueOf(classId));
				}
			}
			
			break;
		}
		if(classIds.size() == 0) {
			return new ArrayList<JSONObject>();
		}
		params.put("classIds", classIds);
		return viewClassScoreDao.getClassExamListByTeacher(termInfoId, params);
	}
	
	@Override
	public List<JSONObject> getClassListByTeacher(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo exam = classScoreCrudDao.getClassExamInfoById(termInfoId, params);
		if (exam == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Long schoolId = params.getLong("schoolId");
		Long accountId = params.getLong("accountId");
		Account acc = allCommonDataService.getAccountAllById(schoolId, accountId, termInfoId);
		if(acc == null || acc.getUsers() == null) {
			throw new CommonRunException(-1, "无法从SDK获取教师信息！");
		}
		List<String> classIds = new ArrayList<String>();
		for(User user : acc.getUsers()) {
			if(user.getTeacherPart() == null) {
				continue;
			}
			List<Long> clasList = user.getTeacherPart().getDeanOfClassIds();
			if (clasList!=null) {
				for (int i = 0; i < clasList.size(); i++) {
					classIds.add(String.valueOf(clasList.get(i)));
				}
			}
			
			
			List<Course> courses = user.getTeacherPart().getCourseIds();
			if(courses == null) {
				continue;
			}
			for(Course course : courses) {
				Long classId = course.getClassId();
				if(!classIds.contains(classId)) {
					classIds.add(String.valueOf(classId));
				}
			}
			break;
		}
		
		if(classIds.size() == 0) {
			return new ArrayList<JSONObject>();
		}
		params.put("classIds", classIds);
		List<Long> classIdList = viewClassScoreDao.getClassIdFromClassExam(termInfoId, exam.getAutoIncr(), params);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIdList, termInfoId);
		
		StringBuffer all = new StringBuffer();
		List<JSONObject> list = new ArrayList<JSONObject>();
		for(Classroom classroom : classroomList) {
			all.append(classroom.getId()).append(",");
			
			JSONObject obj = new JSONObject();
			obj.put("text", classroom.getClassName());
			obj.put("value", classroom.getId());
			list.add(obj);
		}
		
		Collections.sort(list, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String text1 = o1.getString("text");
				if(text1 == null) {
					text1 = "";
				}
				String text2 = o2.getString("text");
				if(text2 == null) {
					text2 = "";
				}
				return text1.compareTo(text2);
			}
		});
		
		if(params.getIntValue("isAll") == 1) {
			all.deleteCharAt(all.length() - 1);
			JSONObject obj = new JSONObject();
			obj.put("text", "全部");
			obj.put("value", all.toString());
			list.add(0, obj);
		}
		
		return list;
	}
}
