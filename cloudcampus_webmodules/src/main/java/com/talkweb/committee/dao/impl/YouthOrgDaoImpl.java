package com.talkweb.committee.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.committee.dao.YouthOrgDao;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.csbasedata.util.DateUtil;
@Repository
public class YouthOrgDaoImpl  extends MyBatisBaseDaoImpl implements YouthOrgDao {

	@Override
	public void deleteYouthOrg(JSONObject param) {
		update("deleteYouthOrgYO",param);
		update("deleteYouthOrgPersonYO",param);
	}

	@Override
	public List<JSONObject> getYouthOrg(JSONObject param) {
		return selectList("getYouthOrgYO",param);
	}

	@Override
	public List<JSONObject> getYouthOrgPerson(JSONObject param) {
		return selectList("getYouthOrgPersonYO",param);
	}

	@Override
	public void addYouthOrgAndPersonBatch( String termInfoId, List<JSONObject> youthOrgList,
			List<JSONObject> youthOrgPersonList) {
		int i = 0;
		Map<String,String> oldIdNewIdRelationMap = new HashMap<String,String>();
		for(JSONObject youthOrg:youthOrgList){
			i++;
			String branchId = youthOrg.getString("branchId");
			String newBranchId = UUIDUtil.getUUID();
			oldIdNewIdRelationMap.put(branchId, newBranchId);
			youthOrg.put("branchId", newBranchId);
			youthOrg.put("termInfoId", termInfoId);
			youthOrg.put("createTime", DateUtil.getTimeAndAddOneSecond(i));
		}
		
		for(JSONObject youthOrgPerson:youthOrgPersonList){
			String branchId = youthOrgPerson.getString("branchId");
			String newBranchId = oldIdNewIdRelationMap.get(branchId);
			youthOrgPerson.put("termInfoId", termInfoId);
			youthOrgPerson.put("branchId", newBranchId);
		}
		if(youthOrgList.size()>0){
			update("addYouthOrgBatchYO",youthOrgList);
		}
		if(youthOrgPersonList.size()>0){
			update("addYouthOrgPersonBatchYO",youthOrgPersonList);
		}
	}

	@Override
	public void addYouthOrg(JSONObject youthOrg) {
		update("addYouthOrgYO",youthOrg);
		
	}

	@Override
	public void deleteYouthOrgPerson(JSONObject youthOrgPerson) {
		update("deleteYouthOrgPersonYO",youthOrgPerson);
	}

	@Override
	public void addYouthOrgPersonBatch(List<JSONObject> headsAndMembers) {
		update("addYouthOrgPersonBatchYO",headsAndMembers);
	}


}
