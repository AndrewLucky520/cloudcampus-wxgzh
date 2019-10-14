package com.talkweb.scoreManage.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.dao.StudentLevelReportDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.StudentLevelReportService;
import com.talkweb.webutils.WebParamUtil;

@Service
public class StudentLevelReportServiceImpl implements StudentLevelReportService {

	@Autowired
	private StudentLevelReportDao studentLevelReportDao;

	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private AllCommonDataService allCommonDataService;

	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	@Override
	public JSONObject getStudentLevelScoreResultDetailTab(JSONObject params) {
		String xnxq = params.getString("xnxq");

		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		String bhStr = params.getString("bhStr");
		String xmxh = params.getString("xmxh");
		Integer topXRank = params.getInteger("topXRank");
		Integer lastXRank = params.getInteger("lastXRank");
		Float topXTotalPer = params.getFloat("topXTotalPer");
		Float lastXTotalPer = params.getFloat("lastXTotalPer");
		String qa = params.getString("qa");
		params.remove("bhStr");
		params.remove("xmxh");
		params.remove("topXRank");
		params.remove("lastXRank");
		params.remove("topXTotalPer");
		params.remove("lastXTotalPer");
		params.remove("qa");

		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));

		String xxdm = params.getString("xxdm");
		Long schoolId = params.getLong("xxdm");
		String nj = params.getString("nj");

		// 获取所有班级
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classrooms = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		List<Long> studIds = new ArrayList<Long>();
		Map<String, Classroom> classroomsMap = new HashMap<String, Classroom>();
		if (CollectionUtils.isNotEmpty(classrooms)) {
			for (Classroom classroom : classrooms) {
				if (classroom == null)
					continue;
				String classId = String.valueOf(classroom.getId());
				if (!classroomsMap.containsKey(classId)) {
					classroomsMap.put(classId, classroom);
					studIds.addAll(classroom.getStudentAccountIds());
				}
			}
		}
		classIds = null;

		// 获取学生信息
		List<Account> students = null;
		if (StringUtils.isNotBlank(xmxh)) {
			HashMap<String, Object> studentParam = new HashMap<String, Object>();
			studentParam.put("schoolId", schoolId);
			studentParam.put("termInfoId", xnxq);
			studentParam.put("usedGradeId", nj);
			studentParam.put("classId", bhStr);
			studentParam.put("keyword", xmxh);
			students = allCommonDataService.getStudentList(studentParam);
			if (CollectionUtils.isEmpty(students)) {
				JSONObject data = new JSONObject();
				data.put("total", 0);
				data.put("rows", new Object[0]);
				data.put("code", -1);
				data.put("msg", "未找到学生信息，请检查学号/姓名！");
				return data;
			}
		} else {
			students = allCommonDataService.getAccountBatch(schoolId, studIds, xnxq);
			if (CollectionUtils.isEmpty(students)) {
				JSONObject data = new JSONObject();
				data.put("total", 0);
				data.put("rows", new Object[0]);
				data.put("code", -1);
				data.put("msg", "");
				return data;
			}
		}

		Map<String, User> studentsMap = new HashMap<String, User>();// 存放学生信息Map
		List<String> xhList = new ArrayList<String>();
		for (Account account : students) {
			if (account == null || account.getUsers() == null) {
				continue;
			}

			String accId = String.valueOf(account.getId());
			for (User user : account.getUsers()) {
				if (user == null || user.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					if (!studentsMap.containsKey(account.getId())) {
						studentsMap.put(accId, user);
						xhList.add(accId);
					}
				}
			}
		}
		params.put("xhList", xhList);

		Integer maxpm = studentLevelReportDao.getNjpmMax(xnxq, autoIncr, params);
		if (maxpm == null) {
			maxpm = 0;
		}

		if (lastXRank != null) {
			lastXRank = maxpm - lastXRank;
		}

		if (topXTotalPer != null && topXTotalPer > 0) {
			topXRank = (int) (topXTotalPer * maxpm / 100);
		}

		if (lastXTotalPer != null && lastXTotalPer > 0) {
			lastXRank = (int) (maxpm - lastXTotalPer * maxpm / 100);
		}

		if (topXRank != null && topXRank < 0) {
			topXRank = 0;
		}

		if (lastXRank != null && lastXRank < 0) {
			lastXRank = Integer.MAX_VALUE;
		}

		String lb = "00";
		boolean ifExistsBjcjzhcj = studentLevelReportDao.ifExistsBjcjzhcj(xnxq, autoIncr, params);
		if (ifExistsBjcjzhcj) {
			lb = "01";
		}

		List<Long> subjectIds = new ArrayList<Long>();
		List<JSONObject> bjcjMk = studentLevelReportDao.getBjcjMk(xnxq, autoIncr, params);
		for (JSONObject obmk : bjcjMk) {
			Long kmdm = obmk.getLong("kmdm");
			if (!subjectIds.contains(kmdm)) {
				subjectIds.add(kmdm);
			}
		}

		Map<String, LessonInfo> lessonInfoMap = new HashMap<String, LessonInfo>();
		List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);
		if (lessonInfos != null) {
			for (LessonInfo lessonInfo : lessonInfos) {
				if (lessonInfo == null) {
					continue;
				}
				String subjectId = String.valueOf(lessonInfo.getId());
				if (!lessonInfoMap.containsKey(subjectId)) {
					lessonInfoMap.put(subjectId, lessonInfo);
				}
			}
		}

		params.put("topXRank", topXRank);
		params.put("lastXRank", lastXRank);

		// String sqlwhere = "";
		// if (qpm != null && !("").equals(qpm)) {
		// sqlwhere = " and b.njpm<=" + qpm;
		// } else if (hpm != null && !("").equals(hpm)) {
		// sqlwhere = " and b.njpm>" + hpm;
		// }

		if (qa != null && ("3").equals(qa)) {
			List<Long> qamk = null;
			if ("00".equals(lb)) {
				qamk = studentLevelReportDao.getbjcjqamk00(xnxq, autoIncr, params);
			} else {
				qamk = studentLevelReportDao.getbjcjqamk01(xnxq, autoIncr, params);
			}
			int mkCount = qamk.size();

			if ("00".equals(lb)) {
				params.put("mkCount", mkCount);
				// sqlwhere = " and b.djxl like '" + mkCount + "A%' ";
			} else {
				params.put("mkCount2", mkCount);
				// sqlwhere = " and b.djxl2 like '" + mkCount + "A%' ";
			}
		}

		List<JSONObject> bjcjXs = studentLevelReportDao.getbjcjxsdd(xnxq, autoIncr, params);
		if (CollectionUtils.isNotEmpty(bjcjXs)) {
			Map<String, List<String>> key2XhList = new HashMap<String, List<String>>();
			Map<String, List<JSONObject>> key2ListMap = new HashMap<String, List<JSONObject>>();
			for (JSONObject obj : bjcjXs) { // 获取对比考试轮次的排名
				String xh = (String) obj.remove("xh");
				String dbkslc = (String) obj.remove("dbkslc");
				String dbxnxq = (String) obj.remove("dbxnxq");

				// obj.put("lastExamRank", 0);

				if (dbkslc == null || dbxnxq == null) {
					continue;
				}

				String key = dbkslc + "_" + dbxnxq;
				if (!key2XhList.containsKey(key)) {
					key2XhList.put(key, new ArrayList<String>());
				}
				key2XhList.get(key).add(xh);

				String key2 = key + "_" + xh;
				if (!key2ListMap.containsKey(key2)) {
					key2ListMap.put(key2, new ArrayList<JSONObject>());
				}
				key2ListMap.get(key2).add(obj);
			}

			for (Map.Entry<String, List<String>> entry : key2XhList.entrySet()) {
				String[] tmp = entry.getKey().split("_");
				List<String> xhList2 = entry.getValue();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("xxdm", xxdm);
				map.put("kslcdm", tmp[0]);
				map.put("kslc", tmp[0]);
				map.put("xnxq", tmp[1]);
				map.put("xhList", xhList2);
				DegreeInfo dbDegreeInfo = scoreDao.getDegreeInfoById(tmp[1], map);
				if (dbDegreeInfo == null) {
					continue;
				}
				Integer autoIncr2 = dbDegreeInfo.getAutoIncr();

				List<JSONObject> njpmList = studentLevelReportDao.getNjpmInDbKslc(tmp[1], autoIncr2, map);
				for (JSONObject obj : njpmList) {
					String xh = obj.getString("xh");
					Integer njpm = obj.getInteger("njpm");
					String key = entry.getKey() + "_" + xh;
					List<JSONObject> list = key2ListMap.get(key);
					if (list == null) {
						continue;
					}
					for (JSONObject json : list) {
						json.put("lastExamRank", njpm);
					}
				}
			}
		}

		EasyUIDatagridHead[][] head = null;
		if (bjcjXs.size() > 0) {
			head = new EasyUIDatagridHead[1][];

			if (ifExistsBjcjzhcj) {
				head[0] = new EasyUIDatagridHead[7 + bjcjMk.size()];
			} else {
				head[0] = new EasyUIDatagridHead[6 + bjcjMk.size()];
			}
			List<JSONObject> bjcjCj = studentLevelReportDao.getbjcjcjdd(xnxq, autoIncr, params);
			int i = 0;
			for (JSONObject obmk : bjcjMk) {
				String subjectId = obmk.getString("kmdm");
				LessonInfo lessonInfo = null;
				if (lessonInfoMap != null)
					lessonInfo = lessonInfoMap.get(subjectId);

				String subjectName = "";
				if (lessonInfo != null)
					subjectName = lessonInfo.getName();

				head[0][i] = new EasyUIDatagridHead(obmk.get("kmdm").toString() + "_score", subjectName, "center", 50,
						1, 1, true);
				i++;
			}
			head[0][bjcjMk.size()] = new EasyUIDatagridHead("totalScore", "总分", "center", 50, 1, 1, true);
			head[0][1 + bjcjMk.size()] = new EasyUIDatagridHead("totalScoreLevel", "等第", "center", 50, 1, 1, true);
			if (ifExistsBjcjzhcj) {
				head[0][2 + bjcjMk.size()] = new EasyUIDatagridHead("compositiveLevel", "综合等第", "center", 100, 1, 1,
						true);
				head[0][3 + bjcjMk.size()] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1, 1,
						true);

				head[0][4 + bjcjMk.size()] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1, 1,
						true);
				head[0][5 + bjcjMk.size()] = new EasyUIDatagridHead("lastExamRank", "上次考试名次", "center", 100, 1, 1,
						true);
				head[0][6 + bjcjMk.size()] = new EasyUIDatagridHead("upDownRank", "升降", "center", 50, 1, 1, true);

			} else {
				head[0][2 + bjcjMk.size()] = new EasyUIDatagridHead("totalScoreClassRank", "班名", "center", 50, 1, 1,
						true);

				head[0][3 + bjcjMk.size()] = new EasyUIDatagridHead("totalScoreGradeRank", "年名", "center", 50, 1, 1,
						true);
				head[0][4 + bjcjMk.size()] = new EasyUIDatagridHead("lastExamRank", "上次考试名次", "center", 100, 1, 1,
						true);
				head[0][5 + bjcjMk.size()] = new EasyUIDatagridHead("upDownRank", "升降", "center", 50, 1, 1, true);
			}

			// 把班级成绩数据放置在Map
			Map<String, JSONObject> bjcjcjMap = new HashMap<String, JSONObject>();
			for (JSONObject obCj : bjcjCj) {
				String xh = obCj.getString("xh");
				String kmdm = obCj.getString("kmdm");

				if (!bjcjcjMap.containsKey(xh + kmdm)) {
					bjcjcjMap.put(xh + kmdm, obCj);
				}
			}

			for (JSONObject obXs : bjcjXs) {
				String studentId = obXs.getString("studentNo");
				String classId = obXs.getString("classId");
				// 设置学生姓名和班级名称
				if (studentsMap != null) {
					User user = studentsMap.get(studentId);
					Account account = user == null ? null : user.getAccountPart();
					String name = account == null ? "" : account.getName();

					StudentPart student = user == null ? null : user.getStudentPart();
					String stdNumber = student == null ? "" : student.getSchoolNumber();// 学号
					obXs.put("studentName", name);
					obXs.put("studentId", stdNumber);
				}
				if (classroomsMap != null) {
					obXs.put("className", classroomsMap.get(classId).getClassName());
				}

				for (JSONObject obMk : bjcjMk) {
					String kmdm = obMk.getString("kmdm");

					JSONObject obCj = bjcjcjMap.get(studentId + kmdm);

					if (obCj != null) {
						obXs.put(kmdm + "_score", obCj.get("cj"));
					}
				}
			}
		}

		Collections.sort(bjcjXs, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("className");
				String className2 = arg2.getString("className");
				return className1.compareTo(className2);
			}
		});

		JSONObject data = new JSONObject();
		data.put("total", bjcjXs.size());
		data.put("rows", bjcjXs);
		data.put("columns", head);
		data.put("code", 1);
		data.put("msg", "");
		return data;
	}

	@Override
	public JSONObject getStudentLevelWeakSubjectList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");
		String bhStr = params.getString("bhStr");
		String kmdmStr = params.getString("kmdmStr");
		params.remove("bhStr");
		params.remove("kmdmStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		params.put("kmdmList", StringUtil.convertToListFromStr(kmdmStr, ",", String.class));

		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		classIds = null;
		if (CollectionUtils.isEmpty(classroomList)) {
			JSONObject data = new JSONObject();
			data.put("total", 0);
			data.put("rows", new ArrayList<JSONObject>());
			return data;
		}

		List<Long> accIds = new ArrayList<Long>();
		Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>(); // 班级类别Map
		for (Classroom classroom : classroomList) { // 把班级信息放置到Map中
			if (classroom == null) {
				continue;
			}
			if (!classroomMap.containsKey(classroom.getId())) {
				classroomMap.put(classroom.getId(), classroom);
				if(CollectionUtils.isEmpty(classroom.getStudentAccountIds())) {
					continue;
				}
				accIds.addAll(classroom.getStudentAccountIds());
			}
		}

		// 基础数据接口获取学生信息，循环删除掉不属于该列表的薄弱科目数据记录，同时设置上班级名称和学名属性值。
		List<Account> accounts = allCommonDataService.getAccountBatch(schoolId, accIds, xnxq);
		accIds = null;
		if (CollectionUtils.isEmpty(accounts)) {
			JSONObject data = new JSONObject();
			data.put("total", 0);
			data.put("rows", new ArrayList<JSONObject>());
			return data;
		}

		List<String> studentnos = new ArrayList<String>();// 学号列表
		Map<String, User> usersMap = new HashMap<String, User>();// 用户信息列表，用来获取学生姓名，班级信息的。
		for (Account account : accounts) {// 循环账号信息，组织学号List和班号List，并且把学生信息放置到Map中
			if (account == null || account.getUsers() == null) {
				continue;
			}

			String accId = String.valueOf(account.getId());
			for (User user : account.getUsers()) {
				if (user == null || user.getUserPart() == null) {
					continue;
				}
				if (T_Role.Student.equals(user.getUserPart().getRole())) {
					if (!usersMap.containsKey(accId)) {
						usersMap.put(accId, user);
						studentnos.add(accId);
					}
				}
			}
		}
		params.put("xhList", studentnos);

		List<Long> subjectIds = StringUtil.convertToListFromStr(kmdmStr.replace("totalScore", "0"), ",", Long.class);
		List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);// 获取科目信息
		subjectIds = null;

		Map<Long, LessonInfo> lessonInfoMap = new HashMap<Long, LessonInfo>();// 存放科目信息Map
		if (CollectionUtils.isNotEmpty(lessonInfos)) {
			for (LessonInfo lessonInfo : lessonInfos) {// 把科目信息放置到Map中
				if (lessonInfo == null) {
					continue;
				}
				if (!lessonInfoMap.containsKey(lessonInfo.getId())) {
					lessonInfoMap.put(lessonInfo.getId(), lessonInfo);
				}
			}
		}

		List<Long> allKmdm = studentLevelReportDao.getGetAllKmdm(xnxq, autoIncr, params);
		int kmCount = allKmdm.size() - 1;
		params.put("kmCount", kmCount);

		// String sqlwhere = "and b.djxl like '" + mkCount + "A%'";
		// map.put("subsql", sqlwhere);
		List<JSONObject> getBrkm = studentLevelReportDao.getGetBrkm(xnxq, autoIncr, params);

		List<JSONObject> backDataList = new ArrayList<JSONObject>();
		for (JSONObject jsonObject : getBrkm) {// 循环数据记录，设置基础数据值
			long subjectId = jsonObject.getLongValue("subjectId");
			String studentNo = jsonObject.getString("studentNo");

			String subjectName = lessonInfoMap.get(subjectId) == null ? "" : lessonInfoMap.get(subjectId).getName();

			User user = usersMap.get(studentNo);
			long classId = 0;
			String studentName = "";
			String studentId = "";
			if (user != null) {
				classId = user.getStudentPart().getClassId();
				studentName = user.getAccountPart().getName();
				studentId = user.getStudentPart().getSchoolNumber();
			}

			String className = "";// 班级名称
			if (classroomMap.get(classId) != null) {
				className = classroomMap.get(classId).getClassName();
			}

			jsonObject.put("studentId", studentId);
			jsonObject.put("weakSubjectName", subjectName);
			jsonObject.put("className", className);
			jsonObject.put("studentName", studentName);

			backDataList.add(jsonObject);
		}

		Collections.sort(backDataList, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject arg1, JSONObject arg2) {
				String className1 = arg1.getString("className");
				String className2 = arg2.getString("className");
				return className1.compareTo(className2);
			}
		});

		JSONObject data = new JSONObject();
		data.put("total", backDataList.size());
		data.put("rows", backDataList);
		return data;
	}

	@Override
	public JSONObject getLevelSubjectAOneThirdStatisList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");
		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));

		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		classIds = null;

		if (CollectionUtils.isEmpty(classroomList)) {
			JSONObject data = new JSONObject();
			data.put("total", 0);
			data.put("columns", new ArrayList<JSONObject>(2));
			data.put("rows", new ArrayList<JSONObject>(0));
			data.put("topmsg", "");
			return data;
		}

		List<Long> tids = new ArrayList<Long>();
		List<String> xhList = new ArrayList<String>();
		Map<String, Classroom> cmap = new HashMap<String, Classroom>();// 班级集合
		Map<String, Classroom> smap = new HashMap<String, Classroom>();// 班级集合
																		// 以学生id为key
		for (Classroom classroom : classroomList) {
			if (classroom == null) {
				continue;
			}

			if (!tids.contains(classroom.getDeanAccountId())) {
				tids.add(classroom.getDeanAccountId());
			}

			cmap.put(String.valueOf(classroom.getId()), classroom);

			List<Long> studIds = classroom.getStudentAccountIds();
			if (studIds == null) {
				continue;
			}

			for (Long sid : studIds) {
				String id = String.valueOf(sid);
				xhList.add(id);
				smap.put(id, classroom);
			}
		}
		params.put("xhList", xhList);
		xhList = null;

		Map<String, Account> tmap = new HashMap<String, Account>();// 老师集合
		List<Account> tlist = allCommonDataService.getAccountBatch(schoolId, tids, xnxq);// 老师信息
		if (CollectionUtils.isNotEmpty(tlist)) {
			for (Account acc : tlist) {
				if (acc == null || acc.getUsers() == null) {
					continue;
				}
				for (User user : acc.getUsers()) {
					if (user == null || user.getUserPart() == null) {
						continue;
					}

					if (T_Role.Teacher.equals(user.getUserPart().getRole())) {
						List<Long> accIds = user.getTeacherPart().getDeanOfClassIds();
						if (CollectionUtils.isNotEmpty(accIds)) {
							for (Long accId : accIds) {
								tmap.put(String.valueOf(accId), acc);
							}
						}
					}
				}
			}
		}

		List<JSONObject> dataList = studentLevelReportDao.getAllTdTj(xnxq, autoIncr, params);

		EasyUIDatagridHead[][] head = new EasyUIDatagridHead[2][];
		int totalCkrs = 0; // 总参考人数
		int i3 = 0;
		if (CollectionUtils.isNotEmpty(dataList)) {
			for (JSONObject tdtj : dataList) {
				String bh = tdtj.getString("bh");
				tdtj.put("className", "");
				if (cmap.get(bh) != null) { // 班级信息
					tdtj.put("className", cmap.get(bh).getClassName());
				}
				if (tmap.get(bh) != null) { // 班主任
					tdtj.put("classAdviser", tmap.get(bh).getName());
				}

				tdtj.put("referenceNum", tdtj.getIntValue("ckrs")); // 参考人数

				totalCkrs += tdtj.getIntValue("ckrs");
			}

			Collections.sort(dataList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject arg1, JSONObject arg2) {
					String className1 = arg1.getString("className");
					String className2 = arg2.getString("className");
					return className1.compareTo(className2);
				}
			});

			// 在最后加一条合计行
			JSONObject totalJsonObject = new JSONObject();
			totalJsonObject.put("classAdviser", "合计");
			dataList.add(totalJsonObject);

			i3 = (int) (totalCkrs / 3);

			// 查科目
			List<Long> kmList = studentLevelReportDao.getAllKmFromStatisticsRankMk(xnxq, autoIncr, params);
			Collections.sort(kmList, new Comparator<Long>() {
				@Override
				public int compare(Long kmdm1, Long kmdm2) {
					return Long.compare(kmdm1, kmdm2);
				}
			});

			head[0] = new EasyUIDatagridHead[kmList.size()];
			head[1] = new EasyUIDatagridHead[3 * kmList.size()];

			Map<String, LessonInfo> kmap = new HashMap<String, LessonInfo>();// 科目集合
			List<LessonInfo> llist = allCommonDataService.getLessonInfoBatch(schoolId, kmList, String.valueOf(xnxq));// 科目信息
			if (CollectionUtils.isNotEmpty(llist)) {
				for (LessonInfo lessonInfo : llist) {
					if (lessonInfo == null)
						continue;
					kmap.put(String.valueOf(lessonInfo.getId()), lessonInfo);
				}
			}
			for (int i = 0; i < kmList.size(); i++) {
				int startCol = i * 3;
				String kmdm = String.valueOf(kmList.get(i));
				// 修改<--------
				String zwmc = "";
				if (kmap.get(kmdm) != null) {
					zwmc = kmap.get(kmdm).getName();
				}
				// 修改结束
				head[0][i] = new EasyUIDatagridHead(null, zwmc, "center", 100, 1, 3, true);

				head[1][startCol] = new EasyUIDatagridHead(kmdm, zwmc + "A等人数", "center", 100, 1, 1, true);
				head[1][startCol + 1] = new EasyUIDatagridHead(kmdm + "OneThirdStuNum", zwmc + "年级名次前" + i3 + "名",
						"center", 150, 1, 1, true);
				head[1][startCol + 2] = new EasyUIDatagridHead(kmdm + "_teacherName", "任课教师", "center", 100, 1, 1,
						true);

			}

			List<JSONObject> dkdjList = studentLevelReportDao.getEverySubjectScore(xnxq, autoIncr, params);

			// 筛选，去多
			for (Iterator<JSONObject> it = dkdjList.iterator(); it.hasNext();) {
				JSONObject json = it.next();
				if (smap.get(json.getString("xh")) != null) {
					json.put("bh", smap.get(json.getString("xh")).getId());
				} else {
					it.remove();
				}
			}

			// List<JSONObject> dvjsList =
			// studentLevelReportService.sql_bjjs(map); //查看修改3<------

			int _totalCkrs = 0;
			JSONObject lastRow = dataList.get(dataList.size() - 1);
			// 为增加的字段填充数据
			for (JSONObject dataRow : dataList) {
				String _bh = dataRow.getString("bh");
				if (StringUtils.isNotBlank(_bh)) {

					if (dataRow.get("ckrs") != null) {
						_totalCkrs += dataRow.getIntValue("ckrs");
					}

					for (Long subjectId : kmList) {
						String mkdm = String.valueOf(subjectId);
						// 从 subjectScoreList 集合中过滤条件
						// " bh=" + strBh + " and mkdm=" + mkdm + " and (dj='A'
						// or dj='A+')"
						List<JSONObject> filterDataListA = this.filterDkdjListByA(dkdjList, _bh, mkdm);
						dataRow.put(mkdm, filterDataListA.size());
						// 合计数
						if (lastRow.getString(mkdm) != null) {
							lastRow.put(mkdm, lastRow.getIntValue(mkdm) + dataRow.getIntValue(mkdm));
						} else {
							lastRow.put(mkdm, dataRow.getIntValue(mkdm));
						}

						List<JSONObject> filterDataListTop = this.filterDkdjListByTop(dkdjList, _bh, mkdm, i3);
						String topMkdmName = mkdm + "OneThirdStuNum";
						dataRow.put(topMkdmName, filterDataListTop.size());
						if (lastRow.getString(topMkdmName) != null) {
							lastRow.put(topMkdmName,
									lastRow.getIntValue(topMkdmName) + dataRow.getIntValue(topMkdmName));
						} else {
							lastRow.put(topMkdmName, dataRow.getIntValue(topMkdmName));
						}

						// 修改3
						List<JSONObject> filterDvjsList = this.filterDvjsList(cmap, schoolId, _bh, mkdm,
								String.valueOf(xnxq));
						// 修改结束
						String jsstr = "";
						for (JSONObject drRow : filterDvjsList) {
							jsstr += StringUtils.isBlank(jsstr) ? drRow.getString("xm") : "," + drRow.getString("xm");
						}
						dataRow.put(mkdm + "_teacherName", jsstr);
					}
				}
			}
			lastRow.put("referenceNum", _totalCkrs);
		}

		// 参考人数*1/3
		String topmsg = i3 + "";

		// 返回构造的JSON对象
		return WebParamUtil.buildJsonObj(dataList.size(), dataList, head, topmsg);
	}

	private List<JSONObject> filterDkdjListByA(List<JSONObject> dkdjList, String bh, String mkdm) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : dkdjList) {
			if (dataRow.getString("bh").equals(bh) && dataRow.getString("kmdm").equals(mkdm)) {
				if (dataRow.getString("dj") != null && dataRow.getString("dj").equals("A")
						|| dataRow.getString("dj") != null && dataRow.getString("dj").equals("A+")) {
					dataList.add(dataRow);
				}
			}
		}
		return dataList;
	}

	private List<JSONObject> filterDkdjListByTop(List<JSONObject> dkdjList, String bh, String mkdm, int topNum) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : dkdjList) {
			if (dataRow.getString("bh").equals(bh) && dataRow.getString("kmdm").equals(mkdm)) {
				if (dataRow.getIntValue("njpm") <= topNum) {
					dataList.add(dataRow);
				}
			}
		}
		return dataList;
	}

	private List<JSONObject> filterDvjsList(Map<String, Classroom> cmap, long schoolId, String bh, String mkdm,
			String termInfoId) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		List<Long> tid = new ArrayList<Long>();
		HashMap<String, Account> tmap = new HashMap<String, Account>();
		if (cmap.get(bh) != null) {

			if (cmap.get(bh).getAccountLessons() != null && cmap.get(bh).getAccountLessons().size() > 0) {
				List<AccountLesson> alist = cmap.get(bh).getAccountLessons();
				for (AccountLesson as : alist) {
					if (String.valueOf(as.getLessonId()).equals(mkdm)) {
						tid.add(as.getAccountId());
					}
				}
			}
			// System.out.println(tid.toString());
			if (tid.size() > 0) {
				List<Account> alist = allCommonDataService.getAccountBatch(schoolId, tid, termInfoId);
				for (Account a : alist) {
					tmap.put(String.valueOf(a.getId()), a);
				}
				if (cmap.get(bh).getAccountLessons() != null && cmap.get(bh).getAccountLessons().size() > 0) {
					for (AccountLesson as : cmap.get(bh).getAccountLessons()) {
						if (String.valueOf(as.getLessonId()).equals(mkdm)) {
							if (tmap.get(String.valueOf(as.getAccountId())) != null) {
								JSONObject o = new JSONObject();
								o.put("xm", tmap.get(String.valueOf(as.getAccountId())).getName());
								dataList.add(o);
							}
						}
					}
				}
			} else {
				JSONObject o = new JSONObject();
				o.put("xm", "");
				dataList.add(o);
			}

		}
		return dataList;
	}

	@Override
	public JSONObject getLevelTotalScoreStaticList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);
		
		Long schoolId = params.getLong("xxdm");
		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		
		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> clist = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);// 班级信息
		
		Map<String, Classroom> cmap = new HashMap<String, Classroom>();// 班级集合
		List<Long> tids = new ArrayList<Long>();
		
		if (CollectionUtils.isNotEmpty(clist)) {
			for (Classroom cr : clist) {
				if (cr == null) {
					continue;
				}
				cmap.put(String.valueOf(cr.getId()), cr);
				if(!tids.contains(cr.getDeanAccountId())) {
					tids.add(cr.getDeanAccountId());
				}
			}
		}
		
		Map<String, Account> tmap = new HashMap<String, Account>();// 老师集合
		List<Account> tlist = allCommonDataService.getAccountBatch(schoolId, tids, xnxq);// 老师信息
		if (CollectionUtils.isNotEmpty(tlist)) {
			for (Account acc : tlist) {
				if (acc == null || acc.getUsers() == null) {
					continue;
				}
				for (User user : acc.getUsers()) {
					if (user == null || user.getUserPart() == null) {
						continue;
					}
					if (T_Role.Teacher.equals(user.getUserPart().getRole())) {
						if(user.getTeacherPart() == null || user.getTeacherPart().getDeanOfClassIds() == null) {
							continue;
						}
						for (Long c : user.getTeacherPart().getDeanOfClassIds()) {
							if (c == null) {
								continue;
							}
							tmap.put(String.valueOf(c), acc);
						}
					}
				}
			}
		}

		EasyUIDatagridHead[][] head = new EasyUIDatagridHead[1][];

		List<JSONObject> dataList = new ArrayList<JSONObject>();

		List<JSONObject> rsList = studentLevelReportDao.sql_ddzftjb(xnxq, autoIncr, params);

		List<JSONObject> bjList = studentLevelReportDao.sql_getAllBj(xnxq, autoIncr, params);

		List<String> zbList = studentLevelReportDao.sql_getAllDjxlInfo(xnxq, autoIncr, params);

		if (rsList != null && rsList.size() > 0) {

			if (bjList != null && bjList.size() > 0) {
				head[0] = new EasyUIDatagridHead[bjList.size() + 1];

				// 最上面3条固定数据
				String[] topRowsName = new String[] { "班主任", "最高分", "人数" };
				for (String rowName : topRowsName) {
					JSONObject topRow = new JSONObject();
					topRow.put("projectName", rowName);
					dataList.add(topRow);
				}

				for (String djxl : zbList) {
					JSONObject row = new JSONObject();
					row.put("projectName", djxl);
					dataList.add(row);
				}

				// zbList放置集合中
				Map<String, JSONObject> rsListMap = new HashMap<String, JSONObject>();
				for (JSONObject dataRow : rsList) {
					String classId = dataRow.getString("bhs");
					String levelSequene = dataRow.getString("djxl");
					if (!rsListMap.containsKey(classId + levelSequene))
						rsListMap.put(classId + levelSequene, dataRow);
				}

				int i = 0;
				int zckrs = 0;
				BigDecimal zzgf = new BigDecimal(0);
				int[] zrs = new int[zbList.size()];
				int[] zljrs = new int[zbList.size()];
				for (JSONObject bj : bjList) {

					String className = "";
					String teacherName = "";

					if (cmap.get(bj.getString("bhs")) != null) {
						className = cmap.get(bj.getString("bhs")).getClassName();
					}
					if (tmap.get(bj.getString("bhs")) != null) {
						teacherName = tmap.get(bj.getString("bhs")).getName();
					}
					String sbh = bj.getString("bhs");
					String bjmc = className;
					String bzr = teacherName;
					BigDecimal zgf = new BigDecimal(bj.getString("zgf"));
					int ckrs = bj.getIntValue("ckrs");

					head[0][i] = new EasyUIDatagridHead(sbh, bjmc, "center", 100, 1, 1, true);

					dataList.get(0).put(sbh, bzr);
					dataList.get(1).put(sbh, zgf);
					dataList.get(2).put(sbh, ckrs);

					zckrs += ckrs;
					if (zgf.compareTo(zzgf) == 1)
						zzgf = zgf;

					int j = 3;
					for (String djxl : zbList) {
						int rs = 0, ljrs = 0;

						JSONObject filterData = rsListMap.get(sbh + djxl);
						if (filterData != null) {
							rs = filterData.getIntValue("rs");
							ljrs = filterData.getIntValue("ljrs");
							dataList.get(j).put(sbh, rs + "/" + ljrs);
							zrs[j - 3] += rs;
							zljrs[j - 3] += ljrs;

						}
						j++;
					}
					i++;
				}
				//
				head[0][i] = new EasyUIDatagridHead("all", "全部", "center", 100, 1, 1, true);
				dataList.get(1).put("all", zzgf);
				dataList.get(2).put("all", zckrs);
				int n = 3;
				for (String djxl : zbList) {
					dataList.get(n).put("all", zrs[n - 3] + "/" + zljrs[n - 3]);
					n++;
				}
				this.filterQbNotEmpty(dataList);
			}
		}
		Arrays.sort(head[0], new Comparator<EasyUIDatagridHead>(){
			@Override
			public int compare(EasyUIDatagridHead arg0, EasyUIDatagridHead arg1) {
				String title0 = arg0.title;
				String title1 = arg1.title;
				if("全部".equals(title0))	{
					return 1;
				}
				if("全部".equals(title1)) {
					return -1;
				}
				return title0.compareTo(title1);
			}
		});
		return WebParamUtil.buildJsonObj(dataList.size(), dataList, head, "");
	}
	
	private List<JSONObject> filterQbNotEmpty(List<JSONObject> sourceList) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : sourceList) {
			if (StringUtils.isEmpty(dataRow.getString("qb"))) {
				dataList.remove(dataRow);
			}
		}
		return dataList;
	}

	@Override
	public JSONObject getLevelEveryAStatisTabList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);
		
		Long schoolId = params.getLong("xxdm");
		String bhStr = params.getString("bhStr");
		params.remove("bhStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		
		int djxllb = params.getIntValue("subjectType");
		params.remove("subjectType");
		
		if (djxllb == 0) {
			params.put("djxllb", "01");
			params.put("isNoAdd", "yes");
		} else {
			params.put("djxllb", "02");
		}

		List<JSONObject> dataList = studentLevelReportDao.getAllTdTj(xnxq, autoIncr, params);

		EasyUIDatagridHead[][] head = new EasyUIDatagridHead[1][];

		Map<String, Object> sm = null;
		if (CollectionUtils.isNotEmpty(dataList)) {
			Map<String, Classroom> cmap = new HashMap<String, Classroom>();// 班级集合
			Map<String, Account> tmap = new HashMap<String, Account>();// 老师集合

			List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
			List<Classroom> clist = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);// 班级信息
			
			List<Long> tids = new ArrayList<Long>();
			List<String> xhList = new ArrayList<String>();
			if (clist != null) {
				for (Classroom classroom : clist) {
					if (classroom == null) {
						continue;
					}
					String classId = String.valueOf(classroom.getId());
					if (!cmap.containsKey(classId)) {
						cmap.put(classId, classroom);
						for(Long id : classroom.getStudentAccountIds()) {
							xhList.add(String.valueOf(id));
						}
					}
						
					if (!tids.contains(classroom.getDeanAccountId())) {
						tids.add(classroom.getDeanAccountId());
					}
				}
			}
			if(xhList.size() > 0) {
				params.put("xhList", xhList);
			}
			
			List<Account> tlist = allCommonDataService.getAccountBatch(schoolId, tids, xnxq);// 老师信息
			if (tlist != null) {
				for (Account acc : tlist) {
					if (acc == null || acc.getUsers() == null) {
						continue;
					}
					for (User user : acc.getUsers()) {
						if (user == null || user.getUserPart() == null || user.getUserPart().getRole() == null) {
							continue;
						}
						if (T_Role.Teacher.equals(user.getUserPart().getRole())) {
							if(user.getTeacherPart() == null || user.getTeacherPart().getDeanOfClassIds() == null) {
								continue;
							}
							for (Long c : user.getTeacherPart().getDeanOfClassIds()) {
								if (c == null) {
									continue;
								}
								String classId = String.valueOf(c);
								if (!tmap.containsKey(classId)) {
									tmap.put(String.valueOf(c), acc);
								}
							}
						}
					}
				}
			}
			
			List<JSONObject> djxlList = studentLevelReportDao.getAllZfxl(xnxq, autoIncr, params);
			List<JSONObject> noAjList = studentLevelReportDao.getNoA(xnxq, autoIncr, params);
			
			List<JSONObject> pmjList = new ArrayList<JSONObject>();
			if(xhList.size() > 0) {
				pmjList.addAll(studentLevelReportDao.getZfJg(xnxq, autoIncr, params));
			}
			
			int cc = pmjList.size();
			int last = 0;
			if (cc > 0) {
				last = pmjList.get(cc - 1).getIntValue("njpm");
			}

			int mf = dataList.get(0).getIntValue("mf");
			int inine = (int) (mf * 0.9);
			int isix = (int) (mf * 0.6);
			int totalCkrs = 0;
			int totalHgrs = 0;

			for (JSONObject row : dataList) {
				String tname = "";
				String cname = "";
				String bh = row.getString("bh");
				if (tmap.get(bh) != null) {
					tname = tmap.get(bh).getName();
				}

				if (cmap.get(bh) != null) {
					cname = cmap.get(bh).getClassName();
				}
				
				row.put("classAdviser", tname);
				row.put("className", cname);

				int temp = row.getIntValue("ckrs");
				int temp2 = row.getIntValue("hgrs");
				totalCkrs += temp;
				totalHgrs += temp2;
			}

			Collections.sort(dataList, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject arg1, JSONObject arg2) {
					String className1 = arg1.getString("className");
					String className2 = arg2.getString("className");
					return className1.compareTo(className2);
				}
			});

			// 在最后加一条合计行
			JSONObject totalJsonObject = new JSONObject();
			totalJsonObject.put("classAdviser", "合计");
			dataList.add(totalJsonObject);

			// 参考人数的1/3
			int i3 = (int) (totalCkrs / 3);

			List<JSONObject> mkList = studentLevelReportDao.getAllKskm(xnxq, autoIncr, params);

			if (mkList != null && mkList.size() > 0) {

				int intKms = mkList.get(0).getIntValue("mks");
				int addRange = 0;
				if (mkList.size() > 1) {
					addRange = mkList.get(1).getIntValue("mks");
				}
				intKms = intKms + addRange;
				if (intKms <= 5) {

					head[0] = new EasyUIDatagridHead[11 + intKms];
					head[0][intKms] = new EasyUIDatagridHead("1Aup", "1A以上合计", "center", 100, 1, 1, true);
					head[0][intKms + 1] = new EasyUIDatagridHead("noA", "无一个A人数", "center", 100, 1, 1, true);
					head[0][intKms + 2] = new EasyUIDatagridHead("first50", "前50名", "center", 50, 1, 1, true);
					head[0][intKms + 3] = new EasyUIDatagridHead("first100", "前100名共计", "center", 100, 1, 1, true);
					head[0][intKms + 4] = new EasyUIDatagridHead("first200", "前200名共计", "center", 100, 1, 1, true);
					head[0][intKms + 5] = new EasyUIDatagridHead("last100", "后100名", "center", 50, 1, 1, true);
					head[0][intKms + 6] = new EasyUIDatagridHead("last50", "后50名", "center", 50, 1, 1, true);
					head[0][intKms + 7] = new EasyUIDatagridHead(inine + "up", inine + "分以上", "center", 100, 1, 1,
							true);
					head[0][intKms + 8] = new EasyUIDatagridHead(isix + "down", isix + "以下", "center", 100, 1, 1, true);
					head[0][intKms + 9] = new EasyUIDatagridHead("first3", "级名" + i3 + "(1/3)名人数", "center", 150, 1, 1,
							true);
					head[0][intKms + 10] = new EasyUIDatagridHead("passRateStudentNum", "总分及格人数", "center", 100, 1, 1,
							true);

				} else if (intKms < 7) {
					head[0] = new EasyUIDatagridHead[17];
					head[0][5] = new EasyUIDatagridHead(intKms - 4 + "Aup", intKms - 4 + "A以上合计", "center", 100, 1, 1,
							true);
					head[0][6] = new EasyUIDatagridHead("1A", "1A人数", "center", 100, 1, 1, true);
					head[0][7] = new EasyUIDatagridHead("noA", "无一个A人数", "center", 100, 1, 1, true);
					head[0][8] = new EasyUIDatagridHead("first50", "前50名", "center", 50, 1, 1, true);
					head[0][9] = new EasyUIDatagridHead("first100", "前100名共计", "center", 100, 1, 1, true);
					head[0][10] = new EasyUIDatagridHead("first200", "前200名共计", "center", 100, 1, 1, true);
					head[0][11] = new EasyUIDatagridHead("last100", "后100名", "center", 50, 1, 1, true);
					head[0][12] = new EasyUIDatagridHead("last50", "后50名", "center", 50, 1, 1, true);
					head[0][13] = new EasyUIDatagridHead(inine + "up", inine + "分以上", "center", 100, 1, 1, true);
					head[0][14] = new EasyUIDatagridHead(isix + "down", isix + "以下", "center", 100, 1, 1, true);
					head[0][15] = new EasyUIDatagridHead("first3", "级名" + i3 + "(1/3)名人数", "center", 150, 1, 1, true);
					head[0][16] = new EasyUIDatagridHead("passRateStudentNum", "总分及格人数", "center", 100, 1, 1, true);
				} else {
					head[0] = new EasyUIDatagridHead[18];
					head[0][5] = new EasyUIDatagridHead(intKms - 4 + "Aup", intKms - 4 + "A以上合计", "center", 100, 1, 1,
							true);
					head[0][6] = new EasyUIDatagridHead("2A", "2A人数", "center", 100, 1, 1, true);
					head[0][7] = new EasyUIDatagridHead("1A", "1A人数", "center", 100, 1, 1, true);
					head[0][8] = new EasyUIDatagridHead("noA", "无一个A人数", "center", 100, 1, 1, true);
					head[0][9] = new EasyUIDatagridHead("first50", "前50名", "center", 50, 1, 1, true);
					head[0][10] = new EasyUIDatagridHead("first100", "前100名共计", "center", 100, 1, 1, true);
					head[0][11] = new EasyUIDatagridHead("first200", "前200名共计", "center", 100, 1, 1, true);
					head[0][12] = new EasyUIDatagridHead("last100", "后100名", "center", 50, 1, 1, true);
					head[0][13] = new EasyUIDatagridHead("last50", "后50名", "center", 50, 1, 1, true);
					head[0][14] = new EasyUIDatagridHead(inine + "up", inine + "分以上", "center", 100, 1, 1, true);
					head[0][15] = new EasyUIDatagridHead(isix + "down", isix + "以下", "center", 100, 1, 1, true);
					head[0][16] = new EasyUIDatagridHead("first3", "级名" + i3 + "(1/3)名人数", "center", 150, 1, 1, true);
					head[0][17] = new EasyUIDatagridHead("passRateStudentNum", "总分及格人数", "center", 100, 1, 1, true);
				}

				if (intKms == 1) {
					head[0][intKms - 1] = new EasyUIDatagridHead("1A", "1A总数", "center", 50, 1, 1, true);

				} else if (intKms == 2) {
					head[0][intKms - 2] = new EasyUIDatagridHead("2A", "2A总数", "center", 50, 1, 1, true);
					head[0][intKms - 1] = new EasyUIDatagridHead("1A", "1A总数", "center", 50, 1, 1, true);

				} else if (intKms == 3) {
					head[0][intKms - 3] = new EasyUIDatagridHead("3A", "3A总数", "center", 50, 1, 1, true);
					head[0][intKms - 2] = new EasyUIDatagridHead("2A", "2A总数", "center", 50, 1, 1, true);
					head[0][intKms - 1] = new EasyUIDatagridHead("1A", "1A总数", "center", 50, 1, 1, true);

				} else if (intKms == 4) {
					head[0][intKms - 4] = new EasyUIDatagridHead("4A", "4A总数", "center", 50, 1, 1, true);
					head[0][intKms - 3] = new EasyUIDatagridHead("3A", "3A总数", "center", 50, 1, 1, true);
					head[0][intKms - 2] = new EasyUIDatagridHead("2A", "2A总数", "center", 50, 1, 1, true);
					head[0][intKms - 1] = new EasyUIDatagridHead("1A", "1A总数", "center", 50, 1, 1, true);

				} else if (intKms >= 5) {
					head[0][0] = new EasyUIDatagridHead(intKms + "A", intKms + "A总数", "center", 50, 1, 1, true);
					head[0][1] = new EasyUIDatagridHead(intKms - 1 + "A", intKms - 1 + "A总数", "center", 50, 1, 1, true);
					head[0][2] = new EasyUIDatagridHead(intKms - 2 + "A", intKms - 2 + "A总数", "center", 50, 1, 1, true);
					head[0][3] = new EasyUIDatagridHead(intKms - 3 + "A", intKms - 3 + "A总数", "center", 50, 1, 1, true);
					head[0][4] = new EasyUIDatagridHead(intKms - 4 + "A", intKms - 4 + "A总数", "center", 50, 1, 1, true);

				}

			} else {
				head[0] = new EasyUIDatagridHead[9];
				head[0][0] = new EasyUIDatagridHead("first50", "前50名", "center", 50, 1, 1, true);
				head[0][1] = new EasyUIDatagridHead("first100", "前100名共计", "center", 100, 1, 1, true);
				head[0][2] = new EasyUIDatagridHead("first200", "前200名共计", "center", 100, 1, 1, true);
				head[0][3] = new EasyUIDatagridHead("last100", "后100名", "center", 50, 1, 1, true);
				head[0][4] = new EasyUIDatagridHead("last50", "后50名", "center", 50, 1, 1, true);
				head[0][5] = new EasyUIDatagridHead(inine + "up", inine + "分以上", "center", 100, 1, 1, true);
				head[0][6] = new EasyUIDatagridHead(isix + "down", isix + "以下", "center", 100, 1, 1, true);
				head[0][7] = new EasyUIDatagridHead("first3", "级名" + i3 + "(1/3)名人数", "center", 150, 1, 1, true);
				head[0][8] = new EasyUIDatagridHead("passRateStudentNum", "总分及格人数", "center", 100, 1, 1, true);

			}

			sm = new HashMap<String, Object>();
			sm.put("90up", inine);
			sm.put("60down", isix);
			sm.put("first3", i3);

			List<Long> sid = new ArrayList<Long>();
			for (JSONObject pmj : pmjList) {
				sid.add(Long.valueOf(pmj.getString("xh")));
			}
			
			Map<String, String> smap = new HashMap<String, String>();
			List<Account> alist = allCommonDataService.getAccountBatch(schoolId, sid, xnxq);
			if (CollectionUtils.isNotEmpty(alist)) {
				for (Account a : alist) {
					if (a == null || a.getUsers() == null) {
						continue;
					}
					for (User user : a.getUsers()) {
						if (user == null || user.getUserPart() == null) {
							continue;
						}
						if (T_Role.Student.equals(user.getUserPart().getRole())) {
							if (user.getUserPart() != null)
								smap.put(String.valueOf(a.getId()), String.valueOf(user.getStudentPart().getClassId()));
						}
					}
				}
			}

			Map<String, List<JSONObject>> pmjListMap = new HashMap<String, List<JSONObject>>();
			for (JSONObject pmj : pmjList) {
				sid.add(Long.valueOf(pmj.getString("xh")));
				String classid = "";
				if (smap.get(pmj.getString("xh")) != null) {
					classid = smap.get(pmj.getString("xh"));
				}

				if (pmjListMap.containsKey(classid)) {
					pmjListMap.get(classid).add(pmj);
				} else if (!"".equals(classid)) {
					List<JSONObject> list = new ArrayList<JSONObject>();
					list.add(pmj);
					pmjListMap.put(classid, list);
				}
			}

			JSONObject rlast = dataList.get(dataList.size() - 1);
			int totalfirst50 = 0, totalfirst100 = 0, totalfirst200 = 0, totallast50 = 0, totallast100 = 0,
					totalinineup = 0, totalisixup = 0, totalfirst3 = 0;
			int totalNoA = 0, totalCkrs1 = 0, totalHgrs1 = 0;
			int totalAup = 0;

			for (JSONObject dataRow : dataList) {
				int first50 = 0, first100 = 0, first200 = 0, last50 = 0, last100 = 0, inineup = 0, isixup = 0,
						first3 = 0;
				int Aup = 0;

				String strBh = dataRow.getString("bh");
				if (StringUtils.isNotEmpty(strBh)) {
					List<JSONObject> filterDjxList = this.filterDjxl(djxlList, strBh, "");
					if (filterDjxList.size() > 0) {
						for (JSONObject json : filterDjxList) {
							if (!json.getString("xl").equals("")) {
								String[] type = json.getString("xl").split("A");
								dataRow.put(type[0] + "A", json.getString("rs"));
								if (rlast.getString(type[0] + "A") != null) {
									rlast.put(type[0] + "A", Integer.valueOf(rlast.getString(type[0] + "A"))
											+ Integer.valueOf(json.getString("rs")));
								} else {
									rlast.put(type[0] + "A", json.getString("rs"));
								}

								JSONObject mk = mkList != null && mkList.size() > 0 ? mkList.get(0) : null;
								int mks = mk == null ? 0 : mk.getIntValue("mks");
								if (mks <= 5) {
									if (Integer.valueOf(type[0]) >= 1) {
										Aup += Integer.valueOf(json.getString("rs"));
										totalAup += Integer.valueOf(json.getString("rs"));
									}
								} else {
									if (Integer.valueOf(type[0]) >= mks - 4) {
										Aup += Integer.valueOf(json.getString("rs"));
										totalAup += Integer.valueOf(json.getString("rs"));
									}
								}
							}
						}
					}

					if (StringUtils.isNotEmpty(dataRow.getString("ckrs"))) {
						totalCkrs1 += dataRow.getIntValue("ckrs");
					}

					if (StringUtils.isNotEmpty(dataRow.getString("hgrs"))) {
						totalHgrs1 += dataRow.getIntValue("hgrs");
					}

					if (noAjList != null && noAjList.size() > 0) {
						List<JSONObject> filterNoAList = this.filterBH(noAjList, strBh);
						if (filterNoAList.size() == 1) {
							dataRow.put("noA", filterNoAList.get(0).getString("rs"));
							totalNoA += dataRow.getIntValue("noA");
						} else {
							dataRow.put("noA", "0");
						}
					}

					JSONObject mk = mkList != null && mkList.size() > 0 ? mkList.get(0) : null;
					int mks = mk == null ? 0 : mk.getIntValue("mks");
					if (mks <= 5) {
						dataRow.put("1Aup", Aup);
						rlast.put("1Aup", totalAup);
					} else {
						dataRow.put(mks - 4 + "Aup", Aup);
						rlast.put(mks - 4 + "Aup", totalAup);
					}

					if (pmjList != null && pmjList.size() > 0) {

						List<JSONObject> jsonList = pmjListMap.get(strBh);

						for (JSONObject pmj : jsonList) {

							if (pmj.getIntValue("njpm") <= 50) {
								totalfirst50++;
								first50++;
							}

							if (pmj.getIntValue("njpm") <= 100) {
								totalfirst100++;
								first100++;

							}

							if (pmj.getIntValue("njpm") <= 200) {
								totalfirst200++;
								first200++;
							}

							if (pmj.getIntValue("zf") >= inine) {
								totalinineup++;
								inineup++;
							}

							if (pmj.getIntValue("zf") <= isix) {
								totalisixup++;
								isixup++;
							}

							if (pmj.getIntValue("njpm") <= i3) {
								totalfirst3++;
								first3++;
							}

							if (last > 100) {
								if (pmj.getIntValue("njpm") > (last - 100)) {
									totallast100++;
									last100++;
								}

								if (pmj.getIntValue("njpm") > (last - 50)) {
									totallast50++;
									last50++;
								}
							}
						}

						dataRow.put("referenceNum", dataRow.getIntValue("ckrs"));
						dataRow.put("passRateStudentNum", dataRow.getIntValue("hgrs"));
						dataRow.put("first50", first50);
						dataRow.put("first100", first100);
						dataRow.put("first200", first200);
						dataRow.put(inine + "up", inineup);
						dataRow.put(isix + "down", isixup);
						dataRow.put("first3", first3);

						if (last > 100) {
							dataRow.put("last100", last100);
							dataRow.put("last50", last50);
						}

					}

				}
			}

			rlast.put("referenceNum", totalCkrs1);
			rlast.put("passRateStudentNum", totalHgrs1);
			rlast.put("noA", totalNoA);
			rlast.put("first50", totalfirst50);
			rlast.put("first100", totalfirst100);
			rlast.put("first200", totalfirst200);
			rlast.put(inine + "up", totalinineup);
			rlast.put(isix + "down", totalisixup);
			rlast.put("first3", totalfirst3);
			rlast.put("last100", totallast100);
			rlast.put("last50", totallast50);

		}

		return WebParamUtil.buildJsonObj(dataList.size(), dataList, head, sm);
	}
	
	private List<JSONObject> filterDjxl(List<JSONObject> sourceList, String bh, String cname) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : sourceList) {
			if (dataRow.getString("bh").equals(bh)) {
				dataList.add(dataRow);
			}

		}
		return dataList;
	}

	private List<JSONObject> filterBH(List<JSONObject> sourceList, String bh) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : sourceList) {
			if (dataRow.getString("bh").equals(bh)) {
				dataList.add(dataRow);
			}
		}
		return dataList;
	}
}
