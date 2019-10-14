package com.talkweb.scoreManage.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ClassScoreLevelReportDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ClassScoreLevelReportService;
import com.talkweb.scoreManage.service.ScoreManageConfigService;

@Service
public class ClassScoreLevelReportServiceImpl implements ClassScoreLevelReportService {

	@Autowired
	private ClassScoreLevelReportDao classScoreLevelReportDao;

	@Autowired
	private ScoreManageDao scoreDao;

	@Autowired
	private ScoreManageConfigService configService;

	@Autowired
	private AllCommonDataService allCommonDataService;

	private static final String ERR_MSG_3 = "没有查询到相应的考试信息，请刷新页面！";

	@Override
	public JSONObject getLevelSubjectStatisTabList(JSONObject params) {
		String xnxq = params.getString("xnxq");
		DegreeInfo degreeInfo = scoreDao.getDegreeInfoById(xnxq, params);
		if (degreeInfo == null) {
			throw new CommonRunException(-3, ERR_MSG_3);
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		params.put("autoIncr", autoIncr);

		Long schoolId = params.getLong("xxdm");
		String xxdm = params.getString("xxdm");

		String bhStr = params.getString("bhStr");
		String kmdmStr = params.getString("kmdmStr");
		String nj = params.getString("nj");
		params.remove("bhStr");
		params.remove("kmdmStr");
		params.put("bhList", StringUtil.convertToListFromStr(bhStr, ",", String.class));
		params.put("kmdmList", StringUtil.convertToListFromStr(kmdmStr, ",", String.class));

		Map<Long, LessonInfo> lessonInfoMap = new HashMap<Long, LessonInfo>(); // 科目信息Map

		List<Long> subjectIds = StringUtil.convertToListFromStr(kmdmStr.replace("totalScore", "0"), ",", Long.class);
		List<LessonInfo> lessonInfos = allCommonDataService.getLessonInfoBatch(schoolId, subjectIds, xnxq);
		if (CollectionUtils.isNotEmpty(lessonInfos)) {
			for (LessonInfo lessonInfo : lessonInfos) {// 把科目信息存放到Map
				if (lessonInfo == null)
					continue;
				lessonInfoMap.put(lessonInfo.getId(), lessonInfo);
			}
		}

		Map<Long, Classroom> classroomMap = new HashMap<Long, Classroom>(); // 班级信息存放的Map
		Map<Long, String> deanMap = new HashMap<Long, String>(); // 班主任信息Map
		Map<String, String> teacherMap = new HashMap<String, String>(); // 设置各个科目任课教师名称

		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		if (CollectionUtils.isNotEmpty(classroomList)) {
			List<Long> deanIds = new ArrayList<Long>(); // 班主任代码
			Map<String, Long> teachTmp = new HashMap<String, Long>();
			for (Classroom classroom : classroomList) {
				if (classroom == null) {
					continue;
				}
				long classId = classroom.getId();

				deanIds.add(classroom.getDeanAccountId());
				classroomMap.put(classId, classroom);

				List<AccountLesson> accountLessonList = classroom.getAccountLessons();
				if (accountLessonList == null) {
					continue;
				}
				for (AccountLesson accLesson : accountLessonList) {
					long subjectId = accLesson.getLessonId();
					long teacherId = accLesson.getAccountId();

					teachTmp.put(String.valueOf(subjectId) + classId, teacherId);
				}
			}

			List<Long> teacherIds = new ArrayList<Long>(deanIds);
			teacherIds.addAll(teachTmp.values());

			List<Account> accList = allCommonDataService.getAccountBatch(schoolId, teacherIds, xnxq);

			Map<Long, Account> id2AccMap = new HashMap<Long, Account>();
			if (CollectionUtils.isNotEmpty(accList)) {
				for (Account acc : accList) {
					if (acc == null) {
						continue;
					}
					id2AccMap.put(acc.getId(), acc);
				}
			}

			for (Long id : deanIds) {
				String name = "";
				Account acc = id2AccMap.get(id);
				if (acc != null) {
					name = acc.getName();
				}
				deanMap.put(id, name);
			}

			for (Map.Entry<String, Long> entry : teachTmp.entrySet()) {
				String key = entry.getKey();
				Long accId = entry.getValue();
				String name = "";
				Account acc = id2AccMap.get(accId);
				if (acc != null) {
					name = acc.getName();
				}
				teacherMap.put(key, name);
			}
		}

		// 所有等级
		List<JSONObject> djList = classScoreLevelReportDao.getAllDjInfo();

		// 科目级别
		List<JSONObject> kmJbList = classScoreLevelReportDao.getDDKMJB(xnxq, autoIncr, params);

		for (JSONObject kmjb : kmJbList) {
			kmjb.put("bjmc", "");
			kmjb.put("bzrxm", "");
			kmjb.put("zgh", "");
			kmjb.put("kmmc", "");

			long subjectId = kmjb.getLongValue("kmdm");
			long classId = kmjb.getLongValue("bh");

			Classroom classroom = classroomMap.get(classId);
			if (classroom != null) {
				kmjb.put("bjmc", classroom.getClassName());
				kmjb.put("bzrxm", deanMap.get(classroom.getDeanAccountId()));
				kmjb.put("zgh", classroom.getDeanAccountId());
			}

			LessonInfo lessonInfo = lessonInfoMap.get(subjectId);
			if (lessonInfo != null) {
				kmjb.put("kmmc", lessonInfo.getName());
			}
		}

		List<JSONObject> list = new LinkedList<JSONObject>();
		EasyUIDatagridHead[][] head = null;
		if (kmJbList.size() > 0) {
			// 等级代码-代码名称
			Map<String, String> djMap = new HashMap<String, String>();
			for (JSONObject j : djList) {
				djMap.put(j.getString("dm"), j.getString("mc"));
			}

			// 存放各个班级的班主任
			JSONObject bzrJson = new JSONObject();
			bzrJson.put("subjectName", "");
			bzrJson.put("projectName", "班主任");
			// 存放各个班级的名称
			JSONObject bjJson = new JSONObject();
			List<JSONObject> classInfoList = new ArrayList<JSONObject>();

			HashMap<String, JSONObject> kmJbMap = new HashMap<String, JSONObject>();
			HashMap<String, JSONObject> allKmJbMap = new HashMap<String, JSONObject>();
			// 所有科目
			LinkedHashMap<String, JSONObject> kmMap = new LinkedHashMap<String, JSONObject>();
			String fzdm = "";
			for (int m = 0; m < kmJbList.size(); m++) {
				JSONObject jm = kmJbList.get(m);
				if (m == 0) {
					fzdm = jm.getString("fzdm");
				}
				String bhs = jm.getString("bh");
				String km = jm.getString("kmdm");
				if (!bjJson.containsKey(bhs)) {
					bzrJson.put(bhs + "_projectValue", jm.getString("bzrxm"));
					bjJson.put(bhs, jm.getString("bjmc"));

					JSONObject classInfo = new JSONObject();
					classInfo.put("classId", bhs);
					classInfo.put("className", jm.getString("bjmc"));
					classInfoList.add(classInfo);
				}
				if (!kmMap.containsKey(km)) {
					kmMap.put(km, jm);
				}
				kmJbMap.put(km + bhs, jm);
				allKmJbMap.put(bhs + km + jm.getString("djdm"), jm);
			}

			Object[] keySets = bjJson.keySet().toArray();// bjJson的键数组
			Arrays.sort(keySets);

			Collections.sort(classInfoList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject arg1, JSONObject arg2) {
					String className1 = arg1.getString("className");
					String className2 = arg2.getString("className");
					return className1.compareTo(className2);
				}
			});

			// 设置表头
			head = new EasyUIDatagridHead[1][];
			head[0] = new EasyUIDatagridHead[classInfoList.size() + 2];
			int hi = 0;
			for (JSONObject key : classInfoList) {

				head[0][hi] = new EasyUIDatagridHead(key.get("classId") + "_projectValue",
						StringUtil.transformString(key.get("className")), "center", 0, 1, 1, true);
				hi++;
			}

			head[0][hi] = new EasyUIDatagridHead("gradeValue", "年级", "center", 0, 1, 1, true);
			head[0][hi + 1] = new EasyUIDatagridHead("gradeDiffereValue", "差值", "center", 0, 1, 1, true);

			// 计算年级名称和入学年度
			String xn = xnxq.substring(0, xnxq.length() - 1);
			int gradeValue = Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(nj, xn));// 年级代码
			T_GradeLevel gradeLevel = T_GradeLevel.findByValue(gradeValue);
			String gradeLevelName = AccountStructConstants.T_GradeLevelName.get(gradeLevel);
			if (gradeLevelName == null) {
				gradeLevelName = "";
			}
			// 年级名称
			bzrJson.put("gradeValue", "[" + allCommonDataService.ConvertSYNJ2RXND(nj, xn) + "]" + gradeLevelName);
			bzrJson.put("gradeDiffereValue", "--");
			// 添加班主任第一行
			Map<String, Integer> fieldAuths = this.getLevelSubjectFieldShowInfo(xxdm);

			if (!fieldAuths.containsKey("headTeacher")) {
				list.add(bzrJson);
			}

			params.put("bmfz", fzdm);
			// 平均分、平均分差、年级人数
			List<JSONObject> averageScoreList = classScoreLevelReportDao.getQnjpjf(xnxq, autoIncr, params);
			Map<String, JSONObject> averageDJMap = new HashMap<String, JSONObject>();
			Map<String, JSONObject> averageMap = new HashMap<String, JSONObject>();
			for (JSONObject jm : averageScoreList) {
				String key = jm.getString("kmdm") + jm.getString("djdm");
				if (!averageDJMap.containsKey(key)) {
					averageDJMap.put(key, jm);
				}
				if (!averageMap.containsKey(key)) {
					averageMap.put(jm.getString("kmdm"), jm);
				}

			}

			List<JSONObject> kmList = new ArrayList<JSONObject>(kmMap.values());
			Collections.sort(kmList, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					long kmdm1 = o1.getLongValue("kmdm");
					long kmdm2 = o2.getLongValue("kmdm");
					return Long.compare(kmdm1, kmdm2);
				}
			});
			
			// 循环科目
			for (JSONObject j : kmList) {
				// 任课教师
				JSONObject rkjs = new JSONObject();
				// 平均分
				JSONObject pjf = new JSONObject();
				// 平均分排名
				JSONObject pjfpm = new JSONObject();
				// 优秀率
				JSONObject yxl = new JSONObject();
				// 优秀率排名
				JSONObject yxlpm = new JSONObject();
				// 合格率
				JSONObject hgl = new JSONObject();
				// 合格率排名
				JSONObject hglpm = new JSONObject();

				String kmmc = j.getString("kmmc");
				String kmcode = j.getString("kmdm");
				rkjs.put("subjectName", kmmc);
				pjf.put("subjectName", kmmc);
				pjfpm.put("subjectName", kmmc);
				yxl.put("subjectName", kmmc);
				yxlpm.put("subjectName", kmmc);
				hgl.put("subjectName", kmmc);
				hglpm.put("subjectName", kmmc);

				rkjs.put("projectName", "任课教师");
				pjf.put("projectName", "平均分");
				pjfpm.put("projectName", "排名");
				yxl.put("projectName", "优秀率");
				yxlpm.put("projectName", "排名");
				hgl.put("projectName", "合格率");
				hglpm.put("projectName", "排名");

				for (Iterator<String> bjit = bjJson.keySet().iterator(); bjit.hasNext();) {
					String bhs = bjit.next();
					rkjs.put(bhs + "_projectValue", teacherMap.get(kmcode + bhs));
					JSONObject jc = kmJbMap.get(kmcode + bhs);
					if (null == jc) {
						jc = new JSONObject();
					}
					pjf.put(bhs + "_projectValue", jc.getFloatValue("pjf"));
					pjfpm.put(bhs + "_projectValue", jc.getIntValue("pm"));
					yxl.put(bhs + "_projectValue", jc.getFloatValue("yxl"));
					yxlpm.put(bhs + "_projectValue", jc.getIntValue("yxlpm"));
					hgl.put(bhs + "_projectValue", jc.getFloatValue("hgl"));
					hglpm.put(bhs + "_projectValue", jc.getIntValue("hglpm"));
				}

				rkjs.put("gradeValue", "--");
				rkjs.put("gradeDiffereValue", "--");
				pjfpm.put("gradeValue", "--");
				pjfpm.put("gradeDiffereValue", "--");
				yxlpm.put("gradeValue", "--");
				yxlpm.put("gradeDiffereValue", "--");
				hglpm.put("gradeValue", "--");
				hglpm.put("gradeDiffereValue", "--");

				JSONObject avj = averageMap.get(kmcode);
				if (avj != null) {
					pjf.put("gradeValue", avj.get("pjf"));
					pjf.put("gradeDiffereValue", avj.get("pjffc"));
					yxl.put("gradeValue", avj.get("yxl"));
					yxl.put("gradeDiffereValue", avj.get("yxlc"));
					hgl.put("gradeValue", avj.get("hgl"));
					hgl.put("gradeDiffereValue", avj.get("hglc"));
				}

				if (!fieldAuths.containsKey("teacher")) {
					list.add(rkjs);
				}
				if (!fieldAuths.containsKey("averageScore")) {
					list.add(pjf);
				}
				if (!fieldAuths.containsKey("averageScoreRank")) {
					list.add(pjfpm);
				}
				if (!fieldAuths.containsKey("excelletRate")) {
					list.add(yxl);
				}
				if (!fieldAuths.containsKey("excelletRateRank")) {
					list.add(yxlpm);
				}
				if (!fieldAuths.containsKey("passRate")) {
					list.add(hgl);
				}
				if (!fieldAuths.containsKey("passRateRank")) {
					list.add(hglpm);
				}

				// 该科目的各个等级
				for (JSONObject jm : djList) {
					String levelName = jm.getString("mc");
					if (!fieldAuths.containsKey(levelName)) {
						JSONObject jd = new JSONObject();
						jd.put("subjectName", kmmc);
						jd.put("projectName", jm.getString("mc") + "等人数");
						for (Iterator<String> bjit = bjJson.keySet().iterator(); bjit.hasNext();) {
							String bhs = bjit.next();
							JSONObject jb = allKmJbMap.get(bhs + kmcode + jm.getString("dm"));
							if (jb != null) {
								jd.put(bhs + "_projectValue", jb.get("rs"));
							}
						}
						JSONObject av = averageDJMap.get(kmcode + jm.getString("dm"));
						if (av != null) {
							jd.put("gradeValue", av.get("njrs"));
							jd.put("gradeDiffereValue", av.get("rsc"));
						}
						list.add(jd);
					}
				}
			}

		}

		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		data.put("columns", head);
		return data;
	}

	/*****
	 * 获取等第科目统计表表头字段显示情况
	 * 
	 * @param schoolId
	 * @return
	 */
	private Map<String, Integer> getLevelSubjectFieldShowInfo(String schoolId) {
		// 1.定义参数，获取配置详情
		Map<String, Integer> show = new HashMap<String, Integer>();// 存放字段显示情况和列数

		String config = this.configService.getReportConfigs("007", schoolId).get("config").toString();// 报表字段是否需要显示配置
		JSONObject fieldAuth = config == null ? null : JSONObject.parseObject(config);
		// 2.根据配置规则。
		if (fieldAuth != null) {
			// 班主任
			String headTeacher = fieldAuth.get("bzr").toString();
			if ("0".equals(headTeacher)) {
				show.put("headTeacher", 0);
			}
			// 任课老师
			String teacher = fieldAuth.get("rjks").toString();
			if ("0".equals(teacher)) {
				show.put("teacher", 0);
			}
			// 平均分
			String averageScore = fieldAuth.get("pjfz").toString();
			if ("0".equals(averageScore)) {
				show.put("averageScore", 0);
			}
			// 平均分排名
			String averageScoreRank = fieldAuth.getString("pm");
			if ("0".equals(averageScoreRank)) {
				show.put("averageScoreRank", 0);
			}
			// 优秀率
			String excelletRate = fieldAuth.getString("yxl");
			if ("0".equals(excelletRate)) {
				show.put("excelletRate", 0);
			}
			// 优秀率排名
			String excelletRateRank = fieldAuth.getString("yxlpm");
			if ("0".equals(excelletRateRank)) {
				show.put("excelletRateRank", 0);
			}
			// 合格率
			String passRate = fieldAuth.getString("hgl");
			if ("0".equals(passRate)) {
				show.put("passRate", 0);
			}
			// 合格率排名
			String passRateRank = fieldAuth.getString("hglpm");
			if ("0".equals(passRateRank)) {
				show.put("passRateRank", 0);
			}
			// a等人数
			String aLevelNum = fieldAuth.getString("Adrs");
			if ("0".equals(aLevelNum)) {
				show.put("A", 0);
			}
			// b等人数
			String bLevelNum = fieldAuth.getString("Bdrs");
			if ("0".equals(bLevelNum)) {
				show.put("B", 0);
			}
			// C等人数
			String cLevelNum = fieldAuth.getString("Cdrs");
			if ("0".equals(cLevelNum)) {
				show.put("C", 0);
			}
			// D等人数
			String dLevelNum = fieldAuth.getString("Ddrs");
			if ("0".equals(dLevelNum)) {
				show.put("D", 0);
			}
			// E等人数
			String eLevelNum = fieldAuth.getString("Edrs");
			if ("0".equals(eLevelNum)) {
				show.put("E", 0);
			}
		}
		return show;
	}

	@Override
	public JSONObject getLeveStudentNumStatisTabList(JSONObject params) {
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
		int subjectType = params.getIntValue("subjectType");
		params.remove("subjectType");
		if (subjectType == 0) {
			params.put("lb", "01");
		} else {
			params.put("lb", "02");
		}

		List<Long> classIds = StringUtil.convertToListFromStr(bhStr, ",", Long.class);
		List<Classroom> classroomList = allCommonDataService.getClassroomBatch(schoolId, classIds, xnxq);
		if(CollectionUtils.isEmpty(classroomList)) {
			throw new CommonRunException(-1, "无法从基础数据获取班级信息，请联系管理员！");
		}

		Map<Long, String> deanId2DeanName = new HashMap<Long, String>();
		Map<Long, Classroom> classId2Obj = new HashMap<Long, Classroom>();
		
		List<Long> deanIds = new ArrayList<Long>();
		for (Classroom classroom : classroomList) {
			if (classroom == null) {
				continue;
			}
			Long classId = classroom.getId();
			deanIds.add(classroom.getDeanAccountId());
			classId2Obj.put(classId, classroom);
		}
		
		List<Account> accList = allCommonDataService.getAccountBatch(schoolId, deanIds, xnxq);
		if(CollectionUtils.isNotEmpty(accList)) {
			for(Account acc : accList) {
				deanId2DeanName.put(acc.getId(), acc.getName());
			}
		}

		// 全部班级id
		List<Long> classIdList = scoreDao.getClassIdListByGroupId(xnxq, autoIncr, params);
		params.put("bhList", classIdList);
		List<JSONObject> numList = classScoreLevelReportDao.getNumInfo(xnxq, autoIncr, params);

		// 获取接口数据，设置到数据列中
		for (JSONObject num : numList) {
			Long classId = num.getLong("bh");
			Classroom classroom = classId2Obj.get(classId);
			if(classroom == null) {
				continue;
			}
			if(classroom.getClassName() != null) {
				num.put("className", classroom.getClassName());
			} else {
				num.put("className", "");
			}
			
			if(deanId2DeanName.containsKey(classroom.getDeanAccountId())) {
				num.put("classAdviser", deanId2DeanName.get(classroom.getDeanAccountId()));
			}
		}

		List<JSONObject> list = new LinkedList<JSONObject>();
		EasyUIDatagridHead[][] head = null;
		if (numList.size() > 0) {
			List<JSONObject> dJxlList = classScoreLevelReportDao.getAllDjxlInfo(xnxq, autoIncr, params);
			int tempCount = dJxlList.size();
			int djxlcount = 0;
			// 确定等级序列数量
			if (tempCount < 6) {
				djxlcount = 2 * tempCount;
			} else {
				djxlcount = 12; // 等级 百分比
			}

			List<JSONObject> pmQjList = classScoreLevelReportDao.getAllPmqj(xnxq, autoIncr, params);
			int tempCount2 = pmQjList.size();
			int pmqjcount = 0;
			int fbxx = 0;
			int fbsx = 0;
			String fbdm = "";
			// 确定排名区间数量
			if (tempCount2 > 0) {
				if (tempCount2 < 4) {
					if (tempCount2 == 1) {
						pmqjcount = tempCount2 + 1;// 前x段总和 后x名
					} else {
						pmqjcount = tempCount2 + 2;// 前n列加上 前x段总和 后x名
					}

				} else if (tempCount2 < 5) {
					pmqjcount = tempCount2 + 3;// 前n列 加上后x名 前3段总和 前x段总和 后x名
				} else {
					tempCount2 = 5;
					pmqjcount = tempCount2 + 3;// 前n列 加上后x名 前3段总和 前5段总和
				}
			}

			head = new EasyUIDatagridHead[1][];
			head[0] = new EasyUIDatagridHead[djxlcount + pmqjcount];

			// 存储所有列对应的key
			List<String> headKey = new ArrayList<String>();
			List<String> headKey2 = new ArrayList<String>();

			// 添加动态等级序列到dt head中
			if (tempCount < 6) {
				for (int i = 0; i < tempCount; i++) {
					JSONObject drxl = dJxlList.get(i);

					String djxla = drxl.getString("djxl");
					// 向表中添加等级序列的列
					head[0][i * 2] = new EasyUIDatagridHead(djxla, djxla, "center", 60, 1, 1, true);
					head[0][i * 2 + 1] = new EasyUIDatagridHead(djxla + "StudentNumRatio", "百分比", "center", 60, 1, 1,
							true);
					headKey.add(djxla);
				}
			} else {
				for (int i = 0; i < 5; i++) {
					JSONObject drxl = dJxlList.get(i);
					String djxla = drxl.getString("djxl");
					head[0][i * 2] = new EasyUIDatagridHead(djxla, djxla, "center", 60, 1, 1, true);
					head[0][i * 2 + 1] = new EasyUIDatagridHead(djxla + "StudentNumRatio", "百分比", "center", 60, 1, 1,
							true);
					headKey.add(djxla);
				}
				JSONObject drxllast = dJxlList.get(tempCount - 1);
				String djxllast = drxllast.getString("djxl");
				head[0][10] = new EasyUIDatagridHead(djxllast, djxllast, "center", 60, 1, 1, true);
				head[0][11] = new EasyUIDatagridHead(djxllast + "StudentNumRatio", "百分比", "center", 60, 1, 1, true);
				headKey.add(djxllast);
			}

			String fmstring = "";
			// 添加动态排名区间到dt head中
			if (tempCount2 > 0) {
				if (tempCount2 < 4) {
					for (int r = 0; r < tempCount2; r++) {
						JSONObject drpm = pmQjList.get(r);
						fbxx = drpm.getIntValue("pmfbxx");	// 排名分布下限
						fbsx = drpm.getIntValue("pmfbsx");	// 排名分布上线
						fbdm = drpm.getString("pmfbdm");	// 排名分布代码
						headKey2.add(fbdm);
						if (tempCount2 == 1) {
							fmstring = "前" + fbsx + "名";
							head[0][djxlcount + r] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center", 100, 1,
									1, true);
						} else {
							if (r == 0) {
								head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, "前" + fbsx, "center", 80, 1, 1,
										true);
							} else if (r == tempCount2 - 1) {
								fmstring = (fbxx + 1) + "-" + fbsx;
								head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1,
										true);
								fmstring = "前" + fbsx + "名";
								head[0][djxlcount + r + 1] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center",
										100, 1, 1, true);
							} else {
								fmstring = (fbxx + 1) + "-" + fbsx;
								head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1,
										true);
							}

						}
					}
					headKey2.add("last" + fbdm);
					String first3 = pmQjList.get(tempCount2 - 1).getString("pmfbdm");
					headKey2.add("before" + first3);
					fmstring = "" + fbsx + "名后";
					if (tempCount2 == 1) {
						head[0][djxlcount + tempCount2] = new EasyUIDatagridHead("last" + fbdm, fmstring, "center", 120,
								1, 1, true);
					} else {
						head[0][djxlcount + tempCount2 + 1] = new EasyUIDatagridHead("last" + fbdm, fmstring, "center",
								120, 1, 1, true);
					}
				} else if (tempCount2 < 5) {
					int j = 0;
					for (int r = 0; r < tempCount2; r++) {
						JSONObject drpm = pmQjList.get(r);
						fbxx = drpm.getIntValue("pmfbxx");
						fbsx = drpm.getIntValue("pmfbsx");
						fbdm = drpm.getString("pmfbdm");
						headKey2.add(fbdm);
						if (r == 0) {
							head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, "前" + fbsx, "center", 80, 1, 1, true);
						} else if (r == 2) {
							fmstring = (fbxx + 1) + "-" + fbsx;

							head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1, true);
							fmstring = "前" + fbsx + "名";
							head[0][djxlcount + r + 1] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center",
									100, 1, 1, true);
							j++;

						} else if (r == tempCount2 - 1) {
							fmstring = (fbxx + 1) + "-" + fbsx;
							head[0][djxlcount + r + j] = new EasyUIDatagridHead(fbdm, fmstring, "center", 80, 1, 1,
									true);
							fmstring = "前" + fbsx + "名";
							head[0][djxlcount + r + j + 1] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center",
									100, 1, 1, true);
						} else {
							fmstring = (fbxx + 1) + "-" + fbsx;
							head[0][djxlcount + r + j] = new EasyUIDatagridHead(fbdm, fmstring, "center", 80, 1, 1,
									true);
						}
					}
					headKey2.add("last" + fbdm);
					String first3 = pmQjList.get(2).getString("pmfbdm");
					headKey2.add("before" + first3);
					String first5 = pmQjList.get(tempCount2 - 1).getString("pmfbdm");
					headKey2.add("before" + first5);
					fmstring = "" + fbsx + "名后";
					head[0][djxlcount + tempCount2 + 2] = new EasyUIDatagridHead("last" + fbdm, fmstring, "center", 120,
							1, 1, true);

				} else {
					int j = 0;
					for (int r = 0; r < 5; r++) {
						JSONObject drpm = pmQjList.get(r);
						fbxx = drpm.getIntValue("pmfbxx");
						fbsx = drpm.getIntValue("pmfbsx");
						fbdm = drpm.getString("pmfbdm");
						headKey2.add(fbdm);
						if (r == 0) {
							head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, "前" + fbsx, "center", 80, 1, 1, true);
						} else if (r == 2) {
							fmstring = (fbxx + 1) + "-" + fbsx;
							head[0][djxlcount + r] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1, true);
							fmstring = "前" + fbsx + "名"; // 前3段的和 第三段
							head[0][djxlcount + r + 1] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center", 80,
									1, 1, true);
							j++;

						} else if (r == 4) {
							fmstring = (fbxx + 1) + "-" + fbsx; // 第五段 前5段的和
							head[0][djxlcount + r + j] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1,
									true);
							j++;
							fmstring = "前" + fbsx + "名";
							head[0][djxlcount + r + j] = new EasyUIDatagridHead("before" + fbdm, fmstring, "center", 80,
									1, 1, true);
						} else {
							fmstring = (fbxx + 1) + "-" + fbsx;
							head[0][djxlcount + r + j] = new EasyUIDatagridHead(fbdm, fmstring, "center", 100, 1, 1,
									true);
						}
					}
					String first3 = pmQjList.get(2).getString("pmfbdm");
					headKey2.add("last" + fbdm);
					headKey2.add("before" + first3);
					String first5 = pmQjList.get(4).getString("pmfbdm");
					headKey2.add("before" + first5);
					fmstring = "" + fbsx + "名后";
					head[0][djxlcount + tempCount2 + 2] = new EasyUIDatagridHead("last" + fbdm, fmstring, "center", 120,
							1, 1, true);
				}
			}

			List<JSONObject> djXlInfoList = classScoreLevelReportDao.getAllDjxlInformation(xnxq, autoIncr, params);
			List<JSONObject> pmInfoList = classScoreLevelReportDao.getAllPmInfo(xnxq, autoIncr, params);
			List<JSONObject> njPmFbList = classScoreLevelReportDao.getNjPmfb(xnxq, autoIncr, params);
			List<JSONObject> njDjXlList = classScoreLevelReportDao.getNjDjxl(xnxq, autoIncr, params);

			HashMap<String, JSONObject> djXlMap = new HashMap<String, JSONObject>();
			HashMap<String, JSONObject> njDjXlMap = new HashMap<String, JSONObject>();
			HashMap<String, JSONObject> pmInfoMap = new HashMap<String, JSONObject>();
			HashMap<String, JSONObject> pmFbMap = new HashMap<String, JSONObject>();
			DecimalFormat df = new DecimalFormat("0.00");
			for (JSONObject jm : djXlInfoList) {
				djXlMap.put(jm.getString("bh") + jm.getString("djxl"), jm);
			}

			for (JSONObject jm : njDjXlList) {
				njDjXlMap.put(jm.getString("djxl"), jm);
			}

			for (JSONObject jm : pmInfoList) {
				pmInfoMap.put(jm.getString("bh") + jm.getString("pmfbdm"), jm);
			}

			for (JSONObject jm : njPmFbList) {
				pmFbMap.put(jm.getString("pmfbdm"), jm);
			}
			
			JSONObject drNj = numList.remove(numList.size() - 2);
			JSONObject drBc = numList.remove(numList.size() - 1);
			int totalCkrs = drNj.getIntValue("referenceNum");// 总参考人数
			
			Collections.sort(numList, new Comparator<JSONObject>(){
				@Override
				public int compare(JSONObject arg0, JSONObject arg1) {
					String className0 = arg0.getString("className");
					String className1 = arg1.getString("className");
					
					if("班差".equals(className0)) {
						return -1;
					}
					if("班差".equals(className1)) {
						return 1;
					}
					if("总分".equals(className0)) {
						return -1;
					}
					if("总分".equals(className1)) {
						return 1;
					}
					
					return className0.compareTo(className1);
				}
			});
																// 方便后面得到年级各个序列的百分比
			JSONObject upAll = new JSONObject();
			JSONObject downAll = new JSONObject();

			for (int b = 0; b < numList.size(); b++) {
				JSONObject row = numList.get(b);
				String strBh = row.getString("bh");

				for (int i = 0; i < headKey.size(); i++) {
					String key = headKey.get(i);
					JSONObject drv = djXlMap.get(strBh + key);
					if (null != drv) {
						// 计算最高 最低
						if (upAll.containsKey(key)) {
							Double up = upAll.getDoubleValue(key);
							if (drv.getDoubleValue("bl") > up) {
								upAll.put(key, drv.getDoubleValue("bl"));
							}
						} else {
							upAll.put(key, drv.getDoubleValue("bl"));
						}

						if (downAll.containsKey(key)) {
							Double down = downAll.getDoubleValue(key);
							if (drv.getDoubleValue("bl") < down) {
								downAll.put(key, drv.getDoubleValue("bl"));
							}
						} else {
							downAll.put(key, drv.getDoubleValue("bl"));
						}
					}
				}
			}
			for (int b = 0; b < numList.size(); b++) {
				JSONObject row = numList.get(b);
				String strBh = row.getString("bh");
				int intCkrs = row.getIntValue("referenceNum");

				// 前面的排名列
				for (int i = 0; i < headKey.size(); i++) {
					String key = headKey.get(i);
					JSONObject drv = djXlMap.get(strBh + key);
					if (null != drv) {
						row.put(key, drv.getIntValue("rs"));
						row.put(key + "StudentNumRatio", df.format(drv.getDoubleValue("bl")) + "%");
					}
					// 总计和班差只弄一次
					if (b == 0) {
						JSONObject drv2 = njDjXlMap.get(key);
						if (null != drv2) {
							drNj.put(key, drv2.getIntValue("djxlrs"));
							drBc.put(key, drv2.getIntValue("djxlcz"));
							drNj.put(key + "StudentNumRatio",
									df.format(drv2.getDoubleValue("djxlrs") / totalCkrs * 100) + "%");
							drBc.put(key + "StudentNumRatio",
									df.format(drv2.getDoubleValue("djxlcz") / totalCkrs * 100) + "%");
						}
					}
				}

				// 后面的名次列
				for (int i = 0; i < headKey2.size(); i++) {
					String key = headKey2.get(i);
					String beforeFbdm = "";
					if (key.indexOf("before") > -1) {
						beforeFbdm = key.substring(6);
						JSONObject drv = pmInfoMap.get(strBh + beforeFbdm);
						if (null != drv) {
							row.put(key, drv.getIntValue("ljrs"));
						}
						if (b == 0) {
							JSONObject drv2 = pmFbMap.get(beforeFbdm);
							if (null != drv2) {
								drNj.put(key, drv2.getIntValue("pmljrs"));	// 排名年级人数
								drBc.put(key, drv2.getIntValue("ljrscz"));
							}

						}
					} else if (key.indexOf("last") > -1) {
						String lastFbdm = pmQjList.get(tempCount2 - 1).getString("pmfbdm");

						JSONObject drv = pmInfoMap.get(strBh + lastFbdm);
						if (null != drv) {
							row.put(key, intCkrs - drv.getIntValue("ljrs"));
						}
						if (b == 0) {
							JSONObject drv2 = pmFbMap.get(lastFbdm);
							if (null != drv2) {
								drNj.put(key, totalCkrs - drv2.getIntValue("pmljrs"));	// 排名年级人数
								drBc.put(key, drv2.getIntValue("lastcz"));	// 差值
							}
						}

					} else {
						JSONObject drv = pmInfoMap.get(strBh + key);

						if (null != drv) {
							row.put(key, drv.getIntValue("rs"));
						}
						if (b == 0) {
							JSONObject drv2 = pmFbMap.get(key);
							if (null != drv2) {
								drNj.put(key, drv2.getIntValue("pmrs"));
								drBc.put(key, drv2.getIntValue("pmcz"));
							}
						}
					}
				}

				list.add(row);
			}
			list.add(drNj);
			list.add(drBc);
		}
		
		JSONObject data = new JSONObject();
		data.put("total", list.size());
		data.put("rows", list);
		data.put("columns", head);
		return data;
	}
}
