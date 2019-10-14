package com.talkweb.scoreManage.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.MathUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.dao.CsCurCommonDataDao;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.schedule.service.ScheduleLookUpService;
import com.talkweb.scoreManage.dao.ClassScoreCrudDao;
import com.talkweb.scoreManage.dao.ClassScoreReportDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.dao.ScoreReportDao;
import com.talkweb.scoreManage.dao.ViewClassScoreDao;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRankMk;
import com.talkweb.scoreManage.po.gm.StudentScoreReportTrace;
import com.talkweb.scoreManage.service.ScoreManageConfigService;
import com.talkweb.scoreManage.service.ScoreReportService;

/****
 * 学生成绩报表查看服务实现
 * 
 * @author guoyuanbing
 * @time 2015-06-01
 */
@Service
public class ScoreReportServiceImple implements ScoreReportService {

	@Autowired
	private ScoreReportDao reportDao;

	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private ClassScoreCrudDao classScoreDao;

	@Autowired
	private ViewClassScoreDao viewClassScoreDao;

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private CsCurCommonDataDao csCurCommonDataDao;
	
	@Autowired
	private ScoreManageConfigService configService;
	
	@Autowired
	private ClassScoreReportDao classScoreReportDao;
	
	@Value("#{settings['firstTermInfoId']}")
	 private String firstTermInfoId;// 
	
	private String first_db_termInfoId = "20151"; // 分库分表从20151开始

	//新高考成绩
	@Autowired
	ScheduleLookUpService scheduleLookUpService;
	
	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";
	Logger logger = LoggerFactory.getLogger(ScoreReportServiceImple.class);

	@Override
	public List<JSONObject> getScoreReportViewList(JSONObject params) throws Exception {
		if(params.getIntValue("isNew")==1){
			return getScoreReportViewList2(params);
		}
		Integer type = params.getInteger("type");
		String curTermInfoId = params.getString("curTermInfoId");
		params.remove("curTermInfoId");
		Long classId = params.getLong("classId");
		params.remove("classId");
		Long schoolId = params.getLong("schoolId");
		List<String> termInfoIdList = null;
		if (type == 0) { // 当前学年学期
			termInfoIdList = new ArrayList<String>();
			termInfoIdList.add(curTermInfoId);
		} else if (type == 1) { // 历年
			termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByClassId(commonDataService, schoolId, classId,
					curTermInfoId, first_db_termInfoId);
		}else if (type == 2) {
			termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByClassId(commonDataService, schoolId, classId,
					curTermInfoId, first_db_termInfoId);
			termInfoIdList.add(curTermInfoId);
		}else {
			throw new CommonRunException(-1, "参数type值异常，请联系管理员！");
		}

		List<JSONObject> data = new ArrayList<JSONObject>();

		params.put("fbflag", "1");
		for (String termInfoId : termInfoIdList) {
			params.put("termInfoId", termInfoId);
			params.put("xnxq", termInfoId); // 兼容 t_gm_degreeinfo表查询

			// 2.读取数据库，从中获取成绩数据，并且根据接口需求组织好数据格式，用以返回给前段解析。
			List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, params);
			for (DegreeInfo degreeInfo : degreeInfoList) {
				Integer autoIncr = degreeInfo.getAutoIncr();
				String examId = degreeInfo.getKslcdm();
				params.put("examId", examId);
				if (reportDao.ifExistsDegreeInfoByStudId(termInfoId, autoIncr, params)) {
					JSONObject json = new JSONObject();
					json.put("examId", examId);
					json.put("examName", degreeInfo.getKslcmc());
					json.put("examType", 1); // 校考
					json.put("createTime", degreeInfo.getCdate());
					json.put("termInfoId", termInfoId);
					json.put("autoIncr", degreeInfo.getAutoIncr());//
					data.add(json);
				}
			}

			params.put("isImport", "1");
			params.put("isPublic", "1");
			List<JSONObject> examList = classScoreDao.getExamList(termInfoId, params); // 班级小考

			for (JSONObject exam : examList) {
				Integer examType = exam.getInteger("examType");
				Integer autoIncr = exam.getInteger("autoIncr");
				String examId = exam.getString("examId");
				params.put("examId", examId);
				if (examType == 1) { // 1：系统格式，数值类型
					if (reportDao.ifExistsClassExamInfoByStudId(termInfoId, autoIncr, params)) {
						JSONObject json = new JSONObject();
						json.put("examId", examId);
						json.put("examName", exam.get("examName"));
						json.put("createTime", exam.getDate("CreateTime"));
						json.put("examType", 2); // 班级小考，系统格式（数据型）
						json.put("termInfoId", termInfoId);
						json.put("autoIncr", exam.getInteger("autoIncr"));//
						data.add(json);
					}
				} else { // 2：自定义格式，非数值类型
					if (reportDao.ifExistsCustomClassExamInfoByStudId(termInfoId, autoIncr, params)) {
						JSONObject json = new JSONObject();
						json.put("examId", examId);
						json.put("examName", exam.get("examName"));
						json.put("createTime", exam.getDate("CreateTime"));
						json.put("examType", 3); // 班级小考，自定义格式（字符型）
						json.put("termInfoId", termInfoId);
						json.put("autoIncr", exam.getInteger("autoIncr"));//
						data.add(json);
					}
				}
			}
		}

		Collections.sort(data, new Comparator<JSONObject>() {// 排序
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				long dt1 = o1.getDate("createTime").getTime();
				long dt2 = o2.getDate("createTime").getTime();
				return -Long.compare(dt1, dt2);
			}
		});

		return data;
		
	}
	
	public List<JSONObject> getScoreReportViewList2(JSONObject params) throws Exception{
		Integer type = params.getInteger("type");
		String curTermInfoId = params.getString("curTermInfoId");
		params.remove("curTermInfoId");
		Long classId = params.getLong("classId");
		params.remove("classId");
		Long schoolId = params.getLong("schoolId");
		String gradeId = params.getString("gradeId");
		List<String> termInfoIdList = null;
		if (type == 0) { // 当前学年学期
			termInfoIdList = new ArrayList<String>();
			termInfoIdList.add(curTermInfoId);
		} else if (type == 1) { // 历年
			termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByClassId(commonDataService, schoolId, classId,
					curTermInfoId, first_db_termInfoId);
		} else {
			throw new CommonRunException(-1, "参数type值异常，请联系管理员！");
		}

		List<JSONObject> data = new ArrayList<JSONObject>();

		params.put("fbflag", "1");
		for (String termInfoId : termInfoIdList) {
			params.put("termInfoId", termInfoId);
			params.put("xnxq", termInfoId); // 兼容 t_gm_degreeinfo表查询

			//+++++++++++++ added +++++++++++++
			School school = commonDataService.getSchoolById(schoolId, termInfoId);
			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoList(school, termInfoId);
			
			Map<String, LessonInfo> lessonInfoMap = StringUtil.convertToMap(lessonInfos, LessonInfo._Fields.ID);
			
			/**/JSONObject obj = new JSONObject();
			//obj.put("termInfoId", "1");
			obj.put("termInfo", termInfoId);
			//obj.put("schoolId", "44898");
			//obj.put("gradeId", "2008");
			obj.put("schoolYear", termInfoId.substring(0, 4));
			
			
			//获取课表Id
			JSONObject schedule = null;//scheduleService.getLatestSchedule(params);
			//获取分班Id
			params.put("isAll", 1);
			params.put("xnxq", termInfoId);
			params.put("scheduleId", schedule.get("scheduleId"));
			List<JSONObject> gradeList = new ArrayList<>();//scheduleService.getScheduleGradeList(params);
			List<String>gIdList = new ArrayList<String>();
			gIdList.add(params.getString("gradeId"));
			for(JSONObject grade : gradeList){
				/*if(!gIdList.contains(grade.getString("value"))){
					gIdList.add(grade.getString("value"));	
				}*/
				if(null!=grade.getString("value") && grade.getString("value").equals(gradeId)){
					if(null!=grade.get("placementId")){
						obj.put("placementId", grade.get("placementId"));
						break;
					}
				}
			}
			
			List<String> stuList = new ArrayList<String>();
			stuList.add(params.getString("studentId"));
			params.put("gIdList", gIdList);
			obj.putAll(params);
			params.put("stuIdList", stuList);
			obj.put("termInfoId", termInfoId.substring(4));

			List<JSONObject> stuTclassMap = scheduleLookUpService.getStuInTclass(obj);//scoreDao.getStuTclassMap(params);
			//Map<String,JSONObject> stuSubTclassMap = new HashMap<String,JSONObject>();
			JSONObject subTclassMap = new JSONObject();
			List<Long> tclsIds = new ArrayList<Long>();
			Map<String,JSONObject> stuIdSubTclassMap = new HashMap<String,JSONObject>();
			for(JSONObject stuTclass : stuTclassMap){
				String stuId = stuTclass.getString("studentId");
				if(null==stuId){
					continue;
				}
				
				JSONObject subTclass = stuIdSubTclassMap.get(stuId);
				if(null==subTclass){
					subTclass= new JSONObject();
					stuIdSubTclassMap.put(stuId, subTclass);
				}
				subTclass.put(stuTclass.getString("subjectId"), stuTclass);
				if(!stuId.equals(stuList.get(0))){
					continue;
				}
				try {
					Long clsId = stuTclass.getLong("tclassId");
					tclsIds.add(clsId);
				} catch (Exception e) {
					// TODO: handle exception
				}
				subTclassMap.put(stuTclass.getString("subjectId"), stuTclass);
			}
			
			List<Classroom> crList = commonDataService.getSimpleClassBatch(schoolId, tclsIds, termInfoId);
			subTclassMap.put("-999",crList.get(0).className);
			//+++++++++++++++++++  end +++++++++++++
			
			// 2.读取数据库，从中获取成绩数据，并且根据接口需求组织好数据格式，用以返回给前段解析。
			List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, params);
			for (DegreeInfo degreeInfo : degreeInfoList) {
				Integer autoIncr = degreeInfo.getAutoIncr();
				String examId = degreeInfo.getKslcdm();
				params.put("examId", examId);
				params.put("kslc", examId);
				
				//++++++++++++++++++++  added   +++++++++++++++++=
				List<JSONObject> scoresOf3Plus3 = null;
				
				//3+3总分
				List<Float> totalScoresInCls = new ArrayList<Float>();
				List<Float> totalScoresInGrade = new ArrayList<Float>();
				//3+3综合
				List<Float> plusScoresInCls = new ArrayList<Float>();
				List<Float> plusScoresInGrade = new ArrayList<Float>();
				Map<String,List<Float>>tclassSubScores = new HashMap<String,List<Float>>();
				String stuId = params.getString("studentId");
				JSONObject queryStu = stuIdSubTclassMap.get(stuId);
				List<String> queryTclassList = new ArrayList<String>();
				for(String subId : queryStu.keySet()){
					JSONObject queryTclass = queryStu.getJSONObject(subId);
					queryTclassList.add(queryTclass.getString("tclassId"));
				}
				
				try {
					scoresOf3Plus3 =scoreDao.get3Plus3Scores(termInfoId, autoIncr, params);
				
					if(CollectionUtils.isNotEmpty(scoresOf3Plus3)){
						String preXh = null;
						float gradeTotalScore=0,gradePlusScore=0;
						JSONObject stuSubScores = null;
						for(JSONObject scores : scoresOf3Plus3){
							String xh = scores.getString("xh");
							String kmdm = scores.getString("kmdm");
							float cj = scores.getFloatValue("cj");
							
							stuSubScores = stuIdSubTclassMap.get(xh);
							if(null!=preXh && !preXh.equals(xh)){
								totalScoresInGrade.add(gradeTotalScore);								
								plusScoresInGrade.add(gradePlusScore);
								String bh = scores.getString("bh");
								
								//班级3+3总成绩、综合成绩
								if(bh.equals(String.valueOf(tclsIds.get(0)))){
									totalScoresInCls.add(gradeTotalScore);
									plusScoresInCls.add(gradePlusScore);
								}
								
								gradeTotalScore = 0;								
								gradePlusScore = 0;	
							}
							preXh = xh;
							//3+3年级总成绩、年级3+3综合成绩
							gradeTotalScore+=cj;
							
							if(null!=stuSubScores){	
								JSONObject subScores = stuSubScores.getJSONObject(kmdm);
								if(null==subScores){
									continue;
								}
								//走班成绩汇总
								List<Float> tclsScores = tclassSubScores.get(subScores.getString("tclassId"));
								if(null==tclsScores){
									tclsScores = new ArrayList<Float>();
									tclassSubScores.put(subScores.getString("tclassId"), tclsScores);
								}
								tclsScores.add(cj);
								
								//年级成绩汇总
								StringBuffer subKey = new StringBuffer();
								subKey.append(kmdm);subKey.append("_");
								subKey.append(subScores.get("tclassLevel"));subKey.append("_grade");
								List<Float> gradeSubScores = tclassSubScores.get(subKey.toString());
								if(gradeSubScores==null){
									gradeSubScores = new ArrayList<Float>();
									tclassSubScores.put(subKey.toString(), gradeSubScores);
								}
								gradeSubScores.add(cj);
								
								if(subScores.getIntValue("tclassLevel")==1){
									gradePlusScore+=cj;
								}
							}
							
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//++++++++++++++++++++++  end +++++++++++++++++++++++
				
				if (reportDao.ifExistsDegreeInfoByStudId(termInfoId, autoIncr, params)) {
					JSONObject json = new JSONObject();
					json.put("examId", examId);
					json.put("examName", degreeInfo.getKslcmc());
					json.put("examType", 1); // 校考
					json.put("createTime", degreeInfo.getCdate());
					json.put("termInfoId", termInfoId);
					data.add(json);
				
					//+++++++++++++++  added +++++++++++++++++
					JSONArray lessoneScore = new JSONArray();
					List<ScoreStuStatisticsRankMk> subScores = scoreDao.getStuSubjectScoreRank(termInfoId, autoIncr, params);
					float totalScore=0,plusScore = 0;
					for(ScoreStuStatisticsRankMk score : subScores){
						JSONObject subScore = new JSONObject();
						subScore.put("score", score.getCj());

						subScore.put("rankInGrade", score.getNjpm());
						subScore.put("rankInClass", score.getBjpm());
						
						String kmdm = score.getKmdm();
						LessonInfo lessonInfo = lessonInfoMap.get(kmdm);
						subScore.put("lessoneId", score.getKmdm());
						subScore.put("lessoneName", lessonInfo.getName());
						if("16".equals(score.getKmdm())
								||"17".equals(score.getKmdm())){
							continue;
						}
						//+++++++++++++++++++  added   +++++++++++++++++++++++++++++++++++++++++
						totalScore += score.getCj();
						if(null!=subTclassMap.get(score.getKmdm())){//走班班级
							JSONObject tclass = subTclassMap.getJSONObject(score.getKmdm());
							int tclassType = tclass.getIntValue("tclassLevel");
							if(1==tclassType){
								plusScore += score.getCj();
							}
							subScore.put("classNamelist", tclass.get("tclassName"));
							subScore.put("rankInClass", 
									getPosition(tclassSubScores.get(tclass.getString("tclassId")), score.getCj()));
							
							StringBuffer subKey = new StringBuffer();
							subKey.append(score.getKmdm());subKey.append("_");
							subKey.append(tclassType);subKey.append("_grade");
							subScore.put("rankInGrade", 
									getPosition(tclassSubScores.get(subKey.toString()), score.getCj()));

						}else if(subTclassMap.getString("-999")!=null){//固定上课
							subScore.put("classNamelist", subTclassMap.get("-999"));
						}
						lessoneScore.add(subScore);
					}

					JSONObject plus3Score = new JSONObject();
					plus3Score.put("lessoneName", "3+3选科综合");
					plus3Score.put("score", plusScore);
					plus3Score.put("classNamelist", "--");
					plus3Score.put("rankInGrade", "--"/*getPosition(plusScoresInGrade, plusScore)*/);
					plus3Score.put("rankInClass", /*getPosition(plusScoresInCls, plusScore)*/"--");
					
					JSONObject totalPlus3Score = new JSONObject();
					totalPlus3Score.put("lessoneName", "总分");
					totalPlus3Score.put("score", totalScore);
					totalPlus3Score.put("classNamelist", "--");
					totalPlus3Score.put("rankInGrade", /*getPosition(totalScoresInGrade, totalScore)*/"--");
					totalPlus3Score.put("rankInClass", /*getPosition(totalScoresInCls, totalScore)*/"--");
					
					lessoneScore.add(plus3Score);
					lessoneScore.add(totalPlus3Score);					
					
					json.put("lessoneScore", lessoneScore);
					//data.add(json);
					
					//+++++++++++++++ end ++++++++++++++++++
				}
			}

			
			params.put("isImport", "1");
			params.put("isPublic", "1");
			List<JSONObject> examList = classScoreDao.getExamList(termInfoId, params); // 班级小考


			
			for (JSONObject exam : examList) {
				Integer examType = exam.getInteger("examType");
				Integer autoIncr = exam.getInteger("autoIncr");
				String examId = exam.getString("examId");
				params.put("examId", examId);
				if (examType == 1) { // 1：系统格式，数值类型
					if (reportDao.ifExistsClassExamInfoByStudId(termInfoId, autoIncr, params)) {
						JSONObject json = new JSONObject();
						json.put("examId", examId);
						json.put("examName", exam.get("examName"));
						json.put("createTime", exam.getDate("CreateTime"));
						json.put("examType", 2); // 班级小考，系统格式（数据型）
						json.put("termInfoId", termInfoId);
						
						JSONArray lessoneScore = new JSONArray();
						List<ScoreStuStatisticsRankMk> subScores = scoreDao.getStuSubjectScoreRank(termInfoId, autoIncr, params);
						for(ScoreStuStatisticsRankMk score : subScores){
							JSONObject subScore = new JSONObject();
							subScore.put("score", score.getCj());
							subScore.put("rankInGrade", score.getNjpm());
							subScore.put("rankInClass", score.getBjpm());
							String kmdm = score.getKmdm();
							LessonInfo lessonInfo = lessonInfoMap.get(kmdm);
							subScore.put("lessoneId", score.getKmdm());
							subScore.put("lessoneName", lessonInfo.getName());
							
							if("16".equals(score.getKmdm())
									||"17".equals(score.getKmdm())){
								continue;
							}
							lessoneScore.add(subScore);
						}
						json.put("lessoneScore", lessoneScore);
						data.add(json);
					}
				} else { // 2：自定义格式，非数值类型
					if (reportDao.ifExistsCustomClassExamInfoByStudId(termInfoId, autoIncr, params)) {
						JSONObject json = new JSONObject();
						json.put("examId", examId);
						json.put("examName", exam.get("examName"));
						json.put("createTime", exam.getDate("CreateTime"));
						json.put("examType", 3); // 班级小考，自定义格式（字符型）
						json.put("termInfoId", termInfoId);
						data.add(json);
					}
				}
			}
		}

		Collections.sort(data, new Comparator<JSONObject>() {// 排序
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				long dt1 = o1.getDate("createTime").getTime();
				long dt2 = o2.getDate("createTime").getTime();
				return -Long.compare(dt1, dt2);
			}
		});

		return data;
	}

	@Override
	public JSONObject getStudentScoreReportList(JSONObject params) {
		long classId = params.getLongValue("classId");
		long schoolId = params.getLongValue("schoolId");
		String termInfoId = params.getString("termInfoId");

		Integer type = params.getInteger("type");
		JSONObject data = null;
		if (type == 1) {
			data = getStudentScoreReportListType1(params);
		} else if (type == 2) {
			data = getStudentScoreReportListType2(params);
		} else {
			data = getStudentScoreReportListType3(params);
		}

		Classroom classroom = commonDataService.getClassById(schoolId, classId, termInfoId);
		data.put("className", classroom.getClassName()); // 班级名称
		data.put("studentName", params.get("studName"));
		return data;
	}

	private JSONObject getStudentScoreReportListType1(JSONObject params) { // 校考
		long schoolId = params.getLongValue("schoolId");
		String termInfoId = params.getString("termInfoId");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfoId, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String fbpmFlag = degreeInfo.getFbpmflag();
		
		JSONObject data = new JSONObject();
		data.put("examName", degreeInfo.getKslcmc());

		JSONObject report = reportDao.getStudentAnalysisReport(termInfoId, autoIncr, params);
		if (report == null) { // 没有分析报告，则取原始数据进行组装数据
			data.put("type", 2); // 没有分析报告，则直接展示原始成绩

			List<JSONObject> listSubjectScore = reportDao.getSchoolExamSubjectScoreList(termInfoId, autoIncr, params);

			School school = commonDataService.getSchoolById(schoolId, termInfoId);
			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoList(school, termInfoId);

			Map<String, LessonInfo> lessonInfoMap = StringUtil.convertToMap(lessonInfos, LessonInfo._Fields.ID);

			List<JSONObject> scoreList = new ArrayList<JSONObject>();
			if (CollectionUtils.isNotEmpty(listSubjectScore)) {
				for (JSONObject subject : listSubjectScore) {
					String subjectId = subject.getString("subjectId");
					LessonInfo lessonInfo = lessonInfoMap.get(subjectId);
					String name = lessonInfo.getName();
					if (name == null) {
						name = "";
					}

					JSONObject score = new JSONObject();
					score.put("name", name);
					score.put("value", StringUtil.formatNumber(subject.get("score"), 1));
					scoreList.add(score);
				}
			}
			data.put("rows", scoreList);
		} else {
			data.put("type", params.get("type")); // type = 1
			JSONObject scoreResultDataJSON = null;
			if (StringUtils.isNotBlank(report.getString("scoreResultData"))) {
				scoreResultDataJSON = JSON.parseObject(report.getString("scoreResultData"));
				if (!"1".equals(fbpmFlag) && scoreResultDataJSON.containsKey("rows")) {
					JSONArray rows = scoreResultDataJSON.getJSONArray("rows");
					Iterator<Object> iterator = rows.iterator();
					while(iterator.hasNext()) {
						JSONObject item = (JSONObject) iterator.next();
						String itemName = item.getString("itemName");
						if("班级排名".equals(itemName) || "年级排名".equals(itemName)) {
							iterator.remove();
						}
					}
					scoreResultDataJSON.put("rows", rows);
				}
			}
			data.put("scoreResultData", scoreResultDataJSON == null ? new JSONObject() : scoreResultDataJSON);

			JSONArray advanceTip = null;
			if (StringUtils.isNotBlank(report.getString("advanceTip"))) {
				advanceTip = JSON.parseArray(report.getString("advanceTip"));
			}
			data.put("advanceTip", advanceTip == null ? new JSONObject() : advanceTip);

			JSONArray fallBehindTip = null;
			if (StringUtils.isNotBlank(report.getString("fallBehindTip"))) {
				fallBehindTip = JSON.parseArray(report.getString("fallBehindTip"));
			}
			data.put("fallBehindTip", fallBehindTip == null ? new JSONObject() : fallBehindTip);

			JSONObject subjectGoodBadData = null;
			if (StringUtils.isNotBlank(report.getString("subjectGoodBadData"))) {
				subjectGoodBadData = JSON.parseObject(report.getString("subjectGoodBadData"));
			}
			data.put("subjectGoodBadData", subjectGoodBadData == null ? new JSONObject() : subjectGoodBadData);

			JSONArray subjectGoodBadTip = null;
			if (StringUtils.isNotBlank(report.getString("subjectGoodBadTip"))) {
				subjectGoodBadTip = JSON.parseArray(report.getString("subjectGoodBadTip"));
			}
			data.put("subjectGoodBadTip", subjectGoodBadTip == null ? new JSONObject() : subjectGoodBadTip);

			/*
			 * JSONObject myScoreChangeData = null;
			 * if(StringUtils.isNotBlank(report.getString("myScoreChangeData")))
			 * { myScoreChangeData =
			 * JSONObject.parseObject(report.getString("myScoreChangeData")); }
			 * data.put("myScoreChangeData", myScoreChangeData == null ? new
			 * JSONObject() : myScoreChangeData);
			 * 
			 * JSONArray myScoreChangeTip = null;
			 * if(StringUtils.isNotBlank(report.getString("myScoreChangeTip")))
			 * { myScoreChangeTip =
			 * JSONObject.parseObject(report.getString("myScoreChangeTip")); }
			 * data.put("myScoreChangeTip", myScoreChangeTip == null ? new
			 * JSONObject() : myScoreChangeTip);
			 */
		}

		params.put("autoIncr", autoIncr); // 给下一个service方法用
		return data;
	}

	private JSONObject getStudentScoreReportListType2(JSONObject params) { // 班级小考，系统格式
		long schoolId = params.getLongValue("schoolId");
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo examInfo = this.classScoreDao.getClassExamInfoById(termInfoId, params);
		if (examInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = examInfo.getAutoIncr();

		params.put("scoreDataType", examInfo.getScoreDataType());

		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoList(school, termInfoId);
		Map<String, String> subjectId2Name = new HashMap<String, String>();// 存放科目信息Map
		for (LessonInfo lessonInfo : lessonInfos) {
			String subjectId = String.valueOf(lessonInfo.getId());
			if (!subjectId2Name.containsKey(subjectId))
				subjectId2Name.put(subjectId, lessonInfo.getName());
		}

		JSONObject data = new JSONObject();
		data.put("examName", examInfo.getExamName());
		data.put("type", 2);

		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<JSONObject> sclist = reportDao.getStudentClassExamByExamId(termInfoId, autoIncr, params);

		JSONObject totalScore = new JSONObject();
		for (JSONObject json : sclist) {
			String subjectId = json.getString("subjectId");
			if ("totalScore".equals(subjectId)) {
				totalScore.put("name", "总分");
				totalScore.put("value", json.get("score"));
				continue;
			}

			JSONObject row = new JSONObject();
			String name = subjectId2Name.get(subjectId);
			if (name == null) {
				name = "";
			}
			row.put("name", name);
			row.put("value", json.get("score"));
			rows.add(row);
		}
		Collections.sort(rows, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				long subjectId1 = arg1.getLongValue("subjectId");
				long subjectId2 = arg2.getLongValue("subjectId");
				return Long.compare(subjectId1, subjectId2);
			}
		});
		rows.add(totalScore);
		data.put("rows", rows);
		data.put("type", params.get("type"));

		params.put("autoIncr", autoIncr); // 给下一个service方法用
		return data;
	}
	
	private JSONObject getStudentScoreReportListType3(JSONObject params) { // 班级小考，自定义格式
		String termInfoId = params.getString("termInfoId");
		ClassExamInfo examInfo = this.classScoreDao.getClassExamInfoById(termInfoId, params);
		if (examInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = examInfo.getAutoIncr();

		JSONObject data = new JSONObject();
		data.put("examName", examInfo.getExamName());
		data.put("type", params.get("type"));

		List<CustomScore> l_salExcel = viewClassScoreDao.getAllCustomExcel(termInfoId, autoIncr, params); // 表头
		Map<String, CustomScore> projId2ObjMap = new HashMap<String, CustomScore>();
		for (CustomScore excel : l_salExcel) {
			projId2ObjMap.put(excel.getProjectId(), excel);
		}

		List<CustomScoreInfo> l_salDetail = viewClassScoreDao.getAllCustomDetail(termInfoId, autoIncr, params);
		Collections.sort(l_salDetail, new Comparator<CustomScoreInfo>(){
			@Override
			public int compare(CustomScoreInfo o1, CustomScoreInfo o2) {
				String projectId1 = o1.getProjectId();
				long sort1 = Long.parseLong(projectId1.substring(projectId1.indexOf("c") + 1));
				String projectId2 = o2.getProjectId();
				long sort2 = Long.parseLong(projectId2.substring(projectId1.indexOf("c") + 1));
				return Long.compare(sort1, sort2);
			}
		});
		
		List<List<JSONObject>> rows = new ArrayList<List<JSONObject>>();
		Stack<CustomScore> stack = new Stack<CustomScore>();
		for (CustomScoreInfo score : l_salDetail) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			
			String projectId = score.getProjectId();
			while(projId2ObjMap.containsKey(projectId)) {
				CustomScore excel = projId2ObjMap.get(projectId);
				stack.push(excel);
				projId2ObjMap.remove(projectId);
				projectId = excel.getParentProjectId();
			}
			
			CustomScore excel = null;
			while(!stack.isEmpty()) {
				excel = stack.pop();
				JSONObject titleCell = new JSONObject();
				titleCell.put("rowspan", excel.getColSpan());
				titleCell.put("colspan", excel.getRowSpan());
				titleCell.put("value", excel.getProjectName());
				titleCell.put("TitleCount", excel.getTitleCount());
				titleCell.put("isHead", 1);
				list.add(titleCell);
			}

			JSONObject valCell = new JSONObject();
			valCell.put("rowspan", 1);
			valCell.put("colspan", 1);
			valCell.put("value", score.getScore());
			valCell.put("TitleCount", excel.getTitleCount());
			valCell.put("isHead", 0);
			list.add(valCell);

			rows.add(list);
		}

		data.put("rows", rows);
		params.put("autoIncr", autoIncr); // 给下一个service方法用
		return data;
	}

	@Override
	public void insertStudentScoreReportTrace(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		Integer autoIncr = params.getInteger("autoIncr");

		StudentScoreReportTrace trace = new StudentScoreReportTrace();
		trace.setExamId(params.getString("examId"));
		trace.setSchoolId(params.getString("schoolId"));
		trace.setStudentId(params.getString("studentId"));
		trace.setAccountId(params.getString("accountId"));
		trace.setClassId(params.getString("classId"));
		trace.setViewFlag(1);
		trace.setTermInfoId(params.getString("termInfoId"));
		reportDao.insertStudentScoreReportTrace(termInfoId, autoIncr, trace);
	}

	@Override
	public JSONArray getScoreReportTypeList(String schooId, String studyState, String roleId) {
		JSONArray result = new JSONArray();// 返回结果json数组

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("studyStage", studyState); // 设置学习阶段
		param.put("xxdm", schooId); // 设置学校编号
		// param.put("role", roleId); // 角色编号

		List<JSONObject> reportConfigList = reportDao.getScoreReportTypeList(param); // 报告配置数据

		result.add(this.extractPointTypeReportTypeJson("01", "分数报表", roleId, reportConfigList));
		result.add(this.extractPointTypeReportTypeJson("02", "等第报表", roleId, reportConfigList));
		result.add(this.extractPointTypeReportTypeJson("03", "综合报表", roleId, reportConfigList));
		// 2.返回结果
		return result;
	}

	@Override
	public List<JSONObject> getExamSubjectList(JSONObject params) {
		String termInfoId = params.getString("xnxq");
		String examId = params.getString("kslc"); // 考试编号
		Long schoolId = params.getLong("xxdm");

		int isAll = params.getIntValue("isAll"); // 0不允许显示全部选项，1允许显示全部选项
		int examType = params.getIntValue("examType"); // 考试科目类型:0普通 1竞赛组统计科目
		Integer type = params.getInteger("type");
		
		List<JSONObject> data = new ArrayList<JSONObject>();
		StringBuffer all = new StringBuffer();
		if (StringUtils.isEmpty(examId)) {
			School school = commonDataService.getSchoolById(schoolId, termInfoId);
			List<LessonInfo> lelist = commonDataService.getLessonInfoByType(school, 0, termInfoId);// 普通
			List<LessonInfo> lelist1 = commonDataService.getLessonInfoByType(school, 1, termInfoId);// 综合
			lelist.addAll(lelist1);
			for (LessonInfo c : lelist) {
				if(type != null && type != c.getType()) {
					continue;
				}
				
				JSONObject obj = new JSONObject();
				obj.put("value", c.getId());
				obj.put("text", c.getName());
				all.append(c.getId()).append(",");
				data.add(obj);
			}
		} else {
			DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfoId, params);
			if (degreeInfo == null) {
				throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
			}
			Integer autoIncr = degreeInfo.getAutoIncr();

			String useGradeId = params.getString("nj");// 使用年级
			String isModify = StringUtil.transformString(params.get("isModify"));

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("xxdm", String.valueOf(schoolId));
			map.put("kslc", examId);
			map.put("xnxq", termInfoId);
			List<String> gralist = StringUtil.convertToListFromStr(useGradeId, ",", String.class);
			if (CollectionUtils.isNotEmpty(gralist)) {
				map.put("njList", gralist);
			}
			// 考试下的科目
			List<Long> examSubjIdList = new ArrayList<Long>();
			if (0 == examType) {// 普通的科目列表
				map.put("isModify", isModify);
				examSubjIdList = reportDao.getExamSubjectList(termInfoId, autoIncr, map);
			} else if (1 == examType) {// 竞赛类型的科目
				examSubjIdList = reportDao.getSubjIdFromCompetitionStu(termInfoId, autoIncr, map);
			}

			if (examSubjIdList.size() == 0) {
				return new ArrayList<JSONObject>();
			}

			List<LessonInfo> list = commonDataService.getLessonInfoBatch(schoolId, examSubjIdList, termInfoId);

			if (CollectionUtils.isEmpty(list)) {
				throw new CommonRunException(-1, "无法从SDK获取基础数据，请联系管理员！");
			}

			for (LessonInfo lessonInfo : list) {
				if(type != null && type != lessonInfo.getType()) {
					continue;
				}
				
				JSONObject obj = new JSONObject();
				obj.put("value", lessonInfo.getId());
				obj.put("text", lessonInfo.getName());
				all.append(lessonInfo.getId()).append(",");
				data.add(obj);
			}
		}

		// 排序
		Collections.sort(data, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				long val1 = arg1.getLongValue("value");
				long val2 = arg2.getLongValue("value");
				return Long.compare(val1, val2);
			}
		});

		int isTotal = params.getIntValue("isTotal"); // 0:不显示总分，1:显示总分
		// 添加总分科目数据
		if (0 != isTotal && 1 != examType && data.size() > 0) {
			JSONObject obj = new JSONObject();
			obj.put("value", "totalScore");
			obj.put("text", "总分");
			all.append("totalScore").append(",");
			data.add(0, obj);
		}

		if (isAll > 0) {// 是否显示全部选项
			String avalues = "";
			if (all.length() > 0) {
				avalues = all.substring(0, all.length() - 1);
			}
			if (data.size() > 0) {
				JSONObject obj = new JSONObject();
				obj.put("value", avalues);
				obj.put("text", "全部");
				data.add(0, obj);
			}
		}
		return data;
	}

	@Override
	public JSONObject getClassScoreReportInfo(JSONObject params) {
		String bmfz = params.getString("bmfz");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		
		//++++++++++++++++++   新高考成绩       +++++++++++++++++++++++
		JSONObject result = new JSONObject();
		String msg = "返回成功！";
		Integer code = 0;
		result.put("msg", msg);
		result.put("code", code);
		
		try {
			//新、旧成绩接口判断标准
			if(StringUtils.isEmpty(bmfz)){
				String subjectIds = params.getString("subjectId");								
				String usedGradeId = params.getString("usedGradeId");
				String classIds = params.getString("classId");
				String queryStuName = params.getString("studentName");
				
				//获取查询教学班下的学生
				List<String> subList = new ArrayList<>(Arrays.asList(subjectIds.split(",")));
				List<String> clsList = Arrays.asList(classIds.split(","));
				
				//科目下的教学班
				Map<String,List<String>> tclassInSub = new HashMap<String,List<String>>();
				
				//获取查询学生
				List<String> stuIdList = new ArrayList<String>();
				Map<String,List<JSONObject>> stuIdTclassMap = new HashMap<String,List<JSONObject>>();
				Map<String,JSONObject> stuIdNameMap = new HashMap<String,JSONObject>();
				
				List<Account> qureryStuList = null;
				List<Long> accountIds = null;
				List<JSONObject> stuInTclassMap = null;
				
				JSONObject queryParams = new JSONObject();
				queryParams.put("schoolId", params.get("schoolId"));
				queryParams.put("termInfoId", xnxq.substring(4));
				queryParams.put("schoolYear", xnxq.substring(0,4));
				queryParams.put("scheduleId", params.get("scheduleId"));
				queryParams.put("placementId", params.get("placementId"));
				
				if(subList.contains("1002")){
					subList.remove("1002");
					subList.add("-999");
				}
				/*if(subList.size()==1 && "1002".equals(subList.get(0))){
					subList.clear();
					//subList.addAll(Arrays.asList("1","2","3"));
					subList.add("-999");
				}*/
				//queryParams.put("subIdList", subList);
				
				//queryParams.put("tclassIdList", clsList);
				queryParams.put("xnxq", xnxq);
				
				try{
					logger.info("查询教学班下学生[入参]："+params);
					stuInTclassMap = scheduleLookUpService.getStuInTclass(queryParams);								
				}catch (Exception e) {
					logger.info("查询教学班下学生出错！");
					throw new Exception(e);
				}				
				
				if(CollectionUtils.isEmpty(stuInTclassMap)){
					msg = "查询教学班下学生为空！";
					return result;
				}
				
				//获取学生Id、学生所在教学班,科目下教学班
				accountIds = new ArrayList<Long>();
				for(JSONObject stuInTclass : stuInTclassMap){
					String subId = stuInTclass.getString("subjectId");
					List<String> tclassList = tclassInSub.get(subId);
					
					//添加学生所在教学班
					List<JSONObject> stuInTclsList = stuIdTclassMap.get(stuInTclass.getString("studentId"));
					if(null==stuInTclsList){
						stuInTclsList = new ArrayList<JSONObject>();
						stuIdTclassMap.put(stuInTclass.getString("studentId"), stuInTclsList);
					}
					stuInTclsList.add(stuInTclass);												
					
					//科目下的教学班
					if(tclassList==null){
						tclassList = new ArrayList<String>();
						tclassInSub.put(subId, tclassList);
					}
					if(!tclassList.contains(stuInTclass.get("tclassId"))){
						tclassList.add(stuInTclass.getString("tclassId"));
					}
					
					//查询学生账号列表
					if(accountIds.contains(stuInTclass.getLong("studentId"))){
						continue;
					}
					accountIds.add(stuInTclass.getLong("studentId"));
				}
				
				
				if(StringUtils.isNotEmpty(queryStuName)){
					qureryStuList = commonDataService.getStudentList(params.getLongValue("schoolId"), xnxq, queryStuName);
					if(CollectionUtils.isEmpty(qureryStuList)){			
						msg = "没有查到对应的学生！";
						return result;	
					}
					/*if(null==accountIds){
						accountIds = new ArrayList<Long>();
					}*/
					List<Long> nameAccounts = new ArrayList<Long>();
					for(Account account : qureryStuList){
						nameAccounts.add(account.getId());						
					}
					Iterator<Long> accountIdsIter = accountIds.iterator();
					while(accountIdsIter.hasNext()){
						Long accountId = accountIdsIter.next();
						if(!nameAccounts.contains(accountId)){
							accountIdsIter.remove();
						}
					}
				}/*else{*/
					//---
				
				logger.info("基础数据库查询学生账户,入参：{schoolId:"+params.getLongValue("schoolId")+"xnxq:"+xnxq+"\naccountIds:"+accountIds+"}");
				qureryStuList = commonDataService.getAccountBatch(params.getLongValue("schoolId"), accountIds, xnxq);					
				//}
				
				if(CollectionUtils.isEmpty(qureryStuList)){
					msg = "查询学生为空！";
					return result;
				}
				
				List<Long> crIds = new ArrayList<Long>();
				//学生Id,Name映射				
				for(Account account : qureryStuList){
					stuIdList.add(String.valueOf(account.getId()));
					JSONObject clsIdName = new JSONObject();
					clsIdName.put("name", account.getName());										
					
					Iterator<User> userIter = account.getUsersIterator();
					while(userIter.hasNext()){
						User user = userIter.next();
						StudentPart stu = user.getStudentPart();
						if(null!=stu){
							if(!crIds.contains(stu.getClassId())){
								crIds.add(stu.getClassId());
							}
							clsIdName.put("crId", stu.getClassId());
							clsIdName.put("studentNo", stu.getStdNumber());
						}
					}
					stuIdNameMap.put(String.valueOf(account.getId()), clsIdName);
				}
				logger.info("基础数据库查询教学班,入参：{}");
				List<Classroom> crList = commonDataService.getSimpleClassBatch(params.getLongValue("schoolId"), crIds, xnxq);
				Map<Long,String> crIdNameMap = new HashMap<Long,String>();
				for(Classroom cr : crList){
					crIdNameMap.put(cr.getId(), cr.getClassName());
				}
				
				
				//获取学生成绩
				List<JSONObject> showDatas = new ArrayList<JSONObject>();
				queryParams = new JSONObject();
				queryParams.put("xxdm", xxdm);
				queryParams.put("xnxq", xnxq);
				queryParams.put("kslcdm", params.get("kslc"));
				queryParams.put("nj", usedGradeId);					
				//queryParams.put("studentId", stuIdList);
				//queryParams.put("bhList", clsList);
				if(subList.size()==1){	
					if(!"-999".equals(subList.get(0))){
						//queryParams.put("subjectId", subList.get(0));
					}else{
						queryParams.put("excludeSubIds", Arrays.asList("4","5","6","7","8","9"));
						queryParams.put("bhList", clsList);
					}
				}
				
				//datagrid 科目表头
				List<Long> querySubIds = new ArrayList<Long>();
				
				try {
					logger.info("查询学生成绩[入参]：{xnxq:"+xnxq+",autoIncr:"+autoIncr+",params:"+queryParams+"}");
					//行政班下所有成绩
					List<JSONObject> datas = null;
					try {
						datas = reportDao.getStuSubjectScoreRank_ScoreReport(xnxq, autoIncr, queryParams);						
					} catch (Exception e) {
						logger.info("查询学生成绩出错！");
						throw new Exception(e);
					}
					String preStuId = null;
					String preClsId = null;	//用于计算3+3选科、总分班级排名
					JSONObject stuScores = null;
					float scoreOf3P3=0,totalScoreOf3P3=0;					
					//用于计算3+3总分、选科年级排名
					List<Float> scoresOf3P3 = new ArrayList<Float>();
					List<Float> totalScoresOf3P3 = new ArrayList<Float>();
					
					List<Float> classTotalScoresOf3P3 = new ArrayList<Float>();
					List<Float> classScoresOf3P3 = new ArrayList<Float>();
					//boolean summaryScoreTime = false;
					int dataSize = datas.size();
					int curShowDataSize = 0;
					int calcClsSize=0;
					//用于统计走班单科成绩班级、年级排名
					Map<String,List<Float>> singleSubScores = new HashMap<String,List<Float>>();
					for(int n=0; n<dataSize; n++){
						JSONObject data = datas.get(n);
						Long curSubId = data.getLong("kmdm");
						if(!querySubIds.contains(curSubId)){
							querySubIds.add(curSubId);
						}
						
						//过滤走班科目(单科查询)
						if(subList.size()==1){
							if(4<=curSubId && curSubId<=9 || curSubId==19){
								if(!curSubId.equals(Long.parseLong(subList.get(0)))){
									//continue;
								}
							}							
						}
						
						//拼凑根据学生排序后的结果
						String studentId = data.getString("xh");						
						if(studentId!=null && !studentId.equals(preStuId) || n ==dataSize-1){
							//需要进行3+3成绩汇总
							if((subList.size()>1 && preStuId!=null) || n == dataSize-1){
								//学生个人3+3汇总
								stuScores.put("total_score", totalScoreOf3P3+scoreOf3P3);//选课综合成绩						
								stuScores.put("comp_score", scoreOf3P3);//3+3总分
								
								//年级
								scoresOf3P3.add(scoreOf3P3);
								totalScoresOf3P3.add(totalScoreOf3P3+scoreOf3P3);
								
								//班级
								classTotalScoresOf3P3.add(totalScoreOf3P3+scoreOf3P3);
								classScoresOf3P3.add(scoreOf3P3);
								
								//班级3+3汇总
								String clsId = data.getString("bh");								
								if(clsId!=null && !clsId.equals(preClsId) || n ==dataSize-1){									
									//int clsSize = classTotalScoresOf3P3.size();
									//回溯-补充学生班级3+3排名
									if(preClsId!=null || n ==dataSize-1){
										for(int i=1; i<=curShowDataSize-calcClsSize; i++){
											JSONObject tmp= showDatas.get(curShowDataSize-i);
											tmp.put("total_classRank", 
													/*getPosition(classTotalScoresOf3P3, tmp.getFloatValue("total_score"))*/"--");
											tmp.put("comp_classRank", 
													/*getPosition(classScoresOf3P3, tmp.getFloatValue("comp_score"))*/"--");
										}
										calcClsSize = curShowDataSize;
										//统计下一个班级
										classTotalScoresOf3P3.clear();
										classScoresOf3P3.clear();
									}
								}
								preClsId = clsId;
							}
							
							if(n != dataSize-1){
								stuScores = new JSONObject();	
								stuScores.put("studentId", studentId);
								//补全姓名、行政班信息
								JSONObject stuInfo = stuIdNameMap.get(studentId);
								if(null!=stuInfo){
									stuScores.put("studentNo", stuInfo.get("studentNo"));
									stuScores.put("studentName", stuInfo.get("name"));
									stuScores.put("classsName", crIdNameMap.get(stuInfo.get("crId")));
								}
								showDatas.add(stuScores);
								preStuId = studentId;
								curShowDataSize++;
								//3+3选科、综合成绩
								scoreOf3P3=0;totalScoreOf3P3=0;
							}
						}																							
						
						//判断学、选考班级
						List<JSONObject> stuInTclsList = stuIdTclassMap.get(studentId);
						int clsType = 2;//默认学考
						String prefix = data.getString("kmdm");
						float score = data.getFloatValue("cj");
						
						if(4<=curSubId && 9>=curSubId || curSubId==19){	//学选考科目(包含技术)
							
							
							//totalScoreOf3P3+=score;	
							if(null!=stuInTclsList){
								for(JSONObject tcls : stuInTclsList){
									Long subId = tcls.getLong("subjectId"); 
									if(subId.equals(curSubId)){
										String tclassId = tcls.getString("tclassId");
										clsType = tcls.getIntValue("tclassLevel");
										stuScores.put(curSubId+"_tclassName", tcls.get("tclassName"));
										stuScores.put(curSubId+"_tclassId", tclassId);
										stuScores.put(curSubId+"_tclassType", clsType);
										//选科成绩列表
										List<Float> tclassScoreSet = singleSubScores.get(tclassId);
										if(tclassScoreSet==null){
											tclassScoreSet = new ArrayList<Float>();
											singleSubScores.put(tclassId, tclassScoreSet);
										}
										tclassScoreSet.add(score);

										//学选年级成绩列表
										List<Float> gradeScoreSet = null;
										String type=String.valueOf(subId);
										
										//3+3选科
										if(clsType==1){
											scoreOf3P3+=score;
											type += "_opt_grade";
										}else{//学考年级成绩列表
											type += "_pro_grade";
										}
										gradeScoreSet = singleSubScores.get(type);
										if(null == gradeScoreSet){
											gradeScoreSet = new ArrayList<Float>();
											singleSubScores.put(type, gradeScoreSet);
										}
										gradeScoreSet.add(score);
										
										break;
									}
								}
							}
						}else{
							totalScoreOf3P3+=score;//语数外等
						}
						
						//设置前缀
						prefix+=(2==clsType)?"_pro":"_";
						
						stuScores.put(prefix+"score", data.get("cj"));
						stuScores.put(prefix+"classRank",data.get("bjpm"));
						stuScores.put(prefix+"gradeRank", data.get("njpm"));
												
					}
					//统计3+3总分、选科成绩排名 & 剔选符合条件的数据
					if(subList.size()>=1){
						//Object[] totalScores = totalScoresOf3P3.descendingSet().toArray();
						//Object[] scores3 = scoresOf3P3.descendingSet().toArray();
						for(JSONObject scores : showDatas){
							//年级排名
							if(null!=scores){
								scores.put("total_gradeRank","--"/*getPosition(totalScoresOf3P3, scores.getFloatValue("total_score"))*/);//总分年级排名							
								scores.put("comp_gradeRank", /*getPosition(scoresOf3P3, scores.getFloatValue("comp_score"))*/"--");//选科年级排名
							}
							
							//学选考班级、年级排名
							for(int i=4;i<10;i++){
								int tclassType = scores.getIntValue(i+"_tclassType");
								String tclassId = scores.getString(i+"_tclassId");
								List<Float> clsScoreList = singleSubScores.get(tclassId);
								float stuSubScore = 0;
								//年级成绩列表
								StringBuffer gradeKey = new StringBuffer().append(i);
								StringBuffer clsRankKey = new StringBuffer().append(i);
								StringBuffer gradeRankKey = new StringBuffer().append(i);
								if(1==tclassType){
									stuSubScore = scores.getFloatValue(i+"_score");
									clsRankKey.append("_classRank");
									gradeRankKey.append("_gradeRank");
									gradeKey.append("_opt_grade");
								}else if(2==tclassType){
									stuSubScore = scores.getFloatValue(i+"_proscore");
									clsRankKey.append("_proclassRank");
									gradeRankKey.append("_progradeRank");
									gradeKey.append("_pro_grade");
								}else{
									continue;
								}
								List<Float> gradeScoreList = singleSubScores.get(gradeKey.toString());
								scores.put(clsRankKey.toString(), 
										getPosition(clsScoreList, stuSubScore));
								scores.put(gradeRankKey.toString(),
										getPosition(gradeScoreList, stuSubScore));								
							}
						}												
					}
				} catch (Exception e) {
					logger.error("内部错误！");
					throw new Exception(e);
				}
				
				//过滤走班科目
				if(subList!=null && subList.size()==1 && "4,5,6,7,8,9".contains(subList.get(0))){
					querySubIds.clear();
					querySubIds.add(Long.parseLong(subList.get(0)));
				}
				
				//剔选查询学生
				Iterator<JSONObject> dataIter = showDatas.iterator();
				while(dataIter.hasNext()){
					JSONObject showData = dataIter.next();
					if(CollectionUtils.isNotEmpty(accountIds)){
						Long studentId = showData.getLong("studentId");
						if(!accountIds.contains(studentId)){
							dataIter.remove();
						}
					}
				}
				
				//过滤班级
				if(clsList.size()==1){
					accountIds = new ArrayList<Long>();
					//固定上课（语数外等）
					try {
						Long crId = Long.parseLong(clsList.get(0));
						if(crIdNameMap.containsKey(crId)){							
							for(Classroom cr : crList){
								if(crId.equals(cr.getId())){
									Classroom room = commonDataService.getClassById(params.getLongValue("schoolId"), crId, xnxq);
									accountIds.addAll(room.getStudentAccountIds());
									Iterator<Long> querySubIter = querySubIds.iterator();
									while(querySubIter.hasNext()){
										Long querySubId = querySubIter.next();
										if(4<=querySubId && querySubId<=9){
											querySubIter.remove();
										}
									}
									break;
								}
							}
						}
					} catch (Exception e) {	//走班班级对应的科目
						String tclassId = clsList.get(0);
						querySubIds.clear();
						for(JSONObject stuInTclass : stuInTclassMap){
							if(tclassId!=null && tclassId.equals(stuInTclass.get("tclassId"))){
								accountIds.add(stuInTclass.getLong("studentId"));
								if(!querySubIds.contains(stuInTclass.getLong("subjectId"))){
									querySubIds.add(stuInTclass.getLong("subjectId"));
								}
							}
						}
					}	
				}
				
				//过滤教学班
				if(querySubIds!=null && querySubIds.size()==1
						&& clsList!=null && clsList.size()==1){
					Iterator<JSONObject>datasIter = showDatas.iterator();
					while(datasIter.hasNext()){
						JSONObject data = datasIter.next();
						String tclassId = data.getString(querySubIds.get(0)+"_tclassId");
						if(tclassId!=null && !tclassId.equals(clsList.get(0)) || tclassId==null){
							datasIter.remove();
						}
					}
				}
				
				// 存放科目信息Map
				Map<Long, String> lessonMap = new HashMap<Long, String>();				
				List<LessonInfo> lessonInfo = commonDataService.getLessonInfoBatch(params.getLong("schoolId"), (List<Long>)querySubIds, xnxq);
				for(LessonInfo lesson : lessonInfo){
					lessonMap.put(lesson.getId(), lesson.getName());
				}
				
				//组合表头
				EasyUIDatagridHead[][] head = new EasyUIDatagridHead[2][];
				//班级、成绩、排名二级表头
				int head1Columns = querySubIds.size(), head2Columns = 0;
				if(querySubIds.size()>3){	//3+3综合
					head2Columns = 2/**3*/; 
					head1Columns += 2;
				}
				//科目表头
				head[0] = new EasyUIDatagridHead[head1Columns];

				for(Long querySub : querySubIds){
					if(4<=querySub && 9>=querySub || querySub==19){
						head2Columns += 7;
					}else{
						head2Columns += 3;
					}
				}
				head[1] = new EasyUIDatagridHead[head2Columns];
				
				//科目Id排序
				Collections.sort(querySubIds);
				int colspan = 3,subSize = querySubIds.size();
				String column1Name = "成绩";
				int colStart=0;
				for(int i=0; i<head1Columns; i++){
					//3+3表头
					if(i>=subSize && querySubIds.size()>3){
						//3+3综合
						head[0][i++] = new EasyUIDatagridHead(null, "选科综合", "center", 0, 1, 1, true);
						head[1][colStart++] = new EasyUIDatagridHead("comp_score", "成绩", "center", 100, 1, 1, true);						
						//head[1][colStart++] = new EasyUIDatagridHead("comp_classRank", "班名", "center", 50, 1,1, true);
						//head[1][colStart++] = new EasyUIDatagridHead("comp_gradeRank", "年名", "center", 50, 1,1, true);
						
						//3+3总分
						head[0][i] = new EasyUIDatagridHead(null, "总分", "center", 0, 1, 1, true);
						head[1][colStart++] = new EasyUIDatagridHead("total_score", "成绩", "center", 100, 1, 1, true);						
						//head[1][colStart++] = new EasyUIDatagridHead("total_classRank", "班名", "center", 50, 1,1, true);
						//head[1][colStart++] = new EasyUIDatagridHead("total_gradeRank", "年名", "center", 50, 1,1, true);						
						break;
					}
					
					//科目表头
					Long querySub = querySubIds.get(i);
					if(4<=querySub && 9>=querySub || querySub==19){
						colspan = 7;
					}
					head[0][i] = new EasyUIDatagridHead(
							null, lessonMap.get(querySub), "center", 0, 1, colspan,true);
					
					if(colspan==7){
						column1Name="选考";
						head[1][colStart++] = new EasyUIDatagridHead(
								querySub + "_tclassName", "走班班级","center", 50, 1, 1, true);
						head[1][colStart++] = new EasyUIDatagridHead(
								querySub + "_score", column1Name,"center", 50, 1, 1, true);
						head[1][colStart++] = new EasyUIDatagridHead(
								querySub + "_classRank", "班名", "center", 50, 1, 1, true);
						head[1][colStart++] = new EasyUIDatagridHead(
								querySub + "_gradeRank", "年名", "center", 50, 1, 1, true);
						
						column1Name="学考";
					}
					
					head[1][colStart++] = new EasyUIDatagridHead(
							querySub + "_proscore", column1Name,"center", 50, 1, 1, true);
					head[1][colStart++] = new EasyUIDatagridHead(
							querySub + "_proclassRank", "班名", "center", 50, 1, 1, true);
					head[1][colStart++] = new EasyUIDatagridHead(
							querySub + "_progradeRank", "年名", "center", 50, 1, 1, true);				
				}
			
				
				result.put("total",showDatas.size());
				result.put("rows", showDatas == null ? "" : showDatas);
				result.put("columns", head == null ? "" : head);
				return result;
			}else{
				return getClassScoreReportInfo2(params);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			result.put("msg", msg);
			result.put("code", code);
			return result;
		}
		
		
		//++++++++++++++++++++++  end   +++++++++++++++++++++++
		
	}
	
	public JSONObject getClassScoreReportInfo2(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		long schoolId = params.getLongValue("xxdm");

		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		List<String> bhList = StringUtil.convertToListFromStr(bhStr, ",", String.class);
		if (CollectionUtils.isNotEmpty(bhList)) {
			params.put("bhList", bhList);
		}

		String nj = params.getString("nj");
		String xxdm = params.getString("xxdm");

		String xmxh = params.getString("xmxh");
		params.remove("xmxh");

		Integer topXRank = params.getInteger("topXRank");
		Integer lastXRank = params.getInteger("lastXRank");
		Float topXTotalPer = params.getFloat("topXTotalPer");
		Float lastXTotalPer = params.getFloat("lastXTotalPer");
		params.remove("topXRank");
		params.remove("lastXRank");
		params.remove("topXTotalPer");
		params.remove("lastXTotalPer");

		// 获取班级信息
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Long> studentIds = new ArrayList<Long>();

		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>();// 存放班级信息列表Map
		if (CollectionUtils.isNotEmpty(classrooms)) {
			for (Classroom classroom : classrooms) {
				classroomMap.put(classroom.getId(), classroom);
				if(classroom.getStudentAccountIds() != null) {
					studentIds.addAll(classroom.getStudentAccountIds());
				}
			}
		}

		// 学生信息列表，从基础数据获取
		Map<Long, User> studentInfos = new HashMap<Long, User>();// 定义map存放学生信息，用以快速取得学生信息
		List<Account> accList = null;
		if (StringUtils.isNotEmpty(xmxh)) {
			HashMap<String, Object> studentParam = new HashMap<String, Object>();
			studentParam.put("schoolId", schoolId);
			studentParam.put("termInfoId", xnxq);
			studentParam.put("usedGradeId", nj);
			studentParam.put("classId", bhStr);
			studentParam.put("keyword", xmxh);
			accList = commonDataService.getStudentList(studentParam);
		} else {
			accList = commonDataService.getAccountBatch(schoolId, studentIds, xnxq);
		}

		if (CollectionUtils.isNotEmpty(accList)) {
			List<Long> accIds = new ArrayList<Long>();
			for (Account acc : accList) {
				if (acc == null) {
					continue;
				}
				long accId = acc.getId();
				accIds.add(accId);
				List<User> users = acc.getUsers();
				if (CollectionUtils.isEmpty(users)) {
					continue;
				}
				for (User user : users) {
					if (user == null) {
						continue;
					}
					UserPart userPart = user.getUserPart();
					if (userPart == null) {
						continue;
					}
					if (T_Role.Student.equals(userPart.getRole())) {
						String classId = String.valueOf(user.getStudentPart().getClassId());
						if (bhList.contains(classId)) {// 当前学生所在班级是在班级范围内
							studentInfos.put(accId, user);
						}
					}
				}
			}
			if (StringUtils.isNotEmpty(xmxh)) {
				params.put("xhList", accIds);
			}
		}
		classIds = null;
		accList = null;
		studentIds = null;

		Integer maxpm = reportDao.getNjpmMax(xnxq, autoIncr, params);
		if (topXTotalPer != null) {
			if (maxpm != null) {
				topXRank = (int) Math.floor(maxpm * topXTotalPer / 100);
			} else {
				topXRank = 0;
			}
		}

		if (lastXRank != null) {
			if (maxpm != null) {
				lastXRank = maxpm - lastXRank;
			} else {
				lastXRank = Integer.MAX_VALUE;
			}
		}

		if (lastXTotalPer != null) {
			if (maxpm != null) {
				lastXRank = (int) Math.floor(maxpm * (1 - lastXTotalPer / 100));
			} else {
				lastXRank = Integer.MAX_VALUE;
			}
		}

		if (topXRank != null) {
			params.put("topXRank", topXRank);
		}
		if (lastXRank != null) {
			params.put("lastXRank", lastXRank);
		}

		// 分组对应的科目
		List<String> kmdmList = reportDao.getKmdmListFromScoretjmk(xnxq, autoIncr, params);
		if(kmdmList.size() > 0) {
			params.put("kmdmList", kmdmList);
		}

		List<JSONObject> bjcjMk = reportDao.getBjcjMk(xnxq, autoIncr, params);
		// 处理科目列表，从外网接口读取科目名称。并设置到json中去
		List<Long> subjectIds = new ArrayList<Long>();
		for (JSONObject json : bjcjMk) {
			long subjectId = json.getLongValue("kmdm");
			
			if (!subjectIds.contains(subjectId)) {
				subjectIds.add(subjectId);
			}
		}

		Map<Long, LessonInfo> lessonMap = new HashMap<Long, LessonInfo>();// 存放科目信息Map
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);// 从基础接口获取科目信息数据
		if (CollectionUtils.isNotEmpty(lessonInfos)) {
			for (LessonInfo lessonInfo : lessonInfos) {// 把科目信息放置到Map
				if (lessonInfo == null) {
					continue;
				}
				lessonMap.put(lessonInfo.getId(), lessonInfo);
			}
		}

		for (JSONObject json : bjcjMk) {// 循环列表，设置科目名称
			long subjectId = json.getLongValue("kmdm");// 科目编号
			LessonInfo lessonInfo = lessonMap.get(subjectId);
			if (lessonInfo != null) {
				json.put("kmmc", lessonInfo.getName());
			}
		}

		// 排序
		Collections.sort(bjcjMk, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				if (!"zf".equals(o1.get("kmdm")) && !"zf".equals(o2.get("kmdm"))) {
					return Long.compare(o1.getLongValue("kmdm"), o2.getLongValue("kmdm"));
				}
				return 0;
			}
		});

		List<JSONObject> bjcjXs = new ArrayList<JSONObject>(); //
		List<JSONObject> noExamList = new ArrayList<JSONObject>(); // 未参加考试的学生
		List<JSONObject> bjcjCj = new ArrayList<JSONObject>(); // 成绩数据
		 
		if ("1".equals(degreeInfo.getFxflag())) { // 是否已经分析过了
			bjcjXs.addAll(reportDao.getBjcjXs(xnxq, autoIncr, params)); // 获取学生id对应年级排名
			noExamList.addAll(reportDao.getNoExamStudentList(xnxq, autoIncr, params)); // 不参与考试的学生
			bjcjCj.addAll(reportDao.getBjcjCj(xnxq, autoIncr, params));
		} else {
			if (lastXRank == null && topXRank == null) {
				bjcjXs.addAll(reportDao.getClassScoreStudentList(xnxq, autoIncr, params));
				bjcjCj.addAll(reportDao.getClassScoreList(xnxq, autoIncr, params));
			}
		}

		// 定义一个新的列表数据，用来存符合条件的返回数据
		List<JSONObject> backDataList = new ArrayList<JSONObject>();
		EasyUIDatagridHead[][] head = null;

		// 处理当搜索前多少名次或是后多少名次的时候，需要特殊处理没有参加统计的学生
		if (lastXRank == null && topXRank == null) {
			bjcjXs.addAll(noExamList);
		}
	 
		if (CollectionUtils.isEmpty(bjcjXs)) {
			JSONObject data = new JSONObject();
			data.put("total", 0);
			data.put("rows", backDataList);
			data.put("columns", "");
			return data;
		}

		
		for (JSONObject json : bjcjXs) {// 过滤数据
			Long studentId = json.getLong("studentId");// 学号

			User user = (User) studentInfos.get(studentId);
			if (user != null) {
				String className = "";// 班级名称
				String studentNo = "";// 学生学号
				if (user.getStudentPart() != null) {
					Classroom classroom = classroomMap.get(user.getStudentPart().getClassId());
					if (classroom != null) {
						className = classroom.getClassName();
					}
					studentNo = user.getStudentPart().getSchoolNumber();
				}

				String studentName = "";// 学生名称
				if (user.getAccountPart() != null) {
					studentName = user.getAccountPart().getName();
				}
				json.put("studentNo", studentNo);
				json.put("classsName", className);
				json.put("studentName", studentName);
				backDataList.add(json);
			}
			
		}
    
		// 3.生成表头，并且设置数据中字段值为空的基础数据
		if (backDataList.size() > 0) {
			
			boolean is16 = false;
			boolean is17 = false;
			// 把科目成绩存放在Map
			Map<String, JSONObject> bjcjCjMap = new HashMap<String, JSONObject>();
			for (JSONObject obCj : bjcjCj) {// 循环把数据放置到Map
				String subjectId = obCj.getString("kmdm");
				String studentId = obCj.getString("xh");
				if (!bjcjCjMap.containsKey(subjectId + studentId)) {
					if (studentInfos.containsKey(Long.valueOf(studentId))) {
						bjcjCjMap.put(subjectId + studentId, obCj);
					}
				}
			}

			for (Map<String, Object> obXs : backDataList) {
				String xh = obXs.get("studentId").toString();
				obXs.put("totalScore", 0);
				obXs.put("totalScoreClassRank", 0);
				obXs.put("totalScoreGradeRank", 0);
				for (Map<String, Object> obMk : bjcjMk) {
					String kmdm = obMk.get("kmdm").toString();
					Map<String, Object> obCj = bjcjCjMap.get(kmdm + xh);
					if (obCj != null) {
						if (obCj.get("cj") == null) {
							obXs.put(kmdm + "_score", 0);
						} else {
							obXs.put(kmdm + "_score", obCj.get("cj"));
						}
						if (obCj.get("bjpm") == null) {
							obXs.put(kmdm + "_classRank", 0);
						} else {
							obXs.put(kmdm + "_classRank", obCj.get("bjpm"));
						}
						if (obCj.get("njpm") == null) {
							obXs.put(kmdm + "_gradeRank", 0);
						} else {
							obXs.put(kmdm + "_gradeRank", obCj.get("njpm"));
						}
					}

					JSONObject kmInfo = new JSONObject();
					kmInfo.put("termInfoId", xnxq);
					kmInfo.put("schoolId", schoolId);
					List<JSONObject> kmInfoLists = csCurCommonDataDao.getLessonListBySchoolId(kmInfo);
					String wzID="";
					String lzID="";
					for(JSONObject kmInfoList:kmInfoLists){
						String kmname = (String) kmInfoList.get("name");
						if(kmname.equals("文综")){
							wzID = kmInfoList.get("lessonId").toString() ;
						}
						if(kmname.equals("理综")){
							lzID = kmInfoList.get("lessonId").toString();
						}
						
					}	
					
						obCj = bjcjCjMap.get(wzID + xh);// 文综
						if (obCj != null) {
							if (obCj.get("cj") == null) {
								obXs.put("comp_score", 0);
							} else {
								obXs.put("comp_score", obCj.get("cj"));
								if (!is16 && !"0.0".equals(obCj.get("cj").toString())) {
									is16 = true;
								}
								
							}
							if (obCj.get("bjpm") == null) {
								obXs.put("comp_classRank", 0);
							} else {
								obXs.put("comp_classRank", obCj.get("bjpm"));
							}
							if (obCj.get("njpm") == null) {
								obXs.put("comp_gradeRank", 0);
							} else {
								obXs.put("comp_gradeRank", obCj.get("njpm"));
							}
						}
						
 
						obCj = bjcjCjMap.get(lzID + xh); //理综
						if (obCj != null) {
							if (obCj.get("cj") == null) {
								obXs.put("total_score", 0);
							} else {
								obXs.put("total_score", obCj.get("cj"));
								if (!is17 && !"0.0".equals(obCj.get("cj").toString())) {
									is17 = true;
								}
							}
							
							if (obCj.get("bjpm") == null) {
								obXs.put("total_classRank", 0);
							} else {
								obXs.put("total_classRank", obCj.get("bjpm"));
							}
							
							if (obCj.get("njpm") == null) {
								obXs.put("total_gradeRank", 0);
							} else {
								obXs.put("total_gradeRank", obCj.get("njpm"));
							}
						}
 
					
					// 学生总分
					obCj = bjcjCjMap.get("totalScore" + xh);
					if (obCj != null) {
						if (obCj.get("cj") == null) {
							obXs.put("totalScore", 0);
						} else {
							obXs.put("totalScore", obCj.get("cj"));
						}

						if (obCj.get("bjpm") == null) {
							obXs.put("totalScoreClassRank", 0);
						} else {
							obXs.put("totalScoreClassRank", obCj.get("bjpm"));
						}

						if (obCj.get("njpm") == null) {
							obXs.put("totalScoreGradeRank", 0);
						} else {
							obXs.put("totalScoreGradeRank", obCj.get("njpm"));
						}
					}
				}
			}
			
			
			
			// 获取报表显示列数，判断需要显示多少列
			String config = (String) configService.getReportFieldAuths(xxdm, "001").get("config");// 获取报表字段显示权限
			JSONObject fieldAuths = config == null ? null : JSONObject.parseObject(config);
			int perFieldNum = 3;// 每科目显示列数，默认显示成绩，班名，年名

			if (fieldAuths != null && "0".equals(fieldAuths.getString("bjpm"))) {
				perFieldNum = perFieldNum - 1;
			}

			if (fieldAuths != null && "0".equals(fieldAuths.getString("njpm"))) {
				perFieldNum = perFieldNum - 1;
			}
            int statistics  = 1 + (is16?1:0) + (is17?1:0) ;// 总分     文综    理综
			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[statistics + bjcjMk.size()];
			head[1] = new EasyUIDatagridHead[(statistics + bjcjMk.size()) * perFieldNum];
			int i = 0;
			for (Map<String, Object> obmk : bjcjMk) {
				head[0][i] = new EasyUIDatagridHead(null, obmk.get("kmmc").toString(), "center", 0, 1, perFieldNum,
						true);
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead(obmk.get("kmdm").toString() + "_score", "成绩",
						"center", 50, 1, 1, true);

				int index = 1;
				if (!(fieldAuths != null && "0".equals(fieldAuths.getString("bjpm")))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead(
							obmk.get("kmdm").toString() + "_classRank", "班名", "center", 50, 1, 1, true);
					index++;
				}

				if (!(fieldAuths != null && "0".equals(fieldAuths.getString("njpm")))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead(
							obmk.get("kmdm").toString() + "_gradeRank", "年名", "center", 50, 1, 1, true);
				}

				i++;
			}
			
 
			int index = 1;
			if (is16) {
				head[0][i] = new EasyUIDatagridHead(null, "文综", "center", 0, 1, perFieldNum, true);
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("comp_score", "成绩", "center", 50, 1, 1, true);
				index = 1;
				if (!"0".equals(fieldAuths.getString("bjpm"))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead("comp_classRank", "班名", "center", 50, 1,
							1, true);
					index++;
				}
				if (!"0".equals(fieldAuths.getString("njpm"))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead("comp_gradeRank", "年名", "center", 50, 1,
							1, true);
				}
				i++;
			}
			 index = 1;
			if (is17) {
				head[0][i] = new EasyUIDatagridHead(null, "理综", "center", 0, 1, perFieldNum, true);
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("total_score", "成绩", "center", 50, 1, 1, true);
				index = 1;
				if (!"0".equals(fieldAuths.getString("bjpm"))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead("total_classRank", "班名", "center", 50, 1,
							1, true);
					index++;
				}
				if (!"0".equals(fieldAuths.getString("njpm"))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead("total_gradeRank", "年名", "center", 50, 1,
							1, true);
				}
				i++;
				
			}
			
			
			head[0][i] = new EasyUIDatagridHead(null, "总分", "center", 0, 1, perFieldNum, true);
			head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("totalScore", "成绩", "center", 50, 1, 1, true);
			index = 1;
			if (!"0".equals(fieldAuths.getString("bjpm"))) {
				head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1,
						1, true);
				index++;
			}
			if (!"0".equals(fieldAuths.getString("njpm"))) {
				head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1,
						1, true);
			}
			
		}

		Collections.sort(backDataList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String className1 = o1.getString("classsName");
				String className2 = o2.getString("classsName");
				System.out.println("o1==>" + o1 );
				System.out.println("o2==>" + o2 );
				return className1.compareTo(className2);
			}
		});

		JSONObject data = new JSONObject();
		data.put("total", backDataList.size());
		data.put("rows", backDataList == null ? "" : backDataList);
		data.put("columns", head == null ? "" : head);
		return data;
	}

	@Override
	public JSONObject getGradeReportList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONObject arnj = reportDao.getARnj(xnxq, autoIncr, params);
		if (arnj == null) {
			arnj = new JSONObject();
			arnj.put("examName", "");
			arnj.put("classViewTip", "");
			arnj.put("averageScoreCompareTab", "");
			arnj.put("excellentRateCompareTab", "");
			arnj.put("passRateCompareTab", "");
			arnj.put("levelValueCompareTab", "");
			arnj.put("rankCompareTab", "");
			arnj.put("subjectViewTip", "");
			arnj.put("subjectCompareTab", "");
			arnj.put("teacherViewTip", "");
			arnj.put("teacherSubjectCompareTab", "");
			return arnj;
		}

		arnj.put("examName", degreeInfo.getKslcmc());
		// 转换属性为json
		String classViewTip = StringUtil.transformString(arnj.get("classViewTip"));
		if (!StringUtil.isEmpty(classViewTip))
			arnj.put("classViewTip", JSON.parse(classViewTip));

		String averageScoreCompareTab = StringUtil.transformString(arnj.get("averageScoreCompareTab"));
		if (!StringUtil.isEmpty(averageScoreCompareTab)) {
			JSONObject json = (JSONObject) JSON.parse(averageScoreCompareTab);
			if (json != null && json.containsKey("series")) {
				JSONArray series = json.getJSONArray("series");
				convertToFloat(series);
			}
			arnj.put("averageScoreCompareTab", json);
		}

		String excellentRateCompareTab = StringUtil.transformString(arnj.get("excellentRateCompareTab"));
		if (!StringUtil.isEmpty(excellentRateCompareTab)) {
			JSONObject json = (JSONObject) JSON.parse(excellentRateCompareTab);
			if (json != null && json.containsKey("series")) {
				JSONArray series = json.getJSONArray("series");
				convertToFloat(series);
			}
			arnj.put("excellentRateCompareTab", json);
		}

		String passRateCompareTab = StringUtil.transformString(arnj.get("passRateCompareTab"));
		if (!StringUtil.isEmpty(passRateCompareTab)) {
			JSONObject json = (JSONObject) JSON.parse(passRateCompareTab);
			if (json != null && json.containsKey("series")) {
				JSONArray series = json.getJSONArray("series");
				convertToFloat(series);
			}
			arnj.put("passRateCompareTab", json);

		}

		String levelValueCompareTab = StringUtil.transformString(arnj.get("levelValueCompareTab"));
		if (!StringUtil.isEmpty(levelValueCompareTab)) {
			arnj.put("levelValueCompareTab", JSON.parse(levelValueCompareTab));
		}

		String rankCompareTab = StringUtil.transformString(arnj.get("rankCompareTab"));
		if (!StringUtil.isEmpty(rankCompareTab)) {
			arnj.put("rankCompareTab", JSON.parse(rankCompareTab));
		}

		String subjectViewTip = StringUtil.transformString(arnj.get("subjectViewTip"));
		if (!StringUtil.isEmpty(subjectViewTip)) {
			arnj.put("subjectViewTip", JSON.parse(subjectViewTip));
		}

		String subjectCompareTab = StringUtil.transformString(arnj.get("subjectCompareTab"));
		if (!StringUtil.isEmpty(subjectCompareTab)) {
			arnj.put("subjectCompareTab", JSON.parse(subjectCompareTab));
		}

		String teacherViewTip = StringUtil.transformString(arnj.get("teacherViewTip"));
		if (!StringUtil.isEmpty(teacherViewTip)) {
			arnj.put("teacherViewTip", JSON.parse(teacherViewTip));
		}

		String teacherSubjectCompareTab = StringUtil.transformString(arnj.get("teacherSubjectCompareTab"));
		if (!StringUtil.isEmpty(teacherSubjectCompareTab)) {
			arnj.put("teacherSubjectCompareTab", JSON.parse(teacherSubjectCompareTab));
		}

		return arnj;
	}

	/****
	 * 转换json中的字符数组为小数
	 * 
	 * @param array
	 */
	private void convertToFloat(JSONArray array) {
		if (array != null) {
			for (Object object : array) {
				JSONObject json = (JSONObject) object;
				if (json != null && json.containsKey("data")) {
					JSONArray data = json.getJSONArray("data");
					for (int i = 0; i < data.size(); i++) {
						data.set(i, Float.parseFloat(data.getString(i)));
					}
				}
			}
		}
	}

	@Override
	public JSONObject getStudentOptimizationList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		List<String> bhList = StringUtil.convertToListFromStr(bhStr, ",", String.class);
		if (CollectionUtils.isNotEmpty(bhList)) {
			params.put("bhList", bhList);
		}

		String nj = params.getString("nj");
		String xxdm = params.getString("xxdm");

		String xmxh = params.getString("xmxh");
		params.remove("xmxh");
		long schoolId = params.getLongValue("xxdm");

		// 获取班级信息
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Long> studentIds = new ArrayList<Long>();

		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>();// 存放班级信息列表Map
		if (CollectionUtils.isNotEmpty(classrooms)) {
			for (Classroom classroom : classrooms) {
				classroomMap.put(classroom.getId(), classroom);
				studentIds.addAll(classroom.getStudentAccountIds());
			}
		}

		// 学生信息列表，从基础数据获取
		Map<Long, User> studentInfos = new HashMap<Long, User>();// 定义map存放学生信息，用以快速取得学生信息
		List<Account> accList = null;
		if (StringUtils.isNotEmpty(xmxh)) {
			HashMap<String, Object> studentParam = new HashMap<String, Object>();
			studentParam.put("schoolId", schoolId);
			studentParam.put("termInfoId", xnxq);
			studentParam.put("usedGradeId", nj);
			studentParam.put("classId", bhStr);
			studentParam.put("keyword", xmxh);
			accList = commonDataService.getStudentList(studentParam);
		} else {
			accList = commonDataService.getAccountBatch(schoolId, studentIds, xnxq);
		}
		studentIds = null;

		if (CollectionUtils.isNotEmpty(accList)) {
			List<Long> accIds = new ArrayList<Long>();
			for (Account acc : accList) {
				if (acc == null) {
					continue;
				}
				long accId = acc.getId();
				accIds.add(accId);
				List<User> users = acc.getUsers();
				if (CollectionUtils.isEmpty(users)) {
					continue;
				}
				for (User user : users) {
					if (user == null) {
						continue;
					}
					UserPart userPart = user.getUserPart();
					if (userPart == null) {
						continue;
					}
					if (T_Role.Student.equals(userPart.getRole())) {
						String classId = String.valueOf(user.getStudentPart().getClassId());
						if (bhList.contains(classId)) {// 当前学生所在班级是在班级范围内
							studentInfos.put(accId, user);
						}
					}
				}
			}
			if (StringUtils.isNotEmpty(xmxh)) {
				params.put("xhList", accIds);
			}
		}
		classIds = null;
		accList = null;

		// 2.读取数据，并且获取班级信息，过滤不属于当前班级学生的成绩信息
		List<JSONObject> bjcjXs = new ArrayList<JSONObject>();
		List<JSONObject> noExamList = new ArrayList<JSONObject>();// 未参加考试的学生
		List<JSONObject> bjcjCj = new ArrayList<JSONObject>();

		List<JSONObject> bjcjMk = reportDao.getBjcjMk(xnxq, autoIncr, params);
		if ("1".equals(degreeInfo.getFxflag())) {
			bjcjXs.addAll(reportDao.getBjcjXs(xnxq, autoIncr, params));
			noExamList.addAll(reportDao.getNoExamStudentList(xnxq, autoIncr, params));
			bjcjCj.addAll(reportDao.getBjcjCj(xnxq, autoIncr, params));
		} else {
			bjcjXs.addAll(reportDao.getClassScoreStudentList(xnxq, autoIncr, params));
			bjcjCj.addAll(reportDao.getClassScoreList(xnxq, autoIncr, params));
		}

		// 定义一个新的列表数据，用来存符合条件的返回数据
		List<JSONObject> backDataList = new ArrayList<JSONObject>();
		EasyUIDatagridHead[][] head = null;

		// 处理当搜索前多少名次或是后多少名次的时候，需要特殊处理没有参加统计的学生
		bjcjXs.addAll(noExamList);

		// 处理数据为空的情况
		if (bjcjXs == null || bjcjXs.size() <= 0) {
			JSONObject data = new JSONObject();
			data.put("total", 0);
			data.put("rows", backDataList);
			data.put("columns", "");
			return data;
		}

		// 处理科目列表，从外网接口读取科目名称。并设置到json中去
		List<Long> subjectIds = new ArrayList<Long>();
		for (JSONObject json : bjcjMk) {
			long subjectId = json.getLongValue("kmdm");// 科目编号
			if (!subjectIds.contains(subjectId)) {
				subjectIds.add(subjectId);
			}
		}

		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);// 从基础接口获取科目信息数据
		Map<Long, LessonInfo> lessonMap = new HashMap<Long, LessonInfo>();// 存放科目信息Map
		if (lessonInfos != null) {
			for (LessonInfo lessonInfo : lessonInfos) {// 把科目信息放置到Map
				if (lessonInfo == null) {
					continue;
				}
				lessonMap.put(lessonInfo.getId(), lessonInfo);
			}
		}

		for (JSONObject json : bjcjMk) {// 循环列表，设置科目名称
			long subjectId = json.getLongValue("kmdm");// 科目编号
			LessonInfo lessonInfo = lessonMap.get(subjectId);
			if (lessonInfo != null) {
				json.put("kmmc", lessonInfo.getName());
			}
		}

		// 排序
		Collections.sort(bjcjMk, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				if (!"zf".equals(o1.get("kmdm")) && !"zf".equals(o2.get("kmdm"))) {
					int firstSubjectId = Integer.parseInt(o1.get("kmdm").toString());
					int secondSubjectId = Integer.parseInt(o2.get("kmdm").toString());
					return firstSubjectId - secondSubjectId;
				}
				return 0;
			}

		});

		for (JSONObject json : bjcjXs) {// 过滤数据
			Long studentId = json.getLong("studentId");// 学号
			if (!studentInfos.containsKey(studentId)) {
				continue;
			}
			User user = studentInfos.get(studentId);
			if (user != null) {
				String className = "";// 班级名称
				String studentNo = user.getStudentPart().getSchoolNumber();// 学生学号
				Classroom classroom = classroomMap.get(user.getStudentPart().getClassId());
				if (classroom != null) {
					className = classroom.getClassName();
				}
				String studentName = user.getAccountPart().getName();// 学生名称

				json.put("studentNo", studentNo);
				json.put("classsName", className);
				json.put("studentName", studentName);
			}
			backDataList.add(json);
		}

		// 3.生成表头，并且设置数据中字段值为空的基础数据
		if (backDataList.size() > 0) {
			// 获取报表显示列数，判断需要显示多少列
			String config = (String) configService.getReportFieldAuths(xxdm, "024").get("config");// 获取报表字段显示权限
			JSONObject fieldAuths = config == null ? null : JSON.parseObject(config);
			int perFieldNum = 4;// 每科目显示列数，默认显示成绩，班名，年名

			if (fieldAuths != null && "0".equals(fieldAuths.getString("cj"))) {
				perFieldNum = perFieldNum - 1;
			}

			if (fieldAuths != null && "0".equals(fieldAuths.getString("bjpm"))) {
				perFieldNum = perFieldNum - 1;
			}

			if (fieldAuths != null && "0".equals(fieldAuths.getString("njpm"))) {
				perFieldNum = perFieldNum - 1;
			}

			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[1 + bjcjMk.size()];
			head[1] = new EasyUIDatagridHead[(1 + bjcjMk.size()) * perFieldNum];
			int i = 0;
			for (JSONObject obmk : bjcjMk) {
				String kmdm = obmk.getString("kmdm");
				String kmmc = obmk.getString("kmmc");
				head[0][i] = new EasyUIDatagridHead(null, kmmc, "center", 0, 1, perFieldNum, true);

				int index = 1;
				if (!(fieldAuths != null && "0".equals(fieldAuths.getString("cj")))) {
					head[1][i * perFieldNum + 0] = new EasyUIDatagridHead(kmdm + "_score", "原始分数", "center", 60, 1, 1,
							true);

					head[1][i * perFieldNum + index] = new EasyUIDatagridHead(kmdm + "_bzscore", "标准分数", "center", 60,
							1, 1, true);

					index++;
				} else {
					head[1][i * perFieldNum + 0] = new EasyUIDatagridHead(kmdm + "_bzscore", "标准分数", "center", 60, 1, 1,
							true);

				}

				if (!(fieldAuths != null && "0".equals(fieldAuths.getString("bjpm")))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead(kmdm + "_classRank", "班名", "center", 50,
							1, 1, true);
					index++;
				}

				if (!(fieldAuths != null && "0".equals(fieldAuths.getString("njpm")))) {
					head[1][i * perFieldNum + index] = new EasyUIDatagridHead(kmdm + "_gradeRank", "年名", "center", 50,
							1, 1, true);
				}

				i++;
			}
			head[0][i] = new EasyUIDatagridHead(null, "总分", "center", 0, 1, perFieldNum, true);
			int index = 1;
			if (!"0".equals(fieldAuths.getString("cj"))) {
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("totalScore", "原始分数", "center", 60, 1, 1, true);

				head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalBzScore", "标准分数", "center", 60, 1, 1,
						true);
				index++;
			} else {

				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("totalBzScore", "标准分数", "center", 60, 1, 1, true);
			}
			if (!"0".equals(fieldAuths.getString("bjpm"))) {
				head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1,
						1, true);
				index++;
			}
			if (!"0".equals(fieldAuths.getString("njpm"))) {
				head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1,
						1, true);
			}

			// 把科目成绩存放在Map
			Map<String, JSONObject> bjcjCjMap = new HashMap<String, JSONObject>();

			Map<String, JSONObject> bjcjCjMap1 = new HashMap<String, JSONObject>();

			for (JSONObject obCj : bjcjCj) {// 循环把数据放置到Map
				String kmdm = obCj.getString("kmdm");
				String totalBzScore = obCj.getString("totalBzScore");
				String studentId = obCj.getString("xh");
				if (!bjcjCjMap.containsKey(kmdm + studentId)) {
					if (studentInfos.containsKey(Long.valueOf(studentId))) {
						bjcjCjMap.put(kmdm + studentId, obCj);
					}
				}
				if (!bjcjCjMap1.containsKey(totalBzScore + studentId)) {
					if (studentInfos.containsKey(Long.valueOf(studentId))) {
						bjcjCjMap1.put(totalBzScore + studentId, obCj);
					}
				}
			}

			for (JSONObject obXs : backDataList) {
				String xh = obXs.getString("studentId");
				obXs.put("totalScore", 0);
				obXs.put("totalBzScore", 0);
				obXs.put("totalScoreClassRank", 0);
				obXs.put("totalScoreGradeRank", 0);
				for (JSONObject obMk : bjcjMk) {
					String kmdm = obMk.getString("kmdm");
					JSONObject obCj = bjcjCjMap.get(kmdm + xh);
					if (obCj != null) {
						if (obCj.get("bzzcj") == null) {
							obXs.put(kmdm + "_bzscore", 0);
						} else {
							obXs.put(kmdm + "_bzscore", obCj.get("bzzcj"));
						}
						if (obCj.get("cj") == null) {
							obXs.put(kmdm + "_score", 0);
						} else {
							obXs.put(kmdm + "_score", obCj.get("cj"));
						}
						if (obCj.get("bzbjpm") == null) {
							obXs.put(kmdm + "_classRank", 0);
						} else {
							obXs.put(kmdm + "_classRank", obCj.get("bzbjpm"));
						}
						if (obCj.get("bznjpm") == null) {
							obXs.put(kmdm + "_gradeRank", 0);
						} else {
							obXs.put(kmdm + "_gradeRank", obCj.get("bznjpm"));
						}
					}

					// 学生总分
					obCj = bjcjCjMap.get("totalScore" + xh);
					// obCj1 = bjcjCjMap1.get("totalBzScore" + xh);

					if (obCj != null) {
						if (obCj.get("cj") == null) {
							obXs.put("totalScore", 0);
						} else {
							obXs.put("totalScore", obCj.get("cj"));
						}
						if (obCj.get("bzzcj") == null) {
							obXs.put("totalBzScore", 0);
						} else {
							obXs.put("totalBzScore", obCj.get("bzzcj"));
						}
						if (obCj.get("bzbjpm") == null) {
							obXs.put("totalScoreClassRank", 0);
						} else {
							obXs.put("totalScoreClassRank", obCj.get("bzbjpm"));
						}

						if (obCj.get("bznjpm") == null) {
							obXs.put("totalScoreGradeRank", 0);
						} else {
							obXs.put("totalScoreGradeRank", obCj.get("bznjpm"));
						}
					}
				}
			}
		}

		Collections.sort(backDataList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("classsName");
				String className2 = arg2.getString("classsName");
				return className1.compareTo(className2);
			}
		});

		JSONObject data = new JSONObject();
		data.put("total", backDataList.size());
		data.put("rows", backDataList == null ? "" : backDataList);
		data.put("columns", head == null ? "" : head);
		return data;
	}

	/****
	 * 返回属于某一个报表类型的所有报表信息集合
	 * 
	 * @param reportType
	 *            报表类型
	 * @param reportTypeName
	 *            报表名称
	 * @param curRole
	 *            当前角色
	 * @param reportConfigs
	 *            报表配置数据记录列表
	 * @return
	 */
	private JSONObject extractPointTypeReportTypeJson(String reportType, String reportTypeName, String curRole,
			List<JSONObject> reportConfigs) {
		// 1.定义返回的json的对象
		JSONObject data = new JSONObject();
		data.put("reportTypeNo", reportType);
		data.put("reportTypeName", reportTypeName);

		List<JSONObject> reprotArray = new ArrayList<JSONObject>();
		// 2.查找指定的报表类型的数据
		for (JSONObject item : reportConfigs) {// 循环所有数据记录，把数据记录分类存储
			String type = item.getString("reportType");
			String flag = item.getString("flag");
			if (reportType != null && reportType.equals(type) && "1".equals(flag)) {
				// 每个报表信息json
				JSONObject report = new JSONObject();
				report.put("reportNo", item.getString("reportNo"));// 报表代码
				report.put("reportName", item.getString("reportName"));// 报表名称
				report.put("orderId", item.getString("orderId"));
				String role = item.getString("role");
				if (role == null || "".equals(role) || curRole.equals("*")) {// 配置中角色为空的时候表示所有用户可以查看
					reprotArray.add(report);
				} else {
					String[] roles = curRole.split(",");
					for (String rol : roles) {
						if (role.contains(rol)) {
							reprotArray.add(report);
							break;
						}
					}
				}
			}
		}

		// 3.返回结果
		if (reprotArray.size() > 0) {// 如果有属于该类型的报表
			data.put("report", reprotArray);
			return data;
		} else {
			return null;
		}
	}

	@Override
	public JSONObject getAllPreviousLevelCompareList(School school, JSONObject params) {
		String xnxq = params.getString("xnxq");
		String xn = xnxq.substring(0, xnxq.length() - 1);
		String nj = params.getString("nj");
		String kmdmStr = params.getString("kmdmStr");
		List<String> kmdmList = StringUtil.convertToListFromStr(kmdmStr, ",", String.class);
		int termInfoRange = params.getIntValue("termInfoRange");
		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");

		T_GradeLevel gl = T_GradeLevel.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(nj, xn)));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, xnxq);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级数据，请联系管理员！");
		}
		if (grade.getClassIds() == null) {
			throw new CommonRunException(-1, "年级下无班级信息，请联系管理员！");
		}
		List<String> bhList = new ArrayList<String>();
		for (Long classId : grade.getClassIds()) {
			bhList.add(String.valueOf(classId));
		}

		List<String> termInfoIds = new ArrayList<String>();
		switch (termInfoRange) {
		case 0: // 本学期
			termInfoIds.add(xnxq);
			break;
		case 1: // 本学年
			termInfoIds.add(xn + "1");
			termInfoIds.add(xn + "2");
			break;
		case 2: // 历年
			termInfoIds.addAll(TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, xnxq));
			break;
		default:
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}

		Map<String, List<JSONObject>> cmap = new HashMap<String, List<JSONObject>>();

		if (CollectionUtils.isNotEmpty(termInfoIds)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("xxdm", xxdm);
			map.put("kmdmList", kmdmList);
			map.put("nj", nj);
			map.put("bhList", bhList);
			for (String termInfoId : termInfoIds) {
				map.put("xnxq", termInfoId);
				List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(xnxq, map);
				for (DegreeInfo degreeInfo : degreeInfoList) {
					Integer autoIncr = degreeInfo.getAutoIncr();
					map.put("kslc", degreeInfo.getKslcdm());
					List<JSONObject> classScore = reportDao.getClassScores(termInfoId, autoIncr, map);
					for (JSONObject obj : classScore) {
						obj.put("examName", degreeInfo.getKslcmc());
						obj.put("cdate", degreeInfo.getCdate());
						obj.put("examId", degreeInfo.getKslcdm());
						obj.put("xnxq", degreeInfo.getXnxq());

						if (!cmap.containsKey(termInfoId)) {
							cmap.put(termInfoId, new ArrayList<JSONObject>());
						}
						cmap.get(termInfoId).add(obj);
					}
				}
			}
		}

		// 2.获取数据
		return produceClassScoreData(school, cmap, params, kmdmList);
	}

	/****
	 * 历次等第成绩对比表
	 * 
	 * @param param
	 * @return
	 */
	public JSONObject produceClassScoreData(School s, Map<String, List<JSONObject>> cmap, JSONObject params,
			List<String> kmdmList) {
		String xxdm = params.getString("xxdm");
		Long schoolId = s.getId();
		// 1.获取班级成绩数据
		List<JSONObject> newClassScoreData = new ArrayList<JSONObject>();// 新组织出来的成绩对比数据。
		List<JSONObject> examineList = new ArrayList<JSONObject>();// 考试信息列表
		// 科目信息，并且存放到Map
		String subjectIds = params.getString("kmdmStr").replace("totalScore", "0");// 科目编号集

		List<Long> subjectIdsList = StringUtil.convertToListFromStr(subjectIds, ",", Long.class);// 科目编号列表
		EasyUIDatagridHead[][] head = null;
		for (Map.Entry<String, List<JSONObject>> entry : cmap.entrySet()) {
			String xnxq = entry.getKey();
			List<JSONObject> classScore = entry.getValue();

			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(s.getId(), subjectIdsList, xnxq);
			Map<Long, LessonInfo> lessonInfoMap = new HashMap<Long, LessonInfo>();// 存放科目信息map
			if (CollectionUtils.isNotEmpty(lessonInfos)) {
				for (LessonInfo lessonInfo : lessonInfos) {
					if (lessonInfo == null)
						continue;
					long lessonId = lessonInfo.getId();
					if (!lessonInfoMap.containsKey(lessonId)) {
						lessonInfoMap.put(lessonId, lessonInfo);
					}
				}
			}

			List<Long> classIds = new ArrayList<Long>();
			for (JSONObject obj : classScore) {
				classIds.add(obj.getLongValue("classId"));
			}

			Map<String, JSONObject> teacherMap = new HashMap<String, JSONObject>();// 存放教师信息Map
			Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>();// 存放班级信息Map
			Map<Long, String> deanteacherMap = new HashMap<Long, String>();// 存放班主任老师列表信息Map

			// 班级信息,并且存放到Map
			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
			if (CollectionUtils.isNotEmpty(classrooms)) {
				List<Long> teacherIds = new ArrayList<Long>();
				List<Long> deanIds = new ArrayList<Long>();
				for (Classroom classroom : classrooms) {
					if (classroom == null) {
						continue;
					}

					long classId = classroom.getId();

					if (!classroomMap.containsKey(classId)) {
						classroomMap.put(classId, classroom);
					}

					teacherIds.add(classroom.getDeanAccountId());
					deanIds.add(classroom.getDeanAccountId());

					List<AccountLesson> accLessons = classroom.getAccountLessons();
					if (CollectionUtils.isNotEmpty(accLessons)) {
						for (AccountLesson accLesson : accLessons) {
							teacherIds.add(accLesson.getAccountId());

							JSONObject json = new JSONObject();
							json.put("teacherId", accLesson.getAccountId());
							teacherMap.put(String.valueOf(classId) + accLesson.getLessonId(), json);
						}
					}
				}

				Map<Long, Account> accId2Obj = new HashMap<Long, Account>();
				List<Account> accList = commonDataService.getAccountBatch(schoolId, teacherIds, xnxq);
				if (CollectionUtils.isNotEmpty(accList)) {
					for (Account acc : accList) {
						accId2Obj.put(acc.getId(), acc);
					}
				}

				for (Long accId : deanIds) {
					deanteacherMap.put(accId, "");
					if (accId2Obj.containsKey(accId)) {
						deanteacherMap.put(accId, accId2Obj.get(accId).getName());
					}
				}

				for (Map.Entry<String, JSONObject> entry1 : teacherMap.entrySet()) {
					JSONObject json = entry1.getValue();
					Long teacherId = json.getLong("teacherId");
					json.put("teacherName", "");
					if (accId2Obj.containsKey(teacherId)) {
						json.put("teacherName", accId2Obj.get(teacherId).getName());
					}
				}
			}

			// 2.组织表头，和新格式的数据列表
			if (CollectionUtils.isNotEmpty(classScore)) {
				// 循环把所有考试的信息放到单独的集合中。
				List<Object> isExistExamList = new ArrayList<Object>();
				for (JSONObject cscore : classScore) {
					String subjectId = cscore.getString("subjectId");
					if (kmdmList != null && !kmdmList.contains("totalScore") && "zf".equals(subjectId)) {
						continue;
					}

					JSONObject json = new JSONObject();
					json.put("examId", cscore.get("examId"));
					json.put("examName", cscore.get("examName"));
					json.put("cdate", cscore.get("cdate"));
					if (!isExistExamList.contains(cscore.get("examId"))) {
						examineList.add(json);
						isExistExamList.add(cscore.get("examId"));
					}
				}

				Collections.sort(examineList, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject arg0, JSONObject arg1) {
						Date cdate0 = arg0.getDate("cdate");
						Date cdate1 = arg1.getDate("cdate");
						return -cdate0.compareTo(cdate1);
					}
				});

				if (CollectionUtils.isNotEmpty(examineList)) {
					int i = 1;
					for (Map<String, Object> examine : examineList) {
						examine.put("shortName", "T" + i);
						++i;
					}
				}

				// 表头组织
				Map<String, Integer> fieldAuths = getEveryTimeFieldShowInfo(xxdm);// 字段显示权限信息

				head = new EasyUIDatagridHead[2][];
				head[0] = new EasyUIDatagridHead[fieldAuths.get("oneRowColumns")];
				head[1] = new EasyUIDatagridHead[examineList.size() * fieldAuths.get("twoRowColumns")];

				int index = 0;// 动态浮动值
				if (!fieldAuths.containsKey("average")) {
					head[0][index] = new EasyUIDatagridHead(null, "平均分", "center", 50, 1,
							examineList.size() * fieldAuths.get("averageColumns"), true);
					index++;
				}
				if (!fieldAuths.containsKey("A")) {
					head[0][index] = new EasyUIDatagridHead(null, "A等率", "center", 50, 1,
							examineList.size() * fieldAuths.get("ALevelColumns"), true);
					index++;
				}
				if (!fieldAuths.containsKey("pass")) {
					head[0][index] = new EasyUIDatagridHead(null, "合格率", "center", 50, 1,
							examineList.size() * fieldAuths.get("passColumns"), true);
					index++;
				}
				if (!fieldAuths.containsKey("excellent")) {
					head[0][index] = new EasyUIDatagridHead(null, "优秀率", "center", 50, 1,
							examineList.size() * fieldAuths.get("excellentColumns"), true);
					index++;
				}
				if (!fieldAuths.containsKey("3ALevelNum")) {
					head[0][index] = new EasyUIDatagridHead(null, "3A人数(语数外)", "center", 100, 1, examineList.size(),
							true);
					index++;
				}
				if (!fieldAuths.containsKey("3AAnd2ABLevelNum")) {
					head[0][index] = new EasyUIDatagridHead(null, "3A+2A1B人数(语数外)", "center", 150, 1,
							examineList.size(), true);
				}

				int h = 0;
				// 平均分
				int scope = 0;// 索引浮动范围
				for (Map<String, Object> examInfo : examineList) {
					scope = 0;
					if (!fieldAuths.containsKey("averageScore")) {
						head[1][h * fieldAuths.get("averageColumns") + scope] = new EasyUIDatagridHead(
								examInfo.get("examId") + "_averageScore", examInfo.get("shortName") + "平均分", "center",
								80, 1, 1, true);
						scope++;
					}
					if (!fieldAuths.containsKey("averageScoreRank")) {
						head[1][h * fieldAuths.get("averageColumns") + scope] = new EasyUIDatagridHead(
								examInfo.get("examId") + "_averageScoreRank", examInfo.get("shortName") + "排名",
								"center", 60, 1, 1, true);
					}
					h++;
				}
				// A等率
				h = 0;
				for (Map<String, Object> examInfo : examineList) {
					scope = 0;
					if (!fieldAuths.containsKey("ALevelRate")) {
						head[1][examineList.size() * fieldAuths.get("averageColumns")
								+ h * fieldAuths.get("ALevelColumns") + scope] = new EasyUIDatagridHead(
										examInfo.get("examId") + "_ALevelRate", examInfo.get("shortName") + "A等率",
										"center", 80, 1, 1, true);
						scope++;
					}
					if (!fieldAuths.containsKey("ALevelRateRank")) {
						head[1][examineList.size() * fieldAuths.get("averageColumns")
								+ h * fieldAuths.get("ALevelColumns") + scope] = new EasyUIDatagridHead(
										examInfo.get("examId") + "_ALevelRateRank", examInfo.get("shortName") + "A等率排名",
										"center", 100, 1, 1, true);
					}
					h++;
				}
				// 合格率
				h = 0;
				int beforeTwoColumns = fieldAuths.get("averageColumns") + fieldAuths.get("ALevelColumns");
				for (Map<String, Object> examInfo : examineList) {
					scope = 0;
					if (!fieldAuths.containsKey("passRate")) {
						head[1][examineList.size() * beforeTwoColumns + h * fieldAuths.get("passColumns")
								+ scope] = new EasyUIDatagridHead(examInfo.get("examId") + "_passRate",
										examInfo.get("shortName") + "合格率", "center", 80, 1, 1, true);
						scope++;
					}
					if (!fieldAuths.containsKey("passRateRank")) {
						head[1][(examineList.size() * beforeTwoColumns + h * fieldAuths.get("passColumns"))
								+ scope] = new EasyUIDatagridHead(examInfo.get("examId") + "_passRateRank",
										examInfo.get("shortName") + "排名", "center", 55, 1, 1, true);
					}
					h++;
				}
				// 优秀率
				h = 0;
				int beforeThreeColumns = fieldAuths.get("averageColumns") + fieldAuths.get("ALevelColumns")
						+ fieldAuths.get("passColumns");
				for (Map<String, Object> examInfo : examineList) {
					scope = 0;
					if (!fieldAuths.containsKey("excellentRate")) {
						head[1][examineList.size() * beforeThreeColumns + h * fieldAuths.get("excellentColumns")
								+ scope] = new EasyUIDatagridHead(examInfo.get("examId") + "_excellentRate",
										examInfo.get("shortName") + "优秀率", "center", 80, 1, 1, true);
						scope++;
					}
					if (!fieldAuths.containsKey("excellentRateRank")) {
						head[1][examineList.size() * beforeThreeColumns + h * fieldAuths.get("excellentColumns")
								+ scope] = new EasyUIDatagridHead(examInfo.get("examId") + "_excellentRateRank",
										examInfo.get("shortName") + "排名", "center", 55, 1, 1, true);
					}
					h++;
				}

				// 3A人数
				h = 0;
				int beforeFourColumns = fieldAuths.get("averageColumns") + fieldAuths.get("ALevelColumns")
						+ fieldAuths.get("passColumns") + fieldAuths.get("excellentColumns");
				for (Map<String, Object> examInfo : examineList) {
					if (!fieldAuths.containsKey("3ALevelNum")) {
						head[1][examineList.size() * beforeFourColumns + h] = new EasyUIDatagridHead(
								examInfo.get("examId") + "_3ALevelNum", examInfo.get("shortName") + "", "center", 100,
								1, 1, true);
					}
					h++;
				}
				// 3A+2A1B人数
				h = 0;
				int beforeFiveColumns = fieldAuths.get("averageColumns") + fieldAuths.get("ALevelColumns")
						+ fieldAuths.get("passColumns") + fieldAuths.get("excellentColumns")
						+ fieldAuths.get("L3AColumns");
				for (Map<String, Object> examInfo : examineList) {
					if (!fieldAuths.containsKey("3AAnd2ABLevelNum")) {
						head[1][examineList.size() * beforeFiveColumns + h] = new EasyUIDatagridHead(
								examInfo.get("examId") + "_3AAnd2ABLevelNum", examInfo.get("shortName") + "", "center",
								150, 1, 1, true);
					}
					h++;
				}

				// 按照表格要求组织新格式的数据列表
				List<String> classIdList = new ArrayList<String>();
				List<String> examIdList = new ArrayList<String>();

				Map<String, JSONObject> classScoreMap = new HashMap<String, JSONObject>();// 存放班级成绩Map
				for (JSONObject score : classScore) {
					String classId = score.getString("classId");
					String subjectId = score.getString("subjectId");
					String examId = score.getString("examId");

					String key = classId + subjectId + examId;
					if (!classScoreMap.containsKey(key)) {
						classScoreMap.put(key, score);
					}

					if (!classIdList.contains(classId)) {
						classIdList.add(classId);
					}

					if (!examIdList.contains(examId)) {
						examIdList.add(examId);
					}
				}

				// 读取等级类型数据记录
				List<JSONObject> leveStudentList = getAssignLevelStudent(xxdm, xnxq, examIdList, classIdList);

				Map<String, Integer> levelStudentMap = new HashMap<String, Integer>();// 存放等第数据Map
				if (leveStudentList != null) {
					for (JSONObject levelStudent : leveStudentList) {
						String levelType = levelStudent.getString("levelType");
						String classId = levelStudent.getString("classId");
						String examId = levelStudent.getString("examId");
						int studentNum = levelStudent.getIntValue("studentNum");

						if (!levelStudentMap.containsKey(classId + examId + levelType)) {
							levelStudentMap.put(classId + examId + levelType, studentNum);
						}
					}
				}

				long deanID = 0;// 班主任编号
				List<String> existsList = new ArrayList<String>();// 存放科目编号和班级编号，用来判断此条数据是否已经存在数据记录中
				for (JSONObject score : classScore) {
					JSONObject json = new JSONObject();

					String subjectId = score.getString("subjectId");
					long classId = score.getLongValue("classId");
					LessonInfo lessoninfo = null;

					// 判断当前科目和班号的记录是否已经放到数据集合中
					if (existsList.contains(classId + "" + subjectId)
							|| "zf".equals(subjectId) && kmdmList.size() == 1 && !kmdmList.contains("totalScore")) {
						continue;
					} else {
						existsList.add(classId + "" + subjectId);
					}

					// 设置班级名称
					Classroom classroom = classroomMap.get(classId);
					if (classroom == null) {
						continue;
					}

					json.put("className", classroom.getClassName());
					if ("zf".equals(subjectId)) {// 如果登录总分的时候，获取班主任账号
						deanID = classroom.getDeanAccountId();
					}

					// 设置科目名称
					json.put("subjectId", subjectId);
					if ("zf".equals(subjectId)) {// 总分时候，科目名称设置
						json.put("subjectName", "总分");
					} else {
						if (lessonInfoMap != null)
							lessoninfo = lessonInfoMap.get(Long.valueOf(subjectId));
						if (lessoninfo != null) {
							json.put("subjectName", lessoninfo.getName());
						}
						json.put("classId", classId);
					}

					// 设置老师信息
					if ("zf".equals(subjectId)) {// 如果是总分，设置老师为班主任
						json.put("teacherId", deanID);
						json.put("teacherName", deanteacherMap.get(deanID));
					} else {
						JSONObject teacher = teacherMap.get(classId + "" + subjectId);
						if (teacher != null) {
							json.put("teacherId", teacher.get("teacherId"));
							json.put("teacherName", teacher.get("teacherName"));
						}
					}

					json.putAll(getClassScoreItemData(examineList, levelStudentMap, classScoreMap,
							String.valueOf(subjectId), String.valueOf(classId)));

					newClassScoreData.add(json);
				}

			}
		}

		// 3.报表说明
		String reportMsg = "";
		for (Map<String, Object> examine : examineList) {
			reportMsg = reportMsg + examine.get("shortName") + "是" + examine.get("examName") + "; ";
		}

		Collections.sort(newClassScoreData, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String subjectId1 = o1.getString("subjectId");
				String subjectId2 = o2.getString("subjectId");
				if("zf".equals(subjectId1) && !"zf".equals(subjectId2)) {
					return 1;
				}
				if(!"zf".equals(subjectId1) && "zf".equals(subjectId2)) {
					return -1;
				}
				if(!"zf".equals(subjectId1) && !"zf".equals(subjectId2)) {
					int result = Long.compare(Long.valueOf(subjectId1), Long.valueOf(subjectId2));
					if(result != 0) {
						return result;
					}
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

		JSONObject data = new JSONObject();
		data.put("total", newClassScoreData == null ? 0 : newClassScoreData.size());
		data.put("rows", newClassScoreData);
		data.put("columns", head);
		data.put("reportMsg", reportMsg);
		return data;
	}

	/***
	 * 历次等第成绩表字段显示情况
	 * 
	 * @return
	 */
	private Map<String, Integer> getEveryTimeFieldShowInfo(String schoolId) {
		// 获取报表显示列数，判断需要显示多少列
		Map<String, Integer> map = new HashMap<String, Integer>();

		String fieldAuthConfig = (String) configService.getReportFieldAuths(schoolId, "011").get("config");// 获取报表字段显示权限
		JSONObject fieldAuths = JSON.parseObject(fieldAuthConfig);

		int oneRowColumns = 6;// 第一行数据列数
		int twoRowColumns = 0;// 第二行数据列数
		int averageColumns = 2;// 平均分的列数
		int ALevelColumns = 2;// A等率列数
		int excellentColumns = 2;// 优秀率的列数
		int passColumns = 2;// 合格率列数
		int L3AColumns = 1;// 3A人数列数
		int L3A2A1BColumns = 1;// 3A和2A1B人数列数

		if (fieldAuths != null) {

			// 平均分
			JSONObject averageScore = (JSONObject) fieldAuths.get("pjf");
			if (averageScore != null) {
				String averageScoreValue = averageScore.get("pjfz").toString();
				String rankValue = averageScore.get("pm").toString();
				if ("0".equals(averageScoreValue)) {
					map.put("averageScore", 0);
					averageColumns = averageColumns - 1;
				}

				if ("0".equals(rankValue)) {
					map.put("averageScoreRank", 0);
					averageColumns = averageColumns - 1;
				}

				if ("0".equals(averageScoreValue) && "0".equals(rankValue)) {
					map.put("average", 0);
					oneRowColumns = oneRowColumns - 1;
				}
			}

			// 优秀率
			JSONObject excellent = (JSONObject) fieldAuths.get("yx");
			if (excellent != null) {
				String excellentRate = excellent.getString("yxl");
				String excellentRank = excellent.getString("yxlpm");

				if ("0".equals(excellentRate)) {
					map.put("excellentRate", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRank)) {
					map.put("excellentRateRank", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRate) && "0".equals(excellentRank)) {
					map.put("excellent", 0);
					oneRowColumns = oneRowColumns - 1;
				}
			}

			// 合格率
			JSONObject pass = (JSONObject) fieldAuths.get("hg");
			if (pass != null) {
				String passRate = pass.getString("hgl");
				String passRateRank = pass.getString("hglpm");

				if ("0".equals(passRate)) {
					map.put("passRate", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRateRank)) {
					map.put("passRateRank", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRate) && "0".equals(passRateRank)) {
					map.put("pass", 0);
					oneRowColumns = oneRowColumns - 1;
				}
			}

			// A等
			JSONObject A = (JSONObject) fieldAuths.get("A");
			if (A != null) {
				String ALevelRate = A.getString("Adl");
				String ALevelRank = A.getString("Adlpm");

				if ("0".equals(ALevelRate)) {
					map.put("ALevelRate", 0);
					ALevelColumns = ALevelColumns - 1;
				}

				if ("0".equals(ALevelRank)) {
					map.put("ALevelRateRank", 0);
					ALevelColumns = ALevelColumns - 1;
				}

				if ("0".equals(ALevelRate) && "0".equals(ALevelRank)) {
					map.put("A", 0);
					oneRowColumns = oneRowColumns - 1;
				}
			}

			// 3A等
			String L3A = fieldAuths.get("3A").toString();
			if ("0".equals(L3A)) {
				map.put("3ALevelNum", 0);
				L3AColumns = L3AColumns - 1;
				oneRowColumns = oneRowColumns - 1;
			}

			// 3A2A1B等
			String L3A2A1B = fieldAuths.get("3AOr2A1B").toString();
			if ("0".equals(L3A2A1B)) {
				map.put("3AAnd2ABLevelNum", 0);
				L3A2A1BColumns = L3A2A1BColumns - 1;
				oneRowColumns = oneRowColumns - 1;
			}

		}

		if (averageColumns < 0)
			averageColumns = 0;
		if (excellentColumns < 0)
			excellentColumns = 0;
		if (passColumns < 0)
			passColumns = 0;
		if (ALevelColumns < 0)
			ALevelColumns = 0;
		if (L3AColumns < 0)
			L3AColumns = 0;
		if (L3A2A1BColumns < 0)
			L3A2A1BColumns = 0;
		if (oneRowColumns < 0)
			oneRowColumns = 0;

		twoRowColumns = averageColumns + excellentColumns + passColumns + ALevelColumns + L3AColumns + L3A2A1BColumns;

		map.put("twoRowColumns", twoRowColumns);
		map.put("averageColumns", averageColumns);
		map.put("excellentColumns", excellentColumns);
		map.put("passColumns", passColumns);
		map.put("ALevelColumns", ALevelColumns);
		map.put("L3AColumns", L3AColumns);
		map.put("L3A2A1BColumns", L3A2A1BColumns);
		map.put("oneRowColumns", oneRowColumns);

		// 2.返回数据
		return map;
	}

	/****
	 * 获取班级成绩每行数据
	 * 
	 * @param examineList
	 * @param classScore
	 * @param subjectId
	 * @param classId
	 * @return
	 */
	private Map<String, Object> getClassScoreItemData(List<JSONObject> examineList,
			Map<String, Integer> levelStudentMap, Map<String, JSONObject> classScoreMap, String subjectId,
			String classId) {

		Map<String, Object> map = new HashMap<String, Object>();

		if (classId == null)
			return map;

		for (Map<String, Object> examineInfo : examineList) {
			String examId = examineInfo.get("examId") + "";

			if (examId == null || "".equals(examId))
				continue;

			Map<String, Object> score = null;
			if (classScoreMap != null) {
				score = classScoreMap.get(classId + subjectId + examId);
			}

			if (score != null) {
				map.put(examId + "_averageScore", score.get("averageScore"));
				map.put(examId + "_averageScoreRank", score.get("averageScoreRank"));
				map.put(examId + "_excellentRate", score.get("excellentRate"));
				map.put(examId + "_excellentRateRank", score.get("excellentRateRank"));
				map.put(examId + "_passRate", score.get("passRate"));
				map.put(examId + "_passRateRank", score.get("passRateRank"));
				if (!subjectId.equals("zf")) {
					map.put(examId + "_ALevelRate",
							score.get("ALevelRate") == null ? "" : score.get("ALevelRate") + "%");
					map.put(examId + "_ALevelRateRank", score.get("ALevelRateRank"));
				}

				if (subjectId.equals("zf")) {
					int threeALevelNum = 0;
					if (levelStudentMap != null && !levelStudentMap.isEmpty()) {
						if (levelStudentMap.containsKey(classId + examId + "3A"))
							threeALevelNum = levelStudentMap.get(classId + examId + "3A");
					}

					map.put(examId + "_3ALevelNum", threeALevelNum);

					int threeTwoALevelNum = 0;
					if (levelStudentMap != null && !levelStudentMap.isEmpty()) {
						if (levelStudentMap.containsKey(classId + examId + "2A1B"))
							threeTwoALevelNum = levelStudentMap.get(classId + examId + "2A1B");
					}

					map.put(examId + "_3AAnd2ABLevelNum", threeTwoALevelNum + threeALevelNum);
				}
			}

		}

		return map;
	}

	/****
	 * 获取指定等级级别的人数
	 * 
	 * @param schoolId
	 * @param termInfoId
	 * @param examId
	 * @param classId
	 * @param levelType
	 * @return
	 */
	private List<JSONObject> getAssignLevelStudent(String schoolId, String termInfoId, List<String> examIdList,
			List<String> classId) {

		List<JSONObject> result = new ArrayList<JSONObject>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xxdm", schoolId);
		map.put("xnxq", termInfoId);
		map.put("kslcdmList", examIdList);

		List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, map);
		map.remove("kslcdmList");

		map.put("bhList", classId);
		for (DegreeInfo degreeInfo : degreeInfoList) {
			map.put("kslc", degreeInfo.getKslcdm());
			Integer autoIncr = degreeInfo.getAutoIncr();
			result.addAll(reportDao.getAssignLevelStudentNum(termInfoId, autoIncr, map));
		}

		return result;
	}

	@Override
	public JSONObject getCompetiteStuAnalysisList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");

		String kmdmStr = params.getString("kmdmStr");
		params.remove("kmdmStr");
		List<String> kmdmList = StringUtil.convertToListFromStr(kmdmStr, ",", String.class);
		params.put("kmdmList", kmdmList);

		Map<String, String> subjectFieldAliasName = new HashMap<String, String>();
		for (String id : kmdmList) {
			if (!subjectFieldAliasName.containsKey(id) && !"totalScore".equals(id)) {
				subjectFieldAliasName.put(id, id + "score");
			}
		}
		params.put("subjectFieldAliasName", subjectFieldAliasName);// 科目字段别名

		// 2.组织竞赛学生统计分析结果报表表头格式，并且按照接口说明组织返回的数据格式
		List<JSONObject> stuStatistics = reportDao.getCompetitionStuStatistics(xnxq, autoIncr, params);// 竞赛学生统计分析数据
		EasyUIDatagridHead[][] head = null;
		if (stuStatistics.size() > 0) {
			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[5];
			head[1] = new EasyUIDatagridHead[8];

			head[0][0] = new EasyUIDatagridHead("subjectName", "科目组名称", "center", 100, 2, 1, true);
			head[0][1] = new EasyUIDatagridHead("referenceNum", "参考人数", "center", 100, 2, 1, true);
			head[0][2] = new EasyUIDatagridHead(null, "平均分比较", "center", 50, 1, 2, true);
			head[0][3] = new EasyUIDatagridHead(null, "优秀率比较", "center", 50, 1, 3, true);
			head[0][4] = new EasyUIDatagridHead(null, "合格率比较", "center", 50, 1, 3, true);

			head[1][0] = new EasyUIDatagridHead("subjectAverageScore", "相应科目平均分", "center", 100, 1, 1, true);
			head[1][1] = new EasyUIDatagridHead("gradeTotalAverageScore", "年级总平均分", "center", 100, 1, 1, true);
			head[1][2] = new EasyUIDatagridHead("subjectExcellentNum", "相应科目优秀人数", "center", 100, 1, 1, true);
			head[1][3] = new EasyUIDatagridHead("subjectExcellentRate", "相应科目优秀率", "center", 100, 1, 1, true);
			head[1][4] = new EasyUIDatagridHead("gradeTotalExcellentRate", "年级总优秀率", "center", 100, 1, 1, true);
			head[1][5] = new EasyUIDatagridHead("subjectPassNum", "相应科目合格人数", "center", 100, 1, 1, true);
			head[1][6] = new EasyUIDatagridHead("subjectPassRate", "相应科目合格率", "center", 100, 1, 1, true);
			head[1][7] = new EasyUIDatagridHead("gradeTotalPassRate", "年级总合格率", "center", 100, 1, 1, true);
		}

		JSONObject statisResult = new JSONObject();
		statisResult.put("total", stuStatistics.size());
		statisResult.put("rows", stuStatistics);
		statisResult.put("columns", head);

		if (stuStatistics == null || stuStatistics.size() <= 0) {
			JSONObject result = new JSONObject();
			result.put("statisResult", statisResult);
			result.put("scoreDetail", "");
			result.put("declare", "学校竞赛组的学生在“" + degreeInfo.getKslcmc() + "”中成绩汇总统计和明细查询");
			return result;
		}

		// 3.组织竞赛学生成绩表头格式，并且按照接口说明组织返回的数据格式

		JSONObject scoreDetail = new JSONObject();
		List<JSONObject> competitionStu = reportDao.getCompetitionStu(xnxq, autoIncr, params);// 竞赛学生列表。
		if (CollectionUtils.isNotEmpty(competitionStu)) {
			List<Long> examSubjectIds = reportDao.getStudentAllExamSubjectList(xnxq, autoIncr, params);// 学生所参加的考试的科目列表

			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(schoolId, examSubjectIds, xnxq);
			// 排序
			Collections.sort(lessonInfos, new Comparator<LessonInfo>() {
				@Override
				public int compare(LessonInfo o1, LessonInfo o2) {
					return (int) ((int) o1.getId() - o2.getId());
				}
			});

			Map<String, String> lessonInfoMap = new HashMap<String, String>();// 存放课程信息Map
			if (CollectionUtils.isNotEmpty(lessonInfos)) {
				for (LessonInfo lessonInfo : lessonInfos) {// 循环账户，把账户信息存在Map中
					if (lessonInfo == null) {
						continue;
					}
					String subjId = String.valueOf(lessonInfo.getId());
					if (!lessonInfoMap.containsKey(subjId)) {
						lessonInfoMap.put(subjId, lessonInfo.getName());
					}
				}
			}

			List<Long> studentIds = new ArrayList<Long>();
			List<String> studIds = new ArrayList<String>();
			if (competitionStu != null) {
				for (JSONObject competition : competitionStu) {
					if (competition == null) {
						continue;
					}
					long studId = competition.getLongValue("studentId");
					if (!studentIds.contains(studId)) {
						studentIds.add(studId);
						studIds.add(String.valueOf(studId));
					}
				}
			}

			List<Account> accounts = this.commonDataService.getAccountBatch(schoolId, studentIds, xnxq);// 学生账户信息
			studentIds = null;
			Map<String, User> users = new HashMap<String, User>();// 存放用户信息Map
			if (CollectionUtils.isNotEmpty(accounts)) {
				for (Account acc : accounts) {// 循环账户，把账户信息存在Map中
					if (acc == null || acc.getUsers() == null) {
						continue;
					}
					for (User user : acc.getUsers()) {
						if (user == null || user.getUserPart() == null) {
							continue;
						}
						if (T_Role.Student.equals(user.getUserPart().getRole())) {
							users.put(String.valueOf(acc.getId()), user);
						}
					}
				}
			}

			// 获取竞赛学生的单科成绩已经总分成绩
			params.put("studIdList", studIds);
			List<JSONObject> competitionStuExamScoreList = reportDao.getCompetitionStuExamScoreList(xnxq, autoIncr,
					params);

			Map<String, JSONObject> competitionStuExamScoreMap = new HashMap<String, JSONObject>();
			for (JSONObject json : competitionStuExamScoreList) {
				if (json == null) {
					continue;
				}
				String subjId = json.getString("subjectId");
				String studId = json.getString("studentId");
				competitionStuExamScoreMap.put(subjId + studId, json);
			}

			if (competitionStu != null) {
				for (JSONObject competition : competitionStu) {
					if (competition == null) {
						continue;
					}
					String studentId = competition.getString("studentId");
					String tempsubjectId = competition.getString("subjectId");

					competition.put("subjectName", StringUtil.transformString(lessonInfoMap.get(tempsubjectId)) + "组");
					User user = users.get(studentId);
					String studentName = "";
					String studentNo = "";
					if (user != null) {
						studentName = user.getAccountPart() == null ? "" : user.getAccountPart().getName();
						studentNo = user.getStudentPart() == null ? "" : user.getStudentPart().getSchoolNumber();
					}
					competition.put("studentName", studentName);
					competition.put("studentNo", studentNo);

					for (LessonInfo lessonInfo : lessonInfos) {
						if (lessonInfo == null) {
							continue;
						}

						if (lessonInfo.getId() <= 0) {
							continue;// 去掉不符合规律的科目编号添加
						}

						JSONObject scoreMap = competitionStuExamScoreMap.get(lessonInfo.getId() + studentId);
						String score = StringUtil.formatNumber(scoreMap.get("score"), 2)
								+ StringUtil.transformString(scoreMap.get("level"));

						competition.put(lessonInfo.getId() + "_score", score);

						String totalScore = StringUtil.formatNumber(scoreMap.get("totalScore"), 2);

						String totalScoreLevel = StringUtil.transformString(scoreMap.get("totalScoreLevel"));

						String totalScoreGradeRank = StringUtil.transformString(scoreMap.get("totalScoreGradeRank"));

						if (!competition.containsKey("totalScore")) {
							competition.put("totalScore", totalScore);
						}
						if (!competition.containsKey("totalScoreLevel")) {
							competition.put("totalScoreLevel", totalScoreLevel);
						}
						if (!competition.containsKey("totalScoreGradeRank")) {
							competition.put("totalScoreGradeRank", totalScoreGradeRank);
						}
					}

				}
			}

			EasyUIDatagridHead[][] secondHead = null;
			if (competitionStu != null && competitionStu.size() > 0 && lessonInfos != null) {
				secondHead = new EasyUIDatagridHead[1][];
				secondHead[0] = new EasyUIDatagridHead[lessonInfos.size() + 6];

				secondHead[0][0] = new EasyUIDatagridHead("subjectName", "科目组名称", "center", 100, 1, 1, true);
				secondHead[0][1] = new EasyUIDatagridHead("studentNo", "学生学号", "center", 80, 1, 1, true);
				secondHead[0][2] = new EasyUIDatagridHead("studentName", "姓名", "center", 50, 1, 1, true);

				for (int i = 3; i < lessonInfos.size() + 3; i++) {// 循环所有科目列表，设置每个科目的表头
					if (lessonInfos.get(i - 3) == null) {
						continue;
					}
					if (lessonInfos.get(i - 3).getId() <= 0) {
						continue;
					}
					secondHead[0][i] = new EasyUIDatagridHead(lessonInfos.get(i - 3).getId() + "_score",
							lessonInfos.get(i - 3).getName() + "", "center", 60, 1, 1, true);

				}
				secondHead[0][3 + lessonInfos.size()] = new EasyUIDatagridHead("totalScore", "总分", "center", 50, 1, 1,
						true);
				secondHead[0][3 + lessonInfos.size() + 1] = new EasyUIDatagridHead("totalScoreLevel", "总分等第", "center",
						100, 1, 1, true);
				secondHead[0][3 + lessonInfos.size() + 2] = new EasyUIDatagridHead("totalScoreGradeRank", "总分年级排名",
						"center", 100, 1, 1, true);
			}

			scoreDetail.put("total", competitionStu == null ? 0 : competitionStu.size());
			scoreDetail.put("rows", competitionStu);
			scoreDetail.put("columns", secondHead);

		}

		// 4.组织返回数据格式，并且返回。
		JSONObject result = new JSONObject();
		result.put("statisResult", statisResult);
		result.put("scoreDetail", scoreDetail);

		result.put("declare", "学校竞赛组的学生在“" + degreeInfo.getKslcmc() + "”中成绩汇总统计和明细查询");

		return result;
	}

	@Override
	public JSONObject getSubjectTopNStatisList(School school, JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");

		int topNRank = params.getIntValue("topNRank");

		// 2.按照班级排名的科目平均分表头组织，和数据组织

		/* 学生科目成绩信息列表 */
		List<JSONObject> studentScoresClassStatic = reportDao.getSubjectScoreClassStatistics(xnxq, autoIncr, params);
		// 学生总分成绩信息列表
		List<JSONObject> studentTotalScoresStatic = reportDao.getSubjectTotalScoreStatistics(xnxq, autoIncr, params);

		// 前N名均分和科目均分列表;
		List<JSONObject> topNStuAverScoreStatis = new ArrayList<JSONObject>();
		List<JSONObject> topNStudentList = new ArrayList<JSONObject>();// 学生科目成绩排名数据列表

		JSONObject topNStudentDetail = new JSONObject();
		JSONObject topNjson = new JSONObject();// 班级均分数据json

		if (CollectionUtils.isNotEmpty(studentScoresClassStatic)
				&& CollectionUtils.isNotEmpty(studentTotalScoresStatic)) {
			// 科目集合
			List<Long> subjIds = new ArrayList<Long>();
			List<Long> classIds = new ArrayList<Long>();

			// 学生成绩Map集合，为了提高程序性能，才把数据存到map来。
			Map<String, String> studentScoresClassStaticMap = new HashMap<String, String>();
			for (JSONObject studentScore : studentScoresClassStatic) {// 循环学生单科成绩数据列表，统计出所有的科目列表，以及班级科目对应的成绩数据
				long subjectId = studentScore.getLongValue("subjectId");
				long classId = studentScore.getLongValue("classId");
				if (!subjIds.contains(subjectId)) {
					subjIds.add(subjectId);
				}
				if (!classIds.contains(classId)) {
					classIds.add(classId);
				}

				String key = String.valueOf(classId) + "_" + subjectId;
				if (!studentScoresClassStaticMap.containsKey(key)) {
					String score = StringUtil.formatNumber(studentScore.get("Score"), 2);
					studentScoresClassStaticMap.put(key, score);
				}
			}

			// 放置科目数据Map,提高性能
			Map<Long, String> lessonInfosMap = new HashMap<Long, String>();

			// 获取基础数据 科目
			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoByType(school, 0, xnxq);
			if (lessonInfos != null) {
				for (LessonInfo lesson : lessonInfos) {
					if (lesson == null) {
						continue;
					}
					Long lessonId = lesson.getId();
					if (!lessonInfosMap.containsKey(lessonId))
						lessonInfosMap.put(lessonId, lesson.getName());
				}
			}

			// 组织表头
			EasyUIDatagridHead[][] head = null;
			if (subjIds.size() > 0) {// 科目信息大于0
				head = new EasyUIDatagridHead[1][];
				head[0] = new EasyUIDatagridHead[subjIds.size() + 3];
				int i = 1;
				head[0][0] = new EasyUIDatagridHead("className", "班级", "center", 50, 1, 1, true);
				for (Long subjectId : subjIds) {// 循环科目，组织表头，每一个科目显示一列。再加上前N名均分列和排名
					String lessonName = lessonInfosMap.get(subjectId);
					head[0][i] = new EasyUIDatagridHead(subjectId + "AverageScore", lessonName, "center", 50, 1, 1,
							true);
					i++;
				}

				// 设置前N名均分和名次两列的表头
				head[0][subjIds.size() + 1] = new EasyUIDatagridHead("topNTotalAverScore", "前" + topNRank + "名均分",
						"center", 100, 1, 1, true);
				head[0][subjIds.size() + 2] = new EasyUIDatagridHead("topNTotalAverScoreRank", "名次", "center", 50, 1, 1,
						true);
			}

			// 获取班级编号列表
			List<String> bhList = new ArrayList<String>();
			Map<String, String> classInfoMap = new HashMap<String, String>();

			List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
			if (classrooms != null) {
				for (Classroom classroom : classrooms) {
					if (classroom == null) {
						continue;
					}
					String classId = String.valueOf(classroom.getId());
					if (!bhList.contains(classId)) {
						bhList.add(classId);
					}
					if (!classInfoMap.containsKey(classId)) {
						classInfoMap.put(classId, classroom.getClassName());
					}
				}
			}
			params.put("bhList", bhList);// 设置班级编号

			// 对总分平均分进行排序
			if (studentTotalScoresStatic != null) {
				Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
					@Override
					public int compare(Map<String, Object> o1, Map<String, Object> o2) {
						float topNTotalAverScore1 = Float.valueOf(o1.get("topNTotalAverScore").toString());
						float topNTotalAverScore2 = Float.valueOf(o2.get("topNTotalAverScore").toString());
						return topNTotalAverScore1 > topNTotalAverScore2 ? -1
								: (topNTotalAverScore1 == topNTotalAverScore2 ? 0 : 1);
					}
				};
				Collections.sort(studentTotalScoresStatic, comparator);
			}

			// 组织数据格式
			int k = 0;
			float lastValue = 0;// 上一名次的前N名总分均值
			for (JSONObject item : studentTotalScoresStatic) {// 循环学生成绩，逐一的设置
				String classNo = item.get("classId").toString();// 班级编号
				JSONObject topNStuAverScoreItem = new JSONObject();
				if (classInfoMap != null) {
					topNStuAverScoreItem.put("className", classInfoMap.get(classNo));
				} else {
					topNStuAverScoreItem.put("className", "");
				}

				for (Long subjectId : subjIds) {
					String score = studentScoresClassStaticMap.get(classNo + "_" + subjectId);
					topNStuAverScoreItem.put(subjectId + "AverageScore", score);
				}

				// 取前N名均分和排名
				topNStuAverScoreItem.put("topNTotalAverScore",
						StringUtil.formatNumber(item.get("topNTotalAverScore"), 2));

				if (lastValue != item.getFloatValue("topNTotalAverScore")) {
					k = k + 1;
				}
				topNStuAverScoreItem.put("topNTotalAverScoreRank", k);
				topNStuAverScoreStatis.add(topNStuAverScoreItem);

				lastValue = item.getFloatValue("topNTotalAverScore");
			}

			Collections.sort(topNStuAverScoreStatis, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					String className1 = o1.getString("className");
					String className2 = o2.getString("className");
					return className1.compareTo(className2);
				}
			});

			topNjson.put("total", topNStuAverScoreStatis.size());
			topNjson.put("rows", topNStuAverScoreStatis);
			topNjson.put("columns", head);

			Map<Long, String> studentMap = new HashMap<Long, String>();

			// 3.班级科目 前N名情况表头和数据组织
			List<JSONObject> studentScores = reportDao.getStudentScores(xnxq, autoIncr, params);
			EasyUIDatagridHead[][] headTwo = null;
			if (studentScores != null && studentScores.size() > 0) {
				headTwo = new EasyUIDatagridHead[2][];
				headTwo[0] = new EasyUIDatagridHead[subjIds.size()];
				headTwo[1] = new EasyUIDatagridHead[subjIds.size() * 4];
				int j = 0;
				for (Long subjectId : subjIds) {// 根据每个科目生成表头格式
					headTwo[0][j] = new EasyUIDatagridHead(null, lessonInfosMap.get(subjectId), "center", 50, 1, 4,
							true);
					headTwo[1][4 * j] = new EasyUIDatagridHead(subjectId + "_className", "班级", "center", 50, 1, 1,
							true);
					headTwo[1][4 * j + 1] = new EasyUIDatagridHead(subjectId + "_studentName", "姓名", "center", 50, 1, 1,
							true);
					headTwo[1][4 * j + 2] = new EasyUIDatagridHead(subjectId + "_score", "成绩", "center", 50, 1, 1,
							true);
					headTwo[1][4 * j + 3] = new EasyUIDatagridHead(subjectId + "_gradeRank", "排名", "center", 50, 1, 1,
							true);
					j++;
				}

				List<Long> studIds = new ArrayList<Long>();
				for (JSONObject studScore : studentScores) {
					long xh = studScore.getLongValue("studentId");
					studIds.add(xh);
				}

				List<Account> studentList = commonDataService.getAccountBatch(schoolId, studIds, xnxq);
				if (studentList != null) {
					for (Account account : studentList) {
						if (account == null) {
							continue;
						}
						if (!studentMap.containsKey(account.getId()))
							studentMap.put(account.getId(), account.getName());
					}
				}
			}

			for (int m = 0; m < subjIds.size(); m++) {// 组织每个科目的排行在前面的成绩信息
				String subjectId = String.valueOf(subjIds.get(m));
				int n = 0;
				for (JSONObject student : studentScores) {// 循环所有成绩信息，查找该科目的排行前n的成绩
					String subjectId2 = student.get("subjectId").toString();
					String classId = student.get("classId").toString();
					Long studentId = student.getLong("studentId");
					if (subjectId2.equals(subjectId)) {
						if (m == 0) {
							JSONObject item = new JSONObject();
							item.put(subjectId + "_className", classInfoMap.get(classId));
							item.put(subjectId + "_studentName", studentMap.get(studentId));
							item.put(subjectId + "_score", student.get("Score"));
							item.put(subjectId + "_gradeRank", student.get("gradeRank"));
							topNStudentList.add(item);
						} else {
							JSONObject item = null;
							if (n < topNStudentList.size()) {
								item = topNStudentList.get(n);
							} else {
								item = new JSONObject();
								topNStudentList.add(item);
							}
							item.put(subjectId + "_className", classInfoMap.get(classId));
							item.put(subjectId + "_studentName", studentMap.get(studentId));
							item.put(subjectId + "_score", student.get("Score"));
							item.put(subjectId + "_gradeRank", student.get("gradeRank"));
							n++;
						}
					}
				}
			}

			topNStudentDetail.put("total", topNStudentList.size());
			topNStudentDetail.put("rows", topNStudentList);
			topNStudentDetail.put("columns", headTwo);
		}

		JSONObject result = new JSONObject();
		result.put("topNStuAverScoreStatis", topNjson);
		result.put("topNStudentDetail", topNStudentDetail);
		return result;

	}

	@Override
	public List<JSONObject> getExamNameList(JSONObject params) {
		// 1.读取考试名称列表和统计类型列表
		String nj = params.getString("nj");
		String xnxq = params.getString("xnxq");
		Integer termInfoRange = params.getInteger("termInfoRange");
		params.remove("termInfoRange");
		List<String> termInfoIds = new ArrayList<String>();
		switch (termInfoRange) {
		case 0: // 本学期
			termInfoIds.add(xnxq);
			break;
		case 1: // 本学年
			String xn = xnxq.substring(0, xnxq.length() - 1);
			termInfoIds.add(xn + "1");
			termInfoIds.add(xn + "2");
			break;
		case 2: // 历年
			termInfoIds.addAll(TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, xnxq));
			break;
		default:
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}

		List<JSONObject> result = new ArrayList<JSONObject>();
		for (String termInfoId : termInfoIds) {
			params.put("xnxq", termInfoId);
			for (JSONObject obj : reportDao.getExamineNameList(termInfoId, params)) {
				obj.put("examId", obj.get("examId") + "|" + obj.get("termInfoId"));
				result.add(obj);
				if (result.size() >= 12) {
					return result;
				}
			}
		}
		return result;
	}

	@Override
	public List<JSONObject> getStatisTypeList(JSONObject params) {
		// 1.读取考试名称列表和统计类型列表
		String xxdm = params.getString("xxdm");

		// 2.根据历次成绩趋势表统计类型权限，该显示哪些权限，并且组织好统计类型数据格式。
		// 获取报表显示列数，判断需要显示多少列
		String config = (String) configService.getReportFieldAuths(xxdm, "019").get("config");// 获取报表字段显示权限
		JSONObject fieldAuths = config == null ? null : JSON.parseObject(config);

		List<JSONObject> statisTypeList = new ArrayList<JSONObject>();

		String totalAverageRank = fieldAuths == null ? "" : StringUtil.transformString(fieldAuths.get("pm"));// 总平均分排名
		if ("1".equals(totalAverageRank)) {
			JSONObject statisType = new JSONObject();
			statisType.put("value", "01");
			statisType.put("text", "总平均分排名");
			statisTypeList.add(statisType);
		}

		String allA = fieldAuths == null ? "" : StringUtil.transformString(fieldAuths.get("allA"));// 全A人数
		if ("1".equals(allA)) {
			JSONObject statisType = new JSONObject();
			statisType.put("value", "02");
			statisType.put("text", "全A人数");
			statisTypeList.add(statisType);
		}

		String secondA1B = fieldAuths == null ? "" : StringUtil.transformString(fieldAuths.get("secondA1B"));// 全A人数
		if ("1".equals(secondA1B)) {
			JSONObject statisType = new JSONObject();
			statisType.put("value", "03");
			statisType.put("text", "次A1B人数");
			statisTypeList.add(statisType);
		}

		String passRate = fieldAuths == null ? "" : StringUtil.transformString(fieldAuths.get("hgl"));// 合格率
		if ("1".equals(passRate)) {
			JSONObject statisType = new JSONObject();
			statisType.put("value", "04");
			statisType.put("text", "合格率");
			statisTypeList.add(statisType);
		}

		String excellentRate = fieldAuths == null ? "" : StringUtil.transformString(fieldAuths.get("yxl"));// 合格率
		if ("1".equals(excellentRate)) {
			JSONObject statisType = new JSONObject();
			statisType.put("value", "05");
			statisType.put("text", "优秀率");
			statisTypeList.add(statisType);
		}

		return statisTypeList;
	}

	@Override
	public List<JSONObject> getAllPreviousTrendList(JSONObject params) {
		String kslcStr = params.getString("kslcStr");
		params.remove("kslcStr");

		// 统计类别编号1:总平均分排名; 02:全A人数; 03:次A1B人数; 04:合格率; 05:优秀率
		String statisTypeId = params.getString("statisTypeId");

		params.remove("statisTypeId");

		Long schoolId = params.getLong("xxdm");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		String nj = params.getString("nj");

		List<JSONObject> kslcList = new ArrayList<JSONObject>();
		if ("".equals(kslcStr)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("xxdm", xxdm);
			map.put("xnxq", xnxq);
			map.put("nj", nj);
			List<JSONObject> examNameList = reportDao.getExamineNameList(xnxq, map); // 考试名称列表
			for (JSONObject m : examNameList) {
				String kslc = m.getString("examId");
				String termInfoId = m.getString("termInfoId");

				JSONObject json = new JSONObject();
				json.put("kslc", kslc);
				json.put("kslcdm", kslc);
				json.put("xnxq", termInfoId);
				json.put("xxdm", xxdm);
				json.put("nj", nj);
				kslcList.add(json);
			}
		} else {
			for (String kslcKey : kslcStr.split(",")) {
				String[] tmp = kslcKey.split("\\|");

				JSONObject json = new JSONObject();
				json.put("kslcdm", tmp[0]);
				json.put("kslc", tmp[0]);
				json.put("xnxq", tmp[1]);
				json.put("xxdm", xxdm);
				json.put("nj", nj);
				kslcList.add(json);
			}
		}

		List<JSONObject> classGraphDataList = new ArrayList<JSONObject>();

		if (kslcList.size() == 0) {
			return classGraphDataList;
		}

		// 班级列表
		HashMap<String, Object> classroomMap = new HashMap<String, Object>();
		classroomMap.put("schoolId", schoolId);
		classroomMap.put("termInfoId", xnxq);
		classroomMap.put("usedGradeId", nj);
		List<Classroom> classrooms = commonDataService.getClassList(classroomMap);

		// 提取班级编号，放置到List列表中
		List<String> bhList = new ArrayList<String>();// 班级编号列表
		if (classrooms != null) {
			for (Classroom classroom : classrooms) {
				if (classroom == null) {
					continue;
				}
				bhList.add(String.valueOf(classroom.getId()));
			}
		}

		List<JSONObject> examList = new ArrayList<JSONObject>();

		List<JSONObject> classAverageScoreList = new ArrayList<JSONObject>();
		List<JSONObject> levelStudentNumList = new ArrayList<JSONObject>();
		for (JSONObject json : kslcList) {
			String termInfoId = json.getString("xnxq");
			DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfoId, json);
			if (degreeInfo == null) {
				continue;
			}
			Integer autoIncr = degreeInfo.getAutoIncr();
			json.put("bhList", bhList);

			JSONObject obj = new JSONObject();
			obj.put("examId", degreeInfo.getKslcdm());
			obj.put("examName", degreeInfo.getKslcmc());
			obj.put("termInfoId", termInfoId);
			examList.add(obj);

			if ("01".equals(statisTypeId) || "04".equals(statisTypeId) || "05".equals(statisTypeId)) {
				classAverageScoreList.addAll(reportDao.getClassAverageScoreList(termInfoId, autoIncr, json));
			} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
				Integer count = reportDao.getExamNormalSubjectCount(termInfoId, autoIncr, json);
				if ("02".equals(statisTypeId)) {
					json.put("allALevel", count + "A");
				}
				if ("03".equals(statisTypeId)) {
					json.put("s6A1BLevel", (count - 1) + "A1B");
				}
				levelStudentNumList.addAll(reportDao.getLevelStudentNumList(termInfoId, autoIncr, json));
			} else {
				throw new CommonRunException(-1, "统计类别参数传递异常，请联系管理员！");
			}
		}

		Map<String, JSONObject> classAverageScoreMap = new HashMap<String, JSONObject>();
		if (CollectionUtils.isNotEmpty(classAverageScoreList)) {
			for (JSONObject classAverageScore : classAverageScoreList) {// 把数据放置到Map中
				if (classAverageScore == null) {
					continue;
				}
				String examId = classAverageScore.getString("examId");
				String classId = classAverageScore.getString("classId");

				String key = examId + classId;
				if (!classAverageScoreMap.containsKey(key)) {
					classAverageScoreMap.put(key, classAverageScore);
				}
			}
		}

		Map<String, JSONObject> levelStudentNumMap = new HashMap<String, JSONObject>();
		if (CollectionUtils.isNotEmpty(levelStudentNumList)) {
			for (JSONObject levelStudentNum : levelStudentNumList) {// 把数据放置到Map中
				if (levelStudentNum == null) {
					continue;
				}
				String examId = levelStudentNum.getString("examId");
				String classId = levelStudentNum.getString("classId");

				String key = examId + classId;
				if (!levelStudentNumMap.containsKey(key)) {
					levelStudentNumMap.put(key, levelStudentNum);
				}
			}
		}

		// 循环班级列表，统计每个列表的曲线数据
		String[] xAxis = new String[examList.size()];// x轴标识，是一个字符型数组
		String identifier = "";
		for (int i = 0; i < examList.size(); i++) {
			// 添加考试数据
			JSONObject exam = examList.get(i);
			if (exam == null) {
				continue;
			}

			identifier = "T" + (i + 1);
			exam.put("identifier", identifier);

			// 设置x坐标的值
			xAxis[i] = identifier;
		}

		if (classrooms != null) {
			Collections.sort(classrooms, new Comparator<Classroom>() {
				@Override
				public int compare(Classroom o1, Classroom o2) {
					String className1 = o1.getClassName();
					if (className1 == null) {
						className1 = "";
					}
					String className2 = o2.getClassName();
					if (className2 == null) {
						className2 = "";
					}
					return className1.compareTo(className2);
				}
			});

			for (Classroom room : classrooms) {
				// 每个班级的曲线图数据
				JSONObject item = new JSONObject();

				// 设置classId:班级代码
				item.put("classId", room.getId());

				// 设置title:表名
				if ("01".equals(statisTypeId)) {// 01: 总平均分排名
					item.put("title", "历次考试总平均分排名趋势表(" + room.getClassName() + ")");
				} else if ("02".equals(statisTypeId)) {
					item.put("title", "历次考试全A人数趋势表(" + room.getClassName() + ")");
				} else if ("03".equals(statisTypeId)) {
					item.put("title", "历次考试次A1B人数趋势表(" + room.getClassName() + ")");
				} else if ("04".equals(statisTypeId)) {
					item.put("title", "历次考试次合格率趋势表(" + room.getClassName() + ")");
				} else {
					item.put("title", "历次考试次优秀率趋势表(" + room.getClassName() + ")");
				}

				List<JSONObject> seriesList = new ArrayList<JSONObject>();// 曲线数据列表
				JSONObject series = new JSONObject();// 曲线数据

				List<JSONObject> dataList = new ArrayList<JSONObject>();

				// 表示总平均分排名趋势线是否删除了y值为零的节点。如果删除了，就要把x轴的列表设置换成新的列表。
				boolean isDeletePoint = false;

				List<JSONObject> classAverScoreRankexamList = new ArrayList<JSONObject>();// 新的班级总平均分排名的考试列表

				List<Float> x = new ArrayList<Float>();// x轴坐标集合
				List<Float> y = new ArrayList<Float>();// y轴坐标集合

				int j = 0;
				for (int i = 0; i < examList.size(); i++) {
					// 添加考试数据
					JSONObject exam = examList.get(i);

					String examId = exam.getString("examId");

					// 设置曲线的点值
					if ("01".equals(statisTypeId)) {
						series.put("name", "排名");
					} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
						series.put("name", "人数");
					} else if ("04".equals(statisTypeId)) {
						series.put("name", "合格率");
					} else {
						series.put("name", "优秀率");
					}

					// 计算曲线x,y坐标
					JSONObject data = new JSONObject();

					float yValue = getYValue(classAverageScoreMap, levelStudentNumMap, examId,
							String.valueOf(room.getId()), statisTypeId);

					// 如果是总平均分排名，并且点的y值小于0，则不添加这次考试
					if ("01".equals(statisTypeId) && yValue < 1) {
						isDeletePoint = true;
						continue;
					}

					data.put("name", exam.get("identifier") + ":" + exam.get("examName"));
					data.put("x", j);
					x.add(Float.valueOf(j + 1));
					data.put("y", yValue);
					y.add(yValue);
					classAverScoreRankexamList.add(exam);
					dataList.add(data);

					j++;
				}

				series.put("data", dataList);

				// 计算趋势线的起点坐标和终点坐标
				float[][] startEnd = MathUtil.trendLineCoordinate(x, y);

				JSONObject trendLineSeries = new JSONObject();// 趋势线线数据
				List<JSONObject> dataList2 = new ArrayList<JSONObject>();

				trendLineSeries.put("name", "趋势");

				if (x.size() > 1) {
					JSONObject start = new JSONObject();// 起点坐标
					start.put("name", "T" + Math.round(startEnd[0][0]));
					start.put("x", Math.round(startEnd[0][0]) - 1);
					start.put("y", startEnd[0][1]);
					dataList2.add(start);

					JSONObject end = new JSONObject();// 终点坐标
					end.put("name", "T" + Math.round(startEnd[1][0]));
					end.put("x", Math.round(startEnd[1][0]) - 1);
					end.put("y", startEnd[1][1]);
					dataList2.add(end);
				}

				trendLineSeries.put("data", dataList2);

				seriesList.add(series);
				seriesList.add(trendLineSeries);

				// x轴标识，是一个字符型数组

				String[] classAverScoreRankXAxis = new String[classAverScoreRankexamList.size()];// 新的班级总平均分排名的x轴列表

				if (isDeletePoint) {// 如果删除掉了部分y值为零的点，则把列表设成下面的x轴
					for (int i = 0; classAverScoreRankexamList != null && i < classAverScoreRankexamList.size(); i++) {
						// 添加考试数据
						JSONObject exam = classAverScoreRankexamList.get(i);
						// 设置x坐标的值
						classAverScoreRankXAxis[i] = StringUtil.transformString(exam.get("identifier"));
					}

					item.put("xAxis", classAverScoreRankXAxis);
				} else {
					item.put("xAxis", xAxis);
				}

				// 设置考试列表
				List<Object> list = new ArrayList<Object>();
				if (isDeletePoint) {
					for (JSONObject json : classAverScoreRankexamList) {
						list.add(json.clone());
					}
				} else {
					for (JSONObject json : examList) {
						list.add(json.clone());
					}
				}
				item.put("examList", list);
				// 设置曲线图形数据
				item.put("Series", seriesList);
				classGraphDataList.add(item);
			}
		}
		return classGraphDataList;
	}

	/****
	 * 返回曲线Y值
	 * 
	 * @param classAverageScoreList
	 * @param levelStudentNumList
	 * @param examId
	 * @param statisTypeId
	 * @return
	 */
	public float getYValue(Map<String, JSONObject> classAverageScoreMap, Map<String, JSONObject> levelStudentNumMap,
			String examId, String classId, String statisTypeId) {
		// 01:总平均分排名,02:全A人数,03:次A1B人数;04:合格率;05:优秀率
		if ("01".equals(statisTypeId) || "04".equals(statisTypeId) || "05".equals(statisTypeId)) {
			// 当是获取总平均分排名，合格率，优秀率的时候，从classAverageScoreList数据集合中去获取曲线的y值
			JSONObject classAverageScore = classAverageScoreMap.get(examId + classId);
			if (classAverageScore != null) {
				if ("01".equals(statisTypeId)) {
					return (int) classAverageScore.get("totalRank");
				} else if ("04".equals(statisTypeId)) {
					return (float) classAverageScore.get("passRate");
				} else if ("05".equals(statisTypeId)) {
					return (float) classAverageScore.get("excellentRate");
				}
			}
			return 0;
		} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
			// 当是获取总平均分排名，合格率，优秀率的时候，从classAverageScoreList数据集合中去获取曲线的y值
			JSONObject levelStudentNum = levelStudentNumMap.get(examId + classId);
			if (levelStudentNum != null) {
				return (int) levelStudentNum.get("studentNum");
			}
			return -1000;
		} else {
			throw new CommonRunException(-1, "统计类别参数传递异常，请联系管理员！");
		}
	}

	@Override
	public JSONObject getClassReportList(JSONObject params) {
		Long classId = params.getLong("bh");
		Long schoolId = params.getLong("xxdm");
		String xnxq = params.getString("xnxq");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-1, "此考试已被删除，请刷新页面！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		JSONObject report = reportDao.getClassReportList(xnxq, autoIncr, params);
		if (report == null) {
			report = new JSONObject();
			report.put("examName", "");
			report.put("className", "");
			report.put("examSituation", "");
			report.put("studentAbnormalSituation", "");
			report.put("scoreTraceData", "");
			report.put("everySubjectRankNum", "");
			report.put("everyClassTotalScoreNum", "");
		} else {
			report.put("examName", degreeInfo.getKslcmc());
			Classroom classroom = commonDataService.getClassById(schoolId, classId, xnxq);
			report.put("className", "");
			if (classroom != null) {
				report.put("className", classroom.getClassName());
			}
			// 转换数据格式为json
			String examSituation = StringUtil.transformString(report.get("examSituation"));
			if (!StringUtil.isEmpty(examSituation)) {
				report.put("examSituation", JSON.parse(examSituation));
			}

			String studentAbnormalSituation = StringUtil.transformString(report.get("studentAbnormalSituation"));
			if (!StringUtil.isEmpty(studentAbnormalSituation)) {
				JSONArray array = (JSONArray) JSON.parse(studentAbnormalSituation);
				if (array != null) {
					for (Object object : array) {
						JSONObject json = (JSONObject) object;
						if (!json.containsKey("jb")) {
							json.put("jb", "");
						}
						if (!json.containsKey("yc")) {
							json.put("yc", "");
						}
					}
				}
				report.put("studentAbnormalSituation", array);
			}

			String scoreTraceData = StringUtil.transformString(report.get("scoreTraceData"));
			if (!StringUtil.isEmpty(scoreTraceData))
				report.put("scoreTraceData", JSON.parse(scoreTraceData));

			String everySubjectRankNum = StringUtil.transformString(report.get("everySubjectRankNum"));
			if (!StringUtil.isEmpty(everySubjectRankNum))
				report.put("everySubjectRankNum", JSON.parse(everySubjectRankNum));

			String everyClassTotalScoreNum = StringUtil.transformString(report.get("everyClassTotalScoreNum"));
			if (!StringUtil.isEmpty(everyClassTotalScoreNum)) {
				JSONObject JSONObj = JSON.parseObject(everyClassTotalScoreNum);
				Collections.sort(JSONObj.getJSONArray("rows"), new Comparator<Object>(){
					@Override
					public int compare(Object o1, Object o2) {
						String bjmc1 = ((JSONObject) o1).getString("bjmc");
						if(bjmc1 == null) {
							return 1;
						}
						String bjmc2 = ((JSONObject) o2).getString("bjmc");
						if(bjmc2 == null) {
							return -1;
						}
						return bjmc1.compareTo(bjmc2);
					}
				});
				report.put("everyClassTotalScoreNum", JSONObj);
			}
		}
		return report;
	}

	@Override
	public JSONObject getStudentScoreReportListByTea(JSONObject params) {
		Long schoolId = params.getLong("xxdm");
		String xnxq = params.getString("xnxq");
		String nj = params.getString("nj");
		String bhStr = params.getString("bhStr");
		String stdNumOrName = params.getString("stdNumOrName");
		Integer termInfoRange = params.getInteger("termInfoRange");
		params.remove("bhStr");
		params.remove("stdNumOrName");
		params.remove("termInfoRange");

		JSONObject data = new JSONObject();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("classId", bhStr);
		map.put("termInfoId", xnxq);
		map.put("usedGradeId", nj);
		map.put("keyword", stdNumOrName);
		List<Account> accList = commonDataService.getStudentList(map);// 学生
		if (CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "未检索出此姓名的学生，请检查学号/姓名参数！");
		}

		StringBuffer classIdBuffer = new StringBuffer();
		List<String> xhList = new ArrayList<String>();
		for (Account acc : accList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}
			for (User user : acc.getUsers()) {
				if (user == null || user.getStudentPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					xhList.add(String.valueOf(acc.getId()));
					classIdBuffer.append(user.getStudentPart().getClassId()).append(",");
					break;
				}
			}
		}
		if (xhList.size() == 0) {
			throw new CommonRunException(-1, "未检索出此姓名的学生，请检查学号/姓名参数！");
		}
		params.put("xhList", xhList);

		if (classIdBuffer.length() > 0) {
			classIdBuffer.deleteCharAt(classIdBuffer.length() - 1);
		}
		params.put("bhList", StringUtil.convertToListFromStr(classIdBuffer.toString(), ",", String.class));
		List<Long> classIds = StringUtil.convertToListFromStr(classIdBuffer.toString(), ",", Long.class);
		List<Classroom> classroomList = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		Map<String, Classroom> cmap = new HashMap<String, Classroom>();
		for (Classroom room : classroomList) {
			cmap.put(String.valueOf(room.getId()), room);
		}

		List<String> termInfoIds = new ArrayList<String>();
		switch (termInfoRange) {
		case 0: // 本学期
			termInfoIds.add(xnxq);
			break;
		case 1: // 本学年
			String xn = xnxq.substring(0, xnxq.length() - 1);
			termInfoIds.add(xn + "1");
			termInfoIds.add(xn + "2");
			break;
		case 2:
			termInfoIds.addAll(TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, xnxq));
			break;
		default:
			throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
		}

		List<JSONObject> studentAllExamScoreList = new ArrayList<JSONObject>();
		if (termInfoIds.size() > 0) {
			for (String termInfoId : termInfoIds) {
				params.put("xnxq", termInfoId);
				params.put("drflag", "1");
				List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, params);
				for (DegreeInfo degreeInfo : degreeInfoList) {
					Integer autoIncr = degreeInfo.getAutoIncr();
					String kslc = degreeInfo.getKslcdm();
					params.put("kslc", kslc);
					params.put("kslcmc", degreeInfo.getKslcmc());
					params.put("cdate", degreeInfo.getCdate());
					params.put("fbpmflag", degreeInfo.getFbpmflag());
					studentAllExamScoreList.addAll(reportDao.getStudentAllExamScoreList(termInfoId, autoIncr, params));
				}
			}
		}

		List<Long> subjIds = new ArrayList<Long>();
		for (JSONObject json : studentAllExamScoreList) {
			String subjectId = json.getString("lessonId");
			if ("totalScore".equals(subjectId)) {
				continue;
			}
			Long subjId = Long.valueOf(subjectId);
			if (!subjIds.contains(subjId)) {
				subjIds.add(subjId);
			}
		}

		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoBatch(schoolId, subjIds, xnxq);
		List<JSONObject> lessonlist = new ArrayList<JSONObject>();
		if (lessonInfoList != null) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				JSONObject json = new JSONObject();
				json.put("lessonId", lessonInfo.getId());
				json.put("lessonName", lessonInfo.getName());
				lessonlist.add(json);
			}
		}

		Collections.sort(lessonlist, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				long subj0 = arg0.getLongValue("lessonId");
				long subj1 = arg1.getLongValue("lessonId");
				return Long.compare(subj0, subj1);
			}
		});

		// 拼装表头
		EasyUIDatagridHead[][] head = null;

		head = new EasyUIDatagridHead[1][];
		head[0] = new EasyUIDatagridHead[3 + lessonlist.size()];
		int i = 0;
		for (JSONObject lm : lessonlist) {
			head[0][i] = new EasyUIDatagridHead(lm.getString("lessonId"), lm.getString("lessonName"), "center", 50, 1,
					1, true);
			i++;
		}
		head[0][lessonlist.size() + 0] = new EasyUIDatagridHead("totalScore", "总分", "center", 50, 1, 1, true);
		head[0][lessonlist.size() + 1] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1, 1, true);
		head[0][lessonlist.size() + 2] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1, 1, true);

		data.put("columns", head);

		// 拼装map
		List<JSONObject> Studentdata = new ArrayList<JSONObject>();
		for (Account acc : accList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}
			for (User user : acc.getUsers()) {
				if (user == null || user.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					String classId = String.valueOf(user.getStudentPart().getClassId());
					if (cmap.containsKey(classId)) {
						JSONObject umap = new JSONObject();
						umap.put("classsName", cmap.get(classId).getClassName());
						umap.put("studentName", acc.getName());
						umap.put("studentNo", acc.getId());
						Studentdata.add(umap);
					}
					break;
				}
			}
		}

		Map<String, LinkedHashMap<String, List<JSONObject>>> exmap = new HashMap<String, LinkedHashMap<String, List<JSONObject>>>();
		for (JSONObject m : studentAllExamScoreList) {
			String examid = m.get("examId").toString();
			String xh = m.get("xh").toString();
			if (exmap.containsKey(xh)) {
				LinkedHashMap<String, List<JSONObject>> mlist = exmap.get(xh);
				if (mlist.containsKey(examid)) {
					List<JSONObject> mm = mlist.get(examid);
					mm.add(m);
					mlist.put(examid, mm);
				} else {
					List<JSONObject> mm = new ArrayList<JSONObject>();
					mm.add(m);
					mlist.put(examid, mm);
				}
				exmap.put(xh, mlist);
			} else {
				LinkedHashMap<String, List<JSONObject>> mlist = new LinkedHashMap<String, List<JSONObject>>();
				List<JSONObject> mm = new ArrayList<JSONObject>();
				mm.add(m);
				mlist.put(examid, mm);
				exmap.put(xh, mlist);
			}
		}

		for (JSONObject m : Studentdata) {
			List<JSONObject> rows = new ArrayList<JSONObject>();
			if (exmap.containsKey(m.get("studentNo").toString())) {
				Map<String, List<JSONObject>> mlist = exmap.get(m.get("studentNo").toString());
				Iterator<Entry<String, List<JSONObject>>> it = mlist.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, List<JSONObject>> entry = it.next();
					List<JSONObject> value = entry.getValue();
					JSONObject row = new JSONObject();
					// 先把所有科目的成绩设置为“-”
					row.put("totalScoreClassRank", "-");
					row.put("totalScoreGradeRank", "-");
					for (JSONObject lm : lessonlist) {
						row.put(lm.getString("lessonId"), "-");
						row.put("totalScore", "-");
					}
					// 拼装
					for (JSONObject ma : value) {
						row.put("examName", ma.get("examName"));
						row.put(ma.get("lessonId").toString(), ma.get("score") == null ? "-" : ma.get("score"));
						if (!"".equals(ma.get("classRank").toString())) {
							row.put("totalScoreClassRank", ma.get("classRank"));
						}
						if (!"".equals(ma.get("gradeRank").toString())) {
							row.put("totalScoreGradeRank", ma.get("gradeRank"));
						}
					}
					rows.add(row);
				}
			}
			m.put("rows", rows);
		}

		data.put("Studentdata", Studentdata);
		return data;
	}

	@Override
	public JSONObject getStudentOptimization(JSONObject params) {
		String xnxq = params.getString("xnxq");
		String nj = params.getString("nj");
		String bhStr = params.getString("bhStr");
		Integer termInfoRange = params.getInteger("termInfoRange");
		String xmxh = params.getString("xmxh");
		Long schoolId = params.getLong("xxdm");
		String xxdm = params.getString("xxdm");

		params.remove("bhStr");
		params.remove("xmxh");
		params.remove("termInfoRange");

		JSONObject data = new JSONObject();

		List<String> termInfoIds = new ArrayList<String>();
		switch (termInfoRange) {
		case 0:
			termInfoIds.add(xnxq);
			break;
		case 1:
			String xn = xnxq.substring(0, xnxq.length() - 1);
			termInfoIds.add(xn + "1");
			termInfoIds.add(xn + "2");
			break;
		case 2:
			termInfoIds.addAll(TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, xnxq));
			break;
		}

		HashMap<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("schoolId", schoolId);
		hashmap.put("classId", bhStr);
		hashmap.put("termInfoId", xnxq);
		hashmap.put("usedGradeId", nj);
		hashmap.put("keyword", xmxh);
		List<Account> accList = commonDataService.getStudentList(hashmap);// 学生
		if (CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "未获取此学生信息，请重新检查学号/姓名！");
		}

		List<String> xhList = new ArrayList<String>();
		StringBuffer classIdBuffer = new StringBuffer();
		for (Account acc : accList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}

			for (User user : acc.getUsers()) {
				if (user == null || user.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					if (user.getStudentPart() == null) {
						continue;
					}
					xhList.add(String.valueOf(acc.getId()));
					classIdBuffer.append(user.getStudentPart().getClassId()).append(",");
					break;
				}
			}
		}
		if (xhList.size() == 0) {
			throw new CommonRunException(-1, "未获取此学生信息，请重新检查学号/姓名！");
		}

		if (classIdBuffer.length() > 0) {
			classIdBuffer.deleteCharAt(classIdBuffer.length() - 1);
		}

		List<Long> classIds = StringUtil.convertToListFromStr(classIdBuffer.toString(), ",", Long.class);
		List<Classroom> classroomList = commonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		Map<String, Classroom> cmap = new HashMap<String, Classroom>();
		if (classroomList != null) {
			for (Classroom classroom : classroomList) {
				cmap.put(String.valueOf(classroom.getId()), classroom);
			}
		}

		List<JSONObject> studentAllExamScoreList = new ArrayList<JSONObject>();
		if (termInfoIds.size() > 0) {
			params.put("xhList", xhList);
			params.put("bhList", StringUtil.convertToListFromStr(classIdBuffer.toString(), ",", String.class));
			for (String termInfoId : termInfoIds) {
				params.put("xnxq", termInfoId);
				params.put("drflag", "1");
				List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, params);
				for (DegreeInfo degreeInfo : degreeInfoList) {
					Integer autoIncr = degreeInfo.getAutoIncr();
					String kslc = degreeInfo.getKslcdm();
					params.put("kslc", kslc);
					params.put("kslcmc", degreeInfo.getKslcmc());
					params.put("cdate", degreeInfo.getCdate());
					params.put("fbpmflag", degreeInfo.getFbpmflag());
					studentAllExamScoreList.addAll(reportDao.getStudentAllExamScoreList(termInfoId, autoIncr, params));
				}
			}
		}

		List<Long> subjIds = new ArrayList<Long>();
		for (JSONObject json : studentAllExamScoreList) {
			String subjectId = json.getString("lessonId");
			if ("totalScore".equals(subjectId)) {
				continue;
			}

			Long subjId = Long.valueOf(subjectId);
			if (!subjIds.contains(subjId)) {
				subjIds.add(subjId);
			}
		}

		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoBatch(schoolId, subjIds, xnxq);
		List<JSONObject> lessonlist = new ArrayList<JSONObject>();
		if (lessonInfoList != null) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				JSONObject json = new JSONObject();
				json.put("lessonId", lessonInfo.getId());
				json.put("lessonName", lessonInfo.getName());
				lessonlist.add(json);
			}
		}

		Collections.sort(lessonlist, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg0, JSONObject arg1) {
				long subj0 = arg0.getLongValue("lessonId");
				long subj1 = arg1.getLongValue("lessonId");
				return Long.compare(subj0, subj1);
			}
		});

		String config = (String) configService.getReportFieldAuths(xxdm, "025").get("config");// 获取报表字段显示权限
		JSONObject fieldAuths = config == null ? null : JSON.parseObject(config);
		int perFieldNum = 2;// 每科目显示列数，默认显示成绩，班名，年名

		if (fieldAuths != null && "0".equals(fieldAuths.getString("cj"))) {
			perFieldNum = perFieldNum - 1;
		}

		// 拼装表头
		EasyUIDatagridHead[][] head = null;

		head = new EasyUIDatagridHead[2][];
		head[0] = new EasyUIDatagridHead[1 + lessonlist.size()];
		head[1] = new EasyUIDatagridHead[(1 + lessonlist.size()) * perFieldNum + 2];
		int i = 0;
		for (JSONObject lm : lessonlist) {
			head[0][i] = new EasyUIDatagridHead(null, lm.getString("lessonName"), "center", 50, 1, perFieldNum, true);

			int index = 1;
			if (!(fieldAuths != null && "0".equals(fieldAuths.getString("cj")))) {
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead(lm.getString("lessonId") + "_score", "原始分数",
						"center", 60, 1, 1, true);

				head[1][i * perFieldNum + index] = new EasyUIDatagridHead(lm.getString("lessonId") + "_bzscore", "标准分数",
						"center", 60, 1, 1, true);

				index++;
			} else {
				head[1][i * perFieldNum + 0] = new EasyUIDatagridHead(lm.getString("lessonId") + "_bzscore", "标准分数",
						"center", 60, 1, 1, true);
			}

			i++;
		}
		head[0][i] = new EasyUIDatagridHead(null, "总分", "center", 0, 1, perFieldNum + 2, true);

		int index = 1;
		if (!"0".equals(fieldAuths.getString("cj"))) {

			head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("totalScore", "原始分数", "center", 60, 1, 1, true);

			head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalBzScore", "标准分数", "center", 60, 1, 1, true);

			index++;
		} else {
			head[1][i * perFieldNum + 0] = new EasyUIDatagridHead("totalBzScore", "标准分数", "center", 60, 1, 1, true);
		}
		if (!"0".equals(fieldAuths.getString("bjpm"))) {
			head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1, 1,
					true);
			index++;
		}
		if (!"0".equals(fieldAuths.getString("njpm"))) {
			head[1][i * perFieldNum + index] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1, 1,
					true);
		}

		data.put("columns", head);
		// 拼装map
		List<JSONObject> Studentdata = new ArrayList<JSONObject>();
		for (Account acc : accList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}
			for (User user : acc.getUsers()) {
				if (user == null || user.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					String clasid = String.valueOf(user.getStudentPart().getClassId());
					if (cmap.containsKey(clasid)) {
						JSONObject umap = new JSONObject();
						umap.put("classsName", cmap.get(clasid).getClassName());
						umap.put("studentName", acc.getName());
						umap.put("studentNo", acc.getId());
						Studentdata.add(umap);
					}
					break;
				}
			}
		}
		Map<String, LinkedHashMap<String, List<JSONObject>>> exmap = new HashMap<String, LinkedHashMap<String, List<JSONObject>>>();

		for (JSONObject m : studentAllExamScoreList) {
			String examid = m.get("examId").toString();
			String xh = m.get("xh").toString();
			if (exmap.containsKey(xh)) {
				LinkedHashMap<String, List<JSONObject>> mlist = exmap.get(xh);
				if (mlist.containsKey(examid)) {
					List<JSONObject> mm = mlist.get(examid);
					mm.add(m);
					mlist.put(examid, mm);
				} else {
					List<JSONObject> mm = new ArrayList<JSONObject>();
					mm.add(m);
					mlist.put(examid, mm);
				}
				exmap.put(xh, mlist);
			} else {
				LinkedHashMap<String, List<JSONObject>> mlist = new LinkedHashMap<String, List<JSONObject>>();
				List<JSONObject> mm = new ArrayList<JSONObject>();
				mm.add(m);
				mlist.put(examid, mm);
				exmap.put(xh, mlist);
			}
		}

		for (JSONObject m : Studentdata) {
			List<JSONObject> rows = new ArrayList<JSONObject>();
			if (exmap.containsKey(m.getString("studentNo"))) {
				Map<String, List<JSONObject>> mlist = exmap.get(m.getString("studentNo"));
				Iterator<Entry<String, List<JSONObject>>> it = mlist.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, List<JSONObject>> entry = it.next();
					List<JSONObject> value = entry.getValue();
					JSONObject row = new JSONObject();
					// 先把所有科目的成绩设置为“-”
					for (JSONObject lm : lessonlist) {
						row.put(lm.getString("lessonId") + "_score", "-");
						row.put(lm.getString("lessonId") + "_bzscore", "-");
					}
					row.put("totalBzScore", "-");
					row.put("totalScore", "-");
					row.put("totalScoreClassRank", "-");
					row.put("totalScoreGradeRank", "-");
					// 拼装
					for (JSONObject ma : value) {
						if(!row.containsKey("examName")) {
							row.put("examName", ma.get("examName"));
						}
						String lessonId = ma.getString("lessonId");
						if(lessonId == null) {
							continue;
						}
						if(!"totalScore".equals(lessonId)) {
							row.put(ma.getString("lessonId") + "_score", ma.get("score") == null ? "-" : ma.get("score"));
							row.put(ma.getString("lessonId") + "_bzscore",
									StringUtils.isBlank(ma.getString("bzcj")) ? "-" : ma.getString("bzcj"));
						} else {
							row.put("totalBzScore", ma.get("bzzcj"));
							row.put("totalScore", ma.get("score") == null ? "-" : ma.get("score"));
							row.put("totalScoreClassRank", ma.get("bzbjpm"));
							row.put("totalScoreGradeRank", ma.get("bznjpm"));
						}
					}
					rows.add(row);
				}
			}
			m.put("rows", rows);
		}

		data.put("Studentdata", Studentdata);
		return data;
	}
	
	//获取成绩排名
	private Integer getPosition(List<Float> array, Float obj){		
		if(null==obj || array==null){
			return 0;
		}
		
		if(0==obj.floatValue()){
			return array.size();
		}
		Collections.sort(array,new Comparator<Float>() {

			@Override
			public int compare(Float o2, Float o1) {
				// TODO Auto-generated method stub
				return (o1.floatValue()-o2.floatValue())>0?1:((o1.floatValue()-o2.floatValue())==0)?0:-1;
			}
		});
		
		int mid = array.size()/2;
		int low = 0, high = array.size()-1;
		while(high>=low){
			if(obj.equals(array.get(mid))){
				while(mid>0){
					if(array.get(mid-1).floatValue()==obj.floatValue()){
						--mid;
					}else{
						return mid+1;
					}
				}
				return /*array.length-*/mid+1;
			}
			if((float)array.get(mid)<obj.floatValue()){
				high = mid-1;
			}else{
				low = mid+1;
			}
			
			mid=(low+high)/2;
		}
		
		return -1;
	}

	// 获取班级最近考试信息
	@Override
	public JSONObject getRecentClassExamInfo(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		Integer autoIncr = 0 ;
		
		List<JSONObject> examList = reportDao.getRecentExams(termInfoId, null, params);
		JSONObject data = null;
		String kslc = null;
		if (examList!=null) {
			for (int i = 0; i < examList.size(); i++) {
				JSONObject exam = examList.get(i);
				kslc = exam.getString("kslcdm");
				autoIncr = exam.getInteger("autoIncr");
				params.put("autoIncr", autoIncr);
				params.put("kslc", kslc);
				Integer cnt = reportDao.getScoreinfoCnt(termInfoId ,autoIncr ,  params);
				if (cnt > 0) {
					data = reportDao.getRecentClassExamInfo(termInfoId, autoIncr, params);
					data.put("examDate", exam.getDate("cdate"));
					data.put("examName", exam.getString("kslcmc"));
					break ;
				}
			}
		}
		if (data == null) {
			data = new JSONObject();
		}
	
		return data;
	}

	// 获取学生最近考试信息
	@Override
	public List<JSONObject> getRecentStuExamInfo(List<JSONObject> examList , Long xh ) {
		List<JSONObject> list =null;
		List<JSONObject> listTmp =null;
		String kslc = null;
		Integer autoIncr = null;
		String termInfoId = null;
 
		int count = 0 ;
		HashMap<String, Integer> map = null;
		if (examList!=null && examList.size() > 0) {
			JSONObject  params = new JSONObject();
			for (int i = 0; i < examList.size(); i++) {
				JSONObject exam = examList.get(i);
				if (exam.getInteger("examType") != 1) {
					continue;
				}
				kslc = exam.getString("examId");
				autoIncr = exam.getInteger("autoIncr");
				termInfoId = exam.getString("termInfoId");
				params.put("autoIncr", autoIncr);
				params.put("kslc", kslc);
				params.put("termInfoId", termInfoId);
				params.put("xh", xh);
				count ++ ;
				if (count == 1) {
					list = reportDao.getRecentStuExamInfo(termInfoId, autoIncr, params);//获取最新一次考试
				}else if (count == 2) {
					listTmp = reportDao.getRecentStuExamInfo(termInfoId, autoIncr, params);// 获取上一次考试  最近一次 和上次数据对比
					if (listTmp!=null) {
						map = new HashMap<String, Integer>();
						for (int j = 0; j < listTmp.size(); j++) {
							JSONObject obj = listTmp.get(j);
							map.put(obj.getString("kmdm"), obj.getInteger("bjpm"));
						}
					}
				}else {
					break;
				}
				}
			}
	 
		if (list == null) {
			list = new ArrayList<JSONObject>();
		}else {
			if (map != null) {
				for (int i = 0; i < list.size(); i++) {
					JSONObject obj= list.get(i);
					String kmdm = obj.getString("kmdm");
					if (map.get(kmdm)  != null) {
						obj.put("beforeRank", map.get(kmdm));
					}
				}
			}
		}
	
		return list;
	}

	@Override
	public JSONObject getTeacherScoreAnalysis(JSONObject params) {

		String statisTypeId = params.getString("statisTypeId");
		params.remove("statisTypeId");
		Long schoolId = params.getLong("xxdm");
		String xxdm = params.getString("xxdm");
		String xnxq = params.getString("xnxq");
		Long accountId = params.getLong("accountId");//当前帐号
		String xn = xnxq.substring(0 , 4);
		String classId = params.getString("classId");
 
		Classroom room = commonDataService.getClassById(schoolId, Long.parseLong(classId), xnxq);
		Grade grade = commonDataService.getGradeById(schoolId, room.getGradeId(), xnxq);
		String nj = commonDataService.getSynjByGrade(grade, xn);
		Long deanAccountId = room.getDeanAccountId();//班主任帐号

		List<JSONObject> examList = new ArrayList<JSONObject>();
		List<JSONObject> kslcList = new ArrayList<JSONObject>();
		
		List<String> bhList = new ArrayList<String>();// 班级编号列表
		bhList.add(classId);
 
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("xxdm", xxdm);
			map.put("xnxq", xnxq);
			map.put("nj", nj);
			List<JSONObject> examNameList = new ArrayList<JSONObject>();
			examNameList.addAll(reportDao.getExamineNameList(xnxq, map));
			String upXnxq = getUpXnxq(xnxq);
			long lfirstTermInfoId = Long.parseLong(firstTermInfoId);
			long  lupXnxq = Long.parseLong(upXnxq); 
			if (lupXnxq >=lfirstTermInfoId ) {
				logger.info("lfirstTermInfoId lupXnxq ==> " + lfirstTermInfoId + "" + lupXnxq );
				map.put("xnxq", getUpXnxq(xnxq));//获取上学年学期
				examNameList.addAll(reportDao.getExamineNameList(upXnxq, map));// 
			}
			List<JSONObject> classAverageScoreList = new ArrayList<JSONObject>();
			List<JSONObject> levelStudentNumList = new ArrayList<JSONObject>();

			for (JSONObject m : examNameList) {
				String kslc = m.getString("examId");
				String termInfoId = m.getString("termInfoId");
				JSONObject json = new JSONObject();
				json.put("kslc", kslc);
				json.put("kslcdm", kslc);
				json.put("xnxq", termInfoId);
				json.put("xxdm", xxdm);
				json.put("nj", nj);
				kslcList.add(json);
			}
			Map<String, JSONObject> pointMap = new HashMap<String, JSONObject>();
			for (JSONObject json : kslcList) {
				 
				String termInfoId = json.getString("xnxq");
				DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(termInfoId, json);
				
				 room = commonDataService.getClassById(schoolId, Long.parseLong(classId), termInfoId);
 
				if (degreeInfo == null) {
					continue;
				}
				Integer autoIncr = degreeInfo.getAutoIncr();
				json.put("bhList", bhList);

				JSONObject obj = new JSONObject();
				obj.put("examId", degreeInfo.getKslcdm());
				obj.put("examName", degreeInfo.getKslcmc());
				obj.put("termInfoId", termInfoId);
				examList.add(obj);
				
				Map<String, Object>  examMap = new HashMap<String, Object>();
				examMap.put("kslc", degreeInfo.getKslcdm());
				examMap.put("classId", classId);
				List<JSONObject> fzdmList = reportDao.getBmfzByClassId(termInfoId, autoIncr, examMap);
				List<String> fzbms = new ArrayList<String>();
				
				if(fzdmList != null && fzdmList.size()>0) {
					for (int i = 0; i < fzdmList.size(); i++) {
						if(fzdmList.get(i) != null)
							fzbms.add(fzdmList.get(i).getString("bmfz"));
					}
				}
				
				if (fzbms.size() ==0) {
					continue;
				}
				examMap.put("fzdmList", fzbms);
				logger.info("deanAccountId================================>" + deanAccountId + "|" + accountId);
				 List<Long> accounts = new ArrayList<Long>();
				 accounts.add(deanAccountId);
				 List<AccountLesson> accountLessons = room.getAccountLessons(); 
				 System.out.println( "accountLessons==" + accountLessons );
				 if (accountLessons==null) {
					 continue;
				 }
				 for (int i = 0; i < accountLessons.size(); i++) {
					AccountLesson accountLesson = accountLessons.get(i);
					accounts.add(accountLesson.getAccountId());
				 }
				
				
				if (accounts.contains(accountId)) {// 如果当前是班主任 deanAccountId.equals(accountId)
					JSONObject gradeObject = classScoreReportDao.getDeanScoreAnalyzeGrade(termInfoId, autoIncr, examMap);
					List<JSONObject> classScoreAnalyzeList = classScoreReportDao.getDeanScoreAnalyzeClass(termInfoId, autoIncr, examMap);
					System.out.println( "examMap==" + examMap );
					if (classScoreAnalyzeList==null || classScoreAnalyzeList.size() == 0) {
						continue;
					}
					JSONObject classScoreAnalyze = classScoreAnalyzeList.get(0);
					JSONObject point = new JSONObject();
					if ("01".equals(statisTypeId) || "04".equals(statisTypeId) || "05".equals(statisTypeId)) {
						classAverageScoreList.addAll(reportDao.getClassAverageScoreList(termInfoId, autoIncr, json));
						point.put("pjf" ,   classScoreAnalyze.getString("pjf") +"/" + gradeObject.getString("pjf"));
						point.put("pjfpm" , classScoreAnalyze.getString("pm"));
						point.put("yxl" ,   classScoreAnalyze.getString("yxl") +"/" + gradeObject.getString("yxl"));
						point.put("yxlpm" , classScoreAnalyze.getString("yxlpm"));
						point.put("hgl" ,   classScoreAnalyze.getString("hgl") +"/" + gradeObject.getString("hgl"));
						point.put("hglpm" , classScoreAnalyze.getString("hglpm"));
					} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
						Integer count = reportDao.getExamNormalSubjectCount(termInfoId, autoIncr, json);
						if ("02".equals(statisTypeId)) {
							json.put("allALevel", count + "A");
						}
						if ("03".equals(statisTypeId)) {
							json.put("s6A1BLevel", (count - 1) + "A1B");
						}
						levelStudentNumList.addAll(reportDao.getLevelStudentNumList(termInfoId, autoIncr, json));
					} else {
						throw new CommonRunException(-1, "统计类别参数传递异常，请联系管理员！");
					}
					pointMap.put(degreeInfo.getKslcdm(), point);
				}else {/*
					//是任课老师     // 童石认为是班主任 任课老师都是出一样的
					 
					 List<AccountLesson> accountLessons = room.getAccountLessons(); 
					 Long subjectId = null;
					 for (int i = 0; i < accountLessons.size(); i++) {
						AccountLesson accountLesson = accountLessons.get(i);
						if (accountLesson.getAccountId() == accountId) {
							subjectId = accountLesson.getLessonId();
							break;
						}
					 }
					logger.info("subjectId================================>" + subjectId);
					if (subjectId == null) {
						return new JSONObject();
					}
					examMap.put("kmdm", subjectId);
					JSONObject scoreAnalyzeGrade = classScoreReportDao.getTeacherScoreAnalyzeGrade(termInfoId, autoIncr, examMap);
					logger.info("scoreAnalyzeGrade================================>" + scoreAnalyzeGrade);
					if (scoreAnalyzeGrade==null) {
						continue;
					}
					JSONObject scoreAnalyzeClass =  classScoreReportDao.getTeacherScoreAnalyzeClass(termInfoId, autoIncr, examMap);
					Map<String, Double> classScoreMap = new HashMap<String, Double>();
					classScoreMap.put("pjzg",  0.0 );//平均最高
					classScoreMap.put("zgf", 0.0);//最高分
 
					JSONObject point = new JSONObject();
				 
					if ("01".equals(statisTypeId) || "04".equals(statisTypeId) || "05".equals(statisTypeId)) {
						classAverageScoreList.addAll(reportDao.getClassAverageScoreList(termInfoId, autoIncr, json));
						point.put("pjf" ,   scoreAnalyzeClass.getString("pjf") +"/" + scoreAnalyzeGrade.getString("pjf"));
						point.put("pjfpm" , scoreAnalyzeClass.getString("pm"));
						point.put("yxl" ,   scoreAnalyzeClass.getString("yxl") +"/" + scoreAnalyzeGrade.getString("yxl"));
						point.put("yxlpm" , scoreAnalyzeClass.getString("yxlpm"));
						point.put("hgl" ,   scoreAnalyzeClass.getString("hgl") +"/" + scoreAnalyzeGrade.getString("hgl"));
						point.put("hglpm" , scoreAnalyzeClass.getString("hglpm"));
					} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
						Integer count = reportDao.getExamNormalSubjectCount(termInfoId, autoIncr, json);
						if ("02".equals(statisTypeId)) {
							json.put("allALevel", count + "A");
						}
						if ("03".equals(statisTypeId)) {
							json.put("s6A1BLevel", (count - 1) + "A1B");
						}
						logger.info(" autoIncr  json  " +  json);
						levelStudentNumList.addAll(reportDao.getLevelStudentNumList(termInfoId, autoIncr, json));
					} else {
						throw new CommonRunException(-1, "统计类别参数传递异常，请联系管理员！");
					}
					pointMap.put(degreeInfo.getKslcdm(), point);
				*/}
				 
			}
			
			 

			Map<String, JSONObject> classAverageScoreMap = new HashMap<String, JSONObject>();
			if (CollectionUtils.isNotEmpty(classAverageScoreList)) {
				for (JSONObject classAverageScore : classAverageScoreList) {// 把数据放置到Map中
					if (classAverageScore == null) {
						continue;
					}
					String examId = classAverageScore.getString("examId");
					classId = classAverageScore.getString("classId");
					String key = examId + classId;
					if (!classAverageScoreMap.containsKey(key)) {
						classAverageScoreMap.put(key, classAverageScore);
					}
				}
			}

			Map<String, JSONObject> levelStudentNumMap = new HashMap<String, JSONObject>();
			if (CollectionUtils.isNotEmpty(levelStudentNumList)) {
				for (JSONObject levelStudentNum : levelStudentNumList) {// 把数据放置到Map中
					if (levelStudentNum == null) {
						continue;
					}
					String examId = levelStudentNum.getString("examId");
					classId = levelStudentNum.getString("classId");
					String key = examId + classId;
					if (!levelStudentNumMap.containsKey(key)) {
						levelStudentNumMap.put(key, levelStudentNum);
					}
				}
			}
			
			// 循环班级列表，统计每个列表的曲线数据
			String[] xAxis = new String[examList.size()];// x轴标识，是一个字符型数组
			String identifier = "";
			for (int i = 0; i < examList.size(); i++) {
				// 添加考试数据
				JSONObject exam = examList.get(i);
				if (exam == null) {
					continue;
				}

				identifier = "T" + (i + 1);
				exam.put("identifier", identifier);

				// 设置x坐标的值
				xAxis[i] = identifier;
			}
 
			// 班级的曲线图数据
			JSONObject item = new JSONObject();
			// 设置classId:班级代码
			item.put("classId", room.getId());
			// 设置title:表名
			if ("01".equals(statisTypeId)) {// 01: 总平均分排名
				item.put("title", "历次考试总平均分排名趋势表(" + room.getClassName() + ")");
			} else if ("02".equals(statisTypeId)) {
				item.put("title", "历次考试全A人数趋势表(" + room.getClassName() + ")");
			} else if ("03".equals(statisTypeId)) {
				item.put("title", "历次考试次A1B人数趋势表(" + room.getClassName() + ")");
			} else if ("04".equals(statisTypeId)) {
				item.put("title", "历次考试次合格率趋势表(" + room.getClassName() + ")");
			} else {
				item.put("title", "历次考试次优秀率趋势表(" + room.getClassName() + ")");
			}

			List<JSONObject> seriesList = new ArrayList<JSONObject>();// 曲线数据列表
			JSONObject series = new JSONObject();// 曲线数据

			List<JSONObject> dataList = new ArrayList<JSONObject>();

			// 表示总平均分排名趋势线是否删除了y值为零的节点。如果删除了，就要把x轴的列表设置换成新的列表。
			boolean isDeletePoint = false;

			List<JSONObject> classAverScoreRankexamList = new ArrayList<JSONObject>();// 新的班级总平均分排名的考试列表

			List<Float> x = new ArrayList<Float>();// x轴坐标集合
			List<Float> y = new ArrayList<Float>();// y轴坐标集合

			int j = 0;
			for (int i = 0; i < examList.size(); i++) {
				// 添加考试数据
				JSONObject exam = examList.get(i);
				String examId = exam.getString("examId");
				// 设置曲线的点值
				if ("01".equals(statisTypeId)) {
					series.put("name", "排名");
				} else if ("02".equals(statisTypeId) || "03".equals(statisTypeId)) {
					series.put("name", "人数");
				} else if ("04".equals(statisTypeId)) {
					series.put("name", "合格率");
				} else {
					series.put("name", "优秀率");
				}

				// 计算曲线x,y坐标
				JSONObject data = new JSONObject();
				float yValue = getYValue(classAverageScoreMap, levelStudentNumMap, examId,
						String.valueOf(room.getId()), statisTypeId);
				if (yValue == -1000  && ("02".equals(statisTypeId) || "03".equals(statisTypeId))) {
					continue; // 没有数据
				}
              
				// 如果是总平均分排名，并且点的y值小于0，则不添加这次考试
				if ("01".equals(statisTypeId) && yValue < 1) {
					isDeletePoint = true;
					continue;
				}

				data.put("name", exam.get("identifier") + ":" + exam.get("examName"));
				data.put("x", j);
				x.add(Float.valueOf(j + 1));
				data.put("y", yValue);
				y.add(yValue);
				classAverScoreRankexamList.add(exam);
				JSONObject pint = pointMap.get(examId);
				if (pint == null) {
					continue;
				}
 
				data.put("averageScore", pint.getString("pjf"));
				data.put("averageRank", pint.getString("pjfpm"));
				
				data.put("excellentRate", pint.getString("yxl"));
				data.put("excellentRank", pint.getString("yxlpm"));
			 
				data.put("passRate", pint.getString("hgl"));
				data.put("passRank", pint.getString("hglpm"));
 
				dataList.add(data);
				j++;
			}

			series.put("data", dataList);

			// 计算趋势线的起点坐标和终点坐标
			float[][] startEnd = MathUtil.trendLineCoordinate(x, y);

			JSONObject trendLineSeries = new JSONObject();// 趋势线线数据
			List<JSONObject> dataList2 = new ArrayList<JSONObject>();

			trendLineSeries.put("name", "趋势");

			if (x.size() > 1) {
				JSONObject start = new JSONObject();// 起点坐标
				start.put("name", "T" + Math.round(startEnd[0][0]));
				start.put("x", Math.round(startEnd[0][0]) - 1);
				start.put("y", startEnd[0][1]);
				dataList2.add(start);

				JSONObject end = new JSONObject();// 终点坐标
				end.put("name", "T" + Math.round(startEnd[1][0]));
				end.put("x", Math.round(startEnd[1][0]) - 1);
				end.put("y", startEnd[1][1]);
				dataList2.add(end);
			}

			trendLineSeries.put("data", dataList2);

			seriesList.add(series);
			//seriesList.add(trendLineSeries);

			// x轴标识，是一个字符型数组

			String[] classAverScoreRankXAxis = new String[classAverScoreRankexamList.size()];// 新的班级总平均分排名的x轴列表

			if (isDeletePoint) {// 如果删除掉了部分y值为零的点，则把列表设成下面的x轴
				for (int i = 0; classAverScoreRankexamList != null && i < classAverScoreRankexamList.size(); i++) {
					// 添加考试数据
					JSONObject exam = classAverScoreRankexamList.get(i);
					// 设置x坐标的值
					classAverScoreRankXAxis[i] = StringUtil.transformString(exam.get("identifier"));
				}

				item.put("xAxis", classAverScoreRankXAxis);
			} else {
				item.put("xAxis", xAxis);
			}

			// 设置考试列表
			List<Object> list = new ArrayList<Object>();
		 
			if (isDeletePoint) {
				for (JSONObject json : classAverScoreRankexamList) {
					list.add(json.clone());
				}
			} else {
				for (JSONObject json : examList) {
					list.add(json.clone());
				}
			}
			item.put("examList", list);
			// 设置曲线图形数据
			item.put("Series", seriesList);
			
			if (list.size() == 0) {
				item = new JSONObject();
				return item;
			}
		return item;
	}

	@Override
	public JSONObject getStudentScoreAnalysis(JSONObject params) {
		List<JSONObject> examList = new ArrayList<JSONObject>();
		String termInfoId = params.getString("xnxq");
		String studentId =  params.getString("studentId");
		String schoolId = params.getString("xxdm"); 
		List<DegreeInfo> degreeInfoList = new ArrayList<DegreeInfo>();
		degreeInfoList.addAll(scoreDao.getDegreeInfoList(termInfoId, params));
		//Account account = commonDataService.getAccountById(Long.parseLong(schoolId), Long.parseLong(studentId), termInfoId);
		List<Long> accountIds = new ArrayList<Long>();
		accountIds.add(Long.parseLong(studentId));
		List<Account> accounts = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
		Account account = accounts.get(0);
		List<User> userList = account.getUsers();
		logger.info("account==" + account); 
		logger.info("userList==" + userList);
		Long classId = 0L;
		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			StudentPart part = user.getStudentPart();
			if (part != null) {
				classId = part.getClassId();
				break; 
			}
		}
		String upXnxq = getUpXnxq(termInfoId);
		
		long lfirstTermInfoId = Long.parseLong(firstTermInfoId);
		long  lupXnxq = Long.parseLong(upXnxq); 
		if (lupXnxq >=lfirstTermInfoId) {
			params.put("xnxq", upXnxq);//获取上一学年学期的考试
			degreeInfoList.addAll(scoreDao.getDegreeInfoList(upXnxq, params));// 
		}
		for (DegreeInfo degreeInfo : degreeInfoList) {
			Integer autoIncr = degreeInfo.getAutoIncr();
			String examId = degreeInfo.getKslcdm();
			termInfoId = degreeInfo.getXnxq();
			params.put("examId", examId);
			params.put("schoolId", schoolId);
			params.put("termInfoId", degreeInfo.getXnxq());
			if (reportDao.ifExistsDegreeInfoByStudId(termInfoId, autoIncr, params)) {
				JSONObject json = new JSONObject();
				json.put("examId", examId);
				json.put("examName", degreeInfo.getKslcmc());
				json.put("examType", 1); // 校考
				json.put("createTime", degreeInfo.getCdate());
				json.put("termInfoId", termInfoId);
				json.put("autoIncr", degreeInfo.getAutoIncr());//
				json.put("fbpmflag", degreeInfo.getFbpmflag());
				examList.add(json);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xh", studentId);
		Iterator<JSONObject> iterator = examList.iterator();
		HashMap<String, JSONObject> studentStatisticsrankMap = new HashMap<String, JSONObject>();
		while (iterator.hasNext()) {
			JSONObject obj = iterator.next();
			map.put("kslc", obj.getString("examId"));
			JSONObject studentStatisticsrank = reportDao.getStudentStatisticsrank(obj.getString("termInfoId"), obj.getInteger("autoIncr"), map);
			if (studentStatisticsrank==null) {
				iterator.remove();
				continue;
			}
			studentStatisticsrankMap.put(studentStatisticsrank.getString("kslc"), studentStatisticsrank);
		}
		
		
		
		// 循环班级列表，统计每个列表的曲线数据
		String[] xAxis = new String[examList.size()];// x轴标识，是一个字符型数组
		String identifier = "";
		for (int i = 0; i < examList.size(); i++) {
			// 添加考试数据
			JSONObject exam = examList.get(i);
			if (exam == null) {
				continue;
			}
			identifier = "T" + (i + 1);
			exam.put("identifier", identifier);
			// 设置x坐标的值
			xAxis[i] = identifier;
		}
		
		JSONObject item = new JSONObject();
 
		List<JSONObject> seriesList = new ArrayList<JSONObject>();// 曲线数据列表
		JSONObject series = new JSONObject();// 曲线数据
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		
		List<Float> x = new ArrayList<Float>();// x轴坐标集合
		List<Float> y = new ArrayList<Float>();// y轴坐标集合
		Map<String, Object> examMap = new HashMap<String, Object>();
		for (int i = 0; i < examList.size(); i++) {
			JSONObject exam = examList.get(i);
			String examId = exam.getString("examId");
			series.put("name", "排名");
			// 计算曲线x,y坐标
			JSONObject data = new JSONObject();
			JSONObject rankObj = studentStatisticsrankMap.get(examId);
			data.put("name", exam.get("identifier") + ":" + exam.get("examName"));
			data.put("x", i);
			x.add(Float.valueOf(i + 1));
			data.put("y", rankObj.getInteger("njpm"));
			y.add(Float.valueOf(rankObj.getInteger("njpm")));
			examMap.put("kslc", exam.getString("examId"));
			termInfoId = exam.getString("termInfoId");
			List<JSONObject> classScoreAnalyzeList = classScoreReportDao.getDeanScoreAnalyzeClass(termInfoId, exam.getInteger("autoIncr"), examMap);
			double highScore = 0.0;
			double myClassHighScore = 0.0;
			for (int j = 0; j < classScoreAnalyzeList.size(); j++) {
				JSONObject object = classScoreAnalyzeList.get(j);
				if (object.getLongValue("bh") == classId) {
					myClassHighScore = object.getDoubleValue("zgf");
				}
				highScore = highScore < object.getDoubleValue("zgf")?object.getDoubleValue("zgf"):highScore;
			}
			DecimalFormat df = new DecimalFormat("#.0");
			
			data.put("highScore", df.format(myClassHighScore)+ "/" + df.format(highScore));// 班级最高分 / 年级最高分
			if (exam.getInteger("fbpmflag")!= null && 1 == exam.getInteger("fbpmflag")) {
				data.put("rank", rankObj.getString("bjpm")+"/" + rankObj.getString("njpm"));
			}
			data.put("totalScore",df.format(rankObj.getDouble("zf")) );
			
			dataList.add(data);
		}
		series.put("data", dataList);
		
		// 计算趋势线的起点坐标和终点坐标
		float[][] startEnd = MathUtil.trendLineCoordinate(x, y);
		JSONObject trendLineSeries = new JSONObject();// 趋势线线数据
		List<JSONObject> dataList2 = new ArrayList<JSONObject>();
		trendLineSeries.put("name", "趋势");
		
		if (x.size() > 1) {
			JSONObject start = new JSONObject();// 起点坐标
			start.put("name", "T" + Math.round(startEnd[0][0]));
			start.put("x", Math.round(startEnd[0][0]) - 1);
			start.put("y", startEnd[0][1]);
			dataList2.add(start);
			JSONObject end = new JSONObject();// 终点坐标
			end.put("name", "T" + Math.round(startEnd[1][0]));
			end.put("x", Math.round(startEnd[1][0]) - 1);
			end.put("y", startEnd[1][1]);
			dataList2.add(end);
		}

		trendLineSeries.put("data", dataList2);

		seriesList.add(series);
		//seriesList.add(trendLineSeries);
		
		item.put("examList", examList);
		item.put("Series", seriesList);
		item.put("xAxis", xAxis);
 
		return item;
	}

	@Override
	public Map<String, List<JSONObject>> getAllHisSubjectfullScore(JSONObject params) {
		Map<String, List<JSONObject>> allSubjectFullScoreMap = new HashMap<String, List<JSONObject>>();
		String termInfoId = params.getString("termInfoId");
		String currentTermInfo = firstTermInfoId;// params.getString("firstTermInfo");
		Long classId = params.getLong("classId");
		Long schoolId =  params.getLong("schoolId");
		JSONObject param = new JSONObject(); 
		int exit = 0;
		Map<String, Classroom> classMap = new HashMap<String, Classroom>();
		Map<String, Grade> gradeMap = new HashMap<String, Grade>();
		while (true) {
			if (currentTermInfo.equals(termInfoId)) {//如果当前学年学期等于遍历到的学年学期 退出循环
	    		  exit = 1;
			}
			 
			Classroom classroom =  classMap.get(schoolId + classId + currentTermInfo);
			if (classroom == null) {
				classroom = commonDataService.getClassById(schoolId, classId, currentTermInfo);
				classMap.put(schoolId + classId + currentTermInfo, classroom);
			} 
			  
			if (classroom == null && exit != 1) {
				currentTermInfo = getNextXnxq(currentTermInfo);
				continue;
			}
			Long gradeId = classroom.getGradeId();
			Grade grade = gradeMap.get(schoolId +  gradeId + currentTermInfo);
			if (grade == null) {
				grade = commonDataService.getGradeById(schoolId, gradeId, currentTermInfo);
				gradeMap.put(schoolId +  gradeId + currentTermInfo, grade);
			}
			 
			String synj = commonDataService.getSynjByGrade(grade, currentTermInfo.substring(0 , 4));
			params.put("synj", synj);
			List<JSONObject> list = reportDao.getExamListBynj(currentTermInfo ,null, params);
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					param.put("kslc", list.get(i).getString("kslcdm"));
					param.put("classId", classId);
					List<JSONObject> fzdmList = reportDao.getBmfzByClassId(currentTermInfo , list.get(i).getInteger("autoIncr") , param);
					if (fzdmList != null && fzdmList.size() > 0) {// 分组代码不为空
						List<String> fzdms = new ArrayList<String>();
						for (int j = 0; j < fzdmList.size(); j++) {
							fzdms.add(fzdmList.get(j).getString("bmfz"));
						}
						param.put("fzdmList",fzdms) ;
						List<JSONObject> scoreList = reportDao.getSubjectfullScoreByfzdm(currentTermInfo , list.get(i).getInteger("autoIncr") , param );
						if (scoreList!=null && scoreList.size() > 0) {
							allSubjectFullScoreMap.put(list.get(i).getString("kslcdm") , scoreList);
						}
					}
				}
			}
		  currentTermInfo = getNextXnxq(currentTermInfo);
		  if (exit == 1) {
			break;
		  }
		  if (currentTermInfo.equals(termInfoId)) {
			  exit = 1;
		  }
		}
		return allSubjectFullScoreMap;
	}

	@Override
	public Map<String, List<JSONObject>> getAllHisSubjectAverageScore(JSONObject params) {
		Map<String, List<JSONObject>> allSubjectAverageScoreMap = new HashMap<String, List<JSONObject>>();
		String termInfoId = params.getString("termInfoId");
		String currentTermInfo = firstTermInfoId;// params.getString("firstTermInfo");
		Long classId = params.getLong("classId");
		Long schoolId =  params.getLong("schoolId");
		JSONObject param = new JSONObject();
		int exit = 0;
	    while (true) {
	    	if (currentTermInfo.equals(termInfoId)) {//如果当前学年学期等于遍历到的学年学期 退出循环
	    		  exit = 1;
			}
	    	Classroom classroom = commonDataService.getClassById(schoolId, classId, currentTermInfo);
			if (classroom == null && exit != 1) {
				currentTermInfo = getNextXnxq(currentTermInfo);
				continue;
			}
			Long gradeId = classroom.getGradeId();
			Grade grade = commonDataService.getGradeById(schoolId, gradeId, currentTermInfo);
			String synj = commonDataService.getSynjByGrade(grade, currentTermInfo.substring(0 , 4));
			params.put("synj", synj);
			List<JSONObject> list = reportDao.getExamListBynj(currentTermInfo ,null, params);
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					param.put("kslc", list.get(i).getString("kslcdm"));
					param.put("classId", classId);
					List<JSONObject> averageScoreList = reportDao.getAllHisSubjectAverageScore(currentTermInfo ,list.get(i).getInteger("autoIncr"),  param);
					if (averageScoreList!=null && averageScoreList.size() > 0) {
						allSubjectAverageScoreMap.put(list.get(i).getString("kslcdm"), averageScoreList);
					}
				}
			}
			  currentTermInfo = getNextXnxq(currentTermInfo);
			  if (exit == 1) {
					break;
			  }
			  if (currentTermInfo.equals(termInfoId)) {
					  exit = 1;
			  }
			
		}
		
		
	 
		
		return allSubjectAverageScoreMap;
	}
	
	
	public static String getNextXnxq(String xnxq) {
		int xn = Integer.parseInt(xnxq.substring(0 , 4));
		int xq = Integer.parseInt(xnxq.substring(4,5));
		if (xq==2) {
			xn = xn + 1;
			xq = 1;
		}else{
			xq = 2;
		}
		return xn + "" + xq;
	}
	
	public static String getUpXnxq(String xnxq) {
		int xn = Integer.parseInt(xnxq.substring(0 , 4));
		int xq = Integer.parseInt(xnxq.substring(4,5));
		if (xq==1) {
			xn = xn - 1;
			xq = 2;
		}else{
			xq = 1;
		}
		return xn + "" + xq;
	}
 
 
 
	
}
