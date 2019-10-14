package com.talkweb.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PathConvertFilter implements Filter {
	private static ResourceBundle rb = ResourceBundle.getBundle("constant.oauthconfig" );
	private String homeUrl=  rb.getString("oauth.home.url");
	private String baseUrl=  rb.getString("oauth.base.url");
	private static String authUrl ;
	static{
		try {
			authUrl = rb.getString("getAuCodeUrl")+URLEncoder.encode(rb.getString("redirectUrl"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		try{  
			HttpServletRequest request = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			boolean isLogin = false;
			if(request.getSession().getAttribute("accessToken") != null){
				isLogin = true;
			}
			//页面跳转
			if(request.getRequestURI().indexOf("getApplication")>0){
        		String menuFlag = request.getParameter("menuFlag");
        		String redirectUrl =  homeUrl+"#"+menuFlag;
        		if(!isLogin){
        			redirectUrl = authUrl+URLEncoder.encode(URLEncoder.encode(redirectUrl,"utf-8"),"utf-8");
        		}
        		response.sendRedirect(redirectUrl);
        		return;
        	}else if(request.getRequestURI().indexOf("getBaseDataManage")>0){
        		String menuFlag = request.getParameter("menuFlag");
        		String redirectUrl =  baseUrl+"#"+menuFlag;
        		if(!isLogin){
        			redirectUrl = authUrl+URLEncoder.encode(URLEncoder.encode(redirectUrl,"utf-8"),"utf-8");
        		}
        		response.sendRedirect(redirectUrl);
        		return;
        	}	
			filterChain.doFilter(request, response); 
	    } catch (Exception e) {     	
	    	throw e;  
	    }  
	}
	

	@Override
	public void destroy() {
		
	}


	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}
}