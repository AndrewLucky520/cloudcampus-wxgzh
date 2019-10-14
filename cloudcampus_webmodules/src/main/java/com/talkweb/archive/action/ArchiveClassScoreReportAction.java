package com.talkweb.archive.action;

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
import com.talkweb.archive.service.ArchiveClassScoreReportService;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;

@Controller
@RequestMapping("/scoremanage/scoreReport/")
public class ArchiveClassScoreReportAction  extends BaseAction{
	@Autowired
	private ArchiveClassScoreReportService classScoreReportService;

	@RequestMapping(value = "getTeacherScoreResultTotalAnalysisList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherScoreOneInThreeRateReportData(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			 
			String xnxq = request.getString("termInfoId");
			String teacherId = request.getString("teacherId");
			if (StringUtils.isBlank(teacherId)) {
				teacherId = String.valueOf((Long)req.getSession().getAttribute("accountId"));
			}
			if( StringUtils.isBlank(xnxq)  ) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("teacherId", teacherId);

			List<JSONObject> list = classScoreReportService.produceTeacherScoreOnInThreeReportData(getSchool(req, xnxq), params);
			response.put("data", list);
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
	
	
	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	
	
}
