package com.talkweb.community.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.community.dao.CommunityDao;
import com.talkweb.community.service.CommunityService;

@Service
public class CommunityServiceImpl implements CommunityService {
	@Autowired
	private CommunityDao communityDao;

	@Override
	public List<JSONObject> getCommunitys(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.getCommunities(params);
	}

	@Override
	public int insertCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.insertCommunity(params);
	}

	@Override
	public int updateCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		 return communityDao.updateCommunity(params);
	}

	@Override
	public int deleteCommunity(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.deleteCommunity(params);
	}

	@Override
	public List<JSONObject> getActions(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.getActions(params);
	}

	@Override
	public int insertAction(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.insertAction(params);
	}

	@Override
	public int updateAction(JSONObject params) {
		// TODO Auto-generated method stub
		return  communityDao.updateAction(params);
	}

	@Override
	public int deleteAction(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.deleteAction(params);
	}

	@Override
	public List<JSONObject> selectStudents(JSONObject params) {
		// TODO Auto-generated method stub
		return communityDao.selectStudents(params);
	}

	

}
