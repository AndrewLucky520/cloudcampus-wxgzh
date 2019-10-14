package com.talkweb.student.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.talkweb.base.common.OutputMessage;
import com.talkweb.student.dao.ClassDao;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.service.ClassService;

/**
 * @Version 2.0
 * @Description 班级信息维护
 * @author 雷智
 * @Data 2015-03-13
 */
@Service
public class ClassServiceImpl implements ClassService {
	@Autowired
	private ClassDao classDao;

	@Override
	public List<Map<String,Object>> queryClassList(Map<String, Object> limit) {
		return classDao.queryClassList(limit);
	}

	@Override
	public Map<String,Object> queryClassOne(Map<String, Object> param) {
		return classDao.queryClassOne(param);
	}

	@Override
	public Map<String, Object> addClass(Map<String, Object> map) {
		Map<String, Object> info = new HashMap<String, Object>();
		int rescode = classDao.addClass(map);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			info.put("msg", OutputMessage.addSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.addDataError.getCode());
			info.put("msg", OutputMessage.addDataError.getDesc());
		}
		return info;
	}

	@Override
	public Map<String, Object> addBatchClass(List<TSsClass> bClass) {
		Map<String, Object> info = new HashMap<String, Object>();
		int rescode = classDao.addBatchClass(bClass);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			info.put("msg", OutputMessage.addSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.addDataError.getCode());
			info.put("msg", OutputMessage.addDataError.getDesc());
		}
		return info;
	}

	@Override
	public Map<String, Object> updateClass(Map<String, Object> updatas) {
		Map<String, Object> info = new HashMap<String, Object>();
		int rescode = classDao.updateClass(updatas);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			info.put("msg", OutputMessage.updateSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.addDataError.getCode());
			info.put("msg", OutputMessage.addDataError.getDesc());
		}
		return info;
	}

	@Override
	public Map<String, Object> delClass(List<String> dels) {
		Map<String, Object> info = new HashMap<String, Object>();
		int rescode = classDao.delClass(dels);
		if (rescode > 0) {
			info.put("code", OutputMessage.success.getCode());
			info.put("msg", OutputMessage.delSuccess.getDesc());
		} else {
			info.put("code", OutputMessage.delDataError.getCode());
			info.put("msg", OutputMessage.delDataError.getDesc());
		}
		return info;
	}

}
