package com.talkweb.teacher.dao;

import java.util.List;

import com.talkweb.teacher.domain.page.TTrBmxxNj;

/**
 * @version 2.0
 * @Description: 部门信息关联年级设置相关底层数据交互接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface BmxxNjDao {
	
	/**
	 * 添加部门管理年级表
	 * @param bmnj 管理年级对象
	 * @return 添加结果
	 * @throws Exception
	 */
	public int addBmxxNjInfo(TTrBmxxNj bmnj);
	
	/**
	 * 删除部门管理年级表
	 * @param jgh 机构代码
	 * @return 删除结果
	 * @throws Exception
	 */
	public int deleteBmxxNjInfo(String jgh);
	
	/**
	 * 修改部门管理年级表
	 * @param bmnj 管理年级对象
	 * @return 修改结果
	 * @throws Exception
	 */
	public int updateBmxxNjInfo(TTrBmxxNj bmnj);
	
	/**
	 * 通过机构号查询管理年级列表
	 * @param jgh 机构号
	 * @return 管理年级列表List
	 */
	public List<TTrBmxxNj> getBmxxNjListByJgh(String jgh);
}
