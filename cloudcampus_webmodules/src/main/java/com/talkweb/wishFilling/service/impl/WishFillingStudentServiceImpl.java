package com.talkweb.wishFilling.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.placementtask.service.PlacementTaskService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingStudentService;

/** 
 * 志愿填报-学生填报serviceImpl
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月1日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Service
public class WishFillingStudentServiceImpl implements WishFillingStudentService {

	@Autowired
	private WishFillingSetDao wishFillingSetDao;
	@Autowired
	private PlacementTaskService placementTaskService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(WishFillingStudentServiceImpl.class);
	@Override
	public JSONObject getStudentTb(JSONObject param) throws Exception {
		JSONObject returnObj = new JSONObject();
		JSONObject wf = wishFillingSetDao.getTb(param);
		logger.info("wf:getStudentTb "+wf.toJSONString()+" param:"+param.toJSONString());
		String isByElection = wf.getString("isByElection");
		String wfWay = wf.getString("wfWay");
		String wfName = wf.getString("wfName");
		String wfId = param.getString("wfId");
		String wfStartTime= wf.getString("wfStartTime");
		String wfEndTime= wf.getString("wfEndTime");
		String byStartTime= wf.getString("byStartTime");
		String byEndTime= wf.getString("byEndTime");
		String wfNum= wf.getString("wfNum");
		String subjectIds = wf.getString("subjectIds");
		List<String> subList = Arrays.asList(subjectIds.split(","));
		 List<JSONObject> lList = wishFillingSetDao.getDicSubjectList(wf.getString("schoolId"),param.getString("areaCode"),wf.getString("pycc"),"0");
		 Map<String,String> subNameMap = new HashMap<String,String>();//id-name
		 Map<String,String> subRuleMap = new HashMap<String,String>();//id-selectRule
		 if(lList!=null){
			 for(JSONObject lObj:lList){
				 subRuleMap.put(lObj.getString("subjectId"), lObj.getString("selectRule"));
				 subNameMap.put(lObj.getString("subjectId"), lObj.getString("subjectName"));
			 }
		 }
		 
		/**
		 *  显示方式有三种：
		 *  一、按单科选 ：显示单科
		 *  二、按组合选|| （补选开始&&该登录人正选选成功即isFixedZh为1时）  ： 显示组合
		 *  三、补选开始&&正选未选成功 ：显示上次选择内容并显示组合
		 */
		 List<JSONObject> subjects = new ArrayList<JSONObject>();
		 List<JSONObject> zhs = new ArrayList<JSONObject>();
		 returnObj.put("isFixed", 0); //默认为0
	     returnObj.put("lastSelectedSubjects", "");
		 if("0".equals(wfWay) && "0".equals(isByElection)){//方案一、
			 returnObj.put("wfStartTime", wfStartTime);
			 returnObj.put("wfEndTime", wfEndTime);
			 List<JSONObject> studentTbList = wishFillingSetDao.getStudentTb(param);
			 List<String> selectedList =new ArrayList<String>();
			 for(JSONObject studentTb:studentTbList){
				 String subjectId = studentTb.getString("subjectId");
				 selectedList.add(subjectId);
			 }
			 for(String subjectId:subList){
				  JSONObject subObj = new JSONObject();
				  subObj.put("subjectId", subjectId);
				  subObj.put("subjectName", subNameMap.get(subjectId));
				  subObj.put("selectRule", subRuleMap.get(subjectId));
				  if(selectedList.contains(subjectId)){
					  subObj.put("isSelected", 1);
				  }else{
					  subObj.put("isSelected", 0);
				  }
				  subjects.add(subObj);
			 }
		 }else{
			 //方案二或者方案三
			 //获取选择的组合
			 JSONObject zhStudent = wishFillingSetDao.getZhStudent(param); //获取当前登录人的正选结果
			 JSONObject byZhStudent = wishFillingSetDao.getByZhStudent(param); //获取当前登录人的补选结果
			 String selectedZhId = "-1";
			 List<JSONObject> zhList = wishFillingSetDao.getZhListByTb(param);
			 Map<String,String> zhIdSubNameMap = new HashMap<String,String>();
			 //获取zhs start
			 if("0".equals(isByElection)){ //补选没开始
				 if(zhStudent!=null){
					 selectedZhId = zhStudent.getString("zhId");
				 }
			 }else{ //补选开始
				 if(byZhStudent!=null){
					 selectedZhId = byZhStudent.getString("zhId");
				 }
			 }
	         for(JSONObject zhObj:zhList){
	        	 JSONObject returnZh = new JSONObject();
	        	 List<JSONObject> zhSubjects = new ArrayList<JSONObject>();
	        	 String zhId = zhObj.getString("zhId");
	        	 String zhWay = zhObj.getString("zhWay");
	        	 returnZh.put("zhId", zhId);
	        	 returnZh.put("zhName", zhObj.getString("zhName"));
	        	 String zhSubjectIds = zhObj.getString("subjectIds");
	        	 List<String> zhSubjectList = Arrays.asList( zhSubjectIds.split(",") );
	        	 String subNames = "";
	        	 if(selectedZhId.equals(zhId)){
	        		 returnZh.put("isSelected", 1);
        		 }else{
        			 returnZh.put("isSelected", 0);
        		 }
	        	 for(String id:zhSubjectList){
	        		 JSONObject zhSubject = new JSONObject();
	        		 String name = subNameMap.get(id);
	        		 subNames+=name+",";
	        		 zhSubject.put("subjectId",id);
	        		 zhSubject.put("subjectName",name);
	        		 zhSubjects.add(zhSubject);
	        	 }
	        	 if(StringUtils.isNotBlank(subNames)){
	        		 subNames = subNames.substring(0,subNames.length()-1);
	        		 zhIdSubNameMap.put(zhId, subNames);
	        	 }
	        	 returnZh.put("zhSubjects", zhSubjects);
	        	 if("0".equals(wfWay)&& "1".equals(isByElection)){
	        		 if("1".equals(zhWay)){
	        			 zhs.add(returnZh);
	        		 }
	        	 }else{
	        		 zhs.add(returnZh);
	        	 }
	         }
			 //end of 获取zhs
	         
	         //获取时间
	         if("0".equals(wfWay)&& "1".equals(isByElection)){ //补选开始 则按补选时间
	        	 returnObj.put("wfStartTime", byStartTime);
				 returnObj.put("wfEndTime", byEndTime);
	         }else{ //按组合选的正选时间
	        	 returnObj.put("wfStartTime", wfStartTime);
				 returnObj.put("wfEndTime", wfEndTime);
	         }
	         //获取lastSelectedSubjects
	         returnObj.put("lastSelectedSubjects", "无");
	         if("0".equals(wfWay)&&"1".equals(isByElection)){
	        	 if(zhStudent!=null){
		        	 String id = zhStudent.getString("zhId");
		        	 String names = zhIdSubNameMap.get(id);
		        	 if(StringUtils.isNotBlank(names)){
		        		 returnObj.put("lastSelectedSubjects", names);
	        	     }
	        	 }
	         }
	         //获取isFixed
	         returnObj.put("isFixed", "0");
	         if("0".equals(wfWay)&&"1".equals(isByElection)){
	        	 if(byZhStudent!=null){
	        		 String isFixedZh = byZhStudent.getString("isFixedZh");
	        		 if("1".equals(isFixedZh)){
	        			 returnObj.put("isFixed", "1");
	        		 }
	        	 }
	         }
		 }
		 returnObj.put("wfId", wfId);
		 returnObj.put("wfName", wfName);
		 returnObj.put("wfNum", wfNum);
		 returnObj.put("isByElection", isByElection);
		 returnObj.put("wfWay", wfWay);
		 returnObj.put("subjects",subjects);
		 returnObj.put("zhs",zhs);
		return returnObj;
	}
	@Override
	public int addStudentTb(JSONObject param) throws Exception {
	   param.put("isImport", 0); 
	   String isManager = param.getString("isManager"); //管理员调整传“1”
	   if(!"1".equals(isManager)){
		   param.put("isAdjust", 0); //学生提交
	   }else{
		   param.put("isAdjust", 1);//管理员
	   }
	   List<Long> subList = (List<Long>) param.get("subList");
	   String subIds = "";
	   for(Long subId:subList){
		subIds+= subId+",";
	   }
	   if(StringUtils.isNotBlank(subIds)){
		   subIds=subIds.substring(0,subIds.length()-1);
	   }
	   String zhId = param.getString("zhId");
	   String schoolId = param.getString("schoolId");
	   String wfId = param.getString("wfId");
	   String accountId=param.getString("accountId");
	   String useGrade=param.getString("useGrade");
	   String classId=param.getString("classId");
	   String termInfoId=param.getString("termInfoId");
	   String schoolYear=param.getString("schoolYear");
	   JSONObject wfObj = wishFillingSetDao.getTb(param);
	   String wfGradeId = wfObj.getString("wfGradeId");
	   String isByElection = wfObj.getString("isByElection");
	   String wfWay = wfObj.getString("wfWay");
	   Integer wfNum = wfObj.getInteger("wfNum");
	   //判断补选是否开始，补选开始 传入的又是组合为空的情况下 返回错误
	   if("0".equals(wfWay) && "1".equals(isByElection)&& StringUtils.isBlank(zhId)){
		   return -5;
	   }
	   //判断补选是否开始，补选未开始 传入的又是组合不为空的情况下 返回错误
	   if("0".equals(wfWay) && "0".equals(isByElection)&& (subList==null|| subList.isEmpty())){
		   return -7;
	   }
	   
		//判断是否到了开放时间
	   if(!"1".equals(isManager)){
		   String startTime = "";
		   String endTime  = "";
		   if("0".equals(isByElection)){
			   startTime = wfObj.getString("wfStartTime");
			   endTime = wfObj.getString("wfEndTime");
		   }else{
			   startTime = wfObj.getString("byStartTime");
			   endTime = wfObj.getString("byEndTime");
		   }
		   Date startDate = null;
		   Date endDate = null;
		   if(StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)){
			   startTime+=":00";
			   endTime+=":59";
			   startDate = DateUtil.parseDateSecondFormat(startTime);
			   endDate = DateUtil.parseDateSecondFormat(endTime);
			   Date d = new Date();
			   if(!(d.getTime()>=startDate.getTime() && d.getTime()<=endDate.getTime())){
				   return -2; //不在开放时间内
			   }
		   }
	   }
	   //判断提交的科目个数是否为符合的个数
	   if("0".equals(wfWay) && "0".equals(isByElection)) //单科选
		{
		   if(subList.size()!=wfNum){
			   return -4;
		   }
		}
	   //判断该组合是否已被删除
	   boolean isDeleted = true;
	   String zhSubjectIds =""; //需要添加的科目代码ids
	   List<Long> zhSubjectIdList = new ArrayList<Long>();
	   List<String> fixedZhIdList = new ArrayList<String>();
	   Map<String,String> fixedZhSubMap = new HashMap<String,String>();// subjectIds-zhId
	   List<JSONObject> lList = wishFillingSetDao.getZhListByTb(param);
	   for(JSONObject obj:lList){
		   String id = obj.getString("zhId");
		   String zhWay = obj.getString("zhWay");
		   String subjectIds = obj.getString("subjectIds");
		   if("1".equals(zhWay)){
			   fixedZhIdList.add(id);
			   fixedZhSubMap.put(subjectIds, id);
		   }
		   //补选的情况下 只获取和判断 固定组合
		   if("0".equals(wfWay) && "1".equals(isByElection) && "0".equals(zhWay)){continue;}
		   if(StringUtils.isNotBlank(id) && (id.equals(zhId) || subjectIds.equals(subIds)  )   ){
			   if(subjectIds.equals(subIds) ){ //zhId为空的情况下 判断subIds 然后找到这个id 保存到zhId下
				   zhId=id;
			   }
			   isDeleted = false;
			   //记下该组合的subjectIds
			   zhSubjectIds = obj.getString("subjectIds");
		   }
	   }
	   if(isDeleted){
		  return -3; 
	   }
	   
	   //判断如果是普通学生提交，则补选开始了，固定的组合的那部分学生是不允许修改的
	   JSONObject byZhStudent = wishFillingSetDao.getByZhStudent(param);
	   if("1".equals(isByElection) && !"1".equals(isManager) ){
		   if(byZhStudent!=null && "1".equals(byZhStudent.getString("isFixedZh"))){ 
			  return -6; 
		   }
	   }
	  
	   if(StringUtils.isNotBlank(zhSubjectIds)){
		   zhSubjectIdList = StringUtil.toListFromString(zhSubjectIds);
	   }
	   //删除原来的studentTb数据 （正选的情况下）
	   if("0".equals(isByElection)){ //删除当前学生的 studenttb表
		   wishFillingSetDao.deleteStudentTb(param);
	   }else{ //删除当前学生的 studenttbby表
		   wishFillingSetDao.deleteByStudentTbByAccountId(param);
	   }
	   List<JSONObject> insertStudentTb = new ArrayList<JSONObject>();
	   if("0".equals(isByElection) && "0".equals(wfWay)){ //单科且没补选
			   for(Long subjectId:subList){ // 单科选
				   JSONObject obj = new JSONObject();
				   obj.put("schoolId", schoolId);
				   obj.put("schoolYear", schoolYear);
				   obj.put("termInfoId", termInfoId);
				   obj.put("classId", classId);
				   obj.put("useGrade", useGrade);
				   obj.put("accountId", accountId);
				   obj.put("wfId", wfId);
				   obj.put("subjectId", subjectId);
				   obj.put("isImport", 0);
				   if(!"1".equals(isManager)){
					   obj.put("isAdjust", 0); //学生提交
				   }else{
					   obj.put("isAdjust", 1);//管理员
				   }
				   insertStudentTb.add(obj);
			   }
	   }else{ //补选 或者 组合选
		  for(Long subjectId:zhSubjectIdList){
			  JSONObject obj = new JSONObject();
			   obj.put("schoolId", schoolId);
			   obj.put("schoolYear", schoolYear);
			   obj.put("termInfoId", termInfoId);
			   obj.put("classId", classId);
			   obj.put("useGrade", useGrade);
			   obj.put("accountId", accountId);
			   obj.put("wfId", wfId);
			   obj.put("subjectId", subjectId);
			   obj.put("isImport", 0);
			   if(null != byZhStudent &&"1".equals(byZhStudent.getString("isFixedZh"))){
				   obj.put("isFixedZh", 1); //固定
			   }else{
				   obj.put("isFixedZh", 2); //补选 且 可修改的
			   }
			   if(!"1".equals(isManager)){
				   obj.put("isAdjust", 0); //学生提交
			   }else{
				   obj.put("isAdjust", 1);//管理员
			   }
			   insertStudentTb.add(obj);
		  }
	   }
	   if("0".equals(isByElection)) { ////不是补选，则插入studenttb表
		   wishFillingSetDao.insertStudentTbBatch(insertStudentTb);
	   }else{
		   wishFillingSetDao.insertByStudentTbBatch(insertStudentTb);//是补选，则插入studenttbby表
	   }
	   //处理zhstudent表
	   if("0".equals(isByElection) && "0".equals(wfWay)){ //单科
		   Collections.sort(subList); //排序
		   String toBeSubjectIds="";
		   for(Long s:subList){
			   toBeSubjectIds+=s+",";
		   }
		   toBeSubjectIds=toBeSubjectIds.substring(0,toBeSubjectIds.length()-1);
		   //获取学生组合（若第一次提交则为null）
		   JSONObject zhStudentObj = wishFillingSetDao.getZhStudent(param);
		   //获取已有的所有组合
		   List<JSONObject> zhSubjects = wishFillingSetDao.getZhSubject(param);
		   String oldZhId = "";
		   for(JSONObject zhSubject:zhSubjects){//获取已经有的组合找到zhId
			  String subjectIds=zhSubject.getString("subjectIds");
			  if(subjectIds.equals(toBeSubjectIds)){
				  oldZhId = zhSubject.getString("zhId");
			  }
		   }
		   if(zhStudentObj!=null){//删除掉以前的已选 组合学生数据
			   zhStudentObj.put("termInfo", param.getString("termInfo"));
			   wishFillingSetDao.deleteZhStudent(zhStudentObj);
		   }
		   if(StringUtils.isBlank(oldZhId)){
			   throw new Exception("Wishfilling ERROR can not find zhId from zhSubject table！");
		   }
		   param.put("zhId", oldZhId);
		   param.put("isFixedZh", 0);
		   wishFillingSetDao.insertZhStudent(param);
	   }else{ //组合 || 补选
		   if("1".equals(wfWay) && "0".equals(isByElection)){
			   wishFillingSetDao.deleteZhStudent(param);
			   param.put("isFixedZh", 0);//zhStudent表中的该字段已废弃
			   wishFillingSetDao.insertZhStudent(param);
		   }else{ 
			   //补选的zhstudentby
			   wishFillingSetDao.deleteByZhStudent(param);
			   if(byZhStudent!=null && "1".equals(byZhStudent.getString("isFixedZh"))){
				   param.put("isFixedZh", 1);
			   }else{
				   param.put("isFixedZh", 2);
			   }
			   wishFillingSetDao.insertByZhStudent(param);
		   }
	   }
	   //调用分班新增、修改志愿的接口
	   List<JSONObject> studentWishs = new ArrayList<JSONObject>();
	   JSONObject wish = new JSONObject();
	   wish.put("accountId", accountId);
	   if("0".equals(isByElection) && "0".equals(wfWay)){ //单科且没补选
		   wish.put("wishId", subIds);
	   }else{ //组合
		   wish.put("wishId", zhSubjectIds);
	   }
	   studentWishs.add(wish);
	   logger.info("wishFillingPlacementtask:updateWish  schoolId："+schoolId+"  wfGradeId:"+wfGradeId+" wfId: "+wfId+"  studentWishs:"+studentWishs.toString() );
	   placementTaskService.updateWish(schoolId,wfGradeId, wfId,studentWishs);
	   return 1;
	}
	@Override
	public List<JSONObject> getTbSelectList(JSONObject param) throws Exception {
		String curTermInfo = param.getString("termInfo"); //当前学年学期
		String accountId = param.getString("accountId");
		String schoolId =  param.getString("schoolId");
		//获取小于等于当前学年学期且大于等于高一年级所在学年学期的列表
		List<JSONObject> list = wishFillingSetDao.getTbSelectList(param);
		List<String> wfIdList = new ArrayList<String>();
		for(JSONObject obj:list){
			String wfId = obj.getString("wfId");
			wfIdList.add(wfId);
		}
		JSONObject json = new JSONObject();
		json.put("wfIdList", wfIdList);
		json.put("accountId",accountId);
		json.put("schoolId", schoolId);
		//根据传入的wfIds查询开放时间内的wfIds
		List<String> timeWfList = wishFillingSetDao.getWfListByOpenTime(param);
		//根据当前用户有无填报数据查询wfIds
		List<String> hasTbWfList = wishFillingSetDao.getWfListByAccountId(json);
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(JSONObject obj:list){
			String wfId = obj.getString("wfId");
			if(timeWfList.contains(wfId)|| hasTbWfList.contains(wfId)){
				obj.put("isHistory", 0);
				if(!timeWfList.contains(wfId)){
					obj.put("isHistory", 1);
				}
				returnList.add(obj);
			}
		}
		return returnList;
	}
	@Override
	public List<JSONObject> getLastOpenTb(JSONObject param) throws Exception {
		return wishFillingSetDao.getLastOpenTb(param);
	}
	@Override
	public void insertFile(JSONObject param) {
		wishFillingSetDao.insertFile(param);
	}
	@Override
	public void deleteFile(JSONObject param) {
		wishFillingSetDao.deleteFile(param);
	}
	@Override
	public JSONObject getFile(JSONObject param) {
		return wishFillingSetDao.getFile(param);
	}
	@Override
	public JSONObject getFileById(JSONObject param) {
		return wishFillingSetDao.getFileById(param);
	}
}
