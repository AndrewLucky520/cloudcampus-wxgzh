package com.talkweb.placementtask.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.dao.DezyPlacementTaskDao;
import com.talkweb.placementtask.dao.PlacementImportDao;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.ClassFixedArrange;
import com.talkweb.placementtask.domain.ClassInfo;
import com.talkweb.placementtask.domain.ExecProgressRedisMsg;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementRule;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentClassInfo;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.StudentbaseInfo;
import com.talkweb.placementtask.domain.SubjectcompToClassInfo;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySettings;
import com.talkweb.placementtask.domain.TPlDezySubjectSet;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.domain.TPlDzbClassLevel;
import com.talkweb.placementtask.domain.TPlStudentinfo;
import com.talkweb.placementtask.domain.TeachingClassInfo;
import com.talkweb.placementtask.process.PlacementProcess;
import com.talkweb.placementtask.service.DezyPlacementTaskService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.placementtask.utils.TmpUtil;
import com.talkweb.placementtask.utils.newdzb.Conflict;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter;
import com.talkweb.placementtask.utils.newdzb.NewDZBPlacementExcuter.SelectType;
import com.talkweb.placementtask.utils.newdzb.Student;
import com.talkweb.placementtask.utils.newdzb.Tclass;
import com.talkweb.placementtask.utils.newdzb.TclassEqualSubjectCombination;
import com.talkweb.schedule.service.ScheduleExternalService;
import com.talkweb.scoreManage.service.impl.ScoreManageAPIServiceImpl;
import com.talkweb.wishFilling.service.WishFillingThirdService;

@Service
public class PlacementTaskServiceImpl implements PlacementTaskService {
	Logger logger = LoggerFactory.getLogger(PlacementTaskServiceImpl.class);

	private final T_GradeLevel[] gradeLevels = new T_GradeLevel[] {
			T_GradeLevel.T_HighOne, T_GradeLevel.T_HighTwo,
			T_GradeLevel.T_HighThree };

	private final Map<T_GradeLevel, String> njName = Collections
			.unmodifiableMap(AccountStructConstants.T_GradeLevelName);

	// 线程池，固定5个线程，最大线程数为1000
	private final ExecutorService threadPool = new ThreadPoolExecutor(5, 1000,
			1L, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>());

	private Set<Long> commSubjectIdSet = new HashSet<Long>(9);
	{
		commSubjectIdSet.add(1L); // 语文
		commSubjectIdSet.add(2L); // 数学
		commSubjectIdSet.add(3L); // 英语
		commSubjectIdSet.add(4L); // 政治
		commSubjectIdSet.add(5L); // 历史
		commSubjectIdSet.add(6L); // 地理
		commSubjectIdSet.add(7L); // 物理
		commSubjectIdSet.add(8L); // 化学
		commSubjectIdSet.add(9L); // 生物
	}

	@Autowired
	private PlacementTaskDao placementTaskDao;
	
	@Autowired
	private PlacementImportDao placementImportDao;

	@Autowired
	private DataSourceTransactionManager transactionManager;

	@Autowired
	private ScoreManageAPIServiceImpl scoreManageAPIService;

	@Autowired
	private WishFillingThirdService wishFillingThirdService;

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private DezyPlacementTaskDao dezyPlacementTaskDao;
	
	@Autowired
	private DezyPlacementTaskService dezyPlacementTaskService;

	@Autowired
	private ScheduleExternalService scheduleExternalService;
	
	
	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;
	
	@Autowired
	DataSourceTransactionManager txManager;

	public String TransformationToName(String zhId){
		String zhName = "";
		if("".equals(zhId)){
			return zhName;
		}
		try{
			switch(zhId){
			case "1":
				zhName = zhName+"语文";
				break;
			case "2":
				zhName = zhName+"数学";
				break;
			case "3":
				zhName = zhName+"英语";
				break;
			case "4":
				zhName = zhName+"政治";
				break;
			case "5":
				zhName = zhName+"历史";
				break;
			case "6":
				zhName = zhName+"地理";
				break;
			case "7":
				zhName = zhName+"物理";
				break;
			case "8":
				zhName = zhName+"化学";
				break;
			case "9":
				zhName = zhName+"生物";
				break;
			case "19":
				zhName = zhName+"技术";
				break;
			case "-999":
				zhName = zhName+"行政班";
				break;
			default:
				throw new Exception("科目Id"+zhId+"不正确");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return zhName;
	}
	
	
	
	@Override
	public List<JSONObject> queryGradeList(JSONObject request) {
		List<JSONObject> grades = new ArrayList<JSONObject>();
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);

		for (T_GradeLevel gl : gradeLevels) {
			JSONObject grade = new JSONObject();
			String usedGrade = commonDataService.ConvertNJDM2SYNJ(
					String.valueOf(gl.getValue()), xn);
			String gradeName = njName.get(gl);
			grade.put("usedGrade", usedGrade);
			grade.put("gradeName", gradeName + "年级");
			grades.add(grade);
		}

		return grades;
	}

	@Override
	public JSONObject queryWishFillingList(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		JSONObject data = new JSONObject();

		String wfId = pl.getWfId();
		String wfTermInfo = pl.getWfTermInfo();
		data.put("wfId", wfId);
		data.put("wfTermInfo", wfTermInfo);

		String usedGrade = pl.getUsedGrade();

		Integer placementType = pl.getPlacementType();
		Long schoolId = request.getLong("schoolId");

		String type = null; // 0:单科，1：组合，2：所有
		if (placementType == 1) { // 微走班
			type = "1";
		} else if (placementType == 2) { // 中走班
			type = "2";
		} else if (placementType == 3) { // 大走班
			type = "2";
		} else {
			throw new CommonRunException(-1, "分班主表中分班类型错误(placementType:"
					+ placementType + ")，请联系管理员！");
		}
		List<JSONObject> list = wishFillingThirdService.getWfListToThird(
				usedGrade, type, schoolId);
		if (list == null) {
			list = new ArrayList<JSONObject>();
		}
		data.put("list", list);
		return data;
	}

	@Override
	public List<JSONObject> queryScoreList(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		String usedGrade = pl.getUsedGrade();
		
		Boolean hasNull = request.getBoolean("hasNull");
		if (hasNull == null) {
			hasNull = true;
		}
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		String classStrs = request.getString("classIds");
		String subjectStrs = request.getString("subjectIds");

		List<String> classIds = null;
		if (StringUtils.isBlank(classStrs)) {
			if (usedGrade == null) {
				throw new CommonRunException(-1, "分班主表年级为空，请联系管理员！");
			}
			T_GradeLevel gl = T_GradeLevel
					.findByValue(Integer.parseInt(commonDataService
							.ConvertSYNJ2NJDM(usedGrade, xn)));
			Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl,
					termInfo);
			if (grade == null || grade.getClassIds() == null) {
				throw new CommonRunException(-1, "无法从SDK获取年级或年级下的班级数据，请联系管理员！");
			}
			classIds = new ArrayList<String>();
			for(Long classId : grade.getClassIds()) {
				classIds.add(String.valueOf(classId));
			}
		} else {
			classIds = StringUtil.convertToListFromStr(classStrs, ",",
					String.class);
		}
		
		List<String> subjectIds = null;
		if (StringUtils.isNotBlank(subjectStrs)) {
			subjectIds = StringUtil.convertToListFromStr(subjectStrs, ",", String.class);
		}
		
		List<JSONObject> list = scoreManageAPIService.getScoreIdAndNameList(termInfo, String.valueOf(schoolId), usedGrade, classIds, subjectIds);
		if (hasNull && list.size() > 0) {
			JSONObject nullScore = new JSONObject();
			nullScore.put("examId", "");
			nullScore.put("examName", "");
			nullScore.put("examTermInfo", "");
			list.add(0, nullScore);
		}
		return list;
	}

	@Override
	public List<JSONObject> queryPlacementSubjectList(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());

		List<JSONObject> data = new ArrayList<JSONObject>();

		List<OpenClassTask> opClassTaskList = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		StringBuffer strbuf = new StringBuffer();

		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		for (OpenClassTask opClassTask : opClassTaskList) {
			String zhName = opClassTask.getOpenClassInfo().getZhName();
			String opClassTaskId = opClassTask.getOpenClassTaskId();

			if (!map.containsKey(zhName)) {
				map.put(zhName, new JSONObject());
			}
			JSONObject json = map.get(zhName);
			if (!json.containsKey("openClassTaskId")) {
				json.put("openClassTaskId", opClassTaskId);
				json.put("zhName", zhName);
				json.put("type", opClassTask.getOpenClassInfo().getType());
			} else {
				json.put("openClassTaskId", json.get("openClassTaskId") + ","
						+ opClassTaskId);
			}
			strbuf.append(opClassTaskId).append(",");
		}

		data.addAll(map.values());
		Collections.sort(data, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				Integer type1 = o1.getInteger("type");
				Integer type2 = o2.getInteger("type");
				int result = Integer.compare(type1, type2);
				if (result != 0) {
					return result;
				}
				String name1 = o1.getString("zhName");
				String name2 = o2.getString("zhName");
				return name1.compareTo(name2);
			}
		});

		Boolean hasAllItem = request.getBoolean("hasAllItem");
		if (hasAllItem == null) {
			hasAllItem = true;
		}
		if (hasAllItem) {
			if (map.size() > 1) {
				strbuf.deleteCharAt(strbuf.length() - 1);
				JSONObject all = new JSONObject();
				all.put("openClassTaskId", strbuf.toString());
				all.put("zhName", "全部");
				data.add(0, all);
			}
		}

		return data;
	}

	@Override
	public List<JSONObject> queryTeachingClassesList(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		List<JSONObject> data = new ArrayList<JSONObject>();
		request.put("placementType", pl.getPlacementType());
		List<TeachingClassInfo> tClassInfoList = placementTaskDao
				.queryTeachingClassInfoWithAll(request);
		StringBuffer strbuf = new StringBuffer();
		for (TeachingClassInfo tClassInfo : tClassInfoList) {
			JSONObject json = new JSONObject();
			String teachingClassId = tClassInfo.getTeachingClassId();
			strbuf.append(teachingClassId).append(",");
			json.put("teachingClassId", teachingClassId);
			json.put("teachingClassName", tClassInfo.getTeachingClassName());
			json.put("type", tClassInfo.getOpenClassTask().getOpenClassInfo()
					.getType());
			data.add(json);
		}

		Collections.sort(data, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				Integer type1 = o1.getInteger("type");
				Integer type2 = o2.getInteger("type");
				int result = Integer.compare(type1, type2);
				if (result != 0) {
					return result;
				}
				String name1 = o1.getString("teachingClassName");
				String name2 = o2.getString("teachingClassName");
				return name1.compareTo(name2);
			}
		});

		Boolean hasAllItem = request.getBoolean("hasAllItem");
		if (hasAllItem == null) {
			hasAllItem = true;
		}
		if (hasAllItem) {
			if (data.size() > 1) {
				strbuf.deleteCharAt(strbuf.length() - 1);
				JSONObject all = new JSONObject();
				all.put("teachingClassId", strbuf.toString());
				all.put("teachingClassName", "全部");
				data.add(0, all);
			}
		}

		return data;
	}

	@Override
	public List<JSONObject> querySubjects(JSONObject request) {
		String termInfo = request.getString("termInfo");
		if (termInfo == null) {
			throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
		}
		Long schoolId = request.getLong("schoolId");
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(
				schoolId, new ArrayList<Long>(commSubjectIdSet), termInfo);
		if (CollectionUtils.isEmpty(lessonInfos)) {
			throw new CommonRunException(-1, "从sdk中未获取到科目数据，请联系管理员！");
		}
		List<JSONObject> result = new ArrayList<JSONObject>(lessonInfos.size());
		for (LessonInfo lessonInfo : lessonInfos) {
			JSONObject obj = new JSONObject();
			obj.put("subjectName", lessonInfo.getName());
			obj.put("subjectId", lessonInfo.getId());
			result.add(obj);
		}
		return result;
	}

	@Override
	public List<PlacementTask> queryPlacementTaskList(JSONObject json) {
		Map<String, Object> map = new HashMap<String,Object>();
		String termInfo = json.get("termInfo").toString();
		String schoolId = json.get("schoolId").toString();
		map.put("termInfo", termInfo);
		map.put("schoolId", schoolId);
		return placementTaskDao.queryPlacementTaskList(map);
	}

	@Override
	public JSONObject queryPlacementTaskById(JSONObject request) {
		String plId = request.getString("placementId");
		JSONObject data = null;
		if (plId != null) {
			PlacementTask placementTask = placementTaskDao
					.queryPlacementTaskById(request);
			if (placementTask == null) {
				data = new JSONObject();
				data.put("placementId", plId);
			} else {
				placementTask.setPlacementId(plId);
				data = (JSONObject) JSON.toJSON(placementTask);
			}
		} else {
			data = new JSONObject();
		}
		return data;
	}

	@Override
	public void insertOrUpdatePlacementTask(JSONObject request) {
		if (placementTaskDao.ifExistsSamePlacementName(request)) {
			throw new CommonRunException(-1, "已有同名分班任务，请修改分班任务名称！！！");
		}

		PlacementTask placementTask = JSON.toJavaObject(request,
				PlacementTask.class);
		String plId = placementTask.getPlacementId();
		if (StringUtils.isBlank(plId)) {
			placementTask.setPlacementId(UUIDUtil.getUUID());
			placementTask.setCreateDate(new Date());
		}
		placementTaskDao.insertOrUpdatePlacementTask(placementTask);
	}

	@Override
	public void deletePlacementTask(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}

		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		placementTaskDao.deleteStudentInfo(request);
		placementTaskDao.deleteTeachingClassInfo(request);
		placementTaskDao.deletePlacementRule(request);
		placementTaskDao.deleteOpenClassTask(request);
		placementTaskDao.deleteOpenClassInfo(request);
		placementTaskDao.deletePlacementTask(request);
	}

	@Override
	public ExecProgressRedisMsg queryExecProgress(JSONObject request,
			String redisKey) throws Exception {
		ExecProgressRedisMsg msg = (ExecProgressRedisMsg) redisOperationDAO
				.get(redisKey);
		if (msg == null) {
			msg = new ExecProgressRedisMsg();
			msg.setCode(-1);
			msg.setErrorMsg("无法从缓存获取对应的分班进度！");
			msg.setMessage("");
		}
		if (msg.getCode() == 1 && msg.getProgress() < 100) {
			// 重置缓存时间
			redisOperationDAO.expire(redisKey,
					CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} else {
			redisOperationDAO.del(redisKey);
		}
		return msg;
	}

	@Override
	public void startExecProcess(JSONObject request, String redisKey)
			throws Exception {
		// 开启线程
		threadPool.execute(new PlacementProcess(request, redisKey,
				transactionManager, placementTaskDao, scoreManageAPIService,
				wishFillingThirdService, commonDataService, redisOperationDAO));
		// new PlacementProcess(request, redisKey, transactionManager,
		// placementTaskDao, scoreInfoDao,
		// wishFillingThirdService, commonDataService, redisOperationDAO).run();
	}

	@Override
	public JSONObject queryOpenClassInfoByWfIdMicro(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 1) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为微走班类型！");
		}

		String wfId = request.getString("wfId");
		String wfTermInfo = request.getString("wfTermInfo");
		if (StringUtils.isBlank(wfId) || StringUtils.isBlank(wfTermInfo)) {
			throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
		}

		JSONObject data = new JSONObject();

		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		String usedGrade = pl.getUsedGrade();

		// 从选课获取数据，查看数据库中是否已经存储了开班数据
		request.put("placementType", pl.getPlacementType());
		request.put("type", 1);
		Map<String, OpenClassTask> sId2NumOfOpCl = new HashMap<String, OpenClassTask>();

		if (wfId.equals(pl.getWfId()) && wfTermInfo.equals(pl.getWfTermInfo())) {
			List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
			if (rules.size() > 0) {
				PlacementRule rule = rules.get(0);
				data.put("examId", rule.getExamId());
				data.put("ruleCode", rule.getRuleCode());
			}

			List<OpenClassTask> list = placementTaskDao
					.queryOpenClassTasksWithInfo(request);
			for (OpenClassTask opClassTask : list) {
				String subjectIdsStr = opClassTask.getOpenClassInfo()
						.getSubjectIdsStr(); // 微走班，都是组合数据
				if (subjectIdsStr == null) {
					continue;
				}
				sId2NumOfOpCl.put(subjectIdsStr, opClassTask);
			}
		}

		// 获取年级的所有人数
		Integer numOfGradeStuds = 0;
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer
				.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		String gradeName = njName.get(gl);
		data.put("gradeName", gradeName);
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl,
				termInfo);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
		}
		List<Classroom> crs = commonDataService.getClassroomBatch(schoolId,
				grade.getClassIds(), termInfo);
		if (crs == null) {
			throw new CommonRunException(-1, "无法获取年级下的班级信息，请联系管理员！");
		}
		for (Classroom cr : crs) {
			numOfGradeStuds += cr.getStudentAccountIdsSize();
		}

		Integer numOfWfStuds = 0;
		List<JSONObject> zhData = wishFillingThirdService
				.getZhStudentNumToThird(wfId, wfTermInfo, schoolId);
		if (CollectionUtils.isNotEmpty(zhData)) {
			for (JSONObject json : zhData) {
				String subjectIds = json.getString("subjectIds");
				Integer numOfStuds = json.getInteger("studentNum");
				json.remove("studentNum");
				json.put("numOfStuds", numOfStuds);
				if (numOfStuds != null) {
					numOfWfStuds += numOfStuds.intValue();
				}
				OpenClassTask opClassTask = sId2NumOfOpCl.get(subjectIds);
				if (opClassTask != null) {
					Integer numOfOpenClasses = opClassTask
							.getNumOfOpenClasses();
					json.put("openClassInfoId",
							opClassTask.getOpenClassInfoId());
					json.put("openClassTaskId",
							opClassTask.getOpenClassTaskId());
					if (numOfOpenClasses != null && numOfOpenClasses != 0) {
						json.put("numOfOpenClasses", numOfOpenClasses);
						json.put("classSize",
								Math.ceil(numOfStuds * 1.0d / numOfOpenClasses));
					}
				}
			}
		}

		data.put("numOfGradeStuds", numOfGradeStuds);
		data.put("numOfWfStuds", numOfWfStuds);
		data.put("zhData", zhData);
		data.put("status", pl.getStatus());
		return data;
	}

	private String queryOrUpdateRule(JSONObject params, String examId, String examTermInfo, 
			Integer ruleCode) {
		// 查询微走班的成绩的规则
		List<PlacementRule> ruleInfos = placementTaskDao.queryRuleInfo(params);
		String ruleId = null;
		if (CollectionUtils.isNotEmpty(ruleInfos)) {
			// 存在规则则进行判断，并且删除多于的规则
			Set<String> deleteRuleIds = new HashSet<String>();
			for (PlacementRule rule : ruleInfos) {
				String examIdRule = rule.getExamId();
				String examTermInfoRule = rule.getExamTermInfo();
				if ((examId == null && examIdRule == null && examTermInfo == null)
						|| (examId.equals(examIdRule) && examTermInfo.equals(examTermInfoRule) && ruleCode == rule
								.getRuleCode())) {
					ruleId = rule.getRuleId();
				} else {
					deleteRuleIds.add(rule.getRuleId());
				}
			}
			if (deleteRuleIds.size() > 0) { // 删除多于的规则
				params.remove("ruleId");
				params.put("ruleIds", deleteRuleIds);
				placementTaskDao.deletePlacementRule(params);
			}
		}
		params.remove("ruleIds");
		// 如果成绩没有被更改，那么ruleId的examId为原成绩id，如果更改，那么重新创建新的规则
		if (ruleId == null) {
			ruleId = UUIDUtil.getUUID();
			PlacementRule rule = new PlacementRule();
			rule.setRuleId(ruleId);
			rule.setPlacementId(params.getString("placementId"));
			rule.setSchoolId(params.getLong("schoolId"));
			rule.setTermInfo(params.getString("termInfo"));
			rule.setExamId(examId);
			rule.setExamTermInfo(examTermInfo);
			rule.setRuleCode(ruleCode);
			placementTaskDao.insertPlacementRule(rule);
			params.remove("ruleId");
		}

		return ruleId;
	}

	@Override
	public void saveOpenClassInfoMicro(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 1) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为微走班类型！");
		}

		Long schoolId = request.getLong("schoolId");

		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");

		String wfId = request.getString("wfId");
		String wfTermInfo = request.getString("wfTermInfo");
		String examId = request.getString("examId");
		String examTermInfo = request.getString("examTermInfo");
		Integer ruleCode = request.getInteger("ruleCode");
		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null || StringUtils.isEmpty(wfId)
				|| StringUtils.isEmpty(wfTermInfo)) {
			throw new CommonRunException(-1, "参数异常，请联系管理员！");
		}

		List<Object> openClassData = request.getJSONArray("openClassData");
		if (CollectionUtils.isEmpty(openClassData)) {
			throw new CommonRunException(-1, "开班数据参数异常，请联系管理员！");
		}

		// 需求：如分班已完成，再重新设置班级数或成绩方式。需要二次确定提示；确定后，进行分班；如只修改班级，没有修改成绩方式，没有修改过的班级仍然保留原分班结果；如果修改过成绩方式，则所有班级全部按新的成绩规则进行分班。
		// ************************** 判断成绩是否被更改 ******************************//
		JSONObject params = new JSONObject();
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("placementType", placementType);
		params.put("type", 1);
		if (!(wfId.equals(pl.getWfId()) && wfTermInfo
				.equals(pl.getWfTermInfo()))) {
			// 先删除所有的数据
			List<OpenClassInfo> openClassInfos = placementTaskDao
					.queryOpenClassInfo(params);
			if (CollectionUtils.isNotEmpty(openClassInfos)) {
				params.put("openClassInfos", openClassInfos);
				placementTaskDao.deleteStudentInfoWaitForPlacement(params);
				placementTaskDao.deleteStudentInfo(params);
				placementTaskDao.deletePlacementRule(params);
				placementTaskDao.deleteOpenClassTask(params);
				placementTaskDao.deleteOpenClassInfo(params);
			}

			// 更新参数
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setWfId(wfId);
			placementTask.setWfTermInfo(wfTermInfo);
			placementTask.setTermInfo(termInfo);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}

		// 查询微走班的成绩的规则
		String ruleId = queryOrUpdateRule(params, examId, examTermInfo, ruleCode);

		// ******************************** 判断开班信息和任务是新增还是更新
		// ***********************************//
		Map<String, OpenClassInfo> infoId2Obj = new HashMap<String, OpenClassInfo>();
		Map<String, OpenClassTask> taskId2Obj = new HashMap<String, OpenClassTask>();

		// 从数据库中获取对应的开班信息或任务
		List<OpenClassTask> openClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(params);
		for (OpenClassTask opClassTask : openClassTasksWithInfo) {
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			if (!infoId2Obj.containsKey(openClassInfoId)) {
				infoId2Obj.put(openClassInfoId, opClassTask.getOpenClassInfo());
			}
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			taskId2Obj.put(openClassTaskId, opClassTask);
		}

		// 需要新增的开班信息
		List<OpenClassInfo> openClassInfos = new ArrayList<OpenClassInfo>();
		// 需要新增的开班任务
		List<OpenClassTask> openClassTasks = new ArrayList<OpenClassTask>();
		// 统计从前台传递过来的开班信息
		Set<String> openClassIdSet = new HashSet<String>();
		for (Object obj : request.getJSONArray("openClassData")) {
			JSONObject json = (JSONObject) obj;
			String openClassInfoId = json.getString("openClassInfoId");
			if (StringUtils.isEmpty(openClassInfoId)) {
				openClassInfoId = UUIDUtil.getUUID();
			} else {
				openClassIdSet.add(openClassInfoId);
			}

			String zhName = json.getString("zhName");
			String subjectIdsStr = json.getString("subjectIds");
			if (infoId2Obj.containsKey(openClassInfoId)) { // 更新操作
				// 数据库存在openClassInfoId的记录
				OpenClassInfo opClassInfoDb = infoId2Obj.get(openClassInfoId);
				if (!ruleId.equals(opClassInfoDb.getRuleId())) {
					OpenClassInfo opClassInfo = new OpenClassInfo();
					opClassInfo.setOpenClassInfoId(openClassInfoId);
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setPlacementType(placementType);
					opClassInfo.setTermInfo(termInfo);
					opClassInfo.setZhName(zhName);
					opClassInfo.setSubjectIdsStr(subjectIdsStr);
					opClassInfo.setRuleId(ruleId);
					placementTaskDao.updateOpenClassInfo(opClassInfo);
				}
			} else { // 新增开班信息
				// 数据库不存在openClassInfoId的记录，则重新插入
				OpenClassInfo opClassInfo = new OpenClassInfo();
				opClassInfo.setOpenClassInfoId(openClassInfoId);
				opClassInfo.setSchoolId(schoolId);
				opClassInfo.setPlacementId(placementId);
				opClassInfo.setPlacementType(placementType);
				opClassInfo.setType(1); // 三科组合
				opClassInfo.setZhName(zhName);
				opClassInfo.setClassIdsStr("");
				opClassInfo.setSubjectIdsStr(subjectIdsStr);
				opClassInfo.setScoreUpLimit(0f); // 分数上线，大走班按成绩分层才有
				opClassInfo.setScoreDownLimit(0f); // 分数下线，大走班按成绩分层才有
				opClassInfo.setRuleId(ruleId);
				opClassInfo.setTermInfo(termInfo);
				openClassInfos.add(opClassInfo);
			}

			String openClassTaskId = json.getString("openClassTaskId");
			Integer numOfStuds = json.getInteger("numOfStuds");
			Integer numOfOpenClasses = json.getInteger("numOfOpenClasses");
			if (0 == numOfStuds) {
				numOfOpenClasses = 0;
			}
			Integer classSize = json.getInteger("classSize");
			if (StringUtils.isEmpty(openClassTaskId)) {
				openClassTaskId = UUIDUtil.getUUID();
			}
			if (taskId2Obj.containsKey(openClassTaskId)) { // 更新开班任务
				OpenClassTask opClassTaskDb = taskId2Obj.get(openClassTaskId);
				OpenClassTask opClassTask = new OpenClassTask();
				opClassTask.setTermInfo(termInfo);
				opClassTask.setOpenClassTaskId(openClassTaskId);
				opClassTask.setSchoolId(schoolId);
				opClassTask.setPlacementId(placementId);

				if (!ruleId
						.equals(opClassTaskDb.getOpenClassInfo().getRuleId())) {
					opClassTask.setStatus(0);
				}
				if (numOfStuds != null
						&& !numOfStuds.equals(opClassTaskDb.getNumOfStuds())) {
					opClassTask.setStatus(0);
					opClassTask.setNumOfStuds(numOfStuds);
				}
				if (numOfOpenClasses != null
						&& !numOfOpenClasses.equals(opClassTaskDb
								.getNumOfOpenClasses())) {
					opClassTask.setStatus(0);
					opClassTask.setNumOfOpenClasses(numOfOpenClasses);
				}
				if (classSize != null
						&& !classSize.equals(opClassTaskDb.getClassSize())) {
					opClassTask.setStatus(0);
					opClassTask.setClassSize(classSize);
				}
				if (opClassTask.getStatus() != null
						&& 0 == opClassTask.getStatus()) {
					placementTaskDao.updateOpenClassTask(opClassTask);
				}
			} else { // 新增开班任务
				OpenClassTask opClassTask = new OpenClassTask();
				opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
				opClassTask.setSchoolId(schoolId);
				opClassTask.setPlacementId(placementId);
				opClassTask.setOpenClassInfoId(openClassInfoId);
				opClassTask.setSubjectLevel(0);
				opClassTask.setLayName("");
				opClassTask.setLayValue(0);
				opClassTask.setNumOfStuds(numOfStuds);
				opClassTask.setNumOfOpenClasses(numOfOpenClasses);
				opClassTask.setClassSize(classSize);
				opClassTask.setTermInfo(termInfo);
				openClassTasks.add(opClassTask);
			}
		}

		// 先删除在新增，数据库中的开班信息没在前台传参中，那么删除这些信息
		List<String> deleteInfoIds = new ArrayList<String>();
		for (String openClassInfoId : infoId2Obj.keySet()) {
			if (!openClassIdSet.contains(openClassInfoId)) {
				deleteInfoIds.add(openClassInfoId);
			}
		}

		if (deleteInfoIds.size() > 0) {
			params.put("openClassInfoIds", deleteInfoIds);
			placementTaskDao.deleteStudentInfoWaitForPlacement(params);
			placementTaskDao.deleteStudentInfo(params);
			placementTaskDao.deleteTeachingClassInfo(params);
			placementTaskDao.deleteOpenClassTask(params);
			placementTaskDao.deleteOpenClassInfo(params);
		}

		if (openClassInfos.size() > 0) {
			placementTaskDao.insertOpenClassInfoBatch(openClassInfos, termInfo);
		}

		if (openClassTasks.size() > 0) {
			placementTaskDao.insertOpenClassTaskBatch(openClassTasks, termInfo);
		}

		// 更新分班任务主表
		if (pl.getStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(status);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	@Override
	public JSONObject queryZhDataMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		String usedGrade = pl.getUsedGrade();
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String wfId = request.getString("wfId");
		String wfTermInfo = request.getString("wfTermInfo");
		if (StringUtils.isEmpty(wfTermInfo) || StringUtils.isEmpty(wfId)) {
			throw new CommonRunException(-1, "传参错误，请联系管理员！");
		}

		Set<String> hasSelected = new HashSet<String>();
		if (wfId.equals(pl.getWfId()) && wfTermInfo.equals(pl.getWfTermInfo())) {
			JSONObject params = new JSONObject();
			params.put("placementType", placementType);
			params.put("schoolId", schoolId);
			params.put("placementId", placementId);
			params.put("type", 1); // 中走班的三科组合；
			params.put("termInfo", termInfo);
			List<OpenClassInfo> openClassInfos = placementTaskDao
					.queryOpenClassInfo(params);
			if (CollectionUtils.isNotEmpty(openClassInfos)) {
				for (OpenClassInfo openClassInfo : openClassInfos) {
					String subjectIdsStr = openClassInfo.getSubjectIdsStr();
					if (StringUtils.isNotEmpty(subjectIdsStr)) {
						hasSelected.add(subjectIdsStr);
					}
				}
			}
		}

		List<JSONObject> zhData = wishFillingThirdService
				.getZhStudentNumToThird(wfId, wfTermInfo, schoolId);
		Integer numOfWfStuds = 0;
		for (JSONObject json : zhData) {
			String subjectIds = json.getString("subjectIds");
			Integer num = json.getInteger("studentNum");
			json.put("numOfStuds", num);
			json.remove("studentNum");
			if (num != null) { // 计算选课人数
				numOfWfStuds += num;
			}
			if (hasSelected.contains(subjectIds)) {
				json.put("selected", 1);
			} else {
				json.put("selected", 0);
			}
		}

		// 获取年级的所有人数
		Integer numOfGradeStuds = 0;
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer
				.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl,
				termInfo);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
		}
		List<Classroom> crs = commonDataService.getClassroomBatch(schoolId,
				grade.getClassIds(), termInfo);
		if (crs == null) {
			throw new CommonRunException(-1, "无法获取年级下的班级信息，请联系管理员！");
		}
		for (Classroom cr : crs) {
			numOfGradeStuds += cr.getStudentAccountIdsSize();
		}

		JSONObject data = new JSONObject();
		data.put("status", pl.getStatus());
		data.put("numOfWfStuds", numOfWfStuds);
		data.put("numOfGradeStuds", numOfGradeStuds);
		data.put("gradeName", njName.get(gl));
		data.put("zhData", zhData);
		return data;
	}

	@Override
	public void saveZhDataMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String wfId = request.getString("wfId");
		String wfTermInfo = request.getString("wfTermInfo");
		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null || StringUtils.isEmpty(wfTermInfo)
				|| StringUtils.isEmpty(wfId)) {
			throw new CommonRunException(-1, "传参错误，请联系管理员！");
		}

		JSONObject params = new JSONObject();
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("placementType", placementType);
		// 如果选课轮次被改动，那么所有的数据都应该被清除重新开班
		if (!(wfId.equals(pl.getWfId()) && wfTermInfo
				.equals(pl.getWfTermInfo()))) {
			// 先删除所有的数据
			List<OpenClassInfo> openClassInfos = placementTaskDao
					.queryOpenClassInfo(params);
			if (CollectionUtils.isNotEmpty(openClassInfos)) {
				params.put("openClassInfos", openClassInfos);
				placementTaskDao.deleteStudentInfoWaitForPlacement(params);
				placementTaskDao.deleteStudentInfo(params);
				placementTaskDao.deletePlacementRule(params);
				placementTaskDao.deleteOpenClassTask(params);
				placementTaskDao.deleteOpenClassInfo(params);
			}

			// 更新参数
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setWfId(wfId);
			placementTask.setWfTermInfo(wfTermInfo);
			placementTask.setTermInfo(termInfo);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}

		// 数据库中三三组合的数据，key为subjectIds，值为信息表和任务表关联数据
		Map<String, OpenClassTask> zhMap = new HashMap<String, OpenClassTask>();
		// 数据库中单科的数据，key为单科subjectId，值为信息表和任务表关联数据
		Map<Long, OpenClassTask[]> dkMap = new HashMap<Long, OpenClassTask[]>();
		// 无固定组合数据，即中走班中开教学班的数据
		OpenClassTask jxObj = null;
		// 从数据库中查询信息和任务数据
		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(params);
		for (OpenClassTask opClassTask : opClassTasksWithInfo) {
			Integer type = opClassTask.getOpenClassInfo().getType();
			if (type == 1) { // 三三固定组合
				zhMap.put(opClassTask.getOpenClassInfo().getSubjectIdsStr(),
						opClassTask);
			} else if (type == 2) { // 无固定组合数据
				jxObj = opClassTask;
			} else if (type == 3) { // 单科志愿数据
				Long subjectId = Long.valueOf(opClassTask.getOpenClassInfo()
						.getSubjectIdsStr());
				if (!dkMap.containsKey(subjectId)) {
					dkMap.put(subjectId, new OpenClassTask[2]);
				}
				Integer subjectLevel = opClassTask.getSubjectLevel(); // 1为选考（高考），2位学考
				dkMap.get(subjectId)[subjectLevel - 1] = opClassTask;
			}
		}

		List<OpenClassInfo> openClassInfos = new ArrayList<OpenClassInfo>();
		List<OpenClassTask> openClassTasks = new ArrayList<OpenClassTask>();

		// 单科组合计算人数的数据结构，科目对应人数
		Map<Long, Integer> subjectId2NumOfStuds = new HashMap<Long, Integer>();
		Integer noZhNumOfStuds = 0; // 未选组合的人数，需要开未固定组合班级（类似行政班级）
		Set<String> hasSubjectIdsSet = new HashSet<String>();
		for (Object obj : request.getJSONArray("zhData")) {
			JSONObject json = (JSONObject) obj;
			// 接口传递的是subjectIds，注意一下
			String subjectIdsStr = json.getString("subjectIds");
			if (StringUtils.isEmpty(subjectIdsStr)) {
				continue;
			}
			// 组合人数
			Integer numOfStuds = json.getInteger("numOfStuds");
			if (numOfStuds == null) {
				numOfStuds = 0;
			}
			// 是否选中了组合
			Integer selected = json.getInteger("selected");
			if (1 == selected) { // 选中组合，开三三组合
				// 传入的组合科目代码，因为没有主键，组合以subjectIds为主键
				hasSubjectIdsSet.add(subjectIdsStr);

				if (zhMap.containsKey(subjectIdsStr)) { // 数据库存有相同的组合数据，则只进行数据更新
					// 存有相同的数据
					OpenClassTask opClassTaskDb = zhMap.get(subjectIdsStr);
					if (numOfStuds != null
							&& !numOfStuds
									.equals(opClassTaskDb.getNumOfStuds())) { // 查看值是否变动
						OpenClassTask opClassTask = new OpenClassTask();
						opClassTask.setTermInfo(termInfo);
						opClassTask.setOpenClassTaskId(opClassTaskDb
								.getOpenClassTaskId());
						opClassTask.setOpenClassInfoId(opClassTaskDb
								.getOpenClassInfoId());
						opClassTask.setSchoolId(schoolId);
						opClassTask.setPlacementId(placementId);
						opClassTask.setNumOfStuds(numOfStuds);
						opClassTask.setStatus(0);
						Integer numOfOpenClasses = opClassTaskDb
								.getNumOfOpenClasses();
						if (numOfOpenClasses != null && numOfOpenClasses != 0) {
							opClassTask.setClassSize((int) Math
									.ceil(((double) numOfStuds)
											/ numOfOpenClasses));
						}
						placementTaskDao.updateOpenClassTask(opClassTask);
					}
				} else {
					// 数据库不存在对应的数据
					String openClassInfoId = UUIDUtil.getUUID();
					OpenClassInfo opClassInfo = new OpenClassInfo();
					opClassInfo.setOpenClassInfoId(openClassInfoId);
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setPlacementType(placementType);
					opClassInfo.setType(1);
					opClassInfo.setZhName(json.getString("zhName"));
					opClassInfo.setSubjectIdsStr(subjectIdsStr);
					opClassInfo.setClassIdsStr(""); // 默认classIds，大走班按成绩分层才有
					opClassInfo.setScoreUpLimit(0f); // 分数上线，大走班按成绩分层才有
					opClassInfo.setScoreDownLimit(0f); // 分数下线，大走班按成绩分层才有
					opClassInfo.setTermInfo(termInfo);
					openClassInfos.add(opClassInfo);

					OpenClassTask opClassTask = new OpenClassTask();
					opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
					opClassTask.setSchoolId(schoolId);
					opClassTask.setPlacementId(placementId);
					opClassTask.setOpenClassInfoId(openClassInfoId);
					opClassTask.setSubjectLevel(0);
					opClassTask.setLayName("");
					opClassTask.setLayValue(0);
					opClassTask.setNumOfStuds(numOfStuds);
					opClassTask.setTermInfo(termInfo);
					openClassTasks.add(opClassTask);
				}
			} else { // 以单科走班的形式
				noZhNumOfStuds += numOfStuds; // 未选组合的所有人数，因为每人只能选三科，一个组合对应三科，因此直接相加就行

				String[] tmp = subjectIdsStr.split(","); // 组合subjectIds拆成单科科目代码
				for (String id : tmp) {
					if (StringUtils.isEmpty(id)) {
						continue;
					}
					Long subjectId = Long.valueOf(id);
					if (!subjectId2NumOfStuds.containsKey(subjectId)) {
						subjectId2NumOfStuds.put(subjectId, 0);
					}
					subjectId2NumOfStuds.put(subjectId,
							subjectId2NumOfStuds.get(subjectId) + numOfStuds);
				}
			}
		}

		// 数据库中的数据存在，而传入选中的组合不存在，则需要删除多于的数据
		List<String> deleteSubjectIds = new ArrayList<String>();
		for (String subjectIds : zhMap.keySet()) {
			if (!hasSubjectIdsSet.contains(subjectIds)) {
				deleteSubjectIds.add(subjectIds);
			}
		}

		if (jxObj != null) { // 三三无固定组合开班，教学班
			if (!noZhNumOfStuds.equals(jxObj.getNumOfStuds())) {
				OpenClassTask opClassTask = new OpenClassTask();
				opClassTask.setTermInfo(termInfo);
				opClassTask.setOpenClassTaskId(jxObj.getOpenClassTaskId());
				opClassTask.setSchoolId(schoolId);
				opClassTask.setPlacementId(placementId);
				opClassTask.setNumOfStuds(noZhNumOfStuds);
				opClassTask.setStatus(0);
				Integer numOfOpenClasses = jxObj.getNumOfOpenClasses();
				if (numOfOpenClasses != null && numOfOpenClasses != 0) {
					opClassTask
							.setClassSize((int) Math
									.ceil(((double) noZhNumOfStuds)
											/ numOfOpenClasses));
				}
				placementTaskDao.updateOpenClassTask(opClassTask);
			}
		} else {
			String openClassInfoId = UUIDUtil.getUUID();
			OpenClassInfo opClassInfo = new OpenClassInfo();
			opClassInfo.setOpenClassInfoId(openClassInfoId);
			opClassInfo.setSchoolId(schoolId);
			opClassInfo.setPlacementId(placementId);
			opClassInfo.setPlacementType(placementType);
			opClassInfo.setType(2);
			opClassInfo.setZhName("教学");
			opClassInfo.setSubjectIdsStr("");
			opClassInfo.setClassIdsStr(""); // 默认classIds，大走班按成绩分层才有
			opClassInfo.setScoreUpLimit(0f); // 分数上线，大走班按成绩分层才有
			opClassInfo.setScoreDownLimit(0f); // 分数下线，大走班按成绩分层才有
			opClassInfo.setTermInfo(termInfo);
			openClassInfos.add(opClassInfo);

			OpenClassTask opClassTask = new OpenClassTask();
			opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
			opClassTask.setSchoolId(schoolId);
			opClassTask.setPlacementId(placementId);
			opClassTask.setOpenClassInfoId(openClassInfoId);
			opClassTask.setSubjectLevel(0); // 中走班组合
			opClassTask.setLayName(""); // 大走班按成绩分层才有
			opClassTask.setLayValue(0); // 大走班按成绩分层才有
			opClassTask.setNumOfStuds(noZhNumOfStuds);
			opClassTask.setTermInfo(termInfo);
			openClassTasks.add(opClassTask);
		}

		Set<Long> existsSubjectSet = new HashSet<Long>();
		if (subjectId2NumOfStuds.size() > 0) { // 中走班单科数据（按志愿分层）
			List<LessonInfo> lessonInfos = commonDataService
					.getLessonInfoBatch(schoolId, new ArrayList<Long>(
							subjectId2NumOfStuds.keySet()), termInfo);
			if (CollectionUtils.isEmpty(lessonInfos)) {
				throw new CommonRunException(-1, "无法获取科目信息，请联系管理员！");
			}

			Map<Long, String> subjectId2Name = new HashMap<Long, String>();
			for (LessonInfo lessonInfo : lessonInfos) {
				subjectId2Name.put(lessonInfo.getId(), lessonInfo.getName());
			}

			for (Map.Entry<Long, Integer> entry : subjectId2NumOfStuds
					.entrySet()) {
				Long subjectId = entry.getKey();

				existsSubjectSet.add(subjectId);

				Integer numOfStuds = entry.getValue();
				if (dkMap.containsKey(subjectId)) {
					OpenClassTask[] opClassTaskDb = dkMap.get(subjectId);
					OpenClassTask opClassTask = new OpenClassTask();
					opClassTask.setTermInfo(termInfo);
					opClassTask.setSchoolId(schoolId);
					opClassTask.setPlacementId(placementId);
					if (!numOfStuds.equals(opClassTaskDb[0].getNumOfStuds())) {
						opClassTask.setOpenClassTaskId(opClassTaskDb[0]
								.getOpenClassTaskId());
						opClassTask.setNumOfStuds(numOfStuds);
						opClassTask.setStatus(0);
						Integer numOfOpenClasses = opClassTaskDb[0]
								.getNumOfOpenClasses();
						if (numOfOpenClasses != null && numOfOpenClasses != 0) {
							opClassTask.setClassSize((int) Math
									.ceil(((double) numOfStuds)
											/ numOfOpenClasses));
						}
						placementTaskDao.updateOpenClassTask(opClassTask);
					}
					if ((noZhNumOfStuds - numOfStuds) != opClassTaskDb[1]
							.getNumOfStuds()) {
						opClassTask.setOpenClassTaskId(opClassTaskDb[1]
								.getOpenClassTaskId());
						opClassTask
								.setNumOfStuds((noZhNumOfStuds - numOfStuds));
						opClassTask.setStatus(0);
						Integer numOfOpenClasses = opClassTaskDb[1]
								.getNumOfOpenClasses();
						if (numOfOpenClasses != null && numOfOpenClasses != 0) {
							opClassTask.setClassSize((int) Math
									.ceil((noZhNumOfStuds - numOfStuds)
											/ (double) numOfOpenClasses));
						}
						placementTaskDao.updateOpenClassTask(opClassTask);
					}
				} else {
					String openClassInfoId = UUIDUtil.getUUID();
					OpenClassInfo opClassInfo = new OpenClassInfo();
					opClassInfo.setOpenClassInfoId(openClassInfoId);
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setPlacementType(placementType);
					opClassInfo.setType(3); // 单科
					opClassInfo.setZhName(subjectId2Name.get(subjectId));
					opClassInfo.setSubjectIdsStr(String.valueOf(subjectId));
					opClassInfo.setClassIdsStr(""); // 默认classIds，大走班按成绩分层才有
					opClassInfo.setScoreUpLimit(0f); // 分数上线，大走班按成绩分层才有
					opClassInfo.setScoreDownLimit(0f); // 分数下线，大走班按成绩分层才有
					opClassInfo.setTermInfo(termInfo);
					openClassInfos.add(opClassInfo);

					// 高考
					OpenClassTask opClassTaskG = new OpenClassTask();
					opClassTaskG.setOpenClassTaskId(UUIDUtil.getUUID());
					opClassTaskG.setSchoolId(schoolId);
					opClassTaskG.setPlacementId(placementId);
					opClassTaskG.setOpenClassInfoId(openClassInfoId);
					opClassTaskG.setSubjectLevel(1); // 中走班组合，(选考)高考
					opClassTaskG.setLayName(""); // 大走班按成绩分层才有
					opClassTaskG.setLayValue(0); // 大走班按成绩分层才有
					opClassTaskG.setNumOfStuds(numOfStuds);
					opClassTaskG.setTermInfo(termInfo);
					openClassTasks.add(opClassTaskG);
					// 学考
					OpenClassTask opClassTaskX = new OpenClassTask();
					opClassTaskX.setOpenClassTaskId(UUIDUtil.getUUID());
					opClassTaskX.setSchoolId(schoolId);
					opClassTaskX.setPlacementId(placementId);
					opClassTaskX.setOpenClassInfoId(openClassInfoId);
					opClassTaskX.setSubjectLevel(2); // 中走班学考
					opClassTaskX.setLayName(""); // 大走班按成绩分层才有
					opClassTaskX.setLayValue(0); // 大走班按成绩分层才有
					opClassTaskX.setNumOfStuds(noZhNumOfStuds - numOfStuds); // 学考人数
					opClassTaskX.setTermInfo(termInfo);
					openClassTasks.add(opClassTaskX);
				}
			}
		}

		for (Long subjectId : dkMap.keySet()) {
			if (!existsSubjectSet.contains(subjectId)) {
				deleteSubjectIds.add(String.valueOf(subjectId));
			}
		}

		// 先删除后添加
		if (deleteSubjectIds.size() > 0) {
			params.put("multipleSubjectIdsStr", deleteSubjectIds);

			List<OpenClassInfo> opClassInfoList = placementTaskDao
					.queryOpenClassInfo(params);
			params.put("openClassInfos", opClassInfoList);
			// 必须先删除任务，再删除信息
			placementTaskDao.deleteStudentInfoWaitForPlacement(params);
			placementTaskDao.deleteStudentInfo(params);
			placementTaskDao.deleteTeachingClassInfo(params);
			placementTaskDao.deleteOpenClassTask(params);
			placementTaskDao.deleteOpenClassInfo(params);
			params.remove("openClassInfos");
			params.remove("multipleSubjectIdsStr");
		}

		if (openClassInfos.size() > 0) {
			placementTaskDao.insertOpenClassInfoBatch(openClassInfos, termInfo);
		}
		if (openClassTasks.size() > 0) {
			placementTaskDao.insertOpenClassTaskBatch(openClassTasks, termInfo);
		}

		// 更新分班任务主表
		if (pl.getStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(status);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	@Override
	public JSONObject queryZhOpenClassInfoMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		JSONObject data = new JSONObject();

		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");

		JSONObject params = new JSONObject();
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		params.put("placementType", placementType); // 中走班
		params.put("type", 1); // 三科组合

		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		if (rules.size() > 0) {
			PlacementRule rule = rules.get(0);
			data.put("examId", rule.getExamId());
			data.put("ruleCode", rule.getRuleCode());
		}

		List<OpenClassTask> opClassTasksWidthInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(params);
		if (CollectionUtils.isEmpty(opClassTasksWidthInfo)) {
			return data;
		}
		Integer numOfZhStud = 0;
		List<JSONObject> openClassData = new ArrayList<JSONObject>();
		for (OpenClassTask opClassTask : opClassTasksWidthInfo) {
			Integer numOfStuds = opClassTask.getNumOfStuds();
			numOfZhStud += numOfStuds;
			Integer numOfOpenClasses = opClassTask.getNumOfOpenClasses();
			if (0 == numOfStuds) {
				numOfOpenClasses = 0;
			}
			Integer classSize = opClassTask.getClassSize();
			String zhName = opClassTask.getOpenClassInfo().getZhName();
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			JSONObject obj = new JSONObject();
			obj.put("openClassInfoId", openClassInfoId);
			obj.put("openClassTaskId", openClassTaskId);
			obj.put("zhName", zhName);
			obj.put("numOfStud", numOfStuds);
			obj.put("numOfOpenClasses", numOfOpenClasses);
			obj.put("classSize", classSize);
			openClassData.add(obj);
		}
		data.put("openClassData", openClassData);
		data.put("numOfZhStud", numOfZhStud);
		data.put("status", pl.getStatus());
		return data;
	}

	@Override
	public void saveZhOpenClassInfoMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String examId = request.getString("examId");
		String examTermInfo = request.getString("examTermInfo");
		Integer ruleCode = request.getInteger("ruleCode");

		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null) {
			throw new CommonRunException(-1, "参数错误，请联系管理员！");
		}

		JSONObject params = new JSONObject();
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("placementType", placementType);
		params.put("type", 1);
		// 查询微走班的成绩的规则
		String ruleId = queryOrUpdateRule(params, examId, examTermInfo, ruleCode);

		Map<String, OpenClassTask> taskId2Obj = new HashMap<String, OpenClassTask>();
		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(params);
		for (OpenClassTask opClassTask : opClassTasksWithInfo) {
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			taskId2Obj.put(openClassTaskId, opClassTask);
		}

		List<Object> list = request.getJSONArray("openClassData");
		if (CollectionUtils.isNotEmpty(list)) {
			for (Object obj : list) {
				JSONObject json = (JSONObject) obj;
				String openClassTaskId = json.getString("openClassTaskId");
				// 数据是从数据库中获取的，不存在删除操作
				OpenClassTask opClassTaskDb = taskId2Obj.get(openClassTaskId);
				if (opClassTaskDb != null) {
					OpenClassTask opClassTask = new OpenClassTask();
					opClassTask.setTermInfo(termInfo);
					opClassTask.setOpenClassTaskId(openClassTaskId);
					opClassTask.setSchoolId(schoolId);
					opClassTask.setPlacementId(placementId);

					if (!ruleId.equals(opClassTaskDb.getOpenClassInfo()
							.getRuleId())) {
						OpenClassInfo opClassInfo = new OpenClassInfo();
						opClassInfo.setOpenClassInfoId(opClassTaskDb
								.getOpenClassInfoId());
						opClassInfo.setTermInfo(termInfo);
						opClassInfo.setSchoolId(schoolId);
						opClassInfo.setPlacementId(placementId);
						opClassInfo.setRuleId(ruleId);
						opClassInfo.setPlacementType(placementType);
						placementTaskDao.updateOpenClassInfo(opClassInfo);

						opClassTask.setStatus(0);
					}
					Integer numOfOpenClasses = json
							.getInteger("numOfOpenClasses");
					if (numOfOpenClasses != null
							&& numOfOpenClasses != opClassTaskDb
									.getNumOfOpenClasses()) {
						opClassTask.setStatus(0);
						opClassTask.setNumOfOpenClasses(numOfOpenClasses);
					}

					Integer classSize = json.getInteger("classSize");
					if (classSize != null
							&& classSize != opClassTaskDb.getClassSize()) {
						opClassTask.setStatus(0);
						opClassTask.setClassSize(classSize);
					}
					if (opClassTask.getStatus() != null
							&& 0 == opClassTask.getStatus()) {
						placementTaskDao.updateOpenClassTask(opClassTask);
					}
				}
			}
		}

		// 更新分班任务主表
		if (pl.getStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(status);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	@Override
	public JSONObject queryRemainOpenClassInfoMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		JSONObject data = new JSONObject();

		request.put("placementType", placementType); // 中走班
		request.put("types", new int[] { 2, 3 }); // 三科组合

		Map<String, PlacementRule> id2Rule = new HashMap<String, PlacementRule>();
		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		for (PlacementRule rule : rules) {
			id2Rule.put(rule.getRuleId(), rule);
		}

		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		if (CollectionUtils.isEmpty(opClassTasksWithInfo)) {
			return data;
		}
		Map<String, JSONObject> remainOpenClassMap = new HashMap<String, JSONObject>();
		String ruleId = null;
		for (OpenClassTask opClassTask : opClassTasksWithInfo) {
			Integer type = opClassTask.getOpenClassInfo().getType();
			// 单科志愿
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			if (type == 2) { // 方案2的无固定组合
				JSONObject jsonObj = new JSONObject();
				String jxRuleId = opClassTask.getOpenClassInfo().getRuleId();
				if (id2Rule.containsKey(jxRuleId)) {
					PlacementRule rule = id2Rule.get(jxRuleId);
					jsonObj.put("examId", rule.getExamId());
					jsonObj.put("ruleCode", rule.getRuleCode());
				}
				jsonObj.put("openClassInfoId", openClassInfoId);
				jsonObj.put("openClassTaskId", opClassTask.getOpenClassTaskId());
				jsonObj.put("numOfStuds", opClassTask.getNumOfStuds());
				jsonObj.put("numOfOpenClasses",
						opClassTask.getNumOfOpenClasses());
				jsonObj.put("classSize", opClassTask.getClassSize());
				data.put("teachingOpenClassData", jsonObj);
				continue;
			}

			ruleId = opClassTask.getOpenClassInfo().getRuleId();

			if (!remainOpenClassMap.containsKey(openClassInfoId)) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("zhName", opClassTask.getOpenClassInfo()
						.getZhName());
				jsonObj.put("subjectId", Long.valueOf(opClassTask
						.getOpenClassInfo().getSubjectIdsStr()));
				remainOpenClassMap.put(openClassInfoId, jsonObj);
			}
			JSONObject jsonObj = remainOpenClassMap.get(openClassInfoId);
			if (1 == opClassTask.getSubjectLevel()) { // 高考
				jsonObj.put("numOfStudsG", opClassTask.getNumOfStuds());
				jsonObj.put("numOfOpenClassesG",
						opClassTask.getNumOfOpenClasses());
				jsonObj.put("openClassInfoIdG",
						opClassTask.getOpenClassInfoId());
				jsonObj.put("openClassTaskIdG",
						opClassTask.getOpenClassTaskId());
				jsonObj.put("classSizeG", opClassTask.getClassSize());
			} else { // 学考
				jsonObj.put("numOfStudsX", opClassTask.getNumOfStuds());
				jsonObj.put("numOfOpenClassesX",
						opClassTask.getNumOfOpenClasses());
				jsonObj.put("openClassInfoIdX",
						opClassTask.getOpenClassInfoId());
				jsonObj.put("openClassTaskIdX",
						opClassTask.getOpenClassTaskId());
				jsonObj.put("classSizeX", opClassTask.getClassSize());
			}
		}
		JSONObject remainOpenClassData = new JSONObject();
		List<JSONObject> list = new ArrayList<JSONObject>(
				remainOpenClassMap.values());
		remainOpenClassData.put("data", list);

		if (id2Rule.containsKey(ruleId)) {
			PlacementRule rule = id2Rule.get(ruleId);
			remainOpenClassData.put("examId", rule.getExamId());
			remainOpenClassData.put("ruleCode", rule.getRuleCode());
		}

		if (remainOpenClassData.size() > 0) {
			Collections.sort(list, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					long subjectId1 = o1.getLongValue("subjectId");
					long subjectId2 = o2.getLongValue("subjectId");
					return Long.compare(subjectId1, subjectId2);
				}
			});
		}

		for (JSONObject json : list) {
			Integer numOfStudsG = json.getInteger("numOfStudsG");
			Integer numOfStudsX = json.getInteger("numOfStudsX");

			Integer numOfOpenClassesG = json.getInteger("numOfOpenClassesG");
			Integer numOfOpenClassesX = json.getInteger("numOfOpenClassesX");
			if (numOfOpenClassesG == null && numOfOpenClassesX == null) {
				json.put("numOfStudsPlaceG", numOfStudsG);
				json.put("numOfStudsPlaceX", numOfStudsX);
				continue;
			}
			if (numOfOpenClassesG != 0 && numOfOpenClassesX == 0) {
				json.put("numOfStudsPlaceG", numOfStudsG + numOfStudsX);
				json.put("numOfStudsPlaceX", 0);
			} else if (numOfOpenClassesG == 0 && numOfOpenClassesX != 0) {
				json.put("numOfStudsPlaceG", 0);
				json.put("numOfStudsPlaceX", numOfStudsG + numOfStudsX);
			} else {
				json.put("numOfStudsPlaceG", numOfStudsG);
				json.put("numOfStudsPlaceX", numOfStudsX);
			}
		}

		data.put("subjectOpenClassData", remainOpenClassData);
		data.put("status", pl.getStatus());
		return data;
	}

	@Override
	public void saveRemainOpenClassInfoMedium(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 2) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null) {
			throw new CommonRunException(-1, "参数错误，请联系管理员！");
		}

		JSONObject data = (JSONObject) request.remove("data");
		JSONObject teachingOpenClassData = data
				.getJSONObject("teachingOpenClassData");
		JSONObject subjectOpenClassData = data
				.getJSONObject("subjectOpenClassData");

		JSONObject params = new JSONObject();
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("placementType", placementType);
		params.put("types", new int[] { 2, 3 }); // 无固定科目组合和单科

		Map<String, OpenClassTask> taskId2Obj = new HashMap<String, OpenClassTask>();
		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(params);
		for (OpenClassTask opClassTask : opClassTasksWithInfo) {
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			taskId2Obj.put(openClassTaskId, opClassTask);
		}
		params.remove("types");

		// 查询中走班无固定组合走班规则
		params.put("type", 2);
		String ruleId = queryOrUpdateRule(params,
				teachingOpenClassData.getString("examId"),
				teachingOpenClassData.getString("examTermInfo"),
				teachingOpenClassData.getInteger("ruleCode"));

		if (teachingOpenClassData != null) { // 教学班级数据存储
			String openClassTaskId = teachingOpenClassData
					.getString("openClassTaskId");

			OpenClassTask opClassTaskDb = taskId2Obj.get(openClassTaskId);
			if (opClassTaskDb != null) {

				OpenClassTask opClassTask = new OpenClassTask();
				opClassTask.setTermInfo(termInfo);
				opClassTask.setOpenClassTaskId(openClassTaskId);
				opClassTask.setSchoolId(schoolId);
				opClassTask.setPlacementId(placementId);

				if (!ruleId
						.equals(opClassTaskDb.getOpenClassInfo().getRuleId())) {
					OpenClassInfo opClassInfo = new OpenClassInfo();
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setRuleId(ruleId);
					opClassInfo.setPlacementType(placementType);
					opClassInfo.setTermInfo(termInfo);
					opClassInfo.setOpenClassInfoId(opClassTaskDb
							.getOpenClassInfoId());
					placementTaskDao.updateOpenClassInfo(opClassInfo);

					opClassTask.setStatus(0);
				}

				Integer numOfOpenClasses = teachingOpenClassData
						.getInteger("numOfOpenClasses");
				if (numOfOpenClasses != null
						&& !numOfOpenClasses.equals(opClassTaskDb
								.getNumOfOpenClasses())) {
					opClassTask.setStatus(0);
					opClassTask.setNumOfOpenClasses(numOfOpenClasses);
				}

				Integer classSize = teachingOpenClassData
						.getInteger("classSize");
				if (classSize != null
						&& !classSize.equals(opClassTaskDb.getClassSize())) {
					opClassTask.setStatus(0);
					opClassTask.setClassSize(classSize);
				}
				if (opClassTask.getStatus() != null
						&& 0 == opClassTask.getStatus()) {
					placementTaskDao.updateOpenClassTask(opClassTask);
				}
			}
		}

		// 查询中走班单科规则
		params.put("type", 3);
		ruleId = queryOrUpdateRule(params,
				subjectOpenClassData.getString("examId"),
				subjectOpenClassData.getString("examTermInfo"),
				subjectOpenClassData.getInteger("ruleCode"));

		if (subjectOpenClassData != null) { // 志愿单科数据存储
			List<Object> list = subjectOpenClassData.getJSONArray("list");
			if (CollectionUtils.isNotEmpty(list)) {
				for (Object obj : list) {
					JSONObject json = (JSONObject) obj;
					String openClassTaskId = json.getString("openClassTaskId");
					OpenClassTask opClassTaskDb = taskId2Obj
							.get(openClassTaskId);
					if (opClassTaskDb == null) {
						continue;
					}

					OpenClassTask opClassTask = new OpenClassTask();
					opClassTask.setTermInfo(termInfo);
					opClassTask.setOpenClassTaskId(json
							.getString("openClassTaskId"));
					opClassTask.setSchoolId(schoolId);
					opClassTask.setPlacementId(placementId);
					if (!ruleId.equals(opClassTaskDb.getOpenClassInfo()
							.getRuleId())) {
						OpenClassInfo opClassInfo = new OpenClassInfo();
						opClassInfo.setOpenClassInfoId(opClassTaskDb
								.getOpenClassInfoId());
						opClassInfo.setSchoolId(schoolId);
						opClassInfo.setTermInfo(termInfo);
						opClassInfo.setRuleId(ruleId);
						opClassInfo.setPlacementId(placementId);
						opClassInfo.setPlacementType(placementType);
						placementTaskDao.updateOpenClassInfo(opClassInfo);

						opClassTask.setStatus(0);
					}

					Integer numOfOpenClasses = json
							.getInteger("numOfOpenClasses");
					if (numOfOpenClasses != null
							&& !numOfOpenClasses.equals(opClassTaskDb
									.getNumOfOpenClasses())) {
						opClassTask.setStatus(0);
						opClassTask.setNumOfOpenClasses(numOfOpenClasses);
					}

					Integer classSize = json.getInteger("classSize");
					if (classSize != null
							&& !classSize.equals(opClassTaskDb.getClassSize())) {
						opClassTask.setStatus(0);
						opClassTask.setClassSize(classSize);
					}
					if (opClassTask.getStatus() != null
							&& 0 == opClassTask.getStatus()) {
						placementTaskDao.updateOpenClassTask(opClassTask);
					}
				}
			}
		}
		// 更新分班任务主表
		if (pl.getStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(status);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	@Override
	public List<JSONObject> queryLayerSetterInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		Long selectedSubjectId = request.getLong("subjectId");

		// ************************** 排除按志愿分层的科目
		// **************************************//
		request.put("placementType", placementType);
		request.put("type", 3); // 需要排除按志愿分层的科目
		List<OpenClassInfo> openClassInfo = placementTaskDao
				.queryOpenClassInfo(request);

		List<Long> subjectIds = new ArrayList<Long>();
		Set<Long> hasSubjectIds = new HashSet<Long>();
		for (OpenClassInfo opClassInfo : openClassInfo) {
			Long subjectId = Long.parseLong(opClassInfo.getSubjectIdsStr());
			hasSubjectIds.add(subjectId);
		}
		for (Long subjectId : commSubjectIdSet) {
			if (!hasSubjectIds.contains(subjectId)) {
				subjectIds.add(subjectId);
			}
		}

		// ************************** 获取未全部设置班级的科目信息和对应的班级
		// **************************************//
		request.remove("type");
		request.put("types", new int[] { 4, 5 }); // 按成绩分层的科目，4：分两层，5：分三层
		openClassInfo = placementTaskDao.queryOpenClassInfo(request);
		if (openClassInfo == null) {
			openClassInfo = new ArrayList<OpenClassInfo>();
		}
		// 科目对应的班级id集合
		Map<Long, Set<Long>> hasSubjId2CIds = new HashMap<Long, Set<Long>>();
		for (OpenClassInfo opClassInfo : openClassInfo) {
			Long subjectId = Long.valueOf(opClassInfo.getSubjectIdsStr());
			if (subjectId == null) {
				continue;
			}
			if (!hasSubjId2CIds.containsKey(subjectId)) {
				hasSubjId2CIds.put(subjectId, new HashSet<Long>());
			}
			Set<Long> classIds = hasSubjId2CIds.get(subjectId);
			classIds.addAll(StringUtil.convertToListFromStr(
					opClassInfo.getClassIdsStr(), ",", Long.class));
		}

		String usedGrade = pl.getUsedGrade();
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer
				.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl,
				termInfo);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
		}
		if (CollectionUtils.isEmpty(grade.getClassIds())) {
			throw new CommonRunException(-1, "此年级下无班级信息，请联系管理员！");
		}

		// 还有班级未选择的subjectId
		Map<Long, List<Long>> subjId2CIds = new HashMap<Long, List<Long>>();
		for (Long subjectId : subjectIds) {
			Set<Long> hasClassId = hasSubjId2CIds.get(subjectId);
			if (CollectionUtils.isEmpty(hasClassId)) {
				subjId2CIds.put(subjectId, grade.getClassIds());
				continue;
			}
			for (Long classId : grade.getClassIds()) {
				if (hasClassId.contains(classId)) {
					continue;
				}
				if (!subjId2CIds.containsKey(subjectId)) {
					subjId2CIds.put(subjectId, new ArrayList<Long>());
				}
				subjId2CIds.get(subjectId).add(classId);
			}
		}

		// ************************** 获取默认选择的科目
		// *********************************//
		if (selectedSubjectId == null
				|| !subjId2CIds.containsKey(selectedSubjectId)) {
			for (Long subjectId : commSubjectIdSet) {
				if (subjId2CIds.containsKey(subjectId)) {
					selectedSubjectId = subjectId;
					break;
				}
			}
		}

		// ******************************获取班级id映射名称结构****************************************//
		List<Classroom> crs = commonDataService.getClassroomBatch(schoolId,
				grade.getClassIds(), termInfo);
		if (CollectionUtils.isNotEmpty(grade.getClassIds())
				&& CollectionUtils.isEmpty(crs)) {
			throw new CommonRunException(-1, "无法获取年级下的班级信息，请联系管理员！");
		}
		Map<Long, String> classId2Name = new HashMap<Long, String>();
		for (Classroom cr : crs) {
			classId2Name.put(cr.getId(), cr.getClassName());
		}

		// **********************************************************************************//
		List<JSONObject> subjects = new ArrayList<JSONObject>();
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(
				schoolId, new ArrayList<Long>(subjId2CIds.keySet()), termInfo);
		if (CollectionUtils.isEmpty(lessonInfos)) {
			return subjects;
		}
		for (LessonInfo lessonInfo : lessonInfos) {
			JSONObject subject = new JSONObject();
			Long subjectId = lessonInfo.getId();
			subject.put("subjectId", subjectId);
			subject.put("subjectName", lessonInfo.getName());
			if (selectedSubjectId == lessonInfo.getId()) {
				subject.put("selected", 1);
			}
			List<Long> classIds = subjId2CIds.get(subjectId);
			List<JSONObject> classes = new ArrayList<JSONObject>();
			for (Long classId : classIds) {
				JSONObject classInfo = new JSONObject();
				classInfo.put("classId", classId);
				classInfo.put("className", classId2Name.get(classId));
				classes.add(classInfo);
			}
			if (CollectionUtils.isNotEmpty(classes)) {
				Collections.sort(classes, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject o1, JSONObject o2) {
						String className1 = o1.getString("className");
						String className2 = o2.getString("className");
						return className1.compareTo(className2);
					}
				});
			}
			subject.put("classes", classes);
			subjects.add(subject);
		}

		return subjects;
	}

	@Override
	public void saveLayerSetterInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		// 层次数
		String classIdsStr = request.getString("classIds");
		String examId = request.getString("examId");
		Long subjectId = request.getLong("subjectId");

		Integer layerNum = request.getInteger("layerNum");

		JSONObject firstLayer = request.getJSONObject("firstLayer");
		JSONObject secondLayer = request.getJSONObject("secondLayer");
		JSONObject thirdLayer = null;
		if (layerNum == 3) {
			thirdLayer = request.getJSONObject("thirdLayer");
		}

		if (StringUtils.isBlank(classIdsStr)) {
			throw new CommonRunException(-1, "班级代码为空，参数传递错误，请联系管理员！");
		}
		if (StringUtils.isBlank(examId)) {
			throw new CommonRunException(-1, "考试轮次代码为空，参数传递错误，请联系管理员！");
		}
		if (subjectId == null) {
			throw new CommonRunException(-1, "科目代码为空，参数传递错误，请联系管理员！");
		}
		if (firstLayer == null || secondLayer == null) {
			throw new CommonRunException(-1, "无法获取分层数据，参数传递错误，请联系管理员！");
		}

		List<Long> subjectIds = new ArrayList<Long>();
		subjectIds.add(subjectId);
		List<LessonInfo> lessonInfos = commonDataService.getLessonInfoBatch(
				schoolId, subjectIds, termInfo);
		if (CollectionUtils.isEmpty(lessonInfos)) {
			throw new CommonRunException(-1, "无法从SDK中获取科目代码为 " + subjectId
					+ " 的科目信息，请联系管理员！");
		}
		final String subjectName = lessonInfos.get(0).getName();

		List<Long> classIds = StringUtil.convertToListFromStr(classIdsStr, ",",
				Long.class);

		// 插入规则信息
		String ruleId = UUIDUtil.getUUID();
		PlacementRule rule = new PlacementRule();
		rule.setTermInfo(termInfo);
		rule.setSchoolId(schoolId);
		rule.setRuleId(ruleId);
		rule.setPlacementId(placementId);
		rule.setExamId(examId);
		placementTaskDao.insertPlacementRule(rule);

		// 通过考试轮次获取分数
		List<String> subjIds = new ArrayList<String>(1);
		subjIds.add(String.valueOf(subjectId));
		List<JSONObject> scoreInfos = scoreManageAPIService.queryScoreInfo(examId, 
					String.valueOf(schoolId), "", subjIds, classIds);
		Map<Long, JSONObject> accId2ScoreDetail = new HashMap<Long, JSONObject>(
				scoreInfos.size());
		if (CollectionUtils.isEmpty(scoreInfos)) {
			throw new CommonRunException(-2, "选择考试成绩数据为空，请重新选择成绩！");
		}

		for (JSONObject scoreInfo : scoreInfos) {
			Long accId = scoreInfo.getLong("accountId");
			if (accId == null) {
				continue;
			}
			accId2ScoreDetail.put(accId, scoreInfo);
		}

		List<Classroom> classrooms = commonDataService.getClassroomBatch(
				schoolId, classIds, termInfo);
		if (CollectionUtils.isEmpty(classrooms)) {
			throw new CommonRunException(-1, "无法从SDK中获取班级代码为 " + classIdsStr
					+ " 的班级信息，请联系管理员！");
		}

		List<StudentInfo> studInfoList = new ArrayList<StudentInfo>();
		for (Classroom classroom : classrooms) {
			List<Long> accountIds = classroom.getStudentAccountIds();
			if (CollectionUtils.isEmpty(accountIds)) {
				continue;
			}
			long classId = classroom.getId();
			for (Long accId : accountIds) {
				StudentInfo studInfo = new StudentInfo();
				studInfo.setSchoolId(schoolId);
				studInfo.setPlacementId(placementId);
				studInfo.setClassId(classId);
				studInfo.setAccountId(accId);
				Float score = 0f;
				JSONObject scoreInfo = accId2ScoreDetail.get(accId);
				if (scoreInfo != null) {
					score = scoreInfo.getFloat("score");
				}
				studInfo.setSortField(score);
				studInfo.setTermInfo(termInfo);
				studInfoList.add(studInfo);
			}
		}

		Collections.sort(studInfoList, new Comparator<StudentInfo>() {
			@Override
			public int compare(StudentInfo o1, StudentInfo o2) {
				Float score1 = o1.getSortField();
				Float score2 = o2.getSortField();
				return -Float.compare(score1, score2);
			}
		});

		// 计算第一层次分数，不能简单将成绩乘以百分比，因为有种情况是分界当好是几个相同成绩之间，会导致相同的分数在不同层次
		Double firstValue = firstLayer.getDouble("value") / 100;
		int firstIdx = (int) (studInfoList.size() * firstValue);
		Float scoreUpLimit = studInfoList.get(firstIdx).getSortField();

		Float scoreDownLimit = 0f;
		if (thirdLayer != null && thirdLayer.size() > 0) {
			Double thirdValue = firstLayer.getDouble("value") / 100;
			int thirdIdx = (int) (studInfoList.size() * (1 - thirdValue));
			scoreDownLimit = studInfoList.get(thirdIdx).getSortField();
		}

		Integer indexNum = null;

		// 因为分层可能出现一个科目对应多个开班信息，分班的时候会出现同名的班级
		// 例如，一条开班信息是“物理：1班，2班，3班”；另一条开班信息是 “物理：4班，5班，6班”
		// 分班的时候会产生 物理A001班 物理A001班 这样两条相同的班级名称的数据，因此需要区分开班信息
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("schoolId", schoolId);
		params.put("placementId", placementId);
		params.put("placementType", pl.getPlacementType());
		params.put("termInfo", termInfo);
		params.put("type", 4);
		params.put("subjectIdsStr", String.valueOf(subjectId));

		List<OpenClassInfo> opClassInfoList = placementTaskDao
				.queryOpenClassInfo(params);
		if (CollectionUtils.isNotEmpty(opClassInfoList)) { // 如果拥有数据
			// 排序，按后面数字升序排序
			Collections.sort(opClassInfoList, new Comparator<OpenClassInfo>() {
				@Override
				public int compare(OpenClassInfo o1, OpenClassInfo o2) {
					String idxStr = o1.getZhName().substring(
							o1.getZhName().indexOf(subjectName)
									+ subjectName.length());
					int idx1;
					if (StringUtils.isEmpty(idxStr)) {
						idx1 = Integer.MAX_VALUE;
					} else {
						idx1 = Integer.parseInt(idxStr);
					}
					idxStr = o2.getZhName().substring(
							o2.getZhName().indexOf(subjectName)
									+ subjectName.length());
					int idx2;
					if (StringUtils.isEmpty(idxStr)) {
						idx2 = Integer.MAX_VALUE;
					} else {
						idx2 = Integer.parseInt(idxStr);
					}
					return Integer.compare(idx1, idx2);
				}
			});

			indexNum = 0;
			for (OpenClassInfo opClassInfo : opClassInfoList) {
				String zhName = opClassInfo.getZhName();
				String idxStr = zhName.substring(zhName.indexOf(subjectName)
						+ subjectName.length());
				if (StringUtils.isEmpty(idxStr)) {
					indexNum++;
					opClassInfo.setZhName(opClassInfo.getZhName() + indexNum);
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setTermInfo(termInfo);
					placementTaskDao.updateOpenClassInfo(opClassInfo);
					continue;
				}
				int idx = Integer.parseInt(idxStr);
				if (idx > indexNum) {
					indexNum = idx;
				}
			}
			indexNum++;
		}

		// 插入开班信息代码
		OpenClassInfo opClassInfo = new OpenClassInfo();
		String openClassInfoId = UUIDUtil.getUUID();
		opClassInfo.setOpenClassInfoId(openClassInfoId);
		opClassInfo.setSchoolId(schoolId);
		opClassInfo.setPlacementId(placementId);
		opClassInfo.setPlacementType(placementType);
		opClassInfo.setType(4); // 按成绩分层
		opClassInfo.setZhName(subjectName + (indexNum == null ? "" : indexNum));
		opClassInfo.setSubjectIdsStr(String.valueOf(subjectId));
		opClassInfo.setClassIdsStr(classIdsStr);
		opClassInfo.setScoreUpLimit(scoreUpLimit);
		opClassInfo.setScoreDownLimit(scoreDownLimit);
		opClassInfo.setTermInfo(termInfo);
		opClassInfo.setRuleId(ruleId);
		placementTaskDao.insertOpenClassInfo(opClassInfo);

		// 学生分层
		int firstLayerNumOfStuds = 0;
		int secondLayerNumOfStuds = 0;
		int thirdLayerNumOfStuds = 0;
		for (StudentInfo studInfo : studInfoList) {
			studInfo.setOpenClassInfoId(openClassInfoId);

			Float score = studInfo.getSortField();
			if (score >= scoreUpLimit) {
				firstLayerNumOfStuds++;
			} else if (score >= scoreDownLimit && score < scoreUpLimit) {
				// 如果只有两层，scoreDownLimit为0，数据会插入第二层
				secondLayerNumOfStuds++;
			} else {
				thirdLayerNumOfStuds++;
			}
		}

		OpenClassTask opClassTask = new OpenClassTask();
		opClassTask.setSchoolId(schoolId);
		opClassTask.setPlacementId(placementId);
		opClassTask.setOpenClassInfoId(openClassInfoId);
		opClassTask.setTermInfo(termInfo);

		opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
		opClassTask.setSubjectLevel(1);
		opClassTask.setLayName(firstLayer.getString("label"));
		opClassTask.setLayValue(firstLayer.getInteger("value"));
		opClassTask.setNumOfStuds(firstLayerNumOfStuds);
		placementTaskDao.insertOpenClassTask(opClassTask);

		opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
		opClassTask.setSubjectLevel(2);
		opClassTask.setLayName(secondLayer.getString("label"));
		opClassTask.setLayValue(secondLayer.getInteger("value"));
		opClassTask.setNumOfStuds(secondLayerNumOfStuds);
		placementTaskDao.insertOpenClassTask(opClassTask);

		if (thirdLayer != null && thirdLayer.size() > 0) {
			opClassTask.setOpenClassTaskId(UUIDUtil.getUUID());
			opClassTask.setSubjectLevel(3);
			opClassTask.setLayName(thirdLayer.getString("label"));
			opClassTask.setLayValue(thirdLayer.getInteger("value"));
			opClassTask.setNumOfStuds(thirdLayerNumOfStuds);
			placementTaskDao.insertOpenClassTask(opClassTask);
		}

		placementTaskDao.insertStudentInfoWaitForPlacementBatch(studInfoList,
				termInfo);
	}

	@Override
	public JSONObject queryLayerOpenClassInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为大走班类型！");
		}
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		request.put("placementType", placementType);

		request.put("type", 4); // 按成绩分层
		List<OpenClassInfo> openClassInfos = placementTaskDao
				.queryOpenClassInfo(request);

		Map<String, JSONObject> infoId2Obj = new HashMap<String, JSONObject>();
		Set<Long> classIdSet = new HashSet<Long>();
		for (OpenClassInfo opClassInfo : openClassInfos) {
			JSONObject json = new JSONObject();
			String openClassInfoId = opClassInfo.getOpenClassInfoId();
			infoId2Obj.put(openClassInfoId, json);

			List<Long> classIds = StringUtil.convertToListFromStr(
					opClassInfo.getClassIdsStr(), ",", Long.class);
			json.put("classIds", classIds);
			json.put("openClassInfoId", openClassInfoId);
			json.put("type", opClassInfo.getType());
			json.put("zhName", opClassInfo.getZhName());
			json.put("subjectId", opClassInfo.getSubjectIdsStr());
			classIdSet.addAll(classIds);
		}

		Integer totalNumOfStuds = 0;
		Map<Long, String> classId2Name = new HashMap<Long, String>();
		if (classIdSet.size() > 0) {
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					schoolId, new ArrayList<Long>(classIdSet), termInfo);
			if (CollectionUtils.isNotEmpty(classrooms)) {
				for (Classroom classroom : classrooms) {
					classId2Name.put(classroom.getId(),
							classroom.getClassName());
					totalNumOfStuds += classroom.getStudentAccountIdsSize();
				}
			}
		}

		if (openClassInfos.size() > 0) {
			request.put("openClassInfos", openClassInfos);
			List<OpenClassTask> openClassTasks = placementTaskDao
					.queryOpenClassTask(request);
			for (OpenClassTask opClassTask : openClassTasks) {
				String openClassInfoId = opClassTask.getOpenClassInfoId();
				if (!infoId2Obj.containsKey(openClassInfoId)) {
					continue;
				}
				JSONObject openClassInfo = infoId2Obj.get(openClassInfoId);
				if (!openClassInfo.containsKey("classNames")) {
					StringBuffer classNames = new StringBuffer("");
					List<Object> classIds = openClassInfo
							.getJSONArray("classIds");
					for (Object id : classIds) {
						classNames.append(classId2Name.get(id)).append(",");
					}
					classNames.deleteCharAt(classNames.length() - 1);
					openClassInfo.put("classNames", classNames.toString());
				}
				Integer type = openClassInfo.getInteger("type");
				if (type == 4) {
					Integer subjectLevel = opClassTask.getSubjectLevel();
					openClassInfo.put("openClassTaskId" + subjectLevel,
							opClassTask.getOpenClassTaskId());
					openClassInfo.put("numOfStuds" + subjectLevel,
							opClassTask.getNumOfStuds());
					openClassInfo.put("numOfOpenClasses" + subjectLevel,
							opClassTask.getNumOfOpenClasses());
					openClassInfo.put("classSize" + subjectLevel,
							opClassTask.getClassSize());
				}
			}
		}

		List<JSONObject> openClassData = new ArrayList<JSONObject>(
				infoId2Obj.values());
		if (openClassData.size() > 0) {
			for (JSONObject json : openClassData) {
				if (!json.containsKey("numOfStuds3")) { // 两层的不全三层
					json.put("numOfStuds3", "—");
					json.put("numOfOpenClasses3", "—");
					json.put("classSize3", "—");
					json.put("layerNum", 2);
				} else {
					json.put("layerNum", 3);
				}
			}

			Collections.sort(openClassData, new Comparator<JSONObject>() { // 按科目排序
						@Override
						public int compare(JSONObject o1, JSONObject o2) {
							Long subjectId1 = o1.getLong("subjectId");
							Long subjectId2 = o2.getLong("subjectId");
							int result = Long.compare(subjectId1, subjectId2);
							if (result != 0) {
								return result;
							}
							String zhName1 = o1.getString("zhName");
							String zhName2 = o2.getString("zhName");
							return zhName1.compareTo(zhName2);
						}
					});
		}

		JSONObject data = new JSONObject();
		data.put("layerStatus", pl.getLayerStatus());
		data.put("openClassData", openClassData);
		data.put("totalNumOfStuds", totalNumOfStuds);
		return data;
	}

	@Override
	public void saveLayerOpenClassInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");

		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null) {
			throw new CommonRunException(-1, "参数错误，请联系管理员！");
		}

		Boolean validate = request.getBoolean("validate");
		if (validate == null) {
			validate = true;
		}

		List<Object> openClassData = request.getJSONArray("openClassData");
		request.remove("openClassData");
		if (CollectionUtils.isEmpty(openClassData)) {
			throw new CommonRunException(-1, "获取存储数据为空，请联系管理员！");
		}

		Map<String, JSONObject> taskId2Obj = new HashMap<String, JSONObject>();
		for (Object object : openClassData) {
			JSONObject json = (JSONObject) object;
			String openClassTaskId = json.getString("openClassTaskId");
			taskId2Obj.put(openClassTaskId, json);
		}

		request.put("openClassTaskIds", taskId2Obj.keySet());
		List<OpenClassTask> openClassTasks = placementTaskDao
				.queryOpenClassTask(request);

		if (validate) {
			validateOpenClassData(request, openClassTasks, taskId2Obj);
		}

		for (OpenClassTask opClassTask : openClassTasks) {
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			JSONObject json = taskId2Obj.get(openClassTaskId);

			Integer numOfStuds = json.getInteger("numOfStuds");
			if (numOfStuds != null
					&& !numOfStuds.equals(opClassTask.getNumOfStuds())) {
				opClassTask.setNumOfStuds(numOfStuds);
				opClassTask.setStatus(0);
			}
			Integer numOfOpenClasses = json.getInteger("numOfOpenClasses");
			if (0 == numOfStuds) {
				numOfOpenClasses = 0;
			}
			if (numOfOpenClasses != null
					&& !numOfOpenClasses.equals(opClassTask
							.getNumOfOpenClasses())) {
				opClassTask.setNumOfOpenClasses(numOfOpenClasses);
				opClassTask.setStatus(0);
			}
			Integer classSize = json.getInteger("classSize");
			if (classSize != null
					&& !classSize.equals(opClassTask.getClassSize())) {
				opClassTask.setClassSize(classSize);
				opClassTask.setStatus(0);
			}

			if (opClassTask.getStatus() != null && 0 == opClassTask.getStatus()) {
				opClassTask.setPlacementId(placementId);
				opClassTask.setSchoolId(schoolId);
				opClassTask.setTermInfo(termInfo);
				placementTaskDao.updateOpenClassTask(opClassTask);
			}
		}

		// 更新分班任务主表
		if (pl.getLayerStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(pl.getStatus());
			placementTask.setLayerStatus(status);
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	private void validateOpenClassData(JSONObject request,
			List<OpenClassTask> openClassTaskList,
			Map<String, JSONObject> taskId2Obj) {
		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");

		Map<String, List<JSONObject>> infoId2Obj = new HashMap<String, List<JSONObject>>();
		for (OpenClassTask opClassTask : openClassTaskList) {
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			if (!infoId2Obj.containsKey(openClassInfoId)) {
				infoId2Obj.put(openClassInfoId, new ArrayList<JSONObject>());
			}

			String openClassTaskId = opClassTask.getOpenClassTaskId();
			JSONObject json = taskId2Obj.get(openClassTaskId);
			json.put("subjectLevel", opClassTask.getSubjectLevel());

			infoId2Obj.get(openClassInfoId).add(json);
		}

		StringBuffer strbuf = new StringBuffer();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		params.put("termInfo", termInfo);
		for (Map.Entry<String, List<JSONObject>> entry : infoId2Obj.entrySet()) {
			String openClassInfoId = entry.getKey();
			List<JSONObject> list = entry.getValue();
			Collections.sort(list, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					int subjectLevel1 = o1.getIntValue("subjectLevel");
					int subjectLevel2 = o2.getIntValue("subjectLevel");
					return Integer.compare(subjectLevel1, subjectLevel2);
				}
			});
			params.put("openClassInfoId", openClassInfoId);

			int skip = list.get(0).getIntValue("numOfStuds");
			params.put("skip", skip - 1);
			List<Float> validScore = placementTaskDao
					.validateScoreLayerLarge(params);
			if (validScore.size() == 2
					&& validScore.get(0).equals(validScore.get(1))) {
				strbuf.append(list.get(0).get("openClassTaskId")).append(",");
			}

			if (list.size() == 3) {
				skip += list.get(1).getIntValue("numOfStuds");
				params.put("skip", skip - 1);
				validScore = placementTaskDao.validateScoreLayerLarge(params);
				if (validScore.size() == 2
						&& validScore.get(0).equals(validScore.get(1))) {
					strbuf.append(list.get(1).get("openClassTaskId")).append(
							",");
				}
			}
		}
		if (strbuf.length() > 0) {
			strbuf.deleteCharAt(strbuf.length() - 1);
			throw new CommonRunException(-2, "有同成绩的学生分在不同层级，是否确定保存？");
		}
	}

	@Override
	public void deleteLayerOpenClassInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		request.put("placementType", placementType);
		request.put("type", 4);

		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		placementTaskDao.deleteStudentInfo(request);
		placementTaskDao.deleteTeachingClassInfo(request);
		placementTaskDao.deletePlacementRule(request);
		placementTaskDao.deleteOpenClassTask(request);
		placementTaskDao.deleteOpenClassInfo(request);
	}

	@Override
	public JSONObject queryWishSetterLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		String wfId = (String) request.remove("wfId");
		String wfTermInfo = (String) request.remove("wfTermInfo");
		if (StringUtils.isEmpty(wfId) || StringUtils.isEmpty(wfTermInfo)) {
			throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
		}

		Long schoolId = request.getLong("schoolId");

		request.put("placementType", placementType);
		// 如果传递的选课id和学年学期等于数据库中存储的选课id和学年学期，那么需要排除已经选中的科目
		if (wfId.equals(pl.getWfId()) && wfTermInfo.equals(pl.getWfTermInfo())) {
			request.put("types", new int[] { 3, 4 });
		} else {
			// 否则只需要排除已经按成绩分层的科目
			request.put("types", new int[] { 4 });
		}
		List<OpenClassInfo> openClassInfos = placementTaskDao
				.queryOpenClassInfo(request);
		Set<Long> subjectIdSet = new HashSet<Long>();
		for (OpenClassInfo opClassInfo : openClassInfos) {
			subjectIdSet.add(Long.valueOf(opClassInfo.getSubjectIdsStr()));
		}

		List<JSONObject> subjects = new ArrayList<JSONObject>();

		List<JSONObject> wfSubjectsData = wishFillingThirdService
				.getSubjectNumToThird(wfId, wfTermInfo, schoolId);
		Integer totalNumOfStuds = 0; // 选课总人数
		if (CollectionUtils.isNotEmpty(wfSubjectsData)) {
			for (JSONObject json : wfSubjectsData) {
				Long subjectId = json.getLong("subjectId");

				Integer numOfStuds = json.getInteger("studentNum");
				if (numOfStuds == null) {
					numOfStuds = 0;
				}
				totalNumOfStuds += numOfStuds;

				if (!subjectIdSet.contains(subjectId)) {
					json.remove("studentNum");
					json.put("numOfStuds", numOfStuds);
					subjects.add(json);
				}
			}
		}

		JSONObject data = new JSONObject();
		// 因为一个人只能选三门科目，因此计算总人数为所有科目人数之和除以3
		data.put("totalNumOfStuds", totalNumOfStuds / 3);
		data.put("subjects", subjects);
		return data;
	}

	@Override
	public void saveWishSetterLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		String wfId = (String) request.remove("wfId");
		String wfTermInfo = (String) request.remove("wfTermInfo");
		Integer totalNumOfStuds = request.getInteger("totalNumOfStuds");
		List<Object> subjects = request.getJSONArray("subjects");
		if (StringUtils.isEmpty(wfId) || StringUtils.isEmpty(wfTermInfo)
				|| totalNumOfStuds == null || CollectionUtils.isEmpty(subjects)) {
			throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
		}

		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");

		if (!wfId.equals(pl.getWfId())
				|| !wfTermInfo.equals(pl.getWfTermInfo())) {
			request.put("placementType", placementType);
			request.put("type", 3);
			List<OpenClassInfo> opClassInfoList = placementTaskDao
					.queryOpenClassInfo(request);
			if (opClassInfoList.size() > 0) {
				request.put("openClassInfos", opClassInfoList);
				placementTaskDao.deleteOpenClassInfo(request);
				placementTaskDao.deleteOpenClassTask(request);
				placementTaskDao.deleteTeachingClassInfo(request);
				placementTaskDao.deleteStudentInfo(request);
				placementTaskDao.deleteStudentInfoWaitForPlacement(request);
				request.remove("openClassInfos");
			}

			pl.setSchoolId(schoolId);
			pl.setPlacementId(placementId);
			pl.setTermInfo(termInfo);
			pl.setWfId(wfId);
			pl.setWfTermInfo(wfTermInfo);
			placementTaskDao.updatePlacementTaskById(pl);
		}

		List<OpenClassInfo> openClassInfos = new ArrayList<OpenClassInfo>();
		List<OpenClassTask> openClassTasks = new ArrayList<OpenClassTask>();
		for (Object object : subjects) {
			JSONObject json = (JSONObject) object;

			Integer numOfStuds = json.getInteger("numOfStuds");
			if (numOfStuds == null) {
				continue;
			}

			String openClassInfoId = UUIDUtil.getUUID();
			OpenClassInfo opClassInfo = new OpenClassInfo();
			opClassInfo.setOpenClassInfoId(openClassInfoId);
			opClassInfo.setSchoolId(schoolId);
			opClassInfo.setPlacementId(placementId);
			opClassInfo.setPlacementType(placementType);
			opClassInfo.setType(3); // 志愿
			opClassInfo.setZhName(json.getString("subjectName"));
			opClassInfo.setSubjectIdsStr(json.getString("subjectId"));
			opClassInfo.setClassIdsStr(""); // 默认classIds，大走班按成绩分层才有
			opClassInfo.setScoreUpLimit(0f); // 分数上线，大走班按成绩分层才有
			opClassInfo.setScoreDownLimit(0f); // 分数下线，大走班按成绩分层才有
			opClassInfo.setTermInfo(termInfo);
			openClassInfos.add(opClassInfo);

			// 高考
			OpenClassTask opClassTaskG = new OpenClassTask();
			opClassTaskG.setOpenClassTaskId(UUIDUtil.getUUID());
			opClassTaskG.setSchoolId(schoolId);
			opClassTaskG.setPlacementId(placementId);
			opClassTaskG.setOpenClassInfoId(openClassInfoId);
			opClassTaskG.setSubjectLevel(1); // 中走班组合，(选考)高考
			opClassTaskG.setLayName(""); // 大走班按成绩分层才有
			opClassTaskG.setLayValue(0); // 大走班按成绩分层才有
			opClassTaskG.setNumOfStuds(numOfStuds);
			opClassTaskG.setTermInfo(termInfo);
			openClassTasks.add(opClassTaskG);
			// 学考
			OpenClassTask opClassTaskX = new OpenClassTask();
			opClassTaskX.setOpenClassTaskId(UUIDUtil.getUUID());
			opClassTaskX.setSchoolId(schoolId);
			opClassTaskX.setPlacementId(placementId);
			opClassTaskX.setOpenClassInfoId(openClassInfoId);
			opClassTaskX.setSubjectLevel(2); // 中走班组合，学考
			opClassTaskX.setLayName(""); // 大走班按成绩分层才有
			opClassTaskX.setLayValue(0); // 大走班按成绩分层才有
			opClassTaskX.setNumOfStuds(totalNumOfStuds - numOfStuds);
			opClassTaskX.setTermInfo(termInfo);
			openClassTasks.add(opClassTaskX);
		}

		if (openClassInfos.size() > 0) {
			placementTaskDao.insertOpenClassInfoBatch(openClassInfos, termInfo);
		}
		if (openClassTasks.size() > 0) {
			placementTaskDao.insertOpenClassTaskBatch(openClassTasks, termInfo);
		}
	}

	@Override
	public JSONObject queryWishOpenClassInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		request.put("placementType", placementType);
		request.put("type", 3);

		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		PlacementRule rule = null;
		if (rules.size() > 0) {
			rule = rules.get(0);
		}

		Map<String, JSONObject> id2OpenClassInfo = new HashMap<String, JSONObject>();
		List<OpenClassInfo> openClassInfos = placementTaskDao
				.queryOpenClassInfo(request);
		for (OpenClassInfo opClassInfo : openClassInfos) {
			JSONObject json = new JSONObject();
			String openClassInfoId = opClassInfo.getOpenClassInfoId();
			json.put("openClassInfoId", openClassInfoId);
			json.put("zhName", opClassInfo.getZhName());
			json.put("subjectId", opClassInfo.getSubjectIdsStr());
			id2OpenClassInfo.put(openClassInfoId, json);
		}

		request.put("openClassInfoIds", id2OpenClassInfo.keySet());
		List<OpenClassTask> openClassTasks = placementTaskDao
				.queryOpenClassTask(request);
		List<JSONObject> openClassData = new ArrayList<JSONObject>();
		for (OpenClassTask opClassTask : openClassTasks) {
			String openClassInfoId = opClassTask.getOpenClassInfoId();
			JSONObject openClassInfo = id2OpenClassInfo.get(openClassInfoId);
			if (openClassInfo == null) {
				continue;
			}
			Integer subjectLevel = opClassTask.getSubjectLevel();
			if (subjectLevel == 1) { // 高考
				openClassInfo.put("openClassTaskIdG",
						opClassTask.getOpenClassTaskId());
				openClassInfo.put("numOfStudsG", opClassTask.getNumOfStuds());
				openClassInfo.put("numOfOpenClassesG",
						opClassTask.getNumOfOpenClasses());
				openClassInfo.put("classSizeG", opClassTask.getClassSize());

				openClassData.add(openClassInfo);
			} else if (subjectLevel == 2) { // 学考
				openClassInfo.put("openClassTaskIdX",
						opClassTask.getOpenClassTaskId());
				openClassInfo.put("numOfStudsX", opClassTask.getNumOfStuds());
				openClassInfo.put("numOfOpenClassesX",
						opClassTask.getNumOfOpenClasses());
				openClassInfo.put("classSizeX", opClassTask.getClassSize());
			}
		}

		Integer numOfStuds = 0;
		if (openClassData.size() > 0) {
			Collections.sort(openClassData, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					Long subjectId1 = o1.getLong("subjectId");
					Long subjectId2 = o2.getLong("subjectId");
					return Long.compare(subjectId1, subjectId2);
				}
			});
			JSONObject json = openClassData.get(0);
			numOfStuds = json.getInteger("numOfStudsG")
					+ json.getInteger("numOfStudsX");
		}

		for (JSONObject json : openClassData) {
			Integer numOfStudsG = json.getInteger("numOfStudsG");
			Integer numOfStudsX = json.getInteger("numOfStudsX");

			Integer numOfOpenClassesG = json.getInteger("numOfOpenClassesG");
			Integer numOfOpenClassesX = json.getInteger("numOfOpenClassesX");
			if (numOfOpenClassesG == null && numOfOpenClassesX == null) {
				json.put("numOfStudsPlaceG", numOfStudsG);
				json.put("numOfStudsPlaceX", numOfStudsX);
				continue;
			}
			if (numOfOpenClassesG != 0 && numOfOpenClassesX == 0) {
				json.put("numOfStudsPlaceG", numOfStudsG + numOfStudsX);
				json.put("numOfStudsPlaceX", 0);
			} else if (numOfOpenClassesG == 0 && numOfOpenClassesX != 0) {
				json.put("numOfStudsPlaceG", 0);
				json.put("numOfStudsPlaceX", numOfStudsG + numOfStudsX);
			} else if (numOfOpenClassesG == 0 && numOfOpenClassesX == 0) {
				json.put("numOfStudsPlaceG", 0);
				json.put("numOfStudsPlaceX", 0);
			} else {
				json.put("numOfStudsPlaceG", numOfStudsG);
				json.put("numOfStudsPlaceX", numOfStudsX);
			}
		}

		int totalNumOfStuds = 0;
		String termInfo = request.getString("termInfo");
		String xn = termInfo.substring(0, termInfo.length() - 1);
		Long schoolId = request.getLong("schoolId");
		String usedGrade = pl.getUsedGrade();
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer
				.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl,
				termInfo);
		if (grade == null) {
			throw new CommonRunException(-1, "无法获取年级信息，请联系管理员！");
		}
		List<Classroom> crs = commonDataService.getClassroomBatch(schoolId,
				grade.getClassIds(), termInfo);
		if (crs == null) {
			throw new CommonRunException(-1, "无法获取年级下的班级信息，请联系管理员！");
		}
		for (Classroom cr : crs) {
			totalNumOfStuds += cr.getStudentAccountIdsSize();
		}

		JSONObject data = new JSONObject();
		data.put("status", pl.getStatus());
		data.put("openClassData", openClassData);
		data.put("numOfStuds", numOfStuds);
		data.put("totalNumOfStuds", totalNumOfStuds);
		data.put("gradeName", njName.get(gl));
		if (rule != null) {
			data.put("examId", rule.getExamId());
			data.put("ruleCode", rule.getRuleCode());
		}
		return data;
	}

	@Override
	public void saveWishOpenClassInfoLarge(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}

		Integer status = request.getInteger("status");
		request.remove("status");
		if (status == null) {
			throw new CommonRunException(-1, "参数错误，请联系管理员！");
		}

		request.put("placementType", placementType);
		request.put("type", 3);
		JSONObject openClassData = request.getJSONObject("openClassData");
		request.remove("openClassData");

		Map<String, OpenClassTask> taskId2Obj = new HashMap<String, OpenClassTask>();
		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		for (OpenClassTask opClassTask : opClassTasksWithInfo) {
			String openClassTaskId = opClassTask.getOpenClassTaskId();
			taskId2Obj.put(openClassTaskId, opClassTask);
		}

		String ruleId = queryOrUpdateRule(request,
				openClassData.getString("examId"),
				openClassData.getString("examTermInfo"),
				openClassData.getInteger("ruleCode"));

		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String placementId = request.getString("placementId");

		List<Object> list = openClassData.getJSONArray("data");
		if (CollectionUtils.isNotEmpty(list)) {
			for (Object object : list) {
				JSONObject json = (JSONObject) object;
				String openClassTaskId = json.getString("openClassTaskId");
				OpenClassTask opClassTaskDb = taskId2Obj.get(openClassTaskId);
				if (opClassTaskDb == null) {
					continue;
				}
				Integer numOfStuds = json.getInteger("numOfStuds");
				Integer numOfOpenClasses = json.getInteger("numOfOpenClasses");
				Integer classSize = json.getInteger("classSize");

				OpenClassTask opClassTask = new OpenClassTask();
				opClassTask.setSchoolId(schoolId);
				opClassTask.setOpenClassTaskId(openClassTaskId);
				opClassTask.setTermInfo(termInfo);
				opClassTask.setPlacementId(placementId);

				if (!ruleId
						.equals(opClassTaskDb.getOpenClassInfo().getRuleId())) {
					OpenClassInfo opClassInfo = new OpenClassInfo();
					opClassInfo.setOpenClassInfoId(opClassTaskDb
							.getOpenClassInfoId());
					opClassInfo.setSchoolId(schoolId);
					opClassInfo.setTermInfo(termInfo);
					opClassInfo.setPlacementType(placementType);
					opClassInfo.setPlacementId(placementId);
					opClassInfo.setRuleId(ruleId);
					placementTaskDao.updateOpenClassInfo(opClassInfo);

					opClassTask.setStatus(0);
				}
				if (numOfStuds != opClassTaskDb.getNumOfStuds()) {
					opClassTask.setNumOfStuds(numOfStuds);
					opClassTask.setStatus(0);
				}
				if (numOfOpenClasses != opClassTaskDb.getNumOfOpenClasses()) {
					opClassTask.setNumOfOpenClasses(numOfOpenClasses);
					opClassTask.setStatus(0);
				}
				if (classSize != opClassTaskDb.getClassSize()) {
					opClassTask.setClassSize(classSize);
					opClassTask.setStatus(0);
				}
				if (opClassTask.getStatus() != null
						&& 0 == opClassTask.getStatus()) {
					placementTaskDao.updateOpenClassTask(opClassTask);
				}
			}
		}
		// 更新分班任务主表
		if (pl.getStatus() < status) {
			PlacementTask placementTask = new PlacementTask();
			placementTask.setPlacementId(placementId);
			placementTask.setSchoolId(schoolId);
			placementTask.setTermInfo(termInfo);
			placementTask.setStatus(status);
			placementTask.setLayerStatus(pl.getLayerStatus());
			placementTaskDao.updatePlacementTaskById(placementTask);
		}
	}

	@Override
	public void deleteWishOpenClassInfo(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		if (placementType != 3) {
			throw new CommonRunException(-1, "分班任务类型被改动，不为中走班类型！");
		}
		request.put("placementType", placementType);
		request.put("type", 3);

		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		placementTaskDao.deleteStudentInfo(request);
		placementTaskDao.deleteTeachingClassInfo(request);
		placementTaskDao.deletePlacementRule(request);
		placementTaskDao.deleteOpenClassTask(request);
		placementTaskDao.deleteOpenClassInfo(request);
	}

	@Override
	public JSONObject queryResultPreview(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		int placementType = pl.getPlacementType();
		request.put("placementType", placementType);

		JSONObject data = new JSONObject();
		data.put("status", pl.getStatus());
		data.put("layerStatus", pl.getLayerStatus());

		List<JSONObject> preview = new ArrayList<JSONObject>();
		data.put("preview", preview);
		if (!request.containsKey("openClassTaskIds")) {
//			return data;
		}

		List<TeachingClassInfo> tClassInfoList = placementTaskDao
				.queryTeachingClassInfoWithAll(request);
		for (TeachingClassInfo tClassInfo : tClassInfoList) {
			OpenClassTask opClassTask = tClassInfo.getOpenClassTask();
			OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
			JSONObject json = new JSONObject();
			json.put("zhName", opClassInfo.getZhName());
			json.put("tclassId", tClassInfo.getTeachingClassId());
			json.put("tclassName", tClassInfo.getTeachingClassName());
			json.put("teachingGroundId", tClassInfo.getTeachingClassId());
			json.put("teachingGround", tClassInfo.getTeachingClassName()+"教室");
			json.put("teachingClassId", tClassInfo.getTeachingClassId());
			json.put("teachingClassName", tClassInfo.getTeachingClassName());
			json.put("type", opClassInfo.getType());
			if (opClassInfo.getType() == 3 || opClassInfo.getType() == 4) {
				json.put("numOfBoys", "-");
				json.put("numOfGirls", "-");
				json.put("numOfTotal", "-");
			} else {
				json.put("numOfBoys", tClassInfo.getNumOfBoys());
				json.put("numOfGirls", tClassInfo.getNumOfGirls());
				json.put("stuNum", tClassInfo.getNumOfStuds());
				json.put("numOfTotal", tClassInfo.getNumOfStuds());
			}
			preview.add(json);
		}

		Collections.sort(preview, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int type1 = o1.getIntValue("type");
				int type2 = o2.getIntValue("type");
				int result = Integer.compare(type1, type2);
				if (result != 0) {
					return result;
				}
				return o1.getString("teachingClassName").compareTo(
						o2.getString("teachingClassName"));
			}
		});

		return data;
	}

	@Override
	public void updateTeachingClassName(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());

		List<Object> teachingClassdata = request
				.getJSONArray("teachingClassdata");
		if (CollectionUtils.isEmpty(teachingClassdata)) {
			return;
		}
		Set<String> tClassNameSet = new HashSet<String>();
		Set<String> tClassIdSet = new HashSet<String>();
		for (Object object : teachingClassdata) {
			JSONObject json = (JSONObject) object;
			String tClassName = json.getString("teachingClassName");
			if (tClassNameSet.contains(tClassName)) {
				throw new CommonRunException(-1, "相同科目教学班级有重复的班级名称，请检查数据！");
			}
			tClassNameSet.add(tClassName);
			tClassIdSet.add(json.getString("teachingClassId"));
		}

		String placementId = request.getString("placementId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");

		List<TeachingClassInfo> tClassInfoList = placementTaskDao
				.queryTeachingClassInfo(request);
		for (TeachingClassInfo tClassInfo : tClassInfoList) {
			String tClassId = tClassInfo.getTeachingClassId();
			if (tClassIdSet.contains(tClassId)) {
				continue;
			}

			if (tClassNameSet.contains(tClassInfo.getTeachingClassName())) {
				throw new CommonRunException(-1, "相同科目教学班级有重复的班级名称，请检查数据！");
			}
		}

		for (Object object : teachingClassdata) {
			JSONObject json = (JSONObject) object;
			json.put("placementId", placementId);
			json.put("schoolId", schoolId);
			json.put("termInfo", termInfo);
			placementTaskDao.updateTeachingClassInfo(json);
		}
	}

	@Override
	public JSONObject queryResultDetail(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());

		String studName = ((String) request.remove("studName")).trim();

		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");

		List<JSONObject> result = new ArrayList<JSONObject>();

		List<OpenClassTask> opClassTaskList = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		Map<String, OpenClassTask> opClassTaskId2Obj = new HashMap<String, OpenClassTask>();
		for (OpenClassTask opClassTask : opClassTaskList) {
			opClassTaskId2Obj
					.put(opClassTask.getOpenClassTaskId(), opClassTask);
		}

		List<TeachingClassInfo> tClassInfoList = placementTaskDao
				.queryTeachingClassInfo(request);
		Map<String, String> tClassInfoId2Name = new HashMap<String, String>();
		for (TeachingClassInfo tClassInfo : tClassInfoList) {
			tClassInfoId2Name.put(tClassInfo.getTeachingClassId(),
					tClassInfo.getTeachingClassName());
		}

		Set<Long> accIdSet = new HashSet<Long>();
		Set<Long> classIdSet = new HashSet<Long>();
		List<StudentInfo> studInfoList = placementTaskDao
				.queryStudentInfo(request);
		for (StudentInfo studInfo : studInfoList) {
			accIdSet.add(studInfo.getAccountId());
			classIdSet.add(studInfo.getClassId());

		}

		Map<Long, String> classId2Name = new HashMap<Long, String>();
		if (classIdSet.size() > 0) {
			List<Classroom> classrooms = commonDataService.getClassroomBatch(
					schoolId, new ArrayList<Long>(classIdSet), termInfo);
			if (CollectionUtils.isEmpty(classrooms)) {
				throw new CommonRunException(-1, "无法从数据库中获取班级信息，请联系管理员！");
			}
			for (Classroom cr : classrooms) {
				classId2Name.put(cr.getId(), cr.getClassName());
			}
		}

		Map<Long, String> accId2Name = new HashMap<Long, String>();
		if (accIdSet.size() > 0) {
			List<Account> accList = commonDataService.getAccountBatch(schoolId,
					new ArrayList<Long>(accIdSet), termInfo);
			if (CollectionUtils.isEmpty(accList)) {
				throw new CommonRunException(-1, "无法从数据库中获取学生信息，请联系管理员！");
			}
			for (Account acc : accList) {
				accId2Name.put(acc.getId(), acc.getName());
			}
		}

		for (StudentInfo studInfo : studInfoList) {
			long accId = studInfo.getAccountId();
			String name = accId2Name.get(accId);
			if (name == null) {
				continue;
			}
			// 过滤名字
			if (StringUtils.isNotEmpty(studName)
					&& name.indexOf(studName) == -1) {
				continue;
			}

			OpenClassTask opClassTask = opClassTaskId2Obj.get(studInfo
					.getOpenClassTaskId());
			String zhName = "已删除";
			int type = 0;
			if (opClassTask != null) {
				zhName = opClassTask.getOpenClassInfo().getZhName();
				type = opClassTask.getOpenClassInfo().getType();
			}

			String teachingClassName = tClassInfoId2Name.get(studInfo
					.getTeachingClassId());
			if (teachingClassName == null) {
				teachingClassName = "已删除";
			}
			String originalClassName = classId2Name.get(studInfo.getClassId());
			if (originalClassName == null) {
				originalClassName = "已删除";
			}
			JSONObject json = new JSONObject();
			json.put("zhName", zhName);
			json.put("type", type);
			json.put("teachingClassId", studInfo.getTeachingClassId());
			json.put("teachingClassName", teachingClassName);
			json.put("studName", name);
			json.put("originalClassName", originalClassName);
			result.add(json);
		}
		Collections.sort(result, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				int type1 = o1.getIntValue("type");
				int type2 = o2.getIntValue("type");
				int result = Integer.compare(type1, type2);
				if (result != 0) {
					return result;
				}
				String tClassName1 = o1.getString("teachingClassName");
				String tClassName2 = o2.getString("teachingClassName");
				return tClassName1.compareTo(tClassName2);
			}
		});
		JSONObject data = new JSONObject();
		data.put("result", result);
		return data;
	}

	@Override
	public JSONObject queryStudenInfoWaitForPlacement(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		String openClassTaskId = (String) request.remove("openClassTaskId");
		request.put("openClassTaskIds", new Object[] { "", openClassTaskId });

		request.put("placementType", pl.getPlacementType());

		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");

		String studName = null;
		if (request.containsKey("studName")) {
			studName = ((String) request.remove("studName")).trim();
		}

		List<OpenClassTask> opClassTaskList = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		Map<String, String> opClassTaskId2ZhName = new HashMap<String, String>();
		for (OpenClassTask opClassTask : opClassTaskList) {
			opClassTaskId2ZhName.put(opClassTask.getOpenClassTaskId(),
					opClassTask.getOpenClassInfo().getZhName());
		}

		Set<Long> accIdSet = new HashSet<Long>();
		List<StudentInfo> studInfoList = placementTaskDao
				.queryStudentInfoWaitForPlacement(request);
		for (StudentInfo studInfo : studInfoList) {
			accIdSet.add(studInfo.getAccountId());
		}

		Map<Long, Account> accId2Acc = new HashMap<Long, Account>();
		if (accIdSet.size() > 0) {
			List<Account> accList = commonDataService.getAccountBatch(schoolId,
					new ArrayList<Long>(accIdSet), termInfo);
			if (CollectionUtils.isEmpty(accList)) {
				throw new CommonRunException(-1, "无法从数据库中获取学生信息，请联系管理员！");
			}
			for (Account acc : accList) {
				accId2Acc.put(acc.getId(), acc);
			}
		}

		List<JSONObject> result = new ArrayList<JSONObject>();
		for (StudentInfo studInfo : studInfoList) {
			long accId = studInfo.getAccountId();
			Account acc = accId2Acc.get(accId);
			String name = acc.getName();
			if (name == null) {
				continue;
			}
			// 过滤名字
			if (StringUtils.isNotEmpty(studName)
					&& name.indexOf(studName) == -1) {
				continue;
			}

			String zhName = opClassTaskId2ZhName.get(studInfo
					.getOpenClassTaskId());
			if (zhName == null) {
				zhName = "";
			}
			JSONObject json = new JSONObject();
			json.put("openClassTaskId", studInfo.getOpenClassTaskId());
			json.put("accountId", accId);
			if (T_Gender.Female.equals(acc.getGender())) {
				json.put("gender", "女");
			} else if (T_Gender.Male.equals(acc.getGender())) {
				json.put("gender", "男");
			}
			json.put("zhName", zhName);
			json.put("studName", name);
			result.add(json);
		}

		JSONObject data = new JSONObject();
		data.put("result", result);
		return data;
	}

	@Override
	public JSONObject queryStudenInfo(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());

		String termInfo = request.getString("termInfo");
		Long schoolId = request.getLong("schoolId");

		String studName = null;
		if (request.containsKey("studName")) {
			studName = ((String) request.remove("studName")).trim();
		}

		List<OpenClassTask> opClassTaskList = placementTaskDao
				.queryOpenClassTasksWithInfo(request);
		Map<String, String> opClassTaskId2ZhName = new HashMap<String, String>();
		for (OpenClassTask opClassTask : opClassTaskList) {
			opClassTaskId2ZhName.put(opClassTask.getOpenClassTaskId(),
					opClassTask.getOpenClassInfo().getZhName());
		}

		Set<Long> accIdSet = new HashSet<Long>();
		Set<String> tClassInfoIds = new HashSet<String>();
		List<StudentInfo> studInfoList = placementTaskDao
				.queryStudentInfo(request);
		for (StudentInfo studInfo : studInfoList) {
			accIdSet.add(studInfo.getAccountId());
			tClassInfoIds.add(studInfo.getTeachingClassId());
		}

		Map<Long, Account> accId2Acc = new HashMap<Long, Account>();
		if (accIdSet.size() > 0) {
			List<Account> accList = commonDataService.getAccountBatch(schoolId,
					new ArrayList<Long>(accIdSet), termInfo);
			if (CollectionUtils.isEmpty(accList)) {
				throw new CommonRunException(-1, "无法从数据库中获取学生信息，请联系管理员！");
			}
			for (Account acc : accList) {
				accId2Acc.put(acc.getId(), acc);
			}
		}

		Map<String, String> tClassInfoId2Name = new HashMap<String, String>();
		if (tClassInfoIds.size() > 0) {
			List<TeachingClassInfo> tClassInfoList = placementTaskDao
					.queryTeachingClassInfo(request);
			for (TeachingClassInfo tClassInfo : tClassInfoList) {
				tClassInfoId2Name.put(tClassInfo.getTeachingClassId(),
						tClassInfo.getTeachingClassName());
			}
		}

		List<JSONObject> result = new ArrayList<JSONObject>();
		for (StudentInfo studInfo : studInfoList) {
			long accId = studInfo.getAccountId();
			Account acc = accId2Acc.get(accId);
			if (acc == null) {
				continue;
			}

			String name = acc.getName();
			if (name == null) {
				continue;
			}
			// 过滤名字
			if (StringUtils.isNotEmpty(studName)
					&& name.indexOf(studName) == -1) {
				continue;
			}

			String zhName = opClassTaskId2ZhName.get(studInfo
					.getOpenClassTaskId());
			if (zhName == null) {
				zhName = "已删除";
			}
			String tClassInfoName = tClassInfoId2Name.get(studInfo
					.getTeachingClassId());
			if (tClassInfoName == null) {
				tClassInfoName = "已删除";
			}
			JSONObject json = new JSONObject();
			json.put("teachingClassId", studInfo.getTeachingClassId());
			json.put("accountId", accId);
			if (T_Gender.Female.equals(acc.getGender())) {
				json.put("gender", "女");
			} else if (T_Gender.Male.equals(acc.getGender())) {
				json.put("gender", "男");
			}
			json.put("zhName", zhName);
			json.put("studName", name);
			json.put("teachingClassName", tClassInfoName);
			result.add(json);
		}

		JSONObject data = new JSONObject();
		data.put("result", result);
		return data;
	}

	@Override
	public void modifyStudenInfoToWaitForPlacement(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());

		String termInfo = request.getString("termInfo");

		List<StudentInfo> studInfoList = placementTaskDao
				.queryStudentInfo(request);
		if (studInfoList.size() > 0) {
			placementTaskDao.deleteStudentInfo(request);
			placementTaskDao.insertStudentInfoWaitForPlacementBatch(
					studInfoList, termInfo);
		}
	}

	@Override
	public void modifyStudenInfoToPlacement(JSONObject request) {
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
		if (pl == null) {
			throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
		}
		request.put("placementType", pl.getPlacementType());
		String termInfo = request.getString("termInfo");

		String openClassTaskId = (String) request.remove("openClassTaskId");
		String teachingClassId = (String) request.remove("teachingClassId");

		request.put("openClassTaskIds", new Object[] { "", openClassTaskId });
		List<StudentInfo> studInfoList = placementTaskDao
				.queryStudentInfoWaitForPlacement(request);
		if (studInfoList.size() > 0) {
			for (StudentInfo studInfo : studInfoList) {
				studInfo.setOpenClassTaskId(openClassTaskId);
				studInfo.setTeachingClassId(teachingClassId);
				studInfo.setType(2);
			}
			placementTaskDao.deleteStudentInfoWaitForPlacement(request);
			placementTaskDao.insertStudentInfoBatch(studInfoList, termInfo);
		}
	}

	@Override
	public void updateAllDezyResult(String schoolId, String placementId,
			String termInfo) throws Exception {
		try {

			HashMap<String, Object> cxmap = new HashMap<String, Object>();
			cxmap.put("schoolId", schoolId);
			cxmap.put("placementId", placementId);
			cxmap.put("termInfo", termInfo);

			placementTaskDao.updateDezyResult(cxmap);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommonRunException(-3, "清空上次数据出错！");
		}
	}


	/**
	 * 长郡滨江版本分班
	 * @param settingParams
	 * @return
	 * @throws Exception 
	 */
	private HashMap<String, Object> genLargeDivTclass(
			Map<String, Object> settingParams) throws Exception {
		
		logger.info("------------------开始进入大走班-分班算法-----------------------------");
		System.out.println("------------------开始进入大走班-分班算法-----------------------------");
		// TODO Auto-generated method stub
		List<JSONObject> zhData = (List<JSONObject>) settingParams
				.get("wishings");
		List<JSONObject> stuentZh = (List<JSONObject>) settingParams
				.get("stuentZh");
		logger.info("------------------学生志愿-----------------------------,{}",stuentZh);
		JSONObject reqParams = (JSONObject) settingParams.get("reqParams");

		String xxdm  = reqParams.getString("schoolId");
		String termInfo  = reqParams.getString("termInfo");
		
		Long schoolId = Long.valueOf(xxdm);
		int isTechExist = reqParams.getIntValue("isTechExist");
		int maxClassNum = reqParams.getIntValue("maxClassNum");

		List<TPlDezySubjectSet> subsetList = (List<TPlDezySubjectSet>) reqParams.get("subsetList");
		logger.info("------------------科目设置-----------------------------,{}",subsetList);

		// 选考课时最大
		int maxOpt = 0;
		// 学考课时最大
		int maxPro = 0;
		int minPro = 0;
		//学考之和
		int allProSum = 0;
		List<String> optLessons = new ArrayList<String>();
		List<String> proLessons = new ArrayList<String>();

		HashMap<String, TPlDezySubjectSet> subjectSetMap = new HashMap<String, TPlDezySubjectSet>();
		//记录学选课时差
		List<JSONObject> subjectXxc = new ArrayList<JSONObject>();
		for (TPlDezySubjectSet ss : subsetList) {
			optLessons.add(ss.getSubjectId());
			if (ss.getIsProExist() > 0) {
				proLessons.add(ss.getSubjectId());
			}
			if (ss.getOptLesson() > maxOpt) {
				maxOpt = ss.getOptLesson();
			}
			if (ss.getProLesson() > maxPro) {
				maxPro = ss.getProLesson();
			}
			if(minPro==0||ss.getProLesson()<minPro){
				minPro = ss.getProLesson();
			}
			allProSum +=  ss.getProLesson();
			JSONObject xxc = new JSONObject();
			xxc.put("sid", ss.getSubjectId());
			xxc.put("cz", Math.abs(ss.getOptLesson()-ss.getProLesson()));
			subjectXxc.add(xxc);
			subjectSetMap.put(ss.getSubjectId(), ss);
		}

		int optGroupNum = 0;
		if (optLessons.size() >= 3) {
			optGroupNum = 3;
		} else {
			optGroupNum = optLessons.size();
		}
		int proGroupNum = 0;
		if (proLessons.size() >= 3 && isTechExist == 1) {
			proGroupNum = 4;
		} else if (proLessons.size() >= 3 && isTechExist == -1) {
			proGroupNum = 3;
		} else if (proLessons.size() < 3) {
			proGroupNum = proLessons.size();
		}
		
		SortUtil.sortListByTime(subjectXxc, "cz", "desc", "", "");
		boolean is5X3 = false;
		List<String> top3 = new ArrayList<String>();
		//学选差值最高的任意三门之和
		int maxCzSum = 0;
		for(int i=0;i<3;i++){
			JSONObject xxc = subjectXxc.get(i);
			String sid = xxc.getString("sid");
			int cz = xxc.getIntValue("cz");
			top3.add(sid);
			
			if(i==2&&is5X3){
				//5选3没有政史地组合 去除该最大值
				if(top3.contains("4")&&top3.contains("5")&&top3.contains("6")){
					xxc = subjectXxc.get(3);
					cz = xxc.getIntValue("cz");
				}
			}
			maxCzSum += cz;
		}
		
		int totalTaskNum = reqParams.getIntValue("gradeSumLesson");
		int fixedTaskNum = reqParams.getIntValue("fixedSumLesson");
		int maxCrossTaskNum = optGroupNum * maxOpt + proGroupNum * maxPro;
		int crossTaskNum = totalTaskNum - fixedTaskNum;
		if (crossTaskNum < optGroupNum * maxOpt
				|| crossTaskNum < proGroupNum * maxPro || crossTaskNum < (allProSum+maxCzSum)) {
			throw new CommonRunException(-1, "走班课时设置不足！");
		}
		// 需要走班的课时与可走班课时差
		int crossCz = maxCrossTaskNum - crossTaskNum;
			
		
		List<TPlDzbClassLevel> classLevelList = (List<TPlDzbClassLevel>) reqParams.get("classLevelList");
		List<Long> allXzclasses = new ArrayList<Long>() ;
		for(TPlDzbClassLevel tcl:classLevelList){			
			allXzclasses.add(Long.valueOf(tcl.getClassId()));
		}
		List<Classroom> allXzclassEntites = commonDataService.getSimpleClassBatch(schoolId, allXzclasses, termInfo);
		//所有行政班Id
		Set<String> classIds = new HashSet();
		for(Classroom cr:allXzclassEntites){
			classIds.add(cr.id+"");
		}
		
		
		//生成教学班
		Map<Integer, Integer> optSubjectLessonsMap =  
				(Map<Integer, Integer>) settingParams.get("optSubjectLessonsMap");
		Map<Integer, Integer> proSubjectLessonsMap  =  
				(Map<Integer, Integer>) settingParams.get("proSubjectLessonsMap");		
		JSONObject divResult = divMainMethod(maxClassNum - 5,
				maxClassNum,
				optSubjectLessonsMap, 
				proSubjectLessonsMap,
				zhData,
				stuentZh,
				classIds);
		JSONArray allTclasses = divResult.getJSONArray("allTclasses");	
		
		
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("allTclasses", allTclasses);
		result.put("allXzclassEntites", allXzclassEntites);
		//result.put("largeDisParams", params);
		result.put("crossCz", crossCz);
		result.put("needCross", crossCz>0);
		result.put("subcompFromList", divResult.get("subcompFromList"));
		result.put("confIndexSubsList", divResult.get("confIndexSubsList"));
		return result;
	}
	
	/**
	 * 长郡滨江分班算法入口
	 * @param args
	 * @throws Exception 
	 */
	private JSONObject divMainMethod(int avgClassStudentCount,
			int maxClassStudentCount ,			
			Map<Integer, Integer> optSubjectLessonsMap,
			Map<Integer, Integer> proSubjectLessonsMap,
			List<JSONObject> wishingList,
			List<JSONObject> stuentZh,
			Set<String> classIds) throws Exception{
		JSONObject result = new JSONObject();
		List<JSONObject> allTclasses = new ArrayList<JSONObject>();
		Map<Integer,String> subjectIdMap = new HashMap<Integer, String>();
		subjectIdMap.put(4, "政");
		subjectIdMap.put(5, "史");
		subjectIdMap.put(6, "地");
		subjectIdMap.put(7, "物");
		subjectIdMap.put(8, "化");
		subjectIdMap.put(9, "生");
		subjectIdMap.put(19, "技");
		//组合志愿数组
		int wishingKinds = wishingList.size();
		Map<String,String> wishingIdNameMap = new HashMap<String,String>();
		for(int n=0; n < wishingKinds; n++){
			JSONObject wishing = wishingList.get(n);
			String zhName = wishing.getString("zhName");
			Integer stuNum = wishing.getInteger("studentNum");
			wishingIdNameMap.put(wishing.getString("subjectIds"), zhName);
		}
		
		SelectType selectType  = SelectType.ThreeFromSix;
		if(optSubjectLessonsMap.size()==7){
			selectType  = SelectType.ThreeFromSeven;
		}
		
		//形成初始的分班结果
		NewDZBPlacementExcuter.PlacementResult placementResult = 
				NewDZBPlacementExcuter.divClass(avgClassStudentCount ,
						maxClassStudentCount , 
						optSubjectLessonsMap, 
						proSubjectLessonsMap, 
						Student.getStudentsMap(stuentZh),classIds,selectType);

		//生成辅助教学班名称
		Map<String,String> allExtGroundIdNameMap = new HashMap();
		int i=1;
		for(String groundId:placementResult.allExtGroundIds){
			allExtGroundIdNameMap.put(groundId, "辅助教室"+i);
			i++;
		}
		//生成教学班详情对应的志愿Id
		List<Tclass> tclassList = placementResult.tclassList;
		
		List<TPlDezySubjectcompStudent> subcompFromList = new ArrayList<TPlDezySubjectcompStudent>();
		Map<Tclass,JSONObject> tclassBeanJSONMap = new HashMap();
		for(Tclass tclass : tclassList){
			JSONObject tclassDetail = new JSONObject();
			tclassDetail.put("tclassLevel", tclass.isOpt==1?1:2);
			tclassDetail.put("subjectId", tclass.subjectId);
			tclassDetail.put("subLevel", 1);
			tclassDetail.put("tclassNum", tclass.studentCount);
			tclassDetail.put("classSeq", tclass.seq);
			tclassDetail.put("tclassId", tclass.tclassId);
			tclassDetail.put("groundId", tclass.groundId);
			tclassDetail.put("groundName",allExtGroundIdNameMap.get(tclass.groundId));
			tclassDetail.put("subjectName",subjectIdMap.get(tclass.subjectId));
			JSONArray tclassSubFrom = new JSONArray();
			tclassDetail.put("tclassSubFrom", tclassSubFrom);
			allTclasses.add(tclassDetail);
			tclassBeanJSONMap.put(tclass, tclassDetail);
		}
			
		//组装小志愿
		int totalStudent = 0;
		for (TclassEqualSubjectCombination tclassEqualSubjectCombination : placementResult.tclassEqualSubjectCombinations){
			JSONObject combination = new JSONObject();
			combination.put("subjectIds", tclassEqualSubjectCombination.subjectIdsStr);
			combination.put("studentNum", tclassEqualSubjectCombination.students.size());
			combination.put("zhName", wishingIdNameMap.get(tclassEqualSubjectCombination.subjectIdsStr));
			combination.put("zhUUID", tclassEqualSubjectCombination.subjectCombinationId);
			//学生进志愿
			for(Student student:tclassEqualSubjectCombination.students){
				TPlDezySubjectcompStudent subjectCompStudent = new TPlDezySubjectcompStudent();
				subjectCompStudent.setPlacementId(null);
				subjectCompStudent.setSchoolId(null);
				subjectCompStudent.setUsedGrade(null);
				subjectCompStudent.setStudentId(student.accountId);
				subjectCompStudent.setSubjectCompId(tclassEqualSubjectCombination.subjectCombinationId);
				subcompFromList.add(subjectCompStudent);
			}
			//班级关联志愿
			for(Tclass tclass:tclassEqualSubjectCombination.tclassList){
				JSONObject tclassJSON= tclassBeanJSONMap.get(tclass);
				List tclassSubFrom = tclassJSON.getObject("tclassSubFrom", List.class);
				if(tclassSubFrom ==null){
					tclassSubFrom = new ArrayList();
					tclassJSON.put("tclassSubFrom", tclassSubFrom);
				}
				tclassSubFrom.add(combination);
			}
		}
		result.put("allTclasses", allTclasses);
		
		//生成冲突详情
		List<Conflict> conflictList =  placementResult.conflictList;
		System.out.println(conflictList);
		List<TPlConfIndexSubs> confIndexSubList = new ArrayList<TPlConfIndexSubs>();
		int CD_OPT = 1;
		int CD_PRO = 0;
		Map<Integer, Integer> tbCdMap = new HashMap();
		tbCdMap.put(CD_OPT, 1);
		tbCdMap.put(CD_PRO, 2);
		int idx = 0;
		for(Conflict conflict : conflictList){
			//学选（0:1）
			for(int isOpt:new Integer[]{CD_OPT,CD_PRO}){
				List<Integer> seqs = conflict.getSeqs()[isOpt];
				for(int seqIndex = 0; seqIndex<3; seqIndex++){
					List<Integer> subjectIds = conflict.getSubjectIds()[isOpt];
					if(seqs.get(seqIndex) == -1){
						continue;
					}
					StringBuffer subIds = new StringBuffer();
					for(Integer subjectId : subjectIds){
						subIds.append(subjectId);
						subIds.append(",");
					}
					TPlConfIndexSubs confIndexSub = new TPlConfIndexSubs();
					confIndexSub.setIdx(idx);
					confIndexSub.setIsIdxRebuild(0);
					confIndexSub.setReIdx(0);
					confIndexSub.setIsOpt(tbCdMap.get(isOpt));
					confIndexSub.setSchoolId(null);
					confIndexSub.setUsedGrade(null);
					confIndexSub.setPlacementId(null);
					confIndexSub.setSeq(seqIndex+1);
					int isSeqRebuild = (seqIndex+1)==seqs.get(seqIndex)?0:1;
					confIndexSub.setIsSeqRebuild(isSeqRebuild);
					confIndexSub.setReSeq(seqs.get(seqIndex));
					confIndexSub.setSubIds(subIds.substring(0,subIds.length()-1));
					confIndexSubList.add(confIndexSub);
				}
			}
			idx++;
		}
		result.put("confIndexSubsList", confIndexSubList);
		result.put("subcompFromList", subcompFromList);
		return result;
	}

	private List<String> getRemovedArr(List<String> crPros, List<String> needRemove) {
		// TODO Auto-generated method stub
		List<String> arrs = new ArrayList<String>();
		for(String cr:crPros){
			if(!needRemove.contains(cr)){
				arrs.add(cr);
			}
		}
		return arrs;
	}

	public boolean isCrossAble(List<String> crOpts, List<String> crPros) {
		List<String> tempCrAll = new ArrayList<String>();
		tempCrAll.addAll(crOpts);
		for(String cr:crPros){
			if(!tempCrAll.contains(cr)){
				tempCrAll.add(cr);
			}
		}
		if(tempCrAll.size()>3){
			return false;
		}
		return true;
	}

	// 获取技术课Id
	private String getTechId(JSONObject obj) {
		Map<String, String> subMap = getSubjectNameMap(obj, true);
		for (String key : subMap.keySet()) {
			if (subMap.get(key).equals("技")) {
				return key;
			}
		}
		return null;
	}

	/**
	 * 获取科目Id与科目名称映射
	 * 
	 * @param obj
	 *            {keys:schoolId}, simple:全名
	 * @return
	 */
	private Map<String, String> getSubjectNameMap(JSONObject obj,
			boolean... simple) {
		Long schoolId = obj.getLong("schoolId");

		String termInfoId = obj.getString("termInfo");
		if (null == termInfoId) {
			termInfoId = commonDataService.getCurTermInfoId(schoolId);
		}
		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		List<LessonInfo> lessonInfoList = commonDataService.getLessonInfoList(
				school, termInfoId);
		Map<String, String> courseMap = new HashMap<String, String>();
		for (LessonInfo lessonInfo : lessonInfoList) {
			String subjectName = (simple.length > 0 && true == simple[0]) ? lessonInfo
					.getSimpleName() : lessonInfo.getName();

			courseMap.put(String.valueOf(lessonInfo.getId()), subjectName);
		}
		return courseMap;
	}

	@Override
	public List<String> getAllUsedWfId(String termInfo) {
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("termInfo", termInfo);
			return placementTaskDao.selectUsedWfId(params);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("查询分班使用的志愿出错！");
		}
		
		return null;
	}

	@Override
	public JSONObject startLargeDivClass(JSONObject obj) {
		// TODO Auto-generated method stub
		String wfId = obj.getString("wfId");
		String wfTermInfo = obj.getString("termInfo");
		Long schoolId = obj.getLong("schoolId");
		String usedGrade = obj.getString("usedGrade");	
		PlacementTask pl = placementTaskDao.queryPlacementTaskById(obj);
		if (StringUtils.isEmpty(usedGrade)) {
			usedGrade = pl.getUsedGrade();
		}
		//int maxStuInClass = obj.getIntValue("maxStuInClass");
		//int avgStuInClass = maxStuInClass - 5;
		JSONObject subjectGroup = obj.getJSONObject("subjectGroup");
	
		//学选课时安排
		Map<Integer,Integer> optSubjectLessonsMap = new HashMap<Integer,Integer>();
		Map<Integer,Integer> proSubjectLessonsMap = new HashMap<Integer,Integer>();
		if(null!=subjectGroup){
			JSONArray subject = subjectGroup.getJSONArray("subject");
			for(Object object : subject){
				JSONObject subLessonMap = (JSONObject)object;
				optSubjectLessonsMap.put(subLessonMap.getInteger("subjectId"), subLessonMap.getInteger("optLesson"));
				proSubjectLessonsMap.put(subLessonMap.getInteger("subjectId"), subLessonMap.getInteger("proLesson"));
			}
		}
		
		//分班类型（1:2）
		int plaType = pl.getPlacementType();
		obj.put("plaType", plaType);
		obj.put("usedGrade", usedGrade);
		Map<String, Object> settingParams = new HashMap<String, Object>();
		// wishFillingThirdService.get
		// 获取学生志愿及人数
		final String placementId = obj.getString("placementId");
		String termInfo = obj.getString("termInfo");
		try {
			// int rePlacement = obj.getInteger("rePlacement");
			// 不重新分班,获取最新一次的数据
			dezyPlacementTaskService.setDivProc(placementId, "5", "正在准备分班数据...", 0 );

			List<JSONObject> zhData = wishFillingThirdService
					.getZhStudentNumToThird(wfId, wfTermInfo, schoolId);
			List<JSONObject> stuentZh = wishFillingThirdService
					.getZhStudentListToThird(wfId, wfTermInfo, schoolId);
			
			if(plaType==2){
				JSONObject req = new JSONObject();
				req.put("placementId", placementId);
				req.put("termInfo", pl.getTermInfo());
				req.put("wfId", pl.getWfId());
				req.put("wfTermInfo", pl.getWfTermInfo());
				req.put("schoolId", schoolId);
				JSONObject cxRs = queryZhDataMedium(req);
				List<String> zhIds = new ArrayList<String>();
				if(cxRs.getInteger("status")==100){
//					JSONObject cxRsData = (JSONObject) cxRs.get("zhData");
					List<JSONObject> zhsList = (List<JSONObject>) cxRs.get("zhData");
					zhsList = GrepUtil.grepJsonKeyBySingleVal("selected", "1", zhsList);
					for(JSONObject zhs:zhsList){
						zhIds.add(zhs.getString("subjectIds"));
					}
				}
				
				List<JSONObject> needRemove1 = new ArrayList<JSONObject>();
								
				for(JSONObject zh:zhData){
					String subjectIds = zh.getString("subjectIds");
					if(zhIds.contains(subjectIds)){
						needRemove1.add(zh);
					}				
				}
				
				zhData.removeAll(needRemove1);
				List<JSONObject> needRemove2 = new ArrayList<JSONObject>();
				for(JSONObject zh:stuentZh){
					String subjectIds = zh.getString("subjectIds");
					if(zhIds.contains(subjectIds)){
						needRemove2.add(zh);
					}
				}
				stuentZh.removeAll(needRemove2);
			}
			settingParams.put("wishings", zhData);
			settingParams.put("stuentZh", stuentZh);
			settingParams.put("reqParams", obj);
			
			settingParams.put("optSubjectLessonsMap", optSubjectLessonsMap);
			settingParams.put("proSubjectLessonsMap", proSubjectLessonsMap);

			if(plaType==2){
				
				dezyPlacementTaskService.setDivProc(placementId, "10","20", "正在进行分班...", 0 );
				//分微走班部分
//				PlacementProcess proc = new PlacementProcess(obj, "null", transactionManager, placementTaskDao, scoreManageAPIService, wishFillingThirdService, commonDataService, redisOperationDAO);
//				proc.execPlacementMedium(pl);
				
			}else{
				
				dezyPlacementTaskService.setDivProc(placementId, "20","70", "正在进行分班...", 0 );
			}

			short isTechExist = -1;
			int totalStuInClass = 0;
			String techId = getTechId(obj);
			Map<String, String> kmMcMap = getSubjectNameMap(obj, true);
			
			for (JSONObject wishing : zhData) {
				String subjectIds = wishing.getString("subjectIds");
				totalStuInClass += wishing.getIntValue("studentNum");
				if (techId != null && subjectIds.contains(techId)) {// 判断是否包含技术选科
					isTechExist = 1;
					break;
				}
			}

			obj.put("isTechExist", isTechExist);

			// 设置班级阈值(及算法班级容量上下限参数)
			Integer classNum = obj.getIntValue("classNum");
			//Integer maxClassNum = obj.getIntValue("maxClassNum");
			//int classRoundNum = obj.getIntValue("classRoundNum") > 2 ? obj.getIntValue("classRoundNum") : 3;
			if (null == classNum || classNum == 0) {
				classNum = 1;
			}
			
			try{
				
				HashMap<String, Object> disRs = genLargeDivTclass(settingParams);//genLargeDisTclass(settingParams);
				List<JSONObject> allTclasses = (List<JSONObject>) disRs.get("allTclasses");
				
				System.out.println(allTclasses);
				dezyPlacementTaskService.setDivProc(placementId, "70", "75","分班结果已生成...", 0 );

				stuentZh = (List<JSONObject>) settingParams.get("stuentZh");
				dezyPlacementTaskService.setDivProc(placementId, "75","80", "生成表数据...", 0 );
				HashMap<String,Object> saveRs = TmpUtil.genLargeDivTclassResult(disRs,stuentZh,obj,kmMcMap);
				dezyPlacementTaskService.setDivProc(placementId, "80", "95","表数据已生成...", 0 );

				
				List<TPlDezyClass> tclassList = (List<TPlDezyClass>) saveRs.get("tclassList");
				//++++++++  去重分班名称    +++++++++++++++++++
				if(CollectionUtils.isNotEmpty(tclassList)){
					List<String> tclassNames = new ArrayList<String>();
					for(TPlDezyClass cls : tclassList){
						if(tclassNames.contains(cls.getTclassName())){
							cls.setTclassName(cls.getTclassName()+"1");
						}
						tclassNames.add(cls.getTclassName());						
					}
				}
				
				//++++++++++++    end    +++++++++++++++
				
				List<TPlDezyTclassSubcomp> tclassSubCompList = (List<TPlDezyTclassSubcomp>) saveRs.get("tclassSubCompList");
				
				List<TPlDezySubjectcompStudent> subcompFromList = (List<TPlDezySubjectcompStudent>) saveRs.get("subcompFromList");
				List<TPlDezySubjectcomp> subCompList = (List<TPlDezySubjectcomp>) saveRs.get("subCompList");
				
				List<TPlStudentinfo> lstStuList = (List<TPlStudentinfo>) saveRs.get("lstStuList");
								
				dezyPlacementTaskService.setDivProc(placementId, "95", "正在保存分班结果...", 0 );
				
				dezyPlacementTaskDao.updateClearLargeResult(obj);
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo,tclassList);
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo,tclassSubCompList);
				
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo,subcompFromList);
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo,subCompList);
				dezyPlacementTaskDao.updateBatchInsertEntity(termInfo,lstStuList);
				
				List<TPlConfIndexSubs> confIndexSubsList = (List<TPlConfIndexSubs>) disRs.get("confIndexSubsList");
				for(TPlConfIndexSubs cf:confIndexSubsList){
					cf.setPlacementId(placementId);
					cf.setUsedGrade(usedGrade);
				}
				if(confIndexSubsList.size()>0){
					dezyPlacementTaskDao.updateBatchInsertEntity(termInfo, confIndexSubsList);
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("termInfo", termInfo);
				map.put("placementId", placementId);
				map.put("status", 100);
				placementTaskDao.updateDivProc(map);
				
				String rsMsg = "";
				dezyPlacementTaskService.setDivProc(placementId, "100", "分班完成", 0,rsMsg );

			}catch(Exception e){
				e.printStackTrace();
				System.out.println(e.getLocalizedMessage());
				dezyPlacementTaskService.setDivProc(placementId, "100", "分班失败！原因："+e.getMessage(), -1 );
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void updateResult(String schoolId,String termInfo,String placementId,List<StudentClassInfo> studentClassInfos,List<TPlDezyClass> tPlDezyClasss,Map<String,List<String>> adminClassDeDistribute){
		try{
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||
					StringUtils.isBlank(placementId)||studentClassInfos.size()<=0||tPlDezyClasss.size()<=0){
				logger.info("参数异常！");
				throw new Exception("参数异常！");
			}
			/*获取班级类型、使用年级*/
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("placementId", placementId);
			map.put("termInfo", termInfo);
			PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
			if("".equals(placementTask)){
				logger.info("分班信息有误！");
				throw new Exception("分班信息有误！");
			}
			List<TPlDezySubjectSet> tPlDezySubjectSets = placementTaskDao.getSubjectSetList(map);
			if(tPlDezySubjectSets.size()<=0){
				throw new Exception("科目信息设置有误");
			}
			//JSONObject.toJSONString(tPlDezySubjectSets)
			Map<String,String> subjectToGroupIds = new HashMap<String,String>();
			for(TPlDezySubjectSet tPlDezySubjectSet:tPlDezySubjectSets){
				String subject=tPlDezySubjectSet.getSubjectId();
				String subjectGroupId=tPlDezySubjectSet.getSubjectGroupId();
				subjectToGroupIds.put(subject, subjectGroupId);
			}
			Integer placementType = placementTask.getPlacementType();
			String usedGrade = placementTask.getUsedGrade();
			
			TPlDezySettings tPlDezySettings = placementTaskDao.getTPlDezySet(map);
			String wfid = tPlDezySettings.getWfId();
			map.put("wfId",wfid);
			String isByElection = placementTaskDao.getWfInfo(map);
	
			//数据转换，每个数据保存在一个Map<String,Object>，Map有三个id，分别对应accountId，tclasss(Map,保存科目和班级id)，classKey(班级Key)
			List<Map<String,Object>> studentClasss =new ArrayList<>();
			
			for(StudentClassInfo studentClassInfo:studentClassInfos){
				
				String classKey="";
				Map<String,Object> studentClass = new HashMap<String,Object>();
				String accountId = studentClassInfo.getAccountId().toString();
				studentClass.put("accountId",accountId);
		        Map<String, String> tclasss = new TreeMap<String, String>(
		                new Comparator<String>() {
		                    public int compare(String obj1, String obj2) {
		                        return obj2.compareTo(obj1);
		                    }
		                });

		        //TreeMap自动按照Key排序
				for(TPlDezyClass tPlDezyClass:studentClassInfo.getTPlDezyClasss()){
					String subjectId = tPlDezyClass.getSubjectId();
					String tclassId = tPlDezyClass.getTclassId();
					tclasss.put(subjectId, tclassId);
					
	 			}
				if(placementType==3){
					tclasss.put("-999", studentClassInfo.getClassId());
				}
				studentClass.put("tclasss", tclasss);
				
				
				Set<String> keySet = tclasss.keySet();
		        Iterator<String> iter = keySet.iterator();
		        while (iter.hasNext()) {			
		        	String key = iter.next();
		        	classKey = classKey +","+ tclasss.get(key);
		        }
		        studentClass.put("classKey", classKey);
		        studentClasss.add(studentClass);
			}
			
			//转换成classKeys为Key，学生列表和班级列表为Value的形式
			Map<String,Map<String,Object>> classKeystus = new HashMap<String,Map<String,Object>>();
			for (Map<String,Object> studentClass : studentClasss){
				String classKey=(String) studentClass.get("classKey");
				if(!classKeystus.containsKey(classKey)){
					Map<String,Object> classKeystu = new HashMap<String,Object>();
					classKeystu.put("tclass", studentClass.get("tclasss"));
					classKeystu.put("studentIds",new ArrayList<String>());
					classKeystus.put(classKey, classKeystu);
				}
				
				Map<String,Object> classKeystu = classKeystus.get(classKey);
				List<String> studentIds = (List<String>) classKeystu.get("studentIds");
				studentIds.add((String) studentClass.get("accountId"));
				classKeystu.put("studentIds",studentIds);
			}
			
			/*处理t_pl_dezy_classgroup表的数据,只有定二走一需要*/
			List<TPlDezyClassgroup> tPlDezyClassgroups = new ArrayList<TPlDezyClassgroup>();
			if(placementType==4){
					List<String> subjectGroupIds = placementTaskDao.getSubjectGroupIDList(map);
					Map<String,String> subjectGroupIdMap = new HashMap<String,String>();
					for(String subjectGroupId :subjectGroupIds){
						if(!subjectGroupIdMap.containsKey(subjectGroupId)){
							subjectGroupIdMap.put(subjectGroupId, subjectGroupId);
						}
					}
					String classIds ="";
					for(TPlDezyClass tPlDezyClass:tPlDezyClasss){
						if("-999".equals(tPlDezyClass.getSubjectId())){
							classIds = classIds + tPlDezyClass.getTclassId() +",";
						}
					}
					String classId = classIds.substring(0,classIds.length()-1);
					String classtGroupId = tPlDezyClasss.get(0).getClassGroupId();
					
					for(Map.Entry<String,String> entrys : subjectGroupIdMap.entrySet()){
						String subjectGroupId = entrys.getKey();
						TPlDezyClassgroup tPlDezyClassgroup = new TPlDezyClassgroup();
						tPlDezyClassgroup.setPlacementId(placementId);
						tPlDezyClassgroup.setSchoolId(schoolId);
						tPlDezyClassgroup.setUsedGrade(usedGrade);
						tPlDezyClassgroup.setSubjectGroupId(subjectGroupId);
						tPlDezyClassgroup.setClasstGroupId(classtGroupId);
						tPlDezyClassgroup.setClassIds(classId);
						tPlDezyClassgroup.setClassGroupName("组1");
						tPlDezyClassgroups.add(tPlDezyClassgroup);
					}
					
					if(tPlDezyClassgroups.size()>0){
						try{
							placementImportDao.deleteTPlDezyClassgroup(termInfo, placementId, schoolId);
							placementImportDao.batchInsertTPlDezyClassgroupList(termInfo, tPlDezyClassgroups);
						}catch (Exception e){
							logger.info("t_pl_dezy_classgroup表插入失败！",e);
							throw new Exception("t_pl_dezy_classgroup表插入失败！");
						}

					}		
			}
			
			
			/*处理t_pl_dezy_class表的数据*/
			if(tPlDezyClasss.size()>0){
				try{
					placementImportDao.deleteTPlDezyClass(termInfo, placementId, schoolId);
					placementImportDao.batchInsertTPlDezyClassList(termInfo, tPlDezyClasss);
				}catch(Exception e){
					logger.info("t_pl_dezy_class表插入失败！",e);
					throw new Exception("t_pl_dezy_class表插入失败！");
				}
			}
			
			/*处理t_pl_dezy_tclassfrom表的数据，只有定二走一需要*/
			if(placementType==4){
				List<TPlDezyTclassfrom> tPlDezyTclassfroms = new ArrayList<TPlDezyTclassfrom>();
				//Key为行政班id+教学班id，value中的Map分别对应行政班和教学班id
				Map<String,Map<String,String>> classTclasss = new HashMap<String,Map<String,String>>();
				for(StudentClassInfo studentClassInfo:studentClassInfos){
					//行政班id
					String classId =studentClassInfo.getClassId();
					for(TPlDezyClass tPlDezyClass:studentClassInfo.getTPlDezyClasss()){
						if("-999".equals(tPlDezyClass.getSubjectId())){
							continue;
						}
						//教学班id
						String tclassId = tPlDezyClass.getTclassId();
						String subjectId = tPlDezyClass.getSubjectId();
						StringBuilder classTclassId =new StringBuilder();
						classTclassId.append(classId).append(",").append(tclassId);
						String classtclassId =""+classTclassId;
						
						if(!classTclasss.containsKey(classtclassId)){
							Map<String,String> classTclass =new HashMap<String,String>();
							classTclass.put("classId",classId);
							classTclass.put("tclassId",tclassId);
							classTclass.put("subjectId",subjectId);
							classTclasss.put(classtclassId, classTclass);
						}
					}	
				}
				
				for(Map.Entry<String,Map<String,String>> entrys : classTclasss.entrySet()){
					TPlDezyTclassfrom tPlDezyTclassfrom = new TPlDezyTclassfrom();
					Map<String,String> entry = entrys.getValue();
					String classId = entry.get("classId");
					String tclassId = entry.get("tclassId");
					String subjectId = entry.get("subjectId");
					String subjectGroupId = subjectToGroupIds.get(subjectId);
					String classGroupId = tPlDezyClassgroups.get(0).getClasstGroupId();
					tPlDezyTclassfrom.setPlacementId(placementId);
					tPlDezyTclassfrom.setSchoolId(schoolId);
					tPlDezyTclassfrom.setUsedGrade(usedGrade);
					tPlDezyTclassfrom.setSubjectGroupId(subjectGroupId);
					tPlDezyTclassfrom.setClassGroupId(classGroupId);
					tPlDezyTclassfrom.setClassId(classId);
					tPlDezyTclassfrom.setTclassId(tclassId);
					tPlDezyTclassfroms.add(tPlDezyTclassfrom);
				}
				if(tPlDezyTclassfroms.size()>0){
					try{
						placementImportDao.deleteTPlDezyTclassfrom(termInfo, placementId, schoolId);
						placementImportDao.batchInsertTPlDezyTclassfromList(termInfo, tPlDezyTclassfroms);
					}catch(Exception e){
						logger.info("t_pl_dezy_tclassfrom表插入失败！",e);
						throw new Exception("t_pl_dezy_tclassfrom表插入失败！");
					}
				}
			}
			
			/*
			 * t_pl_dezy_subjectcomp、t_pl_dezy_tclass_subcomp、t_pl_dezy_subjectcomp_student
			 * 三张表数据相互关联，一起做处理
			 * Map<String,Map<String,Object>>
			 * classKeys为Key，学生列表List<String>和班级列表Map<String(subject), String(classid)>的形式
			 * */
			List<TPlDezySubjectcomp> tPlDezySubjectcomps = new ArrayList<TPlDezySubjectcomp>();
			List<TPlDezyTclassSubcomp> tPlDezyTclassSubcomps = new ArrayList<TPlDezyTclassSubcomp>();
			List<TPlDezySubjectcompStudent> tPlDezySubjectcompStudents = new ArrayList<TPlDezySubjectcompStudent>();
			 for(Map.Entry<String,Map<String,Object>> entrys : classKeystus.entrySet()){
				 String subjectCompId = UUIDUtil.getUUID();
				 Map<String,Object> entry = entrys.getValue();
				 Map<String,String> tclass = (Map<String, String>) entry.get("tclass");
				 List<String> studentIds  = (List<String>) entry.get("studentIds");
				 String accountId = studentIds.get(0);
				 //先遍历学生数据，把t_pl_dezy_subjectcomp_student的记录加上来
				 for(String studentAccountId :studentIds){
					 TPlDezySubjectcompStudent tPlDezySubjectcompStudent =new TPlDezySubjectcompStudent();
					 tPlDezySubjectcompStudent.setPlacementId(placementId);
					 tPlDezySubjectcompStudent.setSchoolId(schoolId);
					 tPlDezySubjectcompStudent.setUsedGrade(usedGrade);
					 tPlDezySubjectcompStudent.setSubjectCompId(subjectCompId);
					 tPlDezySubjectcompStudent.setStudentId(studentAccountId);
					 tPlDezySubjectcompStudents.add(tPlDezySubjectcompStudent);
				 }
				 
				 for(Map.Entry<String,String> tclassOrClass: tclass.entrySet()){
					 TPlDezySubjectcomp tPlDezySubjectcomp = new TPlDezySubjectcomp();
					 if(tclassOrClass.getKey() == "-999"){
						 //t_pl_dezy_subjectcomp表中需要把行政班的数据加上来，t_pl_dezy_tclass_subcomp表中如果是定二走一模式的话也需要添加行政班数据
						 if(placementType==4){
							 TPlDezyTclassSubcomp tPlDezyTclassSubcomp = new TPlDezyTclassSubcomp();
							 tPlDezySubjectcomp.setClassId(tclassOrClass.getValue()); 
							 tPlDezyTclassSubcomp.setTclassId(tclassOrClass.getValue());
							 tPlDezyTclassSubcomp.setPlacementId(placementId);
							 tPlDezyTclassSubcomp.setSchoolId(schoolId);
							 tPlDezyTclassSubcomp.setUsedGrade(usedGrade);
							 tPlDezyTclassSubcomp.setSubjectCompId(subjectCompId);
							 tPlDezyTclassSubcomps.add(tPlDezyTclassSubcomp);
						 }else if(placementType==3){
							 tPlDezySubjectcomp.setClassId("-999");
						 }
						 tPlDezySubjectcomp.setCompNum(studentIds.size());
						 List<JSONObject> zhList = new ArrayList<JSONObject>();
						 JSONObject zh = new  JSONObject();
						 Map<String,String> wf =new HashMap<String,String>();
						 wf.put("schoolId", schoolId);
						 wf.put("wfId", wfid);
						 wf.put("accountId", accountId);
						 if("1".equals(isByElection)){
							 zhList = placementTaskDao.getByZhStudentToThirdByAccountId(wf);
							 if(zhList.size()>0){
								 zh = zhList.get(0);
							 }
						 }else{
							 zhList = placementTaskDao.getZhStudentToThirdByAccountId(wf);
							 if(zhList.size()>0){
								 zh = zhList.get(0); 
							 }
						 }
						 String zhName = zh.getString("zhName");
						 String subjectIds = zh.getString("subjectIds");
						 tPlDezySubjectcomp.setCompName(zhName);
						 
						 //如果是定二走一的话，不是用政史地这样的格式，而是"政治,历史,地理",并且定二的科目要放到前面
						 if(placementType ==4){
							 //Map<String,List<String>> adminClassDeDistribute 行政班id value为List<科目id>
							 String zhNameNew="";
							 //获得这个行政班的定二科目列表
							 List<String> deSubjectList = new ArrayList<String>();
							 for(Map.Entry<String,List<String>> entry2:adminClassDeDistribute.entrySet()){
								 if(tclassOrClass.getValue().equals(entry2.getKey())){
									 deSubjectList = entry2.getValue();
								 }
							 }
							List<String> subjectList = new ArrayList<>(Arrays.asList(subjectIds.split(",")));
							List<String> needToDelete =new ArrayList<String>();
							for(String deSubject:deSubjectList){
								for(String subject:subjectList){
									if(deSubject.equals(subject)){
										switch(subject){
										case "4":
											zhNameNew +="政治,";
											break;
										case "5":
											zhNameNew +="历史,";
											break;
										case "6":
											zhNameNew +="地理,";
											break;
										case "7":
											zhNameNew +="物理,";
											break;
										case "8":
											zhNameNew +="化学,";
											break;
										case "9":
											zhNameNew +="生物,";
											break;
										case "19":
											zhNameNew +="技术,";
											break;
										default:
											break;
										}
										needToDelete.add(subject);
									}
								}
							}
							for(String deleteSubjectId:needToDelete){
								subjectList.remove(deleteSubjectId);
							}
							
							if(subjectList.size()>0){
								for(String subject:subjectList){
									switch(subject){
									case "4":
										zhNameNew +="政治,";
										break;
									case "5":
										zhNameNew +="历史,";
										break;
									case "6":
										zhNameNew +="地理,";
										break;
									case "7":
										zhNameNew +="物理,";
										break;
									case "8":
										zhNameNew +="化学,";
										break;
									case "9":
										zhNameNew +="生物,";
										break;
									case "19":
										zhNameNew +="技术,";
										break;
									default:
										break;
									}
								}
							}
							tPlDezySubjectcomp.setCompName(zhNameNew.substring(0, zhNameNew.length()-1));
						 }
						 
						 tPlDezySubjectcomp.setCompFrom(subjectIds);
						 tPlDezySubjectcomp.setPlacementId(placementId);
						 tPlDezySubjectcomp.setSchoolId(schoolId);
						 tPlDezySubjectcomp.setUsedGrade(usedGrade);
						 tPlDezySubjectcomp.setSubjectCompId(subjectCompId);
						 tPlDezySubjectcomps.add(tPlDezySubjectcomp);
					 }else{
						 //不是行政班的时候，只需要添加t_pl_dezy_tclass_subcomp表中的数据
						 TPlDezyTclassSubcomp tPlDezyTclassSubcomp = new TPlDezyTclassSubcomp();
						 tPlDezyTclassSubcomp.setTclassId(tclassOrClass.getValue());
						 tPlDezyTclassSubcomp.setPlacementId(placementId);
						 tPlDezyTclassSubcomp.setSchoolId(schoolId);
						 tPlDezyTclassSubcomp.setUsedGrade(usedGrade);
						 tPlDezyTclassSubcomp.setSubjectCompId(subjectCompId);
						 tPlDezyTclassSubcomps.add(tPlDezyTclassSubcomp);
					 }
					 
				 }
			 }
			
			 if(tPlDezySubjectcomps.size()>0){
				 try{
					 placementImportDao.deleteTPlDezySubjectcomp(termInfo, placementId, schoolId);
					 placementImportDao.batchInsertTPlDezySubjectcompList(termInfo, tPlDezySubjectcomps); 
				 }catch(Exception e){
					logger.info("t_pl_dezy_subjectcomp表插入失败！",e);
					throw new Exception("t_pl_dezy_subjectcomp表插入失败！");
				 }
			 }
			 
			 if(tPlDezyTclassSubcomps.size()>0){
				 try{
					 placementImportDao.deleteTPlDezyTclassSubcomp(termInfo, placementId, schoolId);
					 placementImportDao.batchInsertTPlDezyTclassSubcompList(termInfo, tPlDezyTclassSubcomps);  
				 }catch(Exception e){
					logger.info("t_pl_dezy_tclass_subcomp表插入失败！",e);
					throw new Exception("t_pl_dezy_tclass_subcomp表插入失败！");
				 }

			 }
			 
			 if(tPlDezySubjectcompStudents.size()>0){
				 try{
					 placementImportDao.deleteTPlDezySubjectcompStudent(termInfo, placementId, schoolId);
					 placementImportDao.batchInsertTPlDezySubjectcompStudentList(termInfo, tPlDezySubjectcompStudents);
				 }catch(Exception e){
					logger.info("t_pl_dezy_subjectcomp_student表插入失败！",e);
					throw new Exception("t_pl_dezy_subjectcomp_student表插入失败！");
				 }
			 }
			 
			 //更新分班状态
			 PlacementTask placementTask2 = new PlacementTask();
			 placementTask2.setSchoolId(Long.parseLong(schoolId));
			 placementTask2.setTermInfo(termInfo);
			 placementTask2.setPlacementId(placementId);
			 placementTask2.setStatus(100);
			 placementTaskDao.updatePlacementTaskById(placementTask2);
			 
		}catch(Exception e){
			Log.error("更新数据失败:"+e.getMessage(), e);
			throw new RuntimeException("更新数据失败！"+e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public void dataConversion(String schoolId,String termInfo,String usedGrade,String placementId,List<ClassInfo> classInfos){
		try{
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||
					StringUtils.isBlank(placementId)||StringUtils.isBlank(usedGrade)||classInfos.size()<=0){
				logger.info("参数异常！");
				throw new Exception("参数异常！");
			}
			
			/*打印查看数据
			System.out.println("打印List<ClassInfo>的数据：");
			for(ClassInfo classInfo:classInfos){
				String tclassName= classInfo.getTclassName();
				String tclassId = classInfo.getTclassId();
				Integer classSeq = classInfo.getClassSeq();
				Integer tclassLevel = classInfo.getTclassLevel();
				Integer classInfo2 = classInfo.getClassInfo();
				String groundId = classInfo.getGroundId();
				String groundNam = classInfo.getGroundName();
				String subjectId = classInfo.getSubjectId();
				Integer tclassNum = classInfo.getTclassNum();
				List<StudentbaseInfo> studentLists = classInfo.getStudentLists();
				Integer[] fixedSubjectIds = classInfo.getFixedSubjectIds();
				String studentList="";
				String fixedSubjectId ="";
				for (StudentbaseInfo student:studentLists){
					String studentInfo = student.getId()+",";
					studentList += studentInfo;
				}
				if(null !=fixedSubjectIds){
					for(int i=0;i<fixedSubjectIds.length;i++){
						fixedSubjectId += fixedSubjectIds[i]+",";
					}
				}
				
				System.out.println("tclassName:"+tclassName+";tclassId:"+tclassId+";classSeq:"+classSeq+
						";tclassLevel:"+tclassLevel+";classInfo:"+classInfo2+";groundId:"+groundId+";groundName:"
						+groundNam+";subjectId:"+subjectId+";tclassNum:"+tclassNum+";studentList:"+studentList+
						";fixedSubjectId:"+fixedSubjectId);
			}
			*/
			
			
			List<StudentClassInfo> studentClassInfos = new ArrayList<StudentClassInfo>();

			List<TPlDezyClass> tPlDezyClasss = new ArrayList<TPlDezyClass>();
			Map<String,StudentClassInfo> keyStudentClassInfo = new HashMap<String,StudentClassInfo>();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("placementId", placementId);
			map.put("termInfo", termInfo);
			PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
			if(placementTask==null){
				throw new Exception("分班信息有误！");
			}
			int placementType = placementTask.getPlacementType();
			String classGroupId = "-999";
			if(placementType ==4){
				classGroupId = UUIDUtil.getUUID();
			}
			List<TPlDezySubjectSet> tPlDezySubjectSets = placementTaskDao.getSubjectSetList(map);
			/*
			 *遍历分班数据，将班级信息保存进tPlDezyClasss，并且传进List;
			 *学生对象的班级数据在多个classInfo中，所以学生需要先保存进Map中，遍历完之后得到学生所有科目班级后，再传入List
			 * */
			try{
				for(ClassInfo classInfo:classInfos){
					TPlDezyClass tPlDezyClass = new TPlDezyClass();	
					tPlDezyClass.setPlacementId(placementId);
					tPlDezyClass.setSchoolId(schoolId);
					tPlDezyClass.setUsedGrade(usedGrade);
					tPlDezyClass.setSubjectId(classInfo.getSubjectId());
					tPlDezyClass.setTclassId(classInfo.getTclassId());
					tPlDezyClass.setTclassType(classInfo.getClassInfo());
					tPlDezyClass.setTclassLevel(classInfo.getTclassLevel());
					tPlDezyClass.setClassSeq(classInfo.getClassSeq());
					tPlDezyClass.setTclassNum(classInfo.getStudentLists().size());
					tPlDezyClass.setGroundId(classInfo.getGroundId());
					tPlDezyClass.setGroundName(classInfo.getGroundName());
					tPlDezyClass.setTclassName(classInfo.getTclassName());
					tPlDezyClass.setOriClassName(classInfo.getTclassName());
					//subjectGroupId和classGroupId的数据
					for(TPlDezySubjectSet tPlDezySubjectSet:tPlDezySubjectSets){
						if(tPlDezySubjectSet.getSubjectId().equals(classInfo.getSubjectId())){
							tPlDezyClass.setSubjectGroupId(tPlDezySubjectSet.getSubjectGroupId());
						}
					}
					if("-999".equals(classInfo.getSubjectId())){
						tPlDezyClass.setSubjectGroupId("-999");
					}
					tPlDezyClass.setClassGroupId(classGroupId);
					
					int subLevel=0;
					if(placementType==3){
						subLevel=1; 
					}
					tPlDezyClass.setSubLevel(subLevel);	
					
					tPlDezyClasss.add(tPlDezyClass);
					
					for(StudentbaseInfo studentbaseInfo:classInfo.getStudentLists()){
						
						if(!keyStudentClassInfo.containsKey(studentbaseInfo.getId())){
							StudentClassInfo studentClassInfo =new StudentClassInfo();
							studentClassInfo.setSchoolId(schoolId);
							studentClassInfo.setPlacementId(placementId);
							studentClassInfo.setAccountId(studentbaseInfo.getId());
							
							if(placementType==3){
								map.put("accountId", studentbaseInfo.getId());
								String classid = placementTaskDao.getStudentClassInfo(map).get(0).get("classId").toString();
								studentClassInfo.setClassId(classid);
							}
							keyStudentClassInfo.put(studentbaseInfo.getId(), studentClassInfo);
						}
						StudentClassInfo studentClassInfo = keyStudentClassInfo.get(studentbaseInfo.getId());
						studentClassInfo.getTPlDezyClasss().add(tPlDezyClass);
						if(classInfo.getClassInfo()==6){
							studentClassInfo.setClassId(classInfo.getTclassId());
						}
					}
					
				}
			}catch(Exception e){
				logger.error("数据格式转换失败！",e);
				throw new RuntimeException("数据格式转换失败！",e);
			}
			
			
			
			//遍历MapkeyStudentClassInfo，将Value数据保存进对应实体对象List中
			for(Map.Entry<String,StudentClassInfo> studentClassInfo: keyStudentClassInfo.entrySet()){
				studentClassInfos.add(studentClassInfo.getValue());
			}
			
			//行政班的定二分布(Key为行政班id，Value为定二科目id)
			Map<String,List<String>> adminClassDeDistribute = new HashMap<String,List<String>>();
			if(placementType==4){
				for(ClassInfo classInfo:classInfos){
					if(classInfo.getClassInfo()==6&& null != classInfo.getFixedSubjectIds()){
						for(int i=0;i<classInfo.getFixedSubjectIds().length;i++){
							if(!adminClassDeDistribute.containsKey(classInfo.getTclassId())){
								adminClassDeDistribute.put(classInfo.getTclassId(), new ArrayList<String>());
							}
							List<String> deSubjectList = adminClassDeDistribute.get(classInfo.getTclassId());
							deSubjectList.add(classInfo.getFixedSubjectIds()[i].toString());
							adminClassDeDistribute.put(classInfo.getTclassId(), deSubjectList);
						}
					}
				}
			}
			
			updateResult(schoolId,termInfo,placementId,studentClassInfos,tPlDezyClasss,adminClassDeDistribute);
			
		}catch(Exception e){
			logger.error("数据转换失败！",e);
			throw new RuntimeException("数据转换失败！", e);
		}
	}
	
	
	
	@Override
	public List<ClassInfo>  arrangeClassGroud(String schoolId,String termInfo,String usedGrade,String placementId,List<ClassInfo> classInfos){
		try{
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||
					StringUtils.isBlank(placementId)||StringUtils.isBlank(usedGrade)||classInfos.size()<=0){
				logger.info("参数异常！");
				throw new Exception("参数异常！");
			}
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("placementId", placementId);
			map.put("termInfo", termInfo);
			PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
			Integer placementType = placementTask.getPlacementType();

			
			//保存每个序列的班级信息（Key为序列，考虑到兼容大走班，用tclassLevel+classSeq的组合字符串作为标识)
			Map<String,List<ClassInfo>> sequenceClassInfos = new HashMap<String,List<ClassInfo>>();
			try{
				for(ClassInfo classInfo:classInfos){
					Integer tclassLevel = classInfo.getTclassLevel();
					Integer classSeq = classInfo.getClassSeq();
					String Key = tclassLevel.toString() + classSeq.toString();
					if(!sequenceClassInfos.containsKey(Key)){
						sequenceClassInfos.put(Key, new ArrayList<ClassInfo>());
					}
					List<ClassInfo> sequenceClassInfo = sequenceClassInfos.get(Key);
					sequenceClassInfo.add(classInfo);
				}
			}catch (Exception e){
				logger.error("保存每个序列的班级信息失败！",e);
				e.printStackTrace();
			}

			//计算出所有时间点的最大开班数，作为需要的场地数量
			Integer maxClassNum = 0;
			for(Map.Entry<String,List<ClassInfo>> entrys : sequenceClassInfos.entrySet()){
				Integer classNum = entrys.getValue().size();
				if(classNum > maxClassNum){
					maxClassNum = classNum;
				}
			}
			
			//获取基础数据中本年级的班级信息(包括班级名、班级id、班级数量)
			String xn = termInfo.substring(0,termInfo.length()-1);
			
			
			//createLevel数据库中保存的字段，记录年级，和usedGrade的转换规则也是固定
			Integer createLevel = 10+(Integer.parseInt(xn)-Integer.parseInt(usedGrade));
			map.put("createLevel", createLevel.toString());
			List<JSONObject> oldClassInfos = placementTaskDao.getOldClassInfo(map);
			Integer oldclassNum = oldClassInfos.size();

			
			//判断班级数量是否足够开班，不够的话就需要新增辅助教室
			List<JSONObject> addclassInfos = new ArrayList<JSONObject>();
			if(maxClassNum > oldclassNum){
				Integer addClassNum = maxClassNum - oldclassNum;
				for(int i=1;i<=addClassNum;i++){
					JSONObject addclassInfo = new JSONObject();
					addclassInfo.put("id", UUIDUtil.getUUID());
					addclassInfo.put("className","辅助"+i);
					addclassInfos.add(addclassInfo);
				}
			}
			
			//如果是定二走一的话，安排行政班场地信息（需要将场地名、行政班名、场地id加上）
			try{
				if(placementType == 4){
					//presentClassInfos的Key为"00"的时候是行政班方式班级
					List<ClassInfo> presentClassInfos = sequenceClassInfos.get("00");
					//i和j作为oldClassInfos和addclassInfos的序列，开班的时候控制尽量先使用基础数据中的班级，不够才用辅助教室
					int i = 0;
					int j = 0;
					int bjxh = 1;
					//根据学年和使用年级转换成入学年级
					String rxnd = commonDataService.ConvertSYNJ2RXND(usedGrade,xn);
					for(ClassInfo presentClassInfo:presentClassInfos){
						if(i<oldClassInfos.size()){
							//班级名规则G1801
							String className = "G" + rxnd.substring(2) +"0" + bjxh;
							//className长度大于5说明bjxh是两位的，那么入学级届后面的0需要删掉
							if(className.length()>5){
								className = className.substring(0,3) +className.substring(4);
							}
							presentClassInfo.setGroundId(oldClassInfos.get(i).get("id").toString());
							presentClassInfo.setTclassName(className);
							presentClassInfo.setGroundName(className+"教室");
							i++;
							bjxh++;
						}else{
							presentClassInfo.setGroundId(addclassInfos.get(j).get("id").toString());
							presentClassInfo.setTclassName(addclassInfos.get(j).get("className").toString());
							presentClassInfo.setGroundName(addclassInfos.get(j).get("className").toString() + "教室");
							j++;
						}
					}
				}
			}catch (Exception e){
				logger.error("定二走一模式下行政班场地信息分配失败！",e);
				e.printStackTrace();
				throw new RuntimeException("定二走一模式下行政班场地信息分配失败！");
			}
			
			//classRoomInfos为场地信息，Key为行政班id，Value为对应的场地信息，包含场地名、场地id、班级名
			Map<String,Map<String,String>> classRoomInfos = new HashMap<String,Map<String,String>>();
			try{
				if(addclassInfos.size()>0){
					for(JSONObject addclassInfo:addclassInfos){
						Map<String,String> classRoomInfo = new HashMap<String,String>();
						classRoomInfo.put("classRoomId", addclassInfo.get("id").toString());
						classRoomInfo.put("className", addclassInfo.get("className").toString());
						classRoomInfo.put("classRoomName", addclassInfo.get("className").toString()+"教室");
						classRoomInfos.put(addclassInfo.get("id").toString(), classRoomInfo);
					}
				}
				if(placementType == 3){
					if(oldClassInfos.size()>0){
						for(JSONObject oldClassInfo:oldClassInfos){
							Map<String,String> classRoomInfo = new HashMap<String,String>();
							classRoomInfo.put("classRoomId",oldClassInfo.get("id").toString());
							classRoomInfo.put("className", oldClassInfo.get("className").toString());
							classRoomInfo.put("classRoomName",oldClassInfo.get("className").toString()+"教室");
							classRoomInfos.put(oldClassInfo.get("id").toString(), classRoomInfo);
						}
					}
				}else if(placementType == 4){
					List<ClassInfo> presentClassInfos = sequenceClassInfos.get("00");
					if(presentClassInfos.size()>0){
						for(ClassInfo presentClassInfo:presentClassInfos){
							Map<String,String> classRoomInfo = new HashMap<String,String>();
							classRoomInfo.put("classRoomId", presentClassInfo.getGroundId());
							classRoomInfo.put("className", presentClassInfo.getTclassName());
							classRoomInfo.put("classRoomName", presentClassInfo.getGroundName());
							classRoomInfos.put(presentClassInfo.getTclassId(), classRoomInfo);
						}
					}
				}
			}catch (Exception e){
				logger.error("classRoomInfos生成失败！",e);
				e.printStackTrace();
			}
			

			//查询本年级所有学生所属的行政班，每个学生一个Map(Key分别为studentAccountId,AdiminClassId)
			Map<String,String> studentAdiminClassInfos =  new HashMap<String,String>();
			try{
				if(placementType == 4){
					List<ClassInfo> presentClassInfos = sequenceClassInfos.get("00");
					for(ClassInfo presentClassInfo:presentClassInfos){
						 for(StudentbaseInfo studentbaseInfo:presentClassInfo.getStudentLists()){
							 studentAdiminClassInfos.put(studentbaseInfo.getId(), presentClassInfo.getTclassId());
						 }
					}
				}else if(placementType == 3){
					List<String> classIds = new ArrayList<String>();
					for(JSONObject oldClassInfo:oldClassInfos){
						classIds.add(oldClassInfo.get("id").toString());
					}
					map.put("classIds", classIds);
					List<JSONObject> studentAdiminClassInfos2 = placementTaskDao.getStudentClassInfo(map);
					for(JSONObject studentAdiminClassInfo2:studentAdiminClassInfos2){
						 studentAdiminClassInfos.put(studentAdiminClassInfo2.get("accountId").toString(), studentAdiminClassInfo2.get("classId").toString());
					}
				}
			}catch (Exception e){
				logger.error("studentAdiminClassInfos生成失败！",e);
				e.printStackTrace();
			}
			
			
			
			//安排教学班场地信息（按照时间点对本序列下的班级学生进行遍历，确定每个学生所属的行政班，计算每个行政班）
			for(Map.Entry<String,List<ClassInfo>> entrys : sequenceClassInfos.entrySet()){
				//如果是行政班的序列，跳过不分配班级（前面已经分配过了）
				if("00".equals(entrys.getKey())){
					continue;
				}
				
				/*
				 * adminTeachInfo key保存行政班id+教学班id，用来确定唯一性，Value里面保存对应分组的教学班id、行政班id、人数等信息
				 * 考虑到学生遍历的时候，只会查有学生的行政班，所以最开始就遍历所有行政班、教学班，把adminTeachInfos生成完成
				 * */
				Map<String,Map<String,Object>> adminTeachInfos = new HashMap<String,Map<String,Object>>();
				try{for(ClassInfo entry:entrys.getValue()){
					if(placementType==4){
						List<ClassInfo> presentClassInfos = sequenceClassInfos.get("00");
						for(ClassInfo presentClassInfo:presentClassInfos){
							String adminTeachKey = presentClassInfo.getTclassId()+","+entry.getTclassId();
							Map<String,Object> adminTeachInfo = new HashMap<String,Object>();
							adminTeachInfo.put("adminClassId", presentClassInfo.getTclassId());
							adminTeachInfo.put("teachClassId", entry.getTclassId());
							adminTeachInfo.put("teachClassNum", entry.getTclassNum());
							adminTeachInfo.put("adminClassNum", 0);
							adminTeachInfo.put("classInfo", entry);
							adminTeachInfos.put(adminTeachKey, adminTeachInfo);
						}
					}else if(placementType == 3){
						if(oldClassInfos.size()>0){
							for(JSONObject oldClassInfo:oldClassInfos){
								String adminTeachKey = oldClassInfo.get("id")+","+entry.getTclassId();
								Map<String,Object> adminTeachInfo = new HashMap<String,Object>();
								adminTeachInfo.put("adminClassId", oldClassInfo.get("id"));
								adminTeachInfo.put("teachClassId", entry.getTclassId());
								adminTeachInfo.put("teachClassNum", entry.getTclassNum());
								adminTeachInfo.put("adminClassNum", 0);
								adminTeachInfo.put("classInfo", entry);
								adminTeachInfos.put(adminTeachKey, adminTeachInfo);
							}
						}
					}
					//有辅助教室的时候也要加上来 id className
					if(addclassInfos.size()>0){
						for(JSONObject addclassInfo:addclassInfos){
							String adminTeachKey = addclassInfo.get("id")+","+entry.getTclassId();
							Map<String,Object> adminTeachInfo = new HashMap<String,Object>();
							adminTeachInfo.put("adminClassId", addclassInfo.get("id"));
							adminTeachInfo.put("teachClassId", entry.getTclassId());
							adminTeachInfo.put("teachClassNum", entry.getTclassNum());
							adminTeachInfo.put("adminClassNum", 0);
							adminTeachInfo.put("classInfo", entry);
							adminTeachInfos.put(adminTeachKey, adminTeachInfo);
						}
					}
				}
			}catch (Exception e){
				logger.error("adminTeachInfos生成失败！",e);
				e.printStackTrace();
			}
				

				//遍历本序列下的所有班级下所有学生，在对应adminTeachKey的行政班人数中+1
				for(ClassInfo entry: entrys.getValue()){
					String teachClassId = entry.getTclassId();
					//遍历本班级下的所有学生
					for(StudentbaseInfo StudentbaseInfo:entry.getStudentLists()){
						String accountId = StudentbaseInfo.getId();
						//行政班id
						String adminClassId = studentAdiminClassInfos.get(accountId);
						String adminTeachKey = adminClassId+","+teachClassId;
						if(!adminTeachInfos.containsKey(adminTeachKey)){
							throw new Exception("数据有误，没找到对应的adminTeachKey");
						}
						Map<String,Object> adminTeachInfo = adminTeachInfos.get(adminTeachKey);
						Integer adminClassNum = (Integer) adminTeachInfo.get("adminClassNum") +1;
						adminTeachInfo.put("adminClassNum", adminClassNum);
					}	
				}
				
				//将adminTeachInfos数据进行转换，转换成List(Map<String，String>),Map在之前的基础上加上一个百分比
				//加上一个已使用行政班和已使用教学班的Map id为班级id，用来这个比例对应的班级有没有被使用
				List<Map<String,Object>> adminTeachInfoList = new ArrayList<Map<String,Object>>();
				try{
					for(Map.Entry<String,Map<String,Object>> entry:adminTeachInfos.entrySet()){
						Map<String,Object> adminTeachInfo = new HashMap<String,Object>();
						int teachClassNum =(int)entry.getValue().get("teachClassNum");
						int adminClassNum =(int)entry.getValue().get("adminClassNum");
						String adminClassId = entry.getValue().get("adminClassId").toString();
						String teachClassId = entry.getValue().get("teachClassId").toString();
						ClassInfo classInfo = (ClassInfo) entry.getValue().get("classInfo");
						
						
						float proportion = 0.000f;
						if(teachClassNum >0){
							proportion = adminClassNum*10000/teachClassNum;
						}
						adminTeachInfo.put("proportion", proportion);
						adminTeachInfo.put("adminClassId", adminClassId);
						adminTeachInfo.put("teachClassId", teachClassId);
						adminTeachInfo.put("classInfo", classInfo);
						adminTeachInfoList.add(adminTeachInfo);
					}
				}catch (Exception e){
					logger.error("adminTeachInfoList生成失败！",e);
					e.printStackTrace();
				}
				
				//对adminTeachInfoList排降序，以Map中的proportion排序
				Collections.sort(adminTeachInfoList, new Comparator<Map<String, Object>>() {
		            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		            	Float proportion1 = (float)o1.get("proportion");
		            	Float proportion2 = (float)o2.get("proportion");
		                return proportion2.compareTo(proportion1);
		            }
		        });
				
				//用来保存本序列已经使用了的行政班和教学班，Key为班级id
				Map<String,String> adminClass = new HashMap<String,String>();
				Map<String,String> teachClass = new HashMap<String,String>();
				//已安排教学班场地的数量，如果等于本序列的教学班数量，那么break
				int completeTeachClassNum = 0;
				for(Map<String,Object> adminTeachInfo :adminTeachInfoList){
					if(completeTeachClassNum == entrys.getValue().size()){
						break;
					}
					String teachClassId = adminTeachInfo.get("teachClassId").toString();
					String adminClassId = adminTeachInfo.get("adminClassId").toString();
					//如果行政班或者教学班已经被使用的话，continue
					if(adminClass.containsKey(adminClassId)||teachClass.containsKey(teachClassId)){
						continue;
					}
					ClassInfo classInfo = (ClassInfo) adminTeachInfo.get("classInfo");					
					String subjectName = "";
					switch (Integer.parseInt(classInfo.getSubjectId())){
					case 4:
						subjectName="政";
						break;
					case 5:
						subjectName="史";
						break;
					case 6:
						subjectName="地";
						break;
					case 7:
						subjectName="物";
						break;
					case 8:
						subjectName="化";
						break;
					case 9:
						subjectName="生";
						break;
					case 19:
						subjectName="技";
						break;
					default:
						throw new Exception("subjectName信息有误");
					}
					String classType ="";
					switch (classInfo.getTclassLevel()){
					case 1:
						classType="选";
						break;
					case 2:
						classType="学";
						break;
					default :
						throw new Exception("classType信息有误");
					}
					String className = subjectName + "(" + classType + ")" + classRoomInfos.get(adminClassId).get("className");
					//判断班级名是否已经存在，如果已经存在的话，流水号+1(认为不会有10个同样类型的班用到同一个场地)
					String classNameNew ="";
					for(int i =0;i<=10;i++){
						Boolean enable = true;
						String suffix ="";
						if(i>0){
							suffix=i+"";
						}
						for(ClassInfo classInfo2:classInfos){
							if(classInfo2.getTclassName().equals(className+suffix)){
								enable = false;
								break;
							}	
						}
						if(enable){
							classNameNew = className+suffix;
						}
						if(!classNameNew.isEmpty()){
							break;
						}
					}
					
					//对教学班的场地id、场地名、班级名进行分配
					classInfo.setTclassName(classNameNew);
					classInfo.setGroundId(classRoomInfos.get(adminClassId).get("classRoomId"));
					classInfo.setGroundName(classRoomInfos.get(adminClassId).get("classRoomName"));
					//已经使用的教学班或者行政班不能再使用，另外就是如果班级的场地都已经安排了就不需要再遍历
					adminClass.put(adminClassId, adminClassId);
					teachClass.put(teachClassId, teachClassId);
					completeTeachClassNum++;
					
				}
			}
		}catch (Exception e){
			logger.error("场地分配失败！",e);
			throw new RuntimeException("场地分配失败！", e);
		}
		
		return classInfos;
	}
	
	

	public void updateWish(String schoolId,String usedGrade,String wfId,List<JSONObject> studentWishs){
		List<JSONObject> jsonList = WishPlacement(schoolId,usedGrade,wfId,studentWishs);
		for(JSONObject json:jsonList){
			PlacementTask placementTask = (PlacementTask) json.get("placementTask");
			String placementId = placementTask.getPlacementId();
			Integer placementType = placementTask.getPlacementType();
			String termInfo = json.get("termInfo").toString();
			List<String> tClassIds = (List<String>) json.get("tclassIdList");
			scheduleExternalService.updateTclass(schoolId, placementId, placementType, termInfo, tClassIds);
		}
	}
	
	@Override
	public List<JSONObject> WishPlacement(String schoolId,String usedGrade,String wfId,List<JSONObject> studentWishs){
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("WishPlacementTans");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus status = txManager.getTransaction(def);
		try {
			//如果没有学生改志愿的话，不做任何处理
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(usedGrade)||StringUtils.isBlank(wfId)||studentWishs.size()<=0){
				return jsonList;
			}
			//termInfo为当前学年学期。只做对本学期分班的影响，历史数据不处理
			String termInfo = placementTaskDao.getCurrenTermInfo();
			//TODO 暂时把学年学期写死成20181，否则126上无法测试(更新的时候记得取消备注)
			//termInfo="20181";

			Map<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("wfId", wfId);
			map.put("termInfo", termInfo);
			String isByElection = placementTaskDao.getWfInfo(map);
			List<String> placementIds = placementTaskDao.getplacementInfoByWfid(map);
			//已分班的轮次中，没有使用指定选科，不做任何处理
			if(placementIds.size()>0){
				//只有大走班和定二走一是从t_pl_dezy_settings表中查询对应的选科id
				for(String placementId:placementIds){
					map.put("placementId", placementId);
					PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
					Integer placementType = placementTask.getPlacementType();
					
					if(placementType ==3 ||placementType ==4){
						//大走班或者定二走一(大体结构类似,在有区别的时候再做特殊处理)
						for(JSONObject studentWish:studentWishs){
							String accountId = studentWish.get("accountId").toString();
							String wishId = studentWish.get("wishId").toString();
							//检查本学生的志愿是否有变化
							Map<String,Object> stuMap = new HashMap<String,Object>();
							stuMap.put("termInfo", termInfo);
							stuMap.put("placementId", placementId);
							stuMap.put("studentId", accountId);
							stuMap.put("schoolId", schoolId);
							List<JSONObject> subjectcompInfos = placementTaskDao.getSubjectcompInfoByAccountId(stuMap);
							//subjectcompInfo为空或者学生原始志愿和新志愿一样，跳过这个学生
							if(subjectcompInfos.size()==0||subjectcompInfos.get(0).get("compFrom").toString().equals(wishId)){
								continue;
							}
							JSONObject json = new JSONObject();
							json.put("placementTask", placementTask);
							json.put("termInfo", termInfo);
							String subjectCompId = subjectcompInfos.get(0).get("subjectCompId").toString();
							Integer compNum = (Integer) subjectcompInfos.get(0).get("compNum");
							String adminClassId = subjectcompInfos.get(0).get("classId").toString();
							List<String> studentClassIds = new ArrayList<String>();
							for(JSONObject subjectcompInfo:subjectcompInfos){
								studentClassIds.add(subjectcompInfo.get("tclassId").toString());
							}
							if(placementType == 4){
								studentClassIds.add(adminClassId);
							}						
							Map<String,Object> subjectCompMap = new HashMap<String,Object>();
							subjectCompMap.put("termInfo", termInfo);
							subjectCompMap.put("placementId", placementId);
							subjectCompMap.put("schoolId", schoolId);
							subjectCompMap.put("subjectCompId", subjectCompId);
							subjectCompMap.put("compNum", compNum-1);
							placementTaskDao.updateTPlDezySubjectcomp(subjectCompMap);

							Map<String,Object> stuInfoMap = new HashMap<String,Object>();
							stuInfoMap.put("placementId", placementId);
							stuInfoMap.put("termInfo", termInfo);
							stuInfoMap.put("schoolId", schoolId);
							stuInfoMap.put("studentClassIds", studentClassIds);
							List<TPlDezyClass> tPlDezyClasss = placementTaskDao.getDezyClassListByClassIds(stuInfoMap);

							List<String> tclassIdList = new ArrayList<String>();
							for(TPlDezyClass tPlDezyClass:tPlDezyClasss){
								tclassIdList.add(tPlDezyClass.getTclassId());
								Integer tclassNum = tPlDezyClass.getTclassNum()-1;
								Map<String,Object> classInfoMap = new HashMap<String,Object>();
								classInfoMap.put("schoolId", schoolId);
								classInfoMap.put("termInfo", termInfo);
								classInfoMap.put("placementId", placementId);
								classInfoMap.put("tclassId", tPlDezyClass.getTclassId());
								classInfoMap.put("tclassNum", tclassNum);
								placementTaskDao.updateTPlDezyClassNum(classInfoMap);
								
							}
							json.put("tclassIdList", tclassIdList);
							jsonList.add(json);
						
							
							Map<String,Object> stuMap2 = new HashMap<String,Object>();
							stuMap2.put("termInfo", termInfo);
							stuMap2.put("placementId", placementId);
							stuMap2.put("studentId", accountId);
							stuMap2.put("schoolId", schoolId);
							stuMap2.put("subjectCompId", subjectCompId);
							placementTaskDao.deleteTPlDezySubjectcompStudentById(stuMap2);

													
						}
						//等所有学生信息调整完之后，再根据选科中的数据，更新t_pl_dezy_subject_set表中每个科目的学、选考人数

						List<JSONObject> subjectCountNums = new ArrayList<JSONObject>();
						if(isByElection.equals("1")){
							subjectCountNums = placementTaskDao.countByIdByWfid(map);
						}else if(isByElection.equals("0")){
							subjectCountNums = placementTaskDao.countIdByWfid(map);
						}
						
						Long totalNum = 0L;
						Map<String,Long> subjectNums = new HashMap<String,Long>();
						for(JSONObject subjectCountNum:subjectCountNums){
							String subjectId = subjectCountNum.get("subjectId").toString();
							Long countNum = (Long) subjectCountNum.get("countNum");
							totalNum += countNum;
							subjectNums.put(subjectId, countNum);
						}
						
						totalNum = totalNum/3;
						
						for(Map.Entry<String,Long> subjectNum :subjectNums.entrySet()){
							String subjectId = subjectNum.getKey();
							Long optNum = subjectNum.getValue();
							Long proNum = totalNum -optNum;
							Map<String,Object> subjectSet = new HashMap<String,Object>();
							subjectSet.put("termInfo", termInfo);
							subjectSet.put("stuNum", optNum);
							subjectSet.put("stuNumOfPro", proNum);
							subjectSet.put("placementId", placementId);
							subjectSet.put("schoolId", schoolId);
							subjectSet.put("subjectId", subjectId);
							placementTaskDao.updateTPlDezySubjectSet(subjectSet);
						}
										
					}
				}
			}
			//微走班模式下的处理
			
			Map<String,Object> map2 = new HashMap<String,Object>();
			map2.put("termInfo", termInfo);
			map2.put("schoolId", schoolId);
			map2.put("wfId", wfId);
			List<String> placementIdTinys = placementTaskDao.getplacementIdByWfid(map2);
			
			if(placementIdTinys.size()>0){
				for(String placementIdTiny:placementIdTinys){						
					for(JSONObject studentWish:studentWishs){
						String accountId = studentWish.get("accountId").toString();
						String wishId = studentWish.get("wishId").toString();						
						Map<String,Object> stuMap = new HashMap<String,Object>();
						stuMap.put("termInfo", termInfo);
						stuMap.put("placementId", placementIdTiny);
						stuMap.put("accountId", accountId);
						stuMap.put("schoolId", schoolId);
						JSONObject openClass = placementTaskDao.getOpenClassByAccountId(stuMap);
						String subjectIdsStr = openClass.get("subjectIdsStr").toString();
						if(openClass==null || openClass.isEmpty()||wishId.equals(subjectIdsStr)){
							continue;
						}
						if(null!=openClass && !openClass.isEmpty()){
							JSONObject json = new JSONObject();
							PlacementTask placementTask = new PlacementTask();
							placementTask.setPlacementId(placementIdTiny);
							placementTask.setPlacementType(1);
							json.put("placementTask", placementTask);
							json.put("termInfo", termInfo);
							List<String> tclassIdList = new ArrayList<String>();
							if(!(wishId.equals(openClass.get("subjectIdsStr").toString()))){
								String openClassInfoId = openClass.get("openClassInfoId").toString();
								String openClassTaskId = openClass.get("openClassTaskId").toString();
								String teachingClassId = openClass.get("teachingClassId").toString();
								tclassIdList.add(teachingClassId);
								json.put("tclassIdList", tclassIdList);
								jsonList.add(json);
								Integer numOfBoys = (Integer) openClass.get("numOfBoys");
								Integer numOfGirls = (Integer) openClass.get("numOfGirls");
								Integer teachingclassNum = (Integer) openClass.get("teachingclassNum")-1;
								stuMap.put("teachingClassId", teachingClassId);
								stuMap.put("openClassInfoId", openClassInfoId);
								stuMap.put("openClassTaskId", openClassTaskId);
								stuMap.put("numOfBoys", numOfBoys);
								stuMap.put("numOfGirls", numOfGirls);
								stuMap.put("numOfStuds", teachingclassNum);
								
								Map<String,Object> queryGender = new HashMap<String,Object>();
								queryGender.put("termInfo", termInfo);
								queryGender.put("accountId", accountId);
								//查询男女
								String gender = placementTaskDao.queryGender(queryGender);
								if("1".equals(gender)){
									stuMap.put("numOfBoys", numOfBoys-1);
								}else if("2".equals(gender)){
									stuMap.put("numOfGirls", numOfGirls-1);
								}
								
								placementTaskDao.updateTPlTeachingclassinfo(stuMap);
								placementTaskDao.deleteTPlStudentinfo(stuMap);
							
							}
						}
						
						//t_pl_studentinfowaitforplacement数据更新
						if(!"".equals(wishId)){
							Map<String,Object> map3 = new HashMap<String,Object>();
							map3.put("termInfo",termInfo);
							map3.put("schoolId", schoolId);
							map3.put("accountId", accountId);
							String classId = placementTaskDao.getStudentClassInfo(map3).get(0).get("classId").toString();
							map3.put("placementId", placementIdTiny);
							map3.put("subjectIdsStr", wishId);
							String openClassTaskId = placementTaskDao.getopenClassTaskId(map3);
							//删除t_pl_studentinfowaitforplacement数据
							map3.put("classId", classId);
							placementTaskDao.deleteTPlStudentinfowaitforplacement(map3);
							map3.put("openClassTaskId", openClassTaskId);
							placementTaskDao.insertTPlStudentinfowaitforplacement(map3);

						}
					}
				}
			}
		}
		catch (Exception ex) {
		  txManager.rollback(status);
		  throw ex;
		}
		
		txManager.commit(status);
		return jsonList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public JSONObject insertNoDividedStuClassInfo(String schoolId,String termInfo,String placementId){
		//TODO 返回值格式需要调整，排课的接口需要分班类型
		JSONObject jsonResult = new JSONObject();
		List<JSONObject> allStuClassInfo = new ArrayList<JSONObject>();
		try{
			if(StringUtils.isBlank(schoolId)||StringUtils.isBlank(termInfo)||StringUtils.isBlank(placementId)){
				throw new RuntimeException("参数异常，请检查传入参数");
			}
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("termInfo", termInfo);
			map.put("schoolId", schoolId);
			map.put("placementId", placementId);
			PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(map);
			if(placementTask == null){
				throw new RuntimeException("无法获取到指定placementTask信息");
			}
			String usedGrade = placementTask.getUsedGrade();
			Integer placementType = placementTask.getPlacementType();
			//只有大走班和定二走一才用本方法调班，其他类型直接跳出
			if(placementType==3||placementType==4){
				TPlDezySettings tPlDezySettings = placementTaskDao.getTPlDezySet(map);
				Integer maxClassNum = tPlDezySettings.getMaxClassNum();
				String wfId = tPlDezySettings.getWfId();
				map.put("wfId", wfId);
				String isByElection = placementTaskDao.getWfInfo(map);
				//accountId、zhName、subjectIds
				List<JSONObject> zhList = new ArrayList<JSONObject>();
				Map<String,String> getZhListByWfid = new HashMap<String,String>();
				getZhListByWfid.put("schoolId", schoolId);
				getZhListByWfid.put("wfId", wfId);
				if("1".equals(isByElection)){
					 zhList = placementTaskDao.getByZhStudentToThirdByAccountId(getZhListByWfid);
				}else if("0".equals(isByElection)){
					 zhList = placementTaskDao.getZhStudentToThirdByAccountId(getZhListByWfid);
				}else{
					throw new RuntimeException("对应选科补选类型isByElection:"+isByElection+" 有误");
				}
				
				//学选交叉情况
				List<TPlConfIndexSubs> tPlConfIndexSubsList = placementTaskDao.getConfIndexSubsList(map);
				//Key为在一个时间点上课的，二级Map的Key为学选，三级的Key为seq和subIdList
				Map<String,Map<String,Map<String,Object>>> crossBaseInfos = new HashMap<String,Map<String,Map<String,Object>>>();
				for(TPlConfIndexSubs tPlConfIndexSubs:tPlConfIndexSubsList){
					String crossKey =  tPlConfIndexSubs.getIdx()+","+tPlConfIndexSubs.getReSeq();
					if(!crossBaseInfos.containsKey(crossKey)){
						Map<String,Map<String,Object>> crossSecondInfos = new HashMap<String,Map<String,Object>>();
						Map<String,Object> crossDetailedInfos = new HashMap<String,Object>();
						crossDetailedInfos.put("seq", tPlConfIndexSubs.getSeq());
						List<String> subIdList = new ArrayList<String>();
						subIdList = Arrays.asList(tPlConfIndexSubs.getSubIds().split(","));
						crossDetailedInfos.put("subIdList", subIdList);
						String isOpt = tPlConfIndexSubs.getIsOpt()+"";
						crossSecondInfos.put(isOpt, crossDetailedInfos);
						crossBaseInfos.put(crossKey, crossSecondInfos);
					}else{
						Map<String,Map<String,Object>> crossSecondInfos = crossBaseInfos.get(crossKey);
						Map<String,Object> crossDetailedInfos = new HashMap<String,Object>();
						crossDetailedInfos.put("seq", tPlConfIndexSubs.getSeq());
						List<String> subIdList = new ArrayList<String>();
						subIdList = Arrays.asList(tPlConfIndexSubs.getSubIds().split(","));
						crossDetailedInfos.put("subIdList", subIdList);
						String isOpt = tPlConfIndexSubs.getIsOpt()+"";
						crossSecondInfos.put(isOpt,crossDetailedInfos);
						crossBaseInfos.put(crossKey, crossSecondInfos);
					}
				}				
				
				//Key为选考的序列+科目，Value的Key为学考时间点，Value为学考冲突科目List
				Map<String,Map<Integer,List<String>>> conflictInfo = new HashMap<String,Map<Integer,List<String>>>();
				for(Map.Entry<String,Map<String,Map<String,Object>>> entry:crossBaseInfos.entrySet()){
					Map<String,Map<String,Object>> crossSecondInfos = entry.getValue();
					Map<String,Object> optSubjectCrosss = crossSecondInfos.get("1");
					Map<String,Object> proSubjectCrosss = crossSecondInfos.get("2");
					Integer proseq =  Integer.parseInt(proSubjectCrosss.get("seq").toString());
					List<String> proSubIdList = (List<String>) proSubjectCrosss.get("subIdList");
					Map<Integer,List<String>> proBeCrossList = new HashMap<Integer,List<String>>();
					proBeCrossList.put(proseq, proSubIdList);
					
					String optseq =  optSubjectCrosss.get("seq").toString();
					List<String> subIdList = (List<String>) optSubjectCrosss.get("subIdList");
					for(String subId:subIdList){
						String Key = optseq+","+subId;
						conflictInfo.put(Key, proBeCrossList);
					}
				}
				
				List<TPlDezySubjectcompStudent> tPlDezySubjectcompStudentList = placementTaskDao.getSubjectcompStuList(map);
				Map<String,String> compStudents = new HashMap<String,String>();
				for(TPlDezySubjectcompStudent tPlDezySubjectcompStudent:tPlDezySubjectcompStudentList){
					String accountId = tPlDezySubjectcompStudent.getStudentId();
					if(!compStudents.containsKey(accountId)){
						compStudents.put(accountId, accountId);
					}
				}
				//获取班级信息(并且遍历班级信息得到科目信息以及学校类型(6选3还是7选3))
				List<TPlDezyClass> tDezyClassList = placementTaskDao.getDezyClassList(map);
				List<String> allSubjectList = new ArrayList<String>();
				Map<String,String> allSubjectS = new HashMap<String,String>();
				Integer optSubjectNum = 0;
				Integer proSubjectNum = 0;
				for(TPlDezyClass tDezyClass:tDezyClassList){
					if(tDezyClass.getTclassType()==6){
						continue;
					}
					String subjectId = tDezyClass.getSubjectId();
					if(!allSubjectS.containsKey(subjectId)){
						allSubjectS.put(subjectId, subjectId);
					}
					if(tDezyClass.getTclassLevel()==1){
						optSubjectNum = Math.max(optSubjectNum,tDezyClass.getClassSeq());
					}else if(tDezyClass.getTclassLevel()==2){
						if(placementType==3){
							proSubjectNum = Math.max(proSubjectNum,tDezyClass.getClassSeq());
						}else if(placementType==4){
							proSubjectNum = Math.max(proSubjectNum,tDezyClass.getClassSeq()-3);
						}
					}
				}
				
				for(Map.Entry<String,String> entry:allSubjectS.entrySet()){
					allSubjectList.add(entry.getKey());
				}
				Collections.sort(allSubjectList);
				
				//需要重新分班的学生信息
				Map<String,List<String>> needToDistribution = new HashMap<String,List<String>>();
				for(JSONObject zhStudentInfo:zhList){
					String accountId = (String) zhStudentInfo.get("accountId");
					if(!compStudents.containsKey(accountId)){
						String sbjectIds = (String) zhStudentInfo.get("subjectIds");
						List<String> subjectIdList = Arrays.asList(sbjectIds.split(","));
						  Collections.sort(subjectIdList);
						needToDistribution.put(accountId, subjectIdList);
					}
				}
				
				//生成Table保存每个时间点每个科目的班级信息，并将List<TPlDezyClass>按照人数排升序
				Table<String, Integer, List<TPlDezyClass>> columnSubjectInfo = HashBasedTable.create();
				for(int i = 0;i<allSubjectList.size();i++){
					for(int column=1;column<=optSubjectNum+proSubjectNum;column++){
						columnSubjectInfo.put(allSubjectList.get(i), column, new ArrayList<TPlDezyClass>());
					}
				}
				
				//获取课表中有调整的教学班，这些教学班中不能添加学生
				List<String> tclassIdList = scheduleExternalService.getChangedTclassList(schoolId, placementId);
				
				for(TPlDezyClass tPlDezyClass:tDezyClassList){
					String tclassId = tPlDezyClass.getTclassId();
					Boolean ifCanUse = true;
					//如果是被调动的班级时候，不加到columnSubjectInfo里面
					for(String schTclassId:tclassIdList){
						if(schTclassId.equals(tclassId)){
							ifCanUse = false;
							break;
						}
					}
					if(!ifCanUse){
						continue;
					}
					Integer tclassType  = tPlDezyClass.getTclassType();
					if(tclassType==7){
						
						String subjectId = tPlDezyClass.getSubjectId();
						Integer column = 0;
						if(placementType ==3){
							if(tPlDezyClass.getTclassLevel()==1){
								column = tPlDezyClass.getClassSeq();
							}else if(tPlDezyClass.getTclassLevel()==2){
								column = optSubjectNum+tPlDezyClass.getClassSeq();
							}
						}else if(placementType ==4){
							column = tPlDezyClass.getClassSeq();
						}
						List<TPlDezyClass> tPlDezyClassList = columnSubjectInfo.get(subjectId,column);
						tPlDezyClassList.add(tPlDezyClass);
						columnSubjectInfo.put(subjectId, column, tPlDezyClassList);
					}
				}
				
				for(int i=0;i<allSubjectList.size();i++){
					for(int column=1;column<=optSubjectNum+proSubjectNum;column++){
						String subjectId = allSubjectList.get(i);
						List<TPlDezyClass> tPlDezyClassList = columnSubjectInfo.get(subjectId,column);
						if(tPlDezyClassList.size()>1){
							Collections.sort(tPlDezyClassList, new Comparator<TPlDezyClass>(){
							  @Override
							  public int compare(TPlDezyClass o1, TPlDezyClass o2) {
							  int Num1 = o1.getTclassNum();
							  int Num2 = o2.getTclassNum();
							  return Integer.compare(Num1, Num2);
							  }
							});
						}
					}
				}
				//开始遍历学生进行分班
				try{
					for(Map.Entry<String,List<String>> entry:needToDistribution.entrySet()){
						String accountId = entry.getKey();
						
						List<String> optSubjectList = entry.getValue();
						Collections.sort(optSubjectList);
						
						//获取选考科目和学考科目，用数组类型，方便后面做遍历
						String[] optSubjectIds = new String[optSubjectNum];
						String[] proSubjectIds = new String[proSubjectNum];
						for(int i=0;i<optSubjectList.size();i++){
							optSubjectIds[i] = optSubjectList.get(i);
						}
						Integer i = 0;
						for(String allSubject:allSubjectList){
							Boolean ifAdd = true;
							for(String optSubjectId:optSubjectIds){
								if(allSubject.equals(optSubjectId)){
									ifAdd = false;
									break;
								}
							}
							if(ifAdd){
								proSubjectIds[i] = allSubject;
								i++;
							}
						}
						List<Map<Integer,String>> optsubjectzhList = new ArrayList<Map<Integer,String>>();
						List<Map<Integer,String>> prosubjectzhList = new ArrayList<Map<Integer,String>>();
						optsubjectzhList = nextPer(optSubjectIds,0,optsubjectzhList);
						prosubjectzhList = nextPer(proSubjectIds,0,prosubjectzhList);
						if(placementType==3){
							//根据accountId获取行政班id，保存数据的时候，不同行政班id会对应不同subjectCompId
							Map<String,Object> map2 = new HashMap<String,Object>();
							map2.put("termInfo", termInfo);
							map2.put("schoolId", schoolId);
							map2.put("accountId", accountId);
							List<JSONObject> stuToClassIdInfo = placementTaskDao.getStudentClassInfo(map2);
							if(stuToClassIdInfo.size()<=0||stuToClassIdInfo.get(0).get("classId")==null){
								throw new RuntimeException("学生信息获取班级失败");
							}
							String adminClassId = stuToClassIdInfo.get(0).get("classId").toString();
							Map<String,Object> LastResult = new HashMap<String,Object>();
							JSONObject json = new JSONObject();
							List<TPlDezyClass> tPlDezyClassList = new ArrayList<TPlDezyClass>();
							json = getOptimalClassAssignment(maxClassNum,optSubjectNum,optsubjectzhList,prosubjectzhList,conflictInfo,columnSubjectInfo,tPlDezyClassList);
							LastResult = (Map<String, Object>) json.get("LastResult");
							//保存学生分班信息
							List<JSONObject> stuAllClassInfo = saveStuClassInfo(termInfo,schoolId,placementId,usedGrade,accountId,adminClassId,placementType,optSubjectList,allSubjectList,LastResult);
							allStuClassInfo.addAll(stuAllClassInfo);
						}else if(placementType ==4){						
							//因为有多步骤，而且还有调用外部方法，用一个Boolean类型值判断是否已经分好班级，如果已经分好的话下面不再执行
							Boolean ifArrange = false;
							String subjects = "";
							for(String optSubject:optSubjectList){
								if("".equals(subjects)){
									subjects = optSubject;
								}else{
									subjects = subjects+","+ optSubject;
								}	
							}
							//先找组合完全一样的
							map.put("compFrom", subjects);
							//考虑到可能有多个同组合出现，要选最优的，bestsubjectCompId保存的是最优的subjectCompId
							String bestsubjectCompId ="";
							//先初始化成一个理论上的最大值，实际分班的差值都会比这个小，最后要取一个最小的值
							Integer bestdifferNum = 0;
							List<TPlDezySubjectcomp> SubjectcompList = placementTaskDao.getSubjectcompList(map);
							for(TPlDezySubjectcomp Subjectcomp:SubjectcompList){
								String subjectCompId = Subjectcomp.getSubjectCompId();
								map.put("subjectCompId", subjectCompId);
								List<TPlDezyClass> tPlDezyClassList = placementTaskDao.getTPlDezyClassBySubjectCompId(map);
								Boolean ifSuccess = true;
								Integer differNum = 0;
								for(TPlDezyClass tPlDezyClass:tPlDezyClassList){
									//TODO 必须不在被调整班级中，实际上应该还要有行政班不一样的条件，但暂时不考虑
									String tclassId = tPlDezyClass.getTclassId();
									for(String schtClassId:tclassIdList){
										if(tclassId.equals(schtClassId)){
											ifSuccess = false;
											break;
										}
									}
									Integer classNum = tPlDezyClass.getTclassNum();
									if(classNum>=maxClassNum){
										ifSuccess = false;
										break;
									}
									differNum += maxClassNum-classNum;
								}
								if(ifSuccess){
									if(bestdifferNum<differNum){
										bestsubjectCompId = subjectCompId;
										bestdifferNum = differNum;
										ifArrange = true;
									}
								}
							}
							if(ifArrange){
								Map<String,Object> map2 = new HashMap<String,Object>();
								map2.put("termInfo", termInfo);
								map2.put("subjectCompId", bestsubjectCompId);
								map2.put("placementId", placementId);
								map2.put("schoolId", schoolId);
								List<TPlDezyClass> tPlDezyClassList = placementTaskDao.getTPlDezyClassBySubjectCompId(map2);
								String adminClassId ="";
								Map<String,Object> LastResult = new HashMap<String,Object>();
								for(TPlDezyClass tPlDezyClass:tPlDezyClassList){
									int tclassType = tPlDezyClass.getTclassType();
									if(tclassType==6){
										adminClassId = tPlDezyClass.getTclassId();
									}else if(tclassType==7){
										String classSeq = tPlDezyClass.getClassSeq()+"";
										String tclassId = tPlDezyClass.getTclassId();
										String subjectId = tPlDezyClass.getSubjectId();
										LastResult.put(classSeq, subjectId+","+tclassId);
									}
								}
								List<JSONObject> stuAllClassInfo = saveStuClassInfo(termInfo,schoolId,placementId,usedGrade,accountId,adminClassId,placementType,optSubjectList,allSubjectList,LastResult);
								allStuClassInfo.addAll(stuAllClassInfo);			
								continue;
							}
							
							
							//再找能定二的组合行政班
							List<JSONObject> ClassToTClassRelationList = new ArrayList<JSONObject>();
							ClassToTClassRelationList = placementTaskDao.getClassToTClassRelation(map);
							if(ClassToTClassRelationList.size()<=0){
								throw new RuntimeException("获取行政班对应教学班信息失败");
							}
							//遍历ClassToTClassRelation找到能完全以行政班开班的教学班(只出现一次的教学班)
							Map<String,Integer> teachClassOccurrence = new HashMap<String,Integer>();
							for(JSONObject ClassToTClassRelation:ClassToTClassRelationList){
								String tClassId = ClassToTClassRelation.get("tclassId").toString();
								//教学班被调动的不能使用
								Boolean ifCanUse = true;
								for(String schtClassId:tclassIdList){
									if(tClassId.equals(schtClassId)){
										ifCanUse = false;
										break;
									}
								}
								if(!ifCanUse){
									continue;
								}
								
								if(!teachClassOccurrence.containsKey(tClassId)){
									teachClassOccurrence.put(tClassId, 1);
								}else{
									Integer num = teachClassOccurrence.get(tClassId) + 1;
									teachClassOccurrence.put(tClassId, num);
								}
							}		
							//保存行政班对应教学班信息，Key是行政班id，为了方便找到行政班用的
							Map<String,ClassFixedArrange> ClassToTClassRelationMap = new HashMap<String,ClassFixedArrange>();
							for(JSONObject ClassToTClassRelation:ClassToTClassRelationList){
								String tclassId = ClassToTClassRelation.get("tclassId").toString();
								if(teachClassOccurrence.get(tclassId)==1){
									String classId = ClassToTClassRelation.get("classId").toString();
									Integer AdminNum =  (Integer) ClassToTClassRelation.get("AdminNum");
									Integer TeachNum = (Integer) ClassToTClassRelation.get("TeachNum");
									String subjectId = ClassToTClassRelation.get("subjectId").toString();
									Integer tclassLevel = (Integer) ClassToTClassRelation.get("tclassLevel");
									Integer classseq = (Integer) ClassToTClassRelation.get("classseq");
									ClassFixedArrange classFixedArrange = new ClassFixedArrange();
									List<TPlDezyClass> TPlDezyClassList = new ArrayList<TPlDezyClass>();
									TPlDezyClass tlDezyClass = new TPlDezyClass();
									List<String> subjectList = new ArrayList<String>();
									if(ClassToTClassRelationMap.containsKey(classId)){
										classFixedArrange = ClassToTClassRelationMap.get(classId);
										if(null != classFixedArrange.getSubjectList()){
											subjectList = classFixedArrange.getSubjectList();
										}
										if(null != classFixedArrange.getTPlDezyClassList()){
											TPlDezyClassList = classFixedArrange.getTPlDezyClassList();
										}
									}								
									if(tclassLevel ==1){
										subjectList.add(subjectId);
									}
									tlDezyClass.setTclassId(tclassId);
									tlDezyClass.setTclassNum(TeachNum);
									tlDezyClass.setSubjectId(subjectId);
									tlDezyClass.setTclassLevel(tclassLevel);
									tlDezyClass.setClassSeq(classseq);
									TPlDezyClassList.add(tlDezyClass);
									classFixedArrange.setClassId(classId);
									classFixedArrange.setAdminNum(AdminNum);
									classFixedArrange.setSubjectList(subjectList);
									classFixedArrange.setTPlDezyClassList(TPlDezyClassList);
									ClassToTClassRelationMap.put(classId, classFixedArrange);
								}
							}
							//获取行政班的组合信息
							Map<String,Object> map3 = new HashMap<String,Object>();
							map3.put("termInfo", termInfo);
							map3.put("placementId", placementId);
							map3.put("schoolId", schoolId);
							List<TPlDezySubjectcomp> tPlDezySubjectcompList = placementTaskDao.getSubjectcompList(map3);
							for(TPlDezySubjectcomp tPlDezySubjectcomp:tPlDezySubjectcompList){
								String classId = tPlDezySubjectcomp.getClassId();
								String compForm = tPlDezySubjectcomp.getCompFrom();
								if(ClassToTClassRelationMap.containsKey(classId)){
									ClassFixedArrange classFixedArrange = ClassToTClassRelationMap.get(classId);
									List<String> compFormList = new ArrayList<String>();
									if(null != classFixedArrange.getCompFormList()){
										compFormList = classFixedArrange.getCompFormList();
									}
									compFormList.add(compForm);
									classFixedArrange.setCompFormList(compFormList);
									ClassToTClassRelationMap.put(classId, classFixedArrange);
								}
							}
							//看能定二的情况,先计算每个行政班的定二还是定一情况，优先定二，不行再定一
							Map<Integer,List<ClassFixedArrange>> ClassFixedArranges = new HashMap<Integer,List<ClassFixedArrange>>();
							for(Map.Entry<String,ClassFixedArrange> ClassToTClassRelation:ClassToTClassRelationMap.entrySet()){
								ClassFixedArrange classFixedArrange = ClassToTClassRelation.getValue();
								Integer sameSubject = 0;
								List<String> subjectList = classFixedArrange.getSubjectList();
								for(String subject:subjectList){
									for(String optSubject:optSubjectList){
										if(subject.equals(optSubject)){
											sameSubject++;
										}
									}
								}
								if(!ClassFixedArranges.containsKey(sameSubject)){
									List<ClassFixedArrange> classFixedArrangeList = new ArrayList<ClassFixedArrange>();
									classFixedArrangeList.add(classFixedArrange);
									ClassFixedArranges.put(sameSubject, classFixedArrangeList);
								}else{
									List<ClassFixedArrange> classFixedArrangeList = ClassFixedArranges.get(sameSubject);
									classFixedArrangeList.add(classFixedArrange);
									ClassFixedArranges.put(sameSubject, classFixedArrangeList);
								}
							}
						/*
							//打印ClassFixedArranges看结果
							for(Map.Entry<Integer,List<ClassFixedArrange>> entry1:ClassFixedArranges.entrySet()){
								Integer Num = entry1.getKey();
								List<ClassFixedArrange> ClassFixedArrangeList = entry1.getValue();
								for(ClassFixedArrange classFixedArrange:ClassFixedArrangeList){
									String classId = classFixedArrange.getClassId();
									Integer AdminNum = classFixedArrange.getAdminNum();
									List<String> subjectList = classFixedArrange.getSubjectList();
									List<String> compFormList = classFixedArrange.getCompFormList();
									List<TPlDezyClass> TPlDezyClassList = classFixedArrange.getTPlDezyClassList();
									String subjectIds ="";
									String compForms ="";
									for(String subject:subjectList){
										subjectIds = subjectIds+ subject;
									}
									for(String compForm:compFormList){
										compForms = compForms+compForm+";";
									}
									System.out.println("");
									System.out.println("");
									System.out.println("Num为"+Num+"的基本班级信息分配");
									System.out.print("classId:"+classId+"    ");
									System.out.print("AdminNum:"+AdminNum+"    ");
									System.out.print("subjectIds:"+subjectIds+"    ");
									System.out.println("compForms:"+compForms+"    ");
									System.out.println("教学班分配");
									for(TPlDezyClass tPlDezyClass:TPlDezyClassList){
										String tclassId = tPlDezyClass.getTclassId();
										Integer TeachNum = tPlDezyClass.getTclassNum();
										String subjectId = tPlDezyClass.getSubjectId();
										Integer tclassLevel = tPlDezyClass.getTclassLevel();
										Integer classseq = tPlDezyClass.getClassSeq();
										System.out.print("tclassId:"+tclassId+"   ");
										System.out.print("TeachNum:"+TeachNum+"   ");
										System.out.print("subjectId:"+subjectId+"   ");
										System.out.print("tclassLevel:"+tclassLevel+"   ");
										System.out.println("classseq:"+classseq);
									}
								}
							}
							*/
							
							//先以定二组合来看
							if(null !=ClassFixedArranges.get(2)){
								List<ClassFixedArrange>  DEClassFixedArrangeList = ClassFixedArranges.get(2);
								List<JSONObject> DEjsonList = new ArrayList<JSONObject>();
								Map<String,Object> DELastResult = new HashMap<String,Object>();
								String DELastAdminClassId = "";
								DEjsonList = findBestClassAssignmentByAdminClass(maxClassNum,optSubjectList,
									optsubjectzhList,prosubjectzhList,DEClassFixedArrangeList,conflictInfo,columnSubjectInfo);
								for(JSONObject Json:DEjsonList){
									Boolean ifSucccess = (Boolean) Json.get("ifSucccess");
									Map<String,Object> LastResultOld = (Map<String, Object>) Json.get("LastResult");
									String adminClassId = Json.get("adminClassId").toString();
									if(ifSucccess){
										ifArrange = true;
										if(DELastResult.isEmpty()){
											DELastResult = LastResultOld;
											DELastAdminClassId = adminClassId;
										}else{
											Integer differNum = (Integer) DELastResult.get("differNum");
											Integer differNumOld = (Integer) LastResultOld.get("differNum");
											if(differNumOld>differNum){
												DELastResult = LastResultOld;
												DELastAdminClassId = adminClassId;
											}
										}
									}
								}
								//如果DELastResult.size>0的时候，以定二方式保存结果，并且continue；开始下一个学生的处理
								if(ifArrange){
									List<JSONObject> stuAllClassInfo = saveStuClassInfo(termInfo,schoolId,placementId,usedGrade,accountId,DELastAdminClassId,placementType,optSubjectList,allSubjectList,DELastResult);
									allStuClassInfo.addAll(stuAllClassInfo);
									continue;
								}
							}
							
							//上面也不行的话就要以定一来算
							if(null!=ClassFixedArranges.get(1)){
								List<ClassFixedArrange>  DYClassFixedArrangeList = ClassFixedArranges.get(1);
								//接收findBestClassAssignmentByAdminClass方法传回来的各行政班下的最优解
								List<JSONObject> DYjsonList = new ArrayList<JSONObject>();
								//保存成功情况下的最优解
								Map<String,Object> DYLastResult = new HashMap<String,Object>();
								//保存失败情况下的最优解
								Map<String,Object> DYFailLastResult = new HashMap<String,Object>();
								String DYLastAdminClassId = "";
								DYjsonList = findBestClassAssignmentByAdminClass(maxClassNum,optSubjectList,
										optsubjectzhList,prosubjectzhList,DYClassFixedArrangeList,conflictInfo,columnSubjectInfo);
								for(JSONObject Json:DYjsonList){
									if(DYjsonList.size()>0){
										ifArrange = true;
									}else{
										throw new RuntimeException("没有找到能分配的定二班级");
									}
									Boolean ifSucccess = (Boolean) Json.get("ifSucccess");
									Map<String,Object> LastResultOld = (Map<String, Object>) Json.get("LastResult");
									String adminClassId = Json.get("adminClassId").toString();
									if(ifSucccess){
										if(DYLastResult.isEmpty()){
											DYLastResult = LastResultOld;
											DYLastAdminClassId = adminClassId;
										}else{
											Integer differNum = (Integer) DYLastResult.get("differNum");
											Integer differNumOld = (Integer) LastResultOld.get("differNum");
											if(differNumOld>differNum){
												DYLastResult = LastResultOld;
												DYLastAdminClassId = adminClassId;
											}
										}
									}else{
										if(DYFailLastResult.isEmpty()){
											DYFailLastResult = LastResultOld;
											DYLastAdminClassId = adminClassId;
										}else{
											Integer differNum = (Integer) DYFailLastResult.get("differNum");
											Integer differNumOld = (Integer) LastResultOld.get("differNum");
											if(differNumOld>differNum){
												DYFailLastResult = LastResultOld;
												DYLastAdminClassId = adminClassId;
											}
										}
									}
								}
								//定一的情况下保存分班数据(定一只是考虑到如果前面两种都超班额的时候备用方案)
								if(ifArrange){
									if(DYLastResult.isEmpty()){
										List<JSONObject> stuAllClassInfo = saveStuClassInfo(termInfo,schoolId,placementId,usedGrade,accountId,DYLastAdminClassId,placementType,optSubjectList,allSubjectList,DYFailLastResult);
										allStuClassInfo.addAll(stuAllClassInfo);
									}else{
										List<JSONObject> stuAllClassInfo = saveStuClassInfo(termInfo,schoolId,placementId,usedGrade,accountId,DYLastAdminClassId,placementType,optSubjectList,allSubjectList,DYLastResult);
										allStuClassInfo.addAll(stuAllClassInfo);
									}
								}
							}						
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException("只支持大走班和定二走一模式下的调班");
			}
			jsonResult.put("placementType", placementType);
			jsonResult.put("allStuClassInfo", allStuClassInfo);
			
		}catch(Exception e){
			throw new RuntimeException("学生调班失败："+e.getMessage());
		}
		return jsonResult;
	}
	
	/**
	 * 根据传过来的科目以及固定科目，生成遍历组
	 * 返回值：每个List的值是对应一类情况，Map中的Integer分别为序号，Vlaue为对应的科目
	 * */
	public List<Map<Integer,String>> nextPer(String[] subjectIdList,int start,List<Map<Integer,String>> subjectzhList){
		if(start==subjectIdList.length-1){
			Map<Integer,String> subjectzh = new HashMap<Integer,String>();
			for(int i =0;i<subjectIdList.length;i++){
				subjectzh.put(i+1, subjectIdList[i]);
			}
			subjectzhList.add(subjectzh);
		}
		for(int i=start;i<subjectIdList.length;i++){
			//start代表的是每一个子序列的第一个位置，我们每一层递归的任务都只有一个：
			//枚举该层子序列第一个位置可以取的值
			String temp=subjectIdList[start];
			subjectIdList[start]=subjectIdList[i];
			subjectIdList[i]=temp;
			//该层递归的子序列第一个位置已经确定了，所以又可以往下再分
			nextPer(subjectIdList,start+1,subjectzhList);
			//把第该层子序列第一个位置的值换成另外一个值，所以要交换回来
			temp=subjectIdList[start];
			subjectIdList[start]=subjectIdList[i];
			subjectIdList[i]=temp;
		}	
		return subjectzhList;
	}
	
	/**
	 * 根据学选的全遍历情况找到最优的解；
	 * 定二走一模式下如果有受到行政班影响导致科目必排位置的，提前将optsubjectzhList和prosubjectzhList不符合要求的值删掉之后再调用方法
	 * */
	public JSONObject getOptimalClassAssignment(Integer maxClassNum,Integer optSubjectNum,
			List<Map<Integer,String>> optsubjectzhList,List<Map<Integer,String>> prosubjectzhList,
			Map<String,Map<Integer,List<String>>> conflictInfo,Table<String, Integer, List<TPlDezyClass>> columnSubjectInfo,List<TPlDezyClass> DtPlDezyClassList){
		JSONObject json = new JSONObject();
		Map<String,Object> LastResult = new HashMap<String,Object>();
		Map<String,Object> successBestResult = new HashMap<String,Object>();
		Map<String,Object> failBestResult = new HashMap<String,Object>();
		for(Map<Integer,String> optsubjectzh:optsubjectzhList){
			for(Map<Integer,String> prosubjectzh:prosubjectzhList){
				Boolean ifSuccess = true;
				Integer differNum = 0;
				Boolean ifCross = false;
				Map<String,Object> epicycleResult = new HashMap<String,Object>();
				//最开始就判断现在的学选科目分配会不会有交叉，有交叉的话continue(如果没有学选交叉的信息，直接跳过这一步)
				if(!conflictInfo.isEmpty()){
					for(Map.Entry<Integer,String> optsubject:optsubjectzh.entrySet()){
						String Key = optsubject.getKey()+","+optsubject.getValue();
						if(!conflictInfo.containsKey(Key)){
							continue;
						}else{
							Map<Integer,List<String>> proSubjectIdLists = conflictInfo.get(Key);
							for(Map.Entry<Integer,List<String>> proSubjectIdList:proSubjectIdLists.entrySet()){
								String proSubjectId = prosubjectzh.get(proSubjectIdList.getKey());
								List<String> proSubjectIdCrossList = proSubjectIdList.getValue();
								for(String proSubjectIdCross:proSubjectIdCrossList){
									if(proSubjectIdCross.equals(proSubjectId)){
										ifCross=true;
										break;
									}
								}
							}
						}
					}
				}
				if(ifCross){
					continue;
				}
				Boolean ifCanUse = true;
				//选考教学班是完全行政班的，直接安排到这个教学班中，如果不是的话，在这个时间点找人数最优的教学班
				for(Map.Entry<Integer,String> optsubject:optsubjectzh.entrySet()){
					Integer classNum = 0;
					String subjectTclassId ="";
					String subjectId = optsubject.getValue();
					Integer orderNum = optsubject.getKey();
					Boolean ifDtclass = false;
					for(TPlDezyClass DtPlDezyClass:DtPlDezyClassList){
						String DsubjectId = DtPlDezyClass.getSubjectId();
						if(subjectId.equals(DsubjectId)){
							subjectTclassId = DtPlDezyClass.getTclassId();
							classNum = DtPlDezyClass.getTclassNum();
							ifDtclass = true;
							break;
						}
					}
					if(!ifDtclass){
						List<TPlDezyClass> tPlDezyClassList = columnSubjectInfo.get(subjectId, orderNum);
						if(tPlDezyClassList.size()<=0){
							ifCanUse = false;
							break;
						}
						classNum = columnSubjectInfo.get(subjectId, orderNum).get(0).getTclassNum();
						subjectTclassId = columnSubjectInfo.get(subjectId, orderNum).get(0).getTclassId();
					}
					String subjectArrage = subjectId+","+subjectTclassId;
					if(ifSuccess==true && classNum >=maxClassNum){
						ifSuccess = false;
					}
					differNum += maxClassNum - classNum;
					epicycleResult.put(orderNum.toString(), subjectArrage);
				}
				if(!ifCanUse){
					continue;
				}
				//学考教学班是完全行政班的，直接安排到这个教学班中，如果不是的话，在这个时间点找人数最优的教学班
				for(Map.Entry<Integer,String> prosubject:prosubjectzh.entrySet()){
					Integer classNum = 0;
					String subjectTclassId = "";
					Boolean ifDtclass = false;
					String subjectId = prosubject.getValue();
					Integer orderNum = prosubject.getKey() + optSubjectNum;
					for(TPlDezyClass DtPlDezyClass:DtPlDezyClassList){
						String DsubjectId = DtPlDezyClass.getSubjectId();
						if(subjectId.equals(DsubjectId)){
							subjectTclassId = DtPlDezyClass.getTclassId();
							classNum = DtPlDezyClass.getTclassNum();
							ifDtclass = true;
							break;
						}
					}
					if(!ifDtclass){
						List<TPlDezyClass> tPlDezyClassList = columnSubjectInfo.get(subjectId, orderNum);
						if(tPlDezyClassList.size()<=0){
							ifCanUse = false;
							break;
						}
						classNum = columnSubjectInfo.get(subjectId, orderNum).get(0).getTclassNum();
						subjectTclassId = columnSubjectInfo.get(subjectId, orderNum).get(0).getTclassId();
					}
					String subjectArrage = subjectId+","+subjectTclassId;
					if(ifSuccess==true && classNum >=maxClassNum){
						ifSuccess = false;
					}
					differNum += maxClassNum - classNum;
					epicycleResult.put(orderNum.toString(), subjectArrage);
				}
				if(!ifCanUse){
					continue;
				}
				//判断ifSuccess的状态，然后保存到对应的Map中，
				if(ifSuccess){
					if(successBestResult.isEmpty()){
						//把分班数据放进来
						successBestResult.putAll(epicycleResult);
						successBestResult.put("differNum", differNum);
					}else{
						//如果differNum>successBestResult.get("differNum"),替换
						Integer oldBestdifferNum = (Integer) successBestResult.get("differNum");
						if(differNum>oldBestdifferNum){
							successBestResult.putAll(epicycleResult);
							successBestResult.put("differNum", differNum);
						}
					}
				}else{
					if(failBestResult.isEmpty()){
						//把分班数据放进来
						failBestResult.putAll(epicycleResult);
						failBestResult.put("differNum", differNum);
					}else{
						//如果differNum>successBestResult.get("differNum"),替换
						Integer oldBestdifferNum = (Integer) failBestResult.get("differNum");
						if(differNum>oldBestdifferNum){
							failBestResult.putAll(epicycleResult);
							failBestResult.put("differNum", differNum);
						}
					}
				}
			}
		}
		Boolean ifSucccess = true;
		if(successBestResult.isEmpty()){
			ifSucccess = false;
			LastResult = failBestResult;
		}else{
			LastResult = successBestResult;
		}
		json.put("LastResult", LastResult);
		json.put("ifSucccess", ifSucccess);
		return json;
	}
	
	/**
	 * 根据行政班列表的教学班定二情况以及是否完全行政班开班，对optsubjectzhList、prosubjectzhList中的全遍历进行筛选
	 * 然后调用getOptimalClassAssignment方法获取本行政班下的最优解
	 * 说明：根据行政班找出能用的随机序列分配，然后调用getOptimalClassAssignment获取每个序列的最优解，最后得到一个总的最优解
	 * */
	@SuppressWarnings("unchecked")
	public List<JSONObject> findBestClassAssignmentByAdminClass(Integer maxClassNum,List<String> optSubjectList,
			List<Map<Integer,String>> optsubjectzhList,List<Map<Integer,String>> prosubjectzhList,
			List<ClassFixedArrange> DEClassFixedArrangeList,Map<String,Map<Integer,List<String>>> conflictInfo,
			Table<String, Integer, List<TPlDezyClass>> columnSubjectInfo){
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for(ClassFixedArrange DEClassFixedArrange:DEClassFixedArrangeList){
			List<TPlDezyClass> DtPlDezyClassList = new ArrayList<TPlDezyClass>();
			Integer AdminNum = DEClassFixedArrange.getAdminNum();
			if(AdminNum<maxClassNum){
				String adminClassId = DEClassFixedArrange.getClassId();
				Integer AdminDifferNum = maxClassNum-AdminNum;
				List<TPlDezyClass> TPlDezyClassList = DEClassFixedArrange.getTPlDezyClassList();
				List<Map<Integer,String>> optsubjectzhNewList = new ArrayList<Map<Integer,String>>(optsubjectzhList);
				List<Map<Integer,String>> prosubjectzhNewList = new ArrayList<Map<Integer,String>>(prosubjectzhList);
				List<Map<Integer,String>> needToDeleteOptList = new ArrayList<Map<Integer,String>>();
				List<Map<Integer,String>> needToDeleteProList = new ArrayList<Map<Integer,String>>();
				//如果定二的科目里面有科目不是自己要上的选考科目，直接跳过这个科目，不做optsubjectzhNewList的删除
				if(TPlDezyClassList.size()>0){
					for(TPlDezyClass tPlDezyClass:TPlDezyClassList){
						Integer tclassLevel = tPlDezyClass.getTclassLevel();
						Integer classseq = tPlDezyClass.getClassSeq();
						Integer order = classseq;
						String subjectId = tPlDezyClass.getSubjectId();
						String tclassId = tPlDezyClass.getTclassId();
						if(tclassLevel==1){
							//如果以行政班开课的科目不在自己的选考科目单的话，跳过
							Boolean ifExise = false;
							for(String optSubject:optSubjectList){
								if(optSubject.equals(subjectId)){
									ifExise = true;
									break;
								}
							}
							if(!ifExise){
								continue;
							}
							//如果是需要以行政班开班的教学班，需要保存
							DtPlDezyClassList.add(tPlDezyClass);
							for(Map<Integer,String> optsubjectzhNew:optsubjectzhNewList){
								String zhSubjectId = optsubjectzhNew.get(order);
								if("".equals(zhSubjectId)||zhSubjectId==null||(!subjectId.equals(zhSubjectId))){
									needToDeleteOptList.add(optsubjectzhNew);
								}
							}
						}else if(tclassLevel==2){
							//如果科目在自己的选考科目单的话(因为tclassLevel是学考，也跳过)
							Boolean ifExise = false;
							for(String optSubject:optSubjectList){
								if(optSubject.equals(subjectId)){
									ifExise = true;
									break;
								}
							}
							if(ifExise){
								continue;
							}
							//如果是需要以行政班开班的教学班，需要保存
							DtPlDezyClassList.add(tPlDezyClass);
							order = classseq - optSubjectList.size();
							for(Map<Integer,String> prosubjectzhNew:prosubjectzhNewList){
								String zhSubjectId = prosubjectzhNew.get(order);
								if("".equals(zhSubjectId)||zhSubjectId==null||(!subjectId.equals(zhSubjectId))){
									needToDeleteProList.add(prosubjectzhNew);
								}
							}
						}
						
					}
					if(needToDeleteOptList.size()>0){
						optsubjectzhNewList.removeAll(needToDeleteOptList);
					}
					if(needToDeleteProList.size()>0){
						prosubjectzhNewList.removeAll(needToDeleteProList);
					}
					//必须optsubjectzhNewList和prosubjectzhNewList的size都大于0才执行
					if(optsubjectzhNewList.size()<=0||prosubjectzhNewList.size()<=0){
						continue;
					}
					JSONObject Json = getOptimalClassAssignment(maxClassNum,optSubjectList.size(),optsubjectzhNewList,prosubjectzhNewList,
							conflictInfo,columnSubjectInfo,DtPlDezyClassList);
					
					Map<String,Object> LastResultOld = (Map<String, Object>) Json.get("LastResult");
					if(LastResultOld.isEmpty()|| LastResultOld== null){
						return jsonList;
					}
					Integer differNumOld = (Integer) LastResultOld.get("differNum");
					Integer differNum = differNumOld+AdminDifferNum;
					LastResultOld.put("differNum", differNum);
					//保存定二科目(按compName的顺序)
					List<String> DESubjectList = new ArrayList<String>();
					List<String> unArrageSubjectId = new ArrayList<String>();
					if(null!=DEClassFixedArrange.getSubjectList()){
						for(String optSubject:optSubjectList){
							Boolean ifExist = false;
							for(int i=0;i<DEClassFixedArrange.getSubjectList().size();i++){
								String subjectId = DEClassFixedArrange.getSubjectList().get(i);
								if(subjectId.equals(optSubject)){
									ifExist = true;
									break;	
								}
							}
							if(ifExist){
								DESubjectList.add(optSubject);
							}else{
								unArrageSubjectId.add(optSubject);
							}
						}
						DESubjectList.addAll(unArrageSubjectId);
					}else{
						DESubjectList.addAll(optSubjectList);
					}
					LastResultOld.put("DESubjectList",DESubjectList);				
					Json.put("LastResult", LastResultOld);
					Json.put("adminClassId", adminClassId);
					jsonList.add(Json);
				}
			}
		}
		return jsonList;
	}
	
	/**
	 * 根据分班结果保存分班数据
	 * */
	@Transactional
	public List<JSONObject> saveStuClassInfo(String termInfo,String schoolId,String placementId,String usedGrade,String accountId,
			String adminClassId,Integer placementType,List<String> optSubjectList,List<String> allSubjectList,Map<String,Object> LastResult){
		List<JSONObject> classInfoList = new ArrayList<JSONObject>();
		try{
			Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfo);
			String userName = account.getName();
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("termInfo", termInfo);
			map.put("schoolId", schoolId);
			map.put("placementId", placementId);
			List<TPlDezySubjectcomp> SubjectcompList = placementTaskDao.getSubjectcompList(map);
			//先获取原本保存的subjectCompId与班级相关信息(后面和本学生要安排的班级进行对比，如果一样的话就安排在这个subjectCompId，没有一样的话就需要生成)
			List<SubjectcompToClassInfo> subjectcompToClassInfoList = new ArrayList<SubjectcompToClassInfo>();
			for(TPlDezySubjectcomp Subjectcomp:SubjectcompList){
				SubjectcompToClassInfo subjectcompToClassInfo = new SubjectcompToClassInfo();
				String subjectCompId = Subjectcomp.getSubjectCompId();
				Integer compNum = Subjectcomp.getCompNum();
				
				subjectcompToClassInfo.setSubjectCompId(subjectCompId);
				subjectcompToClassInfo.setCompNum(compNum);
				map.put("subjectCompId", subjectCompId);
				List<TPlDezyClass> tPlDezyClassListSave = new ArrayList<TPlDezyClass>();
				/*
				if(null !=subjectcompToClassInfo.gettPlDezyClassList()){
					tPlDezyClassListSave = subjectcompToClassInfo.gettPlDezyClassList();
				}
				*/
				List<TPlDezyClass> tPlDezyClassList = placementTaskDao.getTPlDezyClassBySubjectCompId(map);
				for(TPlDezyClass tPlDezyClass:tPlDezyClassList){
					if(tPlDezyClass.getTclassLevel()==0){
						String AdminClassId = tPlDezyClass.getTclassId();
						Integer AdminClassNum = tPlDezyClass.getTclassNum();
						subjectcompToClassInfo.setAdminClassId(AdminClassId);
						subjectcompToClassInfo.setAdminClassNum(AdminClassNum);
					}else{
						tPlDezyClassListSave.add(tPlDezyClass);
					}
				}
				if(placementType==3){
					subjectcompToClassInfo.setAdminClassId(adminClassId);
				}
				Collections.sort(tPlDezyClassListSave, new Comparator<TPlDezyClass>() {
					@Override
					public int compare(TPlDezyClass o1, TPlDezyClass o2) {
					String str1=o1.getSubjectId();
					String str2=o2.getSubjectId();
					if (str1.compareToIgnoreCase(str2)<0){  
						return -1;  
				    	}  
				    return 1;  
				    }
				});
				subjectcompToClassInfo.settPlDezyClassList(tPlDezyClassListSave);
				String classKey = subjectcompToClassInfo.getAdminClassId();
				for(TPlDezyClass tPlDezyClass:tPlDezyClassListSave){
					String classId = tPlDezyClass.getTclassId();
					classKey = classKey+","+classId;
				}
				subjectcompToClassInfo.setClassKey(classKey);
				subjectcompToClassInfoList.add(subjectcompToClassInfo);
			}
			//生成本次安排的ClassKey和开班情况
			String stuClassKey =adminClassId;
			List<Map<String,String>> subjectIdToClassIdList = new ArrayList<Map<String,String>>();
			for(int i=1;i<=allSubjectList.size();i++){
				String subjectIdToClassId = LastResult.get(i+"").toString();
				List<String> splitSubjectIdToClassId = Arrays.asList(subjectIdToClassId.split(","));
				if(splitSubjectIdToClassId.size()!=2){
					throw new RuntimeException("LastResult中科目拆分失败");
				}
				Map<String,String> subjectIdToClassIdMap = new HashMap<String,String>();
				subjectIdToClassIdMap.put("subjectId", splitSubjectIdToClassId.get(0));
				subjectIdToClassIdMap.put("tclassId", splitSubjectIdToClassId.get(1));
				subjectIdToClassIdList.add(subjectIdToClassIdMap);
			}
			Collections.sort(subjectIdToClassIdList, new Comparator<Map<String,String>>() {
				@Override
				public int compare(Map<String,String> o1, Map<String,String> o2) {
				String str1=o1.get("subjectId");
				String str2=o2.get("subjectId");
				if (str1.compareToIgnoreCase(str2)<0){  
					return -1;  
				}  
				return 1;  
				}
			});
			for(Map<String,String> subjectIdToClassId:subjectIdToClassIdList){
				String tclassId = subjectIdToClassId.get("tclassId");
				stuClassKey = stuClassKey+","+tclassId;
			}
			//遍历subjectcompToClassInfoList找是否有和本次相同的classKey
			String subjectCompId="";
			SubjectcompToClassInfo subjectcompToClassInfo = new SubjectcompToClassInfo();
			for(SubjectcompToClassInfo subjectcompToClass:subjectcompToClassInfoList){
				String classKey = subjectcompToClass.getClassKey();
				if(classKey.equals(stuClassKey)){
					subjectCompId = subjectcompToClass.getSubjectCompId();
					subjectcompToClassInfo = subjectcompToClass;
					break;
				}
			}
			//更新t_pl_dezy_class
			if(placementType==4){
				Map<String,Object> map2 = new HashMap<String,Object>();
				map2.put("termInfo", termInfo);
				map2.put("placementId", placementId);
				map2.put("schoolId", schoolId);
				map2.put("tclassId", adminClassId);
 				List<TPlDezyClass> TPlDezyClassList = placementTaskDao.getDezyClassList(map2);
 				if(TPlDezyClassList==null){
 					throw new RuntimeException("行政班不存在");
 				}
 				TPlDezyClass TPlDezyClass = TPlDezyClassList.get(0);
 				JSONObject classInfo = new JSONObject();
 				classInfo.put("accountId", accountId);
 				classInfo.put("userName", userName);
 				classInfo.put("classId", TPlDezyClass.getTclassId());
 				classInfo.put("className", TPlDezyClass.getTclassName());
 				String subjectId = TPlDezyClass.getSubjectId();
 				String subjectName = TransformationToName(subjectId);
 				classInfo.put("subjectId", subjectId);
 				classInfo.put("subjectName", subjectName);
 				classInfoList.add(classInfo);
 				
				Map<String,Object> TPlDezyClassMap = new HashMap<String,Object>();
				TPlDezyClassMap.put("termInfo", termInfo);
				TPlDezyClassMap.put("tclassNum", TPlDezyClass.getTclassNum()+1);
				TPlDezyClassMap.put("placementId", placementId);
				TPlDezyClassMap.put("schoolId", schoolId);
				TPlDezyClassMap.put("tclassId", adminClassId);
				placementTaskDao.updateTPlDezyClassNum(TPlDezyClassMap);
			}

			for(Map<String,String> subjectIdToClassId:subjectIdToClassIdList){
				String tclassId = subjectIdToClassId.get("tclassId");
				Map<String,Object> map2 = new HashMap<String,Object>();
				map2.put("termInfo", termInfo);
				map2.put("placementId", placementId);
				map2.put("schoolId", schoolId);
				map2.put("tclassId", tclassId);
				List<TPlDezyClass> TPlDezyClassList = placementTaskDao.getDezyClassList(map2);
 				if(TPlDezyClassList==null){
 					throw new RuntimeException("教学班"+tclassId+"不存在");
 				}
 				TPlDezyClass TPlDezyClass = TPlDezyClassList.get(0);
 				JSONObject classInfo = new JSONObject();
 				classInfo.put("accountId", accountId);
 				classInfo.put("userName", userName);
 				classInfo.put("classId", TPlDezyClass.getTclassId());
 				classInfo.put("className", TPlDezyClass.getTclassName());
 				String subjectId = TPlDezyClass.getSubjectId();
 				String subjectName = TransformationToName(subjectId);
 				classInfo.put("subjectId", subjectId);
 				classInfo.put("subjectName", subjectName);
 				classInfoList.add(classInfo);
 				
				Map<String,Object> TPlDezyClassMap = new HashMap<String,Object>();
				TPlDezyClassMap.put("termInfo", termInfo);
				TPlDezyClassMap.put("tclassNum", TPlDezyClass.getTclassNum()+1);
				TPlDezyClassMap.put("placementId", placementId);
				TPlDezyClassMap.put("schoolId", schoolId);
				TPlDezyClassMap.put("tclassId", tclassId);
				placementTaskDao.updateTPlDezyClassNum(TPlDezyClassMap);
			}
			
			Collections.sort(classInfoList, new Comparator<JSONObject>() {
			      @Override
			      public int compare(JSONObject o1, JSONObject o2) {
			       String str1=o1.get("subjectId").toString();
			       String str2=o2.get("subjectId").toString();
			             if (str1.compareToIgnoreCase(str2)<0){  
			                 return -1;  
			             }  
			             return 1;  
			      }
			       });
			
			//如果有开班完全一样的情况
			if(!("".equals(subjectCompId))){
				//更新t_pl_dezy_subjectcomp
				Map<String,Object> TPlDezySubjectMap = new HashMap<String,Object>();
				TPlDezySubjectMap.put("termInfo", termInfo);
				TPlDezySubjectMap.put("compNum", subjectcompToClassInfo.getCompNum()+1);
				TPlDezySubjectMap.put("placementId", placementId);
				TPlDezySubjectMap.put("schoolId", schoolId);
				TPlDezySubjectMap.put("subjectCompId", subjectCompId);
				placementTaskDao.updateTPlDezySubjectcomp(TPlDezySubjectMap);
				
				//更新t_pl_dezy_subjectcomp_student
				List<TPlDezySubjectcompStudent> tPlDezySubjectcompStudentList = new ArrayList<TPlDezySubjectcompStudent>();
				TPlDezySubjectcompStudent tPlDezySubjectcompStudent = new TPlDezySubjectcompStudent();
				tPlDezySubjectcompStudent.setPlacementId(placementId);
				tPlDezySubjectcompStudent.setSchoolId(schoolId);
				tPlDezySubjectcompStudent.setUsedGrade(usedGrade);
				tPlDezySubjectcompStudent.setStudentId(accountId);
				tPlDezySubjectcompStudent.setSubjectCompId(subjectCompId);
				tPlDezySubjectcompStudentList.add(tPlDezySubjectcompStudent);
				placementImportDao.batchInsertTPlDezySubjectcompStudentList(termInfo,tPlDezySubjectcompStudentList);
				
			}else{
				//没有开班完全一样的，需要重新生成subjectCompId
				/*
				 * placementTaskDao.getTclassFromList(Map<String, Object> map)termInfo、placementId、schoolId
				 * placementImportDao.batchInsertTPlDezyTclassfromList(String termInfo, List<TPlDezyTclassfrom> list)
				 * */
				String subjectCompIdNew = UUIDUtil.getUUID();
				//更新t_pl_dezy_subjectcomp
				List<TPlDezySubjectcomp> tPlDezySubjectcompList = new ArrayList<TPlDezySubjectcomp>();
				TPlDezySubjectcomp tPlDezySubjectcomp = new TPlDezySubjectcomp();
				tPlDezySubjectcomp.setPlacementId(placementId);
				tPlDezySubjectcomp.setSchoolId(schoolId);
				tPlDezySubjectcomp.setUsedGrade(usedGrade);
				tPlDezySubjectcomp.setClassId(adminClassId);
				tPlDezySubjectcomp.setSubjectCompId(subjectCompIdNew);
				tPlDezySubjectcomp.setCompNum(1);
				String compName ="";
				if(placementType==4){
					List<String> DESubjectList = (List<String>) LastResult.get("DESubjectList");
					for(String DESubject:DESubjectList){
						switch (DESubject){
						case "4":
							compName = compName + "政治,";
							break;
						case "5":
							compName = compName + "历史,";
							break;
						case "6":
							compName = compName + "地理,";
							break;
						case "7":
							compName = compName + "物理,";
							break;
						case "8":
							compName = compName + "化学,";
							break;
						case "9":
							compName = compName + "生物,";
							break;
						case "19":
							compName = compName + "技术,";
							break;
						default:
							break;
						}
					}
					compName = compName.substring(0,compName.length()-1);
				}else if(placementType==3){
					List<String> DESubjectList = optSubjectList;
					for(String DESubject:DESubjectList){
						switch (DESubject){
						case "4":
							compName = compName + "政";
							break;
						case "5":
							compName = compName + "史";
							break;
						case "6":
							compName = compName + "地";
							break;
						case "7":
							compName = compName + "物";
							break;
						case "8":
							compName = compName + "化";
							break;
						case "9":
							compName = compName + "生";
							break;
						case "19":
							compName = compName + "技";
							break;
						default:
							break;
						}
					}
				}
				tPlDezySubjectcomp.setCompName(compName);
				String compFrom = "";
				for(String optSubject:optSubjectList){
					if("".equals(compFrom)){
						compFrom = optSubject;
					}else{
						compFrom = compFrom+","+optSubject;
					}
				}
				tPlDezySubjectcomp.setCompFrom(compFrom);
				tPlDezySubjectcompList.add(tPlDezySubjectcomp);
				placementImportDao.batchInsertTPlDezySubjectcompList(termInfo,tPlDezySubjectcompList);
				
				//更新t_pl_dezy_tclass_subcomp和t_pl_dezy_tclassfrom
				for(int i=1;i<=allSubjectList.size();i++){
					String subjectArrage = LastResult.get(i+"").toString();
					List<String> subjectIdTotClassIdList = Arrays.asList(subjectArrage.split(","));
					if(subjectIdTotClassIdList.size()!=2){
						throw new RuntimeException("获取LastResult中分班信息失败");
					}
					String tclassId = subjectIdTotClassIdList.get(1);
					List<TPlDezyTclassSubcomp> tPlDezyTclassSubcompList = new ArrayList<TPlDezyTclassSubcomp>();
					TPlDezyTclassSubcomp tPlDezyTclassSubcomp = new TPlDezyTclassSubcomp();
					tPlDezyTclassSubcomp.setPlacementId(placementId);
					tPlDezyTclassSubcomp.setSchoolId(schoolId);
					tPlDezyTclassSubcomp.setUsedGrade(usedGrade);
					tPlDezyTclassSubcomp.setSubjectCompId(subjectCompIdNew);
					tPlDezyTclassSubcomp.setTclassId(tclassId);
					tPlDezyTclassSubcompList.add(tPlDezyTclassSubcomp);
					placementImportDao.batchInsertTPlDezyTclassSubcompList(termInfo,tPlDezyTclassSubcompList);
					
					if(placementType==4){
						Map<String, Object> map2 = new HashMap<String,Object>();
						map2.put("termInfo", termInfo);
						map2.put("placementId", placementId);
						map2.put("schoolId", schoolId);
						map2.put("tclassId", tclassId);
						map2.put("classId", adminClassId);
						List<TPlDezyTclassfrom> tclassFromList = new ArrayList<TPlDezyTclassfrom>();
						tclassFromList= placementTaskDao.getTclassFromList(map2);
						//先要查下主键是否存在，已经存在的话不需要更新这个表
						if(null==tclassFromList||tclassFromList.size()<=0){
							map2.remove("classId");
							tclassFromList = placementTaskDao.getTclassFromList(map2);
							if(tclassFromList.size()<=0){
								throw new RuntimeException("t_pl_dezy_tclassfrom数据获取失败");
							}
							String subjectGroupId = tclassFromList.get(0).getSubjectGroupId();
							String classGroupId = tclassFromList.get(0).getClassGroupId();
							List<TPlDezyTclassfrom> tPlDezyTclassfromList = new ArrayList<TPlDezyTclassfrom>();
							TPlDezyTclassfrom tPlDezyTclassfrom = new TPlDezyTclassfrom();
							tPlDezyTclassfrom.setPlacementId(placementId);
							tPlDezyTclassfrom.setSchoolId(schoolId);
							tPlDezyTclassfrom.setUsedGrade(usedGrade);
							tPlDezyTclassfrom.setSubjectGroupId(subjectGroupId);
							tPlDezyTclassfrom.setClassGroupId(classGroupId);
							tPlDezyTclassfrom.setTclassId(tclassId);
							tPlDezyTclassfrom.setClassId(adminClassId);
							tPlDezyTclassfromList.add(tPlDezyTclassfrom);
							placementImportDao.batchInsertTPlDezyTclassfromList(termInfo,tPlDezyTclassfromList);
						}
					}
				}
				
				//更新t_pl_dezy_subjectcomp_student
				List<TPlDezySubjectcompStudent> TPlDezySubjectcompStudentList = new ArrayList<TPlDezySubjectcompStudent>();
				TPlDezySubjectcompStudent tPlDezySubjectcompStudent = new TPlDezySubjectcompStudent();
				tPlDezySubjectcompStudent.setPlacementId(placementId);
				tPlDezySubjectcompStudent.setSchoolId(schoolId);
				tPlDezySubjectcompStudent.setUsedGrade(usedGrade);
				tPlDezySubjectcompStudent.setSubjectCompId(subjectCompIdNew);
				tPlDezySubjectcompStudent.setStudentId(accountId);
				TPlDezySubjectcompStudentList.add(tPlDezySubjectcompStudent);
				placementImportDao.batchInsertTPlDezySubjectcompStudentList(termInfo,TPlDezySubjectcompStudentList);
			}
		}catch(Exception e){
			throw new RuntimeException("保存分班数据失败：",e);	
		}
		return classInfoList;
	}
}
