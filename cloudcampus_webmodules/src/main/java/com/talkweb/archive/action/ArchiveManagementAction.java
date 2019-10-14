package com.talkweb.archive.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.archive.service.ArchiveManagementService;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;

import net.sourceforge.pinyin4j.PinyinHelper;


@RequestMapping("/archiveManage")
@Controller
public class ArchiveManagementAction  extends BaseAction {
 
	@Autowired
	private ArchiveManagementService archiveManagementService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
 
	@RequestMapping(value="/getRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res) {
		
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = getXxdm(req) ;
		String termInfoId = getCurXnxq(req);
		param.put("schoolId", schoolId);
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));

		param.put("teacherId", accountId);
 		boolean flag = isMoudleManager(req, "cs1042");
		if(flag){
			response.put("isAdmin", 1);
		}else {
			response.put("isAdmin", 0);
		}
 
		 Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
		 String userName = account.getName();
		 response.put("userName", userName);
		 List<User> userList = account.getUsers();
		 if (userList!=null) {
			 for (int i = 0; i < userList.size(); i++) {
				 User user = userList.get(i);
				if (user.getUserPart().getRole() ==T_Role.Teacher ) {
					 response.put("isTeacher", 1);
				}
			}
		}
		 
		 if (response.getInteger("isTeacher")==null) {
			 response.put("isTeacher", 0);
		 }
 
		setPromptMessage(response, "1", "查询成功");
		return response;
	}

	@RequestMapping(value="/getAllTeachers",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllTeachers(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		
		String schoolId = getXxdm(req) ;
 		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("selectedSemester");
			if(StringUtils.isEmpty(termInfoId)){
				termInfoId = getCurXnxq(req);
			}
			JSONObject param = new JSONObject();
			param.put("selectedSemester", termInfoId);
			param.put("teacherName", request.getString("teacherName"));
			param.put("schoolId", schoolId);
			List<JSONObject> data = archiveManagementService.getAllTeacherList(param);
			int page = request.getIntValue("page");
			int pageSize = request.getIntValue("pageSize");
			pageSize = pageSize<=0?8:pageSize;
			page = page<=0 ? 1:page;
			int position = (page -1 ) * pageSize;
			param.put("position", position);
			param.put("pageSize", pageSize);
			
			int rowCnt = 0;
			
			List<JSONObject> teacherList = new ArrayList<JSONObject>();
			if (data!=null) {
				rowCnt = data.size();
				 Collections.sort( data , new Comparator<JSONObject>(){
					  public int compare(JSONObject p1, JSONObject p2) { 
				            return (getPinYinHeadChar(p1.getString("teacherName"))).compareTo(getPinYinHeadChar(p2.getString("teacherName")) );  
				        }  
				});
				
					List<Long> idList = new ArrayList<Long>();
					 int cnt = position + pageSize > data.size()?data.size():position + pageSize ;
					for (int i = position ; i < cnt ; i++) {
						idList.add(data.get(i).getLong("teacherId") );
					}
				 
 
				List<Account> list = commonDataService.getAccountBatch(Long.parseLong(schoolId), idList, termInfoId);
				if (list!=null) {
					Account account = null;
					   for (int i = 0; i < list.size(); i++) {
						    account = list.get(i);
							JSONObject object = new JSONObject();
							object.put("teacherId", account.getId());
							if (account.getGender()!=null) {
								if (1 == account.getGender().getValue()) {
									object.put("gender", "男");
								}else {
									object.put("gender", "女");
								}
							}else {
								    object.put("gender", "未知");
							}
							object.put("name", account.getName());
						 
							object.put("mobilePhone", account.getMobilePhone()==null?"":account.getMobilePhone());
							List<User> users = account.getUsers(); 
							if (users!=null && users.size() > 0) {
								User user = null;
								for (int j = 0; j < users.size(); j++) {
									user = users.get(j);
									 if (user.getUserPart().getRole() ==T_Role.Teacher ) {
											object.put("avatar", user.getUserPart().getAvatar() );
											break;
									 }
								}
							
							}
						
							teacherList.add(object);
					   }
					   
					    Collections.sort( teacherList , new Comparator<JSONObject>(){
							  public int compare(JSONObject p1, JSONObject p2) { 
						            return (getPinYinHeadChar(p1.getString("name"))).compareTo(getPinYinHeadChar(p2.getString("name")) );  
						        }  
						});
					    response.put("data", teacherList) ;
				}
				
	    		response.put("page", page);
	    		response.put("pageSize", pageSize);
	    		response.put("rowCnt", rowCnt);
	    		if (pageSize > 0 ) {
	    			response.put("pageCnt", rowCnt/pageSize + (rowCnt%pageSize == 0? 0 : 1 ) );
	    		}
				
				setPromptMessage(response, "1", "查询成功");
			}else {
				setPromptMessage(response, "-1", "查询失败");
			}

		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败");
			e.printStackTrace();
		}
		return response;
		
	 
	}
 
	@RequestMapping(value="/getTeacherBasicInformation",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject  getTeacherBasicInformation(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		    String schoolId = getXxdm(req) ;
			String termInfoId = getCurXnxq(req);
		    JSONObject response = new JSONObject();
            String accountId = request.getString("teacherId");
            if (StringUtils.isBlank(accountId)) {
            	accountId= String.valueOf((Long)req.getSession().getAttribute("accountId"));
			}
            Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
            JSONObject object = new JSONObject();
        	School school = this.getSchool(req,termInfoId);
        	if (account.getGender()!=null) {
				if (1 == account.getGender().getValue()) {
					object.put("gender", "男");
				}else {
					object.put("gender", "女");
				}
			}else {
				    object.put("gender", "未知");
			}
        	object.put("name", account.getName());
        	object.put("mobilePhone", account.getMobilePhone()==null?"":account.getMobilePhone());
        	List<User> userList = account.getUsers();
        	List<JSONObject> teachingRalation = new ArrayList<JSONObject>();
        	Map<Long, String> lessonMap = new HashMap<Long, String>();
        	Map<Long, String> classroomMap = new HashMap<Long, String>();
        	Map<Long, String> gradeMap = new HashMap<Long, String>();
        	Map<Long, Long> classGradeIdMap = new HashMap<Long, Long>();
        	Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
            if (userList!=null) {
				for (int i = 0; i < userList.size(); i++) {
					 User user = userList.get(i);
						if (user.getUserPart().getRole() ==T_Role.Teacher ) {
							List<Course> courseList = user.getTeacherPart().getCourseIds();
							List<Long> lessonIds = new ArrayList<Long>();
							List<Long> classIds = new ArrayList<Long>();
							List<Long> gradeIds = new ArrayList<Long>();
							List<Long>  deanOfClassIds = user.getTeacherPart().getDeanOfClassIds();
							List<LessonInfo> lessonlist = null;
							List<Classroom>  classroomList = null;
							List<Grade> gradeList = null;
							Course course = null;
							if (courseList!=null ) {
								for (int j = 0; j < courseList.size(); j++) {
									course = courseList.get(j);
									lessonIds.add(course.getLessonId());
									classIds.add(course.getClassId());
								}
								lessonlist =  commonDataService.getLessonInfoBatch(Long.valueOf(schoolId), lessonIds, termInfoId);
							
								if (lessonlist!=null) {
									for (int j = 0; j < lessonlist.size(); j++) {
										LessonInfo lessonInfo = lessonlist.get(j);
										lessonMap.put(lessonInfo.getId(), lessonInfo.getName());
									}
								}
								
								classroomList = commonDataService.getClassroomBatch(Long.valueOf(schoolId), classIds, termInfoId);
								
								if (classroomList!=null) {
									Classroom classroom = null;
									for (int j = 0; j < classroomList.size(); j++) {
										classroom = classroomList.get(j);
										classroomMap.put(classroom.getId(), classroom.getClassName());
										gradeIds.add(classroom.getGradeId());
										classGradeIdMap.put(classroom.getId(), classroom.getGradeId());
									}
									gradeList =  commonDataService.getGradeBatch(Long.valueOf(schoolId), gradeIds, termInfoId);
									if (gradeList!=null) {
										for (int j = 0; j < gradeList.size(); j++) {
											Grade grade = gradeList.get(j);
											gradeMap.put(grade.getId(),njName.get(grade.getCurrentLevel()));
										}
									}
									
								}
 
								for (int j = 0; j < courseList.size(); j++) {
									course = courseList.get(j);
									JSONObject teachingObj = new JSONObject();
									if ( StringUtils.isNotBlank(gradeMap.get(classGradeIdMap.get(course.getClassId()))) ) {
										teachingObj.put("gradeName", gradeMap.get(classGradeIdMap.get(course.getClassId())));
										teachingObj.put("className", classroomMap.get(course.getClassId()));
										teachingObj.put("classRole", "任课老师");
										if (deanOfClassIds!=null) {
											for (int k = 0; k < deanOfClassIds.size(); k++) {
												if (deanOfClassIds.get(k) ==course.getClassId() ) {
													teachingObj.put("classRole", "班主任");
													break;
												}
											}
										}
										teachingObj.put("lessonName", lessonMap.get(course.getLessonId())==null?"":lessonMap.get(course.getLessonId()));
										teachingRalation.add(teachingObj);
									}
								
								}
								
							}
							object.put("teachingRalation", teachingRalation);
							break ;
						}
				}
			}
            response.put("data", object);
        	setPromptMessage(response, "1", "查询成功");
		   return response;
    }
    
 
	
	 
 
	
	@RequestMapping(value="/getExamResultsList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamResultsList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		 JSONObject response = new JSONObject();
		
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	
 
	
    public static String getPinYinHeadChar(String str) {
    	if (StringUtils.isBlank(str)) {
			return  "";
		}
        String convert = "";
        char word = str.charAt(0);
        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
        if (pinyinArray != null) {
            convert += pinyinArray[0].charAt(0);
        }
        convert += word;
        return convert;
    }
    
     
 
 
}
