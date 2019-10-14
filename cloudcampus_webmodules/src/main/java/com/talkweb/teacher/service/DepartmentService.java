package com.talkweb.teacher.service;

import java.util.Map;

import com.talkweb.teacher.domain.page.TTrBmxx;

/**
 * @version 2.0
 * @Description: 教师机构部门业务逻辑接口
 * @author 吴安辉
 * @date 2015年3月3日
 */

public interface DepartmentService {

	/**
	 * 添加科室信息
	 * @param bmxx 部门对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> addKsInfo(TTrBmxx bmxx);
	
	/**
	 * 修改科室信息
	 * @param bmxx 部门对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> updateKsInfo(TTrBmxx bmxx);
	
	/**
	 * 通过部门代码数组删除一个（多个）部门信息
	 * @param bmbh 部门编号
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> deleteKsInfo(String[] bmbh);
	
	/**
	 * 查询科室列表
	 * @param bmxx部门信息对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getKsList(TTrBmxx bmxx);
	
	/**
	 * 查询单个科室信息
	 * @param bmxx 部门对象
	 * @return 部门对象
	 */
	public TTrBmxx getOneKsInfo(TTrBmxx bmxx);
	
	/**
	 * 新增年级组：先在机构部门表添加年级组部门，再在部门管理年级表添加部门管理年纪信息
	 * @param bmxx 部门对象
	 * @param grade 年级代码
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> addGrade(TTrBmxx bmxx,String grade,String xn);
	
	/**
	 * 删除年级组:先在部门管理年级表删除部门管理年纪信息,再删除机构部门表的年级组信息
	 * @param grades 年级组代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> deleteGrade(String[] grades);
	
	/**
	 * 修改年级组
	 * @param bmxx 部门对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> updateGrade(TTrBmxx bmxx);
	
	/**
	 * 查询单个年级组
	 * @param bmxx 部门对象 
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getOneGrade(TTrBmxx bmxx,String xn);
	
	/**
	 * 查询年级组List
	 * @param bmxx 部门对象 
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getGradeList(TTrBmxx bmxx);
	
	/**
	 * 查询教研组列表List
	 * @param bmxx 参数对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getResearchGradeList(TTrBmxx bmxx);
	
	/**
	 * 新增教研组
	 * @param bmxx 部门参数对象
	 * @param kmdms 科目代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> addResearchGrade(TTrBmxx bmxx,String[] kmdms);
	
	/**
	 * 修改教研组
	 * @param bmxx 部门参数对象
	 * @param kmdms 科目代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> updateResearchGrade(TTrBmxx bmxx,String[] kmdms);
	
	/**
	 * 查询单个教研组
	 * @param bmxx 部门参数对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getOneResearchGrade(TTrBmxx bmxx);
	
	/**
	 * 删除教研组
	 * @param bmdm 部门代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> deleteResearchGrade(String[] bmdm);
	
	/**
	 * 查询备课组列表
	 * @param bmxx 部门参数对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getLessonPlanningGroupList(TTrBmxx bmxx);

	/**
	 * 新增备课组
	 * @param bmxx 部门参数对象
	 * @param jyzdm 教研科目代码数组
	 * @param njzdm 年级代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> addLessonPlanningGroup(TTrBmxx bmxx,String[] jyzdm,String[] njzdm,String xn);
	
	/**
	 * 修改备课组
	 * @param bmxx 部门参数对象
	 * @param jyzdm 教研科目代码数组
	 * @param njzdm 年级代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> updateLessonPlanningGroup(TTrBmxx bmxx,String[] jyzdm,String[] njzdm,String xn);
	
	/**
	 * 查询单个备课组
	 * @param bmxx 部门参数对象
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> getOneLessonPlanningGroup(TTrBmxx bmxx,String xn);
	
	/**
	 * 删除备课组 
	 * @param jgdm 备课组代码数组
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String,Object> deleteLessonPlanningGroup(String[] jgdm);
	
	
}
