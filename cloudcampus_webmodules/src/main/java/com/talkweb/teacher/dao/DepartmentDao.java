package com.talkweb.teacher.dao;

import java.util.List;
import java.util.Map;

import com.talkweb.teacher.domain.page.TTrBmxx;

/**
 * @version 2.0
 * @Description: 教师机构（科室）数据层接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface DepartmentDao {

	/**
	 * 添加一个机构信息
	 * @return
	 * @throws Exception
	 */
	public int addDepartment(TTrBmxx bmxx) ;
	
	/**
	 * 通过机构号ID删除机构信息
	 * @param jgh
	 * @return
	 * @throws Exception
	 */
	public int deleteDepartment(String jgh);
	
	/**
	 * 修改机构信息
	 * @param bmxx
	 * @return
	 * @throws Exception
	 */
	public int updateDepartment(TTrBmxx bmxx);
	
	/**
	 * 查询科室信息列表
	 * @param bmxx
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getDepartmentList(TTrBmxx bmxx);
	
	/**
	 * 查询单个科室信息
	 * @param bmxx
	 * @return
	 * @throws Exception
	 */
	public TTrBmxx getOneDepartment(TTrBmxx bmxx);
	
	/**
	 * 查询指定学校的年级组列表
	 * @param bmxx
	 * @return
	 */
	public List<Map<String,Object>> getGradeList(TTrBmxx bmxx);
	
	/**
	 * 查询单个年级组
	 * @param bmxx
	 * @return
	 */
	public Map<String,Object> getOneGrade(TTrBmxx bmxx);
	
	/**
	 * 查询教研组列表
	 * @param bmxx
	 * @return
	 */
	public List<Map<String,Object>> getResearchGroupList(TTrBmxx bmxx);
	
	/**
	 * 查询单个教研组
	 * @param bmxx
	 * @return
	 */
	public List<Map<String,Object>> getOneResearchGroup(TTrBmxx bmxx);
	
	/**
	 * 查询备课组列表
	 * @param bmxx
	 * @return
	 */
	public List<Map<String,Object>> getLessonPlanningGroupList(TTrBmxx bmxx);
	
	/**
	 * 查询备课组研究科目列表
	 * @param bmxx 备课组机构号数组
	 * @return
	 */
	public List<Map<String,Object>> getLessonPlanningGroupYjkmList(List<String> bmxx);
	
	/**
	 * 查询单个备课组
	 * @param bmxx
	 * @return
	 */
	public List<Map<String,Object>> getOneLessonPlanningGroup(TTrBmxx bmxx);
}
