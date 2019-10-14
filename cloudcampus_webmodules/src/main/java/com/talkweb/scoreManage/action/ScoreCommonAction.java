package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
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
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.datadictionary.domain.TDmBjlx;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ScoreCommonService;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName: ScoreManageAction.java
 * @version:1.0
 * @Description: 成绩公共管理控制器
 * @author 武洋 ---智慧校
 * @date 2015年3月25日
 */
@Controller
@RequestMapping(value = "/scoremanage1/")
public class ScoreCommonAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(ScoreCommonAction.class);

	@Autowired
	private ScoreManageService scoreService;

	@Autowired
	private ScoreCommonService scoreCommonService;

	@Autowired
	private AllCommonDataService commonDataService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	@RequestMapping(value = "common/getExamGradeList")
	@ResponseBody
	public JSONObject getExamGradeList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			int isAll = request.getIntValue(("isAll"));
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("isAll", isAll);
			params.put("xxdm", getXxdm(req));

			List<JSONObject> data = scoreCommonService.getExamGradeList(params);
			response.put("data", data);
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("data", new ArrayList<JSONObject>());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "后台异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}

	/**
	 * 获取班级 还未添加权限判断、当前学年学期获取 具有考试成绩
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "common/getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(HttpServletRequest req, HttpServletResponse res, @RequestBody JSONObject request) {
		JSONObject response = new JSONObject();
		try {
			String usedGrade = request.getString("usedGrade");
			String xnxq = request.getString("termInfoId");
			if (StringUtils.isBlank(usedGrade) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			int isAll = request.getIntValue("isAll");

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("isAll", isAll);
			params.put("xxdm", getXxdm(req));
			params.put("usedGradeList", StringUtil.convertToListFromStr(usedGrade, ",", String.class));
			List<JSONObject> data = scoreCommonService.getClassList(params);
			response.put("data", data);
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("data", new ArrayList<JSONObject>());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "后台异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}
	
	
	@RequestMapping(value = "common/getExamClassList")
	@ResponseBody
	public JSONObject getExamClassList( @RequestBody JSONObject request , HttpServletRequest req, HttpServletResponse res) {
//		logger.info("getExamClassList: "+request);
		JSONObject response = new JSONObject();
		String examId = request.getString("examId");
		String termInfoId = request.getString("termInfoId");
		Long userId = request.getLong("userId");
		Long schoolId = request.getLong("schoolId");
		request.put("xnxq", termInfoId);
		request.put("kslcdm" , examId);
		request.put("kslc", examId);
		DegreeInfo info = scoreService.getDegreeInfoById(request);
		Integer autoIncr = info.getAutoIncr();
		request.put("autoIncr", autoIncr);
		List<JSONObject> classList = scoreCommonService.getExamClassList(request);
//		logger.info("classList："+JSONObject.toJSONString(classList));
		List<Long> classes = new ArrayList<Long>();
		if (classList!=null) {
			for (int i = 0; i < classList.size(); i++) {
				classes.add(classList.get(i).getLong("bh"));
			}
		}

		List<Long> classIds = new ArrayList<Long>();
		User user = commonDataService.getUserById(schoolId, userId, termInfoId);
//		logger.info("user: "+JSONObject.toJSONString(user));
		if (user.getTeacherPart()!=null) {
			List<Long> classId = user.getTeacherPart().getDeanOfClassIds() ;
			if (classId!=null ) {
				classIds.addAll( classId);//班主任
			}
			List<Course> courses = user.getTeacherPart().getCourseIds();
			if (courses!= null) {
				for (int j = 0; j < courses.size(); j++) {
					classIds.add(courses.get(j).getClassId());
				}
			}
		}
 
		Iterator<Long>  iterator =   classes.iterator();
		while (iterator.hasNext()) {
			 if (!classIds.contains(iterator.next())) {
				 iterator.remove();
			 }
		}
		List<JSONObject> classroomList = new ArrayList<JSONObject>();
		if (classes!= null && classes.size() > 0) {
			List<Classroom> classrooms= commonDataService.getClassroomBatch(schoolId, classes, termInfoId);
		    for (int i = 0; i < classrooms.size(); i++) {
		    	JSONObject obj = new JSONObject();
		    	Classroom room = classrooms.get(i);
		    	obj.put("classId", room.getId());
		    	obj.put("className", room.getClassName());
		    	classroomList.add(obj);		
		    }
		}
		
		 Collections.sort( classroomList , new Comparator<JSONObject>(){
			  public int compare(JSONObject p1, JSONObject p2) { 
		            return (p1.getString("className")).compareTo(p2.getString("className"));  
		        }
		});
		
		response.put("code", 0);
		response.put("msg", "");
		response.put("data", classroomList);
	    return response;
	}
	

	@RequestMapping(value = "getHomePageTabRight", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getHomePageTabRight(HttpServletRequest req, HttpServletResponse res) {

		// 1.获取参数，并处理空数据特殊情况,判断当前用户的角色。
		String menuId = "cs1002";
		// 2.组织返回结果数据 .(创建考试 01，成绩查看02，报表设置03,班内测试04)
		Map<String, Object> result = new HashMap<String, Object>();
		// 默认设置所有tab页面不显示
		result.put("01", 0);
		result.put("02", 0);
		result.put("03", 0);
		result.put("04", 0);
		// 3.根据角色类型确定属于什么角色，设置tab查看权限。
		if (isMoudleManager(req, menuId)) {
			result.put("01", 1);
			result.put("02", 1);
			result.put("03", 1);
		} else {
			result.put("02", 1);// 老师查看成绩的权限
			result.put("04", 1);// 老师查看班内测试的权限
		}
		HttpSession session = req.getSession();
		if (session != null && session.getAttribute("isTeaching") != null) {
			boolean isTeaching = (Boolean) session.getAttribute("isTeaching");
			if (isTeaching) {
				result.put("02", 1);// 老师查看成绩的权限
				result.put("04", 1);// 老师查看班内测试的权限
			}
		}
		// 4.返回结果
		return result;
	}

	/**
	 * 成绩分析--获取文理科分组列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "common/getASGList")
	@ResponseBody
	public JSONObject getASGList(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId").trim();
			int isAll = request.getIntValue("isAll");
			String synj = request.getString("usedGrade").trim();

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("isAll", isAll);
			if (StringUtils.isNotEmpty(synj)) {
				params.put("njList", Arrays.asList(synj.split(",")));
			}
			List<JSONObject> list = scoreCommonService.getwlkGroupList(params);
			response.put("data", list);
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("data", new ArrayList<JSONObject>());
		} catch (Exception e) {
			setResponse(response, -1, "未检索到文理分组！");
			e.printStackTrace();
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}

	/**
	 * 成绩分析--获取班级分组列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "common/getClassGroupList")
	@ResponseBody
	public JSONObject getClassGroupList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId").trim();
			String kslcdm = request.getString("examId").trim();
			int isAll = request.getIntValue("isAll");
			String synj = request.getString("usedGrade").trim();
			String bmfz = request.getString("asgId");

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslcdm);
			params.put("isAll", isAll);
			if (StringUtils.isNotEmpty(synj)) {
				params.put("njList", StringUtil.convertToListFromStr(synj, ",", String.class));
			}
			if (StringUtils.isNotEmpty(bmfz)) {
				params.put("fzList", StringUtil.convertToListFromStr(bmfz, ",", String.class));
			}
			List<JSONObject> list = scoreService.getBjfzList(params);
			response.put("data", list);
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("data", new ArrayList<JSONObject>());
		} catch (Exception e) {
			setResponse(response, -1, "后台异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "common/getClassTypeList")
	@ResponseBody
	public JSONObject getClassTypeList(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			JSONArray rs = new JSONArray();
			List<TDmBjlx> bjlx = commonDataService.getClassTypeList();
			for (TDmBjlx bl : bjlx) {
				JSONObject obj = new JSONObject();
				obj.put("text", bl.getMc());
				obj.put("value", bl.getDm());
				rs.add(obj);
			}
			response.put("data", rs);
			setResponse(response, 1, "");
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 判断权限科目下拉列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getRightSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRightSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			int isAll = request.getIntValue("isAll");// 0不允许显示全部选项，1允许显示全部选项
			String useGradeId = request.getString("useGradeId");// 使用年级
			String examId = request.getString("examId");// 考试编号
			String termInfoId = request.getString("selectedSemester");
			Integer type = request.getInteger("type");
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			if (StringUtils.isBlank(examId)) {
				setResponse(response, 1, "");
				response.put("data", new ArrayList<JSONObject>());
				return response;
			}

			// 考试下的科目
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", examId);
			params.put("kslcdm", examId);
			params.put("xnxq", termInfoId);
			params.put("isAll", isAll);
			params.put("type", type);
			if (StringUtils.isNotEmpty(useGradeId)) {
				params.put("njList", StringUtil.convertToListFromStr(useGradeId, ",", String.class));
			}
			response.put("data", scoreCommonService.getExamSubjectDropDownList(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 查询文理分组下的班级
	 * 
	 * @param session
	 *            会话
	 * @return 班级列表
	 */
	@RequestMapping(value = "common/getClassListByGroupId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassListByGroupId(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			if (termInfoId == null) {
				termInfoId = request.getString("selectedSemester");
			}
			String groupId = request.getString("groupId");
			
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			if(StringUtils.isBlank(kslc) || StringUtils.isBlank(groupId)) {
				setResponse(response, 1, "");
				response.put("data", new ArrayList<JSONObject>());
			}
			
			String isAll = request.getString("isAll");
			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("isAll", isAll);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			if (StringUtils.isNotEmpty(groupId)) {
				params.put("bmfzList", StringUtil.convertToListFromStr(groupId, ",", String.class));
			}
			List<JSONObject> classList = scoreCommonService.getClassListByGroupId(params);
			response.put("data", classList);
			
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			e.printStackTrace();
			setResponse(response, -1, "后台异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 获取考试名称
	 * 
	 * @return
	 */
	@RequestMapping(value = "common/getExamNameList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamNameList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGrade");
			boolean isAdmin = isMoudleManager(req, "cs1002");

			JSONObject params = new JSONObject();
			if (!isAdmin) {
				params.put("fbteaflag", 1);
			}
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			if (StringUtils.isNotEmpty(synj)) {
				params.put("njList", Arrays.asList(synj.split(",")));
			}
			response.put("data", scoreCommonService.getExamNameList(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			e.printStackTrace();
			setResponse(response, -1, "未检索到考试！");
		}
		return response;
	}

	/**
	 * 查询文理分组下教师所教的班级
	 * 
	 * @param session
	 *            会话
	 * @return 班级列表
	 */
	@RequestMapping(value = "common/getTeacherClassListByGroupId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherClassListByGroupId(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			Log.info("getTeacherClassListByGroupId start..");
			String kslc = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			String isAll = request.getString("isAll");
			String groupId = request.getString("groupId");

			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			if (StringUtils.isBlank(kslc)) {
				setResponse(response, 1, "");
				response.put("data", new ArrayList<JSONObject>());
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("isAll", isAll);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			if (StringUtils.isNotEmpty(groupId)) {
				params.put("bmfzList", StringUtil.convertToListFromStr(groupId, ",", String.class));
			}
			List<JSONObject> classList = scoreCommonService.getClassListByGroupId(params);

			request.put("appId", "cs1002");
			request.put("isAll", "0");
			//List<JSONObject> classRightList = getClassRightList(req, request);
			List<JSONObject> classRightList =  getClassList(req,request);
			Map<String, JSONObject> classRightListMap = StringUtil.convertJSONObjectToMap(classRightList, "value");
			List<JSONObject> newClassList = new ArrayList<JSONObject>();
			List<String> namelist = new ArrayList<String>();
			Log.info("getTeacherClassListByGroupId scoreclassRightList:"+classRightList);
			for (JSONObject c : classList) {
				String text = StringUtil.transformString(c.get("text"));
				String key = StringUtil.transformString(c.get("value"));
				if (classRightListMap.containsKey(key) && !text.equals("全部")) {
					newClassList.add(c);
					namelist.add(key);
				}
			}

			if (isAll.equals("1") && classRightListMap != null) { // 不止全部
				String nlist = StringUtils.join(namelist, ",");
				JSONObject alljon = new JSONObject();
				if (newClassList.size() == 0) {
					response.put("code", 1);
					response.put("data", newClassList);
					return response;
				}
				alljon.put("text", "全部");
				alljon.put("value", nlist);
				newClassList.add(0, alljon);
				JSONObject all = newClassList.get(0);
				newClassList.remove(0);
				Collections.sort(newClassList, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject arg1, JSONObject arg2) {
						String text1 = arg1.getString("text");
						String text2 = arg2.getString("text");
						return text1.compareTo(text2);
					}
				});
				newClassList.add(0, all);
				response.put("data", newClassList);
			} else {
				Collections.sort(newClassList, new Comparator<JSONObject>() {
					@Override
					public int compare(JSONObject arg1, JSONObject arg2) {
						String text1 = arg1.getString("text");
						String text2 = arg2.getString("text");
						return text1.compareTo(text2);
					}
				});
				response.put("data", newClassList);
			}
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			e.printStackTrace();
			setResponse(response, -1, "后台异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 无权限过滤获取班级列表 具有考试成绩的班级列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "common/getNoRightClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoRightClassList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			if (StringUtils.isBlank(kslc)) {
				setResponse(response, 1, "");
				response.put("data", new ArrayList<JSONObject>());
				return response;
			}

			int isAll = request.getIntValue("isAll");
			String nj = request.getString("usedGrade");
			String termInfoId = request.getString("selectedSemester");
			if(StringUtils.isBlank(termInfoId)) {
				termInfoId = request.getString("termInfoId");
			}
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", termInfoId);
			params.put("nj", nj);
			params.put("isAll", isAll);
			response.put("data", scoreCommonService.getExamClassDropDownList(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			response.put("data", new ArrayList<JSONObject>());
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * 无权限过滤获取教师所教班级列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "common/getNoRightTeacherClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNoRightTeacherClassList(@RequestBody JSONObject requestParams, HttpServletRequest req,
			HttpServletResponse res) {
		List<JSONObject> rs = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		String msg = "";
		int code = 0;
		try {
			String xnxq = requestParams.getString("termInfoId");
			String frontXnxq = requestParams.getString("selectedSemester");
			if (frontXnxq != null && frontXnxq.trim().length() > 0) {
				xnxq = frontXnxq;
			}
			String kslc = requestParams.getString("examId");
			int isAll = requestParams.getInteger("isAll");
			String usedGrade = requestParams.getString("usedGrade");

			JSONObject params = new JSONObject();
			if(StringUtils.isNotBlank(kslc)) {
				params.put("xxdm", getXxdm(req));
				params.put("kslc", kslc);
				params.put("kslcdm", kslc);
				params.put("xnxq", xnxq);
				params.put("nj", usedGrade);
				params.put("isAll", 1);	// 要全部数据，方便后面使用
				rs.addAll(scoreCommonService.getExamClassDropDownList(params));
			}

			params.clear();
			params.put("appId", "cs1002");
			params.put("isAll", "0");
			params.put("termInfoId", xnxq);
			params.put("usedGradeId", requestParams.getString("usedGrade"));
			List<JSONObject> classRightList = getClassList(req, params);
			logger.info("classRightList:"+classRightList.toString());
			JSONObject allItem = null;
			if(rs.size() > 0) {
				allItem = rs.remove(0);	// 全部数据
			}
			
			Map<String, JSONObject> classRightListMap = StringUtil.convertJSONObjectToMap(classRightList, "value");
			List<JSONObject> newClassList = new ArrayList<JSONObject>();
			StringBuffer allVal = new StringBuffer();
			if (classRightListMap != null) {
				for (JSONObject c : rs) {
					String key = StringUtil.transformString(c.get("value"));
					if (classRightListMap.containsKey(key)) {
						newClassList.add(c);
						allVal.append(key).append(",");
					}
				}
			}
			logger.info("classRightListMap:"+classRightListMap);
			if (newClassList.size() > 0) {
				rs = newClassList;
				if(isAll == 1) {
					if (allVal.length() > 0) {
						allVal.deleteCharAt(allVal.length() - 1);
					}
					JSONObject item = new JSONObject();
					item.put("value", allVal.toString());
					item.put("text", "全部");
					rs.add(0, item);
				}
			} else {
				if (isAll == 1) {
					if(allItem != null) {
						rs.add(0, allItem);
					}  
				}else {   
					rs.clear(); // 没有任教的普通老师全部清掉班级 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = -1;
			msg = "未检索到班级！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", rs);
		return json;
	}
	
 
	
	
}
