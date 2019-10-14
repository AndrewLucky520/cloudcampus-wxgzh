package com.talkweb.complexquality.service;

import java.util.List;
import java.util.Map;

public interface EvalutionService 
{
	   public  List<Map<String,Object>> getStudentReative(Map<String,Object> param);
	   public  int addStudentReative(Map<String,Object> param);
	   public  int updateStudentReative(Map<String,Object> param);
}
