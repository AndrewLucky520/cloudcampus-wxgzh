package com.talkweb.teacher.dao;

import java.util.List;
import java.util.Map;

import com.talkweb.teacher.domain.page.TTrBmxxKm;

/**
 * @version 2.0
 * @Description: 部门信息关联科目设置相关底层数据交互接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface BmxxKmDao {

	/**
	 * 添加关联科目信息
	 * @param bmkm
	 * @return
	 */
	public int addBmxxKm(TTrBmxxKm bmkm);
	
	/**
	 * 通过关联部门Id删除对应的关联科目信息
	 * @param jgh
	 * @return
	 */
	public int deleteBmxxKm(String jgh);

	/**
	 * 通过部门ID查询所有的关联科目信息
	 * @param jgh
	 * @return
	 */
	public List<Map<String,Object>> getYjKmList(String jgh);
}
