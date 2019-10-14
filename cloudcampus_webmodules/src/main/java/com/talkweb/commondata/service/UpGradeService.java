package com.talkweb.commondata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName UpGradeService
 * @author zhanghuihui
 * @version 1.0
 * @Description 升级学年学期
 * @date 2017年8月8日
 */
public interface UpGradeService {

	void upGradeAllSchools(JSONObject json)throws Exception;

	void deGradeAllSchools(JSONObject json)throws Exception;

	void upGradeBySchoolId(JSONObject json)throws Exception;

	void deGradeBySchoolId(JSONObject json)throws Exception;

	List<JSONObject> getTermInfos(JSONObject json)throws Exception;

	List<JSONObject> getTermInfosBySchoolId(JSONObject json)throws Exception;;
}

