package com.talkweb.committee.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * 排序工具类
 * @author zhanghuihui
 *
 */

public class SortUtil {
	public static void sortByStudentImportRule(JSONArray arr,final Map<String,Integer> rule) {
		Collections.sort( arr, new Comparator<Object>() {
              @Override
              public int compare(Object a, Object b) {
            	  String aTitle = ((JSONObject)a).getString("title");
            	  String bTitle =  ((JSONObject)b).getString("title");
            	  Integer aInt = rule.get(aTitle);
            	  Integer bInt = rule.get(bTitle);
                  int flag = aInt.compareTo(bInt);
                  return flag;
              }
          });
	}
	//中文排序
	public static void sortNameJSONList(List<JSONObject> list,final String key) {
		Collections.sort(list,new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return ChinaInitial.getPinYinHeadChar(o1.getString(key)).compareTo(ChinaInitial.getPinYinHeadChar(o2.getString(key)));
			}
			
		});
	}
	public static void sortNameNormalJSONList(List<JSONObject> list,final String key) {
		Collections.sort(list,new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return (o1.getString(key)).compareTo(o2.getString(key));
			}
		});
	}
	public static void sortWsgNormalJSONList(JSONArray arr, final String key) {
		Collections.sort( arr, new Comparator<Object>() {
            @Override
            public int compare(Object a, Object b) {
          	  int aTitle = ((JSONObject)a).getInteger(key);
            	int bTitle =  ((JSONObject)b).getInteger(key);
            	 if (aTitle > bTitle)
                     return 1;
            	 if (aTitle < bTitle)
                     return -1;
                 return 0;
            }
        });
	}
	
}
