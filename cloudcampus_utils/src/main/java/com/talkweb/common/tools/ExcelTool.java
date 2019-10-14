package com.talkweb.common.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: ExcelTool.java
 * @version:1.0
 * @Description: excel json格式处理工具
 * @author 武洋 ---智慧校
 * @date 2015年3月3日
 */
public class ExcelTool {
	private static final Logger logger = LoggerFactory
			.getLogger(ExcelTool.class);
	private static final String JSONObject = null;

	/**
	 * 导入excel 读取其数据
	 * 
	 * @param sheetName
	 *            可为null
	 * @param cnEnMap
	 *            中文英文表头映射表
	 * @param input
	 *            文件流
	 * @param startRowNum
	 *            默认填1
	 * @param fileType
	 *            xls还是xlsx
	 * @return
	 * @throws InstantiationException
	 */
	public static JSONArray importExcel(String sheetName,
			Hashtable<String, String> cnEnMap, InputStream input,
			int startRowNum, String fileType) throws InstantiationException {
		JSONArray result = new JSONArray();

		try {
			Workbook workbook = null;
			if (fileType.equalsIgnoreCase("xls")) {
				workbook = new HSSFWorkbook(input);
			} else if (fileType.equalsIgnoreCase("xlsx")) {
				workbook = new XSSFWorkbook(input);
			}
			Sheet sheet = workbook.getSheet(sheetName);
			if (!sheetName.trim().equals("")) {
				sheet = workbook.getSheet(sheetName);// 如果指定sheet名,则取指定sheet中的内容.
			}
			if (sheet == null || sheetName.trim().equals("")) {
				sheet = workbook.getSheetAt(0); // 如果传入的sheet名不存在则默认指向第1个sheet.
			}
			int rows = sheet.getPhysicalNumberOfRows();
			DecimalFormat df = new DecimalFormat("0");
			if (rows > 0) {// 有数据时才处理
				Row row0 = sheet.getRow(0);
				int cellNum0 = row0.getPhysicalNumberOfCells();
				String[] fields = new String[cellNum0];
				for (int j = 0; j < cellNum0; j++) {
					Cell cell = row0.getCell(j);
					if (cell == null) {
						continue;
					} else {
						String cv = cell.getStringCellValue();
						if (cnEnMap.containsKey(cv)) {
							fields[j] = cnEnMap.get(cv);
						}
					}
				}
				for (int i = startRowNum; i < rows; i++) {// 从第N行开始取数据.
					Row row = sheet.getRow(i);
					int cellNum = row.getPhysicalNumberOfCells();
					JSONObject rowEnt = new JSONObject();
					for (int j = 0; j < cellNum; j++) {
						Cell cell = row.getCell(j);
						if (cell == null) {
							continue;
						}
						String c = cell.getStringCellValue();
						System.out.println(c);
						if (c.equals("")) {
							continue;
						}
						// 取得类型,并根据对象类型设置值.
						switch (cell.getCellType()) {
						case HSSFCell.CELL_TYPE_NUMERIC:
							rowEnt.put(fields[j],
									df.format(cell.getNumericCellValue()));
							break;
						case HSSFCell.CELL_TYPE_STRING:
							rowEnt.put(fields[j], cell.getRichStringCellValue()
									.getString());
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							rowEnt.put(fields[j], cell.getCellFormula());
							break;
						case HSSFCell.CELL_TYPE_BLANK:
							rowEnt.put(fields[j], "");
						}

					}
					if (rowEnt != null) {
						result.add(rowEnt);
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return result;

	}

	/**
	 * 用于动态列导出excel插件
	 * 
	 * @param list
	 *            json队列
	 * @param filedNames
	 *            要导出的字段组
	 * @param filedNamesCHN
	 *            要导出字段组的中文示意
	 * @param sheetName
	 *            页签名称
	 * @param output
	 * @return
	 * @throws IllegalAccessException
	 */
	public static boolean exportExcel(JSONArray list, String[] filedNames,
			String[] filedNamesCHN, String sheetName, OutputStream output)
			throws IllegalAccessException {

		HSSFWorkbook workbook = new HSSFWorkbook();// 产生工作薄对象

		int sheetSize = list.size() + 1;
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}
		double sheetNo = Math.ceil(list.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			HSSFSheet sheet = workbook.createSheet();// 产生工作表对象
			if (sheetNo == 0) {
				workbook.setSheetName(index, sheetName);
			} else {
				workbook.setSheetName(index, sheetName + index);// 设置工作表的名称.
			}
			HSSFRow row;
			HSSFCell cell;// 产生单元格

			row = sheet.createRow(0);// 产生一行
			// 写入各个字段的列头名称
			for (int i = 0; i < filedNames.length; i++) {
				cell = row.createCell(i);// 创建列
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);// 设置列中写入内容为String类型
				cell.setCellValue(filedNamesCHN[i]);// 写入列名

				// 如果设置了提示信息则鼠标放上去提示.
				// if (!attr.prompt().trim().equals("")) {
				// setHSSFPrompt(sheet, "", attr.prompt(), 1, 100, col, col);//
				// 这里默认设了2-101列提示.
				// }
				// // 如果设置了combo属性则本列只能选择不能输入
				// if (attr.combo().length > 0) {
				// setHSSFValidation(sheet, attr.combo(), 1, 100, col, col);//
				// 这里默认设了2-101列只能选择不能输入.
				// }
			}

			int startNo = index * sheetSize;
			int endNo = Math.min(startNo + sheetSize, list.size());
			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i + 1 - startNo);
				JSONObject vo = list.getJSONObject(i); // 得到导出对象.
				for (int j = 0; j < filedNames.length; j++) {
					try {
						// 根据ExcelVOAttribute中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						cell.setCellValue(vo.getString(filedNames[j]) == null ? ""
								: String.valueOf(vo.getString(filedNames[j])));// 如果数据存在就填入,不存在填入空格.
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}

		}
		try {
			output.flush();
			workbook.write(output);
			output.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("exportExcel:{}", e);
			return false;
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 用于大量数据导出excel插件
	 * 
	 * @param list
	 *            json队列
	 * @param filedNames
	 *            要导出的字段组
	 * @param filedNamesCHN
	 *            要导出字段组的中文示意
	 * @param sheetName
	 *            页签名称
	 * @param output
	 * @return
	 * @throws IllegalAccessException
	 */
	public static void exportLargeDataExcel(List<JSONObject> data,
			List<JSONObject> headData, String sheetName, HttpServletResponse res)
			throws IllegalAccessException {
		SXSSFWorkbook wb = new SXSSFWorkbook(1000); // 这里100是在内存中的数量，如果大于此数量时，会写到硬盘，以避免在内存导致内存溢出
		int sheetSize = data.size() + 1;
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			Sheet sheet = wb.createSheet();// 产生工作表对象
			if (sheetNo == 0) {
				wb.setSheetName(index, sheetName);
			} else {
				wb.setSheetName(index, sheetName + index);// 设置工作表的名称.
			}
			Row row;
			Cell cell;// 产生单元格
			row = sheet.createRow(0);// 产生一行
			// 写入各个字段的列头名称
			for (int i = 0; i < headData.size(); i++) {
				cell = row.createCell(i);// 创建列
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);// 设置列中写入内容为String类型
				JSONObject object = (JSONObject) headData.get(i);
				cell.setCellValue(object.getString("headName"));// 写入列名
			}
			int startNo = index * sheetSize;
			int endNo = Math.min(startNo + sheetSize, data.size());
			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i + 1 - startNo);
				JSONObject vo = data.get(i); // 得到导出对象.
				for (int j = 0; j < headData.size(); j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						JSONObject object = (JSONObject) headData.get(j);
						cell.setCellValue(vo.getString(object
								.getString("feilName")) == null ? "" : String
								.valueOf(vo.getString(object
										.getString("feilName"))));// 如果数据存在就填入,不存在填入空格.
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
			if (!temp.exists()) {
				temp.createNewFile();
			}
			out = new FileOutputStream(temp);
			out.flush();
			wb.write(out);
			out.close();
			// long fileLength = temp.length();
			res.setContentType("octets/stream");
			res.addHeader("Content-Type", "text/html; charset=utf-8");
			sheetName += ".xlsx";
			String downLoadName = new String(sheetName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != wb) {
				try {
					wb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportExcelWithData(JSONArray data, JSONArray excelHead,
			String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res) {
		exportExcelWithData(data, excelHead, fileName, needMergDataField, req,
				res, false);
	}

	/**
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 * @param dataNullExport
	 *            默认false 空数据不允许导出
	 */
	public static void exportExcelWithData(JSONArray data, JSONArray excelHead,
			String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res,
			boolean dataNullExport) {
		if (!dataNullExport) {
			if (data == null || data.size() == 0) {
				return;
			}
		} else {
			if (data == null) {
				return;
			}
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setWrapText(true);// 设置自动换行
		
		
		XSSFCellStyle setBorderColor = workbook.createCellStyle();
		setBorderColor.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		setBorderColor.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderColor.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderColor.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderColor.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderColor.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorderColor.setWrapText(true);// 设置自动换行
		XSSFFont fontColor = workbook.createFont();
//		fontColor.setColor(HSSFColor.BLUE.index);
		setBorderColor.setFont(fontColor);
		
		List<String> fieldList = new ArrayList<String>();
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		int tl = excelHead.size(); // 计算行数
		// 计算列数
		int colNum = 0;
		// XSSFRow row0 = sheet.getRow(0);
		JSONArray Line0 = excelHead.getJSONArray(0);
		for (int j = 0; j < Line0.size(); j++) {
			JSONObject d0 = Line0.getJSONObject(j);
			int colspan = 1;
			if (d0.containsKey("colspan")) {
				colspan = d0.getIntValue("colspan");
			}
			colNum = colNum + colspan;
		}
		// 创建表头行
		for (int i = 0; i < tl; i++) {
			sheet.createRow(i);
		}
		// 被占用矩阵
		Hashtable<String, String> tb = new Hashtable<String, String>();
		HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();
		// 循环每行设置内容 及合并
		for (int i = 0; i < tl; i++) {
			XSSFRow row = sheet.getRow(i);
			JSONArray Line = excelHead.getJSONArray(i);
			for (int j = 0; j < Line.size(); j++) {
				JSONObject d = Line.getJSONObject(j);
				String title = null;
				if (d.containsKey("title")) {
					title = d.getString("title");
					if (title.indexOf("&gt;") > -1) {
						title = new String(title.replaceAll("&gt;", ">"));
					}

					else if (title.indexOf("&lt;") > -1) {
						title = new String(title.replaceAll("&lt;", "<"));
					}
				}
				int width = 80;
				if (d.containsKey("width")) {
					width = d.getIntValue("width");
				}
				int rowspan = 1;
				if (d.containsKey("rowspan")) {
					rowspan = d.getIntValue("rowspan");
				}
				int colspan = 1;
				if (d.containsKey("colspan")) {
					colspan = d.getIntValue("colspan");
				}
				boolean hidden = false;
				if (d.containsKey("hidden")) {
					hidden = d.getBooleanValue("hidden");
				}
				boolean isCheckBoxOrCZ = false;
				if (title == null || d.containsKey("checkbox")
						|| d.getString("title").equalsIgnoreCase("操作")) {
					isCheckBoxOrCZ = true;
				}
				String field = null;
				if (d.containsKey("field") && !isCheckBoxOrCZ) {
					field = d.getString("field");
					fieldList.add(field);
				}
				if (!hidden && !isCheckBoxOrCZ) {
					int index = 0;
					if (!tb.isEmpty()) {
						for (int c = 0; c < colNum; c++) {
							String key = i + "," + c;
							if (!tb.containsKey(key)) {
								index = c;
								break;
							}
						}
					}
					XSSFCell cell = row.createCell(index);
					cell.setCellValue(title);
					cell.setCellStyle(setBorderBD);
					sheet.setColumnWidth(index, width * 40);
					headIndexMap.put(index, field);// 存入表头所在的列
					for (int l = i; l < rowspan + i; l++) {
						for (int c = index; c < colspan + index; c++) {
							tb.put(l + "," + c, "1");
						}
					}
					// 跨行合并
					if (rowspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i), (short) (i + rowspan - 1),
								(short) index, (short) index);
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
					// 跨列合并
					if (colspan > 1) {
						CellRangeAddress reg = new CellRangeAddress((short) i,
								(short) i, (short) index, (short) (index
										+ colspan - 1));
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
				}
			}
		}
		int sheetSize = data.size();
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 封装表头结束 开始写入数据
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			XSSFRow row;
			XSSFCell cell;// 产生单元格

			int startNo = index * sheetSize + tl;

			int endNo = Math.min(startNo + sheetSize, tl + data.size());
			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i);
				JSONObject vo = data.getJSONObject(i - tl); // 得到导出对象.
				for (int j = 0; j < colNum; j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						// cell.setCellValue(vo.getString(fieldList.get(j)) ==
						// null ? "" : String.valueOf(vo
						// .getString(fieldList.get(j))));// 如果数据存在就填入,不存在填入空格.
						cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
								: String.valueOf(vo.getString(headIndexMap
										.get(j))));// 如果数据存在就填入,不存在填入空格.
						cell.setCellStyle(setBorder);
						if(j==2){
							cell.setCellStyle(setBorderColor);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (needMergDataField != null && needMergDataField.length > 0) {
			for (int i = 0; i < needMergDataField.length; i++) {
				String ndnm = needMergDataField[i];
				int colInx = findIndexByList(ndnm, fieldList);
				int startNo = tl;

				int endNo = Math.min(startNo + sheetSize, tl + data.size());
				// 写入各条记录,每条记录对应excel表中的一行
				int sr = startNo;
				int er = startNo;
				for (int j = startNo + 1; j < endNo + 1; j++) {
					XSSFRow crow = sheet.getRow(j);
					XSSFRow lrow = sheet.getRow(j - 1);
					boolean bv = false;
					if (crow != null && crow.getCell(colInx) != null) {
						bv = (crow.getCell(colInx).getStringCellValue()
								.equalsIgnoreCase(lrow.getCell(colInx)
										.getStringCellValue()));
					}
					// 如果跟前一行一致 记录索引
					if (bv) {
						er = j;
					} else {
						// 如果跟之前的不一致 判断是否需要进行表格合并
						if (er > sr) {
							// 合并单元格
							CellRangeAddress reg = new CellRangeAddress(
									(short) sr, (short) colInx, (short) er,
									(short) colInx);
							sheet.addMergedRegion(reg);
						}
						sr = j;
						er = j;
					}
				}
			}
		}
		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
			if (!temp.exists()) {
				temp.createNewFile();
			}
			out = new FileOutputStream(temp);
			out.flush();
			workbook.write(out);
			out.close();
			// long fileLength = temp.length();
			res.setContentType("octets/stream");
			res.addHeader("Content-Type", "text/html; charset=utf-8");
			fileName += ".xlsx";
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
			res.addHeader("Content-Disposition", "attachment;filename="
					+ downLoadName);
			// res.setHeader("Content-Length", String.valueOf(fileLength));
			bis = new BufferedInputStream(new FileInputStream(temp));
			bos = new BufferedOutputStream(res.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportExcelWithData:error{}", e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			temp.delete();
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	/** 第三列为蓝色字体
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 * @param dataNullExport
	 *            默认false 空数据不允许导出
	 */
	public static void exportExcelWithDataColor(JSONArray data, JSONArray excelHead,
			String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res,
			boolean dataNullExport) {
		if (!dataNullExport) {
			if (data == null || data.size() == 0) {
				return;
			}
		} else {
			if (data == null) {
				return;
			}
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setWrapText(true);// 设置自动换行
		
		
		XSSFCellStyle setBorderColor = workbook.createCellStyle();
		setBorderColor.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		setBorderColor.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderColor.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderColor.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderColor.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderColor.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorderColor.setWrapText(true);// 设置自动换行
		XSSFFont fontColor = workbook.createFont();
		//影响颜色 暂时注释了
//		fontColor.setColor(HSSFColor.BLUE.index);
		setBorderColor.setFont(fontColor);
		
		List<String> fieldList = new ArrayList<String>();
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		int tl = excelHead.size(); // 计算行数
		// 计算列数
		int colNum = 0;
		// XSSFRow row0 = sheet.getRow(0);
		JSONArray Line0 = excelHead.getJSONArray(0);
		for (int j = 0; j < Line0.size(); j++) {
			JSONObject d0 = Line0.getJSONObject(j);
			int colspan = 1;
			if (d0.containsKey("colspan")) {
				colspan = d0.getIntValue("colspan");
			}
			colNum = colNum + colspan;
		}
		// 创建表头行
		for (int i = 0; i < tl; i++) {
			sheet.createRow(i);
		}
		// 被占用矩阵
		Hashtable<String, String> tb = new Hashtable<String, String>();
		HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();
		// 循环每行设置内容 及合并
		for (int i = 0; i < tl; i++) {
			XSSFRow row = sheet.getRow(i);
			JSONArray Line = excelHead.getJSONArray(i);
			for (int j = 0; j < Line.size(); j++) {
				JSONObject d = Line.getJSONObject(j);
				String title = null;
				if (d.containsKey("title")) {
					title = d.getString("title");
					if (title.indexOf("&gt;") > -1) {
						title = new String(title.replaceAll("&gt;", ">"));
					}

					else if (title.indexOf("&lt;") > -1) {
						title = new String(title.replaceAll("&lt;", "<"));
					}
				}
				int width = 80;
				if (d.containsKey("width")) {
					width = d.getIntValue("width");
				}
				int rowspan = 1;
				if (d.containsKey("rowspan")) {
					rowspan = d.getIntValue("rowspan");
				}
				int colspan = 1;
				if (d.containsKey("colspan")) {
					colspan = d.getIntValue("colspan");
				}
				boolean hidden = false;
				if (d.containsKey("hidden")) {
					hidden = d.getBooleanValue("hidden");
				}
				boolean isCheckBoxOrCZ = false;
				if (title == null || d.containsKey("checkbox")
						|| d.getString("title").equalsIgnoreCase("操作")) {
					isCheckBoxOrCZ = true;
				}
				String field = null;
				if (d.containsKey("field") && !isCheckBoxOrCZ) {
					field = d.getString("field");
					fieldList.add(field);
				}
				if (!hidden && !isCheckBoxOrCZ) {
					int index = 0;
					if (!tb.isEmpty()) {
						for (int c = 0; c < colNum; c++) {
							String key = i + "," + c;
							if (!tb.containsKey(key)) {
								index = c;
								break;
							}
						}
					}
					XSSFCell cell = row.createCell(index);
					cell.setCellValue(title);
					cell.setCellStyle(setBorderBD);
					sheet.setColumnWidth(index, width * 40);
					headIndexMap.put(index, field);// 存入表头所在的列
					for (int l = i; l < rowspan + i; l++) {
						for (int c = index; c < colspan + index; c++) {
							tb.put(l + "," + c, "1");
						}
					}
					// 跨行合并
					if (rowspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i), (short) (i + rowspan - 1),
								(short) index, (short) index);
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
					// 跨列合并
					if (colspan > 1) {
						CellRangeAddress reg = new CellRangeAddress((short) i,
								(short) i, (short) index, (short) (index
										+ colspan - 1));
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
				}
			}
		}
		int sheetSize = data.size();
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 封装表头结束 开始写入数据
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			XSSFRow row;
			XSSFCell cell;// 产生单元格

			int startNo = index * sheetSize + tl;

			int endNo = Math.min(startNo + sheetSize, tl + data.size());
			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i);
				JSONObject vo = data.getJSONObject(i - tl); // 得到导出对象.
				for (int j = 0; j < colNum; j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						// cell.setCellValue(vo.getString(fieldList.get(j)) ==
						// null ? "" : String.valueOf(vo
						// .getString(fieldList.get(j))));// 如果数据存在就填入,不存在填入空格.
						cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
								: String.valueOf(vo.getString(headIndexMap
										.get(j))));// 如果数据存在就填入,不存在填入空格.
						cell.setCellStyle(setBorder);
						if(j==2){
							cell.setCellStyle(setBorderColor);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (needMergDataField != null && needMergDataField.length > 0) {
			for (int i = 0; i < needMergDataField.length; i++) {
				String ndnm = needMergDataField[i];
				int colInx = findIndexByList(ndnm, fieldList);
				int startNo = tl;

				int endNo = Math.min(startNo + sheetSize, tl + data.size());
				// 写入各条记录,每条记录对应excel表中的一行
				int sr = startNo;
				int er = startNo;
				for (int j = startNo + 1; j < endNo + 1; j++) {
					XSSFRow crow = sheet.getRow(j);
					XSSFRow lrow = sheet.getRow(j - 1);
					boolean bv = false;
					if (crow != null && crow.getCell(colInx) != null) {
						bv = (crow.getCell(colInx).getStringCellValue()
								.equalsIgnoreCase(lrow.getCell(colInx)
										.getStringCellValue()));
					}
					// 如果跟前一行一致 记录索引
					if (bv) {
						er = j;
					} else {
						// 如果跟之前的不一致 判断是否需要进行表格合并
						if (er > sr) {
							// 合并单元格
							CellRangeAddress reg = new CellRangeAddress(
									(short) sr, (short) colInx, (short) er,
									(short) colInx);
							sheet.addMergedRegion(reg);
						}
						sr = j;
						er = j;
					}
				}
			}
		}
		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
			if (!temp.exists()) {
				temp.createNewFile();
			}
			out = new FileOutputStream(temp);
			out.flush();
			workbook.write(out);
			out.close();
			// long fileLength = temp.length();
			res.setContentType("octets/stream");
			res.addHeader("Content-Type", "text/html; charset=utf-8");
			fileName += ".xlsx";
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
			res.addHeader("Content-Disposition", "attachment;filename="
					+ downLoadName);
			// res.setHeader("Content-Length", String.valueOf(fileLength));
			bis = new BufferedInputStream(new FileInputStream(temp));
			bos = new BufferedOutputStream(res.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("exportExcelWithData:error{}", e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			temp.delete();
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	/**
	 * 导出两个表头的数据
	 * 
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportTwoHeadExcelWithData(JSONArray firstExcelData,
			JSONArray firstExcelHeads, JSONArray secondExcelData,
			JSONArray secondExcelHeads, String fileName,
			String[] needMergDataField, HttpServletRequest req,
			HttpServletResponse res) {
		if (null == firstExcelData || secondExcelData == null) {
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();

		int endRow = writeDataToExcel(0, 0, workbook, sheet, needMergDataField,
				firstExcelData, firstExcelHeads);

		writeDataToExcel(endRow, 5, workbook, sheet, needMergDataField,
				secondExcelData, secondExcelHeads);

		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
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
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != workbook) {
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
	 * 
	 * @param fromRow
	 *            其实行数
	 * @param gapRow
	 *            与上一个表格相间行数
	 * @param workbook
	 *            表格对象
	 * @param sheet
	 * @param needMergDataField
	 * @param data
	 * @param excelHead
	 * @return
	 */
	private static int writeDataToExcel1(int fromRow, int gapRow,
			XSSFWorkbook workbook, XSSFSheet sheet, String[] needMergDataField,
			List<String> titles, JSONArray data, JSONArray excelHead) {
		// 1.定义变量，表格单元属性对象，以及行数，列数
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBordertitle = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setWrapText(true);// 设置自动换行
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBordertitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBordertitle.setFont(font);
		List<String> fieldList = new ArrayList<String>();// 字段列表
		int corsp = 2;
		int startR = 0 + fromRow + gapRow;
		sheet.createRow(startR);
		XSSFRow row1 = sheet.getRow(startR);
		int u = 0;
		for (String str : titles) {
			if (u == 0) {
				XSSFCell cell = row1.createCell(u);
				cell.setCellValue(str);
				cell.setCellStyle(setBordertitle);
				sheet.setColumnWidth(u, 80 * 40);
				CellRangeAddress reg = new CellRangeAddress((short) (startR),
						(short) (startR), (short) u, (short) (u + 1 - 1));
				sheet.addMergedRegion(reg);
			} else {
				XSSFCell cell = row1.createCell(u);
				cell.setCellValue(str);
				cell.setCellStyle(setBordertitle);
				sheet.setColumnWidth(u, 80 * 40);
				CellRangeAddress reg = new CellRangeAddress((short) (startR),
						(short) (startR), (short) u, (short) (u + corsp - 1));
				sheet.addMergedRegion(reg);
			}
			u++;
		}

		// 2.计算列数，以及 开始的行数

		int startRowNo =titles.size()>0?1:0;// 数据开始插入的行号
		int headRowSize = excelHead.size(); // 计算表头行数
		int colNum = 0;

		JSONArray Line0 = excelHead.getJSONArray(0);
		for (int j = 0; j < Line0.size(); j++) {
			JSONObject d0 = Line0.getJSONObject(j);
			int colspan = 1;
			if (d0.containsKey("colspan")) {
				colspan = d0.getIntValue("colspan");
			}
			colNum = colNum + colspan;
		}

		startRowNo += fromRow + gapRow;// 开始行编号+间隔行数

		// 3. 创建表头行,并且把数据填充到表头单元中
		for (int i = 0; i < headRowSize; i++) {
			sheet.createRow(startRowNo + i);
		}

		Hashtable<String, String> tb = new Hashtable<String, String>(); // 被占用矩阵
		HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();

		for (int i = 0; i < headRowSize; i++) {// 循环每行设置内容 及合并
			XSSFRow row = sheet.getRow(startRowNo + i);
			JSONArray Line = excelHead.getJSONArray(i);

			for (int j = 0; j < Line.size(); j++) {
				JSONObject d = Line.getJSONObject(j);
				String title = null;
				if (d.containsKey("title")) {
					title = d.getString("title");
				}
				int width = 80;
				if (d.containsKey("width")) {
					width = d.getIntValue("width");
				}
				int rowspan = 1;
				if (d.containsKey("rowspan")) {
					rowspan = d.getIntValue("rowspan");
				}
				int colspan = 1;
				if (d.containsKey("colspan")) {
					colspan = d.getIntValue("colspan");
				}
				boolean hidden = false;
				if (d.containsKey("hidden")) {
					hidden = d.getBooleanValue("hidden");
				}
				boolean isCheckBoxOrCZ = false;
				if (title == null || d.containsKey("checkbox")
						|| d.getString("title").equalsIgnoreCase("操作")) {
					isCheckBoxOrCZ = true;
				}
				String field = null;
				if (d.containsKey("field") && !isCheckBoxOrCZ) {
					field = d.getString("field");
					fieldList.add(field);
				}
				if (!hidden && !isCheckBoxOrCZ) {
					int index = 0;
					if (!tb.isEmpty()) {
						for (int c = 0; c < colNum; c++) {
							String key = i + "," + c;
							if (!tb.containsKey(key)) {
								index = c;
								break;
							}
						}
					}
					XSSFCell cell = row.createCell(index);
					cell.setCellValue(title);
					cell.setCellStyle(setBorderBD);
					sheet.setColumnWidth(index, width * 40);
					headIndexMap.put(index, field);// 存入表头所在的列
					for (int l = i; l < rowspan + i; l++) {
						for (int c = index; c < colspan + index; c++) {
							tb.put(l + "," + c, "1");
						}
					}
					// 跨行合并
					if (rowspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo), (short) (i
										+ startRowNo + rowspan - 1),
								(short) index, (short) index);
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
					// 跨列合并
					if (colspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo),
								(short) (i + startRowNo), (short) index,
								(short) (index + colspan - 1));
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
				}
			}
		}

		// 4.创建数据行，已经填充数据，处理数据超出sheet容量的情况
		int sheetSize = data.size();
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 封装表头结束 开始写入数据
		int lastRowNo = startRowNo + data.size();
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			XSSFRow row;
			XSSFCell cell;// 产生单元格
			int startNo = index * sheetSize + headRowSize + startRowNo;
			int endNo = Math.min(startNo + sheetSize, headRowSize + startRowNo
					+ data.size());

			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i);
				JSONObject vo = data.getJSONObject(i
						- (headRowSize + startRowNo)); // 得到导出对象.
				for (int j = 0; j < colNum; j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						// 如果数据存在就填入,不存在填入空格.
						cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
								: String.valueOf(vo.getString(headIndexMap
										.get(j))));
						cell.setCellStyle(setBorder);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}

		}

		// 5.合并列数
		if (needMergDataField != null && needMergDataField.length > 0) {
			for (int i = 0; i < needMergDataField.length; i++) {
				String ndnm = needMergDataField[i];
				int colInx = findIndexByList(ndnm, fieldList);
				int startNo = headRowSize;

				int endNo = Math.min(startNo + sheetSize,
						headRowSize + data.size());
				// 写入各条记录,每条记录对应excel表中的一行
				int sr = startNo;
				int er = startNo;
				for (int j = startNo + 1; j < endNo + 1; j++) {
					XSSFRow crow = sheet.getRow(j);
					XSSFRow lrow = sheet.getRow(j - 1);
					boolean bv = false;
					if (crow != null && crow.getCell(colInx) != null) {
						bv = (crow.getCell(colInx).getStringCellValue()
								.equalsIgnoreCase(lrow.getCell(colInx)
										.getStringCellValue()));
					}
					// 如果跟前一行一致 记录索引
					if (bv) {
						er = j;
					} else {
						// 如果跟之前的不一致 判断是否需要进行表格合并
						if (er > sr) {
							// 合并单元格
							CellRangeAddress reg = new CellRangeAddress(
									(short) sr, (short) colInx, (short) er,
									(short) colInx);
							sheet.addMergedRegion(reg);
						}
						sr = j;
						er = j;
					}
				}
			}
		}

		// 6.返回最后一行的行号
		return lastRowNo;
	}

	private static int findIndexByList(String ndnm, List<String> fieldList) {
		// TODO Auto-generated method stub
		for (int i = 0; i < fieldList.size(); i++) {
			if (ndnm.equalsIgnoreCase(fieldList.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 导出两个表头的数据
	 * 
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportComplexHeadExcelWithData(
			List<JSONArray> firstExcelData, List<JSONArray> firstExcelHeads,
			String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res) {
		if (null == firstExcelData) {
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		writeComplexDataToExcel(0, 0, workbook, sheet, needMergDataField,
				firstExcelData, firstExcelHeads);

		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
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
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != workbook) {
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
	 * 
	 * @param fromRow
	 *            其实行数
	 * @param gapRow
	 *            与上一个表格相间行数
	 * @param workbook
	 *            表格对象
	 * @param sheet
	 * @param needMergDataField
	 * @param data
	 * @param excelHead
	 * @return
	 */
	private static void writeComplexDataToExcel(int fromRow, int gapRow,
			XSSFWorkbook workbook, XSSFSheet sheet, String[] needMergDataField,
			List<JSONArray> data, List<JSONArray> excelHead) {
		// 1.定义变量，表格单元属性对象，以及行数，列数
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		List<String> fieldList = new ArrayList<String>();// 字段列表

		// 2.计算列数，以及 开始的行数

		int startRowNo = 0;// 数据开始插入的行号

		int colNum = 0;
		int lastRowNo = 0;
		for (int index_i = 0; index_i < excelHead.size(); index_i++) {
			int headRowSize = excelHead.get(index_i).size(); // 计算表头行数
			if (lastRowNo != 0) {
				startRowNo = lastRowNo;
			}
			JSONArray jsonArray = excelHead.get(index_i);
			JSONArray Line0 = jsonArray.getJSONArray(0);
			for (int j = 0; j < Line0.size(); j++) {
				JSONObject d0 = Line0.getJSONObject(j);
				int colspan = 1;
				if (d0.containsKey("colspan")) {
					colspan = d0.getIntValue("colspan");
				}
				colNum = colNum + colspan;
			}
			if (lastRowNo == 0) {
				startRowNo = fromRow + gapRow;// 开始行编号+间隔行数
			}
			// 3. 创建表头行,并且把数据填充到表头单元中
			for (int i = 0; i < headRowSize; i++) {
				sheet.createRow(startRowNo + i);
			}

			Hashtable<String, String> tb = new Hashtable<String, String>(); // 被占用矩阵
			HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();

			for (int i = 0; i < headRowSize; i++) {// 循环每行设置内容 及合并
				XSSFRow row = sheet.getRow(startRowNo);
				if (row == null) {
					row = sheet.createRow(startRowNo);
				}
				JSONArray Line = jsonArray.getJSONArray(i);

				for (int j = 0; j < Line.size(); j++) {
					JSONObject d = Line.getJSONObject(j);
					String title = null;
					if (d.containsKey("title")) {
						title = d.getString("title");
					}
					int width = 80;
					if (d.containsKey("width")) {
						width = d.getIntValue("width");
					}
					int rowspan = 1;
					if (d.containsKey("rowspan")) {
						rowspan = d.getIntValue("rowspan");
					}
					int colspan = 1;
					if (d.containsKey("colspan")) {
						colspan = d.getIntValue("colspan");
					}
					boolean hidden = false;
					if (d.containsKey("hidden")) {
						hidden = d.getBooleanValue("hidden");
					}
					boolean isCheckBoxOrCZ = false;
					if (title == null || d.containsKey("checkbox")
							|| d.getString("title").equalsIgnoreCase("操作")) {
						isCheckBoxOrCZ = true;
					}
					String field = null;
					if (d.containsKey("field") && !isCheckBoxOrCZ) {
						field = d.getString("field");
						fieldList.add(field);
					}
					if (!hidden && !isCheckBoxOrCZ) {
						int index = 0;
						if (!tb.isEmpty()) {
							for (int c = 0; c < colNum; c++) {
								String key = i + "," + c;
								if (!tb.containsKey(key)) {
									index = c;
									break;
								}
							}
						}
						XSSFCell cell = row.createCell(index);
						cell.setCellValue(title);
						cell.setCellStyle(setBorderBD);
						sheet.setColumnWidth(index, width * 40);
						headIndexMap.put(index, field);// 存入表头所在的列
						for (int l = i; l < rowspan + i; l++) {
							for (int c = index; c < colspan + index; c++) {
								tb.put(l + "," + c, "1");
							}
						}
						// 跨行合并
						if (rowspan > 1) {
							CellRangeAddress reg = new CellRangeAddress(
									(short) (i + startRowNo), (short) (i
											+ rowspan - 1), (short) index,
									(short) index);
							sheet.addMergedRegion(reg);
						}
						// 跨列合并
						if (colspan > 1) {
							CellRangeAddress reg = new CellRangeAddress(
									(short) (i + startRowNo),
									(short) (i + startRowNo), (short) index,
									(short) (index + colspan - 1));
							sheet.addMergedRegion(reg);
						}
					}
				}
				startRowNo = startRowNo + 1;
			}

			// 4.创建数据行，已经填充数据，处理数据超出sheet容量的情况
			int sheetSize = data.get(index_i).size();
			// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
			if (sheetSize > 65535 || sheetSize < 1) {
				sheetSize = 65535;
			}

			// 封装表头结束 开始写入数据
			int indexNum = 0;
			lastRowNo = startRowNo + data.get(index_i).size();
			for (int startRow = startRowNo; startRow < lastRowNo; startRow++) {
				XSSFRow row;
				XSSFCell cell;// 产生单元格
				row = sheet.getRow(startRow);
				if (row == null) {
					row = sheet.createRow(startRow);
				}
				JSONObject vo = data.get(index_i).getJSONObject(indexNum);
				for (int j = 0; j < colNum; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						cell = row.createCell(j);
					}
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					// 如果数据存在就填入,不存在填入空格.
					cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
							: String.valueOf(vo.getString(headIndexMap.get(j))));
					cell.setCellStyle(setBorder);
					boolean flag = vo.containsKey("targetDetailZgt");
					if (flag) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (startRow), (short) (startRow),
								(short) 0, (short) (0 + 3 - 1));
						sheet.addMergedRegion(reg);
						break;
					}
					// sheet.autoSizeColumn(j);
					// int columnWidth = sheet.getColumnWidth(j);
					// int width = columnWidth * 2;
					// if(width >= 55000){
					int width = 5000;
					// }
					sheet.setColumnWidth(j, width);

				}
				++indexNum;
			}
		}
	}

	/****
	 * 写入指定数据到表格对象中
	 * 
	 * @param fromRow
	 *            其实行数
	 * @param gapRow
	 *            与上一个表格相间行数
	 * @param workbook
	 *            表格对象
	 * @param sheet
	 * @param needMergDataField
	 * @param data
	 * @param excelHead
	 * @return
	 */
	private static int writeDataToExcel(int fromRow, int gapRow,
			XSSFWorkbook workbook, XSSFSheet sheet, String[] needMergDataField,
			JSONArray data, JSONArray excelHead) {
		// 1.定义变量，表格单元属性对象，以及行数，列数
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		List<String> fieldList = new ArrayList<String>();// 字段列表

		// 2.计算列数，以及 开始的行数

		int startRowNo = 0;// 数据开始插入的行号
		int headRowSize = excelHead.size(); // 计算表头行数
		int colNum = 0;

		JSONArray Line0 = excelHead.getJSONArray(0);
		for (int j = 0; j < Line0.size(); j++) {
			JSONObject d0 = Line0.getJSONObject(j);
			int colspan = 1;
			if (d0.containsKey("colspan")) {
				colspan = d0.getIntValue("colspan");
			}
			colNum = colNum + colspan;
		}

		startRowNo = fromRow + gapRow;// 开始行编号+间隔行数

		// 3. 创建表头行,并且把数据填充到表头单元中
		for (int i = 0; i < headRowSize; i++) {
			sheet.createRow(startRowNo + i);
		}

		Hashtable<String, String> tb = new Hashtable<String, String>(); // 被占用矩阵
		HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();

		for (int i = 0; i < headRowSize; i++) {// 循环每行设置内容 及合并
			XSSFRow row = sheet.getRow(startRowNo + i);
			JSONArray Line = excelHead.getJSONArray(i);

			for (int j = 0; j < Line.size(); j++) {
				JSONObject d = Line.getJSONObject(j);
				String title = null;
				if (d.containsKey("title")) {
					title = d.getString("title");
				}
				int width = 80;
				if (d.containsKey("width")) {
					width = d.getIntValue("width");
				}
				int rowspan = 1;
				if (d.containsKey("rowspan")) {
					rowspan = d.getIntValue("rowspan");
				}
				int colspan = 1;
				if (d.containsKey("colspan")) {
					colspan = d.getIntValue("colspan");
				}
				boolean hidden = false;
				if (d.containsKey("hidden")) {
					hidden = d.getBooleanValue("hidden");
				}
				boolean isCheckBoxOrCZ = false;
				if (title == null || d.containsKey("checkbox")
						|| d.getString("title").equalsIgnoreCase("操作")) {
					isCheckBoxOrCZ = true;
				}
				String field = null;
				if (d.containsKey("field") && !isCheckBoxOrCZ) {
					field = d.getString("field");
					fieldList.add(field);
				}
				if (!hidden && !isCheckBoxOrCZ) {
					int index = 0;
					if (!tb.isEmpty()) {
						for (int c = 0; c < colNum; c++) {
							String key = i + "," + c;
							if (!tb.containsKey(key)) {
								index = c;
								break;
							}
						}
					}
					XSSFCell cell = row.createCell(index);
					cell.setCellValue(title);
					cell.setCellStyle(setBorderBD);
					sheet.setColumnWidth(index, width * 40);
					headIndexMap.put(index, field);// 存入表头所在的列
					for (int l = i; l < rowspan + i; l++) {
						for (int c = index; c < colspan + index; c++) {
							tb.put(l + "," + c, "1");
						}
					}
					// 跨行合并
					if (rowspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo),
								(short) (i + rowspan - 1), (short) index,
								(short) index);
						sheet.addMergedRegion(reg);
					}
					// 跨列合并
					if (colspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo),
								(short) (i + startRowNo), (short) index,
								(short) (index + colspan - 1));
						sheet.addMergedRegion(reg);
					}
				}
			}
		}

		// 4.创建数据行，已经填充数据，处理数据超出sheet容量的情况
		int sheetSize = data.size();
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 封装表头结束 开始写入数据
		int lastRowNo = startRowNo + data.size();
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			XSSFRow row;
			XSSFCell cell;// 产生单元格
			int startNo = index * sheetSize + headRowSize + startRowNo;
			int endNo = Math.min(startNo + sheetSize, headRowSize + startRowNo
					+ data.size());

			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.createRow(i);
				JSONObject vo = data.getJSONObject(i
						- (headRowSize + startRowNo)); // 得到导出对象.
				for (int j = 0; j < colNum; j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						// 如果数据存在就填入,不存在填入空格.
						cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
								: String.valueOf(vo.getString(headIndexMap
										.get(j))));
						cell.setCellStyle(setBorder);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 5.合并列数
		if (needMergDataField != null && needMergDataField.length > 0) {
			for (int i = 0; i < needMergDataField.length; i++) {
				String ndnm = needMergDataField[i];
				int colInx = findIndexByList(ndnm, fieldList);
				int startNo = headRowSize;

				int endNo = Math.min(startNo + sheetSize,
						headRowSize + data.size());
				// 写入各条记录,每条记录对应excel表中的一行
				int sr = startNo;
				int er = startNo;
				for (int j = startNo + 1; j < endNo + 1; j++) {
					XSSFRow crow = sheet.getRow(j);
					XSSFRow lrow = sheet.getRow(j - 1);
					boolean bv = false;
					if (crow != null && crow.getCell(colInx) != null) {
						bv = (crow.getCell(colInx).getStringCellValue()
								.equalsIgnoreCase(lrow.getCell(colInx)
										.getStringCellValue()));
					}
					// 如果跟前一行一致 记录索引
					if (bv) {
						er = j;
					} else {
						// 如果跟之前的不一致 判断是否需要进行表格合并
						if (er > sr) {
							// 合并单元格
							CellRangeAddress reg = new CellRangeAddress(
									(short) sr, (short) colInx, (short) er,
									(short) colInx);
							sheet.addMergedRegion(reg);
						}
						sr = j;
						er = j;
					}
				}
			}
		}

		// 6.返回最后一行的行号
		return lastRowNo;
	}

	/**
	 * 学生成绩单导出数据
	 * 
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportExcelWithTea(JSONArray ExcelData,
			JSONArray ExcelHeads, String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res) {
		if (null == ExcelData || ExcelHeads == null) {
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		int endRow = 0;
		for (int i = 0; i < ExcelData.size(); i++) {
			List<String> titles = new ArrayList<String>();
			JSONObject data = ExcelData.getJSONObject(i);
			String classname = "班级名称:" + data.getString("classsName");
			String name = "学生姓名:" + data.getString("studentName");
			titles.add(classname);
			titles.add(name);
			JSONArray rows = JSONArray.parseArray(data.getString("rows"));
			if (i == 0) {
				endRow = writeDataToExcel1(endRow, 0, workbook, sheet,
						needMergDataField, titles, rows, ExcelHeads);
			} else {
				endRow = writeDataToExcel1(endRow, 3, workbook, sheet,
						needMergDataField, titles, rows, ExcelHeads);
			}
		}

		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
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
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 *  班级考场对照表 考场考生名单
	 * 
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportExcelWithTable(JSONArray ExcelData,
			JSONArray ExcelHeads, String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res) {
		if (null == ExcelData || ExcelHeads == null) {
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		int endRow = 0;
		if(!ExcelData.isEmpty()){
			if( ExcelData.getJSONObject(0).getString("list")!=null){
				for (int i = 0; i < ExcelData.size(); i++) {
					List<String> titles = new ArrayList<String>();
					JSONObject data = ExcelData.getJSONObject(i);
					String name = "考场名称:" + data.getString("examPlaceName");
					String number = "座位号:" + data.getString("seatNumber");
					titles.add(name);
					titles.add(number);
					JSONArray rows = JSONArray.parseArray(data.getString("list"));
						if (i == 0) {
							endRow = writeDataToExcel1(endRow, 0, workbook, sheet,
									needMergDataField, titles, rows, ExcelHeads);
						} else {
							endRow = writeDataToExcel1(endRow, 3, workbook, sheet,
									needMergDataField, titles, rows, ExcelHeads);
						}
				}
			}else{
				List<String> titles = new ArrayList<String>();
					endRow = writeDataToExcel1(endRow, 0, workbook, sheet,
							needMergDataField, titles, ExcelData, ExcelHeads);
			}
		}
		

		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
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
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	/**
	 * 桌角条 一行两表
	 * 
	 * @param data
	 *            要导出的数据
	 * @param excelHead
	 *            表头 datagrid组件生成
	 * @param fileName
	 *            文件名
	 * @param needMergDataField
	 *            需要合并的列（值相等则合并）
	 * @param req
	 * @param res
	 */
	public static void exportExcelWithTableByZJT(JSONArray ExcelData,
			JSONArray ExcelHeads, String fileName, String[] needMergDataField,
			HttpServletRequest req, HttpServletResponse res) {
		if (null == ExcelData || ExcelHeads == null) {
			return;
		}
		XSSFWorkbook workbook = new XSSFWorkbook();// 产生工作薄对象
		// 设置表头
		XSSFSheet sheet = workbook.createSheet();
		int endRow = 0;
		if(!ExcelData.isEmpty()){
			if( ExcelData.getJSONObject(0).getString("list")!=null){
				for (int i = 0; i < ExcelData.size(); i++) {
					List<String> titles = new ArrayList<String>();
					JSONObject data = ExcelData.getJSONObject(i);
					String name = "考场名称:" + data.getString("examPlaceName");
					String number = "座位号:" + data.getString("seatNumber");
					titles.add(name);
					titles.add(number);
					JSONArray rows = JSONArray.parseArray(data.getString("list"));
						if (i == 0) {
							writeDataToExcel1(endRow, 0, workbook, sheet,
									needMergDataField, titles, rows, ExcelHeads);
						} else if(i==1){
							endRow=writeDataToExcelTable(endRow, 0, workbook, sheet,
									needMergDataField, titles, rows, ExcelHeads);
						}else {
							if ((i & 1) != 0) {// 奇数
								endRow=writeDataToExcelTable(endRow, 3, workbook, sheet,
										needMergDataField, titles, rows, ExcelHeads);
							}else{
								 writeDataToExcel1(endRow, 3, workbook, sheet,
											needMergDataField, titles, rows, ExcelHeads);
							}
						}
				}
			}else{
				List<String> titles = new ArrayList<String>();
					endRow = writeDataToExcel1(endRow, 0, workbook, sheet,
							needMergDataField, titles, ExcelData, ExcelHeads);
			}
		}
		

		String xls = UUIDUtil.getUUID();
		File temp = new File(xls + ".xlsx");
		OutputStream out = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 写入数据结束
		try {
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
			String downLoadName = new String(fileName.getBytes("gbk"),
					"iso8859-1");
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
			logger.error("exportExcelWithData:error{}", e);
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
			if (null != workbook) {
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
	 * 
	 * @param fromRow
	 *            其实行数
	 * @param gapRow
	 *            与上一个表格相间行数
	 * @param workbook
	 *            表格对象
	 * @param sheet
	 * @param needMergDataField
	 * @param data
	 * @param excelHead
	 * @return
	 */
	private static int writeDataToExcelTable(int fromRow, int gapRow,
			XSSFWorkbook workbook, XSSFSheet sheet, String[] needMergDataField,
			List<String> titles, JSONArray data, JSONArray excelHead) {
		// 1.定义变量，表格单元属性对象，以及行数，列数
		XSSFCellStyle setBorder = workbook.createCellStyle();
		XSSFCellStyle setBordertitle = workbook.createCellStyle();
		XSSFCellStyle setBorderBD = workbook.createCellStyle();
		setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBorder.setWrapText(true);// 设置自动换行
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 粗体显示
		setBorderBD.setFont(font);
		setBorderBD.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBorderBD.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		setBorderBD.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		setBorderBD.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		setBorderBD.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		setBordertitle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		setBordertitle.setFont(font);
		List<String> fieldList = new ArrayList<String>();// 字段列表
		
		JSONArray Line0 = excelHead.getJSONArray(0);
		int corsp = 2;
		int startR = 0 + fromRow + gapRow;
		//sheet.getRow(startR)
		//sheet.createRow(startR);
		XSSFRow row1 = sheet.getRow(startR);
		int gapCol=Line0.size()+1;
		int u =gapCol;
		for (String str : titles) {
			if (u == 6) {
				XSSFCell cell = row1.createCell(u);
				cell.setCellValue(str);
				cell.setCellStyle(setBordertitle);
				sheet.setColumnWidth(u, 80 * 40);
				CellRangeAddress reg = new CellRangeAddress((short) (startR),
						(short) (startR), (short) u, (short) (u + 1 - 1));
				sheet.addMergedRegion(reg);
			} else {
				XSSFCell cell = row1.createCell(u);
				cell.setCellValue(str);
				cell.setCellStyle(setBordertitle);
				sheet.setColumnWidth(u, 80 * 40);
				CellRangeAddress reg = new CellRangeAddress((short) (startR),
						(short) (startR), (short) u, (short) (u + corsp - 1));
				sheet.addMergedRegion(reg);
			}
			u++;
		}

		// 2.计算列数，以及 开始的行数

		int startRowNo = 1;// 数据开始插入的行号
		int headRowSize = excelHead.size(); // 计算表头行数
		int colNum = gapCol;

		
		for (int j = 0; j < Line0.size(); j++) {
			JSONObject d0 = Line0.getJSONObject(j);
			int colspan = 1;
			if (d0.containsKey("colspan")) {
				colspan = d0.getIntValue("colspan");
			}
			colNum = colNum + colspan;
		}

		startRowNo += fromRow + gapRow;// 开始行编号+间隔行数

		// 3. 创建表头行,并且把数据填充到表头单元中
		//for (int i = 0; i < headRowSize; i++) {
		//	sheet.createRow(startRowNo + i);
		//}

		Hashtable<String, String> tb = new Hashtable<String, String>(); // 被占用矩阵
		HashMap<Integer, String> headIndexMap = new HashMap<Integer, String>();

		for (int i = 0; i < headRowSize; i++) {// 循环每行设置内容 及合并
			XSSFRow row = sheet.getRow(startRowNo + i);
			JSONArray Line = excelHead.getJSONArray(i);

			for (int j = 0; j < Line.size(); j++) {
				JSONObject d = Line.getJSONObject(j);
				String title = null;
				if (d.containsKey("title")) {
					title = d.getString("title");
				}
				int width = 80;
				if (d.containsKey("width")) {
					width = d.getIntValue("width");
				}
				int rowspan = 1;
				if (d.containsKey("rowspan")) {
					rowspan = d.getIntValue("rowspan");
				}
				int colspan = 1;
				if (d.containsKey("colspan")) {
					colspan = d.getIntValue("colspan");
				}
				boolean hidden = false;
				if (d.containsKey("hidden")) {
					hidden = d.getBooleanValue("hidden");
				}
				boolean isCheckBoxOrCZ = false;
				if (title == null || d.containsKey("checkbox")
						|| d.getString("title").equalsIgnoreCase("操作")) {
					isCheckBoxOrCZ = true;
				}
				String field = null;
				if (d.containsKey("field") && !isCheckBoxOrCZ) {
					field = d.getString("field");
					fieldList.add(field);
				}
				if (!hidden && !isCheckBoxOrCZ) {
					int index = gapCol;
					if (!tb.isEmpty()) {
						for (int c = gapCol; c < colNum; c++) {
							String key = i + "," + c;
							if (!tb.containsKey(key)) {
								index = c;
								break;
							}
						}
					}
					XSSFCell cell = row.createCell(index);
					cell.setCellValue(title);
					cell.setCellStyle(setBorderBD);
					sheet.setColumnWidth(index, width * 40);
					headIndexMap.put(index, field);// 存入表头所在的列
					for (int l = i; l < rowspan + i; l++) {
						for (int c = index; c < colspan + index; c++) {
							tb.put(l + "," + c, "1");
						}
					}
					// 跨行合并
					if (rowspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo), (short) (i
										+ startRowNo + rowspan - 1),
								(short) index, (short) index);
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
					// 跨列合并
					if (colspan > 1) {
						CellRangeAddress reg = new CellRangeAddress(
								(short) (i + startRowNo),
								(short) (i + startRowNo), (short) index,
								(short) (index + colspan - 1));
						sheet.addMergedRegion(reg);
						RegionUtil.setBorderLeft(1, reg, sheet, workbook);
						RegionUtil.setBorderBottom(1, reg, sheet, workbook);
						RegionUtil.setBorderRight(1, reg, sheet, workbook);
						RegionUtil.setBorderTop(1, reg, sheet, workbook);
					}
				}
			}
		}

		// 4.创建数据行，已经填充数据，处理数据超出sheet容量的情况
		int sheetSize = data.size();
		// excel2003中每个sheet中最多有65536行,为避免产生错误所以加这个逻辑.
		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 封装表头结束 开始写入数据
		int lastRowNo = startRowNo + data.size();
		double sheetNo = Math.ceil(data.size() / sheetSize);// 取出一共有多少个sheet.
		for (int index = 0; index <= sheetNo; index++) {
			XSSFRow row;
			XSSFCell cell;// 产生单元格
			int startNo = index * sheetSize + headRowSize + startRowNo;
			int endNo = Math.min(startNo + sheetSize, headRowSize + startRowNo
					+ data.size());

			// 写入各条记录,每条记录对应excel表中的一行
			for (int i = startNo; i < endNo; i++) {
				row = sheet.getRow(i);
				JSONObject vo = data.getJSONObject(i
						- (headRowSize + startRowNo)); // 得到导出对象.
				for (int j = gapCol; j < colNum; j++) {
					try {
						cell = row.createCell(j);// 创建cell
						cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						// 如果数据存在就填入,不存在填入空格.
						cell.setCellValue(vo.getString(headIndexMap.get(j)) == null ? ""
								: String.valueOf(vo.getString(headIndexMap
										.get(j))));
						cell.setCellStyle(setBorder);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}

		}

		// 5.合并列数
		if (needMergDataField != null && needMergDataField.length > 0) {
			for (int i = 0; i < needMergDataField.length; i++) {
				String ndnm = needMergDataField[i];
				int colInx = findIndexByList(ndnm, fieldList);
				int startNo = headRowSize;

				int endNo = Math.min(startNo + sheetSize,
						headRowSize + data.size());
				// 写入各条记录,每条记录对应excel表中的一行
				int sr = startNo;
				int er = startNo;
				for (int j = startNo + 1; j < endNo + 1; j++) {
					XSSFRow crow = sheet.getRow(j);
					XSSFRow lrow = sheet.getRow(j - 1);
					boolean bv = false;
					if (crow != null && crow.getCell(colInx) != null) {
						bv = (crow.getCell(colInx).getStringCellValue()
								.equalsIgnoreCase(lrow.getCell(colInx)
										.getStringCellValue()));
					}
					// 如果跟前一行一致 记录索引
					if (bv) {
						er = j;
					} else {
						// 如果跟之前的不一致 判断是否需要进行表格合并
						if (er > sr) {
							// 合并单元格
							CellRangeAddress reg = new CellRangeAddress(
									(short) sr, (short) colInx, (short) er,
									(short) colInx);
							sheet.addMergedRegion(reg);
						}
						sr = j;
						er = j;
					}
				}
			}
		}

		// 6.返回最后一行的行号
		return lastRowNo;
	}
}
