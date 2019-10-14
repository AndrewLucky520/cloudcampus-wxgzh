package com.talkweb.systemManager.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.systemManager.domain.business.Role;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;
import com.talkweb.systemManager.exception.DeleteOperationException;
import com.talkweb.systemManager.exception.InsertOperationException;
/**
 * @ClassName UserService
 * @author Homer
 * @version 1.0
 * @Description 系统管理Service
 * @date 2015年3月3日
 */
public interface UserService {
	/**
	 * 查询是否有账号
	 * @param userId 账号
	 * @return 0 or 1
	 * @throws Exception
	 */
	int selectUserId(String userId);
	/**
	 * 验证账号和密码
	 * @param userId 账号
	 * @param passWord 密码
	 * @return 用户实体
	 * @throws Exception
	 */
	TUcUser selectUser(String userId, String passWord) ;
	/**
	 * 通过用户账号查询所在单位
	 * @param userId 账号
	 * @return 学校实体
	 * @throws Exception
	 */
	TTrSchool selectSchoolByUserId(String userId);
	/**
	 * 修改密码
	 * @param oldPassWord 老密码
	 * @param passWord 新密码
	 * @param userId 账号
	 * @return 是否修改成功
	 */
	int updatePassword(String oldPassWord,String passWord,String userId) ;
	/**
	 * 修改账号
	 * @param newUserId 新账号
	 * @param passWord 密码
	 * @param userId 老账号
	 * @return
	 * @throws Exception
	 */
	int updateAccount(String newUserId,String passWord,String userId);
	/**
	 * 根据userId获得用户菜单资源等
	 * @param userSysId 账号
	 * @param sf 身份
	 * @return 菜单资源集
	 */
	List<Map<String,Object>> getMySysMenu(String userSysId,String sf);
	/**
	 * 根据userId获得用户系统资源等
	 * @param userSysId 账号
	 * @return 系统资源集
	 */
	List<Map<String,Object>> getSubSys(String userSysId);
	/**
	 * 根据userId获得类别名称
	 * @param userSysId 账号
	 * @return 类别名称集
	 */
	List<Map<String,Object>> getSortName(String userSysId);
	
	/**
	 * 根据家长用户代码查询学生代码
	 * @author userSysId 家长用户代码
	 * @return 学生列表
	 */
	List<String> getStudentXHByParentSysId(String parentsysid);
	/**
	 * 根据用户代码查询角色
	 * @param userSysId 用户代码
	 * @return 角色列表
	 */
	List<String> getRoleByUserSysId(String userSysId);
	/**
	 * 获取用户具有权限的菜单树
	 * @param userSysId
	 * @param xxdm
	 * @return
	 */
    JSONArray getUserSysMenu(String userSysId);
    /**
     *  获取功能模块信息
     * @param map
     * @return
     */
    JSONArray getModelsInfo(HashMap map);
	
	/**
	 * 根据学校代码查询管理员
	 * @param xxdm 学校代码
	 * @return 管理员列表
	 */
	List<Role> querySuperManagerList(String xxdm);
	
	/**
	 * 根据学校代码查询其他管理员
	 * @param xxdm 学校代码
	 * @return 管理员列表
	 */
	List<Role> queryOtherManagerList(String xxdm);
	
	/**
	 * 查询设置管理员
	 * @param xxdm 学校代码
	 * @param roleDm 角色代码
	 * @param zghxm 职工号或姓名
	 * @return
	 */
	List<Map<String,Object>> querySetManagerList(String xxdm,String roleDm,String zghxm);
	
	/**
	 * 非系统管理员插入用户年级
	 * @param roleDm 角色代码
	 * @param zgh 系统代码
	 * @param grade 年级
	 * @return 影响记录数
	 */
	List<Map<String,Object>> insertManagerList(String roleDm,String zgh,List<String> grade)throws InsertOperationException;
	
	/**
	 * 系统管理员插入用户年级
	 * @param roleDm 角色代码
	 * @param zgh 系统代码
	 * @param grade 年级
	 * @return 影响记录数
	 */
	List<Map<String,Object>> insertManagerList2(String roleDm,List<String> zgh)throws InsertOperationException;
	
	/**
	 * 查询教师系统代码和权限信息
	 * @param zgh 教师系统代码列表
	 * @return
	 */
	List<Map<String,Object>> getTeacherQXXX(List<String> zgh);
	
	/**
	 * 删除管理员
	 * @param roleDm 角色代码
	 * @param zgh 系统代码
	 * @return 影响记录数
	 */
	int deleteManagerList(String roleDm,String zgh) throws DeleteOperationException;
	
	/**
	 * 查询所在学校所有班级
	 * @param xxdm 学校代码
	 * @return
	 */
	List<Map<String,Object>> getManagerGradeList(String xxdm);
	
	/**
	 * 查询教师账号
	 * @param zghxm 职工号或姓名
	 * @return 账号列表
	 */
	List<Map<String,Object>> queryTeacherAccountList(String xxdm,String zghxm);
	
	/**
	 * 修改密码为账号
	 * @param zgh 教师系统代码
	 * @return
	 */
	int updatePwdsEqualsAccount(String zgh);
	
	/**
	 * 修改密码为指定密码或者随机密码
	 * @param zgh 教师系统代码
	 * @param passWord 密码（秘文）
	 * @param oriPassWord 初始密码（明文）
	 * @return
	 */
	int updatePwds(List<String> zgh,String passWord,String oriPassWord);
	
	/**
	 * 查询学生账号
	 * @param xxdm 学校代码
	 * @param xn 学年
	 * @param xqm 学期
	 * @param synj 使用年级
	 * @param bjmc 班级名称
	 * @param xjhxm 学籍号或者姓名
	 * @return
	 */
	List<Map<String,Object>> queryStudentAccountList(String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);

	/**
	 * 查询家长账号
	 * @param xxdm 学校代码
	 * @param xn 学年
	 * @param xqm 学期
	 * @param synj 使用年级
	 * @param bjmc 班级名称
	 * @param xjhxm 学籍号或者姓名
	 * @return
	 */
	List<Map<String,Object>> queryParentAccountList(String xxdm,String xn,String xqm,List<String> synj,List<String> bjmc,String xjhxm);

	/**
	 * 查询非超级管理员
	 * @param xxdm 学校代码
	 * @param roleDm 角色代码
	 * @param zghxm 职工号或姓名
	 * @return
	 */
	List<Map<String,Object>> querySetOtherManagerList(String xxdm,String roleDm,String zghxm);
	/**
	 * 删除非超级管理员
	 * @param roleDm 角色代码
	 * @param zgh 系统代码
	 * @return 影响记录数
	 */
	int deleteOtherManagerList(String roleDm,String zgh)throws DeleteOperationException;
	/**
	 * 查询教师系统代码和权限信息
	 * @param zgh 教师系统代码列表
	 * @return
	 */
	List<Map<String,Object>> getTeacherQXXX2(List<String> zgh,String roleDm);
}
