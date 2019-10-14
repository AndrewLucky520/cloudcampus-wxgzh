/**
 * 
 */
package com.talkweb.aspect.controller;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.talkweb.base.annotation.SystemControllerLog;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;
import com.talkweb.systemManager.domain.business.TWsSyslog;
import com.talkweb.systemManager.service.LogService;


/**
 * @ClassName: SystemLogAspect
 * @version:1.0
 * @Description: 记录日志切面
 * @author 廖刚 ---智慧校
 * @date 2015年3月13日
 */
//@Component
//@Aspect
public class SystemLogAspect {

	//注入Service用于把日志保存数据库
	//@Resource(name="logServiceImpl")
//	@Resource(name="logServiceImplMongodb")
	private LogService logService;
	
	//本地异常日志记录对象
	private static final Logger logger = LoggerFactory.getLogger(SystemLogAspect.class);
		
	//Controller层日志切点
	@Pointcut("@annotation(com.talkweb.base.annotation.SystemControllerLog)")
	public  void controllerLogAspect() {
	}
	//Controller层异常切点
	@Pointcut("execution(public * com.talkweb.*.action..*.*(..))")
	public  void controllerAspect() {
	}
	
	/** 
     * 后置通知：用于拦截Controller层记录用户的操作 
     * 
     * @param joinPoint 切点 
     */
	@After("controllerLogAspect()")
	public void doAfter(JoinPoint joinPoint) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		//获取前端传过来的当前操作参数：菜单、事件
		String menuId = request.getParameter("menuId");
		String menuName = request.getParameter("menuName");
		String opertype = request.getParameter("opertype");
		String userEvent = request.getParameter("userEvent");
		//请求的IP
		String ip = request.getRemoteAddr();
		//获取session保存值：学校、用户
		HttpSession session = request.getSession();
		TTrSchool school = (TTrSchool) session.getAttribute("school");
		TUcUser user = (TUcUser) session.getAttribute("user");
		try {
			Map<String, String> descMap = getControllerMethodDescription(joinPoint);
			if ((descMap != null) && (descMap.size() > 0)) {
				TWsSyslog log = new TWsSyslog();
				log.setUsersysid(user.getUsersysid());
				log.setSf(user.getSf());
				log.setMenuid(menuId);
				log.setMenuname(menuName);
				log.setOpertype(opertype);
				log.setUserevent(userEvent);
	            log.setFunctionalid(descMap.get("functionalId"));
	            log.setFunctionaldescription(descMap.get("functionalDescription"));
	            log.setEventid(descMap.get("eventId"));
	            log.setEventdescription(descMap.get("eventDescription"));
	            log.setXxdm(school.getXxdm());
	            log.setIp(ip);
	            log.setLogtype("01");
	            //保存数据库  
	            logService.insertLog(log);
			}
        }  catch (Exception e) {
            //记录本地异常日志 
            logger.error("Controller层切面运行异常:{}", e.getMessage());
        }
    }
  
	
    /** 
     * 异常通知：用于拦截service层记录异常日志 
     * 
     * @param joinPoint 
     * @param e 
     */
//	@AfterThrowing(pointcut = "controllerAspect()", throwing = "e") 
	public  void doAfterThrowing(JoinPoint joinPoint, Throwable e) {		
		//获取用户请求方法的参数并序列化为JSON格式字符串 
		String params = "";
		if (joinPoint.getArgs() !=  null && joinPoint.getArgs().length > 0) {
			for ( int i = 0; i < joinPoint.getArgs().length; i++) {
				params += JSON.toJSONString(joinPoint.getArgs()[i]) + ";";
			}
		}
		
		try {
			//以后增加异常监控
			logger.error("异常方法:{}异常代码:{}异常信息:{}参数:{}", joinPoint.getTarget().getClass().getName() + joinPoint.getSignature().getName(), e.getClass().getName(), e.getMessage(), params);
        }  catch (Exception ex) {
            //记录本地异常日志 
            logger.error("controller层切面异常信息:{}", ex.getMessage());
        }
    }  
  
  	/** 
     * 获取注解中对方法的描述信息,用于Controller层注解 
     * 
     * @param joinPoint 切点 
     * @return 方法描述 
     * @throws Exception 
     */
	@SuppressWarnings("null")
	public static Map<String,String> getControllerMethodDescription(JoinPoint joinPoint)
			throws Exception {
		String targetName = joinPoint.getTarget().getClass().getName();
		String methodName = joinPoint.getSignature().getName();
		Object[] arguments = joinPoint.getArgs();
		Class<?> targetClass = Class.forName(targetName);
		Method[] methods = targetClass.getMethods();		
		Map<String,String> descMap = new HashMap<String,String>();
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {		
				@SuppressWarnings("rawtypes")
				Class[] clazzs = method.getParameterTypes();
				if (clazzs.length == arguments.length) {
					if (method.getAnnotation(SystemControllerLog.class).equals(null)) {
						descMap.put("functionalId",targetName);
						descMap.put("eventId".toString(),methodName);
					} else {
						String description = method.getAnnotation(SystemControllerLog.class).toString();
						description = description.substring(description.indexOf("(") + 1, description.indexOf(")"));
						String[] descSplit = description.split(",");
						for( int i = 0; i < descSplit.length; i++) {
							String[] temp = descSplit[i].split("=");
							switch(temp.length) {
							case 2:
								descMap.put(temp[0].trim(), temp[1].trim());
								break;
							case 1:
								temp[0] = temp[0].trim();
								if (temp[0].equalsIgnoreCase("functionalId")) {
									descMap.put(temp[0].toString(),targetName);
								} else if (temp[0].equalsIgnoreCase("eventId")) {
									descMap.put(temp[0].toString(),methodName);
								} else {
									descMap.put(temp[0].toString(),"");
								}								
								break;
							}
						}
					}
					break;
				}
			}
		}
		return descMap;
	}
	
	
}
