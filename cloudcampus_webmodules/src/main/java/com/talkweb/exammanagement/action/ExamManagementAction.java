package com.talkweb.exammanagement.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.exammanagement.service.ExamManagementArrangeExamService;
import com.talkweb.exammanagement.service.ExamManagementExamPlanService;
import com.talkweb.exammanagement.service.ExamManagementHomepageService;

@Controller
@RequestMapping("/examManagement")
public class ExamManagementAction extends BaseAction {
	Logger logger = LoggerFactory.getLogger(ExamManagementAction.class);

	@Autowired
	private ExamManagementHomepageService examManagementHomepageService;
	
	@Autowired
	private ExamManagementArrangeExamService examManagementArrangeExamService;
	
	@Autowired
	private ExamManagementExamPlanService examManagementExamPlanService;
	
	private final String errorMsg = "操作异常，请联系管理员！";

	@RequestMapping(value = "/homepage/getExamManagementList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamManagementList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementHomepageService.getExamManagementList(request));
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/homepage/createOrUpdateExamManagement", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateExamManagement(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("accountId", req.getSession().getAttribute("accountId"));
			request.put("schoolId", getXxdm(req));
			examManagementHomepageService.insertOrupdateExamManagement(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/homepage/deleteExamManagement", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteExamManagement(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			examManagementHomepageService.deleteExamManagement(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/examPlan/getExamPlanList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlanList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementExamPlanService.getExamPlanList(request));
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/examPlan/deleteExamPlan", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteExamPlan(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			examManagementExamPlanService.deleteExamPlan(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/examPlan/getExamPlan", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlan(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementExamPlanService.getExamPlan(request));
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/examPlan/saveExamPlan", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveExamPlan(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			examManagementExamPlanService.saveExamPlan(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/arrangeExam/getArrangeExamInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getArrangeExamInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			
			String examSubjectIdsStr = (String) request.remove("examSubjectIds");
			String[] temp = examSubjectIdsStr.split(",");
			
			Set<String> examSubjectIds = new HashSet<String>();
			for(String examSubjectId : temp) {
				examSubjectIds.add(examSubjectId);
			}
			
			request.put("examSubjectIds", examSubjectIds);
			response.put("data", examManagementArrangeExamService.getArrangeExamInfo(request));
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/arrangeExam/arrangeExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject arrangeExam(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			
			String examSubjectIdStr = (String) request.remove("examSubjectIds");
			List<String> examSubjectIds = StringUtil.convertToListFromStr(examSubjectIdStr, ",", String.class);
			request.put("examSubjectIds", examSubjectIds);
			
			String tClassIdStr = (String) request.remove("tClassIds");
			List<String> tClassIds = StringUtil.convertToListFromStr(tClassIdStr, ",", String.class);
			request.put("tClassIds", tClassIds);
			
			examManagementArrangeExamService.arrangeExam(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/arrangeExam/delArrangeExamInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delArrangeExamInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			examManagementArrangeExamService.deleteArrangeExamInfo(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/arrangeExam/autoArrangeExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject autoArrangeExam(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			
			String examPlanIds = request.getString("examPlanIds");
			if(StringUtils.isBlank(examPlanIds)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			request.put("examPlanIds", StringUtil.convertToListFromStr(examPlanIds, ",", String.class));
			
			String redisKey = "examManagement.execprocess.progress." + req.getSession().getId();
			request.put("redisKey", redisKey);
			
			examManagementArrangeExamService.autoArrangeExam(request);
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/arrangeExam/queryProgress", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryProgress(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			String redisKey = "examManagement.execprocess.progress." + req.getSession().getId();
			request.put("redisKey", redisKey);
			response.put("data", examManagementArrangeExamService.queryProgress(request));
		} catch (CommonRunException e) {
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
}
