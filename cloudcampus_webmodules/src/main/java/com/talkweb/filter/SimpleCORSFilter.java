/**
 * 
 */
package com.talkweb.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @ClassName: SimpleCORSFilter
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年5月11日
 */
public class SimpleCORSFilter extends OncePerRequestFilter {

	/**
	 * 
	 */
	public SimpleCORSFilter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req,
			HttpServletResponse res, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpServletResponse response = (HttpServletResponse) res;
	    response.setHeader("Access-Control-Allow-Origin", "*");
	    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
	    response.setHeader("Access-Control-Max-Age", "3600");
	    response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
	    filterChain.doFilter(req, res);

	}

}
