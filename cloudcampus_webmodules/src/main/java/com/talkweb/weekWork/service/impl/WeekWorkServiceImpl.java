package com.talkweb.weekWork.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.weekWork.dao.WeekWorkDao;
import com.talkweb.weekWork.pojo.ReportingPersonnel;
import com.talkweb.weekWork.pojo.WeeklyRecord;
import com.talkweb.weekWork.service.WeekWorkService;

@Service
public class WeekWorkServiceImpl implements WeekWorkService {

	@Autowired
	private WeekWorkDao weekWorkDao;
	@Autowired
	private AllCommonDataService commonDataService;
	
	Logger logger = LoggerFactory.getLogger(WeekWorkService.class);

	@Override
	public boolean getFillManInfo(JSONObject param) {
		return weekWorkDao.getFillManInfo(param);
	}

	@Override
	public Date createOrGetStartWeekByTermInfo(JSONObject param) {
		Date startTime = weekWorkDao.getTerminfoAndStartWeek(param);
		if(startTime != null) {
			return startTime;
		}
		
		int term = param.getIntValue("term");
		int schoolYear = param.getIntValue("schoolYear");
		if (term == 1) {
			 startTime  = DateUtil.parseDateDayFormat(schoolYear + "-09-01");
		}if (term == 2) {
			startTime  = DateUtil.parseDateDayFormat((schoolYear + 1) + "-03-01");
		}
		
		param.put("weekStartTime", startTime);
		insertTerminfoAndStartWeek(param);
		
		return startTime;
	}
	
	@Override
	public String getCurrentTermWeek(JSONObject param) {
		// TODO Auto-generated method stub
		return weekWorkDao.getCurrentTermWeek(param);
	}

	@Override
	public JSONObject updateOrGetWeeklyRecordDetail(JSONObject param) {
		JSONObject data = new JSONObject();
		WeeklyRecord weeklyRecord = weekWorkDao.getWeeklyRecordListByDepartment(param);
		if (weeklyRecord == null) { // if no week  work record, get the template
			JSONObject obj = this.getDepartmentById(param);// 
			 if (obj != null) {
				 data.put("content", obj.getString("content"));
			 }
			 if (StringUtil.isEmpty(data.getString("content"))) {
				 data.put("noContent", "1");
			 }
			data.put("version", "1");
			return data;
		}
		
		if (weeklyRecord.getVersion() == 1) {// has record and version is 1 .
			data.put("content", weeklyRecord.getContent());
			data.put("version", "1");
			return data;
		}
	 
		String type = param.getString("type");
		//先查询当前有没有填写内容
		int isFill = weekWorkDao.getFillRecord(param);
		List<JSONObject> tableHead;
		if(isFill>0){
			tableHead = weekWorkDao.getDistinctRecordDetail(param);
		}else{
			tableHead = weekWorkDao.getBaseRecordDetail(param);
		}
		List<JSONObject> lj = weekWorkDao.getWeeklyRecordDetail(param);
		for(JSONObject o:tableHead){
			List<JSONObject> sortContent = new ArrayList<JSONObject>();
			Map<String,List<JSONObject>> ml = new LinkedHashMap<>();
			//判断某行的内容是否全为空，若全为空则不显示
			Map<String,Integer> contentRowNullMap = new LinkedHashMap<>(); //row--为空的列个数
			for(JSONObject json:lj){
				String sortId = json.getString("sortId");
				String tableHeadId = json.getString("tableHeadId");
				String headRow = json.getString("tableRowsNum");
				String contentText = json.getString("content");
				if(!sortId.equals(o.getString("sortId")))continue;
				if(StringUtils.isBlank(contentText)){
					if(contentRowNullMap.containsKey(headRow)){
						int contentRowFlag2=contentRowNullMap.get(headRow);
						contentRowFlag2++;
						contentRowNullMap.put(headRow, contentRowFlag2);
					}else{
						contentRowNullMap.put(headRow,1);
					}
				}
				if(!ml.containsKey(headRow)){
					List<JSONObject> l_col = new ArrayList<JSONObject>();
					JSONObject object = new JSONObject();
					object.put("tableHeadId", tableHeadId);
					object.put("content", contentText);
					l_col.add(object);
					ml.put(headRow, l_col);
				}else{
					List<JSONObject> l_col = ml.get(headRow);
					JSONObject object = new JSONObject();
					object.put("tableHeadId", tableHeadId);
					object.put("content", contentText);
					l_col.add(object);
				}
			}
			for(Map.Entry<String, List<JSONObject>> en:ml.entrySet()){
				Integer totalCols = en.getValue().size();
				Integer cols=contentRowNullMap.get(en.getKey());
				if(StringUtils.isNotBlank(type)&& "0".equals(type)){ //查看
					if(cols==null || cols!=totalCols){  //填写列的内容全有|| 有部分有内容
						JSONObject sortJson = new JSONObject();
						sortJson.put("rowNum", en.getKey());
						sortJson.put("col", en.getValue());
						sortContent.add(sortJson);
					}
				}else{
					JSONObject sortJson = new JSONObject();
					sortJson.put("rowNum", en.getKey());
					sortJson.put("col", en.getValue());
					sortContent.add(sortJson);
				}
				
			}
			if(sortContent.size()==0){
				o.put("isShow", false);
			}else{
				o.put("isShow", true);
			}
			o.put("sortContent", sortContent);
		}
		
		// 若该类别下一个填写信息都无，则整个类别和内容都不显示
		List<JSONObject> tableHeadReturn = new ArrayList<JSONObject>();
		if(StringUtils.isNotBlank(type) && "0".equals(type)){ //查看
			for (JSONObject th : tableHead) {
					if(th.containsKey("isShow") && th.getBoolean("isShow")){
						tableHeadReturn.add(th);
					}
			}
		}else{
			tableHeadReturn.addAll(tableHead);
		}
		data.put("version", "0");
		data.put("weekWorkDetails", tableHeadReturn);
		return data;
	}

	@Override
	public List<JSONObject> getWeeklyRecordList(JSONObject param) {
		long accId = param.getLongValue("accountId");
		param.remove("accountId");
		String termInfoId = param.getString("termInfoId");
		param.remove("termInfoId");
		boolean isMoudleManager = param.getBooleanValue("isMoudleManager");	// 是否是管理员
		param.remove("isMoudleManager");
		long schoolId = param.getLongValue("schoolId");
		
		Set<Long> ids = new HashSet<Long>();
		List<String> departmentIds = new ArrayList<String>();
		List<ReportingPersonnel> reportingPersonnelList = weekWorkDao.queryReportingPersonnel(param);
		Map<String, Set<Long>> departId2PersonIds = new HashMap<String, Set<Long>>();
		for(ReportingPersonnel person : reportingPersonnelList) {
			String departmentId = person.getDepartmentId();
			if(!departId2PersonIds.containsKey(departmentId)) {
				departmentIds.add(departmentId);
				departId2PersonIds.put(departmentId, new HashSet<Long>());
			}
			departId2PersonIds.get(departmentId).add(person.getTeacherId());
			ids.add(person.getTeacherId());
		}
		
		List<Account> accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(ids), termInfoId);
		Map<Long, String> id2Name = new HashMap<Long, String>();
		if(CollectionUtils.isNotEmpty(accList)) {
			for(Account acc : accList) {
				id2Name.put(acc.getId(), acc.getName());
			}
		}
		
		List<WeeklyRecord> weeklyRecordList = new ArrayList<WeeklyRecord>();
		if(departmentIds.size() > 0) {
			param.put("departmentIds", departmentIds);
			weeklyRecordList = weekWorkDao.queryWeeklyRecordList(param);
		}
		
		List<JSONObject> response = new ArrayList<JSONObject>();
		int index = 1;
		for(WeeklyRecord record : weeklyRecordList) {
			JSONObject json = new JSONObject();
			json.put("departmentName", record.getDepartmentName());
			json.put("departmentId", record.getDepartmentId());
			
			Collection<Long> teacherIds = null;
			if(StringUtils.isNotBlank(record.getTeacherId())) {
				teacherIds = StringUtil.convertToListFromStr(record.getTeacherId(), ",", Long.class);
			} else {
				teacherIds = departId2PersonIds.get(record.getDepartmentId());
			}
			
			StringBuffer teacherName = new StringBuffer();
			for(Long teacherId : teacherIds) {
				if (StringUtils.isNotBlank(id2Name.get(teacherId))) {
					if (StringUtils.isBlank(id2Name.get(teacherId))) {
						continue;
					}
					teacherName.append(id2Name.get(teacherId)).append(",");
				}else {
					accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(teacherIds), termInfoId);
					if(CollectionUtils.isNotEmpty(accList)) {
						for(Account acc : accList) {
							id2Name.put(acc.getId(), acc.getName());
						}
					}
					if (StringUtils.isBlank(id2Name.get(teacherId))) {
						continue;
					}
					teacherName.append(id2Name.get(teacherId)).append(",");
				}
				
			}
			if (teacherName.length() > 0) {
				teacherName.deleteCharAt(teacherName.length() - 1);
			}
			json.put("teacherName", teacherName.toString());
			
			Date submitTime = record.getSubmitTime();
			if(submitTime != null) {	// 外链接关联，如果没提交，则submitTime为空
				json.put("submitTime", DateUtil.getDateFormat(submitTime));
				
				if(isMoudleManager) {	// 如果是管理员，能对所有周工作进行修改
					json.put("isOwn", 1);
				} else {	// 不是管理员，则需要对其进行判断是否能够修改
					if(departId2PersonIds.get(record.getDepartmentId()).contains(accId)) {
						json.put("isOwn", 1);
					} else {
						json.put("isOwn", 0);
					}
				}
			} else {
				json.put("isOwn", 2);	// 显示未提交
			}
			json.put("index", index);
			index ++;
			json.put("version", record.getVersion());
			response.add(json);
		}
		return response;
	}

	@Override
	public void updateWeeklyRecordDetail(JSONObject param) {
		Long schoolId = param.getLong("schoolId");
		String schoolYear = param.getString("schoolYear");
		String term = param.getString("term");
		Integer weekNum = param.getInteger("weekNum");
		Integer version = param.getInteger("version");
		String departmentId = param.getString("departmentId");
		Long accountId = param.getLong("accountId");
		
		WeeklyRecord weeklyRecord = weekWorkDao.getWeeklyRecordListByDepartment(param);
		
		if (weeklyRecord != null) {	// 存在记录
			String teacherId = weeklyRecord.getTeacherId();
			Set<Long> teacherIdSet = StringUtil.convertToSetFromStr(teacherId, ",", Long.class);
			if(!teacherIdSet.contains(accountId)) {	// 不包含accountId
				if(StringUtils.isEmpty(teacherId)) {
					weeklyRecord.setTeacherId(String.valueOf(accountId));
				} else {
					weeklyRecord.setTeacherId(teacherId + "," + accountId);
				}
			}
		}else{
			weeklyRecord = new WeeklyRecord();
			weeklyRecord.setDepartmentId(departmentId);
			weeklyRecord.setSchoolId(schoolId);
			weeklyRecord.setSchoolYear(schoolYear);
			weeklyRecord.setTerm(term);
			weeklyRecord.setWeek(weekNum);
			weeklyRecord.setVersion(version);
			weeklyRecord.setTeacherId(String.valueOf(accountId));
		}
		
		if (weeklyRecord.getVersion() == 1) {
			String content = param.getJSONObject("data").getString("content");
			content = StringEscapeUtils.unescapeHtml3(content);
			weeklyRecord.setContent(content);
			weeklyRecord.setSubmitTime(new Date());
			weeklyRecord.setContent(content);
			
			weekWorkDao.deleteWeeklyRecord(param);
			weekWorkDao.insertWeeklyRecord(weeklyRecord);
			return ;
		}
		
		//先查询当前有没有填写内容
		int isFill = weekWorkDao.getFillRecord(param);
		List<JSONObject> tableHead;
		if(isFill > 0){
			tableHead = weekWorkDao.getDistinctRecordDetail(param);
		}else{
			tableHead = weekWorkDao.getBaseRecordDetail(param);
			//现在content表中插入空值，第一个类别插入6条，以后每个类别插入三条
			List<JSONObject> initContent = new ArrayList<JSONObject>();
			for(int i=0;i<tableHead.size();i++){
				JSONObject table = tableHead.get(i);
				String sortId = table.getString("sortId");
				String sortName = table.getString("sortName");
				int sortNum = table.getIntValue("sortNum");
				int loopNum;
				if(i==0){
					loopNum = 6;
				}else{
					loopNum = 3;
				}
				JSONArray sortDetails = table.getJSONArray("sortDetails");
				if(CollectionUtils.isNotEmpty(sortDetails)){
					for(int j=0;j<sortDetails.size();j++){
						JSONObject sortDetail = sortDetails.getJSONObject(j);
						String tableHeadId = sortDetail.getString("tableHeadId");
						String tableHeadName = sortDetail.getString("tableHeadName");
						int tableColNum = j;
						for(int k=0;k<loopNum;k++){
							JSONObject content0 = new JSONObject();
							content0.put("schoolId", schoolId);
							content0.put("schoolYear", schoolYear);
							content0.put("term", term);
							content0.put("weekNum", weekNum);
							content0.put("departmentId", departmentId);
							content0.put("teacherId", weeklyRecord.getTeacherId());
							content0.put("sortId", sortId);
							content0.put("sortName", sortName);
							content0.put("sortNum", sortNum);
							content0.put("tableHeadId", tableHeadId);
							content0.put("tableHeadName", tableHeadName);
							content0.put("tableColNum", tableColNum);
							content0.put("tableRowsNum", k);
							content0.put("content", null);
							initContent.add(content0);
						}
					}
				}
			}
			
			if(initContent.size()>0){
				weekWorkDao.updateWeeklyContent(initContent);
			}
		}
		
		JSONArray weekWorkDetails = param.getJSONObject("data").getJSONArray("weekWorkDetails");
		List<JSONObject> updateRecords = new ArrayList<JSONObject>();
		List<JSONObject> deleteRecords = new ArrayList<JSONObject>();
		for(int i = 0;i<weekWorkDetails.size();i++){
			JSONObject workDetail = weekWorkDetails.getJSONObject(i);
			String sortId = workDetail.getString("sortId");
			String sortName = workDetail.getString("sortName");
			int sortNum = workDetail.getIntValue("sortNum");
			JSONArray sortDetails = workDetail.getJSONArray("sortDetails");
			Map<String,String> headIdName = new HashMap<String, String>();
			for(int j=0;j<sortDetails.size();j++){
				JSONObject sort = sortDetails.getJSONObject(j);
				String tableHeadId = sort.getString("tableHeadId");
				String tableHeadName = sort.getString("tableHeadName");
				headIdName.put(tableHeadId, tableHeadName);
			}
			
			JSONArray sortContent = workDetail.getJSONArray("sortContent");
			for(int m =0;m<sortContent.size();m++){
				JSONObject row = sortContent.getJSONObject(m);
				Integer rowNum = row.getInteger("rowNum");
				int type = row.getIntValue("type");
				JSONArray  col= row.getJSONArray("col");
				for(int n=0;n<col.size();n++){
					JSONObject rowContent = col.getJSONObject(n);
					String tableHeadId = rowContent.getString("tableHeadId");
					Integer tableColNum = rowContent.getInteger("tableColNum");
					String content = rowContent.getString("content");
					JSONObject oneContent = new JSONObject();
					oneContent.put("schoolId", schoolId);
					oneContent.put("schoolYear", schoolYear);
					oneContent.put("term", term);
					oneContent.put("weekNum", weekNum);
					oneContent.put("departmentId", departmentId);
					oneContent.put("teacherId", weeklyRecord.getTeacherId());
					oneContent.put("sortId", sortId);
					oneContent.put("sortName", sortName);
					oneContent.put("sortNum", sortNum);
					oneContent.put("tableHeadId", tableHeadId);
					oneContent.put("tableHeadName", headIdName.get(tableHeadId));
					oneContent.put("tableColNum", tableColNum);
					oneContent.put("tableRowsNum", rowNum);
					oneContent.put("content", content);
					if(type == 0){
						updateRecords.add(oneContent);
					}else{
						deleteRecords.add(oneContent);
					}
				}
			}
		}

		if(updateRecords.size() > 0 || deleteRecords.size() > 0){
			weekWorkDao.deleteWeeklyRecord(param);
			weekWorkDao.insertWeeklyRecord(weeklyRecord);
			if(deleteRecords.size()>0){
				for(JSONObject del : deleteRecords){
					weekWorkDao.deleteWeeklyContent(del);
				}
			}
			
			if(updateRecords.size() > 0){
				weekWorkDao.updateWeeklyContent(updateRecords);
			}
			return ;
		}
	}

	@Override
	public List<JSONObject> getDepartmentInfoByDataBase(JSONObject param) {
		// TODO Auto-generated method stub
		return weekWorkDao.getDepartmentInfoByDataBase(param);
	}

	@Override
	public int insertTerminfoAndStartWeek(JSONObject param) {
		weekWorkDao.deleteTerminfoAndStartWeek(param);
		return weekWorkDao.insertTerminfoAndStartWeek(param);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getReportingPersonList(JSONObject param) {
		Long schoolId = param.getLong("schoolId");
		String termInfoId = param.getString("termInfoId");
		
		Set<Long> idSet = new HashSet<Long>();
		List<JSONObject> list = weekWorkDao.getReportingPersonList(param);
		for(JSONObject json : list) {
			idSet.add(json.getLong("teacherId"));
		}
		
		Map<Long, String> id2Name = new HashMap<Long, String>();
		List<Account> accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(idSet), termInfoId);
		if(CollectionUtils.isNotEmpty(accList)) {
			for(Account acc : accList) {
				id2Name.put(acc.getId(), acc.getName());
			}
		}
		
		Map<String, JSONObject> id2Json = new HashMap<String, JSONObject>();
		List<JSONObject> result = new ArrayList<JSONObject>();
		int idx = 0;
		for(JSONObject json : list) {
			String departmentId = json.getString("departmentId");
			Long teacherId = json.getLong("teacherId");
			
			if(!id2Json.containsKey(departmentId)) {
				json.remove("teacherId");
				json.put("teachersInfo", new ArrayList<JSONObject>());
				idx ++;
				json.put("index", idx);
				
				id2Json.put(departmentId, json);
				result.add(json);
			}
            if (StringUtils.isBlank(id2Name.get(teacherId))) {
				continue;
			}
			JSONObject teacherInfo = new JSONObject();
			teacherInfo.put("teacherId", teacherId);
			teacherInfo.put("teacherName", id2Name.get(teacherId));
			
			List<JSONObject> subList =  ( List<JSONObject> )id2Json.get(departmentId).get("teachersInfo");
			subList.add(teacherInfo);
		}
		
		return result;
	}

	@Override
	public JSONObject getAppRecordDetail(JSONObject param) {
		JSONObject data = new JSONObject();
		 
		WeeklyRecord weeklyRecord = weekWorkDao.getWeeklyRecordListByDepartment(param);
		if (weeklyRecord == null) {// if no week  work record, get the template
			JSONObject obj = this.getDepartmentById(param);// 
			 if (obj!=null) {
				 data.put("content", obj.getString("content"));
			 }
			 if (StringUtil.isEmpty(data.getString("content"))) {
				 data.put("noContent", "1");
			 }
			data.put("version", "1");
			return data;
		}
		
		if (weeklyRecord.getVersion() == 1) {
			data.put("content", weeklyRecord.getContent());
			data.put("version", "1");
			return data;
		}
		
		List<JSONObject> lj = weekWorkDao.getWeeklyRecordDetail(param);
		if(CollectionUtils.isEmpty(lj)){
			return data;
		}
		Map<String, JSONObject> dt = new LinkedHashMap<String, JSONObject>();
		for(JSONObject o:lj){
			String sortId = o.getString("sortId");
			String sortName = o.getString("sortName");
			JSONObject oneSort = new JSONObject();
			oneSort.put("sortId", sortId);
			oneSort.put("sortName",sortName);
			dt.put(sortId, oneSort);
		}
		List<JSONObject> weekWorkDetails = new ArrayList<JSONObject>(dt.values());
		for(JSONObject o:weekWorkDetails){
			List<List<JSONObject>> sortContent = new ArrayList<List<JSONObject>>();
			List<JSONObject> rowContent = new ArrayList<JSONObject>();
			List<String> ltemp = new ArrayList<String>();
			for(int i=0;i<lj.size();i++){
				JSONObject json = lj.get(i);
				String sortId = json.getString("sortId");
				String headRow = json.getString("tableRowsNum");
				String tableHeadName = json.getString("tableHeadName");
				String contentText = json.getString("content");
				if(!sortId.equals(o.getString("sortId"))){
					if(!rowContent.isEmpty()){
						sortContent.add(rowContent);
						rowContent = new ArrayList<JSONObject>();
					}
					continue;
				}else{
					JSONObject content = new JSONObject();
					if(ltemp.isEmpty()){
						ltemp.add(headRow);
					}
					if(ltemp.contains(headRow)){
						content.put("content", contentText);
						content.put("tableHeadName", tableHeadName);
						rowContent.add(content);
						if(i==lj.size()-1){
							sortContent.add(rowContent);
						}
						
					}else{
						sortContent.add(rowContent);
						rowContent = new ArrayList<JSONObject>();
						content.put("content", contentText);
						content.put("tableHeadName", tableHeadName);
						rowContent.add(content);
						ltemp.add(headRow);
						if(i==lj.size()-1){
							sortContent.add(rowContent);
						}
					}
				}
			}
			o.put("sortContent", sortContent);
		}
		data.put("weekWorkDetails", weekWorkDetails);
		data.put("version", "0");
		return data;
	}


	@Override
	public List<JSONObject> getAllTeacherList(JSONObject param)
			throws Exception {
		List<JSONObject> rList = new ArrayList<JSONObject>();
		
		Long schoolId = param.getLong("schoolId");
		String termInfoId = param.getString("selectedSemester");
		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		String teacherName = param.getString("teacherName");
		if(teacherName == null) {
			teacherName = "";
		}
		
		List<Account> tList = commonDataService.getAllSchoolEmployees(school, termInfoId, teacherName.trim());//2016-12-06-dlm
		for (Account account : tList) {
			if(StringUtils.isBlank(account.getName())) {
				continue;
			}
			JSONObject line = new JSONObject();
			line.put("teacherId", account.getId());
			line.put("teacherName", account.getName());
			rList.add(line);
		}
		return rList;
	}
	

	@Override
	public int insertReportStyle(JSONObject param) throws Exception {
		int r = 1;
		String schoolId = param.getString("schoolId");
		String departmentId = param.getString("departmentId");
		JSONObject delParam = new JSONObject();
		delParam.put("schoolId", schoolId);
		delParam.put("departmentId", departmentId);
		List<JSONObject> weekWorkDetails = (List<JSONObject>) param
				.get("weekWorkDetails");

		List<JSONObject> weekWorkSorts = new ArrayList<JSONObject>();
		List<JSONObject> weekWorkTableHeads = new ArrayList<JSONObject>();
		List<String> weekWorkSortNames = new ArrayList<String>();//即将插入更新数据库的name，用来判断重名
		Map<String,List<String>> hNamesTobeInsertMap = new HashMap<String,List<String>>(); //即将插入更新数据库的name，用来判断重名

		if (CollectionUtils.isNotEmpty(weekWorkDetails)) {
			for (int i = 0; i < weekWorkDetails.size(); i++) {
				JSONObject weekWorkDetail = weekWorkDetails.get(i);
				JSONObject weekWorkSort = new JSONObject();
				String sortId = weekWorkDetail.getString("sortId");
				String sortName = weekWorkDetail.getString("sortName");
				if (StringUtils.isBlank(sortId)) {
					sortId = UUIDUtil.getUUID();
				}
				weekWorkSort.put("schoolId", schoolId);
				weekWorkSort.put("sortId", sortId);
				weekWorkSort.put("departmentId", departmentId);
				weekWorkSort.put("sortName",sortName);
				weekWorkSort.put("sortNum", i);
				weekWorkSort.put("inputTime",DateUtil.getDateFormatNow());
				weekWorkSorts.add(weekWorkSort);
				if(!weekWorkSortNames.contains(sortName)){
					weekWorkSortNames.add(sortName);
				}else{//判断类别传入的相互之间是否有重名
					r=-2;
					return r;
				}
				
				List<JSONObject> sortDetails = (List<JSONObject>) weekWorkDetail
						.get("sortDetails");
				for (int j = 0; j < sortDetails.size(); j++) {
					JSONObject sortDetail = sortDetails.get(j);
					JSONObject weekWorkTableHead = new JSONObject();
					String tableHeadId = sortDetail.getString("tableHeadId");
					String tableHeadName = sortDetail.getString("tableHeadName");
					if (StringUtils.isBlank(tableHeadId)) {
						tableHeadId = UUIDUtil.getUUID();
					}
					weekWorkTableHead.put("schoolId", schoolId);
					weekWorkTableHead.put("sortId", sortId);
					weekWorkTableHead.put("departmentId", departmentId);
					weekWorkTableHead.put("tableHeadId", tableHeadId);
					weekWorkTableHead.put("tableHeadName",tableHeadName);
					weekWorkTableHead.put("tableHeadNum", j);
					weekWorkTableHead.put("inputTime",
							DateUtil.getDateFormatNow());
					weekWorkTableHeads.add(weekWorkTableHead);
					
					List<String> hNames = hNamesTobeInsertMap.get(sortId);
					if (hNames == null) {
						hNames = new ArrayList<String>();
					}
					if (hNames.contains(tableHeadName)) {//相同的sortId下，判断表头传入的相互之间是否有重名
						r = -2;
						return r;
					}
					hNames.add(tableHeadName);
					hNamesTobeInsertMap.put(sortId, hNames);
				}
			}// end of for rolling
			//判断与数据库中已有的 是否重名
			/*if(r==1){
				String sNames = weekWorkDao.getWeekWorkSortNames(param);
				for (String sName : weekWorkSortNames) {
					if (sNames.contains(sName)) {
						r = -2;
						return r;
					}
				}
				Map<String,String> weekWorkTableHeadNamesMap = new HashMap<String, String>();
				List<JSONObject> hNames = weekWorkDao.getWeekWorkTableHeadNames(param);
				for(JSONObject tName:hNames){
					weekWorkTableHeadNamesMap.put(tName.getString("sortId"), tName.getString("tableHeadNames"));
				}
				for (Map.Entry<String, List<String> > entry : hNamesTobeInsertMap.entrySet()) {
					String hasNames = weekWorkTableHeadNamesMap.get(entry.getKey());
					List<String> NamesToBeInsert = entry.getValue();
					for(String nameToBeInsert:NamesToBeInsert){
						if(StringUtils.isNotBlank(hasNames) && hasNames.contains(nameToBeInsert)){
							r = -2;
							return r;
						}
					}
				}
				
			}*/
			if(r==1){
				weekWorkDao.deleteWeekWorkSortBySchoolId(delParam);
				weekWorkDao.deleteWeekWorkTableHeadBySchoolId(delParam);
				weekWorkDao.insertWeekWorkSortBatch(weekWorkSorts);
				weekWorkDao.insertWeekWorkTableHeadBatch(weekWorkTableHeads);
			}
		}// end of weekWorkDetails==null
		return r;
	}

	@Override
	public void deleteReportStyle(JSONObject param) throws Exception {
		weekWorkDao.deleteWeekWorkSortById(param);
		weekWorkDao.deleteWeekWorkTableHeadById(param);
	}

	@Override
	public void updateReportPerson(JSONObject param) {
		String departmentId = param.getString("departmentId");
		if(StringUtils.isBlank(departmentId)){
			departmentId = UUIDUtil.getUUID();
		}
		String departmentName = param.getString("departmentName");
		String schoolId = param.getString("schoolId");
		String content = param.getString("content");
		
		JSONArray teacherIds = param.getJSONArray("teacherIds");
		
		JSONObject depart = new JSONObject();
		depart.put("schoolId", schoolId);
		depart.put("departmentName", departmentName);
		depart.put("createDate", new Date());
		depart.put("content", content);
		depart.put("departmentId", departmentId);
		
		if(weekWorkDao.isExsitedSameDepartmentName(depart)) {
			throw new CommonRunException(-2, "设置部门名称不能相同！");
		}
		weekWorkDao.insertDepartment(depart);
		
		// 清空以前设置的该部门老师
		weekWorkDao.deleteTeacher(depart);
		
		if(CollectionUtils.isNotEmpty(teacherIds)) {
			Map<String, Object> teacherMap = new HashMap<String, Object>();
			teacherMap.put("schoolId", schoolId);
			teacherMap.put("departmentId", departmentId);
			teacherMap.put("teacherIds", teacherIds);
			weekWorkDao.insertTeacherBatch(teacherMap);
		}
	}

	@Override
	public void deleteReportPerson(JSONObject param) {
		weekWorkDao.deleteDepartment(param);
		weekWorkDao.deleteTeacher(param);
	}

	@Override
	public List<JSONObject> getBaseRecordDetail(JSONObject param) {
		// TODO Auto-generated method stub
		return weekWorkDao.getBaseRecordDetail(param);
	}

	@Override
	public List<JSONObject> getDepartmentList(JSONObject param) {
		// TODO Auto-generated method stub
		List<JSONObject> data = weekWorkDao.getDepartmentList(param);
		List<JSONObject> response = new ArrayList<JSONObject>();
		if(CollectionUtils.isNotEmpty(data)){
			for(JSONObject json:data){
				String departmentId = json.getString("departmentId");
				String departmentName = json.getString("departmentName");
				JSONObject o = new JSONObject();
				o.put("value", departmentId);
				o.put("text", departmentName);
				response.add(o);
			}
		}
		return response;
	}

	@Override
	public List<JSONObject> getMaxWeekFromRecord(JSONObject param) {
		// TODO Auto-generated method stub
		return weekWorkDao.getMaxWeekFromRecord(param);
	}
	
 

	@Override
	public List<JSONObject> getMaxWeekFromRecord2(JSONObject param) {
		// TODO Auto-generated method stub
		return weekWorkDao.getMaxWeekFromRecord2(param);
	}

	@Override
	public int delWeeklyRecordDetail(JSONObject param) {
		weekWorkDao.delWeeklyRecordDetail1(param);
		weekWorkDao.delWeeklyRecordDetail2(param);
		return 0;
	}
	/**
	 * 按周次获取所有已提交的周工作报表 getWeeklyRecordDetailAllDepartment
	 * @param JSONOBJ
	 * @return JSONOBJ
	 * @author zhh
	 */
	@Override
	public JSONObject getWeeklyRecordDetailAllDepartment(JSONObject param)
			throws Exception {
		JSONObject data = new JSONObject();
		String weekDate = param.getString("weekDate");
		String weekNum = param.getString("weekNum");
		String fileName = "工作周报" + weekDate;
		int isFill2 = weekWorkDao.getFillRecord2(param);
		if (isFill2 > 0) {
			List<JSONObject> contentList = weekWorkDao.getDistinctRecordDetail2(param);
			for(JSONObject table:contentList){ 
				String departmentName = table.getString("departmentName");
				String tableTitle = departmentName+"第"+weekNum+"周工作报表"+weekDate;
				table.put("tableTitle", tableTitle);
			}

			data.put("weekWorkTable", contentList);
			data.put("fileName", fileName);
			data.put("version" , "1");
			
			return data;
		}// when it's < 0 , maybe it's old data.
		
		
		//先查询当前有没有填写内容
		int isFill = weekWorkDao.getFillRecord(param);
		List<JSONObject> tables;
		if(isFill>0){
			tables = weekWorkDao.getDistinctRecordDetail1(param);
		}else{
			return null;
		}
		
		List<JSONObject> lj = weekWorkDao.getWeeklyRecordDetail(param);
		Map<String,Boolean> contentFlagMap=new HashMap<String, Boolean>(); //如果所有的content都为null 则无记录，并且导不出报表
		for(JSONObject table:tables){ 
			String departmentId = table.getString("departmentId");
			String departmentName = table.getString("departmentName");
			String tableTitle = departmentName+"第"+weekNum+"周工作报表"+weekDate;
			table.put("tableTitle", tableTitle);
			List<JSONObject> tableHead = (List<JSONObject>) table.get("weekWorkDetails");
			
			for(JSONObject o:tableHead){
				List<JSONObject> sortContent = new ArrayList<JSONObject>();
				Map<String,List<JSONObject>> ml = new LinkedHashMap<>();
				///判断某行的内容是否全为空，若全为空则不显示
				Map<String,Integer> contentRowNullMap = new LinkedHashMap<>(); //row--为空的列个数
				for(JSONObject json:lj){
					String departmentIdContent = json.getString("departmentId");
					String sortId = json.getString("sortId");
					String tableHeadId = json.getString("tableHeadId");
					String headRow = json.getString("tableRowsNum");
					String contentText = json.getString("content");
					if(!sortId.equals(o.getString("sortId")))continue;
					if(!departmentIdContent.equals(departmentId))continue;
					if(!ml.containsKey(headRow)){
						List<JSONObject> l_col = new ArrayList<JSONObject>();
						JSONObject object = new JSONObject();
						object.put("tableHeadId", tableHeadId);
						if( StringUtils.isBlank(contentText) ){
							object.put("content", "");
							if(contentRowNullMap.containsKey(headRow)){
								int contentRowFlag2=contentRowNullMap.get(headRow);
								contentRowFlag2++;
								contentRowNullMap.put(headRow, contentRowFlag2);
							}else{
								contentRowNullMap.put(headRow,1);
							}
						}else{
							object.put("content", contentText);
							contentFlagMap.put(departmentId, true);
						}
						l_col.add(object);
						ml.put(headRow, l_col);
					}else{
						List<JSONObject> l_col = ml.get(headRow);
						JSONObject object = new JSONObject();
						object.put("tableHeadId", tableHeadId);
						object.put("content", contentText);
						l_col.add(object);
						if( StringUtils.isBlank(contentText)){
							if(contentRowNullMap.containsKey(headRow)){
								int contentRowFlag2=contentRowNullMap.get(headRow);
								contentRowFlag2++;
								contentRowNullMap.put(headRow, contentRowFlag2);
							}else{
								contentRowNullMap.put(headRow,1);
							}
						}else{
							contentFlagMap.put(departmentId, true);
							
						}
						
					}
				}
				for(Map.Entry<String, List<JSONObject>> en:ml.entrySet()){
					Integer totalCols = en.getValue().size();
					Integer cols=contentRowNullMap.get(en.getKey());
					if(cols==null || cols!=totalCols){ //填写列的内容全有 || 部分填写
						JSONObject sortJson = new JSONObject();
						sortJson.put("rowNum", en.getKey());
						sortJson.put("col", en.getValue());
						sortContent.add(sortJson);
					}
				}
				o.put("sortContent", sortContent);
				if(sortContent.size()==0){
					o.put("isShow", false);
				}else{
					o.put("isShow", true);
				}
			}
		}
		//去掉某个报表，填写内容全为空的情况
		List<JSONObject> tablesReturn = new ArrayList<JSONObject>();
		for(JSONObject t:tables){
			String departmentId = t.getString("departmentId");
			if(contentFlagMap.containsKey(departmentId) && contentFlagMap.get(departmentId)!=null){
				boolean isReturn = contentFlagMap.get(departmentId);
				if(isReturn){ //为true则返回
					tablesReturn.add(t);
				}
			}
		}
		// 若该类别下一个填写信息都无，则整个类别和内容都不显示
		for (JSONObject t : tablesReturn) {
				List<JSONObject> weekWorkDetails = (List<JSONObject>) t.get("weekWorkDetails");
				List<JSONObject> weekWorkDetailsReturn = new ArrayList<JSONObject>();
				for (JSONObject wd : weekWorkDetails) {
					if(wd.getBooleanValue("isShow")){
						weekWorkDetailsReturn.add(wd);
					}
				}
				t.put("weekWorkDetails", weekWorkDetailsReturn);
		}
		
		data.put("weekWorkTable", tablesReturn);
		data.put("fileName", fileName);
		data.put("version" , "0");
		
		return data;
	}

	@Override
	public JSONObject getDepartmentById(JSONObject param) {
		JSONObject data = weekWorkDao.getDepartmentById(param);
		if(data != null && data.containsKey("content")) {
		} else {
			data.put("content", "");
		}
		return data;
	}

	@Override
	public List<JSONObject> getNoticePerson(JSONObject param) {
	    
		List<JSONObject> departmentTeacherList = weekWorkDao.getReportingPersonList(param);
		List<JSONObject> fillList = weekWorkDao.getFilledList(param);
		List<JSONObject> list = new ArrayList<JSONObject>();
		Map<String, String> fillMap = new HashMap<String, String>();
		for (int i = 0; i < fillList.size(); i++) {
			JSONObject fill = fillList.get(i);
			fillMap.put(fill.getString("departmentId"), "Y");// 部门已经填写了的
		}
		
		logger.info("fillMap==>" + fillMap); 
		logger.info("param==>" + param);
		
		 String departmentId = null;
		 String teacherId = null;
		for (int i = 0; i < departmentTeacherList.size(); i++) {
			JSONObject department = departmentTeacherList.get(i);
			departmentId = department.getString("departmentId");
			teacherId = department.getString("teacherId");
			String isFilled =  fillMap.get(departmentId);// 部门已经填写的
			logger.info("isFilled==>" + departmentId + "==" + isFilled);
			String departmentName = department.getString("departmentName");
			if (StringUtils.isEmpty(isFilled)) {//当前部门没有一个人填写
				JSONObject object = new JSONObject();
				object.put("teacherId", teacherId);
				object.put("departmentName", departmentName);
				list.add(object);
			} 
		}
		 
		return list;
	}

}
