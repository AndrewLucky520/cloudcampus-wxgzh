package com.talkweb.placementtask.utils.div.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 单元格
 * @author hushowly@foxmail.com
 *
 */
public class Cell{
	
	public static final String ONE_ROW_KEY = "-1";
	
	private String id;
	
	private String rowKey;
	
	private String columnKey;

	private int studentCount = 0;
	
	private Boolean isNeed = false;
	
	private List<Student> students = new ArrayList<Student>();
	
	private String splitWishId;
	
	public Cell() {
		this.id = UUID.randomUUID().toString();
	}
	
	public Cell(Cell cell) {
		this.id = UUID.randomUUID().toString();
		this.rowKey = cell.getRowKey();
		this.columnKey = cell.getColumnKey();
		this.studentCount = cell.getStudentCount();
	}
	
	public Cell(String rowKey, String columnKey) {
		this.id = UUID.randomUUID().toString();
		this.rowKey = rowKey;
		this.columnKey = columnKey;
	}
	
	
	public Cell(String rowKey, String columnKey, int studentCount) {
		this.id = UUID.randomUUID().toString();
		this.rowKey = rowKey;
		this.columnKey = columnKey;
		this.studentCount = studentCount;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRowKey() {
		return rowKey;
	}

	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}

	public String getColumnKey() {
		return columnKey;
	}

	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}

	public int getStudentCount() {
		return studentCount;
	}

	public void setStudentCount(int studentCount) {
		this.studentCount = studentCount;
	}


	public List<Student> getStudents() {
		return students;
	}


	public void setStudents(List<Student> students) {
		this.students = students;
	}

	public Boolean getIsNeed() {
		return isNeed;
	}

	public void setIsNeed(Boolean isNeed) {
		this.isNeed = isNeed;
	}
	
	

	public String getSplitWishId() {
		return splitWishId;
	}

	public void setSplitWishId(String splitWishId) {
		this.splitWishId = splitWishId;
	}

	@Override
	public String toString() {
		return String.valueOf(this.rowKey+":"+this.columnKey+":"+this.studentCount);
	}
	
	
}
