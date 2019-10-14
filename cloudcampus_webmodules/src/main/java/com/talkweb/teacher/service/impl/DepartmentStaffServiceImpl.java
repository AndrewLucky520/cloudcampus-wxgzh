package com.talkweb.teacher.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.base.common.OutputMessage;
import com.talkweb.teacher.dao.BmfzrDao;
import com.talkweb.teacher.dao.BmxxKmDao;
import com.talkweb.teacher.dao.TeadeptDao;
import com.talkweb.teacher.domain.page.THrTeadept;
import com.talkweb.teacher.domain.page.TTrBmfzr;
import com.talkweb.teacher.service.DepartmentStaffService;

/**
* @version 2.0
* @Description: 教师机构人员设置相关处理业务逻辑实现类
* @author 吴安辉
* @date 2015年3月3日
*/
@Service
public class DepartmentStaffServiceImpl implements DepartmentStaffService {

	@Autowired
	private TeadeptDao teadeptDao;
	
	@Autowired
	private BmfzrDao bmfzrDao;
	
	@Autowired
	private BmxxKmDao bmxxKmDao;

	//安排 部门人员
	@Override
	public Map<String, Object> addTeadept(String bm, String xxdm, String[] zghs,String jglb,String km) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String zgh : zghs){
			THrTeadept teadept = new THrTeadept();
			teadept.setBm(bm);
			teadept.setXxdm(xxdm);
			teadept.setZgh(zgh);
			teadept.setJglb(jglb);

			count = teadeptDao.addTeadept(teadept);
			if(count < 0){
				break;
			}
		}
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.addDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.addDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//删除安排人员
	@Override
	public Map<String, Object> deleteTeadept(String bm, String[] zghs) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		for(String zgh : zghs){
			THrTeadept teadept = new THrTeadept();
			teadept.setBm(bm);
			teadept.setZgh(zgh);
			
			TTrBmfzr fzr = new TTrBmfzr();
			fzr.setFzrgh(zgh);
			fzr.setJgh(bm);
			bmfzrDao.deleteBmfzr(fzr);
			
			count = teadeptDao.deleteTeadept(teadept);
			if(count < 0){
				break;
			}
		}
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询指定部门的已安排人员
	@Override
	public Map<String, Object> queryDepartmentStaff(Map<String,String> teadept) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = teadeptDao.getDepartmentStaff(teadept);
		List<Map<String,Object>> fzrList = bmfzrDao.getDepartmentFzrList(teadept.get("jgh"));
		for(Map<String,Object> map : list){
			boolean flag = false;
			for(Map<String,Object> m : fzrList){
				if(map.get("zgh").equals(m.get("fzrgh"))){
					flag = true;
				}
			}
			if(flag){
				map.put("fzr", "1");
			}else{
				map.put("fzr", "0");
			}
		}
		data.put("rows",list);
		return data;
	}

	//增加负责人
	@Override
	public Map<String, Object> addBmFzr(TTrBmfzr bmfzr) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		count = bmfzrDao.addBmfzr(bmfzr);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.addDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.addDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//删除负责人
	@Override
	public Map<String, Object> deleteBmFzr(TTrBmfzr bmfzr) {
		Map<String,Object> data = new HashMap<String,Object>();
		int count = 0;
		count = bmfzrDao.deleteBmfzr(bmfzr);
		if(count > 0){
			data.put("code", OutputMessage.success.getCode());
		}else{
			data.put("code", OutputMessage.delDataError.getCode());
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("msg", OutputMessage.delDataError.getDesc());
			data.put("data", msg);
		}
		return data;
	}

	//查询未安排人员
	@Override
	public Map<String, Object> queryDepartmentNoStaff(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		List<Map<String,Object>> list = null;
		list = teadeptDao.getDepartmentNoStaff(params);		
		if(list != null){
			data.put("total", list.size());
		}else{
			data.put("total", 0);
		}
		data.put("rows", list);
		return data;
	}
	
	//查询备课组已设置的人员列表
	public Map<String,Object> queryLessonDepartmentStaff(String bmbh,String xxdm,String xm){
		Map<String,Object> data = new HashMap<String,Object>();
		Map<String,String> param = new HashMap<String,String>();
		param.put("bm", bmbh);
		param.put("xm", xm);
		List<Map<String,Object>> list = teadeptDao.getLessonDepartmentStaff(param);
		List<Map<String,Object>> fzrList = bmfzrDao.getDepartmentFzrList(bmbh);
		for(Map<String,Object> map : list){
			boolean flag = false;
			for(Map<String,Object> m : fzrList){
				if(map.get("zgh").equals(m.get("fzrgh"))){
					flag = true;
				}
			}
			if(flag){
				map.put("fzr", "1");
			}else{
				map.put("fzr", "0");
			}
		}
		data.put("total", list.size());
		
		data.put("rows", list);
		return data;
	}

	//获取教研组科目列表
	@Override
	public List<Map<String,Object>> getSubjectList(String jgh) {
		List<Map<String,Object>> list = bmxxKmDao.getYjKmList(jgh);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", "");
		map.put("text", "全部");
		result.add(map);
		for(Map<String,Object> m : list){
			result.add(m);
		}
		return result;
	}

}
