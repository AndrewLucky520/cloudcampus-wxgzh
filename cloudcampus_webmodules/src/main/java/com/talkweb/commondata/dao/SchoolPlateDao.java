package com.talkweb.commondata.dao;


import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName SchoolPlateDao
 * @author zhh
 * @version 1.0
 * @Description 基础数据-学校平台关联表Dao
 * @date 2017年2月13日
 */

public interface SchoolPlateDao {
   List<JSONObject> getSchoolPlateListBy(JSONObject param);
   JSONObject getSchoolPlateBySchoolId(JSONObject param);
   void addSchoolPlate(JSONObject param);
   void deleteSchoolPlate(JSONObject param);
   void updateSchoolPlate(JSONObject param);
}
