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

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.scoreManage.service.ClassScoreReportService;
import com.talkweb.scoreManage.service.StudentLevelReportService;

/**
 * @ClassName ScoreLessionSetAction
 * @author wy
 * @Desc 学生成绩表
 * @date 2015年4月8日
 */
@Controller
@RequestMapping("/scoremanage1/scoreReport")
public class StudentLevelReportAction extends BaseAction {
	
	@Autowired
	private StudentLevelReportService studentLevelReportService;

	@Autowired
	private ClassScoreReportService classScoreReportService;

	private void setResponse(JSONObject response, int code, String msg) {
		response.put("code", code);
		response.put("msg", msg);
	}
	
	/**
	 * 
	 * 5. 学生等第成绩明细表
	 *
	 * @author 魏春林
	 *
	 * @request req
	 * @request res
	 * @return
	 *
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容)
	 *         </p>
	 */
	@RequestMapping(value = "getStudentLevelScoreResultDetailTab", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentLevelScoreResultDetailTab(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bmfz = request.getString("asgID");
			String bhStr = request.getString("classId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bmfz) || StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("nj", nj);
			params.put("bmfz", bmfz);
			params.put("bhStr", bhStr);
			
			params.put("xmxh", request.get("stdNumOrName"));
			params.put("topXRank", request.get("topXTotalGrade"));
			params.put("lastXRank", request.get("lastXTotalGrade"));
			params.put("topXTotalPer", request.get("topXTotalPer"));
			params.put("lastXTotalPer", request.get("lastXTotalPer"));
			params.put("qa", request.get("allLevelA"));
			
			response.putAll(studentLevelReportService.getStudentLevelScoreResultDetailTab(params));
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/**
	 * 
	 * 6. 学生等第薄弱科目表
	 *
	 * @author 魏春林
	 *
	 * @request req
	 * @request res
	 * @return
	 *
	 *         <p>
	 *         修改历史 ：(修改人，修改时间，修改原因/内容)
	 *         </p>
	 */
	@RequestMapping(value = "getStudentLevelWeakSubjectList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentLevelWeakSubjectList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String kslc = request.getString("examId");
			String nj = request.getString("usedGradeId");
			String bhStr = request.getString("classId");
			String kmdmStr = request.getString("subjectId");
			
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(kslc) || StringUtils.isBlank(nj)
					|| StringUtils.isBlank(bhStr) || StringUtils.isBlank(kmdmStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("xxdm", getXxdm(req));
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("kmdmStr", kmdmStr);
			
			response.putAll(studentLevelReportService.getStudentLevelWeakSubjectList(params));
			
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			e.printStackTrace();
			setResponse(response, -1, "服务器异常，请联系管理员！");
		}
		return response;
	}

	/****
	 * 7. 等第总分析表
	 * 
	 * @request request
	 * @time 2015-05-08
	 * @return
	 */
	@RequestMapping(value = "getLevelTotalAnalysisTabList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScoreAllStatisReportData(@RequestBody JSONObject request, HttpServletRequest req) {
		JSONObject response = new JSONObject();
		try {
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String fzdmStr = request.getString("classGroupId");
			String bhStr = request.getString("classId");
			String kmdmStr = request.getString("subjectId");
			
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc) 
					|| StringUtils.isBlank(fzdmStr) || StringUtils.isBlank(bhStr) || StringUtils.isBlank(kmdmStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}

			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("kslc", kslc);
			params.put("kslcdm", kslc);
			params.put("xnxq", xnxq);
			params.put("nj", nj);
			
			params.put("fzdmStr", fzdmStr);
			params.put("bhStr", bhStr);
			params.put("kmdmStr", kmdmStr);
			
			response.putAll(classScoreReportService.produceLevelAllStatisReportData(getSchool(req, xnxq),
					params));
			
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
	 * 13. 等第科目各Ａ及前１/3名人数统计表
	 * 
	 * @request request
	 * @request res
	 * @return
	 */
	@RequestMapping(value = "getLevelSubjectAOneThirdStatisList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLevelSubjectAOneThirdStatisList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			// 页面参数
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xxdm", getXxdm(req));
			params.put("xnxq", xnxq);
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			
			response.putAll(studentLevelReportService.getLevelSubjectAOneThirdStatisList(params));
			
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
	 * 等第总分统计表
	 * 
	 * @request request
	 * @request res
	 * @return
	 */
	@RequestMapping(value = "getLevelTotalScoreStaticList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLevelTotalScoreStaticList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			// 页面参数
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			
			response.putAll(studentLevelReportService.getLevelTotalScoreStaticList(params));
			
			setResponse(response, 1, "");
		} catch(CommonRunException e) {
			setResponse(response, e.getCode(), e.getMessage());
		} catch(Exception e) {
			setResponse(response, -1, "服务器异常，请联系管理员！");
			e.printStackTrace();
		}
		
		return response;
	}

	private List<JSONObject> filterDdzftjbList(List<JSONObject> sourceList, String bhs, String djxl) {
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		for (JSONObject dataRow : sourceList) {
			if (dataRow.getString("bhs").equals(bhs) && dataRow.getString("djxl").equals(djxl)) {
				dataList.add(dataRow);
			}
		}
		return dataList;
	}

	/**
	 * 10. 等第各A统计表
	 * 
	 * @request request
	 * @request res
	 * @return
	 */
	@RequestMapping(value = "getLevelEveryAStatisTabList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLevelEveryAStatisTabList(@RequestBody JSONObject request, HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject response = new JSONObject();
		try {
			// 页面参数
			String xnxq = request.getString("termInfoId");
			String nj = request.getString("usedGradeId");
			String kslc = request.getString("examId");
			String bhStr = request.getString("classId");
			if(StringUtils.isBlank(xnxq) || StringUtils.isBlank(nj) || StringUtils.isBlank(kslc)
					|| StringUtils.isBlank(bhStr)) {
				throw new CommonRunException(-1, "参数传递异常，请联系管理员！");
			}
			
			JSONObject params = new JSONObject();
			params.put("xnxq", xnxq);
			params.put("xxdm", getXxdm(req));
			params.put("kslcdm", kslc);
			params.put("kslc", kslc);
			params.put("nj", nj);
			params.put("bhStr", bhStr);
			params.put("subjectType", request.get("subjectType"));
			
			response.putAll(studentLevelReportService.getLevelEveryAStatisTabList(params));
			
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
