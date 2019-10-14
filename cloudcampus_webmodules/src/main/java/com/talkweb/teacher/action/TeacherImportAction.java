package com.talkweb.teacher.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.IDCard;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.CommonDataDao;
import com.talkweb.commondata.domain.TEdTerminfo;
import com.talkweb.datadictionary.dao.DataDictionaryDao;
import com.talkweb.datadictionary.domain.TDmXb;
import com.talkweb.student.domain.page.StartImportTaskParam;
import com.talkweb.student.service.XmlUtil;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;
import com.talkweb.teacher.domain.page.THrTeacher;
import com.talkweb.teacher.service.TeacherService;

/**
 * 
 * @ClassName: StudentImportAction.java
 * @version:1.0
 * @Description: 教师导入管理
 * @author 武洋 ---智慧校
 * @date 2015年3月19日
 */
@Controller
@RequestMapping("/teacher/import")
public class TeacherImportAction {

    @Autowired
    private TeacherService teaService;
    @Autowired
    private DataDictionaryDao dataDictionaryDao;
    
    @Autowired
    private CommonDataDao cmdDao ;
    /**
     * 获取临时文件保存目录
     */
    @Value("#{settings['tempFilePath']}")
    private String tempFilePath;

    /**
     * 导入教师excel的表头--系统字段
     */
    private static final String[] stuTitle = {"工号","姓名", "性别", "出生日期", "婚姻状况","民族","籍贯","身份证号","政治面貌","职务","职称","参加工作年月","从教年月","来校时间" ,"教职工类别","所任科目","文化程度","最高学位","联系电话","通讯地址","邮政编码","电子信箱"};

    /**
     * 导入教师excel的表头--系统字段是否必填
     */
    private static final int[] stuTitleNeed = { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    /**
     * 导入教师excel的表头--系统字段字段名
     */
    private static final String[] stuTitleName = { "jsdm", "xm", "xbm", "csrq", "hyxkm", "mzm", "jgm", "sfzh","zzmmm","zw","zc","gzny","cjny","rxny","bzlbm","srkm","xlm","xwm","lxdh","yzbm","dzxx","txdz" };
    /**
     * 导入进程百分之*
     */
    // private static int progNum = 0;
    /**
     * 导入进程--当前进程名称
     */
    // private static String progName = "正在开始";
    /**
     * 导入进程结果
     */
    // private static int progCode = 0;
    /**
     * 多线程
     * 每个用户有自己的参数组和进程
     */
    private static Hashtable<String, JSONObject> progressMap = new Hashtable<String, JSONObject>();
    /**
     * 多线程
     * 每个用户有自己的临时文件目录
     */
    private static Hashtable<String, String> tempFileMap = new Hashtable<String, String>();
    /**
     * 多线程
     * 每个用户有自己的临时excel表头
     */
    private static Hashtable<String, String[]> excelTitleMap = new Hashtable<String, String[]>();
    /**
     * 性别列表
     */
    private static JSONArray xbList = new JSONArray();
    /**
     * 籍贯列表
     */
    private static JSONArray jgList = new JSONArray();
    /**
     * 婚姻状况列表
     */
    private static JSONArray hyList = new JSONArray();
    /**
     * 民族列表
     */
    private static JSONArray mzList = new JSONArray();
    /**
     * 政治面貌列表
     */
    private static JSONArray zzmmList = new JSONArray();
    /**
     * 职务列表
     */
    private static JSONArray zwList = new JSONArray();
    /**
     * 职称列表
     */
    private static JSONArray zcList = new JSONArray();
    /**
     * 职工类别列表
     */
    private static JSONArray zglbList = new JSONArray();
    /**
     * 文化程度列表
     */
    private static JSONArray whcdList = new JSONArray();
    /**
     * 最高学位列表
     */
    private static JSONArray zgxwList = new JSONArray();
    /**
     * 岗位状态列表
     */
    private static JSONArray gwztList = new JSONArray();
    /**
     * 任教科目列表
     */
    private static JSONArray rjkmList = new JSONArray();
    /**
     * 所有参数列表
     */
    private static JSONObject allParamMap = null;

    /**
     * 子线程是否在跑
     */
    // private static int subProSta = 0;
    @RequestMapping(value = "/getParam")
    @ResponseBody
    public JSONObject getParam( HttpServletRequest req, HttpServletResponse res) {
        if(allParamMap==null){
            getParams();
        } 
        return allParamMap;
    }

    private void getParams() {
        // TODO Auto-generated method stub
        allParamMap = new JSONObject();
        JSONArray rows = new JSONArray();
        allParamMap.put("total", 11);
        allParamMap.put("rows", rows);
        xbList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMXB());
        jgList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMJG());
        JSONObject row = new JSONObject();
        String value = "";
        row.put("name", "性别");
        value = getValByList(xbList);
        row.put("value", value );
        rows.add(row);
        //婚姻状况
        row = new JSONObject();
        row.put("name", "婚姻状况");
        hyList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMHYZK());
        value = getValByList(hyList);
        row.put("value", value );
        rows.add(row);
        //民族
        row = new JSONObject();
        row.put("name", "民族");
        mzList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMMZ());
        value = getValByList(mzList);
        row.put("value", value );
        rows.add(row);
        //政治面貌
        row = new JSONObject();
        row.put("name", "政治面貌");
        zzmmList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMZZMM());
        value = getValByList(zzmmList);
        row.put("value", value );
        rows.add(row);
        //职务
        row = new JSONObject();
        row.put("name", "职务");
        zwList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMZW());
        value = getValByList(zwList);
        row.put("value", value );
        rows.add(row);
        //职称
        row = new JSONObject();
        row.put("name", "职称");
        zcList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMZC());
        value = getValByList(zcList);
        row.put("value", value );
        rows.add(row);
        //教职工类别
        row = new JSONObject();
        row.put("name", "教职工类别(编制类别)");
        zglbList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMBZLB());
        value = getValByList(zglbList);
        row.put("value", value );
        rows.add(row);
        //文化程度
        row = new JSONObject();
        row.put("name", "文化程度");
        whcdList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMXL());
        value = getValByList(whcdList);
        row.put("value", value );
        rows.add(row);
        //最高学位
        row = new JSONObject();
        row.put("name", "最高学位");
        zgxwList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMXW());
        value = getValByList(zgxwList);
        row.put("value", value );
        rows.add(row);
        //岗位状态
        row = new JSONObject();
        row.put("name", "岗位状态");
        JSONArray arr = new JSONArray();
        JSONObject b = new JSONObject();
        b.put("userDm", "0");
        b.put("mc", "在岗");
        arr.add(b);
        b = new JSONObject();
        b.put("userDm", "1");
        b.put("mc", "不在岗");
        arr.add(b);
        b = new JSONObject();
        b.put("userDm", "1");
        b.put("mc", "离校");
        arr.add(b);
        gwztList = arr;
        value = getValByList(gwztList);
        row.put("value", value );
        rows.add(row);
        //任教科目
        row = new JSONObject();
        row.put("name", "任教科目");
        rjkmList = (JSONArray) JSON.toJSON(dataDictionaryDao.getTDMRJKM());
        value = getValByList(rjkmList);
        row.put("value", value );
        rows.add(row);
        
    }

    private String getValByList(JSONArray list) {
        // TODO Auto-generated method stub
        String space = "                                                 ";
        String x = "<div>";
        for(int i=0;i<list.size();i++){
            JSONObject obj = list.getJSONObject(i);
            String x1 = "<span style='width:100px;display:inline-block;'>"+
                    obj.getString("mc").trim()+"["+obj.getString("userDm").trim()+"]"
                    +"</span>";
            x = x + x1;
        } 
        x = x+"</div>";
        return x;
    }

    /**
     * Excel上传接口
     * 
     * @param file
     * @param req
     * @param res
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = "/uploadExcel")
    @ResponseBody
    public JSONObject uploadExcel(@RequestParam("stuExcel") MultipartFile file, HttpServletRequest req,
            HttpServletResponse res) {

        JSONObject obj = new JSONObject();

        int code = 0;
        String msg = "";
        // 临时文件保存目录
        File dir = new File(tempFilePath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            // 初步解析上传的excel，判断字段是否符合原始系统字段
            String s = UUID.randomUUID().toString();
            // 获取源文件后缀名
            String fileName = file.getOriginalFilename();
            String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);

            File df = new File(tempFilePath + "/" + s + "." + prefix);
            System.out.println("目标目录：" + tempFilePath + "/" + s + "." + prefix);
            file.transferTo(df);
            String userid = ((TUcUser) req.getSession().getAttribute("user")).getUsersysid();
            // req.getSession().putValue("tempStuImpExcFileDir", tempFilePath + "/" + s + "." + prefix);
            tempFileMap.put(userid, tempFilePath + "/" + s + "." + prefix);
            Workbook workbook = null;
            if(prefix.equalsIgnoreCase("xls")){
                workbook = new HSSFWorkbook(new FileInputStream(df));
            }else if(prefix.equalsIgnoreCase("xlsx")){
                workbook = new XSSFWorkbook(new FileInputStream(df));
            }else{
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
                    String excelVal = row.getCell(i).getStringCellValue();
                    if (!isInArray(excelVal, stuTitle)) {
                        code = 2;
                        msg = "文件格式正确,字段需要匹配";
                    }
                    // 放置临时保存目录
                    tempStuImpExcTitle[i] = excelVal;
                }

                excelTitleMap.put(userid, tempStuImpExcTitle);
            } else {
                code = -2102;
                msg = OutputMessage.getDescByCode(code + "");
            }
            workbook.close();
        } catch (Exception e) {
            code = -2101;
            msg = "文件格式错误或无法读取！";
            msg = OutputMessage.getDescByCode(code + "");
        }
        // 封装返回结果
        JSONObject data = new JSONObject();
        data.put("msg", msg);

        obj.put("code", code);
        obj.put("data", data);
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
        // TODO Auto-generated method stubr
        boolean rs = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equalsIgnoreCase(string)) {
                rs = true;
                return rs;
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
        // TODO Auto-generated method stub
        int rs = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null && arr[i].equalsIgnoreCase(string)) {
                rs = i;
                return rs;
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
        String userid = ((TUcUser) req.getSession().getAttribute("user")).getUsersysid();
        String[] tmpTit = (String[]) excelTitleMap.get(userid);
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
     */
    @RequestMapping(value = "/startImportTask")
    @ResponseBody
    public synchronized JSONObject startImportTask(HttpServletRequest req,
            HttpServletResponse res) {
        StartImportTaskParam stt = new StartImportTaskParam();
        //获取数据字典
        if(allParamMap==null){
            getParams();
        } 
        stt.setIsMatch(Integer.parseInt(req.getParameter("isMatch")));
        stt.setMatchResult(JSON.parseArray(req.getParameter("matchResult")));
        
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        TUcUser tcUser = (TUcUser) req.getSession().getAttribute("user");
        TTrSchool tcSchool = (TTrSchool) req.getSession().getAttribute("school");
        TEdTerminfo ti = (TEdTerminfo) req.getSession().getAttribute("currentXNXQ");
        JSONObject procObj = new JSONObject();
        //  根据使用年级、学年获取入学年度
        procObj.put("taskParam", stt);
        stt.setXn(ti.getXn());
        stt.setXq(ti.getXqm());
        stt.setXxdm(tcSchool.getXxdm());
        //设置获取的参数 
        procObj.put("school", tcSchool);
        progressMap.put(tcUser.getUsersysid(), procObj);

        SubProcess sp = new SubProcess(tcUser.getUsersysid());
        sp.start();
        obj.put("code", 0);
        System.out.println("主线程结束！");
        return obj;

    }

    /**
     * 校验表格数据
     * 
     * @param datas
     * @param mrs
     * @param isMatch
     * @return
     */
    private JSONObject checkImpData(JSONArray datas, JSONArray mrs, int isMatch) {
        if(mrs==null){
            isMatch = 0;
        }
        JSONObject rs = new JSONObject();
        String[] titles = (String[]) datas.get(0);
        // 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射教师实体字段[3]
        String[][] arr = new String[4][titles.length];
        // 无需手工匹配
        if (isMatch == 0) {
            // 封装表头
            for (int i = 0; i < titles.length; i++) {
                arr[0][i] = titles[i];
                int needIndex = strIndexInArray(titles[i], stuTitle);
                if (needIndex >= 0) {
                    // 在系统字段中能找到
                    arr[1][i] = stuTitleNeed[needIndex] + "";
                    arr[2][i] = stuTitle[needIndex];
                    arr[3][i] = stuTitleName[needIndex];
                } else {
                    // 在系统字段中找不到 标记为不录入
                    arr[1][i] = "-1";
                    arr[2][i] = "none";
                }
            }
        } else {
            // 需要手工匹配的 根据匹配关系封装表头

            // 封装表头
            for (int i = 0; i < titles.length; i++) {
                String sysTit = "none";
                for (int j = 0; j < mrs.size(); j++) {
                    JSONObject obj = mrs.getJSONObject(j);
                    if (titles[i].equalsIgnoreCase(obj.getString("excelField"))) {
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
                }
            }
        }
        // progNum = 6;
        // 开始校验 excel表头[0] 是否必填[1] 映射的系统字段[2] 映射教师实体字段[3]
        JSONArray wrongMsg = new JSONArray();
        JSONArray exDatas = new JSONArray();
        HashMap<String, String> ghMap = new HashMap<String, String>();
        int xmIndex = strIndexInArray("xm", arr[3]);
        int xjhIndex = strIndexInArray("jsdm", arr[3]);
        int csrqIndex = strIndexInArray("csrq", arr[3]);
        int sfzhIndex = strIndexInArray("sfzh", arr[3]);
        int xbIndex = strIndexInArray("xbm", arr[3]);
        List<TDmXb> xbList = dataDictionaryDao.getTDMXB();
        Hashtable<Integer,JSONObject> wmsgMap = new Hashtable<Integer, JSONObject>(); 
        for (int i = 0; i < arr[0].length; i++) {
            /**
             * 是否必填
             */
            int isIn = Integer.parseInt(arr[1][i]);
            if (isIn != -1) {
                for (int j = 1; j < datas.size(); j++) {
                    // 是否已校验 出生日期和性别 用于出生日期和性别为空时 由身份证自动补充过的
                    boolean isCkCsrq = false;
                    boolean isCkXb = false;

                    String[] cell = (String[]) datas.get(j);
                    JSONObject wmsg = new JSONObject();

                    wmsg.put("row", (j + 1));
                    wmsg.put("xm", cell[xmIndex]);
                    wmsg.put("xjh", cell[xjhIndex]);
                    String err = "";
                    if (isIn == 1) {
                        // 先校验必填项是否为空
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            err += "[" + arr[2][i] + "]不能为空！";

                        }
                        if (arr[3][i].equalsIgnoreCase("jsdm")) {
                            // 校验教师代码(工号)是否重复
                            if (!ghMap.isEmpty() && cellVal != null && cellVal.trim().length() > 0) {
                                if (ghMap.containsKey(cellVal)) {

                                    err += "[工号]重复！";

                                } else {
                                    ghMap.put(cellVal, "exist");
                                }
                            }
                        }
                    } else if (arr[3][i].equalsIgnoreCase("csrq")) {
                        if(isCkCsrq){
                            // 非必填验证出生日期
                            String cellVal = cell[i];
                            if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                                ((String[]) datas.get(j))[i] = null;
                            } else {
                                // 验证日期格式是否合法
                                JSONObject obj = checkIsStrDate(cellVal);
                                if (obj.getBooleanValue("ckRs")) {
                                    ((String[]) datas.get(j))[i] = obj.getString("date");
                                } else {
                                    err += "[出生日期]格式有误！";
                                }
                            }
                        }else{
                            String cellVal = cell[i];
                            if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                                ((String[]) datas.get(j))[i] = null;
                            } 
                        }
                    } else if (arr[3][i].equalsIgnoreCase("sfzh")) {
                        // 非必填验证身份证号
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证身份证格式是否合法
                            if (IDCard.IDCardValidate(cellVal).equalsIgnoreCase("YES")) {
                                String xbm = IDCard.getXbm(cellVal);
                                String csrq = IDCard.getCsDate(cellVal);
                                if (cell[xbIndex] == null || cell[xbIndex].equalsIgnoreCase("")
                                        || cell[xbIndex].trim().length() == 0) {
                                    ((String[]) datas.get(j))[xbIndex] = getCodeByXBM(xbm, xbList);
                                    isCkXb = true;
                                }
                                if (cell[csrqIndex] == null || cell[csrqIndex].equalsIgnoreCase("")
                                        || cell[csrqIndex].trim().length() == 0) {
                                    ((String[]) datas.get(j))[csrqIndex] = csrq;
                                    isCkCsrq = true;
                                }
                            } else {
                                err += "[身份证]格式有误！";
                            }
                        }
                    } else if (arr[3][i].equalsIgnoreCase("xbm") && isCkXb) {
                        // 非必填验证性别
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证性别是否合法
                            String xbm = getCodeByXBM(cellVal, xbList);
                            if (xbm == null) {
                                err += "[性别]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    } else if (arr[3][i].equalsIgnoreCase("hyxkm") ) {
                        // 非必填验证性别
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证性别是否合法
                            String xbm = XmlUtil.getCodeByValue(cellVal, hyList);
                            if (xbm == null) {
                                err += "[婚姻状况]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("mzm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, mzList);
                            if (xbm == null) {
                                err += "[民族]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("jgm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, jgList);
                            if (xbm == null) {
                                err += "[籍贯]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("zzmmm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, zzmmList);
                            if (xbm == null) {
                                err += "[政治面貌]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("zw") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, zwList);
                            if (xbm == null) {
                                err += "[职务]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("zc") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, zcList);
                            if (xbm == null) {
                                err += "[职称]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("bzlbm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, zglbList);
                            if (xbm == null) {
                                err += "[教职工类别]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("srkm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, rjkmList);
                            if (xbm == null) {
                                err += "[所任科目]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("xlm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, whcdList);
                            if (xbm == null) {
                                err += "[文化程度]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if (arr[3][i].equalsIgnoreCase("xwm") ) {
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            String xbm = XmlUtil.getCodeByValue(cellVal, zgxwList);
                            if (xbm == null) {
                                err += "[最高学位]输入错误";
                            } else {
                                ((String[]) datas.get(j))[i] = xbm;
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("gzny")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证日期格式是否合法
                            JSONObject obj = checkIsStrDateMonth(cellVal);
                            if (obj.getBooleanValue("ckRs")) {
                                ((String[]) datas.get(j))[i] = obj.getString("date");
                            } else {
                                err += "[参加工作年月]格式有误！";
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("cjny")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证日期格式是否合法
                            JSONObject obj = checkIsStrDateMonth(cellVal);
                            if (obj.getBooleanValue("ckRs")) {
                                ((String[]) datas.get(j))[i] = obj.getString("date");
                            } else {
                                err += "[从教工作年月]格式有误！";
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("rxny")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            // 验证日期格式是否合法
                            JSONObject obj = checkIsStrDate(cellVal);
                            if (obj.getBooleanValue("ckRs")) {
                                ((String[]) datas.get(j))[i] = obj.getString("date");
                            } else {
                                err += "[来校年月]格式有误！";
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("lxdh")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            if(cellVal.length()>29){
                                err += "[联系电话]长度超出范围！";
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("yzbm")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            if(cellVal.length()>6){
                                err += "[邮政编码]长度超出范围！";
                            }
                        }
                    }else if(arr[3][i].equalsIgnoreCase("dzxx")){
                        String cellVal = cell[i];
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            ((String[]) datas.get(j))[i] = null;
                        } else {
                            if(cellVal.length()>29){
                                err += "[电子信箱]长度超出范围！";
                            }
                        }
                    }
                    if (err.length() > 0) {
                        wmsg.put("err", err);
//                        wrongMsg.add(wmsg);
                        if(wmsgMap!=null&&wmsgMap.containsKey(j)){
                            JSONObject msg = wmsgMap.get(j);
                            String err0 = msg.getString("err"); 
                            err = err0+err;
                            msg.put("err", err);
                            wmsgMap.put(j, msg);
                        }else{
                            wmsgMap.put(j, wmsg);
                        }
                    }
                }
            }
            // progNum = i + 6;
        }
        for (int i = 1; i < datas.size(); i++) {
            JSONObject d = new JSONObject();
            for (int j = 0; j < arr[0].length; j++) {
                int isIn = Integer.parseInt(arr[1][j]);
                if (isIn != -1) {

                    // 放入数据 （excel映射json）
                    String[] cell = (String[]) datas.get(i);
                    String cellVal = cell[j];
                    d.put(arr[3][j], cellVal);
                }
            }
            exDatas.add(d);
        }
        if (!wmsgMap.keySet().isEmpty()) {
            for(Iterator   it   =   wmsgMap.keySet().iterator();   it.hasNext();   )   { 
                int key = (int) it.next();
                Object value = wmsgMap.get(key);
                wrongMsg.add(value);
            }
            
            rs.put("ckRs", false);
        } else {
            rs.put("ckRs", true);
        }
        rs.put("wrongMsg", wrongMsg);
        rs.put("exDatas", exDatas);
        return rs;
    }

    /**
     * 根据性别名 获取性别码
     * @param cellVal
     * @param xbList
     * @return
     */
    private String getCodeByXBM(String cellVal, List<TDmXb> xbList) {
        // TODO Auto-generated method stub
        for (int i = 0; i < xbList.size(); i++) {
            if (xbList.get(i).getMc().equalsIgnoreCase(cellVal)) {
                return xbList.get(i).getDm();
            }
        }
        return null;
    }

    /**
     * 校验输入的日期是否为时间格式
     * 
     * @param cellVal
     * @return
     */
    private JSONObject checkIsStrDate(String cellVal) {
        // TODO Auto-generated method stub
        JSONObject obj = new JSONObject();
        boolean ckRs = true;
        Date date = null;
        try {
            date = DateUtil.parseDateDayFormat(cellVal);
        } catch (Exception e) {
            try {
                date = DateUtil.parseDateFormat(cellVal, "yyyy/MM/dd");
            } catch (Exception e2) {
                cellVal = trimToDate(cellVal);
                date = DateUtil.parseDateFormat(cellVal, "yyyy年MM月dd");
            }
        }
        if (date == null) {
            obj.put("ckRs", false);
        } else {
            obj.put("ckRs", true);
            obj.put("date", DateUtil.getDateDayFormat(date));
        }
        return obj;
    }
    /**
     * 校验输入的日期是否为时间格式
     * 
     * @param cellVal
     * @return
     */
    private JSONObject checkIsStrDateMonth(String cellVal) {
        // TODO Auto-generated method stub
        JSONObject obj = new JSONObject();
        boolean ckRs = true;
        Date date = null;
        try {
            date = DateUtil.parseDateFormat(cellVal, "yyyy-MM");
        } catch (Exception e) {
            try {
                date = DateUtil.parseDateFormat(cellVal, "yyyy/MM");
            } catch (Exception e2) {
                cellVal = trimToDate(cellVal);
                date = DateUtil.parseDateFormat(cellVal, "yyyy年MM月");
            }
        }
        if (date == null) {
            obj.put("ckRs", false);
        } else {
            obj.put("ckRs", true);
            obj.put("date", DateUtil.parseDateFormat(cellVal, "yyyy-MM"));
        }
        return obj;
    }
    private String trimToDate(String cellVal) {
        // TODO Auto-generated method stub
        String val = "";
        return null;
    }

    /**
     * 校验数据库，若校验通过则
     * 将校验后的数据插入数据库
     * 
     * @param grade
     * @param imType
     * @param jsonArray
     * @return
     */
    private JSONObject importStuToDB(JSONArray arr,StartImportTaskParam sp) {
        int imType = sp.getImportType();
        String grade = sp.getGrade();
        String pycc = sp.getPycc();
        String xq = sp.getXq();
        String xn = sp.getXn();
        String xxdm = sp.getXxdm();
        String nj = sp.getNj();
        String rxnj = sp.getRxnd();
        HashMap map = new HashMap();
        map.put("xxdm", xxdm);
        JSONObject users = teaService.getAllTeaBySchoolNum(map);
        // TODO Auto-generated method stub
        List<THrTeacher> needInsert = new ArrayList<THrTeacher>(); //需要新增的教师列表
        HashMap<String, String> gh = (HashMap<String, String>) users.get("ghMap");  //学号与教师代码(工号)映射表
        HashMap<String, String> sfzh = (HashMap<String, String>) users.get("sfzhMap");  //学号与教师代码(工号)映射表
        HashMap<String, Integer> ghNum = (HashMap<String, Integer>) users.get("ghNumMap");  //教师代码(工号)与其数量映射表
        long t0 = (new Date()).getTime();
        JSONArray wrongMsg = new JSONArray();
        // 判断哪些需要更新 哪些需要插入
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            obj.put("xm", obj.getString("xm").replaceAll(" ", ""));
            JSONObject wmsg = new JSONObject();
            String err = "";
            wmsg.put("row", (i + 2));
            wmsg.put("xm", obj.getString("xm"));
            wmsg.put("jsdm", obj.getString("jsdm"));
            String usfzh = obj.getString("sfzh"); //即将导入的身份证号
            String jsdm =  obj.getString("jsdm");
            THrTeacher stu = JSON.parseObject(obj.toJSONString(), THrTeacher.class);
            boolean isUpdateStu = false;    //是更新还是新增教师信息 
            if (gh.containsKey(jsdm)) { // 如果教师代码(工号)重复
                int xjhNum = 1;
                if(ghNum.get(jsdm)!=null){
                    xjhNum = ghNum.get(jsdm);
                }
                if (xjhNum > 1) {
                    err += "[工号]在系统中已使用两次以上！";
                } else {
                    stu.setZgh(gh.get(jsdm));
                    isUpdateStu = true;
                }
            } 
            
            if (sfzh.containsKey(usfzh)&&usfzh!=null) {
                // 如果身份证号发生重复 取出该身份证号对应的数据库记录 判断教师代码(工号)是否与当前记录相等
                String cGh = sfzh.get(usfzh);
                if (!cGh.equalsIgnoreCase(jsdm)) {
                    err += "[身份证号]在系统中重复！";
                }
            } 
            if(!isUpdateStu){
                stu.setZgh(UUIDUtil.getUUID());
            }
            stu.setXxdm(xxdm);
            needInsert.add(stu);
            if (err.length() > 0) {
                wmsg.put("err", err);
                wrongMsg.add(wmsg);
            }
        }
        long t1 = (new Date()).getTime();
        System.out.println("数据库验证耗时：" + (t1 - t0) + "ms");
        try {
            long dd0 = (new Date()).getTime();
            if(wrongMsg.size()==0){
                
                if (needInsert.size() > 0) {
                    //需插入的教师列表
                    teaService.insertTeaList(needInsert);
                }
                
            }
            long dd2 = (new Date()).getTime();
            System.out.println("数据库语句耗时：" + (dd2 - dd0) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject rs = new JSONObject();
        if (wrongMsg != null && wrongMsg.size() > 0) {
            rs.put("ckRs", false);
            rs.put("wrongMsg", wrongMsg);
        } else {
            rs.put("ckRs", true);
        }
        return rs;
    }

    /**
     * 获取导入进度
     * 
     * @return
     */
    @RequestMapping(value = "/importProgress")
    @ResponseBody
    public JSONObject importProgress(HttpServletRequest req, HttpServletResponse res) {
        TUcUser tcUser = (TUcUser) req.getSession().getAttribute("user");
        JSONObject obj = progressMap.get(tcUser.getUsersysid());
        // obj.put("code", progCode);
        // JSONObject data = new JSONObject();
        // obj.put("data", data);
        // data.put("progress", progNum);
        // data.put("msg", progName);
        JSONObject rs = new JSONObject();
        rs.put("code", obj.get("code"));
        rs.put("data", obj.get("data"));
        rs.put("progress", obj.get("progress"));
        rs.put("msg", obj.get("msg"));
        return rs;
    }

    class SubProcess extends Thread {
        private String processId;

        public SubProcess(String processId) {
            this.processId = processId;
        }

        @Override
        public void run() {
            long t1 = (new Date()).getTime();
            // excel导入处理开始
            StartImportTaskParam sp = (StartImportTaskParam) progressMap.get(processId).get("taskParam");
            String impFrc = tempFileMap.get(processId);
            int isMatch = sp.getIsMatch();
            JSONArray mrs = sp.getMatchResult();

            Workbook workbook = null;
            JSONObject toFront = progressMap.get(processId);
            JSONObject data = new JSONObject();
            toFront.put("code", 1);
            toFront.put("data", data);
            try {
                // 解析excel 封装对象
                if(impFrc.endsWith("xls")){
                    workbook = new HSSFWorkbook(new FileInputStream(new File(impFrc)));
                }else if(impFrc.endsWith("xlsx")){
                    workbook = new XSSFWorkbook(new FileInputStream(new File(impFrc)));
                }else{
                    data.put("progress", 0);
                    data.put("msg", "文件格式不正确！");
                    toFront.put("code", -1);
                    progressMap.put(processId, toFront);
                }
                Sheet sheet = workbook.getSheetAt(0);
                int rows = sheet.getPhysicalNumberOfRows();
                // 转换器 一般poi取数字格式需转换
                DecimalFormat df = new DecimalFormat("0");
                if (rows > 0) {
                    int cols = sheet.getRow(0).getPhysicalNumberOfCells();
                    JSONArray datas = new JSONArray();
                    for (int i = 0; i < rows; i++) {
                    	if(sheet.getRow(i)==null){
                        	continue;
                        }
                    	boolean isTrueNull = true;
                        String[] temp = new String[cols];
                        for (int j = 0; j < cols; j++) {
                            if ( sheet.getRow(i).getCell(j) != null&&sheet.getRow(i).getCell(j).getCellType()!=HSSFCell.CELL_TYPE_BLANK) {
                                Cell cell = sheet.getRow(i).getCell(j);
                                switch (cell.getCellType()) {
                                    case HSSFCell.CELL_TYPE_NUMERIC:
                                        temp[j] = df.format(cell.getNumericCellValue());
                                        break;
                                    case HSSFCell.CELL_TYPE_STRING:
                                        temp[j] = cell.getRichStringCellValue().getString().trim();
                                        break;
                                    case HSSFCell.CELL_TYPE_FORMULA:
                                        temp[j] = cell.getCellFormula();
                                        break;
                                }
                                isTrueNull = false;
                            } else {
                                temp[j] = "";
                            }
                        }
                        if(!isTrueNull){
                        	
                        	datas.add(temp);
                        }
                    }
                    // 开始校验
                    // toFront.put("code", value);
                    data.put("progress", 5);
                    data.put("msg", "正在校验excel数据");
                    progressMap.put(processId, toFront);
                    JSONObject ckrs = checkImpData(datas, mrs, isMatch);
                    // 测试代码
                    System.out.println("查询结果:" + ckrs);
                    if (ckrs.getBooleanValue("ckRs")) {
                        // 开始入库
                        data.put("progress", 30);
                        data.put("msg", "正在校验系统数据");
                        progressMap.put(processId, toFront);

                        JSONObject dbInRs = importStuToDB(ckrs.getJSONArray("exDatas"),sp);
                        if (dbInRs.getBooleanValue("ckRs")) {
                            toFront.put("code", 2);
                            data.put("progress", 100);
                            data.put("msg", "导入成功，共计导入"+(datas.size()-1)+"条教师数据！");
                            progressMap.put(processId, toFront);
                        } else {
                            toFront.put("code", -2);
                            data.put("progress", 100);
                            data.put("msg", "系统数据校验不通过!");
                            JSONObject validateMsg = new JSONObject();
                            validateMsg.put("total", dbInRs.getJSONArray("wrongMsg").size());
                            validateMsg.put("rows", dbInRs.getJSONArray("wrongMsg"));
                            data.put("validateMsg", validateMsg);
                            progressMap.put(processId, toFront);
                        }
                    } else {
                        toFront.put("code", -2);
                        data.put("progress", 100);
                        data.put("msg", "Excel数据校验不通过!");
                        JSONObject validateMsg = new JSONObject();
                        validateMsg.put("total", ckrs.getJSONArray("wrongMsg").size());
                        validateMsg.put("rows", ckrs.getJSONArray("wrongMsg"));
                        data.put("validateMsg", validateMsg);
                        progressMap.put(processId, toFront);
                    }
                }
                workbook.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                // progCode = -1;
                // progName = "服务器出错，上传的文件不存在！";
                toFront.put("code", -1);
                data.put("progress", 100);
                data.put("msg", "服务器出错，上传的文件不存在!");
                progressMap.put(processId, toFront);
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                toFront.put("code", -1);
                data.put("progress", 100);
                data.put("msg", "解析excel时出错，可能为文件格式问题!");
                progressMap.put(processId, toFront);
                e.printStackTrace();
            }
            // excel导入处理结束
            long t2 = (new Date()).getTime();
            System.out.println("导入子线程结束,耗时：" + (t2 - t1));
            System.out.println("开始删除临时excel");
            File del = new File(impFrc);
            del.delete();
        }
    }

}
