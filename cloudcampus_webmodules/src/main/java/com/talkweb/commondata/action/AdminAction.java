package com.talkweb.commondata.action;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AdminManageService;
import com.talkweb.commondata.util.OutputMessage;

/**
 * 管理员管理-action
 * @author zhh
 * @version 1.0
 * @version 新增云平台环境及本地环境判断：云平台从基础数据取管理员权限，老师也有管理员权限 而schoolmanager表无值 2018.11
 * 
 */
@Controller
@RequestMapping(value = "/adminManage/")
public class AdminAction extends BaseAction {
	@Autowired
	private AdminManageService adminManageService;
	ResourceBundle rb = ResourceBundle.getBundle("constant.constant" );
	String from = rb.getString("from");
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
/**
 * 管理员
 */
	/** -----查询管理员列表----- **/
	@RequestMapping(value = "getAdministratorList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAdministratorList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		String condition = request.getString("condition");
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		param.put("from", from);
		param.put("schoolId", schoolId);
		if("1".equals(from)){
			param.put("role", T_Role.Teacher.getValue());//老师
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		param.put("condition", condition);
		param.put("termInfoId", termInfoId);
		List<JSONObject> data = null;
		try {
			data = adminManageService.getAdministratorList(param);
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
	/** -----获取管理员详情----- **/
	@RequestMapping(value = "getAdministrator", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAdministrator(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String administratorId = request.getString("administratorId");
		String administratorName = request.getString("administratorName");
		param.put("accountId", administratorId);
		param.put("accountName", administratorName);
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		param.put("from", from);
		if("1".equals(from)){
			param.put("role", T_Role.Teacher.getValue());//老师
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		List<JSONObject> data = null;
		try {
			data = adminManageService.getAdministrator(param);
			if (data!=null) {
				response.put("menus", data);
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
	/** -----更新管理员----- **/
	@RequestMapping(value = "updateAdministrator", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateAdministrator(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String administratorName = request.getString("administratorName");
		String navIds = request.getString("navIds");
		String administratorId = request.getString("administratorId");
		String oldAdministratorId = request.getString("oldAdministratorId");
		List<String> navIdList = Arrays.asList(navIds.split(","));
		param.put("oldAccountId", oldAdministratorId);
		param.put("accountId", administratorId);
		param.put("schoolId", schoolId);
		param.put("accountName", administratorName);  
		param.put("navIds", navIdList); 
		param.put("from", from);
		if("1".equals(from)){
			param.put("role", T_Role.Teacher.getValue());//老师
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		param.put("termInfoId", termInfoId);
		try {
			int i = adminManageService.updateAdministrator(param);
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if(i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"选择的管理员已设置权限");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"系统异常");
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----重置管理员密码----- **/
	@RequestMapping(value = "resetAdminPassword", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject resetAdminPassword(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String administratorId = request.getString("administratorId");
		param.put("accountId", administratorId);
		param.put("schoolId", schoolId);
		if("1".equals(from)){
			setPromptMessage(response, OutputMessage.generalFail.getCode(),"不支持重置密码！ ");
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		param.put("termInfoId", termInfoId);
		param.put("from", from);
		try {
			adminManageService.resetAdminPassword(param);
			setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----删除管理员----- **/
	@RequestMapping(value = "deleteAdministrator", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteAdministrator(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String administratorId = request.getString("administratorId");
		List<String> ids = Arrays.asList(administratorId.split(","));
		param.put("ids", ids);//accountIds
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		param.put("from", from);
		if("1".equals(from)){
			param.put("role", T_Role.Teacher.getValue());//老师
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		try {
			adminManageService.deleteAdministrator(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----获取管理员下拉列表----- **/
	@RequestMapping(value = "getAdministratorSelectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAdministratorSelectList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String administratorId = request.getString("administratorId");
		param.put("schoolId", schoolId);
		param.put("noAccountId", administratorId); 
		param.put("termInfoId", termInfoId);
		if("1".equals(from)){
			param.put("role", T_Role.Teacher.getValue());//老师
		}else{
			param.put("role", T_Role.SchoolManager.getValue());//学校管理员
		}
		param.put("from", from);
		List<JSONObject> data = null;
		try {
			data = adminManageService.getAdministratorSelectList(param);
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
}
