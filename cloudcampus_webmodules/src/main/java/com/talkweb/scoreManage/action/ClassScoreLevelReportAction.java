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
import com.talkweb.scoreManage.service.ClassScoreLevelReportService;

/**
 * 等第-班级成绩分析表
 * 
 * @author zxy
 *
 */
@Controller
public class ClassScoreLevelReportAction extends BaseAction {
	@Autowired
	private ClassScoreLevelReportService classScoreLevelReportService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	/**
	 * 科目等级统计表
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/scoremanage1/scoreReport/getLevelSubjectStatisTabList.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLevelSubjectStatisTabList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");// 学年学期
			String nj = request.getString("usedGradeId");// 使用年级，多个年级用逗号分隔
			String kslc = request.getString("examId");// 考试代码
			String bhStr = request.getString("classId");// 班级，多个班级用逗号分隔
			String kmdmStr = request.getString("subjectId");// 科目
			
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc) 
					|| StringUtils.isBlank(bhStr) || StringUtils.isBlank(kmdmStr)) {
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
			
			response.putAll(classScoreLevelReportService.getLevelSubjectStatisTabList(params));
			
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * 等第人数统计表 head:等级序列[序列数>6：取前面5段+最后一段，序列数<=6：全部依次取出]+排名区间
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/scoremanage1/scoreReport/getLeveStudentNumStatisTabList.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLeveStudentNumStatisTabList(@RequestBody JSONObject request, HttpServletRequest req,
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
			params.put("subjectType", request.get("subjectType"));
			
			response.putAll(classScoreLevelReportService.getLeveStudentNumStatisTabList(params));
			
		} catch (CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch (Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}
}
