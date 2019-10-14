package com.talkweb.system.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.system.dao.UserDao;
import com.talkweb.system.domain.business.User;


@Repository
public class UserDaoImpl extends MyBatisBaseDaoImpl implements UserDao{

	@Override
	public List<User> getAllUsers(){
		
		return selectList("getAllUsers");
	}


	
}
