# 任务调度系统

## 简介

它为您提供秒级、精准、高可靠、高可用的定时（基于 Cron 表达式）任务调度服务。同时提供分布式的任务执行模型，如网格任务。网格任务支持海量子任务均匀分配到所有 Worker（schedule-client）上执行。
## 工作原理

Schedule有三个组件，schedule-console、schedule-server 和 schedule-client。

![](http://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/pic/43136/cn_zh/1543826328302/edas-schedulerX-archit.png)

schedule-console 是 Schedule 的控制台，用于创建、管理定时任务。负责数据的创建、修改和查询。在产品内部与 schedule server 交互。
schedule-server 是 Schedule 的服务端，是 Scheduler的核心组件。负责客户端任务的调度触发以及任务执行状态的监测。
schedule-client 是 Schedule 的客户端。每个接入客户端的应用进程就是一个的 Worker。Worker 负责与 schedule-server 建立通信，让 schedule-server 发现客户端的机器。并向 schedulerx-server 注册当前应用所在的分组，这样 schedulerx-server 才能向客户端定时触发任务。

## 文档
- [Home](https://github.com/makemyownlife/canal/wiki/Home)
- [Introduction](https://github.com/alibaba/canal/wiki/Introduction)
- [QuickStart](https://github.com/alibaba/canal/wiki/QuickStart)
  - [Docker QuickStart](https://github.com/alibaba/canal/wiki/Docker-QuickStart)
  - [Canal Kafka/RocketMQ QuickStart](https://github.com/alibaba/canal/wiki/Canal-Kafka-RocketMQ-QuickStart")
  - [Aliyun RDS for MySQL QuickStart](https://github.com/alibaba/canal/wiki/aliyun-RDS-QuickStart)
  - [Prometheus QuickStart](https://github.com/alibaba/canal/wiki/Prometheus-QuickStart)
- Canal Admin
  - [Canal Admin QuickStart](https://github.com/alibaba/canal/wiki/Canal-Admin-QuickStart)
  - [Canal Admin Guide](https://github.com/alibaba/canal/wiki/Canal-Admin-Guide)
  - [Canal Admin ServerGuide](https://github.com/alibaba/canal/wiki/Canal-Admin-ServerGuide)
  - [Canal Admin Docker](https://github.com/alibaba/canal/wiki/Canal-Admin-Docker)
- [AdminGuide](https://github.com/alibaba/canal/wiki/AdminGuide)
- [ClientExample](https://github.com/alibaba/canal/wiki/ClientExample)
- [ClientAPI](https://github.com/alibaba/canal/wiki/ClientAPI)
- [Performance](https://github.com/alibaba/canal/wiki/Performance)
- [DevGuide](https://github.com/alibaba/canal/wiki/DevGuide)
- [BinlogChange(MySQL 5.6)](https://github.com/alibaba/canal/wiki/BinlogChange%28mysql5.6%29)
- [BinlogChange(MariaDB)](https://github.com/alibaba/canal/wiki/BinlogChange%28MariaDB%29)
- [TableMetaTSDB](https://github.com/alibaba/canal/wiki/TableMetaTSDB)
- [ReleaseNotes](http://alibaba.github.com/canal/release.html)
- [Download](https://github.com/alibaba/canal/releases)
- [FAQ](https://github.com/alibaba/canal/wiki/FAQ)