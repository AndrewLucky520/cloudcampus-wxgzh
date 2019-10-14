package com.talkweb.common.tools;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.LessonInfo._Fields;

public class StringUtil {
	private static final boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
        return false;  
    }  
  
	public static int getNumberIndex(String str){
		 for (int i = 0; i < str.length(); i++){
			   if (Character.isDigit(str.charAt(i))){
				   return i;
			   }
		 }
		 return 0;
	}
	/***
	 * 判断是否含有中文字符
	 * @param strName
	 * @return
	 */
    public static final boolean isChinese(String str) {  
        char[] ch = str.toCharArray();  
        for (int i = 0; i < ch.length; i++) {  
            char c = ch[i];  
            if (isChinese(c)) {  
                return true;  
            }  
        }  
        return false;  
    }  
    
    /***
     * 把List转成以逗号分隔开的字符
     * @param list
     * @return
     */
    public static final String toStringBySeparator(List<String> list) {
    	
    	if(list==null||list.size()<=0) return "";
    	
    	String argString="";
    	
		for(String o:list)
		{
			if(o==null) continue;
			argString+=","+o;
		}
		
		if(argString!=null) argString=argString.substring(1);
		
		return argString;
	}
    
    
	/***
	 * 把List转成以逗号分隔开的字符
	 * 
	 * @param list
	 * @return
	 */
	public static final String toStringBySeparator(List<Long> list, Long type) {
		if (list == null || list.size() == 0) {
			return "";
		}

		StringBuffer strbuf = new StringBuffer();

		for (Long o : list) {
			strbuf.append(o).append(",");
		}
		if (strbuf.length() > 0) {
			strbuf.deleteCharAt(strbuf.length() - 1);
		}
		
		return strbuf.toString();
	}
    
    /***
     * 获取list中的最大值
     * @param sampleList
     * @return
     */
	public static final String ArrayListMax(List<String> sampleList) {
		try {
			String maxDevation = "";
			int totalCount = sampleList.size();
			if (totalCount >= 1) {
				String max =sampleList.get(0).toString();
				for (int i = 0; i < totalCount; i++) {
					String temp =sampleList.get(i).toString();
					if (temp.compareTo(max)>0) {
						max = temp;
					}
				}
				maxDevation = max;
			}
			return maxDevation;
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	/***
	 * 把以逗号分隔开的字符转换成List数据结构存储
	 * @param args 以逗号分隔开的字符窜
	 * @return
	 */
	public static final List<Long> toListFromString(String args)
	{
		//1.处理特殊数据情况
		if(args==null||"".equals(args)) return null;
		
	    //2.把以逗号分隔开的字符串拆成List
		String array[]=args.split(",");
		List<Long> argsList=new ArrayList<Long>();
		
		for (int i = 0; i < array.length; i++) {
			argsList.add(Long.valueOf(array[i]));
		}
		
		//3.返回
		return  argsList;
	}
	/***
	 * 把以逗号分隔开的字符转换成List数据结构存储
	 * @param args 以逗号分隔开的字符窜
	 * @return
	 */
	public static final List<String> toStringListFromString(String args)
	{
		//1.处理特殊数据情况
		if(args==null||"".equals(args)) return null;
		
		//2.把以逗号分隔开的字符串拆成List
		String array[]=args.split(",");
		List<String> argsList=new ArrayList<String>();
		
		for (int i = 0; i < array.length; i++) {
			argsList.add(   array[i] );
		}
		
		//3.返回
		return  argsList;
	}
	
	
	/***
	 * 把字符型的List转成Long类型的list
	 * @param args 
	 * @return
	 */
	public static final List<Long> toLongList(List<String> args)
	{
		//1.处理特殊数据情况
		if(CollectionUtils.isEmpty(args)) return null;
		
	    //2.把元素转成Long
		List<Long> argsList=new ArrayList<Long>();
		
		for (int i = 0; i < args.size(); i++) {
			argsList.add(Long.valueOf(args.get(i)));
		}
		
		//3.返回
		return  argsList;
	}
	
	
	/***
	 * 把小数精确到指定的小数位数，如果是整数或是一位小数不处理。
	 * @param num 小数或是整数
	 * @param digits 精确位数
	 * @return
	 */
	public  static final String formatNumber(Object num,int digits) {
		
		String integerRegex="\\d+";//判断为整数的正则表达式
		String numberRegex="\\d+\\.\\d{1}";//判断只有一位小数的正则表达式
		String twoPointRegex="\\d+\\.\\d{2,}";//两位小数以上的正则表达式
		
		String twoZeroRegex="\\d+\\.0{2}";//两位0小数以上的正则表达式
		
		String oneZeroRegex="\\d+\\.0{1}";//两位0小数以上的正则表达式
		
		if(num==null) return "";
		
		String input=String.valueOf(num);
		
		if(Pattern.matches(integerRegex,input))
		{		
			return  input;
		}
		
		if(Pattern.matches(numberRegex,input))
		{		
			if(Pattern.matches(oneZeroRegex,input))
			{
				DecimalFormat df=new DecimalFormat("0");
				input=df.format(Double.valueOf(input));
			}
			return  input;
		}
		
		//2位小数以上的数据，按照精确度格式化
		if(Pattern.matches(twoPointRegex,input))
		{		
			String format="0.";
		    for (int i = 0; i <digits; i++) {
				format=format+"0";
			}
		    DecimalFormat df=new DecimalFormat(format);
			input=df.format(Double.valueOf(input));
			
			if(Pattern.matches(twoZeroRegex,input))
			{
			    df=new DecimalFormat("0");
				input=df.format(Double.valueOf(input));
			}
			
			if(Pattern.matches(oneZeroRegex,input))
			{
			    df=new DecimalFormat("0");
				input=df.format(Double.valueOf(input));
			}
			
			return  String.valueOf(input);
		}
		
		return input;
	}
	
	/****
	 * 把参数转化成字符型
	 * @param source
	 * @return
	 */
	public static String transformString(Object source)
	{
		if(source==null){
			return "";
		}
		return String.valueOf(source);
	}
	
	
	/****
	 * 把参数转化成字符型
	 * @param source
	 * @return
	 */
	public static String transformString(long source)
	{
		return String.valueOf(source);
	}
	
	
	/****
	 * 把参数转化成长整型
	 * @param source
	 * @return
	 */
	public static long transformLong(Object source)
	{
		if(source==null||"".equals(source))
		{
			return 0;
		}
		
		return Long.parseLong(source.toString());
	}
	
	/****
	 * 把参数转化成长整型
	 * @param source
	 * @return
	 */
	public static long transformLong(String source)
	{
		if(source==null||"".equals(source)||"null".equals(source))
		{
			return 0;
		}
		
		return Long.parseLong(source);
	}
	
	/***
	 * 判断空字符窜
	 * @param source
	 * @return
	 */
	public static boolean isEmpty(String source)
	{
		if(source==null||"".equals(source)){
			return true;
		}
		
		return false;
	}
	
	/***
	 * 根据学年学期获取学年
	 * @param termInfoId
	 * @return
	 */
	public static String getSchoolYear(String termInfoId)
	{
		if(termInfoId==null||"".equals(termInfoId))
		{
			return "";
		}
		
		return termInfoId.substring(0,termInfoId.length()-1);
	}
	
	/***
	 * 根据学年学期获取学期
	 * @param termInfoId
	 * @return
	 */
	public static String getSchoolTerm(String termInfoId)
	{
		if(termInfoId==null||"".equals(termInfoId))
		{
			return "";
		}
		
		return termInfoId.substring(termInfoId.length()-1);
	}
	
	/***
	 * 根据学年学期获取学年
	 * @param termInfoId
	 * @return
	 */
	public static String getSchoolYear(Object termInfoId)
	{
		if(termInfoId==null||"".equals(termInfoId))
		{
			return "";
		}
		
		String termInfo=String.valueOf(termInfoId);
		
		return termInfo.substring(0,termInfo.length()-1);
	}
	
	/***
	 * 根据学年学期获取学期
	 * @param termInfoId
	 * @return
	 */
	public static String getSchoolTerm(Object termInfoId)
	{
		if(termInfoId==null||"".equals(termInfoId))
		{
			return "";
		}
		
		String termInfo=String.valueOf(termInfoId);
		return termInfo.substring(termInfo.length()-1);
	}
	
	public static int transToInt(Object object)
	{
		if(object==null||"".equals(object))
		{
			return 0;
		}
		
		
		return Integer.parseInt(object.toString());
	}
	
	public static float transToFloat(Object object)
	{
		if(object==null||"".equals(object))
		{
			return 0;
		}
		
		
		return Float.parseFloat(object.toString());
	}
	
	/****
	 * 把列表数据转成Map接口，以指定的keyProperty 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,Map<String,Object>> convertToMap(List<Map<String,Object>> data,String keyProperty)
	{
		Map<String,Map<String,Object>> result=new HashMap<String,Map<String,Object>>();
		
		if(keyProperty==null||"".equals(keyProperty)) return result;
		
		if(data!=null&&data.size()>0)
		{
			for(Map<String,Object> item:data)
			{
				String key=transformString(item.get(keyProperty));
				
				if(!result.containsKey(key))
				{
					result.put(key, item);
				}
			}
		}
		
		return result;
	}
	
	
	/****
	 * 把列表数据转成List Map，以指定的keyProperty 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,List<Map<String,Object>>> convertToListMap(List<Map<String,Object>> data,String keyProperty)
	{
		Map<String,List<Map<String,Object>>> result=new HashMap<String,List<Map<String,Object>>>();
		
		if(keyProperty==null||"".equals(keyProperty)) return result;
		
		if(data!=null&&data.size()>0)
		{
			for(Map<String,Object> item:data)
			{
				String key=transformString(item.get(keyProperty));
				
				if(!result.containsKey(key))
				{
					List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
					list.add(item);
					result.put(key, list);
				}else 
				{
					result.get(key).add(item);
				}
			}
		}
		
		return result;
	}
	
	/****
	 * 把列表数据转成List Map，以指定的keyProperty+keyPropertyTwo 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,List<Map<String,Object>>> convertToListMap(List<Map<String,Object>> data,String keyProperty,String keyPropertyTwo)
	{
		Map<String,List<Map<String,Object>>> result=new HashMap<String,List<Map<String,Object>>>();
		
		if(keyProperty==null||"".equals(keyProperty)) return result;
		
		if(data!=null&&data.size()>0)
		{
			for(Map<String,Object> item:data)
			{
				String key=transformString(item.get(keyProperty));
				String keyTwo=transformString(item.get(keyPropertyTwo));
				
				if(!result.containsKey(key+keyTwo))
				{
					List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
					list.add(item);
					result.put(key+keyTwo, list);
				}else 
				{
					result.get(key+keyTwo).add(item);
				}
			}
		}
		
		return result;
	}
	
	/****
	 * 把列表数据转成Map接口，以指定的keyProperty 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,JSONObject> convertJSONObjectToMap(List<JSONObject> data,String keyProperty)
	{
		Map<String,JSONObject> result=new HashMap<String,JSONObject>();
		
		if(keyProperty==null||"".equals(keyProperty)) return result;
		
		if(data!=null&&data.size()>0)
		{
			for(JSONObject item:data)
			{
				String key=transformString(item.get(keyProperty));
				
				if(!result.containsKey(key))
				{
					result.put(key, item);
				}
			}
		}
		
		return result;
	}
	
	
	/****
	 * 把列表数据转成Map接口，以指定的key 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,LessonInfo> convertToMap(List<LessonInfo> data,_Fields field)
	{
	
		Map<String,LessonInfo> result=new HashMap<String,LessonInfo>();
		
		if(data!=null&&data.size()>0)
		{
			for(LessonInfo object:data)
			{
			  try {
				String fieldValue=transformString(object.getFieldValue(field));
				if(!result.containsKey(fieldValue))  result.put(fieldValue, object);
			  } catch (IllegalArgumentException e) {
				 e.printStackTrace();
			  }
			}
		}
		
		return result;
	}
	
	
	/****
	 * 把列表数据转成Map接口，以指定的key 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,Long> convertToMap(List<Account> data,com.talkweb.accountcenter.thrift.Account._Fields field,Account account)
	{
	
		Map<String,Long> result=new HashMap<String,Long>();
		
		if(data!=null&&data.size()>0)
		{
			for(Account object:data)
			{
			  if(object==null) continue;
			  try {
				String fieldValue=transformString(object.getFieldValue(field));
				if(!result.containsKey(fieldValue))  result.put(fieldValue, object.id);
			  } catch (IllegalArgumentException e) {
				 e.printStackTrace();
			  }
			}
		}
		
		return result;
	}
	
	
	
	/****
	 * 把列表数据转成Map接口，以指定的onewKeyPropert,twoKeyProperty字段存放的内容 作为主键。
	 * @param data
	 * @param keyProperty
	 * @return
	 */
	public static Map<String,Map<String,Object>> convertToMap(List<Map<String,Object>> data,String onewKeyProperty,String twoKeyProperty)
	{
		Map<String,Map<String,Object>> result=new HashMap<String,Map<String,Object>>();
		
		if(isEmpty(onewKeyProperty)||isEmpty(twoKeyProperty)) return result;
		
		if(data!=null&&data.size()>0)
		{
			for(Map<String,Object> item:data)
			{
				String oneKey=transformString(item.get(onewKeyProperty));
				String twoKey=transformString(item.get(twoKeyProperty));
				
				if(!result.containsKey(oneKey+twoKey))
				{
					result.put(oneKey+twoKey, item);
				}
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertToListFromStr(String str, String seperate, Class<T> clazz){
		if(StringUtil.isEmpty(str)){
			return new ArrayList<T>();
		}
		if(StringUtil.isEmpty(seperate)){
			List<T> list = new ArrayList<T>(1);
			list.add(JSONObject.parseObject(str, clazz));
			return list;
		}
		String[] tmps = str.split(seperate);
		List<T> list = new ArrayList<T>(tmps.length);
		for(String tmp : tmps) {
			if(String.class.equals(clazz)) {
				list.add((T) tmp);
			} else {
				list.add(JSONObject.parseObject(tmp, clazz));
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Set<T> convertToSetFromStr(String str, String seperate, Class<T> clazz){
		if(StringUtil.isEmpty(str)){
			return new HashSet<T>();
		}
		if(StringUtil.isEmpty(seperate)){
			Set<T> set = new HashSet<T>(1);
			set.add(JSONObject.parseObject(str, clazz));
			return set;
		}
		String[] tmps = str.split(seperate);
		Set<T> set = new HashSet<T>(tmps.length);
		for(String tmp : tmps) {
			if(String.class.equals(clazz)) {
				set.add((T) tmp);
			} else {
				set.add(JSONObject.parseObject(tmp, clazz));
			}
		}
		return set;
	}
}
