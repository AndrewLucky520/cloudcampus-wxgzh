/**
 * 
 */
package com.talkweb.common.tools;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @ClassName: 
 * @version:1.0
 * @Description: 
 * @author 廖刚 ---智慧校
 * @date 2015年10月21日
 */
public class PrintStackTrace {

	/**
	 * 
	 */
	public PrintStackTrace() {
		// TODO Auto-generated constructor stub
	}
	
	public static String getErrorInfoFromException(Throwable e) {  
        try {  
            StringWriter sw = new StringWriter();  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
            return "\r\n" + sw.toString() + "\r\n";  
        } catch (Exception e2) {  
            return "bad getErrorInfoFromException";  
        }  
    }

}
