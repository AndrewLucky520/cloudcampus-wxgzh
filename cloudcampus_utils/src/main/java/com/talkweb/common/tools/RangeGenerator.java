package com.talkweb.common.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RangeGenerator {

	/**
	 * 安装百分比分组总分间隔,此方法适用于单科
	 * @param mf 总分
	 * @param iv 间隔 0 为系统默认, 其他数字为自定义间隔
	 * @return
	 */
	public static List<Map<String,Object>> getMaxAndMinByPercent(int mf,int iv){
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		double interval = 0;
		//系统默认间隔：总分的10%
		//if(iv==0){
			//interval = (int) (mf * 0.1);
		//用户自定义间隔	
		//}else{
			//interval = iv;
		//}
		double percent = iv/100.0;
		interval = mf * percent;
		int temp = mf;	
		int end = (int) (mf * 0.4);
		
		while(temp > end){
			Map<String,Object> uads = null;
			if(temp - end < interval){
				uads = new HashMap<String, Object>();
				uads.put("upScore", temp);
				uads.put("downScore", end);
				list.add(uads);
				break;
			}else{
				uads = new HashMap<String, Object>();
				uads.put("upScore", temp);
				temp = (int) (temp - interval);
				uads.put("downScore", temp);
			}
			
			list.add(uads);
		}
		   
		Map<String,Object> uads = new HashMap<String, Object>();
		uads.put("upScore", end);
		uads.put("downScore",0);
		list.add(uads);
		return list;
	}
	
	
	/**
	 * 安装百分比分组总分间隔,此方法适用于单科
	 * @param mf 总分
	 * @param iv 间隔 0 为系统默认, 其他数字为自定义间隔
	 * @return
	 */
	public static List<Map<String,Object>> getMaxAndMinByNumber(int mf,int iv){
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		int interval = 0;
		//系统默认间隔：总分的10%
		//if(iv==0){
			//interval = (int) (mf * 0.1);
		//用户自定义间隔	
		//}else{
			//interval = iv;
		//}
		interval = iv;
		int temp = mf;	
		int end = (int) (mf * 0.4);
		
		while(temp > end){
			Map<String,Object> uads = null;
			if(temp - end < interval){
				uads = new HashMap<String, Object>();
				uads.put("upScore", temp);
				uads.put("downScore", end);
				list.add(uads);
				break;
			}else{
				uads = new HashMap<String, Object>();
				uads.put("upScore", temp);
				temp = temp - interval;
				uads.put("downScore", temp);
			}
			
			list.add(uads);
		}
		   
		Map<String,Object> uads = new HashMap<String, Object>();
		uads.put("upScore", end);
		uads.put("downScore",0);
		list.add(uads);
		return list;
	}
	
	
	
	/**
	 * 用户自定义间隔
	 * @param str 间隔字符串
	 * @return
	 */
	public static List<Map<String,Object>> getUserDefMaxAndMin(String str){
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		String[] strNum = str.split(",");	
		int[] num = new int[strNum.length];
		for (int i = 0; i < strNum.length; i++) {
			num[i] = Integer.valueOf(strNum[i]);	
		}
		
		
		for (int i = 0; i < num.length-1; i++) {
			Map<String,Object> uads = new HashMap<String,Object>();
			uads.put("upScore", num[i]);
			uads.put("downScore", num[i+1]);
			list.add(uads);
		}
			
		return list;
	}
	
	
	
	
	public static void main(String[] args) {
		
		List<Map<String, Object>> maxAndMin = RangeGenerator.getMaxAndMinByPercent(750, 10);
		System.out.println(maxAndMin);
	}
	
	
}

