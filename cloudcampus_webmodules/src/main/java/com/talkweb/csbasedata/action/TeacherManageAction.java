package com.talkweb.csbasedata.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.ImportManageService;
import com.talkweb.csbasedata.service.TeacherManageService;

@Controller
@RequestMapping(value = "/teacherManage/")
public class TeacherManageAction extends BaseAction { 
	
	@Autowired
	private TeacherManageService teacherManageService;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	private static final String headString="teacherManage_";
	private static final String headStringd="teacherManage.";
	@Autowired
	private ImportManageService teacherImportService;
    
    /** -----查询教师信息数目----- **/
	@RequestMapping(value = "getTeacherTotal", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTotal(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		int teacherTotal=0;
		try {
			teacherTotal = teacherManageService.getTeacherTotal(Long.parseLong(getXxdm(req)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (teacherTotal > 0) {
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----查询教师列表----- **/
	@RequestMapping(value = "getTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherList(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		param.put("schoolId", getXxdm(req));
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		try {
			teacherList = teacherManageService.getTeacherList(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (teacherList.size() > 0) {
			response.put("data", teacherList);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----增加教师----- **/
	@RequestMapping(value = "addTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addTeacher(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		requestParam.put("schoolId", getXxdm(req));
		int result = 0;
		try {
			result = teacherManageService.addTeacher(requestParam);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result > 0){
			setPromptMessage(response, OutputMessage.addSuccess.getCode(),
					OutputMessage.addSuccess.getDesc());
		}else if(result == -100){
			setPromptMessage(response, OutputMessage.nameDublicateError.getCode(),
					OutputMessage.nameDublicateError.getDesc());
		}else {
			setPromptMessage(response, 
					OutputMessage.addDataError.getCode(),
					OutputMessage.addDataError.getDesc());
		}		
	    return response;
	}
	
	/** -----更新教师信息----- **/
	@RequestMapping(value = "updateTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeacher(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String accountId = param.getString("accountId");
		String teacherName = param.getString("teacherName");
		if (StringUtils.isNotEmpty(accountId) 
				&& StringUtils.isNotEmpty(teacherName)){
			param.put("schoolId", getXxdm(req));
			int count = 0 ;
			try {
				count = teacherManageService.updateTeacher(param);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (count > 0){
				setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}else if(count == -100){
				setPromptMessage(response, OutputMessage.nameDublicateError.getCode(),
						OutputMessage.nameDublicateError.getDesc());
			}else{
				setPromptMessage(response, OutputMessage.updateFail.getCode(),
						OutputMessage.updateFail.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----重置老师密码----- **/
	@RequestMapping(value = "resetTeacherPassword", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject resetTeacherPassword(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String accountId = requestParam.getString("accountId");
		if (StringUtils.isNotEmpty(accountId)) {
			int success = 0;
			try {
				success = teacherManageService.resetTeacherPassword(accountId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (success > 0) {
				setPromptMessage(response, OutputMessage.resetPwSuccess.getCode(),
						OutputMessage.resetPwSuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.updateDataError.getCode(),
						OutputMessage.updateDataError.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----查询教师详细信息----- **/
	@RequestMapping(value = "getTeacherInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherInfo(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();		
		String teacherId = requestParam.getString("teacherId");
		String accountId = requestParam.getString("accountId");
		if (StringUtils.isNotEmpty(accountId)
				&& StringUtils.isNotEmpty(teacherId))
		{
			JSONObject param = new JSONObject();
			param.put("teacherId", teacherId);
			param.put("schoolId", getXxdm(req));
			param.put("accountId", accountId);
			JSONObject teacherInfo = new JSONObject();
			try {
				teacherInfo = teacherManageService.getTeacherInfo(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (null != teacherInfo) {
				response.put("data", teacherInfo);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
						OutputMessage.queryEmpty.getDesc());
			}
		}else{
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----删除老师信息----- **/
	@RequestMapping(value = "deleteTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteTeacher(HttpServletRequest req,
			@RequestBody JSONArray param, HttpServletResponse res) {
		JSONObject response = new JSONObject();	
		if (CollectionUtils.isNotEmpty(param)){
			try {
				teacherManageService.deleteTeacher(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
		return response;
	}	
	
	/** -----删除老师任教关系----- **/
	@RequestMapping(value = "deleteTeacherCourse", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteTeacherCourse(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();	
		String classId = param.getString("classId");
		String teacherId = param.getString("teacherId");	
		String lessonId = param.getString("lessonId");
		if (StringUtils.isNotEmpty(classId) 
				&& StringUtils.isNotEmpty(teacherId)
				&& StringUtils.isNotEmpty(lessonId))
		{
			try {
				teacherManageService.deleteTeacherCourse(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
		return response;
	}	
	
	/** -----更新老师任教关系----- **/
	@RequestMapping(value = "updateTeacherCourses", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeacherCourses(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();	
		String teacherId = param.getString("teacherId");
		String lessonId = param.getString("lessonId");
		JSONArray list = param.getJSONArray("teachingClass");
		if (StringUtils.isNotEmpty(teacherId) 
				&& CollectionUtils.isNotEmpty(list)
				&& StringUtils.isNotEmpty(lessonId))
		{
			try {
				teacherManageService.updateTeacherCourses(param);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
					OutputMessage.updateSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
		return response;
	}
	
	/* *//**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     *//*
    @RequestMapping(value = "/importTeacher")
    @ResponseBody
	public JSONObject uploadExcel(@RequestParam("importFile") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {
    	JSONObject object = new JSONObject();
    	JSONObject param = new JSONObject(); 
    	String schoolId = getXxdm(req);
    	param.put("schoolId", schoolId);
        // 获取文件后缀名和文件名称
    	String fileName = file.getOriginalFilename();
		String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		fileName = fileName.substring(0,fileName.lastIndexOf(".") + 1) + prefix;
		String uuid = UUID.randomUUID().toString();
		File dfile = new File(uuid + "." + prefix);
		try {
			 file.transferTo(dfile);
		} catch (Exception e) {
			String code = "-2103";
			object.put("code", code);
			String msg = OutputMessage.getDescByCode(code);
			object.put("msg", msg);
		}
    	param.put("file", dfile);
    	param.put("fileName", fileName);
    	param.put("uuid", uuid);
    	param.put("prefix", prefix);
    	param.put("result", object);
    	object = teacherImportService.uploadExcel(param);
    	if (object.containsKey("keyId"))
    	{
    		req.getSession().setAttribute("keyId", object.get("keyId"));
    		object.remove("keyId");
    	}
    	if(object.containsKey("tempTitle"))
    	{
			req.getSession().setAttribute(
					"teacherImport." + schoolId + ".title",
					object.get("tempTitle"));
			object.remove("tempTitle");
    	}
    	return object;   	
    }
    
    *//**
     * 获取Excel表头和系统字段
     * 
     * @param req
     * @return JSONObject
     * 
     *//*
    @RequestMapping(value = "/getExcelMatch")
    @ResponseBody
    public JSONObject getExcelHead(HttpServletRequest req){ 	
    	String sessionId = req.getSession().getId();
    	String schoolId = getXxdm(req);
    	String[] tmpTitle =(String[]) req.getSession().getAttribute("teacherImport."+schoolId+".title");
    	JSONObject param = new JSONObject();
    	param.put("sessionId", sessionId);
    	param.put("tempTitle", tmpTitle);	
    	return teacherImportService.getExcelHead(param);
    }
    
    *//**
     * 导入任务启动接口
     * 
     * @param req
     * @param res
     * @return JSONObject
     *//*
    @RequestMapping(value = "/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody Map<String,Object> mappost, HttpServletResponse res) {      
    	String isMatch = mappost.get("isMatch").toString();
        JSONObject param = new JSONObject();
        String progressId = req.getSession().getId();
        if (!"0".equals(isMatch)){
        	param.put("matchResult",JSON.parseArray(mappost.get("matchResult").toString()));
        } 
        param.put("isMatch", Integer.parseInt(isMatch));
        param.put("schoolId",getXxdm(req));       
		String keyId = req.getSession().getAttribute("keyId").toString();
		param.put("keyId",keyId);   
        
	 	JSONObject procObj = new JSONObject();
	 	procObj.put("taskParam", param);
	 	Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();		
	 	JSONObject data = new JSONObject();
		data.put("progress", 0);
		data.put("msg", OutputMessage.getDescByCode("100"));
		procObj.put("data", data);
		procObj.put("code", 1);
		progressMap.put(progressId, procObj);
		
	 	String progressMapKey = "teacherImport."+progressId+".progressMap";
	 	JSONObject object = new JSONObject();
	 	object.put("progressId",progressId);
	 	object.put("progerssMapKey", progressMapKey);
	 	object.put("progressMap", progressMap);                            
	 	return teacherImportService.startImportTask(object);
    }
    
    *//**
     * 获取导入进度
     * 
     * @return JSONObject
     *//*
	@RequestMapping(value = "/importProgress")
    @ResponseBody
    public JSONObject importProgress(HttpServletRequest req, HttpServletResponse res) {
    	JSONObject param = new JSONObject();
    	String processId = req.getSession().getId();
    	param.put("progressMapKey", "teacherImport."+ processId +".progressMap");
    	param.put("progressId", processId);
    	param.put("schoolId", getXxdm(req));
    	param.put("keyId", req.getSession().getAttribute("keyId").toString());
    	return teacherImportService.importProgress(param);
    } 
	
	*//**
	 * 单条数据修改
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception 
	 *//*
	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req,
			@RequestBody JSONObject post, HttpServletResponse res) throws Exception {
		JSONObject param = new JSONObject();
		String progressId = req.getSession().getId();
		param.put("progressId", progressId);
		String prepDataMapKey="teacherImport."+progressId+".prepDataMap";
		param.put("prepDataMapKey", prepDataMapKey);	
		String progressMapKey = "teacherImport."+progressId+".progressMap";
		param.put("progressMapKey", progressMapKey);	
		int row = post.getIntValue("row");
		param.put("row", row);
		JSONArray mrows = post.getJSONArray("mrows");
		param.put("mrows", mrows);
		int code = post.getIntValue("code");
		param.put("code", code);
		param.put("schoolId", getXxdm(req));
		return teacherImportService.singleDataCheck(param);
	}
	
	 *//**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 *//*
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject param = new JSONObject();
		param.put("schoolId", getXxdm(req));
		String progressId = req.getSession().getId();
		param.put("progressId", progressId);
		String prepDataMapKey="teacherImport."+progressId+".prepDataMap";
		param.put("prepDataMapKey", prepDataMapKey);	
		String progressMapKey = "teacherImport."+progressId+".progressMap";
		param.put("progressMapKey", progressMapKey);
		return teacherImportService.continueImport(param);
	}*/
	
/**
 * 老师导入
 * @author zhanghuihui
 */
	 /**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     */
    @RequestMapping(value = "/importTeacher")
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
			object = teacherImportService.uploadExcel(param);
			
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
    		 json = teacherImportService.getExcelMatch(param);
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
    	String xnxq = getCurXnxq(req);
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
		object.put("xnxq", xnxq);
	 	object.put("keyId", keyId);
	 	object.put("schoolId", getXxdm(req));
	 	object.put("isMatch", isMatch);
	 	object.put("processId", processId);
	 	object.put("stuTitle", stuTitle);
		object.put("stuTitleNeed", stuTitleNeed);
		object.put("stuTitleName", stuTitleName);
	 	JSONObject json = new JSONObject();
	 	try {
			json = teacherImportService.startImportTask(object);
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
			json = teacherImportService.importProgress(param);
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
			json = teacherImportService.singleDataCheck(param);
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
			json = teacherImportService.continueImport(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json; 
	}
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

}