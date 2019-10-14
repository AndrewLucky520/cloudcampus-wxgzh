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
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.FileImportInfoDao;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.util.OutputMessage;
import com.talkweb.csbasedata.dao.OrgManageDao;
import com.talkweb.csbasedata.service.OrgImportManageService;
import com.talkweb.csbasedata.service.OrgManageService;
import com.talkweb.csbasedata.util.ImportTaskParameter;
import com.talkweb.csbasedata.util.SortUtil;
import com.talkweb.filemanager.service.FileServer;
/**
 * 机构导入-SerivceImpl
 * @author zhh
 *
 */
@Service("orgImportManageService")
public class OrgImportManageServiceImpl implements OrgImportManageService {
	@Autowired
	private OrgManageDao orgManageDao;
	@Autowired
	private OrgManageService orgManageService;
	@Autowired
	private FileServer fileServerImplFastDFS; //文件服务
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['tempFilePath']}")
	private String tempFilePath;
	
	@Autowired
	private FileImportInfoDao fileImportDao;  //操作文件数据库
	
	@Resource(name="redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;	   
	
	private static final String headString="orgManage_";
	private static final String headStringd ="orgManage.";
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
	/**
	 * 获取excel文件,检查文件格式
	 * 
	 * @param school
	 *            学校代码
	 * @param file
	 *            MultipartFile
	 * @return JSONObject
	 * @author zhh
	*/
	@Override
	public JSONObject uploadExcel(JSONObject param) throws Exception {
		//MultipartFile file = (MultipartFile) param.get("file");
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
			stuTitle[0] = "科室名称";
			stuTitle[1] = "姓名";
			stuTitle[2] = "是否是领导";
			
			stuTitleName = new String[stuTitle.length];
			stuTitleName[0] = "departmentName";
			stuTitleName[1] = "name";
			stuTitleName[2] = "isLeader";
			
			stuTitleNeed = new int[stuTitle.length];
			stuTitleNeed[0] = 1;
			stuTitleNeed[1] = 1;
			stuTitleNeed[2] = 1;

		}
		obj.put("stuTitle",stuTitle );
		obj.put("stuTitleName", stuTitleName);
		obj.put("stuTitleNeed", stuTitleNeed);
		int code = 1;
		String msg = "";
		// 临时文件保存目录
		/*File dir = new File(tempFilePath);
		if (!dir.exists()) {
			dir.mkdir();
		}*/
		//String s="";
		try {
			/*// 初步解析上传的excel，判断字段是否符合原始系统字段
		    s = UUID.randomUUID().toString();
			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			//文件名后缀转小写
			fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +prefix;
			df = new File(tempFilePath + "/" + s + "." + prefix);
			file.transferTo(df);*/
			
			
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
					if (isInArray("科室名称", tempStuImpExcTitle)&&isInArray("姓名", tempStuImpExcTitle)
							&&isInArray("是否是领导", tempStuImpExcTitle)) {
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
			e.printStackTrace();
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
	public JSONObject continueImport(JSONObject param) throws Exception {
		String processId = param.getString("processId");
		String keyId = param.getString("keyId");
		int code = 1;
		String schoolId= param.getString("schoolId");
		String msg = "正常启动！";
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
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		try {
			prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
			progressMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyProgressMap);
		} catch (Exception e) {
			e.printStackTrace();
			rs.put("code", -50);
			JSONObject data = new JSONObject();
			data.put("msg", "redis获取数据异常..");
			rs.put("data", data);
			  delFileDBAndFileServer(schoolId,headString+keyId);
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
	public JSONObject startImportTask(JSONObject param) throws Exception {
		ImportTaskParameter stt = new ImportTaskParameter();
		String [] stuTitleName = (String[]) param.get("stuTitleName");
		int [] stuTitleNeed = (int[]) param.get("stuTitleNeed");
		String [] stuTitle = (String[]) param.get("stuTitle");
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
	private int setAllCommonInfo(ImportTaskParameter stt) {
		JSONObject obj = new JSONObject();
		int code = 1;
		//获取所有科室机构及机构领导/成员信息
		obj.put("code", 0);
		obj.put("orgType", stt.getOrgType());
		obj.put("schoolId", stt.getSchoolId());
		obj.put("isQueryMember", 1);
		List<JSONObject> orgList = orgManageDao.getOrgInfos(obj);
		Map<String,String> namePositionMap = new HashMap<String, String>();
		Map<String,String> nameMap = new HashMap<String, String>();
		Map<String,String> depatmentNameMap = new HashMap<String, String>();
		Map<String,String> idMap = new HashMap<String, String>();
		for(JSONObject org:orgList){
			String orgType = org.getString("orgType");
			String orgId = org.getString("orgId");
			String uuid = org.getString("uuid");
			String name = org.getString("name");
			String orgName = org.getString("orgName");
			if(StringUtils.isBlank(orgName) || StringUtils.isBlank(orgType)
					||StringUtils.isBlank(orgId)||StringUtils.isBlank(uuid)){
				continue;
			}
			idMap.put(uuid, orgId);
			depatmentNameMap.put(orgName, uuid);
			
			if(StringUtils.isBlank(name)){continue;}
			namePositionMap.put(orgName+"_"+name+"_"+orgType, name);
			nameMap.put(orgName+"_"+name, name);
			
		}
		//获取所有的老师
		Map<String,String> teacherNameUserIdMap = new HashMap<String, String>();
		List<JSONObject> teacherList = orgManageDao.getTeacherListBySchoolId(obj);
		for(JSONObject teacher:teacherList){
			String userId = teacher.getString("userId");
			String name = teacher.getString("name");
			if(StringUtils.isBlank(name)||StringUtils.isBlank(userId)){continue;}
			teacherNameUserIdMap.put(name, userId);
		}
		Object keyNamePositionMap =  headStringd +stt.getSchoolId()+stt.getProcessId()+".00.namePositionMap";
		Object keyTeacherNameUserIdMap = headStringd +stt.getSchoolId()+stt.getProcessId()+".00.teacherNameUserIdMap";
		Object keyIdMap = headStringd +stt.getSchoolId()+stt.getProcessId()+".00.idMap";
		Object keyDepatmentNameMap = headStringd +stt.getSchoolId()+stt.getProcessId()+".00.depatmentNameMap";
		Object keyNameMap = headStringd +stt.getSchoolId()+stt.getProcessId()+".00.nameMap";
		Object keyOrgList = headStringd+stt.getSchoolId()+stt.getProcessId()+".00.orgList";
		try {
			redisOperationDAO.set(keyNamePositionMap, namePositionMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyTeacherNameUserIdMap, teacherNameUserIdMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyIdMap, idMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyNameMap, nameMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyDepatmentNameMap, depatmentNameMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(keyOrgList, orgList, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			delFileDBAndFileServer(stt.getSchoolId(),headString+stt.getKeyId());
			e.printStackTrace();
			code = -50;
		}
		return code;
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
									int num = orgManageService.addImportDepartmentBatch(needInsert);						
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
	 * @param data 
	 * @param keyProgressMap 
	 * @param keyProgressMap2 
	 * @param progressMap 
	 */
@SuppressWarnings("unchecked")
private Map<String,Object> getInsertEntityByCkrs( Object keyProgressMap,  ImportTaskParameter sp, String processId,Hashtable<String, JSONObject> progressMap) {
	Map<String,Object>  returnParam = new HashMap<String,Object>();
	int noNeedInsertNum = 0 ;
	String schoolId=sp.getSchoolId();
	String keyId = sp.getKeyId();
	JSONObject toFront = progressMap.get(processId);
	Object keyPrepDataMap =headStringd+sp.getSchoolId()+processId+".00.prepDataMap";
	Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
	Object keyDepatmentNameMap = headStringd +schoolId+processId+".00.depatmentNameMap";
	Object keyNameMap = headStringd +schoolId+processId+".00.nameMap";
	Object keyTeacherNameUserIdMap = headStringd +schoolId+processId+".00.teacherNameUserIdMap";
	Object keyIdMap = headStringd +schoolId+processId+".00.idMap";
	Object keyNamePositionMap =  headStringd +schoolId+processId+".00.namePositionMap";
	Map<String,String> setedNameMap = new HashMap<String, String>();
	Map<String,String> setedDepartmentNameMap = new HashMap<String, String>();
	Map<String,String> idMap = new HashMap<String,String>();
	Map<String,String> teacherNameUserIdMap = new HashMap<String, String>();
	Map<String,String> namePositionMap = new HashMap<String, String>();
	try {
		namePositionMap =  (Map<String, String>) redisOperationDAO.get(keyNamePositionMap);
		teacherNameUserIdMap = (Map<String, String>) redisOperationDAO.get(keyTeacherNameUserIdMap);
		idMap = (Map<String, String>) redisOperationDAO.get(keyIdMap);
		setedNameMap = (Map<String, String>) redisOperationDAO.get(keyNameMap);
		setedDepartmentNameMap = (Map<String, String>) redisOperationDAO.get(keyDepatmentNameMap);
		prepDataMap = (Hashtable<String, JSONObject>) redisOperationDAO.get(keyPrepDataMap);
	} catch (Exception e) {
		e.printStackTrace();
		JSONObject data = new JSONObject();
		toFront.put("code", -50);
        data.put("progress", 100);
        data.put("msg", "redis缓存失败。");
        toFront.put("data", data);
        progressMap.put(processId, toFront);
        delFileDBAndFileServer(schoolId,headString+keyId);
	}
	List<JSONObject> insertLeaderList = new ArrayList<JSONObject>();
	List<JSONObject> insertMemberList = new ArrayList<JSONObject>();
	List<JSONObject> insertStaffList = new ArrayList<JSONObject>();
	List<String> deleteStaffList = new ArrayList<String>();
	List<JSONObject> deleteLeaderList = new ArrayList<JSONObject>();
	List<JSONObject> deleteMemberList = new ArrayList<JSONObject>();
	if(prepDataMap!=null){
		HashMap<Integer, JSONObject> rows = (HashMap<Integer, JSONObject>) prepDataMap
				.get(processId).get("rowcolMap");
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
			String departmentName=row.getString("departmentName");
			String name=row.getString("name");
			String isLeader=row.getString("isLeader");
		
			int type = 3;
			if(namePositionMap.get(departmentName+"_"+name+"_1")!=null){ //原先有领导身份
				type=1;
			}else if(namePositionMap.get(departmentName+"_"+name+"_2")!=null){ //原先有成员身份
				type=2;
			}else{ //原先无身份，新创建
				type=3;
			}
			String uuid = setedDepartmentNameMap.get(departmentName);
			if(StringUtils.isBlank(uuid)){continue;}
			String orgId = idMap.get(uuid);
			String userId = teacherNameUserIdMap.get(name);
			if(StringUtils.isBlank(orgId) && StringUtils.isBlank(userId)){continue;}
			if("是".equals(isLeader)){//待插入的身份是领导
				//判断原先身份是否为领导 如果是则忽略
				if(type==1){
					noNeedInsertNum++;
					continue;
				}else if(type==2) { //判断原先身份是成员
					//删除成员
					JSONObject deleteMember  = new JSONObject();
					deleteMember.put("orgId", orgId);
					deleteMember.put("userId", userId);
					deleteMemberList.add(deleteMember);
				}
				//新增领导
				JSONObject insertLeader = new JSONObject();
				insertLeader.put("orgId", orgId);
				insertLeader.put("userId", userId);
				insertLeaderList.add(insertLeader);
				//新增staff
				JSONObject insertStaff = new JSONObject();
				insertStaff.put("userId", userId);
				insertStaff.put("schoolId", schoolId);
				insertStaff.put("jobType", 0);
				insertStaffList.add(insertStaff);
			}else{ //待插入的身份为成员
				if(type==1){ //原先身份为领导
					//删除领导
					JSONObject deleteLeader = new JSONObject();
					deleteLeader.put("orgId", orgId);
					deleteLeader.put("userId", userId);
					deleteLeaderList.add(deleteLeader);
					//删除staff
					if(!deleteStaffList.contains(userId)){
						deleteStaffList.add(userId);
					}
				}else if(type==2){ //原先身份为成员
					noNeedInsertNum++;
					continue;
				}
				//新增成员
				JSONObject insertMember = new JSONObject();
				insertMember.put("orgId", orgId);
				insertMember.put("userId", userId);
				insertMemberList.add(insertMember);
			}
		}
		returnParam.put("insertLeaderList", insertLeaderList);
		returnParam.put("insertMemberList", insertMemberList);
		returnParam.put("insertStaffList", insertStaffList);
		returnParam.put("deleteLeaderList", deleteLeaderList);
		returnParam.put("deleteMemberList", deleteMemberList);
		returnParam.put("deleteStaffList", deleteStaffList);
		returnParam.put("schoolId", schoolId);
		returnParam.put("noNeedInsertNum", noNeedInsertNum);
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

		//redis获取departmentNameMap
		Object keyTeacherNameUserIdMap = headStringd +schoolId+processId+".00.teacherNameUserIdMap";
		Object keyDepatmentNameMap = headStringd +schoolId+processId+".00.depatmentNameMap";
		Map<String,String> setedDepartmentNameMap = new HashMap<String, String>();
		Map<String,String> teacherNameUserIdMap = new HashMap<String, String>();
		Object keyPrepDataMap = headStringd+schoolId+processId+".00.prepDataMap";
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		Object keyArr = headStringd+schoolId+processId+".00.arr";
		String [] arr = null;
 		try {
 			arr = (String[]) redisOperationDAO.get(keyArr);
			teacherNameUserIdMap = (Map<String, String>) redisOperationDAO.get(keyTeacherNameUserIdMap);
			setedDepartmentNameMap = (Map<String, String>) redisOperationDAO.get(keyDepatmentNameMap);
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

		JSONObject nameMap =preData.getJSONObject("nameMap"); //科室名称_姓名，姓名 
		//name-rowNums     防止excel名称重复(重复则多条都要显示出来)
		Map<String,LinkedHashSet<String>> excelNameMap =(LinkedHashMap<String, LinkedHashSet<String>>) preData.get("excelNameMap");
				
		if(nameMap==null){
			nameMap=new JSONObject();
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

		LinkedHashMap<Integer, JSONObject> rowcolMap= new LinkedHashMap<Integer, JSONObject>(); //待插入的数据保存
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
		Map<Integer,JSONObject> isExcelRepeatMap = new HashMap<Integer,JSONObject>(); //是否已匹配重复
		for (Iterator<Integer> it = pureDatas.keySet().iterator(); it.hasNext();) 
		{
			index++;
			boolean isWsg=false;
			boolean isDepartmentWsg=false;
			int rowNum = it.next();
			JSONObject pd = pureDatas.get(rowNum);
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			JSONArray mrows = new JSONArray();
			wmsg.put("mrows", mrows);
			
			// excel必输入字段验证
			String departmentName = pd.containsKey("departmentName") ? pd.getString("departmentName"):null;
			String name = pd.containsKey("name") ? pd.getString("name"): null;
			String isLeader = pd.containsKey("isLeader") ? pd.getString("isLeader"):null;

			

			// excel验证科室名称不能在系统中没有，不能为空
			String title=pd.getString("departmentNameName");
			if (departmentName != null && departmentName.trim().length() > 0) {
				 //判断系统中是否有科室
				if(departmentName.trim().length()>30){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", departmentName);
					wsg.put("err", "输入值不合法！");
					isWsg=true;
					isDepartmentWsg=true;
					mrows.add(wsg);
				}else if(setedDepartmentNameMap!=null && !setedDepartmentNameMap.containsKey(departmentName.trim())){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", departmentName);
					wsg.put("err", "无匹配记录！");
					isWsg=true;
					isDepartmentWsg=true;
					mrows.add(wsg);
				}
			}else{
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", departmentName);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}
			
			//excel验证姓名在一个科室中不能重复，不能为空
			title=pd.getString("nameName");
			if (name != null && name.trim().length() > 0) {
				if(teacherNameUserIdMap!=null && !teacherNameUserIdMap.containsKey(name.trim())){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", name);
					wsg.put("err", "无匹配记录！");
					isWsg=true;
					mrows.add(wsg);
				}else {
					LinkedHashSet<String> repeatNameList = new LinkedHashSet<String>();
					if(excelNameMap.containsKey(departmentName.trim()+"_"+name.trim()) ){	
							repeatNameList = excelNameMap.get(departmentName.trim()+"_"+name.trim());
							if(repeatNameList!=null && !repeatNameList.contains(rowNum+"")){
								repeatNameList.add(rowNum+"");
								JSONObject json = new JSONObject();
								//json.put("excelRepeatKey", teacherName);
								json.put("repeatTitle", title);
								json.put("repeatDepartmentTitle",pd.getString("departmentNameName"));
								json.put("isRepeat", true);
								json.put("departmentName",departmentName);
								json.put("teacherName", name);
								json.put("isDepartmentWsg", isDepartmentWsg);
								isExcelRepeatMap.put(rowNum, json);
							}else if(repeatNameList!=null && repeatNameList.contains(rowNum+"")){
								repeatNameList.add(rowNum+"");
								excelNameMap.put(departmentName.trim()+"_"+name.trim(), repeatNameList);
								JSONObject json = new JSONObject();
								json.put("isRepeat", false);
								isExcelRepeatMap.put(rowNum, json);
							}else{
								JSONObject wsg = new JSONObject();
								wsg.put("title", title);
								wsg.put("oldValue", name);
								wsg.put("err", "excel中重复！");
								isWsg=true;
								mrows.add(wsg);
								JSONObject wsg1 = new JSONObject();
								wsg1.put("title",  pd.getString("departmentNameName"));
								wsg1.put("oldValue", departmentName);
								wsg1.put("err", "excel中重复！");
								mrows.add(wsg1);
							}
					}else{
						repeatNameList.add(rowNum+"");
						excelNameMap.put(departmentName.trim()+"_"+name.trim(), repeatNameList);
						JSONObject json = new JSONObject();
						json.put("isRepeat", false);
						isExcelRepeatMap.put(rowNum, json);
					}
				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", name);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}
			//excel验证isLeader数据合理性
			title=pd.getString("isLeaderName");
			if (!(isLeader != null && isLeader.trim().length() > 0)) {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", isLeader);
				wsg.put("err", "不能为空！");
				mrows.add(wsg);
				isWsg=true;
			}else{
				if (!"是".equals(isLeader.trim()) && !"否".equals(isLeader.trim())) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", isLeader);
					wsg.put("err", "输入值不合法！");
					mrows.add(wsg);
					isWsg=true;
				}
			}
			if (mrows.size() > 0) 
			{
				if( isNameRepeat ||isWsg)
				{
						nameMap.remove(departmentName.trim()+"_"+name.trim());
				}				
				wrongMsg.add(wmsg);
			} 
			else 
			{
				wmsg = null;
			}
			
			if(mrows.size()==0)
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
			String repeatDepartmentTitle = isExcelRepeatJSON.getString("repeatDepartmentTitle");
			String teacherName = isExcelRepeatJSON.getString("teacherName");
			String departmentName = isExcelRepeatJSON.getString("departmentName");
			Boolean isDepartmentWsg = isExcelRepeatJSON.getBoolean("isDepartmentWsg");
			LinkedHashSet<String> repeatRowNums = excelNameMap.get(departmentName.trim()+"_"+teacherName.trim());
			for(String repeatRowNum:repeatRowNums){
				boolean isFind = false; //是否找到
				for(int g = 0;g<wrongMsg.size();g++){
					JSONObject wmsg1 = (JSONObject) wrongMsg.get(g);
					int rowN = wmsg1.getIntValue("row");
					if(!repeatRowNum.equals(rowN+"") || rowNum<rowN){continue;}
					if(isRepeatMsg.contains(departmentName.trim()+"_"+teacherName.trim()+"_"+repeatRowNum)){
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
						if(isDepartmentWsg!=null &&!isDepartmentWsg ){
							JSONObject wsg2 = new JSONObject();
							wsg2.put("title", repeatDepartmentTitle);
							wsg2.put("oldValue", departmentName);
							wsg2.put("err", "excel中重复！");
								mrows1.add(wsg2);
						}
						isFind=true;
						isRepeatMsg.add(departmentName.trim()+"_"+teacherName.trim()+"_"+repeatRowNum);
						if(repeatMrows==null){
							rowcolMap.remove(Integer.parseInt(repeatRowNum));
						}
					}
				}
				//第一次的情况 即wrongMsg为空
				if(!isFind && null!=(excelNameMap.get(departmentName.trim()+"_"+teacherName.trim())) && !isRepeatMsg.contains(departmentName.trim()+"_"+teacherName.trim()+"_"+repeatRowNum)){
					JSONObject wmsg1 = new JSONObject();
					JSONArray mrows1=new JSONArray();
					JSONObject wsg1 = new JSONObject();
					wsg1.put("title", repeatTitle);
					wsg1.put("oldValue", teacherName);
					wsg1.put("err", "excel中重复！");
					mrows1.add(wsg1);
					if(isDepartmentWsg!=null &&!isDepartmentWsg ){
						JSONObject wsg2 = new JSONObject();
						wsg2.put("title", repeatDepartmentTitle);
						wsg2.put("oldValue", departmentName);
						wsg2.put("err", "excel中重复！");
							mrows1.add(wsg2);
					}
					wmsg1.put("row", repeatRowNum);
					wmsg1.put("mrows", mrows1);
					wrongMsg.add(wmsg1);
					isRepeatMsg.add(departmentName.trim()+"_"+teacherName.trim()+"_"+repeatRowNum);
					if(repeatMrows==null){
						rowcolMap.remove(Integer.parseInt(repeatRowNum));
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
		preData.put("nameMap", nameMap);
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
