package com.talkweb.repairManage.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.repairManage.dao.RepairManageDao;
import com.talkweb.repairManage.service.RepairManageService;

@Service
public class RepairManageServiceImpl implements RepairManageService {

	@Autowired
	private RepairManageDao repairManageDao;
	@Autowired
	private AllCommonDataService commonDataService;
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Override
	public List<JSONObject> getRepairInfoList(JSONObject param) {
		return repairManageDao.getRepairInfoList(param);
	}
	
	@Override
	public List<JSONObject> getRepairInfoListPlus(JSONObject param) {
		return repairManageDao.getRepairInfoListPlus(param);
	}
	
	@Override
	public List<JSONObject> getOwnRepairInfoList(JSONObject param) {
		return repairManageDao.getOwnRepairInfoList(param);
	}
	@Override
	public int deleteRepairInfo(JSONObject param) {
		String repairId = param.getString("repairId");
		repairManageDao.deleteRepair_person(repairId);
		List<JSONObject> list = repairManageDao.selectRepairpictures(param);
		if (list!=null) {
			for (int i = 0; i < list.size(); i++) {
				try {
					fileServerImplFastDFS.deleteFile(list.get(i).getString("picUrl"));
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		
		repairManageDao.deleteRepairpicture(param);
		return repairManageDao.deleteRepairInfo(param);
	}
	@Override
	public int addFeedbackInfo(JSONObject param) {
		repairManageDao.updateFeedbackState(param);
		param.put("feedbackId", UUIDUtil.getUUID());
		return repairManageDao.addFeedbackInfo(param);
	}
	@Override
	public List<JSONObject> getFeedbackInfo(JSONObject param) {
		List<JSONObject> data = repairManageDao.getFeedbackInfo(param);
		return data;
		
	}
	@Override
	public int addRepairInfo(JSONObject param) {
		repairManageDao.addRepairInfo(param);
		List<JSONObject> lj = new ArrayList<JSONObject>();
		String repairId = param.getString("repairId");
		String isSendMsg = param.getString("isSendMsg");
		JSONArray arr = param.getJSONArray("repairPersons");
		for(int i = 0;i<arr.size();i++){
			JSONObject json = new JSONObject();
			json.put("repairId", repairId);
			json.put("isSendMsg", isSendMsg);
			json.put("repairPersonId", arr.getString(i));
			lj.add(json);
		}
		JSONArray attachmentIds = param.getJSONArray("attachmentIds");
		List<JSONObject> picList = new ArrayList<JSONObject>(); 
		if (attachmentIds!=null && attachmentIds.size() > 0) {
			for (int i = 0; i < attachmentIds.size(); i++) {
				JSONObject object = attachmentIds.getJSONObject(i);
				object.put("repairId", repairId);
				object.put("picName", object.getString("name"));
				object.put("picUrl", object.getString("accessUrl"));
				picList.add(object);
			}
			repairManageDao.insertRepairpicture(picList);
		}
 
		if(lj.size()>0){
			return repairManageDao.addRepair_person(lj);
		}else{
			return 0;
		}
	}
	@Override
	public List<JSONObject> getRepairTypeInfo(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getRepairTypeInfo(param);
	}
	@Override
	public List<JSONObject> getRepairPersonInfo(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getRepairPersonInfo(param);
	}
	@Override
	public JSONObject getRepairDetail(JSONObject param) {
		String repairId = param.getString("repairId");
		List<JSONObject> repairPersion = repairManageDao.getRepair_person(repairId);
		JSONObject repairDetail = repairManageDao.getRepairDetail(param);
		List<String> repairPersons = new ArrayList<String>();
		String isSendMsg = "0";
		for(JSONObject o:repairPersion){
			repairPersons.add(o.getString("repairPersonId"));
			isSendMsg = o.getString("isSendMsg");
		}
		if(repairDetail != null) {
			repairDetail.put("isSendMsg", isSendMsg);
			repairDetail.put("repairPersons", repairPersons);
		}
		return repairDetail;
	}
	@Override
	public JSONObject getAPPRepairDetail(JSONObject param){
		String repairId = param.getString("repairId");
		List<JSONObject> result = repairManageDao.getAPPRepair_person(repairId);
		JSONObject object = repairManageDao.getAPPRepairDetail(param);
		if(object != null) {
			object.put("repairPersons", result);
		}
		return object;
	}
	
	@Override
	public int updateCheckRepairInfo(JSONObject param) {
		repairManageDao.updateCheckRepairInfo(param);
		List<JSONObject> lj = new ArrayList<JSONObject>();
		String repairId = param.getString("repairId");
		String isSendMsg = param.getString("isSendMsg");
		JSONArray arr = param.getJSONArray("repairPersons");
		for(int i = 0;i<arr.size();i++){
			JSONObject json = new JSONObject();
			json.put("repairId", repairId);
			json.put("isSendMsg", isSendMsg);
			json.put("repairPersonId", arr.getString(i));
			lj.add(json);
		}
		repairManageDao.deleteRepair_person(repairId);
		if(lj.size()>0){
			return repairManageDao.addRepair_person(lj);
		}else{
			return 0;
		}
	}
	@Override
	public int updateRepairInfo(JSONObject param) {
		repairManageDao.updateRepairInfo(param);
		List<JSONObject> lj = new ArrayList<JSONObject>();
		String repairId = param.getString("repairId");
		String isSendMsg = param.getString("isSendMsg");
		JSONArray arr = param.getJSONArray("repairPersons");
		for(int i = 0;i<arr.size();i++){
			JSONObject json = new JSONObject();
			json.put("repairId", repairId);
			json.put("isSendMsg", isSendMsg);
			json.put("repairPersonId", arr.getString(i));
			lj.add(json);
		}
		repairManageDao.deleteRepair_person(repairId);
		if(lj.size()>0){
			return repairManageDao.addRepair_person(lj);
		}else{
			return 0;
		}
	}
	@Override
	public List<JSONObject> getRepairTypeList(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getRepairTypeList(param);
	}
	@Override
	public int deleteRepairTypeInfo(JSONObject param) {
		repairManageDao.deleteRepairPersonInfo(param);
		return repairManageDao.deleteRepairTypeInfo(param);
	}
	@Override
	public JSONObject getEditRepairType(JSONObject param) {
		JSONObject result = repairManageDao.getEditRepairType(param);
		String[] checkPersons = result.getString("checkPersons").split(",");
		result.put("checkPersons", checkPersons);
		return result;
	}
	@Override
	public int addRepairTypeInfo(JSONObject param) {
		JSONArray param1 = param.getJSONArray("repairPersons");
		for(int i=0;i<param1.size();i++){
			param1.getJSONObject(i).put("repairPersonId", UUID.randomUUID().toString());
			param1.getJSONObject(i).put("repairTypeId", param.getString("repairTypeId"));
			param1.getJSONObject(i).put("schoolId", param.getString("schoolId"));
		}
		
		JSONArray array = param.getJSONArray("checkPersons");
		String checkPersons = "";
		for(int i=0;i<array.size();i++){
			checkPersons+=array.getString(i);
			checkPersons+=",";
		}
		int index = checkPersons.lastIndexOf(",");
		if(index>-1){
			checkPersons = checkPersons.substring(0,index);
		}
		param.put("checkPersons", checkPersons);
		repairManageDao.addRepairTypeInfo(param);
		if(param1.size()>0){
			repairManageDao.addRepairPersonInfo(param1);
		}
		return 0;
	}
	@Override
	public int updateRepairTypeInfo(JSONObject param) {
		JSONArray param1 = param.getJSONArray("repairPersons");
		JSONArray array = param.getJSONArray("checkPersons");
		String checkPersons = "";
		for(int i=0;i<array.size();i++){
			checkPersons+=array.getString(i);
			checkPersons+=",";
		}
		int index = checkPersons.lastIndexOf(",");
		if(index>-1){
			checkPersons = checkPersons.substring(0,index);
		}
		param.put("checkPersons", checkPersons);
		repairManageDao.updateRepairTypeInfo(param);
		repairManageDao.deleteRepairPersonInfo(param);
		for(int i=0;i<param1.size();i++){
			JSONObject json = param1.getJSONObject(i);
			json.put("repairPersonId", UUIDUtil.getUUID());
			json.put("schoolId", param.getString("schoolId"));
			json.put("repairTypeId", param.getString("repairTypeId"));
		}
		if(param1.size()>0){
			repairManageDao.addRepairPersonInfo(param1);
		}
		return 0;
	}
	@Override
	public List<JSONObject> getRepairStatistics(JSONObject param) {
		String role = param.getString("role");
		String schoolId = param.getString("schoolId");
		Map<String,String> teacherIdName = getTeacherNameMap(schoolId);
		List<JSONObject> result;
		if("0".equals(role)){
			result = repairManageDao.getAdminRepairStatistics(param);
		}else{
			result = repairManageDao.getRepairStatistics(param);
		}
		List<String> repairIds = new ArrayList<String>();
		for(JSONObject json: result){
			String evalInfo = json.getString("evalInfo");
			String evalTemp = "";
			switch (evalInfo) {
			case "1":
				evalTemp = "不满意";
				break;
			case "2":
				evalTemp = "一般";
				break;
			case "3":
				evalTemp = "满意";
				break;
			case "4":
				evalTemp = "非常满意";
				break;

			default:
				break;
			}
			json.put("evalInfo", evalTemp);
			repairIds.add(json.getString("repairId"));
		}
		List<JSONObject> persons = null;
		List<JSONObject> feedbackInfos = null;
		if(result.size()>0){
			persons = repairManageDao.getAllRepairPersonByschoolId(repairIds);
			feedbackInfos = repairManageDao.getAllRepairFeedbackInfosById(repairIds);
		}
		for(JSONObject json:result){
			String repairId = json.getString("repairId");
			StringBuilder repairPersonName = new StringBuilder();
			StringBuilder repairFeedbackInfo = new StringBuilder();
			for(JSONObject person:persons){
				if(person.getString("repairId").equals(repairId)){
					repairPersonName.append(",").append(person.getString("repairPersonName"));
				}
			}
			if(CollectionUtils.isNotEmpty(feedbackInfos)){
				for(JSONObject feedback:feedbackInfos){
					if(feedback.getString("repairId").equals(repairId)){
						String feedbackPerson = feedback.getString("feedbackPerson");
						repairFeedbackInfo.append(";").append(feedback.getString("feedbackInfo"))
						.append("-").append(teacherIdName.get(feedbackPerson));
					}
				}
			}
			if(StringUtils.isNotBlank(repairPersonName.toString())){
			json.put("repairPersonName", repairPersonName.toString().substring(1));
			}else{
				json.put("repairPersonName", "");
			}
			if(StringUtils.isNotBlank(repairFeedbackInfo.toString())){
				json.put("feedbackInfo", repairFeedbackInfo.toString().substring(1));
			}else{
				json.put("feedbackInfo", "");
			}
		}
		return result;
	}
	@Override
	public String getIsCheckPerson(String repairTypeId) {
		// TODO Auto-generated method stub
		return repairManageDao.getIsCheckPerson(repairTypeId);
	}
	@Override
	public int getRepairTypeCount(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getRepairTypeCount(param);
	}
	@Override
	public List<String> getSelPersons(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getSelPersons(param);
	}
	@Override
	public List<JSONObject> getAllRepairInfoList(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getAllRepairInfoList(param);
	}
	@Override
	public String getPersonsByRepairId(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getPersonsByRepairId(param);
	}
	@Override
	public int addRepairEvalInfo(JSONObject param) {
		repairManageDao.updateRepairEvalState(param);
		return repairManageDao.addRepairEvalInfo(param);
	}

	public Map<String,String> getTeacherNameMap(String schoolId){
		String termInfo = commonDataService.getCurTermInfoId(Long.parseLong(schoolId));
		School school = commonDataService.getSchoolById(Long.parseLong(schoolId),termInfo);
		List<Account> ll = commonDataService.getAllSchoolEmployees(school,termInfo,"");
		Map<String,String> teacherIdName = new HashMap<String, String>();
		for(int i = 0;i<ll.size();i++){
			String teacherId = String.valueOf(ll.get(i).getId());
			String teacherName = ll.get(i).getName();
			teacherIdName.put(teacherId, teacherName);
		}
		return teacherIdName;
	}
	@Override
	public List<JSONObject> getAPPRepairInfoList(JSONObject param) {
		Calendar ca = Calendar.getInstance();
		Date endTime = ca.getTime();
		ca.add(Calendar.MONTH, -1);
		Date startTime = ca.getTime();
		param.put("startDate", startTime);
		param.put("endDate", endTime);
		List<JSONObject> data = repairManageDao.getAllRepairInfoList(param);
		return data;
	}
	@Override
	public int updateRepairEvalState(JSONObject param) {
		return repairManageDao.updateRepairEvalState(param);
	}
	@Override
	public JSONObject getAPPRepairCheckDetail(JSONObject param) {
	
		JSONObject repairDetail = repairManageDao.getAPPRepairDetailCheck(param);
		if(repairDetail != null) {
			String persons = repairManageDao.getPersonsByRepairId(param);
			if(StringUtils.isNotBlank(persons)){
				repairDetail.put("isCheckconfig", 0);
			}else{
				repairDetail.put("isCheckconfig", 1);
			}
			//getRepairPersonInfo
			//获取报修人
			List<String> repairPersons= new ArrayList<String>();
			List<JSONObject> result = repairManageDao.getRepair_person(param.getString("repairId"));
			String isSendMsg = "";
			for(JSONObject o:result){
				repairPersons.add(o.getString("repairPersonId"));
				isSendMsg = o.getString("isSendMsg");
			}
			//repairDetail.put("isSendMsg", isSendMsg);
			repairDetail.put("repairPersons", repairPersons);
		}
		return repairDetail;
	}
	@Override
	public List<JSONObject> getRepairPersonByIds(JSONObject param) {
		// TODO Auto-generated method stub
		return repairManageDao.getRepairPersonByIds(param);
	}
	@Override
	public int deleteRepairpicture(JSONObject param) {
        
		return repairManageDao.deleteRepairpicture(param);
	}
	@Override
	public List<JSONObject> selectRepairpictures(JSONObject param) {
		 
		return repairManageDao.selectRepairpictures(param);
	}
	@Override
	public int insertRepairpicture(JSONObject param) {
		List<JSONObject> list = new ArrayList<JSONObject>(); 
		list.add(param);
		return repairManageDao.insertRepairpicture(list);
	}

	@Override
	public JSONObject getRepairInfo(JSONObject param) {
		return repairManageDao.getRepairDetail(param);
	}
}
