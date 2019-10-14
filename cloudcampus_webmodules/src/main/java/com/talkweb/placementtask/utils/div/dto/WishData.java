package com.talkweb.placementtask.utils.div.dto;

public class WishData {
	
	private String wishName;
	
	private int studentCount;
	
	public WishData() {
		// TODO Auto-generated constructor stub
	}
	
	public WishData(String wishName, int studentCount) {
		this.wishName = wishName;
		this.studentCount = studentCount;
	}
	
	public String getWishName() {
		return wishName;
	}

	public void setWishName(String wishName) {
		this.wishName = wishName;
	}

	public int getStudentCount() {
		return studentCount;
	}

	public void setStudentCount(int studentCount) {
		this.studentCount = studentCount;
	}

	
	
	
	
}
