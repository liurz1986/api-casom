package com.vrv.vap.apicasom.business.meeting.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入Excel
 * 
 * @author wd-pc
 *
 */
public class ImportExcelUtil {

	private static boolean isMergedRegion(Sheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获得ExcelTitle（标题）
	 * 
	 * @param filePath
	 * @return
	 */
	public static Map<String, List<String>> getExcelTitle(HSSFWorkbook workbook) {
		Map<String, List<String>> map = new HashMap<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			HSSFSheet sheet = workbook.getSheetAt(i);
			int startIndex=0;
			if(sheet.isColumnHidden(i)){ // 隐藏sheet不处理
				continue;
			}
			while(isMergedRegion(sheet,startIndex,0)) {
				startIndex++;
			}
			
			int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); // 获得行数
			if (physicalNumberOfRows > startIndex) {
				List<String> list = new ArrayList<>();
				String sheetName = sheet.getSheetName();
				HSSFRow row = sheet.getRow(startIndex); // title
				for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {// 获取每个单元格
					String titleName = row.getCell(k).toString();
					list.add(titleName);
				}
				map.put(sheetName, list);
			}
		}
		return map;
	}
	
	public static Map<String, List<String>> getExcelTitle(XSSFWorkbook workbook) {
		Map<String, List<String>> map = new HashMap<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			XSSFSheet sheet = workbook.getSheetAt(i);
			int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); // 获得行数
			if (physicalNumberOfRows > 0) {
				List<String> list = new ArrayList<>();
				String sheetName = sheet.getSheetName();
				XSSFRow row = sheet.getRow(0); // title
				for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {// 获取每个单元格
					String titleName = row.getCell(k).toString();
					list.add(titleName);
				}
				map.put(sheetName, list);
			}
		}
		return map;
	}

	public static Map<String, List<List<String>>> getExcelContent(HSSFWorkbook workbook) {
		Map<String, List<List<String>>> map = new HashMap<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) { // 获得多个sheet
			HSSFSheet sheet = workbook.getSheetAt(i);
			int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); // 获得行数
			if(sheet.isColumnHidden(i)){ // 隐藏sheet不处理
				continue;
			}
			int startIndex=0;
			while(isMergedRegion(sheet,startIndex,0)) {
				startIndex++;
			}

			if (physicalNumberOfRows > startIndex) {
				List<List<String>> parentList = new ArrayList<>();
				String sheetName = sheet.getSheetName();
				HSSFRow firstRow = sheet.getRow(0);
				for (int j = startIndex+1; j < physicalNumberOfRows; j++) { // 从第一行开始获得每个sheet当中的数据
					List<String> list = new ArrayList<>();
					HSSFRow row = sheet.getRow(j);
					//for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {// 获取每个单元格
					
					if(row==null) {   // 中间空行后面数据读不全处理
						physicalNumberOfRows ++;
						continue;
					}

					for (int k = 0; k < firstRow.getLastCellNum(); k++) {// 获取每个单元格
						HSSFCell cell = row.getCell(k);

						
						if(cell!=null){
							if(cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
								cell.setCellType(CellType.STRING);
							}
							list.add(cell.toString());
						}else{
							list.add("");
						}
					}
					parentList.add(list);
				}
				map.put(sheetName, parentList);
			}
		}
		return map;
	}

	public static  List<List<String>> getExcelContent(HSSFSheet sheet) {
		   List<List<String>> parentList = new ArrayList<>();
			int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); // 获得行数
			int startIndex=0;
			while(isMergedRegion(sheet,startIndex,0)) {
				startIndex++;
			}
			if (physicalNumberOfRows > startIndex) {
				HSSFRow firstRow = sheet.getRow(0);
				for (int j = startIndex+1; j < physicalNumberOfRows; j++) { // 从第一行开始获得每个sheet当中的数据
					List<String> list = new ArrayList<>();
					HSSFRow row = sheet.getRow(j);
					if(row==null) {   // 中间空行后面数据读不全处理
						physicalNumberOfRows ++;
						continue;
					}
					for (int k = 0; k < firstRow.getLastCellNum(); k++) {// 获取每个单元格
						HSSFCell cell = row.getCell(k);
						if(cell!=null){
							if(cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
								cell.setCellType(CellType.STRING);
							}
							list.add(cell.toString());
						}else{
							list.add("");
						}
					}
					parentList.add(list);
				}
			}
		return parentList;
	}
	
	public static Map<String, List<List<String>>> getExcelContent(XSSFWorkbook workbook) {
		Map<String, List<List<String>>> map = new HashMap<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) { // 获得多个sheet
			XSSFSheet sheet = workbook.getSheetAt(i);
			int physicalNumberOfRows = sheet.getPhysicalNumberOfRows(); // 获得行数
			if (physicalNumberOfRows > 0) {
				List<List<String>> parentList = new ArrayList<>();
				String sheetName = sheet.getSheetName();
				for (int j = 1; j < physicalNumberOfRows; j++) { // 从第一行开始获得每个sheet当中的数据
					List<String> list = new ArrayList<>();
					XSSFRow row = sheet.getRow(j);
					//for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {// 获取每个单元格
					for (int k = 0; k < row.getLastCellNum(); k++) {// 获取每个单元格
						XSSFCell cell = row.getCell(k);

						
						if(cell!=null){
							if(cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
						
								cell.setCellType(CellType.STRING);
							}
							list.add(cell.toString());
						}else{
							list.add("");
						}
					}
					parentList.add(list);
				}
				map.put(sheetName, parentList);
			}
		}
		return map;
	}

}
