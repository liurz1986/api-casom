package com.vrv.vap.apicasom.business.task.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author: 梁国露
 * @since: 2023/2/22 10:03
 * @description:
 */
@Data
@Entity
@Table(name = "hw_sync_error_log")
public class HwSyncErrorLog {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="error_method")
    private String errorMethod;

    @Column(name="error_param")
    private String errorParam;

    @Column(name="error_msg")
    private String errorMsg;

    @Column(name = "error_time")
    private Date errorTime;
}
