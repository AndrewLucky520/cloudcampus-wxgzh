package com.talkweb.venueManage.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.venueManage.dao.VenueManageSetDao;
import com.talkweb.venueManage.service.VenueManageImportService;
import com.talkweb.venueManage.service.VenueManageSetService;

/** 
 * 场馆使用-导入SIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Service
public class VenueManageImportServiceImpl implements VenueManageImportService {
	@Autowired
	private VenueManageSetDao venueManageSetDao;
	@Autowired
	private VenueManageSetService venueManageSetService;
	/**
	 * 获取所有场馆类别列表
	 * @author zhh
	 */
	@Override
	public List<JSONObject> getAllVenueTypeList(JSONObject param) {	
		return venueManageSetDao.getVenueTypeList(param);
	}
	/**
	 * 获取批量新增场馆
	 * @author zhh
	 */
	@Override
	public int addVenueSetBatch(List<JSONObject> param) {
		int count=0;
		for(int i=0;i<param.size();i++){
			JSONObject venueSet=param.get(i);
			String setId=venueSet.getString("setId");
			if(StringUtils.isBlank(setId)){
				setId=UUIDUtil.getUUID();
				venueSet.put("setId", setId);
			}

			//获取该类别名称下的类别代码
			String venueTypeId="";
			JSONObject venueType = venueManageSetDao.getVenueType(venueSet);
			if( venueType!=null ){ //若有，获取该类别代码
		        venueTypeId = venueType.getString("venueTypeId");
		        venueSet.put("venueTypeId",venueTypeId);
			}else{ //若无，新增这个名称的类别
				venueTypeId = UUIDUtil.getUUID();
				venueSet.put("venueTypeId",venueTypeId );
				venueManageSetDao.addVenueType(venueSet);
			}
			//删除该场馆下的所有管理人员
			venueManageSetDao.deleteManager(venueSet);
			List<JSONObject> teachers = (List<JSONObject>) venueSet.get("teachers");
			for(int j=0;j<teachers.size();j++){
				JSONObject teacher = teachers.get(j);
				teacher.put("schoolId", venueSet.getString("schoolId"));
				teacher.put("setId", setId);
			}
			//新增管理人员
			venueManageSetDao.addManagerBatch(teachers);
			//删除该场馆下的所有设备管理人员
			venueManageSetDao.deleteEquipmentManager(venueSet);
			List<JSONObject> equipmentTeachers = (List<JSONObject>) venueSet.get("equipmentTeachers");
			if (equipmentTeachers!=null) {
				for(int j=0;j<equipmentTeachers.size();j++){
					JSONObject teacher = equipmentTeachers.get(j);
					teacher.put("schoolId", venueSet.getString("schoolId"));
					teacher.put("setId", setId);
				}
				//新增设备管理人员
				venueManageSetDao.addEquipmentManagerBatch(equipmentTeachers);
			}
 
			venueSet.put("isImport", 1);
			 Calendar calendar = Calendar.getInstance ();
			 count++;
			 calendar.add (Calendar.SECOND, param.size()-count);
			String createDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime ());
			venueSet.put("createDate", createDate);
			//新增场馆
		     venueManageSetDao.addVenueSet(venueSet);
		}
		return param.size();
	}
}
