package com.talkweb.placementtask.domain;
/**
 * 计算学选交叉情况的时候，用来保存每一个Table格子中数据的信息
 * (每个Table格子中的数据格式是List<CellCourseInfo>)
 * */
public class CellCourseInfo {
	//科目id
	private String subjectId;
	//科目学选类型(选考为1，学考为2)
	private Integer subjectType;
	//格子对应的行(从1开始,前面是选后面是学)
	private Integer cellRowNum;
	//格子对应的列(从1开始,对应Excel表中的列)
	private Integer cellColumnNum;
	
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public Integer getSubjectType() {
		return subjectType;
	}
	public void setSubjectType(Integer subjectType) {
		this.subjectType = subjectType;
	}
	public Integer getCellRowNum() {
		return cellRowNum;
	}
	public void setCellRowNum(Integer cellRowNum) {
		this.cellRowNum = cellRowNum;
	}
	public Integer getCellColumnNum() {
		return cellColumnNum;
	}
	public void setCellColumnNum(Integer cellColumnNum) {
		this.cellColumnNum = cellColumnNum;
	}
	
	
}
