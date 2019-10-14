package com.talkweb.student.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class XmlUtil {

    public static String getCodeByValue(String val,JSONArray list){
        String rs = null;
        
        for(int i=0;i<list.size();i++){
            JSONObject obj  = list.getJSONObject(i);
            String userDm = obj.getString("userDm");
            String value = obj.getString("mc");
            
            if(val.equalsIgnoreCase(userDm)||value.indexOf(val)!=-1){
                return userDm;
            }
        }
        
        return rs;
        
    }
}
