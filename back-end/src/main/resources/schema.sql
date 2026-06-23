drop table if exists consultation_admin_session_reads;
drop table if exists consultation_admin_reads;
drop table if exists consultation_user_reads;
drop table if exists consultation_reads;
drop table if exists consultations;
drop table if exists companions;
drop table if exists applications;
drop table if exists travel_plans;
drop table if exists users;

create table users (
    id bigint primary key auto_increment,
    employee_no varchar(32) not null,
    name varchar(80) not null,
    email varchar(160) not null,
    role tinyint not null,
    password_md5 varchar(32) not null,
    enabled boolean not null default true,
    created_at timestamp not null default current_timestamp,
    unique key uk_users_employee_no (employee_no)
) engine=InnoDB default charset=utf8mb4;

create table travel_plans (
    id bigint primary key auto_increment,
    plan_no varchar(40) not null,
    destination varchar(120) not null,
    start_date date not null,
    end_date date not null,
    price decimal(12, 2) not null,
    capacity int not null,
    published boolean not null default false,
    file_path varchar(260) null,
    file_name varchar(180) null,
    status tinyint not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    unique key uk_travel_plans_plan_no (plan_no),
    key idx_travel_plans_status (status),
    key idx_travel_plans_published (published)
) engine=InnoDB default charset=utf8mb4;

create table applications (
    id bigint primary key auto_increment,
    plan_id bigint not null,
    user_id bigint not null,
    applicant_count int not null,
    option_text varchar(500) null,
    status tinyint not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    key idx_applications_plan_status (plan_id, status),
    key idx_applications_user (user_id)
) engine=InnoDB default charset=utf8mb4;

create table companions (
    id bigint primary key auto_increment,
    application_id bigint not null,
    name varchar(80) not null,
    gender varchar(20) not null,
    id_card varchar(32) not null,
    bed_needed boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    key idx_companions_application (application_id)
) engine=InnoDB default charset=utf8mb4;

create table consultations (
    id bigint primary key auto_increment,
    plan_id bigint not null,
    user_id bigint not null,
    participant_user_id bigint not null,
    sender_role tinyint not null,
    content varchar(1000) not null,
    status varchar(20) not null default 'OPEN',
    created_at timestamp not null default current_timestamp,
    key idx_consultations_plan_user (plan_id, participant_user_id),
    key idx_consultations_user (user_id),
    key idx_consultations_participant (participant_user_id),
    key idx_consultations_created_at (created_at)
) engine=InnoDB default charset=utf8mb4;

create table consultation_reads (
    plan_id bigint not null,
    participant_user_id bigint not null,
    reader_role tinyint not null,
    reader_user_id bigint not null,
    last_read_at timestamp not null default current_timestamp,
    primary key (plan_id, participant_user_id, reader_role, reader_user_id),
    key idx_consultation_reads_reader (reader_role, reader_user_id),
    key idx_consultation_reads_participant (participant_user_id)
) engine=InnoDB default charset=utf8mb4;
