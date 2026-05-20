# WebNEI 路线图

记录 WebNEI 各阶段的目标、任务拆解与验收。M0/M1/M2/M3 已上线,见 git 历史与现网。

## 状态总览

| 阶段 | 主题 | 状态 |
|---|---|---|
| M0 | 后端骨架 + 前端骨架 + Dataset API | ✅ |
| M1.1 | 物品浏览 + 卡片视图 + 分页 + Mod 筛选 | ✅ |
| M1.2 | 配方 R/U 查询 + 配方面板 + 分类页 + 详情 tab | ✅ |
| M1.3 | 流体浏览 + 流体详情面板 | ✅ |
| M2 | 物品/流体扩展信息(矿典、容器、Thaumcraft 要素、容器全量页) | ✅ |
| M3 | GregTech 配方专属渲染(电压配色、tick 默认 hover 换算、特殊物品行) | ✅ |
| M4.1 | 任务图谱(BetterQuesting):浏览 + 主题适配 + BBCode + 入/出边高亮 + 工具栏 | ✅ |
| M4.2 | 生物浏览 | 🟡 待开始 |
| M5 | 配方分类钻入 + 全文搜索 + 收藏夹 | 🟡 待开始 |
| M6 | 性能 / 缓存 / 部署 / 国际化 | 🟡 待开始 |

---

## 已知阻塞项(待 exporter 修复)

这些不是 webnei 代码问题,是上游数据缺口。

### A) `gregtech_recipe` 表行数 = 0 — ✅ 已修复

- **根因**:`GregTechRecipeFactory.java:49` `gregTechRecipe.owners.stream()` NPE,全 38691 条 GT 配方被吞。
- **修复**(2026-05-14):加 null 守卫。已重跑,81941 行到位。

### B) 流体 PNG 未落盘 — ✅ 已修复

- 原因:用了 `--no-assets`。重跑后 fluid 1391 张 PNG 全部就位。

### C) `nei_recipe_handler` 表 NULL 字段 — ✅ 已修复

- `NeiRegistrySnapshot.safeGetGuiTexture` 返回 null,`gui_texture_resource NOT NULL` 报错。加 `safe()` 包裹。

### D) GT 工具 (gt.metatool) 图标部分空白 — ✅ 已修复

- **现象**:约 2007/12997 (15.4%) 带 NBT 的 GT 工具变体(锯子、锤子、扳手、螺丝刀等)图标渲染为 100% 透明 PNG。游戏内 NEI 正常显示。
- **根因**(2026-05-19 定位完成):exporter 用独立 FBO 渲染时,标准路径(`GuiContainerManager.drawItem` → `MetaGeneratedToolRenderer`)有两个问题:
  1. NEI `safeItemRenderContext` 启用 `GL_DEPTH_TEST`(默认 `GL_LESS`),GT 的工具头/手柄两层贴图都在 z=0.001,第二层因深度测试失败被丢弃 — 这就是"层缺失"
  2. GT 内部反复 `glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)`,在我们 alpha=0 的清屏 framebuffer 上等价于 `out.a = src.a²`,抗锯齿边缘被平方"压"成几乎透明
- **修复**(commit `a1c08d0`):新增专门的 `renderMetaGeneratedTool` 路径,`glDisable(GL_DEPTH_TEST)` + 分离 alpha 的 `glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)`,自己按 toolStats 渲染手柄 + 头两层并应用材质 modulation。`glPushAttrib/glPopAttrib` 隔离状态。重跑后所有 GT 工具变体正常显示。

---

### E) Set A 数据移除 — ✅ 已完成

- **变更**(commit `e819810`,2026-05-19):exporter 移除 4 个 NEI handler 表 + 2 个 UI 模板表 + 2 个浏览视图。
- **删除清单**:`nei_recipe_handler` / `nei_recipe_handler_catalyst` / `nei_gui_overlay` / `nei_transfer_rect` / `recipe_ui_template` / `recipe_ui_template_asset`;视图 `v_nei_recipe_handler_browser` / `v_nei_handler_catalyst_browser`;6 个相关 btree 索引;`NeiRegistrySnapshot.java` 整文件删除。
- **原因**:数据不全且对 WebNEI 前端没有实用价值(in-game UI 像素还原非目标)。
- **影响**:exporter 版本号 `0.6.0 → 0.6.1`,产物体积/SQL 行数下降。webnei 后端无引用、零冲击。

### F) schema.sql 不支持已有库的增量列升级 — 🟡 暂缓

- **现象**:导出的 schema.sql 用 `CREATE TABLE IF NOT EXISTS`,表已存在则整条建表语句被跳过,新版本加的列不会自动补到旧表上。例如本次新增 `recipe.description` 列后,旧库重跑 schema.sql 时 `CREATE OR REPLACE VIEW v_recipe_lookup_detail` 引用 `r.description` 直接报 `[42703] 字段不存在`。
- **不能简单 DROP 重建**:多 dataset 共用同一份库,DROP 会清掉其他版本的数据;schema.sql 必须保持"零数据动作"。
- **方向**:`PostgresSchemaWriter` 生成时,对每张表的每一列都额外吐一条 `ALTER TABLE <tbl> ADD COLUMN IF NOT EXISTS <col> <type> ...`,让 schema.sql 变成真正幂等的迁移脚本。新库 ALTER 是 no-op,老库自动补齐缺列;以后任何加列变更都自动覆盖。
- **决定**(2026-05-20):暂缓。开发期遇到时手动 `DROP DATABASE` 重建即可。等所有 milestone 完成、schema 收敛后再统一改造 exporter,避免开发期反复改 writer 逻辑。

---

## M2 · 物品 / 流体扩展信息 — ✅ 已完成

**实现于 2026-05-15**:

- 后端 `extras` package: `GET /items/{id}/extras` + `GET /fluids/{id}/extras` + `GET /items/{id}/containers`(全量容器表分页页用)
- 物品 extras: 矿典词条、流体容器(摘要 + 计数,超过 50 → 跳独立页)、Thaumcraft 要素、参与配方计数(分 recipe/usage)
- 流体 extras: 容器矩阵、对应方块、参与配方计数
- 前端 `useExtrasStore` Pinia store,inflight 去重 + dataset 切换清缓存
- `ItemDetailPanel` / `FluidDetailPanel` "扩展信息" Card 真填:计数卡片(点击切 lookup tab)、矿典 tag、容器三元组(点击跳详情)、要素 chip
- 独立页 `ItemContainersView` 显示某个物品作为容器/空桶映射的全量列表(支持搜索 + 容量排序)

---

## M3 · GregTech 配方专属渲染 — ✅ 已完成

**实现于 2026-05-15**:

- 后端 `RecipeDao.loadGregTechInfo` + `loadGregTechSpecialItems` batch 查 `gregtech_recipe` + `gregtech_recipe_special_item`
- DTO `GregTechRecipeDto` 去掉无意义的 `requiresLowGravity`(GTNH 2.8.4 全 0),加 `specialItems`
- `GregTechBadge.vue` 全部重写:
  - **电压等级**:15 档 ULV→MAX 各有专属配色 chip(半透明背景 + 同色边框 + 同色文字)
  - **耗电 / 电流 / 时长**:中性灰 chip,完整数字 `1,920 EU/t`,千分隔
  - **时长**默认 ticks,hover 显示秒/分/时换算
  - **洁净室**:橙色 warning chip,只在需要时出现
  - **特殊物品**(446 条 IV+ 装配线模板):图标 + 名字 chip,点击跳详情
- 整体融入 RecipePanel chip 风格,不喧宾夺主

---

## M4 · 任务图谱 + 生物浏览

### 4.1 任务图谱(BetterQuesting) — ✅ 已完成

**背景**:用户之前明确 WebNEI 不做任务(`feedback-project-webnei-scope` memory),但导出数据完整后做了个 demo,实际体验良好,决定正式保留。

**已实现**:

**后端 `quest` package**(2026-05-18):
- `GET /quest-lines` 列任务线
- `GET /quest-lines/{lineId}` 单线节点 + 依赖边(SQL 层过滤跨线边)
- `GET /quests/{questId}` 任务详情(task + reward + 通过 ingredient_entry 物品组)
- 路由用 `?id=<base64>` query 参数避开 base64 padding `==` 在路径里被 Vue Router 路径匹配丢字符

**前端**(2026-05-18 至 2026-05-19):
- `QuestLineList.vue` 任务线卡片网格
- `QuestLineCanvas.vue` 用 AntV G6 v5 画依赖图,核心修复:
  - **节点尺寸 1:1**(不再 `+4`,匹配 BQ pos/size 坐标系,相邻节点缝隙正确)
  - 节点用 G6 `rect` + `iconSrc`,`imageSmoothingEnabled=false` 保持像素贴图清晰
  - autoFit + padding,大小节点共存时按比例
  - 浮层工具栏:适应窗口 / 1:1 / 居中 / 缩放 ±
- **主题适配**:浅 / 深色模式动态切换,palette 套色,主题切换时重渲
- **入边 / 出边 hover 区分**:hover 节点时入边(前置)蓝色、出边(后续)橙色,通过 G6 `state` 机制实现
- `QuestText.vue` BetterQuesting BBCode 解析(`[note]/[warn]/[quest]/[url]/[bold]/[italic]/[underline]/[strikethrough]/[obfuscated]`),递归下降,XSS 防护(`javascript:` 等协议降级)
- 抽屉显示 task / reward / 物品 chip(可点跳详情),BBCode 描述渲染

**Exporter 配套修复**(2026-05-19):
- `QuestFactory` / `QuestLineFactory` 改用 `QuestTranslation.translateQuestName(UUID, IQuest)` 等带 UUID 重载,txloader 注入的 `betterquesting.quest.<uuid>.name` 翻译生效(commit `ae5e072`)
- `QuestLineProcessor` 改用 `QuestLineDatabase.INSTANCE.getOrderedEntries()`,任务线表加 `order_index` 列保留 BQ 内显示顺序;webnei DAO `ORDER BY ql.order_index, ql.quest_line_id`

**可选后续**(优先级低):
- 节点密集时(如 Tier 1 155 节点)的局部缩放定位/搜索
- `SECRET / HIDDEN` 任务可见性策略(目前一视同仁展示)

### 4.2 生物浏览 — 🟡 待开始

**目标**:生物详情 + 掉落表展示。

**输入**:
- 表:`mob`、`mob_variant`、`mob_drop`、`mob_info`
- 视图:`v_mob_variant_browser`

**后端**:
1. `GET /api/datasets/{id}/mobs?q&modId&page&size` — 基于 `v_mob_variant_browser`
2. `GET /api/datasets/{id}/mobs/{mobVariantId}` — 详情 + 掉落表
   - mob_info 字段:`allowedInPeaceful`、`soulVialUsable`、infernal 标识等
   - drops:`mob_drop` join 物品/流体显示信息

**前端**:
1. `MobBrowser.vue`:卡片网格(渲染图 + 名称 + Mod + 血量/护甲快照)
2. `MobDetailPanel.vue`:Hero(大渲染图 + 标签) + 基本属性 + 掉落表(概率排序,点物品跳详情)
3. `RecipeLookup` 的"详情"tab 检测 mob_variant_id 也能展示

**估算**: ~1.5 天

---

## M5 · 浏览体验深化

### 5.1 配方分类钻入

- `CategoryGallery` 卡片点击 → `/datasets/{id}/categories/{categoryId}` 列出该类全部配方(分页)
- 类似 lookup 但 target 是 category 而非 item

### 5.2 全文搜索

- 后端开 `pg_trgm` 索引(只对 `display_name` + `registry_name`)
- 顶栏加全局搜索框,跨物品/流体/配方分类,结果分组
- API:`GET /api/datasets/{id}/search?q=&kind=...`

### 5.3 收藏夹 / 历史

- 完全前端化:localStorage 存 `{datasetId, type, targetId}[]`
- 顶栏抽屉显示最近浏览物品列表
- 详情页加"⭐ 收藏"按钮

### 5.4 路径推算(尝试性)

- 从材料 → 目标物品的最短合成路径(BFS in `recipe_lookup_index`)
- 这块不在 NEI 原版,但 GTNH 玩家常用 — 可放低优先级

### 估算

5.1 ~1 天,5.2 ~2 天,5.3 ~1 天,5.4 ~3 天(可选)

---

## M6 · 工程 / 部署

### 6.1 性能

- **后端**:lookup 大页(48 行)时 JOIN ingredient_entry 会爆,做 `pageSize <= 48` 强制 + 慢查询日志
- **前端**:首屏 Vite 构建 chunk 拆分(element-plus 独立 chunk;`vue-virtual-scroller` 按需 import)
- 图标懒加载已就绪;加 `<picture>` 或 WebP 转换 — 90MB 资源压缩到 ~30MB
- 加 Spring Boot caching:`@Cacheable` 对 `recipe_category` / `mod` 列表(几乎不变,内存缓存 10min)

### 6.2 部署

- Dockerfile:`FROM eclipse-temurin:21-jre`,COPY jar + 健康检查
- 资源目录 mount(只读):`-v /data/webnei-assets:/app/assets:ro`
- 反向代理:Nginx 把 `/assets/` 直出文件,绕过 JVM
- CI:GitHub Actions `build + dockerize + push`

### 6.3 国际化

- Element Plus 内置 zh-cn ✓
- 自有文案:目前都是中文 — 用 vue-i18n 抽 message 表,加 en-US 等
- 物品名称:Minecraft 资源包驱动,exporter 端导出多语言时改 schema

### 6.4 多数据集体验

- 切换数据集时清缓存(stores 里清 categoryMap、modList)
- URL 切换 dataset 时尝试保留 target(如果新数据集存在同名 item_variant_id 就跳过去)
- 数据集对比模式(高难度,M7+)

### 估算

6.1 ~2 天,6.2 ~1.5 天,6.3 ~2 天,6.4 ~1 天

---

## 跨阶段技术债

逐步还,不要在某个 milestone 一次清算。

- **类型重复**:`ItemDetail`、`FluidDetail`、`Recipe` 等 TS 类型手写;考虑用 `openapi-typescript` 从 Spring controller 反生成
- **API 客户端**:axios 实例直接散在 `api/*.ts`,可以做一层 `useApi(datasetId)` composable,自动注入 dataset 参数
- **错误处理**:目前都是 `err.message` toast,缺乏统一的 `ApiError` 解析 + Sentry / 自托管 errortracking
- **测试**:无 — 至少给关键 DAO 加 testcontainer postgres 集成测试,给 RecipePanel 加截图回归测试
- **CLAUDE.md**:在 webnei 根目录初始化一份开发约定文档(命名、目录、提交格式),减少协作摩擦
- **路由元信息**:`route.meta.title` + `document.title` 自动更新

---

## 风险/未决

| 项 | 说明 |
|---|---|
| Exporter 重跑成本 | 一次 ~60 分钟(`--no-assets` 几分钟);schema 变更 / 渲染逻辑改动都需要重跑 |
| `recipe_slot_layout` 数据质量 | M1.2 暴露出某些类的 W×H 为 0、shapeless 配方 W×H 不实用;后续可能需要 exporter 端补维度 |
| Asset 体积 | 单数据集 ~90MB,多数据集叠加;CDN/磁盘策略要早定 |

---

## 决策日志(供 future-me)

- **不用 SSE/WebSocket**:数据是离线快照,无实时需求,REST 足够
- **不用 GraphQL**:schema 已稳定;REST + 路径化资源更容易缓存
- **前端用 Element Plus 而非 shadcn-vue**:NEI 类应用密集表格/树/抽屉/对话框多,EP 组件覆盖度高
- **配方面板放弃 NEI 像素坐标**:可读性远比像素还原重要;Shaped 保留 slot_index 位置,无序压缩展示
- **GT 数据缺失暂不绕过**:数据/UI 分层清晰,等上游修好直接生效;不在前端伪造数据
- **任务图谱正式保留**(2026-05-19):原先认为 NEI 不含任务,但实测体验良好且数据完整,正式纳入
- **删除 NEI handler / UI 模板表**(Set A, 2026-05-19):数据不全且对前端无价值,decouple 简化 schema
