package com.talkweb.placementtask.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.ExcelUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.placementtask.service.DezyPlacementTaskService;
import com.talkweb.placementtask.service.PlacementImportService;
import com.talkweb.placementtask.service.PlacementMediumService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.placementtask.vo.AcademicElectiveCross;
import com.talkweb.placementtask.vo.PlacementInfo;
import com.talkweb.placementtask.vo.Result;
import com.talkweb.schedule.service.ScheduleExternalService;

@Controller
@RequestMapping("/placementtask")
public class PlacementTaskAction extends BaseAction {
	
	Logger logger = LoggerFactory.getLogger(PlacementTaskAction.class);

	@Autowired
	private PlacementTaskService placementTaskService;
	
	@Autowired
	private DezyPlacementTaskService dezyPlacementService;
	
	@Autowired
	private PlacementImportService placementImportService;
	
	@Autowired
	private PlacementMediumService placementMediumService;
	
	@Autowired
	private ScheduleExternalService scheduleExternalService;
	
	private final String errorMsg = "操作异常，请联系管理员！";
	
	@RequestMapping(value = "/common/queryGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryGradeList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryGradeList(request));
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
	
	@RequestMapping(value = "/common/queryWishFillingList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryWishFillingList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryWishFillingList(request));
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
	
	@RequestMapping(value = "/common/queryScoreList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryScoreList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryScoreList(request));
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
	
	@RequestMapping(value = "/common/queryPlacementSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryOpenClassInfoList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryPlacementSubjectList(request));
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
	
	@RequestMapping(value = "/common/queryTeachingClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryTeachingClassesList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			String openClassTaskId = request.getString("openClassTaskId");
			if(openClassTaskId.indexOf(",") != -1) {
				request.remove("openClassTaskId");
				request.put("openClassTaskIds", StringUtil.convertToListFromStr(openClassTaskId, ",", String.class));
			}
			response.put("data", placementTaskService.queryTeachingClassesList(request));
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
	
	@RequestMapping(value = "/common/querySubjects", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject querySubjects(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.querySubjects(request));
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
	
	@RequestMapping(value = "/queryPlacementTaskList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryPlacementTaskList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		req.getSession().setAttribute("termInfo", request.get("termInfo"));
		response.put("code", 1);
		response.put("msg", "");
		try{
			response.put("schoolId", Long.valueOf(getXxdm(req)));
			JSONObject json = new JSONObject();
			json.put("termInfo", request.get("termInfo"));
			json.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryPlacementTaskList(json));
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
	
	@RequestMapping(value = "/queryPlacementTaskById", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryPlacementTaskById(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryPlacementTaskById(request));
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
	
	@RequestMapping(value = "/replacePlacementTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject replacePlacementTask(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			request.put("accountId", req.getSession().getAttribute("accountId"));
			placementTaskService.insertOrUpdatePlacementTask(request);
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
	
	@RequestMapping(value = "/deletePlacementTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deletePlacementTask(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "删除成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.deletePlacementTask(request);
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
	
	
	@RequestMapping(value = "/startExecProcess", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject startExecProcess(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
//			String placementId = request.getString("placementId");
			String redisKey = "placementTask.execprocess.progress." + req.getSession().getId();
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.startExecProcess(request, redisKey);
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
	
	@RequestMapping(value = "/queryExecProgress", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryExecProgress(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			String redisKey = "placementTask.execprocess.progress." + req.getSession().getId();
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryExecProgress(request, redisKey));
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
	
	/********************************************* 微走班 ***************************************************/
	
	@RequestMapping(value = "/micro/queryOpenClassInfoByWfId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryOpenClassInfoByWfIdMicro(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryOpenClassInfoByWfIdMicro(request));
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
	
	@RequestMapping(value = "/micro/saveOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveOpenClassInfoMicro(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveOpenClassInfoMicro(request);
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
	
	/**************************************  中走班    ******************************************************/
	
	@RequestMapping(value = "/medium/queryZhData", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryZhDataMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryZhDataMedium(request));
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
	
	@RequestMapping(value = "/medium/saveZhData", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveZhDataMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveZhDataMedium(request);
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
	
	@RequestMapping(value = "/medium/queryZhOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryZhOpenClassInfoMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryZhOpenClassInfoMedium(request));
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
	
	@RequestMapping(value = "/medium/saveZhOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveZhOpenClassInfoMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveZhOpenClassInfoMedium(request);
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
	
	@RequestMapping(value = "/medium/queryRemainOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryRemainOpenClassInfoMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryRemainOpenClassInfoMedium(request));
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
	
	@RequestMapping(value = "/medium/saveRemainOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveRemainOpenClassInfoMedium(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveRemainOpenClassInfoMedium(request);
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
	
	@RequestMapping(value = "/large/queryLayerSetterInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryLayerSetterInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryLayerSetterInfoLarge(request));
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
	
	@RequestMapping(value = "/large/saveLayerSetterInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveLayerSetterInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveLayerSetterInfoLarge(request);
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
	
	@RequestMapping(value = "/large/queryLayerOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryLayerOpenClassInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryLayerOpenClassInfoLarge(request));
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
	
	@RequestMapping(value = "/large/saveLayerOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveLayerOpenClassInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveLayerOpenClassInfoLarge(request);
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
	
	@RequestMapping(value = "/large/deleteLayerOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteLayerOpenClassInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "删除成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.deleteLayerOpenClassInfoLarge(request);
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
	
	//*************************************** 大走班按志愿分层 *********************************************//
	@RequestMapping(value = "/large/queryWishSetter", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryWishSetterLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryWishSetterLarge(request));
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
	
	@RequestMapping(value = "/large/saveWishSetter", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveWishSetterLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveWishSetterLarge(request);
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
	
	@RequestMapping(value = "/large/queryWishOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryWishOpenClassInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryWishOpenClassInfoLarge(request));
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
	
	@RequestMapping(value = "/large/saveWishOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveWishOpenClassInfoLarge(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.saveWishOpenClassInfoLarge(request);
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
	
	@RequestMapping(value = "/large/deleteWishOpenClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteWishOpenClassInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "删除成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.deleteWishOpenClassInfo(request);
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
	
	//************************************ 分班结果 ******************************************//
	
	@RequestMapping(value = "/queryResultPreview", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryResultPreview(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			String openClassTaskId = (String) request.remove("openClassTaskId");
			List<String> openClassTaskIds = StringUtil.convertToListFromStr(openClassTaskId, ",", String.class);
			if(CollectionUtils.isNotEmpty(openClassTaskIds)) {
				request.put("openClassTaskIds", openClassTaskIds);
			}
			response.put("data", placementTaskService.queryResultPreview(request));
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
	
	@RequestMapping(value = "/updateTeachingClassName", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeachingClassName(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			placementTaskService.updateTeachingClassName(request);
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
	
	@RequestMapping(value = "/queryResultDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryResultDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			String teachingClassId = request.getString("teachingClassId");
			if(teachingClassId.indexOf(",") != -1) {
				request.remove("teachingClassId");
				request.put("teachingClassIds", StringUtil.convertToListFromStr(teachingClassId, ",", String.class));
			}
			response.put("data", placementTaskService.queryResultDetail(request));
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
	
	@RequestMapping(value = "/fineTune/queryStudenInfoWaitForPlacement", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryStudenInfoWaitForPlacement(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryStudenInfoWaitForPlacement(request));
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
	
	@RequestMapping(value = "/fineTune/queryStudenInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryStudenInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			response.put("data", placementTaskService.queryStudenInfo(request));
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
	
	@RequestMapping(value = "/fineTune/moveStudenInfoToWaitForPlacement", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveStudenInfoToWaitForPlacement(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			String accountIdStr = request.getString("accountIds");
			List<Long> accountIds = StringUtil.convertToListFromStr(accountIdStr, ",", Long.class);
			if(accountIds.size() > 0) {
				request.put("accountIds", accountIds);
				placementTaskService.modifyStudenInfoToWaitForPlacement(request);
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
	
	@RequestMapping(value = "/fineTune/moveStudenInfoToPlacement", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveStudenInfoToPlacement(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		response.put("code", 1);
		response.put("msg", "保存成功！");
		try{
			request.put("schoolId", Long.valueOf(getXxdm(req)));
			String accountIdStr = request.getString("accountIds");
			List<Long> accountIds = StringUtil.convertToListFromStr(accountIdStr, ",", Long.class);
			if(accountIds.size() > 0) {
				request.put("accountIds", accountIds);
				placementTaskService.modifyStudenInfoToPlacement(request);
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
	
	/********************************************* 定二走一 ***************************************************/
	@RequestMapping(value = "/dezy/getWfList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getWfList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		if(null==request.get("termInfo")){
			request.put("termInfo", req.getSession().getAttribute("termInfo"));
			//request.put("termInfo", getCurXnxq(req));
		}
		List<JSONObject> data = dezyPlacementService.getWfListToThird(request);
		response.put("data", data);
		
		setPropMsg(response, 0, "返回成功！");
		
		return response;
	}
	
	@RequestMapping(value = "/dezy/getWishingDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getWishingDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		JSONObject data = dezyPlacementService.getWishingDetail(request);
		setPropMsg(data, 0, "返回成功！");
		
		return data;
	}
	@RequestMapping(value = "/medium/getWishingDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumWishingDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		JSONObject data = dezyPlacementService.getWishingDetail(request);
		setPropMsg(data, 0, "返回成功！");
		
		return data;
	}
	
	@RequestMapping(value = "/dezy/setPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		int result = 0;
		String msg = "插入设置成功！";
		
		try {
			String plcId = request.getString("placementId");
			String termInfo = request.getString("termInfo");
			//placementTaskService.updateAllDezyResult(schoolId,plcId,termInfo);
			result = dezyPlacementService.insertDezyPreSettings(request);
			if(result<0){
				msg = "插入设置失败！";
			}else if(result==2){
				msg = "已排课,分班不允许修改！";
			}else if(result==3){
				msg = "分班正在执行中，请5分钟后再试！";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = -1;
			msg = "插入设置失败！";
			e.printStackTrace();
		}

		
		setPropMsg(response, result, msg);
		
		return response;
	}
	
	
	@RequestMapping(value = "/dezy/getPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		response = dezyPlacementService.getDezyPreSettings(request);		
		
		return response;
	}
	
	@RequestMapping(value = "/dezy/getProcessing", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getProcessing(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		Float currentProgress = request.getFloat("currentProgress");
		JSONObject resultCode = dezyPlacementService.getGenDezyClassProc(request.getString("placementId"),currentProgress);
		//setPropMsg(response, resultCode, "返回成功！");
		if(null == resultCode || resultCode.size() == 0){
			resultCode = new JSONObject();
			resultCode.put("progress", 100);
			resultCode.put("msg", "获取分班进度失败！");
			resultCode.put("code", -1);
		}
		return resultCode;
	}
	
	@RequestMapping(value = "/dezy/getQuerySubList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getQuerySubList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject result = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		JSONArray response  = dezyPlacementService.getQuerySubList(request);
		result.put("data", response);
		setPropMsg(result, 0, "返回成功！");
		
		return result;
	}
	
	@RequestMapping(value = "/dezy/getTclassPreview", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTclassPreview(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		List<JSONObject> data = dezyPlacementService.getTclassPreview(request);
		
		response.put("data", data);
		setPropMsg(response, 0, "返回成功！");
		
		return response;
	}
	
	@RequestMapping(value = "/dezy/renameClassName", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject modifyClassName(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		int data = dezyPlacementService.modifyClassName(request);
		
		setPropMsg(response, data, "返回成功！");
		
		return response;
	}
	
	
	@RequestMapping(value = "/dezy/recoveryClassName", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject recoveryClassName(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		int data = dezyPlacementService.recoveryClassName(request);
		
		setPropMsg(response, data, "返回成功！");
		
		return response;
	}
	
	@RequestMapping(value = "/dezy/getClassInfoDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassInfoDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		List<JSONObject> data = dezyPlacementService.getClassAll(request);
		response.put("data", data);
		setPropMsg(response, 0, "返回成功！");
		
		return response;
	}
	
	@RequestMapping(value = "/dezy/getStuClassInfoDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStuClassInfoDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		int curPage = request.getIntValue("curPage");
		int curPageCount = request.getIntValue("curPageCount");
		int start = (curPage - 1)*curPageCount;
		int end = start + curPageCount;
		
		List<JSONObject> data = dezyPlacementService.getStuInClassDetail(request);
		int totalCounts = data.size();
		end = Math.min(end, totalCounts);
		long totalPage = Math.round(0.5+totalCounts/curPageCount);
		if(curPage!=0){
			data = data.subList(start, end);
		}
		response.put("curPage", curPage);
		response.put("totalCounts", totalCounts);
		response.put("totalPage", totalPage);
		response.put("data", data);
		setPropMsg(response, 0, "返回成功！");
		
		return response;
	}
	
	
	@RequestMapping(value = "/dezy/exportDezyDivResult", method = RequestMethod.POST)
	@ResponseBody
	public void exportDezyDivResult(HttpServletRequest req,HttpServletResponse res) throws Exception{
		logger.info("exportExcelComplexData 进入复杂导出功能！");
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        String fileName  = req.getParameter("fileName");
        List<JSONArray> jsonHeadList = new ArrayList<JSONArray>();
        for(Object jsonArr : excelHeads){
        	jsonHeadList.add((JSONArray)jsonArr);
        }
        List<JSONObject> jsonDataList = new ArrayList<JSONObject>();
        for(Object jsonArr : excelData){
        	jsonDataList.add((JSONObject)jsonArr);
        }
        exportComplexHeadExcelWithData(jsonDataList , jsonHeadList,fileName, req, res);
        logger.info("exportExcelComplexData 导出结束！");
	}
	private void setPropMsg(JSONObject res, int code, String msg){
		res.put("code", code);
		res.put("msg", msg);
	}
	private void exportComplexHeadExcelWithData(List<JSONObject> jsonDataList, List<JSONArray> jsonHeadList,String fileName,HttpServletRequest req,HttpServletResponse res){
		// 1.定义变量，表格单元属性对象，以及行数，列数
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBordertitle = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setWrapText(true);// 设置自动换行
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBordertitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBordertitle.setFont(font);
		
		Sheet sheet = workbook.createSheet("new sheet1");
		
		//写入表头
		//表头与列的映射
		Map<String,Integer> fields = new HashMap<String,Integer>();
		//Auqa背景色
		XSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.AUTOMATIC.getIndex());
		//style.setFillPattern(CellStyle.BORDER_HAIR);
		for(int rowNum=0; rowNum<jsonHeadList.size(); rowNum++){//表头行数
			JSONArray head = jsonHeadList.get(rowNum);
			int colMergeStart = 0;
			for(int n=0; n<head.size(); n++){//表头列
				JSONObject h = head.getJSONObject(n);
				int rowspan = h.getIntValue("rowspan");
				int colspan = h.getIntValue("colspan");
				String title = h.getString("title");
				String field = h.getString("field");
								
				
				//合并行
				for(int r=0; r<rowspan; r++){
					Row row = sheet.getRow(r)==null?sheet.createRow(r):sheet.getRow(r);
					Cell cell = row.createCell(n);	
					cell.setCellStyle(style);
					cell.setCellValue(title);
					if(StringUtils.isNotEmpty(field) && !fields.containsKey(field)){
						fields.put(field, n);
					}
				}
				//合并列	
				//colMergeStart = n;
				for(int col=0; col<colspan; col++){
					Row row = sheet.getRow(rowNum)==null?sheet.createRow(rowNum):sheet.getRow(rowNum);
					Cell cell = row.createCell(((colMergeStart==0)?n:colMergeStart)+col);	
					cell.setCellValue(title);
					cell.setCellStyle(style);
					if(StringUtils.isNotEmpty(field) && !fields.containsKey(field)){
						fields.put(field, n);
					}
				}
				//新增的表头列
				if(field!=null && !fields.containsKey(field)){
					Row row = sheet.getRow(rowNum)==null?sheet.createRow(rowNum):sheet.getRow(rowNum);
					Cell cell = row.createCell(fields.size());	
					cell.setCellValue(title);
					cell.setCellStyle(style);
					fields.put(field, fields.size());
				}
				if(rowspan>0){
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum+rowspan-1, n, n));
				}
				if(colspan>0){
					int start = (colMergeStart==0)?n:colMergeStart;
					sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum,start , start+colspan-1));
					colMergeStart = start+colspan;
				}
			}
		}
		
		//写入数据到excel
		int startRow = jsonHeadList.size();
		Map<String,String> preDataMap = new HashMap<String,String>();
		Map<String,Integer> startRowMap = new HashMap<String,Integer>();
		int end = jsonDataList.size()+startRow;
		for(int rowNum=startRow; rowNum<end; rowNum++){
			Row row = sheet.getRow(rowNum)==null?sheet.createRow(rowNum):sheet.getRow(rowNum);
			JSONObject singleData = jsonDataList.get(rowNum-startRow);
			for(String field : fields.keySet()){
				int col = fields.get(field);
				String fieldName = singleData.getString(field);
				Cell cell = row.createCell(col);
				cell.setCellValue(fieldName);								
				
				//合并
				if(preDataMap.containsKey(field) && !fieldName.equals(preDataMap.get(field))){					
					sheet.addMergedRegion(new CellRangeAddress(startRowMap.get(field), rowNum-1, col, col));
					startRowMap.put(field, rowNum);
				}

				switch(field){
				case "classGroupName":
				case "className":
				case "classCount":
				case "tclassName_7":
				case "groundName_7":
				case "tclassCount_7":
				case "tclassName_8":
				case "groundName_8":
				case "tclassCount_8":
				case "tclassName_9":
				case "groundName_9":
				case "tclassCount_9":
					preDataMap.put(field,fieldName);
					//合并行from
					if(!startRowMap.containsKey(field)){
						startRowMap.put(field, rowNum);
					}
					break;
				}
			}
		}
		
		
		
		//String xls = UUIDUtil.getUUID();
		File temp = new File(fileName + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 导出数据
		try {
			if (!temp.exists()) {
				temp.createNewFile();
			}
			out = new FileOutputStream(temp);
			out.flush();
			workbook.write(out);
			out.close();
			// long fileLength = temp.length();
			res.setContentType("octets/stream");
			res.addHeader("Content-Type", "text/html; charset=utf-8");
			fileName += ".xlsx";
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
			res.addHeader("Content-Disposition", "attachment;filename="
					+ downLoadName);
			// res.setHeader("Content-Length", String.valueOf(fileLength));
			bis = new BufferedInputStream(new FileInputStream(temp));
			bos = new BufferedOutputStream(res.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportExcelWithData:error{}", e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			temp.delete();
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//>>>>>>>>>>>>>>>>>>>>>   大走班        <<<<<<<<<<<<<<<<<<<<<<<<<<
	@RequestMapping(value = "/xgk/dzb/getWfList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbWfList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return getWfList(req, request, res);
	}
	
	@RequestMapping(value = "/xgk/dzb/getWishingDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbWishingDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return getWishingDetail(req, request, res);
	}
	
	@RequestMapping(value = "/xgk/dzb/setPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setDzbPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);
		String msg = "返回成功!";
		request.put("schoolId", schoolId);
		JSONObject result = new JSONObject();
		
		int re = dezyPlacementService.insertDzbPreSettings(request);
		if (re<0){
			msg = "数据库操作失败!";
		}
		
		if(re == 2){
			msg = "已排课分班,不允许修改！";
		}
		
		setPropMsg(result, re, msg);
		
		return result;
	}
	/*
	@RequestMapping(value = "/medium/setPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setMediumPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) throws Exception {
		String schoolId = getXxdm(req);
		String msg = "返回成功!";
		request.put("schoolId", schoolId);
		JSONObject result = new JSONObject();
		String redisKey = "placementTask.execprocess.progress." + req.getSession().getId();
		request.put("schoolId", Long.valueOf(getXxdm(req)));
		placementTaskService.startExecProcess(request, redisKey);
		int re = dezyPlacementService.insertDzbPreSettings(request);
		if (re<0){
			msg = "数据库操作失败!";
		}
		if(re == 2){
			msg = "已排课分班,不允许修改！";
		}
		
		setPropMsg(result, re, msg);
		
		return result;
	}
	*/
	//xgk/dzb/getProcessing
	@RequestMapping(value = "/xgk/dzb/getProcessing", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbgProcessing(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return getProcessing(req, request, res);
	}
	@RequestMapping(value = "/medium/getProcessing", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumgProcessing(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		return getProcessing(req, request, res);
	}
	
	@RequestMapping(value = "/xgk/dzb/getPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		JSONObject result = dezyPlacementService.getDzbPreSettings(request);
		
		return result;
	}
	/*
	@RequestMapping(value = "/medium/getPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		request.put("type", "medium");
		
		JSONObject result = dezyPlacementService.getDzbPreSettings(request);
		
		return result;
	}
	*/
	
	//
	@RequestMapping(value = "/xgk/dzb/getQuerySubList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbQuerySubList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		JSONObject result = new JSONObject();
		
		List<JSONObject> data = dezyPlacementService.getDzbDivQueryParams(request);
		if(CollectionUtils.isNotEmpty(data)){
			result.put("data", data);
			result.put("msg", "返回查询参数成功！");
			result.put("code", 0);
			return result;
		}
		result.put("msg", "返回查询参数失败！");
		result.put("code", -1);
		return result;
	}
	/*
	//中走班查询
	@RequestMapping(value = "/medium/getQuerySubList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumQuerySubList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		JSONObject result = new JSONObject();
		
		List<JSONObject> data = dezyPlacementService.getDzbDivQueryParams(request);
		if(CollectionUtils.isNotEmpty(data)){
			result.put("data", data);
			result.put("msg", "返回查询参数成功！");
			result.put("code", 0);
			return result;
		}
		result.put("msg", "返回查询参数失败！");
		result.put("code", -1);
		return result;
	}
	*/
	@RequestMapping(value = "/xgk/dzb/getTclassPreview", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbTclassPreview(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		String msg = "查询分班预览成功！";
		int code = 0;
		List<JSONObject> data = dezyPlacementService.getDzbDivResult(request);
				
		JSONObject result = new JSONObject();
		setPropMsg(result, code, msg);
		result.put("data", data);
		return result ;
	}
	/**
	 * 获取中走班分班预览
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/medium/getTclassPreview", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumTclassPreview(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		String msg = "查询分班预览成功！";
		int code = 0;
		int type = request.getIntValue("type");
		List<JSONObject> data = null;
		if(type==2){
			
			data = dezyPlacementService.getDzbMainTable(request);
		}else{
			JSONObject queryResultPreview = placementTaskService.queryResultPreview(request);
			data = (List<JSONObject>) queryResultPreview.get("preview");
		}
		
		JSONObject result = new JSONObject();
		setPropMsg(result, code, msg);
		result.put("data", data);
		return result ;
	}
	
	
	@RequestMapping(value = "/xgk/dzb/getTclassMainTable", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTclassMainTable(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		String msg = "查询分班预览成功！";
		int code = 0;
		List<JSONObject> data = dezyPlacementService.getDzbMainTable(request);
				
		JSONObject result = new JSONObject();
		setPropMsg(result, code, msg);
		result.put("data", data);
		return result ;
	}
	@RequestMapping(value = "/xgk/dzb/getStuClassInfoDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDzbStuClassInfoDetail(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		String schoolId = getXxdm(req);		
		request.put("schoolId", schoolId);
		String msg = "查询学生分班明细成功！";
		int code = 0;
		List<JSONObject> data = dezyPlacementService.getDzbStuClassInfoDetail(request);
				
		JSONObject result = new JSONObject();
		setPropMsg(result, code, msg);
		result.put("data", data);
		return result ;
	}
	
	@RequestMapping(value = "/xgk/dzb/getStuQueryParams", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStuQueryParams(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject result = new JSONObject();
		
		request.put("schoolId", getXxdm(req));
		result.put("data", dezyPlacementService.getDzbStuQueryParams(request));
		
		result.put("msg", "返回成功！");
		result.put("code", 0);
		return result;
	}
	
	@RequestMapping(value = "/xgk/dzb/getTeachingResourses", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeachingResourses(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject result = new JSONObject();
		
		request.put("schoolId", getXxdm(req));
		List<JSONObject> data = dezyPlacementService.getDzbTeachingResource(request);
		
		result.put("data", data);
		
		result.put("code", 0);
		result.put("msg", "返回成功！");
		return result;
	}
	
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/xgk/dzb/exportTeachingResourses")
    @ResponseBody  
    public void demoForDownExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{
        
        /**excelHead 前台传过来的datagrid表头
         * 示例3 用于合并表头
         */

        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));

        for(Object object : excelHeads.getJSONArray(1)){
        	JSONObject row = (JSONObject) object;
        	
        	String field = row.getString("field");
        	String title = row.getString("title");
        	
        	if("maxClassOptions".equals(field) || "subClassOptions".equals(field)){
        		StringBuffer sb = new StringBuffer(field);
        		sb.append("_");
        		switch(title){
        		case "学考":
        			sb.append("pro");
        			break;
        		case "选考":
        			sb.append("opt");
        			break;
        		case "总数":
        			sb.append("tot");
        			break;
        		}
        		row.put("field", sb.toString());
        	}
        }
        
        for(Object object : excelData){
        	JSONObject row = (JSONObject) object;
        	
        	//补全格式（科目班级数）
        	Integer optClassNum = null,proClassNum = null;
        	JSONObject maxClassOptions = (JSONObject)row.remove("maxClassOptions");        	
        	if(null!=maxClassOptions){
        		optClassNum =  maxClassOptions.getInteger("optClassNum");
        		proClassNum =  maxClassOptions.getInteger("optClassNum");
        		row.put("maxClassOptions_opt", optClassNum);
        		row.put("maxClassOptions_pro", proClassNum);
        		row.put("maxClassOptions_tot", Math.max(proClassNum, optClassNum));
        	}
        	//补全格式（同时最大班级数）
        	JSONObject subClassOptions = (JSONObject)row.remove("subClassOptions");     
        	if(null!=subClassOptions){
	        	optClassNum =  subClassOptions.getInteger("optClassNum");
	        	proClassNum =  subClassOptions.getInteger("optClassNum");
	        	row.put("subClassOptions_opt", optClassNum);
	        	row.put("subClassOptions_pro", proClassNum);
	        	row.put("subClassOptions_tot", Math.max(proClassNum, optClassNum)); 
        	}
        }
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/xgk/dzb/exportStuClassInfoDetail")
    @ResponseBody  
    public void exportStuClassInfoDetail(HttpServletRequest req,HttpServletResponse res) throws Exception{
    	 JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
         JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));

         String fileName  = req.getParameter("fileName");
         ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/xgk/dzb/exportDivResult")
    @ResponseBody  
    public void exportDivResult(HttpServletRequest req,HttpServletResponse res) throws Exception{
    	 JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
         JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));

         String fileName  = req.getParameter("fileName");
         ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/xgk/dzb/exportTclassMainTable")
    @ResponseBody  
    public void exportTclassMainTable(HttpServletRequest req,HttpServletResponse res) throws Exception{
    	 JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
         JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));

         String fileName  = req.getParameter("fileName");
         ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    
    
	/**
	 * 导入手工分班信息
	 * @return
	 */
	@RequestMapping(value = "/importResult", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> importResult(@RequestParam(value = "placementId", required = true) String placementId,
			@RequestParam(value = "termInfo", required = true) String termInfo, HttpServletRequest request) {
		
		Result<String> result = new Result<>();
		long startTime = System.currentTimeMillis();
		
        try {
        	
        	MultipartFile file = null;
        	if(request instanceof MultipartHttpServletRequest) {
        		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        		Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
        		if(fileMap.size()>0) file = fileMap.values().iterator().next();		
        	}
        	
        	Assert.notNull(file, "导入附件不能为空");
        	
        	String fileType = null;
        	if(file.getOriginalFilename().indexOf(".xlsx")!= -1) {
        		fileType ="xlsx";
        	}else if(file.getOriginalFilename().indexOf(".xls") != -1) {
        		fileType = "xls";
        	}else {
        		throw new IllegalArgumentException("不支持的导入格式");
        	}
        	
        	//File tempDir = WebUtils.getTempDir(context);
        	ExcelUtil<PlacementInfo> placementExcelUtil = new ExcelUtil<>(PlacementInfo.class);
        	List<PlacementInfo> placementRowsList = placementExcelUtil.importExcel("学生分班", file.getInputStream(), fileType);
        	Assert.notEmpty(placementRowsList, "学生分班导入数据不能为空");
        	Assert.isTrue(null!=placementRowsList && placementRowsList.size()>1, "学生分班导入数据不能为空");
        	
        	ExcelUtil<AcademicElectiveCross> crossExcelUtil = new ExcelUtil<>(AcademicElectiveCross.class);
        	List<AcademicElectiveCross> crossRowsList = crossExcelUtil.importExcel("学选交叉", file.getInputStream(), fileType);
        	
        	placementRowsList = placementRowsList.subList(1, placementRowsList.size());
        	String schoolId = getXxdm(request);
        	placementImportService.importPlacementInfo(placementId, termInfo, schoolId, placementRowsList, crossRowsList);
        	
        	result.setCode(HttpStatus.OK.value());
        	result.setMessage(HttpStatus.OK.name());
            return new ResponseEntity<Result<String>>(result, HttpStatus.OK);
            
        }catch(IllegalArgumentException e) {
        	logger.error(e.getMessage(), e);
        	result.setCode(HttpStatus.BAD_REQUEST.value());
        	result.setMessage(e.getMessage());
        	return new ResponseEntity<Result<String>>(result, HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
        	logger.error(e.getMessage(), e);
            result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setMessage("导入失败"+e.getMessage());
            return new ResponseEntity<Result<String>>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }finally{
        	logger.debug("importResult totalTime:{}", (System.currentTimeMillis() - startTime));
        }
	}
	
	/**
	 * 根据分班id，将未分班的学生找最优解并保存分班数据
	 * */
	@RequestMapping(value = "/xgk/dzb/insertNoDividedStuClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertNoDividedStuClassInfo(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			List<JSONObject> allStuClassInfo = new ArrayList<JSONObject>();
			String schoolId = getXxdm(req);
			String termInfo = request.getString("termInfo");
			String placementId = request.getString("placementId");
			JSONObject json = placementTaskService.insertNoDividedStuClassInfo(schoolId, termInfo, placementId);
			
			if(json!=null){
				allStuClassInfo = (List<JSONObject>) json.get("allStuClassInfo");
				Integer placementType = (Integer) json.get("placementType");
				List<String> tClassIds = new ArrayList<String>();
				Map<String,String> tClassIdMap = new HashMap<String,String>();
				for(JSONObject StuClassInfo:allStuClassInfo){
					String tClassId = StuClassInfo.get("classId").toString();
					if(!tClassIdMap.containsKey(tClassId)){
						tClassIdMap.put(tClassId, tClassId);
					}
				}

				for(Map.Entry<String,String> entry:tClassIdMap.entrySet()){
					String tClassId = entry.getKey();
					tClassIds.add(tClassId);
				}
				
				scheduleExternalService.updateTclass(schoolId, placementId, placementType, termInfo, tClassIds);
			}
			
			response.put("stuClassInfo", allStuClassInfo);
			response.put("code", 1);
			response.put("msg", "");
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
	 * 中走班中成绩的下拉接口
	 * 根据学校分班id和学年学期以及学校id获取成绩下拉列表以及当前选中的成绩
	 * */
	@RequestMapping(value = "/common/newqueryScoreList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject newqueryScoreList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			JSONObject data = new JSONObject();
			String schoolId = getXxdm(req);
			String termInfo = request.getString("termInfo");
			String placementId = request.getString("placementId");
			data = placementMediumService.newqueryScoreList(schoolId,termInfo,placementId);
			response.put("data", data);
			response.put("code", 1);
			response.put("msg", "");
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
	 * 中走班根据选科下拉获取组合信息接口
	 * */
	@RequestMapping(value = "/medium/queryZhInfoByWfId", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryZhInfoByWfId(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			List<JSONObject> zhSetList = new ArrayList<JSONObject>();
			String schoolId = getXxdm(req);
			String termInfo = request.getString("termInfo");
			String placementId = request.getString("placementId");
			String wfId = request.getString("wfId");
			zhSetList = placementMediumService.QueryZhInfoByWfId(schoolId,termInfo,placementId,wfId);
			response.put("zhSetList", zhSetList);
			response.put("code", 1);
			response.put("msg", "");
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
	
	
	@RequestMapping(value = "/medium/getPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMediumPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			JSONObject data = new JSONObject();
			String schoolId = getXxdm(req);
			String termInfo = request.getString("termInfo");
			String placementId = request.getString("placementId");
			String wfId = request.getString("wfId");
			data = placementMediumService.GetMediumPreSetting(schoolId,termInfo,placementId,wfId);
			response.put("data", data);
			response.put("code", 1);
			response.put("msg", "");
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
	
	@RequestMapping(value = "/medium/setPreSetting", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setMediumPreSetting(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			String schoolId = getXxdm(req);
			String placementId = request.getString("placementId");
			String termInfo = request.getString("termInfo");
			String examTermInfo = request.getString("examTermInfo");
			String wfId = request.getString("wfId");
			String examId = request.getString("examId");
			Integer ruleCode = request.getInteger("ruleCode");
			Integer maxClassNum = request.getInteger("maxClassNum");
			Integer gradeSumLesson = request.getInteger("gradeSumLesson");
			Integer fixedSumLesson = request.getInteger("fixedSumLesson");
			List<JSONObject> zhSetList = request.getObject("zhSetList", List.class);
			List<JSONObject> classlevelList = request.getObject("classlevelList", List.class);
			List<JSONObject> subjectSetList = request.getObject("subjectSetList", List.class);
			placementMediumService.SetMediumPreSetting(schoolId,termInfo,placementId,examTermInfo,wfId,examId,
					ruleCode,maxClassNum,gradeSumLesson,fixedSumLesson,zhSetList,classlevelList,subjectSetList);
			response.put("code", 1);
			response.put("msg", "");
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
	
	@RequestMapping(value = "/medium/getQuerySubList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject GetMediumQuerySubList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res){
		JSONObject response = new JSONObject();
		try{
			JSONObject data = new JSONObject();
			String schoolId = getXxdm(req);
			String termInfo = request.getString("termInfo");
			String placementId = request.getString("placementId");
			String usedGrade = request.getString("usedGrade");
			data = placementMediumService.GetQuerySubList(schoolId,termInfo,placementId,usedGrade);
			response.put("data", data);
			response.put("code", 1);
			response.put("msg", "");
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
