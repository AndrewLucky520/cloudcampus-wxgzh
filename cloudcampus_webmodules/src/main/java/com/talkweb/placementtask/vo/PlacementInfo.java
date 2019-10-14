package com.talkweb.placementtask.vo;

import com.talkweb.common.tools.ExcelVOAttribute;

/**
 * 分班信息导入实体
 * @author hushowly@foxmail.com
 */
public class PlacementInfo {
	
	
	@ExcelVOAttribute(name= "基础数据班级", column="A")
	private String baseClassName;
	
	@ExcelVOAttribute(name= "学生", column="B")
	private String studentName;
	
	@ExcelVOAttribute(name= "志愿组合", column="C")
	private String subjectCombination;
	
	@ExcelVOAttribute(name= "志愿科目id", column="D")
	private String subjectCombinationIds;
	
	
	/**
	 * 行政班
	 */
	@ExcelVOAttribute(name= "行政班场地", column="E")
	private String adminClassSiteName;
	
	@ExcelVOAttribute(name= "行政班", column="F")
	private String adminClassName;
	
	
	/**
	 * 政治
	 */
	@ExcelVOAttribute(name= "政治班级类型", column="G")
	private String politicsClassType;

	@ExcelVOAttribute(name= "政治所属班级序列", column="H")
	private String politicsClassSeq;
	
	@ExcelVOAttribute(name= "政治班级场地", column="I")
	private String politicsClassSite;
	
	@ExcelVOAttribute(name= "政治班级名称", column="J")
	private String politicsClassName;
	
	
	/**
	 * 历史
	 */
	@ExcelVOAttribute(name= "历史班级类型", column="K")
	private String historyClassType;

	@ExcelVOAttribute(name= "历史所属班级序列", column="L")
	private String historyClassSeq;
	
	@ExcelVOAttribute(name= "历史班级场地", column="M")
	private String historyClassSite;
	
	@ExcelVOAttribute(name= "历史班级名称", column="N")
	private String historyClassName;
	
	
	/**
	 * 地理
	 */
	@ExcelVOAttribute(name= "地理班级类型", column="O")
	private String geographyClassType;

	@ExcelVOAttribute(name= "地理所属班级序列", column="P")
	private String geographyClassSeq;
	
	@ExcelVOAttribute(name= "地理班级场地", column="Q")
	private String geographyClassSite;
	
	@ExcelVOAttribute(name= "地理班级名称", column="R")
	private String geographyClassName;
	
	
	/**
	 * 物理
	 */
	@ExcelVOAttribute(name= "物理班级类型", column="S")
	private String physicsClassType;

	@ExcelVOAttribute(name= "物理所属班级序列", column="T")
	private String physicsClassSeq;
	
	@ExcelVOAttribute(name= "物理班级场地", column="U")
	private String physicsClassSite;
	
	@ExcelVOAttribute(name= "物理班级名称", column="V")
	private String physicsClassName;

	
	/**
	 * 化学
	 */
	@ExcelVOAttribute(name= "化学班级类型", column="W")
	private String chemistryClassType;

	@ExcelVOAttribute(name= "化学所属班级序列", column="X")
	private String chemistryClassSeq;
	
	@ExcelVOAttribute(name= "化学场地", column="Y")
	private String chemistryClassSite;
	
	@ExcelVOAttribute(name= "化学班级名称", column="Z")
	private String chemistryClassName;
	
	
	/**
	 * 生物
	 */
	@ExcelVOAttribute(name= "生物班级类型", column="AA")
	private String biologyClassType;

	@ExcelVOAttribute(name= "生物所属班级序列", column="AB")
	private String biologyClassSeq;
	
	@ExcelVOAttribute(name= "生物场地", column="AC")
	private String biologyClassSite;
	
	@ExcelVOAttribute(name= "生物班级", column="AD")
	private String biologyClassName;
	
	
	/**
	 * 技术
	 */
	@ExcelVOAttribute(name= "技术班级类型", column="AE")
	private String technicalClassType;

	@ExcelVOAttribute(name= "技术所属班级序列", column="AF")
	private String technicalClassSeq;
	
	@ExcelVOAttribute(name= "技术场地", column="AG")
	private String technicalClassSite;
	
	@ExcelVOAttribute(name= "技术班级", column="AH")
	private String technicalClassName;
	

	public String getBaseClassName() {
		return baseClassName;
	}

	public void setBaseClassName(String baseClassName) {
		this.baseClassName = baseClassName;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public String getSubjectCombination() {
		return subjectCombination;
	}

	public void setSubjectCombination(String subjectCombination) {
		this.subjectCombination = subjectCombination;
	}

	public String getSubjectCombinationIds() {
		return subjectCombinationIds;
	}

	public void setSubjectCombinationIds(String subjectCombinationIds) {
		this.subjectCombinationIds = subjectCombinationIds;
	}



	public String getAdminClassSiteName() {
		return adminClassSiteName;
	}

	public void setAdminClassSiteName(String adminClassSiteName) {
		this.adminClassSiteName = adminClassSiteName;
	}

	public String getAdminClassName() {
		return adminClassName;
	}

	public void setAdminClassName(String adminClassName) {
		this.adminClassName = adminClassName;
	}

	public String getPoliticsClassType() {
		return politicsClassType;
	}

	public void setPoliticsClassType(String politicsClassType) {
		this.politicsClassType = politicsClassType;
	}

	public String getPoliticsClassSeq() {
		return politicsClassSeq;
	}

	public void setPoliticsClassSeq(String politicsClassSeq) {
		this.politicsClassSeq = politicsClassSeq;
	}

	public String getPoliticsClassSite() {
		return politicsClassSite;
	}

	public void setPoliticsClassSite(String politicsClassSite) {
		this.politicsClassSite = politicsClassSite;
	}

	public String getPoliticsClassName() {
		return politicsClassName;
	}

	public void setPoliticsClassName(String politicsClassName) {
		this.politicsClassName = politicsClassName;
	}

	public String getHistoryClassType() {
		return historyClassType;
	}

	public void setHistoryClassType(String historyClassType) {
		this.historyClassType = historyClassType;
	}

	public String getHistoryClassSeq() {
		return historyClassSeq;
	}

	public void setHistoryClassSeq(String historyClassSeq) {
		this.historyClassSeq = historyClassSeq;
	}

	public String getHistoryClassSite() {
		return historyClassSite;
	}

	public void setHistoryClassSite(String historyClassSite) {
		this.historyClassSite = historyClassSite;
	}

	public String getHistoryClassName() {
		return historyClassName;
	}

	public void setHistoryClassName(String historyClassName) {
		this.historyClassName = historyClassName;
	}

	public String getGeographyClassType() {
		return geographyClassType;
	}

	public void setGeographyClassType(String geographyClassType) {
		this.geographyClassType = geographyClassType;
	}

	public String getGeographyClassSeq() {
		return geographyClassSeq;
	}

	public void setGeographyClassSeq(String geographyClassSeq) {
		this.geographyClassSeq = geographyClassSeq;
	}

	public String getGeographyClassSite() {
		return geographyClassSite;
	}

	public void setGeographyClassSite(String geographyClassSite) {
		this.geographyClassSite = geographyClassSite;
	}

	public String getGeographyClassName() {
		return geographyClassName;
	}

	public void setGeographyClassName(String geographyClassName) {
		this.geographyClassName = geographyClassName;
	}

	public String getPhysicsClassType() {
		return physicsClassType;
	}

	public void setPhysicsClassType(String physicsClassType) {
		this.physicsClassType = physicsClassType;
	}

	public String getPhysicsClassSeq() {
		return physicsClassSeq;
	}

	public void setPhysicsClassSeq(String physicsClassSeq) {
		this.physicsClassSeq = physicsClassSeq;
	}

	public String getPhysicsClassSite() {
		return physicsClassSite;
	}

	public void setPhysicsClassSite(String physicsClassSite) {
		this.physicsClassSite = physicsClassSite;
	}

	public String getPhysicsClassName() {
		return physicsClassName;
	}

	public void setPhysicsClassName(String physicsClassName) {
		this.physicsClassName = physicsClassName;
	}

	public String getChemistryClassType() {
		return chemistryClassType;
	}

	public void setChemistryClassType(String chemistryClassType) {
		this.chemistryClassType = chemistryClassType;
	}

	public String getChemistryClassSeq() {
		return chemistryClassSeq;
	}

	public void setChemistryClassSeq(String chemistryClassSeq) {
		this.chemistryClassSeq = chemistryClassSeq;
	}

	public String getChemistryClassSite() {
		return chemistryClassSite;
	}

	public void setChemistryClassSite(String chemistryClassSite) {
		this.chemistryClassSite = chemistryClassSite;
	}

	

	public String getChemistryClassName() {
		return chemistryClassName;
	}

	public void setChemistryClassName(String chemistryClassName) {
		this.chemistryClassName = chemistryClassName;
	}

	public String getBiologyClassType() {
		return biologyClassType;
	}

	public void setBiologyClassType(String biologyClassType) {
		this.biologyClassType = biologyClassType;
	}

	public String getBiologyClassSeq() {
		return biologyClassSeq;
	}

	public void setBiologyClassSeq(String biologyClassSeq) {
		this.biologyClassSeq = biologyClassSeq;
	}

	public String getBiologyClassSite() {
		return biologyClassSite;
	}

	public void setBiologyClassSite(String biologyClassSite) {
		this.biologyClassSite = biologyClassSite;
	}

	public String getBiologyClassName() {
		return biologyClassName;
	}

	public void setBiologyClassName(String biologyClassName) {
		this.biologyClassName = biologyClassName;
	}

	public String getTechnicalClassType() {
		return technicalClassType;
	}

	public void setTechnicalClassType(String technicalClassType) {
		this.technicalClassType = technicalClassType;
	}

	public String getTechnicalClassSeq() {
		return technicalClassSeq;
	}

	public void setTechnicalClassSeq(String technicalClassSeq) {
		this.technicalClassSeq = technicalClassSeq;
	}

	public String getTechnicalClassSite() {
		return technicalClassSite;
	}

	public void setTechnicalClassSite(String technicalClassSite) {
		this.technicalClassSite = technicalClassSite;
	}

	public String getTechnicalClassName() {
		return technicalClassName;
	}

	public void setTechnicalClassName(String technicalClassName) {
		this.technicalClassName = technicalClassName;
	}
}
