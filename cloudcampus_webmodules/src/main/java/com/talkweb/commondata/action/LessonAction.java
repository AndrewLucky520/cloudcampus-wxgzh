package com.talkweb.commondata.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.LessonManageService;
import com.talkweb.commondata.util.OutputMessage;

@Controller
@RequestMapping(value = "/lessonManage/")
public class LessonAction extends BaseAction {
	
	@Autowired
	private LessonManageService lessonManageService;
	
	/** -----查询科目列表----- **/
	@RequestMapping(value = "getLessonList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLessonList(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String termInfoId = getCurXnxq(req);
		List<JSONObject> lessonList = new ArrayList<JSONObject>();
		try {
			lessonList = lessonManageService.getLessonList(schoolId,termInfoId);
		} catch (Exception e) {
			setPromptMessage(response, 
					OutputMessage.queryFail.getCode(),
					OutputMessage.queryFail.getDesc());
			e.printStackTrace();
		}
		if (lessonList.size() > 0) {
			response.put("data", lessonList);
			setPromptMessage(response, 
					OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, 
					OutputMessage.queryEmpty.getCode(),
					OutputMessage.queryEmpty.getDesc());
		}
	    return response;
	}
	
	/** -----更新科目信息----- **/
	@RequestMapping(value = "updateLesson", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateLesson(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String termInfoId = getCurXnxq(req);
		param.put("schoolId", getXxdm(req));
		param.put("termInfoId", termInfoId);
		int count=0;
		try {
			count = lessonManageService.updateLesson(param);
		} catch (Exception e) {
			setPromptMessage(response, 
					OutputMessage.updateFail.getCode(),
					OutputMessage.updateFail.getDesc());
			e.printStackTrace();
		}
		if (count > 0) {
			setPromptMessage(response, 
					OutputMessage.updateSuccess.getCode(),
					OutputMessage.updateSuccess.getDesc());
		} else if(count==-1){
			setPromptMessage(response, 
					OutputMessage.updateDataError.getCode(),
					"科目名称重复");
		}else {
			setPromptMessage(response, 
					OutputMessage.updateDataError.getCode(),
					OutputMessage.updateDataError.getDesc());
		}
	    return response;
	}
	
	/** -----删除科目信息----- **/
	@RequestMapping(value = "deleteLesson", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteLesson(HttpServletRequest req,
			@RequestBody JSONObject param, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String termInfoId = getCurXnxq(req);
		String lessonIds = param.getString("lessonIds");
		if (StringUtils.isNotEmpty(lessonIds)) {
			param.put("lessonIds", lessonIds);
			param.put("termInfoId", termInfoId);
			try {
				lessonManageService.deleteLesson(param);
			} catch (Exception e) {
				setPromptMessage(response, 
						OutputMessage.delFail.getCode(),
						OutputMessage.delFail.getDesc());
				e.printStackTrace();
			}
			setPromptMessage(response, 
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());		
		} else {
			setPromptMessage(response, 
					OutputMessage.inputError.getCode(),
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