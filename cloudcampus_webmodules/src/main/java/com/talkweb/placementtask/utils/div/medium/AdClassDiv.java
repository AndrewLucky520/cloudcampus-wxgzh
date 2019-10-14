package com.talkweb.placementtask.utils.div.medium;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.talkweb.placementtask.utils.div.Divide;
import com.talkweb.placementtask.utils.div.Utils;
import com.talkweb.placementtask.utils.div.dto.Cell;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.DivContext;
import com.talkweb.placementtask.utils.div.dto.Student;
import com.talkweb.placementtask.utils.div.medium.dto.LayInfo;
import com.talkweb.placementtask.utils.div.medium.dto.MediumWishSubjectGroup;


/**
 * 分行政玫(后续非走班班级要考虑按成绩、区间均衡来分)
 * @author hushow
 *
 */
public class AdClassDiv extends Divide<List<ClassResult>> {
	
	/**
	 * 分层信息
	 */
	private LayInfo layInfo;
	
	
	public AdClassDiv(DivContext divContext, LayInfo layInfo) {
		
		super(divContext);
		
		this.layInfo = layInfo;
		
		//初始化分层志愿数据
		int totalStudentCount=0;
		for (String wishId : layInfo.getWishGroupIds()) {
			MediumWishSubjectGroup wish = (MediumWishSubjectGroup)divContext.getWishId2SubjectGroupMap().get(wishId);
			Cell cell = new Cell(Cell.ONE_ROW_KEY, wishId, wish.getGoStudentCount());
			cell.setStudents(new ArrayList<>(wish.getGoStudents()));
			this.table.put(Cell.ONE_ROW_KEY, wishId, cell);
			totalStudentCount+=wish.getGoStudentCount();
		}
		this.setTotalStudentCount(totalStudentCount);
	}
	
	
	@Override
	public List<ClassResult> excuteDiv() {
		
		System.out.println("==========================="+layInfo.getName()+"开始分行政班级，参与人数:"+this.getTotalStudentCount());
		this.printInfo();
		
		Integer total = 0; 
		List<Cell> cellList = new ArrayList<>();
		for (com.google.common.collect.Table.Cell<String, String, Cell> entry : this.table.cellSet()) {
			if(entry.getValue().getStudentCount()<=0) continue;
			cellList.add(entry.getValue());
			total+=entry.getValue().getStudentCount();
		}
		
		Double upClassCount = Math.ceil(total.doubleValue()/divContext.getMaxClassSize());
		Double avgSize = Math.ceil(total.doubleValue()/upClassCount);
		List<LinkedList<Cell>> splitList = Utils.splitSubClass(cellList, avgSize.intValue());
		List<ClassResult> classList = this.extractStudent(splitList);
		
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
	public List<ClassResult> extractStudent(List<LinkedList<Cell>> cellList) {
		
		List<ClassResult> classLists = new ArrayList<ClassResult>();
		
		for (LinkedList<Cell> oneClassList : cellList) {
			
			//开班
			ClassResult classResult = new ClassResult(divContext, oneClassList);
			
			for (Cell learnCell : oneClassList) {
				Cell cell = this.table.get(learnCell.getRowKey(), learnCell.getColumnKey());
				List<Student> extracTstudents = cell.getStudents().subList(0, learnCell.getStudentCount());
				classResult.getWishId2StudentList().putAll(cell.getColumnKey(), new ArrayList<Student>(extracTstudents));
				cell.getStudents().removeAll(extracTstudents);
				cell.setStudentCount(cell.getStudentCount()-learnCell.getStudentCount());
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

	public LayInfo getLayInfo() {
		return layInfo;
	}

	public void setLayInfo(LayInfo layInfo) {
		this.layInfo = layInfo;
	}
}
