package com.talkweb.exammanagement.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.service.ExamManagementExamPlaceService;
import com.talkweb.exammanagement.service.ExamManagementExamStudsService;
import com.talkweb.schedule.service.ScheduleExternalService;
@Controller
@RequestMapping("/examManagement")
public class ExamManagementSetAction extends BaseAction{
	Logger logger = LoggerFactory.getLogger(ExamManagementSetAction.class);
	
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private ExamManagementExamStudsService examManagementExamStudsService;
	@Autowired
	private ExamManagementExamPlaceService examManagementExamPlaceService;
	
	
	@Autowired
	private ScheduleExternalService scheduleExternalService;
	private final String errorMsg = "操作异常，请联系管理员！";
	
	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
	
	@RequestMapping(value = "/examPlace/getExamPlaceList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlaceList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		
		String termInfo = request.getString("termInfo");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			response.put("data", examManagementExamPlaceService.getExamPlaceList(param));
			response.put("ifHasHistory", examManagementExamPlaceService.hasOldExamPlaceList(param))  ;
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
	
	@RequestMapping(value = "/examPlace/deleteExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		
		String examPlaceId=request.getString("examPlaceId");
		
		String termInfo = request.getString("termInfo");
		String schoolId=getXxdm(req);
		
		String isQueryOrDelete=request.getString("isQueryOrDelete");
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examPlaceId", examPlaceId);
		param.put("isQueryOrDelete", isQueryOrDelete);
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamPlaceService.deleteExamPlace(param);
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
	
	@RequestMapping(value = "/examPlace/getExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		
		String examPlaceId=request.getString("examPlaceId");
		
		String termInfo = request.getString("termInfo");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examPlaceId", examPlaceId);
		
		response.put("code", 1);
		response.put("msg", "");
		
		try{
			List<JSONObject> dalist=examManagementExamPlaceService.getExamPlace(param);
			response.put("data",dalist.isEmpty()?"":dalist.get(0));
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
	
	@RequestMapping(value = "/examPlace/saveExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		
		String examPlaceCode=request.getString("examPlaceCode");
		String examPlaceId=request.getString("examPlaceId");
		
		String termInfo = request.getString("termInfo");
		String examPlaceName = request.getString("examPlaceName");
		String numOfExaminee = request.getString("numOfExaminee");
		String numOfTeacher = request.getString("numOfTeacher");
		String buildingName = request.getString("buildingName");
		String floor = request.getString("buildingFloor");
		String roomName = request.getString("roomName");
		
		JSONObject data=new JSONObject();
		
		try{
			Integer.valueOf(numOfExaminee);
			Integer.valueOf(numOfTeacher);
		}catch(Exception e){
			response.put("code", 0);
			response.put("msg", "计划考生数和监考老师数 必须是数字");
			return response;
		}
		
		List<JSONObject> list=new ArrayList<JSONObject>();
		String schoolId=getXxdm(req);
		data.put("schoolId", schoolId);
		data.put("termInfo", termInfo);
		data.put("examManagementId", examManagementId);
		data.put("examPlaceName", examPlaceName);
		data.put("numOfExaminee", numOfExaminee);
		data.put("numOfTeacher", numOfTeacher);
		data.put("buildingName", buildingName);
		data.put("floor", floor);
		data.put("roomName", roomName);
		data.put("examPlaceCode",examPlaceCode);
		data.put("isup", examPlaceId==null?"1":"0");
		if(examPlaceId==null){
			data.put("examPlaceId", UUIDUtil.getUUID());
		}else{
			data.put("examPlaceId", examPlaceId);
		}
		list.add(data);
		param.put("list", list);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		if(examPlaceId==null){
			param.put("examPlaceId", UUIDUtil.getUUID());
		}else{
			param.put("examPlaceId", examPlaceId);
		}
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamPlaceService.saveExamPlace(param);
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
	
	@RequestMapping(value = "/examPlace/copyExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject copyExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String copyExamManagementId=request.getString("copyExamManagementId");
		String copyTermInfo = request.getString("copyTermInfo");
		String termInfo = request.getString("termInfo");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("copyExamManagementId",copyExamManagementId);
		param.put("copyTermInfo", copyTermInfo);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamPlaceService.saveCopyExamPlace(param);
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
	
	
	@RequestMapping(value = "/examStuds/getNonparticipationExamList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getNonparticipationExamList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String termInfo = request.getString("termInfo");
		String examPlanId=request.getString("examPlanId");
		String examSubjectId=request.getString("examSubjectId");
		String tClassId=request.getString("tClassId");
		String studName=request.getString("studName");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("school", getSchool(req, termInfo));
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examPlanIds", Arrays.asList(examPlanId.split(",")));
		param.put("examSubjectId", examSubjectId);
		param.put("tClassId", tClassId);
		param.put("studName", studName);
		
		response.put("code", 1);
		response.put("msg", "");
		
		/*
		 * 这里还需新高考那边获取 参考学生  然后和 不参考学生对比   剔除掉不参考的学生
		 * 	
		 * examSubjectId 反写回  参考学生list里面
		 */
		
		try{
			response.put("data",examManagementExamStudsService.getNonparticipationExamList(param));
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
	
	
	@RequestMapping(value = "/examStuds/moveToNotTakingExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveToNotTakingExam(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		Map<String, Object> ob=new HashMap<String, Object>();
		String examManagementId = request.getString("examManagementId");
		JSONArray data = request.getJSONArray("data");
		String termInfo = request.getString("termInfo");
		
		String schoolId=getXxdm(req);
		List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
		for(Object j:data){
			Map<String, Object> param=new HashMap<String, Object>();
			JSONObject da=(JSONObject) j;
			param.put("schoolId", schoolId);
			param.put("termInfo", termInfo);
			param.put("examManagementId", examManagementId);
			param.put("examSubjectId",da.getString("examSubjectId"));
			param.put("tClassId", da.getString("tClassId"));
			param.put("accountId", da.getString("accountId"));
			param.put("examPlanId", da.getString("examPlanId"));
			list.add(param);
		}
		ob.put("list", list);
		ob.put("schoolId", schoolId);
		ob.put("termInfo", termInfo);
		ob.put("examManagementId", examManagementId);
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamStudsService.saveNonparticipationExamList(ob);
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
	@RequestMapping(value = "/examStuds/moveToTakingExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveToTakingExam(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		String examManagementId = request.getString("examManagementId");
		JSONArray data = request.getJSONArray("data");
		String termInfo = request.getString("termInfo");
		
		String schoolId=getXxdm(req);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			for(Object j:data){
				Map<String, Object> param=new HashMap<String, Object>();
				JSONObject da=(JSONObject) j;
				param.put("schoolId", schoolId);
				param.put("termInfo", termInfo);
				param.put("examManagementId", examManagementId);
				param.put("examSubjectId",da.getString("examSubjectId"));
				param.put("tClassId", da.getString("tClassId"));
				param.put("accountId", da.getString("accountId"));
				param.put("examPlanId", da.getString("examPlanId"));
				examManagementExamStudsService.serializabdeleteNonparticipation(param);
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
	
	@RequestMapping(value = "/examStuds/examPlaceInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject examPlaceInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String termInfo = request.getString("termInfo");
		String name=request.getString("name");
		String examPlanId=request.getString("examPlanId");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlanId", examPlanId);
		param.put("name", name);
		response.put("code", 1);
		response.put("msg", "");
		 
		try{
			JSONObject data=examManagementExamStudsService.getUserExamPlace(param);
			response.put("data", data);
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
	
	/**
	 * 废除
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/examStuds/studsWaiting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject studsWaiting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String termInfo = request.getString("termInfo");
		String name=request.getString("name");
		String examPlanId=request.getString("examPlanId");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlanId", examPlanId);
		param.put("name", name);
		response.put("code", 1);
		response.put("msg", "");
		try{
			List<JSONObject> dlist=examManagementExamStudsService.studsWaiting(param);
			response.put("data", dlist);
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
	
	@RequestMapping(value = "/examStuds/getStudsInExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudsInExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String examPlaceId=request.getString("examPlaceId");
		String termInfo = request.getString("termInfo");
		String examPlanId=request.getString("examPlanId");
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlaceId", examPlaceId);
		param.put("examPlanId", examPlanId);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			JSONObject dlist=examManagementExamStudsService.getStudsInExamPlace(param);
			response.put("data", dlist.get("data"));
			response.put("isKeepContinuous", dlist.get("isKeepContinuous"));
			response.put("counts", dlist.get("counts"));
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
	
	@RequestMapping(value = "/examStuds/moveStudToExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveStudToExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String accountId=request.getString("accountId");
		String examPlaceId=request.getString("examPlaceId");
		String tClassId=request.getString("tClassId");
		String isKeepContinuous=request.getString("isKeepContinuous");
		String termInfo = request.getString("termInfo");
		String examPlanId=request.getString("examPlanId");
		String testNumber=request.getString("testNumber");
		
		
		String schoolId=getXxdm(req);
		List<String> acclist=new ArrayList<String>();
		acclist.add(accountId);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlaceId", examPlaceId);
		param.put("accountId", acclist);
		param.put("tClassId", tClassId);
		param.put("isKeepContinuous", isKeepContinuous);
		param.put("examPlanId", examPlanId);
		param.put("testNumber", testNumber);
		param.put("acc", accountId);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamStudsService.deleteStudToExamPlace(param);
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
	
	
	@RequestMapping(value = "/examStuds/moveStudToWait", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveStudToWait(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String accountId=request.getString("accountId");
		String examPlaceId=request.getString("examPlaceId");
		String tClassId=request.getString("tClassId");
		String isKeepContinuous=request.getString("isKeepContinuous");
		String termInfo = request.getString("termInfo");
		String examPlanId=request.getString("examPlanId");
		String testNumber=request.getString("testNumber");
		
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlaceId", examPlaceId);
		param.put("accountId", accountId);
		param.put("tClassId", tClassId);
		param.put("isKeepContinuous", isKeepContinuous);
		param.put("examPlanId", examPlanId);
		param.put("testNumber", testNumber);
		
		response.put("code", 1);
		response.put("msg", "");
		try{
			examManagementExamStudsService.deleteStudToWait(param);
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
	
	@RequestMapping(value = "/examStuds/exchStudsInExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject exchStudsInExamPlace(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param=new HashMap<String, Object>();
		
		String examManagementId = request.getString("examManagementId");
		String examSubjectGroupId = request.getString("examSubjectGroupId");
		String examPlanId=request.getString("examPlanId");
		String examPlaceId=request.getString("examPlaceId");
		String accountId1=request.getString("accountId1");
		String accountId2=request.getString("accountId2");
		String isKeepContinuous=request.getString("isKeepContinuous");
		String termInfo = request.getString("termInfo");
		
		
		String schoolId=getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examPlaceId", examPlaceId);
		param.put("examPlanId", examPlanId);
		param.put("accountId", accountId1);
		param.put("isKeepContinuous", isKeepContinuous);
		
		response.put("code", 1);
		response.put("msg", "");
		
		List<JSONObject> inser=new ArrayList<JSONObject>();
		try{
			List<JSONObject> data1=examManagementExamStudsService.getStudsInExamPlaceByAccountId(param);
			param.put("accountId", accountId2);
			List<JSONObject> data2=examManagementExamStudsService.getStudsInExamPlaceByAccountId(param);
			
			if(data1!=null&&data1.size()>0&&data2!=null&&data2.size()>0){
				JSONObject da1=data1.get(0);
				JSONObject da2=data2.get(0);
				int num1=da1.getIntValue("seatNumber");
				int num2=da2.getIntValue("seatNumber");
				da1.put("seatNumber", num2);
				da2.put("seatNumber", num1);
				inser.add(da1);
				inser.add(da2);
			}
			param.put("list", inser);
			examManagementExamStudsService.saveArrangeExamResult(param);
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
