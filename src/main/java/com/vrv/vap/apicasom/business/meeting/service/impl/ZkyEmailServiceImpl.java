package com.vrv.vap.apicasom.business.meeting.service.impl;
import com.vrv.vap.apicasom.business.meeting.bean.ZkyEmail;
import com.vrv.vap.apicasom.business.meeting.repository.ZkyEmailRepository;
import com.vrv.vap.apicasom.business.meeting.service.ZkyEmailService;
import com.vrv.vap.apicasom.business.meeting.util.ImportExcelUtil;
import com.vrv.vap.apicasom.business.meeting.util.excel.ExportExcelUtils;
import com.vrv.vap.apicasom.business.meeting.vo.EmailExportVO;
import com.vrv.vap.apicasom.business.meeting.vo.ZkyEmailSearchVO;
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
 * 邮件处理
 *
 * 2023-5-24
 */
@Service
public class ZkyEmailServiceImpl extends BaseServiceImpl<ZkyEmail, String> implements ZkyEmailService {
    private static Logger logger = LoggerFactory.getLogger(ZkyEmailServiceImpl.class);
    @Autowired
    private FileConfiguration fileConfiguration;
    @Autowired
    private ZkyEmailRepository zkyEmailRepository;

    private String emailSheetName="邮箱收发";
    @Override
    public BaseRepository<ZkyEmail, String> getRepository() {
        return zkyEmailRepository;
    }

    /**
     * 邮件模板生成
     * @return
     */
    @Override
    public Result<String> emailTemplate() {
        String fileName = "邮件模板" + DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
        String rootPath = fileConfiguration.getTemplatePath();
        File targetFile = new File(rootPath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String filePath = Paths.get(rootPath, fileName).toString();
        try {
            ExportExcelUtils.getInstance().createExcel(null, EmailExportVO.class, emailSheetName,filePath,false);
            return ResultUtil.success(fileName);
        } catch (ExcelException | IOException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("邮件模板导出excel异常", e);
            return ResultUtil.error(-1,"导出excel异常");
        }
    }

    /**
     * 邮件导入
     *   导入列：部门名称、时间、收件数、发件数
     *   部门名称、时间、收件数、发件数非空校验，收件数和发件数数据格式校验
     * 根据时间和部门名称判断，存在进行更新,不存在进行新增
     * @param file
     * @return
     */
    @Override
    public Result<String> importEmailFile(MultipartFile file) throws IOException {
        Result<List<ZkyEmail>> result =  getParseData(file);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(result.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),result.getMsg());
        }
        // 导入文件数据
        List<ZkyEmail> list = result.getData();
        //获取历史数据
        List<ZkyEmail> hisDatas = this.findAll();
        //数据处理
        List<ZkyEmail> saveDatas = dataHandle(list,hisDatas);
        //保存数据
        this.save(saveDatas);
        return ResultUtil.success("success");
    }



    /**
     * 数据解析及校验
     * @param file
     * @return
     * @throws IOException
     */
    private Result<List<ZkyEmail>> getParseData(MultipartFile file) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
        Map<String, List<List<String>>> excelContent = ImportExcelUtil.getExcelContent(workbook);
        // 获取sheet名称为节点基础配置数据
        List<List<String>> list = excelContent.get(emailSheetName);
        List<ZkyEmail> datas = new ArrayList<>();
        if(null == list){
            logger.error("当前excel中"+emailSheetName+"sheet中没有数据");
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"当前excel中"+emailSheetName+"sheet中没有数据,请下载模板填写数据");
        }
        List<String> orgNameAndTime = new ArrayList<>();
        for(List<String> row : list){
            Result<ZkyEmail> result = getZkEmailAndValidate(row);
            if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(result.getCode())){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),result.getMsg());
            }
            ZkyEmail zkyEmail =  result.getData();
            // 部门名称和时间重复校验
            String str = zkyEmail.getOrgName()+"-"+zkyEmail.getEmailTime();
            if(orgNameAndTime.contains(str)){
                return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"导入数据中部门名称和时间存在重复的："+str);
            }
            datas.add(result.getData());
        }
        return ResultUtil.success(datas);
    }

    /**
     * 校验及组装数据
     * @param row
     * @return
     */
    private Result<ZkyEmail> getZkEmailAndValidate(List<String> row) {
        ZkyEmail bean = new ZkyEmail();
        // 部门名称必填校验
        String orgName = row.get(0);
        if(StringUtils.isEmpty(orgName)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"部门名称不能为空");
        }
        bean.setOrgName(orgName);
        // 时间必填及格式
        String emailTime = row.get(1);
        if(StringUtils.isEmpty(emailTime)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"时间不能为空");
        }
        Result<Date> emailResult = transferDate(emailTime);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(emailResult.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"时间:"+emailResult.getMsg());
        }
        bean.setEmailTime(emailResult.getData());
        // 收件数必填、格式校验
        String receiveNum = row.get(2);
        if(StringUtils.isEmpty(receiveNum)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"收件数不能为空");
        }
        Result<Long> receiveNumResult = transferLong(receiveNum);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(receiveNumResult.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"收件数："+receiveNumResult.getMsg());
        }
        bean.setReceiveNum(receiveNumResult.getData());
        // 发件数必填和有效性校验
        String sendNum = row.get(3);
        if(StringUtils.isEmpty(sendNum)){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"发件数不能为空");
        }
        Result<Long> sendNumResult = transferLong(sendNum);
        if(ResultCodeEnum.UNKNOW_FAILED.getCode().equals(sendNumResult.getCode())){
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),"发件数："+sendNumResult.getMsg());
        }
        bean.setSendNum(sendNumResult.getData());
        return ResultUtil.success(bean);
    }



    private Result<Date> transferDate(String emailTime) {
        try{
            Date emailTimeDate = DateUtils.str2Date(emailTime,"yyyy.M.d");
            return ResultUtil.success(emailTimeDate);
        }catch (Exception e){
            logger.error("时间解析失败："+emailTime,e);
            return ResultUtil.error(ResultCodeEnum.UNKNOW_FAILED.getCode(),emailTime+"格式不正确");
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

    /**
     * 导入数据与历史处理
     * 根据时间和部门名称判断，存在进行更新,不存在进行新增
     * @param list
     * @param hisDatas
     * @return
     */
    private List<ZkyEmail> dataHandle(List<ZkyEmail> list, List<ZkyEmail> hisDatas) {
        for(ZkyEmail zkyEmail : list){
            Date emailTime = zkyEmail.getEmailTime();
            String orgName = zkyEmail.getOrgName();
            zkyEmail.setImportTime(new Date());
            // 没有历史数据的情况
            if(CollectionUtils.isEmpty(hisDatas)){
                zkyEmail.setGuid(UUIDUtils.get32UUID());
            }else{ // 存在历史数据的情况，跟历史数据进行比较
                ZkyEmail zkyEmailOld =  getHisData(emailTime,orgName,hisDatas);
                // 存在的话，用历史的数据Guid，数据做更新
                if(null != zkyEmailOld){
                    zkyEmail.setGuid(zkyEmailOld.getGuid());
                }else{
                    zkyEmail.setGuid(UUIDUtils.get32UUID());
                }
            }
        }
       return list;
    }

    private ZkyEmail getHisData(Date emailTime, String orgName, List<ZkyEmail> hisDatas) {
        for(ZkyEmail zkyEmail : hisDatas){
            Date emailTimeOld = zkyEmail.getEmailTime();
            String orgNameOld = zkyEmail.getOrgName();
            if(emailTime.equals(emailTimeOld)&&orgName.equals(orgNameOld)){
                return zkyEmail;
            }
        }
        return null;
    }

    /**
     * 邮箱页面展示
     * @param searchVO
     * @return
     */
    @Override
    public PageRes<ZkyEmail> emailGetPage(ZkyEmailSearchVO searchVO) {
        PageReq pageReq=new PageReq();
        pageReq.setCount(searchVO.getCount_());
        pageReq.setBy("desc");
        pageReq.setOrder("importTime");
        pageReq.setStart(searchVO.getStart_());
        List<QueryCondition> conditions =  getQueryCondition(searchVO);
        Page<ZkyEmail> zkyEmail = this.findAll(conditions,pageReq.getPageable());

        PageRes<ZkyEmail> pageRes = new PageRes<>();
        pageRes.setCode(String.valueOf(ResultCodeEnum.SUCCESS.getCode()));
        pageRes.setList(zkyEmail.getContent());
        pageRes.setTotal(zkyEmail.getTotalElements());
        pageRes.setMessage(ResultCodeEnum.SUCCESS.getMsg());
        return pageRes;
    }

    private List<QueryCondition> getQueryCondition(ZkyEmailSearchVO searchVO) {
        List<QueryCondition> cons = new ArrayList<>();
        // 部门模糊查询
        if(StringUtils.isNotEmpty(searchVO.getOrgName())){
            cons.add(QueryCondition.like("orgName",searchVO.getOrgName()));
        }
        return cons;
    }

    @Override
    public void emailDelete(String guid) {
        this.delete(guid);
    }

}
