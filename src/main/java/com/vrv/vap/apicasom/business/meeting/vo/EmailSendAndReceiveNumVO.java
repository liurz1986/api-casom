package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

import java.util.List;

/**
 * 院机关各部门邮件收发数量
 * @author vrv
 */
@Data
public class EmailSendAndReceiveNumVO {

    private int sendTotal;  //发件总数
    private int receiveTotal; //收件总数
    private List<KeyValueQueryVO> sendTop; // 发件详情
    private List<KeyValueQueryVO> receiveTop; // 收件详情
}
