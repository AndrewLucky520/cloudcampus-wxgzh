package com.talkweb.jasperReport.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.UUIDUtil;


/**
 * @ClassName: ExcelTool.java
 * @version:1.0
 * @Description: excel 
 * @author 智慧校
 * @date 2016年8月28日
 */
public class ExcelExportTool {
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelExportTool.class);

    /**
     * @param data 要导出的数据
     * @param excelHead 表头 datagrid组件生成
     * @param fileName 文件名
     * @param needMergDataField 需要合并的列（值相等则合并）
     * @param req
     * @param res
     */
    public static void exportMoreExcelWithData(List<JSONObject> excelData, String fileName,
    		HttpServletRequest req, HttpServletResponse res, boolean show) {
    	
    	Workbook workbook = new SXSSFWorkbook(1000);// 产生工作薄对象
    	  // 设置表头
        Sheet sheet = workbook.createSheet();
        //定义变量字体大小，对齐方式等样式
   	    XSSFStyleTool.setBorderStyle(workbook, show);
        int row = 0;
        for(int i = 0; i < excelData.size() ; i++){
			JSONObject excelTile = excelData.get(i).getJSONObject("excelTitle");
			JSONObject pageHead = excelData.get(i).getJSONObject("pageHead");
			JSONArray tableHead = excelData.get(i).getJSONArray("tableHead");
			JSONArray tableData = excelData.get(i).getJSONArray("tableData");
			JSONArray execlTail = excelData.get(i).getJSONArray("excelTail");
			if (tableHead.size() > 0 && tableData.size() > 0) {
				row = writeDataToExcel(row, workbook, sheet, excelTile, pageHead, tableHead, tableData, execlTail, show);
			}
        }       
        String xls = UUIDUtil.getUUID();
        File temp = new File(xls + ".xlsx");
        OutputStream out = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        // 写入数据结束
        try{
            if (!temp.exists()) {
                temp.createNewFile();
            }
            out = new FileOutputStream(temp);
            out.flush();
            workbook.write(out);
            out.close();
            res.setContentType("octets/stream");
            res.addHeader("Content-Type", "text/html; charset=utf-8");  
            fileName += ".xlsx";
            String downLoadName = new String(fileName.getBytes("gbk"), "iso8859-1"); 
            res.addHeader("Content-Disposition", "attachment;filename="  
                    + downLoadName); 
            bis = new BufferedInputStream(new FileInputStream(temp));
            bos = new BufferedOutputStream(res.getOutputStream());
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                   bos.write(buff, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("exportExcelWithData:error{}",e);
        } finally {
            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (bos != null)
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            temp.delete();
            if(null != workbook)
            {
            	try {
					 workbook.close();
				} catch (IOException e) {
					 e.printStackTrace();
				}
            }
        }
    }
    
    /****
     * 写入指定数据到表格对象中
     * @param fromRow 其实行数
     * @param show 
     * @param workbook 表格对象
     * @param sheet 
     * @param pageHead 
     * @param excelHead 
     * @param tableHead
     * @param data
     * @param excelTail
     * @return
     */
    private static int writeDataToExcel(int fromRow,Workbook workbook,Sheet sheet,
    		JSONObject excelHead, JSONObject pageHead, JSONArray tableHead, JSONArray data, 
    		JSONArray excelTail, boolean show)
    {    	 
	     //开始行编号+间隔行数
    	 int titleRowNo = fromRow,mixWidth = 80; 
         //创建表头行,并且把数据填充到标题中
         Row trow = sheet.createRow(titleRowNo);
         String bigTitle = null;
         if(excelHead.containsKey("title")){
        	 bigTitle = excelHead.getString("title");
         }
         int tcolspan = 1;
         if (excelHead.containsKey("colspan")) {
        	 tcolspan = excelHead.getIntValue("colspan");
         }
         Cell tcell = trow.createCell(0);
         tcell.setCellValue(bigTitle);
         tcell.setCellStyle(XSSFStyleTool.bTStyle);
         sheet.setColumnWidth(0, mixWidth * 40);
         // 跨列合并
         if (tcolspan > 1) {
             CellRangeAddress reg = new CellRangeAddress((short) (titleRowNo), (short) (titleRowNo),(short) 0,  (short) (tcolspan-1));
             sheet.addMergedRegion(reg);
         }	                                 
         //开始行编号+间隔行数
    	 int pageRowNo = titleRowNo; 
         //创建表头行,并且把数据填充到标题中
    	 if (null != pageHead){
    		 pageRowNo = titleRowNo + 1;
    		 Row prow = sheet.createRow(pageRowNo);
             String pageTitle = null;
             if(pageHead.containsKey("title")){
            	 pageTitle = pageHead.getString("title");
             }
             int pcolspan = 1;
             if (pageHead.containsKey("colspan")) {
            	 pcolspan = pageHead.getIntValue("colspan");
             }
             Cell pcell = prow.createCell(0);
             pcell.setCellValue(pageTitle);
             pcell.setCellStyle(XSSFStyleTool.bWStyle);
             sheet.setColumnWidth(0, mixWidth * 40);
             // 跨列合并
             if (pcolspan > 1) {
                 CellRangeAddress reg = new CellRangeAddress((short) (pageRowNo), (short) (pageRowNo),(short) 0,  (short) (pcolspan-1));
                 sheet.addMergedRegion(reg);
             }		   	 
    	 }  
         /**创建表头行，填充表头数据 */ 
         int headRowSize = tableHead.size(); //计算表头行数
         int colNum = 0;   
         JSONArray Line0 = tableHead.getJSONArray(0);
         for(int j = 0; j < Line0.size(); j++) {
             JSONObject d0 = Line0.getJSONObject(j);
             int colspan = 1;
             if (d0.containsKey("colspan")) {
                 colspan = d0.getIntValue("colspan");
             }
             colNum = colNum + colspan;
         }      
         int headRowNo = pageRowNo + 1;//开始行编号+间隔行数 
         //3. 创建表头行,并且把数据填充到表头单元中
         for (int i = 0; i < headRowSize; i++) {
              sheet.createRow(headRowNo+i);
         }
         Hashtable<String, String> headb = new Hashtable<String, String>(); // 被占用矩阵
         HashMap<Integer,String> headIndexMap= new  HashMap<Integer,String>();   
         for(int i =0; i < headRowSize; i++) 
         {//循环每行设置内容 及合并
            Row row = sheet.getRow(headRowNo+i);
            JSONArray Line = tableHead.getJSONArray(i);        
            for(int j = 0; j < Line.size(); j++) 
            {
                JSONObject d = Line.getJSONObject(j);
                String title = null;
                if(d.containsKey("title")){
                    title = d.getString("title");
                }
                if (d.containsKey("width")) {
                	mixWidth = d.getIntValue("width");
                }
                int rowspan = 1;
                int colspan = 1;
                if (d.containsKey("colspan")) {
                    colspan = d.getIntValue("colspan");
                }
                String field = null;
                if (d.containsKey("field")) {
                    field = d.getString("field");
                }
                int index = 0;
                if (!headb.isEmpty()) {
                    for (int c = 0; c < colNum; c++) {
                        String key = i + "," + c;
                        if (!headb.containsKey(key)) {
                            index = c;break;
                        }
                    }
                }
                Cell cell = row.createCell(index);
                cell.setCellValue(title);
                cell.setCellStyle(XSSFStyleTool.bDStyle);
                sheet.setColumnWidth(index, mixWidth * 40);
                headIndexMap.put(index, field);//存入表头所在的列
                for (int l = i; l < rowspan + i; l++) {
                    for (int c = index; c < colspan + index; c++) {
                    	 headb.put(l + "," + c, "1");
                    }
                }
                // 跨行合并
                /**
                if (rowspan > 1) {
                    CellRangeAddress reg = new CellRangeAddress((short) (i+headRowNo), (short) (i + rowspan - 1),(short) index,  (short) index);
                    sheet.addMergedRegion(reg);                                        
                } */
                // 跨列合并
                if (colspan > 1) {
                	CellRangeAddress reg = new CellRangeAddress((short) (i+headRowNo), (short) (i+headRowNo),(short) index,  (short) (index + colspan - 1));
                    sheet.addMergedRegion(reg);
                    XSSFStyleTool.setRegFrameLine(workbook, sheet, reg);
                }              
            }
          }  
          /**创建数据行，填充数据 */
          int sheetSize = data.size();
          // 封装表头结束 开始写入数据
          double sheetNo = Math.ceil(data.size()/ sheetSize);// 取出一共有多少个sheet.
		  for(int index = 0; index <= sheetNo; index++) {
			  Row row;
			  Cell cell;// 产生单元格
			  List<Integer> rowList = new ArrayList<Integer>();
			  int startNo = index * sheetSize + headRowSize + headRowNo;
			  int endNo = Math.min(startNo + sheetSize, headRowSize + headRowNo
					+ data.size());
			  // 写入各条记录,每条记录对应excel表中的一行
			  boolean isCombineData = true;
			  for (int i = startNo; i < endNo; i++) {
				   row = sheet.createRow(i);
				   JSONObject vo = data.getJSONObject(i
						- (headRowSize + headRowNo)); // 得到导出对象.
				   for (int j = 0; j < colNum; j++) {
					    try {
					         cell = row.createCell(j);// 创建cell			         
							 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
							 // 如果数据存在就填入,不存在填入空格.
					         String value = vo.getString(headIndexMap.get(j));
					         if (StringUtils.isNotEmpty(value)){
					        	 value = String.valueOf(vo.getString(headIndexMap
											.get(j)));
					        	 if (j == 0 && value.indexOf("NEEDCOMBINE")> -1 ) {
					        		 rowList.add(row.getRowNum());
					        		 value = value.substring(value.indexOf("NEEDCOMBINE") + 11 );
					        		 isCombineData = false;
								 }
					         }else{
					        	 value = "";
					        	 if(j == 0)rowList.add(row.getRowNum());
					         }
							 cell.setCellValue(value);
							 cell.setCellStyle(XSSFStyleTool.contentStyle);
					    } catch (IllegalArgumentException e) {
						    e.printStackTrace();
					    }
				   }
		 	  }
			  // 跨行合并
			  if (isCombineData) {
				  for(int k = 0;k < rowList.size(); k++){
					  CellRangeAddress reg = new CellRangeAddress(rowList.get(k)-1, rowList.get(k), 0, 0);				 
					  sheet.getRow(rowList.get(k)-1).getCell(0).setCellStyle(XSSFStyleTool.regionStyle);
		              sheet.addMergedRegion(reg);          
				  }	
			  }
			  for(int l = 0;l < rowList.size(); l++){
				  for(int h = 0;h < colNum; h ++){
					  CellRangeAddress reg = new CellRangeAddress(rowList.get(l)-1, rowList.get(l), h, h);				 
					  XSSFStyleTool.setRegFrameLine(workbook, sheet, reg); 					  
				  }
			  }	
		  }
		  /**创建表尾行，填充表尾数据 */  
		  int tailRowSize= excelTail.size();
		  int tailRowNo = 0;
		  if (tailRowSize > 0){
			  //开始行编号+间隔行数
			  tailRowNo = headRowNo + headRowSize + sheetSize; 
		      //3. 创建表头行,并且把数据填充到表头单元中
	          for(int i = 0; i < tailRowSize; i++) {
	              sheet.createRow(tailRowNo+i);
	          }
	          for(int i =0; i < tailRowSize; i++){
		          Row row = sheet.getRow(tailRowNo+i);
		          JSONArray Line = excelTail.getJSONArray(i);        
		          for(int j = 0; j < Line.size(); j++){
		              JSONObject d = Line.getJSONObject(j);
		              String title = null;
		              if(d.containsKey("title")){
		                 title = d.getString("title");
		              }
		              int colspan = 1;
		              if (d.containsKey("colspan")) {
		                  colspan = d.getIntValue("colspan");
		              }
		              Cell cell = row.createCell(0);
	                  cell.setCellValue(title);
	                  cell.setCellStyle(XSSFStyleTool.bWStyle);
	                  sheet.setColumnWidth(0, mixWidth * 40);
	                  // 跨列合并
	                  if (colspan > 1) {
	                      CellRangeAddress reg = new CellRangeAddress((short) (i+tailRowNo), (short) (i+tailRowNo),(short) 0,  (short) (colspan-1));
	                      sheet.addMergedRegion(reg);
	                  }	                               
		          }
	          }  
		  }
	      int lastRowNo = tailRowNo + tailRowSize;
          //6.返回最后一行的行号
          return lastRowNo;
    }
   
}