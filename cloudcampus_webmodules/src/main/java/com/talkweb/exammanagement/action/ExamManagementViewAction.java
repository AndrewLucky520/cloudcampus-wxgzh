package com.talkweb.exammanagement.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.service.ExamManagementExamPlanService;
import com.talkweb.exammanagement.service.ExamManagementViewService;

@Controller
@RequestMapping("/examManagement/view")
public class ExamManagementViewAction extends BaseAction {

	Logger logger = LoggerFactory.getLogger(ExamManagementAction.class);
	@Autowired
	private ExamManagementViewService examManagementViewService;
	@Autowired
	private ExamManagementExamPlanService examManagementExamPlanService;
	@Autowired
	private AllCommonDataService allCommonDataService;

	private final String errorMsg = "操作异常，请联系管理员！";

	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	@RequestMapping(value = "/getExamPlaceInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlaceInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param = new HashMap<String, Object>();

		String examManagementId = request.getString("examManagementId");

		String termInfo = request.getString("termInfo");

		String examSubjectGroupIds = request.getString("examSubjectGroupIds");
		
		String subjectGroupName = request.getString("subjectGroupName");
		
		String examPlanId = request.getString("examPlanId");
		
		List<String> examSubjectGroupId = Arrays.asList(examSubjectGroupIds
				.split(","));

		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examPlanId", examPlanId);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examManagementId", examManagementId);
		param.put("subjectGroupName", subjectGroupName);

		response.put("code", 1);
		response.put("msg", "");
		try {
			response.put("data",
					examManagementViewService.getExamPlaceInfo(param));
		} catch (CommonRunException e) {
			List<JSONObject> data=new ArrayList<JSONObject>();
			response.put("data",data);
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	@RequestMapping(value = "/getExamPlanInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getExamPlanInfo(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();

		response.put("code", 1);
		response.put("msg", "");
		try {
			request.put("schoolId", getXxdm(req));
			response.put("data",
					examManagementExamPlanService.getExamPlanList(request));
		} catch (CommonRunException e) {
			List<JSONObject> data=new ArrayList<JSONObject>();
			response.put("data",data);
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 考场桌角条
	 * 
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getTableCornerList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTableCornerList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param = new HashMap<String, Object>();

		String examManagementId = request.getString("examManagementId");

		String termInfo = request.getString("termInfo");

		String examPlanId = request.getString("examPlanId");

		String startExamPlaceCode = request.getString("startExamPlaceCode");

		String endExamPlaceCode = request.getString("endExamPlaceCode");

		int curPage = request.getIntValue("curPage");

		int curPageCount =request.getIntValue("curPageCount");

		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examPlanId", examPlanId);
		param.put("startExamPlaceCode", Integer.valueOf(startExamPlaceCode));
		param.put("endExamPlaceCode", Integer.valueOf(endExamPlaceCode));
		param.put("pageStart", (curPage - 1) * curPageCount);
		param.put("pageEnd", curPage * curPageCount);
		param.put("curPageCount", curPageCount);
		response.put("code", 1);
		response.put("msg", "");
		School school = getSchool(req, termInfo);
		param.put("school", school);
		try {
			JSONObject data = examManagementViewService
					.getTableCornerList(param);
			response.put("curPage", curPage);
			response.put("totalPage", data.get("totalPage"));
			response.put("totalCounts", data.get("totalCount"));
			response.put("data", data.get("data"));

		} catch (CommonRunException e) {
			List<JSONObject> data=new ArrayList<JSONObject>();
			response.put("data",data);
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 2.8.4班级考场对照表
	 * 
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getTClassAndExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTClassAndExamPlace(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param = new HashMap<String, Object>();

		String examManagementId = request.getString("examManagementId");

		String termInfo = request.getString("termInfo");

		String examSubjectGroupIds = request.getString("examSubjectGroupIds");

		String tClassId = request.getString("tClassId");

		String examPlanId = request.getString("examPlanId");

		List<String> examSubjectGroupId = Arrays.asList(examSubjectGroupIds
				.split(","));

		List<String> tClassIds = Arrays.asList(tClassId.split(","));

		String name = request.getString("studName");

		int curPage = request.getIntValue("curPage");

		int curPageCount = request.getIntValue("curPageCount");
		String subjectGroupName = request.getString("subjectGroupName");
		
		param.put("subjectGroupName", subjectGroupName);
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("tClassId", tClassIds);
		param.put("curPage", curPage);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examManagementId", examManagementId);
		param.put("examPlanId", examPlanId);
		param.put("pageStart", (curPage - 1) * curPageCount);
		param.put("pageEnd", curPage * curPageCount);
		param.put("curPageCount", curPageCount);
		param.put("name", name);
		try {
			JSONObject data = examManagementViewService
					.getTClassAndExamPlace(param);
			response.put("code", 1);
			response.put("msg", "");
			response.put("curPage", curPage);
			response.put("totalPage", data.get("totalPage"));
			response.put("totalCounts", data.get("totalCount"));
			response.put("data", data.get("data"));
		} catch (CommonRunException e) {
			List<JSONObject> data=new ArrayList<JSONObject>();
			response.put("data",data);
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 2.8.5考场学生名单
	 * 
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/getStudsAndExamPlace", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudsAndExamPlace(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		Map<String, Object> param = new HashMap<String, Object>();

		String examManagementId = request.getString("examManagementId");

		String termInfo = request.getString("termInfo");

		String examSubjectGroupIds = request.getString("examSubjectGroupIds");

		String examPlaceId = request.getString("examPlaceId");

		String examPlanId = request.getString("examPlanId");

		List<String> examSubjectGroupId = Arrays.asList(examSubjectGroupIds
				.split(","));

		List<String> examPlaceIds = Arrays.asList(examPlaceId.split(","));

		String name = request.getString("studName");

		int curPage = request.getIntValue("curPage");

		int curPageCount = request.getIntValue("curPageCount");
		String subjectGroupName = request.getString("subjectGroupName");
		
		
		param.put("subjectGroupName", subjectGroupName);
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examPlaceId", examPlaceIds);
		param.put("examSubjectGroupId", examSubjectGroupId);
		param.put("examManagementId", examManagementId);
		param.put("examPlanId", examPlanId);
		param.put("curPage", curPage);
		param.put("pageStart", (curPage - 1) * curPageCount);
		param.put("pageEnd", curPage * curPageCount);
		param.put("curPageCount", curPageCount);
		param.put("name", name);

		try {
			JSONObject data = examManagementViewService
					.getStudsAndExamPlace(param);
			response.put("curPage", curPage);
			response.put("totalPage", data.get("totalPage"));
			response.put("totalCounts", data.get("totalCount"));
			response.put("data", data.get("data"));
			response.put("code", 1);
			response.put("msg", "");
		} catch (CommonRunException e) {
			List<JSONObject> data=new ArrayList<JSONObject>();
			response.put("data",data);
			response.put("code", e.getCode());
			response.put("msg", e.getMessage());
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", errorMsg);
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	private static final String[] field = { "xh","subjectName", "testNumber",
			"studName", "tClassName" };
	private static final String[] title = { "序号","考试科目", "考号", "姓名", "班级" };

	/**
	 * 后三个表导出
	 * 
	 * @param req
	 * @param res
	 * @throws Exception
	 */
	@RequestMapping(value = "/exportTableCornerList")
	@ResponseBody
	public void exportTableCornerList(HttpServletRequest request,
			HttpServletResponse res) throws Exception {

		JSONArray excelHeads = JSONArray.parseArray(request
				.getParameter("excelHead"));
		JSONArray excelParam = JSONArray.parseArray(request
				.getParameter("excelParam"));
		JSONObject para = excelParam.getJSONObject(0);

		Map<String, Object> param = new HashMap<String, Object>();

		String examManagementId = para.getString("examManagementId");

		String termInfo = para.getString("termInfo");

		String examPlanId = para.getString("examPlanId");

		String startExamPlaceCode = para.getString("startExamPlaceCode");

		String endExamPlaceCode = para.getString("endExamPlaceCode");

		String examSubjectGroupIds = para.getString("examSubjectGroupIds");

		String examPlaceId = para.getString("examPlaceId");

		String tClassId = para.getString("tClassId");

		String studName = para.getString("studName");
		
		String subjectGroupName=para.getString("subjectGroupName");
		
		String type=para.getString("type");//1.桌角条 ，2.班级对照表，2.考场考生名单

		if (examSubjectGroupIds != null) {

			List<String> examSubjectGroupId = Arrays.asList(examSubjectGroupIds
					.split(","));
			param.put("examSubjectGroupId", examSubjectGroupId);
		}

		if (examPlaceId != null) {
			List<String> examPlaceIds = Arrays.asList(examPlaceId.split(","));
			param.put("examPlaceId", examPlaceIds);
		}

		if (tClassId != null) {
			List<String> tClassIds = Arrays.asList(tClassId.split(","));
			param.put("tClassId", tClassIds);
		}
		
		param.put("subjectGroupName", subjectGroupName);
		String schoolId = getXxdm(request);
		param.put("pageStart", "");
		param.put("pageEnd", "");
		param.put("curPageCount", 0);
		School school = getSchool(request, termInfo);
		param.put("school", school);
		param.put("export", "export");
		param.put("name", studName);
		param.put("schoolId", schoolId);
		param.put("termInfo", termInfo);
		param.put("examManagementId", examManagementId);
		param.put("examPlanId", examPlanId);
		param.put("startExamPlaceCode", startExamPlaceCode);
		param.put("endExamPlaceCode", endExamPlaceCode);
		String fileName = request.getParameter("fileName");
		
		JSONArray darray = new JSONArray();
		switch (type) {
		case "1":
			JSONArray parent = new JSONArray();
			JSONArray d = new JSONArray();
			parent.add(d);
			for (int i = 0; i < field.length; i++) {
				JSONObject col = new JSONObject();
				col.put("field", field[i]);
				col.put("title", title[i]);
				col.put("align", "center");
				d.add(col);
			}
			JSONObject data = examManagementViewService.getTableCornerList(param);
			List<JSONObject> dl = (List<JSONObject>) data.get("data");
			for (JSONObject da : dl) {
				darray.add(da);
			}
			ExcelTool.exportExcelWithTableByZJT(darray, parent, fileName, null, request,
					res);
			break;
		case "2":
			JSONObject classdata = examManagementViewService.getTClassAndExamPlace(param);
			List<JSONObject> clalist = (List<JSONObject>) classdata.get("data");
			for (JSONObject da : clalist) {
				darray.add(da);
			}
			ExcelTool.exportExcelWithTable(darray, excelHeads, fileName, null, request,
					res);
			break;
		case "3":
			JSONObject studata = examManagementViewService.getStudsAndExamPlace(param);
			List<JSONObject> stulist = (List<JSONObject>) studata.get("data");
			for (JSONObject da : stulist) {
				darray.add(da);
			}
			ExcelTool.exportExcelWithTable(darray, excelHeads, fileName, null, request,
					res);
			break;
		}
		
	}

}
