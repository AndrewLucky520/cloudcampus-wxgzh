package com.talkweb.archive.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.archive.service.ArchiveManagementService;
import com.talkweb.commondata.service.AllCommonDataService;

@Service
public class ArchiveManagementServiceImpl implements ArchiveManagementService{

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Override
	public List<JSONObject> getAllTeacherList(JSONObject param) {
 
		List<JSONObject> rList = new ArrayList<JSONObject>();
		
		Long schoolId = param.getLong("schoolId");
		String termInfoId = param.getString("selectedSemester");
		School school = commonDataService.getSchoolById(schoolId, termInfoId);
		String teacherName = param.getString("teacherName");
		if(teacherName == null) {
			teacherName = "";
		}
		List<Account> tList = commonDataService.getAllSchoolEmployees(school, termInfoId, teacherName.trim());
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
 
	
}
