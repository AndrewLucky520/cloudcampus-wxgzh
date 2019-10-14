package com.talkweb.webutils;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

public class WebParamUtil {

	public static String getReqParam(HttpServletRequest request,String name){
		
		return getReqParam(request, name, "");
		
	}
	
	public static String getReqParam(HttpServletRequest request,String name, String defaultValue){
		String value = request.getParameter(name);
		if(StringUtils.isNotEmpty(value)){
			return value;
		}else{
			return defaultValue;
		}
		
	}
	
	public static int getReqParamInt(HttpServletRequest request,String name, int defaultValue){
		String value = request.getParameter(name);
		if(StringUtils.isNotBlank(value)){
			try{
			return Integer.parseInt(value);
			}catch(Exception e){ return defaultValue;}
		}else{
			return defaultValue;
		}
	}
	
	public static long getReqParamLong(HttpServletRequest request,String name, long defaultValue){
		String value = request.getParameter(name);
		if(StringUtils.isNotBlank(value)){
			try{
			return Integer.parseInt(value);
			}catch(Exception e){ return defaultValue;}
		}else{
			return defaultValue;
		}
	}
	
	public  static <T> JSONObject buildJsonObj(int total,List<T> dataRows,Object rowsHeader,String topmsg){
		JSONObject obj = new JSONObject();
		
		obj.put("total", total);
        obj.put("rows", dataRows);
        
        if(rowsHeader!=null){
        	obj.put("columns", rowsHeader);
        }
        
        if(topmsg!=null){
        	obj.put("topmsg", topmsg);
        }
		
        return obj;
	}
	
	public  static <T> JSONObject buildJsonObj(int total,List<T> dataRows,Object rowsHeader,Map<String,Object> topmsg){
		JSONObject obj = new JSONObject();
		
		obj.put("total", total);
        obj.put("rows", dataRows);
        
        if(rowsHeader!=null){
        	obj.put("columns", rowsHeader);
        }
        
        if(topmsg!=null){
        	obj.put("topmsg", topmsg);
        }
		
        return obj;
	}
	
}
