package com.talkweb.common.action;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.talkweb.common.tools.GeneratorRandImage;

/**
 * @ClassName GenerateRandImage
 * @author Homer
 * @version 1.0
 * @Description 生成验证码
 * @date 2015年3月5日
 */
@Controller
@RequestMapping("/common/authcode/")
public class AuthCodeAction {
	
	/**
	 * 生成随便图片验证码
	 * @param request 请求对象
	 * @param response 响应对象
	 * @return
	 */
	@RequestMapping(value="generateImage",method=RequestMethod.GET)
	public void generateImage(HttpServletResponse response,HttpSession session){
		
		//获取流，指定流给图片
	    OutputStream out = null;
		try {
			out = response.getOutputStream();
			//实例化GeneratorRandImage。
		    GeneratorRandImage fr = new GeneratorRandImage(out);		
			//调用产生图片的方法，同事获得正确的验证码
			String strCode = fr.getRandImage();
	        //获取绘画对象，并把正确的验证码保存到session里面
		    session.setAttribute("validate", strCode);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	
}
