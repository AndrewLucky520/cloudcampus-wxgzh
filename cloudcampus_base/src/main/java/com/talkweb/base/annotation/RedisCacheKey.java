/**
 * 
 */
package com.talkweb.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: RedisCacheKey
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年4月15日
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ElementType.PARAMETER}) 
public @interface RedisCacheKey {

}
