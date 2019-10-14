package com.talkweb.venueManage.dao.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.venueManage.dao.VenueManageSetDao;

/** 
 * 场馆使用-设置DIMPL
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Repository
public class VenueManageSetDaoImpl  extends MyBatisBaseDaoImpl implements VenueManageSetDao {

	@Override
	public List<JSONObject> getVenueTypeList(JSONObject param) {
		return selectList("getVenueTypeList",param);
	}

	@Override
	public List<JSONObject> getEquipmentRequireList(JSONObject param) {
		return selectList("getEquipmentRequireList",param);
	}

	@Override
	public List<JSONObject> getVenueSetList(JSONObject param) {
		return selectList("getVenueSetList",param);
	}

	@Override
	public List<JSONObject> getVenueManagerList(JSONObject param) {
		return selectList("getVenueManagerList",param);
	}

	@Override
	public List<JSONObject> getVenueSetAndTypeAndManagerList(JSONObject param) {
		return selectList("getVenueSetAndTypeAndManagerList",param);
	}

	@Override
	public void addInspectionItemBatch(List<JSONObject> param) {
		update("addInspectionItemBatch",param);
	}

	@Override
	public void addInspectionItemComment(JSONObject param) {
		update("addInspectionItemComment",param);
	}

	@Override
	public void deleteInspectionItem(JSONObject param) {
		update("deleteInspectionItem",param);
	}

	@Override
	public List<JSONObject> getInspectionItemList(JSONObject param) {
		return selectList("getInspectionItemList",param);
	}

	@Override
	public JSONObject getInspectionItemComment(JSONObject param) {
		return selectOne("getInspectionItemComment",param);
	}

	@Override
	public JSONObject getVenueSet(JSONObject param) {
		return selectOne("getVenueSet",param);
	}

	@Override
	public JSONObject getVenueType(JSONObject param) {
	   return selectOne("getVenueType", param);
	}

	@Override
	public void addVenueType(JSONObject param) {
		update("addVenueType", param);
	}

	@Override
	public void addManagerBatch(List<JSONObject> param) {
		update("addManagerBatch", param);
	}

	@Override
	public void deleteManager(JSONObject param) {
		update("deleteManager", param);
	}

	@Override
	public void addVenueSet(JSONObject param) {
		update("addVenueSet",param);
		
	}

	@Override
	public void deleteVenueType(JSONObject param) {
		update("deleteVenueType", param);
	}

	@Override
	public void deleteVenueSet(JSONObject param) {
		update("deleteVenueSet", param);
	}

	@Override
	public void deleteEquipmentRequireContent(JSONObject param) {
		update("deleteEquipmentRequireContent", param);
	}

	@Override
	public List<JSONObject> getEquipmentRequireContentList(JSONObject param) {
		return selectList("getEquipmentRequireContentList", param);
	}

	@Override
	public void addEquipmentRequireContentBatch(List<JSONObject> param) {
	update("addEquipmentRequireContentBatch", param);
	}

	@Override
	public void deleteApply(JSONObject param) {
		update("deleteApply", param);
	}

	@Override
	public void deleteOccupy(JSONObject param) {
		update("deleteOccupy", param);
	}

	@Override
	public void addOccupy(JSONObject param) {
		update("addOccupy", param);
	}

	@Override
	public void addApply(JSONObject param) {
		update("addApply", param);
	}

	@Override
	public List<JSONObject> getApplyAndOccupyList(JSONObject param) {
		return selectList("getApplyAndOccupyList",param);
	}
	@Override
	public List<JSONObject> getApplyAndOccupyListPlus(JSONObject param) {
		return selectList("getApplyAndOccupyListPlus",param);
	}
	@Override
	public void addEquipmentRequireBatch(List<JSONObject> param) {
		update("addEquipmentRequireBatch", param);
	}
	@Override
	public void addExamApply(JSONObject param) {
		update("addExamApply", param);
	}
	@Override
	public void addInspectionApplyBatch(List<JSONObject> param) {
		update("addInspectionApplyBatch", param);
	}

	@Override
	public List<JSONObject> getInspectionApplyList(JSONObject param) {
		return selectList("getInspectionApplyList", param);
	}

	@Override
	public List<JSONObject> getExamApplyList(JSONObject param) {
		return selectList("getExamApplyList", param);
	}

	@Override
	public List<JSONObject> getVenueStaticList(JSONObject param) {
		return selectList("getVenueStaticList", param);
	}

	@Override
	public List<JSONObject> getInspectionStaticList(JSONObject param) {
		return selectList("getInspectionStaticList", param);
	}

	@Override
	public void updateApplyState(JSONObject param) {
		update("updateApplyState", param);
	}

	@Override
	public void updateCheckState(JSONObject param) {
		update("updateCheckState", param);
	}

	@Override
	public void addInspectionApply(JSONObject param) {
		update("addInspectionApply",param);
	}

	@Override
	public List<JSONObject> getInspectionApplyRecordList(JSONObject param) {
		return selectList("getInspectionApplyRecordList", param);
	}

	@Override
	public void deleteExamApply(JSONObject param) {
		 update("deleteExamApply",param);
	}

	@Override
	public void deleteInspectionRecordApply(JSONObject param) {
		  update("deleteInspectionRecordApply",param);
	}

	@Override
	public void deleteInspectionApply(JSONObject param) {
	   update("deleteInspectionApply",param);
	}

	@Override
	public List<JSONObject> getApplyHistoryList(JSONObject param) {
		return selectList("getApplyHistoryList",param);
	}

	@Override
	public void addApplyHistoryBatch(List<JSONObject> param) {
	   update("addApplyHistoryBatch", param);
	}

	@Override
	public void deleteApplyHistory(JSONObject param) {
		update("deleteApplyHistory",param);
	}

	@Override
	public void updateApplyHistory(JSONObject param) {
		update("updateApplyHistory",param);
		
	}

	/**设备管理*/
	@Override
	public void addEquipmentManagerBatch(List<JSONObject> param) {
		update("addEquipmentManagerBatch", param);
	}

	@Override
	public void deleteEquipmentManager(JSONObject param) {
		update("deleteEquipmentManager", param);
	}

	@Override
	public void updateVenueApplyEquipmentExam(JSONObject param) {
		 
		update("updateVenueApplyEquipmentExam", param);
	}

	@Override
	public List<JSONObject> getEquipmentManagerList(JSONObject param) {
		 
		return selectList("getEquipmentManagerList",param);
	}
	
	@Override
	public List<JSONObject> getEquipmentrequirecontentPrepared(JSONObject param) {
		 
		return selectList("getEquipmentrequirecontentPrepared",param);
	}

	@Override
	public void updateApplyEquipmentStatus(JSONObject param) {
	 
		update("updateApplyEquipmentStatus", param);
	}

	@Override
	public JSONObject getVenueAndAprovel(JSONObject param) {
		return selectOne("getVenueAndAprovel", param);
	}

	@Override
	public List<JSONObject> getApplyList(JSONObject param) {
		param.put("currentDate", getNowDate());
		return selectList("getApplyList",param);
	}

	private static String getNowDate() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
}
