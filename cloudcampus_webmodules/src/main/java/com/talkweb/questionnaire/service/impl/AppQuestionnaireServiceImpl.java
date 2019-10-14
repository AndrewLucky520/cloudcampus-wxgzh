package com.talkweb.questionnaire.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.questionnaire.dao.AppQuestionnaireDao;
import com.talkweb.questionnaire.dao.QuestionnaireDao;
import com.talkweb.questionnaire.service.AppQuestionnaireService;

@Service
public class AppQuestionnaireServiceImpl implements AppQuestionnaireService {
	@Autowired
	private AppQuestionnaireDao appQuestionnaireDao;
	
	private final static String[] units = { "", "十", "百", "千", "万", "十万", "百万", "千万", "亿"};

	private final static char[] numArray = { '零', '一', '二', '三', '四', '五', '六', '七', '八', '九' };

	@Value("#{settings['questionnaire.h5.pagesize']}")
	private int pageSize = 10;
	
	@Autowired
	private QuestionnaireDao questionnaireDao;

	
	@Override
	public JSONObject queryQuestionnaireByUser(JSONObject request) throws Exception {
		JSONObject data = new JSONObject();
		Long userId = request.getLong("userId");	// 用户id
		Long schoolId = request.getLong("schoolId");
		String questionId = request.getString("questionId");	// 问卷调查id
		Integer lastIndex = request.getInteger("lastIndex");	// 当前页
		boolean isRequestFirstPageData = request.getBooleanValue("isRequestFirstPageData");
		User user = appQuestionnaireDao.getUserById(schoolId, userId);	// 获取用户信息
		if(user == null){	// 如果找不到用户信息，抛出异常
			throw new RuntimeException("找不到用户信息");
		}
		// 获取账号id
		Long accountId = user.getAccountPart().getId();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("schoolId", schoolId);
		params.put("todayDate", DateUtil.getDateDayFormat());	// 今天日期
		params.put("accountId", accountId);
		
		int role = user.getUserPart().getRole().getValue();	// 判断账户角色
		//t_qn_obj数据库中，老师为1，学生为2，家长为3
		if(role == T_Role.Teacher.getValue()){	// 账户角色是老师
			// 老师只需要通过学校id和accountId去过滤查询
			params.put("objType", 1);
		}else if(role == T_Role.Student.getValue()){	// 账户角色是学生
			// 学生只需要通过班级id和学校id过滤查询
			params.put("objType", 2);
			params.put("classId", user.getStudentPart().getClassId());
		}else if(role == T_Role.Parent.getValue()){	// 账户角色是家长
			// 家长需要获取学生的班级来学校来过滤查询
			params.put("objType", 3);
			params.put("classId", user.getParentPart().getClassId());
			params.put("userId", userId);
		}else{	// 不是上述三种人员，直接返回空数据
			return data;
		}
		
		JSONObject question = null;
		if(StringUtil.isEmpty(questionId)){	
			// 如果没有从前端传递questionId，那么这是整个页面的第一次请求，需要查询所有的可以填写的问卷调查
			List<JSONObject> tabs = new ArrayList<JSONObject>();
			List<JSONObject> list = appQuestionnaireDao.queryAppQuestionnairesByUser(params);
			if(CollectionUtils.isEmpty(list)){	// 如果查询不到数据，直接返回
				return data;
			}

			List<String> recordIdList = new ArrayList<String>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String s = sdf.format(new Date());
			Date date =  sdf.parse(s);
            int index = 0;
            Iterator<JSONObject> iterator =  list.iterator();
			while (iterator.hasNext()) {
				JSONObject obj = iterator.next();
				Date questionEndDate =  obj.getDate("questionEndDate");
				 if (questionEndDate == null || questionEndDate.getTime() < date.getTime()) { // 去掉已经过期的
					 iterator.remove();
					 continue;
				}
				 Date questionStartDate =  obj.getDate("questionStartDate");
				 if (questionStartDate == null || questionStartDate.getTime() > date.getTime() ) {// 去掉还未开始
					 iterator.remove();
					 continue;
				}
				index = index + 1;
				String recordId = obj.getString("recordId");
				String questionType =  obj.getString("questionType");
				JSONObject json = new JSONObject();
				json.put("isSelected", 0);
				json.put("questionId", obj.getString("questionId"));
				if ("1".equals(questionType)) {
					recordIdList.clear();
					if (StringUtils.isNotEmpty(recordId)) {
						recordIdList.add(recordId);
						List<JSONObject> list2 = questionnaireDao.getRecorddetailfortableSubmit(recordIdList);
						if (list2.size() > 0) {
							json.put("isFilled", 0);
							if(question == null){ 	// 优先显示没有填写记录的问卷调查
								question = obj;
								json.put("isSelected", 1);
							}
						}else {
							json.put("isFilled", 1);
						}
					}else {
						json.put("isFilled", 0);
					}
					
					
				}else {
					if(StringUtils.isNotEmpty(recordId)){
						json.put("isFilled", 1);
					}else{
						json.put("isFilled", 0);
						if(question == null){ 	// 优先显示没有填写记录的问卷调查
							question = obj;
							json.put("isSelected", 1);
						}
					}
				}
				json.put("text", "问卷" + convertToChinaNumber(index));
				tabs.add(json);
			}
			if (list.size() == 0) {
				return data;
			}
		 
			if(question == null){	// 仍然为空，表示所有问卷调查都填写完成，那么只取第一个记录
				question = list.get(0);
				tabs.get(0).put("isSelected", 1);
			}
			data.put("tabs", tabs);
		} else {
			// 如果从前端有传递questionId，通过这个id查询数据
			params.put("questionId", questionId);
			List<JSONObject> list = appQuestionnaireDao.queryAppQuestionnairesByUser(params);
			if(CollectionUtils.isEmpty(list)){	// 如果查询不到数据，直接返回
				return data;
			}
			question = list.get(0);
			System.out.println( "params===" + params );
			List<JSONObject> record = appQuestionnaireDao.queryAppQuestionnairesByUser(params);
			System.out.println( "record===" + record );
			if (record!=null && record.size() > 0 && !StringUtils.isBlank(record.get(0).getString("recordId"))) {
				String questionType = question.getString("questionType");
				if ("1".equals(questionType)) {
					params.put("recordId", record.get(0).getString("recordId"));
					List<JSONObject> details = appQuestionnaireDao.getRecorddetailfortable(params);
					if (details== null || details.size() == 0) {
						question.put("isFill", 1);
					}else {
						JSONObject detail = details.get(0);
						if ("1".equals(detail.getString("isSubmit"))) {
							question.put("isFill", 1);
						}else {
							question.put("isFill", 0);
						}
					}
				}else {
					question.put("isFill", 1);
				}
				
			}else {
				question.put("isFill", 0);
			}
		}
	    String allowModify = null;
	    if (question!=null) {
	    	allowModify = question.getString("allowModify");
		}
		
		int questionType = question.getIntValue("questionType");
		String recordId = question.getString("recordId");
		questionId = question.getString("questionId");
		
		if(questionType == 1) {	// 表格形的指标
			if(!isRequestFirstPageData) {	// 如果不是第一页数据，其他数据已经加载了，只传questionByTable数据
				question = new JSONObject();	// 其他信息不应该带入数据中
				question.put("allowModify", allowModify);
			}
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("schoolId", schoolId);
			parameters.put("questionId", questionId);
			JSONObject ruleForTable = appQuestionnaireDao.queryRuleFortable(parameters);
			if(ruleForTable == null){
				return data;
			}
			String tableRuleId = ruleForTable.getString("tableRuleId");
			
			// 表格形的列数，主要用于计算分页
			int cols = ruleForTable.getIntValue("cols");
			parameters.put("startIndex", lastIndex * cols);
			parameters.put("dataLength", pageSize * cols);	//一次获取分页数据应该是cols的整数倍
			parameters.put("cols", cols);
			
			parameters.put("tableRuleId", tableRuleId);
			parameters.put("recordId", recordId);
			
			question.put("questionByTable", appQuestionnaireDao.queryOptionFortable(parameters));
			
			if(isRequestFirstPageData){	// 是第一页数据，则需要传maxVote,vote,tableRuleId
				int maxVote = ruleForTable.getIntValue("maxNum");
				int minNum = ruleForTable.getIntValue("minNum");
				if(maxVote != -1) {	//等于-1则是没有设置最大票数
					maxVote = Math.min(maxVote, (int) parameters.get("maxVote"));
				}else{
					maxVote = (int) parameters.get("maxVote");
				}
				question.put("vote", (int) parameters.get("vote"));
				question.put("maxVote", maxVote);
				question.put("minNum", minNum);
				question.put("tableRuleId", tableRuleId);
			}
		} else {	// 个性题的指标
			if(!isRequestFirstPageData) {	// 如果不是第一页数据，其他数据已经加载了，只传questionByTable数据
				question = new JSONObject();	// 其他信息不应该带入数据中
				question.put("allowModify", allowModify);
			}
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("schoolId", schoolId);
			parameters.put("questionId", questionId);
			parameters.put("recordId", recordId);
			
			parameters.put("startIndex", lastIndex);
			parameters.put("dataLength", pageSize);
			question.put("questionByTarget", appQuestionnaireDao.queryTargetForSpecific(parameters));
		}
		data.put("question", question);
		return data;
	}
	
	public static String convertToChinaNumber(long num){
		char[] val = String.valueOf(num).toCharArray();
		int len = val.length;
		if(len == 1){
			int n = Integer.valueOf(val[0]) - 48;
			return String.valueOf(numArray[n]);
		}
		
		StringBuilder result = new StringBuilder();
		int pre = 0;
		for (int i = 0; i < len; i++) {
			String unit = units[(len - 1) - i];
			int n = Integer.valueOf(val[i]) - 48;
			if(n == 0 && pre == 0){
				if(i == len - 1){	//如果最后一个还为0，那么删除最后一个0
					result.deleteCharAt(result.length() - 1);
				}
				continue;
			}else if(n == 0 && pre != 0){
				// pre不为0，而n为0，那么字符串添加0，如果知道最后一个字符都是0，那么将删除这个零字符
				result.append(numArray[n]);
			}else{
				result.append(numArray[n]);
				result.append(unit);
			}
			pre = n;
		}
		if(len == 2 && result.charAt(0) == '一'){
			result.deleteCharAt(0);
		}
		return result.toString();
	}

	@Override
	public void updateForSpecificTarget(JSONObject request) throws Exception {
		Long userId = request.getLong("userId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = appQuestionnaireDao.getCurTermInfoId(schoolId);
		String questionId = request.getString("questionId");
		JSONArray array = request.getJSONArray("questionByTarget");

		
		User user = appQuestionnaireDao.getUserById(schoolId, userId);	// 获取用户信息
		if(user == null){	// 如果找不到用户信息，抛出异常
			throw new RuntimeException("找不到用户信息");
		}
		Long accountId = user.getAccountPart().getId();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("questionId", questionId);
		params.put("schoolId", schoolId);
		params.put("accountId", accountId);
		
 
		int role = user.getUserPart().getRole().getValue();	// 判断账户角色
		if(role == T_Role.Parent.getValue()){
			long classId = user.getParentPart().getClassId();
			params.put("classId", classId);
			JSONObject gradeInfo = appQuestionnaireDao.getGradeByClassId(classId, schoolId, termInfo);
			params.put("usedGrade", gradeInfo.getString("usedGrade"));
			params.put("userId", userId);
		}else if(role == T_Role.Student.getValue()){	// 账户角色是学生
			// 学生只需要通过班级id和学校id过滤查询
			long classId = user.getStudentPart().getClassId();
			params.put("classId", classId);
			JSONObject gradeInfo = appQuestionnaireDao.getGradeByClassId(classId, schoolId, termInfo);
			params.put("usedGrade", gradeInfo.getString("usedGrade"));
			params.put("userId", "");
		}else if(role == T_Role.Teacher.getValue()){
			params.put("classId", null);
			params.put("usedGrade", null);
			params.put("userId", "");
		}else {
			return ;
		}
		
		String recordId = appQuestionnaireDao.getRecordIdByParams(params);
		
		if(recordId == null){
			recordId = UUIDUtil.getUUID();
			params.put("recordId", recordId);
			appQuestionnaireDao.insertRecord(params);
		}else{
			params.put("recordId", recordId);
			appQuestionnaireDao.deleteAppRecordDetailForSpecific(params);
		}
		
		if (array==null || array.size() == 0) { // 没有数据  就不要插一条记录到 t_qn_record
			JSONObject param = new JSONObject();
			param.put("accountId", accountId);
			param.put("schoolId", schoolId);
			param.put("questionId", questionId);
			appQuestionnaireDao.deleteAppRecord(param);
			return ;
		}
		
		appQuestionnaireDao.insertAppRecordDetailForSpecific(array, recordId, schoolId);
	}

	@Override
	public void updateForTableTarget(JSONObject request) throws Exception {
		Long userId = request.getLong("userId");
		Long schoolId = request.getLong("schoolId");
		String termInfo = appQuestionnaireDao.getCurTermInfoId(schoolId);
		String questionId = request.getString("questionId");
		String tableRuleId = request.getString("tableRuleId");
		Integer indexRow = request.getInteger("indexRow");
		String tableOptionId = request.getString("tableOptionId");

		User user = appQuestionnaireDao.getUserById(schoolId, userId);	// 获取用户信息
		if(user == null){	// 如果找不到用户信息，抛出异常
			throw new RuntimeException("找不到用户信息");
		}
		Long accountId = user.getAccountPart().getId();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("questionId", questionId);
		params.put("schoolId", schoolId);
		params.put("accountId", accountId);
		
		int role = user.getUserPart().getRole().getValue();	// 判断账户角色
		if(role == T_Role.Parent.getValue()){
			long classId = user.getParentPart().getClassId();
			params.put("classId", classId);
			JSONObject gradeInfo = appQuestionnaireDao.getGradeByClassId(classId, schoolId, termInfo);
			params.put("usedGrade", gradeInfo.getString("usedGrade"));
			params.put("userId", userId);
		}else if(role == T_Role.Student.getValue()){	// 账户角色是学生
			// 学生只需要通过班级id和学校id过滤查询
			long classId = user.getStudentPart().getClassId();
			params.put("classId", classId);
			JSONObject gradeInfo = appQuestionnaireDao.getGradeByClassId(classId, schoolId, termInfo);
			params.put("usedGrade", gradeInfo.getString("usedGrade"));
			params.put("userId", "");
		}else if(role == T_Role.Teacher.getValue()){
			params.put("classId", null);
			params.put("usedGrade", null);
			params.put("userId", "");
		}else {
			return ;
		}
		
		String recordId = appQuestionnaireDao.getRecordIdByParams(params);
		
		if(recordId == null){
			recordId = UUIDUtil.getUUID();
			params.put("recordId", recordId);
			appQuestionnaireDao.insertRecord(params);
		}else{
			params.put("recordId", recordId);
			params.put("indexRow", indexRow);
			params.put("tableRuleId", tableRuleId);
			appQuestionnaireDao.deleteRecordDetailForTable(params);
		}
		
		if(StringUtils.isNotEmpty(tableOptionId)){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("evalRecordResultId", UUIDUtil.getUUID());
			map.put("recordId", recordId);
			map.put("schoolId", schoolId);
			map.put("tableRuleId", tableRuleId);
			map.put("tableOptionId", tableOptionId);
			appQuestionnaireDao.insertRecordDetailForTable(map);
		}else {
			
			params.put("recordId", recordId);
			params.put("indexRow", indexRow);
			params.put("tableRuleId", tableRuleId);
			List<JSONObject> list = appQuestionnaireDao.getRecorddetailfortable(params);
			if (list.size() == 0) {
				JSONObject param = new JSONObject();
				param.put("accountId", accountId);
				param.put("schoolId", schoolId);
				param.put("questionId", questionId);
				appQuestionnaireDao.deleteAppRecord(param);// 没有 要删除Record 的记录
			}
				
				 
		}
		
	}
	
}
