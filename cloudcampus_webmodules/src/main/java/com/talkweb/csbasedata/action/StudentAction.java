package com.talkweb.csbasedata.action;

import java.io.File;
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
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.StudentBaseManageService;
import com.talkweb.csbasedata.service.StudentImportManageService;
/**
 * 学生管理-action
 * @author zhh
 * @version 1.0
 */
@Controller
@RequestMapping(value = "/studentManage/")
public class StudentAction extends BaseAction {
	@Autowired
	private StudentImportManageService studentImportManageService;
	@Autowired
	private StudentBaseManageService studentManageService;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	private static final String headString="studentManage_";
	private static final String headStringd="studentManage.";
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
/**
 * 学生模块
 */
	/** -----获取学生条数----- **/
	@RequestMapping(value = "getStudentTotal", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentTotal(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String xnxq = getCurXnxq(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue());//学生
		param.put("pRole", T_Role.Parent.getValue());//家长
		param.put("xn", xnxq.substring(0,4));
		int num = 0 ;
		try {
			num = studentManageService.getStudentTotal(param);
			if (num>0) {
				setPromptMessage(response,"1",OutputMessage.querySuccess.getDesc());
			} else if(num==0){
				setPromptMessage(response, "0",OutputMessage.querySuccess.getDesc());
			}else{
				setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.queryFail.getCode(),OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		
	    return response;
	}
	/** -----查询学生列表----- **/
	@RequestMapping(value = "getStudentList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		String condition = request.getString("condition");
		String gradeId = request.getString("gradeId");
		String classId = request.getString("classId");
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String xnxq = getCurXnxq(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue());//学生
		param.put("pRole", T_Role.Parent.getValue());//家长
		param.put("condition", condition);
		param.put("classId", classId);
		param.put("gradeId", gradeId);
		param.put("xn", xnxq.substring(0,4));
		List<JSONObject> data = null;
		try {
			data = studentManageService.getStudentList(param);
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
	/** -----获取学生详情----- **/
	@RequestMapping(value = "getStudentInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String xnxq = getCurXnxq(req);
		JSONObject param = new JSONObject();
		String userId = request.getString("studentId");
		String accountId = request.getString("accountId");
		String parentId = request.getString("parentId");
		String parentAccountId = request.getString("parentAccountId");
		param.put("xn", xnxq.substring(0, 4));
		param.put("parentAccountId", parentAccountId);
		param.put("parentUserId", parentId);
		param.put("accountId", accountId);
		param.put("userId", userId);
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue());//学生
		param.put("pRole", T_Role.Parent.getValue());//家长
		JSONObject data = null;
		try {
			data = studentManageService.getStudentInfo(param);
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
	/** -----重置学生（家长）密码----- **/
	@RequestMapping(value = "resetStudentPassword", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject resetStudentPassword(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String accountId = request.getString("accountId");
		String parentAccountId = request.getString("parentAccountId");
		param.put("parentAccountId", parentAccountId);
		param.put("accountId", accountId);
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue()); 
		param.put("pRole", T_Role.Parent.getValue());//家长
		try {
			studentManageService.resetStudentPassword(param);
			setPromptMessage(response, OutputMessage.generalSuccess.getCode(),"重置密码成功！");
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----删除学生----- **/
	@RequestMapping(value = "deleteStudent", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteStudent(HttpServletRequest req,
		   @RequestBody JSONArray deleteStudents , HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue()); 
		param.put("pRole", T_Role.Parent.getValue());//家长
		param.put("deleteStudents", deleteStudents);//删除学生
		try {
			studentManageService.deleteStudent(param);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),OutputMessage.delSuccess.getDesc());
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.delFail.getCode(),OutputMessage.delFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
	/** -----更新学生----- **/
	@RequestMapping(value = "updateStudent", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateStudent(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		JSONObject param = new JSONObject();
		String accountId = request.getString("accountId");
		String studentId = request.getString("studentId");
		String studentName = request.getString("studentName");
		String gender = request.getString("gender");
		String gradeId = request.getString("gradeId");
		String classId = request.getString("classId");
		String parentMobilePhone = request.getString("parentMobilePhone");
		String studentNumber = request.getString("studentNumber");
		String studentCard = request.getString("studentCard");
		String seatNumber = request.getString("seatNumber");
		String electronicCardNumber = request.getString("electronicCardNumber");
		String parentId = request.getString("parentId");
		String parentAccountId = request.getString("parentAccountId");
		param.put("parentAccountId", parentAccountId);
		param.put("parentId", parentId);
		param.put("schoolId", schoolId);
		param.put("role", T_Role.Student.getValue());
		param.put("pRole", T_Role.Parent.getValue());//家长
		param.put("studentId", studentId);
		param.put("accountId", accountId);
		param.put("name", studentName);  
		if(StringUtils.isBlank(gender)){
			param.put("gender", null); //默认null 
		}else{
			param.put("gender", gender); 
		}
		param.put("gradeId", gradeId); 
		param.put("classId", classId); 
		if(StringUtils.isBlank(parentMobilePhone)){
			param.put("parentMobilePhone", null); 
		}else{
			param.put("parentMobilePhone", parentMobilePhone); 
		}
		if(StringUtils.isBlank(studentNumber)){
			param.put("studentNumber", null);
		}else{
			param.put("studentNumber", studentNumber);
		}
		if(StringUtils.isBlank(studentCard)){
			param.put("studentCard", null); 
		}else{
			param.put("studentCard", studentCard); 
		}
		if(StringUtils.isBlank(seatNumber)){
			param.put("seatNumber", null); 
		}else{
			param.put("seatNumber", seatNumber); 
		}
		param.put("electronicCardNumber", electronicCardNumber); 
		try {
			int i = studentManageService.updateStudent(param);
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if(i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"同班级下学生姓名不能重复");
			}else if(i==-2){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"学生校内电子卡号不能重复");
			}else if(i==-3){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"学生校内学籍号不能重复");
			}else if(i==-4){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"学生校内学号不能重复");
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
 * 学生导入
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
			object = studentImportManageService.uploadExcel(param);
			
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
    		 json = studentImportManageService.getExcelMatch(param);
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
			json = studentImportManageService.startImportTask(object);
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
			json = studentImportManageService.importProgress(param);
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
			json = studentImportManageService.singleDataCheck(param);
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
			json = studentImportManageService.continueImport(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json; 
	}
	
}
