package com.talkweb.common.utils;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(1)
public class MultipleDataSourceAspectAdvice{
	private static ResourceBundle rb = ResourceBundle.getBundle("config.datasource" );
	private static int readDatasourceCount = Integer.parseInt(rb.getString("dataSource.read.count"));
	private static String matchRule = rb.getString("dataSource.read.matchRule");
	private static final Logger logger = LoggerFactory.getLogger(MultipleDataSourceAspectAdvice.class);
	
    @Before("execution(public * com.talkweb.*.dao.*.*(..))")
    public void selectDataSource(JoinPoint joinPoint){ 
    	String shortMethodName = joinPoint.getSignature().getName();
        // 验证规则
        String regEx = String.format("^(%s).*$",matchRule);
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(shortMethodName);
        // 字符串是否与正则表达式相匹配
        if(matcher.matches()){
        	//读库
        	String dataSourceKey = "readDataSource"+(int) (System.currentTimeMillis()%10%readDatasourceCount+1);
            MultipleDataSource.setDataSourceKey(dataSourceKey);
            logger.debug(String.format("method:%s----type:read---dataSourceKey:%s", joinPoint.getSignature().toString(),dataSourceKey));
        }else{
        	//写库
        	String longMethodName = joinPoint.getSignature().getDeclaringType().getName();
        	String[] words = longMethodName.split("\\.");
        	String moduleName = "dataSource.write."+ words[2];
        	String dataSourceKey = null;
            if(rb.containsKey(moduleName)){
              	dataSourceKey = rb.getString(moduleName);
            }else{
            	dataSourceKey = "writeDefaultDataSource";
            }
            MultipleDataSource.setDataSourceKey(dataSourceKey);
            logger.debug(String.format("method:%s----type:write---dataSourceKey:%s", joinPoint.getSignature().toString(),dataSourceKey));
        }
    }
    
}
