package com.talkweb.auth.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.auth.dao.AuthDao;
import com.talkweb.auth.entity.NavInfo;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;

/**
 * @ClassName AuthDaoImpl
 * @author Homer
 * @version 1.0
 * @Description 权限控制Dao实现类
 * @date 2015年3月9日
 */
@Repository
public class AuthDaoImpl extends MyBatisBaseDaoImpl implements AuthDao {

	@Override
	public List<Map<String, Object>> getMenuByUserSysId(String userSysId) {
		List<Map<String,Object>> menus = selectList("getMenuByUserSysId", userSysId);
		return menus;
	}
	
	@Override
	public List<Map<String, Object>> getAdminNJ(String xn, String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xn", xn);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getAdminNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getAdminBJ(String xxdm,String xnxq) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("xxdm", xxdm);
		params.put("xnxq", xnxq);
		List<Map<String, Object>> bjs = selectList("getAdminBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getAdminKM() {
		List<Map<String, Object>> kms = selectList("getAdminKM");
		return kms;
	}
	
	@Override
	public List<Map<String, Object>> getRKJSNJ(String zgh,String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getRKJSNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getRKJSBJ(String zgh,String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getRKJSBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getRKJSKM(String zgh,String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getRKJSKM", params);
		return kms;
	}

	@Override
	public List<Map<String, Object>> getBZRNJ(String zgh, String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getBZRNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getBZRBJ(String zgh, String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getBZRBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getBZRKM(String zgh, String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getBZRKM", params);
		return kms;
	}

	@Override
	public List<Map<String, Object>> getNJZZNJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getNJZZNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getNJZZBJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getNJZZBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getNJZZKM(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getNJZZKM", params);
		return kms;
	}

	@Override
	public List<Map<String, Object>> getJYZZNJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getJYZZNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getJYZZBJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getJYZZBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getJYZZKM(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getJYZZKM", params);
		return kms;
	}

	@Override
	public List<Map<String, Object>> getBKZZNJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getBKZZNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getBKZZBJ(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getBKZZBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getBKZZKM(String nj, String zgh,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", zgh);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getBKZZKM", params);
		return kms;
	}

	@Override
	public List<Map<String, Object>> getZDYNJ(String nj, String userSysId,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", userSysId);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> njs = selectList("getZDYNJ", params);
		return njs;
	}

	@Override
	public List<Map<String, Object>> getZDYBJ(String nj, String userSysId,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", userSysId);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> bjs = selectList("getZDYBJ", params);
		return bjs;
	}

	@Override
	public List<Map<String, Object>> getZDYKM(String nj, String userSysId,
			String xxdm) {
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("nj", nj);
		params.put("zgh", userSysId);
		params.put("xxdm", xxdm);
		List<Map<String, Object>> kms = selectList("getZDYKM", params);
		return kms;
	}

    @Override
    public List<JSONObject> getRkjsBJ(HashMap map) {
        // TODO Auto-generated method stub
        List<JSONObject> list = selectList("getNj001",map);
        return list;
    }

    @Override
    public List<JSONObject> getBzrBJ(HashMap map) {
        String role = (String) map.get("roleId");
        String rl = role.substring(3,role.length());
        List<JSONObject> list = selectList("getNj"+rl,map);
        return list;
    }

    @Override
    public List<JSONObject> getJyzzKM(HashMap map) {
        // TODO Auto-generated method stub
        List<JSONObject> list = selectList("getJyzzKM",map);
        return list;
    }

    @Override
    public List<JSONObject> getBkzzNJ(HashMap map) {
        // TODO Auto-generated method stub
        List<JSONObject> list = selectList("getBkzzNJ",map);
        return list;
    }

	@Override
	public List<NavInfo> updateGetNavListByRoleAndSchool(HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return selectList("getNavListByRoleAndSchool",cxMap);
	}

	@Override
	public Object updateMoblieLoginState(long accId, String openId,
			String sourceType,int status) {
		// TODO Auto-generated method stub
		Map<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("accountId",accId);
		cxMap.put("openId",openId);
		cxMap.put("sourceType",sourceType);
		cxMap.put("status",status);
		
		return update("updateMoblieLoginState",cxMap);
	}

	@Override
	public JSONObject getMoblieLoginState( String openId, String sourceType) {
		// TODO Auto-generated method stub
		Map<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("openId",openId);
		cxMap.put("sourceType",sourceType);
		JSONObject rs  = new JSONObject();
		int state = 0;
		long accountId = 0l;
		if(openId!=null&&openId.trim().length()>0&&sourceType!=null&&sourceType.trim().length()>0){
			
			JSONObject temp = selectOne("getMobileLoginState",cxMap);
			if(temp!=null){
				int status = temp.getIntValue("status");
				if(status==1){
					
					java.sql.Date lastLoginTime = temp.getSqlDate("loginTime");
					if(lastLoginTime!=null){
						long lst = lastLoginTime.getTime();
						long now = new java.util.Date().getTime();
						long dt = now - lst;
						if(dt<1l*30*24*60*60*1000){
							state = 1;
							accountId = temp.getLongValue("accountId");
						}
					}
				}
			}
		}
		
		rs.put("status", state);
		rs.put("accountId", accountId);
		
		return rs;
	}

	@Override
	public JSONObject getNewEntranceSchool(HashMap<String, Object> cxMap) {
		return selectOne("getNewEntranceSchool",cxMap);
	}

	@Override
	public JSONObject getExtIdByUserId(long uId,String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("userId", uId);
		return selectOne("getExtIdByUserId",param);
	}

	@Override
	public JSONObject getExtStudentByParentId(long uId, String termInfoId) {
		JSONObject param = new JSONObject();
		param.put("termInfoId", termInfoId);
		param.put("userId", uId);
		return selectOne("getExtStudentByParentId",param);
	}

	@Override
	public String getTokenByClientId(String clientId) {
		JSONObject param = new JSONObject();
		param.put("clientId", clientId);
		return selectOne("getTokenByClientId",param);
	}

}
