package com.talkweb.csbasedata.service;

import com.alibaba.fastjson.JSONObject;


public interface OrgImportManageService {

	JSONObject uploadExcel(JSONObject param) throws Exception;

	JSONObject getExcelMatch(JSONObject param)throws Exception;

	JSONObject importProgress(JSONObject param)throws Exception;

	JSONObject singleDataCheck(JSONObject param)throws Exception;

	JSONObject continueImport(JSONObject param)throws Exception;

	JSONObject startImportTask(JSONObject object)throws Exception;
}
