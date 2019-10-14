package com.talkweb.wishFilling.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.service.WishFillingTeacherService;

/** 
 * 新高考志愿填报-老师查看action
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Controller
@RequestMapping(value = "/wishFilling/teacher")
public class WishFillingTeacherAction extends BaseAction{
	 @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingService  wishFillingService;
	    @Autowired
	    private WishFillingTeacherService  wishFillingTeacherService;
	    
		private static final Logger logger = LoggerFactory.getLogger(WishFillingTeacherAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
/**
 * 六、老师统计模块
 */
		/**
		 * (1)获取填报列表
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getTbList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getTbList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				String schoolId = getXxdm(req);
				param.put("schoolId", schoolId);
				String termInfo = request.getString("termInfo");
				param.put("termInfo", termInfo);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				String schoolYear = termInfo.substring(0,4);
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
				Long userId = -1L;
				if(user.getUserPart()!=null){
					userId = user.getUserPart().getId();
					user = allCommonDataService.getUserById(Long.parseLong(schoolId), userId, termInfo);
					if(user==null|| user.getUserPart()==null){
						response.put("data", data);
						setPromptMessage(response, "0", "查询成功");
						return response;
					}
				}else{
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
					return response;
				}
				TeacherPart tp = user.getTeacherPart();
				List<Long> classIdList = new ArrayList<Long>();
				if(tp!=null){
					List<Course> courseList = tp.getCourseIds(); //获取任教关系
					if(courseList!=null){
						for(Course course:courseList){
							long classId = course.getClassId();
							classIdList.add(classId);
						}
					}
					if(classIdList.size()==0){ //如果任教关系为空 则直接不出任何信息（老师角色）
						response.put("data", data);
						setPromptMessage(response, "0", "查询成功");
						return response;
					}
				}else{
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
					return response;
				}
				
				List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), classIdList, termInfo);
				Set<Long> gSet=new HashSet<Long>();
				for(Classroom c: cList){
					gSet.add(c.getGradeId());
				}
				List<Grade> gList = allCommonDataService.getGradeBatch(Long.parseLong(schoolId), new ArrayList<Long>(gSet), termInfo);
				//当前登录人的任教使用年级列表
				List<String> useGrades = new ArrayList<String>();
				for(Grade g:gList){
					String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
					useGrades.add(useGrade);
				}
				param.put("useGrades", useGrades); //当前登录人的任教使用年级列表
				param.put("userId", userId);
			    data = wishFillingTeacherService.getTbList(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * (2)按学生查看结果
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStaticListByStudent", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStaticListByStudent(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String classId = request.getString("classId");
				String name = request.getString("name");
				String schoolId = getXxdm(req);
				String termInfo =request.getString("termInfo");
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4, 5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("wfId", wfId);
				param.put("schoolId", schoolId);
				param.put("name", name);
				if(StringUtils.isBlank(classId)){
					param.put("classId", "-1");
				}else{
					param.put("classId", classId);
				}
				param.put("termInfo", termInfo);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
				
				//获取任教班级
				List<Long> courseIds = new ArrayList<Long>();
		
				//只显示当前登录人的任教科目
				HttpSession sess = req.getSession();
   				User user=(User)(sess.getAttribute("user"));
   				Long userId = user.getUserPart().getId();
				user = allCommonDataService.getUserById(Long.parseLong(schoolId), userId, termInfo);
   				if(user!=null){
					TeacherPart tp = user.getTeacherPart();
	   				List<Course> courseList = tp.getCourseIds(); //获取任教关系
	   				if(courseList!=null){
		   				for(Course course:courseList){
		   					courseIds.add(course.getClassId());
		   				}
	   				}
   				}
				//只要 开放选课年级的任教班级
   				logger.info("wishFilling 1:param: " +param.toJSONString() );
   				JSONObject wf = wishFillingService.getTb(param);
   				logger.info("wishFilling 2:param: " +param.toJSONString() );
				String useGrade = wf.getString("wfGradeId");
				T_GradeLevel currentLevel = T_GradeLevel.findByValue(Integer.parseInt(allCommonDataService.ConvertSYNJ2NJDM(useGrade, termInfo.substring(0, 4))));
				Grade g = allCommonDataService.getGradeByGradeLevel(Long.parseLong(schoolId), currentLevel, termInfo);
				List<Grade> gList= new ArrayList<Grade>();
				gList.add(g);
				List<Classroom> cList = allCommonDataService.getSimpleClassList(school, gList, termInfo);
				List<Long> classIds = new ArrayList<Long>();
				String allClassIds ="";
				for(Classroom c:cList){
					Long cId = c.getId();
					if(!courseIds.contains(cId)){
						continue;
					}
					classIds.add(cId);
					allClassIds+=cId+",";
				}
				if(classIds.size()>0){
					param.put("allClassIdList", classIds);
				}else{
					Long cId= -1l;
					classIds.add(cId);
					param.put("allClassIdList", classIds);
				}
				if(StringUtils.isNotBlank(allClassIds)){
					allClassIds=allClassIds.substring(0,allClassIds.length()-1);
				}else{
					allClassIds="-1";
				}
				param.put("allClassIds", allClassIds);
				logger.info("wishFilling 3:param: " +param.toJSONString() );
			    data = wishFillingTeacherService.getStaticListByStudent(param);
			    if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （3）按学生查看结果-未提交学生名单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getNoselectedStudentList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getNoselectedStudentList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String classId = request.getString("classId");
				String schoolId = getXxdm(req);
				String termInfo =request.getString("termInfo");
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4, 5);
				param.put("schoolId", schoolId);
				param.put("classId", classId);
				param.put("wfId", wfId);
				param.put("schoolYear", schoolYear);
				param.put("termInfoId", termInfoId);
				param.put("termInfo", termInfo);
				
			    data = wishFillingService.getNoselectedStudentList(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （4）按学生查看结果-未提交学生名单
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getProgressTbByTeacher", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getProgressTbByTeacher(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String schoolId = getXxdm(req);
				String termInfo =request.getString("termInfo");
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				HttpSession sess = req.getSession();
				User user=(User)(sess.getAttribute("user"));
				Long userId = user.getUserPart().getId();
				user = allCommonDataService.getUserById(Long.parseLong(schoolId), userId, termInfo);
				List<Long> classIdList = new ArrayList<Long>();
				if(user!=null){
					TeacherPart tp = user.getTeacherPart();
					if(tp!=null){
						List<Course> courseList = tp.getCourseIds(); //获取任教关系
						if(courseList!=null){
							for(Course course:courseList){
								long classId = course.getClassId();
								classIdList.add(classId);
							}
						}
					}else{
						setPromptMessage(response, "-1", "查询失败");
						return response;
					}
				}
				param.put("classIdList", classIdList);
			    data = wishFillingTeacherService.getProgressTbByTeacher(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （5）按科目查看结果
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStaticListBySubject", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStaticListBySubject(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			JSONObject param = new JSONObject();
			try {
				String wfId = request.getString("wfId");
				String subjectId = request.getString("subjectId");
				String schoolId = getXxdm(req);
				String termInfo =request.getString("termInfo");
				String schoolYear = termInfo.substring(0,4);
				String termInfoId = termInfo.substring(4,5);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("termInfo", termInfo);
				param.put("subjectId", subjectId);
			    data = wishFillingService.getStaticListBySubject(param);
				if(data!=null){
					response.put("data", data);
					setPromptMessage(response, "0", "查询成功");
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
}
