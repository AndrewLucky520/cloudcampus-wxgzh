package com.talkweb.placementtask.utils.div.dto;

public class StudentbaseInfo {
	
	private String id;
	private String name;
	private String wishId;
	
	public StudentbaseInfo(String id, String name) {
		this.id = id;
		this.name = name;
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

	public String getWishId() {
		return wishId;
	}

	public void setWishId(String wishId) {
		this.wishId = wishId;
	}
}