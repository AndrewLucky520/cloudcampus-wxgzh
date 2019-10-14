package com.talkweb.common.tools.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * @ClassName MetalSort.java
 * @author liboqi
 * @version 1.0
 * @Description 天地排序
 * @date 2015年12月18日 下午5:41:22
 */
public class MetalSort {
	private static final String[] meatlArr = {"甲","乙","丙","丁","戊","己","庚"
			,"辛","壬","癸","子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"};
	public static List<String> sortByJx(String[] split){
		List<String> returnList = new ArrayList<String>();
		TreeMap<Integer, ArrayList<String>> treeMap = metalConvert(split);
		if(!treeMap.isEmpty()){
			Set<Entry<Integer, ArrayList<String>>> entrySet = treeMap.entrySet();
			Iterator<Entry<Integer, ArrayList<String>>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, ArrayList<String>> entry = (Map.Entry<Integer, ArrayList<String>>) iterator.next();
				ArrayList<String> value = entry.getValue();
				for (String string : value) {
					returnList.add(string);
				}
			}
		}
		Collections.reverse(returnList);
		return returnList;
	}
	
	public static List<String> sortBySx(String[] split){
		List<String> returnList = new ArrayList<String>();
		TreeMap<Integer, ArrayList<String>> treeMap = metalConvert(split);
		if(!treeMap.isEmpty()){
			Set<Entry<Integer, ArrayList<String>>> entrySet = treeMap.entrySet();
			Iterator<Entry<Integer, ArrayList<String>>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<Integer, ArrayList<String>> entry = (Map.Entry<Integer, ArrayList<String>>) iterator.next();
				ArrayList<String> value = entry.getValue();
				for (String string : value) {
					returnList.add(string);
				}
			}
		}
		return returnList;
	}
	
	public static TreeMap<Integer, ArrayList<String>> metalConvert(String[] split){
		TreeMap<Integer, ArrayList<String>> treeMap = new TreeMap<Integer, ArrayList<String>>();
		for (String string : split) {
			if(StringUtils.startsWithAny(string, meatlArr)){
				String substring = string.substring(0, 1);
				Integer value = MetalEnum.getValue(substring);
				if(StringUtils.isNotBlank(value+"")){
					if(treeMap.containsKey(value)){
						treeMap.get(value).add(string);
					}else{
						treeMap.put(value, new ArrayList<String>());
						treeMap.get(value).add(string);
					}
				}
			}
		}
		return treeMap;
	}
	
	public static boolean isMetal(String str){
		return StringUtils.startsWithAny(str, meatlArr);
	}
}
