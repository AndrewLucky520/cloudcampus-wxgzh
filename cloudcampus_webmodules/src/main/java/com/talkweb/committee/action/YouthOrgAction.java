package com.talkweb.committee.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.utils.HttpClientUtil;
import com.talkweb.utils.SplitUtil;

/**
 * 团组织管理
 * @author zhanghuihui
 *
 */
@Controller
@RequestMapping(value = "/youthOrg/")
public class YouthOrgAction extends BaseAction{

	String rootPath = SplitUtil.getRootPath("education.url");

	private static final Logger logger = LoggerFactory.getLogger(YouthOrgAction.class);
	
	private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
	 }
	
/**公共接口 ***/
		/**
		 * 该模块是否为管理员
		 * @param requestParams
		 * @param req
		 * @param res
		 * @return
		 */
	    @RequestMapping(value = "common/getYORole")
	    @ResponseBody
	    public JSONObject getYORole(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
	        //List<JSONObject> arr = new ArrayList<JSONObject>();
	        JSONObject json=new JSONObject();
			String msg = "查询成功";
			String  code = "0";
			boolean flag = isMoudleManager(req, "cs1045");
			if(flag){ 
				json.put("isManager", 1);//是
			}else{  
				json.put("isManager", 0); //否
			}
			setPromptMessage(json, code, msg);
			return json;
	   }
	    
	    /**
		 * 根据姓名模糊查询学生列表
		 * @param requestParams
		 * @param req
		 * @param res
		 * @return
		 */
	    @RequestMapping(value = "common/getAccountListByName")
	    @ResponseBody
	    public JSONObject getAccountListByName(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
	        JSONObject response = new JSONObject();
			
	        try {
	        	String schoolId = this.getXxdm(req);
	        	requestParams.put("schoolId", schoolId);
				String result = HttpClientUtil.doPostJson(rootPath+"youthOrg/common/getAccountListByName", requestParams.toString());
				return JSONObject.parseObject(result);
			} catch (Exception e) {
				setPromptMessage(response, OutputMessage.delFail.getCode(),
						OutputMessage.delFail.getDesc());
			}
	        
			return response;
	   }
/**管理员首页 ***/
	/**
	 * 获取支部列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getYouthOrgList")
    @ResponseBody
    public JSONObject getYouthOrgList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	requestParams.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"youthOrg/getYouthOrgList", requestParams.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
    }
    
	/**
	 * 创建/编辑支部
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "addYouthOrg")
    @ResponseBody
    public JSONObject addYouthOrg(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	requestParams.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"youthOrg/addYouthOrg", requestParams.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
        
		return response;
    }
	/**
	 * 删除支部
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "deleteYouthOrg")
    @ResponseBody
    public JSONObject deleteYouthOrg(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
    	
    	try {
        	String schoolId = this.getXxdm(req);
        	requestParams.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"youthOrg/deleteYouthOrg", requestParams.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
    	
    	return response;
    }
    
	/**
	 * 获取支部详情
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getYouthOrgDetail")
    @ResponseBody
    public JSONObject getYouthOrgDetail(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
    	logger.info("getYouthOrgDetail");
    	try {
        	String schoolId = this.getXxdm(req);
        	requestParams.put("schoolId", schoolId);
        	logger.info("params:"+requestParams+",url:"+rootPath+"youthOrg/getYouthOrgDetail");
        	
			String result = HttpClientUtil.doPostJson(rootPath+"youthOrg/getYouthOrgDetail", requestParams.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
    	
    	return response;
    }

}
