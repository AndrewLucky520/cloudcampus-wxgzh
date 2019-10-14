package com.talkweb.commondata.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.TeacherLessonService;

@Controller
@RequestMapping(value="/teacherLessonAction/")
public class TeacherLessonAction {
	@Autowired
	private TeacherLessonService teacherLessonService;
	
	
	private static final Logger logger = LoggerFactory.getLogger(TeacherLessonAction.class);
	
	 private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
	 }
	 
	@RequestMapping(value="updateTeacherLesson")
	@ResponseBody
	public JSONObject updateTeacherLesson(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject returnObj = new JSONObject();
		String accessToken = request.getString("accessToken");
		String extTeacherId = request.getString("extTeacherId");
		String extSchoolId = request.getString("extSchoolId");
		String clientId =  request.getString("clientId");
		List<JSONObject> classLessons=  (List<JSONObject>) request.get("classLessons");
		int i=0;
		try {
			i = teacherLessonService.updateTeacherLesson(extSchoolId,extTeacherId,classLessons,accessToken,clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(i>=0){
			setPromptMessage(returnObj, "0", "操作成功");
		}else{
			if(i==-2){
				setPromptMessage(returnObj, "-2", "accessToken验证失败");
			}else{
				setPromptMessage(returnObj, "-1", "操作失败");
			}
		}
		return returnObj;
	}
	
	@RequestMapping(value="updateTeacherLessonBatch")
	@ResponseBody
	public JSONObject updateTeacherLessonBatch(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject returnObj = new JSONObject();
		String accessToken =   request.getString("accessToken");
		String extSchoolId =  request.getString("extSchoolId");
		String clientId =   request.getString("clientId");
		List<JSONObject> teacherClassLessons= (List<JSONObject>) request.get("teacherClassLessons");
		int i=0;
		try {
			i = teacherLessonService.updateTeacherLessonBatch(extSchoolId,teacherClassLessons,accessToken,clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(i>=0){
			setPromptMessage(returnObj, "0", "操作成功");
		}else{
			if(i==-2){
				setPromptMessage(returnObj, "-2", "accessToken验证失败");
			}else{
				setPromptMessage(returnObj, "-1", "操作失败");
			}
		}
		return returnObj;
	}
	
	@RequestMapping(value="updateClassroom")
	@ResponseBody
	public JSONObject updateClassroom(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject returnObj = new JSONObject();
		String accessToken = request.getString("accessToken");
		String extSchoolId = request.getString("extSchoolId");
		String extClassId = request.getString("extClassId");
		String clientId = request.getString("clientId");
		String extHeadClassTeacherId = (String) request.getString("extHeadClassTeacherId");
		List<JSONObject> teacherLessons= (List<JSONObject>) request.get("teacherLessons");
		int i=0;
		try {
			i = teacherLessonService.updateClassroom(extSchoolId,extClassId,extHeadClassTeacherId,teacherLessons,accessToken,clientId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(i>=0){
			setPromptMessage(returnObj, "0", "操作成功");
		}else{
			if(i==-2){
				setPromptMessage(returnObj, "-2", "accessToken验证失败");
			}else{
				setPromptMessage(returnObj, "-1", "操作失败");
			}
		}
		return returnObj;
	}
	public static void main(String[] args) {
		String uuid = UUIDUtil.getUUID();
		System.out.println(uuid);
	}
	
}
