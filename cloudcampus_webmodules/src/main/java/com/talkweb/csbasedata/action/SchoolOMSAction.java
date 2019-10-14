package com.talkweb.csbasedata.action;

import java.util.Arrays;
import java.util.List;

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
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.service.SchoolOMSManageService;

/**
 * 学校OMS管理-action
 * @author zhh
 * @version 1.0
 */
@Controller
@RequestMapping(value = "/schoolOMSManage/")
public class SchoolOMSAction extends BaseAction {
	@Autowired
	private SchoolOMSManageService schoolOMSManageService;
	@Value("#{settings['currentTermInfo']}")
	private String termInfoId;
	
	/** ---设置提示信息--- **/
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

	/** -----获取区域下拉列表----- **/
	@RequestMapping(value = "getSelectAreaCodeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSelectAreaCodeList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		List<JSONObject> data = null;
		try {
			data = schoolOMSManageService.getSelectAreaCodeList(param);
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
	/** -----查询学校详情----- **/
	@RequestMapping(value = "getSchoolInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String schoolId = request.getString("schoolId");
		param.put("schoolId", schoolId);
		JSONObject data = null;
		try {
			data = schoolOMSManageService.getSchoolInfo(param);
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
	/** -----更新（增加）学校详情----- **/
	@RequestMapping(value = "updateSchoolInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateSchoolInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String countryCode = request.getString("countryCode");
		String schoolId = request.getString("schoolId");
		String schoolName = request.getString("schoolName");
		String phone = request.getString("phone");
		String schoolAddress = request.getString("schoolAddress");
		String schoolType = request.getString("schoolType");
		String teachingStage = request.getString("teachingStage");
		String oldTeachingStage = request.getString("oldTeachingStage");
		List<String> stages = Arrays.asList(teachingStage.split(","));
		List<String> oldStages = Arrays.asList(oldTeachingStage.split(","));
		param.put("areaCode", countryCode);
		param.put("schoolId", schoolId);
		param.put("termInfoId", termInfoId);
		param.put("name", schoolName);  
		param.put("phone", phone); 
		param.put("address", schoolAddress);
		param.put("schoolType", schoolType);
		param.put("stages", stages);
		param.put("oldStages", oldStages);
		int  i = 1;
		try {
			i = schoolOMSManageService.updateSchoolInfo(param);
			if(i>0){
				setPromptMessage(response, OutputMessage.generalSuccess.getCode(),OutputMessage.generalSuccess.getDesc());
			}else if (i==-1){
				setPromptMessage(response, OutputMessage.generalFail.getCode(),"学校名称不能重复");
			}else{
				setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			}
		} catch (Exception e) {
			setPromptMessage(response, OutputMessage.generalFail.getCode(),OutputMessage.generalFail.getDesc());
			e.printStackTrace();
		}
	
	    return response;
	}
}
