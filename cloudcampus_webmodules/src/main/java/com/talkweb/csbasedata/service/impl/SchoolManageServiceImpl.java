package com.talkweb.csbasedata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_SchoolType;
import com.talkweb.csbasedata.dao.SchoolManageDao;
import com.talkweb.csbasedata.service.SchoolManageService;

@Service("schoolManageService")
public class SchoolManageServiceImpl implements SchoolManageService {

	@Autowired
	SchoolManageDao schoolManageDao;
	
	/**
	 * 获取学校信息列表
	 * 
	 * @param schoolId
	 *          学校代码        
	 *            
	 * @return 学校信息
	*/	
	@Override
	public JSONObject getSchoolInfo(String school_id) {
		long schoolId = Long.parseLong(school_id);
		JSONObject school = schoolManageDao.getSchoolInfo(schoolId);
		if (null != school)
		{
			String stage = school.getString("stages");
			if (StringUtils.isNotEmpty(stage))
			{
				String[] stages = stage.split(",");
				JSONArray stageList = new JSONArray();
				for(int i = 0 ;i < stages.length;i++)
				{
					JSONObject s = new JSONObject();
					String[] array = stages[i].split("%");
					s.put("stage", array[0]);
					s.put("stageName", array[1]);
					stageList.add(s);
				}
				if (stageList.size() > 0){
					school.put("teachingStages", stageList);
				}    
			}
			int type = school.getIntValue("schoolType");
			String schoolType = AccountStructConstants.SchoolTypeNames
					.get(T_SchoolType.findByValue(type));
			JSONArray typeList = new JSONArray();
			JSONObject t = new JSONObject();
			t.put("type", type);
			t.put("typeName", schoolType);
			typeList.add(t);
			school.put("schoolTypes", typeList);
		}	
		return school;
	}

	/**
	 * 更新学校详细信息
	 * 
	 * @param JSONObject
	 *          学校详情        
	 *            
	 * @return 是否成功
	*/	
	@Override 
	public void updateSchoolInfo(JSONObject param) {
		String schoolId = param.getString("schoolId");
		int count = schoolManageDao.updateSchoolInfo(param);
		if (count > 0){
			schoolManageDao.deleteSchoolStage(schoolId);		
			List<JSONObject> list = new ArrayList<JSONObject>();
			String stage = param.getString("teachingStage");
			String[] stages = stage.split(",");
			for(int i = 0 ;i < stages.length;i++)
			{
				JSONObject s = new JSONObject();
				s.put("schoolId", schoolId);
				s.put("stage", stages[i]);
				list.add(s);
			}
			if (list.size() > 0){
				schoolManageDao.insertSchoolStage(list);
			}
		}
	}
}