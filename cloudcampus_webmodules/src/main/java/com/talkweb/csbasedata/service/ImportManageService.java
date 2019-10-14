package com.talkweb.csbasedata.service;

import com.alibaba.fastjson.JSONObject;

public interface ImportManageService {
	
	public JSONObject uploadExcel(JSONObject param) throws Exception;
	
	/*public JSONObject getExcelHead(JSONObject param);
	
	public JSONObject startImportTask(JSONObject param);
	
	public JSONObject importProgress(JSONObject param);
	
	public JSONObject singleDataCheck(JSONObject param);
	
	public JSONObject continueImport(JSONObject param);
*/
	public JSONObject getExcelMatch(JSONObject param)throws Exception;

	public JSONObject continueImport(JSONObject param)throws Exception;

	public JSONObject singleDataCheck(JSONObject param)throws Exception;

	public JSONObject importProgress(JSONObject param)throws Exception;

	public JSONObject startImportTask(JSONObject object)throws Exception;

}