package com.talkweb.scoreManage.proc;

import java.io.Serializable;

public class ExcelTitleInfo implements Serializable {
	private static final long serialVersionUID = -2532037091001132553L;

	private String excelData = null;

	private String sysTitleName = null;
	private String sysTitleCode = null;
	private int sysTitleNeed = 0;

	private long sort = 0;

	public ExcelTitleInfo(String sysTitleName, String sysTitleCode, int sysTitleNeed, long sort) {
		this.sysTitleName = sysTitleName;
		this.sysTitleCode = sysTitleCode;
		this.sysTitleNeed = sysTitleNeed;
		this.sort = sort;
	}

	public long getSort() {
		return sort;
	}

	public void setSort(long sort) {
		this.sort = sort;
	}

	public String getExcelData() {
		return excelData;
	}

	public void setExcelData(String excelData) {
		this.excelData = excelData;
	}

	public String getSysTitleName() {
		return sysTitleName;
	}

	public void setSysTitleName(String sysTitleName) {
		this.sysTitleName = sysTitleName;
	}

	public String getSysTitleCode() {
		return sysTitleCode;
	}

	public void setSysTitleCode(String sysTitleCode) {
		this.sysTitleCode = sysTitleCode;
	}

	public int getSysTitleNeed() {
		return sysTitleNeed;
	}

	public void setSysTitleNeed(int sysTitleNeed) {
		this.sysTitleNeed = sysTitleNeed;
	}
}
