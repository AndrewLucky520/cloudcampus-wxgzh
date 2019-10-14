package com.talkweb.system.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.talkweb.systemManager.domain.business.TUcUser;

public class LoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest servletRequest = (HttpServletRequest)request;
		HttpServletResponse servletResponse = (HttpServletResponse)response;
		HttpSession session = servletRequest.getSession();
		
		String path = servletRequest.getRequestURI();
		
		
		TUcUser user = (TUcUser) session.getAttribute("user");
		
		
		if(path.indexOf("index.html") > -1){
			chain.doFilter(servletRequest, servletResponse);
			return;
		}
		
		if(null == user){
			String path2 = servletRequest.getContextPath();
			String basePath = servletRequest.getScheme()+"://"+servletRequest.getServerName()+":"+request.getServerPort()+path2;
			servletResponse.sendRedirect(basePath+"/page/index.html");
		}else{
			chain.doFilter(servletRequest, servletResponse);
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
