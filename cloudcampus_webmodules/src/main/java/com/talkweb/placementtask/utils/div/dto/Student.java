package com.talkweb.placementtask.utils.div.dto;

public class Student {
	
	private String id;
	private String name;
	private String subjectIds;
	private String wishName;
	
	public Student() {
		// TODO Auto-generated constructor stub
	}
	
	public Student(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Student(String id, String name, String wishId, String wishName) {
		this.id = id;
		this.name = name;
		this.subjectIds = wishId;
		this.wishName = wishName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getSubjectIds() {
		return subjectIds;
	}

	public void setSubjectIds(String subjectIds) {
		this.subjectIds = subjectIds;
	}

	public String getWishName() {
		return wishName;
	}

	public void setWishName(String wishName) {
		this.wishName = wishName;
	}
	
	@Override
	public String toString() {
		return "Student [id=" + id + ", name=" + name + ", subjectIds=" + subjectIds + ", wishName=" + wishName + "]";
	}

}
