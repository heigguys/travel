insert ignore into users(id, employee_no, name, email, role, password_md5, enabled) values
(1, 'A001', '管理员', 'admin@zhimingsoft.com', 'ADMIN', 'e10adc3949ba59abbe56e057f20f883e', true),
(2, 'U001', '张三', 'zhangsan@zhimingsoft.com', 'USER', 'e10adc3949ba59abbe56e057f20f883e', true),
(3, 'U002', '李四', 'lisi@zhimingsoft.com', 'USER', 'e10adc3949ba59abbe56e057f20f883e', true);

insert ignore into travel_plans(id, plan_no, destination, start_date, end_date, price, capacity, published, status) values
(1, 'TP20241001001', '敦煌莫高窟', '2026-10-01', '2026-10-07', 1500.00, 20, true, '未开始'),
(2, 'TP20241001002', '青海湖', '2026-10-01', '2026-10-05', 2000.00, 20, true, '未开始'),
(3, 'TP20241001003', '成都', '2026-10-01', '2026-10-06', 5200.00, 20, false, '未开始'),
(4, 'TP20241001004', '西安', '2026-10-01', '2026-10-04', 2500.00, 20, false, '未开始'),
(5, 'TP20241001005', '厦门', '2026-10-01', '2026-10-07', 4000.00, 20, true, '未开始'),
(6, 'TP20241001006', '桂林', '2026-10-01', '2026-10-08', 2490.00, 20, true, '未开始'),
(7, 'TP20241001007', '张家界', '2026-10-01', '2026-10-07', 4000.00, 20, true, '未开始'),
(8, 'TP20241001008', '长白山', '2026-10-01', '2026-10-04', 1500.00, 10, true, '未开始');

insert ignore into applications(id, plan_id, user_id, applicant_count, option_text, status) values
(1, 1, 2, 3, '希望安排靠窗座位', 'ACTIVE'),
(2, 2, 3, 2, '无', 'ACTIVE');
