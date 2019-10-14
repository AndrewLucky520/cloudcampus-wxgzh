package com.talkweb.csbasedata.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.ClassManageService;

@Controller
@RequestMapping(value = "/classManage/")
public class ClassManageAction extends BaseAction {
	
	@Autowired
	private ClassManageService classManageService;
	
	/** -----查询教师信息数目----- **/
	@RequestMapping(value = "getClassTotal", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTotal(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		param.put("schoolId", this.getXxdm(req));
		int classTotal = classManageService.getClassTotal(param);
		if (classTotal > 0) {
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----查询班级列表----- **/
	@RequestMapping(value = "getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xnxq = this.getCurXnxq(req);
		if (StringUtils.isNotEmpty(xnxq) && xnxq.length()>=4)
		{
			param.put("schoolId", getXxdm(req));
			param.put("xn", xnxq.substring(0, 4));
			List<JSONObject> classList = classManageService.getClassList(param);
			if (classList.size() > 0) {
				response.put("data", classList);
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
	
	/** -----批量创建班级----- **/
	@RequestMapping(value = "batchCreateClass", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject batchCreateClass(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();		
		String firstClassName = param.getString("firstClassName");
		String classNum = param.getString("classNum");
		String gradeId = param.getString("gradeId");
		if (StringUtils.isNotEmpty(firstClassName) 
				&& StringUtils.isNotEmpty(gradeId)
				&& StringUtils.isNotEmpty(classNum))
		{
			param.put("schoolId", getXxdm(req));
			int count = classManageService.batchCreateClass(param);
			if (count > 0) {
				setPromptMessage(response, 
						OutputMessage.addSuccess.getCode(),
						OutputMessage.addSuccess.getDesc());
			}else if(count == -100){
				setPromptMessage(response, OutputMessage.nameDublicateError.getCode(),
						"班级名称重复！");			
			} else {
				setPromptMessage(response, 
						OutputMessage.addDataError.getCode(),
						OutputMessage.addDataError.getDesc());
			}
		} else {
			setPromptMessage(response, 
					OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----创建单个班级----- **/
	@RequestMapping(value = "createClass", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject createClass(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();	
		String className = param.getString("className");
		String gradeId = param.getString("gradeId");
		if (StringUtils.isNotEmpty(className) 
				&& StringUtils.isNotEmpty(gradeId))
		{
			param.put("schoolId", getXxdm(req));
			int count = classManageService.createClass(param);
			if (count > 0) {
				setPromptMessage(response, 
						OutputMessage.addSuccess.getCode(),
						OutputMessage.addSuccess.getDesc());
			} else if(count == -100){
				setPromptMessage(response, OutputMessage.nameDublicateError.getCode(),
						"班级名称重复！");			
			}else {
				setPromptMessage(response, 
						OutputMessage.addDataError.getCode(),
						OutputMessage.addDataError.getDesc());
			}
		} else {
			setPromptMessage(response, 
					OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----查询班级详细信息----- **/
	@RequestMapping(value = "getClassInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassInfo(HttpServletRequest req,
			@RequestBody JSONObject requestParam, HttpServletResponse res) {
		JSONObject response = new JSONObject();		
		String classId = requestParam.getString("classId");		
		String xnxq = this.getCurXnxq(req);		
		if (StringUtils.isNotEmpty(classId) && StringUtils.isNotEmpty(xnxq) 
				&& xnxq.length()>=4) {
			requestParam.put("schoolId", getXxdm(req));
			requestParam.put("xn", xnxq.substring(0, 4));
			JSONObject classInfo = classManageService.getClassInfo(requestParam);
			if (null != classInfo) {
				response.put("data", classInfo);
				setPromptMessage(response, 
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, 
						OutputMessage.queryEmpty.getCode(),
						OutputMessage.queryEmpty.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
	    return response;
	}
	
	/** -----更新/编辑班级信息----- **/
	@RequestMapping(value = "updateClass", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateClass(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String classId = param.getString("classId");
		
			param.put("schoolId", Long.parseLong(getXxdm(req)));
			int count = classManageService.updateClass(param);
			
		if (StringUtils.isNotEmpty(classId) && count >0) {
			setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
					OutputMessage.updateSuccess.getDesc());
		}else if(count == -100){
			setPromptMessage(response, OutputMessage.nameDublicateError.getCode(),
					"班级名称重复！");			
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}	
	    return response;
	}
	
	/** -----删除班级----- **/
	@RequestMapping(value = "deleteClass", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteClass(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String classIds = param.getString("classIds");
		if (StringUtils.isNotEmpty(classIds)) {
			param.put("schoolId", getXxdm(req));
			classManageService.deleteClass(param);
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
			classManageService.deleteTeacherCourse(param);
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
		String classId = param.getString("classId");
		JSONArray list = param.getJSONArray("teachers");
		String lessonId = param.getString("lessonId");
		if (StringUtils.isNotEmpty(classId) 
				&& CollectionUtils.isNotEmpty(list)
				&& StringUtils.isNotEmpty(lessonId))
		{
			classManageService.updateTeacherCourse(param);
			setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
					OutputMessage.updateSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.inputError.getCode(),
					OutputMessage.inputError.getDesc());
		}
		return response;
	}
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

}