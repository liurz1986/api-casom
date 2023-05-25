package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.List;

/**
 * 公文及文件交换系统发件数量、收件数量vo
 */
@Data
public class FileSendAndReceiveNumVO {
    // 发件
    private List<KeyValueQueryVO> send;
    // 收件
    private List<KeyValueQueryVO> receive;
}
