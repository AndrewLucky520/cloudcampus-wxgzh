package com.talkweb.csbasedata.util;

import java.util.Calendar;
import java.util.Date;
/**
 * 时间处理工具
 * @author zhanghuihui
 *
 */
public class DateUtil {
	//增加1秒，用来排序
	public static long getTimeAndAddOneSecond(int interval){
		 Calendar calendar = Calendar.getInstance ();
	     calendar.add (Calendar.SECOND, interval);
	     Date d = calendar.getTime();
	     System.out.println("[time created:]"+d.getTime()/1000+" interval:"+interval);
	     return d.getTime()/1000;
	}
}
