package com.talkweb.cas.service.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.cas.service.CASUserLoginService;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;

@Service(value="casLoginService")
public class CASUserLoginServiceImpl implements CASUserLoginService {
	
	@Autowired
	private SchoolPlateService schoolPlateService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;

    @Autowired
    private AllCommonDataService commonDataService;
	
	@Override
	public User getUserById(long schoolId, long userId) {
		return commonDataService.getUserById(schoolId,userId);
	}

	@Override
	public String getCurrentXnxq(School school) {
		return commonDataService.getCurrentXnxq(school);
	}

	@Override
	public School getSchoolByUserId(long schoolId, long userId) {
		return commonDataService.getSchoolByUserId(schoolId,userId);
	}

	@Override
	public List<JSONObject> getUserIdByExtId(String extId, String termInfoId) {
		return commonDataService.getUserIdByExtId(extId, termInfoId);
	}

	@Override
	public List<JSONObject> getUserIdByConditon(JSONObject condition) {
		return commonDataService.getUserIdByConditon(condition);
	}

	@Override
	public void setRedisSchoolPlateKey() {		
		JSONObject param = new JSONObject();
 	    List<JSONObject> schoolList = schoolPlateService.getSchoolPlateListBy(param);
 		Map<String,String> paramMap = new HashMap<String,String>();
 	    for(JSONObject schoolObj : schoolList){
 			String schoolId = schoolObj.getString("schoolId");
 			String schoolPlateKey = "common."+schoolId+".00.schoolPlate";
 			paramMap.put(schoolPlateKey, "1");
 	    }
 	    try {
 			redisOperationDAO.multiSet(paramMap);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
	}
	
}