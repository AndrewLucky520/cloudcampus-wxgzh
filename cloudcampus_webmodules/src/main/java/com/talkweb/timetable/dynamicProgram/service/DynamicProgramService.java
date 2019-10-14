package com.talkweb.timetable.dynamicProgram.service;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.timetable.arrangement.exception.ArrangeTimetableException;


public interface DynamicProgramService {

	/**
	 * 智能排课算法入口
	 * @param session
	 * @param school
	 * @param timetableId
	 * @param gradeIds
	 * @param progressMap
	 * @param runParams 
	 * @throws ArrangeTimetableException
	 */
	public void startTask(HttpSession session, School school,
			String timetableId, List<String> gradeIds,
			  JSONObject runParams)
			throws ArrangeTimetableException ;

	public void updateArrangeProgress(String jsessionID,  int code, double progress,
			String msg);
}
