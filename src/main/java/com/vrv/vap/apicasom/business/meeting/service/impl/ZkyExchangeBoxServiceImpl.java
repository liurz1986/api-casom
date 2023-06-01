package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.vrv.vap.apicasom.business.meeting.bean.ZkyExchangeBox;
import com.vrv.vap.apicasom.business.meeting.repository.ZkyExchangeBoxRepository;
import com.vrv.vap.apicasom.business.meeting.service.ZkyExchangeBoxService;
import com.vrv.vap.apicasom.business.meeting.util.ImportExcelUtil;
import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelUtils;
import com.vrv.vap.apicasom.business.meeting.vo.DocumentFileExportVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyExchangeBoxSearchVO;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.exception.ExcelException;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.web.Result;
import com.vrv.vap.jpa.web.ResultCodeEnum;
import com.vrv.vap.jpa.web.ResultUtil;
import com.vrv.vap.jpa.web.page.PageReq;
import com.vrv.vap.jpa.web.page.PageRes;
import com.vrv.vap.jpa.web.page.QueryCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 公文交换箱
 *
 * 2023-5-25
 */
@Service
public class ZkyExchangeBoxServiceImpl extends BaseServiceImpl<ZkyExchangeBox,String> implements ZkyExchangeBoxService {
    private static Logger logger = LoggerFactory.getLogger(ZkyExchangeBoxServiceImpl.class);
    @Autowired
    private FileConfiguration fileConfiguration;
    @Autowired
    private ZkyExchangeBoxRepository zkyExchangeBoxRepository;

    private String zkyExchangeBoxSheetName="公文交换箱";

    @Override
    public BaseRepository<ZkyExchangeBox, String> getRepository() {
        return zkyExchangeBoxRepository;
    }
    /**
     * excel展示行中第一列
     * @return
     */
    private List<String> getDocumentCol(){
        List<String> cols = new ArrayList<>();
        cols.add("截止时间");
        cols.add("收件总数");
        cols.add("收件流转总数");
        cols.add("收件登记总数");
        cols.add("签批件总数");
        cols.add("签批件流转总数");
        cols.add("签批件登记总数");
        cols.add("密刊总数");
        cols.add("密刊流转总数");
        cols.add("密刊登记总数");
        cols.add("总用户数");
        cols.add("用户登录次数");
        return cols;
    }

    /**
     * 公文文件导入模板生成
     * @return Result
     */
    @Override
    public Result<String> documentTemplate() {
        String fileName = "公文交换箱模板" + DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        String rootPath = fileConfiguration.getTemplatePath();
        File targetFile = new File(rootPath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String filePath = Paths.get(rootPath, fileName).toString();
        try {
            List<DocumentFileExportVO> datas =getDocumentFilTemplate();
            ExportExcelUtils.getInstance().createExcel(datas, DocumentFileExportVO.class, zkyExchangeBoxSheetName,filePath,false);
            return ResultUtil.success(fileName);
        } catch (ExcelException | IOException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("公文交换箱模板导出excel异常", e);
            return ResultUtil.error(-1,"导出excel异常");
        }
    }

    /**
     * 构造模板详情
     * @return
     */
    private List<DocumentFileExportVO> getDocumentFilTemplate() {
        List<DocumentFileExportVO> list = new ArrayList<>();
        List<String> cols = getDocumentCol();
        for(int i=0;i<12;i++){
            DocumentFileExportVO data = new DocumentFileExportVO();
            data.setName(cols.get(i));
            list.add(data);
        }
        return list;
    }

    @Override
    public Result<String> importDocumentFile(MultipartFile file) throws IOException {
        Result<ZkyExchangeBox> result =  getParseData(file);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(result.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),result.getMsg());
        }
        // 导入文件数据
        ZkyExchangeBox data = result.getData();
        //获取历史数据
        List<ZkyExchangeBox> hisDatas = this.findAll();
        //数据处理
        ZkyExchangeBox saveData = dataHandle(data,hisDatas);
        //保存数据
        this.save(saveData);
        return ResultUtil.success("success");
    }


    /**
     * 数据解析及校验
     * @param file
     * @return
     * @throws IOException
     */
    private Result<ZkyExchangeBox> getParseData(MultipartFile file) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        Map<String, List<List<String>>> excelContent = ImportExcelUtil.getExcelContent(workbook);
        // 获取sheet名称为节点基础配置数据
        List<List<String>> list = excelContent.get(zkyExchangeBoxSheetName);
        List<ZkyExchangeBox> datas = new ArrayList<>();
        if(null == list){
            logger.error("当前excel中"+zkyExchangeBoxSheetName+"sheet中没有数据");
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"当前excel中"+zkyExchangeBoxSheetName+"sheet中没有数据,请下载模板填写数据");
        }
        List<String> cols = getDocumentCol();
        ZkyExchangeBox zkyExchangeBox = new ZkyExchangeBox();
        for(int i = 0 ;i < cols.size();i++){ // 目前为12行数据
            List<String> row = list.get(i);
            String tempName = cols.get(i);
            if(CollectionUtils.isEmpty(row)){
                continue;
            }
            Result<String> rowHandelResult =rowHandel(zkyExchangeBox,tempName,row);
            if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(rowHandelResult.getCode())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),rowHandelResult.getMsg());
            }
        }
        return ResultUtil.success(zkyExchangeBox);
    }

    /**
     * 行数据处理：
     * 1.判断第一列是否与模板一致
     * 2.必填校验
     * 3.格式校验:截止时间为时间校验，其他int类型校验
     * @param zkyExchangeBox
     * @param tempName
     * @param row
     * @return
     */
    private Result<String> rowHandel(ZkyExchangeBox zkyExchangeBox, String tempName,List<String> row) {
        // 判断第一列是否与模板一致
        String celName = row.get(0);
        if(!tempName.equals(celName)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),celName+"位置有误，请下载模板重新进行导入");
        }
        // 必填校验
        String value = row.get(1);
        if(StringUtils.isEmpty(value)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),celName+"不能为空");
        }
        // 格式校验:截止时间为时间校验，其他int类型校验
        if("截止时间".equals(tempName)){
            Result<Date> timeResult = transferDate(value);
            if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(timeResult.getCode())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"截至时间："+timeResult.getMsg());
            }
            zkyExchangeBox.setDeadline(timeResult.getData());
        }else{
            Result<Long> integerResult = transferLong(value);
            if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(integerResult.getCode())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),tempName+":"+integerResult.getMsg());
            }
            setZkyExchangeBoxValue(tempName,integerResult.getData(),zkyExchangeBox);
        }
        return ResultUtil.success("success");
    }

    private void setZkyExchangeBoxValue(String tempName, Long data,ZkyExchangeBox zkyExchangeBox) {
        switch (tempName){
            case "收件总数":
                zkyExchangeBox.setReceiveTotal(data);
                break;
            case "收件流转总数":
                zkyExchangeBox.setReceiveRoamTotal(data);
                break;
            case "收件登记总数":
                zkyExchangeBox.setReceiveRegisterTotal(data);
                break;
            case "签批件总数":
                zkyExchangeBox.setSignTotal(data);
                break;
            case "签批件流转总数":
                zkyExchangeBox.setSignRoamTotal(data);
                break;
            case "签批件登记总数":
                zkyExchangeBox.setSignRegisterTotal(data);
                break;
            case "密刊总数":
                zkyExchangeBox.setSecrecyTotal(data);
                break;
            case "密刊流转总数":
                zkyExchangeBox.setSecrecyRoamTotal(data);
                break;
            case "密刊登记总数":
                zkyExchangeBox.setSecrecyRegisterTotal(data);
                break;
            case "总用户数":
                zkyExchangeBox.setUserTotal(data);
                break;
            case "用户登录次数":
                zkyExchangeBox.setUserLoginCount(data);
                break;
        }
    }


    private Result<Date> transferDate(String time) {
        try{
            Date emailTimeDate = DateUtils.str2Date(time,"yyyy.M.d");
            return ResultUtil.success(emailTimeDate);
        }catch (Exception e){
            logger.error("时间解析失败："+time,e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),time+"格式不正确");
        }
    }
    private Result<Long> transferLong(String str) {
        try{
            Long intNum = Long.parseLong(str);
            return ResultUtil.success(intNum);
        }catch (Exception e){
            logger.error("解析为long类型失败："+str,e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),str+"格式不正确");
        }
    }

    private ZkyExchangeBox dataHandle(ZkyExchangeBox data, List<ZkyExchangeBox> hisDatas) {
        Date deadlineTime = data.getDeadline();
        data.setImportTime(new Date());
        // 没有历史数据的情况
        if(CollectionUtils.isEmpty(hisDatas)){
            data.setGuid(UUIDUtils.get32UUID());
        }else{ // 存在历史数据的情况，跟历史数据进行比较
            ZkyExchangeBox zkyExchangeBoxOld =  getHisData(deadlineTime,hisDatas);
            // 存在的话，用历史的数据Guid，数据做更新
            if(null != zkyExchangeBoxOld){
                data.setGuid(zkyExchangeBoxOld.getGuid());
            }else{
                data.setGuid(UUIDUtils.get32UUID());
            }
        }
        return data;
    }

    private ZkyExchangeBox getHisData(Date deadlineTime, List<ZkyExchangeBox> hisDatas) {
        for (ZkyExchangeBox data : hisDatas) {
            Date deadLine = data.getDeadline();
            if (deadlineTime.equals(deadLine)) {
                return data;
            }
        }
        return null;
    }

    /**
     * 展示
     * 截至时间、导入时间查询
     * @param searchVO
     * @return
     */
    @Override
    public PageRes<ZkyExchangeBox> documentGetPage(ZkyExchangeBoxSearchVO searchVO) {
        PageReq pageReq=new PageReq();
        pageReq.setCount(searchVO.getCount_());
        pageReq.setBy("desc");
        pageReq.setOrder("importTime");
        pageReq.setStart(searchVO.getStart_());
        List<QueryCondition> conditions =  getQueryCondition(searchVO);
        Page<ZkyExchangeBox> zkyExchangeBox = this.findAll(conditions,pageReq.getPageable());
        PageRes<ZkyExchangeBox> pageRes = new PageRes<>();
        pageRes.setCode(String.valueOf(ResultCodeEnum.SUCCESS.getCode()));
        pageRes.setList(zkyExchangeBox.getContent());
        pageRes.setTotal(zkyExchangeBox.getTotalElements());
        pageRes.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        return pageRes;
    }
    private List<QueryCondition> getQueryCondition(ZkyExchangeBoxSearchVO searchVO) {
        List<QueryCondition> cons = new ArrayList<>();
        // 导入时间
        if(null != searchVO.getImportTimeStart() && null != searchVO.getImportTimeEnd()){
            cons.add(QueryCondition.between("importTime", searchVO.getImportTimeStart(),searchVO.getImportTimeEnd()));
        }
        // 截至时间
        if(null != searchVO.getDeadlineStart() && null != searchVO.getDeadlineEnd()){
            cons.add(QueryCondition.between("deadline", searchVO.getDeadlineStart(),searchVO.getDeadlineEnd()));
        }
        return cons;
    }

    /**
     * 删除操作
     *
     * @param guid
     */
    @Override
    public void documentDelete(String guid) {
       this.delete(guid);
    }

}
