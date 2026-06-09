create table if not exists users (
    id bigint primary key auto_increment,
    employee_no varchar(32) not null unique,
    name varchar(80) not null,
    email varchar(160) not null,
    role tinyint not null comment '0 管理员，1 普通用户',
    password_md5 varchar(32) not null,
    enabled boolean not null default true,
    created_at timestamp not null default current_timestamp
);

create table if not exists travel_plans (
    id bigint primary key auto_increment,
    plan_no varchar(40) not null unique,
    destination varchar(10) not null,
    start_date date not null,
    end_date date not null,
    price decimal(12, 2) not null,
    capacity int not null,
    published boolean not null default false,
    file_path varchar(260),
    file_name varchar(180),
    status tinyint not null default 0 comment '0=可申请，1=已成团，2=进行中，3=已结束，4=未成团',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists applications (
    id bigint primary key auto_increment,
    plan_id bigint not null,
    user_id bigint not null,
    applicant_count int not null,
    option_text varchar(500),
    status tinyint not null default 0 comment '0 有效，1 已取消',
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint fk_applications_plan foreign key (plan_id) references travel_plans(id) on delete cascade,
    constraint fk_applications_user foreign key (user_id) references users(id),
    index idx_applications_plan_status(plan_id, status),
    index idx_applications_user(user_id)
);

create table if not exists companions (
    id bigint primary key auto_increment,
    application_id bigint not null,
    name varchar(80) not null,
    gender varchar(20) not null,
    id_card varchar(32) not null,
    bed_needed boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint fk_companions_application foreign key (application_id) references applications(id) on delete cascade
);

create table if not exists consultations (
    id bigint primary key auto_increment,
    plan_id bigint not null,
    user_id bigint not null,
    participant_user_id bigint not null,
    sender_role tinyint not null comment '0 管理员，1 普通用户',
    content varchar(1000) not null,
    status varchar(20) not null default 'OPEN',
    created_at timestamp not null default current_timestamp,
    constraint fk_consultations_plan foreign key (plan_id) references travel_plans(id) on delete cascade,
    constraint fk_consultations_user foreign key (user_id) references users(id),
    constraint fk_consultations_participant foreign key (participant_user_id) references users(id),
    index idx_consultations_plan_user(plan_id, participant_user_id)
);
