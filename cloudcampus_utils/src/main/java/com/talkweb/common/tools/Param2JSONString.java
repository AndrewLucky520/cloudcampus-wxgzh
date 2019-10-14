package com.talkweb.common.tools;

public class Param2JSONString {

    /**
     * 
     * @param param 前端传过来的字符串(序列化和json数据的结合)
     * @return json字符串
     */
	public static String toJSONString(String param){
		
		if(null == param){
			throw new NullPointerException();
		}
		String trimParam = param.trim();
		if("".equals(trimParam)){
			throw new IllegalArgumentException(); 
		}
		
		StringBuffer jsonString = new StringBuffer("{");
		String[] splitParam = trimParam.split("&");
		for (String sp : splitParam) {
			String[] entry = sp.trim().split("=");
			String key = entry[0];
			String value = entry[1];
			if(value.contains("{")){
				jsonString.append("\"" + key+"\":"+ value);
			}else{
				jsonString.append("\"" + key+"\":" +"\""+ value+"\"");
			}
			jsonString.append(",");
		}	
		jsonString.append("}");
		jsonString.deleteCharAt(jsonString.lastIndexOf(","));
		return jsonString.toString();
		
	}
	

}
