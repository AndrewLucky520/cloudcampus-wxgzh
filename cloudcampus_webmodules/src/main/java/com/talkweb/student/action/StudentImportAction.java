package com.talkweb.student.action;

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
import com.talkweb.commondata.domain.TEdTerminfo;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.datadictionary.dao.DataDictionaryDao;
import com.talkweb.datadictionary.domain.TDmXb;
import com.talkweb.student.domain.business.TSsClass;
import com.talkweb.student.domain.business.TSsClassenrol;
import com.talkweb.student.domain.business.TSsStudenrol;
import com.talkweb.student.domain.business.TSsStudent;
import com.talkweb.student.domain.page.StartImportTaskParam;
import com.talkweb.student.service.StudentImportService;
import com.talkweb.systemManager.domain.business.TTrSchool;
import com.talkweb.systemManager.domain.business.TUcUser;

/**
 * 
 * @ClassName: StudentImportAction.java
 * @version:1.0
 * @Description: 学生导入管理
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
@Controller
@RequestMapping("/student/import")
public class StudentImportAction {

    @Autowired
    private StudentImportService studentService;
    @Autowired
    private DataDictionaryDao dataDictionaryDao;
    
    @Autowired
    private AllCommonDataService commonDataService ;
    /**
     * 获取临时文件保存目录
     */
    @Value("#{settings['tempFilePath']}")
    private String tempFilePath;

    /**
     * 导入学生excel的表头--系统字段
     */
    private static final String[] stuTitle = { "姓名", "班级名称", "学籍号", "学号", "身份证号码", "性别", "出生日期", "家长手机号" };

    /**
     * 导入学生excel的表头--系统字段是否必填
     */
    private static final int[] stuTitleNeed = { 1, 1, 1, 0, 0, 0, 0, 0 };
    /**
     * 导入学生excel的表头--系统字段字段名
     */
    private static final String[] stuTitleName = { "xm", "bjmc", "xjh", "userXh", "sfzh", "xbm", "csrq", "jfrlxdh" };
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
     * 子线程是否在跑
     */
    // private static int subProSta = 0;

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
        stt.setGrade(req.getParameter("grade"));
        stt.setImportType(Integer.parseInt(req.getParameter("importType")));
        stt.setIsMatch(Integer.parseInt(req.getParameter("isMatch")));
        stt.setMatchResult(JSON.parseArray(req.getParameter("matchResult")));
        
        JSONObject obj = new JSONObject();
        JSONObject data = new JSONObject();
        TUcUser tcUser = (TUcUser) req.getSession().getAttribute("user");
        TTrSchool tcSchool = (TTrSchool) req.getSession().getAttribute("school");
        TEdTerminfo ti = (TEdTerminfo) req.getSession().getAttribute("currentXNXQ");
        JSONObject procObj = new JSONObject();
        //  根据使用年级、学年获取入学年度
        String rxnd = commonDataService.ConvertSYNJ2RXND(stt.getGrade(), ti.getXn());
        //  获取培养层次
        String pycc = commonDataService.getPYCCBySYNJ(stt.getGrade(), ti.getXn());
        String nj = (commonDataService.ConvertSYNJ2NJDM(stt.getGrade(), ti.getXn()));
        procObj.put("taskParam", stt);
        stt.setXn(ti.getXn());
        stt.setXq(ti.getXqm());
        stt.setRxnd(rxnd);
        stt.setPycc(pycc);
        stt.setNj(nj);
        stt.setXxdm(tcSchool.getXxdm());
        //设置获取的参数 
        procObj.put("school", tcSchool);
        progressMap.put(tcUser.getUsersysid(), procObj);

        SubProcess sp = new SubProcess(tcUser.getUsersysid());
        sp.start();
        // if (progCode != 1) {
        // final String impFrc = req.getParameter("tempStuImpExcTitle");
        // final String impFrc = "d:\\temp\\test22.xls";
        // Thread t = new Thread(new Runnable( ) {
        // public void run() {
        // System.out.println("导入子线程开始！");
        //
        // }
        //
        // });
        // t.start();
        // } else {
        //
        // obj.put("code", -1);
        // data.put("msg", "学生导入进程正在执行，请稍后处理");
        // }
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
        JSONObject rs = new JSONObject();
        String[] titles = (String[]) datas.get(0);
        // 存储导入excel表头[0] 是否必填[1] 映射的系统字段[2] 映射学生实体字段[3]
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
        // 开始校验 excel表头[0] 是否必填[1] 映射的系统字段[2] 映射学生实体字段[3]
        JSONArray wrongMsg = new JSONArray();
        JSONArray exDatas = new JSONArray();
        HashMap<String, String> xjhMap = new HashMap<String, String>();
        HashMap<String, String> xhMap = new HashMap<String, String>();
        int xmIndex = strIndexInArray("xm", arr[3]);
        int xjhIndex = strIndexInArray("xjh", arr[3]);
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
                        if (arr[3][i].equalsIgnoreCase("xjh")) {
                            // 校验学籍号是否重复
                            if (!xjhMap.isEmpty() && cellVal != null && cellVal.trim().length() > 0) {
                                if (xjhMap.containsKey(cellVal)) {

                                    err += "[学籍号]重复！";

                                } else {
                                    xjhMap.put(cellVal, "exist");
                                }
                            }
                        }
                    } else if (arr[3][i].equalsIgnoreCase("userXh")) {
                        // 非必填验证学号
                        String cellVal = cell[i];
                        String user_xh = "";
                        if (cellVal == null || cellVal.equalsIgnoreCase("") || cellVal.trim().length() == 0) {
                            user_xh = cell[xjhIndex];
                            ((String[]) datas.get(j))[i] = user_xh;
                        }
                        // 校验学号是否重复
                        if (!xhMap.isEmpty()) {
                            if (xhMap.containsKey(user_xh)) {

                                err += "[学号]重复！";

                            } else {
                                xhMap.put(user_xh, "exist");
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
        JSONObject users = studentService.getAllStuXJHBySchoolNum(map);
        // TODO Auto-generated method stub
        List<TSsStudent> needInsert = new ArrayList<TSsStudent>(); //需要新增的学生列表
        List<TSsStudent> needUpdate = new ArrayList<TSsStudent>();  //需要更新的学生列表
        List<TSsStudenrol> needIOUStuEnrol = new ArrayList<TSsStudenrol>(); //需要插入或更新的学生注册表信息
        List<TSsStudenrol> delStuEnrol = new ArrayList<TSsStudenrol>(); //需要删除的学生注册表信息
        List<TSsClass> needInsertClass = new ArrayList<TSsClass>(); //需要插入的班级列表
        List<TSsClassenrol> needInsertClassEnrol = new ArrayList<TSsClassenrol>();  //需要插入的班级注册列表
        HashMap<String, String> xh = (HashMap<String, String>) users.get("xhMap");  //学号与学籍号映射表
        HashMap<String, Integer> xjh = (HashMap<String, Integer>) users.get("xjhMap");  //学籍号与其数量映射表
        HashMap<String, JSONObject> xjhStuInfo = (HashMap<String, JSONObject>) users.get("xjhStuInfo"); //学籍号与学生基本信息映射表
        HashMap<String, String> sfzh = (HashMap<String, String>) users.get("sfzhMap");   //身份证号与学籍号映射
        HashMap<String, String> keyMap = (HashMap<String, String>) users.get("keyMap"); //学籍号与学号映射表
        HashMap<String, String> bjMcDmMap = (HashMap<String, String>) users.get("bjMcdmMap"); //班级名称与班级代码映射表
        HashMap<String, String> bjSynjMap = (HashMap<String, String>) users.get("bjSynjMap");   //班级名称与使用年级映射表
        HashMap<String, String> bjHasInsert = new HashMap<String, String>();    //已进入插入队列的班级队列 班级名称与班级号的映射表
        HashMap<String, JSONObject> bhBjMap = (HashMap<String, JSONObject>) users.get("bhBjMap"); //班级代码与班级基本信息映射表
        // HashMap<String, String> bjMcBhMap = new HashMap<String, String>();
        long t0 = (new Date()).getTime();
        JSONArray wrongMsg = new JSONArray();
        // 判断哪些需要更新 哪些需要插入
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            JSONObject wmsg = new JSONObject();
            String err = "";
            wmsg.put("row", (i + 2));
            wmsg.put("xm", obj.getString("xm"));
            wmsg.put("xjh", obj.getString("xjh"));
            obj.put("xm", obj.getString("xm").replaceAll(" ", ""));
            String uxh = obj.getString("userXh"); // 即将导入的学号
            String uxjh = obj.getString("xjh"); // 即将导入的学籍号
            String usfzh = obj.getString("sfzh"); //即将导入的身份证号
            String bjmc = obj.getString("bjmc"); // 即将导入的班级名称
            String bh = "";
            TSsStudent stu = JSON.parseObject(obj.toJSONString(), TSsStudent.class);
            int xz = 3;
            stu.setPycc(pycc);
            if (pycc.equalsIgnoreCase("1")) {
                xz = 6;
            }
            boolean isUpdateStu = false;    //是更新还是新增学生信息 
            if (xjh.containsKey(uxjh)) { // 如果学籍号重复
                int xjhNum = xjh.get(uxjh);
                if (xjhNum > 1) {
                    err += "[学籍号]在系统中已使用两次以上！";
                } else {
                    stu.setXh(keyMap.get(uxjh));
                    needUpdate.add(stu);
                    isUpdateStu = true;
                }
            } 
            if (uxh!=null&&xh.containsKey(uxh)) {
                // 如果学号发生重复 取出该学号对应的数据库记录 判断学籍号是否与当前记录相等
                String cXjh = xh.get(uxh);
                if (cXjh!=null&&!cXjh.equalsIgnoreCase(uxjh)) {
                    err += "[学号]在系统中重复！";
                }
            } 
            if (usfzh!=null&&sfzh.containsKey(usfzh)) {
                // 如果身份证号发生重复 取出该身份证号对应的数据库记录 判断学籍号是否与当前记录相等
                String cXjh = sfzh.get(usfzh);
                if (cXjh!=null&&!cXjh.equalsIgnoreCase(usfzh)) {
                    err += "[身份证号]在系统中重复！";
                }
            }
            stu.setXxdm(xxdm);
            stu.setSfzx("1");
            if(!isUpdateStu){
                stu.setParentsysid(UUIDUtil.getUUID());
                stu.setXh(UUIDUtil.getUUID());
                stu.setNj(rxnj);
                needInsert.add(stu);
            }
            // 单独判断班级 是否存在于新系统中
            if (bjMcDmMap!=null&&bjMcDmMap.containsKey(bjmc)) {
                if (!bjSynjMap.get(bjmc).toString().equalsIgnoreCase(grade)) {
                    // 班级存在于其它年级
                    err += "[班级名称]已存在于其他年级！";
                } else {
                    stu.setBh(bjMcDmMap.get(bjmc));
                    bh = bjMcDmMap.get(bjmc);
                }
            } else {
                // 班级名称不存在 判断当前导入是否已新增 未新增则新增
                if (bjHasInsert == null || bjHasInsert.size() == 0 || !bjHasInsert.containsKey(bjmc)) {

                    TSsClass cla = new TSsClass();
                    cla.setBh(UUIDUtil.getUUID());
                    stu.setBh(cla.getBh()); //设置学生所属班号
                    cla.setXxdm(xxdm);  
                    cla.setBjmc(bjmc);
                    cla.setUserBh(bjmc);
                    cla.setSynj(grade);
                    cla.setPycc(pycc);
                    cla.setBjlxm("01");
                    cla.setXz(xz);
                    cla.setNj(rxnj);
                    cla.setSfcx("0");
                    needInsertClass.add(cla);
                    bjHasInsert.put(bjmc, cla.getBh());
                    bh = cla.getBh();
                    // 增加班级注册信息 判断是新增班级还是更新班级
                    for (int k = Integer.parseInt(rxnj ); k <= Integer.parseInt(rxnj) + xz - 1; k++){
                        for (int l = 1; l <= 2; l++){
                            TSsClassenrol arg0 = new TSsClassenrol();
                            arg0.setBh(bh);
                            arg0.setBjlxm("01");
                            arg0.setSfcx("0");
                            arg0.setXn(k + "");
                            arg0.setXqm(l + "");
                            arg0.setXxdm(xxdm);
                            needInsertClassEnrol.add(arg0);
                        }
                    }
                } else {
                    // if(bjHasInsert.containsKey(bjmc)){
                    // 获取班级名称映射的班号代码（班级主键）
                    stu.setBh(bjHasInsert.get(bjmc));
                    bh = bjHasInsert.get(bjmc);
                    // }
                }
            }
            String y_synj = null;
            String y_rxnj = null;
            if(xjhStuInfo.containsKey(stu.getXjh())&&xjhStuInfo.get(stu.getXjh()).containsKey("rxnj")){
                y_rxnj = xjhStuInfo.get(stu.getXjh()).getString("rxnj");
            }
            if(y_rxnj!=null&&y_rxnj.trim().length() != 0){
                y_synj = commonDataService.ConvertRXND2SYNJ(y_rxnj, pycc);
            }
            // 开始学生注册部分
            if (isUpdateStu) {
                // 原使用年级为空的
                if (y_synj == null || y_synj.trim().length() == 0) {
                    y_rxnj = rxnj;
                    for (int k = Integer.parseInt(y_rxnj); k <= Integer.parseInt(y_rxnj) + xz - 1; k++) {
                        for (int l = 1; l <= 2; l++) {
                            TSsStudenrol arg0 = new TSsStudenrol();
                            arg0.setBh(bh);
                            arg0.setSfzx("1");
                            arg0.setXh(stu.getXh());
                            arg0.setXn(k + "");
                            arg0.setXqm(l + "");
                            arg0.setXxdm(xxdm);
                            if(xq.equalsIgnoreCase("2")&&arg0.getXn().equalsIgnoreCase(xn)&&l==1){
                            }else{
                                needIOUStuEnrol.add(arg0);
                            }
                        }
                    }
                    stu.setNj(y_rxnj);
                }else{
                 // 原使用年纪不等于新使用年级
                    if (y_synj != null&&!y_synj.equalsIgnoreCase(grade)) {
                        // 毕业的那年
                        int maxxn = Integer.parseInt(y_rxnj) + xz - 1;
                        int inj = Integer.parseInt(y_synj);
                        int itznj = Integer.parseInt(grade);
                        int b = inj - itznj;
                        if (b < 0) {    // 降级增加注册记录

                            for (int k = maxxn + 1; k <= maxxn + Math.abs(b); k++) {
                                for (int l = 1; l <= 2; l++) {
                                    TSsStudenrol arg0 = new TSsStudenrol();
                                    arg0.setBh(bh);
                                    arg0.setSfzx("1");
                                    arg0.setXh(stu.getXh());
                                    arg0.setXn(k + "");
                                    arg0.setXqm(l + "");
                                    arg0.setXxdm(xxdm);
                                    needIOUStuEnrol.add(arg0);
                                }
                            }
                        } else if (b > 0){  // 升级删除多余注册记录
                            if (maxxn - Math.abs(b) >= Integer.parseInt(xn)) {
                                for (int k = maxxn; k >= maxxn - Math.abs(b) + 1; k--) {
                                    for (int l = 1; l <= 2; l++) {
                                        //
                                        TSsStudenrol arg0 = new TSsStudenrol();
                                        arg0.setBh(bh);
                                        arg0.setSfzx("0");
                                        arg0.setXh(stu.getXh());
                                        arg0.setXn(k + "");
                                        arg0.setXqm(l + "");
                                        arg0.setXxdm(xxdm);
                                        delStuEnrol.add(arg0);
                                    }
                                }
                            }
                        }
                    }
                    stu.setNj(rxnj);
                }
                
            } else {
                //新增学生注册信息
                for (int k = Integer.parseInt(rxnj ); k <= Integer.parseInt(rxnj) + xz - 1; k++){
                    for (int l = 1; l <= 2; l++){
                        TSsStudenrol arg0 = new TSsStudenrol();
                        arg0.setBh(bh);
                        arg0.setSfzx("1");
                        arg0.setXh(stu.getXh());
                        arg0.setXn(k + "");
                        arg0.setXqm(l + "");
                        arg0.setXxdm(xxdm);
                        needIOUStuEnrol.add(arg0);
                    }
                }

                stu.setNj(rxnj);
            }
            if (err.length() > 0) {
                wmsg.put("err", err);
                wrongMsg.add(wmsg);
            }
        }
        long t1 = (new Date()).getTime();
        System.out.println("数据库验证耗时：" + (t1 - t0) + "ms");
        try {
            if(wrongMsg.size()==0){
                if (imType != 1) {
                    studentService.updateStuStatusByParam(map);
                }
                long dd0 = (new Date()).getTime();
                if (needInsert.size() > 0) {
                    //需插入的学生列表
                    studentService.insertStuList(needInsert);
                }
                if(needIOUStuEnrol.size()>0){
                    //需插入或更新的学生注册信息
                    studentService.updateStuEnrolList(needIOUStuEnrol);
                }
                long dd1 = (new Date()).getTime();
                System.out.println("插入语句耗时：" + (dd1 - dd0) + "ms");
                if (needUpdate.size() > 0) {
                    //需更新的学生列表
                    studentService.updateStuList(needUpdate);
                }
                if(delStuEnrol.size()>0){
                    //需删除的学生注册列表
                    studentService.deleteStuEnrol(delStuEnrol);
                }
                if(needInsertClass.size()>0){
                    studentService.insertClassList(needInsertClass);
                }
                if(needInsertClassEnrol.size()>0){
                    studentService.insertClassEnrolList(needInsertClassEnrol);
                }
                long dd2 = (new Date()).getTime();
                System.out.println("更新语句耗时：" + (dd2 - dd1) + "ms");
                System.out.println("数据库语句耗时：" + (dd2 - dd0) + "ms");
            }
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
                            data.put("msg", "导入成功，共计导入"+(datas.size()-1)+"条学生数据！");
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
