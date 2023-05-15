-- liquibase formatted sql
-- changeset lianggl:20230222-alarmdeal-001 labels:"华为会议同步"
CREATE TABLE IF NOT EXISTS `hw_meeting_alarm` (
        `id` varchar(125) NOT NULL COMMENT '记录ID',
        `meeting_id` varchar(125) DEFAULT NULL COMMENT '会议ID',
        `alarm_no` varchar(48) DEFAULT NULL COMMENT '告警标识',
        `name` varchar(48) DEFAULT NULL COMMENT '异常名称',
        `severity` varchar(48) DEFAULT NULL COMMENT '严重级别',
        `alarm_type` varchar(48) DEFAULT NULL COMMENT '异常类型',
        `alarm_time` datetime DEFAULT NULL COMMENT '告警时间',
        `cleared_time` datetime DEFAULT NULL COMMENT '告警确认或恢复时间',
         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='会议告警';

CREATE TABLE IF NOT EXISTS `hw_meeting_attendee` (
       `id` varchar(125) NOT NULL COMMENT '记录ID',
       `meeting_id` varchar(125) DEFAULT NULL COMMENT '会议ID',
       `participant_name` varchar(255) DEFAULT NULL COMMENT '节点名称(会场名称)',
       `user_count` int(10) DEFAULT NULL COMMENT '参会人数',
       `city` varchar(48) DEFAULT NULL COMMENT '城市',
       `branch` varchar(48) DEFAULT NULL COMMENT '分院',
       `duration` int(10) DEFAULT NULL COMMENT '会议时长',
        PRIMARY KEY (`id`),
        INDEX `meetingId`(`meeting_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议会与人';

CREATE TABLE IF NOT EXISTS `hw_meeting_info` (
       `meeting_id` varchar(125) NOT NULL COMMENT '会议ID',
       `duration` int(48) DEFAULT NULL COMMENT '会议时长',
       `schedule_start_time` datetime DEFAULT NULL COMMENT '会议开始时间',
       `schedule_end_time` datetime DEFAULT NULL COMMENT '会议结束时间',
       `organization_name` varchar(125) DEFAULT NULL COMMENT '参会单位',
       `stage` varchar(48) DEFAULT NULL COMMENT '会议状态',
       `attendee_count` int(10) DEFAULT NULL COMMENT '会议人数',
       `participant_count` int(10) DEFAULT NULL COMMENT '参会节点数',
       `participant_unity` varchar(255) DEFAULT NULL COMMENT '参会节点',
        PRIMARY KEY (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议详情';

CREATE TABLE IF NOT EXISTS `hw_meeting_participant` (
      `id` varchar(128) NOT NULL COMMENT '记录ID',
      `meeting_id` varchar(128) DEFAULT NULL COMMENT '会议ID',
      `name` varchar(128) DEFAULT NULL COMMENT '节点名称',
      `organization_name` varchar(125) DEFAULT NULL COMMENT '组织名称',
      `branch` varchar(48) DEFAULT NULL COMMENT '分院',
      `city` varchar(48) DEFAULT NULL COMMENT '城市',
      `duration` int(10) DEFAULT NULL COMMENT '会议时长',
      `schedule_start_time` datetime DEFAULT NULL COMMENT '会议开始时间',
      `schedule_end_time` datetime DEFAULT NULL COMMENT '会议结束时间',
      `terminal_type` varchar(10) DEFAULT NULL COMMENT '设备型号',
      `stage` varchar(10) DEFAULT NULL COMMENT '会议类型',
      `out_service` varchar(2) DEFAULT NULL COMMENT '是否对外服务',
      PRIMARY KEY (`id`),
      INDEX `meetingId`(`meeting_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议节点（接入点）';

CREATE TABLE IF NOT EXISTS `hw_sync_error_log` (
     `id` varchar(125) NOT NULL COMMENT '记录ID',
     `error_method` varchar(255) DEFAULT NULL COMMENT '错误方法',
     `error_param` varchar(255) DEFAULT NULL COMMENT '错误参数',
     `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
     `error_time` datetime DEFAULT NULL COMMENT '错误时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为同步错误日志表';

CREATE TABLE IF NOT EXISTS `zky_unit` (
            `id` varchar(125) NOT NULL COMMENT '记录ID',
            `participant_name` varchar(125) DEFAULT NULL COMMENT '节点名称',
            `city` varchar(125) DEFAULT NULL COMMENT '城市',
            `branch` varchar(255) DEFAULT NULL COMMENT '分院',
            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='中科院城市节点表';

-- changeset liurz:20230315-zky_unit-001 labels:"新增字段"
alter table zky_unit add participant_code varchar(200) DEFAULT NULL COMMENT '节点编号';
alter table zky_unit add name varchar(200) DEFAULT NULL COMMENT '研究所名称';
alter table zky_unit add create_time datetime DEFAULT NULL COMMENT '记录时间';

-- changeset liangguol:20230315-hw_meeting_alarm-002 labels:"新增字段"
alter table hw_meeting_alarm add alarm_status varchar(48) DEFAULT NULL COMMENT '告警状态';

-- changeset liangguol:20230406-hw_meeting_alarm-002 labels:"新增字段"
alter table hw_meeting_participant add participant_code varchar(80) DEFAULT NULL COMMENT '节点名称code';
alter table hw_meeting_attendee add participant_code varchar(80) DEFAULT NULL COMMENT '节点名称code';

-- changeset liangguol:20230406-zky_send-001 labels:"新增发送接收文件表"
CREATE TABLE `zky_send` (
        `id` varchar(125) NOT NULL COMMENT '记录ID',
        `org_name` varchar(255) DEFAULT NULL COMMENT '组织机构名称',
        `org_code` varchar(255) DEFAULT NULL COMMENT '组织机构编码',
        `start_time` datetime DEFAULT NULL COMMENT '开始时间',
        `end_time` datetime DEFAULT NULL COMMENT '结束时间',
        `send_type` varchar(125) DEFAULT NULL COMMENT '类型',
        `send_scope` varchar(125) DEFAULT NULL COMMENT '单位',
        `receive_num` int(10) DEFAULT NULL COMMENT '接收数量',
        `send_num` int(10) DEFAULT NULL COMMENT '发送数量',
        PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- changeset liangguol:20230508-zky_send-002 labels:"新增字段"
alter table zky_send add send_region int(2) DEFAULT NULL COMMENT '发送区域';

-- changeset liangguol:20230510-zky_send-003 labels:"新增字段"
alter table zky_send add branch varchar (10) DEFAULT NULL COMMENT '分院';
CREATE TABLE `exchange_box_file` (
         `guid` varchar(125) NOT NULL COMMENT '主键ID',
         `file_type` varchar(5) NOT NULL COMMENT '文件类型',
         `total` int(10) DEFAULT NULL COMMENT '总数',
         `flowingTotal` int(10) DEFAULT NULL COMMENT '流转总数',
         `registrationTotal` int(10) DEFAULT NULL COMMENT '登记总数',
         `create_time` datetime DEFAULT NULL COMMENT '时间',
         PRIMARY KEY (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='交换箱文件数据';

CREATE TABLE `exchange_box_usr` (
        `guid` varchar(125) NOT NULL COMMENT '主键ID',
        `total_user` int(10) DEFAULT NULL COMMENT '总用户数',
        `login_count` int(10) DEFAULT NULL COMMENT '用户登录次数',
        `create_time` datetime DEFAULT NULL COMMENT '时间',
        PRIMARY KEY (`guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='交换箱用户数据';
