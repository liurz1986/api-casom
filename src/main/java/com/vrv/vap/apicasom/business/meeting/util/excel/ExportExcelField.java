package com.vrv.vap.apicasom.business.meeting.util.excel;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excel导出注解类
 *
 * title: excel为excel的表头
 * order: 取值从1开始，不能重复
 * width：单元格的宽度
 *
 * title和order必填
 * 2023-4-25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExportExcelField {

    String title();

    int order() ;

    int width() default 20*256;


}
