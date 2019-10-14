package com.talkweb.salary.domain;

import java.io.Serializable;

/**
 * 工资单表头信息，包括工资单Id，学校Id，工资单元格编码（例如r0c1）,
 * 工资单元格中文名称，工资单上级头单元格编码，行跨度，列跨度，所在行，所在列，表头行数等
 * @author WXQ
 *
 */
public class SalExcel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String salaryId;
	private String schoolId;
	private String en_sal;
	private String zh_sal;
	private String pen_sal;//父表头代码
	private Integer rowspan = 1;
	private Integer colspan = 1;
	private String xn;
	private String xqm;
	//所在行
	private Integer in_rowNum;
	//所在列
	private Integer in_colNum;
	//表头行数
	private Integer head_rowNum;
	
	public String getSalaryId() {
		return salaryId;
	}
	public void setSalaryId(String salaryId) {
		this.salaryId = salaryId;
	}
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getEn_sal() {
		return en_sal;
	}
	public void setEn_sal(String en_sal) {
		this.en_sal = en_sal;
	}
	public String getZh_sal() {
		return zh_sal;
	}
	public void setZh_sal(String zh_sal) {
		this.zh_sal = zh_sal;
	}
	public String getPen_sal() {
		return pen_sal;
	}
	public void setPen_sal(String pen_sal) {
		this.pen_sal = pen_sal;
	}
	public Integer getRowspan() {
		return rowspan;
	}
	public void setRowspan(Integer rowspan) {
		this.rowspan = rowspan;
	}
	public Integer getColspan() {
		return colspan;
	}
	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}
	
	public Integer getIn_rowNum() {
		return in_rowNum;
	}
	public void setIn_rowNum(Integer in_rowNum) {
		this.in_rowNum = in_rowNum;
	}
	public Integer getIn_colNum() {
		return in_colNum;
	}
	public void setIn_colNum(Integer in_colNum) {
		this.in_colNum = in_colNum;
	}
	public Integer getHead_rowNum() {
		return head_rowNum;
	}
	public void setHead_rowNum(Integer head_rowNum) {
		this.head_rowNum = head_rowNum;
	}
	
	public String getXn() {
		return xn;
	}
	public void setXn(String xn) {
		this.xn = xn;
	}
	public String getXqm() {
		return xqm;
	}
	public void setXqm(String xqm) {
		this.xqm = xqm;
	}
	@Override
	public String toString() {
		return "SalExcel [salaryId=" + salaryId + ", schoolId=" + schoolId
				+ ", en_sal=" + en_sal + ", zh_sal=" + zh_sal + ", pen_sal="
				+ pen_sal + ", rowspan=" + rowspan + ", colspan=" + colspan
				+ ", in_rowNum=" + in_rowNum + ", in_colNum=" + in_colNum
				+ ", head_rowNum=" + head_rowNum + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((en_sal == null) ? 0 : en_sal.hashCode());
		result = prime * result + ((pen_sal == null) ? 0 : pen_sal.hashCode());
		result = prime * result + ((rowspan == null) ? 0 : rowspan.hashCode());
		result = prime * result
				+ ((salaryId == null) ? 0 : salaryId.hashCode());
		result = prime * result + ((zh_sal == null) ? 0 : zh_sal.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SalExcel other = (SalExcel) obj;
		if (en_sal == null) {
			if (other.en_sal != null)
				return false;
		} else if (!en_sal.equals(other.en_sal))
			return false;
		if (pen_sal == null) {
			if (other.pen_sal != null)
				return false;
		} else if (!pen_sal.equals(other.pen_sal))
			return false;
		if (rowspan == null) {
			if (other.rowspan != null)
				return false;
		} else if (!rowspan.equals(other.rowspan))
			return false;
		if (salaryId == null) {
			if (other.salaryId != null)
				return false;
		} else if (!salaryId.equals(other.salaryId))
			return false;
		if (zh_sal == null) {
			if (other.zh_sal != null)
				return false;
		} else if (!zh_sal.equals(other.zh_sal))
			return false;
		return true;
	}
	
}
