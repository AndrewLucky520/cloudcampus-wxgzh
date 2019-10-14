package com.talkweb.commondata.util;

import java.util.HashMap;
import java.util.Map;
/**
 * 权限工具类
 * @author zhh
 *
 */
public class PermissionUtil {
	  public static final String NAVTYPE = "2"; //有的项目没有新高考 有的有 作为区别
	  public static final Map<String,String> permissonNormalMap = new HashMap<String,String>();
	  public static final Map<String,String> permissonNewSchoolMap = new HashMap<String,String>();
	  public static final Map<String, Map<String,String>>  permissionTypeMap = new HashMap<String, Map<String,String>>();
	  static{
		  /* // permissonNormalMap.put("18050", "1042");//校内通知
		  permissonNormalMap.put("18051", "1024");//周工作
		  permissonNormalMap.put("18052", "1031");//请假
		  permissonNormalMap.put("18053", "1011");//工资
		//  permissonNormalMap.put("18054", "1015");//短信
		  permissonNormalMap.put("18055", "1012");//选课
		  permissonNormalMap.put("18056", "1009");//成绩
		  permissonNormalMap.put("18057", "1008");//课表
		  permissonNormalMap.put("18058", "1021");//设备报修
		  permissonNormalMap.put("18059", "1022");//场馆使用
		 // permissonNormalMap.put("18060", "1026");//考勤
		 // permissonNormalMap.put("18061", "1023");//基础数据
		  permissonNormalMap.put("18062", "1041");//公文查阅
		  permissonNormalMap.put("18063", "1042");//消息通知
		  permissonNormalMap.put("18064", "1043");//物资申报
		  permissonNormalMap.put("18065", "1049");//班级班风
		  
		  permissonNormalMap.put("19050", "1020");//问卷调查
		  permissonNormalMap.put("19051", "1025");//教师成果
		  permissonNormalMap.put("19052", "1014");//教学考评
		  permissonNormalMap.put("19053", "1013");//教师评价
		  
		  permissonNormalMap.put("24050", "1044");//团籍管理
		  permissonNormalMap.put("24051", "1045");//团组织管理
		  permissonNormalMap.put("24052", "1046");//学生会
		  permissonNormalMap.put("24053", "1047");//社团
		  permissonNormalMap.put("24054", "1048");//扶贫
		  */
		  
		  permissonNormalMap.put("20051", "1024");//周工作 18051--〉20051
		  permissonNormalMap.put("20052", "1031");//请假
		  permissonNormalMap.put("20053", "1011");//工资
		  permissonNormalMap.put("20054", "1015");//绩效考核
		  permissonNormalMap.put("20055", "1012");//选课
		  permissonNormalMap.put("20056", "1009");//成绩
		  permissonNormalMap.put("20057", "1008");//课表
		  permissonNormalMap.put("20058", "1021");//设备报修
		  permissonNormalMap.put("20059", "1022");//场馆使用
		  permissonNormalMap.put("20062", "1041");//公文查阅
		  permissonNormalMap.put("20063", "1042");//消息通知
		  permissonNormalMap.put("20064", "1043");//物资申报
		  permissonNormalMap.put("20065", "1049");//班级班风
		  
		  permissonNormalMap.put("21050", "1020");//问卷调查 19050--〉21050
		  permissonNormalMap.put("21051", "1025");//教师成果
		  permissonNormalMap.put("21052", "1014");//教学考评
		  permissonNormalMap.put("21053", "1013");//教师评价
		  
		  permissonNormalMap.put("25050", "1044");//团籍管理 24050--〉25050
		  permissonNormalMap.put("25051", "1045");//团组织管理
		  permissonNormalMap.put("25052", "1046");//学生会
		  permissonNormalMap.put("25053", "1047");//社团
		  permissonNormalMap.put("25054", "1048");//扶贫
		  
		  
		  //permissonNewSchoolMap.put("20050", "1042"); //校内通知
		  permissonNewSchoolMap.put("20051", "1024");//周工作
		  permissonNewSchoolMap.put("20052", "1031");//请假
		  permissonNewSchoolMap.put("20053", "1011");//工资
		  permissonNewSchoolMap.put("20054", "1015");//绩效考核
		  permissonNewSchoolMap.put("20055", "1012");//选课
		  permissonNewSchoolMap.put("20056", "1009");//成绩
		  permissonNewSchoolMap.put("20057", "1008");//课表
		  permissonNewSchoolMap.put("20058", "1021");//设备报修
		  permissonNewSchoolMap.put("20059", "1022");//场馆使用
		 // permissonNewSchoolMap.put("20060", "1026");//考勤
		 //permissonNewSchoolMap.put("20061", "1023");//基础数据
		  permissonNewSchoolMap.put("20062", "1041");//公文查阅
		  permissonNewSchoolMap.put("20063", "1042");//消息通知
		  permissonNewSchoolMap.put("20064", "1043");//物资申报
		  permissonNewSchoolMap.put("20065", "1049");//班级班风
		  
		  permissonNewSchoolMap.put("21050", "1020");//问卷调查
		  permissonNewSchoolMap.put("21051", "1025");//教师成果
		  permissonNewSchoolMap.put("21052", "1014");//教学考评
		  permissonNewSchoolMap.put("21053", "1013");//教师评价
		  
		  permissonNewSchoolMap.put("22050", "1027");//3+3选科
		  permissonNewSchoolMap.put("22051", "1028");//分班
		  permissonNewSchoolMap.put("22052", "1029");//排课
		  permissonNewSchoolMap.put("22053", "1030");//考务
		  permissonNewSchoolMap.put("22054", "1041");//成绩
		  permissonNewSchoolMap.put("22055", "1042");//走班
		  
		  permissonNewSchoolMap.put("25050", "1044");//团籍管理
		  permissonNewSchoolMap.put("25051", "1045");//团组织管理
		  permissonNewSchoolMap.put("25052", "1046");//学生会
		  permissonNewSchoolMap.put("25053", "1047");//社团
		  permissonNewSchoolMap.put("25054", "1048");//扶贫
		  
		  permissionTypeMap.put("1", permissonNewSchoolMap); //新高考省份
		  permissionTypeMap.put("2", permissonNormalMap);//非新高考省份
	  }
}
