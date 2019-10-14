package com.talkweb.questionnaire.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherPart;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.jasperReport.util.XSSFStyleTool;
import com.talkweb.questionnaire.service.QuestionnaireService;
import com.talkweb.utils.KafkaUtils;

/**
 * @ClassName QuestionnaireAction.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:45:26
 */
@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireAction extends BaseAction{
	
	Logger logger = LoggerFactory.getLogger(QuestionnaireAction.class);
	
	@Autowired
	private QuestionnaireService questionnaireService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	
	@Value("#{settings['kafkaClientId']}")
	private String clientId;

	@Value("#{settings['kafkaClientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Value("#{settings['question.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['question.msgUrlApp']}")
	private String msgUrlApp;
	
	
	@RequestMapping(value="/queryTermInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryTermInfo(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		response.put("code", 0);
		response.put("msg", "");
		try{
			request.put("schoolId", getXxdm(req));
			// 当前角色
			request.put("user", req.getSession().getAttribute("user"));
			response.put("data", questionnaireService.queryTermInfo(request));
		} catch (Exception e) {
			response.put("code", -1);
			response.put("msg", "查询异常，请稍后再试！");
			e.printStackTrace();
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryQuestionList",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryQuestionList(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		JSONArray arr = new JSONArray();
		String msg = "";
		int code = 0;
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：1）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		String accountId = req.getSession().getAttribute("accountId").toString();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		try {
			List<JSONObject> questionList = questionnaireService.queryQuestionList(paramMap);
			List<String> questionIds = new ArrayList<String>();
			for (JSONObject jsonObject : questionList) {
				questionIds.add(jsonObject.getString("questionId"));
			}
			if(CollectionUtils.isNotEmpty(questionIds)){
				paramMap.put("questionIds", questionIds);
			}
			List<JSONObject> recordByList = questionnaireService.queryRecordByList(paramMap);
			
			List<JSONObject> objInfos = questionnaireService.queryObInfoByList(paramMap);
			Map<String, List<JSONObject>> objInfoMap = new HashMap<String, List<JSONObject>>();
			Map<String, JSONObject> allMap = new HashMap<String, JSONObject>();
			Map<String, JSONObject> classInfoMap = new HashMap<String, JSONObject>();
			Map<String, JSONObject> gradeInfoMap = new HashMap<String, JSONObject>();
			Map<String, Integer> recordInfoMap = new HashMap<String, Integer>();
			List<Long> allClassIds = new ArrayList<Long>();
			List<Long> allGradeIds = new ArrayList<Long>();
			List<Classroom> allClassroomBatch = null;
			List<Grade> allGradeBatch = null;
			if(CollectionUtils.isNotEmpty(recordByList) && recordByList.size() > 0){
				for (JSONObject jsonObject : recordByList) {
					recordInfoMap.put(jsonObject.getString("questionId"), jsonObject.getInteger("count"));
				}
			}
		 
			if(CollectionUtils.isNotEmpty(objInfos) && objInfos.size() > 0){
				for (JSONObject jsonObject : objInfos) {
					String objType = jsonObject.getString("objType");
					if(!"1".equals(objType)){
						Long classId = jsonObject.getLong("classId");
						if(!allClassIds.contains(classId)){
							allClassIds.add(classId);
						}
					}
				}
				if(CollectionUtils.isNotEmpty(allClassIds) && allClassIds.size() > 0){
					allClassroomBatch = commonDataService.getClassroomBatch(Long.valueOf(schoolId), allClassIds, termInfoId);
				}
				if(CollectionUtils.isNotEmpty(allClassroomBatch) && allClassroomBatch.size() > 0){
					for (Classroom classroom : allClassroomBatch) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("gradeId", String.valueOf(classroom.getGradeId()));
						jsonObject.put("classId", String.valueOf(classroom.getId()));
						jsonObject.put("className", classroom.getClassName());
						List<Long> studentIds = classroom.getStudentAccountIds();
						if(CollectionUtils.isNotEmpty(studentIds) && studentIds.size() > 0){
							jsonObject.put("count", studentIds.size());
						}else{
							jsonObject.put("count", "0");
						}
						classInfoMap.put(String.valueOf(classroom.getId()), jsonObject);
						if(!allGradeIds.contains(classroom.getGradeId())){
							allGradeIds.add(classroom.getGradeId());
						}
					}
					if(CollectionUtils.isNotEmpty(allGradeIds) && allGradeIds.size() > 0){
						Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
						allGradeBatch = commonDataService.getGradeBatch(Long.valueOf(schoolId), allGradeIds, termInfoId);
						if(CollectionUtils.isNotEmpty(allGradeBatch) && allGradeBatch.size() > 0){
							for (Grade grade : allGradeBatch) {
								String string = njName.get(grade.getCurrentLevel());
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("gradeId", String.valueOf(grade.getId()));
								jsonObject.put("gradeName", string);
								gradeInfoMap.put(String.valueOf(grade.getId()), jsonObject);
							}
						}
					}
				}
				for (JSONObject jsonObject : objInfos) {
					String questionId = jsonObject.getString("questionId");
					if(objInfoMap.containsKey(questionId)){
						objInfoMap.get(questionId).add(jsonObject);
					}else{
						objInfoMap.put(questionId, new ArrayList<JSONObject>());
						objInfoMap.get(questionId).add(jsonObject);
					}
				}
				if(!objInfoMap.isEmpty()){
					Set<Entry<String, List<JSONObject>>> entrySet = objInfoMap.entrySet();
					Iterator<Entry<String, List<JSONObject>>> iterator = entrySet.iterator();
					while (iterator.hasNext()) {
						Map.Entry<String, List<JSONObject>> entry = (Map.Entry<String, List<JSONObject>>) iterator.next();
						String key = entry.getKey();
						//0aff7c98-63f3-4115-92d0-551a5465b66f
						List<JSONObject> value = entry.getValue();
						JSONObject allJsonObject = new JSONObject();
						if(CollectionUtils.isNotEmpty(value) && value.size() > 0){
							String dcdxType = value.get(0).getString("objType");
							Integer peopleNum = 0;
							List<JSONObject> classObjects = new ArrayList<JSONObject>();
							List<JSONObject> gradeObjects = new ArrayList<JSONObject>();
							for (JSONObject jsonObject : value) {
								if(!"1".equals(dcdxType)){
									JSONObject jsonObject2 = classInfoMap.get(jsonObject.getString("classId"));
									if(jsonObject2 != null){
										classObjects.add(jsonObject2);
										JSONObject jsonObject3 = gradeInfoMap.get(jsonObject2.getString("gradeId"));
										if(jsonObject3 != null){
											if(!gradeObjects.contains(jsonObject3)){
												gradeObjects.add(jsonObject3);
											}
										}
										if(jsonObject2 != null){
											peopleNum += jsonObject2.getInteger("count");
										}
									}
								}else{
									peopleNum = value.size();
								}
							}
							if(!"1".equals(dcdxType)){
								classObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, classObjects, "gradeId,className");
								gradeObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, gradeObjects, "gradeName");
								JSONObject cObject = new JSONObject();
								cObject.put("gradeId", "-1");
								cObject.put("classId", "-1");
								cObject.put("className", "全部");
								classObjects.add(0,cObject);
								JSONObject gObject = new JSONObject();
								gObject.put("gradeId", "-1");
								gObject.put("gradeName", "全部");
								gradeObjects.add(0,gObject);
								allJsonObject.put("classObjects", classObjects);
								allJsonObject.put("gradeObjects", gradeObjects);
							}
							allJsonObject.put("dcdxType", dcdxType);
							allJsonObject.put("objNum", peopleNum);
						}
						allMap.put(key, allJsonObject);
					}
				}
			} 
			
		 
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String now = format.format(new Date());
		    Date date =  format.parse(now);
			
			for (JSONObject jsonObject : questionList) {
				String questionId = jsonObject.getString("questionId");
				if(jsonObject.containsKey("questionStartDate") && jsonObject.containsKey("questionEndDate")){
					HashMap<String, Object> statusMap = new HashMap<String, Object>();
					statusMap.put("questionId", questionId);
					statusMap.put("yearId", xn);
					statusMap.put("termId", xq);
					statusMap.put("schoolId", schoolId);
					String questionStartDate = jsonObject.getString("questionStartDate");
					String questionEndDate = jsonObject.getString("questionEndDate");
					String dateDayFormat = DateUtil.getDateDayFormat();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date dateDay = sdf.parse(dateDayFormat);
					Date startDate = sdf.parse(questionStartDate);
					Date endDate = sdf.parse(questionEndDate);
					if(startDate.getTime() > dateDay.getTime()){
						paramMap.put("questionStatus", "3");
						jsonObject.put("questionStatus", "3");
					}else{
						if(dateDay.getTime() >= startDate.getTime() && dateDay.getTime() <= endDate.getTime()){
							statusMap.put("questionStatus", "1");
							jsonObject.put("questionStatus", "1");
						}else{
							statusMap.put("questionStatus", "2");
							jsonObject.put("questionStatus", "2");
						}
					}
					questionnaireService.updateQuestionnaireStatus(statusMap);
				}
				JSONObject attrObject = allMap.get(questionId);
				if(attrObject != null){
					String type = attrObject.getString("dcdxType");
					jsonObject.put("dcdxType", attrObject.getString("dcdxType"));
					jsonObject.put("objNum", attrObject.getString("objNum"));
					if(!"1".equals(type)){
						jsonObject.put("classObjects", attrObject.getString("classObjects"));
						jsonObject.put("gradeObjects", attrObject.getString("gradeObjects"));
					}
				}else{
					jsonObject.put("objNum", 0);
					jsonObject.put("dcdxType", 0);
				}
				Integer integer = recordInfoMap.get(questionId) == null ? 0 : recordInfoMap.get(questionId);
				if(integer > 0){
					jsonObject.put("updateStatus", "1");
				}else{
					jsonObject.put("updateStatus", "0");
				}
				if(attrObject != null && "1".equals(attrObject.getString("dcdxType"))){
					//d9e987a3-fa73-4ec7-81dc-9848ecf6eddf
					HashMap<String, Object> objMap = new HashMap<String, Object>();
					objMap.put("questionId", questionId);
					objMap.put("schoolId", schoolId);
					objMap.put("accountId", accountId);
					List<JSONObject> tables = null;
					if(jsonObject.containsKey("questionType")){
						String questionType = jsonObject.getString("questionType");
						if("1".equals(questionType)){
							 tables = questionnaireService.queryOptionForTableById(objMap);
						}else{
							 tables = questionnaireService.queryTargetByQueId(objMap);
						}
						if (objInfoMap.containsKey(questionId) && tables!=null && tables.size() > 0 && date.getTime() <= jsonObject.getDate("questionEndDate").getTime() ) {
							jsonObject.put("showNotice", 1);
						}else {
							jsonObject.put("showNotice", 0);
						}
					}
					List<JSONObject> queryObInfo = questionnaireService.queryObInfo(objMap);
					if(attrObject != null && 
							jsonObject.containsKey("questionEndDate") && 
							jsonObject.containsKey("questionStartDate") &&
							jsonObject.containsKey("questionType") &&
							jsonObject.containsKey("questionDetail") && 
							CollectionUtils.isNotEmpty(tables) && CollectionUtils.isNotEmpty(queryObInfo)
							){
						jsonObject.put("objStatus", "1");
					}else{
						jsonObject.put("objStatus", "0");
					}
				}else{
					List<JSONObject> tables = null;
					HashMap<String, Object> objMap = new HashMap<String, Object>();
					objMap.put("questionId", questionId);
					 
						String questionType = jsonObject.getString("questionType");
						if("1".equals(questionType)){
							 tables = questionnaireService.queryOptionForTableById(objMap);
						}else{
							 tables = questionnaireService.queryTargetByQueId(objMap);
						}
						if (objInfoMap.containsKey(questionId) && tables!=null && tables.size() > 0 && date.getTime() <= jsonObject.getDate("questionEndDate").getTime() ) {
							jsonObject.put("showNotice", 1);
						}else {
							jsonObject.put("showNotice", 0);
						}

					jsonObject.put("objStatus", "0");
				}
				if(jsonObject.containsKey("questionStatus")){
					Integer questionStatus = jsonObject.getInteger("questionStatus");
					if(1 == questionStatus){
						jsonObject.put("questionStatus", "进行中");
					}
					if(2 == questionStatus){
						jsonObject.put("questionStatus", "已结束");
					}
					if(questionStatus != 1 && questionStatus != 2){
						jsonObject.put("questionStatus", "");
					}
				}
			}
			arr = (JSONArray) JSON.toJSON(questionList);
		} catch (Exception e) {
			code = -1;
			msg = "查询异常，请稍后再试！";
			e.printStackTrace();
		}
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("data", arr);
		return rs;
	}
	
	@RequestMapping(value="/createQuestionnaire",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject createQuestionnaire(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionName = request.getString("questionName");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionName)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：2）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", UUIDUtil.getUUID());
		paramMap.put("questionName", questionName);
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createDate", new Date());
		paramMap.put("createAccount", accountId);
		try {
			Integer num = questionnaireService.createQuestionnaire(paramMap);
			if(num > 0){
				code = 0;
				msg = "新增成功！";
			}else{
				code = -1;
				msg = "新增失败！";
			}
		} catch (Exception e) {
			code = -1;
			msg = "新增异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	@RequestMapping(value="/updateQuestionByName",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateQuestionByName(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String termInfoId = request.getString("termInfoId");
		String questionName = request.getString("questionName");
		String questionId = request.getString("questionId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：3）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionName", questionName);
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createAccount", accountId);
		paramMap.put("questionId", questionId);
		try {
			Integer num = questionnaireService.updateQuestionByName(paramMap);
			if(num > 0){
				code = 0;
				msg = "保存成功！";
			}else{
				code = -1;
				msg = "保存失败！";
			}
		} catch (Exception e) {
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/updateQuestionnaireByDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateQuestionnaireByDetail(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) throws ParseException{
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String questionStartDate = request.getString("questionStartDate");
		String questionEndDate = request.getString("questionEndDate");
		String questionDetail = request.getString("questionDetail");
		String questionType = request.getString("questionType");
		Integer  anonymous = request.getInteger("anonymous");
		Integer  allowModify = request.getInteger("allowModify");
		if (anonymous == null) {
			anonymous = 0;
		}
		if (allowModify==null) {
			allowModify = 0;
		}
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId) || StringUtils.isEmpty(questionStartDate)
				|| StringUtils.isEmpty(questionEndDate) || StringUtils.isEmpty(questionDetail) || StringUtils.isEmpty(questionType)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：4）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		JSONObject questionRule = new JSONObject();
		if("1".equals(questionType)){
			 questionRule = (JSONObject) request.get("questionRule");
		}
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionStartDate", questionStartDate);
		paramMap.put("questionEndDate", questionEndDate);
		paramMap.put("questionDetail", questionDetail);
		paramMap.put("questionType", questionType);
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createAccount", accountId);
		paramMap.put("questionId", questionId);
		paramMap.put("questionRule", questionRule);
		paramMap.put("anonymous", anonymous);
		paramMap.put("allowModify", allowModify);
		String dateDayFormat = DateUtil.getDateDayFormat();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateDay = sdf.parse(dateDayFormat);
		Date startDate = sdf.parse(questionStartDate);
		Date endDate = sdf.parse(questionEndDate);
		if(startDate.getTime() > dateDay.getTime()){
			paramMap.put("questionStatus", "3");
		}else{
			if(dateDay.getTime() >= startDate.getTime() && dateDay.getTime() <= endDate.getTime()){
				paramMap.put("questionStatus", "1");
			}else{
				paramMap.put("questionStatus", "2");
			}
		}
		try {
			Integer num = questionnaireService.updateQuestionnaireByDetail(paramMap);
			if(num > 0){
				code = 0;
				msg = "保存成功！";
			}else{
				code = -1;
				msg = "保存失败！";
			}
		} catch (Exception e) {
			logger.error("保存问卷调查出错。", e);
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	@RequestMapping(value="/initQuestionnaireByDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject initQuestionnaireByDetail(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：5）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createAccount", accountId);
		paramMap.put("questionId", questionId);
		try {
			List<JSONObject> jsonObjects = questionnaireService.queryQuestionList(paramMap);
			if(CollectionUtils.isNotEmpty(jsonObjects)){
				JSONObject jsonObject = jsonObjects.get(0);
				String questionType = jsonObject.getString("questionType");
				if("1".equals(questionType)){
					List<JSONObject> ruleForTable = questionnaireService.queryRuleForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(ruleForTable)){
						rs.put("ruleForTable", ruleForTable.get(0));
					}
					List<JSONObject> optionForTable = questionnaireService.queryOptionForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(optionForTable)){
						rs.put("status", "1");
					}else {
						rs.put("status", "0");
					}
				}else{
					List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
					if(CollectionUtils.isNotEmpty(targetS)){
						rs.put("status", "1");
					}else {
						rs.put("status", "0");
					}
				}
				rs.put("question", jsonObject);
			}
		} catch (Exception e) {
			logger.error("查询问卷调查出错。", e);
			code = -1;
			msg = "查询异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	@RequestMapping(value="/initQuestionnaireByWjTm",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject initQuestionnaireByWjTm(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：6）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createAccount", accountId);
		paramMap.put("questionId", questionId);
		try {
			List<JSONObject> jsonObjects = questionnaireService.queryQuestionList(paramMap);
			if(CollectionUtils.isNotEmpty(jsonObjects)){
				JSONObject jsonObject = jsonObjects.get(0);
				String questionType = jsonObject.getString("questionType");
				if("1".equals(questionType)){
					List<JSONObject> ruleForTable = questionnaireService.queryRuleForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(ruleForTable)){
						rs.put("ruleForTable", ruleForTable.get(0));
					}
					List<JSONObject> optionForTableList = questionnaireService.queryOptionForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(optionForTableList)){
						rs.put("optionForTable", optionForTableList);
						rs.put("initRowLength", optionForTableList.get(optionForTableList.size()-1).getInteger("indexRow"));
					}
				}else{
					List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
					if(CollectionUtils.isNotEmpty(targetS)){
						for(JSONObject target : targetS){
							if("1".equals(target.getString("targetType"))){
								HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
								String targetId = target.getString("targetId");
								paramMap2.put("targetId", targetId);
								List<JSONObject> targetLevels = questionnaireService.queryTargetLevelById(paramMap2);
								if(CollectionUtils.isNotEmpty(targetLevels)){
									target.put("level", targetLevels);
								}
							}
						}
					}
					rs.put("target", targetS);
				}
				rs.put("question", jsonObject);
			}
		} catch (Exception e) {
			logger.error("查询问卷调查出错。", e);
			code = -1;
			msg = "查询异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	@RequestMapping(value="/saveQuestionByTab",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveQuestionByTab(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		JSONArray questionOption = (JSONArray) request.get("questionOption");
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		String questionId = "";
		try {
			List<JSONObject> listObject = new ArrayList<JSONObject>();
			for(int index = 0;index<questionOption.size();index++){
				JSONObject object = (JSONObject)questionOption.get(index);
				if(0 == index){
					questionId = object.getString("questionId");
				}
				object.put("tableOptionId", UUIDUtil.getUUID());
				listObject.add(object);
			}
			paramMap.put("optionForTableList", listObject);
			Integer saveNum = questionnaireService.saveQuestionByTab(paramMap,questionId);
			if(saveNum > 0){
				msg = "保存成功！";
			}else{
				code = -1;
				msg = "保存异常，请稍后再试！";
			}
		} catch (Exception e) {
			logger.error("保存问卷调查出错。", e);
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(
			@RequestParam("excelBody") MultipartFile file,@RequestParam("questionId") String questionId,
			HttpServletRequest req, HttpServletResponse res) throws IOException {
		JSONObject importJson = questionnaireService.importExcel(file, questionId);
		if(0 == importJson.getInteger("code") && importJson.getInteger("failNum") <= 10){
			HashMap<String, List<List<JSONObject>>> map  = (HashMap<String, List<List<JSONObject>>>)importJson.get("data");
			if(map.containsKey("success")){
				List<JSONObject> success = new ArrayList<JSONObject>();
				List<List<JSONObject>> list = map.get("success");
				for(List<JSONObject> zz : list){
					for(JSONObject jsonObject : zz){
						success.add(jsonObject);
					}
				}
				HashMap<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("optionForTableList", success);
				questionnaireService.saveQuestionByTab(paramMap,questionId);
			}
		}
		
		return importJson;
	}
	@RequestMapping(value="/saveImportFail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveImportFail(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		List<JSONObject> list = new ArrayList<JSONObject>();
		JSONArray failList = (JSONArray) request.get("failList");
		Integer indexRow = request.getInteger("indexRow");
		String questionId = request.getString("questionId");
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		Integer oldIndexRow = 0;
		List<JSONObject> optionForTableList = questionnaireService.queryOptionForTableById(paramMap);
		for(int index = 0;index < optionForTableList.size();index++){
			JSONObject jsonObject = optionForTableList.get(index);
				oldIndexRow = jsonObject.getInteger("indexRow");
				if(oldIndexRow >= indexRow){
					oldIndexRow = oldIndexRow + 1;
				}
				jsonObject.put("indexRow", oldIndexRow);
				list.add(jsonObject);
		}
		for(int index = 0;index < failList.size();index++){
			JSONArray objectList = (JSONArray) failList.get(index);
			for(int in = 0;in < objectList.size();in++){
				JSONObject object = (JSONObject)objectList.get(in);
				Integer numRows = object.getInteger("indexRow");
				if(numRows >= oldIndexRow){
					object.put("indexRow", oldIndexRow+1);
				}
				list.add(object);
			}
		}
		
		try {
			
			paramMap.put("optionForTableList", list);
			Integer saveNum = questionnaireService.saveQuestionByTab(paramMap,questionId);
			if(saveNum > 0){
				msg = "保存成功！";
			}else{
				code = -1;
				msg = "保存异常，请稍后再试！";
			}
		} catch (Exception e) {
			logger.error("保存问卷调查出错。", e);
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	//questionId,targetType,optionType,targetDetail,targetSeq,isNeed,targetLevel
	@RequestMapping(value="/saveQuestionByQx",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveQuestionByQx(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String schoolId = getXxdm(req);
		String questionId = request.getString("questionId");
		String targetType = request.getString("targetType");
		String optionType = request.getString("optionType");
		String targetDetail = request.getString("targetDetail");
		String targetSeq = request.getString("targetSeq");
		String isNeed = request.getString("isNeed");
		Integer maxmum = request.getInteger("maxmum");
		@SuppressWarnings("unchecked")
		List<JSONObject> targetLevels = (List<JSONObject>)request.get("targetLevel");
		try {
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("targetId", UUIDUtil.getUUID());
			paramMap.put("optionType", optionType);
			paramMap.put("targetType", targetType);
			paramMap.put("questionId", questionId);
			paramMap.put("targetDetail", targetDetail);
			paramMap.put("targetSeq", targetSeq);
			paramMap.put("isNeed", isNeed);
			paramMap.put("targetLevel", targetLevels);
			paramMap.put("schoolId", schoolId);
			paramMap.put("maxmum", maxmum);
			Integer saveNum = questionnaireService.saveQuestionByQx(paramMap);
			if(saveNum > 0){
				msg = "保存成功！";
			}else{
				code = -1;
				msg = "保存异常，请稍后再试！";
			}
		} catch (Exception e) {
			logger.error("保存问卷调查出错。", e);
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/queryTargetMaxSeqByQueId",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryTargetMaxSeqByQueId(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		Integer saveNum = questionnaireService.queryTargetMaxSeqByQueId(paramMap);
		if(saveNum == null){
			saveNum = 0;
		}
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("maxNum", saveNum);
		return rs;
	}
	
	@RequestMapping(value="/queryTargetByQueId",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryTargetByQueId(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		JSONArray arr = new JSONArray();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String targetId = request.getString("targetId");
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		if(StringUtils.isNotBlank(targetId)){
			paramMap.put("targetId", targetId);
		}
		List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
		if(CollectionUtils.isNotEmpty(targetS)){
			for(JSONObject target : targetS){
				if("1".equals(target.getString("targetType"))){
					HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
					String targetId2 = target.getString("targetId");
					paramMap2.put("targetId", targetId2);
					List<JSONObject> targetLevels = questionnaireService.queryTargetLevelById(paramMap2);
					if(CollectionUtils.isNotEmpty(targetLevels)){
						target.put("level", targetLevels);
					}
				}
			}
		}
		arr = (JSONArray) JSON.toJSON(targetS);
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("data", arr);
		return rs;
	}
	
	@RequestMapping(value="/editQuesitonByGx",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject editQuesitonByGx(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String schoolId = getXxdm(req);
		String questionId = request.getString("questionId");
		String targetId = request.getString("targetId");
		String targetType = request.getString("targetType");
		String targetDetail = request.getString("targetDetail");
		String isNeed = request.getString("isNeed");
		@SuppressWarnings("unchecked")
		List<JSONObject> targetLevels = (List<JSONObject>)request.get("targetLevel");
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		try {
			paramMap.put("targetId", targetId);
			paramMap.put("questionId", questionId);
			paramMap.put("targetDetail", targetDetail);
			paramMap.put("isNeed", isNeed);
			paramMap.put("targetLevel", targetLevels);
			paramMap.put("schoolId", schoolId);
			paramMap.put("targetType", targetType);
			Integer updateNum = questionnaireService.updateQuestionByQx(paramMap);
			if(updateNum > 0){
				msg = "保存成功！";
				List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
				if(CollectionUtils.isNotEmpty(targetS)){
					returnMap.put("target", targetS.get(0));
				}
				if("1".equals(targetType)){
					List<JSONObject> newTargetLevels = questionnaireService.queryTargetLevelById(paramMap);
					returnMap.put("targetLevel", newTargetLevels);
				}
			}else{
				code = -1;
				msg = "保存异常，请稍后再试！";
			}
		} catch (Exception e) {
			logger.error("保存问卷调查出错。", e);
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("data", returnMap);
		return rs;
	}
	
	@RequestMapping(value="/moveTargetById",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject moveTargetById(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String[] tmp = {"questionId", "targetId", "moveDirection"};
		for(String key : tmp){
			if(!request.containsKey(key) || StringUtils.isEmpty(request.getString(key))){
				logger.error("moveTargetById:" + key + "参数为空。");
				rs.put("code", -1);
				rs.put("msg", "参数为空");
				return rs;
			}
		}
		String schoolId = getXxdm(req);
		request.put("schoolId", schoolId);
		
		Integer moveDirection = request.getInteger("moveDirection");
		Integer moveNum = questionnaireService.updateUpOrDownTargetSeq(request);
		if(moveNum > 0){
			if(moveDirection > 0){
				msg = "上移成功！";
			}else{
				msg = "下移成功！";
			}
		}else{
			code = -1;
			if(moveDirection > 0){
				msg = "上移异常，请稍后再试！";
			}else{
				msg = "下移异常，请稍后再试！";
			}
		}
		
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/deleteQuestionByGx",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteQuestionByGx(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String targetId = request.getString("targetId");
		String schoolId = getXxdm(req);
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("targetId", targetId);
		paramMap.put("schoolId", schoolId);
		Integer deleteNum = questionnaireService.deleteQuestionByGx(paramMap);
		if(deleteNum > 0){
			msg = "删除成功！";
		}else{
			code = -1;
			msg = "删除失败！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/queryQuestionListByUser",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryQuestionListByUser(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		JSONArray arr = new JSONArray();
		String msg = "";
		int code = 0;
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：7）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		User userxx = (User)req.getSession().getAttribute("user");
		String accountId = req.getSession().getAttribute("accountId").toString();
		String roleFlag = "";
		String type = "1";
		List<User> users = new ArrayList<User>();
		if(userxx != null){
			logger.info(" queryQuestionListByUser =====> " + JSONObject.toJSONString(userxx));
			users.add(userxx);
		}
		if(CollectionUtils.isNotEmpty(users) && users.size() > 0){
			User user = users.get(0);
			T_Role role = user.getUserPart().getRole();
			if(role.getValue() == 0){
				type="3";
				roleFlag = "0";
			}else if( role.getValue() == 2){
				type = "2";
				roleFlag = "2";
			}
		}
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		try {
			List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
			List<JSONObject> allJsonObjects = new ArrayList<JSONObject>();
			List<JSONObject> questionList = questionnaireService.queryQuestionList(paramMap);
			for (int index = 0;index < questionList.size();index++) {
				JSONObject jsonObject = questionList.get(index);
				if(jsonObject.containsKey("questionStartDate") && jsonObject.containsKey("questionEndDate")){
					HashMap<String, Object> statusMap = new HashMap<String, Object>();
					statusMap.put("questionId", jsonObject.get("questionId"));
					statusMap.put("yearId", xn);
					statusMap.put("termId", xq);
					statusMap.put("schoolId", schoolId);
					String questionStartDate = jsonObject.getString("questionStartDate");
					String questionEndDate = jsonObject.getString("questionEndDate");
					String dateDayFormat = DateUtil.getDateDayFormat();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date dateDay = sdf.parse(dateDayFormat);
					Date startDate = sdf.parse(questionStartDate);
					Date endDate = sdf.parse(questionEndDate);
					if(startDate.getTime() > dateDay.getTime()){
						paramMap.put("questionStatus", "3");
						jsonObject.put("questionStatus", "3");
					}else{
						if(dateDay.getTime() >= startDate.getTime() && dateDay.getTime() <= endDate.getTime()){
							statusMap.put("questionStatus", "1");
							jsonObject.put("questionStatus", "1");
						}else{
							statusMap.put("questionStatus", "2");
							jsonObject.put("questionStatus", "2");
						}
					}
					questionnaireService.updateQuestionnaireStatus(statusMap);
				}
				if(jsonObject.containsKey("questionDetail") && jsonObject.containsKey("questionStartDate") && 
				   jsonObject.containsKey("questionEndDate") && jsonObject.containsKey("questionType")){
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("questionId", jsonObject.getString("questionId"));
					String questionType = jsonObject.getString("questionType");
					if("2".equals(questionType)){
						List<JSONObject> targetS = questionnaireService.queryTargetByQueId(map);
						if(CollectionUtils.isNotEmpty(targetS) && targetS.size() > 0){
							jsonObjects.add(jsonObject);
						}
					}else{
						List<JSONObject> optionForTableS = questionnaireService.queryOptionForTableById(map);
						if(CollectionUtils.isNotEmpty(optionForTableS) && optionForTableS.size() > 0){
							jsonObjects.add(jsonObject);
						}
					}
				}
			}
			for(JSONObject json :jsonObjects){
				if(json.containsKey("questionId")){
					String questionId = json.getString("questionId");
					if("0".equals(roleFlag)){
						User user = users.get(0);
						long classId = user.getParentPart().getClassId();
						HashMap<String, Object> objMap = new HashMap<String, Object>();
						objMap.put("questionId", questionId);
						objMap.put("schoolId", schoolId);
						objMap.put("classId", classId);
						objMap.put("type", type);
						List<JSONObject> queryObInfo1 = questionnaireService.queryObInfo(objMap);
						if(CollectionUtils.isNotEmpty(queryObInfo1) && queryObInfo1.size() > 0){
							json.put("selectType", type);
							allJsonObjects.add(json);
						}
					}else if("2".equals(roleFlag)){
						User user = users.get(0);
						long classId = user.getStudentPart().getClassId();
						HashMap<String, Object> objMap = new HashMap<String, Object>();
						objMap.put("questionId", questionId);
						objMap.put("schoolId", schoolId);
						objMap.put("classId", classId);
						objMap.put("type", type);
						List<JSONObject> queryObInfo1 = questionnaireService.queryObInfo(objMap);
						if(CollectionUtils.isNotEmpty(queryObInfo1) && queryObInfo1.size() > 0){
							json.put("selectType", type);
							allJsonObjects.add(json);
						}
					}else{
						HashMap<String, Object> objMap = new HashMap<String, Object>();
						objMap.put("questionId", questionId);
						objMap.put("schoolId", schoolId);
						objMap.put("accountId", accountId);
						List<JSONObject> queryObInfo1 = questionnaireService.queryObInfo(objMap);
						if(CollectionUtils.isNotEmpty(queryObInfo1) && queryObInfo1.size() > 0){
							JSONObject jsonObject = queryObInfo1.get(0);
							json.put("selectType", jsonObject.getString("objType"));
							allJsonObjects.add(json);
						}
						if(CollectionUtils.isNotEmpty(users) && users.size() > 0){
							User user = users.get(0);
							TeacherPart teacherPart = user.getTeacherPart();
							List<Long> deanOfClassIds = null;
							if(teacherPart != null){
								deanOfClassIds = teacherPart.getDeanOfClassIds();
							}
							List<Long> classIds = new ArrayList<Long>();
							if(CollectionUtils.isNotEmpty(deanOfClassIds) && deanOfClassIds.size() > 0){
								for (Long long1 : deanOfClassIds) {
									classIds.add(long1);
								}
							}
							if(CollectionUtils.isNotEmpty(classIds) && classIds.size() > 0){
								HashMap<String, Object> objMap1 = new HashMap<String, Object>();
								objMap1.put("questionId", questionId);
								objMap1.put("schoolId", schoolId);
								objMap1.put("classIds", classIds);
								List<JSONObject> queryObInfo2 = questionnaireService.queryObInfo(objMap1);
								if(CollectionUtils.isNotEmpty(queryObInfo2) && queryObInfo2.size() > 0){
									JSONObject jsonObject = queryObInfo2.get(0);
									json.put("statusViem", "2");
									json.put("selectType", jsonObject.getString("objType"));
									allJsonObjects.add(json);
								}
							}
						}
					}
				}
			}
			for(int i = 0;i<allJsonObjects.size();i++){
				JSONObject jsonObject = allJsonObjects.get(i);
				if(jsonObject.containsKey("questionStatus")){
					Integer questionStatus = jsonObject.getInteger("questionStatus");
					if(1 == questionStatus){
						jsonObject.put("questionStatus", "进行中");
					}
					if(2 == questionStatus){
						jsonObject.put("questionStatus", "已结束");
					}
					if(questionStatus != 1 && questionStatus != 2){
						jsonObject.put("questionStatus", "");
					}
				}
			}
			if(CollectionUtils.isNotEmpty(allJsonObjects)){
				for (int i = 0;i<allJsonObjects.size();i++) {
					JSONObject jsonObject = allJsonObjects.get(i);
					if(StringUtils.isBlank(jsonObject.getString("questionStatus"))){
						allJsonObjects.remove(i);
					}
				}
			}
			
			arr = (JSONArray) JSON.toJSON(allJsonObjects);
		} catch (Exception e) {
			logger.error("查询问卷调查出错。", e);
			code = -1;
			msg = "查询失败，请稍后再试！";
			e.printStackTrace();
		}
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("data", arr);
		return rs;
	}
	
	@RequestMapping(value="/saveQuestionByUserDetails",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveQuestionByUserDetails(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		@SuppressWarnings("unchecked")
		List<JSONObject> recordDetailsList= (List<JSONObject>)request.get("recordDetail");
		String schoolId = request.getString("schoolId");
		String userId = request.getString("userId");
		Integer indexRow = request.getInteger("indexRow");
		String accountId = null;
		String classId = null;// String classId = ""; 手机端是null保存的。 mybits 没有判断 "" ,造成手机端提交一次  电脑端提交一次
		if (StringUtils.isNotBlank(schoolId) &&StringUtils.isNotBlank(userId) ) {
			User user = commonDataService.getUserById(Long.parseLong(schoolId), Long.parseLong(userId) );
			accountId = user.getAccountPart().getId()+"";
			 if(T_Role.Parent.equals(user.getUserPart().getRole())){
					classId = String.valueOf(user.getParentPart().getClassId());
			 }else if(T_Role.Student.equals(user.getUserPart().getRole())){
					classId = String.valueOf(user.getStudentPart().getClassId());
			 }
		}else {
			 schoolId = getXxdm(req);
			 accountId = req.getSession().getAttribute("accountId").toString();
			 User user = (User) req.getSession().getAttribute("user");
			 if(T_Role.Parent.equals(user.getUserPart().getRole())){
					classId = String.valueOf(user.getParentPart().getClassId());
					userId = String.valueOf(req.getSession().getAttribute("userId"));
			 }else if(T_Role.Student.equals(user.getUserPart().getRole())){
					classId = String.valueOf(user.getStudentPart().getClassId());
			 }
			 
		}
 
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("schoolId", schoolId);
		paramMap.put("accountId", accountId);
		paramMap.put("classId", classId);
		paramMap.put("userId", userId);
		paramMap.put("isSubmit",  1);
		paramMap.put("recordDetail", recordDetailsList);
		paramMap.put("indexRow", indexRow);
 		List<Long> accountIds = new ArrayList<Long>();
		accountIds.add(Long.parseLong(accountId));
		paramMap.put("accountIds", accountIds); 
		Integer saveNum = questionnaireService.saveQuestionByUserDetails(paramMap);
		if(saveNum > 0){
			msg = "提交成功！";
		}else {
			code = -1;
			msg = "提交失败！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/initQuestionnaireByUser",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject initQuestionnaireByUser(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：8）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		String classId = null;// 此处不做查询条件，所以为“” 的数据也能出来
		
		User user = (User) req.getSession().getAttribute("user");
		String userId = null;
		if(T_Role.Parent.equals(user.getUserPart().getRole())){
			classId = String.valueOf(user.getParentPart().getClassId());
			userId = String.valueOf(req.getSession().getAttribute("userId"));
		}else if(T_Role.Student.equals(user.getUserPart().getRole())){
			classId = String.valueOf(user.getStudentPart().getClassId());
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("questionId", questionId);
		try {
			List<JSONObject> jsonObjects = questionnaireService.queryQuestionList(paramMap);
			if(CollectionUtils.isNotEmpty(jsonObjects)){
				JSONObject jsonObject = jsonObjects.get(0);
				String questionType = jsonObject.getString("questionType");
				if("1".equals(questionType)){	// 表格题
					List<JSONObject> ruleForTable = questionnaireService.queryRuleForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(ruleForTable)){
						rs.put("ruleForTable", ruleForTable.get(0));
					}
					List<JSONObject> optionForTableList = questionnaireService.queryOptionForTableById(paramMap);
					if(CollectionUtils.isNotEmpty(optionForTableList)){
						rs.put("optionForTable", optionForTableList);
						rs.put("initRowLength", optionForTableList.get(optionForTableList.size()-1).getInteger("indexRow"));
					}
					HashMap<String, Object> recordMap = new HashMap<String, Object>();
					recordMap.put("questionId", questionId);
					recordMap.put("schoolId", schoolId);
					recordMap.put("accountId", accountId);
					recordMap.put("classId", classId);
					if(userId != null){
						recordMap.put("userId", userId);
					}
					List<JSONObject> recordList = questionnaireService.queryRecordByUser(recordMap);
					if(CollectionUtils.isNotEmpty(recordList) && recordList.size() > 0){
						JSONObject record = recordList.get(0);
						rs.put("record", record);
						HashMap<String, Object> recordDetailMap = new HashMap<String, Object>();
						recordDetailMap.put("recordId", record.get("recordId"));
						recordDetailMap.put("schoolId", schoolId);
						List<JSONObject> recordDetailList = questionnaireService.queryRecordDetailForTableByUser(recordDetailMap);
						if(CollectionUtils.isNotEmpty(recordDetailList) && recordDetailList.size() > 0){
							rs.put("recordDetail", recordDetailList);
						}
					}
				}else{	// 个性题
					List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
					if(CollectionUtils.isNotEmpty(targetS)){
						for(JSONObject target : targetS){
							if("1".equals(target.getString("targetType"))){
								HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
								String targetId = target.getString("targetId");
								paramMap2.put("targetId", targetId);
								List<JSONObject> targetLevels = questionnaireService.queryTargetLevelById(paramMap2);
								if(CollectionUtils.isNotEmpty(targetLevels)){
									target.put("level", targetLevels);
								}
							}
						}
						HashMap<String, Object> recordMap = new HashMap<String, Object>();
						recordMap.put("questionId", questionId);
						recordMap.put("schoolId", schoolId);
						recordMap.put("accountId", accountId);
						if(StringUtils.isNotEmpty(classId)) {
							recordMap.put("classId", classId);
						}
						if(StringUtils.isNotEmpty(userId)) {
							recordMap.put("userId", userId);
						}
						List<JSONObject> recordList = questionnaireService.queryRecordByUser(recordMap);
						if(CollectionUtils.isNotEmpty(recordList) && recordList.size() > 0){
							JSONObject record = recordList.get(0);
							HashMap<String, Object> paramMap3 = new HashMap<String, Object>();
							paramMap3.put("recordId", record.get("recordId"));
							paramMap3.put("schoolId", schoolId);
							List<JSONObject> specificS = questionnaireService.queryRecordForSpecific(paramMap3);
							List<JSONObject> subjectS = questionnaireService.querySubjectResultForSpecific(paramMap3);
							List<JSONObject> resultList = new ArrayList<JSONObject>();
							if(CollectionUtils.isNotEmpty(subjectS) && subjectS.size() > 0){
								for (JSONObject jsonObject2 : subjectS) {
									resultList.add(jsonObject2);
								}
							}
							if(CollectionUtils.isNotEmpty(specificS) && specificS.size() > 0){
								for (JSONObject jsonObject2 : specificS) {
									resultList.add(jsonObject2);
								}
							}
							rs.put("result", resultList);
						}
					}
					rs.put("target", targetS);
				}
				rs.put("question", jsonObject);
			}
		} catch (Exception e) {
			logger.error("查询问卷调查出错。", e);
			code = -1;
			msg = "查询异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/saveQuestionByUserDetailsToGx",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveQuestionByUserDetailsToGx(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		List<JSONObject> targetZgt= (List<JSONObject>)request.get("targetZgt");
		List<JSONObject> targetKgt= (List<JSONObject>)request.get("targetKgt");
		String schoolId = getXxdm(req);
		String classId = null;// String classId = ""; 手机端是null保存的。 mybits 没有判断 "" ,造成手机端提交一次  电脑端提交一次
		String accountId = req.getSession().getAttribute("accountId").toString();
		
		User user = (User) req.getSession().getAttribute("user");
		String userId = "";
		if(T_Role.Parent.equals(user.getUserPart().getRole())){
			classId = String.valueOf(user.getParentPart().getClassId());
			userId = String.valueOf(req.getSession().getAttribute("userId"));
		}else if(T_Role.Student.equals(user.getUserPart().getRole())){
			classId = String.valueOf(user.getStudentPart().getClassId());
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("schoolId", schoolId);
		paramMap.put("accountId", accountId);
		paramMap.put("userId", userId);
		paramMap.put("classId", classId);
		paramMap.put("targetZgt", targetZgt);
		paramMap.put("targetKgt", targetKgt);
		Integer saveNum = questionnaireService.saveQuestionByUserDetailsToGx(paramMap);
		if(saveNum > 0){
			msg = "提交成功！";
		}else {
			code = -1;
			msg = "提交失败！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/queryQuestionResult",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryQuestionResult(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：9）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String classId = request.getString("classId");
		String gradeId = request.getString("gradeId");
		if(StringUtils.isNotBlank(classId) && !"-1".equals(classId)){
			gradeId = "";
		}
		String schoolId = getXxdm(req);
		String accountId = req.getSession().getAttribute("accountId").toString();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		paramMap.put("schoolId", schoolId);
		paramMap.put("createAccount", accountId);
		paramMap.put("questionId", questionId);
		Integer type = null;
		List<JSONObject> queryObInfo = questionnaireService.queryObInfo(paramMap);
		List<Long> classIds = new ArrayList<Long>();
		if(CollectionUtils.isNotEmpty(queryObInfo) && queryObInfo.size() > 0){
			type = queryObInfo.get(0).getInteger("objType");
			if(type != 1){
				for (JSONObject jsonObject6 : queryObInfo) {
					classIds.add(jsonObject6.getLong("classId"));
				}
			}
		}
		
		Map<Long, String> classNameMap = new HashMap<Long, String>();
		List<Classroom> classroomBatchNoAccount = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), classIds, termInfoId);
		if(CollectionUtils.isNotEmpty(classroomBatchNoAccount) && classroomBatchNoAccount.size() > 0){
			for (Classroom classroom : classroomBatchNoAccount) {
				classNameMap.put(classroom.getId(), classroom.getClassName());
			}
		}
		List<JSONObject> jsonObjects = questionnaireService.queryQuestionList(paramMap);
		if(CollectionUtils.isNotEmpty(jsonObjects) && jsonObjects.size() > 0){
			JSONObject jsonObject = jsonObjects.get(0);
			String questionType = jsonObject.getString("questionType");
			if("1".equals(questionType)){
				List<JSONObject> ruleForTable = questionnaireService.queryRuleForTableById(paramMap);
				if(CollectionUtils.isNotEmpty(ruleForTable)){
					rs.put("ruleForTable", ruleForTable.get(0));
				}
				List<JSONObject> optionForTableList = questionnaireService.queryOptionForTableById(paramMap);
				if(CollectionUtils.isNotEmpty(optionForTableList)){
					rs.put("optionForTable", optionForTableList);
					rs.put("initRowLength", optionForTableList.get(optionForTableList.size()-1).getInteger("indexRow"));
				}
				if(StringUtils.isNotBlank(classId) && !"-1".equals(classId)){
					List<String> classIdsa = new ArrayList<String>();
					classIdsa.add(classId);
					paramMap.put("classIds", classIdsa);
				}else if(StringUtils.isNotBlank(gradeId)  && !"-1".equals(gradeId)){
					Grade grade = commonDataService.getGradeById(Long.valueOf(schoolId), Long.valueOf(gradeId), termInfoId);
					if(grade != null && CollectionUtils.isNotEmpty(grade.getClassIds())){
						paramMap.put("classIds", grade.getClassIds());
					}
				}
				List<JSONObject> recordTableResult = questionnaireService.queryRecordTableResult(paramMap);
				if(CollectionUtils.isNotEmpty(recordTableResult) && recordTableResult.size() > 0){
					rs.put("recordTableResult", recordTableResult);
				}
			}else{
				List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);
				if(CollectionUtils.isNotEmpty(targetS)){
					for(JSONObject target : targetS){
						if("1".equals(target.getString("targetType"))){
							HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
							String targetId = target.getString("targetId");
							paramMap2.put("targetId", targetId);
							List<JSONObject> targetLevels = questionnaireService.queryTargetLevelById(paramMap2);
							if(CollectionUtils.isNotEmpty(targetLevels)){
								target.put("level", targetLevels);
							}
						}
					}
					HashMap<String, Object> recordMap = new HashMap<String, Object>();
					recordMap.put("questionId", questionId);
					recordMap.put("schoolId", schoolId);
					if(StringUtils.isNotBlank(classId) && !"-1".equals(classId)){
						List<String> classIdsa = new ArrayList<String>();
						classIdsa.add(classId);
						recordMap.put("classIds", classIdsa);
					}else if(StringUtils.isNotBlank(gradeId) && !"-1".equals(gradeId)){
						Grade grade = commonDataService.getGradeById(Long.valueOf(schoolId), Long.valueOf(gradeId), termInfoId);
						if(grade != null && CollectionUtils.isNotEmpty(grade.getClassIds())){
							recordMap.put("classIds", grade.getClassIds());
						}
					}
					HashMap<String, List<JSONObject>> hashMap = new HashMap<String, List<JSONObject>>(); 
					List<JSONObject> recordResultList = questionnaireService.queryRecordForSpecificResult(recordMap);
					if(CollectionUtils.isNotEmpty(recordResultList) && recordResultList.size() > 0){
						for(int index = 0;index<recordResultList.size();index++){
							JSONObject jsonObject2 = recordResultList.get(index);
							if(jsonObject2.containsKey("targetId")){
								String targetId = jsonObject2.getString("targetId");
								if(hashMap.containsKey(targetId)){
									hashMap.get(targetId).add(jsonObject2);
								}else{
									List<JSONObject> list = new ArrayList<JSONObject>();
									list.add(jsonObject2);
									hashMap.put(targetId, list);
								}
							}
						}
						for(int index = 0;index<recordResultList.size();index++){
							JSONObject jsonObject2 = recordResultList.get(index);
							if(jsonObject2.containsKey("targetId")){
								Integer totalNum = 0;
								String targetId = jsonObject2.getString("targetId");
								List<JSONObject> list = hashMap.get(targetId);
								for(JSONObject jsonObject3 : list){
									totalNum = totalNum +jsonObject3.getInteger("levelNum");
								}
								jsonObject2.put("totalNum", totalNum);
							}
							
						}
					}
				
					List<JSONObject> subjectResultList = questionnaireService.querySubjectForSpecificResult(recordMap);
					if(CollectionUtils.isNotEmpty(subjectResultList)){
						Map<Long, Set<Long>> accId2UserIds = new HashMap<Long, Set<Long>>();
						for (JSONObject json : subjectResultList) {
							Long accId = json.getLong("accountId");
							if(accId == null){
								continue;
							}
							if(!accId2UserIds.containsKey(accId)){
								accId2UserIds.put(accId, new HashSet<Long>());
							}
							Long userId = json.getLong("userId");
							if(type == 3 && userId != null){
								accId2UserIds.get(accId).add(userId);
							}
						}
						List<Account> accountBatch = commonDataService.getAccountBatch(Long.valueOf(schoolId), 
								new ArrayList<Long>(accId2UserIds.keySet()), termInfoId);
						
						Map<Long, String> id2Name = new HashMap<Long, String>();
						Map<Long, Long> id2ClassId = new HashMap<Long, Long>();
						if(CollectionUtils.isNotEmpty(accountBatch)){
							for(Account acc : accountBatch){
								long accId = acc.getId();
								if(type == 1){	// 老师，accId对应相应的名称
									id2Name.put(accId, acc.getName());
								}else if(type == 2){	// 学生，accId对应相应的名称
									id2Name.put(accId, acc.getName());
									List<User> users = acc.getUsers();
									if(CollectionUtils.isEmpty(users)){
										continue;
									}
									for(User user : users){
										if(T_Role.Student.equals(user.getUserPart().getRole())){
											id2ClassId.put(accId, user.getStudentPart().getClassId());
											break;
										}
									}
								}else if(type == 3){	// 家长，userId对应相应的名称
									Set<Long> userIds = accId2UserIds.get(accId);
									List<User> users = acc.getUsers();
									if(CollectionUtils.isEmpty(users)){
										continue;
									}
									for(User user : users){
										if(T_Role.Parent.equals(user.getUserPart().getRole()) && 
												userIds.contains(user.getParentPart().getId())){
											long userId = user.getParentPart().getId();
											id2Name.put(userId, user.getParentPart().getStudentName() + "家长");
											id2ClassId.put(userId, user.getParentPart().getClassId());
										}
									}
								}
							}
						}
						
						for(JSONObject json : subjectResultList){
							Long accId = json.getLong("accountId");
							if(accId == null){
								continue;
							}
							if(type == 1){
								json.put("userName", id2Name.get(accId));
								
							}else if(type == 2){
								json.put("userName", id2Name.get(accId));
								Long cId = id2ClassId.get(accId);
								if(cId == null){
									json.put("className", "");
								}else{
									json.put("className", classNameMap.get(cId));
								}
							}else if(type == 3){
								Long userId = json.getLong("userId");
								if(userId == null){
									continue;
								}
								json.put("userName", id2Name.get(userId));
								Long cId = id2ClassId.get(userId);
								if(cId == null){
									json.put("className", "");
								}else{
									json.put("className", classNameMap.get(cId));
								}
							}
						}
						
//						if(type == 2 || type == 1){
//							for (JSONObject jsonObject2 : subjectResultList) {
//								if(jsonObject2.containsKey("accountId")){
//									for(int index = 0;index < accountBatch.size();index++){
//										Account account = accountBatch.get(index);
//										if(jsonObject2.getLong("accountId").equals(account.getId())){
//											jsonObject2.put("userName", account.getName());
//											if("2".equals(type)){
//												if(CollectionUtils.isNotEmpty(account.getUsers())){
//													for(User user : account.getUsers()){
//														if(T_Role.Student.equals(user.getUserPart().getRole())){
//															jsonObject2.put("className", classNameMap.get(user.getStudentPart().getClassId()));
//															break;
//														}
//													}
//												}
//											}else if("3".equals(type)){
//												if(CollectionUtils.isNotEmpty(account.getUsers())){
//													for(User user : account.getUsers()){
//														if(T_Role.Parent.equals(user.getUserPart().getRole())){
//															jsonObject2.put("className", classNameMap.get(user.getParentPart().getClassId()));
//															break;
//														}
//													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}else{
//							List<Account> accountBatch = commonDataService.getAccountBatch(Long.valueOf(schoolId), ids, termInfoId);
//							List<Long> studentIds = new ArrayList<Long>();
//							if(CollectionUtils.isNotEmpty(accountBatch)){
//								for (Account account : accountBatch) {
//									if(CollectionUtils.isEmpty(account.getUsers())){
//										continue;
//									}
//									if(CollectionUtils.isNotEmpty(account.getUsers())){
//										for(User user : account.getUsers()){
//											if(T_Role.Parent.equals(user.getUserPart().getRole())){
//												long studentId = user.getParentPart().getStudentId();
//												studentIds.add(studentId);
//												for (JSONObject jsonObject2 : subjectResultList) {
//													if(jsonObject2.getLong("accountId").equals(account.getId())){
//														jsonObject2.put("accountId", studentId);
//													}
//												}
//												break;
//											}
//										}
//									}
//								}
//							}
//							if(CollectionUtils.isNotEmpty(studentIds)){
//								List<User> users = commonDataService.getUserBatch(Long.valueOf(schoolId), studentIds);
//								Map<Long, User> id2UserMap = new HashMap<Long, User>();
//								if(CollectionUtils.isNotEmpty(users)){
//									for(User user : users){
//										id2UserMap.put(user.getUserPart().getId(), user);
//									}
//								}
//								
//								for(JSONObject jsonObject2 : subjectResultList){
//									long accId = jsonObject2.getLongValue("accountId");
//									String userName = "";
//									String className = "";
//									
//									if(id2UserMap.containsKey(accId)){
//										User user = id2UserMap.get(accId);
//										userName = user.getAccountPart().getName() + "家长";
//										long stuClassId = user.getStudentPart().getClassId();
//										if(classNameMap.containsKey(stuClassId)){
//											className = classNameMap.get(stuClassId);
//										}
//									}
//									jsonObject2.put("userName", userName);
//									jsonObject2.put("className", className);
//								}
//							}
//						}
					}
					List<JSONObject> subjectMaxList = questionnaireService.querySubjectResultMax(recordMap);
					rs.put("recordResult", recordResultList);
					rs.put("subjectResult", subjectResultList);
					rs.put("subjectMax", subjectMaxList);
				}
				rs.put("target", targetS);
			}
			rs.put("question", jsonObject);
		}
		rs.put("type", type);
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/queryStatisticsResultAccToVoter",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryStatisticsResultAccToVoter(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject response = new JSONObject();
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			response.put("code", -1);
			response.put("msg", "参数传递错误（代码：10）！");
			return response;
		}
		String schoolId = getXxdm(req);
		if(StringUtils.isEmpty(schoolId)){
			response.put("code", -1);
			response.put("msg", "获取学校id失败，请联系管理员！");
			return response;
		}
		try{
			response.put("data", questionnaireService.queryStatisticsResultAccToVoter(request, Long.parseLong(schoolId)));
			response.put("code", 0);
			response.put("msg", "");
		}catch(RuntimeException e){
			e.printStackTrace();
			response.put("code", -1);
			response.put("msg", e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			response.put("code", -1);
			response.put("msg", "后台出错，请联系管理员！");
			logger.error(e.getMessage());
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/showResultUserName",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject showResultUserName(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String termInfoId = request.getString("termInfoId");
		String questionId = request.getString("questionId");
		String tableOptionIds = request.getString("tableOptionId");
		String teacherType = request.getString("teacherType");
		String schoolId = getXxdm(req);
		String[] split = tableOptionIds.split(",");
		String type = "1";
		List<String> list = new ArrayList<String>();
		for(String tableOptionId : split){
			if(StringUtils.isNotBlank(tableOptionId)){
				list.add(tableOptionId);
			}
		}
		Set<Long> classIdas = new HashSet<Long>();
		if(StringUtils.isNotBlank(teacherType) && "1".equals(teacherType)){
			String accountId = req.getSession().getAttribute("accountId").toString();
			Account account = commonDataService.getAccountAllById(Long.valueOf(accountId));
			List<User> users2 = account.getUsers();
			if(CollectionUtils.isNotEmpty(users2)){
				for(User user : users2){
					if(T_Role.Teacher.equals(user.getUserPart().getRole())){
						TeacherPart teacherPart = user.getTeacherPart();
						List<Long> deanOfClassIds = teacherPart.getDeanOfClassIds();
						if(CollectionUtils.isNotEmpty(deanOfClassIds)){
							classIdas.addAll(deanOfClassIds);
						}
						break;
					}
				}
			}
		}
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("schoolId", schoolId);
		paramMap.put("tableOptionIds", list);
		paramMap.put("questionId", questionId);
		List<JSONObject> queryObInfo = questionnaireService.queryObInfo(paramMap);
		Set<Long> classIds = new HashSet<Long>();
		if(CollectionUtils.isNotEmpty(queryObInfo) && queryObInfo.size() > 0){
			type = queryObInfo.get(0).getString("objType");
			if(!"1".equals(type)){
				for (JSONObject jsonObject : queryObInfo) {
					classIds.add(jsonObject.getLong("classId"));
				}
			}
		}
		List<Account> accountBatch = null;
		List<JSONObject> recordTableResultList = questionnaireService.queryRecordTableResultAccountId(paramMap);
		Map<Long, Set<Long>> accId2UserIds = new HashMap<Long, Set<Long>>();
		if(CollectionUtils.isNotEmpty(recordTableResultList)){
			for (JSONObject jsonObject : recordTableResultList) {
				Long accId = jsonObject.getLong("accountId");
				if(!accId2UserIds.containsKey(accId)){
					accId2UserIds.put(accId, new HashSet<Long>());
				}
				Long userId = jsonObject.getLong("userId");
				if("3".equals(type)){ // 家长才有userId
					accId2UserIds.get(accId).add(userId);
				}
			}
			accountBatch = commonDataService.getAccountBatch(Long.valueOf(schoolId), new ArrayList<Long>(accId2UserIds.keySet()), termInfoId);
		}
		
		if("1".equals(type)){	// 老师
			List<JSONObject> jsonName = new ArrayList<JSONObject>();
			if(CollectionUtils.isNotEmpty(accountBatch) && accountBatch.size() > 0){
				for (Account account : accountBatch) {
					String userName = account.getName();
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("userName", userName);
					jsonObject.put("userId", account.getId());
					jsonName.add(jsonObject);
				}
			}
			rs.put("userList", jsonName);
		}else{	// 家长和学生
			Map<Long, Integer> yesTotalCount = new HashMap<Long, Integer>();
			if(CollectionUtils.isNotEmpty(classIds)){
				if(CollectionUtils.isNotEmpty(accountBatch)){
					for (Account account : accountBatch) {
						List<User> users = account.getUsers();
						if(CollectionUtils.isEmpty(users)){
							continue;
						}
						if("2".equals(type)){ // 学生
							for(User user: users){
								if(!T_Role.Student.equals(user.getUserPart().getRole())){
									continue;
								}
								Long classId = user.getStudentPart().getClassId();
								if(!yesTotalCount.containsKey(classId)){
									yesTotalCount.put(classId, 0);
								}
								Integer integer = yesTotalCount.get(classId);
								yesTotalCount.put(classId, ++integer);
								break;
							}
						}else if("3".equals(type)){	// 家长
							Set<Long> userIdSet = accId2UserIds.get(account.getId());
							for(User user: users){
								if(!T_Role.Parent.equals(user.getUserPart().getRole())){
									continue;
								}
								Long userId = user.getParentPart().getId();
								if(!userIdSet.contains(userId)){
									continue;
								}
								Long classId = user.getParentPart().getClassId();
								if(!yesTotalCount.containsKey(classId)){
									yesTotalCount.put(classId, 0);
								}
								Integer integer = yesTotalCount.get(classId);
								yesTotalCount.put(classId, ++integer);
								// 不能break，因为可能一个家长对应了多个学生
							}
						}else{
							throw new RuntimeException("填写考评人错误，请联系管理员！");
						}
					}
				}
				List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), new ArrayList<Long>(classIds), termInfoId);
				Map<Long, List<JSONObject>> gradeMap = new HashMap<Long, List<JSONObject>>();
				Map<String, List<JSONObject>> returnMap = new HashMap<String, List<JSONObject>>();
				if(CollectionUtils.isNotEmpty(classroomBatch)){
					List<Long> gradeIds = new ArrayList<Long>();
					for (Classroom classroom : classroomBatch) {
						long gradeId = classroom.getGradeId();
						JSONObject classJson = new JSONObject();
						List<Long> studentIds = classroom.getStudentIds();
						int count = 0;
						if(CollectionUtils.isNotEmpty(studentIds)){
							count = studentIds.size();
						}
						classJson.put("classId", String.valueOf(classroom.getId()));
						classJson.put("className", classroom.getClassName());
						classJson.put("totalCount", count);
						if(yesTotalCount.containsKey(classroom.getId())){
							classJson.put("yesTotalCount", yesTotalCount.get(classroom.getId()));
						}else{
							classJson.put("yesTotalCount", 0);
						}
						if(StringUtils.isNotBlank(teacherType) && "1".equals(teacherType)){
							if(CollectionUtils.isNotEmpty(classIdas)){
								if(classIdas.contains(classroom.getId())){
									if(gradeMap.containsKey(gradeId)){
										gradeMap.get(gradeId).add(classJson);
									}else{
										List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
										jsonObjects.add(classJson);
										gradeMap.put(gradeId, jsonObjects);
										gradeIds.add(gradeId);
									}
								}
							}
						}else{
							if(gradeMap.containsKey(gradeId)){
								gradeMap.get(gradeId).add(classJson);
							}else{
								List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
								jsonObjects.add(classJson);
								gradeMap.put(gradeId, jsonObjects);
								gradeIds.add(gradeId);
							}
						}
					}
					if(CollectionUtils.isNotEmpty(gradeIds)){
						Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
						List<Grade> gradeBatch = commonDataService.getGradeBatch(Long.valueOf(schoolId), gradeIds, termInfoId);
						if(CollectionUtils.isNotEmpty(gradeBatch)){
							for (Grade grade : gradeBatch) {
								String string = njName.get(grade.getCurrentLevel());
								List<JSONObject> arrayList = gradeMap.get(grade.getId());
								try {
									arrayList = (ArrayList<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, arrayList, "className");
								} catch (Exception e) {
									e.printStackTrace();
								}
								returnMap.put(string, arrayList);
							}
						}
					}
				}
				returnMap = sortMapByKey(returnMap);
				rs.put("userList", returnMap);
			}
		}
//		
//		
//		
//		if(!"1".equals(type)){
//			Map<Long, Integer> yesTotalCount = new HashMap<Long, Integer>();
//			if(CollectionUtils.isNotEmpty(classIds)){
//				if(CollectionUtils.isNotEmpty(accountBatch)){
//					for (Account account : accountBatch) {
//						Set<Long> userIdSet = accId2UserIds.get(account.getId());
//						List<User> users = account.getUsers();
//						if(CollectionUtils.isNotEmpty(users)){
//							for(User user: users){
//								Long classId = null;
//								Long userId = user.getUserPart().getId();
//								if(userIdSet.contains(userId)){
//									if("2".equals(type) && T_Role.Student.equals(user.getUserPart().getRole())){
//										classId = user.getStudentPart().getClassId();
//									}else if("3".equals(type) && T_Role.Parent.equals(user.getUserPart().getRole())){
//										classId = user.getParentPart().getClassId();
//									}
//									if(!yesTotalCount.containsKey(classId)){
//										yesTotalCount.put(classId, 0);
//									}
//									Integer integer = yesTotalCount.get(classId);
//									yesTotalCount.put(classId, ++integer);
//								}
//							}
//						}
//					}
//				}
//				List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), new ArrayList<Long>(classIds), termInfoId);
//				Map<Long, List<JSONObject>> gradeMap = new HashMap<Long, List<JSONObject>>();
//				Map<String, List<JSONObject>> returnMap = new HashMap<String, List<JSONObject>>();
//				if(CollectionUtils.isNotEmpty(classroomBatch)){
//					List<Long> gradeIds = new ArrayList<Long>();
//					for (Classroom classroom : classroomBatch) {
//						long gradeId = classroom.getGradeId();
//						JSONObject classJson = new JSONObject();
//						List<Long> studentIds = classroom.getStudentIds();
//						int count = 0;
//						if(CollectionUtils.isNotEmpty(studentIds)){
//							count = studentIds.size();
//						}
//						classJson.put("classId", String.valueOf(classroom.getId()));
//						classJson.put("className", classroom.getClassName());
//						classJson.put("totalCount", count);
//						if(yesTotalCount.containsKey(classroom.getId())){
//							classJson.put("yesTotalCount", yesTotalCount.get(classroom.getId()));
//						}else{
//							classJson.put("yesTotalCount", 0);
//						}
//						if(StringUtils.isNotBlank(teacherType) && "1".equals(teacherType)){
//							boolean flag = false;
//							if(CollectionUtils.isNotEmpty(classIdas) && classIdas.size() > 0){
//								for (Long classId : classIdas) {
//									if(classId.equals(classroom.getId())){
//										flag = true;
//										break;
//									}
//								}
//							}
//							if(flag){
//								if(gradeMap.containsKey(gradeId)){
//									gradeMap.get(gradeId).add(classJson);
//								}else{
//									List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//									jsonObjects.add(classJson);
//									gradeMap.put(gradeId, jsonObjects);
//									gradeIds.add(gradeId);
//								}
//							}
//						}else{
//							if(gradeMap.containsKey(gradeId)){
//								gradeMap.get(gradeId).add(classJson);
//							}else{
//								List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
//								jsonObjects.add(classJson);
//								gradeMap.put(gradeId, jsonObjects);
//								gradeIds.add(gradeId);
//							}
//						}
//					}
//					if(CollectionUtils.isNotEmpty(gradeIds)){
//						Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
//						List<Grade> gradeBatch = commonDataService.getGradeBatch(Long.valueOf(schoolId), gradeIds, termInfoId);
//						if(CollectionUtils.isNotEmpty(gradeBatch)){
//							for (Grade grade : gradeBatch) {
//								String string = njName.get(grade.getCurrentLevel());
//								List<JSONObject> arrayList = gradeMap.get(grade.getId());
//								try {
//									arrayList = (ArrayList<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, arrayList, "className");
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//								returnMap.put(string, arrayList);
//							}
//						}
//					}
//				}
//				returnMap = sortMapByKey(returnMap);
//				rs.put("userList", returnMap);
//			}
//			
//		}else{
//			List<JSONObject> jsonName = new ArrayList<JSONObject>();
//			if(CollectionUtils.isNotEmpty(accountBatch) && accountBatch.size() > 0){
//				for (Account account : accountBatch) {
//					String userName = account.getName();
//					JSONObject jsonObject = new JSONObject();
//					jsonObject.put("userName", userName);
//					jsonObject.put("userId", account.getId());
//					jsonName.add(jsonObject);
//				}
//			}
//			rs.put("userList", jsonName);
//		}
		rs.put("type", type);
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/showResultUserNameByWcqk",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject showResultUserNameByWcqk(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		// zy
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		long schoolId = Long.parseLong(getXxdm(req));
		Long gradeId  = request.getLong("gradeId");
		Long classId = request.getLong("classId");
		
		JSONObject params = new JSONObject();
		params.put("schoolId", schoolId);
		params.put("questionId", questionId);
		params.put("termInfoId", termInfoId);
		params.put("gradeId", gradeId);
		params.put("classId", classId);
		return questionnaireService.showResultUserNameByWcqk(params);
	}	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/saveQuestionDcdx",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveQuestionDcdx(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		List<JSONObject> selectedTeachers = (List<JSONObject>)request.get("selectedTeachers");
		String type = request.getString("type");
		String schoolId = getXxdm(req);
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("schoolId", schoolId);
		try {
			List<JSONObject> queryObInfo = questionnaireService.queryObInfo(paramMap);
			if(CollectionUtils.isNotEmpty(queryObInfo) && queryObInfo.size() > 0){
				String oldType = queryObInfo.get(0).getString("objType");
				Integer deleteCount = questionnaireService.deleteObjInfo(paramMap);
				if(deleteCount > 0){
					List<Long> allIds = new ArrayList<Long>();
					List<Long> newIds = new ArrayList<Long>();
					List<Long> oldIds = new ArrayList<Long>();
					if(type.equals(oldType)){
						if("2".equals(oldType) || "3".equals(oldType)){
							for (JSONObject jsonObject : queryObInfo) {
								Long classId = jsonObject.getLong("classId");
								oldIds.add(classId);
							}
							for (JSONObject jsonObject : selectedTeachers) {
								Long classId = jsonObject.getLong("classId");
								newIds.add(classId);
							}
						}else{
							for (JSONObject jsonObject : queryObInfo) {
								Long accountId = jsonObject.getLong("accountId");
								oldIds.add(accountId);
							}
							for (JSONObject jsonObject : selectedTeachers) {
								Long accountId = jsonObject.getLong("teacherId");
								newIds.add(accountId);
							}
						}
						for (Long long1 : oldIds) {
							boolean flag = true;
							for (Long long2 : newIds) {
								if(long1.equals(long2)){
									flag = false;
									break;
								}
							}
							if(flag){
								allIds.add(long1);
							}
						}
					}else{
						questionnaireService.deleteRecordByQuestionId(paramMap);
					}
					
					if(CollectionUtils.isNotEmpty(allIds) && allIds.size() > 0){
						if("2".equals(oldType) || "3".equals(oldType)){
							paramMap.put("classIds", allIds);
							questionnaireService.deleteRecordByQuestionId(paramMap);
						}else{
							paramMap.put("accountIds", allIds);
							questionnaireService.deleteRecordByQuestionId(paramMap);
						}
					}
				}
			}
			for(JSONObject jsonObject : selectedTeachers){
				jsonObject.put("accountId", jsonObject.get("teacherId"));
				jsonObject.put("objId", UUIDUtil.getUUID());
				jsonObject.put("questionId", questionId);
				jsonObject.put("useGrade", "");
				if("2".equals(type) || "3".equals(type)){
					jsonObject.put("classId", jsonObject.getString("classId"));
				}
				jsonObject.put("schoolId", schoolId);
				jsonObject.put("objType", type);
			}
			paramMap.put("objs", selectedTeachers);
			if(CollectionUtils.isNotEmpty(selectedTeachers) && selectedTeachers.size() > 0){
				questionnaireService.saveObjInfo(paramMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			code = -1;
			msg = "保存异常，请稍后再试！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@RequestMapping(value="/queryObInfo",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryObInfo(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String termInfoId = request.getString("termInfoId");
		String questionId = request.getString("questionId");
		String type = request.getString("type");
		String schoolId = getXxdm(req);
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("schoolId", schoolId);
		paramMap.put("type", type);
		List<JSONObject> queryObInfo = questionnaireService.queryObInfo(paramMap);
		if(CollectionUtils.isNotEmpty(queryObInfo) && queryObInfo.size() > 0){
			JSONObject jsonObject2 = queryObInfo.get(0);
			type = jsonObject2.getString("objType");
			List<Long> ids = new ArrayList<Long>();
			if("1".equals(type)){
				for (JSONObject jsonObject : queryObInfo) {
					ids.add(jsonObject.getLong("accountId"));
				}
				if(CollectionUtils.isNotEmpty(ids) && ids.size() > 0){
					List<Account> accountBatch = commonDataService.getAccountBatch(Long.valueOf(schoolId), ids , termInfoId);
					if(CollectionUtils.isNotEmpty(accountBatch) && accountBatch.size() > 0){
						for (JSONObject jsonObject : queryObInfo) {
							for (Account account : accountBatch) {
								if(jsonObject.containsKey("accountId")){
									if(jsonObject.getLong("accountId").equals(account.getId())){
										jsonObject.put("teacherName", account.getName());
									}
								}
							}
						}
					}
				}
			}else{
				for (JSONObject jsonObject : queryObInfo) {
					ids.add(jsonObject.getLong("classId"));
				}
				if(CollectionUtils.isNotEmpty(ids) && ids.size() > 0){
					List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), ids , termInfoId);
					if(CollectionUtils.isNotEmpty(classroomBatch) && classroomBatch.size() > 0){
						for (JSONObject jsonObject : queryObInfo) {
							for (Classroom classroom : classroomBatch) {
								if(jsonObject.containsKey("classId")){
									if(jsonObject.getLong("classId").equals(classroom.getId())){
										jsonObject.put("className", classroom.getClassName());
									}
								}
							}
						}
					}
				}
			}
		}
		List<JSONObject> selectedTeachers = new ArrayList<JSONObject>();
		if("1".equals(type)){
			for (JSONObject jsonObject : queryObInfo) {
				if(jsonObject.containsKey("accountId") && jsonObject.containsKey("teacherName") && jsonObject.containsKey("nodeId")){
					JSONObject selected = new JSONObject();
					selected.put("nodeId", jsonObject.getString("nodeId"));
					selected.put("teacherName", jsonObject.getString("teacherName"));
					selected.put("teacherId", jsonObject.getString("accountId"));
					selectedTeachers.add(selected);
				}
			}
		}else{
			for (JSONObject jsonObject : queryObInfo) {
				if(jsonObject.containsKey("classId") && jsonObject.containsKey("className") && jsonObject.containsKey("nodeId")){
					JSONObject selected = new JSONObject();
					selected.put("nodeId", jsonObject.getString("nodeId"));
					selected.put("className", jsonObject.getString("className"));
					selected.put("classId", jsonObject.getString("classId"));
					selectedTeachers.add(selected);
				}
			}
		}
		rs.put("selectedTeachers", selectedTeachers);
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("type", type);
		return rs;
	}
	
	@SuppressWarnings("unused")
	@RequestMapping(value="/deleteQuestionById",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteQuestionById(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		if(StringUtils.isEmpty(termInfoId) || StringUtils.isEmpty(questionId)){
			rs.put("code", -1);
			rs.put("msg", "参数传递错误（代码：11）！");
			return rs;
		}
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		String gradeId = request.getString("gradeId");
		String schoolId = getXxdm(req);
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("questionId", questionId);
		paramMap.put("schoolId", schoolId);
		paramMap.put("yearId", xn);
		paramMap.put("termId", xq);
		try {
			Integer deleteNum = questionnaireService.deleteQuestionById(paramMap);
			if(deleteNum > 0){
				msg = "删除成功！";
			}else{
				code = -1;
				msg = "删除失败！";
			}
		} catch (Exception e) {
			code = -1;
			msg = "删除失败！";
		}
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryQueByClass",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryQueByClass(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		String msg = "";
		int code = 0;
		String questionId = request.getString("questionId");
		String gradeId = request.getString("gradeId");
		String type = request.getString("type");
		String schoolId = getXxdm(req);
		String termInfoId = request.getString("termInfoId");
		if("2".equals(type)){
			List<JSONObject> classObjects = new ArrayList<JSONObject>();
			List<JSONObject> gradeObjects = new ArrayList<JSONObject>();
			List<Long> gradeIds = new ArrayList<Long>();
			String accountId = req.getSession().getAttribute("accountId").toString();
			Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(accountId), termInfoId);
			List<User> users = account.getUsers();
			if(CollectionUtils.isNotEmpty(users)){
				for(User user : users){
					if(T_Role.Teacher.equals(user.getUserPart().getRole())){
						TeacherPart teacherPart = user.getTeacherPart();
						List<Long> deanOfClassIds = teacherPart.getDeanOfClassIds();
						if(CollectionUtils.isNotEmpty(deanOfClassIds)){
							List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), deanOfClassIds, termInfoId);
							if(CollectionUtils.isNotEmpty(classroomBatch)){
								for (Classroom classroom : classroomBatch) {
									if(!gradeIds.contains(classroom.getGradeId())){
										gradeIds.add(classroom.getGradeId());
									}
									JSONObject jsonObject = new JSONObject();
									jsonObject.put("gradeId", String.valueOf(classroom.getGradeId()));
									jsonObject.put("classId", String.valueOf(classroom.getId()));
									jsonObject.put("className", classroom.getClassName());
									classObjects.add(jsonObject);
								}
							}
						}
						if(CollectionUtils.isNotEmpty(gradeIds)){
							List<Grade> gradeBatch = commonDataService.getGradeBatch(Long.valueOf(schoolId), gradeIds, termInfoId);
							Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
							if(CollectionUtils.isNotEmpty(gradeBatch) && gradeBatch.size() > 0){
								for (Grade grade : gradeBatch) {
									String string = njName.get(grade.getCurrentLevel());
									JSONObject jsonObject1 = new JSONObject();
									jsonObject1.put("gradeId", String.valueOf(grade.getId()));
									jsonObject1.put("gradeName", string);
									gradeObjects.add(jsonObject1);
								}
							}
						}
						break;
					}
				}
			}
			rs.put("code", code);
			rs.put("msg", msg);
			try {
				classObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, classObjects, "gradeId,className");
				gradeObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, gradeObjects, "gradeName");
			} catch (Exception e) {
				e.printStackTrace();
			}
			rs.put("classObjects", classObjects);
			rs.put("gradeObjects", gradeObjects);
		}else{
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("questionId", questionId);
			paramMap.put("schoolId", schoolId);
			List<JSONObject> queryObInfo = questionnaireService.queryObInfo(paramMap);
			List<Long> classIds = new ArrayList<Long>();
			if(CollectionUtils.isNotEmpty(queryObInfo) && queryObInfo.size() > 0){
				for (JSONObject jsonObject : queryObInfo) {
					classIds.add(jsonObject.getLong("classId"));
				}
			}
			List<JSONObject> classObjects = new ArrayList<JSONObject>();
			JSONObject cObject = new JSONObject();
			cObject.put("gradeId", "-1");
			cObject.put("classId", "-1");
			cObject.put("className", "全部");
			//classObjects.add(cObject);
			List<JSONObject> gradeObjects = new ArrayList<JSONObject>();
			JSONObject gObject = new JSONObject();
			gObject.put("gradeId", "-1");
			gObject.put("gradeName", "全部");
			//gradeObjects.add(gObject);
			List<Long> gradeIds = new ArrayList<Long>();
			if(CollectionUtils.isNotEmpty(classIds) && classIds.size() > 0){
				List<Classroom> classroomBatch = commonDataService.getClassroomBatchNoAccount(Long.valueOf(schoolId), classIds, termInfoId);
				if(CollectionUtils.isNotEmpty(classroomBatch) && classroomBatch.size() > 0){
					for (Classroom classroom : classroomBatch) {
						if(!"-1".equals(gradeId)){
							if(gradeId.equals(String.valueOf(classroom.getGradeId()))){
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("gradeId", String.valueOf(classroom.getGradeId()));
								jsonObject.put("classId", String.valueOf(classroom.getId()));
								jsonObject.put("className", classroom.getClassName());
								classObjects.add(jsonObject);
							}
						}else{
							if(!gradeIds.contains(classroom.getGradeId())){
								gradeIds.add(classroom.getGradeId());
							}
						}
					}
				}
			}
			if("-1".equals(gradeId)){
				if(CollectionUtils.isNotEmpty(gradeIds)){
					List<Grade> gradeBatch = commonDataService.getGradeBatch(Long.valueOf(schoolId), gradeIds, termInfoId);
					Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;
					if(CollectionUtils.isNotEmpty(gradeBatch) && gradeBatch.size() > 0){
						for (Grade grade : gradeBatch) {
							String string = njName.get(grade.getCurrentLevel());
							JSONObject jsonObject1 = new JSONObject();
							jsonObject1.put("gradeId", String.valueOf(grade.getId()));
							jsonObject1.put("gradeName", string);
							gradeObjects.add(jsonObject1);
						}
					}
				}
			}
			try {
				classObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, classObjects, "gradeId,className");
				classObjects.add(0,cObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			rs.put("code", code);
			rs.put("msg", msg);
			rs.put("classObjects", classObjects);
			if("-1".equals(gradeId)){
				try {
					gradeObjects = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, gradeObjects, "gradeName");
					gradeObjects.add(0,gObject);
				} catch (Exception e) {
					e.printStackTrace();
				}
				rs.put("gradeObjects", gradeObjects);
			}
		}
		
		return rs;
	}
	public static Map<String, List<JSONObject>> sortMapByKey(Map<String, List<JSONObject>> returnMap2) {  
        if (returnMap2 == null || returnMap2.isEmpty()) {  
            return null;  
        }  
        Map<String, List<JSONObject>> sortMap = new TreeMap<String, List<JSONObject>>(new MapKeyComparator());  
        sortMap.putAll(returnMap2);  
        return sortMap;  
	}
	
	
	@RequestMapping(value="/copyQuestionnaire",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject copyQuestionnaire(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		request.put("schoolId", getXxdm(req));
		Integer result = questionnaireService.copyQuestionnaire(request);
		if (result > 0) {
			rs.put("code", "1");
			rs.put("msg", "复制成功！");
		}else if (result == -100) {
			rs.put("code", "-1");
			rs.put("msg", "调查进行中不能修改！");
		} else{
			rs.put("code", "-1");
			rs.put("msg", "复制失败！");
		}
		
		return rs;  
	}
	
	@RequestMapping(value="/getRecentQuestionnaire",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRecentQuestionnaire(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject rs = new JSONObject();
		JSONObject parm = new JSONObject();
		parm.put("schoolId", getXxdm(req));
		parm.put("limitNum", 6);
		parm.put("questionTypeNoNull", "Y");
		List<JSONObject> list = questionnaireService.getRecentQuestionnaire(parm);
		Iterator<JSONObject> iterator = list.iterator();
		String questionId = request.getString("questionId");
		while (iterator.hasNext()) {
			 JSONObject question = iterator.next();
			 String questionIdTmp = question.getString("questionId");
			 if (questionIdTmp.equals(questionId)) {
				 iterator.remove();
				 break;
			}
		}
		rs.put("data", list);
		rs.put("code", "1");
		rs.put("msg", "查询成功！");
		return rs;  
	}
	
	@RequestMapping(value="/noticeQuestionnaire",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject noticeQuestionnaire(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String schoolId =  getXxdm(req);
		String termInfoId = request.getString("termInfoId");
		String questionId = request.getString("questionId");
		
		
		JSONObject param = new JSONObject();
		param.put("questionId", questionId);
		SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd");
		String nowDay = format.format(new Date());
		try {
			param.put("noticeDate",format.parse(nowDay) );
		} catch (ParseException e2) {
			e2.printStackTrace();
		}
		Integer  noticeTimes = questionnaireService.getNoticeTime(param);
		logger.info("noticeTimes===>" + noticeTimes);
		if (noticeTimes==null) {
			noticeTimes = 0 ;
		}
		if (noticeTimes >=3) {
			response.put("code", -1);
			response.put("msg", "本次问卷今天提醒次数已达3次上限");
			return response;
		}
		
		School school = commonDataService.getSchoolById(Long.parseLong(schoolId), termInfoId);
		request.put("schoolId", schoolId);
		
		
		List<JSONObject> questionList = questionnaireService.queryQuestionList(request);
		JSONObject question = questionList.get(0);
		request.put("questionType", question.getString("questionType"));
		Map<String, List<Long>> map = questionnaireService.getWXNoticePerson(request);
		List<Long> accountIds = map.get("accountlist");

        List<Long> teacherAccountIds = map.get("teacherAccountIds");
        List<Long> studentParentUserIds = map.get("studentParentUserIds");
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<Account> accountList = null;
        
        Map<String,String> accountMap = new HashMap<String,String>();
        
        /** 根据ZHXY-440 添加家长类型的处理 **/
        List<Long> studentAccountList = map.get("studentAccountList");
        if(studentAccountList != null && studentAccountList.size()>0) {
        	//根据ZHXY-440 当类型为家长时，通过学生accountId列表，获取家长通知列表
        	List<JSONObject> parentReceivers = commonDataService.getSimpleParentByStuMsg(studentAccountList, termInfoId, Long.valueOf(schoolId));
        	logger.info("ZHXY-440 getSimpleParentByStuMsg parentReceivers : " + parentReceivers);
        	if(parentReceivers != null && parentReceivers.size()>0) {
        		for(JSONObject each : parentReceivers) {
        			String accountId = each.getString("accountId"); 
        	        accountMap.put(accountId, accountId);	
        		}
        		//排除有记录的accountId
        		questionnaireService.checkHasRecord(schoolId, questionId, accountMap);
        		
        		for(JSONObject each : parentReceivers) {
        			String accountId = each.getString("accountId");
        			if(accountMap.containsKey(accountId)) {
		        		JSONObject object = new JSONObject();
						object.put("userId", each.getString("extUserId"));
						object.put("userName", each.getString("name"));
						list.add(object);
        			}
        		}
        	}
        } else {
        	//原来的处理
        	for(Long a : accountIds) {
        		accountMap.put(String.valueOf(a), String.valueOf(a));
        	}
        	//排除有记录的accountId
        	accountIds = questionnaireService.checkHasRecord(schoolId, questionId, accountMap);
    		
	        accountList = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
	        logger.info("ZHXY-440 getAccountBatch accountList : " + accountList.toString());
	//		List<JSONObject> list = new ArrayList<JSONObject>();
			for (int i = 0; i < accountList.size(); i++) {
				Account account = accountList.get(i);
				if (teacherAccountIds.contains(account.getId())) {// 是老师
					JSONObject object = new JSONObject();
					object.put("userId", account.getExtId());
					object.put("userName", account.getName());
					List<User> users = account.getUsers();
					for (int j = 0; j < users.size(); j++) {
						User user = users.get(j);
						if (user.getUserPart().getRole() == T_Role.Teacher) {
							object.put("yxhUserId", user.getUserPart().getId());
//							list.add(object);
							break;
						}
					}
					list.add(object);
				}else {
					JSONObject object = new JSONObject();
					object.put("userId", account.getExtId());
					object.put("userName", account.getName());
					List<User> users = account.getUsers();
					for (int j = 0; j < users.size(); j++) {
						User user = users.get(j);
						if (studentParentUserIds.contains(user.getUserPart().getId())) {
							object.put("yxhUserId", user.getUserPart().getId());
//							list.add(object);
							break;
						}
					}
					list.add(object);
				}
				
			}
        }
		
		List<JSONObject> msgCenterReceiversArray = new ArrayList<JSONObject>(); 
		
		if(studentAccountList == null) {//根据ZHXY-440 添加
	        if (accountList==null || accountList.size() == 0) {
	        	response.put("code", -1);
				response.put("msg", "没有可以通知的人员!");
				return response;
			}
		}
        if (noticeTimes < 3) {
        	param.put("noticeTimes", noticeTimes + 1);
			questionnaireService.updateNoticeTimes(param);
		}
		for (int i = 0; i < list.size(); i++) {
			JSONObject acc = list.get(i);
			JSONObject msgCenterReceiver = new JSONObject();
			msgCenterReceiver.put("userId", acc.getString("userId"));
			msgCenterReceiver.put("userName", acc.getString("userName"));
//			msgCenterReceiver.put("yxhUserId", acc.getString("yxhUserId"));//ZHXY-440 删除，yxhUserId为不用字段
			msgCenterReceiversArray.add(msgCenterReceiver);
		}
		logger.info("ZHXY-440 msgCenterReceiversArray : " + msgCenterReceiversArray);
		
		accountIds.clear();
		Long accountId = (Long)req.getSession().getAttribute("accountId");
		logger.info("ZHXY-440 accountId : " + accountId);
		accountIds.add(accountId);
		List<Account> accountList2 = commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
		logger.info("ZHXY-440 accountList2 : " + accountList2);
		Account account = accountList2.get(0);
		String msgId = UUIDUtil.getUUID().replace("-", "");
		JSONObject msgCenterPayLoad = new JSONObject();
		JSONObject msg = new JSONObject();
		msg.put("msgId", msgId);
		msg.put("msgTitle", "问卷调查");
		msg.put("msgContent",   "你收到一份新的问卷调查");

		msg.put("msgUrlPc", msgUrlPc);
		//msg.put("msgUrlApp", "http://www.yunxiaoyuan.com/apph5/openH5/questionNaire/index.html?schoolId="+school.getId());
		msg.put("msgUrlApp", msgUrlApp+questionId);
		msg.put("msgOrigin", "问卷调查");
		msg.put("msgTypeCode", "WJDC");
		msg.put("msgTemplateType", "WJDC");
		msg.put("schoolId", school.getExtId());
		msg.put("creatorName", account.getName());

		JSONObject first = new JSONObject();
		first.put("value", "你收到一份新的问卷调查");

		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", question.getString("questionName"));
	 
				
		JSONObject keyword2 = new JSONObject();
		keyword2.put("value", format.format(question.getDate("questionStartDate")));

		JSONObject keyword3 = new JSONObject();
		keyword3.put("value",  format.format(question.getDate("questionEndDate")));
 
		JSONObject remark = new JSONObject();
		remark.put("value", "请在指定时间完成问卷提交！");
		
		JSONObject data = new JSONObject();
		data.put("first", first);
		data.put("keyword1", keyword1);
		data.put("keyword2", keyword2);
		data.put("keyword3", keyword3);
		data.put("remark", remark);
		Date now = new Date();
		String nowday = format.format(now);
		try {
			now = format.parse(nowday);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		if ( now.getTime() <= question.getDate("questionEndDate").getTime()) {
			//data.put("url", "http://www.yunxiaoyuan.com/apph5/openH5/questionNaire/index.html?schoolId="+school.getId());
			data.put("url", msgUrlApp+questionId);
		}
		msg.put("msgWxJson", data);
		msgCenterPayLoad.put("msg", msg);
		msgCenterPayLoad.put("receivers", msgCenterReceiversArray); 
		try {
			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgCenterPayLoad, "WJDC", clientId, clientSecret);
			response.put("code", 200);
			response.put("msg", "发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			response.put("code", -1);
			response.put("msg", "发送失败");
		}
 
		return response;
	}
	
	
	
	@RequestMapping(value="/viewVoteDetail",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject viewVoteDetail(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res) {
		String levelId = request.getString("levelId");
		String targetId = request.getString("targetId");
		String termInfoId = request.getString("termInfoId");
		String schoolId =  request.getString("schoolId");// getXxdm(req);
		JSONObject response = new JSONObject();
		if (StringUtils.isBlank(levelId) ||StringUtils.isBlank(targetId) ||StringUtils.isBlank(termInfoId) ) {
			response.put("code", -1);
			response.put("msg", "参数异常!");
			return response;
		}
		List<JSONObject> list = questionnaireService.getViewVoteDetail(request);
		if (list!=null && list.size() > 0) {
			JSONObject detail = list.get(0);
			String anonymous = detail.getString("anonymous");
			List<Long> accountIds = new ArrayList<Long>();
			Integer isEdit = 0;
			for (int i = 0; i <list.size(); i++) {
				detail =  list.get(i);
				accountIds.add(detail.getLong("accountId"));
				isEdit = detail.getInteger("isEdit");
			}
			if (isEdit == 2) {
				for (int i = 0; i <list.size(); i++) {
					list.get(i).remove("content");
				}
			}
			
			if ("1".equals(anonymous)) {
				for (int i = 0; i < list.size(); i++) {
					detail =  list.get(i);
					detail.put("accountName", "匿名");
				}
				response.put("data", list);
			}else {
				List<Account> accounts= commonDataService.getAccountBatch(Long.parseLong(schoolId), accountIds, termInfoId);
				HashMap<Long, String> map = new HashMap<Long, String>();
				if (accounts!=null && accounts.size() > 0) {
					for (int i = 0; i < accounts.size(); i++) {
						Account account = accounts.get(i);
						map.put(account.getId(), account.getName());
					}
				}
				for (int i = 0; i < list.size(); i++) {
					detail =  list.get(i);
					detail.put("accountName", map.get(detail.getLong("accountId")));
				}
				response.put("data", list);
			}		
		}
		response.put("code", 1);
		response.put("msg", "查询成功!");
		
		return response;
	}
	
	@RequestMapping(value="/exportSataticByQuestionNo",method = RequestMethod.GET)
	@ResponseBody
	public void exportSataticByQuestionNo(HttpServletRequest req, HttpServletResponse res) {
		String questionType =req.getParameter("questionType"); 
		String schoolId =req.getParameter("schoolId");
		String questionId =req.getParameter("questionId");
		if (StringUtils.isBlank(questionType) || StringUtils.isBlank(schoolId)) {
			logger.info("参数异常");
			return ;
		}
		if ("2".equals(questionType)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("schoolId", schoolId);
			paramMap.put("questionId", questionId);
			List<JSONObject> quetions =  questionnaireService.queryQuestionList(paramMap);
			if (quetions==null || quetions.size() == 0) {
				logger.info("没有问卷记录");
				return ;
			}
			JSONObject question = quetions.get(0);
			
			List<JSONObject> targetS = questionnaireService.queryTargetByQueId(paramMap);// 题目
			if(CollectionUtils.isNotEmpty(targetS)){
				for(JSONObject target : targetS){
					if("1".equals(target.getString("targetType"))){
						HashMap<String, Object> paramMap2 = new HashMap<String, Object>();
						String targetId = target.getString("targetId");
						paramMap2.put("targetId", targetId);
						List<JSONObject> targetLevels = questionnaireService.queryTargetLevelById(paramMap2);
						if(CollectionUtils.isNotEmpty(targetLevels)){
							target.put("level", targetLevels);
						}
					}
				}
				List<JSONObject> recordResultList = questionnaireService.queryRecordForSpecificResult(paramMap);
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (int i = 0; i < recordResultList.size(); i++) {
					JSONObject record = recordResultList.get(i);
					map.put(record.getString("levelId"), record.getInteger("levelNum"));
				}
				String fileName =question.getString("questionName") + "--统计情况";
			 	Workbook workbook = new SXSSFWorkbook(1000);// 产生工作薄对象
			 	Sheet sheet = workbook.createSheet();
			 	XSSFStyleTool.setBorderStyle(workbook, true);
			 	CellStyle setBorder = workbook.createCellStyle();
			 	setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
			    Font btFont = workbook.createFont();
		        btFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		        btFont.setFontHeightInPoints((short) 15);
			 	setBorder.setFont(btFont);
			 	int row = 0;
			    DecimalFormat df = new DecimalFormat("#.00");
			    JSONObject par = new JSONObject();
				for (int i = 0; i < targetS.size(); i++) {
					JSONObject target = targetS.get(i);
					if("1".equals(target.getString("targetType"))){
						  Row trow = sheet.createRow(row);
						  CellRangeAddress reg = new CellRangeAddress((short) (row), (short) (row),(short) 0,  (short) 2);
				          sheet.addMergedRegion(reg);
						  row = row + 1;
						  Cell tcell = trow.createCell(0);
						  tcell.setCellStyle(setBorder);
						  String optionType = "2".equals(target.getString("optionType"))?"多选题":"单选题";
					      tcell.setCellValue("第"+ (i + 1) + "题 " + target.getString("targetDetail") + "(" + optionType +")");
					      JSONArray array = target.getJSONArray("level");
					      if (array!=null) {
					    	  int allOption = 0;
					    	  for (int j = 0; j <array.size(); j++) {
					    		  JSONObject object2 = array.getJSONObject(j);
					    		  Integer cnt = map.get(object2.getString("levelId"));
					    		  if (cnt!=null) {
					    			  allOption = allOption + cnt;
								  }
							  }
					    	  trow = sheet.createRow(row);
							  row = row + 1;
							  tcell = trow.createCell(0);
							  tcell.setCellValue("选项");
							  tcell = trow.createCell(1);
							  tcell.setCellValue("内容");
							  tcell = trow.createCell(2);
							  tcell.setCellValue("小计");
							  tcell = trow.createCell(3);
							  tcell.setCellValue("比例");
					    	  for (int j = 0; j < array.size(); j++) {
					    		   JSONObject object2 = array.getJSONObject(j);
					    		   trow = sheet.createRow(row);
					    		   row = row + 1;
					    		   tcell = trow.createCell(0);
								   tcell.setCellValue( object2.getString("targetLevelOpt") +"、" +  object2.getString("levelName"));
								   Integer cnt = map.get(object2.getString("levelId"));
								  tcell = trow.createCell(2);
								  if (cnt==null) {
									  tcell.setCellValue("0");
									  tcell = trow.createCell(3);
									  tcell.setCellValue("0%");
								  }else {
									  tcell.setCellValue(cnt);
									  tcell = trow.createCell(3);
									  tcell.setCellValue(df.format(  (cnt /( allOption + 0.0)) * (100.0) ) + "%");
								  }
								  if ("1".equals(object2.getString("isEdit"))) {
									  par.put("levelId", object2.getString("levelId"));
									  System.out.println( " levelId "  + object2.getString("levelId")  );
									  List<JSONObject> details =  questionnaireService.getViewVoteDetail(par);
									  if (details.size() > 0) {
										  tcell = trow.createCell(1);
										  tcell.setCellValue(details.get(0).getString("content"));
										  if (details.size() > 1) {
											for (int k = 1; k < details.size(); k++) {
												  trow = sheet.createRow(row);
									    		  row = row + 1;
									    		  tcell = trow.createCell(1);
												   tcell.setCellValue(details.get(k).getString("content") );
											}
										  }
									  }
									  
								  }
							 }
					    	  
					    	  trow = sheet.createRow(row);
							  row = row + 1;
							  tcell = trow.createCell(0);
							  tcell.setCellValue("总数");
							  tcell = trow.createCell(2);
							  tcell.setCellValue(allOption);
						  }
					}else {
						 Row trow = sheet.createRow(row);
						  CellRangeAddress reg = new CellRangeAddress((short) (row), (short) (row),(short) 0,  (short) 2);
				          sheet.addMergedRegion(reg);
						  row = row + 1;
						  Cell tcell = trow.createCell(0);
						  tcell.setCellStyle(setBorder);
					      tcell.setCellValue("第"+ (i + 1) + "题 " + target.getString("targetDetail") + "(主观题)");
					      trow = sheet.createRow(row);
						  row = row + 1;
						 
						  tcell = trow.createCell(0);
						  tcell.setCellValue("小计");
						  tcell = trow.createCell(1);
						  tcell.setCellValue("提交人");
						  tcell = trow.createCell(2);
						  tcell.setCellValue("回答内容");
						  paramMap.put("targetId", target.getString("targetId"));
						  System.out.println( "paramMap=="  + paramMap  );
						  List<JSONObject> subjectList = questionnaireService.getSubjectResultByTargetId(paramMap);
						  if ("0".equals(question.getString("anonymous"))) {
							  if (subjectList!=null && subjectList.size() > 0) {
								  List<Long> accounts = new ArrayList<Long>();
								  for (int j = 0; j < subjectList.size(); j++) {
									  accounts.add(subjectList.get(j).getLong("accountId"));
								  }
								  
								List<Account> accounts2 =  commonDataService.getAccountBatch(Long.parseLong(schoolId), accounts, question.getString("yearId") + question.getString("termId") );
								Map<Long, String> accountMap = new HashMap<Long, String>();
								for (int j = 0; j < accounts2.size(); j++) {
									accountMap.put(accounts2.get(j).getId(), accounts2.get(j).getName());
								}
								for (int j = 0; j < subjectList.size(); j++) {
									  JSONObject object = subjectList.get(j);
											  trow = sheet.createRow(row);
											  row = row + 1;
											  tcell = trow.createCell(0);
											  tcell.setCellValue("1");
											  tcell = trow.createCell(1);
											  tcell.setCellValue(accountMap.get(object.getLong("accountId")));
											  tcell = trow.createCell(2);
											  tcell.setCellValue(object.getString("content"));
								 }
							  }
						  }else {
							  if (subjectList!=null && subjectList.size() > 0) {
								  for (int j = 0; j < subjectList.size(); j++) {
									  JSONObject object = subjectList.get(j);
											  trow = sheet.createRow(row);
											  row = row + 1;
											  tcell = trow.createCell(0);
											  tcell.setCellValue("1");
											  tcell = trow.createCell(1);
											  tcell.setCellValue("匿名");
											  tcell = trow.createCell(2);
											  tcell.setCellValue(object.getString("content"));
								 }
							  }
						  }
					 
						  
					}
					
				}
			 	
 
			 	 String xls = UUIDUtil.getUUID();
			        File temp = new File(xls + ".xlsx");
			        OutputStream out = null;
			        BufferedInputStream bis = null;
			        BufferedOutputStream bos = null;
 
			   try {
				 if (!temp.exists()) {
		                temp.createNewFile();
		            }
		            out = new FileOutputStream(temp);
		            out.flush();
		            workbook.write(out);
		            out.close();
		            res.setContentType("octets/stream");
		            res.addHeader("Content-Type", "text/html; charset=utf-8");  
		            fileName += ".xlsx";
		            String downLoadName = new String(fileName.getBytes("gbk"), "iso8859-1"); 
		            res.addHeader("Content-Disposition", "attachment;filename="  
		                    + downLoadName); 
		            bis = new BufferedInputStream(new FileInputStream(temp));
		            bos = new BufferedOutputStream(res.getOutputStream());
		            byte[] buff = new byte[2048];
		            int bytesRead;
		            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		                   bos.write(buff, 0, bytesRead);
		            }
		            bos.flush();
				 
			 }catch  (Exception e){
				 e.printStackTrace();
			 } finally {
				 if (bis != null)
		                try {
		                    bis.close();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            if (bos != null)
		                try {
		                    bos.close();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            temp.delete();
		            if(null != workbook)
		            {
		            	try {
							 workbook.close();
						} catch (IOException e) {
							 e.printStackTrace();
						}
		            }
			 }	
			}
		}else {
			logger.info("类型不支持该接口导出");
		}
		
	}
	
	
}
class MapKeyComparator implements Comparator<String>{  
    public int compare(String str1, String str2) {  
        return str1.compareTo(str2);  
    }  
} 
