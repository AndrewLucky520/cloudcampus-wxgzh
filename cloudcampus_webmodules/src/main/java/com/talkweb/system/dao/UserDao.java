package com.talkweb.system.dao;

import java.util.List;

import com.talkweb.system.domain.business.User;

public interface UserDao {

	List<User> getAllUsers();
}
