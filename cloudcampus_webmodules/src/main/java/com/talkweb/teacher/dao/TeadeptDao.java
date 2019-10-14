package com.talkweb.teacher.dao;

import java.util.List;
import java.util.Map;

import com.talkweb.teacher.domain.page.THrTeadept;

/**
 * @version 2.0
 * @Description: 教师机构人员设置相关底层数据交互接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface TeadeptDao {

	/**
	 * 添加一个部门人员安排信息
	 * @param teadept 参数对象
	 * @return 添加结果
	 * @throws Exception
	 */
	public int addTeadept(THrTeadept teadept);
	
	/**
	 * 通过指定的人员ID和部门ID删除人员安排信息
	 * @param teadept 参数对象
	 * @return 删除结果
	 * @throws Exception
	 */
	public int deleteTeadept(THrTeadept teadept);
	
	/**
	 * 查询指定学校的指定部门的已安排人员列表信息
	 * @param teadept 参数对象
	 * @return 结果List
	 * @throws Exception
	 */
	public List<Map<String,Object>> getDepartmentStaff(Map<String,String> teadept);
	
	/**
	 * 查询指定学校的备课组的已安排人员列表信息
	 * @param param 参数map
	 * @return 结果List
	 * @throws Exception
	 */
	public List<Map<String,Object>> getLessonDepartmentStaff(Map<String,String> param);
	
	/**
	 * 查询指定学校的指定部门的未安排人员列表信息
	 * @param teadept 参数对象map
	 * @return 结果List
	 * @throws Exception
	 */
	public List<Map<String,Object>> getDepartmentNoStaff(Map<String,String> teadept);

}
