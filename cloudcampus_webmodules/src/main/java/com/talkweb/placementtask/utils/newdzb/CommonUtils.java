package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONObject;

public class CommonUtils {

	/**
	 * 数组求和
	 * @param datas
	 * @return
	 */
	public static int sum(Integer[] datas){
		int result = 0;
		for(Integer data:datas){
			result += data;
		}
		return result;
	}
	
	/**
	 * 集合求和
	 */
	public static int sum(Collection<Integer> datas){
		int result = 0;
		for(Integer data:datas){
			result += data;
		}
		return result;
	}
	
	/**
	 * 获取所有可能性
	 */
	public static List<Integer[]> getAllComb(List<Integer> selectNums,int count){
		List<Integer[]> allCombList =null;
		for(int i=0;i<count;i++){
			List<Integer[]> allCombListTemp = new ArrayList((int) Math.pow(selectNums.get(i), i+1));
			for(int j=0;j<selectNums.get(i);j++){
				if(allCombList==null){
					Integer[] one = new Integer[i+1];
					one[i]=j;
					allCombListTemp.add(one);
				}else{
					for(Integer[] oldOne :allCombList){
						Integer[] one =  Arrays.copyOf(oldOne, i+1);
						one[i]=j;
						allCombListTemp.add(one);
					}
				}
			}
			allCombList = allCombListTemp;
		}
		
		return allCombList;
	}
	
	/**
	 * 获取数组元素所有排列
	 * @param datas
	 * @return
	 */
	public static Set<List<Integer>> getPermutation(List<Integer> datas){
		Set<Integer> dataSet = new TreeSet(datas);
		Set<List<Integer>> results = new TreeSet(new Comparator<List<Integer>>(){
			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				// TODO Auto-generated method stub
				return JSONObject.toJSONString(o1).compareTo(JSONObject.toJSONString(o2));
			}
			
		});
		if(dataSet.size() == 1){
			List<Integer> result = new ArrayList(datas);
			results.add(result);
		}else{
			for(int data:dataSet){
				List<Integer> dataCopy = new ArrayList(datas);
				dataCopy.remove(datas.indexOf(data));
				Set<List<Integer>>  tempResults = getPermutation(dataCopy);
				for(List<Integer> tempResult:tempResults){
					List<Integer> result = new ArrayList();
					result.add(data);
					result.addAll(tempResult);
					results.add(result);
				}
			}
		}
		return results;
	}

	
	/**
	 * 数据平分
	 * @param divisor
	 * @param remainer
	 * @param count
	 * @return
	 */
	public static Integer[] getRandomData(int divident,int divisor,int count){
		int remainer = divident % divisor ;
		int baseNum = divident / divisor ;
		List<Integer> all = new ArrayList();
		Integer[] result = new Integer[count];
		for(int i=0;i<divisor;i++){
			int one = baseNum;
			if(remainer>0){
				one += 1;
				remainer -= 1;
			}
			all.add(one);
		}
		Collections.shuffle(all);
		all.subList(0, count).toArray(result);
		return result;
	}
}
