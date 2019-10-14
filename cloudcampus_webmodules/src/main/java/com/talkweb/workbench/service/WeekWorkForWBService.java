package com.talkweb.workbench.service;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface WeekWorkForWBService {
	List<JSONObject> getWeekWorkItems(JSONObject param);
}
