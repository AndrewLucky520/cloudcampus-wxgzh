package com.talkweb.common.tools;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class SortUtil {

	
	/**
     * 	根据成绩列表获取排序后的列表 并存入排名字段
     * @param scoreList 成绩列表
     * @param orderKey	排序字段
     * @param orderType	排序类型：asc/desc
     * @param njbj		是年级排名还是班级排名
     * @param km		是总分还是科目
     * @return
     */
	public static List<JSONObject> sortListByTime(List<JSONObject> scoreList,
			String orderKey, String orderType,String njbj,String km) {
		// TODO Auto-generated method stub
		if (orderType.equalsIgnoreCase("asc")) {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					JSONObject l = scoreList.get(j);
					JSONObject r = scoreList.get(j + 1);
					long fl = 0,rl=0;
					if(l.containsKey(orderKey)){
						fl = l.getLongValue(orderKey);
					}
					if(r.containsKey(orderKey)){
						rl = r.getLongValue(orderKey);
					}
					if (rl < fl) {
						scoreList.set(j, r);
						scoreList.set(j + 1, l);
					}
				}
			}
		} else {
			for (int i = 0; i < scoreList.size() - 1; i++) {
				for (int j = 0; j < scoreList.size() - 1 - i; j++) {
					JSONObject l = scoreList.get(j);
					JSONObject r = scoreList.get(j + 1);
					long fl = 0,rl=0;
					if(l.containsKey(orderKey)){
						fl = l.getLongValue(orderKey);
					}
					if(r.containsKey(orderKey)){
						rl = r.getLongValue(orderKey);
					}
					if (rl > fl) {
						scoreList.set(j, r);
						scoreList.set(j + 1, l);
					}

				}
			}
		}

		if( km.equals("dfl")){
			// 放入成绩排名
			int pm = 1;
			boolean never = true;
			for (int i = 0; i < scoreList.size(); i++) {
				if(scoreList.get(i).containsKey(orderKey)){
					never = false;
				}
				if(scoreList.get(i).containsKey(orderKey)&&i!=0 ){
					long last = scoreList.get(i-1).getLongValue(orderKey);
					long now = scoreList.get(i).getLongValue(orderKey);
					if(now!=last&&!never){
						pm++;
					}
				}
				scoreList.get(i).put(njbj+km+"pm", pm);
			}
		}else{

			// 放入成绩排名
			for (int i = 0; i < scoreList.size(); i++) {
				if(i!=0){
					long last = scoreList.get(i-1).getLongValue(orderKey);
					long now = scoreList.get(i).getLongValue(orderKey);
					int lastPm = scoreList.get(i-1).getIntValue(njbj+km+"pm");
					if(now==last){
						scoreList.get(i).put(njbj+km+"pm", lastPm);
					}else{
						scoreList.get(i).put(njbj+km+"pm", i+1);
					}
				}else{
					scoreList.get(i).put(njbj+km+"pm", 1);
				}
			}
		}
		return scoreList;
	}
	
	/**
	 * 
	 * @param data
	 * @param order {0:升序,1:降序}
	 * @return
	 */
	public static List<JSONObject> sortJsonListByTclassName(List<JSONObject> data, int order, List<String> tClassNames,String sortColumn){
		if(null!=data){
			Collections.sort(tClassNames,Collator.getInstance(java.util.Locale.CHINA));
			List<JSONObject> sortedData = new ArrayList<JSONObject>();
			for(String tClassName : tClassNames){
				for(JSONObject singleData : data){
					if(tClassName.equals(singleData.get(sortColumn)) && !sortedData.contains(singleData)){						
						sortedData.add(BeanTool.castBeanToFirstLowerKey(singleData));
						break;
					}
				}
			}
			
			return sortedData;	
		}
		return null;
	}
}
