package com.talkweb.scoreManage.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.ScoreManageService;

/**
 * @ClassName ScoreLessionSetAction
 * @author wy
 * @Desc 成绩分析 - 科目设置
 * @date 2015年4月8日
 */
@Controller
@RequestMapping("/scoremanage1/setting/")
public class ScoreLessionSetAction extends BaseAction {

	@Autowired
	private ScoreManageService scoreService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 获取统计科目与满分列表 no
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getStatSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStatSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");

			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xnxq", xnxq);
			params.put("nj", synj);
			params.put("xxdm", getXxdm(req));
			params.put("bmfz", bmfz);
			params.put("school", getSchool(req, xnxq));

			response.putAll(scoreService.getStatSubjectList(params));

			setResponse(response, 0, "保存成功");
			response.getJSONObject("data").put("msg", "保存成功");
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
	 * 成绩分析--保存统计科目与满分列表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "updateStatSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateStatSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");
			String kmmf = request.getString("subjectFullScore");
			String termInfoId = request.getString("termInfoId");
			if (StringUtils.isBlank(kmmf) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(bmfz)
					|| StringUtils.isBlank(termInfoId) || StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			params.put("bmfz", bmfz);
			params.put("nj", synj);
			params.put("bjfzList", JSON.parseArray(kmmf));

			scoreService.updateStatSubjectList(params);

			setResponse(response, 0, "保存成功");
			response.getJSONObject("data").put("msg", "保存成功");
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
	 * 获取合并科目 no需从深圳获取科目
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "getMergedSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMergedSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String xnxq = request.getString("termInfoId");
			String kslcdm = request.getString("examId");
			String synj = request.getString("usedGrade");
			String bmfz = request.getString("asgId");

			if (StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslcdm) || StringUtils.isBlank(synj)
					|| StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslcdm", kslcdm);
			params.put("kslc", kslcdm);
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("bmfz", bmfz);
			params.put("nj", synj);
			params.put("school", getSchool(req, xnxq));

			response.putAll(scoreService.getMergedSubjectList(params));

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
	 * 成绩设置--新增合并科目
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "addMergedSubject", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addMergedSubject(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String synj = request.getString("usedGrade");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgId");
			String kmdm = request.getString("chsId");
			String subjectIds = request.getString("subjectIds");
			String termInfoId = request.getString("termInfoId");

			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(kslc) || StringUtils.isBlank(subjectIds)
					|| StringUtils.isBlank(bmfz) || StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			if(StringUtils.isBlank(kmdm)) {
				throw new CommonRunException(-1, "综合科目为空，请选择中和科目！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			params.put("nj", synj);
			params.put("fzdm", bmfz);
			params.put("kmdm", kmdm);
			params.put("dykm", subjectIds);
			scoreService.addMergerSubject(params);

			setResponse(response, 0, "保存成功");
			response.getJSONObject("data").put("msg", "保存成功");
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
	 * 删除合并科目
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "delMergedSubject", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delMergedSubject(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletRequest res) {
		JSONObject response = new JSONObject();
		response.put("data", new JSONObject());
		try {
			String synj = request.getString("usedGrade");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgId");
			String kmdm = request.getString("chsId");
			String termInfoId = request.getString("termInfoId");

			if (StringUtils.isBlank(termInfoId) || StringUtils.isBlank(kslc) || StringUtils.isBlank(bmfz)
					|| StringUtils.isBlank(synj)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("nj", synj);
			params.put("fzdm", bmfz);
			params.put("kmdm", kmdm);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", termInfoId);
			
			scoreService.delMergerSubject(params);

			setResponse(response, 0, "删除成功");
			response.getJSONObject("data").put("msg", "删除成功");
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
