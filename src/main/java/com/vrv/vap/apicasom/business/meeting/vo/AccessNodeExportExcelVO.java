package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.exportAndImport.excel.annotation.ExcelField;
import lombok.Data;

/**
 * 接入节点列表导出列表
 * @author vrv
 */
@Data
public class AccessNodeExportExcelVO {

    @ExcelField(title = "节点名称", order = 1)
    private String nodeName;

    @ExcelField(title = "会议次数", order = 2)
    private String meetingCount;

    @ExcelField(title = "会议时长", order = 3)
    private String meetingTimeTotal;

    @ExcelField(title = "所属分院/地区", order = 4)
    private String region;

    @ExcelField(title = "设备型号", order = 5)
    private String  assetType ;


}
