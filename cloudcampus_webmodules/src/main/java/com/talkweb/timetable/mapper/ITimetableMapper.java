package com.talkweb.timetable.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.talkweb.timetable.bo.TaskTeacherSetBo;
import com.talkweb.timetable.bo.TimetableBo;
import com.talkweb.timetable.bo.TimetableGradeSetBO;
import com.talkweb.utils.SqlMapper;

@SqlMapper
public interface ITimetableMapper {
	
	TimetableGradeSetBO getTimetableGradeSetById(String timetableId);
	
	
	List<TaskTeacherSetBo> listTaskTeacherByTimetableId(String timetableId);
	
	
	List<TimetableBo> listPublishedTimetable(@Param("schoolId") String schoolId, @Param("termInfo") String termInfo);
	
	
	TimetableBo getLastPublishedTimetableByClassId(@Param("schoolId") String schoolId, @Param("termInfo") String termInfo, @Param("classId") String classId);
	
}
