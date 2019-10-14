package com.talkweb.system.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.talkweb.system.dao.UserDao;
import com.talkweb.system.domain.business.User;
import com.talkweb.system.service.UserService;
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDaoImpl;
	
	@Override
	@Cacheable(value="usercache")
	public List<User> getAllUsers() {
		List<User> list = null;
		try {
			list =  userDaoImpl.getAllUsers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
