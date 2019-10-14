package com.talkweb.wishFilling.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.LessonInfo;

public class Util {
	public static String getPycc(int currentLevel){
		if(currentLevel>=19 && currentLevel<=21){
			return "4";//高中
		}else{
			return "3";
		}
	}
	public static boolean isToJunior(String areaCode,String schoolId){
		//获取该区域码的省份区域码
		String provinceCode = areaCode.substring(0, 2)+"0000" ;
		if("110000".equals(provinceCode)|| "44898".equals(schoolId)){
			return true;
		}
		return false;
	}
	/**
	 * 按字段 排序科目
	 * @param List<JSONObject>  obj 根据obj中的subjectId排序
	 * @return  List<JSONObject>
	 */
	public static List<JSONObject> lessonSort(List<JSONObject> lList,String key){
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		if(lList==null){
			return returnList;
		}
	
		for(int i=0;i<lList.size();i++){
			 JSONObject obj = lList.get(i);
			 Long subjectId = obj.getLong(key);
			for(int j=i+1;j<lList.size();j++){
				 JSONObject obj1 = lList.get(j);
				 Long subjectId1 = obj1.getLong(key);
				 if(subjectId1<subjectId){
					 lList.add(i, obj1);
					 lList.add(j,obj);
					 lList.remove(i+1);
					 lList.remove(j+1);
					 obj = lList.get(i);
					 subjectId = obj.getLong(key);
					 obj1 = lList.get(j);
				 }
			}
		}
		return lList;
	}

  
	/**
	 * 排序 List<LessonInfo> lList
	 * List<JSONObject>
	 */
public static List<JSONObject> lessonNameSort(List<LessonInfo> lList){
	  List<JSONObject> lessonList= new ArrayList<JSONObject>();
	  for(LessonInfo l:lList){
		  JSONObject obj = new JSONObject();
		  obj.put("subjectId", l.getId());
		  obj.put("subjectName", l.getName());
		  obj.put("subjectType", l.getType());
		  lessonList.add(obj);
	  }
	  try {
		  lessonList=lessonSort(lessonList,"subjectId");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return lessonList;
}
  
  public static void main(String[] args) {
	  List<JSONObject> subList = new ArrayList<>();
	  JSONObject obj = new JSONObject();
	  obj.put("subjectId",1);
	  obj.put("subjectName","A");
	  subList.add(obj);
	   obj = new JSONObject();
	  obj.put("subjectId",3);
	  obj.put("subjectName","C");
	  subList.add(obj);
	  obj = new JSONObject();
	  obj.put("subjectId",2);
	  obj.put("subjectName","B");
	  subList.add(obj);
	  subList = lessonSort(subList,"subjectId");
	  System.out.println(subList);

  }
  
}
