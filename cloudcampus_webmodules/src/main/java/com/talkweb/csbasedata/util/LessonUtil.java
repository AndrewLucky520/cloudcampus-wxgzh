package com.talkweb.csbasedata.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LessonUtil {
	 public static final String highSchool = "1,2,3,4,5,6,7,8,9,12,13,14,15,16,17,18,19";
	 public static final String middleSchool = "1,2,3,4,5,6,7,8,9,12,13,14,15,16,17,18";
	 public static final String primarySchool = "1,2,3,10,11,12,13,14,15,18";
	 public static final String gradePrimaryNames = "一年级,二年级,三年级,四年级,五年级,六年级";
	 public static final String gradeMiddleNames = "初一,初二,初三";
	 public static final String gradeHighNames = "高一,高二,高三";
	 public static final String primaryGradeLevels = "10,11,12,13,14,15";
	 public static final String middleGradeLevels = "16,17,18";
	 public static final String highGradeLevels = "19,20,21";
	 public static final Map<String, String> lessonIdNameMap = new LinkedHashMap<String, String>();
	 public static final Map<String, String> lessonNameIdMap = new LinkedHashMap<String, String>();
	 public static final Map<String, List<String>> lessonStageMap = new LinkedHashMap<String, List<String>>();
	 public static final Map<String, String> gradeStageMap = new LinkedHashMap<String, String>();
	 public static final Map<String, String> gradeStageNameMap = new LinkedHashMap<String, String>();
	 public static final Map<String, List<String>> gradeGroupStageNameMap = new LinkedHashMap<String, List<String>>();
	 public static final Map<String, String> gradeGroupStageLevelMap = new LinkedHashMap<String, String>();
	 public static final Map<String, List<String>> gradeLevelMap = new LinkedHashMap<String, List<String>>();
	 static {
		 lessonIdNameMap.put("1", "语文");
		 lessonIdNameMap.put("2", "数学");
		 lessonIdNameMap.put("3", "英语");
		 lessonIdNameMap.put("4", "政治");
		 lessonIdNameMap.put("5", "历史");
		 lessonIdNameMap.put("6", "地理");
		 lessonIdNameMap.put("7", "物理");
		 lessonIdNameMap.put("8", "化学");
		 lessonIdNameMap.put("9", "生物");
		 lessonIdNameMap.put("10", "品德与生活");
		 lessonIdNameMap.put("11", "品德与社会");
		 lessonIdNameMap.put("12", "体育");
		 lessonIdNameMap.put("13", "信息");
		 lessonIdNameMap.put("14", "音乐");
		 lessonIdNameMap.put("15", "美术");
		 lessonIdNameMap.put("16", "文综");
		 lessonIdNameMap.put("17", "理综");
		 lessonIdNameMap.put("18", "自习");
		 lessonIdNameMap.put("19", "技术");
		 
		 
		 lessonNameIdMap.put( "语文","1");
		 lessonNameIdMap.put( "数学","2");
		 lessonNameIdMap.put( "英语","3");
		 lessonNameIdMap.put( "政治","4");
		 lessonNameIdMap.put( "历史","5");
		 lessonNameIdMap.put( "地理","6");
		 lessonNameIdMap.put( "物理","7");
		 lessonNameIdMap.put( "化学","8");
		 lessonNameIdMap.put( "生物","9");
		 lessonNameIdMap.put( "品德与生活","10");
		 lessonNameIdMap.put( "品德与社会","11");
		 lessonNameIdMap.put( "体育","12");
		 lessonNameIdMap.put( "信息","13");
		 lessonNameIdMap.put( "音乐","14");
		 lessonNameIdMap.put( "美术","15");
		 lessonNameIdMap.put( "文综","16");
		 lessonNameIdMap.put( "理综","17");
		 lessonNameIdMap.put( "自习","18");
		 lessonNameIdMap.put( "技术","19");
		 
		 
		 
		 
		 lessonStageMap.put("2", Arrays.asList(primarySchool.split(",")));
		 lessonStageMap.put("3", Arrays.asList(middleSchool.split(",")));
		 lessonStageMap.put("4", Arrays.asList(highSchool.split(",")));
		 
		 gradeStageMap.put("2", "15"); //六年级
		 gradeStageMap.put("3", "18"); //初三
		 gradeStageMap.put("4", "21"); //高三
		 
		 gradeStageNameMap.put("2", "graduatePrimary");
		 gradeStageNameMap.put("3", "graduateJunior");
		 gradeStageNameMap.put("4", "graduateHigh");
	 
		 gradeGroupStageNameMap.put("2", Arrays.asList(gradePrimaryNames.split(",")));
		 gradeGroupStageNameMap.put("3", Arrays.asList(gradeMiddleNames.split(",")));
		 gradeGroupStageNameMap.put("4", Arrays.asList(gradeHighNames.split(",")));
		
		 gradeGroupStageLevelMap.put("一年级","10");
		 gradeGroupStageLevelMap.put("二年级", "11");
		 gradeGroupStageLevelMap.put("三年级", "12");
		 gradeGroupStageLevelMap.put("四年级", "13");
		 gradeGroupStageLevelMap.put("五年级", "14");
		 gradeGroupStageLevelMap.put("六年级", "15");
		 gradeGroupStageLevelMap.put("初一", "16");
		 gradeGroupStageLevelMap.put("初二", "17");
		 gradeGroupStageLevelMap.put("初三", "18");
		 gradeGroupStageLevelMap.put("高一", "19");
		 gradeGroupStageLevelMap.put("高二", "20");
		 gradeGroupStageLevelMap.put("高三", "21");
		 
		 gradeLevelMap.put("2", Arrays.asList(primaryGradeLevels.split(",")));
		 gradeLevelMap.put("3", Arrays.asList(middleGradeLevels.split(",")));
		 gradeLevelMap.put("4", Arrays.asList(highGradeLevels.split(",")));
	 }
}
