package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelField;
import lombok.Data;

import java.util.Date;

/**
 *
 * 油价导出VO
 * @data 2023-05-23
 *
 */
@Data
public class EmailExportVO {
    @ExportExcelField(title = "部门名称", order = 1,width =40*256)
    private String name;
    @ExportExcelField(title = "时间", order = 2,width =20*256)
    private Date importTime;
    @ExportExcelField(title = "收件数", order =3,width =20*256)
    private Integer receiveNum;
    @ExportExcelField(title = "发件数", order =4,width =20*256)
    private Integer sendNum;
}
