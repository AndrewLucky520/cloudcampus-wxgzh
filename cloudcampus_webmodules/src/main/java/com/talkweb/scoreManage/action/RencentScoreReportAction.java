package com.talkweb.scoreManage.action;
/** 
* @author  Administrator
* @version 创建时间：2018年3月5日 上午9:29:07 
* 人人通获取最近一次考试成绩信息(嵌入人人通页面)
*/

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.service.ScoreReportService;


@RequestMapping("/talkCloud/scoreReport1/") // 人人通Auth 单点登录web.xml url 过滤
@Controller
public class RencentScoreReportAction extends BaseAction { 

	@Autowired
	private ScoreReportService reportService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	
	private static final Logger logger = LoggerFactory.getLogger(RencentScoreReportAction.class);

	//成绩报告人人通接口-s 
	
	// 教师获取任教班级
	@RequestMapping(value = "getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(@RequestBody JSONObject request, HttpServletRequest req ,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
		if (accountId == null) {
			throw new CommonRunException(-1, "无法获取用户信息，请联系管理员！");
		}
		
		Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
		List<User> list = account.getUsers();
		Set<Long> classIds = new HashSet<Long>();
		if (list!=null ) {
			for (int i = 0; i < list.size(); i++) {
				User user = list.get(i);
				TeacherPart teacherPart = user.getTeacherPart();
				if (teacherPart!=null) {
					List<Course> courseList = teacherPart.getCourseIds();
					if (courseList != null &&  courseList.size() > 0 ) {
						for (int j = 0; j < courseList.size(); j++) {
							Course course = courseList.get(j);
							course.getClassId();
							classIds.add(course.getClassId());
						}
					}else {
						response.put("data", new JSONArray());
						setResponse(response, 1, "查询成功！");
						return response;
					}

				 
				}
			}
		}

		List<Long> ids = new ArrayList<Long>(classIds);
		List<Classroom> classrooms = commonDataService.getClassroomBatch(Long.parseLong(schoolId), ids, termInfoId);
		try {
			classrooms = (List<Classroom>) Sort.sort(SortEnum.ascEnding0rder, classrooms, "className");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray array = new JSONArray();
		for (int i = 0; i <  classrooms.size(); i++) {
			Classroom classroom = classrooms.get(i);
			JSONObject classObj = new JSONObject();
			classObj.put("classId", classroom.getId());
			classObj.put("className", classroom.getClassName());
			array.add(classObj);
		}
		
		
		
		response.put("data", array);
		setResponse(response, 1, "查询成功！");
		return response;
	}
	
	
	// 教师获取班级最近一次考试的信息  
	// 参数 ： 班级 
	@RequestMapping(value = "getRecentClassExamInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRecentClassExamInfo(@RequestBody JSONObject request, HttpServletRequest req ,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xxdm = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		String accountId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
		if (accountId == null) {
			throw new CommonRunException(-1, "无法获取用户信息，请联系管理员！");
		}
		request.put("xxdm", xxdm);
		request.put("termInfoId", termInfoId);
		JSONObject data = reportService.getRecentClassExamInfo(request);
		JSONObject obj = new JSONObject();
		
		if (data.getInteger("pm") != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
			obj.put("classRank", data.getInteger("pm"));
			obj.put("classCount", data.getInteger("cnt"));
			obj.put("AverScore", new Integer []{data.getInteger("bjpjf") , data.getInteger("njpjf")});
			if (data.getDate("examDate")!=null) {
				obj.put("examDate",format.format(data.getDate("examDate")) );
			}
			obj.put("examName", data.getString("examName"));
		}
	
 
		response.put("data", obj);
		setResponse(response, 1, "查询成功！");
		
		return response;
	}
	
	// 学生获取最近一次考试的信息
	// 参数 ： 无
	@RequestMapping(value = "getRecentStuExamInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRecentStuExamInfo(@RequestBody JSONObject request, HttpServletRequest req ,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			request.put("type", 2);
			User user = (User) req.getSession().getAttribute("user");
			Long accountId =  (Long)req.getSession().getAttribute("accountId") ;
			if (user == null) {
				throw new CommonRunException(-1, "无法获取用户信息，请联系管理员！");
			}
			String schoolId = this.getXxdm(req);
			String curTermInfoId = getCurXnxq(req); // 学年学期

			if (T_Role.Parent.equals(user.getUserPart().getRole())) { // 如果是家长用户
				user = commonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),
						curTermInfoId);
			}

			if (!T_Role.Student.equals(user.getUserPart().getRole())) {
				throw new CommonRunException(-1, "无法获取学生信息，请联系管理员！");
			}
			long studentId = user.getAccountPart().getId();
			StudentPart studPart = user.getStudentPart();
			//studPart.en
			Classroom cr = commonDataService.getClassById(Long.parseLong(schoolId), studPart.classId, curTermInfoId);
			Grade g = commonDataService.getGradeById(Long.parseLong(schoolId), cr.gradeId, curTermInfoId);
			int curYear = Integer.parseInt(curTermInfoId.substring(0,4));
			int gap = g.getCurrentLevel().getValue();
			
			int gradeId = curYear - gap + 10;
			
			// 1.获取学年学期，学校代码，账号，并且设置到参数map中。
			request.put("schoolId", schoolId);
			request.put("xxdm", schoolId); // 兼容 t_gm_degreeinfo表查询
			request.put("studentId", studentId);
			request.put("curTermInfoId", curTermInfoId);
			request.put("classId", studPart.getClassId());
			request.put("gradeId", gradeId);
			
			List<JSONObject> data = reportService.getScoreReportViewList(request);
			List<JSONObject> recentList = reportService.getRecentStuExamInfo(data , studentId);
			logger.info("accountId===>" + accountId); 
			logger.info("studentId===>" + studentId);
			List<JSONObject> list = new ArrayList<JSONObject>();
			if (recentList!=null && recentList.size() > 0) {
				School school = getSchool(req, curTermInfoId);
				List<LessonInfo>  lessonList =commonDataService.getLessonInfoList(school, curTermInfoId);
				HashMap<Long, String> map = new HashMap<Long, String>();
				for (int i = 0; i < lessonList.size(); i++) {
					LessonInfo lessonInfo = lessonList.get(i);
					map.put(lessonInfo.getId(), lessonInfo.getName());
					
				}
				map.put( -1L , "全科");
				for (int i = 0; i < recentList.size(); i++) {
					JSONObject obj = recentList.get(i);
					obj.put("subjectName", map.get(obj.getLong("kmdm")));
					
					JSONObject object = new JSONObject();
					object.put("subjectId", obj.getString("kmdm"));
					object.put("subjectName",  map.get(obj.getLong("kmdm")));
					object.put("score", obj.getString("zf")) ;
					object.put("totalScore", obj.getString("mf")) ;
				    if (obj.getString("beforeRank")!= null) {
				    	object.put("ClassRank",  new Integer []{obj.getInteger("bjpm") , obj.getInteger("beforeRank")} );
					}else {
						object.put("ClassRank", new Integer[]{obj.getInteger("bjpm")});
					}    
					list.add(object);
				}
			}
			
			response.put("data", list);
			setResponse(response, 1, "查询成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
 
	}

	//成绩报告人人通接口-e
	
	
	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	
}
