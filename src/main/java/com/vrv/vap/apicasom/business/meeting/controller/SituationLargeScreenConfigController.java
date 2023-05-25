package com.vrv.vap.apicasom.business.meeting.controller;
import com.vrv.vap.apicasom.business.meeting.service.ZkyEmailService;
import com.vrv.vap.apicasom.business.meeting.service.ZkyExchangeBoxService;
import com.vrv.vap.apicasom.business.meeting.service.ZkyPrintUserOrgService;
import com.vrv.vap.apicasom.business.meeting.vo.PrintUserOrgSerachVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyEmailSearchVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyExchangeBoxSearchVO;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyEmail;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyExchangeBox;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.jpa.common.FileUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import com.vrv.vap.syslog.common.annotation.SysRequestLog;
import com.vrv.vap.syslog.common.enums.ActionType;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

/**
 * 态势应用大屏涉及配置信息接口
 *  1. 邮件导入
 *  2. 公文文件导入
 *  3. 打印用户机构数据导入及编辑、删除
 *
 * @author liurz
 * @data 2023-05-25
 */
@RestController
@RequestMapping(value = "/situationConfig")
public class SituationLargeScreenConfigController {
    private static Logger logger = LoggerFactory.getLogger(SituationLargeScreenConfigController.class);

    @Autowired
    private FileConfiguration fileConfiguration;
    @Autowired
    private ZkyEmailService zkyEmailService;
    @Autowired
    private ZkyExchangeBoxService zkyExchangeBoxService;
    @Autowired
    private ZkyPrintUserOrgService zkyPrintUserOrgService;
    /**
     * 邮件收发导入模板生成
     * @return Result
     */
    @PostMapping(value = "/emailTemplate")
    @ApiOperation(value = "邮件收发导入模板生成", notes = "")
    @SysRequestLog(description = "邮件收发导入模板生成", actionType = ActionType.SELECT)
    public Result<String> emailTemplate() {
        try {
            return zkyEmailService.emailTemplate();
        } catch (Exception e) {
            logger.error("邮件导入模板生成异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "邮件收发导入模板生成异常");
        }
    }
    /**
     * 导入邮件收发文件
     * @param file
     */
    @PostMapping(value="importEmailFile")
    @SysRequestLog(description="导入邮件收发文件", actionType = ActionType.IMPORT,manually=false)
    @ApiOperation(value="导入邮件收发文件",notes="")
    public Result<String> importEmailFile(@RequestParam("file") MultipartFile file){
        try{
            return zkyEmailService.importEmailFile(file);
        }catch (Exception e){
            logger.error("导入邮件收发文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入邮件收发文件异常");
        }

    }
    /**
     * 邮件收发展示
     * {"orgName": "上海微系统与信息技术研究所","start_": 0,"count_": 6}
     * @param searchVO
     */
    @PostMapping(value="emailGetPage")
    @SysRequestLog(description="邮件收发展示", actionType = ActionType.SELECT,manually=false)
    @ApiOperation(value="邮件收发展示",notes="")
    public PageRes<ZkyEmail> emailGetPage(@RequestBody ZkyEmailSearchVO searchVO){
        return zkyEmailService.emailGetPage(searchVO);
    }

    /**
     * 邮件删除
     * {"guid": "439f99d3693a4602b73869733e2d981a"}
     * @return Result
     */
    @PostMapping(value = "/emailDelete")
    @ApiOperation(value = "邮件删除", notes = "")
    @SysRequestLog(description = "邮件删除", actionType = ActionType.SELECT)
    public Result<String> emailDelete(@RequestBody ZkyEmail zkyEmail) {
        try {
            String guid = zkyEmail.getGuid();
            if(StringUtils.isEmpty(guid)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"删除guid不能为空");
            }
            zkyEmailService.emailDelete(guid);
            return ResultUtil.success("success");
        } catch (Exception e) {
            logger.error("公文文件导入模板生成异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "邮件删除异常");
        }
    }


    /**
     * 公文交换箱文件模板生成
     * @return Result
     */
    @PostMapping(value = "/documentTemplate")
    @ApiOperation(value = "公文交换箱文件模板生成", notes = "")
    @SysRequestLog(description = "公文交换箱文件模板生成", actionType = ActionType.SELECT)
    public Result<String> fileTemplate() {
        try {
            return zkyExchangeBoxService.documentTemplate();
        } catch (Exception e) {
            logger.error("公文文件导入模板生成异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "公文交换箱文件模板生成异常");
        }
    }

    /**
     * 导入公文交换箱文件
     * @param file
     */
    @PostMapping(value="importDocumentFile")
    @SysRequestLog(description="导入公文交换箱文件", actionType = ActionType.IMPORT,manually=false)
    @ApiOperation(value="导入公文交换箱文件",notes="")
    public Result<String> importDocumentFile(@RequestParam("file") MultipartFile file){
        try{
            return zkyExchangeBoxService.importDocumentFile(file);
        }catch (Exception e){
            logger.error("导入公文交换箱文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入公文交换箱文件异常");
        }

    }

    /**
     * 公文交换箱展示
     * @param searchVO
     */
    @PostMapping(value="documentGetPage")
    @SysRequestLog(description="公文交换箱展示", actionType = ActionType.SELECT,manually=false)
    @ApiOperation(value="公文交换箱展示",notes="")
    public PageRes<ZkyExchangeBox> documentGetPage(@RequestBody ZkyExchangeBoxSearchVO searchVO){
        return zkyExchangeBoxService.documentGetPage(searchVO);
    }

    /**
     * 公文交换箱删除
     * @return Result
     */
    @PostMapping(value = "/documentDelete")
    @ApiOperation(value = "公文交换箱删除", notes = "")
    @SysRequestLog(description = "公文交换箱删除", actionType = ActionType.DELETE)
    public Result<String> documentDelete(@RequestBody ZkyExchangeBox zkyExchangeBox) {
        try {
            String guid = zkyExchangeBox.getGuid();
            if(StringUtils.isEmpty(guid)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"删除guid不能为空");
            }
            zkyExchangeBoxService.documentDelete(guid);
            return ResultUtil.success("success");
        } catch (Exception e) {
            logger.error("公文交换箱删除异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "公文交换箱删除异常");
        }
    }
    /**
     * 打印用户机构数据导入模板生成
     * @return Result
     */
    @PostMapping(value = "/printUserOrgTemplate")
    @ApiOperation(value = "打印用户机构数据导入模板生成", notes = "")
    @SysRequestLog(description = "打印用户机构数据导入模板生成", actionType = ActionType.SELECT)
    public Result<String> printUserOrgTemplate() {
        try {
            return zkyPrintUserOrgService.printUserOrgTemplate();
        } catch (Exception e) {
            logger.error("打印用户机构数据导入模板生成异常,{}", e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(), "打印用户机构数据导入模板生成异常");
        }
    }

    /**
     * 导入打印用户机构数据文件
     * @param file
     */
    @PostMapping(value="importprintUserOrgFile")
    @SysRequestLog(description="导入打印用户机构数据文件", actionType = ActionType.IMPORT,manually=false)
    @ApiOperation(value="导入打印用户机构数据文件",notes="")
    public Result<String> importprintUserOrgFile(@RequestParam("file") MultipartFile file){
        try{
            return zkyPrintUserOrgService.importprintUserOrgFile(file);
        }catch (Exception e){
            logger.error("导入打印用户机构数据文件异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入打印用户机构数据文件异常");
        }

    }

    /**
     * 打印用户机构数据查询
     * @param printUserOrgSerachVO
     */
    @PostMapping(value="printUserOrgGetPage")
    @SysRequestLog(description="打印用户机构数据查询", actionType = ActionType.SELECT,manually=false)
    @ApiOperation(value="打印用户机构数据查询",notes="")
    public PageRes<ZkyPrintUserOrg> printUserOrgGetPage(@RequestBody PrintUserOrgSerachVO printUserOrgSerachVO){
        return zkyPrintUserOrgService.printUserOrgGetPage(printUserOrgSerachVO);
    }
    /**
     * 打印用户机构数据编辑
     * @param printUserOrg
     */
    @PostMapping(value="printUserOrgEdit")
    @SysRequestLog(description="打印用户机构数据编辑", actionType = ActionType.UPDATE,manually=false)
    @ApiOperation(value="打印用户机构数据编辑",notes="")
    public Result<String> printUserOrgEdit(@RequestBody ZkyPrintUserOrg printUserOrg){
        try{
            return zkyPrintUserOrgService.printUserOrgEdit(printUserOrg);
        }catch (Exception e){
            logger.error("打印用户机构数据编辑异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"打印用户机构数据编辑异常");
        }
    }

    /**
     * 打印用户机构数据删除
     * @param printUserOrg
     */
    @PostMapping(value="printUserOrgDel")
    @SysRequestLog(description="打印用户机构数据删除", actionType = ActionType.DELETE,manually=false)
    @ApiOperation(value="打印用户机构数据删除",notes="")
    public Result<String> printUserOrgDel(@RequestBody ZkyPrintUserOrg printUserOrg){
        try{
            String guid = printUserOrg.getGuid();
            if(StringUtils.isEmpty(guid)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"删除guid不能为空");
            }
            zkyPrintUserOrgService.printUserOrgDel(guid);
            return ResultUtil.success("success");
        }catch (Exception e){
            logger.error("打印用户机构数据删除异常",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"打印用户机构数据删除异常");
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
    public void exportAssetInfo(@PathVariable  String fileName, HttpServletResponse response){
        FileUtil.downLoadFile(fileName+ ".xls", fileConfiguration.getTemplatePath(), response);
    }
}
