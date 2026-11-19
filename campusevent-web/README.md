# campusevent-web

CampusEvent 校园活动报名与管理系统的前端，基于 Vue 3 + Vite + Element Plus。

## 启动

要求 Node.js 22.18+。

```powershell
npm install
npm run dev
```

开发地址：http://127.0.0.1:5173

Vite 已把 `/api` 代理到 `http://localhost:8080`，启动前端前需先启动 `campusevent-server`。

## 脚本

| 命令 | 说明 |
|---|---|
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 生产构建，输出到 dist/ |
| `npm run preview` | 预览生产构建 |

## 目录

```
src/
  api/          axios 实例与接口封装
  components/   StatusTag、PolicyTag
  layouts/      MainLayout：学生 / 管理员共用顶栏
  router/       路由与角色守卫
  stores/       Pinia 登录状态
  styles/       全局样式
  utils/        format.js：日期、状态文案
  views/        9 个页面
```

## 页面

- 公共：/login
- 学生：/activities、/activities/:id、/my-registrations、/messages
- 管理员：/admin/activities、/admin/activities/new、/admin/activities/:id/edit、/admin/registrations、/admin/statistics

登录状态保存在 HttpSession，前端通过 `GET /api/auth/me` 恢复；未登录访问受保护路由会跳转登录页。

## 说明

- 演示账号：student / 123456、admin / 123456。
- 生产构建会提示主 chunk 超过 500 kB，这是 Element Plus 全量引入导致的体积提示，不影响运行；后续可按需引入组件或拆分 chunk。
- 项目整体说明、后端启动方式、C1—C10 验收见上级目录 [README.md](../README.md)。
