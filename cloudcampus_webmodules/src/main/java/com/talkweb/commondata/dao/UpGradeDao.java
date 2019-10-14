package com.talkweb.commondata.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName UpGradeDao
 * @author Homer
 * @version 1.0
 * @Description 升级学年学期Dao
 * @date 2017年8月8日
 * @author zhanghuihui
 */
public interface UpGradeDao {

	List<JSONObject> getTermInfosBySchoolId(JSONObject json);

	List<JSONObject> getTermInfos(JSONObject json);

	void deGradeBySchoolId(JSONObject json);

	void upGradeBySchoolId(JSONObject param);

	void deGradeAllSchools(JSONObject json);

	void updateGradeAllSchools(JSONObject param);
	    
}
