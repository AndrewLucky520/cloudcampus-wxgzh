package com.talkweb.student.dao.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.ExcelToolDemo.Student;
import com.talkweb.student.dao.StudentImportDao;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.domain.business.TSsClassenrol;
import com.talkweb.student.domain.business.TSsStudenrol;
import com.talkweb.student.domain.business.TSsStudent;
/**
 * @ClassName: StudentDaoImpl.java	
 * @version:1.0
 * @Description: 学生信息导入管理DAO实现
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
@Repository
public class StudentImportDaoImpl  extends MyBatisBaseDaoImpl implements StudentImportDao {

    @Override
    public void insertStudent(Student stu) {
        // TODO Auto-generated method stub
        try {
            insert("insertStudent",stu);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getAllStuXJHBySchoolNum(HashMap map) {
        // TODO Auto-generated method stubr
        JSONObject res = new JSONObject();
        HashMap<String,String> xhMap = new HashMap<String, String>();
        HashMap<String,Integer> xjhMap = new HashMap<String, Integer>();
        HashMap<String,String> sfzhMap = new HashMap<String, String>();
        HashMap<String,String> keyMap = new HashMap<String, String>();
        HashMap<String,String> bjSynjMap = new HashMap<String, String>();
        HashMap<String,String> bjMcdmMap = new HashMap<String, String>();
        HashMap<String,JSONObject> bhBjMap = new HashMap<String, JSONObject>();
        HashMap<String,JSONObject> xjhStuInfo = new HashMap<String, JSONObject>();
        try {
            List<JSONObject> rs1 = selectList("getAllStuXH",map);
            for(int i=0;i<rs1.size();i++){
                JSONObject obj = rs1.get(i);
                xhMap.put(obj.getString("xh"), obj.getString("xjh"));
                if(xjhMap!=null&&xjhMap.containsKey(obj.getString("xjh"))){
                    int next = xjhMap.get(obj.getString("xjh"))+1;
                    xjhMap.put(obj.getString("xjh"), next);
                }else{
                    xjhMap.put(obj.getString("xjh"), 1);
                }
                if(obj.getString("sfzh")!=null){
                    sfzhMap.put(obj.getString("sfzh"), obj.getString("xjh"));
                }
                keyMap.put(obj.getString("xjh"),obj.getString("id"));
//                JSONObject stuObj = new JSONObject();
//                stuObj.put("rxnj", obj.getString("rxnj"));
//                stuObj.put("synj",obj.getString("synj"));
//                stuObj.put("pycc",obj.getString("pycc"));
                xjhStuInfo.put(obj.getString("xjh"),obj);
                
            }
            List<JSONObject> bjs = selectList("getAllBjDmNj",map);
            for(int i=0;i<bjs.size();i++){
                JSONObject obj = bjs.get(i);
                bjSynjMap.put(obj.getString("bjmc"), obj.getString("synj"));
                bjMcdmMap.put(obj.getString("bjmc"), obj.getString("bh"));
                bhBjMap.put(obj.getString("bh"), obj);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        res.put("xhMap", xhMap);
        res.put("xjhMap", xjhMap);
        res.put("sfzhMap", sfzhMap);
        res.put("keyMap", keyMap);
        res.put("bjSynjMap", bjSynjMap);
        res.put("bjMcdmMap", bjMcdmMap);
        res.put("xjhStuInfo", xjhStuInfo);
        res.put("bhBjMap", bhBjMap);
        return res;
    }

    @Override
    public void updateStuStatusByParam(HashMap map1) throws Exception {
        // TODO Auto-generated method stub
        update("updateStuSta", map1);
    }

    @Override
    public void insertStuList(List<TSsStudent> needInsert) throws Exception {
        // TODO Auto-generated method stub
        insert("insertStuList", needInsert);
    }

    @Override
    public void updateStuList(List<TSsStudent> needUpdate) throws Exception {
        // TODO Auto-generated method stub
//        for(int i=0;i<needUpdate.size();i++){
//            update("updateStudent", needUpdate.get(i));
//        }
        update("updateStudentList", needUpdate);
    }

    @Override
    public void updateStuEnrolList(List<TSsStudenrol> needIOUStuEnrol) throws Exception {
        // TODO Auto-generated method stub
        update("updateStudentEnrolList",needIOUStuEnrol);
    }

    @Override
    public void deleteStuEnrol(List<TSsStudenrol> delStuEnrol) throws Exception {
        // TODO Auto-generated method stub
        for(int i=0;i<delStuEnrol.size();i++){
            
            delete("deleteStuEnrol",delStuEnrol.get(i));
        }
//        delete("deleteStuEnrolList",delStuEnrol);
    }

    @Override
    public void insertClassEnrolList(List<TSsClassenrol> needInsertClassEnrol) throws Exception {
        // TODO Auto-generated method stub
        insert("insertClassEnrolList",needInsertClassEnrol);
    }

    @Override
    public void insertClassList(List<TSsClass> needInsertClass) throws Exception {
        // TODO Auto-generated method stub
        insert("insertClassList",needInsertClass);
    }

}
