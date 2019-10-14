package com.talkweb.common.tools.sort;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @ClassName ChineseSort.java
 * @author liboqi
 * @version 1.0
 * @Description 中文排序
 * @date 2015年12月18日 下午5:45:13
 */
public class ChineseSort {
	/*零 :38646,一:19968,二:20108,三:19977,四:22235,五:20116,六:20845,七:19971,八:20843,九:20061,十:21313
	    	       壹:22777,贰:36144,叁:21441,肆:32902,伍:20237,陆:38470,柒:26578,捌:25420,玖:29590,拾:25342*/
	private static final Integer[] numberByXx = {38646,19968,20108,19977,22235,20116,20845,19971,20843,20061,21313};
	private static final Integer[] numberByDx = {22777,36144,21441,32902,20237,38470,26578,25420,29590,25342};
	private static final char[] charByNumber = {38646,19968,20108,19977,22235,20116,20845,19971,20843,20061,21313,
		22777,36144,21441,32902,20237,38470,26578,25420,29590,25342};
	
	public static void sortByJx(List<String> list){
		Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(list, com);
		joinNumber(list);
	}
	
	public static void sortBySx(List<String> list){
		Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(list, com);
		joinNumber(list);
		Collections.reverse(list);
	}
	
	public static void joinNumber(List<String> list){
		List<Integer> charNumberListByXx = Arrays.asList(numberByXx);
		List<Integer> charNumberListByDx = Arrays.asList(numberByDx);
		Map<String,TreeMap<Integer, ArrayList<String>>> groupMap = new HashMap<String,TreeMap<Integer, ArrayList<String>>>();
		for (String string : list) {
			String value = "";
			String value2 = "";
			boolean isflag = true;
			String[] split = string.split("");
			for (String string2 : split) {
				boolean flag = true;
				if(StringUtils.isNotBlank(string2)){
					int charNum = (int)string2.charAt(string2.length() - 1);
					if(charNumberListByXx.contains(charNum)){
						for (int i = 0; i < charNumberListByXx.size(); i++) {
							Integer integer = charNumberListByXx.get(i);
							if(integer == charNum){
								value += i;
								flag = false;
								isflag = false;
								break;
							}
						}
					}
					if(charNumberListByDx.contains(charNum)){
						for (int i = 0; i < charNumberListByDx.size(); i++) {
							Integer integer = charNumberListByDx.get(i);
							if(integer == charNum){
								value += (i + 1);
								flag = false;
								isflag = false;
								break;
							}
						}
					}
					if(flag){
						value2 += string2;
					}
				}
			}
			if(StringUtils.isNotBlank(value2) && !isflag && StringUtils.isNotBlank(value)){
				Integer value3 = Integer.valueOf(value);
				if(groupMap.containsKey(value2)){
					TreeMap<Integer, ArrayList<String>> treeMap2 = groupMap.get(value2);
					if(treeMap2.containsKey(value3)){
						treeMap2.get(value3).add(string);
					}else {
						treeMap2.put(value3,new ArrayList<String>());
						treeMap2.get(value3).add(string);
					}
				}else{
					groupMap.put(value2, new TreeMap<Integer, ArrayList<String>>());
					TreeMap<Integer, ArrayList<String>> treeMap2 = groupMap.get(value2);
					if(treeMap2.containsKey(value3)){
						treeMap2.get(value3).add(string);
					}else {
						treeMap2.put(value3,new ArrayList<String>());
						treeMap2.get(value3).add(string);
					}
				}
			}
		}
		int indexOrder = 0;
		for (int i = 0; i < list.size(); i++) {
			String string = list.get(i);
			if(StringUtils.containsAny(string, charByNumber)){
				String string2 = StringUtils.deleteWhitespace(Arrays.toString(charByNumber).replace("[", "").replace("]", "")).trim();
				String[] split = string2.split(",");
				string = StringUtils.replaceEach(string,split , new String[]{"","","","","","","","","","","","","","","","","","","","",""});	
				if(groupMap.containsKey(string)){
					TreeMap<Integer, ArrayList<String>> treeMap = groupMap.get(string);
					if(!treeMap.isEmpty()){
						Entry<Integer, ArrayList<String>> firstEntry = treeMap.firstEntry();
						Integer key = firstEntry.getKey();
						ArrayList<String> value = firstEntry.getValue();
						if(CollectionUtils.isNotEmpty(value)){
							indexOrder = i;
							for (int index = 0;index < value.size();index++) {
								if(value.size() > 1){
									indexOrder = i + index;
								}
								String string3 = value.get(index);
								list.remove(indexOrder);
								list.add(indexOrder, string3);
							}
							i = i + value.size() - 1;
							treeMap.remove(key);
						}
					}
				}
			}
		}
	}
}
