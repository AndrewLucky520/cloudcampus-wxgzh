package com.talkweb.placementtask.utils.div;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.dezy.dto.AdClassResult;
import com.talkweb.placementtask.utils.div.dezy.dto.AdScoreResult;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.CellStack;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.ScoreResult;
import com.talkweb.placementtask.utils.div.dto.SeqCell;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.Subject.UP_OR_DOWN;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.dto.TeachClassResult;

public class Utils {
	
	private static int index = 1;
	
	private static int num = 0;
	
	public static String SEPARATOR_SYMBOL="@";
	
	public static String WISH_SEPARATOR_SYMBOL="#";
	
	
	/**
	 * 467#1
	 * 获取志愿id
	 * @param wishKey
	 * @return
	 */
	public static String  extractWishSubjectGroupId(String wishKey) {
		if(wishKey.indexOf(Utils.SEPARATOR_SYMBOL)!=-1) {
			String tmp = wishKey.split(Utils.SEPARATOR_SYMBOL)[1];
			return tmp.split(Utils.WISH_SEPARATOR_SYMBOL)[0];
		}else {
			return wishKey.split(Utils.WISH_SEPARATOR_SYMBOL)[0];
		}
	}
	
	
	/**
	 * 获取上一级志愿
	 * @param wishKey
	 * @return
	 */
	public static String getParentWishId(String wishKey) {
		int end = wishKey.lastIndexOf(Utils.WISH_SEPARATOR_SYMBOL);
		if(end==-1) end = wishKey.length();
		return wishKey.substring(0, end);
	}
	
	
	public static String generateClassId(String prefix) {
		index++;
		String str = String.valueOf(index);
		int x= 3-str.toCharArray().length;
		if(x>0) {
			for(int i=0;i<x;i++) {
				str="0"+str;
			}
		}
		return prefix+str;
	}
	
	
	public static void printChooseThreeTable2(DivContext divContext, TreeBasedTable<String, String, Cell> chooseThreeTable) {
		
		StringBuffer title = new StringBuffer("                                               ");
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			title.append("  00"+subject.getId());
		}
		System.out.println(title);
		
		for (Entry<String, Map<String, Cell>> rowEntry : chooseThreeTable.rowMap().entrySet()) {
			SubjectGroup wish = divContext.getWishId2SubjectGroupMap().get(rowEntry.getKey().split(SEPARATOR_SYMBOL)[1]);
			StringBuffer str = new StringBuffer(wish.getName()+"["+rowEntry.getKey()+"]"+" ");
			
			for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
				Cell v = rowEntry.getValue().get(subject.getId());
				if(null == v || v.getStudentCount()<=0) {
					str.append("  000");
				}else {
					str.append("  "+fillStr(String.valueOf(v.getStudentCount()), 3));
				}
			}
			System.out.println(str.toString());
		}
	}
	
	public static String fillStr(String str, int num) {
		int x= num-str.toCharArray().length;
		if(x>0) {
			for(int i=0;i<x;i++) {
				str="0"+str;
			}
		}
		return str;
	}
	
	public static String fillStr(int value, int num) {
		String str = String.valueOf(value);
		int x= num-str.toCharArray().length;
		if(x>0) {
			for(int i=0;i<x;i++) {
				str="0"+str;
			}
		}
		return str;
	}
	
	
	public static SubjectGroup getfixTwoSubjectGroup(List<SubjectGroup> fixTwoSubjectGroupList, List<Subject> fixTwoSubjects) {
		
		if(fixTwoSubjects.size()!=2) throw new RuntimeException("非定二科目");
		for (SubjectGroup subjectGroup : fixTwoSubjectGroupList) {
			if(subjectGroup.getSubjects().containsAll(fixTwoSubjects)) {
				return subjectGroup;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 计算定二组合数据
	 * @param wishSubjectGroupList
	 * @param subjectMap
	 * @return
	 */
	public static List<SubjectGroup> calculateFixTwoSubjectGroup(List<SubjectGroup> wishSubjectGroupList, Map<String, Subject> subjectMap) {
		//两两组合情况
		List<SubjectGroup> fixTwoSubjectGroupList = new ArrayList<SubjectGroup>();
		List<LinkedList<Subject>> list = Utils.generateCombine(subjectMap.values().toArray(new Subject[subjectMap.size()]), 0, new LinkedList<Subject>(), 2);
		for (LinkedList<Subject> linkedList : list) {
			//统计该科目组合下人数
			SubjectGroup fixTwoSubjectGroup = new SubjectGroup(linkedList);
			for(SubjectGroup wishSubjectGroup: wishSubjectGroupList) {
				
				//计算总人数
				if(wishSubjectGroup.contains(fixTwoSubjectGroup.getSubjects())) {
					fixTwoSubjectGroup.setStudentCount(fixTwoSubjectGroup.getStudentCount()+wishSubjectGroup.getStudentCount());
				}
			}
			
			fixTwoSubjectGroupList.add(fixTwoSubjectGroup);
			
			BigDecimal total = new BigDecimal(0);
			for (Subject subject : fixTwoSubjectGroup.getSubjects()) {
				total = total.add(subject.getAvgClassSize());
			}
			
			//fixTwoSubjectGroup.setAvgClassSize(total.divide(new BigDecimal(fixTwoSubjectGroup.getSubjects().size()), 0, BigDecimal.ROUND_DOWN).intValue());
			int downCount = 0;
			for (Subject adClassCell : fixTwoSubjectGroup.getSubjects()) {
				if(UP_OR_DOWN.DOWN.equals(adClassCell.getUpOrDown())) {
					downCount++;
				}
			}
			
			//两个科目班级数都是向下取整计算的
			if(downCount==fixTwoSubjectGroup.getSubjects().size()) {
				fixTwoSubjectGroup.setAvgClassSize(total.divide(new BigDecimal(fixTwoSubjectGroup.getSubjects().size()), 0, BigDecimal.ROUND_UP).intValue());
			}else if(downCount==0) { //两个科目班级数都是向上取整
				fixTwoSubjectGroup.setAvgClassSize(total.divide(new BigDecimal(fixTwoSubjectGroup.getSubjects().size()), 0, BigDecimal.ROUND_DOWN).intValue());
			}else { //一个向上一个向下四舍五入
				fixTwoSubjectGroup.setAvgClassSize(total.divide(new BigDecimal(fixTwoSubjectGroup.getSubjects().size()), 0, BigDecimal.ROUND_HALF_UP).intValue());
			}
			System.out.println(JSONObject.toJSONString(fixTwoSubjectGroup));
		}
		
		return fixTwoSubjectGroupList;
	}
	
	
	/**
	 * 计算组合
	 * @param datas       元素
	 * @param step        当前选择步数
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static <T> List<LinkedList<T>> generateCombine(T[] datas, int step, LinkedList<T> selectData, int selectCount) {
		
		if(selectData.size() == selectCount) {
			//System.out.println(JSONObject.toJSONString(selectData));
			LinkedList<T> okSelectData = new LinkedList<T>(selectData);
			List<LinkedList<T>> result = new ArrayList<LinkedList<T>>();
			result.add(okSelectData);
			return result;
		}
		
		if(step>= datas.length) {
			return null;
		}
		
		//选择当前元素
		try {
			selectData.add(datas[step]);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<LinkedList<T>> result = new ArrayList<LinkedList<T>>();
		
		List<LinkedList<T>> clist1 = generateCombine(datas, step+1, selectData, selectCount);
		
		selectData.remove(datas[step]);
		
		//不选择当前元素
		List<LinkedList<T>> clist2 = generateCombine(datas, step+1, selectData, selectCount);
		
		if(null != clist1 && clist1.size()>0) result.addAll(clist1);
		
		if(null != clist2 && clist2.size()>0) result.addAll(clist2);
		
		return result;
	}
	
	
	/**
	 * 计算组合
	 * @param <T>
	 * @param datas       元素
	 * @param step        当前选择步数
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static <T> void combine(T[] datas, int step, LinkedList<T> selectData, int selectCount) {
		
		if(selectData.size() == selectCount) {
			System.out.println(JSONObject.toJSONString(selectData));
			return;
		}
		
		if(step>=datas.length) {
			return;
		}
		
		//选择当前元素
		try {
			selectData.add(datas[step]);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		combine(datas, step+1, selectData, selectCount);;
		selectData.remove(datas[step]);
		
		//不选择当前元素
		combine(datas, step+1, selectData, selectCount);
	}
	
	public static void main(String[] args) {
		
		String str="";
		
		LinkedList<Cell> otherCells = new LinkedList<>();
		otherCells.add(new Cell("654", "56", 1));
		otherCells.add(new Cell("658", "56", 11));

		LinkedList<Cell> defaultSelect = new LinkedList<>();
		defaultSelect.add(new Cell("654", "56", 57));
		defaultSelect.add(new Cell("6522", "56", 19));
		
		List<CellStack> cellStackList = Utils.generateSumCombineArrange(otherCells.toArray(new Cell[otherCells.size()]), defaultSelect, 52, new TimeoutWatch(10L));

		System.out.println(cellStackList.size());
	}
	
	
	public static void mainkkk(String[] args) {
		
		String str = "[{\"columnKey\":\"6\",\"rowKey\":\"BJ004@789\",\"studentCount\":62},{\"columnKey\":\"6\",\"rowKey\":\"BJ005@578\",\"studentCount\":7},{\"columnKey\":\"6\",\"rowKey\":\"BJ005@579\",\"studentCount\":12},{\"columnKey\":\"6\",\"rowKey\":\"BJ009@478\",\"studentCount\":13},{\"columnKey\":\"6\",\"rowKey\":\"BJ009@489\",\"studentCount\":9}]";
		
		List<Cell> cells = JSONArray.parseArray(str, Cell.class);
		
		List<CellStack> list = Utils.generateSumCombineArrange(cells.toArray(new Cell[cells.size()]), new LinkedList<Cell>(), 58, new TimeoutWatch(10L));
		
		//List<CellStack> list = generateSumCombine(cells.toArray(new Cell[cells.size()]), 0, new LinkedList<Cell>(), 58);
		
		System.out.println(list.size());
	}
	
	public static void main223(String[] args) {
		
		//Integer[] sz = new Integer[] {8,17,9,7,25,23,20,12};
		Integer[] sz = new Integer[] {17,12,21};
		String str = "[{\"adClassId\":\"BJ003\",\"studentCount\":14,\"students\":[{\"id\":\"100003972413\",\"name\":\"100003972413\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972597\",\"name\":\"100003972597\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972509\",\"name\":\"100003972509\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003973215\",\"name\":\"100003973215\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972769\",\"name\":\"100003972769\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003973223\",\"name\":\"100003973223\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972605\",\"name\":\"100003972605\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003973337\",\"name\":\"100003973337\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972719\",\"name\":\"100003972719\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003973387\",\"name\":\"100003973387\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003973019\",\"name\":\"100003973019\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972517\",\"name\":\"100003972517\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972997\",\"name\":\"100003972997\",\"wishId\":\"459\",\"wishName\":\"政史生\"},{\"id\":\"100003972323\",\"name\":\"100003972323\",\"wishId\":\"459\",\"wishName\":\"政史生\"}],\"subjectId\":8,\"wishId\":\"459\"},{\"adClassId\":\"BJ003\",\"studentCount\":18,\"students\":[{\"id\":\"100003972773\",\"name\":\"100003972773\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972697\",\"name\":\"100003972697\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973427\",\"name\":\"100003973427\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972425\",\"name\":\"100003972425\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972879\",\"name\":\"100003972879\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972375\",\"name\":\"100003972375\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972599\",\"name\":\"100003972599\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972655\",\"name\":\"100003972655\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973391\",\"name\":\"100003973391\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973133\",\"name\":\"100003973133\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972661\",\"name\":\"100003972661\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973273\",\"name\":\"100003973273\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972975\",\"name\":\"100003972975\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972967\",\"name\":\"100003972967\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972809\",\"name\":\"100003972809\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972763\",\"name\":\"100003972763\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972841\",\"name\":\"100003972841\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972417\",\"name\":\"100003972417\",\"wishId\":\"469\",\"wishName\":\"政地生\"}],\"subjectId\":8,\"wishId\":\"469\"},{\"adClassId\":\"BJ003\",\"studentCount\":17,\"students\":[{\"id\":\"100003972955\",\"name\":\"100003972955\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973083\",\"name\":\"100003973083\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972623\",\"name\":\"100003972623\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973371\",\"name\":\"100003973371\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972961\",\"name\":\"100003972961\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973101\",\"name\":\"100003973101\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973173\",\"name\":\"100003973173\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973241\",\"name\":\"100003973241\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972583\",\"name\":\"100003972583\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972421\",\"name\":\"100003972421\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972639\",\"name\":\"100003972639\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972753\",\"name\":\"100003972753\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973201\",\"name\":\"100003973201\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973257\",\"name\":\"100003973257\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972851\",\"name\":\"100003972851\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003973469\",\"name\":\"100003973469\",\"wishId\":\"479\",\"wishName\":\"政物生\"},{\"id\":\"100003972525\",\"name\":\"100003972525\",\"wishId\":\"479\",\"wishName\":\"政物生\"}],\"subjectId\":8,\"wishId\":\"479\"},{\"adClassId\":\"BJ004\",\"studentCount\":20,\"students\":[{\"id\":\"100003973079\",\"name\":\"100003973079\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973233\",\"name\":\"100003973233\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973067\",\"name\":\"100003973067\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972703\",\"name\":\"100003972703\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973299\",\"name\":\"100003973299\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973321\",\"name\":\"100003973321\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973161\",\"name\":\"100003973161\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972681\",\"name\":\"100003972681\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973163\",\"name\":\"100003973163\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972843\",\"name\":\"100003972843\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972865\",\"name\":\"100003972865\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972675\",\"name\":\"100003972675\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973461\",\"name\":\"100003973461\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973293\",\"name\":\"100003973293\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003973027\",\"name\":\"100003973027\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972553\",\"name\":\"100003972553\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972477\",\"name\":\"100003972477\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972615\",\"name\":\"100003972615\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972905\",\"name\":\"100003972905\",\"wishId\":\"567\",\"wishName\":\"史地物\"},{\"id\":\"100003972871\",\"name\":\"100003972871\",\"wishId\":\"567\",\"wishName\":\"史地物\"}],\"subjectId\":8,\"wishId\":\"567\"},{\"adClassId\":\"BJ004\",\"studentCount\":5,\"students\":[{\"id\":\"100003972299\",\"name\":\"100003972299\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972485\",\"name\":\"100003972485\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973343\",\"name\":\"100003973343\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972863\",\"name\":\"100003972863\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972641\",\"name\":\"100003972641\",\"wishId\":\"679\",\"wishName\":\"地物生\"}],\"subjectId\":8,\"wishId\":\"679\"},{\"adClassId\":\"BJ005\",\"studentCount\":32,\"students\":[{\"id\":\"100003973155\",\"name\":\"100003973155\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973255\",\"name\":\"100003973255\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972293\",\"name\":\"100003972293\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973091\",\"name\":\"100003973091\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973313\",\"name\":\"100003973313\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972903\",\"name\":\"100003972903\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972705\",\"name\":\"100003972705\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972503\",\"name\":\"100003972503\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972651\",\"name\":\"100003972651\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972695\",\"name\":\"100003972695\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972779\",\"name\":\"100003972779\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973149\",\"name\":\"100003973149\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972895\",\"name\":\"100003972895\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972937\",\"name\":\"100003972937\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973003\",\"name\":\"100003973003\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972699\",\"name\":\"100003972699\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973269\",\"name\":\"100003973269\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973353\",\"name\":\"100003973353\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973397\",\"name\":\"100003973397\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972473\",\"name\":\"100003972473\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972735\",\"name\":\"100003972735\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972409\",\"name\":\"100003972409\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972429\",\"name\":\"100003972429\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972637\",\"name\":\"100003972637\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973323\",\"name\":\"100003973323\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972465\",\"name\":\"100003972465\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972319\",\"name\":\"100003972319\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973317\",\"name\":\"100003973317\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972845\",\"name\":\"100003972845\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972981\",\"name\":\"100003972981\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972861\",\"name\":\"100003972861\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003972567\",\"name\":\"100003972567\",\"wishId\":\"579\",\"wishName\":\"史物生\"}],\"subjectId\":8,\"wishId\":\"579\"},{\"adClassId\":\"BJ006\",\"studentCount\":4,\"students\":[{\"id\":\"100003973059\",\"name\":\"100003973059\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973463\",\"name\":\"100003973463\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973139\",\"name\":\"100003973139\",\"wishId\":\"579\",\"wishName\":\"史物生\"},{\"id\":\"100003973185\",\"name\":\"100003973185\",\"wishId\":\"579\",\"wishName\":\"史物生\"}],\"subjectId\":8,\"wishId\":\"579\"},{\"adClassId\":\"BJ006\",\"studentCount\":20,\"students\":[{\"id\":\"100003973015\",\"name\":\"100003973015\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973259\",\"name\":\"100003973259\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972701\",\"name\":\"100003972701\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972725\",\"name\":\"100003972725\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972825\",\"name\":\"100003972825\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972827\",\"name\":\"100003972827\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972947\",\"name\":\"100003972947\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972539\",\"name\":\"100003972539\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973011\",\"name\":\"100003973011\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972295\",\"name\":\"100003972295\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973319\",\"name\":\"100003973319\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973473\",\"name\":\"100003973473\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973329\",\"name\":\"100003973329\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972711\",\"name\":\"100003972711\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972811\",\"name\":\"100003972811\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003972855\",\"name\":\"100003972855\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973443\",\"name\":\"100003973443\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973081\",\"name\":\"100003973081\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973445\",\"name\":\"100003973445\",\"wishId\":\"679\",\"wishName\":\"地物生\"},{\"id\":\"100003973429\",\"name\":\"100003973429\",\"wishId\":\"679\",\"wishName\":\"地物生\"}],\"subjectId\":8,\"wishId\":\"679\"},{\"adClassId\":\"BJ008\",\"studentCount\":55,\"students\":[{\"id\":\"100003972953\",\"name\":\"100003972953\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973451\",\"name\":\"100003973451\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973205\",\"name\":\"100003973205\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972561\",\"name\":\"100003972561\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973047\",\"name\":\"100003973047\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972405\",\"name\":\"100003972405\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972831\",\"name\":\"100003972831\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972491\",\"name\":\"100003972491\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972833\",\"name\":\"100003972833\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972877\",\"name\":\"100003972877\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973053\",\"name\":\"100003973053\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973097\",\"name\":\"100003973097\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973051\",\"name\":\"100003973051\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972713\",\"name\":\"100003972713\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972451\",\"name\":\"100003972451\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973331\",\"name\":\"100003973331\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972707\",\"name\":\"100003972707\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972511\",\"name\":\"100003972511\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973291\",\"name\":\"100003973291\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972437\",\"name\":\"100003972437\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972519\",\"name\":\"100003972519\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972297\",\"name\":\"100003972297\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973093\",\"name\":\"100003973093\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973441\",\"name\":\"100003973441\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972359\",\"name\":\"100003972359\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973237\",\"name\":\"100003973237\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972595\",\"name\":\"100003972595\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973113\",\"name\":\"100003973113\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973157\",\"name\":\"100003973157\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972587\",\"name\":\"100003972587\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973087\",\"name\":\"100003973087\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973121\",\"name\":\"100003973121\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973287\",\"name\":\"100003973287\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972867\",\"name\":\"100003972867\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972823\",\"name\":\"100003972823\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972669\",\"name\":\"100003972669\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972983\",\"name\":\"100003972983\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972389\",\"name\":\"100003972389\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972619\",\"name\":\"100003972619\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972501\",\"name\":\"100003972501\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973045\",\"name\":\"100003973045\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972505\",\"name\":\"100003972505\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973305\",\"name\":\"100003973305\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972507\",\"name\":\"100003972507\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973345\",\"name\":\"100003973345\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972287\",\"name\":\"100003972287\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973421\",\"name\":\"100003973421\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972301\",\"name\":\"100003972301\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973029\",\"name\":\"100003973029\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973107\",\"name\":\"100003973107\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972303\",\"name\":\"100003972303\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972427\",\"name\":\"100003972427\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973033\",\"name\":\"100003973033\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972309\",\"name\":\"100003972309\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972577\",\"name\":\"100003972577\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972899\",\"name\":\"100003972899\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973195\",\"name\":\"100003973195\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972395\",\"name\":\"100003972395\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972727\",\"name\":\"100003972727\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972923\",\"name\":\"100003972923\",\"wishId\":\"456\",\"wishName\":\"政史地\"}],\"subjectId\":8,\"wishId\":\"456\"},{\"adClassId\":\"BJ008\",\"studentCount\":11,\"students\":[{\"id\":\"100003973315\",\"name\":\"100003973315\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973459\",\"name\":\"100003973459\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972901\",\"name\":\"100003972901\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972479\",\"name\":\"100003972479\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973217\",\"name\":\"100003973217\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973381\",\"name\":\"100003973381\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973279\",\"name\":\"100003973279\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003973189\",\"name\":\"100003973189\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972889\",\"name\":\"100003972889\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972571\",\"name\":\"100003972571\",\"wishId\":\"469\",\"wishName\":\"政地生\"},{\"id\":\"100003972335\",\"name\":\"100003972335\",\"wishId\":\"469\",\"wishName\":\"政地生\"}],\"subjectId\":8,\"wishId\":\"469\"},{\"adClassId\":\"BJ009\",\"studentCount\":26,\"students\":[{\"id\":\"100003972535\",\"name\":\"100003972535\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972761\",\"name\":\"100003972761\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973335\",\"name\":\"100003973335\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973379\",\"name\":\"100003973379\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973413\",\"name\":\"100003973413\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972803\",\"name\":\"100003972803\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972333\",\"name\":\"100003972333\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972965\",\"name\":\"100003972965\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973385\",\"name\":\"100003973385\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972891\",\"name\":\"100003972891\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972337\",\"name\":\"100003972337\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973213\",\"name\":\"100003973213\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972687\",\"name\":\"100003972687\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973265\",\"name\":\"100003973265\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972921\",\"name\":\"100003972921\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972767\",\"name\":\"100003972767\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972647\",\"name\":\"100003972647\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972717\",\"name\":\"100003972717\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972957\",\"name\":\"100003972957\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972673\",\"name\":\"100003972673\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972795\",\"name\":\"100003972795\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972873\",\"name\":\"100003972873\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972959\",\"name\":\"100003972959\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973449\",\"name\":\"100003973449\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003973325\",\"name\":\"100003973325\",\"wishId\":\"456\",\"wishName\":\"政史地\"},{\"id\":\"100003972341\",\"name\":\"100003972341\",\"wishId\":\"456\",\"wishName\":\"政史地\"}],\"subjectId\":8,\"wishId\":\"456\"},{\"adClassId\":\"BJ009\",\"studentCount\":30,\"students\":[{\"id\":\"100003972793\",\"name\":\"100003972793\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972909\",\"name\":\"100003972909\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973235\",\"name\":\"100003973235\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972743\",\"name\":\"100003972743\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973361\",\"name\":\"100003973361\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972381\",\"name\":\"100003972381\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972531\",\"name\":\"100003972531\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972499\",\"name\":\"100003972499\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973333\",\"name\":\"100003973333\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973355\",\"name\":\"100003973355\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973411\",\"name\":\"100003973411\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972783\",\"name\":\"100003972783\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972709\",\"name\":\"100003972709\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973013\",\"name\":\"100003973013\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972847\",\"name\":\"100003972847\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973153\",\"name\":\"100003973153\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972631\",\"name\":\"100003972631\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973327\",\"name\":\"100003973327\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973005\",\"name\":\"100003973005\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972373\",\"name\":\"100003972373\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972493\",\"name\":\"100003972493\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972971\",\"name\":\"100003972971\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973249\",\"name\":\"100003973249\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972387\",\"name\":\"100003972387\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972617\",\"name\":\"100003972617\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973465\",\"name\":\"100003973465\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972715\",\"name\":\"100003972715\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972737\",\"name\":\"100003972737\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003972447\",\"name\":\"100003972447\",\"wishId\":\"569\",\"wishName\":\"史地生\"},{\"id\":\"100003973401\",\"name\":\"100003973401\",\"wishId\":\"569\",\"wishName\":\"史地生\"}],\"subjectId\":8,\"wishId\":\"569\"}]\r\n" + 
				"";
		System.out.println(str);
		List<Cell> cells = JSONArray.parseArray(str, Cell.class);
		//List<LinkedList<Integer>> cList = generateSumCombine(sz, 0, new LinkedList<Integer>(), 7);
		//List<ArrangeItem> cList = generateCellSumArrange(cells.toArray(new LearnCell[cells.size()]), new LinkedList<LearnCell>(), 112);
		
		
		//List<LinkedList<LearnCell>> list = generateSumCombine(cells.toArray(new LearnCell[cells.size()]), 0, new LinkedList<LearnCell>(), 112);
		List<CellStack> list = generateSumCombineArrange(cells.toArray(new Cell[cells.size()]), new LinkedList<Cell>(), 112, new TimeoutWatch(10L));
		System.out.println(num);
		System.out.println(list.size());
		
		//generateCombine(cList.toArray(), 0, new LinkedList<Integer>(), 2);
	}
	
	
	public static void main23(String[] args) {
		
		//Integer[] sz = new Integer[] {62,7,12,13,9};
		Integer[] sz = new Integer[] {7,9,12,13,62};
		
		//List<LinkedList<Integer>> cList = generateSumCombine(sz, 0, new LinkedList<Integer>(), 7);
		//List<ArrangeItem> cList = generateCellSumArrange(cells.toArray(new LearnCell[cells.size()]), new LinkedList<LearnCell>(), 112);
		List<LinkedList<Integer>> list1 = generateSumCombine(sz, 0, new LinkedList<Integer>(), 58);
		List<LinkedList<Integer>> list2 = generateSumArrange(sz, new LinkedList<Integer>(), 58);
		System.out.println(list1.size());
		System.out.println(list2.size());
		//System.out.println(cList);
		
		
		//generateCombine(cList.toArray(), 0, new LinkedList<Integer>(), 2);
	}
	
	public static LinkedList<Cell> splitClass(CellStack cellStack, TreeBasedTable<String, String, Cell> table) {
		
		//拆班
		LinkedList<Cell> selectLinked = new LinkedList<Cell>();
		int totalCount = 0;
		for (int i=0;i<cellStack.getCells().size();i++) {

			Cell learnCell = cellStack.getCells().get(i);
			int studentCount = learnCell.getStudentCount();
			if(i==cellStack.getCells().size()-1 ) {
				studentCount = cellStack.getMaxValue()-totalCount;
			}
			
			if(studentCount<=0) {
				throw new RuntimeException("不正确的待拆情况, maxValue:"+cellStack.getMaxValue()+" cells:"+ cellStack.getCells());
			}
			
			Cell divClass = new Cell(learnCell.getRowKey(), learnCell.getColumnKey(), studentCount);

			totalCount+=studentCount;
			
			selectLinked.add(divClass);
		}
		
		return selectLinked;
	}
	

	
	
	public static List<LinkedList<Cell>> splitSubClass(Collection<Cell> items, int avgClassSize) {
		
		Assert.isTrue(avgClassSize>0, "平均班额不正确:"+avgClassSize);
		
		List<LinkedList<Cell>> classList = new ArrayList<LinkedList<Cell>>();
		//拆班
		LinkedList<Cell> selectLinked = new LinkedList<Cell>();
		int totalCount = 0;
		
		//复制数据
		List<Cell> tempCellList = new ArrayList<Cell>();
		for (Cell learnCell : items) {
			tempCellList.add(new Cell(learnCell.getRowKey(), learnCell.getColumnKey(), learnCell.getStudentCount()));
		}
		
		for (int i=0;i<tempCellList.size();i++) {
			
			Cell learnCell = tempCellList.get(i);
			int studentCount = learnCell.getStudentCount();
			if(totalCount+studentCount>avgClassSize) {
				studentCount = avgClassSize-totalCount;
			}
			
			Cell divClass = new Cell(learnCell.getRowKey(), learnCell.getColumnKey(), studentCount);
			learnCell.setStudentCount(learnCell.getStudentCount()-studentCount);
			selectLinked.add(divClass);
			totalCount+=studentCount;
			
			if(learnCell.getStudentCount()>0) {
				classList.add(selectLinked);
				selectLinked = new LinkedList<Cell>();
				totalCount=0;
				i--;
			}
			
		}
		
		classList.add(selectLinked);
		
		return classList;
	}
	
	
	/**
	 * 计算排列
	 * @param datas       元素
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static List<CellStack> generateCellSumArrange(Cell[] datas, LinkedList<Cell> selectData, int maxValue) {
		//System.out.println(JSONObject.toJSONString(datas));
		int totalValue = 0;
		for (Cell cell : selectData) {
			totalValue +=cell.getStudentCount();
		}
		
		//成功
		if(totalValue >= maxValue) {
		//if(selectData.size() == 4) {
			
			num++;
			//System.out.println(JSONObject.toJSONString(selectData));
			LinkedList<Cell> okSelectData = new LinkedList<Cell>(selectData);
			CellStack cellStack = new CellStack();
			cellStack.setCells(okSelectData);
			cellStack.setMaxValue(maxValue);
			List<CellStack> result = new ArrayList<CellStack>();
			result.add(cellStack);
			return result;
		}
		
		List<CellStack> result = new ArrayList<CellStack>();
		Set<Cell> set1 = Sets.newHashSet(datas);
		Set<Cell> set2 = new HashSet<Cell>(selectData);
		SetView<Cell> diff = Sets.difference(set1, set2);
		for (Cell df : diff) {
			
			selectData.add(df);
			
			List<CellStack> clist1 = generateCellSumArrange(datas, selectData, maxValue);
			if(null != clist1 && clist1.size()>0) result.addAll(clist1);
			
			selectData.remove(df);
		}
		
		return result;
	}
	
	/**
	 * 计算排列
	 * @param datas       元素
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static List<LinkedList<Integer>> generateSumArrange(Integer[] datas, LinkedList<Integer> selectData, int maxValue) {
		
		int totalValue = 0;
		for (Integer v : selectData) {
			totalValue +=v;
		}
		
		//成功
		if(totalValue >= maxValue) {
		//if(selectData.size() == 4) {
			
			num++;
			//System.out.println(JSONObject.toJSONString(selectData));
			LinkedList<Integer> okSelectData = new LinkedList<Integer>(selectData);
			List<LinkedList<Integer>> result = new ArrayList<LinkedList<Integer>>();
			result.add(okSelectData);
			return result;
		}
		
		List<LinkedList<Integer>> result = new ArrayList<LinkedList<Integer>>();
		Set<Integer> set1 = Sets.newHashSet(datas);
		Set<Integer> set2 = new HashSet<Integer>(selectData);
		SetView<Integer> diff = Sets.difference(set1, set2);
		for (Integer df : diff) {
			
			selectData.add(df);
			
			List<LinkedList<Integer>> clist1 = generateSumArrange(datas, selectData, maxValue);
			if(null != clist1 && clist1.size()>0) result.addAll(clist1);
			
			selectData.remove(df);
			
		}
		
		return result;
	}
	
	public static List<CellStack> generateSumCombineArrange(Cell[] datas, LinkedList<Cell> defaultSelect, int maxValue, TimeoutWatch timeoutWatch) {
		
		List<CellStack> allList = new LinkedList<>();
		
		if(maxValue<=0) return allList;
		
		int defaultCount = 0;
		Set<String> defaultkeys = new HashSet<String>();
		for (Cell learnCell : defaultSelect) {
			defaultkeys.add(learnCell.getRowKey()+Utils.SEPARATOR_SYMBOL+learnCell.getColumnKey());
			defaultCount+=learnCell.getStudentCount();
		}
		
		if(defaultCount>maxValue) return allList;
		
		//排序
		for(int i=0;i<datas.length;i++) {
			for(int j=i+1;j<datas.length;j++) {
				int count_i = datas[i].getStudentCount();
				int count_j = datas[j].getStudentCount();
				if(count_i>count_j){
					Cell temp=datas[i];
					datas[i]=datas[j];
					datas[j]=temp;
				}
			}
		}

		List<CellStack> list = generateSumCombine(datas, 0, defaultSelect, maxValue, timeoutWatch);
		if(null ==  list) return allList;
		
		for (CellStack cellStack : list) {
			//System.out.println(cellStack.getCells().toString());
			//默认情况
			allList.add(cellStack);
			
			//判断组合中拆的情况
			for (int i=cellStack.getCells().size()-2;i>=0;i--) {
				
				Cell waitDivCell = cellStack.getCells().get(i);
				
				//必须的不能被拆
				if(defaultkeys.contains(waitDivCell.getRowKey()+Utils.SEPARATOR_SYMBOL+waitDivCell.getColumnKey())){
					continue;
				}
				
				List<Cell> otherList = new ArrayList<Cell>(cellStack.getCells());
				otherList.remove(waitDivCell);
				
				int otherTotal = 0;
				for (Cell cell : otherList) {
					otherTotal+=cell.getStudentCount();
				}
				
				//如果相等，用默认情况
				if(waitDivCell.getStudentCount()+otherTotal == maxValue) {
					break;
				}
				
				//可以拆
				if(otherTotal<maxValue) {
					LinkedList<Cell> okList = new LinkedList<Cell>(otherList);
					okList.addLast(waitDivCell);
					//组合内其它可拆情况
					CellStack newCellStack = new CellStack();
					newCellStack.setCells(okList);
					newCellStack.setMaxValue(maxValue);
					
					allList.add(newCellStack);
				}
			}
		}
		return allList;
	}

		
	
	/**
	 * 计算组合
	 * @param datas       原素
	 * @param step        当前选择步数
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static List<CellStack> generateSumCombine(Cell[] datas, int step, LinkedList<Cell> selectData, int maxValue, TimeoutWatch dsw) {
		
		int totalValue = 0;
		for (Cell sv : selectData) {
			totalValue +=sv.getStudentCount();
		}
		
		//成功
		if(totalValue >= maxValue) {
			//System.out.println(JSONObject.toJSONString(selectData));
			//为每种组合情况复制新对象数据
			LinkedList<Cell> newSelectData = new LinkedList<Cell>();
			for (Cell learnCell : selectData) {
				Cell lc = new Cell(learnCell.getRowKey(), learnCell.getColumnKey(), learnCell.getStudentCount());
				lc.setRowKey(learnCell.getRowKey());
				lc.setColumnKey(learnCell.getColumnKey());
				newSelectData.add(lc);
			}
			LinkedList<Cell> okSelectData = new LinkedList<Cell>(newSelectData);
			
			CellStack cellStack = new CellStack();
			cellStack.setCells(okSelectData);
			cellStack.setMaxValue(maxValue);
			
			List<CellStack> result = new ArrayList<>();
			result.add(cellStack);
			return result;
		}
		
		if(step>= datas.length) {
			return null;
		}

		if(dsw.isTimeout()) {
			return null;
		}
		
		//选择当前元素
		try {
			selectData.add(datas[step]);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<CellStack> result = new ArrayList<>();
		
		List<CellStack> clist1 = generateSumCombine(datas, step+1, selectData, maxValue, dsw);
		
		selectData.remove(datas[step]);
		
		//不选择当前元素
		List<CellStack> clist2 = generateSumCombine(datas, step+1, selectData, maxValue, dsw);
		
		if(null != clist1 && clist1.size()>0) result.addAll(clist1);
		
		if(null != clist2 && clist2.size()>0) result.addAll(clist2);
		
		return result;
	}
	
	/**
	 * 计算组合
	 * @param datas       原素
	 * @param step        当前选择步数
	 * @param selectData  选中原素
	 * @param selectCount 要选择个数
	 */
	public static List<LinkedList<Integer>> generateSumCombine(Integer[] datas, int step, LinkedList<Integer> selectData, int maxValue) {
		
		int totalValue = 0;
		for (Integer sv : selectData) {
			totalValue +=sv;
		}
		
		//成功
		if(totalValue >= maxValue) {
			//System.out.println(JSONObject.toJSONString(selectData));
			LinkedList<Integer> okSelectData = new LinkedList<Integer>(selectData);
			List<LinkedList<Integer>> result = new ArrayList<LinkedList<Integer>>();
			result.add(okSelectData);
			return result;
		}
		
		if(step>= datas.length) {
			return null;
		}
		
		//选择当前元素
		try {
			selectData.add(datas[step]);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<LinkedList<Integer>> result = new ArrayList<LinkedList<Integer>>();
		
		List<LinkedList<Integer>> clist1 = generateSumCombine(datas, step+1, selectData, maxValue);
		
		selectData.remove(datas[step]);
		
		//不选择当前元素
		List<LinkedList<Integer>> clist2 = generateSumCombine(datas, step+1, selectData, maxValue);
		
		if(null != clist1 && clist1.size()>0) result.addAll(clist1);
		
		if(null != clist2 && clist2.size()>0) result.addAll(clist2);
		
		return result;
	}
	
	
	/**
	 * 复制集合数据
	 * @param scrList
	 * @return
	 */
	public static LinkedList<Cell> copyLearnCellList(List<Cell> scrList) {
		
		LinkedList<Cell> newList = new LinkedList<Cell>();
		for (Cell learnCell : scrList) {
			newList.add(new Cell(learnCell));
		}
		
		return newList;
	}
	
	
	/**
	 * 行政班打分
	 * @param divContext
	 * @param divAdClassList
	 * @return
	 */
	public static ScoreResult getScore(DivContext divContext, List<ClassResult> divAdClassList) {
		
		ScoreResult score = new AdScoreResult();
		score.setClassResultList(divAdClassList);
		
		//行班班方差
		Double adVarianceValue = calculateVariance(divAdClassList, null);
		
		//计算科目下行政班开班数
		 HashMultiset<String> optSubjectAdCountMultiSet = HashMultiset.create();
		
		//计算选三科目人数
		HashMultiset<String> optThreeSubjectId2StudentCountMultiSet = HashMultiset.create();
		TreeBasedTable<String, String, Cell> chooseThreeTable=TreeBasedTable.create();
		
		for (ClassResult divClass : divAdClassList) {
			
			AdClassResult divAdClass = (AdClassResult) divClass;
			
			SubjectGroup fixTwoSubjectGroup = divAdClass.getFixTwoSubjectGroup();
			
			for (Cell cell : divAdClass.getCells()) {
				SubjectGroup wish = divContext.getWishId2SubjectGroupMap().get(cell.getRowKey());
				SetView<String> diff = Sets.difference(new HashSet<>(wish.getIds()), new HashSet<>(fixTwoSubjectGroup.getIds()));
				try {
					optThreeSubjectId2StudentCountMultiSet.add(diff.iterator().next(), cell.getStudentCount());
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				Cell optCell = new Cell(divAdClass.getId()+Utils.SEPARATOR_SYMBOL+cell.getRowKey(), diff.iterator().next(), cell.getStudentCount());
				chooseThreeTable.put(divAdClass.getId()+Utils.SEPARATOR_SYMBOL+cell.getRowKey(), diff.iterator().next(), optCell);
			}
			
			//累计科目下行政班选考开班数
			for (Subject subject : fixTwoSubjectGroup.getSubjects()) {
				optSubjectAdCountMultiSet.add(subject.getId());
			}
		}
		//printChooseThreeTable2(divContext, chooseThreeTable);
		//计算教学班方差(按科目最优平均值)
		Double teachVarianceTotal = 0D;
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			
			List<TeachClassResult> optThreeDivClassList = new ArrayList<>();

			//选三科目剩余总人数
			Integer subjectStudentCount = optThreeSubjectId2StudentCountMultiSet.count(subject.getId());
			
			if(subjectStudentCount<=0) {
				continue;
			}
			
			Integer subjectAdClassCount = optSubjectAdCountMultiSet.count(subject.getId());
			//科目选三剩余开班数
			Integer subjectRemainCount = subject.getClassTotalCount()-subjectAdClassCount;

			if(subjectRemainCount<=0) {
				return null;
			}
			
			Double upAvgSize = Math.ceil(subjectStudentCount.doubleValue()/subjectRemainCount);
			
			Collection<Cell> validList = Collections2.filter( chooseThreeTable.column(subject.getId()).values(), new Predicate<Cell>() {
				public boolean apply(Cell input) {
					return input.getStudentCount()>0;
				};
			});
			
			List<LinkedList<Cell>> splitList = splitSubClass(validList, upAvgSize.intValue());
			for (LinkedList<Cell> cells : splitList) {
				optThreeDivClassList.add(new TeachClassResult(divContext, subject.getId(), cells));
			}
			
			((AdScoreResult)score).getOpThreeDivClassList().addAll(optThreeDivClassList);
			
			Double teachVarianceValue = calculateVariance(optThreeDivClassList, subject.getAvgClassSize().doubleValue());
			
			teachVarianceTotal+=teachVarianceValue;
		}
		
		score.setScore(adVarianceValue+teachVarianceTotal);
		
		return score;
	}
	
	
	public static <T extends ClassResult> Double calculateVariance(List<T> divClassList, Double avgSize) {
		
		//为空按这组数据平均值计算
		if(null == avgSize) {
			//所有班级总人数
			Integer adTotalStudentCount = 0;
			for (ClassResult divClass : divClassList) {
				adTotalStudentCount+=divClass.getTotalStudent();
			}
			avgSize = adTotalStudentCount.doubleValue()/divClassList.size();
		}
		
		Double offsetTotal = 0D;
		for (ClassResult divAdClass : divClassList) {
			offsetTotal+=Math.pow(divAdClass.getTotalStudent()-avgSize, 2);
		}
		
		//Double adVarianceValue = offsetTotal/divClassList.size();
		
		return offsetTotal;
		
	}

	
	/**
	 * 计算新的课时分布
	 * @param subjectMap
	 * @param classHourTable
	 */
	public static TreeBasedTable<Integer, String, SeqCell>  calculateAdClassCountTable(DivContext divContext) {
		
		TreeBasedTable<Integer, String, SeqCell> adClassCountTable=TreeBasedTable.create();
		for(int i=1;i<=2;i++) {
			for (Map.Entry<String, Subject> entry : divContext.getSubjectId2SubjectMap().entrySet()) {
				adClassCountTable.put(i, entry.getKey(), new SeqCell(i, entry.getValue()));
			}
		}
		
		//初始化矩阵科目开班分布
		int preIndex = 0;
		//循环科目
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			int classNumber = subject.getAdClassTotalCount();
			Map<Integer, SeqCell> seqMap =  adClassCountTable.column(subject.getId());
			Integer[] seqArray = seqMap.keySet().toArray(new Integer[seqMap.keySet().size()]);
			int i=preIndex>=seqMap.keySet().size()?0:preIndex;
			//循环序列
			while(i<seqArray.length && classNumber > 0) {
				SeqCell seqCell = seqMap.get(seqArray[i]);
				seqCell.setClassCount(seqCell.getClassCount()+1);
				classNumber--;
				if(i==seqArray.length-1 && classNumber>0) {
					i=0;
				}else {
					i++;
				}
			}
			preIndex = i;
		}
		
		//剩下的开班数放选三中
		for (Map.Entry<String, Subject> entry : divContext.getSubjectId2SubjectMap().entrySet()) {
			SeqCell cell = new SeqCell(3, entry.getValue());
			cell.setClassCount(entry.getValue().getClassTotalCount() - entry.getValue().getAdClassTotalCount());
			adClassCountTable.put(3, entry.getKey(), cell);
		}
		System.out.println("===打印选一选二选三最新开班分布");
		printSeqHour(divContext.getSubjectId2SubjectMap(), adClassCountTable);
		
		return adClassCountTable;
	}
	
	
	/**
	 * 打印科目序列课时分布图
	 */
	public static void  printSeqHour(Map<String, Subject> subjectMap, Table<Integer, String, SeqCell> table) {
		
		StringBuffer title = new StringBuffer("      ");
		StringBuffer avgClassSizeStr = new StringBuffer("平均班额  ");
		StringBuffer classTotalStr  = new StringBuffer("科目班数  ");
		for (Subject subject : subjectMap.values()) {
			title.append("   "+subject.getName()+"("+subject.getTotalStudentCount()+")");
			avgClassSizeStr.append("  "+subject.getAvgClassSize());
			classTotalStr.append("  "+subject.getClassTotalCount());
		}
		System.out.println(title.toString());
		
		for (Map.Entry<Integer, Map<String, SeqCell>> rowEntry : table.rowMap().entrySet()) {
			StringBuffer sb = new StringBuffer(rowEntry.getKey()+"序列       ");
			for (Subject subject : subjectMap.values()) {
				SeqCell cell = rowEntry.getValue().get(subject.getId());
				sb.append("  "+cell.getClassCount());
			}
			System.out.println(sb.toString());
		}
		
		System.out.println(classTotalStr);
		System.out.println(avgClassSizeStr);
	}
}
