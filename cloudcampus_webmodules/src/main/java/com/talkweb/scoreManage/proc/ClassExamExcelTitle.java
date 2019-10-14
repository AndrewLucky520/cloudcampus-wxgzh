package com.talkweb.scoreManage.proc;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 工资单表头信息，包括工资单Id，学校Id，工资单元格编码（例如r0c1）,
 * 工资单元格中文名称，工资单上级头单元格编码，行跨度，列跨度，所在行，所在列，表头行数等
 * 
 * @author WXQ
 *
 */
public class ClassExamExcelTitle implements Serializable {
	private static final long serialVersionUID = -9018441586861895595L;
	private String examId;
	private String schoolId;
	private String cellId;
	private String titleName;
	private String p_cellId;	// 父表头代码
	private Integer rowspan = 1;
	private Integer colspan = 1;
	private String xnxq;
	// 所在行
	private Integer in_rowNum;
	// 所在列
	private Integer in_colNum;
	// 表头行数
	private Integer head_rowNum;

	public String getExamId() {
		return examId;
	}

	public void setExamId(String examId) {
		this.examId = examId;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getCellId() {
		return cellId;
	}

	public void setCellId(String cellId) {
		this.cellId = cellId;
	}

	public String getTitleName() {
		return titleName;
	}

	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	public String getP_cellId() {
		return p_cellId;
	}

	public void setP_cellId(String p_cellId) {
		this.p_cellId = p_cellId;
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

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cellId == null) ? 0 : cellId.hashCode());
		result = prime * result + ((p_cellId == null) ? 0 : p_cellId.hashCode());
		result = prime * result + ((rowspan == null) ? 0 : rowspan.hashCode());
		result = prime * result + ((examId == null) ? 0 : examId.hashCode());
		result = prime * result + ((titleName == null) ? 0 : titleName.hashCode());
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
		ClassExamExcelTitle other = (ClassExamExcelTitle) obj;
		if (cellId == null) {
			if (other.cellId != null)
				return false;
		} else if (!cellId.equals(other.cellId))
			return false;
		if (p_cellId == null) {
			if (other.p_cellId != null)
				return false;
		} else if (!p_cellId.equals(other.p_cellId))
			return false;
		if (rowspan == null) {
			if (other.rowspan != null)
				return false;
		} else if (!rowspan.equals(other.rowspan))
			return false;
		if (examId == null) {
			if (other.examId != null)
				return false;
		} else if (!examId.equals(other.examId))
			return false;
		if (titleName == null) {
			if (other.titleName != null)
				return false;
		} else if (!titleName.equals(other.titleName))
			return false;
		return true;
	}
}
