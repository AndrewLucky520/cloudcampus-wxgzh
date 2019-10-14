package com.talkweb.wishFilling.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.wishFilling.service.WishFillingCommonService;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.util.Util;


/** 
 * 新高考志愿填报-公共接口
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Controller
@RequestMapping(value = "/wishFilling/common/")
public class WishFillingCommonAction extends BaseAction{
	 @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingCommonService wishFillingCommonService;
	    @Autowired
	    private WishFillingService wishFillingService;
		private static final Logger logger = LoggerFactory.getLogger(WishFillingCommonAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
/**
 * 一、公共模块
 */
		/**
		 * （1）获取年级列表（高中/初中）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getHighGradeList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getHighGradeList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				String isAll = request.getString("isAll");
				String termInfo = request.getString("termInfo");//getCurXnxq(req);
				School s = getSchool(req, termInfo);
				String areaCode = s.getAreaCode()+"";
				
				String xn = termInfo.substring(0, 4);
				String schoolId = getXxdm(req);
				Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
				List<T_GradeLevel> gradeLevels = new ArrayList<T_GradeLevel> ();
				if(Util.isToJunior(areaCode,schoolId)){
					gradeLevels.add(T_GradeLevel.T_JuniorOne);
					gradeLevels.add(T_GradeLevel.T_JuniorTwo);
					gradeLevels.add(T_GradeLevel.T_JuniorThree);
				}
				gradeLevels.add(T_GradeLevel.T_HighOne);
				gradeLevels.add(T_GradeLevel.T_HighTwo);
				gradeLevels.add(T_GradeLevel.T_HighThree);
				List<Grade> gradeList = allCommonDataService.getGradeByGradeLevelBatch(Long.parseLong(schoolId), gradeLevels, termInfo);
				
				for(Grade grade:gradeList){
					JSONObject obj = new JSONObject();
				    String useGrade = allCommonDataService.ConvertNJDM2SYNJ(grade.getCurrentLevel().getValue()+"", xn);
					obj.put("value",  useGrade);
					obj.put("text", njName.get(grade.getCurrentLevel()));
					String pycc = Util.getPycc(grade.getCurrentLevel().getValue());
					obj.put("pycc", pycc);
					data.add(obj);
				}
				data=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, data, "text"); //年级名称排序
				if("1".equals(isAll)){
					JSONObject obj = new JSONObject();
					obj.put("value", "");
					obj.put("text", "全部");
					data.add(0,obj);
				}
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （2）获取科目列表1（主副科目）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 * 第二版废弃
		 */
		/*@RequestMapping(value = "/getSubjectList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getSubjectList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			List<JSONObject> returnList= new ArrayList<JSONObject>();
			try {
				String isAll = request.getString("isAll");
				String isRelatedTeacher = request.getString("isRelatedTeacher");
				String termInfo = getCurXnxq(req);
				String schoolId = getXxdm(req);
				
				//登陆人的任教科目
				List<Long> courseLesson = new ArrayList<Long>();
				if("1".equals(isRelatedTeacher)){
					 //只显示当前登录人的任教科目
					HttpSession sess = req.getSession();
    				User user=(User)(sess.getAttribute("user"));
    				TeacherPart tp = user.getTeacherPart();
    				List<Course> courseList = tp.getCourseIds(); //获取任教关系
    				for(Course course:courseList){
    					long lesson = course.getLessonId();
    					courseLesson.add(lesson);
    				}
				}
				School s = allCommonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
				//全校科目
				List<LessonInfo> lessonList = allCommonDataService.getLessonInfoList(s, termInfo);
				for(LessonInfo l:lessonList){
					int type=l.getType();
					long id = l.getId();
					//既不是主科也不是副科
					if(type!=0 && type!=1){
						continue;
					}
					//关联任教老师，但该科目不在该老师的任教关系科目中
					if("1".equals(isRelatedTeacher) &&  !courseLesson.contains(id)){
						continue;
					}
					JSONObject returnObj = new JSONObject();
					returnObj.put("value", id);
					returnObj.put("text", l.getName());
					returnList.add(returnObj);
				}
				returnList = Util.lessonSort(returnList, "value"); //排序 根据obj中的value
				if("1".equals(isAll)){
					JSONObject returnObj = new JSONObject();
					returnObj.put("value", "");
					returnObj.put("text", "全部");
					returnList.add(0,returnObj);
				}
				response.put("data", returnList);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}*/
		/**
		 * （3）获取科目列表2（某个填报轮次设置的）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getSubjectListByTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getSubjectListByTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				String isAll = request.getString("isAll");
				String wfId = request.getString("wfId");
				String isRelatedTeacher = request.getString("isRelatedTeacher");
				String schoolId = getXxdm(req);
				String termInfo = request.getString("termInfo");
				//登陆人的任教科目
				List<Long> courseLesson = new ArrayList<Long>();
				if("1".equals(isRelatedTeacher)){
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
		    					long lesson = course.getLessonId();
		    					courseLesson.add(lesson);
		    				}
	    				}
    				}
				}
				param.put("schoolId", schoolId);
				param.put("wfId",wfId);
				param.put("isRelatedTeacher", isRelatedTeacher);
				param.put("courseLesson", courseLesson);
				param.put("isAll", isAll);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("termInfo", termInfo);
				param.put("areaCode", areaCode);
				JSONObject wfObj = wishFillingService.getTb(param);
				String pycc = wfObj.getString("pycc");
				param.put("pycc", pycc);
				//String termInfo = wfObj.getString("schoolYear")+wfObj.getString("termInfoId");
				data = wishFillingCommonService.getSubjectListByTb(param); // sql已排序
				/*List<JSONObject> dicSubList = wishFillingService.getDicSubjectList(schoolId, areaCode);
				Map<String,String> idNameMap = new HashMap<String,String>();
				for(JSONObject obj:dicSubList){
					idNameMap.put(obj.getString("subjectId"), obj.getString("subjectName"));
				}
				for(JSONObject obj:data){
					String sId=obj.getString("subjectId");
					if(idNameMap.get(sId)!=null && StringUtils.isNotBlank(idNameMap.get(sId))){
						obj.put("subjectName", idNameMap.get(sId));
					}else if (sId.contains(",")){	
						obj.put("subjectName", "全部");
					}else{
						obj.put("subjectName", "[已删除]");
					}
				}*/
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （4）获取班级列表（根据单个年级代码）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		 @RequestMapping(value = "/getClassList", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getClassList(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			//JSONObject param = new JSONObject();
			List<JSONObject> returnList = new ArrayList<JSONObject>();
			try {
				String isRelatedTeacher = request.getString("isRelatedTeacher");
				String isAll = request.getString("isAll");
				String useGrade = request.getString("gradeId");
				String termInfo = request.getString("termInfo");
				String schoolId = getXxdm(req);
				//param.put("schoolId", schoolId);
				School school = getSchool(req, termInfo);
				String schoolYear = termInfo.substring(0, 4);
				String currentLevel=allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
				//获取任教班级
				List<Long> courseIds = new ArrayList<Long>();
				if("1".equals(isRelatedTeacher)){
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
			   					long classId = course.getClassId();
			   					courseIds.add(classId);
			   				}
		   				}
	   				}
				}
				Grade g = allCommonDataService.getGradeByGradeLevel(Long.parseLong(schoolId), T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
				List<Grade> gList= new ArrayList<Grade>();
				gList.add(g);
				List<Classroom> cList = allCommonDataService.getSimpleClassList(school, gList, termInfo);
				List<Long> classIds = new ArrayList<Long>();
				for(Classroom c:cList){
					Long classId = c.getId();
					if("1".equals(isRelatedTeacher) && !courseIds.contains(classId)){
						continue;
					}
					JSONObject returnObj = new JSONObject();
					classIds.add(classId);
					returnObj.put("value", classId);
					returnObj.put("text", c.getClassName());
					returnList.add(returnObj);
				}
				returnList=(List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, returnList, "text"); //根据班级名称升序排序
				if("1".equals(isAll)){
					JSONObject returnObj = new JSONObject();
					returnObj.put("value",classIds);
					returnObj.put("text", "全部");
					returnList.add(0,returnObj);
				}
				response.put("data", returnList);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		} 
	 
		/**
		 * （5）获取登录角色
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 *   role:0填报管理员 1 一般老师 2学生家长
			haveData（一般老师是否显示填报数据）:0 不显示 1显示 （一般老师这里 只取当前的任教关系）
		 */
		@RequestMapping(value = "/getTbRole", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getTbRole(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
					String schoolId = getXxdm(req);
					String termInfo =getCurXnxq(req);// request.getString("termInfo");
					String xn = termInfo.substring(0, 4);
					String teacherId = String.valueOf((long)req.getSession().getAttribute("accountId"));
					boolean flag = isMoudleManager(req, "cs1025");
					School school = getSchool(req, termInfo);
					String areaCode = school.getAreaCode()+"";
					param.put("areaCode", areaCode);
					if(Util.isToJunior(areaCode,schoolId)){ //是否为5选3学校
						//是否有初中部
						List<T_GradeLevel> gList =new ArrayList<T_GradeLevel>();
						gList.add(T_GradeLevel.T_JuniorOne);
						gList.add(T_GradeLevel.T_JuniorTwo);
						gList.add(T_GradeLevel.T_JuniorThree);
						List<Grade> graList =	allCommonDataService.getGradeByGradeLevelBatch(Long.parseLong(schoolId), gList, termInfo);
						if(graList==null || graList.size()==0){
							data.put("selectWayToJunior", 0); //没有初中部则按非5选3流程走
						}else{
							data.put("selectWayToJunior", 1);
						}
					}else{
						data.put("selectWayToJunior", 0);
					}
					HttpSession sess = req.getSession();
    				User user=(User)(sess.getAttribute("user"));
    				Long userId = user.getUserPart().getId();
    				user = allCommonDataService.getUserById(Long.parseLong(schoolId), userId, termInfo);
    				if(user==null){setPromptMessage(data, "-1", "角色为空");}
    				if(flag) {//是系统管理员
    					data.put("role", 0);
    					data.put("haveData", 1);
    				}
    				//当前登录身份为老师
    				if(!flag){
    					logger.info("[xgk select getTbRole]"+user);
	    				if(user.getUserPart().getRole().equals(T_Role.Student)|| user.getUserPart().getRole().equals(T_Role.Parent))//学生家长
	    				{
	    					data.put("role", 2);
	    				}else { //老师或者管理员（无权限）
	    					data.put("role", 1);
	    					//老师身份查看是否有填报数据 && 该老师任教关系下的填报数据 && 所有学年学期的
	    					TeacherPart tp=user.getTeacherPart();
	    					List<Course> courseList = tp.getCourseIds();
	    					List<Long> cIds = new ArrayList<Long>();
	    					if(courseList!=null){
		    					for(Course c:courseList){
		    						cIds.add(c.getClassId());
		    					}
	    					}
	    					List<Classroom> cList = allCommonDataService.getSimpleClassBatch(Long.parseLong(schoolId), cIds, termInfo);
	    					List<String> useGrades = new ArrayList<String>();
	    					Set<String> useGradesSet = new HashSet<String>();
	    					List<Long> gList = new ArrayList<Long>();//年级gradeId
	    					for(Classroom c:cList){
	    						if(!gList.contains(c.getGradeId())){
	    							gList.add(c.getGradeId());	
	    						}
	    						
	    					}
	    					List<Grade> gradeList = allCommonDataService.getGradeBatch(Long.parseLong(schoolId), gList, termInfo);
	    					for(Grade g:gradeList){
	    						useGradesSet.add(allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", xn));
	    					}
	    					if(useGradesSet.size()>0){
	    						useGrades.addAll(useGradesSet);
	    					}
	    					if(useGrades.size()>0){
		    					param.put("schoolId", schoolId);
		    					param.put("teacherId",  teacherId);
		    					param.put("useGrades", useGrades);
		    					param.put("termInfo", termInfo);
		    					int haveData = wishFillingCommonService.hasTbByUseGrades(param);
		    					data.put("haveData" , haveData);
	    					}else{
	    						data.put("haveData" , 0);
	    					}
	    				}
			        }
					
				setPromptMessage(data, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(data, "-1", "查询失败");
				e.printStackTrace();
			}
			return data;
		}
		/**
		 * （6）获取设置的组合列表（某个填报轮次设置的）
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getZhListByTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getZhListByTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
					String schoolId = getXxdm(req);
					String termInfo = request.getString("termInfo");
					String wfId = request.getString("wfId");
					School school = getSchool(req, termInfo);
					String areaCode = school.getAreaCode()+"";
					param.put("areaCode", areaCode);
					param.put("schoolId", schoolId);
					param.put("termInfo", termInfo);
					param.put("wfId", wfId);
					List<JSONObject> list = wishFillingCommonService.getZhListByTb(param);
					response.put("data", list);
					setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		
}
