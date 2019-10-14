package com.talkweb.scoreManage.action;

import java.util.List;

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
import com.talkweb.scoreManage.service.ClassScoreCrudService;

@RequestMapping("/scoreReport1/classScoreCRUD/")
@Controller
public class ClassScoreCrudAction extends BaseAction {
	@Autowired
	private ClassScoreCrudService classScoreCrudService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	@RequestMapping(value = "getExamList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamList(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			request.put("schoolId", this.getXxdm(req));// 设置学校
			request.put("createUserId", req.getSession().getAttribute("accountId"));
			List<JSONObject> examList = classScoreCrudService.getExamList(request);// 考试列表

			response.put("data", examList);
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -2, "服务器异常，请联系管理员！");
		}
		return response;
	}

	@RequestMapping(value = "createExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject createExam(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			String examName = request.getString("examName");
			if (StringUtils.isBlank(termInfoId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			if (StringUtils.isBlank(examName)) {
				throw new CommonRunException(-1, "考试名称不能为空！");
			}
			request.put("schoolId", this.getXxdm(req));
			request.put("accountId", req.getSession().getAttribute("accountId"));

			classScoreCrudService.createExam(request);// 插入记录数量

			setResponse(response, 0, "添加考试成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -2, "服务器异常，请联系管理员！");
		}
		return response;
	}

	@RequestMapping(value = "updateExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateExam(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			String examName = request.getString("examName");
			String examId = request.getString("examId");
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			if (StringUtils.isBlank(examName)) {
				throw new CommonRunException(-1, "考试名称不能为空！");
			}

			request.put("schoolId", this.getXxdm(req));
			request.put("updateTime", getNowString());
			request.put("accountId", req.getSession().getAttribute("accountId"));
			
			classScoreCrudService.updateExam(request);// 插入记录数量

			setResponse(response, 0, "更改考试名称成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -2, "服务器异常，请联系管理员！");
		}
		return response;
	}

	@RequestMapping(value = "deleteExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteExam(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			String examId = request.getString("examId");
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			request.put("schoolId", this.getXxdm(req));
			request.put("accountId", req.getSession().getAttribute("accountId"));
			classScoreCrudService.deleteExam(request);

			setResponse(response, 0, "删除成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -2, "服务器异常，请联系管理员！");
		}
		return response;
	}

	@RequestMapping(value = "releaseClassExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject releaseClassExam(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String termInfoId = request.getString("termInfoId");
			String examId = request.getString("examId");
			Integer pub2StuFlag = request.getInteger("pub2StuFlag"); // 1：发布到学生；0：取消发布
			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(examId) || pub2StuFlag == null) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			request.put("schoolId", getXxdm(req));
			request.put("accountId", req.getSession().getAttribute("accountId"));
			request.put("time", getNowString());

			int isShow = classScoreCrudService.updateReleaseExam(request);
			response.put("pub2StuFlag", isShow);
			setResponse(response, 0, "操作成功");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			response.put("pub2StuFlag", 0);
		} catch (Exception e) {
			e.printStackTrace();
			setResponse(response, -2, "服务器异常，请联系管理员！");
			response.put("pub2StuFlag", 0);
		}
		return response;
	}
}
