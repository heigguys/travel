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

update users set role = '0' where cast(role as char) = 'ADMIN';
update users set role = '1' where cast(role as char) = 'USER';
alter table users modify role tinyint not null comment '0 管理员，1 普通用户';

create table if not exists travel_plans (
    id bigint primary key auto_increment,
    plan_no varchar(40) not null unique,
    destination varchar(120) not null,
    start_date date not null,
    end_date date not null,
    price decimal(12, 2) not null,
    capacity int not null,
    published boolean not null default false,
    file_path varchar(260),
    file_name varchar(180),
    status tinyint not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists applications (
    id bigint primary key auto_increment,
    plan_id bigint not null,
    user_id bigint not null,
    applicant_count int not null,
    option_text varchar(500),
    status tinyint not null default 0,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint fk_applications_plan foreign key (plan_id) references travel_plans(id) on delete cascade,
    constraint fk_applications_user foreign key (user_id) references users(id),
    index idx_applications_plan_status(plan_id, status),
    index idx_applications_user(user_id)
);

update applications set status = '0' where cast(status as char) = 'ACTIVE';
update applications set status = '1' where cast(status as char) = 'CANCELED';
alter table applications modify status tinyint not null default 0;

update travel_plans p
set status = case
    when coalesce((select sum(a.applicant_count) from applications a where a.plan_id = p.id and a.status = 0), 0) >= p.capacity then '1'
    else '0'
end;
alter table travel_plans modify status tinyint not null default 0;

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
    sender_role tinyint not null,
    content varchar(1000) not null,
    status varchar(20) not null default 'OPEN',
    created_at timestamp not null default current_timestamp,
    constraint fk_consultations_plan foreign key (plan_id) references travel_plans(id) on delete cascade,
    constraint fk_consultations_user foreign key (user_id) references users(id),
    constraint fk_consultations_participant foreign key (participant_user_id) references users(id),
    index idx_consultations_plan_user(plan_id, participant_user_id)
);

update consultations set sender_role = '0' where cast(sender_role as char) = 'ADMIN';
update consultations set sender_role = '1' where cast(sender_role as char) = 'USER';
alter table consultations modify sender_role tinyint not null;
