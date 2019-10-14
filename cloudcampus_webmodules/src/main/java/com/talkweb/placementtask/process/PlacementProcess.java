package com.talkweb.placementtask.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.ExecProgressRedisMsg;
import com.talkweb.placementtask.domain.OpenClassInfo;
import com.talkweb.placementtask.domain.OpenClassTask;
import com.talkweb.placementtask.domain.PlacementRule;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.StudentInfo;
import com.talkweb.placementtask.domain.TeachingClassInfo;
import com.talkweb.scoreManage.service.impl.ScoreManageAPIServiceImpl;
import com.talkweb.wishFilling.service.WishFillingThirdService;

public class PlacementProcess implements Runnable {
	Logger logger = LoggerFactory.getLogger(PlacementProcess.class);

	private DataSourceTransactionManager transactionManager;
	private PlacementTaskDao placementTaskDao;
	private ScoreManageAPIServiceImpl scoreManageAPIService;
	private WishFillingThirdService wishFillingThirdService;
	private AllCommonDataService commonDataService;
	private RedisOperationDAO redisOperationDAO;

	private ExecProgressRedisMsg progress = new ExecProgressRedisMsg();
	private JSONObject request = null;
	private String redisKey = null;
	
	// 班级数字前缀补0
	private static final String TEACHING_CLASS_NAME_FORMAT = "%03d";

	private DefaultTransactionDefinition def = new DefaultTransactionDefinition();

	public PlacementProcess(JSONObject request, String redisKey, DataSourceTransactionManager transactionManager,
			PlacementTaskDao placementTaskDao, ScoreManageAPIServiceImpl scoreManageAPIService,
			WishFillingThirdService wishFillingThirdService, AllCommonDataService commonDataService,
			RedisOperationDAO redisOperationDAO) {
		this.request = request;
		this.redisKey = redisKey;
		this.transactionManager = transactionManager;
		this.placementTaskDao = placementTaskDao;
		this.scoreManageAPIService = scoreManageAPIService;
		this.wishFillingThirdService = wishFillingThirdService;
		this.commonDataService = commonDataService;
		this.redisOperationDAO = redisOperationDAO;

		// 设置事务隔离级别，开启新事务
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	private void setProgress(double progress, String msg) {
		this.progress.setMessage(msg);
		if(progress != -1){
			this.progress.setProgress(progress);
		}
		this.progress.setCode(1);
		try {
			redisOperationDAO.set(redisKey, this.progress, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private void setError(int code, String errorMsg) {
		setError(code, errorMsg, null);
	}
	
	private void setError(int code, String errorMsg, String message) {
		this.progress.setCode(code);
		this.progress.setErrorMsg(errorMsg);
		if(message != null) {
			this.progress.setMessage(message);
		}
		try {
			redisOperationDAO.set(redisKey, this.progress, CacheExpireTime.temporaryDataMinExpireTime.getTimeValue());
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	private List<Long> getAccountIds(Long schoolId, String usedGrade, String termInfo) {
		List<Long> allAccIds = new ArrayList<Long>();

		String xn = termInfo.substring(0, termInfo.length() - 1);
		T_GradeLevel gl = T_GradeLevel.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
		Grade grade = commonDataService.getGradeByGradeLevel(schoolId, gl, termInfo);
		if (grade == null) {
			throw new CommonRunException(-1, "从SDK无法获取对应的年级数据，请联系管理员！");
		}
		List<Long> classIds = grade.getClassIds();
		if (CollectionUtils.isEmpty(classIds)) {
			return allAccIds;
		}

		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, grade.getClassIds(), termInfo);
		if (CollectionUtils.isEmpty(classrooms)) {
			return allAccIds;
		}

		for (Classroom classroom : classrooms) {
			List<Long> accIds = classroom.getStudentAccountIds();
			if (CollectionUtils.isEmpty(accIds)) {
				continue;
			}
			allAccIds.addAll(accIds);
		}
		return allAccIds;
	}

	private List<Account> getAccountBatch(Long schoolId, String usedGrade, String termInfo) {
		List<Long> accountIds = getAccountIds(schoolId, usedGrade, termInfo);
		if (CollectionUtils.isEmpty(accountIds)) {
			return new ArrayList<Account>();
		}
		List<Account> accList = commonDataService.getAccountBatch(schoolId, accountIds, termInfo);
		if (accList == null) {
			return new ArrayList<Account>();
		}
		return accList;
	}
	
	private StudentPart getStudentPart(Account acc){
		StudentPart studPart = null;
		List<User> users = acc.getUsers(); // 获取身份信息列表
		if (CollectionUtils.isEmpty(users)) {
			return studPart;
		}
		for (User user : users) { // 查询学生身份信息
			if (T_Role.Student.equals(user.getUserPart().getRole())) {
				studPart = user.getStudentPart();
				break;
			}
		}
		return studPart;
	}

	public void updateOpenClassTaskStatus(List<OpenClassTask> opClassTaskList, Long schoolId, String termInfo) {
		if(CollectionUtils.isEmpty(opClassTaskList)){
			return ;
		}
		for(OpenClassTask opClassTask : opClassTaskList){
			opClassTask.setStatus(1);
			opClassTask.setSchoolId(schoolId);
			opClassTask.setTermInfo(termInfo);
			placementTaskDao.updateOpenClassTask(opClassTask);
		}
	}
	
	private List<TeachingClassInfo> createTeachingClass(OpenClassTask opClassTask, Long schoolId, 
			String usedGrade, String placementId, String termInfo){
		List<TeachingClassInfo> tClassInfos = new ArrayList<TeachingClassInfo>();
		
		OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
		Integer numOfOpenClasses = opClassTask.getNumOfOpenClasses();
		// 开班数为0，因此不开此班级
		if(numOfOpenClasses == null || numOfOpenClasses == 0){
			return tClassInfos;
		}
		
		// 创建教学班，numOfOpenClasses表示开班数
		for (int idx = 1; idx <= numOfOpenClasses; idx++) {
			TeachingClassInfo tClassInfo = new TeachingClassInfo();
			tClassInfo.setTeachingClassId(UUIDUtil.getUUID());
			tClassInfo.setSchoolId(schoolId);
			tClassInfo.setUsedGrade(usedGrade);
			
			String teachingClassName = null;
			switch (opClassInfo.getType()) {
			case 1: // 三三固定组合
			case 2: // 无固定组合
				teachingClassName = new StringBuffer().append(opClassInfo.getZhName())
						.append(String.format(TEACHING_CLASS_NAME_FORMAT, idx)).append("班").toString();
				break;
			case 3: // 单科
				if (opClassTask.getSubjectLevel() == 1) {
					teachingClassName = new StringBuffer().append(opClassInfo.getZhName()).append("（选）")
							.append(String.format(TEACHING_CLASS_NAME_FORMAT, idx)).append("班").toString();
				} else if (opClassTask.getSubjectLevel() == 2) {
					teachingClassName = new StringBuffer().append(opClassInfo.getZhName()).append("（学）")
							.append(String.format(TEACHING_CLASS_NAME_FORMAT, idx)).append("班").toString();
				} else {
					throw new CommonRunException(-1, "在开班任务中找到非法的单科类型，请联系管理员！");
				}
				break;
			case 4: // 成绩分层
				teachingClassName = new StringBuffer().append(opClassInfo.getZhName()).append(opClassTask.getLayName())
						.append(String.format(TEACHING_CLASS_NAME_FORMAT, idx)).append("班").toString();
				break;
			default:
				throw new CommonRunException(-1, "在开班信息中找到非法的开班类型，请联系管理员！");
			}
			
			tClassInfo.setTeachingClassName(teachingClassName);
			tClassInfo.setPlacementId(placementId);
			tClassInfo.setOpenClassInfoId(opClassInfo.getOpenClassInfoId());
			tClassInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
			tClassInfo.setTermInfo(termInfo);

			tClassInfos.add(tClassInfo);
		}
		return tClassInfos;
	}
	
	/**
	 * 均衡分班
	 * @param femaleList 学生链表
	 * @param tClassInfos 班级列表
	 * @param isAsc	true为开始顺序插入，false先逆序插入
	 * @param diffName	是否姓名相同的需要分在不同的班级
	 */
	private void balancedPlacement(LinkedList<StudentInfo> studInfoList, List<TeachingClassInfo> tClassInfos, 
			boolean isAsc, Integer diffName){
		
		if(CollectionUtils.isEmpty(studInfoList)){
			return ;
		}
		// 向教学班级中插入学生（组合才需要，单科的由于排课会冲突，因此不在此处插入学生）
		// 考虑均衡性，因此不能一直使用正序循环，应该先顺序后逆序，使得成绩更加均匀
		int idx = 0, len = tClassInfos.size();
		if(!isAsc){
			idx = len;
		}
		while (CollectionUtils.isNotEmpty(studInfoList)) {
			if (idx < 0) {
				isAsc = true;
			} else if (idx > len - 1) {
				isAsc = false;
			}
			
			if(idx < 0) {
				idx = 0;
			}
			
			if(idx > len - 1) {
				idx = len -1;
			}
			
			// 获取创建的教学班级
			TeachingClassInfo tClassInfo = tClassInfos.get(idx);

			StudentInfo studInfo = null;
			for (int i = 0; i < studInfoList.size(); i++) {	// 循环获取学生，链表的第一个开始
				StudentInfo stud = studInfoList.get(i);
				
				// 校验是否需要判断名字是否冲突，如果需要校验姓名相同的需要分在不同的班级，并且此班级已经存有这个名字，不取此学生信息
				if (diffName == 1 && tClassInfo.hasStudName(stud.getName())) {
					continue;
				}
				
				// 有合适的学生信息，那么将此学生从链表中移除，以后分班不再考虑
				studInfo = studInfoList.remove(i);
				break;
			}

			if (studInfo == null) {	// 如果循环所有的数据没有合适的数据，那么取第一条数据
				studInfo = studInfoList.removeFirst();
			}

			if(T_Gender.Female.equals(studInfo.getGender())){
				tClassInfo.incrNumOfGirls(); // 女生数自增
			}else if(T_Gender.Male.equals(studInfo.getGender())){
				tClassInfo.incrNumOfBoys(); // 男生数自增
			}
			
			tClassInfo.incrNumOfStuds(); // 学生总数自增

			// 教学班级学生名字集合里添加学生名字，用于校验
			tClassInfo.addStudNames(studInfo.getName());

			// 给教学班添加学生关联
			tClassInfo.addStudentInfo(studInfo);
			// 学生信息和教学班关联
			studInfo.setTeachingClassId(tClassInfo.getTeachingClassId());
			studInfo.setOpenClassInfoId(tClassInfo.getOpenClassInfoId());
			studInfo.setOpenClassTaskId(tClassInfo.getOpenClassTaskId());

			if (isAsc && idx <= len - 1) { // 为了保证均衡，先顺序后逆序
				idx ++;
			}
			
			if (!isAsc && idx >= 0) {
				idx --;
			}
		}
	}
	
	@Override
	public void run() {
		// 获得事务状态
		TransactionStatus transactionStatus = transactionManager.getTransaction(def);

		try {
			setProgress(0, "校验分班任务信息");
			PlacementTask pl = placementTaskDao.queryPlacementTaskById(request);
			if (pl == null) {
				throw new CommonRunException(-1, "查询不到对应的分班任务，请联系管理员！");
			}
			Integer placementType = pl.getPlacementType();
			request.put("placementType", placementType);
			
			PlacementTask updatePl = new PlacementTask();
			updatePl.setPlacementId(request.getString("placementId"));
			updatePl.setSchoolId(request.getLong("schoolId"));
			updatePl.setTermInfo(request.getString("termInfo"));
			updatePl.setLayerStatus(pl.getLayerStatus());
			updatePl.setStatus(pl.getStatus());
			
			switch (placementType) {
			case 1: // 微走班
				execPlacementMicro(pl);
				updatePl.setStatus(100);
				break;
			case 2: // 中走班
				execPlacementMedium(pl);
				updatePl.setStatus(100);
				break;
			case 3: // 大走班
				Integer type = request.getInteger("type");
				if(type == 3) {	// 单科志愿
					execWishPlacementLarge(pl);
					updatePl.setStatus(100);
				} else if(type == 4) {
					execLayerPlacementLarge(pl);
					updatePl.setLayerStatus(100);
				}
				break;
			}
			
			setProgress(98, "更新分班主任务状态");
			placementTaskDao.updatePlacementTaskById(updatePl);
			// 提交事务
			transactionManager.commit(transactionStatus);
			
			setProgress(100, "分班已完成！");
		} catch (CommonRunException e) {
			// 回滚事务
			transactionManager.rollback(transactionStatus);
			setError(e.getCode(), e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// 回滚事务
			transactionManager.rollback(transactionStatus);
			setError(-1, "后台异常，请联系管理员！", e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 按三三组合分班
	 * @param placementTask	分班任务对象
	 * @param studInfoList	学生信息列表
	 * @param opClassTasks	开班任务列表
	 */
	private void execZhPlacement(PlacementTask placementTask, List<StudentInfo> studInfoList, 
			List<OpenClassTask> opClassTasks) {
		
		if(CollectionUtils.isEmpty(opClassTasks)){
			return ;
		}
		
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");
		String usedGrade = placementTask.getUsedGrade();
		
		Integer diffName = placementTask.getDifferentName();
		final Integer genderBalance = placementTask.getGenderBalance();
		
		setProgress(31, "获取组合分班规则");
		
		// 获取组合规则
		request.put("type", 1);
		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		Integer ruleCode = null;
		String examId = null;
		String examTermInfo = null;
		if (CollectionUtils.isNotEmpty(rules)) {
			examId = rules.get(0).getExamId();
			examTermInfo = rules.get(0).getExamTermInfo();
			ruleCode = rules.get(0).getRuleCode();
			request.put("examId", rules.get(0).getExamId());
		}
		
		setProgress(35, "获取学生成绩数据");
		//*******************************************获取学生成绩数据**********************************************//
		// examId为空，没有成绩
		Map<Long, StudentInfo> studInfoMap = null;
		if(StringUtils.isNotBlank(examId) && StringUtils.isNotBlank(examTermInfo)) {
			studInfoMap = StudentInfo.convertToStudentScoreMap(
					scoreManageAPIService.queryScoreInfo(examId, String.valueOf(schoolId), examTermInfo, null, null));
		} else {
			studInfoMap = new HashMap<Long, StudentInfo>();
		}
		
		Map<String, LinkedList<StudentInfo>> subjectIdStr2List = new HashMap<String, LinkedList<StudentInfo>>();
		// 为填写志愿的学生信息
		for (StudentInfo studInfo : studInfoList) {
			Long accId = studInfo.getAccountId();

			// 学生对应的成绩，如果没有成绩则让其为0
			StudentInfo studInfoScore = studInfoMap.get(accId);
			if (studInfoScore != null) {	// 补全学生数据
				studInfo.putScoreAll(studInfoScore.getScoreAll());
			}

			String key = studInfo.getSubjectIdsStr();
			
			// 如果不需要按男女比例均衡，即genderBalance == 0，则将女生的数据全部放入otherList链表里
			if (T_Gender.Female.equals(studInfo.getGender()) && genderBalance == 1) {
				key = key + "_female";
			} else { // 其他人分配到男生中去
				key = key + "_other";
			}

			if (!subjectIdStr2List.containsKey(key)) {
				subjectIdStr2List.put(key, new LinkedList<StudentInfo>());
			}
			subjectIdStr2List.get(key).add(studInfo);
		}
		
		setProgress(45, "进行分班操作");
		//***************************** 开班并给每班分配学生 ****************************************//
		for (OpenClassTask opClassTask : opClassTasks) {
			String subjectIdsStr = opClassTask.getOpenClassInfo().getSubjectIdsStr();
			
			List<TeachingClassInfo> tClassInfos = createTeachingClass(opClassTask, schoolId, usedGrade, placementId, termInfo);
			if(CollectionUtils.isEmpty(tClassInfos)){
				continue;
			}

			// 组合结构，插入学生
			LinkedList<StudentInfo> femaleList = subjectIdStr2List.get(subjectIdsStr + "_female");
			LinkedList<StudentInfo> otherList = subjectIdStr2List.get(subjectIdsStr + "_other");
			if(femaleList == null){
				femaleList = new LinkedList<StudentInfo>();
			}
			if(otherList == null){
				otherList = new LinkedList<StudentInfo>();
			}
			
			
			if (StringUtils.isNotBlank(examId) && ruleCode == 1) { // 按成绩总分均衡 排序
				if (CollectionUtils.isNotEmpty(femaleList)) {
					Collections.sort(femaleList, new ScoreSort());
				}
				if (CollectionUtils.isNotEmpty(otherList)) {
					Collections.sort(otherList, new ScoreSort());
				}
			} else if (StringUtils.isNotBlank(examId) && ruleCode == 2) { // 按语数外三科成绩总分均衡 排序
				if (CollectionUtils.isNotEmpty(femaleList)) {
					Collections.sort(femaleList, new ScoreSort(new Long[] { 1L, 2L, 3L }));
				}
				if (CollectionUtils.isNotEmpty(otherList)) {
					Collections.sort(otherList, new ScoreSort(new Long[] { 1L, 2L, 3L }));
				}
			} else {
				// 随机排序
				if (CollectionUtils.isNotEmpty(femaleList)) {
					Collections.shuffle(femaleList);
				}
				if (CollectionUtils.isNotEmpty(otherList)) {
					Collections.shuffle(otherList);
				}
			}
			
			if(CollectionUtils.isNotEmpty(femaleList)){
				otherList.addAll(femaleList);
			}
			// 平均分配
			balancedPlacement(otherList, tClassInfos, true, diffName);

			if(tClassInfos.size() > 0){
				placementTaskDao.insertTeachingClassInfoBatch(tClassInfos, termInfo);
				List<StudentInfo> studentInfos = new ArrayList<StudentInfo>();
				for(TeachingClassInfo tClassInfo : tClassInfos){
					if(tClassInfo.getStudInfos().size() > 0) {
						studentInfos.addAll(tClassInfo.getStudInfos());
					}
				}
				if(studentInfos.size() > 0) {
					placementTaskDao.insertStudentInfoBatch(studentInfos, termInfo);
				}
			}
		}
	}
	
	/**
	 * 执行中走班无固定三科教学班分班
	 * @param placementTask	分班任务对象
	 * @param studInfoList	学生信息列表
	 * @param opClassTask	开班任务对象
	 */
	private void execTeachingPlacement(PlacementTask placementTask, List<StudentInfo> studInfoList, 
			OpenClassTask opClassTask) {
		
		if(opClassTask == null){
			return ;
		}
		
		setProgress(48, "开始教学班分班操作");
		
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");
		String usedGrade = placementTask.getUsedGrade();
		
		Integer diffName = placementTask.getDifferentName();
		final Integer genderBalance = placementTask.getGenderBalance();
		
		setProgress(50, "获取教学班分班规则");
		
		// 获取教学组合规则
		request.put("type", 2);
		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		String examId = null;
		String examTermInfo = null;
		Integer ruleCode = null;
		if (CollectionUtils.isNotEmpty(rules)) {
			examId = rules.get(0).getExamId();
			examTermInfo = rules.get(0).getExamTermInfo();
			ruleCode = rules.get(0).getRuleCode();
			request.put("examId", examId);
		}
		
		setProgress(53, "获取学生成绩数据");
		//*******************************************获取学生成绩数据**********************************************//
		// examId为空，则没有成绩
		Map<Long, StudentInfo> studInfoMap = null;
		if(StringUtils.isNotBlank(examId) && StringUtils.isNotBlank(examTermInfo)) {
			studInfoMap = StudentInfo.convertToStudentScoreMap(
					scoreManageAPIService.queryScoreInfo(examId, String.valueOf(schoolId), examTermInfo, null, null));
		} else {
			studInfoMap = new HashMap<Long, StudentInfo>();
		}
		
		LinkedList<StudentInfo> femaleList = new LinkedList<StudentInfo>();
		LinkedList<StudentInfo> otherList = new LinkedList<StudentInfo>();
		for (StudentInfo studInfo : studInfoList) {
			Long accId = studInfo.getAccountId();

			// 学生对应的成绩，如果没有成绩则让其为0
			StudentInfo studInfoScore = studInfoMap.get(accId);
			if (studInfoScore != null) {	// 补全学生数据
				studInfo.putScoreAll(studInfoScore.getScoreAll());
			}

			// 如果不需要按男女比例均衡，即genderBalance == 0，则将女生的数据全部放入otherList链表里
			if (T_Gender.Female.equals(studInfo.getGender()) && genderBalance == 1) { 
				femaleList.add(studInfo);
			} else { // 其他人分配到男生中去
				otherList.add(studInfo);
			}
		}
		
		setProgress(55, "进行教学班分班任务");
		//***************************** 开班并给每班分配学生 ****************************************//
		List<TeachingClassInfo> tClassInfos = createTeachingClass(opClassTask, schoolId, usedGrade, placementId, termInfo);
		if(CollectionUtils.isEmpty(tClassInfos)) {	// 没有班级则直接返回
			return ;
		}

		if (StringUtils.isNotBlank(examId) && ruleCode == 1) { // 按成绩总分均衡 排序
			if (CollectionUtils.isNotEmpty(femaleList)) {
				Collections.sort(femaleList, new ScoreSort());
			}
			if (CollectionUtils.isNotEmpty(otherList)) {
				Collections.sort(otherList, new ScoreSort());
			}
		} else if (StringUtils.isNotBlank(examId) && ruleCode == 2) { // 按语数外三科成绩总分均衡 排序
			if (CollectionUtils.isNotEmpty(femaleList)) {
				Collections.sort(femaleList, new ScoreSort(new Long[] { 1L, 2L, 3L }));
			}
			if (CollectionUtils.isNotEmpty(otherList)) {
				Collections.sort(otherList, new ScoreSort(new Long[] { 1L, 2L, 3L }));
			}
		} else {
			// 随机排序
			if (CollectionUtils.isNotEmpty(femaleList)) {
				Collections.shuffle(femaleList);
			}
			if (CollectionUtils.isNotEmpty(otherList)) {
				Collections.shuffle(otherList);
			}
		}

		if(CollectionUtils.isNotEmpty(femaleList)){
			otherList.addAll(femaleList);
		}
		
		// 平均分配
		balancedPlacement(otherList, tClassInfos, true, diffName);

		if(tClassInfos.size() > 0){
			placementTaskDao.insertTeachingClassInfoBatch(tClassInfos, termInfo);
			List<StudentInfo> studentInfos = new ArrayList<StudentInfo>();
			for(TeachingClassInfo tClassInfo : tClassInfos){
				if(tClassInfo.getStudInfos().size() > 0) {
					studentInfos.addAll(tClassInfo.getStudInfos());
				}
			}
			placementTaskDao.insertStudentInfoBatch(studentInfos, termInfo);
		}
	}
	
	/**
	 * 按志愿单科进行分班
	 * @param placementTask	分班任务对象
	 * @param studInfoList	学生信息列表
	 * @param opClassTasks	开班任务列表
	 */
	private void execDkPlacement(PlacementTask placementTask, Map<String, List<StudentInfo>> subjectId2GaoList,
			Map<String, List<StudentInfo>> subjectId2XueList, List<OpenClassTask> opClassTasks) {
		
		if(CollectionUtils.isEmpty(opClassTasks)){
			return ;
		}
		setProgress(60, "进行单科分班操作");
		
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");
		String usedGrade = placementTask.getUsedGrade();
		
		setProgress(60, "获取单科分班规则数据");
		
		request.put("type", 3);
		List<PlacementRule> rules = placementTaskDao.queryRuleInfo(request);
		String examId = "";
		String examTermInfo = "";
		if (CollectionUtils.isNotEmpty(rules)) {
			request.put("examId", rules.get(0).getExamId());
			examId = rules.get(0).getExamId();
			examTermInfo = rules.get(0).getExamTermInfo();
		}
		
		setProgress(65, "获取学生成绩数据");
		//*******************************************获取学生成绩数据**********************************************//
		// examId为空，则没有成绩
		Map<Long, StudentInfo> studScoreMap = null;
		if(StringUtils.isNotBlank(examId) && StringUtils.isNotBlank(examTermInfo)) {
			Set<String> subjectIdSet = new HashSet<String>();
			subjectIdSet.addAll(subjectId2GaoList.keySet());
			subjectIdSet.addAll(subjectId2XueList.keySet());
			if(subjectIdSet.size() > 0){
				studScoreMap = StudentInfo.convertToStudentScoreMap(
						scoreManageAPIService.queryScoreInfo(examId, String.valueOf(schoolId), examTermInfo, 
								new ArrayList<String>(subjectIdSet), null));
			}
		} else {
			studScoreMap = new HashMap<Long, StudentInfo>();
		}
		
		if(studScoreMap != null) {
			for(Map.Entry<String, List<StudentInfo>> entry : subjectId2GaoList.entrySet()){
				Long subjectId = Long.parseLong(entry.getKey());
				List<StudentInfo> studInfoList = entry.getValue();
				for(StudentInfo studInfo : studInfoList) {
					Long accId = studInfo.getAccountId();
					StudentInfo studScore = studScoreMap.get(accId);
					if(studScore != null) {
						studInfo.putScore(subjectId, studScore.getScore(subjectId));
					}
				}
			}
			
			for(Map.Entry<String, List<StudentInfo>> entry : subjectId2XueList.entrySet()){
				Long subjectId = Long.parseLong(entry.getKey());
				List<StudentInfo> studInfoList = entry.getValue();
				for(StudentInfo studInfo : studInfoList) {
					Long accId = studInfo.getAccountId();
					StudentInfo studScore = studScoreMap.get(accId);
					if(studScore != null) {
						studInfo.putScore(subjectId, studScore.getScore(subjectId));
					}
				}
			}
		}
		
		setProgress(70, "进行单科分班操作");
		
		//***************************** 开班并给每班分配学生 ****************************************//
		for (OpenClassTask opClassTask : opClassTasks) {
			List<TeachingClassInfo> tClassInfos = createTeachingClass(opClassTask, schoolId, usedGrade, placementId,
					termInfo);
			// 过滤开班数为0的情况
			if(CollectionUtils.isEmpty(tClassInfos)) {
				continue;
			}
			
			placementTaskDao.insertTeachingClassInfoBatch(tClassInfos, termInfo);
			
			// 组合结构，插入学生
			String subjectIdsStr = opClassTask.getOpenClassInfo().getSubjectIdsStr();
			
			List<StudentInfo> studInfos = null;
			Integer subjectLevel = opClassTask.getSubjectLevel();
			if(subjectLevel == 1) {	// 高考
				studInfos = subjectId2GaoList.get(subjectIdsStr);
			} else if(subjectLevel == 2) {	// 学考
				studInfos = subjectId2XueList.get(subjectIdsStr);
			}
			
			if (CollectionUtils.isNotEmpty(studInfos)) {
				Collections.sort(studInfos, new ScoreSort(new Long[]{Long.parseLong(subjectIdsStr)}));
				for(int i = 0, len = studInfos.size(); i < len; i ++){
					StudentInfo studInfo = studInfos.get(i);
					studInfo.setSortField((float) i);
					studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
					studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
				}
				// 插入待排区
				placementTaskDao.insertStudentInfoWaitForPlacementBatch(studInfos, termInfo);
			}
		}
	}
	
	// 微走班分班
	private void execPlacementMicro(PlacementTask placementTask) {
		setProgress(0, "开始进行微走班分班操作");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String placementId = request.getString("placementId");
		String wfId = placementTask.getWfId();
		String wfTermInfo = placementTask.getWfTermInfo();
		
		setProgress(0, "获取开班任务数据");
		request.put("status", 0);
		List<OpenClassTask> opClassTaskListStatus0 = placementTaskDao.queryOpenClassTasksWithInfo(request);
		// 状态为0的组合任务id集合
		Set<String> opClassTaskIds = new HashSet<String>();
		
		for(OpenClassTask opClassTask : opClassTaskListStatus0) {
			opClassTaskIds.add(opClassTask.getOpenClassTaskId());
		}
		
		setProgress(3, "删除相应的教学班级和学生信息数据");
		
		// 删除没有志愿的人，重新判断是否填写了志愿
		request.put("openClassTaskId", "");
		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		request.remove("openClassTaskId");
		// 删除状态为0的组合开班任务对应的教学班级、已分配班级的学生信息、未分配班级的学生信息数据
		if(opClassTaskIds.size() > 0) {	
			// 删除变更任务的学生和教学班级
			request.put("openClassTaskIds", opClassTaskIds);
			
			// 如果后来修改志愿，给志愿那边提供接口，同步删除这些数据
			placementTaskDao.deleteStudentInfoWaitForPlacement(request);
			placementTaskDao.deleteStudentInfo(request);
			placementTaskDao.deleteTeachingClassInfo(request);
			request.remove("openClassTaskIds");
		}
		
		setProgress(5, "获取已开班的开班任务和相应的学生信息数据");
		// 三三组合是一对一的，判断开班任务是否已经被用来开班，已经开了班的任务不能继续插入学生，只能通过微调插入
		Map<String, OpenClassTask> subjId2OpClassTask = new HashMap<String, OpenClassTask>();
		// 过滤已存在的数据
		request.put("status", 1);
		List<OpenClassTask> opClassTaskListStatus1 = placementTaskDao.queryOpenClassTasksWithInfo(request);
		for(OpenClassTask opClassTask : opClassTaskListStatus1) {
			subjId2OpClassTask.put(opClassTask.getOpenClassInfo().getSubjectIdsStr(), opClassTask);
		}
		
		// 开班任务id对应学生账户id
		Map<String, Set<Long>> opClassTaskId2AccId = new HashMap<String, Set<Long>>();
		
		List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfo(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		studInfoList = placementTaskDao.queryStudentInfoWaitForPlacement(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		setProgress(10, "获取志愿数据");
		//*********************************** 获取志愿，并转化为accId为key，Json为值的Map对象 *******************************//
		Map<Long, JSONObject> accId2WishStud = new HashMap<Long, JSONObject>();
		List<JSONObject> wishStudList = wishFillingThirdService.getZhStudentListToThird(wfId, wfTermInfo, schoolId);
		if (CollectionUtils.isNotEmpty(wishStudList)) {
			for (JSONObject json : wishStudList) {
				Long accountId = json.getLong("accountId");
				if (accountId == null) {
					continue;
				}
				accId2WishStud.put(accountId, json);
			}
		}
		
		setProgress(13, "获取年级下的学生数据");
		//*********************************** 从SDK获取所有学生数据，补全学生数据 *********************************************//
		String usedGrade = placementTask.getUsedGrade();
		List<Account> accList = getAccountBatch(schoolId, usedGrade, termInfo);
		if (CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "从SDK无法获取对应的班级的学生数据，请联系管理员！");
		}

		setProgress(20, "组装学生数据");
		// 参与分班并且已经填写志愿的学生信息
		List<StudentInfo> zhStudInfos = new ArrayList<StudentInfo>();
		// 参与分班并且未填写志愿或者已经填写志愿但对应的任务已经分了教学班
		List<StudentInfo> studInfoWaitForPlacementList = new ArrayList<StudentInfo>();
		for (Account acc : accList) {
			StudentPart studPart = getStudentPart(acc);
			if (studPart == null) { // 账户没有学生角色，则过滤掉此数据
				continue;
			}
			
			long accId = acc.getId();
			long classId = studPart.getClassId();
			String nameSpelling = studPart.getNameSpelling();
			String name = acc.getName();
			T_Gender gender = acc.getGender();
			
			// 通过accId获取学生志愿
			JSONObject wishStud = accId2WishStud.get(accId);
			String subjectIdsStr = null;	// 志愿的组合科目
			if(wishStud != null) {	// 如果存在志愿
				subjectIdsStr = wishStud.getString("subjectIds");
			}
			
			if(subjectIdsStr == null) {	
				// 在前面代码中，未分班学生表中志愿为空的数据都已经删除了，因此直接插入志愿为空的数据到未分班学生表中
				StudentInfo studInfo = new StudentInfo();
				studInfo.setAccountId(accId);
				studInfo.setClassId(classId);
				studInfo.setSchoolId(schoolId);
				studInfo.setPlacementId(placementId);
				studInfo.setTermInfo(termInfo);
				studInfo.setName(name);
				studInfo.setNameSpelling(nameSpelling);
				studInfo.setGender(gender);
				
				studInfoWaitForPlacementList.add(studInfo);
				continue;
			}
			
			// 查看组合科目中是否已经存在开了班的开班任务
			OpenClassTask opClassTask = subjId2OpClassTask.get(subjectIdsStr);
			if(opClassTask != null) {	
				// opClassTaskId不为null，则表示对应的科目已经开了班，不能通过流程自动插入（否则会破坏规则），必须手工调入
				Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());
				if(accIdSet == null || !accIdSet.contains(accId)) {
					// accIdSet为空表示开了对应开班任务班级，但是没有学生，让其手动调整
					// 如果accId不存在accIdSet中，则意味着在已分班或未分班学生信息的表中不存在此学生账户（未分班学生可能是手工调出的）
					// 需要插入到未分班学生信息表中
					StudentInfo studInfo = new StudentInfo();
					studInfo.setAccountId(accId);
					studInfo.setClassId(classId);
					studInfo.setSchoolId(schoolId);
					studInfo.setPlacementId(placementId);
					studInfo.setTermInfo(termInfo);
					
					studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
					studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
					
					studInfoWaitForPlacementList.add(studInfo);
				}
				continue;
			}
			
			StudentInfo studInfo = new StudentInfo();
			studInfo.setAccountId(accId);
			studInfo.setClassId(classId);
			studInfo.setSchoolId(schoolId);
			studInfo.setPlacementId(placementId);
			studInfo.setTermInfo(termInfo);
			studInfo.setName(name);
			studInfo.setNameSpelling(nameSpelling);
			studInfo.setGender(gender);
			
			studInfo.setSubjectIdsStr(subjectIdsStr);	// 添加学生自愿
			studInfo.setType(1);	// 拥有自愿，自动分班
			zhStudInfos.add(studInfo);
		}

		setProgress(30, "插入没有志愿的学生数据");
		if(studInfoWaitForPlacementList.size() > 0) {	// 没有志愿的学生插入未排区
			placementTaskDao.insertStudentInfoWaitForPlacementBatch(studInfoWaitForPlacementList, termInfo);
		}
		
		setProgress(30, "开始进行组合分班操作");
		// 执行组合分班
		execZhPlacement(placementTask, zhStudInfos, opClassTaskListStatus0);
		
		setProgress(95, "更新开班任务状态");
		updateOpenClassTaskStatus(opClassTaskListStatus0, schoolId, termInfo);
	}

	// 中走班
	public void execPlacementMedium(PlacementTask placementTask) {
		setProgress(0, "进行中走班分班操作");
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");
		String wfId = placementTask.getWfId();
		String wfTermInfo = placementTask.getWfTermInfo();

		Set<String> zhSubjectIdSet = new HashSet<String>();
		Set<String> dkSubjectIdSet = new HashSet<String>();
		
		// 状态等于0的开班任务信息列表，为后面更新status使用
		List<OpenClassTask> opClassTaskListStatus0 = new ArrayList<OpenClassTask>();
		// 组合数据列表
		List<OpenClassTask> zhOpClassTaskList = new ArrayList<OpenClassTask>();
		// 单科数据列表
		List<OpenClassTask> dkOpClassTaskList = new ArrayList<OpenClassTask>();
		// 教学班
		OpenClassTask tOpClassTask = null;
		
		Set<String> opClassTaskIds = new HashSet<String>();
		
		setProgress(3, "获取开班任务信息");
		
		// 数据库查询数据
		request.put("status", 0);
		List<OpenClassTask> opClassTasksWithInfo = placementTaskDao.queryOpenClassTasksWithInfo(request);
		for(OpenClassTask opClassTask : opClassTasksWithInfo){
			OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
			
			opClassTaskListStatus0.add(opClassTask);
			
			opClassTaskIds.add(opClassTask.getOpenClassTaskId());
			
			if(1 == opClassInfo.getType()) {	// 三三组合
				zhOpClassTaskList.add(opClassTask);
				zhSubjectIdSet.add(opClassInfo.getSubjectIdsStr());
			}else if(2 == opClassInfo.getType()) {	// 无固定组合
				tOpClassTask = opClassTask;
			}else if(3 == opClassInfo.getType()) {	// 单科走班
//				dkOpClassTaskList.add(opClassTask);
//				dkSubjectIdSet.add(opClassInfo.getSubjectIdsStr());
			}
		}
		
		setProgress(7, "删除开班任务对应的教学班级和学生数据");
		
		// 删除没有志愿的人，重新判断是否填写了志愿
		request.put("openClassTaskId", "");
		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		request.remove("openClassTaskId");
		// 删除状态为0的组合开班任务对应的教学班级、已分配班级的学生信息、未分配班级的学生信息数据
		if(opClassTaskIds.size() > 0) {	
			// 删除变更任务的学生和教学班级
			request.put("openClassTaskIds", opClassTaskIds);
			
			// 如果后来修改志愿，给志愿那边提供接口，同步删除这些数据
			placementTaskDao.deleteStudentInfoWaitForPlacement(request);
			placementTaskDao.deleteStudentInfo(request);
			placementTaskDao.deleteTeachingClassInfo(request);
			request.remove("openClassTaskIds");
		}
		
		setProgress(15, "获取已完成分班的分班任务和对应的学生信息");
		
		// 三三组合是一对一的，判断开班任务是否已经被用来开班，组合的科目id对应开班任务
		Map<String, OpenClassTask> zhSubjId2OpClassTask = new HashMap<String, OpenClassTask>();
		// 科目id对应一个高考开班任务
		Map<String, OpenClassTask> dkGaoSubjId2OpClassTask = new HashMap<String, OpenClassTask>();
		// 科目id对应一个学考开班任务
		Map<String, OpenClassTask> dkXueSubjId2OpClassTask = new HashMap<String, OpenClassTask>();
		// 三三无固定，开教学班
		String teachOpClassTaskId = null;
		
		request.put("status", 1);
		List<OpenClassTask> opClassTaskListStatus1 = placementTaskDao.queryOpenClassTasksWithInfo(request);
		for(OpenClassTask opClassTask : opClassTaskListStatus1) {
			OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
			if(opClassInfo.getType() == 1) {	// 三三固定组合
				zhSubjId2OpClassTask.put(opClassInfo.getSubjectIdsStr(), opClassTask);
				zhSubjectIdSet.add(opClassInfo.getSubjectIdsStr());
				
			} else if(opClassInfo.getType() == 2) {	// 无固定组合
				teachOpClassTaskId = opClassTask.getOpenClassTaskId();
				
			} else if(opClassInfo.getType() == 3) {	// 单科
//				dkSubjectIdSet.add(opClassInfo.getSubjectIdsStr());
//				
//				if(opClassTask.getSubjectLevel() == 1) {	// 高考
//					dkGaoSubjId2OpClassTask.put(opClassInfo.getSubjectIdsStr(), opClassTask);
//				} else if(opClassTask.getSubjectLevel() == 2) {	// 学考
//					dkXueSubjId2OpClassTask.put(opClassInfo.getSubjectIdsStr(), opClassTask);
//				}
			}
		}
		
		// 已经开班的任务id对应学生账户id，排除那些已经分好班的学生
		Map<String, Set<Long>> opClassTaskId2AccId = new HashMap<String, Set<Long>>();
		
		// 已经分好班的学生
		List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfo(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		// 在待排区的学生，走班和按成绩分班的学生不在智能分班模块分班，因此都放到了待排区
		studInfoList = placementTaskDao.queryStudentInfoWaitForPlacement(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		setProgress(18, "获取志愿数据");

		// 从选课接口获取学生志愿
		List<JSONObject> wishStudList = wishFillingThirdService.getZhStudentListToThird(wfId, wfTermInfo, schoolId);
		Map<Long, JSONObject> accId2WishStud = new HashMap<Long, JSONObject>();
		if (CollectionUtils.isNotEmpty(wishStudList)) {
			for (JSONObject json : wishStudList) {
				Long accountId = json.getLong("accountId");
				if (accountId == null) {
					continue;
				}
				accId2WishStud.put(accountId, json);
			}
		}
		
		setProgress(22, "获取年级下的所有学生数据");
		
		// 获取整个年级的学生信息
		String usedGrade = placementTask.getUsedGrade();
		List<Account> accList = getAccountBatch(schoolId, usedGrade, termInfo);
		if (CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "从SDK无法获取对应的班级的学生数据，请联系管理员！");
		}

		setProgress(25, "组装学生数据");
		
		// 按组合分班的学生列表
		List<StudentInfo> zhStudInfos = new ArrayList<StudentInfo>();
		// 走班的学生列表
		Map<String, List<StudentInfo>> subjectId2GaoList = new HashMap<String, List<StudentInfo>>();
		Map<String, List<StudentInfo>> subjectId2XueList = new HashMap<String, List<StudentInfo>>();
		// 教学班学生列表
		List<StudentInfo> teachingStudInfoList = new ArrayList<StudentInfo>();
		// 未填写志愿的学生列表
		List<StudentInfo> studInfoWaitForPlacementList = new ArrayList<StudentInfo>();
		for (Account acc : accList) {
			StudentPart studPart = getStudentPart(acc);
			if (studPart == null) { // 没有学生部分
				continue;
			}
			long accId = acc.getId();	// 账户id
			long classId = studPart.getClassId();	// 学生班级id
			String nameSpelling = studPart.getNameSpelling();	// 学生拼音（目前没用到）
			String name = acc.getName();	// 学生名字
			T_Gender gender = acc.getGender();	// 性别
			
			JSONObject wishStud = accId2WishStud.get(accId);	// 获取学生志愿
			String subjectIdsStr = null;	// 学生选课结果，一个科目列表，使用逗号分隔
			if(wishStud != null) {
				subjectIdsStr = wishStud.getString("subjectIds");
			}
			
			if(subjectIdsStr == null) {	// 判断此人有没有填写志愿，即选择科目
				// 没有填写志愿，放入待排区域
				StudentInfo studInfo = new StudentInfo();
				studInfo.setAccountId(accId);
				studInfo.setClassId(classId);
				studInfo.setSchoolId(schoolId);
				studInfo.setPlacementId(placementId);
				studInfo.setTermInfo(termInfo);
				studInfo.setName(name);
				studInfo.setNameSpelling(nameSpelling);
				studInfo.setGender(gender);
				
				studInfo.setType(3);	// 没有自愿
				studInfoWaitForPlacementList.add(studInfo);
				continue;
			}
			
			
			if(zhSubjectIdSet.contains(subjectIdsStr)) {
				// 志愿的科目组合存在组合开班集合里面，表示所选择的科目组合是按三三固定组合开班
				OpenClassTask opClassTask = zhSubjId2OpClassTask.get(subjectIdsStr);	// 查看对应的科目组合是否已有分班完成的开班任务
				if(opClassTask != null) {
					// opClassTaskId不为null，则表示对应的科目组合已经开了班，不能通过流程自动插入（否则会破坏规则），必须手工调入
					Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());	// 获取对应的学生id集合
					if(accIdSet == null || !accIdSet.contains(accId)){
						// accIdSet为空表示开了对应开班任务班级，但是没有学生，让其手动调整
						// 如果accId不存在accIdSet中，则意味着在已分班或未分班学生信息的表中不存在此学生账户（未分班学生可能是手工调出的）
						// 需要插入到未分班学生信息表中
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						
						studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
						studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
						
						studInfoWaitForPlacementList.add(studInfo);
					}
					continue;	// 跳过后面操作
				}
				// 否则把学生信息放入组合列表中
				StudentInfo studInfo = new StudentInfo();
				studInfo.setAccountId(accId);
				studInfo.setClassId(classId);
				studInfo.setSchoolId(schoolId);
				studInfo.setPlacementId(placementId);
				studInfo.setTermInfo(termInfo);
				studInfo.setName(name);
				studInfo.setNameSpelling(nameSpelling);
				studInfo.setGender(gender);
				studInfo.setType(1);	// 拥有自愿，自动分班
				studInfo.setSubjectIdsStr(subjectIdsStr);	// 添加学生自愿
				// 组合数据
				zhStudInfos.add(studInfo);
				
			} else {	// 如果科目志愿不在组合集合中，那么就按照单科分班
//				continue;
				if(teachOpClassTaskId != null) {	// 判断是否已经分了教学班
					Set<Long> accIdSet = opClassTaskId2AccId.get(teachOpClassTaskId);
					if(accIdSet == null || !accIdSet.contains(accId)){
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						
						studInfo.setOpenClassTaskId(teachOpClassTaskId);
						
						studInfoWaitForPlacementList.add(studInfo);
					}
				} else {
					StudentInfo studInfo = new StudentInfo();
					studInfo.setAccountId(accId);
					studInfo.setClassId(classId);
					studInfo.setSchoolId(schoolId);
					studInfo.setPlacementId(placementId);
					studInfo.setTermInfo(termInfo);
					studInfo.setName(name);
					studInfo.setNameSpelling(nameSpelling);
					studInfo.setGender(gender);
					studInfo.setType(1);	// 拥有自愿，自动分班
					
					teachingStudInfoList.add(studInfo);
				}
				
				Set<String> subjectIdSet = StringUtil.convertToSetFromStr(subjectIdsStr, ",", String.class);	// 拆分科目
				for(String gaoSubjectId : subjectIdSet) {	// 高考科目
					OpenClassTask opClassTask = dkGaoSubjId2OpClassTask.get(gaoSubjectId);
					if(opClassTask != null) {
						Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());
						if(accIdSet == null || !accIdSet.contains(accId)){
							StudentInfo studInfo = new StudentInfo();
							studInfo.setAccountId(accId);
							studInfo.setClassId(classId);
							studInfo.setSchoolId(schoolId);
							studInfo.setPlacementId(placementId);
							studInfo.setTermInfo(termInfo);
							studInfo.setName(name);
							studInfo.setNameSpelling(nameSpelling);
							studInfo.setGender(gender);
							
							studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
							studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
							
							studInfoWaitForPlacementList.add(studInfo);
						}
					} else {
						if(!subjectId2GaoList.containsKey(gaoSubjectId)){
							subjectId2GaoList.put(gaoSubjectId, new ArrayList<StudentInfo>());
						}
						
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						studInfo.setSubjectIdsStr(gaoSubjectId);
						studInfo.setType(1);	// 拥有自愿，自动分班
						
						subjectId2GaoList.get(gaoSubjectId).add(studInfo);
					}
				}
				
				for(String xueSubjectId : dkSubjectIdSet) {	
					if(subjectIdSet.contains(xueSubjectId)) {
						continue;
					}// 学考科目
					
					OpenClassTask opClassTask = dkXueSubjId2OpClassTask.get(xueSubjectId);
					if(opClassTask != null) {
						Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());
						if(accIdSet == null || !accIdSet.contains(accId)){
							StudentInfo studInfo = new StudentInfo();
							studInfo.setAccountId(accId);
							studInfo.setClassId(classId);
							studInfo.setSchoolId(schoolId);
							studInfo.setPlacementId(placementId);
							studInfo.setTermInfo(termInfo);
							studInfo.setName(name);
							studInfo.setNameSpelling(nameSpelling);
							studInfo.setGender(gender);
							
							studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
							studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
							
							studInfoWaitForPlacementList.add(studInfo);
						}
					} else {
						if(!subjectId2XueList.containsKey(xueSubjectId)){
							subjectId2XueList.put(xueSubjectId, new ArrayList<StudentInfo>());
						}
						
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						studInfo.setSubjectIdsStr(xueSubjectId);
						studInfo.setType(1);	// 拥有自愿，自动分班
						
						subjectId2XueList.get(xueSubjectId).add(studInfo);
					}
				}
			}
			
		}

		setProgress(28, "保存未选课的学生数据");
		
		if(studInfoWaitForPlacementList.size() > 0) {	// 没有志愿的学生插入未排区
			placementTaskDao.insertStudentInfoWaitForPlacementBatch(studInfoWaitForPlacementList, termInfo);
		}
		
		setProgress(30, "保存未选课的学生数据");
		// 组合分班
		execZhPlacement(placementTask, zhStudInfos, zhOpClassTaskList);
		// 教学班分班
//		execTeachingPlacement(placementTask, teachingStudInfoList, tOpClassTask);
		// 单科分班
//		execDkPlacement(placementTask, subjectId2GaoList, subjectId2XueList, dkOpClassTaskList);
	
		updateOpenClassTaskStatus(opClassTaskListStatus0, schoolId, termInfo);
	}
	
	
	private void execWishPlacementLarge(PlacementTask placementTask) {
		
		setProgress(0, "进行大走班分班操作");
		
		Long schoolId = request.getLong("schoolId");
		String placementId = request.getString("placementId");
		String termInfo = request.getString("termInfo");
		String wfId = placementTask.getWfId();
		String wfTermInfo = placementTask.getWfTermInfo();

		setProgress(3, "获取开班任务数据");
		
		// 数据库查询数据
		request.put("status", 0);
		List<OpenClassTask> opClassTaskListStatus0 = placementTaskDao.queryOpenClassTasksWithInfo(request);

		Set<String> dkSubjectIdSet = new HashSet<String>();
		
		Set<String> opClassTaskIds = new HashSet<String>();
		for(OpenClassTask opClassTask : opClassTaskListStatus0){
			dkSubjectIdSet.add(opClassTask.getOpenClassInfo().getSubjectIdsStr());
			
			opClassTaskIds.add(opClassTask.getOpenClassTaskId());
		}
		
		// 删除没有志愿的人，重新判断是否填写了志愿
		request.put("openClassTaskId", "");
		placementTaskDao.deleteStudentInfoWaitForPlacement(request);
		request.remove("openClassTaskId");
		// 删除状态为0的组合开班任务对应的教学班级、已分配班级的学生信息、未分配班级的学生信息数据
		if(opClassTaskIds.size() > 0) {	
			// 删除变更任务的学生和教学班级
			request.put("openClassTaskIds", opClassTaskIds);
			
			// 如果后来修改志愿，给志愿那边提供接口，同步删除这些数据
			placementTaskDao.deleteStudentInfoWaitForPlacement(request);
			placementTaskDao.deleteStudentInfo(request);
			placementTaskDao.deleteTeachingClassInfo(request);
			request.remove("openClassTaskIds");
		}
		
		// 三三组合是一对一的，判断开班任务是否已经被用来开班
		Map<String, OpenClassTask> dkGaoSubjId2OpClassTask = new HashMap<String, OpenClassTask>();
		Map<String, OpenClassTask> dkXueSubjId2OpClassTask = new HashMap<String, OpenClassTask>();
		
		request.put("status", 1);
		List<OpenClassTask> opClassTaskListStatus1 = placementTaskDao.queryOpenClassTasksWithInfo(request);
		for(OpenClassTask opClassTask : opClassTaskListStatus1) {
			OpenClassInfo opClassInfo = opClassTask.getOpenClassInfo();
			
			dkSubjectIdSet.add(opClassInfo.getSubjectIdsStr());
			
			if(opClassTask.getSubjectLevel() == 1) {	// 高考
				dkGaoSubjId2OpClassTask.put(opClassInfo.getSubjectIdsStr(), opClassTask);
			} else if(opClassTask.getSubjectLevel() == 2) {	// 学考
				dkXueSubjId2OpClassTask.put(opClassInfo.getSubjectIdsStr(), opClassTask);
			}
		}
		
		// 开班任务id对应学生账户id
		Map<String, Set<Long>> opClassTaskId2AccId = new HashMap<String, Set<Long>>();
		
		List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfo(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		studInfoList = placementTaskDao.queryStudentInfoWaitForPlacement(request);
		for(StudentInfo studInfo : studInfoList) {
			String openClassTaskId = studInfo.getOpenClassTaskId();
			if(!opClassTaskId2AccId.containsKey(openClassTaskId)) {
				opClassTaskId2AccId.put(openClassTaskId, new HashSet<Long>());
			}
			opClassTaskId2AccId.get(openClassTaskId).add(studInfo.getAccountId());
		}
		
		//**************************************** 获取所有志愿数据 ****************************************//
		List<JSONObject> wishStudList = wishFillingThirdService.getSubjectStudentListToThird(wfId, wfTermInfo, schoolId);
		// 所有填写志愿的人对应其选择的志愿科目
		Map<Long, Set<String>> accId2SubjectIds = new HashMap<Long, Set<String>>();
		if (CollectionUtils.isNotEmpty(wishStudList)) {
			for (JSONObject json : wishStudList) {
				Long accountId = json.getLong("accountId");
				if (accountId == null) {
					continue;
				}
				
				if(!accId2SubjectIds.containsKey(accountId)){
					accId2SubjectIds.put(accountId, new HashSet<String>());
				}
				String subjectId = json.getString("subjectId");
				if (dkSubjectIdSet.contains(subjectId)){	// 如果科目代码不包含在单科科目集合中
					// 不要使用wishSubjectIdSet在此处过滤，否则无法计算志愿人数
					accId2SubjectIds.get(accountId).add(subjectId);
				}
			}
		}
		
		String usedGrade = placementTask.getUsedGrade();
		List<Account> accList = getAccountBatch(schoolId, usedGrade, termInfo);
		if (CollectionUtils.isEmpty(accList)) {
			throw new CommonRunException(-1, "从SDK无法获取对应的班级的学生数据，请联系管理员！");
		}

		// 学生志愿学生信息
		Map<String, List<StudentInfo>> subjectId2GaoList = new HashMap<String, List<StudentInfo>>();
		Map<String, List<StudentInfo>> subjectId2XueList = new HashMap<String, List<StudentInfo>>();
		// 未填写志愿的学生信息
		List<StudentInfo> studInfoWaitForPlacementList = new ArrayList<StudentInfo>();
		for (Account acc : accList) {
			StudentPart studPart = getStudentPart(acc);
			if (studPart == null) { // 没有学生部分
				continue;
			}
			long accId = acc.getId();
			long classId = studPart.getClassId();
			String nameSpelling = studPart.getNameSpelling();
			String name = acc.getName();
			T_Gender gender = acc.getGender();
			
			Set<String> wishSubjectIds = accId2SubjectIds.get(accId);
			if(wishSubjectIds == null) {
				StudentInfo studInfo = new StudentInfo();
				studInfo.setAccountId(accId);
				studInfo.setClassId(classId);
				studInfo.setSchoolId(schoolId);
				studInfo.setPlacementId(placementId);
				studInfo.setTermInfo(termInfo);
				studInfo.setName(name);
				studInfo.setNameSpelling(nameSpelling);
				studInfo.setGender(gender);
				studInfo.setOpenClassTaskId("");
				
				studInfoWaitForPlacementList.add(studInfo);
				continue;
			}
			
			for(String subjectId : wishSubjectIds) {
				OpenClassTask opClassTask = dkGaoSubjId2OpClassTask.get(subjectId);
				if(opClassTask != null){
					Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());
					if(accIdSet == null || !accIdSet.contains(accId)) {
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						
						studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
						studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
						
						studInfoWaitForPlacementList.add(studInfo);
					}
				} else {
					if(!subjectId2GaoList.containsKey(subjectId)) {
						subjectId2GaoList.put(subjectId, new ArrayList<StudentInfo>());
					}
					
					StudentInfo studInfo = new StudentInfo();
					studInfo.setAccountId(accId);
					studInfo.setClassId(classId);
					studInfo.setSchoolId(schoolId);
					studInfo.setPlacementId(placementId);
					studInfo.setTermInfo(termInfo);
					studInfo.setName(name);
					studInfo.setNameSpelling(nameSpelling);
					studInfo.setGender(gender);
					studInfo.setType(1);	// 拥有自愿，参与自动分班
					
					subjectId2GaoList.get(subjectId).add(studInfo);
				}
			}
			
			for(String subjectId : dkSubjectIdSet) {
				if(wishSubjectIds.contains(subjectId)){
					continue;
				}
				
				OpenClassTask opClassTask = dkGaoSubjId2OpClassTask.get(subjectId);
				if(opClassTask != null){
					Set<Long> accIdSet = opClassTaskId2AccId.get(opClassTask.getOpenClassTaskId());
					if(accIdSet == null || !accIdSet.contains(accId)) {
						StudentInfo studInfo = new StudentInfo();
						studInfo.setAccountId(accId);
						studInfo.setClassId(classId);
						studInfo.setSchoolId(schoolId);
						studInfo.setPlacementId(placementId);
						studInfo.setTermInfo(termInfo);
						studInfo.setName(name);
						studInfo.setNameSpelling(nameSpelling);
						studInfo.setGender(gender);
						
						studInfo.setOpenClassInfoId(opClassTask.getOpenClassInfoId());
						studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
						
						studInfoWaitForPlacementList.add(studInfo);
					}
				} else {
					if(!subjectId2XueList.containsKey(subjectId)) {
						subjectId2XueList.put(subjectId, new ArrayList<StudentInfo>());
					}
					
					StudentInfo studInfo = new StudentInfo();
					studInfo.setAccountId(accId);
					studInfo.setClassId(classId);
					studInfo.setSchoolId(schoolId);
					studInfo.setPlacementId(placementId);
					studInfo.setTermInfo(termInfo);
					studInfo.setName(name);
					studInfo.setNameSpelling(nameSpelling);
					studInfo.setGender(gender);
					studInfo.setType(1);	// 拥有自愿，参与自动分班
					
					subjectId2XueList.get(subjectId).add(studInfo);
				}
			}
		}

		if(studInfoWaitForPlacementList.size() > 0) {	// 没有志愿的学生插入未排区
			placementTaskDao.insertStudentInfoWaitForPlacementBatch(studInfoWaitForPlacementList, termInfo);
		}
		
		execDkPlacement(placementTask, subjectId2GaoList, subjectId2XueList, opClassTaskListStatus0);
		
		updateOpenClassTaskStatus(opClassTaskListStatus0, schoolId, termInfo);
	}
	
	
	private void execLayerPlacementLarge(PlacementTask placementTask) {
		setProgress(3, "按成绩分层");
		Long schoolId = request.getLong("schoolId");
		String termInfo = request.getString("termInfo");
		String placementId = request.getString("placementId");
		
		// 数据库查询数据
		request.put("status", 0);
		List<OpenClassTask> opClassTaskListStatus0 = placementTaskDao.queryOpenClassTasksWithInfo(request);
		if(CollectionUtils.isEmpty(opClassTaskListStatus0)) {
			return ;
		}
		
		setProgress(10, "进行分层分班操作");
		Set<String> opClassTaskIdSet = new HashSet<String>();
		for(OpenClassTask opClassTask : opClassTaskListStatus0) {
			opClassTaskIdSet.add(opClassTask.getOpenClassTaskId());
		}
		
		setProgress(20, "移除对应的教学班级和学生信息数据");
		
		// 删除状态为0的组合开班任务对应的教学班级、已分配班级的学生信息、未分配班级的学生信息数据
		if(opClassTaskIdSet.size() > 0) {	
			// 删除变更任务的学生和教学班级
			request.put("openClassTaskIds", opClassTaskIdSet);
			placementTaskDao.deleteTeachingClassInfo(request);
			request.remove("openClassTaskIds");
		}
		
		setProgress(35, "进行大走班分层分班操作");
		String usedGrade = placementTask.getUsedGrade();
		Map<String, List<OpenClassTask>> opClassInfoId2ObjList = new HashMap<String, List<OpenClassTask>>();
		for(OpenClassTask opClassTask : opClassTaskListStatus0) {
			String opClassInfoId = opClassTask.getOpenClassInfoId();
			Integer subjectLevel = opClassTask.getSubjectLevel();
			if(subjectLevel == null || subjectLevel == 0){
				throw new CommonRunException(-1, "大走班分班层次为空，请联系管理员！");
			}
			
			if(!opClassInfoId2ObjList.containsKey(opClassInfoId)){
				opClassInfoId2ObjList.put(opClassInfoId, new ArrayList<OpenClassTask>());
			}
			
			opClassInfoId2ObjList.get(opClassInfoId).add(opClassTask);
			
			List<TeachingClassInfo> tClassInfos = createTeachingClass(opClassTask, schoolId, usedGrade, placementId,
					termInfo);
			if(CollectionUtils.isEmpty(tClassInfos)){
				continue;
			}
			placementTaskDao.insertTeachingClassInfoBatch(tClassInfos, termInfo);
		}

		setProgress(40, "更新学生信息");
		for(Map.Entry<String, List<OpenClassTask>> entry : opClassInfoId2ObjList.entrySet()){
			String opClassInfoId = entry.getKey();
			request.put("openClassInfoId", opClassInfoId);
			List<StudentInfo> studInfoList = placementTaskDao.queryStudentInfoWaitForPlacement(request);
			placementTaskDao.deleteStudentInfoWaitForPlacement(request);
			if(CollectionUtils.isEmpty(studInfoList)){
				continue;
			}
			Collections.sort(studInfoList, new Comparator<StudentInfo>(){
				@Override
				public int compare(StudentInfo o1, StudentInfo o2) {
					Float sort1 = o1.getSortField();
					Float sort2 = o2.getSortField();
					return -Float.compare(sort1, sort2);
				}
			});
			int index = 0;
			List<OpenClassTask> list = entry.getValue();
			Collections.sort(list, new Comparator<OpenClassTask>(){
				@Override
				public int compare(OpenClassTask o1, OpenClassTask o2) {
					Integer level1 = o1.getSubjectLevel();
					Integer level2 = o2.getSubjectLevel();
					return Integer.compare(level1, level2);
				}
			});
			List<StudentInfo> insertStudList = new ArrayList<StudentInfo>();
			for(OpenClassTask opClassTask : list){
				Integer numOfStuds = opClassTask.getNumOfStuds();
				for(int i = 0; i < numOfStuds; i ++, index ++){
					StudentInfo studInfo = studInfoList.get(index);
					studInfo.setOpenClassTaskId(opClassTask.getOpenClassTaskId());
					insertStudList.add(studInfo);
					if(insertStudList.size() > 50) {	// 每50条批量插入记录
						placementTaskDao.insertStudentInfoWaitForPlacementBatch(insertStudList, termInfo);
						insertStudList.clear();
					}
				}
			}
			if(insertStudList.size() > 0) {
				placementTaskDao.insertStudentInfoWaitForPlacementBatch(insertStudList, termInfo);
				insertStudList.clear();
			}
			setProgress(40 + 50 / opClassInfoId2ObjList.size(), "更新学生信息");
		}
		
		setProgress(90, "更新开班任务状态");
		updateOpenClassTaskStatus(opClassTaskListStatus0, schoolId, termInfo);
	}
	
	class ScoreSort implements Comparator<StudentInfo> {
		private Long[] subjectIds;

		public ScoreSort() {
		}

		public ScoreSort(Long[] subjectIds) {
			this.subjectIds = subjectIds;
		}

		@Override
		public int compare(StudentInfo o1, StudentInfo o2) {
			if (subjectIds == null || subjectIds.length == 0) {
				return -Float.compare(o1.getTotalScore(), o2.getTotalScore());
			}
			return -Float.compare(o1.getScore(subjectIds), o2.getScore(subjectIds));
		}
	}
}
