package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.T_StageType;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.service.ScoreReportService;

@RequestMapping("/scoremanage1/scoreReport/")
@Controller
public class ScoreReportAction extends BaseAction {
	@Autowired
	private ScoreReportService reportService;
	
	@Autowired
	private AllCommonDataService commonDataService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	@RequestMapping(value = "getScoreReportTypeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONArray getScoreReportTypeList(HttpServletRequest request) {
		JSONArray response = new JSONArray();
		try {
			User user = (User) request.getSession().getAttribute("user");
			String termInfoId = getCurXnxq(request);
			School school = getSchool(request, termInfoId); // 学校信息

			String roleID = null;
			if (isMoudleManager(request, "cs1002")) {
				roleID = "*";
			} else {
				roleID = changeRole(user, school, termInfoId); // 获取当前用户所有角色代码
			}

			// 2.循环找到每个学校的培养层次，并且保证数字少的层次排在前面。
			StringBuffer stateType = new StringBuffer();
			List<T_StageType> stageList = school.getStage(); // 学习阶段，幼小初高
			if (stageList != null) {
				for (T_StageType state : stageList) {
					if (state.getValue() >= T_StageType.Primary.getValue()) { // 从小学开始计算
						stateType.append(state.getValue() - 1).append(",");
					}
				}
			}
			if (stateType.length() > 0) {
				stateType.deleteCharAt(stateType.length() - 1);
			}

			response = reportService.getScoreReportTypeList(String.valueOf(school.getId()), stateType.toString(),
					roleID);
		} catch (CommonRunException e) {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 转换用户角色
	 * 
	 * @param user
	 *            用户
	 * @param schooId
	 *            学校编号
	 * @return
	 */
	private String changeRole(User user, School school, String selectedSemester) {
		// 1.定义参数
		List<String> roleIDs = new ArrayList<String>();
		int roleType = user == null ? 0 : user.getUserPart().getRole().getValue();// 角色类型

		// 2.根据身份类型计算角色编号
		List<OrgInfo> orgs = commonDataService.getSchoolOrgList(school, selectedSemester); // 学校机构

		List<Long> orgIds = user.getUserPart().getDeanOfOrgIds(); // 当前用户（机构的领导）管理的机构代码

		if (roleType == T_Role.Teacher.getValue()) {// 老师身份
			TeacherPart tp = user.getTeacherPart();
			if (tp != null) {
				List<Long> deanOfClassId = tp.getDeanOfClassIds(); // 当前用户作为班主任所管理的班级代码
				List<Course> courlist = tp.getCourseIds(); // 所教科目信息
				if (CollectionUtils.isNotEmpty(orgIds) && CollectionUtils.isNotEmpty(orgs)) { // 如果是普通老师又是其他机构的领导
					for (Long orgId : orgIds) {
						for (OrgInfo org : orgs) {
							if (orgId == org.getId()) {// 找到对应的机构
								int orgType = org.orgType;// 结构类型
								if (orgType == T_OrgType.T_Teach.getValue()) {// 教研组
									roleIDs.add("04"); // 教研组组长
								} else if (orgType == T_OrgType.T_Grade.getValue()) {// 年级组
									roleIDs.add("03"); // 年级组组长
								} else if (orgType == T_OrgType.T_PreLesson.getValue()) {// 备课组
									roleIDs.add("05"); // 备课组组长
								} else if (orgType == T_OrgType.T_Manage.getValue()) {// 校领导
									roleIDs.add("06"); // 校领导
								}
								break;
							}
						}
					}
				}
				if (CollectionUtils.isNotEmpty(deanOfClassId)) {
					roleIDs.add("02");// 班主任
				}
				if (CollectionUtils.isNotEmpty(courlist)) {
					roleIDs.add("01");// 任课老师
				}
			}
		} else if (roleType == T_Role.Staff.getValue()) { // 教研组长 年级组长 备课组长等
			// 循环该用户的所有机构
			if (CollectionUtils.isNotEmpty(orgIds) && CollectionUtils.isNotEmpty(orgs)) {
				for (Long orgId : orgIds) {
					for (OrgInfo org : orgs) {
						if (orgId == org.getId()) {// 找到对应的机构
							int orgType = org.orgType;// 结构类型
							if (orgType == T_OrgType.T_Teach.getValue()) {// 教研组
								roleIDs.add("04");
							} else if (orgType == T_OrgType.T_Grade.getValue()) {// 年级组
								roleIDs.add("03");
							} else if (orgType == T_OrgType.T_PreLesson.getValue()) {// 备课组
								roleIDs.add("05");
							} else if (orgType == T_OrgType.T_Manage.getValue()) {// 校领导
								roleIDs.add("06");
							}
							break;
						}
					}
				}
			}
		} else {// 其它身份
			roleIDs.add(String.valueOf(roleType));
		}
		// 2.返回
		return StringUtils.join(roleIDs, ",");
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
			// 1.定义参数,获取参数值
			String termInfoId = request.getString("selectedSemester");
			if (StringUtils.isEmpty(termInfoId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			String examId = request.getString("examId"); // 考试编号
			params.put("kslcdm", examId);
			params.put("kslc", examId);
			params.put("xnxq", termInfoId);
			params.put("xxdm", getXxdm(req));
			params.put("nj", request.get("useGradeId"));

			params.put("isAll", request.getIntValue("isAll"));
			params.put("examType", request.get("examType"));
			params.put("isTotal", request.get("isTotal"));
			params.put("isModify", request.get("isModify"));
			params.put("type", request.get("type"));

			response.put("data", reportService.getExamSubjectList(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}

		return response;
	}

	/**
	 * 2. 学生成绩明细表
	 * 
	 * @author 魏春林
	 * @param req
	 * @param res
	 * @return
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容) update by guoyuanbing
	 *         </p>
	 */
	@RequestMapping(value = "getClassScoreReportList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassScoreReportList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		// 1.获取参数，处理部分数据特殊情况，获取基础数据，设置调用参数Map
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgID");
			String bhStr = request.getString("classId");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(synj) || StringUtils.isBlank(kslc)
					/*|| StringUtils.isBlank(bmfz) */|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xnxq", xnxq);
			params.put("bmfz", bmfz);
			params.put("nj", synj);
			params.put("bhStr", bhStr);
			
			
			if(null!=request.getString("subjectId") && request.getString("subjectId").split(",").length==1){
				params.put("subjectId", request.get("subjectId"));				
			}
			
			params.put("xmxh", request.get("stdNumOrName"));
			params.put("topXRank", request.get("topXTotalGrade")); // 前X名 qpm
			params.put("lastXRank", request.get("lastXTotalGrade")); // 后X名 hpm
			params.put("topXTotalPer", request.get("topXTotalPer"));
			params.put("lastXTotalPer", request.get("lastXTotalPer"));

			if(StringUtils.isEmpty(bmfz)){
				request.put("schoolId", getXxdm(req));
				request.put("termInfoId", xnxq);
				request.putAll(params);
				//params.put("placementId", request.get("placementId"));
				//params.put("scheduleId", request.get("scheduleId"));
				response.putAll(reportService.getClassScoreReportInfo(request));
			}else{
				response.putAll(reportService.getClassScoreReportInfo(params));
			}
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 年级报告
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param param
	 * @return
	 *         <p>
	 */
	@RequestMapping(value = "getGradeReportList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradeReportList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		// 1.获取参数，处理部分数据特殊情况，获取基础数据，设置调用参数Map
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String synj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgID");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(synj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xnxq", xnxq);
			params.put("bmfz", bmfz);
			params.put("nj", synj);

			response.putAll(reportService.getGradeReportList(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/****
	 * 12.历次等第成绩对比表
	 * 
	 * @param param
	 * @return
	 */
	@RequestMapping(value = "getAllPreviousLevelCompareList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllPreviousLevelCompareList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String kmdmStr = request.getString("subjectId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String termInfoRange = request.getString("termInfoRange");
			if (StringUtils.isBlank(kmdmStr) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)
					|| StringUtils.isBlank(termInfoRange)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kmdmStr", kmdmStr);
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("termInfoRange", termInfoRange);
			params.put("xxdm", getXxdm(req));

			response.putAll(reportService.getAllPreviousLevelCompareList(getSchool(req, xnxq), params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}

		return response;
	}

	/****
	 * 15. 竞赛学生成绩分析表
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getCompetiteStuAnalysisList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCompetiteStuAnalysisList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			// 1.获取参数，判断空置，并且加以处理
			String nj = request.getString("usedGradeId");// 使用年级,多个使用年级用逗号分隔
			String kmdmStr = request.getString("subjectId");// 科目代码，多个班级用逗号分隔
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			if (StringUtils.isBlank(nj) || StringUtils.isBlank(kmdmStr) || StringUtils.isBlank(xnxq)
					|| StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("kmdmStr", kmdmStr);

			response.putAll(reportService.getCompetiteStuAnalysisList(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}

		return response;
	}

	/****
	 * 19. 科目前N名统计表
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getSubjectTopNStatisList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSubjectTopNStatisList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String fzdm = request.getString("asgId");
			Integer topNRank = request.getInteger("topNRank");
			if (StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)
					|| StringUtils.isBlank(fzdm) || topNRank == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("bmfz", fzdm);
			params.put("topNRank", topNRank);

			response.putAll(reportService.getSubjectTopNStatisList(getSchool(req, xnxq), params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 考试名称，统计类别接口(历次成绩趋势表)
	 * 
	 * @return
	 */
	@RequestMapping(value = "getExamNameList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamNameList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			Integer termInfoRange = request.getInteger("termInfoRange");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || termInfoRange == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("termInfoRange", termInfoRange);

			response.put("examList", reportService.getExamNameList(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 考试名称，统计类别接口(历次成绩趋势表)
	 * 
	 * @return
	 */
	@RequestMapping(value = "getStatisTypeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStatisTypeList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);

			response.put("data", reportService.getStatisTypeList(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * （2）历次成绩趋势表接口(历次成绩趋势表)
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getAllPreviousTrendList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getAllPreviousTrendList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String statisTypeId = request.getString("statisTypeId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			if (StringUtils.isBlank(statisTypeId) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcStr", request.get("examId"));
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			// 统计类别编号1:总平均分排名; 02:全A人数; 03:次A1B人数; 04:合格率; 05:优秀率
			params.put("statisTypeId", statisTypeId);

			response.put("data", reportService.getAllPreviousTrendList(params));

			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 22. 班级报告
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getClassReportList", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getClassReportList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String bh = request.getString("classId");
			String kslc = request.getString("examId");
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			if (StringUtils.isBlank(bh) || StringUtils.isBlank(kslc) || StringUtils.isBlank(xnxq)
					|| StringUtils.isBlank(nj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("bh", bh);

			response.putAll(reportService.getClassReportList(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 23. 学生成绩报告
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getStudentScoreReportList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentScoreReportList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			String termInfoId = request.getString("termInfoId");
			Integer type = request.getInteger("type");
			if (StringUtils.isBlank(examId) || StringUtils.isBlank(termInfoId) || type == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			request.put("kslcdm", examId); // 符合t_gm_degreeinfo表模式
			request.put("xnxq", termInfoId); // 符合t_gm_degreeinfo表模式
			String schoolId = getXxdm(req);
			request.put("schoolId", getXxdm(req));
			request.put("xxdm", schoolId); // 符合t_gm_degreeinfo表模式

			HttpSession session = req.getSession();
			User user = (User) session.getAttribute("user");
			if (user == null || user.getUserPart() == null) {
				throw new CommonRunException(-1, "无法获取用户信息，请联系管理员！");
			}
			request.put("accountId", user.getAccountPart().getId()); // 账户代码
			if (T_Role.Parent.equals(user.getUserPart().getRole())) {
				long userId = user.getParentPart().getStudentId();
				user = commonDataService.getUserById(Long.valueOf(schoolId), userId, termInfoId);
			}
			if (user == null) {
				throw new CommonRunException(-1, "无法获取用户信息，请联系管理员！");
			}
			long studentId = user.getAccountPart().getId(); // 学生代码
			String studName = user.getAccountPart().getName();
			long classId = user.getStudentPart().getClassId();

			request.put("studentId", studentId);
			request.put("studName", studName);
			request.put("classId", classId);
			response.put("data", reportService.getStudentScoreReportList(request));
			reportService.insertStudentScoreReportTrace(request);
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/***
	 * 25 成绩查看（学生或家长）
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getScoreReportViewList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreReportViewList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			Integer type = request.getInteger("type"); // 0:当前学期，1：历年
			if (type == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			User user = (User) req.getSession().getAttribute("user");
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
			response.put("data", data);
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/***
	 * 26 成绩详情查看（学生或家长）
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getScoreReportViewDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreReportViewDetail(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String examId = request.getString("examId");
			if(StringUtils.isEmpty(examId)){
				throw new CommonRunException(-1, "请求参数examId不能为空！");
			}
			
			JSONObject oriData = getScoreReportViewList(request, req);
			
			if(null!=oriData && oriData.size()>0){
				JSONArray dataList = oriData.getJSONArray("data");
				if(CollectionUtils.isNotEmpty(dataList)){
					for(Object obj : dataList){
						JSONObject singleData = (JSONObject)obj;
						if(examId.equals(singleData.get("examId"))){
							response.put("data", singleData);						
							break;
						}
					}
				}
			
			}
					
			
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	
	/**
	 * 27 学生成绩单
	 */
	/**
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getStudentScoreReportListByTea", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentScoreReportListByTea(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String stdNumOrName = request.getString("stdNumOrName");
			if (StringUtils.isBlank(stdNumOrName)) {
				throw new CommonRunException(-1, "必须输入学号/姓名");
			}
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			Integer termInfoRange = request.getInteger("termInfoRange");
			String bhStr = request.getString("classId");

			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || termInfoRange == null
					|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("termInfoRange", termInfoRange);
			params.put("bhStr", bhStr);
			params.put("stdNumOrName", stdNumOrName);

			response.put("data", reportService.getStudentScoreReportListByTea(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	private static List<Map<String, Object>> getLesson(List<JSONObject> l, List<LessonInfo> lelist) {

		TreeSet<String> set = new TreeSet<String>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, LessonInfo> ma = new HashMap<String, LessonInfo>();
		List<String> list = new ArrayList<String>();
		List<String> alist = new ArrayList<String>();
		for (LessonInfo le : lelist) {
			ma.put(String.valueOf(le.getId()), le);
		}
		if (l != null && l.size() > 0) {
			for (Map<String, Object> m : l) {
				set.add(m.get("lessonId").toString());
			}
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String str = it.next();
				if (str.equals("totalScore")) {
					alist.add(str);
				} else {
					list.add(str);
				}
			}
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					int gg = Integer.valueOf(o1);
					int gg1 = Integer.valueOf(o2);
					return gg < gg1 ? -1 : 1;
				}

			});
			list.addAll(alist);
			for (String str : list) {
				if (ma.containsKey(str)) {
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("lessonName", ma.get(str).getName());
					m.put("lessonId", str);
					result.add(m);
				}
			}
		}

		return result;
	}

	/**
	 * 学生成绩单导出 多带2个标题
	 * 
	 * @param req
	 * @param res
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportStudentScoreReportListByTea")
	@ResponseBody
	public void exportStudentScoreReportListByTea(HttpServletRequest req, HttpServletResponse res) throws Exception {
		String data = req.getParameter("excelData");
		JSONObject gg = JSONObject.parseObject(data);
		JSONArray firstExcelHeads = JSONArray.parseArray(gg.getString("columns"));
		JSONArray firstExcelData = JSONArray.parseArray(gg.getString("Studentdata"));
		ExcelTool.exportExcelWithTea(firstExcelData, firstExcelHeads, "学生成绩单", null, req, res);
	}

	/**
	 * 学生优化成绩单导出 多带2个标题
	 * 
	 * @param req
	 * @param res
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportStudentOptimization")
	@ResponseBody
	public void exportStudentOptimization(HttpServletRequest req, HttpServletResponse res) throws Exception {
		String data = req.getParameter("excelData");
		JSONObject gg = JSONObject.parseObject(data);
		JSONArray firstExcelHeads = JSONArray.parseArray(gg.getString("columns"));
		JSONArray firstExcelData = JSONArray.parseArray(gg.getString("Studentdata"));
		ExcelTool.exportExcelWithTea(firstExcelData, firstExcelHeads, "学生优化成绩单", null, req, res);
	}

	/**
	 * 2. 学生成绩明细表
	 * 
	 * @author 魏春林
	 * @param req
	 * @param res
	 * @return
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容) update by guoyuanbing
	 *         </p>
	 */
	@RequestMapping(value = "getStudentOptimizationList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentOptimizationList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			String bmfz = request.getString("asgID");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bhStr) || StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("bmfz", bmfz);
			params.put("xmxh", request.getString("stdNumOrName"));
			params.put("xxdm", getXxdm(req));

			response.putAll(reportService.getStudentOptimizationList(params));

		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 学生优化成绩单
	 */
	/**
	 * @param param
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "getStudentOptimization", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getStudentOptimization(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String stdNumOrName = request.getString("stdNumOrName");
			if (StringUtils.isBlank(stdNumOrName)) {
				throw new CommonRunException(-1, "必须输入学号/姓名！");
			}
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String bhStr = request.getString("classId");
			Integer termInfoRange = request.getInteger("termInfoRange");
			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(bhStr)
					|| termInfoRange == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("termInfoRange", termInfoRange);
			params.put("xmxh", stdNumOrName);
			params.put("xxdm", getXxdm(req));

			response.put("data", reportService.getStudentOptimization(params));

			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}
	
 
}
