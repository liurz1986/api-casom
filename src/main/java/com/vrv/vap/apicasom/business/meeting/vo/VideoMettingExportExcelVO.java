package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.exportAndImport.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/**
 * 历史会议列表导出
 * @author liurz
 */
@Data
public class VideoMettingExportExcelVO {

    @ExcelField(title = "会议日期", order = 1)
    private Date  meetingDate;

    @ExcelField(title = "会议时间", order = 2)
    private String meetingTime;

    @ExcelField(title = "参会节点", order = 3)
    private String nodeNames ;

    @ExcelField(title = "参会单位", order = 4)
    private String  companys ;

    @ExcelField(title = "参会节点数", order = 5)
    private String  nodeNumber ;

    @ExcelField(title = "参数人数", order = 6)
    private String  peopleNumber ;

}
