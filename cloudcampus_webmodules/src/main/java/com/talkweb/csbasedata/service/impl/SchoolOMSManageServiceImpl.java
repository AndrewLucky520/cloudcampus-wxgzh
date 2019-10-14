package com.talkweb.csbasedata.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_JobType;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.common.tools.MD5Util;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.AdminManageDao;
import com.talkweb.commondata.util.PermissionUtil;
import com.talkweb.csbasedata.dao.OrgManageDao;
import com.talkweb.csbasedata.dao.SchoolOMSManageDao;
import com.talkweb.csbasedata.service.SchoolOMSManageService;
import com.talkweb.csbasedata.util.ChinaInitial;
import com.talkweb.csbasedata.util.DateUtil;
import com.talkweb.csbasedata.util.LessonUtil;
import com.talkweb.csbasedata.util.StringUtil;
import com.talkweb.workbench.service.WorkbenchThridService;


/**
 * 学校OMS管理-serviceImpl
 * @author zhh
 *
 */
@Service("schoolOMSManageService")
public class SchoolOMSManageServiceImpl implements SchoolOMSManageService{
	@Autowired
	private SchoolOMSManageDao schoolOMSManageDao;
	@Autowired
	private AdminManageDao adminManageDao;
	@Autowired
	private OrgManageDao orgManageDao;
	@Autowired
	private WorkbenchThridService workbenchThridService;
	Logger logger = LoggerFactory.getLogger(SchoolOMSManageServiceImpl.class);
/**
 * 学校管理OMS
 * @author zhh
 */

	@Override
	public List<JSONObject> getSelectAreaCodeList(JSONObject obj)
			throws Exception {
		return schoolOMSManageDao.getSelectAreaCodeList(obj);
	}
	@Override
	public JSONObject getSchoolInfo(JSONObject param) throws Exception {
		JSONObject schoolInfo = schoolOMSManageDao.getSchoolInfo(param);
		/*Date createTime = schoolInfo.getDate("createTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String createTimeString = sdf.format(createTime);
		schoolInfo.put("createTime", createTimeString);*/
		if(schoolInfo!=null && StringUtils.isNotBlank(schoolInfo.getString("areaCode"))){
			String areaCode = schoolInfo.getString("areaCode");
			String countryCode = areaCode;
			String cityCode = countryCode.substring(0, 4)+"00";
			String provinceCode = countryCode.substring(0, 2)+"0000" ;
			schoolInfo.put("countryCode", countryCode);
			schoolInfo.put("cityCode",  cityCode);
			schoolInfo.put("provinceCode", provinceCode);
		}
		return schoolInfo;
	}
	@Override
	public int updateSchoolInfo(JSONObject param) throws Exception {
		String schoolId = param.getString("schoolId");
		if(StringUtils.isBlank(schoolId)){ //创建
			return this.createSchool(param);
		}else{//更新
			return this.updateSchool(param);
		}
	}
	//更新学校信息(教学阶段更新 对应的机构没变@@@,年级没变@@@)
	private int updateSchool(JSONObject param) throws Exception{
		String schoolId = param.getString("schoolId");
		//判断学校是否重复 
		param.put("noSchoolId", schoolId);
		List<JSONObject> schoolObjList = schoolOMSManageDao.getSchoolByName(param);
		if(schoolObjList!=null && schoolObjList.size()>0){
			return -1;
		}
		//更新学校
		schoolOMSManageDao.updateSchool(param);
		//删除教学阶段
		schoolOMSManageDao.deleteSchoolStage(param);
		List<String> oldStages = (List<String>) param.get("oldStages");
		List<String> stages = (List<String>) param.get("stages");
		List<JSONObject> stageList = new ArrayList<JSONObject>();
		List<String> toBeInsertStages = new ArrayList<String>();
		if(stages!=null){
			for(String stage:stages){
				if(oldStages!=null && !oldStages.contains(stage)){
					if(!toBeInsertStages.contains(stage)){
						toBeInsertStages.add(stage);
					}
				}
				JSONObject schoolStage = new JSONObject();
				schoolStage.put("schoolId", param.getString("schoolId"));
				schoolStage.put("stage", stage);
				stageList.add(schoolStage);
			}
		}
		//插入年级
		this.insertGrade(schoolId,toBeInsertStages);
		//插入教学阶段
		schoolOMSManageDao.insertSchoolStageBatch(stageList);
		//插入学校学年学期
		schoolOMSManageDao.deleteTermInfo(param);
		this.insertTermInfo(schoolId);
		//插入学校年级对应的默认科目
		schoolOMSManageDao.deleteSchoolLesson(param);
		this.insertLesson(schoolId,stages);
		//插入学校年级阶段关系信息表
		schoolOMSManageDao.deleteSchoolGradeStage(param);
		this.insertSchoolGradeStage(schoolId,stages);
		return 1;
	}
	//创建学校信息
	private int createSchool(JSONObject param) throws Exception {
		int i = 1;
		//判断学校是否重复 
		List<JSONObject> schoolObjList = schoolOMSManageDao.getSchoolByName(param);
		logger.info("[csjcsj schoolObjList]:"+schoolObjList.toString());
		if(schoolObjList!=null && schoolObjList.size()>0){
			return -1;
		}
		String uuid = UUIDUtil.getUUID();
		param.put("uuid", uuid);
		//param.put("createTime", new Date().getTime());
		//插入学校
		schoolOMSManageDao.insertSchool(param);
		logger.info("schoolName:    "+param.toJSONString());
		JSONObject school = schoolOMSManageDao.getSchoolInfo(param);
		logger.info("schoolName2:  "+school.toJSONString());
		String schoolId = school.getString("schoolId");
		List<String> stages = (List<String>) param.get("stages");
		List<JSONObject> stageList = new ArrayList<JSONObject>();
		if(stages!=null){
			for(String stage:stages){
				JSONObject schoolStage = new JSONObject();
				schoolStage.put("schoolId", schoolId);
				schoolStage.put("stage", stage);
				stageList.add(schoolStage);
			}
		}
		//插入教学阶段
		schoolOMSManageDao.insertSchoolStageBatch(stageList);
		//插入学校年级对应的默认科目
		List<String> insertLessonNameList = this.insertLesson(schoolId,stages);
		//插入机构科室、机构年级组、机构教研组、机构备课组
		i = this.insertOrg(param,schoolId,stages,insertLessonNameList);
		//插入教学阶段对应年级
		i = this.insertGrade(schoolId,stages);
		//插入超级管理员
		i = this.insertSchoolManager(param,school);
		//插入学校学年学期
		i = this.insertTermInfo(schoolId);
		//插入学校默认职务类型
		i = this.insertJobType(schoolId);
		//插入学校年级阶段关系信息表
		i = this.insertSchoolGradeStage(schoolId,stages);
		//插入机构类型
		i = this.insertOrgType(schoolId);
		//插入学校平台类
		JSONObject schoolPlate = new JSONObject();
		schoolPlate.put("schoolId", schoolId);
		schoolOMSManageDao.insertSchoolPlate(schoolPlate);
		if(i<0){
			throw new Exception();
		}
		//增加默认菜单
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		/*if(PermissionUtil.ISNEWENTREANCE){
			json.put("type", 2); //新高考学校
		}else{
			json.put("type", 1); //非新高考学校
		}*/
		json.put("type", PermissionUtil.NAVTYPE);
		try {
			workbenchThridService.addNavSchool(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	private int insertGrade(String schoolId,List<String> stages){
		List<JSONObject> gradeList = new ArrayList<JSONObject>();
		Map<String, List<String>> gradeMap = LessonUtil.gradeLevelMap;
		if(stages!=null){
			int i=0;
			for(String stage:stages){
				List<String> levelList = gradeMap.get(stage);
				for(String level:levelList){
					JSONObject grade = new JSONObject();
					grade.put("uuid", UUIDUtil.getUUID());
					grade.put("schoolId", schoolId);
					grade.put("createLevel", level);
					i++;
					grade.put("createTime", DateUtil.getTimeAndAddOneSecond(i));
					gradeList.add(grade);
				}
			}
		}
		schoolOMSManageDao.insertGradeBatch(gradeList);
		return 1;
	}
	private int insertSchoolGradeStage(String schoolId, List<String> stages) {
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		if(stages!=null){
			for(String stage:stages){
				String key = LessonUtil.gradeStageNameMap.get(stage);
				String value =LessonUtil.gradeStageMap.get(stage);
				if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
					param.put(key, value);
				}
			}
		}
		schoolOMSManageDao.insertSchoolGradeStage(param);
		return 1;
	}
	private int insertOrgType(String schoolId) {
		List<JSONObject>  orgTypeList = new ArrayList<JSONObject>();
		for(T_OrgType jt : T_OrgType.values()){
			if(jt.equals(T_OrgType.T_Other)||jt.equals(T_OrgType.T_Manage)){continue;}
			JSONObject orgType = new JSONObject();
			orgType.put("schoolId",schoolId );
			orgType.put("orgTypeId",jt.getValue() );
			orgTypeList.add(orgType);
		}
		schoolOMSManageDao.insertOrgTypeBatch(orgTypeList);
		return 1;
	}
	private int insertJobType(String schoolId) {
		List<JSONObject> jobTypeList = new ArrayList<JSONObject>();
		for(T_JobType jt : T_JobType.values()){
			JSONObject jobTypeObj = new JSONObject();
			jobTypeObj.put("schoolId", schoolId);
			jobTypeObj.put("jobTypeId", jt.getValue());
			jobTypeList.add(jobTypeObj);
		}
		schoolOMSManageDao.insertJobtypeBatch(jobTypeList);
		return 1;
	}
	private List<String> insertLesson(String schoolId, List<String> stages) {
		List<String> lessonNameList = new ArrayList<String>();
		Set<String> lessonIds = new LinkedHashSet<String>();
		if(stages!=null){
			for(String stage:stages){
				lessonIds.addAll(LessonUtil.lessonStageMap.get(stage));
			}
		}
		Map<String,String> lessonIdNameMap = LessonUtil.lessonIdNameMap;
		List<JSONObject> sl = new ArrayList<JSONObject>();
		List<String> lessonIdList =new ArrayList<String>(lessonIds);
		Collections.sort(lessonIdList, new Comparator<String>(){
			@Override
			public int compare(String arg0, String arg1) {
				return Long.compare(Long.valueOf(arg0), Long.valueOf(arg1));
			}
		});
		for(String l:lessonIdList){
			JSONObject slObj = new JSONObject();
			slObj.put("schoolId", schoolId);
			slObj.put("lessonId", l);
			if(StringUtils.isNotBlank(lessonIdNameMap.get(l))){
				lessonNameList.add(lessonIdNameMap.get(l));
			}
			sl.add(slObj);
		}
		schoolOMSManageDao.insertSchoolLessonBatch(sl);
		return lessonNameList;
	}
	private int insertTermInfo(String schoolId) {
		JSONObject termInfo = new JSONObject();
		termInfo.put("schoolId", schoolId);
		termInfo.put("uuid", UUIDUtil.getUUID());
		List<Long> dList = this.getDate();
		Long s1 = dList.get(0);
		Long s2 = dList.get(1);
		termInfo.put("startTime", s1);
		termInfo.put("endTime", s2);
		schoolOMSManageDao.insertTermInfo(termInfo);
		Long s3 = dList.get(2);
		Long s4 = dList.get(3);
		termInfo.put("uuid", UUIDUtil.getUUID());
		termInfo.put("startTime", s3);
		termInfo.put("endTime", s4);
		schoolOMSManageDao.insertTermInfo(termInfo);
		return 1;
	}
	private List<Long> getDate() {
		List<Long> dList = new ArrayList<Long>();
		Calendar calendar = Calendar.getInstance(); 
		Calendar calendar1 = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH)+1; 
		if(month<8){
			calendar.add(Calendar.YEAR, -1);
		}
		calendar.set(Calendar.MONTH, 7);
		calendar.set(Calendar.DAY_OF_MONTH, 10);
		 DateFormat fmtDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		 
		calendar1.setTime(new Date());
		calendar1.set(Calendar.MONTH, 1);
		calendar1.set(Calendar.DAY_OF_MONTH, 10);
		dList.add(calendar.getTimeInMillis());
		dList.add(calendar1.getTimeInMillis());
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(new Date(calendar1.getTimeInMillis()));
		Calendar calendar3 = Calendar.getInstance();
		calendar3.setTime(new Date(calendar1.getTimeInMillis()));
		calendar2.set(Calendar.MONTH, 1);
		calendar2.set(Calendar.DAY_OF_MONTH, 20);
		calendar3.set(Calendar.MONTH, 6);
		calendar3.set(Calendar.DAY_OF_MONTH, 25);
		dList.add(calendar2.getTimeInMillis());
		dList.add(calendar3.getTimeInMillis());
		return dList;
	}
	/*private long getTimeAndAddOneSecond(int interval){
		 Calendar calendar = Calendar.getInstance ();
	     calendar.add (Calendar.SECOND, interval);
	     Date d = calendar.getTime();
	     return d.getTime()/1000;
	}*/
	//插入科室
	private int insertOrg(JSONObject param, String schoolId,List<String> stages,List<String> lessonNameList) {
		List<JSONObject> orgList = new ArrayList<JSONObject>();
		List<String> ids = new ArrayList<String>();
		List<JSONObject> orgLessonList = new ArrayList<JSONObject>();
		List<JSONObject> orgScopeList = new ArrayList<JSONObject>();
		//插入校办、办公室、教务处、总务处
		int interval=0;
		JSONObject obj = new JSONObject();
		String xb = UUIDUtil.getUUID();
		ids.add(xb);
		obj.put("uuid", xb);
		obj.put("orgName", "校办");
		obj.put("orgType", T_OrgType.T_Depart.getValue());
		interval++;
		obj.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
		orgList.add(obj);
		JSONObject obj1 = new JSONObject();
		String bgs = UUIDUtil.getUUID();
		ids.add(bgs);
		obj1.put("uuid",bgs );
		obj1.put("orgName", "办公室");
		obj1.put("orgType", T_OrgType.T_Depart.getValue());
		interval++;
		obj1.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
		orgList.add(obj1);
		JSONObject obj2 = new JSONObject();
		String jwc = UUIDUtil.getUUID();
		ids.add(jwc);
		obj2.put("uuid", jwc);
		obj2.put("orgName", "教务处");
		obj2.put("orgType", T_OrgType.T_Depart.getValue());
		interval++;
		obj2.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
		orgList.add(obj2);
		JSONObject obj3 = new JSONObject();
		String zwc = UUIDUtil.getUUID();
		ids.add(zwc);
		obj3.put("uuid", zwc);
		obj3.put("orgName", "总务处");
		obj3.put("orgType", T_OrgType.T_Depart.getValue());
		obj3.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
		orgList.add(obj3);
		
		//插入年级组
		Map<String,String> gradeNameLevelMap = LessonUtil.gradeGroupStageLevelMap;
		for (Map.Entry<String, List<String>> entry : LessonUtil.gradeGroupStageNameMap.entrySet()) 
		{
			if(stages!=null && !stages.contains(entry.getKey())){
				continue;
			}
			List<String> names = entry.getValue();
			
			for(String name:names){
				JSONObject gradeGroupObj = new JSONObject();
				String njz = UUIDUtil.getUUID();
				ids.add(njz);
				gradeGroupObj.put("uuid", njz);
				gradeGroupObj.put("orgName", name+"年级组");
				gradeGroupObj.put("orgType", T_OrgType.T_Grade.getValue());
				interval++;
				gradeGroupObj.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
				orgList.add(gradeGroupObj);
				String scopeId = gradeNameLevelMap.get(name);
				JSONObject orgScope = new JSONObject();
				orgScope.put("scopeId", scopeId);
				orgScope.put("orgId", njz);
				orgScopeList.add(orgScope);
			}
			
		}
		//插入教研组
		Map<String,String> lessonNameIdMap = LessonUtil.lessonNameIdMap;
		if(lessonNameList!=null){
			for(String researchName:lessonNameList){
				JSONObject researchGroupObj = new JSONObject();
				String jyz = UUIDUtil.getUUID();
				ids.add(jyz);
				researchGroupObj.put("uuid", jyz);
				researchGroupObj.put("orgName", researchName+"教研组");
				researchGroupObj.put("orgType", T_OrgType.T_Teach.getValue());
				interval++;
				researchGroupObj.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
				orgList.add(researchGroupObj);
				String lessonId = lessonNameIdMap.get(researchName);
				JSONObject orgLesson = new JSONObject();
				orgLesson.put("lessonId", lessonId);
				orgLesson.put("orgId", jyz);
				orgLessonList.add(orgLesson);
			}
		}
		//插入备课组
		for (Map.Entry<String, List<String>> entry : LessonUtil.gradeGroupStageNameMap.entrySet()) 
		{//循环年级名称
			if(stages!=null && !stages.contains(entry.getKey())){
				continue;
			}
			List<String> gradeNameList = entry.getValue();
			for(String gradeName:gradeNameList){
				for(String lessonName:lessonNameList){ 
					//循环科目名称
					String preName=gradeName+lessonName+"备课组"; 
					JSONObject preGroupObj = new JSONObject();
					String bkz = UUIDUtil.getUUID();
					ids.add(bkz);
					preGroupObj.put("uuid", bkz);
					preGroupObj.put("orgName",preName );
					preGroupObj.put("orgType", T_OrgType.T_PreLesson.getValue());
					interval++;
					preGroupObj.put("createTime",DateUtil.getTimeAndAddOneSecond(interval));
					orgList.add(preGroupObj);
					String lessonId = lessonNameIdMap.get(lessonName);
					String scopeId = gradeNameLevelMap.get(gradeName);
					JSONObject orgLesson = new JSONObject();
					JSONObject orgScope = new JSONObject();
					orgLesson.put("orgId", bkz);
					orgLesson.put("lessonId", lessonId);
					orgLessonList.add(orgLesson);
					orgScope.put("orgId", bkz);
					orgScope.put("scopeId", scopeId);
					orgScopeList.add(orgScope);
				}
			}
		}
		orgManageDao.insertOrgBatch(orgList);
		//获取所有的uuid对应的orgId
		JSONObject oObj = new JSONObject();
		oObj.put("isInsert", 1);
		List<JSONObject> oList = orgManageDao.getOrgList(oObj);
		//uuid-orgId
		Map<String,String> uuidOrgIdMap = new HashMap<String, String>();
		for(JSONObject o:oList){
			String uuid = o.getString("uuid");
			String orgId = o.getString("orgId");
			if(StringUtils.isNotBlank(uuid) && StringUtils.isNotBlank(orgId)){
				uuidOrgIdMap.put(uuid, orgId);
			}
		}
		List<JSONObject> insertOrgLessonList = new ArrayList<JSONObject>();
		List<JSONObject> insertOrgScopeList = new ArrayList<JSONObject>();
		for(JSONObject orgLesson:orgLessonList){
			String orgId = orgLesson.getString("orgId");
			String trueOrgId = uuidOrgIdMap.get(orgId);
			if(StringUtils.isNotBlank(trueOrgId)){
				orgLesson.put("orgId", trueOrgId);
				insertOrgLessonList.add(orgLesson);
			}
		}
		for(JSONObject orgScope: orgScopeList){
			String orgId = orgScope.getString("orgId");
			String trueOrgId = uuidOrgIdMap.get(orgId);
			if(StringUtils.isNotBlank(trueOrgId)){
				orgScope.put("orgId", trueOrgId);
				insertOrgScopeList.add(orgScope);
			}
		}
		orgManageDao.insertOrgLessonBatch(insertOrgLessonList);
		orgManageDao.insertOrgScopeBatch(insertOrgScopeList);
		//获取这些uuid对应的 orgId
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("ids", ids);
		List<JSONObject> list = schoolOMSManageDao.getOrgListByUUID(json);
		Map<String,String> uuidIdMap = new HashMap<String, String>();
		for(JSONObject org:list){
			String uuid = org.getString("uuid");
			String orgId = org.getString("orgId");
			if(StringUtils.isNotBlank(uuid)&& StringUtils.isNotBlank(orgId)){
				uuidIdMap.put(uuid, orgId);
			}
		}
		for(JSONObject orgObj:orgList){
			String uuid = orgObj.getString("uuid");
			if(StringUtils.isBlank(uuidIdMap.get(uuid))){continue;}
			orgObj.put("orgId", uuidIdMap.get(uuid));
			orgObj.put("schoolId", schoolId);
		}
		orgManageDao.insertSchoolOrgBatch(orgList);
		return 1;
		
	}
	//插入管理员
	private int insertSchoolManager(JSONObject param,JSONObject school) throws Exception {
		//获取超级管理员的账户
		if(school==null){return -100;}
		String schoolName = school.getString("schoolName");
		String schoolId = school.getString("schoolId");
		if(StringUtils.isBlank(schoolName) || StringUtils.isBlank(schoolId)){
			return -100;
		}
		//获取首字母并转大写
		String headName = ChinaInitial.getPYIndexStr(schoolName, true);
		String accountName = headName+StringUtil.createRandom(4);
		logger.info("[csjcsj]中文schoolName: "+schoolName+"  accountName"+accountName);
		//判断账户有无使用过
		JSONObject json = new JSONObject();
		json.put("accountName", accountName);
		List<JSONObject> getAccountObjList = schoolOMSManageDao.getAccountObj(json);
		if(getAccountObjList!=null && getAccountObjList.size()>0){
			return -100;
		}
		//判断姓名有无重复
		json.clear();
		json.put("name", schoolName);
		json.put("schoolId", schoolId);
		getAccountObjList = schoolOMSManageDao.getSchoolManagerAccountObj(json);
		if(getAccountObjList!=null && getAccountObjList.size()>0){
			return -100;
		}
		//创建账户
		JSONObject accountObj = new JSONObject();
		String accountUUID = UUIDUtil.getUUID();
		accountObj.put("uuid", accountUUID);
		accountObj.put("accountName", accountName);
		accountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
		accountObj.put("accountStatus", 0);
		accountObj.put("name", schoolName);
		accountObj.put("gender", 1);
		accountObj.put("mobilePhone", "");
		//accountObj.put("createTime", new Date().getTime());
		schoolOMSManageDao.insertAccount(accountObj);
		long accountId = accountObj.getLongValue("id");
		if(accountId==0){
			return -100;
		}
		String pwd = "";
		//截后六位
		String pwdQZ=accountName.substring(accountName.length()-6,accountName.length());
		//加密
		pwd = MD5Util.getMD5String(MD5Util.getMD5String(pwdQZ)+accountId);
		JSONObject pwdObj = new JSONObject();
		pwdObj.put("accountId", accountId);
		pwdObj.put("pwd", pwd);
		pwdObj.put("schoolId", schoolId);
		schoolOMSManageDao.updateAccountPwd(pwdObj);
		//创建角色
		JSONObject userObj = new JSONObject();
		userObj.put("uuid", UUIDUtil.getUUID());
		userObj.put("role", T_Role.SchoolManager.getValue());
		userObj.put("schoolId", schoolId);
		userObj.put("accountId", accountId);
		userObj.put("termInfoId", param.getString("termInfoId"));
		adminManageDao.insertUser(userObj);
		List<JSONObject> userList = schoolOMSManageDao.getUserByAccountId(userObj);
		if(userList==null || userList.size()<1||userList.get(0)==null || StringUtils.isBlank(userList.get(0).getString("userId"))){
			return -100;
		}
		JSONObject user = userList.get(0);
		String userId = user.getString("userId");
		//创建管理员
		JSONObject smObj = new JSONObject();
		smObj.put("schoolId", schoolId);
		smObj.put("userId", userId);
		smObj.put("hasAllRight", 1); //超级管理员
		smObj.put("termInfoId", param.getString("termInfoId"));
		adminManageDao.insertSchoolManager(smObj);
		//创建超级管理员权限
		Map<String,String> pMap = new HashMap<String, String>();
		 //判断是否为新高考学校
		/*JSONObject newSchool = new JSONObject();
		newSchool.put("schoolId", schoolId);
		newSchool.put("termInfoId", param.getString("termInfoId"));
		JSONObject  newEntranceSchool = adminManageDao.getNewEntranceSchool(newSchool);
		if(newEntranceSchool!=null){
			 pMap = PermissionUtil.permissionTypeMap.get("1");
		}else{
			 pMap = PermissionUtil.permissionTypeMap.get("2");
		}*/
		 pMap = PermissionUtil.permissionTypeMap.get(PermissionUtil.NAVTYPE);
		List<JSONObject> list = new ArrayList<JSONObject>();
		for (Map.Entry<String, String> entry : pMap.entrySet()) 
		{
			String navId = entry.getKey();
			String permissiontype = entry.getValue();
			if(pMap.get(navId)==null){
				continue;
			}
			JSONObject pObj = new JSONObject();
			pObj.put("userId", userId);
			pObj.put("navId", navId);
			pObj.put("permissiontype",permissiontype);
			list.add(pObj);
		}
		if(list!=null && list.size()>0){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("termInfoId", param.getString("termInfoId"));
			jsonObj.put("list", list);
			adminManageDao.insertPermissionspcBatch(jsonObj);
		}
		//adminManageDao.insertPermissiontypeBatch(list);
		return 1;
	}
		
	
}
