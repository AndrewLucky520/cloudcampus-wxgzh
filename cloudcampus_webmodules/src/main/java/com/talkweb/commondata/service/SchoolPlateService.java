package com.talkweb.commondata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.commondata.dao.SchoolPlateDao;
/**
 * @ClassName SchoolPlateService
 * @author zhh
 * @version 1.0
 * @Description 基础数据-学校平台关联Service
 * @date 2017年2月13日
 */
@Service
public class SchoolPlateService {
	
	@Autowired
	private SchoolPlateDao schoolPlateDao;
	
	/**
	 * 获取学校平台对象
	 * @param param
	 * {
	 *   plateType
	 *   schoolId
	 * }
	 * @return
	 */
	public List<JSONObject> getSchoolPlateListBy(JSONObject param){
		return schoolPlateDao.getSchoolPlateListBy(param);
	}
	/**
	 * 获取学校平台对象列表
	 * @param param
	 * {
	 *   plateType
	 *   schoolId（必填）
	 * }
	 * @return
	 */
	public JSONObject getSchoolPlateBySchoolId(JSONObject param){
		return schoolPlateDao.getSchoolPlateBySchoolId(param);
	}
	/**
	 * 增加学校平台，增加是需程序员自己判断schoolId的唯一性与否
	 * @param param
	 */
	public void addSchoolPlate(JSONObject param){
		 schoolPlateDao.addSchoolPlate(param);
	}
	/**
	 * 删除学校平台
	 * @param param
	 * {
	 *    schoolId:学校代码（必填）,
	 *    plateType:1长沙 0其他
	 * }
	 */
	public void deleteSchoolPlate(JSONObject param){
		 schoolPlateDao.deleteSchoolPlate(param);
	}
	/**
	 * 更新学校平台
	 * @param param  
	 * {
	 *   schoolId:学校代码,（必填）
	 *   status:状态 0已删除 1正常,（必填）
	 *   plateType:1长沙 0其他（必填）
	 * }
	 */
	public void updateSchoolPlate(JSONObject param){
		 schoolPlateDao.updateSchoolPlate(param);
	}
}
