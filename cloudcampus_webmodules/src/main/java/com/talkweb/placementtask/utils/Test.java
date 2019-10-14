package com.talkweb.placementtask.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {
	
	public static void main(String args[]){
		Permutation("abc");
	}
    public static ArrayList<String> Permutation(String str) {
        ArrayList<String> res = new ArrayList<String>();
        if(str == null || str.length() <= 0)
            return res;
        Set<List<Integer>> set = new HashSet<List<Integer>>(); //结果去重
        List<Integer> oriData = new ArrayList<Integer>();
        oriData.add(0);oriData.add(1); oriData.add(2);
        dfs(set, oriData, 0);
        //res.addAll(set);
        //Collections.sort(res);
        return res;
    }

    public static <T> void dfs(Set<List<T>> set, List<T>str, int k){
        if(k == str.size()){  //得到结果
            List<T> ls = new ArrayList<T>();
            ls.addAll(str);
        	set.add(ls);
            return ;
        }
        for(int i = 0; i < str.size(); i ++){
            swap(i, k, str);
            dfs(set, str, k + 1);
            swap(i, k, str);  //回溯
        }
    }

    public static <T> void swap(int i, int j, List<T>str){
        if(i != j){
            T temp = str.get(i);
            str.set(i, str.get(j));
            str.set(j, temp);
        }
    }
}
