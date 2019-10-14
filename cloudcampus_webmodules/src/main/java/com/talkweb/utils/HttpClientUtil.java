package com.talkweb.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

 

public class HttpClientUtil {

	static ResourceBundle rb = null;

	public static String getPath(String key) {
		if (rb == null) {
			rb = ResourceBundle.getBundle("constant.appconfig");
		}
		String path = rb.getString(key);
		return path;
	}

	public static String doGet(String url, Map<String, String> param)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String resultString = "";
		CloseableHttpResponse response = null;
		// 封装请求参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (param != null) {
			for (String key : param.keySet()) {
				params.add(new BasicNameValuePair(key, param.get(key)));
			}
		}
		try {
			// 转换为键值对
			String urlParam = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
			System.out.println("urlParam:" + urlParam);
			if (url.indexOf("?") > -1) {
				url += urlParam;
			} else {
				url += "?" + urlParam;
			}
			// 创建http GET请求
			HttpGet httpGet = new HttpGet(url);
			// 执行请求
			response = httpClient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
			} else {
				System.out.println(url + ",网络错误：" + response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return resultString;
	}

	public static JSONObject doGetToken(String url, Map<String, String> param, String access_token)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String resultString = "";
		CloseableHttpResponse response = null;
		JSONObject returnJson =null;
		// 封装请求参数
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (param != null) {
			for (String key : param.keySet()) {
				params.add(new BasicNameValuePair(key, param.get(key)));
			}
		}
		try {
			// 转换为键值对
			String urlParam = EntityUtils.toString(new UrlEncodedFormEntity(
					params, Consts.UTF_8));
			if (url.indexOf("?") > -1) {
				url += "&" + urlParam;
			} else {
				url += "?" + urlParam;
			}
			// 创建http GET请求
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("access_token", access_token);
			// 执行请求
			response = httpClient.execute(httpGet);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(),
						Consts.UTF_8);
				returnJson = JSONObject.parseObject(resultString);
			} else {
				System.out.println(url + ",网络错误："
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return returnJson;
	}

	public static String doPost(String url, Map<String, String> param)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			// 创建参数列表
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}
				// 模拟表单
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
				httpPost.setEntity(entity);
			}
			// 执行http请求
			response = httpClient.execute(httpPost);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
			} else {
				System.out.println(url + ",网络错误：" + response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return resultString;
	}

	public static String doPostToken(String url, Map<String, String> param, String access_token)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			// 创建参数列表
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}
				// 模拟表单
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
						paramList, Consts.UTF_8);
				httpPost.setEntity(entity);
			}
			httpPost.addHeader("access_token", access_token);
			// 执行http请求
			response = httpClient.execute(httpPost);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(),
						Consts.UTF_8);
			} else {
				System.out.println(url + ",网络错误："
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return resultString;
	}

	public static String doPostJson(String url, String json)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			// 创建请求内容
			StringEntity entity = new StringEntity(json,
					ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			// 执行http请求
			response = httpClient.execute(httpPost);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(),
						Consts.UTF_8);
			} else {
				System.out.println(url + ",网络错误："
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return resultString;
	}
	
	public static JSONObject doPostJsonToken(String url, String json,String access_token)
	{

		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		JSONObject returnJosn = null;
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			// 创建请求内容
			StringEntity entity = new StringEntity(json,
					ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			httpPost.addHeader("access_token", access_token);
			// 执行http请求
			response = httpClient.execute(httpPost);
			// 判断返回状态是否为200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(),
						Consts.UTF_8);
				returnJosn =JSONObject.parseObject(resultString);
			} else {
				System.out.println(url + ",网络错误："
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("httpclient close fail");
			}
		}
		return returnJosn;
	}
	
	public static JSONObject doPostNoToken(String url, String json,String clientId,String clientSecret) {
		// 创建Httpclient对象
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String resultString = "";
		JSONObject returnJosn=null;
		try {
			// 创建Http Post请求
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Client-Id", clientId);
			httpPost.addHeader("Client-Secret", clientSecret);
			// 创建请求内容
			StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			// 执行http请求
			response = httpClient.execute(httpPost);
			if(response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "utf-8");
				returnJosn =JSONObject.parseObject(resultString);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return returnJosn;
	}

	public static JSONObject postAction(String url, JSONObject param) {
		//log.debug("访问:{} 参数:{}", url, param.toJSONString());
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		JSONObject ret = new JSONObject();
		try {  
			if (param != null) {// 解决中文乱码问题
				StringEntity input = new StringEntity(param.toString(), "utf-8");
				input.setContentEncoding("UTF-8");    
				input.setContentType("application/json");
			    httppost.setEntity(input);
			}
		    
		    response = httpclient.execute(httppost);
		    String str = EntityUtils.toString(response.getEntity());
		    
		    int status = response.getStatusLine().getStatusCode();
		    if (status == 200) {
			    ret = JSONObject.parseObject(str);
		    } else {
		    	return JSONUtil.getResponse(-1, "访问URL:" + url + "异常." + response.getStatusLine());
		    }
        } catch (Exception e) {
	    	e.printStackTrace();
            return JSONUtil.getResponse(-1, "访问URL:" + url + "异常.");
        } finally {
			closeResponse(response);
			closeClient(httpclient);
        }
		return ret;
	}

	public static JSONObject postFile(String url, MultipartFile file, String key, JSONObject param) {
		try {
			HttpPost httppost = new HttpPost(url);
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
		    entityBuilder.setCharset(Charset.forName("utf-8"));  
		    entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		    entityBuilder.addBinaryBody(key, file.getBytes(), 
	    			ContentType.DEFAULT_BINARY, file.getOriginalFilename());
		    if (param != null) {// 解决中文乱码问题
		    	entityBuilder.addPart("param", new StringBody(param.toString(), ContentType.TEXT_PLAIN));
		    }
		    httppost.setEntity(entityBuilder.build());

		    CloseableHttpResponse response = null;
			CloseableHttpClient httpclient = HttpClients.createDefault();
			try {
			    response = httpclient.execute(httppost);
			    
			    int status = response.getStatusLine().getStatusCode();
			    String str = EntityUtils.toString(response.getEntity());
			    if (status == 200) {
				    return JSONObject.parseObject(str);
			    }
			    
		    	return JSONUtil.getResponse(-1, "访问URL:" + url + "异常." + response.getStatusLine());
	        } catch (Exception e) {
		    	return JSONUtil.getResponse(-1, "访问URL:" + url + "异常.", e);
	        } finally {
				closeResponse(response);
				closeClient(httpclient);
	        }
        } catch (Exception e) {
	    	return JSONUtil.getResponse(-1, "访问URL:" + url + "异常.", e);
        }
	}
	
	private static void closeResponse(CloseableHttpResponse closeableHttpResponse) {
		if (closeableHttpResponse != null) {
            try {
                closeableHttpResponse.close();
            } catch (Exception e) {
            }
        }
	}
	
	private static void closeClient(CloseableHttpClient client) {
		if (client != null) {
            try {
            	client.close();
            } catch (Exception e) {
            }
        }
	}

}
