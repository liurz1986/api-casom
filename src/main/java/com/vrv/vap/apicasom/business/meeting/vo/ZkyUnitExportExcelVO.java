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

    @ExcelField(title = "研究所名称", order = 1)
    private String name;

    @ExcelField(title = "节点名称", order = 2)
    private String participantName;

    @ExcelField(title = "节点编号", order = 3)
    private String  participantCode;

    @ExcelField(title = "分院/地区", order = 4)
    private String  branch ;

    @ExcelField(title = "城市", order = 5)
    private String city ;


}
