package com.talkweb.community.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.community.dao.CommunityDao;

@Repository
public class CommunityDaoImpl extends MyBatisBaseDaoImpl  implements CommunityDao {

	@Override
	public List<JSONObject> getCommunities(JSONObject params) {
		// TODO Auto-generated method stu
		List<JSONObject> communities = this.selectList("getCommunities",params);
		if(communities.size()!=0){
			params.put("communities", communities);
			List<JSONObject> members = this.selectList("selectCommunityMembers",params);
			Map<String,List<JSONObject>> memberMap = new HashMap();
			for(JSONObject memberJSON:members){
				String communityId = memberJSON.getString("communityId");
				List<JSONObject> memberList= memberMap.get(communityId);
				if(memberList==null){
					memberList = new ArrayList();
					memberMap.put(communityId,memberList);
				}
				memberList.add(memberJSON);
			}
			for(JSONObject communityJSON:communities){
				String communityId = communityJSON.getString("communityId");
				communityJSON.put("members", memberMap.get(communityId)==null?new ArrayList():memberMap.get(communityId));
			}
		}
		return communities;
	}

	@Override
	public int insertCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		int result = -1;
		String communityId = UUIDUtil.getUUID();
		params.put("communityId", communityId);
		JSONObject isExistJSON = this.selectOne("selectCommunityNameExist", params);
		if(isExistJSON.getIntValue("isExist") !=0){
			result = -2;
			return result;
		}
		result = this.insert("insertCommunity", params);
		if(params.getJSONArray("members")!=null&&params.getJSONArray("members").size()!=0){
			result = this.insert("insertCommunityMember",params);
		}
		return result;
	}

	@Override
	public int updateCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		int result = -1;
		JSONObject isExistJSON = this.selectOne("selectCommunityNameExist", params);
		if(isExistJSON.getIntValue("isExist") !=0){
			result = -2;
			return result;
		}
		result = this.delete("deleteCommunityMember",params);
		if(params.getJSONArray("members")!=null&&params.getJSONArray("members").size()!=0){
			result = this.insert("insertCommunityMember",params);
		}
		result = this.update("updateCommunity", params);
		return result ;
	}

	@Override
	public int deleteCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		int result = -1;
		result = this.delete("deleteCommunityMember",params);
		result = this.delete("deleteCommunity", params);
		return result;
	}

	@Override
	public int insertAction(JSONObject params) {
		// TODO Auto-generated method stub
		int result = -1;
		String actionId = UUIDUtil.getUUID();
		params.put("actionId", actionId);
		result = this.insert("insertAction", params);
		return result;
	}

	@Override
	public int updateAction(JSONObject params) {
		// TODO Auto-generated method stub
		return this.update("updateAction", params);
	}

	@Override
	public int deleteAction(JSONObject params) {
		// TODO Auto-generated method stub
		return this.delete("deleteAction", params);
	}

	@Override
	public List<JSONObject> selectStudents(JSONObject params) {
		// TODO Auto-generated method stub
		return this.selectList("selectStudents", params);
	}

	@Override
	public List<JSONObject> getActions(JSONObject params) {
		// TODO Auto-generated method stub
		return this.selectList("getActions", params);
	}

	
}
