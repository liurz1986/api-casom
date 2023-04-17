package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.service.ZkyUnitConfigService;
import com.vrv.vap.apicasom.business.meeting.util.ImportExcelUtil;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitExportExcelVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitSerachVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyUnitVO;
import com.vrv.vap.apicasom.business.task.bean.ZkyUnitBean;
import com.vrv.vap.apicasom.business.task.service.ZkyUnitService;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.ExcelUtils;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.common.FileUtil;
import com.vrv.vap.jpa.common.UUIDUtils;
import com.vrv.vap.jpa.mapper.MapperUtil;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点基础数据配置
 */
@Service
public class ZkyUnitConfigServiceImpl  implements ZkyUnitConfigService {
    private static Logger logger = LoggerFactory.getLogger(ZkyUnitConfigServiceImpl.class);

    @Autowired
    private ZkyUnitService zkyUnitService;
    @Autowired
    private MapperUtil mapper;
    @Autowired
    private FileConfiguration fileConfiguration;

    /**
     * 支持节点名称、城市、分院模糊查询
     * @param zkyUnitSerachVO
     * @return
     */
    @Override
    public PageRes<ZkyUnitBean> getPage(ZkyUnitSerachVO zkyUnitSerachVO) {
        PageReq pageReq=new PageReq();
        pageReq.setCount(zkyUnitSerachVO.getCount_());
        pageReq.setBy("desc");
        pageReq.setOrder("createTime");
        pageReq.setStart(zkyUnitSerachVO.getStart_());
        List<QueryCondition> conditions =  getQueryCondition(zkyUnitSerachVO);
        Page<ZkyUnitBean> ZkyUnitBeans = zkyUnitService.findAll(conditions,pageReq.getPageable());

        PageRes<ZkyUnitBean> pageRes = new PageRes<>();
        pageRes.setCode(String.valueOf(ResultCodeEnum.SUCCESS.getCode()));
        pageRes.setList(ZkyUnitBeans.getContent());
        pageRes.setTotal(ZkyUnitBeans.getTotalElements());
        pageRes.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        return pageRes;
    }

    private List<QueryCondition> getQueryCondition(ZkyUnitSerachVO zkyUnitSerachVO) {
        List<QueryCondition> cons = new ArrayList<>();
        if(StringUtils.isNotEmpty(zkyUnitSerachVO.getName())){
            cons.add(QueryCondition.like("name",zkyUnitSerachVO.getName()));
        }
        if(StringUtils.isNotEmpty(zkyUnitSerachVO.getParticipantName())){
            cons.add(QueryCondition.like("participantName",zkyUnitSerachVO.getParticipantName()));
        }
        if(StringUtils.isNotEmpty(zkyUnitSerachVO.getParticipantCode())){
            cons.add(QueryCondition.like("participantCode",zkyUnitSerachVO.getParticipantCode()));
        }
        if(StringUtils.isNotEmpty(zkyUnitSerachVO.getCity())){
            cons.add(QueryCondition.like("city",zkyUnitSerachVO.getCity()));
        }
        if(StringUtils.isNotEmpty(zkyUnitSerachVO.getBranch())){
            cons.add(QueryCondition.like("branch",zkyUnitSerachVO.getBranch()));
        }
        return cons;
    }

    /**
     * 节点基础数据配置新增
     * 1.必填校验：节点名称、节点编号、城市、分院
     * 2. 节点编号、名称唯一性校验
     * @param zkyUnitVO
     * @return
     */
    @Override
    public Result<String> save(ZkyUnitVO zkyUnitVO) {
        // 数据校验
        ZkyUnitBean bean =  mapper.map(zkyUnitVO,ZkyUnitBean.class);
        Result<String> result = saveValidate(bean);
        if(result.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return result;
        }
        bean.setId(UUIDUtils.get32UUID());
        bean.setCreateTime(new Date());
        zkyUnitService.save(bean);
        return ResultUtil.success("success");
    }

    /**
     * 单个数据保存校验：
     * 1.必填校验：节点名称、节点编号、城市、分院
     * 2. 节点编号、名称唯一性校验
     * @param zkyUnitVO
     * @return
     */
    private Result<String> saveValidate(ZkyUnitBean zkyUnitVO) {
        // 必填校验
        Result<String> isMustResult = isMustValidate(zkyUnitVO);
        if(isMustResult.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return isMustResult;
        }
        // 节点编号、名称唯一性校验
        List<QueryCondition> cons = new ArrayList<>();
        cons.add(QueryCondition.eq("participantCode",zkyUnitVO.getParticipantCode()));
        List<ZkyUnitBean> zkyUnit = zkyUnitService.findAll(cons);
        if(null != zkyUnit && zkyUnit.size()>0){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点编号已经存在："+zkyUnitVO.getParticipantCode());
        }
        cons = new ArrayList<>();
        cons.add(QueryCondition.eq("participantName",zkyUnitVO.getParticipantName()));
        zkyUnit = zkyUnitService.findAll(cons);
        if(null != zkyUnit && zkyUnit.size()>0){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点名称已经存在："+zkyUnitVO.getParticipantName());
        }
        return ResultUtil.success("success");
    }



    private Result<String> isMustValidate(ZkyUnitBean zkyUnitVO) {
        if(StringUtils.isEmpty(zkyUnitVO.getName())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"研究所名称不能为空");
        }
        if(StringUtils.isEmpty(zkyUnitVO.getParticipantCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点编号不能为空");
        }
        if(StringUtils.isEmpty(zkyUnitVO.getParticipantName())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点名称不能为空");
        }
        if(StringUtils.isEmpty(zkyUnitVO.getBranch())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"分院/地区不能为空");
        }
        if(StringUtils.isEmpty(zkyUnitVO.getCity())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"城市不能为空");
        }
        return ResultUtil.success("success");
    }

    /**
     * 节点基础数据配置修改
     * 1.必填校验：节点名称、节点编号、城市、分院
     * 2.节点编号、名称唯一性校验
     * @param zkyUnitVO
     * @return
     */
    @Override
    public Result<String> update(ZkyUnitVO zkyUnitVO) {
        if(StringUtils.isEmpty(zkyUnitVO.getId())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"id不能为空："+zkyUnitVO.getId());
        }
        ZkyUnitBean beanOld = zkyUnitService.getOne(zkyUnitVO.getId());
        if(null == beanOld){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"当前节点不存在："+zkyUnitVO.getId());
        }
        // 数据校验
        ZkyUnitBean bean =  mapper.map(zkyUnitVO,ZkyUnitBean.class);
        Result<String> result = updateValidate(bean);
        if(result.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return result;
        }
        bean.setCreateTime(new Date());
        zkyUnitService.save(bean);
        return ResultUtil.success("success");
    }

    /**
     * 单个数据修改校验：
     * 1.必填校验：节点名称、节点编号、城市、分院
     * 2. 节点编号唯一性校验
     * @param zkyUnitVO
     * @return
     */
    private Result<String> updateValidate(ZkyUnitBean zkyUnitVO) {
        // 必填校验
        Result<String> isMustResult = isMustValidate(zkyUnitVO);
        if(isMustResult.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return isMustResult;
        }
        // 节点编号唯一性校验
        List<QueryCondition> cons = new ArrayList<>();
        cons.add(QueryCondition.eq("participantCode",zkyUnitVO.getParticipantCode()));
        cons.add(QueryCondition.notEq("id",zkyUnitVO.getId()));
        List<ZkyUnitBean> zkyUnit = zkyUnitService.findAll(cons);
        if(null != zkyUnit && zkyUnit.size()>0){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点编号已经存在："+zkyUnitVO.getParticipantCode());
        }
        cons = new ArrayList<>();
        cons.add(QueryCondition.eq("participantName",zkyUnitVO.getParticipantName()));
        cons.add(QueryCondition.notEq("id",zkyUnitVO.getId()));
        zkyUnit = zkyUnitService.findAll(cons);
        if(null != zkyUnit && zkyUnit.size()>0){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"节点名称已经存在："+zkyUnitVO.getParticipantName());
        }
        return ResultUtil.success("success");
    }

    @Override
    public void delete(String guid) {
        zkyUnitService.delete(guid);
    }

    @Override
    public Result<String> exportDataTemplate() {
        String filePath = fileConfiguration.getFilePath();
        File newFile= new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String uuid="节点配置导入模板"+ DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        logger.info("fileName: "+uuid);
        // 文件名称
        String fileName = uuid + ".xls";
        filePath=filePath+ File.separator+fileName;
        try{
            String templatePath=fileConfiguration.getTemplatePath()+"/zkyunit_template.xls";
            logger.info("节点配置导入模板路径: "+templatePath);
            Map<String, String> extenddata = new HashMap<String, String>(1);
            extenddata.put("title", "节点数据");
            List<ZkyUnitExportExcelVO> datas = getTemplateData();
            ExcelUtils.getInstance().exportObjects2Excel(templatePath,datas,extenddata, ZkyUnitExportExcelVO.class, false, filePath);
        }catch(Exception e){
            logger.error("生成节点配置导入模板文件失败",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成节点配置导入模板文件失败");
        }
        return ResultUtil.success(uuid);
    }

    /**
     * 构造一条模板数据
     * @return
     */
    private List<ZkyUnitExportExcelVO> getTemplateData() {
        List<ZkyUnitExportExcelVO> datas = new ArrayList<>();
        ZkyUnitExportExcelVO data = new ZkyUnitExportExcelVO();
        data.setName("研究所xxx");
        data.setParticipantName("节点名称xxx");
        data.setParticipantCode("节点编号xxx");
        data.setBranch("xxx");
        data.setCity("xxx");
        datas.add(data);
        return datas;
    }

    /**
     * 生成节点基础数据配置导出文件
     * @param zkyUnitSerachVO
     * @return
     */
    @Override
    public Result<String> exportData(ZkyUnitSerachVO zkyUnitSerachVO) {
        logger.debug("节点基础数据配置导出请求参数："+ JSON.toJSONString(zkyUnitSerachVO));
        List<QueryCondition> conditions =  getQueryCondition(zkyUnitSerachVO);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        List<ZkyUnitBean> list = zkyUnitService.findAll(conditions,sort);
        List<ZkyUnitExportExcelVO> exportDatas = mapper.mapList(list,ZkyUnitExportExcelVO.class);
        String filePath = fileConfiguration.getFilePath();
        File newFile= new File(filePath);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String uuid="节点基础数据"+ DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        logger.info("fileName: "+uuid);
        // 文件名称
        String fileName = uuid + ".xls";
        filePath=filePath+ File.separator+fileName;
        try{
            String templatePath=fileConfiguration.getTemplatePath()+"/zkyunit_template.xls";
            logger.info("节点基础数据模板路径: "+templatePath);
            Map<String, String> extenddata = new HashMap<String, String>(1);
            extenddata.put("title", "节点数据");
            ExcelUtils.getInstance().exportObjects2Excel(templatePath,exportDatas,extenddata, ZkyUnitExportExcelVO.class, false, filePath);
        }catch(Exception e){
            logger.error("生成节点基础数据配置导出文件失败",e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"生成节点基础数据配置导出文件失败");
        }
        return ResultUtil.success(uuid);
    }


    @Override
    public void exportFileInfo(String fileName, HttpServletResponse response) {
        FileUtil.downLoadFile(fileName+ ".xls", fileConfiguration.getFilePath(), response);
    }


    /**
     * 数据导入
     * 节点基础配置
     *  1.必填校验：节点名称、节点编号、城市、分院
     *  2. 节点编号唯一性校验
     *  3. 节点编号存在做更新处理、不存在新增处理
     * @param file
     * @return
     */
    @Override
    public Result<String> importFileInfo(MultipartFile file) throws IOException {
        // 解析数据
        List<ZkyUnitBean> datas = getParseData(file);
        if(CollectionUtils.isEmpty(datas)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入的excel中没有数据");
        }
        // 数据校验
        Result<String> result =validateImportData(datas);
        if(result.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return result;
        }
        // 判断节点编号是否存在，存在的话做更新处理
        dataUpdateHandle(datas);
        // 数据保存
        zkyUnitService.save(datas);
        return ResultUtil.success("success");
    }



    private Result<String> validateImportData(List<ZkyUnitBean> datas) {
        for(ZkyUnitBean bean : datas){
            // 必填校验
            Result<String> isMustResult = isMustValidate(bean);
            if(isMustResult.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
                return isMustResult;
            }
        }
        // 节点编号、节点名称重复判断
        Result<String> repeatNameCodeResult = nameCodeRepeatValidate(datas);
        if(repeatNameCodeResult.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return repeatNameCodeResult;
        }
        Result<String> repeatNameResult = nameRepeatValidate(datas);
        if(repeatNameResult.getCode().equals(ResultCodeEnum.UNKNOW_FAILED.getCode())){
            return repeatNameResult;
        }
        return ResultUtil.success("success");
    }

    private Result<String> nameCodeRepeatValidate(List<ZkyUnitBean> datas) {
        // 节点编号分组
        Map<String, List<ZkyUnitBean>> maps =  datas.stream().collect(Collectors.groupingBy(map -> map.getParticipantCode(), Collectors.toList()));
        Set<String> keys = maps.keySet();
        for(String key : keys){
            int size = maps.get(key).size();
            if(size > 1){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入数据中节点编号<<"+key+">>重复");
            }
        }
        return ResultUtil.success("success");
    }
    private Result<String> nameRepeatValidate(List<ZkyUnitBean> datas) {
        // 节点名称分组
        Map<String, List<ZkyUnitBean>> maps =  datas.stream().collect(Collectors.groupingBy(map -> map.getParticipantName(), Collectors.toList()));
        Set<String> keys = maps.keySet();
        for(String key : keys){
            int size = maps.get(key).size();
            if(size > 1){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入数据中节点名称<<"+key+">>重复");
            }
        }
        return ResultUtil.success("success");
    }

    /**
     * 判断节点编号是否存在，存在的话做更新处理,不存在做新增处理
     *
     * @param datas
     */
    private void dataUpdateHandle(List<ZkyUnitBean> datas) {
        if(CollectionUtils.isEmpty(datas)){
           return;
        }
        // 获取现有所有数据
        List<ZkyUnitBean> oldDatas = zkyUnitService.findAll();
        // 节点编号去重处理
        for(ZkyUnitBean bean : datas){
            // 是否节点编号是否存在
            ZkyUnitBean oldData = getParticipantCodeExist(bean.getParticipantCode(),oldDatas);
            if(null != oldData){
                //更新，保留guid
                bean.setId(oldData.getId());
            }else{
                bean.setId(UUIDUtils.get32UUID());
            }
            bean.setCreateTime(new Date());
        }
    }

    private ZkyUnitBean getParticipantCodeExist(String participantCode, List<ZkyUnitBean> oldDatas) {
        if(CollectionUtils.isEmpty(oldDatas)){
            return null;
        }
        for(ZkyUnitBean old:oldDatas){
            if(participantCode.equals(old.getParticipantCode())){
                return old;
            }
        }
        return null;
    }



    private List<ZkyUnitBean> getParseData(MultipartFile file) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        Map<String, List<List<String>>> excelContent = ImportExcelUtil.getExcelContent(workbook);
        // 获取sheet名称为节点基础配置数据
        List<List<String>> list = excelContent.get("节点基础配置");
        List<ZkyUnitBean> datas = new ArrayList<>();
        if(null == list){
            return datas;
        }
        for(List<String> row : list){
            ZkyUnitBean bean = getZkUnit(row);
            datas.add(bean);
        }
        return datas;
    }

    private ZkyUnitBean getZkUnit(List<String> row) {
        ZkyUnitBean bean = new ZkyUnitBean();
        bean.setName(row.get(1));
        bean.setParticipantName(row.get(2));
        bean.setParticipantCode(row.get(3));
        bean.setBranch(row.get(4));
        bean.setCity(row.get(5));
        return bean;
    }

}
