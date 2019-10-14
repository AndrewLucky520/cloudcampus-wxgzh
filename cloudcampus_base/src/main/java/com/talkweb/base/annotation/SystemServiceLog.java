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
 * @ClassName: SystemServiceLog
 * @version:1.0
 * @Description: 自定义注解 拦截service 
 * @author 廖刚 ---智慧校
 * @date 2015年3月13日
 */

@Target({ElementType.PARAMETER, ElementType.METHOD})  
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SystemServiceLog {
	String description() default "";
}
