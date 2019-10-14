package com.talkweb.workbench.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.workbench.service.ExamForWBService;
import com.talkweb.workbench.service.NoticeForWBService;
import com.talkweb.workbench.service.PendingItem;
import com.talkweb.workbench.service.PendingItem.ModualType;
import com.talkweb.workbench.service.PendingItemService;
import com.talkweb.workbench.service.TimeTableForWBService;
import com.talkweb.workbench.service.WeekWorkForWBService;
@Service
public class TestServiceImpl implements PendingItemService ,TimeTableForWBService,WeekWorkForWBService,ExamForWBService,NoticeForWBService{
	@Override
	public ModualType getModualType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PendingItem> getPendingItem(JSONObject param) {
		// TODO Auto-generated method stub
		PendingItem one = new PendingItem();
		one.setContent("问卷调查待填写");
		one.setEndDate(new Date());
		one.setStartDate(new Date());
		one.setStartDate(new Date());
		one.setTargetUrl("http://baidu.com");
		List<PendingItem> result = new ArrayList<PendingItem>();
		result.add(one);
		return result;
	}

	@Override
	public JSONObject getExamForWB(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getNoticeForWB(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JSONObject> getWeekWorkItems(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getTimetableForWB(JSONObject param) {
		// TODO Auto-generated method stub
		return null;
	}


}
