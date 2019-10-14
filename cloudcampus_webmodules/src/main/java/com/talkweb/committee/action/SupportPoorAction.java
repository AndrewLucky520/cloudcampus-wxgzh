package com.talkweb.committee.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.action.BaseAction;
import com.talkweb.utils.HttpClientUtil;
import com.talkweb.utils.SplitUtil;
 
/** 
* @author  Administrator
* @version 创建时间：2017年12月11日 下午2:32:59 
* 程序的简单说明
*/

@Controller
@RequestMapping(value="/supportPoor")
public class SupportPoorAction  extends BaseAction{
    static final Logger logger = LoggerFactory.getLogger(SupportPoorAction.class);

	String rootPath = SplitUtil.getRootPath("education.url");

	Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
 
	@RequestMapping(value="/getRole",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
 		boolean flag = isMoudleManager(req, "cs1048");
		if(flag){
			response.put("isAdmin", 1);
		}else {
			response.put("isAdmin", 0);
		}
		setPromptMessage(response, "1", "查询成功");
		return response;
	}
	
	
	@RequestMapping(value="querySupportPoorList",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject querySupportPoorList(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject param){
	    JSONObject response = new JSONObject();
		try {
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"supportPoor/querySupportPoorList", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败!");
		}
		
	   return response;
	}

	@RequestMapping(value="updateSupportPoor",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject updateSupportPoor(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject param){
	    JSONObject response = new JSONObject();
		
		try {
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"supportPoor/updateSupportPoor", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败!");
		}
		
	   return response;
	}
 

	@RequestMapping(value="deleteSupportPoor",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteSupportPoor(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject param){
	    JSONObject response = new JSONObject();
	    
	    try {
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"supportPoor/deleteSupportPoor", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败!");
		}
	    
	   return response;
	}
	
	
	@RequestMapping(value="getSupportPoor",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getSupportPoor(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject param){
	    JSONObject response = new JSONObject();
		
	    try {
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"supportPoor/getSupportPoor", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败!");
		}
	    
	   return response;
	}
	
	@RequestMapping(value="getStudents",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudents(@RequestBody JSONObject param , HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();

		try {
			String schoolId = getXxdm(req);
			param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"supportPoor/getStudents", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, "-1", "查询失败!");
		}
		
		return response;
	}
 
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
 
	
}
