package com.talkweb.common.action;

import java.util.Comparator;

import com.alibaba.fastjson.JSONObject;

/** 
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年8月10日 上午10:47:25 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
public class ListComparetor implements Comparator<Object>{
    @Override
    public int compare(Object o1, Object o2)
   {

         JSONObject obj1= (JSONObject )o1;
         JSONObject obj2= (JSONObject )o2;	         
         return  (int) (obj1.getLongValue("value")-obj2.getLongValue("value"));
   }
}
