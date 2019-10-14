package com.talkweb.student.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.ExcelToolDemo.Student;
import com.talkweb.student.dao.StudentImportDao;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.domain.business.TSsClassenrol;
import com.talkweb.student.domain.business.TSsStudenrol;
import com.talkweb.student.domain.business.TSsStudent;
import com.talkweb.student.service.StudentImportService;

@Service
public class StudentImportServiceImpl implements StudentImportService {

    @Autowired
    private StudentImportDao studentDao;
    
    @Override
    public void insertStudent(Student stu) {
        // TODO Auto-generated method stub
        studentDao.insertStudent(stu);
    }

    @Override
    public JSONObject getAllStuXJHBySchoolNum(HashMap map) {
        // TODO Auto-generated method stub
        return studentDao.getAllStuXJHBySchoolNum(map);
    }

    @Override
    public void updateStuStatusByParam(HashMap map1) throws Exception {
        // TODO Auto-generated method stub
        studentDao.updateStuStatusByParam( map1);
    }

    @Override
    public void insertStuList(List<TSsStudent> needInsert) throws Exception {
        // TODO Auto-generated method stub
        studentDao.insertStuList(needInsert);
    }

    @Override
    public void updateStuList(List<TSsStudent> needUpdate) throws Exception {
        // TODO Auto-generated method stub
        studentDao.updateStuList(needUpdate);
    }

    @Override
    public void updateStuEnrolList(List<TSsStudenrol> needIOUStuEnrol) throws Exception {
        // TODO Auto-generated method stub
        studentDao.updateStuEnrolList(needIOUStuEnrol);
    }

    @Override
    public void deleteStuEnrol(List<TSsStudenrol> delStuEnrol) throws Exception {
        // TODO Auto-generated method stub
        studentDao.deleteStuEnrol(delStuEnrol);
    }

    @Override
    public void insertClassEnrolList(List<TSsClassenrol> needInsertClassEnrol) throws Exception {
        // TODO Auto-generated method stub
        studentDao.insertClassEnrolList(needInsertClassEnrol);
    }

    @Override
    public void insertClassList(List<TSsClass> needInsertClass) throws Exception {
        // TODO Auto-generated method stub
        studentDao.insertClassList(needInsertClass);
    }

}
