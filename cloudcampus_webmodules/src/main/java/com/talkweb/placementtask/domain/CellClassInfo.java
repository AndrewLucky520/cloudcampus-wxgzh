package com.talkweb.placementtask.domain;
/**
 * 班级安排的时候，用来保存每一个学、选序列下指定科目的组合安排情况
 * (每个Table格子中的数据格式是List<CellClassInfo>)
 * */
public class CellClassInfo {
	//科目id
	private String subjectId;
	//行id
	private Integer row;
	//志愿id(注意不同的行志愿id可能一样)
	private String wishId;
	//本志愿学生
	private Integer Num;
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public Integer getRow() {
		return row;
	}
	public void setRow(Integer row) {
		this.row = row;
	}
	public String getWishId() {
		return wishId;
	}
	public void setWishId(String wishId) {
		this.wishId = wishId;
	}
	public Integer getNum() {
		return Num;
	}
	public void setNum(Integer num) {
		Num = num;
	}
	
}
