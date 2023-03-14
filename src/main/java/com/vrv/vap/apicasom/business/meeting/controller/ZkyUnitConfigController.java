package com.vrv.vap.apicasom.business.meeting.controller;

import com.vrv.vap.apicasom.business.meeting.service.ZkyUnitConfigService;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitSerachVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitVO;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 节点基础数据配置
 *
 * @author liurz
 * @Date 202303
 */
@RestController
@RequestMapping("zkyUnitConfig")
public class ZkyUnitConfigController {
    private static Logger logger = LoggerFactory.getLogger(ZkyUnitConfigController.class);

    @Autowired
    private ZkyUnitConfigService zkyUnitConfigService;

    /**
     * 节点基础数据配置查询
     * 节点名称、城市、分院
     * @param zkyUnitSerachVO
     * @return
     */
    @PostMapping("getPage")
    @SysRequestLog(description="节点基础数据配置查询", actionType = ActionType.ADD,manually = false)
    @ApiOperation(value="节点基础数据配置查询",notes="")
    public PageRes<ZkyUnitBean> getPage(@RequestBody ZkyUnitSerachVO zkyUnitSerachVO){
        PageRes<ZkyUnitBean> result = new PageRes<>();
        try{
            return zkyUnitConfigService.getPage(zkyUnitSerachVO);
        }catch (Exception e){
            logger.error("节点基础数据配置查询异常",e);
            result.setCode(ResultCodeEnum.UNKNOW_FAILED.getCode().toString());
            result.setMessage("节点基础数据配置查询异常");
            return result;
        }
    }
    /**
     * 节点基础数据配置新增
     * 1.必填校验：节点名称、节点code、城市、分院
     * 2. 节点code唯一性校验
     * @param zkyUnitVO
     * @return
     */
    @PostMapping("save")
    @SysRequestLog(description="节点基础数据配置新增", actionType = ActionType.ADD,manually = false)
    @ApiOperation(value="节点基础数据配置新增",notes="")
    public Result<String> save(@RequestBody ZkyUnitVO zkyUnitVO){
        try{
            return zkyUnitConfigService.save(zkyUnitVO);
        }catch (Exception e){
            logger.error("节点基础数据配置新增异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点基础数据配置新增异常");
        }
    }

    /**
     * 节点基础数据配置修改
     * 1.必填校验：节点名称、节点code、城市、分院
     * 2.节点code唯一性校验
     * @param zkyUnitVO
     * @return
     */
    @PostMapping("update")
    @SysRequestLog(description="节点基础数据配置修改", actionType = ActionType.UPDATE,manually = false)
    @ApiOperation(value="节点基础数据配置修改",notes="")
    public Result<String> update(@RequestBody ZkyUnitVO zkyUnitVO){
        try{
            return zkyUnitConfigService.update(zkyUnitVO);
        }catch (Exception e){
            logger.error("节点基础数据配置修改异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点基础数据配置修改异常");
        }
    }

    /**
     * 节点基础数据配置删除
     * @param guid
     * @return
     */
    @DeleteMapping("{guid}")
    @SysRequestLog(description="节点基础数据配置删除", actionType = ActionType.DELETE,manually = false)
    @ApiOperation(value="节点基础数据配置删除",notes="")
    public Result<String> delete(@PathVariable("guid") String guid){
        try{
            zkyUnitConfigService.delete(guid);
            return ResultUtil.success("success");
        }catch (Exception e){
            logger.error("节点基础数据配置删除异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点基础数据配置删除异常");
        }
    }

    /**
     * 生成节点基础数据配置导出模板
     * @param
     * @return
     */
    @PostMapping("exportDataTemplate")
    @SysRequestLog(description="生成节点基础数据配置导出模板", actionType = ActionType.EXPORT,manually = false)
    @ApiOperation(value="生成节点基础数据配置导出模板",notes="")
    public Result<String> exportDataTemplate(){
        try{
            return zkyUnitConfigService.exportDataTemplate();
        }catch (Exception e){
            logger.error("生成节点基础数据配置导出模板文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成节点基础数据配置导出模板文件异常");
        }
    }
    /**
     * 生成节点基础数据配置导出数据
     * @param zkyUnitSerachVO
     * @return
     */
    @PostMapping("exportData")
    @SysRequestLog(description="生成节点基础数据配置导出数据", actionType = ActionType.EXPORT,manually = false)
    @ApiOperation(value="生成节点基础数据配置导出数据",notes="")
    public Result<String> exportData(@RequestBody ZkyUnitSerachVO zkyUnitSerachVO){
        try{
            return zkyUnitConfigService.exportData(zkyUnitSerachVO);
        }catch (Exception e){
            logger.error("生成节点基础数据配置导出数据异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成节点基础数据配置导出数据异常");
        }
    }
    /**
     * 下载导出文件
     * @param fileName
     * @param response
     */
    @GetMapping(value="/exportFile/{fileName}")
    @SysRequestLog(description="下载导出文件", actionType = ActionType.EXPORT,manually=false)
    @ApiOperation(value="下载导出文件",notes="")
    public void exportFileInfo(@PathVariable  String fileName, HttpServletResponse response){
        zkyUnitConfigService.exportFileInfo(fileName,response);
    }

    /**
     * 上传节点基础数据配置文件
     * @param file
     */
    @PostMapping(value="importFile")
    @SysRequestLog(description="上传节点基础数据配置文件", actionType = ActionType.EXPORT,manually=false)
    @ApiOperation(value="上传节点基础数据配置文件",notes="")
    public Result<String> importFileInfo(@RequestParam("file") MultipartFile file){
        try{
            return zkyUnitConfigService.importFileInfo(file);
        }catch (Exception e){
            logger.error("上传节点基础数据配置文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"上传节点基础数据配置文件异常");
        }

    }
}
