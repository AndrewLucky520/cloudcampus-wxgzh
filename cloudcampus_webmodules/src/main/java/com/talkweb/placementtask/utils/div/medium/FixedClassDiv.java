package com.talkweb.placementtask.utils.div.medium;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.dto.SubjectGroup;
import com.talkweb.placementtask.utils.div.medium.dto.FixedClassResult;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;


/**
 * 分固定班级
 * @author hushow
 *
 */
public class FixedClassDiv extends Divide<List<ClassResult>>{
	
	public FixedClassDiv(DivContext divContext) {
		
		super(divContext);
		
		
		int totalStudentCount = 0;
		for(SubjectGroup wishSubjectGroup: this.divContext.getWishSubjectGroupList()) {
			
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)wishSubjectGroup;
			
			if(wish.getStudentCount()<=0) continue;
			
			Cell cell = new Cell(Cell.ONE_ROW_KEY, wish.getId(), wish.getFixedStudentCount());
			
			//初始化固定人数(后续可能增加成绩、区间等均衡判断 )
			List<Student> extracTstudents = wish.getStudents().subList(0, wish.getFixedStudentCount());
			
			//固定
			wish.setFixedStudents(new ArrayList<>(extracTstudents));
			cell.setStudents(new ArrayList<>(extracTstudents));
			totalStudentCount+=wish.getFixedStudentCount();
			
			//走班
			List<Student> tmpList = new ArrayList<>(wish.getStudents());
			tmpList.removeAll(extracTstudents);
			wish.setGoStudents(tmpList);
			
			this.table.put(Cell.ONE_ROW_KEY,  wish.getId(), cell);
		}
		
		this.setTotalStudentCount(totalStudentCount);
		
	}
	
	
	@Override
	public List<ClassResult> excuteDiv() {
		
		List<ClassResult> classList = new  ArrayList<>();
		
		System.out.println("===========================开始分固定班级,参与总人数:"+this.getTotalStudentCount());
		
		this.printInfo();
		
		//循环志愿开固定班级
		for(SubjectGroup wishSubjectGroup: this.divContext.getWishSubjectGroupList()) {
			
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)wishSubjectGroup;
			if(wish.getFixedStudentCount()<=0) continue;
			Collection<Cell> items = this.getColumnValidCells(wish.getId());
			Double  avgSize = Math.ceil(wish.getFixedStudentCount()/wish.getFixedClassCount().doubleValue());
			
			List<LinkedList<Cell>> splitList = Utils.splitSubClass(items, avgSize.intValue());
			
			List<ClassResult> cellClasslist = this.extractStudent(wish.getId(), splitList);
			
			classList.addAll(cellClasslist);
		}
		
		
		for (ClassResult classResult : classList) {
			classResult.print();
		}
		
		return classList;
	}
	
	
	/**
	 * 取人
	 * @param cellList
	 * @return
	 */
	public List<ClassResult> extractStudent(String wishId, List<LinkedList<Cell>> cellList) {
		
		List<ClassResult> classLists = new ArrayList<ClassResult>();
		
		for (LinkedList<Cell> oneClassList : cellList) {
			
			//开班
			FixedClassResult classResult = new FixedClassResult(divContext, wishId, oneClassList);
			
			for (Cell learnCell : oneClassList) {
				Cell cell = this.table.get(learnCell.getRowKey(), learnCell.getColumnKey());
				List<Student> extracTstudents = cell.getStudents().subList(0, learnCell.getStudentCount());
				classResult.getWishId2StudentList().putAll(cell.getColumnKey(), new ArrayList<Student>(extracTstudents));
				cell.getStudents().removeAll(extracTstudents);
			}
			
			classLists.add(classResult);
		}
		
		return classLists;
	}
	
	
	/**
	 * 打印数据
	 */
	@Override
	public void  printInfo() {
		
		
	}
	
	
}
