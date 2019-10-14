/**
 * 
 */
package com.talkweb.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: SystemControllerLog
 * @version:1.0
 * @Description: 自定义注解 拦截Controller
 * @author 廖刚 ---智慧校
 * @date 2015年3月12日
 */

@Target({ElementType.PARAMETER, ElementType.METHOD})  
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SystemControllerLog {
	
	//功能代码
	String functionalId() default "";
	//功能描述
	String functionalDescription() default "";
	//事件代码
	String eventId() default "";
	//事件描述
	String eventDescription() default "";
	
}
