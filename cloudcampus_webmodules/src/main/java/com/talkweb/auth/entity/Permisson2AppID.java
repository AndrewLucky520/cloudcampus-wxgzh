package com.talkweb.auth.entity;

import java.util.HashMap;

/**
 * @author talkweb
 * 根据权限id获取具有权限的功能列表
 *
 */
public class Permisson2AppID {

	public static HashMap<Integer,String> perAppIDMap = new HashMap<Integer,String>();
	
	static{
		
		if(perAppIDMap.isEmpty()){
			//Homework	家庭作业
			perAppIDMap.put(1000, "cs1000");
			//Notice	通知
			perAppIDMap.put(1001, "cs1001");
			//ScoreReport	考试成绩
			perAppIDMap.put(1002, "cs1002");
			//Performance	日常表现
			perAppIDMap.put(1007, "cs1007");
			//CourseSchedule	课表
			perAppIDMap.put(1008, "cs1008");
			//CourseManage
			perAppIDMap.put(1010, "");
			//WagesManage	工资
			perAppIDMap.put(1011, "cs1011");
			//SelectCourseManage 选课
			perAppIDMap.put(1012, "cs1012");
			//CheckEvaluateManage	教师评价
			perAppIDMap.put(1013, "cs1013");
			//TeachEvaluateManage
			perAppIDMap.put(1014, "");
			//ShortMessageManage	短信
			perAppIDMap.put(1015, "cs1015");
			//OAManage
			perAppIDMap.put(1016, "");
			//教学考评
			perAppIDMap.put(1017, "cs1017");
			//问卷调查
			perAppIDMap.put(1018, "cs1018");
			//设备报修
			perAppIDMap.put(1019, "cs1019");
			//周工作
			perAppIDMap.put(1020, "cs1020");
			//教研成果
			perAppIDMap.put(1021, "cs1021");
			//场馆使用
			perAppIDMap.put(1022, "cs1022");
			//考勤
			perAppIDMap.put(1023, "cs1023");
			//综合素质评价
			perAppIDMap.put(1024, "cs1024");
			//志愿填报--新高考选课
			perAppIDMap.put(1025, "cs1025");
			//新高考分班
			perAppIDMap.put(1026, "cs1026");
			//新高考排课
			perAppIDMap.put(1027, "cs1027");
			//新高考考务管理员
			perAppIDMap.put(1028, "cs1028");
			//请假管理员
			perAppIDMap.put(1029, "cs1029");
			//监控管理员
			perAppIDMap.put(1030, "cs1030");
			//招聘系统管理员--24
			perAppIDMap.put(1031, "cs1031");
			//招生系统管理员
			perAppIDMap.put(1032, "cs1032");
			//短信中心管理员
			perAppIDMap.put(1033, "cs1033");
			//成长系统管理员
			perAppIDMap.put(1034, "cs1034");
			//资源中心管理员---28
			perAppIDMap.put(1035, "cs1035");
			//门户配置管理员--50
			perAppIDMap.put(1057, "cs1057");
			
			
		}
	}

	/**
	 * 根据权限获取权限所管辖的菜单
	 */
	public static String getAppIdsByPerName(int permission) {
		
		return perAppIDMap.get(permission);
	}

}
