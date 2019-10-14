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
 * @ClassName RomeSort.java
 * @author liboqi
 * @version 1.0
 * @Description 罗马数字排序
 * @date 2015年12月18日 下午5:49:16
 */

public class RomeSort {
	
	private static final String[] romeArr = {"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ"
		,"Ⅷ","Ⅸ","Ⅹ","Ⅺ","Ⅻ","Ⅼ","Ⅽ","Ⅾ","Ⅿ"};
	
	public static List<String> sortByJx(List<String> list) throws Exception{
		List<String> returnList = new ArrayList<String>();
		TreeMap<Integer, ArrayList<String>> treeMap = romeConvert(list);
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
	
	public static List<String> sortBySx(List<String> list) throws Exception{
		List<String> returnList = new ArrayList<String>();
		TreeMap<Integer, ArrayList<String>> treeMap = romeConvert(list);
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

	public static TreeMap<Integer, ArrayList<String>> romeConvert(List<String> parse) throws Exception{
		TreeMap<Integer, ArrayList<String>> treeMap = new TreeMap<Integer, ArrayList<String>>();
		for (String jsonObject : parse) {
			String str = "";
			boolean flag = false;
			if(StringUtils.isNotBlank(jsonObject)){
				String[] split = jsonObject.split("");
				RomeConvert romeConvert = null;
				for (int i = 0; i < split.length; i++) {
					String string = split[i];
					if(!"".equals(split[i])){
						int charAt = string.charAt(string.length() - 1);
						if(charAt >= 8544 && charAt <= 8576){
							flag = true;
							str += string;
						}
					}
				}
				if(flag){
					romeConvert = new RomeConvert(str);
					if(StringUtils.isNotBlank(romeConvert.getNum()+"")){
						if(treeMap.containsKey(romeConvert.getNum())){
							treeMap.get(romeConvert.getNum()).add(jsonObject);
						}else{
							treeMap.put(romeConvert.getNum(), new ArrayList<String>());
							treeMap.get(romeConvert.getNum()).add(jsonObject);
						}
					}
				}
			}
		}
		return treeMap;
	}
	
	public static boolean isRome(String str){
		return StringUtils.startsWithAny(str, romeArr);
	}
}
