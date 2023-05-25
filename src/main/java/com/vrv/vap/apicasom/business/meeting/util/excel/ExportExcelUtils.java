package com.vrv.vap.apicasom.business.meeting.util.excel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * excel导出工具类poi
 *
 * 2023-04-25
 *
 */
public class ExportExcelUtils {
	private static Logger logger = LoggerFactory.getLogger(ExportExcelUtils.class);

	public static ExportExcelUtils getInstance() {
		return new ExportExcelUtils();
	}

	/**
	 * excel导出入口
	 *
	 * @param data 导出数据
	 * @param clazz 导出对象注解类
	 * @param filePath 导出路径
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws FileNotFoundException
	 */
	public void createExcel(List<?> data, Class clazz,String filePath) throws NoSuchFieldException, IllegalAccessException, FileNotFoundException {
		createExcel(data, clazz,null,filePath,false);
	}
	/**
	 * excel导出入口
	 *
	 * @param data 导出数据
	 * @param clazz 导出对象注解类
	 * @param filePath 导出路径
	 * @param isXSSF
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws FileNotFoundException
	 */
	public void createExcel(List<?> data, Class clazz,String filePath,boolean isXSSF) throws NoSuchFieldException, IllegalAccessException, FileNotFoundException {
		createExcel(data, clazz,null,filePath,isXSSF);
	}
	/**
	 * excel导出入口
	 *
	 * @param data 导出数据
	 * @param clazz 导出对象注解类
	 * @param sheetName  sheet页名称
	 * @param filePath 导出路径
	 * @param isXSSF
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws FileNotFoundException
	 */
	public void createExcel(List<?> data, Class clazz,String sheetName,String filePath,boolean isXSSF) throws NoSuchFieldException, IllegalAccessException, FileNotFoundException {
		if(isXSSF){
			filePath = filePath+".xlsx";
		}else{
			filePath = filePath+".xls";
		}
		FileOutputStream fos = new FileOutputStream(filePath);
		Workbook workBook = creatWorkBook(data,clazz,sheetName,isXSSF);
		try{
			workBook.write(fos);
		}catch (Exception e){
			logger.error("生成excle异常",e);
		}finally {
			try{
				workBook.close();
			}catch (Exception e){
				logger.error("workBook关闭异常",e);
			}
			try{
				fos.close();
			}catch (Exception e){
				logger.error("fos关闭异常",e);
			}
		}
	}
	private Workbook creatWorkBook(List<?> data, Class clazz, String sheetName, boolean isXSSF) throws NoSuchFieldException, IllegalAccessException {
		Workbook workbook;
		if (isXSSF) {
			workbook = new XSSFWorkbook();
		} else {
			workbook = new HSSFWorkbook();
		}
		createSheet(workbook,data,clazz,sheetName);
		return workbook;
	}

	/**
	 * 构造sheet数据
	 * @param workbook
	 * @param data
	 * @param clazz
	 * @param sheetName
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private void createSheet(Workbook workbook, List<?> data, Class clazz, String sheetName) throws NoSuchFieldException, IllegalAccessException {
		Sheet sheet;
		if(StringUtils.isEmpty(sheetName)){
			sheet = workbook.createSheet();
		}else{
			sheet = workbook.createSheet(sheetName);
		}
		// 单元格内容显示不下时自动换行配置
		CellStyle style =getCellType(workbook);
		// 构建标题行
		Row titleRow = sheet.createRow(0);
		// 获取表头中字段名并进行排序
		List<String> headers = getHeaders(clazz);
		// 表头赋值
		createTitleRow(clazz,titleRow,headers,sheet,style);
		// 注入数据
		createData(sheet,data,headers,style);
	}

	private CellStyle getCellType(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);
		return style;
	}

	/**
	 * 获取表头中字段名并进行排序
	 * 1. 校验order配置情况，order的值大于0；order不能重复；
	 * @param clazz
	 * @return
	 */
	private List<String> getHeaders(Class clazz) {
        if(null == clazz){
			throw new RuntimeException("clazz is null");
		}
		List<Integer> orders = new ArrayList<>();
		Map<Integer,String> headerMap = new HashMap<>();
		// 所有列
		Field[] cloumns = clazz.getDeclaredFields();
		for(Field field : cloumns){
			boolean isExist = field.isAnnotationPresent(ExportExcelField.class);
			if(!isExist){
				continue;
			}
			ExportExcelField excelField = field.getAnnotation(ExportExcelField.class);
			int order = excelField.order();
			String fieldName = field.getName();
			if(order < 1){
				throw new RuntimeException(fieldName+"中ExportExcelField注解中order的值配置无效，order的值应大于等于1");
			}
			if(orders.contains(order)){
				throw new RuntimeException(fieldName+"中ExportExcelField注解中order配置有误,是否存在重复");
			}
			field.setAccessible(true);
			headerMap.put(order,field.getName());
			orders.add(order);
		}
		if(headerMap.size() == 0){
			throw new RuntimeException(clazz.getSimpleName()+"对象没有配置ExportExcelField注解");
		}
		// 根据order获取title的顺序
		List<String> headers = getHeadersByOrder(headerMap);
		return headers;
	}

	/**
	 * 根据order获取title的顺序
	 *
	 * @param headerMap
	 * @return
	 */
	private List<String> getHeadersByOrder(Map<Integer, String> headerMap) {
		List<String> headers = new ArrayList<>();
		Set<Integer> keys =headerMap.keySet();
		// 升序排列
		Set<Integer> sortSet = new TreeSet<Integer>((o1,o2)-> o2.compareTo(o1));
		sortSet.addAll(keys);
		for(Integer key :keys){
			headers.add(headerMap.get(key));
		}
		return headers;
	}

	/**
	 * 表头赋值：
	 * 将注解中title的值，填充到表头中
	 * @param clazz
	 * @param titleRow
	 * @param headers
	 * @param sheet
	 * @param style
	 * @throws NoSuchFieldException
	 */
	private void createTitleRow(Class clazz,Row titleRow,List<String> headers,Sheet sheet,CellStyle style) throws NoSuchFieldException {
		for(int i=0;i< headers.size();i++){
			Field field = clazz.getDeclaredField(headers.get(i));
			ExportExcelField excelField = field.getAnnotation(ExportExcelField.class);
			Cell cell = titleRow.createCell(i);
			cell.setCellValue(excelField.title());
			cell.setCellStyle(style);
			sheet.setColumnWidth(i, excelField.width()); //设置列宽度
		}
	}

	/**
	 * 注入数据
	 * 单元格内容显示不下自动换行
	 * @param sheet
	 * @param data
	 * @param headers
	 * @param style
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private void createData(Sheet sheet, List<?> data, List<String> headers,CellStyle style) throws NoSuchFieldException, IllegalAccessException {
		if(CollectionUtils.isEmpty(data)){
			return;
		}
		for(int i =0 ;i< data.size();i++){
			Row row = sheet.createRow(i+1);
			for(int y=0;y< headers.size();y++){
				Field field = data.get(i).getClass().getDeclaredField(headers.get(y));
				boolean isExist = field.isAnnotationPresent(ExportExcelField.class);
				if(!isExist){
					continue;
				}
				field.setAccessible(true);
				Object obj = field.get(data.get(i));
				String value = "";
				if(obj instanceof Date){
					value = format((Date)obj);
				}else{
					value = obj == null?"":String.valueOf(obj);
				}
				Cell cell = row.createCell(y);
				cell.setCellValue(value);
				cell.setCellStyle(style);
			}
		}
	}

	private  String format(Date date) {
		SimpleDateFormat formatTool = new SimpleDateFormat();
		formatTool.applyPattern("yyyy-MM-dd HH:mm:ss");
		return formatTool.format(date);
	}
}
