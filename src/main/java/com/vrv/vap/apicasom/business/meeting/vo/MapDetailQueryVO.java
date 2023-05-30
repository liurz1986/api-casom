package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

@Data
public class MapDetailQueryVO {
    private String  orgName;
    private String sendRegion;
    private String sendType;
    private int receiveNum;
    private int sendNum;
}
