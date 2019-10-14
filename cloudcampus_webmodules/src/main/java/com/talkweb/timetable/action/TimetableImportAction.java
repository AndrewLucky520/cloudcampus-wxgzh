package com.talkweb.timetable.action;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
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
@RequestMapping("/timetableManage/importTimetable")
public class TimetableImportAction extends BaseAction {
	
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
    
    /**
     * 常量参数-星期节次
     */
    private static String[] weekDays = new String[71];
    
    /**
     * 常量参数-星期
     */
    private static String[] weeks = { "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日" };
    /**
     * 常量参数-节次
     */
    private static String[] sections = { "第一节", "第二节", "第三节", "第四节", "第五节", "第六节", "第七节",
			"第八节", "第九节", "第十节" };
    
    /**
     * 常量参数-星期节次MAP
     */
    private static HashMap<String,JSONObject> weekDayMap = new HashMap<String,JSONObject>();
    
    /**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     */
    @RequestMapping(value = "/importTimetable")
    @ResponseBody
	public JSONObject uploadExcel(@RequestParam("importFile") MultipartFile file,
			HttpServletRequest req, HttpServletResponse res) {	
    	JSONObject object = new JSONObject();
		String msg = "";
		File df = null;
		int code = 0;
		String fileId = "";		
		String s = UUID.randomUUID().toString();
		String schoolId = getXxdm(req);
		String keyId = "timetable_" + s;
		try {
			// 初始化基础数据及参数
			setHeadParameter(req);
			// 获取源文件后缀名
			String fileName = file.getOriginalFilename();
			String prefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			fileName = fileName.substring(0,fileName.lastIndexOf(".") + 1) + prefix;

			df = new File(s + "." + prefix);
			System.out.println("目标目录:" + s + "." + prefix);
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
							&&inArrayNum(excelVal, weekDays)==0) {
						code = 2;
						msg = "文件格式正确,字段需要匹配!";
					}
					// 放置临时保存目录
					tempTeacherImpExcTitle[i] = excelVal;
				}
				if(code>=0)
				{
					fileId = fileServerImplFastDFS.uploadFile(df,fileName);
					req.getSession().setAttribute("fileId", fileId);
					fileImportInfoService.addFile(schoolId, keyId, fileId);
					req.getSession().setAttribute("keyId", keyId);
					req.getSession().setAttribute("timetable."+this.getXxdm(req)+".excelTitle", tempTeacherImpExcTitle);
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
		// 封装返回结果
		object.put("msg", msg);
		object.put("code", code);
		return object;
	}

	private void setHeadParameter(HttpServletRequest req) throws Exception {				
		// 学年学期
        String termInfo = req.getParameter("selectedSemester");
        // 获取学校代码		
     	School school = this.getSchool(req,termInfo);
        req.getSession().setAttribute("termInfo", termInfo);  
        // 表头常量数据    
 		weekDays[0] = "班级名称";
 		for (int i = 0; i < weeks.length; i++){
 			for (int j = 0; j < sections.length; j++){
 				 String weekday = weeks[i] + sections[j];
 				 weekDays[i*sections.length + j + 1] = weekday;
 				 JSONObject weekdayObj = new JSONObject();
 				 weekdayObj.put("dayOfWeek", String.valueOf(i));
 				 weekdayObj.put("lessonOfDay", String.valueOf(j));
 				 weekDayMap.put(weekday, weekdayObj);
 			}
 		}	      
	    // 获取班级列表        
        List<Classroom> classList = commonService.getAllClass(school,termInfo);       	
        String[] className = new String[classList.size()];
        HashMap<String,String> classMap = new HashMap<String,String>();
        HashMap<String,Long> classGradeMap = new HashMap<String,Long>();
    	for(int i=0; i < classList.size(); i++){
            className[i] = classList.get(i).getClassName();
            String classId = classList.get(i).getId() + "";           
            classGradeMap.put(classId, classList.get(i).getGradeId());    
            classMap.put(className[i], classId);
        }
    	 // 获取教师列表
        List<Account> accoutList = commonService.getAllSchoolEmployees(school, termInfo, null);
        String[] teacherName = new String[accoutList.size()]; 
        HashMap<String,String> teacherNameMap = new HashMap<String,String>();
    	for(int i=0; i < accoutList.size(); i++){
    		Account account = accoutList.get(i);
            teacherName[i] = account.getName();
            String teacherId = account.getId()+"";
            teacherNameMap.put(teacherName[i], teacherId);
    	}
        // 获取课程列表
        List<LessonInfo> courseList = commonService.getLessonInfoList(school,termInfo);
        String[] courseName = new String[courseList.size()]; 
        HashMap<String,String> courseMap = new HashMap<String,String>();
    	for(int i=0; i < courseList.size(); i++){
            courseName[i] = courseList.get(i).getName();
            String courseId = courseList.get(i).getId() + "";
            courseMap.put(courseName[i], courseId);
    	}	  	
    	// 公共基础数据存储
        String processId = req.getSession().getId();
        String commonMapKey="timetable."+processId+".commonDataMap";
	 	JSONObject commonObj = new JSONObject();
    	commonObj.put("courseName", courseName);
    	commonObj.put("courseMap", courseMap);
    	commonObj.put("className", className);
    	commonObj.put("classMap", classMap);
    	commonObj.put("teacherName", teacherName);
    	commonObj.put("teacherNameMap", teacherNameMap);
    	commonObj.put("classGradeMap", classGradeMap);
    	redisOperationDAO.set(commonMapKey, commonObj, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
  
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
     */
    @RequestMapping(value = "/getExcelMatch")
    @ResponseBody
    public JSONObject getExcelHead(HttpServletRequest req) {
        JSONObject excelHead = new JSONObject();
        // 获取session中保存的临时表头
        String[] tmpTit =(String[]) req.getSession().getAttribute("timetable."+this.getXxdm(req)+".excelTitle");
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
        
        // 直接使用系统表头 系统表头是否必填数组开始拼装返回数据结构
        JSONObject moduleField = new JSONObject();
        JSONArray sysrows = new JSONArray();   
        for (int i = 0; i < weekDays.length; i++) {       	
    		JSONObject obj = new JSONObject();
            obj.put("field", weekDays[i]);
            obj.put("sysfield", 0);                
            sysrows.add(obj);                  
        }    
        moduleField.put("rows", sysrows);
        moduleField.put("total", weekDays.length);
              
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
     */
    @RequestMapping(value = "/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,@RequestBody Map<String,Object> mappost, HttpServletResponse res) {
        String isMatch = mappost.get("isMatch").toString();
        String timetableId = mappost.get("timetableId").toString();
        ImportTaskParameter stt = new ImportTaskParameter();
        String processId = req.getSession().getId();
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
        stt.setTermInfo(req.getSession().getAttribute("termInfo").toString());		
		stt.setProcessId(processId);
		String keyId=req.getSession().getAttribute("keyId").toString();
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
	 	String progressMapKey="timetable."+processId+".progressMap";	
	 	try{
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			String fileId = (String) req.getSession().getAttribute("fileId");
			req.getSession().removeAttribute("fileId");
			SubProcess sp = new SubProcess(processId , fileId);
	        sp.start();         	        
	        msg = "正常启动导入任务!!!";
	 	} catch (Exception e) {
			code = -2;
			msg = "redis保存变量出错!!!";
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
    		String progressMapKey="timetable."+req.getSession().getId()+".progressMap";
    		Object progressMapObj = redisOperationDAO.get(progressMapKey);
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
    		if(progressMap!=null && obj!=null){
    			if(obj.getIntValue("code")==-50)
    			{
    				String schoolId = this.getXxdm(req);
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
		// 设置进度
		String processId = req.getSession().getId();		
		int row = post.getIntValue("row");
		rs.put("rowNum", row);
		JSONArray mrows = post.getJSONArray("mrows");
		int code = post.getIntValue("code");
		
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		JSONObject commonObj = new JSONObject();
		String prepDataMapKey="timetable."+processId+".prepDataMap";
		String commonMapKey="timetable."+processId+".commonDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
		Object commonMapObj=redisOperationDAO.get(commonMapKey);
		if(null != prepDataMapObj && null != commonMapObj)
		{
			prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
			commonObj = (JSONObject)commonMapObj;
		}
		Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();
		String progressMapKey = "timetable."+processId+".progressMap";
		Object progressMapObj = redisOperationDAO.get(progressMapKey);
		if(null!=progressMapObj)
		{
			progressMap=(Hashtable<String, JSONObject>) progressMapObj;
		}	
		if(null==progressMapObj||null==prepDataMapObj||null==commonObj)
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
		if (code == -1) {			
			int lastIndex = datas.get(row-1).length-1;
			int length = datas.size();
			String label = (datas.get(row-1))[lastIndex];
			if ("1".equals(label)){
				datas.set(row-1, null);				
				if (row < length){
					String nextLabel = (datas.get(row))[lastIndex];
					if ("2".equals(nextLabel)){
						datas.set(row, null);
					}
				}				
			}else{
				datas.set(row-1, null);			
			}			
			rs.put("code", 1);
		} else if (code == 1) {			
			List<String[]> pureDatas = new ArrayList<String[]>();
			String[] sd = datas.get(row-1);
			for (int i = 0; i < mrows.size(); i++) {
				JSONObject o = mrows.getJSONObject(i);
			    String title = replaceChar(o.getString("title"));				
				int index = strIndexInArray(title,headTitleName);
				sd[index] = replaceChar(o.getString("value"));
			}
			pureDatas.add(datas.get(0));
			pureDatas.add(sd);
			JSONObject cr = checkImpData(pureDatas, heads, sp, commonObj);

			if (cr.getBooleanValue("ckRs")) {
				rs.put("code", 1);
				JSONObject infor = new JSONObject();
				infor.put("msg", "校验通过！");
				rs.put("data", infor);
			} else {
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
		ImportTaskParameter sp = null;
		List<JSONObject> targetList = null;
		try {
			Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
			String progressMapKey="timetable."+processId+".progressMap";
			Object progressMapObj=redisOperationDAO.get(progressMapKey);
			if(null!=progressMapObj)
			{
				progressMap=(Hashtable<String, JSONObject>) progressMapObj;
			}
			Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
			JSONObject commonObj = new JSONObject();
			String prepDataMapKey="timetable."+processId+".prepDataMap";
			String commonMapKey="timetable."+processId+".commonDataMap";
			Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
			Object commonMapObj=redisOperationDAO.get(commonMapKey);
			if(null != prepDataMapObj && null != commonMapObj)
			{
				prepDataMap=(Hashtable<String, JSONObject>) prepDataMapObj;
				commonObj = (JSONObject)commonMapObj;
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
			JSONObject result = transformData(datas, heads, sp, commonObj);
			targetList = (List<JSONObject>)result.get("rowDatas");	
			HashSet<String> record = (HashSet<String>)commonObj.get("record");
			// 开始入库
			toFront.put("code", 1);
			data.put("msg", "正在保存excel数据,请稍后...");
			data.put("progress", 50);
			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());	
			if (CollectionUtils.isNotEmpty(targetList)) {
				//---删除已经存在的课表---
				List<String> deleteIdList = new ArrayList<String>(record);
				if (CollectionUtils.isNotEmpty(deleteIdList)){
					JSONObject deleteObj = new JSONObject();
					deleteObj.put("timetableId", sp.getTimetableId());
					deleteObj.put("schoolId", sp.getXxdm());
					deleteObj.put("classIdList", deleteIdList);
					timetableService.deleteCourseWalkthrough(deleteObj);
				}
			    int count = timetableService.addTimetableList(targetList);
			    if (count > 0){
			    	JSONObject status = new JSONObject();
			    	status.put("timetableId", sp.getTimetableId());
			    	status.put("schoolId", sp.getXxdm());
			    	status.put("published","1");
			    	timetableService.updateTimetable(status);
			    }
				toFront.put("code", 2);
                data.put("progress", 100);
                msg = "导入成功，共计导入"+ record.size() +"条信息记录！";
                data.put("msg", "导入成功，共计导入"+ record.size() +"条信息记录！");
    			redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			}	
			//---设置教学任务
			List<JSONObject> taskList = (List<JSONObject>)result.get("taskList");
			if (CollectionUtils.isNotEmpty(taskList)) {
				timetableService.setImportTeachingTasks(taskList,sp.getSchool(), sp.getTermInfo());
				//---批量任课教师信息回写,Rsp为空报异常
				try{
					/** ---批量任课教师信息回写--- **/
						timetableService.writebackTeacherList(taskList,
								sp.getSchool(), sp.getTermInfo());		
				}catch (Exception e) {
					System.out.println("打印异常信息:" + e.getMessage());
				}
			}
			//---设置课表基础信息
			JSONObject parameter = result.getJSONObject("section");
			if (parameter.size() > 0){
				timetableService.updateTimetableSection(parameter);
			}
			//---设置单双周课程信息
			List<JSONObject> monoList = (List<JSONObject>)result.get("monoDatas");
			if (CollectionUtils.isNotEmpty(monoList)) {	
				timetableService.setImportMonoWeeks(monoList,sp.getXxdm(),sp.getTimetableId());
			}
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
			String progressMapKey="timetable."+processId+".progressMap";
			String commonMapKey="timetable."+processId+".commonDataMap";
			String suffix = "处理成功,请稍后...";
			String fileIdTmp = "";
			String schoolId = "";
			String keyId = "";
			Hashtable<String, JSONObject> progressMap=new Hashtable<String, JSONObject>();	
			JSONObject commonObj = new JSONObject();
			ImportTaskParameter sp = null;
			List<JSONObject> targetList = null;
			try{
				Object progressMapObj = redisOperationDAO.get(progressMapKey);
				Object commonMapObj=redisOperationDAO.get(commonMapKey);
				if(null != progressMapObj && null != commonMapObj)
				 {
					progressMap = (Hashtable<String, JSONObject>) progressMapObj;
					commonObj = (JSONObject)commonMapObj;
				 }
				else
				{
				   JSONObject toFront= new JSONObject();
				   JSONObject data = new JSONObject();
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
				if (fileIdTmp!=null) {
					fileId = fileIdTmp;
				}
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
				String[][] heads = handleHeadData(datas,sp);
				// 开始校验数据  
				toFront.put("code", 1);
				data.put("progress", 25);
				data.put("msg", "正在校验excel数据" + suffix);
				redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
				JSONObject ckrs = checkImpData(datas, heads, sp, commonObj);				
				if (ckrs.getBooleanValue("ckRs")) {
					// 处理表格数据					   
					toFront.put("code", 1);
					data.put("progress", 35);
					data.put("msg", "转换excel数据" + suffix);
					redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					JSONObject result = transformData(datas, heads, sp, commonObj);
					targetList = (List<JSONObject>)result.get("rowDatas");	
					HashSet<String> record = (HashSet<String>)commonObj.get("record");
					// 开始入库
					toFront.put("code", 1);
					data.put("progress", 50);
					data.put("msg", "保存excel数据" + suffix);
					redisOperationDAO.set(progressMapKey, progressMap, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
					//---设置节次
					JSONObject parameter = result.getJSONObject("section");
					if (parameter.size() > 0){
						timetableService.updateTimetableSection(parameter);
					}
					//---设置教学任务\课表设置					
					List<JSONObject> taskList = (List<JSONObject>)result.get("taskList");
					if (CollectionUtils.isNotEmpty(taskList)) {
						timetableService.setImportTeachingTasks(taskList,sp.getSchool(),sp.getTermInfo());
						//---批量任课教师信息回写,Rsp为空报异常
						try{
							/** ---批量任课教师信息回写--- **/
								timetableService.writebackTeacherList(taskList,
										sp.getSchool(), sp.getTermInfo());		
						}catch (Exception e) {
							System.out.println("打印异常信息:" + e.getMessage());
						}
					}
					//---设置单双周课程信息
					List<JSONObject> monoList = (List<JSONObject>)result.get("monoDatas");
					if (CollectionUtils.isNotEmpty(monoList)) {	
						timetableService.setImportMonoWeeks(monoList,sp.getXxdm(),sp.getTimetableId());						
					}
					if (CollectionUtils.isNotEmpty(targetList)) {
						//---删除已经存在的课表---
						List<String> deleteIdList = new ArrayList<String>(record);
						if (CollectionUtils.isNotEmpty(deleteIdList)){
							JSONObject deleteObj = new JSONObject();
							deleteObj.put("timetableId", sp.getTimetableId());
							deleteObj.put("schoolId", sp.getXxdm());
							deleteObj.put("classIdList", deleteIdList);
							timetableService.deleteCourseWalkthrough(deleteObj);
						}
						//---导入课表课程信息---
					    int count = timetableService.addTimetableList(targetList);
					    if (count > 0){
					    	JSONObject status = new JSONObject();
					    	status.put("timetableId", sp.getTimetableId());
					    	status.put("schoolId", sp.getXxdm());
					    	status.put("published","1");
					    	timetableService.updateTimetable(status);
					    }
					    toFront.put("code", 2);
						data.put("progress", 100);
	                    data.put("msg", "导入成功，共计导入"+ record.size() +"条信息记录！");
					}else{
						    toFront.put("code", 2);
							data.put("progress", 100);
		                    data.put("msg", "没有有效的任教数据！");
					}						
				} else {					
					toFront.put("code", -2);
					data.put("progress", 100);
					data.put("msg", "Excel数据校验不通过!");
					data.put("total", datas.size()-1);					
					data.put("validateMsg", ckrs.get("validateMsg"));
				}				
			} catch (Exception e) {
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
	private JSONObject readExcelToData(String fileId,String processId)throws Exception {
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
		/** ---合并单元格数量--- */
		int sheetMergeCount = sheet.getNumMergedRegions();	
		int rows = sheet.getLastRowNum();
		/** 一般poi取数字格式需转换  */
		DecimalFormat df = new DecimalFormat("0");

		if (rows > 0) {
			int cols = sheet.getRow(0).getPhysicalNumberOfCells();
			headTitleName = new String[cols];
			for (int i = 0; i < rows + 1; i++) {
				if (sheet.getRow(i) == null) {
					continue;
				}
				String[] temp = null;
				if (i > 0){
					temp = new String[cols+1];
					temp[cols] = "1";
				}else{
					temp = new String[cols];
				}
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
			if (sheetMergeCount > 0){
				for (int k = 0; k < sheetMergeCount; k++) {
					 CellRangeAddress range = sheet.getMergedRegion(k);
					 int firstRow = range.getFirstRow();
					 int firstCol = range.getFirstColumn();
					 int lastRow = range.getLastRow();
					 int lastCol = range.getLastColumn();
					 if (lastRow < datas.size() && lastCol < cols)
					 {
						 String value = (datas.get(firstRow))[firstCol];
						 if(StringUtils.isNotEmpty(value))
						   (datas.get(lastRow))[lastCol] = value;
						   (datas.get(lastRow))[cols] = "2";
					 }	
				}
			}		
		}
		workbook.close();
		if (null != file)
		{
			file.delete();
		}
		rs.put("datas", datas);
		rs.put("headTitleName", headTitleName);
		Hashtable<String, JSONObject> prepDataMap = new Hashtable<String, JSONObject>();
		prepDataMap.put(processId, rs);
		String prepDataMapKey="timetable."+processId+".prepDataMap";
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
	 * @param datas
	 * @param mrs
	 * @param isMatch
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String[][] handleHeadData(List<String[]> datas,ImportTaskParameter sp) throws Exception{
		int isMatch = sp.getIsMatch();
		String[] titles = (String[]) datas.get(0);
		// 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射实体字段[3]
		String[][] array = new String[5][titles.length];
		// 无需手工匹配
		if (isMatch == 0) {
			// 封装表头
			for (int i = 0; i < titles.length; i++) {
				array[0][i] = titles[i];
				int needIndex = strIndexInArray(titles[i], weekDays);
				JSONObject weekDay = weekDayMap.get(titles[i]);
				if (needIndex > 0) {
					// 在系统字段中能找到
					array[1][i] = "0";
					array[2][i] = weekDays[needIndex];
					array[3][i] = weekDay.getString("dayOfWeek");
					array[4][i] = weekDay.getString("lessonOfDay");
				}else if(needIndex == 0){
					array[1][i] = "1";
					array[2][i] = weekDays[needIndex]; 			
				}else {
					// 在系统字段中找不到 标记为不录入
					array[1][i] = "-1";
					array[2][i] = "none";
				}
			}
		} else {
			// 需要手工匹配的 根据匹配关系封装表头
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
				int needIndex = strIndexInArray(sysTit, weekDays);
				JSONObject weekDay = weekDayMap.get(sysTit);
				if (needIndex > 0) {
					// 在系统字段中能找到
					array[1][i] = "0";
					array[2][i] = weekDays[needIndex];
					array[3][i] = weekDay.getString("dayOfWeek");
					array[4][i] = weekDay.getString("lessonOfDay");
				}else if(needIndex == 0){
					array[1][i] = "1";
					array[2][i] = weekDays[needIndex];
				}else {
					// 在系统字段中找不到 标记为不录入
					array[1][i] = "-1";
					array[2][i] = "none";
					array[0][i] = null;
				}
			}
		}
		String prepDataMapKey="timetable."+sp.getProcessId()+".prepDataMap";
		Object prepDataMapObj=redisOperationDAO.get(prepDataMapKey);
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
	private JSONObject checkImpData(List<String[]> datas,
			String[][] head,ImportTaskParameter stt, JSONObject commonObj) {
		JSONObject result = new JSONObject();
		JSONObject validateMsg = new JSONObject();
		JSONArray rows = new JSONArray();
		//基础信息
		String[] className = (String[])commonObj.get("className");
		String[] courseName = (String[])commonObj.get("courseName");
		String[] teacherName = (String[])commonObj.get("teacherName");
		for(int i = 1; i < datas.size(); i++){
			JSONArray mrows = new JSONArray();
			JSONObject row = new JSONObject();
			row.put("row", i + 1);
			String[] title = datas.get(0);
			String[] array = datas.get(i);
			String label = array[array.length -1];
			for(int j = 0; j < array.length -1; j++){
				int isIn = Integer.parseInt(head[1][j]);
				if (isIn != -1){
					String fields = array[j];
					String[] fieldList = new String[0];
					fieldList = fields.replace("/", ",").split(",");
					for(String field : fieldList){
						if (j > 0){
							if ("1".equals(label)){
								if (StringUtils.isNotEmpty(field)){
									if (inArrayNum(field,courseName)==0){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[j]);
										wsg.put("oldValue", fields);
										wsg.put("type", label);
										wsg.put("err", "课程不匹配!");
										mrows.add(wsg);break;
									}else if(inArrayNum(field,courseName) > 1){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[j]);
										wsg.put("oldValue", fields);
										wsg.put("type", label);
										wsg.put("err", "多个课程重名!!!");
										mrows.add(wsg);break;
									}
								}
							}else if("2".equals(label)){
								if (StringUtils.isNotEmpty(field)){
									if (inArrayNum(field,teacherName)==0){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[j]);
										wsg.put("oldValue", fields);
										wsg.put("type", label);
										wsg.put("err", "教师不匹配!");
										mrows.add(wsg);break;
									}else if(inArrayNum(field,teacherName) > 1){
										JSONObject wsg = new JSONObject();
										wsg.put("title", title[j]);
										wsg.put("oldValue", fields);
										wsg.put("type", label);
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
			if (mrows.size() > 0){
				row.put("mrows",mrows);
				rows.add(row);
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
	
	/** 年级ID转使用年级**/ 
	private String convertSYNJ(long schoolId,long gradeId,String termInfo) {
		String synj = "";
		Grade grade = commonService.getGradeById(schoolId,gradeId,termInfo);
		if (null != grade){
		    String xn = termInfo.substring(0, 4);
			String gradeLevel = grade.getCurrentLevel().getValue()+"";
			synj = commonService.ConvertNJDM2SYNJ(gradeLevel, xn);
		}
		return synj;	
	} 
    
	/**
	 * 校验表格数据
	 * 
	 * @param datas
	 * @param array
	 * @param sp 
	 * @param commonObj 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private JSONObject transformData(List<String[]> datas,
			String[][] array, ImportTaskParameter sp, JSONObject commonObj) throws Exception {
		// 基础信息
		HashMap<String,String> classMap = (HashMap<String,String>)commonObj.get("classMap");
		HashMap<String,String> courseMap = (HashMap<String,String>)commonObj.get("courseMap");
		HashMap<String,String> teacherNameMap = (HashMap<String,String>)commonObj.get("teacherNameMap");
		HashMap<String,Long> classGradeMap = (HashMap<String,Long>)commonObj.get("classGradeMap");
		
		String ttdm = sp.getTimetableId();
		String xxdm = sp.getXxdm();
		int length = datas.size();
		int size = array[0].length;
		Map<String,HashMap<String,JSONObject>> task = new HashMap<String,HashMap<String,JSONObject>>();
		Map<Long,String> gradeIds = new HashMap<Long,String>();
		List<JSONObject> rowDatas = new ArrayList<JSONObject>();
		List<JSONObject> monoDatas = new ArrayList<JSONObject>();
		HashSet<String> record = new HashSet<String>();
		for (int i = 1; i < length;) {
			if (null == datas.get(i)) {
				i = i + 1;
				continue;			
			}
			String[] cell = (String[]) datas.get(i); 
			String[] cellNext = null;
			int k = i + 1;
			if( k < length){				
				cellNext = (String[]) datas.get(k);
				if (ArrayUtils.isNotEmpty(cellNext)
						&& "2".equals(cellNext[size])) {
					i = k + 1;
				} else {
					i = k;
					cellNext = null;
				}
			}else{
				i = i + 1;
			}
			String classId = "";
			String upstr = cell[0].toUpperCase();
			String lowstr = cell[0].toLowerCase();
			if (classMap.containsKey(upstr)){
				classId = classMap.get(upstr);
			}else if(classMap.containsKey(lowstr)){
				classId = classMap.get(lowstr);
			}
			if(StringUtils.isEmpty(classId))continue;
			record.add(classId);
			commonObj.put("record", record);
			String commonMapKey="timetable."+sp.getProcessId()+".commonDataMap";
			redisOperationDAO.set(commonMapKey, commonObj, CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
			long gradeId = classGradeMap.get(classId);
			String usedGrade = "";
			if(!gradeIds.containsKey(gradeId)){
				usedGrade = convertSYNJ(Long.parseLong(xxdm),gradeId,sp.getTermInfo());
				gradeIds.put(gradeId, usedGrade);
			}else{
				usedGrade = gradeIds.get(gradeId);
			}
			HashMap<String,JSONObject> course = new HashMap<String,JSONObject>();
			task.put(classId, course);
			for (int j = 1; j < size; j++) {
				 int isIn = Integer.parseInt(array[1][j]);
				 if (isIn != -1 && StringUtils.isNotEmpty(cell[j])) {
					 List<String> teachers = new ArrayList<String>();
					 List<String> lessons = new ArrayList<String>();
                     // --- 设置单元格课程及教师信息 
					 String lessonValue = cell[j];
					 String[] ls = lessonValue.split("/");
					 for(int v = 0;v < ls.length; v++) {
						 lessons.add(courseMap.get(ls[v]));
					 }
					 if(null != cellNext && StringUtils.isNotEmpty(cellNext[j])) {
						String teacherValue = cellNext[j];
						String[] ts = teacherValue.split("/");
						for(int m = 0; m < ts.length; m++) {
						    String tsvalue = ts[m];
						    boolean isAdd = false;
						    String[] values = tsvalue.split(",");
						    for(String value : values) {
							if(StringUtils.isEmpty(value))break;
						    String v = teacherNameMap.get(value);
						    tsvalue = tsvalue.replace(value, v);
						    isAdd = true;
						    }
						    if(isAdd)teachers.add(tsvalue);
						}
					 }
					 if (lessons.size() == 1){
						 packTimetable(array, ttdm, xxdm, rowDatas, classId,
									course, j, teachers, lessons, 0, 1f, "0");						
					 }else if(lessons.size() == 2){
						for(int n = 0; n < 2; n++){
							packTimetable(array, ttdm, xxdm, rowDatas, classId,
									course, j, teachers, lessons, n, 0.5f, String.valueOf(n+1));	
						}
						// --- 设置单双周信息 
						JSONObject m = new JSONObject();
						String course_one = lessons.get(0);
						String course_two = lessons.get(1);
						m.put("timetableId", ttdm);
						m.put("schoolId", xxdm);
						m.put("gradeId", usedGrade);
						m.put("classIds", classId);
						m.put("courseIdOne", course_one);
						m.put("courseIdTwo", course_two);
						repeatMonos(m,monoDatas);
					 }
				}
			}			
		}
		
		/** ---课表设置周期天数及上下午节数--- **/
		String section = Arrays.toString(datas.get(0));
		int maxWeek = 0;
		int maxSection = 0;
        JSONObject parameter = new JSONObject();
		for(int k = weeks.length-1;k >= 0;k--){
			if (section.contains(weeks[k])){
				maxWeek = k + 1;
				break;
			}
		}
        for(int l = sections.length-1;l >= 0;l--){
        	if (section.contains(sections[l])){
        		maxSection = l + 1;
				break;
			}
		}       		
        
		if (maxWeek > 0 && maxSection > 0) { 
			int amLessonNum = 0;
			int pmLessonNum = 0;
            if (maxSection > 3){
            	amLessonNum = 4;
            	pmLessonNum = maxSection - amLessonNum;
            }else{
            	amLessonNum = maxSection;
            }
            List<String> gradeIdList = new ArrayList<String>();
            JSONArray row = new JSONArray();         
            for(Map.Entry<Long, String> entry : gradeIds.entrySet()){
            	String usedGrade = entry.getValue();
            	if (StringUtils.isNotEmpty(usedGrade)){
    				  gradeIdList.add(usedGrade);
    				JSONObject grade = new JSONObject();
      			    grade.put("gradeId", usedGrade);
      			    grade.put("amLessonNum", amLessonNum);
      			    grade.put("pmLessonNum", pmLessonNum);
      			    row.add(grade);
    			}
            }     
            if (row.size() > 0){
            	parameter.put("timetableId", ttdm);
    			parameter.put("schoolId", xxdm);
    			parameter.put("selectedSemester", sp.getTermInfo());
    			parameter.put("maxDaysForWeek", maxWeek);
    			parameter.put("rows", row);
    			parameter.put("gradeIdList", gradeIdList);
            }   
		}
		
		/** ---课表设置教学任务--- **/
		List<JSONObject> taskList = new ArrayList<JSONObject>();
		Iterator<Entry<String, HashMap<String,JSONObject>>> iter = task.entrySet().iterator();
        while (iter.hasNext()) {
        	   Map.Entry<String,HashMap<String,JSONObject>> entry = (Map.Entry<String,HashMap<String,JSONObject>>) iter.next();
        	   String bjid = entry.getKey();
        	   HashMap<String,JSONObject> lesson = entry.getValue();
        	   Iterator<Entry<String, JSONObject>> iterators = lesson.entrySet().iterator();
        	   while (iterators.hasNext()) {
        		   Map.Entry<String,JSONObject> crs = (Map.Entry<String,JSONObject>) iterators.next();
            	   JSONObject courseObj = new JSONObject();
            	   JSONObject les = crs.getValue();   
                   courseObj.put("timetableId", ttdm);
                   courseObj.put("schoolId", xxdm);
                   courseObj.put("courseId", crs.getKey());
                   courseObj.put("classId", bjid);
                   courseObj.put("weekNum", les.getFloat("number"));
                   courseObj.put("nearNum", "0");
                   courseObj.put("teacherIds", les.getString("teacher")==null?"":les.getString("teacher"));
                   taskList.add(courseObj);
        	   } 
        }			
		JSONObject rs = new JSONObject();
		rs.put("rowDatas", rowDatas);
		rs.put("taskList", taskList);
		rs.put("section", parameter);
		rs.put("monoDatas", monoDatas);
		return rs;
	}

	/** ---封装课表数据--- **/
	private void packTimetable(String[][] array, String ttdm, String xxdm,
			List<JSONObject> rowDatas, String classId,
			HashMap<String, JSONObject> course, int j, List<String> teachers,
			List<String> lessons, int n, float m, String type) {
		String lessonId = lessons.get(n);
		if(course.containsKey(lessonId)){
		   JSONObject lesson = course.get(lessonId);
		   float num = lesson.getFloat("number");
		   lesson.put("number", num + m);
		   String teach = lesson.getString("teacher");
		   if (teachers.size() > n) {	
			   lesson.put("teacher", repeatTearch(teach,teachers.get(n)));
		   }
		   course.put(lessonId, lesson);
		 }else{
		   JSONObject lesson = new JSONObject();
		   lesson.put("number", m);
		   if(teachers.size() > n){
			  lesson.put("teacher", teachers.get(n));
		   }
		   course.put(lessonId, lesson);
		 }
		 JSONObject d = new JSONObject();
		 d.put("classId", classId);
		 d.put("dayOfWeek", array[3][j]);
		 d.put("lessonOfDay", array[4][j]);
		 d.put("courseId", lessonId);
		 d.put("timetableId", ttdm);
		 d.put("schoolId", xxdm);
		 d.put("isAdvance", "0");
		 d.put("courseType", type);
		 d.put("walkGroupId", null);
		 rowDatas.add(d);
	}
	
	private String repeatTearch(String teacher, String target) {
		String[] targets = target.split(",");
		if (StringUtils.isEmpty(teacher)){
			teacher = target;
		}else{
			for(String value : targets){
				if(!teacher.contains(value))
					teacher = teacher + "," + value;	
			}
		}
		return teacher;
	}
    
	/** -----过滤重复单双周数据----- */
	private void repeatMonos(JSONObject m, List<JSONObject> monos) {
		boolean isMerge = false;
		for (JSONObject source : monos) {
			String s_course1 = source.getString("courseIdOne");
			String t_course1 = m.getString("courseIdOne");
			String s_course2 = source.getString("courseIdTwo");
			String t_course2 = m.getString("courseIdTwo");
			String s_gradeId = source.getString("gradeId");
			String t_gradeId = m.getString("gradeId");
			if (s_course1.equals(t_course1) && s_course2.equals(t_course2)
					&& s_gradeId.equals(t_gradeId)) {
				source.put(
						"classIds",
						source.getString("classIds") + ","
								+ m.getString("classIds"));
				isMerge = true;break;
			}
		}
		if (!isMerge)monos.add(m);
	}

}