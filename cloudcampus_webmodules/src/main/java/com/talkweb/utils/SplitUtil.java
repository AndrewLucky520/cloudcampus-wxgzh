package com.talkweb.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.ExcelTool;

public class SplitUtil {

	static ResourceBundle rb = null;

	public static String getRootPath(String key) {
		return getRootPath(key, null);
	}
	
	public static String getRootPath(String key, String defaul) {
		if (rb == null) {
			rb = ResourceBundle.getBundle("constant.appconfig");
		}
		String baseurl = null;
		try {
			if (key != null && rb.containsKey(key)) {
				String tmp = rb.getString(key);
				if (!StringUtils.isEmpty(tmp)) {
					baseurl = tmp;
				}
			}
		} catch(Exception ex) {}
		
		if (StringUtils.isEmpty(baseurl) && rb.containsKey("base.url")) {
			baseurl = rb.getString("base.url");
			if (baseurl != null && !baseurl.endsWith("/")) {
				baseurl += "/";
			}
			if (!StringUtils.isEmpty(defaul)) {
				baseurl += defaul;
			} else if (!StringUtils.isEmpty(key)) {
				baseurl += FilenameUtils.getBaseName(key);
			}
		}
		if (baseurl != null && !baseurl.endsWith("/")) {
			baseurl += "/";
		}
		return baseurl;
	}

	public static JSONObject postAction(HttpServletRequest request, String url, JSONObject param) {
		return postAction(request, url, param, null);
	}
	
	public static JSONObject postAction(HttpServletRequest request, String url, JSONObject param, T_Role role) {
		//System.out.println("param ===== " + param);
		JSONObject ck = checkUser(request, param, role);
		if (ck != null)
			return ck;
		//System.out.println("param ==2222=== " + param);
		return HttpClientUtil.postAction(url, param);		
	}

	private static JSONObject checkUser(HttpServletRequest request, JSONObject param, T_Role role) {
		if (request == null) return null;
		
		School school = (School) request.getSession().getAttribute("school");
		Object schoolId = request.getSession().getAttribute("xxdm");
		User user = (User) request.getSession().getAttribute("user");

		if (user == null && param != null && param.containsKey("curUser")) {
			user = (User) param.get("curUser");
		}
		Object rtype = request.getSession().getAttribute("roleType");
		if (role != null) {
			if (user == null) {
				return JSONUtil.getResponse(-1, "系统异常！");
			}
			
			if (!role.equals(user.getUserPart().getRole())) {
				return JSONUtil.getResponse(-1, "用户角色错误 !!!");
			}
		}

		if (param == null) return null;
		
		if (!param.containsKey("schoolId"))
			param.put("schoolId", schoolId);
		param.put("sessionId", request.getSession().getId());
		if (!StringUtils.isEmpty(rtype)) {
			param.put("roleType", rtype);
		} else if (user != null) {
			param.put("roleType", user.getUserPart().getRole().getValue());
		}

		JSONObject cur = new JSONObject();
		param.put("curUser", cur);
		if (user != null) {
			cur.put("accountId", user.getAccountPart().getId());
			cur.put("accountName", user.getAccountPart().getName());
		}
		if (school != null) {
			param.put("schoolExtId", school.getExtId());
			param.put("schoolName", school.getName());
		}
		return null;
	}

	public static JSONObject postFile(String url, MultipartFile file, String key, JSONObject param) {
		return HttpClientUtil.postFile(url, file, key, param);
	}
	
	public static void downloadStream(HttpServletResponse res, String fileName, byte[] stream) {
		BufferedOutputStream bos = null;
		try {
			// 写入数据结束
			res.setContentType("octets/stream");
			String downLoadName = URLEncoder.encode(fileName, "UTF-8");
			res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName));
			bos = new BufferedOutputStream(res.getOutputStream());
			bos.write(stream);
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static byte[] urlToBytes(String str) throws Exception {
		ByteArrayOutputStream fOut = new ByteArrayOutputStream();
		URL url = new URL(str.replaceAll(" ", "%20"));
		IOUtils.copy(url.openStream(), fOut);
		byte[] bt = fOut.toByteArray();
		return bt;
	}

    public static byte[] compressImage(InputStream image, float newWidth, 
    		float newHeight, String ext) throws Exception {
        Image src = ImageIO.read(image);
        int width = src.getWidth(null); // 得到源图片宽
        int height = src.getHeight(null);// 得到源图片高
        if (width < newWidth && height < newHeight) {
        	return null;
        }
        //按比例缩放或扩大图片大小，将浮点型转为整型
        Float rate = ((float)newWidth) / width;
        int newH = (int) (height * rate);
        if (newHeight > 0 && newH > newHeight) {
        	rate = ((float)newHeight) / height;
        	newWidth = (int) (width * rate);
        } else {
        	newHeight = newH;
        }

        // 构造一个类型为预定义图像类型之一的 BufferedImage
        BufferedImage tag = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB);

        //绘制图像  getScaledInstance表示创建此图像的缩放版本，返回一个新的缩放版本Image,按指定的width,height呈现图像
        //Image.SCALE_SMOOTH,选择图像平滑度比缩放速度具有更高优先级的图像缩放算法。
        tag.getGraphics().drawImage(src.getScaledInstance((int) newWidth, (int) newHeight, Image.SCALE_SMOOTH), 0, 0, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //创建文件输出流
        ImageIO.write(tag, ext, out);
        byte[] bt = out.toByteArray();
        //关闭文件输出流
        out.close();
        return bt;
    }

	public static void exportExcelWithData(HttpServletRequest req, HttpServletResponse res) {
		JSONArray arr = JSONArray.parseArray(req.getParameter("param"));
		JSONArray excelHeads = new JSONArray();
		JSONArray line = new JSONArray();
		JSONObject col = new JSONObject();
		col.put("field", "rowNum");
		col.put("title", "行号");
		line.add(col);

		col = new JSONObject();
		col.put("field", "msg");
		col.put("title", "错误描述");
		line.add(col);
		excelHeads.add(line);
		JSONArray excelData = new JSONArray();
		for (int i = 0; i < arr.size(); i++) {
			JSONObject o = arr.getJSONObject(i);
			JSONObject row = new JSONObject();
			row.put("rowNum", o.get("row"));
			JSONArray cols = o.getJSONArray("mrows");
			String msg = "";
			for (int j = 0; j < cols.size(); j++) {
				JSONObject co = cols.getJSONObject(j);
				msg += co.getString("title") + "：" + co.getString("err") + "；";
			}
			row.put("msg", msg);
			excelData.add(row);
		}
		ExcelTool.exportExcelWithData(excelData, excelHeads, "错误信息", null, req, res, false);
	}

//
//	public static void main(String[] args) {
//		try {
//			byte[] bt = compressImage(new ByteArrayInputStream(urlToBytes("https://res-obs1.obs.cn-south-1.myhwclouds.com/NotAuthentication/a977bccc-dfd4-11e7-9100-fa163ed3019d/61ef553c57764d588164bddd7291a31a/01/04/06/8b922bf8-642b-483d-be5a-d6de3ce86433.jpg")), 100,100,"png");
//			FileOutputStream fs = new FileOutputStream("d:\\test.png");
//			fs.write(bt);
//			fs.close();
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
//	}

}
