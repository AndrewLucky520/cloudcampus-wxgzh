package com.talkweb.archive.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.archive.dao.ArchiveClassScoreReportDao;
import com.talkweb.archive.service.ArchiveClassScoreReportService;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ScoreManageConfigService;

@Service
public class ArchiveClassScoreReportServiceImpl implements ArchiveClassScoreReportService{

	@Autowired
	private ArchiveClassScoreReportDao classScoreReportDao;
	@Autowired
	private AllCommonDataService commonDataService;
	@Autowired
	private ScoreManageDao scoreDao;
	@Autowired
	private ScoreManageConfigService configService;
	
	
	@Override
	public List<JSONObject> produceTeacherScoreOnInThreeReportData(School school, JSONObject params) {

		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");
		String xnxq = params.getString("xnxq");
        String teacherId = params.getString("teacherId");
        Account acc = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(teacherId), xnxq);
        if (acc==null) {
			return  null;
		}
        List<User> teachers = acc.getUsers();
        List<Course> courseList =null;
        Set<Long> classIdset = new HashSet<Long>();
        Set<Long> lessonIdset = new HashSet<Long>();
        List<JSONObject> list = null;
		Map<String, List<String>> teachClasssMap = new HashMap<String, List<String>>();// 老师教的班级数量
		Map<String, Account> classCourseMap = new HashMap<String, Account>();// 存放班级科目老师信息Map
        for (int i = 0; i < teachers.size(); i++) {
        	User user = teachers.get(i);
        	String lessonId = null;
        	String classId = null;
        	if (user.getUserPart().getRole() ==T_Role.Teacher) {
        		courseList = user.getTeacherPart().getCourseIds();
        		if (courseList!=null) {
        			Course course = null ;
        			for (int j = 0; j < courseList.size(); j++) {
        				course = courseList.get(j);
        				lessonId = String.valueOf(course.getLessonId());
        				classId = String.valueOf(course.getClassId());
        				classIdset.add(course.getClassId());
        				lessonIdset.add(course.getLessonId());
						if (!classCourseMap.containsKey(classId + lessonId)) {
							classCourseMap.put(classId + lessonId, acc);
						}
    				}
				}
        		break;
			}
		}
 
        if (classIdset.size()==0 || lessonIdset.size() ==0) {
			return null;
		}

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("xxdm", schoolId);
        map.put("xnxq", xnxq);
        map.put("fbteaflag", 1);
        List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(xnxq, map);
        if (degreeInfoList==null || degreeInfoList.size()==0) {
			return null;
		}else {
			DegreeInfo degreeInfo = null;
			Integer autoIncr = null;
			list = new ArrayList<JSONObject>();
			for (int i = 0; i < degreeInfoList.size(); i++) {
				degreeInfo = degreeInfoList.get(i);
				autoIncr = degreeInfo.getAutoIncr();
				params.put("autoIncr", autoIncr);
				Map<String, Object> queryMap = new HashMap<>();
				queryMap.put("bhList", classIdset);
				queryMap.put("kmdmList", lessonIdset);
				params.put("kmdmList", lessonIdset);
				queryMap.put("xnxq", xnxq);
				queryMap.put("kslc", degreeInfo.getKslcdm());
				params.put("kslc", degreeInfo.getKslcdm());
				queryMap.put("xxdm", schoolId);
				List<JSONObject> classScore = classScoreReportDao.getTeacherClassScoreAnalyze(xnxq, autoIncr, queryMap);
				if (classScore==null || classScore.size() ==0) {
					continue;
				}
				
				List<JSONObject> classGroupClassList = classScoreReportDao.selectViewScoreClassGroupClassList(xnxq,
						autoIncr, queryMap);// 班级分组下的班级列表
				Map<String, String> classGroupClassMap = new HashMap<String, String>();
				for (JSONObject obj : classGroupClassList) {
					String bh = obj.getString("bh");
					String bmfz = obj.getString("bmfz");
					classGroupClassMap.put(bh, bmfz);
				}
				classGroupClassList = null;
				
				 //科目 和班级应该分开 ，老师教语文和政治，但是班级可能不一样
				Iterator<JSONObject> iterator = classScore.iterator();
				boolean classAndLesson = false;
				while (iterator.hasNext()) {
					JSONObject jsonObject = (JSONObject) iterator.next();
					classAndLesson = false;
					Course course = null ;
					for (int j = 0; j < courseList.size(); j++) {
						 course = courseList.get(j);
						 if (course.getClassId()==jsonObject.getLongValue("bhs") && course.getLessonId() == jsonObject.getLongValue("mkdm")) {
							 classAndLesson = true;
							 break;
						}
					}
					if (!classAndLesson) {
						iterator.remove();
					}else {
						String classGroup = classGroupClassMap.get(course.getClassId()+"");	// 获取班级分组
						if (classGroup == null) {
							classGroup = "";
						}
						String key = classGroup + "," + teacherId + "," + course.getLessonId();
						if (!teachClasssMap.containsKey(key)) {
							teachClasssMap.put(key, new ArrayList<String>());
						}
						List<String> classList = teachClasssMap.get(key);
						if (!classList.contains(course.getClassId()+"")) {
							classList.add(course.getClassId()+"");
						}
						
					}
				}
 
				if (classScore.size() ==0) {
					continue;
				}
 
				// 1.获取数据
				EasyUIDatagridHead[][] head = null;
				EasyUIDatagridHead[][] frozenColumns = null;

				Map<String, Object> topMsg = new HashMap<String, Object>();
				JSONObject data = new JSONObject();

			

 
				List<JSONObject> excellentPassRate = classScoreReportDao.getExcellentPassRate(xxdm);// 优秀率合格率参数

				// 2.设置科目名称（从远程接口获取基础数据）
				List<LessonInfo> lessonInfos = commonDataService.getLessonInfoList(school, xnxq);// 制定学校的科目列表
				Map<String, LessonInfo> lessonInfoMap = new HashMap<String, LessonInfo>();// 存放科目信息Map
				if (lessonInfos != null) {
					for (LessonInfo lessonInfo : lessonInfos) {
						if (lessonInfo == null)
							continue;
						if (!lessonInfoMap.containsKey(lessonInfo.getId() + "")) {
							lessonInfoMap.put(lessonInfo.getId() + "", lessonInfo);
						}
					}
				}
				// 班级数据
				Map<Long, Classroom> classRoomMap = new HashMap<Long, Classroom>();// 存放班级信息Map
				List<Classroom> classrooms = commonDataService.getClassroomBatch(school.getId(),
						   new ArrayList<Long>(classIdset), xnxq);// 获取班级信息列表
				if (classrooms != null) {
					for (Classroom classroom : classrooms) {// 把科目信息放置到Map
						if (classroom == null) {
							continue;
						}
						classRoomMap.put(classroom.getId(), classroom);
					}
				}

				for (JSONObject cscore : classScore) {// 循环班级成绩数据，设置班级名称和任课老师名称
					long classId = cscore.getLongValue("bhs");
					String subjectId = cscore.getString("mkdm");

					Classroom classroom = classRoomMap.get(classId);
					if (classroom != null) {
						cscore.put("bjmc", classroom.getClassName());
					}
					
					Account teacherAcc = classCourseMap.get(classId + "" + subjectId);
					if (teacherAcc != null) {
						cscore.put("xm", teacherAcc.getName());
						cscore.put("teacherId", teacherAcc.getId());
					}
					cscore.put("zgf", cscore.getString("zgf"));
					cscore.put("zdf", cscore.getString("zdf"));
				}

				// 3.组装表头和数据模型
				head = assembleTeacherOneThreeRateTableHeader(school.getId() , params);

				frozenColumns = frozenColumns(school.getId(), "007", head.length);
				List<JSONObject> classDataTable = this.assemleTeacherOneThreeRateTableData(classScore, classRoomMap,  
						teachClasssMap, lessonInfoMap ,params);
				String[] arrsm = new String[] { "", "", "", "" };
				if (excellentPassRate != null && excellentPassRate.size() > 0) {
					arrsm[0] = excellentPassRate.get(0).getString("fs");// dsfxcs.Tables[0].Rows[0]["fs"].ToString();
					String dm = "";// 代码 01 优秀率 ， 02 合格率，03 低分率， 04 尖子生 05 潜能生
					String totalPeopleRate = "";// 总分/人数百分比
					for (JSONObject rate : excellentPassRate) {
						dm = rate.getString("dm");
						totalPeopleRate = rate.getString("fzbfb");
						if ("01".equals(dm)) {
							arrsm[1] = totalPeopleRate;
						} else if ("02".equals(dm)) {
							arrsm[2] = totalPeopleRate;
						} else if ("03".equals(dm)) {
							arrsm[3] = totalPeopleRate;
						}
					}

					topMsg.put("staticMethod", arrsm[0]);
					topMsg.put("excellentRatio", arrsm[1]);
					topMsg.put("passRatio", arrsm[2]);
					topMsg.put("lowScoreRatio", arrsm[3]);

				}

				Collections.sort(classDataTable, new Comparator<JSONObject>(){
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						String classGroupName1 = o1.getString("classGroupName");
						if(StringUtils.isBlank(classGroupName1)) {
							return 1;
						}
						String classGroupName2 = o2.getString("classGroupName");
						if(StringUtils.isBlank(classGroupName2)) {
							return -1;
						}
						
						int result = classGroupName1.compareTo(classGroupName2);
						if(result != 0) {
							return result;
						}
						
						String className1 = o1.getString("className");
						if(className1 == null) {
							return 1;
						}
						String className2 = o2.getString("className");
						if(className2 == null) {
							return -1;
						}
						
						return className1.compareTo(className2);
					}
				});
				
				// 4.设置返回数据
				data.put("total", classDataTable == null ? 0 : classDataTable.size());// 总记录数
				data.put("rows", classDataTable);// 数据行
				data.put("columns", head);// 表头格式
				data.put("topmsg", topMsg);//
				data.put("frozenColumns", frozenColumns);// 冻结表头
				data.put("ksmc", degreeInfo.getKslcmc());
				list.add(data);
			}
		}
		

		return list;
	}
	
	
	public EasyUIDatagridHead[][] assembleTeacherOneThreeRateTableHeader(long schoolId  
			, JSONObject params) { 
		
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");
		
		EasyUIDatagridHead head[][] = {};

		Map<String, Integer> fieldShowInfo = getFieldShowInfo(String.valueOf(schoolId));// 获取表报表字段显示情况
		
		if (classScoreReportDao.ifExistsTopGroupData(xnxq, autoIncr, params)) {
			int oneRowColumns = fieldShowInfo.get("eachSubColumns");
			int twoRowColumns = fieldShowInfo.get("twoRowColumns");// 表头第二行数据每个科目显示的列数
			if (fieldShowInfo.get("teacherName" )==null ||  fieldShowInfo.get("teacherName" )!=0) {
				twoRowColumns = twoRowColumns - 1 ;
			}
			if (fieldShowInfo.get("teacherName" )==null ||  fieldShowInfo.get("teacherName" )!=0) {
				oneRowColumns = oneRowColumns - 1 ;
			}
			
			int threeRowColumns = fieldShowInfo.get("threeRowColumns");// 表头第三行数据每个科目显示的列数

			head = new EasyUIDatagridHead[3][];
			head[0] = new EasyUIDatagridHead[2];
			head[0][0] = new EasyUIDatagridHead(null, "按范围统计", "center", 0, 1,
					oneRowColumns, false);
			head[0][1] = new EasyUIDatagridHead(null, "全员统计", "center", 0, 1,
					oneRowColumns, false);

			head[1] = new EasyUIDatagridHead[ twoRowColumns * 2];
			head[2] = new EasyUIDatagridHead[ threeRowColumns * 2];

				int scope = 0;// 索引移动幅度，默认为零
	 
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[1][scope] = new EasyUIDatagridHead(
							  "_staticNumByRange", "统计人数", "center", 65, 2, 1, false);
					scope++;
				}
 
				if (!fieldShowInfo.containsKey("zdf")) {
					head[1][scope] = new EasyUIDatagridHead("zdf", "最低分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[1][scope] = new EasyUIDatagridHead("zgf", "最高分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[1][scope] = new EasyUIDatagridHead(null, "平均", "center", 0, 1,
							fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[1][scope] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1,
							fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[1][scope] = new EasyUIDatagridHead(null, "合格", "center", 0, 1,
							fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[1][scope] = new EasyUIDatagridHead(null, "不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[1][scope] = new EasyUIDatagridHead(null, "低分", "center", 0, 1,
							fieldShowInfo.get("lowColumns"), false);
				}

				int nextScope = 0;// 第三行索引移动幅度，默认为零
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							  "_averageScoreByRange", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_averageScoreDifValueByRange", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_averageScoreRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_averageScoreMeanValueByRange", "均值", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_excellentNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_excellentRateByRange", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_excellentRateDifValueByRange", "率差", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_excellentRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_excellentRateMeanValueByRange", "均值", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_passNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_passRateByRange", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_passRateDifValueByRange", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_passRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_passRateMeanValueByRange", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_failNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreRateByRange", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreRateDifValueByRange", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[2][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreRateMeanValueByRange", "均值", "center", 50, 1, 1,
							false);

				}

				scope = 0;
	 
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(
							  "_staticNum", "统计人数", "center", 55, 2, 1, false);
					scope++;
				}
 
				if (!fieldShowInfo.containsKey("zdf")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(
							 "zdf", "最低分", "center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(
							"zgf", "最高分", "center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"平均分", "center", 0, 1, fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"优秀率", "center", 0, 1, fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"合格率", "center", 0, 1, fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[1][twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"低分率", "center", 0, 1, fieldShowInfo.get("lowColumns"), false);
					scope++;
				}

				nextScope = 0;
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_averageScore", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_averageScoreDifValue", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_averageScoreRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_averageScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_excellentRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_excellentRate", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_excellentRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_excellentRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_excellentRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_passRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_passRate", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_passRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_passRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_passRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_failNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_lowScoreNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_lowScoreRate", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_lowScoreDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_lowScoreRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[2][threeRowColumns  + nextScope] = new EasyUIDatagridHead(
							"_lowScoreMeanValue", "均值", "center", 50, 1, 1, false);
				}
				 
			 
		} else {
			int twoRowColumns = fieldShowInfo.get("twoRowColumns");// 表头第二行数据每个科目显示的列数
			if (fieldShowInfo.get("teacherName" )==null ||  fieldShowInfo.get("teacherName" )!=0) {
				twoRowColumns = twoRowColumns - 1 ;
			}
			int threeRowColumns = fieldShowInfo.get("threeRowColumns");// 表头第三行数据每个科目显示的列数

			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[twoRowColumns];
			head[1] = new EasyUIDatagridHead[threeRowColumns];
   
				int scope = 0;// 索引移动幅度，默认为零

				if (!fieldShowInfo.containsKey("staticNum")) {
					head[0][scope] = new EasyUIDatagridHead( "_staticNum",
							"统计人数", "center", 65, 2, 1, false);
					scope++;
				}
 
				if (!fieldShowInfo.containsKey("zdf")) {
					head[0][scope] = new EasyUIDatagridHead( "zdf", "最低分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[0][scope] = new EasyUIDatagridHead( "zgf", "最高分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[0][scope] = new EasyUIDatagridHead(null, "平均", "center", 0, 1,
							fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[0][scope] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1,
							fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[0][scope] = new EasyUIDatagridHead(null, "合格", "center", 0, 1,
							fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[0][scope] = new EasyUIDatagridHead(null, "不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[0][scope] = new EasyUIDatagridHead(null, "低分", "center", 0, 1,
							fieldShowInfo.get("lowColumns"), false);
				}

				int nextScope = 0;// 第三行索引移动幅度，默认为零
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_averageScore", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_averageScoreDifValue", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_averageScoreRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_averageScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_excellentRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_excellentRate", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_excellentRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							 "_excellentRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_excellentRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_passRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_passRate", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_passRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_passRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_passRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_failNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_lowScoreNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							 "_lowScoreRate", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_lowScoreDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_lowScoreRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[1][nextScope] = new EasyUIDatagridHead(
							  "_lowScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				
		}
 
		// 3.返回
		return head;
	}
	
	
	public EasyUIDatagridHead[][] frozenColumns(long schoolId, String type, int length) {

		EasyUIDatagridHead head[][] = {};
		  if (type.equals("007")) {
			head = new EasyUIDatagridHead[length][];
			head[0] = new EasyUIDatagridHead[3];
			head[0][0] = new EasyUIDatagridHead("subjectName", "学科", "center", 100, length, 1, false);
			head[0][1] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
			head[0][2] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, length, 1, false);
			for (int i = 1; i < length; i++) {
				head[i] = new EasyUIDatagridHead[0];
				/*head[i][0] = new EasyUIDatagridHead("subjectName", "学科", "center", 100, length, 1, false);
				head[i][1] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
				head[i][2] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, length, 1, false);*/
			}
		}

		return head;
	}
	
	
	private Map<String, Integer> getFieldShowInfo(String schoolId) {
		// 获取报表显示列数，判断需要显示多少列
		Map<String, Integer> map = new HashMap<String, Integer>();

		String fieldAuthConfig = (String) configService.getReportFieldAuths(schoolId, "002").get("config");// 获取报表字段显示权限
		JSONObject fieldAuths = JSONObject.parseObject(fieldAuthConfig);

		int twoRowColumns = 9;// 默认第二行7列数据
		int threeRowColumns = 20;// 默认第三行20列数据
		int eachSubColumns = 4;// 每科的列数，默认显示统计人数和任课老师两列
		int averageColumns = 4;// 平均分的列数
		int excellentColumns = 5;// 优秀率的列数
		int passColumns = 5;// 合格率列数
		int lowColumns = 5;// 低分率列数

		if (fieldAuths != null) {

			String staticNum = fieldAuths.get("tjrs").toString();
			String teacherName = fieldAuths.get("rkjs").toString();
			String zgf = fieldAuths.get("zgf").toString();
			String zdf = fieldAuths.get("zdf").toString();
			String ckrs = fieldAuths.get("ckrs").toString();
			String fzmc = fieldAuths.get("fzmc").toString();

			if ("0".equals(ckrs)) {
				map.put("ckrs", 0);
			}

			if ("0".equals(fzmc)) {
				map.put("fzmc", 0);
			}

			if ("0".equals(staticNum)) {
				twoRowColumns = twoRowColumns - 1;
				map.put("staticNum", 0);
				eachSubColumns = eachSubColumns - 1;
			}

			if ("0".equals(teacherName)) {
				twoRowColumns = twoRowColumns - 1;
				map.put("teacherName", 0);
				eachSubColumns = eachSubColumns - 1;
			}

			if ("0".equals(zgf)) {
				twoRowColumns = twoRowColumns - 1;
				map.put("zgf", 0);
				eachSubColumns = eachSubColumns - 1;
			}
			if ("0".equals(zdf)) {
				twoRowColumns = twoRowColumns - 1;
				map.put("zdf", 0);
				eachSubColumns = eachSubColumns - 1;
			}
			// 平均分
			JSONObject averageScore = (JSONObject) fieldAuths.get("pjf");
			if (averageScore != null) {
				String averageScoreValue = averageScore.get("pjfz").toString();
				String averageScoreDifValue = averageScore.get("pjffc").toString();
				String rankValue = averageScore.get("pm").toString();
				String averageMeanValue = averageScore.get("pjfjz").toString();

				if ("0".equals(averageScoreValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScore", 0);

					averageColumns = averageColumns - 1;
				}

				if ("0".equals(averageScoreDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScoreDifValue", 0);

					averageColumns = averageColumns - 1;
				}

				if ("0".equals(rankValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScoreRank", 0);

					averageColumns = averageColumns - 1;
				}

				if ("0".equals(averageMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScoreMeanValue", 0);

					averageColumns = averageColumns - 1;
				}

				if ("0".equals(averageScoreValue) && "0".equals(averageScoreDifValue) && "0".equals(rankValue)
						&& "0".equals(averageMeanValue)) {
					twoRowColumns = twoRowColumns - 1;
					map.put("average", 0);
				}

			}

			// 优秀率
			JSONObject excellent = (JSONObject) fieldAuths.get("yx");
			if (excellent != null) {
				String excellentNum = excellent.getString("yxrs");
				String excellentRate = excellent.getString("yxl");
				String excellentRateDifValue = excellent.getString("yxlc");
				String excellentRank = excellent.getString("yxlpm");
				String excellentMeanValue = excellent.getString("yxljz");

				if ("0".equals(excellentNum)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateNum", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRate", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRateDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateDifValue", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateRank", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateMeanValue", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentNum) && "0".equals(excellentRate) && "0".equals(excellentRateDifValue)
						&& "0".equals(excellentRank) && "0".equals(excellentMeanValue)) {
					twoRowColumns = twoRowColumns - 1;
					map.put("excellent", 0);
				}
			}

			// 合格率
			JSONObject pass = (JSONObject) fieldAuths.get("hg");
			if (pass != null) {
				String passNum = pass.getString("hgrs");
				String passRate = pass.getString("hgl");
				String passRateDifValue = pass.getString("hglc");
				String passRateRank = pass.getString("hglpm");
				String passRateMeanValue = pass.getString("hgljz");

				if ("0".equals(passNum)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateNum", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRate", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRateDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateDifValue", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRateRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateRank", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRateMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateMeanValue", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passNum) && "0".equals(passRate) && "0".equals(passRateDifValue)
						&& "0".equals(passRateRank) && "0".equals(passRateMeanValue)) {
					twoRowColumns = twoRowColumns - 1;
					map.put("pass", 0);
				}
			}

			// 不合格
			String failNum = fieldAuths.get("bhgrs").toString();

			if ("0".equals(failNum)) {
				twoRowColumns = twoRowColumns - 1;
				threeRowColumns = threeRowColumns - 1;
				map.put("failNum", 0);
			}

			// 低分率
			JSONObject low = (JSONObject) fieldAuths.get("df");
			if (low != null) {
				String lowNum = low.getString("dfrs");
				String lowRate = low.getString("dfl");
				String lowRateDifValue = low.getString("dflc");
				String lowRateRank = low.getString("dflpm");
				String lowRateMeanValue = low.getString("dfljz");

				if ("0".equals(lowNum)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("lowScoreNum", 0);
					lowColumns = lowColumns - 1;
				}

				if ("0".equals(lowRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("lowScoreRate", 0);
					lowColumns = lowColumns - 1;
				}

				if ("0".equals(lowRateDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("lowScoreDifValue", 0);
					lowColumns = lowColumns - 1;
				}

				if ("0".equals(lowRateRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("lowScoreRateRank", 0);
					lowColumns = lowColumns - 1;
				}

				if ("0".equals(lowRateMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("lowScoreMeanValue", 0);
					lowColumns = lowColumns - 1;
				}

				if ("0".equals(lowNum) && "0".equals(lowRate) && "0".equals(lowRateDifValue) && "0".equals(lowRateRank)
						&& "0".equals(lowRateMeanValue)) {
					twoRowColumns = twoRowColumns - 1;
					map.put("low", 0);
				}
			}

		}

		if (twoRowColumns < 0)
			twoRowColumns = 0;
		if (threeRowColumns < 0)
			threeRowColumns = 0;

		eachSubColumns = eachSubColumns + threeRowColumns;
		if (eachSubColumns < 0)
			eachSubColumns = 0;
		if (averageColumns < 0)
			averageColumns = 0;
		if (excellentColumns < 0)
			excellentColumns = 0;
		if (passColumns < 0)
			passColumns = 0;
		if (lowColumns < 0)
			lowColumns = 0;

		map.put("twoRowColumns", twoRowColumns);
		map.put("threeRowColumns", threeRowColumns);
		map.put("eachSubColumns", eachSubColumns);
		map.put("averageColumns", averageColumns);
		map.put("excellentColumns", excellentColumns);
		map.put("passColumns", passColumns);
		map.put("lowColumns", lowColumns);
		// 2.返回数据
		return map;
	}
	
	
	public List<JSONObject> assemleTeacherOneThreeRateTableData(List<JSONObject> classScore, Map<Long, Classroom> classroomMap,
			  Map<String, List<String>> teachClasssMap, Map<String, LessonInfo> lessonInfoMap ,
			JSONObject params) {
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");
		List<JSONObject> classDataTable = new ArrayList<JSONObject>();// 按照班级分的数据存储模型
		if (classScore == null || classScore.size() <= 0)
			return null;

		List<JSONObject> averageValueList = classScoreReportDao.getStudentScoreAverageValue(xnxq, autoIncr, params);// 班级科目均值
		Map<String, JSONObject> averageValueMap = new HashMap<String, JSONObject>();// 按照班级科目存放成绩Map

		for (JSONObject averageValue : averageValueList) {
			String subjectId = averageValue.get("subjectId").toString();
			String classId = averageValue.get("classId").toString();

			if (!averageValueMap.containsKey(subjectId + classId)) {
				averageValueMap.put(subjectId + classId, averageValue);
			}
		}

		// 计算教师所教科目的均值
		Map<String, Map<String, Object>> teachCourseAverValue = new HashMap<String, Map<String, Object>>();// 所教科目的均值

		if (teachClasssMap != null && teachClasssMap.size() > 0) {// 计算老师所教科目的各种率的均值
			Iterator<String> iterator = teachClasssMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();// 键值

				String lessonId = key.split(",")[2];// 科目编号
				List<String> classList = teachClasssMap.get(key);// 教师所教科目的班级列表

				float averageScore = 0;
				float excellentRate = 0;
				float passRate = 0;
				float lowScoreRate = 0;
				float averageScoreByRange = 0;
				float excellentRateByRange = 0;
				float passRateByRange = 0;
				float lowScoreRateByRang = 0;

				for (String classId : classList) {// 循环班级，导出各种率的和值
					JSONObject averageValue = averageValueMap.get(lessonId + classId);
					if (averageValue != null) {
						averageScore += averageValue.getFloatValue("averageScore");
						excellentRate += averageValue.getFloatValue("excellentRate");
						passRate += averageValue.getFloatValue("passRate");
						lowScoreRate += averageValue.getFloatValue("lowScoreRate");
						averageScoreByRange += averageValue.getFloatValue("averageScoreByRange");
						excellentRateByRange += averageValue.getFloatValue("excellentRateByRange");
						passRateByRange += averageValue.getFloatValue("passRateByRange");
						lowScoreRateByRang += averageValue.getFloatValue("lowScoreRateByRang");
					}
				}
				// 计算各种率的平均值
				Map<String, Object> value = new HashMap<String, Object>();
				value.put("averageScoreMeanValue", StringUtil.formatNumber(averageScore / classList.size(), 2));
				value.put("excellentRateMeanValue", StringUtil.formatNumber(excellentRate / classList.size(), 2));
				value.put("passRateMeanValue", StringUtil.formatNumber(passRate / classList.size(), 2));
				value.put("lowScoreMeanValue", StringUtil.formatNumber(lowScoreRate / classList.size(), 2));

				value.put("averageScoreMeanValueByRange",
						StringUtil.formatNumber(averageScoreByRange / classList.size(), 2));
				value.put("excellentRateMeanValueByRange",
						StringUtil.formatNumber(excellentRateByRange / classList.size(), 2));
				value.put("passRateMeanValueByRange", StringUtil.formatNumber(passRateByRange / classList.size(), 2));
				value.put("lowScoreRateMeanValueByRange",
						StringUtil.formatNumber(lowScoreRateByRang / classList.size(), 2));
				teachCourseAverValue.put(key, value);
			}
		}
		HashMap<String, List<JSONObject>> groupm = new HashMap<String, List<JSONObject>>();
		for (JSONObject score : classScore) {
			if (groupm.containsKey(score.getString("bfz"))) {
				List<JSONObject> jlist = groupm.get(score.getString("bfz"));
				jlist.add(score);
			} else {
				List<JSONObject> jlist = new ArrayList<JSONObject>();
				jlist.add(score);
				groupm.put(score.getString("bfz"), jlist);
			}
		}

		Iterator<Entry<String, List<JSONObject>>> it = groupm.entrySet().iterator();

		while (it.hasNext()) {
			Entry<String, List<JSONObject>> entry = it.next();
			List<JSONObject> scr = entry.getValue();

			if (scr != null && scr.size() > 0) {
				String groupCodeTemp = scr.get(0).getString("bfz");// 文理分组代码(零时变量)
				String groupCode = "";// 文理分组编号

				for (JSONObject score : scr) {
					groupCode = score.getString("bfz");
					JSONObject dataRow = null;
					// 监测是否已经添加了该分组班级的数据,添加了就跳过后面的处理逻辑，执行下一条记录的处理
					if (findDataRow(classDataTable, score.getString("bhs"), score.getString("bmfz"))) {
						continue;
					}

					if (classScoreReportDao.ifExistsTopGroupData(xnxq, autoIncr, params)) {// 全范围统计
						dataRow = createTeacherOneThreeRateDataRow(score, scr,   classroomMap,  
								teachCourseAverValue, lessonInfoMap, groupCodeTemp);
						
					} else {// 不包含全范围统计
						dataRow = createTeacherOneThreeRateDataRowForNoAll(score, scr,  classroomMap,  
								teachCourseAverValue, lessonInfoMap,  groupCodeTemp);
					}

					if (!groupCodeTemp.equals(groupCode))
						groupCodeTemp = groupCode;

					// 添加数据列
					classDataTable.add(dataRow);
				}
			 
			}

		}
		return classDataTable;
	}
	
	
	public JSONObject createTeacherOneThreeRateDataRow(JSONObject score, List<JSONObject> classScore,
			 Map<Long, Classroom> classroomMap,  
			Map<String, Map<String, Object>> teachCourseAverValue, Map<String, LessonInfo> lessonInfoMap , String tempGroupCode) {

		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();
		List<JSONObject> scoreList = null;// 成绩信息列表；可能是班级成绩也可能是全年级成绩分级结果
		boolean computerGradeData = false;
		String groupCode = score.getString("bfz");// 文理分组
		String compareClassGroup = "";// 比较的分组，如果是去班级成绩则是班班级分组，如果是年级数据则是文理分组
		if (!tempGroupCode.equals(groupCode) && score.getString("mkdm").equals("zf")) {// 统计全年级成绩
	 
			
		} else {// 统计班级成绩
			dataRow.put("bh", score.getString("bhs"));
			dataRow.put("bmfz", score.getString("bmfz"));
			dataRow.put("classGroupName", score.getString("fzmc"));
			dataRow.put("className", score.getString("bjmc"));
			compareClassGroup = score.getString("bmfz");
			scoreList = classScore;// 设置为班级成绩分级结果
		}

		// 2.循环班级成绩数据或是年级成绩数据，找到制定班级和科目的数据列，并设置到到dataRow中。
		String smkdm = "";// 科目名称
		String sbh = "";// 班级号
		String classGroup = "";// 班级分组
		String kmdm = null;
		for (JSONObject cscore : scoreList) {
			 
			classGroup = ConvertEmptyString(cscore.getString("bmfz"));
			sbh = ConvertEmptyString(cscore.getString("bhs"));
			kmdm = ConvertEmptyString(cscore.getString("mkdm"));
			if (classGroup.equals(compareClassGroup) && (sbh.equals(score.getString("bhs")) || computerGradeData)) {
				LessonInfo lessonInfo = lessonInfoMap.get(ConvertEmptyString(cscore.getString("mkdm")));
				 if ( lessonInfo!=null) {
					 dataRow.put("subjectName", lessonInfo.getName());
				}
				dataRow.put(smkdm + "zgf", cscore.get("zgf"));
				dataRow.put(smkdm + "zdf", cscore.get("zdf"));
				Map<String, Object> everyRateAverValue = teachCourseAverValue
						.get(classGroup + "," + cscore.getString("teacherId") + "," + kmdm);// 各个率的均值
				dataRow.put(smkdm + "_staticNum", cscore.get("tjrs"));
				dataRow.put(smkdm + "_averageScore", cscore.get("pjf"));
				dataRow.put(smkdm + "_averageScoreDifValue", cscore.get("pjffc"));
				dataRow.put(smkdm + "_averageScoreRank", cscore.get("pm"));
				if (everyRateAverValue != null)
					dataRow.put(smkdm + "_averageScoreMeanValue", everyRateAverValue.get("averageScoreMeanValue"));
				dataRow.put(smkdm + "_excellentRateNum", cscore.get("yxrs"));
				dataRow.put(smkdm + "_excellentRate", cscore.get("yxl"));
				dataRow.put(smkdm + "_excellentRateDifValue", cscore.get("yxlc"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_excellentRateMeanValue", everyRateAverValue.get("excellentRateMeanValue"));
				dataRow.put(smkdm + "_passRateNum", cscore.get("hgrs"));
				dataRow.put(smkdm + "_passRate", cscore.get("hgl"));
				dataRow.put(smkdm + "_passRateDifValue", cscore.get("hglc"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_passRateMeanValue", everyRateAverValue.get("passRateMeanValue"));
				dataRow.put(smkdm + "_failNum",
						dealNegativeNumber(cscore.getInteger("tjrs") - (cscore.getInteger("hgrs"))));
				dataRow.put(smkdm + "_lowScoreNum", cscore.get("dfrs"));
				dataRow.put(smkdm + "_lowScoreDifValue", cscore.get("dflc"));
				dataRow.put(smkdm + "_lowScoreRate", cscore.get("dfl"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_lowScoreMeanValue", everyRateAverValue.get("lowScoreMeanValue"));
				dataRow.put(smkdm + "_averageScoreRank", cscore.get("pm"));
				dataRow.put(smkdm + "_excellentRateRank", cscore.get("yxlpm"));
				dataRow.put(smkdm + "_passRateRank", cscore.get("hglpm"));
				dataRow.put(smkdm + "_lowScoreRateRank", cscore.get("dflpm"));
				dataRow.put(smkdm + "_staticNumByRange", cscore.get("tjrs1"));
				dataRow.put(smkdm + "_averageScoreByRange", cscore.get("pjf1"));
				dataRow.put(smkdm + "_averageScoreDifValueByRange", cscore.get("pjffc1"));
				dataRow.put(smkdm + "_averageScoreRankByRange", cscore.get("pm1"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_averageScoreMeanValueByRange",
							everyRateAverValue.get("averageScoreMeanValueByRange"));
				dataRow.put(smkdm + "_excellentNumByRange", cscore.get("yxrs1"));
				dataRow.put(smkdm + "_excellentRateByRange", cscore.get("yxl1"));
				dataRow.put(smkdm + "_excellentRateDifValueByRange", cscore.get("yxlc1"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_excellentRateMeanValueByRange",
							everyRateAverValue.get("excellentRateMeanValueByRange"));
				dataRow.put(smkdm + "_passNumByRange", cscore.get("hgrs1"));
				dataRow.put(smkdm + "_passRateByRange", cscore.get("hgl1"));
				dataRow.put(smkdm + "_passRateDifValueByRange", cscore.get("hglc1"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_passRateMeanValueByRange",
							everyRateAverValue.get("passRateMeanValueByRange"));

				int tjrs1 = cscore.get("tjrs1") == null ? 0 : cscore.getInteger("tjrs1");
				int hgrs1 = cscore.get("hgrs1") == null ? 0 : cscore.getInteger("hgrs1");
				dataRow.put(smkdm + "_failNumByRange", dealNegativeNumber(tjrs1 - hgrs1));
				dataRow.put(smkdm + "_lowScoreNumByRange", cscore.get("dfrs1"));
				dataRow.put(smkdm + "_lowScoreRateByRange", cscore.get("dfl1"));
				dataRow.put(smkdm + "_lowScoreRateDifValueByRange", cscore.get("dflc1"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_lowScoreRateMeanValueByRange",
							everyRateAverValue.get("lowScoreRateMeanValueByRange"));

				if (!computerGradeData) {
					dataRow.put(smkdm + "_excellentRateRankByRange", cscore.get("yxlpm1"));
					dataRow.put(smkdm + "_passRateRankByRange", cscore.get("hglpm1"));
					dataRow.put(smkdm + "_lowScoreRateRankByRange", cscore.get("dflpm1"));
				}
				dataRow.put("referenceNum", cscore.getString("ckrs"));

			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	}
	
	public JSONObject createTeacherOneThreeRateDataRowForNoAll(JSONObject score, List<JSONObject> classScore,
			Map<Long, Classroom> classroomMap,
			Map<String, Map<String, Object>> teachCourseAverValue,Map<String, LessonInfo> lessonInfoMap , String tempGroupCode) {
		
		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();
		List<JSONObject> scoreList = null;// 成绩信息列表；可能是班级成绩也可能是全年级成绩分级结果
		boolean computerGradeData = false;
 
		String compareClassGroup = "";// 比较的分组，如果是去班级成绩则是班班级分组，如果是年级数据则是文理分组
 
			dataRow.put("bh", score.getString("bhs"));
			dataRow.put("bmfz", score.getString("bmfz"));
			dataRow.put("classGroupName", score.getString("fzmc"));
			dataRow.put("className", score.getString("bjmc"));
			compareClassGroup = score.getString("bmfz");
			scoreList = classScore;// 设置为班级成绩分级结果
	 

		// 2.循环班级成绩数据或是年级成绩数据，找到制定班级和科目的数据列，并设置到到dataRow中。

		String smkdm = "";// 科目名称
		String bhs = "";// 使用年级
		String classGroup = "";// 班级分组
		String kmdm = null;

		for (JSONObject cscore : scoreList) {
 
			classGroup = ConvertEmptyString(cscore.getString("bmfz"));
			bhs = ConvertEmptyString(cscore.getString("bhs"));
			kmdm = ConvertEmptyString(cscore.getString("mkdm"));
			if (classGroup.equals(compareClassGroup) && (bhs.equals(score.getString("bhs")) || computerGradeData)) {
				LessonInfo lessonInfo = lessonInfoMap.get(ConvertEmptyString(cscore.getString("mkdm")));
				 if ( lessonInfo!=null) {
					 dataRow.put("subjectName", lessonInfo.getName());
				}
				dataRow.put(smkdm + "zgf", cscore.get("zgf"));
				dataRow.put(smkdm + "zdf", cscore.get("zdf"));
				Map<String, Object> everyRateAverValue = teachCourseAverValue
						.get(classGroup +  "," + cscore.getString("teacherId") + "," + kmdm);// 各个率的均值
				dataRow.put(smkdm + "_staticNum", cscore.get("tjrs"));
				dataRow.put(smkdm + "_averageScore", cscore.get("pjf"));
				dataRow.put(smkdm + "_averageScoreDifValue", cscore.get("pjffc"));

				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_averageScoreMeanValue", everyRateAverValue.get("averageScoreMeanValue"));
				dataRow.put(smkdm + "_excellentRateNum", cscore.get("yxrs"));
				dataRow.put(smkdm + "_excellentRate", cscore.get("yxl"));
				dataRow.put(smkdm + "_excellentRateDifValue", cscore.get("yxlc"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_excellentRateMeanValue", everyRateAverValue.get("excellentRateMeanValue"));
				dataRow.put(smkdm + "_passRateNum", cscore.get("hgrs"));
				dataRow.put(smkdm + "_passRate", cscore.get("hgl"));
				dataRow.put(smkdm + "_passRateDifValue", cscore.get("hglc"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_passRateMeanValue", everyRateAverValue.get("passRateMeanValue"));
				dataRow.put(smkdm + "_failNum",
						dealNegativeNumber(cscore.getInteger("tjrs") - (cscore.getInteger("hgrs"))));
				dataRow.put(smkdm + "_lowScoreNum", cscore.get("dfrs"));
				dataRow.put(smkdm + "_lowScoreDifValue", cscore.get("dflc"));
				dataRow.put(smkdm + "_lowScoreRate", cscore.get("dfl"));
				if (everyRateAverValue != null && !computerGradeData)
					dataRow.put(smkdm + "_lowScoreMeanValue", everyRateAverValue.get("lowScoreMeanValue"));

				if (!computerGradeData) {
					dataRow.put(smkdm + "_averageScoreRank", cscore.get("pm"));
					dataRow.put(smkdm + "_excellentRateRank", cscore.get("yxlpm"));
					dataRow.put(smkdm + "_passRateRank", cscore.get("hglpm"));
					dataRow.put(smkdm + "_lowScoreRateRank", cscore.get("dflpm"));
				}
				dataRow.put("referenceNum", cscore.get("ckrs"));

				if (computerGradeData)
					continue;

 
			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	
		
	}
	
	
	public String ConvertEmptyString(String args) {
		if (args == null)
			return "";

		return args;
	}
	
	public float dealNegativeNumber(float number) {
		if (number < 0) {
			number = 0;
		}
		return number;
	}
	
	public boolean findDataRow(List<JSONObject> list, String bh, String bmfz) {
		for (JSONObject json : list) {
			if (json.getString("bh").equals(bh) && json.getString("bmfz").equals(bmfz)) {
				return true;
			}
		}
		return false;
	}
	
	
 
	
}
