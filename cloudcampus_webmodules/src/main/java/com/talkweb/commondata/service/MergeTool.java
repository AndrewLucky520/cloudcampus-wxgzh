package com.talkweb.commondata.service;

import java.util.HashMap;
import java.util.Iterator;

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
		HashMap<String, HashMap<String, JSONObject>> rs = new HashMap<String, HashMap<String,JSONObject>>();
		if(left.isEmpty()){
			rs = right;
		}else{
			
			for(Iterator<String> it = right.keySet().iterator();it.hasNext();){
				String key = it.next();
				HashMap<String, JSONObject> tmp = new HashMap<String, JSONObject>();
				if(key!=null&&left.containsKey(key)){
					tmp = MergeSynjRight(left.get(key),right.get(key));
				}else{
					tmp =  MergeSynjRight(new HashMap<String, JSONObject>(),right.get(key));
				}
				rs.put(key, tmp);
			}
			
			
			for(Iterator<String> it = left.keySet().iterator();it.hasNext();){
				String key = it.next();
				HashMap<String, JSONObject> tmp = new HashMap<String, JSONObject>();
				if(key!=null&&right.containsKey(key)){
					tmp = MergeSynjRight(right.get(key),left.get(key));
				}else{
					tmp =  MergeSynjRight(new HashMap<String, JSONObject>(),left.get(key));
				}
				rs.put(key, tmp);
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
		if(right!=null){
			
			for(Iterator<String> it = right.keySet().iterator();it.hasNext();){
				String key = it.next();
				JSONObject synjObjTmp = new JSONObject();
				JSONObject synjObj = left.get(key);
				JSONObject rightSynjObj = right.get(key);
				if(left.containsKey(key)){
					
					synjObjTmp.putAll(synjObj);
					HashMap<Long,JSONObject> classList = (HashMap<Long, JSONObject>) synjObj.get("classList");
					HashMap<Long,JSONObject> rightClassList = (HashMap<Long, JSONObject>) rightSynjObj.get("classList");
					
					HashMap<Long,JSONObject> mergeClassList = MergeClassList(classList,rightClassList);
					synjObjTmp.put("classList", mergeClassList);
					
				}else{
					synjObjTmp.putAll(rightSynjObj);
					HashMap<Long,JSONObject> rightClassList = (HashMap<Long, JSONObject>) rightSynjObj.get("classList");
					
					HashMap<Long,JSONObject> mergeClassList = new HashMap<Long, JSONObject>();
					if(rightClassList!=null){
						mergeClassList =MergeClassList(new HashMap<Long, JSONObject>(),rightClassList);
					}
					synjObjTmp.put("classList", mergeClassList);
					
				}
				rs.put(key,synjObjTmp);
			}
		}
		if(left!=null){
			
			for(Iterator<String> it = left.keySet().iterator();it.hasNext();){
				String key = it.next();
				JSONObject synjObjTmp = new JSONObject();
				JSONObject synjObj = right.get(key);
				JSONObject rightSynjObj = left.get(key);
				if(right.containsKey(key)){
					
					synjObjTmp.putAll(synjObj);
					HashMap<Long,JSONObject> classList = (HashMap<Long, JSONObject>) synjObj.get("classList");
					HashMap<Long,JSONObject> rightClassList = (HashMap<Long, JSONObject>) rightSynjObj.get("classList");
					
					HashMap<Long,JSONObject> mergeClassList = MergeClassList(classList,rightClassList);
					synjObjTmp.put("classList", mergeClassList);
					
				}else{
					synjObjTmp.putAll(rightSynjObj);
					HashMap<Long,JSONObject> rightClassList = (HashMap<Long, JSONObject>) rightSynjObj.get("classList");
					
					HashMap<Long,JSONObject> mergeClassList = new HashMap<Long, JSONObject>();
					if(rightClassList!=null){
						mergeClassList =MergeClassList(new HashMap<Long, JSONObject>(),rightClassList);
					}
					synjObjTmp.put("classList", mergeClassList);
					
				}
				rs.put(key,synjObjTmp);
			}
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
		
		for(Iterator<Long> it=right.keySet().iterator();it.hasNext();){
			long key = it.next();
			JSONObject classtmp  = new JSONObject();
			if(left.containsKey(key)){
				JSONObject classObj = left.get(key);
				classtmp.putAll(classObj);
				
				JSONObject rightClassObj = right.get(key);
				HashMap<Long,JSONObject> kmList = (HashMap<Long, JSONObject>) classObj.get("kmList");
				HashMap<Long,JSONObject> rightKmList = (HashMap<Long, JSONObject>) rightClassObj.get("kmList");
				
				HashMap<Long,JSONObject> mergeKmList = MergeKmList(kmList,rightKmList);
				
				classtmp.put("kmList", mergeKmList);
			}else{
				JSONObject classObj =  right.get(key);
				classtmp.putAll(classObj);
				HashMap<Long,JSONObject> beCopyKmList = (HashMap<Long, JSONObject>) classObj.get("kmList");
				HashMap<Long,JSONObject> mergeKmList = MergeKmList(beCopyKmList,new HashMap<Long,JSONObject>());
				
				classtmp.put("kmList",mergeKmList);
			}
			rs.put(key, classtmp);
		}
		

		for(Iterator<Long> it=left.keySet().iterator();it.hasNext();){
			long key = it.next();
			JSONObject classtmp  = new JSONObject();
			if(right.containsKey(key)){
				JSONObject classObj = right.get(key);
				classtmp.putAll(classObj);
				
				JSONObject rightClassObj = left.get(key);
				HashMap<Long,JSONObject> kmList = (HashMap<Long, JSONObject>) classObj.get("kmList");
				HashMap<Long,JSONObject> rightKmList = (HashMap<Long, JSONObject>) rightClassObj.get("kmList");
				
				HashMap<Long,JSONObject> mergeKmList = MergeKmList(kmList,rightKmList);
				
				classtmp.put("kmList", mergeKmList);
			}else{
				JSONObject classObj =  left.get(key);
				classtmp.putAll(classObj);
				HashMap<Long,JSONObject> beCopyKmList = (HashMap<Long, JSONObject>) classObj.get("kmList");
				HashMap<Long,JSONObject> mergeKmList = MergeKmList(beCopyKmList,new HashMap<Long,JSONObject>());
				
				classtmp.put("kmList",mergeKmList);
			}
			rs.put(key, classtmp);
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
		HashMap<Long, JSONObject> rs = new HashMap<Long, JSONObject>();
		for(Iterator<Long> it=left.keySet().iterator();it.hasNext();){
			long key = it.next();
			JSONObject tmp =  new JSONObject();
			if(!left.containsKey(key) ){
				if(right.get(key)!=null){
					tmp = BeanTool.castBeanToFirstLowerKey( right.get(key));
				}
			}else{
				tmp = BeanTool.castBeanToFirstLowerKey( left.get(key));
			}
			rs.put(key,tmp);
		}
		
		for(Iterator<Long> it=right.keySet().iterator();it.hasNext();){
			long key = it.next();
			JSONObject tmp =  new JSONObject();
			if(!right.containsKey(key) ){
				if(left.get(key)!=null){
					tmp = BeanTool.castBeanToFirstLowerKey( right.get(key));
				}
			}else{
				tmp = BeanTool.castBeanToFirstLowerKey( right.get(key));
			}
			rs.put(key,tmp);
		}
		// TODO Auto-generated method stub
		return rs;
	}
}
