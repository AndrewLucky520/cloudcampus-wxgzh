package com.talkweb.venueManage.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.venueManage.service.VenueManageImportService;
import com.talkweb.venueManage.service.VenueManageSetService;
import com.talkweb.venueManage.util.ImportTaskParameter;

/** 
 * 场馆使用-导入
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2015年11月24日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
@Controller
@RequestMapping(value = "/venueManage/venueImport/")
public class VenueManageImportAction extends BaseAction{
	    @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private VenueManageImportService venueManageImportService;
	    @Autowired
	    private VenueManageSetService venueManageSetService;
	    @Autowired
	    private FileImportInfoService fileImportInfoService;
		/**
		 * 获取临时文件保存目录
		 */
		@Value("#{settings['tempFilePath']}")
		private String tempFilePath;
		/**
		 * 文件服务
		 */
		@Autowired
		private FileServer fileServerImplFastDFS;
		/**
		 * redis
		 */
		@Resource(name="redisOperationDAOSDRTempDataImpl")
	 	private RedisOperationDAO redisOperationDAO;
		/**
		 * 导入场馆excel的表头--系统字段: 表头中文名称
		 * --->现在改存session
		 */
		//private static String[] stuTitle = null;

		/**
		 * 导入场馆excel的表头--系统字段字段名:表头英文名称
		 * --->现在改存session
		 */
		//private static String[] stuTitleName = null;
		/**
		 * 导入场馆excel的表头--系统字段字段名 是否必录 0:否 1：是
		 * --->现在改存session
		 */
		//private static int[] stuTitleNeed = null;
		/**
		 * 多线程 每个用户有自己的参数组和进程
		 * --->现在改存redis
		 */
		//private static Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		/**
		 * 多线程 每个线程有自己的参数组和进程
		 * --->现在改存redis
		 */
		//private static Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		/**
		 * 多线程 每个用户有自己的临时文件目录
		 *  --->现在改存session
		 */
		//private static Hashtable<String, String> tempFileMap = new Hashtable<String, String>();
		/**
		 * 多线程 每个线程有自己的临时excel表头
		 * --->现在改存session
		 */
		//private static Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
		  /**
	     * 导入场馆excel的表头--教师姓名列表
	     * --->现在改存session
	     */
	    //private static String[] teacherName = null;
	    /**
	     * 导入场馆excel的表头--教师信息MAP （教师名称---教师JSONOBJ OBJ中包括了 该名称出现的次数）
	     *  --->现在改存session
	     */
	    //private static HashMap<String,JSONObject> teacherMap = null;
	    /**
	     * 导入场馆excel的表头--场馆类别MAP （场馆类别名称---JSONOBJ）
	     * --->现在改存session
	     */
	    //private static HashMap<String,JSONObject> venueTypeMap = null;
	    /**
	     * 导入场馆excel的表头--场馆类别MAP （场馆类别名称_场馆名称---setId）
	     * --->现在改存session
	     */
	    //private static HashMap<String,String> venueSetAndTypeMap = null;
		
	    
		private static final Logger logger = LoggerFactory.getLogger(VenueManageImportAction.class);
		/**
		 * 删除文件数据库中的文件及文件服务器的文件
		 * @param schoolId
		 * @param keyId
		 */
		private int delFileDBAndFileServer(String schoolId,String keyId){
			String fId=fileImportInfoService.getFileBy(schoolId, keyId);
			if(StringUtils.isNotBlank(fId)){
				fileImportInfoService.deleteFile(schoolId, keyId);	
				try {
					fileServerImplFastDFS.deleteFile(fId);
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
			return 1;
		}
		/**
		 * 上传文件
		 * @param file
		 * @param req
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/uploadExcel")
		@ResponseBody
		public JSONObject uploadExcel(
				@RequestParam("excelBody") MultipartFile file,
				HttpServletRequest req, HttpServletResponse res) {
			JSONObject obj = new JSONObject();
			File df = null;
		    String[] stuTitle = null;
		    String[] stuTitleName = null;
		    int[] stuTitleNeed = null;
		    String schoolId = getXxdm(req);
			logger.debug("导入sessionID:"+req.getSession().getId());
			logger.debug("获取学校代码："+getXxdm(req));
			if (stuTitle == null) {
				stuTitle = new String[8];
				stuTitle[0] = "场馆类别";
				stuTitle[1] = "场馆名称";
				stuTitle[2] = "管理人员";
				//2017-04-27-lime-S
				stuTitle[3] = "设备管理员";
				//2017-04-27-lime-E
				stuTitle[4] = "地点";
				stuTitle[5] = "容纳人数";
				stuTitle[6] = "是否需审核";
				stuTitle[7] = "备注说明";
				
				stuTitleName = new String[stuTitle.length];
				stuTitleName[0] = "venueType";
				stuTitleName[1] = "venueName";
				stuTitleName[2] = "teacherNames";
				stuTitleName[3] = "equipmentTeacherNames";
				stuTitleName[4] = "venueAddr";
				stuTitleName[5] = "venueNum";			
				stuTitleName[6] = "isNeedExam";
				stuTitleName[7] = "comment";
				
				stuTitleNeed = new int[stuTitle.length];
				stuTitleNeed[0] = 1;
				stuTitleNeed[1] = 1;
				stuTitleNeed[2] = 1;
				stuTitleNeed[3] = 0;
				stuTitleNeed[4] = 0;
				stuTitleNeed[5] = 0;
				stuTitleNeed[6] = 0;
				stuTitleNeed[7] = 0;

			}
			req.getSession().setAttribute("stuTitle",stuTitle );
			req.getSession().setAttribute("stuTitleName", stuTitleName);
			req.getSession().setAttribute("stuTitleNeed", stuTitleNeed);
			int code = 1;
			String msg = "";
			// 临时文件保存目录
			File dir = new File(tempFilePath);
			if (!dir.exists()) {
				dir.mkdir();
			}
			String s="";
			try {
				// 初步解析上传的excel，判断字段是否符合原始系统字段
			    s = UUID.randomUUID().toString();
				// 获取源文件后缀名
				String fileName = file.getOriginalFilename();
				String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
				//文件名后缀转小写
				fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +prefix;
				df = new File(tempFilePath + "/" + s + "." + prefix);
				logger.debug("目标目录：" + tempFilePath + "/" + s + "." + prefix);
				file.transferTo(df);
				
				
				String fileId = fileServerImplFastDFS.uploadFile(df,fileName); //上传至文件服务器
				//将文件信息传入数据库
				req.getSession().setAttribute("keyId", s);
				req.getSession().setAttribute("fileId", fileId);
				fileImportInfoService.addFile(schoolId,"venueManage_"+s, fileId);
				
				String processId = req.getSession().getId();

				/*tempFileMap.put(processId, tempFilePath + "/" + s + "." + prefix);//存公共变量
				Hashtable<String, String> tempFileMap=new Hashtable<String, String>(); //存redis
				tempFileMap.put(processId, fileId);
				tempFileMap.put(processId+"_fileName", fileName);
				Object key = "venueManage."+getXxdm(req)+".00.tempFileMap";
				redisOperationDAO.set(key, tempFileMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			   */
				Workbook workbook = null;
				if (prefix.equalsIgnoreCase("xls")) {
					workbook = new HSSFWorkbook(new FileInputStream(df));
				} else if (prefix.equalsIgnoreCase("xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(df));
				} else {
					code = -2;
					msg = "文件不是excel格式！";
				}
				Sheet sheet = workbook.getSheetAt(0);
				int rows = sheet.getPhysicalNumberOfRows();

				if (rows > 0) {// 有数据时才处理
					Row row = sheet.getRow(0);
					int cellNum = row.getPhysicalNumberOfCells();
					//
					String[] tempStuImpExcTitle = new String[cellNum];
					// 判断是否需要进行手工字段匹配
					for (int i = 0; i < cellNum; i++) {
						String excelVal = row.getCell(i).getStringCellValue().trim();
						if (!isInArray(excelVal, stuTitle)) {
							code = 2;
							msg = "文件格式正确,字段需要匹配";
						}
						// 放置临时保存目录
						tempStuImpExcTitle[i] = excelVal;
					}
					//判断3个必填项标识是否存在
					if (code == 1) {
						if (isInArray("场馆类别", tempStuImpExcTitle)&&isInArray("场馆名称", tempStuImpExcTitle)
								&&isInArray("管理人员", tempStuImpExcTitle)) {
							code = 1;
						} 
						else {
							code = -3;
							msg = "请检查必填字段标识是否正确！";
						}
					}
					Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
					excelTitleMap.put(processId, tempStuImpExcTitle);
					req.getSession().setAttribute("excelTitleMap",excelTitleMap);
				} else {
					code = -2102;
					msg = OutputMessage.getDescByCode(code + "");
				}
				workbook.close();
			} catch (Exception e) {
				delFileDBAndFileServer(schoolId,"venueManage_"+s);
				logger.error("uploadExcel", e);
				code = -2101;
				msg = "文件格式错误或无法读取！";
				msg = OutputMessage.getDescByCode(code + "");
				
			}finally{
				if(df!=null){
					df.delete();
				}
			}
			// 封装返回结果
			obj.put("code", code);
			obj.put("msg", msg);
			return obj;
		}
		/**
		 * 判断字符串是否在字符串数组内
		 * 
		 * @param string
		 * @param arr
		 * @return boolean
		 * @author zhh
		 */
		private boolean isInArray(String string, String[] arr) {
			String source = string.replace(" ", "");				
			boolean rs = false;
			for (int i = 0; i < arr.length; i++) {
				String target = (arr[i] == null?"":arr[i]);
				target = target.replace(" ", "");
				if (target.equalsIgnoreCase(source)) {
					rs = true; break;
				}
			}
			return rs;
		}
		/**
		 * 字符串在数组中的索引
		 * 
		 * @param string
		 * @param stutitle2
		 * @return int
		 * @author zhh
		 */
	    private int strIndexInArray(String string, String[] arr) {
			String source = string.replace(" ", "");				
	        int rs = -1;
	        for (int i = 0; i < arr.length; i++) {
				String target = (arr[i] == null?"":arr[i]);
				target = target.replace(" ", "");
	            if (target.equalsIgnoreCase(source)) {
	                rs = i;break;
	            }
	        }
	        return rs;
	    }
	    /**
		 * 获取Excel表头和系统字段
		 * @param req
		 * @return  JSONOBJ
		 * @author zhh
		 */
		@RequestMapping(value = "/getExcelMatch")
		@ResponseBody
		public JSONObject getExcelMatch(HttpServletRequest req) {
			JSONObject excelHead = new JSONObject();
			JSONObject rs = new JSONObject();
			// 获取session中保存的临时表头
			String processId = req.getSession().getId();
			Hashtable<String, String[]> excelTitleMap = (Hashtable<String, String[]>) req.getSession().getAttribute("excelTitleMap");
			if(excelTitleMap!=null){
				String[] tmpTit = (String[]) excelTitleMap.get(processId);
				if (tmpTit != null) {
					// 开始拼装返回数据结构
					excelHead.put("total", tmpTit.length);
					JSONArray rows = new JSONArray();
					excelHead.put("rows", rows);
					for (int i = 0; i < tmpTit.length; i++) {
						JSONObject obj = new JSONObject();
						obj.put("field", tmpTit[i]);
						rows.add(obj);
					}
				} else {
					excelHead.put("total", 0);
				}
				String [] stuTitle=(String[]) req.getSession().getAttribute("stuTitle" );
				int [] stuTitleNeed=(int[]) req.getSession().getAttribute("stuTitleNeed" );
				if(stuTitle!=null && stuTitleNeed!=null){
					JSONObject moduleField = new JSONObject();
					// 直接使用系统表头 系统表头是否必填数组开始拼装返回数据结构
					moduleField.put("total", stuTitle.length);
					JSONArray rows = new JSONArray();
					moduleField.put("rows", rows);
					for (int i = 0; i < stuTitle.length; i++) {
						JSONObject obj = new JSONObject();
						obj.put("field", stuTitle[i]);
						obj.put("sysfield", stuTitleNeed[i]);
						rows.add(obj);
					}
					
					rs.put("excelHead", excelHead);
					rs.put("moduleField", moduleField);
				}
		    }
			return rs;

		}

		/**
		 * 导入任务启动接口
		 * 
		 * @param req
		 * @param res
		 * @return
		 * @author zhh
		 */
		@RequestMapping(value = "/startImportTask")
		@ResponseBody
		public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody JSONObject mappost, HttpServletResponse res) {
			
			ImportTaskParameter stt = new ImportTaskParameter();
			User u = (User) req.getSession().getAttribute("user");
			String  keyId = (String) req.getSession().getAttribute("keyId");
			String accountId=u.getAccountPart().getId()+"";
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			String schoolId=this.getXxdm(req);
			stt.setIsMatch(mappost.getIntValue("isMatch"));
			if(stt.getIsMatch()==1)
			{
				stt.setMatchResult(mappost.getJSONArray("matchResult"));
			}
			
			stt.setSchoolId(schoolId);
			stt.setAccountId(accountId);
			//设置公共参数
			int commonCode=this.setAllCommonInfo(req,keyId);
			if(commonCode<0){
				JSONObject obj = new JSONObject();
				delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
		        return obj;
			}
			// 设置获取的参数
			String processId = req.getSession().getId();
			JSONObject procObj = new JSONObject();
			procObj.put("taskParam", stt);
			progressMap.put(processId, procObj);
			Object keyProgressMap = "venueManage."+getXxdm(req)+processId+".00.progressMap";
			JSONObject obj = new JSONObject();
	        obj.put("code", 0);
	        obj.put("msg", "正常启动任务");
			try {
				redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
				logger.info("progressMap缓存redis失败！...");
				e.printStackTrace();
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
				return obj;
			}
			
			SubProcess sp = new SubProcess(processId, 0, null,schoolId,req.getSession());
			sp.start();		

			JSONObject data = new JSONObject();
			data.put("progress", 0);
			data.put("msg", "正常启动任务");
			procObj.put("code", 0);
			procObj.put("data", data);
			logger.debug("主线程结束！");
			try {
				redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.info("progressMap缓存redis失败！...");
				delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
				e.printStackTrace();
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
				return obj;
			}
			return obj;

		}

		class SubProcess extends Thread {
			private String processId;
			/**
			 * 学校代码
			 */
			private String schoolId;
			/**
			 * 导入类型0：第一次导入，1：校验后继续导入
			 */
			private int impType;
			/**
			 * 请求的req
			 */
			private HttpSession ses;

			private JSONObject singleData;

			public SubProcess(String processId, int impType, JSONObject singleData,String schoolId,HttpSession ses) {
				this.processId = processId;
				this.impType = impType;
				this.singleData = singleData;
				this.schoolId= schoolId;
				this.ses= ses;
			}

			@Override
			public void run() {
				    long t1 = (new Date()).getTime();
				    Hashtable<String, JSONObject> progressMap = null;
				    Object keyProgressMap = "venueManage."+schoolId+processId+".00.progressMap";
				    String keyId = (String) ses.getAttribute("keyId");
				    try {
						progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
					} catch (Exception e2) {
						logger.info("keyProgressMap获取redis失败！...");
						e2.printStackTrace();
						delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
						JSONObject toFront = progressMap.get(processId);
						JSONObject data = new JSONObject();
						toFront.put("code", -50);
			            data.put("progress", 100);
			            data.put("msg", "redis缓存失败。");
			            toFront.put("data", data);
			            progressMap.put(processId, toFront);
						
					}
					if(progressMap==null){
						try {
							throw new Exception("progressMap为空");
						} catch (Exception e) {
							logger.info("progressMap为空");
							e.printStackTrace();
							delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
							JSONObject toFront = progressMap.get(processId);
							JSONObject data = new JSONObject();
							toFront.put("code", -50);
				            data.put("progress", 100);
				            data.put("msg", "redis缓存失败。");
				            toFront.put("data", data);
				            progressMap.put(processId, toFront);
						}
					}
				   
					// excel导入处理开始
					ImportTaskParameter sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
					String fId=(String) ses.getAttribute("fileId");
					if(StringUtils.isNotBlank(fId)){
						 
						int isMatch = sp.getIsMatch();  
						JSONArray mrs = sp.getMatchResult();
	
						JSONObject toFront = progressMap.get(processId);
						JSONObject data = new JSONObject();
						data.put("progress", 0);
						data.put("msg", "正在准备导入任务");
						toFront.put("data", data);
						toFront.put("code", 1);
						String[] ckExcTt = null;//excel表头
						JSONObject readRs = new JSONObject();
						readRs.put("code", 1);
						JSONArray datas = new JSONArray();
						int readCode=1;
						if (impType == 0) 
						{
							readRs = readExcelToData(fId,schoolId,keyId);
							readCode=readRs.getInteger("code");
							datas = readRs.getJSONArray("datas");
							ckExcTt = (String[]) datas.get(0);
						}
						try 
						{
							if ((readCode == 1 && datas.size() > 0) || impType == 1) 
							{
								// 开始校验
								JSONObject ckrs = new JSONObject();
								ckrs.put("ckRs", true);
								if (impType == 0) 
								{
									data.put("progress", 5);
									data.put("msg", "正在校验excel数据");
									toFront.put("data", data);
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
									JSONObject preDatas = changeData(datas, mrs, isMatch,processId,schoolId,ses,progressMap);
									HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) preDatas.get("rowDatas");
									ckrs = checkImpData(pureDatas, mrs, isMatch, processId, sp,ses,progressMap);
								}
								logger.debug("查询结果:" + ckrs);
								if (ckrs.getBooleanValue("ckRs") || impType == 1)
								{
									// 开始入库
									data.put("progress", 30);
									data.put("msg", "正在保存数据！");
									toFront.put("data", data);
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
									List<JSONObject> needInsert = getInsertEntityByCkrs(sp, processId,ses,progressMap);
									if (needInsert.size() >= 0) {
										int num=venueManageImportService.addVenueSetBatch(needInsert);						
										toFront.put("code", 2);
							            data.put("progress", 100);
							            data.put("msg", "导入成功，共计导入"+ num +"条信息记录！");
							            delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
							            progressMap.put(processId, toFront);
										try {
											redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
										} catch (Exception e) {
											logger.info("progressMap缓存redis失败！...");
											e.printStackTrace();
											delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
											toFront.put("code", -50);
								            data.put("progress", 100);
								            data.put("msg", "redis缓存失败。");
								            toFront.put("data", data);
								            progressMap.put(processId, toFront);
										}
							            toFront.put("data", data);
									}
			
								} else {
									toFront.put("code", -2);
									data.put("progress", 100);
									data.put("msg", "Excel数据校验不通过!");
									if(impType==0){						
										data.put("total", datas.size()-1);
									}else{
										data.put("total", 1);
									}
									JSONObject validateMsg = new JSONObject();
									validateMsg.put("total", ckrs.getJSONArray("wrongMsg")
											.size());
									validateMsg.put("rows", ckrs.getJSONArray("wrongMsg"));
									data.put("validateMsg", validateMsg);
									toFront.put("data", data);
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
								}
							}
							else
							{
								if (readCode == -10) {
									toFront.put("code", -1);
									data.put("progress", 100);
									data.put("msg", "服务器出错，上传的文件不存在!");
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
								} else if (readCode == -20) {
									toFront.put("code", -1);
									data.put("progress", 100);
									data.put("msg", "解析Excel时出错，可能为文件格式问题!");
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
								} else if (readCode == -30) {
									toFront.put("code", -1);
									data.put("progress", 100);
									data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
								}
							}
	
					} 
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						logger.error("run:", e);
						e.printStackTrace();
						delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
						readCode=-30;
						toFront.put("code", -1);
						data.put("progress", 100);
						data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
						progressMap.put(processId, toFront);
						try {
							redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
						} catch (Exception e1) {
							logger.info("progressMap缓存redis失败！...");
							e1.printStackTrace();
							delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
							toFront.put("code", -50);
				            data.put("progress", 100);
				            data.put("msg", "redis缓存失败。");
				            toFront.put("data", data);
				            progressMap.put(processId, toFront);
						}
					}
					
					finally
					{
						// excel导入处理结束
						long t2 = (new Date()).getTime();
						logger.info("导入子线程结束,耗时：" + (t2 - t1));
						logger.info("开始删除临时excel");
						
					}
				}else{
					logger.info("数据库files为空！...");
					//delFileDBAndFileServer(schoolId, schoolId);
					JSONObject toFront = progressMap.get(processId);
					JSONObject data = new JSONObject();
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "文件数据库为空！");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
					
				}
			}

		}
		/**
		 * 将excel读取为数据
		 * 
		 * @param impFrc
		 * @return
		 * @author zhh
		 */
		private JSONObject readExcelToData(String fileId,String schoolId,String keyId) {
			// TODO Auto-generated method stub
			JSONObject rs = new JSONObject();
			int code = 1;
			String impFrc = UUIDUtil.getUUID();
			JSONArray datas = new JSONArray();
			// 解析excel 封装对象
			Workbook workbook = null;
			try {
				//下载文件
				fileServerImplFastDFS.downloadFile(fileId,impFrc);
				/*if (impFrc.endsWith("xls")) {
					workbook = new HSSFWorkbook(new FileInputStream(
							new File(impFrc)));
				} else if (impFrc.endsWith("xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(
							new File(impFrc)));
				}*/
				workbook = WorkbookFactory.create(new File(impFrc)); //与上面写的一致，自动区分格式
				Sheet sheet = workbook.getSheetAt(0);
				int rows = sheet.getLastRowNum();
				// 转换器 一般poi取数字格式需转换
				DecimalFormat df = new DecimalFormat("0");

				if (rows > 0) {
					int cols = sheet.getRow(0).getPhysicalNumberOfCells();
					datas = new JSONArray();
					for (int i = 0; i <= rows; i++) {
						if (sheet.getRow(i) == null) {
							continue;
						}
						String[] temp = new String[cols + 1];
						boolean isTrueNull = true;
						for (int j = 0; j < cols; j++) {
							if (sheet.getRow(i).getCell(j) != null
									&& sheet.getRow(i).getCell(j).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
								Cell cell = sheet.getRow(i).getCell(j);
								switch (cell.getCellType()) {
								case HSSFCell.CELL_TYPE_NUMERIC:
									if (cell.getNumericCellValue()==Math.floor(cell.getNumericCellValue())) {
										temp[j] = df.format(cell.getNumericCellValue());
									}else {
										temp[j] =cell.getNumericCellValue() + "";
									}
									break;
								case HSSFCell.CELL_TYPE_STRING:
									temp[j] = cell.getRichStringCellValue()
											.getString().trim().replaceAll("，", ",");
									break;
								case HSSFCell.CELL_TYPE_FORMULA:
									try {
										temp[j] = String.valueOf(cell
												.getStringCellValue());
									} catch (IllegalStateException e) {
										temp[j] = String.valueOf(cell
												.getNumericCellValue());
									}
									break;

								}
								isTrueNull = false;
							} else {
								temp[j] = "";
							}
						}
						if (!isTrueNull   ) {
							if(i!=0){
								temp[cols] = i + ""; //最后一列存放第几行 从第一行开始
							}
							datas.add(temp);
						}
					}
				}
			} catch (FileNotFoundException e) {
				code = -10;
				delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			} catch (IOException e) {
				code = -20;
				e.printStackTrace();
				 delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			} catch (Exception e) {
				code = -30;
				e.printStackTrace();
				delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			}finally {
				File file=new File(impFrc);
				file.delete();
			}
	
			rs.put("code", code);
			rs.put("datas", datas);
			return rs;

		}
		/**
		 * 根据映射字段映射生成数据
		 * 
		 * @param datas excel表格数据
		 * @param mrs  匹配结果{sysField:系统字段,excelField：excel字段,mustField：是否必录, 1 必录（前端验证时需注意规则，所有必录项需匹配）
		 * @param isMatch 是否需要手工匹配,1 手工匹配，0 无需匹配
		 * @param processId 进程id
		 * @return JSONOBJ
		 * @author zhh
		 */
		private JSONObject changeData(JSONArray datas, JSONArray mrs, int isMatch,String processId,String schoolId,HttpSession ses,Hashtable<String, JSONObject> progressMap ) {
			JSONArray exDatas = new JSONArray();
			JSONObject rs = new JSONObject();
			String keyId=(String) ses.getAttribute("keyId");
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			HashMap<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
			HashMap<String, String> titleEnMap = new HashMap<String, String>();
			
			String [] stuTitle =(String []) ses.getAttribute("stuTitle");
			String [] stuTitleName = (String [])ses.getAttribute("stuTitleName");
			int [] stuTitleNeed= (int [])ses.getAttribute("stuTitleNeed");
			if(stuTitle!=null && stuTitleName !=null && stuTitleNeed!=null){
				String[] titles = (String[]) datas.get(0);//得到excel第一行表头
				//存储导入excel表头arr[0][]  是否必填arr[1][]  映射的系统字段：中文名称arr[2][]  映射实体字段：对应的英文名称 arr[3][]
				String[][] arr = new String[4][titles.length];
				// 无需手工匹配
				if (isMatch == 0)
				{
					// 封装表头
					for (int i = 0; i < titles.length-1; i++)
					{
						arr[0][i] = titles[i];
						int needIndex = strIndexInArray(titles[i], stuTitle);
						if (needIndex >= 0)
						{
							// 在系统字段中能找到
							arr[1][i] = stuTitleNeed[needIndex] + "";
							arr[2][i] = stuTitle[needIndex];
							arr[3][i] = stuTitleName[needIndex];
						}
						else
						{
							// 在系统字段中找不到 标记为不录入
							arr[1][i] = "-1";
							arr[2][i] = "none";
						}
					}
				}
				else
				{
					// 需要手工匹配的 根据匹配关系封装表头
					// 封装表头
					for (int i = 0; i < titles.length-1; i++)
					{
						String sysTit = "none";
						for (int j = 0; j < mrs.size(); j++) 
						{
							JSONObject obj = mrs.getJSONObject(j);
							if (titles[i]!=null&&titles[i].equalsIgnoreCase(obj.getString("excelField")))
							{
								sysTit = obj.getString("sysField");
								continue;
							}
						}
						arr[0][i] = titles[i];
						int needIndex = strIndexInArray(sysTit, stuTitle);
						if (needIndex >= 0) {
							// 在系统字段中能找到
							arr[1][i] = stuTitleNeed[needIndex] + "";
							arr[2][i] = stuTitle[needIndex];
							arr[3][i] = stuTitleName[needIndex];
						} else {
							// 在系统字段中找不到 标记为不录入
							arr[1][i] = "-1";
							arr[2][i] = "none";
							arr[0][i] = null;
						}
					}
				}
				//开始循环excel数据   第二行开始
				for (int i = 1; i < datas.size(); i++)
				{
					JSONObject d = new JSONObject();
					String[] cell = (String[]) datas.get(i);
					for (int j = 0; j < arr[0].length -1; j++)
					{
						int isIn = Integer.parseInt(arr[1][j]);
						if (isIn != -1) {
							// 放入数据 （excel映射json）
							String cellVal = cell[j];
							d.put(arr[3][j], cellVal);
							d.put(arr[3][j] + "Name", arr[0][j]);					
							titleEnMap.put(arr[0][j], arr[3][j]);
						}
					}
					int rowNum = Integer.parseInt(cell[arr[0].length-1 ])+1;  //表示取 readData的最后一列，该列表示当前第几行，从1开始
					d.put("rowNum", rowNum);
					rowDatas.put(rowNum, d);
					exDatas.add(d);
				}
	
				
				rs.put("rowDatas", rowDatas);
				rs.put("titleEnMap", titleEnMap);
				rs.put("impHead", arr[3]);
				prepDataMap.put(processId, rs);
			}
			Object keyPrepDataMap = "venueManage."+schoolId+processId+".00.prepDataMap";
			try {
				redisOperationDAO.set(keyPrepDataMap, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.info("redis缓存prepDataMap出现问题！...");
				JSONObject toFront = progressMap.get(processId);
				JSONObject data = new JSONObject();
				toFront.put("code", -50);
	            data.put("progress", 100);
	            data.put("msg", "redis缓存失败。");
	            toFront.put("data", data);
	            progressMap.put(processId, toFront);
	            delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
	            e.printStackTrace();
			}
			return rs;
		}
		
		/**
		 * 校验表格数据
		 * 
		 * @param pureDatas
		 * @param mrs
		 * @param isMatch
		 * @param sp
		 * @return JSONOBJ
		 * @author zhh
		 */
		@SuppressWarnings("unchecked")
		private JSONObject checkImpData(HashMap<Integer, JSONObject> pureDatas,JSONArray mrs, int isMatch, String processId,ImportTaskParameter sp,HttpSession ses,Hashtable<String, JSONObject> progressMap) {
			String schoolId=sp.getSchoolId();
			String keyId = (String) ses.getAttribute("keyId");
			// excel验证部分
			JSONObject rs = new JSONObject();
			// 导入进度
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			
			
			data.put("progress", 10);
			data.put("msg", "正在校验excel数据,匹配表头完成,校验数据");
			toFront.put("data", data);

			//redis获取teacherMap
			Object keyPrepDataMap = "venueManage."+schoolId+processId+".00.prepDataMap";
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			try {
				prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
			} catch (Exception e) {
				logger.info("redis获取 prepDataMap失败！");
				e.printStackTrace();
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
		            delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			}
			HashMap<String,JSONObject> teacherMap = (HashMap<String, JSONObject>) ses.getAttribute("teacherMap");
			if(prepDataMap==null){
				try {
					throw new Exception("prepDataMap为空");
				} catch (Exception e) {
					logger.info("prepDataMap为空");
					e.printStackTrace();
					toFront.put("code", -50);
			        data.put("progress", 100);
			        data.put("msg", "redis缓存失败。");
			        toFront.put("data", data);
			        delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			        progressMap.put(processId, toFront);
				}
			}
			//progNum = 6;
			JSONArray wrongMsg = new JSONArray();
			JSONArray exDatas = new JSONArray();
			JSONObject preData = prepDataMap.get(processId);

			JSONObject venueNameMap =preData.getJSONObject("venueNameMap");  //种类_名称  ， 名称 
			if(venueNameMap==null){
				venueNameMap=new JSONObject();
			}
			
			
			String[] impHead = (String[]) preData.get("impHead");		
			progressMap.put(processId, toFront);
			Object keyProgressMap = "venueManage."+schoolId+processId+".00.progressMap";
			try {
				redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.info("progressMap获取redis失败！...");
				e.printStackTrace();
			    toFront.put("code", -50);
		        data.put("progress", 100);
		        data.put("msg", "redis缓存失败。");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			}

			HashMap<Integer, JSONObject> rowcolMap= new HashMap<Integer, JSONObject>(); //待插入的数据保存
			if (preData.containsKey("rowcolMap")) 
			{
				rowcolMap = (HashMap<Integer, JSONObject>)preData.get("rowcolMap");
			} 
			else 
			{
				preData.put("rowcolMap", rowcolMap);
			}
			
			int index = 0;
			boolean isVenueNameRepeat=false;
			
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
			{
				int rowNum = it.next();
				JSONObject pd = pureDatas.get(rowNum);
				String venueName = pd.containsKey("venueName") ? pd.getString("venueName"):null;
				String venueType = pd.containsKey("venueType") ? pd.getString("venueType"): null;
				if (map.get(venueType+"_"+venueName) == null) {
					map.put(venueType+"_"+venueName, 1);
				}else {
					map.put(venueType+"_"+venueName, map.get(venueType+"_"+venueName) + 1);
				}
			}
			
			for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
			{
				index++;
				boolean isWsg=false;
				int rowNum = it.next();
				JSONObject pd = pureDatas.get(rowNum);
				JSONObject wmsg = new JSONObject();
				wmsg.put("row", rowNum);
				JSONArray mrows = new JSONArray();
				wmsg.put("mrows", mrows);
				
				// excel必输入字段验证
				String venueName = pd.containsKey("venueName") ? pd.getString("venueName"):null;
				String venueType = pd.containsKey("venueType") ? pd.getString("venueType"): null;
				String teacherNames = pd.containsKey("teacherNames") ? pd.getString("teacherNames"):null;
				
				String equipmentTeacherNames = pd.containsKey("equipmentTeacherNames") ? pd.getString("equipmentTeacherNames"):null;
				String venueNum =  pd.containsKey("venueNum") ? pd.getString("venueNum"):null;
				//excel验证venueType数据合理性
				String 	title=pd.getString("venueTypeName");
				if (!(venueType != null && venueType.trim().length() > 0)) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", venueType);
					wsg.put("err", "不能为空！");
					mrows.add(wsg);
					isWsg=true;
				}

				// excel验证场馆名称不能重复，不能为空
				title=pd.getString("venueNameName");
				if (venueName != null && venueName.trim().length() > 0) {
					Integer cnt = map.get(venueType+"_"+venueName);
					if (cnt !=null && cnt > 1) {
						JSONObject wsg = new JSONObject();
						wsg.put("title", title);
						wsg.put("oldValue", venueName);
						wsg.put("err", "excel中重复！");
						isVenueNameRepeat = true;
						isWsg=true;
						mrows.add(wsg);
					}
					venueNameMap.put(venueType+"_"+venueName, venueName);
				} else {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", venueName);
					wsg.put("err", "不能为空！");
					isWsg=true;
					mrows.add(wsg);
				}
				
				
				//excel验证teacherNames数据合理性
				title=pd.getString("teacherNamesName");
				if (teacherNames != null && teacherNames.trim().length() > 0) {
					String[] ids=teacherNames.split(",");
					JSONObject tobj=new JSONObject();
					for (String s : ids)
					{		
						s=s.trim();
						if(tobj.containsKey(s))
						{
							JSONObject wsg = new JSONObject();
							wsg.put("title", title);
							wsg.put("oldValue", teacherNames);
							wsg.put("err",  "输入值不合法！");
							isWsg=true;
							mrows.add(wsg);
							break;
						}
						else
						{
							tobj.put(s, s);
							if(teacherMap!=null){
								if ( !teacherMap.containsKey(s)) {
									JSONObject wsg = new JSONObject();
									wsg.put("title", title);
									wsg.put("oldValue", teacherNames);
									wsg.put("err",  "无匹配记录！");
									mrows.add(wsg);
									isWsg=true;
									break;
								} else {
									JSONObject sysObj = teacherMap.get(s);
									int count = sysObj.getIntValue("count");
									if (count >1) {
										JSONObject wsg = new JSONObject();
										wsg.put("title", title);
										wsg.put("oldValue", teacherNames);
										wsg.put("err",  "匹配到多条记录！");
										mrows.add(wsg);
										isWsg=true;
										break;
									}
								}
							}
						}							
					}	
				} else {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", teacherNames);
					wsg.put("err", "不能为空！");
					isWsg=true;
					mrows.add(wsg);
				}
				
				title=pd.getString("equipmentTeacherNamesName");
				if(StringUtils.isNotBlank(equipmentTeacherNames)){// 判断非必须的 设备管理员数据是否合法
					String[] ids=equipmentTeacherNames.split(",");
					JSONObject tobj=new JSONObject();
					for (String s : ids)
					{		
						s=s.trim();
						if(tobj.containsKey(s))
						{
							JSONObject wsg = new JSONObject();
							wsg.put("title", title);
							wsg.put("oldValue", equipmentTeacherNames);
							wsg.put("err",  "输入值不合法！");
							isWsg=true;
							mrows.add(wsg);
							break;
						}
						else
						{
							tobj.put(s, s);
							if(teacherMap!=null){
								if ( !teacherMap.containsKey(s)) {
									JSONObject wsg = new JSONObject();
									wsg.put("title", title);
									wsg.put("oldValue", equipmentTeacherNames);
									wsg.put("err",  "无匹配记录！");
									mrows.add(wsg);
									isWsg=true;
									break;
								} else {
									JSONObject sysObj = teacherMap.get(s);
									int count = sysObj.getIntValue("count");
									if (count >1) {
										JSONObject wsg = new JSONObject();
										wsg.put("title", title);
										wsg.put("oldValue", equipmentTeacherNames);
										wsg.put("err",  "匹配到多条记录！");
										mrows.add(wsg);
										isWsg=true;
										break;
									}
								}
							}
						}							
					}	
					
				}
				
		 
				String err = "";			
				boolean noRecord = false;
				// 非必输字段	（不需判断空）		
					for (int j = 0; j < impHead.length; j++) 
					{					
						if(impHead[j]==null){
							continue;
						}
						String tit = impHead[j];
						String val = pd.containsKey(tit) ? pd.getString(tit):"" ;
						String titName = pd.getString(tit + "Name");
						JSONObject wsg = new JSONObject();
						wsg.put("title", titName);
						wsg.put("titleEnName", tit);
						wsg.put("oldValue", val);

					    if (tit.equalsIgnoreCase("isNeedExam")){
							val = val.replaceAll(" ", "");
							if(StringUtils.isNotEmpty(val))
							{
								if (!"是".equals(val)&&!"否".equals(val)) {
									noRecord=true;
									err= "输入值不合法！";
									isWsg=true;
								}
							}
						}else if (tit.equalsIgnoreCase("venueNum")){
							val = val.replaceAll(" ", "");
							if(StringUtils.isNotEmpty(val))
							{
								if (!StringNumTool.isInteger(val)||Integer.parseInt(val)<=0) {
									noRecord=true;
									err= "输入值不合法！";
									isWsg=true;
								}
							}
						}
				
						if (err.length() > 0) {
							wsg.put("err", err);
							mrows.add(wsg);
							err = "";
						} else {
							wsg = null;
						}
					} //end of for

				
				if (mrows.size() > 0) 
				{
					if(isVenueNameRepeat ||isWsg)
					{
						venueNameMap.remove(venueType+"_"+venueName);
					}				
					wrongMsg.add(wmsg);
				} 
				else 
				{
					wmsg = null;
				}
				
				if(mrows.size()==0&&err.length()==0&&!noRecord)
				{
					rowcolMap.put(rowNum, pd);//将该行数据存入
				}
				data.put("progress", 10+(20*(index/pureDatas.keySet().size())));
			}//end of for
			
			
			 
			
			preData.put("rowcolMap", rowcolMap);
			preData.put("venueNameMap", venueNameMap);
			if (wrongMsg != null && wrongMsg.size() > 0)
			{
				rs.put("ckRs", false);
				rs.put("wrongMsg", wrongMsg);
			} 
			else
			{
				rs.put("ckRs", true);
			}
			rs.put("exDatas", exDatas);
			try {
				redisOperationDAO.set(keyPrepDataMap, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.info("redis缓存prepDataMap出现问题！...");
				e.printStackTrace();
			    toFront.put("code", -50);
		        data.put("progress", 100);
		        data.put("msg", "redis缓存失败。");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			}
			return rs;
		
		}
		/**
		 * 继续导入任务启动接口
		 * 
		 * @param req
		 * @param res
		 * @return  
		 * @author zhh
		 */
		@RequestMapping(value = "/continueImport")
		@ResponseBody
		public synchronized JSONObject continueImport(HttpServletRequest req,
				HttpServletResponse res) {
			String processId = req.getSession().getId();
			User u = (User) req.getSession().getAttribute("user");
			String keyId = (String) req.getSession().getAttribute("keyId");
			String accountId=u.getAccountPart().getId()+"";
			int code = 1;
			String schoolId= getXxdm(req);
			String msg = "正常启动！";
			JSONObject obj = new JSONObject();
			try {

				SubProcess sp = new SubProcess(processId, 1, null,getXxdm(req),req.getSession());
				sp.start();
			} catch (Exception e) {
				logger.error("continueImport", e);
				code = -1;
				msg = "启动进程异常！";
				  delFileDBAndFileServer(schoolId,"venueManage_"+keyId);

			}

			obj.put("code", code);
			JSONObject data = new JSONObject();
			data.put("msg", msg);
			obj.put("data", data);
			logger.debug("继续导入-主线程结束！");
			return obj;

		}

		/**
		 * 单条修改验证
		 * 
		 * @param req
		 * @param res
		 * @return
		 * @author zhh
		 */
		@SuppressWarnings("unchecked")
		@RequestMapping(value = "/singleDataCheck")
		@ResponseBody
		public synchronized JSONObject singleDataCheck(HttpServletRequest req,
				@RequestBody JSONObject post, HttpServletResponse res) {
			JSONObject rs = new JSONObject();
			String schoolId = getXxdm(req);
			User u = (User) req.getSession().getAttribute("user");
			String keyId = (String) req.getSession().getAttribute("keyId");
			String accountId=u.getAccountPart().getId()+"";
			String processId = req.getSession().getId();
			int row = post.getIntValue("row");
			JSONArray mrows = post.getJSONArray("mrows");
			int code = post.getIntValue("code");
			rs.put("rowNum", row);
			Object keyPrepDataMap = "venueManage."+getXxdm(req)+processId+".00.prepDataMap";
			Object keyProgressMap = "venueManage."+getXxdm(req)+processId+".00.progressMap";
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			try {
				prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
				progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
			} catch (Exception e) {
				logger.info("redis获取prepDataMap/progressMap失败！");
				e.printStackTrace();
				rs.put("code", -50);
				JSONObject data = new JSONObject();
				data.put("msg", "redis获取数据异常..");
				rs.put("data", data);
				  delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			}
			if(prepDataMap!=null && progressMap !=null){
				HashMap<Integer, JSONObject> rowDatas = (HashMap<Integer, JSONObject>) prepDataMap.get(processId).get("rowDatas");		
				HashMap<String,String> titleEnMap = (HashMap<String, String>) prepDataMap.get(processId).get("titleEnMap");
				// -1忽略 1保存
				if (code == -1) 
				{
					rowDatas.remove(row);
					rs.put("code", 1);
				} 
				else if (code == 1)
				{
					ImportTaskParameter sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
	//				String impFrc = tempFileMap.get(processId);
					int isMatch = sp.getIsMatch();
					JSONArray mrs = sp.getMatchResult();
					HashMap<Integer, JSONObject> pureDatas = new HashMap<Integer, JSONObject>();
					JSONObject sd = rowDatas.get(row);
					for (int i = 0; i < mrows.size(); i++) 
					{
						JSONObject o = mrows.getJSONObject(i);
						sd.put(titleEnMap.get(o.getString("title")), o.getString("value"));
					}
	
					pureDatas.put(row, sd);
					JSONObject cr = checkImpData(pureDatas, mrs, isMatch, processId, sp,req.getSession(),progressMap);
					
					if (cr.getBooleanValue("ckRs")) {
						rs.put("code", 1);
						JSONObject data = new JSONObject();
						data.put("msg", "校验通过！");
						rs.put("data", data);
					} else {
						rs.put("code", -1);
						JSONObject data = new JSONObject();
						data.put("msg", "校验不通过！");
						rs.put("data", data);
						rs.put("mrows", cr.getJSONArray("wrongMsg").getJSONObject(0)
								.get("mrows"));
					}
				}
			}
			
			return rs;
		}

		/**
		 * 获取导入进度
		 * 
		 * @return
		 * @author zhh
		 */
		@RequestMapping(value = "/importProgress")
		@ResponseBody
		public JSONObject importProgress(HttpServletRequest req,
				HttpServletResponse res) {
			JSONObject rs = new JSONObject();
			String schoolId = getXxdm(req);
			User u = (User) req.getSession().getAttribute("user");
			String keyId = (String) req.getSession().getAttribute("keyId");
			String processId=req.getSession().getId();
			String accountId=u.getAccountPart().getId()+"";
			 Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			 Object keyProgressMap = "venueManage."+getXxdm(req)+processId+".00.progressMap";
			   try {
					progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
				} catch (Exception e2) {
					logger.info("keyProgressMap获取redis失败！...");
					e2.printStackTrace();
					rs.put("code",-50);
					rs.put("data", "redis获取数据失败...");
					  delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
			
				}
				if(progressMap!=null){
					JSONObject obj = progressMap.get(req.getSession().getId());
					if(progressMap!=null&&obj!=null){			
						rs.put("code", obj.get("code"));
						rs.put("data", obj.get("data"));
					}else{
						rs.put("code",-100);
						rs.put("data", "用户身份信息已失效，请重新登陆！");
						
					}
				}
			return rs;
		}
		/**
		 * 设置公共参数，教师，场馆类别
		 * @param req
		 * @author zhh
		 */
		private int setAllCommonInfo(HttpServletRequest req,String keyId)
		{
			School school =this.getSchool(req,null); 
			long schoolId=school.getId();
			int code=1;
	        // 获取教师列表
	        List<Account> accoutList = allCommonDataService.getAllSchoolEmployees(school, getCurXnxq(req), "");
	        String [] teacherName = new String[accoutList.size()];
	        HashMap<String,JSONObject> teacherMap = new HashMap<String,JSONObject>();
	    	for(int i=0; i < accoutList.size(); i++){
	    		String key = accoutList.get(i).getName();
	            teacherName[i] = key;
	            Account account=accoutList.get(i);
	            JSONObject obj=new JSONObject();
	            obj.put("teacherId", account.getId());
	            obj.put("teacherName", account.getName());
	        	if(teacherMap.containsKey(key)){
	        		JSONObject obj1 = teacherMap.get(key);
	        		int count = obj1.getIntValue("count");
	        		count ++;
	        		obj1.put("count", count);
	        				
	        	}else{
	        		obj.put("count", 1);
	        		teacherMap.put(key, obj);
	        	}
	    	}
	    	
	    	req.getSession().setAttribute("teacherName", teacherName);
	    	req.getSession().setAttribute("teacherMap", teacherMap);
	    	//获取场馆类别
	    	JSONObject param = new JSONObject();
	    	HashMap<String,JSONObject> venueTypeMap = new HashMap<String,JSONObject>();
	    	param.put("schoolId",schoolId );
	    	List<JSONObject> venueTypeList=null;
			try {
				venueTypeList = venueManageImportService.getAllVenueTypeList(param);
			} catch (Exception e1) {
				e1.printStackTrace();
				code=-50;
				  delFileDBAndFileServer(schoolId+"","venueManage_"+keyId);
			}
	    	if(CollectionUtils.isNotEmpty(venueTypeList)){
	    	    for(JSONObject venueType : venueTypeList){
	    	    	venueTypeMap.put(venueType.getString("venueType"), venueType);
	    	    }
	    	}
	    	
	         req.getSession().setAttribute("venueTypeMap", venueTypeMap);
	    	//获取场馆类别、场馆名称
	        HashMap<String, String> venueSetAndTypeMap = new HashMap<String, String>();
	    	try {
				List<JSONObject> data=venueManageSetService.getVenueSetList(param);
				for(JSONObject set:data){
					String venueType = set.getString("venueType");
					String venueName = set.getString("venueName");
					String setId = set.getString("setId");
					venueSetAndTypeMap.put(venueType+"_"+venueName, setId);
				}
				req.getSession().setAttribute("venueSetAndTypeMap", venueSetAndTypeMap);
	    	} catch (Exception e) {
	    		logger.info("venueSet and venueType 设置或者缓存redis失败..");
				e.printStackTrace();
				code=-50;
				  delFileDBAndFileServer(schoolId+"","venueManage_"+keyId);
			}
	    	return code;
		}
		/**
		 * 根据参数生成需插入数据
		 * 
		 * @param sp
		 * @param processId
		 * @return
		 * @author zhh
		 */
	@SuppressWarnings("unchecked")
	private List<JSONObject> getInsertEntityByCkrs(ImportTaskParameter sp, String processId,HttpSession ses,Hashtable<String, JSONObject> progressMap) {
		String schoolId=sp.getSchoolId();
		String keyId = (String) ses.getAttribute("keyId");
		List<JSONObject> param = new ArrayList<JSONObject>();
		Object keyPrepDataMap = "venueManage."+sp.getSchoolId()+processId+".00.prepDataMap";
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		try {
			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
		} catch (Exception e) {
			logger.info("redis获取prepDataMap失败！");
			e.printStackTrace();
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			toFront.put("code", -50);
            data.put("progress", 100);
            data.put("msg", "redis缓存失败。");
            toFront.put("data", data);
            progressMap.put(processId, toFront);
            delFileDBAndFileServer(schoolId,"venueManage_"+keyId);
		}
		if(prepDataMap!=null){
			HashMap<Integer, JSONObject> rows = (HashMap<Integer, JSONObject>) prepDataMap
					.get(processId).get("rowcolMap");
			List<String> delVenueSetList=new ArrayList<String>();//和系统场馆重名的则覆盖	
		
			HashMap<String, String> venueSetAndTypeMap = null;
			HashMap<String,JSONObject> teacherMap = null;
			teacherMap = (HashMap<String, JSONObject>) ses.getAttribute("teacherMap");
			venueSetAndTypeMap=(HashMap<String, String>) ses.getAttribute("venueSetAndTypeMap");
			if(venueSetAndTypeMap!=null && teacherMap!=null){
				for (Iterator<Integer> it = rows.keySet().iterator(); it.hasNext();) {
					int key = it.next();
					JSONObject row = rows.get(key);
					String venueType=row.getString("venueType");
					String venueName=row.getString("venueName");
					String setId=venueSetAndTypeMap.get(venueType+"_"+venueName);
					if(StringUtils.isNotBlank(setId)&& setId!=null){
						row.put("setId", setId);
					}else{
						row.put("setId","");
					}
					row.put("schoolId", schoolId);
					String isNeedExam=row.getString("isNeedExam");
					List<JSONObject> teachers=new ArrayList<JSONObject>();
					String teacherNames = row.getString("teacherNames");
					List<String> teacherNameList = Arrays.asList(teacherNames.split(","));
					for(int j=0;j<teacherNameList.size();j++){
						JSONObject teacherObj = teacherMap.get(teacherNameList.get(j));
						teachers.add(teacherObj);
					}
					row.put("teachers",teachers );
					//2017-04-27-lime-s
					String equipmentTeacherNames = row.getString("equipmentTeacherNames");
					if (StringUtils.isNotBlank(equipmentTeacherNames)) {
						List<JSONObject> equipmentTeachers=new ArrayList<JSONObject>();
						List<String> equipmentteacherNameList = Arrays.asList(equipmentTeacherNames.split(","));
						for(int j=0;j<equipmentteacherNameList.size();j++){
							JSONObject teacherObj = teacherMap.get(equipmentteacherNameList.get(j));
							equipmentTeachers.add(teacherObj);
						}
						row.put("equipmentTeachers",equipmentTeachers );
					}
					
					
					//2017-04-27-lime-e
					if("是".equals(isNeedExam)){
						row.put("isNeedExam", 1);
					}else{
						row.put("isNeedExam", 2);
					}
					param.add(row);
				}
			}
		}
		return param;
	}
}
