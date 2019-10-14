package com.talkweb.venueManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 场馆使用-设置SERVICE
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface VenueManageSetService {
	/**设置**/
	List<JSONObject> getVenueSetList(JSONObject param)throws Exception;
	List<JSONObject> getVenueSetListForEdit(JSONObject param)throws Exception;
	int addVenueSet(JSONObject param)throws Exception;
	int deleteVenueSet(JSONObject param)throws Exception;
	JSONObject getVenueSet(JSONObject param)throws Exception;
	int addInspectionItemSet(JSONObject param)throws Exception;
	int deleteInspectionItemSet(JSONObject param)throws Exception;
	JSONObject getInspectionItemSet(JSONObject param)throws Exception;
	/**申请单**/
	int addVenueApply(JSONObject param)throws Exception;
	int deleteVenueApply(JSONObject param)throws Exception;
	JSONObject getVenueApplyDetail(JSONObject param)throws Exception;
	List<JSONObject> getVenueApplyList(JSONObject param)throws Exception;
	List<JSONObject> getVenueApplyListPlus(JSONObject param)throws Exception;
	List<JSONObject> getVenueSetListPlus(JSONObject param) throws Exception;
	/**审批检查**/
	int addVenueApplyInspection(JSONObject param)throws Exception;
	int addVenueApplyExam(JSONObject param)throws Exception;
	JSONObject getVenueApplyInspectionDetail(JSONObject param)throws Exception;
	JSONObject getVenueApplyExamDetail(JSONObject param)throws Exception;
	/**统计**/
	JSONObject getVenueStaticList(JSONObject param)throws Exception;
 
	List<JSONObject> getPrePareEquipMentList(JSONObject param)throws Exception;
	int updateVenueApplyEquipmentExam(JSONObject param)throws Exception;
	int updateApplyEquipmentStatus(JSONObject param)throws Exception;
	
	List<JSONObject> getEquipmentRequireContentList(JSONObject param);
	
	List<JSONObject> getExamApplyList(JSONObject param);
}
