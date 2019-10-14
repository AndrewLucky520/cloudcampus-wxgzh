package com.talkweb.timetable.bo;

import java.util.List;

/**
 * 课表年级设置
 * @author hushowly@foxmail.com
 *
 */
public class TimetableGradeSetBO {
	
	private String timetableId;
	
	private String timetableName;
	
	private Integer maxDaysForWeek;
	
	private List<GradeSetBo> gradeSets;

	public String getTimetableId() {
		return timetableId;
	}

	public void setTimetableId(String timetableId) {
		this.timetableId = timetableId;
	}

	public String getTimetableName() {
		return timetableName;
	}

	public void setTimetableName(String timetableName) {
		this.timetableName = timetableName;
	}

	public Integer getMaxDaysForWeek() {
		return maxDaysForWeek;
	}

	public void setMaxDaysForWeek(Integer maxDaysForWeek) {
		this.maxDaysForWeek = maxDaysForWeek;
	}

	public List<GradeSetBo> getGradeSets() {
		return gradeSets;
	}

	public void setGradeSets(List<GradeSetBo> gradeSets) {
		this.gradeSets = gradeSets;
	}
	
	
}
