package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelField;
import lombok.Data;


/**
 *
 * 打印用户机构数据导出VO
 * @data 2023-05-23
 *
 */
@Data
public class PrintUserOrgExportVO {
    @ExportExcelField(title = "用户名", order = 1,width =20*256)
    private String userName;
    @ExportExcelField(title = "分院(地区)", order = 2,width =20*256)
    private String branch;
    @ExportExcelField(title = "单位/部门", order =3,width =40*256)
    private String organizationName;

}
