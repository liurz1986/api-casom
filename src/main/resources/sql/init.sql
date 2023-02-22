-- liquibase formatted sql
-- changeset lianggl:20230222-alarmdeal-001 labels:"华为会议同步"
CREATE TABLE `hw_meeting_alarm` (
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

CREATE TABLE `hw_meeting_attendee` (
       `id` varchar(125) NOT NULL COMMENT '记录ID',
       `meeting_id` varchar(125) DEFAULT NULL COMMENT '会议ID',
       `participant_name` varchar(255) DEFAULT NULL COMMENT '节点名称(会场名称)',
       `user_count` int(10) DEFAULT NULL COMMENT '参会人数',
       `city` varchar(48) DEFAULT NULL COMMENT '城市',
       `branch` varchar(48) DEFAULT NULL COMMENT '分院',
       `duration` int(10) DEFAULT NULL COMMENT '会议时长',
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议会与人';

CREATE TABLE `hw_meeting_info` (
       `meeting_id` varchar(125) NOT NULL COMMENT '会议ID',
       `duration` int(48) DEFAULT NULL COMMENT '会议时长',
       `schedule_start_time` datetime DEFAULT NULL COMMENT '会议开始时间',
       `schedule_end_time` datetime DEFAULT NULL COMMENT '会议结束时间',
       `organization_name` varchar(125) DEFAULT NULL COMMENT '参会单位',
       `stage` varchar(48) DEFAULT NULL COMMENT '会议状态',
       `attendee_count` int(10) DEFAULT NULL COMMENT '会议人数',
       `participant_count` int(10) DEFAULT NULL COMMENT '参会节点数',
       `participant_unity` varchar(255) DEFAULT NULL COMMENT '参会节点',
       `out_service` varchar(2) DEFAULT NULL COMMENT '是否对外服务',
       PRIMARY KEY (`meeting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议详情';

CREATE TABLE `hw_meeting_participant` (
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
      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为会议节点（接入点）';

CREATE TABLE `hw_sync_error_log` (
     `id` varchar(125) NOT NULL COMMENT '记录ID',
     `error_method` varchar(255) DEFAULT NULL COMMENT '错误方法',
     `error_param` varchar(255) DEFAULT NULL COMMENT '错误参数',
     `error_msg` varchar(255) DEFAULT NULL COMMENT '错误信息',
     `error_time` datetime DEFAULT NULL COMMENT '错误时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='华为同步错误日志表';