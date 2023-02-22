-- liquibase formatted sql
-- changeset linaggl:20230222-alarmdeal-001 labels:process_job
INSERT INTO `process_job`(`id`, `job_name`, `status`, `create_time`, `job_cron`, `type`, `day`, `time_str`, `describe`, `service_id`) VALUES (11, 'InitHwMeetingJob', 1, '2023-02-21 09:59:05', NULL, 0, NULL, NULL, '初始化华为会议数据', 'api-alarmdeal');
INSERT INTO `process_job`(`id`, `job_name`, `status`, `create_time`, `job_cron`, `type`, `day`, `time_str`, `describe`, `service_id`) VALUES (12, 'HwMeetingSync', 1, '2023-02-21 09:59:57', '0 */2 * * * ?', 0, NULL, NULL, '华为数据同步任务', 'api-alarmdeal');

