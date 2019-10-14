package com.talkweb.elective.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.StringNumTool;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.commondata.util.NameRepeatJudge;
import com.talkweb.elective.domain.ImportTaskParameter;
import com.talkweb.elective.service.ElectiveService;
import com.talkweb.filemanager.service.FileServer;



@Controller
@RequestMapping("/elective/import")
@SuppressWarnings("rawtypes")
public class ElectiveImportAction  extends BaseAction  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3051509131916777461L;
	@Autowired
	private ElectiveService electiveService;
    @Autowired
    private AllCommonDataService allCommonService;
	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	@Autowired
	private FileServer fileServerImplFastDFS;
	@Autowired
	private FileImportInfoService fileImportInfoService;
	private static final Logger logger = LoggerFactory.getLogger(ElectiveImportAction.class);

	/**
	 * 导入成绩excel的表头--系统字段: 课程名称
	 */
	private static String[] stuTitle = null;

	/**
	 * 导入成绩excel的表头--系统字段字段名:courseName
	 */
	private static String[] stuTitleName = null;
	/**
	 * 导入成绩excel的表头--系统字段字段名 是否必录 0:否 1：是
	 */
	private static int[] stuTitleNeed = null;

    static
    {
		if (stuTitle == null) {
			stuTitle = new String[10];
			stuTitle[0] = "课程名称";
			stuTitle[1] = "人数上限";
			stuTitle[2] = "开课时间";
			stuTitle[3] = "周次";
			stuTitle[4] = "选课年级";
			stuTitle[5] = "班级至多可选人数";
			stuTitle[6] = "适用性别";
			stuTitle[7] = "授课教师";
			stuTitle[8] = "教学场地";
			stuTitle[9] = "课程简介";
			
			stuTitleName = new String[stuTitle.length];
			stuTitleName[0] = "courseName";
			stuTitleName[1] = "maxNum";
			stuTitleName[2] = "schoolTime";
			stuTitleName[3] = "weekType";
			stuTitleName[4] = "offerGrade";			
			stuTitleName[5] = "classMaxNum";
			stuTitleName[6] = "adaptSex";
			stuTitleName[7] = "teachers";
			stuTitleName[8] = "classroom";
			stuTitleName[9] = "courseDesc";
			
			stuTitleNeed = new int[stuTitle.length];
			stuTitleNeed[0] = 1;
			stuTitleNeed[1] = 1;
			stuTitleNeed[2] = 1;
			stuTitleNeed[3] = 0;
			stuTitleNeed[4] = 1;
			stuTitleNeed[5] = 0;
			stuTitleNeed[6] = 0;
			stuTitleNeed[7] = 0;
			stuTitleNeed[8] = 0;
			stuTitleNeed[9] = 0;
		}
    }
  //清除相关redis和session的信息
	private void deleteRedisAndSessionContent(String sessionId) throws Exception{
		String excelNameMapKey="elective."+sessionId+".excelNameMap";
		String nameRepeatMapKey="elective."+sessionId+".nameRepeatMap";
		redisOperationDAO.del(nameRepeatMapKey);
		redisOperationDAO.del(excelNameMapKey);
	}
	/**
	 * Excel上传接口
	 * 
	 * @param file
	 * @param req
	 * @param res
	 * @throws Exception 
	 * @throws IOException 
	 */
	@RequestMapping(value = "/uploadExcel")
	@ResponseBody
	public JSONObject uploadExcel(
			@RequestParam("excelBody") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		JSONObject obj = new JSONObject();
		logger.debug("导入sessionID:"+req.getSession().getId());
		String schoolId=getXxdm(req);
		logger.debug("获取学校代码："+schoolId);
		int code = 1;
		String msg = "";
		// 临时文件保存目录
		File df = null;
		String s = UUID.randomUUID().toString();
		String keyId="elective_"+s;
		String fileId ="";
		try {
			// 初步解析上传的excel，判断字段是否符合原始系统字段
			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			//扩展名并转小写
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			//文件名后缀转小写
			fileName = fileName.substring(0,fileName.lastIndexOf(".")+1) +prefix;
			df = new File(s + "." + prefix);
			logger.debug("目标文件："+ s + "." + prefix);
			file.transferTo(df);
			
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
				//判断4个必填项标识是否存在
				if (code == 1) {
					if (isInArray("课程名称", tempStuImpExcTitle)&&isInArray("人数上限", tempStuImpExcTitle)
							&&isInArray("开课时间", tempStuImpExcTitle)&&isInArray("选课年级", tempStuImpExcTitle)) {
						code = 1;
					} 
					else {
						code = -3;
						msg = "请检查必填字段标识是否正确！";
					}
				}
				
				if(code>=0)
				{
					fileId = fileServerImplFastDFS.uploadFile(df,fileName);
					req.getSession().setAttribute("fileId", fileId);
					fileImportInfoService.addFile(schoolId, keyId, fileId);
					req.getSession().setAttribute("keyId", keyId);
					req.getSession().setAttribute("elective."+this.getXxdm(req)+".excelTitle", tempStuImpExcTitle);
				}
				
			} else {
				code = -2102;
				msg = OutputMessage.getDescByCode(code + "");
			}
			workbook.close();
		} catch (Exception e) {
			logger.error("uploadExcel", e);
			code = -2101;
			msg = "文件格式错误或无法读取！";
			msg = OutputMessage.getDescByCode(code + "");
			deleteRedisAndSessionContent(req.getSession().getId());
			if(null!=fileId && StringUtils.isNotEmpty(fileId))
			{
				fileServerImplFastDFS.deleteFile(fileId);
				fileImportInfoService.deleteFile(schoolId, keyId);
			}
		}finally{
			if(null!=df)
			{
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
	 * @return
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
	 * @return
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
	 * 
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelHead(HttpServletRequest req) throws Exception {
		JSONObject excelHead = new JSONObject();
		// 获取session中保存的临时表头
		String[] tmpTit =(String[]) req.getSession().getAttribute("elective."+this.getXxdm(req)+".excelTitle");
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
		JSONObject rs = new JSONObject();
		rs.put("excelHead", excelHead);
		rs.put("moduleField", moduleField);
		return rs;

	}

	/**
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/startImportTask")
	@ResponseBody
	public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody JSONObject mappost, HttpServletResponse res) throws Exception {
		String selectedSemester=mappost.getString("selectedSemester");
		if(selectedSemester==null || StringUtils.isBlank(selectedSemester)){
			selectedSemester=getCurXnxq(req);
		}
		String termInfoId = mappost.getString("termInfoId");
        String schoolYear = termInfoId.substring(0, termInfoId.length() - 1);
		String term = termInfoId.substring(termInfoId.length() - 1);
		ImportTaskParameter stt = new ImportTaskParameter();
		String schoolId=this.getXxdm(req);
		stt.setIsMatch(mappost.getIntValue("isMatch"));
		if(stt.getIsMatch()==1)
		{
			stt.setMatchResult(mappost.getJSONArray("matchResult"));
		}
		stt.setSchoolYear(schoolYear);
		stt.setTerm(term);
		stt.setSchoolId(schoolId);
		stt.setElectiveId(mappost.getString("electiveId"));
		// 设置获取的参数
		String processId = req.getSession().getId();
		stt.setProcessId(processId);
		//设置公共参数
		this.setAllCommonInfo(req, mappost.getString("electiveId"),schoolYear,selectedSemester);
		JSONObject procObj = new JSONObject();
		procObj.put("taskParam", stt);
		Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
		progressMap.put(processId, procObj);
		String progressMapKey="elective."+processId+".progressMap";
		redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		
		JSONObject obj = new JSONObject();
        obj.put("code", 0);
        obj.put("msg", "正常启动任务");
        String keyId=req.getSession().getAttribute("keyId").toString();
		SubProcess sp = new SubProcess((HashMap)req.getSession().getAttribute("elective."+schoolId+".commonInfoMap"),keyId,processId, 0, null,schoolId ,  req);
		sp.start();		

		JSONObject data = new JSONObject();
		data.put("progress", 0);
		data.put("msg", "正常启动任务");
		procObj.put("code", 0);
		procObj.put("data", data);
		logger.debug("主线程结束！");
		return obj;

	}

	/**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req,
			HttpServletResponse res) {
		String processId = req.getSession().getId();
		String schoolId=this.getXxdm(req);
		int code = 1;
		String msg = "正常启动！";
		JSONObject obj = new JSONObject();
		String keyId=req.getSession().getAttribute("keyId").toString();
		try {

			SubProcess sp = new SubProcess((HashMap)req.getSession().getAttribute("elective."+schoolId+".commonInfoMap"),keyId,processId, 1, null,getXxdm(req) , req);
			sp.start();
		} catch (Exception e) {
			logger.error("continueImport", e);
			try {
				deleteRedisAndSessionContent(req.getSession().getId());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
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
	 * 导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/singleDataCheck")
	@ResponseBody
	public synchronized JSONObject singleDataCheck(HttpServletRequest req,
			@RequestBody JSONObject post, HttpServletResponse res) throws Exception {
		JSONObject rs = new JSONObject();
		String processId = req.getSession().getId();
		JSONObject data = new JSONObject();
//		String selectedSemester=post.getString("selectedSemester");
		int row = post.getIntValue("row");
		JSONArray mrows = post.getJSONArray("mrows");
		int code = post.getIntValue("code");
		rs.put("rowNum", row);
		
		Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
		String progressMapKey="elective."+processId+".progressMap";
		Object progressMapObj=redisOperationDAO.get(progressMapKey);
		if(null!=progressMapObj)
		{
			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
		}
		
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		String prepDataMapKey="elective."+processId+".prepDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
		if(null!=prepDataMapObj)
		{
			prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
		}
		if(null==progressMapObj||null==prepDataMapObj)
		{
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			rs.put("code", -50);
			rs.put("data", data);
			progressMap.put(processId, rs);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			return rs;
		}
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
			String schoolId = sp.getSchoolId();
			String termInfoId = sp.getSchoolYear()+sp.getTerm();
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
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			String nameRepeatMapKey="elective."+processId+".nameRepeatMap";
			Map<String,String> nameRepeatMap = (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
			JSONObject json = new JSONObject();
			json.put("pureDatas", pureDatas);
			List<JSONObject> titleNames = new ArrayList<JSONObject>();
			JSONObject titleName = new JSONObject();
			titleName.put("titleName","teachers" );
			titleName.put("splitIndex","," );
			titleNames.add(titleName);
			json.put("titleNames", titleNames);
			String accountListKey="elective."+processId+".accountList";
			List<Account> accoutList = (List<Account>) redisOperationDAO.get(accountListKey);
			json.put("list", accoutList);
			json.put("roleType", "Teacher");
			json.put("plate", 1);
			json.put("nameRepeatMap", nameRepeatMap);
			json.put("excelType", "2");
			json.put("excelRule", "teachers");
			
			JSONObject returnRepeatName = NameRepeatJudge.judgeNameRepeatImport(json);
			JSONArray wrongMsg = new JSONArray();
			Map<String,String>  stringMap = new HashMap<String, String>(); 
			if(returnRepeatName!=null){
				nameRepeatMap = (Map<String, String>) returnRepeatName.get("nameRepeatMap");
				wrongMsg = (JSONArray) returnRepeatName.get("wrongMsg");
				stringMap = (Map<String, String>) returnRepeatName.get("stringMap");
			}
			String stringMapKey="elective."+processId+".stringMap";
			redisOperationDAO.set(stringMapKey, stringMap);
			redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
			JSONObject cr = checkImpData(stringMap,nameRepeatMap,wrongMsg,(HashMap) req.getSession().getAttribute("elective."+this.getXxdm(req)+".commonInfoMap"),pureDatas, mrs, isMatch, processId, sp);
			redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
			if (cr.getBooleanValue("ckRs")) {
				rs.put("code", 1);
				data.put("msg", "校验通过！");
				rs.put("data", data);
			} else {
				if(cr.containsKey("errorCode"))
				{
					rs.put("code", -50);
					data.put("progress", 100);
					data.put("msg", "由于长时间未操作，请重新导入！");
					rs.put("data", data);
					progressMap.put(processId, rs);
					return rs;
				}
				rs.put("code", -1);
				data.put("msg", "校验不通过！");
				rs.put("data", data);
				rs.put("mrows", cr.getJSONArray("wrongMsg").getJSONObject(0)
						.get("mrows"));
			}
		}
		return rs;
	}

	/**
	 * 获取导入进度
	 * 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/importProgress")
	@ResponseBody
	public JSONObject importProgress(HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		JSONObject rs = new JSONObject();
		Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
		String progressMapKey="elective."+req.getSession().getId()+".progressMap";
		Object progressMapObj=redisOperationDAO.get(progressMapKey);
		if(null!=progressMapObj)
		{
			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
		}
		else
		{
			JSONObject data = new JSONObject();
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			rs.put("code", -50);
			rs.put("data", data);
			progressMap.put(req.getSession().getId(), rs);
		}
		JSONObject obj = progressMap.get(req.getSession().getId());
		if(progressMap!=null&&obj!=null){
			if(obj.getIntValue("code")==-50)
			{
				String schoolId=this.getXxdm(req);
				String keyId=req.getSession().getAttribute("keyId").toString();
				String fileId = fileImportInfoService.getFileBy(schoolId, keyId);
				if(null!=fileId && StringUtils.isNotEmpty(fileId))
				{
					fileServerImplFastDFS.deleteFile(fileId);
				}
				if(null!=keyId && StringUtils.isNotEmpty(keyId))
				{
					fileImportInfoService.deleteFile(schoolId, keyId);
				}
			}
			rs.put("code", obj.get("code"));
			rs.put("data", obj.get("data"));
		}else{
			rs.put("code",-100);
			rs.put("data", "用户身份信息已失效，请重新登陆！");
		}
		redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		return rs;
	}
	@SuppressWarnings("unused")
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

		private JSONObject singleData;
		
		private HashMap commonInfoMap;
		
		private String keyId;
		
		private String fileId;
		
		public SubProcess(HashMap commonInfoMap,String keyId,String processId, int impType, JSONObject singleData,String schoolId ,HttpServletRequest req) {
			this.processId = processId;
			this.impType = impType;
			this.singleData = singleData;
			this.schoolId= schoolId;
			this.commonInfoMap=commonInfoMap;
			this.keyId=keyId;
			this.fileId = (String)req.getSession().getAttribute("fileId");// 这个属性还不能删 有continueimport
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			long t1 = (new Date()).getTime();
			int readCode ;
			JSONObject data = new JSONObject();
			JSONObject toFront= new JSONObject();
			String fileIdTmp =null;
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
			String progressMapKey="elective."+processId+".progressMap";
			try 
			{
				
				Object progressMapObj=redisOperationDAO.get(progressMapKey);
				if(null!=progressMapObj)
				{
					progressMap=(Hashtable<String, JSONObject>) progressMapObj;
				}
				else
				{
					toFront.put("code", -50);
					data.put("progress", 100);
					data.put("msg", "由于长时间未操作，请重新导入");
					toFront.put("data", data);
					progressMap.put(processId, toFront);
					redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					return;
				}
				// excel导入处理开始
				ImportTaskParameter sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
				fileIdTmp = fileImportInfoService.getFileBy(schoolId, keyId);//没有读出数据
				if (fileIdTmp!=null) {
					fileId = fileIdTmp;
				}
				String termInfoId = sp.getSchoolYear()+sp.getTerm();
				int isMatch = sp.getIsMatch();
				JSONArray mrs = sp.getMatchResult();

				toFront = progressMap.get(processId);
				data.put("progress", 0);
				data.put("msg", "正在准备导入任务");
				toFront.put("data", data);
				toFront.put("code", 1);
				redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				
				String[] ckExcTt = null;//excel表头
				JSONObject readRs = new JSONObject();
				readRs.put("code", 1);
				JSONArray datas = new JSONArray();
				if (impType == 0) 
				{
					readRs = readExcelToData(fileId,processId);
					datas = readRs.getJSONArray("datas");
					ckExcTt = (String[]) datas.get(0);
				}
				readCode = readRs.getIntValue("code");
				
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
							redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
							JSONObject preDatas = changeData(datas, mrs, isMatch,processId);
							HashMap<Integer, JSONObject> pureDatas = (HashMap<Integer, JSONObject>) preDatas.get("rowDatas");
							
							//@在校验之前添加姓名重复判断-姓名重复的行不再检验了 author：zhh
							String nameRepeatMapKey="elective."+processId+".nameRepeatMap";
							Map<String,String> nameRepeatMap = (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
							JSONObject json = new JSONObject();
							json.put("pureDatas", pureDatas);
							List<JSONObject> titleNames = new ArrayList<JSONObject>();
							JSONObject titleName = new JSONObject();
							titleName.put("titleName","teachers" );
							titleName.put("splitIndex","," );
							titleNames.add(titleName);
							json.put("titleNames", titleNames);
							String accountListKey="elective."+processId+".accountList";
							List<Account> accoutList = (List<Account>) redisOperationDAO.get(accountListKey);
							json.put("list", accoutList);
							json.put("roleType", "Teacher");
							json.put("plate", 1);
							json.put("nameRepeatMap", nameRepeatMap);
							json.put("excelType", "2");
							json.put("excelRule", "teachers");
						
							
							JSONObject returnRepeatName = NameRepeatJudge.judgeNameRepeatImport(json);
							JSONArray wrongMsg = new JSONArray();
							Map<String,String> stringMap = new HashMap<String,String>();
							if(returnRepeatName!=null){
								nameRepeatMap = (Map<String, String>) returnRepeatName.get("nameRepeatMap");
								wrongMsg = (JSONArray) returnRepeatName.get("wrongMsg");
								stringMap = (Map<String, String>) returnRepeatName.get("stringMap");
							}
							String stringMapKey="elective."+processId+".stringMap";
							redisOperationDAO.set(stringMapKey, stringMap);
							redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
							ckrs = checkImpData(stringMap,nameRepeatMap,wrongMsg,commonInfoMap,pureDatas, mrs, isMatch, processId, sp);
							redisOperationDAO.set(nameRepeatMapKey, nameRepeatMap); //存起来，在拼插入JSON的时候使用
							if(!ckrs.getBooleanValue("ckRs")&&ckrs.containsKey("errorCode"))
							{
								toFront.put("code", -50);
								data.put("progress", 100);
								data.put("msg", "由于长时间未操作，请重新导入！");
								toFront.put("data", data);
								deleteRedisAndSessionContent(processId);
								progressMap.put(processId, toFront);
								return;
							}
						}
						logger.debug("查询结果:" + ckrs);
						if (ckrs.getBooleanValue("ckRs") || impType == 1)
						{
							// 开始入库
							data.put("progress", 30);
							data.put("msg", "正在保存数据！");
							toFront.put("data", data);
							progressMap.put(processId, toFront);
							redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
							
							HashMap<String, Object> needInsert = getInsertEntityByCkrs(commonInfoMap,sp, processId);
							List<JSONObject> courseList = (List<JSONObject>) needInsert.get("courseList");
							if (courseList.size() > 0) {
								//@update 添加并发控制   @author zhh
								int num=electiveService.insertBatchElectiveCourse(needInsert);						
								toFront.put("code", 2);
					            data.put("progress", 100);
					            data.put("msg", "导入成功，共计导入"+ num +"条信息记录！");
					            progressMap.put(processId, toFront);
					            toFront.put("data", data);	
					            deleteRedisAndSessionContent(processId);
							}else{
								toFront.put("code", 2);
					            data.put("progress", 100);
					            data.put("msg", "流程正常结束，未导入任何记录！");
					            progressMap.put(processId, toFront);
					            deleteRedisAndSessionContent(processId);
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
							
						}
				}
				else
				{
						if (readCode == -10) {
							toFront.put("code", -1);
							data.put("progress", 100);
							data.put("msg", "服务器出错，上传的文件不存在!");
							progressMap.put(processId, toFront);
							deleteRedisAndSessionContent(processId);
						} else if (readCode == -20) {
							toFront.put("code", -1);
							data.put("progress", 100);
							data.put("msg", "解析Excel时出错，可能为文件格式问题!");
							deleteRedisAndSessionContent(processId);
							progressMap.put(processId, toFront);
						} else if (readCode == -30) {
							toFront.put("code", -1);
							data.put("progress", 100);
							data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
							progressMap.put(processId, toFront);
							deleteRedisAndSessionContent(processId);
						}
				}

			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				logger.error("run:", e);
				e.printStackTrace();
				readCode=-30;
				toFront.put("code", -1);
				data.put("progress", 100);
				data.put("msg", "解析Excel时出错，请检查Excel格式及内容或联系管理员!");
				try {
					deleteRedisAndSessionContent(processId);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				progressMap.put(processId, toFront);
			}
			finally
			{
				// excel导入处理结束
				long t2 = (new Date()).getTime();
				logger.info("导入子线程结束,耗时：" + (t2 - t1));
				logger.info("开始删除临时excel");

				try {
					redisOperationDAO.set(progressMapKey, progressMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					//在删除表中的记录
					if (null != fileId && StringUtils.isNotEmpty(fileId)) {
						fileServerImplFastDFS.deleteFile(fileId);
					}
					if (null != keyId && StringUtils.isNotEmpty(keyId)) {
						fileImportInfoService.deleteFile(schoolId, keyId);
					}
					deleteRedisAndSessionContent(processId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("选课：删除文件服务器上面的临时文件失败！");
				}
			}
		}

	}


	/**
	 * 根据映射字段映射生成数据
	 * 
	 * @param datas excel表格数据
	 * @param mrs  匹配结果{sysField:系统字段,excelField：excel字段,mustField：是否必录, 1 必录（前端验证时需注意规则，所有必录项需匹配）
	 * @param isMatch 是否需要手工匹配,1 手工匹配，0 无需匹配
	 * @param processId 进程id
	 * @return
	 * @throws Exception 
	 */
	private JSONObject changeData(JSONArray datas, JSONArray mrs, int isMatch,String processId) throws Exception {
		JSONArray exDatas = new JSONArray();
		HashMap<Integer, JSONObject> rowDatas = new HashMap<Integer, JSONObject>();
		HashMap<String, String> titleEnMap = new HashMap<String, String>();

		String[] titles = (String[]) datas.get(0);//得到excel第一行表头
		//存储导入excel表头arr[0][]  是否必填arr[1][]  映射的系统字段：课程名称arr[2][]  映射实体字段：courseName arr[3][]
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
			int rowNum = Integer.parseInt(cell[arr[0].length-1 ])+1;
			d.put("rowNum", rowNum);
			rowDatas.put(rowNum, d);
			exDatas.add(d);
		}

		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		rs.put("titleEnMap", titleEnMap);
		rs.put("impHead", arr[3]);
		
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		prepDataMap.put(processId, rs);
		String prepDataMapKey="elective."+processId+".prepDataMap";
		redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		return rs;
	}

	/**
	 * 将excel读取为数据
	 * 
	 * @param fileId 文件服务器对应的fileId
	 * @return
	 */
	private JSONObject readExcelToData(String fileId,String processId) {
		// TODO Auto-generated method stub
		JSONObject rs = new JSONObject();
		int code = 1;
		JSONArray datas = new JSONArray();
		// 解析excel 封装对象
		Workbook workbook = null;
		File dFile =null;
		try {
			String impFrc=UUID.randomUUID().toString();
			fileServerImplFastDFS.downloadFile(fileId, impFrc);
			dFile=new File(impFrc);			
			workbook=WorkbookFactory.create(dFile);
			
//			if (impFrc.endsWith("xls")) {
//				workbook = new HSSFWorkbook(new FileInputStream(
//						new File(impFrc)));
//			} else if (impFrc.endsWith("xlsx")) {
//				workbook = new XSSFWorkbook(new FileInputStream(
//						new File(impFrc)));
//			}
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
							temp[cols] = i + "";
						}
						datas.add(temp);
					}
				}
			}
		} catch (FileNotFoundException e) {
			code = -10;
		} catch (IOException e) {
			code = -20;
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			code = -30;
			e.printStackTrace();
		}
		finally
		{
			if(null!=dFile)
			{
				dFile.delete();
			}
		}

		rs.put("code", code);
		rs.put("datas", datas);
		return rs;

	}

	/**
	 * 校验表格数据
	 * 
	 * @param pureDatas
	 * @param mrs
	 * @param isMatch
	 * @param sp
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private JSONObject checkImpData(Map<String,String>stringMap,Map<String,String> nameRepeatMap ,JSONArray wrongMsgRepeat,HashMap commonInfoMap,HashMap<Integer, JSONObject> pureDatas,JSONArray mrs, int isMatch, String processId,ImportTaskParameter sp) throws Exception {
		// excel验证部分
		JSONObject rs = new JSONObject();
		// 导入进度
		JSONObject toFront =new JSONObject();
		JSONObject data = new JSONObject();
		Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
		String progressMapKey="elective."+processId+".progressMap";
		Object progressMapObj=redisOperationDAO.get(progressMapKey);
//		String schoolId=sp.getSchoolId();
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
		if(null!=progressMapObj)
		{
			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
			toFront = progressMap.get(processId);
			data.put("progress", 10);
			data.put("msg", "正在校验excel数据,匹配表头完成,校验数据");
			toFront.put("data", data);
		}
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		String prepDataMapKey="elective."+processId+".prepDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
		if(null!=prepDataMapObj)
		{
			prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
		}
		if(null==progressMapObj||null==prepDataMapObj)
		{
			toFront.put("code", -50);
			data.put("progress", 100);
			data.put("msg", "由于长时间未操作，请重新导入");
			toFront.put("data", data);
			progressMap.put(processId, toFront);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			rs.put("ckRs", false);
			rs.put("errorCode", -50);
			return rs;
		}

		//progNum = 6;
		JSONArray wrongMsg = new JSONArray();
		JSONArray exDatas = new JSONArray();
		JSONObject preData = prepDataMap.get(processId);

		JSONObject courseNameMap = new JSONObject();
		if (preData.containsKey("courseNameMap")) {
			courseNameMap = preData.getJSONObject("courseNameMap");		
		}
		
		String[] impHead = (String[]) preData.get("impHead");		
		progressMap.put(processId, toFront);
		redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		
		HashMap<String,Grade> gradeMap=(HashMap<String, Grade>) commonInfoMap.get("gradeMap");
        HashMap<String,Classroom> classMap = (HashMap<String, Classroom>) commonInfoMap.get("classMap");
        HashMap<String,JSONObject> teacherMap = (HashMap<String, JSONObject>) commonInfoMap.get("teacherMap");

		
		HashMap<Integer, JSONObject> rowcolMap= new HashMap<Integer, JSONObject>();
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
			boolean isCourseNameRepeat=false;
			int rowNum = it.next();
			String nameRepeatCheckedAccountIds =nameRepeatMap.get(rowNum+"_teachers");
			JSONObject pd = pureDatas.get(rowNum);
			JSONObject wmsg = new JSONObject();
			wmsg.put("row", rowNum);
			JSONArray mrows = new JSONArray();
			wmsg.put("mrows", mrows);
			//如果有姓名重复，直接出错，进行下一轮判断
			JSONArray mrowRepeat = mrowRepeatMap.get(rowNum);
			if(mrowRepeat!=null){
				if(mrowRepeat.size()>0){
					wmsg.put("mrows", mrowRepeat);
					wrongMsg.add(wmsg);
					continue;
				}
			}
			// excel重复性验证
			String courseName = pd.containsKey("courseName") ? pd.getString("courseName"):null;
			String maxNum = pd.containsKey("maxNum") ? pd.getString("maxNum"): null;
			String schoolTime = pd.containsKey("schoolTime") ? pd.getString("schoolTime"):null;
			String offerGrade = pd.containsKey("offerGrade") ? pd.getString("offerGrade"):null;
			
			// excel重复性验证
			String 	title=pd.getString("courseNameName");
			if (courseName != null && courseName.trim().length() > 0) {
//				boolean dbck = false;
				courseName=courseName.trim().replaceAll("，", ",");
				if (courseNameMap.containsKey(courseName)) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", courseName);
					wsg.put("err", "excel中重复！");
					isCourseNameRepeat = true;
					mrows.add(wsg);
				} else {
					courseNameMap.put(courseName, courseName);
//					dbck = true;
				}
//				if (dbck) {
//					if (courseMap.containsKey(courseName)) {
//						JSONObject wsg = new JSONObject();
//						wsg.put("title", title);
//						wsg.put("oldValue", pd.get("courseName"));
//						wsg.put("err", "系统已存在！");
//						mrows.add(wsg);
//					}
//				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", courseName);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}
			//excel中验证数据合理性
			title = pd.getString("maxNumName");
			if (maxNum != null && maxNum.trim().length() > 0) {
				if (!StringNumTool.isInteger(maxNum)  ) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", maxNum);
					wsg.put("err", "输入不合法！");
					isWsg=true;
					mrows.add(wsg);
				}else if(Integer.parseInt(maxNum)<=0){
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", maxNum);
					wsg.put("err", "输入不合法！");
					isWsg=true;
					mrows.add(wsg);
				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", maxNum);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}
			
			title = pd.getString("schoolTimeName");
			if (schoolTime != null && schoolTime.trim().length() > 0) {
				if (!StringNumTool.isSchoolTime(schoolTime)) {
					JSONObject wsg = new JSONObject();
					wsg.put("title", title);
					wsg.put("oldValue", schoolTime);
					wsg.put("err", "输入不合法！");
					isWsg=true;
					mrows.add(wsg);
				}
			} else {
				JSONObject wsg = new JSONObject();
				wsg.put("title", title);
				wsg.put("oldValue", schoolTime);
				wsg.put("err", "不能为空！");
				isWsg=true;
				mrows.add(wsg);
			}
			
			// 开设年级
			boolean isOk = true;
			String errText = "不能为空！";
			if (offerGrade != null && offerGrade.trim().length() > 0) {
				String[] ids = offerGrade.split(";");
				JSONObject cobj = new JSONObject();
				JSONObject jobj = new JSONObject();
				for (String s : ids) {
					for (String k : s.split(",")) {
						k = k.trim();
						if (!gradeMap.containsKey(k) && !classMap.containsKey(k)) {
							isOk = false;
							errText = k + "无匹配记录！";
							break;
						} else {
							if (gradeMap.containsKey(k)) {
								jobj.put(gradeMap.get(k).getId() + "", k);
							}
							if ((gradeMap.containsKey(k) || classMap.containsKey(k)) && cobj.containsKey(k)) {
								isOk = false;
								errText = "输入值不合法！";
								break;
							} else if (classMap.containsKey(k) && jobj.containsKey(classMap.get(k).getGradeId() + "")) {
								isOk = false;
								errText = "输入值不合法！";
								break;
							} else {
								cobj.put(k, k);
							}
						}
					}
				}
			} else {
				isOk = false;
				errText = "不能为空！";
			}
			if (!isOk) {
				JSONObject wsg = new JSONObject();
				wsg.put("title", pd.getString("offerGradeName"));
				wsg.put("oldValue", offerGrade);
				wsg.put("err", errText);
				isWsg=true;
				mrows.add(wsg);
			}
			
			String err = "";			
			boolean noRecord = false;
			// 非必输字段
//			if(mrows.size()==0)
//			{				
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

					if (tit.equalsIgnoreCase("classMaxNum")) 
					{
						val = val.replaceAll(" ", "");
						if(StringUtils.isNotEmpty(val))
						{
							if(!StringNumTool.isInteger(val)||Integer.parseInt(val)<=0)
							{
								noRecord=true;
								err= "输入不合法！";
							}
							else if(StringUtils.isNotEmpty(maxNum)&&StringNumTool.isInteger(maxNum))
							{	
								if(Integer.valueOf(val)>Integer.valueOf(maxNum))
								{
									noRecord=true;
									err= "输入不合法！";
								}
							}
						}
					}
					else if (tit.equalsIgnoreCase("adaptSex")) 
					{
						val = val.replaceAll(" ", "");
						if(StringUtils.isNotEmpty(val))
						{
							if(!StringNumTool.isAdaptSex(val))
							{
								noRecord=true;
								err= "输入值不合法！";
							}
						}
					}
					else if (tit.equalsIgnoreCase("teachers")) 
					{
						val = val.replaceAll(" ", "");
						if(StringUtils.isNotEmpty(val))
						{							
							String[] names=val.split(",");
							for(int p=0;p<names.length;p++){
								if(StringUtils.isNumeric(names[p])){
									String trueName = stringMap.get(names[p]);
									names[p]=trueName;
								}
							}
							String[] ids=val.split(",");
							if(StringUtils.isNotBlank( nameRepeatCheckedAccountIds ) ){
							    ids = 	nameRepeatCheckedAccountIds.split(",");
							}
							JSONObject tobj=new JSONObject();
							int p=0;
							for (String s : ids)
							{		
								s=s.trim();
								if(tobj.containsKey(names[p]))
								{
									noRecord = true;
									err = "输入值不合法！";
									break;
								}
								else
								{
									tobj.put(s, s);
									if (!teacherMap.containsKey(names[p])) {
										noRecord = true;
										err = s+"无匹配记录！";
										break;
									} 
									/*else {
										JSONObject sysObj = teacherMap.get(s);
										int count = sysObj.getIntValue("count");
										if (count >1) {
											noRecord = true;
											err = s + "匹配到多条记录！";
											break;
										}
									}*/
								}	
								p++;
							}			
						}
					}					
					else if (tit.equalsIgnoreCase("classroom")) 
					{
						err = "";
					}
					else if (tit.equalsIgnoreCase("courseDesc")) 
					{
						err = "";
					}
					else if (tit.equalsIgnoreCase("weekType")) 
					{
						val = val.replaceAll(" ", "");
						if(StringUtils.isNotEmpty(val))
						{
							if(!StringNumTool.isWeekType(val))
							{
								noRecord=true;
								err= "输入值不合法！";
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
				}
//			}
			
			if (mrows.size() > 0) 
			{
				if(!isCourseNameRepeat&&(isWsg||noRecord))
				{
					courseNameMap.remove(courseName);
				}			
				wrongMsg.add(wmsg);
				nameRepeatMap.remove(rowNum+"_teachers");
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
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());

		}
		preData.put("rowcolMap", rowcolMap);
		preData.put("courseNameMap", courseNameMap);
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
		redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		
		return rs;
	}

	/**
	 * 设置公共参数，班级，年级，教师
	 * @param req
	 * @param electiveId 选课id
	 * @param schoolYear 学年
	 * @throws Exception 
	 */
	private void setAllCommonInfo(HttpServletRequest req,String electiveId,String schoolYear,String selectedSemester) throws Exception
	{
		String processId = req.getSession().getId();
		HashMap<String,HashMap> commonInfoMap=new HashMap<String,HashMap>();
		School school =this.getSchool(req,selectedSemester); 
		long schoolId=school.getId();
		// 进程ID
//		String processId = req.getSession().getId();
		// 获取年级列表        
        List<Grade> gradeList = allCommonService.getGradeList(school,selectedSemester);       	
        HashMap<String,Grade> gradeMap = new HashMap<String,Grade>();
        HashMap<Long,JSONObject> useGradeMap=new HashMap<Long,JSONObject>();
    	for(int i=0; i < gradeList.size(); i++){
    		JSONObject obj=new JSONObject();
    		Grade grade=gradeList.get(i);
    		String gName= AccountStructConstants.T_GradeLevelName.get((grade.getCurrentLevel()));
    		gradeMap.put(gName, grade);
    		obj.put("gradeName", gName);
    		obj.put("useGrade", allCommonService.ConvertNJDM2SYNJ(grade.getCurrentLevel().getValue()+"", schoolYear));
    		useGradeMap.put(grade.getId(), obj);
        }
		// 获取班级列表        
        List<Classroom> classList = allCommonService.getAllClass(school,selectedSemester);       	
        HashMap<String,Classroom> classMap = new HashMap<String,Classroom>();
        HashMap<Long,String> classNameMap=new HashMap<Long,String>();
    	for(int i=0; i < classList.size(); i++){
    		Classroom c=classList.get(i);
            classMap.put(c.getClassName(),c);
            classNameMap.put(c.getId(), c.getClassName());
        }
        // 获取教师列表
        List<Account> accoutList = allCommonService.getAllSchoolEmployees(school, selectedSemester, "");
        HashMap<String,JSONObject> teacherMap = new HashMap<String,JSONObject>();
    	for(int i=0; i < accoutList.size(); i++){
    		String key = accoutList.get(i).getName();
            JSONObject obj=(JSONObject)JSON.toJSON(accoutList.get(i));  		  		
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
    	
		HashMap<String,Object> map=new HashMap<String,Object>();
		map.put("schoolId", schoolId);
		map.put("electiveId", electiveId);
        // 获取课程列表
        List<JSONObject> courseList = electiveService.getAllCourse(map);
        HashMap<String,JSONObject> courseMap = new HashMap<String,JSONObject>();
    	for(int i=0; i < courseList.size(); i++){
    		JSONObject obj=courseList.get(i);
    		String key = obj.getString("courseName");
        	if(courseMap.containsKey(key)){
        		JSONObject obj1 = courseMap.get(key);
        		int count = obj1.getIntValue("count");
        		count ++;
        		obj1.put("count", count);
        				
        	}else{
        		obj.put("count", 1);
        		courseMap.put(key, obj);
        	}
    	}
		commonInfoMap.put("courseMap", courseMap);
		commonInfoMap.put("gradeMap", gradeMap);
		commonInfoMap.put("useGradeMap", useGradeMap);
		commonInfoMap.put("classNameMap", classNameMap);
		commonInfoMap.put("classMap", classMap);
		commonInfoMap.put("teacherMap", teacherMap);
		String accountListKey="elective."+processId+".accountList";
		redisOperationDAO.set(accountListKey, accoutList);
		req.getSession().setAttribute("elective."+schoolId+".commonInfoMap", commonInfoMap);
		
	}
	
	/**
	 * 根据参数生成需插入数据
	 * 
	 * @param sp
	 * @param processId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getInsertEntityByCkrs(HashMap commonInfoMap,ImportTaskParameter sp,String processId) throws Exception {
		// TODO Auto-generated method stub
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		String prepDataMapKey="elective."+processId+".prepDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
		if(null!=prepDataMapObj)
		{
			prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
		}
		HashMap<Integer, JSONObject> rows = (HashMap<Integer, JSONObject>) prepDataMap.get(processId).get("rowcolMap");		
		String schoolId = sp.getSchoolId();
		String schoolYear = sp.getSchoolYear();
		String term = sp.getTerm();
		String electiveId = sp.getElectiveId();
		HashMap<String, Object> map=new HashMap<String, Object>();
		List<JSONObject> courseList = new ArrayList<JSONObject>();
		List<JSONObject> classList = new ArrayList<JSONObject>();
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		List<JSONObject> schoolTimeList = new ArrayList<JSONObject>();
		List<String> delCourseNameList=new ArrayList<String>();//和系统本次选课课程重名的则覆盖	
		String stringMapKey="elective."+processId+".stringMap";
		String nameRepeatMapKey="elective."+processId+".nameRepeatMap";
		Map<String,String> stringMap =   (Map<String, String>) redisOperationDAO.get(stringMapKey);
		Map<String,String> nameRepeatMap =   (Map<String, String>) redisOperationDAO.get(nameRepeatMapKey);
		HashMap<String,JSONObject> courseMap=(HashMap<String, JSONObject>) commonInfoMap.get("courseMap");
		HashMap<String,Grade> gradeMap=(HashMap<String, Grade>)commonInfoMap.get("gradeMap");
        HashMap<String,Classroom> classMap = (HashMap<String, Classroom>) commonInfoMap.get("classMap");
        HashMap<Long,String> classNameMap=(HashMap<Long, String>) commonInfoMap.get("classNameMap");
        HashMap<Long,JSONObject> useGradeMap=(HashMap<Long, JSONObject>) commonInfoMap.get("useGradeMap");
        HashMap<String,JSONObject> teacherMap = (HashMap<String, JSONObject>)commonInfoMap.get("teacherMap");
		for (Iterator<Integer> it = rows.keySet().iterator(); it.hasNext();)
		{
			int key = it.next();
			JSONObject row = rows.get(key);
			String courseName=row.getString("courseName");
			String courseId=UUIDUtil.getUUID();
			if(courseMap.containsKey(courseName))
			{
				delCourseNameList.add(courseName); //改成不删除，保留原来的courseId
				JSONObject cObj=courseMap.get(courseName);
				courseId=cObj.getString("courseId");
			}
			
			row.put("electiveId", electiveId);
			row.put("schoolYear", schoolYear);
			row.put("term", term);
			row.put("schoolId", schoolId);
			row.put("courseId", courseId);
			row.put("maxNum", row.getIntValue("maxNum"));

			if(!row.containsKey("classMaxNum") || StringUtils.isEmpty(row.getString("classMaxNum")))
			{
				row.put("classMaxNum", null);
			}
			else
			{
				row.put("classMaxNum", row.getIntValue("classMaxNum"));
			}

			if(!row.containsKey("adaptSex"))
			{
				row.put("adaptSex", 0);
			}
			else
			{
				row.put("adaptSex", StringNumTool.getSexValue(row.getString("adaptSex")));
			}
			if(!row.containsKey("teachers"))
			{
				row.put("teachers", null);
			}else{
				String teachers = row.getString("teachers");
				String teachersName = "";
				List<String> teacherNameList = Arrays.asList(teachers.split(","));
				for(String teacherName:teacherNameList){
					String trueName = stringMap.get(teacherName);
					if(StringUtils.isNotBlank(trueName)){
						teachersName+=trueName+",";
					}else{
						teachersName+=teacherName+",";
					}
				}
				if(StringUtils.isNotBlank(teachersName)){
					teachersName = teachersName.substring(0,teachersName.length()-1);
					row.put("teachers", teachersName);
				}else{
					row.put("teachers", null);
				}
			}
			if(!row.containsKey("classroom"))
			{
				row.put("classroom", null);
			}		
			row.put("createTime", DateUtil.getDateFormatNow());			

			//课程对应的开课年级、班级
			String offerGrade=row.getString("offerGrade");
			List<Grade> gradeList=new ArrayList<Grade>();
			List<Classroom> cList=new ArrayList<Classroom>();
			JSONArray arr=new JSONArray();
			for(String s:offerGrade.split(";"))
			{
				int isAll=0;
				JSONObject obj=new JSONObject();
				JSONArray carr=new JSONArray();
				long gid=0;
				for(String name:s.split(","))
				{
					if(gradeMap.containsKey(name))
					{
						isAll=1;
						Grade g=gradeMap.get(name);
						gradeList.add(g);
						gid=g.getId();
						for(Long l:g.getClassIds())
						{
							JSONObject j=new JSONObject();
							j.put("classId", l);
							j.put("classText", classNameMap.get(l));
							carr.add(j);
						}
					    break;
					}
					else if(classMap.containsKey(name))
					{
						Classroom c=classMap.get(name);
						cList.add(c);
						JSONObject j=new JSONObject();
						j.put("classId", c.getId());
						j.put("classText", c.getClassName());
						carr.add(j);
						if(gid==0)
						{
							gid=c.getGradeId();
						}
					}
				}

				JSONObject uj=useGradeMap.get(gid);
				obj.put("useGrade", uj.getString("useGrade"));
				obj.put("useGradeText", uj.getString("gradeName"));
				obj.put("classList", carr);
				obj.put("isAll", isAll);
				arr.add(obj);
			}
			row.getString("offerGrade");
			row.put("offerGrade", arr.toString());
			courseList.add(row);
			
			for(Grade g:gradeList)
			{
				String useGrade=allCommonService.ConvertNJDM2SYNJ(g.getCurrentLevel().getValue()+"", schoolYear);
				for(Long id:g.getClassIds())
				{
					JSONObject obj=new JSONObject();
					obj.put("schoolYear", schoolYear);
					obj.put("term", term);
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					obj.put("classId", id);
					obj.put("useGrade", useGrade);
					classList.add(obj);
				}
			}
			for(Classroom c:cList)
			{	
				JSONObject obj=new JSONObject();
				obj.put("schoolYear", schoolYear);
				obj.put("term", term);
				obj.put("electiveId", electiveId);
				obj.put("schoolId", schoolId);
				obj.put("courseId", courseId);
				obj.put("classId", c.getId());
				obj.put("useGrade", useGradeMap.get(c.getGradeId()).getString("useGrade"));
				classList.add(obj);
			}
			//任课教师
			if(null!=row.get("teachers")&&StringUtils.isNotEmpty(row.getString("teachers")))
			{
				String[] teacherNames=row.getString("teachers").split(",");
				//得到库中重复的姓名的accountIds（按顺序）
				String repeatTeacherAccountIds = nameRepeatMap.get(key+"_teachers");
				String[]  repeatTeacherAccountIdStr = null;
				if(StringUtils.isNotBlank(repeatTeacherAccountIds)){
					repeatTeacherAccountIdStr = repeatTeacherAccountIds.split(",");
				}
				int i=0;
				for(String name:teacherNames)
				{
					JSONObject obj=new JSONObject();
					obj.put("schoolYear", schoolYear);
					obj.put("term", term);
					obj.put("electiveId", electiveId);
					obj.put("schoolId", schoolId);
					obj.put("courseId", courseId);
					if(repeatTeacherAccountIdStr!=null){
						if(StringUtils.isNotBlank(repeatTeacherAccountIdStr[i])){
							obj.put("teacherId", repeatTeacherAccountIdStr[i]);
						}
						i++;
					}else{
						obj.put("teacherId", teacherMap.get(name.trim()).getString("id"));
					}
//					if(!teacherList.contains(obj))
//					{
						teacherList.add(obj);
//					}
				}
			}
			
			//开设课程时间    星期一7,8;星期二5,6
			if(StringUtils.isNotEmpty(row.getString("schoolTime")))
			{
				int weekType=0;
				if(null!=row.get("weekType")&&StringUtils.isNotEmpty(row.getString("weekType")))
				{
					weekType=StringNumTool.getWeekTypeValue(row.getString("weekType"));
				}						
				for(String s:row.getString("schoolTime").split(";"))
				{
					int dayOfWeek=StringNumTool.getDayOfWeekValue(s.substring(0,3));
					String days=s.substring(3, s.length());
					for(String str:days.split(","))
					{
						str=str.trim();
						if(StringUtils.isNotEmpty(str))
						{
							JSONObject obj=new JSONObject();
							obj.put("schoolYear", schoolYear);
							obj.put("term", term);
							obj.put("electiveId", electiveId);
							obj.put("schoolId", schoolId);
							obj.put("courseId", courseId);
							obj.put("weekType", weekType);
							obj.put("dayOfWeek", dayOfWeek);
							obj.put("lessonOfDay", Integer.parseInt(str)-1);
							if(!schoolTimeList.contains(obj))
							{
								schoolTimeList.add(obj);
							}
						}						
					}
				}
			}			
		}		
	    Collections.reverse(courseList);
		map.put("courseList", courseList);
		map.put("classList", classList);
		map.put("teacherList", teacherList);
		map.put("schoolTimeList", schoolTimeList);
		if(!delCourseNameList.isEmpty())
		{
			HashMap<String,Object> delMap=new HashMap<String,Object>();
			delMap.put("electiveId", electiveId);
			delMap.put("schoolId", schoolId);
			delMap.put("courseNameList", delCourseNameList);
            List<String> courseIds= electiveService.getCourseIdsByName(delMap);
            if(null!=courseIds && courseIds.size()>0)
            {
//            	StringBuffer sb=new StringBuffer();
//            	for(String s:courseIds)
//            	{
//            		sb.append("'").append(s).append("'").append(",");
//            	}
//            	delMap.put("courseId", sb.substring(0,sb.length()-1));
            	map.put("delMap", delMap);
            	map.put("courseIds", courseIds);
            }
		}
		map.put("schoolId", schoolId);
		map.put("electiveId", electiveId);
		map.put("termInfo", schoolYear+term);
		return map;
	}

}