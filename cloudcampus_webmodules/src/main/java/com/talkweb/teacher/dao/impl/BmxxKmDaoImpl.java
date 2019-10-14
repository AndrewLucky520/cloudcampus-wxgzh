package com.talkweb.teacher.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.BmxxKmDao;
import com.talkweb.teacher.domain.page.TTrBmxxKm;

/**
 * @version 2.0
 * @Description: 部门信息关联科目信息设置相关底层数据交互实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */
@Repository
public class BmxxKmDaoImpl extends MyBatisBaseDaoImpl implements BmxxKmDao {

	@Override
	public int addBmxxKm(TTrBmxxKm bmkm){
		return insert("addBmkm",bmkm);
	}

	@Override
	public int deleteBmxxKm(String jgh){
		return delete("deleteBmkm",jgh);
	}

	@Override
	public List<Map<String, Object>> getYjKmList(String jgh) {
		return selectList("getYjkmList",jgh);
	}

}
