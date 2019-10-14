package com.talkweb.scoreManage.action;

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
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.StudentScoreReportService;

/**
 * @ClassName ScoreLessionSetAction
 * @author zxy
 * @Desc 分数段分布表
 * @date 2015年4月27日
 */
@Controller
public class ScoreDistributionReportAction extends BaseAction {
	@Autowired
	private StudentScoreReportService studentScoreReport;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	/**
	 * 总分分数段分布表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/scoremanage1/scoreReport/getScoreSectionTotalList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreSectionTotalList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			String bmfz = request.getString("asgID");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bhStr) || StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("bmfz", bmfz);
			
			response.putAll(studentScoreReport.getScoreSectionTotalList(params));
			
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * 单科分数段分布表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/scoremanage1/scoreReport/getScoreSectionSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreSectionSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kmdmStr = request.getString("subjectId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			String bmfz = request.getString("asgID");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(kmdmStr) || StringUtils.isBlank(bhStr) || StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("kmdmStr", kmdmStr);
			params.put("bmfz", bmfz);
			
			response.putAll(studentScoreReport.getScoreSectionSubjectList(params));
			
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}

	/****
	 * 18. 名次段分布表
	 * 
	 * @param parm
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/scoremanage1/scoreReport/getRankSectionList.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRankSectionList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kmdm = request.getString("subjectId");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(kmdm) || StringUtils.isBlank(bmfz)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("kmdm", kmdm);
			params.put("bmfz", bmfz);
			
			response.putAll(studentScoreReport.getRankSectionList(params));
			
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
}
