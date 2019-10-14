package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingCommonService;

/** 
 * 志愿填报-公共SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Service
public class WishFillingCommonServiceImpl implements WishFillingCommonService {

	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingCommonServiceImpl.class);
	/**
	 * 获取初/高中年级列表
	 * @param JSONObject
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getSubjectListByTb(JSONObject param)
			throws Exception {
		String pycc = param.getString("pycc");
		long schoolId = param.getLongValue("schoolId");
		String termInfo = param.getString("termInfo");
		String isAll = param.getString("isAll");
		String isRelatedTeacher = param.getString("isRelatedTeacher");
		List<Long> courseLesson = (List<Long>) param.get("courseLesson");
		//从subjecttb表中获取，数据从创建选课轮次时从字典表中插入该表中（第二版）
		List<JSONObject> subjectList = wishFillingSetDao.getDicSubjectList(schoolId+"",param.getString("areaCode"),pycc,"0");//wishFillingSetDao.getSubjectListByTb(param);
		List<JSONObject> subjectDividedList =wishFillingSetDao.getDividedSubjectList(param);
		List<JSONObject> returnList= new ArrayList<JSONObject>();
		List<Long> allIds = new ArrayList<Long>();
		Map<Long,List<JSONObject>> subMap = new HashMap<Long,List<JSONObject>>();
		for(JSONObject sub:subjectDividedList){
			Long ssubjectId = sub.getLong("ssubjectId");
			List<JSONObject> sList = new ArrayList<JSONObject>();
			if(subMap.containsKey(ssubjectId)){
				sList=subMap.get(ssubjectId);
			}
			sList.add(sub);
			subMap.put(ssubjectId, sList);
		}
		if(subjectList!=null){
			for(JSONObject l:subjectList){
				String subjectName = l.getString("subjectName");
				Long subjectId = l.getLong("subjectId");
				String isDivided = l.getString("isDivided");
				List<JSONObject> sList = subMap.get(subjectId);
				//关联任教老师，但该科目不在该老师的任教关系科目中 -合并科目需要单独分开后判断
				if("1".equals(isRelatedTeacher) && "1".equals(isDivided) && sList!=null){
					boolean isContinue = true;
					for(JSONObject s:sList){
						Long sId=s.getLong("subjectId");
						if(courseLesson.contains(sId)){
							isContinue = false;
						}
					}
					if(isContinue){
						continue;
					}
				}else if("1".equals(isRelatedTeacher) &&  !courseLesson.contains(subjectId)){//关联任教老师，但该科目不在该老师的任教关系科目中
					continue;
				}
				JSONObject returnObj = new JSONObject();
				returnObj.put("subjectName",subjectName);
				returnObj.put("subjectId", subjectId);
				allIds.add(subjectId);
				returnList.add(returnObj);
			}
		}
		if("1".equals(isAll)){
			JSONObject returnObj = new JSONObject();
			returnObj.put("subjectName","全部");
			returnObj.put("subjectId", allIds);
			returnList.add(0,returnObj);
		}
		return returnList;
	}
	@Override
	public int hasTbByUseGrades(JSONObject param) throws Exception {
		return wishFillingSetDao.hasTbByUseGrades(param);
	}
	@Override
	public List<JSONObject> getZhListByTb(JSONObject param) throws Exception {
		return wishFillingSetDao.getZhListByTb(param);
	}
	
	
}
