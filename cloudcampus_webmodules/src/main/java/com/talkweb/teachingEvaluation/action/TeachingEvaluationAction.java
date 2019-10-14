package com.talkweb.teachingEvaluation.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/")
@RestController
public class TeachingEvaluationAction {

	Logger logger = LoggerFactory.getLogger(TeachingEvaluationAction.class);
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private AllCommonDataService allCommonDataService;

	String rootPath = SplitUtil.getRootPath("evaluation.url");

	@RequestMapping("/evalBasic/queryBasicData")
	@ResponseBody
	public JSONObject queryBasicData(@RequestBody JSONObject param) {
		return postAction(param, "evalBasic/queryBasicData");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByTarget")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByTarget(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByTarget");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByClassMaster")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByClassMaster(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByClassMaster");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByClassTeach")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByClassTeach(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByClassTeach");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByClassGradeLeader")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByClassGradeLeader(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByClassGradeLeader");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByClassQuestionnaire")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByClassQuestionnaire(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByClassQuestionnaire");
	}

	@RequestMapping("/evalResult/queryObjectTiveQuesResultByTeacher")
	@ResponseBody
	public JSONObject queryObjectTiveQuesResultByTeacher(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/queryObjectTiveQuesResultByTeacher");
	}

	@RequestMapping("/evalResult/querySubjectTiveQuesResult")
	@ResponseBody
	public JSONObject querySubjectTiveQuesResult(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/querySubjectTiveQuesResult");
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/evalResult/queryRecordSubjectByExport")
	@ResponseBody
	public void queryRecordSubjectByExport(HttpServletRequest req, HttpServletResponse res) {
		try {
			JSONObject request = (JSONObject) JSONObject.parse(req.getParameter("excelData"));
			request.put("isExport", true);
			String targetBody = request.getString("targetType");

			JSONObject response = querySubjectTiveQuesResult(request);
			JSONObject evalObj = response.getJSONObject("evaluate");
			List<JSONObject> subjectTive = (List<JSONObject>) response.get("data");
			List<JSONObject> jsonHead = joinJsonHead(targetBody, evalObj);
			String excelName = evalObj.getString("evalName") + "_" + response.getString("baseName");
			// 拼装Excel表格
			ExcelTool.exportLargeDataExcel(subjectTive, jsonHead, excelName, res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/evalResult/deleteSubjectTiveQuesResult")
	@ResponseBody
	public JSONObject deleteSubjectTiveQuesResult(@RequestBody JSONObject param) {
		return postAction(param, "evalResult/deleteSubjectTiveQuesResult");
	}

	@RequestMapping("/evalStu/queryEvalForStudent")
	@ResponseBody
	public JSONObject queryEvalForStudent(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evalStu/queryEvalForStudent");
	}

	@RequestMapping("/evalStu/queryEvalForStudentByOpt")
	@ResponseBody
	public JSONObject queryEvalForStudentByOpt(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evalStu/queryEvalForStudentByOpt");
	}

	@RequestMapping("/evalStu/submitEvalForStudent")
	@ResponseBody
	public JSONObject submitEvalForStudent(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evalStu/submitEvalForStudent");
	}

	@RequestMapping("/evalStu/queryEvalForStudentAnonymous")
	@ResponseBody
	public JSONObject queryEvalForStudentAnonymous(@RequestBody JSONObject param) {
		JSONObject user = (JSONObject) request.getSession().getAttribute("nmAccInfo");
		JSONObject evaluate = (JSONObject) request.getSession().getAttribute("evaluate");

		param.putAll(user);
		param.putAll(evaluate);

		return postAction(param, "evalStu/queryEvalForStudentAnonymous");
	}

	@RequestMapping("/evalStu/submitEvalForStudentAnonymous")
	@ResponseBody
	public JSONObject submitEvalForStudentAnonymous(@RequestBody JSONObject param) {
		JSONObject user = (JSONObject) request.getSession().getAttribute("nmAccInfo");
		JSONObject evaluate = (JSONObject) request.getSession().getAttribute("evaluate");

		param.putAll(user);
		param.putAll(evaluate);

		return postAction(param, "evalStu/submitEvalForStudentAnonymous");
	}

	@RequestMapping("/evalStu/queryEvalForStudentOptAnonymous")
	@ResponseBody
	public JSONObject queryEvalForStudentOptAnonymous(@RequestBody JSONObject param) {
		JSONObject user = (JSONObject) request.getSession().getAttribute("nmAccInfo");
		JSONObject evaluate = (JSONObject) request.getSession().getAttribute("evaluate");

		param.putAll(user);
		param.putAll(evaluate);

		return postAction(param, "evalStu/queryEvalForStudentOptAnonymous");
	}

	@RequestMapping("/evalStuLogin/nmStuLogin")
	@ResponseBody
	public JSONObject nmStuLogin(@RequestBody JSONObject param) {
		JSONObject ret = postAction(param, "evalStuLogin/nmStuLogin");
		if (ret != null) {
			request.getSession().setAttribute("nmAccInfo", ret.get("nmAccInfo"));
			request.getSession().setAttribute("evaluate", ret.get("evaluate"));
		}
		return ret;
	}

	@RequestMapping("/evalTeach/queryEvalForTeacher")
	@ResponseBody
	public JSONObject queryEvalForTeacher(@RequestBody JSONObject param) {
		JSONObject result = new JSONObject();
		try {
			getGradeList(param);
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", "操作异常，请联系管理员！");
			e.printStackTrace();
			return result;
		}
		return postAction(param, "evalTeach/queryEvalForTeacher");
	}

	@RequestMapping("/evalTeach/queryEvalForTeacherByDetails")
	@ResponseBody
	public JSONObject queryEvalForTeacherByDetails(@RequestBody JSONObject param) {
		return postAction(param, "evalTeach/queryEvalForTeacherByDetails");
	}

	@RequestMapping("/evaluate/startAnalysisEval")
	@ResponseBody
	public JSONObject startAnalysisEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/startAnalysisEval");
	}

	@RequestMapping("/evaluate/queryAnalysisEval")
	@ResponseBody
	public JSONObject queryAnalysisEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryAnalysisEval");
	}

	@RequestMapping("/evaluate/queryEvaluateList")
	@ResponseBody
	public JSONObject queryEvaluateList(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryEvaluateList");
	}

	@RequestMapping("/evaluate/createEval")
	@ResponseBody
	public JSONObject createEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/createEval");
	}

	@RequestMapping("/evaluate/updateEvalName")
	@ResponseBody
	public JSONObject updateEvalName(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateEvalName");
	}

	@RequestMapping("/evaluate/updateEvalDate")
	@ResponseBody
	public JSONObject updateEvalDate(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateEvalDate");
	}

	@RequestMapping("/evaluate/deleteEvalByEvalId")
	@ResponseBody
	public JSONObject deleteEvalByEvalId(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/deleteEvalByEvalId");
	}

	@RequestMapping("/evaluate/evalRange")
	@ResponseBody
	public JSONObject evalRange(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/evalRange");
	}

	@RequestMapping("/evaluate/updateEvalRange")
	@ResponseBody
	public JSONObject updateEvalRange(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateEvalRange");
	}

	@RequestMapping("/evaluate/evalSubject")
	@ResponseBody
	public JSONObject evalSubject(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/evalSubject");
	}

	@RequestMapping("/evaluate/updateEvalSubject")
	@ResponseBody
	public JSONObject updateEvalSubject(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateEvalSubject");
	}

	@RequestMapping("/evaluate/queryTeachingRelationship")
	@ResponseBody
	public JSONObject queryTeachingRelationship(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryTeachingRelationship");
	}

	@RequestMapping("/evaluate/queryTeacherInfo")
	@ResponseBody
	public JSONObject queryTeacherInfo(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryTeacherInfo");
	}

	@RequestMapping("/evaluate/updateTeachingRelationship")
	@ResponseBody
	public JSONObject updateTeachingRelationship(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateTeachingRelationship");
	}

	@RequestMapping("/evaluate/updateEvalType")
	@ResponseBody
	public JSONObject updateEvalType(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/updateEvalType");
	}

	@RequestMapping("/evaluate/createAnonymousByEval")
	@ResponseBody
	public JSONObject createAnonymousByEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/createAnonymousByEval");
	}

	@RequestMapping("/evaluate/target/queryTargetForEval")
	@ResponseBody
	public JSONObject queryTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/queryTargetForEval");
	}

	@RequestMapping("/evaluate/target/createTargetForEval")
	@ResponseBody
	public JSONObject createTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/createTargetForEval");
	}

	@RequestMapping("/evaluate/target/updateTargetForEval")
	@ResponseBody
	public JSONObject updateTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate//target/updateTargetForEval");
	}

	@RequestMapping("/evaluate/target/moveTargetForEval")
	@ResponseBody
	public JSONObject moveTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/moveTargetForEval");
	}

	@RequestMapping("/evaluate/target/deleteTargetForEval")
	@ResponseBody
	public JSONObject deleteTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/deleteTargetForEval");
	}

	@RequestMapping("/evaluate/queryEvaluateListByName")
	@ResponseBody
	public JSONObject queryEvaluateListByName(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryEvaluateListByName");
	}

	@RequestMapping("/evaluate/target/copyTargetForEval")
	@ResponseBody
	public JSONObject copyTargetForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/copyTargetForEval");
	}

	@RequestMapping("/evaluate/target/createTargetValueForEval")
	@ResponseBody
	public JSONObject createTargetValueForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/createTargetValueForEval");
	}

	@RequestMapping("/evaluate/target/queryTargetValueForEval")
	@ResponseBody
	public JSONObject queryTargetValueForEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/queryTargetValueForEval");
	}

	@RequestMapping("/evaluate/queryNotEvalForStudent")
	@ResponseBody
	public JSONObject queryNotEvalForStudent(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryNotEvalForStudent");
	}

	@RequestMapping("/evaluate/queryAnonymousByEval")
	@ResponseBody
	public JSONObject queryAnonymousByEval(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/queryAnonymousByEval");
	}

	@RequestMapping("/evaluate/sendEvaltoTeacher")
	@ResponseBody
	public JSONObject sendEvaltoTeacher(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/sendEvaltoTeacher");
	}

	@RequestMapping("/evaluate/target/getEvaluateList")
	@ResponseBody
	public JSONObject getEvaluateList(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/getEvaluateList");
	}

	@RequestMapping("/evaluate/target/getEvalLevelOptForEvalId")
	@ResponseBody
	public JSONObject getEvalLevelOptForEvalId(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/target/getEvalLevelOptForEvalId");
	}

	@RequestMapping("/evaluate/evalRange2")
	@ResponseBody
	public JSONObject evalRange2(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/evalRange2");
	}

	@RequestMapping("/evaluate/exportObjectTiveExcel")
	@ResponseBody
	public void exportObjectTiveExcel(HttpServletRequest req, HttpServletResponse res) {
		String evalId = req.getParameter("evalId");
		String targetBody = req.getParameter("targetBody");
		String grades = req.getParameter("grades");
		String termInfo = req.getParameter("selectedSemester");
		String evalName = req.getParameter("evalName");
		JSONObject params = new JSONObject();
		params.put("evalId", evalId);
		params.put("gradeId", grades.substring(0, grades.indexOf(",")));
		params.put("targetBody", targetBody);
		params.put("termInfo", termInfo);
		JSONObject data = postAction(params, "evaluate/exportObjectTiveExcel");
		if (data != null && data.getIntValue("code") == 0) {
			JSONArray excelData = data.getJSONArray("excelData");
			JSONArray excelHeads = data.getJSONArray("excelHeads");
			ExcelTool.exportExcelWithData(excelData, excelHeads, evalName, null, req, res, true);
		} else {
			logger.error(data == null ? "" : data.getString("msg"));
		}
	}

	@RequestMapping("/evaluate/notice")
	@ResponseBody
	public JSONObject notice(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/notice");
	}

	@RequestMapping("/evaluate/app/queryTeacherEvaluateList")
	@ResponseBody
	public JSONObject queryTeacherEvaluateList(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/app/queryTeacherEvaluateList");
	}

	@RequestMapping("/evaluate/app/queryTeachingRelation")
	@ResponseBody
	public JSONObject queryTeachingRelation(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/app/queryTeachingRelation");
	}

	@RequestMapping("/evaluate/app/getEvaluateDetail")
	@ResponseBody
	public JSONObject getEvaluateDetail(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/app/getEvaluateDetail");
	}

	@RequestMapping("/evaluate/app/getsubjectiveDetail")
	@ResponseBody
	public JSONObject getsubjectiveDetail(@RequestBody JSONObject param) {
		return postAction(param, "evaluate/app/getsubjectiveDetail");
	}

	@RequestMapping("/evaluate/app/queryEvalForStudent")
	@ResponseBody
	public JSONObject queryEvalForStudentForApp(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evaluate/app/queryEvalForStudent");
	}

	@RequestMapping("/evaluate/app/getEvalTargetList")
	@ResponseBody
	public JSONObject getEvalTargetList(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evaluate/app/getEvalTargetList");
	}

	@RequestMapping("/evaluate/app/queryEvalForStudentByOpt")
	@ResponseBody
	public JSONObject queryEvalForStudentByOptForApp(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evaluate/app/queryEvalForStudentByOpt");
	}

	@RequestMapping("/evaluate/app/submitEvalForStudent")
	@ResponseBody
	public JSONObject submitEvalForStudentForApp(@RequestBody JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		return postAction(param, "evaluate/app/submitEvalForStudent");
	}

	private JSONObject postAction(JSONObject param, String action) {
		return SplitUtil.postAction(request, rootPath + action, param);
	}

	private JSONObject checkParent(JSONObject param) {
		JSONObject result = new JSONObject();
		try {
			this.accountToUser(param, false);
			User user = (User) request.getSession().getAttribute("user");
			if (user != null && T_Role.Parent.equals(user.getUserPart().getRole())) {
				if (!param.containsKey("userId")) {
					param.put("userId", user.getUserPart().getId());
				}
			}
			return null;
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", "操作异常，请联系管理员！");
			e.printStackTrace();
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	private void getGradeList(JSONObject param) {
		List<String> mangeSynjList = (List<String>) request.getSession().getAttribute("manageSynjList");
		if (mangeSynjList != null) {
			param.put("gradeList", mangeSynjList);
		}
	}

	private void accountToUser(JSONObject param, boolean isteacher) {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {//手机App端
			if (param.containsKey("accountId") && !param.containsKey("userId")) {//账号id转用户id
				String xnxq = (String) request.getSession().getAttribute("curXnxq");
				Long schoolId = getSchoolId(param);
				Long accountId = param.getLong("accountId");
				Account acct = allCommonDataService.getAccountAllById(schoolId, accountId, xnxq);
				for (User u : acct.getUsers()) {
					if (u.getUserPart().getRole().getValue() == T_Role.Parent.getValue()) {
						param.put("userId", u.getUserPart().getId());
						break;
					}
					if (u.getUserPart().getRole().getValue() == T_Role.Student.getValue()) {
						param.put("userId", u.getUserPart().getId());
						break;
					}
				}
			}
		}
	}

	private Long getSchoolId(JSONObject param) {
		School school = (School) request.getSession().getAttribute("school");
		if (school == null)
			return param.getLong("schoolId");
		return school.getId();
	}

	private List<JSONObject> joinJsonHead(String targetBody, JSONObject evalObj) {
		// targetType:1(班主任)，2（任课教师），3（年级组长），4（公共问卷）
		List<JSONObject> jsonHead = new ArrayList<JSONObject>();
		JSONObject json = new JSONObject();
		json.put("headName", "主观题评价指标");
		json.put("feilName", "targetName");
		jsonHead.add(json);

		if ("2".equals(targetBody)) {
			json = new JSONObject();
			json.put("headName", "任课教师");
			json.put("feilName", "teachClass");
			jsonHead.add(json);

			json = new JSONObject();
			json.put("headName", "科目");
			json.put("feilName", "subjectName");
			jsonHead.add(json);
		} else if ("1".equals(targetBody)) {
			json = new JSONObject();
			json.put("headName", "班主任");
			json.put("feilName", "ClassTeacher");
			jsonHead.add(json);
		} else if ("3".equals(targetBody)) {
			json = new JSONObject();
			json.put("headName", "年级组长");
			json.put("feilName", "gradeName");
			jsonHead.add(json);
		}

		json = new JSONObject();
		json.put("headName", "班级名称");
		json.put("feilName", "ClassName");
		jsonHead.add(json);

		if (evalObj.containsKey("evalType")) {
			// evalType:1(实名制)，2(匿名制)
			if ("1".equals(evalObj.getString("evalType"))) {
				json = new JSONObject();
				json.put("headName", "学号");
				json.put("feilName", "studentCode");
				jsonHead.add(json);
				json = new JSONObject();
				json.put("headName", "姓名");
				json.put("feilName", "studentName");
				jsonHead.add(json);
			} else {
				json = new JSONObject();
				json.put("headName", "账号");
				json.put("feilName", "anonymousAcc");
				jsonHead.add(json);
			}
		}

		json = new JSONObject();
		json.put("headName", "学生评价内容");
		json.put("feilName", "stuContent");
		jsonHead.add(json);
		return jsonHead;
	}

}
