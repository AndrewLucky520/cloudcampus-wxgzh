package com.talkweb.committee.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.committee.dao.YouthOrgDao;
import com.talkweb.committee.service.YouthOrgService;
import com.talkweb.committee.util.DateUtil;
import com.talkweb.committee.util.SortUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;

@Service
public class YouthOrgServiceImpl implements YouthOrgService {
	@Autowired
	private YouthOrgDao youthOrgDao;
	@Autowired
    private AllCommonDataService allCommonDataService;
	@Override
	public List<JSONObject> setGetYouthOrgList(JSONObject param) throws Exception {
		long schoolId = param.getLongValue("schoolId");
		String termInfoId = param.getString("termInfoId");
		String schoolYear = termInfoId.substring(0, 4);
		String termId = termInfoId.substring(4,5);
		
		List<JSONObject> youthOrgList = youthOrgDao.getYouthOrg(param);
		List<JSONObject> youthOrgPersonList = youthOrgDao.getYouthOrgPerson(param);
		if(youthOrgList==null || youthOrgList.size()==0){
			String newTermId = "1";
			String newSchoolYear = new String(schoolYear);
			//如果为空则自动取上个学期的数据过来
			if("1".equals(termId)){
				newTermId = "2";
				newSchoolYear = (Integer.parseInt(newSchoolYear)-1)+"";
			}
			String newTermInfoId = newSchoolYear+newTermId;
			//根据上个学期的数据自动继承并插入本学期
			JSONObject newParam = new JSONObject();
			newParam.put("schoolId", schoolId);
			newParam.put("termInfoId",  newTermInfoId);
			List<JSONObject> newYouthOrgList = youthOrgDao.getYouthOrg(newParam);
			List<JSONObject> newYouthOrgPersonList = youthOrgDao.getYouthOrgPerson(newParam);
			
			youthOrgDao.addYouthOrgAndPersonBatch(termInfoId,newYouthOrgList,newYouthOrgPersonList);
			
			youthOrgList = youthOrgDao.getYouthOrg(param);
			youthOrgPersonList = youthOrgDao.getYouthOrgPerson(param);
		}
		Map<String,String> nameMap =new HashMap<String,String>();
		
		if(youthOrgPersonList!=null && youthOrgPersonList.size()>0){
			Set<Long> accountIds = new HashSet<Long>();
			for(JSONObject youthOrgPerson :youthOrgPersonList){
				accountIds.add(youthOrgPerson.getLongValue("accountId"));
			}
			School school = allCommonDataService.getSchoolById(schoolId, termInfoId);
			List<Account> aList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(accountIds), termInfoId);
			for(Account a:aList){
				nameMap.put(a.getId()+"",a.getName());
			}
		}
		Map<String,List<JSONObject>>  youthOrgMap = new HashMap<String,List<JSONObject>>();
		for(JSONObject youthOrgPerson : youthOrgPersonList){
			String branchId = youthOrgPerson.getString("branchId");
			String accountId = youthOrgPerson.getString("accountId");
			String name = nameMap.get(accountId);
			if(StringUtils.isBlank(name)){
				name = "[已删除]";
			}
			youthOrgPerson.put("name", name);
			List<JSONObject> list = new ArrayList<JSONObject>();
			if(youthOrgMap.containsKey(branchId)){
				list = youthOrgMap.get(branchId);
			}
			list.add(youthOrgPerson);
			youthOrgMap.put(branchId, list);
		}
		for(Map.Entry<String,List<JSONObject>> entry: youthOrgMap.entrySet()) { 
			   List<JSONObject> list = entry.getValue() ;
			   SortUtil.sortNameJSONList(list,"name");
		}
		
		for(JSONObject youthOrg:youthOrgList){
			String branchId = youthOrg.getString("branchId");
			int number = 0 ;
			String heads = "";
			String members = "";
			if(!youthOrgMap.containsKey(branchId)){
				youthOrg.put("number", number);
				youthOrg.put("heads", heads);
				youthOrg.put("members", members);
				continue;
			}
			List<JSONObject> list = youthOrgMap.get(branchId);
			number = list.size();
			for(JSONObject obj:list){
				String type = obj.getString("type");
				String name = obj.getString("name");
				if("0".equals(type)){ //管理员
					heads+=name+",";
				}else if ("1".equals(type)){ //普通成员
					members+=name+",";
				}
			}
			if(heads.length()>0){
				heads=heads.substring(0,heads.length()-1);
			}
			if(members.length()>0){
				members=members.substring(0,members.length()-1);
			}
			youthOrg.put("number", number);
			youthOrg.put("heads", heads);
			youthOrg.put("members", members);
		}
		return youthOrgList;
	}

	@Override
	public int addYouthOrg(JSONObject param) throws Exception {
		String schoolId = param.getString("schoolId");
		String branchId = param.getString("branchId");
		String termInfoId = param.getString("termInfoId");
		String branchName = param.getString("branchName");
		String branchContent = param.getString("branchContent");
		String headAccountIds = param.getString("headAccountIds");
		String memberAccountIds = param.getString("memberAccountIds");
		List<String> headIdList = new ArrayList<String>();
		List<String> memberIdList = new ArrayList<String>();
		List<String> allIdList = new ArrayList<String>();
		//判断重名
		JSONObject repeatParam = new JSONObject();
		repeatParam.put("schoolId", schoolId);
		repeatParam.put("noBranchId", branchId);
		repeatParam.put("termInfoId", termInfoId);
 		List<JSONObject> repeatList = youthOrgDao.getYouthOrg(repeatParam);
		if(repeatList!=null && repeatList.size()>0){
			for(JSONObject repeat:repeatList){
				String branchNameRepeat = repeat.getString("branchName");
				if(branchName.equals(branchNameRepeat)){
					return -2;
				}
			}
		}
		
		if(StringUtils.isNotBlank(headAccountIds)){
			headIdList = Arrays.asList(headAccountIds.split(","));
		}
		if(StringUtils.isNotBlank(memberAccountIds)){
			memberIdList = Arrays.asList(memberAccountIds.split(","));
		}
		allIdList.addAll(headIdList);
		allIdList.addAll(memberIdList);
		if(StringUtils.isBlank(branchId)){ //添加
			branchId = UUIDUtil.getUUID();
		}
		List<JSONObject> headsAndMembers = new ArrayList<JSONObject>();
		for(String id:allIdList){
			JSONObject json = new JSONObject();
			json.put("schoolId", schoolId);
			json.put("termInfoId", termInfoId);
			json.put("branchId", branchId);
			json.put("accountId", id);
			if(headIdList.contains(id)){
				json.put("type","0");
			}else{
				json.put("type","1");
			}
			headsAndMembers.add(json);
		}
		if(StringUtils.isNotBlank(branchId)){ //添加
			//删除原来的人员关系
			JSONObject delete = new JSONObject();
			delete.put("termInfoId", termInfoId);
			delete.put("schoolId", schoolId);
			delete.put("branchId", branchId);
			youthOrgDao.deleteYouthOrgPerson(delete);
		}
		//添加支部表和支部人员
		JSONObject youthOrg = new JSONObject();
		youthOrg.put("termInfoId", termInfoId);
		youthOrg.put("schoolId", schoolId);
		youthOrg.put("branchId", branchId);
		youthOrg.put("branchName", branchName);
		youthOrg.put("branchContent", branchContent);
		youthOrg.put("createTime", DateUtil.getTimeAndAddOneSecond(0));
		youthOrgDao.addYouthOrg(youthOrg);
		if(headsAndMembers.size()>0){
			youthOrgDao.addYouthOrgPersonBatch(headsAndMembers);
		}
		return 1;
	}

	@Override
	public void deleteYouthOrg(JSONObject param) throws Exception {
		youthOrgDao.deleteYouthOrg(param);
	}

	@Override
	public JSONObject getYouthOrgDetail(JSONObject param) throws Exception {
		List<JSONObject> youthOrgs = youthOrgDao.getYouthOrg(param);
		JSONObject youthOrg = new JSONObject();
		if(youthOrgs==null || youthOrgs.size()<1){
			return null;
		}
		youthOrg = youthOrgs.get(0);
		List<JSONObject> youthOrgPersons = youthOrgDao.getYouthOrgPerson(param);
		List<JSONObject> heads = new ArrayList<JSONObject>();
		List<JSONObject> members = new ArrayList<JSONObject>();
		
		String termInfoId = param.getString("termInfoId");
		long schoolId = param.getLongValue("schoolId");

		Set<Long> aIds = new HashSet<Long>();
		for(JSONObject youthOrgPerson:youthOrgPersons){
			aIds.add(youthOrgPerson.getLongValue("accountId"));
		}
		List<Account> aList = allCommonDataService.getAccountBatch(schoolId, new ArrayList<Long>(aIds), termInfoId);
		Map<String,String> nameMap = new HashMap<String,String>();
		for(Account a:aList){
			nameMap.put(a.getId()+"", a.getName());
		}
		for(JSONObject youthOrgPerson:youthOrgPersons){
			String accountId = youthOrgPerson.getString("accountId");
			String type = youthOrgPerson.getString("type");
			String name = nameMap.get(accountId);
			if(StringUtils.isBlank(name)){
				name =  "[已删除]";
			}
			youthOrgPerson.put("name", name);
			if("0".equals(type)){
				heads.add(youthOrgPerson); //管理员
			}else{
				members.add(youthOrgPerson);//普通成员
			}
		}
		SortUtil.sortNameJSONList(heads,"name");
		SortUtil.sortNameJSONList(members,"name");
		youthOrg.put("heads", heads);
		youthOrg.put("members", members);
		return youthOrg;
	}

}
