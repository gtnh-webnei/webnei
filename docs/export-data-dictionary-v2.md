# NESQL PostgreSQL 导出数据字典 v2

本文档对应 `schema_version = 2` 的 `schema.sql` / `data.sql`。它描述当前导出 SQL 的表、字段含义、ID 规则和前端使用方式。

## 通用规则

- `dataset_id`：整包数据集 ID，格式为 `<pack_slug>:<pack_version>:<variant>`，例如 `gtnh:2.8.4:official`。
- `*_id`：业务引用 ID。物品、流体、生物等尽量使用 MC/Forge 自然身份；只有 NBT、配方、候选组等无法自然表达的部分使用短 hash。
- `runtime_*_id`：MC 运行时数字 ID，只用于排查，不作为跨版本业务 ID。
- `nbt_hash`：`nbt_text` 的短 hash。无 NBT 时为空字符串。
- `asset_id`：优先等于 `assets.zip` 内路径，便于前端直接定位资源。
- 文本空值当前导出为空字符串；SQL `NULL` 尽量不用，方便 DataGrip/JDBC 执行和前端处理。

## 索引和视图

`schema.sql` 会额外创建一组 `idx_nesql_*` btree 索引。索引目标是前端常见访问路径：NEI 物品面板分页、物品/流体/实体按 Mod 和名称浏览、R/U 配方查询、配方分类页、机器/catalyst 入口、资源来源解析、任务和 aspect 反查。

`schema.sql` 也会创建只读视图，前端优先从视图取列表页数据，只有进入详情页或调试时再查底层表。

| 视图 | 用途 |
|---|---|
| `v_item_variant_browser` | 物品变体、物品定义、图标路径合并视图。用于 NEI 物品列表和搜索结果。 |
| `v_fluid_variant_browser` | 流体变体、流体定义、图标路径合并视图。 |
| `v_mob_variant_browser` | 实体变体、实体定义、渲染图路径合并视图。 |
| `v_recipe_category_base` | 配方分类、布局、图标等基础展示数据，不含数量聚合。 |
| `v_recipe_category_counts` | 每个配方分类的配方数量和机器数量聚合。 |
| `v_recipe_category_browser` | 分类浏览页用视图，由 `v_recipe_category_base` 与 `v_recipe_category_counts` 合并。 |
| `v_recipe_item_slot` | 展开后的配方物品槽位。输入组会展开到候选物品，输出直接使用输出物品。 |
| `v_recipe_fluid_slot` | 展开后的配方流体槽位。输入组会展开到候选流体，输出直接使用输出流体。 |
| `v_nei_recipe_handler_browser` | NEI handler、tab 图标、GUI 纹理、catalyst 数量合并视图。 |
| `v_nei_handler_catalyst_browser` | NEI handler 的 catalyst/机器入口 + 物品显示数据。 |

## 数据集和 Mod

### `dataset`

一次整包导出。

| 字段 | 说明 |
|---|---|
| `dataset_id` | 数据集主键，例如 `gtnh:2.8.4:official`。 |
| `pack_slug` | 整包短名，例如 `gtnh`。 |
| `pack_version` | 整包版本，例如 `2.8.4`。 |
| `variant` | 数据变体，例如 `official`、`local`。 |
| `display_name` | 人类可读名称。 |
| `schema_version` | 导出 schema 版本。当前为 `2`。 |
| `exporter_version` | 导出器版本。 |
| `created_at` | 导出时间。 |
| `minecraft_version` | MC 版本。 |
| `active_plugins` | 本次启用的采集插件 JSON 数组。 |

### `mod`

本次运行加载的 Mod 列表。

| 字段 | 说明 |
|---|---|
| `dataset_id` | 所属数据集。 |
| `mod_id` | FML/Forge Mod ID。 |
| `name` | Mod 显示名。 |
| `version` | Mod 版本。 |
| `source_type` | 来源类型，例如 jar 或 dev classpath。 |
| `source_file_name` | 来源文件名。 |
| `source_sha256` | 来源文件 hash。 |
| `enabled` | 是否启用。 |

## 资源和渲染

### `asset`

前端可显示的规范资源快照，包括物品图标、流体图标、生物渲染图、生成版 NEI UI 基础素材，以及从当前 ResourceManager 拷出的 NEI/Minecraft 原始 GUI 纹理。

| 字段 | 说明 |
|---|---|
| `dataset_id` | 所属数据集。 |
| `asset_id` | 资源 ID；当前通常等于 `assets.zip` 内路径。 |
| `kind` | 资源类型：`item_icon`、`fluid_icon`、`mob_render`、`ui_texture` 等。 |
| `usage` | 主要用途：`inventory_icon`、`fluid_icon`、`entity_portrait` 等。 |
| `path` | 资源在 `assets.zip` 内的路径。 |
| `sha256` | 资源内容 hash。 |
| `width` / `height` | PNG 尺寸。 |
| `content_type` | 当前为 `image/png`。 |
| `render_style` | 渲染方式：`flat_icon`、`fluid_icon`、`entity_portrait` 等。 |
| `generated` | 是否由运行时渲染/导出器生成。原始 ResourceLocation 纹理为 `false`。 |
| `frame_count` | 动画帧数；当前规范快照为 `1`。 |
| `representative_frame` | 代表帧序号；静态图为 `0`。 |

### `asset_usage`

资源和业务对象的使用关系，前端通过它判断图片用在什么地方。

| 字段 | 说明 |
|---|---|
| `asset_id` | 资源 ID。 |
| `owner_type` | 使用者类型：`item_variant`、`fluid_variant`、`mob_variant`、`recipe_category` 等。 |
| `owner_id` | 使用者 ID。 |
| `usage` | 使用角色，例如 `inventory_icon`、`entity_portrait`。 |
| `display_order` | 同一对象多个资源时的排序。 |

### `asset_source`

资源来源追踪。用于解释“这个图标为什么长这样”。

| 字段 | 说明 |
|---|---|
| `asset_id` | 资源 ID。 |
| `source_type` | 来源类型：`item_renderer`、`fluid_icon`、`entity_renderer`、`resource_location`。 |
| `resource_domain` | ResourceLocation domain，例如 `minecraft`、`gregtech`。 |
| `resource_path` | ResourceLocation path 或注册名。 |
| `atlas` | 所属 atlas，例如 items/blocks。 |
| `sprite_name` | atlas sprite 名称。 |
| `renderer_class` | renderer class；当前多数情况下为空。 |
| `render_notes` | 无法结构化的渲染说明。 |

### `asset_frame`

动画或精灵图的帧信息。当前每个规范资源至少写第 0 帧。

| 字段 | 说明 |
|---|---|
| `asset_id` | 逻辑资源。 |
| `frame_index` | 帧序号。 |
| `path` | 该帧 PNG 路径。 |
| `sha256` | 该帧 hash。 |
| `duration_ms` | 帧持续时间；未知为 `0`。 |
| `width` / `height` | 帧尺寸。 |

### `render_profile`

运行时渲染快照参数。

| 字段 | 说明 |
|---|---|
| `asset_id` | 资源 ID。 |
| `profile_id` | 渲染配置，例如 `item-icon-v1`、`mob-portrait-v1`。 |
| `renderer_version` | 导出器渲染逻辑版本。 |
| `image_size` | 输出尺寸。 |
| `background` | 背景，当前为 `transparent`。 |
| `camera_yaw` / `camera_pitch` | 实体相机角度；物品/流体通常为 0。 |
| `scale` | 渲染缩放。 |
| `pose` | 渲染姿态，例如 `inventory`、`idle`。 |
| `render_nbt` | 参与渲染的 NBT。 |

## 物品和 NEI 集合

### `item`

Forge 注册表中的物品定义。

| 字段 | 说明 |
|---|---|
| `item_id` | 物品定义 ID，格式 `mod_id:registry_name`。 |
| `mod_id` | 注册 Mod。 |
| `registry_name` | 注册名本地部分。 |
| `unlocalized_name` | 未本地化名称。 |
| `max_stack_size` | 最大堆叠数。 |
| `max_damage` | 最大耐久/损坏值。 |
| `runtime_item_id` | MC 运行时数字 ID。 |

### `item_variant`

NEI、配方和渲染实际使用的物品变体。

| 字段 | 说明 |
|---|---|
| `item_variant_id` | 变体 ID，格式 `item_id@damage[#nbt_hash]`。 |
| `item_id` | 所属物品定义。 |
| `damage` | metadata/damage。 |
| `nbt_hash` | NBT 短 hash。 |
| `nbt_text` | 原始 NBT 文本。 |
| `display_name` | 显示名。 |
| `tooltip_text` | tooltip 文本。 |
| `asset_id` | 物品图标资源。 |

### `item_tool_class`

工具类型和等级。

| 字段 | 说明 |
|---|---|
| `item_variant_id` | 物品变体。 |
| `tool_class` | 工具类型，例如 `pickaxe`。 |
| `harvest_level` | 挖掘等级。 |

### `item_collection`

通用物品集合，用于 NEI 面板、配方候选组、矿辞组、任务需求等。

| 字段 | 说明 |
|---|---|
| `collection_id` | 集合 ID。NEI 面板固定为 `nei:item_panel`。 |
| `domain` | 集合域，当前为 `item`。 |
| `source` | 来源：`nei`、`ingredient_group` 等。 |
| `kind` | 类型：`item_panel`、`explicit`、`ore_dictionary` 等。 |
| `name` | 人类可读名称。 |
| `source_ref` | 来源引用，例如原始候选组 ID。 |
| `filter_text` | NEI filter 表达式；当前未采集时为空。 |
| `parent_collection_id` | 父集合 ID。 |
| `sort_index` | 集合排序。 |
| `expanded_by_default` | 默认是否展开。 |

### `item_collection_entry`

物品集合成员。

| 字段 | 说明 |
|---|---|
| `collection_id` | 集合 ID。 |
| `item_variant_id` | 成员物品变体。 |
| `stack_size` | 成员堆叠数。 |
| `entry_index` | 集合内顺序。 |
| `match_reason` | 成员来源原因，例如 `nei_item_panel`、`explicit`。 |

### `nei_item_panel_entry`

NEI 物品面板顺序。当前按导出的 item variant 顺序写入；NEI collapsible 组后续可补。

| 字段 | 说明 |
|---|---|
| `item_variant_id` | 物品变体。 |
| `panel_index` | 面板顺序。 |
| `collapsible_collection_id` | 所属 NEI 折叠集合；当前为空。 |
| `visible_when_collapsed` | 折叠时是否可见；当前为 `true`。 |

## 流体和生物

### `fluid`

Forge FluidRegistry 中的流体定义。

| 字段 | 说明 |
|---|---|
| `fluid_id` | 流体定义 ID，格式 `mod_id:registry_name`。 |
| `mod_id` | 注册 Mod。 |
| `registry_name` | 流体注册名本地部分。 |
| `unlocalized_name` | 未本地化名称。 |
| `display_name` | 显示名。 |
| `runtime_fluid_id` | 运行时数字 ID。 |
| `luminosity` | 亮度。 |
| `density` | 密度。 |
| `temperature` | 温度。 |
| `viscosity` | 黏度。 |
| `gaseous` | 是否气体。 |

### `fluid_variant`

带 NBT 的 FluidStack 变体。

| 字段 | 说明 |
|---|---|
| `fluid_variant_id` | 流体变体 ID，格式 `fluid_id[#nbt_hash]`。 |
| `fluid_id` | 流体定义。 |
| `nbt_hash` / `nbt_text` | NBT 信息。 |
| `asset_id` | 流体图标。 |

### `mob`

实体定义。

| 字段 | 说明 |
|---|---|
| `mob_id` | 生物定义 ID，格式 `mod_id:entity_name`。 |
| `mod_id` | 来源 Mod。 |
| `entity_name` | 实体内部名。 |
| `display_name` | 显示名。 |
| `width` / `height` | 实体碰撞盒尺寸。 |
| `max_health` | 最大生命。 |
| `armor` | 护甲值。 |
| `immune_to_fire` | 是否免疫火焰。 |
| `leashable` | 是否可栓绳。 |

### `mob_variant`

带 NBT 的实体变体。

| 字段 | 说明 |
|---|---|
| `mob_variant_id` | 生物变体 ID，格式 `mob_id[#nbt_hash]`。 |
| `mob_id` | 生物定义。 |
| `nbt_hash` / `nbt_text` | NBT 信息。 |
| `asset_id` | 实体渲染图。 |

## NEI 配方 UI 和配方

### `recipe_category`

NEI 配方分类/处理器。一个 category = 一个“已注册的配方结构”：slot 网格尺寸固定，info 字段类型/名称固定。具体内容（数值、物品、metadata 值）写在 `recipe` / `gregtech_recipe` / `gregtech_recipe_metadata`。

GregTech 的 category 已按机器合并，**不再按电压拆分**：同一台机器的 15 个电压共用 1 个 `category_id`，电压退化为 `gregtech_recipe.voltage_tier` / `voltage` / `amperage` 上的 recipe 属性。fuel 同理合并。

| 字段 | 说明 |
|---|---|
| `category_id` | 分类 ID，例如 `minecraft:crafting:shaped`、`gregtech:assembler`、`gregtech:fuel_diesel`。 |
| `plugin` | 来源插件/Mod,例如 `minecraft`、`gregtech`。 |
| `handler_id` | 处理器 ID,通常为分类前两段。 |
| `display_name` | 分类显示名。 |
| `shapeless` | 是否无序配方。 |
| `icon_item_variant_id` | 分类图标物品。优先来自 NEI `HandlerInfo.itemStack`；当 HandlerInfo 用贴图当图标时为空。 |
| `icon_info` | 图标角标/附加文本。 |
| `item_input_width` / `item_input_height` | 物品输入网格尺寸。 |
| `fluid_input_width` / `fluid_input_height` | 流体输入网格尺寸。 |
| `item_output_width` / `item_output_height` | 物品输出网格尺寸。 |
| `fluid_output_width` / `fluid_output_height` | 流体输出网格尺寸。 |
| `supports_recipe_lookup` | 是否支持“查询合成”。 |
| `supports_usage_lookup` | 是否支持“查询用途”。 |
| `display_order` | 分类排序。 |
| `mod_id` | NEI handler 所属 mod ID。来自 `HandlerInfo.modId`。 |
| `mod_name` | NEI handler 所属 mod 显示名。来自 `HandlerInfo.modName`。 |
| `handler_class` | NEI handler Java 全类名（调试 / 路由识别用）。 |
| `handler_canvas_width` / `handler_canvas_height` | NEI 配方面板画布尺寸。 |
| `handler_y_shift` | NEI 内部 Y 偏移。 |
| `handler_multiple_widgets_allowed` | 同一页面是否允许多个 recipe widget。 |
| `icon_image_resource` | 贴图图标的 ResourceLocation，格式 `domain:path`。当 HandlerInfo 用贴图当图标时填，否则为空。对应 PNG 由导出器抠到 `exports/<dataset>/nei-texture/<domain>/<path>`，在 `nei_texture_export` 表登记。 |
| `icon_image_x` / `icon_image_y` | 贴图图标在原始大图中的裁剪起点。 |
| `icon_image_width` / `icon_image_height` | 贴图图标的裁剪宽高。 |
| `icon_image_texture_width` / `icon_image_texture_height` | 原始贴图整体尺寸（用于 UV 计算）。 |

> UI 类型 / 模板 / 动态 annotation 当前**不写在导出数据里**。前端按 `category_id` 路由到对应 renderer（[`registry.ts`](../ui/src/components/recipe/registry.ts)），后端按 category 派生 `infoSchema` 描述如何把 `gregtech_recipe` 列和 `gregtech_recipe_metadata` 行解释为带 label / valueType 的字段。

### `recipe_category_machine`

配方分类对应的机器/工作台图标。

GregTech 的 category 已按机器合并，因此**不再写电压 tier 行**：一个 GT category 通常只有一行 machine（通用图标），具体电压由 `gregtech_recipe.voltage_tier` 在 recipe 维度承载。

| 字段 | 说明 |
|---|---|
| `category_id` | 配方分类。 |
| `item_variant_id` | 机器或工作台物品。 |
| `role` | `machine` 或 `workstation`。 |
| `opens_category` | 点击该机器是否进入该配方分类。 |
| `display_order` | 排序。 |
| `source_ref` | 原始 RecipeType ID。 |

### `recipe_category_layout`

分类级 UI 画布配置。

| 字段 | 说明 |
|---|---|
| `category_id` | 配方分类。 |
| `canvas_width` / `canvas_height` | NEI 风格画布尺寸。 |
| `background_asset_id` | 背景资源；当前未采集时为空。 |
| `layout_version` | 布局版本。 |

### `recipe_ui_template`

> 重构中移除：UI 模板由前端 [`registry.ts`](../ui/src/components/recipe/registry.ts) 按 `category_id` 直接路由，不再以表形式导出。

### `recipe_ui_template_asset`

> 重构中移除：归一化模板素材（背景 / slot / 箭头 / 能量条）不再随导出落库。前端按 `category_id` 选 renderer，自行绘制；NEI 原始 handler GUI 纹理仍通过 `nei_recipe_handler.gui_texture_asset_id` 暴露。

### `nei_recipe_handler`

NEI 运行时实际注册的 recipe/usage handler 列表。这个表来自 `GuiCraftingRecipe` / `GuiUsageRecipe` 的 handler registry，用于还原 NEI tab、真实 GUI 纹理、handler 所属 Mod 和 overlay 标识。

| 字段 | 说明 |
|---|---|
| `handler_key` | 导出器生成的 handler 行 ID，格式 `nei:<role>:<main 或 serial>:<recipe_id 或 handler_id>:<registry_order>`。 |
| `role` | `crafting` 或 `usage`。 |
| `serial` | 是否在 NEI serial handler 队列中执行。 |
| `handler_id` | `IRecipeHandler.getHandlerId()`。 |
| `recipe_id` | `RecipeCatalysts.getRecipeID(handler)`，NEI catalyst 匹配优先使用这个 ID。 |
| `overlay_identifier` | `handler.getOverlayIdentifier()`。 |
| `recipe_name` / `recipe_tab_name` | NEI 页面标题和 tab tooltip 名称。 |
| `handler_class` | handler 实现类。 |
| `gui_class` | `TemplateRecipeHandler.getGuiClass()`；没有绑定 GUI 时为空。 |
| `gui_texture_resource` | `TemplateRecipeHandler.getGuiTexture()` 返回的 ResourceLocation 文本。 |
| `gui_texture_asset_id` | 对应已拷入 `assets.zip` 的原始 GUI 纹理资源。 |
| `tab_icon_item_variant_id` | HandlerInfo 使用物品作为 tab 图标时对应的物品变体。 |
| `tab_image_resource` / `tab_image_asset_id` | HandlerInfo 使用图片作为 tab 图标时的原始纹理和资产。 |
| `tab_image_u` / `tab_image_v` / `tab_image_width` / `tab_image_height` | tab 图标在原始纹理中的裁剪区域。 |
| `mod_id` / `mod_name` | HandlerInfo 中声明的所属 Mod。 |
| `canvas_width` / `canvas_height` | HandlerInfo 尺寸，等价于 NEI recipe widget 区域尺寸。 |
| `y_shift` | HandlerInfo 的 Y 偏移。 |
| `multiple_widgets_allowed` | 同页是否允许多个 recipe widget。 |
| `show_favorites_button` / `show_overlay_button` | NEI tab 按钮显示配置。 |
| `use_custom_scroll` | 是否使用自定义滚动逻辑。 |
| `loaded_recipe_count` | 当前 handler 实例已加载的 recipe 数。注册原型通常为 `0`。 |
| `display_order` | handler registry 顺序。 |

### `nei_recipe_handler_catalyst`

NEI handler 对应的 catalyst/机器入口。前端可以用它生成类似 NEI 左侧 recipe catalyst 列表。

| 字段 | 说明 |
|---|---|
| `handler_key` | 对应 `nei_recipe_handler.handler_key`。 |
| `item_variant_id` | catalyst 物品/机器。 |
| `stack_size` | 显示堆叠数量。 |
| `display_order` | NEI catalyst 顺序。 |
| `source_ref` | 来源说明，当前为 `RecipeCatalysts.getRecipeCatalysts`。 |

### `nei_gui_overlay`

NEI 问号 overlay 和物品摆放 positioner 注册表，来自 `RecipeInfo`。

| 字段 | 说明 |
|---|---|
| `overlay_index` | 导出排序序号。 |
| `gui_class` | 被 overlay 的 `GuiContainer` 类。 |
| `ident` | overlay 标识，例如 `crafting`、`smelting`。 |
| `overlay_handler_class` | 自定义 overlay handler 类。 |
| `positioner_class` | 默认 stack positioner 类。 |
| `offset_x` / `offset_y` | NEI 在该 GUI 中摆放 recipe slot 的偏移。 |

### `nei_transfer_rect`

NEI recipe UI 内可点击热区。点击这些区域会跳转到其他 recipe/usage 页面，例如炉子箭头跳到 smelting，燃料槽跳到 fuel。

| 字段 | 说明 |
|---|---|
| `owner_type` | `nei_recipe_handler` 或 `gui_class`。 |
| `owner_id` | handler_key 或 GUI class 名。 |
| `gui_class` | 热区所在 GUI class；未知时为空。 |
| `rect_index` | 同一 owner 下热区序号。 |
| `x` / `y` / `width` / `height` | 热区坐标和尺寸，使用 NEI recipe GUI 像素坐标。 |
| `output_id` | 点击后传给 NEI 的 output/input id，例如 `item`、`fuel`。 |
| `results_text` | 跳转参数的调试文本。 |

### `recipe_slot_layout`

分类里每个 slot 的坐标。

| 字段 | 说明 |
|---|---|
| `category_id` | 配方分类。 |
| `role` | `item_input`、`fluid_input`、`item_output`、`fluid_output`。 |
| `slot_index` | slot 序号。 |
| `x` / `y` | slot 左上角坐标。 |
| `width` / `height` | slot 尺寸。 |
| `slot_style` | slot 样式，当前为 `nei_slot`。 |

### `recipe_ui_annotation`

> 重构中移除：动态文字/条件标记（GT 电压、耗时、洁净室等）不再通过专表导出。这些值来自 `gregtech_recipe` 列或 `gregtech_recipe_metadata` 行，后端按 category 派生 `infoSchema`（含 label / valueType / fieldGroup），前端 renderer 按 schema 渲染。

### `recipe`

单条配方。

| 字段 | 说明 |
|---|---|
| `recipe_id` | 配方 ID，格式 `category_id:hash`。 |
| `category_id` | 所属分类。 |
| `source_plugin` | 来源插件。 |
| `source_ref` | 原始内部 recipe key。 |

### `recipe_source`

配方来源追踪。

| 字段 | 说明 |
|---|---|
| `recipe_id` | 配方。 |
| `source_type` | 来源类型：`vanilla_crafting`、`vanilla_furnace`、`gregtech_recipe_map`、`nei_handler` 等。 |
| `source_mod_id` | 来源 Mod/插件。 |
| `handler_id` | NEI/导出器处理器 ID。 |
| `source_class` | 原始类名；当前可为空。 |
| `source_map_id` | 来源 recipe map ID。 |
| `source_map_name` | 来源 recipe map 名称。 |
| `raw_recipe_key` | 原始 recipe key。 |
| `owner_mod_ids` | GT 等来源提供的 owner mod 列表 JSON。 |

### `ingredient_group`

配方输入候选组，可以是物品组或流体组。

| 字段 | 说明 |
|---|---|
| `group_id` | 候选组 ID。 |
| `domain` | `item` 或 `fluid`。 |
| `kind` | `explicit`、`ore_dictionary` 等。 |
| `source_ref` | 原始组或基础组引用。 |

### `ingredient_entry`

候选组成员。

| 字段 | 说明 |
|---|---|
| `group_id` | 候选组。 |
| `item_variant_id` | 物品成员；流体组为空。 |
| `fluid_variant_id` | 流体成员；物品组为空。 |
| `amount` | 物品堆叠数或流体 mB。 |

### `recipe_slot`

配方的实际输入/输出槽。

| 字段 | 说明 |
|---|---|
| `recipe_id` | 配方。 |
| `role` | `item_input`、`fluid_input`、`item_output`、`fluid_output`。 |
| `slot_index` | slot 序号。 |
| `group_id` | 输入候选组；输出为空。 |
| `item_variant_id` | 输出物品；输入组为空。 |
| `fluid_variant_id` | 输出流体；输入组为空。 |
| `amount` | 输出数量；输入组数量在 `ingredient_entry`。 |
| `probability` | 输出概率。 |

### `recipe_lookup_index`

前端快速实现 NEI 的 R/U 查询。

| 字段 | 说明 |
|---|---|
| `target_domain` | 查询目标域：`item` 或 `fluid`。 |
| `target_id` | item/fluid variant ID。 |
| `lookup_kind` | `recipe` 表示查合成，`usage` 表示查用途。 |
| `recipe_id` | 命中的配方。 |
| `category_id` | 命中的分类。 |
| `role` | 命中的 slot role。 |
| `slot_index` | 命中的 slot。 |
| `group_id` | 输入组 ID；输出为空。 |
| `amount` | 数量。 |
| `probability` | 概率。 |
| `display_order` | 前端显示顺序。 |

## Forge / GregTech / MobInfo / Quest / Thaumcraft

### `ore_dictionary`

| 字段 | 说明 |
|---|---|
| `ore_name` | 矿辞名。 |
| `group_id` | 对应物品候选组。 |

### `fluid_container`

| 字段 | 说明 |
|---|---|
| `fluid_variant_id` | 容器内流体。 |
| `amount` | 流体量 mB。 |
| `container_item_variant_id` | 满容器物品。 |
| `empty_container_item_variant_id` | 空容器物品。 |

### `fluid_block`

| 字段 | 说明 |
|---|---|
| `fluid_variant_id` | 流体。 |
| `block_item_variant_id` | 对应流体方块物品。 |

### `gregtech_recipe`

GregTech 配方的固定列。voltage 三元字段对 fuel 配方为空（`RecipeKind.FUEL`）。其余 GT 特性（洁净室、低重力、coil_heat、燃烧值、material 等）一律下沉到 [`gregtech_recipe_metadata`](#gregtech_recipe_metadata)，**不再在本表占列**。

| 字段 | 说明 |
|---|---|
| `recipe_id` | 对应通用配方。 |
| `recipe_kind` | `PROCESSING` 或 `FUEL`。 |
| `visible_in_nei` | 是否在 NEI 中可见。 |
| `voltage_tier` | 电压等级，例如 `LV`、`HV`；fuel 为空。 |
| `voltage` | EU/t；fuel 为空。 |
| `amperage` | 安培；fuel 为空。 |
| `duration_ticks` | 耗时 tick。 |
| `special_value` | 上游 `GTRecipe.mSpecialValue` 原值；语义因 RecipeMap 而异，由后端按 category 解释。 |

### <a id="gregtech_recipe_metadata"></a>`gregtech_recipe_metadata`

GregTech 配方的可变 metadata，按 (recipe, key) 一行。导出器**不解释语义**：原样落库，由后端按 category 的 `infoSchema` 决定 label / valueType / 展示分组，前端按 valueType 选 renderer。

| 字段 | 说明 |
|---|---|
| `recipe_id` | 对应 GT 配方。 |
| `metadata_key` | metadata 键，例如 `coil_heat`、`fuel_value`、`material`。 |
| `value_type` | 原始值类型：`bool` / `int` / `long` / `float` / `double` / `enum` / `json`。前端按此选 renderer。 |
| `value_text` | 标量字面值（`int` / `long` / `float` / `double` / `bool` / `enum`）；`json` 为空。 |
| `value_json` | `value_type = json` 时的结构化值（jsonb），例如 `material`、`pcb_nanite_material`、`solar_factory_wafer_data`、`quantum_computer_data`。其余类型为空。 |

当前观察到的 metadata key（`gtnh:2.8.4:official` 数据集，22 个）：

| key | value_type | 大致含义 |
|---|---|---|
| `recycle` | bool | 是否为回收配方。 |
| `cleanroom` | bool | 是否要求洁净室。 |
| `low_gravity` | bool | 是否要求低重力。 |
| `no_gas` | bool | 是否禁用气态 circuit。 |
| `additives` | int | 添加剂数量/标志。 |
| `coil_heat` | int | 线圈热容（K）。 |
| `compression_tier` | int | 压缩机 tier。 |
| `eu_multiplier` | int | EU 倍率。 |
| `fuel_type` | int | 燃料类型枚举。 |
| `fuel_value` | int | 燃烧值（EU）。 |
| `nano_forge_tier` | int | 纳米锻造 tier。 |
| `no_gas_circuit_config` | int | 无气体配方下的 circuit 编号。 |
| `pcb_factory_tier` | int | PCB 工厂 tier。 |
| `fusion_threshold` | long | 聚变阈值（EU）。 |
| `purification_plant_base_chance` | float | 净化厂基础几率。 |
| `half-life` | double | 衰变半衰期。 |
| `decay-type` | enum | 衰变类型。 |
| `pcb_factory_bio_upgrade` | enum | PCB 生物升级类型。 |
| `material` | json | 物料引用（mod_id + name）。 |
| `pcb_nanite_material` | json | PCB 纳米机器人物料。 |
| `quantum_computer_data` | json | 量子计算机配方数据。 |
| `solar_factory_wafer_data` | json | 太阳能厂晶圆数据。 |

> 列表会随 mod 版本变化。后端按 category 注册的 `infoSchema` 是这张表的“词典”：未在 schema 中登记的 key 仍可读出，前端按 valueType 走通用 renderer 兜底显示。


### `gregtech_recipe_special_item` / `gregtech_recipe_mod_owner`

| 字段 | 说明 |
|---|---|
| `recipe_id` | 对应 GT 配方。 |
| `list_index` | 列表顺序。 |
| `item_variant_id` | 特殊物品，仅 `gregtech_recipe_special_item`。 |
| `mod_id` | owner mod，仅 `gregtech_recipe_mod_owner`。 |

### `mob_info` / `mob_drop` / `mob_spawn_info`

| 表 | 字段说明 |
|---|---|
| `mob_info` | `mob_variant_id` 生物变体；`allowed_in_peaceful` 是否和平可用；`soul_vial_usable` 是否可灵魂瓶；`allowed_infernal`/`always_infernal` Infernal 信息。 |
| `mob_drop` | `drop_type` 掉落类型；`item_variant_id` 掉落物；`stack_size` 数量；`probability` 概率；`lootable` 是否受抢夺影响；`player_only` 是否玩家击杀限定。 |
| `mob_spawn_info` | `spawn_info` 原始生成信息文本。 |

### `quest_line` / `quest` / 任务和奖励表

| 表 | 字段说明 |
|---|---|
| `quest_line` | 任务线 ID、图标、名称、描述、可见性。 |
| `quest` | BQ 任务 ID、图标、名称、描述、可见性、重复时间、任务逻辑。 |
| `quest_dependency` | 任务依赖关系。 |
| `quest_line_entry` | 任务在线内的坐标和尺寸。 |
| `quest_task` | 任务条目：类型、是否消耗、目标生物、数量、维度。 |
| `quest_task_item` | 任务物品需求候选组。 |
| `quest_task_fluid` | 任务流体需求。 |
| `quest_reward` | 奖励条目：类型、命令、经验、完成任务 ID。 |
| `quest_reward_item` | 奖励物品候选组。 |

### `aspect` / `aspect_component` / `aspect_item`

Thaumcraft aspect 数据。

| 表 | 字段说明 |
|---|---|
| `aspect` | Aspect ID、图标、名称、描述、是否 primal。 |
| `aspect_component` | 复合 aspect 的组成关系。 |
| `aspect_item` | 物品包含的 aspect 和数量。 |

## GregTech NEI Ore Plugin

来自 `gtneioreplugin` 模块（gtneioreplugin.util.GT5OreLayerHelper / GT5OreSmallHelper / GT5UndergroundFluidHelper），用于回答“这种矿/流体在哪个维度怎么生成”。

### `gt_ore_vein`

GT 大矿脉档案，每个矿脉一行。

| 字段 | 说明 |
|---|---|
| `vein_name` | 矿脉内部名（如 `ore.mix.copper`）。 |
| `display_name` | 矿脉本地化名（已是导出时 locale 的最终中文）。 |
| `weight` | 生成权重（生成概率）。 |
| `size` | 矿脉大小。 |
| `density` | 矿脉密度。 |
| `height_min` / `height_max` | 生成高度范围（解析自 `worldGenHeightRange` "min-max"）。 |

### `gt_ore_vein_layer`

矿脉每一层（主层 / 副层 / 夹层 / 零星层）对应的矿物材料。

| 字段 | 说明 |
|---|---|
| `vein_name` | 所属矿脉。 |
| `layer` | `primary` / `secondary` / `between` / `sporadic`。 |
| `material_name` | GT 材料枚举名（如 `Copper`、`Iron`）。 |
| `ore_meta` | 矿石方块 metadata（`GregTechAPI.sBlockOres1` 的 sub-id）。 |
| `block_item_variant_id` | 派生的矿石方块物品 variant ID（前端可显示图标）。 |

### `gt_ore_vein_dimension`

矿脉允许生成的维度列表。

| 字段 | 说明 |
|---|---|
| `vein_name` | 所属矿脉。 |
| `dimension` | 维度缩写（`Ow` / `Ne` / `Ma` 等，来自 GT 的 `DimensionHelper` 缩写表）。 |
| `enabled` | 该维度该矿脉是否启用。 |

### `gt_ore_small`

GT 贫瘠矿（散落小矿块）档案。

| 字段 | 说明 |
|---|---|
| `ore_gen_name` | 小矿内部名。 |
| `ore_meta` | 矿石方块 metadata。 |
| `material_name` | GT 材料枚举名。 |
| `amount_per_chunk` | 每个区块生成数量。 |
| `height_min` / `height_max` | 生成高度范围。 |

### `gt_ore_small_dimension`

小矿允许生成的维度列表（结构同 `gt_ore_vein_dimension`）。

### `gt_underground_fluid`

地下流体在各维度的产出。

| 字段 | 说明 |
|---|---|
| `fluid_id` | 流体注册名。 |
| `fluid_variant_id` | 关联到 `fluid_variant` 的 ID（如能反查）。 |
| `dimension` | 维度名。 |
| `chance` | 该流体在该维度的生成几率，按千分比表达（10000 = 100%）。 |
| `min_amount` / `max_amount` | 每抽产量范围。 |

## NEI Custom Diagram

来自 `nei-custom-diagram` mod，用于电路图谱 / 矿物处理流程图 / 透镜对照表 / 材料零件总览 / OreDict 同义词等结构化图谱。**不是配方**，需独立模型。

### `diagram_group`

每个图谱组（一个 `DiagramGenerator` 对应一组）。当前可能出现的 group：`gregtech.5.circuits` / `gregtech.5.lenses` / `gregtech.5.materialparts` / `gregtech.5.materialtools` / `gregtech.5.oreprocessing` / `gregtech.5.oredictionary` / `gregtech.5.oreprefixes`。

| 字段 | 说明 |
|---|---|
| `group_id` | 组 ID。 |
| `display_name` | 组显示名（已本地化）。 |
| `description` | 组描述。 |
| `icon_item_variant_id` | 组图标对应的物品 variant ID。 |
| `diagrams_per_page` | 每页显示几张图。 |
| `ignore_nbt` | 匹配时是否忽略 NBT。 |
| `default_visibility` | 默认可见性枚举。 |

### `diagram`

一个图谱组内的单张图。

| 字段 | 说明 |
|---|---|
| `group_id` | 所属组。 |
| `diagram_id` | 组内唯一 ID，格式 `<group_id>:d<index>`。 |
| `display_order` | 在组内的显示顺序。 |
| `width` / `height` | 画布尺寸（来自 `layout.maxDimension`）。 |

### `diagram_slot`

图内静态槽位的拓扑（来自 `Layout.slots` + `Layout.slotGroups`）。

| 字段 | 说明 |
|---|---|
| `slot_key` | 槽位 key；slotGroup 内的槽位 key 形如 `<groupKey>[<index>]`。 |
| `slot_group_key` | 所属 slotGroup key；独立槽位为空。 |
| `slot_index` | 在 slotGroup 内的序号；独立槽位为 0。 |
| `x` / `y` / `width` / `height` | 槽位位置和尺寸。 |
| `tooltip_text` | 槽位 tooltip（暂未抓取，留空）。 |

### `diagram_slot_entry`

槽位里塞的物品/流体（来自 `InteractiveComponentGroup.components`，反射读 protected 字段）。注意：图谱里的"槽位"key 实际可能是 `interactable[<i>]:<x>,<y>`（动态产生的可交互组）。

| 字段 | 说明 |
|---|---|
| `slot_key` | 关联到 `diagram_slot`，或动态槽位 key。 |
| `entry_index` | 槽位内候选序号。 |
| `component_type` | `item` / `fluid` / `other`。 |
| `item_variant_id` | 物品 variant；非物品为空。 |
| `fluid_variant_id` | 流体 variant；非流体为空。 |
| `stack_size` | 数量（来自 `DisplayComponent.stackSize`）。 |
| `additional_info` | DisplayComponent 上的附加信息文本。 |
| `tooltip_text` | DisplayComponent 的 tooltip（暂未抓取，留空）。 |

### `diagram_label`

图上的标签/装饰文字（`CustomInteractable` 实例）。

| 字段 | 说明 |
|---|---|
| `label_index` | 序号。 |
| `x` / `y` | 位置。 |
| `text` | 文字（当前未提取，留空）。 |
| `icon_item_variant_id` | 关联物品图标（当前未提取，留空）。 |

### `diagram_line`

图上的连线（当前未导出，预留表）。

### `diagram_match`

哪些物品/流体（点击后）触发显示这张图。来自 `ComponentDiagramMatcher.matchData` 反射读出。仅 `ComponentDiagramMatcher` 类型的 matcher 可静态导出；其它 matcher（如 `CustomDiagramMatcher`）需要运行时匹配，本表为空。

| 字段 | 说明 |
|---|---|
| `recipe_type` | `CRAFTING` / `USAGE`。 |
| `component_type` | `item` / `fluid` / `other`。 |
| `item_variant_id` / `fluid_variant_id` | 触发该 diagram 的组件。 |

## NEI 贴图导出

### `nei_texture_export`

记录所有从游戏 ResourceManager 抠出来的 PNG 文件。来源：`recipe_category.icon_image_resource` 字段引用到的 ResourceLocation；导出器在所有插件 postProcess 后统一去重并落盘到 `exports/<dataset>/nei-texture/<domain>/<path>`。

| 字段 | 说明 |
|---|---|
| `resource` | 完整 ResourceLocation，格式 `domain:path`。 |
| `domain` / `path` | 拆解的两段。 |
| `exported_path` | 相对 `exports/<dataset>/` 的路径，前端 fetch 用。 |
| `sha256` | PNG 内容 hash（用于缓存校验）。 |
| `width` / `height` | PNG 整张图尺寸。 |

## 外置文件（不进 PG）

- `exports/<dataset>/nei-display-snapshot/<category>.jsonl`：GT 每条 recipe 在 NEI 上实际渲染的中文文本行。每行一条 recipe：`{"recipe_id":"...","lines":["电压: 32 EU/t","耗时: 10s",...]}`。**只覆盖 GT 系**（其它 mod handler 不走 `RecipeDisplayInfo`）。供前后端开发对照 NEI 真实展示文案，不在运行时查询。
- `exports/<dataset>/nei-texture/<domain>/<path>`：贴图 PNG 物理文件，路径与 ResourceLocation 一一对应。

## 当前实现边界

- UI 模板 / 模板素材表已被移除。前端按 `category_id` 选 renderer，`recipe_category` / `recipe_category_layout` 给出 slot 网格尺寸和画布尺寸，复杂 UI 由前端组件直接实现。NEI / Mod 原始 GUI 纹理仍按 ResourceLocation 拷入 `assets.zip`，通过 `asset_source` 暴露；NEI handler 自带的小图标贴图走 `nei_texture_export` 表。
- GT 的 category 已按机器合并：同一机器的 15 个电压共用 1 个 `category_id`，电压退化为 `gregtech_recipe.voltage_tier` / `voltage` / `amperage`。fuel category 同样按机器合并。
- GT 配方的可变属性（洁净室、低重力、coil_heat、fuel_value、material 等）全部走 `gregtech_recipe_metadata` 键值表；导出器不解释语义，后端按 category 的 `infoSchema` 决定如何展示。
- NEI handler 元数据（图标、画布尺寸、所属 mod 等）从 `GuiCraftingRecipe.craftinghandlers` / `GuiUsageRecipe.usagehandlers` 等 NEI 静态注册表 + `GuiRecipeTab.getHandlerInfo` 抓取，落到 `recipe_category` 新增 14 列。匹配 RecipeType ↔ HandlerInfo 走多策略 key（handlerId / overlayIdent / handlerClass），未匹配上的 category 这些列为默认空值。
- GT NEI 渲染文本快照通过子类化 `gregtech.nei.RecipeDisplayInfo`（包内侵入）+ 调用 `RecipeMapFrontend.drawDescription` 抓取，**仅 GT 系**有效。其它 mod handler 未覆盖。
- `nei_item_panel_entry.collapsible_collection_id` 已预留，但 NEI collapsible preset 还未解析，当前为空。
- `asset_source.renderer_class` 当前多数为空，后续可以通过 renderer 反射补充。
- 配方 ID 使用 `category_id + 原始 recipe key hash`，因为大量 NEI/GregTech 配方没有稳定自然主键。
- 多方块结构投影（BlockRenderer6343）暂未导出，留待下一阶段。
