insert ignore into users(id, employee_no, name, email, role, password_md5, enabled) values
(1, 'S0763', '聂宁波', 'nie-ningbo@zhimingsoft.com', 0, 'e10adc3949ba59abbe56e057f20f883e', true),
(2, 'S0765', '唐笑松', 'tang-xiaosong@zhimingsoft.com', 1, 'e10adc3949ba59abbe56e057f20f883e', true),
(3, 'S0759', '史简', 'shi-jian@zhimingsoft.com', 1, 'e10adc3949ba59abbe56e057f20f883e', true);

-- 日期使用 current_date 相对计算，登录时重新计算状态后仍能稳定覆盖五种计划状态。
insert ignore into travel_plans(id, plan_no, destination, start_date, end_date, price, capacity, published, status) values
-- 可申请：未出发且申请总人数未达到定员。
(1, 'TP20260615001', '敦煌莫高窟', date_add(current_date, interval 90 day), date_add(current_date, interval 96 day), 3280.00, 20, true, 0),
(2, 'TP20260615002', '青海湖', date_add(current_date, interval 60 day), date_add(current_date, interval 64 day), 2980.00, 18, true, 0),
(3, 'TP20260615003', '成都', date_add(current_date, interval 30 day), date_add(current_date, interval 35 day), 2680.00, 16, false, 5),
(4, 'TP20260615004', '西安', date_add(current_date, interval 120 day), date_add(current_date, interval 124 day), 2380.00, 14, false, 5),
-- 已成团：未出发且申请总人数等于定员。
(8, 'TP20260615008', '长白山', date_add(current_date, interval 150 day), date_add(current_date, interval 155 day), 3880.00, 10, true, 1),
-- 进行中：当前日期处于行程日期内，且申请总人数等于定员。
(9, 'TP20260615009', '杭州西湖', date_sub(current_date, interval 2 day), date_add(current_date, interval 2 day), 1980.00, 22, true, 2),
(10, 'TP20260615010', '重庆', date_sub(current_date, interval 1 day), date_add(current_date, interval 3 day), 2280.00, 20, true, 2),
(11, 'TP20260615011', '三亚', current_date, date_add(current_date, interval 5 day), 4680.00, 18, true, 2),
-- 未成团：已经开始或结束，但申请总人数未达到定员。
(12, 'TP20260615012', '南京', date_sub(current_date, interval 20 day), date_sub(current_date, interval 10 day), 1880.00, 16, false, 5),
-- 已结束：行程已结束，且申请总人数等于定员。
(17, 'TP20260615017', '呼伦贝尔', date_sub(current_date, interval 60 day), date_sub(current_date, interval 54 day), 4380.00, 20, true, 3),
(18, 'TP20260615018', '武夷山', date_sub(current_date, interval 40 day), date_sub(current_date, interval 35 day), 2580.00, 15, true, 3),
-- 未成团：行程已结束且申请人数不足；未公开计划不生成申请。
(19, 'TP20260615019', '丽江', date_sub(current_date, interval 30 day), date_sub(current_date, interval 24 day), 3680.00, 18, true, 4),
(20, 'TP20260615020', '珠海', date_sub(current_date, interval 15 day), date_sub(current_date, interval 10 day), 2080.00, 12, false, 5);

-- 所有已公开计划均由三个指定账户提交真实申请，未公开计划不生成申请。
insert ignore into applications(id, plan_id, user_id, applicant_count, option_text, status) values
(1, 1, 1, 2, '希望安排相邻座位并提供行程提醒', 0),
(2, 1, 2, 2, '希望安排靠窗座位', 0),
(3, 1, 3, 2, '需要提前确认集合地点', 0),

(4, 2, 1, 2, '希望安排安静区域座位', 0),
(5, 2, 2, 2, '需要提供详细行李清单', 0),
(6, 2, 3, 2, '希望提前通知天气情况', 0),

(7, 8, 1, 4, '同行人员需要安排相邻房间', 0),
(8, 8, 2, 3, '希望安排靠近电梯的房间', 0),
(9, 8, 3, 3, '需要提前确认接送车辆', 0),

(10, 9, 1, 8, '团队同行，希望统一安排座位', 0),
(11, 9, 2, 7, '希望安排同一楼层住宿', 0),
(12, 9, 3, 7, '需要提前发送每日行程安排', 0),

(13, 10, 1, 7, '希望统一安排往返座位', 0),
(14, 10, 2, 7, '同行人员需要相邻房间', 0),
(15, 10, 3, 6, '需要提前确认集合时间', 0),

(16, 11, 1, 6, '希望安排海景方向房间', 0),
(17, 11, 2, 6, '需要提前确认接机地点', 0),
(18, 11, 3, 6, '希望统一安排用餐座位', 0),

(19, 17, 1, 7, '希望安排靠窗座位并提供提醒', 0),
(20, 17, 2, 7, '同行人员需要相邻住宿', 0),
(21, 17, 3, 6, '需要提前确认当地天气', 0),

(22, 18, 1, 5, '希望安排相邻座位', 0),
(23, 18, 2, 5, '需要提前发送登山注意事项', 0),
(24, 18, 3, 5, '希望统一安排接送车辆', 0),

(25, 19, 1, 2, '希望安排安静房间', 0),
(26, 19, 2, 2, '需要提前确认集合地点', 0),
(27, 19, 3, 2, '希望提供当地天气提醒', 0);

-- 根据每条申请的 applicant_count 自动生成等量随行人，避免出现空白人员信息。
insert ignore into companions(application_id, name, gender, id_card, bed_needed)
select
    a.id,
    case
        when numbers.seq_no = 1 then u.name
        when numbers.seq_no = 2 then '张伟'
        when numbers.seq_no = 3 then '王芳'
        when numbers.seq_no = 4 then '李娜'
        when numbers.seq_no = 5 then '刘洋'
        when numbers.seq_no = 6 then '陈晨'
        when numbers.seq_no = 7 then '赵敏'
        else '周强'
    end,
    case when mod(a.id + numbers.seq_no, 2) = 0 then '男' else '女' end,
    case
        when numbers.seq_no = 1 and a.user_id = 1 then '110101199001011234'
        when numbers.seq_no = 1 and a.user_id = 2 then '110101199202023456'
        when numbers.seq_no = 1 and a.user_id = 3 then '110101199403034567'
        else concat('11010119900101', lpad(a.id * 100 + numbers.seq_no, 4, '0'))
    end,
    case when mod(a.id + numbers.seq_no, 3) = 0 then false else true end
from applications a
join users u on u.id = a.user_id
join (
    select 1 as seq_no
    union all select 2
    union all select 3
    union all select 4
    union all select 5
    union all select 6
    union all select 7
    union all select 8
) numbers on numbers.seq_no <= a.applicant_count
where a.status = 0;
