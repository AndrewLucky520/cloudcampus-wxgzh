package com.talkweb.community.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.CsCurCommonDataService;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.community.service.CommunityService;
import com.talkweb.utils.HttpClientUtil;
import com.talkweb.utils.SplitUtil;

@Controller
@RequestMapping(value="/community/")
public class CommunityAction  extends BaseAction  {

	String rootPath = SplitUtil.getRootPath("education.url");

	@Autowired 
	CommunityService communityService;
	
	@Autowired
	CsCurCommonDataService csCurCommonDataService;
	
	@Value("#{settings['clientId']}")
	private String clientId;
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	/**
	 * 查询社团
	 * @param req
	 * @param res
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "getComunities", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getComunities(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
        	Long userId = (Long)req.getSession().getAttribute("userId");
    		// 判断该user是否为家长
    		User user = csCurCommonDataService.getUserById(Long.parseLong(schoolId), userId, getCurXnxq(req));
    		if(user.getParentPart() != null) {
    			// 获取家长的学生
    			long stuUserId = user.getParentPart().getStudentId();
    			params.put("memberId", stuUserId);
    		}else {
    			params.put("memberId", userId);
    		}
			String result = HttpClientUtil.doPostJson(rootPath+"community/getComunities", params.toString());
			return JSONObject.parseObject(result == null ? "": result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
	}
	
	/**
	 * 创建社团
	 * @param req
	 * @param res
	 * @param params
	 * @return
	 */
	
	@RequestMapping(value = "operateCommunity", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject operateComminity(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"community/operateCommunity", params.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
	}
	
	
	/**
	 * 获取获取信息
	 * @param req
	 * @param res
	 * @param params
	 * @return
	 */
	
	@RequestMapping(value = "getActions", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getActions(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"community/getActions", params.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
	}
	
	
	/**
	 * 创建修改活动
	 * @param req
	 * @param res
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "operateAction", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject operateAction(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"community/operateAction", params.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
	}
	
	/**
	 * 查询学生
	 */
	@RequestMapping(value = "selectStudent", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject selectStudent(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"community/selectStudent", params.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
	}
	
	/*
	 * 获取角色
	 */
	@RequestMapping(value = "getRole", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRole(HttpServletRequest req, HttpServletResponse res,
			@RequestBody JSONObject params) {
		User user = (User)req.getSession().getAttribute("user");
		int roleId = user.getUserPart().getRole().getValue();
		long accountId = user.getAccountPart().getId();
		JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	params.put("schoolId", schoolId);
        	params.put("roleId", roleId);
        	params.put("accountId", accountId+"");
        	params.put("userId", user.getUserPart().getId());
			//System.out.println(rootPath+":"+params);
			String result = HttpClientUtil.doPostJson(rootPath+"community/getRole", params.toString());
			//System.out.println(rootPath+":"+result);
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
		
	}
}
