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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.FileImportInfoDao;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.dao.StudentBaseManageDao;
import com.talkweb.csbasedata.service.StudentBaseManageService;
import com.talkweb.csbasedata.service.StudentImportManageService;
import com.talkweb.csbasedata.util.DateUtil;
import com.talkweb.csbasedata.util.ImportTaskParameter;
import com.talkweb.csbasedata.util.SortUtil;
import com.talkweb.csbasedata.util.StringUtil;
import com.talkweb.filemanager.service.FileServer;
/**
 * 学生导入-serviceImpl
 * @author zhh
 *
 */
@Service("studentImportManageService")
public class StudentImportManageServiceImpl implements StudentImportManageService {

	@Autowired
	private FileServer fileServerImplFastDFS;
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	
	@Autowired
	private FileImportInfoDao fileImportDao;
	
	@Autowired
	private StudentBaseManageDao studentManageDao;
	
	@Autowired
	private StudentBaseManageService studentManageService;
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;	    
	
	private static final String headString="studentManage_";
	private static final String headStringd ="studentManage.";
	
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
	public JSONObject uploadExcel(JSONObject param) throws Exception {
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
			stuTitle = new String[9];
			stuTitle[0] = "姓名";
			stuTitle[1] = "性别";
			stuTitle[2] = "年级";
			stuTitle[3] = "班级";
			stuTitle[4] = "家长手机号码";
			stuTitle[5] = "学生学籍号";
			stuTitle[6] = "学生学号";
			stuTitle[7] = "学生班级座位号";
			stuTitle[8] = "学生电子卡号";
			
			stuTitleName = new String[stuTitle.length];
			stuTitleName[0] = "studentName";
			stuTitleName[1] = "gender";
			stuTitleName[2] = "gradeName";
			stuTitleName[3] = "className";
			stuTitleName[4] = "parentMobilePhone";
			stuTitleName[5] = "studentNumber";
			stuTitleName[6] = "studentCard";
			stuTitleName[7] = "seatNumber";
			stuTitleName[8] = "electronicCardNumber";
			
			stuTitleNeed = new int[stuTitle.length];
			stuTitleNeed[0] = 1;
			stuTitleNeed[1] = 0;
			stuTitleNeed[2] = 1;
			stuTitleNeed[3] = 1;
			stuTitleNeed[4] = 0;
			stuTitleNeed[5] = 0;
			stuTitleNeed[6] = 0;
			stuTitleNeed[7] = 0;
			stuTitleNeed[8] = 0;

		}
		obj.put("stuTitle",stuTitle );
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
					if (isInArray("姓名", tempStuImpExcTitle)&&isInArray("年级", tempStuImpExcTitle)
							&&isInArray("班级", tempStuImpExcTitle)) {
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
	/**
	 * 获取excel头部
	 * @author zhh
	 */
	@Override
	public JSONObject getExcelMatch(JSONObject param) throws Exception {
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
	public JSONObject importProgress(JSONObject param) throws Exception {
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
	public JSONObject singleDataCheck(JSONObject param) throws Exception {
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
	public JSONObject continueImport(JSONObject param) throws Exception {
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
	public JSONObject startImportTask(JSONObject param) throws Exception {
		ImportTaskParameter stt = new ImportTaskParameter();
		String [] stuTitleName = (String[]) param.get("stuTitleName");
		int [] stuTitleNeed = (int[]) param.get("stuTitleNeed");
		String [] stuTitle = (String[]) param.get("stuTitle");
		String xnxq = param.getString("xnxq");
		String fileId = param.getString("fileId");
		String orgType = param.getString("orgType");
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
		stt.setOrgType(orgType);
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
									int num = studentManageService.insertImportStudentBatch(needInsert);						
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
	private int setAllCommonInfo(ImportTaskParameter stt) {
		JSONObject obj = new JSONObject();
		int code = 1;
		//获取所有年级/班级信息
		String xnxq = stt.getXnxq();
		obj.put("xn", xnxq.substring(0, 4));
		obj.put("schoolId", stt.getSchoolId());
		Map<T_GradeLevel,String> nameGradeMap = AccountStructConstants.T_GradeLevelName;
		List<JSONObject> gList = studentManageDao.getGradeListBySchoolId(obj);
		Map<String,String> gradeNameIdMap = new HashMap<String, String>();
		if(gList!=null){
			for(JSONObject gObj:gList){
				String gradeId = gObj.getString("gradeId");
				int currentLevel = gObj.getIntValue("currentLevel");
				String gradeName = nameGradeMap.get(T_GradeLevel.findByValue(currentLevel));
				if(StringUtils.isBlank(gradeName) || StringUtils.isBlank(gradeId)){
					continue;
				}
				gradeNameIdMap.put(gradeName, gradeId);
			}
		}
		List<JSONObject> cList = studentManageDao.getClassListBySchoolId(obj);
		Map<String,String> classNameIdMap = new HashMap<String, String>();
		if(cList!=null){
			for(JSONObject cObj:cList){
				int currentLevel = cObj.getIntValue("currentLevel");
				String gradeName = nameGradeMap.get(T_GradeLevel.findByValue(currentLevel));
				String classId = cObj.getString("classId");
				String className = cObj.getString("className");
				if(StringUtils.isBlank(className)|| StringUtils.isBlank(classId)){
					continue;
				}
				classNameIdMap.put(gradeName+"_"+className, classId);
			}
		}
		//获取设置过的学生
		//parentId,name
		Map<String,String> parentIdNameMap = new HashMap<String, String>();
		 //name,parentId *
		Map<String,String> parentUserMap = new HashMap<String, String>();
		 //name,parentAccountId *
		Map<String,String> parentAccountMap = new HashMap<String, String>();
		//mobilePhone,parentAccountId
		//Map<String,String> mobilePhoneParentMap = new  HashMap<String,String>();
		 //name,studentId *
		Map<String,String> studentUserMap = new HashMap<String, String>();
		 //name,accountId*
		Map<String,String> studentAccountMap = new HashMap<String, String>();
		 //accountId,name
		Map<String,String> accountNameMap = new HashMap<String, String>();
		
		obj.put("role", T_Role.Student.getValue());
		List<JSONObject> list = studentManageDao.getStudentListBySchoolId(obj);
		List<String> pUserIds = new ArrayList<String>();
		for(JSONObject studentObj:list){
			String name = studentObj.getString("name");
			String studentId = studentObj.getString("studentId");
			String parentId = studentObj.getString("parentId");
			String accountId = studentObj.getString("accountId");
			String className = studentObj.getString("className");
			if(StringUtils.isBlank(name)){
				continue;
			}
			parentUserMap.put(className+"_"+name, parentId);
			parentIdNameMap.put(parentId, className+"_"+name);
			studentAccountMap.put(className+"_"+name, accountId);
			accountNameMap.put(accountId,name);
			studentUserMap.put(className+"_"+name, studentId);
			if(!pUserIds.contains(parentId)){
				pUserIds.add(parentId);
			}
		}
		if(pUserIds.size()>0){
			obj.put("ids",pUserIds);
			obj.put("role", T_Role.Parent.getValue());
			List<JSONObject> pList = studentManageDao.getAccountByUserId(obj);
			for(JSONObject pUser :pList){
				String mobilePhone = pUser.getString("mobilePhone");
				String accountId = pUser.getString("accountId");
				String parentUserId = pUser.getString("userId");
				String cname = parentIdNameMap.get(parentUserId);
				if(StringUtils.isBlank(cname)){
					continue;
				}
				parentAccountMap.put(cname, accountId);
				/*if(StringUtils.isNotBlank(mobilePhone)){
					mobilePhoneParentMap.put(mobilePhone, accountId);
				}*/
			}
		}
		//获取当前学校已设置过的学籍号和学号
		JSONObject sr = new JSONObject();
		sr.put("schoolId", stt.getSchoolId());
		Map<String,String>studentSchoolNumberMap = new HashMap<String,String>();
		Map<String,String>studentStdNumberMap = new HashMap<String,String>();
		List<JSONObject> srList = studentManageDao.getStudentRepeatList(sr);
		for(JSONObject studentRepeat:srList){
			String schoolNumber = studentRepeat.getString("schoolNumber");
			String stdNumber = studentRepeat.getString("stdNumber");
			String name = studentRepeat.getString("name");
			if(StringUtils.isNotBlank(schoolNumber) && StringUtils.isNotBlank(name)){
				studentSchoolNumberMap.put(schoolNumber,name );
			}
			if(StringUtils.isNotBlank(stdNumber) && StringUtils.isNotBlank(name)){
				studentStdNumberMap.put(stdNumber, name);
			}
		}
		//获取当前学校已设置过的电子卡号
		JSONObject ue = new JSONObject();
		ue.put("schoolId", stt.getSchoolId());
		Map<String,String> userExtendMap = new HashMap<String,String>();
		List<JSONObject> userExtends = studentManageDao.getUserExtendName(ue);
		for(JSONObject userExtend:userExtends){
			String cardNumber = userExtend.getString("cardNumber");
			String name = userExtend.getString("name");
			userExtendMap.put(cardNumber, name);
		}
		Object keyStudentSchoolNumberMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.studentSchoolNumberMap";
		Object keyStudentStdNumberMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.studentStdNumberMap";
		Object keyUserExtendMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.userExtendMap";
		Object keyClassNameIdMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.classNameIdMap";
		Object keyGradeNameIdMap= headStringd +stt.getSchoolId()+stt.getProcessId()+".00.gradeNameIdMap";
		Object keyParentUserMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.parentUserMap";
		Object keyParentAccountMap= headStringd +stt.getSchoolId()+stt.getProcessId()+".00.parentAccountMap";
		Object keyStudentUserMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.studentUserMap";
		Object keyStudentAccountMap= headStringd +stt.getSchoolId()+stt.getProcessId()+".00.studentAccountMap";
		//Object keyMobilePhoneParentMap= headStringd +stt.getSchoolId()+stt.getProcessId()+".00.mobilePhoneParentMap";
		try {
			redisOperationDAO.set(keyStudentSchoolNumberMap, studentSchoolNumberMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyStudentStdNumberMap, studentStdNumberMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyUserExtendMap, userExtendMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			//redisOperationDAO.set(keyMobilePhoneParentMap, mobilePhoneParentMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyParentUserMap, parentUserMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyParentAccountMap, parentAccountMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyStudentUserMap, studentUserMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyStudentAccountMap, studentAccountMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyClassNameIdMap, classNameIdMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyGradeNameIdMap, gradeNameIdMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			delFileDBAndFileServer(stt.getSchoolId(),headString+stt.getKeyId());
			e.printStackTrace();
			code = -50;
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
	private Map<String, Object> getInsertEntityByCkrs(Object keyProgressMap, ImportTaskParameter sp, String processId,Hashtable<String, JSONObject> progressMap) {
		Map<String, Object>  returnParam = new HashMap<String, Object>();
		String schoolId=sp.getSchoolId();
		String keyId = sp.getKeyId();
		JSONObject toFront = progressMap.get(processId);
		Object keyGradeNameIdMap = headStringd+schoolId+processId+".00.gradeNameIdMap";
		Object keyClassNameIdMap = headStringd+schoolId+processId+".00.classNameIdMap";
		Object keyPrepDataMap =headStringd+sp.getSchoolId()+processId+".00.prepDataMap";
		Object keyParentUserMap =headStringd+sp.getSchoolId()+processId+".00.parentUserMap";
		Object keyParentAccountMap =headStringd+sp.getSchoolId()+processId+".00.parentAccountMap";
		Object keyStudentUserMap =headStringd+sp.getSchoolId()+processId+".00.studentUserMap";
		Object keyStudentAccountMap =headStringd+sp.getSchoolId()+processId+".00.studentAccountMap";
		//Object keyMobilePhoneParentMap= headStringd +sp.getSchoolId()+processId+".00.mobilePhoneParentMap";
		Map<String,String> parentUserMap = new HashMap<String, String>();
		 //name,parentAccountId
		Map<String,String> parentAccountMap = new HashMap<String, String>();
		 //name,studentId
		Map<String,String> studentUserMap = new HashMap<String, String>();
		//Map<String,String> mobilePhoneParentMap = new HashMap<String, String>();
		 //name,accountId
		Map<String,String> studentAccountMap = new HashMap<String, String>();
		Map<String,String> classNameIdMap = new HashMap<String, String>();
		Map<String,String> gradeNameIdMap = new HashMap<String, String>();
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		try {
			//mobilePhoneParentMap = (Map<String, String>) redisOperationDAO.get(keyMobilePhoneParentMap);
			parentUserMap =  (Map<String, String>) redisOperationDAO.get(keyParentUserMap);
			parentAccountMap = (Map<String, String>) redisOperationDAO.get(keyParentAccountMap);
			studentUserMap =  (Map<String, String>) redisOperationDAO.get(keyStudentUserMap);
			studentAccountMap = (Map<String, String>) redisOperationDAO.get(keyStudentAccountMap);
			gradeNameIdMap =  (Map<String, String>) redisOperationDAO.get(keyGradeNameIdMap);
			classNameIdMap = (Map<String, String>) redisOperationDAO.get(keyClassNameIdMap);
			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
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
		List<JSONObject> insertStudentList = new ArrayList<JSONObject>();
		List<JSONObject> insertStudentAccountList = new ArrayList<JSONObject>();
		List<JSONObject> insertStudentUserList = new ArrayList<JSONObject>();
		List<JSONObject> insertParentAccountList = new ArrayList<JSONObject>();
		List<JSONObject> insertParentUserList = new ArrayList<JSONObject>();
		List<JSONObject> insertParentList = new ArrayList<JSONObject>();
		List<JSONObject> insertUserExtendList = new ArrayList<JSONObject>();
		Map<String,JSONObject> needUpdatePswAll = new HashMap<String,JSONObject>();
		
		List<JSONObject> updateUserExtendList = new ArrayList<JSONObject>();
		List<JSONObject> updateStudentAccountList = new ArrayList<JSONObject>();
		List<JSONObject> updateStudentList = new ArrayList<JSONObject>();
		List<JSONObject> updateParentClassIdList = new ArrayList<JSONObject>();
		List<JSONObject> updateParentAccountList = new ArrayList<JSONObject>();
		List<String> deleteUserExtendIdList = new ArrayList<String>();
		List<String> parentAccountNamePhones = new ArrayList<String>(); 
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
				data.put("progress", (int)pint);
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
				String studentName=row.getString("studentName");
				String gender=row.getString("gender");
				if(StringUtils.isBlank(gender)){
					gender="0"; //不填  
				}else if( "男".equals(gender)){
					gender="1"; //男
				}else{
					gender="2"; //女
				}
				String gradeName=row.getString("gradeName");
				String className=row.getString("className");
				String parentMobilePhone=row.getString("parentMobilePhone");
				String studentNumber=row.getString("studentNumber");
				String studentCard=row.getString("studentCard");
				String seatNumber=row.getString("seatNumber");
				String electronicCardNumber = row.getString("electronicCardNumber");
				String gradeId = gradeNameIdMap.get(gradeName);
				String classId = classNameIdMap.get(gradeName+"_"+className);
				String studentAccountName = "s"+StringUtil.createRandom(10);
				if(StringUtils.isBlank(gradeId)||StringUtils.isBlank(classId)){
					continue;
				}
				//判断学生姓名是否重复
				if(studentAccountMap.containsKey(className+"_"+studentName)){
					//更新学生姓名、性别
					JSONObject studentAccountObj = new JSONObject();
					studentAccountObj.put("id", studentAccountMap.get(className+"_"+studentName));
					studentAccountObj.put("gender", gender);
					studentAccountObj.put("name", studentName);
					updateStudentAccountList.add(studentAccountObj);
					//更新学籍号、学号、座位号、年级、班级
					JSONObject student = new JSONObject();
					student.put("stdId", studentUserMap.get(className+"_"+studentName));
					student.put("classId",classId);
					student.put("gradeId", gradeId);
					student.put("sindex", seatNumber);
					student.put("stdNumber", studentNumber);
					student.put("schoolNumber",studentCard);
					student.put("schoolId", schoolId);
					student.put("accountId", studentAccountMap.get(className+"_"+studentName));
					updateStudentList.add(student);
					//更新家长表中的classId
					JSONObject parent = new JSONObject();
					parent.put("parentUserId", parentUserMap.get(className+"_"+studentName));
					parent.put("classId", classId);
					updateParentClassIdList.add(parent);
					//更新电子卡号
					deleteUserExtendIdList.add(studentUserMap.get(className+"_"+studentName));
					JSONObject userExtend = new JSONObject();
					if(StringUtils.isNotBlank(electronicCardNumber)){
						userExtend.put("electronicCardNumber",electronicCardNumber);
						userExtend.put("accountId", studentAccountMap.get(className+"_"+studentName));
						userExtend.put("userId", studentUserMap.get(className+"_"+studentName));
						userExtend.put("schoolId", schoolId);
						updateUserExtendList.add(userExtend);
					}
					//更新家长电话号码
					JSONObject parentAccountObj = new JSONObject();
					parentAccountObj.put("mobilePhone",parentMobilePhone);
					parentAccountObj.put("id",parentAccountMap.get(className+"_"+studentName));
					updateParentAccountList.add(parentAccountObj);
				}else{
					JSONObject nu = new JSONObject();
					//创建学生account
					JSONObject studentAccountObj = new JSONObject();
					String studentAccountUUID = UUIDUtil.getUUID();
					nu.put("accountUUID", studentAccountUUID);
					studentAccountObj.put("uuid", studentAccountUUID);
					studentAccountObj.put("accountName", studentAccountName);
					studentAccountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
					studentAccountObj.put("accountStatus", 0);
					studentAccountObj.put("name", studentName);
					studentAccountObj.put("gender", gender);
					studentAccountObj.put("mobilePhone", "");
					studentAccountObj.put("createTime", DateUtil.getTimeAndAddOneSecond(++interval));
					insertStudentAccountList.add(studentAccountObj);
					//创建学生user
					JSONObject userStudent = new JSONObject();
					String studentUserUUID = UUIDUtil.getUUID();
					nu.put("studentUUID", studentUserUUID);
					userStudent.put("uuid", studentUserUUID);
					userStudent.put("role", T_Role.Student.getValue());
					userStudent.put("avatar", "");
					userStudent.put("accountId",studentAccountUUID);//#
					//userStudent.put("createTime", new Date().getTime());
					insertStudentUserList.add(userStudent);
					//创建学生关联的电子一卡通
					if(StringUtils.isNotBlank(electronicCardNumber)){
						JSONObject userExtendObj = new JSONObject();
						userExtendObj.put("electronicCardNumber", electronicCardNumber);
						userExtendObj.put("accountId",studentAccountUUID);//#
						userExtendObj.put("userId",studentUserUUID);//#
						userExtendObj.put("schoolId",schoolId);
						nu.put("electronicCardNumber", electronicCardNumber);
						insertUserExtendList.add(userExtendObj);
					}
					//创建student
					JSONObject studentUserObj = new JSONObject();
					studentUserObj.put("userId", studentUserUUID);//#
					studentUserObj.put("studentCard", studentCard);
					studentUserObj.put("studentNumber", studentNumber);
					studentUserObj.put("seatNumber",seatNumber);
					studentUserObj.put("gradeId", gradeId);
					studentUserObj.put("classId", classId);
					studentUserObj.put("schoolId", schoolId);
					studentUserObj.put("accountId", studentAccountUUID);//#
					studentUserObj.put("name", studentName);
					insertStudentList.add(studentUserObj);
					//创建家长account
					String	parentAccountName = "";
					parentAccountName = "p"+StringUtil.createRandom(10);
					if(!parentAccountNamePhones.contains(parentMobilePhone)){
						parentAccountNamePhones.add(parentMobilePhone);
					}
					JSONObject parentAccountObj = new JSONObject();
					String parentAccountUUID = UUIDUtil.getUUID();
					nu.put("parentAccountUUID", parentAccountUUID);
					parentAccountObj.put("uuid", parentAccountUUID);
					parentAccountObj.put("accountName", parentAccountName);
					parentAccountObj.put("pwd", ""); //先设置为空 待得到accountId再更新
					parentAccountObj.put("accountStatus", 0);
					parentAccountObj.put("name", "");
					parentAccountObj.put("gender", 1);//家长默认为男
					parentAccountObj.put("mobilePhone", parentMobilePhone);
					parentAccountObj.put("createTime", DateUtil.getTimeAndAddOneSecond(++interval));
					insertParentAccountList.add(parentAccountObj);
					//创建家长user
					JSONObject userParent = new JSONObject();
					String parentUserUUID = UUIDUtil.getUUID();
					nu.put("parentUUID", parentUserUUID);
					userParent.put("uuid", parentUserUUID);
					userParent.put("role", T_Role.Parent.getValue());
					userParent.put("avatar", "");
					userParent.put("accountId",parentAccountUUID);//#
					//userParent.put("createTime", new Date().getTime());
					insertParentUserList.add(userParent);
					//创建parent
					JSONObject parentUserObj = new JSONObject();
					parentUserObj.put("parentUserId", parentUserUUID );//#
					parentUserObj.put("studentUserUUID", studentUserUUID); //#
					parentUserObj.put("classId", classId);
					insertParentList.add(parentUserObj);
					
					needUpdatePswAll.put(className+"_"+studentName, nu);
				}
			}
			returnParam.put("insertStudentList", insertStudentList);
			returnParam.put("insertStudentAccountList", insertStudentAccountList);
			returnParam.put("insertStudentUserList", insertStudentUserList);
			returnParam.put("insertParentAccountList", insertParentAccountList);
			returnParam.put("insertParentUserList", insertParentUserList);
			returnParam.put("insertParentList", insertParentList);
			returnParam.put("insertUserExtendList", insertUserExtendList);
			returnParam.put("needUpdatePswAll",needUpdatePswAll);
			
			returnParam.put("updateStudentList", updateStudentList);
			returnParam.put("updateStudentAccountList", updateStudentAccountList);
			returnParam.put("updateParentClassIdList", updateParentClassIdList);
			returnParam.put("updateParentAccountList", updateParentAccountList);
			returnParam.put("updateUserExtendList", updateUserExtendList);
			returnParam.put("parentAccountNamePhones", parentAccountNamePhones);
			JSONObject deleteUserExtendObj = new JSONObject();
			deleteUserExtendObj.put("schoolId",schoolId );
			deleteUserExtendObj.put("ids", deleteUserExtendIdList); //userId
			returnParam.put("deleteUserExtendObj", deleteUserExtendObj);
		}
		return returnParam;
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

		//redis获取studentNameIdMap
		Object keyGradeNameIdMap = headStringd+schoolId+processId+".00.gradeNameIdMap";
		Object keyClassNameIdMap = headStringd+schoolId+processId+".00.classNameIdMap";
		Object keyPrepDataMap = headStringd+schoolId+processId+".00.prepDataMap";
		Object keyUserExtendMap =  headStringd +schoolId+processId+".00.userExtendMap";
		Object keyStudentSchoolNumberMap =  headStringd +schoolId+processId+".00.studentSchoolNumberMap";
		Object keyStudentStdNumberMap =  headStringd +schoolId+processId+".00.studentStdNumberMap";
		Object keyArr = headStringd+schoolId+processId+".00.arr";
		
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Map<String,String> classNameIdMap = new HashMap<String, String>();
		Map<String,String> gradeNameIdMap = new HashMap<String, String>();
		Map<String,String> userExtendMap = new HashMap<String, String>();
		Map<String,String> studentSchoolNumberMap = new HashMap<String, String>();
		Map<String,String> studentStdNumberMap = new HashMap<String, String>();
		String [] arr = null;
		try {
			arr = (String[]) redisOperationDAO.get(keyArr);
			studentSchoolNumberMap  =  (Map<String, String>)redisOperationDAO.get(keyStudentSchoolNumberMap);
			studentStdNumberMap  =  (Map<String, String>)redisOperationDAO.get(keyStudentStdNumberMap);
			userExtendMap  =  (Map<String, String>)redisOperationDAO.get(keyUserExtendMap);
			gradeNameIdMap =  (Map<String, String>) redisOperationDAO.get(keyGradeNameIdMap);
			classNameIdMap = (Map<String, String>) redisOperationDAO.get(keyClassNameIdMap);
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
		//className_studentName,studentName
		JSONObject studentNameMap =preData.getJSONObject("studentNameMap");
		JSONObject studentCardMap =preData.getJSONObject("studentCardMap");
		JSONObject studentNumberMap =preData.getJSONObject("studentNumberMap");
		JSONObject eCardMap =preData.getJSONObject("eCardMap");
		//className-rowNums     防止excel名称重复(重复则多条都要显示出来)
		Map<String,LinkedHashSet<String>> excelNameMap =(Map<String, LinkedHashSet<String>>) preData.get("excelNameMap");
				
		if(studentNameMap==null){
			studentNameMap = new JSONObject();
		}
		if(eCardMap==null){
			eCardMap = new JSONObject();
		}
		if(studentNumberMap==null){
			studentNumberMap = new JSONObject();
		}
		if(studentCardMap==null){
			studentCardMap = new JSONObject();
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
		boolean isStudentNumberRepeat=false;
		boolean isStudentCardRepeat=false;
		boolean isECardRepeat=false;
		Map<String,JSONObject> isExcelRepeatMap = new HashMap<String,JSONObject>(); //是否已匹配重复
		Set<String> isRepeatMsg = new HashSet<String>(); 
		for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
		{
			index++;
			boolean isWsg=false;
			boolean isClassWsg=false;
			int rowNum = it.next();
			JSONObject pd = pureDatas.get(rowNum);
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			JSONArray mrows = new JSONArray();
			wmsg.put("mrows", mrows);
			
			// excel必输入字段验证
			String studentName = pd.containsKey("studentName") ? pd.getString("studentName"):null;
			String gradeName = pd.containsKey("gradeName") ? pd.getString("gradeName"): null;
			String className = pd.containsKey("className") ? pd.getString("className"):null;

			//excel验证gradeName数据合理性
			String title=pd.getString("gradeNameName");
			if (!(gradeName != null && gradeName.trim().length() > 0)) {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", gradeName);
				wsg.put("err", "不能为空！");
				mrows.add(wsg);
				isWsg=true;
			}else{
				if(gradeNameIdMap!=null && !gradeNameIdMap.containsKey(gradeName.trim())){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", gradeName);
					wsg.put("err", "无匹配记录！");
					mrows.add(wsg);
					isWsg=true;
				}
			}
			// excel验证className
			title=pd.getString("classNameName");
			if (className != null && className.trim().length() > 0) {
				if(classNameIdMap!=null && !classNameIdMap.containsKey(gradeName+"_"+className.trim())){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", className);
					wsg.put("err", "无匹配记录！");
					mrows.add(wsg);
					isWsg=true;
					isClassWsg=true;
				}
			}else{
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", className);
				wsg.put("err", "不能为空！");
				mrows.add(wsg);
				isWsg=true;
				isClassWsg=true;
			}
			//excel验证姓名
			title=pd.getString("studentNameName");
			if (studentName == null || studentName.trim().length() < 1) {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", studentName);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}else{
				if(studentName.trim().length()>20){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", studentName);
					wsg.put("err", "输入值不合法！");
					isWsg=true;
					mrows.add(wsg);
				}else{
					LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
					if(excelNameMap.containsKey(className.trim()+"_"+studentName.trim()+"_1")){
							repeatNameList = excelNameMap.get(className.trim()+"_"+studentName.trim()+"_1");
							if(repeatNameList!=null && !repeatNameList.contains(rowNum+"")){
								repeatNameList.add(rowNum+"");
								JSONObject json = new JSONObject();
								json.put("excelRepeatKey", className.trim()+"_"+studentName.trim()+"_1");
								json.put("isExcelRepeat", true);
								json.put("isTwoTitle", true);
								json.put("repeatClassTitle", pd.getString("classNameName"));
								json.put("repeatNameTitle",title);
								json.put("studentName",studentName);
								json.put("className",className);
								json.put("isClassWsg", isClassWsg);
								isExcelRepeatMap.put(title+"_"+rowNum, json);
							}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
								repeatNameList.add(rowNum+"");
								excelNameMap.put(className.trim()+"_"+studentName.trim()+"_1", repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(title+"_"+rowNum, json);
							}else{
								JSONObject wsg = new JSONObject();
								wsg.put("title", title);
								wsg.put("oldValue", studentName);
								wsg.put("err", "excel中重复！");
								isWsg=true;
								mrows.add(wsg);
								JSONObject wsg1 = new JSONObject();
								wsg1.put("title",  pd.getString("classNameName"));
								wsg1.put("oldValue", className);
								wsg1.put("err", "excel中重复！");
								mrows.add(wsg1);
							}
					}else{
						repeatNameList.add(rowNum+"");
						excelNameMap.put(className.trim()+"_"+studentName.trim()+"_1", repeatNameList);
						JSONObject json = new JSONObject();
						json.put("isRepeat", false);
						isExcelRepeatMap.put(title+"_"+rowNum, json);
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
				if("studentName".equals(impHead[j])|| "gradeName".equals(impHead[j])|| "className".equals(impHead[j])){
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
				}else if (tit.equalsIgnoreCase("parentMobilePhone")){
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotEmpty(val))
					{
						if (!StringNumTool.isInteger(val)|| val.length()!=11 || "-".equals(val.substring(0, 1))) {
							noRecord=true;
							err= "输入值不合法！";
							isWsg=true;
						}
					}
				}else if(tit.equalsIgnoreCase("electronicCardNumber")){
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotBlank(val) && (!val.matches("[A-Za-z0-9]+") || val.length()>20 )){
						noRecord=true;
						err= "输入值不合法！";
						isWsg=true;
					}else if(StringUtils.isNotBlank(val) && userExtendMap.containsKey(val) && !studentName.equals(userExtendMap.get(val))){
						noRecord=true;
						err= "系统中重复！";
						isWsg=true;
					}else{
						if(StringUtils.isNotBlank(val) ){
							LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
							if(excelNameMap.containsKey(val+"_2")){
									repeatNameList = excelNameMap.get(val+"_2");
									if(repeatNameList!=null && !repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										JSONObject json = new JSONObject();
										json.put("isTwoTitle", false);
										json.put("excelRepeatKey", val+"_2");
										json.put("isExcelRepeat", true);
										json.put("repeatNameTitle",titName);
										json.put("studentName",val);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										excelNameMap.put(val+"_2", repeatNameList);
										JSONObject json = new JSONObject();
										json.put("isRepeat", false);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else{
										noRecord=true;
										err= "excel中重复！";
										isWsg=true;
									}
							}else{
								repeatNameList.add(rowNum+"");
								excelNameMap.put(val+"_2", repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(titName+"_"+rowNum, json);
							}
						}
					}
				}else if(tit.equalsIgnoreCase("studentNumber")){ //学籍号
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotBlank(val) &&( !val.matches("[A-Za-z0-9]+") || !(val.length()<=20 && val.length()>=6))){
						noRecord=true;
						err= "输入值不合法！";
						isWsg=true;
					}else if(StringUtils.isNotBlank(val) && studentStdNumberMap.containsKey(val) && !studentName.equals(studentStdNumberMap.get(val))){
						noRecord=true;
						err= "系统中重复！";
						isWsg=true;
					}else{
						if(StringUtils.isNotBlank(val) ){
							LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
							if(excelNameMap.containsKey(val+"_3")){
									repeatNameList = excelNameMap.get(val+"_3");
									if(repeatNameList!=null && !repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										JSONObject json = new JSONObject();
										json.put("isTwoTitle", false);
										json.put("excelRepeatKey", val+"_3");
										json.put("isExcelRepeat", true);
										json.put("repeatNameTitle",titName);
										json.put("studentName",val);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										excelNameMap.put(val+"_3", repeatNameList);
										JSONObject json = new JSONObject();
										json.put("isRepeat", false);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else{
										noRecord=true;
										err= "excel中重复！";
										isWsg=true;
									}
							}else{
								repeatNameList.add(rowNum+"");
								excelNameMap.put(val+"_3", repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(titName+"_"+rowNum, json);
							}
						}
					}
				}else if(tit.equalsIgnoreCase("studentCard")){
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotBlank(val) && (!val.matches("[A-Za-z0-9]+")||!(val.length()<=20 && val.length()>=6))){
						noRecord=true;
						err= "输入值不合法！";
						isWsg=true;
					}else if(StringUtils.isNotBlank(val) && studentSchoolNumberMap.containsKey(val) && !(studentName.equals(studentSchoolNumberMap.get(val)))){
						noRecord=true;
						err= "系统中重复！";
						isWsg=true;
					}else{
						if(StringUtils.isNotBlank(val) ){
							LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
							if(excelNameMap.containsKey(val+"_4")){
									repeatNameList = excelNameMap.get(val+"_4");
									if(repeatNameList!=null && !repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										JSONObject json = new JSONObject();
										json.put("isTwoTitle", false);
										json.put("excelRepeatKey", val+"_4");
										json.put("isExcelRepeat", true);
										json.put("repeatNameTitle",titName);
										json.put("studentName",val);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
										repeatNameList.add(rowNum+"");
										excelNameMap.put(val+"_4", repeatNameList);
										JSONObject json = new JSONObject();
										json.put("isRepeat", false);
										isExcelRepeatMap.put(titName+"_"+rowNum, json);
									}else{
										noRecord=true;
										err= "excel中重复！";
										isWsg=true;
									}
							}else{
								repeatNameList.add(rowNum+"");
								excelNameMap.put(val+"_4", repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(titName+"_"+rowNum, json);
							}
						}
					}
				} else{
					val = val.replaceAll(" ", "");
					if(StringUtils.isNotEmpty(val))
					{
						if ( val != null && (!val.matches("[A-Za-z0-9]+") || val.length()>20 )) { //其余的字母或数字组合
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
				if( !isNameRepeat || !isStudentNumberRepeat || !isStudentCardRepeat || !isECardRepeat || isWsg)
				{
						studentNameMap.remove(className.trim()+"_"+studentName.trim());
						studentNumberMap.remove(studentName.trim());
						studentCardMap.remove(studentName.trim());
						eCardMap.remove(studentName.trim());
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
		for (Map.Entry<String, JSONObject> entry : isExcelRepeatMap.entrySet()) {
			String key= entry.getKey();
			Integer rowNum = Integer.parseInt(key.split("_")[1]);
			JSONObject isExcelRepeatJSON = entry.getValue();
			Boolean isExcelRepeat = isExcelRepeatJSON.getBooleanValue("isExcelRepeat");
			if(!isExcelRepeat){continue;}
			Boolean isTwoTitle = isExcelRepeatJSON.getBoolean("isTwoTitle");
			String repeatClassTitle = isExcelRepeatJSON.getString("repeatClassTitle");
			String repeatNameTitle = isExcelRepeatJSON.getString("repeatNameTitle");
			String excelRepeatKey = isExcelRepeatJSON.getString("excelRepeatKey");
			String studentName = isExcelRepeatJSON.getString("studentName");
			String className = isExcelRepeatJSON.getString("className");
			Boolean isClassWsg = isExcelRepeatJSON.getBoolean("isClassWsg");
			String repeatKey = "";
			if(isTwoTitle){
				repeatKey = repeatClassTitle.trim()+"_"+repeatNameTitle.trim();
			}else{
				repeatKey = repeatNameTitle.trim();
			}
			LinkedHashSet<String> repeatRowNums = excelNameMap.get(excelRepeatKey);
			if(repeatRowNums!=null){
				for(String repeatRowNum:repeatRowNums){
					boolean isFind = false; //是否找到
					for(int g = 0;g<wrongMsg.size();g++){
						JSONObject wmsg1 = (JSONObject) wrongMsg.get(g);
						int rowN = wmsg1.getIntValue("row");
						if(!repeatRowNum.equals(rowN+"") ){continue;} //|| rowNum<rowN
						if(isRepeatMsg.contains(repeatKey+"_"+repeatRowNum)){
							continue;
						}
						JSONArray mrows1 = wmsg1.getJSONArray("mrows");
						if(mrows1==null){
							mrows1=new JSONArray();
						}
						if(repeatRowNum.equals(rowN+"")){
							JSONObject wsg1 = new JSONObject();
							wsg1.put("title", repeatNameTitle);
							wsg1.put("oldValue", studentName);
							wsg1.put("err", "excel中重复！");
								mrows1.add(wsg1);
							if(isClassWsg!=null &&!isClassWsg && isTwoTitle){
								JSONObject wsg2 = new JSONObject();
								wsg2.put("title", repeatClassTitle);
								wsg2.put("oldValue", className);
								wsg2.put("err", "excel中重复！");
									mrows1.add(wsg2);
							}
							isFind=true;
							isRepeatMsg.add(repeatKey+"_"+repeatRowNum);
							if(repeatMrows==null){
								rowcolMap.remove(Integer.parseInt(repeatRowNum));
							}
						}
					}
					//第一次的情况 即wrongMsg为空
					if(!isFind && excelNameMap.containsKey(excelRepeatKey) && !isRepeatMsg.contains(repeatKey+"_"+repeatRowNum)){
						JSONObject wmsg1 = new JSONObject();
						JSONArray mrows1=new JSONArray();
						JSONObject wsg1 = new JSONObject();
						wsg1.put("title", repeatNameTitle);
						wsg1.put("oldValue", studentName);
						wsg1.put("err", "excel中重复！");
						mrows1.add(wsg1);
						if(isClassWsg!=null && !isClassWsg && isTwoTitle){
							JSONObject wsg2 = new JSONObject();
							wsg2.put("title", repeatClassTitle);
							wsg2.put("oldValue", className);
							wsg2.put("err", "excel中重复！");
							mrows1.add(wsg2);
						}
						wmsg1.put("row", repeatRowNum);
						wmsg1.put("mrows", mrows1);
						wrongMsg.add(wmsg1);
						prepDataMap.put(processId,preData);
						isRepeatMsg.add(repeatKey+"_"+repeatRowNum);
						if(repeatMrows==null){
							rowcolMap.remove(Integer.parseInt(repeatRowNum));
						}
					}
				}
			}
		}
		 if(repeatMrows!=null){
			 for(String m:isRepeatMsg){
				 String [] str = m.split("_");
				 if(StringUtils.isNotBlank(str[0]+"_"+str[1])){
					 excelNameMap.put(str[0]+"_"+str[1],null);
				 }
			 }
		 }
		 if(repeatMrows==null){
			 excelNameMap.clear();
		 }
		preData.put("rowcolMap", rowcolMap);
		preData.put("studentNameMap", studentNameMap);
		preData.put("studentCardMap", studentCardMap);
		preData.put("studentNumberMap", studentNumberMap);
		preData.put("eCardMap", eCardMap);
		preData.put("excelNameMap", excelNameMap);
		if (wrongMsg != null && wrongMsg.size() > 0)
		{
			rs.put("ckRs", false);
			//wrongMsg排序
			//按姓名 -班级-性别-年级-家长手机号码-学籍号-学号-座位号-电子卡号排序
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
}
