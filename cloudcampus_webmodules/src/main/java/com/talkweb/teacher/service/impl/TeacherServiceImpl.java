package com.talkweb.teacher.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.teacher.dao.TeacherDao;
import com.talkweb.teacher.domain.page.THrTeacher;
import com.talkweb.teacher.service.TeacherService;

/**
 * @version 2.0
 * @Description: 教师业务逻辑实现类
 * @author 雷智
 * @date 2015年3月5日
 */
@Service
public class TeacherServiceImpl implements TeacherService {
	@Autowired
	private TeacherDao teacherDaoImpl;

	/* 根据id删除教师人员信息 */
	@Override
	public Map<String, Object> delTeacherById(String id) {
		Map<String, Object> info = new HashMap<String, Object>();
		int rescode = teacherDaoImpl.deleteTeacher(id);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			info.put("msg", OutputMessage.delSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.delDataError.getCode());
			info.put("msg", OutputMessage.delDataError.getDesc());
		}
		return info;
	}

	/* 添加教师信息 */
	@Override
	public Map<String, Object> addTeacher(THrTeacher teacher) {
		Map<String, Object> info = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		int rescode = teacherDaoImpl.addTeacher(teacher);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			data.put("msg", OutputMessage.addSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.addDataError.getCode());
			data.put("msg", OutputMessage.addDataError.getDesc());
		}
		info.put("data", data);
		return info;
	}

	/* 修改教师信息 */
	@Override
	public Map<String, Object> updateTeacher(THrTeacher teacher) {
		Map<String, Object> info = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		int rescode = teacherDaoImpl.updateTeacher(teacher);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			data.put("msg", OutputMessage.updateSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.updateDataError.getCode());
			data.put("msg", OutputMessage.updateDataError.getDesc());
		}
		info.put("data", data);
		return info;
	}

	/* 根据条件查询 */
	@Override
	public List<THrTeacher> queryTeacherByLimit(Map<String, Object> limit) {
		return teacherDaoImpl.queryTeacherInLimit(limit);
	}

	/* 根据id查询教师信息 */
	@Override
	public THrTeacher queryTeacherById(String zgh) {
		return teacherDaoImpl.queryTeacherOne(zgh);
	}

	/* 根据多个id删除多个教师信息 */
	@Override
	public Map<String, Object> deleteTeacherByIdList(List<String> IdList) {
		Map<String, Object> info = new HashMap<String, Object>();
		Map<String, Object> data = new HashMap<String, Object>();
		int rescode = teacherDaoImpl.deleteTeacherByIdList(IdList);
		if (rescode > 0) {
			info.put("code", OutputMessage.delSuccess.getCode());
			data.put("msg", OutputMessage.delSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.delDataError.getCode());
			data.put("msg", OutputMessage.delDataError.getDesc());
		}
		info.put("data", data);
		System.out.println(JSON.toJSON(info));
		return info;
	}

    @Override
    public JSONObject getAllTeaBySchoolNum(HashMap map) {
        // TODO Auto-generated method stub
        List<JSONObject> list = teacherDaoImpl.getAllTeaBySchoolNum(map);
        JSONObject res = new JSONObject();
        HashMap<String,String> ghMap = new HashMap<String, String>();
        HashMap<String,String> sfzhMap = new HashMap<String, String>();
        HashMap<String,Integer> ghNumMap = new HashMap<String, Integer>();
        for(int i=0;i<list.size();i++){
            JSONObject obj = list.get(i);
            ghMap.put(obj.getString("jsdm"), obj.getString("zgh"));
            if(ghNumMap!=null&&ghNumMap.containsKey(obj.getString("jsdm"))){
                int next = ghNumMap.get(obj.getString("jsdm"))+1;
                ghNumMap.put(obj.getString("jsdm"), next);
            }else{
                ghNumMap.put(obj.getString("jsdm"), 1);
            }
            sfzhMap.put( obj.getString("sfzh"),obj.getString("jsdm"));
        }
        JSONObject obj = new JSONObject();
        obj.put("ghMap", ghMap);
        obj.put("ghNumMap", ghNumMap);
        obj.put("sfzhMap", sfzhMap);
        return obj;
    }

    @Override
    public void insertTeaList(List<THrTeacher> needInsert) {
        // TODO Auto-generated method stub
        teacherDaoImpl.insertTeaList(needInsert);
    }

}
