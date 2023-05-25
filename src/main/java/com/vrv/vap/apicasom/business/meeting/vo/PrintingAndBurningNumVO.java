package com.vrv.vap.apicasom.business.meeting.vo;

import lombok.Data;

/**
 * 打印和刻录数量vo
 */
@Data
public class PrintingAndBurningNumVO {
    private String name;

    private int printingNum;  //打印数量

    private int burningNum; //刻录数量
}
