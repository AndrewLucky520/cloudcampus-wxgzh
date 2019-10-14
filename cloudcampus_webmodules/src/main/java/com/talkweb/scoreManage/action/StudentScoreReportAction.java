package com.talkweb.scoreManage.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.talkweb.scoreManage.service.StudentScoreReportService;

/**
 * @ClassName ScoreLessionSetAction
 * @author wy
 * @Desc 学生成绩表
 * @date 2015年4月8日
 */
@Controller
@RequestMapping("/scoremanage1/scoreReport/")
public class StudentScoreReportAction extends BaseAction {
	@Autowired
	private StudentScoreReportService studentScoreReport;

	public void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 
	 * 14. 学生成绩跟踪表
	 * 
	 * @author 魏春林
	 * @param req
	 * @param res
	 * @return
	 *
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容)
	 *         </p>
	 */
	@RequestMapping(value = "getStudentScoreResultTrackList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentScoreResultTrackList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc) 
					|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			String xmxh = request.getString("stdNumOrName");
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("xmxh", xmxh == null ? "" : xmxh);
			
			response.putAll(studentScoreReport.getStudentScoreResultTrackList(params));
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		return response;
	}
}
