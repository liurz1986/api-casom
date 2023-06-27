package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.task.service.ZkySendDataService;
import com.vrv.vap.jpa.common.DateUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 手动补全文件数据
 *
 * @author liurz
 * @Date 202306
 */

@RestController
@RequestMapping("zkySend")
public class ZkySendController {
    private static Logger logger = LoggerFactory.getLogger(ZkySendController.class);

    @Autowired
    private ZkySendDataService zkySendDataService;
    /**
     * 执行手动补全
     * zkySend/completion?time=2023-05-08
     * @return
     */
    @GetMapping("completion")
    @SysRequestLog(description="执行手动补全收发件信息", actionType = ActionType.ADD,manually = false)
    @ApiOperation(value="执行手动补全收发件信息",notes="")
    public Result<Boolean> completion(@RequestParam("time") String time){
        Result<Boolean> result = new Result<>();
        try{
            logger.info("执行手动补全收发件信息");
            if(StringUtils.isEmpty(time)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"时间不能为空");
            }
            if(!DateParseStatus(time)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"时间格式(yyyy-MM-dd)不正确，"+time);
            }
            String startTime = time+" 00:00:00";
            String endTime = time+" 23:59:59";
            zkySendDataService.dataSyncHandle(endTime,startTime);
            return ResultUtil.success(true);
        }catch (Exception e){
            logger.error("执行手动补全收发件信息异常",e);
            result.setCode(ResultCodeEnum.UNKNOW_FAILED.getCode());
            result.setMsg("执行手动补全收发件信息异常");
            return result;
        }
    }

    private boolean DateParseStatus(String startTime) {
        try{
            DateUtil.parseDate(startTime,DateUtil.Year_Mouth_Day);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
