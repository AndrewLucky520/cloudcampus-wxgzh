package com.talkweb.discipline.domain;

import java.io.Serializable;

public class DisciplineExcel implements Serializable{

 
	private static final long serialVersionUID = -3922221220673777465L;
	
 
	private String disciplineId;
	private String schoolId;
	private String en_disci;
	private String zh_disci;
	private String pen_disci;//父表头代码
	private Integer rowspan = 1;
	private Integer colspan = 1;

	//所在行
	private Integer in_rowNum;
	//所在列
	private Integer in_colNum;
	//表头行数
	private Integer head_rowNum;
 
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
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
	
	@Override
	public String toString() {
		return "DisciplineExcel [disciplineId=" + disciplineId + ", schoolId=" + schoolId
				+ ", en_esti=" + en_disci + ", zh_esti=" + zh_disci + ", pen_esti="
				+ pen_disci + ", rowspan=" + rowspan + ", colspan=" + colspan
				+ ", in_rowNum=" + in_rowNum + ", in_colNum=" + in_colNum
				+ ", head_rowNum=" + head_rowNum + "]";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((en_disci == null) ? 0 : en_disci.hashCode());
		result = prime * result + ((pen_disci == null) ? 0 : pen_disci.hashCode());
		result = prime * result + ((rowspan == null) ? 0 : rowspan.hashCode());
		result = prime * result
				+ ((disciplineId == null) ? 0 : disciplineId.hashCode());
		result = prime * result + ((zh_disci == null) ? 0 : zh_disci.hashCode());
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
		DisciplineExcel other = (DisciplineExcel) obj;
		if (en_disci == null) {
			if (other.en_disci != null)
				return false;
		} else if (!en_disci.equals(other.en_disci))
			return false;
		if (pen_disci == null) {
			if (other.pen_disci != null)
				return false;
		} else if (!pen_disci.equals(other.pen_disci))
			return false;
		if (rowspan == null) {
			if (other.rowspan != null)
				return false;
		} else if (!rowspan.equals(other.rowspan))
			return false;
		if (disciplineId == null) {
			if (other.disciplineId != null)
				return false;
		} else if (!disciplineId.equals(other.disciplineId))
			return false;
		if (zh_disci == null) {
			if (other.zh_disci != null)
				return false;
		} else if (!zh_disci.equals(other.zh_disci))
			return false;
		return true;
	}
	public String getDisciplineId() {
		return disciplineId;
	}
	public void setDisciplineId(String disciplineId) {
		this.disciplineId = disciplineId;
	}
	public String getEn_disci() {
		return en_disci;
	}
	public void setEn_disci(String en_disci) {
		this.en_disci = en_disci;
	}
	public String getZh_disci() {
		return zh_disci;
	}
	public void setZh_disci(String zh_disci) {
		this.zh_disci = zh_disci;
	}
	public String getPen_disci() {
		return pen_disci;
	}
	public void setPen_disci(String pen_disci) {
		this.pen_disci = pen_disci;
	}
	
 
	
}
