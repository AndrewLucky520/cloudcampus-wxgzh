package com.talkweb.dayDayUpLogin.action;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.AESUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CsCurCommonDataService;
import com.talkweb.commondata.service.SchoolPlateService;
import com.talkweb.systemManager.service.LoginService;


/**
 * 松耦合接入云校园登录Action
 * @author zhanghuihui
 * @date 2017.09.05
 */
@Controller
@RequestMapping(value="/loginLoose/")
public class LoginLooseAction {
	private static final Logger logger = LoggerFactory.getLogger(LoginLooseAction.class);
	
	@Autowired
	private SchoolPlateService schoolPlateService;
	@Autowired
	private AuthService authServiceImpl;
	@Autowired
	private AllCommonDataService allCommonDataService;
	@Autowired
	private CsCurCommonDataService csCurCommonDataService;
	@Autowired
	private LoginService loginService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	@Value("#{settings['currentTermInfo']}")
	private String termInfoId;
	@Value("#{settings['aes.decrypt']}")
	private String decrypt;
	@Value("#{settings['aes.key']}")
	private String key;
	ResourceBundle rb = ResourceBundle.getBundle("constant.dayDayUp" );
	
	/**
	 * 松耦合接入入口（暂不支持深圳SDK方式）
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 * @author zhanghuihui
	 */
	@SuppressWarnings("resource")
	@RequestMapping(value = "/getloginUrl", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getloginUrl(@RequestBody JSONObject requestParams,HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		String code = "-1";
		String msg = "系统错误";
		
		String AESEncryptInfo = requestParams.getString("AESEncryptInfo");
		String roleString = requestParams.getString("role");
		String menuId = requestParams.getString("menuId");
		JSONObject userInfo = (JSONObject) requestParams.get("userInfo");
		String role = "1";
		if(StringUtils.isBlank(AESEncryptInfo)){
			json.put("code", code);
			json.put("msg", "密匙参数为空");
			return json;
		}
		if(StringUtils.isBlank(roleString)){
			json.put("code", code);
			json.put("msg", "角色参数为空");
			return json;
		}
		if(StringUtils.isBlank(menuId)){
			json.put("code", code);
			json.put("msg", "菜单参数为空");
			return json;
		}
		if(userInfo==null){
			json.put("code", code);
			json.put("msg", "身份信息参数为空");
			return json;
		}
		if("Teacher".equals(roleString)){
			role="1";
			if(!(userInfo.containsKey("phone") || userInfo.containsKey("idCardNo") || userInfo.containsKey("schoolName")&& userInfo.containsKey("name")) ){
				json.put("code", code);
				json.put("msg", "老师身份信息参数不准确");
				return json;
			}
		}
		if("Student".equals(roleString)){
			role="2";
			if(!(userInfo.containsKey("areaStudentCode") || userInfo.containsKey("schoolName")&& userInfo.containsKey("className")&&userInfo.containsKey("name") || userInfo.containsKey("idCardNo")) ){
				json.put("code", code);
				json.put("msg", "学生身份信息参数不准确");
				return json;
			}
		}
		if("Parent".equals(roleString)){
			role="3";
			if(!(userInfo.containsKey("phone") || userInfo.containsKey("schoolName")&& userInfo.containsKey("className")&&userInfo.containsKey("name") || userInfo.containsKey("idCardNo")) ){
				json.put("code", code);
				json.put("msg", "学生身份信息参数不准确");
				return json;
			}
		}
		String AESEncryptInfoDecrypt = "";
		 try {
			AESEncryptInfoDecrypt = AESUtil.Decrypt(AESEncryptInfo,decrypt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 String userId = "";
		 String type ="";
		 //String fromPlate =  "2"; //"1"长沙， "2"深圳 ，"3"教育云   
		 if(key.equals(AESEncryptInfoDecrypt)){
			 if("Teacher".equals(roleString)){
				String phone = userInfo.getString("phone");
				String idCardNo = userInfo.getString("idCardNo");
				String schoolName = userInfo.getString("schoolName");
				String name = userInfo.getString("name");
				if(StringUtils.isNotBlank(phone)){
					type="11";
				}else if(StringUtils.isNotBlank(idCardNo)){
					type="13";
				}else{
					type="12";
				}
				JSONObject param = new JSONObject();
				param.put("mobilePhone", phone);
				param.put("idNumber", idCardNo);
				param.put("schoolName", schoolName);
				param.put("name", name);
				param.put("role", role);
				param.put("termInfoId", termInfoId);
				param.put("type", type);
				List<JSONObject> list = csCurCommonDataService.getAccountByRoleAndCondition(param);
				if(list==null|| list.size()==0){
					json.put("code", code);
					json.put("msg", "查询不到用户信息");
					 return json;
				}else{
					JSONObject obj = list.get(0);
					userId = obj.getString("userId");
				}
			 }else if("Student".equals(roleString)){
					String areaStudentCode = userInfo.getString("areaStudentCode");
					String idCardNo = userInfo.getString("idCardNo");
					String schoolName = userInfo.getString("schoolName");
					String className = userInfo.getString("className");
					String name = userInfo.getString("name");
					if(StringUtils.isNotBlank(areaStudentCode)){
						type="21";
					}else if(StringUtils.isNotBlank(idCardNo)){
						type="23";
					}else{
						type="22";
					}
					JSONObject param = new JSONObject();
					param.put("stdNumber", areaStudentCode);
					param.put("idNumber", idCardNo);
					param.put("schoolName", schoolName);
					param.put("className", className);
					param.put("name", name);
					param.put("termInfoId", termInfoId);
					param.put("type", type);
					List<JSONObject> list = csCurCommonDataService.getAccountByRoleAndCondition(param);
					if(list==null|| list.size()==0){
						json.put("code", code);
						json.put("msg", "查询不到用户信息");
						 return json;
					}else{
						JSONObject obj = list.get(0);
						userId = obj.getString("userId");
					}
			 }else{
					String phone = userInfo.getString("phone");
					String idCardNo = userInfo.getString("idCardNo");
					String schoolName = userInfo.getString("schoolName");
					String className = userInfo.getString("className");
					String name = userInfo.getString("name");
					if(StringUtils.isNotBlank(phone)){
						type="31";
					}else if(StringUtils.isNotBlank(idCardNo)){
						type="33";
					}else{
						type="32";
					}
					JSONObject param = new JSONObject();
					param.put("mobilePhone", phone);
					param.put("idNumber", idCardNo);
					param.put("schoolName", schoolName);
					param.put("className", className);
					param.put("name", name);
					param.put("termInfoId", termInfoId);
					param.put("type", type);
					List<JSONObject> list = csCurCommonDataService.getAccountByRoleAndCondition(param);
					if(list==null|| list.size()==0){
						json.put("code", code);
						json.put("msg", "查询不到用户信息");
						 return json;
					}else{
						JSONObject obj = list.get(0);
						userId = obj.getString("userId");
					}
			 }
			 if(StringUtils.isNotBlank(userId)){
				 String refreshToken = UUIDUtil.getUUID();
				 Object key = "loginLoose."+refreshToken+".00.userId";
				try {
					redisOperationDAO.set( key, userId,CacheExpireTime.temporaryDataMinExpireTime.getTimeValue()  );
				} catch (Exception e) {
					e.printStackTrace();
				}
				 String progressName = req.getContextPath();//工程名
				 String requestUrl=rb.getString("localUrl")+progressName;
				 String redirectUrl = requestUrl+"/loginLoose/gotoAppUrl.do"+"?refreshToken="+refreshToken+"&menuId="+menuId;
				 JSONObject data = new JSONObject();
				 data.put("redirectUrl", redirectUrl);
				 json.put("data", data);
				 json.put("code", 1);
				 json.put("msg", "success");
				 return json;
			 }else{
				 json.put("code", code);
				 json.put("msg", msg); //系统错误
				 return json;
			 }
		 }else{
			 json.put("code", code);
			 json.put("msg", "加密串参数验证不通过");
			 return json;
		 }
	}
	/**
	 * 跳入功能页面
	 * @param req
	 * @param res
	 * @author zhanghuihui
	 */
	@SuppressWarnings("resource")
	@RequestMapping(value = "/gotoAppUrl", method = RequestMethod.GET)
	public void gotoAppUrl(HttpServletRequest req,HttpServletResponse res){
		String refreshToken = req.getParameter("refreshToken");
		String menuId = req.getParameter("menuId");
		Object key = "loginLoose."+refreshToken+".00.userId";
		String userId = "";
		try {
			userId = (String) redisOperationDAO.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//重写SESSION
		JSONObject loginReturn= new JSONObject();
		try {
			if(StringUtils.isNotBlank(userId)){
				loginReturn = loginService.setSessionAndGetLoginStatus(req.getSession(), Long.parseLong(userId));
			}
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		 if(loginReturn!=null ){
			 String code1 = loginReturn.getString("code");
			 if("1".equals(code1)){
				 //登录成功
				 //跳转
				 String redirectUrl = rb.getString("menu_"+menuId);
				 try {
					res.sendRedirect(redirectUrl);
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		 }
	}
	
	
}
