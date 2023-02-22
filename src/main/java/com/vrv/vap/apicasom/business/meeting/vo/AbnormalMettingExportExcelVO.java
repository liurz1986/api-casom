package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.exportAndImport.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/**
 * 异常会议
 * @author vrv
 */
@Data
public class AbnormalMettingExportExcelVO {
    /**
     * 异常名称
     */
    @ExcelField(title = "异常名称", order = 1)
    private String name;
    /**
     * 异常类型
     */
    @ExcelField(title = "异常类型", order = 1)
    private String abnormalType;
    /**
     * 严重等级
     */
    @ExcelField(title = "严重等级", order = 1)
    private String grade;
    /**
     * 故障开始时间
     */
    @ExcelField(title = "故障开始时间", order = 1)
    private Date startTime;
    /**
     * 故障持续时间长度
     */
    @ExcelField(title = "故障持续时间长度", order = 1)
    private String abnormalTime;
}
