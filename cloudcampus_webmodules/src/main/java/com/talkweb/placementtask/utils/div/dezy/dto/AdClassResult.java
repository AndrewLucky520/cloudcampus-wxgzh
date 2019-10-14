package com.talkweb.placementtask.utils.div.dezy.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.Subject;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;

/**
 * 分行政班结果记录
 * @author hushowly@foxmail.com
 *
 */
public class AdClassResult extends ClassResult{
	
	/**
	 * 所属定二科目组
	 */
	private SubjectGroup fixTwoSubjectGroup;
	
	
	public AdClassResult(DivContext divContext, SubjectGroup fixTwoSubjectGroup, List<Cell> cellList) {
		
		super(divContext, cellList);
		
		this.fixTwoSubjectGroup = fixTwoSubjectGroup;
		
		this.setCells(cellList);
		
		//初始化志愿和科目人数信息
		ArrayList<SubjectGroup> wishSubjectGroups = new ArrayList<SubjectGroup>();
		for (Cell adClassCell : cellList) {
			SubjectGroup wishSubjectGroup = divContext.getWishId2SubjectGroupMap().get(adClassCell.getRowKey());
			wishSubjectGroups.add(wishSubjectGroup);
			this.getWishId2StudentCountMap().put(wishSubjectGroup.getId(), adClassCell.getStudentCount());
			for (Subject subject : wishSubjectGroup.getSubjects()) {
				this.getTable().put(wishSubjectGroup.getId(), subject.getId(), adClassCell.getStudentCount());
			}
		}
		this.setWishSubjectGroups(wishSubjectGroups);
	}
	
	/**
	 * 获取当前行政班总人数
	 * @return
	 */
	public int getTotalStudent() {
		
		int total = 0;
		for (Entry<String, Map<String, Integer>> rowEntry : this.getTable().rowMap().entrySet()) {
			
			for (Entry<String, Integer> columnEntry : rowEntry.getValue().entrySet()) {
				if(null != columnEntry.getValue()) {
					total += columnEntry.getValue();
					break;
				}
			}
		}
		
		return total;
	}
	
	public void print(DivContext divContext) {
		System.out.println("===行政班："+fixTwoSubjectGroup.getName()+"["+ this.getId()+"]"+ fixTwoSubjectGroup.getId() + "总人数:  "+this.getTotalStudent());
		
		StringBuffer title = new StringBuffer("        ");
		for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
			title.append("  00"+subject.getId());
		}
		System.out.println(title);
		
		for (Entry<String, Map<String, Integer>> rowEntry : this.getTable().rowMap().entrySet()) {
			SubjectGroup wish = divContext.getWishId2SubjectGroupMap().get(rowEntry.getKey());
			StringBuffer str = new StringBuffer(wish.getName()+wish.getId()+" ");
			
			for (Subject subject : divContext.getSubjectId2SubjectMap().values()) {
				Integer v = rowEntry.getValue().get(subject.getId());
				if(null == v) {
					str.append("  000");
				}else {
					str.append("  "+this.fillStr(String.valueOf(v), 3));
				}
			}
			System.out.println(str.toString());
		}
	}

	
	public String fillStr(String str, int num) {
		int x= num-str.toCharArray().length;
		if(x>0) {
			for(int i=0;i<x;i++) {
				str="0"+str;
			}
		}
		return str;
	}
	
	
	/**
	 * 复制数据
	 * @param ar
	 * @return
	 */
	public static AdClassResult cloneAdClassResult(AdClassResult src) {
		
		AdClassResult newAd = new AdClassResult(src.getDivContext(), src.getFixTwoSubjectGroup(), src.getCells());
		newAd.setId(src.getId());
		newAd.setFixTwoSubjectGroup(src.getFixTwoSubjectGroup());
		newAd.setWishSubjectGroups(src.getWishSubjectGroups());
		newAd.setWishId2StudentCountMap(new HashMap<String, Integer>(src.getWishId2StudentCountMap()));
		
		TreeBasedTable<String, String, Integer> srcTable = src.getTable();
    	for (Entry<String, Map<String, Integer>> rowEntry : srcTable.rowMap().entrySet()) {
    		for (Entry<String, Integer> cellEntry : rowEntry.getValue().entrySet()) {
    			Integer srcCell = srcTable.get(rowEntry.getKey(), cellEntry.getKey());
    			newAd.getTable().put(rowEntry.getKey(), cellEntry.getKey(), srcCell);
			}
		}
    	
    	return newAd;
	}
	
	
	/**
	 * 复制数据
	 * @param ar
	 * @return
	 */
	public static void copyAdClassResult(AdClassResult src, AdClassResult target) {
		
		target.setId(src.getId());
		target.setFixTwoSubjectGroup(src.getFixTwoSubjectGroup());
		target.setWishSubjectGroups(src.getWishSubjectGroups());
		target.setWishId2StudentCountMap(src.getWishId2StudentCountMap());
		
		TreeBasedTable<String, String, Integer> srcTable = src.getTable();
    	for (Entry<String, Map<String, Integer>> rowEntry : srcTable.rowMap().entrySet()) {
    		for (Entry<String, Integer> cellEntry : rowEntry.getValue().entrySet()) {
    			Integer srcCell = srcTable.get(rowEntry.getKey(), cellEntry.getKey());
    			target.getTable().put(rowEntry.getKey(), cellEntry.getKey(), srcCell);
			}
		}
	}
	
	
	
	/**
	 * 获取志愿下人数
	 * @param subjectGroupId
	 * @return
	 */
	public int getSubjectGroupStudentCount(String subjectGroupId) {
		Integer num = this.getWishId2StudentCountMap().get(subjectGroupId);
		return null ==num?0:num;
	}
	
	
	/**
	 * 往班级增加组合
	 * @param subjectGroup
	 */
	public void addSubjectGroup(SubjectGroup subjectGroup, int addAccount) {
		
		SubjectGroup findGroup = null;
		for (SubjectGroup sg : this.getWishSubjectGroups()) {
			if(sg.getId().equals(subjectGroup.getId())) {
				findGroup = sg;
				addAccount += this.getWishId2StudentCountMap().get(sg.getId());
			}
		}
		
		if(null == findGroup) {
			this.getWishSubjectGroups().add(subjectGroup);
			findGroup = subjectGroup;
		}
		
		this.getWishId2StudentCountMap().put(findGroup.getId(), addAccount);
		
		for (Subject subject : findGroup.getSubjects()) {
			this.getTable().put(findGroup.getId(), subject.getId(), addAccount);
		}
		
	}


	public SubjectGroup getFixTwoSubjectGroup() {
		return fixTwoSubjectGroup;
	}


	public void setFixTwoSubjectGroup(SubjectGroup fixTwoSubjectGroup) {
		this.fixTwoSubjectGroup = fixTwoSubjectGroup;
	}





	

	
}
