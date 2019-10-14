package com.talkweb.placementtask.utils.div.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.talkweb.placementtask.utils.div.Utils;

/**
 * 班级结果记录
 * @author hushowly@foxmail.com
 *
 */
public class TeachClassResult extends ClassResult{
	
	/**
	 * 教学班所属科目
	 */
	private String subjectId;
	
	
	public TeachClassResult(DivContext divContext, String subjectId, List<Cell> cells) {
		super(divContext, cells);
		this.subjectId = subjectId;
		this.setCells(cells);
	}
	
	
	
	
	public void print() {
		
		Subject subject = this.getDivContext().getSubjectId2SubjectMap().get(this.subjectId);
		
		System.out.println("===教学班级："+subject.getName()+subject.getId()+"["+ this.getId()+"]" + " 总人数:"+this.getTotalStudent());
		
		for (Entry<String, Collection<Student>> wishStudentEntry : this.getWishId2StudentList().asMap().entrySet()) {
			String expandWishId =  wishStudentEntry.getKey();
			String parentWishId = null;
			if(expandWishId.indexOf(Utils.SEPARATOR_SYMBOL)!=-1) {
				 String temp=expandWishId.split(Utils.SEPARATOR_SYMBOL)[1];
				 parentWishId=temp.split(Utils.WISH_SEPARATOR_SYMBOL)[0];
			}else {
				parentWishId =expandWishId.split(Utils.WISH_SEPARATOR_SYMBOL)[0];
			}
			
			SubjectGroup wish = this.getDivContext().getWishId2SubjectGroupMap().get(parentWishId);
			List<String> ids = Lists.transform(new ArrayList<Student>(wishStudentEntry.getValue()), new Function<Student, String>() {
				public String apply(Student input) {
					return input.getId();
				}
			});
			//String str = "  "+wish.getName()+" "+expandWishId+" 人数:"+ids.size()+" 列表："+ids;
			String str = "  "+wish.getName()+" "+expandWishId+" 人数:"+ids.size();
			System.out.println(str);
		}
	}


	public String getSubjectId() {
		return subjectId;
	}


	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	
	
	

}
