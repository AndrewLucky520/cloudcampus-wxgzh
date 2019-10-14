package com.talkweb.placementtask.utils.div.medium.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;

/**
 * 固定班级结果记录
 * @author hushow
 *
 */
public class FixedClassResult extends ClassResult{
	
	/**
	 * 教学班所属科目
	 */
	private String wishId;
	
	
	public FixedClassResult(DivContext divContext, String wishId, List<Cell> cells) {
		super(divContext, cells);
		this.wishId = wishId;
		this.setCells(cells);
	}
	
	
	public void print() {
		
		SubjectGroup wishSubjectGroup = this.getDivContext().getWishId2SubjectGroupMap().get(wishId);
		
		System.out.println("===固定班级："+wishSubjectGroup.getName()+wishSubjectGroup.getId()+"["+ this.getId()+"]" + " 总人数:"+this.getTotalStudent());
		
		for (Entry<String, Collection<Student>> wishStudent : this.getWishId2StudentList().asMap().entrySet()) {
			SubjectGroup wish = this.getDivContext().getWishId2SubjectGroupMap().get(wishStudent.getKey());
			
			List<String> ids = Lists.transform(new ArrayList<Student>(wishStudent.getValue()), new Function<Student, String>() {
				public String apply(Student input) {
					return input.getId();
				}
			});
			
			//String str = "  "+wish.getName()+wish.getId()+" 人数:"+ids.size()+" "+ids;
			String str = "  "+wish.getName()+wish.getId()+" 人数:"+ids.size();
			System.out.println(str);
		}
	}


	public String getWishId() {
		return wishId;
	}


	public void setWishId(String wishId) {
		this.wishId = wishId;
	}

}
