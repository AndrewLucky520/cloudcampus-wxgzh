package com.talkweb.teacher.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.BmfzrDao;
import com.talkweb.teacher.domain.page.TTrBmfzr;

/**
 * @version 2.0
 * @Description: 部门人员负责人设置相关底层数据交互实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */
@Repository
public class BmfzrDaoImpl extends MyBatisBaseDaoImpl implements BmfzrDao {

	@Override
	public int addBmfzr(TTrBmfzr fzr){
		return insert("addBmfzr",fzr);
	}

	@Override
	public int deleteBmfzr(TTrBmfzr fzr){
		return delete("deleteBmfzr",fzr);
	}

	@Override
	public List<Map<String, Object>> getDepartmentFzrList(String jgh) {
		return selectList("getDepartmentFzrList",jgh);
	}

}
