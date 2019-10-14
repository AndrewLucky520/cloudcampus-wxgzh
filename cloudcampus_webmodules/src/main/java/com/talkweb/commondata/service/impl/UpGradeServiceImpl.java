package com.talkweb.commondata.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.commondata.dao.UpGradeDao;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.UpGradeService;

/**
 * @ClassName UpGradeServicImpl
 * @author zhanghuihui
 * @version 1.0
 * @date 2017.08.10
 */
@Service
public class UpGradeServiceImpl implements UpGradeService{
	@Autowired
	private UpGradeDao upGradeDao;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(UpGradeServiceImpl.class);
	@Override
	public void upGradeAllSchools(JSONObject json) throws Exception {
		List<JSONObject> tList = upGradeDao.getTermInfos(json);
		List<String> sIds = new ArrayList<String>();
		//uuid - startime和endTime
		Map<String,JSONObject>  timeMap = new HashMap<String,JSONObject>();
		for(JSONObject t:tList){
			String schoolId = t.getString("schoolId");
			String uuid = t.getString("uuid");
			Long s = t.getLongValue("startTime")*1000;
			Long e = t.getLongValue("endTime")*1000;
			Date sTime = new Date(s);
			Date eTime =  new Date(e);
			List<Long> dList = this.getDate(1, sTime, eTime);
			if(dList==null ||dList.size()<2){
				continue;
			}
			sIds.add(schoolId);
			if(timeMap.containsKey(uuid)){
				JSONObject timeObj = timeMap.get(uuid);
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}else{
				JSONObject timeObj = new JSONObject();
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}
		}
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : timeMap.entrySet()) {
			  JSONObject param = new JSONObject();
			  String uuid = entry.getKey();
			  param.put("uuid", uuid);
			  param.putAll(entry.getValue());
			  param.put("termInfoId",json.getString("termInfoId"));
			  jsonList.add(param);
		}
		logger.info("[UpGrade]updateGradeAllSchools出参："+jsonList.toString());
		for(int i=0;i<jsonList.size();i++){
			JSONObject param = jsonList.get(i);
			upGradeDao.updateGradeAllSchools(param);
		}
	}

	@Override
	public void deGradeAllSchools(JSONObject json) throws Exception {
		List<JSONObject> tList = upGradeDao.getTermInfos(json);
		List<String> sIds = new ArrayList<String>();
		//uuid - startime和endTime
		Map<String,JSONObject>  timeMap = new HashMap<String,JSONObject>();
		for(JSONObject t:tList){
			String schoolId = t.getString("schoolId");
			String uuid = t.getString("uuid");
			Long s = t.getLongValue("startTime")*1000;
			Long e = t.getLongValue("endTime")*1000;
			Date sTime = new Date(s);
			Date eTime =  new Date(e);
			List<Long> dList = this.getDate(-1, sTime, eTime);
			if(dList==null ||dList.size()<2){
				continue;
			}
			sIds.add(schoolId);
			if(timeMap.containsKey(uuid)){
				JSONObject timeObj = timeMap.get(uuid);
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}else{
				JSONObject timeObj = new JSONObject();
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}
		}
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : timeMap.entrySet()) {
			  JSONObject param = new JSONObject();
			  String uuid = entry.getKey();
			  param.put("uuid", uuid);
			  param.putAll(entry.getValue());
			  param.put("termInfoId",json.getString("termInfoId"));
			  jsonList.add(param);
		}
		logger.info("[UpGrade]deGradeAllSchools出参："+jsonList.toString());
		for(int i=0;i<jsonList.size();i++){
			JSONObject param = jsonList.get(i);
			upGradeDao.deGradeAllSchools(param);
		}
	}

	@Override
	public void upGradeBySchoolId(JSONObject json) throws Exception {
		List<JSONObject> tList = upGradeDao.getTermInfosBySchoolId(json);
		List<String> sIds = new ArrayList<String>();
		//uuid - startime和endTime
		Map<String,JSONObject>  timeMap = new HashMap<String,JSONObject>();
		for(JSONObject t:tList){
			String schoolId = t.getString("schoolId");
			String uuid = t.getString("uuid");
			Long s = t.getLongValue("startTime")*1000;
			Long e = t.getLongValue("endTime")*1000;
			Date sTime = new Date(s);
			Date eTime =  new Date(e);
			List<Long> dList = this.getDate(1, sTime, eTime);
			if(dList==null ||dList.size()<2){
				continue;
			}
			sIds.add(schoolId);
			if(timeMap.containsKey(uuid)){
				JSONObject timeObj = timeMap.get(uuid);
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}else{
				JSONObject timeObj = new JSONObject();
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}
		}
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : timeMap.entrySet()) {
			  JSONObject param = new JSONObject();
			  String uuid = entry.getKey();
			  param.put("uuid", uuid);
			  param.putAll(entry.getValue());
			  param.put("termInfoId",json.getString("termInfoId"));
			  jsonList.add(param);
		}
		logger.info("[UpGrade]upGradeBySchoolId出参："+jsonList.toString());
		for(int i=0;i<jsonList.size();i++){
			JSONObject param = jsonList.get(i);
			upGradeDao.upGradeBySchoolId(param);
		}
	}

	@Override
	public void deGradeBySchoolId(JSONObject json) throws Exception {
		List<JSONObject> tList = upGradeDao.getTermInfosBySchoolId(json);
		List<String> sIds = new ArrayList<String>();
		//uuid - startime和endTime
		Map<String,JSONObject>  timeMap = new HashMap<String,JSONObject>();
		for(JSONObject t:tList){
			String schoolId = t.getString("schoolId");
			String uuid = t.getString("uuid");
			Long s = t.getLongValue("startTime")*1000;
			Long e = t.getLongValue("endTime")*1000;
			Date sTime = new Date(s);
			Date eTime =  new Date(e);
			List<Long> dList = this.getDate(-1, sTime, eTime);
			if(dList==null ||dList.size()<2){
				continue;
			}
			sIds.add(schoolId);
			if(timeMap.containsKey(uuid)){
				JSONObject timeObj = timeMap.get(uuid);
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}else{
				JSONObject timeObj = new JSONObject();
				timeObj.put("startTime", dList.get(0));
				timeObj.put("endTime", dList.get(1));
				timeMap.put(uuid, timeObj);
			}
		}
		List<JSONObject> jsonList = new ArrayList<JSONObject>();
		for (Map.Entry<String, JSONObject> entry : timeMap.entrySet()) {
			  JSONObject param = new JSONObject();
			  String uuid = entry.getKey();
			  param.put("uuid", uuid);
			  param.putAll(entry.getValue());
			  param.put("termInfoId",json.getString("termInfoId"));
			  jsonList.add(param);
		}
		logger.info("[UpGrade]deGradeBySchoolId出参："+jsonList.toString());
		for(int i=0;i<jsonList.size();i++){
			JSONObject param = jsonList.get(i);
			upGradeDao.deGradeBySchoolId(param);
		}
	}

	@Override
	public List<JSONObject> getTermInfos(JSONObject json) throws Exception {
		logger.info("[UpGrade]getTermInfos入参："+ json.toJSONString());
		List<JSONObject> tList = upGradeDao.getTermInfos(json);
		logger.info("[UpGrade]getTermInfos出参："+tList.toString());
		return tList;
	}

	@Override
	public List<JSONObject> getTermInfosBySchoolId(JSONObject json) throws Exception {
		logger.info("[UpGrade]getTermInfosBySchoolId入参："+ json.toJSONString());
		List<JSONObject> tList  = upGradeDao.getTermInfosBySchoolId(json);
		logger.info("[UpGrade]getTermInfosBySchoolId出参："+tList.toString());
		return tList;
	}
	public List<Long> getDate(int dis,Date s,Date e) {
		List<Long> resultList = new ArrayList<Long>();
		//开始时间
		Calendar cldFirstTermStart = Calendar.getInstance();
		cldFirstTermStart.setTime(s);
		//结束时间
		Calendar cldFirstTermEnd =  Calendar.getInstance();
		cldFirstTermEnd.setTime(e);
		cldFirstTermStart.add(Calendar.YEAR, dis);
		cldFirstTermEnd.add(Calendar.YEAR, dis);
		
		Calendar[] cldArray = {cldFirstTermStart,cldFirstTermEnd};
		for(int i=0;i<cldArray.length;i++){
			Calendar oneCal = cldArray[i];
			resultList.add(oneCal.getTimeInMillis()/1000);
		}
		logger.info("[UpGrade]getDate入参："+dis+"sDate:"+s+"eDate:"+e+"出参："+resultList.toString());
		return resultList;
	}
}
