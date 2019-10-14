package com.talkweb.teacher.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.teacher.dao.BmxxNjDao;
import com.talkweb.teacher.domain.page.TTrBmxxNj;

/**
 * @version 2.0
 * @Description: 部门信息关联年级设置相关底层数据交互实现类
 * @author 吴安辉
 * @date 2015年3月3日
 */
@Repository
public class BmxxNjDaoImpl extends MyBatisBaseDaoImpl implements BmxxNjDao {

	@Override
	public int addBmxxNjInfo(TTrBmxxNj bmnj){
		return insert("addBmnj",bmnj);
	}

	@Override
	public int deleteBmxxNjInfo(String bmnj){
		return delete("deleteBmnj",bmnj);
	}

	@Override
	public int updateBmxxNjInfo(TTrBmxxNj bmnj){
		return update("updateBmnj",bmnj);
	}

	@Override
	public List<TTrBmxxNj> getBmxxNjListByJgh(String jgh) {
		return selectList("getNjListByJgh",jgh);
	}

}
