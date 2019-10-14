package com.talkweb.placementtask.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.PlacementConstant.ClassLevel;
import com.talkweb.base.common.PlacementConstant.ClassType;
import com.talkweb.base.common.PlacementConstant.PlacementType;
import com.talkweb.base.common.PlacementConstant.SubjectType;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.dao.PlacementImportDao;
import com.talkweb.placementtask.dao.PlacementTaskDao;
import com.talkweb.placementtask.domain.PlacementTask;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.service.PlacementImportService;
import com.talkweb.placementtask.utils.ImportCacheContext;
import com.talkweb.placementtask.vo.AcademicElectiveCross;
import com.talkweb.placementtask.vo.PlacementInfo;
import com.talkweb.placementtask.vo.SubjectClassInfo;
import com.talkweb.placementtask.vo.SubjectGroupIdInfo;

@Service
public class PlacementImportServiceImpl implements PlacementImportService{
	
	Logger logger = LoggerFactory.getLogger(PlacementImportServiceImpl.class);
	
	@Autowired
	PlacementTaskDao placementTaskDao;
	
	@Autowired
	AllCommonDataService allCommonDataService;
	
	@Autowired
	PlacementImportDao placementImportDao;
	
	@Override
	@Transactional
	public void importPlacementInfo(String placementId, String termInfo, String schoolId,
			List<PlacementInfo> placementRowsList, List<AcademicElectiveCross> crossRowsList) {
		
		
		Assert.notEmpty(placementRowsList, "学生分班导入数据不能为空");
		
		Map<String, Object> params = new HashMap<>();
		params.put("placementId", placementId);
		params.put("termInfo", termInfo);
		params.put("schoolId", schoolId);
		PlacementTask placementTask = placementTaskDao.queryPlacementTaskById(params);
		Assert.notNull(placementTask, "不存在分班id:"+placementId);
		
		PlacementType placementType = PlacementType.getEnumByCode(placementTask.getPlacementType());
		Assert.notNull(placementType, "不支持的走班类型:"+placementTask.getPlacementType());
		
		if (!PlacementType.FIXED_TWO_GO_ONE.equals(placementType)
				&& !PlacementType.BIG_GO_CLASS.equals(placementType)) {
			throw new IllegalArgumentException("目前仅支持大走班和定二走一分班信息导入");
		}
		
		//初始化导入数据上下文
		ImportCacheContext context = new ImportCacheContext();
		
		//准备科目组和班级组信息
		List<SubjectGroupIdInfo> subjectGroupIdInfoList = placementImportDao
				.selectDistinctSubjectGroupIdByPlacementId(termInfo, placementId, schoolId);
		Assert.notEmpty(subjectGroupIdInfoList, "查询科目组信息为空");
		for (SubjectGroupIdInfo subjectGroupIdInfo : subjectGroupIdInfoList) {
			context.getSubjectId2SubjectGroupId().put(subjectGroupIdInfo.getSubjectId(), subjectGroupIdInfo.getSubjectGroupId());
		}
		
		List<String> classIdList = placementImportDao.selectDistinctClassGroupIdByPlacementId(termInfo, placementId, schoolId);
		Assert.notEmpty(classIdList, "查询班级组信息为空");
		if(classIdList.size() > 1) {throw new IllegalArgumentException("查询多条班级组信息，请确认当前分班数据");}
		context.setClassGroupId(classIdList.get(0));
		
		//准备学生班级信息
		School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
		
		Map<Long, String> classId2ClassName = new HashMap<>();
		List<Classroom> classroomList = allCommonDataService.getAllClass(school, termInfo);
		Assert.notEmpty(classroomList, "查询学校["+schoolId+"]学期["+termInfo+"]的班级数据为空");
		for (Classroom classroom : classroomList) {
			classId2ClassName.put(classroom.getId(), classroom.getClassName());
		}
		//classId2ClassName.put(2017301L, "G1701"); //学生011
		//classId2ClassName.put(2017301L, "G1701"); //学生011
		
		List<Account> accountList = allCommonDataService.getAllStudent(school, termInfo);
		//logger.debug(JSONObject.toJSONString(accountList));
		
		for (Account account : accountList) {
			for (User user : account.getUsers()) {
				long classId = user.getStudentPart().getClassId();
				long studentId = account.getId();
				String className = classId2ClassName.get(classId);
				int key = Objects.hash(account.getName(), className);
				context.getClassNameAndStudentName2StudentId().put(key, studentId);
			}
		}
		//logger.debug(JSONObject.toJSONString(accountList));
		Assert.notEmpty(classroomList, "查询学校["+schoolId+"]学期["+termInfo+"]的学生数据为空");
		
		//开始循环数据行
		int placementRowNum = 0;
    	for (PlacementInfo placementInfo : placementRowsList) {
    		
    		try {
    			
    			placementRowNum ++;
    			
    			Assert.hasText(placementInfo.getBaseClassName(), "基础数据班级不能为空");
    			Assert.hasText(placementInfo.getStudentName(), "学生不能为空");
    			Assert.hasText(placementInfo.getSubjectCombination(), "志愿组合为空");
    			Assert.hasText(placementInfo.getSubjectCombinationIds(), "志愿组合科目id不能为空");
    			Assert.hasText(placementInfo.getAdminClassSiteName(), "行政班场地不能为空");
    			Assert.hasText(placementInfo.getAdminClassName(), "行政班名称不能为空");
    			
        		List<String> wishKeys = new ArrayList<>();
        		
        		//定二走一缓存去重得到行政班信息
        		TPlDezyClass adminDezyClass = null;
        		if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
        			
    	    		SubjectClassInfo adminClassInfo =this.getRowAdminClass(placementInfo); 
    	    		adminDezyClass = this.cacheDistinctDezyClass(placementTask, placementInfo, adminClassInfo, ClassType.ADMIN_CLASS, context);
    	    		
    	    		//添加志愿分组字段
    	    		wishKeys.add(placementInfo.getAdminClassName());
        		}
        		
        		//循环所有教学班
        		List<SubjectClassInfo> teachList = this.getRowTeachClassList(placementInfo);
        		for (SubjectClassInfo subInfo : teachList) {
        			
        			//缓存去重得到教学班级信息
        			TPlDezyClass teachDezyClass = this.cacheDistinctDezyClass(placementTask, placementInfo, subInfo, ClassType.TEACH_CLASS, context);
        			
        			//添加志愿分组字段
        			wishKeys.add(subInfo.getClassName());
        			
        			//如果是定二走一组装行政班和教学班二二对应关系
        			if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
        				this.cacheTeachAndAdminMapping(placementTask, placementInfo, adminDezyClass, teachDezyClass, context);
        			}
    			}
        		
        		//志原去重
        		int key = Arrays.hashCode(wishKeys.toArray());
    			TPlDezySubjectcomp subjectcomp = this.cacheDistinctDezySubjectcomp(key, placementTask, placementInfo,
    					placementType, context);

    			//学生对应班级
    			this.cacheSubjectcompStudent(placementTask, subjectcomp, placementInfo, context);
    		}catch(Exception e) {
    			logger.debug("处理学生分班数据行[{}]异常:{}, 数据:{}",placementRowNum, e.getMessage(), JSONObject.toJSONString(placementInfo));
    			throw new IllegalArgumentException("处理学生分班数据行["+placementRowNum+"]异常:"+e.getMessage(), e);
    		}
		}
    	
    	//组装学选交叉数据
    	List<TPlConfIndexSubs> tPlConfIndexSubsList = new ArrayList<>();
    	int corssRowNum = 0;
    	for (AcademicElectiveCross cross : crossRowsList) {
    		
    		try {
    			
    			corssRowNum ++;
    			TPlConfIndexSubs tPlConfIndexSubs = new TPlConfIndexSubs();
        		tPlConfIndexSubs.setPlacementId(placementTask.getPlacementId());
        		tPlConfIndexSubs.setSchoolId(String.valueOf(placementTask.getSchoolId()));
        		tPlConfIndexSubs.setUsedGrade(placementTask.getUsedGrade());
        		tPlConfIndexSubs.setIdx(Integer.parseInt(cross.getIdx()));
        		ClassLevel classLevel = ClassLevel.getEnumByLabel(cross.getIsOpt());
        		tPlConfIndexSubs.setIsOpt(classLevel.getCode());
        		tPlConfIndexSubs.setSeq(Integer.parseInt(cross.getSeq()));
        		tPlConfIndexSubs.setSubIds(cross.getSubIds());
        		tPlConfIndexSubs.setIsSeqRebuild(Integer.parseInt(cross.getIsSeqRebuild()));
        		tPlConfIndexSubs.setReSeq(Integer.parseInt(cross.getReSeq()));
        		tPlConfIndexSubs.setIsIdxRebuild(0);
        		tPlConfIndexSubs.setReIdx(0);
        		tPlConfIndexSubsList.add(tPlConfIndexSubs);
        		
    		}catch(Exception e) {
    			logger.debug("处理学选交叉数据行[{}]异常:{}, 数据:{}",corssRowNum, e.getMessage(), JSONObject.toJSONString(cross));
    			throw new IllegalArgumentException("处理学选交叉数据行["+corssRowNum+"]异常:"+e.getMessage(), e);
    		}
		}
    	
    	//重写开班表信息
    	try {
        	int deleteTPlDezyClassNum = placementImportDao.deleteTPlDezyClass(termInfo, placementId, schoolId);
        	int insertAdminTPlDezyClassNum = 0;
        	if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
            	insertAdminTPlDezyClassNum = placementImportDao.batchInsertTPlDezyClassList(termInfo, new ArrayList<TPlDezyClass>(context.getAdminClassName2InfoMap().values()));
        	}
        	int insertTeachTPlDezyClassNum = placementImportDao.batchInsertTPlDezyClassList(termInfo, new ArrayList<TPlDezyClass>(context.getTeachClassName2InfoMap().values()));
        	logger.debug("重写开班表-deleteTPlDezyClassNum：{},insertAdminTPlDezyClassNum：{},inserttTeachTPlDezyClassNum:{}", deleteTPlDezyClassNum, insertAdminTPlDezyClassNum, insertTeachTPlDezyClassNum);
    	}catch(Exception e) {
    		logger.debug("重写开班表异常："+e.getMessage(),e);
    		logger.debug("AdminClassName2InfoMap:{},TeachClassName2InfoMap:{}", JSONObject.toJSONString(context.getAdminClassName2InfoMap()), JSONObject.toJSONString(context.getTeachClassName2InfoMap()));
    		throw e;
    	}

    	//重写行政班对应志愿
    	try {
        	int deleteTPlDezySubjectcompNum = placementImportDao.deleteTPlDezySubjectcomp(termInfo, placementId, schoolId);
        	int insertTPlDezySubjectcompNum = placementImportDao.batchInsertTPlDezySubjectcompList(termInfo, new ArrayList<TPlDezySubjectcomp>(context.getAdminWishId2InfoMap().values()));
        	logger.debug("重写行政班对应志愿组-deleteTPlDezySubjectcompNum：{},insertTPlDezySubjectcompNum：{}", deleteTPlDezySubjectcompNum, insertTPlDezySubjectcompNum);
    	}catch(Exception e) {
    		logger.debug("重写行政班对应志愿组异常："+e.getMessage(),e);
    		logger.debug("AdminWishId2InfoMap:{}", JSONObject.toJSONString(context.getAdminWishId2InfoMap()));
    		throw e;
    	}

    	//重写教学班对应志愿
    	try {
        	int deleteTPlDezyTclassSubcompNum = placementImportDao.deleteTPlDezyTclassSubcomp(termInfo, placementId, schoolId);
        	int insertTPlDezyTclassSubcompNum = placementImportDao.batchInsertTPlDezyTclassSubcompList(termInfo, context.getTeachWishList());
        	logger.debug("重写教学班对应志愿组-deleteTPlDezyTclassSubcompNum：{},insertTPlDezyTclassSubcompNum：{}", deleteTPlDezyTclassSubcompNum, insertTPlDezyTclassSubcompNum);
        	
    	}catch(Exception e) {
    		logger.debug("重写教学班对应志愿组异常："+e.getMessage(),e);
    		logger.debug("TeachWishList:{}", JSONObject.toJSONString(context.getTeachWishList()));
    		throw e;
    	}

    	//重写教学班所属的行政班
    	try {
        	if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
            	int deleteTPlDezyTclassfromNum = placementImportDao.deleteTPlDezyTclassfrom(termInfo, placementId, schoolId);
            	int insertTPlDezyTclassfromNum = placementImportDao.batchInsertTPlDezyTclassfromList(termInfo, new ArrayList<TPlDezyTclassfrom>(context.getTeachAndAdminMappingMap().values()));
            	logger.debug("重写教学班所属的行政班-deleteTPlDezyTclassfromNum：{},insertTPlDezyTclassfromNum：{}", deleteTPlDezyTclassfromNum, insertTPlDezyTclassfromNum);
        	}
    	}catch(Exception e) {
    		logger.debug("重写教学班所属的行政班异常："+e.getMessage(),e);
    		logger.debug("TeachAndAdminMappingMap:{}", JSONObject.toJSONString(context.getTeachAndAdminMappingMap()));
    		throw e;
    	}

    	//重写学生对应班级
    	try {
        	int deleteTPlDezySubjectcompStudentNum = placementImportDao.deleteTPlDezySubjectcompStudent(termInfo, placementId, schoolId);
        	int insertTPlDezySubjectcompStudentNum = placementImportDao.batchInsertTPlDezySubjectcompStudentList(termInfo, context.getStudentClassList());
        	logger.debug("重写学生对应志原信息-deleteTPlDezySubjectcompStudentNum：{},insertTPlDezySubjectcompStudentNum：{}", deleteTPlDezySubjectcompStudentNum, insertTPlDezySubjectcompStudentNum);
        	
    	}catch(Exception e) {
    		logger.debug("重写学生对应志原信息异常："+e.getMessage(),e);
    		logger.debug("StudentClassList:{}", JSONObject.toJSONString(context.getStudentClassList()));
    		throw e;
    	}
    	
    	//重写学选交叉数据
    	try {
    		
    		if(null!=tPlConfIndexSubsList && tPlConfIndexSubsList.size()>0) {
            	int deleteTPlConfIndexSubsNum = placementImportDao.deleteTPlConfIndexSubs(termInfo, placementId, schoolId);
            	int insertTPlConfIndexSubsNum = placementImportDao.batchInsertTPlConfIndexSubsList(termInfo, tPlConfIndexSubsList);
            	logger.debug("重写学选交叉数据-deleteTPlConfIndexSubsNum:{},insertTPlConfIndexSubsNum:{}", deleteTPlConfIndexSubsNum, insertTPlConfIndexSubsNum);
    		}

    	}catch(Exception e) {
    		logger.debug("重写学选交叉数据异常："+e.getMessage(),e);
    		logger.debug("tPlConfIndexSubsList:{}", JSONObject.toJSONString(tPlConfIndexSubsList));
    		throw e;
    	}

    	//更新班级组下班级ids
    	try {
        	if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
        		placementImportDao.updateClassGroupClassIds(termInfo, placementId, schoolId, context.getClassIdForclassGroupId());
        	}
    	}catch(Exception e) {
    		logger.debug("更新班级组下班级ids异常："+e.getMessage(),e);
    		logger.debug("ClassIdForclassGroupId:{}", JSONObject.toJSONString(context.getClassIdForclassGroupId()));
    		throw e;
    	}

    	
	}
	
	
	private void cacheTeachAndAdminMapping(PlacementTask placementTask, PlacementInfo placementInfo, TPlDezyClass adminDezyClass,
			TPlDezyClass teachDezyClass, ImportCacheContext context) {
		
		int key = Objects.hash(adminDezyClass.getTclassName(), teachDezyClass.getTclassName());
		TPlDezyTclassfrom tclassfrom = context.getTeachAndAdminMappingMap().get(key);
		if(null == tclassfrom) {
			
			tclassfrom = new TPlDezyTclassfrom();
			tclassfrom.setPlacementId(placementTask.getPlacementId());
			tclassfrom.setSchoolId(String.valueOf(placementTask.getSchoolId()));
			tclassfrom.setUsedGrade(placementTask.getUsedGrade());
			//教学班所属的科目组（从t_pl_dezy_class表找教学班对应的字段）
			tclassfrom.setSubjectGroupId(teachDezyClass.getGroundId());
			//行政班所属的分组（从t_pl_dezy_class表找行政班对应的字段）
			tclassfrom.setClassGroupId(adminDezyClass.getClassGroupId());
			tclassfrom.setTclassId(teachDezyClass.getTclassId());
			tclassfrom.setClassId(adminDezyClass.getTclassId());
			context.getTeachAndAdminMappingMap().put(key, tclassfrom);
		}
	}
	
	/**
	 * 学生对应班级
	 */
	private void cacheSubjectcompStudent(PlacementTask placementTask, TPlDezySubjectcomp subjectcomp,
			PlacementInfo placementInfo, ImportCacheContext context) {
		
		TPlDezySubjectcompStudent tPlDezySubjectcompStudent = new TPlDezySubjectcompStudent();
		tPlDezySubjectcompStudent.setPlacementId(placementTask.getPlacementId());
		tPlDezySubjectcompStudent.setSchoolId(String.valueOf(placementTask.getSchoolId()));
		tPlDezySubjectcompStudent.setUsedGrade(placementTask.getUsedGrade());
		tPlDezySubjectcompStudent.setSubjectCompId(subjectcomp.getSubjectCompId());
		
		int key = Objects.hash(placementInfo.getStudentName(),placementInfo.getBaseClassName());
		Long studentId = context.getClassNameAndStudentName2StudentId().get(key);
		Assert.notNull(studentId, "从基础数据中匹配用户["+placementInfo.getStudentName()+"]和班级["+placementInfo.getBaseClassName()+"]的学生信息为空");
		tPlDezySubjectcompStudent.setStudentId(String.valueOf(studentId));
		context.getStudentClassList().add(tPlDezySubjectcompStudent);
	}
	
	private TPlDezySubjectcomp cacheDistinctDezySubjectcomp(int key, PlacementTask placementTask,
			PlacementInfo placementInfo, PlacementType placementType, ImportCacheContext context) {

		TPlDezySubjectcomp tPlDezySubjectcomp = context.getAdminWishId2InfoMap().get(key);
		if(null == tPlDezySubjectcomp) {
			
			//行政班对应志愿组
			tPlDezySubjectcomp = new TPlDezySubjectcomp();
			tPlDezySubjectcomp.setPlacementId(placementTask.getPlacementId());
			tPlDezySubjectcomp.setSchoolId(String.valueOf(placementTask.getSchoolId()));
			tPlDezySubjectcomp.setUsedGrade(placementTask.getUsedGrade());
			if(PlacementType.FIXED_TWO_GO_ONE.equals(placementType)) {
				TPlDezyClass tPlDezyClass = context.getAdminClassName2InfoMap().get(placementInfo.getAdminClassName());
				tPlDezySubjectcomp.setClassId(tPlDezyClass.getTclassId());
			}else {
				tPlDezySubjectcomp.setClassId("-999");
			}
			String wishId = UUIDUtil.getUUID();
			tPlDezySubjectcomp.setSubjectCompId(wishId);
			tPlDezySubjectcomp.setCompName(placementInfo.getSubjectCombination());
			tPlDezySubjectcomp.setCompFrom(placementInfo.getSubjectCombinationIds());
			tPlDezySubjectcomp.setCompNum(1);
			
			//教学班对应志愿组
			List<SubjectClassInfo> teacheClassList = this.getRowTeachClassList(placementInfo);
			for (SubjectClassInfo subInfo : teacheClassList) {
				TPlDezyTclassSubcomp tPlDezyTclassSubcomp = new TPlDezyTclassSubcomp();
				tPlDezyTclassSubcomp.setPlacementId(placementTask.getPlacementId());
				tPlDezyTclassSubcomp.setSchoolId(String.valueOf(placementTask.getSchoolId()));
				tPlDezyTclassSubcomp.setUsedGrade(placementTask.getUsedGrade());
				tPlDezyTclassSubcomp.setSubjectCompId(wishId);
				TPlDezyClass tPlDezyClass = context.getTeachClassName2InfoMap().get(subInfo.getClassName());
				tPlDezyTclassSubcomp.setTclassId(tPlDezyClass.getTclassId());
				context.getTeachWishList().add(tPlDezyTclassSubcomp);
			}
			context.getAdminWishId2InfoMap().put(key, tPlDezySubjectcomp);
		}else {
			tPlDezySubjectcomp.setCompNum(tPlDezySubjectcomp.getCompNum()+1);
		}
		
		return tPlDezySubjectcomp;
	}
	
	
	/**
	 * 处理行政班和教学班
	 */
	private TPlDezyClass cacheDistinctDezyClass(PlacementTask placementTask, PlacementInfo placementInfo,
			SubjectClassInfo subjectClassInfo, ClassType classType, ImportCacheContext context) {
		
		Map<String, TPlDezyClass> cacheClassName2InfoMap = null;
		if(ClassType.ADMIN_CLASS.equals(classType)) {
			cacheClassName2InfoMap = context.getAdminClassName2InfoMap();
		}else {
			cacheClassName2InfoMap = context.getTeachClassName2InfoMap();
		}
		
		String className = subjectClassInfo.getClassName();
		String classSiteName = subjectClassInfo.getClassSiteName();
		String classSiteId = context.getSiteName2IdMap().get(classSiteName);
		if(null == classSiteId) {
			classSiteId = UUIDUtil.getUUID();
			context.getSiteName2IdMap().put(classSiteName, classSiteId);
		}
		
		
		TPlDezyClass tPlDezyClass = cacheClassName2InfoMap.get(className);
		if(null == tPlDezyClass) {
			String classId = UUIDUtil.getUUID();
			tPlDezyClass = new TPlDezyClass();
			tPlDezyClass.setPlacementId(placementTask.getPlacementId());
			tPlDezyClass.setSchoolId((placementTask.getSchoolId() == null) ? null : placementTask.getSchoolId().toString());
			tPlDezyClass.setUsedGrade(placementTask.getUsedGrade());
			
			//设置科目
			if(ClassType.ADMIN_CLASS.equals(classType)) {
				tPlDezyClass.setSubjectId("-999");
			}else {
				SubjectType subjectType = SubjectType.getEnumByLabel(subjectClassInfo.getSubjectName());
				tPlDezyClass.setSubjectId(subjectType.getCode());
			}
			
			//设置科目组
			String subjectGroupId = context.getSubjectId2SubjectGroupId().get(tPlDezyClass.getSubjectId());
			Assert.hasText(subjectGroupId, "获取科目["+tPlDezyClass.getSubjectId()+"]的科目组id为空");
			tPlDezyClass.setSubjectGroupId(subjectGroupId);
			
			//设置统一的班级组
			tPlDezyClass.setClassGroupId(context.getClassGroupId());
			context.getClassIdForclassGroupId().add(classId);
			
			tPlDezyClass.setTclassId(classId);
			tPlDezyClass.setTclassType(classType.getCode());
			tPlDezyClass.setTclassLevel(subjectClassInfo.getClassType());
			tPlDezyClass.setTclassNum(1);
			tPlDezyClass.setGroundId(classSiteId);
			tPlDezyClass.setGroundName(classSiteName);
			tPlDezyClass.setClassSeq(subjectClassInfo.getClassSeq());
			tPlDezyClass.setTclassName(className);
			tPlDezyClass.setOriClassName(className);
			
			cacheClassName2InfoMap.put(className, tPlDezyClass);
		}else {
			//这么算可能存在重复??
			tPlDezyClass.setTclassNum(tPlDezyClass.getTclassNum()+1);
		}
		
		return tPlDezyClass;
	}
	
	
	public List<SubjectClassInfo> getRowTeachClassList(PlacementInfo placementInfo) {
		
		List<SubjectClassInfo>  teachClassList = new ArrayList<>();
		
		SubjectClassInfo politicsopenClassInfo = new SubjectClassInfo();
		politicsopenClassInfo.setSubjectName("政治");
		politicsopenClassInfo.setClassName(placementInfo.getPoliticsClassName());
		politicsopenClassInfo.setClassSeq(placementInfo.getPoliticsClassSeq());
		politicsopenClassInfo.setClassType(placementInfo.getPoliticsClassType());
		politicsopenClassInfo.setClassSiteName(placementInfo.getPoliticsClassSite());
		teachClassList.add(politicsopenClassInfo);
		
		SubjectClassInfo historyOpenClassInfo = new SubjectClassInfo();
		historyOpenClassInfo.setSubjectName("历史");
		historyOpenClassInfo.setClassName(placementInfo.getHistoryClassName());
		historyOpenClassInfo.setClassSeq(placementInfo.getHistoryClassSeq());
		historyOpenClassInfo.setClassType(placementInfo.getHistoryClassType());
		historyOpenClassInfo.setClassSiteName(placementInfo.getHistoryClassSite());
		teachClassList.add(historyOpenClassInfo);
		
		SubjectClassInfo geographyOpenClassInfo = new SubjectClassInfo();
		
		geographyOpenClassInfo.setSubjectName("地理");
		geographyOpenClassInfo.setClassName(placementInfo.getGeographyClassName());
		geographyOpenClassInfo.setClassSeq(placementInfo.getGeographyClassSeq());
		geographyOpenClassInfo.setClassType(placementInfo.getGeographyClassType());
		geographyOpenClassInfo.setClassSiteName(placementInfo.getGeographyClassSite());
		teachClassList.add(geographyOpenClassInfo);
		
		SubjectClassInfo physicsopenClassInfo = new SubjectClassInfo();

		physicsopenClassInfo.setSubjectName("物理");
		physicsopenClassInfo.setClassName(placementInfo.getPhysicsClassName());
		physicsopenClassInfo.setClassSeq(placementInfo.getPhysicsClassSeq());
		physicsopenClassInfo.setClassType(placementInfo.getPhysicsClassType());
		physicsopenClassInfo.setClassSiteName(placementInfo.getPhysicsClassSite());
		teachClassList.add(physicsopenClassInfo);
		
		SubjectClassInfo chemistryClassInfo = new SubjectClassInfo();

		chemistryClassInfo.setSubjectName("化学");
		chemistryClassInfo.setClassName(placementInfo.getChemistryClassName());
		chemistryClassInfo.setClassSeq(placementInfo.getChemistryClassSeq());
		chemistryClassInfo.setClassType(placementInfo.getChemistryClassType());
		chemistryClassInfo.setClassSiteName(placementInfo.getChemistryClassSite());
		teachClassList.add(chemistryClassInfo);
		
		SubjectClassInfo biologyClassInfo = new SubjectClassInfo();

		biologyClassInfo.setSubjectName("生物");
		biologyClassInfo.setClassName(placementInfo.getBiologyClassName());
		biologyClassInfo.setClassSeq(placementInfo.getBiologyClassSeq());
		biologyClassInfo.setClassType(placementInfo.getBiologyClassType());
		biologyClassInfo.setClassSiteName(placementInfo.getBiologyClassSite());
		teachClassList.add(biologyClassInfo);
		
		if (StringUtils.hasText(placementInfo.getTechnicalClassType())
				&& StringUtils.hasText(placementInfo.getTechnicalClassSeq())
				&& StringUtils.hasText(placementInfo.getTechnicalClassName())
				&& StringUtils.hasText(placementInfo.getTechnicalClassSite())) {
			
			SubjectClassInfo technicalClassInfo = new SubjectClassInfo();
			technicalClassInfo.setSubjectName("技术");
			technicalClassInfo.setClassName(placementInfo.getTechnicalClassName());
			technicalClassInfo.setClassSeq(placementInfo.getTechnicalClassSeq());
			technicalClassInfo.setClassType(placementInfo.getTechnicalClassType());
			technicalClassInfo.setClassSiteName(placementInfo.getTechnicalClassSite());
			teachClassList.add(technicalClassInfo);
		}
		
		return teachClassList;
	}
	
	
	public SubjectClassInfo getRowAdminClass(PlacementInfo placementInfo) {
		SubjectClassInfo subjectClassInfo = new SubjectClassInfo();
		subjectClassInfo.setClassName(placementInfo.getAdminClassName());
		subjectClassInfo.setClassSiteName(placementInfo.getAdminClassSiteName());
		subjectClassInfo.setClassSeq("0");
		subjectClassInfo.setClassType(ClassType.ADMIN_CLASS.getLabel());
		return subjectClassInfo;
	}
}
