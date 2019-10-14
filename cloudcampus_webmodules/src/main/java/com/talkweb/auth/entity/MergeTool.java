package com.talkweb.auth.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections.ListUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.BeanTool;

public class MergeTool {
	/**
	 * 合并app权限
	 * @param rs
	 * @param right
	 * @return 
	 */
	public static HashMap<String, HashMap<String, JSONObject>> MergeAppLeftRight(
			HashMap<String, HashMap<String, JSONObject>> left,
			HashMap<String, HashMap<String, JSONObject>> right) {
		// TODO Auto-generated method stub
		HashMap<String, HashMap<String, JSONObject>> rs = null;
		if(left.isEmpty()){
			rs = right;
		}else{
			rs = new HashMap<String, HashMap<String,JSONObject>>();
			List<String> rightList = new ArrayList(right.keySet());
			List<String> leftList = new ArrayList(left.keySet());
			List<String> commonList = ListUtils.retainAll(leftList, rightList);
			rs.putAll(left);
			rs.putAll(right);
			for(String key:commonList){
				rs.put(key, MergeSynjRight(left.get(key),right.get(key)));
			}
			
		}
		
		return rs;
	}

	/**
	 * 合并使用年级权限
	 * @param hashMap
	 * @param hashMap2
	 * @return
	 */
	public static HashMap<String, JSONObject> MergeSynjRight(
			HashMap<String, JSONObject> left,
			HashMap<String, JSONObject> right) {
		// TODO Auto-generated method stub
		HashMap<String, JSONObject> rs = new HashMap<String, JSONObject>();
		List<String> rightList = new ArrayList(right.keySet());
		List<String> leftList = new ArrayList(left.keySet());
		List<String> commonList = ListUtils.retainAll(leftList, rightList);
		rs.putAll(left);
		rs.putAll(right);
		for(String key:commonList){
			JSONObject leftObj = left.get(key);
			JSONObject rightObj = right.get(key);
			HashMap<Long,JSONObject> mergeClassList = MergeClassList((HashMap<Long, JSONObject>) leftObj.get("classList"),(HashMap<Long, JSONObject>) rightObj.get("classList"));
			leftObj.put("classList", mergeClassList);
			rs.put(key, leftObj);
		}
		
	
		return rs;
	}
	/**
	 * 合并班级权限
	 * @param hashMap
	 * @param hashMap2
	 * @return
	 */
	public static HashMap<Long, JSONObject> MergeClassList(
			HashMap<Long, JSONObject> left,
			HashMap<Long, JSONObject> right) {
		// TODO Auto-generated method stub
		HashMap<Long, JSONObject> rs = new HashMap<Long, JSONObject>();
		List<Long> rightList = new ArrayList(right.keySet());
		List<Long> leftList = new ArrayList(left.keySet());
		List<Long> commonList = ListUtils.retainAll(leftList, rightList);
		rs.putAll(left);
		rs.putAll(right);
		for(Long key:commonList){
			JSONObject leftObj = left.get(key);
			JSONObject rightObj = right.get(key);
			HashMap<Long,JSONObject> leftKmList = (HashMap<Long, JSONObject>) leftObj.get("kmList");
			HashMap<Long,JSONObject> rightKmList = (HashMap<Long, JSONObject>) rightObj.get("kmList");
			leftObj.put("kmList", MergeKmList(leftKmList,rightKmList));
			rs.put(key, leftObj);
		}
	
		return rs;
	}

	/**
	 * 合并科目权限
	 * @param kmList
	 * @param rightKmList
	 * @return
	 */
	public static HashMap<Long, JSONObject> MergeKmList(HashMap<Long, JSONObject> left,
			HashMap<Long, JSONObject> right) {
		left.putAll(right);
		for(Entry<Long, JSONObject> entry:left.entrySet()){
			entry.setValue(BeanTool.castBeanToFirstLowerKey(entry.getValue()));
		}
		// TODO Auto-generated method stub
		return left;
	}
}
