package com.talkweb.commondata.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
/**
 * 提供基础数据接口供长沙使用通过调用封装的基础数据接口
 * @author zxy
 *
 */
//@Service
public class BaseInterfaceServicebak{
	@Autowired
	private CommonDataServicebak commonDataService=new CommonDataServicebak();
	private static final Logger logger = LoggerFactory.getLogger(BaseInterfaceServicebak.class);
	
	/**
	 * 获取任课教师
	 * 
	 * @param map
	 * schoolId 学校ID（必传） 
	 * lessonId 科目ID，多个使用逗号分隔
	 * classId 班级id,多个使用逗号分隔，（如果有班级ID，则忽略其他条件）
     * termInfoId 学年学期（必传） 
     * usedGradeId 使用年级[可以多个逗号隔开] name 教师姓名
	 * @return JSONArray
	 * {"lessonName":"英语","lessonId":750,"classId":10033,"teaId":108688,"teaName":"白华"}
	 */
	public JSONArray getCourseTeacherList(School school,HashMap<String, Object> map){
		if(null==map.get("schoolId")||StringUtils.isEmpty(map.get("schoolId")))
		{
			return new JSONArray();
		}
		List<LessonInfo> lessonList=commonDataService.getLessonInfoList(school);
		HashMap<Long,String> lessonMap=new HashMap<Long,String>();
		for(LessonInfo le: lessonList)
		{
			lessonMap.put(le.getId(), le.getName());
		}
		
		List<Account> accountList=commonDataService.getTeacherList(map);
		JSONArray arr=new JSONArray();
		for(Account a:accountList)
		{
			if(a.getUsers()==null){
		       continue;
		    }
			for(User u:a.getUsers())
			{
 				if(u==null||u.getUserPart()==null||u.getUserPart().getRole()==null||u.getTeacherPart()==null){
 					continue;
 				}
				if(u.getUserPart().getRole().equals(T_Role.Teacher))
				{
					List<Course> clist=	u.getTeacherPart().getCourseIds();
					if(null!=clist&&clist.size()>0)
					{
						Long tId=a.getId();
						String teaName=a.getName();
						for(Course c:clist)
						{
							JSONObject obj=new JSONObject();
							obj.put("classId", c.getClassId());
							obj.put("lessonId", c.getLessonId());
							obj.put("teaId", tId);
							obj.put("teaName", teaName);
							obj.put("lessonName", lessonMap.get(c.getLessonId()));				
							arr.add(obj);
						}						
					}
					break;
				}
			}

		}
		return arr;
	}
	
	/**
	 * 查询一个学校所有的学生
	 * @param schoolId 学校id
	 * @return
	 */
	public List<Account> getAllStudent(School school) {	  
		long d1 = new Date().getTime();
		long schoolId=school.getId();
	    List<Grade> gradeList=commonDataService.getGradeBatch(schoolId,school.getGrades());
	    List<Long> cids=new ArrayList<Long>();
	    for(Grade g:gradeList)
	    {
	    	if(g!=null&&g.getClassIds()!=null){
	    		
	    		cids.addAll(g.getClassIds());
	    	}
		}
	    List<Classroom> clist=commonDataService.getClassroomBatch(schoolId,cids);
	    List<Long> stuids=new ArrayList<Long>();
	    for(Classroom c:clist)
	    {
	    	if(null!=c.getStudentAccountIds())
	    	{
		    	stuids.addAll(c.getStudentAccountIds());
	    	}
	    }
	    List<Account> stu=commonDataService.getAccountBatch(schoolId,stuids);
	    
	    long d2 = new Date().getTime();
	    logger.info("【基础数据】查询组装学生列表，个数:"+stu.size()+"耗时:"+(d2-d1));	    
		return stu;
	}
	
	/**
	 * 查询一个学校所有的班级
	 * @param school 学校对象
	 * @return
	 */
	public List<Classroom> getAllClass(School school) {	 
		List<Grade> gradeList=commonDataService.getGradeList(school);
	    List<Long> cids=new ArrayList<Long>();
	    for(Grade g:gradeList)
	    {
	    	if(null!=g && null !=g.getClassIds())
	    	{
	    		cids.addAll(g.getClassIds());
	    	}
	    	
		}
	    return commonDataService.getClassroomBatch(school.getId(),cids);	    
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
	 * 查询老师 所属机构 （多个老师list）
	 * @param tlist
	 * @param SchoolId
	 * @return List<JSONObject> 
	 */
	public List<JSONObject> getOrgTeacherList(List<Account> tlist,School school){
		List<OrgInfo> orgs = commonDataService.getSchoolOrgList(school);
		List<JSONObject> dalist=new ArrayList<JSONObject>();
		for(Account a:tlist){
			JSONObject data=new JSONObject();
			HashSet<String> gName = new HashSet<String>();
			HashSet<String> rName = new HashSet<String>();
			List<User> ulist=null;
			if(null==a.getUsers() && null!=a.getUserIds())
			{
				ulist=commonDataService.getUserBatch(school.getId(), a.getUserIds());
			}
			else
			{
				ulist=a.getUsers();
			}
			
			if (null != ulist) {
				for (User u : ulist) {
					if (u == null || u.getUserPart() == null || u.getUserPart().getRole() == null) {
						continue;
					}
					if (u.getUserPart().getRole().equals(T_Role.Teacher)) {
						List<Long> orgIds = u.getUserPart().getOrgIds();
						if (orgIds != null) {
							for (Long orgId : orgIds) {
								for (OrgInfo org : orgs) {
									if (orgId == org.getId()) {// 找到对应的机构
										int orgType = org.orgType;// 结构类型
										if (orgType == T_OrgType.T_Teach.getValue()) {// 教研组
											rName.add(org.getOrgName());
										} else if (orgType == T_OrgType.T_Grade.getValue()) {// 年级组
											gName.add(org.getOrgName());
										}

									}
								}
							}
						}

					}
				}
			}

			data.put("teacherId", a.getId());
			data.put("gradeName", gName);
			data.put("reaName", rName);
			dalist.add(data);
		}
		return dalist;
	}

}
