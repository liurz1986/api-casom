package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.AccessNodeService;
import com.vrv.vap.apicasom.business.meeting.vo.AccessNodeSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.AccessNodeVO;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;


/**
 * 接入节点
 * @author vrv
 */
@RestController
@RequestMapping("accessNode")
public class AccessNodeController {
    private static Logger logger = LoggerFactory.getLogger(AccessNodeController.class);

    @Autowired
    private AccessNodeService accessNodeService;

    /**
     * 接入节点列表展示
     */
    @PostMapping("getPage")
    @SysRequestLog(description="接入节点列表展示", actionType = ActionType.SELECT,manually = false)
    @ApiOperation(value="接入节点列表展示",notes="")
    public PageRes<AccessNodeVO> getPage(@RequestBody AccessNodeSearchVO accessNodeSearchVO){
       return accessNodeService.getPage(accessNodeSearchVO);
    }
    /**
     * 接入节点列表导出
     */
    @PostMapping("exportData")
    @SysRequestLog(description="生成接入节点列表导出文件", actionType = ActionType.EXPORT,manually = false)
    @ApiOperation(value="生成接入节点列表导出文件",notes="")
    public Result<String>  exportData(@RequestBody AccessNodeSearchVO accessNodeSearchVO){
        try{
            return accessNodeService.exportData(accessNodeSearchVO);
        }catch(Exception e){
            logger.error("生成接入节点列表导出文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成接入节点列表导出文件异常");
        }
    }

    /**
     * 下载接入节点列表导出文件
     * @param fileName
     * @param response
     */
    @GetMapping(value="/exportFile/{fileName}")
    @SysRequestLog(description="下载接入节点列表导出文件", actionType = ActionType.EXPORT,manually=false)
    @ApiOperation(value="下载接入节点列表导出文件",notes="")
    public void exportAssetInfo(@PathVariable  String fileName, HttpServletResponse response){
        accessNodeService.exportAssetInfo(fileName,response);
    }
}
