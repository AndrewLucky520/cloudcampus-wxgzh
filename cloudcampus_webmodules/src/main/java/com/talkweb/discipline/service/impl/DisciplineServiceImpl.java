package com.talkweb.discipline.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.discipline.dao.DisciplineDao;
import com.talkweb.discipline.domain.DisciplineDetail;
import com.talkweb.discipline.domain.DisciplineExcel;
import com.talkweb.discipline.service.DisciplineService;
import com.talkweb.utils.KafkaUtils;

@Service
public class DisciplineServiceImpl implements DisciplineService{
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AllCommonDataService commonDataService;
	  
	@Autowired
	private  DisciplineDao disciplineDao; 
	
	@Value("#{settings['discipline.msgUrlPc']}")
	private String msgUrlPc;
	
	@Value("#{settings['discipline.msgUrlApp']}")
	private String msgUrlApp;
	
	@Value("#{settings['kafkaClientId']}")
	private String clientId;

	@Value("#{settings['kafkaClientSecret']}")
	private String clientSecret;
	
	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
	
	@Override
	public int addDiscipline(JSONObject param) {
		 disciplineDao.addRecordInfo(param);
		return disciplineDao.addDiscipline(param);
	}

	@Override
	public int updateDiscipline(JSONObject param) {
		 
		return disciplineDao.updateDiscipline(param);
	}
	
	

	@Override
	public int deleteDiscipline(JSONObject param) {
		disciplineDao.deleteDisciplineExcel(param);
		disciplineDao.deleteDisciplineDetail(param);
		return disciplineDao.deleteDiscipline(param);
	}

	@Override
	public int updateDisciplinePublished(JSONObject param) {
		Integer isPublished = param.getInteger("isPublished");
		Integer publishFlag = param.getInteger("publishFlag");
		Integer publishTeacherFlag = param.getInteger("publishTeacherFlag");
		JSONObject object = disciplineDao.getDiscipline(param);
		if (isPublished==null) {
			Integer publishFlagTmp =  object.getInteger("publishFlag");
			Integer publishTeacherFlagTmp =  object.getInteger("publishTeacherFlag");
			if (publishFlag!=null && publishFlag == 0 ) {
				if (publishTeacherFlagTmp!=null ) {
					param.put("isPublished", publishTeacherFlagTmp);
				}
			}
			if (publishTeacherFlag!=null && publishTeacherFlag == 0 ) {
				if (publishFlagTmp!=null ) {
					param.put("isPublished", publishFlagTmp);
				}
			}
			
			
		}
		return disciplineDao.updateDisciplinePublished(param);
	}

	@Override
	public List<JSONObject> getDisciplineList(JSONObject param) {
		 
		return disciplineDao.getDisciplineList(param);
	}

	@Override
	public JSONObject getDisciplines(JSONObject param) {

		JSONObject data = new JSONObject();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<List<JSONObject>> columns = new ArrayList<List<JSONObject>>();
		List<JSONObject> l_se = disciplineDao.getAllDisciplineExcel(param);
		List<JSONObject> l_sd = disciplineDao.getAllDisciplineDetail(param);
		
		
		HashMap<Integer, JSONObject> map = new HashMap<Integer, JSONObject>();
		for (int i = 0; i < l_sd.size(); i++) {
			JSONObject sd = l_sd.get(i);
			Integer row = sd.getInteger("rowNum");
			if (map.get(row)!=null) {
				JSONObject rowInfo = map.get(row);
				rowInfo.put("r0c0", sd.getString("classId"));
				rowInfo.put(sd.getString("disciComponentId"), sd.getString("score"));
			}else {
				JSONObject rowInfo = new JSONObject();
				rowInfo.put(sd.getString("disciComponentId"), sd.getString("score"));
				rowInfo.put("r0c0", sd.getString("classId"));
				map.put(row, rowInfo);
			}
		}

		 for (Integer in : map.keySet()) {
			  JSONObject object = map.get(in);
			  rows.add(object);
		 } 
		
		
 	
		
		Integer headRowNum =0;
		if(l_se.size()>0){
			headRowNum = l_se.get(0).getInteger("headRowNum");
		}
		for(int i=0;i<headRowNum;i++){
			List<JSONObject> l_json = new ArrayList<JSONObject>();
			for(JSONObject se:l_se){
				int rowNum = se.getInteger("rowNum");
				int rowspan = se.getInteger("rowspan");
				int ColNum = se.getIntValue("colNum");
				if(rowNum == i){
					JSONObject headInfo = new JSONObject();
					if(headRowNum == rowNum + rowspan){
						headInfo.put("field", se.getString("disciComponentId"));
						if(ColNum==0){
							headInfo.put("field","r0c0");
						}
					}else{
						headInfo.put("field", null);
					}
					headInfo.put("title", se.getString("disciComponentName"));
					headInfo.put("align", "center");
					headInfo.put("width", 100);
					headInfo.put("rowspan", rowspan);
					headInfo.put("colspan", se.getInteger("colspan"));
					headInfo.put("sortable", false);
					l_json.add(headInfo);
				}
			}
			columns.add(l_json);
		}
 	
		data.put("total", rows.size());
		data.put("rows", rows);
		if(rows.size()>0){
			data.put("columns", columns);
		}
		
		return data;
	}
	
	@Override
	public JSONObject getPersonalDiscipline(JSONObject param) {
		JSONObject data = new JSONObject();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		List<List<JSONObject>> columns = new ArrayList<List<JSONObject>>();
		List<JSONObject> l_se = disciplineDao.getAllDisciplineExcel(param);
		List<JSONObject> l_sd = disciplineDao.getAllDisciplineDetail(param);
		
		//过滤重复元素
		List<String> classIds = new ArrayList<String>();
		if(!CollectionUtils.isEmpty(l_sd)){
			for(JSONObject sd:l_sd){
				if(!classIds.contains(sd.getString("classId"))){
					classIds.add(sd.getString("classId"));
				}
			}
		}
		
		for(int i=0;i<classIds.size();i++){
			JSONObject rowInfo = new JSONObject();
			//第一列为教师姓名列
			rowInfo.put("r0c0", classIds.get(i));
			for(JSONObject sd:l_sd){
				if(classIds.get(i).equals(sd.getString("classId"))){
					rowInfo.put(sd.getString("disciComponentId"), sd.getString("score"));
				}
			}
			rows.add(rowInfo);
		}
		
		
		Integer headRowNum =0;
		if(l_se.size()>0){
			headRowNum = l_se.get(0).getInteger("headRowNum");
		}
		for(int i=0;i<headRowNum;i++){
			List<JSONObject> l_json = new ArrayList<JSONObject>();
			for(JSONObject se:l_se){
				int rowNum = se.getInteger("rowNum");
				int rowspan = se.getInteger("rowspan");
				int ColNum = se.getIntValue("colNum");
				if(rowNum == i){
					JSONObject headInfo = new JSONObject();
					if(headRowNum == rowNum + rowspan){
						headInfo.put("field", se.getString("disciComponentId"));
	 
						if(ColNum==0){
							headInfo.put("field","r0c0");
						}
					}else{
						headInfo.put("field", null);
					}
					headInfo.put("title", se.getString("disciComponentName"));
					headInfo.put("align", "center");
					headInfo.put("width", 100);
					headInfo.put("rowspan", rowspan);
					headInfo.put("colspan", se.getInteger("colspan"));
					headInfo.put("sortable", false);
					l_json.add(headInfo);
				}
			}
			columns.add(l_json);
		}
	
		data.put("total", classIds.size());
		data.put("rows", rows);
		if(rows.size()>0){
			data.put("columns", columns);
		}
		return data;
	
	}
	

	@Override
	public int addImportDiscipline(List<DisciplineDetail> successInfos, List<DisciplineExcel> l_disciExcel) {

		List<JSONObject> l_params = new ArrayList<JSONObject>();
		String disciplineId = successInfos.get(0).getDisciplineId();
		for(DisciplineDetail sd:successInfos){
			for(int i =0;i<sd.getDisciComponentIds().size();i++){
				JSONObject object = new JSONObject();
				object.put("disciplineId", sd.getDisciplineId());
				object.put("schoolId", sd.getSchoolId());
				object.put("classId", sd.getClassId());
				object.put("disciComponentId", sd.getDisciComponentIds().get(i));
				object.put("score", sd.getScores().get(i));
				object.put("rowNum", sd.getRowNum());
				l_params.add(object);
			}
		}
 
		JSONObject json = new JSONObject();
		json.put("disciplineId", disciplineId);
		disciplineDao.deleteDisciplineExcel(json);
		disciplineDao.deleteDisciplineDetail(json);
		
		disciplineDao.insertDisciExcel(l_disciExcel);
		int result = disciplineDao.insertDisciDetail(l_params);
		disciplineDao.updateDisciplineImported(json);
		return result;
 
	}

	@Override
	public List<JSONObject> getAPPClassList(JSONObject param) {
		return disciplineDao.getAPPClassList(param);
	}

	public void sendKafka(JSONObject input, Boolean isTeacher, JSONArray msgCenterReceiversArray) {
		logger.info("--------enroll send msg : {}", input.toJSONString());
		String schoolId = input.getString("schoolId");
		// 发送消息通知
		String uuid = UUID.randomUUID().toString().replace("-", "");
		// 发送到消息中心
		JSONObject msgCenterPayLoad = new JSONObject();
		JSONObject msg = new JSONObject();
		msg.put("msgId", uuid);
		msg.put("msgTitle", input.getString("disciplineName"));
		msg.put("msgContent", input.getString("disciplineName") + "班纪班风结果已经发布！");
		msg.put("msgUrlPc", msgUrlPc);
		msg.put("msgUrlApp", msgUrlApp + "#/?name=disciplineDetail&type=2&id=" + input.getString("disciplineId"));
		msg.put("msgOrigin", "班纪班风");
		msg.put("msgTypeCode", "BJBF");
		msg.put("msgTemplateType", "BJTZ");
		msg.put("schoolId", schoolId);

		JSONObject dataObj = new JSONObject();

		JSONObject first = new JSONObject();
		if(isTeacher){
			first.put("value", "老师,你好!");
		}else{
			first.put("value", "家长,你好!");
		}
		dataObj.put("first", first);

		JSONObject keyword1 = new JSONObject();
		keyword1.put("value", "全部班级");
		dataObj.put("keyword1", keyword1);

		JSONObject keyword2 = new JSONObject();
		keyword2.put("value",  "班纪班风管理员");
		dataObj.put("keyword2", keyword2);

		JSONObject Keyword3 = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		String str = sdf.format(new Date());
		Keyword3.put("value", str);
		dataObj.put("keyword3", Keyword3);

		JSONObject Keyword4 = new JSONObject();
		String k4 = new StringBuffer().append("《").append(input.getString("disciplineName")).append("》").append("班纪班风结果已经发布，请点击详情进行查看！").toString();
		Keyword4.put("value", k4);
		dataObj.put("keyword4", Keyword4);

		JSONObject remark = new JSONObject();
		remark.put("value", "");
		dataObj.put("remark", remark);
		dataObj.put("url", msgUrlApp + "#/?name=disciplineDetail&type=2&id=" + input.getString("disciplineId"));
		msg.put("msgWxJson", dataObj);
		msgCenterPayLoad.put("msg", msg);
		msgCenterPayLoad.put("receivers", msgCenterReceiversArray);
		logger.info("========send kafka msg : {}", msgCenterPayLoad.toJSONString());
		logger.info("=========send kafka url : {}", kafkaUrl);
		KafkaUtils.sendAppMsg(kafkaUrl, uuid, msgCenterPayLoad, "HDBM", clientId, clientSecret);
	}

	@Override
	public JSONObject getDiscipline(JSONObject param) {
		return disciplineDao.getDiscipline(param);
	}

	@Override
	public List<JSONObject> queryParentInfos(JSONObject param) {
		/** 根据ZHXY-440修改 **/
		@SuppressWarnings("unchecked")
		List<Long> classIds = (List<Long>) param.get("classList");
		String termInfoId = param.getString("termInfoId");
		long schoolId = param.getLongValue("schoolId");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("termInfoId", termInfoId);
		map.put("classId", StringUtils.join(classIds.toArray(), ","));
		logger.info("queryParentInfos getStudentList map : " + map);
		List<Account> studentAccounts = commonDataService.getStudentList(map);
		logger.info("queryParentInfos getStudentList studentAccounts : " + studentAccounts.toString());
		List<Long> accountIds = new ArrayList<Long>();
		for(Account each : studentAccounts) {
			accountIds.add(Long.valueOf(each.getId()));
		}
		logger.info("queryParentInfos accountIds : " + accountIds);
		return commonDataService.getSimpleParentByStuMsg(accountIds, termInfoId, schoolId);
//		return disciplineDao.queryParentInfos(param);
	}

	@Override
	public List<JSONObject> queryTeacherInfos(JSONObject param) {
		return disciplineDao.queryTeacherInfos(param);
	}

	@Override
	public List<JSONObject> getClassList(JSONObject param) {
		return disciplineDao.getClassList(param);
	}

	@Override
	public JSONObject getRecordInfo(JSONObject param) {
		return disciplineDao.getRecordInfo(param);
	}

	@Override
	public int addRecordInfo(JSONObject param) {
		return disciplineDao.addRecordInfo(param);
	}

	@Override
	public int updateRecordInfo(JSONObject param) {
		return disciplineDao.updateRecordInfo(param);
	}

}
