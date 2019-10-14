package com.talkweb.common.tools;

import java.lang.reflect.Method;
import java.util.Iterator;

import com.alibaba.fastjson.JSONObject;
/**
 * @ClassName: BeanUtil.java	
 * @version:1.0
 * @Description: POJO浅复制等相关功能
 * @author 武洋 ---智慧校
 * @date 2015年3月11日
 */
public class BeanTool {

    /**
     * @param obj1  //源对象
     * @param obj2  //目标对象
     * @return
     * @throws Exception
     */
    public static Object CopyBeanToBean(Object obj1, Object obj2) throws Exception {

        Method[] method1 = obj1.getClass().getMethods();

        Method[] method2 = obj2.getClass().getMethods();

        String methodName1;

        String methodFix1;

        String methodName2;

        String methodFix2;

        for (int i = 0; i < method1.length; i++) {

            methodName1 = method1[i].getName();

            methodFix1 = methodName1.substring(3, methodName1.length());

            if (methodName1.startsWith("get")) {

                for (int j = 0; j < method2.length; j++) {

                    methodName2 = method2[j].getName();

                    methodFix2 = methodName2.substring(3, methodName2.length());

                    if (methodName2.startsWith("set")) {

                        if (methodFix2.equals(methodFix1)) {

                            Object[] objs1 = new Object[0];

                            Object[] objs2 = new Object[1];
                            objs2[0] = method1[i].invoke(obj1, objs1);// 调用obj1的相应的get的方法，objs1数组存放调用该方法的参数,此例中没有参数，该数组的长度�?
                            if (objs2[0] != null && (!"".equals(objs2[0]))) {
                                method2[j].invoke(obj2, objs2);// 调用obj2的相应的set的方法，objs2数组存放调用该方法的参数
                            }
                            continue;
                        }

                    }

                }

            }

        }

        return obj2;

    }

	public static JSONObject castBeanToFirstLowerKey(JSONObject tmp) {
		
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		for(Iterator<String> it = tmp.keySet().iterator();it.hasNext();){
			String key  = it.next();
			if(key!=null&&key.trim().length()>0){
				
				String low = key.substring(0,1).toLowerCase();
				String sub = key.substring(1,key.length());
				String keyNow = low+sub;
				obj.put(keyNow, tmp.get(key));
			}
		}
		return obj;
	}

	public static JSONObject castBeanToFirstHigherKey(JSONObject tmp) {
		// TODO Auto-generated method stub
		JSONObject obj = new JSONObject();
		for(Iterator<String> it = tmp.keySet().iterator();it.hasNext();){
			String key  = it.next();
			String low = key.substring(0,1).toUpperCase();
			String sub = key.substring(1,key.length());
			String keyNow = low+sub;
			obj.put(keyNow, tmp.get(key));
		}
		return obj;
	}
}
