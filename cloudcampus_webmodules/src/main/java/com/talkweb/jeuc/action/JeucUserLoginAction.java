package com.talkweb.jeuc.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.cas.utils.CASHttpClient;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.CsCurCommonDataService;
@Controller
@RequestMapping(value = "/jeuc/")
public class JeucUserLoginAction extends BaseAction {
	
	@Autowired
	private CsCurCommonDataService csCurService;
	
	ResourceBundle rb = ResourceBundle.getBundle("constant.dayDayUp" );
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "jumpAppPage.do" )
	@ResponseBody
	public void jumpAppPage(HttpServletRequest request ,
			@RequestParam("token") String jeucToken,
			@RequestParam("menuId") String menuId,
			HttpServletResponse response) {
		try{
			HttpSession session = request.getSession(true);
			//从URL获取接口调用的token信息,从配置文件中获取clientId,clientSecret
			JSONObject param = new JSONObject();
			String menuIds = rb.getString("jeuc.menuIds");
			if (StringUtils.isEmpty(jeucToken)||!menuIds.contains(menuId)){
				log.info("-----jeuc token is error or or menuId is not define!-----");
				printInfo(response, rb.getString("jeuc.prompt.info1"));
			}else{
				param.put("token", jeucToken);
			}
			param.put("clientId", rb.getString("jeuc.clientId_"+menuId));
			param.put("clientSecret", rb.getString("jeuc.clientSecret_"+menuId));
			//调用捷成教育提供的接口，获取用户的信息
			String serverUrl = rb.getString("jeuc.serverUrl");
			JSONObject userInfo = sendRequest(serverUrl, param);
			if (null == userInfo || !userInfo.containsKey("ret") 
					|| !"1".equals(userInfo.getString("ret"))){
				log.info("-----the interface call returns an error!------");
				printInfo(response, rb.getString("jeuc.prompt.info2"));
			}
			// 从长沙OMS中查询相关的用户信息,学校信息构建session
			String userRole = userInfo.getString("userRole");
			String idCard = "",phone = "",stuNo=""; 
			List<Integer> roleList = new ArrayList<Integer>();
			String termInfoId = rb.getString("jeuc.currentTermInfo");
			if ("stu".equals(userRole)){
				JSONObject stu = userInfo.getJSONObject("studentInfo");
				idCard = stu.getString("stuIdCard");
				roleList.add(2);
				phone = stu.getString("stuPhone");
				stuNo = stu.getString("stuNo");
			}else if("tea".equals(userRole)){
				JSONObject tea = userInfo.getJSONObject("teacherInfo");
				idCard = tea.getString("teaIdCard");
				roleList.add(1);
				roleList.add(4);
				phone = tea.getString("teaTel");
			}else if("parents".equals(userRole)){
				JSONObject parent = userInfo.getJSONObject("parentsInfo");
				roleList.add(0);
				phone = parent.getString("parentsPhone");
				stuNo = parent.getString("parentsStuNo");
			}
			if ((StringUtils.isEmpty(idCard)&&StringUtils.isEmpty(phone)
					&& StringUtils.isEmpty(stuNo))||CollectionUtils.isEmpty(roleList)){
				log.info("-----the interface info is not full!-----");
				printInfo(response, rb.getString("jeuc.prompt.info3"));
			}
			JSONObject condition = new JSONObject();   
			condition.put("idNumber", idCard);
			condition.put("roleList", roleList);
			condition.put("stuNo", stuNo);
			condition.put("mobilePhone", phone);
			condition.put("termInfoId", termInfoId);

	        List<JSONObject> userList = csCurService.getUserIdByConditon(condition);	             
			
	        /***-----获取所有的学校关联平台表数据----- */
	    	if (CollectionUtils.isNotEmpty(userList)){
	    		JSONObject userObj = userList.get(0);
	    		if (userObj==null){
	    			log.info("------jeuc userObj[userId] is null!-------");
	    			printInfo(response, rb.getString("jeuc.prompt.info4"));
	            }
	    		long userId = userObj.getLongValue("userId");
	    		User user = csCurService.getUserById(-1000,userId,termInfoId);
	    		if (user==null){
	    			log.info("------jeuc user info is null!------");
	    			printInfo(response, rb.getString("jeuc.prompt.info5"));
	            }
	    		School sch = csCurService.getSchoolByUserId(-1000,userId,termInfoId);
	    		if (sch==null||sch.getId()==0l ){
	    			log.info("------jeuc school info is null!------");
	    			printInfo(response, rb.getString("jeuc.prompt.info6"));
	            }
	    		long schoolId = sch.getId();
	    		//如果身份不是000 就 获取学校代码
	    		session.setAttribute("xxdm", schoolId+"");
	    		session.setAttribute("user", user);
	    		session.setAttribute("school", sch);
	    		//设置session有效期10小时
	    		session.setMaxInactiveInterval(3600*10);
	     	
	        	session.setAttribute("userId", userId);
	        	session.setAttribute("userName", userObj.getString("accountName"));
	        	session.setAttribute("accountId", user.getAccountPart().getId());
	        	session.setAttribute("account", user.getAccountPart());
	        	session.setAttribute("curXnxq", termInfoId);
	        	session.setAttribute("isTeaching", false); 
	    	}else{
	    		printInfo(response, rb.getString("jeuc.prompt.info4"));
	    	}
	    	//本过滤器处理完成，处理下个过滤器
	    	if (StringUtils.isNotEmpty(menuId)) {
	    		String domain = rb.getString("jeuc.domain");
	    		String urlSuffix = rb.getString("menu_jeuc_" + menuId);
	    		if (StringUtils.isEmpty(urlSuffix))
	    		{
	    			printInfo(response, rb.getString("jeuc.prompt.info7"));
	    		}else{
	    			response.sendRedirect(domain + urlSuffix);	
					return;
	    		}			
			}
		} catch (Exception e) {  
	    	log.info("a servletException is appear!"); 
	    }  
	}

	private void printInfo(HttpServletResponse response, String message)
			throws IOException {
		PrintWriter out = response.getWriter();
		out.print("<html><head><meta charset='UTF-8'></head>");
		out.println("<script>");
		out.println("alert('" + message + "');");
		out.println("</script>");
		return;
	}

	private JSONObject sendRequest(String url, JSONObject param) {
		return JSON.parseObject(CASHttpClient.callHttpRemoteInterface(url, param));		
	}
	
}