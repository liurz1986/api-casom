package com.vrv.vap.apicasom.business.task.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column(name="participant_name")
    private String participantName;

    @Column(name="city")
    private String city;

    @Column(name="branch")
    private String branch;
}
