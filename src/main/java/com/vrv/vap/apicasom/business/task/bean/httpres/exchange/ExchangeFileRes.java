package com.vrv.vap.apicasom.business.task.bean.httpres.exchange;

import com.vrv.vap.apicasom.business.task.bean.ExchangeBoxFile;
import lombok.Data;

/**
 * @author: 梁国露
 * @since: 2023/5/10 14:53
 * @description:
 */
@Data
public class ExchangeFileRes {
    private String responseStatus;
    private String status;
    private String message;
    private ExchangeBoxFile data;
}
