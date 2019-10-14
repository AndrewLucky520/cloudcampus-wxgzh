package com.talkweb.ueditor.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.talkweb.common.action.BaseAction;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.ueditor.service.ActionEnterService;
 


@Controller
@RequestMapping("/UEditor")
public class UEditorAction extends BaseAction {
	
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Autowired
	private ActionEnterService actionEnterService;
	
	@RequestMapping(value = "/control" )
	public void control(HttpServletRequest request , HttpServletResponse response ){
 
		String str = null;
		if (actionEnterService!=null) {
			str = actionEnterService.exec(request);
		}else {
			str="{}";
		}
		try {
			PrintWriter writer = response.getWriter();
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
 	
		 
	}
	
	
	
	@RequestMapping(value = "/preDownloadFile")
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		String urlTemp = req.getParameter("url");
		String originTemp = req.getParameter("origin");
		String url = URLDecoder.decode(urlTemp, "UTF-8");
 
		int pos = url.lastIndexOf(".");
		if (url!=null && pos > 0) {
			url = url.substring(0, pos);
		}
		
		if (originTemp!=null) {
			String encode = getEncoding(originTemp); 
			originTemp = new String(originTemp.getBytes(encode),"UTF-8");
		}
		 
		ByteArrayInputStream bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		try {
				String fileName = originTemp.split("/")[originTemp.split("/").length - 1];//urlTemp.split("/")[urlTemp.split("/").length - 1];
				fileServerImplFastDFS.downloadFile(url, fileName);
				
				File temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = String.valueOf(URLEncoder.encode(fileName, "UTF-8"));
				System.out.println(downLoadName);
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName ));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
				temp.delete();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (bis1 != null)
				try {
					bis1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}
	
 
	
	public static String getEncoding(String str) {  
		
		String encode = "UTF-8";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s2 = encode;  
                return s2;  
            }  
        } catch (Exception exception2) {  
        } 
        
		 encode = "GB2312";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s = encode;  
                return s;  
            }  
        } catch (Exception exception) {  
        } 
        
        encode = "GBK";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s3 = encode;  
                return s3;  
            }  
        } catch (Exception exception3) {  
        } 
        
        
        encode = "ISO-8859-1";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
                String s1 = encode;  
                return s1;  
            }  
        } catch (Exception exception1) {  
        }  
 
 
        return "UTF-8";  
    }  
 

}
