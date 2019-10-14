package com.talkweb.workbench.service;

import java.util.Date;
/**
 * 待办事项
 * @author Administrator
 *
 */
public class PendingItem {
	public enum ModualType {
		/**学生选科 */
		COURSE,
		/** 3+3选科 */
		WISH_FILLING,
	    /** 教学考评 */
	    EVALUATION, 
	    /**问卷调查 */
	    QUESTIONNAIRE;
	}
	ModualType modualType;
	String content;
	Date startDate;
	Date endDate;
	String targetUrl;
	
	public ModualType getModualType() {
		return modualType;
	}
	public void setModualType(ModualType modualType) {
		this.modualType = modualType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	
}
