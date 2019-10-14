package com.talkweb.scoreManage.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.ParentPart;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.AppScoreReportDao;
import com.talkweb.scoreManage.dao.ClassScoreCrudDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.StudentScoreReportTrace;
import com.talkweb.scoreManage.service.AppScoreReportService;

@Service
public class AppScoreReportServiceImpl implements AppScoreReportService {

	@Autowired
	private AppScoreReportDao appDao;

	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private ClassScoreCrudDao classScoreDao;

	@Autowired
	private AllCommonDataService allCommonDataService;
	
	
	private static final Logger logger = LoggerFactory.getLogger(AppScoreReportServiceImpl.class);


	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	public JSONObject getStudentInfo(String termInfoId, Long schoolId, Long userId, int examType) {
		if (termInfoId == null) {
			termInfoId = allCommonDataService.getCurTermInfoId(schoolId);
		}
		if (StringUtils.isBlank(termInfoId)) {
			throw new CommonRunException(-1, "无法获取当前学年学期，请联系管理员！");
		}
		// 读取用户信息，获取班级编号
		logger.info("schoolId , userId, termInfoId ==> "  + schoolId +" " + userId +" " + termInfoId);
		User user = allCommonDataService.getUserById(schoolId, userId, termInfoId);
		if (user == null || user.getAccountPart() == null || user.getUserPart() == null) {
			throw new CommonRunException(-1, "无法从基础数据中获取当前用户信息，请联系管理员！");
		}

		Long classId = null;
		Long studentId = null;
		Long accId = null;
		UserPart userPart = user.getUserPart();
		if (T_Role.Parent.equals(userPart.getRole())) {
			ParentPart parentPart = user.getParentPart();
			if (parentPart == null) {
				throw new CommonRunException(-1, "无法获取家长部分数据，请联系管理员！");
			}
			// 设置学生编号
			User studentUser = allCommonDataService.getUserById(schoolId, parentPart.getStudentId(), termInfoId);
			if (studentUser == null || studentUser.getAccountPart() == null) {
				throw new CommonRunException(-1, "无法通过家长获取学生数据，请联系管理员！");
			}
			classId = studentUser.getStudentPart().getClassId(); // parentPart.getClassId();
			studentId = studentUser.getAccountPart().getId();
			accId = user.getAccountPart().getId();
		} else if (T_Role.Student.equals(userPart.getRole())) {
			StudentPart studentPart = user.getStudentPart();
			if (studentPart == null) {
				throw new CommonRunException(-1, "无法获取学生部分数据，请联系管理员！");
			}
			classId = studentPart.getClassId();
			studentId = user.getAccountPart().getId();
			accId = user.getAccountPart().getId();
		} else {
			throw new CommonRunException(-3, "用户信息不为家长、学生，请联系管理员！");
		}

		Classroom classroom = allCommonDataService.getClassById(schoolId, classId, termInfoId);
		if (classroom == null) {
			throw new CommonRunException(-1, "无法获取班级信息，请联系管理员！");
		}
		long gradeId = classroom.getGradeId();
		Grade grade = allCommonDataService.getGradeById(schoolId, gradeId, termInfoId);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
		}
		String xn = termInfoId.substring(0, termInfoId.length() - 1);
		T_GradeLevel gl = grade.getCurrentLevel();
		String nj = allCommonDataService.ConvertNJDM2SYNJ(String.valueOf(gl.getValue()), xn);

		JSONObject result = new JSONObject();
		if (examType == 1) {
			result.put("bh", String.valueOf(classId)); // 班级代码
			result.put("xh", String.valueOf(studentId)); // 学生代码
			result.put("accountId", String.valueOf(accId)); // 学生代码
			result.put("xnxq", termInfoId); // 学年学期
			result.put("nj", nj); // 年级
			result.put("xn", xn); // 学年
			result.put("njdm", gl.getValue()); // 年级代码
			result.put("xxdm", String.valueOf(schoolId));
		} else {
			result.put("classId", String.valueOf(classId)); // 班级代码
			result.put("studentId", String.valueOf(studentId)); // 学生代码
			result.put("accountId", String.valueOf(accId)); // 学生代码
			result.put("termInfoId", termInfoId); // 学年学期
			result.put("usedGradeId", nj); // 年级
			result.put("xn", xn); // 学年
			result.put("njdm", gl.getValue()); // 年级代码
			result.put("schoolId", String.valueOf(schoolId));
		}
		return result;
	}

	public JSONObject getTeacherInfo(String termInfoId, Long schoolId, Long userId, int examType) {
		if (termInfoId == null) {
			termInfoId = allCommonDataService.getCurTermInfoId(schoolId);
		}
		if (StringUtils.isBlank(termInfoId)) {
			throw new CommonRunException(-1, "无法获取当前学年学期，请联系管理员！");
		}
		// 读取用户信息，获取班级编号
		User user = allCommonDataService.getUserById(schoolId, userId, termInfoId);
		if (user == null || user.getAccountPart() == null || user.getUserPart() == null) {
			throw new CommonRunException(-1, "无法从基础数据中获取当前用户信息，请联系管理员！");
		}

		UserPart userPart = user.getUserPart();
		if (!T_Role.Teacher.equals(userPart.getRole())) {
			throw new CommonRunException(-1, "用户信息不为老师，请联系管理员！");
		}

		TeacherPart teacherPart = user.getTeacherPart();
		if (teacherPart == null) {
			throw new CommonRunException(-1, "无法获取老师部分数据，请联系管理员！");
		}

		List<Course> courseList = teacherPart.getCourseIds();
		Long account = user.getUserPart().getAccountId();
		Account account2 = allCommonDataService.getAccountAllById(schoolId, account, termInfoId);
		List<User> users = account2.getUsers();
		List<Long> classIds = null;
		for (int i = 0; i < users.size(); i++) {
			User user2 = users.get(i);
			if (user2.getUserPart().getRole() == T_Role.Teacher) {
				classIds = user2.getTeacherPart().getDeanOfClassIds();
				break;
			}
		}
	 
		List<Long> classIdList = new ArrayList<Long>();
		logger.info(" classIds=== "  + classIds  );
		if (classIds !=null) {
			 classIdList.addAll(classIds);
		}
		
		List<Long> subjectIdList = new ArrayList<Long>();
		for (Course course : courseList) {
			Long classId = course.getClassId();
			Long lessonId = course.getLessonId();
			
			if(!classIdList.contains(classId)) {
				classIdList.add(classId);
			}
			if(!subjectIdList.contains(lessonId)) {
				subjectIdList.add(lessonId);
			}
		}
		

		StringBuffer classIdBuffer = new StringBuffer();
		for(Long classId : classIdList) {
			classIdBuffer.append(classId).append(",");
		}
		if(classIdBuffer.length() > 0) {
			classIdBuffer.deleteCharAt(classIdBuffer.length() - 1);
		}
		
		StringBuffer subjectIdBuffer = new StringBuffer();
		for(Long subjectId : subjectIdList) {
			subjectIdBuffer.append(subjectId).append(",");
		}
		if(subjectIdBuffer.length() > 0) {
			subjectIdBuffer.deleteCharAt(subjectIdBuffer.length() - 1);
		}
		
		JSONObject result = new JSONObject();
		if (examType == 1) {
			result.put("bhStr", classIdBuffer.toString()); // 班级代码
			result.put("kmdmStr", subjectIdBuffer.toString()); // 班级代码
			result.put("teacherId", String.valueOf(user.getAccountPart().getId())); // 老师代码
			result.put("xnxq", termInfoId); // 学年学期
			result.put("xxdm", String.valueOf(schoolId));
		} else {
			result.put("classIdStr", classIdBuffer.toString()); // 班级代码
			result.put("subjectIdStr", subjectIdBuffer.toString()); // 班级代码
			result.put("teacherId", String.valueOf(user.getAccountPart().getId())); // 老师代码
			result.put("termInfoId", termInfoId); // 学年学期
			result.put("schoolId", String.valueOf(schoolId));
		}
		return result;
	}

	@Override
	public List<JSONObject> getExamList(JSONObject params) {
		String xnxq = params.getString("xnxq");

		String nj = params.getString("nj");
		List<String> termInfoIds = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(allCommonDataService, nj, xnxq);

		List<JSONObject> examList = new ArrayList<JSONObject>();
		List<JSONObject> classExamTotalScoreInfoList = new ArrayList<JSONObject>();
		List<JSONObject> officialExamTotalScoreInfoList = new ArrayList<JSONObject>();
		List<JSONObject> officialExamAchievementList = new ArrayList<JSONObject>();
		params.remove("bh");//有学生转班情况，历史库就查不到数据了。
		if (termInfoIds.size() > 0) {
			for (String termInfoId : termInfoIds) {
				params.put("xnxq", termInfoId);
				List<JSONObject> examTmp = appDao.getExamList(termInfoId, params);
				for (JSONObject exam : examTmp) {
					Integer examType = exam.getInteger("examType");
					
					params.put("kslc", exam.getString("examId"));
					params.put("kslcmc", exam.get("examName"));
					params.put("examType", examType);
					Integer autoIncr = exam.getInteger("autoIncr");
					if(!appDao.ifExistsStuInExamScore(termInfoId, autoIncr, params)) {
						continue;
					}
					examList.add(exam);
					
					if (examType == 1) {
						officialExamTotalScoreInfoList
								.addAll(appDao.getOfficialExamTotalScoreInfoList(termInfoId, autoIncr, params));
						officialExamAchievementList
								.addAll(appDao.getOfficialExamAchievementList(termInfoId, autoIncr, params));
					} else if (examType == 2) {
						classExamTotalScoreInfoList
								.addAll(appDao.getClassExamTotalScoreInfoList(termInfoId, autoIncr, params));
					}
				}
			}
		}

		Collections.sort(examList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				Date date0 = arg0.getDate("time");
				Date date1 = arg1.getDate("time");
				return - date0.compareTo(date1);
			}
		});

		Map<String, JSONObject> classExamTotalScoreInfoMap = new HashMap<String, JSONObject>();
		for (JSONObject json : classExamTotalScoreInfoList) {
			String examId = json.getString("examId");
			classExamTotalScoreInfoMap.put(examId, json);
		}

		Map<String, JSONObject> officialExamTotalScoreInfoMap = new HashMap<String, JSONObject>();
		for (JSONObject json : officialExamTotalScoreInfoList) {
			String examId = json.getString("examId");
			officialExamTotalScoreInfoMap.put(examId, json);
		}

		Map<String, JSONObject> officialExamAchievementMap = new HashMap<String, JSONObject>();
		for (JSONObject json : officialExamAchievementList) {
			String examId = json.getString("examId");
			String studentId = json.getString("studentId");
			officialExamAchievementMap.put(examId + studentId, json);
		}

		String studentId = params.getString("xh");

		// 3.1 计算统考中每个学生的击败率
		Map<String, String> passRationMap = new HashMap<String, String>();// 击败比率Map
		for (JSONObject exam : examList) {
			if (exam != null) {
				String examId = exam.getString("examId");
				JSONObject curStudentAchive = officialExamAchievementMap.get(examId + studentId);
				float classRank = curStudentAchive == null ? 0 : curStudentAchive.getFloatValue("classRank");
				float joinNum = curStudentAchive == null ? 0 : curStudentAchive.getFloatValue("joinNum");

				String passRation = "";// 击败比率
				DecimalFormat format = new DecimalFormat("0");
				if (joinNum != 0) {
					passRation = format.format((1 - classRank / joinNum) * 100) + "%";
				}
				passRationMap.put(examId + studentId, passRation);
			}
		}

		// 3.2 设置每个考试的总分，总分等级，击败率
		for (JSONObject exam : examList) {
			if (exam == null) {
				continue;
			}
			// 考试类型 1：校内考试，统考 2班级测试
			Integer examType = exam.getInteger("examType");
			String examId = exam.getString("examId");// 考试编号
			if (examType == 1) {// 校内考试，统考
				JSONObject officialTotalScoreInfo = officialExamTotalScoreInfoMap.get(examId);
				Object totalScore = officialTotalScoreInfo == null ? "" : officialTotalScoreInfo.get("totalScore");
				Object totalScoreLevel = officialTotalScoreInfo == null ? ""
						: officialTotalScoreInfo.get("totalScoreLevel");

				exam.put("totalScore", totalScore);
				exam.put("totalScoreLevel", totalScoreLevel);
				exam.put("achievement", passRationMap.get(examId + studentId));
			} else if (examType == 2) { // 班级内部考试
				Map<String, Object> totalScoreInfo = classExamTotalScoreInfoMap.get(examId);
				Object totalScore = totalScoreInfo == null ? "" : totalScoreInfo.get("totalScore");
				Object surpassRatio = totalScoreInfo == null ? "" : totalScoreInfo.get("surpassRatio");
				exam.put("totalScore", totalScore);
				exam.put("totalScoreLevel", "");
				exam.put("achievement", surpassRatio);
			}
		}
		return examList;
	}

	@Override
	public JSONObject getSchoolExamStudentScoreReport(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			logger.error("getSchoolExamStudentScoreReport:" +ERR_MSG_3 + params.toJSONString());
			return null;
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");

		Map<Long, LessonInfo> lessonInfoMap = new HashMap<Long, LessonInfo>();
		School school = allCommonDataService.getSchoolById(schoolId, xnxq);
		List<LessonInfo> lessonInfoList = allCommonDataService.getLessonInfoList(school, xnxq);
		if (lessonInfoList != null) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				if (lessonInfo == null) {
					continue;
				}
				lessonInfoMap.put(lessonInfo.getId(), lessonInfo);
			}
		}

		// 设置学生编号
		String report = appDao.getOfficailAppStudentReport(xnxq, autoIncr, params);// 获取校内考试的学生成绩报告
		if (report == null) {// 如果当前考试没有分析报告数据，只从原始成绩表里取
			List<JSONObject> listSubjectScore = appDao.selectSchoolExamSubjectScoreList(xnxq, autoIncr, params);

			JSONObject data = new JSONObject();
			List<JSONObject> scoreList = new ArrayList<JSONObject>();
			if (CollectionUtils.isNotEmpty(listSubjectScore)) {
				for (JSONObject subject : listSubjectScore) {
					JSONObject subjectScore = new JSONObject();

					Long subjectId = subject.getLong("subjectId");
					LessonInfo lessonInfo = lessonInfoMap.get(subjectId);
					if (lessonInfo != null) {
						subjectScore.put("subjectName", lessonInfo.getName());
					} else {
						subjectScore.put("subjectName", "");
					}

					subjectScore.put("score", StringUtil.formatNumber(subject.get("score"), 1));
					scoreList.add(subjectScore);
				}

				data.put("scoreList", scoreList);
			}

			JSONObject result = new JSONObject();
			result.put("code", 2);
			result.put("msg", "成功");
			result.put("data", data);
			return result;
		}

		JSONObject data = (JSONObject) JSONObject.parse(report);

		String isShow = degreeInfo.getFbpmflag();// 是否显示排名

		if (data != null)
			data.put("isShow", StringUtil.transformString(isShow));// 设置是否显示排名

		// 计算学生当前考试前6次考试的总分排名趋势
		if ("1".equals(isShow)) {
			List<JSONObject> sixScoreList = new ArrayList<JSONObject>();
			List<JSONObject> examList = new ArrayList<JSONObject>();

			String nj = params.getString("nj");
			List<String> termInfoIds = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(allCommonDataService, nj, xnxq);
			int limit = 6;
			params.put("cdate", degreeInfo.getCdate());
			for (String termInfoId : termInfoIds) {
				params.put("xnxq", termInfoId);
				params.put("examType", 1);	// 校考
				// 获取当前考试和之前的五次考试的id
				List<JSONObject> examTmp = appDao.getFrontFiveSchoolExamList(termInfoId, params);
				logger.info("examTmp====>" + examTmp);
				for (JSONObject exam : examTmp) {
					params.put("kslc", exam.getString("kslcdm"));
					Integer autoIncr2 = exam.getInteger("autoIncr");
					exam.put("examId",  exam.getString("kslcdm"));
					if(!appDao.ifExistsStuInExamScore(termInfoId, autoIncr2, params)) {
						continue;
					}
					params.put("kslcmc", exam.getString("kslcmc"));
					sixScoreList.addAll(appDao.getOfficialExamTotalScoreInfoList(termInfoId, autoIncr2, params));
					examList.add(exam);
					limit--;
					if (limit <= 0) {
						break;
					}
				}
				if (limit <= 0) {
					break;
				}
			}

			Map<String, JSONObject> sixScoreMap = new HashMap<String, JSONObject>();
			for (JSONObject json : sixScoreList) {
				String examId = json.getString("examId");
				sixScoreMap.put(examId, json);
			}

			List<String> xAxis = new ArrayList<String>(); // x轴标识
			List<JSONObject> Series = new ArrayList<JSONObject>();// 曲线数据点
			if (CollectionUtils.isNotEmpty(examList)) {
				int i = 1;
				Collections.reverse(examList);
				JSONObject line = new JSONObject();// 曲线
				line.put("name", "考试排名趋势");
				logger.info("examList====>" + examList);
				List<JSONObject> dataList = new ArrayList<JSONObject>();
				for (JSONObject exam : examList) {
					String examId = exam.getString("examId");
					if (StringUtil.isEmpty(examId)) {
						continue;
					}

					JSONObject score = sixScoreMap.get(examId);
					logger.info("score====>" + score);
					if (score != null && score.containsKey("classRank")) {
						if (examId.equals(params.get("kslc"))) {
							xAxis.add("本次");
						} else {
							xAxis.add("T" + i);
						}

						JSONObject point = new JSONObject();
						if (examId.equals(params.get("kslc"))) {
							point.put("name", "本次" + ":" + score.get("examName"));
						} else {
							point.put("name", "T" + i + ":" + score.get("examName"));
						}
						point.put("y", score.get("classRank"));
						dataList.add(point);
						i++;
					}
				}

				line.put("data", dataList);
				Series.add(line);
			}

			JSONObject rankTendency = new JSONObject();// 考试排名趋势

			rankTendency.put("xAxis", xAxis.toArray());// 设置x轴标识
			rankTendency.put("Series", Series);// 设置Series

			if (data != null) {
				data.put("rankTendency", rankTendency);// 设置考试排名趋势
			}
		}

		if ("0".equals(isShow) || StringUtil.isEmpty(isShow)) {
			if (data != null)
				data.put("rankTendency", "");// 设置考试排名趋势
		}

		// 组织返回数据结果
		JSONObject result = new JSONObject();
		result.put("code", 0);
		result.put("msg", "成功!");
		result.put("data", data == null ? "" : data);
		return result;
	}

	@Override
	public void insertExamViewByExamType(int examType, JSONObject params) {
		Integer autoIncr = params.getInteger("autoIncr");
		String termInfoId = null;
		StudentScoreReportTrace trace = new StudentScoreReportTrace();
		if(examType == 1) {
			termInfoId = params.getString("xnxq");
			trace.setAccountId(params.getString("accountId"));
			trace.setClassId(params.getString("bh"));
			trace.setExamId(params.getString("kslcdm"));
			trace.setSchoolId(params.getString("xxdm"));
			trace.setStudentId(params.getString("xh"));
			trace.setTermInfoId(termInfoId);
			trace.setViewFlag(1);
		} else {
			termInfoId = params.getString("termInfoId");
			trace.setAccountId(params.getString("accountId"));
			trace.setClassId(params.getString("classId"));
			trace.setExamId(params.getString("examId"));
			trace.setSchoolId(params.getString("schoolId"));
			trace.setStudentId(params.getString("studentId"));
			trace.setTermInfoId(termInfoId);
			trace.setViewFlag(1);
		}
		appDao.insertExamView(termInfoId, autoIncr, trace);
	}
	
	@Override
	public JSONObject getClassExamStudentScoreReport(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo examInfo = classScoreDao.getClassExamInfoById(termInfoId, params);
		if (examInfo == null) {
			logger.error("getClassExamStudentScoreReport:" +ERR_MSG_3 + params.toJSONString());
			return null;
		}
		Integer autoIncr = examInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("schoolId");

		List<JSONObject> everySubjectScore = appDao.selectClassExamEverSubjectScore(termInfoId, autoIncr, params);	// 每科成绩列表
		JSONObject totalScore = appDao.selectClassExamTotalScore(termInfoId, autoIncr, params);// 学生总分
		JSONObject classtotatScore = appDao.selectClassExamClassTotalScore(termInfoId, autoIncr, params);// 班级学生总分统计分析结果

		if (CollectionUtils.isNotEmpty(everySubjectScore)) {
			// 获取科目列表，以及科目信息
			List<Long> subjectList = appDao.selectClassExamSubjectList(termInfoId, autoIncr, params);
			// 存放科目信息Map
			Map<String, LessonInfo> subjectMap = new HashMap<String, LessonInfo>();
			if (subjectList != null && subjectList.size() > 0) {
				List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(schoolId, subjectList,
						termInfoId);
				for (LessonInfo lessonInfo : lessonInfos) {
					subjectMap.put(String.valueOf(lessonInfo.getId()), lessonInfo);
				}
			}

			for (JSONObject every : everySubjectScore) {// 循环把设置科目名称
				String subjectId = every.getString("subjectId");// 科目编号
				LessonInfo lessonInfo = subjectMap.get(subjectId);
				if (lessonInfo != null) {
					every.put("subjectName", lessonInfo.getName());
				} else {
					every.put("subjectName", "");
				}

			}
		}

		// 3.组织数据格式，返回给前段
		Map<String, Object> data = new HashMap<String, Object>();// 成绩结果数据
		data.put("totalScore", totalScore == null ? "" : StringUtil.formatNumber(totalScore.get("totalScore"), 2));// 设置总分
		data.put("achievement", totalScore == null ? "" : totalScore.get("surpassRatio"));// 设置击败率
		data.put("classAverageScore", classtotatScore == null ? ""
				: StringUtil.formatNumber(classtotatScore.get("classTotalScoreAverScore"), 2));
		data.put("classMaxScore", classtotatScore == null ? ""
				: StringUtil.formatNumber(classtotatScore.get("classTopTotalScore"), 2));// 设置班级最高总分
		data.put("scoreDataType", "1");
		data.put("classExamDetails", everySubjectScore);// 设置科目成绩列表

		// 生成前三榜单数据列表
		List<Map<String, Object>> topThree = new ArrayList<Map<String, Object>>();

		Map<String, Object> topOneScore = new HashMap<String, Object>();
		topOneScore.put("totalScore",
				classtotatScore == null ? "" : StringUtil.formatNumber(classtotatScore.get("topOneScore"), 2));
		topOneScore.put("rank", 1);
		topThree.add(topOneScore);

		Map<String, Object> topTwoScore = new HashMap<String, Object>();
		topTwoScore.put("totalScore",
				classtotatScore == null ? "" : StringUtil.formatNumber(classtotatScore.get("topTwoScore"), 2));
		topTwoScore.put("rank", 2);
		topThree.add(topTwoScore);

		Map<String, Object> topThreeScore = new HashMap<String, Object>();
		topThreeScore.put("totalScore",
				classtotatScore == null ? "" : StringUtil.formatNumber(classtotatScore.get("topThreeScore"), 2));
		topThreeScore.put("rank", 3);
		topThree.add(topThreeScore);
		data.put("topThree", topThree);// 设置前三榜单

		// 返回结果
		JSONObject result = new JSONObject();
		result.put("msg", "成功");
		result.put("code", 0);
		result.put("data", data);
		return result;
	}

	@Override
	public JSONObject getCustomExamStudentScoreReport(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo examInfo = classScoreDao.getClassExamInfoById(termInfoId, params);
		if (examInfo == null) {
			logger.error("getCustomExamStudentScoreReport:" +ERR_MSG_3 + params.toJSONString());
			return null;
		}
		Integer autoIncr = examInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		List<JSONObject> scorelist = appDao.selectCustomScoreByStudentId(termInfoId, autoIncr, params);
		for (Iterator<JSONObject> it = scorelist.iterator(); it.hasNext();) {
			JSONObject json = (JSONObject) it.next();
			if (json.get("score").equals("xm") || json.get("score").equals("userXh")
					|| json.get("score").equals("bjmc")) {
				it.remove();
			}
		}

		JSONObject result = new JSONObject();
		result.put("code", 0);
		result.put("msg", "");
		result.put("data", scorelist);
		return result;
	}

	@Override
	public JSONObject getClassReportExamList(JSONObject params) {
		Long schoolId = params.getLong("xxdm");
		String xnxq = params.getString("xnxq");
		String xn = xnxq.substring(0, xnxq.length() - 1);

		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		if (CollectionUtils.isEmpty(classroomList)) {
			return new JSONObject();
		}

		Map<Long, Classroom> classmap = new HashMap<Long, Classroom>();// 班级map集合
		List<Long> gradeIds = new ArrayList<Long>();
		for (Classroom classroom : classroomList) {
			long gradeId = classroom.getGradeId();
			if (!gradeIds.contains(gradeId)) {
				gradeIds.add(gradeId);
			}
			classmap.put(classroom.getId(), classroom);
		}

		int gradeLevelCode = 6;
		String maxNj = null;
		List<Grade> gradeList = allCommonDataService.getGradeBatch(schoolId, gradeIds, xnxq);
		if (gradeList == null) {
			throw new CommonRunException(-1, "无法从基础数据获取年级信息，请联系管理员！");
		}
		for (Grade grade : gradeList) {
			if (grade.getCurrentLevel().getValue() > gradeLevelCode) {
				gradeLevelCode = grade.getCurrentLevel().getValue();
				maxNj = allCommonDataService.ConvertNJDM2SYNJ(String.valueOf(gradeLevelCode), xn);
			}
		}

		List<String> termInfoIds = new ArrayList<String>();
		if (maxNj != null) {
			termInfoIds = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(allCommonDataService, maxNj, xnxq);
		}

		List<JSONObject> examList = new ArrayList<JSONObject>();
		for (String termInfoId : termInfoIds) {
			params.put("xnxq", termInfoId);
			List<JSONObject> examTmpList = appDao.selectTeacherExamSoreList(termInfoId, params);
			for (JSONObject exam : examTmpList) {
				Integer autoIncr = exam.getInteger("autoIncr");
				Integer examType = exam.getInteger("examType");
				String kslc = exam.getString("examId");
				params.put("kslc", kslc);
				List<Long> classIdList = null;
				List<JSONObject> ckrsList = null;
				if (examType == 1) { // 校考
					ckrsList = appDao.selectSchoolExamCount(termInfoId, autoIncr, params);
					classIdList = appDao.getBhListFromStatistics(termInfoId, autoIncr, params);
				} else if (examType == 2) { // 小考
					ckrsList = appDao.selectClassExamCount(termInfoId, autoIncr, params);
					classIdList = appDao.getClassIdFromClassExamRelative(termInfoId, autoIncr, params);
				} else { // 自定义考试
					ckrsList = appDao.selectCustomExamCount(termInfoId, autoIncr, params);
					classIdList = appDao.getClassIdFromCustomScoreInfo(termInfoId, autoIncr, params);
				}
				if (CollectionUtils.isEmpty(classIdList)) {
					continue;
				}

				Map<Long, Integer> classId2Ckrs = new HashMap<Long, Integer>();
				for (JSONObject json : ckrsList) {
					Long classId = json.getLong("classId");
					if (classId == null) {
						continue;
					}
					int ckrs = json.getIntValue("count");
					classId2Ckrs.put(classId, ckrs);
				}

				Map<Long, JSONObject> viewMap = new HashMap<Long, JSONObject>();
				List<JSONObject> viewlist = appDao.selectParentViewList(termInfoId, autoIncr, params);// 各老师家长查看情况列表
				for (JSONObject json : viewlist) {
					Long classId = json.getLong("classId");
					viewMap.put(classId, json);
				}

				List<JSONObject> classViewSituation = new ArrayList<JSONObject>();
				for (Long classId : classIdList) {
					int ckrs = classId2Ckrs.get(classId) == null ? 0 : classId2Ckrs.get(classId);

					JSONObject json = viewMap.get(classId);
					if (json == null) {
						json = new JSONObject();
						json.put("classId", classId);
						json.put("parentNoViewNum", 0);
						json.put("parentViewedNum", 0);
					}

					int parentViewedNum = json.getIntValue("parentViewedNum");
					int parentNoViewNum = ckrs - parentViewedNum;

					json.put("parentNoViewNum", parentNoViewNum < 0 ? 0 : parentNoViewNum);
					json.put("className", "");
					if (classmap.containsKey(classId)) {
						json.put("className", classmap.get(classId).getClassName());
					}
					classViewSituation.add(json);
				}

				Collections.sort(classViewSituation, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject arg0, JSONObject arg1) {
						String className0 = arg0.getString("className");
						String className1 = arg1.getString("className");
						return className0.compareTo(className1);
					}
				});

				exam.put("classViewSituation", classViewSituation);
				examList.add(exam);
			}
		}

		Collections.sort(examList, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				Date date1 = o1.getDate("time");
				Date date2 = o2.getDate("time");
				return - date1.compareTo(date2);
			}
		});
		
		JSONObject data = new JSONObject();
		data.put("code", 0);
		data.put("msg", "");
		data.put("data", examList);
		return data;
	}

	@Override
	public JSONObject getSchoolExamClassScoreReport(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		logger.info("degreeInfo："+JSONObject.toJSONString(degreeInfo));
		if (degreeInfo == null) {
			logger.error("getSchoolExamClassScoreReport:" + ERR_MSG_3 + params.toJSONString());
			return null;
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String reportData = appDao.selectClassReportViewList(xnxq, autoIncr, params);
		logger.info("reportData: "+reportData);
		if (reportData == null) {
			return new JSONObject();
		}
		return JSONObject.parseObject(reportData);
	}

	@Override
	public List<JSONObject> getViewScoreParentList(JSONObject params) {
		Integer examType = params.getInteger("examType");
		String termInfoId = params.getString("termInfoId");
		Integer autoIncr = null;
		if (examType == 1) {
			DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfoId, params);
			if (degreeInfo == null) {
				logger.error("getViewScoreParentList:" + ERR_MSG_3 + params.toJSONString());
				return null;
			}
			autoIncr = degreeInfo.getAutoIncr();
		} else {
			ClassExamInfo examInfo = classScoreDao.getClassExamInfoById(termInfoId, params);
			if (examInfo == null) {
				logger.error("getViewScoreParentList2:" +ERR_MSG_3 + params.toJSONString() );
				return null;
			}
			autoIncr = examInfo.getAutoIncr();
		}

		Long schoolId = params.getLong("schoolId");

		List<Long> stulist = new ArrayList<Long>();
		if (examType == 1) {
			stulist = appDao.selectSchoolExamUser(termInfoId, autoIncr, params);
		} else if (examType == 2) {
			stulist = appDao.selectClassExamUser(termInfoId, autoIncr, params);
		} else {
			stulist = appDao.selectCustomExamUser(termInfoId, autoIncr, params);
		}

		List<Account> ulist = allCommonDataService.getAccountBatch(schoolId, stulist, termInfoId);
		Map<Long, Account> umap = new HashMap<Long, Account>();
		for (Account a : ulist) {
			umap.put(a.getId(), a);
		}

		Set<Long> studIdSet = appDao.selectParentView(termInfoId, autoIncr, params);// 各老师家长查看情况列表

		List<JSONObject> datalist = new ArrayList<JSONObject>();
		for (Long stuId : stulist) {
			JSONObject data = new JSONObject();
			int state = 0;
			String name = "";
			if (umap.containsKey(stuId)) {
				name = umap.get(stuId).getName();
			}

			if (studIdSet.contains(stuId)) {
				state = 1;
			}

			data.put("studentId", stuId);
			data.put("studentName", name);
			data.put("isView", state);
			datalist.add(data);
		}

		return datalist;
	}

	@Override
	public List<JSONObject> getClassExamClassScoreReport(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo examInfo = classScoreDao.getClassExamInfoById(termInfoId, params);
		if (examInfo == null) {
			logger.error("getClassExamClassScoreReport:" + ERR_MSG_3 + params.toJSONString());
			return null;
		}
		Integer autoIncr = examInfo.getAutoIncr();

		Long schoolId = params.getLong("schoolId");

		List<JSONObject> datalist = new ArrayList<JSONObject>();

		List<Long> subjIds = appDao.selectClassExamSubjectList(termInfoId, autoIncr, params);

		Map<Long, LessonInfo> lemap = new HashMap<Long, LessonInfo>();
		List<LessonInfo> lessonInfoList = allCommonDataService.getLessonInfoBatch(schoolId, subjIds, termInfoId);
		if (lessonInfoList != null) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				lemap.put(lessonInfo.getId(), lessonInfo);
			}
		}

		params.put("subjectIds", subjIds);

		List<JSONObject> scorelist = appDao.selectClassExamSubjectScoreList(termInfoId, autoIncr, params);// 科目分数
		Map<Long, JSONObject> subjId2ScoreMap = new HashMap<Long, JSONObject>();
		for(JSONObject json : scorelist) {
			Long subjectId = json.getLong("SubjectId");
			double score = json.getDoubleValue("Score");
			if(!subjId2ScoreMap.containsKey(subjectId)) {
				JSONObject obj = new JSONObject();
				obj.put("count", 0);
				obj.put("score", 0d);
				subjId2ScoreMap.put(subjectId, obj);
			}
			JSONObject obj = subjId2ScoreMap.get(subjectId);
			obj.put("count", obj.getIntValue("count") + 1);
			obj.put("score", obj.getDoubleValue("score") + score);
		}
		
		for(Map.Entry<Long, JSONObject> entry : subjId2ScoreMap.entrySet()) {
			Long subjectId = entry.getKey();
			JSONObject obj = entry.getValue();
			int count = obj.getIntValue("count");
			double score = obj.getDoubleValue("score");
			
			double avascore = Math.rint(score * 100 / count) / 100;
			
			JSONObject data = new JSONObject();
			data.put("subjecteId", subjectId);
			data.put("subjectName", "");
			if (lemap.containsKey(subjectId)) {
				data.put("subjectName", lemap.get(subjectId).getName());
			}
			data.put("averageScore", avascore);
			datalist.add(data);
		}
		return datalist;
	}

	@Override
	public JSONObject getGradeExamScore(JSONObject params) {
		Long schoolId = params.getLong("schoolId");
		Long classId =  params.getLong("classId");
		String termInfoId = allCommonDataService.getCurTermInfoId(schoolId);
		List<String> termInfoIds = TermInfoIdUtils.getAllTermInfoIds(termInfoId);
		JSONObject exa = null;
		for (int i = 0; i < termInfoIds.size(); i++) { //找最近一次考试
			termInfoId = termInfoIds.get(i);
			List<JSONObject> examTmp = appDao.getSchoolExamList(termInfoId, null, params);
			for (JSONObject exam : examTmp) {
				params.put("kslc", exam.getString("kslc"));
				Integer autoIncr2 = exam.getInteger("autoIncr");
				params.put("examType", exam.getInteger("examType"));
				if(appDao.ifExistsClassOrStuInExamScore(termInfoId, autoIncr2, params)) {
					exa = exam;
					break;
				}else {
					continue;
				}
			}
			if (exa ==null) {
				continue;
			}else {
				break;
			}
		}
 
		if (exa == null) {
			logger.error("getGradeExamScore:" + ERR_MSG_3 + params.toJSONString());
			return null;
		}
		
		int examType = exa.getInteger("examType")  + 1;
		Integer autoIncr = exa.getInteger("autoIncr");
		List<JSONObject> ckrsList = null;
		params.put("xnxq", termInfoId);
		params.put("xxdm", schoolId);
		List<Long> bhList = new ArrayList<Long>();
		bhList.add(classId);
		params.put("bhList", bhList);
		List<JSONObject> list = appDao.selectParentViewList(termInfoId, autoIncr, params);
		if (examType == 1) { // 校考
			ckrsList = appDao.selectSchoolExamCount(termInfoId, autoIncr, params);
		} else if (examType == 2) { // 小考
			ckrsList = appDao.selectClassExamCount(termInfoId, autoIncr, params);
		} else { // 自定义考试
			ckrsList = appDao.selectCustomExamCount(termInfoId, autoIncr, params);
		}
		
		
		if (exa.getInteger("examType") > 0) {
			JSONObject result = new JSONObject();
			result.put("examId", exa.getString("kslc"));
			result.put("examType", exa.getInteger("examType") + 1);
			result.put("termInfo", termInfoId);
			result.put("examName", exa.getString("examName"));
			result.put("kslc", exa.getString("kslc"));
			result.put("autoIncr", exa.getInteger("autoIncr"));
			
			if (list!=null && list.size() > 0) {
				JSONObject parent = list.get(0);
			    JSONObject ckrsObj = ckrsList.get(0);
			    int ckrs = ckrsObj.getIntValue("count");
				result.put("viewedParentCnt", parent.getInteger("parentViewedNum"));
				int noViewParentCnt = ckrs - parent.getInteger("parentViewedNum");
				result.put("noViewParentCnt", noViewParentCnt < 0 ? 0 : noViewParentCnt);
			}else {
				   result.put("viewedParentCnt" , 0);
				   JSONObject ckrsObj = ckrsList.get(0);
				   int ckrs = ckrsObj.getIntValue("count");

				   result.put("noViewParentCnt",  ckrs < 0 ? 0 : ckrs);
			}
			return result;
		}
		
		JSONObject result = appDao.getGradeExamScore(termInfoId, autoIncr, params);
 
		if (list!=null && list.size() > 0) {
			JSONObject parent = list.get(0);
		    JSONObject ckrsObj = ckrsList.get(0);
		    int ckrs = ckrsObj.getIntValue("count");
			result.put("viewedParentCnt", parent.getInteger("parentViewedNum"));
			int noViewParentCnt = ckrs - parent.getInteger("parentViewedNum");
			result.put("noViewParentCnt", noViewParentCnt < 0 ? 0 : noViewParentCnt);
		}else {
			   result.put("viewedParentCnt" , 0);
			  JSONObject ckrsObj = ckrsList.get(0);
			  int ckrs = ckrsObj.getIntValue("count");
			  result.put("noViewParentCnt",  ckrs<0?0:ckrs);
		}
		result.put("examId", exa.getString("kslc"));
		result.put("examName", exa.getString("examName"));
		result.put("termInfo", termInfoId);
		result.put("examType", exa.getInteger("examType") + 1 );
		
		return result;
	}

	@Override
	public JSONObject getStudentList(JSONObject params) {
	    String termInfoId = params.getString("termInfoId");
	    Long schoolId = params.getLong("schoolId");
	    Long classId =  params.getLong("classId");
	    String examId = params.getString("examId");
	  /*  Classroom classroom = allCommonDataService.getClassById(schoolId, classId, termInfoId);
	    List<Long> stuAccounts = classroom.getStudentAccountIds();
		List<Account> accountList =  allCommonDataService.getAccountBatch(schoolId, stuAccounts, termInfoId);*/
		
		/*
		JSONObject result = new JSONObject();
		List<JSONObject> studentList = new ArrayList<JSONObject>();
		 for (int i = 0; i < accountList.size(); i++) {
			 JSONObject object = new JSONObject();
			 Account account = accountList.get(i);
			 List<User> users =  account.getUsers();
			 if (users!=null && users.size() > 0) {
				 for (int j = 0; j < users.size(); j++) {
						User user = users.get(j);
						if (user.getUserPart().getRole() == T_Role.Student) {
							 object.put("studentId", user.getUserPart().getId());
							 break;
						}
				}
			}
			 object.put("studentName", account.getName());
			 studentList.add(object);
		 }
		 result.put("classId", classId);
		 result.put("className", classroom.getClassName());
		 result.put("studentList", studentList);
		 */
	    
		List<JSONObject> examTmp = appDao.getSchoolExamList(termInfoId, null, params);
		logger.info("examTmp:"+examTmp);
		JSONObject object = new JSONObject();
		if (examTmp!=null && examTmp.size() > 0 ) {
			JSONObject exam = examTmp.get(0);
			 Integer autoIncr = exam.getInteger("autoIncr");
			 Integer examType = exam.getInteger("examType");
			 params.put("examType", examType);
			 List<JSONObject> data = null;
			 data = appDao.getStudentList(termInfoId, autoIncr, params);
			 List<Long> accounts = new ArrayList<Long>();
			 for (int i = 0; i < data.size(); i++) {
				 JSONObject stu = data.get(i);
				 accounts.add(stu.getLong("studentId"));
			 }
			 List<Account> accountList =  allCommonDataService.getAccountBatch(schoolId, accounts, termInfoId);
			 Map<Long, String> map = new HashMap<Long, String>();
			 Map<Long, Long> userIdMap = new HashMap<Long, Long>();
			 for (int i = 0; i < accountList.size(); i++) {
				 Account account = accountList.get(i);
				 map.put(account.getId(), account.getName());
				 List<User> users =  account.getUsers();
				 for (int j = 0; j < users.size(); j++) {
						User user = users.get(j);
						if (user.getUserPart().getRole() == T_Role.Student) {
							 userIdMap.put(account.getId(), user.getUserPart().getId());
							 break;
						}
				 }
			 }
			 
			 Set<Long> studIdSet = appDao.selectParentView(termInfoId, autoIncr, params);// 各老师家长查看情况列表
			 logger.info("studIdSet:"+JSONObject.toJSONString(studIdSet));
			 logger.info("data:"+JSONObject.toJSONString(data));
			 for (int i = 0; i < data.size(); i++) {
				 JSONObject stu = data.get(i);
				 stu.put("studentName", map.get(stu.getLong("studentId")));
				 stu.put("studentId", userIdMap.get(stu.getLong("studentId")));
				 
				 if(studIdSet.contains(stu.getLong("studentId")))
					 stu.put("isView", 1);
				 else
					 stu.put("isView", 0);
			 }
			 object.put("studentList", data)  ;
			 
		}
		return object;
	}

	@Override
	public JSONObject getExamStudentScoreReport(JSONObject params) {
 
		Long schoolId = params.getLong("schoolId");
		String termInfoId = allCommonDataService.getCurTermInfoId(schoolId);
		List<String> termInfoIds = TermInfoIdUtils.getAllTermInfoIds(termInfoId);
		JSONObject exa = null;
		for (int i = 0; i < termInfoIds.size(); i++) { //找最近一次考试
			termInfoId = termInfoIds.get(i);
			List<JSONObject> examTmp = appDao.getSchoolExamList(termInfoId, null, params);
			for (JSONObject exam : examTmp) {
				params.put("kslc", exam.getString("kslc"));
				params.put("examType", exam.getInteger("examType"));
				Integer autoIncr2 = exam.getInteger("autoIncr");
				if(appDao.ifExistsClassOrStuInExamScore(termInfoId, autoIncr2, params)) {
					exa = exam;
					break;
				}else {
					continue;
				}
			}
			if (exa ==null) {
				continue;
			}else {
				break;
			}
		}
		if (exa == null) {
			logger.error("getExamStudentScoreReport:" + ERR_MSG_3 + params);
			return null;
		}
		Integer autoIncr = exa.getInteger("autoIncr");
		List<JSONObject> list = appDao.getExamStudentScoreReport(termInfoId, autoIncr, params);
		List<Long> subjects = new ArrayList<Long>();
		
		for (int i = 0; i < list.size(); i++) {
			 JSONObject object = list.get(i);
			 subjects.add(object.getLong("subjectId"));
			 params.put("nj", object.getString("nj"));
			 params.put("classId", object.getString("classId"));
		}
		List<LessonInfo> lessons =  allCommonDataService.getLessonInfoBatch(schoolId, subjects, termInfoId);
		Map<Long, String> lessonMap = new HashMap<Long, String>();
		for (int i = 0; i < lessons.size(); i++) {
			LessonInfo info = lessons.get(i);
			lessonMap.put(info.getId(), info.getName());
		}
		JSONObject data = new JSONObject();
		data.put("examId", exa.getString("kslc"));
		data.put("examName", exa.getString("examName"));
		data.put("examType", exa.getInteger("examType") + 1) ;
		data.put("termInfo", termInfoId);
		if ( exa.getInteger("examType") ==1 ) {
			for (int i = 0; i < list.size(); i++) {
				 JSONObject object = list.get(i);
				 object.put("subjectName", lessonMap.get(object.getLong("subjectId")));
			}
			data.put("subjects", list);
			return data;
		}else if ( exa.getInteger("examType") == 2) {
			data.put("subjects", list);
			return data;
		}
		List<JSONObject> fullScoreList = appDao.getFullScore(termInfoId, autoIncr, params);
		Map<Long , Integer> fullScoreMap = new HashMap<Long , Integer>();
		for (int i = 0; i < fullScoreList.size(); i++) {
			 JSONObject fullscore = fullScoreList.get(i);
			 fullScoreMap.put(fullscore.getLong("subjectId"), fullscore.getInteger("fullScore"));
		}
		Double totalScore = 0.0;
		int totalFullMark = 0;
		for (int i = 0; i < list.size(); i++) {
			 JSONObject object = list.get(i);
			 if ( object.getDouble("score") != null) {
				 totalScore = totalScore + object.getDoubleValue("score");
			 }
			 totalFullMark = totalFullMark + fullScoreMap.get(object.getLong("subjectId") );
			 object.put("subjectName", lessonMap.get(object.getLong("subjectId")));
			 object.put("fullScore", fullScoreMap.get(object.getLong("subjectId") ));
		}

	
		data.put("subjects", list);
		data.put("totalFullMark", totalFullMark);
		data.put("totalScore", totalScore);
		
 
		String report = appDao.getOfficailAppStudentReport(termInfoId, autoIncr, params);// 获取校内考试的学生成绩报告
		if (report!=null) {
			JSONObject result = (JSONObject) JSONObject.parse(report);
			data.put("achievement", result.getString("achievement"));
		}
		
		JSONObject classPbfobject = appDao.getClassAvergaeScore(termInfoId, autoIncr, params);
		data.put("classAverageScore", String.format("%.2f", classPbfobject.getDouble("pjf")) );
		JSONObject gradePbfobject = appDao.getGradeAvergaeScore(termInfoId, autoIncr, params);
		data.put("gradeAverageScore", String.format("%.2f", gradePbfobject.getDouble("pjf")));
  
		return data;
	}

	@Override
	public DegreeInfo getDegreeInfoById(String termInfo, Map<String, Object> map) {
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfo, map);
		return degreeInfo;
	}
}
