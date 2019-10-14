package com.talkweb.common.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.talkweb.common.tools.ExcelTool;
/**
 * @ClassName: ExcelModuleAction.java	
 * @version:1.0
 * @Description: excel模版工具类--公共下载等处理
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
@Controller
@RequestMapping("/common/excel")
public class ExcelModuleAction {

	private Logger logger = LoggerFactory.getLogger(ExcelModuleAction.class);
    /**
     * 公共文件下载
     * @throws Exception 
     */
    @RequestMapping(value="/downloadExcel")
    @ResponseBody   
    public void downExcel(HttpServletRequest request,HttpServletResponse response) throws Exception{
        response.setContentType("text/html;charset=utf-8");   
        request.setCharacterEncoding("UTF-8");   
        java.io.BufferedInputStream bis = null;   
        java.io.BufferedOutputStream bos = null;   
  
        String ctxPath = request.getSession().getServletContext().getRealPath(   
                "/")   
                + "\\" + "data\\excelModule\\";   
        logger.debug("ctxPath:"+ctxPath);
        //入参srcNameCHN即可生成该文件名 即下载时文件名
        String fileName = URLDecoder.decode(request.getParameter("srcNameCHN"),"UTF-8");
        String downLoadPath = ctxPath + fileName;  
//        String downLoadPath = "d://excle.xls";
        logger.debug(downLoadPath);   
        try {   
            long fileLength = new File(downLoadPath).length();   
//            if (request.getHeader("User-Agent").toUpperCase().indexOf("MSIE") > 0) {
//                fileName = URLEncoder.encode(fileName, "UTF-8");
//            } else {
//                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
//            }
//            response.setContentType("application/x-msdownload;");   
//            response.setHeader("Content-disposition", "attachment; filename="  
//                    + fileName);   
//            response.setHeader("Content-Length", String.valueOf(fileLength));   
            response.setContentType("octets/stream");
            response.addHeader("Content-Type", "text/html; charset=utf-8");  
            String downLoadName = new String(fileName.getBytes("gbk"), "iso8859-1"); 
            response.addHeader("Content-Disposition", "attachment;filename="  
                    + downLoadName); 
            bis = new BufferedInputStream(new FileInputStream(downLoadPath));   
            bos = new BufferedOutputStream(response.getOutputStream());   
            byte[] buff = new byte[2048];   
            int bytesRead;   
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {   
                bos.write(buff, 0, bytesRead);   
            }   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            if (bis != null)   
                bis.close();   
            if (bos != null)   
                bos.close();   
        }   
    }
    
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/exportExcelByData")
    @ResponseBody  
    public void demoForDownExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{
        
        /**excelHead 前台传过来的datagrid表头
         * 示例3 用于合并表头
         */
//        String datas = req.getParameter("data");
//        System.out.println("前台参数："+datas);
//        JSONArray excelHead = JSON.parseArray("[[{field:'itemid',title:'Item ID',rowspan:3,width:80,sortable:true},{field:'productid',title:'Product ID',rowspan:2,colspan:2,width:80,sortable:true},{title:'Item Details',colspan:6}],"
//                + "[{field:'listprice',title:'List Price',colspan:2,width:80,align:'right',sortable:true},{field:'unitcost',colspan:2,title:'Unit Cost',width:80,align:'right',sortable:true},{field:'attr1',title:'Attribute',width:100},{field:'status',title:'Status',width:60}]"
//                + ",["
//                + "{field:'star3',title:'star3',width:80,align:'right',sortable:true},"
//                + "{field:'start3',title:'start3',width:80,align:'right',sortable:true},"
//                + "{field:'listprice1',title:'listprice1',width:100},"
//                + "{field:'listp2',title:'listp2',width:100},"
//                + "{field:'u1',title:'u1',width:100},"
//                + "{field:'u2',title:'u2',width:100},"
//                + "{field:'a1',title:'a1',width:100},"
//                + "{field:'s2',title:'s2',width:100}"
//                + "]"
//                + "]");
//        //封装数据 json格式 键名需与表头中的field字段一致
//        JSONArray data = new JSONArray();
//        JSONObject obj = new JSONObject();
//        data.add(obj);
//        obj.put("itemid", "abc");
//        obj = new JSONObject();
//        data.add(obj);
//        obj.put("itemid", "abc");
//        obj = new JSONObject();
//        data.add(obj);
//        String[] needMerg = {"itemid"};
//        ExcelTool.exportExcelWithData(data , excelHead,"导出文件", needMerg, req, res);
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportExcelWithData(excelData , excelHeads,fileName, null, req, res);
    }
    /**
     * 公共下载HTML表格方法使用示例
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/exportExcelComplexData")
    @ResponseBody  
    public void exportExcelComplexData(HttpServletRequest req,HttpServletResponse res) throws Exception{
    	logger.info("exportExcelComplexData 进入复杂导出功能！");
        JSONArray excelHeads = JSONArray.parseArray(req.getParameter("excelHead"));
        JSONArray excelData =  JSONArray.parseArray(req.getParameter("excelData"));
        String fileName  = req.getParameter("fileName");
        List<JSONArray> jsonHeadList = new ArrayList<JSONArray>();
        for(Object jsonArr : excelHeads){
        	jsonHeadList.add((JSONArray)jsonArr);
        }
        List<JSONArray> jsonDataList = new ArrayList<JSONArray>();
        for(Object jsonArr : excelData){
        	jsonDataList.add((JSONArray)jsonArr);
        }
        ExcelTool.exportComplexHeadExcelWithData(jsonDataList , jsonHeadList,fileName, null, req, res);
        logger.info("exportExcelComplexData 导出结束！");
    }

    
    /**
     * 导出两个表头的表格为excel
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value="/exportTwoTableExcelByData")
    @ResponseBody  
    public void downMultiTableExcelByHTMLTable(HttpServletRequest req,HttpServletResponse res) throws Exception{
        JSONArray firstExcelHeads = JSONArray.parseArray(req.getParameter("firstExcelHead"));
        JSONArray firstExcelData =  JSONArray.parseArray(req.getParameter("firstExcelData"));
        JSONArray secondExcelHeads = JSONArray.parseArray(req.getParameter("secondExcelHead"));
        JSONArray secondExcelData =  JSONArray.parseArray(req.getParameter("secondExcelData"));
        String fileName  = req.getParameter("fileName");
        ExcelTool.exportTwoHeadExcelWithData(firstExcelData,firstExcelHeads,secondExcelData,secondExcelHeads,fileName, null, req, res);
    }
}
