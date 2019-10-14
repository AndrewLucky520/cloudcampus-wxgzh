package com.talkweb.venueManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
 * 场馆使用-导入SERVICE
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public interface VenueManageImportService {
  List<JSONObject> getAllVenueTypeList(JSONObject param) throws Exception;
  int  addVenueSetBatch(List<JSONObject> param) throws Exception;
}
