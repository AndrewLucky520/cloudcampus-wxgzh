package com.talkweb.scoreManage.action;

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
import com.talkweb.scoreManage.business.ClassScoreInParam;
import com.talkweb.scoreManage.service.ClassScoreReportService;

/**
 * @ClassName ClassScoreReportAction
 * @author gyb
 * @Desc 班级成绩分析表
 * @date 2015年4月28日
 */
@Controller
@RequestMapping("/scoremanage1/scoreReport/")
public class ClassScoreReportAction extends BaseAction {
	@Autowired
	private ClassScoreReportService classScoreReportService;
	
	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}

	/**
	 * 功能描述： 成绩全指标分析表
	 * 
	 * @author guoyuanbing
	 *         <p>
	 *         创建日期 ：2015年4月29日
	 * @return
	 */
	@RequestMapping(value = "getqzbfxbList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassScoreReportList(ClassScoreInParam param) {
		// 1.param参数，部分字段是以逗号分隔的数组，因此在这里根据逗号分隔字段，循环每次去调用函数获取数据
		return classScoreReportService.produceScoreAllIndexDataModel(param);
	}

	/****
	 * 3. 分数成绩总分析表
	 * 
	 * @param param
	 * @time 2015-05-04
	 * @return
	 */
	@RequestMapping(value = "getScoreResultTotalAnalysisList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreOneInThreeRateReportData(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String nj = request.getString("usedGradeId");
			String xnxq = request.getString("termInfoId");
			
			String kmdmStr = request.getString("subjectId");
			String bhStr = request.getString("classId");
			String fzdmStr = request.getString("classGroupId");
			String kslc = request.getString("examId");
			
			if(StringUtils.isBlank(nj) || StringUtils.isBlank(xnxq) || StringUtils.isBlank(kmdmStr)
					|| StringUtils.isBlank(bhStr) || StringUtils.isBlank(fzdmStr) || StringUtils.isBlank(kslc)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			params.put("kmdmStr", kmdmStr);
			params.put("bhStr", bhStr);
			params.put("fzdmStr", fzdmStr);

			response.putAll(classScoreReportService.produceScoreOnInThreeReportData(getSchool(req, xnxq), params));
			
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
	 * 4.历次分数成绩对比表
	 * 
	 * @param param
	 * @time 2015-05-07
	 * @return
	 */
	@RequestMapping(value = "getAllPreviousScoreResultCompareList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassCompareTableData(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String nj = request.getString("usedGradeId");
			String kmdmStr = request.getString("subjectId");
			String xnxq = request.getString("termInfoId");
			if(StringUtils.isBlank(nj) || StringUtils.isBlank(kmdmStr) || StringUtils.isBlank(xnxq)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("kmdmStr", kmdmStr);
			params.put("nj", nj);
			params.put("termInfoRange", request.get("termInfoRange"));
			
			response.putAll(classScoreReportService.produceClassScoreData(getSchool(req, xnxq), params));
			
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
