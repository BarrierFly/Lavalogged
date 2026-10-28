# Lavalogged 多版本移植静态检查报告

对照 [guardian](../../guardian)（Minecraft 反编译源码库）的四个版本分支逐一核对
`lavalogged` / `waterlogged` 属性相关代码。核对方法：

1. 解析每个版本 `net.minecraft(.world.level).block` 包内全部类声明，构建继承图；
2. 计算水登录接口（yarn `Waterloggable` / mojmap `SimpleWaterloggedBlock`）的**直接实现类**
   与**传递闭包**（所有含水子类）；
3. 对每个闭包成员检查是否在**自身源码**中声明目标方法（`onUse` / `useItemOn` /
   `getStateForNeighborUpdate` / `getPlacementState` 等）——mixin 只能注入自己声明的方法；
4. 逐一核对各版本 API 签名与字段结构（见下文"机制核对"）。

## 版本矩阵

| 版本 | mappings | Java | 属性后端 | waterlogged 直接实现类数 |
| --- | --- | --- | --- | --- |
| 1.19.4 | yarn `1.19.4+build.2` | 17 | Statement 4.2.9（单 jar 覆盖 1.14.4–1.20.4） | 33 |
| 1.20.1 | yarn `1.20.1+build.10` | 17 | Statement 4.2.9 | 34（+DecoratedPotBlock） |
| 1.21.11 | 官方映射（共用 26.3 源码） | 21 | 模组自带 `LavaStateRefresher` | 39 |
| 26.3 | 无混淆（`fabric.loom.disableObfuscation=true`） | 21 | 模组自带 `LavaStateRefresher` | 39（SpeleothemBlock 换名） |

## 上游代码的问题与修正

上游（2024-09 快照，目标 1.20.4）的类清单与"所有含水方块都可 lavalog"的设计意图不完全一致。
逐项核对结果：

### 已修正（按版本）

| 问题 | 说明 | 修正 |
| --- | --- | --- |
| `GrateBlock` / `BarrierBlock` 在 1.19.4 / 1.20.1 不可含水 | Barrier 自 1.21 起才有 waterlogged；Grate 自 1.21 才存在 | 这两个版本从 `Lavaloggable` 中移除（1.19.4 同时移除 1.20 才有的 `DecoratedPotBlock`） |
| `BigDripleafStemBlock` 遗漏 | 所有版本均为含水直接实现类，上游清单缺失 | 四个版本全部加入 |
| `LightBlock`、`StairsBlock`、`HangingSignBlock`、`WallHangingSignBlock` 遗漏于桶交互 | 它们各自声明 `onUse`（1.21.11 为 `onUseWithItem`），不注入时基类拦截被覆写，方块无法用岩浆桶/桶交互 | 按版本加入 `BucketUseMixin` |
| `SculkShriekerBlock`、`AbstractSignBlock`、`CoralBlock` 遗漏于邻居更新 | 均声明 `getStateForNeighborUpdate`，缺失时对应方块 lavalog 后岩浆不更新流动 | 按版本加入 `NeighborUpdateMixin` |
| `CalibratedSculkSensorBlock` 遗漏于放置 | 1.20.1+ 声明 `getPlacementState` | 1.20.1 加入 |
| `CoralBlockBlock` 等 | 其继承自 `Block` 而非 `CoralParentBlock`，不可含水 | 不加入（确认上游未加入是对的） |
| `BlockMixin` 为死代码 | 其注入的 `Block#setDefaultState` 在属性添加前运行，守卫恒假；Statement 的重建流程也不依赖它 | 移除整个文件与 mixins.json 条目 |
| 上游未声明 Statement 依赖 | `fabric.mod.json` 缺 `"statement"` depends，运行时会缺库崩溃 | 1.19.4 / 1.20.1 补上；1.21.11 / 26.3 无此依赖 |

### 1.21.11 / 26.3 的新清单要点

* 珊瑚：yarn `CoralParentBlock` → mojmap `BaseCoralPlantTypeBlock`；
* 亚麻石/发光地衣：`GlowLichenBlock` → `MultifaceBlock`（`GlowLichenBlock`、
  `MultifaceGrowthBlock`、`SculkVeinBlock` 均为其子类，由父类覆盖）；
* 1.21+ 新增含水方块：`BarrierBlock`、`GrateBlock`/`WaterloggedTransparentBlock`（26.3 改名）、
  `CopperGolemStatueBlock`、`DriedGhastBlock`、`HeavyCoreBlock`、`ShelfBlock`；
* 26.3 改名：`PointedDripstoneBlock` → `SpeleothemBlock`、`PropaguleBlock` → `MangrovePropaguleBlock`、
  `StairsBlock` → `StairBlock`、`TrapdoorBlock` → `TrapDoorBlock`、`HorizontalConnectingBlock` → `CrossCollisionBlock`；
* `CrafterBlock` / `VaultBlock`：1.21.11 / 26.3 均无 waterlogged 属性（1.21.11 方块状态
  JSON 与类声明都已确认），不加入。

## 机制核对（1.21.11 / 26.3 自带属性注入）

上游依赖 Statement 追加属性；其官方构建止步 1.20.4。1.21.11 / 26.3 由模组自带
`org.lavalogged.state.LavaStateRefresher` 完成，核心步骤与逐版本差异如下：

| 环节 | 1.21.11（mojmap） | 26.3 |
| --- | --- | --- |
| 值存储 | `StateHolder.values: Reference2ObjectArrayMap`（可变 map，直接 put） | `propertyKeys[] / propertyValues[]` 并行数组（重建数组替换，`@Mutable`） |
| 转移表 | `neighbours: Map<Property<?>, S[]>`，置 null 后 `populateNeighbours(...)` 重建 | `neighbors: S[][]` 矩阵，按 `getPossibleValues()` 序号手工重建 |
| codec | 每状态一个 `propertiesCodec`，逐状态替换 | 移至 `StateDefinition.propertiesCodec`，在定义上替换 |
| 新状态构造 | `factory.create(owner, map, codec)` | `factory.create(owner, keys[], values[])` |
| 注册 | 新状态按注册表顺序追加进 `Block.BLOCK_STATE_REGISTRY`（mojmap 名，yarn 为 `STATE_IDS`）；`getId() == -1` 才 add，**既有 id 全部保持不变** | 同左 |

关键正确性要点（均已在实现中落实）：

1. **id 稳定性**：只追加新状态，原状态对象原样保留 → 客户端/服务端只要按同一注册表顺序
   执行就得到一致 id；连接原版服务器时原版 id 前缀不受影响；旧存档不受影响。
2. **默认状态对象不变**：新状态表 = 旧状态列表 + 新变体，`states.get(0)` 仍是原默认状态，
   `Block.defaultBlockState` 字段无需改动。
3. **`initCache()` 必须手动调用**：mojmap 两个版本的 `BlockStateBase.fluidState` 都是在
   `initCache()`（vanilla 于 `Blocks` 类初始化时调用）里缓存的，新状态不调用则
   `getFluidState()` 永远返回空 → 不调用 `AbstractBlockMixin.getFluidState` 的注入就看不到岩浆。
   Statement 老版本对应 `statement_initShapeCache`，1.19.4 / 1.20.1 由 Statement 自己处理。
4. **codec 重建**：经 `@Invoker appendPropertyCodec` 链式重建，使 `/data`、世界生成 JSON、
   NBT 磁盘路径能解析 `lavalogged` 字段；NBT 读写本身（`NbtHelper.toBlockState/fromBlockState`）
   是动态遍历 state manager 属性的，属性加进 manager 后天然兼容。
5. **方块状态 JSON 无需改动**：现代版本的 blockstate 变体键只包含影响模型用的属性
   （如 `acacia_slab.json` 只写 `type`），`lavalogged=true` 的状态按其余属性命中同一模型，
   渲染与原版一致（岩浆由流体渲染层绘制）。
6. **网络交互**：26.3 起挥动手臂由 `InteractionResult.SUCCESS`（`SwingSource`）驱动，
   不再手动 `swing`；流体 tick 名为 `getTickDelay(LevelReader)`（yarn 为 `getTickRate`）；
   `Fluids.LAVA.defaultFluidState()` 替代 yarn 的 `getStill(false)`。

## 与 guardian 分支的对照方式

```
guardian（yarn/vineflower 分支）：  be0c9cb421 = 1.19.4   44b34e1b42 = 1.20.1   a67a152b87 = 1.21.11
guardian（mojmap/vineflower 分支）：192e7132b4 = 1.21.11   d5822ca2a1 = 26.3
```

每个版本独立跑"直接实现 + 传递闭包 + 方法声明矩阵"三份脚本，交叉验证后生成最终清单。
1.19.4 源码里已含 1.20 实验内容（樱花树、饰纹罐、悬挂木牌等类），但 `DecoratedPotBlock`
在 1.19.4 未实现 `Waterloggable`，仍按 1.19.4 实际含水关系处理。

## 构建结果

```
./gradlew :1.19.4:build :1.20.1:build :1.21.11:build :26.3:build
```

| 产物 | 类 | Statement 依赖 | mixins 配置 |
| --- | --- | --- | --- |
| `versions/1.19.4/build/libs/Lavalogged-1.0.0+1.19.4.jar` | 6 | depends 声明 | 1 个（5 个 mixin） |
| `versions/1.20.1/build/libs/Lavalogged-1.0.0+1.20.1.jar` | 6 | depends 声明 | 1 个（5 个 mixin） |
| `versions/1.21.11/build/libs/Lavalogged-1.0.0+1.21.11.jar` | 11 | 无 | 2 个（+状态注入 2 mixin） |
| `versions/26.3/build/libs/Lavalogged-1.0.0+26.3.jar` | 11 | 无 | 2 个（+状态注入 2 mixin） |

四个版本的 stonecutter 指令展开均已抽查（1.19.4 正确剔除 `DecoratedPotBlock`、
26.3 正确切换 `SpeleothemBlock` 与并行数组实现）。mixin 运行期应用情况需在游戏内验证。
