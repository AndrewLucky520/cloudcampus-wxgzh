package com.talkweb.csbasedata.service.impl;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

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
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.FileImportInfoDao;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.dao.TeacherManageDao;
import com.talkweb.csbasedata.service.ImportManageService;
import com.talkweb.csbasedata.service.TeacherManageService;
import com.talkweb.csbasedata.util.DateUtil;
import com.talkweb.csbasedata.util.ImportTaskParameter;
import com.talkweb.csbasedata.util.SortUtil;
import com.talkweb.csbasedata.util.StringUtil;
import com.talkweb.filemanager.service.FileServer;

@Service("teacherImportService")
public class TeacherImportServiceImpl implements ImportManageService {
	
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Autowired
	private FileImportInfoDao fileImportDao;

	@Autowired
	private TeacherManageService teacherManageService;
	
	@Autowired
	private TeacherManageDao teacherManageDao;
	private static final Logger logger = LoggerFactory.getLogger(TeacherImportServiceImpl.class);
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	
	private static final String headString="teacherManage_";
	private static final String headStringd ="teacherManage.";
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;	    
	
	/**
	 * 删除文件数据库中的文件及文件服务器的文件
	 * @param schoolId
	 * @param keyId
	 */
	private int delFileDBAndFileServer(String schoolId,String keyId){
		JSONObject fileObj = new JSONObject();
		fileObj.put("schoolId", schoolId);
		fileObj.put("keyId", keyId);
		String fId=fileImportDao.getFileBy(fileObj);
		if(StringUtils.isNotBlank(fId)){
			fileImportDao.deleteFile(fileObj);	
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
	@Override
	public JSONObject uploadExcel(JSONObject param) throws Exception{
		String schoolId = param.getString("schoolId");
		String s = param.getString("s");
		String fileName = param.getString("fileName");
		String prefix = param.getString("prefix");
		File df = (File) param.get("df");
		
		JSONObject obj = new JSONObject();
	    String[] stuTitle = null;
	    String[] stuTitleName = null;
	    int[] stuTitleNeed = null;
		if (stuTitle == null) {
			stuTitle = new String[3];
			stuTitle[0] = "姓名";
			stuTitle[1] = "性别";
			stuTitle[2] = "手机号码";
			
			
			stuTitleName = new String[stuTitle.length];
			stuTitleName[0] = "teacherName";
			stuTitleName[1] = "gender";
			stuTitleName[2] = "mobilePhone";
			
			stuTitleNeed = new int[stuTitle.length];
			stuTitleNeed[0] = 1;
			stuTitleNeed[1] = 0;
			stuTitleNeed[2] = 0;

		}
		obj.put("stuTitle",stuTitle);
		obj.put("stuTitleName", stuTitleName);
		obj.put("stuTitleNeed", stuTitleNeed);
		int code = 1;
		String msg = "";
		// 临时文件保存目录
		File dir = new File(tempFilePath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			
			String fileId = fileServerImplFastDFS.uploadFile(df,fileName); //上传至文件服务器
			//将文件信息传入数据库
			obj.put("keyId", headString+s);
			obj.put("fileId", fileId);
			JSONObject json = new JSONObject();
			json.put("schoolId", schoolId);
			json.put("keyId", headString+s);
			json.put("fileId", fileId);
			fileImportDao.addFile(json);
			
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
					if (isInArray("姓名", tempStuImpExcTitle)) {
						code = 1;
					} 
					else {
						code = -3;
						msg = "请检查必填字段标识是否正确！";
					}
				}
				obj.put("tempStuImpExcTitle",tempStuImpExcTitle);
				if(rows<2){ //表示仅有表头数据
					obj.put("isContentEmpty", true);
					obj.put("code", -4);
					obj.put("msg", "Excel数据为空！");
					return obj;
				}
			} else {
				code = -2102;
				msg = OutputMessage.getDescByCode(code + "");
			}
			workbook.close();
		} catch (Exception e) {
			delFileDBAndFileServer(schoolId,headString+s);
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

	@Override
	public JSONObject getExcelMatch(JSONObject param)  throws Exception{
		JSONObject excelHead = new JSONObject();
		JSONObject rs = new JSONObject();
		// 获取session中保存的临时表头
		String[] tmpTit = (String[]) param.get("tempTitle");
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
		String [] stuTitle=(String[]) param.get("stuTitle");
		int [] stuTitleNeed=(int[]) param.get("stuTitleNeed");
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
		return rs;
	}

	@Override
	public JSONObject continueImport(JSONObject param)  throws Exception{
		String processId = param.getString("processId");
		String keyId = param.getString("keyId");
		int code = 1;
		String schoolId= param.getString("schoolId");
		String msg = "正在保存";
		JSONObject obj = new JSONObject();
		try {

			SubProcess sp = new SubProcess(processId, 1, schoolId,keyId);
			sp.start();
		} catch (Exception e) {
			code = -1;
			msg = "启动进程异常！";
			  delFileDBAndFileServer(schoolId,headString+keyId);

		}

		obj.put("code", code);
		JSONObject data = new JSONObject();
		data.put("msg", msg);
		obj.put("data", data);
		return obj;
	}

	@Override
	public JSONObject singleDataCheck(JSONObject param)  throws Exception{
		JSONObject rs = new JSONObject();

		String schoolId = param.getString("schoolId");
		String keyId = param.getString("keyId");
		String processId = param.getString("processId");
		int row = param.getIntValue("row");
		JSONArray mrows = param.getJSONArray("mrows");
		int code = param.getIntValue("code");
		rs.put("rowNum", row);
		Object keyPrepDataMap = headStringd+schoolId+processId+".00.prepDataMap";
		Object keyProgressMap = headStringd+schoolId+processId+".00.progressMap";
		Object keyNumRepeatMap = headStringd+schoolId+processId+".00.numRepeatMap";
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		Map<String,LinkedHashSet<String>> excelNameMap =  new LinkedHashMap<String,LinkedHashSet<String>>();
		Map<String,Integer> numRepeatMap = new HashMap<String,Integer>();
		JSONObject preData = new JSONObject();
		try {
			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
			progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
			preData = prepDataMap.get(processId);
			excelNameMap =(Map<String, LinkedHashSet<String>>) preData.get("excelNameMap");
			if(excelNameMap==null){
				excelNameMap = new HashMap<String,LinkedHashSet<String>>();
			}
			numRepeatMap = (Map<String, Integer>) preData.get("numRepeatMap");
			if(numRepeatMap==null){
				numRepeatMap =  new HashMap<String,Integer>();
			}
			redisOperationDAO.set(keyNumRepeatMap, numRepeatMap);
		} catch (Exception e) {
			e.printStackTrace();
			rs.put("code", -50);
			JSONObject data = new JSONObject();
			data.put("msg", "redis获取数据异常..");
			rs.put("data", data);
			  delFileDBAndFileServer(schoolId,headString+keyId);
		}
		if(prepDataMap!=null && progressMap !=null){
			JSONObject toFront = progressMap.get(processId);
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
					rs.put("code", -50);
					JSONObject data = new JSONObject();
					data.put("msg", "redis获取数据异常..");
					rs.put("data", data);
					  delFileDBAndFileServer(schoolId,headString+keyId);
					  e.printStackTrace();
				}
				JSONObject cr = checkImpData(mrows,keyId,pureDatas, mrs, isMatch, processId, sp,progressMap);
				
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

	@Override
	public JSONObject importProgress(JSONObject param)  throws Exception{
		JSONObject rs = new JSONObject();
		String schoolId =param.getString("schoolId");
		String keyId = param.getString("keyId");
		String processId=param.getString("processId");
		 Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		 Object keyProgressMap = headStringd+schoolId+processId+".00.progressMap";
		   try {
				progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
			} catch (Exception e2) {
				e2.printStackTrace();
				rs.put("code",-50);
				rs.put("data", "redis获取数据失败...");
			    delFileDBAndFileServer(schoolId,headString+keyId);
		
			}
			if(progressMap!=null){
				JSONObject obj = progressMap.get(processId);
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

	@Override
	public JSONObject startImportTask(JSONObject param)  throws Exception{
		ImportTaskParameter stt = new ImportTaskParameter();
		String [] stuTitleName = (String[]) param.get("stuTitleName");
		int [] stuTitleNeed = (int[]) param.get("stuTitleNeed");
		String [] stuTitle = (String[]) param.get("stuTitle");
		String xnxq = param.getString("xnxq");
		String fileId = param.getString("fileId");
		String  keyId = param.getString("keyId");
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		String schoolId=param.getString("schoolId");
		stt.setIsMatch(param.getIntValue("isMatch"));
		if(stt.getIsMatch()==1)
		{
			stt.setMatchResult(param.getJSONArray("matchResult"));
		}
		stt.setXnxq(xnxq);
		stt.setStuTitle(stuTitle);
		stt.setStuTitleName(stuTitleName);
		stt.setStuTitleNeed(stuTitleNeed);
		stt.setFileId(fileId);
		stt.setKeyId(keyId);
		stt.setSchoolId(schoolId);
		// 设置获取的参数
		String processId = param.getString("processId");
		stt.setProcessId(processId);
		//设置公共参数
		int commonCode=this.setAllCommonInfo(stt);
		if(commonCode<0){
			JSONObject obj = new JSONObject();
			delFileDBAndFileServer(schoolId,headString+keyId);
			obj.put("code", -50); //code=-50为redis读取异常
	        obj.put("msg", "出现异常，导入失败！");
	        return obj;
		}
		
		JSONObject procObj = new JSONObject();
		procObj.put("taskParam", stt);
		progressMap.put(processId, procObj);
		Object keyProgressMap = headStringd+schoolId+processId+".00.progressMap";
		JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "正常启动任务");
		try {
			redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			delFileDBAndFileServer(schoolId,headString+keyId);
			e.printStackTrace();
			obj.put("code", -50); //code=-50为redis读取异常
	        obj.put("msg", "出现异常，导入失败！");
			return obj;
		}
		
		SubProcess sp = new SubProcess(processId, 0,schoolId,keyId);
		sp.start();		

		JSONObject data = new JSONObject();
		data.put("progress", 0);
		data.put("msg", "正常启动任务");
		procObj.put("code", 0);
		procObj.put("data", data);
		try {
			redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			delFileDBAndFileServer(schoolId,headString+keyId);
			e.printStackTrace();
			obj.put("code", -50); //code=-50为redis读取异常
	        obj.put("msg", "出现异常，导入失败！");
			return obj;
		}
		return obj;
	}
	class SubProcess extends Thread {
		/**
		 * 进程代码
		 */
		private String processId;
		/**
		 * 学校代码
		 */
		private String schoolId;
		/**
		 * 导入类型0：第一次导入，1：校验后继续导入
		 */
		private int impType;
		private String keyId;

		public SubProcess(String processId, int impType,String schoolId,String keyId) {
			this.processId = processId;
			this.impType = impType;
			this.schoolId= schoolId;
			this.keyId = keyId;
		}

		@Override
		public void run() {
			  long t1 = (new Date()).getTime();
			    Hashtable<String, JSONObject> progressMap = null;
			    Object keyProgressMap = headStringd+schoolId+processId+".00.progressMap";
			    try {
					progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
				} catch (Exception e2) {
					e2.printStackTrace();
					delFileDBAndFileServer(schoolId,headString+keyId);
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
						e.printStackTrace();
						delFileDBAndFileServer(schoolId,headString+keyId);
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
				String fId=sp.getFileId();
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
						if(datas.size()>0){
							ckExcTt = (String[]) datas.get(0);
						}
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
									toFront.put("code", -50);
						            data.put("progress", 100);
						            data.put("msg", "redis缓存失败。");
						            toFront.put("data", data);
						            progressMap.put(processId, toFront);
								}
								JSONObject changeParam = new JSONObject();
								changeParam.put("datas", datas);
								changeParam.put("progressMap", progressMap);
								JSONObject preDatas = changeData(sp,changeParam);
								HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) preDatas.get("rowDatas");
								ckrs = checkImpData(null,keyId,pureDatas, mrs, isMatch, processId, sp,progressMap);
							}
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
									toFront.put("code", -50);
						            data.put("progress", 100);
						            data.put("msg", "redis缓存失败。");
						            toFront.put("data", data);
						            progressMap.put(processId, toFront);
								}
								Map<String,Object> needInsert = getInsertEntityByCkrs(keyProgressMap,sp, processId,progressMap);
								if (needInsert.size() >= 0) {
									data.put("progress", 90);
									data.put("msg", "正在保存数据！");
									toFront.put("data", data);
									progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,headString+keyId);
										toFront.put("code", -50);
							            data.put("progress", 100);
							            data.put("msg", "redis缓存失败。");
							            toFront.put("data", data);
							            progressMap.put(processId, toFront);
									}
									logger.info("90progress start.......");
									int num = teacherManageService.insertImportTeacherBatch(needInsert);						
									logger.info("90progress end.......");
									toFront.put("code", 2);
						            data.put("progress", 100);
						            data.put("msg", "导入成功，共计导入"+ num +"条信息记录！");
						            delFileDBAndFileServer(schoolId,headString+keyId);
						            progressMap.put(processId, toFront);
									try {
										redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
									} catch (Exception e) {
										e.printStackTrace();
										delFileDBAndFileServer(schoolId,headString+keyId);
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
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
									e.printStackTrace();
									delFileDBAndFileServer(schoolId,headString+keyId);
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
					e.printStackTrace();
					delFileDBAndFileServer(schoolId,headString+keyId);
					readCode=-30;
					toFront.put("code", -1);
					data.put("progress", 100);
					data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
					progressMap.put(processId, toFront);
					try {
						redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					} catch (Exception e1) {
						e1.printStackTrace();
						delFileDBAndFileServer(schoolId,headString+keyId);
						toFront.put("code", -50);
			            data.put("progress", 100);
			            data.put("msg", "redis缓存失败。");
			            toFront.put("data", data);
			            progressMap.put(processId, toFront);
					}
				}
			}else{
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
	 * 根据参数生成需插入数据
	 * 
	 * @param sp
	 * @param processId
	 * @return
	 * @author zhh
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getInsertEntityByCkrs(Object keyProgressMap,ImportTaskParameter sp, String processId,Hashtable<String, JSONObject> progressMap) {
		Map<String, Object>  returnParam = new HashMap<String, Object>();
		String schoolId=sp.getSchoolId();
		String keyId = sp.getKeyId();
		JSONObject toFront = progressMap.get(processId);
		Object keyPrepDataMap =headStringd+sp.getSchoolId()+processId+".00.prepDataMap";
		 //name,accountId*
		Map<String,String> nameTeacherMap = new HashMap<String, String>();
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Object keyNameTeacherMap= headStringd +sp.getSchoolId()+processId+".00.nameTeacherMap";
		try {
			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
			nameTeacherMap = (Map<String, String>) redisOperationDAO.get(keyNameTeacherMap);
		} catch (Exception e) {
			e.printStackTrace();
			 toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			toFront.put("code", -50);
	        data.put("progress", 100);
	        data.put("msg", "redis缓存失败。");
	        toFront.put("data", data);
	        progressMap.put(processId, toFront);
	        delFileDBAndFileServer(schoolId,headString+keyId);
		}
		List<JSONObject> insertTeacherAccountList = new ArrayList<JSONObject>();
		List<JSONObject> insertTeacherUserList = new ArrayList<JSONObject>();
		List<JSONObject> insertTeacherList = new ArrayList<JSONObject>();
		List<String> teacherAccountNamePhones = new ArrayList<String>();
		Map<String,JSONObject> needUpdatePswAll = new HashMap<String,JSONObject>();
		
		List<JSONObject> updateTeacherAccountList = new ArrayList<JSONObject>();
		
		if(prepDataMap!=null){
			HashMap<Integer, JSONObject> rows = (LinkedHashMap<Integer, JSONObject>) prepDataMap
					.get(processId).get("rowcolMap");
			int interval=0;
			System.out.println("colrowMap:"+rows);
			int q=0 ;
			for (Iterator<Integer> it = rows.keySet().iterator(); it.hasNext();) {
				//设置进度条参数
				q++;
				JSONObject data = new JSONObject();
				float pint=30+40*((float)q/rows.keySet().size());
				data.put("progress",( int)pint);
				data.put("msg", "正在保存数据！");
				toFront.put("data", data);
				progressMap.put(processId, toFront);
				
				try {
					redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				} catch (Exception e) {
					e.printStackTrace();
					data = new JSONObject();
					toFront.put("code", -50);
			        data.put("progress", 100);
			        data.put("msg", "redis缓存失败。");
			        toFront.put("data", data);
			        progressMap.put(processId, toFront);
			        delFileDBAndFileServer(schoolId,headString+keyId);
				}
				int key = it.next();
				JSONObject row = rows.get(key);
				String teacherName=row.getString("teacherName");
				String gender=row.getString("gender");
				String mobilePhone = row.getString("mobilePhone");
				if(StringUtils.isBlank(gender)){
					gender="0"; //不填  
				}else if( "男".equals(gender)){
					gender="1"; //男
				}else{
					gender="2"; //女
				}
				String teacherAccountName = "t"+StringUtil.createRandom(10);
				//判断老师姓名是否重复
				if(nameTeacherMap.containsKey(teacherName)){
					//更新老师姓名、性别、电话号码
					JSONObject teacherAccountObj = new JSONObject();
					teacherAccountObj.put("id", nameTeacherMap.get(teacherName));
					teacherAccountObj.put("gender", gender);
					teacherAccountObj.put("name", teacherName);
					teacherAccountObj.put("mobilePhone", mobilePhone);
					updateTeacherAccountList.add(teacherAccountObj);
				}else{
					JSONObject nu = new JSONObject();
					//创建老师account
					JSONObject teacherAccountObj = new JSONObject();
					String teacherAccountUUID = UUIDUtil.getUUID();
					teacherAccountNamePhones.add(mobilePhone);
					nu.put("accountUUID", teacherAccountUUID);
					teacherAccountObj.put("uuid", teacherAccountUUID);
					teacherAccountObj.put("accountName", teacherAccountName);
					teacherAccountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
					teacherAccountObj.put("accountStatus", 0);
					teacherAccountObj.put("name", teacherName);
					teacherAccountObj.put("gender", gender);
					teacherAccountObj.put("mobilePhone", mobilePhone);
					teacherAccountObj.put("createTime", DateUtil.getTimeAndAddOneSecond(++interval));
					insertTeacherAccountList.add(teacherAccountObj);
					//创建老师user
					JSONObject userTeacher = new JSONObject();
					String teacherUserUUID = UUIDUtil.getUUID();
					nu.put("userUUID", teacherUserUUID);
					userTeacher.put("uuid", teacherUserUUID);
					userTeacher.put("role", T_Role.Teacher.getValue());
					userTeacher.put("avatar", "");
					userTeacher.put("accountId",teacherAccountUUID);//#
					//userStudent.put("createTime", new Date().getTime());
					insertTeacherUserList.add(userTeacher);
					//创建teacher
					JSONObject teacherObj = new JSONObject();
					teacherObj.put("uuid", teacherUserUUID);
					teacherObj.put("schoolId", schoolId);
					insertTeacherList.add(teacherObj);
					
					needUpdatePswAll.put(teacherName, nu);
				}
			}
			returnParam.put("insertTeacherAccountList", insertTeacherAccountList);
			returnParam.put("insertTeacherUserList", insertTeacherUserList);
			returnParam.put("insertTeacherList", insertTeacherList);
			returnParam.put("needUpdatePswAll",needUpdatePswAll);
			returnParam.put("teacherAccountNamePhones", teacherAccountNamePhones);
			returnParam.put("updateTeacherAccountList", updateTeacherAccountList);
		}
		return returnParam;
	}
	
	private int setAllCommonInfo(ImportTaskParameter stt) {
		String xnxq = stt.getXnxq();
		String schoolId = stt.getSchoolId();
		int code = 1;
		//获取全校老师信息
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		List<JSONObject> tList = teacherManageDao.getTeacherListBySchoolId(param);
		Map<String,String> nameTeacherMap = new HashMap<String,String>();
		for(JSONObject teacherObj:tList){
			String name = teacherObj.getString("name");
			String accountId = teacherObj.getString("accountId");
			if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(accountId) && !"0".equals(accountId)){
				nameTeacherMap.put(name, accountId);
			}
		}
		Object keyNameTeacherMap= headStringd +stt.getSchoolId()+stt.getProcessId()+".00.nameTeacherMap";
		try {
			redisOperationDAO.set(keyNameTeacherMap, nameTeacherMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			delFileDBAndFileServer(stt.getSchoolId(),headString+stt.getKeyId());
			e.printStackTrace();
			code = -50;
		}
		return code;
	}
	/**
	 * 将excel读取为数据
	 * 
	 * @param impFrc
	 * @return
	 * @author zhh
	 */
	private JSONObject readExcelToData(String fileId,String schoolId,String keyId) {
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
			delFileDBAndFileServer(schoolId,headString+keyId);
		} catch (IOException e) {
			code = -20;
			e.printStackTrace();
			 delFileDBAndFileServer(schoolId,headString+keyId);
		} catch (Exception e) {
			code = -30;
			e.printStackTrace();
			delFileDBAndFileServer(schoolId,headString+keyId);
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
	 * String[]stuTitle,String[]stuTitleName,String[]stuTitleNeed,String keyId,JSONArray datas, JSONArray mrs, int isMatch,String processId,String schoolId,Hashtable<String, JSONObject> progressMap 
	 * @param datas excel表格数据
	 * @param mrs  匹配结果{sysField:系统字段,excelField：excel字段,mustField：是否必录, 1 必录（前端验证时需注意规则，所有必录项需匹配）
	 * @param isMatch 是否需要手工匹配,1 手工匹配，0 无需匹配
	 * @param processId 进程id
	 * @return JSONOBJ
	 * @author zhh
	 */
	private JSONObject changeData(ImportTaskParameter stt,JSONObject param) {
		String [] stuTitle = stt.getStuTitle();
		String [] stuTitleName = stt.getStuTitleName();
		int [] stuTitleNeed = stt.getStuTitleNeed();
		JSONArray datas = param.getJSONArray("datas");
		Hashtable<String, JSONObject> progressMap = (Hashtable<String, JSONObject>) param.get("progressMap");
		int isMatch = stt.getIsMatch();
		String keyId = stt.getKeyId();
		String schoolId = stt.getSchoolId();
		String processId = stt.getProcessId();
		JSONArray mrs = stt.getMatchResult();
		JSONArray exDatas = new JSONArray();
		JSONObject rs = new JSONObject();
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		HashMap<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
		HashMap<String, String> titleEnMap = new HashMap<String, String>();
		String[][] arr = null;
		if(stuTitle!=null && stuTitleName !=null && stuTitleNeed!=null){
			String[] titles = (String[]) datas.get(0);//得到excel第一行表头
			//存储导入excel表头arr[0][]  是否必填arr[1][]  映射的系统字段：中文名称arr[2][]  映射实体字段：对应的英文名称 arr[3][]
			arr = new String[4][titles.length];
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
		Object keyArr = headStringd+schoolId+processId+".00.arr";
		Object keyPrepDataMap = headStringd+schoolId+processId+".00.prepDataMap";
		try {
			redisOperationDAO.set(keyArr, arr[0], CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyPrepDataMap, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = new JSONObject();
			toFront.put("code", -50);
            data.put("progress", 100);
            data.put("msg", "redis缓存失败。");
            toFront.put("data", data);
            progressMap.put(processId, toFront);
            delFileDBAndFileServer(schoolId,headString+keyId);
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
	private JSONObject checkImpData(JSONArray repeatMrows,String keyId,HashMap<Integer, JSONObject> pureDatas,JSONArray mrs, int isMatch, String processId,ImportTaskParameter sp,Hashtable<String, JSONObject> progressMap) {
		String schoolId=sp.getSchoolId();
		// excel验证部分
		JSONObject rs = new JSONObject();
		// 导入进度
		JSONObject toFront = progressMap.get(processId);
		JSONObject data = new JSONObject();
		
		data.put("progress", 10);
		data.put("msg", "正在校验excel数据,匹配表头完成,校验数据");
		toFront.put("data", data);

		Object keyNameTeacherMap= headStringd +schoolId+processId+".00.nameTeacherMap";
		Object keyPrepDataMap = headStringd+schoolId+processId+".00.prepDataMap";
		Object keyArr = headStringd+schoolId+processId+".00.arr";
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Map<String,String> nameTeacherMap = new HashMap<String,String>();
		String [] arr = null;
 		try {
 			arr = (String[]) redisOperationDAO.get(keyArr);
 			nameTeacherMap  =  (Map<String, String>)redisOperationDAO.get(keyNameTeacherMap);
 			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
 		} catch (Exception e) {
			e.printStackTrace();
			toFront.put("code", -50);
            data.put("progress", 100);
            data.put("msg", "redis缓存失败。");
            toFront.put("data", data);
            progressMap.put(processId, toFront);
            delFileDBAndFileServer(schoolId,headString+keyId);
		}
		if(prepDataMap==null){
			try {
				throw new Exception("prepDataMap为空");
			} catch (Exception e) {
				e.printStackTrace();
				toFront.put("code", -50);
		        data.put("progress", 100);
		        data.put("msg", "redis缓存失败。");
		        toFront.put("data", data);
		        delFileDBAndFileServer(schoolId,headString+keyId);
		        progressMap.put(processId, toFront);
			}
		}
		//progNum = 6;
		JSONArray wrongMsg = new JSONArray();
		JSONArray exDatas = new JSONArray();
		JSONObject preData = prepDataMap.get(processId);
		
		Map<String,String> teacherNameMap =(Map<String, String>) preData.get("teacherNameMap");
		//name-rowNums     防止excel名称重复(重复则多条都要显示出来)
		Map<String,LinkedHashSet<String>> excelNameMap =(Map<String, LinkedHashSet<String>>) preData.get("excelNameMap");
		
		if(teacherNameMap==null){
			teacherNameMap = new HashMap<String,String>();
		}
		if(excelNameMap==null){
			excelNameMap = new LinkedHashMap<String,LinkedHashSet<String>>();
		}
		String[] impHead = (String[]) preData.get("impHead");		
		progressMap.put(processId, toFront);
		Object keyProgressMap = headStringd+schoolId+processId+".00.progressMap";
		try {
			redisOperationDAO.set(keyProgressMap, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			e.printStackTrace();
		    toFront.put("code", -50);
	        data.put("progress", 100);
	        data.put("msg", "redis缓存失败。");
	        toFront.put("data", data);
	        progressMap.put(processId, toFront);
	        delFileDBAndFileServer(schoolId,headString+keyId);
		}

		HashMap<Integer, JSONObject> rowcolMap= new LinkedHashMap<Integer, JSONObject>(); //待插入的数据保存
		if (preData.containsKey("rowcolMap")) 
		{
			rowcolMap = (LinkedHashMap<Integer, JSONObject>)preData.get("rowcolMap");
		} 
		else 
		{
			preData.put("rowcolMap", rowcolMap);
		}
		
		int index = 0;
		boolean isNameRepeat=false;
		Set<String> isRepeatMsg = new HashSet<String>(); 
		String value = "";  //单步时，用于去除已修改的name的excelNameMap中的值
		String oldValue = "";
		Map<Integer,JSONObject> isExcelRepeatMap = new HashMap<Integer,JSONObject>(); //是否已匹配重复
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
			String teacherName = pd.containsKey("teacherName") ? pd.getString("teacherName"):null;

			//excel验证gradeName数据合理性
			String title=pd.getString("teacherNameName");
			if (teacherName == null || teacherName.trim().length() < 1) {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", teacherName);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}else{
				if(teacherName.trim().length()>20){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", teacherName);
					wsg.put("err", "输入值不合法！");
					isWsg=true;
					mrows.add(wsg);
				}else {
					LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
					if(excelNameMap.containsKey(teacherName.trim()) ){
							repeatNameList = excelNameMap.get(teacherName.trim());
							if(repeatNameList!=null && !repeatNameList.contains(rowNum+"") ){
								repeatNameList.add(rowNum+"");
								JSONObject json = new JSONObject();
								//json.put("excelRepeatKey", teacherName);
								json.put("repeatTitle", title);
								json.put("isRepeat", true);
								json.put("teacherName", teacherName);
								isExcelRepeatMap.put(rowNum, json);
							}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
								repeatNameList.add(rowNum+"");
								excelNameMap.put(teacherName.trim(), repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(rowNum, json);
							}else{
								JSONObject wsg = new JSONObject();
								wsg.put("title", title);
								wsg.put("oldValue", teacherName);
								wsg.put("err", "excel中重复！");
								isWsg=true;
								mrows.add(wsg);
							}
					}else{
						repeatNameList.add(rowNum+"");
						excelNameMap.put(teacherName.trim(), repeatNameList);
						JSONObject json = new JSONObject();
						json.put("isRepeat", false);
						isExcelRepeatMap.put(rowNum, json);
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
				if("teacherName".equals(impHead[j])){
					continue;
				}
				String tit = impHead[j];
				String val = pd.containsKey(tit) ? pd.getString(tit):"" ;
				String titName = pd.getString(tit + "Name");
				JSONObject wsg = new JSONObject();
				wsg.put("title", titName);
				wsg.put("titleEnName", tit);
				wsg.put("oldValue", val);

			    if (tit.equalsIgnoreCase("gender")){
					val = val.replaceAll(" ", "").trim();
					if(StringUtils.isNotEmpty(val))
					{
						if (!"男".equals(val)&&!"女".equals(val)) {
							noRecord=true;
							err= "输入值不合法！";
							isWsg=true;
						}
					}
				}else if (tit.equalsIgnoreCase("mobilePhone")){
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotEmpty(val))
					{
						if (!StringUtil.isChinaPhoneLegal(val)) {//   !StringNumTool.isInteger(val)|| val.length()!=11 || "-".equals(val.substring(0, 1))
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
				if( !isNameRepeat || isWsg)
				{
						teacherNameMap.remove(teacherName.trim());
				}
					
				wrongMsg.add(wmsg);
			} 
			else 
			{
				wmsg = null;
			}
			
			if(mrows.size()==0&&err.length()==0&&!noRecord )
			{
				rowcolMap.put(rowNum, pd);//将该行数据存入
			}
			data.put("progress", 10+(20*(index/pureDatas.keySet().size())));
		}//end of for

		 for (Map.Entry<Integer, JSONObject> entry : isExcelRepeatMap.entrySet()) {
			 Integer rowNum= entry.getKey();
			JSONObject isExcelRepeatJSON = entry.getValue();
			Boolean isExcelRepeat = isExcelRepeatJSON.getBooleanValue("isRepeat");
			if(!isExcelRepeat){continue;}
			String repeatTitle = isExcelRepeatJSON.getString("repeatTitle");
			//String excelRepeatKey = isExcelRepeatJSON.getString("excelRepeatKey");
			String teacherName = isExcelRepeatJSON.getString("teacherName");
			LinkedHashSet<String> repeatRowNums = excelNameMap.get(teacherName.trim());
			for(String repeatRowNum:repeatRowNums){
				boolean isFind = false; //是否找到
				for(int g = 0;g<wrongMsg.size();g++){
					JSONObject wmsg1 = (JSONObject) wrongMsg.get(g);
					int rowN = wmsg1.getIntValue("row");
					if(!repeatRowNum.equals(rowN+"") || rowNum<rowN){continue;}
					if(isRepeatMsg.contains(teacherName.trim()+"_"+repeatRowNum)){
						continue;
					}
					JSONArray mrows1 = wmsg1.getJSONArray("mrows");
					if(mrows1==null){
						mrows1=new JSONArray();
					}
					if(repeatRowNum.equals(rowN+"")){
						JSONObject wsg1 = new JSONObject();
						wsg1.put("title", repeatTitle);
						wsg1.put("oldValue", teacherName);
						wsg1.put("err", "excel中重复！");
						mrows1.add(wsg1);
						isFind=true;
						isRepeatMsg.add(teacherName.trim()+"_"+repeatRowNum);
						if(repeatMrows==null){
							rowcolMap.remove(Integer.parseInt(repeatRowNum));
						}
					}
				}
				//第一次的情况 即wrongMsg为空
				if(!isFind && null!=(excelNameMap.get(teacherName.trim())) && !isRepeatMsg.contains(teacherName.trim()+"_"+repeatRowNum)){
					JSONObject wmsg1 = new JSONObject();
					JSONArray mrows1=new JSONArray();
					JSONObject wsg1 = new JSONObject();
					wsg1.put("title", repeatTitle);
					wsg1.put("oldValue", teacherName);
					wsg1.put("err", "excel中重复！");
					mrows1.add(wsg1);
					wmsg1.put("row", repeatRowNum);
					wmsg1.put("mrows", mrows1);
					wrongMsg.add(wmsg1);
					isRepeatMsg.add(teacherName.trim()+"_"+repeatRowNum);
					if(repeatMrows==null){
						rowcolMap.remove(Integer.parseInt(repeatRowNum));
					}
				}
			}
			
		}
		 if(repeatMrows!=null){
			 for(String m:isRepeatMsg){
				 String [] str = m.split("_");
				 if(StringUtils.isNotBlank(str[0])){
					 excelNameMap.put(str[0],null);
				 }
			 }
		 }
		 if(repeatMrows==null){
			 excelNameMap.clear();
		 }
		 //MapRemoveNullUtil.removeNullValue(excelNameMap);
		preData.put("rowcolMap", rowcolMap);
		preData.put("teacherNameMap", teacherNameMap);
		preData.put("excelNameMap", excelNameMap);
		if (wrongMsg != null && wrongMsg.size() > 0)
		{
			rs.put("ckRs", false);
			//wrongMsg排序
			Map<String,Integer> rule = new LinkedHashMap<String,Integer>();
			if(arr!=null){
				for(int i=0;i<arr.length;i++){
					rule.put(arr[i], i+1);
				}
			}
			for(int g = 0;g<wrongMsg.size();g++){
				JSONObject wmsg1 = (JSONObject) wrongMsg.get(g);
				JSONArray mrows1 = wmsg1.getJSONArray("mrows");
				if(mrows1!=null){
					SortUtil.sortByStudentImportRule(mrows1,rule);
				}
			}
			SortUtil.sortWsgNormalJSONList(wrongMsg,"row");
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
			e.printStackTrace();
		    toFront.put("code", -50);
	        data.put("progress", 100);
	        data.put("msg", "redis缓存失败。");
	        toFront.put("data", data);
	        progressMap.put(processId, toFront);
	        delFileDBAndFileServer(schoolId,headString+keyId);
		}
		return rs;
	}
	/**
	 * 获取excel文件,检查文件格式
	 * 
	 * @param school
	 *            学校代码
	 * @param file
	 *            MultipartFile
	 * @return JSONObject
	*//*
	public JSONObject uploadExcel(JSONObject param) {
		JSONObject result = param.getJSONObject("result");
		String code = "0";
		String msg = ImportOutputMessage.getDescByCode("0");
		String fileId = "";		
		String schoolId = param.getString("schoolId");
		String keyId = "teacherImport_" + param.getString("uuid");
		File df = (File)param.get("file");
		try{			
			String prefix = param.getString("prefix");
			String fileName = param.getString("fileName");
			Workbook workbook = null;
			if (prefix.equalsIgnoreCase("xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(df));
			} else if (prefix.equalsIgnoreCase("xlsx")) {
				workbook = new XSSFWorkbook(new FileInputStream(df));
			} else {
				code = "-2";
			}
			Sheet sheet = workbook.getSheetAt(0);
			int rows = sheet.getPhysicalNumberOfRows();

			if (rows > 0) {// 有数据时才处理
				Row row = sheet.getRow(0);
				int cellNum = row.getPhysicalNumberOfCells();
				//
				String[] tempTeacherImpExcTitle = new String[cellNum];
				code = "1";
				// 判断是否需要进行手工字段匹配
				for(int i = 0; i < cellNum; i++) 
				{
					String excelVal = row.getCell(i).getStringCellValue();
					excelVal = StringUtil.replaceChar(excelVal);
					if (StringUtils.isNotEmpty(excelVal)
							&& inArrayNum(excelVal, sysTitleName)==0)
					{
						code = "2";											
					}
					// 放置临时保存目录
					tempTeacherImpExcTitle[i] = excelVal;
				}
				if(Integer.parseInt(code) >= 0)
				{
					fileId = fileUtil.uploadFile(df,fileName); 
					JSONObject param_add = new JSONObject();
					param_add.put("schoolId", schoolId);
					param_add.put("keyId", keyId);
					param_add.put("fileId", fileId);
					fileImportDao.addFile(param_add);
					result.put("keyId", keyId);
					result.put("tempTitle", tempTeacherImpExcTitle);
				}
			} else {
				code = "-2102";
			}						
			workbook.close();
		} catch (Exception e) {
			code = "-2101";
			if(null!=fileId && StringUtils.isNotEmpty(fileId))
			{
				try {
					fileUtil.deleteFile(fileId);
				}catch (Exception ex) {
					code = "-3";				
				}
				JSONObject param_delete = new JSONObject();
				param_delete.put("schoolId", schoolId);
				param_delete.put("keyId", keyId);
				fileImportDao.deleteFile(param_delete);
			}
		}finally{
			if(null!=df)
			{
				df.delete();
			}
			msg = ImportOutputMessage.getDescByCode(code);
			result.put("code", code);
			result.put("msg", msg);
		}
		return result;
	}

	@Override
	public JSONObject getExcelHead(JSONObject param) {
		JSONObject excelHead = new JSONObject();
		JSONObject rsult = new JSONObject();
		JSONObject moduleField = new JSONObject();
		try{        
	        // 获取session中保存的临时表头
	        String[] tmpTit = (String[])param.get("tempTitle");
	        if (tmpTit != null) {
	            // 开始拼装返回数据结构
	            excelHead.put("total", tmpTit.length);
	            JSONArray rows = new JSONArray();
	            excelHead.put("rows", rows);
	            for (int i = 0; i < tmpTit.length; i++) {
	                JSONObject obj = new JSONObject();
	                obj.put("field", StringUtil.replaceChar(tmpTit[i]));
	                rows.add(obj);
	            }
	        } else {
	            excelHead.put("total", 0);
	        }
	        
	        // 直接使用系统表头,开始拼装返回数据结构
	        moduleField.put("total", sysTitleName.length);
	        JSONArray sysrows = new JSONArray();
	        moduleField.put("rows", sysrows);
	        for (int i = 0; i < sysTitleName.length; i++) {
	            JSONObject obj = new JSONObject();
	            obj.put("field", sysTitleName[i]);
	            obj.put("sysfield", sysTitleNeed[i]);
	            sysrows.add(obj);
	        }
	        rsult.put("excelHead", excelHead);
	        rsult.put("moduleField", moduleField);
		} catch (Exception e) {
			System.out.println("message: " + e.getMessage());
		}
		return rsult;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject startImportTask(JSONObject param) {
		JSONObject result = new JSONObject();
		String code = "106",msg = ImportOutputMessage.getDescByCode("106");	     
	 	try{
	 		String progressId = param.getString("progressId");
	 		String progressMapKey = param.getString("progerssMapKey");
	 		Hashtable<String, JSONObject> progressMap = (Hashtable<String, JSONObject>)param.get("progressMap");
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			SubProcess sp = new SubProcess(progressId,progressMapKey);
	        sp.start();
	        code = "107";
	 	} catch (Exception ex) {
	 		code = "-4";		
		}finally{
			msg = ImportOutputMessage.getDescByCode(code);
			result.put("code", code);
		 	result.put("msg", msg);
		}
        return result;
	}
	
    class SubProcess extends Thread {		
		private String processId;
		private String progressMapKey;
		
		public  SubProcess(String processId,String progressMapKey){
			this.processId = processId;
			this.progressMapKey = progressMapKey;
		}
		@SuppressWarnings("unchecked")
		public void run() {
			// excel导入处理开始
			String fileId = "";
			String schoolId = "";
			String keyId = "";
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
			try {
				 Object progressMapObj = redisOperationDAO.get(progressMapKey);
				 if(null != progressMapObj)
				 {
					progressMap = (Hashtable<String, JSONObject>) progressMapObj;
				 }else{
					JSONObject data = new JSONObject();
					JSONObject toFront= new JSONObject();
					toFront.put("code", -50);
					data.put("progress", 100);
					data.put("msg", ImportOutputMessage.getDescByCode("-7"));
					toFront.put("data", data);
					progressMap.put(processId, toFront);
					redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					return;
				 }
				 // excel导入处理开始
				 JSONObject sp = (JSONObject) progressMap.get(processId).get("taskParam");
				 schoolId = sp.getString("schoolId");
				 keyId = sp.getString("keyId");
				 JSONObject param_get = new JSONObject();
				 param_get.put("schoolId", schoolId);
				 param_get.put("keyId", keyId);
				 fileId = fileImportDao.getFileBy(param_get);				
				 JSONObject toFront = progressMap.get(processId);
				 JSONObject data = toFront.getJSONObject("data");				 
				 // 开始读取数据
				 toFront.put("code", 1);    
				 data.put("progress", 5);
				 data.put("msg", ImportOutputMessage.getDescByCode("101"));
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 JSONObject readRs = readExcelToData(fileId,processId);
				 List<String[]> datas = (List<String[]>)readRs.get("datas");
				 // 开始封装表头
				 toFront.put("code", 1);    
				 data.put("progress", 15);
				 data.put("msg", ImportOutputMessage.getDescByCode("102"));
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 String[][] heads = handleHeadData(datas, sp, processId);
				 // 开始校验数据
				 toFront.put("code", 1);    
				 data.put("progress", 25);
				 data.put("msg", ImportOutputMessage.getDescByCode("103"));
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 JSONObject ckrs = checkImpData(datas, heads);
				 if (ckrs.getBooleanValue("ckRs")) {
					 // 处理表格数据
					 toFront.put("code", 1);    
					 data.put("progress", 35);
					 data.put("msg", ImportOutputMessage.getDescByCode("104"));
					 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					 JSONObject result = changeData(datas, heads, schoolId);
					 List<JSONObject> teacherList = (List<JSONObject>)result.get("rowDatas");
					 List<String> delIdList = (List<String>)result.get("delIdList");
					 // 开始入库
					 toFront.put("code", 1);
					 data.put("progress", 50);
					 data.put("msg", ImportOutputMessage.getDescByCode("105"));
					 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());					 
					 if (CollectionUtils.isNotEmpty(teacherList)) 
					 {
					     addImportTeachers(teacherList,schoolId,delIdList);															
						 toFront.put("code", 2);
	                     data.put("progress", 100);
	                     data.put("msg", "导入成功，共计导入"+ teacherList.size() +"条信息记录！");
					 }					
				 }else {
					 toFront.put("code", -2);
					 data.put("progress", 100);
					 data.put("msg", ImportOutputMessage.getDescByCode("-5"));
					 data.put("total", datas.size()-1);
					 data.put("validateMsg", ckrs.get("validateMsg"));
				 }	
			 }catch(Exception e){
				 JSONObject toFront= new JSONObject();
				 JSONObject data = new JSONObject();  
				 toFront.put("code", -1);
				 data.put("progress", 100);
				 data.put("msg", ImportOutputMessage.getDescByCode("-6"));
				 toFront.put("data", data);
				 progressMap.put(processId, toFront);
			 }finally{
				 // excel导入处理结束
				 try {
					  redisOperationDAO.set(progressMapKey, progressMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					  //在删除表中的记录
					  if (null != fileId && StringUtils.isNotEmpty(fileId)) {
						  fileUtil.deleteFile(fileId);
					  }
					  if (null != keyId && StringUtils.isNotEmpty(keyId)) {
						  JSONObject param_delete = new JSONObject();
						  param_delete.put("schoolId", schoolId);
						  param_delete.put("keyId", keyId);
						  fileImportDao.deleteFile(param_delete);
					  }
				  } catch (Exception e) {
					  JSONObject toFront= new JSONObject();
					  JSONObject data = new JSONObject();  
					  toFront.put("code", -1);
					  data.put("progress", 100);
					  data.put("msg", ImportOutputMessage.getDescByCode("-12"));
					  toFront.put("data", data);
					  progressMap.put(processId, toFront);
				  }
			}	
	    }	
	}
    
    private void addImportTeachers(List<JSONObject> teacherList,String schoolId,List<String> idsList) {
    	List<Long> accountIdList = new ArrayList<Long>();
    	List<Long> teacherIdList = new ArrayList<Long>();
    	for(String value : idsList)
    	{
    		String[] values = value.split(",");
    		accountIdList.add(Long.parseLong(values[0]));
    		teacherIdList.add(Long.parseLong(values[1]));
    		teacherManageDao.deleteInfoByAccountIds(accountIdList);
    		teacherManageDao.deleteInfoByTeacherIds(teacherIdList);
    	}	
    	for(JSONObject account : teacherList)
    	{
    		account.put("uuid", UUID.randomUUID().toString());  
    		JSONObject json = new JSONObject();
    		json.put("schoolId", schoolId);
    		JSONObject at = teacherManageDao.getNamesFromAccount(json);			
    		String mobilePhone = account.getString("mobilePhone");		
    		String accountName = "";
    		if (StringUtils.isNotEmpty(mobilePhone))
    		{
    			accountName = mobilePhone;
    			account.put("mobilePhone", mobilePhone);
    		}else{
    		    accountName = SerialGenerater.getInstance().getNextSerial("t");
    		}		
    		String accountNames = "";
    		if (null != at 
    				&& StringUtils.isNotEmpty(at.getString("names"))){
    			accountNames = at.getString("names").toLowerCase();
    		}
    		String accLowerName = accountName.toLowerCase();
    		while(accountNames.contains(accLowerName)){
    			accountName = SerialGenerater.getInstance().getNextSerial("t");
    		}
    		account.put("accountName",accountName);
			int lenght = accountName.length();
			String pwd = accountName.substring(lenght-6, lenght);
			account.put("status", 1);
			account.put("accountStatus", 0);	
			account.put("createTime", getDateMilliFormat(new Date()));
			account.put("autoCreateTime", new Date());
			int sum = teacherManageDao.addAccountByTeacher(account);
			if (sum > 0)
			{
				JSONObject user = new JSONObject();
				user.put("uuid", UUID.randomUUID().toString());
				user.put("role", 1);
				long account_id = Long.parseLong(account.getString("id"));
				JSONObject pw = new JSONObject();
				pw.put("accountId", account_id);
				String passwd = MD5Util.getMD5String(MD5Util.getMD5String(pwd)+account_id);
				pw.put("pwd", passwd);
				teacherManageDao.resetTeacherPassword(pw);
				user.put("status", 1);
				user.put("accountId", account_id);
				user.put("createTime", getDateMilliFormat(new Date()));
				user.put("autoCreateTime", new Date());
				sum = teacherManageDao.addUserByTeacher(user);
				if (sum > 0)
				{
					JSONObject teacher = new JSONObject();
					teacher.put("teacherId", Long.parseLong(user.getString("id")));
					teacher.put("autoCreateTime", new Date());
					teacher.put("schoolId", schoolId);
					teacherManageDao.addTeacher(teacher);
				}
				
			}   		
    	} 	  	
    }
    
    *//**
	 * 将excel读取为数据
	 * 
	 * @param field,processId
	 * @return JSONObject
	 *//*
    private JSONObject readExcelToData(String fileId, String processId) throws Exception{
		JSONObject rs = new JSONObject();
		List<String[]> datas = new ArrayList<String[]>();
		String[] headTitleName = null;
		// 解析excel 封装对象
		Workbook workbook = null;
	
        File file = null;		
		String uuid=UUID.randomUUID().toString();
		fileUtil.downloadFile(fileId, uuid);
		file=new File(uuid);			
		workbook=WorkbookFactory.create(file);
		Sheet sheet = workbook.getSheetAt(0);
		int rows = sheet.getLastRowNum();
		// 转换器 一般poi取数字格式需转换
		DecimalFormat df = new DecimalFormat("0");

		if (rows > 0) {
			int cols = sheet.getRow(0).getPhysicalNumberOfCells();
			headTitleName = new String[cols];
			for (int i = 0; i < rows + 1; i++) {
				if (sheet.getRow(i) == null) {
					continue;
				}
				String[] temp = new String[cols];
				boolean isTrueNull = true;
				for (int j = 0; j < cols; j++) {
					if (sheet.getRow(i).getCell(j) != null
							&& sheet.getRow(i).getCell(j).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
						Cell cell = sheet.getRow(i).getCell(j);
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_NUMERIC:
							temp[j] = StringUtil.replaceChar(df.format(cell.getNumericCellValue()));
							break;
						case HSSFCell.CELL_TYPE_STRING:
							temp[j] = StringUtil.replaceChar(cell.getRichStringCellValue().getString());
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							try {
								temp[j] = StringUtil.replaceChar(String.valueOf(cell.getStringCellValue()));
							} catch (IllegalStateException e) {
								temp[j] = StringUtil.replaceChar(String.valueOf(cell.getNumericCellValue()));
							}
							break;
						}
						isTrueNull = false;
					} else {
						temp[j] = "";
					}
					if (i == 0){
						headTitleName[j] = temp[j];
					}
				}
				if (!isTrueNull) {
					datas.add(temp);
				}
			}
		}
		workbook.close();
		rs.put("datas", datas);
		rs.put("headTitleName", headTitleName);
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		prepDataMap.put(processId, rs);
		String prepDataMapKey="teacherImport."+processId+".prepDataMap";
		redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
		return rs;
	
	}

	*//**
	 * @param datas
	 * @param sp 
	 * @param processId
	 * @return JSONObject
	 * @throws Exception 
	*//*
    @SuppressWarnings("unchecked")
	public String[][] handleHeadData(List<String[]> datas, JSONObject sp, String processId) throws Exception{
		int isMatch = sp.getIntValue("isMatch");
		String[] titles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] heads = new String[3][titles.length];
		// 无需手工匹配
		if (isMatch == 0) {
			// 封装表头
			for (int i = 0; i < titles.length; i++) {
				 heads[0][i] = titles[i];
				 int needIndex = strIndexInArray(titles[i], sysTitleName);
				 if (needIndex >= 0) {
					 // 在系统字段中能找到
					 heads[1][i] = sysTitleNeed[needIndex] + "";
					 heads[2][i] = sysTitleName[needIndex];
				} else {
					 // 在系统字段中找不到 标记为不录入
					 heads[1][i] = "-1";
					 heads[2][i] = "none";
				}
			}
		} else {
			// 需要手工匹配的 根据匹配关系封装表头
			// 封装表头
			JSONArray mrs = (JSONArray)sp.get("matchResult");
			for (int i = 0; i < titles.length; i++) {
				 String sysTit = "none";
				 for (int j = 0; j < mrs.size(); j++) {
					  JSONObject obj = mrs.getJSONObject(j);
					  if (titles[i]!=null&&titles[i].equalsIgnoreCase(obj.getString("excelField"))) {
						  sysTit = obj.getString("sysField");
						  continue;
					  }
				 }
				 heads[0][i] = titles[i];
				 int needIndex = strIndexInArray(sysTit, sysTitleName);
				 if (needIndex >= 0) {
					 // 在系统字段中能找到
					 heads[1][i] = sysTitleNeed[needIndex] + "";
					 heads[2][i] = sysTitleName[needIndex];
				 } else {
					// 在系统字段中找不到 标记为不录入
					 heads[1][i] = "-1";
					 heads[2][i] = "none";
					 heads[0][i] = null;
				 }
			}
		}
		String prepDataMapKey = "teacherImport."+ processId +".prepDataMap";
		Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
		if(null!=prepDataMapObj)
		{
			Hashtable<String, JSONObject> prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			JSONObject rs = prepDataMap.get(processId);
			rs.put("heads", heads);
			redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
		}
		return heads;
	}
    
    *//**
     * 检查导入数据
	 * @param commonObj 
     * 
     * @return
    *//*
    public JSONObject checkImpData(List<String[]> datas, String[][] heads) {
    	List<String> nameList = new ArrayList<String>();
		JSONObject result = new JSONObject();
		JSONObject validateMsg = new JSONObject();
		JSONArray rows = new JSONArray();
		for(int i = 1; i < datas.size(); i++){
			JSONArray mrows = new JSONArray();
			JSONObject row = new JSONObject();
			row.put("row", i + 1);
			String[] title = datas.get(0);
			String[] array = datas.get(i);
			for(int j = 0; j < array.length; j++){			
				int isIn = Integer.parseInt(heads[1][j]);
				if (isIn > 0){	
					String field = array[j];
					if (StringUtils.isEmpty(field)){
						JSONObject wsg = new JSONObject();
						wsg.put("title", title[j]);
						wsg.put("oldValue", field);
						wsg.put("err", "教师姓名不能为空!");
						mrows.add(wsg);
					}else if(nameList.contains(field)){
						JSONObject wsg = new JSONObject();
						wsg.put("title", title[j]);
						wsg.put("oldValue", field);
						wsg.put("err", "表格中的教师姓名不能重复!");
						mrows.add(wsg);						
					}else{
						nameList.add(field);
					}
				}								
			}
			if (mrows.size() > 0){
				row.put("mrows",mrows);
				rows.add(row);
			}else{
				datas.set(i, array);
			}					
		}		
		if (rows.size() > 0) {
			validateMsg.put("rows", rows);
			validateMsg.put("total", rows.size());
			result.put("validateMsg", validateMsg);
			result.put("ckRs", false);
		} else {
			result.put("ckRs", true);
		}
		return result;
	}

    *//**
	 * 转换表格数据
	 * 
	 * @param datas
	 * @param array
	 * @param commonObj 
	 * @param stt
	 * @return
	 *//*
	public JSONObject changeData(List<String[]> datas, String[][] heads, String schoolId) {		
    	Map<String,String> namesMap = new HashMap<String,String>();
    	List<String> delIdList = new ArrayList<String>();
    	List<JSONObject> list = teacherManageDao.getNamesMapBySchoolId(Long.parseLong(schoolId));
    	if (CollectionUtils.isNotEmpty(list)){
    		for(JSONObject j : list)
    		{
    			namesMap.put(j.getString("name"),j.getString("ids"));
    		}
    	}				
		List<JSONObject> rowDatas = new ArrayList<JSONObject>();
		for (int i = 1; i < datas.size(); i++) {
			 if(null == datas.get(i)) continue;			
			 String[] cell = (String[]) datas.get(i); 
			 String name = cell[0];
			 if (StringUtils.isNotEmpty(name))
			 {
				 JSONObject data = new JSONObject();
				 data.put("name", name);
				 for (int j = 1; j < heads[2].length; j++) 
				 {
					  int isIn = Integer.parseInt(heads[1][j]);
					  if (isIn != -1 && StringUtils.isNotEmpty(cell[j])) 
					  {					
						  String value = cell[j]; 						  
						  if (heads[2][j].equals("\u6027\u522B")) 
						  {
							  if (value.contains("\u7537")){
								  data.put("gender", T_Gender.Male.getValue());
							  }else if(value.contains("\u5973")){
								  data.put("gender", T_Gender.Female.getValue());
							  }else{
								  data.put("gender", 0);
							  }
						  } else if (heads[2][j]
								.equals("\u624B\u673A\u53F7\u7801")) 
						  {
							  data.put("mobilePhone", value);
						  }
					  }
				 }
				 rowDatas.add(data);
				 if (namesMap.containsKey(name))
				 {
					 delIdList.add(namesMap.get(name));
				 }
			 }
		}
	    // 封装返回数据
		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		rs.put("delIdList", delIdList);
		return rs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject importProgress(JSONObject param) {
		JSONObject result = new JSONObject();
        try{
        	Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
    		String progressMapKey= param.getString("progressMapKey");
    		String progressId = param.getString("progressId");
    		Object progressMapObj = redisOperationDAO.get(progressMapKey);
    		if (null!=progressMapObj)
    		{
    			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
    		}
    		else
    		{
    			JSONObject data = new JSONObject();
    			data.put("progress", 100);
    			data.put("msg", ImportOutputMessage.getDescByCode("-7"));
    			result.put("code", -50);
    			result.put("data", data);
    			progressMap.put(progressId, result);
    		}
    		JSONObject obj = progressMap.get(progressId);
    		if (progressMap!=null && obj!=null){
    			if (obj.getIntValue("code")==-50)
    			{
    				String schoolId = param.getString("schoolId");
    				String keyId = param.getString("keyId");
    				JSONObject param_get = new JSONObject();
    				param_get.put("schoolId", schoolId);
    				param_get.put("keyId", keyId);
    				String fileId = fileImportDao.getFileBy(param_get);
    				if (null!=fileId && StringUtils.isNotEmpty(fileId))
    				{
    					fileUtil.deleteFile(fileId);
    				}
    				if (null!=keyId && StringUtils.isNotEmpty(keyId))
    				{
    					JSONObject param_delete = new JSONObject();
    					param_delete.put("schoolId", schoolId);
    					param_delete.put("keyId", keyId);
    					fileImportDao.deleteFile(param_delete);
    				}
    			}
    			result.put("code", obj.get("code"));
    			result.put("data", obj.get("data"));
    		}else{
    			result.put("code", "-8");
    			result.put("data", ImportOutputMessage.getDescByCode("-8"));
    		}
    		redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
        }catch(Exception e){
        	result.put("code","-9");
        	result.put("data", ImportOutputMessage.getDescByCode("-9"));
        }
		return result;	
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject singleDataCheck(JSONObject param){
		JSONObject result = new JSONObject();
		String prepDataMapKey = param.getString("prepDataMapKey");
		int row = param.getIntValue("row");
		String progressId = param.getString("progressId");
		JSONArray mrows = param.getJSONArray("mrows");
		int code = param.getIntValue("code");
		String progressMapKey = param.getString("progressMapKey");
		try{			
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
			if(null != prepDataMapObj)
			{
				prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			}
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if(null!=progressMapObj)
			{
				progressMap=(Hashtable<String, JSONObject>) progressMapObj;
			}				
			if(null==progressMapObj||null==prepDataMapObj)
			{
				JSONObject data = new JSONObject();
				data.put("msg", ImportOutputMessage.getDescByCode("-7"));
				data.put("progress", 100);
				result.put("code", 1);	
				result.put("data", data);
				progressMap.put(progressId, result);
				redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				return result;
			}
			List<String[]> datas = (List<String[]>) prepDataMap.get(progressId).get("datas");
			String[] headTitleName = (String[]) prepDataMap.get(progressId).get("headTitleName");
			String[][] heads = (String[][]) prepDataMap.get(progressId).get("heads");			
			if (code == -1) {				
				datas.set(row-1, null);	
				result.put("code", 1);
			} else if (code == 1) {	
				List<String[]> pureDatas = new ArrayList<String[]>();
				String[] sd = datas.get(row-1);
				if (mrows!=null) {
					for(int i = 0; i < mrows.size(); i++) {
						JSONObject o = mrows.getJSONObject(i);
						String title = StringUtil.replaceChar(o.getString("title"));
						int index = strIndexInArray(title,headTitleName);
						sd[index] = StringUtil.replaceChar(o.getString("value"));
					}
				}
				pureDatas.add(datas.get(0));
				pureDatas.add(sd);
				
				JSONObject cr = checkImpData(pureDatas, heads);				
				if (cr.getBooleanValue("ckRs")) {
					result.put("code", 1);
					JSONObject data = new JSONObject();
					data.put("msg", "校验通过！");
					result.put("data", data);
				} else {
					result.put("code", -1);
					result.put("mrows",
					cr.getJSONObject("validateMsg").getJSONArray("rows")
					.getJSONObject(0).getJSONArray("mrows"));	
				}	
			}
			redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());			
		}catch(Exception ex){
			result.put("code","-11");
        	result.put("data", ImportOutputMessage.getDescByCode("-11"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject continueImport(JSONObject param) {
		String prepDataMapKey = param.getString("prepDataMapKey");
		String schoolId = param.getString("schoolId");
		String progressId = param.getString("progressId");
		String progressMapKey = param.getString("progressMapKey");
		List<JSONObject> targetList = null;
		List<String> delIdList = null;
		String code = "6",msg = ImportOutputMessage.getDescByCode(code);
		JSONObject result = new JSONObject();
		try{	
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
			if(null != prepDataMapObj)
			{
				prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			}
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
			Object progressMapObj = redisOperationDAO.get(progressMapKey);
			if(null!=progressMapObj)
			{
				progressMap=(Hashtable<String, JSONObject>) progressMapObj;
			}
			if(null==progressMapObj||null==prepDataMapObj)
			{
				JSONObject toFront = new JSONObject();
				JSONObject data = new JSONObject();
				data.put("progress", 100);
				data.put("msg", ImportOutputMessage.getDescByCode("-7"));
				toFront.put("code", -50);
				toFront.put("data", data);
				progressMap.put(progressId, toFront);
				redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				return toFront;
			}
			List<String[]> datas = (List<String[]>) prepDataMap.get(progressId).get("datas");
			String[][] heads = (String[][]) prepDataMap.get(progressId).get("heads");
			// 处理表格数据
			JSONObject toFront = progressMap.get(progressId);
			JSONObject data = toFront.getJSONObject("data");
			toFront.put("code", 1);
			data.put("msg", ImportOutputMessage.getDescByCode("104"));
			data.put("progress", 35);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			JSONObject object = changeData(datas, heads, schoolId);
			targetList = (List<JSONObject>)object.get("rowDatas");
			delIdList = (List<String>)object.get("delIdList");
			// 开始入库
			toFront.put("code", 1);
			data.put("msg", ImportOutputMessage.getDescByCode("105"));
			data.put("progress", 50);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			if (CollectionUtils.isNotEmpty(targetList)) {
			    addImportTeachers(targetList,schoolId,delIdList);																			
				toFront.put("code", 1);
                data.put("progress", 100);
                data.put("msg", "导入成功，共计导入"+ targetList.size() +"条信息记录！");
                msg = "导入成功，共计导入"+ targetList.size() +"条信息记录！"; 
			}
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			code = "-6";
			msg = ImportOutputMessage.getDescByCode(code);		
		}
		result.put("code", code);
		result.put("msg", msg);
		return result;
	}
	
    *//**
     * 字符串在数组中的索引
     * 
     * @param string
     * @param stutitle2
     * @return
     *//*
    private int strIndexInArray(String source, String[] arr) {				
        int rs = -1;
        for (int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null?"":arr[i]);
            if (target.equalsIgnoreCase(source)) {
                rs = i;break;
            }
        }
        return rs;
    }
	
	*//**
	 * 判断字符串是否在字符串数组内
	 * 
	 * @param string
	 * @param courseTitleName
	 * @return
	 *//*
	private int inArrayNum(String source, String[] courseTitleName) {
		int number = 0;
		for(int i = 0; i < courseTitleName.length; i++) {
			String target = (courseTitleName[i] == null?"":courseTitleName[i]);
			if(target.equalsIgnoreCase(source))number ++;
		}
		return number;
	}
	*/
    /**
     * 将java.util.date转换为指定格式的类型
     * 
     * @param date
     * @return long
     */
    private static synchronized long getDateMilliFormat(java.util.Date date) {
        return date.getTime()/1000;
    }

	

}