package com.vrv.vap.apicasom.business.meeting.service.impl;

import com.alibaba.fastjson.JSON;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyPrintUserOrg;
import com.vrv.vap.apicasom.business.meeting.cache.LocalCache;
import com.vrv.vap.apicasom.business.meeting.repository.ZkyPrintUserOrgRepository;
import com.vrv.vap.apicasom.business.meeting.service.ZkyPrintUserOrgService;
import com.vrv.vap.apicasom.business.meeting.util.ImportExcelUtil;
import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelUtils;
import com.vrv.vap.apicasom.business.meeting.vo.PrintUserOrgExportVO;
import com.vrv.vap.apicasom.business.meeting.vo.PrintUserOrgSerachVO;
import com.vrv.vap.apicasom.frameworks.config.FileConfiguration;
import com.vrv.vap.exportAndImport.excel.exception.ExcelException;
import com.vrv.vap.exportAndImport.excel.util.DateUtils;
import com.vrv.vap.jpa.basedao.BaseRepository;
import com.vrv.vap.jpa.baseservice.impl.BaseServiceImpl;
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
 * 用户打印组织机构数据
 *
 *  2023-5-25
 */
@Service
public class ZkyPrintUserOrgServiceImpl extends BaseServiceImpl<ZkyPrintUserOrg, String> implements ZkyPrintUserOrgService {
    private static Logger logger = LoggerFactory.getLogger(ZkyPrintUserOrgServiceImpl.class);
    @Autowired
    private FileConfiguration fileConfiguration;
    @Autowired
    private ZkyPrintUserOrgRepository zkyPrintUserOrgRepository;
    @Autowired
    private MapperUtil mapper;

    private String zkyPrintUserOrgSheetName="打印用户机构";
    @Override
    public BaseRepository<ZkyPrintUserOrg, String> getRepository() {
        return zkyPrintUserOrgRepository;
    }
    /**
     * 打印用户机构数据模板导出
     * @return
     */
    @Override
    public Result<String> printUserOrgTemplate() {
        String fileName = "打印用户机构数据模板" + DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        String rootPath = fileConfiguration.getTemplatePath();
        File targetFile = new File(rootPath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String filePath = Paths.get(rootPath, fileName).toString();
        try {
            ExportExcelUtils.getInstance().createExcel(null, PrintUserOrgExportVO.class, zkyPrintUserOrgSheetName,filePath,false);
            return ResultUtil.success(fileName);
        } catch (ExcelException | IOException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("打印用户机构数据模板导出excel异常", e);
            return ResultUtil.error(-1,"导出excel异常");
        }
    }

    /**
     * 导入文件
     *
     * @param file
     * @return
     */
    @Override
    public Result<String> importprintUserOrgFile(MultipartFile file) throws IOException {
        // 解析数据
        Result<List<ZkyPrintUserOrg>> result =  getParseData(file);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(result.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),result.getMsg());
        }
        List<ZkyPrintUserOrg> lists = result.getData();
        // 校验数据
        Result<String> resultValidate = getValidateResult(lists);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(resultValidate.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),resultValidate.getMsg());
        }
        //获取历史数据
        List<ZkyPrintUserOrg> hisDatas = this.findAll();
        //数据处理
        List<ZkyPrintUserOrg> saveDatas = dataHandle(lists,hisDatas);
        //保存数据
        this.save(saveDatas);
        // 更新缓存
        upDateZkyPrintUserOrgCache();
        return ResultUtil.success("success");
    }


    /**
     * 全量更新缓存
     */
    private void upDateZkyPrintUserOrgCache(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    List<ZkyPrintUserOrg> list = zkyPrintUserOrgRepository.findAll();
                    LocalCache.upDateAllZkyPrintUserOrgCache(list);
                }catch (Exception e){
                   logger.error("全量更新用户打印机构数据异常",e);
                    LocalCache.clearAll();
                }
            }
        }).start();
    }


    /**
     * 解析数据
     * @param file
     * @return
     * @throws IOException
     */
    private Result<List<ZkyPrintUserOrg>> getParseData(MultipartFile file) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        Map<String, List<List<String>>> excelContent = ImportExcelUtil.getExcelContent(workbook);
        // 获取模板sheet对应的数据
        List<List<String>> list = excelContent.get(zkyPrintUserOrgSheetName);
        List<ZkyPrintUserOrg> datas = new ArrayList<>();
        if(null == list){
            logger.error("当前excel中"+zkyPrintUserOrgSheetName+"sheet中没有数据");
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"当前excel中"+zkyPrintUserOrgSheetName+"sheet中没有数据,请下载模板填写数据");
        }
        ZkyPrintUserOrg org = null;
        for(List<String> row : list){
            org = new ZkyPrintUserOrg();
            org.setUserName(row.get(0));
            org.setBranch(row.get(1));
            org.setOrganizationName(row.get(2));
            datas.add(org);
        }
        return ResultUtil.success(datas);
    }

    /**
     * 数据校验
     * 1. 用户名、分院、单位非空校验
     * 2. 用户重复校验
     * @param data
     * @return
     */
    private Result<String> getValidateResult(List<ZkyPrintUserOrg> data) {
        // 用户名重复校验
        List<String> userNames =new  ArrayList<>();
        for(ZkyPrintUserOrg org : data){
            Result<String> validate =  validateprintUserOrgEdit(org);
            if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(validate.getCode())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),validate.getMsg());
            }
            String userName = org.getUserName();
            userName = userName.trim(); // 去掉前后非空
            if(StringUtils.isEmpty(userName)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"用户名不能为空："+ JSON.toJSONString(org));
            }
            if(userNames.contains(userName)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),userName+"：用户名重复");
            }
            userNames.add(userName);
            org.setUserName(userName);
        }
        return ResultUtil.success("success");
    }

    /**
     * 数据处理
     * 1. 用户名存在将历史数据更新，不存在新增处理
     * @param lists
     * @param hisDatas
     * @return
     */
    private List<ZkyPrintUserOrg> dataHandle(List<ZkyPrintUserOrg> lists, List<ZkyPrintUserOrg> hisDatas) {
        for(ZkyPrintUserOrg data : lists){
            String userName = data.getUserName();
            data.setImportTime(new Date());
            if(CollectionUtils.isEmpty(hisDatas)){
                data.setGuid(UUIDUtils.get32UUID());
            }else{// 存在历史数据的情况，跟历史数据进行比较
                ZkyPrintUserOrg hisData = getHisData(hisDatas,userName);
                if(null != hisData){
                    data.setGuid(hisData.getGuid()); //存在的话，更新历史数据
                }else{
                    data.setGuid(UUIDUtils.get32UUID());
                }
            }
        }
        return lists;
    }

    private ZkyPrintUserOrg getHisData(List<ZkyPrintUserOrg> hisDatas, String userName) {
        for(ZkyPrintUserOrg data : hisDatas){
            String userNameOld = data.getUserName();
            if(userName.equals(userNameOld)){
                return data;
            }
        }
        return null;
    }

    /**
     * 展示数据
     * 1. 查询条件：用户名、 分院(地区)、 单位/部门
     * @param printUserOrgSerachVO
     * @return
     */
    @Override
    public PageRes<ZkyPrintUserOrg> printUserOrgGetPage(PrintUserOrgSerachVO printUserOrgSerachVO) {
        PageReq pageReq=new PageReq();
        pageReq.setCount(printUserOrgSerachVO.getCount_());
        pageReq.setBy("desc");
        pageReq.setOrder("importTime");
        pageReq.setStart(printUserOrgSerachVO.getStart_());
        List<QueryCondition> conditions =  getQueryCondition(printUserOrgSerachVO);
        Page<ZkyPrintUserOrg> zkyPrintUserOrgs = this.findAll(conditions,pageReq.getPageable());
        PageRes<ZkyPrintUserOrg> pageRes = new PageRes<>();
        pageRes.setCode(String.valueOf(ResultCodeEnum.SUCCESS.getCode()));
        pageRes.setList(zkyPrintUserOrgs.getContent());
        pageRes.setTotal(zkyPrintUserOrgs.getTotalElements());
        pageRes.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        return pageRes;
    }

    /**
     * 查询条件
     * @param printUserOrgSerachVO
     * @return
     */
    private List<QueryCondition> getQueryCondition(PrintUserOrgSerachVO printUserOrgSerachVO) {
        List<QueryCondition> cons = new ArrayList<>();
        // 用户名
        if(StringUtils.isNotEmpty(printUserOrgSerachVO.getUserName())){
            cons.add(QueryCondition.like("userName",printUserOrgSerachVO.getUserName()));
        }
        // 分院(地区)
        if(StringUtils.isNotEmpty(printUserOrgSerachVO.getBranch())){
            cons.add(QueryCondition.like("branch",printUserOrgSerachVO.getBranch()));
        }
        // 单位/部门
        if(StringUtils.isNotEmpty(printUserOrgSerachVO.getOrganizationName())){
            cons.add(QueryCondition.like("organizationName",printUserOrgSerachVO.getOrganizationName()));
        }
        return cons;
    }
    /**
     * 打印用户机构数据导出数据生成
     * @param printUserOrgSerachVO
     * @return
     */
    @Override
    public Result<String> printUserOrgExportData(PrintUserOrgSerachVO printUserOrgSerachVO) {
        List<QueryCondition> conditions =  getQueryCondition(printUserOrgSerachVO);
        List<ZkyPrintUserOrg> userOrgs = this.findAll(conditions);
        String fileName = "打印用户机构数据" + DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        String rootPath = fileConfiguration.getTemplatePath();
        File targetFile = new File(rootPath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        List<PrintUserOrgExportVO> datas = null;
        if(null != userOrgs && userOrgs.size() > 0){
            datas = mapper.mapList(userOrgs,PrintUserOrgExportVO.class);
        }
        String filePath = Paths.get(rootPath, fileName).toString();
        try {
            ExportExcelUtils.getInstance().createExcel(datas, PrintUserOrgExportVO.class, zkyPrintUserOrgSheetName,filePath,false);
            return ResultUtil.success(fileName);
        } catch (ExcelException | IOException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("打印用户机构数据导出数据生成Excel异常", e);
            return ResultUtil.error(-1,"打印用户机构数据导出数据生成Excel异常");
        }
    }
    /**
     * 删除
     * @param guid
     */
    @Override
    public void printUserOrgDel(String guid) {
       this.delete(guid);
        // 更新缓存
        upDateZkyPrintUserOrgCache();
    }

    /**
     * 编辑
     * 1. 必填校验
     * 2. 用户名是否存在校验
     * @param printUserOrg
     * @return
     */
    @Override
    public Result<String> printUserOrgEdit(ZkyPrintUserOrg printUserOrg) {
        String guid = printUserOrg.getGuid();
        if(StringUtils.isEmpty(guid)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"guid不能为空");
        }
        ZkyPrintUserOrg bean = this.getOne(guid);
        if(null == bean){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"当前编辑的数据不能存在");
        }
        // 必填校验
        Result<String> validateResult = validateprintUserOrgEdit(printUserOrg);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(validateResult.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),validateResult.getMsg());
        }
        // 用户名是否存在
        String userName = printUserOrg.getUserName();
        boolean userNameRes = isExistUserName(userName,guid);
        if(userNameRes){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"用户名："+userName+"已经存在");
        }
        printUserOrg.setImportTime(new Date()); // 更新时间
        this.save(printUserOrg); // 保存数据
        // 更新缓存
        upDateZkyPrintUserOrgCache();
        return ResultUtil.success("success");
    }



    private boolean isExistUserName(String userName,String guid) {
        List<QueryCondition> conditions = new ArrayList<>();
        conditions.add(QueryCondition.eq("userName",userName));
        conditions.add(QueryCondition.notEq("guid",guid));
        List<ZkyPrintUserOrg> list = this.findAll(conditions);
        if(list.size() > 0){
            return true;
        }
        return false;
    }

    /**
     * 用户名、分院(地区)、单位/部门非空校验
     * @param printUserOrg
     * @return
     */
    private Result<String> validateprintUserOrgEdit(ZkyPrintUserOrg printUserOrg) {
        String userName = printUserOrg.getUserName();
        if(StringUtils.isEmpty(userName)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"用户名不能为空："+ JSON.toJSONString(printUserOrg));
        }
        String branch = printUserOrg.getBranch();
        if(StringUtils.isEmpty(branch)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"分院(地区)不能为空："+ JSON.toJSONString(printUserOrg));
        }
        String organizationName = printUserOrg.getOrganizationName();
        if(StringUtils.isEmpty(organizationName)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"单位/部门不能为空："+ JSON.toJSONString(printUserOrg));
        }
        return ResultUtil.success("success");
    }
}
