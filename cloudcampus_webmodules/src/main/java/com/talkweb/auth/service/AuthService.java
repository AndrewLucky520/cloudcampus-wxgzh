package com.talkweb.auth.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.auth.entity.NavInfo;

/**
 * @ClassName AuthService
 * @author Homer
 * @version 1.0
 * @Description 权限控制Service
 * @date 2015年3月9日
 */
public interface AuthService {

	
	
    /**
     * 获取权限树
     * @param usersysid
     * @param session
     * @return
     */
	public  HashMap<String,HashMap<String,HashMap<String,JSONObject>>> getAllRightByParam( HttpSession session,String xnxq);
	
	
	/**
	 * 根据账号id和模块代码获取是否模块管理员
	 * @param accountId
	 * @param appId 模块代码
	 * @return
	 */
	public boolean getIsMoudleManagerByAccountId(long schoolId,long accountId,String appId,String xnxq);
	
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
	public void updateMoblieLoginState(long accId, String openId,
			String sourceType,int status);
	
	public JSONObject getMoblieLoginState(  String openId,
			String sourceType);


	public JSONObject getNewEntranceSchool(HashMap<String, Object> cxMap);


	public JSONObject getExtIdByUserId(long uId, String termInfoId);


	public JSONObject getExtStudentByParentId(long uId, String termInfoId);
}
