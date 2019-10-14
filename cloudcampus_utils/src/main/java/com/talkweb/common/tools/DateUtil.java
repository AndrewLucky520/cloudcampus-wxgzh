package com.talkweb.common.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @ClassName: DateUtil.java
 * @version:1.0
 * @Description: 时间日期日历处理工具
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
public class DateUtil implements java.io.Serializable {
    private static final long serialVersionUID = 7867268199240522574L;
    private static final int[] dayArray = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private static SimpleDateFormat sdf = new SimpleDateFormat();

    public static synchronized Calendar getCalendar() {
        return GregorianCalendar.getInstance();
    }

    /**
     * 获取格式为yyyy-MM-dd HH:mm:ss,SSS格式的当前日期字符串
     * @return String
     */
    public static synchronized String getDateMilliFormat() {
        Calendar cal = Calendar.getInstance();
        return getDateMilliFormat(cal);
    }

    /**
     * 将日历转换为yyyy-MM-dd HH:mm:ss,SSS格式的字符串
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateMilliFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        return getDateFormat(cal, pattern);
    }

    /**
     * 将java.util.date转换为yyyy-MM-dd HH:mm:ss,SSS格式的字符串
     * 
     * @param date
     * @return String
     */
    public static synchronized String getDateMilliFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        return getDateFormat(date, pattern);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss,SSS格式的字符串转换为日历实体
     * 
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarMilliFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        return parseCalendarFormat(strDate, pattern);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss,SSS格式的字符串转换为java.util.date实体
     * 
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateMilliFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss,SSS";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * 获取当前时间yyyy-MM-dd HH:mm:ss格式的字符串转
     * 
     * @return String
     */
    public static synchronized String getDateSecondFormat() {
        Calendar cal = Calendar.getInstance();
        return getDateSecondFormat(cal);
    }

    /**
     * 将日历实体转换为yyyy-MM-dd HH:mm:ss格式的字符串转
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateSecondFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(cal, pattern);
    }

    /**
     * 将java.util.date实体转换为yyyy-MM-dd HH:mm:ss格式的字符串转
     * 
     * @param date
     * @return String
     */
    public static synchronized String getDateSecondFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(date, pattern);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转转换为日历实体
     * 
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarSecondFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseCalendarFormat(strDate, pattern);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转转换为date实体
     * 
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateSecondFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * @return String
     */
    public static synchronized String getDateMinuteFormat() {
        Calendar cal = Calendar.getInstance();
        return getDateMinuteFormat(cal);
    }

    /**
     * @param cal
     * @return String
     */
    public static synchronized String getDateMinuteFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd HH:mm";
        return getDateFormat(cal, pattern);
    }

    /**
     * @param date
     * @return String
     */
    public static synchronized String getDateMinuteFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd HH:mm";
        return getDateFormat(date, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarMinuteFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm";
        return parseCalendarFormat(strDate, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateMinuteFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * 获取今天
     * 
     * @return String
     */
    public static synchronized String getDateDayFormat() {
        Calendar cal = Calendar.getInstance();
        return getDateDayFormat(cal);
    }

    /**
     * 根据日历(Calendar)获取目标天 格式为yyyy-MM-dd
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateDayFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd";
        return getDateFormat(cal, pattern);
    }

    /**
     * 根据java.util.date获取目标天 格式为yyyy-MM-dd
     * 
     * @param date
     * @return String
     */
    public static synchronized String getDateDayFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd";
        return getDateFormat(date, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarDayFormat(String strDate) {
        String pattern = "yyyy-MM-dd";
        return parseCalendarFormat(strDate, pattern);
    }

    /**将yyyy-MM-dd格式的字符串转为日期
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateDayFormat(String strDate) {
        String pattern = "yyyy-MM-dd";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateFileFormat(String strDate) {
        String pattern = "yyyy-MM-dd_HH-mm-ss";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * @return String
     */
    public static synchronized String getDateW3CFormat() {
        Calendar cal = Calendar.getInstance();
        return getDateW3CFormat(cal);
    }

    /**
     * @param cal
     * @return String
     */
    public static synchronized String getDateW3CFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(cal, pattern);
    }

    /**
     * @param date
     * @return String
     */
    public static synchronized String getDateW3CFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(date, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarW3CFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseCalendarFormat(strDate, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateW3CFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * 获取格式为yyyy-MM-dd HH:mm:ss的当前时间
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateFormatNow() {
        Calendar cal = Calendar.getInstance();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(cal, pattern);
    }
    
    /**
     * 获取格式为yyyy-MM-dd的当前时间
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateFormatNow1() {
        Calendar cal = Calendar.getInstance();
        String pattern = "yyyy-MM-dd";
        return getDateFormat(cal, pattern);
    }
    /**
     * 根据Calendar获取格式为yyyy-MM-dd HH:mm:ss的日期
     * 
     * @param cal
     * @return String
     */
    public static synchronized String getDateFormat(java.util.Calendar cal) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(cal, pattern);
    }

    /**
     * 根据Date获取格式为yyyy-MM-dd HH:mm:ss的日期
     * 
     * @param date
     * @return String
     */
    public static synchronized String getDateFormat(java.util.Date date) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return getDateFormat(date, pattern);
    }

    /**
     * @param strDate
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseCalendarFormat(strDate, pattern);
    }

    /**yyyy-MM-dd HH:mm:ss
     * @param strDate
     * @return java.util.Date
     */
    public static synchronized Date parseDateFormat(String strDate) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return parseDateFormat(strDate, pattern);
    }

    /**
     * 根据日历实例和目标时间格式获取字符串时间
     * 
     * @param cal 日历实例
     * @param pattern 目标格式 例如'yyyy-MM-dd HH:mm:ss,SSS"
     * @return String
     */
    public static synchronized String getDateFormat(java.util.Calendar cal, String pattern) {
        return getDateFormat(cal.getTime(), pattern);
    }

    /**
     * 根据date实例和目标时间格式获取字符串时间
     * 
     * @param date
     * @param pattern 目标格式 例如'yyyy-MM-dd HH:mm:ss,SSS"
     * @return String
     */
    public static synchronized String getDateFormat(java.util.Date date, String pattern) {
        synchronized (sdf) {
            String str = null;
            sdf.applyPattern(pattern);
            str = sdf.format(date);
            return str;
        }
    }

    /**
     * @param strDate
     * @param pattern
     * @return java.util.Calendar
     */
    public static synchronized Calendar parseCalendarFormat(String strDate, String pattern) {
        synchronized (sdf) {
            Calendar cal = null;
            sdf.applyPattern(pattern);
            try {
                sdf.parse(strDate);
                cal = sdf.getCalendar();
            } catch (Exception e) {
            }
            return cal;
        }
    }

    /**
     * 根据时间类型的字符串及转换格式 转换为日期实例 
     * @param strDate
     * @param pattern
     * @return java.util.Date
     */
    public static synchronized Date parseDateFormat(String strDate, String pattern) {
        synchronized (sdf) {
            Date date = null;
            sdf.applyPattern(pattern);
            try {
                date = sdf.parse(strDate);
            } catch (Exception e) {
            }
            return date;
        }
    }

    public static synchronized int getLastDayOfMonth(int month) {
        if (month < 1 || month > 12) {
            return -1;
        }
        int retn = 0;
        if (month == 2) {
            if (isLeapYear()) {
                retn = 29;
            } else {
                retn = dayArray[month - 1];
            }
        } else {
            retn = dayArray[month - 1];
        }
        return retn;
    }

    public static synchronized int getLastDayOfMonth(int year, int month) {
        if (month < 1 || month > 12) {
            return -1;
        }
        int retn = 0;
        if (month == 2) {
            if (isLeapYear(year)) {
                retn = 29;
            } else {
                retn = dayArray[month - 1];
            }
        } else {
            retn = dayArray[month - 1];
        }
        return retn;
    }

    public static synchronized boolean isLeapYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        return isLeapYear(year);
    }

    public static synchronized boolean isLeapYear(int year) {
        /**
         * ��ϸ��ƣ� 1.��400��������꣬���� 2.���ܱ�4����������� 3.�ܱ�4���ͬʱ���ܱ�100�����������
         * 3.�ܱ�4���ͬʱ�ܱ�100�����������
         */
        if ((year % 400) == 0)
            return true;
        else if ((year % 4) == 0) {
            if ((year % 100) == 0)
                return false;
            else
                return true;
        } else
            return false;
    }

    /**
     */
    public static synchronized boolean isLeapYear(java.util.Date date) {
        /**
         */
        // int year = date.getYear();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        int year = gc.get(Calendar.YEAR);
        return isLeapYear(year);
    }

    public static synchronized boolean isLeapYear(java.util.Calendar gc) {
        /**
         * 判断是否闰年
         */
        int year = gc.get(Calendar.YEAR);
        return isLeapYear(year);
    }

    /**
     * 获取上周
     */
    public static synchronized java.util.Date getPreviousWeekDay(java.util.Date date) {
        {

            GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
            gc.setTime(date);
            return getPreviousWeekDay(gc);
            // switch ( gc.get( Calendar.DAY_OF_WEEK ) )
            // {
            // case ( Calendar.MONDAY ):
            // gc.add( Calendar.DATE, -3 );
            // break;
            // case ( Calendar.SUNDAY ):
            // gc.add( Calendar.DATE, -2 );
            // break;
            // default:
            // gc.add( Calendar.DATE, -1 );
            // break;
            // }
            // return gc.getTime();
        }
    }

    public static synchronized java.util.Date getPreviousWeekDay(java.util.Calendar gc) {
        {

            switch (gc.get(Calendar.DAY_OF_WEEK)) {
                case (Calendar.MONDAY):
                    gc.add(Calendar.DATE, -3);
                    break;
                case (Calendar.SUNDAY):
                    gc.add(Calendar.DATE, -2);
                    break;
                default:
                    gc.add(Calendar.DATE, -1);
                    break;
            }
            return gc.getTime();
        }
    }

    /**
     * 获取下周
     */
    public static synchronized java.util.Date getNextWeekDay(java.util.Date date) {

        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        switch (gc.get(Calendar.DAY_OF_WEEK)) {
            case (Calendar.FRIDAY):
                gc.add(Calendar.DATE, 3);
                break;
            case (Calendar.SATURDAY):
                gc.add(Calendar.DATE, 2);
                break;
            default:
                gc.add(Calendar.DATE, 1);
                break;
        }
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getNextWeekDay(java.util.Calendar gc) {

        switch (gc.get(Calendar.DAY_OF_WEEK)) {
            case (Calendar.FRIDAY):
                gc.add(Calendar.DATE, 3);
                break;
            case (Calendar.SATURDAY):
                gc.add(Calendar.DATE, 2);
                break;
            default:
                gc.add(Calendar.DATE, 1);
                break;
        }
        return gc;
    }

    public static synchronized java.util.Date getLastDayOfNextMonth(java.util.Date date) {

        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.setTime(DateUtil.getNextMonth(gc.getTime()));
        gc.setTime(DateUtil.getLastDayOfMonth(gc.getTime()));
        return gc.getTime();
    }

    public static synchronized java.util.Date getLastDayOfNextWeek(java.util.Date date) {

        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.setTime(DateUtil.getNextWeek(gc.getTime()));
        gc.setTime(DateUtil.getLastDayOfWeek(gc.getTime()));
        return gc.getTime();
    }

    public static synchronized java.util.Date getFirstDayOfNextMonth(java.util.Date date) {

        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.setTime(DateUtil.getNextMonth(gc.getTime()));
        gc.setTime(DateUtil.getFirstDayOfMonth(gc.getTime()));
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getFirstDayOfNextMonth(java.util.Calendar gc) {

        gc.setTime(DateUtil.getNextMonth(gc.getTime()));
        gc.setTime(DateUtil.getFirstDayOfMonth(gc.getTime()));
        return gc;
    }

    public static synchronized java.util.Date getFirstDayOfNextWeek(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.setTime(DateUtil.getNextWeek(gc.getTime()));
        gc.setTime(DateUtil.getFirstDayOfWeek(gc.getTime()));
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getFirstDayOfNextWeek(java.util.Calendar gc) {
        /**
		 */
        gc.setTime(DateUtil.getNextWeek(gc.getTime()));
        gc.setTime(DateUtil.getFirstDayOfWeek(gc.getTime()));
        return gc;
    }

    /**
	 */
    public static synchronized java.util.Date getNextMonth(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.MONTH, 1);
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getNextMonth(java.util.Calendar gc) {
        /**
		 */
        gc.add(Calendar.MONTH, 1);
        return gc;
    }

    /**
	 */
    public static synchronized java.util.Date getNextDay(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.DATE, 1);
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getNextDay(java.util.Calendar gc) {
        /**
		 */
        gc.add(Calendar.DATE, 1);
        return gc;
    }

    /**
	 */
    public static synchronized java.util.Date getNextWeek(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.add(Calendar.DATE, 7);
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getNextWeek(java.util.Calendar gc) {
        /**
		 */
        gc.add(Calendar.DATE, 7);
        return gc;
    }

    /**
	 */
    public static synchronized java.util.Date getLastDayOfWeek(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        switch (gc.get(Calendar.DAY_OF_WEEK)) {
            case (Calendar.SUNDAY):
                gc.add(Calendar.DATE, 6);
                break;
            case (Calendar.MONDAY):
                gc.add(Calendar.DATE, 5);
                break;
            case (Calendar.TUESDAY):
                gc.add(Calendar.DATE, 4);
                break;
            case (Calendar.WEDNESDAY):
                gc.add(Calendar.DATE, 3);
                break;
            case (Calendar.THURSDAY):
                gc.add(Calendar.DATE, 2);
                break;
            case (Calendar.FRIDAY):
                gc.add(Calendar.DATE, 1);
                break;
            case (Calendar.SATURDAY):
                gc.add(Calendar.DATE, 0);
                break;
        }
        return gc.getTime();
    }
       public static synchronized java.util.Date getLastDayOfLearnWeek(java.util.Date date) {
           GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
           gc.setTime(date);
           switch (gc.get(Calendar.DAY_OF_WEEK)) {
               case (Calendar.SUNDAY):
                   gc.add(Calendar.DATE, 6);
                   break;
               case (Calendar.MONDAY):
                   gc.add(Calendar.DATE, 5);
                   break;
               case (Calendar.TUESDAY):
                   gc.add(Calendar.DATE, 4);
                   break;
               case (Calendar.WEDNESDAY):
                   gc.add(Calendar.DATE, 3);
                   break;
               case (Calendar.THURSDAY):
                   gc.add(Calendar.DATE, 2);
                   break;
               case (Calendar.FRIDAY):
                   gc.add(Calendar.DATE, 1);
                   break;
               case (Calendar.SATURDAY):
                   gc.add(Calendar.DATE, 0);
                   break;
           }
           return gc.getTime();
       }

    /**
	 */
    public static synchronized java.util.Date getFirstDayOfWeek(java.util.Date date) {
        /**
		 */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        switch (gc.get(Calendar.DAY_OF_WEEK)) {
            case (Calendar.SUNDAY):
                gc.add(Calendar.DATE, 0);
                break;
            case (Calendar.MONDAY):
                gc.add(Calendar.DATE, -1);
                break;
            case (Calendar.TUESDAY):
                gc.add(Calendar.DATE, -2);
                break;
            case (Calendar.WEDNESDAY):
                gc.add(Calendar.DATE, -3);
                break;
            case (Calendar.THURSDAY):
                gc.add(Calendar.DATE, -4);
                break;
            case (Calendar.FRIDAY):
                gc.add(Calendar.DATE, -5);
                break;
            case (Calendar.SATURDAY):
                gc.add(Calendar.DATE, -6);
                break;
        }
        return gc.getTime();
    }
    /**
   	 */
       public static synchronized java.util.Date getFirstDayOfLearnWeek(java.util.Date date) {
           /**
   		 */
           GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
           gc.setTime(date);
           switch (gc.get(Calendar.DAY_OF_WEEK)) {
              
               case (Calendar.MONDAY):
                   gc.add(Calendar.DATE, 0);
                   break;
               case (Calendar.TUESDAY):
                   gc.add(Calendar.DATE, -1);
                   break;
               case (Calendar.WEDNESDAY):
                   gc.add(Calendar.DATE, -2);
                   break;
               case (Calendar.THURSDAY):
                   gc.add(Calendar.DATE, -3);
                   break;
               case (Calendar.FRIDAY):
                   gc.add(Calendar.DATE, -4);
                   break;
               case (Calendar.SATURDAY):
                   gc.add(Calendar.DATE, -5);
                   break;
               case (Calendar.SUNDAY):
                   gc.add(Calendar.DATE, -6);
                   break;
           }
           return gc.getTime();
       }
    public static synchronized java.util.Calendar getFirstDayOfWeek(java.util.Calendar gc) {
        switch (gc.get(Calendar.DAY_OF_WEEK)) {
            case (Calendar.SUNDAY):
                gc.add(Calendar.DATE, 0);
                break;
            case (Calendar.MONDAY):
                gc.add(Calendar.DATE, -1);
                break;
            case (Calendar.TUESDAY):
                gc.add(Calendar.DATE, -2);
                break;
            case (Calendar.WEDNESDAY):
                gc.add(Calendar.DATE, -3);
                break;
            case (Calendar.THURSDAY):
                gc.add(Calendar.DATE, -4);
                break;
            case (Calendar.FRIDAY):
                gc.add(Calendar.DATE, -5);
                break;
            case (Calendar.SATURDAY):
                gc.add(Calendar.DATE, -6);
                break;
        }
        return gc;
    }

    public static synchronized java.util.Date getLastDayOfMonth(java.util.Date date) {
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        switch (gc.get(Calendar.MONTH)) {
            case 0:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 1:
                gc.set(Calendar.DAY_OF_MONTH, 28);
                break;
            case 2:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 3:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 4:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 5:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 6:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 7:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 8:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 9:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 10:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 11:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
        }
        // �������
        if ((gc.get(Calendar.MONTH) == Calendar.FEBRUARY) && (isLeapYear(gc.get(Calendar.YEAR)))) {
            gc.set(Calendar.DAY_OF_MONTH, 29);
        }
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getLastDayOfMonth(java.util.Calendar gc) {
        switch (gc.get(Calendar.MONTH)) {
            case 0:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 1:
                gc.set(Calendar.DAY_OF_MONTH, 28);
                break;
            case 2:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 3:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 4:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 5:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 6:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 7:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 8:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 9:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
            case 10:
                gc.set(Calendar.DAY_OF_MONTH, 30);
                break;
            case 11:
                gc.set(Calendar.DAY_OF_MONTH, 31);
                break;
        }
        // �������
        if ((gc.get(Calendar.MONTH) == Calendar.FEBRUARY) && (isLeapYear(gc.get(Calendar.YEAR)))) {
            gc.set(Calendar.DAY_OF_MONTH, 29);
        }
        return gc;
    }

    public static synchronized java.util.Date getFirstDayOfMonth(java.util.Date date) {
        /**
         * ��ϸ��ƣ� 1.����Ϊ1��
         */
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        gc.set(Calendar.DAY_OF_MONTH, 1);
        return gc.getTime();
    }

    public static synchronized java.util.Calendar getFirstDayOfMonth(java.util.Calendar gc) {
        /**
		 */
        gc.set(Calendar.DAY_OF_MONTH, 1);
        return gc;
    }

    /**
     * �����ڶ���ת����Ϊָ��ORA���ڡ�ʱ���ʽ���ַ���ʽ��������ڶ���Ϊ�գ����� һ�����ַ���󣬶���һ���ն���
     * 
     * @param theDate
     * ��Ҫת��Ϊ�ַ�����ڶ���
     * @param hasTime
     * ���ص��ַ��ʱ����Ϊtrue
     * @return ת���Ľ��
     */
    public static synchronized String toOraString(Date theDate, boolean hasTime) {
        /**
         * ��ϸ��ƣ�
         * 1.�����ʱ�䣬�����ø�ʽΪgetOraDateTimeFormat()�ķ���ֵ
         * 2.�������ø�ʽΪgetOraDateFormat()�ķ���ֵ
         * 3.����toString(Date theDate, DateFormat
         * theDateFormat)
         */
        DateFormat theFormat;
        if (hasTime) {
            theFormat = getOraDateTimeFormat();
        } else {
            theFormat = getOraDateFormat();
        }
        return toString(theDate, theFormat);
    }

    /**
     * �����ڶ���ת����Ϊָ�����ڡ�ʱ���ʽ���ַ���ʽ��������ڶ���Ϊ�գ����� һ�����ַ���󣬶���һ���ն���
     * 
     * @param theDate
     * ��Ҫת��Ϊ�ַ�����ڶ���
     * @param hasTime
     * ���ص��ַ��ʱ����Ϊtrue
     * @return ת���Ľ��
     */
    public static synchronized String toString(Date theDate, boolean hasTime) {
        /**
         * ��ϸ��ƣ�
         * 1.�����ʱ�䣬�����ø�ʽΪgetDateTimeFormat�ķ���ֵ
         * 2.�������ø�ʽΪgetDateFormat�ķ���ֵ
         * 3.����toString(Date theDate, DateFormat theDateFormat)
         */
        DateFormat theFormat;
        if (hasTime) {
            theFormat = getDateTimeFormat();
        } else {
            theFormat = getDateFormat();
        }
        return toString(theDate, theFormat);
    }

    /**
     * MM/dd/yyyy HH:mm
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    /**
     * MM/dd/yyyy HH:mm
     */
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    /**
     * yyyyMMdd
     */
    private static final SimpleDateFormat ORA_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    /**
     * oracle日期格式
     */
    private static final SimpleDateFormat ORA_DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");

    /**
     * 获取MM/dd/yyyy HH:mm格式的时间转换器
     */
    public static synchronized DateFormat getDateFormat() {
        /**
		 */
        SimpleDateFormat theDateFormat = (SimpleDateFormat) DATE_FORMAT.clone();
        theDateFormat.setLenient(false);
        return theDateFormat;
    }

    /**
     * 获取转换器
     */
    public static synchronized DateFormat getDateTimeFormat() {
        /**
		 */
        SimpleDateFormat theDateTimeFormat = (SimpleDateFormat) DATE_TIME_FORMAT.clone();
        theDateTimeFormat.setLenient(false);
        return theDateTimeFormat;
    }

    /**
     * 获取oracle时间转换器
     */
    public static synchronized DateFormat getOraDateFormat() {
        /**
		 */
        SimpleDateFormat theDateFormat = (SimpleDateFormat) ORA_DATE_FORMAT.clone();
        theDateFormat.setLenient(false);
        return theDateFormat;
    }

    /**
     * 获取ora转换器
     */
    public static synchronized DateFormat getOraDateTimeFormat() {
        /**
		 */
        SimpleDateFormat theDateTimeFormat = (SimpleDateFormat) ORA_DATE_TIME_FORMAT.clone();
        theDateTimeFormat.setLenient(false);
        return theDateTimeFormat;
    }

    /**
     * 根据自定义的时间格式转换器 转换时间
     * 
     * @param theDate
     * 源日期
     * @param theDateFormat
     * 源格式
     * @return 目标格式
     */
    public static synchronized String toString(Date theDate, DateFormat theDateFormat) {
        /**
		 */
        if (theDate == null)
            return "";
        return theDateFormat.format(theDate);
    }
    
    /**
     * 将日期增加秒数
     * @param date
     * @param i 秒数
     * @return
     */
	public static synchronized String addDateSecond(Date date,int i) {    
	    Calendar calendar = Calendar.getInstance();    
	    calendar.setTime(date);    
	    calendar.add(Calendar.SECOND, i);    
	    return getDateSecondFormat(calendar.getTime());    
	}
	
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
		Date date = new Date();
		date.setTime(1554768000000l/1000);
		System.out.println(DateUtil.getDateW3CFormat(date));
	}
}
