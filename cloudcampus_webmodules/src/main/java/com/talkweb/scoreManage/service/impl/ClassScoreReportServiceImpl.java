package com.talkweb.scoreManage.service.impl;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.business.ClassScoreInParam;
import com.talkweb.scoreManage.dao.ClassScoreReportDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ClassScoreReportService;
import com.talkweb.scoreManage.service.ScoreManageConfigService;

@Service
public class ClassScoreReportServiceImpl implements ClassScoreReportService {
	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private ClassScoreReportDao classScoreReportDao;

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ScoreManageConfigService configService;

	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	@Override
	public JSONObject produceScoreAllIndexDataModel(ClassScoreInParam param) {
		// 1.获取分析表数据
		int topNumber = classScoreReportDao.getTopGourpNumber(param);//
		List<JSONObject> classScoreList = classScoreReportDao.getAllIndexClassScoreList(param);// 班级成绩
		List<JSONObject> gradeScoreList = classScoreReportDao.getAllIndexGradeScorelist(param);// 年级成绩
		List<JSONObject> maxMinScoreEveryCourseList = classScoreReportDao.getMaxMinScoreByCourseList(param);// 各个科目，总分的最高分最低分
		List<JSONObject> everyTeacherAverageList = classScoreReportDao.getFullRangeEveryTeacherAverageList(param);// 各个老师均值
		List<JSONObject> classTeacherList = classScoreReportDao.getClassCourses(param);
		// int
		// statisticsDataSize=statisticsData==null?0:statisticsData.size();//成绩全指标分析表数据记录行数
		List<JSONObject> excellentPassRate = classScoreReportDao.getExcellentPassRate(param.getXxdm());// 优秀率合格率

		EasyUIDatagridHead head[][] = {};
		JSONObject object = new JSONObject();

		ClassScoreInParam newParam = new ClassScoreInParam();

		newParam.setXnxq(param.getXnxq());
		newParam.setSynj(param.getSynj());
		newParam.setKslc(param.getKslc());
		List<JSONObject> dt = classScoreList;
		// 2.生成数据（动态）
		if (classScoreList != null && classScoreList.size() > 0) {
			String tmpfz = classScoreList.get(0).getString("bfz");
			String fz = classScoreList.get(0).getString("bfz");
			String tempmkdm = classScoreList.get(0).getString("mkdm");
			String cmkdm = classScoreList.get(0).getString("mkdm");
			String fzmc = classScoreList.get(0).getString("bfz");
			String mkmc = classScoreList.get(0).getString("mkmc");
			String bhs = classScoreList.get(0).getString("bhs");
			for (JSONObject classScore : classScoreList) {
				fz = classScore.getString("bfz");
				cmkdm = classScore.getString("mkdm");
				if (cmkdm != tempmkdm || tmpfz != fz) {
					// 得到当前行的索引
					int x = 0;
					for (int i = 0; i < classScoreList.size(); i++) {
						if (bhs.equals(classScoreList.get(i).getString("bhs"))
								&& tempmkdm.equals(classScoreList.get(i).getString("mkdm"))) {
							x = i + 1;
							break;
						}
					}

					JSONObject rowT = new JSONObject();
					rowT.put("fzmc", "");
					rowT.put("kmmc", mkmc);
					rowT.put("mkdm", "全年级");
					rowT.put("mkdm", tempmkdm);
					rowT.put("bfz", tmpfz);
					rowT.put("bhs", "");
					dt.add(x, rowT);// 将新创建的行插入到dt中
					tmpfz = fz;
					tempmkdm = cmkdm;
					fzmc = "";
					mkmc = classScore.getString("mkmc");

				}
				bhs = classScore.getString("bhs");

			}
			JSONObject rowT = new JSONObject();
			rowT.put("fzmc", "");
			rowT.put("kmmc", mkmc);
			rowT.put("mkdm", "全年级");
			rowT.put("mkdm", tempmkdm);
			rowT.put("bfz", tmpfz);
			rowT.put("bhs", "");
			dt.add(rowT);
		}

		if (topNumber > 0) {
			for (JSONObject d : dt) {
				d.put("jz", "");
				d.put("yxljz", "");
				d.put("hgljz", "");
				d.put("dfljz", "");
				d.put("jz1", "");
				d.put("yxljz1", "");
				d.put("hgljz1", "");
				d.put("dfljz1", "");
			}

			for (JSONObject dr1 : dt) {
				String strbh = dr1.getString("bhs");
				if (!strbh.equals("")) {
					if (!dr1.getString("mkmc").equals("总分")) {
						String smkdm = dr1.getString("mkdm");
						String sbh = dr1.getString("bhs");

						List<JSONObject> drv2 = new ArrayList<JSONObject>();
						for (JSONObject classTeacher : classTeacherList) {
							String ssbh = classTeacher.getString("bhs");
							String ssmkdm = classTeacher.getString("mkdm");
							if (ssbh == null)
								sbh = "";
							if (ssmkdm == null)
								ssmkdm = "";
							if (ssbh.equals(sbh) && ssmkdm.equals(smkdm)) {
								drv2.add(classTeacher);
							}
						}
						String jsstr = "";
						String strZgh = "";
						if (drv2 != null && drv2.size() > 0) {
							for (JSONObject dr : drv2) {
								jsstr += jsstr == "" ? dr.getString("xm") : "," + dr.getString("xm");
							}
							strZgh = drv2.get(0).getString("zgh");
						}
						dr1.put("xm", jsstr);

						for (JSONObject teacherAverage : everyTeacherAverageList) {
							if (smkdm.equals(teacherAverage.getString("mkdm"))
									&& strZgh.equals(teacherAverage.getString("zgh"))) {
								JSONObject drv3 = teacherAverage;
								dr1.put("jz", drv3.getString("pjf"));
								dr1.put("hgljz", drv3.getString("hgl"));
								dr1.put("yxljz", drv3.getString("yxl"));
								dr1.put("dfljz", drv3.getString("dfl"));

								dr1.put("jz1", drv3.getString("pjf1"));
								dr1.put("hgljz1", drv3.getString("hgl1"));
								dr1.put("yxljz1", drv3.getString("ysl1"));
								dr1.put("dfljz1", drv3.getString("dfl1"));
								break;
							}
						}
					}
				} else {
					String kmbfz = dr1.getString("bfz");
					String kmdm1 = dr1.getString("mkdm");
					for (JSONObject gradeScore : gradeScoreList) {
						if (kmbfz == null)
							kmbfz = "";
						if (kmdm1 == null)
							kmdm1 = "";
						if (kmbfz.equals(gradeScore.getString("bfz")) && kmdm1.equals(gradeScore.getString("mkdm"))) {
							JSONObject drv = gradeScore;
							dr1.put("ckrs", drv.getString("ckrs"));
							dr1.put("tjrs", drv.getString("tjrs"));
							dr1.put("pjf", drv.getString("pjf"));
							dr1.put("ysrs", drv.getString("yslrs"));
							dr1.put("hgrs", drv.getString("hglrs"));
							dr1.put("dfrs", drv.getString("dflrs"));
							dr1.put("ldxs", drv.getString("ldxs"));
							dr1.put("qfd", drv.getString("qfd"));

							for (JSONObject maxmin : maxMinScoreEveryCourseList) {
								String maxminbfz = maxmin.getString("bfz");
								if (maxminbfz == null)
									maxminbfz = "";
								String maxminmkdm = maxmin.getString("mkdm");
								if (maxminmkdm == null)
									maxminmkdm = "";
								if (maxminbfz.equals(dr1.getString("bfz"))
										&& maxminmkdm.equals(dr1.getString("mkdm"))) {
									JSONObject drvx = maxmin;
									dr1.put("zgf", drvx.getString("zgf"));
									dr1.put("zdf", drvx.getString("zdf"));
									dr1.put("zgf1", drvx.getString("zgf1"));
									dr1.put("zdf1", drvx.getString("zdf1"));
									break;
								}
							}
							dr1.put("yxl", drv.getString("yxl"));
							dr1.put("hgl", drv.getString("hgl"));
							dr1.put("dfl", drv.getString("dfl"));
							dr1.put("pjffc", drv.getString("pjffc"));
							dr1.put("yxlc", drv.getString("yxlc"));
							dr1.put("hglc", drv.getString("hglc"));
							dr1.put("dflc", drv.getString("dflc"));
							dr1.put("ckrs1", drv.getString("ckrs1"));
							dr1.put("tjrs1", drv.getString("tjrs1"));
							dr1.put("pjf1", drv.getString("pjf1"));
							dr1.put("ysrs1", drv.getString("yslrs1"));
							dr1.put("hgrs1", drv.getString("hglrs1"));
							dr1.put("dfrs1", drv.getString("dflrs1"));
							dr1.put("ldxs1", drv.getString("ldxs1"));
							dr1.put("qfd1", drv.getString("qfd1"));
							dr1.put("yxl1", drv.getString("ysl1"));
							dr1.put("hgl1", drv.getString("hgl1"));
							dr1.put("dfl1", drv.getString("dfl1"));
							dr1.put("pjffc1", drv.getString("pjffc1"));
							dr1.put("yxlc1", drv.getString("yxlc1"));
							dr1.put("hglc1", drv.getString("hglc1"));
							dr1.put("dflc1", drv.getString("dflc1"));
							break;
						}

					}

				}
			}
		} else {

			for (JSONObject d : dt) {
				d.put("jz", "");
				d.put("yxljz", "");
				d.put("hgljz", "");
				d.put("dfljz", "");
			}

			for (JSONObject dr1 : dt) {
				String strbh = dr1.getString("bhs");
				if (!strbh.equals("")) {
					if (!dr1.getString("mkmc").equals("总分")) {
						String smkdm = dr1.getString("mkdm");
						String sbh = dr1.getString("bhs");

						String jsstr = "";
						String strZgh = "";
						for (JSONObject classTeacher : classTeacherList) {

							if (sbh == null)
								sbh = "";
							if (smkdm == null)
								smkdm = "";
							if (sbh.equals(classTeacher.getString("bhs"))
									&& smkdm.equals(classTeacher.getString("mkdm"))) {
								jsstr += jsstr == "" ? classTeacher.getString("xm")
										: "," + classTeacher.getString("xm");
							}

						}
						if (classTeacherList != null && classTeacherList.size() > 0)
							strZgh = classTeacherList.get(0).getString("zgh");
						dr1.put("xm", jsstr);
						for (JSONObject average : everyTeacherAverageList) {
							if (smkdm == null)
								smkdm = "";
							if (strZgh == null)
								strZgh = "";
							if (smkdm.equals(average.getString("mkdm")) && strZgh.equals(average.getString("zgh"))) {
								JSONObject drv3 = average;
								dr1.put("jz", drv3.getString("pjf"));
								dr1.put("hgljz", drv3.getString("hgl"));
								dr1.put("yxljz", drv3.getString("ysl"));
								dr1.put("dfljz", drv3.getString("dfl"));
								break;
							}
						}
					}
				} else {
					String kmbfz = dr1.getString("bfz");
					String kmdm1 = dr1.getString("mkdm");
					if (kmdm1 == null)
						kmdm1 = "";
					if (kmbfz == null)
						kmbfz = "";
					for (JSONObject gradeScore : gradeScoreList) {

						if (kmbfz.equals(gradeScore.getString("bfz")) && kmdm1.equals(gradeScore.getString("mkdm"))) {
							JSONObject drv = gradeScore;
							dr1.put("ckrs", drv.getString("ckrs"));
							dr1.put("tjrs", drv.getString("tjrs"));
							dr1.put("pjf", drv.getString("pjf"));
							dr1.put("ysrs", drv.getString("yslrs"));
							dr1.put("hgrs", drv.getString("hglrs"));
							dr1.put("dfrs", drv.getString("dflrs"));
							dr1.put("ldxs", drv.getString("ldxs"));
							dr1.put("qfd", drv.getString("qfd"));
							String dr1bfz = dr1.getString("bfz");
							String dr1mkdm = dr1.getString("mkdm");
							if (dr1bfz == null)
								dr1bfz = "";
							if (dr1mkdm == null)
								dr1mkdm = "";
							for (JSONObject maxmin : maxMinScoreEveryCourseList) {
								if (dr1bfz.equals(maxmin.getString("bfz"))
										&& dr1mkdm.equals(maxmin.getString("mkdm"))) {
									JSONObject drvx = maxmin;
									dr1.put("zgf", drvx.getString("zgf"));
									dr1.put("zdf", drvx.getString("zdf"));
									dr1.put("yxl", drv.getString("yxl"));
									dr1.put("hgl", drv.getString("hgl"));
									dr1.put("dfl", drv.getString("dfl"));
									dr1.put("pjffc", drv.getString("pjffc"));
									dr1.put("yxlc", drv.getString("yxlc"));
									dr1.put("hglc", drv.getString("hglc"));
									dr1.put("dflc", drv.getString("dflc"));
									break;
								}
							}

							break;
						}
					}
				}
			}
		}
		// 3.param参数，部分字段是以逗号分隔的数组，因此在这里根据逗号分隔字段，循环每次去调用函数获取数据
		int statisticsDataSize = dt == null ? 0 : dt.size();
		head = assembleFullRangeIndexStatisTableHeader(topNumber, statisticsDataSize);
		// 4.组织报表说明数据
		String topmsg = "";
		String[] arrsm = { "", "", "", "" };
		for (JSONObject rate : excellentPassRate) {
			arrsm[0] = rate.getString("fs");// 统计方式
			/***
			 * 01 优秀率 02 合格率 03 低分率 04 尖子生 05 潜能生
			 */
			String dm = rate.getString("dm");// 统计方式：1：按满分计算，2：按人数计算
			String zfbfb = rate.getString("zfbfb");// 百分比率
			if ("01".equals(dm)) {
				arrsm[1] = zfbfb;// 优秀比例
			} else if ("02".equals(dm)) {
				arrsm[2] = zfbfb;// 合格比例
			} else if ("03".equals(dm)) {
				arrsm[3] = zfbfb;// 低分比例
			}
		}
		topmsg = arrsm[0] + "|" + arrsm[1] + "|" + arrsm[2] + "|" + arrsm[3];

		// 5.按照给定格式封装成json
		object.put("total", statisticsDataSize);
		object.put("rows", dt);
		object.put("columns", head);
		object.put("topmsg", topmsg);

		return object;
	}

	/****
	 * 分数成绩总分析表
	 * 
	 * @param param
	 * @return
	 */
	public JSONObject produceScoreOnInThreeReportData(School school, JSONObject params) {
		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");
		String xnxq = params.getString("xnxq");
		String nj = params.getString("nj");

		String kmdmStr = params.getString("kmdmStr");
		String bhStr = params.getString("bhStr");
		String fzdmStr = params.getString("fzdmStr");
		params.remove("kmdmStr");
		params.remove("bhStr");
		params.remove("fzdmStr");
		List<String> kmdmList = StringUtil.convertToListFromStr(kmdmStr, ",", String.class);
		params.put("kmdmList", kmdmList);
		List<String> bhList = StringUtil.convertToListFromStr(bhStr, ",", String.class);
		params.put("bhList", bhList);
		List<String> fzdmList = StringUtil.convertToListFromStr(fzdmStr, ",", String.class);
		params.put("fzdmList", fzdmList);

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		// 1.获取数据
		List<JSONObject> classScore = classScoreReportDao.getClassScoreAnalyze(xnxq, autoIncr, params);// 班级成绩分析数据

		EasyUIDatagridHead[][] head = null;
		EasyUIDatagridHead[][] frozenColumns = null;

		Map<String, Object> topMsg = new HashMap<String, Object>();
		JSONObject data = new JSONObject();

		if (CollectionUtils.isEmpty(classScore)) {
			data.put("total", 0);// 总记录数
			data.put("rows", new ArrayList<Object>());// 数据行
			data.put("columns", head);// 表头格式
			data.put("topmsg", topMsg);//
			data.put("frozenColumns", frozenColumns);// 冻结表头
			return data;
		}

		HashMap<String, Object> courseParam = new HashMap<String, Object>();
		courseParam.put("schoolId", schoolId);
		courseParam.put("lessonId", kmdmStr.replaceAll("totalScore", "0"));
		courseParam.put("usedGradeId", nj);
		courseParam.put("termInfoId", xnxq);
		List<Account> teacherList = commonDataService.getCourseTeacherList(courseParam);

		Map<String, Account> classCourseMap = new HashMap<String, Account>();// 存放班级科目老师信息Map
		Map<String, List<String>> teachClasssMap = new HashMap<String, List<String>>();// 老师教的班级数量

		if (teacherList != null) {// 循环老师所教班级和科目列表，并且计算出教师教的班级列表。
			List<JSONObject> classGroupClassList = classScoreReportDao.selectViewScoreClassGroupClassList(xnxq,
					autoIncr, params);// 班级分组下的班级列表
			Map<String, String> classGroupClassMap = new HashMap<String, String>();
			for (JSONObject obj : classGroupClassList) {
				String bh = obj.getString("bh");
				String bmfz = obj.getString("bmfz");
				classGroupClassMap.put(bh, bmfz);
			}
			classGroupClassList = null;

			Set<String> classIdSet = classScoreReportDao.getBhFromScoreInfo(xnxq, autoIncr, params);// 导入的班级列表

			for (Account acc : teacherList) {
				if (acc == null || acc.getUsers() == null) {
					continue;
				}
				String teacherId = String.valueOf(acc.getId());
				for(User user : acc.getUsers()) {
					if(user == null || user.getTeacherPart() == null || user.getTeacherPart().getCourseIds() == null) {
						continue;
					}
					for(Course course : user.getTeacherPart().getCourseIds()) {
						String classId = String.valueOf(course.getClassId());
						String lessonId = String.valueOf(course.getLessonId());
						if(!classIdSet.contains(classId)) {
							continue;
						}
						if (!classCourseMap.containsKey(classId + lessonId)) {
							classCourseMap.put(classId + lessonId, acc);
						}

						String classGroup = classGroupClassMap.get(classId);	// 获取班级分组
						if (classGroup == null) {
							classGroup = "";
						}

						String key = classGroup + "," + teacherId + "," + lessonId;
						if (!teachClasssMap.containsKey(key)) {
							teachClasssMap.put(key, new ArrayList<String>());
						}
						List<String> classList = teachClasssMap.get(key);
						if (!classList.contains(classId)) {
							classList.add(classId);
						}
					}
					break;
				}
			}
		}

		List<JSONObject> gradeScore = classScoreReportDao.getGradeScoreAnalyze(xnxq, autoIncr, params);// 年级成绩分析数据
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
				StringUtil.convertToListFromStr(bhStr, ",", Long.class), xnxq);// 获取班级信息列表
		if (classrooms != null) {
			for (Classroom classroom : classrooms) {// 把科目信息放置到Map
				if (classroom == null) {
					continue;
				}
				classRoomMap.put(classroom.getId(), classroom);
			}
		}

		// 老师信息
		HashMap<String, Object> teacherParam = new HashMap<String, Object>();
		teacherParam.put("schoolId", schoolId);
		teacherParam.put("classId", bhStr);
		teacherParam.put("termInfoId", xnxq);
		List<Account> accounts = commonDataService.getDeanList(teacherParam);

		Map<Long, User> accountMap = new HashMap<Long, User>();// 老师信息存放Map
		if (accounts != null) {
			for (Account account : accounts) {
				if (account == null || account.getUsers() == null) {
					continue;
				}
				long teachId = account.getId();
				for (User user : account.getUsers()) {
					if (user == null || user.getUserPart() == null) {
						continue;
					}
					UserPart userPart = user.getUserPart();
					if (T_Role.Teacher.equals(userPart.getRole())) {
						accountMap.put(teachId, user);
					}
				}
			}
		}

		for (JSONObject grade : gradeScore) {// 循环成绩数据，设置科目名称
			String lessonNo = grade.getString("mkdm");// 科目代码
			if (!"zf".equals(lessonNo)) {// 不是班级总分情况下才需要设置科目名称。
				LessonInfo lessonInfo = lessonInfoMap.get(lessonNo);
				if (lessonInfo != null)
					grade.put("mkmc", lessonInfo.getName());
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
			cscore.put(subjectId + "zgf", cscore.getString("zgf"));
			cscore.put(subjectId + "zdf", cscore.getString("zdf"));
		}

		// 3.组装表头和数据模型
		head = assembleOneThreeRateTableHeader(school.getId(), classScore, params, kmdmList, lessonInfoMap);

		frozenColumns = frozenColumns(school.getId(), "002", head.length);
		List<JSONObject> classDataTable = this.assemleOneThreeRateTableData(classScore, classRoomMap, accountMap,
				teachClasssMap, gradeScore, params);
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

		return data;
	}

	public EasyUIDatagridHead[][] frozenColumns(long schoolId, String type, int length) {

		EasyUIDatagridHead head[][] = {};
		if (type.equals("002")) {
			Map<String, Integer> fieldShowInfo = getFieldShowInfo(String.valueOf(schoolId));// 获取表报表字段显示情况
			head = new EasyUIDatagridHead[1][];
			int i = 1;
			if (!fieldShowInfo.containsKey("fzmc")) {
				i++;
			}
			if (!fieldShowInfo.containsKey("ckrs")) {
				i++;
			}

			head[0] = new EasyUIDatagridHead[i];
			if (!fieldShowInfo.containsKey("fzmc") && !fieldShowInfo.containsKey("ckrs")) {
				head[0][0] = new EasyUIDatagridHead("classGroupName", "组名", "center", 100, length, 1, false);
				head[0][1] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
				head[0][2] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, length, 1, false);
			} else if (!fieldShowInfo.containsKey("ckrs") && fieldShowInfo.containsKey("fzmc")) {
				// head[0][0]= new EasyUIDatagridHead("classGroupName", "组名",
				// "center",100, 4, 1, false);
				head[0][0] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
				head[0][1] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, length, 1, false);
			} else if (!fieldShowInfo.containsKey("fzmc") && fieldShowInfo.containsKey("ckrs")) {
				head[0][0] = new EasyUIDatagridHead("classGroupName", "组名", "center", 100, length, 1, false);
				head[0][1] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
			} else {
				head[0][0] = new EasyUIDatagridHead("className", "班级", "center", 60, length, 1, false);
			}
		} else if (type.equals("006")) {
			Map<String, Integer> fieldShowInfo = getLevlAllFieldShowInfo(String.valueOf(schoolId));// 获取表报表字段显示情况

			head = new EasyUIDatagridHead[1][];
			int i = 2;
			if (!fieldShowInfo.containsKey("fzmc")) {
				i++;
			}
			if (!fieldShowInfo.containsKey("ckrs")) {
				i++;
			}

			head[0] = new EasyUIDatagridHead[i];
			if (!fieldShowInfo.containsKey("fzmc") && !fieldShowInfo.containsKey("ckrs")) {
				head[0][0] = new EasyUIDatagridHead("classGroupName", "组名", "center", 100, 3, 1, false);
				head[0][1] = new EasyUIDatagridHead("className", "班级", "center", 60, 3, 1, false);
				head[0][2] = new EasyUIDatagridHead("classAdviser", "班主任", "center", 60, 3, 1, false);
				head[0][3] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, 3, 1, false);
			} else if (!fieldShowInfo.containsKey("ckrs") && fieldShowInfo.containsKey("fzmc")) {
				// head[0][0]= new EasyUIDatagridHead("classGroupName", "组名",
				// "center",100, 4, 1, false);
				head[0][0] = new EasyUIDatagridHead("className", "班级", "center", 60, 3, 1, false);
				head[0][1] = new EasyUIDatagridHead("classAdviser", "班主任", "center", 60, 3, 1, false);
				head[0][2] = new EasyUIDatagridHead("referenceNum", "考试人数", "center", 60, 3, 1, false);
			} else if (!fieldShowInfo.containsKey("fzmc") && fieldShowInfo.containsKey("ckrs")) {
				head[0][0] = new EasyUIDatagridHead("classGroupName", "组名", "center", 100, 3, 1, false);
				head[0][1] = new EasyUIDatagridHead("className", "班级", "center", 60, 3, 1, false);
				head[0][2] = new EasyUIDatagridHead("classAdviser", "班主任", "center", 60, 3, 1, false);
			} else {
				head[0][0] = new EasyUIDatagridHead("className", "班级", "center", 60, 3, 1, false);
				head[0][1] = new EasyUIDatagridHead("classAdviser", "班主任", "center", 60, 3, 1, false);
			}

		}

		return head;
	}

	/****
	 * 分数成绩总分析表表头格式
	 * 
	 * @param courseDataTable
	 *            所有课程列表
	 * @param param
	 *            输入参数
	 * @return
	 */
	public EasyUIDatagridHead[][] assembleOneThreeRateTableHeader(long schoolId, List<JSONObject> classsScore,
			JSONObject params, List<String> kmdmList, Map<String, LessonInfo> lessonInfoMap) {
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");

		// 1.定义表格二维对象数组,课程数量
		EasyUIDatagridHead head[][] = {};

		if (classsScore == null || classsScore.size() <= 0)
			return null;

		// 2.获取客户数量
		List<JSONObject> courseDataTable = new ArrayList<JSONObject>();
		for (JSONObject json : classsScore) {

			if (this.findDataRow(courseDataTable, json.getString("mkdm"))) {// 存在该课程数据了，就跳过执行
				continue;
			}

			JSONObject object = new JSONObject();
			String subjectId = json.getString("mkdm");
			if (!"zf".equals(subjectId)) {// 不是总分
				LessonInfo lessonInfo = null;
				String subjectName = "";
				if (lessonInfoMap != null)
					lessonInfo = lessonInfoMap.get(subjectId);
				if (lessonInfo != null)
					subjectName = lessonInfo.getName();

				object.put("zwmc", ConvertEmptyString(subjectName));
				object.put("mkdm", subjectId);
				courseDataTable.add(object);

			} else {// 总分
				if (kmdmList.contains("totalScore")) {
					object.put("zwmc", "总分");
					object.put("mkdm", subjectId);
					courseDataTable.add(object);
				}
			}

		}
		// 排序
		Collections.sort(courseDataTable, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				if (!"zf".equals(o1.getString("mkdm")) && !"zf".equals(o2.getString("mkdm"))) {
					Long firstSubjectId = Long.parseLong(o1.getString("mkdm"));
					Long secondSubjectId = Long.parseLong(o2.getString("mkdm"));
					int i = -1;
					if(firstSubjectId>secondSubjectId){
						i =1;
					}
					
					return i;
				}

				return 0;
			}

		});

		int courseNumber = courseDataTable.size();// 课程数量

		// 2.组成表头
		Map<String, Integer> fieldShowInfo = getFieldShowInfo(String.valueOf(schoolId));// 获取表报表字段显示情况
		if (classScoreReportDao.ifExistsTopGroupData(xnxq, autoIncr, params)) {
			int twoRowColumns = fieldShowInfo.get("twoRowColumns");// 表头第二行数据每个科目显示的列数
			int threeRowColumns = fieldShowInfo.get("threeRowColumns");// 表头第三行数据每个科目显示的列数

			head = new EasyUIDatagridHead[4][];
			head[0] = new EasyUIDatagridHead[2];
			head[0][0] = new EasyUIDatagridHead(null, "按范围统计", "center", 0, 1,
					courseNumber * fieldShowInfo.get("eachSubColumns"), false);
			head[0][1] = new EasyUIDatagridHead(null, "全员统计", "center", 0, 1,
					courseNumber * fieldShowInfo.get("eachSubColumns"), false);
			head[1] = new EasyUIDatagridHead[courseNumber * 2];
			head[2] = new EasyUIDatagridHead[courseNumber * twoRowColumns * 2];
			head[3] = new EasyUIDatagridHead[courseNumber * threeRowColumns * 2];

			int i = 0;
			for (JSONObject course : courseDataTable) {

				int scope = 0;// 索引移动幅度，默认为零
				String subjectName = course.getString("zwmc") == null ? "" : course.getString("zwmc");
				head[1][i] = new EasyUIDatagridHead(null, subjectName, "center", 0, 1,
						fieldShowInfo.get("eachSubColumns"), false);
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_staticNumByRange", "统计人数", "center", 65, 2, 1, false);
					scope++;
				}
				if ("zf".equals(course.getString("mkdm"))) {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherNameByRange", "班主任", "center", 65, 2, 1, false);
						scope++;
					}
				} else {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherNameByRange", "任课教师", "center", 65, 2, 1, false);
						scope++;
					}
				}
				if (!fieldShowInfo.containsKey("zdf")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(course.getString("mkdm") + "zdf", "最低分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(course.getString("mkdm") + "zgf", "最高分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "平均", "center", 0, 1,
							fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1,
							fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "合格", "center", 0, 1,
							fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[2][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "低分", "center", 0, 1,
							fieldShowInfo.get("lowColumns"), false);
				}

				int nextScope = 0;// 第三行索引移动幅度，默认为零
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreByRange", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreDifValueByRange", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreMeanValueByRange", "均值", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateByRange", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateDifValueByRange", "率差", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateMeanValueByRange", "均值", "center", 50, 1, 1,
							false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateByRange", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateDifValueByRange", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateMeanValueByRange", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_failNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreNumByRange", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateByRange", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateDifValueByRange", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateRankByRange", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[3][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateMeanValueByRange", "均值", "center", 50, 1, 1,
							false);

				}

				scope = 0;
				head[1][i + courseNumber] = new EasyUIDatagridHead(null, course.getString("zwmc"), "center", 0, 1,
						fieldShowInfo.get("eachSubColumns"), false);
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_staticNum", "统计人数", "center", 55, 2, 1, false);
					scope++;
				}
				if ("zf".equals(course.getString("mkdm"))) {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherName", "班主任", "center", 55, 2, 1, false);
						scope++;
					}
				} else {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherName", "任课教师", "center", 55, 2, 1, false);
						scope++;
					}
				}
				if (!fieldShowInfo.containsKey("zdf")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "zdf", "最低分", "center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "zgf", "最高分", "center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"平均分", "center", 0, 1, fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"优秀率", "center", 0, 1, fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"合格率", "center", 0, 1, fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[2][twoRowColumns * courseNumber + i * twoRowColumns + scope] = new EasyUIDatagridHead(null,
							"低分率", "center", 0, 1, fieldShowInfo.get("lowColumns"), false);
					scope++;
				}

				nextScope = 0;
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScore", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreDifValue", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRate", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRate", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_failNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRate", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[3][threeRowColumns * courseNumber + i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreMeanValue", "均值", "center", 50, 1, 1, false);
				}
				i++;
			}
		} else {

			int twoRowColumns = fieldShowInfo.get("twoRowColumns");// 表头第二行数据每个科目显示的列数
			int threeRowColumns = fieldShowInfo.get("threeRowColumns");// 表头第三行数据每个科目显示的列数

			head = new EasyUIDatagridHead[3][];
			head[0] = new EasyUIDatagridHead[courseNumber];
			head[1] = new EasyUIDatagridHead[courseNumber * twoRowColumns];
			head[2] = new EasyUIDatagridHead[courseNumber * threeRowColumns];
			int i = 0;
			for (JSONObject course : courseDataTable) {

				String subjectName = course.getString("zwmc") == null ? "" : course.getString("zwmc");

				int scope = 0;// 索引移动幅度，默认为零

				head[0][i] = new EasyUIDatagridHead(null, subjectName, "center", 0, 1,
						fieldShowInfo.get("eachSubColumns"), false);
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(course.getString("mkdm") + "_staticNum",
							"统计人数", "center", 65, 2, 1, false);
					scope++;
				}
				if ("zf".equals(course.getString("mkdm"))) {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherName", "班主任", "center", 65, 2, 1, false);
						scope++;
					}
				} else {
					if (!fieldShowInfo.containsKey("teacherName")) {
						head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(
								course.getString("mkdm") + "_teacherName", "任课教师", "center", 65, 2, 1, false);
						scope++;
					}
				}
				if (!fieldShowInfo.containsKey("zdf")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(course.getString("mkdm") + "zdf", "最低分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("zgf")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(course.getString("mkdm") + "zgf", "最高分",
							"center", 55, 2, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "平均", "center", 0, 1,
							fieldShowInfo.get("averageColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1,
							fieldShowInfo.get("excellentColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "合格", "center", 0, 1,
							fieldShowInfo.get("passColumns"), false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "不合格", "center", 0, 1, 1, false);
					scope++;
				}
				if (!fieldShowInfo.containsKey("low")) {
					head[1][i * twoRowColumns + scope] = new EasyUIDatagridHead(null, "低分", "center", 0, 1,
							fieldShowInfo.get("lowColumns"), false);
				}

				int nextScope = 0;// 第三行索引移动幅度，默认为零
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScore", "平均分", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreDifValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreDifValue", "分差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("averageScoreMeanValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRate", "优秀率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDifValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("excellentRateMeanValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateNum")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRate", "合格率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateDifValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("passRateMeanValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_failNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreNum")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreNum", "人数", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRate", "低分率", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreDifValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreDifValue", "率差", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreRateRank", "排名", "center", 50, 1, 1, false);
					nextScope++;
				}
				if (!fieldShowInfo.containsKey("lowScoreMeanValue")) {
					head[2][i * threeRowColumns + nextScope] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_lowScoreMeanValue", "均值", "center", 50, 1, 1, false);
					nextScope++;
				}

				i++;
			}
		}

		// 3.返回
		return head;
	}
	
 

	/***
	 * 分数成绩总分析表字段显示情况
	 * 
	 * @return
	 */
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

	/*****
	 * 分数成绩总分析表
	 * 
	 * @param classScore
	 *            班级成绩分析表
	 * @param classCourse
	 *            课程数据
	 * @param gradeScore
	 *            年级成绩分析数据。
	 * @param param
	 *            前段输入参数
	 * @return
	 */
	public List<JSONObject> assemleOneThreeRateTableData(List<JSONObject> classScore, Map<Long, Classroom> classroomMap,
			Map<Long, User> accountMap, Map<String, List<String>> teachClasssMap, List<JSONObject> gradeScore,
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
					// else if(findDataRow(classDataTable,
					// score.getString("bhs"),
					// score.getString("bmfz"))&&!score.getString("mkdm").equals("zf")){
					//
					// }
					if (classScoreReportDao.ifExistsTopGroupData(xnxq, autoIncr, params)) {// 全范围统计

						dataRow = createOneThreeRateDataRow(score, scr, gradeScore, classroomMap, accountMap,
								teachCourseAverValue, groupCodeTemp);

					} else {// 不包含全范围统计

						dataRow = createOneThreeRateDataRowForNoAll(score, scr, gradeScore, classroomMap, accountMap,
								teachCourseAverValue, groupCodeTemp);
					}

					if (!groupCodeTemp.equals(groupCode))
						groupCodeTemp = groupCode;

					// 添加数据列
					classDataTable.add(dataRow);
				}
				// 最后一组数据循环后添加上全年级分析统计结果
				JSONObject dataRow = null;
				groupCodeTemp = "";// 把分组设置为空，表示最后一组循环结束后，需要统计最后一组的年级分组
				if (classScoreReportDao.ifExistsTopGroupData(xnxq, autoIncr, params)) {// 全范围统计
					dataRow = createOneThreeRateDataRow(scr.get(scr.size() - 1), scr, gradeScore, classroomMap,
							accountMap, teachCourseAverValue, groupCodeTemp);
				} else {// 不包含全范围统计
					dataRow = createOneThreeRateDataRowForNoAll(scr.get(scr.size() - 1), scr, gradeScore, classroomMap,
							accountMap, teachCourseAverValue, groupCodeTemp);
				}
				classDataTable.add(dataRow);
			}

		}
		return classDataTable;
		// if (classScore != null && classScore.size() > 0) {
		// String groupCodeTemp = classScore.get(0).getString("bfz");//
		// 文理分组代码(零时变量)
		// String groupCode = "";// 文理分组编号
		//
		// for (JSONObject score : classScore) {
		// groupCode = score.getString("bfz");
		// JSONObject dataRow = null;
		// // 监测是否已经添加了该分组班级的数据,添加了就跳过后面的处理逻辑，执行下一条记录的处理
		// if (findDataRow(classDataTable, score.getString("bhs"),
		// score.getString("bmfz"))&&!score.getString("mkdm").equals("zf")) {
		// continue;
		// }
		// else if(findDataRow(classDataTable, score.getString("bhs"),
		// score.getString("bmfz"))&&!score.getString("mkdm").equals("zf")){
		//
		// }
		// if (checkScopeStatistics(param)) {// 全范围统计
		//
		// dataRow = createOneThreeRateDataRow(score, classScore,
		// gradeScore, classroomMap, accountMap,
		// teachCourseAverValue, groupCodeTemp);
		//
		// } else {// 不包含全范围统计
		//
		// dataRow = createOneThreeRateDataRowForNoAll(score,
		// classScore, gradeScore, classroomMap, accountMap,
		// teachCourseAverValue, groupCodeTemp);
		// }
		//
		// if (!groupCodeTemp.equals(groupCode))
		// groupCodeTemp = groupCode;
		//
		// // 添加数据列
		// classDataTable.add(dataRow);
		// }
		// // 最后一组数据循环后添加上全年级分析统计结果
		//// JSONObject dataRow = null;
		//// groupCodeTemp = "";// 把分组设置为空，表示最后一组循环结束后，需要统计最后一组的年级分组
		//// if (checkScopeStatistics(param)) {// 全范围统计
		////
		//// dataRow = createOneThreeRateDataRow(
		//// classScore.get(classScore.size() - 1), classScore,
		//// gradeScore, classroomMap, accountMap,
		//// teachCourseAverValue, groupCodeTemp);
		////
		//// } else {// 不包含全范围统计
		////
		//// dataRow = createOneThreeRateDataRowForNoAll(
		//// classScore.get(classScore.size() - 1), classScore,
		//// gradeScore, classroomMap, accountMap,
		//// teachCourseAverValue, groupCodeTemp);
		//// }
		//
		// //classDataTable.add(dataRow);
		//
		// return classDataTable;
		// }

	}
	
 

	/****
	 * 
	 * @param scoreGrade
	 * @param classGrageScore
	 * @param classCourse
	 * @param classOrGradeClass
	 * @return
	 */
	public JSONObject createOneThreeRateDataRow(JSONObject score, List<JSONObject> classScore,
			List<JSONObject> gradeScore, Map<Long, Classroom> classroomMap, Map<Long, User> accountMap,
			Map<String, Map<String, Object>> teachCourseAverValue, String tempGroupCode) {
		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();
		List<JSONObject> scoreList = null;// 成绩信息列表；可能是班级成绩也可能是全年级成绩分级结果
		boolean computerGradeData = false;
		String groupCode = score.getString("bfz");// 文理分组
		String compareClassGroup = "";// 比较的分组，如果是去班级成绩则是班班级分组，如果是年级数据则是文理分组
		if (!tempGroupCode.equals(groupCode) && score.getString("mkdm").equals("zf")) {// 统计全年级成绩
			dataRow.put("bh", "");
			dataRow.put("bfz", tempGroupCode);
			dataRow.put("classGroupName", "");
			computerGradeData = true;
			dataRow.put("className", "全年级");
			scoreList = gradeScore;// 设置为年级成绩分级结果
			compareClassGroup = score.getString("bfz");
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

		for (JSONObject cscore : scoreList) {
			smkdm = ConvertEmptyString(cscore.getString("mkdm"));
			classGroup = ConvertEmptyString(cscore.getString("bmfz"));
			sbh = ConvertEmptyString(cscore.getString("bhs"));

			if (classGroup.equals(compareClassGroup) && (sbh.equals(score.getString("bhs")) || computerGradeData)) {
				dataRow.put(smkdm + "zgf", cscore.get("zgf"));
				dataRow.put(smkdm + "zdf", cscore.get("zdf"));
				Map<String, Object> everyRateAverValue = teachCourseAverValue
						.get(classGroup + "," + cscore.getString("teacherId") + "," + smkdm);// 各个率的均值
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

				// 班级分析成绩数据列，处理下面字段
				if ("zf".equals(smkdm)) {// 总分的时候老师设置为班主任
					long classNo = score.getLongValue("bhs");// 班号

					Classroom classroom = null;// 班级
					String teacherName = "";// 老师名称
					if (classroomMap != null) {
						classroom = classroomMap.get(classNo);
						long teacherId = 0;
						if (classroom != null)
							teacherId = classroom.getDeanAccountId();
						User teacherUser = null;// 老师

						if (accountMap != null)
							teacherUser = accountMap.get(teacherId);
						if (teacherUser != null)
							teacherName = teacherUser.getAccountPart().getName();
					}
					dataRow.put(smkdm + "_teacherName", teacherName);
					dataRow.put(smkdm + "_teacherNameByRange", teacherName);
				} else {
					dataRow.put(smkdm + "_teacherName", cscore.get("xm"));
					dataRow.put(smkdm + "_teacherNameByRange", cscore.get("xm"));
				}
			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	}
	
	
 
	

	/****
	 * 
	 * @param scoreGrade
	 * @param classGrageScore
	 * @param classCourse
	 * @param classOrGradeClass
	 * @return
	 */
	public JSONObject createOneThreeRateDataRowForNoAll(JSONObject score, List<JSONObject> classScore,
			List<JSONObject> gradeScore, Map<Long, Classroom> classroomMap, Map<Long, User> accountMap,
			Map<String, Map<String, Object>> teachCourseAverValue, String tempGroupCode) {
		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();
		List<JSONObject> scoreList = null;// 成绩信息列表；可能是班级成绩也可能是全年级成绩分级结果
		boolean computerGradeData = false;
		String groupCode = score.getString("bfz");
		String compareClassGroup = "";// 比较的分组，如果是去班级成绩则是班班级分组，如果是年级数据则是文理分组
		if (!tempGroupCode.equals(groupCode)) {
			// if(score.getString("mkdm").equals("zf")){
			dataRow.put("bh", "");
			dataRow.put("bfz", tempGroupCode);
			dataRow.put("classGroupName", "");
			dataRow.put("className", "全年级");
			computerGradeData = true;
			scoreList = gradeScore;// 设置为年级成绩分级结果
			compareClassGroup = score.getString("bfz");
		} else {
			dataRow.put("bh", score.getString("bhs"));
			dataRow.put("bmfz", score.getString("bmfz"));
			dataRow.put("classGroupName", score.getString("fzmc"));
			dataRow.put("className", score.getString("bjmc"));
			compareClassGroup = score.getString("bmfz");
			scoreList = classScore;// 设置为班级成绩分级结果
		}

		// 2.循环班级成绩数据或是年级成绩数据，找到制定班级和科目的数据列，并设置到到dataRow中。

		String smkdm = "";// 科目名称
		String bhs = "";// 使用年级
		String classGroup = "";// 班级分组

		for (JSONObject cscore : scoreList) {

			smkdm = ConvertEmptyString(cscore.getString("mkdm"));
			classGroup = ConvertEmptyString(cscore.getString("bmfz"));
			bhs = ConvertEmptyString(cscore.getString("bhs"));

			if (classGroup.equals(compareClassGroup) && (bhs.equals(score.getString("bhs")) || computerGradeData)) {

				dataRow.put(smkdm + "zgf", cscore.get("zgf"));
				dataRow.put(smkdm + "zdf", cscore.get("zdf"));
				Map<String, Object> everyRateAverValue = teachCourseAverValue
						.get(classGroup + "," + cscore.getString("teacherId") + "," + smkdm);// 各个率的均值
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

				// 班级分析成绩数据列，处理下面字段
				if ("zf".equals(smkdm)) {
					long classNo = score.getLongValue("bhs");// 班号
					Classroom classroom = null;// 班级
					String teacherName = "";// 老师名称

					if (classroomMap != null) {
						classroom = classroomMap.get(classNo);
						long teacherId = 0;
						User teacherUser = null;// 老师

						if (classroom != null)
							teacherId = classroom.getDeanAccountId();

						if (accountMap != null)
							teacherUser = accountMap.get(teacherId);
						if (teacherUser != null)
							teacherName = teacherUser.getAccountPart().getName();
					}
					dataRow.put(smkdm + "_teacherName", teacherName);
				} else {
					dataRow.put(smkdm + "_teacherName", cscore.get("xm"));
				}
			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	}
	
 

	/****
	 * 历次分数成绩对比表
	 * 
	 * @param param
	 * @return
	 */
	public JSONObject produceClassScoreData(School school, JSONObject params) {
		String kmdmStr = params.getString("kmdmStr");
		params.remove("kmdmStr");
		int termInfoRange = params.getIntValue("termInfoRange"); // 0:本学期，1:本学年，2:历年

		String nj = params.getString("nj");
		String xnxq = params.getString("xnxq");
		String xxdm = params.getString("xxdm");

		Long schoolId = params.getLong("xxdm");

		List<String> termInfoIds = new ArrayList<String>();
		switch (termInfoRange) {
		case 0: // 本学期
			termInfoIds.add(xnxq);
			break;
		case 1: // 本学年
			String xn = xnxq.substring(0, xnxq.length() - 1);
			termInfoIds.add(xn + 2);
			termInfoIds.add(xn + 1);
			break;
		case 2: // 历年
			termInfoIds.addAll(TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, xnxq));
			break;
		default:
			throw new CommonRunException(-1, "时间跨度数据错误，请联系管理员！");
		}

		// 1.获取班级成绩数据
		List<JSONObject> classScore = new ArrayList<JSONObject>();
		if (CollectionUtils.isNotEmpty(termInfoIds)) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("xxdm", params.get("xxdm"));
			map.put("nj", nj);
			map.put("isTotal", kmdmStr.indexOf("totalScore"));
			map.put("kmdmList", StringUtil.convertToListFromStr(kmdmStr, ",", String.class));
			for (String termInfoId : termInfoIds) {
				map.put("xnxq", termInfoId);
				List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(termInfoId, map);
				for (DegreeInfo degreeInfo : degreeInfoList) {
					map.put("kslc", degreeInfo.getKslcdm());
					map.put("kslcmc", degreeInfo.getKslcmc());
					map.put("cdate", degreeInfo.getCdate());
					Integer autoIncr = degreeInfo.getAutoIncr();
					classScore.addAll(classScoreReportDao.getClassScores(termInfoId, autoIncr, map));
				}
			}
		}

		List<JSONObject> newClassScoreData = new ArrayList<JSONObject>();// 新组织出来的成绩对比数据。
		List<JSONObject> examineList = new ArrayList<JSONObject>();// 考试信息列表

		// 2.组织表头，和新格式的数据列表
		EasyUIDatagridHead[][] head = null;
		if (classScore != null && classScore.size() > 0) {
			// 循环把所有考试的信息放到单独的集合中,
			Set<String> isExistsList = new HashSet<String>();// 是否存在考试编号List

			List<Long> subjectIdList = new ArrayList<Long>();// 科目编号列表
			List<Long> classIdList = new ArrayList<Long>();// 班级编号列表
			Map<String, JSONObject> classScoreMap = new HashMap<String, JSONObject>();// 班级成绩数据Map

			for (JSONObject cscore : classScore) {
				String examId = cscore.getString("examId");
				if (!isExistsList.contains(examId)) {
					JSONObject json = new JSONObject();
					json.put("examId", examId);
					json.put("examName", cscore.getString("examName"));
					json.put("cdate", cscore.get("cdate"));
					examineList.add(json);

					isExistsList.add(examId);
				}

				String subjectId = cscore.getString("subjectId");
				Long classId = cscore.getLong("classId");

				if (!"totalScore".equals(subjectId)) {
					Long subjId = Long.valueOf(subjectId);
					if (!subjectIdList.contains(subjId)) {
						subjectIdList.add(subjId);
					}
				}

				if (!classIdList.contains(classId)) {
					classIdList.add(classId);
				}

				String key = classId + subjectId + examId;
				if (!classScoreMap.containsKey(key)) {
					classScoreMap.put(key, cscore);
				}
			}

			Collections.sort(examineList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					long cdate1 = o1.getLongValue("cdate");
					long cdate2 = o2.getLongValue("cdate");
					return Long.compare(cdate1, cdate2);
				}
			});

			if (!CollectionUtils.isEmpty(examineList)) {
				int i = 1;
				for (Map<String, Object> map : examineList) {
					if (map == null)
						continue;
					map.put("shortName", "T" + i);
					++i;
				}
			}

			// 表头组织
			Map<String, Integer> fieldShowInfo = this.getEveryExamFieldShowInfo(xxdm);// 字段动态显示详情

			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[fieldShowInfo.get("oneRowColuns")];
			head[1] = new EasyUIDatagridHead[examineList.size() * fieldShowInfo.get("twoRowColumns")];

			int scope = 0;// 浮动范围
			if (!fieldShowInfo.containsKey("average")) {
				head[0][scope] = new EasyUIDatagridHead(null, "平均分", "center", 50, 1,
						examineList.size() * fieldShowInfo.get("averageColumns"), true);
				scope++;
			}
			if (!fieldShowInfo.containsKey("excellent")) {
				head[0][scope] = new EasyUIDatagridHead(null, "优秀率", "center", 50, 1,
						examineList.size() * fieldShowInfo.get("excellentColumns"), true);
				scope++;
			}
			if (!fieldShowInfo.containsKey("pass")) {
				head[0][scope] = new EasyUIDatagridHead(null, "合格率", "center", 50, 1,
						examineList.size() * fieldShowInfo.get("passColumns"), true);
				scope++;
			}
			if (!fieldShowInfo.containsKey("low")) {
				head[0][scope] = new EasyUIDatagridHead(null, "低分率", "center", 50, 1,
						examineList.size() * fieldShowInfo.get("lowColumns"), true);
				scope++;
			}
			if (!fieldShowInfo.containsKey("giftedStudentNum")) {
				head[0][scope] = new EasyUIDatagridHead(null, "尖子生人数", "center", 100, 1, examineList.size(), true);
				scope++;
			}
			if (!fieldShowInfo.containsKey("potentialStudentNum")) {
				head[0][scope] = new EasyUIDatagridHead(null, "潜能生人数", "center", 100, 1, examineList.size(), true);
			}

			int h = 0;
			for (Map<String, Object> examInfo : examineList) {
				int index = 0;
				// 平均分
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[1][h * fieldShowInfo.get("averageColumns") + index] = new EasyUIDatagridHead(
							"averageScoreT" + examInfo.get("examId"), examInfo.get("shortName") + "平均分", "center", 100,
							1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[1][h * fieldShowInfo.get("averageColumns") + index] = new EasyUIDatagridHead(
							"averageScoreRankT" + examInfo.get("examId"), examInfo.get("shortName") + "排名", "center",
							60, 1, 1, true);
				}
				// 优秀率
				index = 0;
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[1][examineList.size() * fieldShowInfo.get("averageColumns")
							+ h * fieldShowInfo.get("excellentColumns") + index] = new EasyUIDatagridHead(
									"excellentRateT" + examInfo.get("examId"), examInfo.get("shortName") + "优秀率",
									"center", 100, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[1][examineList.size() * fieldShowInfo.get("averageColumns")
							+ h * fieldShowInfo.get("excellentColumns") + index] = new EasyUIDatagridHead(
									"excellentRateRankT" + examInfo.get("examId"), examInfo.get("shortName") + "排名",
									"center", 60, 1, 1, true);
				}
				// 合格率
				index = 0;
				if (!fieldShowInfo.containsKey("passRate")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns");
					head[1][examineList.size() * beforeColumns + h * fieldShowInfo.get("passColumns")
							+ index] = new EasyUIDatagridHead("passRateT" + examInfo.get("examId"),
									examInfo.get("shortName") + "合格率", "center", 100, 1, 1, true);
					index++;
				}

				if (!fieldShowInfo.containsKey("passRateRank")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns");
					head[1][(examineList.size() * beforeColumns + h * fieldShowInfo.get("passColumns"))
							+ index] = new EasyUIDatagridHead("passRateRankT" + examInfo.get("examId"),
									examInfo.get("shortName") + "排名", "center", 60, 1, 1, true);
				}
				// 低分率
				index = 0;
				if (!fieldShowInfo.containsKey("lowScoreRate")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns")
							+ fieldShowInfo.get("passColumns");
					head[1][examineList.size() * beforeColumns + h * fieldShowInfo.get("lowColumns")
							+ index] = new EasyUIDatagridHead("lowScoreRateT" + examInfo.get("examId"),
									examInfo.get("shortName") + "低分率", "center", 100, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("lowScoreRateRank")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns")
							+ fieldShowInfo.get("passColumns");
					head[1][(examineList.size() * beforeColumns + h * fieldShowInfo.get("lowColumns"))
							+ index] = new EasyUIDatagridHead("lowScoreRateRankT" + examInfo.get("examId"),
									examInfo.get("shortName") + "排名", "center", 60, 1, 1, true);
				}
				// 尖子生人数
				if (!fieldShowInfo.containsKey("giftedStudentNum")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns")
							+ fieldShowInfo.get("passColumns") + fieldShowInfo.get("lowColumns");
					head[1][examineList.size() * beforeColumns + h] = new EasyUIDatagridHead(
							"giftedStudentNumT" + examInfo.get("examId"), examInfo.get("shortName") + "", "center", 100,
							1, 1, true);
				}
				// 潜能生人数
				if (!fieldShowInfo.containsKey("potentialStudentNum")) {
					int beforeColumns = fieldShowInfo.get("averageColumns") + fieldShowInfo.get("excellentColumns")
							+ fieldShowInfo.get("passColumns") + fieldShowInfo.get("lowColumns");
					head[1][examineList.size() * beforeColumns
							+ examineList.size() * fieldShowInfo.get("giftedStudentColumns")
							+ h] = new EasyUIDatagridHead("potentialStudentNumT" + examInfo.get("examId"),
									examInfo.get("shortName") + "", "center", 100, 1, 1, true);
				}
				h++;
			}

			// 获取基础数据，并且把基础数据放置Map中.
			Map<String, String> lessonMap = new HashMap<String, String>();// 存科目信息Map
			lessonMap.put("totalScore", "总分");
			List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(school.getId(), subjectIdList, xnxq);// 科目信息列表
			if (lessonInfos != null) {
				for (LessonInfo lessonInfo : lessonInfos) {
					if (lessonInfo == null) {
						continue;
					}
					lessonMap.put(String.valueOf(lessonInfo.getId()), lessonInfo.getName());
				}
			}

			// List<Classroom>
			// classrooms=commonDataService.getClassroomBatch(school.getId(),classIdList,param.getXnxq());//班级信息列表
			HashMap<String, Object> classListParam = new HashMap<String, Object>();
			classListParam.put("schoolId", schoolId);
			classListParam.put("termInfoId", xnxq);
			classListParam.put("usedGradeId", nj);
			List<Classroom> classrooms = commonDataService.getClassList(classListParam);
			Map<String, Classroom> classroomMap = new HashMap<String, Classroom>();// 班级信息Map
			if (classrooms != null) {
				for (Classroom classroom : classrooms) {
					if (classroom == null) {
						continue;
					}
					classroomMap.put(String.valueOf(classroom.getId()), classroom);
				}
			}

			HashMap<String, Object> teacherParam = new HashMap<String, Object>();
			teacherParam.put("schoolId", schoolId);
			teacherParam.put("lessonId", kmdmStr.replaceAll("totalScore", "0"));
			teacherParam.put("usedGradeId", nj);
			teacherParam.put("termInfoId", xnxq);
			JSONArray teacherList = commonDataService.getCourseTeacherList(school, teacherParam);// 老师信息列表
			Map<String, JSONObject> teacherMap = new HashMap<String, JSONObject>();// 存放老师信息Map结构
			if (teacherList != null) {
				for (Object teacher : teacherList) {// 循环把数据接口以班级编号+科目编号为主键存在Map
					if (teacher == null)
						continue;
					JSONObject teacherJson = (JSONObject) teacher;
					String classId = teacherJson.getString("classId");
					String lessonId = teacherJson.getString("lessonId");

					String key = classId + lessonId;
					if (!teacherMap.containsKey(key))
						teacherMap.put(key, teacherJson);
				}
			}

			// 按照表格要求组织新格式的数据列表
			Set<String> isExistsMap = new HashSet<String>();// 是否存在List
			for (JSONObject score : classScore) {// 循环成绩数据，设置基础数据属性值
				JSONObject json = new JSONObject();
				String subjectId = score.getString("subjectId");
				String classId = score.getString("classId");

				Classroom classroom = classroomMap.get(classId);
				if (classroom == null) {
					continue;
				}

				json.put("subjectId", subjectId);
				if (lessonMap.containsKey(subjectId)) {
					json.put("subjectName", lessonMap.get(subjectId));
				}

				json.put("classId", classId);
				json.put("className", classroom.getClassName());

				String key = classId + subjectId;
				JSONObject teacher = teacherMap.get(key);
				if (teacher != null) {
					json.put("teacherId", teacher.get("teaId"));
					json.put("teacherName", teacher.get("teaName"));
				}

				json.putAll(getClassScoreItemData(examineList, classScoreMap, subjectId, String.valueOf(classId)));

				if (!isExistsMap.contains(key)) {
					newClassScoreData.add(json);
					isExistsMap.add(key);
				}
			}

		}

		// 3.报表说明
		String reportMsg = "";
		for (JSONObject examine : examineList) {
			reportMsg = reportMsg + examine.getString("shortName") + "是" + examine.getString("examName") + "; ";
		}

		Collections.sort(newClassScoreData, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String subjectId1 = o1.getString("subjectId");
				String subjectId2 = o2.getString("subjectId");
				if("totalScore".equals(subjectId1) && !"totalScore".equals(subjectId2)) {
					return 1;
				}
				if(!"totalScore".equals(subjectId1) && "totalScore".equals(subjectId2)) {
					return -1;
				}
				if(!"totalScore".equals(subjectId1) && !"totalScore".equals(subjectId2)) {
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

		// 4。组织成json格式返回
		JSONObject data = new JSONObject();
		data.put("total", newClassScoreData == null ? 0 : newClassScoreData.size());
		data.put("rows", newClassScoreData);
		data.put("columns", head);
		data.put("reportMsg", reportMsg);
		return data;
	}

	/*****
	 * 获取历次成绩趋势表的表头字段显示情况
	 * 
	 * @param schoolId
	 * @return
	 */
	private Map<String, Integer> getEveryExamFieldShowInfo(String schoolId) {
		// 1.定义参数，获取配置详情
		Map<String, Integer> show = new HashMap<String, Integer>();// 存放字段显示情况和列数

		int averageColumns = 2;// 默认平均分的列数是2列
		int excellentColumns = 2;// 默认优秀率的列数是2
		int passColumns = 2;// 合格率默认的列数是2
		int lowColumns = 2;// 低分率默认的列数是2
		int giftedStudentColumns = 1;// 尖子生人数列数
		int potentialStudentColumns = 1;// 潜能生人数
		int oneRowColuns = 6;// 第一行动态列的列数，默认为6

		String config = configService.getReportConfigs("003", schoolId).get("config").toString();// 报表字段是否需要显示配置
		JSONObject fieldAuth = config == null ? null : JSONObject.parseObject(config);

		// 2.根据配置规则，判断字段是否需要显示，同时计算出优秀，合格率，低分率等列需要跨几列。

		// 平均分
		JSONObject average = (JSONObject) fieldAuth.get("pjf");

		if (average != null) {
			String averageScore = average.get("pjfz").toString();
			String averageScoreRank = average.get("pm").toString();

			if ("0".equals(averageScore)) {
				show.put("averageScore", 0);
				averageColumns = averageColumns - 1;
			}

			if ("0".equals(averageScoreRank)) {
				show.put("averageScoreRank", 0);
				averageColumns = averageColumns - 1;
			}

			if ("0".equals(averageScore) && "0".equals(averageScoreRank)) {
				show.put("average", 0);
				oneRowColuns = oneRowColuns - 1;
			}
		}
		// 优秀率
		JSONObject excellent = fieldAuth.getJSONObject("yx");

		if (excellent != null) {
			String excellentRate = excellent.getString("yxl");
			String excellentRateRank = excellent.getString("yxlpm");

			if ("0".equals(excellentRate)) {
				show.put("excellentRate", 0);
				excellentColumns = excellentColumns - 1;
			}
			if ("0".equals(excellentRateRank)) {
				show.put("excellentRateRank", 0);
				excellentColumns = excellentColumns - 1;
			}

			if ("0".equals(excellentRate) && "0".equals(excellentRateRank)) {
				show.put("excellent", 0);
				oneRowColuns = oneRowColuns - 1;
			}
		}

		// 合格率
		JSONObject pass = fieldAuth.getJSONObject("hg");

		if (pass != null) {
			String passRate = pass.getString("hgl");
			String passRateRank = pass.getString("hglpm");

			if ("0".equals(passRate)) {
				show.put("passRate", 0);
				passColumns = passColumns - 1;
			}

			if ("0".equals(passRateRank)) {
				show.put("passRateRank", 0);
				passColumns = passColumns - 1;
			}

			if ("0".equals(passRate) && "0".equals(passRateRank)) {
				show.put("pass", 0);
				oneRowColuns = oneRowColuns - 1;
			}
		}

		// 低分率
		JSONObject low = fieldAuth.getJSONObject("df");

		if (low != null) {
			String lowScoreRate = low.getString("dfl");
			String lowScoreRateRank = low.getString("dflpm");

			if ("0".equals(lowScoreRate)) {
				show.put("lowScoreRate", 0);
				lowColumns = lowColumns - 1;
			}
			if ("0".equals(lowScoreRateRank)) {
				show.put("lowScoreRateRank", 0);
				lowColumns = lowColumns - 1;
			}

			if ("0".equals(lowScoreRate) && "0".equals(lowScoreRateRank)) {
				show.put("low", 0);
				oneRowColuns = oneRowColuns - 1;
			}
		}

		// 尖子生人数
		String giftedStudentNum = fieldAuth.getString("jzsrs");

		if ("0".equals(giftedStudentNum)) {
			show.put("giftedStudentNum", 0);
			giftedStudentColumns = giftedStudentColumns - 1;
			oneRowColuns = oneRowColuns - 1;
		}

		// 尖子生人数
		String potentialStudentNum = fieldAuth.getString("qnsrs");

		if ("0".equals(potentialStudentNum)) {
			show.put("potentialStudentNum", 0);
			potentialStudentColumns = potentialStudentColumns - 1;
			oneRowColuns = oneRowColuns - 1;
		}

		// 3.计算各列需要的跨的列数

		if (averageColumns < 0)
			averageColumns = 0;
		if (excellentColumns < 0)
			excellentColumns = 0;
		if (passColumns < 0)
			passColumns = 0;
		if (lowColumns < 0)
			lowColumns = 0;
		if (giftedStudentColumns < 0)
			giftedStudentColumns = 0;
		if (potentialStudentColumns < 0)
			potentialStudentColumns = 0;
		if (oneRowColuns < 0)
			oneRowColuns = 0;

		// 动态表头第一行需要跨的列数
		int twoRowColumns = averageColumns + excellentColumns + passColumns + lowColumns + giftedStudentColumns
				+ potentialStudentColumns;

		show.put("averageColumns", averageColumns);
		show.put("excellentColumns", excellentColumns);
		show.put("passColumns", passColumns);
		show.put("lowColumns", lowColumns);
		show.put("twoRowColumns", twoRowColumns);
		show.put("oneRowColuns", oneRowColuns);
		show.put("giftedStudentColumns", giftedStudentColumns);
		// 4.返回结果

		return show;

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
			Map<String, JSONObject> classScoreMap, String subjectId, String classId) {

		Map<String, Object> map = new HashMap<String, Object>();

		for (Map<String, Object> examineInfo : examineList) {
			String examId = ConvertEmptyString(examineInfo.get("examId") + "");
			Map<String, Object> score = classScoreMap.get(classId + subjectId + examId);

			if (score != null) {
				map.put("averageScoreT" + examId, score.get("averageScore"));
				map.put("averageScoreRankT" + examId, score.get("averageScoreRank"));
				map.put("excellentRateT" + examId, score.get("excellentRate"));
				map.put("excellentRateRankT" + examId, score.get("excellentRateRank"));
				map.put("passRateT" + examId, score.get("passRate"));
				map.put("passRateRankT" + examId, score.get("passRateRank"));
				map.put("lowScoreRateT" + examId, score.get("lowScoreRate"));
				map.put("lowScoreRateRankT" + examId, score.get("lowScoreRateRank"));
				map.put("giftedStudentNumT" + examId, score.get("giftedStudentNum"));
				map.put("potentialStudentNumT" + examId, score.get("potentialStudentNum"));
			}
		}

		return map;
	}

	/****
	 * 7. 等第总分析表
	 * 
	 * @param param
	 * @return
	 */
	@Override
	public JSONObject produceLevelAllStatisReportData(School school, JSONObject params) {
		String xnxq = params.getString("xnxq");
		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");

		String nj = params.getString("nj");
		String fzdmStr = params.getString("fzdmStr");
		String bhStr = params.getString("bhStr");
		String kmdmStr = params.getString("kmdmStr");

		List<String> fzdmList = StringUtil.convertToListFromStr(fzdmStr, ",", String.class);
		params.put("fzdmList", fzdmList);
		List<String> bhList = StringUtil.convertToListFromStr(bhStr, ",", String.class);
		params.put("bhList", bhList);
		List<String> kmdmList = StringUtil.convertToListFromStr(kmdmStr, ",", String.class);
		params.put("kmdmList", kmdmList);

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		JSONObject data = new JSONObject();

		HashMap<String, Object> courseParam = new HashMap<String, Object>();
		courseParam.put("schoolId", schoolId);
		courseParam.put("lessonId", String.valueOf(kmdmStr).replace("totalScore", "0"));
		courseParam.put("classId", bhStr);
		courseParam.put("termInfoId", xnxq);
		JSONArray classCourse = commonDataService.getCourseTeacherList(school, courseParam);// 班级课程信息列表

		Map<String, JSONObject> classCourseMap = new HashMap<String, JSONObject>();// 班级课程信息列表Map
		Map<String, List<String>> teachClassMap = new HashMap<String, List<String>>();// 教师所教科目的班级列表
		if (classCourse != null) {
			List<JSONObject> classGroupClassList = classScoreReportDao.selectViewScoreClassGroupClassList(xnxq,
					autoIncr, params);// 班级分组下的班级列表
			Map<String, JSONObject> classGroupClassMap = new HashMap<String, JSONObject>();
			for(JSONObject obj : classGroupClassList) {
				String bh = obj.getString("bh");
				classGroupClassMap.put(bh, obj);
			}
			
			Set<String> classIdSet = classScoreReportDao.getBhFromScoreInfo(xnxq, autoIncr, params);// 导入的班级列表

			for (Object object : classCourse) {// 把班级课程任课老师信息放置到Map
				if (object == null) {
					continue;
				}
				JSONObject json = (JSONObject) object;
				String classId = json.getString("classId");
				String lessonId = json.getString("lessonId");
				String teaId = json.getString("teaId");
				
				if (classIdSet.contains(classId)) {// 导入班级里没有该班级 则不加入计算
					if (!classCourseMap.containsKey(classId + lessonId)) {
						classCourseMap.put(classId + lessonId, json);
					}

					JSONObject group = classGroupClassMap.get(classId);
					// 统计出科目任课老师所教的班级
					String key = group.getString("bmfz") + "," + teaId + "," + lessonId;
					if (!teachClassMap.containsKey(key)) {
						teachClassMap.put(key, new ArrayList<String>());
					}
					teachClassMap.get(key).add(classId);
				}
			}
		}

		// 获取老师信息，并且把教师信息放置到Map
		HashMap<String, Object> teacherParam = new HashMap<String, Object>();
		teacherParam.put("schoolId", schoolId);
		teacherParam.put("termInfoId", xnxq);
		teacherParam.put("usedGradeId", nj);

		List<Account> teachers = commonDataService.getCourseTeacherList(teacherParam);// 教师信息列表
		Map<Long, User> teacherMap = new HashMap<Long, User>();// 教师信息存放Map

		if (CollectionUtils.isNotEmpty(teachers)) {
			for (Account account : teachers) {
				if (account == null || account.getUsers() == null) {
					continue;
				}
				
				long accountId = account.getId();
				for (User user : account.getUsers()) {
					if(user == null || user.getUserPart() == null) {
						continue;
					}
					
					UserPart userPart = user.getUserPart();
					if (!T_Role.Teacher.equals(userPart.getRole())) {
						continue;
					}
					
					if (!teacherMap.containsKey(accountId)) {
						teacherMap.put(accountId, user);
					}
				}
			}
		}

		List<JSONObject> gradeScore = classScoreReportDao.getGradeScoreAnalyze(xnxq, autoIncr, params);// 年级成绩分析数据
		List<JSONObject> excellentPassRate = classScoreReportDao.getExcellentPassRate(xxdm);// 优秀率合格率参数

		// 2.设置科目名称（从远程接口获取基础数据）
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoList(school, xnxq);// param.getXxdm()));//制定学校的科目列表
		Map<Long, LessonInfo> lessonInfoMap = new HashMap<Long, LessonInfo>();
		if (CollectionUtils.isNotEmpty(lessonInfos)) {
			for (LessonInfo lessonInfo : lessonInfos) {// 科目信息存放到Map中
				if (lessonInfo == null)
					continue;
				long lessonId = lessonInfo.getId();
				if (!lessonInfoMap.containsKey(lessonId))
					lessonInfoMap.put(lessonId, lessonInfo);
			}
		}

		// 获取老师信息，并且把教师信息放置到Map
		HashMap<String, Object> deanParam = new HashMap<String, Object>();
		deanParam.put("schoolId", schoolId);
		deanParam.put("termInfoId", xnxq);
		deanParam.put("usedGradeId", nj);

		List<Account> deans = commonDataService.getDeanList(deanParam);// 教师信息列表
		Map<Long, User> deanMap = new HashMap<Long, User>();
		
		if(CollectionUtils.isNotEmpty(deans)) {
			for (Account account : deans) {
				if (account == null || account.getUsers() == null) {
					continue;
				}
				long accountId = account.getId();
				for (User user : account.getUsers()) {
					if (!deanMap.containsKey(accountId)) {
						deanMap.put(accountId, user);
					}
				}
			}
		}
		
		// 获取班级信息，并且把班级信息存放到Map，把班主任信息放置到Map
		HashMap<String, Object> classParam = new HashMap<String, Object>();
		classParam.put("schoolId", schoolId);
		classParam.put("termInfoId", xnxq);
		classParam.put("usedGradeId", nj);
		List<Classroom> classrooms = commonDataService.getClassList(classParam);
		
		Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>();
		Map<Long, String> deanTeacherMap = new HashMap<Long, String>();// 班主任老师Map
		if (CollectionUtils.isNotEmpty(classrooms)) {
			for (Classroom classroom : classrooms) {// 班级信息存放到Map
				if (classroom == null) {
					continue;
				}

				if (!classroomMap.containsKey(classroom.getId())) {
					classroomMap.put(classroom.getId(), classroom);

					long deanId = classroom.getDeanAccountId();// 班主任账号ID

					User user = deanMap.get(deanId);
					if (user != null && !deanTeacherMap.containsKey(classroom.getId())) {
						deanTeacherMap.put(classroom.getId(), user.getAccountPart().getName());
					}
				}
			}
		}

		Map<String, JSONObject> gradeScoreMap = new HashMap<String, JSONObject>();
		for (JSONObject grade : gradeScore) {
			String lessonNo = grade.getString("mkdm");// 科目代码

			if (!"zf".equals(lessonNo)) {// 不是班级总分情况下才需要设置科目名称。
				LessonInfo lessonInfo = lessonInfoMap.get(Long.valueOf(lessonNo));
				if (lessonInfo != null) {
					grade.put("mkmc", lessonInfo.getName());
				}
			}

			String classGroup = ConvertEmptyString(grade.getString("bmfz"));// 文理分组

			if (!gradeScoreMap.containsKey(classGroup + lessonNo)) {
				gradeScoreMap.put(classGroup + lessonNo, grade);
			}
		}

		// 1.获取数据
		List<JSONObject> classScore = classScoreReportDao.getClassScoreAnalyze(xnxq, autoIncr, params);// 班级成绩分析数据
		Map<String, JSONObject> classScoreMap = new HashMap<String, JSONObject>();
		for (JSONObject score : classScore) {
			String lessonNo = score.getString("mkdm");// 科目代码
			long classId = score.getLong("bhs");// 班级编号
			String groupId = score.getString("bmfz");

			if (!"zf".equals(lessonNo)) {// 不是班级总分情况下才需要设置科目名称。

				LessonInfo lessonInfo = lessonInfoMap.get(Long.valueOf(lessonNo));
				if (lessonInfo != null) {
					score.put("mkmc", lessonInfo.getName());
				}
			}

			Classroom classroom = classroomMap.get(classId);
			if (classroom != null) {
				score.put("bjmc", classroom.getClassName());
			}

			JSONObject coureseTeach = classCourseMap.get(classId + "" + lessonNo);
			if (coureseTeach != null) {
				score.put("xm", coureseTeach.get("teaName"));
				score.put("teaId", coureseTeach.get("teaId"));
			}

			if (!classScoreMap.containsKey(groupId + "" + classId + lessonNo))
				classScoreMap.put(groupId + "" + classId + lessonNo, score);
		}

		// 3.组装表头和数据模型
		EasyUIDatagridHead[][] head = assembleLevelOneThreeRateTableHeader(classScore, params, kmdmList);
		EasyUIDatagridHead[][] frozenColumns = frozenColumns(school.getId(), "006", 0);
		List<JSONObject> classDataTable = this.assemleLevelAllStatisTableData(classScore, classScoreMap, gradeScoreMap,
				deanTeacherMap, classCourseMap, teachClassMap, params, kmdmList);
		String[] arrsm = new String[] { "", "", "", "" };
		if (excellentPassRate != null && excellentPassRate.size() > 0) {
			arrsm[0] = excellentPassRate.get(0).getString("fs");
			String dm = "";// 代码 01 优秀率 ， 02 合格率，03 低分率， 04 尖子生 05 潜能生
			String totalPeopleRate = "";// 总分/人数百分比
			for (JSONObject rate : excellentPassRate) {
				dm = rate.getString("dm");
				totalPeopleRate = rate.getString("fzbfb");
				if (dm.equals("01")) {
					arrsm[1] = totalPeopleRate;
				} else if (dm.equals("02")) {
					arrsm[2] = totalPeopleRate;
				} else if (dm.equals("03")) {
					arrsm[3] = totalPeopleRate;
				}
			}
		}
		Map<String, Object> topMsg = new HashMap<String, Object>();
		topMsg.put("staticMethod", arrsm[0]);
		topMsg.put("excellentRatio", arrsm[1]);
		topMsg.put("passRatio", arrsm[2]);
		topMsg.put("lowScoreRatio", arrsm[3]);

		Collections.sort(classDataTable, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String classGroupName1 = o1.getString("classGroupName");
				String classGroupName2 = o2.getString("classGroupName");
				if(StringUtils.isBlank(classGroupName1) && StringUtils.isNotBlank(classGroupName2)) {
					return 1;
				}
				if(StringUtils.isNotBlank(classGroupName1) && StringUtils.isBlank(classGroupName2)) {
					return -1;
				}
				if(StringUtils.isNotBlank(classGroupName1) && StringUtils.isNotBlank(classGroupName2)) {
					int result = classGroupName1.compareTo(classGroupName2);
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
		
		// 4.设置返回数据
		data.put("total", classDataTable == null ? 0 : classDataTable.size());// 总记录数
		data.put("rows", classDataTable);// 数据行
		data.put("columns", head);// 表头格式
		data.put("topmsg", topMsg);//
		data.put("frozenColumns", frozenColumns);// 表头格式

		return data;
	}

	/*****
	 * 组装等第等(第总分析表)数据模型， 需要按照班级和分组，统计出对应的各科目的成绩信息，。 多个分组班级就会有多行数据，因此需要一个二维数据模型
	 * 
	 * @param classScore
	 *            班级成绩分析表
	 * @param classCourse
	 *            课程数据
	 * @param gradeScore
	 *            年级成绩分析数据。
	 * @param param
	 *            前段输入参数
	 * @return
	 */
	public List<JSONObject> assemleLevelAllStatisTableData(List<JSONObject> classScore,
			Map<String, JSONObject> classScoreMap, Map<String, JSONObject> gradeScoreMap,
			Map<Long, String> deanTeacherMap, Map<String, JSONObject> classCourseMap,
			Map<String, List<String>> teachClassMap, JSONObject params, List<String> kmdmList) {
		String xnxq = params.getString("xnxq");
		Integer autoIncr = params.getInteger("autoIncr");
		
		if (CollectionUtils.isEmpty(classScore)) {
			return null;
		}

		List<JSONObject> classDataTable = new ArrayList<JSONObject>();// 按照班级分的数据存储模型

		List<JSONObject> gradeRate = this.classScoreReportDao.getAGradeRate(xnxq, autoIncr, params);// 班级单科A等率记录集

		List<JSONObject> groupGradeRate = classScoreReportDao.getAGroupGradeRate(xnxq, autoIncr, params);// 年级单科成绩A等率记录
		Map<String, JSONObject> groupGradeRateMap = new HashMap<String, JSONObject>();// 年级单科成绩A等率Map
		for (JSONObject map : groupGradeRate) {
			String subjectId = map.getString("mkdm");
			String groupId = map.getString("fzdm");
			if (!groupGradeRateMap.containsKey(subjectId + groupId)) {
				groupGradeRateMap.put(subjectId + groupId, map);
			}
		}

		// 计算教师所教科目的均值
		Map<String, JSONObject> averageValueMap = new HashMap<String, JSONObject>();// 按照班级科目存放成绩Map

		for (JSONObject averageValue : classScore) {
			String subjectId = averageValue.get("mkdm").toString();
			String classId = averageValue.get("bhs").toString();

			if (!averageValueMap.containsKey(subjectId + classId)) {
				averageValueMap.put(subjectId + classId, averageValue);
			}
		}

		Map<String, JSONObject> teachCourseAverValue = new HashMap<String, JSONObject>();// 所教科目的均值
		if (teachClassMap != null && teachClassMap.size() > 0) {// 计算老师所教科目的各种率的均值
			for(Map.Entry<String, List<String>> entry : teachClassMap.entrySet()) {
				String key = entry.getKey();// 键值
				String[] tmp = key.split(",");
				String classGroup = tmp[0];// 班级分组
				String lessonId = tmp[2];// 科目编号
				List<String> classList = entry.getValue();// 教师所教科目的班级列表
				float averageScore = 0;
				float excellentRate = 0;
				float passRate = 0;
				float ALevelAverageValue = 0;
				for (String classId : classList) {// 循环班级，导出各种率的和值
					JSONObject averageValue = averageValueMap.get(lessonId + classId);
					JSONObject agradeDataRow = this.findAgradeData(gradeRate, lessonId, classId);
					if (averageValue != null) {
						averageScore += Float.parseFloat(averageValue.get("pjf").toString());
						excellentRate += Float.parseFloat(averageValue.get("yxl").toString());
						passRate += Float.parseFloat(averageValue.get("hgl").toString());
						if (agradeDataRow != null) {
							ALevelAverageValue += Float.parseFloat(agradeDataRow.get("bl").toString());
						}
					}
				}
				// 计算各种率的平均值
				JSONObject value = new JSONObject();
				value.put("averageScoreValue", StringUtil.formatNumber(averageScore / classList.size(), 2));
				value.put("excellentRateAverageValue", StringUtil.formatNumber(excellentRate / classList.size(), 2));
				value.put("passRateAverageValue", StringUtil.formatNumber(passRate / classList.size(), 2));
				value.put("ALevelAverageValue", StringUtil.formatNumber(ALevelAverageValue / classList.size(), 2));
				teachCourseAverValue.put(key, value);
			}
		}

		List<String> subjectList = new ArrayList<String>();
		subjectList.addAll(kmdmList);
		subjectList.add("zf");

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

		for(Map.Entry<String, List<JSONObject>> entry : groupm.entrySet()) {
			List<JSONObject> scr = entry.getValue();

			if (CollectionUtils.isNotEmpty(scr)) {
				String groupCodeTemp = scr.get(0).getString("bfz");// 文理分组代码(零时变量)
				for (JSONObject score : scr) {
					// 监测是否已经添加了该分组班级的数据,添加了就跳过后面的处理逻辑，执行下一条记录的处理
					if (this.findDataRow(classDataTable, score.getString("bhs"), score.getString("bmfz"))) {
						continue;
					}
					JSONObject dataRow = createLevelAllStatisDataRow(score, deanTeacherMap, classScoreMap, subjectList, gradeRate,
							teachCourseAverValue);
					// 添加数据列
					classDataTable.add(dataRow);
				}

				// 处理上面循环中，最后一组分组成绩没有全年级数据行和差值数据行
				JSONObject dataRow = createLevelAllStatisDataRowForGrades(gradeScoreMap, groupCodeTemp, groupGradeRateMap,
						subjectList, "全年级");
				classDataTable.add(dataRow);
			}
		}
		return classDataTable;
	}

	/****
	 * 产生(等第总分析表)表数据行(年级成绩数据行)
	 * 
	 * @param scoreGrade
	 * @param classGrageScore
	 * @param classCourse
	 * @param classOrGradeClass
	 * @return
	 */
	public JSONObject createLevelAllStatisDataRowForGrades(Map<String, JSONObject> gradeScoreMap, String tempGroup,
			Map<String, JSONObject> groupGradeRateMap, List<String> subjectList, String className) {
		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();

		dataRow.put("bh", "");
		dataRow.put("bmfz", tempGroup);
		dataRow.put("classGroupName", "");
		dataRow.put("className", className);

		// 2.循环班级成绩数据或是年级成绩数据，找到班级和科目的数据列，并设置到到dataRow中。

		// 循环科目列表
		for (String subjectId : subjectList) {
			JSONObject cscore = gradeScoreMap.get(tempGroup + subjectId);
			if(cscore == null) {
				continue;
			}
			// 全年级记录需要的参数
			dataRow.put("referenceNum", cscore.get("ckrs"));
			
			String kmdm = ConvertEmptyString(cscore.getString("mkdm"));// 科目名称

			dataRow.put(kmdm + "zgf", cscore.get("zgf"));
			dataRow.put(kmdm + "zdf", cscore.get("zdf"));
			
			dataRow.put(kmdm + "_passNum", cscore.get("hgrs"));	// 合格人数
			dataRow.put(kmdm + "_failNum", Math.abs(cscore.getInteger("tjrs") - cscore.getInteger("hgrs")));	// 不合格人数
			dataRow.put(kmdm + "_excellentRateNum", cscore.get("yxrs"));	// 优秀人数

			dataRow.put(kmdm + "_averageScore", cscore.get("pjf"));
			dataRow.put(kmdm + "_excellentRate", cscore.get("yxl"));
			dataRow.put(kmdm + "_passRate", cscore.get("hgl"));
			dataRow.put(kmdm + "_scoreDiffereValue", cscore.get("pjffc"));
			dataRow.put(kmdm + "_excellentRateDiffereValue", cscore.get("yxlc"));
			dataRow.put(kmdm + "_passRateDiffereValue", cscore.get("hglc"));
			dataRow.put(kmdm + "_staticNum", cscore.get("tjrs"));
			
			if (!"总分".equals(cscore.getString("mkmc"))) {
				Map<String, Object> groupGradeA = groupGradeRateMap.get(kmdm + tempGroup);
				if (groupGradeA != null) {
					dataRow.put(kmdm + "_ALevelNum", groupGradeA.get("rs"));	// A等人数
					dataRow.put(kmdm + "_ALevelRate", groupGradeA.get("bl"));
					dataRow.put(kmdm + "_ALevelRateDiffereValue", groupGradeA.get("lc"));
				}
			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	}

	/****
	 * 产（等第总分析表）数据行
	 * 
	 * @param scoreGrade
	 * @param classGrageScore
	 * @param classCourse
	 * @param classOrGradeClass
	 * @return
	 */
	public JSONObject createLevelAllStatisDataRow(JSONObject score, Map<Long, String> deanTeacherMap,
			Map<String, JSONObject> classScoreMap, List<String> subjectList, List<JSONObject> gradeRate,
			Map<String, JSONObject> teachCourseAverValue) {
		// 1.定义参数，初始化每行数据固定列的值
		JSONObject dataRow = new JSONObject();

		dataRow.put("bh", score.getString("bhs"));
		dataRow.put("bmfz", score.getString("bmfz"));
		dataRow.put("classGroupName", score.getString("fzmc"));
		dataRow.put("className", score.getString("bjmc"));

		// 2.循环班级成绩数据或是年级成绩数据，找到班级和科目的数据列，并设置到到dataRow中。
		// 获取班主任信息
		String deanName = "";// 班主任姓名
		if (deanTeacherMap != null)
			deanName = deanTeacherMap.get(score.getLong("bhs"));

		dataRow.put("classAdviser", deanName);
		dataRow.put("referenceNum", score.getShort("ckrs"));

		// 循环科目列表
		for (String subjectId : subjectList) {
			String classGroup = score.getString("bmfz");
			JSONObject cscore = classScoreMap.get(score.getString("bmfz") + score.getString("bhs") + subjectId);
			if (cscore != null) {
				String kmdm = ConvertEmptyString(cscore.getString("mkdm"));
				String sbh = cscore.getString("bhs");// 班级号
				dataRow.put(kmdm + "zgf", cscore.get("zgf"));
				dataRow.put(kmdm + "zdf", cscore.get("zdf"));
				Map<String, Object> averValue = teachCourseAverValue
						.get(classGroup + "," + cscore.get("teaId") + "," + kmdm);
				// 平均分
				dataRow.put(kmdm + "_averageScore", cscore.get("pjf"));
				dataRow.put(kmdm + "_scoreDiffereValue", cscore.get("pjffc"));// 总分的
				dataRow.put(kmdm + "_averageScoreRank", cscore.get("pm"));
				// 优秀率
				dataRow.put(kmdm + "_excellentRate", cscore.get("yxl"));
				dataRow.put(kmdm + "_excellentRateDiffereValue", cscore.get("yxlc"));// 总分的
				dataRow.put(kmdm + "_excellentRateRank", cscore.get("yxlpm"));

				// 合格率
				dataRow.put(kmdm + "_passRate", cscore.get("hgl"));
				dataRow.put(kmdm + "_passRateDiffereValue", cscore.get("hglc"));// 总分的
				dataRow.put(kmdm + "_passRateRank", cscore.get("hglpm"));

				// 班级分析成绩数据列
				if (!"总分".equals(cscore.getString("mkmc"))) {

					dataRow.put(kmdm + "_teacherName", cscore.get("xm"));

					// 平均分
					dataRow.put(kmdm + "_scoreDiffereValue", cscore.get("pjffc"));
					if (averValue != null)
						dataRow.put(kmdm + "_averageScoreValue", averValue.get("averageScoreValue"));

					// 合格率
					dataRow.put(kmdm + "_passNum", cscore.get("hgrs"));
					dataRow.put(kmdm + "_passRateDiffereValue", cscore.get("hglc"));

					if (averValue != null)
						dataRow.put(kmdm + "_passRateAverageValue", averValue.get("passRateAverageValue"));

					// 不合格人数
					dataRow.put(kmdm + "_failNum", Math.abs(cscore.getInteger("tjrs") - cscore.getInteger("hgrs")));

					dataRow.put(kmdm + "_staticNum", cscore.get("tjrs"));

					// 优秀率
					dataRow.put(kmdm + "_excellentRateNum", cscore.get("yxrs"));
					dataRow.put(kmdm + "_excellentRateDiffereValue", cscore.get("yxlc"));
					if (averValue != null)
						dataRow.put(kmdm + "_excellentRateAverageValue", averValue.get("excellentRateAverageValue"));

					// 差值
					JSONObject agradeDataRow = this.findAgradeData(gradeRate, kmdm, sbh);
					if (agradeDataRow != null) {
						// A等率
						dataRow.put(kmdm + "_ALevelNum", agradeDataRow.get("rs"));
						dataRow.put(kmdm + "_ALevelRate", agradeDataRow.get("bl"));
						dataRow.put(kmdm + "_ALevelRank", agradeDataRow.get("pm"));
						dataRow.put(kmdm + "_ALevelRateDiffereValue", agradeDataRow.get("lc"));
						if (averValue != null)
							dataRow.put(kmdm + "_ALevelAverageValue", averValue.get("ALevelAverageValue"));

					}
				}
			}
		}

		// 3。返回指定的班级分组的数据列
		return dataRow;
	}

	/****
	 * 组装一分三率表表格格式(等第成绩)
	 * 
	 * @param courseDataTable
	 *            所有课程列表
	 * @param param
	 *            输入参数
	 * @return
	 */
	public EasyUIDatagridHead[][] assembleLevelOneThreeRateTableHeader(List<JSONObject> classsScore,
			JSONObject params, List<String> kmdmList) {
		// 1.定义表格二维对象数组,课程数量
		EasyUIDatagridHead head[][] = new EasyUIDatagridHead[3][];

		if (classsScore == null || classsScore.size() <= 0)
			return null;

		// 2.获取客户数量
		List<JSONObject> courseDataTable = new ArrayList<JSONObject>();
		boolean isHaveTotalScore = false;// 是否有总分的数据
		for (JSONObject json : classsScore) {

			if ("zf".equals(json.getString("mkdm"))) {
				isHaveTotalScore = true;
			}

			if (this.findDataRow(courseDataTable, json.getString("mkdm")) || "zf".equals(json.getString("mkdm"))) {// 存在该课程数据了，就跳过执行
				continue;
			}

			JSONObject object = new JSONObject();
			object.put("zwmc", ConvertEmptyString(json.getString("mkmc")));
			object.put("mkdm", ConvertEmptyString(json.getString("mkdm")));
			courseDataTable.add(object);
		}
		// 排序
		
		Collections.sort(courseDataTable, new Comparator<JSONObject>() {
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			if (!"zf".equals(o1.getString("mkdm")) && !"zf".equals(o2.getString("mkdm"))) {
				Long firstSubjectId = Long.parseLong(o1.getString("mkdm"));
				Long secondSubjectId = Long.parseLong(o2.getString("mkdm"));
				int i=-1;
				if(firstSubjectId>secondSubjectId){
					i=1;
				}
				return i;
			}
	
			return 0;
		}

});
		// 单独把总分放置最后添加，这样就保证了总分的科目放置到了列表的末尾

		if (isHaveTotalScore && (kmdmList.size() == 1 && kmdmList.contains("totalScore")
				|| kmdmList.size() > 1)) {
			JSONObject object = new JSONObject();
			object.put("zwmc", "总分");
			object.put("mkdm", "zf");
			courseDataTable.add(object);
		}

		int courseNumber = courseDataTable.size();// 课程数量

		// 2.组成表头
		Map<String, Integer> fieldShowInfo = this.getLevlAllFieldShowInfo(params.getString("xxdm"));// 表格动态字段显示详情

		if (fieldShowInfo.get("totalScoreThreeRowColumns") + fieldShowInfo.get("eachSubColumns") > 0) {
			if (fieldShowInfo.get("totalScoreThreeRowColumns") > 0)
				head[0] = new EasyUIDatagridHead[courseNumber];
			else
				head[0] = new EasyUIDatagridHead[courseNumber - 1];
		} else {
			head[0] = new EasyUIDatagridHead[0];
		}

		if (isHaveTotalScore && (kmdmList.size() == 1 && kmdmList.contains("totalScore")
				|| kmdmList.size() > 1)) {
			head[1] = new EasyUIDatagridHead[((courseNumber - 1) * fieldShowInfo.get("twoRowColumns")
					+ fieldShowInfo.get("totalScoreTwoRowColumns"))];
			head[2] = new EasyUIDatagridHead[((courseNumber - 1) * fieldShowInfo.get("threeRowColumns")
					+ fieldShowInfo.get("totalScoreThreeRowColumns"))];
		} else {
			head[1] = new EasyUIDatagridHead[courseNumber * fieldShowInfo.get("twoRowColumns")];
			head[2] = new EasyUIDatagridHead[courseNumber * fieldShowInfo.get("threeRowColumns")];
		}

		int i = 0;
		for (JSONObject course : courseDataTable) {
			if (!"总分".equals(course.getString("zwmc"))) {
				if (fieldShowInfo.get("eachSubColumns") > 0)
					head[0][i] = new EasyUIDatagridHead(null, course.getString("zwmc"), "center", 0, 1,
							fieldShowInfo.get("eachSubColumns"), true);
			} else {
				if (fieldShowInfo.get("totalScoreThreeRowColumns") > 0)
					head[0][i] = new EasyUIDatagridHead(null, course.getString("zwmc"), "center", 0, 1,
							fieldShowInfo.get("totalScoreThreeRowColumns"), true);
			}
			if (!"总分".equals(course.getString("zwmc"))) {
				int index = 0;

				if (!fieldShowInfo.containsKey("teacherName")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_teacherName", "任课教师", "center", 70, 2, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("staticNum")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_staticNum", "统计人数", "center", 70, 2, 1, true);
					index++;
				}

				if (!fieldShowInfo.containsKey("zgf")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "zgf", "最高分", "center", 70, 2, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("zdf")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "zdf", "最低分", "center", 70, 2, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("average")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "平均分",
							"center", 0, 1, fieldShowInfo.get("averageColumns"), true);
					index++;
				}
				if (!fieldShowInfo.containsKey("pass")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "合格",
							"center", 0, 1, fieldShowInfo.get("passColumns"), true);
					index++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "不合格",
							"center", 0, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("A")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "A等",
							"center", 0, 1, fieldShowInfo.get("AColumns"), true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellent")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "优秀",
							"center", 0, 1, fieldShowInfo.get("excellentColumns"), true);
				}

				index = 0;
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScore", "平均分", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("scoreDiffereValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_scoreDiffereValue", "分差", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreRank", "排名", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("averageScoreValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreValue", "均值", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passNum")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passNum", "人数", "center", 70, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRate", "合格率", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRateDiffereValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateDiffereValue", "率差", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateRank", "排名", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRateAverageValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateAverageValue", "均值", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("failNum")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_failNum", "人数", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("ALevelNum")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_ALevelNum", "人数", "center", 70, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("ALevelRate")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_ALevelRate", "A等率", "center", 55, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("ALevelRateDiffereValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_ALevelRateDiffereValue", "率差", "center", 70, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("ALevelRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_ALevelRank", "排名", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("ALevelAverageValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_ALevelAverageValue", "均值", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateNum")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateNum", "人数", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRate", "优秀率", "center", 55, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDiffereValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateDiffereValue", "率差", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateRank", "排名", "center", 75, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateAverageValue")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateAverageValue", "均值", "center", 75, 1, 1, true);
				}
			} else {
				int index = 0;
				if (!fieldShowInfo.containsKey("totalScoreAverage")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "平均分",
							"center", 0, 1, fieldShowInfo.get("totalScoreAverageColumns"), true);
					index++;
				}
				if (!fieldShowInfo.containsKey("totalScorePass")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "合格",
							"center", 0, 1, fieldShowInfo.get("totalScorePassColumns"), true);
					index++;
				}
				if (!fieldShowInfo.containsKey("totalScoreExcellent")) {
					head[1][i * fieldShowInfo.get("twoRowColumns") + index] = new EasyUIDatagridHead(null, "优秀",
							"center", 0, 1, fieldShowInfo.get("totalScoreExcellentColumns"), true);
				}

				index = 0;
				if (!fieldShowInfo.containsKey("averageScore")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScore", "平均分", "center", 55, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("scoreDiffereValue")) {// 平均分差
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_scoreDiffereValue", "分差", "center", 55, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("averageScoreRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_averageScoreRank", "排名", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRate")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRate", "合格率", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRateDiffereValue")) {// 合格率差
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateDiffereValue", "率差", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("passRateRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_passRateRank", "排名", "center", 50, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRate")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRate", "优秀率", "center", 55, 1, 1, true);
					index++;
				}
				if (!fieldShowInfo.containsKey("excellentRateDiffereValue")) {// 优秀率差
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateDiffereValue", "率差", "center", 55, 1, 1, true);
					index++;

				}

				if (!fieldShowInfo.containsKey("excellentRateRank")) {
					head[2][i * fieldShowInfo.get("threeRowColumns") + index] = new EasyUIDatagridHead(
							course.getString("mkdm") + "_excellentRateRank", "排名", "center", 50, 1, 1, true);
				}
			}

			i++;
		}

		// 3.返回
		return head;
	}

	/***
	 * 等第总分析表字段显示情况
	 * 
	 * @return
	 */
	private Map<String, Integer> getLevlAllFieldShowInfo(String schoolId) {
		// 获取报表显示列数，判断需要显示多少列
		Map<String, Integer> map = new HashMap<String, Integer>();

		String fieldAuthConfig = (String) configService.getReportFieldAuths(schoolId, "006").get("config");// 获取报表字段显示权限
		JSONObject fieldAuths = JSONObject.parseObject(fieldAuthConfig);

		int twoRowColumns = 9;// 默认第二行7列数据
		int threeRowColumns = 20;// 默认第三行20列数据
		int eachSubColumns = 4;// 每科的列数，默认显示统计人数和任课老师两列
		int averageColumns = 4;// 平均分的列数
		int excellentColumns = 5;// 优秀率的列数
		int passColumns = 5;// 合格率列数
		int AColumns = 5;// A等率列数
		int totalScoreTwoRowColumns = 3;// 总分的第二行数据列数
		int totalScoreThreeRowColumns = 9;// 总分第三行数据列数
		int totalScoreAverageColumns = 3;// 总分的平均分显示列数
		int totalScoreExcellentColumns = 3;// 总分的优秀率显示列数
		int totalScorePassColumns = 3;// 总分的合格率的显示列数

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
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
					totalScoreAverageColumns = totalScoreAverageColumns - 1;
				}

				if ("0".equals(averageScoreDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("scoreDiffereValue", 0);
					totalScoreAverageColumns = totalScoreAverageColumns - 1;
					averageColumns = averageColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
				}

				if ("0".equals(rankValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScoreRank", 0);

					averageColumns = averageColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
					totalScoreAverageColumns = totalScoreAverageColumns - 1;
				}

				if ("0".equals(averageMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("averageScoreValue", 0);

					averageColumns = averageColumns - 1;
				}

				if ("0".equals(averageScoreValue) && "0".equals(rankValue)) {
					totalScoreTwoRowColumns = totalScoreTwoRowColumns - 1;
					map.put("totalScoreAverage", 0);
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
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
					totalScoreExcellentColumns = totalScoreExcellentColumns - 1;
				}

				if ("0".equals(excellentRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRate", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentRateDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateDiffereValue", 0);
					excellentColumns = excellentColumns - 1;
					totalScoreExcellentColumns = totalScoreExcellentColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
				}

				if ("0".equals(excellentRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateRank", 0);
					excellentColumns = excellentColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
					totalScoreExcellentColumns = totalScoreExcellentColumns - 1;
				}

				if ("0".equals(excellentMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("excellentRateAverageValue", 0);
					excellentColumns = excellentColumns - 1;
				}

				if ("0".equals(excellentNum) && "0".equals(excellentRank)) {
					totalScoreTwoRowColumns = totalScoreTwoRowColumns - 1;
					map.put("totalScoreExcellent", 0);
				}

				if ("0".equals(excellentNum) && "0".equals(excellentRateDifValue) && "0".equals(excellentRate)
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
					map.put("passNum", 0);
					passColumns = passColumns - 1;
					totalScorePassColumns = totalScorePassColumns - 1;
				}

				if ("0".equals(passRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRate", 0);
					passColumns = passColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
				}

				if ("0".equals(passRateDifValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateDiffereValue", 0);
					passColumns = passColumns - 1;
					totalScorePassColumns = totalScorePassColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
				}

				if ("0".equals(passRateRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateRank", 0);
					passColumns = passColumns - 1;
					totalScorePassColumns = totalScorePassColumns - 1;
					totalScoreThreeRowColumns = totalScoreThreeRowColumns - 1;
				}

				if ("0".equals(passRateMeanValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("passRateAverageValue", 0);
					passColumns = passColumns - 1;
				}

				if ("0".equals(passRate) && "0".equals(passRateRank)) {
					totalScoreTwoRowColumns = totalScoreTwoRowColumns - 1;
					map.put("totalScorePass", 0);
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

			// A等
			JSONObject A = (JSONObject) fieldAuths.get("A");
			if (A != null) {
				String ALevelNum = A.getString("Adrs");
				String ALevelRate = A.getString("Adl");
				String ALevelRateDiffereValue = A.getString("Adlc");
				String ALevelRank = A.getString("Adlpm");
				String ALevelAverageValue = A.getString("Adljz");

				if ("0".equals(ALevelNum)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("ALevelNum", 0);
					AColumns = AColumns - 1;
				}

				if ("0".equals(ALevelRate)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("ALevelRate", 0);
					AColumns = AColumns - 1;
				}

				if ("0".equals(ALevelRateDiffereValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("ALevelRateDiffereValue", 0);
					AColumns = AColumns - 1;
				}

				if ("0".equals(ALevelRank)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("ALevelRank", 0);
					AColumns = AColumns - 1;
				}

				if ("0".equals(ALevelAverageValue)) {
					threeRowColumns = threeRowColumns - 1;
					map.put("ALevelAverageValue", 0);
					AColumns = AColumns - 1;
				}

				if ("0".equals(ALevelNum) && "0".equals(ALevelRate) && "0".equals(ALevelRateDiffereValue)
						&& "0".equals(ALevelRank) && "0".equals(ALevelAverageValue)) {
					twoRowColumns = twoRowColumns - 1;
					map.put("A", 0);
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
		if (AColumns < 0)
			AColumns = 0;

		// 总分
		if (totalScoreTwoRowColumns < 0)
			totalScoreTwoRowColumns = 0;// 总分的第二行数据列数
		if (totalScoreThreeRowColumns < 0)
			totalScoreThreeRowColumns = 0;// 总分第三行数据列数
		if (totalScoreAverageColumns < 0)
			totalScoreAverageColumns = 0;// 总分的平均分显示列数
		if (totalScoreExcellentColumns < 0)
			totalScoreExcellentColumns = 0;// 总分的优秀率显示列数
		if (totalScorePassColumns < 0)
			totalScorePassColumns = 0;// 总分的合格率的显示列数

		map.put("twoRowColumns", twoRowColumns);
		map.put("threeRowColumns", threeRowColumns);
		map.put("eachSubColumns", eachSubColumns);
		map.put("averageColumns", averageColumns);
		map.put("excellentColumns", excellentColumns);
		map.put("passColumns", passColumns);
		map.put("AColumns", AColumns);

		// 总分
		map.put("totalScoreTwoRowColumns", totalScoreTwoRowColumns);
		map.put("totalScoreThreeRowColumns", totalScoreThreeRowColumns);
		map.put("totalScoreAverageColumns", totalScoreAverageColumns);
		map.put("totalScoreExcellentColumns", totalScoreExcellentColumns);
		map.put("totalScorePassColumns", totalScorePassColumns);

		// 2.返回数据
		return map;
	}

	/****
	 * 统计方式
	 * 
	 * @param param
	 * @return true：全员统计和按照范围统计 false:全员统计
	 */
	public boolean checkScopeStatistics(ClassScoreInParam param) {
		boolean b = false;
		int number = classScoreReportDao.getTopGourpNumber(param);
		if (number > 0) {
			b = true;
		}
		return b;
	}

	/***
	 * 从数据结合中找到按照条件的数据行 按照班号和班级分组查匹配
	 * 
	 * @param bh
	 * @param bfz
	 * @return
	 */
	public boolean findDataRow(List<JSONObject> list, String bh, String bmfz) {
		for (JSONObject json : list) {
			if (json.getString("bh").equals(bh) && json.getString("bmfz").equals(bmfz)) {
				return true;
			}
		}
		return false;
	}

	/****
	 * 生成成绩全指标表表头
	 * 
	 * @param topNumber
	 * @param statisticsDataSize
	 * @return
	 */
	private EasyUIDatagridHead[][] assembleFullRangeIndexStatisTableHeader(int topNumber, int statisticsDataSize) {
		EasyUIDatagridHead head[][] = {};
		if (statisticsDataSize <= 0)
			return head;
		if (topNumber > 0) {
			head = new EasyUIDatagridHead[3][];
			head[0] = new EasyUIDatagridHead[2];
			head[0][0] = new EasyUIDatagridHead(null, "全员统计", "center", 0, 1, 25, true);
			head[0][1] = new EasyUIDatagridHead(null, "按范围统计", "center", 0, 1, 25, true);
			head[1] = new EasyUIDatagridHead[20];
			head[1][0] = new EasyUIDatagridHead("ckrs", "参考人数", "center", 55, 2, 1, true);
			head[1][1] = new EasyUIDatagridHead("tjrs", "计算人数", "center", 55, 2, 1, true);
			head[1][2] = new EasyUIDatagridHead("zgf", "最高分", "center", 50, 2, 1, true);
			head[1][3] = new EasyUIDatagridHead("zdf", "最低分", "center", 50, 2, 1, true);
			head[1][4] = new EasyUIDatagridHead(null, "平均分", "center", 0, 1, 4, true);
			head[1][5] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1, 5, true);
			head[1][6] = new EasyUIDatagridHead(null, "合格", "center", 0, 1, 5, true);
			head[1][7] = new EasyUIDatagridHead(null, "低分", "center", 0, 1, 5, true);
			head[1][8] = new EasyUIDatagridHead("ldxs", "难度", "center", 50, 2, 1, true);
			head[1][9] = new EasyUIDatagridHead("qfd", "区分度", "center", 55, 2, 1, true);

			head[1][10] = new EasyUIDatagridHead("ckrs1", "参考人数", "center", 55, 2, 1, true);
			head[1][11] = new EasyUIDatagridHead("tjrs1", "计算人数", "center", 55, 2, 1, true);
			head[1][12] = new EasyUIDatagridHead("zgf1", "最高分", "center", 50, 2, 1, true);
			head[1][13] = new EasyUIDatagridHead("zdf1", "最低分", "center", 50, 2, 1, true);
			head[1][14] = new EasyUIDatagridHead(null, "平均分", "center", 0, 1, 4, true);
			head[1][15] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1, 5, true);
			head[1][16] = new EasyUIDatagridHead(null, "合格", "center", 0, 1, 5, true);
			head[1][17] = new EasyUIDatagridHead(null, "低分", "center", 0, 1, 5, true);
			head[1][18] = new EasyUIDatagridHead("ldxs1", "难度", "center", 50, 2, 1, true);
			head[1][19] = new EasyUIDatagridHead("qfd1", "区分度", "center", 55, 2, 1, true);

			head[2] = new EasyUIDatagridHead[38];
			head[2][0] = new EasyUIDatagridHead("pjf", "平均分", "center", 50, 1, 1, true);
			head[2][1] = new EasyUIDatagridHead("pjffc", "分差", "center", 50, 1, 1, true);
			head[2][2] = new EasyUIDatagridHead("pm", "排名", "center", 50, 1, 1, true);
			head[2][3] = new EasyUIDatagridHead("jz", "均值", "center", 50, 1, 1, true);

			head[2][4] = new EasyUIDatagridHead("ysrs", "人数", "center", 50, 1, 1, true);
			head[2][5] = new EasyUIDatagridHead("ysl", "优秀率", "center", 50, 1, 1, true);
			head[2][6] = new EasyUIDatagridHead("yslc", "率差", "center", 50, 1, 1, true);
			head[2][7] = new EasyUIDatagridHead("yxlpm", "排名", "center", 50, 1, 1, true);
			head[2][8] = new EasyUIDatagridHead("yxljz", "均值", "center", 50, 1, 1, true);

			head[2][9] = new EasyUIDatagridHead("hgrs", "人数", "center", 50, 1, 1, true);
			head[2][10] = new EasyUIDatagridHead("hgl", "合格率", "center", 50, 1, 1, true);
			head[2][11] = new EasyUIDatagridHead("hglc", "率差", "center", 50, 1, 1, true);
			head[2][12] = new EasyUIDatagridHead("hglpm", "排名", "center", 50, 1, 1, true);
			head[2][13] = new EasyUIDatagridHead("hgljz", "均值", "center", 50, 1, 1, true);

			head[2][14] = new EasyUIDatagridHead("dfrs", "人数", "center", 50, 1, 1, true);
			head[2][15] = new EasyUIDatagridHead("dfl", "低分率", "center", 50, 1, 1, true);
			head[2][16] = new EasyUIDatagridHead("dflc", "率差", "center", 50, 1, 1, true);
			head[2][17] = new EasyUIDatagridHead("dflpm", "排名", "center", 50, 1, 1, true);
			head[2][18] = new EasyUIDatagridHead("dfljz", "均值", "center", 50, 1, 1, true);

			head[2][19] = new EasyUIDatagridHead("pjf1", "平均分", "center", 50, 1, 1, true);
			head[2][20] = new EasyUIDatagridHead("pjffc1", "分差", "center", 50, 1, 1, true);
			head[2][21] = new EasyUIDatagridHead("pm1", "排名", "center", 50, 1, 1, true);
			head[2][22] = new EasyUIDatagridHead("jz1", "均值", "center", 50, 1, 1, true);

			head[2][23] = new EasyUIDatagridHead("ysrs1", "人数", "center", 50, 1, 1, true);
			head[2][24] = new EasyUIDatagridHead("ysl1", "优秀率", "center", 50, 1, 1, true);
			head[2][25] = new EasyUIDatagridHead("yslc1", "率差", "center", 50, 1, 1, true);
			head[2][26] = new EasyUIDatagridHead("yxlpm1", "排名", "center", 50, 1, 1, true);
			head[2][27] = new EasyUIDatagridHead("yxljz1", "均值", "center", 50, 1, 1, true);

			head[2][28] = new EasyUIDatagridHead("hgrs1", "人数", "center", 50, 1, 1, true);
			head[2][29] = new EasyUIDatagridHead("hgl1", "合格率", "center", 50, 1, 1, true);
			head[2][30] = new EasyUIDatagridHead("hglc1", "率差", "center", 50, 1, 1, true);
			head[2][31] = new EasyUIDatagridHead("hglpm1", "排名", "center", 50, 1, 1, true);
			head[2][32] = new EasyUIDatagridHead("hgljz1", "均值", "center", 50, 1, 1, true);

			head[2][33] = new EasyUIDatagridHead("dfrs1", "人数", "center", 50, 1, 1, true);
			head[2][34] = new EasyUIDatagridHead("dfl1", "低分率", "center", 50, 1, 1, true);
			head[2][35] = new EasyUIDatagridHead("dflc1", "率差", "center", 50, 1, 1, true);
			head[2][36] = new EasyUIDatagridHead("dflpm1", "排名", "center", 50, 1, 1, true);
			head[2][37] = new EasyUIDatagridHead("dfljz1", "均值", "center", 50, 1, 1, true);
		} else {
			head = new EasyUIDatagridHead[2][];
			head[0] = new EasyUIDatagridHead[10];
			head[0][0] = new EasyUIDatagridHead("ckrs", "参考人数", "center", 55, 2, 1, true);
			head[0][1] = new EasyUIDatagridHead("tjrs", "计算人数", "center", 55, 2, 1, true);
			head[0][2] = new EasyUIDatagridHead("zgf", "最高分", "center", 50, 2, 1, true);
			head[0][3] = new EasyUIDatagridHead("zdf", "最低分", "center", 50, 2, 1, true);
			head[0][4] = new EasyUIDatagridHead(null, "平均分", "center", 0, 1, 4, true);
			head[0][5] = new EasyUIDatagridHead(null, "优秀", "center", 0, 1, 5, true);
			head[0][6] = new EasyUIDatagridHead(null, "合格", "center", 0, 1, 5, true);
			head[0][7] = new EasyUIDatagridHead(null, "低分", "center", 0, 1, 5, true);
			head[0][8] = new EasyUIDatagridHead("ldxs", "难度", "center", 50, 2, 1, true);
			// head[0][9] = new EasyUIDatagridHead("ckrs", "参考人数", "center", 55,
			// 2, 1, true);
			head[0][9] = new EasyUIDatagridHead("qfd", "区分度", "center", 55, 2, 1, true);

			head[1] = new EasyUIDatagridHead[19];
			head[1][0] = new EasyUIDatagridHead("pjf", "平均分", "center", 50, 1, 1, true);
			head[1][1] = new EasyUIDatagridHead("pjffc", "分差", "center", 50, 1, 1, true);
			head[1][2] = new EasyUIDatagridHead("pm", "排名", "center", 50, 1, 1, true);
			head[1][3] = new EasyUIDatagridHead("jz", "均值", "center", 50, 1, 1, true);

			head[1][4] = new EasyUIDatagridHead("ysrs", "人数", "center", 50, 1, 1, true);
			head[1][5] = new EasyUIDatagridHead("ysl", "优秀率", "center", 50, 1, 1, true);
			head[1][6] = new EasyUIDatagridHead("yslc", "率差", "center", 50, 1, 1, true);
			head[1][7] = new EasyUIDatagridHead("yxlpm", "排名", "center", 50, 1, 1, true);
			head[1][8] = new EasyUIDatagridHead("yxljz", "均值", "center", 50, 1, 1, true);

			head[1][9] = new EasyUIDatagridHead("hgrs", "人数", "center", 50, 1, 1, true);
			head[1][10] = new EasyUIDatagridHead("hgl", "合格率", "center", 50, 1, 1, true);
			head[1][11] = new EasyUIDatagridHead("hglc", "率差", "center", 50, 1, 1, true);
			head[1][12] = new EasyUIDatagridHead("hglpm", "排名", "center", 50, 1, 1, true);
			head[1][13] = new EasyUIDatagridHead("hgljz", "均值", "center", 50, 1, 1, true);

			head[1][14] = new EasyUIDatagridHead("dfrs", "人数", "center", 50, 1, 1, true);
			head[1][15] = new EasyUIDatagridHead("dfl", "低分率", "center", 50, 1, 1, true);
			head[1][16] = new EasyUIDatagridHead("dflc", "率差", "center", 50, 1, 1, true);
			head[1][17] = new EasyUIDatagridHead("dflpm", "排名", "center", 50, 1, 1, true);
			head[1][18] = new EasyUIDatagridHead("dfljz", "均值", "center", 50, 1, 1, true);

		}

		return head;
	}

	/***
	 * 从数据结合中找到按照条件的数据行
	 * 
	 * @param bh
	 * @param bfz
	 * @return
	 */
	public boolean findDataRow(List<JSONObject> list, String mkdm) {
		for (JSONObject json : list) {
			if (json.getString("mkdm") != null && json.getString("mkdm").equals(mkdm)) {
				return true;
			}
		}

		return false;
	}

	public float dealNegativeNumber(float number) {
		if (number < 0) {
			number = 0;
		}
		return number;
	}

	public String ConvertEmptyString(String args) {
		if (args == null)
			return "";

		return args;
	}

	/***
	 * 找到对应科目老师的A等率相关参数
	 * 
	 * @param gradeRate
	 * @return
	 */
	public JSONObject findAgradeData(List<JSONObject> gradeRate, String mkdm, String classId) {
		if (mkdm == null || "".equals(mkdm) || classId == null || "".equals(classId))
			return null;

		for (JSONObject rate : gradeRate) {
			if (rate.getString("mkdm") == null || rate.getString("bh") == null)
				continue;

			if (rate.getString("mkdm").equals(mkdm) && rate.getString("bh").equals(classId)) {
				return rate;
			}
		}

		return null;
	}

	 

}
