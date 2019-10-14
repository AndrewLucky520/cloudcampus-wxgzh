package com.talkweb.document.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.document.dao.DocumentDao;

 

@Repository
public class DocumentDaoImpl extends MyBatisBaseDaoImpl  implements DocumentDao{

	@Override
	public List<JSONObject> getDocumentManager(JSONObject param) {
		 
		return selectList("getDocumentManager", param) ;
	}

	@Override
	public List<JSONObject> getDocumentList(JSONObject param) {
		 
		return selectList("getDocumentList", param) ;
	}

	@Override
	public int uploadDocument(JSONObject param) {
		 
		return insert("uploadDocument", param);
	}

	@Override
	public JSONObject getDocumentDetail(JSONObject param) {
 
		return selectOne("getDocumentDetail", param) ;
	}

	@Override
	public int uploadDocumentFile(JSONObject param) {
	 
		return insert("uploadDocumentFile", param);
	}

	@Override
	public int delDocumentFile(JSONObject param) {
		 
		return delete("delDocumentFile", param);
	}

 

	@Override
	public int delDocument(JSONObject param) {
	 
		  return delete("delDocument", param);
	}

	@Override
	public int addDocumentManager(List<JSONObject> list) {
 
		  	return insert("addDocumentManager", list); 
	}

	@Override
	public int delDocumentManager(JSONObject param) {
		 
		 return delete("delDocumentManager", param);
	}

	@Override
	public List<JSONObject> getDocumentTypeList(JSONObject param) {
		 
		return selectList("getDocumentTypeList" , param );
	}

	@Override
	public int addDocumentType(List<JSONObject> list) {
		 
		return insert("addDocumentType", list);
	}

	@Override
	public int delDocumentType(JSONObject param) {
		 
		 return delete("delDocumentType", param);
	}

	@Override
	public List<JSONObject> getDocumentFileList(JSONObject param) {
	 
		return selectList("getDocumentFileList" , param);
	}

	@Override
	public List<JSONObject> getReviewList(JSONObject param) {
		 
		return  selectList("getReviewList" , param);
	}

	@Override
	public int addDocumentReview(List<JSONObject> list) {
		 
		return insert("addDocumentReview", list);
	}

	@Override
	public int delDocumentReview(JSONObject param) {
		 
		 return delete("delDocumentReview", param);
	}

 
}
