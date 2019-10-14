package com.talkweb.onecard.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.common.action.BaseAction;
import com.talkweb.onecard.common.AESUtil;
import com.talkweb.onecard.common.Constants;

/**
 * @ClassName: OnecardManageAction.java
 * @version:1.0
 * @Description: 考勤管理控制器
 * @date 2016年3月7日
 */
@Controller
@RequestMapping(value = "/onecardManage/")
public class OnecardManageAction extends BaseAction {

	private boolean JSONObjectNotEmpty(JSONObject object) {
		return null != object && object.size() > 0;
	}

	/** -----跳转一卡通页面----- **/
	@RequestMapping(value = "/common/jumpOnecardPage.do", method = RequestMethod.GET)
	@ResponseBody
	public void getTimetableList(HttpServletRequest request,
			HttpServletResponse response) {	 
		PrintWriter out = null;
		try{
			// 通过getWriter方法获取PrintWriter对象
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
			
			HttpSession session = request.getSession();
			// 获取用户账号信息
			Account account = (Account) (session.getAttribute("account"));
			
			String schoolId = getXxdm(request);//学校编号
			String accountId = account.getId() + "";
			// 此处使用BASE64做转码功能		
			String code = AESUtil.Encrypt(accountId+"|"+(new Date()).getTime(),Constants.AESKEY);		
			String parameter = "userId="+accountId+"&schoolId="+schoolId+""+"&code="+ code;
			// 发送POST请求
			String responseJson = sendPost(Constants.POSTURL,parameter);
			JSONObject json = JSON.parseObject(responseJson);
			if (JSONObjectNotEmpty(json) && json.containsKey("resultCode")){
				String resultCode = json.getString("resultCode");			
				if (resultCode.equals("0")){
					// 获取考勤系统跳转页面地址
					String targetUrl = json.getString("url");
					// 重定向到考勤系统
					response.sendRedirect(targetUrl);
				}else if(resultCode.equals("-1")){
					printErrorPage(out,"该帐号验证错误!!!");
				}else{
					printErrorPage(out,"加密串验证错误!!!");
				}
			}else{
				printErrorPage(out,"POST请求信息异常!!!");
			}		
		} catch (Exception ex) {
			printErrorPage(out,ex.getMessage());
		}finally{
            if(out!=null){
               out.close();
            }
		}
	}
	
	/**
    * 向指定 URL 发送POST方法的请求
    * 
    * @param url
    *            发送请求的 URL
    * @param param
    *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
    * @return 所代表远程资源的响应结果
    * 
    */
    public String sendPost(String url, String param){
    	OutputStreamWriter writer = null;
        BufferedReader reader = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);// 向服务器写入数据
            conn.setDoInput(true);// 从服务器获取数据
            conn.setRequestMethod("POST");// POST方法
            conn.setConnectTimeout(3000);
            
            // 获取URLConnection对象对应的输出流
            writer = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            // 发送请求参数
            writer.write(param);
            // flush输出流的缓冲
            writer.flush();
            // 定义BufferedReader输入流来读取URL的响应
            reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                     result += line;
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }finally{
            try{
                if(writer!=null){
                   writer.close();
                }
                if(reader!=null){
                   reader.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }   
    
    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = URLEncoder.encode(url + "?" + param, "UTF-8");
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                     result += line;
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    /**
     * 跳转失败时输出提示信息到网页
     * 
     * @param out
     *            流对象
     * @param message
     *            提示信息
     * @return void
     * 
     */
    private void printErrorPage(PrintWriter out,String message){
    	out.flush();//清空缓存
    	//输出script标签    	
        out.write("<script language=\"JavaScript\" type=\"text/JavaScript\">");
        //js语句：输出alert语句
        out.write("alert('"+message+"');");
        out.write("window.close();");
        //输出网页回退语句
        //out.println("history.back();");
        out.write("</script>");//输出script结尾标签
    }
	
}