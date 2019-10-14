package com.talkweb.scoreManage.action;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.T_StageType;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/")
@RestController
public class ScoreAction {

	Logger logger = LoggerFactory.getLogger(ScoreAction.class);
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private AllCommonDataService allCommonDataService;

	static ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant");
	static String FIRST_TERMINFOID = rbConstant.getString("firstTermInfoId");

	String rootPath = SplitUtil.getRootPath("scoremanage.url");

	@RequestMapping("/scoreRport/app/getExamList")
	@ResponseBody
	public JSONObject getExamListForApp(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreRport/app/getExamList");
	}

	@RequestMapping("/scoreRport/app/getSchoolExamStudentScoreReport")
	@ResponseBody
	public JSONObject getSchoolExamStudentScoreReport(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreRport/app/getSchoolExamStudentScoreReport");
	}

	@RequestMapping("/scoreRport/app/getClassExamStudentScoreReport")
	@ResponseBody
	public JSONObject getClassExamStudentScoreReport(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreRport/app/getClassExamStudentScoreReport");
	}

	@RequestMapping("/scoreRport/app/getCustomExamStudentScoreReport")
	@ResponseBody
	public JSONObject getCustomExamStudentScoreReport(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreRport/app/getCustomExamStudentScoreReport");
	}

	@RequestMapping("/scoreRport/app/getClassReportExamList")
	@ResponseBody
	public JSONObject getClassReportExamList(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/getClassReportExamList");
	}

	@RequestMapping("/scoreRport/app/getSchoolExamClassScoreReport")
	@ResponseBody
	public JSONObject getSchoolExamClassScoreReport(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/getSchoolExamClassScoreReport");
	}

	@RequestMapping("/scoreRport/app/getViewScoreParentList")
	@ResponseBody
	public JSONObject getViewScoreParentList(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/getViewScoreParentList");
	}

	@RequestMapping("/scoreRport/app/getClassExamClassScoreReport")
	@ResponseBody
	public JSONObject getClassExamClassScoreReport(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/getClassExamClassScoreReport");
	}

	@RequestMapping("/scoreRport/app/gradeExamScore")
	@ResponseBody
	public JSONObject gradeExamScore(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/gradeExamScore");
	}

	@RequestMapping("/scoreRport/app/getStudentList")
	@ResponseBody
	public JSONObject getStudentListForApp(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/getStudentList");
	}

	@RequestMapping("/scoreRport/app/getExamStudentScoreReport")
	@ResponseBody
	public JSONObject getExamStudentScoreReport(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreRport/app/getExamStudentScoreReport");
	}

	@RequestMapping("/scoreRport/app/sendWxMsg")
	@ResponseBody
	public JSONObject sendWxMsgForApp(@RequestBody JSONObject param) {
		return postAction(param, "scoreRport/app/sendWxMsg");
	}

	@RequestMapping("/scoreReport/classScoreCRUD/getExamList")
	@ResponseBody
	public JSONObject getExamListForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreCRUD/getExamList");
	}

	@RequestMapping("/scoreReport/classScoreCRUD/createExam")
	@ResponseBody
	public JSONObject createExamForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreCRUD/createExam");
	}

	@RequestMapping("/scoreReport/classScoreCRUD/updateExam")
	@ResponseBody
	public JSONObject updateExamForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreCRUD/updateExam");
	}

	@RequestMapping("/scoreReport/classScoreCRUD/deleteExam")
	@ResponseBody
	public JSONObject deleteExamForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreCRUD/deleteExam");
	}

	@RequestMapping("/scoreReport/classScoreCRUD/releaseClassExam")
	@ResponseBody
	public JSONObject releaseClassExamForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreCRUD/releaseClassExam");
	}

	@RequestMapping("/scoremanage/scoreReport/getLevelSubjectStatisTabList")
	@ResponseBody
	public JSONObject getLevelSubjectStatisTabList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLevelSubjectStatisTabList");
	}

	@RequestMapping("/scoremanage/scoreReport/getLeveStudentNumStatisTabList")
	@ResponseBody
	public JSONObject getLeveStudentNumStatisTabList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLeveStudentNumStatisTabList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreResultTotalAnalysisList")
	@ResponseBody
	public JSONObject getScoreResultTotalAnalysisList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getScoreResultTotalAnalysisList");
	}

	@RequestMapping("/scoremanage/scoreReport/getAllPreviousScoreResultCompareList")
	@ResponseBody
	public JSONObject getAllPreviousScoreResultCompareList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getAllPreviousScoreResultCompareList");
	}

	@RequestMapping("/competitionGroup/import/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcelForCompet(@RequestParam("excelBody") MultipartFile file, 
			HttpServletRequest req) {
		Object schoolId = request.getSession().getAttribute("xxdm");
		JSONObject param = new JSONObject();
		param.put("headRowNum", req.getParameter("headRowNum"));
		param.put("schoolId", schoolId);
		param.put("sessionId", req.getSession().getId());
		return SplitUtil.postFile(rootPath + "competitionGroup/import/uploadExcel", 
				file, "excelBody", param);
	}

	@RequestMapping("/competitionGroup/import/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatchForCompet() {
		return postAction(new JSONObject(), "competitionGroup/import/getExcelMatch");
	}

	@RequestMapping("/competitionGroup/import/startImportTask")
	@ResponseBody
	public JSONObject startImportTaskForCompet(@RequestBody JSONObject param) {
		return postAction(param, "competitionGroup/import/startImportTask");
	}

	@RequestMapping("/competitionGroup/import/importProgress")
	@ResponseBody
	public JSONObject importProgressForCompet() {
		return postAction(new JSONObject(), "competitionGroup/import/importProgress");
	}

	@RequestMapping("/competitionGroup/import/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheckForCompet(@RequestBody JSONObject param) {
		return postAction(param, "competitionGroup/import/singleDataCheck");
	}

	@RequestMapping("/competitionGroup/import/continueImport")
	@ResponseBody
	public JSONObject continueImportForCompet() {
		return postAction(new JSONObject(), "competitionGroup/import/continueImport");
	}

	@RequestMapping("/competitionGroup/curd/getCompStdList")
	@ResponseBody
	public JSONObject getCompStdList(@RequestBody JSONObject param) {
		return postAction(param, "competitionGroup/curd/getCompStdList");
	}

	@RequestMapping("/competitionGroup/curd/delCompStdList")
	@ResponseBody
	public JSONObject delCompStdList(@RequestBody JSONObject param) {
		return postAction(param, "competitionGroup/curd/delCompStdList");
	}

	@RequestMapping("/scoreReport/customScoreImport/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcelForCustom(@RequestParam("excelBody") MultipartFile file, 
			HttpServletRequest req) {
		Object schoolId = request.getSession().getAttribute("xxdm");
		JSONObject param = new JSONObject();
		param.put("headRowNum", req.getParameter("headRowNum"));
		param.put("schoolId", schoolId);
		param.put("sessionId", req.getSession().getId());
		return SplitUtil.postFile(rootPath + "scoreReport/customScoreImport/uploadExcel", 
				file, "excelBody", param);
	}

	@RequestMapping("/scoreReport/customScoreImport/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatchForCustom() {
		return postAction(new JSONObject(), "scoreReport/customScoreImport/getExcelMatch");
	}

	@RequestMapping("/scoreReport/customScoreImport/startImportTask")
	@ResponseBody
	public JSONObject startImportTaskForCustom(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/customScoreImport/startImportTask");
	}

	@RequestMapping("/scoreReport/customScoreImport/importProgress")
	@ResponseBody
	public JSONObject importProgressForCustom() {
		return postAction(new JSONObject(), "scoreReport/customScoreImport/importProgress");
	}

	@RequestMapping("/scoreReport/customScoreImport/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheckForCustom(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/customScoreImport/singleDataCheck");
	}

	@RequestMapping("/scoreReport/customScoreImport/continueImport")
	@ResponseBody
	public JSONObject continueImportForCustom() {
		return postAction(new JSONObject(), "scoreReport/customScoreImport/continueImport");
	}

	@RequestMapping("/scoreReport/customScoreImport/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsgForCustom(HttpServletRequest req, HttpServletResponse res) {
		SplitUtil.exportExcelWithData(req, res);
	}

	@RequestMapping("/scoreReport/customScoreImport/ignoreAllError")
	@ResponseBody
	public JSONObject ignoreAllErrorForCustom(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/customScoreImport/ignoreAllError");
	}

	@RequestMapping("/scoreReport/classScoreImport/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcelForClass(@RequestParam("excelBody") MultipartFile file, 
			HttpServletRequest req) {
		Object schoolId = request.getSession().getAttribute("xxdm");
		String termInfoId = req.getParameter("termInfoId"); 
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("schoolId", schoolId);
		param.put("sessionId", req.getSession().getId());
		return SplitUtil.postFile(rootPath + "scoreReport/classScoreImport/uploadExcel", 
				file, "excelBody", param);
	}

	@RequestMapping("/scoreReport/classScoreImport/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatchForClass() {
		return postAction(new JSONObject(), "scoreReport/classScoreImport/getExcelMatch");
	}

	@RequestMapping("/scoreReport/classScoreImport/startImportTask")
	@ResponseBody
	public JSONObject startImportTaskForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreImport/startImportTask");
	}

	@RequestMapping("/scoreReport/classScoreImport/importProgress")
	@ResponseBody
	public JSONObject importProgressForClass() {
		return postAction(new JSONObject(), "scoreReport/classScoreImport/importProgress");
	}

	@RequestMapping("/scoreReport/classScoreImport/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheckForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreImport/singleDataCheck");
	}

	@RequestMapping("/scoreReport/classScoreImport/continueImport")
	@ResponseBody
	public JSONObject continueImportForClass() {
		return postAction(new JSONObject(), "scoreReport/classScoreImport/continueImport");
	}

	@RequestMapping("/scoreReport/classScoreImport/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsgForClass(HttpServletRequest req, HttpServletResponse res) {
		SplitUtil.exportExcelWithData(req, res);
	}

	@RequestMapping("/scoreReport/classScoreImport/ignoreAllError")
	@ResponseBody
	public JSONObject ignoreAllErrorForClass(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/classScoreImport/ignoreAllError");
	}

	@RequestMapping("/talkCloud/scoreReport/getClassList")
	@ResponseBody
	public JSONObject getRecentClassList(@RequestBody JSONObject param) {
		return postAction(param, "talkCloud/scoreReport/getClassList");
	}

	@RequestMapping("/talkCloud/scoreReport/getRecentClassExamInfo")
	@ResponseBody
	public JSONObject getRecentClassExamInfo(@RequestBody JSONObject param) {
		return postAction(param, "talkCloud/scoreReport/getRecentClassExamInfo");
	}

	@RequestMapping("/talkCloud/scoreReport/getRecentStuExamInfo")
	@ResponseBody
	public JSONObject getRecentStuExamInfo(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "talkCloud/scoreReport/getRecentStuExamInfo");
	}

	@RequestMapping("/scoremanage/scoreAlz/getResultAnalysisProgress")
	@ResponseBody
	public JSONObject getResultAnalysisProgress() {
		return postAction(new JSONObject(), "scoremanage/scoreAlz/getResultAnalysisProgress");
	}

	@RequestMapping("/scoremanage/scoreAlz/startResultAnalysisTask")
	@ResponseBody
	public JSONObject startResultAnalysisTask(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreAlz/startResultAnalysisTask");
	}

	@RequestMapping("/scoremanage/setting/getExamSettingConfig")
	@ResponseBody
	public JSONObject getExamSettingConfig(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getExamSettingConfig");
	}

	@RequestMapping("/scoremanage/setting/getASGList")
	@ResponseBody
	public JSONArray getASGSettingList(@RequestBody JSONObject param) {
		return postActionForArray(param, "scoremanage/setting/getASGList");
	}

	@RequestMapping("/scoremanage/setting/saveASG")
	@ResponseBody
	public JSONObject saveASG(@RequestBody JSONObject param) {
		//param.put("accessToken", this.request.getSession().getAttribute("accessToken"));
		return postAction(param, "scoremanage/setting/saveASG");
	}

	@RequestMapping("/scoremanage/setting/getClassGroupList")
	@ResponseBody
	public JSONObject getClassGroupSettingList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getClassGroupList");
	}

	@RequestMapping("/scoremanage/setting/addClassGroup")
	@ResponseBody
	public JSONObject addClassGroup(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/addClassGroup");
	}

	@RequestMapping("/scoremanage/setting/delClassGroup")
	@ResponseBody
	public JSONObject delClassGroup(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/delClassGroup");
	}

	@RequestMapping("/scoremanage/setting/getStaticSelectedSetting")
	@ResponseBody
	public JSONObject getStaticSelectedSetting(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getStaticSelectedSetting");
	}

	@RequestMapping("/scoremanage/setting/getASGListForStatic")
	@ResponseBody
	public JSONObject getASGListForStatic(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getASGListForStatic");
	}

	@RequestMapping("/scoremanage/setting/updateASGStatRules")
	@ResponseBody
	public JSONObject updateASGStatRules(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateASGStatRules");
	}

	@RequestMapping("/scoremanage/setting/getClassGroupListStatic")
	@ResponseBody
	public JSONObject getClassGroupListStatic(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getClassGroupListStatic");
	}

	@RequestMapping("/scoremanage/setting/updateClassGroupStatic")
	@ResponseBody
	public JSONObject updateClassGroupStatic(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateClassGroupStatic");
	}

	@RequestMapping("/scoremanage/setting/getStatStuList")
	@ResponseBody
	public JSONObject getStatStuList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getStatStuList");
	}

	@RequestMapping("/scoremanage/setting/addStatStuList")
	@ResponseBody
	public JSONObject addStatStuList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/addStatStuList");
	}

	@RequestMapping("/scoremanage/setting/delStatStuList")
	@ResponseBody
	public JSONObject delStatStuList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/delStatStuList");
	}

	@RequestMapping("/scoremanage/common/getExamGradeList")
	@ResponseBody
	public JSONObject getExamGradeList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getExamGradeList");
	}

	@RequestMapping("/scoremanage/common/getClassList")
	@ResponseBody
	public JSONObject getClassList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getClassList");
	}

	@RequestMapping("/scoremanage/common/getExamClassList")
	@ResponseBody
	public JSONObject getExamClassList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getExamClassList");
	}

	@RequestMapping("/scoremanage/getHomePageTabRight")
	@ResponseBody
	public JSONObject getHomePageTabRight() {
		JSONObject param = new JSONObject();
		param.put("isTeaching", this.request.getSession().getAttribute("isTeaching"));
		return postAction(param, "scoremanage/getHomePageTabRight");
	}

	@RequestMapping("/scoremanage/common/getASGList")
	@ResponseBody
	public JSONObject getASGList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getASGList");
	}

	@RequestMapping("/scoremanage/common/getClassGroupList")
	@ResponseBody
	public JSONObject getClassGroupList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getClassGroupList");
	}

	@RequestMapping("/scoremanage/common/getClassTypeList")
	@ResponseBody
	public JSONObject getClassTypeList() {
		return postAction(new JSONObject(), "scoremanage/common/getClassTypeList");
	}

	@RequestMapping("/scoremanage/common/getRightSubjectList")
	@ResponseBody
	public JSONObject getRightSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getRightSubjectList");
	}

	@RequestMapping("/scoremanage/common/getClassListByGroupId")
	@ResponseBody
	public JSONObject getClassListByGroupId(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getClassListByGroupId");
	}

	@RequestMapping("/scoremanage/common/getExamNameList")
	@ResponseBody
	public JSONObject getExamNameList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getExamNameList");
	}

	@RequestMapping("/scoremanage/common/getTeacherClassListByGroupId")
	@ResponseBody
	public JSONObject getTeacherClassListByGroupId(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getTeacherClassListByGroupId");
	}

	@RequestMapping("/scoremanage/common/getNoRightClassList")
	@ResponseBody
	public JSONObject getNoRightClassList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getNoRightClassList");
	}

	@RequestMapping("/scoremanage/common/getNoRightTeacherClassList")
	@ResponseBody
	public JSONObject getNoRightTeacherClassList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/common/getNoRightTeacherClassList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreSectionTotalList")
	@ResponseBody
	public JSONObject getScoreSectionTotalList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getScoreSectionTotalList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreSectionSubjectList")
	@ResponseBody
	public JSONObject getScoreSectionSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getScoreSectionSubjectList");
	}

	@RequestMapping("/scoremanage/scoreReport/getRankSectionList")
	@ResponseBody
	public JSONObject getRankSectionList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getRankSectionList");
	}

	@RequestMapping("/scoremanage/import/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(@RequestParam("excelBody") MultipartFile file, 
			HttpServletRequest req) {
		Object schoolId = request.getSession().getAttribute("xxdm");
		String termInfoId = req.getParameter("termInfoId"); 
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("schoolId", schoolId);
		param.put("sessionId", req.getSession().getId());
		return SplitUtil.postFile(rootPath + "scoremanage/import/uploadExcel", 
				file, "excelBody", param);
	}

	@RequestMapping("/scoremanage/import/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatch() {
		return postAction(new JSONObject(), "scoremanage/import/getExcelMatch");
	}

	@RequestMapping("/scoremanage/import/startImportTask")
	@ResponseBody
	public JSONObject startImportTask(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/import/startImportTask");
	}

	@RequestMapping("/scoremanage/import/importProgress")
	@ResponseBody
	public JSONObject importProgress() {
		return postAction(new JSONObject(), "scoremanage/import/importProgress");
	}

	@RequestMapping("/scoremanage/import/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheck(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/import/singleDataCheck");
	}

	@RequestMapping("/scoremanage/import/continueImport")
	@ResponseBody
	public JSONObject continueImport() {
		return postAction(new JSONObject(), "scoremanage/import/continueImport");
	}

	@RequestMapping("/scoremanage/import/exportWrongMsg")
	@ResponseBody
	public void exportWrongMsg(HttpServletRequest req, HttpServletResponse res) {
		SplitUtil.exportExcelWithData(req, res);
	}

	@RequestMapping("/scoremanage/setting/getStatSubjectList")
	@ResponseBody
	public JSONObject getStatSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getStatSubjectList");
	}

	@RequestMapping("/scoremanage/setting/updateStatSubjectList")
	@ResponseBody
	public JSONObject updateStatSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateStatSubjectList");
	}

	@RequestMapping("/scoremanage/setting/getMergedSubjectList")
	@ResponseBody
	public JSONObject getMergedSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getMergedSubjectList");
	}

	@RequestMapping("/scoremanage/setting/addMergedSubject")
	@ResponseBody
	public JSONObject addMergedSubject(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/addMergedSubject");
	}

	@RequestMapping("/scoremanage/setting/delMergedSubject")
	@ResponseBody
	public JSONObject delMergedSubject(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/delMergedSubject");
	}

	@RequestMapping("/scoremanage/curd/getExamList")
	@ResponseBody
	public JSONArray getExamList(@RequestBody JSONObject param) {
		return postActionForArray(param, "scoremanage/curd/getExamList");
	}

	@RequestMapping("/scoremanage/curd/createExam")
	@ResponseBody
	public JSONObject createExam(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/createExam");
	}

	@RequestMapping("/scoremanage/curd/updateExam")
	@ResponseBody
	public JSONObject updateExam(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/updateExam");
	}

	@RequestMapping("/scoremanage/curd/deleteExam")
	@ResponseBody
	public JSONObject deleteExam(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/deleteExam");
	}

	@RequestMapping("/scoremanage/curd/getScoreList")
	@ResponseBody
	public JSONObject getScoreList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/getScoreList");
	}

	@RequestMapping("/scoremanage/curd/updateScore")
	@ResponseBody
	public JSONObject updateScore(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/updateScore");
	}

	@RequestMapping("/scoremanage/curd/getStuScoreDetail")
	@ResponseBody
	public JSONObject getStuScoreDetail(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/getStuScoreDetail");
	}

	@RequestMapping("/scoremanage/curd/getAllScoreList")
	@ResponseBody
	public JSONObject getAllScoreList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/curd/getAllScoreList");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/getScoreReportNameList")
	@ResponseBody
	public JSONArray getScoreReportNameList() {
		return postActionForArray(new JSONObject(), "scoremanage/ScoreReportSet/getScoreReportNameList");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/getReportConfigs")
	@ResponseBody
	public JSONObject getReportConfigs(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/getReportConfigs");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/saveReportConfigs")
	@ResponseBody
	public JSONObject saveReportConfigs(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/saveReportConfigs");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/setReportHidden")
	@ResponseBody
	public JSONObject setReportHidden(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/setReportHidden");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/setReportShow")
	@ResponseBody
	public JSONObject setReportShow(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/setReportShow");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/saveCustomReportConfigs")
	@ResponseBody
	public JSONObject saveCustomReportConfigs(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/saveCustomReportConfigs");
	}

	@RequestMapping("/scoremanage/ScoreReportSet/getCustomReportConfigs")
	@ResponseBody
	public JSONObject getCustomReportConfigs(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/ScoreReportSet/getCustomReportConfigs");
	}

	@RequestMapping("/scoremanage/scoreAlz/publishExamResult")
	@ResponseBody
	public JSONObject publishExamResult(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreAlz/publishExamResult");
	}

	@RequestMapping("/scoremanage/getScoreReleaseList")
	@ResponseBody
	public JSONArray getScoreReleaseList(@RequestBody JSONObject param) {
		return postActionForArray(param, "scoremanage/scoreAlz/getScoreReleaseList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreReportTypeList")
	@ResponseBody
	public JSONArray getScoreReportTypeList() {
		return postActionForArray(new JSONObject(), "scoremanage/scoreReport/getScoreReportTypeList");
	}

	@RequestMapping("/scoremanage/scoreReport/getRightSubjectList")
	@ResponseBody
	public JSONObject getRightSubjectListForReport(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getRightSubjectList");
	}

	@RequestMapping("/scoremanage/scoreReport/getClassScoreReportList")
	@ResponseBody
	public JSONObject getClassScoreReportList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getClassScoreReportList");
	}

	@RequestMapping("/scoremanage/scoreReport/getGradeReportList")
	@ResponseBody
	public JSONObject getGradeReportList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getGradeReportList");
	}

	@RequestMapping("/scoremanage/scoreReport/getAllPreviousLevelCompareList")
	@ResponseBody
	public JSONObject getAllPreviousLevelCompareList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getAllPreviousLevelCompareList");
	}

	@RequestMapping("/scoremanage/scoreReport/getCompetiteStuAnalysisList")
	@ResponseBody
	public JSONObject getCompetiteStuAnalysisList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getCompetiteStuAnalysisList");
	}

	@RequestMapping("/scoremanage/scoreReport/getSubjectTopNStatisList")
	@ResponseBody
	public JSONObject getSubjectTopNStatisList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getSubjectTopNStatisList");
	}

	@RequestMapping("/scoremanage/scoreReport/getExamNameList")
	@ResponseBody
	public JSONObject getExamNameListForReport(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getExamNameList");
	}

	@RequestMapping("/scoremanage/scoreReport/getStatisTypeList")
	@ResponseBody
	public JSONObject getStatisTypeList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStatisTypeList");
	}

	@RequestMapping("/scoremanage/scoreReport/getAllPreviousTrendList")
	@ResponseBody
	public JSONObject getAllPreviousTrendList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getAllPreviousTrendList");
	}

	@RequestMapping("/scoremanage/scoreReport/getClassReportList")
	@ResponseBody
	public JSONObject getClassReportList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getClassReportList");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentScoreReportList")
	@ResponseBody
	public JSONObject getStudentScoreReportList(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoremanage/scoreReport/getStudentScoreReportList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreReportViewList")
	@ResponseBody
	public JSONObject getScoreReportViewList(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoremanage/scoreReport/getScoreReportViewList");
	}

	@RequestMapping("/scoremanage/scoreReport/getScoreReportViewDetail")
	@ResponseBody
	public JSONObject getScoreReportViewDetail(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getScoreReportViewDetail");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentScoreReportListByTea")
	@ResponseBody
	public JSONObject getStudentScoreReportListByTea(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentScoreReportListByTea");
	}

	@RequestMapping("/scoremanage/scoreReport/exportStudentScoreReportListByTea")
	@ResponseBody
	public void exportStudentScoreReportListByTea(HttpServletRequest req, HttpServletResponse res) {
		String data = req.getParameter("excelData");
		JSONObject gg = JSONObject.parseObject(data);
		JSONArray firstExcelHeads = gg.getJSONArray("columns");
		JSONArray firstExcelData = gg.getJSONArray("Studentdata");
		ExcelTool.exportExcelWithTea(firstExcelData, firstExcelHeads, "学生成绩单", null, req, res);
	}

	@RequestMapping("/scoremanage/scoreReport/exportStudentOptimization")
	@ResponseBody
	public void exportStudentOptimization(HttpServletRequest req, HttpServletResponse res) {
		String data = req.getParameter("excelData");
		JSONObject gg = JSONObject.parseObject(data);
		JSONArray firstExcelHeads = gg.getJSONArray("columns");
		JSONArray firstExcelData = gg.getJSONArray("Studentdata");
		ExcelTool.exportExcelWithTea(firstExcelData, firstExcelHeads, "学生优化成绩单", null, req, res);
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentOptimizationList")
	@ResponseBody
	public JSONObject getStudentOptimizationList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentOptimizationList");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentOptimization")
	@ResponseBody
	public JSONObject getStudentOptimization(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentOptimization");
	}

	@RequestMapping("/scoreReport/teacher/scoreAnalysis")
	@ResponseBody
	public JSONObject scoreAnalysis(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/teacher/scoreAnalysis");
	}

	@RequestMapping("/scoreReport/student/scoreAnalysis")
	@ResponseBody
	public JSONObject scoreAnalysisForStudent(@RequestBody JSONObject param) {
		JSONObject ck = this.checkStudent(param);
		if (ck != null)
			return ck;
		return postAction(param, "scoreReport/student/scoreAnalysis");
	}

	@RequestMapping("/scoremanage/setting/getClassReportParam")
	@ResponseBody
	public JSONObject getClassReportParam() {
		return postAction(new JSONObject(), "scoremanage/setting/getClassReportParam");
	}

	@RequestMapping("/scoremanage/setting/updateClassReportParam")
	@ResponseBody
	public JSONObject updateClassReportParam(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateClassReportParam");
	}

	@RequestMapping("/scoremanage/setting/getStudentReportParam")
	@ResponseBody
	public JSONObject getStudentReportParam() {
		return postAction(new JSONObject(), "scoremanage/setting/getStudentReportParam");
	}

	@RequestMapping("/scoremanage/setting/updateStudentReportParam")
	@ResponseBody
	public JSONObject updateStudentReportParam(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateStudentReportParam");
	}

	@RequestMapping("/scoremanage/setting/getStaticParameters")
	@ResponseBody
	public JSONObject getStaticParameters() {
		return postAction(new JSONObject(), "scoremanage/setting/getStaticParameters");
	}

	@RequestMapping("/scoremanage/setting/updateStatisticalParameters")
	@ResponseBody
	public JSONObject updateStatisticalParameters(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateStatisticalParameters");
	}

	@RequestMapping("/scoremanage/setting/getIntervalList")
	@ResponseBody
	public JSONObject getIntervalList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getIntervalList");
	}

	@RequestMapping("/scoremanage/setting/updateInterval")
	@ResponseBody
	public JSONObject updateInterval(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateInterval");
	}

	@RequestMapping("/scoremanage/setting/getTotalScoreSection")
	@ResponseBody
	public JSONObject getTotalScoreSection(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getTotalScoreSection");
	}

	@RequestMapping("/scoremanage/setting/updateTotalScoreSection")
	@ResponseBody
	public JSONObject updateTotalScoreSection(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateTotalScoreSection");
	}

	@RequestMapping("/scoremanage/setting/getContrastExamList")
	@ResponseBody
	public JSONObject getContrastExamList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getContrastExamList");
	}

	@RequestMapping("/scoremanage/setting/updateContrastExamList")
	@ResponseBody
	public JSONObject updateContrastExamList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateContrastExamList");
	}

	@RequestMapping("/scoremanage/setting/getRankingsValueList")
	@ResponseBody
	public JSONObject getRankingsValueList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getRankingsValueList");
	}

	@RequestMapping("/scoremanage/setting/getBillDiviLevelSetting")
	@ResponseBody
	public JSONArray getBillDiviLevelSetting(@RequestBody JSONObject param) {
		return postActionForArray(param, "scoremanage/setting/getBillDiviLevelSetting");
	}

	@RequestMapping("/scoremanage/setting/updateBillDiviLevelSetting")
	@ResponseBody
	public JSONObject updateBillDiviLevelSetting(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateBillDiviLevelSetting");
	}

	@RequestMapping("/scoremanage/setting/delBillDiviLevelSetting")
	@ResponseBody
	public JSONObject delBillDiviLevelSetting(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/delBillDiviLevelSetting");
	}

	@RequestMapping("/scoremanage/setting/updateRankingsValue")
	@ResponseBody
	public JSONObject updateRankingsValue(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/updateRankingsValue");
	}

	@RequestMapping("/scoremanage/setting/getSingleSubjectGrade")
	@ResponseBody
	public JSONObject getSingleSubjectGrade(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/setting/getSingleSubjectGrade");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentLevelScoreResultDetailTab")
	@ResponseBody
	public JSONObject getStudentLevelScoreResultDetailTab(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentLevelScoreResultDetailTab");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentLevelWeakSubjectList")
	@ResponseBody
	public JSONObject getStudentLevelWeakSubjectList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentLevelWeakSubjectList");
	}

	@RequestMapping("/scoremanage/scoreReport/getLevelTotalAnalysisTabList")
	@ResponseBody
	public JSONObject getLevelTotalAnalysisTabList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLevelTotalAnalysisTabList");
	}

	@RequestMapping("/scoremanage/scoreReport/getLevelSubjectAOneThirdStatisList")
	@ResponseBody
	public JSONObject getLevelSubjectAOneThirdStatisList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLevelSubjectAOneThirdStatisList");
	}

	@RequestMapping("/scoremanage/scoreReport/getLevelTotalScoreStaticList")
	@ResponseBody
	public JSONObject getLevelTotalScoreStaticList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLevelTotalScoreStaticList");
	}

	@RequestMapping("/scoremanage/scoreReport/getLevelEveryAStatisTabList")
	@ResponseBody
	public JSONObject getLevelEveryAStatisTabList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getLevelEveryAStatisTabList");
	}

	@RequestMapping("/scoremanage/scoreReport/getStudentScoreResultTrackList")
	@ResponseBody
	public JSONObject getStudentScoreResultTrackList(@RequestBody JSONObject param) {
		return postAction(param, "scoremanage/scoreReport/getStudentScoreResultTrackList");
	}

	@RequestMapping("/scoremanage/upGrade/updateBzf")
	@ResponseBody
	public JSONObject updateBzf() {
		return postAction(new JSONObject(), "scoremanage/upGrade/updateBzf");
	}

	@RequestMapping("/scoreReport/viewClassScore/getClassList")
	@ResponseBody
	public JSONObject getClassListForView(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/viewClassScore/getClassList");
	}

	@RequestMapping("/scoreReport/viewClassScore/getClassExamScore")
	@ResponseBody
	public JSONObject getClassExamScore(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/viewClassScore/getClassExamScore");
	}

	@RequestMapping("/scoreReport/viewClassScore/getClassExamListByTeacher")
	@ResponseBody
	public JSONObject getClassExamListByTeacher(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/viewClassScore/getClassExamListByTeacher");
	}

	@RequestMapping("/scoreReport/viewClassScore/getClassListByTeacher")
	@ResponseBody
	public JSONObject getClassListByTeacher(@RequestBody JSONObject param) {
		return postAction(param, "scoreReport/viewClassScore/getClassListByTeacher");
	}

	private JSONArray postActionForArray(JSONObject param, String action) {
		JSONObject ret = postAction(param, action);
		return ret == null || ret.get("data") == null ? new JSONArray() : ret.getJSONArray("data");
	}
	
	private JSONObject postAction(JSONObject param, String action) {
		JSONObject obj = (JSONObject) request.getSession().getAttribute("curRole");
		if (obj != null) {
			param.put("roleMds", obj.get("roleMds"));
		}

		User user = this.getUserInfo(param, true);
		if (user != null) {
			if (user.getTeacherPart() != null) {
				List<Long> classIds = user.getTeacherPart().getDeanOfClassIds();
				if (classIds != null)
					param.put("deanOfClassIds", classIds);
				List<Course> courseList = user.getTeacherPart().getCourseIds();
				if (courseList != null) {
					List<JSONObject> list = new ArrayList<>();
					for (Course c : courseList) {
						obj = new JSONObject();
						obj.put("lessonId", c.getLessonId());
						obj.put("classId", c.getClassId());
						list.add(obj);
					}
					param.put("courseIds", list);
				}
			}
			if (user.getUserPart() != null) {
				List<Long> orgIds = user.getUserPart().getDeanOfOrgIds();
				if (orgIds != null)
					param.put("deanOfOrgIds", orgIds);
			}
		}
		School school = (School) request.getSession().getAttribute("school");
		if (school == null) {
			Long schoolId = getSchoolId(param);
			String termInfo = getTermInfo(param);
			school = this.allCommonDataService.getSchoolById(schoolId, termInfo);
		}
		if (school != null) {
			List<T_StageType> list = school.getStage();
			if (list != null) {
				List<Integer> slist = new ArrayList<>();
				for (T_StageType st : list) {
					slist.add(st.getValue());
				}
				param.put("stageTypes", slist);
			}
		}
		return SplitUtil.postAction(request, rootPath + action, param);
	}

	private JSONObject checkParent(JSONObject param) {
		JSONObject result = new JSONObject();
		try {
			User user = this.getUserInfo(param);
			if (T_Role.Parent.equals(user.getUserPart().getRole())) {
				if (user.getParentPart() != null && user.getParentPart().getStudentId() != 0) {
					String termInfo = getTermInfo(param);
					Long schoolId = getSchoolId(param);
					Long stdId = user.getParentPart().getStudentId();
					user = allCommonDataService.getUserById(schoolId, stdId, termInfo);
					param.put("studentAccountId", user.getAccountPart().getId());
					param.put("studentAccountName", user.getAccountPart().getName());
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

	private JSONObject checkStudent(JSONObject param) {
		JSONObject ck = this.checkParent(param);
		if (ck != null)
			return ck;
		JSONObject result = new JSONObject();
		try {
			User user = this.getUserInfo(param);
			Long classId = null;
			if (user.getStudentPart() != null) {
				classId = user.getStudentPart().getClassId(); // 获取学生班级
			} else if (user.getParentPart() != null) {
				classId = user.getParentPart().getClassId();
			}
			if (classId != null && !param.containsKey("classId")) {
				param.put("classId", classId);
			}

			return null;
		} catch (Exception e) {
			result.put("code", -1);
			result.put("msg", "操作异常，请联系管理员！");
			e.printStackTrace();
			return result;
		}
	}

	private User getUserInfo(JSONObject param) {
		return getUserInfo(param, false);
	}

	private User getUserInfo(JSONObject param, boolean isteacher) {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null && !param.containsKey("curUser")) {
			String xnxq = getTermInfo(param);
			Long schoolId = getSchoolId(param);
			if (param.containsKey("userId") || param.containsKey("studentId")) {
				Long userId = param.getLong("userId");
				if (userId == null)
					userId = param.getLong("studentId");
				user = allCommonDataService.getUserById(schoolId, userId, xnxq);
			} else if (param.containsKey("accountId")) {// 老师
				Long accountId = param.getLong("accountId");
				Account acct = allCommonDataService.getAccountAllById(schoolId, accountId, xnxq);
				for (User u : acct.getUsers()) {
					if (isteacher && u.getUserPart().getRole().getValue() == T_Role.Teacher.getValue()) {
						user = u;
						break;
					}
					if (!isteacher && u.getUserPart().getRole().getValue() == T_Role.Parent.getValue()) {
						user = u;
						break;
					}
					if (!isteacher && u.getUserPart().getRole().getValue() == T_Role.Student.getValue()) {
						user = u;
						break;
					}
				}
				// System.out.println(acct.getUsers()+"############"+user);
			}
			param.put("curUser", user);
		} else if (param.containsKey("curUser")) {
			user = (User) param.get("curUser");
		}
		return user;
	}

	private String getTermInfo(JSONObject param) {
		String xnxq = param.getString("selectedSemester");
		if (!StringUtils.isEmpty(xnxq))
			return xnxq;
		xnxq = param.getString("xnxq");
		if (!StringUtils.isEmpty(xnxq))
			return xnxq;
		xnxq = param.getString("termInfo");
		if (!StringUtils.isEmpty(xnxq))
			return xnxq;
		xnxq = (String) request.getSession().getAttribute("curXnxq");
		if (!StringUtils.isEmpty(xnxq))
			return xnxq;
		Long schoolId = param.getLong("schoolId");
		xnxq = allCommonDataService.getCurTermInfoId(schoolId);
		param.put("termInfo", xnxq);
		return xnxq;
	}

	private Long getSchoolId(JSONObject param) {
		School school = (School) request.getSession().getAttribute("school");
		if (school == null)
			return param.getLong("schoolId");
		return school.getId();
	}

}
