package com.talkweb.timetable.dynamicProgram.core;

import java.util.Hashtable;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;

public class DynamicSubTask implements Runnable{
	
	private ScheduleTable scheduleTable;
	public Hashtable<String, JSONObject> getProgressMap() {
		return progressMap;
	}

	public void setProgressMap(Hashtable<String, JSONObject> progressMap) {
		this.progressMap = progressMap;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	private Hashtable<String, JSONObject> progressMap;
	private String sessionID;
	private int taskNum;
	public int getTaskNum() {
		return taskNum;
	}

	public void setTaskNum(int taskNum) {
		this.taskNum = taskNum;
	}

	public ScheduleTable getScheduleTable() {
		return scheduleTable;
	}

	public void setScheduleTable(ScheduleTable scheduleTable) {
		this.scheduleTable = scheduleTable;
	}

	
	
	public DynamicSubTask(ScheduleTable scheduleTable){
		this.scheduleTable = scheduleTable;
	}

	@Override
	public void run() {
		
		// TODO Auto-generated method stub
//		updateArrangeProgress(sessionID, progressMap, 0, 40, "智能排课-启动子任务" + taskNum );
		System.out.println("【智能排课】启动子任务"+taskNum);
		scheduleTable.startArrange();
//		updateArrangeProgress(sessionID, progressMap, 0, 40, "智能排课-子任务" + taskNum +"执行完成");
		System.out.println("【智能排课】子任务结束"+taskNum);
	}

	public void updateArrangeProgress(String jsessionID,
			Hashtable<String, JSONObject> session, int code, double progress,
			String msg) {

		JSONObject rs = new JSONObject();
		rs.put("arrangeCode", code);
		rs.put("arrangeProgress", progress);
		rs.put("arrangeMsg", msg);

		session.put(jsessionID, rs);
	}
}
