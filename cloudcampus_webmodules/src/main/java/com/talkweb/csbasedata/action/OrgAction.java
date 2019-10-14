package com.talkweb.csbasedata.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_OrgType;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.OrgImportManageService;
import com.talkweb.csbasedata.service.OrgManageService;
/**
 * 机构管理-action
 * @author zhh
 * @version 1.0
 */
@Controller
@RequestMapping(value = "/orgManage/")
public class OrgAction extends BaseAction{
	@Autowired
	private OrgManageService orgManageService;
	@Autowired
	private OrgImportManageService orgImportManageService;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	
	private static final String headString="orgManage_";
	private static final String headStringd="orgManage.";
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
/**
 * 科室
 */
	/** -----查询科室列表----- **/
	@RequestMapping(value = "getDepartmentList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDepartmentList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("isQueryMember", 1); //查询机构成员信息
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		List<JSONObject> data = null;
		try {
			data = orgManageService.getDepartmentList(param);
			if (data!=null ) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	/** -----查询科室详情----- **/
	@RequestMapping(value = "getDepartmentInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getDepartmentInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentId = request.getString("departmentId");
		param.put("uuid", departmentId);
		param.put("schoolId", schoolId);
		param.put("isQueryMember", 1); //查询机构成员信息
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		JSONObject data = null;
		try {
			data = orgManageService.getDepartmentInfo(param);
			if (data!=null) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	/** -----更新（增加）科室详情----- **/
	@RequestMapping(value = "updateDepartment", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateDepartment(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentName = request.getString("departmentName");
		String oldDepartmentLeader = request.getString("oldDepartmentLeader");
		String departmentLeader = request.getString("departmentLeader");
		String departmentId = request.getString("departmentId");
		List<String> orgLeaders = new ArrayList<String>();
		List<String> oldOrgLeaders = new ArrayList<String>();
		if(StringUtils.isNotBlank(departmentLeader)){
			orgLeaders = Arrays.asList(departmentLeader.split(","));
		}
		if(StringUtils.isNotBlank(oldDepartmentLeader)){
			oldOrgLeaders = Arrays.asList(oldDepartmentLeader.split(","));
		}
		param.put("uuid", departmentId);
		param.put("schoolId", schoolId);
		param.put("orgName", departmentName);  
		param.put("orgLeader", orgLeaders); 
		param.put("oldOrgLeader", oldOrgLeaders); 
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		int i =1;
		try {
			i = orgManageService.updateDepartment(param);
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if(i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"名称不能重复");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----更新（增加）科室详情成员----- **/
	@RequestMapping(value = "updateDepartmentMember", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateDepartmentMember(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentId = request.getString("departmentId");
		String departments =request.getString("departmentMember");
		List<String> list = new ArrayList<String>();
		if(StringUtils.isNotBlank(departments)){
			list = Arrays.asList(departments.split(","));
		}
		param.put("uuid", departmentId);
		param.put("schoolId", schoolId);
		param.put("orgMember", list); 
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		try {
			orgManageService.updateDepartmentMember(param);
			setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----删除科室----- **/
	@RequestMapping(value = "deleteDepartment", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteDepartment(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentIds = request.getString("departmentIds");
		List<String> dIdlist = Arrays.asList(departmentIds.split(","));
		param.put("ids", dIdlist);
		param.put("schoolId", schoolId);
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		try {
			orgManageService.deleteDepartment(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	    return response;
	}
	/** -----删除科室成员----- **/
	@RequestMapping(value = "deleteDepartmentMember", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteDepartmentMember(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentId = request.getString("departmentId");
		String accountId = request.getString("accountId");
		String position = request.getString("position");
		param.put("uuid", departmentId);
		param.put("accountId", accountId);
		param.put("position", position);
		param.put("schoolId", schoolId);
		param.put("orgType", T_OrgType.T_Depart.getValue());//科室
		try {
			orgManageService.deleteDepartmentMember(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	    return response;
	}
/**
 * 年级组
 */
	
	/** -----更新（增加）年级组详情----- **/
	@RequestMapping(value = "updateGradegroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateGradegroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String gradegroupName = request.getString("gradegroupName");
		String gradegroupId = request.getString("gradegroupId");
		String departments =request.getString("gradegroupLeader");
		String oldDepartments =request.getString("oldGradegroupLeader");
		String gradeId = request.getString("gradeId");
		List<String> glist = new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		List<String> oldList = new ArrayList<String>();
		if(StringUtils.isNotBlank(gradeId)){
			glist = Arrays.asList(gradeId.split(","));
		}
		if(StringUtils.isNotBlank(departments)){
			list = Arrays.asList(departments.split(","));
		}
		if(StringUtils.isNotBlank(oldDepartments)){
			oldList = Arrays.asList(oldDepartments.split(","));
		}
		
		String xnxq = getCurXnxq(req);
		param.put("uuid", gradegroupId);
		param.put("schoolId", schoolId);
		param.put("orgLeader", list); 
		param.put("orgName", gradegroupName); 
		param.put("oldOrgLeader", oldList); 
		param.put("gradeId", glist); 
		param.put("xnxq", xnxq); 
		param.put("xn", xnxq.substring(0,4)); 
		param.put("orgType", T_OrgType.T_Grade.getValue());//年级组
		JSONObject obj = new JSONObject();
		try {
			obj = orgManageService.updateGradegroup(param);
			int i = obj.getIntValue("returnNum");
			String returnGradeName = obj.getString("returnGradeName");
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if (i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"名称不能重复");
			}else if(i==-2){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),returnGradeName+"不能重复设置");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----删除年级组----- **/
	@RequestMapping(value = "deleteGradegroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteGradegroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String gradegroupIds = request.getString("gradegroupIds");
		List<String> dIdlist = Arrays.asList(gradegroupIds.split(","));
		param.put("ids", dIdlist);
		param.put("schoolId", schoolId);
		param.put("orgType", T_OrgType.T_Grade.getValue());//年级组
		try {
			orgManageService.deleteGradegroup(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	    return response;
	}
	/** -----查询年级组列表----- **/
	@RequestMapping(value = "getGradegroupList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradegroupList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xnxq = getCurXnxq(req);
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("xn", xnxq.substring(0, 4));
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师人数
		param.put("orgType", T_OrgType.T_Grade.getValue());//年级组
		List<JSONObject> data = null;
		try {
			data = orgManageService.getGradegroupList(param);
			if (data!=null ) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	/** -----查询年级组详情----- **/
	@RequestMapping(value = "getGradegroupInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGradegroupInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String xnxq = getCurXnxq(req);
		String gradegroupId = request.getString("gradegroupId");
		param.put("uuid", gradegroupId);
		param.put("schoolId", schoolId);
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师姓名
		param.put("orgType", T_OrgType.T_Grade.getValue());//年级组
		param.put("xnxq", xnxq);
		param.put("xn", xnxq.substring(0, 4));
		JSONObject data = null;
		try {
			data = orgManageService.getGradegroupInfo(param);
			if (data!=null) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
/**
 * 教研组
 */
	/** -----查询教研组详情----- **/
	@RequestMapping(value = "getResearchgroupInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getResearchgroupInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String xnxq = getCurXnxq(req);
		String researchgroupId = request.getString("researchgroupId");
		param.put("uuid", researchgroupId);
		param.put("schoolId", schoolId);
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师姓名
		param.put("orgType", T_OrgType.T_Teach.getValue());//教研组
		param.put("xnxq", xnxq);
		param.put("xn", xnxq.substring(0, 4));
		JSONObject data = null;
		try {
			data = orgManageService.getResearchgroupInfo(param);
			if (data!=null) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	
	/** -----查询教研组列表----- **/
	@RequestMapping(value = "getResearchgroupList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getResearchgroupList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String xnxq = getCurXnxq(req);
		param.put("xn", xnxq.substring(0, 4));
		param.put("schoolId", schoolId);
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师人数
		param.put("orgType", T_OrgType.T_Teach.getValue());//教研组
		List<JSONObject> data = null;
		try {
			data = orgManageService.getResearchgroupList(param);
			if (data!=null ) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	
	/** -----删除教研组----- **/
	@RequestMapping(value = "deleteResearchgroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteResearchgroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String researchgroupIds = request.getString("researchgroupIds");
		List<String> dIdlist = Arrays.asList(researchgroupIds.split(","));
		param.put("ids", dIdlist);
		param.put("schoolId", schoolId);
		param.put("orgType", T_OrgType.T_Teach.getValue());//教研组
		try {
			orgManageService.deleteResearchgroup(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	    return response;
	}
	
	/** -----更新（增加）教研详情----- **/
	@RequestMapping(value = "updateResearchgroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateResearchgroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentName = request.getString("researchgroupName");
		String departmentLeader = request.getString("researchgroupLeader");
		String oldDepartmentLeader = request.getString("oldResearchgroupLeader");
		String lessonId = request.getString("lessonId");
		String departmentId = request.getString("researchgroupId");
		List<String> orgLeaders = new ArrayList<String>();
		List<String> oldOrgLeaders = new ArrayList<String>();
		List<String> lessonIdList = new ArrayList<String>();
		if(StringUtils.isNotBlank(departmentLeader)){
			orgLeaders = Arrays.asList(departmentLeader.split(","));
		}
		if(StringUtils.isNotBlank(oldDepartmentLeader)){
			oldOrgLeaders = Arrays.asList(oldDepartmentLeader.split(","));
		}
		if(StringUtils.isNotBlank(lessonId)){
			lessonIdList = Arrays.asList(lessonId.split(","));
		}
		param.put("uuid", departmentId);
		param.put("schoolId", schoolId);
		param.put("orgName", departmentName);  
		param.put("orgLeader", orgLeaders); 
		param.put("oldOrgLeader", oldOrgLeaders); 
		param.put("lessonId", lessonIdList); 
		param.put("orgType", T_OrgType.T_Teach.getValue());//教研组
		JSONObject obj = new JSONObject();
		try {
			obj = orgManageService.updateResearchgroup(param);
			int i = obj.getIntValue("returnNum");
			String returnLessonName = obj.getString("returnLessonName");
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if (i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"名称不能重复");
			}else if(i==-2){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),returnLessonName+"不能重复设置");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
/**
 * 备课组
 */
	/** -----删除备课组----- **/
	@RequestMapping(value = "deletePreparation", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deletePreparation(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String preparationIds = request.getString("preparationIds");
		List<String> dIdlist = Arrays.asList(preparationIds.split(","));
		param.put("ids", dIdlist);
		param.put("schoolId", schoolId);
		param.put("orgType", T_OrgType.T_PreLesson.getValue());//备课组
		try {
			orgManageService.deletePreparation(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	    return response;
	}
	/** -----更新（增加）备课详情----- **/
	@RequestMapping(value = "updatePreparation", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updatePreparation(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String departmentName = request.getString("preparationName");
		String preparationLeader = request.getString("preparationLeader");
		String oldPreparationLeader = request.getString("oldPreparationLeader");
		String departmentId = request.getString("preparationId");
		String lessonId = request.getString("lessonId");
		String gradeId = request.getString("gradeId");
		List<String> orgLeaders = new ArrayList<String>();
		List<String> oldOrgLeaders = new ArrayList<String>();
		List<String> lessonIdList = new ArrayList<String>();
		List<String> gradeIdList = new ArrayList<String>();
		if(StringUtils.isNotBlank(preparationLeader)){
			orgLeaders = Arrays.asList(preparationLeader.split(","));
		}
		if(StringUtils.isNotBlank(oldPreparationLeader)){
			oldOrgLeaders = Arrays.asList(oldPreparationLeader.split(","));
		}
		if(StringUtils.isNotBlank(lessonId)){
			lessonIdList = Arrays.asList(lessonId.split(","));
		}
		if(StringUtils.isNotBlank(gradeId)){
			gradeIdList = Arrays.asList(gradeId.split(","));
		}
		String xnxq = getCurXnxq(req);
		param.put("xnxq", xnxq); 
		param.put("xn", xnxq.substring(0,4)); 
		param.put("uuid", departmentId);
		param.put("schoolId", schoolId);
		param.put("orgName", departmentName);  
		param.put("orgLeader", orgLeaders); 
		param.put("oldOrgLeader", oldOrgLeaders); 
		param.put("lessonId", lessonIdList); 
		param.put("gradeId", gradeIdList); 
		param.put("orgType", T_OrgType.T_PreLesson.getValue());//备课组
		JSONObject obj = new JSONObject();
		try {
			obj = orgManageService.updatePreparation(param);
			int i = obj.getIntValue("returnNum");
			String returnString = obj.getString("returnString");
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if (i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"名称不能重复");
			}else if(i==-2){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),returnString+"不能重复设置");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----查询备课组列表----- **/
	@RequestMapping(value = "getPreparationList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPreparationList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String xnxq = getCurXnxq(req);
		JSONObject param = new JSONObject();
		param.put("xn", xnxq.substring(0, 4));
		param.put("schoolId", schoolId);
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师人数
		param.put("orgType", T_OrgType.T_PreLesson.getValue());//备课组
		List<JSONObject> data = null;
		try {
			data = orgManageService.getPreparationList(param);
			if (data!=null ) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	/** -----查询备课组详情----- **/
	@RequestMapping(value = "getPreparationInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getPreparationInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String xnxq = getCurXnxq(req);
		String preparationId = request.getString("preparationId");
		param.put("uuid", preparationId);
		param.put("schoolId", schoolId);
		//param.put("isQueryMember", 1); //查询机构成员信息//动态查询年级组的老师姓名
		param.put("orgType", T_OrgType.T_PreLesson.getValue());//备课组
		param.put("xnxq", xnxq);
		param.put("xn", xnxq.substring(0, 4));
		JSONObject data = null;
		try {
			data = orgManageService.getPreparationInfo(param);
			if (data!=null) {
				response.put("data", data);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
/**
 * 科室导入
 */
	 /**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     */
    @RequestMapping(value = "/uploadExcel")
    @ResponseBody
	public JSONObject uploadExcel(@RequestParam("importFile") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {
    	JSONObject object = new JSONObject();
		try {
			JSONObject param = new JSONObject(); 
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			//param.put("file", file);
			// 临时文件保存目录
			File dir = new File(tempFilePath);
			if (!dir.exists()) {
				dir.mkdir();
			}
			// 获取源文件后缀名
			File df = null;
			String fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			//文件名后缀转小写
			String s =  UUID.randomUUID().toString();
			fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +prefix;
			df = new File(tempFilePath + "/" + s + "." + prefix);
			file.transferTo(df);
			
			param.put("s", s);
			param.put("fileName", fileName);
			param.put("prefix", prefix);
			param.put("df", df);
			object = orgImportManageService.uploadExcel(param);
			
			if(object.containsKey("stuTitle")){
				req.getSession().setAttribute(headString+"stuTitle",object.get("stuTitle") );
				object.remove("stuTitle");
			}
			if(object.containsKey("stuTitleName")){
				req.getSession().setAttribute(headString+"stuTitleName",object.get("stuTitleName") );
				object.remove("stuTitleName");
			}
			if(object.containsKey("stuTitleNeed")){
				req.getSession().setAttribute(headString+"stuTitleNeed",object.get("stuTitleNeed") );
				object.remove("stuTitleNeed");
			}
			
			if (object.containsKey("keyId"))
			{
				req.getSession().setAttribute(headString+"keyId", object.get("keyId"));
				object.remove("keyId");
			}
			if(object.containsKey("fileId"))
			{
				req.getSession().setAttribute(headString+"fileId",object.get("fileId"));
				object.remove("fileId");
			}
			if(object.containsKey("tempStuImpExcTitle"))
			{
				String processId = req.getSession().getId();
				Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
				excelTitleMap.put(processId, (String[])object.get("tempStuImpExcTitle"));
				req.getSession().setAttribute(headString+"excelTitleMap",excelTitleMap);
				object.remove("tempStuImpExcTitle");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return object;   	
    }
    
    /**
     * 获取Excel表头和系统字段
     * 
     * @param req
     * @return JSONObject
     * 
     */
    @RequestMapping(value = "/getExcelMatch")
    @ResponseBody
    public JSONObject getExcelMatch(HttpServletRequest req){ 	
    	String processId = req.getSession().getId();
    	Hashtable<String, String[]> excelTitleMap = (Hashtable<String, String[]>) req.getSession().getAttribute(headString+"excelTitleMap");
    	String [] stuTitle=(String[]) req.getSession().getAttribute(headString+"stuTitle");
    	int [] stuTitleNeed=(int[]) req.getSession().getAttribute(headString+"stuTitleNeed");
    	String [] stuTitleName = (String[]) req.getSession().getAttribute(headString+"stuTitleName");
    	String[] tmpTitle = excelTitleMap.get(processId);
    	String fileId = (String) req.getSession().getAttribute(headString+"fileId");
    	JSONObject param = new JSONObject();
    	param.put("processId", processId);
    	param.put("tempTitle", tmpTitle);
    	param.put("stuTitle", stuTitle);
    	param.put("stuTitleNeed", stuTitleNeed);
    	param.put("fileId",fileId);
    	param.put("stuTitleName", stuTitleName);
    	JSONObject json = new JSONObject();
    	try {
    		 json = orgImportManageService.getExcelMatch(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return json;
    }
    
    /**
     * 导入任务启动接口
     * 
     * @param req
     * @param res
     * @return JSONObject
     */
    @RequestMapping(value = "/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody Map<String,Object> mappost, HttpServletResponse res) {      
    	JSONObject object = new JSONObject();
    	String isMatch = mappost.get("isMatch").toString();
        String processId = req.getSession().getId();
        if (!"0".equals(isMatch)){
        	object.put("matchResult",JSON.parseArray(mappost.get("matchResult").toString()));
        } 
        String [] stuTitle=(String[]) req.getSession().getAttribute(headString+"stuTitle");
    	int [] stuTitleNeed=(int[]) req.getSession().getAttribute(headString+"stuTitleNeed");
    	String [] stuTitleName = (String[]) req.getSession().getAttribute(headString+"stuTitleName");
		String keyId = req.getSession().getAttribute(headString+"keyId").toString();
		String fileId = (String) req.getSession().getAttribute(headString+"fileId");
		object.put("fileId",fileId);
		object.put("keyId", keyId);
	 	object.put("schoolId", getXxdm(req));
	 	object.put("isMatch", isMatch);
	 	object.put("processId", processId);
	 	object.put("orgType", T_OrgType.T_Depart.getValue());//科室的导入
		object.put("stuTitle", stuTitle);
		object.put("stuTitleNeed", stuTitleNeed);
		object.put("stuTitleName", stuTitleName);
	 	JSONObject json = new JSONObject();
	 	try {
			json = orgImportManageService.startImportTask(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 	return json;
    }
    
    /**
     * 获取导入进度
     * 
     * @return JSONObject
     */
	@RequestMapping(value = "/importProgress")
    @ResponseBody
    public JSONObject importProgress(HttpServletRequest req, HttpServletResponse res) {
    	JSONObject param = new JSONObject();
    	String processId = req.getSession().getId();
    	param.put("processId", processId);
    	param.put("schoolId", getXxdm(req));
    	param.put("keyId", req.getSession().getAttribute(headString+"keyId").toString());
    	JSONObject json = new JSONObject();
    	try {
			json = orgImportManageService.importProgress(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return json;
    } 
	
	/**
	 * 单条数据修改
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req,
			@RequestBody JSONObject post, HttpServletResponse res){
		JSONObject param = new JSONObject();
		String processId = req.getSession().getId();
		param.put("processId", processId);
		int row = post.getIntValue("row");
		param.put("row", row);
		JSONArray mrows = post.getJSONArray("mrows");
		param.put("mrows", mrows);
		int code = post.getIntValue("code");
		param.put("code", code);
		param.put("schoolId", getXxdm(req));
		param.put("keyId", req.getSession().getAttribute(headString+"keyId").toString());
		JSONObject json = new JSONObject();
		try {
			json = orgImportManageService.singleDataCheck(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	 /**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		String processId = req.getSession().getId();
		param.put("processId", processId);
		param.put("keyId", req.getSession().getAttribute(headString+"keyId").toString());
		JSONObject json = new JSONObject();
		try {
			json = orgImportManageService.continueImport(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json; 
	}
	
}
