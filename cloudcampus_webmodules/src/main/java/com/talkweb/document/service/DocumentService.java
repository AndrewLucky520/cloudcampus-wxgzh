package com.talkweb.document.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface DocumentService {

	List<JSONObject> getDocumentManager(JSONObject param);
	List<JSONObject> getDocumentList(JSONObject param);
	int uploadDocument(JSONObject param);
	int delDocument(JSONObject param);
	JSONObject getDocumentDetail(JSONObject param);
	
	List<JSONObject> getDocumentFileList(JSONObject param);
	int uploadDocumentFile(JSONObject param);
	int delDocumentFile(JSONObject param);
	 
	
	int addDocumentManager(List<JSONObject> list);
	int delDocumentManager(JSONObject param);
	
	List<JSONObject> getDocumentTypeList(JSONObject param);
	int addDocumentType(List<JSONObject> list);
	int delDocumentType(JSONObject param);
	
	
	List<JSONObject> getReviewList(JSONObject param);
	int addDocumentReview(List<JSONObject> list);
	int delDocumentReview(JSONObject param);
	  
	
}
