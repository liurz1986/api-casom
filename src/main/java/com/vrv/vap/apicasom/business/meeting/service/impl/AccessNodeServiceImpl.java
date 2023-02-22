package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.dao.AccessNodeDao;
import com.vrv.vap.apicasom.business.meeting.service.AccessNodeService;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.ExcelUtils;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.common.FileUtil;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 接入节点列表
 * @author liurz
 */
@Service
public class AccessNodeServiceImpl implements AccessNodeService {
    private static Logger logger = LoggerFactory.getLogger(AccessNodeServiceImpl.class);

    @Autowired
    private AccessNodeDao accessNodeDao;
    @Autowired
    private FileConfiguration fileConfiguration;

    /**
     * 接入节点列表查询
     * @param accessNodeSearchVO
     * @return
     */
    @Override
    public PageRes<AccessNodeVO> getPage(AccessNodeSearchVO accessNodeSearchVO) {
        logger.debug("接入节点列表查询请求参数："+ JSON.toJSONString(accessNodeSearchVO));
        PageRes<AccessNodeVO> data =new PageRes<>();
        // 获取总数
        long totalNum = accessNodeDao.getPageTotal(accessNodeSearchVO);
        // 获取分页数据
        List<AccessNodeVO> list = accessNodeDao.getPageList(accessNodeSearchVO);
        data.setTotal(totalNum);
        data.setList(list);
        data.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        data.setCode(ResultCodeEnum.SUCCESS.getCode().toString());
        return data;
    }

    /**
     * 接入节点列表导出
     * @param accessNodeSearchVO
     * @return
     */
    @Override
    public Result<String> exportData(AccessNodeSearchVO accessNodeSearchVO) {
        logger.debug("接入节点列表导出请求参数："+ JSON.toJSONString(accessNodeSearchVO));
        String filePath = fileConfiguration.getFilePath();
        List<AccessNodeExportExcelVO> lists = accessNodeDao.exportData(accessNodeSearchVO);
        File newFile= new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String uuid="接入节点列表"+ DateUtils.date2Str(new Date(), "yyyyMMddHHmmss")+".xlsx";
        logger.info("fileName: "+uuid);
        filePath=filePath+ File.separator+uuid;
        try{
            ExcelUtils.getInstance().exportObjects2Excel(lists, AccessNodeExportExcelVO.class, true, filePath);
        }catch(Exception e){
            logger.error("生成接入节点列表数据导出文件导出失败",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成接入节点列表数据导出文件失败");
        }
        return ResultUtil.success(uuid);
    }

    @Override
    public void exportAssetInfo(String fileName, HttpServletResponse response) {
        FileUtil.downLoadFile(fileName, fileConfiguration.getFilePath(), response);
    }
}
