package com.talkweb.wishFilling.action;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.StudentPart;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.scoreManage.service.ScoreManageService;
import com.talkweb.wishFilling.dao.WishFillingSetDao;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.service.WishFillingStudentService;
import com.talkweb.wishFilling.util.CalibrationSpiderWebPlot;

import net.coobird.thumbnailator.Thumbnails;


/** 
 * 新高考志愿填报-手机端app action
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2017年3月2日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 */
@Controller
@RequestMapping(value = "/wishFilling/app/")
public class WishFillingAppAction extends BaseAction{
		@Autowired
		private FileServer fileServerImplFastDFS; 	
		@Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingStudentService wishFillingStudentService;
	    @Autowired
	    private WishFillingService wishFillingService;
	    @Autowired
	    private ScoreManageService scoreManageService;
	    @Autowired
		private WishFillingSetDao wishFillingSetDao;
	    /**
		 * 获取临时文件保存目录
		 */
		@Value("#{settings['tempFilePath']}")
		private String tempFilePath;
	    private  final String ATTACHMENTNAME ="radar.png" ;
		private static final Logger logger = LoggerFactory.getLogger(WishFillingAppAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
		
/**
 * 七、APPH5接口
 */
		/**
		 * （1）获取最近一次开放的待选课程
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			JSONObject returnObj = new JSONObject();
			try{
				String wfId = request.getString("wfId");
				logger.info("wf wfId===>:"+wfId);
				String schoolId = request.getString("schoolId");
				Long userId = request.getLong("userId");
				String termInfo = allCommonDataService.getCurTermInfoId( Long.parseLong(schoolId) );
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4,5);
				School school = allCommonDataService.getSchoolById(Long.parseLong(schoolId), termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				User user = allCommonDataService.getUserById(Long.valueOf(schoolId), userId,termInfo);
				T_Role role = user.getUserPart().getRole();
				if(role.getValue()!=T_Role.Student.getValue() && role.getValue()!=T_Role.Parent.getValue()){
					setPromptMessage(response, "2", "当前身份无法查看信息!");
					return  response;
				}
				 if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				StudentPart sp = user.getStudentPart();
				if(sp!=null && sp.getClassId()!=0){
					Long classId=sp.getClassId();
					Classroom c = allCommonDataService.getClassById(Long.parseLong(schoolId), classId, termInfo);
					Long gid = c.getGradeId();
					Grade g = allCommonDataService.getGradeById(Long.parseLong(schoolId), gid, termInfo);
					if(g.isGraduate){
						response.put("data",returnObj);
						setPromptMessage(response, "0", "查询成功");
						return response;
					}
					String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
					//获取最近一次开放中的选课的wfId
					JSONObject json = new JSONObject();
					List<JSONObject> wfList = null;
					if(StringUtils.isNotBlank(wfId)) {
						json.put("areaCode", areaCode);
						json.put("wfId", wfId);
						json.put("schoolId", schoolId);
						json.put("termInfo", termInfo);
						JSONObject wf = wishFillingSetDao.getTb(json);
						String wfGradeId =  wf.getString("wfGradeId");
						if(!useGrade.equals(wfGradeId)) {//查看当前人是否在待选学生/家长中
							setPromptMessage(response, "2", "当前身份无法查看信息!");
							return  response;
						}
						wfList = new ArrayList<JSONObject>();
						wfList.add(wf);
						logger.info("wf: wfList.add(wf)"+wf  );
					}else {
						json.put("schoolId", schoolId);
						json.put("wfUseGrade", useGrade);
					    wfList = wishFillingStudentService.getLastOpenTb(json);
					}
					if(wfList!=null && wfList.size()>0 &&  wfList.get(0)!=null){
						JSONObject wfObj = wfList.get(0);
						logger.info("wf: wfObj:"+wfObj  );
						String isByElection = wfObj.getString("isByElection");
						String wfStartTimeD = wfObj.getString("wfStartTime") ;
						String wfEndTimeD = wfObj.getString("wfEndTime") ;
						String byStartTimeD = wfObj.getString("byStartTime") ;
						String byEndTimeD = wfObj.getString("byEndTime") ;
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						Date wfStartTimeDate = null;
						Date wfEndTimeDate = null;
						Date byStartTimeDate = null;
						Date byEndTimeDate = null;
						if(StringUtils.isNotBlank(wfStartTimeD)) {
						  wfStartTimeDate = sdf.parse(wfStartTimeD);
						}
						if(StringUtils.isNotBlank(wfEndTimeD)) {
						  wfEndTimeDate = sdf.parse(wfEndTimeD);
						}
						if(StringUtils.isNotBlank(byStartTimeD)) {
						  byStartTimeDate = sdf.parse(byStartTimeD);
						}
						if(StringUtils.isNotBlank(byEndTimeD)) {
						  byEndTimeDate = sdf.parse(byEndTimeD);
						}
						Date nowDate = new Date();
						if("1".equals(isByElection)) { //补选
							if(nowDate.before(byStartTimeDate)){
								setPromptMessage(response, "5", "当前数据未开始");
								return response;
							}else if(nowDate.after(byEndTimeDate)){
								setPromptMessage(response, "4", "当前数据已结束");
								return response;
							}
						}else {
							if(nowDate.before(wfStartTimeDate)){
								setPromptMessage(response, "5", "当前数据未开始");
								return response;
							}else if(nowDate.after(wfEndTimeDate)){
								setPromptMessage(response, "4", "当前数据已结束");
								return response;
							}
						}
						if(StringUtils.isBlank(wfId)) {
						 wfId = wfObj.getString("wfId");
						}
						param.put("wfId", wfId);
						param.put("schoolId", schoolId);
						param.put("termInfo", termInfo);
						param.put("schoolYear", schoolYear);
						param.put("termInfoId", termInfoId);
						param.put("accountId", user.getAccountPart().getId());
						param.put("classId", classId);
						param.put("useGrade", useGrade);
						param.put("isJudge", 1);
						param.put("areaCode", areaCode);
						returnObj = wishFillingStudentService.getStudentTb(param);
						response.put("data", returnObj);
						setPromptMessage(response, "0", "查询成功");
					}else{
						response.put("data",returnObj);
						setPromptMessage(response, "3", "当前数据已失效");
					}
				}else{
					setPromptMessage(response, "-1", "查询失败");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * （2）提交填报
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/addStudentTb", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject addStudentTb(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			List<JSONObject> data = new ArrayList<JSONObject>();
			try {
				Long schoolId = request.getLong("schoolId");
				Long userId = request.getLong("userId");
				String termInfo = allCommonDataService.getCurTermInfoId(schoolId);
				School school = allCommonDataService.getSchoolById(schoolId, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				String wfId = request.getString("wfId");
				String zhId = request.getString("zhId");
				String schoolYear = termInfo.substring(0, 4);
				String termInfoId = termInfo.substring(4,5);
				JSONArray s = request.getJSONArray("subjectIds");
				List<Long> subList= new ArrayList<Long>();
				if(s!=null){
					for(int i=0;i<s.size();i++){
						String ss=(String) s.get(i);
						subList.add(Long.parseLong(ss));
					}
				}
				
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				param.put("zhId", zhId);
				param.put("termInfoId", termInfoId);
				param.put("schoolYear", schoolYear);
				param.put("subList", subList);
				User user=allCommonDataService.getUserById(schoolId, userId);
				
				if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001409664L);
				param.put("accountId", user.getAccountPart().getId());
				Long classId=user.getStudentPart().getClassId();
				param.put("classId", classId);
				List<Long> ids = new ArrayList<Long>();
				ids.add(classId);
				List<Classroom> cList = allCommonDataService.getSimpleClassBatch(schoolId, ids, termInfo);
				if(cList!=null && cList.size()>0){
					Classroom c = cList.get(0);
					Grade g = allCommonDataService.getGradeById(schoolId, c.getGradeId(), termInfo);
				    String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
					param.put("useGrade", useGrade);
				    int i = wishFillingStudentService.addStudentTb(param);
					if(i>0){
						setPromptMessage(response, "0", "操作成功");
					}else if(i==-2){
						setPromptMessage(response, "-1", "选科时间未开放！");
					}else if(i==-3){
						setPromptMessage(response, "-1", "您选择的组合已删除");
					}else if(i==-4){
						setPromptMessage(response, "-1", "提交的填报科目个数错误");
					}else if(i==-5){ 
						setPromptMessage(response, "-1", "补选已开始，请刷新后重新提交");
					}else if(i==-6){
						setPromptMessage(response, "-1", "您不能修改填报的科目组合");
					}else if(i==-7){
						setPromptMessage(response, "-1", "补选还未开始，请刷新后重新提交");
					}else{
						setPromptMessage(response, "-1", "操作失败");
					}
				}else{
					setPromptMessage(response, "-1", "当前登录用户的班级信息为空");
				}
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 *(3)获取蜘网图所需参数
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/getSpiderChart", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject getSpiderChart(HttpServletRequest req,
				@RequestBody JSONObject request, HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject data = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				long time1=new Date().getTime();
				int height = request.getInteger("height");
				int width = request.getInteger("width");
				String isCreateImage = request.getString("isCreateImage");
				String wfId = request.getString("wfId");
				Long schoolId = request.getLong("schoolId");
				String termInfo = allCommonDataService.getCurTermInfoId(schoolId);
				School school = allCommonDataService.getSchoolById(schoolId, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				Long userId = request.getLong("userId");
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				JSONObject tbObj = wishFillingService.getTb(param);
				String subjectIds = tbObj.getString("subjectIds");
				String isByElection = tbObj.getString("isByElection");
				String wfWay = tbObj.getString("wfWay");
				String pycc = tbObj.getString("pycc");
				/*if("0".equals(wfWay) && "1".equals(isByElection)){ //查询补选科目
					JSONObject tbObj1 = wishFillingService.getTb(param);
					subjectIds = tbObj1.getString("subjectIds");
				}*/
				User user = allCommonDataService.getUserById(schoolId, userId);
				if(user.getUserPart().getRole().equals(T_Role.Parent))
				{
					user=allCommonDataService.getUserById(Long.valueOf(schoolId), user.getParentPart().getStudentId(),termInfo);
				}
				//User user=allCommonDataService.getUserById(Long.valueOf(schoolId), 1001411924L);
				Long accountId = user.getAccountPart().getId();
				param.put("accountId", accountId );
				
				List<Long> fakeSubList = StringUtil.toListFromString(subjectIds);
				//转换成divideSubject
				Map<String,List<JSONObject>> subIdObjMap = new HashMap<String,List<JSONObject>>();
				List<Long> subList =new ArrayList<Long>(fakeSubList); 
				if("3".equals(pycc)){
					subList.clear();
					param.put("pycc", pycc);
					List<JSONObject> subjectDividedList =wishFillingSetDao.getDividedSubjectList(param);
					param.remove("pycc");
					for(JSONObject subDiv:subjectDividedList){
						List<JSONObject> sub = new ArrayList<JSONObject>();
						if(subIdObjMap.containsKey(subDiv.getString("ssubjectId"))){
							sub = subIdObjMap.get(subDiv.getString("ssubjectId"));
						}
						sub.add(subDiv);
						subIdObjMap.put(subDiv.getString("ssubjectId"), sub);
					}
					for(int i=0;i<fakeSubList.size();i++){
						long subId = fakeSubList.get(i);
						if(subIdObjMap.containsKey(subId+"")){
							List<JSONObject> subs = subIdObjMap.get(subId+"");
							if(subs!=null){
								for(JSONObject sub:subs){
									subList.add(sub.getLong("subjectId"));
								}
							}
						}else{
							subList.add(subId);
						}
					}
				}
				List<JSONObject> lList = wishFillingService.getDicSubjectList(schoolId+"",areaCode,tbObj.getString("pycc"), "0");
				Map<Long,JSONObject> lMap = new HashMap<Long,JSONObject>();
				if(lList!=null){
					for(JSONObject l:lList){
						lMap.put(l.getLong("subjectId"), l);
					}
				}
				HashMap<String, Object> map = new HashMap<String,Object>();
				map.put("schoolId", schoolId);
				map.put("studentId", accountId);
				Map<String, List<JSONObject>> returnMap = scoreManageService.getScoreStuBZF(map);
				//科目分值=A/N
				//A：标准分A分形式=基准分 +扩大系数*标准分
				//N：学生考试个数
				int N = returnMap.size(); //获取考试次数
				//subjectId-A
				Map<Long,Float> subMap = new HashMap<Long,Float>();
				for (Map.Entry<String, List<JSONObject>> entry : returnMap.entrySet()) 
				{
					List<JSONObject> list = entry.getValue();
					for(JSONObject obj:list){
						Long kmdm = obj.getLong("kmdm");
						if(!subList.contains(kmdm)){continue;}
						Float bzf = obj.getFloat("bzf");
						if(bzf==null ||kmdm==null){continue;}
						Float B = 80+5*bzf;
						if(B>100){B=100.0F;}
						if(subMap.containsKey(kmdm)){
							Float A = subMap.get(kmdm);
							A += B;
							subMap.put(kmdm, A);
						}else{
							Float A = B;
							subMap.put(kmdm, A);
						}
					}
				}
				List<JSONObject> subjects = new ArrayList<JSONObject>();
				int kmNumber = subMap.size();
				Float kmAverageScore = 0F;
				for (Map.Entry<Long, Float> entry : subMap.entrySet()) 
				{
					Long subjectId = entry.getKey();
					Float score = entry.getValue();
					Float s = score/N;
					kmAverageScore+=s;
					JSONObject obj = new JSONObject();
					obj.put("subjectId",subjectId);
					obj.put("score",Float.parseFloat(StringUtil.formatNumber(s,2)));
					JSONObject l = lMap.get(subjectId);
					if(l!=null){
						obj.put("subjectName", l.getString("subjectName"));
					}
					subjects.add(obj);
				}
				data.put("subjects", subjects);
				Float averageMax = kmAverageScore/kmNumber+5;
				Float averageMin = kmAverageScore/kmNumber-5;
				data.put("averageMax", averageMax);
				data.put("averageMin", averageMin);
				long time2=new Date().getTime();
				System.out.println("testzhh---------"+(time2-time1));
				 //删除相关图片文件
				
				if("1".equals(isCreateImage) && subjects!=null && subjects.size()>=3){
					JSONObject json = new JSONObject();
					json.put("schoolId", schoolId);
					String schoolYear=  termInfo.substring(0, 4);
					String termInfoId=  termInfo.substring(4, 5);
					json.put("schoolYear", schoolYear);
					json.put("termInfoId", termInfoId);
					json.put("accountId", accountId);
					JSONObject fileObj = wishFillingStudentService.getFile(json);
					if(fileObj!=null && fileObj.containsKey("attachmentId") && fileObj.containsKey("attachmentAddr")){
						String attachmentAddr = fileObj.getString("attachmentAddr");
						fileServerImplFastDFS.deleteFile(attachmentAddr);
						json.put("attachmentId", fileObj.getString("attachmentId"));
						wishFillingStudentService.deleteFile(json);
					}
					//操作后端生成蜘网图(同时文件上传、插入数据库)
					long time3=new Date().getTime();
					String attachmentId = this.createSpiderImage(subjects,json,width,height);
					long time4=new Date().getTime();
					System.out.println("testzhh---------"+(time4-time3));
					data.put("attachmentId", attachmentId);
					
				}
				response.put("data", data);
				setPromptMessage(response, "0", "查询成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "查询失败");
				e.printStackTrace();
			}
			return response;
		}
		/**
		 * 获取蛛网图片
		 * @param subjects 科目-成绩列表
		 * @param json 学校学生等信息
		 * @param width 宽度 
		 * @param height 高度
		 * @return
		 * @author zhanghuihui
		 */
		private String createSpiderImage(List<JSONObject> subjects , JSONObject json,int width,int height){
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for(JSONObject subject : subjects){
				String subjectName = subject.getString("subjectName");
				double score = subject.getDouble("score");
				String title  = subjectName+"("+ StringUtil.formatNumber(score+"",2)+")";
				dataset.addValue(score, "", title);
			}
			
			Font font = new Font("宋体",Font.PLAIN,16);
			CalibrationSpiderWebPlot webPlot = new CalibrationSpiderWebPlot(dataset);
			//设置无边线
			webPlot.setOutlinePaint(null);
			webPlot.setLabelFont(font);//设置字体大小
			webPlot.setLabelPaint(Color.black) ; // 字体颜色new Color(192,192,192)  
			JFreeChart jfreechart = new JFreeChart(webPlot);
			// 设置外层图片 无边框 无背景色 背景图片透明     
			jfreechart.setBorderVisible(false);  
			jfreechart.setBackgroundPaint(null);  
			jfreechart.setBackgroundImageAlpha(0.0f);
			//去掉底部title
			jfreechart.setSubtitles(new ArrayList());
			File file = null;
			String attachmentAddr = ""; 
			String attachmentId = UUIDUtil.getUUID(); 
			OutputStream os = null;
			try {
			    os = new FileOutputStream(tempFilePath+"/"+ATTACHMENTNAME);
				ChartUtilities.writeChartAsPNG(os, jfreechart, width, height);
				file = new File(tempFilePath+"/"+ATTACHMENTNAME);
				try {
						attachmentAddr = fileServerImplFastDFS.uploadFile(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(StringUtils.isNotBlank(attachmentAddr)){
					json.put("attachmentAddr", attachmentAddr);
					json.put("attachmentName", ATTACHMENTNAME);
					json.put("attachmentId", attachmentId);
					wishFillingStudentService.insertFile(json);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if(file!=null){
					file.delete();
				}
				if(os!=null){
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}// 关闭输出流
				}
			}
			return attachmentId;
		}
		/**
		 * 附件预览 thumbnailator-0.4.2版本
		 * @param req
		 * @param res
		 * @throws UnsupportedEncodingException
		 * @author zhanghuihui 
		 */
		@RequestMapping(value = "/preDownloadFile")
		public void preDownloadFile(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
			Integer outwidth = Integer.parseInt(req.getParameter("width"));
			Integer outheight = Integer.parseInt(req.getParameter("height"));
			String attachmentId = req.getParameter("attachmentId");
			JSONObject param = new JSONObject();
			param.put("attachmentId", attachmentId);
			JSONObject attObj = wishFillingStudentService.getFileById(param);
			String urlTemp = attObj.getString("attachmentAddr");
			InputStream  bis = null;
			BufferedOutputStream bos = null;
			BufferedInputStream bis1 = null;
			try {
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
 