package com.talkweb.student.service;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.ExcelToolDemo.Student;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.domain.business.TSsClassenrol;
import com.talkweb.student.domain.business.TSsStudenrol;
import com.talkweb.student.domain.business.TSsStudent;

public interface StudentImportService {

    public void insertStudent(Student stu);

    public JSONObject getAllStuXJHBySchoolNum(HashMap map);

    public void updateStuStatusByParam(HashMap map1) throws Exception;

    public void insertStuList(List<TSsStudent> needInsert) throws Exception;

    public void updateStuList(List<TSsStudent> needUpdate) throws Exception;

    public void updateStuEnrolList(List<TSsStudenrol> needIOUStuEnrol) throws Exception;

    public void deleteStuEnrol(List<TSsStudenrol> delStuEnrol) throws Exception;

    public void insertClassEnrolList(List<TSsClassenrol> needInsertClassEnrol) throws Exception;

    public void insertClassList(List<TSsClass> needInsertClass) throws Exception;
}
