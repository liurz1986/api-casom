package com.vrv.vap.apicasom.business.meeting.dao.impl;

import com.vrv.vap.apicasom.business.meeting.dao.AccessNodeDao;
import com.vrv.vap.apicasom.business.meeting.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 接入节点列表
 *
 * @author vrv
 */
@Repository
public class AccessNodeDaoImpl implements AccessNodeDao {
    private static Logger logger = LoggerFactory.getLogger(AccessNodeDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    /**
     * 获取分页中的总数
     * @param accessNodeSearchVO
     * @return
     */
    @Override
    public long getPageTotal(AccessNodeSearchVO accessNodeSearchVO) {
        String sql = "select count(*) as number from("+getCommonSql(accessNodeSearchVO)+")a";
        logger.debug("分页查询获取总数查询sql:"+sql);
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Long.parseLong(String.valueOf(result.get("number")));
    }
    /**
     * 获取分页的数据
     * 查询条件：节点名称、地区精确查询
     * @param accessNodeSearchVO
     * @return
     */
    @Override
    public List<AccessNodeVO> getPageList(AccessNodeSearchVO accessNodeSearchVO) {
        int start =  accessNodeSearchVO.getStart_();
        int end = accessNodeSearchVO.getStart_() * accessNodeSearchVO.getCount_() + accessNodeSearchVO.getCount_();
        String sql= getCommonSql(accessNodeSearchVO) +  " limit "+start+","+end;
        logger.debug("分页查询获取数据查询sql:"+sql);
        List<AccessNodeVO> details = jdbcTemplate.query(sql,new AccessNodeVOMapper());
        return details;
    }
    public class AccessNodeVOMapper implements RowMapper<AccessNodeVO> {
        @Override
        public AccessNodeVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessNodeVO data = new AccessNodeVO();
            data.setNodeName(rs.getString("name"));
            data.setRegion(rs.getString("branch"));
            data.setAssetType(rs.getString("terminal_type"));
            data.setMeetingTimeTotal(rs.getString("duration"));
            data.setMeetingCount(rs.getString("num"));
            return data;
        }
    }

    @Override
    public List<AccessNodeExportExcelVO> exportData(AccessNodeSearchVO accessNodeSearchVO) {
        String sql= getCommonSql(accessNodeSearchVO);
        logger.debug("导出获取数据查询sql:"+sql);
        List<AccessNodeExportExcelVO> details = jdbcTemplate.query(sql,new AccessNodeExportExcelVOMapper());
        return details;
    }


    private String getCommonSql(AccessNodeSearchVO accessNodeSearchVO) {
        // 节点名称、所有分院/地区、设备类型分组其中会议时长的和除以60转为小时，统计次数作为参会次数,状态为会议结束offline
        String sql="select count(id) as num,ROUND(sum(duration)/60) as duration,name,branch,terminal_type from hw_meeting_participant where 1=1 ";
        // 节点名称精确查询
        if(StringUtils.isNotEmpty(accessNodeSearchVO.getNodeName())){
            sql = sql +" and name='"+accessNodeSearchVO.getNodeName()+"'";
        }
        // 分院精确查询
        if(StringUtils.isNotEmpty(accessNodeSearchVO.getRegion())){
            sql = sql +" and branch='"+accessNodeSearchVO.getRegion()+"'";
        }
        sql= sql+" and stage='OFFLINE' group by name,branch,terminal_type ";
        return sql;
    }

    public class AccessNodeExportExcelVOMapper implements RowMapper<AccessNodeExportExcelVO> {
        @Override
        public AccessNodeExportExcelVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AccessNodeExportExcelVO data = new AccessNodeExportExcelVO();
            data.setMeetingCount(rs.getString("num"));
            data.setNodeName(rs.getString("name"));
            data.setRegion(rs.getString("branch"));
            data.setAssetType(rs.getString("terminal_type"));
            data.setMeetingTimeTotal(rs.getString("duration"));
            return data;
        }
    }

    /**
     * 获取在线节点总数
     * @return
     */
    @Override
    public int getOnLineNodes(String status) {
        String sql = "select count(*) as number from hw_meeting_participant where stage='"+status+"'";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        if (null == result || result.size() == 0) {
            return 0;
        }
        return result.get("number")==null?0:Integer.parseInt(String.valueOf(result.get("number")));
    }

    /**
     * 按城市、节点分组
     * @param type
     * @return
     */
    @Override
    public List<CommonQueryVO> queryNodesGroupByCity(String type) {
        String sql ="select city as keyName,name from hw_meeting_participant  where 1=1 " +largeScreenCommonSql(type)+" GROUP BY city,organization_name";
        List<CommonQueryVO> details = jdbcTemplate.query(sql,new CommonQueryVoMapper());
        return details;
    }
    public class CommonQueryVoMapper implements RowMapper<CommonQueryVO> {
        @Override
        public CommonQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommonQueryVO data = new CommonQueryVO();
            data.setKey(rs.getString("keyName"));
            data.setValue(rs.getString("name"));
            return data;
        }
    }
    /**
     * 获取不存在存在异常会议并且正在开会的城市
     * @param type
     * @return
     */
    @Override
    public List<String> getRunMettingCitys(String type) {
        String sql="select DISTINCT city from hw_meeting_participant where meeting_id not in(select  meeting_id from hw_meeting_alarm)"  +
                "and stage='ONLINE'";
        if(StringUtils.isNotEmpty(type)){
            sql = sql+ largeScreenCommonSql(type);
        }
        List<String> list = jdbcTemplate.queryForList(sql,String.class);
        return list;
    }


    private String largeScreenCommonSql(String type){
        String sql ="";
        switch (type) {
            case "quarter":
                sql =" and DATE_SUB(CURDATE(), INTERVAL 3 MONTH) <= date(schedule_start_time)";
                break;
            case "halfyear":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 6 MONTH) <= date(schedule_start_time)";
                break;
            case "year":
                sql ="and DATE_SUB(CURDATE(), INTERVAL 1 YEAR) <= date(schedule_start_time)";
                break;
            default:
                break;
        }
        return sql;
    }

    /**
     * 城市所有节点名称
     * @param type
     * @param cityName
     * @return
     */
    @Override
    public List<CommonQueryVO> queryNodeNamesByCity(String type, String cityName) {
        String sql ="select organization_name as keyName, name from hw_meeting_participant where city='"+cityName+"'" +largeScreenCommonSql(type)+" GROUP BY organization_name,name";
        List<CommonQueryVO> details = jdbcTemplate.query(sql,new CommonQueryVoMapper());
        return details;
    }

    public class NodeVOMapper implements RowMapper<NodeVO> {
        @Override
        public NodeVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            NodeVO data = new NodeVO();
            data.setName(rs.getString("name"));
            data.setOrganizationName(rs.getString("organization_name"));
            data.setStartTime(rs.getTimestamp("schedule_start_time"));
            data.setEndTime(rs.getTimestamp("schedule_end_time"));
            data.setStage(rs.getString("stage"));
            return data;
        }
    }

    /**
     * 当前城市正在开会的节点信息
     * @param type
     * @param cityName
     * @return
     */
    @Override
    public List<NodeVO> queryRunNodesByCity(String type, String cityName) {
        String sql ="select name,organization_name,schedule_start_time,schedule_end_time,stage from hw_meeting_participant where stage='ONLINE' and city='"+cityName+"'" +largeScreenCommonSql(type);
        List<NodeVO> details = jdbcTemplate.query(sql,new NodeVOMapper());
        return details;
    }

}
