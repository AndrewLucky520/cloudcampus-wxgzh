package com.talkweb.dayDayUpLogin.utils;

import java.util.HashMap;
import java.util.Map;

public class JYcloudMenuProperties {
	private static Map<String,String> menuMap = new HashMap<String, String>();
	static{
		menuMap.put("1", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/equipmentRepair");//设备报修
		menuMap.put("2", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/venueManage");//场馆使用
		menuMap.put("3", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/salaryManagement");//工资管理
		menuMap.put("4", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=newNEMT/examResults");//成绩
		menuMap.put("5", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/syllabus");//课表
		//menuMap.put("6", "/cloudcampus_webmodules/zhx/redirect/authredirect.do");//短信
		menuMap.put("7", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=teacherDevelopment/questionnaire");//问卷调查
		menuMap.put("8", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/course");//选课
		menuMap.put("9", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=teacherDevelopment/evaluation");//教学考评
		menuMap.put("10", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/weekWork");//周工作
		menuMap.put("11", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/leave");//请假
		menuMap.put("12", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=teacherDevelopment/teacherEvaluation");//教师评价
		menuMap.put("13", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=teacherDevelopment/teachingResearch");//教学成果
		menuMap.put("14", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=newNEMT/wishFilling");//3+3选科
		menuMap.put("15", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=newNEMT/placementTask");//智能分班
		menuMap.put("16", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=newNEMT/schedule");//走班排课
		menuMap.put("17", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=newNEMT/examManagement");//考务安排
		//menuMap.put("18", "/cloudcampus_webmodules/onecardManage/common/jumpOnecardPage.do");//考勤
		menuMap.put("19", "http://pre.yunxiaoyuan.com/cloudcampus_webmodules/talkCloud/getApplication?menuFlag=cloudManager/notice");//通知
	}
	public static Map<String,String> getMenuMap() {
		return menuMap;
	}
}
