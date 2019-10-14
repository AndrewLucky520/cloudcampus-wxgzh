package com.talkweb.document.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.document.dao.DocumentDao;
import com.talkweb.document.service.DocumentService;

 
@Service
public class DocumentServiceImpl implements DocumentService{

	
	@Autowired
	private DocumentDao documentDao;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	
	@Override
	public List<JSONObject> getDocumentManager(JSONObject param) {
		return documentDao.getDocumentManager(param);
	}

	@Override
	public List<JSONObject> getDocumentList(JSONObject param) {
		 
		 List<JSONObject> list = documentDao.getDocumentList(param);
		 String termInfoId = param.getString("termInfoId");
		 Long schoolId = param.getLong("schoolId");
		 Set<Long> ids = new HashSet<Long>();
		 for (int i = 0; i < list.size(); i++) {
			 JSONObject object = list.get(i);
			 ids.add(object.getLong("teacherId"));
		}
		 Map<Long, String> id2Name = new HashMap<Long, String>();
		 List<Account> accList = commonDataService.getAccountBatch(schoolId, new ArrayList<Long>(ids), termInfoId);
		 if(CollectionUtils.isNotEmpty(accList)) {
				for(Account acc : accList) {
					id2Name.put(acc.getId(), acc.getName());
				}
		 }
		 for (int i = 0; i < list.size(); i++) {
			 JSONObject object = list.get(i);
			 object.put("teacherName", id2Name.get(object.getLong("teacherId")));
		}
		 
		
		return list;
	}

	@Override
	public int uploadDocument(JSONObject param) {
		JSONArray array = param.getJSONArray("files");
		String documentId = param.getString("documentId");
		String schoolId = param.getString("schoolId");
		if (array!= null && array.size() > 0 ) {
			for (int i = 0; i < array.size(); i++) {
				JSONObject object = array.getJSONObject(i);
				object.put("documentId", documentId);
				object.put("schoolId", schoolId);
				documentDao.uploadDocumentFile(object);
			}
		}
		return documentDao.uploadDocument(param);
	}

	@Override
	public JSONObject getDocumentDetail(JSONObject param) {
		 
		return documentDao.getDocumentDetail(param);
	}

	@Override
	public int uploadDocumentFile(JSONObject param) {
		 
		return documentDao.uploadDocumentFile(param);
	}

	@Override
	public int delDocumentFile(JSONObject param) {
		 
		return documentDao.delDocumentFile(param);
	}

	@Override
	public int delDocument(JSONObject param) {
		 
		return documentDao.delDocument(param);
	}

	@Override
	public int addDocumentManager(List<JSONObject> list) {
	 
		return documentDao.addDocumentManager(list);
	}

	@Override
	public int delDocumentManager(JSONObject param) {
		 
		return documentDao.delDocumentManager(param);
	}

	@Override
	public List<JSONObject> getDocumentTypeList(JSONObject param) {
		 
		return documentDao.getDocumentTypeList(param);
	}

	@Override
	public int addDocumentType(List<JSONObject> list) {
		 
		return documentDao.addDocumentType(list);
	}

	@Override
	public int delDocumentType(JSONObject param) {
		 
		return documentDao.delDocumentType(param);
	}

	@Override
	public List<JSONObject> getDocumentFileList(JSONObject param) {
		 
		return documentDao.getDocumentFileList(param);
	}

	@Override
	public List<JSONObject> getReviewList(JSONObject param) {
		 
		return documentDao.getReviewList(param);
	}

	@Override
	public int addDocumentReview(List<JSONObject> list) {
		// TODO Auto-generated method stub
		return documentDao.addDocumentReview(list);
	}

	@Override
	public int delDocumentReview(JSONObject param) {
		return documentDao.delDocumentReview(param);
	}
 

}
