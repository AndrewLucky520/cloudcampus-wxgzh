package com.talkweb.placementtask.utils.div.medium.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 分层信息
 * @author hushow
 *
 */
public class LayInfo {
	
	private Integer id;
	private String name;
	private List<String> wishGroupIds = new ArrayList<>();
	
	public LayInfo(Integer id, String name, List<String> wishGroupIds) {
		this.id = id;
		this.name = name;
		this.wishGroupIds = wishGroupIds;
	}
	
	public LayInfo(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<String> getWishGroupIds() {
		return wishGroupIds;
	}

	public void setWishGroupIds(List<String> wishGroupIds) {
		this.wishGroupIds = wishGroupIds;
	}

}
