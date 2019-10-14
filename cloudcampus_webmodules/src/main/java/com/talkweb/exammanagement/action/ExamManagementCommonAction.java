package com.talkweb.exammanagement.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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
import com.talkweb.exammanagement.service.ExamManagementCommonService;
import com.talkweb.exammanagement.service.ExamManagementExamPlaceService;

@Controller
@RequestMapping("/examManagement/common")
public class ExamManagementCommonAction extends BaseAction {
	Logger logger = LoggerFactory.getLogger(ExamManagementCommonAction.class);

	@Autowired
	private ExamManagementCommonService examManagementCommonService;
	
	@Autowired
	private ExamManagementExamPlaceService examManagementExamPlaceService;
	
	private final String errorMsg = "操作异常，请联系管理员！";
	
	@RequestMapping(value = "/getStatus", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStatus(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getStatus(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradeList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getGradeList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getScheduleList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScheduleList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getScheduleList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSubjectList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getSubjectList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getExamPlanList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlanList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getExamPlanList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getExamSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamSubjectList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getExamSubjectList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getGroupSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroupSubjectList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getGroupSubjectList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getTClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTClassList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getTClassList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getTClassListByGroupId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTClassListByGroupId(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		String examSubjectGroupIds=request.getString("examSubjectGroupIds");
		if(examSubjectGroupIds.isEmpty()){
			response.put("code", 0);
			response.put("msg","未检索到科目组合");
			return response;
		}
		request.put("examSubjectGroupIds", Arrays.asList(examSubjectGroupIds.split(",")));
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getTClassListByGroupId(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	@RequestMapping(value = "/getPlaceInfoList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPlaceInfoList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		String examSubjectGroupIds=request.getString("examSubjectGroupIds");
		if(!examSubjectGroupIds.isEmpty()){
			request.put("examSubjectGroupIds", Arrays.asList(examSubjectGroupIds.split(",")));
		}
		try{
			request.put("schoolId", getXxdm(req));
			response.put("data", examManagementCommonService.getPlaceInfoList(request));
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	@RequestMapping(value = "/getExamManagementList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamManagementList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		Map<String, Object> param=new HashMap<String, Object>();
		
		String termInfo = request.getString("termInfo");
		
		String examManagementId = request.getString("examManagementId");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("curExamManagementId", examManagementId);
		try{
			List<JSONObject> d=examManagementExamPlaceService.getHasOldExamPlaceList(param);
			for(JSONObject json:d){
				json.put("emTermInfo", json.get("termInfo"));
			}
			request.put("schoolId", getXxdm(req));
			response.put("data", d);
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/getScoreList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			String usedGradesStr = request.getString("usedGrades");
			String xn=request.getString("termInfo").substring(0, 4);
			List<String> usedGrades = StringUtil.convertToListFromStr(usedGradesStr, ",", String.class);
			if(StringUtils.isNotBlank(usedGradesStr) && CollectionUtils.isNotEmpty(usedGrades)) {
				request.put("usedGrades", usedGrades);
				request.put("size", usedGrades.size());
				request.put("xn", xn);
				request.put("termInfo", request.getString("termInfo"));
				response.put("data", examManagementCommonService.getScoreList(request));
			} else {
				response.put("data", new ArrayList<JSONObject>());
			}
		}catch(CommonRunException e){
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		}catch(Exception e){
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
}
