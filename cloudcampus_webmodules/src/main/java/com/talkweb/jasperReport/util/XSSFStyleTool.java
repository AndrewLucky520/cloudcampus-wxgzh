package com.talkweb.jasperReport.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

public class XSSFStyleTool {
	
	static CellStyle bTStyle;
	
	static CellStyle bZStyle;
	
	static CellStyle bDStyle;
	
	static CellStyle contentStyle;
	
	static CellStyle bWStyle;
	
	static CellStyle regionStyle;
	
	//定义变量字体大小，对齐方式等样式	
	public static void setBorderStyle(Workbook workbook,boolean show){
		
		//---------------------大标题-------------------------
		bTStyle = workbook.createCellStyle();
        Font btFont = workbook.createFont();
        btFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
        btFont.setFontHeightInPoints((short) 15);
        bTStyle.setFont(btFont);
        bTStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //---------------------信息标注-------------------------
        bZStyle = workbook.createCellStyle();
        Font bzFont = workbook.createFont();
        bzFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
        bzFont.setFontHeightInPoints((short) 15);
        bZStyle.setFont(btFont);
        bZStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //---------------------表头-------------------------
        bDStyle = workbook.createCellStyle();
        Font headFont = workbook.createFont();
        headFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
        headFont.setFontHeightInPoints((short) 11);
        bDStyle.setFont(headFont);
        setFrameLine(bDStyle);
        bDStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //---------------------内容------------------------- 	
        contentStyle = workbook.createCellStyle();
        contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中        
        Font contentFont = workbook.createFont();
        contentFont.setFontHeightInPoints((short) 10);
        contentStyle.setFont(contentFont);  
        contentStyle.setFillBackgroundColor(HSSFColor.WHITE.index);
        if(!show)setFrameLine(contentStyle);
        //---------------------底注-------------------------        
        bWStyle = workbook.createCellStyle();
        Font bwFont = workbook.createFont();
        bwFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
        bwFont.setFontHeightInPoints((short) 11);
        bWStyle.setFont(bwFont);
        bWStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        //----------------合并单元格样式-------------------
        regionStyle = workbook.createCellStyle();
        regionStyle.setFont(contentFont);
        regionStyle.setVerticalAlignment((short)1);
        regionStyle.setAlignment((short)2);
        regionStyle.setFillBackgroundColor(HSSFColor.WHITE.index);
	}
	
    // 设置边框
	private static void setFrameLine(CellStyle setBorderBD) {		
         setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);
         setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);
	}
	
    // 设置合并行的边框
    public static void setRegFrameLine(Workbook workbook, Sheet sheet,
			CellRangeAddress reg) {
		RegionUtil.setBorderBottom(1, reg, sheet, workbook);
		RegionUtil.setBorderLeft(1, reg, sheet, workbook);
		RegionUtil.setBorderRight(1, reg, sheet, workbook);
		RegionUtil.setBorderTop(1, reg, sheet, workbook);
	}

}