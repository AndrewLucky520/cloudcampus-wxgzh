package com.talkweb.exammanagement.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.dao.ExamManagementSetDao;
import com.talkweb.exammanagement.domain.ArrangeExamPlaceInfo;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.service.ExamManagementExamPlaceService;

@Service
public class ExamManagementExamPlaceServiceImpl implements
		ExamManagementExamPlaceService {
	Logger logger = LoggerFactory
			.getLogger(ExamManagementExamPlaceServiceImpl.class);

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private ExamManagementDao examManagementDao;

	@Autowired
	private ExamManagementSetDao examManagementSetDao;
	
	
	static ResourceBundle rbConstant = ResourceBundle.getBundle("constant.constant" );
	public static  String FIRST_TERMINFOID =  rbConstant.getString("exammanagementFirstTermInfoId");

	@Override
	public List<JSONObject> getExamPlaceList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);

		return examManagementSetDao.getExamPlaceList(param);
	}

	@Override
	public List<JSONObject> getHasOldExamPlaceList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		String termInfo = param.get("termInfo").toString();
		List<JSONObject> alllist = new ArrayList<JSONObject>();
		int count = 0;
		do {
			param.put("termInfo", termInfo);
			List<ExamManagement> emList = examManagementDao
					.getExamManagementList(param, termInfo);
			for (ExamManagement em : emList) {
				Integer autoIncr = em.getAutoIncr();
				param.put("autoIncr", autoIncr);
				param.put("examManagementId", em.getExamManagementId());
				param.put("limit", 10 - count);
				List<JSONObject> datalist = examManagementSetDao
						.getHasOldExamPlaceList(param);

				alllist.addAll(datalist);
				if (CollectionUtils.isNotEmpty(alllist) && alllist.size() >= 10) {
					return alllist;
				}
				count = CollectionUtils.isNotEmpty(datalist) ? datalist.size()
						: 0;
			}
			if (FIRST_TERMINFOID.equals(termInfo)) {
				break;
			}
			termInfo = TermInfoIdUtils.decreaseTermInfo(termInfo);

		} while (true);
		// 删除本次考务
		for (Iterator it = alllist.iterator(); it.hasNext();) {
			JSONObject dd = (JSONObject) it.next();
			if (dd.getString("examManagementId").equals(
					param.get("curExamManagementId").toString())) {
				it.remove();
			}
		}
		return alllist;
	}

	@Override
	public int hasOldExamPlaceList(Map<String, Object> param) {
		String termInfo = param.get("termInfo").toString();
		param.put("limit", 1);
		//List<JSONObject> datalist = new ArrayList<JSONObject>();
		String curexamManagementId =  param.get("examManagementId").toString();
		param.put("curexamManagementId", curexamManagementId);
		
		//当前考试的考场列表
		ExamManagement em1 = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em1 == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr1 = em1.getAutoIncr();
		param.put("autoIncr", autoIncr1);

		
		/*
		List<JSONObject> placelist=examManagementSetDao.getExamPlaceList(param);
		
		 do {
			param.put("termInfo", termInfo);
			List<ExamManagement> emList = examManagementDao
					.getExamManagementList(param, termInfo);
			for(JSONObject pj:placelist){
				param.put("examPlaceId", pj.getString("examPlaceId"));
				List<ArrangeExamPlaceInfo> d=examManagementDao.getArrangeExamPlaceInfo(param,param.get("termInfo").toString(),autoIncr1);
				if(d!=null&&d.size()>0){
					return 2;
				}
			}
			
			for (ExamManagement em : emList) {
				Integer autoIncr = em.getAutoIncr();
				param.put("autoIncr", autoIncr);
				param.put("examManagementId", em.getExamManagementId());
				datalist = examManagementSetDao.getHasOldExamPlaceList(param);
				// 删除本次考务
				for (Iterator it = datalist.iterator(); it.hasNext();) {
					JSONObject dd = (JSONObject) it.next();
					if (dd.getString("examManagementId").equals(
							param.get("curexamManagementId").toString())) {
						it.remove();
					}
				}
				if (CollectionUtils.isNotEmpty(datalist)) {
					return 1;
				}
			}
			 if (FIRST_TERMINFOID.equals(termInfo)) {
				break;
			} 
			 termInfo = TermInfoIdUtils.decreaseTermInfo(termInfo);
		  } while (false);*/
		

		// TODO Auto-generated method stub
	 
		List<JSONObject> alllist = new ArrayList<JSONObject>();
		int count = 0;
		do {
			param.put("termInfo", termInfo);
			List<ExamManagement> emList = examManagementDao
					.getExamManagementList(param, termInfo);
			for (ExamManagement em : emList) {
				Integer autoIncr = em.getAutoIncr();
				param.put("autoIncr", autoIncr);
				param.put("examManagementId", em.getExamManagementId());
				param.put("limit", 10 - count);
				List<JSONObject> datalist = examManagementSetDao
						.getHasOldExamPlaceList(param);

				alllist.addAll(datalist);
				if (CollectionUtils.isNotEmpty(alllist) && alllist.size() >= 10) {
					return 1;
				}
				count = CollectionUtils.isNotEmpty(datalist) ? datalist.size()
						: 0;
			}
			if (FIRST_TERMINFOID.equals(termInfo)) {
				break;
			}
			termInfo = TermInfoIdUtils.decreaseTermInfo(termInfo);

		} while (true);
		// 删除本次考务
		for (Iterator it = alllist.iterator(); it.hasNext();) {
			JSONObject dd = (JSONObject) it.next();
			if (dd.getString("examManagementId").equals(param.get("curexamManagementId").toString())) {
				it.remove();
			}
		}
		if (alllist.size() > 0) {
			return 1;
		}
 
		
		return 0;

	}

	@Override
	public void deleteExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		if(param.get("isQueryOrDelete").toString().equals("0")){
			List<ArrangeExamPlaceInfo> d=examManagementDao.getArrangeExamPlaceInfo(param,param.get("termInfo").toString(),autoIncr);
			if(d!=null&&d.size()>0){
				throw new CommonRunException(-2, "已经做了安排的考场不允许删除！");
			}
		}else{
			examManagementSetDao.deleteExamPlace(param);
		}
		
	}

	@Override
	public List<JSONObject> getExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		return examManagementSetDao.getExamPlace(param);
	}

	@Override
	public void saveExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<JSONObject> datalist = examManagementSetDao
				.getExamPlaceList(param);
		HashMap<String, JSONObject> examPlaceCodeMap = new HashMap<String, JSONObject>();
		HashMap<String, JSONObject> examPlaceNameMap = new HashMap<String, JSONObject>();
		for (JSONObject data : datalist) {
			examPlaceCodeMap.put(data.getString("examPlaceCode"), data);
			examPlaceNameMap.put(data.getString("examPlaceName"), data);
		}
		List<JSONObject> data = (List<JSONObject>) param.get("list");
		for (JSONObject da : data) {
			if (examPlaceCodeMap
					.containsKey(da.get("examPlaceCode").toString())) {
				throw new CommonRunException(0, "考场编号重复，请重新填写");
			}
			if (examPlaceNameMap
					.containsKey(da.get("examPlaceName").toString())) {
				throw new CommonRunException(0, "考场名称重复，请重新填写");
			}
		}

		if (CollectionUtils.isNotEmpty(data)
				&& data.get(0).getString("isup").equals("0")) {
			Map<String, Object> da = data.get(0);
			da.put("autoIncr", autoIncr);
			examManagementSetDao.updateExamPlace(da);
		} else if (CollectionUtils.isNotEmpty(data)
				&& data.get(0).getString("isup").equals("1")) {
			em.setStatus(2);
			examManagementDao.updateExamManagementStatus(em,
					param.get("termInfo").toString());
			examManagementSetDao.saveExamPlace(param);
		}
		//修改非新增
		List<JSONObject> coverlist = (List<JSONObject>) param.get("coverlist");
		if(coverlist!=null){
		for(JSONObject cover:coverlist){
			cover.put("autoIncr", autoIncr);
			examManagementSetDao.updateExamPlace(cover);
		}
		}
	}

	@Override
	public void saveCopyExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub

		Map<String, Object> pra = new HashMap<String, Object>();
		pra.put("termInfo", param.get("copyTermInfo"));
		pra.put("examManagementId", param.get("copyExamManagementId"));
		pra.put("schoolId", param.get("schoolId"));
		ExamManagement em = examManagementDao.getExamManagementListById(pra,
				pra.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		List<JSONObject> oldlist = examManagementSetDao.getOldExamPlace(param);
		for (JSONObject data : oldlist) {
			data.put("examManagementId", param.get("examManagementId"));
			data.put("termInfo", param.get("termInfo"));
			data.put("floor", data.get("buildingFloor"));
			data.put("schoolId", param.get("schoolId"));
			data.put("examPlaceId", UUIDUtil.getUUID());
		}
		param.put("list", oldlist);
		em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		examManagementSetDao.deleteExamPlace(param);
		examManagementSetDao.saveExamPlace(param);
	}

	@Override
	public JSONObject getExamPlaceListMap(HashMap param) {
		// TODO Auto-generated method stub
		ExamManagement em = examManagementDao.getExamManagementListById(param,
				param.get("termInfo").toString());
		if (em == null) {
			throw new CommonRunException(-1, "没有查询到相应的考试信息，请刷新页面！");
		}
		Integer autoIncr = em.getAutoIncr();
		param.put("autoIncr", autoIncr);
		JSONObject re = new JSONObject();
		List<JSONObject> datalist = examManagementSetDao
				.getExamPlaceList(param);
		HashMap<String, JSONObject> examPlaceCodeMap = new HashMap<String, JSONObject>();
		HashMap<String, JSONObject> examPlaceNameMap = new HashMap<String, JSONObject>();
		for (JSONObject data : datalist) {
			examPlaceCodeMap.put(data.getString("examPlaceCode"), data);
			examPlaceNameMap.put(data.getString("examPlaceName"), data);
		}
		re.put("examPlaceCodeMap", examPlaceCodeMap);
		re.put("examPlaceNameMap", examPlaceNameMap);
		return re;
	}

}
