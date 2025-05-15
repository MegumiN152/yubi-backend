# 建表脚本
# @author <a href="https://github.com/MegumiN152">黄昊</a>
# @from <a href="http://www.huanghao.icu/">GBC智能BI</a>

-- 创建库
create database if not exists yubi;

-- 切换库
use yubi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
    ) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表表
create table if not exists chart
(
    id           bigint auto_increment comment 'id' primary key,
    goal				 text  null comment '分析目标',
    chartData    text  null comment '图表数据',
    chartType	   varchar(128) null comment '图表类型',
    genChart		 text	 null comment '生成的图表数据',
    genResult		 text	 null comment '生成的分析结论',
    userId       bigint null comment '创建用户 id',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

create table if not exists team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       null comment '队伍名称',
    imgUrl      varchar(512)                       null comment '队伍图片',
    userId      bigint                             null comment '队长id',
    description varchar(128)                       null comment '队伍描述',
    maxNum      int                                null comment '最大人数',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍';

create index idx_team_name
    on yubi.team (name(10));

create index idx_team_userId
    on yubi.team (userId);

create table if not exists team_chart
(
    id         bigint auto_increment comment 'id'
        primary key,
    teamId     bigint                             null comment '队伍id',
    chartId    bigint                             null comment '图表id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '队伍图表关系表';

create index idx_team_chart_chartId
    on yubi.team_chart (chartId);

create index idx_team_chart_teamId
    on yubi.team_chart (teamId);

create table if not exists team_user
(
    id         bigint auto_increment comment 'id'
        primary key,
    teamId     bigint                             null comment '队伍id',
    userId     bigint                             null comment '用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '队伍用户关系表';

create index idx_team_user_teamId
    on yubi.team_user (teamId);

create index idx_team_user_userId
    on yubi.team_user (userId);