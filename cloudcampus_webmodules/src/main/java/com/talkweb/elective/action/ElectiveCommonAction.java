/**
 * 选课管理
 */
package com.talkweb.elective.action;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.elective.service.ElectiveService;
import com.talkweb.filemanager.service.FileServer;

import net.coobird.thumbnailator.Thumbnails;

/** 
 * 选课管理公共方法
 * @author  作者 E-mail: zhuxiaoyue@talkweb.com.cn
 * @date 创建时间：2015年7月16日 下午3:51:27 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/elective/common/")
public class ElectiveCommonAction extends BaseAction{

	@Autowired
	private ElectiveService electiveService;
	@Autowired
	private AllCommonDataService allCommonDataService;
	private static final Logger logger = LoggerFactory.getLogger(ElectiveCommonAction.class);
	@Autowired
	private FileServer fileServerImplFastDFS;
    //按最大展示比例来压缩上传到服务器
	private final Integer IMAGE_WIDTH = 460;
	private final Integer IMAGE_HEIGHT = 350;
	/**
	 * 获取课程类别列表
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getTypeList")
    @ResponseBody
    public JSONObject getTypeList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
			int isAll=requestParams.getIntValue("isAll");
			String electiveId=requestParams.getString("electiveId");
			String schoolId=getXxdm(req);
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("electiveId", electiveId);
			map.put("schoolId", schoolId);
			List<JSONObject> list=electiveService.getCourseTypeList(map);
			if(isAll>0){
			    JSONObject obj = new JSONObject();
			    obj.put("text", "全部");
			    String vals = "";
			    for(JSONObject ob :list){
			        vals += ob.getString("value")+",";
			    }
			    if(vals.length()>0){
			        vals = vals.substring(0,vals.length()-1);
			    }
			    obj.put("value", vals);
			    list.add(0, obj);
			}
			arr = (JSONArray) JSON.toJSON(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/common/getTypeList:",e);
			code = -1;
			msg = "未检索到课程类别！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }	
    
	/**
	 * 获取年级下的选修课程
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @RequestMapping(value = "getElectiveList")
    @ResponseBody
    public JSONObject getElectiveList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
//    		String selectedSemester=requestParams.getString("selectedSemester");
    		String schoolId=getXxdm(req);
			int isAll=requestParams.getIntValue("isAll");
			String electiveId=requestParams.getString("electiveId");
			String useGrade=requestParams.getString("useGrade");
			HashMap<String,Object> map=new HashMap<String,Object>();
			map.put("electiveId", electiveId);
			if(StringUtils.isNotEmpty(useGrade))
			{
				map.put("gradeList", Arrays.asList(useGrade.split(",")));
			}
			map.put("schoolId", schoolId);
			List<JSONObject> list=electiveService.getElectiveListByGrade(map);
			if(isAll>0){
			    JSONObject obj = new JSONObject();
			    obj.put("text", "全部");
			    String vals = "";
			    for(JSONObject ob :list){
			        vals += ob.getString("value")+",";
			    }
			    if(vals.length()>0){
			        vals = vals.substring(0,vals.length()-1);
			    }
			    obj.put("value", vals);
			    list.add(0, obj);
			}
			arr = (JSONArray) JSON.toJSON(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("/elective/common/getElectiveList:",e);
			e.printStackTrace();
			code = -1;
			msg = "未检索到选修课程！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }
    
	/**
	 * 获取课程下的班级
	 * @param requestParams
	 * @param req
	 * @param res
	 * @return
	 */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "getClassList")
    @ResponseBody
    public JSONObject getClassList(@RequestBody JSONObject requestParams,HttpServletRequest req, HttpServletResponse res) {
        JSONArray arr = new JSONArray();
        JSONObject json=new JSONObject();
		String msg = "";
		int code = 0;
    	try {
    		String selectedSemester=requestParams.getString("selectedSemester");
    		if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
    			selectedSemester=getCurXnxq(req);
    		}
    		String schoolId=getXxdm(req);
			int isAll=requestParams.getIntValue("isAll");
			String electiveId=requestParams.getString("electiveId");
			String useGrade=requestParams.getString("useGrade");
			String courseId=requestParams.getString("courseId");
			if(StringUtils.isNotEmpty(courseId))
			{
				HashMap<String,Object> map=new HashMap<String,Object>();
				map.put("electiveId", electiveId);
				map.put("courseId", courseId);
				map.put("schoolId", schoolId);
				map.put("gradeList", Arrays.asList(useGrade.split(",")));
				String classIds=electiveService.getClassListByCourse(map);
				if(null!=classIds&&StringUtils.isNotEmpty(classIds))
				{
					List<JSONObject> list=new ArrayList<JSONObject>();
					List<Long> cIds=StringUtil.toListFromString(classIds);
					List<Classroom> classList=allCommonDataService.getClassroomBatch(Long.valueOf(schoolId), cIds,selectedSemester);
					for(Classroom c:classList)
					{
						JSONObject obj = new JSONObject();
						if(StringUtils.isBlank(c.getClassName())|| c.getId()==0   ){continue;}
						obj.put("value", c.getId());
						obj.put("text", c.getClassName());
						list.add(obj);
					}
					if(isAll>0){
						JSONObject obj = new JSONObject();
					    obj.put("text", "全部");
					    obj.put("value", classIds);
					    list.add(0, obj);
					}
					list= (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, list, "text");
					arr = (JSONArray) JSON.toJSON(list);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("/elective/common/getClassList:",e);
			code = -1;
			msg = "未检索到该课程下的班级！";
		}
		json.put("code", code);
		json.put("msg", msg);
		json.put("data", arr);
		return json;
    }
    
    /**
     * 下载HTML表格方法
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/exportExcelByData")
    @ResponseBody  
    public void demoForDownExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        //JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        JSONArray excelData = (JSONArray) req.getSession().getAttribute("exportStaticByCourse");
		if(excelData==null){
			excelData=new JSONArray();
		}
        JSONArray newExcelHeads=new JSONArray();
        JSONArray newHead=new JSONArray();
        JSONArray oldHead=excelHeads.getJSONArray(0);
        
        JSONObject head0=new JSONObject();
        head0.put("field", "courseName");
        head0.put("title", "课程名称");
        head0.put("width", 150);
        head0.put("align", "center");
        head0.put("boxWidth", 150);
        head0.put("deltaWidth", 9);
        JSONObject head1=new JSONObject();
        head1.put("title", "选课学生人数");
        head1.put("field", "count");
        head1.put("width", 80);
        head1.put("align", "center");
        head1.put("boxWidth", 80);
        head1.put("deltaWidth", 9);
        JSONObject head2=new JSONObject();
        head2.put("title", "任课教师");
        head2.put("field", "teacher");
        head2.put("width", 100);
        head2.put("align", "center");
        head2.put("boxWidth", 100);
        head2.put("deltaWidth", 9);
        JSONObject head3=new JSONObject();
        head3.put("title", "教学场地");
        head3.put("field", "place");
        head3.put("width", 150);
        head3.put("align", "center");
        head3.put("boxWidth", 150);
        head3.put("deltaWidth", 9);
        newHead.add(head0);
        newHead.add(head1);
        newHead.add(head2);
        newHead.add(head3);
        for(int i=1;i<oldHead.size();i++)
        {
        	newHead.add(oldHead.get(i));
        }
        newExcelHeads.add(newHead);
        //"courseNameText":"生活中的物理;选课学生人数  0人;任课教师  ;教学场地 教学场地7"
        if(null!=excelData)
        {
        	List<JSONObject> removeList = new ArrayList<JSONObject>();
        	for(int i=0;i<excelData.size();i++)
        	{
        		JSONObject obj=excelData.getJSONObject(i);
        		String courseNameText=obj.getString("courseNameText");
        		if(StringUtils.isNotEmpty(courseNameText))
        		{
        			String[] strs=courseNameText.split(";");
        			logger.info("[elective]strs length;-----------------------strs.length",strs.length);
        			logger.info("[elective]strs length;-----------------------strs[0]",strs[0]);
        			if(strs.length==4){
        				
        				if(StringUtils.isNotBlank(strs[0].trim())){
        					System.out.println("[elective]00000000000");
        					obj.put("courseName", strs[0]);
        				}
        				logger.info("[elective]strs length;-----------------------strs.length{}",strs.length);
        				logger.info("[elective]strs[1] strs[1].substring(7, strs[1].length()-1);-----------------------strs[1]={}",strs[1]);
        				if(StringUtils.isNotBlank(strs[1].trim())){
        					System.out.println("[elective]11111111111111");
        					obj.put("count", strs[1].substring(7, strs[1].length()-1));
        				}
        				logger.info("[elective]obj.put(teacher, strs[2].substring(4, strs[2].length()).trim());-----------------------strs[2]={}",strs[2]);
        				if(StringUtils.isNotBlank(strs[2].trim())){
        					System.out.println("[elective]2222222222222222");
        					obj.put("teacher", strs[2].substring(4, strs[2].length()).trim());
        				}
        				if(StringUtils.isNotBlank(strs[3].trim())){
        					System.out.println("[elective]333333333333333");
        					obj.put("place", strs[3].substring(4, strs[3].length()).trim());
        				}
        			}else{
        				removeList.add(obj);
        			}
        			
        		}
        	}
        	
        	excelData.removeAll(removeList);
        }
        
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportExcelWithData(excelData , newExcelHeads,fileName, null, req, res);
    }
//    public static void main(String[] args) {
////		String courseNameText="生活中的物理;选课学生人数 0人;任课教师 ;教学场地 教学场地7";
//		String courseNameText="生活中的物理;选课学生人数 123人;任课教师 ;教学场地 教学场地7 哈哈哈";
////		String courseNameText="生活中的物理;选课学生人数 12人;任课教师 张三,李四;教学场地 ";
//		JSONObject obj=new JSONObject();
//			String[] strs=courseNameText.split(";");
//			obj.put("courseName", strs[0]);
//			obj.put("count", strs[1].substring(7, strs[1].length()-1));
//			obj.put("teacher", strs[2].substring(4, strs[2].length()).trim());
//			obj.put("place", strs[3].substring(4, strs[3].length()).trim());
//			System.out.println( obj);
//	}
    
    
    private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
    
    /**
     * 上传附件（一个轮次的一个课程只支持一张图片）JDK版本
     * @param file
     * @param req
     * @param res
     * @return
     */
   /* @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadFile(@RequestParam("fileBoby") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String electiveId = req.getParameter("electiveId");
		String termInfo = req.getParameter("termInfo");
		String schoolId = getXxdm(req);
		param.put("termInfo", termInfo);
		param.put("electiveId", electiveId);
		param.put("courseId", "");
		param.put("schoolId", schoolId);
		if(StringUtils.isNotBlank(termInfo)) {
			param.put("schoolYear", termInfo.substring(0, 4));
			param.put("term", termInfo.substring(4));
		}
		//查询是否已有图片
		try {
			param.put("isNotNull", "1");
			List<JSONObject> objList = electiveService.getAttachmentById(param);
			if(objList!=null && objList.size()>0){
				setPromptMessage(response, "-1", "您已上传过图片，请勿重复上传");
				return  response;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String fileNameTemp = file.getOriginalFilename();
		String fileTemp1 = fileNameTemp.replace("\\", "/");
		String fileName = fileTemp1.substring(fileTemp1.lastIndexOf("/")+1);
		
		String suffix = fileNameTemp
				.substring(fileNameTemp.lastIndexOf(".") + 1);
		String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF","BMP"};
		String[] pdfSubfix = new String[] {"PDF"};
		List<String> picExtension = Arrays.asList(picSubfix);
		List<String> pdfExtension = Arrays.asList(pdfSubfix);
		if (!picExtension.contains(suffix.toUpperCase()) && !pdfExtension.contains(suffix.toUpperCase())) {
			setPromptMessage(response, "-1", "文件格式不正确，请上传jpg,jpeg,png,gif,pdf格式文件");
			return response;
		}
		File saveFile = null;
		File sourceFile = null;
		File ratioFile = null;
		try {
			if(picExtension.contains(suffix.toUpperCase())){
				String tempName0 = UUID.randomUUID().toString() + "." + suffix;
				sourceFile = new File(tempName0);
				file.transferTo(sourceFile);
				String tempName1 = UUID.randomUUID().toString() + "." + suffix;
				ratioFile = new File(tempName1);
				Map<String, Integer> widthHeight = ImageUtil.getImageWidthHeight(sourceFile);
				Integer width = widthHeight.get("width");
				Integer height = widthHeight.get("height");
				float ratio;
				float widthRatio = ((float) width) / IMAGE_WIDTH;
				float heightRatio = ((float) height) / IMAGE_HEIGHT;
				if (widthRatio > 1 || heightRatio > 1) {
					ratio = Math.max(widthRatio, heightRatio);
					ImageUtil.resize(sourceFile, ratioFile, ratio, 1f);
					saveFile = ratioFile;
				}else{
					saveFile = sourceFile;
				}
			}else{
				String tempName0 = UUID.randomUUID().toString() + "." + suffix;
				saveFile = new File(tempName0);
				file.transferTo(saveFile);
			}
			param.put("attachmentName", fileName);
			param.put("createDate", new Date());
			JSONObject data = electiveService.uploadFile(param, saveFile);
			response.put("fileName", fileName);
			response.put("attachmentId", data.getString("attachmentId"));
			response.put("url", data.getString("url"));
			setPromptMessage(response, "0", "上传成功");
			// String url = fileServerImplFastDFS.uploadFile(ratioFile);
			// 保存，返回
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "服务器错误，上传失败");
		}
		if(saveFile!=null)saveFile.delete();
		if(sourceFile!=null)sourceFile.delete();
		if(ratioFile!=null)ratioFile.delete();
		return response;
	}*/
    
    /**
     * 上传附件（一个轮次的一个课程只支持一张图片）thumbnailator-0.4.2版本
     * @param file
     * @param req
     * @param res
     * @return
     * @author zhanghuihui
     */
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject uploadFile(@RequestParam("fileBoby") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {
    	JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String electiveId = req.getParameter("electiveId");
		String termInfo = req.getParameter("termInfo");
		String schoolId = getXxdm(req);
		param.put("termInfo", termInfo);
		param.put("electiveId", electiveId);
		param.put("courseId", "");
		param.put("schoolId", schoolId);
		if(StringUtils.isNotBlank(termInfo)) {
			param.put("schoolYear", termInfo.substring(0, 4));
			param.put("term", termInfo.substring(4));
		}
		
		String fileNameTemp = file.getOriginalFilename();
		String fileTemp1 = fileNameTemp.replace("\\", "/");
		String fileName = fileTemp1.substring(fileTemp1.lastIndexOf("/")+1);
		
		String suffix = fileNameTemp
				.substring(fileNameTemp.lastIndexOf(".") + 1);
		String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF","BMP"};
		String[] pdfSubfix = new String[] {"PDF"};
		List<String> picExtension = Arrays.asList(picSubfix);
		List<String> pdfExtension = Arrays.asList(pdfSubfix);
		if (!picExtension.contains(suffix.toUpperCase()) && !pdfExtension.contains(suffix.toUpperCase())) {
			setPromptMessage(response, "-1", "文件格式不正确，请上传jpg,jpeg,png,gif,pdf格式文件");
			return response;
		}
		File saveFile = null;
		File sourceFile = null;
		File ratioFile = null;
		try {
			if(picExtension.contains(suffix.toUpperCase())){
				String tempName0 = UUID.randomUUID().toString() + "." + suffix;
				sourceFile = new File(tempName0);
				file.transferTo(sourceFile);
				String tempName1 = UUID.randomUUID().toString() + "." + suffix;
				ratioFile = new File(tempName1);
				Map<String, Integer> widthHeight = getImageWidthHeight(sourceFile);
				Integer width = widthHeight.get("width");
				Integer height = widthHeight.get("height");
				float ratio;
				float widthRatio = ((float) width) / IMAGE_WIDTH;
				float heightRatio = ((float) height) / IMAGE_HEIGHT;
				if (widthRatio > 1 || heightRatio > 1) {
					//压缩
					Thumbnails.of(sourceFile).scale(1f).outputQuality(0.25f).toFile(ratioFile); 
					//等比缩放 按比例来缩放 而不是固定压缩
					 Thumbnails.of(ratioFile).size(IMAGE_WIDTH,IMAGE_HEIGHT).toFile(ratioFile);	
					saveFile = ratioFile;
				}else{
					saveFile = sourceFile;
				}
			}else{
				String tempName0 = UUID.randomUUID().toString() + "." + suffix;
				saveFile = new File(tempName0);
				file.transferTo(saveFile);
			}
			param.put("attachmentName", fileName);
			param.put("createDate", new Date());
			JSONObject data = electiveService.uploadFile(param, saveFile);
			response.put("fileName", fileName);
			response.put("attachmentId", data.getString("attachmentId"));
			response.put("url", data.getString("url"));
			setPromptMessage(response, "0", "上传成功");
			// String url = fileServerImplFastDFS.uploadFile(ratioFile);
			// 保存，返回
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "服务器错误，上传失败");
		}
		if(saveFile!=null)saveFile.delete();
		if(sourceFile!=null)sourceFile.delete();
		if(ratioFile!=null)ratioFile.delete();
		return response;
    }
    
    public static Map<String,Integer> getImageWidthHeight(File srcFile)throws IOException{
		Map<String,Integer> widHeght = new HashMap<String, Integer>();
		BufferedImage src = null;
		try{
			src = javax.imageio.ImageIO.read(srcFile);
			
		}catch(Exception e){
             FileInputStream inFile = new FileInputStream(srcFile);
             JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(inFile);
             BufferedImage image = decoder.decodeAsBufferedImage(); 
             
             FileOutputStream outFile = new FileOutputStream(srcFile);
             ImageIO.write(image, srcFile.getName().substring(srcFile.getName().lastIndexOf(".")+1), outFile);  
             inFile.close();
             outFile.close();

             src = ImageIO.read(srcFile);
             


		}
		int width = src.getWidth();
		int height = src.getHeight();
		widHeght.put("width", width);
		widHeght.put("height", height);
		return widHeght;
	}

   /**
    * 下载附件
    * @param req
    * @param attachmentId
    * @param res
    */
	@RequestMapping(value = "/downloadFile")
	public void downloadFile(HttpServletRequest req, HttpServletResponse res) {
		JSONObject param = new JSONObject();
		String courseId = req.getParameter("courseId");
		String electiveId = req.getParameter("electiveId");
		String termInfo = req.getParameter("termInfo");
		String attachmentId = req.getParameter("attachmentId");
		String schoolId = getXxdm(req);
		param.put("termInfo", termInfo);
		param.put("electiveId", electiveId);
		param.put("courseId", courseId);
		param.put("schoolId", schoolId);
		param.put("attachmentId", attachmentId);
		JSONObject data = new JSONObject();
		try {
			List<JSONObject> dataList = electiveService.getAttachmentById(param);
			if(dataList!=null && dataList.size()>0){
				data = dataList.get(0);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String attachmentName = data.getString("attachmentName");
		String attachmentAddr = data.getString("attachmentAddr");
		File temp = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			fileServerImplFastDFS.downloadFile(attachmentAddr, attachmentName);
			temp = new File(attachmentName);
			// 写入数据结束
			res.setContentType("octets/stream");
			String downLoadName = String.valueOf(URLEncoder.encode(attachmentName, "UTF-8"));
			res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName));
			bis = new BufferedInputStream(new FileInputStream(temp));
			bos = new BufferedOutputStream(res.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
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
			temp.delete();
		}
	}
	/**
	 * 删除附件
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/deleteFileById",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteFileById(HttpServletRequest req,@RequestBody JSONObject request,HttpServletResponse res){
		JSONObject response = new JSONObject();
		JSONObject param = new JSONObject();
		String attachmentId = request.getString("attachmentId");
		String termInfo = request.getString("termInfo");
		String schoolId = getXxdm(req);
		param.put("schoolId", schoolId);
		param.put("attachmentId", attachmentId);
		param.put("termInfo", termInfo);
		try {
			electiveService.deleteFileById(param);
			setPromptMessage(response, "0", "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			setPromptMessage(response, "-1", "删除失败");
		}
		return response;
	}
	/**
	 * 附件预览 （jdk版本）
	 * @param req
	 * @param res
	 * @throws UnsupportedEncodingException
	 */
	/*@RequestMapping(value = "/preDownloadFile")
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		JSONObject param = new JSONObject();
		float outwidth = Float.parseFloat(req.getParameter("width"));
		float outheight = Float.parseFloat(req.getParameter("height"));
		String courseId = req.getParameter("courseId");
		String electiveId = req.getParameter("electiveId");
		String termInfo = req.getParameter("termInfo");
		String attachmentId = req.getParameter("attachmentId");
		String schoolId = getXxdm(req);
		param.put("termInfo", termInfo);
		param.put("electiveId", electiveId);
		param.put("courseId", courseId);
		param.put("schoolId", schoolId);
		param.put("attachmentId", attachmentId);
		ByteArrayInputStream bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		try {
			param.put("isNotNull", "1");
			List<JSONObject> dataList = electiveService.getAttachmentById(param);
			JSONObject data = new JSONObject();
			if(dataList!=null && dataList.size()>0){
				data = dataList.get(0);
			}
			String urlTemp = data.getString("attachmentAddr");
			String url = URLDecoder.decode(urlTemp, "UTF-8");
			String suffix = urlTemp.substring(urlTemp.lastIndexOf(".") + 1);
			String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF"};
			List<String> picExtension = Arrays.asList(picSubfix);
			
			if (picExtension.contains(suffix.toUpperCase())) {
				byte[] buff = fileServerImplFastDFS.downloadFile(url);
				bis = new ByteArrayInputStream(buff);
				byte[] outBuff = ImageUtil.reduceImageOut(bis,outwidth,outheight);
				res.setContentType("image/jpeg");
				bos = new BufferedOutputStream(res.getOutputStream());
				bos.write(outBuff);
				bos.flush();
			}else{
				String fileName = urlTemp.split("/")[urlTemp.split("/").length - 1];
				fileServerImplFastDFS.downloadFile(url, fileName);
				File temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = String.valueOf(URLEncoder.encode(fileName, "UTF-8"));
				System.out.println(downLoadName);
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
			}
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
	}*/
	/**
	 * 附件预览 thumbnailator-0.4.2版本
	 * @param req
	 * @param res
	 * @throws UnsupportedEncodingException
	 * @author zhanghuihui 
	 */
	@RequestMapping(value = "/preDownloadFile")
	public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		JSONObject param = new JSONObject();
		Integer outwidth = Integer.parseInt(req.getParameter("width"));
		Integer outheight = Integer.parseInt(req.getParameter("height"));
		String termInfo = req.getParameter("termInfo");
		String attachmentId = req.getParameter("attachmentId");
		String schoolId = getXxdm(req);
		if(schoolId==null|| "null".equals(schoolId) || StringUtils.isBlank(schoolId)){
			schoolId = req.getParameter("schoolId");
		}
		param.put("termInfo", termInfo);
		param.put("schoolId", schoolId);
		param.put("attachmentId", attachmentId);
		InputStream  bis = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis1 = null;
		try {
			param.put("isNotNull", "1");
			List<JSONObject> dataList = electiveService.getAttachmentById(param);
			JSONObject data = new JSONObject();
			if(dataList!=null && dataList.size()>0){
				data = dataList.get(0);
			}
			String urlTemp = data.getString("attachmentAddr");
			String url = URLDecoder.decode(urlTemp, "UTF-8");
			String suffix = urlTemp.substring(urlTemp.lastIndexOf(".") + 1);
			String[] picSubfix = new String[] { "JPG", "JREG", "PNG", "GIF"};
			List<String> picExtension = Arrays.asList(picSubfix);
			
			if (picExtension.contains(suffix.toUpperCase())) {
				byte[] buff = fileServerImplFastDFS.downloadFile(url);
				bis = new ByteArrayInputStream(buff);
				Thumbnails.of(bis).size(outwidth,outheight);
				ByteArrayOutputStream swapStream = new ByteArrayOutputStream(); 
				byte[] buff1 = new byte[100]; //buff用于存放循环读取的临时数据 
				int rc = 0; 
				while ((rc = bis.read(buff1, 0, 100)) > 0) { 
					swapStream.write(buff1, 0, rc); 
				} 
				swapStream.flush();
				byte[] outBuff = swapStream.toByteArray(); //outBuff为转换之后的结果 
				res.setContentType("image/jpeg");
				bos = new BufferedOutputStream(res.getOutputStream());
				bos.write(outBuff);
				bos.flush();
			}else{
				String fileName = urlTemp.split("/")[urlTemp.split("/").length - 1];
				fileServerImplFastDFS.downloadFile(url, fileName);
				File temp = new File(fileName);
				// 写入数据结束
				res.setContentType("octets/stream");
				String downLoadName = String.valueOf(URLEncoder.encode(fileName, "UTF-8"));
				System.out.println(downLoadName);
				res.addHeader("Content-Disposition", "attachment;filename=".concat(downLoadName));
				bis1 = new BufferedInputStream(new FileInputStream(temp));
				bos = new BufferedOutputStream(res.getOutputStream());
				byte[] buff = new byte[2048];
				int bytesRead;
				while (-1 != (bytesRead = bis1.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
				bos.flush();
			}
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
}
