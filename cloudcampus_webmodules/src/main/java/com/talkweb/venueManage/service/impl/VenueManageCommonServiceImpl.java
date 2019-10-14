package com.talkweb.venueManage.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.venueManage.dao.VenueManageSetDao;
import com.talkweb.venueManage.service.VenueManageCommonService;

/** 
 * 场馆使用-公共SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Service
public class VenueManageCommonServiceImpl implements VenueManageCommonService{
	
	@Autowired
	private VenueManageSetDao venueManageSetDao;
	@Autowired
	private AllCommonDataService allCommonDataService;
	/**
	 * 获取所有教师列表
	 * @param req
	 * @param request
	 * @param res
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getAllTeacherList(JSONObject param)
			throws Exception {
		List<JSONObject> rList = new ArrayList<JSONObject>();
		School school = (School) param.get("school");
		String termInfoId = param.getString("termInfoId");
		String teacherName = param.getString("teacherName");
		teacherName = teacherName==null?"":teacherName.trim();
		List<Account> tList = allCommonDataService.getAllSchoolEmployees(school, termInfoId, teacherName);
		for (Account account : tList) {
			JSONObject line = new JSONObject();
			line.put("teacherId", account.getId());
			line.put("teacherName", account.getName());
			rList.add(line);
		}
		return rList;
	}
	/**
	 * 获取所有场馆类别列表
	 * @param req
	 * @param request
	 * @param res
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getAllVenueTypeList(JSONObject param)
			throws Exception {
		return venueManageSetDao.getVenueTypeList(param);
	}
	/**
	 * 获取所有设备要求列表，若数据库无数据则新增
	 * @param req
	 * @param request
	 * @param res
	 * @author zhh
	 */
	@Override
	public List<JSONObject> updateAllEquipmentRequireList(JSONObject param)
			throws Exception {
		List<JSONObject> equipmentRequires=new ArrayList<JSONObject>(); 
		 equipmentRequires = venueManageSetDao.getEquipmentRequireList(param);
		 //首次使用则添加至数据库
		if(CollectionUtils.isEmpty(equipmentRequires) || equipmentRequires.size()<1){
			equipmentRequires =getEquipmentRequire(param.getString("schoolId"));
			venueManageSetDao.addEquipmentRequireBatch(equipmentRequires);
		}
		return equipmentRequires;
	}
	/**
	 * 首次使用设备要求设定项
	 * @param req
	 * @param request
	 * @param res
	 * @author zhh
	 */
	private List<JSONObject> getEquipmentRequire(String schoolId){
		List<JSONObject> equipmentRequires=new ArrayList<JSONObject>(); 
		JSONObject htEquipment= new JSONObject();
		htEquipment.put("schoolId", schoolId);
		htEquipment.put("requireId", UUIDUtil.getUUID());
		htEquipment.put("requireName", "有线话筒");
		htEquipment.put("requireContent", "数量");
		htEquipment.put("type", 1);
		htEquipment.put("createDate", DateUtil.getDateFormatNow());
		htEquipment.put("index", 1);
		equipmentRequires.add(htEquipment);
		
		JSONObject wxhtEquipment= new JSONObject();
		wxhtEquipment.put("schoolId", schoolId);
		wxhtEquipment.put("requireId", UUIDUtil.getUUID());
		wxhtEquipment.put("requireName", "无线话筒");
		wxhtEquipment.put("requireContent", "数量");
		wxhtEquipment.put("type", 1);
		wxhtEquipment.put("createDate", DateUtil.getDateFormatNow());
		wxhtEquipment.put("index", 2);
		equipmentRequires.add(wxhtEquipment);
		
		JSONObject screenEquipment= new JSONObject();
		screenEquipment.put("schoolId", schoolId);
		screenEquipment.put("requireId", UUIDUtil.getUUID());
		screenEquipment.put("requireName", "使用显示屏");
		screenEquipment.put("requireContent", "显示内容");
		screenEquipment.put("type", 2);
		screenEquipment.put("createDate", DateUtil.getDateFormatNow());
		screenEquipment.put("index", 3);
		equipmentRequires.add(screenEquipment);
		
		JSONObject tyEquipment= new JSONObject();
		tyEquipment.put("schoolId", schoolId);
		tyEquipment.put("requireId", UUIDUtil.getUUID());
		tyEquipment.put("requireName", "使用投影仪");
		tyEquipment.put("requireContent", "使用原因");
		tyEquipment.put("type", 2);
		tyEquipment.put("createDate", DateUtil.getDateFormatNow());
		tyEquipment.put("index", 4);
		equipmentRequires.add(tyEquipment);
		
		JSONObject otherEquipment= new JSONObject();
		otherEquipment.put("schoolId", schoolId);
		otherEquipment.put("requireId", UUIDUtil.getUUID());
		otherEquipment.put("requireName", "其他要求");
		otherEquipment.put("requireContent", "");
		otherEquipment.put("type", 3);
		otherEquipment.put("createDate", DateUtil.getDateFormatNow());
		otherEquipment.put("index", 5);
		equipmentRequires.add(otherEquipment);
		
		return equipmentRequires;
	}
	/**
	 * 获取场馆角色
	 * @param req
	 * @param request
	 * @param res
	 * @author zhh
	 */
	@Override
	public JSONObject getVenueRole(JSONObject param) throws Exception {
		JSONObject data = new JSONObject(); 
		boolean flag = param.getBoolean("flag");
		
		if(flag) {//是场馆系统管理员
			data.put("role", 0);
		}
		//查询当前是否设置了场馆
		List<JSONObject> venueSetList = venueManageSetDao.getVenueSetList(param);
		if(CollectionUtils.isNotEmpty(venueSetList) && venueSetList.size()>0 ){
			data.put("haveData", 1);
		}else{
			data.put("haveData", 0);
		}
		//是否为审核人员 ，否则为普通老师
		String role = data.getString("role");
		if(StringUtils.isBlank(role)|| role==null){
			List<JSONObject> venueManagerList = venueManageSetDao.getVenueManagerList(param);
			if(CollectionUtils.isNotEmpty(venueManagerList) && venueManagerList.size()>0  ){
				data.put("role", 1);
			}else{
				data.put("role", 2);
			}
			List<JSONObject> equipmentManagerList = venueManageSetDao.getEquipmentManagerList(param);
			if(CollectionUtils.isNotEmpty(equipmentManagerList) && equipmentManagerList.size()>0  ){
				data.put("isEquipmentAdmin", 1);
			}else{
				data.put("isEquipmentAdmin", 0);
			}
			
		}
		return data;
	}
	@Override
	public List<JSONObject> getApplyList(JSONObject param) {
		param.put("currentDate", getNowDate());
		return venueManageSetDao.getApplyList(param);
	}

	private String getNowDate() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
}
