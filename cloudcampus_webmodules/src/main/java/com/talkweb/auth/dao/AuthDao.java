package com.talkweb.auth.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.auth.entity.NavInfo;

/**
 * @ClassName AuthDao
 * @author Homer
 * @version 1.0
 * @Description 权限控制Dao
 * @date 2015年3月9日
 */
public interface AuthDao {

	/**
	 * 通过视图v_ws_usermenu取用户对应菜单
	 * @param userSysId 用户系统Id
	 * @return 菜单列表
	 */
	public List<Map<String,Object>> getMenuByUserSysId(String userSysId);
	
	/**
	 * 系统管理员角色-年级
	 * @param xn 学年
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getAdminNJ(String xn,String xxdm);
	
	/**
	 * 系统管理员-班级
	 * @param xxdm 学校代码
	 * @param xnxq 
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getAdminBJ(String xxdm, String xnxq);
	
	/**
	 * 系统管理员-科目
	 * @return 科目列表
	 */
	public List<Map<String,Object>> getAdminKM();
	
	
	/**
	 * 任课教师-年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getRKJSNJ(String zgh,String xxdm);
	/**
	 * 任课教师-班级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getRKJSBJ(String zgh,String xxdm);
	/**
	 * 任课教师-科目
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目列表
	 */
	public List<Map<String, Object>> getRKJSKM(String zgh,String xxdm);
	
	/**
	 * 班主任-年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getBZRNJ(String zgh,String xxdm);
	
	/**
	 * 班主任-班级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getBZRBJ(String zgh,String xxdm);
	
	/**
	 * 班主任-科目
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目列表
	 */
	public List<Map<String,Object>> getBZRKM(String zgh,String xxdm);
	
	
	/**
	 * 年级组长-年级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getNJZZNJ(String nj,String zgh,String xxdm);
	
	/**
	 * 年级组长-班级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getNJZZBJ(String nj,String zgh,String xxdm);
	
	/**
	 * 年级组长-科目
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目代码
	 */
	public List<Map<String,Object>> getNJZZKM(String nj,String zgh,String xxdm);
	
	
	
	/**
	 * 教研组长-年级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getJYZZNJ(String nj,String zgh,String xxdm);
	
	/**
	 * 教研组长-班级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getJYZZBJ(String nj,String zgh,String xxdm);
	
	/**
	 * 教研组长-科目
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目代码
	 */
	public List<Map<String,Object>> getJYZZKM(String nj,String zgh,String xxdm);
	
	
	/**
	 * 备课组长-年级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getBKZZNJ(String nj,String zgh,String xxdm);
	
	/**
	 * 备课组长-班级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getBKZZBJ(String nj,String zgh,String xxdm);
	
	/**
	 * 备课组长-科目
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目代码
	 */
	public List<Map<String,Object>> getBKZZKM(String nj,String zgh,String xxdm);
	
	
	/**
	 * 自定义角色-年级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 年级列表
	 */
	public List<Map<String,Object>> getZDYNJ(String nj,String userSysId,String xxdm);
	
	/**
	 * 自定义角色-班级
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 班级列表
	 */
	public List<Map<String,Object>> getZDYBJ(String nj,String userSysId,String xxdm);
	
	/**
	 * 自定义角色-科目
	 * @param nj 年级
	 * @param zgh 职工号
	 * @param xxdm 学校代码
	 * @return 科目代码
	 */
	public List<Map<String,Object>> getZDYKM(String nj,String userSysId,String xxdm);

	/**
	 * 获取任课教师的权限列表
	 * @param map
	 * @return
	 */
    public List<JSONObject> getRkjsBJ(HashMap map);
    /**
     * 获取班主任的权限列表
     * @param map
     * @return
     */
    public List<JSONObject> getBzrBJ(HashMap map);

    /**
     * 获取教研组长具有权限的科目
     * @param map
     * @return
     */
    public List<JSONObject> getJyzzKM(HashMap map);
    /**
     * 获取备课组长具有权限的年级
     * @param map
     * @return
     */
    public List<JSONObject> getBkzzNJ(HashMap map);
    
    /**
     * 根据设定条件查询学校的菜单 条件：
     * xxdm,
     * countyCode:县代码
     * cityCode：市代码
     * provinceCode：省代码
     * role：角色
     */
    public List<NavInfo> updateGetNavListByRoleAndSchool(HashMap<String,Object> cxMap);

    /**
     * 微信公众号对接登陆成功绑定账号
     * @param accId
     * @param openId
     * @param sourceType
     */
	public Object updateMoblieLoginState(long accId, String openId,
			String sourceType,int status);

	public JSONObject getMoblieLoginState(  String openId, String sourceType);

	public JSONObject getNewEntranceSchool(HashMap<String, Object> cxMap);

	public JSONObject getExtIdByUserId(long uId, String termInfoId);

	public JSONObject getExtStudentByParentId(long uId, String termInfoId);

	public String getTokenByClientId(String clientId);
	
}
