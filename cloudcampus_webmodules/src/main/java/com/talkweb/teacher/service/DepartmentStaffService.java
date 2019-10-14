package com.talkweb.teacher.service;

import java.util.List;
import java.util.Map;

import com.talkweb.teacher.domain.page.TTrBmfzr;

/**
 * @version 2.0
 * @Description: 教师机构人员设置相关处理逻辑接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface DepartmentStaffService {

	/**
	 * 安排科室人员
	 * @param bm 部门ID
	 * @param xxdm 学校ID
	 * @param zghs 人员ID数组
	 * @param jglb 机构类别
	 * @param km 此熟悉为教研组专用
	 * @return 
	 */
	public Map<String,Object> addTeadept(String bm,String xxdm,String[] zghs,String jglb,String km);
	
	/**
	 * 删除人员安排
	 * @param bm 部门ID
	 * @param zghs 教师ID数组
	 * @return 
	 */
	public Map<String,Object> deleteTeadept(String bm,String[] zghs);
	
	/**
	 * 查询已设置的部门人员列表
	 * @param param 参数map
	 * @return 
	 */
	public Map<String,Object> queryDepartmentStaff(Map<String,String> param);
	
	/**
	 * 查询备课组已设置的部门人员列表
	 * @param bmbh 部门代码
	 * @param xxdm 学校代码
	 * @param xm 姓名
	 * @return 
	 */
	public Map<String,Object> queryLessonDepartmentStaff(String bmbh,String xxdm,String xm);
	
	/**
	 * 查询未设置的部门人员列表
	 * @param params 参数map
	 * @return
	 */
	public Map<String,Object> queryDepartmentNoStaff(Map<String,String> params);
	
	/**
	 * 添加部门负责人
	 * @param bmfzr 负责人对象
	 * @return
	 */
	public Map<String,Object> addBmFzr(TTrBmfzr bmfzr);
	
	/**
	 * 删除部门负责人
	 * @param bmfzr 负责人对象
	 * @return
	 */
	public Map<String,Object> deleteBmFzr(TTrBmfzr bmfzr);
	
	/**
	 * 通过研究科目的结构号获取研究科目下拉列表
	 * @param jgh 机构号
	 * @return
	 */
	public List<Map<String,Object>> getSubjectList(String jgh);
}
