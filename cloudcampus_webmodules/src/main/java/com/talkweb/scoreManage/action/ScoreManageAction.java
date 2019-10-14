package com.talkweb.scoreManage.action;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName: ScoreManageAction.java
 * @version:1.0
 * @Description: 成绩管理控制器
 * @author 武洋 ---智慧校
 * @date 2015年3月25日
 */
@Controller
@RequestMapping(value = "/scoremanage1/curd/")
public class ScoreManageAction extends BaseAction {
	@Autowired
	private ScoreManageService scoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 录入成绩--获取考试列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getExamList", method = RequestMethod.POST)
	@ResponseBody
	public List<JSONObject> getExamList(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String usedGradeId = request.getString("usedGradeId");

			if (StringUtils.isBlank(usedGradeId) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));

			List<String> usedGradeList = StringUtil.convertToListFromStr(usedGradeId, ",", String.class);
			params.put("usedGradeList", usedGradeList);
			setResponse(response, 1, "");
			return scoreService.getDegreeInfoList(params);
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			setResponse(response, -1, e.getMessage());
			e.printStackTrace();
		}
		return new ArrayList<JSONObject>();
	}

	/**
	 * 新增考试
	 * 
	 * @param req
	 * @param mappost
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "createExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject createExam(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String usedGradeId = request.getString("usedGradeId");
			String kslcmc = request.getString("examName");

			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(usedGradeId) || StringUtils.isBlank(kslcmc)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcmc", kslcmc);
			params.put("xnxq", xnxq);
			params.put("usedGradeId", usedGradeId);
			params.put("lrr", req.getSession().getAttribute("accountId"));
			scoreService.createExam(params);
			setResponse(response, 1, "创建成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			setResponse(response, -1, e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 更新考试轮次名称
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updatetExamName(HttpServletRequest req, @RequestBody JSONObject request,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String kslcdm = request.getString("examId");
			String kslcmc = request.getString("examName");
			String xnxq = request.getString("termInfoId");
			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(kslcmc) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslcdm);
			params.put("kslcmc", kslcmc);
			params.put("xnxq", xnxq);
			scoreService.updatetExamName(params);
			setResponse(response, 1, "修改成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			setResponse(response, -1, e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 删除考试轮次
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "deleteExam", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteExam(@RequestBody JSONObject request, HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String kslcdm = request.getString("examId");
			String xnxq = request.getString("termInfoId");

			if (StringUtils.isBlank(kslcdm) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			scoreService.deleteExam(params);
			scoreService.deleteDegreeinfoRelate(params);//删除考网信息
			setResponse(response, 1, "删除成功！");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			setResponse(response, -1, e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 获取单次考试成绩列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getScoreList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.get("examId") == null ? "" : request.get("examId").toString();
			String bhs = request.getString("classId");
			String kmdms = request.getString("lessonInfoId");
			String stdNumOrName = request.getString("stdNumOrName");
			if (StringUtils.isEmpty(xnxq) || StringUtils.isEmpty(kslc) || StringUtils.isEmpty(bhs)
					|| StringUtils.isEmpty(kmdms)) {
				throw new CommonRunException(-1, "参数传递错误，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("bhs", bhs);
			params.put("kmdms", kmdms);
			params.put("stdNumOrName", stdNumOrName);
			JSONObject data = scoreService.getScoreInfoList(params);
			response.putAll(data);
			setResponse(response, 0, "");
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "后台异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 保存成绩-新增或修改
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateScore", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateScore(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject data = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			JSONArray scores = request.getJSONArray("score");
			if (StringUtils.isEmpty(xnxq) || scores == null || StringUtils.isEmpty(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("scores", scores);

			scoreService.updateScore(params);

			response.put("code", 0);
			data.put("msg", "保存成功！");
		} catch (CommonRunException e) {
			data.put("msg", e.getMessage());
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			data.put("msg", "后台异常，请联系管理员！");
			setResponse(response, -1, "后台异常，请联系管理员！");
			e.printStackTrace();
		}
		response.put("data", data);
		return response;
	}
	
	/**
	 * 获取当前账号下该学生的成绩
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStuScoreDetail", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStuScoreDetail(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		
		return response;
	}
	
	/**
	 * 获取所有学生成绩列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getAllScoreList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllScoreList(HttpServletRequest req, @RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
	
		return response;
	}
}
