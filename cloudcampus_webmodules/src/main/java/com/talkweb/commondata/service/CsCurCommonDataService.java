package com.talkweb.commondata.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.CurrentTermRsp;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.ParentPart;
import com.talkweb.accountcenter.thrift.PermissionTemplateIns;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.SchoolManagerPart;
import com.talkweb.accountcenter.thrift.StaffPart;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_AccountStatus;
import com.talkweb.accountcenter.thrift.T_ClassType;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_LessonType;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_PermissionLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.T_SchoolType;
import com.talkweb.accountcenter.thrift.T_StageType;
import com.talkweb.accountcenter.thrift.TeacherLesson;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.TermInfo;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPart;
import com.talkweb.accountcenter.thrift.UserPermissions;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.dao.CsCurCommonDataDao;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.util.TeachingRelationShipUtil;
import com.talkweb.commondata.util.Util;
import com.talkweb.filter.util.HttpClientToken;
import com.talkweb.scoreManage.action.ScoreUtil;

/**
 * @ClassName CsCurCommonDataService
 * @author zhanghuihui
 * @version 1.0
 * @Description 当前长沙学年学期基础数据接口
 * @date 2017年2月15日
 */
@Service
@SuppressWarnings(value={"unchecked", "rawtypes", "deprecation"})
public class CsCurCommonDataService {

	//@Autowired
	//private OrgService orgService;
	@Autowired
	private CsCurCommonDataDao csCurCommonDataDao;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;

	private static final Logger logger = LoggerFactory
			.getLogger(CsCurCommonDataService.class);
	public static ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	final String LEADERTYPE="1"; //领导
	final String MEMBERTYPE="2"; //成员
	/**
	 * 学校权限等级名称
	 */
	public static final Map<T_PermissionLevel, String> SchoolPermissionLevelName = AccountStructConstants.SchoolPermissionLevelName;
	/**
	 * 平台权限等级名称
	 */
	public static final Map<T_PermissionLevel, String> PlatePermissionLevelName = AccountStructConstants.PlatePermissionLevelName;
	/**
	 * 学校类型
	 */
	public static final Map<T_SchoolType, String> SchoolTypeNames = AccountStructConstants.SchoolTypeNames;
	/**
	 * 年级名称
	 */
	public static final Map<T_GradeLevel, String> T_GradeLevelName = AccountStructConstants.T_GradeLevelName;
	/**
	 * 机构类型
	 */
	public static final Map<T_OrgType, String> DefaultOrgTypeNames = AccountStructConstants.DefaultOrgTypeNames;
	/**
	 * 科目类型
	 */
	public static final Map<T_LessonType, String> DefaultLessonTypeNames = AccountStructConstants.DefaultLessonTypeNames;
	/**
	 * 判断某个年级某个历史时刻是否毕业
	 * @param createLevel
	 * @param currentLevel
	 * @return 布尔 
	 * @author zhh
	 */
	private boolean  isGraduate(int createLevel,int currentLevel){
		T_StageType st1=getStageType(currentLevel);
		T_StageType st2=getStageType(createLevel);
		return !(st1==st2);
	}
	/**
	 * 根据年级level获取对应的教学阶段
	 * @param gradeLevel
	 * @return T_StageType 枚举
	 * @author zhh
	 */
	private T_StageType  getStageType(int gradeLevel){
		if(gradeLevel<T_GradeLevel.T_PrimaryOne.getValue()){
			return T_StageType.Kindergarten;
		}else if(gradeLevel<=T_GradeLevel.T_PrimarySix.getValue() && gradeLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
			return T_StageType.Primary;
		}else if(gradeLevel<=T_GradeLevel.T_JuniorThree.getValue() && gradeLevel>=T_GradeLevel.T_JuniorOne.getValue()){
			return T_StageType.Junior;
		}else if(gradeLevel<=T_GradeLevel.T_HighThree.getValue()&& gradeLevel>=T_GradeLevel.T_HighOne.getValue()){
			return T_StageType.High;
		}
		return T_StageType.Invalid;
	}
	/**
	 * 获取当前学年学期
	 * 
	 * @param schoolId
	 *            学校id
	 * @return CurrentTermRsp
	 * @author zhanghuihui
	 */
	// @Cacheable
	public CurrentTermRsp getCurTermInfo(JSONObject param) {
		CurrentTermRsp ctr = new CurrentTermRsp();
		String schoolId = param.getString("schoolId");
		String termInfoId = param.getString("termInfoId");
		try {
			List<JSONObject> termInfos = csCurCommonDataDao.getTermInfos(param);
			if(termInfos==null || termInfos.size()!=2 ){
				return null;
			}
			
			String startTime1 = termInfos.get(0).getString("startTime");
			String endTime1 = termInfos.get(0).getString("endTime");
			String startTime2 = termInfos.get(1).getString("startTime");
			String endTime2 = termInfos.get(1).getString("endTime");
			if(StringUtils.isBlank(startTime1)|| StringUtils.isBlank(startTime2) ||StringUtils.isBlank(endTime1) || StringUtils.isBlank(endTime2)){
				return null;
			}
			String year = termInfos.get(0).getString("startTime").substring(0, 4);
			Date nowDate = new Date();  //当前时间
			DateFormat fmtDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date sDate1 = fmtDateTime.parse(startTime1);
			Date sDate2 = fmtDateTime.parse(startTime2);
			Date eDate1 = fmtDateTime.parse(endTime1);
			Date eDate2 = fmtDateTime.parse(endTime2);
			ctr.setSchoolYear(year);
			//如果在时间1段之内
			if(sDate1.before(nowDate) && eDate1.after(nowDate) ){
				ctr.setTermIdx(0);
				ctr.setTermId(termInfos.get(0).getLongValue("id"));
			}
			//如果在时间2段之内
			if(sDate2.before(nowDate) && eDate2.after(nowDate)){
				ctr.setTermIdx(1);
				ctr.setTermId(termInfos.get(1).getLongValue("id"));
			}
			//如果在两段时间中间,则按第二个学期来算
			if(eDate1.before(nowDate) && sDate2.after(nowDate)){
				ctr.setTermIdx(1);
				ctr.setTermId(termInfos.get(1).getLongValue("id"));
			}
			//如果时间1段之前
			if(sDate1.after(nowDate)){
				ctr.setTermIdx(0);
				ctr.setTermId(termInfos.get(0).getLongValue("id"));
			}
			//如果时间2段之后
			if(eDate2.before(nowDate)){
				ctr.setTermIdx(1);
				ctr.setTermId(termInfos.get(1).getLongValue("id"));
			}
			
		} catch (ParseException e) {
			logger.error("CsCurCommonDataService.getCurTermInfo报错，入参schoolId:{}, termInfoId:{}",schoolId,termInfoId);
			e.printStackTrace();
		}
		return ctr;
	}


	/**
	 * 获取格式为 20151 格式的学年学期
	 * @param School	学校实例
	 * @return
	 * @author zhanghuihui
	 */
	public String getCurrentXnxq(JSONObject param){
		String schoolId = param.getString("schoolId");
		String termInfoId = param.getString("termInfoId");
		School sch = (School) param.get("school");
		if(StringUtils.isBlank(termInfoId) || StringUtils.isBlank(schoolId)||sch==null||sch.getTermIds()==null||sch.getId()==0){
			return "";
		}
		String xnxq = "";
		JSONObject param1 = new JSONObject();
		param1.put("schoolId", sch.getId());
		param1.put("termInfoId", termInfoId);
		long curTermId = 0L;
		if(getCurTermInfo(param1)!=null){
			 curTermId = getCurTermInfo(param1).getTermId();
		}else{
			return "";
		}
		List<TermInfo> terms = this.getTermInfoBatch(sch.getTermIds(), sch.getId(),termInfoId);
		
		List<JSONObject> sorList = new ArrayList<JSONObject>();
		int minxn = 10000;
		if(terms!=null){
			for(TermInfo ter:terms){
				JSONObject obj = (JSONObject) JSON.toJSON(ter);
				sorList.add(obj);
				Timestamp d  = new Timestamp(ter.getStartTime()*1000);
				int now = d.getYear()+1900;
				if(now<minxn){
					minxn = now;
				}
			}
		}
		
		ScoreUtil.sorStuScoreList(sorList, "endTime", "asc", "", "");
		int xq = -1;
		for(JSONObject obj:sorList){
			long tid = obj.getLongValue("id");
			if(tid==curTermId){
				xq = obj.getIntValue("pm");
			}
		}
		if(minxn!=10000&&xq!=-1){
			xnxq = minxn + "" + xq;
		}
		return xnxq;
		//return "20181";//注释写死学年学期
		
	}
	/**
	 * 获取学期列表
	 * 
	 * @param termId
	 *            学年学期ID
	 * @return TermInfo
	 * @author zhanghuihui
	 */
	// @Cacheable
	public List<TermInfo> getTermInfoBatch(List<Long> termIds, long schoolId,String termInfoId) {
		if(termIds==null ||termIds.size()<0 || schoolId==0||StringUtils.isBlank(termInfoId)){
			return null;
		}
		JSONObject param = new JSONObject();
		param.put("ids", termIds);
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		List<JSONObject> list = csCurCommonDataDao.getTermInfoBatch(param);
		List<TermInfo>  rList = new ArrayList<TermInfo>();
		if(list!=null){
			for(JSONObject obj:list){
				TermInfo termInfo = new TermInfo();
				termInfo.setId(obj.getLongValue("id"));
				termInfo.setStartTime(obj.getLongValue("startTime"));
				termInfo.setEndTime(obj.getLongValue("endTime"));
				termInfo.setSchoolId(obj.getLongValue("schoolId"));
				rList.add(termInfo);
			}
		}
		return rList;
	}

	/**
	 * 通过用户ID得到学校对象
	 * 
	 * @param schoolId
	 *            学校ID 此参数设置为0
	 * @param userId
	 *            教师/学生/用户ID
	 * @return School
	 * @author zhanghuihui
	 */
	public School getSchoolByUserId(long schoolId, long userId,String termInfoId) {
		if(userId==0 || StringUtils.isBlank(termInfoId)){
			return null;
		}
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("termInfoId", termInfoId);
 		JSONObject userObj = csCurCommonDataDao.getUserById(param);
 		if(userObj==null||userObj.getInteger("role")==null){
 			return null;
 		}
 		T_Role role = T_Role.findByValue(userObj.getIntValue("role"));
		Long sId=schoolId; 
		JSONObject json = new JSONObject();
 		switch (role) {
			case Parent:
				json = csCurCommonDataDao.getParentById(userId,termInfoId);
				if(json!=null){
					sId = json.getLong("schoolId");
				}
				break;
			case Student:
				json = csCurCommonDataDao.getStudentById(userId,termInfoId);
				if(json!=null){
					sId = json.getLong("schoolId");
				}
				break;
			case Teacher:
				if(json!=null){
					json = csCurCommonDataDao.getTeacherById(userId,termInfoId);
					sId = json.getLong("schoolId");
				}
				break;
			case Staff:
				if(json!=null){
					json = csCurCommonDataDao.getStaffById(userId, termInfoId);
					sId = json.getLong("schoolId");
				}
				break;
			case SchoolManager:
				if(json!=null){
					json = csCurCommonDataDao.getSchoolManagerById(userId, termInfoId);
					sId = json.getLong("schoolId");
				}
				break;
			default:
				break;
		}
 		if(sId!=null && sId!=0){
 			param.remove("userId");
 			param.put("schoolId", sId);
 			School s=this.getSchoolById(param);
 			return s;
 		}else{
 			return null;
 		}
	}
    /**
     * 获取用户菜单权限(管理员角色才返回值)
     * @param schoolId
     * @param userId
     * @param termInfoId
     * @param accessToken 当走教育云登录且调用教育云接口，则获取accessToken存入head才能通过http调用远程接口
     * @return
     * @author zhanghuihui
     */
	public UserPermissions getUserPermissionById(long schoolId,
			long userId,
			String termInfoId,
			String from) { //
		if(schoolId==0|| userId==0|| StringUtils.isBlank(termInfoId)){
			return null;
		}
		//判断当前的用户的staff表中的权限信息 若为超级管理员则直接获取所有权限
		boolean isSuperManager = false;
		if("1".equals(from)){
			String isSuperManangerKey = String.format("common.%s.%s.00.isSuperManager",schoolId,userId);
			Object isSuperManagerObj;
			try {
				isSuperManagerObj = redisOperationDAO.get(isSuperManangerKey);
				if(isSuperManagerObj!=null){
					isSuperManager = (boolean)isSuperManagerObj;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			JSONObject smObj = csCurCommonDataDao.getSchoolManagerById(userId, termInfoId);
			if(smObj!=null){
				String hasAllRight = smObj.getString("hasAllRight");
				if("1".equals(hasAllRight)){
					isSuperManager=true;
				}
			}
		}
		Log.info("[jycloud]:isSuperManager"+isSuperManager);
		//isSuperManager=true;
		List<PermissionTemplateIns> ptList = new ArrayList<PermissionTemplateIns>();
		UserPermissions up = new UserPermissions();
		List<JSONObject> pList = new ArrayList<JSONObject>();
		//if(!isSuperManager){ 
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("userId", userId);
			param.put("termInfoId", termInfoId);
			pList = csCurCommonDataDao.getUserPermissionById(param);
		//}
		/*else{ //超级管理员返回所有的权限
			JSONObject param = new JSONObject();
			param.put("termInfoId", termInfoId);
			pList = csCurCommonDataDao.getDicPermissions(param);
			Log.info("[jycloud]pList:"+pList.toString());
		}*/
		if(pList!=null){
			for(JSONObject pObj:pList){
				PermissionTemplateIns pt = new PermissionTemplateIns();
				pt.setLevelv2(pObj.getLong("levelv2"));
				pt.setUserId(userId);
				ptList.add(pt);
			}
		}
		up.setPermissionTemplateInss(ptList);
		return up;
	}
	/**
	 * 根据accountName获取Account
	 * @param schoolId
	 * @param accountNum
	 * @param termInfoId
	 * @return
	 * @author zhanghuihui
	 */
	public Account getAccountAllByAccount(long schoolId, String accountName, String termInfoId) {
		JSONObject params = new JSONObject();
		params.put("schoolId",schoolId );
		params.put("accountName",accountName );
		params.put("termInfoId",termInfoId );
		JSONObject json = csCurCommonDataDao.getBaseAccountUserIdsByAccount(params);
		Account account = new Account();
		if(json==null){
			return account;
		}
		account.setIdNumber(json.getString("idNumber"));
		account.setId(json.getLongValue("id"));
		account.setAccount(json.getString("accountName"));
		T_AccountStatus status = T_AccountStatus.findByValue(json.getIntValue("accountStatus"));
		account.setAccountStatus(status);
		account.setName(json.getString("name"));
		account.setMobilePhone(json.getString("mobilePhone"));
		List<JSONObject> userRole = csCurCommonDataDao.getUserIdsAndRole(json.getLongValue("id"),termInfoId);
		List<Long> userIds = new ArrayList<Long>();
		List<User> l_user = new ArrayList<User>();
		if(userRole!=null){
			for(JSONObject object:userRole){
				userIds.add(object.getLongValue("id"));
				User user = new User();
				UserPart up = new UserPart();
				JSONObject obj;
				int role = object.getIntValue("role");
				T_Role tRole = T_Role.findByValue(role);
				switch (tRole) {
				case Parent://家长
					up.setRole(T_Role.Parent);
					up.setId(object.getLongValue("id"));
					break;
				case Teacher://教师 
					up.setRole(T_Role.Teacher);
					up.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getTeacherRoleByAccount(object.getLong("id"),termInfoId);
					if(obj!=null&&obj.getString("orgIds")!=null){
						List<Long> orgIds = StringUtil.toListFromString(obj.getString("orgIds"));
						up.setOrgIds(orgIds);
					}
					TeacherPart tp = new TeacherPart();
					tp.setId(object.getLongValue("id"));
					tp.setSchoolId(obj.getLong("schoolId"));
					List<Course> lc = new ArrayList<Course>();
					List<Long> courseIds = StringUtil.toListFromString(obj.getString("lessonIds"));
					List<Long> classIds = StringUtil.toListFromString(obj.getString("classIds"));
					for(int i=0;i<courseIds.size();i++){
						Course course = new Course();
						course.setLessonId(courseIds.get(i));
						course.setClassId(classIds.get(i));
						lc.add(course);
					}
					tp.setCourseIds(lc);
					user.setUserPart(up);
					user.setTeacherPart(tp);
					l_user.add(user);
					break;
				case Student:
					up.setRole(T_Role.Student);
					up.setId(object.getLongValue("id"));
					break;
				case Staff:
					up.setRole(T_Role.Staff);
					up.setId(object.getLongValue("id"));
					StaffPart sp = new StaffPart();
					sp.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getStaffRole(object.getLong("id"),termInfoId);
					if(obj!=null&&obj.getString("orgIds")!=null){
						List<Long> orgIds1 = StringUtil.toListFromString(obj.getString("orgIds"));
						up.setOrgIds(orgIds1);
					}
					sp.setJobType(obj.getLongValue("jobType"));
					sp.setJobName(obj.getString("jobTypeName"));
					sp.setSchoolId(obj.getLongValue("schoolId"));
					user.setUserPart(up);
					user.setStaffPart(sp);
					l_user.add(user);
					break;
				case SchoolManager:
					up.setRole(T_Role.SchoolManager);
					up.setId(object.getLongValue("id"));
					SchoolManagerPart smp = new SchoolManagerPart();
					smp.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getSchoolManagerPartRole(object.getLong("id"),termInfoId);
					if(obj!=null){
						smp.setId(obj.getLongValue("managerId"));
						smp.setSchoolId(obj.getLongValue("schoolId"));
					}
					user.setUserPart(up);
					user.setSchoolManagerPart(smp);
					l_user.add(user);
					break;
				case PlateManager:
					break;
				case SystemManager:
					break;
				default:
					break;
				}
			}
		}
		account.setUserIds(userIds);
		account.setUsers(l_user);
		return account;
	}
	/**
	 * 获取所有的学校列表(暂时用于OMS系统)
	 * @return List<School>  school包含schoolId和schoolName
	 * @author zhanghuihui
	 * @PS： 当前学年学期的基础数据，这个接口暂时直接查询数据库
	 */
	public List<School> getAllSchoolList(String termInfoId){
		List<School> schoolList = new ArrayList<School>();
		List<JSONObject> objList = csCurCommonDataDao.getAllSchoolList(termInfoId);
		if(objList!=null){
			for(JSONObject obj:objList){
				School s = new School(); 
				s.setId(obj.getLong("id"));
				s.setName(obj.getString("name"));
				schoolList.add(s);
			}
		}
		return schoolList;
	}
	  /**
     * 通过学校名称（模糊查询）获取学校列表(暂时用于OMS系统)
     * @param schoolName
     * @return List<School>  school包含schoolId和schoolName
     * @author zhanghuihui
     * @PS： 当前学年学期的基础数据，这个接口暂时直接查询数据库
     */
	public List<School>  getSchoolInfoByName(String schoolName , String termInfoId){
		List<School> schoolList = new ArrayList<School>();
		List<JSONObject> objList = csCurCommonDataDao.getSchoolInfoByName(schoolName, termInfoId);
		if(objList!=null){
			for(JSONObject obj:objList){
				School s = new School(); 
				s.setId(obj.getLong("id"));
				s.setName(obj.getString("name"));
				schoolList.add(s);
			}
		}
		return schoolList;
	}

	/**
	 * 更新教师任教关系
	 * @param schoolId  学校
	 * @param teacherId 老师
	 * @param courseIds 任教关系列表
	 * @author zhanghuihui
	 * @param accessToken 
	 * @param from 
	 */
	public void updateTeacherLesson(long schoolId, long teacherId, List<Course> courseIds,String termInfoId, String from) {
		if(StringUtils.isBlank(termInfoId) 
			|| schoolId==0|| teacherId==0 || courseIds==null || courseIds.size()<1){
			return ;
		}
		
		//先删除
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("teacherId", teacherId);
		param.put("termInfoId", termInfoId);
		csCurCommonDataDao.deleteTeacherLesson(param);
				
		//新增
		List<JSONObject> insertParamList = new ArrayList<JSONObject>();
		int count=0;
		List<String> cIds = new ArrayList<String>();
		List<Long> lIds = new ArrayList<Long>();
		for(Course c:courseIds){
			JSONObject insertParam = new JSONObject();
			long classId = c.getClassId();
			long lessonId = c.getLessonId();
			if(lessonId==0|| classId==0){
				continue;
			}
			if(!cIds.contains(classId+"")){
				cIds.add(classId+"");
			}
			lIds.add(lessonId);
			insertParam.put("lessonId", lessonId);
			insertParam.put("classId", classId);
			insertParam.put("teacherId", teacherId);
			
			insertParamList.add(insertParam);
			count++;
			if(count==500){
				JSONObject p = new JSONObject();
				p.put("termInfoId", termInfoId);
				p.put("insertParamList", insertParamList);
				csCurCommonDataDao.insertTeacherLessonBatch(p);
				count=0;
				insertParamList.clear();
			}
			
		}//end for courseIds
		if(insertParamList.size()<500 && insertParamList.size()>0){
			JSONObject p = new JSONObject();
			p.put("termInfoId", termInfoId);
			p.put("insertParamList", insertParamList);
			csCurCommonDataDao.insertTeacherLessonBatch(p);
		}
		  Map<String,String> llMap = new HashMap<String,String>();
			 List<JSONObject> lList =this.getExtLessonIdByIds(termInfoId,lIds);
			 for(JSONObject l :lList){
				   llMap.put( l.getString("lessonId"),  l.getString("extId"));
			 } 
		//处理同步任教关系
		if("1".equals(from)){
			String updateTeacherLessonsUrl = rb.getString("updateTeacherLessonsUrl");
			//处理调用云平台任教同步接口的数据
			//获取extClassId
			List<JSONObject> extClassIds = new ArrayList<JSONObject>();
			Map<Long,String> classMap = new HashMap<Long,String>();
			if(cIds.size()>0){
				JSONObject classParam = new JSONObject();
				classParam.put("ids", cIds);
				classParam.put("termInfoId", termInfoId);
				extClassIds = csCurCommonDataDao.getExtClassIdByclassId(classParam);
			}
			if(extClassIds!=null ){
				for(JSONObject classObj :extClassIds){
					String extClassId = classObj.getString("extId");
					long classId = classObj.getLongValue("classId");
					classMap.put(classId, extClassId);
				}
			}
			//拼接参数
			Map<String,ArrayList<String>> classLessonMap = new HashMap<String,ArrayList<String>>();
			for(Course c:courseIds){
				String classId = classMap.get(c.getClassId());
				long lessonId = c.getLessonId();
				if(lessonId==0|| StringUtils.isBlank(classId) || !TeachingRelationShipUtil.teachingRelationshipMap.containsKey(lessonId)){
					continue;
				}
				ArrayList<String> lessons = new ArrayList<String>();
				if(classLessonMap.containsKey(classId)){
					lessons = classLessonMap.get(classId);
				}
				//String extLessonId = TeachingRelationShipUtil.teachingRelationshipMap.get(lessonId);
				String extLessonId = llMap.get(lessonId+"");
				if(!lessons.contains(extLessonId)){
					lessons.add(extLessonId);
					classLessonMap.put(classId, lessons);
				}
				
			} //end  for courseIds
			//根据teacherId查询extAccountId
			JSONObject relationshipParam = new JSONObject();
			List<String> ids = new ArrayList<String>();
			ids.add(teacherId+"");
			relationshipParam.put("ids", ids);
			relationshipParam.put("termInfoId", termInfoId);
			List<JSONObject> extAccountIdlist = csCurCommonDataDao.getExtAccountIdByUserIds(relationshipParam);
			if(extAccountIdlist!=null && extAccountIdlist.size()>0){
				String extAccountId = extAccountIdlist.get(0).getString("extAccountId");
				JSONArray jsonArr = new JSONArray();
				JSONObject json = new JSONObject();
				JSONArray data = new JSONArray();
				for (Map.Entry<String,ArrayList<String>> entry : classLessonMap.entrySet()) {
					   String key = entry.getKey().toString();
					   List<String> value = entry.getValue();
					   JSONObject classlesson = new JSONObject();
					   classlesson.put("classId", key);
					   classlesson.put("courseCodes", value);
					   data.add(classlesson);
				}
				json.put("data", data);
				json.put("accountId", extAccountId);
				jsonArr.add(json);
				logger.debug("[update] updateTeacherLesson param: updateTeacherLessonsUrl"+updateTeacherLessonsUrl+" jsonArr:"+jsonArr.toJSONString());
				JSONObject returnHttpObj = null;
				try {
					returnHttpObj = HttpClientToken.sendPostNoToken(updateTeacherLessonsUrl ,jsonArr);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(returnHttpObj!=null){
					logger.debug("[update] updateTeacherLesson: returnHttpObj"+returnHttpObj.toJSONString());
				}else{
					logger.debug("[update] updateTeacherLesson: returnHttpObj is null");
				}
			}
		}
	}

   /**
    * 批量更新任课教师
    * @param schoolId 学校代码
    * @param teacherCourseMap  老师-任课关系列表
    * @param termInfoId 学年学期
    * @author zhanghuihui
 * @param accessToken 
 * @param from 
    */
	public void updateTeacherLessonBatch(long schoolId, Map<Long, List<Course>> teacherCourseMap, String termInfoId, String from) {
		if(StringUtils.isBlank(termInfoId) 
				|| schoolId==0|| teacherCourseMap.size()<1 || teacherCourseMap==null  ){
				return ;
		}
		//先删除
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		List<Long> teacherIds = new ArrayList<Long>();
		for (Map.Entry<Long, List<Course>> entry : teacherCourseMap.entrySet()) 
		{
			if(!teacherIds.contains(entry.getKey())){
				teacherIds.add(entry.getKey());
			}
		}
		param.put("teacherIds", teacherIds);
		param.put("termInfoId", termInfoId);
		if(teacherIds.size()>0){
			csCurCommonDataDao.deleteTeacherLesson(param);
		}
		
		//新增
		List<JSONObject> insertParamList = new ArrayList<JSONObject>();
		int count=0;
		List<Long> lIds  = new ArrayList<Long>();
 		List<String> cIds = new ArrayList<String>();
		for (Map.Entry<Long, List<Course>> entry : teacherCourseMap.entrySet()) 
		{
			List<Course> courseIds = entry.getValue();
			for(Course c:courseIds){
				JSONObject insertParam = new JSONObject();
				if(c.getLessonId()==0|| c.getClassId()==0){
					continue;
				}
				if(!cIds.contains(c.getClassId()+"")){
					cIds.add(c.getClassId()+"");
				}
				lIds.add(c.getLessonId());
				insertParam.put("lessonId", c.getLessonId());
				insertParam.put("classId", c.getClassId());
				insertParam.put("teacherId", entry.getKey());
				insertParam.put("termInfoId", termInfoId);
				insertParamList.add(insertParam);
				count++;
				if(count==500){
					JSONObject p = new JSONObject();
					p.put("termInfoId", termInfoId);
					p.put("insertParamList", insertParamList);
					csCurCommonDataDao.insertTeacherLessonBatch(p);
					count=0;
					insertParamList.clear();
				}
			}
		}
		if(insertParamList.size()<500 && insertParamList.size()>0){
			JSONObject p = new JSONObject();
			p.put("termInfoId", termInfoId);
			p.put("insertParamList", insertParamList);
			csCurCommonDataDao.insertTeacherLessonBatch(p);
		}
		//处理同步任教关系
		if("1".equals(from)){
			String updateTeacherLessonsUrl = rb.getString("updateTeacherLessonsUrl");
			//获取extClassId
			List<JSONObject> extClassIds = new ArrayList<JSONObject>();
			Map<Long,String> classMap = new HashMap<Long,String>();
			if(cIds.size()>0){
				JSONObject classParam = new JSONObject();
				classParam.put("ids", cIds);
				classParam.put("termInfoId", termInfoId);
				extClassIds = csCurCommonDataDao.getExtClassIdByclassId(classParam);
			}
			if(extClassIds!=null ){
				for(JSONObject classObj :extClassIds){
					String extClassId = classObj.getString("extId");
					long classId = classObj.getLongValue("classId");
					classMap.put(classId, extClassId);
				}
			}
			  Map<String,String> llMap = new HashMap<String,String>();
			 List<JSONObject> lList =this.getExtLessonIdByIds(termInfoId,lIds);
			 for(JSONObject l :lList){
				   llMap.put( l.getString("lessonId"),  l.getString("extId"));
			 } 
			//处理调用云平台任教同步接口的数据
			Map<Long,HashMap<String,ArrayList<String>>> teacherClassLessonMap = new HashMap<Long,HashMap<String,ArrayList<String>>>();
			for (Map.Entry<Long, List<Course>> entry : teacherCourseMap.entrySet()) 
			{
				List<Course> courseIds = entry.getValue();
				long teacherId = entry.getKey();
				HashMap<String,ArrayList<String>> classLessonMap = new HashMap<String,ArrayList<String>>();
				if(teacherClassLessonMap.containsKey(teacherId)){
					classLessonMap = teacherClassLessonMap.get(teacherId);
				}
				for(Course c:courseIds){
					String classId = classMap.get(c.getClassId());
					long lessonId = c.getLessonId();
					if(lessonId==0|| StringUtils.isBlank(classId) || !TeachingRelationShipUtil.teachingRelationshipMap.containsKey(lessonId)){
						continue;
					}
					ArrayList<String> lessons = new ArrayList<String>();
					if(classLessonMap.containsKey(classId)){
						lessons = classLessonMap.get(classId);
					}
					String extLessonId = llMap.get(lessonId+"");
					//String extLessonId = TeachingRelationShipUtil.teachingRelationshipMap.get(lessonId);
					if(!lessons.contains(extLessonId)){
						lessons.add(extLessonId);
						classLessonMap.put(classId, lessons);
					}
					
				} //end  for courseIds
				teacherClassLessonMap.put(teacherId, classLessonMap);
			}
			//根据teacherId查询extAccountId
			List<JSONObject> extAccountIdlist = new ArrayList<JSONObject>();
			if(teacherIds.size()>0){
				JSONObject relationshipParam = new JSONObject();
				relationshipParam.put("ids", teacherIds);
				relationshipParam.put("termInfoId", termInfoId);
				extAccountIdlist = csCurCommonDataDao.getExtAccountIdByUserIds(relationshipParam);
			}
			if(extAccountIdlist!=null && extAccountIdlist.size()>0){
				JSONArray jsonArr = new JSONArray();
				for(JSONObject extAccountIdObj : extAccountIdlist){
					JSONObject json = new JSONObject();
					String extAccountId = extAccountIdObj.getString("extAccountId");
					long teacherId = extAccountIdObj.getLongValue("userId");
					JSONArray data = new JSONArray();
					HashMap<String,ArrayList<String>> classLessonMap=teacherClassLessonMap.get(teacherId);
					if(classLessonMap!=null){
						for (Map.Entry<String,ArrayList<String>> entry : classLessonMap.entrySet()) {
							   String key = entry.getKey().toString();
							   List<String> value = entry.getValue();
							   JSONObject classlesson = new JSONObject();
							   classlesson.put("classId", key);
							   classlesson.put("courseCodes", value);
							   data.add(classlesson);
						}
					}
					json.put("data", data);
					json.put("accountId", extAccountId);
					jsonArr.add(json);
				}
				logger.debug("[update] updateTeacherLessonBatch param: updateTeacherLessonsUrl"+updateTeacherLessonsUrl+" jsonArr:"+jsonArr.toJSONString());
				JSONObject returnHttpObj=null;
				try {
					returnHttpObj = HttpClientToken.sendPostNoToken(updateTeacherLessonsUrl ,jsonArr);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(returnHttpObj!=null){
					logger.debug("[update] updateTeacherLessonBatch: returnHttpObj"+returnHttpObj.toJSONString());
				}else{
					logger.debug("[update] updateTeacherLessonBatch: returnHttpObj is null");
				}
			}
		}
	}

   
	/**
     * 更新课程 （支持更新班级基本信息和任教关系信息）
     * @param schoolId 学校代码
     * @param c 课程
     * @param termInfoId 学年学期
     * @author zhanghuihui
     * @param accessToken 
     * @param from 
     * @throws Exception 
     */
	public void updateClassroom(long schoolId, Classroom c, String termInfoId, String from) throws Exception {
		if(schoolId==0||  c==null || c.getId()==0 || StringUtils.isBlank(termInfoId)){
			return ;
		}
		logger.debug("updateClassroomClassroom:"+c.toString());
		//（1）更新基本班级信息
		JSONObject basicClassObj = new JSONObject();
		basicClassObj.put("termInfoId", termInfoId);
		basicClassObj.put("id", c.getId());
		/*if(c.getGradeId()!=0){
			basicClassObj.put("gradeId", c.getGradeId());
		}*/
		if( c.getClassNo()!=0){
			basicClassObj.put("classNo", c.getClassNo());
		}
		if(StringUtils.isNotBlank(c.getClassName()))
		{
			basicClassObj.put("className", c.getClassName());
		}
		if(c.getTeacherId() !=0){
			basicClassObj.put("headTeacherId",c.getTeacherId());
		}
	    if(StringUtils.isNotBlank(c.getUSER_BH())){
	    	basicClassObj.put("USER_BH", c.getUSER_BH());
	    }
	    if(null != c.getClassType()  ){
	    	basicClassObj.put("classType", c.getClassType().getValue()+"");
	    }
	   /* csCurCommonDataDao.updateClassroom(basicClassObj);
		logger.info("updateClassroom basicClassObj:"+basicClassObj);*/
		//（2）更新教师、班级、课程信息
		 
		List<TeacherLesson> tlList = c.getTeacherLessons();
		List<JSONObject> insertParamList= new ArrayList<JSONObject>();
		List<String> tIds= new ArrayList<String>();
		tIds.add(c.getTeacherId()+"");
		int count=0;
		
		//处理同步任教关系
		Boolean isOK = false;
		if("1".equals(from)){
			//logger.info("[update]updateClassroom111  ");
			String updateClassroomUrl = rb.getString("updateClassroomUrl");
			//获取extClassId
			List<JSONObject> extClassIds = new ArrayList<JSONObject>();
			Map<Long,String> classMap = new HashMap<Long,String>();
			JSONObject classParam = new JSONObject();
			List<Long> ids = new ArrayList<Long>();
			ids.add(c.getId());
			classParam.put("ids", ids);
			classParam.put("termInfoId", termInfoId);
			extClassIds = csCurCommonDataDao.getExtClassIdByclassId(classParam);
			if(extClassIds!=null ){
				for(JSONObject classObj :extClassIds){
					String extClassId = classObj.getString("extId");
					long classId = classObj.getLongValue("classId");
					classMap.put(classId, extClassId);
				}
			}
			//
			//logger.info("[update]updateClassroom222  ");
			//根据teacherId查询extAccountId
			Map<Long,String> accountMap = new HashMap<Long,String>();
			List<JSONObject> extAccountIdlist = new ArrayList<JSONObject>();
			if(tlList!=null){
				for(TeacherLesson tl:tlList){
					long teacherId = tl.getTeacherId();
					if (teacherId == 0) {
						continue;
					}

					if(!tIds.contains(teacherId+"")){
						tIds.add(teacherId+"");
					}
				}
			}
			//logger.info("[update]updateClassroom333  ");
			if(tIds.size()>0){
				JSONObject relationshipParam = new JSONObject();
				relationshipParam.put("ids", tIds);
				relationshipParam.put("termInfoId", termInfoId);
				extAccountIdlist = csCurCommonDataDao.getExtAccountIdByUserIds(relationshipParam);
			}
			//logger.info("[update]updateClassroom444  ");
			if(extAccountIdlist.size()>0){
				for(JSONObject extAccountIdObj:extAccountIdlist){
					String extId = extAccountIdObj.getString("extAccountId");
					long userId = extAccountIdObj.getLongValue("userId");
					accountMap.put(userId, extId);
				}
			}
		    JSONObject json = new JSONObject();
		    //logger.debug("[update]updateClassroom555  classMap:"+classMap+" c.getId():"+ c.getId()+" accountMap:"+accountMap+ " c.getTeacherId():"+c.getTeacherId());
		    if(StringUtils.isBlank(classMap.get(c.getId()))){
		    	logger.info("[update]updateClassroom classMap.get(c.getId()) is null  c.getId:"+c.getId()+ " classMap:"+classMap.toString());
		    	return ;
		    }
		    json.put("classId", classMap.get(c.getId()));
		    //logger.info("[update]updateClassroom511"); 
		    if(c.getClassType()!=null){
		    	json.put("classType", c.getClassType().getValue()+"");
		    }
		    //logger.info("[update]updateClassroom522"); 
		    if(StringUtils.isBlank(accountMap.get(c.getTeacherId()))){
		    	logger.info("[update]updateClassroom accountMap.get(c.getTeacherId()) is null  c.getTeacherId:"+c.getTeacherId()+ " accountMap:"+accountMap.toString());
		    }else{
		    	json.put("classMasterId", accountMap.get(c.getTeacherId()));
		    }
		    //logger.info("[update]updateClassroom666  ");
		    JSONArray data = new JSONArray();
		    Map<String,ArrayList<String>> teacherLessonMap = new HashMap<String,ArrayList<String>>();
		    Map<String,String> llMap = new HashMap<String,String>();
		    List<JSONObject> lList =this.getExtLessonIdBySchoolId(termInfoId, schoolId);
		   for(JSONObject l :lList){
			   llMap.put( l.getString("lessonId"),  l.getString("extId"));
		   }
		   //logger.info("[update]updateClassroom777  ");
		    if(tlList!=null){
				for(TeacherLesson tl:tlList){
					String extTeacherId = accountMap.get(tl.getTeacherId());
					long lessonId = tl.getLessonId();
					String extClassId=classMap.get(c.getId());
					//String extLessonId = TeachingRelationShipUtil.teachingRelationshipMap.get(lessonId);
					String extLessonId = llMap.get(lessonId+"");
					if(StringUtils.isBlank(extTeacherId)|| StringUtils.isBlank(extClassId)|| StringUtils.isBlank(extLessonId)){
						continue;
					}
					ArrayList<String> lessons=new ArrayList<String>();
					if(teacherLessonMap.containsKey(extTeacherId)){
						lessons=teacherLessonMap.get(extTeacherId);
					}
					if(!lessons.contains(extLessonId)){
						lessons.add(extLessonId);
					}
					teacherLessonMap.put(extTeacherId, lessons);
				}
		    }
		    //logger.info("[update]updateClassroom888  ");
		    //封装teacherClassLessonMap数据为data数据
		    for (Map.Entry<String,ArrayList<String>> entry : teacherLessonMap.entrySet()) { 
		    	String extTeacherId = entry.getKey();
		    	 ArrayList<String>  extLessons = entry.getValue();
		    	 JSONObject tldata = new JSONObject();
		    	 tldata.put("accountId", extTeacherId);
		    	 tldata.put("courseCodes", extLessons);
		    	 data.add(tldata);
 		    }
		    json.put("data", data);
		    //logger.info("[update] updateClassroom param: updateClassroomUrl"+updateClassroomUrl+" json:"+json.toJSONString());
		    JSONObject returnObj = null;
		    try{
		    	returnObj = HttpClientToken.sendPostNoToken(updateClassroomUrl, json);
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    //logger.debug("[update]updateClassroom999  "+returnObj);
		    if(returnObj!=null){
		    	JSONObject serverResult=(JSONObject) returnObj.get("serverResult");
		    	String resultCode = serverResult.getString("resultCode");
		    	if("200".equals(resultCode)){
		    		isOK=true;
		    	}
		    	logger.debug("[update] updateClassroom returnObj:"+returnObj.toJSONString());
		    }else{
		    	logger.debug("[update] updateClassroom returnObj is null");
		    }
		 }
		if(!(isOK&&"1".equals(from))){ //调基础平台接口成功才更新自己的基础数据
			return ;
		}
		//更新基础表
		csCurCommonDataDao.updateClassroom(basicClassObj);
		logger.debug("updateClassroom basicClassObj:"+basicClassObj);
		//先删除
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("classId", c.getId());
		param.put("termInfoId", termInfoId);
		csCurCommonDataDao.deleteTeacherLesson(param);
		if(tlList!=null){
			for(TeacherLesson tl:tlList){
				long teacherId = tl.getTeacherId();
				if (teacherId == 0) {
					continue;
				}
				JSONObject json = new JSONObject();
				json.put("lessonId", tl.getLessonId());
				json.put("classId", c.getId());
				json.put("teacherId", teacherId);

				if(!tIds.contains(teacherId+"")){
					tIds.add(teacherId+"");
				}
				insertParamList.add(json);
				count++;
				if(count==500){
					JSONObject p = new JSONObject();
					p.put("termInfoId", termInfoId);
					p.put("insertParamList", insertParamList);
					logger.debug("updateClassroom insertTeacherLessonBatch1:"+p.toJSONString());
					csCurCommonDataDao.insertTeacherLessonBatch(p);
					count=0;
					insertParamList.clear();
				}
			}
		}
		if(insertParamList.size()<500 && insertParamList.size()>0){
			JSONObject p = new JSONObject();
			p.put("termInfoId", termInfoId);
			p.put("insertParamList", insertParamList);
			logger.debug("updateClassroom insertTeacherLessonBatch2:"+p.toJSONString());
			csCurCommonDataDao.insertTeacherLessonBatch(p);
		}
	}
	/**
	 * 根据多个机构的任教科目或者任教年级获取老师姓名（机构成员）  
	 * @param leaderNameMap<String,List<String>> orgId-name 要过滤的名称(orgId非uuid)
	 * @param orgIdScopeIdmap<String,String> orgId-scopeId  (currentLevel)
	 * @param orgIdLessonIdmap<String,String> orgId-lessonId
	 * @param schoolId 学校
	 * @author zhh
	 */
	 
	private List<JSONObject> getTeacherByLessonOrGradeBatch(String termInfoId,String name,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String xn)throws Exception{
		if(StringUtils.isBlank(schoolId))
		{
			return null;
		}
		if(orgIdScopeIdsmap==null || orgIdLessonIdmap==null)
		{
			return null;
		}
		if(StringUtils.isBlank(xn))
		{
			return null;
		}
		Set<String> scopeList = new HashSet<String>();//存放所有的scopeId
		if(orgIdScopeIdsmap!=null)
		{
			
			for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
			{
				 Set<String> s = entry.getValue();
					 if(s!=null && s.size()>0){
						 scopeList.addAll(s);
					 }
			}
			
		}
		Set<String> lessonList = new HashSet<String>(); //存放所有的lessonId
		if(orgIdLessonIdmap!=null){
			for (Map.Entry<String, String> entry : orgIdLessonIdmap.entrySet()) 
			{
				 String s = entry.getValue();
				 if(StringUtils.isNotBlank(s)){
					 List<String> eList = Arrays.asList(s.split(","));
					 if(eList!=null && eList.size()>0){
						 lessonList.addAll(eList);
					 }
				 }
			}
		}
		JSONObject obj = new JSONObject();
		if(scopeList.size()<1){
			scopeList.add("-1");
		}
		obj.put("ids", new ArrayList(scopeList)); //currentLevels
		if(lessonList.size()<1){
			lessonList.add("-1");
		}
		obj.put("lessonIds", new ArrayList(lessonList));
		
		obj.put("schoolId", schoolId);
		obj.put("xn", xn);
		obj.put("noIds", removeGIdList);
		obj.put("name", name);
		obj.put("termInfoId", termInfoId);
		List<JSONObject> list = csCurCommonDataDao.getTeacherByLessonOrGradeBatch(obj);
		return list;
	} 
	  public List<Account> getOrgTeacherList(JSONObject obj) {
		String termInfoId = obj.getString("termInfoId");
		String xn = obj.getString("xn");
		String schoolId = obj.getString("schoolId");
		List<JSONObject> dInfos = csCurCommonDataDao.getOrgInfos(obj);
		List<Account> aList = new ArrayList<Account>();
		List<String> idsList = new ArrayList<String>(); //返回结果去重
		for(JSONObject dInfo:dInfos){
			String name = dInfo.getString("name");
			Long accountId = dInfo.getLong("accountId");
			int type = dInfo.getIntValue("type");
			if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
				continue;
			}
			if(type==1){
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!idsList.contains(accountId+"")){ //获取领导
					aList.add(a);
					idsList.add(accountId+"");
				}
			}
		} //end of for dInfos
		//获取机构-年级数据
		Map<String,LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		List<JSONObject> osList = csCurCommonDataDao.getOrgScopeList(obj);
		List<String> removeGIdList = new ArrayList<String>();
		for(JSONObject os:osList){
			int currentLevel = os.getIntValue("currentLevel");
			Boolean isGraduate = isGraduate(os.getInteger("createLevel"), os.getInteger("currentLevel"));
			if(isGraduate){
				if(StringUtils.isNotBlank(os.getString("gradeId"))){
					if(!removeGIdList.contains(os.getString("gradeId"))){
						removeGIdList.add(os.getString("gradeId"));
					}
				}
				continue;
			}
			String orgId = os.getString("orgId");
			LinkedHashSet<String> idSet =  new LinkedHashSet();
			if(orgIdScopeIdsmap.containsKey(orgId)){
				idSet = orgIdScopeIdsmap.get(orgId);	
			}
			if(currentLevel!=0){
				idSet.add(currentLevel+"");
				orgIdScopeIdsmap.put(orgId, idSet);
			}
		}
		//获取机构-科目数据
		Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();
		//Map<String,String> orgIdLessonNamesMap = new HashMap<String, String>();
		List<JSONObject> olList = csCurCommonDataDao.getOrgLessonList(obj);
		for(JSONObject ol:olList){
			String orgId = ol.getString("orgId");
			String lessonId = ol.getString("lessonId");
			String ids = "";
			if(orgIdLessonIdsmap.containsKey(orgId)){
				ids = orgIdLessonIdsmap.get(orgId);	
			}
			if(StringUtils.isNotBlank(lessonId)){
				ids+= lessonId +",";
				orgIdLessonIdsmap.put(orgId, ids);
			}
		}
		//获取机构成员数据
		List<JSONObject> rList=null;
		try {
			rList = getTeacherByLessonOrGradeBatch(termInfoId,obj.getString("name"),removeGIdList,orgIdScopeIdsmap,orgIdLessonIdsmap,schoolId,xn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//返回成员数据
		if(rList!=null){
			for(JSONObject r:rList){
				String name = r.getString("name");
				Long accountId = r.getLong("accountId");
				if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
					continue;
				}
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!idsList.contains(accountId+"")){  //去重
					idsList.add(accountId+"");
					aList.add(a);
				}
			}
		}
		return aList;
	}  
	/**
	 * 通过机构id 机构type 查询教师accountId name
	 * @param obj   orgIdList name type
	 * @return
	 */
	/*public List<Account> getOrgTeacherList(JSONObject obj)
	{
		List<JSONObject> list=new ArrayList<JSONObject>();
		if(obj.containsKey("orgResearchIdList")&&obj.containsKey("orgGradeIdList"))
		{
			list=csCurCommonDataDao.getOrgIntersectionTeacherList(obj);
		}
		else
		{
			list=csCurCommonDataDao.getOrgTeacherList(obj);
		}
		List<Account> aList=new ArrayList<Account>();
		if(null!=list && list.size()>0)
		{
			for(JSONObject json:list)
			{
				Account a=new Account();
				a.setId(json.getLongValue("id"));
				a.setName(json.getString("name"));
				aList.add(a);
			}
		}
		return aList;
	}*/
	
	 public List<Account> getOrgTeacherListAllType(JSONObject obj) {
		String termInfoId = obj.getString("termInfoId");
		List<String> tIds = (List<String>) obj.get("tIds");
		//List<String> ids = (List<String>) obj.get("ids"); //机构代码
		String xn = obj.getString("xn");
		String schoolId = obj.getString("schoolId");
		obj.put("isQueryMember", 1);
		List<JSONObject> dInfos = csCurCommonDataDao.getOrgInfos(obj);
		List<Account> aList = new ArrayList<Account>();
		List<String> idsList = new ArrayList<String>(); //返回结果去重
		List<String> oIds = new ArrayList<String>(); //年级和教研组Id
		List<String> pIds = new ArrayList<String>(); //备课组Id
		for(JSONObject dInfo:dInfos){
			String name = dInfo.getString("name");
			Long accountId = dInfo.getLong("accountId");
			int orgType = dInfo.getIntValue("orgType");
			int type = dInfo.getIntValue("type");
			String orgId = dInfo.getString("orgId");
			if(StringUtils.isBlank(orgId)){
				continue;
			}
			if(T_OrgType.T_Depart.getValue() != orgType && T_OrgType.T_PreLesson.getValue()!=orgType){ //不为科室,不为备课组
				if(!oIds.contains(orgId)){
					oIds.add(orgId);
				}
			}else if(T_OrgType.T_PreLesson.getValue()==orgType){ //为备课组
				if(!pIds.contains(orgId)){
					pIds.add(orgId);
				}
			}
			if( StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
				continue;
			}
			if(T_OrgType.T_Depart.getValue() == orgType ||type==1){ //科室取所有人，其他只取领导，成员从任教关系中获取
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!idsList.contains(accountId+"")){ //获取领导（年级教研备课）或者领导成员（科室）
					aList.add(a);
					idsList.add(accountId+"");
				}
			}
			
		} //end of for dInfos
		List<String> getIds = new ArrayList<String>();
		getIds.addAll(oIds);
		getIds.addAll(pIds);
		obj.put("ids", getIds);
		tIds.remove(T_OrgType.T_Depart.getValue()+"");
		//获取不为科室的成员列表
		if(getIds.size()>0){
			//获取机构-年级数据
			Map<String,LinkedHashSet<String>> pOrgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>(); //备课组的年级
			Map<String,LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();//年级组
			List<JSONObject> osList = csCurCommonDataDao.getOrgScopeList(obj);
			List<String> removeGIdList = new ArrayList<String>();
			for(JSONObject os:osList){
				int currentLevel = os.getIntValue("currentLevel");
				Boolean isGraduate = isGraduate(os.getInteger("createLevel"), os.getInteger("currentLevel"));
				if(isGraduate){
					if(StringUtils.isNotBlank(os.getString("gradeId"))){
						if(!removeGIdList.contains(os.getString("gradeId"))){
							removeGIdList.add(os.getString("gradeId"));
						}
					}
					continue;
				}
				String orgId = os.getString("orgId");
				if(pIds.contains(orgId)){ //备课组
					LinkedHashSet<String> idSet =  new LinkedHashSet();
					if(pOrgIdScopeIdsmap.containsKey(orgId)){
						idSet = pOrgIdScopeIdsmap.get(orgId);	
					}
					if(currentLevel!=0){
						idSet.add(currentLevel+"");
						pOrgIdScopeIdsmap.put(orgId, idSet);
					}
				}else{ //年级组
					LinkedHashSet<String> idSet =  new LinkedHashSet();
					if(orgIdScopeIdsmap.containsKey(orgId)){
						idSet = orgIdScopeIdsmap.get(orgId);	
					}
					if(currentLevel!=0){
						idSet.add(currentLevel+"");
						orgIdScopeIdsmap.put(orgId, idSet);
					}
				}
			}
			//获取机构-科目数据
			Map<String,String> pOrgIdLessonIdsmap = new HashMap<String, String>(); //备课组
			Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();//教研组
			List<JSONObject> olList = csCurCommonDataDao.getOrgLessonList(obj);
			for(JSONObject ol:olList){
				String orgId = ol.getString("orgId");
				String lessonId = ol.getString("lessonId");
				if(pIds.contains(orgId)){ //备课组
					String oids = "";
					if(pOrgIdLessonIdsmap.containsKey(orgId)){
						oids = pOrgIdLessonIdsmap.get(orgId);	
					}
					if(StringUtils.isNotBlank(lessonId)){
						oids+= lessonId +",";
						pOrgIdLessonIdsmap.put(orgId, oids);
					}
				}else{
					String oids = "";
					if(orgIdLessonIdsmap.containsKey(orgId)){
						oids = orgIdLessonIdsmap.get(orgId);	
					}
					if(StringUtils.isNotBlank(lessonId)){
						oids+= lessonId +",";
						orgIdLessonIdsmap.put(orgId, oids);
					}
				}
			}
			//获取年级组或者教研组机构成员数据
			List<JSONObject> rList=null;
			try {
				rList = getTeacherByLessonOrGradeBatch(termInfoId,obj.getString("name"),removeGIdList,orgIdScopeIdsmap,orgIdLessonIdsmap,schoolId,xn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//获取备课组机构成员数据
			List<JSONObject> pList=null;
			try {
				pList = getTeacherByLessonAndGradeBatch(termInfoId,obj.getString("name"),removeGIdList,pOrgIdScopeIdsmap,pOrgIdLessonIdsmap,schoolId,xn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//返回成员数据
			if(rList!=null){
				for(JSONObject r:rList){
					String name = r.getString("name");
					Long accountId = r.getLong("accountId");
					if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
						continue;
					}
					Account a = new Account();
					a.setName(name);
					a.setId(accountId);
					if(!idsList.contains(accountId+"")){  //去重
						idsList.add(accountId+"");
						aList.add(a);
					}
				}
			}
			if(pList!=null){
				for(JSONObject p:pList){
					String name = p.getString("name");
					Long accountId = p.getLong("accountId");
					if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
						continue;
					}
					Account a = new Account();
					a.setName(name);
					a.setId(accountId);
					if(!idsList.contains(accountId+"")){  //去重
						idsList.add(accountId+"");
						aList.add(a);
					}
				}
			}
		}
		return aList;
	} 
	/*	*//**
		 * 根据多个机构的任教科目且任教年级获取老师姓名（机构成员）  
		 * @param leaderNameMap<String,List<String>> orgId-name 要过滤的名称(orgId非uuid)
		 * @param orgIdScopeIdmap<String,String> orgId-scopeId  (currentLevel)
		 * @param orgIdLessonIdmap<String,String> orgId-lessonId
		 * @param schoolId 学校
		 * @author zhh
		 */ 
		private List<JSONObject> getTeacherByLessonAndGradeBatch(String termInfoId,String name,List<String> removeGIdList,Map<String, LinkedHashSet<String>> orgIdScopeIdsmap,Map<String,String> orgIdLessonIdmap, String schoolId,String xn)throws Exception{
			if(StringUtils.isBlank(schoolId))
			{
				return null;
			}
			if(orgIdScopeIdsmap==null || orgIdLessonIdmap==null)
			{
				return null;
			}
			if(StringUtils.isBlank(xn))
			{
				return null;
			}
			Set<String> scopeList = new HashSet<String>();//存放所有的scopeId
			if(orgIdScopeIdsmap!=null)
			{
				
				for (Map.Entry<String, LinkedHashSet<String>> entry : orgIdScopeIdsmap.entrySet()) 
				{
					 Set<String> s = entry.getValue();
						 if(s!=null && s.size()>0){
							 scopeList.addAll(s);
						 }
				}
				
			}
			Set<String> lessonList = new HashSet<String>(); //存放所有的lessonId
			if(orgIdLessonIdmap!=null){
				for (Map.Entry<String, String> entry : orgIdLessonIdmap.entrySet()) 
				{
					 String s = entry.getValue();
					 if(StringUtils.isNotBlank(s)){
						 List<String> eList = Arrays.asList(s.split(","));
						 if(eList!=null && eList.size()>0){
							 lessonList.addAll(eList);
						 }
					 }
				}
			}
			JSONObject obj = new JSONObject();
			obj.put("ids", new ArrayList(scopeList)); //currentLevels
			obj.put("lessonIds", new ArrayList(lessonList));
			obj.put("schoolId", schoolId);
			obj.put("xn", xn);
			obj.put("noIds", removeGIdList);
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			List<JSONObject> list = new ArrayList<JSONObject>();
			if(scopeList.size()>0 && lessonList.size()>0){
				list = csCurCommonDataDao.getTeacherByLessonAndGradeBatch(obj);
			}
			return list;
	} 
	/**
	 * 通过学校ID获取学校
	 * @param schoolId termInfoId
	 * @return  School  long id;
						List<Long> grades;
						List<Long> managerAccountIds
						List<Long> teacherAccountIds
						List<Long> staffAccountIds
						@author zhanghuihui 添加 termIds
	 */
	public School getSchoolById(JSONObject param)
	{
		String termInfoId = param.getString("termInfoId");
		School school=new School();
		Long schoolId=param.getLongValue("schoolId");
		JSONObject areaObj = csCurCommonDataDao.getAreaCode(param);
		JSONObject s = csCurCommonDataDao.getSchoolById1(schoolId, termInfoId);
		String name = s.getString("name");
		String extId = s.getString("extId");
		school.setExtId(extId);
		school.setName(name);
		if(areaObj!=null){
			school.setAreaCode(areaObj.getIntValue("areaCode"));
		}
		school.setId(schoolId);
		List<JSONObject> list=csCurCommonDataDao.getSchoolById(schoolId,termInfoId);
		List<Long> managerAccountIds=new ArrayList<Long>();
		List<Long> teacherAccountIds=new ArrayList<Long>();
		List<Long> staffAccountIds=new ArrayList<Long>();
		List<Long> grades=new ArrayList<Long>();
		if(null!=list && list.size()>0)
		{
			for(JSONObject obj:list)
			{
				int role=obj.getIntValue("role");
				T_Role tRole=T_Role.findByValue(role);
				String ids=obj.getString("ids");
				if(StringUtils.isNotEmpty(ids))
				{
					if(role==99)
					{
						grades=StringUtil.toListFromString(ids);
					}
					else
					{
						switch (tRole) {
						case Teacher:
							teacherAccountIds=StringUtil.toListFromString(ids);
							break;
						case Staff:
							staffAccountIds=StringUtil.toListFromString(ids);
							break;
						case SchoolManager:
							managerAccountIds=StringUtil.toListFromString(ids);
							break;
						default:
							break;
						}
					}
					
					
				}
			}
		}
		school.setStaffAccountIds(staffAccountIds);
		school.setTeacherAccountIds(teacherAccountIds);
		school.setManagerAccountIds(managerAccountIds);
		if(grades.size()>0)
		{
			int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
			param.put("xn", xn);
			param.put("ids", grades);
			List<JSONObject> gradeList=csCurCommonDataDao.getGradeListBySchoolId(param);
			if(null!=gradeList)
			{
				grades=new ArrayList<Long>();
				for(JSONObject g:gradeList)
				{
					//获取currentLevel
					int  currentLevel = g.getInteger("currentLevel");
					int  createLevel = g.getInteger("createLevel");			
					//获取isGraduate
					if(!isGraduate(createLevel, currentLevel))
					{
						grades.add(g.getLongValue("id"));
					}
				}
			}
		}
		school.setGrades(grades);
		//获取termIds
		JSONObject json = new JSONObject();
		json.put("termInfoId", termInfoId);
		json.put("schoolId", schoolId);
		List<Long> termIds = csCurCommonDataDao.getSchoolTermIds(json);
		if(termIds!=null){
			school.setTermIds(termIds);
		}
		return school;
	}
	/**
	 *@see 创建的原因
	 *@date 2016年1月20日 上午11:26:20
	 *@version 版本
	 *@author zxy
	 *@update 20160504 teacher角色添加empno返回    BY:zhh
	 *@update 20160509 添加parent角色返回（parentPart新增studentName字段） BY:zhh 
	 *@update 20160726处理添加accountPart返回  BY：zhh
	 */
	public List<Account> getAccountBatch(long schoolId, List<Long> accountIds,String termInfoId) {
		List<Account> accounts = new ArrayList<Account>();
		JSONObject jsonObject = new JSONObject();
		if(null==accountIds||accountIds.isEmpty()||schoolId==0)
		{
			return accounts;
		}
		jsonObject.put("ids", accountIds);
		jsonObject.put("termInfoId", termInfoId);
		Long start = System.currentTimeMillis();
		List<JSONObject> accountBatch = csCurCommonDataDao.getAccountBatch(jsonObject);
		Long end = System.currentTimeMillis();
		//System.out.println("[getAccountBatch]耗时："+(end-start));
		if(CollectionUtils.isNotEmpty(accountBatch)){
			List<Long> teacherIds = new ArrayList<Long>();
			List<Long> parentIds = new ArrayList<Long>();
			List<Long> studentIds = new ArrayList<Long>();
			List<Long> staffIds = new ArrayList<Long>();
			List<Long> schoolIds = new ArrayList<Long>();
			Map<Long,Account> accountMap=new HashMap<Long,Account>();
			Map<Long,List<Long>> accountIdToUserIdsMap = new HashMap<Long,List<Long>>();
			Map<Long,TeacherPart> teacherPartMap=new HashMap<Long,TeacherPart>();
			Map<Long,StudentPart> studentPartMap=new HashMap<Long,StudentPart>();
			Map<Long,ParentPart> parentPartMap=new HashMap<Long,ParentPart>();
			Map<Long,StaffPart> staffPartMap=new HashMap<Long,StaffPart>();
			Map<Long,SchoolManagerPart> schoolManagerPartMap=new HashMap<Long,SchoolManagerPart>();
			for (JSONObject jsonObject2 : accountBatch) {
				String idNumber=jsonObject2.getString("idNumber");
				Long accountId=jsonObject2.getLongValue("accountId");
				Long userId=jsonObject2.getLong("id");
				String deanOfOrgIds = jsonObject2.getString("deanOfOrgIds");
				String orgIds = jsonObject2.getString("orgIds");
				//String extAccountId = jsonObject2.getString("extAccountId");
				Account account = null;
//				List<Long> userIds = null;
				List<User> users=null;
				//处理account
				if(!accountMap.containsKey(accountId))
				{
					account = new Account();
					account.setIdNumber(idNumber);
					account.setId(accountId);
					account.setExtId(jsonObject2.getString("extUserId"));
					account.setAccount(jsonObject2.getString("accountName"));
					account.setName(jsonObject2.getString("name"));
					account.setGender(T_Gender.findByValue(jsonObject2.getIntValue("gender")));
//					userIds = new ArrayList<Long>();
					users=new ArrayList<User>();
					accountMap.put(accountId, account);
					accounts.add(account);
				}
				else
				{
					account=accountMap.get(accountId);
//					userIds=account.getUserIds();
					users=account.getUsers();
				}
				//处理AccountPart中的userIds
				if(!accountIdToUserIdsMap.containsKey(accountId)){
					List<Long> userIds  = new  ArrayList<Long>();
					userIds.add(userId);
					accountIdToUserIdsMap.put(accountId,userIds);
				}else{
					List<Long> userIds=accountIdToUserIdsMap.get(accountId);
					userIds.add(userId);
					accountIdToUserIdsMap.put(accountId, userIds);
				}
				
				//处理users
//				userIds.add(userId);
//				account.setUserIds(userIds);
				UserPart userPart = new UserPart();
				userPart.setId(userId);
		
				List<Long> oList= null;
				List<Long> odList= null;
				if(StringUtils.isNotBlank(orgIds)){
					oList=StringUtil.toListFromString(orgIds);
				}
				if(StringUtils.isNotBlank(deanOfOrgIds)){
					odList=StringUtil.toListFromString(deanOfOrgIds);
				}
				userPart.setOrgIds(oList);
				userPart.setDeanOfOrgIds(odList);
				userPart.setAccountId(accountId);
				userPart.setRole(T_Role.findByValue(jsonObject2.getIntValue("role")));
				User user = new User();
				users.add(user.setUserPart(userPart));
				Account accountPart= new Account();
				accountPart.setId(accountId);
				accountPart.setAccount(jsonObject2.getString("accountName"));
				accountPart.setName(jsonObject2.getString("name"));
				accountPart.setGender(T_Gender.findByValue(jsonObject2.getIntValue("gender")));
				user.setAccountPart(accountPart);
				user.setExtId(jsonObject2.getString("extAccountId"));
				//users.add();
				account.setUsers(users);
				int role=jsonObject2.getIntValue("role");
				T_Role tRole = T_Role.findByValue(role);
				switch (tRole) {
					case Teacher://教师 
						teacherIds.add(userId);
						break;
					case Student://学生
						studentIds.add(userId);
						break;
					case Parent://家长
						parentIds.add(userId);
						break;
					case Staff://学校人员
						staffIds.add(userId);
						break;
					case SchoolManager://学校管理人员
						schoolIds.add(userId);
						break;
					default:
						break;
				}
			}//end of for accountBatch
			//老师代码块
			if(CollectionUtils.isNotEmpty(teacherIds) && teacherIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", teacherIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> teacherRoles = csCurCommonDataDao.queryTeacherRoleByUserId(jsonObject2);
				//查询任教关系
				Map<String,List<JSONObject>> hashMap = new HashMap<String,List<JSONObject>>();
				if(teacherIds.size()>0){
					List<JSONObject> cList = csCurCommonDataDao.queryTeacherCourseList(jsonObject2);
					for(JSONObject c:cList){
						String teacherId = c.getString("teacherId");
						if(hashMap.containsKey(teacherId)){
							List<JSONObject> list = hashMap.get(teacherId);
							list.add(c);
							hashMap.put(teacherId, list);
						}else{
							List<JSONObject> list = new ArrayList<JSONObject>();
							list.add(c);
							hashMap.put(teacherId, list);
						}
					}
				}
				if(CollectionUtils.isNotEmpty(teacherRoles) && teacherIds.size() > 0){
					for (JSONObject jsonObject3 : teacherRoles) {
						TeacherPart tp = new TeacherPart();
						tp.setId(jsonObject3.getLongValue("teacherId"));
						tp.setEmpno(jsonObject3.getString("empno"));
					    tp.setDeanOfClassIds(StringUtil.toListFromString(jsonObject3.getString("deanOfClassIds")));
					    teacherPartMap.put(jsonObject3.getLongValue("teacherId"), tp);
					   if(hashMap.containsKey(jsonObject3.getString("teacherId"))){
						 List<Course> courseList = new ArrayList<Course>();
						 List<JSONObject> tcList =  hashMap.get(jsonObject3.getString("teacherId"));
						 for(JSONObject tc:tcList){
							 Long classId = tc.getLongValue("classId");
							 Long lessonId = tc.getLongValue("lessonId");
							 Course c = new Course();
							 c.setClassId(classId);
							 c.setLessonId(lessonId);
							 courseList.add(c);
						 }
						 tp.setCourseIds(courseList);
					   }
					}
				}
			}
			//学生代码块
			if(CollectionUtils.isNotEmpty(studentIds) && studentIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", studentIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> studentList = csCurCommonDataDao.queryStudentListByUserId(jsonObject2);
				if(studentList!=null){
					for (JSONObject jsonObject3 : studentList) {
						StudentPart studentPart = new StudentPart();
						studentPart.setId(jsonObject3.getLongValue("id"));
						if(null!=jsonObject3.get("parentId")&&StringUtils.isNotEmpty(jsonObject3.getString("parentId")))
						{
							studentPart.setParentId(jsonObject3.getLongValue("parentId"));
						}
						studentPart.setIsYouth(jsonObject3.getIntValue("isYouth"));
						studentPart.setClassId(jsonObject3.getLongValue("classId"));
						studentPart.setSchoolNumber(jsonObject3.getString("schoolNumber"));
						studentPart.setStdNumber(jsonObject3.getString("stdNumber"));
						studentPartMap.put(jsonObject3.getLongValue("id"), studentPart);
	//					ParentPart parentPart=new ParentPart();
	//					parentPart.setClassId(jsonObject3.getLongValue("classId"));
	//					parentPart.setStudentId(jsonObject3.getLongValue("id"));
	//					parentPartMap.put(jsonObject3.getLongValue("id"), parentPart);
					}
				}
			}
			//家长代码模块
			if(CollectionUtils.isNotEmpty(parentIds) && parentIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", parentIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> parentList = csCurCommonDataDao.queryParentListByUserId(jsonObject2);
				//通过sIds返回学生的姓名 studentName
				List<Long> sIds= new ArrayList<Long>();
				Map<Long,String> studentIdNameMap = new  HashMap<Long, String>(); //studentId-accountName
				if(parentList!=null){
					for(JSONObject parentObj:parentList){
						sIds.add(parentObj.getLongValue("studentId"));
					}
				}
				JSONObject jsonObject22 = new JSONObject();
				if(sIds.size()>0){
					jsonObject22.put("userIds", sIds);
					jsonObject22.put("schoolId", schoolId);
					jsonObject22.put("termInfoId", termInfoId);
					List<JSONObject> studentList = csCurCommonDataDao.queryStudentNameListByUserId(jsonObject22);
					if(studentList!=null){
						for(JSONObject studentObj:studentList){
							Long studentId = studentObj.getLong("studentId");
							String studentName = studentObj.getString("studentName");
							if(studentId!=null){
								studentIdNameMap.put(studentId, studentName);
							}
						}
					}
				}
				if(parentList!=null){
					for (JSONObject jsonObject3 : parentList) {
						ParentPart parentPart = new ParentPart();
						parentPart.setId(jsonObject3.getLongValue("parentId"));
						Long studentId=jsonObject3.getLongValue("studentId");
						String studentName="";
						if(studentId!=null){
							studentName = studentIdNameMap.get(studentId);
							parentPart.setStudentName(studentName);
						}
						parentPart.setStudentId(studentId);
						parentPart.setClassId(jsonObject3.getLongValue("classId"));
						parentPartMap.put(jsonObject3.getLongValue("parentId"), parentPart);
					}
				}
			}
			//学校工作人员代码块
			if(CollectionUtils.isNotEmpty(staffIds) && staffIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", staffIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> staffRoles = csCurCommonDataDao.queryStaffListByUserId(jsonObject2);
				if(CollectionUtils.isNotEmpty(staffRoles) && teacherIds.size() > 0){
					for (JSONObject jsonObject3 : staffRoles) {
						StaffPart tp = new StaffPart();
						tp.setId(jsonObject3.getLongValue("staffId"));
					    tp.setEmpno(jsonObject3.getString("empno"));
					    staffPartMap.put(jsonObject3.getLongValue("staffId"), tp);
					}
				}
			}
			
			//学校管理员代码块
			if(CollectionUtils.isNotEmpty(schoolIds) && schoolIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", schoolIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> managerRoles = csCurCommonDataDao.queryManagerListByUserId(jsonObject2);
				if(CollectionUtils.isNotEmpty(managerRoles) && managerRoles.size() > 0){
					for (JSONObject jsonObject3 : managerRoles) {
						SchoolManagerPart tp = new SchoolManagerPart();
						tp.setId(jsonObject3.getLongValue("managerId"));
						tp.setEmpno(jsonObject3.getString("empno"));
						schoolManagerPartMap.put(jsonObject3.getLongValue("managerId"), tp);
					}
				}
			}
			
			for (Map.Entry<Long, Account> entry : accountMap.entrySet()) {
//				   System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
				Account account=entry.getValue();
				Long accountId = account.getId();
				if(null!=account)
				{
					List<User> users=account.getUsers();
					
					if(null!=users&&users.size()>0)
					{
						List<Long> userIds =new ArrayList<Long>();
						for(int i=0;i<users.size();i++)
						{
							User u=users.get(i);
							Account accountPart = u.getAccountPart();
							List<Long> userIdss = accountIdToUserIdsMap.get(accountId);
							if(accountPart!=null && userIdss!=null){
								accountPart.setUserIds(userIdss);
							}
							T_Role role=u.getUserPart().getRole();
							switch (role) {
							case Teacher://教师 
								if(teacherPartMap.containsKey(u.getUserPart().getId()))
								{
									userIds.add(u.getUserPart().getId());
									u.setTeacherPart(teacherPartMap.get(u.getUserPart().getId()));
								}
								else
								{
									users.remove(i);
									i--;
								}
								break;
							case Student://学生
//								if(parentPartMap.containsKey(u.getUserPart().getId()))
//								{
//									u.setParentPart(parentPartMap.get(u.getUserPart().getId()));
//								}
								if(studentPartMap.containsKey(u.getUserPart().getId()))
								{
									userIds.add(u.getUserPart().getId());
									u.setStudentPart(studentPartMap.get(u.getUserPart().getId()));
								}
								else
								{
									users.remove(i);
									i--;
								}
								
								break;
							case Parent: //家长
								if(parentPartMap.containsKey(u.getUserPart().getId())){
									userIds.add(u.getUserPart().getId());
									u.setParentPart(parentPartMap.get(u.getUserPart().getId()));
								}else{
									users.remove(i);
									i--;
								}
								break;
							case Staff://学校人员
								if(staffPartMap.containsKey(u.getUserPart().getId()))
								{
									userIds.add(u.getUserPart().getId());
									u.setStaffPart(staffPartMap.get(u.getUserPart().getId()));
								}
								else
								{
									users.remove(i);
									i--;
								}
								break;
							case SchoolManager://学校管理人员
								if(schoolManagerPartMap.containsKey(u.getUserPart().getId()))
								{
									userIds.add(u.getUserPart().getId());
									u.setSchoolManagerPart(schoolManagerPartMap.get(u.getUserPart().getId()));
								}
								else
								{
									users.remove(i);
									i--;
								}
								break;
							default:
								users.remove(i);
								i--;
								break;
						     }
					    }
					}//end of users null
					//account.getUsers()
				}//end of account null
		    }//end of map for
		} //end of accountBatch null
		
		return accounts;
	}
	/**
	 * 获取全校当前任课的老师信息accountId name userid 任课班级id 课程id
	 * @param obj  name
	 * @return  
	 */
	public List<Account> getCourseTeacherList(JSONObject obj)
	{
		List<JSONObject> list=csCurCommonDataDao.getCourseTeacherList(obj);
		List<Account> aList=new ArrayList<Account>();
		if(null!=list && list.size()>0)
		{
			HashMap<Long,List<Course>> map=new HashMap<Long,List<Course>>();
			for(JSONObject json:list)
			{
				Long accountId=json.getLongValue("teaId");
				Course c=new Course();
				c.setClassId(json.getLongValue("classId"));
				c.setLessonId(json.getLongValue("lessonId"));
				if(map.containsKey(accountId))
				{
					List<Course> courseIds=map.get(accountId);
					courseIds.add(c);
				}
				else
				{
					List<Course> courseIds=new ArrayList<Course>();
					courseIds.add(c);
					map.put(accountId, courseIds);
					Account a=new Account();
					a.setId(accountId);
					a.setName(json.getString("teaName"));
					List<Long> userIdList=new ArrayList<Long>();
					userIdList.add(json.getLongValue("userId"));
					a.setUserIds(userIdList);
					aList.add(a);
				}
			}
			
			Long schoolId=obj.getLongValue("schoolId");
			for(Account a:aList)
			{
				TeacherPart tp=new TeacherPart();
				tp.setSchoolId(schoolId);
				if(map.containsKey(a.getId()))
				{
					tp.setCourseIds(map.get(a.getId()));
				}
				User u=new User();
				UserPart up=new UserPart();
				up.setAccountId(a.getId());
				up.setId(a.getUserIds().get(0));
				up.setRole(T_Role.Teacher);
				u.setUserPart(up);
				u.setTeacherPart(tp);
				List<User> uList=new ArrayList<User>();
				uList.add(u);
				a.setUsers(uList);
				u.setAccountPart(Util.deepCopy(a));
			}
		}
		return aList;
	}
	/**
	 * 获取任课教师 JSONArray
	 * 
	 * @param obj
	 * schoolId 学校ID（必传） 
	 * lessonId 科目ID，多个使用逗号分隔
	 * classId 班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件）
     * termInfoId 学年学期（必传） 
     * usedGradeId 使用年级[可以多个逗号隔开] 
     * name 教师姓名
	 * @return JSONArray
	 * {"lessonName":"英语","lessonId":750,"classId":10033,"teaId":108688,"teaName":"白华"}
	 */
	public JSONArray getSimpleCourseTeacherList(JSONObject obj)
	{
		List<JSONObject> list=csCurCommonDataDao.getCourseTeacherList(obj);
		JSONArray arr=new JSONArray();
		if(null!=list && list.size()>0)
		{
			arr=JSON.parseArray(list.toString());
		}
		
		return arr;
	}
	
	/**
	 * 获取班主任信息 accountId name userid deanOfClassIds;
	 * @param obj  name
	 * @return
	 */
	public List<Account> getDeanList(JSONObject obj)
	{
		List<JSONObject> list=csCurCommonDataDao.getDeanList(obj);
		List<Account> aList=new ArrayList<Account>();
		if(null!=list && list.size()>0)
		{
			Long schoolId=obj.getLongValue("schoolId");
			for(JSONObject json:list)
			{
				Long accountId = json.getLongValue("id");
				Account a = new Account();
				a.setId(accountId);
				a.setName(json.getString("name"));
				List<Long> userIdList = new ArrayList<Long>();
				userIdList.add(json.getLongValue("userId"));
				a.setUserIds(userIdList);
				TeacherPart tp=new TeacherPart();
				tp.setSchoolId(schoolId);
				tp.setDeanOfClassIds(StringUtil.toListFromString(json.getString("deanOfClassIds")));
				User u=new User();
				UserPart up=new UserPart();
				up.setAccountId(a.getId());
				up.setId(a.getUserIds().get(0));
				up.setRole(T_Role.Teacher);
				u.setUserPart(up);
				u.setTeacherPart(tp);
				List<User> uList=new ArrayList<User>();
				uList.add(u);
				a.setUsers(uList);
				u.setAccountPart(Util.deepCopy(a));
				aList.add(a);
			}
		}
		return aList;
	}
	
	/**
	 * 获取全校所有教职工
	 * @param obj name
	 * @return
	 * Account{
		id：代码（long）
		name：账户名称（String）
		userIds：(List<Long>)
		users:(List<User>-》UserPart、AccountPart) 
		}
	 */
	public List<Account> getAllSchoolEmployees(JSONObject obj)
	{
		List<JSONObject> list=csCurCommonDataDao.getAllSchoolEmployees(obj);
		List<Long> aIds = new ArrayList<Long>();
		List<Account> aList=new ArrayList<Account>();
		if(null!=list && list.size()>0)
		{
			HashMap<Long,Account> accountMap=new HashMap<Long,Account>();
			for(JSONObject json:list)
			{
				Long accountId=json.getLongValue("id");
				if(!aIds.contains(accountId)){
					aIds.add(accountId);
				}
				Long userId=json.getLong("userId");
				Account account = null;
				List<Long> userIds = null;
				List<User> users=null;
				if(!accountMap.containsKey(accountId))
				{
					account = new Account();
					account.setId(accountId);
					account.setName(json.getString("name"));
					//account.setIdNumber(json.getString("idNumber"));
					userIds = new ArrayList<Long>();
					users=new ArrayList<User>();
					accountMap.put(accountId, account);
					aList.add(account);
				}
				else
				{
					account=accountMap.get(accountId);
					userIds=account.getUserIds();
					users=account.getUsers();
				}
				userIds.add(userId);
				account.setUserIds(userIds);
				UserPart userPart = new UserPart();
				userPart.setId(userId);
				userPart.setAccountId(accountId);
				userPart.setRole(T_Role.findByValue(json.getIntValue("role")));
				User user = new User();
				users.add(user.setUserPart(userPart));
				account.setUsers(users);
			}
		}
		Long schoolId = obj.getLong("schoolId");
		String termInfoId = obj.getString("termInfoId");
		List<Account> aLists = this.getAccountBatch(schoolId, aIds, termInfoId);
		return aLists;
	}
	
	/**
	 * 根据学校ID获取年级列表
	 * @update 已毕业的和幼儿园的年级不返回  By：zhh
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Grade> getGradeList(JSONObject param){
		//long start=System.currentTimeMillis();
		String termInfoId = param.getString("termInfoId");
		if(StringUtils.isBlank(termInfoId) || StringUtils.isBlank(param.getString("schoolId"))){
			return null;
		}
		int xn = Integer.parseInt(termInfoId.substring(0, 4));
		param.put("xn", xn);
		//根据学校ID获取年级列表
		List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
		//开始封装List<Grade>
		List<Grade> grades = new ArrayList<Grade>();
		if(gradeList!=null){
			for(JSONObject g:gradeList){
				Grade grade=new Grade();
				
				grade.setId(g.getLong("id"));
				//String [] str=g.getString("classIds").split(",");
				//NumberSort.halfSortBySx(str);
				//grade.setClassIds(stringArrToListLong(str));
				if(StringUtils.isNotBlank(g.getString("classIds"))){
					grade.setClassIds(StringUtil.toListFromString(g.getString("classIds")));
				}
				//获取currentLevel
				int  currentLevel = g.getInteger("currentLevel");
				int  createLevel = g.getInteger("createLevel");

				grade.setCreateLevel(T_GradeLevel.findByValue(createLevel));
				grade.setCurrentLevel(T_GradeLevel.findByValue(currentLevel));
				//获取isGraduate
				boolean isGraduate = isGraduate(createLevel, currentLevel);
				grade.setIsGraduate(isGraduate);
				if(!isGraduate){ //只有未毕业的年级才能出来
					 //过滤幼儿园
					//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
						grades.add(grade);
					//}
				}
			}
		}
		//logger.info("@@@@【getGradeList】耗时："+(System.currentTimeMillis()-start));
		return grades;
	}
	/**
	 * 根据年级ID获取年级列表(已毕业，则过滤)
	 * @update 已毕业的和幼儿园的年级不返回  By：zhh
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Grade> getGradeBatch(JSONObject param){
		//long start=System.currentTimeMillis();
		String termInfoId = param.getString("termInfoId");
		if(StringUtils.isBlank(termInfoId)){
			return null;
		}
		int xn = Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		param.put("xn", xn);
		//根据学校ID获取年级列表
		List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
		logger.debug("[getGradeBatch]gradeList:"+gradeList.toString());
		//开始封装List<Grade>
		List<Grade> grades = new ArrayList<Grade>();
		if(gradeList!=null){
			for(JSONObject g:gradeList){
				Grade grade=new Grade();
				
				grade.setId(g.getLong("id"));
				if(StringUtils.isNotBlank(g.getString("classIds"))){
					grade.setClassIds(StringUtil.toListFromString(g.getString("classIds")));
				}
				//获取currentLevel
				int  currentLevel = g.getInteger("currentLevel");
				int  createLevel = g.getInteger("createLevel");
				
				grade.setCreateLevel(T_GradeLevel.findByValue(createLevel));
				grade.setCurrentLevel(T_GradeLevel.findByValue(currentLevel));
				//获取isGraduate
				boolean isGraduate = isGraduate(createLevel, currentLevel);
				grade.setIsGraduate(isGraduate);
				if(!isGraduate){
					 //过滤幼儿园
					//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
						grades.add(grade);
					//}
				}
			}
		}
		//logger.info("@@@@【getGradeBatch】耗时："+(System.currentTimeMillis()-start));
		return grades;
	}
	/**
	 * 获取班级列表
	 * @update 已毕业的和幼儿园的年级的班级不返回  By：zhh
	 * @param map
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getClassList(HashMap<String, Object> map){
		//Long start = System.currentTimeMillis();
	    List<Classroom> cList = new ArrayList<Classroom>();
	    String termInfoId = (String) map.get("termInfoId");
	    if(StringUtils.isBlank(termInfoId) ||  map.get("schoolId")==null){
			return null;
		}
	    int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		//根据学校ID获取年级列表
	    JSONObject param = new JSONObject();
	    param.put("schoolId", map.get("schoolId"));
	    param.put("xn",xn);
	    param.put("termInfoId",termInfoId);
		List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
		Map<String,Boolean> isGraduateMap = new HashMap<String, Boolean>(); //currentLevel-- isGraduate
		if(gradeList!=null){
			for(JSONObject g:gradeList){
				//获取currentLevel
				int  currentLevel = g.getInteger("currentLevel");
				int  createLevel = g.getInteger("createLevel");
		
				//获取isGraduate
				boolean isGraduate = isGraduate(createLevel, currentLevel);
				//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
					isGraduateMap.put(currentLevel+"", isGraduate);
				//}
			}
		}
	    //转换 使用年级转 currentLevel
	    String usedGradeIds=(String) map.get("usedGradeId");
	    List<Integer> currentLevels = new ArrayList<Integer>();
	    if(StringUtils.isNotBlank(usedGradeIds)&& usedGradeIds!=null){
		    String [] usedgs=usedGradeIds.trim().split(",");
		    for(String ug:usedgs){
		    	int currentLevel=xn-Integer.parseInt(ug)+10;
		    	//判断传入的年级是否已毕业(已毕业的班级不返回)
		    	if(isGraduateMap.containsKey(currentLevel+"")){
		    		if(!(Boolean)isGraduateMap.get(currentLevel+"")){
		    			//过滤幼儿园年级
		    			//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
		    				currentLevels.add(currentLevel);
		    			//}
		    		}
		    	}
		    }
		   
	    }else{
	    	 for(Map.Entry<String, Boolean> entry : isGraduateMap.entrySet()){
	    		if(!(boolean)entry.getValue()){
	    			currentLevels.add(Integer.parseInt(entry.getKey()));
	    		}
	    		
	    	}
	    }
	    if(currentLevels.size()==0){
	    	List<Integer> list = new ArrayList<Integer>();
	    	list.add(-1);
	    	map.put("currentLevels",list);
	    }else{
	    	map.put("currentLevels",currentLevels);
	    }
	    map.put("xn", xn);
	    //logger.info("@@@@【getClassList】过滤毕业年级耗时："+(System.currentTimeMillis()-start));
	    //Long start1=System.currentTimeMillis();
	    //获取班级列表
	    List<JSONObject> classList=csCurCommonDataDao.getClassListBy(map);
	    Map<String,JSONObject> classMap = new HashMap<String, JSONObject>();
	    Set<String> classIdsSet=new HashSet<String>();
	    Set<String> gradeIdsSet=new HashSet<String>();
	    //List<String> classIds=new ArrayList<String>();
	    List<String> gradeIds=new ArrayList<String>();
	    if(classList!=null){
		    for(JSONObject c:classList){
		    	String gradeId = c.getString("gradeId");
		    	String classId = c.getString("id");
		    	gradeIdsSet.add(gradeId);
		    	classIdsSet.add(classId);
		    	classMap.put(classId, c);
		    	if(c.containsKey("classType")){
		    		int classType = c.getIntValue("classType");
		    		c.put("classType",classType);
		    	}else{
		    		c.put("classType", T_ClassType.multiple.getValue()); //默认为综合
		    	}
		    	
		    }
	    }
	   /* if(classIdsSet.size()==0){
	    	classIds.add("-1");
	    }else{
	    	classIds.addAll(classIdsSet);
	    }*/
	    if(gradeIdsSet.size()==0){
	    	gradeIds.add("-1");
	    }else{
	    	gradeIds.addAll(gradeIdsSet);
	    }
	    //logger.info("@@@@【getClassList】获取所有班级列表耗时："+(System.currentTimeMillis()-start1));
	    //Long start2=System.currentTimeMillis();
	    //获取班级里面的  班主任、学生、家长UserIds
	    JSONObject classObj= new JSONObject();
	    classObj.put("gradeIds", gradeIds);
	    //classObj.put("classIds", classIds);
	    classObj.put("schoolId", map.get("schoolId"));
	    classObj.put("termInfoId", termInfoId);
	    List<JSONObject> deanObjs = csCurCommonDataDao.getDeanIdByClassId(classObj);
	    List<JSONObject> studentObjs=csCurCommonDataDao.getStudentIdByClassId(classObj);
	    List<JSONObject> parentObjs=csCurCommonDataDao.getParentIdByClassId(classObj);
	   
	    //将班主任、家长、学生的UserIds 封装到userClass中
	    if(deanObjs!=null && deanObjs.size()>0){
	    	for(JSONObject deanObj:deanObjs){
		    	String teacherAccountId = deanObj.getString("accountId");
		    	String classId = deanObj.getString("classId");
		    	JSONObject c=classMap.get(classId);
		    	if(c!=null){
		    		c.put("deanAccountId", teacherAccountId);
		    	}
	    	}
	    }
	    if(studentObjs!=null && studentObjs.size()>0){
	    	for(JSONObject studentObj:studentObjs){
	    		String classId=studentObj.getString("classId");
	    		long studentAccountId=studentObj.getLong("accountId");
	    		JSONObject c=classMap.get(classId);
	    		if(c!=null){
	    			if(!c.containsKey("studentAccountIds")){
	    				c.put("studentAccountIds", new   ArrayList<Long>());
	    			}
	    			List<Long> studentAccountIdList=(List<Long>) c.get("studentAccountIds");
	    			studentAccountIdList.add(studentAccountId);
	    		}
	    	}
	    }
	    if(parentObjs!=null && parentObjs.size()>0){
	    	for(JSONObject parentObj:parentObjs){
	    		String classId=parentObj.getString("classId");
	    		long parentAccountId=parentObj.getLong("accountId");
	    		JSONObject c=classMap.get(classId);
	    		if(c!=null){
	    			if(!c.containsKey("parentAccountIds")){
	    				c.put("parentAccountIds", new   ArrayList<Long>());
	    			}
	    			List<Long> parentAccountIdList=(List<Long>) c.get("parentAccountIds");
	    			parentAccountIdList.add(parentAccountId);
	    		}
	    	}
	    }
	    //logger.info("@@@@【getClassList】获取班主任 学生 家长耗时："+(System.currentTimeMillis()-start2));
	    //Long start3= System.currentTimeMillis();
	    //获取班级里面的科目和老师对应信息
	    List<JSONObject> ltList= csCurCommonDataDao.getLessonAndTeacherByClassId(classObj);
		Map<String,List<JSONObject>> ltMap=new HashMap<String, List<JSONObject>>();
		if(ltList!=null){
		    for(JSONObject lt:ltList){
		    	String classId = lt.getString("classId");
		    	if(!ltMap.containsKey(classId)){
		    		ltMap.put(classId,new ArrayList<JSONObject>());
		    	}
		    	List<JSONObject> cLists=ltMap.get(classId);
		    	cLists.add(lt);
		    }
		}
	    
	    //填充cList
	    for (Map.Entry<String, JSONObject> entry : classMap.entrySet()) {
	    	  Classroom cr=new Classroom();
	    	  JSONObject c= entry.getValue();
	    	  cr.setClassName(c.getString("className"));
	    	  cr.setClassType((T_ClassType.findByValue(c.getIntValue("classType"))));
	    	  cr.setId(c.getLongValue("id"));
	    	  cr.setGradeId(c.getLongValue("gradeId"));
	    	  cr.setDeanAccountId(c.getLongValue("deanAccountId"));
	    	  cr.setStudentAccountIds((List<Long>)c.get("studentAccountIds"));
	    	  cr.setParentAccountIds((List<Long>)c.get("parentAccountIds"));
	    	 
	    	    List<JSONObject> lts=ltMap.get(c.getString("id"));
				//List<TeacherLesson> tlList = new ArrayList<TeacherLesson>();
				List<AccountLesson> alList = new ArrayList<AccountLesson>();
				if(lts!=null){
					for(JSONObject lt:lts){
						long aId = lt.getLongValue("accountId");
						Long lId=lt.getLong("lessonId");
						//TeacherLesson tl=new TeacherLesson();
						AccountLesson al=new AccountLesson();
						//tl.setLessonId(lId);
						//tl.setTeacherId(tId);
						if(lId!=null && lId!=0L && aId!=0L){
							al.setLessonId(lId);
							al.setAccountId(aId);
							//tlList.add(tl);
							alList.add(al);
						}
					}
					cr.setAccountLessons(alList);
			        //cr.setTeacherLessons(tlList);
			     
				}
	    	  cList.add(cr);
	    }
	    //logger.info("@@@@【getClassList】获取科目老师耗时："+(System.currentTimeMillis()-start3));
	    return cList;
	}
	/**
	 * 获取简单班级列表(全校) 
	 * @update 已毕业的和幼儿园的年级的班级不返回  By：zhh
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getSimpleClassList(JSONObject param){
		 //Long start = System.currentTimeMillis();
		 String termInfoId =  param.getString("termInfoId");
		 if(StringUtils.isBlank(termInfoId) || StringUtils.isBlank(param.getString("schoolId"))){
				return null;
		 }
		 int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		 List<Classroom> cList = new ArrayList<Classroom>();
		 List<Grade> gradeList=(List<Grade>) param.get("gradeList");
		 List<Long> gradeIds=new ArrayList<Long>();
		 List<Long> gradeIdsSet=new ArrayList<Long>();
		 
		//根据学校ID获取年级列表
		    JSONObject gradeParam = new JSONObject();
		    gradeParam.put("schoolId", param.get("schoolId"));
		    gradeParam.put("xn", xn);
		    gradeParam.put("termInfoId", termInfoId);
			List<JSONObject> gradeListAll = csCurCommonDataDao.getGradeListBySchoolId(gradeParam);
			Map<String,Boolean> isGraduateMap = new HashMap<String, Boolean>(); //createLevel-- isGraduate
			if(gradeListAll!=null){
				for(JSONObject g:gradeListAll){
					//获取currentLevel
					int  currentLevel = g.getInteger("currentLevel");
					int  createLevel = g.getInteger("createLevel");
			
					//获取isGraduate
					boolean isGraduate = isGraduate(createLevel, currentLevel);
					//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
						isGraduateMap.put(g.getString("id")+"", isGraduate);
					//}
				}
			}
		 
		 if(gradeList!=null){
			 for(Grade g:gradeList){
				 Boolean isGraudate=isGraduateMap.get(g.getId()+"");
				 if(!isGraudate){
					 //if(g.getCurrentLevel().getValue()>=T_GradeLevel.T_PrimaryOne.getValue()){
						 gradeIdsSet.add(g.getId());
					 //}
				 }
			 }
			
		 }else{
			 for(Map.Entry<String, Boolean> entry : isGraduateMap.entrySet()){
		    		if(!(boolean)entry.getValue()){
		    			gradeIdsSet.add(Long.parseLong(entry.getKey()));
		    		}
		    		
		    }
		 }
		 if(gradeIdsSet.size()==0){
			 gradeIds.add(-1L);
		 }else{
			 gradeIds.addAll(gradeIdsSet);
		 }
		 //获取班级列表
		 HashMap<String,Object> paramMap = new HashMap<String, Object>();
		 paramMap.put("schoolId", param.get("schoolId"));
		 paramMap.put("gradeIds", gradeIds);
		 paramMap.put("termInfoId", termInfoId);
		 List<JSONObject> classList=csCurCommonDataDao.getClassListBy(paramMap);
		 if(classList!=null){
			 for(JSONObject c:classList){
				 Classroom cr = new Classroom();
				  cr.setClassName(c.getString("className"));
				  /*if(c.containsKey("classType")){
					  int classType=c.getInteger("classType");
					  cr.setClassType(T_ClassType.findByValue(classType));
				  }else{
					  cr.setClassType(T_ClassType.multiple);
				  }*/
		    	  cr.setId(c.getLongValue("id"));
		    	  cr.setGradeId(c.getLongValue("gradeId"));
		    	  cList.add(cr);
			 }
		 }
		 //logger.info("@@@@【getSimpleClassList】耗时："+(System.currentTimeMillis()-start));
		 return cList;
	}
	/**
	 * 根据班级ID查询班级
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getClassroomBatch(JSONObject param){
		//Long start = System.currentTimeMillis();
		 List<Classroom> cList = new ArrayList<Classroom>();
		    //获取班级列表
		 	HashMap<String,Object> map = new HashMap<String, Object>();
			map.put("schoolId", param.getString("schoolId"));
			map.put("ids", param.get("ids"));
			map.put("termInfoId", param.getString("termInfoId"));
		    List<JSONObject> classList=csCurCommonDataDao.getClassListBy(map);
		    Map<String,JSONObject> classMap = new HashMap<String, JSONObject>();
		    Set<String> classIdsSet=new HashSet<String>();
		    List<String> classIds=new ArrayList<String>();
		    if(classList!=null){
			    for(JSONObject c:classList){
			    	String classId = c.getString("id");
			    	classIdsSet.add(classId);
			    	classMap.put(classId, c);
			    	if(c.containsKey("classType")){
			    		int classType = c.getIntValue("classType");
			    		c.put("classType",classType);
			    	}else{
			    		c.put("classType", T_ClassType.multiple.getValue()); //默认为综合
			    	}
			    }
		    }
		    if(classIdsSet.size()==0){
		    	classIds.add("-1");
		    }else{
		    	classIds.addAll(classIdsSet);
		    }
		    //获取班级里面的  班主任、学生、家长UserIds
		    JSONObject classObj= new JSONObject();
		    classObj.put("classIds", classIds);
		    classObj.put("schoolId", param.get("schoolId"));
		    classObj.put("termInfoId", param.getString("termInfoId"));
		    List<JSONObject> deanObjs = csCurCommonDataDao.getDeanIdByClassId(classObj);
		    List<JSONObject> studentObjs=csCurCommonDataDao.getStudentIdByClassId(classObj);
		    List<JSONObject> parentObjs=csCurCommonDataDao.getParentIdByClassId(classObj);
		    //将班主任、家长、学生的UserIds 封装到userClass中
		    if(deanObjs!=null && deanObjs.size()>0){
		    	for(JSONObject deanObj:deanObjs){
			    	String teacherAccountId = deanObj.getString("accountId");
			    	String classId = deanObj.getString("classId");
			    	JSONObject c=classMap.get(classId);
			    	if(c!=null){
			    		c.put("deanAccountId", teacherAccountId);
			    	}
		    	}
		    }
		    if(studentObjs!=null && studentObjs.size()>0){
		    	for(JSONObject studentObj:studentObjs){
		    		String classId=studentObj.getString("classId");
		    		long studentAccountId=studentObj.getLong("accountId");
		    		long studentId=studentObj.getLong("studentId");
		    		JSONObject c=classMap.get(classId);
		    		if(c!=null){
		    			if(!c.containsKey("studentAccountIds")){
		    				c.put("studentAccountIds", new   ArrayList<Long>());
		    			}
		    			List<Long> studentAccountIdList=(List<Long>) c.get("studentAccountIds");
		    			studentAccountIdList.add(studentAccountId);
		    			if(!c.containsKey("studentIds")){
		    				c.put("studentIds", new   ArrayList<Long>());
		    			}
		    			List<Long> studentIdList=(List<Long>) c.get("studentIds");
		    			studentIdList.add(studentId);
		    		}
		    	}
		    }
		    if(parentObjs!=null && parentObjs.size()>0){
		    	for(JSONObject parentObj:parentObjs){
		    		String classId=parentObj.getString("classId");
		    		long parentAccountId=parentObj.getLong("accountId");
		    		long parentId = parentObj.getLong("parentId");
		    		
		    		JSONObject c=classMap.get(classId);
		    		if(c!=null){
		    			if(!c.containsKey("parentAccountIds")){
		    				c.put("parentAccountIds", new   ArrayList<Long>());
		    			}
		    			List<Long> parentAccountIdList=(List<Long>) c.get("parentAccountIds");
		    			parentAccountIdList.add(parentAccountId);
		    			if(!c.containsKey("parentIds")){
		    				c.put("parentIds", new   ArrayList<Long>());
		    			}
		    			List<Long> parentIdList=(List<Long>) c.get("parentIds");
		    			parentIdList.add(parentId);
		    		}
		    	}
		    }
		    //获取班级里面的科目和老师对应信息
		    List<JSONObject> ltList= csCurCommonDataDao.getLessonAndTeacherByClassId(classObj);
			Map<String,List<JSONObject>> ltMap=new HashMap<String, List<JSONObject>>();
			if(ltList!=null){
			    for(JSONObject lt:ltList){
			    	String classId = lt.getString("classId");
			    	if(!ltMap.containsKey(classId)){
			    		ltMap.put(classId,new ArrayList<JSONObject>());
			    	}
			    	List<JSONObject> cLists=ltMap.get(classId);
			    	cLists.add(lt);
			    }
			}
		    //填充cList
		    for (Map.Entry<String, JSONObject> entry : classMap.entrySet()) {
		    	  Classroom cr=new Classroom();
		    	  JSONObject c= entry.getValue();
		    	  cr.setClassName(c.getString("className"));
		    	  cr.setClassType((T_ClassType.findByValue(c.getIntValue("classType"))));
		    	  cr.setId(c.getLongValue("id"));
		    	  cr.setGradeId(c.getLongValue("gradeId"));
		    	  cr.setDeanAccountId(c.getLongValue("deanAccountId"));
		    	  cr.setStudentAccountIds((List<Long>)c.get("studentAccountIds"));
		    	  cr.setParentAccountIds((List<Long>)c.get("parentAccountIds"));
		    	  cr.setStudentIds((List<Long>)c.get("studentIds"));
		    	  cr.setParentIds((List<Long>)c.get("parentIds"));
		    	  
		    	    List<JSONObject> lts=ltMap.get(c.getString("id"));
					List<TeacherLesson> tlList = new ArrayList<TeacherLesson>();
					List<AccountLesson> alList = new ArrayList<AccountLesson>();
					if(lts!=null){
						for(JSONObject lt:lts){
							Long tId = lt.getLong("teacherId");
							long aId = lt.getLongValue("accountId");
							long lId=lt.getLongValue("lessonId");
							TeacherLesson tl=new TeacherLesson();
							AccountLesson al=new AccountLesson();
							tl.setLessonId(lId);
							tl.setTeacherId(tId);
							if(lId!=0l && aId!=0L){
							al.setLessonId(lId);
							al.setAccountId(aId);
							tlList.add(tl);
							alList.add(al);
							}
						}
						cr.setAccountLessons(alList);
				        cr.setTeacherLessons(tlList);
				     
					}
		    	  cList.add(cr);
		    }
		    //logger.info("@@@@【getClassroomBatch】耗时："+(System.currentTimeMillis()-start));
		 return cList;
	}
	/**
	 * 根据班级ID查询班级（NoAccount）
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getClassroomBatchNoAccount(JSONObject param){
		//Long start = System.currentTimeMillis();
		 List<Classroom> cList = new ArrayList<Classroom>();
		 
		  //获取班级列表
		 	HashMap<String,Object> map = new HashMap<String, Object>();
			map.put("schoolId", param.getString("schoolId"));
			map.put("ids", param.get("ids"));
			map.put("termInfoId", param.getString("termInfoId"));
		    List<JSONObject> classList=csCurCommonDataDao.getClassListBy(map);
		    Map<String,JSONObject> classMap = new HashMap<String, JSONObject>(); //id --classobj
		    Set<String> classIdsSet=new HashSet<String>(); 	//for 班级ID去重
		    List<String> classIds=new ArrayList<String>(); //存放所有去重后的班级ID
		    if(classList!=null){
			    for(JSONObject c:classList){
			    	String classId = c.getString("id");
			    	classIdsSet.add(classId);
			    	classMap.put(classId, c);
			    	if(c.containsKey("classType")){
			    		int classType = c.getIntValue("classType");
			    		c.put("classType",classType);
			    	}else{
			    		c.put("classType", T_ClassType.multiple.getValue()); //默认为综合
			    	}
			    }
		    }
		    if(classIdsSet.size()==0){
		    	classIds.add("-1");
		    }else{
		    	classIds.addAll(classIdsSet);
		    }
		    //获取班级里面的  班主任、学生、家长UserIds
		    JSONObject classObj= new JSONObject();
		    classObj.put("classIds", classIds);
		    classObj.put("schoolId", param.get("schoolId"));
		    classObj.put("termInfoId", param.getString("termInfoId"));
		    List<JSONObject> deanObjs = csCurCommonDataDao.getDeanIdByClassIdNoAccount(classObj);
		    List<JSONObject> studentObjs=csCurCommonDataDao.getStudentIdByClassIdNoAccount(classObj);
		    List<JSONObject> parentObjs=csCurCommonDataDao.getParentIdByClassIdNoAccount(classObj);
		    //将班主任、家长、学生的UserIds 封装到userClass中
		    if(deanObjs!=null && deanObjs.size()>0){
		    	for(JSONObject deanObj:deanObjs){
			    	String teacherAccountId = deanObj.getString("teacherId");//no account 这里实际是userId，下面的一样
			    	String classId = deanObj.getString("classId");
			    	JSONObject c=classMap.get(classId);
			    	if(c!=null){
			    		c.put("deanAccountId", teacherAccountId);
			    	}
		    	}
		    }
		    if(studentObjs!=null && studentObjs.size()>0){
		    	for(JSONObject studentObj:studentObjs){
		    		String classId=studentObj.getString("classId");
		    		long studentAccountId=studentObj.getLong("studentId");
		    		JSONObject c=classMap.get(classId);
		    		if(c!=null){
		    			if(!c.containsKey("studentAccountIds")){
		    				c.put("studentAccountIds", new   ArrayList<Long>());
		    			}
		    			List<Long> studentAccountIdList=(List<Long>) c.get("studentAccountIds");
		    			studentAccountIdList.add(studentAccountId);
		    		}
		    	}
		    }
		    if(parentObjs!=null && parentObjs.size()>0){
		    	for(JSONObject parentObj:parentObjs){
		    		String classId=parentObj.getString("classId");
		    		long parentAccountId=parentObj.getLong("parentId");
		    		JSONObject c=classMap.get(classId);
		    		if(c!=null){
		    			if(!c.containsKey("parentAccountIds")){
		    				c.put("parentAccountIds", new   ArrayList<Long>());
		    			}
		    			List<Long> parentAccountIdList=(List<Long>) c.get("parentAccountIds");
		    			parentAccountIdList.add(parentAccountId);
		    		}
		    	}
		    }
		    //获取班级里面的科目和老师对应信息
		    List<JSONObject> ltList= csCurCommonDataDao.getLessonAndTeacherByClassIdNoAccount(classObj);
			Map<String,List<JSONObject>> ltMap=new HashMap<String, List<JSONObject>>();
			if(ltList!=null){
			    for(JSONObject lt:ltList){
			    	String classId = lt.getString("classId");
			    	if(!ltMap.containsKey(classId)){
			    		ltMap.put(classId,new ArrayList<JSONObject>());
			    	}
			    	List<JSONObject> cLists=ltMap.get(classId);
			    	cLists.add(lt);
			    }
			}
		    //填充cList
		    for (Map.Entry<String, JSONObject> entry : classMap.entrySet()) {
		    	  Classroom cr=new Classroom();
		    	  JSONObject c= entry.getValue();
		    	  cr.setClassName(c.getString("className"));
		    	  cr.setClassType((T_ClassType.findByValue(c.getIntValue("classType"))));
		    	  cr.setId(c.getLongValue("id"));
		    	  cr.setGradeId(c.getLongValue("gradeId"));
		    	  cr.setDeanAccountId(c.getLongValue("deanAccountId")); //因为接口为noAccountId所以存的实际上是userId
		    	  cr.setStudentIds((List<Long>)c.get("studentAccountIds"));
		    	  cr.setParentIds((List<Long>)c.get("parentAccountIds"));
		    	 
		    	    List<JSONObject> lts=ltMap.get(c.getString("id"));
					//List<TeacherLesson> tlList = new ArrayList<TeacherLesson>();
					List<AccountLesson> alList = new ArrayList<AccountLesson>();
					if(lts!=null){
						for(JSONObject lt:lts){
							long tId = lt.getLongValue("teacherId");
							long lId=lt.getLongValue("lessonId");
							//TeacherLesson tl=new TeacherLesson();
							AccountLesson al=new AccountLesson();
							//tl.setLessonId(lId);
							//tl.setTeacherId(tId);
							if(lId!=0L && tId!=0L){
								al.setLessonId(lId);
								al.setAccountId(tId);
								//tlList.add(tl);
								alList.add(al);
							}
						}
						cr.setAccountLessons(alList);
				       // cr.setTeacherLessons(tlList);
				     
					}
		    	  cList.add(cr);
		    }
		 //logger.info("@@@@【getClassroomBatchNoAccount】耗时："+(System.currentTimeMillis()-start));
		 return cList;
	}
	/**
	 * 根据班级ID获取简单班级列表
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getSimpleClassBatch(JSONObject param){
		String termInfoId = param.getString("termInfoId");
		//Long start = System.currentTimeMillis();
		 List<Classroom> cList = new ArrayList<Classroom>();
		 //获取班级列表
		 HashMap<String,Object> paramMap = new HashMap<String, Object>();
		 paramMap.put("schoolId", param.get("schoolId"));
		 paramMap.put("ids", param.get("ids"));
		 paramMap.put("termInfoId", termInfoId);
		 List<JSONObject> classList=csCurCommonDataDao.getClassListBy(paramMap);
		 if(classList!=null){
			 for(JSONObject c:classList){
				 Classroom cr = new Classroom();
				  cr.setClassName(c.getString("className"));
		    	// cr.setClassType((T_ClassType)c.get("classType"));
		    	  cr.setId(c.getLongValue("id"));
		    	  cr.setGradeId(c.getLongValue("gradeId"));
		    	  cList.add(cr);
			 }
		 }
		 //logger.info("@@@@【getSimpleClassBatch】耗时："+(System.currentTimeMillis()-start));
		 return cList;
	}
	/**
	 * 根据科目ID获取科目列表
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<LessonInfo> getLessonInfoBatch(JSONObject param){
		//Long start = System.currentTimeMillis();
		List<LessonInfo> lessonList= new ArrayList<LessonInfo>();
		List<JSONObject> lList=csCurCommonDataDao.getLessonListBySchoolId(param);
		if(lList!=null){
			for(JSONObject l:lList){
				LessonInfo lesson= new LessonInfo();
				String name=l.getString("name").trim();
				if(StringUtils.isBlank(name)){
					continue;
				}
				lesson.setName(name);
				String simpleName = l.getString("simpleName");
				if(StringUtils.isBlank(simpleName)){
					if(StringUtils.isNotBlank(name)){
						lesson.setSimpleName(name.substring(0,1)); //如果为空则给第一个字
					}
				}else{
					lesson.setSimpleName(l.getString("simpleName"));
				}
				lesson.setId(l.getLongValue("lessonId"));
				lesson.setType(l.getIntValue("lessontype"));
				lessonList.add(lesson);
			}
		}
		//logger.info("@@@@【getLessonInfoBatch】耗时："+(System.currentTimeMillis()-start));
		return lessonList;
	}
	/**
	 * 获取全校科目列表
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<LessonInfo> getLessonInfoList(JSONObject param){
		//Long start = System.currentTimeMillis();
		List<LessonInfo> lessonList= new ArrayList<LessonInfo>();
		List<JSONObject> lList=csCurCommonDataDao.getLessonListBySchoolId(param);
		if(lList!=null){
			for(JSONObject l:lList){
				LessonInfo lesson= new LessonInfo();
				String name=l.getString("name").trim();
				if(StringUtils.isBlank(name)){
					continue;
				}
				lesson.setName(name);
				String simpleName = l.getString("simpleName");
				if(StringUtils.isBlank(simpleName)){
					if(StringUtils.isNotBlank(name)){
						lesson.setSimpleName(name.substring(0,1)); //如果为空则给第一个字
					}
				}else{
					lesson.setSimpleName(l.getString("simpleName"));
				}
				lesson.setId(l.getLongValue("lessonId"));
				lesson.setType(l.getIntValue("lessontype"));
				lessonList.add(lesson);
			}
		}
		//logger.info("@@@@【getLessonInfoList】耗时："+(System.currentTimeMillis()-start));
		return lessonList;
	}
	/**
	 * 获取某个类型下的科目
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<LessonInfo> getLessonInfoByType(JSONObject param){
		//Long start=System.currentTimeMillis();
		List<LessonInfo> lessonList= new ArrayList<LessonInfo>();
		List<JSONObject> lList=csCurCommonDataDao.getLessonListBySchoolId(param);
		if(lList!=null){
			for(JSONObject l:lList){
				LessonInfo lesson= new LessonInfo();
				String name=l.getString("name").trim();
				if(StringUtils.isBlank(name)){
					continue;
				}
				lesson.setName(name);
				String simpleName = l.getString("simpleName");
				if(StringUtils.isBlank(simpleName)){
					if(StringUtils.isNotBlank(name)){
						lesson.setSimpleName(name.substring(0,1)); //如果为空则给第一个字
					}
				}else{
					lesson.setSimpleName(l.getString("simpleName"));
				}
				lesson.setId(l.getLongValue("lessonId"));
				lesson.setType(l.getIntValue("lessonType"));
				lessonList.add(lesson);
			}
		}
		//logger.info("@@@@【getLessonInfoByType】耗时："+(System.currentTimeMillis()-start));
		return lessonList;
	}
	/**
	 * 获取查询条件下（schoolId,name）学校学生信息
	 * @param params
	 * @return
	 * author wxq
	 */
	public List<Account> getSchoolStudentList(JSONObject params) {
		//判断当前年级是否已毕业
		String termInfoId = params.getString("termInfoId");
		String schoolId = params.getString("schoolId");
		  int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
			//根据学校ID获取年级列表
		    JSONObject param = new JSONObject();
		    param.put("schoolId", schoolId);
		    param.put("xn",xn);
		    param.put("termInfoId", termInfoId);
			List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
			Map<Long,Boolean> isGraduateMap = new HashMap<Long, Boolean>(); //createLevel-- isGraduate
			if(gradeList!=null){
				for(JSONObject g:gradeList){
					//获取currentLevel
					int  currentLevel = g.getInteger("currentLevel");
					int  createLevel = g.getInteger("createLevel");
			
					//获取isGraduate
					boolean isGraduate = isGraduate(createLevel, currentLevel);
					isGraduateMap.put(g.getLong("id"), isGraduate);
				}
			}
		    //转换 使用年级转 createLevel
		    List<Long> gids = new ArrayList<Long>();
	    	 for(Map.Entry<Long, Boolean> entry : isGraduateMap.entrySet()){
	    		if(!(boolean)entry.getValue()){
	    			gids.add(entry.getKey());
	    		}
	    		
	    	}
		
		List<JSONObject> list = null;
		if(gids.size()>0){
			params.put("gids", gids);
			list = csCurCommonDataDao.getSchoolStudentList(params);
		}
		List<Account> aList = new ArrayList<Account>();
		List<String> studentIds = new ArrayList<String>();
		if(CollectionUtils.isNotEmpty(list)){
			for(JSONObject json:list){
				long accountId = json.getLongValue("accountId");
				if(!studentIds.contains(accountId+"")){
					studentIds.add(accountId+"");
				}
				Account account = new Account();
				account.setId(accountId);
				account.setName(json.getString("name"));
				List<User> lu = new ArrayList<User>();
				User user = new User();
				UserPart up = new UserPart();
				StudentPart sp = new StudentPart();
				up.setId(json.getLongValue("userId"));
				up.setAccountId(accountId);
				up.setRole(T_Role.Student);
				sp.setId(json.getLongValue("userId"));
				sp.setSchoolNumber(json.getString("schoolNumber"));
				sp.setStdNumber(json.getString("stdNumber"));
				sp.setClassId(json.getLongValue("classId"));
				user.setStudentPart(sp);
				user.setUserPart(up);
				lu.add(user);
				account.setUsers(lu);
				user.setAccountPart(Util.deepCopy(account));
				aList.add(account);
			}
		}
		//获取学生的身份证号
		if(studentIds.size()>0){
			JSONObject json = new JSONObject();
			json.put("studentIds", studentIds);
			json.put("termInfoId", termInfoId);
			List<JSONObject> accountIdList = csCurCommonDataDao.getAccountByIds(json);
			Map<String,String> idNumberMap = new HashMap<String,String>();
			for(JSONObject a:accountIdList){
				String idNumber = a.getString("idNumber");
				String accountId = a.getString("id");
				idNumberMap.put(accountId, idNumber);
			}
			for(int i =0  ; i<aList.size();i++){
				Account account = aList.get(i);
				String accountId = account.getId()+"";
				String idNumber = idNumberMap.get(accountId);
				account.setIdNumber(idNumber);
			}
		}
		return aList;
	}
	/**
	 * 获取学生集合
	 * @param params
	 * @return
	 * author wxq
	 */
	public List<Account> getStudentList(JSONObject params) {
		String termInfoId = params.getString("termInfoId");
		List<JSONObject> list = csCurCommonDataDao.getStudentList(params);
		List<Account> aList = new ArrayList<Account>();
		List<String> studentIds = new ArrayList<String>();
		if(CollectionUtils.isNotEmpty(list)){
			for(JSONObject json:list){
				long accountId = json.getLongValue("accountId");
				if(!studentIds.contains(accountId+"")){
					studentIds.add(accountId+"");
				}
				Account account = new Account();
				account.setId(accountId);
				account.setName(json.getString("name"));
				/*account.setAccount(json.getString("accountName"));
				T_AccountStatus status =T_AccountStatus.findByValue(json.getIntValue("accountStatus"));
				account.setAccountStatus(status);*/
				List<User> lu = new ArrayList<User>();
				User user = new User();
				UserPart up = new UserPart();
				StudentPart sp = new StudentPart();
				up.setId(json.getLongValue("userId"));
				up.setAccountId(accountId);
				up.setRole(T_Role.Student);
				sp.setId(json.getLongValue("userId"));
				sp.setSchoolNumber(json.getString("schoolNumber"));
				sp.setStdNumber(json.getString("stdNumber"));
				sp.setClassId(json.getLongValue("classId"));
				user.setStudentPart(sp);
				user.setUserPart(up);
				lu.add(user);
				account.setUsers(lu);
				user.setAccountPart(Util.deepCopy(account));
				aList.add(account);
			}
			
		}
		//获取学生的身份证号
		if(studentIds.size()>0){
			JSONObject json = new JSONObject();
			json.put("studentIds", studentIds);
			json.put("termInfoId", termInfoId);
			List<JSONObject> accountIdList = csCurCommonDataDao.getAccountByIds(json);
			Map<String,String> idNumberMap = new HashMap<String,String>();
			for(JSONObject a:accountIdList){
				String idNumber = a.getString("idNumber");
				String accountId = a.getString("id");
				idNumberMap.put(accountId, idNumber);
			}
			for(int i =0  ; i<aList.size();i++){
				Account account = aList.get(i);
				String accountId = account.getId()+"";
				String idNumber = idNumberMap.get(accountId);
				account.setIdNumber(idNumber);
			}
		}
		return aList;
	}
	/**
	 * 获取学校机构信息
	 * @param param
	 * @update 添加机构相关scope的封装  2016.7.30 BY:zhh
	 * @return List<OrgInfo>
	 */
	public List<OrgInfo> getSchoolOrgList(JSONObject param) {
		List<JSONObject> gList = csCurCommonDataDao.getSchoolOrgList(param);
		
		Map<String,String> accountHeaderMap = new HashMap<String,String>();
		List<JSONObject> oList = csCurCommonDataDao.getTeacherHeaderAccount(param);
		for(JSONObject o:oList){
			accountHeaderMap.put(o.getString("orgId"),o.getString("headerIds"));
		}
		List<OrgInfo> orgList = new ArrayList<OrgInfo>();
		//获取老师
		/*JSONObject obj = new JSONObject();
		List<JSONObject> gradeList = null;
		List<JSONObject> researchList = null;
		List<JSONObject> preparationList = null;
		Map<Long,List<Long>> orgMap =  new HashMap<Long,List<Long>>();
		try {
			obj.put("schoolId", param.getString("schoolId"));
			obj.put("xn", param.getString("termInfoId").substring(0, 4));
			obj.put("orgType", T_OrgType.T_Grade.getValue());//年级组
			obj.put("termInfoId", param.getString("termInfoId"));
			gradeList = orgService.getGradegroupList(obj);
			obj.put("orgType", T_OrgType.T_Teach.getValue());//教研组
			researchList = orgService.getResearchgroupList(obj);
			obj.put("orgType", T_OrgType.T_PreLesson.getValue());//备课组
			preparationList = orgService.getPreparationList(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(researchList!=null){
			gradeList.addAll(researchList);
		}
		if(preparationList!=null){
			gradeList.addAll(preparationList);
		}
		if(gradeList!=null){
			for(JSONObject grade:gradeList){
				List<Long> memberIds = (List<Long>) grade.get("memberIds");
				orgMap.put(grade.getLongValue("orgId"), memberIds);
			}
			
		}*/
		if(gList!=null){
			for(JSONObject object : gList){
				OrgInfo orgInfo = new OrgInfo();
				List<Long> memberIdList = new ArrayList<Long>();
				List<Long> headerIdList = new ArrayList<Long>();
				long orgId = object.getLongValue("orgId");
				int orgType = object.getIntValue("orgType");
				List<T_GradeLevel> sList = new ArrayList<T_GradeLevel>();
				List<Long> lList = new ArrayList<Long>();
				/** -----机构名称----- */
				orgInfo.setOrgName(object.getString("orgName"));
				/** -----机构Id----- */
	            orgInfo.setId(orgId);
	            /** -----机构类型----- */
	            orgInfo.setOrgType(orgType);
	            /** -----机构headerIds----- */
	           /* String headerIds = object.getString("headerIds");
	            if (StringUtils.isNotEmpty(headerIds)){
	            	headerIdList = StringUtil.toListFromString(headerIds);          	
	            }  
	            orgInfo.setHeaderAccountIds(headerIdList);*/
	            if(accountHeaderMap.containsKey(orgId+"")){
	            	String headerIds = accountHeaderMap.get(orgId+"");
	            	 if (StringUtils.isNotEmpty(headerIds)){
	                 	headerIdList = StringUtil.toListFromString(headerIds);          	
	                 } 
	            	 orgInfo.setHeaderAccountIds(headerIdList);
	            }
	            /** -----机构memberIds----- */
	            //if(orgType==T_OrgType.T_Depart.getValue()){
	            	String memberIds = object.getString("memberIds");
	            	if (StringUtils.isNotEmpty(memberIds)){
	 	            	memberIdList = StringUtil.toListFromString(memberIds);
	 	            } 
	            	for(Long hId:headerIdList){
		            	if(!memberIdList.contains(hId)){
		            		memberIdList.add(hId);
		            	}
	            	}
	            
	            /*}else{
	            	memberIdList = orgMap.get(orgId);
	            }*/
	            orgInfo.setMemberAccountIds(memberIdList);
	            /*机构范围scopes*/
	            String scopeIds = object.getString("scopeIds");
	            if(StringUtils.isNotBlank(scopeIds)){
	            	//得到当前机构的管辖年级 currentLevel
	            	List<String> scopeIdList = Arrays.asList(scopeIds.split(","));
					for(int i=0;i<scopeIdList.size();i++){
	            		sList.add(T_GradeLevel.findByValue(Integer.parseInt(scopeIdList.get(i))));
	            	}
	            }
	            orgInfo.setScopeTypes(sList);
	            /*机构科目*/
	            String lessonIds = object.getString("lessonIds");
	            if(StringUtils.isNotBlank(lessonIds)){
	            	//得到当前机构的管辖年级 currentLevel
	            	List<String> lessonIdList = Arrays.asList(lessonIds.split(","));
					for(int i=0;i<lessonIdList.size();i++){
						lList.add(Long.parseLong(lessonIdList.get(i)));
	            	}
	            }
	            orgInfo.setLessonIds(lList);
				orgList.add(orgInfo);
			}
		}
		
		return orgList;
	}
	/**
	 * 获取学校机构信息
	 * @param param
	 * @update 添加机构相关scope的封装  2016.7.30 BY:zhh
	 * @return OrgInfo
	 */
	public OrgInfo getSchoolOrgById(JSONObject param) {
		List<JSONObject> gList = csCurCommonDataDao.getSchoolOrgList(param);
		String schoolId = param.getString("schoolId");
		String termInfoId = param.getString("termInfoId");
		String orgId = param.getString("orgId");
		List<T_GradeLevel> sList = new ArrayList<T_GradeLevel>();
		OrgInfo orgInfo = null;
		
		if (gList!=null && gList.size() > 0){
			orgInfo = new OrgInfo();
			List<Long> headerIdList = new ArrayList<Long>();
			JSONObject object = gList.get(0);
			/** -----机构名称----- */
			orgInfo.setOrgName(object.getString("orgName"));
			/** -----机构Id----- */
            orgInfo.setId(object.getLongValue("orgId"));
            /** -----机构类型----- */
            orgInfo.setOrgType(object.getIntValue("orgType"));
            JSONObject obj = new JSONObject();
            obj.put("schoolId", schoolId);
			obj.put("ids", Arrays.asList(orgId.split(",")));
			obj.put("name", "");
			obj.put("termInfoId", termInfoId);
			obj.put("xn", termInfoId.substring(0,4));
			List<String> tIds = new ArrayList<String>();
			if(object.getIntValue("orgType")==2){
				tIds.add(T_OrgType.T_Grade.getValue()+"");
			}else if(object.getIntValue("orgType")==1){
				tIds.add(T_OrgType.T_Teach.getValue()+"");
			}else if(object.getIntValue("orgType")==3){
				tIds.add(T_OrgType.T_PreLesson.getValue()+"");
			}else if(object.getIntValue("orgType")==6){
				tIds.add(T_OrgType.T_Depart.getValue()+"");
			}
			obj.put("tIds", tIds);
            List<Account>  aList = this.getOrgTeacherListAllType(obj);
     
            /** -----机构headerIds----- */
            String headerIds = object.getString("headerIds");
            if (StringUtils.isNotEmpty(headerIds)){
            	headerIdList = StringUtil.toListFromString(headerIds);          	
            }  
            orgInfo.setHeaderAccountIds(headerIdList);
            /** -----机构memberIds----- */
            List<Long> memberIdList = new ArrayList<Long>(headerIdList);
            if(aList!=null){
            	for(Account a :aList){
            		long id=a.getId();
            		if(!memberIdList.contains(id)){
            			memberIdList.add(id);
            		}
            	}
            }
            /*String memberIds = object.getString("memberIds");
            if (StringUtils.isNotEmpty(memberIds)){
            	memberIdList = StringUtil.toListFromString(memberIds);
            } */
            orgInfo.setMemberAccountIds(memberIdList);
            /*机构范围scopes*/
            String scopeIds = object.getString("scopeIds");
            if(StringUtils.isNotBlank(scopeIds)){
            	//得到当前机构的管辖年级 currentLevel
            	List<String> scopeIdList = Arrays.asList(scopeIds.split(","));
				for(int i=0;i<scopeIdList.size();i++){
            		sList.add(T_GradeLevel.findByValue(Integer.parseInt(scopeIdList.get(i))));
            	}
            }
            orgInfo.setScopeTypes(sList);
		}
		return orgInfo;
	}
	/**
	 * 通过年级ID获取年级对象
	 * @param param
	 * @return
	 * @author zhh
	 */
	public Grade getGradeById(JSONObject param){
		//Long start= System.currentTimeMillis();
		Grade grade = new Grade();
		String termInfoId=param.getString("termInfoId");
		if(StringUtils.isBlank(termInfoId)){
			return null;
		}
		int xn = Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		param.put("xn", xn);
		List<JSONObject> gList=csCurCommonDataDao.getGradeListBySchoolId(param);
		if(gList!=null && gList.size()>0){
			JSONObject g=gList.get(0);
			grade.setId(g.getLongValue("id"));
			if(StringUtils.isNotBlank(g.getString("classIds"))){
				String [] str=g.getString("classIds").split(",");
				//NumberSort.halfSortBySx(str);
				grade.setClassIds(stringArrToListLong(str));
			}
			//获取currentLevel
			int createLevel=g.getInteger("createLevel");
			int currentLevel=g.getInteger("currentLevel");
			grade.setCreateLevel(T_GradeLevel.findByValue(createLevel));
			grade.setCurrentLevel(T_GradeLevel.findByValue(currentLevel));
			//获取isGraduate
			boolean isGraduate = isGraduate(createLevel, currentLevel);
			grade.setIsGraduate(isGraduate);
		}
		//logger.info("@@@@【getGradeById】耗时："+(System.currentTimeMillis()-start));
		return grade;
	}
	/**
	 * String [] 转换为 List Long 类型
	 * @param arr
	 * @return List Long
	 * @author zhh
	 */
	private List<Long> stringArrToListLong(String [] arr){
		List<Long> list = new ArrayList<Long>();
		for(int i =0;i<arr.length;i++){
			String s=arr[i];
			long l = Long.parseLong(s);
			list.add(l);
		}
		return list; 
	}
	/**
	 * 通过年级LEVEL获取年级对象(gradeLevel为currentLevel)
	 * @param param
	 * @return
	 * @author zhh
	 */
	public Grade getGradeByGradeLevel(JSONObject param){
		//Long start = System.currentTimeMillis();
		Grade grade = new Grade();
		T_GradeLevel gradeLevel=(T_GradeLevel) param.get("gradeLevel");
		String termInfoId=param.getString("termInfoId");
		if(StringUtils.isBlank(termInfoId)){
			return null;
		}
		int xn = Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		//先查出该学校所有年级
		param.put("xn", xn);
		List<JSONObject> gList=csCurCommonDataDao.getGradeListBySchoolId(param);
		if(gList!=null && gList.size()>0){
			for(JSONObject g:gList){
				
				//获取currentLevel
				int createLevel=g.getInteger("createLevel");
				int currentLevel=g.getInteger("currentLevel");
				
				//根据currentLevel过滤该校年级
				if(gradeLevel!=null && gradeLevel.getValue()==currentLevel){
					grade.setId(g.getLongValue("id"));
					if(StringUtils.isNotBlank(g.getString("classIds"))){
						String [] str=g.getString("classIds").split(",");
						//NumberSort.halfSortBySx(str);
						grade.setClassIds(stringArrToListLong(str));
					}
					grade.setCreateLevel(T_GradeLevel.findByValue(createLevel));
					grade.setCurrentLevel(T_GradeLevel.findByValue(currentLevel));
					//获取isGraduate
					boolean isGraduate = isGraduate(createLevel, currentLevel);
					grade.setIsGraduate(isGraduate);
					if(!isGraduate){
						break;
					}else{
						grade = new Grade();
					}
				}
			}
		}
		//logger.info("@@@@【getGradeByGradeLevel】耗时："+(System.currentTimeMillis()-start));
		return grade;
	}
	/**
	 * 通过多个年级LEVEL获取年级对象(gradeLevel为currentLevel)
	 * 过滤已毕业年级
	 * @param param
	 * @return
	 * @author zhh
	 */
	public List<Grade> getGradeByGradeLevelBatch(JSONObject param){
		//Long start = System.currentTimeMillis();
		List<Grade> gradeList= new ArrayList<Grade>();
		List<T_GradeLevel> gradeLevels=(List<T_GradeLevel>) param.get("gradeLevels");
		List<Integer> gradeLevelList=new ArrayList<Integer>();
		if(gradeLevels!=null){
			for(T_GradeLevel gl: gradeLevels){
				gradeLevelList.add(gl.getValue());
			}
		}
		String termInfoId=param.getString("termInfoId");
		if(StringUtils.isBlank(termInfoId)){
			return null;
		}
		int xn = Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		//先查出该学校所有年级
		param.put("xn", xn);
		List<JSONObject> gList=csCurCommonDataDao.getGradeListBySchoolId(param);
		if(gList!=null && gList.size()>0){
			for(JSONObject g:gList){
				
				//获取currentLevel
				int createLevel=g.getInteger("createLevel");
				int currentLevel=g.getInteger("currentLevel");
				//根据currentLevel过滤该校年级
				if(gradeLevelList.contains(currentLevel)){
					Grade grade=new Grade();
					grade.setId(g.getLongValue("id"));
					if(StringUtils.isNotBlank(g.getString("classIds"))){
						String [] str=g.getString("classIds").split(",");
						//NumberSort.halfSortBySx(str);
						grade.setClassIds(stringArrToListLong(str));
					}
					grade.setCreateLevel(T_GradeLevel.findByValue(createLevel));
					grade.setCurrentLevel(T_GradeLevel.findByValue(currentLevel));
					//获取isGraduate
					boolean isGraduate = isGraduate(createLevel, currentLevel);
					if(!isGraduate){
						grade.setIsGraduate(isGraduate);
						gradeList.add(grade);
					}
					
				}
			}
		}
		//logger.info("@@@@【getGradeByGradeLevelBatch】耗时："+(System.currentTimeMillis()-start));
		return gradeList;
	}
	/**
	 *@name getUserBatch
	 *@date 2016年5月10日 
	 *@version 版本
	 *@author zhh
	 */
	public List<User> getUserBatch(long schoolId, List<Long> ids,String termInfoId) {
		List<User> users= new ArrayList<User>();
		Map<Long,User> userMap = new HashMap<Long, User>(); 
		if(null==ids||ids.isEmpty())
		{
			return users;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("userIds", ids);
		jsonObject.put("termInfoId", termInfoId);
		List<Long> teacherIds = new ArrayList<Long>();
		List<Long> parentIds = new ArrayList<Long>();
		List<Long> studentIds = new ArrayList<Long>();
		List<Long> staffIds = new ArrayList<Long>();
		List<Long> schoolIds = new ArrayList<Long>();
		List<JSONObject> userBatch = csCurCommonDataDao.getUserBatchById(jsonObject);
		if(CollectionUtils.isNotEmpty(userBatch)){
			for(JSONObject userObj:userBatch){
				Long userId = userObj.getLongValue("id");
				Long accountId = userObj.getLongValue("accountId");
				String idNumber = userObj.getString("idNumber");
				
				User user= new User();
				user.setExtId(userObj.getString("extAccountId"));
				Account accountPart = new Account(); 
				UserPart userPart = new UserPart();
				accountPart.setExtId(userObj.getString("extUserId"));
				userPart.setId(userId);
				userPart.setAccountId(accountId);
				userPart.setRole(T_Role.findByValue(userObj.getIntValue("role")));
				user.setUserPart(userPart);
				accountPart.setIdNumber(idNumber);
				accountPart.setName(userObj.getString("name"));
				accountPart.setId(accountId);
				accountPart.setAccount(userObj.getString("accountName"));
				user.setAccountPart(accountPart);
				//users.add(user);
				userMap.put(userId, user);
				int role=userObj.getIntValue("role");
				T_Role tRole = T_Role.findByValue(role);
				switch (tRole) {
					case Teacher://教师 
						teacherIds.add(userId);
						break;
					case Student://学生
						studentIds.add(userId);
						break;
					case Parent://家长
						parentIds.add(userId);
						break;
					case Staff://学校人员
						staffIds.add(userId);
						break;
					case SchoolManager://学校管理人员
						schoolIds.add(userId);
						break;
					default:
						break;
				}
			
			} //end of for userBatch 
			//老师代码块
			if(CollectionUtils.isNotEmpty(teacherIds) && teacherIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", teacherIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> teacherRoles = csCurCommonDataDao.queryTeacherRoleByUserId(jsonObject2);
				if(CollectionUtils.isNotEmpty(teacherRoles) && teacherIds.size() > 0){
					for (JSONObject jsonObject3 : teacherRoles) {
						Long teacherId = jsonObject3.getLongValue("teacherId");
						TeacherPart tp = new TeacherPart();
						tp.setId(teacherId);
						tp.setEmpno(jsonObject3.getString("empno"));
					    tp.setDeanOfClassIds(StringUtil.toListFromString(jsonObject3.getString("deanOfClassIds")));
					    User user = userMap.get(teacherId);
					    if(null!=user){
					    	user.setTeacherPart(tp);
					    }
					}
				}
			}
			//学生代码块
			if(CollectionUtils.isNotEmpty(studentIds) && studentIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", studentIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> studentList = csCurCommonDataDao.queryStudentListByUserId(jsonObject2);
				if(studentList!=null){
					for (JSONObject jsonObject3 : studentList) {
						StudentPart studentPart = new StudentPart();
						Long id = jsonObject3.getLongValue("id");
						studentPart.setId(id);
						if(null!=jsonObject3.get("parentId")&&StringUtils.isNotEmpty(jsonObject3.getString("parentId")))
						{
							studentPart.setParentId(jsonObject3.getLongValue("parentId"));
						}
						studentPart.setClassId(jsonObject3.getLongValue("classId"));
						studentPart.setSchoolNumber(jsonObject3.getString("schoolNumber"));
						studentPart.setStdNumber(jsonObject3.getString("stdNumber"));
						User user = userMap.get(id);
						if(null!=user){
						    	user.setStudentPart(studentPart);
						}
					}
				}
			}
			//家长代码模块
			if(CollectionUtils.isNotEmpty(parentIds) && parentIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", parentIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> parentList = csCurCommonDataDao.queryParentListByUserId(jsonObject2);
				//通过sIds返回学生的姓名 studentName
				List<Long> sIds= new ArrayList<Long>();
				Map<Long,String> studentIdNameMap = new  HashMap<Long, String>(); //studentId-accountName
				if(parentList!=null){
					for(JSONObject parentObj:parentList){
						sIds.add(parentObj.getLongValue("studentId"));
					}
				}
				JSONObject jsonObject22 = new JSONObject();
				if(sIds.size()>0){
					jsonObject22.put("userIds", sIds);
					jsonObject22.put("schoolId", schoolId);
					jsonObject22.put("termInfoId", termInfoId);
					List<JSONObject> studentList = csCurCommonDataDao.queryStudentNameListByUserId(jsonObject22);
					if(studentList!=null){
						for(JSONObject studentObj:studentList){
							Long studentId = studentObj.getLong("studentId");
							String studentName = studentObj.getString("studentName");
							if(studentId!=null){
								studentIdNameMap.put(studentId, studentName);
							}
						}
					}
				}
				if(parentList!=null){
					for (JSONObject jsonObject3 : parentList) {
						ParentPart parentPart = new ParentPart();
						Long parentId = jsonObject3.getLongValue("parentId");
						parentPart.setId(parentId);
						Long studentId=jsonObject3.getLongValue("studentId");
						String studentName="";
						if(studentId!=null){
							studentName = studentIdNameMap.get(studentId);
							parentPart.setStudentName(studentName);
						}
						parentPart.setStudentId(studentId);
						parentPart.setClassId(jsonObject3.getLongValue("classId"));
						User user = userMap.get(parentId);
						if(null!=user){
						    	user.setParentPart(parentPart);
						}
					}
				}
			}
			//学校工作人员代码块
			if(CollectionUtils.isNotEmpty(staffIds) && staffIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", staffIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> staffRoles = csCurCommonDataDao.queryStaffListByUserId(jsonObject2);
				if(CollectionUtils.isNotEmpty(staffRoles) && teacherIds.size() > 0){
					for (JSONObject jsonObject3 : staffRoles) {
						StaffPart tp = new StaffPart();
						Long staffId = jsonObject3.getLongValue("staffId");
						tp.setId(staffId);
					    tp.setEmpno(jsonObject3.getString("empno"));
					    User user = userMap.get(staffId);
						if(null!=user){
						    	user.setStaffPart(tp);
						}
					}
				}
			}
			
			//学校管理员代码块
			if(CollectionUtils.isNotEmpty(schoolIds) && schoolIds.size() > 0){
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("userIds", schoolIds);
				jsonObject2.put("schoolId", schoolId);
				jsonObject2.put("termInfoId", termInfoId);
				List<JSONObject> managerRoles = csCurCommonDataDao.queryManagerListByUserId(jsonObject2);
				if(CollectionUtils.isNotEmpty(managerRoles) && managerRoles.size() > 0){
					for (JSONObject jsonObject3 : managerRoles) {
						SchoolManagerPart tp = new SchoolManagerPart();
						Long managerId = jsonObject3.getLongValue("managerId");
						tp.setId(managerId);
						tp.setEmpno(jsonObject3.getString("empno"));
						  User user = userMap.get(managerId);
							if(null!=user){
							    	user.setSchoolManagerPart(tp);
							}
					}
				}
			}
			for (Map.Entry<Long, User> entry : userMap.entrySet()) {
//				   System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
				User user=entry.getValue();
				users.add(user);
			}
		}//end of userBatch null
		
		return users;
	}
	/**
	 * 获取帐号完整信息，包括userPart
	 * @param params(schoolId可选,accountId)
	 * @return
	 * author wxq
	 */
	public Account getAccountAllById(JSONObject params) {
		Account account = new Account();
		String termInfoId = params.getString("termInfoId");
		JSONObject json = csCurCommonDataDao.getBaseAccountUserIds(params);
		if(json==null){return account;}
		account.setIdNumber(json.getString("idNumber"));
		account.setId(json.getLongValue("id"));
		account.setAccount(json.getString("accountName"));
		T_AccountStatus status = T_AccountStatus.findByValue(json.getIntValue("accountStatus"));
		account.setAccountStatus(status);
		account.setName(json.getString("name"));
		account.setMobilePhone(json.getString("mobilePhone"));
		List<JSONObject> userRole = csCurCommonDataDao.getUserIdsAndRole(json.getLongValue("id"),termInfoId);
		List<Long> userIds = new ArrayList<Long>();
		List<User> l_user = new ArrayList<User>();
		if(userRole!=null){
			for(JSONObject object:userRole){
				userIds.add(object.getLongValue("id"));
				User user = new User();
				UserPart up = new UserPart();
				Account ap = new Account();
				ap.setId(json.getLongValue("id"));
				ap.setName(json.getString("name"));
				user.setAccountPart(ap);
				JSONObject obj;
				int role = object.getIntValue("role");
				T_Role tRole = T_Role.findByValue(role);
				switch (tRole) {
				case Parent://家长
					up.setRole(T_Role.Parent);
					up.setId(object.getLongValue("id"));
					break;
				case Teacher://教师 
					up.setRole(T_Role.Teacher);
					up.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getTeacherRole(object.getLong("id"),termInfoId);
					if(obj!=null&&obj.getString("orgIds")!=null){
						List<Long> orgIds = StringUtil.toListFromString(obj.getString("orgIds"));
						up.setOrgIds(orgIds);
					}
					TeacherPart tp = new TeacherPart();
					tp.setId(object.getLongValue("id"));
					List<Course> lc = new ArrayList<Course>();
					List<Long> courseIds = new ArrayList<Long>();
					List<Long> classIds = new ArrayList<Long>();
					if(obj!=null && obj.getString("lessonIds")!=null){
						courseIds = StringUtil.toListFromString(obj.getString("lessonIds"));
					}
					if(obj!=null && obj.getString("classIds")!=null){
						classIds = StringUtil.toListFromString(obj.getString("classIds"));
					}
					for(int i=0;i<courseIds.size();i++){
						Course course = new Course();
						course.setLessonId(courseIds.get(i));
						course.setClassId(classIds.get(i));
						lc.add(course);
					}
					List<JSONObject> deanOfClassList = csCurCommonDataDao.getTeacherDeanOfClassIds(object.getLong("id"),termInfoId);
					List<Long> deanOfClassIds = new ArrayList<Long>();
					if(deanOfClassList!=null){
						for(JSONObject deanOfClassIdsObj:deanOfClassList){
							deanOfClassIds.add(deanOfClassIdsObj.getLong("classId"));
						}
					}
					tp.setDeanOfClassIds(deanOfClassIds);
					tp.setCourseIds(lc);
					user.setUserPart(up);
					user.setTeacherPart(tp);
					l_user.add(user);
					break;
				case Student:
					up.setRole(T_Role.Student);
					up.setId(object.getLongValue("id"));
					break;
				case Staff:
					up.setRole(T_Role.Staff);
					up.setId(object.getLongValue("id"));
					StaffPart sp = new StaffPart();
					sp.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getStaffRole(object.getLong("id"),termInfoId);
					if(obj!=null){
						if(obj.getString("orgIds")!=null){
							List<Long> orgIds1 = StringUtil.toListFromString(obj.getString("orgIds"));
							up.setOrgIds(orgIds1);
						}
						sp.setJobType(obj.getLongValue("jobType"));
						sp.setJobName(obj.getString("jobTypeName"));
						sp.setSchoolId(obj.getLongValue("schoolId"));
					}
					user.setUserPart(up);
					user.setStaffPart(sp);
					l_user.add(user);
					break;
				case SchoolManager:
					up.setRole(T_Role.SchoolManager);
					up.setId(object.getLongValue("id"));
					SchoolManagerPart smp = new SchoolManagerPart();
					smp.setId(object.getLongValue("id"));
					obj = csCurCommonDataDao.getSchoolManagerPartRole(object.getLong("id"),termInfoId);
					if(obj!=null){
						smp.setId(obj.getLongValue("managerId"));
						smp.setSchoolId(obj.getLongValue("schoolId"));
					}
					user.setUserPart(up);
					user.setSchoolManagerPart(smp);
					l_user.add(user);
					break;
				case PlateManager:
					break;
				case SystemManager:
					break;
				default:
					break;
				}
			}
		}
		account.setUserIds(userIds);
		account.setUsers(l_user);
		return account;
	}
	/**
	 * 获取Account的id和账号名称
	 * @param params
	 * @return
	 * author wxq
	 */
	public Account getAccountById(JSONObject params) {
		JSONObject result = csCurCommonDataDao.getAccountById(params);
		Account acc = new Account();
		if(result!=null){
			acc.setIdNumber(result.getString("idNumber"));
			acc.setId(result.getLongValue("id"));
			acc.setAccount(result.getString("accountName"));
			T_AccountStatus status = T_AccountStatus.findByValue(result.getIntValue("accountStatus"));
			acc.setAccountStatus(status);
			acc.setName(result.getString("name"));
			T_Gender gender = T_Gender.findByValue(result.getIntValue("gender"));
			acc.setGender(gender);
			acc.setExtId(result.getString("extId"));
		}
		return acc;
	}
	
	/**
	 * 得到全校所有学生信息
	 * @update 若该学生所在年级已毕业或者为幼儿园，则不返回该学生）
	 * @param schoolId
	 * @return
	 * author wxq
	 */
	public List<Account> getAllStudent(long schoolId,String termInfoId) {
		//判断年级是否已毕业
		//判断当前年级是否已毕业
		Long s=System.currentTimeMillis();
		int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		//根据学校ID获取年级列表
	    JSONObject param = new JSONObject();
	    param.put("schoolId", schoolId);
	    param.put("xn",xn);
	    param.put("termInfoId",termInfoId);
		List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
		Map<Long,Boolean> isGraduateMap = new HashMap<Long, Boolean>(); //createLevel-- isGraduate
		if(gradeList!=null){
			for(JSONObject g:gradeList){
				//获取currentLevel
				int  currentLevel = g.getInteger("currentLevel");
				int  createLevel = g.getInteger("createLevel");
		
				//获取isGraduate
				boolean isGraduate = isGraduate(createLevel, currentLevel);
				//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
					isGraduateMap.put(g.getLong("id"), isGraduate);
				//}
			}
		}
	    //转换 使用年级转 createLevel
	    List<Long> gids = new ArrayList<Long>();
	    for(Map.Entry<Long, Boolean> entry : isGraduateMap.entrySet()){
	    	if(!(boolean)entry.getValue()){
	    			gids.add(entry.getKey());
	    	}
	    		
	    }
		System.out.println("@@@@@@@@@@times:"+(System.currentTimeMillis()-s));
		JSONObject paramObj = new JSONObject();
		paramObj.put("schoolId", schoolId);
		paramObj.put("ids", gids);
		paramObj.put("termInfoId", termInfoId);
		List<JSONObject> list = new ArrayList<JSONObject>();
		List<Account> aList = new ArrayList<>();
		if(schoolId!=0){
			list = csCurCommonDataDao.getAllStudent(paramObj);
		}
		List<String> studentIds = new ArrayList<String>();
		if(null!=list && list.size()>0)
		{
			for(JSONObject json:list)
			{
				long accountId = json.getLongValue("accountId");
				Account account = new Account();
				if(!studentIds.contains(accountId+"")){
					studentIds.add(accountId+"");
				}
				account.setId(accountId);
				account.setName(json.getString("name"));
				/*account.setAccount(json.getString("accountName"));
				T_AccountStatus status =T_AccountStatus.findByValue(json.getIntValue("accountStatus"));
				account.setAccountStatus(status);*/
				List<User> lu = new ArrayList<User>();
				User user = new User();
				UserPart up = new UserPart();
				StudentPart sp = new StudentPart();
				up.setId(json.getLongValue("userId"));
				up.setAccountId(accountId);
				up.setRole(T_Role.Student);
				sp.setId(json.getLongValue("userId"));
				sp.setSchoolNumber(json.getString("schoolNumber"));
				sp.setStdNumber(json.getString("stdNumber"));
				sp.setClassId(json.getLongValue("classId"));
				user.setStudentPart(sp);
				user.setUserPart(up);
				lu.add(user);
				account.setUsers(lu);
				user.setAccountPart(Util.deepCopy(account));
				aList.add(account);
			}
		}
		//获取学生的身份证号
		if(studentIds.size()>0){
			JSONObject json = new JSONObject();
			json.put("studentIds", studentIds);
			json.put("termInfoId", termInfoId);
			List<JSONObject> accountIdList = csCurCommonDataDao.getAccountByIds(json);
			Map<String,String> idNumberMap = new HashMap<String,String>();
			for(JSONObject a:accountIdList){
				String idNumber = a.getString("idNumber");
				String accountId = a.getString("id");
				idNumberMap.put(accountId, idNumber);
			}
			for(int i =0  ; i<aList.size();i++){
				Account account = aList.get(i);
				String accountId = account.getId()+"";
				String idNumber = idNumberMap.get(accountId);
				account.setIdNumber(idNumber);
			}
		}
		return aList;
	}
	/**
	 * 得到年级下的所有班级
	 * @update （判断该学生是否已毕业获取是否为幼儿园，若是则不返回该学生）By:zhh
	 * @param gradeIds
	 * @return
	 * author wxq
	 */
	public List<Classroom> getAllClass(List<Long> gradeIds,String termInfoId,long schoolId) {
		List<Classroom> aList = new ArrayList<>();
		List<JSONObject> list= new ArrayList<JSONObject>();
		if(StringUtils.isBlank(termInfoId)||schoolId==0||gradeIds==null){
			return aList;
		}
		//判断当前年级是否已毕业
	    int xn=Integer.parseInt(termInfoId.substring(0, termInfoId.length()-1));
		//根据学校ID获取年级列表
	    JSONObject param = new JSONObject();
	    param.put("schoolId", schoolId);
	    param.put("xn",xn);
	    param.put("termInfoId",termInfoId);
		List<JSONObject> gradeList = csCurCommonDataDao.getGradeListBySchoolId(param);
		Map<Long,Boolean> isGraduateMap = new HashMap<Long, Boolean>(); //createLevel-- isGraduate
		if(gradeList!=null){
			for(JSONObject g:gradeList){
				//获取currentLevel
				int  currentLevel = g.getInteger("currentLevel");
				int  createLevel = g.getInteger("createLevel");
		
				//获取isGraduate
				boolean isGraduate = isGraduate(createLevel, currentLevel);
				//if(currentLevel>=T_GradeLevel.T_PrimaryOne.getValue()){
					isGraduateMap.put(g.getLong("id"), isGraduate);
				//}
			}
		}
	    //转换 使用年级转 createLevel
	    List<Long> gids = new ArrayList<Long>();
	    if(gradeIds!=null){
		    for(long gid:gradeIds){
		    	//判断传入的年级是否已毕业(已毕业的班级不返回)
		    	if(isGraduateMap.containsKey(gid)){
		    		if(!(Boolean)isGraduateMap.get(gid)){
		    			gids.add(gid);
		    		}
		    	}
		    }
		   
	    }
	    if(gids.size()>0){
	     list = csCurCommonDataDao.getAllClass(gids,termInfoId);
	    }
		if(list!=null){
			for(JSONObject json:list){
				Classroom cr = new Classroom();
				cr.setId(json.getLongValue("id"));
				cr.setClassName(json.getString("className"));
				T_ClassType type = T_ClassType.findByValue(json.getIntValue("classType"));
				cr.setClassType(type);
				cr.setGradeId(json.getLongValue("gradeId"));
				aList.add(cr);
			}
		}
		return aList;
	}
	/**
	 * 通过班级ID得到班级
	 * @param schoolId 学校ID
	 * @param gradeId
	 * @return
	 */
	public Classroom getClassById(long schoolId, long classId,String termInfoId) {
		List<Long> ids = new ArrayList<Long>();
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		ids.add(classId);
		param.put("ids", ids);
		List<Classroom> cList = this.getClassroomBatch(param);
		if(cList==null){
			return  null;
		}
		/*Classroom classroom = new Classroom();
		if(StringUtils.isBlank(termInfoId)||schoolId==0||classId==0){
			return classroom;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("classId", classId);
		jsonObject.put("termInfoId", termInfoId);
		JSONObject classInfo = csCurCommonDataDao.getClassById(jsonObject);
		List<JSONObject> ClassStu = csCurCommonDataDao.getClassByStudent(jsonObject);
		//a.id,a.gradeId,a.classNo,a.className,a.createTime,a.USER_BH,a.classType,a.educationalSys,a.headTeacherId as teacherId
		if(classInfo==null||!classInfo.containsKey("id")||!classInfo.containsKey("gradeId")){
			return null;
		}
		classroom.setId(classInfo.getLongValue("id"));
		classroom.setGradeId(classInfo.getLongValue("gradeId"));
		classroom.setClassNo(classInfo.getLongValue("classNo"));
		classroom.setClassName(classInfo.getString("className"));
		classroom.setCreateTime(classInfo.getLongValue("createTime"));
		classroom.setUSER_BH(classInfo.getString("USER_BH"));
		classroom.setClassType(T_ClassType.findByValue(classInfo.getIntValue("classType")));
		//classroom.setEducationalSys(classInfo.getIntValue("educationalSys"));
		classroom.setTeacherId(classInfo.getLongValue("teacherId"));
		List<Long> parentIds = new ArrayList<Long>();
		List<Long> studentIds = new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(ClassStu)){
			for (JSONObject jsonObject2 : ClassStu) {
				parentIds.add(jsonObject2.getLongValue("parentId"));
				studentIds.add(jsonObject2.getLongValue("studentId"));
			}
		}
		classroom.setParentIds(parentIds);
		classroom.setStudentIds(studentIds);*/
		return cList.get(0);
	}
	/**
	 *@see 创建的原因 
	 *@date 2016年1月14日 下午4:03:56
	 *@version 版本
	 *@author liboqi
	 */
	public User getUserById(long schoolId, long userId, String termInfoId) {
		User user = new User();
		if(schoolId==0||userId==0||StringUtils.isBlank(termInfoId)){
			return user;
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("userId", userId);
		jsonObject.put("termInfoId", termInfoId);
		JSONObject userInfo = csCurCommonDataDao.getUserById(jsonObject);
		if(userInfo==null){return user;}
		//a.id,a.accountId,a.role,a.createTime,a.avatar
		UserPart userPart = new UserPart();
		userPart.setId(userInfo.getLongValue("id"));
		userPart.setAccountId(userInfo.getLongValue("accountId"));
		userPart.setRole(T_Role.findByValue(userInfo.getIntValue("role")));
		userPart.setCreateTime(userInfo.getLongValue("createTime"));
		userPart.setAvatar(userInfo.getString("avatar"));
		
		if("0".equals(userInfo.getString("role"))){
			JSONObject parent = csCurCommonDataDao.getParentByUserId(jsonObject);
			ParentPart parentPart = new ParentPart();
			if(parent!=null){
				parentPart.setId(parent.getLongValue("id"));
				parentPart.setStudentId(parent.getLongValue("studentId"));
				parentPart.setClassId(parent.getLongValue("classId"));
			}
			user.setParentPart(parentPart);
		}else if("1".equals(userInfo.getString("role"))){
			List<Long> orgIds = csCurCommonDataDao.getOrgListByUserId(jsonObject);
			userPart.setOrgIds(orgIds);
			TeacherPart teacherPart = new TeacherPart();
			teacherPart.setId(userInfo.getLongValue("id"));
			List<JSONObject> teacherCourseList = csCurCommonDataDao.getTeacherCourseListByUserId(jsonObject);
			List<Course> courses = new ArrayList<Course>();
			if(CollectionUtils.isNotEmpty(teacherCourseList)){
				for (JSONObject jsonObject2 : teacherCourseList) {
					Course course = new Course();
					course.setLessonId(jsonObject2.getLongValue("lessonId"));
					course.setClassId(jsonObject2.getLongValue("classId"));
					courses.add(course);
				}
			}
			teacherPart.setCourseIds(courses);
			teacherPart.setSchoolId(schoolId);
			user.setTeacherPart(teacherPart);
		}else if("2".equals(userInfo.getString("role"))){
			jsonObject.put("accountId", userInfo.getLongValue("accountId"));
			JSONObject student = csCurCommonDataDao.getStudentByUserId(jsonObject);
			StudentPart studentPart = new StudentPart();
			if(student!=null){
				studentPart.setId(student.getLongValue("id"));
				studentPart.setParentId(student.getLongValue("parentId"));
				studentPart.setClassId(student.getLongValue("classId"));
			}
			user.setStudentPart(studentPart);
		}else if("3".equals(userInfo.getString("role"))){
			
		}else if("4".equals(userInfo.getString("role"))){
			SchoolManagerPart schoolManagerPart = new SchoolManagerPart();
			schoolManagerPart.setId(userInfo.getLongValue("id"));
			schoolManagerPart.setSchoolId(schoolId);
			user.setSchoolManagerPart(schoolManagerPart);
		}else if("5".equals(userInfo.getString("role"))){
			
		}else if("6".equals(userInfo.getString("role"))){
			
		}
		JSONObject accountObj = csCurCommonDataDao.getAccountByUserId(jsonObject);
		if(accountObj!=null){
			Account account = new Account();
			account.setIdNumber(accountObj.getString("idNumber"));
			account.setId(accountObj.getLongValue("id"));
			account.setAccount(accountObj.getString("account"));
			//account.setFirstLogin(accountObj.getLongValue("firstLogin"));
			//account.setLastLogin(accountObj.getLongValue("lastLogin"));
			//account.setDevice(accountObj.getIntValue("device"));
			account.setAccountStatus(T_AccountStatus.findByValue(accountObj.getIntValue("accountStatus")));
			ArrayList<Long> arrayList = new ArrayList<Long>();
			arrayList.add(userId);
			account.setUserIds(arrayList);
			account.setName(accountObj.getString("name"));
			account.setGender(T_Gender.findByValue(accountObj.getIntValue("gender")));
			//account.setIdNumber(accountObj.getString("idNumber"));
			account.setEmail(accountObj.getString("email"));
			user.setAccountPart(account);
			user.setUserPart(userPart);
		}
		return user;
	}
	
	 
	/** 阳光平台的extId转化为OMS中的userId 
	 * @param role */
	public List<JSONObject> getUserIdByExtId(String extId, String currentXnxq){
		JSONObject param = new JSONObject();
		param.put("extId" , extId);
		param.put("termInfoId", currentXnxq);
		return csCurCommonDataDao.getUserIdByExtId(param);
	}
	public List<Account> getOrgTeacherListIntersect(JSONObject obj) {
		List<Account> aList = new ArrayList<Account>();
		List<String> gids = (List<String>) obj.get("gids");
		List<String> rids = (List<String>) obj.get("rids");
		String termInfoId = obj.getString("termInfoId");
		String xn = obj.getString("xn");
		String schoolId = obj.getString("schoolId");
		obj.put("ids", gids);
		List<JSONObject> gdInfos = csCurCommonDataDao.getOrgInfos(obj);
		obj.put("ids", rids);
		List<JSONObject> rdInfos = csCurCommonDataDao.getOrgInfos(obj);
		List<Account> gaList = new ArrayList<Account>();
		List<Account> raList = new ArrayList<Account>();
		List<String> gidsList = new ArrayList<String>(); //返回结果去重
		List<String> ridsList = new ArrayList<String>(); //返回结果去重
		for(JSONObject dInfo:gdInfos){
			String name = dInfo.getString("name");
			Long accountId = dInfo.getLong("accountId");
			int type = dInfo.getIntValue("type");
			if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
				continue;
			}
			if(type==1){
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!gidsList.contains(accountId+"")){ //获取领导
					gaList.add(a);
					gidsList.add(accountId+"");
				}
			}
		} //end of for dInfos
		for(JSONObject dInfo:rdInfos){
			String name = dInfo.getString("name");
			Long accountId = dInfo.getLong("accountId");
			int type = dInfo.getIntValue("type");
			if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
				continue;
			}
			if(type==1){
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!ridsList.contains(accountId+"")){ //获取领导
					raList.add(a);
					ridsList.add(accountId+"");
				}
			}
		} //end of for dInfos
		//获取机构-年级数据
		Map<String,LinkedHashSet<String>> orgIdScopeIdsmap = new HashMap<String, LinkedHashSet<String>>();
		obj.put("ids", gids);
		List<JSONObject> osList = csCurCommonDataDao.getOrgScopeList(obj);
		List<String> removeGIdList = new ArrayList<String>();
		for(JSONObject os:osList){
			int currentLevel = os.getIntValue("currentLevel");
			Boolean isGraduate = isGraduate(os.getInteger("createLevel"), os.getInteger("currentLevel"));
			if(isGraduate){
				if(StringUtils.isNotBlank(os.getString("gradeId"))){
					if(!removeGIdList.contains(os.getString("gradeId"))){
						removeGIdList.add(os.getString("gradeId"));
					}
				}
				continue;
			}
			String orgId = os.getString("orgId");
			LinkedHashSet<String> idSet =  new LinkedHashSet();
			if(orgIdScopeIdsmap.containsKey(orgId)){
				idSet = orgIdScopeIdsmap.get(orgId);	
			}
			if(currentLevel!=0){
				idSet.add(currentLevel+"");
				orgIdScopeIdsmap.put(orgId, idSet);
			}
		}
		//获取机构-科目数据
		Map<String,String> orgIdLessonIdsmap = new HashMap<String, String>();
		//Map<String,String> orgIdLessonNamesMap = new HashMap<String, String>();
		obj.put("ids", rids);
		List<JSONObject> olList = csCurCommonDataDao.getOrgLessonList(obj);
		for(JSONObject ol:olList){
			String orgId = ol.getString("orgId");
			String lessonId = ol.getString("lessonId");
			String ids = "";
			if(orgIdLessonIdsmap.containsKey(orgId)){
				ids = orgIdLessonIdsmap.get(orgId);	
			}
			if(StringUtils.isNotBlank(lessonId)){
				ids+= lessonId +",";
				orgIdLessonIdsmap.put(orgId, ids);
			}
		}
		//获取机构成员数据
		List<JSONObject> rList=null;
		List<JSONObject> gList=null;
		try {
			gList = getTeacherByLessonOrGradeBatch(termInfoId,obj.getString("name"),removeGIdList,orgIdScopeIdsmap,new HashMap<String,String>(),schoolId,xn);
			rList = getTeacherByLessonOrGradeBatch(termInfoId,obj.getString("name"),new ArrayList<String>(),new HashMap<String,LinkedHashSet<String>>(),orgIdLessonIdsmap,schoolId,xn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//返回成员数据
		if(rList!=null){
			for(JSONObject r:rList){
				String name = r.getString("name");
				Long accountId = r.getLong("accountId");
				if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
					continue;
				}
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!ridsList.contains(accountId+"")){  //去重
					ridsList.add(accountId+"");
					raList.add(a);
				}
			}
		}
		if(gList!=null){
			for(JSONObject g:gList){
				String name = g.getString("name");
				Long accountId = g.getLong("accountId");
				if(StringUtils.isEmpty(name) && (accountId==null || accountId==0)){
					continue;
				}
				Account a = new Account();
				a.setName(name);
				a.setId(accountId);
				if(!gidsList.contains(accountId+"")){  //去重
					gidsList.add(accountId+"");
					gaList.add(a);
				}
			}
		}
		raList.retainAll(gaList);
		aList.addAll(raList);
		return aList;
	}
	public List<JSONObject> getAccountByRoleAndCondition(JSONObject param) {
		return csCurCommonDataDao.getAccountByRoleAndCondition(param);
	}
	public List<JSONObject> getUserIdByConditon(JSONObject param) {
		return csCurCommonDataDao.getUserIdByConditon(param);
	}
	public String getSchoolIdByExtId(String extSchoolId, String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("extId", extSchoolId);
		json.put("termInfoId", termInfoId);
		return csCurCommonDataDao.getSchoolIdByExtId(json);
	}
	public List<JSONObject> getUserIdByExtIdRole(String extId, String currentXnxq, int role) {
			JSONObject param = new JSONObject();
			param.put("extId" , extId);
			param.put("termInfoId", currentXnxq);
			param.put("role", role);
			return csCurCommonDataDao.getUserIdByExtIdRole(param);
	}
	public List<Long> getModuleManagers(String appId, String termInfoId,long schoolId) {
		JSONObject param = new JSONObject();
		param.put("appId" , appId);
		param.put("termInfoId", termInfoId);
		param.put("schoolId", schoolId);
		return csCurCommonDataDao.getModuleManagers(param);
	}
	public List<JSONObject> getExtLessonIdBySchoolId(String termInfoId, long schoolId) {
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("schoolId", schoolId);
		return csCurCommonDataDao.getExtLessonIdBySchoolId(param);
	}
	public List<JSONObject> getExtLessonIdByIds(String termInfoId, List<Long> lIds) {
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("ids", lIds);
		return csCurCommonDataDao.getExtLessonIdByIds(param);
	}
	
	/*
	 * 角色类型,0.家长,1.老师, 2.学生, 3.学校工作人员, 4.学校管理员,5.平台管理员, 6.系统管理员
	 */
	public List<JSONObject> getUserByAccountIds(List<Long> ids,String termInfoId,int roleId,long classId){
		JSONObject param = new JSONObject();
		param.put("accountIds", ids);
		param.put("termInfoId", termInfoId);
		param.put("roleId", roleId);
		param.put("classId", classId);
		return csCurCommonDataDao.getUserByAccountIds(param);
	}
	
	public List<JSONObject> getStudentParentByClassId(long classId,String termInfoId){
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("classId", classId);
		return csCurCommonDataDao.getStudentParentByClassId(param);
	}
	
	public List<JSONObject> getAccountBatch(List<Long> ids,String termInfoId){
		JSONObject obj = new JSONObject();
		obj.put("ids", ids);
		obj.put("termInfoId", termInfoId);
		return csCurCommonDataDao.getAccountBatch(obj);
	}
	
	public List<JSONObject> getParentByStudentIds(List<Long> ids,String termInfoId){
		JSONObject obj = new JSONObject();
		obj.put("ids", ids);
		obj.put("termInfoId", termInfoId);
		logger.info("getParentByStudentIds:"+termInfoId+",ids:"+ids.size());
		return csCurCommonDataDao.getParentByStudentIds(obj);
	}
	public List<JSONObject> getSimpleParentByStuMsg(List<Long> accountIds, String termInfoId, long schoolId) {
		if(accountIds==null || accountIds.size()<1) {
			return null;
		}
		JSONObject obj = new JSONObject();
		obj.put("ids", accountIds);
		obj.put("termInfoId", termInfoId);
		obj.put("schoolId", schoolId);
		List<JSONObject> pIdObjs = csCurCommonDataDao.getParentIdsByStu(obj);
		List<String> pIds =  new ArrayList<String>();
		Map<String,JSONObject> studentParentMap = new HashMap<String,JSONObject>();
		for(JSONObject pId:pIdObjs) {
			String parentId = pId.getString("parentId");
			studentParentMap.put(parentId, pId);
			pIds.add(parentId);
		}
		List<JSONObject> pObjs = null;
		if(pIds!=null && pIds.size()>0) {
			obj.put("ids", pIds);
			 pObjs = csCurCommonDataDao.getSimpleParentObj(obj);
			 for(JSONObject pObj :pObjs) {
				String parentId = pObj.getString("parentId");
				JSONObject studentParent = studentParentMap.get(parentId);
				if(studentParent!=null) {
					String studentAccountId = studentParent.getString("studentAccountId");
					String studentName = studentParent.getString("studentName");
					pObj.put("studentAccountId", studentAccountId);
					pObj.put("studentName", studentName);
				}
			 }
			 
		}
		return pObjs;
	}
}

