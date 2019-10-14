package com.talkweb.venueManage.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.venueManage.dao.VenueManageSetDao;
import com.talkweb.venueManage.service.VenueManageTeacherService;
import com.talkweb.venueManage.util.Util;

/** 
 * 场馆使用-老师SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Service
public class VenueManageTeacherServiceImpl implements VenueManageTeacherService {
	@Autowired
	private VenueManageSetDao venueManageSetDao;
	/**
	 * 老师获取场馆申请单列表
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getVenueApplyList(JSONObject param)
			throws Exception {
		final String AGREE ="1"; //同意
		final String NONEEDEXAM ="2"; //无需审核
		String teacherId = param.getString("teacherId"); //当前登录用户
		String role = param.getString("role");
	
		//查询申请单（时间段）
	    List<JSONObject> applyOccupyList =	venueManageSetDao.getApplyAndOccupyList(param); 
	    //查询类别和场馆名称下的场馆（场馆名称+类别）
		List<JSONObject> venueList = venueManageSetDao.getVenueSetAndTypeAndManagerList(param);
		//查询所有的审批记录
		List<JSONObject> examApplyList = venueManageSetDao.getExamApplyList(param);
		Map<String,JSONObject> examApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject examApply :examApplyList){
			if(examApplyMap.get(examApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			examApplyMap.put(examApply.getString("applyId"), examApply);
		}
		//查询所有的检查记录
		List<JSONObject> inspectionApplyist = venueManageSetDao.getInspectionApplyList(param);
		Map<String,JSONObject> inspectionApplyMap = new HashMap<String, JSONObject>(); 
		for(JSONObject inspectionApply :inspectionApplyist){
			if(inspectionApplyMap.get(inspectionApply.getString("applyId"))!=null){    //根据最近一次的审批情况考虑
				continue;
			}
			inspectionApplyMap.put(inspectionApply.getString("applyId"), inspectionApply);
		}
		Map<String, JSONObject> venueMap = new HashMap<String, JSONObject>(); //setId --JSONOBJ
		for(JSONObject venue:venueList){
			String setId = venue.getString("setId");
			venueMap.put(setId, venue);
		}
		//开始返回data
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		int count=0;
		for(int i=0;i<applyOccupyList.size();i++ ){
			JSONObject applyOccupy = applyOccupyList.get(i);
			String useDate = Util.getFormatDate(applyOccupy.getString("useStartDate"),applyOccupy.getString("useEndDate"));
			applyOccupy.put("useDate", useDate);
			String setId = applyOccupy.getString("setId");
			String applyId = applyOccupy.getString("applyId");
			String teacherIdApply = applyOccupy.getString("teacherId");//申请人
			if(!teacherId.equals(teacherIdApply)){ //当前登录人必须是老师
				continue;
			}
			JSONObject venue = venueMap.get(setId);
			if(venue==null){
				continue;
			}
			count++;
			applyOccupy.put("index", count);
			applyOccupy.put("teacherName", venue.getString("teacherNames"));
			applyOccupy.put("venueName", venue.getString("venueName"));
			applyOccupy.put("venueAddr", venue.getString("venueAddr"));
			applyOccupy.put("isDelete", 2);//初始化 默认为2
			String teacherIds=venue.getString("teacherIds"); //该申请的对应场馆的审批人员
			String isNeedExam=venue.getString("isNeedExam"); //无需审核
			JSONObject examApply = examApplyMap.get(applyId); //最近审批记录
			JSONObject inspectionApply = inspectionApplyMap.get(applyId); //最近检查记录
			//申请状态、是否可编辑、是否可删除
			if(NONEEDEXAM.equals(isNeedExam)){  //无需审核
				applyOccupy.put("applyState", 1);
				if(inspectionApply!=null){ //有检查记录
					applyOccupy.put("isUpdate", 2);
					applyOccupy.put("isDelete", 2);
				}else{
					applyOccupy.put("isUpdate", 1);
					applyOccupy.put("isDelete", 1);
				}
			}else{   //需审核
				if(examApply==null){  //无审核记录
					applyOccupy.put("applyState", 2);
					applyOccupy.put("isUpdate", 1);
					applyOccupy.put("isDelete", 1);
				}else{ //有审核记录
					String examState = examApply.getString("examState");
					if(AGREE.equals(examState)){ //同意
						applyOccupy.put("applyState", 3);
						applyOccupy.put("isUpdate", 2);
						applyOccupy.put("isDelete", 2);
					}else{   //不同意
						applyOccupy.put("applyState", 4);
						applyOccupy.put("isUpdate", 2);
						applyOccupy.put("isDelete", 2);
					}
				}
			}
			returnList.add(applyOccupy);
		}
		return returnList;
	}
}
