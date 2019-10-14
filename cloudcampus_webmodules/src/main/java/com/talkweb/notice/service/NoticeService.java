package com.talkweb.notice.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/** 
* @author  Administrator
* @version 创建时间：2017年6月19日 下午2:33:35 
* 程序的简单说明
*/
public interface NoticeService {
 
	public List<JSONObject> getNoticeList(JSONObject param);
	public JSONObject getNotice(JSONObject param);
	public int insertNoticeRecord(JSONObject  param);
	public int insertNoticePersonnel(List<JSONObject>  list);
	public List<JSONObject> getNoticePersonnelList(JSONObject param);
	public int delNoticePersonnel(JSONObject  param);
	public int delNoticeViewed(JSONObject  param);
	public int updateNotice(JSONObject  param);
	public int updateClickCnt(JSONObject  param);	
	public int delNotice(JSONObject  param);
	public int insertNoticeViewed(JSONObject param);
}
