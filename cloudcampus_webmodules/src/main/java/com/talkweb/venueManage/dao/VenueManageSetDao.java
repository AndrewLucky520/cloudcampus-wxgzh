package com.talkweb.venueManage.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 场馆使用-设置DAO
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface VenueManageSetDao {
   /**设备要求**/
   List<JSONObject> getEquipmentRequireList(JSONObject param);
   void deleteEquipmentRequireContent(JSONObject param);
   void addEquipmentRequireBatch(List<JSONObject> param);
   List<JSONObject> getEquipmentRequireContentList(JSONObject param);
   void addEquipmentRequireContentBatch(List<JSONObject> param);
   /**场馆设置**/
   List<JSONObject> getVenueTypeList(JSONObject param);
   List<JSONObject> getVenueSetList(JSONObject param);
   List<JSONObject> getVenueSetAndTypeAndManagerList(JSONObject param);
   List<JSONObject> getVenueManagerList(JSONObject param);
   JSONObject getVenueSet(JSONObject param);
   JSONObject getVenueType(JSONObject param);
   void addVenueType(JSONObject param);
   void addVenueSet(JSONObject param);
   void deleteVenueType(JSONObject param);
   void deleteVenueSet(JSONObject param);
   /**管理人员设置**/
   void addManagerBatch(List<JSONObject> param);
   void deleteManager(JSONObject param);
   /**检查项目设置**/
   void addInspectionItemBatch(List<JSONObject> param);
   void addInspectionItemComment(JSONObject param);
   void deleteInspectionItem(JSONObject param);
   List<JSONObject> getInspectionItemList(JSONObject param);
   JSONObject getInspectionItemComment(JSONObject param);
   /**申请单设置**/
   void deleteApply(JSONObject param);
   void deleteOccupy(JSONObject param);
   void addOccupy(JSONObject param);
   void addApply(JSONObject param);
   List<JSONObject> getApplyAndOccupyList(JSONObject param);
   List<JSONObject> getApplyAndOccupyListPlus(JSONObject param);
   void updateApplyState(JSONObject param);
   void updateCheckState(JSONObject param);
   /**审核**/
   void addExamApply(JSONObject param);
   void addInspectionApplyBatch(List<JSONObject> param);
   void addInspectionApply(JSONObject param);
   List<JSONObject> getInspectionApplyList(JSONObject param);
   List<JSONObject> getInspectionApplyRecordList(JSONObject param);
   List<JSONObject> getExamApplyList(JSONObject param);
   void deleteExamApply(JSONObject param);
   void deleteInspectionRecordApply(JSONObject param);
   void deleteInspectionApply(JSONObject param);
   /**统计**/
   List<JSONObject> getVenueStaticList(JSONObject param);
   List<JSONObject> getInspectionStaticList(JSONObject param);
   /**历史**/
   List<JSONObject> getApplyHistoryList(JSONObject param);
   void addApplyHistoryBatch(List<JSONObject> param);
   void deleteApplyHistory(JSONObject param);
   void updateApplyHistory(JSONObject param);
   /**设备管理*/
   public void addEquipmentManagerBatch(List<JSONObject> param);
   public void deleteEquipmentManager(JSONObject param);
   void updateVenueApplyEquipmentExam(JSONObject param);
   List<JSONObject> getEquipmentManagerList(JSONObject param);
   public List<JSONObject> getEquipmentrequirecontentPrepared(JSONObject param) ;
   void updateApplyEquipmentStatus(JSONObject param);
   
   public JSONObject getVenueAndAprovel(JSONObject param);
   
   /**ZHXY-334 **/
   public List<JSONObject> getApplyList(JSONObject param);
}
