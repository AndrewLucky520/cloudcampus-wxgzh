package com.talkweb.scoreManage.action;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName ScoreLessionSetAction
 * @author zhuxiaoyue
 * @Desc 成绩分析 - 报告参数设置
 * @date 2015年5月15日
 */
@Controller
@RequestMapping("/scoremanage1/setting/")
public class ScoreReportSetAction extends BaseAction {
	@Autowired
	private ScoreManageService scoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 获取班级报告参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getClassReportParam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassReportParam(HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xxdm = getXxdm(req);

			response.putAll(scoreService.getClassReportParam(xxdm));

			setResponse(response, 0, "");
			response.getJSONObject("data").put("msg", "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 保存班级报告参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateClassReportParam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateClassReportParam(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xxdm = getXxdm(req);
			request.put("xxdm", xxdm);

			scoreService.updateClassReportParam(request);

			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取学生报告参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStudentReportParam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentReportParam(HttpServletRequest req, HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {

			String xxdm = getXxdm(req);

			response.putAll(scoreService.getStudentReportParam(xxdm));

			setResponse(response, 0, "");
			response.getJSONObject("data").put("msg", "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 保存学生报告参数
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateStudentReportParam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateStudentReportParam(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {

			request.put("xxdm", getXxdm(req));

			scoreService.updateStudentReportParam(request);

			setResponse(response, 0, "保存成功！");
			response.getJSONObject("data").put("msg", "保存成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.getJSONObject("data").put("msg", e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			response.getJSONObject("data").put("msg", "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

}
