package com.talkweb.timetable.action;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.auth.service.AuthService;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.FileImportInfoService;
import com.talkweb.filemanager.service.FileServer;
import com.talkweb.timetable.domain.ImportTaskParameter;
import com.talkweb.timetable.service.TimetableService;

@Controller
@RequestMapping("/timetableManage/import")
public class TimetableTeacherImportAction extends BaseAction {
	
    @Value("#{settings['tempFilePath']}")
    private String tempFilePath;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AllCommonDataService commonService;
    
    @Autowired
    private TimetableService timetableService;
    
    @Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
    
	@Autowired
	private FileServer fileServerImplFastDFS;
	
	@Autowired
	private FileImportInfoService fileImportInfoService;
	
	 private static final Logger logger = LoggerFactory.getLogger( TimetableTeacherImportAction.class);
    
    /**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     */
    @RequestMapping(value = "/importTeacher")
    @ResponseBody
	public JSONObject uploadExcel(@RequestParam("importFile") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {		
		String msg = "";
		int code = 0;
		File df = null;
		String fileId = "";		
		String s = UUID.randomUUID().toString();
		String schoolId = getXxdm(req);
		String keyId = "timetableTeacher_" + s;
		try {
			// 初始化基础数据及参数
			String[] courseTitleName = setParamAndRetHead(req); 		
			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			fileName = fileName.substring(0,fileName.lastIndexOf(".") + 1) + prefix;
			
			df = new File(s + "." + prefix);
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
				String[] tempTeacherImpExcTitle = new String[cellNum];
				code = 1;
				msg = "文件格式正确,字段无需匹配!";
				// 判断是否需要进行手工字段匹配
				for (int i = 0; i < cellNum; i++) {
					String excelVal = row.getCell(i).getStringCellValue();
					excelVal = replaceChar(excelVal);
					if (StringUtils.isNotEmpty(excelVal)
							&&inArrayNum(excelVal, courseTitleName)==0) {
						code = 2;
						msg = "文件格式正确,字段需要匹配!";
					}
					// 放置临时保存目录
					tempTeacherImpExcTitle[i] = excelVal;
				}
				if(code>=0)
				{
					fileId = fileServerImplFastDFS.uploadFile(df,fileName);
					logger.error("fileServerImplFastDFS.uploadFile ==>" + fileId);
					fileImportInfoService.addFile(schoolId, keyId, fileId);
					req.getSession().setAttribute("keyId", keyId);
					req.getSession().setAttribute("fileId", fileId);
					req.getSession().setAttribute("timetableTeacher."+this.getXxdm(req)+".excelTitle", tempTeacherImpExcTitle);
				}
			} else {
				code = -2102;
				msg = OutputMessage.getDescByCode(code + "");				
			}
			workbook.close();
		} catch (Exception e) {
			code = -2101;
			msg = "文件格式错误或未知错误！";
			if(null!=fileId && StringUtils.isNotEmpty(fileId))
			{
				try {
					fileServerImplFastDFS.deleteFile(fileId);
				}catch (Exception ex) {
					code = -1005;
					msg = msg +  OutputMessage.getDescByCode(code + "");
				}
				fileImportInfoService.deleteFile(schoolId, keyId);
			}
		}finally{
			if(null!=df)
			{
				df.delete();
			}
		}
		JSONObject object = new JSONObject();
		// 封装返回结果
		object.put("msg", msg);
		object.put("code", code);
		return object;
	}
    
    private String[] setParamAndRetHead(HttpServletRequest req) throws Exception{
        // 学年学期
        String termInfo = req.getParameter("selectedSemester");
        // 学校代码
     	School school = this.getSchool(req,termInfo);
        req.getSession().setAttribute("termInfo", termInfo);		
		// 获取系统表头 		
		List<Map<String, Object>> kms = commonService.getAdminKM(school,termInfo);
		// 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-s
		String[] courseTitleName = new String[2+kms.size()];
		int[] courseTitleNeed = new int[2+kms.size()];
		String[] courseTitleId = new String[2+kms.size()];
		courseTitleName[0] = "班级名称";
		courseTitleNeed[0] = 1;
		courseTitleId[0] = "bjmc";
		courseTitleName[1] = "\u73ed\u4e3b\u4efb";
		courseTitleNeed[1] = 0;
		courseTitleId[1] = "bzr";
		for(int i=0;i<kms.size();i++){
            courseTitleName[i+2] = ((String) kms.get(i).get("zwmc"));
            courseTitleId[i+2] = kms.get(i).get("kmdm").toString();
            courseTitleNeed[i+2] = 0;
        }
		// 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-e
		// 获取班级列表        
        List<Classroom> classList = commonService.getAllClass(school,termInfo);       	
        String[] className = new String[classList.size()];
        HashMap<String,String> classMap = new HashMap<String,String>();
    	for(int i=0; i < classList.size(); i++){
            className[i] = classList.get(i).getClassName();
            String classId = classList.get(i).getId() + "";
            classMap.put(className[i], classId);
        }
        // 获取教师列表
        List<Account> accoutList = commonService.getAllSchoolEmployees(school, termInfo ,null);
        String[] teacherName = new String[accoutList.size()]; 
        HashMap<String,String> teacherNameMap = new HashMap<String,String>();
    	for(int i=0; i < accoutList.size(); i++){
    		Account account = accoutList.get(i);
            teacherName[i] = account.getName();
            String teacherId = account.getId()+"";
            teacherNameMap.put(teacherName[i], teacherId);
    	}	
        // 公共基础数据存储
        String processId = req.getSession().getId();
        String commonMapKey="timetableTeacher."+processId+".commonDataMap";
	 	JSONObject commonObj = new JSONObject();
    	commonObj.put("courseTitleName", courseTitleName);
    	commonObj.put("courseTitleNeed", courseTitleNeed);
    	commonObj.put("courseTitleId", courseTitleId);
    	commonObj.put("className", className);
    	commonObj.put("classMap", classMap);
    	commonObj.put("teacherName", teacherName);
    	commonObj.put("teacherNameMap", teacherNameMap);
    	redisOperationDAO.set(commonMapKey, commonObj, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
    	return courseTitleName;
    }

	/**
	 * 判断字符串是否在字符串数组内
	 * 
	 * @param string
	 * @param arr
	 * @return
	 */
	private int inArrayNum(String source, String[] arr) {
		int number = 0;
		for(int i = 0; i < arr.length; i++) {
			String target = (arr[i] == null?"":arr[i]);
			if(target.equalsIgnoreCase(source))number ++;
		}
		return number;
	}
	
    /**
     * 字符串在数组中的索引
     * 
     * @param string
     * @param stutitle2
     * @return
     */
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
	
    /**
     * 获取Excel表头和系统字段
     * 
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/getExcelMatch")
    @ResponseBody
    public JSONObject getExcelHead(HttpServletRequest req){
		String commonMapKey = "timetableTeacher."+ req.getSession().getId() +".commonDataMap";		
		JSONObject excelHead = new JSONObject();
		JSONObject rs = new JSONObject();
		JSONObject moduleField = new JSONObject();
		try{
			Object commonMapObj = redisOperationDAO.get(commonMapKey);
			if (null != commonMapObj){
				JSONObject commonObj = (JSONObject)commonMapObj;
				
				String[] courseTitleName = (String[])commonObj.get("courseTitleName");
				int[] courseTitleNeed = (int[])commonObj.get("courseTitleNeed");
					        
		        // 获取session中保存的临时表头
		        String[] tmpTit =(String[]) req.getSession().getAttribute("timetableTeacher."+this.getXxdm(req)+".excelTitle");
		        if (tmpTit != null) {
		            // 开始拼装返回数据结构
		            excelHead.put("total", tmpTit.length);
		            JSONArray rows = new JSONArray();
		            excelHead.put("rows", rows);
		            for (int i = 0; i < tmpTit.length; i++) {
		                JSONObject obj = new JSONObject();
		                obj.put("field", replaceChar(tmpTit[i]));
		                rows.add(obj);
		            }
		        } else {
		            excelHead.put("total", 0);
		        }
		        
		        // 直接使用系统表头 系统表头是否必填数组开始拼装返回数据结构
		        moduleField.put("total", courseTitleName.length);
		        JSONArray sysrows = new JSONArray();
		        moduleField.put("rows", sysrows);
		        for (int i = 0; i < courseTitleName.length; i++) {
		            JSONObject obj = new JSONObject();
		            obj.put("field", courseTitleName[i]);
		            obj.put("sysfield", courseTitleNeed[i]);
		            sysrows.add(obj);
		        }
		        rs.put("excelHead", excelHead);
		        rs.put("moduleField", moduleField);
			}
		} catch (Exception e) {
			System.out.println("Message: " + e.getMessage());
		}
        return rs;
    }

    /**
     * 导入任务启动接口
     * 
     * @param req
     * @param res
     * @return
     */
    @RequestMapping(value = "/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody Map<String,Object> mappost, HttpServletResponse res) {      
    	String isMatch = mappost.get("isMatch").toString();
        String timetableId = mappost.get("timetableId").toString();
        ImportTaskParameter stt = new ImportTaskParameter();
        String processId = req.getSession().getId();
    	String fileId = (String)req.getSession().getAttribute("fileId");
    	req.getSession().removeAttribute("fileId");
        String termInfo = (String)req.getSession().getAttribute("termInfo");
        JSONObject obj = new JSONObject();
        int code = 1;
        String msg = "正在准备导入任务!!!";
        if ("0".equals(isMatch)){
            stt.setIsMatch(Integer.parseInt(isMatch));
            stt.setTimetableId(timetableId);
        }else{
            stt.setIsMatch(Integer.parseInt(isMatch));
        	stt.setMatchResult(JSON.parseArray(mappost.get("matchResult").toString()));
            stt.setTimetableId(timetableId);
        }   
    	String xxdm = getXxdm(req);
    	stt.setXxdm(xxdm);
        stt.setSchool(this.getSchool(req,termInfo));
        stt.setTermInfo(termInfo);//req.getSession().getAttribute("termInfo").toString()
		stt.setProcessId(processId);
		String keyId = req.getSession().getAttribute("keyId").toString();
		req.getSession().removeAttribute("keyId");
		stt.setKeyId(keyId);             
                  
	 	JSONObject procObj = new JSONObject();
	 	procObj.put("taskParam", stt);
	 	Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();		
	 	JSONObject data = new JSONObject();
		data.put("progress", 0);
		data.put("msg", msg);
		procObj.put("data", data);
		procObj.put("code", code);
		progressMap.put(processId, procObj);
	 	String progressMapKey="timetableTeacher."+processId+".progressMap";	
	 	try{
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			SubProcess sp = new SubProcess(processId , fileId);
	        sp.start();
	        msg = "正常启动导入任务!!!";
	 	} catch (Exception e) {
	 		code = -2;
			msg = "正常启动，redis保存变量出错!!!";
		}
	 	obj.put("code", code);
        obj.put("msg", msg);  
        return obj;
    }
    
    /**
     * 获取导入进度
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/importProgress")
    @ResponseBody
    public JSONObject importProgress(HttpServletRequest req, HttpServletResponse res) {
    	JSONObject rs = new JSONObject();
        try{
        	Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
    		String progressMapKey="timetableTeacher."+req.getSession().getId()+".progressMap";
    		Object progressMapObj = redisOperationDAO.get(progressMapKey);
    		if (null!=progressMapObj)
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
    		if (progressMap!=null && obj!=null){
    			if (obj.getIntValue("code")==-50)
    			{
    				String schoolId = this.getXxdm(req);
    				String keyId=req.getSession().getAttribute("keyId").toString();
    				String fileId = fileImportInfoService.getFileBy(schoolId, keyId);
    				logger.error("fileImportInfoService.getFileBy fileId==>" + fileId);
    				if (null!=fileId && StringUtils.isNotEmpty(fileId))
    				{
    					fileServerImplFastDFS.deleteFile(fileId);
    				}
    				if (null!=keyId && StringUtils.isNotEmpty(keyId))
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
        }catch(Exception e){
        	rs.put("code",-100);
			rs.put("data", "获取进度信息失败!!!");
        }
		return rs;	
    } 
    
	/**
	 * 单条数据修改
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
		int row = post.getIntValue("row");
		rs.put("rowNum", row);
		JSONArray mrows = post.getJSONArray("mrows");
		int code = post.getIntValue("code");
		
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		JSONObject commonObj = new JSONObject();
		String prepDataMapKey="timetableTeacher."+processId+".prepDataMap";
		String commonMapKey="timetableTeacher."+processId+".commonDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
		Object commonMapObj=redisOperationDAO.get(commonMapKey);
		if(null != prepDataMapObj && null != commonMapObj)
		{
			prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			commonObj = (JSONObject)commonMapObj;
		}
		Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
		String progressMapKey = "timetableTeacher."+processId+".progressMap";
		Object progressMapObj = redisOperationDAO.get(progressMapKey);
		if(null!=progressMapObj)
		{
			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
		}				
		if(null==progressMapObj||null==prepDataMapObj||null ==commonObj)
		{
			JSONObject data = new JSONObject();
			data.put("msg", "由于长时间未操作，请重新导入!!!");
			data.put("progress", 100);
			rs.put("code", 1);	
			rs.put("data", data);
			progressMap.put(processId, rs);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			return rs;
		}
		List<String[]> datas = (List<String[]>) prepDataMap.get(processId).get("datas");
		String[] headTitleName = (String[]) prepDataMap.get(processId).get("headTitleName");
		String[][] heads = (String[][]) prepDataMap.get(processId).get("heads");
		ImportTaskParameter sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
		
		
		List<String[]> pureDatas = new ArrayList<String[]>();
		String[] sd = datas.get(row-1);
		if (mrows!=null) {
			for (int i = 0; i < mrows.size(); i++) {
				JSONObject o = mrows.getJSONObject(i);
				String title = replaceChar(o.getString("title"));
				int index = strIndexInArray(title,headTitleName);
				sd[index] = replaceChar(o.getString("value"));
			}
		}
		pureDatas.add(datas.get(0));
		pureDatas.add(sd);
		
		JSONObject cr = checkImpData(pureDatas, heads, sp, commonObj , code );
		
		if (cr.getBooleanValue("ckRs")) {
			rs.put("code", 1);
			JSONObject data = new JSONObject();
			data.put("msg", "校验通过！");
			rs.put("data", data);
		} else {
			if (code == -1) {
				datas.set(row-1, null);// class is not match,should delete.
				rs.put("code", 1);
			}else{
				rs.put("code", -1);
				rs.put("mrows",
						cr.getJSONObject("validateMsg").getJSONArray("rows")
								.getJSONObject(0).getJSONArray("mrows"));
			}
			
		}

		redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
		return rs;
	}
    
    /**
	 * 继续导入任务启动接口
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/continueImport")
	@ResponseBody
	public synchronized JSONObject continueImport(HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject object = new JSONObject();
		int code = 1;
		String msg = "正常启动,保存数据操作成功！";		
		String processId = req.getSession().getId();
		List<JSONObject> targetList = null;
		ImportTaskParameter sp = null;	
		// 学校代码
		String termInfo = (String)req.getSession().getAttribute("termInfo");
		try{	
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			JSONObject commonObj = new JSONObject();
			String prepDataMapKey="timetableTeacher."+processId+".prepDataMap";
			String commonMapKey="timetableTeacher."+processId+".commonDataMap";
			Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
			Object commonMapObj=redisOperationDAO.get(commonMapKey);
			if(null != prepDataMapObj && null != commonMapObj)
			{
				prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
				commonObj = (JSONObject)commonMapObj;
			}
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
			String progressMapKey="timetableTeacher."+processId+".progressMap";
			Object progressMapObj=redisOperationDAO.get(progressMapKey);
			if(null!=progressMapObj)
			{
				progressMap=(Hashtable<String, JSONObject>) progressMapObj;
			}
			if(null==progressMapObj||null==prepDataMapObj||null==commonObj)
			{
				JSONObject toFront = new JSONObject();
				JSONObject data = new JSONObject();
				data.put("progress", 100);
				data.put("msg", "由于长时间未操作，请重新导入");
				toFront.put("code", -50);
				toFront.put("data", data);
				progressMap.put(processId, toFront);
				redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				return toFront;
			}
			List<String[]> datas = (List<String[]>) prepDataMap.get(processId).get("datas");
			String[][] heads = (String[][]) prepDataMap.get(processId).get("heads");
			sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");
			// 处理表格数据
			JSONObject toFront = progressMap.get(processId);
			JSONObject data = toFront.getJSONObject("data");
			toFront.put("code", 1);
			data.put("msg", "正在转换excel数据,请稍后...");
			data.put("progress", 35);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			JSONObject result = changeData(datas, heads, sp, commonObj);
			targetList = (List<JSONObject>)result.get("rowDatas");					
			// 开始入库
			toFront.put("code", 1);
			data.put("msg", "正在保存excel数据,请稍后...");
			data.put("progress", 50);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			if (CollectionUtils.isNotEmpty(targetList)) {
			    timetableService.addImportTeachers(getSchool(req,termInfo),targetList,sp);																			
				toFront.put("code", 2);
                data.put("progress", 100);
                data.put("msg", "导入成功，共计导入"+ (datas.size()-1) +"条信息记录！");
                msg = "导入成功，共计导入"+ (datas.size()-1) +"条信息记录！"; 
                //---批量任课教师信息回写,Rsp为空报异常
				try{
					/** ---批量任课教师信息回写--- **/
						timetableService.writebackTeacherList(targetList,
								sp.getSchool(), sp.getTermInfo());	
				}catch (Exception e) {
					System.out.println("打印异常信息:" + e.getMessage());
				}
			}
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			code = -1;
			msg = "保存数据失败,请坚持excel或联系管理员！";
		}
		object.put("code", code);
		object.put("msg", msg);
		return object;
	}

	class SubProcess extends Thread {
		
		private String processId;
		private String fileId;
		
		public  SubProcess(String processId , String fileId){
			this.processId = processId;
			this.fileId = fileId;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// excel导入处理开始
			String progressMapKey="timetableTeacher."+processId+".progressMap";
			String commonMapKey="timetableTeacher."+processId+".commonDataMap";
			String suffix = "处理成功,请稍后...";
			String fileIdTmp = "";
			String schoolId = "";
			String keyId = "";
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
			JSONObject commonObj = new JSONObject();
			List<JSONObject> targetList = null;
			ImportTaskParameter sp = null;
			try {
				 Object progressMapObj=redisOperationDAO.get(progressMapKey);
				 Object commonMapObj=redisOperationDAO.get(commonMapKey);
				 if(null != progressMapObj && null != commonMapObj)
				 {
					progressMap = (Hashtable<String, JSONObject>) progressMapObj;
					commonObj = (JSONObject)commonMapObj;
				 }
				 else
				 {
					JSONObject data = new JSONObject();
					JSONObject toFront= new JSONObject();
					toFront.put("code", -50);
					data.put("progress", 100);
					data.put("msg", "由于长时间未操作，请重新导入");
					toFront.put("data", data);
					progressMap.put(processId, toFront);
					redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					return;
				 }
				 // excel导入处理开始
				 sp = (ImportTaskParameter) progressMap.get(processId).get("taskParam");				 
				 fileIdTmp = fileImportInfoService.getFileBy(sp.getXxdm(), sp.getKeyId());	
				 logger.info("fileIdTmp==>" + fileIdTmp );
				 if (fileIdTmp !=null) {
					 fileId = fileIdTmp;
				 }
				 logger.error("sp.getXxdm()==>" + sp.getXxdm());
				 logger.error("sp.getKeyId()==>" + sp.getKeyId());
				 logger.error("fileImportInfoService.getFileBy fileId2==>" + fileId);
				 logger.error("processId==>" + processId);
				 JSONObject toFront = progressMap.get(processId);
				 JSONObject data = toFront.getJSONObject("data");				 
				 // 开始读取数据
				 toFront.put("code", 1);    
				 data.put("progress", 5);
				 data.put("msg", "读取excel表格数据" + suffix);
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 JSONObject readRs = readExcelToData(fileId,processId);
				 List<String[]> datas = (List<String[]>)readRs.get("datas");
				 // 开始封装表头
				 toFront.put("code", 1);    
				 data.put("progress", 15);
				 data.put("msg", "封装excel表头数据" + suffix);
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 String[][] heads = handleHeadData(datas, sp, commonObj);
				 // 开始校验数据
				 toFront.put("code", 1);    
				 data.put("progress", 25);
				 data.put("msg", "校验excel数据" + suffix);
				 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				 JSONObject ckrs = checkImpData(datas, heads, sp, commonObj , 1);
				 if (ckrs.getBooleanValue("ckRs")) {
					 // 处理表格数据
					 toFront.put("code", 1);    
					 data.put("progress", 35);
					 data.put("msg", "转换excel数据" + suffix);
					 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					 JSONObject result = changeData(datas, heads, sp, commonObj);
					 targetList = (List<JSONObject>)result.get("rowDatas");					
					 // 开始入库
					 toFront.put("code", 1);
					 data.put("progress", 50);
					 data.put("msg", "保存excel数据" + suffix);
					 redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());					 
					 if (CollectionUtils.isNotEmpty(targetList)) {
					     timetableService.addImportTeachers(sp.getSchool(),targetList,sp);															
						 toFront.put("code", 2);
	                     data.put("progress", 100);
	                     data.put("msg", "导入成功，共计导入"+ (datas.size()-1) +"条信息记录！");
	                     //---批量任课教师信息回写,Rsp为空报异常
	 					 try{
	 						 /** ---批量任课教师信息回写--- **/
	 						 timetableService.writebackTeacherList(targetList,
	 									sp.getSchool(), sp.getTermInfo());	
	 					 }catch (Exception e) {
	 						 System.out.println("打印异常信息:" + e.getMessage());
	 					 }
					 }
					
				 }else {
					 toFront.put("code", -2);
					 data.put("progress", 100);
					 data.put("msg", "Excel数据校验不通过!");
					 data.put("total", datas.size()-1);
					 data.put("validateMsg", ckrs.get("validateMsg"));
				 }	
			 }catch(Exception e){
				 JSONObject toFront= new JSONObject();
				 JSONObject data = new JSONObject();  
				 toFront.put("code", -1);
				 data.put("progress", 100);
				 data.put("msg", "处理失败，请检查Excel或联系管理员!");
				 toFront.put("data", data);
				 progressMap.put(processId, toFront);
			 }finally{
				 // excel导入处理结束
				 try {
					  redisOperationDAO.set(progressMapKey, progressMap,CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					  //在删除表中的记录
					  if (null != fileId && StringUtils.isNotEmpty(fileId)) {
						  fileServerImplFastDFS.deleteFile(fileId);
					  }
					  if (null != keyId && StringUtils.isNotEmpty(keyId)) {
						  fileImportInfoService.deleteFile(schoolId, keyId);
					  }
				  } catch (Exception e) {
					  e.printStackTrace();
				  }
			}	
	    }	
	}
	
	/**
	 * 将excel读取为数据
	 * 
	 * @param impFrc
	 * @return
	 */
	private JSONObject readExcelToData(String fileId,String processId)throws Exception{
		JSONObject rs = new JSONObject();
		List<String[]> datas = new ArrayList<String[]>();
		String[] headTitleName = null;
		// 解析excel 封装对象
		Workbook workbook = null;
	
        File file = null;		
		String uuid=UUID.randomUUID().toString();
		fileServerImplFastDFS.downloadFile(fileId, uuid);
		file=new File(uuid);			
		workbook=WorkbookFactory.create(file);
//		if (impFrc.endsWith("xls")) {
//			workbook = new HSSFWorkbook(new FileInputStream(
//					new File(impFrc)));
//		} else if (impFrc.endsWith("xlsx")) {
//			workbook = new XSSFWorkbook(new FileInputStream(
//					new File(impFrc)));
//		}
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
							temp[j] = replaceChar(df.format(cell.getNumericCellValue()));
							break;
						case HSSFCell.CELL_TYPE_STRING:
							temp[j] = replaceChar(cell.getRichStringCellValue().getString());
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							try {
								temp[j] = replaceChar(String.valueOf(cell.getStringCellValue()));
							} catch (IllegalStateException e) {
								temp[j] = replaceChar(String.valueOf(cell.getNumericCellValue()));
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
		String prepDataMapKey="timetableTeacher."+processId+".prepDataMap";
		redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
		return rs;
	}
	
	/** 过滤中文/半角|全角/特殊字符 */
	private String replaceChar(String objective){
		objective = objective.replace("\u0020", "");
		objective = objective.replace("\u3000", "");
		objective = objective.replace("\u00A0", "");
		objective = objective.replace("，", ",");
		objective = objective.replace("\\u000A", "");
		objective = objective.replace("\\u000D", "");
		objective = objective.replace("\u0008", "");
//		objective = objective.replace("（", "(");
//		objective = objective.replace("）", ")");
//		objective = objective.replace("【", "[");
//		objective = objective.replace("】", "]");
//		objective = objective.replace("｛", "{");
//		objective = objective.replace("｝", "}");
//		objective = objective.replace("《", "<");
//		objective = objective.replace("》", ">");
		return objective;
	}
	

	/**
	 * 校验表格数据
	 * 
	 * @param datas
	 * @param array
	 * @param commonObj 
	 * @param stt
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONObject changeData(List<String[]> datas,String[][] array,ImportTaskParameter sp, JSONObject commonObj) {
		List<JSONObject> rowDatas = new ArrayList<JSONObject>();
		// 班级/老师信息
		HashMap<String,String> classMap = (HashMap<String,String>)commonObj.get("classMap");
		HashMap<String,String> teacherNameMap = (HashMap<String,String>)commonObj.get("teacherNameMap");
		for (int i = 1; i < datas.size(); i++) {
			if(null == datas.get(i)) continue;			
			String[] cell = (String[]) datas.get(i); //--["10003","",""]
			String xxbjmc = cell[0];
			String bzr = null;
			for (int j = 1; j < array[0].length; j++) {
				if(StringUtils.isEmpty(cell[j]))continue;
				  if ("\u73ed\u4e3b\u4efb".equals(array[2][j])) {
				     bzr = teacherNameMap.get(cell[j].trim());
				     break ;
				  }
			}
			boolean  bzrOnly = true;
			for (int j = 1; j < array[0].length; j++) {
				int isIn = Integer.parseInt(array[1][j]);
				if (isIn != -1 && StringUtils.isNotEmpty(cell[j])) {
					
					// 导入的班级Id列表
					String[] values = cell[j].split(",");
					
				    for(String value : values) {
				     
				    	if(StringUtils.isEmpty(value))continue;
				    	if ("\u73ed\u4e3b\u4efb".equals(array[2][j])) {
				    		continue;
						}else{
							String v = teacherNameMap.get(value);     
					        // 放入数据 
							JSONObject d = new JSONObject();
							String classId = "";
							String upstr = xxbjmc.toUpperCase();
							String lowstr = xxbjmc.toLowerCase();
							if (classMap.containsKey(upstr)){
								classId = classMap.get(upstr);
							}else if(classMap.containsKey(lowstr)){
								classId = classMap.get(lowstr);
							}
							if(StringUtils.isEmpty(classId))continue;
							d.put("classId", classId);
							d.put("courseId", array[3][j]);
							d.put("teacherId", v);
							d.put("timetableId", sp.getTimetableId());
							d.put("schoolId", sp.getXxdm());
							d.put("bzr", bzr);
							rowDatas.add(d);  
							bzrOnly = false;
						}
				    }
				}
			}
			
			
		    if (bzrOnly) { // 只有班主任导入
		    	JSONObject d = new JSONObject();
		    	String classId = "";
				String upstr = xxbjmc.toUpperCase();
				String lowstr = xxbjmc.toLowerCase();
				if (classMap.containsKey(upstr)){
					classId = classMap.get(upstr);
				}else if(classMap.containsKey(lowstr)){
					classId = classMap.get(lowstr);
				}
				if(StringUtils.isEmpty(classId))continue;
				d.put("classId", classId);
				d.put("timetableId", sp.getTimetableId());
				d.put("schoolId", sp.getXxdm());
				d.put("bzr", bzr);
				rowDatas.add(d);
			}
			
			
    
		}
	    // 封装返回数据
		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		return rs;
	}

	/**
	 * @param datas
	 * @param commonObj 
	 * @param mrs
	 * @param isMatch
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private String[][] handleHeadData(List<String[]> datas,ImportTaskParameter sp, JSONObject commonObj) throws Exception {
		int isMatch = sp.getIsMatch();
		String[] titles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] array = new String[4][titles.length];
		// 科目信息
		int[] courseTitleNeed = (int[])commonObj.get("courseTitleNeed");
		String[] courseTitleName = (String[])commonObj.get("courseTitleName");
		String[] courseTitleId = (String[])commonObj.get("courseTitleId");
		// 无需手工匹配
		if (isMatch == 0) {
			// 封装表头
			for (int i = 0; i < titles.length; i++) {
				array[0][i] = titles[i];
				int needIndex = strIndexInArray(titles[i], courseTitleName);
				if (needIndex >= 0) {
					// 在系统字段中能找到
					array[1][i] = courseTitleNeed[needIndex] + "";
					array[2][i] = courseTitleName[needIndex];
					array[3][i] = courseTitleId[needIndex];
				} else {
					// 在系统字段中找不到 标记为不录入
					array[1][i] = "-1";
					array[2][i] = "none";
				}
			}
		} else {
			// 需要手工匹配的 根据匹配关系封装表头
			// 封装表头
			JSONArray mrs = sp.getMatchResult();
			for (int i = 0; i < titles.length; i++) {
				String sysTit = "none";
				for (int j = 0; j < mrs.size(); j++) {
					JSONObject obj = mrs.getJSONObject(j);
					if (titles[i]!=null&&titles[i].equalsIgnoreCase(obj.getString("excelField"))) {
						sysTit = obj.getString("sysField");
						continue;
					}
				}
				array[0][i] = titles[i];
				int needIndex = strIndexInArray(sysTit, courseTitleName);
				if (needIndex >= 0) {
					// 在系统字段中能找到
					array[1][i] = courseTitleNeed[needIndex] + "";
					array[2][i] = courseTitleName[needIndex];
					array[3][i] = courseTitleId[needIndex];
				} else {
					// 在系统字段中找不到 标记为不录入
					array[1][i] = "-1";
					array[2][i] = "none";
					array[0][i] = null;
				}
			}
		}
		String prepDataMapKey = "timetableTeacher."+sp.getProcessId()+".prepDataMap";
		Object prepDataMapObj = redisOperationDAO.get(prepDataMapKey);
		if(null!=prepDataMapObj)
		{
			Hashtable<String, JSONObject> prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			JSONObject rs = prepDataMap.get(sp.getProcessId());
			rs.put("heads", array);
			redisOperationDAO.set(prepDataMapKey, prepDataMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
		}
		return array;
	}

	 /**
     * 检查导入数据
	 * @param commonObj 
     * 
     * @return
     */
	private JSONObject checkImpData(List<String[]> datas,String[][] head,
			ImportTaskParameter stt, JSONObject commonObj , int ignore) {
		JSONObject result = new JSONObject();
		JSONObject validateMsg = new JSONObject();
		JSONArray rows = new JSONArray();
		// 班级/老师信息
		String[] className = (String[])commonObj.get("className");
		String[] teacherName = (String[])commonObj.get("teacherName");
		for(int i = 1; i < datas.size(); i++){
			JSONArray mrows = new JSONArray();
			JSONObject row = new JSONObject();
			row.put("row", i + 1);
			String[] title = datas.get(0);
			String[] array = datas.get(i);
			for(int j = 0; j < array.length; j++){			
				int isIn = Integer.parseInt(head[1][j]);
				if (isIn != -1){
					String fields = array[j];
					String[] fieldList = new String[0];
					fieldList = fields.split(",");
					// 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-s
					 if ("\u73ed\u4e3b\u4efb".equals(head[2][j])  && fieldList.length > 1 ) {
						   if(ignore < 0){
							   array[j] = null;
						   }else{
							   JSONObject wsg = new JSONObject();
								wsg.put("title", title[j]);
								wsg.put("oldValue", fields);
								wsg.put("err", "\u4e00\u4e2a\u73ed\u7ea7\u4ec5\u53ef\u5bfc\u5165\u4e00\u4e2a\u73ed\u4e3b\u4efb!");
								mrows.add(wsg); 
						   }
					 }else{
					 // 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-e
						 for(String field : fieldList){
								if (j > 0){
									if (StringUtils.isNotEmpty(field)){
										if (inArrayNum(field,teacherName)==0){
											   if(ignore < 0){
												   array[j] = null;
											   }else{
													JSONObject wsg = new JSONObject();
													wsg.put("title", title[j]);
													wsg.put("oldValue", fields);
													wsg.put("err", "教师不匹配!");
													mrows.add(wsg);break;
											  }
										}else if(inArrayNum(field,teacherName) > 1)	{
											if (ignore < 0) {
												 array[j] = null;
											}else{
												JSONObject wsg = new JSONObject();
												wsg.put("title", title[j]);
												wsg.put("oldValue", fields);
												wsg.put("err", "多个教师重名!!!");
												mrows.add(wsg);break;
											}
											
										}					
									}
								}else{
									if (StringUtils.isEmpty(field) || inArrayNum(field,className)==0){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[0]);
										wsg.put("oldValue", fields);
										wsg.put("err", "班级名称不匹配!");
										mrows.add(wsg);break;
									}else if(inArrayNum(field,className) > 1){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[0]);
										wsg.put("oldValue", fields);
										wsg.put("err", "多个班级名称重名!!!");
										mrows.add(wsg);break;
									}
								}		
							}
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
	
}