package com.talkweb.common.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: StringNumTool.java	
 * @version:1.0
 * @Description: 字符串、数字处理工具
 * @author 武洋 ---智慧校
 * @date 2015年3月23日
 */
public class StringNumTool {

    /**
     * 将字符串的最后一串数字加1
     * @return
     */
    public static String getLastNumAdd1(String str) {
        Pattern p = Pattern.compile("(\\d+)");
//        Pattern p = Pattern.compile("([0-9]{1,})");
        Matcher m = p.matcher(str);
        ArrayList<String> strs = new ArrayList<String>();
        while (m.find()) {
            strs.add(m.group(1));            
        } 
        if(strs.size()>0){
            
            int l = strs.size()-1;
            String oldStr = strs.get(l);
            int num = Integer.parseInt(oldStr)+1;
            
            int index = str.lastIndexOf(oldStr);
            
            String pre = str.substring(0,index);
            String beh = str.substring(index+oldStr.length(),str.length());
            str = pre+num+beh;
        }else{
            str = str+"1";
        }
        return str;
    }
    
    /**
     * 两个list去掉重复元素合并
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List removeListRepeat(List a,List b)
    {
    	int size=a.size()-b.size();
    	if(size>0)
    	{
    		a.removeAll(b);
    		a.addAll(b);
    		return a;
    	}
    	else
    	{
    		b.removeAll(a);
    		b.addAll(a);
    		return b;
    	}
    }
    /**
     * 得到上课时间-星期几
     * @param dayOfWeek 星期几
     * @return
     */
    public static String getDayOfWeek(int dayOfWeek)
    {
    	String s="星期";
    	switch (dayOfWeek) {
		case 0:
			s+="一";
			break;
		case 1:
			s+="二";
			break;
		case 2:
			s+="三";
			break;
		case 3:
			s+="四";
			break;
		case 4:
			s+="五";
			break;
		case 5:
			s+="六";
			break;
		case 6:
			s+="天";
			break;				
		default:
			break;
		}
    	
    	return s;
    }
    
    /**
     * 得到周次类型
     * @param type 周次类型（0：不限， 1：单周， 2：双周）
     * @return
     */
    public static String getWeekType(int type)
    {
    	String s="";
    	switch (type) {
		case 0:
			s="不限";
			break;
		case 1:
			s="单";
			break;
		case 2:
			s="双";
			break;	
		default:
			break;
		}    	
    	return s;
    }
    
    /**
     * 根据单双周得到对应的值
     * @param type
     * @return
     */
    public static int getWeekTypeValue(String type)
    {
    	int s=0;
    	switch (type) {
		case "不限":
			s=0;
			break;
		case "单":
			s=1;
			break;
		case "双":
			s=2;
			break;	
		default:
			break;
		}    	
    	return s;
    }
    
    
/**
 * 
 * 得到上课时间（单双周）
 * @param shoolTime 上课时间
 * @param type 周次
 * @return
 */
    public static String getSchoolTimeText(String shoolTime,int type)
    {
    	if(type==0)
    	{
    		return shoolTime;
    	}
    	else 
    	{

    		String typeName=getWeekType(type);
    		return shoolTime+"("+typeName+")";
    	}
    }
    
    /**
     * 
     * 得到上课时间（单双周）
     * @param shoolTime 上课时间
     * @param type 周次
     * @return
     */
        public static String getSchoolTimeText2(String shoolTime,int type)
        {
        	if(type==0)
        	{
        		return shoolTime;
        	}
        	else 
        	{

        		String typeName=getWeekType(type);
        		return shoolTime+"    ("+typeName+"周)";
//        		return typeName+"周("+shoolTime+")";
        	}
        }
    
    /**
     * 根据开设年级数组得到对应的字符串   如：  高一 ；G003班，G002班
     * @param array
     * @return
     */
    public static String getOfferGradeText(JSONArray array)
    {
		String offerGrade = "";
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			int isAll=obj.getIntValue("isAll");			
			if (isAll==1) {
				offerGrade += obj.getString("useGradeText") + ";";
			} else {
				JSONArray ja = obj.getJSONArray("classList");
				for (int k = 0; k < ja.size(); k++) {
					offerGrade += ja.getJSONObject(k).getString("classText") + ",";
				}
				offerGrade=offerGrade.substring(0,offerGrade.length()-1)+";";
			}
		}
		if (StringUtils.isNotEmpty(offerGrade)) {
			offerGrade = offerGrade.substring(0, offerGrade.length() - 1);
		}
		return offerGrade;
    }
    /**
     * 根据星期得到对应的值
     * @param str
     * @return
     */
    public static int getDayOfWeekValue(String str)
    {
    	int s=0;
    	switch (str) {
		case "星期一":
			s=0;
			break;
		case "星期二":
			s=1;
			break;
		case "星期三":
			s=2;
			break;
		case "星期四":
			s=3;
			break;
		case "星期五":
			s=4;
			break;
		case "星期六":
			s=5;
			break;
		case "星期天":
			s=6;
			break;				
		default:
			break;
		}    	
    	return s;
    }
    /**
     * 得到性别名称
     * @param sex
     * @return
     */
    public static String getSex(int sex)
    {
    	String s="";
    	switch (sex) {
		case 0:
			s="不限";
			break;
		case 1:
			s="男";
			break;
		case 2:
			s="女";
			break;
		default:
			break;
		}
    	return s;
    }
    
    /**
     * 根据性别得到对应的值
     * @param sex
     * @return
     */
    public static int getSexValue(String sex)
    {
    	int s=0;
    	switch (sex) {
		case "不限":
			s=0;
			break;
		case "男":
			s=1;
			break;
		case "女":
			s=2;
			break;
		default:
			break;
		}
    	return s;
    }
    
    /** 
     * 判断是否为整数  
     * @param str 传入的字符串  
     * @return 是整数返回true,否则返回false  
    */
    public static boolean isInteger(String str) {    
       Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
       return pattern.matcher(str).matches();    
     } 
    
    /** 
     * 判断是否为上课时间  
     * @param str 传入的字符串  
     * @return 是整数返回true,否则返回false  
    */
    public static boolean isSchoolTime(String str) {
//        Pattern pattern = Pattern.compile("^星期[一二三四五六天][1-9](\\(单\\)|\\(双\\))?$|^星期[一二三四五六天][1-9]\\,{1}[1-9](\\(单\\)|\\(双\\))?$"); 
//        Pattern pattern = Pattern.compile("^星期[一二三四五六天][1-8]$|^星期[一二三四五六天][1-8]{1}(\\,[1-8]{1,7})$"); 
        Pattern pattern = Pattern.compile("^星期[一二三四五六天][1-8]$|^星期[一二三四五六天][1-8]{1}((\\,[1-8]){1,7})$"); 

        for(String s:str.split(";"))
        {        	
     	   if(!pattern.matcher(s).matches())
     	   {
     		   return false;
     	   }
        }
        return true;
     } 
    
    /** 
     * 判断是否为适用性别 
     * @param str 传入的字符串  
     * @return 是整数返回true,否则返回false  
    */
    public static boolean isAdaptSex(String str) {
        Pattern pattern = Pattern.compile("^不限|男|女$"); 
        return pattern.matcher(str).matches();   
     }
    
    /** 
     * 判断是否为周次
     * @param str 传入的字符串  
     * @return 是整数返回true,否则返回false  
    */
    public static boolean isWeekType(String str) {
        Pattern pattern = Pattern.compile("^不限|单|双$"); 
        return pattern.matcher(str).matches();   
     }
    
 

    public static void main(String[] args) {
//        System.out.println("Ls:"+getLastNumAdd1("fsdfs"));
//    	System.out.println(isSchoolTime("星期一1,2;星期三7,9"));
//    	System.out.println(isAdaptSex("不限"));
    	String s="星期一1,2,3,4,5,6,8,8;星期一1,2,3,4,5,6,8,8";
//    	String s1="星期三2";
//    	String s2="星期三1(双)";
//    	
//    	String s3="星期三1,2(单)";
//    	String s4="星期三2,4";
//    	String s5="星期三1,2(单);星期四1,2(双)";
    	
    	
    	System.out.println(isSchoolTime(s));
//    	System.out.println(isSchoolTime(s1));
//    	System.out.println(isSchoolTime(s2));
//    	
//    	System.out.println(isSchoolTime(s3));
//    	System.out.println(isSchoolTime(s4));
//    	System.out.println(isSchoolTime(s5));
    	
//    	System.out.println(getWeekTypeValue(s.substring(s.length()-2, s.length()-1)));
//    	System.out.println(s2.substring(s2.length()-2, s2.length()-1));
//    	System.out.println(getWeekTypeValue(s2.substring(s2.length()-2, s2.length()-1)));
    }
}
