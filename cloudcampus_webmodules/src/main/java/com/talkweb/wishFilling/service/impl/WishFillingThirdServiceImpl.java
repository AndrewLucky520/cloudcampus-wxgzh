package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.dao.WishFillingThirdDao;
import com.talkweb.wishFilling.service.WishFillingThirdService;
/** 
 * 志愿填报-对外接口SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @time 2016年11月23日  author：zhh
 */
@Service
public class WishFillingThirdServiceImpl implements WishFillingThirdService {
	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	@Autowired
	private WishFillingThirdDao wishFillingThirdDao;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingThirdServiceImpl.class);
	@Override
	public List<JSONObject> getWfListToThird(String gradeId, String type,Long schoolId) {
		if(StringUtils.isBlank(gradeId)|| StringUtils.isBlank(type)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("gradeId", gradeId);
		List<JSONObject> list = wishFillingThirdDao.getWfListToThird(json);
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String wfWay = obj.getString("wfWay");
			String isByElection = obj.getString("isByElection");
			obj.remove("wfWay");
			obj.remove("isByElection");
			if("2".equals(type)){ //全部
				returnList.add(obj);
			}else{
				if("0".equals(type)){ //单科
					if("0".equals(wfWay)&& "0".equals(isByElection)){
						returnList.add(obj);
					}
				}else if ("1".equals(type)){ //组合
					if("1".equals(wfWay) || ("0".equals(wfWay)&& "1".equals(isByElection))){
						returnList.add(obj);
					}
				}
			}
		}
		return returnList;
	}
	@Override
	public List<JSONObject> getZhStudentNumToThird(String wfId, String wfTermInfo,Long schoolId) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId", schoolId);
		json.put("termInfo", wfTermInfo);
		School school = allCommonDataService.getSchoolById(schoolId, wfTermInfo);
		json.put("areaCode", school.getAreaCode());
		JSONObject wf = wishFillingSetDao.getTb(json);
		String pycc =wf.getString("pycc");
		Map<String,List<JSONObject>> divideSubjectMap = new HashMap<String,List<JSONObject>>();
		if("3".equals(pycc)){
			json.put("pycc", pycc);
			List<JSONObject> dicSubs = wishFillingThirdDao.getDicSubjectListToThird(json);
			//ssubjectId-JSON
			if(dicSubs!=null){
				for(JSONObject obj:dicSubs){
					String isDivided = obj.getString("isDivided");
					if("0".equals(isDivided)){
						continue;
					}
					String ssubjectId = obj.getString("ssubjectId");
					List<JSONObject> list = new ArrayList<JSONObject>();
					if(!divideSubjectMap.containsKey(ssubjectId)){
						list = divideSubjectMap.get(ssubjectId);
					}
					list.add(obj);
					divideSubjectMap.put(ssubjectId, list);
				}
			}
		}
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		List<JSONObject> staticZhs = new ArrayList<JSONObject>();
		List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(json);
		Map<String,String> zhSubMap = new HashMap<String,String>();
		for(JSONObject zh:zhList){
			zhSubMap.put(zh.getString("zhId"), zh.getString("subjectIds"));
		}
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			json.put("zhWay", 1);
			staticZhs = wishFillingSetDao.getByStaticListByZh(json);
		}else{
			staticZhs = wishFillingSetDao.getStaticListByZh(json);
		}
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(JSONObject obj:staticZhs){
			String zhId = obj.getString("zhId");
			String zhName = obj.getString("zhName");
			String studentNum = obj.getString("studentNum");
			JSONObject returnObj = new JSONObject();
			returnObj.put("zhName", zhName);
			returnObj.put("studentNum", studentNum);
			if(StringUtils.isNotBlank(zhSubMap.get(zhId))){
				String subjectIds = zhSubMap.get(zhId);
				List<String> sIds = Arrays.asList(subjectIds.split(","));
				String trueSubjectIds = "";
				for(String id:sIds){
					if(divideSubjectMap.containsKey(id)){
						List<JSONObject> sList = divideSubjectMap.get(id);
						for(JSONObject sObj:sList){
							String subjectId = sObj.getString("subjectId");
							trueSubjectIds+=subjectId+",";
						}
					}else{
						trueSubjectIds+=id+",";
					}
				}
				trueSubjectIds=trueSubjectIds.substring(0, trueSubjectIds.length()-1);
				returnObj.put("subjectIds", trueSubjectIds);
			}else{
				returnObj.put("subjectIds", "");
			}
			returnList.add(returnObj);
		}
		return returnList;
	}
	@Override
	public List<JSONObject> getZhSubjectListToThird(String wfId, String wfTermInfo, Long schoolId) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("termInfo", wfTermInfo);
		json.put("schoolId", schoolId);
		School school = allCommonDataService.getSchoolById(schoolId, wfTermInfo);
		json.put("areaCode", school.getAreaCode());
		JSONObject wf = wishFillingSetDao.getTb(json);
		String pycc = wf.getString("pycc");
		Map<String,List<JSONObject>> divideSubjectMap = new HashMap<String,List<JSONObject>>();
		if("3".equals(pycc)){
			json.put("pycc", pycc);
			List<JSONObject> dicSubs = wishFillingThirdDao.getDicSubjectListToThird(json);
			//ssubjectId-JSON
			if(dicSubs!=null){
				for(JSONObject obj:dicSubs){
					String isDivided = obj.getString("isDivided");
					if("0".equals(isDivided)){
						continue;
					}
					String ssubjectId = obj.getString("ssubjectId");
					List<JSONObject> list = new ArrayList<JSONObject>();
					if(!divideSubjectMap.containsKey(ssubjectId)){
						list = divideSubjectMap.get(ssubjectId);
					}
					list.add(obj);
					divideSubjectMap.put(ssubjectId, list);
				}
			}
		}
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		List<JSONObject> list = wishFillingSetDao.getZhListByTb(json);
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String zhWay = obj.getString("zhWay");
			if("0".equals(wfWay)&&"1".equals(isByElection) &&"0".equals(zhWay)){
				continue;
			}
			returnList.add(obj);
		}
		if("3".equals(wf.getString("pycc"))){
			for(JSONObject obj:returnList){
				String subjectIds = obj.getString("subjectIds");
				List<String> sIds = Arrays.asList(subjectIds.split(","));
				String trueSubjectIds = "";
				for(String id:sIds){
					if(divideSubjectMap.containsKey(id)){
						List<JSONObject> sList = divideSubjectMap.get(id);
						for(JSONObject sObj:sList){
							String subjectId = sObj.getString("subjectId");
							trueSubjectIds+=subjectId+",";
						}
					}else{
						trueSubjectIds+=id+",";
					}
				}
				trueSubjectIds=trueSubjectIds.substring(0, trueSubjectIds.length()-1);
				obj.put("subjectIds", trueSubjectIds);
			}
		}
		return returnList;
	}
	@Override
	public List<JSONObject> getZhStudentListToThird(String wfId, String wfTermInfo, Long schoolId) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId", schoolId);
		json.put("termInfo", wfTermInfo);
		School school = allCommonDataService.getSchoolById(schoolId, wfTermInfo);
		json.put("areaCode", school.getAreaCode());
		JSONObject wf = wishFillingSetDao.getTb(json);
		String pycc = wf.getString("pycc");
		Map<String,List<JSONObject>> divideSubjectMap = new HashMap<String,List<JSONObject>>();
		if("3".equals(pycc)){
			json.put("pycc", pycc);
			List<JSONObject> dicSubs = wishFillingThirdDao.getDicSubjectListToThird(json);
			//ssubjectId-JSON
			if(dicSubs!=null){
				for(JSONObject obj:dicSubs){
					String isDivided = obj.getString("isDivided");
					if("0".equals(isDivided)){
						continue;
					}
					String ssubjectId = obj.getString("ssubjectId");
					List<JSONObject> list = new ArrayList<JSONObject>();
					if(!divideSubjectMap.containsKey(ssubjectId)){
						list = divideSubjectMap.get(ssubjectId);
					}
					list.add(obj);
					divideSubjectMap.put(ssubjectId, list);
				}
			}
		}
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		List<JSONObject> zhStudentList = new ArrayList<JSONObject>();
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			json.put("zhWay", "1");
			zhStudentList = wishFillingThirdDao.getByZhStudentToThird(json);
		}else{
			zhStudentList = wishFillingThirdDao.getZhStudentToThird(json);
		}
		if("3".equals(wf.getString("pycc"))){
			for(JSONObject obj:zhStudentList){
				String subjectIds = obj.getString("subjectIds");
				List<String> sIds = Arrays.asList(subjectIds.split(","));
				String trueSubjectIds = "";
				for(String id:sIds){
					if(divideSubjectMap.containsKey(id)){
						List<JSONObject> sList = divideSubjectMap.get(id);
						for(JSONObject sObj:sList){
							String subjectId = sObj.getString("subjectId");
							trueSubjectIds+=subjectId+",";
						}
					}else{
						trueSubjectIds+=id+",";
					}
				}
				trueSubjectIds=trueSubjectIds.substring(0, trueSubjectIds.length()-1);
				obj.put("subjectIds", trueSubjectIds);
			}
		}
		return zhStudentList;
	}
	@Override
	public List<JSONObject> getSubjectStudentListToThird(String wfId, String wfTermInfo, Long schoolId) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId", schoolId);
		json.put("termInfo", wfTermInfo);
		School school = allCommonDataService.getSchoolById(schoolId, wfTermInfo);
		json.put("areaCode", school.getAreaCode());
		JSONObject wf = wishFillingSetDao.getTb(json);
		String pycc = wf.getString("pycc");
		json.put("pycc", pycc);
		Map<String,List<JSONObject>> divideSubjectMap = new HashMap<String,List<JSONObject>>();
		if("3".equals(pycc)){
			List<JSONObject> dicSubs = wishFillingThirdDao.getDicSubjectListToThird(json);
			//ssubjectId-JSON
			if(dicSubs!=null){
				for(JSONObject obj:dicSubs){
					String ssubjectId = obj.getString("ssubjectId");
					List<JSONObject> list = new ArrayList<JSONObject>();
					if(!divideSubjectMap.containsKey(ssubjectId)){
						list = divideSubjectMap.get(ssubjectId);
					}
					list.add(obj);
					divideSubjectMap.put(ssubjectId, list);
				}
			}
		}
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		List<JSONObject> subjectStudentList = new ArrayList<JSONObject>();
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			json.put("subjectIsBy", "1");
			json.put("isFixedZh", "0");
			subjectStudentList = wishFillingThirdDao.getBySubjectStudentToThird(json);
		}else{
			subjectStudentList = wishFillingThirdDao.getSubjectStudentToThird(json);
		}
		if("3".equals(pycc)){
			for(JSONObject obj:subjectStudentList){
				String isDivided = obj.getString("isDivided");
				if("0".equals(isDivided)){
					continue;
				}
				String ssubjectId = obj.getString("ssubjectId");
				if(divideSubjectMap.containsKey(ssubjectId)){
					List<JSONObject> sList = divideSubjectMap.get(ssubjectId);
					for(JSONObject sObj:sList){
						JSONObject sObjTrue= (JSONObject) obj.clone();
						sObjTrue.put("subjectId", sObj.getString("subjectId"));
						subjectStudentList.add(sObjTrue);
					}
					subjectStudentList.remove(obj);
				}
			}
		}
		return subjectStudentList;
	}
	@Override
	public List<JSONObject> getSubjectNumToThird(String wfId, String wfTermInfo, Long schoolId) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId == null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId", schoolId);
		json.put("termInfo", wfTermInfo);
		School school = allCommonDataService.getSchoolById(schoolId, wfTermInfo);
		json.put("areaCode", school.getAreaCode());
		JSONObject wf = wishFillingSetDao.getTb(json);
		String pycc = wf.getString("pycc");
		json.put("pycc", pycc);
		Map<String,List<JSONObject>> divideSubjectMap = new HashMap<String,List<JSONObject>>();
		if("3".equals(pycc)){
			List<JSONObject> dicSubs = wishFillingThirdDao.getDicSubjectListToThird(json);
			//ssubjectId-JSON
			if(dicSubs!=null){
				for(JSONObject obj:dicSubs){
					String ssubjectId = obj.getString("ssubjectId");
					List<JSONObject> list = new ArrayList<JSONObject>();
					if(!divideSubjectMap.containsKey(ssubjectId)){
						list = divideSubjectMap.get(ssubjectId);
					}
					list.add(obj);
					divideSubjectMap.put(ssubjectId, list);
				}
			}
		}
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		List<JSONObject> subjectNumList = new ArrayList<JSONObject>();
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			json.put("subjectIsBy", "1");
			json.put("isFixedZh", "0");
			subjectNumList = wishFillingThirdDao.getBySubjectNumToThird(json);
		}else{
			subjectNumList = wishFillingThirdDao.getSubjectNumToThird(json);
		}
		if("3".equals(pycc)){
			for(JSONObject obj:subjectNumList){
				String isDivided = obj.getString("isDivided");
				if("0".equals(isDivided)){
					continue;
				}
				String ssubjectId = obj.getString("ssubjectId");
				if(divideSubjectMap.containsKey(ssubjectId)){
					List<JSONObject> sList = divideSubjectMap.get(ssubjectId);
					for(JSONObject sObj:sList){
						JSONObject sObjTrue= (JSONObject) obj.clone();
						sObjTrue.put("subjectId", sObj.getString("subjectId"));
						subjectNumList.add(sObjTrue);
					}
					subjectNumList.remove(obj);
				}
			}
		}
		return subjectNumList;
	}
	@Override
	public List<JSONObject> getSubjectListToThird(String wfId, String wfTermInfo, Long schoolId,String areaCode) {
		if(StringUtils.isBlank(wfId)|| StringUtils.isBlank(wfTermInfo)||schoolId==null){
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("wfId", wfId);
		json.put("schoolId", schoolId);
		json.put("termInfo", wfTermInfo);
		json.put("areaCode", areaCode);
		
		JSONObject wf = wishFillingSetDao.getTb(json);
		String wfWay = wf.getString("wfWay");
		String isByElection = wf.getString("isByElection");
		String subjectIds = wf.getString("subjectIds");
		if("0".equals(wfWay)&& "1".equals(isByElection)){
			json.put("isGetBySubject", "1");
			JSONObject wf1 = wishFillingSetDao.getTb(json);
			subjectIds = wf1.getString("subjectIds");
		}
		List<Long> subList = StringUtil.toListFromString(subjectIds);
		List<JSONObject> lList = wishFillingSetDao.getDicSubjectList(schoolId+"",areaCode,wf.getString("pycc"),"1");
		Map<Long,String> idNameMap = new HashMap<Long,String>();
		if(lList!=null){
			for(JSONObject l : lList){
				Long subjectId = l.getLong("subjectId");
				String subjectName = l.getString("subjectName");
				idNameMap.put(subjectId, subjectName);
			}
		}
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(Long id:subList){
			JSONObject returnObj = new JSONObject();
			returnObj.put("subjectId", id);
			if(StringUtils.isNotBlank(idNameMap.get(id))){
				returnObj.put("subjectName", idNameMap.get(id));
			}else{
				returnObj.put("subjectName", "[已删除]");
			}
			returnList.add(returnObj);
		}
		return returnList;
	}

}
