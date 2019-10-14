package com.talkweb.commondata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.CurrentTermRsp;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.TermInfo;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.accountcenter.thrift.UserPermissions;
import com.talkweb.auth.entity.MergeTool;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.datadictionary.domain.TDmBjlx;

/**
 * 基础数据统一调用接口
 * @author zxy
 * @date 2016-01-15
 * @data 2017-02-15 添加当前长沙库基础数据  @author zhanghuihui
 */
@Service
public class AllCommonDataService {
	
	@Autowired
	private CommonDataService commonDataService; //基础数据公共层
	@Autowired
	private CurCommonDataService curCommonDataService; //深圳当前库（远程sdk）
	@Autowired 
	private CsCurCommonDataService csCurCommonDataService;//长沙当前库
	@Autowired
	private HisCommonDataService hisCommonDataService; //历史库
	
	private static final Logger logger = LoggerFactory.getLogger(AllCommonDataService.class);
	ResourceBundle rb = ResourceBundle.getBundle("constant.constant" );
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	/**
	 * 获取配置文件中constant.properties当前学年学期的值
	 */
	@Value("#{settings['currentTermInfo']}")
	private String currentTermInfo;
	/**
	 * 查询年级组和教研组下面的教师信息  交集
	 * @param termInfoId
	 *            学年学期（ 必传）
	 * @param schoolId
	 *            学校ID（ 必传）
	 * @param gradeId
	 *            年级组ID（ 必传，多个用逗号分隔）
	 * @param researchId
	 *            教研组ID （必传，多个用逗号分隔）
	 * @param name
	 *            教师姓名 （可以[为空]）
	 * @return
	 */
	public List<Account> getOrgTeacherListIntersect(String termInfoId,long schoolId, String gradeId,
			String researchId, String name) {
		if(StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(termInfoId)){
			return new ArrayList<Account>();
		}
		if(StringUtils.isEmpty(researchId) && StringUtils.isEmpty(gradeId)){
			return new ArrayList<Account>();
		}
		if(isCurTermInfo(schoolId,termInfoId))
		{
			if(isCsCurPlate(schoolId)){ //长沙
				JSONObject obj=new JSONObject();
				 termInfoId=judgeTermInfoId(termInfoId,schoolId);
				 String xn = termInfoId.substring(0, 4);
				obj.put("schoolId", schoolId);
				Set gidSet = new HashSet<String>();
				Set ridSet = new HashSet<String>();
				List<String> rids = Arrays.asList(researchId.split(","));
				List<String> gids = Arrays.asList(gradeId.split(","));
				ridSet.addAll(rids);
				gidSet.addAll(gids);
				List<String> tIds = new ArrayList<String>();
				tIds.add(T_OrgType.T_Grade.getValue()+"");
				tIds.add(T_OrgType.T_Teach.getValue()+"");
				obj.put("tIds", tIds);
				obj.put("gids", new ArrayList<String>(gidSet));
				obj.put("rids", new ArrayList<String>(ridSet));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				obj.put("xn", xn); 
				return csCurCommonDataService.getOrgTeacherListIntersect(obj); 
				/*termInfoId=judgeTermInfoId(termInfoId,schoolId);
				obj.put("schoolId", schoolId);
				obj.put("orgResearchIdList", Arrays.asList(researchId.split(",")));
				obj.put("orgGradeIdList", Arrays.asList(gradeId.split(",")));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				
				return csCurCommonDataService.getOrgTeacherList(obj);*/ 
			}else{ //深圳只支持并集
				return curCommonDataService.getOrgTeacherList(schoolId, gradeId, researchId, name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,schoolId);
			obj.put("schoolId", schoolId);
			obj.put("orgResearchIdList", Arrays.asList(researchId.split(",")));
			obj.put("orgGradeIdList", Arrays.asList(gradeId.split(",")));
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			return hisCommonDataService.getOrgTeacherList(obj);
		}
	}
	/**
	 * 查询年级组和教研组下面的教师信息  并集
	 * @param termInfoId
	 *            学年学期（ 必传）
	 * @param schoolId
	 *            学校ID（ 必传）
	 * @param gradeId
	 *            年级组ID（ 必传，多个用逗号分隔）
	 * @param researchId
	 *            教研组ID （必传，多个用逗号分隔）
	 * @param name
	 *            教师姓名 （可以[为空]）
	 * @return
	 */
	public List<Account> getOrgTeacherList(String termInfoId,long schoolId, String gradeId,
			String researchId, String name) {
		if(StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(termInfoId)){
			return new ArrayList<Account>();
		}
		if(StringUtils.isEmpty(researchId) && StringUtils.isEmpty(gradeId)){
			return new ArrayList<Account>();
		}
		if(isCurTermInfo(schoolId,termInfoId))
		{
			if(isCsCurPlate(schoolId)){ //长沙
				JSONObject obj=new JSONObject();
				 termInfoId=judgeTermInfoId(termInfoId,schoolId);
				 String xn = termInfoId.substring(0, 4);
				obj.put("schoolId", schoolId);
				Set idSet = new HashSet<String>();
				List<String> ids1 = Arrays.asList(researchId.split(","));
				List<String> ids2 = Arrays.asList(gradeId.split(","));
				idSet.addAll(ids1);
				idSet.addAll(ids2);
				List<String> tIds = new ArrayList<String>();
				tIds.add(T_OrgType.T_Grade.getValue()+"");
				tIds.add(T_OrgType.T_Teach.getValue()+"");
				obj.put("tIds", tIds);
				obj.put("ids", new ArrayList<String>(idSet));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				obj.put("xn", xn); 
				return csCurCommonDataService.getOrgTeacherList(obj); 
				/*termInfoId=judgeTermInfoId(termInfoId,schoolId);
				obj.put("schoolId", schoolId);
				obj.put("orgResearchIdList", Arrays.asList(researchId.split(",")));
				obj.put("orgGradeIdList", Arrays.asList(gradeId.split(",")));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				
				return csCurCommonDataService.getOrgTeacherList(obj);*/ 
			}else{ //深圳
				return curCommonDataService.getOrgTeacherList(schoolId, gradeId, researchId, name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,schoolId);
			obj.put("schoolId", schoolId);
			obj.put("orgResearchIdList", Arrays.asList(researchId.split(",")));
			obj.put("orgGradeIdList", Arrays.asList(gradeId.split(",")));
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			return hisCommonDataService.getOrgTeacherList(obj);
		}
	}

	/**
	 * 查询多个机构下面的教师信息
	 * @param termInfoId
	 *            学年学期（ 必传）
	 * @param schoolId
	 *            学校ID（ 必传）
	 * @param orgId
	 *            机构id(多个使用逗号分隔)
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return
	 * 
	 */
	public List<Account> getOrgTeacherList(String termInfoId,long schoolId, String orgId,
			String name) {
		if(StringUtils.isEmpty(schoolId) || StringUtils.isEmpty(termInfoId)){
			return new ArrayList<Account>();
		}
		if(StringUtils.isEmpty(orgId)){
			return new ArrayList<Account>();
		}
		if(isCurTermInfo(schoolId,termInfoId))
		{
			if(isCsCurPlate(schoolId)){
				JSONObject obj=new JSONObject();
				 termInfoId=judgeTermInfoId(termInfoId,schoolId);
				String xn = termInfoId.substring(0, 4);
				obj.put("schoolId", schoolId);
				obj.put("ids", Arrays.asList(orgId.split(",")));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				obj.put("xn", xn);
				List<String> tIds = new ArrayList<String>();
				tIds.add(T_OrgType.T_Grade.getValue()+"");
				tIds.add(T_OrgType.T_Teach.getValue()+"");
				tIds.add(T_OrgType.T_PreLesson.getValue()+"");
				tIds.add(T_OrgType.T_Depart.getValue()+"");
				obj.put("tIds", tIds);
				return csCurCommonDataService.getOrgTeacherListAllType(obj); 
				/* termInfoId=judgeTermInfoId(termInfoId,schoolId);
				obj.put("schoolId", schoolId);
				obj.put("orgIdList", Arrays.asList(orgId.split(",")));
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				return csCurCommonDataService.getOrgTeacherList(obj);*/ 
			}else{
				return curCommonDataService.getOrgTeacherList(schoolId, orgId, name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,schoolId);
			obj.put("schoolId", schoolId);
			obj.put("orgIdList", Arrays.asList(orgId.split(",")));
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			return hisCommonDataService.getOrgTeacherList(obj);
		}
		
	}

	/**
	 * 查询某种机构(年级组/教研组/备课组/科室)下面的教师信息
	 * @param termInfoId
	 *            学年学期（ 必传）
	 * @param school
	 *            学校
	 * @param type
	 *            机构类型 1:教研组，2:年级组，3:备课组，6:科室
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return List<Account>
	 * 
	 */
	public List<Account> getOrgTeacherList(String termInfoId,School school, int type, String name) {
		if(StringUtils.isEmpty(termInfoId)||school==null){
			return new ArrayList<Account>();
		}
		if(type!=T_OrgType.T_Grade.getValue() && type!=T_OrgType.T_Depart.getValue() && type!=T_OrgType.T_Teach.getValue() && type!=T_OrgType.T_PreLesson.getValue()){
			return new ArrayList<Account>();
		}
		if(isCurTermInfo(school.getId(),termInfoId))
		{
			if(isCsCurPlate(school.getId())){
				JSONObject obj=new JSONObject();
				 termInfoId=judgeTermInfoId(termInfoId,school.getId());
				 String xn = termInfoId.substring(0, 4);
				obj.put("schoolId", school.getId());
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				obj.put("xn", xn);
				List<String> tIds = new ArrayList<String>();
				tIds.add(type+"");
				obj.put("tIds", tIds); 
				 return csCurCommonDataService.getOrgTeacherListAllType(obj); 
				/*termInfoId=judgeTermInfoId(termInfoId,school.getId());
				obj.put("schoolId", school.getId());
				obj.put("type", type);
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				return csCurCommonDataService.getOrgTeacherList(obj);*/ 
			}else{
				return curCommonDataService.getOrgTeacherList(school, type, name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,school.getId());
			obj.put("schoolId", school.getId());
			obj.put("type", type);
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			return hisCommonDataService.getOrgTeacherList(obj);
		}
	}
	/**
	 * 查询老师 所属机构 （多个老师list）
	 * @param tlist<Account> 教师信息
	 * 
	 * @param school 学校信息
	 * 
	 * @param termInfo 学年学期
	 * 
	 * @return List<JSONObject>
	 * ## 
	 */
	/*public List<JSONObject> getOrgTeacherList(List<Account> tlist,School school,String termInfoId){
		if(StringUtils.isEmpty(termInfoId)){
			return new ArrayList<JSONObject>();
		}
		if(school ==null){
			return new ArrayList<JSONObject>();
		}
		if (StringUtils.hasLength(termInfoId)&&isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				JSONObject object=new JSONObject();
				object.put("schoolId", school.getId());
				object.put("teacherList", tlist);
				object.put("termInfoId", termInfoId);
				return hisCommonDataService.getSchoolOrgBytist(object);
			}else{
				return curCommonDataService.getOrgTeacherList(tlist, school);
			}
			
		}else{
			JSONObject object=new JSONObject();
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			object.put("schoolId", school.getId());
			object.put("teacherList", tlist);
			object.put("termInfoId", termInfoId);
			return hisCommonDataService.getSchoolOrgBytist(object);
		}		
	}*/
	
	/**
	 * 通过学校ID获取学校
	 * 
	 * @param schoolId
	 *            学校ID
	 * @return School
	 */
	public School getSchoolById(long schoolId,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId))
		{
			
				JSONObject param = new JSONObject();
				param.put("termInfoId", termInfoId);
				param.put("schoolId", schoolId);
				return csCurCommonDataService.getSchoolById(param);
			
		}
		else
		{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("termInfoId", termInfoId);
			param.put("schoolId", schoolId);
			return hisCommonDataService.getSchoolById(param);
		}
	}
	/**
	 * 批量获取账号用户,调用并发接口getAccountBatchParallel
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountIds
	 *            账号ids
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 * @update 2018.11.20 添加account及user的extId返回
	 */
	public List<Account> getAccountBatch(long schoolId, List<Long> accountIds,	String termInfoId) {
		if(accountIds==null||accountIds.size()==0) {return new ArrayList<Account>();}
		if(isCurTermInfo(schoolId, termInfoId)){
//			if(isCsCurPlate(schoolId)){
				return csCurCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
//			}else{
//				return curCommonDataService.getAccountBatch(schoolId, accountIds);
//			}
//			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			return hisCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
		}
	}
	
	/**
	 * 获取全校当前任课的老师信息
	 * @param termInfoId
	 *            学年学期（ 必传） 
	 * @param school
	 *            学校对象
	 * @param name
	 *            教师姓名(可以[为空])
	 * @return List<User>
	 */
	public List<Account> getCourseTeacherList(String termInfoId,School school, String name) {
		if(isCurTermInfo(school.getId(),termInfoId))
		{
			if(isCsCurPlate(school.getId())){
				JSONObject obj=new JSONObject();
				obj.put("schoolId", school.getId());
				obj.put("name", name);
				obj.put("termInfoId", termInfoId);
				return csCurCommonDataService.getCourseTeacherList(obj);
			}else{
				return curCommonDataService.getCourseTeacherList(school, name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,school.getId());
			obj.put("schoolId", school.getId());
			obj.put("name", name);
			obj.put("termInfoId", termInfoId);
			return hisCommonDataService.getCourseTeacherList(obj);
		}
		
	}
	
	/**
	 * 获取任课老师信息
	 * 
	 * @param map
	 *            schoolId 学校ID（必传） lessonId 科目ID，多个使用逗号分隔 classId
	 *            班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件） termInfoId 学年学期（必传）
	 *            usedGradeId 使用年级[可以多个逗号隔开] name 教师姓名
	 * @return List<Account>
	 */
	public List<Account> getCourseTeacherList(HashMap<String, Object> map) {
		if(isCurTermInfo(Long.parseLong(map.get("schoolId").toString()),map.get("termInfoId").toString()))
		{
			if(isCsCurPlate(Long.parseLong(map.get("schoolId").toString()))){
				JSONObject obj=new JSONObject();
				obj.put("termInfoId",  map.get("termInfoId").toString());
				obj.put("schoolId",map.get("schoolId"));
				if(map.containsKey("lessonId"))
				{
					obj.put("lessonIdList", Arrays.asList(map.get("lessonId").toString().split(",")));
				}
				if(map.containsKey("classId"))
				{
					obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
				}
				else if(map.containsKey("usedGradeId"))
				{
					List<Integer> njdmList=new ArrayList<Integer>();
					String xnxq = map.get("termInfoId").toString();
					String xn = xnxq.substring(0, 4);
					for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
					{
						njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
					}
					obj.put("njdmList", njdmList);
					obj.put("xn", Integer.valueOf(xn));
				}
				if(map.containsKey("name"))
				{
					obj.put("name", map.get("name"));
				}
				return csCurCommonDataService.getCourseTeacherList(obj);
			}else{
				return curCommonDataService.getCourseTeacherList(map);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			String termInfoId=judgeTermInfoId( map.get("termInfoId").toString(),Long.parseLong(map.get("schoolId").toString()));
			obj.put("termInfoId",  termInfoId);
			obj.put("schoolId",map.get("schoolId"));
			if(map.containsKey("lessonId"))
			{
				obj.put("lessonIdList", Arrays.asList(map.get("lessonId").toString().split(",")));
			}
			if(map.containsKey("classId"))
			{
				obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
			}
			else if(map.containsKey("usedGradeId"))
			{
				List<Integer> njdmList=new ArrayList<Integer>();
				String xnxq = map.get("termInfoId").toString();
				String xn = xnxq.substring(0, 4);
				for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
				{
					njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
				}
				obj.put("njdmList", njdmList);
				obj.put("xn", Integer.valueOf(xn));
			}
			if(map.containsKey("name"))
			{
				obj.put("name", map.get("name"));
			}
			return hisCommonDataService.getCourseTeacherList(obj);
		}
		
	}
	
	/**
	 * 获取任课教师 JSONArray
	 * 
	 * @param map
	 * schoolId 学校ID（必传） 
	 * lessonId 科目ID，多个使用逗号分隔
	 * classId 班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件）
     * termInfoId 学年学期（必传） 
     * usedGradeId 使用年级[可以多个逗号隔开] 
     * name 教师姓名
	 * @return JSONArray
	 * {"lessonName":"英语","lessonId":750,"classId":10033,"teaId":108688,"teaName":"白华"}
	 */
	public JSONArray getCourseTeacherList(School school,HashMap<String, Object> map){
		if(isCurTermInfo(school.getId(),map.get("termInfoId").toString()))
		{
			if(isCsCurPlate(school.getId())){
				JSONObject obj=new JSONObject();
				obj.put("termInfoId",  map.get("termInfoId").toString());
				obj.put("schoolId",school.getId());
				if(map.containsKey("lessonId"))
				{
					obj.put("lessonIdList", Arrays.asList(map.get("lessonId").toString().split(",")));
				}
				if(map.containsKey("classId"))
				{
					obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
				}
				else if(map.containsKey("usedGradeId"))
				{
					List<Integer> njdmList=new ArrayList<Integer>();
					String xnxq = map.get("termInfoId").toString();
					String xn = xnxq.substring(0, 4);
					for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
					{
						njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
					}
					obj.put("njdmList", njdmList);
					obj.put("xn", Integer.valueOf(xn));
				}
				if(map.containsKey("name"))
				{
					obj.put("name", map.get("name"));
				}
				return csCurCommonDataService.getSimpleCourseTeacherList(obj);
			}else{
				return curCommonDataService.getCourseTeacherList(school,map);	
			}
			
		}
		else
		{
			JSONObject obj=new JSONObject();
			String termInfoId=judgeTermInfoId( map.get("termInfoId").toString(),Long.parseLong(map.get("schoolId").toString()));
			obj.put("termInfoId",  termInfoId);
			obj.put("schoolId",school.getId());
			if(map.containsKey("lessonId"))
			{
				obj.put("lessonIdList", Arrays.asList(map.get("lessonId").toString().split(",")));
			}
			if(map.containsKey("classId"))
			{
				obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
			}
			else if(map.containsKey("usedGradeId"))
			{
				List<Integer> njdmList=new ArrayList<Integer>();
				String xnxq = map.get("termInfoId").toString();
				String xn = xnxq.substring(0, 4);
				for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
				{
					njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
				}
				obj.put("njdmList", njdmList);
				obj.put("xn", Integer.valueOf(xn));
			}
			if(map.containsKey("name"))
			{
				obj.put("name", map.get("name"));
			}
			return hisCommonDataService.getSimpleCourseTeacherList(obj);
		}
	}

	/**
	 * 获取班主任信息
	 * 
	 * @param map
	 *            schoolId 学校ID（必传） classId 班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件）
	 *            termInfoId 学年学期（必传） usedGradeId 使用年级[可以多个逗号隔开] name 教师姓名
	 * @return List<Account>
	 */
	public List<Account> getDeanList(HashMap<String, Object> map) {
		if(isCurTermInfo(Long.parseLong(map.get("schoolId").toString()),map.get("termInfoId").toString()))
		{
			if(isCsCurPlate(Long.parseLong(map.get("schoolId").toString()))){
				JSONObject obj=new JSONObject();
				obj.put("termInfoId",  map.get("termInfoId").toString());
				obj.put("schoolId",map.get("schoolId"));
				if(map.containsKey("classId"))
				{
					obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
				}
				else if(map.containsKey("usedGradeId"))
				{
					List<Integer> njdmList=new ArrayList<Integer>();
					String xnxq = map.get("termInfoId").toString();
					String xn = xnxq.substring(0, 4);
					for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
					{
						njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
					}
					obj.put("njdmList", njdmList);
					obj.put("xn", Integer.valueOf(xn));
				}
				if(map.containsKey("name"))
				{
					obj.put("name", map.get("name"));
				}
				return csCurCommonDataService.getDeanList(obj);
			}else{
				return curCommonDataService.getDeanList(map);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			String termInfoId=judgeTermInfoId( map.get("termInfoId").toString(),Long.parseLong(map.get("schoolId").toString()));
			obj.put("termInfoId",  termInfoId);
			obj.put("schoolId",map.get("schoolId"));
			if(map.containsKey("classId"))
			{
				obj.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
			}
			else if(map.containsKey("usedGradeId"))
			{
				List<Integer> njdmList=new ArrayList<Integer>();
				String xnxq = map.get("termInfoId").toString();
				String xn = xnxq.substring(0, 4);
				for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
				{
					njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
				}
				obj.put("njdmList", njdmList);
				obj.put("xn", Integer.valueOf(xn));
			}
			if(map.containsKey("name"))
			{
				obj.put("name", map.get("name"));
			}
			return hisCommonDataService.getDeanList(obj);
		}
	}
	
	/**
	 * 获取全校所有教职工（`t_school_manager` `t_school_staff` `t_school_teacher`）
	 * 只要不是使用oms里面的任课，就调用这个接口
	 * @param school
	 * @param termInfoId学年学期（必传）
	 * @param name 姓名
	 * @return
	 */
	public List<Account> getAllSchoolEmployees(School school,String termInfoId,String name) {
		if(isCurTermInfo(school.getId(), termInfoId))
		{
			if(isCsCurPlate(school.getId())){
				JSONObject obj=new JSONObject();
				obj.put("schoolId", school.getId());
				obj.put("name", name);
				obj.put("termInfoId",  termInfoId);
				return csCurCommonDataService.getAllSchoolEmployees(obj);
			}else{
				return curCommonDataService.getAllSchoolEmployees(school,name);
			}
		}
		else
		{
			JSONObject obj=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,school.getId());
			obj.put("schoolId", school.getId());
			obj.put("name", name);
			obj.put("termInfoId",  termInfoId);
			return hisCommonDataService.getAllSchoolEmployees(obj);
		}
	}

	/**
	 * 供根据学校得到所有的年级
	 * 
	 * @param sch
	 *            学校对象
	 * @return
	 */
	public List<Grade> getGradeList(School sch,String termInfoId) {
		Long schoolId=sch.getId();
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(sch.getId())){
				JSONObject param=new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId",termInfoId );
				return csCurCommonDataService.getGradeList(param);
			}else{
				return curCommonDataService.getGradeList(sch);
			}
		}else{
			JSONObject param=new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,schoolId);
			param.put("schoolId", schoolId);
			param.put("termInfoId",termInfoId );
			return hisCommonDataService.getGradeList(param);
		}
	}

	/**
	 * 批量调用 通过多个年级ID得到年级对象List<LessonInfo>
	 * 
	 * @param schoolId
	 *            学校id
	 * @param ids
	 *            年级ids
	 * @return
	 */
	public List<Grade> getGradeBatch(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("ids", ids);
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getGradeBatch(param);
			}else{
				return curCommonDataService.getGradeBatch(schoolId, ids);
			}
		}else{
			JSONObject param = new JSONObject();
			termInfoId=judgeTermInfoId(termInfoId,schoolId);
			param.put("schoolId", schoolId);
			param.put("ids", ids);
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getGradeBatch(param);
		}
	}

	/**
	 * 获取班级 (查询多个年级或单个年级下面的班级)
	 * 
	 * @param map
	 *            schoolId 学校ID（必传 Long类型） classTypeId 班级类型 0综合 1文科 2理科 termInfoId
	 *            学年学期（必传） usedGradeId 使用年级[可以多个逗号隔开]
	 * @return List<Classroom>
	 */
	public List<Classroom> getClassList(HashMap<String, Object> map) {
		if(isCurTermInfo(Long.parseLong(map.get("schoolId").toString()),map.get("termInfoId").toString()))
		{
			if(isCsCurPlate(Long.parseLong(map.get("schoolId").toString()))){
				map.put("termInfoId", map.get("termInfoId").toString());
				return csCurCommonDataService.getClassList(map);
			}else{
				return curCommonDataService.getClassList(map);
			}
			
		}
		else
		{
			String termInfoId=judgeTermInfoId(map.get("termInfoId").toString(),Long.parseLong(map.get("schoolId").toString()));
			map.put("termInfoId", termInfoId);
			return hisCommonDataService.getClassList(map);
		}
	}

	
	/**
	 * 获取全校所有班级-简版【仅含name，id，gradeId】
	 * @param sch
	 * @param grade	可以为空
	 * @return
	 */
	public List<Classroom> getSimpleClassList(School sch,List<Grade> grade,String termInfoId ) {
		if(isCurTermInfo(sch.getId(), termInfoId)){
			if(isCsCurPlate(sch.getId())){
				JSONObject param = new JSONObject();
				param.put("schoolId", sch.getId());
				param.put("termInfoId", termInfoId);
				param.put("gradeList", grade);
				return csCurCommonDataService.getSimpleClassList(param); 
			}else{
				return curCommonDataService.getSimpleClassList(sch, grade);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, sch.getId());
			JSONObject param = new JSONObject();
			param.put("schoolId", sch.getId());
			param.put("termInfoId", termInfoId);
			param.put("gradeList", grade);
			return hisCommonDataService.getSimpleClassList(param);
		}
	}

	/**
	 * 批量调用 通过多个班级ID得到班级对象List<Classroom>
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
	public List<Classroom> getClassroomBatch(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("ids", ids);
				return csCurCommonDataService.getClassroomBatch(param);
			}else{
				return curCommonDataService.getClassroomBatch(schoolId, ids);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("termInfoId", termInfoId);
			param.put("ids", ids);
			return hisCommonDataService.getClassroomBatch(param);
		}
	}
	/**
	 * 批量调用 通过多个班级ID得到班级对象List<Classroom>
	 * (NoAccoumt的意思是所有的studentId和parentId等等都是userId而不是AccountId)
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 * @author zhh
	 */
	public List<Classroom> getClassroomBatchNoAccount(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("ids", ids);
				return csCurCommonDataService.getClassroomBatchNoAccount(param);
			}else{
				return curCommonDataService.getClassroomBatchNoAccount(schoolId, ids);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("termInfoId", termInfoId);
			param.put("ids", ids);
			return hisCommonDataService.getClassroomBatchNoAccount(param);
		}
	}

	/**
	 * 批量调用 通过多个班级ID得到班级对象List<Classroom>
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 */
	public List<Classroom> getSimpleClassBatch(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("ids", ids);
				return csCurCommonDataService.getSimpleClassBatch(param);
			}else{
				return curCommonDataService.getSimpleClassBatch(schoolId, ids);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("termInfoId", termInfoId);
			param.put("ids", ids);
			return hisCommonDataService.getSimpleClassBatch(param);
		}
		
	}

	/**
	 * 批量调用 通过多个科目ID得到科目对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 *            科目ids
	 * @return List<LessonInfo>
	 */
	public List<LessonInfo> getLessonInfoBatch(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				param.put("ids", ids);
				return csCurCommonDataService.getLessonInfoBatch(param);
			}else{
				return curCommonDataService.getLessonInfoBatch(schoolId, ids);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("termInfoId", termInfoId);
			param.put("ids", ids);
			return hisCommonDataService.getLessonInfoBatch(param);
		}
		
	}

	/**
	 * 根据学校获取学校所有科目
	 * 
	 * @param school
	 *            学校
	 * @return List<LessonInfo>
	 */
	public List<LessonInfo> getLessonInfoList(School school,String termInfoId) {
		if(isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				JSONObject param = new JSONObject();
				param.put("schoolId", school.getId());
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getLessonInfoList(param);
			}else{
				return curCommonDataService.getLessonInfoList(school);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			JSONObject param = new JSONObject();
			param.put("schoolId", school.getId());
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getLessonInfoList(param);
		}
	}

	/**
	 * 获取某种类型科目
	 * 
	 * @param school
	 *            学校
	 * @param type
	 *            0普通  1综合2活动
	 * @return
	 */
	public List<LessonInfo> getLessonInfoByType(School school, int type,String termInfoId) {
		if(isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				JSONObject param = new JSONObject();
				param.put("schoolId", school.getId());
				param.put("type", type);
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getLessonInfoByType(param);
			}else{
				return curCommonDataService.getLessonInfoByType(school, type);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			JSONObject param = new JSONObject();
			param.put("schoolId", school.getId());
			param.put("type", type);
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getLessonInfoByType(param);
		}
		
	}

	/**
	 * （全校的）根据学号/学籍号/姓名查询学生
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param termInfoId
	 *            学年学期ID
	 * @param keyword
	 *            学号/学籍号/姓名
	 * @return List<Account>
	 * @update 2017.08.14 添加idNumber返回
	 */
	public List<Account> getStudentList(long schoolId, String termInfoId,
			String keyword) {
		if(isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject params = new JSONObject();
				params.put("schoolId", schoolId);
				params.put("keyword", keyword);
				params.put("termInfoId", termInfoId);
				return csCurCommonDataService.getSchoolStudentList(params);
			}else{
				return curCommonDataService.getStudentList(schoolId,termInfoId,keyword);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject params = new JSONObject();
			params.put("schoolId", schoolId);
			params.put("keyword", keyword);
			params.put("termInfoId", termInfoId);
			return hisCommonDataService.getSchoolStudentList(params);
		}
	}

	/**
	 * 获取学生 （年级的）年级、班级类型的
	 * 
	 * @param map
	 *            schoolId 学校代码 （必传） classTypeId 班级类型0:综合，1：文科，2： 理科， classId
	 *            班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件） termInfoId 学年学期（必传）
	 *            usedGradeId 使用年级[可以多个逗号隔开] keyword 姓名/学号/学籍号 可以[为空]
	 * @return List<Account>
	 * @update 2017.08.14 添加idNumber返回
	 */
	public List<Account> getStudentList(HashMap<String, Object> map) {
		long schoolId = Long.parseLong(map.get("schoolId").toString());
		if(map.containsKey("termInfoId")&&isCurTermInfo(schoolId, map.get("termInfoId").toString())){
			if(isCsCurPlate(schoolId)){
				JSONObject params = new JSONObject();
				params.put("termInfoId",  map.get("termInfoId").toString());
				params.put("schoolId",schoolId);
				if(map.containsKey("classTypeId")){
					params.put("classType", map.get("classTypeId").toString());
				}
				if(map.containsKey("classId")){
					params.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
				}else if(map.containsKey("usedGradeId"))
				{
					List<Integer> njdmList=new ArrayList<Integer>();
					String xnxq = map.get("termInfoId").toString();
					String xn = xnxq.substring(0, 4);
					for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
					{
						njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
					}
					params.put("njdmList", njdmList);
					params.put("xn", Integer.valueOf(xn));
				}
				if(map.containsKey("keyword"))
				{
					params.put("keyword", map.get("keyword"));
				}
				return csCurCommonDataService.getStudentList(params);
			}else{
				return curCommonDataService.getStudentList(map);
			}
		}else{
			String termInfoId = judgeTermInfoId(map.get("termInfoId").toString(), schoolId);
			JSONObject params = new JSONObject();
			params.put("termInfoId", termInfoId);
			params.put("schoolId",schoolId);
			if(map.containsKey("classTypeId")){
				params.put("classType", map.get("classTypeId").toString());
			}
			if(map.containsKey("classId")){
				params.put("classIdList", Arrays.asList(map.get("classId").toString().split(",")));
			}else if(map.containsKey("usedGradeId"))
			{
				List<Integer> njdmList=new ArrayList<Integer>();
				String xnxq = map.get("termInfoId").toString();
				String xn = xnxq.substring(0, 4);
				for (String s : Arrays.asList(map.get("usedGradeId").toString().split(","))) 
				{
					njdmList.add(Integer.valueOf(ConvertSYNJ2NJDM(s, xn)));
				}
				params.put("njdmList", njdmList);
				params.put("xn", Integer.valueOf(xn));
			}
			if(map.containsKey("keyword"))
			{
				params.put("keyword", map.get("keyword"));
			}
			return hisCommonDataService.getStudentList(params);
		}
	}

	/**
	 * 获取机构信息（年级组，教研组，备课组，科室）
	 * 
	 * @param map
	 *            school 学校 gradeId 年级组Id，多个使用逗号分隔
	 * @param type
	 *            1:教研组，2:年级组，3:备课组，6:科室
	 * @return List<OrgInfo>
	 */
	public List<OrgInfo> getSchoolOrgList(School school, int type, String termInfoId) {
		if (StringUtils.hasLength(termInfoId)&&isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				JSONObject param = new JSONObject();
				param.put("schoolId", school.getId());
				param.put("type", type);
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getSchoolOrgList(param);
			}else{
				return curCommonDataService.getSchoolOrgList(school, type);	
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			JSONObject param = new JSONObject();
			param.put("schoolId", school.getId());
			param.put("type", type);
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getSchoolOrgList(param);
		}
	}

	/**
	 * 通过学校获取机构信息
	 * 
	 * @param school
	 *            学校对象
	 * @return List<OrgInfo>
	 */
	public List<OrgInfo> getSchoolOrgList(School school, String termInfoId) {
		if (StringUtils.hasLength(termInfoId)&&isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				JSONObject param = new JSONObject();
				param.put("schoolId", school.getId());
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getSchoolOrgList(param);
			}else{
				return curCommonDataService.getSchoolOrgList(school);	
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			JSONObject param = new JSONObject();
			param.put("schoolId", school.getId());
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getSchoolOrgList(param);
		}
	}

	/**
	 * 通过机构ID获取机构详细信息
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param orgId
	 * @return OrgInfo
	 */
	public OrgInfo getSchoolOrgAllById(long schoolId, long orgId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("orgId", orgId);
		param.put("termInfoId", termInfoId);
		 csCurCommonDataService.getSchoolOrgById(param);
		if (StringUtils.hasLength(termInfoId)&&isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				 param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("orgId", orgId);
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getSchoolOrgById(param);
			}else{
				return curCommonDataService.getSchoolOrgAllById(schoolId, orgId);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			  param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("orgId", orgId);
			param.put("termInfoId", termInfoId);
			return hisCommonDataService.getSchoolOrgById(param);
		}
	}

	/**
	 * 批量接口 通过机构ID获取机构详细信息
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return List<OrgInfo>
	 */
	public List<OrgInfo> getSchoolOrgBatch(long schoolId, List<Long> ids, String termInfoId) {
		if (StringUtils.hasLength(termInfoId)&&isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				if (CollectionUtils.isNotEmpty(ids))
					param.put("ids", ids);
				param.put("termInfoId", termInfoId);
			    return csCurCommonDataService.getSchoolOrgList(param);
			}else{
				return curCommonDataService.getSchoolOrgBatch(schoolId, ids);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			if (CollectionUtils.isNotEmpty(ids))
				param.put("ids", ids);
			param.put("termInfoId", termInfoId);
		    return hisCommonDataService.getSchoolOrgList(param);
		}
	}

	/**
	 * 通过年级id获取年级对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 *            年级id
	 * @return
	 */
	public Grade getGradeById(long schoolId, long gradeId,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("termInfoId", termInfoId);
				param.put("schoolId", schoolId);
				param.put("gradeId", gradeId);
				return csCurCommonDataService.getGradeById(param);
			}else{
				return curCommonDataService.getGradeById(schoolId, gradeId);	
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("termInfoId", termInfoId);
			param.put("schoolId", schoolId);
			param.put("gradeId", gradeId);
			return hisCommonDataService.getGradeById(param);
		}
		
	}
	
	/**
	 * 通过年级代码gradeLevel获取年级对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 *            年级id
	 * @return
	 */
	public Grade getGradeByGradeLevel(long schoolId, T_GradeLevel gradeLevel,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("termInfoId", termInfoId);
				param.put("schoolId", schoolId);
				param.put("gradeLevel", gradeLevel);
				return csCurCommonDataService.getGradeByGradeLevel(param);
			}else{
				return curCommonDataService.getGradeByGradeLevel(schoolId, gradeLevel);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("termInfoId", termInfoId);
			param.put("schoolId", schoolId);
			param.put("gradeLevel", gradeLevel);
			return hisCommonDataService.getGradeByGradeLevel(param);
		}
		
	}
	/**
	 * 通过多个年级代码gradeLevel获取年级对象
	 * @param schoolId
	 * @param gradeLevels
	 * @param termInfoId
	 * @return
	 * @author zhh
	 */
	public List<Grade> getGradeByGradeLevelBatch(long schoolId, List<T_GradeLevel> gradeLevels,String termInfoId) {
		if(isCurTermInfo(schoolId,termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject param = new JSONObject();
				param.put("termInfoId", termInfoId);
				param.put("schoolId", schoolId);
				param.put("gradeLevels", gradeLevels);
				return csCurCommonDataService.getGradeByGradeLevelBatch(param);
			}else{
				return curCommonDataService.getGradeByGradeLevelBatch(schoolId, gradeLevels);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject param = new JSONObject();
			param.put("termInfoId", termInfoId);
			param.put("schoolId", schoolId);
			param.put("gradeLevels", gradeLevels);
			return hisCommonDataService.getGradeByGradeLevelBatch(param);
		}
	}
	

	
	/**
	 * 批量调用 通过多个用户（教师/学生/其他user）ID得到用户对象List<User>
	 * 调用并发接口getUserBatchParallel 当前学年学期，历史不调
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public List<User> getUserBatch(long schoolId, List<Long> ids,String termInfoId) {
		if(isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				return csCurCommonDataService.getUserBatch(schoolId, ids, termInfoId);
			}else{
				return curCommonDataService.getUserBatch(schoolId, ids);	
			}
				
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			return hisCommonDataService.getUserBatch(schoolId, ids, termInfoId);
		}
		
		
	}

	/**
	 * 通过用户账号得到账号对象
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountId
	 *            账号
	 * @return Account
	 * @update 2017.08.14 添加idNumber返回
	 * @update 2018.11.20 添加account及user的extId返回
	 */
	public Account getAccountAllById(long schoolId, long accountId,String termInfoId) {
		if(termInfoId!=null&&isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				/*JSONObject params = new JSONObject();
				params.put("schoolId", schoolId);
				params.put("accountId", accountId);
				params.put("termInfoId", termInfoId);*/
				//return  csCurCommonDataService.getAccountAllById(params);
				List<Long> accountIds = new ArrayList<Long>();
				accountIds.add(accountId);
				List<Account> aList = csCurCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
				if(aList!=null){
					return aList.get(0);
				}else{
					return null;
				}
			}else{
				return curCommonDataService.getAccountAllById(schoolId, accountId);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			/*JSONObject params = new JSONObject();
			params.put("schoolId", schoolId);
			params.put("accountId", accountId);
			params.put("termInfoId", termInfoId);*/
			//return  hisCommonDataService.getAccountAllById(params);
			List<Long> accountIds = new ArrayList<Long>();
			accountIds.add(accountId);
			List<Account> aList = hisCommonDataService.getAccountBatch(schoolId, accountIds, termInfoId);
			if(aList!=null && aList.size() > 0){
				return aList.get(0);
			}else{
				return null;
			}
		}
	}
	/**
	 * 通过账号ID得到用户账号名,,
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountId
	 *            账号ID
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public Account getAccountById(long schoolId, long accountId,String termInfoId) {
		if(termInfoId!=null&&isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				JSONObject params = new JSONObject();
				params.put("schoolId", schoolId);
				params.put("accountId", accountId);
				params.put("termInfoId", termInfoId);
				return csCurCommonDataService.getAccountById(params);
			}else{
				return curCommonDataService.getAccountById(schoolId, accountId);
			}
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			JSONObject params = new JSONObject();
			params.put("schoolId", schoolId);
			params.put("accountId", accountId);
			params.put("termInfoId", termInfoId);
			return hisCommonDataService.getAccountById(params);
		}
	}
	/**
	 * 通过用户账号得到账号对象!!!!!登陆专用，其他人不要使用
	 * 
	 * @param schoolId
	 *            学校id
	 * @param accountId
	 *            账号
	 * @return Account
	 * @update 2017.08.14 添加idNumber返回
	 */
	public Account getAccountAllByIdNoFilter(long schoolId, long accountId,String termInfoId) {
		if(isCsCurPlate(schoolId)){
			JSONObject params = new JSONObject();
			params.put("schoolId", schoolId);
			params.put("accountId", accountId);
			params.put("termInfoId", termInfoId);
			return  csCurCommonDataService.getAccountAllById(params);
		}else{
			return curCommonDataService.getAccountAllByIdNoFilter(schoolId, accountId);
		}
	}
	/**
	 * 根据账号id获取账号全部信息
	 * @param accountId
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public Account getAccountAllById(long accountId){
		Account a = null;
		JSONObject json = new JSONObject();
		json.put("accountId", accountId);
		json.put("termInfoId", currentTermInfo); //取自配置文件
		a = curCommonDataService.getAccountAllById(accountId);
		if(a==null){
			a = csCurCommonDataService.getAccountAllById(json);
		}
		return a;
	}
	
	/**
	 * 查询一个学校所有的学生
	 * @param schoolId 学校id
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public List<Account> getAllStudent(School school,String termInfoId) {	
		if(termInfoId!=null&&isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				return csCurCommonDataService.getAllStudent(school.getId(),termInfoId);
			}else{
				return curCommonDataService.getAllStudent(school);
			}
			
		}else{
//			List<Long> gradeIds = school.getGrades();
			termInfoId = judgeTermInfoId(termInfoId, school.getId());
			return hisCommonDataService.getAllStudent(school.getId(),termInfoId);
		}
	}
	
	/**
	 * 查询一个学校所有的班级
	 * @param school 学校对象
	 * @return
	 */
	public List<Classroom> getAllClass(School school,String termInfoId) {	
		if(termInfoId!=null&&isCurTermInfo(school.getId(), termInfoId)){
			if(isCsCurPlate(school.getId())){
				long schoolId=school.getId();
				List<Long> gradeIds = school.getGrades();
				return csCurCommonDataService.getAllClass(gradeIds,termInfoId,schoolId);
			}else{
				return curCommonDataService.getAllClass(school);
			}
			
		}else{
			long schoolId=school.getId();
			List<Long> gradeIds = school.getGrades();
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			return hisCommonDataService.getAllClass(gradeIds,termInfoId,schoolId);
		}
	}
	
	
	
	/**
	 * 通过班级ID得到班级
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param gradeId
	 * @return
	 */
	public Classroom getClassById(long schoolId, long classId,String termInfoId) {
		if(isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				return csCurCommonDataService.getClassById(schoolId, classId, termInfoId);
			}else{
				return curCommonDataService.getClassById(schoolId, classId);
			}
		
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			return hisCommonDataService.getClassById(schoolId, classId, termInfoId);
		}
		
	}
	
	/**
	 * 通过用户ID得到用户对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param userId
	 *            教师/学生/用户ID
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public User getUserById(long schoolId, long userId,	String termInfoId) {
		if(isCurTermInfo(schoolId, termInfoId)){
			if(isCsCurPlate(schoolId)){
				return csCurCommonDataService.getUserById(schoolId, userId, termInfoId);
			}else{
				return curCommonDataService.getUserById(schoolId, userId);
			}
			
		}else{
			termInfoId = judgeTermInfoId(termInfoId, schoolId);
			return hisCommonDataService.getUserById(schoolId, userId, termInfoId);
		}
	}
	/**
	 * 通过用户ID得到用户对象
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param userId
	 *            教师/学生/用户ID
	 * @return @@
	 * @update 2017.08.14 添加idNumber返回
	 */
	public User getUserById(long schoolId, long userId) {
			String termInfoId = this.getCurTermInfoId(schoolId);
			User u = null;
			if(schoolId==0){
				u = curCommonDataService.getUserById(schoolId, userId);
				if(u==null){
					u = hisCommonDataService.getUserById(schoolId, userId,termInfoId);
				}
			}else{
				if("-1000".equals(schoolId+"")||isCsCurPlate(schoolId)){ //-1000为登录提供无法获取schoolId又走光阳平台的方法
					u = csCurCommonDataService.getUserById(schoolId, userId,termInfoId);
				}else{
					u = curCommonDataService.getUserById(schoolId, userId);
				}
			}
			return u;
		
	}
	
	/**
	 * 获取用户权限 勿删(仅限当前学年学期)
	 * 
	 * @param schoolId
	 *            学校id
	 * @param userId
	 * @return  
	 */
	public UserPermissions getUserPermissionById(
			long schoolId, 
			long userId,
			String accountId) {
		String from = rb.getString("from");
		
		if(isCsCurPlate(schoolId)){
			String termInfoId = this.getCurTermInfoId(schoolId);
			return csCurCommonDataService.getUserPermissionById(schoolId,
					userId,
					termInfoId,
					from); //
		}else{
			return curCommonDataService.getUserPermissionById(schoolId, userId);
		}
	}
	
	/**
	 * 批量调用 通过多个用户（教师/学生/其他user）ID得到用户对象List<User>
	 * 调用并发接口getUserBatchParallel
	 * @param schoolId
	 *            学校ID
	 * @param ids
	 * @return 
	 */
	/*public List<User> getUserBatch(long schoolId, List<Long> ids) {
		return curCommonDataService.getUserBatch(schoolId, ids);
	}*/
	
	/**
	 * 通过账号获取账号信息，包括账号下所有User的详细信息 账号可跨校
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param account
	 *            账号
	 * @return
	 * @update 2017.08.14 添加idNumber返回
	 */
	public Account getAccountAllByAccount(long schoolId, String account) {
		if(isCsCurPlate(schoolId)){
			String termInfoId = this.getCurTermInfoId(schoolId);
			return csCurCommonDataService.getAccountAllByAccount(schoolId, account,termInfoId);
		}else{
			return curCommonDataService.getAccountAllByAccount(schoolId, account);
		}
		
	}
	  /**
	   * 
	   * @param grade
	   * @param xn
	   * @return  
	   */
		public String getSynjByGrade(Grade grade, String xn) {
			return commonDataService.getSynjByGrade(grade,xn);
		}
		/**
		 * 获取年级名称
		 * 
		 * @param 
		 * synj 使用年级（必传） 
		 * xn 当前学年
		 * @return String
		 * 
		 */
		public String getGradeNameBySynj(String synj,String xn){
			String njdm = commonDataService.ConvertSYNJ2NJDM(synj, xn);	
			T_GradeLevel tgl = T_GradeLevel.findByValue(Integer.parseInt(njdm));
			return AccountStructConstants.T_GradeLevelName.get(tgl);
		}
		
	/**
	 * 通过用户ID得到学校对象,,
	 * 
	 * @param schoolId
	 *            学校ID 此参数设置为0
	 * @param userId
	 *            教师/学生/用户ID
	 * @return School @@
	 */
	public School getSchoolByUserId(long schoolId, long userId) {
		String termInfoId = this.getCurTermInfoId(schoolId);
		return csCurCommonDataService.getSchoolByUserId(schoolId, userId,termInfoId);
	}
	
	/**
	 * 获取所有的学校列表(暂时用于OMS系统)
	 * @return List<School>  school包含schoolId和schoolName
	 * @author zhanghuihui 
	 */
	public List<School> getAllSchoolList(){
		return csCurCommonDataService.getAllSchoolList(currentTermInfo);
	}
    /**
     * 通过学校名称（模糊查询）获取学校列表(暂时用于OMS系统)
     * @param schoolName
     * @return List<School>  school包含schoolId和schoolName
     * @author zhanghuihui 
     */
	public List<School>  getSchoolInfoByName(String schoolName){
		return csCurCommonDataService.getSchoolInfoByName(schoolName,currentTermInfo); 
	}

	/**
	 * 更新任课教师的课程
	 * @param schoolId 学校id
	 * @param teacherId 任课教师的userId
	 * @param courseIds 课程List<Course>  
	 */
	public void updateTeacherLesson(long schoolId,
			long teacherId, 
			List<Course> courseIds,
			String termInfoId
			) {
			String from = rb.getString("from");
//			if("1".equals(from)){//铜仁环境，则走平台接口取学校管理员数据
//				Object accessTokenKey = "common."+schoolId+".00.accessToken";
//				try {
//					accessToken = (String) redisOperationDAO.get(accessTokenKey);
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			}
			if(termInfoId!=null&&isCurTermInfo(schoolId, termInfoId)){
				csCurCommonDataService.updateTeacherLesson(schoolId, teacherId, courseIds,termInfoId,from);
			}else{
				termInfoId = judgeTermInfoId(termInfoId, schoolId);
				hisCommonDataService.updateTeacherLesson(schoolId, teacherId, courseIds,termInfoId);
			}
		}
	
	
	/**
	 * 批量更新任课教师的课程
	 * @param schoolId 学校id
	 * @param teacherCourseMap 任课教师课程map  键为老师的userId
	 */
		public void updateTeacherLessonBatch(long schoolId,Map<Long,List<Course>> teacherCourseMap,String termInfoId) {
			String from = rb.getString("from");
			if(termInfoId!=null&&isCurTermInfo(schoolId, termInfoId)){
					csCurCommonDataService.updateTeacherLessonBatch(schoolId, teacherCourseMap,termInfoId,from);
			}else{
				termInfoId = judgeTermInfoId(termInfoId, schoolId);
				hisCommonDataService.updateTeacherLessonBatch(schoolId, teacherCourseMap,termInfoId);
			}
		}

		/**
		 * 更新班级对象（支持更新班级基本信息和任教关系信息）
		 * 
		 * @param schoolId
		 *            学校id
		 * @param c
		 * @return 
		 */
		public void updateClassroom(long schoolId,
				Classroom c,
				String termInfoId) {
//			//String accessToken = "";
//			String from = rb.getString("from");
////			if("1".equals(from)){//铜仁环境，则走平台接口取学校管理员数据
////				Object accessTokenKey = "common."+schoolId+".00.accessToken";
////				try {
////					accessToken = (String) redisOperationDAO.get(accessTokenKey);
////				} catch (Exception e) {
////					e.printStackTrace();
////				} 
////			}
			if(termInfoId!=null&&isCurTermInfo(schoolId, termInfoId)){
//				if(isCsCurPlate(schoolId)){
					try {
						csCurCommonDataService.updateClassroom(schoolId,c,termInfoId,rb.getString("from"));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//				}else{
//					curCommonDataService.updateClassroom(schoolId, c);
//				}
			
			}else{
				termInfoId = judgeTermInfoId(termInfoId, schoolId);
				hisCommonDataService.updateClassroom(schoolId,c,termInfoId);
			}
		}
	/**
	 * 查询用户付费状态接口（仅供当前学年学期使用）
	 * @param schoolId
	 * @param userId
	 * @param termInfoId
	 * @return  用户付费状态： 0：无状态（学校不在直通卡名单内，学校在直通卡名单内但角色为非家长/学生）  1：没有付费   2： 已付费
	 *                    -1表示程序执行过程错误
	 * @author zhh
	 */
	public int getUserPrivilegeStatus(long schoolId,long userId){
			if(isCsCurPlate(schoolId)){ //走长沙接口默认为都已付费
				return 2;
			}else{
				return curCommonDataService.getUserPrivilegeStatus(schoolId, userId);
			}
	}
	
	/**
	 * 
	 * @param school
	 * @param cxxnxq
	 * @return
	 */
	public List<Map<String, Object>> getAdminKM(School school, String cxxnxq) {
		List<LessonInfo> list = getLessonInfoList(school, cxxnxq);
		List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
		for (LessonInfo ls : list) {
			// if(ls.getType()==0||ls.getType()==2){

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("zwmc", ls.getName());
			map.put("kmdm", ls.getId() + "");
			rs.add(map);
			// }
		}
		return rs;
	}
	/**
	 * 获取历史权限
	 * @param session
	 * @param cxxnxq
	 * @return
	 *//*
	@SuppressWarnings({ "unchecked", "unused" })
	public HashMap<String, HashMap<String, HashMap<String, JSONObject>>> getHisRightByParam(
			HttpSession session, 
			String cxxnxq) {
		// TODO Auto-generated method stub
		long xxdm = Long.parseLong(session.getAttribute("xxdm").toString());
		School school = this.getSchoolById(xxdm, cxxnxq);
		if(school==null){
			logger.info("【登陆权限】查询"+cxxnxq+"学年学校无数据,学校代码为：" + xxdm);
			return null;
		}
		// 科目列表
		long d20 = new Date().getTime();
		List<LessonInfo> kmList = this.getLessonInfoList(school,cxxnxq);
		long d21 = new Date().getTime();
		logger.info("【登陆权限】查询科目耗时：" + (d21 - d20));
		HashMap<Long, JSONObject> kmMap = new HashMap<Long, JSONObject>();
		for (LessonInfo km : kmList) {
			JSONObject kmObj = new JSONObject();
			kmObj.put("kmdm", km.getId());
			kmObj.put("kmmc", km.getName());
			kmObj.put("kmlx", km.getType());
			kmMap.put(km.getId(), kmObj);
		}
		// 所有菜单
		List<String> appIds = new ArrayList<String>();
		HashMap<Integer, String> appMap = Permisson2AppID.perAppIDMap;
		for (Iterator<Integer> it = appMap.keySet().iterator(); it.hasNext();) {
			int key = it.next();
			appIds.add(appMap.get(key));
		}
		// 年级-名称Map
		Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
		// 年级--使用年级映射
		HashMap<Long, String> njSynjMap = new HashMap<Long, String>();
		HashMap<String, HashMap<String, JSONObject>> allRtMap = new HashMap<String, HashMap<String, JSONObject>>();
		HashMap<String, JSONObject> synjRtMap = new HashMap<String, JSONObject>();

		long d22 = new Date().getTime();
		List<OrgInfo> orgs = (List<OrgInfo>) session.getAttribute("auth.orgList");
		long d23 = new Date().getTime();
		logger.info("【登陆权限】查询机构耗时：" + (d23 - d22));
		// 机构map
		HashMap<Long, OrgInfo> orgMap = new HashMap<Long, OrgInfo>();
		for (OrgInfo org : orgs) {
			orgMap.put(org.getId(), org);
		}
		List<Grade> njList = this.getGradeList(school,cxxnxq);
		long d24 = new Date().getTime();
		logger.info("【登陆权限】查询年级耗时：" + (d24 - d23));
		String xn = cxxnxq.substring(0, 4);
		HashMap<String, String> synjMcMap = new HashMap<String, String>();
		HashMap<Long, String> classMcMap = new HashMap<Long, String>();
		HashMap<Long, Long> classNjMap = new HashMap<Long, Long>();
		HashMap<Long, Grade> levelGradeMap = new HashMap<Long, Grade>();
		HashMap<Integer, Long>  levelGradeIdMap = (HashMap<Integer, Long>) session.getAttribute("auth.levelGradeIdMap");
		List<Long> allClassIds = new ArrayList<Long>();
		for (Grade nj : njList) {
			if (!nj.isGraduate && nj.getClassIds() != null
					&& nj.getClassIds().size() > 0) {
				allClassIds.addAll(nj.getClassIds());
			}
		}
		List<Classroom> bjs = this.getSimpleClassBatch(xxdm, allClassIds,cxxnxq);

		for (Classroom bj : bjs) {
			if (bj.getId() != 0) {

				classMcMap.put(bj.getId(), bj.getClassName());
				classNjMap.put(bj.getId(), bj.getGradeId());
			}
		}
		for (Grade nj : njList) {
			if (nj == null || nj.getCurrentLevel() == null) {
				continue;
			}
			int njdm = nj.getCurrentLevel().getValue();
			levelGradeMap.put(nj.getId(), nj);
			String synj = this.ConvertNJDM2SYNJ(njdm + "", xn);
			long d26 = new Date().getTime();
			njSynjMap.put(nj.getId(), synj);
			List<Long> bjIds = nj.getClassIds();
			if (bjIds == null || nj.isGraduate) {
				logger.error(
						"【登陆权限】班级gradeId:{},【年级名称】:{}的班级为空--null！nj.isGraduate{}",
						nj.getId(), nj.getCurrentLevel(), nj.isGraduate);
				continue;
			}

			long d27 = new Date().getTime();
			logger.info("【登陆权限】查询班级耗时：" + (d27 - d26));
			JSONObject njright = new JSONObject();

			String gradeName = "[" + this.ConvertSYNJ2RXND(synj, xn)
					+ "]" + njName.get(nj.getCurrentLevel());

			synjMcMap.put(synj, gradeName);
			njright.put("gradeName", gradeName);
			HashMap<Long, JSONObject> classList = new HashMap<Long, JSONObject>();
			for (Long clr : bjIds) {
				if (clr != 0 && classMcMap.containsKey(clr)) {
					JSONObject bj = new JSONObject();
					bj.put("className", classMcMap.get(clr));
					bj.put("kmList", kmMap);
					classList.put(clr, bj);
				}
			}

			njright.put("classList", classList);
			njright.put("gradeLevel",  nj.getCurrentLevel());
			synjRtMap.put(synj, njright);
		}
		// 系统管理员权限树
		for (int i = 1000; i <= 1030; i++) {
			allRtMap.put("cs" + i, synjRtMap);
		}
		long accountId = (long) session.getAttribute("accountId");
		// 获取用户权限
		Account act = (Account) session.getAttribute("account");
		List<User> users = act.getUsers();
		List<HashMap<String, HashMap<String, JSONObject>>> rightList = new ArrayList<HashMap<String, HashMap<String, JSONObject>>>();

		boolean isOtherAdmin = false;
		List<String> otherAdminRt = new ArrayList<String>();
		boolean isAdmin = false;
		boolean isCjgly = false;

		List<String> otherRightStr = new ArrayList<String>();

		for (User user : users) {

			long usersysid = user.getUserPart().getId();
			// 任课教师及班主任 通过teacherPart的courseIds、deanOfClassIds来判断
			UserPart up = user.getUserPart();
			int roleType = up.getRole().getValue();

			// 使用年级-班级-科目树
			HashMap<String, JSONObject> tpnjRight = new HashMap<String, JSONObject>();
			// 学生 家长
			if (roleType == T_Role.Parent.getValue()
					|| roleType == T_Role.Student.getValue()) {
				continue;
			} else if (roleType == T_Role.Teacher.getValue()) {
				// 教研组长等 通过userPart获取所属机构信息，再获取orgInfos里的headerIds来判断
				TeacherPart tp = user.getTeacherPart();
				// 如果teacherPart不为空 取其teacherPart部分权限
				if (tp != null) {
					HashMap<Long, JSONObject> tpclaRight = new HashMap<Long, JSONObject>();
					// 班主任 含各科权限
					List<Long> deanOfClass = tp.getDeanOfClassIds();
					List<Course> courseIds = tp.getCourseIds();
					if (deanOfClass != null && deanOfClass.size() != 0) {
						for (Long clas : deanOfClass) {
							if (!classMcMap.containsKey(clas)
									|| clas == 0
									|| !njSynjMap.containsKey(classNjMap
											.get(clas))) {
								continue;
							}
							String synj = njSynjMap.get(classNjMap.get(clas));
							JSONObject claJSON = new JSONObject();
							if (tpnjRight.containsKey(synj)) {
								JSONObject njJSON = tpnjRight.get(synj);
								HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
										.get("classList");
								if (clasRight != null
										&& clasRight.containsKey(clas)) {
									claJSON = clasRight.get(clas);
								}
							}
							claJSON.put("className", classMcMap.get(clas));
							claJSON.put("kmList", kmMap);
							tpclaRight.put(clas, claJSON);
							JSONObject njJSON = new JSONObject();
							if (tpnjRight.containsKey(synj)) {
								njJSON = tpnjRight.get(synj);
								HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
										.get("classList");
								clasRight.put(clas, claJSON);
							} else {
								HashMap<Long, JSONObject> clasRight = new HashMap<Long, JSONObject>();
								clasRight.put(clas, claJSON);
								njJSON.put("classList", clasRight);
							}
							njJSON.put("gradeName", synjMcMap.get(synj));
							tpnjRight.put(synj, njJSON);
						}
					}
					// 任课教师 含部分科目权限
					if (courseIds != null && courseIds.size() != 0) {
						for (Course course : courseIds) {
							if (deanOfClass == null
									|| !deanOfClass.contains(course
											.getClassId())) {
								long lesson = course.getLessonId();
								long classId = course.getClassId();

								if (!classMcMap.containsKey(classId)
										|| classId == 0
										|| !njSynjMap.containsKey(classNjMap
												.get(classId))) {
									continue;
								}
								String synj = njSynjMap.get(classNjMap
										.get(classId));
								JSONObject claJSON = new JSONObject();
								if (tpnjRight.containsKey(synj)) {
									JSONObject njJSON = tpnjRight.get(synj);
									HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
											.get("classList");
									if (clasRight != null
											&& clasRight.containsKey(classId)) {
										claJSON = clasRight.get(classId);
									}
								}
								claJSON.put("className",
										classMcMap.get(classId));
								HashMap<Long, JSONObject> kmArr = new HashMap<Long, JSONObject>();
								if (claJSON.containsKey("kmList")) {
									kmArr = (HashMap<Long, JSONObject>) claJSON
											.get("kmList");
								} else {
									claJSON.put("kmList", kmArr);
								}
								if (kmMap.get(lesson) == null) {
									continue;
								}
								kmArr.put(lesson, kmMap.get(lesson));

								JSONObject njJSON = new JSONObject();
								if (tpnjRight.containsKey(synj)) {
									njJSON = tpnjRight.get(synj);
									HashMap<Long, JSONObject> clasRight = (HashMap<Long, JSONObject>) njJSON
											.get("classList");
									clasRight.put(classId, claJSON);
								} else {
									HashMap<Long, JSONObject> clasRight = new HashMap<Long, JSONObject>();
									clasRight.put(classId, claJSON);
									njJSON.put("classList", clasRight);
								}
								njJSON.put("gradeName", synjMcMap.get(synj));
								tpnjRight.put(synj, njJSON);
							}
						}
					}
				}

			} else if (roleType == T_Role.Staff.getValue()) {
				// 教研组长 年级组长 备课组长等
				List<Long> orgIds = user.getUserPart().getDeanOfOrgIds();
				if (orgIds == null) {
					logger.error("【登陆权限】查询用户所管辖机构为空！");
					continue;
				}
				for (Long orgId : orgIds) {
					if (orgId == 0) {
						continue;
					}
					OrgInfo org = orgMap.get(orgId);
					if (org == null) {
						continue;
					}
					// 人员为该机构管理员
					// 根据机构类型来记录机构权限
					int orgType = org.orgType;
					if (orgType == T_OrgType.T_Teach.getValue()) {
						// 教研组 所有年级 一个科目
						long lessId = org.getLessonIds().get(0);
						HashMap<Long, JSONObject> kmArr = new HashMap<Long, JSONObject>();
						kmArr.put(lessId, kmMap.get(lessId));
						HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
						for (Iterator<String> synj = synjRtMap.keySet()
								.iterator(); synj.hasNext();) {
							String synjStr = synj.next();
							JSONObject synjObj = synjRtMap.get(synjStr);

							HashMap<Long, JSONObject> classList = (HashMap<Long, JSONObject>) synjObj
									.get("classList");
							HashMap<Long, JSONObject> classListClone = new HashMap<Long, JSONObject>();

							for (Iterator<Long> classId = classList.keySet()
									.iterator(); classId.hasNext();) {
								long ClassId = classId.next();
								JSONObject classObj = classList.get(ClassId);
								JSONObject classObjClone = new JSONObject();
								classObjClone.put("className",
										classObj.get("className"));
								classObjClone.put("kmList", kmArr);
								classListClone.put(ClassId, classObjClone);
							}

							JSONObject synjObjClone = new JSONObject();
							synjObjClone.put("gradeName",
									synjObj.get("gradeName"));
							synjObjClone.put("classList", classListClone);
							synjRtMapClone.put(synjStr, synjObjClone);
						}

						tpnjRight = synjRtMapClone;

					} else if (orgType == T_OrgType.T_Grade.getValue()) {
						// 年级组 一个年级 所有科目
						int orgLevel = org.getScopeTypes().get(0).getValue();
						if(!levelGradeIdMap.containsKey(orgLevel)){
							continue;
						}
						Grade gd = levelGradeMap.get(levelGradeIdMap.get(orgLevel));
						if (gd != null) {

							HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
							synjRtMapClone.put(njSynjMap.get(gd.getId()),
									synjRtMap.get(njSynjMap.get(gd.getId())));

							tpnjRight = synjRtMapClone;
						}
					} else if (orgType == T_OrgType.T_PreLesson.getValue()) {
						// 备课组 一个年级 一个科目
						long lessId = org.getLessonIds().get(0);
						int orgLevel = org.getScopeTypes().get(0).getValue();
						if(!levelGradeIdMap.containsKey(orgLevel)){
							continue;
						}
						Grade gd = levelGradeMap.get(levelGradeIdMap.get(orgLevel));

						HashMap<Long, JSONObject> kmArr = new HashMap<Long, JSONObject>();
						kmArr.put(lessId, kmMap.get(lessId));

						HashMap<String, JSONObject> synjRtMapClone = new HashMap<String, JSONObject>();
						String synjStr = njSynjMap.get(gd.getId());
						JSONObject synjObj = synjRtMap.get(synjStr);

						HashMap<Long, JSONObject> classList = (HashMap<Long, JSONObject>) synjObj
								.get("classList");
						HashMap<Long, JSONObject> classListClone = new HashMap<Long, JSONObject>();

						for (Iterator<Long> classId = classList.keySet()
								.iterator(); classId.hasNext();) {
							long ClassId = classId.next();
							JSONObject classObj = classList.get(ClassId);
							JSONObject classObjClone = new JSONObject();
							classObjClone.put("className",
									classObj.get("className"));
							classObjClone.put("kmList", kmArr);
							classListClone.put(ClassId, classObjClone);
						}

						JSONObject synjObjClone = new JSONObject();
						synjObjClone.put("gradeName", synjObj.get("gradeName"));
						synjObjClone.put("classList", classListClone);
						synjRtMapClone.put(synjStr, synjObjClone);

						tpnjRight = synjRtMapClone;
					}
				}

			} else if (roleType == T_Role.SchoolManager.getValue()) {
				// 成绩管理员 /系统管理员等
				isOtherAdmin = true;
				long perLevel = 0;
				long d33 = new Date().getTime();
				UserPermissions upers = this.getUserPermissionById(xxdm,
						usersysid,
						session.getAttribute("accountId").toString());
				long d34 = new Date().getTime();
				logger.info("【登陆权限】查询userPermission耗时：" + (d34 - d33));
				if (upers == null) {
					logger.error("【登陆权限】查询userPermission为空：" + upers);
					continue;
				}
				List<PermissionTemplateIns> ps = upers
						.getPermissionTemplateInss();
				for (PermissionTemplateIns p : ps) {
					if (p == null || p.getLevelv2() == 0) {
						continue;
					}
					perLevel = p.getLevelv2();
					if (perLevel == T_PermissionLevel.Level1.getValue()) {
					} else {
						otherAdminRt.add(perLevel+"");
						otherRightStr.add(perLevel+"");
						if (perLevel == T_PermissionLevel.Level3.getValue()) {
							// 成绩管理员
							isCjgly = true;
						}
					}
				}

			}
			if (isAdmin) {
				break;
			} else {
				HashMap<String, HashMap<String, JSONObject>> curUserRt = new HashMap<String, HashMap<String, JSONObject>>();
				if(isOtherAdmin){
					 for(String permissionStr:otherAdminRt){
						 String csPermissionStr= "cs"+permissionStr;
						 curUserRt.put(csPermissionStr, allRtMap.get(csPermissionStr));
					 }
				}
				 
			}

		}
		HashMap<String, HashMap<String, HashMap<String, JSONObject>>> xnxqRightTreeMap = new HashMap<String, HashMap<String, HashMap<String, JSONObject>>>();
		if (session.getAttribute("rightTree") != null) {
			xnxqRightTreeMap = (HashMap<String, HashMap<String, HashMap<String, JSONObject>>>) session
					.getAttribute("rightTree");
		}
		if (rightList.size() > 0) {

			HashMap<String, HashMap<String, JSONObject>> partRight = MergeAppRight(rightList);
			xnxqRightTreeMap.put(cxxnxq, partRight);
			session.setAttribute("rightTree", xnxqRightTreeMap);
		}

		System.out.println("构建" + cxxnxq + "session树完成！");

		return xnxqRightTreeMap;
		return null;
	}*/

	
	/**
	 * 逐级-合并权限 app-synj-classId-kmList
	 * 
	 * @param rightList
	 * @return
	 */
	private HashMap<String, HashMap<String, JSONObject>> MergeAppRight(
			List<HashMap<String, HashMap<String, JSONObject>>> rightList) {
		// TODO Auto-generated method stub
		HashMap<String, HashMap<String, JSONObject>> rs = new HashMap<String, HashMap<String, JSONObject>>();

		for (HashMap<String, HashMap<String, JSONObject>> appRight : rightList) {
			if (appRight != null && rightList != null) {
				rs = MergeTool.MergeAppLeftRight(rs, appRight);
			}
		}

		return rs;
	}
	
	/**
	 * 逐级-合并权限 app-synj-classId-kmList
	 * 
	 * @param rightList
	 * @return
	 */
	public JSONObject getGroupsLevelByStudent(School school,String xnxq,String type,String granule) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
	/*	String type = requestParams.getString("type").trim();
		String granule = requestParams.getString("granule").trim();*/
		if(org.apache.commons.lang.StringUtils.isBlank(granule)){
			granule = "1";
		}
		if(org.apache.commons.lang.StringUtils.isBlank(type)){
			type = "1";
		}
		/*String xnxq = requestParams.getString("termInfoId").trim();*/
		/*if(xnxq == null || xnxq.length() == 0){
		    xnxq = getCurXnxq(req);
		}*/
		/*School school = getSchool(req);*/
		try {
			List<Grade> gradeList = curCommonDataService.getGradeList(school);
			Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
			Map<String, String> gradeMap = new HashMap<String, String>();
			List<Long> classIds = new ArrayList<Long>();
			if(CollectionUtils.isNotEmpty(gradeList)){
				for (Grade grade : gradeList) {
					JSONObject jsonObject = new JSONObject();
					String convertNJDM2SYNJ = commonDataService.ConvertNJDM2SYNJ(String.valueOf(grade.getCurrentLevel().getValue()),xnxq.substring(0, 4));
					String string = njName.get(grade.getCurrentLevel());
					gradeMap.put(String.valueOf(grade.getId()), convertNJDM2SYNJ);
					Iterator<Long> classIdsIterator = grade.getClassIdsIterator();
					while (classIdsIterator.hasNext()) {
						Long classId = (Long) classIdsIterator.next();
						classIds.add(classId);
					}
					jsonObject.put("id", grade.getId());
					jsonObject.put("text", string);
					jsonObject.put("state", "closed");
					jsonObject.put("children", new ArrayList<JSONObject>());
					rs.add(jsonObject);
				}
			}
			Map<String, List<JSONObject>> gradeClassMap = new HashMap<String, List<JSONObject>>();
			List<Long> accountIds = new ArrayList<Long>();
			if(CollectionUtils.isNotEmpty(classIds)){
				List<Classroom> classroomBatch = curCommonDataService.getClassroomBatchNoAccount(school.getId(), classIds);
				if(CollectionUtils.isNotEmpty(classroomBatch)){
					for (Classroom classroom : classroomBatch) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("id", classroom.getId());
						jsonObject.put("text", classroom.getClassName());
						if("1".equals(granule)){
							JSONObject classObj = new JSONObject();
							classObj.put("id", classroom.getId());
							classObj.put("className", classroom.getClassName());
							classObj.put("gradeId", gradeMap.get(String.valueOf(classroom.getGradeId())));
							Integer count = 0;
							if("1".equals(type)){
								if(CollectionUtils.isNotEmpty(classroom.getStudentIds())){
									count = classroom.getStudentIds().size();
								}
							}else{
								if(CollectionUtils.isNotEmpty(classroom.getParentIds())){
									count = classroom.getParentIds().size();
								}
							}
							classObj.put("count", count);
							jsonObject.put("attributes", classObj);
						}else{
							jsonObject.put("state", "closed");
							jsonObject.put("children", new ArrayList<JSONObject>());
							List<Long> ids = null;
							if("1".equals(type)){
								if(CollectionUtils.isNotEmpty(classroom.getStudentIds())){
									ids = classroom.getStudentIds();
									for (Long id : ids) {
										if(!accountIds.contains(id)){
											accountIds.add(id);
										}
									}
								}
							}else{
								if(CollectionUtils.isNotEmpty(classroom.getParentIds())){
									ids = classroom.getParentIds();
									for (Long id : ids) {
										if(!accountIds.contains(id)){
											accountIds.add(id);
										}
									}
								}
							}
						}
						String gradeId = String.valueOf(classroom.getGradeId());
						if(gradeClassMap.containsKey(gradeId)){
							gradeClassMap.get(gradeId).add(jsonObject);
						}else{
							gradeClassMap.put(gradeId, new ArrayList<JSONObject>());
							gradeClassMap.get(gradeId).add(jsonObject);
						}
					}
				}
			}
			if("1".equals(granule)){
				if(CollectionUtils.isNotEmpty(rs)){
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						List<JSONObject> list = gradeClassMap.get(gradeId);
						jsonObject.put("children", list);
					}
				}
				if(CollectionUtils.isNotEmpty(rs)){
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						jsonObject.put("id", gradeMap.get(gradeId));
					}
				}
			}else{
				Map<String, List<JSONObject>> classStuMap = new HashMap<String, List<JSONObject>>();
				List<Account> accountBatch = curCommonDataService.getAccountBatch(school.getId(), accountIds);
				if(CollectionUtils.isNotEmpty(accountBatch)){
					for (Account account : accountBatch) {
						if(CollectionUtils.isNotEmpty(account.getUsers())){
							User user = account.getUsers().get(0);
							long classId = user.getStudentPart().getClassId();
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("id", account.getId());
							jsonObject.put("text", account.getName());
							JSONObject sutObj = new JSONObject();
							sutObj.put("id", account.getId());
							sutObj.put("studentName", account.getName());
							sutObj.put("classId", classId);
							jsonObject.put("attributes", sutObj);
							if(classStuMap.containsKey(String.valueOf(classId))){
								classStuMap.get(String.valueOf(classId)).add(jsonObject);
							}else{
								classStuMap.put(String.valueOf(classId), new ArrayList<JSONObject>());
								classStuMap.get(String.valueOf(classId)).add(jsonObject);
							}
						}
					}
				}
				if(!gradeClassMap.isEmpty()){
					Set<Entry<String, List<JSONObject>>> entrySet = gradeClassMap.entrySet();
					Iterator<Entry<String, List<JSONObject>>> iterator = entrySet.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, List<JSONObject>> entry = (Map.Entry<String, List<JSONObject>>) iterator.next();
						List<JSONObject> value = entry.getValue();
						if(CollectionUtils.isNotEmpty(value)){
							for (JSONObject jsonObject : value) {
								jsonObject.put("children", jsonObject.getString("id"));
							}
						}
					}
				}
				if(CollectionUtils.isNotEmpty(rs)){
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						List<JSONObject> list = gradeClassMap.get(gradeId);
						jsonObject.put("children", list);
					}
				}
				if(CollectionUtils.isNotEmpty(rs)){
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						jsonObject.put("id", gradeMap.get(gradeId));
					}
				}
			}
		} catch (Exception e) {
			json.put("code", "-1");
			json.put("msg", "查询错误");
			e.printStackTrace();
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}
	/**
	 * 如果传入的termInfoId大于当前则取当前学年学期
	 * 传入的termInfoId小于20151则取20151
	 * @param termInfoId
	 * @return
	 * @author zhh
	 */
    public String judgeTermInfoId(String termInfoId,long schoolId){
    	if(StringUtils.isEmpty(termInfoId)|| termInfoId==""|| termInfoId==null){
    		return "";
    	}
    	String curTermInfoId = getCurTermInfoId(schoolId);
    	if(StringUtils.isEmpty(curTermInfoId)|| curTermInfoId==""|| curTermInfoId==null){
    		return "";
    	}
    	int intCurT = Integer.parseInt(curTermInfoId);
    	int intT=Integer.parseInt(termInfoId);
    	//无这种可能 ，因为满足条件的情况下，进的是当前学年学期调用
    	/*if(intT > intCurT){
    		return curTermInfoId;
    	}*/
    	
    	if(intT<20151){
    		return "20151";
    	}
    	return termInfoId;
    }
	
    /**
	 * 通过redis获取当前学年学期
	 * @return
	 */
	public String getCurTermInfoId(long schoolId)
	{
		String xnxq="";
		try 
		{
			Object key = "common."+schoolId+".00.currentTermInfo";
			if(null!=redisOperationDAO.get(key))
			{
				xnxq=redisOperationDAO.get(key).toString();
			}
			else
			{
				//logger.info("AllCommonDataService.getCurTermInfoId方法中redisOperationDAO获取CurXnxq为null！,入参schoolId:{},学年学期取配置文件中currentTermInfo的值:{}",schoolId,currentTermInfo);
				xnxq=currentTermInfo;
			}
			if(StringUtils.isEmpty(xnxq))
			{
				//logger.info("AllCommonDataService.getCurTermInfoId方法中redisOperationDAO获取CurXnxq为空字符串！,入参schoolId:{},学年学期取配置文件中currentTermInfo的值:{}",schoolId,currentTermInfo);
				xnxq=currentTermInfo;
//				throw new Exception("AllCommonDataService方法中getCurTermInfoId得到学校："+schoolId+"的学年学期值为空字符串！");
			
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("AllCommonDataService.getCurTermInfoId方法报错！入参schoolId:{}",schoolId);
			e.printStackTrace();
		}
		
		return xnxq;
	}
	
	/**
	 * 判断传入的学年学期和当前学年学期的大小，如果小于，则调用历史接口，反之，调用当前接口
	 * @param schoolId
	 * @param termInfoId
	 * @return true or false
	 */
	private boolean isCurTermInfo(long schoolId,String termInfoId)
	{
		if(Integer.valueOf(termInfoId)<Integer.valueOf(this.getCurTermInfoId(schoolId)))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	/**
	 * 是否为长沙平台 ，若是则从长沙基础数据库取数据
	 * @param schoolId
	 * @return true 是   false 否
	 */
	private boolean isCsCurPlate(long schoolId){
//		Object schoolPlateKey = "common."+schoolId+".00.schoolPlate";
//		Object schoolPlateObj = null;
//		try {
//			 schoolPlateObj = redisOperationDAO.get(schoolPlateKey);
//		} catch (Exception e) {
//			logger.error("AllCommonDataService.isCsCurPlate方法出错，入参schoolId:{},出参schoolPlateObj:{}",schoolId,schoolPlateObj);
//			e.printStackTrace();
//		}
//		logger.info("AllCommonDataService.isCsCurPlate方法正常，schoolPlateKey:{},入参schoolId:{},出参schoolPlateObj:{}",schoolPlateKey,schoolId,schoolPlateObj);
//		if(schoolPlateObj==null){ //|| "50513".equals(schoolId+"")
//			return false; //是长沙则不可能插入该表中  ，则走深圳SDK
//		}else{
			return true;
//		}
	
	}
	public List<Map<String, Object>> getSystemVersion() {
		return curCommonDataService.getSystemVersion();
	}
	
	/**
	 * 获取当前学年学期
	 * 
	 * @param schoolId
	 *            学校id
	 * @return CurrentTermRsp
	 */
	public CurrentTermRsp getCurTermInfo(long schoolId) {
		if(isCsCurPlate(schoolId)){ 
			String termInfoId = getCurTermInfoId(schoolId);
			if(org.apache.commons.lang.StringUtils.isNotBlank(termInfoId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("termInfoId", termInfoId);
				return csCurCommonDataService.getCurTermInfo(param);
			}
		}else{
           return curCommonDataService.getCurTermInfo(schoolId);
		}
		return null;	
	}

	/**
	 * 获取格式为 20151 格式的学年学期（登录时用的）
	 * @param School	学校实例
	 * @return
	 */
	public String getCurrentXnxq(School sch){
		if(sch==null ||sch.getId()==0){
			return null;
		}
		if(isCsCurPlate(sch.getId())){
			String termInfoId = getCurTermInfoId(sch.getId());
			if(org.apache.commons.lang.StringUtils.isNotBlank(termInfoId)){
				JSONObject param = new JSONObject();
				param.put("schoolId", sch.getId());
				param.put("termInfoId", termInfoId);
				param.put("school", sch);
				return csCurCommonDataService.getCurrentXnxq(param);
			}
		}else{
			return curCommonDataService.getCurrentXnxq(sch);	
		}
		return null;
		
	}
	/**
	 * 获取学期列表
	 * 
	 * @param termId
	 *            学年学期ID
	 * @return TermInfo
	 */
	public List<TermInfo> getTermInfoBatch(List<Long> termIds, long schoolId) {
		if(isCsCurPlate(schoolId)){ 
			String termInfoId = getCurTermInfoId(schoolId);
			if(org.apache.commons.lang.StringUtils.isNotBlank(termInfoId)){
				return csCurCommonDataService.getTermInfoBatch(termIds, schoolId,termInfoId);
			}
		}else{
			return curCommonDataService.getTermInfoBatch(termIds, schoolId);	
		}
		return null;
	}
	
	/**
	 * 得到培养层次通过年级代码
	 * 
	 * @param njCode
	 *            Grade的gradeLevel的值
	 * @return
	 */
	public Integer getPYCCByNJCode(long njCode) {
		return commonDataService.getPYCCByNJCode(njCode);
	}

	public Integer getQSNJByPYCC(int pycc) {
		return commonDataService.getQSNJByPYCC(pycc);
	}

	/**
	 * 得到所有的班级类型，深圳的枚举，
	 * 
	 * @return
	 */
	public List<TDmBjlx> getClassTypeList() {
		return commonDataService.getClassTypeList();
	}

	/**
	 * （使用年级、学年）转入学年度
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            学年
	 * @return 入学年度
	 */
	public String ConvertSYNJ2RXND(String synj, String xn) {
		return commonDataService.ConvertSYNJ2RXND(synj,xn);
	}

	/**
	 * （入学年度、培养层次）转使用年级
	 * 
	 * @param rxnd
	 *            入学年度
	 * @param pycc
	 *            培养层次
	 * @return 使用年级
	 */
	public String ConvertRXND2SYNJ(String rxnd, String pycc) {

		return commonDataService.ConvertRXND2SYNJ(rxnd,pycc);
	}

	/**
	 * （使用年级、学年）转年级代码
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            当前学年
	 * @return 年级代码 为Grade的gradeLevel的值  currentLevel 注释by:zhh
	 */
	public String ConvertSYNJ2NJDM(String synj, String xn) {

		return commonDataService.ConvertSYNJ2NJDM(synj,xn);
	}

	/**
	 * (年级代码、学年）转使用年级
	 * 
	 * @param njdm
	 *            年级代码 Grade.getCurrentLevel.getValue()的值  注释by:zhh
	 * @param xn
	 *            当前学年
	 * @return 使用年级
	 */
	public String ConvertNJDM2SYNJ(String njdm, String xn) {

		return commonDataService.ConvertNJDM2SYNJ(njdm,xn);
	}

	/**
	 * （入学年度、学年、培养层次）转年级代码
	 * 
	 * @param rxnd
	 *            入学年度
	 * @param xn
	 *            当前学年
	 * @param pycc
	 *            培养层次
	 * @return 年级代码 为Grade的gradeLevel的值 currentLevel  注释by:zhh
	 */
	public String ConvertRXND2NJDM(String rxnd, String xn, String pycc) {

		return commonDataService.ConvertRXND2NJDM(rxnd,xn,pycc);
	}

	/**
	 * （年级代码、学年）转入学年度
	 * 
	 * @param njdm
	 *            年级代码 Grade的gradeLevel的值 currentLevel
	 * @param xn
	 *            当前学年
	 * @return 入学年度
	 */
	public String ConvertNJDM2RXND(String njdm, String xn) {

		return commonDataService.ConvertNJDM2RXND(njdm,xn);

	}

	/**
	 * （使用年级、学年）获取培养层次
	 * 
	 * @param synj
	 *            使用年级
	 * @param xn
	 *            学年
	 * @return 培养层次
	 */
	public String getPYCCBySYNJ(String synj, String xn) {

		return commonDataService.getPYCCBySYNJ(synj,xn);

	}
	
	public List<JSONObject> getUserIdByExtId(String extId,String currentXnxq) {
		return csCurCommonDataService.getUserIdByExtId(extId,currentXnxq);
	}
	public List<JSONObject> getUserIdByExtIdRole(String extId,String currentXnxq, int role) {
		return csCurCommonDataService.getUserIdByExtIdRole(extId,currentXnxq,role);
	}
	public List<JSONObject> getUserIdByConditon(JSONObject param) {
		return csCurCommonDataService.getUserIdByConditon(param);
	}
	public String getSchoolIdByExtId(String extSchoolId, String termInfoId) {
		return csCurCommonDataService.getSchoolIdByExtId(extSchoolId,termInfoId);
	}
	/**
	 * 根据appId获取模块管理员列表
	 * @param appId
	 * @param termInfoId
	 * @return userId   用户的userId
	 */
	public List<Long>  getModuleManagers(String appId ,String termInfoId,long schoolId){
		if(StringUtils.isEmpty(appId)||schoolId==0|| StringUtils.isEmpty(termInfoId)){
			return null;
		}
		//返回业务管理员userIds
		appId = appId.substring(2, appId.length());
		System.out.println(appId);
		return csCurCommonDataService.getModuleManagers(appId, termInfoId,schoolId);
	}
	public List<JSONObject> getExtLessonIdBySchoolId(String termInfoId,long schoolId){
		return csCurCommonDataService.getExtLessonIdBySchoolId( termInfoId,schoolId);
	}
	/**
	 * 模板消息专用 
	 * 根据学生id获取家长信息发送模板消息
	 * @param accountId 学生accountId
	 * @param termInfoId  学年学期
	 * @param schoolId 学校id
	 * @return  { name:"", extUserId:"account表里面的id",studentName:"",studentAccountId:""}
	 */
	public List<JSONObject>  getSimpleParentByStuMsg(List<Long> accountIds,String termInfoId,long schoolId) {
		termInfoId = judgeTermInfoId(termInfoId,schoolId );
		return csCurCommonDataService.getSimpleParentByStuMsg(accountIds,termInfoId,schoolId);	
	}
	
}
