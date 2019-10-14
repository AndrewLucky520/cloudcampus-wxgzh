package com.talkweb.committee.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.utils.HttpClientUtil;
import com.talkweb.utils.SplitUtil;

@Controller
@RequestMapping(value = "/TJManage/")
public class TJManageAction extends BaseAction{

	String rootPath = SplitUtil.getRootPath("education.url");

	/**
	 * 该模块是否为团籍管理员
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "common/getTJRole.do")
    @ResponseBody
    public JSONObject getTJRole(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONObject json = new JSONObject();
		boolean flag = isMoudleManager(req, "cs1044");
		if(flag){ 
			json.put("isManager", 1);//是
		}else{  
			json.put("isManager", 0); //否
		}
		setPromptMessage(json, "0" , "查询成功");
		return json;
   }
			
	/** 添加\编辑学生信息 */
    @RequestMapping(value = "updateStudentInfo.do")
    @ResponseBody
    public JSONObject updateStudentInfo(@RequestBody JSONObject param,HttpServletRequest req, HttpServletResponse res) {
        JSONObject response = new JSONObject();
		
        try {
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/updateStudentInfo.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
		
	    return response;
    }

	/** 删除支部 */
    @RequestMapping(value = "deleteStudent.do")
    @ResponseBody
    public JSONObject deleteStudent(@RequestBody JSONObject param,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
    	try {
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/deleteStudent.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
		return response;
    }
	
	/** 根据学生列表 */
	@RequestMapping(value = "getStudentList.do")
	@ResponseBody
	public JSONObject getStudentList(@RequestBody JSONObject param, HttpServletRequest req,HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/getStudentList.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
		return response;
	}

	/** 获取支部列表 */
    @RequestMapping(value = "getStudentForBD.do")
    @ResponseBody
    public JSONObject getStudentForBD(@RequestBody JSONObject param,HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
    	try {
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/getStudentForBD.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
		return response;
    }
    
    /**
     * 公共下载EXCEL表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="exportExcelByData.do")
    @ResponseBody  
    public void demoForDownExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{      
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    
	/** -----查询年级信息列表----- **/
	@RequestMapping(value = "getTJGradeList.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTJGradeList(HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();		
		
		try {
			JSONObject param = new JSONObject();	
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/getTJGradeList.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
	    return response;
	}
	
	/** -----获取年级下的班级----- **/
	@RequestMapping(value = "getTJClassList.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTJClassList(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		try {
        	String schoolId = this.getXxdm(req);
        	param.put("schoolId", schoolId);
			String result = HttpClientUtil.doPostJson(rootPath+"TJManage/getTJClassList.do", param.toString());
			return JSONObject.parseObject(result);
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),
					OutputMessage.delFail.getDesc());
		}
		
	    return response;
	}
    
    private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
    }
    
}