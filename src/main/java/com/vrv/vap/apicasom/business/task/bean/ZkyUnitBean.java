package com.vrv.vap.apicasom.business.task.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author: 梁国露
 * @since: 2023/2/21 15:44
 * @description:
 */
@Data
@Entity
@Table(name = "zky_unit")
public class ZkyUnitBean {
    @Id
    @Column(name="id")
    private String id;

    /**
     * 研究所名称
     */
    @Column(name="name")
    private String name;
    /**
     * 节点编号
     */
    @Column(name="participant_code")
    private String participantCode;

    @Column(name="participant_name")
    private String participantName;

    @Column(name="city")
    private String city;

    @Column(name="branch")
    private String branch;

    @Column(name="create_time")
    private Date createTime;
}
