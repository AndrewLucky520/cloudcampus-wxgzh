package com.talkweb.csbasedata.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.SchoolManageService;

@Controller
@RequestMapping(value = "/schoolManage/")
public class SchoolAction extends BaseAction {
	
	@Autowired
	private SchoolManageService schoolManageService;
		
	/** -----查询学校详细信息----- **/
	@RequestMapping(value = "getSchoolInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolInfo(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject school = schoolManageService.getSchoolInfo(schoolId);
		if (null != school) {
			response.put("data", school);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----更新学校详细信息----- **/
	@RequestMapping(value = "updateSchoolInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateSchoolInfo(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		requestParam.put("schoolId", getXxdm(req));
		schoolManageService.updateSchoolInfo(requestParam);	
		setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
				OutputMessage.updateSuccess.getDesc());
	    return response;
	}
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
}