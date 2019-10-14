package com.talkweb.filter;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.cas.service.CASUserLoginService;

public class YDUserLoginFilter implements Filter {
	
	private CASUserLoginService loginService;
	
	ResourceBundle rb = ResourceBundle.getBundle("constant.casconfig" );

	protected final Log log = LogFactory.getLog(getClass());

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext sc = filterConfig.getServletContext(); 
        XmlWebApplicationContext cxt = (XmlWebApplicationContext)WebApplicationContextUtils.getWebApplicationContext(sc);
        
        if (cxt != null && cxt.getBean("casLoginService") != null 
        		&& loginService == null)
        {
        	loginService = (CASUserLoginService) cxt.getBean("casLoginService");
        }  	
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		try{  
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			HttpSession session = request.getSession();
			//加上自己应用的逻辑，例如构造用户的信息和权限等
            AttributePrincipal principal = (AttributePrincipal)request.getUserPrincipal();  
            Map<String,Object> attributes = principal.getAttributes();
            if (null == attributes .get("userId")){
            	log.info("------userId info is null!------");
    			request.getRequestDispatcher("/cas_error.html").forward(request, response);
    			return;
            }
            String user_Id = attributes.get("userId").toString();
            if (StringUtils.isNotEmpty(user_Id)){
            	long userId = Long.parseLong(user_Id);
            	User user = loginService.getUserById(-1000,userId);
        		if (user==null){
        			log.info("------user info is null!------");
        			request.getRequestDispatcher("/cas_error.html").forward(request, response);
        			return;
        		}
        		School sch = loginService.getSchoolByUserId(-1000,userId);
	    		if (sch==null||sch.getId()==0l ){
	    			log.info("------school info is null!------");
	    			request.getRequestDispatcher("/cas_error.html").forward(request, response);
        			return;
	            }
	    		long schoolId = sch.getId();
	    		String xnxq = rb.getString("currentTermInfo");
	    		if(xnxq==null||xnxq.trim().length()==0){
	    			log.info("------termInfo info is null!------");
	    			request.getRequestDispatcher("/cas_error.html").forward(request, response);
        			return;
	    		}
	    		String roleName = user.getUserPart().getRole().name();
	    		if("SchoolManager".equals(roleName))
	    		{
	    			session.setAttribute("isManager",true);
	    		}else{
	    			session.setAttribute("isManager",false);
	    		}
	    		//如果身份不是000 就 获取学校代码
	    		session.setAttribute("xxdm", schoolId+"");
	    		session.setAttribute("user", user);
	    		session.setAttribute("school", sch);
	    		//设置session有效期10小时
	    		session.setMaxInactiveInterval(3600*10);
	        	session.setAttribute("userId", userId);
	        	session.setAttribute("accountId", user.getAccountPart().getId());
	        	session.setAttribute("account", user.getAccountPart());
	        	session.setAttribute("curXnxq", xnxq);
	        	session.setAttribute("isTeaching", false);	
	        		
	        	// 初始化REDIS中保存的学校信息
	        	loginService.setRedisSchoolPlateKey();        	
            }else{
            	log.info("get user info is error!!!");
            	request.getRequestDispatcher("/cas_error.html").forward(request, response);
            	return;	
            }
            String url = request.getRequestURL().toString();
	    	//本过滤器处理完成，处理下个过滤器
	    	if (url.endsWith("/newNEMT")||url.endsWith("/newNEMT/")) {
				response.sendRedirect(rb.getString("cas.newNEMT.url"));	
				return;
			}
	    	filterChain.doFilter(request, response); 
	    } catch (Exception e) {  
	    	throw new ServletException(e);  
	    }  
	}

	public void destroy() {}
	
}