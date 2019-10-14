package com.talkweb.commondata.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.action.ListComparetor;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;

/**
 * @ClassName CommonDataAction
 * @author Homer
 * @version 1.0
 * @Description 公共数据
 * @date 2015年3月4日
 */
@Controller
@RequestMapping(value = "/commondata/")
public class CommonDataAction extends BaseAction {
	@Autowired
	private AllCommonDataService commonDataService;

	private static final Logger logger = LoggerFactory.getLogger(CommonDataAction.class);

	private static ListComparetor comparetor = new ListComparetor();
	ResourceBundle rb = ResourceBundle.getBundle("constant.casconfig" );

	/**
	 * 得到当前学期学年
	 * 
	 * @return 学期学年
	 */
	@RequestMapping(value = "getCurTermInfo", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getCurTermInfo(HttpSession session) {
		JSONObject obj = new JSONObject();
		School school = (School) session.getAttribute("school");
		String xnxq = commonDataService.getCurrentXnxq(school);
		int schoolYear = Integer.valueOf(xnxq.substring(0, 4));
		obj.put("id", xnxq);
		obj.put("schoolYear", schoolYear);
		obj.put("termName", schoolYear + "-" + (schoolYear + 1) + "学年第" + xnxq.substring(4, 5) + "学期");
		return obj;
	}
	/**
	 *通过教育云的accountId获取云校园的userId
	 * 
	 * @author zhanghuihui
	 */
	@RequestMapping(value = "getUserIdByJYCloudAccountId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getUserIdByJYCloudAccountId(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		 String jyAccountId = requestParams.getString("accountId");
		 String termInfoId = rb.getString("currentTermInfo"); 
		 List<JSONObject> userList = commonDataService.getUserIdByExtId(jyAccountId,termInfoId);
		 JSONObject obj = new JSONObject();
		  if (CollectionUtils.isNotEmpty(userList)){
    		JSONObject userObj = userList.get(0); 
    		long userId =  userObj.getLongValue("userId");
    		if(userId==0L){return obj;}
    		User user = commonDataService.getUserById(-1000,userId);
    		School sch = commonDataService.getSchoolByUserId(-1000,userId);
    		if(user==null ||sch==null|| sch.getId()==0L){return obj;}
    		obj.put("userId", userId);
    		obj.put("schoolId", sch.getId());
    		obj.put("nickName",user.getAccountPart().getName());
    		obj.put("role", user.getUserPart().getRole().getValue());
		 }
		return obj;
	}

	/**
	 * 获取该学校所有教师列表
	 * 
	 * @param session
	 *            会话
	 * @return 教师列表
	 */
	@RequestMapping(value = "getAllTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllTeacherList(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {

		JSONObject json = new JSONObject();
		int code = 0;
		String msg = "";
		String selectedSemester = requestParams.getString("selectedSemester");
		if (selectedSemester == null || selectedSemester.trim().length() == 0) {
			selectedSemester = getCurXnxq(req);
		}
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		try {

			School school = getSchool(req, selectedSemester);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", school.getId());
			List<Account> accounts = commonDataService.getAllSchoolEmployees(school, selectedSemester, "");
			for (Account a : accounts) {
				if (a.getUsers() == null) {
					continue;
				}
				for (User u : a.getUsers()) {
					if (u == null || u.getUserPart() == null || u.getUserPart().getRole() == null) {
						continue;
					}
					if (u.getUserPart().getRole().equals(T_Role.Teacher)) {
						JSONObject j = new JSONObject();
						j.put("text", a.getName());
						j.put("value", a.getId());
						teacherList.add(j);
						break;
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			code = -1;
			msg = "未检索到教师！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", teacherList);
		return json;
	}

	/**
	 * 获取该学校所有学生列表
	 * 
	 * @param session
	 *            会话
	 * @return 学生列表
	 * @throws Exception
	 */
	@RequestMapping(value = "getAllStudentList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllStudentList(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		JSONObject json = new JSONObject();
		int code = 0;
		String msg = "";
		String selectedSemester = requestParams.getString("selectedSemester");
		if (selectedSemester == null || selectedSemester.trim().length() == 0) {
			selectedSemester = getCurXnxq(req);
		}
		List<JSONObject> studentList = new ArrayList<JSONObject>();
		try {

			School school = getSchool(req, selectedSemester);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", school.getId());
			List<Account> accounts = commonDataService.getStudentList(map);
			for (Account a : accounts) {
				if (a.getUsers() == null) {
					continue;
				}
				for (User u : a.getUsers()) {
					if (u == null || u.getUserPart() == null || u.getUserPart().getRole() == null
							|| u.getStudentPart() == null) {
						continue;
					}
					if (u.getUserPart().getRole().equals(T_Role.Student)) {
						JSONObject j = new JSONObject();
						j.put("text", a.getName());
						j.put("value", a.getId());
						studentList.add(j);
						break;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到学生！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", studentList);
		return json;
	}

	/**
	 * 获取年级（入学年度） 还未添加权限判断、当前学年学期获取
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradeList(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		int isAll = requestParams.getIntValue("isAll");
		List<JSONObject> rs = new ArrayList<JSONObject>();
		long d1 = new Date().getTime();
		int code = 0;
		String msg = "";
		String menuId = requestParams.getString("appId");
		String pyccs = requestParams.getString("pyccs");
		logger.info("【权限查询】查sessionId:{}", req.getSession().getId());
		try {
			//rs = getGradeRightList(req, requestParams);
			 
			Boolean isManager = false;
			if(isMoudleManager(req, menuId)){
				isManager=true;
			}
			School sch = new School();
			long schoolId=Long.parseLong(getXxdm(req));
			String xnxq = requestParams.getString("termInfoId");
			String selectedSemester = requestParams.getString("selectedSemester");
			if(selectedSemester!=null&&selectedSemester.trim().length()>0){
				xnxq = selectedSemester;
			}
			String xn= xnxq.substring(0, 4);
			sch.setId(schoolId);
			List<Grade> gList = commonDataService.getGradeList(sch, xnxq);
			logger.info("gList:"+gList.toString()); 
			List<Long> ids = new ArrayList<Long>();
			List<Long> gids = new ArrayList<Long>();
			if(!isManager){
				School school = new School();
				school.setId(schoolId);
				List<Long> accountIds = new ArrayList<>();
				long  accountId =    (long) req.getSession().getAttribute("accountId");
				long  userId =    (long) req.getSession().getAttribute("userId");
				accountIds.add(accountId);
				List<Account> aList = commonDataService.getAccountBatch(schoolId, accountIds, xnxq);
				logger.info("aList:"+aList.toString());
				if( accountId==0||userId==0){
					JSONObject json = new JSONObject();
					json.put("code", -1);
					json.put("msg", "获取当前信息失败");
					return json;
	
				}
				for(Account a :aList){
					if(accountId==a.getId()){
						List<User> userList = a.getUsers();
						if(userList!=null){
							for(User user:userList){
								if(user.getUserPart()!=null&& userId==user.getUserPart().getId() && user.getUserPart().getRole()!=null && user.getUserPart().getRole().getValue()==T_Role.Teacher.getValue())
								{ 
									List<Course> courseList = user.getTeacherPart().getCourseIds();
									if(courseList!=null){
										for(Course c :courseList){
											long classId = c.getClassId();
											ids.add(classId);
										}
									}
									/*List<Long> cIds = user.getTeacherPart().getDeanOfClassIds();
									if(cIds!=null){
										ids.addAll(cIds);
									}*/
								}
							}
						}
					}
				}
				logger.info("ids:"+ids);
				List<Classroom> cList = commonDataService.getClassroomBatch(schoolId, ids, xnxq);
				logger.info("cList:"+cList);
				for(Classroom c:cList){
					gids.add(c.getGradeId());
				}
			}
			
			StringBuffer all=new StringBuffer();
			
			for(Grade grade:gList)
			{
				long gradeId=grade.getId();
				if(!isManager){
					if(!gids.contains(gradeId)){
						continue;
					}
				}
				int njdm = grade.getCurrentLevel().getValue();
				//过滤培养层次
				if(pyccs!=null&&pyccs.trim().length()>0){
					T_GradeLevel gLevel = (T_GradeLevel)grade.getCurrentLevel();
					logger.info("gLevel:"+gLevel.toString());
					//无高中则过滤
					if(gLevel.getValue()>=T_GradeLevel.T_HighOne.getValue()&&pyccs.indexOf("3")==-1
							&&gLevel.getValue()!=T_GradeLevel.T_JuniorFour.getValue()){
						continue;
					}
					//无初中则过滤
					if((gLevel.getValue()>=T_GradeLevel.T_JuniorOne.getValue()
							&&gLevel.getValue()<=T_GradeLevel.T_JuniorThree.getValue()
							||gLevel.getValue()==T_GradeLevel.T_JuniorFour.getValue())
							&&pyccs.indexOf("2")==-1){
						continue;
					}
					//无小学则过滤
					if(gLevel.getValue()>=T_GradeLevel.T_PrimaryOne.getValue()
							&&gLevel.getValue()<=T_GradeLevel.T_PrimarySix.getValue()
							&&pyccs.indexOf("1")==-1){
						continue;
					}
					//幼儿园直接过滤
					if(gLevel.getValue()<T_GradeLevel.T_PrimaryOne.getValue()){
						continue;
					}
				}
				
				String synj = commonDataService.ConvertNJDM2SYNJ(njdm + "", xn);
				String gradeName = AccountStructConstants.T_GradeLevelName
				.get((grade.getCurrentLevel()));
				  String s = "[" + commonDataService.ConvertSYNJ2RXND(synj, xn)
				+ "]" + gradeName;
				
				all.append(synj+",");
				JSONObject j = new JSONObject();
				
				j.put("value",synj );
				j.put("text", s);
				rs.add(j);
			}
			Collections.sort(rs,comparetor);
			Collections.reverse(rs);
			if (isAll == 1) {				
				if (all.length() > 0) {
					String a = all.substring(0, all.length() - 1);
					JSONObject obj = new JSONObject();
					obj.put("value", a);
					obj.put("text", "全部");
					rs.add(0,obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到年级！";
		}

		if (rs.isEmpty()) {
			code = -1;
			msg = "未检索到年级！";
		}

		JSONObject json = new JSONObject();
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);

		long d2 = new Date().getTime();

		return json;
	}

	/**
	 * 获取班级 还未添加权限判断、当前学年学期获取
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		int isAll =  requestParams.getIntValue("isAll");
		String menuId =  requestParams.getString("appId");
		try {
			//rs = getClassRightList(req, requestParams);
			//
			School sch = new School();
			long schoolId= Long.parseLong(getXxdm(req));
			String synj  =  requestParams.getString("usedGradeId");
			String xnxq =  requestParams.getString("termInfoId");
			String selectedSemester = requestParams.getString("selectedSemester");
			if(selectedSemester!=null&&selectedSemester.trim().length()>0){
				xnxq = selectedSemester;
			}
			String xn= xnxq.substring(0, 4);
			sch.setId(schoolId);
			 Boolean isManager = false;
			if(isMoudleManager(req, menuId)){
				isManager=true;
			} 
			logger.info("isManager:"+isManager);
			//Boolean isManager = true;
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put("schoolId", schoolId);
			map.put("termInfoId", xnxq);
			map.put("usedGradeId", synj);
			List<Classroom> cList =commonDataService.getClassList(map);
			logger.info("cList:"+cList.toString()); 
			List<Long> ids = new ArrayList<Long>();
			if(!isManager){
				School school = new School();
				school.setId(schoolId);
				List<Long> accountIds = new ArrayList<>();
				Long  accountId =    (Long) req.getSession().getAttribute("accountId");
				Long  userId =    (Long) req.getSession().getAttribute("userId");
				accountIds.add( accountId);
				List<Account> aList = commonDataService.getAccountBatch(schoolId, accountIds, xnxq);
				logger.info("aList:"+aList.toString());
				if(0==accountId ||0==userId){
					json.put("code", -1);
					json.put("msg", "accountId/userId为空");
					json.put("data", rs);
					return json;
	
				}
				for(Account a :aList){
					if(accountId==a.getId()){
						List<User> userList = a.getUsers();
						if(userList!=null){
							for(User user:userList){
								if(user.getUserPart()!=null&& userId==user.getUserPart().getId() && user.getUserPart().getRole()!=null && user.getUserPart().getRole().getValue()==T_Role.Teacher.getValue())
								{ 
									List<Course> courseList = user.getTeacherPart().getCourseIds();
									if(courseList!=null){
										for(Course c :courseList){
											long classId = c.getClassId();
											ids.add(classId);
										}
									}
									List<Long> cIds = user.getTeacherPart().getDeanOfClassIds();
									if(cIds!=null){
										ids.addAll(cIds);
									}
								}
							}
						}
					}
				}
			}
			StringBuffer all=new StringBuffer();
			for(Classroom c:cList)
			{
				if(!isManager)	 {
					if(!ids.contains(c.getId())){continue;}
				}				
			    all.append(c.getId()+",");
				JSONObject j = new JSONObject();
				j.put("value", c.getId());
				j.put("text", c.getClassName());
				rs.add(j);
			}
			Collections.sort(rs,comparetor);
			if (isAll == 1) {				
				if (all.length() > 0) {
					String a = all.substring(0, all.length() - 1);
					JSONObject obj = new JSONObject();
					obj.put("value", a);
					obj.put("text", "全部");
					rs.add(0,obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到班级！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}

	/**
	 * 获取所有科目，，还有权限判断 获取科目 前端传入值为null，则无需判断学年学期、年级、班级，科目取值为对应功能（菜单）的所有科目。
	 * 
	 * @param req
	 * @param res
	 * @return
	 */ 
	@RequestMapping(value = "getSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSubjectList(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		try {
			rs = getSubjectList(req, requestParams);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到科目！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	} 

	@RequestMapping(value = "getSystemVersion", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String, Object>> getSystemVersion() {

		Map<String, Object> result = new HashMap<String, Object>();
		List<Map<String, Object>> systemVersion = commonDataService.getSystemVersion();
		return systemVersion;
	}

	/**
	 * 获取是否管理员，需深圳提供
	 * 
	 * @return
	 */
	@RequestMapping(value = "getScoreManager", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreManager(HttpServletRequest req, HttpServletResponse res) {
		JSONObject rs = new JSONObject();
		HttpSession sess = req.getSession();
		boolean isAdmin = false;
		if (sess.getAttribute("isAdmin") != null) {
			isAdmin = (boolean) sess.getAttribute("isAdmin");
		}
		boolean isCjgly = false;
		if (sess.getAttribute("isCjgly") != null) {
			isCjgly = (boolean) sess.getAttribute("isCjgly");
		}
		if (isAdmin || isCjgly) {
			rs.put("gly", 1);
		} else {
			rs.put("gly", 0);
		}
		return rs;
	}

	/**
	 * 获取综合科目列表 需深圳提供接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getComprehensiveSubjects", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getComprehensiveSubjects(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {
		JSONArray rs = new JSONArray();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		String selectedSemester = requestParams.getString("selectedSemester");
		if (selectedSemester == null || selectedSemester.trim().length() == 0) {
			selectedSemester = getCurXnxq(req);
		}
		try {

			List<LessonInfo> list = commonDataService.getLessonInfoByType(this.getSchool(req, selectedSemester), 1,
					selectedSemester);
			for (LessonInfo le : list) {
				JSONObject j = new JSONObject();
				j.put("value", le.getId());
				j.put("text", le.getName());
				rs.add(j);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到综合科目！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}

	/**
	 * 无权限过滤获取班级列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getNoRightClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoRightClassList(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		try {
			// String selectedSemester = requestParams.getString("termInfoId");
			// if(selectedSemester==null||selectedSemester.trim().length()==0){
			// selectedSemester = getCurXnxq(req);
			// }
			String selectedSemester = requestParams.getString("termInfoId");
			String frontXnxq = requestParams.getString("selectedSemester");
			if (frontXnxq != null && frontXnxq.trim().length() > 0) {
				selectedSemester = frontXnxq;
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", getXxdm(req));
			map.put("termInfoId", selectedSemester);
			map.put("usedGradeId", requestParams.getString("usedGrade"));
			int isAll = requestParams.getInteger("isAll");
			List<Classroom> list = commonDataService.getClassList(map);
			StringBuffer all = new StringBuffer();
			for (Classroom c : list) {
				JSONObject obj = new JSONObject();
				obj.put("value", c.getId());
				obj.put("text", c.getClassName());
				all.append(c.getId()).append(",");
				rs.add(obj);
			}
			// Collections.sort(rs, comparetor);
			rs = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, rs, "text");

			if (isAll == 1) {
				String avalues = "";
				if (all.length() > 0) {
					avalues = all.substring(0, all.length() - 1);
				}
				JSONObject obj = new JSONObject();
				obj.put("value", avalues);
				obj.put("text", "全部");
				rs.add(0, obj);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到班级！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}

	/**
	 * 获取无权限科目列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */

	@RequestMapping(value = "getNoRightSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoRightSubjectList(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {

		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		try {
			String selectedSemester = requestParams.getString("termInfoId");
			String frontXnxq = requestParams.getString("selectedSemester");
			if (frontXnxq != null && frontXnxq.trim().length() > 0) {
				selectedSemester = frontXnxq;
			}
			int isAll = requestParams.getInteger("isAll");
			List<LessonInfo> list = commonDataService.getLessonInfoList(this.getSchool(req, selectedSemester),
					selectedSemester);
			StringBuffer all = new StringBuffer();
			for (LessonInfo c : list) {
				JSONObject obj = new JSONObject();
				obj.put("value", c.getId());
				obj.put("text", c.getName());
				all.append(c.getId()).append(",");
				rs.add(obj);
			}
			Collections.sort(rs, comparetor);
			if (isAll == 1) {
				String avalues = "";
				if (all.length() > 0) {
					avalues = all.substring(0, all.length() - 1);
				}
				JSONObject obj = new JSONObject();
				obj.put("value", avalues);
				obj.put("text", "全部");
				rs.add(0, obj);
			}
			JSONObject obj = new JSONObject();
			obj.put("value", "totalScore");
			obj.put("text", "总分");
			rs.add(1, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			code = -1;
			msg = "未检索到科目！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}

	/**
	 * 查询教师组织架构
	 * 
	 * @param session
	 *            会话
	 * @return 教师组织架构列表
	 */
	@RequestMapping(value = "getTeacherLevel", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherLevel(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject json = new JSONObject();
		try {
			int groupTypeId = 0;
			JSONArray array = new JSONArray();
			List<OrgInfo> orgList = null;
			String termInfo = requestParams.getString("selectedSemester");
			String isAll = requestParams.getString("isAll");
			if (requestParams.containsKey("groupTypeId")) {
				String typeId = requestParams.getString("groupTypeId");
				groupTypeId = Integer.parseInt(typeId);
			}
			String selectedSemester = requestParams.getString("selectedSemester");
			if (selectedSemester == null || selectedSemester.trim().length() == 0) {
				selectedSemester = getCurXnxq(req);
			}
			if (groupTypeId > 0) {
				orgList = commonDataService.getSchoolOrgList(this.getSchool(req, selectedSemester), groupTypeId,
						selectedSemester);
			} else {
				orgList = commonDataService.getSchoolOrgList(this.getSchool(req, selectedSemester), selectedSemester);
			}
			String ids = "";
			if (CollectionUtils.isNotEmpty(orgList)) {
				for (OrgInfo info : orgList) {
					JSONObject temp = new JSONObject();
					String groupLevelId = info.getId() + "";
					String groupLevelName = info.getOrgName();
					temp.put("groupLevelId", groupLevelId);
					temp.put("groupLevelName", groupLevelName);
					ids+=groupLevelId+",";
					array.add(temp);
				}
			}
			if (array.size() > 0) {
				List<JSONObject> a = new ArrayList<JSONObject>();
				if (groupTypeId == 2) { // getTeacherLevel 年级列表排序 @author zhh
					a = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, array, "groupLevelName");
					if("1".equals(isAll)){
						ids = ids.substring(0, ids.length()-1);
						JSONObject first = new JSONObject();
						first.put("groupLevelId", ids);
						first.put("groupLevelName", "全部");
						a.add(0, first);
					}
					json.put("groupLevelList", a);
				} else {// getTeacherLevel 机构列表排序 @author zhh
					a = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, array, "groupLevelId");
					if("1".equals(isAll)){
						ids = ids.substring(0, ids.length()-1);
						JSONObject first = new JSONObject();
						first.put("groupLevelId", ids);
						first.put("groupLevelName", "全部");
						a.add(0, first);
					}
					json.put("groupLevelList", a);
				}
				json.put("code", OutputMessage.querySuccess.getCode());
				json.put("msg", OutputMessage.querySuccess.getDesc());
			} else {
				json.put("msg", "未查询到结构信息！！");
				json.put("code", OutputMessage.querySuccess.getCode());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json.put("msg", "查询到结构信息出错！！");
			json.put("code", OutputMessage.queryDataError.getCode());
		}
		return json;
	}

	/**
	 * 查询组下的教师列表
	 * 
	 * @param session
	 *            会话
	 * @return 教师列表
	 */
	@RequestMapping(value = "getGroupLevelTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupLevelTeacher(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {

		JSONObject json = new JSONObject();
		String selectedSemester = requestParams.getString("selectedSemester");
		if (selectedSemester == null || selectedSemester.trim().length() == 0) {
			selectedSemester = getCurXnxq(req);
		}
		try {
			JSONArray array = new JSONArray();
			if (requestParams.containsKey("groupLevelId")) {
				String levelId = requestParams.getString("groupLevelId");
				long schoolId = Long.valueOf(getCurXnxq(req));
				List<Account> accountList = commonDataService.getOrgTeacherList(selectedSemester, schoolId, levelId,
						null);
				// .getAllTeacherList(Long.valueOf(this.getXxdm(req)),levelId,
				// null);
				if (CollectionUtils.isNotEmpty(accountList)) {
					for (Account info : accountList) {
						JSONObject temp = new JSONObject();
						temp.put("teacherId", info.getId() + "");
						temp.put("teacherName", info.getName());
						array.add(temp);
					}
				}
			}
			if (array.size() > 0) {
				json.put("teacherList", array);
				json.put("code", OutputMessage.querySuccess.getCode());
				json.put("msg", OutputMessage.querySuccess.getDesc());
			} else {
				json.put("msg", "未查询到教师信息！！");
				json.put("code", OutputMessage.querySuccess.getCode());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json.put("msg", "查询到教师信息出错！！");
			json.put("code", OutputMessage.queryDataError.getCode());
		}
		return json;
	}

	/**
	 * 查询多个组下的教师列表
	 * 
	 * @param session
	 *            会话
	 * @return 教师列表
	 */
	@RequestMapping(value = "getGroupsTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupsTeacher(@RequestBody JSONObject params, HttpServletRequest req,
			HttpServletResponse res) {

		JSONObject json = new JSONObject();
		try {

			String selectedSemester = params.getString("selectedSemester");
			if (selectedSemester == null || selectedSemester.trim().length() == 0) {
				selectedSemester = getCurXnxq(req);
			}
			JSONArray array = new JSONArray();
			long schoolId = Long.parseLong(getXxdm(req));
			List<Account> accountList = null;
			String orgIds = "";
			String termInfo = params.getString("selectedSemester");
			if (params.containsKey("gradeGroupId") && StringUtils.isNotEmpty(params.getString("gradeGroupId"))) {
				orgIds = params.getString("gradeGroupId");
			}
			if (params.containsKey("teacherGroupId") && StringUtils.isNotEmpty(params.getString("teacherGroupId"))) {
				orgIds = params.getString("teacherGroupId");
			}
			if (StringUtils.isEmpty(orgIds)) {
				accountList = commonDataService.getCourseTeacherList(termInfo, this.getSchool(req, selectedSemester),
						null);
			} else {
				accountList = commonDataService.getOrgTeacherList(selectedSemester, schoolId, orgIds, null);
			}
			if (CollectionUtils.isNotEmpty(accountList)) {
				for (Account info : accountList) {
					JSONObject temp = new JSONObject();
					temp.put("teacherId", info.getId() + "");
					temp.put("teacherName", info.getName());
					array.add(temp);
				}
			}
			if (array.size() > 0) {
				json.put("teacherList", array);
				json.put("code", OutputMessage.querySuccess.getCode());
				json.put("msg", OutputMessage.querySuccess.getDesc());
			} else {
				json.put("msg", "未查询到教师信息！！");
				json.put("code", OutputMessage.querySuccess.getCode());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json.put("msg", "查询到教师信息出错！！");
			json.put("code", OutputMessage.queryDataError.getCode());
		}
		return json;
	}

	/**
	 * 无权限过滤获取教师所教班级列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getGroupsLevelByStudent", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupsLevelByStudent(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		List<JSONObject> rsa = new ArrayList<JSONObject>();
		JSONObject returnObj = new JSONObject();
		returnObj.put("id", "-1");
		returnObj.put("text", "年级");
		returnObj.put("state", "closed");
		returnObj.put("children", new ArrayList<JSONObject>());
		rsa.add(returnObj);
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		String type = requestParams.getString("type").trim();
		// granule(颗粒：[1==班级，2==人])
		// type(类型[1==学生，2==家长])
		String granule = requestParams.getString("granule").trim();
		if (org.apache.commons.lang.StringUtils.isBlank(granule)) {
			granule = "1";
		}
		if (org.apache.commons.lang.StringUtils.isBlank(type)) {
			type = "1";
		}
		String xnxq = requestParams.getString("termInfoId").trim();
		if (xnxq == null || xnxq.length() == 0) {
			xnxq = getCurXnxq(req);
		}
		School school = getSchool(req, xnxq);
		try {
			List<Grade> gradeList = commonDataService.getGradeList(school, xnxq);
			Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
			Map<String, String> gradeMap = new HashMap<String, String>();
			List<Long> classIds = new ArrayList<Long>();
			if (CollectionUtils.isNotEmpty(gradeList)) {
				for (Grade grade : gradeList) {
					JSONObject jsonObject = new JSONObject();
					String convertNJDM2SYNJ = commonDataService
							.ConvertNJDM2SYNJ(String.valueOf(grade.getCurrentLevel().getValue()), xnxq.substring(0, 4));
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
			Map<String, List<JSONObject>> gradeClassMap = new LinkedHashMap<String, List<JSONObject>>();
			List<Long> accountIds = new ArrayList<Long>();
			if (CollectionUtils.isNotEmpty(classIds)) {
				List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(school.getId(), classIds,
						xnxq);
				
				if (CollectionUtils.isNotEmpty(classroomBatch)) {
					//排序
					Collections.sort(classroomBatch, new Comparator<Classroom>(){
						@Override
						public int compare(Classroom obj1, Classroom obj2) {
							return obj1.getClassName().compareTo( obj2.getClassName());
						}
					});
					for (Classroom classroom : classroomBatch) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("id", classroom.getId());
						jsonObject.put("text", classroom.getClassName());
						if ("1".equals(granule)) {
							JSONObject classObj = new JSONObject();
							classObj.put("id", classroom.getId());
							classObj.put("className", classroom.getClassName());
							classObj.put("gradeId", gradeMap.get(String.valueOf(classroom.getGradeId())));
							Integer count = 0;
							if ("1".equals(type)) {
								if (CollectionUtils.isNotEmpty(classroom.getStudentIds())) {
									count = classroom.getStudentIds().size();
								}
							} else {
								if (CollectionUtils.isNotEmpty(classroom.getParentIds())) {
									count = classroom.getParentIds().size();
								}
							}
							classObj.put("count", count);
							jsonObject.put("attributes", classObj);
						} else {
							jsonObject.put("state", "closed");
							jsonObject.put("children", new ArrayList<JSONObject>());
							List<Long> ids = null;
							if ("1".equals(type)) {
								if (CollectionUtils.isNotEmpty(classroom.getStudentIds())) {
									ids = classroom.getStudentIds();
									for (Long id : ids) {
										if (!accountIds.contains(id)) {
											accountIds.add(id);
										}
									}
								}
							} else {
								if (CollectionUtils.isNotEmpty(classroom.getParentIds())) {
									ids = classroom.getParentIds();
									for (Long id : ids) {
										if (!accountIds.contains(id)) {
											accountIds.add(id);
										}
									}
								}
							}
						}
						String gradeId = String.valueOf(classroom.getGradeId());
						if (gradeClassMap.containsKey(gradeId)) {
							gradeClassMap.get(gradeId).add(jsonObject);
						} else {
							gradeClassMap.put(gradeId, new ArrayList<JSONObject>());
							gradeClassMap.get(gradeId).add(jsonObject);
						}
					}
				}
			}
			if ("1".equals(granule)) {
				if (CollectionUtils.isNotEmpty(rs)) {
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						List<JSONObject> list = gradeClassMap.get(gradeId);
						jsonObject.put("children", list);
					}
				}
				if (CollectionUtils.isNotEmpty(rs)) {
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						jsonObject.put("id", gradeMap.get(gradeId));
					}
				}
				rs = (List<JSONObject>) Sort.sort(SortEnum.descEndingOrder, rs, "id");
			} else {
				Map<String, List<JSONObject>> classStuMap = new HashMap<String, List<JSONObject>>();
				List<Account> accountBatch = commonDataService.getAccountBatch(school.getId(), accountIds, xnxq);
				if (CollectionUtils.isNotEmpty(accountBatch)) {
					for (Account account : accountBatch) {
						if (CollectionUtils.isNotEmpty(account.getUsers())) {
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
							if (classStuMap.containsKey(String.valueOf(classId))) {
								classStuMap.get(String.valueOf(classId)).add(jsonObject);
							} else {
								classStuMap.put(String.valueOf(classId), new ArrayList<JSONObject>());
								classStuMap.get(String.valueOf(classId)).add(jsonObject);
							}
						}
					}
				}
				if (!gradeClassMap.isEmpty()) {
					Set<Entry<String, List<JSONObject>>> entrySet = gradeClassMap.entrySet();
					Iterator<Entry<String, List<JSONObject>>> iterator = entrySet.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, List<JSONObject>> entry = (Map.Entry<String, List<JSONObject>>) iterator
								.next();
						List<JSONObject> value = entry.getValue();
						if (CollectionUtils.isNotEmpty(value)) {
							for (JSONObject jsonObject : value) {
								jsonObject.put("children", jsonObject.getString("id"));
							}
						}
					}
				}
				if (CollectionUtils.isNotEmpty(rs)) {
					for (JSONObject jsonObject : rs) {
						String gradeId = jsonObject.getString("id");
						List<JSONObject> list = gradeClassMap.get(gradeId);
						jsonObject.put("children", list);
					}
				}
				if (CollectionUtils.isNotEmpty(rs)) {
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
		if (CollectionUtils.isNotEmpty(rsa) && rsa.size() > 0) {
			rsa.get(0).put("children", rs);
		}
		// returnObj.put("children", rs);
		json.put("data", rsa);
		return json;
	}

	/**
	 * 查询多个组下的教师列表
	 * 
	 * @param session
	 *            会话
	 * @return 教师列表
	 * @update 添加 管理组和其他组返回  zhanghuihui15222@talkweb.com.cn 2018.12.18
	 */
	@RequestMapping(value = "getGroupsLevel", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupsLevel(@RequestBody JSONObject params, HttpServletRequest req, HttpServletResponse res) {

		JSONObject json = new JSONObject();
		long schoolId = Long.parseLong(getXxdm(req));//Long.parseLong("1002");//
		String termInfo = params.getString("xnxq");
		if (StringUtils.isEmpty(termInfo)) {
			termInfo = commonDataService.getCurTermInfoId(schoolId);
		}
		Map<Long, String> teacherMap = new HashMap<Long, String>();
		School sch =getSchool(req, termInfo);// commonDataService.getSchoolById(1002, "20181");//
		try {
			/** 声明所有的节点基础信息 */
			// 科室下的科室列表
			JSONArray data = new JSONArray();
			JSONObject department = new JSONObject();
			String departmentIds = "88881";
			department.put("id", departmentIds);
			department.put("text", "科室");
			// department.put("checked", "false");
			department.put("state", "closed");
			JSONArray dChildren = new JSONArray();

			// 年级下的年级列表
			JSONObject gradeGroup = new JSONObject();
			String gradeGroupIds = "88882";
			gradeGroup.put("id", gradeGroupIds);
			gradeGroup.put("text", "年级组");
			// gradeGroup.put("checked", "false");
			gradeGroup.put("state", "closed");
			List<JSONObject> gChildren = new ArrayList<JSONObject>();

			// 教研下的教研列表
			JSONObject researchGroup = new JSONObject();
			String researchGroupIds = "88883";
			researchGroup.put("id", researchGroupIds);
			researchGroup.put("text", "教研组");
			// researchGroup.put("checked", "false");
			researchGroup.put("state", "closed");
			JSONArray rChildren = new JSONArray();

			// 备课下的备课列表
			JSONObject preparation = new JSONObject();
			String preparationIds = "88884";
			preparation.put("id", preparationIds);
			preparation.put("text", "备课组");
			// preparation.put("checked", "false");
			preparation.put("state", "closed");
			List<JSONObject> pChildren = new ArrayList<JSONObject>();

			// 管理组列表
			JSONObject manager = new JSONObject();
			String managerIds = "88888";
			manager.put("id", managerIds);
			manager.put("text", "管理组");
			// preparation.put("checked", "false");
			manager.put("state", "closed");
			List<JSONObject> mChildren = new ArrayList<JSONObject>();
			
			// 其他组列表
			JSONObject other = new JSONObject();
			String otherIds = "88889";
			other.put("id", otherIds);
			other.put("text", "其他组");
			// preparation.put("checked", "false");
			other.put("state", "closed");
			List<JSONObject> oChildren = new ArrayList<JSONObject>();

			
			// 各部门的责任人列表
			JSONObject departHead = new JSONObject();
			String departHeadIds = "88885";
			departHead.put("id", departHeadIds);
			departHead.put("text", "部门负责人");
			// departHead.put("checked", "false");
			departHead.put("state", "closed");
			JSONArray hChildren = new JSONArray();
			// 部门责任人-科室
			JSONObject depHead = new JSONObject();
			String depHeadId = departHeadIds + "01";
			depHead.put("id", depHeadId);
			depHead.put("text", "科室负责人");
			// depHead.put("checked", "false");
			depHead.put("state", "closed");
			JSONArray dHeadArray = new JSONArray();
			// 部门责任人-年级组
			JSONObject gradeHead = new JSONObject();
			String gradeHeadId = departHeadIds + "02";
			gradeHead.put("id", gradeHeadId);
			gradeHead.put("text", "年级组负责人");
			// gradeHead.put("checked", "false");
			gradeHead.put("state", "closed");
			JSONArray gradeHeadArray = new JSONArray();
			// 部门责任人-教研组
			JSONObject researchHead = new JSONObject();
			String researchHeadId = departHeadIds + "03";
			researchHead.put("id", researchHeadId);
			researchHead.put("text", "教研组负责人");
			// researchHead.put("checked", "false");
			researchHead.put("state", "closed");
			JSONArray researchHeadArray = new JSONArray();
			// 部门责任人-备课组
			JSONObject preparationHead = new JSONObject();
			String preparationHeadId = departHeadIds + "04";
			preparationHead.put("id", preparationHeadId);
			preparationHead.put("text", "备课组负责人");
			// preparationHead.put("checked", "false");
			preparationHead.put("state", "closed");
			JSONArray preparationHeadArray = new JSONArray();
			// 部门责任人-管理组
			JSONObject managerHead = new JSONObject();
			String managerHeadId = departHeadIds + "05";
			managerHead.put("id", managerHeadId);
			managerHead.put("text", "管理组负责人");
			// preparationHead.put("checked", "false");
			managerHead.put("state", "closed");
			JSONArray managerHeadArray = new JSONArray();
			// 部门责任人-其他组
			JSONObject otherHead = new JSONObject();
			String otherHeadId = departHeadIds + "06";
			otherHead.put("id", otherHeadId);
			otherHead.put("text", "其他组负责人");
			// preparationHead.put("checked", "false");
			otherHead.put("state", "closed");
			JSONArray otherHeadArray = new JSONArray();
			
			// 班主任列表
			JSONObject headmaster = new JSONObject();
			headmaster.put("text", "班主任");
			// headmaster.put("checked", "false");
			headmaster.put("state", "closed");
			String headmasterId = "88886";
			headmaster.put("id", headmasterId);
			// 年级下的班主任列表
			JSONArray gradeArray = new JSONArray();

			// 所有的教师列表
			JSONObject teachers = new JSONObject();
			teachers.put("text", "所有教师");
			// teachers.put("checked", "false");
			teachers.put("state", "closed");
			String teacherIds = "88887";
			teachers.put("id", teacherIds);

			/*** 获取全校教师信息列表 */
			// List<Account> accountList
			// =commonDataService.getCourseTeacherList(termInfo ,sch, null);
			Map<String,String> noRepeatLeaderMap = new HashMap<String,String>(); 
			List<Account> accountList = commonDataService.getAllSchoolEmployees(sch, termInfo, null);
			JSONArray allTeacherArray = new JSONArray();
			if (CollectionUtils.isNotEmpty(accountList)) {
				for (Account account : accountList) {
					long teacherId = account.getId();
					String teacherName = account.getName();
					teacherMap.put(teacherId, teacherName);

					JSONObject temp = new JSONObject();
					temp.put("id", teacherIds + teacherId);
					temp.put("text", teacherName);
					// temp.put("checked", "false");
					JSONObject attibute = new JSONObject();
					attibute.put("teacherId", teacherId + "");
					attibute.put("teacherName", teacherName);
					temp.put("attributes", attibute);
					allTeacherArray.add(temp);
				}
			}
			/*** 获取全校教师信息列表 */
			if (allTeacherArray.size() > 0) {
				teachers.put("children", allTeacherArray);
			}

			/** 查询学校-机构-教师,责任人 */
			List<OrgInfo> OrgList = commonDataService.getSchoolOrgList(sch, termInfo);
			for (OrgInfo orgInfo : OrgList) {
				JSONObject org = new JSONObject();
				if (orgInfo.getOrgType() ==  T_OrgType.T_Teach.getValue()) { // 1:教研组，2:年级组，3:备课组，6:科室,4管理组 ，5其他
					String parentId = researchGroupIds + orgInfo.getId();
					// 教研组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						rChildren.add(org);
					}
					// 教研组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_1")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_1", headId+"_1");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						researchHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == T_OrgType.T_Grade.getValue()) {
					String parentId = gradeGroupIds + orgInfo.getId();
					// 年级组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					// org.put("checked", "false");
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						gChildren.add(org);
					}
					// 年级组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_2")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_2", headId+"_2");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						gradeHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == T_OrgType.T_PreLesson.getValue()) {
					String parentId = preparationIds + orgInfo.getId();
					// 备课组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					// org.put("checked", "false");
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						pChildren.add(org);
					}
					// 备课组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_3")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_3", headId+"_3");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						preparationHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == T_OrgType.T_Depart.getValue()) {
					String parentId = departmentIds + orgInfo.getId();
					// 科室组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					// org.put("checked", "false");
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						dChildren.add(org);
					}
					// 科室责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_6")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_6", headId+"_6");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						dHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
				}else if(orgInfo.getOrgType() == T_OrgType.T_Manage.getValue()){ //管理组
					String parentId = managerIds + orgInfo.getId();
					// 管理组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					// org.put("checked", "false");
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						mChildren.add(org);
					}
					// 管里组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_4")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_4", headId+"_4");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						managerHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
					
					
				}else if(orgInfo.getOrgType() == T_OrgType.T_Other.getValue()){//其他组
					String parentId = otherIds + orgInfo.getId();
					// 其他组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					// org.put("checked", "false");
					org.put("state", "closed");
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						org.put("children", Fteacher.getJSONArray("teachers"));
						oChildren.add(org);
					}
					// 其他组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					List<Long> trueHeadIdList = new ArrayList<Long>();
					if(headIdList!=null){
						for(Long headId:headIdList){
							if(!noRepeatLeaderMap.containsKey(headId+"_5")){
								trueHeadIdList.add(headId);
								noRepeatLeaderMap.put(headId+"_5", headId+"_5");
							}
						}
					}
					JSONObject Tteacher = getTeacherArray(parentId, trueHeadIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						otherHeadArray.addAll(Tteacher.getJSONArray("teachers"));
					}
					
				}
			}
			/*** 获取班主任的信息列表 */
			List<Grade> gradeList = commonDataService.getGradeList(sch, termInfo);
			for (Grade grade : gradeList) {
				JSONObject gObject = new JSONObject();
				String gradeIds = headmasterId + grade.getId();
				gObject.put("id", gradeIds);
				String gradeName = AccountStructConstants.T_GradeLevelName.get((grade.getCurrentLevel()));
				gObject.put("text", gradeName);
				// gObject.put("checked", "false");
				gObject.put("state", "closed");
				List<Long> classIds = grade.getClassIds();
				if (CollectionUtils.isNotEmpty(classIds)) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					String ids = classIds.toString();
					ids = ids.replace("[", "").replace("]", "").replace(" ", "");
					map.put("classId", ids);
					map.put("schoolId", schoolId);
					map.put("termInfoId", termInfo);
					List<Account> aList = commonDataService.getDeanList(map);
					JSONArray deanArray = new JSONArray();
					if (CollectionUtils.isNotEmpty(aList)) {
						for (Account info : aList) {
							JSONObject temp = new JSONObject();
							temp.put("id", gradeIds + info.getId());
							temp.put("text", info.getName());
							// temp.put("checked", "false");
							JSONObject attibute = new JSONObject();
							attibute.put("teacherId", info.getId() + "");
							attibute.put("teacherName", info.getName());
							temp.put("attributes", attibute);
							deanArray.add(temp);
						}
					}
					if (deanArray.size() > 0) {
						gObject.put("children", deanArray);
						gradeArray.add(gObject);
					}
				}
			}

			/** 根据子节点信息添加父节点 */
			if (dChildren.size() > 0) {
				department.put("children", dChildren);
				data.add(department);
			}
			if (gChildren.size() > 0) {
				gradeGroup.put("children", Sort.sort(SortEnum.ascEnding0rder, gChildren, "text"));
				data.add(gradeGroup);
			}
			if (rChildren.size() > 0) {
				researchGroup.put("children", rChildren);
				data.add(researchGroup);
			}
			if (pChildren.size() > 0) {
				preparation.put("children", Sort.sort(SortEnum.ascEnding0rder, pChildren, "text"));
				data.add(preparation);
			}
			if (mChildren.size() > 0) {
				manager.put("children", Sort.sort(SortEnum.ascEnding0rder, mChildren, "text"));
				data.add(manager);
			}
			if (oChildren.size() > 0) {
				other.put("children", Sort.sort(SortEnum.ascEnding0rder, oChildren, "text"));
				data.add(other);
			}
			if (dHeadArray.size() > 0) {
				depHead.put("children", dHeadArray);
				hChildren.add(depHead);
			}
			if (gradeHeadArray.size() > 0) {
				gradeHead.put("children", gradeHeadArray);
				hChildren.add(gradeHead);
			}
			if (researchHeadArray.size() > 0) {
				researchHead.put("children", researchHeadArray);
				hChildren.add(researchHead);
			}
			if (preparationHeadArray.size() > 0) {
				preparationHead.put("children", preparationHeadArray);
				hChildren.add(preparationHead);
			}
			if (managerHeadArray.size() > 0) {
				managerHead.put("children", managerHeadArray);
				hChildren.add(managerHead);
			}
			if (otherHeadArray.size() > 0) {
				otherHead.put("children", otherHeadArray);
				hChildren.add(otherHead);
			}
			if (hChildren.size() > 0) {
				departHead.put("children", hChildren);
				data.add(departHead);
			}
			if (gradeArray.size() > 0) {
				headmaster.put("children", gradeArray);
				data.add(headmaster);
			}
			if (teachers.containsKey("children")) {
				data.add(teachers);
			}
			json.put("data", data);
			json.put("code", OutputMessage.querySuccess.getCode());
			json.put("msg", OutputMessage.querySuccess.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			json.put("msg", "查询到教师组织层次出错！！");
			json.put("code", OutputMessage.queryDataError.getCode());
		}
		return json;
	}

	/** 获取教师子节点的基础信息 */
	private JSONObject getTeacherArray(String parentId, List<Long> accountIdList, Map<Long, String> teacherMap) {
		JSONArray teacherArray = new JSONArray();
		if (CollectionUtils.isNotEmpty(accountIdList)) {
			for (long accountId : accountIdList) {
				String teacherName = "";
				if (teacherMap.containsKey(accountId)) {
					teacherName = teacherMap.get(accountId);
				}
				JSONObject temp = new JSONObject();
				temp.put("id", parentId + accountId);
				temp.put("text", teacherName);
				// temp.put("checked", "false");
				JSONObject attibute = new JSONObject();
				attibute.put("teacherId", accountId + "");
				attibute.put("teacherName", teacherName);
				temp.put("attributes", attibute);
				teacherArray.add(temp);
			}
		}
		JSONObject result = new JSONObject();
		if (teacherArray.size() > 0) {
			result.put("isEmpty", "false");
			result.put("teachers", teacherArray);
		} else {
			result.put("isEmpty", "true");
		}
		return result;
	}

	/**
	 * 导出验证结果
	 * 
	 * @return
	 */
	@RequestMapping(value = "/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(HttpServletRequest req, HttpServletResponse res) {
		JSONArray arr = JSONArray.parseArray(req.getParameter("param"));
		JSONArray excelHeads = new JSONArray();
		JSONArray line = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "rowNum");
		col.put("title", "行号");
		col.put("width", 120);
		line.add(col);

		col = new JSONObject();
		col.put("field", "msg");
		col.put("title", "错误描述");
		col.put("width", 800);
		line.add(col);
		excelHeads.add(line);

		JSONArray excelData = new JSONArray();
		for (int i = 0; i < arr.size(); i++) {
			JSONObject o = arr.getJSONObject(i);
			JSONObject row = new JSONObject();
			row.put("rowNum", o.get("row"));
			JSONArray cols = o.getJSONArray("mrows");
			String msg = "";
			for (int j = 0; j < cols.size(); j++) {
				JSONObject co = cols.getJSONObject(j);
				msg += co.getString("title") + "：" + co.getString("err") + "；";
			}

			row.put("msg", msg);
			excelData.add(row);
		}
		ExcelTool.exportExcelWithData(excelData, excelHeads, "错误信息", null, req, res);
	}

	/**
	 * 得到当前学期学年
	 * 
	 * @return 学期学年
	 */
	@RequestMapping(value = "nosession", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject nosession(HttpSession session) {
		JSONObject obj = new JSONObject();
		obj.put("code", -100);
		obj.put("msg", "会话失效，请重新登陆!");

		return obj;
	}

	/**
	 * 判断是否为当前模块管理员
	 */
	@RequestMapping(value = "getIsAppManager", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getBJByNJ(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject requestParams) {

		JSONObject rs = new JSONObject();
		String appId = requestParams.getString("appId");

		int code = 0;

		if (isMoudleManager(req, appId)) {
			code = 1;
		}

		rs.put("code", code);

		return rs;
	}
	
	
	@RequestMapping(value = "getOrganizationLevel", method = RequestMethod.POST)
 	@ResponseBody
	public JSONObject getOrganizationLevel(@RequestBody JSONObject params,
 			HttpServletRequest req, HttpServletResponse res) { 
 
		JSONObject json = new JSONObject();
		long schoolId = Long.parseLong(getXxdm(req));
		String termInfo = params.getString("xnxq");
		if (StringUtils.isEmpty(termInfo)) {
			termInfo = commonDataService.getCurTermInfoId(schoolId);
		}
		Map<Long, String> teacherMap = new HashMap<Long, String>();
		School sch = getSchool(req, termInfo);
		try {
			/** 声明所有的节点基础信息 */
			// 科室下的科室列表
			JSONArray data = new JSONArray();
			JSONObject department = new JSONObject();
			String departmentIds = "88881";
			department.put("id", departmentIds);
			department.put("text", "科室");
			// department.put("checked", "false");
			department.put("state", "closed");
			JSONArray dChildren = new JSONArray();

			// 年级下的年级列表
			JSONObject gradeGroup = new JSONObject();
			String gradeGroupIds = "88882";
			gradeGroup.put("id", gradeGroupIds);
			gradeGroup.put("text", "年级组");
			// gradeGroup.put("checked", "false");
			gradeGroup.put("state", "closed");
			List<JSONObject> gChildren = new ArrayList<JSONObject>();

			// 教研下的年级列表
			JSONObject researchGroup = new JSONObject();
			String researchGroupIds = "88883";
			researchGroup.put("id", researchGroupIds);
			researchGroup.put("text", "教研组");
			// researchGroup.put("checked", "false");
			researchGroup.put("state", "closed");
			JSONArray rChildren = new JSONArray();

			// 备课下的年级列表
			JSONObject preparation = new JSONObject();
			String preparationIds = "88884";
			preparation.put("id", preparationIds);
			preparation.put("text", "备课组");
			// preparation.put("checked", "false");
			preparation.put("state", "closed");
			List<JSONObject> pChildren = new ArrayList<JSONObject>();

			// 各部门的责任人列表
			JSONObject departHead = new JSONObject();
			String departHeadIds = "88885";
			departHead.put("id", departHeadIds);
			departHead.put("text", "部门负责人");
			departHead.put("state", "closed");
		 
			JSONArray hChildren = new JSONArray();
			// 部门责任人-科室
			JSONObject depHead = new JSONObject();
			String depHeadId =   "86";
			depHead.put("id", depHeadId);
			depHead.put("text", "科室负责人");
			 
			JSONArray dHeadArray = new JSONArray();
			// 部门责任人-年级组
			JSONObject gradeHead = new JSONObject();
			String gradeHeadId =   "82";
			gradeHead.put("id", gradeHeadId);
			gradeHead.put("text", "年级组负责人");
			 
			JSONArray gradeHeadArray = new JSONArray();
			// 部门责任人-教研组
			JSONObject researchHead = new JSONObject();
			String researchHeadId =  "81";
			researchHead.put("id", researchHeadId);
			researchHead.put("text", "教研组负责人");
			 
			JSONArray researchHeadArray = new JSONArray();
			// 部门责任人-备课组
			JSONObject preparationHead = new JSONObject();
			String preparationHeadId =   "83";
			preparationHead.put("id", preparationHeadId);
			preparationHead.put("text", "备课组负责人");
			 
			JSONArray preparationHeadArray = new JSONArray();

			// 班主任列表
			JSONObject headmaster = new JSONObject();
			headmaster.put("text", "班主任");
			// headmaster.put("checked", "false");
			headmaster.put("state", "closed");
			String headmasterId = "88886";
			headmaster.put("id", headmasterId);
			// 年级下的班主任列表
			JSONArray gradeArray = new JSONArray();

			// 所有的教师列表
 

 
 

			/** 查询学校-机构-教师,责任人 */
			List<OrgInfo> OrgList = commonDataService.getSchoolOrgList(sch, termInfo);
			logger.info("getOrganizationLevel:"+OrgList);
			for (OrgInfo orgInfo : OrgList) {
				JSONObject org = new JSONObject();
				if (orgInfo.getOrgType() == 1) { // 1:教研组，2:年级组，3:备课组，6:科室
					String parentId = "" + orgInfo.getId();
					// 教研组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
				 
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray2(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						//org.put("children", Fteacher.getJSONArray("teachers"));
						rChildren.add(org);
					}
					// 教研组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					JSONObject Tteacher = getTeacherArray2(parentId, headIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						researchHeadArray.add(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == 2) {
					String parentId = "" + orgInfo.getId();
					// 年级组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
				 
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray2(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						//org.put("children", Fteacher.getJSONArray("teachers"));
						gChildren.add(org);
					}
					// 年级组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					JSONObject Tteacher = getTeacherArray2(parentId, headIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						gradeHeadArray.add(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == 3) {
					String parentId = ""   + orgInfo.getId();
					// 备课组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					 
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray2(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						//org.put("children", Fteacher.getJSONArray("teachers"));
						pChildren.add(org);
					}
					// 备课组责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					JSONObject Tteacher = getTeacherArray2(parentId, headIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						preparationHeadArray.add(Tteacher.getJSONArray("teachers"));
					}
				} else if (orgInfo.getOrgType() == 6) {
					String parentId = "" + orgInfo.getId();
					// 科室组教师列表
					org.put("text", orgInfo.getOrgName());
					org.put("id", parentId);
					 
					List<Long> accountIdList = orgInfo.getMemberAccountIds();
					JSONObject Fteacher = getTeacherArray2(parentId, accountIdList, teacherMap);
					if ("false".equals(Fteacher.getString("isEmpty"))) {
						//org.put("children", Fteacher.getJSONArray("teachers"));
						dChildren.add(org);
					}
					// 科室责任人列表
					List<Long> headIdList = orgInfo.getHeaderAccountIds();
					JSONObject Tteacher = getTeacherArray2(parentId, headIdList, teacherMap);
					if ("false".equals(Tteacher.getString("isEmpty"))) {
						dHeadArray.add(Tteacher.getJSONArray("teachers"));
					}
				}
			}
			/*** 获取班主任的信息列表 */
			List<Grade> gradeList = commonDataService.getGradeList(sch, termInfo);
			for (Grade grade : gradeList) {
				JSONObject gObject = new JSONObject();
				String gradeIds = "" + grade.getId();
				gObject.put("id", gradeIds);
				String gradeName = AccountStructConstants.T_GradeLevelName.get((grade.getCurrentLevel()));
				gObject.put("text", gradeName);
				gradeArray.add(gObject);
			}

			/** 根据子节点信息添加父节点 */
			if (dChildren.size() > 0) {
				department.put("children", dChildren);
				data.add(department);
			}
			if (gChildren.size() > 0) {
				gradeGroup.put("children", Sort.sort(SortEnum.ascEnding0rder, gChildren, "text"));
				data.add(gradeGroup);
			}
			if (rChildren.size() > 0) {
				researchGroup.put("children", rChildren);
				data.add(researchGroup);
			}
			if (pChildren.size() > 0) {
				preparation.put("children", Sort.sort(SortEnum.ascEnding0rder, pChildren, "text"));
				data.add(preparation);
			}
			if (dHeadArray.size() > 0) {
			 
				hChildren.add(depHead);
			}
			if (gradeHeadArray.size() > 0) {
				 
				hChildren.add(gradeHead);
			}
			if (researchHeadArray.size() > 0) {
			 
				hChildren.add(researchHead);
			}
			if (preparationHeadArray.size() > 0) {
				 
				hChildren.add(preparationHead);
			}
			if (hChildren.size() > 0) {
				departHead.put("children", hChildren);
				data.add(departHead);
			}
			if (gradeArray.size() > 0) {
				headmaster.put("children", gradeArray);
				data.add(headmaster);
			}
 
			json.put("data", data);
			json.put("code", OutputMessage.querySuccess.getCode());
			json.put("msg", OutputMessage.querySuccess.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			json.put("msg", "查询到教师组织层次出错！！");
			json.put("code", OutputMessage.queryDataError.getCode());
		}
		return json;
	
		
	}	
	
	private JSONObject getTeacherArray2(String parentId,
 			List<Long> accountIdList,Map<Long,String> teacherMap ) {
 		JSONArray teacherArray = new JSONArray();
 		JSONObject result = new JSONObject();
 		if (CollectionUtils.isNotEmpty(accountIdList)) {
 			result.put("isEmpty", "false");
 			result.put("teachers", teacherArray);
 		}else {
 			result.put("isEmpty", "true");
		}
	 		return result;
 	} 
}
