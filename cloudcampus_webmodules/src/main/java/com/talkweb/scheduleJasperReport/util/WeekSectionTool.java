package com.talkweb.scheduleJasperReport.util;

import java.util.ArrayList;
import java.util.List;

public class WeekSectionTool {
	
	public static List<String> week = new ArrayList<String>();
	
	public static List<String> section = new ArrayList<String>();
	
	public static List<String> StudentPlacement = new ArrayList<String>();
	
	static{
		week.add("星期一");
		week.add("星期二");
		week.add("星期三");
		week.add("星期四");
		week.add("星期五");
		week.add("星期六");
		week.add("星期日");
		
		section.add("一");
		section.add("二");
		section.add("三");
		section.add("四");
		section.add("五");
		section.add("六");
		section.add("七");
		section.add("八");
		section.add("九");
		
		StudentPlacement.add("学生姓名");
		StudentPlacement.add("科目组合");
		StudentPlacement.add("教学班");
		StudentPlacement.add("原行政班");
		
	}

}