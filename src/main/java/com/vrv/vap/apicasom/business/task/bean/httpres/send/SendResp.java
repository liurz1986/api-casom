package com.vrv.vap.apicasom.business.task.bean.httpres.send;

import com.vrv.vap.apicasom.business.task.bean.ZkySend;
import lombok.Data;

import java.util.List;

/**
 * @author: 梁国露
 * @since: 2023/3/8 18:37
 * @description:
 */
@Data
public class SendResp {
    private List<ZkySend> data;
}
