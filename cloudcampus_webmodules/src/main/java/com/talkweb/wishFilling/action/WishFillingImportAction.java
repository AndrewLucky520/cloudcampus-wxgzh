package com.talkweb.wishFilling.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.ExcelTool;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.commondata.util.NameRepeatJudge;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.wishFilling.service.WishFillingImportService;
import com.talkweb.wishFilling.service.WishFillingService;
import com.talkweb.wishFilling.util.ImportTaskParameter;


/** 
 * 新高考志愿填报-导入接口
 * @author  作者 E-mail:zhanghuihui15222@talkweb.com.cn
 * @date 创建时间：2016年9月10日 上午11:00:07 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 * @version 2.0 2016年11月3日  author：zhh
 */
@Controller
@RequestMapping(value = "/wishFilling/import/")
public class WishFillingImportAction extends BaseAction{
	 @Autowired
	    private AllCommonDataService allCommonDataService;
	    @Autowired
	    private WishFillingImportService wishFillingImportService;
	    @Autowired
	    private WishFillingService wishFillingService;
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
		
		private static final Logger logger = LoggerFactory.getLogger(WishFillingImportAction.class);
		
		private void setPromptMessage(JSONObject object, String code, String message) {
			object.put("code", code);
			object.put("msg", message);
		}
		/**
		 * 删除文件数据库中的文件及文件服务器的文件
		 * @param schoolId
		 * @param keyId
		 */
		private int delFileDBAndFileServer(String schoolId,String keyId,String sessionId){
			String fId=fileImportInfoService.getFileBy(schoolId, keyId);
			fileImportInfoService.deleteFile(schoolId, keyId);	
			try {
				fileServerImplFastDFS.deleteFile(fId);
				deleteRedisAndSessionContent(sessionId);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
			return 1;
		}
		//清除相关redis和session的信息
		private void deleteRedisAndSessionContent(String sessionId) throws Exception{
			String excelNameMapKey="wf."+sessionId+".excelNameMap";
			String nameRepeatMapKey="wf."+sessionId+".nameRepeatMap";
			redisOperationDAO.del(nameRepeatMapKey);
			redisOperationDAO.del(excelNameMapKey);
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
 * 四、导入学生模板
 */
		/**
		 * （1）获取下载模板
		 * @param req
		 * @param request
		 * @param res
		 * @author zhh
		 */
		@RequestMapping(value = "/downloadExcelTemplate", method = RequestMethod.POST)
		@ResponseBody
		public JSONObject downloadExcelTemplate(HttpServletRequest req,
				 HttpServletResponse res) {
			JSONObject response = new JSONObject();
			JSONObject param = new JSONObject();
			try {
				String schoolId = getXxdm(req);
				String wfId = req.getParameter("wfId");
				String termInfo = req.getParameter("termInfo");
				param.put("schoolId", schoolId);
				param.put("wfId", wfId);
				param.put("termInfo", termInfo);
				School school = getSchool(req, termInfo);
				String areaCode = school.getAreaCode()+"";
				param.put("areaCode", areaCode);
				JSONObject tbObj = wishFillingService.getTb(param);
				String isByElection = tbObj.getString("isByElection");
				String subjectIds = tbObj.getString("subjectIds");
				List<Long> subList = StringUtil.toListFromString(subjectIds);
				/*if("1".equals(isByElection)){ //如果是补选则只取补选的数据
					param.put("isGetBySubject", 1);
					JSONObject wfObj1 = wishFillingService.getTb(param);
					subjectIds = wfObj1.getString("subjectIds");
					subList = StringUtil.toListFromString(subjectIds);
					param.remove("isGetBySubject");
				}*/
				List<JSONObject> llList = wishFillingService.getDicSubjectList(schoolId,areaCode,tbObj.getString("pycc"),"0");
				Map<Long,String> lessonNameMap = new HashMap<Long,String>();
				if(llList!=null){
					for(JSONObject l:llList){
						lessonNameMap.put(l.getLong("subjectId"), l.getString("subjectName"));
					}
				}
				JSONArray excelHeads = new JSONArray();
				JSONArray line = new JSONArray();
				JSONObject col = new JSONObject();
				col.put("field", "className");
				col.put("title", "班级");
				line.add(col);
				
				col = new JSONObject();
				col.put("field", "name");
				col.put("title", "姓名");
				line.add(col);
			
				
				for(JSONObject obj:llList){
					Long Id = obj.getLong("subjectId");
					String name=lessonNameMap.get(Id);
					col = new JSONObject();
					col.put("field", Id);
					col.put("title", name);
					line.add(col);
				}
				excelHeads.add(line);
				
				JSONArray excelData = new JSONArray();
				JSONObject row = new JSONObject();
				excelData.add(row);
				ExcelTool.exportExcelWithData(excelData, excelHeads, "选科导入模板", null, req,res);
				setPromptMessage(response, "0", "操作成功");
			} catch (Exception e) {
				setPromptMessage(response, "-1", "操作失败");
				e.printStackTrace();
			}
			return response;
		}
		
		/**
		 * （2）上传文件
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
			JSONObject param = new JSONObject();
			File df = null;
		    String[] stuTitle = null;
		    String[] stuTitleName = null;
		    int[] stuTitleNeed = null;
		    String schoolId = getXxdm(req);
		    String wfId = req.getParameter("wfId");
		    String termInfo = req.getParameter("termInfo");
		    param.put("schoolId", schoolId);
		    param.put("wfId", wfId);
		    param.put("termInfo", termInfo);
		    School school = getSchool(req, termInfo);
			String areaCode = school.getAreaCode()+"";
			param.put("areaCode", areaCode);
			List<JSONObject> llList = new ArrayList<JSONObject>();
			JSONObject tbObj = new JSONObject();
			String processId = req.getSession().getId();
			Object keyPycc = "wf."+getXxdm(req)+processId+".00.pycc";
		    try {
		        tbObj = wishFillingService.getTb(param); 
		        String pycc = tbObj.getString("pycc");
		        redisOperationDAO.set(keyPycc, pycc);
				llList = wishFillingService.getDicSubjectList(schoolId+"",areaCode,tbObj.getString("pycc"),"0"); //获取所有科目
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		    if(llList==null){
		    	llList = new ArrayList<JSONObject>();
		    }
		    String isByElection = tbObj.getString("isByElection");
		    String subjectIds = tbObj.getString("subjectIds"); //获取已设置的科目
		    List<Long> subList = StringUtil.toListFromString(subjectIds);
		   /* if("1".equals(isByElection)){ //如果是补选则只取补选的数据
				try {
					param.put("isGetBySubject", 1);
					JSONObject wfObj1 = new JSONObject();
					wfObj1 = wishFillingService.getTb(param);
					subjectIds = wfObj1.getString("subjectIds");
					subList = StringUtil.toListFromString(subjectIds);
					param.remove("isGetBySubject");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
		   /* Map<Long,JSONObject> subMap = new HashMap<Long, JSONObject>();
		    for(JSONObject l:lList){
		    	Long subjectId = l.getLong("subjectId");
		    	subMap.put(subjectId, l);
		    }
		    List<JSONObject> llList = new ArrayList<JSONObject>();
		    for(Long id:subList){
		    	JSONObject subject = subMap.get(id);
		    	if(subject!=null){
		    		llList.add(subject);
		    	}
		    }*/
			logger.debug("导入sessionID:"+req.getSession().getId());
			logger.debug("获取学校代码："+getXxdm(req));
			
			if (stuTitle == null) {
				stuTitle = new String[2+llList.size()];
				stuTitle[0] = "班级";
				stuTitle[1] = "姓名";
				
				stuTitleName = new String[stuTitle.length];
				stuTitleName[0] = "className";
				stuTitleName[1] = "name";
			
				stuTitleNeed = new int[stuTitle.length];
				stuTitleNeed[0] = 1;
				stuTitleNeed[1] = 1;
				
			    int count =2;
				for(JSONObject l:llList){
					stuTitle[count]=l.getString("subjectName");
					stuTitleName[count]=l.getString("subjectId");
					stuTitleNeed[count]=0;
					count++;
				}

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
				fileImportInfoService.addFile(schoolId,"wf_"+s, fileId);
				
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
						excelVal = excelVal.replaceAll("（", "(");
						excelVal = excelVal.replaceAll("）", ")");
						if (!isInArray(excelVal, stuTitle)) {
							code = 2;
							msg = "文件格式正确,字段需要匹配";
						}
						// 放置临时保存目录
						tempStuImpExcTitle[i] = excelVal;
					}
					//判断2个必填项标识是否存在
					if (code == 1) {
						if (isInArray("班级", tempStuImpExcTitle)&&isInArray("姓名", tempStuImpExcTitle)) {
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
				delFileDBAndFileServer(schoolId,"wf_"+s,req.getSession().getId());
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
		 * （3）获取Excel表头和系统字段
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
		 * （4）导入任务启动接口
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
			String accountId=u.getAccountPart().getId()+"";
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			String schoolId=this.getXxdm(req);
			String keyId = (String) req.getSession().getAttribute("keyId");
			//获取学年学期
			String wfId =mappost.getString("wfId");
			String termInfo =mappost.getString("termInfo");
			String schoolYear = termInfo.substring(0, 4);
			JSONObject param = new JSONObject();
			param.put("schoolId", schoolId);
			param.put("wfId", wfId);
			param.put("termInfo", termInfo);
			School school = getSchool(req, termInfo);
		    String areaCode = school.getAreaCode()+"";
		    param.put("areaCode", areaCode);
			JSONObject tbObj = new JSONObject();
			try {
				tbObj = wishFillingService.getTb(param);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				delFileDBAndFileServer(schoolId,"wf_"+keyId,req.getSession().getId());
				e1.printStackTrace();
			}
			String subjectIds = tbObj.getString("subjectIds");
			int wfNum = tbObj.getInteger("wfNum");
			String isByElection = tbObj.getString("isByElection");
			/*if("1".equals(isByElection)){ //如果是补选则只取补选的数据
				try {
					param.put("isGetBySubject", 1);
					JSONObject wfObj1 = new JSONObject();
					wfObj1 = wishFillingService.getTb(param);
					subjectIds = wfObj1.getString("subjectIds");
					param.remove("isGetBySubject");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			String wfWay = tbObj.getString("wfWay");
			String useGrade = tbObj.getString("wfGradeId");
			String currentLevel = allCommonDataService.ConvertSYNJ2NJDM(useGrade, schoolYear);
			Grade g = allCommonDataService.getGradeByGradeLevel(Long.parseLong(schoolId), T_GradeLevel.findByValue(Integer.parseInt(currentLevel)), termInfo);
			JSONObject obj = new JSONObject();
			String processId = req.getSession().getId();
			String excelNameMapKey="wf."+processId+".excelNameMap";
			String nameRepeatMapKey="wf."+processId+".nameRepeatMap";
			try {
				redisOperationDAO.del(excelNameMapKey);
				redisOperationDAO.del(nameRepeatMapKey);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("progressMap缓存redis失败！...");
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
				return obj;
			}
			
			stt.setGradeId(g.getId());
			stt.setWfNum(wfNum);
			stt.setIsMatch(mappost.getIntValue("isMatch"));
			if(stt.getIsMatch()==1)
			{
				stt.setMatchResult(mappost.getJSONArray("matchResult"));
			}
			stt.setWfWay(wfWay);
			stt.setIsByElection(isByElection);
			stt.setSchoolId(schoolId);
			stt.setAccountId(accountId);
			stt.setTermInfo(termInfo);
			stt.setWfId(wfId);
			stt.setSubjectIds(subjectIds);
			stt.setSchool(school);
			stt.setAreaCode(areaCode);
			// 设置获取的参数
			
			JSONObject procObj = new JSONObject();
			procObj.put("taskParam", stt);
			progressMap.put(processId, procObj);
			Object keyProgressMap = "wf."+getXxdm(req)+processId+".00.progressMap";
			
	        obj.put("code", 0);
	        obj.put("msg", "正常启动任务");
			try {
				redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			} catch (Exception e) {
				logger.info("progressMap缓存redis失败！...");
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				e.printStackTrace();
				obj.put("code", -50); //code=-50为redis读取异常
		        obj.put("msg", "出现异常，导入失败！");
				return obj;
			}
			
			SubProcess sp = new SubProcess(processId, 0, null,schoolId,req.getSession(),null);
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
				e.printStackTrace();
				delFileDBAndFileServer(schoolId,"wf_"+keyId,req.getSession().getId());
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
			private Object isSetFlag;
			/**
			 * 请求的req
			 */
			private HttpSession ses;

			private JSONObject singleData = null;
			
			private Hashtable<String, JSONObject> progressMap = null;

			public SubProcess(String processId, int impType, JSONObject singleData,String schoolId,HttpSession ses,Object isSetFlag) {
				this.processId = processId;
				this.impType = impType;
				this.singleData = singleData;
				this.schoolId= schoolId;
				this.ses= ses;
				this.isSetFlag=isSetFlag;
			}

			@Override
			public void run() {
				long t1 = (new Date()).getTime();
			    
			    Object keyProgressMap = "wf."+schoolId+processId+".00.progressMap";
			    String keyId = (String) ses.getAttribute("keyId");
			    try {
					progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
				} catch (Exception e2) {
					logger.info("keyProgressMap获取redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
						delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
						JSONObject toFront = progressMap.get(processId);
						JSONObject data = new JSONObject();
						toFront.put("code", -50);
			            data.put("progress", 100);
			            data.put("msg", "redis缓存失败。");
			            toFront.put("data", data);
			            progressMap.put(processId, toFront);
					}
				}
			 
			 //excel导入处理开始
				ImportTaskParameter sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
				
				String fId=(String) ses.getAttribute("fileId");
				if(StringUtils.isNotBlank(fId)){
					 
					int isMatch = sp.getIsMatch();  
					JSONArray mrs = sp.getMatchResult();

					JSONObject toFront = progressMap.get(processId);
					 //设置公共参数/
					JSONObject data = new JSONObject();
					int commonCode=1;
					if(isSetFlag==null){
					    commonCode=this.setAllCommonInfo(sp.getTermInfo(),sp.getSubjectIds(),sp.getWfId(),sp.getSchool(),keyId,sp.getGradeId(),sp.getIsByElection(),sp.getWfWay());
					}
					
					if(commonCode<0){
						JSONObject obj = new JSONObject();
						obj.put("code", -50); //code=-50为redis读取异常
				        obj.put("msg", "出现异常，导入失败！");
				        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				        toFront.put("code", -50);
			            data.put("progress", 100);
			            data.put("msg", "redis缓存失败。");
			            toFront.put("data", data);
			            progressMap.put(processId, toFront);
					}
					
					data.put("progress", 50);
					data.put("msg", "正在准备读取数据");
					toFront.put("data", data);
					toFront.put("code", 1);
					String[] ckExcTt = null;//excel表头
					JSONObject readRs = new JSONObject();
					readRs.put("code", 1);
					JSONArray datas = new JSONArray();
					int readCode=1;
					if (impType == 0) 
					{
						readRs = readExcelToData(fId,schoolId,keyId,processId);
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
								data.put("progress", 60);
								data.put("msg", "正在校验excel数据");
								toFront.put("data", data);
								progressMap.put(processId, toFront);
								try {
									redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
								} catch (Exception e) {
									logger.info("progressMap缓存redis失败！...");
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
									toFront.put("code", -50);
						            data.put("progress", 100);
						            data.put("msg", "redis缓存失败。");
						            toFront.put("data", data);
						            progressMap.put(processId, toFront);
								}
								JSONObject preDatas = changeData(datas, mrs, isMatch,processId,schoolId,ses,progressMap);
								HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) preDatas.get("rowDatas");
								String excelNameMapKey="wf."+processId+".excelNameMap";
								String nameRepeatMapKey="wf."+processId+".nameRepeatMap";
								String classIdNameMapKey = "wf."+schoolId+processId+".00.classIdNameMap";
								Map<String,String> classIdNameMap = (Map<String, String>) redisOperationDAO.get(classIdNameMapKey);
								Map<String,String> nameRepeatMap = (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
								Map<String,String> excelNameMap = (Map<String, String>) redisOperationDAO.get(excelNameMapKey);
								JSONObject json = new JSONObject();
								json.put("pureDatas", pureDatas);
								List<JSONObject> titleNames = new ArrayList<JSONObject>();
								JSONObject titleName = new JSONObject();
								titleName.put("classTitleName","className" );
								titleName.put("titleName","name" );
								titleName.put("splitIndex","" );
								titleNames.add(titleName);
								json.put("titleNames", titleNames);
								String accountListKey="wf."+processId+".accountList";
								List<Account> accoutList = (List<Account>) redisOperationDAO.get(accountListKey);
								json.put("list", accoutList);
								json.put("roleType", "Student");
								json.put("plate", 1);
								json.put("nameRepeatMap", nameRepeatMap);
								json.put("excelType", "1");
								json.put("excelRule", "name");
								json.put("excelNameMap", excelNameMap);
								json.put("classIdNameMap", classIdNameMap);
								JSONObject returnRepeatName = NameRepeatJudge.judgeNameRepeatImport(json);
								JSONArray wrongMsg = new JSONArray();
								Map<String,String> stringMap = new HashMap<String,String>();
								if(returnRepeatName!=null){
									nameRepeatMap = (Map<String, String>) returnRepeatName.get("nameRepeatMap");
									wrongMsg = (JSONArray) returnRepeatName.get("wrongMsg");
									stringMap = (Map<String, String>) returnRepeatName.get("stringMap");
									excelNameMap= (Map<String, String>) returnRepeatName.get("excelNameMap");
								}
								String stringMapKey="wf."+processId+".stringMap";
							
								redisOperationDAO.set(excelNameMapKey, excelNameMap);
								redisOperationDAO.set(stringMapKey, stringMap);
								redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
								Object keyArr = "wf."+schoolId+processId+".00.arr";
								String [][] arr = (String [][]) redisOperationDAO.get(keyArr);
								ckrs = checkImpData(arr,stringMap,nameRepeatMap,wrongMsg,pureDatas, mrs, isMatch, processId, sp,ses,progressMap);
								redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
							}
							logger.debug("查询结果:" + ckrs);
							if (ckrs.getBooleanValue("ckRs") || impType == 1)
							{
								// 开始入库
								data.put("progress", 85);
								data.put("msg", "正在保存数据！");
								toFront.put("data", data);
								progressMap.put(processId, toFront);
								try {
									redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
								} catch (Exception e) {
									logger.info("progressMap缓存redis失败！...");
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
									toFront.put("code", -50);
						            data.put("progress", 100);
						            data.put("msg", "redis缓存失败。");
						            toFront.put("data", data);
						            progressMap.put(processId, toFront);
								}
								Map<String,Object> needInsert = getInsertEntityByCkrs(sp, processId,ses,progressMap);
								if (needInsert.size() >= 0) {
									int num=wishFillingImportService.addStudentTbBatch(needInsert);	
									if(num>0){
										toFront.put("code", 2);
							            data.put("progress", 100);
							            data.put("msg", "导入成功，共计导入"+ num +"条信息记录！");
									}else{
										toFront.put("code", -1);
							            data.put("progress", 100);
							            data.put("msg", "导入条数为零或系统异常，请刷新重新导入！");
							            
									}
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
						            progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										logger.info("progressMap缓存redis失败！...");
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
								delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
								progressMap.put(processId, toFront);
								try {
									redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
								} catch (Exception e) {
									logger.info("progressMap缓存redis失败！...");
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
								delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
								try {
									redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
								} catch (Exception e) {
									logger.info("progressMap缓存redis失败！...");
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
								delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
								try {
									redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
								} catch (Exception e) {
									logger.info("progressMap缓存redis失败！...");
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
					logger.error("run:", e);
					e.printStackTrace();
					delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
						delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				JSONObject toFront = progressMap.get(processId);
				JSONObject data = new JSONObject();
				toFront.put("code", -50);
	            data.put("progress", 100);
	            data.put("msg", "文件数据库为空！");
	            toFront.put("data", data);
	            progressMap.put(processId, toFront);
				
			}
			}
			/**
			 * 设置公共参数，学生，科目
			 * @param req
			 * @author zhh
			 */
			private int setAllCommonInfo(String termInfo,String subjectIds,String wfId,School school,String keyId,Long gradeId,String isByElection,String wfWay )
			{
				Object keyProgressMap = "wf."+schoolId+processId+".00.progressMap";
				JSONObject data = new JSONObject();
				JSONObject toFront = progressMap.get(processId);
				toFront.put("code", 1);
				data.put("progress", 10);
		        data.put("msg", "正在准备导入任务所需数据");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        try {
					redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e1) {
					logger.info("progressMap缓存redis失败！...");
					e1.printStackTrace();
					delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
				long schoolId=school.getId();
				int code=1;
				//获取年级
				Grade g = allCommonDataService.getGradeById(schoolId, gradeId, termInfo);
				List<Long> classIds = g.getClassIds();
				//获取班级
		    	List<Classroom> allClass=allCommonDataService.getClassroomBatch(schoolId,classIds, termInfo);
		    	HashMap<Long,Classroom> classMap = new HashMap<Long,Classroom>();
		    	HashMap<String,Classroom> classNameMap = new HashMap<String,Classroom>();
		    	HashMap<String,String> classIdNameMap = new HashMap<String,String>();
		        Set<Long> studentSet = new HashSet<Long>();
		    	for(Classroom c:allClass){
		        	if(StringUtils.isBlank(c.getClassName())){
		        		continue;
		        	}
		        	classNameMap.put(c.getClassName(), c);
		        	classMap.put(c.getId(), c);
		        	classIdNameMap.put(c.getId()+"", c.getClassName());
		        	if(c.getStudentAccountIds()!=null){
		        		studentSet.addAll(c.getStudentAccountIds());
		        	}
		        }
		    	//ses.setAttribute("classNameMap", classNameMap);
		    	String classNameMapKey = "wf."+schoolId+processId+".00.classNameMap";
		    	String classMapKey = "wf."+schoolId+processId+".00.classMap";
		    	String classIdNameMapKey = "wf."+schoolId+processId+".00.classIdNameMap";
		    	try {
		    		redisOperationDAO.set(classIdNameMapKey, classIdNameMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		    		redisOperationDAO.set(classNameMapKey, classNameMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(classMapKey, classMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e2) {
					logger.info("progressMap缓存redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		    	data.put("progress", 35);
		    	toFront.put("code", 1);
		        data.put("msg", "正在准备导入任务所需数据");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        try {
					redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e1) {
					logger.info("progressMap缓存redis失败！...");
					e1.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		        //获取学生列表
		        List<Account> accoutList = allCommonDataService.getAccountBatch(schoolId,new ArrayList<Long>(studentSet), termInfo);
		        HashMap<String,JSONObject> studentMap = new HashMap<String,JSONObject>();
		    	for(int i=0; i < accoutList.size(); i++){
		    		String key = "-1";
		    		Account account=accoutList.get(i);
		    		String name = account.getName();
		            
		            JSONObject obj=new JSONObject();
		            obj.put("studentId", account.getId());
		            obj.put("studentName", account.getName());
		            
		            List<User> users = account.getUsers();
		            for(User u:users){
		            	if(u.getUserPart()==null){
		            		continue;
		            	}
		            	if(u.getUserPart().role!=T_Role.Student || u.getStudentPart()==null){
		            		continue;
		            	}
		               Long classId = u.getStudentPart().getClassId();
		               Classroom c = classMap.get(classId);
		             
		               obj.put("classId", classId);
		               
		               JSONObject scObj = new JSONObject();
		               scObj.put("name", account.getName());
		               scObj.put("classId",classId );
		               scObj.put("className",c.getClassName() );
		             
		               key= name+"_"+classId;
		               break;
		            }
		            if(!"-1".equals(key)){
			        	if(studentMap.containsKey(key)){
			        		JSONObject obj1 = studentMap.get(key);
			        		int count = obj1.getIntValue("count");
			        		count ++;
			        		obj1.put("count", count);
			        				
			        	}else{
			        		obj.put("count", 1);
			        		studentMap.put(key, obj);
			        	}
		            }
		    	}
		    	String studentMapKey = "wf."+schoolId+processId+".00.studentMap";
		    	String accountListKey="wf."+processId+".accountList";
		     	try {
		     		redisOperationDAO.set(accountListKey, accoutList,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(studentMapKey, studentMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e2) {
					logger.info("progressMap缓存redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		    	data.put("progress", 40);
		    	toFront.put("code", 1);
		        data.put("msg", "正在准备导入任务所需数据");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        try {
					redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e1) {
					logger.info("progressMap缓存redis失败！...");
					e1.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		    	//处理科目
				List<JSONObject> lList = new ArrayList<JSONObject>();
				Object keyPycc = "wf."+schoolId+processId+".00.pycc";
				try {
					String pycc = (String) redisOperationDAO.get(keyPycc);
					lList = wishFillingService.getDicSubjectList(schoolId+"",school.getAreaCode()+"",pycc,"0");
				} catch (Exception e3) {
					e3.printStackTrace();
				}
				Map<Long,JSONObject> allLessonMap = new HashMap<Long,JSONObject>();
				Map<String,JSONObject> allLessonNameMap = new HashMap<String,JSONObject>();
				if(lList!=null){
					for(JSONObject l:lList){
						allLessonMap.put(l.getLong("subjectId"), l);
						allLessonNameMap.put(l.getString("subjectName"), l);
					}
				}
			    List<Long> subList = StringUtil.toListFromString(subjectIds);
			    Map<Long,JSONObject> lessonMap = new LinkedHashMap<Long,JSONObject>();
			    for(Long id:subList){
			    	JSONObject subject = allLessonMap.get(id);
			    	if(subject!=null){
			    		lessonMap.put(id, subject);
			    	}
			    }
				
		    	String lessonMapKey = "wf."+schoolId+processId+".00.lessonMap";
		    	try {
					redisOperationDAO.set(lessonMapKey, lessonMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e2) {
					logger.info("progressMap缓存redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
			   
		    	//年级
		    	List<Grade> gList = allCommonDataService.getGradeList(school, termInfo);
		    	HashMap<Long,Grade> gMap = new HashMap<Long,Grade>();
		    	for(Grade g1:gList){
		    		gMap.put(g1.getId(), g1);
		    	}
		    	String gMapKey = "wf."+schoolId+processId+".00.gMap";
		    	try {
					redisOperationDAO.set(gMapKey, gMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e2) {
					logger.info("progressMap缓存redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		    	
				//处理组合科目 //处理组合列表（获取所有组合（包括固定和不固定的）） //在补选的情况下，获取所有学生的选择情况zhstudent
		    	Map<String,String> zhSubjectMap = new HashMap<String,String>();//subjectIds-zhId
		    	JSONObject param = new JSONObject();
		    	param.put("schoolId", schoolId);
		    	param.put("wfId", wfId);
		    	param.put("termInfo", termInfo);
		    	param.put("areaCode", school.getAreaCode());
		    	List<JSONObject> zhSubjects = new ArrayList<JSONObject>();
		    	Map<String,JSONObject> subMap = new HashMap<String,JSONObject>(); 
		    	List<JSONObject> studentTbs = new ArrayList<JSONObject>();
		    	//accountId - isFixedZh
				Map<String,String> byZhStudentMap = new HashMap<String,String>();
		    	try {
					zhSubjects = wishFillingService.getZhSubject(param);
					for(JSONObject zh:zhSubjects){
						zhSubjectMap.put(zh.getString("subjectIds"), zh.getString("zhId"));
						subMap.put(zh.getString("zhId"), zh);
					}
					if("1".equals(isByElection)){
						List<JSONObject> byZhStudentList = wishFillingService.getByAllZhStudent(param);
						for(JSONObject byZhStudent:byZhStudentList){
							String isFixedZh = byZhStudent.getString("isFixedZh");
							String accountId = byZhStudent.getString("accountId");
							byZhStudentMap.put(accountId, isFixedZh); //学生在补选的情况下是否为正选选成功的人
						}
					}
		    	} catch (Exception e) {
					code=-1;
			        e.printStackTrace();
				}
		    	
		    	String zhSubjectMapKey =  "wf."+schoolId+processId+".00.zhSubjectMap";
		    	String subMapKey =  "wf."+schoolId+processId+".00.subMap";
		    	String byZhStudentMapKey =  "wf."+schoolId+processId+".00.byZhStudentMap";
		    	String allLessonNameMapKey =  "wf."+schoolId+processId+".00.allLessonNameMap";
		    	try {
		    		redisOperationDAO.set(allLessonNameMapKey, allLessonNameMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(zhSubjectMapKey, zhSubjectMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(subMapKey, subMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					redisOperationDAO.set(byZhStudentMapKey, byZhStudentMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e) {
					logger.info("progressMap缓存redis失败！...");
					e.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
				data.put("progress", 45);
				toFront.put("code", 1);
		        data.put("msg", "正在准备导入任务所需数据");
		        toFront.put("data", data);
		        progressMap.put(processId, toFront);
		        try {
					redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e1) {
					logger.info("progressMap缓存redis失败！...");
					e1.printStackTrace();
					delFileDBAndFileServer(schoolId+"","wf_"+keyId,processId);
					toFront.put("code", -50);
		            data.put("progress", 100);
		            data.put("msg", "redis缓存失败。");
		            toFront.put("data", data);
		            progressMap.put(processId, toFront);
				}
		    	return code;
			}
		}
		
		/**
		 * （5）获取导入进度
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
			 Object keyProgressMap = "wf."+getXxdm(req)+processId+".00.progressMap";
			   try {
					progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
				} catch (Exception e2) {
					logger.info("keyProgressMap获取redis失败！...");
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
					rs.put("code",-50);
					rs.put("data", "redis获取数据失败...");
			
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
		 * （6）单条修改验证
		 * 
		 * @param req
		 * @param res
		 * @return
		 * @author zhh
		 * @throws Exception 
		 */
		@SuppressWarnings("unchecked")
		@RequestMapping(value = "/singleDataCheck")
		@ResponseBody
		public synchronized JSONObject singleDataCheck(HttpServletRequest req,
				@RequestBody JSONObject post, HttpServletResponse res) throws Exception {
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
			Object keyPrepDataMap = "wf."+getXxdm(req)+processId+".00.prepDataMap";
			Object keyProgressMap = "wf."+getXxdm(req)+processId+".00.progressMap";
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			try {
				prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
				progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
			} catch (Exception e) {
				logger.info("redis获取prepDataMap/progressMap失败！");
				e.printStackTrace();
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				rs.put("code", -50);
				JSONObject data = new JSONObject();
				data.put("msg", "redis获取数据异常..");
				rs.put("data", data);
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
					rowDatas.put(row, sd);
					prepDataMap.get(processId).put("rowDatas", rowDatas);
					try {
						redisOperationDAO.set(keyPrepDataMap, prepDataMap, 
								CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					//增加导入姓名重复判断
					JSONObject json = new JSONObject();
					String classIdNameMapKey = "wf."+schoolId+processId+".00.classIdNameMap";
					String nameRepeatMapKey="wf."+processId+".nameRepeatMap";
					Map<String,String> classIdNameMap = (Map<String, String>) redisOperationDAO.get(classIdNameMapKey);
					Map<String,String> nameRepeatMap = (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
					json.put("pureDatas", pureDatas);
					List<JSONObject> titleNames = new ArrayList<JSONObject>();
					JSONObject titleName = new JSONObject();
					titleName.put("classTitleName","className" );
					titleName.put("titleName","name" );
					titleName.put("splitIndex","" );
					titleNames.add(titleName);
					json.put("titleNames", titleNames);
					String accountListKey="wf."+processId+".accountList";
					String excelNameMapKey="wf."+processId+".excelNameMap";
					Map<String,String> excelNameMap = (Map<String, String>) redisOperationDAO.get(excelNameMapKey);
					List<Account> accoutList = (List<Account>) redisOperationDAO.get(accountListKey);
					json.put("list", accoutList);
					json.put("roleType", "Student");
					json.put("plate", 1);
					json.put("nameRepeatMap", nameRepeatMap);
					json.put("excelType", "1");
					json.put("excelRule", "name");
					json.put("excelNameMap", excelNameMap);
					json.put("classIdNameMap", classIdNameMap);
					JSONObject returnRepeatName = NameRepeatJudge.judgeNameRepeatImport(json);
					JSONArray wrongMsg = new JSONArray();
					Map<String,String>  stringMap = new HashMap<String, String>(); 
					if(returnRepeatName!=null){
						nameRepeatMap = (Map<String, String>) returnRepeatName.get("nameRepeatMap");
						wrongMsg = (JSONArray) returnRepeatName.get("wrongMsg");
						stringMap = (Map<String, String>) returnRepeatName.get("stringMap");
						excelNameMap = (Map<String,String>) returnRepeatName.get("excelNameMap");
					}
					String stringMapKey="wf."+processId+".stringMap";
					redisOperationDAO.set(excelNameMapKey, excelNameMap);
					redisOperationDAO.set(stringMapKey, stringMap);
					redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
					Object keyArr = "wf."+schoolId+processId+".00.arr";
					String [][] arr = (String [][]) redisOperationDAO.get(keyArr);
					JSONObject cr = checkImpData(arr,stringMap,nameRepeatMap,wrongMsg,pureDatas, mrs, isMatch, processId, sp,req.getSession(),progressMap);
					redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
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
		private JSONObject checkImpData( String [][] arr ,Map<String,String> stringMap,Map<String,String> nameRepeatMap ,JSONArray wrongMsgRepeat,HashMap<Integer, JSONObject> pureDatas,JSONArray mrs, int isMatch, String processId,ImportTaskParameter sp,HttpSession ses,Hashtable<String, JSONObject> progressMap) {
			String schoolId=sp.getSchoolId();
			String termInfo = sp.getTermInfo();
			String keyId = (String) ses.getAttribute("keyId");
			// excel验证部分
			JSONObject rs = new JSONObject();
			// 导入进度
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			
			int wfNum = 3;//  sp.getWfNum();
			String wfWay = sp.getWfWay();
			String  isByElection = sp.getIsByElection();
			Long gradeId = sp.getGradeId();
			Grade g = allCommonDataService.getGradeById(Long.parseLong(schoolId), gradeId, termInfo);
			List<Long> classIds = g.getClassIds();
			data.put("progress", 65);
			data.put("msg", "正在校验excel数据,匹配表头完成,校验数据");
			toFront.put("data", data);
			//处理因姓名重复而不再判断的行
			Map<Integer,JSONArray> mrowRepeatMap = new HashMap<Integer,JSONArray>();// 行号-错误信息
			if(wrongMsgRepeat!=null){
				for(int i =0;i<wrongMsgRepeat.size();i++){
					JSONObject wrongMsgRepeatObj = (JSONObject) wrongMsgRepeat.get(i);
					Integer row = wrongMsgRepeatObj.getInteger("row");
					JSONArray  mrows = (JSONArray) wrongMsgRepeatObj.get("mrows");
					mrowRepeatMap.put(row, mrows);
				}
			}
			//redis获取teacherMap
			Object keyPrepDataMap = "wf."+schoolId+processId+".00.prepDataMap";
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
		            delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			}
			String classNameMapKey = "wf."+schoolId+processId+".00.classNameMap";
			String studentMapKey = "wf."+schoolId+processId+".00.studentMap";
			String subMapKey =  "wf."+schoolId+processId+".00.subMap";
			String zhSubjectMapKey =  "wf."+schoolId+processId+".00.zhSubjectMap";
			String allLessonNameMapKey =  "wf."+schoolId+processId+".00.allLessonNameMap";
			Object keyPycc = "wf."+schoolId+processId+".00.pycc";
			HashMap<String, Classroom> classNameMap = new HashMap<String, Classroom>();
			HashMap<String,JSONObject> studentMap = new HashMap<String,JSONObject>();
			HashMap<String,JSONObject> subMap = new HashMap<String,JSONObject>(); //zhId - JSONOBJ
			HashMap<String,String> zhSubjectMap = new HashMap<String,String>(); //subjectIds - zhId
			HashMap<String,JSONObject> allLessonNameMap = new HashMap<String,JSONObject>(); //subjectId - lesson JSON
			String pycc = "";
			try {
				classNameMap = (HashMap<String, Classroom>) redisOperationDAO.get(classNameMapKey);
				studentMap = (HashMap<String, JSONObject>) redisOperationDAO.get(studentMapKey);
				subMap =  (HashMap<String, JSONObject>) redisOperationDAO.get(subMapKey);
				zhSubjectMap =  (HashMap<String, String>) redisOperationDAO.get(zhSubjectMapKey);
				allLessonNameMap = (HashMap<String, JSONObject>) redisOperationDAO.get(allLessonNameMapKey);
				pycc = (String) redisOperationDAO.get(keyPycc);
				logger.info("studentMap:"+studentMap+"classNameMap:"+classNameMap);
			} catch (Exception e1) {
				logger.info("prepDataMap为空");
				e1.printStackTrace();
				toFront.put("code", -50);
		        data.put("progress", 100);
		        data.put("msg", "redis缓存失败。");
		        toFront.put("data", data);
		        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
		        progressMap.put(processId, toFront);
			}
			
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
			        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			        progressMap.put(processId, toFront);
				}
			}
			//progNum = 6;
			JSONArray wrongMsg = new JSONArray();
			JSONArray exDatas = new JSONArray();
			JSONObject preData = prepDataMap.get(processId);

			/*JSONObject studentNameObj =preData.getJSONObject("studentNameObj");   
			if(studentNameObj==null){
				studentNameObj=new JSONObject();
			}*/
			
			String[] impHead = (String[]) preData.get("impHead");		
			progressMap.put(processId, toFront);
			Object keyProgressMap = "wf."+schoolId+processId+".00.progressMap";
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
		        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
			for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
			{
				index++;
				boolean isWsg=false;
				boolean isStudentNameRepeat=false;
				int rowNum = it.next();
				
				JSONObject pd = pureDatas.get(rowNum);
				JSONObject wmsg = new JSONObject();
				wmsg.put("row", rowNum);
				
				//如果有姓名重复，直接出错，进行下一轮判断
				JSONArray mrowRepeat = mrowRepeatMap.get(rowNum);
				/*if(mrowRepeat!=null){
					if(mrowRepeat.size()>0){
						wmsg.put("mrows", mrowRepeat);
						wrongMsg.add(wmsg);
						continue;
					}
				}*/
				JSONArray mrows = new JSONArray();
				if(mrowRepeat!=null){
					mrows=mrowRepeat;
				}
				wmsg.put("mrows", mrows);
				// excel字段验证
				String className = pd.containsKey("className") ? pd.getString("className"):null;
				String name = pd.containsKey("name") ? pd.getString("name"):null;
				name=name.trim();
				String trueName=name;
				if(StringUtils.isNotBlank(trueName) && StringUtils.isNumeric(trueName)){
					String trueNameString = stringMap.get(trueName);
					trueName=trueNameString;
				}
				//excel验证className数据合理性
				String title=pd.getString("classNameName");
				Long cId = -1L;
				  //className不为空
				if (className != null && className.trim().length() > 0) {
					//有无班级记录
					if(classNameMap!=null && classNameMap.containsKey(className)){
						   Classroom  c= classNameMap.get(className);
						   if(c!=null && c.getId()!=-1){
							 //查看这个班级是否符合设置的年级
							   cId=c.getId();
							   if(!classIds.contains(c.getId())){
								   //不符合
								   JSONObject wsg = new JSONObject();
									wsg.put("title", title);
									wsg.put("oldValue", className);
									wsg.put("err",  "该班级不符合设置的开放年级！");
									mrows.add(wsg);
									isWsg=true;
							   }else{ 
								   //查看该班级下有无 该学生
									if(StringUtils.isNotBlank(trueName)){
										   if(studentMap!=null && !studentMap.containsKey(trueName.trim()+"_"+cId)){
											   JSONObject wsg = new JSONObject();
												wsg.put("title", pd.getString("nameName"));
												wsg.put("oldValue", name);
												wsg.put("err",  "班级下无匹配学生！");
												mrows.add(wsg);
												isWsg=true;
												
										   }
									}
							   }
						   }
					}else{
						   JSONObject wsg = new JSONObject();
							wsg.put("title", title);
							wsg.put("oldValue", className);
							wsg.put("err",  "无匹配记录！");
							mrows.add(wsg);
							isWsg=true;
					 }
				}else{
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", className);
					wsg.put("err", "不能为空！");
					mrows.add(wsg);
					isWsg=true;
				}
				
				
				//excel验证name数据合理性
				title=pd.getString("nameName");
				if (!(trueName != null && trueName.trim().length() > 0))  {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", name);
					wsg.put("err", "不能为空！");
					isWsg=true;
					mrows.add(wsg);
				}
				
				String err = "";			
				boolean noRecord = false;
				// 非必输字段	（不需判断空）	
				int count=0;
				String subIds = "" ;
				List<JSONObject> mrowsForSubject = new ArrayList<JSONObject>();
				Boolean isLessSelect = false; //至少选择一门
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

						val = val.replaceAll(" ", "");
						val = val.trim();
						if("1".equals(val)){
							subIds += tit+",";
						}
						if("className".equals(tit) || "name".equals(tit) || "mobilePhoneOrIdNumber".equals(tit)){continue;}
						String trueTitName = "";
						for(int k=0;k<arr[0].length;k++){
							String titNameExcel = arr[0][k];
							if(titName!=null && titName.equals(titNameExcel)){
								trueTitName = arr[2][k];
								break;
							}
						}
						String selectRule = allLessonNameMap.get(trueTitName).getString("selectRule");//titName为政治1 ;找到政治再获取selectRule
						if(!(StringUtils.isNotBlank(val) && "1".equals(val)) ){
							if(StringUtils.isNotBlank(val)){
								wsg.put("type", "0"); //格式不正确	
							}
			            }else{
			            	if(StringUtils.isNotBlank(val) && "1".equals(val)){
			            			count++; 
			            			//输入的是1
			            			wsg.put("type", "1");
			            			if(selectRule!=null && "1".equals(selectRule)){ //至少选择了一门该科目
			            				isLessSelect=true;
			            			}
			            	}
			            }
						mrowsForSubject.add(wsg);
					} //end of for
					if(count!=wfNum){
						isWsg=true;
						for(JSONObject row : mrowsForSubject){
							row.put("err", "选择的科目个数不正确!");
						}
						mrows.addAll(mrowsForSubject);
					}else if(isLessSelect==false && "3".equals(pycc)){ //是选择的初中且未选物理或者生（化）则给出提示
						isWsg=true;
						for(JSONObject row : mrowsForSubject){
							row.put("err", "请至少选择一门物理或生物(化学)！");
						}
						mrows.addAll(mrowsForSubject);
					} else{
						
						for(JSONObject row : mrowsForSubject){
							String type = row.getString("type");
							if("0".equals(type)){
								isWsg=true;
								noRecord=true;
								row.put("err", "输入值不合法！");
								mrows.add(row);
							}
							
						}
						 //输入都合法的情况下  && 为补选或者组合选
						if(!noRecord && ("1".equals(wfWay) || ("0".equals(wfWay) && "1".equals(isByElection)))){
							if(subIds.length()>0){
								subIds = subIds.substring(0,subIds.length()-1);
							}
							String zhId = zhSubjectMap.get(subIds);
							if("1".equals(wfWay)){ //组合
								if(StringUtils.isBlank(zhId)){
									isWsg=true;
									for(JSONObject row : mrowsForSubject){
										row.put("err", "科目对应的组合不匹配!");
									}
									mrows.addAll(mrowsForSubject);
								}
							}else if("1".equals(isByElection) && "0".equals(wfWay)){ //补选
								if(StringUtils.isNotBlank(zhId)){
									JSONObject subjectObj =	subMap.get(zhId);
									String  zhWay= subjectObj.getString("zhWay"); //得到改组合方式
									if(!"1".equals(zhWay)){ //不是固定的组合
										isWsg=true;
										for(JSONObject row : mrowsForSubject){
											row.put("err", "科目对应的固定组合不匹配!");
										}
										mrows.addAll(mrowsForSubject);
									}
								}else{
									isWsg=true;
									for(JSONObject row : mrowsForSubject){
										row.put("err", "科目对应的组合不匹配!");
									}
									mrows.addAll(mrowsForSubject);
								}
							}
					   }
					}

				if (mrows.size() > 0) 
				{
					if(!isStudentNameRepeat&& (isWsg||noRecord) )
					{
						//studentNameObj.remove(name+"_"+cId);
					}				
					wrongMsg.add(wmsg);
					nameRepeatMap.remove(rowNum+"_name");
				} 
				else 
				{
					wmsg = null;
				}
				
				if(mrows.size()==0&&err.length()==0&&!noRecord)
				{
					rowcolMap.put(rowNum, pd);//将该行数据存入
				}
				data.put("progress", 65+(20*(index/pureDatas.keySet().size())));
			}//end of for
			preData.put("rowcolMap", rowcolMap);
			//preData.put("studentNameObj", studentNameObj);
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
		        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			}
			return rs;
		}
		/**
		 * （7）继续导入任务启动接口
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

				SubProcess sp = new SubProcess(processId, 1, null,getXxdm(req),req.getSession(),1);
				sp.start();
			} catch (Exception e) {
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				logger.error("continueImport", e);
				code = -1;
				msg = "启动进程异常！";

			}

			obj.put("code", code);
			JSONObject data = new JSONObject();
			data.put("msg", msg);
			obj.put("data", data);
			logger.debug("继续导入-主线程结束！");
			return obj;

		}

	/**
	 * 将excel读取为数据
	 * 
	 * @param impFrc
	 * @return
	 * @author zhh
	 */
	private JSONObject readExcelToData(String fileId,String schoolId,String keyId,String processId) {
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
									temp[j] = df.format(cell.getNumericCellValue());
									break;
								case HSSFCell.CELL_TYPE_STRING:
									temp[j] = cell.getRichStringCellValue()
											.getString().trim().replaceAll("，", ",").replaceAll("（", "(").replaceAll("）", ")");
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
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			} catch (IOException e) {
				code = -20;
				e.printStackTrace();
				 delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			} catch (Exception e) {
				code = -30;
				e.printStackTrace();
				delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
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
			String[] titles = (String[]) datas.get(0);//得到excel第一行表头
			//存储导入excel表头arr[0][]  是否必填arr[1][]  映射的系统字段：中文名称arr[2][]  映射实体字段：对应的英文名称 arr[3][]
			String[][] arr = new String[4][titles.length];
			if(stuTitle!=null && stuTitleName !=null && stuTitleNeed!=null){
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
			Object keyArr = "wf."+schoolId+processId+".00.arr";
			Object keyPrepDataMap = "wf."+schoolId+processId+".00.prepDataMap";
			try {
				redisOperationDAO.set(keyArr, arr, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
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
		        delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
		        e.printStackTrace();
			}
			return rs;
		}	
		/**
		 * 根据参数生成需插入数据
		 * 
		 * @param sp
		 * @param processId
		 * @return
		 * @author zhh
		 * @throws Exception 
		 */
		@SuppressWarnings("unchecked")
		private Map<String,Object> getInsertEntityByCkrs(ImportTaskParameter sp, String processId,HttpSession ses,Hashtable<String, JSONObject> progressMap)  {
			String schoolId=sp.getSchoolId();
			String isByElection = sp.getIsByElection();
			String wfWay = sp.getWfWay();
			String wfId = sp.getWfId();
			String termInfo = sp.getTermInfo();
			String xn = termInfo.substring(0,4);
			String keyId = (String) ses.getAttribute("keyId");
			Map<String,Object> param = new HashMap<String,Object>();
			
			List<JSONObject> insertStudentTb = new ArrayList<JSONObject>();
			List<JSONObject> insertStudentZh = new ArrayList<JSONObject>();
			//分班修改志愿数据
			Map<String,List<String>> studentFbMap = new HashMap<String,List<String>>();
 			JSONObject delStudentTb = new JSONObject();
			delStudentTb.put("wfId", wfId);
			delStudentTb.put("schoolId", schoolId);
			delStudentTb.put("termInfo", termInfo);
			Set<String> accountIds = new HashSet<String>(); 
			Object keyPrepDataMap = "wf."+sp.getSchoolId()+processId+".00.prepDataMap";
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
			    delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
			}
			if(prepDataMap!=null){
				HashMap<Integer, JSONObject> rows = (HashMap<Integer, JSONObject>) prepDataMap
						.get(processId).get("rowcolMap");
				List<String> delStudentTbList=new ArrayList<String>();//和系统场馆重名的则覆盖	
				Map<String,String> nameRepeatMap = new HashMap<String,String>();
				Map<String,String> stringMap = new HashMap<String,String>();
				String nameRepeatMapKey="wf."+processId+".nameRepeatMap";
				String stringMapKey="wf."+processId+".stringMap";
				try {
					 stringMap =   (Map<String, String>) redisOperationDAO.get(stringMapKey);
					 nameRepeatMap =   (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
				} catch (Exception e1) {
					logger.info("redis获取prepDataMap失败！");
					e1.printStackTrace();
					JSONObject toFront = progressMap.get(processId);
					JSONObject data = new JSONObject();
					toFront.put("code", -50);
				    data.put("progress", 100);
				    data.put("msg", "redis缓存失败。");
				    toFront.put("data", data);
				    progressMap.put(processId, toFront);
				    delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				}
				
				HashMap<String,JSONObject> studentMap = new HashMap<String,JSONObject>();//(HashMap<String, JSONObject>) ses.getAttribute("studentMap");
				HashMap<Long,Classroom> classMap = new HashMap<Long,Classroom>();//(HashMap<Long, Classroom>) ses.getAttribute("classMap");
				HashMap<Long,Grade> gMap = new HashMap<Long,Grade>(); //(HashMap<Long, Grade>) ses.getAttribute("gMap");
				HashMap<Long,JSONObject> lessonMap = new HashMap<Long,JSONObject> (); //(HashMap<Long, JSONObject>) ses.getAttribute("lessonMap");
				HashMap<String,String> zhSubjectMap =  new HashMap<String,String> ();//(HashMap<String, String>) ses.getAttribute("zhSubjectMap");
				HashMap<String,String> byZhStudentMap =  new HashMap<String,String> ();
				HashMap<String,Classroom> classNameMap =new HashMap<String,Classroom> (); // (HashMap<String,Classroom>) ses.getAttribute("classNameMap");
				String classNameMapKey = "wf."+schoolId+processId+".00.classNameMap";
				String classMapKey = "wf."+schoolId+processId+".00.classMap";
				String studentMapKey = "wf."+schoolId+processId+".00.studentMap";
				String lessonMapKey = "wf."+schoolId+processId+".00.lessonMap";
				String gMapKey = "wf."+schoolId+processId+".00.gMap";
				String zhSubjectMapKey =  "wf."+schoolId+processId+".00.zhSubjectMap";
				String byZhStudentMapKey =  "wf."+schoolId+processId+".00.byZhStudentMap";
				try {
					studentMap = (HashMap<String, JSONObject>) redisOperationDAO.get(studentMapKey);
					classMap = (HashMap<Long, Classroom>) redisOperationDAO.get(classMapKey);
					gMap=(HashMap<Long, Grade>) redisOperationDAO.get(gMapKey);
					lessonMap=(HashMap<Long, JSONObject>) redisOperationDAO.get(lessonMapKey);
					zhSubjectMap = (HashMap<String, String>) redisOperationDAO.get(zhSubjectMapKey);
					classNameMap = (HashMap<String, Classroom>) redisOperationDAO.get(classNameMapKey);
					byZhStudentMap = (HashMap<String, String>) redisOperationDAO.get(byZhStudentMapKey);
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
				    delFileDBAndFileServer(schoolId,"wf_"+keyId,processId);
				}
				
				
				if(studentMap!=null && classMap!=null  && gMap!=null && classNameMap!=null && lessonMap!=null && zhSubjectMap!=null){
					for (Iterator<Integer> it = rows.keySet().iterator(); it.hasNext();) {
						int key = it.next(); //行号索引
						
						JSONObject row = rows.get(key);
						String className=row.getString("className");
						Classroom c1 = classNameMap.get(className);
						String name=row.getString("name");
						if(StringUtils.isNumeric(name)){
							name= stringMap.get(name);
						}
						//得到库中重复的姓名的accountIds（按顺序）
						String repeatTeacherAccountIds = nameRepeatMap.get(key+"_name");
						JSONObject obj = studentMap.get(name+"_"+c1.getId());
						if(obj!=null){
							Long classId = c1.getId();
							Integer count = obj.getInteger("count");
							String accountId = "";
							if(count>1){
								 accountId = repeatTeacherAccountIds;
							}else{
								 accountId = obj.getString("studentId");
							}
							Classroom  c = classMap.get(classId);
							Long gId = c.getGradeId();
							Grade g=gMap.get(gId);
							String useGrade = allCommonDataService.ConvertNJDM2SYNJ(g.currentLevel.getValue()+"", xn);
							
							String subjectIds = "" ;
							JSONObject studentZhObj= new JSONObject();
							for (Map.Entry<Long, JSONObject> entry : lessonMap.entrySet()) 
							{
//								 System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
								Long lessonId = entry.getKey();
								String lessonValue = row.getString(lessonId+"");
								if("1".equals(lessonValue)){
									accountIds.add(accountId);
									JSONObject studentTbObj= new JSONObject();
									studentTbObj.put("accountId", accountId);
									studentTbObj.put("useGrade", useGrade);
									studentTbObj.put("classId", classId);
									studentTbObj.put("schoolId", schoolId);
									studentTbObj.put("termInfoId", termInfo.substring(4, termInfo.length()));
									studentTbObj.put("schoolYear", termInfo.substring(0,4));
									studentTbObj.put("wfId", wfId);
									studentTbObj.put("isImport", 1);
									studentTbObj.put("isAdjust", 0);
									studentTbObj.put("subjectId", lessonId);
									//isFixedZh
									if("1".equals(isByElection)){
										String isFixedZh = byZhStudentMap.get(accountId);
										if(StringUtils.isBlank(isFixedZh)||"0".equals(isFixedZh)){
											studentTbObj.put("isFixedZh", 2);
										}else{
											studentTbObj.put("isFixedZh", isFixedZh);
										}
									}
									insertStudentTb.add(studentTbObj);
									//得到分班修改志愿的组装数据
									List<String> lessonIds = new ArrayList<String>();
									if(studentFbMap.containsKey(accountId)){
										lessonIds =  (List<String>) studentFbMap.get(accountId);
									}
									lessonIds.add(lessonId+"");
									studentFbMap.put(accountId, lessonIds);
									
									studentZhObj.put("accountId", accountId);
									studentZhObj.put("useGrade", useGrade);
									studentZhObj.put("classId", classId);
									studentZhObj.put("schoolId", schoolId);
									studentZhObj.put("termInfoId", termInfo.substring(4, termInfo.length()));
									studentZhObj.put("schoolYear", termInfo.substring(0,4));
									studentZhObj.put("wfId", wfId);
									studentZhObj.put("isAdjust", 0);
									studentZhObj.put("isImport", 1);
									if("1".equals(isByElection)){
										String isFixedZh = byZhStudentMap.get(accountId);
										if(StringUtils.isBlank(isFixedZh)||"0".equals(isFixedZh)){
											studentZhObj.put("isFixedZh", 2);
										}else{
											studentZhObj.put("isFixedZh", isFixedZh);
										}
									}
									subjectIds+=lessonId+",";
									
								}
							}//end of for subject
							subjectIds = subjectIds.substring(0, subjectIds.length()-1);
							String zhId = zhSubjectMap.get(subjectIds);
							if(StringUtils.isNotBlank(zhId)){
								studentZhObj.put("zhId", zhId);
								studentZhObj.put("subjectIds", subjectIds);
								//isFixedZh
								insertStudentZh.add(studentZhObj);
							}
						}// end of obj ！= null
						
					}//end of for row
				}
			}
			param.put("isByElection",isByElection);
			param.put("wfWay",wfWay);
			param.put("insertStudentTb",insertStudentTb);
			param.put("insertStudentZh",insertStudentZh);
			if(accountIds.size()>0){
				delStudentTb.put("accountIds", new ArrayList<String>(accountIds));
			}else{
				accountIds.add("-1");
				delStudentTb.put("accountIds", new ArrayList<String>(accountIds));
			}
			param.put("delStudentTb",delStudentTb);
			param.put("areaCode", sp.getAreaCode());
			param.put("studentFbMap", studentFbMap);
			return param;
		}
}
