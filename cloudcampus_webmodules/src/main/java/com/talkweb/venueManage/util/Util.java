package com.talkweb.venueManage.util;



import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;



public class Util {
   /**
    * 
    * @param startTime  开始时间
    * @param endTime    结束时间
    * @param format
    * @param str
    * @return
    */
	public static Long dateDiff(String startTime, String endTime,  
            String format, String str) {  
        // 按照传入的格式生成一个simpledateformate对象  
        SimpleDateFormat sd = new SimpleDateFormat(format);  
        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数  
        long nh = 1000 * 60 * 60;// 一小时的毫秒数  
        long nm = 1000 * 60;// 一分钟的毫秒数  
        long ns = 1000;// 一秒钟的毫秒数  
        long diff;  
        long day = 0;  
        long hour = 0;  
        long min = 0;  
        long sec = 0;  
        // 获得两个时间的毫秒时间差异  
        try {  
            diff = sd.parse(endTime).getTime() - sd.parse(startTime).getTime();  
            day = diff / nd;// 计算差多少天  
            hour = diff % nd / nh + day * 24;// 计算差多少小时  
            min = diff % nd % nh / nm + day * 24 * 60;// 计算差多少分钟  
            sec = diff % nd % nh % nm / ns;// 计算差多少秒  
            // 输出结果  
            System.out.println("时间相差：" + day + "天" + (hour - day * 24) + "小时"  
                    + (min - day * 24 * 60) + "分钟" + sec + "秒。");  
            System.out.println("hour=" + hour + ",min=" + min);  
            System.out.println("HOUR="+(hour+min/5.0));
            if (str.equalsIgnoreCase("h")) {  
                return hour;  
            } else {  
                return min;  
            }  
  
        } catch (ParseException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        if (str.equalsIgnoreCase("h")) {  
            return hour;  
        } else {  
            return min;  
        }  
    }  
	public static  Date stringToDate(String str) {  
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date date = null;  
        try {  
            // Fri Feb 24 00:00:00 CST 2012  
            date = format.parse(str);   
        } catch (ParseException e) {  
            e.printStackTrace();  
        }  
        // 2012-02-24  
        date = java.sql.Date.valueOf(str);  
                                              
        return date;  
} 
	/**
  	 * 将两个时间以  2015-12-18 08:00 -- 12:00
  	 *  或者以   2015-12-18 08:00 -- 2015-12-19 12:00返回
  	 * @param d1
  	 * @param d2
  	 * @return
  	 */
	public static  String getFormatDate(String d1,String d2){
		String d="";
		if(StringUtils.isNotBlank(d1)&& StringUtils.isNotBlank(d2)){
			String d1Return=d1.substring(0, 16);
			String d2Return=d2.substring(0,16);
		    String d1Equal=d1.substring(0, 10);
		    String d2Equal=d2.substring(0, 10);
		    if(!d1Equal.equals(d2Equal)){
		    	d=d1Return+" -- "+d2Return;
		    }else{
		    	d=d1Return+" -- "+d2Return.substring(11);
		    }
		
		
		}
  		return d;
  	}
	public static void main(String[] args) {
		String s="2015-11-26 10:27:08";
		String e="2015-11-26 12:35:08";
		String f="yyyy-mm-dd hh:mm:ss";
		
		System.out.println(s.substring(0, 16));
		System.out.println(s.substring(0, 10));
		System.out.println(s.substring(11));
		
	}
	
}
