package com.vrv.vap.apicasom.business.meeting.vo;

import com.vrv.vap.exportAndImport.excel.annotation.ExcelField;
import lombok.Data;

import java.util.Date;

/**
 * 节点配置数据
 * @author liurz
 */
@Data
public class ZkyUnitExportExcelVO {

    @ExcelField(title = "节点code", order = 1)
    private String  participantCode;

    @ExcelField(title = "节点名称", order = 2)
    private String participantName;

    @ExcelField(title = "城市", order = 3)
    private String city ;

    @ExcelField(title = "分院", order = 4)
    private String  branch ;
}
