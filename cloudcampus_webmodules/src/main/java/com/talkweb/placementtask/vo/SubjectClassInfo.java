package com.talkweb.placementtask.vo;

import org.springframework.util.Assert;

import com.talkweb.base.common.PlacementConstant.ClassLevel;

public class SubjectClassInfo {
	
	/**
	 * 科目名称
	 */
	private String subjectName;
	
	
	/**
	 * 教学班类型(选考、学考)
	 */
	private Integer classType;
	
	/**
	 * 所属班级序列
	 */
	private Integer classSeq;
	
	/**
	 * 班级场地
	 */
	private String classSiteName;
	
	/**
	 * 班级名称
	 */
	private String className;

	
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}


	public Integer getClassType() {
		return classType;
	}
	
	
	public void setClassType(String classType) {
		Assert.hasText(classType, this.getSubjectName()+"科目班级类型不能为空");
		ClassLevel level = ClassLevel.getEnumByLabel(classType);
		Assert.notNull(level, this.getSubjectName()+"科目教学班类型不正确");
		this.classType = level.getCode();
	}


	public Integer getClassSeq() {
		return classSeq;
	}

	
	public void setClassSeq(String classSeq) {
		Assert.notNull(classSeq, this.getSubjectName()+"科目所属班级序列不能为空");
		try {
			this.classSeq = Integer.parseInt(classSeq);
		} catch (Exception e) {
			throw new IllegalArgumentException(this.getSubjectName()+"科目所属班级序列类型错误");
		}
	}


	public String getClassSiteName() {
		return classSiteName;
	}


	public void setClassSiteName(String classSiteName) {
		Assert.notNull(classSiteName, this.getSubjectName()+"科目场地不能为空");
		this.classSiteName = classSiteName.trim();
	}


	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		Assert.hasText(className, this.getSubjectName()+"科目班级名称不能为空");
		this.className = className.trim();
	}
	
}
