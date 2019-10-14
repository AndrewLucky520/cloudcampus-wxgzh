package com.talkweb.placementtask.utils.div.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeBasedTable;
import com.talkweb.placementtask.utils.div.Utils;

/**
 * 班级结果记录
 * @author hushowly@foxmail.com
 *
 */
public class ClassResult{
	
	public enum ClassType{
		OPT,
		Pro;
	}
	
	
	/**
	 * 班级id
	 */
	private String id;
	
	private Integer seqId;
	
	private List<Cell>  cells;
	
	private Integer totalStudent;
	
	/**
	 * 所属志愿科目组
	 */
	private List<SubjectGroup> wishSubjectGroups = new ArrayList<SubjectGroup>();
	
	/**
	 * 班级下志愿科目人数分布图
	 */
	private TreeBasedTable<String, String, Integer> table=TreeBasedTable.create();
	
	/**
	 * 上下文对象
	 */
	private DivContext divContext;
	
	
	private Map<String, Integer> wishId2StudentCountMap = new HashMap<String, Integer>();
	
	
	private ArrayListMultimap<String, Student> wishId2StudentList = ArrayListMultimap.create();
	
	
	/**
	 * 获取当前行政班总人数
	 * @return
	 */
	public int getTotalStudent() {
		return totalStudent;
	}
	
	
	public ClassResult(String prefix, DivContext divContext, List<Cell> cells) {
		this.divContext = divContext;
		this.id = Utils.generateClassId(prefix);
		this.cells = cells;
		totalStudent=0;
		for (Cell cell : cells) {
			totalStudent+=cell.getStudentCount();
		}
	}
	
	
	public ClassResult(DivContext divContext, List<Cell> cells) {
		this.divContext = divContext;
		this.id = Utils.generateClassId("BJ");
		this.cells = cells;
		totalStudent=0;
		for (Cell cell : cells) {
			totalStudent+=cell.getStudentCount();
		}
	}
	
	
	public void print() {
		
		System.out.println("===班级:["+ this.getId()+"]" + " 总人数:"+this.getTotalStudent());
		
		for (Entry<String, Collection<Student>> wishStudent : this.getWishId2StudentList().asMap().entrySet()) {
			SubjectGroup wish = this.getDivContext().getWishId2SubjectGroupMap().get(wishStudent.getKey());
			
			List<String> ids = Lists.transform(new ArrayList<Student>(wishStudent.getValue()), new Function<Student, String>() {
				public String apply(Student input) {
					return input.getId();
				}
			});
			
			//String str = "  "+wish.getName()+wish.getId()+" 人数:"+ids.size()+" 列表:"+ids;
			String str = "  "+wish.getName()+wish.getId()+" 人数:"+ids.size();
			System.out.println(str);
		}
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public TreeBasedTable<String, String, Integer> getTable() {
		return table;
	}


	public void setTable(TreeBasedTable<String, String, Integer> table) {
		this.table = table;
	}


	public DivContext getDivContext() {
		return divContext;
	}


	public void setDivContext(DivContext divContext) {
		this.divContext = divContext;
	}


	public List<SubjectGroup> getWishSubjectGroups() {
		return wishSubjectGroups;
	}


	public void setWishSubjectGroups(List<SubjectGroup> wishSubjectGroups) {
		this.wishSubjectGroups = wishSubjectGroups;
	}


	public Map<String, Integer> getWishId2StudentCountMap() {
		return wishId2StudentCountMap;
	}


	public void setWishId2StudentCountMap(Map<String, Integer> wishId2StudentCountMap) {
		this.wishId2StudentCountMap = wishId2StudentCountMap;
	}


	public ArrayListMultimap<String, Student> getWishId2StudentList() {
		return wishId2StudentList;
	}


	public void setWishId2StudentList(ArrayListMultimap<String, Student> wishId2StudentList) {
		this.wishId2StudentList = wishId2StudentList;
	}


	public Integer getSeqId() {
		return seqId;
	}


	public void setSeqId(Integer seqId) {
		this.seqId = seqId;
	}


	public List<Cell> getCells() {
		return cells;
	}


	public void setCells(List<Cell> cells) {
		this.cells = cells;
	}


	@Override
	public String toString() {
		return "ClassResult [id=" + id + ", seqId=" + seqId + ", totalStudent=" + totalStudent + "]";
	}
	
	

}
