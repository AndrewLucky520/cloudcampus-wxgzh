package com.talkweb.commondata.util;

import java.util.HashMap;
import java.util.Map;

public class TeachingRelationShipUtil {
	  public static final Map<Long,String> teachingRelationshipMap = new HashMap<Long,String>();
	  public static final Map<String,Long> teachingRelationshipTwoMap = new HashMap<String,Long>();
	  static{
		  teachingRelationshipMap.put(1L,"lesson_chinese");
		  teachingRelationshipMap.put(2L,"lesson_math");
		  teachingRelationshipMap.put(3L,"lesson_english");
		  teachingRelationshipMap.put(4L,"lesson_politics");
		  teachingRelationshipMap.put(5L,"lesson_history");
		  teachingRelationshipMap.put(6L,"lesson_geography");
		  teachingRelationshipMap.put(7L,"lesson_physic");
		  teachingRelationshipMap.put(8L,"lesson_chem");
		  teachingRelationshipMap.put(9L,"lesson_biology");
		  teachingRelationshipMap.put(10L,"lesson_moralitylife");
		  teachingRelationshipMap.put(12L,"lesson_sport");
		  teachingRelationshipMap.put(13L,"lesson_infomation");
		  teachingRelationshipMap.put(14L,"lesson_music");
		  teachingRelationshipMap.put(15L,"lesson_art");
		  teachingRelationshipMap.put(30L,"lesson_moralitylife");
		  teachingRelationshipMap.put(31L,"lesson_science");  

		  teachingRelationshipTwoMap.put("lesson_chinese",1L);
		  teachingRelationshipTwoMap.put("lesson_math",2L);
		  teachingRelationshipTwoMap.put("lesson_english",3L);
		  teachingRelationshipTwoMap.put("lesson_politics",4L);
		  teachingRelationshipTwoMap.put("lesson_history",5L);
		  teachingRelationshipTwoMap.put("lesson_geography",6L);
		  teachingRelationshipTwoMap.put("lesson_physic",7L);
		  teachingRelationshipTwoMap.put("lesson_chem",8L);
		  teachingRelationshipTwoMap.put("lesson_biology",9L);
		  teachingRelationshipTwoMap.put("lesson_moralitylife",10L);
		  teachingRelationshipTwoMap.put("lesson_sport",12L);
		  teachingRelationshipTwoMap.put("lesson_infomation",13L);
		  teachingRelationshipTwoMap.put("lesson_music",14L);
		  teachingRelationshipTwoMap.put("lesson_art",15L);
		  teachingRelationshipTwoMap.put("lesson_moralitylife",30L);
		  teachingRelationshipTwoMap.put("lesson_science",31L); 
	  }
}
