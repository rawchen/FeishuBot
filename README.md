# 简介

集成ChatGPT的飞书机器人。使用方式：创建群后添加机器人到群中，@机器人发送内容，也可以私聊机器人发送内容。

该项目通过chatgpt账号，与[自建ChatGPT接口代理](https://github.com/linweiyuan/go-chatgpt-api)方式无需科学上网也能使用chatgpt服务。

该项目使用accesstoken方式，无需花费apikey额度，只要有chatgpt账号就能使用。

默认使用3.5模型，plus账户也可以接入4.0模型。

一个账号同一时间只能一次对话，因此对于多人同时使用时，通过配置多账号，自动切换空闲账号服务。

## 效果
![](https://s2.loli.net/2023/07/26/dM8BKgDjuNkZGCb.png)
![](https://s2.loli.net/2023/07/26/nAaQrmKplcjSENw.png)

## 功能支持 

- [x] 飞书集成
- [x] 流式显示
- [x] 支持配置多账户
- [x] 多人使用自动切换账号服务

## 运行环境

JDK1.8

## 飞书配置

[飞书机器人配置和部署](docs/feishu.md)