package com.talkweb.student.dao;

import java.util.List;
import java.util.Map;

import com.talkweb.student.domain.business.TSsClass;

/**
 * @Description: 班级信息维护DAO层
 * @author 雷智
 * @date 2015年3月9日
 */
public interface ClassDao {
	/**
	 * 查询班级列表
	 * 
	 * @param xnxq
	 *            , synj, bjmc
	 * @return List<TSsClass>
	 */
	public List<Map<String, Object>> queryClassList(Map<String, Object> limit);

	/**
	 * 查询单个班级
	 * 
	 * @param bh
	 * @return
	 */
	public Map<String, Object> queryClassOne(Map<String, Object> param);

	/**
	 * 添加单个班级
	 * 
	 * @param sclass
	 * @return
	 */
	public int addClass(Map<String, Object> map);

	/**
	 * 批量添加班级
	 * 
	 * @param addCondition
	 * @return
	 */
	public int addBatchClass(List<TSsClass> bClass);

	/**
	 * 修改班级信息
	 * 
	 * @param updatas
	 * @return
	 */
	public int updateClass(Map<String, Object> updatas);

	/**
	 * 删除班级信息，可删除多个
	 * 
	 * @param dels
	 * @return
	 */
	public int delClass(List<String> dels);

}
