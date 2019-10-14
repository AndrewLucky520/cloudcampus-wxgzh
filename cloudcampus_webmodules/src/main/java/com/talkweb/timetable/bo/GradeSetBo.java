package com.talkweb.timetable.bo;

/**
 * 年级设置
 * @author hushowly@foxmail.com
 *
 */
public class GradeSetBo {
	
	private String gradeId;
	
	private Integer amLessonNum;
	
	private Integer pmLessonNum;
	
	private String generated;

	public String getGradeId() {
		return gradeId;
	}

	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

	public Integer getAmLessonNum() {
		return amLessonNum;
	}

	public void setAmLessonNum(Integer amLessonNum) {
		this.amLessonNum = amLessonNum;
	}

	public Integer getPmLessonNum() {
		return pmLessonNum;
	}

	public void setPmLessonNum(Integer pmLessonNum) {
		this.pmLessonNum = pmLessonNum;
	}

	public String getGenerated() {
		return generated;
	}

	public void setGenerated(String generated) {
		this.generated = generated;
	}
	
	
	
}
