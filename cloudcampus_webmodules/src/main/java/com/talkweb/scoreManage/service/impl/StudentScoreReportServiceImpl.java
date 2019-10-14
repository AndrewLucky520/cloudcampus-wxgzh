package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreDistributionReportDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.dao.StudentScoreReportDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.StudentScoreReportService;

@Service
public class StudentScoreReportServiceImpl implements StudentScoreReportService {
	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private StudentScoreReportDao studentScoreReportDao;

	@Autowired
	private ScoreDistributionReportDao scoreDistributionReportDao;

	@Autowired
	private AllCommonDataService allCommonDataService;

	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	@Override
	public JSONObject getClassScoreReportList(JSONObject params) {
		return null;
	}

	@Override
	public JSONObject getStudentScoreResultTrackList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();

		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");
		String nj = params.getString("nj");
		String bh = params.getString("bhStr");
		String xmxh = params.getString("xmxh");
		
		HashMap<String, Object> smap = new HashMap<String, Object>();
		smap.put("schoolId", schoolId);
		smap.put("classId", bh);
		smap.put("termInfoId", xnxq);
		smap.put("usedGradeId", nj);
		smap.put("keyword", xmxh);

		Map<String, User> umap = new HashMap<String, User>();
		List<Account> accList = allCommonDataService.getStudentList(smap);
		if (CollectionUtils.isEmpty(accList)) {
            JSONObject data = new JSONObject();
    		data.put("total", 0);
    		data.put("rows", new Object[0]);
    		data.put("msg", "请重新检查学号/姓名");
    		data.put("columns", new Object[0]);
    		data.put("code", 1);
    		return data;
		}
		
		for (Account acc : accList) {
			if (acc == null || acc.getUsers() == null) {
				continue;
			}

			String accId = String.valueOf(acc.getId());
			for (User user : acc.getUsers()) {
				if (user == null || user.getUserPart() == null || user.getUserPart().getRole() == null) {
					continue;
				}

				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					if (!umap.containsKey(accId)) {
						umap.put(accId, user);
					}
				}
			}
		}
		List<String> xhList = new ArrayList<String>(umap.keySet());
		params.put("xhList", xhList);
		params.put("bh", bh);

		JSONObject sm = new JSONObject();
		List<JSONObject> bjcjgzxs = studentScoreReportDao.getbjcjgzxs(xnxq, autoIncr, params);
		EasyUIDatagridHead[][] head = null;
		if (bjcjgzxs.size() > 0) {
			List<JSONObject> bjcjgzsmxx = studentScoreReportDao.getbjcjgzsmxx(xnxq, autoIncr, params);
			if (bjcjgzsmxx.size() > 0) {
				String bczm = bjcjgzsmxx.get(0).getString("fzmc");
				String bcbjrs = bjcjgzsmxx.get(0).getString("ckrs");
				String bcnjrs = bjcjgzsmxx.get(0).getString("fzrs");
				sm.put("curClassRankNum", bcbjrs);	// 当前班级排名人数
				sm.put("curAsgName", bczm);	// 当前分组名称
				sm.put("curAsgNum", bcnjrs);	// 当前分组人数
				
				List<JSONObject> bjcjgzdbkslc = new ArrayList<JSONObject>();
				Map<String, List<String>> dbxnxq2KslcMap = studentScoreReportDao.getDbxnxq2KslcMap(xnxq, autoIncr, params);
				for(Map.Entry<String, List<String>> entry : dbxnxq2KslcMap.entrySet()) {
					String dbxnxq = entry.getKey();
					List<String> kslcList = entry.getValue();
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("xnxq", dbxnxq);
					map.put("kslcdmList", kslcList);
					map.put("xxdm", xxdm);
					List<DegreeInfo> degreeInfoList = scoreDao.getDegreeInfoList(dbxnxq, map);
					for(DegreeInfo degreeInfo1 : degreeInfoList) {
						JSONObject json = new JSONObject();
						json.put("dbxnxq", dbxnxq);
						json.put("dbkslc", degreeInfo1.getKslcdm());
						json.put("kslcmc", degreeInfo1.getKslcmc());
						json.put("autoIncr", degreeInfo1.getAutoIncr());
						bjcjgzdbkslc.add(json);
					}
				}
				
				if (bjcjgzdbkslc.size() > 0) {
					String dbkslcmc = bjcjgzdbkslc.get(0).getString("kslcmc");
					String dbskslc = bjcjgzdbkslc.get(0).getString("dbkslc");
					String dbxnxq = bjcjgzdbkslc.get(0).getString("dbxnxq");
					Integer autoIncr1 = bjcjgzdbkslc.get(0).getInteger("autoIncr");
					
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("xxdm", xxdm);
					map.put("xnxq", dbxnxq);
					map.put("kslc", dbskslc);
					map.put("bh", bh);	// 此报表是按照班级查询的，没有全部班级数据
					List<JSONObject> dbbjcjgzsmxx = studentScoreReportDao.getbjcjgzsmxx(dbxnxq, autoIncr1, map);
					if (dbbjcjgzsmxx.size() > 0) {
						String sczm = dbbjcjgzsmxx.get(0).getString("fzmc");
						String scbjrs = dbbjcjgzsmxx.get(0).getString("ckrs");
						String scnjrs = dbbjcjgzsmxx.get(0).getString("fzrs");
						
						sm.put("compareExamName", dbkslcmc);	// 对比考试名称
						sm.put("compareExamClassNum", scbjrs);	// 对比考试班级人数
						sm.put("compareExamAsgName", sczm);		// 对比分组名称
						sm.put("compareExamAsgNum", scnjrs);	// 对比年级人数
					}
				}
			}

			head = new EasyUIDatagridHead[2][];
			List<Long> bjcjKmdmList = studentScoreReportDao.getBjcjMkGZ(xnxq, autoIncr, params);
			List<JSONObject> bjcjgzcj = studentScoreReportDao.getbjcjgzcj(xnxq, autoIncr, params);

			Map<String, JSONObject> bjcjgzcjMap = new HashMap<String, JSONObject>();
			for (JSONObject bjcjgz : bjcjgzcj) {
				String studentNo = bjcjgz.getString("xh");
				String subjectNo = bjcjgz.getString("kmdm");
				
				String key = studentNo + subjectNo;
				if (!bjcjgzcjMap.containsKey(key)) {
					bjcjgzcjMap.put(key, bjcjgz);
				}
			}

			head[0] = new EasyUIDatagridHead[1 + bjcjKmdmList.size()];
			head[1] = new EasyUIDatagridHead[(1 + bjcjKmdmList.size()) * 3];
			List<LessonInfo> lessonInfoList = allCommonDataService.getLessonInfoBatch(schoolId, bjcjKmdmList, xnxq);// 科目
			Map<Long, LessonInfo> kmdm2LessonInfoMap = new HashMap<Long, LessonInfo>();
			for (LessonInfo lessonInfo : lessonInfoList) {
				if (!kmdm2LessonInfoMap.containsKey(lessonInfo.getId()))
					kmdm2LessonInfoMap.put(lessonInfo.getId(), lessonInfo);
			}
			int i = 0;
			for (Long kmdm : bjcjKmdmList) {
				LessonInfo lessonInfo = kmdm2LessonInfoMap.get(kmdm);
				if(lessonInfo == null) {
					continue;
				}
				head[0][i] = new EasyUIDatagridHead("null", lessonInfo.getName(), "center", 0, 1, 3,
						true);
				head[1][i * 3 + 0] = new EasyUIDatagridHead(
						String.valueOf(lessonInfo.getId()) + "_score", "成绩", "center", 50, 1, 1,
						true);
				head[1][i * 3 + 1] = new EasyUIDatagridHead(
						String.valueOf(lessonInfo.getId()) + "_scoreClassRank", "班名", "center", 100,
						1, 1, true);
				head[1][i * 3 + 2] = new EasyUIDatagridHead(
						String.valueOf(lessonInfo.getId()) + "_scoreGradeRank", "年名", "center", 100,
						1, 1, true);
				i++;
			}
			head[0][i] = new EasyUIDatagridHead("null", "总分", "center", 0, 1, 3, true);
			head[1][i * 3 + 0] = new EasyUIDatagridHead("totalScore", "成绩", "center", 50, 1, 1, true);
			head[1][i * 3 + 1] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 100, 1, 1, true);
			head[1][i * 3 + 2] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 100, 1, 1, true);
			List<Long> cidlist = new ArrayList<Long>();
			for (JSONObject obXs : bjcjgzxs) {
				if (umap.get(obXs.getString("xh")) != null) {
					User u = umap.get(obXs.getString("xh"));
					if (u != null) {
						obXs.put("ClassId", u.getStudentPart().getClassId());

						long classId = u.getStudentPart().getClassId();
						if (!cidlist.contains(classId)) {
							cidlist.add(classId);
						}
					}
				}
			}
			List<Classroom> claslist = allCommonDataService.getClassroomBatch(schoolId, cidlist, xnxq);// 班级
			HashMap<String, Classroom> cmap = new HashMap<String, Classroom>();
			for (Classroom cr : claslist) {
				cmap.put(String.valueOf(cr.getId()), cr);
			}

			for (JSONObject obXs : bjcjgzxs) {
				if (umap.get(obXs.getString("xh")) != null) {
					String xh = obXs.getString("xh");
					for (Long kmdm : bjcjKmdmList) {
						JSONObject obCj = bjcjgzcjMap.get(xh + kmdm);
						if (obCj != null) {
							obXs.put(kmdm + "_score", obCj.get("cj"));
							obXs.put(kmdm + "_scoreClassRank", obCj.get("bjpm"));
							obXs.put(kmdm + "_scoreGradeRank", obCj.get("njpm"));
						}
					}
					obXs.put("totalScore", obXs.get("zf"));
					obXs.put("totalScoreClassRank", obXs.get("bjpm"));
					obXs.put("totalScoreGradeRank", obXs.get("njpm"));
					User user = umap.get(obXs.getString("xh"));
					Account account = user == null ? null : user.getAccountPart();
					String name = account == null ? "" : account.getName();
					String studentId = (user == null || user.getStudentPart() == null) ? ""
							: user.getStudentPart().getSchoolNumber();
					obXs.put("studentId", studentId);
					obXs.put("studentName", name);
					if (cmap.get(obXs.getString("ClassId")) != null) {
						obXs.put("className", cmap.get(obXs.getString("ClassId")).getClassName());
					}
				}
			}
		}

		JSONObject data = new JSONObject();
		data.put("total", bjcjgzxs.size());
		data.put("rows", bjcjgzxs);
		data.put("topmsg", sm);
		data.put("columns", head);
		data.put("code", 1);
		data.put("msg", "");
		return data;
	}

	@Override
	public JSONObject getScoreSectionTotalList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		
		Long schoolId = params.getLong("xxdm");
		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		params.put("xn", xnxq.substring(0, xnxq.length() - 1));
		
		// 得到 该年级的各个班级统计分数区间、班级代码、分布代码、人数、年级
		List<JSONObject> distributeList = scoreDistributionReportDao.getScoreClassDistributeList(xnxq, autoIncr, params);

		// 得到班主任姓名、最高分、参考人数、班级名称、班级代码、年级
		List<JSONObject> statisticList = scoreDistributionReportDao.getScoreClassStatisticsList(xnxq, autoIncr, params);

		List<JSONObject> list = new LinkedList<JSONObject>();
		EasyUIDatagridHead[][] head = null;
		if (statisticList.size() > 0) {
			JSONObject jo = new JSONObject();
			JSONObject jo1 = new JSONObject();
			JSONObject jo2 = new JSONObject();

			// 第一列，设置前三行数据
			jo.put("projectName", "班主任");
			jo1.put("projectName", "最高分");
			jo2.put("projectName", "人数");
			head = new EasyUIDatagridHead[1][];
			head[0] = new EasyUIDatagridHead[statisticList.size() + 1];
			String bjdm = "";
			int totalP = 0;// 参考总人数
			float maxScore = 0;// 最高分
			List<Long> cids = new ArrayList<Long>();

			for (JSONObject json : statisticList) {
				cids.add(json.getLong("bhs"));
			}
			List<Classroom> clist = allCommonDataService.getClassroomBatch(schoolId, cids, xnxq);// 班级信息
			Map<String, Classroom> cmap = new HashMap<String, Classroom>();// 班级集合
			Map<String, Account> tmap = new HashMap<String, Account>();// 老师集合

			List<Long> tids = new ArrayList<Long>();
			Set<Long> tset = new HashSet<Long>();
			for (Classroom cr : clist) {
				cmap.put(String.valueOf(cr.getId()), cr);
				tset.add(cr.getDeanAccountId());
			}
			Iterator<Long> iterator = tset.iterator();
			while (iterator.hasNext()) {
				tids.add(iterator.next());
			}
			List<Account> tlist = allCommonDataService.getAccountBatch(schoolId, tids, xnxq);// 老师信息
			if (tlist != null) {
				for (Account a : tlist) {
					if (a != null && a.getUsers() != null) {
						for (User u : a.getUsers()) {
							UserPart userPart = u == null ? null : u.getUserPart();
							T_Role role = userPart == null ? null : userPart.getRole();
							if (role != null && role.equals(T_Role.Teacher)) {
								List<Long> ttlist = u.getTeacherPart().getDeanOfClassIds();
								if (CollectionUtils.isNotEmpty(ttlist)) {
									for (Long c : ttlist) {
										tmap.put(String.valueOf(c), a);
									}
								}
							}
						}
					}
				}
			}

			for (JSONObject json : statisticList) {
				String className = "";
				String classId = json.getString("bhs");
				if (cmap.get(classId) != null) {
					className = cmap.get(classId).getClassName();
				}
				json.put("className", className);
			}
			
			Collections.sort(statisticList, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject arg0, JSONObject arg1) {
					String className0 = arg0.getString("className");
					String className1 = arg1.getString("className");
					return className0.compareTo(className1);
				}
			});

			for (int i = 0; i < statisticList.size(); i++) {
				String teacherName = "";
				String className = "";

				if (cmap.get(statisticList.get(i).getString("bhs")) != null) {
					className = cmap.get(statisticList.get(i).getString("bhs")).getClassName();
				}
				if (tmap.get(statisticList.get(i).getString("bhs")) != null) {
					teacherName = tmap.get(statisticList.get(i).getString("bhs")).getName();
				}

				JSONObject sj = statisticList.get(i);
				bjdm = sj.getString("bhs");// 使用班级代码作为key
				// 设置表头，列使用班级号进行标记，列头从第二列开始，不包括固定列第一列班级
				head[0][i] = new EasyUIDatagridHead(bjdm + "projectValue", className, "center", 100, 1, 1, true);

				// 班主任姓名
				jo.put(bjdm + "projectValue", teacherName);
				// 最高分
				jo1.put(bjdm + "projectValue", sj.get("zgf"));
				// 人数
				jo2.put(bjdm + "projectValue", sj.get("ckrs"));

				if (Float.parseFloat(sj.getString("zgf")) > maxScore) {
					maxScore = Float.parseFloat(sj.getString("zgf"));
				}

				totalP += sj.getIntValue("ckrs");

			}
			jo.put("all", "");
			jo1.put("all", maxScore);
			jo2.put("all", totalP);
			list.add(jo);
			list.add(jo1);
			list.add(jo2);

			head[0][statisticList.size()] = new EasyUIDatagridHead("all", "全部", "center", 100, 1, 1, true);

			if (distributeList.size() > 0) {
				String dm = distributeList.get(0).getString("dm");
				params.put("dm", dm);
				// 得到代码，分布代码，总分，最高分
				List<JSONObject> totalScoreList = scoreDistributionReportDao.getTotalScoreList(xnxq, autoIncr, params);
				// 循环分数区间
				for (int j = 0; j < totalScoreList.size(); j++) {
					JSONObject dj = totalScoreList.get(j);

					if (j == 0) {
						dj.put("projectName", "[" + dj.getString("fbsx") + "-" + dj.getString("fbxx") + "]");
					} else {
						dj.put("projectName", "(" + dj.getString("fbsx") + "-" + dj.getString("fbxx") + "]");
					}

					String fbdm = dj.getString("fbdm");
					int sumljrs = 0;
					int sumrs = 0;

					// 循环各个分数区间对应的人数比例
					for (int m = 0; m < distributeList.size(); m++) {
						JSONObject dbo = distributeList.get(m);
						if (fbdm.equals(dbo.getString("fbdm"))) {
							dj.put(dbo.getString("bhs") + "projectValue",
									dbo.getIntValue("rs") + "/" + dbo.getIntValue("ljrs"));
							sumrs += dbo.getIntValue("rs");
							sumljrs += dbo.getIntValue("ljrs");
						}
					}
					dj.put("all", sumrs + "/" + sumljrs);
					list.add(dj);
				}

			}
		}

		JSONObject response = new JSONObject();
		response.put("total", list.size());
		response.put("rows", list);
		response.put("columns", head);
		return response;
	}
	
	@Override
	public JSONObject getScoreSectionSubjectList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		
		Long schoolId = params.getLong("xxdm");
		String nj = params.getString("nj");
		String bhStr = params.getString("bhStr");
		String kmdmStr = params.getString("kmdmStr");
		params.remove("bhStr");
		params.remove("kmdmStr");
		params.put("kmdmList", StringUtil.convertToListFromStr(kmdmStr, ",", String.class));
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Long> subjectIds = StringUtil.convertToListFromStr(kmdmStr, ",", Long.class);

		Map<String, Classroom> cmap = new HashMap<String, Classroom>();// 班级集合
		Map<String, Classroom> smap = new HashMap<String, Classroom>();// 班级集合，key:学生accountId
		List<Long> deanIdList = new ArrayList<Long>();	// 班主任
		
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);// 班级信息
		if (classroomList != null) {
			for (Classroom classroom : classroomList) {
				if (classroom == null) {
					continue;
				}
				long deanId = classroom.getDeanAccountId();
				if(!deanIdList.contains(deanId)) {
					deanIdList.add(deanId);
				}
				String classId = String.valueOf(classroom.getId());
				cmap.put(classId, classroom);
				if (classroom.getStudentAccountIds() != null) {
					for (Long sid : classroom.getStudentAccountIds()) {
						smap.put(String.valueOf(sid), classroom);
					}
				}
			}
		}
		
		Map<String, String> teacherMap = new HashMap<String, String>();
		HashMap<String, Object> ttmap = new HashMap<String, Object>();
		ttmap.put("schoolId", schoolId);
		ttmap.put("termInfoId", xnxq);
		ttmap.put("usedGradeId", nj);
		ttmap.put("classId", bhStr);
		List<Account> ttlist = allCommonDataService.getCourseTeacherList(ttmap);
		if (ttlist != null) {
			for (Account a : ttlist) {
				if (a == null || a.getUsers() == null)
					continue;
				for (User u : a.getUsers()) {
					if (u == null || u.getUserPart() == null || u.getUserPart().getRole() == null)
						continue;
					if (u.getUserPart().getRole().equals(T_Role.Teacher)) {
						List<Course> colist = u.getTeacherPart() == null ? null : u.getTeacherPart().getCourseIds();
						if (colist != null) {
							for (Course c : colist) {
								if (c == null)
									continue;
								teacherMap.put(String.valueOf(c.getLessonId()) + String.valueOf(c.getClassId()),
										a.getName());
							}
						}

					}
				}
			}
		}

		Map<String, Account> tmap = new HashMap<String, Account>();// 班主任老师集合
		Map<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();// 班主任老师集合
		List<Account> deanList = allCommonDataService.getAccountBatch(schoolId, deanIdList, xnxq);// 老师信息
		if (deanList != null) {
			for (Account acc : deanList) {
				if (acc == null || acc.getUsers() == null) {
					continue;
				}
				for (User user : acc.getUsers()) {
					if(user == null || user.getUserPart() == null) {
						continue;
					}
					if (T_Role.Teacher.equals(user.getUserPart().getRole())) {
						List<Long> list = user.getTeacherPart().getDeanOfClassIds();
						if (CollectionUtils.isNotEmpty(list)) {
							for (Long c : list) {
								tmap.put(String.valueOf(c), acc);
							}
						}
					}
				}
			}
		}

		List<LessonInfo> lessonInfoList = allCommonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);
		if (lessonInfoList != null) {
			for (LessonInfo lessonInfo : lessonInfoList) {
				if (lessonInfo == null) {
					continue;
				}
				lmap.put(String.valueOf(lessonInfo.getId()), lessonInfo);
			}
		}
		// 修改结束

		List<JSONObject> distributeNumList = scoreDistributionReportDao.getKMScoreDistributeNumList(xnxq, autoIncr, params);

		// 得到班主任
		List<JSONObject> scoreClassList = scoreDistributionReportDao.getKMScoreClassStatisticList(xnxq, autoIncr, params);// 已改

		List<JSONObject> scoredistributeList = scoreDistributionReportDao.getKMScoreDistributeList(xnxq, autoIncr, params);

		List<JSONObject> ScoreGrouplist = scoreDistributionReportDao.getScoreGroupStatisticsList(xnxq, autoIncr, params);// 年级平均分

		Map<String, JSONObject> ScoreGroupmap = new HashMap<String, JSONObject>();
		for (JSONObject o : ScoreGrouplist) {
			ScoreGroupmap.put(o.getString("kmdm"), o);
		}
		
		List<JSONObject> list = new LinkedList<JSONObject>();
		EasyUIDatagridHead[][] head = null;

		// 设置表头
		if (scoreClassList.size() > 0) {
			// 存放各个班级的班主任
			JSONObject bzrJson = new JSONObject();
			bzrJson.put("projectName", "");
			bzrJson.put("className", "班主任");
			// 存放各个班级的名称
			JSONObject bjJson = new JSONObject();

			// 各个班级对应科目对应的区间最高分、参考人数、平均值，班主任名称
			HashMap<String, JSONObject> allZgfMap = new HashMap<String, JSONObject>();
			List<Long> cidlist = new ArrayList<Long>();
			for (JSONObject json : scoreClassList) {
				cidlist.add(Long.valueOf(json.getString("bhs")));
			}

			for (int m = 0; m < scoreClassList.size(); m++) {
				JSONObject jm = scoreClassList.get(m);
				String bhs = jm.getString("bhs");
				String km = jm.getString("kmdm");
				if (cmap.get(bhs) != null) {
					if (!bjJson.containsKey(bhs)) {
						bjJson.put(bhs, cmap.get(bhs).getClassName());
						if (tmap.get(bhs) != null) {
							bzrJson.put(bhs, tmap.get(bhs).getName());
						}
					}
				}

				allZgfMap.put(km + bhs, jm);

			}

			// 所有科目
			LinkedHashMap<String, JSONObject> kmMap = new LinkedHashMap<String, JSONObject>();
			HashMap<String, JSONObject> disRsMap = new HashMap<String, JSONObject>();// 各个科目区间对应的人数

			// 修改2<-----
			for (JSONObject json : distributeNumList) {
				if (lmap.get(json.getString("kmdm")) != null) {
					json.put("zwmc", lmap.get(json.getString("kmdm")).getName());
				}
			}

			for (int m = 0; m < distributeNumList.size(); m++) {
				JSONObject jm = distributeNumList.get(m);
				disRsMap.put(jm.getString("kmdm") + jm.getString("bhs") + jm.getString("dm") + jm.getString("fbdm"),
						jm);
				if (!kmMap.containsKey(jm.getString("kmdm"))) {
					kmMap.put(jm.getString("kmdm"), jm);
				}
			}

			// 设置表头
			List<JSONObject> keyList = new ArrayList<JSONObject>();
			if (bjJson != null) {
				for (String key : bjJson.keySet()) {
					JSONObject object = new JSONObject();
					object.put("className", bjJson.get(key));
					object.put("classId", key);
					keyList.add(object);
				}
			}
			
			Collections.sort(keyList, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject arg0, JSONObject arg1) {
					String className0 = arg0.getString("className");
					String className1 = arg1.getString("className");
					return className0.compareTo(className1);
				}
			});
			
			head = new EasyUIDatagridHead[1][];
			head[0] = new EasyUIDatagridHead[bjJson.size() + 1];
			int hi = 0;
			for (JSONObject key : keyList) {
				head[0][hi] = new EasyUIDatagridHead(key.get("classId") + "",
						StringUtil.transformString(key.get("className")), "center", 100, 1, 1, true);
				hi++;
			}

			head[0][bjJson.size()] = new EasyUIDatagridHead("all", "全部", "center", 100, 1, 1, true);

			// 添加班主任第一行
			list.add(bzrJson);

			// 循环科目
			for (Iterator<String> it = kmMap.keySet().iterator(); it.hasNext();) {
				JSONObject j = kmMap.get(it.next());
				// 任课教师
				JSONObject rkjs = new JSONObject();
				// 最高分
				JSONObject zgf = new JSONObject();
				// 平均分
				JSONObject pjf = new JSONObject();
				// 参考
				JSONObject ckrs = new JSONObject();
				float zpjf = 0;
				float zzgf = 0;
				int zckrs = 0;

				String kmmc = j.getString("zwmc");
				String kmcode = j.getString("kmdm");
				rkjs.put("projectName", kmmc);
				rkjs.put("className", "任课教师");
				zgf.put("projectName", kmmc);
				zgf.put("className", "最高分");
				pjf.put("projectName", kmmc);
				pjf.put("className", "平均分");
				ckrs.put("projectName", kmmc);
				ckrs.put("className", "参考人数");
				for (Iterator<String> bjit = bjJson.keySet().iterator(); bjit.hasNext();) {
					String bh1 = bjit.next();
					rkjs.put(bh1 + "", teacherMap.get(kmcode + bh1));
					JSONObject jc = allZgfMap.get(kmcode + bh1);
					if (null == jc) {
						jc = new JSONObject();
					}

					zgf.put(bh1 + "", jc.getFloatValue("zgf"));
					pjf.put(bh1 + "", jc.getFloatValue("pjf"));
					ckrs.put(bh1 + "", jc.getIntValue("ckrs"));

					if (jc.getFloatValue("zgf") > zzgf) {
						zzgf = jc.getFloatValue("zgf");
					}
					zckrs += jc.getIntValue("ckrs");
					zpjf += jc.getDoubleValue("pjf");
				}

				rkjs.put("all", "");
				zgf.put("all", zzgf);
				pjf.put("all", ScoreGroupmap.get(kmcode) != null ? ScoreGroupmap.get(kmcode).getString("pjf") : "");
				ckrs.put("all", zckrs);
				list.add(rkjs);
				list.add(zgf);

				// 得到此区间种类的分布区间
				String dm = j.getString("dm");
				params.put("dm", dm);
				scoredistributeList = scoreDistributionReportDao.getKMScoreDistributeList(xnxq, autoIncr, params);
				int i = 0;
				for (JSONObject json : scoredistributeList) {
					JSONObject row = new JSONObject();
					row.put("projectName", kmmc);
					if (i == scoredistributeList.size() - 1) {
						row.put("className", json.getString("fbsx") + "分以下");
					} else {
						row.put("className", json.getString("fbxx") + "分以上");
					}

					String fbdm = json.getString("fbdm");
					int sumRs = 0;
					for (Iterator<String> bjit = bjJson.keySet().iterator(); bjit.hasNext();) {
						String bh1 = bjit.next();
						int rs = 0;
						if (disRsMap.get(kmcode + bh1 + dm + fbdm) != null) {
							rs = disRsMap.get(kmcode + bh1 + dm + fbdm).getIntValue("ljrs");
						}
						if (i == scoredistributeList.size() - 1) {
							if (disRsMap.get(kmcode + bh1 + dm + fbdm) != null) {
								rs = disRsMap.get(kmcode + bh1 + dm + fbdm).getIntValue("rs");
							}
						}
						sumRs += rs;
						row.put(bh1 + "", rs);

					}
					row.put("all", sumRs);
					list.add(row);

					i++;
				}

				list.add(ckrs);
				list.add(pjf);
			}

		}

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		data.put("columns", head);
		return data;
	}
	
	@Override
	public JSONObject getRankSectionList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		
		String xn = xnxq.substring(0, xnxq.length() - 1);
		params.put("xn", xn);
		Long schoolId = params.getLong("xxdm");
		String nj = params.getString("nj");
		
		// 修改
		HashMap<String, Object> symap = new HashMap<String, Object>();
		symap.put("schoolId", schoolId);
		symap.put("termInfoId", xnxq);
		symap.put("usedGradeId", nj);
		List<Classroom> classrooms = allCommonDataService.getClassList(symap);
		Map<String, String> classroomMap = new HashMap<String, String>();
		if (classrooms != null) {
			for (Classroom classroom : classrooms) {
				if (classroom == null) {
					continue;
				}
				String classId = String.valueOf(classroom.getId());
				if (!classroomMap.containsKey(classId)) {
					classroomMap.put(classId, classroom.getClassName());
				}
			}
		}
		params.put("bhList", new ArrayList<String>(classroomMap.keySet()));
		// 修改结束

		List<JSONObject> statisticList = scoreDistributionReportDao.getScoreRankStatisticsList(xnxq, autoIncr, params);
		List<JSONObject> numList = scoreDistributionReportDao.getRangeNumList(xnxq, autoIncr, params);
		List<JSONObject> distributeList = scoreDistributionReportDao.getScoreRankDistributeList(xnxq, autoIncr, params);
		List<JSONObject> list = new LinkedList<JSONObject>();

		EasyUIDatagridHead[][] head = null;
		if (distributeList.size() > 0) {
			// 设置表头
			head = new EasyUIDatagridHead[1][];
			head[0] = new EasyUIDatagridHead[distributeList.size() + 2];
			int j = 1;
			head[0][0] = new EasyUIDatagridHead("referenceNum", "人数", "center", 50, 1, 1, true);
			String sx = "";
			JSONObject row0 = new JSONObject();
			JSONObject row = new JSONObject();
			row0.put("className", "全部");
			for (int i = 0; i < distributeList.size(); i++) {
				JSONObject jn = distributeList.get(i);
				head[0][j] = new EasyUIDatagridHead(jn.getString("pmfbdm"),
						jn.getString("pmfbxx") + "-" + jn.getString("pmfbsx"), "center", 100, 1, 1, true);
				j++;
				if (i == distributeList.size() - 1) {
					sx = jn.getString("pmfbsx");
				}
				row0.put(jn.getString("pmfbdm"), 0);
			}
			head[0][j] = new EasyUIDatagridHead("moreUpperLimit", ">" + sx, "center", 100, 1, 1, true);

			// 设置表格的数据
			int zckrs = 0;
			for (int i = 0; i < numList.size(); i++) {
				JSONObject js = numList.get(i);
				String bhs = js.getString("bhs");
				int ckrs = js.getIntValue("referenceNum");
				zckrs += ckrs;
				int rs = 0;
				
				if(!js.containsKey("groupName")) {
					js.put("groupName", "");
				}
				
				js.put("className", "");
				// 设置班级名称
				if (classroomMap != null) {
					js.put("className", classroomMap.get(bhs));
				}

				for (int m = 0; m < statisticList.size(); m++) {
					JSONObject jd = statisticList.get(m);
					String pmfbdm = jd.getString("pmfbdm");
					if (bhs.equals(jd.getString("bhs")) && row0.containsKey(pmfbdm)) {
						int fbrs = jd.getIntValue("rs");
						int fbljrs = jd.getIntValue("ljrs");
						rs += fbrs;
						row0.put(pmfbdm, row0.getIntValue(pmfbdm) + fbrs);
						row.put(pmfbdm, row.getIntValue(pmfbdm) + fbljrs);
						js.put(pmfbdm, jd.getIntValue("rs") + "/" + jd.getIntValue("ljrs"));
					}

				}
				row0.put("moreUpperLimit", row0.getIntValue("moreUpperLimit") + ckrs - rs);
				js.put("moreUpperLimit", ckrs - rs + "/" + ckrs);
				list.add(js);
			}

			Collections.sort(list, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject arg0, JSONObject arg1) {
					String groupName0 = arg0.getString("groupName");
					String groupName1 = arg1.getString("groupName");
					int result = groupName0.compareTo(groupName1);
					if(result != 0) {
						return result;
					}
					String className0 = arg0.getString("className");
					String className1 = arg1.getString("className");
					return className0.compareTo(className1);
				}
			});
			
			// 设置第一行：全部
			for (Iterator<String> it = row.keySet().iterator(); it.hasNext();) {
				String pmfbdm = it.next();
				row0.put(pmfbdm, row0.getIntValue(pmfbdm) + "/" + row.getIntValue(pmfbdm));
			}
			row0.put("referenceNum", zckrs);
			row0.put("moreUpperLimit", row0.getIntValue("moreUpperLimit") + "/" + zckrs);
			list.add(0, row0);
		}
		
		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		data.put("columns", head);
		return data;
	}
}
