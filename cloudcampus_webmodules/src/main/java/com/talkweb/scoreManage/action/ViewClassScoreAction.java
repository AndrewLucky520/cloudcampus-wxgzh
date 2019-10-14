package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.ViewClassScoreService;

@RequestMapping("/scoreReport1/viewClassScore/")
@Controller
public class ViewClassScoreAction extends BaseAction {

	@Autowired
	private ViewClassScoreService viewClassScoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	@RequestMapping(value = "getClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("selectedSemester");// 学年学期
			String examId = request.getString("examId");
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("schoolId", this.getXxdm(req));
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			response.put("data", viewClassScoreService.getClassInfoDropDownList(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}

	@RequestMapping(value = "getClassExamScore", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> getClassExamScore(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();// 返回结果
		
		try {
			String termInfoId = request.getString("termInfoId");// 学年学期
			String examId = request.getString("examId");
			String classIdStr = request.getString("classId");// 班级编号
			String type = request.getString("type"); // 1，系统导入，2，自定义导入
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId) || StringUtils.isBlank(classIdStr)
					|| StringUtils.isBlank(type)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String keyword = request.getString("studentNameId");// 学生姓名或是编号
			
			JSONObject params = new JSONObject();
			params.put("termInfoId", termInfoId);
			params.put("examId", examId);
			params.put("schoolId", getXxdm(req));
			params.put("classIdStr", classIdStr);
			params.put("type", type);
			params.put("keyword", keyword);
			params.put("schoolId", getXxdm(req));
			
			JSONObject data = viewClassScoreService.getClassExamScore(params);
			
			response.put("data", data);
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("data", new JSONObject());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.put("data", new JSONObject());
		}
		return response;
	}
	
	@RequestMapping(value = "getClassExamListByTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassExamListByTeacher(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");// 学年学期
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("schoolId", this.getXxdm(req));
			params.put("termInfoId", termInfoId);
			params.put("accountId", req.getSession().getAttribute("accountId"));
			response.put("data", viewClassScoreService.getClassExamListByTeacher(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}
	
	@RequestMapping(value = "getClassListByTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassListByTeacher(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");// 学年学期
			String examId = request.getString("examId");
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("schoolId", this.getXxdm(req));
			params.put("examId", examId);
			params.put("termInfoId", termInfoId);
			params.put("accountId", req.getSession().getAttribute("accountId"));
			params.put("isAll", request.getIntValue("isAll"));
			response.put("data", viewClassScoreService.getClassListByTeacher(params));
			setResponse(response, 1, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.put("data", new ArrayList<JSONObject>());
		}
		return response;
	}
}
