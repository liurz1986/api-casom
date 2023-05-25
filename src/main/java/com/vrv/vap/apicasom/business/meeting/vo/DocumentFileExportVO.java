package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelField;
import lombok.Data;


/**
 *
 * 公文文件导出VO
 * @data 2023-05-23
 *
 */
@Data
public class DocumentFileExportVO {
    @ExportExcelField(title = "属性名称", order = 1,width =40*256)
    private String name;
    @ExportExcelField(title = "属性值", order = 2,width =20*256)
    private String value;
}
