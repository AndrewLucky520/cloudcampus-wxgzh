package com.talkweb.common.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesUtil {
	
	private static Properties props = new Properties();
	
	private static PropertiesUtil instance;
		
	private PropertiesUtil(String relativePath, String fileName){
		try {
			 String prefix = this.getClass().getResource("/").getPath();
			 File parent = new File(prefix);
			 prefix = parent.getParent();
			 StringBuffer sb = new StringBuffer(prefix);
			 sb.append("\\");
			 sb.append(relativePath);
			 sb.append(fileName);
			 String filePath = sb.toString();
			 //String filePath = "/../config/server-path.properties"; 
			 FileInputStream fis =new FileInputStream(filePath);
			 props.load(fis);
		} catch (Exception ex) {
			 ex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param relativePath:{like ..\..\}
	 * @param fileName
	 * @return
	 */
	public static PropertiesUtil getInstance(String relativePath, String fileName) {  
	     if (instance == null) {  
	         instance = new PropertiesUtil(relativePath, fileName);  
	     }  
         return instance;  
    }  
	
	/**  -----获取属性值------ */
	public String getProperty(String key){
		return props.getProperty(key);
	}
		
}