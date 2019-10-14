package com.talkweb.questionnaire.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.business.EasyUIDatagridHead;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.questionnaire.dao.QuestionnaireDao;
import com.talkweb.questionnaire.service.QuestionnaireService;

/**
 * @ClassName QuestionnaireServiceImpl.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2015年10月20日 上午8:47:33
 */
@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private QuestionnaireDao questionnaireDao;
	
	Logger logger = LoggerFactory.getLogger(QuestionnaireServiceImpl.class);
	
	public String queryTermInfo(JSONObject request) {
		String termInfo = (String) request.remove("termInfo");
		User user = (User) request.remove("user");
		Long schoolId = request.getLong("schoolId");
		
		if(T_Role.Teacher.equals(user.getUserPart().getRole())) {	// 当前角色为老师
			Long accId = user.getAccountPart().getId();
			Account acc = commonDataService.getAccountAllById(schoolId, accId, termInfo);
			List<User> userList = acc.getUsers();
			for(User u : userList) {	// 如果身为老师，并且拥有管理员身份，那么直接返回当前学年学期
				if(T_Role.SchoolManager.equals(u.getUserPart().getRole())){
					return termInfo;
				}
			}
			// 否则查询老师是否拥有问卷调查数据
			request.put("accountId", accId);
			request.put("objType", 1);
		}else if(T_Role.Student.equals(user.getUserPart().getRole())) {	// 当前角色为学生
			request.put("classId", user.studentPart.getClassId());
			request.put("objType", 2);
		}else if(T_Role.Parent.equals(user.getUserPart().getRole())) {	// 当前角色为家长
			request.put("classId", user.getParentPart().getClassId());
			request.put("objType", 3);
		} else {
			return termInfo;
		}

		int len = termInfo.length();
		String xn = termInfo.substring(0, len - 1);
		String xq = termInfo.substring(len - 1);
		
		request.put("yearId", xn);
		request.put("termId", xq);
		
		// 判断本学期是否有数据，如果有，返回当前学年学期
		if(questionnaireDao.ifExistsDataInTermInfo(request)){
			return termInfo;
		}
		
		// 回退一个学期
		String preTermInfo = null;
		if("2".equals(xq)) {
			request.put("termId", "1");
			preTermInfo = xn + "1";
		} else {
			String preXn = String.valueOf(Integer.parseInt(xn) - 1);
			request.put("yearId", preXn);
			request.put("termId", "2");
			preTermInfo = preXn + "2";
		}
		
		// 如果当前学年学期没有数据，并且上一个学年学期有数据，注意：endDate，返回上一个学年学期
		request.put("endDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		if(questionnaireDao.ifExistsDataInTermInfo(request)){
			return preTermInfo;
		}
		
		return termInfo;
	}

	public Integer queryObjTypeFromQnObj(Map<String, Object> map){
		return questionnaireDao.queryObjTypeFromQnObj(map);
	}
	/**
	 * @see 查询问卷
	 * @date 2015年10月20日 下午4:01:05
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryQuestionList(Map<String, Object> map) {
		return questionnaireDao.queryQuestionList(map);
	}

	/**
	 * @see 新增问卷调查
	 * @date 2015年10月20日 下午4:01:05
	 * @version 版本
	 * @author liboqi
	 */
	public Integer createQuestionnaire(Map<String, Object> map) {
		return questionnaireDao.createQuestionnaire(map);
	}

	/**
	 * @see 编辑问卷调查名字
	 * @date 2015年10月20日 下午4:01:05
	 * @version 版本
	 * @author liboqi
	 */
	public Integer updateQuestionByName(Map<String, Object> map) {
		return questionnaireDao.updateQuestionByName(map);
	}

	/**
	 * @see 编辑问卷调查详细信息
	 * @date 2015年10月20日 下午4:01:05
	 * @version 版本
	 * @author liboqi
	 */
	public Integer updateQuestionnaireByDetail(Map<String, Object> map) {
		String questionType = map.get("questionType").toString();
		List<JSONObject> questionList = questionnaireDao.queryQuestionList(map);
		if (CollectionUtils.isNotEmpty(questionList)) {
			JSONObject jsonObject = questionList.get(0);
			if (!questionType.equals(jsonObject.getString("questionType"))) {
				if ("1".equals(jsonObject.getString("questionType"))) {
					questionnaireDao.deleteRuleForTable(map);
					questionnaireDao.deleteQuestionByTab(map);
				}
				if ("2".equals(jsonObject.getString("questionType"))) {
					questionnaireDao.deleteQuestionByGxLevel(map);
					questionnaireDao.deleteQuestionByGx(map);
				}
			}
		}
		if ("1".equals(questionType)) {
			JSONObject object = (JSONObject) map.get("questionRule");
			HashMap<String, Object> ruleForTable = new HashMap<String, Object>();
			ruleForTable.put("tableRuleId", UUIDUtil.getUUID());
			ruleForTable.put("schoolId", map.get("schoolId").toString());
			ruleForTable.put("questionId", map.get("questionId").toString());
			ruleForTable.put("contentNum1", object.getString("contentNum1"));
			ruleForTable.put("contentNum2", object.getString("contentNum2"));
			ruleForTable.put("optionNum1", object.getString("optionNum1"));
			ruleForTable.put("optionNum2", object.getString("optionNum2"));
			ruleForTable.put("isImport", "2");
			ruleForTable.put("maxNum", object.getString("maxNum"));
			ruleForTable.put("minNum", object.getString("minNum"));
			List<JSONObject> ruleForTableList = questionnaireDao.queryRuleForTableById(ruleForTable);
			if (CollectionUtils.isNotEmpty(ruleForTableList) && ruleForTableList.size() > 0) {
				JSONObject jsonObject = ruleForTableList.get(0);
				boolean flag = false;
				if (object.getInteger("contentNum1") != jsonObject.getInteger("contentNum1")
						|| jsonObject.getInteger("contentNum2") != object.getInteger("contentNum2")) {
					questionnaireDao.deleteQuestionByTab(ruleForTable);
					flag = true;
				}
				if (jsonObject.containsKey("tableRuleId")) {
					ruleForTable.put("tableRuleId", jsonObject.get("tableRuleId"));
				}
				if (flag) {
					List<JSONObject> recordS = questionnaireDao.queryRecordByUser(ruleForTable);
					if (CollectionUtils.isNotEmpty(recordS) && recordS.size() > 0) {
						for (JSONObject record : recordS) {
							HashMap<String, Object> hashMap = new HashMap<String, Object>();
							hashMap.put("recordId", record.getString("recordId"));
							hashMap.put("schoolId", map.get("schoolId").toString());
							questionnaireDao.deleteRecordDetailForTable(hashMap);
						}
					}
					questionnaireDao.deleteRecordByQuestionId(ruleForTable);
				}
			}
			questionnaireDao.deleteRuleForTable(ruleForTable);
			questionnaireDao.createRuleForTable(ruleForTable);
		}
		return questionnaireDao.updateQuestionnaireByDetail(map);
	}

	/**
	 * @see 查询问卷的详细信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRuleForTableById(Map<String, Object> map) {
		return questionnaireDao.queryRuleForTableById(map);
	}

	/**
	 * @see 保存表格信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public Integer saveQuestionByTab(Map<String, Object> map, String questionId) {
		if (StringUtils.isNotBlank(questionId)) {
			HashMap<String, Object> selectMap = new HashMap<String, Object>();
			selectMap.put("questionId", questionId);
			List<JSONObject> optionForTableList = questionnaireDao.queryOptionForTableById(selectMap);
			if (CollectionUtils.isNotEmpty(optionForTableList)) {
				questionnaireDao.deleteQuestionByTab(selectMap);
			}
		}
		return questionnaireDao.saveQuestionByTab(map);
	}

	/**
	 * @see 保存表格信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryOptionForTableById(Map<String, Object> map) {
		return questionnaireDao.queryOptionForTableById(map);
	}

	/**
	 * @see 创建的原因
	 * @date 2015年10月25日 下午3:42:07
	 * @version 版本
	 * @author liboqi
	 */
	public JSONObject importExcel(MultipartFile file, String questionId) {
		JSONObject returnJson = new JSONObject();
		String code = "0";
		String msg = "";
		try {
			int excel = isExcel(file.getOriginalFilename());
			Workbook book = null;
			if (1 == excel) {
				book = new HSSFWorkbook(file.getInputStream());
			} else if (excel == 2) {
				book = new XSSFWorkbook(file.getInputStream());
			} else {
				code = "-1";
				msg = "导入的不是EXCEL模板，请重新导入！";
			}
			HashMap<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("questionId", questionId);
			Sheet sheet = book.getSheetAt(0);
			int maxRows = sheet.getLastRowNum();
			List<JSONObject> ruleForTable = questionnaireDao.queryRuleForTableById(paramMap);
			if (CollectionUtils.isNotEmpty(ruleForTable)) {
				JSONObject jsonObject = ruleForTable.get(0);
				HashMap<String, List<List<JSONObject>>> map = new HashMap<String, List<List<JSONObject>>>();
				int maxCells = 0;
				int failNum = 0;
				int successNum = 0;
				for (int index = 0; index <= maxRows; index++) {
					boolean rowsFalg = true;
					List<Boolean> flags = new ArrayList<Boolean>();
					List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
					Row row = sheet.getRow(index);
					if (row == null) {
						row = sheet.createRow(index);
					}
					boolean flag = false;
					if (index == 0) {
						maxCells = row.getPhysicalNumberOfCells();
						if (maxCells != jsonObject.getInteger("optionNum2")) {
							code = "-1";
							msg = "第一行的列数不正确，请校验后，重新导入！";
							break;
						}
					}
					for (int cellIndex = 0; cellIndex < maxCells; cellIndex++) {
						JSONObject cellJson = new JSONObject();
						Cell cell = row.getCell(cellIndex);
						if (cell == null) {
							cell = row.createCell(cellIndex);
						}
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						String content = cell.getStringCellValue();
						String tableOptionType = "";
						if (cellIndex > (jsonObject.getInteger("contentNum2") - 1) && index != 0
								&& StringUtils.isEmpty(content)) {
							content = "no";
							tableOptionType = "1";
						} else {
							tableOptionType = "2";
						}
						if (StringUtils.isEmpty(content)) {
							flag = true;
						}
						flags.add(flag);
						cellJson.put("questionId", questionId);
						cellJson.put("tableOptionId", UUIDUtil.getUUID());
						cellJson.put("indexColumn", (cellIndex + 1));
						cellJson.put("tableOptionType", tableOptionType);
						cellJson.put("content", content);
						jsonObjects.add(cellJson);
					}
					for (boolean onFlag : flags) {
						if (onFlag) {
							rowsFalg = false;
						}
					}
					if (rowsFalg) {
						for (JSONObject jsonObject2 : jsonObjects) {
							jsonObject2.put("indexRow", index - failNum);
						}
					} else {
						for (JSONObject jsonObject2 : jsonObjects) {
							jsonObject2.put("indexRow", index);
						}
					}
					if (flag) {
						if (map.containsKey("fail")) {
							failNum = failNum + 1;
							map.get("fail").add(jsonObjects);
						} else {
							List<List<JSONObject>> list = new ArrayList<List<JSONObject>>();
							failNum = failNum + 1;
							list.add(jsonObjects);
							map.put("fail", list);
						}
					} else {
						if (map.containsKey("success")) {
							map.get("success").add(jsonObjects);
							successNum = successNum + 1;
						} else {
							List<List<JSONObject>> list = new ArrayList<List<JSONObject>>();
							;
							list.add(jsonObjects);
							map.put("success", list);
							successNum = successNum + 1;
						}
					}
				}

				returnJson.put("maxRows", maxRows + 1);
				returnJson.put("data", map);
				returnJson.put("failNum", failNum);
				returnJson.put("successNum", successNum);
			}
		} catch (IOException e) {
			code = "-1";
			msg = "导入的EXCEL模板失败，请稍后再试！";
		}
		returnJson.put("code", code);
		returnJson.put("msg", msg);
		return returnJson;
	}

	/**
	 * 依据后缀名判断读取的是否为Excel文件
	 * 
	 * @param filePath
	 * @return
	 */
	public static int isExcel(String filePath) {
		if (filePath.matches("^.+\\.(?i)(xls)$")) {
			return 1;
		} else if (filePath.matches("^.+\\.(?i)(xlsx)$")) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	 * @see 新增个性表格信息
	 * @date 2015年11月4日 上午10:16:51
	 * @version 版本
	 * @author liboqi
	 */
	public Integer saveQuestionByQx(Map<String, Object> map) {
		String targetType = map.get("targetType").toString();
		if ("1".equals(targetType)) {
			@SuppressWarnings("unchecked")
			List<JSONObject> jsonObjects = (List<JSONObject>) map.get("targetLevel");
			for (JSONObject jsonObject : jsonObjects) {
				jsonObject.put("levelId", UUIDUtil.getUUID());
				jsonObject.put("schoolId", map.get("schoolId"));
				jsonObject.put("targetId", map.get("targetId"));
			}
			questionnaireDao.saveQuestionByQxLevel(map);
		}
		Integer saveQuestionByQx = questionnaireDao.saveQuestionByQx(map);
		return saveQuestionByQx;
	}

	/**
	 * @see 获取个性题目最大的序号
	 * @date 2015年11月4日 上午10:16:51
	 * @version 版本
	 * @author liboqi
	 */
	public Integer queryTargetMaxSeqByQueId(Map<String, Object> map) {
		return questionnaireDao.queryTargetMaxSeqByQueId(map);
	}

	/**
	 * @see 查询问卷的指标信息
	 * @date 2015年11月4日 上午11:30:12
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryTargetByQueId(Map<String, Object> map) {
		return questionnaireDao.queryTargetByQueId(map);
	}

	/**
	 * @see 查询问卷的指标等级信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryTargetLevelById(Map<String, Object> map) {
		return questionnaireDao.queryTargetLevelById(map);
	}

	/**
	 * @see 修改个性等级信息
	 * @date 2015年11月5日 下午6:34:30
	 * @version 版本
	 * @author liboqi
	 */
	public Integer updateQuestionByQx(Map<String, Object> map) {
		String targetType = map.get("targetType").toString();
		if ("1".equals(targetType)) {
			questionnaireDao.deleteQuestionByGxLevel(map);
			@SuppressWarnings("unchecked")
			List<JSONObject> levels = (List<JSONObject>) map.get("targetLevel");
			for (JSONObject level : levels) {
				level.put("levelId", UUIDUtil.getUUID());
				level.put("schoolId", map.get("schoolId"));
				level.put("targetId", map.get("targetId"));
			}
			questionnaireDao.saveQuestionByQxLevel(map);
		}
		Integer updateNum = questionnaireDao.updateQuestionByQx(map);
		return updateNum;
	}

	/**
	 * @see 个性信息上移下移
	 * @date 2015年11月5日 下午6:34:30
	 * @version 版本
	 * @author liboqi
	 */
	public Integer updateUpOrDownTargetSeq(JSONObject request) {
		int moveDirection = request.getIntValue("moveDirection");
		
		Map<String, Object> originalMap = new HashMap<String, Object>();
		originalMap.put("questionId", request.get("questionId"));
		originalMap.put("targetId", request.get("targetId"));
		originalMap.put("schoolId", request.get("schoolId"));
		
		Map<String, Object> moveMap = new HashMap<String, Object>();
		if (moveDirection > 0) {
			moveMap.put("questionId", request.get("questionId"));
			moveMap.put("targetId", request.get("upTargetId"));
			moveMap.put("schoolId", request.get("schoolId"));
		} else {
			moveMap.put("questionId", request.get("questionId"));
			moveMap.put("targetId", request.get("downTargetId"));
			moveMap.put("schoolId", request.get("schoolId"));
		}
		
		List<JSONObject> targets = questionnaireDao.queryTargetByQueId(originalMap);
		List<JSONObject> moveTargets = questionnaireDao.queryTargetByQueId(moveMap);
		
		if (CollectionUtils.isNotEmpty(targets) && CollectionUtils.isNotEmpty(moveTargets)) {
			JSONObject target = targets.get(0);
			JSONObject moveTarget = moveTargets.get(0);
			Integer seq = target.getInteger("targetSeq");
			Integer moveSeq = moveTarget.getInteger("targetSeq");
			
			originalMap.put("targetSeq", moveSeq);
			moveMap.put("targetSeq", seq);
			
			questionnaireDao.updateQuestionBySeq(originalMap);
			questionnaireDao.updateQuestionBySeq(moveMap);
			
			return 1;
		}
		return -1;
	}

	/**
	 * @see 删除个性等级信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public Integer deleteQuestionByGx(Map<String, Object> map) {
		List<JSONObject> targetS = questionnaireDao.queryTargetByQueId(map);
		if (CollectionUtils.isNotEmpty(targetS)) {
			JSONObject jsonObject = targetS.get(0);
			String targetSeq = jsonObject.get("targetSeq").toString();
			HashMap<String, Object> targetSeqMap = new HashMap<String, Object>();
			targetSeqMap.put("questionId", map.get("questionId"));
			targetSeqMap.put("targetSeq", targetSeq);
			List<JSONObject> updateTargetSeqS = questionnaireDao.queryTargetByQueId(targetSeqMap);
			if (CollectionUtils.isNotEmpty(updateTargetSeqS)) {
				for (JSONObject jsonObject2 : updateTargetSeqS) {
					HashMap<String, Object> updateTargetSeq = new HashMap<String, Object>();
					updateTargetSeq.put("targetSeq", jsonObject2.getInteger("targetSeq") - 1);
					updateTargetSeq.put("schoolId", map.get("schoolId"));
					updateTargetSeq.put("targetId", jsonObject2.getString("targetId"));
					updateTargetSeq.put("questionId", jsonObject2.getString("questionId"));
					questionnaireDao.updateQuestionBySeq(updateTargetSeq);
				}
			}
		}
		questionnaireDao.deleteQuestionByGxLevel(map);
		Integer deleteQuestionByGx = questionnaireDao.deleteQuestionByGx(map);
		return deleteQuestionByGx;
	}

	/**
	 * @see 编辑问卷调查状态信息
	 * @date 2015年11月7日 下午4:42:38
	 * @version 版本
	 * @author liboqi
	 */
	public Integer updateQuestionnaireStatus(Map<String, Object> map) {
		return questionnaireDao.updateQuestionnaireStatus(map);
	}

	/**
	 * @see 保存用户填写的问卷 （表格）
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public Integer saveQuestionByUserDetails(Map<String, Object> paramMap) {
		HashMap<String, Object> recordMap = new HashMap<String, Object>();
		recordMap.put("questionId", paramMap.get("questionId"));
		recordMap.put("accountId", paramMap.get("accountId"));
		recordMap.put("schoolId", paramMap.get("schoolId"));
		recordMap.put("classId", paramMap.get("classId"));
		List<JSONObject> recordList = questionnaireDao.queryRecordByUser(recordMap);
		recordMap.put("userId", paramMap.get("userId"));
		Integer indexRow = (Integer)paramMap.get("indexRow"); 
		logger.info("recordList====>" + recordList);
		if (CollectionUtils.isEmpty(recordList) && recordList.size() == 0) {
			String recordId = UUIDUtil.getUUID();
			recordMap.put("recordId", recordId);
			recordMap.put("createDate", new Date());
			paramMap.put("recordId", recordId);
		} else {
			paramMap.put("recordId", recordList.get(0).get("recordId"));
			recordMap.put("recordId", recordList.get(0).get("recordId"));
		}
		questionnaireDao.saveRecord(recordMap);
		
		Map<String, Object> recordDetailMap = new HashMap<String, Object>();
		recordDetailMap.put("recordId", paramMap.get("recordId"));
		recordDetailMap.put("schoolId", paramMap.get("schoolId"));
       /*List<JSONObject> recordDetailList = questionnaireDao.queryRecordDetailForTableByUser(recordDetailMap);
		if (CollectionUtils.isNotEmpty(recordDetailList) && recordDetailList.size() > 0) {
			questionnaireDao.deleteRecordDetailForTable(recordDetailMap);
		}*/
		@SuppressWarnings("unchecked")
		List<JSONObject> newRecordDetailList = (List<JSONObject>) paramMap.get("recordDetail");
		paramMap.put("isSubmit",  1);
		
		for (JSONObject jsonObject : newRecordDetailList) {
			jsonObject.put("evalRecordResultId", UUIDUtil.getUUID());
			jsonObject.put("recordId", paramMap.get("recordId"));
			jsonObject.put("schoolId", paramMap.get("schoolId"));
			jsonObject.put("isSubmit", paramMap.get("isSubmit"));
		}
		recordDetailMap.put("recordDetail", newRecordDetailList);
		if (indexRow != null && indexRow > 0) {
			recordDetailMap.put("indexRow", indexRow);
			questionnaireDao.deleteRecordDetailForTable(recordDetailMap);
			questionnaireDao.updateSubmitStatus(recordDetailMap);
		}else {
			questionnaireDao.deleteRecordDetailForTable(recordDetailMap);
		}
		Integer saveNum = questionnaireDao.saveRecordDetailForTable(recordDetailMap);
		
		return saveNum;
	}

	/**
	 * @see 查询记录信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordByUser(Map<String, Object> map) {
		return questionnaireDao.queryRecordByUser(map);
	}

	/**
	 * @see 查询记录详细信息
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordDetailForTableByUser(Map<String, Object> map) {
		return questionnaireDao.queryRecordDetailForTableByUser(map);
	}

	/**
	 * @see 保存用户填写的问卷（个性）
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	@SuppressWarnings("unchecked")
	public Integer saveQuestionByUserDetailsToGx(Map<String, Object> paramMap) {
		Map<String, Object> recordMap = new HashMap<String, Object>();
		Map<String, Object> specificMap = new HashMap<String, Object>();
		Map<String, Object> subjectMap = new HashMap<String, Object>();
		Integer saveNum = 0;
		recordMap.put("questionId", paramMap.get("questionId"));
		recordMap.put("schoolId", paramMap.get("schoolId"));
		recordMap.put("classId", paramMap.get("classId"));
		recordMap.put("accountId", paramMap.get("accountId"));
		recordMap.put("userId", paramMap.get("userId"));
		List<JSONObject> recordList = questionnaireDao.queryRecordByUser(recordMap);
		if (CollectionUtils.isEmpty(recordList)) {
			String recordId = UUIDUtil.getUUID();
			recordMap.put("recordId", recordId);
			recordMap.put("createDate", new Date());
			questionnaireDao.saveRecord(recordMap);
			paramMap.put("recordId", recordId);
		} else {
			paramMap.put("recordId", recordList.get(0).get("recordId"));
		}
		
		List<JSONObject> targetKgt = (List<JSONObject>) paramMap.get("targetKgt");
		List<JSONObject> targetZgt = (List<JSONObject>) paramMap.get("targetZgt");
		if (CollectionUtils.isNotEmpty(targetZgt) && targetZgt.size() > 0) {
			subjectMap.put("recordId", paramMap.get("recordId"));
			subjectMap.put("schoolId", paramMap.get("schoolId"));
			List<JSONObject> subjectList = questionnaireDao.querySubjectResultForSpecific(subjectMap);
			if (CollectionUtils.isNotEmpty(subjectList) && subjectList.size() > 0) {
				questionnaireDao.deleteSubjectResultForSpecific(subjectMap);
			}
			for (JSONObject zgt : targetZgt) {
				zgt.put("recordId", paramMap.get("recordId"));
				zgt.put("schoolId", paramMap.get("schoolId"));
				zgt.put("evalRecordResultId", UUIDUtil.getUUID());
			}
			subjectMap.put("subjects", targetZgt);
		} else {
			subjectMap.put("recordId", paramMap.get("recordId"));
			subjectMap.put("schoolId", paramMap.get("schoolId"));
			List<JSONObject> subjectList = questionnaireDao.querySubjectResultForSpecific(subjectMap);
			if (CollectionUtils.isNotEmpty(subjectList) && subjectList.size() > 0) {
				questionnaireDao.deleteSubjectResultForSpecific(subjectMap);
			}
		}
		if (CollectionUtils.isNotEmpty(targetKgt) && targetKgt.size() > 0) {
			specificMap.put("recordId", paramMap.get("recordId"));
			specificMap.put("schoolId", paramMap.get("schoolId"));
			List<JSONObject> specificList = questionnaireDao.queryRecordForSpecific(specificMap);
			if (CollectionUtils.isNotEmpty(specificList) && specificList.size() > 0) {
				questionnaireDao.deleteRecordForSpecific(specificMap);
			}
			List<JSONObject> kgtAll = new ArrayList<JSONObject>();
			for (JSONObject target : targetKgt) {
				if (target.containsKey("levelArr")) {
					List<JSONObject> levelArr = (List<JSONObject>) target.get("levelArr");
					for (JSONObject levels : levelArr) {
						levels.put("recordId", paramMap.get("recordId"));
						levels.put("schoolId", paramMap.get("schoolId"));
						levels.put("evalRecordResultId", UUIDUtil.getUUID());
						kgtAll.add(levels);
					}
				}
			}
			specificMap.put("specifics", kgtAll);
		} else {
			specificMap.put("recordId", paramMap.get("recordId"));
			specificMap.put("schoolId", paramMap.get("schoolId"));
			List<JSONObject> specificList = questionnaireDao.queryRecordForSpecific(specificMap);
			if (CollectionUtils.isNotEmpty(specificList) && specificList.size() > 0) {
				questionnaireDao.deleteRecordForSpecific(specificMap);
			}
		}
		if (targetZgt.size() > 0) {
			saveNum = questionnaireDao.saveSubjectResultForSpecific(subjectMap);
		}
		if (targetKgt.size() > 0) {
			saveNum = questionnaireDao.saveRecordForSpecific(specificMap);
		}
		return saveNum;
	}

	/**
	 * @see 查询记录详细信息（主观题）
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> querySubjectResultForSpecific(Map<String, Object> map) {
		return questionnaireDao.querySubjectResultForSpecific(map);
	}

	/**
	 * @see 查询记录详细信息（客观题）
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordForSpecific(Map<String, Object> map) {
		return questionnaireDao.queryRecordForSpecific(map);
	}

	/**
	 * @see 统计客观题
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordForSpecificResult(Map<String, Object> map) {
		return questionnaireDao.queryRecordForSpecificResult(map);
	}

	/**
	 * @see 统计主观题
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> querySubjectForSpecificResult(Map<String, Object> map) {
		return questionnaireDao.querySubjectForSpecificResult(map);
	}

	/**
	 * @see 统计主观题行数
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> querySubjectResultMax(Map<String, Object> map) {
		return questionnaireDao.querySubjectResultMax(map);
	}

	/**
	 * @see 统计表格题目
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordTableResult(Map<String, Object> map) {
		return questionnaireDao.queryRecordTableResult(map);
	}

	/**
	 * @see 统计表格回答人数
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordTableResultAccountId(Map<String, Object> map) {
		return questionnaireDao.queryRecordTableResultAccountId(map);
	}

	/**
	 * @see 获取设置的对象
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryObInfo(Map<String, Object> map) {
		return questionnaireDao.queryObInfo(map);
	}

	/**
	 * @see 获取设置的对象
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryRecordByList(Map<String, Object> map) {
		return questionnaireDao.queryRecordByList(map);
	}

	/**
	 * @see 删除调查对象
	 * @date 2015年11月3日 下午8:11:42
	 * @version 版本
	 * @author liboqi
	 */
	public Integer deleteObjInfo(Map<String, Object> map) {
		return questionnaireDao.deleteObjInfo(map);
	}

	/**
	 * @see 新增调查对象
	 * @date 2015年11月3日 下午8:11:42
	 * @version 版本
	 * @author liboqi
	 */
	public Integer saveObjInfo(Map<String, Object> map) {
		return questionnaireDao.saveObjInfo(map);
	}

	/**
	 * @see 删除问卷调查
	 * @date 2015年11月3日 下午8:11:42
	 * @version 版本
	 * @author liboqi
	 */
	public Integer deleteQuestionById(Map<String, Object> map) {
		List<JSONObject> questionList = questionnaireDao.queryQuestionList(map);
		if (CollectionUtils.isNotEmpty(questionList) && questionList.size() > 0) {
			JSONObject jsonObject = questionList.get(0);
			String questionType = jsonObject.getString("questionType");
			if (StringUtils.isNotBlank(questionType)) {
				List<JSONObject> queryRecordByUser = questionnaireDao.queryRecordByUser(map);
				if (CollectionUtils.isNotEmpty(queryRecordByUser) && queryRecordByUser.size() > 0) {
					if ("1".equals(questionType)) {
						for (JSONObject jsonObject2 : queryRecordByUser) {
							HashMap<String, Object> map2 = new HashMap<String, Object>();
							map2.put("recordId", jsonObject2.get("recordId"));
							map2.put("schoolId", map.get("schoolId"));
							questionnaireDao.deleteRecordDetailForTable(map);
						}
						questionnaireDao.deleteRecordByQuestionId(map);
						questionnaireDao.deleteRuleForTable(map);
						questionnaireDao.deleteQuestionByTab(map);
					} else {
						for (JSONObject jsonObject2 : queryRecordByUser) {
							HashMap<String, Object> map2 = new HashMap<String, Object>();
							map2.put("recordId", jsonObject2.get("recordId"));
							map2.put("schoolId", map.get("schoolId"));
							questionnaireDao.deleteRecordForSpecific(map2);
							questionnaireDao.deleteSubjectResultForSpecific(map2);
						}
						questionnaireDao.deleteRecordByQuestionId(map);
						questionnaireDao.deleteQuestionByGx(map);
						questionnaireDao.deleteQuestionByGxLevel(map);
					}
				}
			}
		}
		questionnaireDao.deleteObjInfo(map);
		return questionnaireDao.deleteQuestionById(map);
	}

	/**
	 * @see 删除记录信息
	 * @date 2015年11月3日 下午8:11:42
	 * @version 版本
	 * @author liboqi
	 */
	public Integer deleteRecordByQuestionId(Map<String, Object> map) {
		return questionnaireDao.deleteRecordByQuestionId(map);
	}

	/**
	 * @see 获取设置的对象
	 * @date 2015年10月20日 下午4:03:17
	 * @version 版本
	 * @author liboqi
	 */
	public List<JSONObject> queryObInfoByList(Map<String, Object> map) {
		return questionnaireDao.queryObInfoByList(map);
	}
	
	/**
	 * 按照投票人查看统计信息
	 */
	public JSONObject queryStatisticsResultAccToVoter(JSONObject request, long schoolId) throws Exception{
		JSONObject data = new JSONObject();
		String questionId = request.getString("questionId");
		String termInfoId = request.getString("termInfoId");
		int len = termInfoId.length();
		String xn = termInfoId.substring(0, len - 1);
		String xq = termInfoId.substring(len - 1, len);
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("questionId", questionId);
		params.put("termInfoId", termInfoId);
		params.put("yearId", xn);
		params.put("termId", xq);
		params.put("questionType", 2);
		params.put("schoolId", schoolId);
		List<JSONObject> questionList = questionnaireDao.queryQuestionList(params);
		if(CollectionUtils.isEmpty(questionList)){
			throw new RuntimeException("无法获取当前问卷调查信息。");
		}
		
		List<List<EasyUIDatagridHead>> header = new ArrayList<List<EasyUIDatagridHead>>();
		List<EasyUIDatagridHead> columns = new ArrayList<EasyUIDatagridHead>();
		List<JSONObject> targets = questionnaireDao.queryTargetByQueId(params);
		for(JSONObject target : targets){
			int targetSeq = target.getIntValue("targetSeq");
			String targetId = target.getString("targetId");
			int targetType = target.getIntValue("targetType");
			int width = 100;
			String title = "第" + targetSeq + "题";
			if(targetType == 2){
				title += "主观题";
				width = 200;
			}
			columns.add(new EasyUIDatagridHead(targetId, title, "center", width, 1, 1, false));
		}
		header.add(columns);
		
		String classIdStr = request.getString("classIds");
		if(StringUtils.isNotEmpty(classIdStr)){
			List<Long> classIds = new ArrayList<Long>();
			for(String classId : classIdStr.split(",")){
				classIds.add(Long.valueOf(classId));
			}
			params.put("classIds", classIds);
		}
		
		data.put("columns", header);
		data.put("rows", questionnaireDao.queryStatisticsResultAccToVoter(params));
		return data;
	}
	
	public JSONObject showResultUserNameByWcqk(JSONObject params){
		JSONObject result = new JSONObject();
		result.put("code", 0);
		result.put("msg", "");
		Long gradeId = (Long)params.remove("gradeId");
		Long classId = (Long)params.remove("classId");
		Long schoolId = params.getLong("schoolId");
		String termInfoId = params.getString("termInfoId");
		
		List<JSONObject>  list = questionnaireDao.queryQuestionList(params);
		JSONObject question = list.get(0);
		String questionType = question.getString("questionType");
		Integer type = questionnaireDao.queryObjTypeFromQnObj(params);
		if(type == null){
			return result;
		}
		result.put("type", type);
		params.put("questionType", questionType);
		if(type != 1) {  // 不为老师
			List<Long> classIdFilter = new ArrayList<Long>();
			if(classId != null && classId != -1){
				classIdFilter.add(classId);
			}else{
				if(gradeId != null && gradeId != -1){
					Grade grade = commonDataService.getGradeById(schoolId, gradeId, termInfoId);
					if(CollectionUtils.isNotEmpty(grade.getClassIds())){
						classIdFilter.addAll(grade.getClassIds());
					}
				}
			}
			if(classIdFilter.size() > 0){
				params.put("classIds", classIdFilter);
			}
			queryResultByWcqkAboutParentAndStu(params, result);
		}else{	// 老师
			queryResultByWcqkAboutTeacher(params, result);
		}
		return result;
	}
	
	public void queryResultByWcqkAboutTeacher(JSONObject params, JSONObject result){
		String termInfoId = params.getString("termInfoId");
		long schoolId = params.getLongValue("schoolId");
		
		Set<Long> totalIds = new HashSet<Long>();	// 总ids
		List<JSONObject> objInfo = queryObInfo(params);
		for (JSONObject jsonObject : objInfo) {
			Long accountId = jsonObject.getLong("accountId");
			if(accountId == null){
				continue;
			}
			totalIds.add(accountId);
		}
		
		Map<Long, Account> id2Account = new HashMap<Long, Account>();	// 账户id对应名称
		List<Account> accountBatch = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(totalIds), termInfoId);
		if(CollectionUtils.isNotEmpty(accountBatch)){
			for(Account acc : accountBatch){
				Long accId = acc.getId();
				id2Account.put(accId, acc);
			}
		}
		
		Set<Long> completeAccIds = new HashSet<Long>();
		String questionType = params.getString("questionType");
		List<JSONObject> recordByUser = null;
		if ("1".equals(questionType)) {
			recordByUser = questionnaireDao.getRecordSubmited(params);
		}else{
			recordByUser = queryRecordByUser(params);
		}
		if(CollectionUtils.isNotEmpty(recordByUser)){
			for (JSONObject jsonObject : recordByUser) {
				Long accId = jsonObject.getLong("accountId");
				if(accId == null){
					continue;
				}
				completeAccIds.add(accId);
			}
		}
		
		List<JSONObject> userList = new ArrayList<JSONObject>();
		List<JSONObject> weiUserList = new ArrayList<JSONObject>();
		for(long accId : totalIds){
			Account acc = id2Account.get(accId);
			if(acc == null){
				continue;
			}
			List<User> users = acc.getUsers();
			if(CollectionUtils.isEmpty(users)){
				continue;
			}
			String name = acc.getName();
			
			JSONObject json = new JSONObject();
			json.put("userId", accId);
			json.put("userName", name);
			if(completeAccIds.contains(accId)){
				userList.add(json);
			}else{
				weiUserList.add(json);
			}
		}
		
		result.put("userList", userList);
		result.put("weiUserList", weiUserList);
		
		result.put("code", 0);
		result.put("msg", "");
	}
	 
	public void queryResultByWcqkAboutParentAndStu(JSONObject params, JSONObject result){
		String termInfoId = params.getString("termInfoId");
		long schoolId = params.getLongValue("schoolId");
		Integer type = result.getInteger("type");
		String questionType = params.getString("questionType");
		List<JSONObject> objInfo = questionnaireDao.queryObInfo(params);
		Set<Long> classIds = new HashSet<Long>();
		for (JSONObject jsonObject : objInfo) {
			Long classId = jsonObject.getLong("classId");
			if(classId == null) {
				continue;
			}
			classIds.add(classId);
		}
		
		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId, new ArrayList<Long>(classIds), termInfoId);
		if(CollectionUtils.isEmpty(classrooms)){
			return ;
		}
		
		Set<Long> totalAccIds = new HashSet<Long>();	// 总ids
		Map<Long, JSONObject> classId2Data = new HashMap<Long, JSONObject>();
		for (Classroom classroom : classrooms) {
			long classId = classroom.getId();
			String className = classroom.getClassName();
			if(className == null){
				className = "";
			}
			List<Long> ids = null;
			if(type == 2){
				ids = classroom.getStudentAccountIds();
			}else{
				ids = classroom.getParentAccountIds();
			}
			if(CollectionUtils.isEmpty(ids)){
				continue;
			}
			totalAccIds.addAll(ids);
			
			JSONObject json = new JSONObject();
			json.put("className", className);
			json.put("accIds", new JSONArray());
			json.put("userIds", new JSONArray());
			classId2Data.put(classId, json);
		}
		
		// 学生为accountId对应名称
		// 家长为userId对应名称
		Map<Long, String> id2Name = new HashMap<Long, String>();
		List<Account> accountBatch = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(totalAccIds), termInfoId);
		if(CollectionUtils.isNotEmpty(accountBatch)){
			for(Account acc : accountBatch) {
				if(acc == null){
					continue;
				}
				List<User> users = acc.getUsers();
				if(CollectionUtils.isEmpty(users)){
					continue;
				}
				Long accId = acc.getId();
				for(User user : users){
					if(type == 2 && T_Role.Student.equals(user.getUserPart().getRole())){	// 学生
						Long classId = user.getStudentPart().getClassId();
						JSONObject json = classId2Data.get(classId);
						if(json != null){
							json.getJSONArray("accIds").add(accId);
							id2Name.put(accId, acc.getName());
						}
						break;
					}else if(type == 3 && T_Role.Parent.equals(user.getUserPart().getRole())){	// 家长
						Long classId = user.getParentPart().getClassId();
						Long userId = user.getParentPart().getId();
						JSONObject json = classId2Data.get(classId);
						if(json != null){
							json.getJSONArray("accIds").add(accId);
							json.getJSONArray("userIds").add(userId);
							id2Name.put(userId, user.getParentPart().getStudentName() + "家长");
						}
						// 家长没有break，因为可能一个家长对应多个学生
					}
				}
			}
		}
		
		Map<Long, Set<Long>> completeAccId2UserIds = new HashMap<Long, Set<Long>>();	// 已完成人的id
		List<JSONObject> recordByUser = null ;
		if ("1".equals(questionType)) {
			recordByUser = questionnaireDao.getRecordSubmited(params);
		}else {
			recordByUser = questionnaireDao.queryRecordByUser(params);
		}
		
	 
		if(CollectionUtils.isNotEmpty(recordByUser)){
			for (JSONObject jsonObject : recordByUser) {
				Long accId = jsonObject.getLong("accountId");
				if(accId == null){
					continue;
				}
				if(!completeAccId2UserIds.containsKey(accId)){
					completeAccId2UserIds.put(accId, new HashSet<Long>());
				}
				if(type == 3){ // 家长
					Long userId = jsonObject.getLong("userId");
					if(userId != null){
						completeAccId2UserIds.get(accId).add(userId);
					}
				}
			}
		}
		
		Map<String, List<JSONObject>> returnMap = new HashMap<String, List<JSONObject>>();
		for(Map.Entry<Long, JSONObject> entry : classId2Data.entrySet()){
			JSONObject json = entry.getValue();
			long classId = entry.getKey();
			String className = json.getString("className");
			
			List<JSONObject> list = new ArrayList<JSONObject>();
			if(type == 2){ // 学生
				for(Object id : json.getJSONArray("accIds")){
					Long accId = (Long) id;
					JSONObject obj = new JSONObject();
					obj.put("userName", id2Name.get(accId));
					obj.put("accId", accId);
					obj.put("classId", classId);
					obj.put("className", className);
					if(completeAccId2UserIds.containsKey(accId)){
						obj.put("status", 1);
					}else {
						obj.put("status", 0);
					}
					list.add(obj);
				}
			}else if(type == 3){ // 家长
				JSONArray userIds = json.getJSONArray("userIds");
				JSONArray accIds = json.getJSONArray("accIds");
				for(int i = 0; i < userIds.size(); i ++){
					Long userId = (Long) userIds.get(i);
					Long accId = (Long) accIds.get(i);
					JSONObject obj = new JSONObject();
					obj.put("userName", id2Name.get(userId));
					obj.put("accId", accId);
					obj.put("userId", userId);
					obj.put("classId", classId);
					obj.put("className", className);
					
					Set<Long> userIdSet = completeAccId2UserIds.get(accId);
					if(userIdSet != null && userIdSet.contains(userId)){
						obj.put("status", 1);
					}else{
						obj.put("status", 0);
					}
					list.add(obj);
				}
			}
			returnMap.put(className, list);
		}
		sortMapByKey(returnMap);
		result.put("returnMap", returnMap);
		result.put("code", 0);
		result.put("msg", "");
	}
	
	public static Map<String, List<JSONObject>> sortMapByKey(Map<String, List<JSONObject>> returnMap2) {  
        if (returnMap2 == null || returnMap2.isEmpty()) {  
            return null;  
        }  
        Map<String, List<JSONObject>> sortMap = new TreeMap<String, List<JSONObject>>(new Comparator<String>(){
        	public int compare(String str1, String str2) {  
                return str1.compareTo(str2);  
            }
        });  
        sortMap.putAll(returnMap2);  
        return sortMap;  
	}

	
	@Override
	public Integer copyQuestionnaire(JSONObject params) { 

		String  questionId = params.getString("questionId"); // 被复制的问卷编号
		String  copyQuestionId = params.getString("copyQuestionId"); //新的问卷编号
		String  schoolId = params.getString("schoolId");
		JSONObject para = new JSONObject();
		para.put("schoolId", schoolId);
		para.put("questionId", questionId);
		List<JSONObject> questionList = questionnaireDao.queryQuestionList(para);
		JSONObject question = questionList.get(0);

		String  questionType = question.getString("questionType");
		
		para.put("questionId", copyQuestionId);
		Integer cnt =  questionnaireDao.getRecordCnt(para);
		if (cnt > 0) {
			return -100;
		}
		
		List<JSONObject> questionTmpList = questionnaireDao.queryQuestionList(para);
		JSONObject questionTmp = questionTmpList.get(0);

		questionTmp.put("questionStartDate", question.getDate("questionStartDate"));
		questionTmp.put("questionEndDate", question.getDate("questionEndDate"));
		questionTmp.put("questionDetail", question.getString("questionDetail"));
		questionTmp.put("questionType", question.getString("questionType"));
		questionnaireDao.updateQuestionnaireByDetail(questionTmp);
		
		int result = 0;
		if ("1".equals(questionType)) {
			// 删除旧数据
			para.put("questionId", copyQuestionId);
			questionnaireDao.deleteRuleForTable(para);
			questionnaireDao.deleteQuestionByTab(para);
			para.put("questionId", questionId);//更换
			List<JSONObject> ruleList = questionnaireDao.queryRuleForTableById(para);

			if (ruleList!=null && ruleList.size() > 0) {
				for (int i = 0; i < ruleList.size(); i++) {
					String tableRuleId = UUIDUtil.getUUID();
					JSONObject rule = ruleList.get(i); 
					rule.put("questionId", copyQuestionId);// 要复制的问题编号
					rule.put("tableRuleId", tableRuleId); //createRuleForTable
					result = result + questionnaireDao.createRuleForTable(rule);
				}
			}
			List<JSONObject> optionList = questionnaireDao.queryOptionForTableById(para);
			if (optionList!=null && optionList.size() > 0) {
				for (int i = 0; i < optionList.size(); i++) {
					String tableOptionId = UUIDUtil.getUUID();
					JSONObject option = optionList.get(i); 
					option.put("questionId", copyQuestionId);
					option.put("tableOptionId", tableOptionId); 
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("optionForTableList", optionList);
				result = result + questionnaireDao.saveQuestionByTab(map);
			}
		}else if ("2".equals(questionType)) {
            // 删除旧数据
			para.put("questionId", copyQuestionId);
			questionnaireDao.deleteQuestionByGx(para);
			questionnaireDao.deleteQuestionByGxLevel(para);
			para.put("questionId", questionId);//更换
			List<JSONObject> targetList = questionnaireDao.queryTargetByQueId(para);

			if (targetList != null && targetList.size() > 0 ) {
				for (int i = 0; i < targetList.size(); i++) {
					JSONObject target = targetList.get(i);
					String  targetId = target.getString("targetId");
					String optionType = target.getString("optionType");
					String copyTargetId = UUIDUtil.getUUID();
					target.put("questionId", copyQuestionId);
					target.put("targetId", copyTargetId);

					target.put("schoolId", schoolId);//数据库大小写
					result = result + questionnaireDao.saveQuestionByQx(target);
					if ( StringUtils.isNotBlank(optionType)) {
						para.put("targetId", targetId); 
						List<JSONObject> targetLevelList = questionnaireDao.queryTargetLevelById(para);
						if (targetLevelList != null && targetLevelList.size() > 0) {
							for (int j = 0; j < targetLevelList.size(); j++) {
								String levelId = UUIDUtil.getUUID();
								JSONObject level = targetLevelList.get(j);
								level.put("targetId" ,copyTargetId);
								level.put("levelId" , levelId);
								level.put("questionId", copyQuestionId);
								level.put("schoolId", schoolId);//数据库大小写
							}
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("targetLevel", targetLevelList);
							result = result + questionnaireDao.saveQuestionByQxLevel(map);
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<JSONObject> getRecentQuestionnaire(JSONObject params) {
		List<JSONObject> list = questionnaireDao.queryQuestionList(params);
		return list;
	}

	@Override
	public Map<String, List<Long>> getWXNoticePerson(JSONObject params) {
		List<JSONObject> objList = questionnaireDao.queryObInfo(params); //调查的对象
		List<JSONObject> recordList = questionnaireDao.queryRecordByUser(params); //已经填写了的人
		String termInfoId =  params.getString("termInfoId");
		String questionType =  params.getString("questionType");
		String classId = null;
		int objType = 0;
		Long   schoolId =  null;
		List<Long> list = new ArrayList<Long>();
 
		List<Long> studentParentUserIds = new ArrayList<Long>();
		List<Long> teacherAccountIds = new ArrayList<Long>();
		Map<String, List<Long>> map = new HashMap<String, List<Long>>();

		List<Long> classIds = new ArrayList<Long>();//学生/家长类型用班级id列表，用于批量获取班级
		
		for (int i = 0; i < objList.size(); i++) {
			JSONObject obj = objList.get(i);
			classId = obj.getString("classId");
			schoolId = obj.getLong("schoolId");
			if (StringUtils.isBlank(classId)) {//是老师账户
				Long accountId = obj.getLong("accountId");
				list.add(accountId);
				teacherAccountIds.add(accountId);
			}else {// 学生和家长账户
				//加入班级列表
				classIds.add(Long.valueOf(classId));
				objType = obj.getIntValue("objType");// 1，教师 2，学生 3，家长'
//				Classroom classroom = commonDataService.getClassById(schoolId,Long.parseLong(classId) , termInfoId);
//				logger.info("ZHXY-440 classroom : " + classroom.toString());
//				
//				if (objType == 2) {
//					List<Long> students = classroom.getStudentAccountIds();
//					if (students != null) {
//						list.addAll(students);
//					}
//					List<Long>  students2 = classroom.getStudentIds();
//					if (students2 != null) {
//						studentParentUserIds.addAll(students2);
//					}
//				}else if (objType == 3) {
//					/** 根据ZHXY-440修改 **/
//					//获取学生的accountId列表
//					list.addAll(classroom.getStudentAccountIds());
////					List<Long> parents = classroom.getParentAccountIds();
////					if (parents!=null) {
////						list.addAll(parents);
////					}
////					List<Long> parents2 =classroom.getParentIds();
////					if (parents2 != null) {
////						studentParentUserIds.addAll(parents2);
////					}
//					
//				}
			}
		}
		
		if(classIds.size() > 0) {
			//学生和家长类型,批量获取班级
			logger.info("getClassroomBatch param : " + "schoolId=" + schoolId + " classIds=" + classIds + " termInfoId=" + termInfoId);
			List<Classroom> classRoomList = commonDataService.getClassroomBatch(schoolId,classIds , termInfoId);
			logger.info("classRoomList : " + classRoomList.toString());
			for(Classroom classroom : classRoomList) {
				if (objType == 2) {
					List<Long> students = classroom.getStudentAccountIds();
					if (students != null) {
						list.addAll(students);
					}
					List<Long>  students2 = classroom.getStudentIds();
					logger.info("classroom.getStudentIds() students2 : " + students2);
					if (students2 != null) {
						studentParentUserIds.addAll(students2);
					}
				} else if (objType == 3) {
					//获取学生的accountId列表
					list.addAll(classroom.getStudentAccountIds());
				}
			}
		}

		Long accountId = null;
		List<String> recordIds = new ArrayList<>();
		if ("1".equals(questionType)) {
			for (int i = 0; i <  recordList.size(); i++) {
				recordIds.add(recordList.get(i).getString("recordId"));
			}
		}else {
			for (int i = 0; i < recordList.size(); i++) {
				accountId = recordList.get(i).getLong("accountId");
				if (list.contains(accountId)) {
					list.remove(accountId);
				}
			}
		}
		if (recordIds.size() > 0) {//
			List<JSONObject> list2 = questionnaireDao.getRecorddetailfortableSubmit(recordIds);
			HashMap<String, Integer> recordeMap = new HashMap<String, Integer>();
			for (int i = 0; i < list2.size(); i++) {
				JSONObject object = list2.get(i);
				recordeMap.put(object.getString("recordId"), object.getInteger("isSubmit"));
			}
			for (int i = 0; i < recordList.size(); i++) {
				accountId = recordList.get(i).getLong("accountId");
				if (list.contains(accountId) && recordeMap.get(recordList.get(i).getString("recordId"))==null) {
					list.remove(accountId);
				}
			}
		}
		
		map.put("studentParentUserIds", studentParentUserIds);
		map.put("teacherAccountIds", teacherAccountIds);
		//根据ZHXY-440修改
		if(objType == 3) {
			//家长类型时，返回学生的accountId列表
			map.put("studentAccountList", list);
			map.put("accountlist", new ArrayList<Long>());
		} else {
			map.put("accountlist", list);
		}
		logger.info("ZHXY-440 getWXNoticePerson return map : " + map);
		return map;
	}

	@Override
	public Integer getNoticeTime(Map<String, Object> map) {
		 
		return questionnaireDao.getNoticeTime(map);
	}

	@Override
	public Integer updateNoticeTimes(Map<String, Object> map) {
		 
		return questionnaireDao.updateNoticeTimes(map);
	}

	/**
	 * 排除有记录的accountId
	 */
	@Override
	public List<Long> checkHasRecord(String schoolId,String questionId,Map<String, String> accountIds) {
		logger.info("checkHasRecord in : " + accountIds.toString());
		List<Long> list = new ArrayList<Long>();
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("questionId", questionId);
		List<JSONObject> recordList = questionnaireDao.queryRecordByUser(param); //已经填写了的人
		for(JSONObject record: recordList) {
			String accountId = record.getString("accountId");
			if(accountIds.containsKey(accountId)) {
				accountIds.remove(accountId);
				logger.info("checkHasRecord remove : " + accountId);
			}
		}
		
		//返回没有记录的accountId
		for(Map.Entry<String, String> item:accountIds.entrySet()) {
			list.add(Long.valueOf(item.getKey()));
		}
		logger.info("checkHasRecord out list : " + list.toString());
		logger.info("checkHasRecord out accountIds : " + accountIds.toString());
		return list;
	}

	@Override
	public List<JSONObject> getViewVoteDetail(JSONObject params) {
		List<JSONObject> list = questionnaireDao.getViewVoteDetail(params);  
		return list;
	}

	@Override
	public List<JSONObject> getSubjectResultByTargetId(Map<String, Object> map) {
		List<JSONObject> list = questionnaireDao.getSubjectResultByTargetId(map);
		return list;
	}
}
