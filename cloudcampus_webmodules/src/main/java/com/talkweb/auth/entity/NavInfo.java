package com.talkweb.auth.entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 菜单信息
 * @author talkweb
 *
 */
public class NavInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3344137712600559015L;
	private String navId;
	private String navName;
	private int sort;
	private String target;
	private String navUrl;
	private String iconUrl;
	private String parentId;
	private String permissionId;
	private String isOutLink;
	
	public String getIsOutLink() {
		return isOutLink;
	}
	public void setIsOutLink(String isOutLink) {
		this.isOutLink = isOutLink;
	}
	public String getPermissionId() {
		return permissionId;
	}
	public void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}
	public String getNavId() {
		return navId;
	}
	public void setNavId(String navId) {
		this.navId = navId;
	}
	public String getNavName() {
		return navName;
	}
	public void setNavName(String navName) {
		this.navName = navName;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getNavUrl() {
		return navUrl;
	}
	public void setNavUrl(String navUrl) {
		this.navUrl = navUrl;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
