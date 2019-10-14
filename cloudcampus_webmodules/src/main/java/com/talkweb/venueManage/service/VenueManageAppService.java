package com.talkweb.venueManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
/** 
 * 场馆使用-App SERVICE
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年4月19日  
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface VenueManageAppService {
	List<JSONObject> getVenueApplyList(JSONObject param)throws Exception;
	int addAppVenueApply(JSONObject param)throws Exception;
	JSONObject getAppVenueApplyExamDetail(JSONObject param)throws Exception;
    JSONObject getAppVenueApplyInspectionDetail(JSONObject param)throws Exception;
    List<JSONObject> getAppVenueApplyExamList(JSONObject param)throws Exception;
    
    public JSONObject getVenueAndAprovel(JSONObject param);
    public List<JSONObject> getExamApplyList(JSONObject param);
    public void addExamApply(JSONObject param);
    public List<JSONObject> getVenueManagerList(JSONObject param);
    
    public void sendWxTemplateMsg(JSONObject params);
}
