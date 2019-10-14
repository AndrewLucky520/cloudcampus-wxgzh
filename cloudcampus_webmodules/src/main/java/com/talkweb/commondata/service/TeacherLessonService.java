package com.talkweb.commondata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface TeacherLessonService {

	int updateTeacherLesson(String extSchoolId,String extTeacherId, List<JSONObject> classLessons, String accessToken, String clientId) throws Exception;

	int updateTeacherLessonBatch(String extSchoolId,List<JSONObject> teacherClassLessons, String accessToken, String clientId)throws Exception;

	int updateClassroom(String extSchoolId,String extClassId, String extHeadClassTeacherId, List<JSONObject> teacherLessons,
			String accessToken, String clientId)throws Exception;
	
}
