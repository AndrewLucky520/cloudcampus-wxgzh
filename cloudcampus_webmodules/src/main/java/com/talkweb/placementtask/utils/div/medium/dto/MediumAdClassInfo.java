package com.talkweb.placementtask.utils.div.medium.dto;

import com.talkweb.placementtask.domain.ClassInfo;

/**
 * 中走班行政班级信息
 * @author hushow
 *
 */
public class MediumAdClassInfo  extends ClassInfo{
	
	/**
	 * 志愿id
	 */
	private String wishId;
	
	/**
	 * 志愿名称
	 */
	private String wishName;

	public String getWishId() {
		return wishId;
	}

	public void setWishId(String wishId) {
		this.wishId = wishId;
	}

	public String getWishName() {
		return wishName;
	}

	public void setWishName(String wishName) {
		this.wishName = wishName;
	}
	
	
	
}
