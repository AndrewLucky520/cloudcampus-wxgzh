package com.talkweb.teacher.dao;

import java.util.List;
import java.util.Map;

import com.talkweb.teacher.domain.page.TTrBmfzr;

/**
 * @version 2.0
 * @Description: 部门人员负责人设置相关底层数据交互接口
 * @author 吴安辉
 * @date 2015年3月3日
 */
public interface BmfzrDao {

	/**
	 * 添加部门负责人
	 * @param fzr 负责人对象
	 * @return 添加结果
	 * @throws Exception
	 */
	public int addBmfzr(TTrBmfzr fzr) ;
	
	/**
	 * 删除部门负责人
	 * @param fzr 负责人对象
	 * @return 删除结果
	 * @throws Exception
	 */
	public int deleteBmfzr(TTrBmfzr fzr);
	
	/**
	 * 查询部门负责人列表
	 * @param jgh
	 * @return
	 */
	public List<Map<String,Object>> getDepartmentFzrList(String jgh);
}
